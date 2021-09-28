(function ($, window) {
    var endTime;
    var startTime;
    var myChart;
    var option;
    var myTable;
    // 车辆list
    var vehicleValue = $("#vehicleList").attr("value");
    var vehicleList = !vehicleValue ? [] : JSON.parse($("#vehicleList").attr("value"));
    var defaultShowInfoValue = $("#defaultShowInfo").val();
    var defaultShowInfo = !defaultShowInfoValue ? [] : JSON.parse(defaultShowInfoValue);// obd下拉框选择数据
    var zTreeIdJson = {};
    var size;//当前权限监控对象数量
    var checkFlag = false;
    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于100自动勾选
    //用于ie浏览器判断选择节点树时是否需要调用搜索功能
    var isSearch = true;
    var checkObdInfo = [];// 选择的obd数据
    var legendData = [];//图表legend数据
    var legendSelected = {};//图表legend默认显示项
    var yAxisData = [];// y轴坐标系
    var yAxisSeries = [];// y轴显示数据
    var unitData = [];//单位
    //图表数据
    var chartAllData = {};
    var keepLegendSelect = false;//是否保留图表legend勾选状态
    var topNum = 80; // charts与顶部分类的距离

    obdVehicleDataReport = {
        init: function () {
            var len = defaultShowInfo.length;
            var checkString = '';
            for (var i = 0; i < len; i++) {
                defaultShowInfo[i].isParent = false;
                defaultShowInfo[i].checked = defaultShowInfo[i].showByDefault;
                defaultShowInfo[i].name = defaultShowInfo[i].displayName;
                if (defaultShowInfo[i].showByDefault) {
                    checkString += defaultShowInfo[i].name + ',';
                    checkObdInfo.push(defaultShowInfo[i]);
                }
            }
            $("#groupSelectObd").val(checkString.slice(0, checkString.length - 1));
            var setting = {
                async: {
                    type: "post",
                    enable: true,
                    autoParam: ["type"],
                    dataType: "json",
                    icon: false,
                    otherParam: {"type": "multiple", "icoType": "0"},
                },
                check: {
                    enable: true,
                    chkStyle: "checkbox",
                    radioType: "all"
                },
                view: {
                    dblClickExpand: false,
                    nameIsHTML: true,
                    showIcon: false
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    onCheck: obdVehicleDataReport.onCheckType,
                    onClick: obdVehicleDataReport.onClickBack
                }
            };
            $.fn.zTree.init($("#obdInfoTree"), setting, defaultShowInfo);
        },
        onCheckType: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("obdInfoTree");
            var nodes = zTree.getCheckedNodes(true);
            if (nodes.length > 20 && treeNode.checked) {
                layer.msg('最多勾选20项');
                zTree.checkNode(treeNode, false, true);
                return;
            }
            if (nodes.length == 0) {
                $("#groupSelectObd").val('');
            } else {
                var checkString = '';
                var len = nodes.length;
                for (var i = 0; i < len; i++) {
                    checkString += nodes[i].name + ',';
                }
                $("#groupSelectObd").val(checkString.slice(0, checkString.length - 1));
            }
        },
        onClickBack: function (e, treeId, treeNode, clickFlag) {
            var zTreeObj = $.fn.zTree.getZTreeObj("obdInfoTree");
            zTreeObj.checkNode(treeNode, !treeNode.checked, true);
            obdVehicleDataReport.onCheckType(e, treeId, treeNode);
        },
        chartInit: function () {
            myChart = echarts.init(document.getElementById('sjcontainer'));
            option = {
                tooltip: {
                    trigger: 'axis',
                    textStyle: {
                        fontSize: 20
                    },
                    formatter: function (a) {
                        var unit = unitData;
                        var relVal = "";
                        var addRelVal = "";
                        var relValTime = a[0].name;
                        if (a[0].data == null) {
                            relVal = "无相关数据";
                        } else {
                            for (var i = 0; i < a.length; i++) {
                                var info =
                                    relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + (unit[a[i].seriesIndex] == '' ? '' : '(' + unit[a[i].seriesIndex] + ')') + "：" + (a[i].data.showStr != '' ? a[i].data.showStr : a[i].data.value);
                            }
                        }
                        var allVal = relValTime + addRelVal + relVal;
                        return allVal;
                    }
                },
                grid: {
                    left: 200,   // 与容器左侧的距离
                    right: 200, // 与容器右侧的距离
                    top: topNum,   // 与容器顶部的距离
                },
                legend: {
                    selected: legendSelected,
                    data: legendData,
                    left: 'auto'
                },
                toolbox: {
                    show: false
                },
                xAxis: {
                    type: 'category',
                    boundaryGap: false,
                    data: chartAllData.vtimeStr
                },
                yAxis: yAxisData,
                dataZoom: [{
                    type: 'inside'

                }, {
                    start: 0,
                    end: 10,
                    handleIcon: 'M10.7,11.9v-1.3H9.3v1.3c-4.9,0.3-8.8,4.4-8.8,9.4c0,5,3.9,9.1,8.8,9.4v1.3h1.3v-1.3c4.9-0.3,8.8-4.4,8.8-9.4C19.5,16.3,15.6,12.2,10.7,11.9z M13.3,24.4H6.7V23h6.6V24.4z M13.3,19.6H6.7v-1.4h6.6V19.6z',
                    handleSize: '80%',
                    handleStyle: {
                        color: '#fff',
                        shadowBlur: 3,
                        shadowColor: 'rgba(0, 0, 0, 0.6)',
                        shadowOffsetX: 2,
                        shadowOffsetY: 2
                    }
                }],
                series: yAxisSeries
            };
            myChart.setOption(option);
            myChart.on('legendselectchanged', function (obj) {// 监听legend点击事件
                var selected = obj.selected;
                if (selected != undefined) {
                    var selectNum = 0;
                    var data = [];
                    for (key in selected) {
                        if (selected[key]) {
                            selectNum++;
                            data.push(key);
                        }
                    }
                    if (selectNum > 6) {
                        layer.msg('最多勾选6项');
                        selected[obj.name] = false;
                        myChart.setOption(option);
                        return;
                    } else if (selectNum == 0) {
                        layer.msg('至少勾选1项');
                        selected[obj.name] = true;
                        option.legend.selected = selected;
                        myChart.setOption(option);
                        return;
                    }
                    else {
                        obdVehicleDataReport.setChartData(data.slice(0, 6));
                        legendSelected = selected;
                        obdVehicleDataReport.chartInit();
                    }
                }
            });
            window.onresize = myChart.resize;
        },
        treeInit: function () {
            var setting = {
                async: {
                    url: obdVehicleDataReport.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple"},
                    otherParam: {"icoType": "0"},
                    dataFilter: obdVehicleDataReport.ajaxDataFilter
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
                    beforeClick: obdVehicleDataReport.beforeClickVehicle,
                    onCheck: obdVehicleDataReport.onCheckVehicle,
                    beforeCheck: obdVehicleDataReport.zTreeBeforeCheck,
                    onExpand: obdVehicleDataReport.zTreeOnExpand,
                    onAsyncSuccess: obdVehicleDataReport.zTreeOnAsyncSuccess,
                    onNodeCreated: obdVehicleDataReport.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
            $("[data-toggle='tooltip']").tooltip();
        },
        //模糊查询树
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选
            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                obdVehicleDataReport.treeInit();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/m/functionconfig/fence/bindfence/monitorTreeFuzzy",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": "multiple", "queryParam": param, "queryType": "name"},
                        dataFilter: obdVehicleDataReport.ajaxQueryDataFilter
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
                        beforeClick: obdVehicleDataReport.beforeClickVehicle,
                        onCheck: obdVehicleDataReport.onCheckVehicle,
                        onExpand: obdVehicleDataReport.zTreeOnExpand,
                        onNodeCreated: obdVehicleDataReport.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData))
            var nodesArr = filterQueryResult(responseData, crrentSubV);
            for (var i = 0; i < nodesArr.length; i++) {
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        getTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/functionconfig/fence/bindfence/alarmSearchTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=monitor";
            }
        },
        //组织树预处理加载函数
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (responseData.msg) {
                var obj = JSON.parse(ungzip(responseData.msg));
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
            }
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (size <= 5000 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            obdVehicleDataReport.getCharSelect(treeObj);
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
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle" || nodes[i].type == "thing") {
                            //查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                            var nd = zTree.getNodeByParam("tId", nodes[i].tId, treeNode);
                            if (nd == null && $.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength += ns.length;
                } else if (treeNode.type == "people" || treeNode.type == "vehicle" || treeNode.type == "thing") { //若勾选的为监控对象
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;
                    //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    //节点id
                    var nodeId;
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle" || nodes[i].type == "thing") {
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
            }
            if (treeNode.type != "assignment" || (treeNode.type == "assignment" && treeNode.children != undefined)) {
                obdVehicleDataReport.getCharSelect(zTree);
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
                    "monitorType": "monitor"
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
            var veh = [];
            var vid = [];
            for (var i = 0; i < nodes.length; i++) {
                if (nodes[i].type == "people" || nodes[i].type == "vehicle" || nodes[i].type == "thing") {
                    veh.push(nodes[i].name)
                    vid.push(nodes[i].id)
                }
            }
            var vehName = obdVehicleDataReport.unique(veh);
            var vehId = obdVehicleDataReport.unique(vid);
            $("#charSelect").empty();
            var deviceDataList = {value: []};
            for (var j = 0; j < vehName.length; j++) {
                for (var k = 0; k < vehicleList.length; k++) {
                    if (vehId[j] == vehicleList[k]) {
                        deviceDataList.value.push({
                            name: vehName[j],
                            id: vehId[j]
                        });
                    }
                }
            };
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
            $("#groupSelect,#groupSelectSpan").bind("click", obdVehicleDataReport.showMenu);
            $("#button").removeClass('disabled loading-state-button').find('i').attr("class", 'caret');
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
                tMonth = obdVehicleDataReport.doHandleMonth(tMonth + 1);
                tDate = obdVehicleDataReport.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * parseInt(num);
                today.setTime(end_milliseconds); // 注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = obdVehicleDataReport.doHandleMonth(endMonth + 1);
                endDate = obdVehicleDataReport.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " " + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = obdVehicleDataReport.doHandleMonth(vMonth + 1);
                vDate = obdVehicleDataReport.doHandleMonth(vDate);
                startTime = vYear + "-" + vMonth + "-" + vDate + " "
                    + "00:00:00";
                if (day == 1) {
                    endTime = vYear + "-" + vMonth + "-" + vDate + " "
                        + "23:59:59";
                } else {
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = obdVehicleDataReport.doHandleMonth(vendMonth + 1);
                    vendDate = obdVehicleDataReport.doHandleMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate + " "
                        + "23:59:59";
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
            startTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate()) + " " + "00:00:00";
            endTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate())
                + " "
                + ("23")
                + ":"
                + ("59")
                + ":"
                + ("59");
        },
        // ajax请求数据
        ajaxList: function (band, startTime, endTime) {
            var brandName = $("input[name='charSelect']").val();
            if (brandName.length > 8) {
                $("#carName").attr("title", brandName).tooltip('fixTitle');
                brandName = brandName.substring(0, 7) + '...';
            } else {
                $('#carName').removeAttr('data-original-title');
            }
            $("#carName").text(brandName);
            $(".toopTip-btn-left,.toopTip-btn-right").css("display", "inline-block");
            var url = "/clbs/v/obdManager/obdVehicleDataReport/getOBDVehicleDataReport";
            var parameter = {"monitorId": band, "startTimeStr": startTime, "endTimeStr": endTime};
            json_ajax("POST", url, "json", true, parameter, obdVehicleDataReport.getCallback);
        },
        getCallback: function (data) {
            $('#timeInterval').val(startTime + '--' + endTime);
            obdVehicleDataReport.restoreThead();
            var zTree = $.fn.zTree.getZTreeObj("obdInfoTree");
            checkObdInfo = zTree.getCheckedNodes(true);
            var checkLen = checkObdInfo.length;
            chartAllData = {};
            legendData = [];
            for (var i = 0; i < checkLen; i++) {// 组装图表显示数据
                chartAllData[checkObdInfo[i].columnName] = [];
                legendData.push(checkObdInfo[i].name);
            }
            topNum = legendData.length > 0 ? legendData.length / 10 * 16 + 80 : 80;
            console.log(topNum, 'topNum');
            chartAllData.vtimeStr = [];
            var selectData = [];//默认显示项
            if (!keepLegendSelect) {
                legendSelected = {};
                for (var i = 0; i < legendData.length; i++) {// 组装图表legend默认显示项
                    if (i > 5) {
                        legendSelected[legendData[i]] = false;
                    }
                }
            } else {// 保留用户勾选的显示项内容
                for (key in legendSelected) {
                    if (legendSelected[key]) {
                        selectData.push(key);
                    }
                }
            }
            if (data.success) {
                var obdVehicleData = ungzip(data.msg);
                var responseData = $.parseJSON(obdVehicleData);
                responseData = responseData.result;
                $('#timeInterval').val(startTime + '--' + endTime);
                var len = responseData.length;
                for (var i = 0; i < len; i++) {
                    for (key in chartAllData) {
                        var obj = {
                            value: responseData[i][key] ? responseData[i][key] : '',
                            showStr: responseData[i][key + 'Str'] ? responseData[i][key + 'Str'] : ''
                        }
                        chartAllData[key].push(obj);
                    }
                }
                $("#graphShow").show();
                $("#showClick").attr("class", "fa fa-chevron-down");
            }
            obdVehicleDataReport.setChartData(selectData.length > 0 ? selectData : undefined);
            obdVehicleDataReport.chartInit();
            obdVehicleDataReport.tableInit();
        },
        // 组装图表显示的y轴数据以及对应显示单位
        setChartData: function (currArr) {
            yAxisData = [];// y轴坐标系
            yAxisSeries = [];// y轴显示数据
            unitData = [];//单位
            var checkLen = checkObdInfo.length;
            var num = currArr ? currArr.length : 6;
            var offsetData = [125, 62, 0, 0, 62, 125];
            var arrLen = currArr ? currArr.length : checkLen;
            switch (arrLen) {
                case 5:
                    offsetData = offsetData.slice(0, 5);
                    break;
                case 4:
                    offsetData = [65, 0, 0, 65];
                    break;
                case 3:
                    offsetData = [65, 0, 0];
                    break;
            }
            for (var i = 0; i < checkLen; i++) {
                var item = checkObdInfo[i];
                var serObj = {
                    name: item.name,
                    yAxisIndex: i % num,
                    type: 'line',
                    smooth: true,
                    symbol: 'none',
                    sampling: 'average',
                    data: chartAllData[item.columnName]
                };
                if (currArr) {
                    for (var j = 0; j < arrLen; j++) {
                        if (currArr[j] == item.name) {
                            var obj = {
                                type: 'value',
                                name: item.name.length < 6 ? item.name : item.name.substring(0, 4) + '...',
                                scale: true,
                                min: 0,
                                offset: arrLen > 2 ? offsetData[j] : 0,
                                position: j < Math.ceil(arrLen / 2) ? 'left' : 'right',
                                axisLabel: {
                                    formatter: '{value}'
                                },
                                splitLine: {
                                    show: false
                                }
                            };
                            serObj.yAxisIndex = j;
                            yAxisData.push(obj);
                        }
                    }
                } else if (i < 6) {
                    var obj = {
                        type: 'value',
                        name: item.name.length < 6 ? item.name : item.name.substring(0, 4) + '...',
                        scale: true,
                        min: 0,
                        offset: arrLen > 2 ? offsetData[i] : 0,
                        position: i < Math.ceil((arrLen > 6 ? 6 : arrLen) / 2) ? 'left' : 'right',
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine: {
                            show: false
                        }
                    };
                    yAxisData.push(obj);
                }
                yAxisSeries.push(serObj);
                unitData.push(item.unit);
            }
        },
        //上一天(前段没有此按钮)
        upDay: function () {
            obdVehicleDataReport.startDay(1);
            keepLegendSelect = true;
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (charNum != "" && groupValue != "") {
                var dateValue = new Date().getTime();
                var startTimeValue = new Date(startTime.split(" ")[0].replace(/-/g, "/")).getTime();
                if (startTimeValue <= dateValue) {
                    obdVehicleDataReport.ajaxList(charNum, startTime, endTime);
                    obdVehicleDataReport.validates()
                } else {
                    layer.msg("暂时没办法穿越，明天我再帮您看吧！");
                }
            } else {
                layer.msg("请选择监控对象！", {move: false});
            }
        },
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
                    groupSelectObd: {
                        required: true
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
                    groupSelectObd: {
                        required: "请勾选OBD数据"
                    },
                    charSelect: {
                        required: vehicleSelectBrand,
                    }
                }
            }).form();
        },
        // 查询
        inquireClick: function (e, number) {
            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];
            if (number == 0) {
                obdVehicleDataReport.nowDay();
            } else if (number) {
                obdVehicleDataReport.startDay(number);
            }
            if (e.target.id == 'right-arrow') {// 点击图表中的前一天按钮
                keepLegendSelect = true;
            } else {
                keepLegendSelect = false;
            }
            var charNum = $("#charSelect").attr("data-id");
            if (!obdVehicleDataReport.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            } else {
                $("#charSelect-error").hide();
            }
            obdVehicleDataReport.ajaxList(charNum, startTime, endTime);
        },
        // 时间戳转换日期         
        UnixToDate: function (unixTime, isFull, timeZone) {
            if (typeof (timeZone) == 'number') {
                unixTime = parseInt(unixTime) + parseInt(timeZone) * 60 * 60;
            }
            var time = new Date(unixTime * 1000);
            var ymdhis = "";
            ymdhis += time.getFullYear() + "-";
            ymdhis += (time.getMonth() + 1) + "-";
            ymdhis += time.getDate();
            if (isFull === true) {
                ymdhis += " " + time.getHours() + ":";
                ymdhis += time.getMinutes() + ":";
                ymdhis += time.getSeconds();
            }
            return ymdhis;
        },
        showClick: function () {
            if ($(this).hasClass("fa-chevron-up")) {
                $(this).attr("class", "fa fa-chevron-down");
                $("#graphShow").show();
            } else {
                $(this).attr("class", "fa fa-chevron-up");
                $("#graphShow").hide('300');
            }
        },
        draggle: function () {
            $("#showClick").attr("class", "fa fa-chevron-down");
            $("#graphShow").show();
        },
        // 清除表格数据
        restoreThead: function () {
            $(".insertData").remove();
            if (myTable) {
                myTable.dataTable.clear();
            }
        },
        //动态组装表头
        setTableThead: function () {
            var len = checkObdInfo.length;
            if (len > 0) {
                var html = '';
                var addCol = [];
                for (var i = 0; i < len; i++) {
                    var unit = checkObdInfo[i].unit == '' ? '' : '(' + checkObdInfo[i].unit + ')';
                    html += '<th class="text-center insertData">' + checkObdInfo[i].name + unit + '</th>';
                    var name = checkObdInfo[i].columnName + 'Str';
                    addCol.push({
                        "data": checkObdInfo[i].columnName, "class": "text-center",
                        "render": (function (name) {
                            return function (data, type, row, meta) {
                                if (row[name]) {
                                    return row[name];
                                }
                                if (data == undefined || data == null) {
                                    return '';
                                }
                                return data;
                            }
                        })(name)
                    });
                }
                $('#insertAddr').after(html);
                return addCol;
            }
        },
        //创建表格
        tableInit: function () {
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var addCol = obdVehicleDataReport.setTableThead() || [];
            var columns = [{
                //第一列，用来显示序号
                "data": null,
                "class": "text-center"
            }, {
                "data": "plateNumber",
                "class": "text-center"
            }, {
                "data": "vtimeStr",
                "class": "text-center"
            }];
            columns = columns.concat(addCol);
            //表格setting
            var setting = {
                listUrl: '/clbs/v/obdManager/obdVehicleDataReport/getOBDVehicleDataTable',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'obdTable', //表格
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                getAddress: false,//是否逆地理编码
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        toHHMMSS: function (data) {
            var totalSeconds = data * 60 * 60;
            var hour = Math.floor(totalSeconds / 60 / 60);
            var minute = Math.floor(totalSeconds / 60 % 60);
            var second = Math.floor(totalSeconds % 60);
            return hour != 0 ? hour + "小时" + minute + "分" + second + "秒" : minute != 0 ? minute + "分" + second + "秒" : second != 0 ? second + "秒" : 0
        },
        timeStamp2String: function (time) {
            var time = time.toString();
            var startTimeIndex = time.replace("-", "/").replace("-", "/");
            var val = Date.parse(startTimeIndex);
            var datetime = new Date();
            datetime.setTime(val);
            var year = datetime.getFullYear();
            var month = datetime.getMonth() + 1 < 10 ? "0" + (datetime.getMonth() + 1) : datetime.getMonth() + 1;
            var date = datetime.getDate() < 10 ? "0" + datetime.getDate() : datetime.getDate();
            var hour = datetime.getHours() < 10 ? "0" + datetime.getHours() : datetime.getHours();
            var minute = datetime.getMinutes() < 10 ? "0" + datetime.getMinutes() : datetime.getMinutes();
            var second = datetime.getSeconds() < 10 ? "0" + datetime.getSeconds() : datetime.getSeconds();
            return year + "-" + month + "-" + date + " " + hour + ":" + minute + ":" + second;
        },
        timeAdd: function (time) {
            var str = time.toString();
            str = str.replace(/-/g, "/");
            return new Date(str);
        },
        GetDateDiff: function (startTime, endTime, diffType) {
            // 将xxxx-xx-xx的时间格式，转换为 xxxx/xx/xx的格式
            startTime = startTime.replace(/-/g, "/");
            endTime = endTime.replace(/-/g, "/");
            // 将计算间隔类性字符转换为小写
            diffType = diffType.toLowerCase();
            var sTime = new Date(startTime); // 开始时间
            var eTime = new Date(endTime); // 结束时间
            // 作为除数的数字
            var divNum = 1;
            switch (diffType) {
                case "second":
                    divNum = 1000;
                    break;
                case "minute":
                    divNum = 1000 * 60;
                    break;
                case "hour":
                    divNum = 1000 * 3600;
                    break;
                case "day":
                    divNum = 1000 * 3600 * 24;
                    break;
                default:
                    break;
            }
            return parseFloat((eTime.getTime() - sTime.getTime()) / parseInt(divNum)); //
        },
        //过滤数组空值
        filterTheNull: function (value) {
            for (var i = 0; i < value.length; i++) {
                if (value[i] == null || value[i] == "" || typeof(value[i]) == "undefined") {
                    value.splice(i, 1);
                    i = i - 1;
                }
            }
            return value
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
        endTimeClick: function () {
            var width = $(this).width();
            var offset = $(this).offset();
            var left = offset.left - (207 - width);
            $("#laydate_box").css("left", left + "px");
        }
    }
    $(function () {
        $("#toggle-left").bind("click", function () {
            setTimeout(function () {
                obdVehicleDataReport.chartInit();
            }, 500)
        });
        Array.prototype.isHas = function (a) {
            if (this.length === 0) {
                return false
            };
            for (var i = 0; i < this.length; i++) {
                if (this[i].seriesName === a) {
                    return true
                }
            }
        };
        obdVehicleDataReport.init();
        obdVehicleDataReport.treeInit();
        obdVehicleDataReport.nowDay();
        $('#timeInterval').dateRangePicker(
            {
                dateLimit: 7
            }
        );
        $("#todayClick").bind("click", function (e) {
            obdVehicleDataReport.inquireClick(e, 0);
        });
        $("#yesterdayClick,#right-arrow").bind("click", function (e) {
            obdVehicleDataReport.inquireClick(e, -1);
        });
        $("#nearlyThreeDays").bind("click", function (e) {
            obdVehicleDataReport.inquireClick(e, -3);
        });
        $("#nearlySevenDays").bind("click", function (e) {
            obdVehicleDataReport.inquireClick(e, -7);
        });
        $("#inquireClick").bind("click", obdVehicleDataReport.inquireClick);
        $("#showClick").bind("click", obdVehicleDataReport.showClick);
        $("#left-arrow").bind("click", obdVehicleDataReport.upDay);
        $("#endTime").bind("click", obdVehicleDataReport.endTimeClick);
        $("#groupSelect").bind("click", showMenuContent); //组织下拉显示
        $("#groupSelectObd").bind("click", showMenuContent); //组织下拉显示
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                obdVehicleDataReport.searchVehicleTree(param);
            };
        });
        /**
         * 监控对象树模糊查询
         */
        var inputChange;
        $("#groupSelect").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                isSearch = true;
            }
            $("#charSelect").val("").attr("data-id", "").bsSuggest("destroy");
            inputChange = setTimeout(function () {
                if (isSearch) {
                    var param = $("#groupSelect").val();
                    obdVehicleDataReport.searchVehicleTree(param);
                }
                isSearch = true;
            }, 500);
        });
    });
})($, window);
