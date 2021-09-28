(function ($, window) {
    var endTime;
    var startTime;
    var myTable;
    var myChart;
    // 车辆list
    var vehicleList = JSON.parse($("#vehicleList").attr("value"));
    var zTreeIdJson = {};
    var size; //当前权限监控对象数量
    var checkFlag = false;
    var ifAllCheck = true;

    var terminal = [];//终端数据
    var ioCollectOne = [];//I/O采集1数据
    var ioCollectTwo = [];//I/O采集2数据
    var curChart = 0;//当前图表
    var curTable = 0;//当前表格
    //用于ie浏览器判断选择节点树时是否需要调用搜索功能
    var isSearch = true;

    switchSignalStatistics = {
        init: function () {
            if (myChart)
                myChart.clear();
            myChart = echarts.init(document.getElementById('sjcontainer'));
            var chartData = [];
            switch (curChart) {
                case 0:
                    chartData = terminal;
                    break;
                case 1:
                    chartData = ioCollectOne;
                    break;
                case 2:
                    chartData = ioCollectTwo;
                    break;
            }

            // 组装图表数据
            var seriesData = [];// 显示line数据
            var xData = [];// x轴显示数据
            var legendData = [];// legend显示字段
            var legendColor = ['rgb(99,204,247)', 'rgb(160,224,39)'];
            var len = chartData.length;
            if (len > 0) {
                var mileObj = {
                    name: '里程',
                    type: 'line',
                    symbol: "none",
                    showSymbol: false,
                    yAxisIndex: 0,
                    data: [],
                    lineStyle: {
                        color: 'rgb(99,204,247)'
                    }
                };
                var speedObj = {
                    name: '速度',
                    type: 'line',
                    symbol: "none",
                    showSymbol: false,
                    yAxisIndex: 1,
                    data: [],
                    lineStyle: {
                        color: 'rgb(160,224,39)'
                    }
                };
                legendData.push('里程', '速度');

                var info = chartData[0].ioInfo;
                var infoLen = info.length;
                if (infoLen > 0) {
                    // 组装需要显示的线段(每个状态由4条不同颜色线段组成)
                    for (var j = 0; j < infoLen; j++) {
                        var objState1 = {
                            name: info[j].columnName,
                            type: 'line',
                            symbol: "none",
                            yAxisIndex: 2,
                            state: 1,
                            index: j,
                            data: [],
                            lineStyle: {
                                color: '#FF9ABA'
                            }
                        };
                        var objState2 = {
                            name: info[j].columnName,
                            type: 'line',
                            symbol: "none",
                            showSymbol: false,
                            yAxisIndex: 2,
                            state: 2,
                            index: j,
                            data: [],
                            lineStyle: {
                                color: '#344453'
                            }
                        };
                        var objState3 = {
                            name: info[j].columnName,
                            type: 'line',
                            symbol: "none",
                            showSymbol: false,
                            yAxisIndex: 2,
                            state: 3,
                            index: j,
                            data: [],
                            lineStyle: {
                                color: '#CC99FF'
                            }
                        };
                        var objState4 = {
                            name: info[j].columnName,
                            type: 'line',
                            symbol: "none",
                            showSymbol: false,
                            yAxisIndex: 2,
                            state: 4,
                            index: j,
                            data: [],
                            lineStyle: {
                                color: '#EAEAEA'
                            }
                        };

                        legendColor.push('#61a0a8');
                        seriesData.push(objState1, objState2, objState3, objState4);
                        legendData.push(info[j].columnName);
                    }
                }
                // 组装series显示数据
                for (var i = 0; i < len; i++) {
                    if (infoLen > 0) {
                        for (var j = 0; j < infoLen; j++) {
                            var stateValue = Math.floor(180 / (infoLen + 1));
                            var state = chartData[i].ioInfo[j].state;
                            var prevState = null;//前一个状态
                            var nextState = null;//后一个状态
                            if (i > 0) {
                                prevState = chartData[i - 1].ioInfo[j].state;
                            }
                            if (i < len - 1) {
                                nextState = chartData[i + 1].ioInfo[j].state;
                            }
                            var serLen = seriesData.length;
                            for (var k = 0; k < serLen; k++) {
                                if (seriesData[k].index == j) {
                                    var curState = seriesData[k].state;
                                    if (curState == state) {
                                        seriesData[k].data.push({
                                            value: stateValue + (infoLen - j - 1) * stateValue,
                                            state: state,
                                            stateName: chartData[i].ioInfo[j].stateName
                                        });
                                        // if (nextState != null && curState != nextState) {
                                        //     seriesData[k].data.push({
                                        //         value: stateValue + (infoLen - j - 1) * stateValue,
                                        //         state: state,
                                        //         stateName: chartData[i].ioInfo[j].stateName
                                        //     });
                                        // }
                                    }
                                    else if (curState != prevState) {
                                        seriesData[k].data.push({
                                            value: '-',
                                            state: state,
                                            stateName: chartData[i].ioInfo[j].stateName
                                        });
                                    } else {
                                        seriesData[k].data.push({
                                            value: stateValue + (infoLen - j - 1) * stateValue,
                                            state: state,
                                            stateName: chartData[i].ioInfo[j].stateName
                                        });
                                    }
                                    if (k < serLen - 1 && seriesData[k + 1].index != j) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    xData.push(chartData[i].time);
                    mileObj.data.push(chartData[i].gpsMile == 'null' ? 0 : chartData[i].gpsMile);
                    speedObj.data.push(chartData[i].speed == 'null' ? 0 : chartData[i].speed);
                }
                seriesData.unshift(speedObj);
                seriesData.unshift(mileObj);
            }
            var option = {
                tooltip: {// 图表鼠标悬停样式
                    trigger: 'axis',
                    formatter: function (a) {
                        var oldLen = a.length;
                        var newArr = [];
                        var html = '';
                        if (oldLen == 0) {
                            html = '无相关数据'
                        } else if (0 < oldLen && oldLen <= 2) {
                            newArr = a;
                        } else {
                            for (var i = 0; i < oldLen - 1; i++) {
                                if (a[i].seriesName != a[i + 1].seriesName) {
                                    newArr.push(a[i]);
                                }
                            }
                            newArr.push(a[oldLen - 1]);
                        }
                        var len = newArr.length;

                        if (0 < len && len < 12) {
                            html = '<h4 style="margin: 5px 0 -10px;padding: 0">' + newArr[0].name + '</h4>';
                            for (var i = 0; i < len; i++) {
                                if (newArr[i].seriesName == '里程') {
                                    html += '<br/>' + newArr[i].seriesName + '：' + newArr[i].data + 'km';
                                } else if (newArr[i].seriesName == '速度') {
                                    html += '<br/>' + newArr[i].seriesName + '：' + newArr[i].data + 'km/h';
                                } else {
                                    html += '<br/>' + newArr[i].seriesName + '：' + newArr[i].data.stateName;
                                }
                            }
                        } else if (12 <= len && len < 24) {
                            html = '<h4 style="margin: 5px 0 -10px;padding: 0">' + newArr[0].name + '</h4>' + '<span style="float:left">';
                            for (var i = 0; i < 12; i++) {
                                if (newArr[i].seriesName == '里程') {
                                    html += '<br/>' + newArr[i].seriesName + '：' + newArr[i].data + 'km';
                                } else if (newArr[i].seriesName == '速度') {
                                    html += '<br/>' + newArr[i].seriesName + '：' + newArr[i].data + 'km/h';
                                } else {
                                    html += '<br/>' + newArr[i].seriesName + '：' + newArr[i].data.stateName;
                                }
                            }
                            html += '</span><span style="float:left;margin-left: 10px;">';
                            for (var i = 12; i < len; i++) {
                                html += '<br/>' + newArr[i].seriesName + '：' + newArr[i].data.stateName;
                            }
                            html += '</span>';
                        } else if (24 <= len) {
                            html = '<h4 style="margin: 5px 0 -10px;padding: 0">' + newArr[0].name + '</h4>' + '<span style="float:left">';
                            for (var i = 0; i < 12; i++) {
                                if (newArr[i].seriesName == '里程') {
                                    html += '<br/>' + newArr[i].seriesName + '：' + newArr[i].data + 'km';
                                } else if (newArr[i].seriesName == '速度') {
                                    html += '<br/>' + newArr[i].seriesName + '：' + newArr[i].data + 'km/h';
                                } else {
                                    html += '<br/>' + newArr[i].seriesName + '：' + newArr[i].data.stateName;
                                }
                            }
                            html += '</span><span style="float:left;margin-left: 10px;">';
                            for (var i = 12; i < 24; i++) {
                                html += '<br/>' + newArr[i].seriesName + '：' + newArr[i].data.stateName;
                            }
                            html += '</span><span style="float:left;margin-left: 10px;">';
                            for (var i = 24; i < len; i++) {
                                html += '<br/>' + newArr[i].seriesName + '：' + newArr[i].data.stateName;
                            }
                            html += '</span>';
                        }
                        return html;
                    }
                },
                grid: {
                    top: 120,
                },
                // 图表缩放控制
                dataZoom: [{
                    startValue: 0
                }, {
                    type: 'inside'
                }],
                // legend显示颜色
                color: legendColor,
                legend: {
                    top: 20,
                    left: 80,
                    right: 80,
                    data: legendData
                },
                // 声明X轴
                xAxis: {
                    type: 'category',
                    data: xData,
                    boundaryGap: false,
                },
                // 声明Y轴
                yAxis: [{
                    name: '里程(km)',
                    splitLine: {
                        show: false
                    }
                }, {
                    name: '速度',
                    right: 0,
                    min: 0,
                    max: 240,
                    splitLine: {
                        show: false
                    }
                }, {
                    name: '检测功能类型',
                    min: 0,
                    max: 180,
                    show: false,
                    splitLine: {
                        show: false
                    }
                }],
                // 动态控制line每段显示颜色
                // visualMap: colorArr,
                // 绘制多条line
                series: seriesData
            };
            myChart.setOption(option, true);
            //myChart.on('legendselectchanged', switchSignalStatistics.legendEvent);
            window.onresize = myChart.resize;
        },
        treeInit: function () {
            var setting = {
                async: {
                    //url: "/clbs/m/functionconfig/fence/bindfence/getTreeByMonitorCount",
                    url: switchSignalStatistics.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: switchSignalStatistics.ajaxDataFilter
                },
                check: {
                    enable: true,
                    chkStyle: "checkbox",
                    radioType: "all",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    }
                },
                view: {
                    dblClickExpand: false,
                    nameIsHTML: true,
                    countClass: "group-number-statistics"
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    beforeClick: switchSignalStatistics.beforeClickVehicle,
                    onCheck: switchSignalStatistics.onCheckVehicle,
                    beforeCheck: switchSignalStatistics.zTreeBeforeCheck,
                    onExpand: switchSignalStatistics.zTreeOnExpand,
                    //beforeAsync: switchSignalStatistics.zTreeBeforeAsync,
                    onAsyncSuccess: switchSignalStatistics.zTreeOnAsyncSuccess,
                    onNodeCreated: switchSignalStatistics.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
            $("[data-toggle='tooltip']").tooltip();
        },
        //模糊查询树
        searchVehicleTree: function (param) {

            ifAllCheck = false;//模糊查询不自动勾选

            crrentSubV = [];
            $('#charSelect').val('');
            if (param == null || param == undefined || param == '') {
                bflag = true;
                switchSignalStatistics.treeInit()
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType').val(), "queryParam": param},
                        dataFilter: switchSignalStatistics.ajaxQueryDataFilter
                    },
                    check: {
                        enable: true,
                        chkStyle: "checkbox",
                        radioType: "all",
                        chkboxType: {
                            "Y": "s",
                            "N": "s"
                        }
                    },
                    view: {
                        dblClickExpand: false,
                        nameIsHTML: true,
                        countClass: "group-number-statistics"
                    },
                    data: {
                        simpleData: {
                            enable: true
                        }
                    },
                    callback: {
                        beforeClick: switchSignalStatistics.beforeClickVehicle,
                        onCheck: switchSignalStatistics.onCheckVehicle,
                        onExpand: switchSignalStatistics.zTreeOnExpand,
                        onNodeCreated: switchSignalStatistics.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData));
            var nodesArr;
            if ($('#queryType').val() == "vehicle") {
                nodesArr = filterQueryResult(responseData, crrentSubV);
            } else {
                nodesArr = responseData;
            }
            for (var i = 0; i < nodesArr.length; i++) {
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        getTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=allMonitor";
            }
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var rData;
            if (!responseData.msg) {
                rData = responseData;
            } else {
                rData = responseData.msg;
            }
            //if (responseData.msg) {
            var obj = JSON.parse(ungzip(rData));
            var data;
            if (obj.tree != null && obj.tree != undefined) {
                data = obj.tree;
                size = obj.size;
            } else {
                data = obj
            }
            for (var i = 0; i < data.length; i++) {
                data[i].open = true;
            }
            return data;
        },
        zTreeBeforeAsync: function () {
            return bflag;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {

            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            // if(size <= 5000){
            //     treeObj.checkAllNodes(true);
            // }
            if (size <= 5000 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            switchSignalStatistics.getCharSelect(treeObj);

            bflag = false;
        },
        zTreeOnNodeCreated: function (event, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            var id = treeNode.id.toString();
            var list = [];
            if (zTreeIdJson[id] == undefined || zTreeIdJson[id] == null) {
                list = [treeNode.tId];
                zTreeIdJson[id] = list;
            } else {
                zTreeIdJson[id].push(treeNode.tId)
            }
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;

                    json_ajax("post", "/clbs/m/personalized/ico/getCheckedVehicle",
                        "json", false, {"parentId": treeNode.id, "type": treeNode.type}, function (data) {
                            if (data.success) {
                                nodesLength += data.obj;
                            } else {
                                layer.msg(data.msg);
                            }
                        });

                    //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    //节点id
                    var nodeId;
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle") {
                            //查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                            var nd = zTree.getNodeByParam("tId", nodes[i].tId, treeNode);
                            if (nd == null && $.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength += ns.length;
                } else if (treeNode.type == "people" || treeNode.type == "vehicle") { //若勾选的为监控对象
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;
                    //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    //节点id
                    var nodeId;
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle") {
                            if ($.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength = ns.length + 1;
                }
                if (nodesLength > 5000) {
                    layer.msg(maxSelectItem);
                    flag = false;
                }
            }
            if (flag) {
                //若组织节点已被勾选，则是勾选操作，改变勾选操作标识
                if (treeNode.type == "group" && !treeNode.checked) {
                    checkFlag = true;
                }
            }
            return flag;
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                isSearch = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    switchSignalStatistics.validates();
                }, 600);
            }
            if (treeNode.type != "assignment" || (treeNode.type == "assignment" && treeNode.children != undefined)) {
                switchSignalStatistics.getCharSelect(zTree);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
        },
        zTreeOnExpand: function (event, treeId, treeNode) {
            //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if (treeNode.type == "group" && !checkFlag) {
                return;
            }
            //初始化勾选操作判断表示
            checkFlag = false;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (treeNode.type == "group") {
                var url = "/clbs/m/functionconfig/fence/bindfence/putMonitorByGroup";
                json_ajax("post", url, "json", false, {
                    "groupId": treeNode.id,
                    "isChecked": treeNode.checked,
                    "monitorType": "vehicle"
                }, function (data) {
                    var result = data.obj;
                    if (result != null && result != undefined) {
                        $.each(result, function (i) {
                            var pid = i; //获取键值
                            var chNodes = result[i] //获取对应的value
                            var parentTid = zTreeIdJson[pid][0];
                            var parentNode = treeObj.getNodeByTId(parentTid);
                            if (parentNode.children === undefined) {
                                treeObj.addNodes(parentNode, []);
                            }
                        });
                    }
                })
            }
        },
        getCharSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $("#groupSelect").val(allNodes[0].name);
            } else {
                $("#groupSelect").val("");
            }
            $("#charSelect").val("").attr("data-id", "").bsSuggest("destroy");
            var arrays = [];
            for (var i = 0; i < nodes.length; i++) {
                if (nodes[i].type == "vehicle" || nodes[i].type == "people" || nodes[i].type == "thing") {
                    for (var k = 0; k < vehicleList.length; k++) {
                        if (nodes[i].id == vehicleList[k].vehicleId) {
                            arrays.push({
                                name: nodes[i].name,
                                id: nodes[i].id
                            });
                        }
                    }
                }
            }
            // 去掉数组中id相同的元素
            arrays = objArrRemoveRepeat(arrays);
            var deviceDataList = {value: arrays};
            $("#charSelect").empty();
            $("#charSelect").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: deviceDataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on("click", function () {
            }).on('onSetSelectValue', function (e, keyword, data) {
            }).on('onUnsetSelectValue', function () {
            });
            if (deviceDataList.value.length > 0) {
                $("#charSelect").val(deviceDataList.value[0].name).attr("data-id", deviceDataList.value[0].id);
            }
            $("#groupSelect,#groupSelectSpan").bind("click", switchSignalStatistics.showMenu);
            $("#button").removeClass('disabled loading-state-button').find('i').attr("class", 'caret');
        },
        unique: function (arr) {
            var result = [], hash = {};
            for (var i = 0, elem; (elem = arr[i]) != null; i++) {
                if (!hash[elem]) {
                    result.push(elem);
                    hash[elem] = true;
                }
            }
            return result;
        },
        showMenu: function (e) {
            if ($("#menuContent").is(":hidden")) {
                var inpwidth = $("#groupSelect").outerWidth();
                $("#menuContent").css("width", inpwidth + "px");
                $(window).resize(function () {
                    var inpwidth = $("#groupSelect").outerWidth();
                    $("#menuContent").css("width", inpwidth + "px");
                })
                $("#menuContent").slideDown("fast");
            } else {
                $("#menuContent").is(":hidden");
            }
            $("body").bind("mousedown", switchSignalStatistics.onBodyDown);
        },
        hideMenu: function () {
            $("#menuContent").fadeOut("fast");
            $("body").unbind("mousedown", switchSignalStatistics.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "menuContent" || $(event.target).parents("#menuContent").length > 0)) {
                switchSignalStatistics.hideMenu();
            }
        },
        // 开始时间
        startDay: function (day) {
            var timeInterval = $('#timeInterval').val().split('--');
            var startValue = timeInterval[0];
            var endValue = timeInterval[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * day;
                today.setTime(targetday_milliseconds); // 注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = switchSignalStatistics.doHandleMonth(tMonth + 1);
                tDate = switchSignalStatistics.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * parseInt(num);
                today.setTime(end_milliseconds); // 注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = switchSignalStatistics.doHandleMonth(endMonth + 1);
                endDate = switchSignalStatistics.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " " + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = switchSignalStatistics.doHandleMonth(vMonth + 1);
                vDate = switchSignalStatistics.doHandleMonth(vDate);
                startTime = vYear + "-" + vMonth + "-" + vDate + " " + "00:00:00";
                if (day == 1) {
                    endTime = vYear + "-" + vMonth + "-" + vDate + " " + "23:59:59";
                } else {
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = switchSignalStatistics.doHandleMonth(vendMonth + 1);
                    vendDate = switchSignalStatistics.doHandleMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate + " " + "23:59:59";
                }
            }
        },
        doHandleMonth: function (month) {
            var m = month;
            if (month.toString().length == 1) {
                m = "0" + month;
            }
            return m;
        },
        // 当前时间
        nowDay: function () {
            var nowDate = new Date();
            startTime = nowDate.getFullYear() + "-" + (parseInt(nowDate.getMonth() + 1) < 10 ? "0" + parseInt(nowDate.getMonth() +
                1) : parseInt(nowDate.getMonth() + 1)) + "-" + (nowDate.getDate() < 10 ? "0" + nowDate.getDate() :
                nowDate.getDate()) + " " + "00:00:00";
            endTime = nowDate.getFullYear() + "-" + (parseInt(nowDate.getMonth() + 1) < 10 ? "0" + parseInt(nowDate.getMonth() +
                1) : parseInt(nowDate.getMonth() + 1)) + "-" + (nowDate.getDate() < 10 ? "0" + nowDate.getDate() :
                nowDate.getDate()) + " " + ("23") + ":" + ("59") + ":" + ("59");
        },
        //判断是否绑定传感器（"true"为绑定,""为非绑定）
        getSensorMessage: function (band) {
            var flog;
            var url = "/clbs/v/oilmassmgt/oilquantitystatistics/getSensorMessage";
            var data = {
                "band": band
            };
            json_ajax("POST", url, "json", false, data, function (data) {
                flog = data;
            });
            return flog;
        },
        //上一天(前段没有此按钮)
        upDay: function () {
            switchSignalStatistics.startDay(1);
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (charNum != "" && groupValue != "") {
                var dateValue = new Date().getTime();
                var startTimeValue = new Date(startTime.split(" ")[0].replace(/-/g, "/")).getTime();
                if (startTimeValue <= dateValue) {
                    if (charNum != "" && groupValue != "") {
                        if (switchSignalStatistics.validates()) {
                            var param = {
                                vehicleId: charNum,
                                startTime: startTime,
                                endTime: endTime
                            };
                            switchSignalStatistics.inquireChart(param);
                        }
                    } else {
                        layer.msg(selectMonitoringObjec, {
                            move: false
                        });
                    }
                } else {
                    layer.msg("暂时没办法穿越，明天我再帮您看吧！");
                }
            } else {
                layer.msg("请选择监控对象！", {
                    move: false
                });
            }
        },
        //查询条件检索
        validates: function () {
            return $("#oilist").validate({
                rules: {
                    startTime: {
                        required: true
                    },
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval",
                    },
                    groupSelect: {
                        zTreeChecked: "treeDemo"
                    },
                    charSelect: {
                        required: true
                    }
                },
                messages: {
                    startTime: {
                        required: "请选择开始日期！",
                    },
                    endTime: {
                        required: "请选择结束时间！",
                        compareDate: endtimeComStarttime,
                    },
                    groupSelect: {
                        zTreeChecked: vehicleSelectBrand,
                    },
                    charSelect: {
                        required: vehicleSelectBrand,
                    }
                }
            }).form();
        },
        // 查询数据
        inquireClick: function (num) {
            if (!isNaN(num)) {
                if (num == 0) {
                    switchSignalStatistics.nowDay();
                } else {
                    switchSignalStatistics.startDay(num);
                }
            } else {
                var timeInterval = $('#timeInterval').val().split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
            }
            var charNum = $("#charSelect").attr("data-id");
            if (!switchSignalStatistics.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            } else {
                $("#charSelect-error").hide();
            }
            var param = {
                vehicleId: charNum,
                startTime: startTime,
                endTime: endTime
            }
            switchSignalStatistics.inquireChart(param);
        },
        // 查询图表数据
        inquireChart: function (param) {
            $('#carName').text(switchSignalStatistics.plateNumberSplitFun($("input[name='charSelect']").val()));
            //没有截取就不显示tooltip
            if ($("input[name='charSelect']").val() == switchSignalStatistics.plateNumberSplitFun($("input[name='charSelect']").val())) {
                $('#carName').removeAttr('data-original-title')
            } else {
                $('#carName').attr('data-original-title', $("input[name='charSelect']").val());
            }

            // 查询之前，恢复到默认的表头
            $(".toopTip-btn-left,.toopTip-btn-right").css("display", "inline-block");
            json_ajax('post', '/clbs/v/switching/switchSignalStatistics/getSwitchSignalChartInfo', 'json', true, param,
                switchSignalStatistics.ajaxDataCallback)
        },
        // 清除表格数据
        restoreThead: function () {
            $(".insertData").remove();
            if (myTable) {
                myTable.dataTable.clear();
            }
            terminal = [];//终端数据
            ioCollectOne = [];//I/O采集1数据
            ioCollectTwo = [];//I/O采集2数据
        },
        // 图表数据回调
        ajaxDataCallback: function (data) {
            switchSignalStatistics.restoreThead();
            if (data.success) {
                layer.closeAll('loading');
                $('#timeInterval').val(startTime + '--' + endTime);
                terminal = JSON.parse(ungzip(data.obj.terminal));//终端数据
                ioCollectOne = JSON.parse(ungzip(data.obj.acquisitionBoardOne));//I/O采集1数据
                ioCollectTwo = JSON.parse(ungzip(data.obj.acquisitionBoardTwo));//I/O采集2数据

                var tableInfo = [];
                switch (curChart) {
                    case 0:
                        if (terminal.length > 0) {
                            tableInfo = terminal;
                            $("#graphShow").show();
                            $("#showClick").attr("class", "fa fa-chevron-down");
                        }
                        break;
                    case 1:
                        if (ioCollectOne.length > 0) {
                            tableInfo = ioCollectOne;
                            $("#graphShow").show();
                            $("#showClick").attr("class", "fa fa-chevron-down");
                        }
                        break;
                    case 2:
                        if (ioCollectTwo.length > 0) {
                            tableInfo = ioCollectTwo;
                            $("#graphShow").show();
                            $("#showClick").attr("class", "fa fa-chevron-down");
                        }
                        break;
                }
                //图表初始化
                if (!$("#graphShow").is(":hidden")) {
                    switchSignalStatistics.init();
                }
                //表格信息初始化
                switchSignalStatistics.tableInit(tableInfo);
            } else {
                if (data.msg) {
                    layer.msg(data.msg)
                }
            }
        },
        //动态组装表头
        setTableThead: function (tableInfo) {
            var data = tableInfo;
            if (data.length > 0) {
                var ioInfo = data[0].ioInfo;
                if (ioInfo) {
                    var len = ioInfo.length;
                    if (len > 0) {
                        var html = '';
                        var addCol = [];
                        for (var i = 0; i < len; i++) {
                            html += '<th class="text-center insertData">' + ioInfo[i].columnName + '</th>';
                            addCol.push({
                                "data": "ioInfo." + i + ".stateName", "class": "text-center",
                                "render": function (data, type, row, meta) {
                                    if (data == undefined || data == null) {
                                        return '';
                                    }
                                    return data;
                                }
                            });
                        }
                        $('#insertAddr').after(html);
                        return addCol;
                    }
                }
            }
        },
        //表格初始化
        tableInit: function (tableInfo) {
            var tableUrl = '';
            switch (curTable) {
                case 0: //终端I/O
                    tableUrl = '/clbs/v/switching/switchSignalStatistics/getSwitchSignalTerminalFormInfo';
                    break;
                case 1: //I/O采集1
                    tableUrl = '/clbs/v/switching/switchSignalStatistics/getSwitchSignalAcquisitionBoardOneFormInfo';
                    break;
                case 2: //I/O采集2
                    tableUrl = '/clbs/v/switching/switchSignalStatistics/getSwitchSignalAcquisitionBoardTwoFormInfo';
                    break;
            }

            //表格列定义
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            // 动态组装列表显示数据
            var addCol = switchSignalStatistics.setTableThead(tableInfo) || [];
            var columns = [
                {
                    //第一列，用来显示序号
                    "data": null,
                    "class": "text-center"
                },
                {
                    "data": 'brand',
                    "class": "text-center"
                },
                {
                    "data": 'time',
                    "class": "text-center"
                },
            ];
            var lastCol = [
                {
                    "data": 'gpsMile',
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == 'null') {
                            return '-'
                        } else {
                            return data;
                        }
                    }
                },
                {
                    "data": 'speed',
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == 'null') {
                            return '-'
                        } else {
                            return data;
                        }
                    }
                },
                {
                    "data": null,
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return "加载中..."
                    }
                }
            ]
            columns = columns.concat(addCol);
            columns = columns.concat(lastCol);

            //ajax参数
            var ajaxDataParamFun = function (d) {

            };
            //表格setting
            var setting = {
                listUrl: tableUrl,
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'switchTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                getAddress: true,
                address_index: $("#switchTable thead th").length,
                sync_address: true,// 如果地址为空,就执行异步查询地址
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        showClick: function () {
            if ($(this).hasClass("fa-chevron-up")) {
                $(this).attr("class", "fa fa-chevron-down");
                $("#graphShow").slideDown();
            } else {
                $(this).attr("class", "fa fa-chevron-up");
                $("#graphShow").slideUp();
            }
        },
        left_arrow: function () {
            $("#button.dropdown-toggle").click();
            $("#button.dropdown-toggle").click();

            var trIndex = $(".table-condensed tr").size() - 1;
            var nowIndex = 0;
            $(".table-condensed tr").each(function () {
                if ($(this).attr("data-id") == $("input[name='charSelect']").attr("data-id")) {
                    nowIndex = $(this).attr("data-index");
                }
            })
            if (0 == nowIndex) {
                $("input[name='charSelect']").attr("data-id", $(".table-condensed tr").eq(trIndex).attr("data-id"));
                $("input[name='charSelect']").val($(".table-condensed tr").eq(trIndex).attr("data-key"));
            } else {
                $("input[name='charSelect']").attr("data-id", $(".table-condensed tr").eq(nowIndex - 1).attr("data-id"));
                $("input[name='charSelect']").val($(".table-condensed tr").eq(nowIndex - 1).attr("data-key"));
            }
            $("#inquireClick").click();
        },
        right_arrow: function () {
            $("#button.dropdown-toggle").click();
            $("#button.dropdown-toggle").click();

            var trIndex = $(".table-condensed tr").size() - 1;
            var nowIndex = 0;
            $(".table-condensed tr").each(function () {
                if ($(this).attr("data-id") == $("input[name='charSelect']").attr("data-id")) {
                    nowIndex = $(this).attr("data-index");
                }
            })
            if (trIndex == nowIndex) {
                $("input[name='charSelect']").attr("data-id", $(".table-condensed tr").eq(0).attr("data-id"));
                $("input[name='charSelect']").val($(".table-condensed tr").eq(0).attr("data-key"));
            } else {
                var nextIndex = parseInt(nowIndex) + 1;
                $("input[name='charSelect']").attr("data-id", $(".table-condensed tr").eq(nextIndex).attr("data-id"));
                $("input[name='charSelect']").val($(".table-condensed tr").eq(nextIndex).attr("data-key"));
            }
            $("#inquireClick").click();
        },
        plateNumberSplitFun: function (str) {
            if (str.length > 8) {
                str = str.substring(0, 7) + '...'
            }
            return str
        },
        //tab选项卡切换
        tabToggle: function (e) {
            e.stopPropagation();
            if (!$(this).hasClass('active')) {
                $(this).addClass('active').siblings().removeClass('active');
                var curId = $(this).attr('id');
                var chart = $(this).data('chart');
                var table = $(this).data('table');
                if (chart != undefined) {
                    curChart = chart;
                }
                if (table != undefined) {
                    curTable = table;
                }
                var eleId = '';
                switch (curId) {
                    case 'terminalIo':
                        curTable = 0;
                        eleId = 'terminalIoTable';
                        break;
                    case 'ioCollectOne':
                        curTable = 1;
                        eleId = 'ioCollectOneTable';
                        break;
                    case 'ioCollectTwo':
                        curTable = 2;
                        eleId = 'ioCollectTwoTable';
                        break;
                    case 'terminalIoTable':
                        curChart = 0;
                        eleId = 'terminalIo';
                        break;
                    case 'ioCollectOneTable':
                        curChart = 1;
                        eleId = 'ioCollectOne';
                        break;
                    case 'ioCollectTwoTable':
                        curChart = 2;
                        eleId = 'ioCollectTwo';
                        break;
                }
                $("#" + eleId).addClass('active').siblings().removeClass('active');
                switchSignalStatistics.inquireClick();
            }
        },
    };
    $(function () {
        $('input').inputClear();
        // switchSignalStatistics.init();
        switchSignalStatistics.treeInit();
        switchSignalStatistics.nowDay();
        $('#timeInterval').dateRangePicker({
            dateLimit: 7
        });

        $("#todayClick").bind("click", function () {
            switchSignalStatistics.inquireClick(0);
        });
        $("#yesterdayClick,#right-arrow").bind("click", function () {
            switchSignalStatistics.inquireClick(-1);
        });
        $("#nearlyThreeDays").bind("click", function () {
            switchSignalStatistics.inquireClick(-3);
        });
        $("#nearlySevenDays").bind("click", function () {
            switchSignalStatistics.inquireClick(-7);
        });
        $("#inquireClick").bind("click", switchSignalStatistics.inquireClick);

        $("#showClick").bind("click", switchSignalStatistics.showClick);
        $("#left-arrow").bind("click", switchSignalStatistics.upDay);
        $("#endTime").bind("click", switchSignalStatistics.endTimeClick);
        $("#groupSelect").bind("click", showMenuContent); //组织下拉显示

        //选项卡切换
        $('.nav-tabs li').on('click', switchSignalStatistics.tabToggle);

        //监控对象树模糊查询
        var inputChange;
        $("#groupSelect").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            ;
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                isSearch = true;
            }
            $("#charSelect").val("").attr("data-id", "").bsSuggest("destroy");
            inputChange = setTimeout(function () {
                if (isSearch) {
                    var param = $("#groupSelect").val();
                    switchSignalStatistics.searchVehicleTree(param);
                }
                isSearch = true;
            }, 500);
        });

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                switchSignalStatistics.searchVehicleTree(param);
            }
            ;
        });
        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            switchSignalStatistics.searchVehicleTree(param);
        });

    });
})($, window);