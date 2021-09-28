(function (window, $) {
    var startTime;
    var endTime;
    // 车辆list
    var vehicleList = JSON.parse($("#vehicleList").attr("value"));
    var zTreeIdJson = {};
    var checkFlag = false;
    var size;//当前权限监控对象数量

    var mileageData = [];
    var speedData = [];
    var accData = [];
    var vTime = [];

    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于100自动勾选
    //用于ie浏览器判断选择节点树时是否需要调用搜索功能
    var isSearch = true;

    mailStatistics = {
        init: function () {
            //车辆树
            var setting = {
                async: {
                    //url: "/clbs/m/functionconfig/fence/bindfence/vehicelTree",
                    url: mailStatistics.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: mailStatistics.ajaxDataFilter
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
                    beforeClick: mailStatistics.beforeClickVehicle,
                    onCheck: mailStatistics.onCheckVehicle,
                    beforeCheck: mailStatistics.zTreeBeforeCheck,
                    onExpand: mailStatistics.zTreeOnExpand,
                    //beforeAsync: mailStatistics.zTreeBeforeAsync,
                    onAsyncSuccess: mailStatistics.zTreeOnAsyncSuccess,
                    onNodeCreated: mailStatistics.zTreeOnNodeCreated
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
                mailStatistics.init();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType').val(), "queryParam": param, "queryType": "vehicle"},
                        dataFilter: mailStatistics.ajaxQueryDataFilter
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
                        beforeClick: mailStatistics.beforeClickVehicle,
                        onCheck: mailStatistics.onCheckVehicle,
                        onExpand: mailStatistics.zTreeOnExpand,
                        onNodeCreated: mailStatistics.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData))
            var nodesArr;
            if($('#queryType').val() == "vehicle"){
                nodesArr = filterQueryResult(responseData, crrentSubV);
            }else {
                nodesArr = responseData;
            }
            for (var i=0;i<nodesArr.length;i++){
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        getTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
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
        //zTreeBeforeAsync: function () {
        //   return bflag;
        //},
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (size <= 5000 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            mailStatistics.getCharSelect(treeObj);
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
                setTimeout(() => {
                    mailStatistics.validates();
                }, 600);
            }
            if (treeNode.type != "assignment" || (treeNode.type == "assignment" && treeNode.children != undefined)) {
                mailStatistics.getCharSelect(zTree);
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
            $("#groupSelect,#groupSelectSpan").bind("click", mailStatistics.showMenu);
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
            $("body").bind("mousedown", mailStatistics.onBodyDown);
        },
        hideMenu: function () {
            $("#menuContent").fadeOut("fast");
            $("body").unbind("mousedown", mailStatistics.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "menuContent" || $(event.target).parents("#menuContent").length > 0)) {
                mailStatistics.hideMenu();
            }
        },
        //创建表格
        searchInitTabData: function (url) {
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
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
            }, {
                "data": "acc",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if ((data + "").length == 1) {
                        return (data == 0 ? "关" : "开");
                    } else if (data == "21") {
                        return "点火静止";
                    } else if (data == "16") {
                        return "熄火拖车";
                    } else if (data == "1A") {
                        return "熄火假拖车";
                    } else if (data == "11") {
                        return "熄火静止";
                    } else if (data == "12") {
                        return "熄火移动";
                    } else if (data == "22") {
                        return "点火移动";
                    } else if (data == "41") {
                        return "无点火静止";
                    } else if (data == "42") {
                        return "无点火移动";
                    } else {
                        return "ACC状态异常";
                    }
                }
            }, {
                "data": "mileageTotal",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return parseFloat(data);
                }
            }, {
                "data": "mileageSpeed",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return parseFloat(data);
                }
            }, {
                "data": "",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return '加载中...'
                }
            }];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                var vehicleId = $("input[name='charSelect']").attr("data-id");
                var timeInterval = $('#timeInterval').val().split('--');
                var startTime = timeInterval[0];
                var endTime = timeInterval[1];
                d.vehicleId = vehicleId; //模糊查询
                d.startTime = startTime; //模糊查询
                d.endTime = endTime; //模糊查询
            };
            //表格setting
            var setting = {
                listUrl: url,
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                pageable: true, //是否分页
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                getAddress: true,//是否逆地理编码
                address_index: 7
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        //图表
        mileageChart: function () {
            //图表
            var myChart = echarts.init(document.getElementById('mileageChart'));
            var option = {
                tooltip: {
                    trigger: 'axis',
                    textStyle: {
                        fontSize: 20
                    },
                    formatter: function (a) {
                        var unit = ['km', 'km/h', 'ACC'];
                        var relVal = "";
                        relVal = a[0].name;
                        if (a[0].data == null || a[0].data == 0) {
                            relVal = "无相关数据";
                        } else {
                            for (var i = 0; i < a.length; i++) {
                                if (a[i].seriesName == "ACC") {
                                    if (a[i].data == 1) {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：关闭";
                                    } else {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：开启";
                                    }
                                } else {
                                    if (a[i].seriesName == "里程") {
                                        if (a[i].data === "" || a[i].data == null) {
                                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：- " + unit[a[i].seriesIndex] + "";
                                        } else {
                                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + parseFloat(Number(a[i].data).toFixed(1)) + unit[a[i].seriesIndex] + "";
                                        }
                                    } else {
                                        if (a[i].data === "" || a[i].data == null) {
                                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "： - " + unit[a[i].seriesIndex] + "";
                                        } else {
                                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + parseFloat(Number(a[i].data).toFixed(2)) + unit[a[i].seriesIndex] + "";
                                        }

                                    }

                                }
                            }
                        }
                        return relVal;
                    }
                },
                legend: {
                    selected: {
                        '总里程': true,
                        '速度': true,
                        'ACC': true,
                    },
                    left: 'left',
                    data: ['总里程', '速度', 'ACC']
                },
                toolbox: {
                    show: false
                },
                xAxis: {
                    type: 'category',
                    boundaryGap: false,
                    data: vTime //数据日期
                },
                yAxis: [
                    {
                        type: 'value',
                        name: '总里程(km)',
                        position: 'left',
                        scale: true,
                        minInterval: 1,
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine: {
                            show: false
                        }
                    },
                    {
                        type: 'value',
                        name: '速度(km/h)',
                        scale: true,
                        min: 0,
                        max: 240,
                        position: 'right',
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine: {
                            show: false
                        }
                    },
                    {
                        type: 'value',
                        name: 'ACC',
                        scale: true,
                        min: 0,
                        max: 1,
                        splitNumber: 1,
                        position: 'right',
                        offset: 60,
                        axisLabel: {
                            // formatter: '{value}',
                            formatter: function (value) {
                                if (value == 0 || value == "0") {
                                    return '开';
                                } else {
                                    return '关';
                                }
                            },
                        },
                        splitLine: {
                            show: false
                        }
                    }
                ],
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
                series: [
                    {
                        name: '总里程',
                        yAxisIndex: 0,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(109, 207, 246)'
                            }
                        },
                        data: mileageData//总里程
                    },
                    {
                        name: '速度',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(145, 218, 0)'
                            }
                        },
                        data: speedData//速度
                    },
                    {
                        name: 'ACC',
                        yAxisIndex: 2,
                        type: 'line',
                        smooth: false,
                        symbol: 'none',
                        sampling: 'average',
                        step: true,
                        itemStyle: {
                            normal: {
                                color: 'rgb(199, 209, 223)'
                            }
                        },
                        areaStyle: {
                            normal: {
                                color: 'rgba(199, 209, 223,0.9)'
                            }
                        },
                        data: accData//ACC状态
                    }
                ]
            };
            myChart.setOption(option);
            window.onresize = myChart.resize;
            layer.closeAll('loading');
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
                tMonth = mailStatistics.doHandleMonth(tMonth + 1);
                tDate = mailStatistics.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * parseInt(num);
                today.setTime(end_milliseconds); // 注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = mailStatistics.doHandleMonth(endMonth + 1);
                endDate = mailStatistics.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " " + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = mailStatistics.doHandleMonth(vMonth + 1);
                vDate = mailStatistics.doHandleMonth(vDate);
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
                    vendMonth = mailStatistics.doHandleMonth(vendMonth + 1);
                    vendDate = mailStatistics.doHandleMonth(vendDate);
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
        getsTheCurrentTime: function () {
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
        //上一天
        upDay: function () {
            mailStatistics.startDay(1);
            $('#timeInterval').val(startTime + '--' + endTime);
            mailStatistics.inquireClick();
        },
        // 今天
        todayClick: function () {
            mailStatistics.getsTheCurrentTime();
            $('#timeInterval').val(startTime + '--' + endTime);
            mailStatistics.inquireClick();
        },
        // 前一天
        yesterdayClick: function () {
            mailStatistics.startDay(-1);
            $('#timeInterval').val(startTime + '--' + endTime);
            mailStatistics.inquireClick();
        },
        // 近三天
        nearlyThreeDays: function () {
            mailStatistics.startDay(-3);
            $('#timeInterval').val(startTime + '--' + endTime);
            mailStatistics.inquireClick();
        },
        // 近七天
        nearlySevenDays: function () {
            mailStatistics.startDay(-7);
            $('#timeInterval').val(startTime + '--' + endTime);
            mailStatistics.inquireClick();
        },
        leftClickVehicleClick: function () {
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
            mailStatistics.inquireClick();
        },
        rightClickVehicleClick: function () {
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
            $("#vehicle").val($(".table-condensed tr").eq(nowIndex - 1).attr("data-id"));
            mailStatistics.inquireClick();
        },
        endTimeClick: function () {
            var width = $(this).width();
            var offset = $(this).offset();
            var left = offset.left - (207 - width);
            $("#laydate_box").css("left", left + "px");
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
        //查询
        inquireClick: function () {
            mailStatistics.hideErrorMsg();
            var vehicleId = $("input[name='charSelect']").attr("data-id");
            var timeInterval = $('#timeInterval').val().split('--');
            var startTime = timeInterval[0];
            var endTime = timeInterval[1];
            if (!mailStatistics.validates()) {
                return;
            }
            if (vehicleId == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            } else {
                $("#charSelect-error").hide();
            }
            $("#travelTime").html("0");
            $("#stopTime").html("0");
            $("#totalMileage").html("0km");
            $("#averageSpeed").html("0km/h");
            $.ajax({
                type: "POST",
                url: "/clbs/v/meleMonitor/mileStatistics/getHistoryInfoByVid",
                data: {"vehicleId": vehicleId, "startTime": startTime, "endTime": endTime},
                dataType: "json",
                async: true,
                timeout: 30000, //超时时间设置，单位毫秒
                beforeSend: function () {
                    //异步请求时spinner出现
                    layer.load(2);
                },
                success: function (data) {
                    if (!data.success) {
                        layer.msg(historyinfoNull);
                        return;
                    } else {
                        /* var msg = $.parseJSON(data.msg);
                         // 解压缩
                         var responseData = JSON.parse(ungzip(msg.mileageData));
                         msg.mileageData = responseData;
                         mailStatistics.mileageData = msg.mileageData;
                         mailStatistics.speedData = msg.speedData;
                         mailStatistics.accData = msg.accData;
                         for(var i = 0, len = mailStatistics.accData.length; i < len; i++){
                             if(mailStatistics.accData[i] == 1){
                                 mailStatistics.accData[i] = 0;
                             }else if(mailStatistics.accData[i] == 0){
                                 mailStatistics.accData[i] = 1
                             };
                         };
                         mailStatistics.vTime = msg.vTime;*/
                        mileageData = [];
                        speedData = [];
                        accData = [];
                        vTime = [];
                        var msg = $.parseJSON(data.msg);
                        mailStatistics.mileageChart();
                        var positionals = JSON.parse(ungzip(msg.positionals));
                        var miles;
                        var speeds;
                        for (var i = 0; i < positionals.length; i++) {
                            miles = Number(positionals[i].mileageTotal === undefined ? 0 : positionals[i].mileageTotal);
                            speeds = Number(positionals[i].mileageSpeed === undefined ? 0 : positionals[i].mileageSpeed);
                            if (miles == NaN || miles == 0) {
                                miles = "";
                            }
                            if (speeds == null || miles == NaN || miles == 0) {
                                speeds = "";
                            }
                            var vtime = positionals[i].vtime;
                            if (i != positionals.length - 1) {
                                var vtime1 = positionals[i + 1].vtime;
                            }
                            var time = vtime1 - vtime;
                            if (time > 300) {
                                var j = Math.floor(time / 300);
                                for (var k = 0; k < j; k++) {
                                    vtime = vtime + 300;
                                    var time = mailStatistics.getTime(vtime);
                                    vTime.push(time);
                                    mileageData.push("");
                                    speedData.push("");
                                    accData.push("");
                                }
                            }
                            var time = mailStatistics.getTime(positionals[i].vtime);
                            var acc = parseInt(positionals[i].acc);
                            mileageData.push(miles);
                            speedData.push(speeds);
                            accData.push(acc);
                            vTime.push(time);
                        }
                        $("#travelTime").html(msg.travelTime);
                        $("#stopTime").html(msg.idleTime);
                        if (msg.totalMaile) {
                            var totalMaile = msg.totalMaile.replace(/公里/g, "km");
                            totalMaile = totalMaile.substring(0, totalMaile.length - 2);
                            $("#totalMileage").html(parseFloat(Number(totalMaile).toFixed(1)) + "km");
                        }
                        if (msg.averageVelocity) {
                            var averageVelocity = msg.averageVelocity.replace("公里/时", "km/h");
                            averageVelocity = averageVelocity.substring(0, averageVelocity.length - 4);
                            $("#averageSpeed").html(parseFloat(Number(averageVelocity).toFixed(1)) + "km/h");
                        }

                        //显示相关内容
                        $("#graphShow").css({"height": "680px", "display": "block"});
                        $(".carName,.left-btn,.right-btn,.item-title").css("display", "block");
                        $("#faChevronDown").removeClass("fa fa-chevron-up");
                        $("#faChevronDown").addClass("fa fa-chevron-down");
                        var brandName = $("input[name='charSelect']").val();
                        if (brandName.length > 8) {
                            $("#carName").attr("title", brandName).tooltip('fixTitle');
                            brandName = brandName.substring(0, 7) + '...';
                        }else{
                            $('#carName').removeAttr('data-original-title');
                        }
                        $("#carName").text(brandName);

                        //查询显示图表
                        mailStatistics.mileageChart();
                        mailStatistics.searchInitTabData("/clbs/v/meleMonitor/mileStatistics/list");
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    layer.closeAll('loading');
                    if (textStatus == "timeout") {
                        layer.msg("加载超时，请重试");
                    }
                },
            });
        },
        // 时间转换
        getTime: function (time) {
            var date = new Date(time * 1000)
                , y = date.getFullYear()
                , m = (date.getMonth() + 1) >= 10 ? (date.getMonth() + 1) : '0' + (date.getMonth() + 1)
                , d = date.getDate() >= 10 ? date.getDate() : '0' + date.getDate()
                , hh = date.getHours() >= 10 ? date.getHours() : '0' + date.getHours()
                , mm = date.getMinutes() >= 10 ? date.getMinutes() : '0' + date.getMinutes()
                , ss = date.getSeconds() >= 10 ? date.getSeconds() : '0' + date.getSeconds()
            return y + '-' + m + '-' + d + ' ' + hh + ':' + mm + ':' + ss;
        },
        faChevronDownClick: function () {
            if ($(this).hasClass("fa-chevron-up")) {
                $(this).attr("class", "fa fa-chevron-down");
                $("#graphShow").show();
            } else {
                $(this).attr("class", "fa fa-chevron-up");
                $("#graphShow").hide('300');
            }
        },
        rightArrow: function () {
            mailStatistics.startDay(-1);
            $('#timeInterval').val(startTime + '--' + endTime);
            mailStatistics.inquireClick();
        },
        leftArrow: function () {
            mailStatistics.startDay(+1);
            var dateValue = new Date().getTime();
            var startTimeValue = new Date(startTime.split(" ")[0].replace(/-/g, "/")).getTime();
            if (startTimeValue <= dateValue) {
                $('#timeInterval').val(startTime + '--' + endTime);
                mailStatistics.inquireClick();
            } else {
                layer.msg("暂时没办法穿越，明天我再帮您看吧！");
            }
        },
        showErrorMsg: function (msg, inputId) {
            if ($("#error_label").is(":hidden")) {
                $("#error_label").text(msg);
                $("#error_label").insertAfter($("#" + inputId));
                $("#error_label").show();
            } else {
                $("#error_label").is(":hidden");
            }
        },
        hideErrorMsg: function () {
            $("#error_label").hide();
        },
        fiterNumber: function (data) {
            if (data == null || data == undefined || data == "") {
                return data;
            } else {
                var data = data.toString();
                data = parseFloat(data);
                return data;
            }
        },
    }
    $(function () {
        $('input').inputClear();
        mailStatistics.init();
        mailStatistics.getsTheCurrentTime();
        $('#timeInterval').dateRangePicker({
            dateLimit: 7
        });
        $("#groupSelectSpan,#groupSelect").bind("click", mailStatistics.showMenu); //组织下拉显示
        //查询
        $("#inquireClick").on("click", mailStatistics.inquireClick);
        $("#todayClick").bind("click", mailStatistics.todayClick);
        $("#yesterdayClick,#right-arrow").bind("click", mailStatistics.yesterdayClick);
        $("#nearlyThreeDays").bind("click", mailStatistics.nearlyThreeDays);
        $("#nearlySevenDays").bind("click", mailStatistics.nearlySevenDays);
        $("#left-arrow").bind("click", mailStatistics.upDay);
        //图表显示
        $("#faChevronDown").on("click", mailStatistics.faChevronDownClick);
        $("#rightClickVehicle").on("click", mailStatistics.rightClickVehicleClick);
        $("#leftClickVehicle").on("click", mailStatistics.leftClickVehicleClick);
        $("#rightArrow").on("click", mailStatistics.rightArrow);
        $("#leftArrow").on("click", mailStatistics.leftArrow);

        $('input').inputClear().on('onClearEvent',function(e,data){
            var id = data.id;
            if(id == 'groupSelect'){
                var param = $("#groupSelect").val();
                mailStatistics.searchVehicleTree(param);
            };
        });

        /**
         * 监控对象树模糊查询
         */
        var inputChange;
        $("#groupSelect").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            };
            $("#charSelect").val("").attr("data-id", "").bsSuggest("destroy");
            inputChange = setTimeout(function () {
                if (isSearch){
                    var param = $("#groupSelect").val();
                    mailStatistics.searchVehicleTree(param);
                }
                isSearch = true;
            }, 500);
        });

        //菜单切换重新绘制
        $("#toggle-left").bind("click", function () {
            setTimeout(function () {
                mailStatistics.mileageChart();
            }, 500)
        });
        $('#queryType').on('change', function (){
            var param=$("#groupSelect").val();
            mailStatistics.searchVehicleTree(param);
        });
    })
})(window, $)