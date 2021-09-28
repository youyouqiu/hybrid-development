(function ($, window) {
    var startTime;
    var endTime;
    var date = [];//图形表时间
    var accTemp = [];//acc

    var speed = [];//速度
    var instanceWeightArr = []; //瞬时载重
    var weightAdArr = []; //载重相对值
    var originalAdArr = [];//  AD 值
    var floatAdArr = [];// 浮动零点
    var duration_state = [];// 状态持续时长
    var loadStatusArr = [];// 载重状态

    /*状态数组, 用于设置图标背景色*/
    var noLoadStatusArr = []; // 空载
    var underLoadStatusArr = [];// 轻载
    var heavyLoadStatusArr = [];// 重载
    var fullLoadStatusTimeArr = [];// 满载
    var overLoadStatusTimeArr = [];// 超载

    var myChart;
    var option;
    // 车辆list
    var vehicleList = JSON.parse($("#vehicleList").attr("value"));

    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询？

    var zTreeIdJson = {};
    var size;//当前权限监控对象数量
    var checkFlag = false;
    var searchSate = true;

    var ifAllCheck = true; //刚进入页面小于5000自动勾选

    var sensorSequence = '0';

    var ifEmptyData = [] //是否空数据

    var _vehicleId;

    var myTable;

    var searchFlag = true;

    var exportFlag = false;

    var allTableDataColumns = [{
        //第一列，用来显示序号
        "data": null,
        "class": "text-center"
    }, {
        "data": "plateNumber", //监控对象
        "class": "text-center"
    }, {
        "data": "groupName", //所属企业
        "class": "text-center"
    }, {
        "data": "status", //载重状态
        "class": "text-center",
        render: function (data, type, row, meta) {
            if (row.status == '1') {
                return '<i class="noLoadTimeState"></i> 空载';
            } else if (row.status == '2') {
                return '<i class="fullLoadTimeState"></i> 满载';
            } else if (row.status == '3') {
                return '<i class="overLoadTimeState"></i> 超载';
            } else if (row.status == '4') {
                return '<i class="holdLoadTimeState"></i> 装载';
            } else if (row.status == '5') {
                return '<i class="unloadTimeState"></i> 卸载';
            } else if (row.status == '6') {
                return '<i class="underLoadTimeState"></i> 轻载';
            } else if (row.status == '7') {
                return '<i class="heavyLoadTimeState"></i> 重载';
            } 
                return '';
            
        }
    }, {
        "data": "vtimeStr", //时间
        "class": "text-center"
    }, {
        "data": "continueTimeStr", //状态持续时长
        "class": "text-center"
    }, {
        "data": "instanceWeight", //瞬时载重
        "class": "text-center"
    }, {
        "data": null,
        "class": "text-center",
        render: function (data, type, row, meta) {
            return "加载中..."
        }
    }];

    loadStatistic = {
        init: function () {
            if (searchSate) {
                for (var i = 0, len = accTemp.length; i < len; i++) {
                    if (accTemp[i] == 1) {
                        accTemp[i] = 0;
                    } else if (accTemp[i] == 0) {
                        accTemp[i] = 1
                    }
                    ;
                }
                ;
                searchSate = false;
            }
            myChart = echarts.init(document.getElementById('sjcontainer'));

            option = {
                tooltip: {
                    trigger: 'axis',
                    textStyle: {
                        fontSize: 20
                    },
                    formatter: function (loadData) {
                        console.log(loadData);
                        var relVal = "";
                        var unit = ['', '', 'km/h', 'kg', '', '', '', ''];
                        if (loadData[0]) {
                            relVal = loadData[0].name;
                            var filterSeriesNameArr = ['空载', '满载', '超载', '装载', '卸载', '轻载', '重载'];
                            if (loadData[0].data == null && $.inArray(loadData[0].seriesName, filterSeriesNameArr) == -1) {
                                relVal = "无相关数据";
                            }

                            var ifDataNull = false;
                            loadData.forEach(function (val, index) {
                                if (val.data != null) {
                                    ifDataNull = true;
                                }
                            });

                            if (!ifDataNull) {
                                relVal = "无相关数据";
                                return relVal;
                            }

                            if (ifEmptyData[loadData[0].dataIndex] == 'empty') {
                                relVal = '无相关数据';
                                return relVal;
                            }

                            var isEffectiveData = true; // 是否无效数据
                            for (var i = 0; i < loadData.length; i++) {

                                var seriesName = loadData[i].seriesName;

                                if (seriesName == '' || $.inArray(seriesName, filterSeriesNameArr) != -1) {
                                    continue;
                                }
                                if (seriesName == '载重状态') {
                                    var loadStatus = loadData[i].value;
                                    console.log(loadData[i]);
                                    if (loadData[i].data != undefined) {
                                        relVal += "<br/>载重状态：" + loadStatus;
                                        isEffectiveData = false;
                                    } else {
                                        relVal += "<br/>载重状态：";
                                    }
                                } else {
                                    if (loadData[i].value === '' || loadData[i].value == undefined) {
                                        relVal += "<br/>" + seriesName + "：";
                                    } else {
                                        relVal += "<br/>" + seriesName + "：" + loadData[i].value + unit[loadData[i].seriesIndex] + "";
                                    }
                                }
                            }
                            return relVal;
                        }
                    }
                },
                legend: {
                    data: ['速度', '瞬时载重', '空载', '轻载', '重载', '满载', '超载'],
                    left: 'auto'
                },
                toolbox: {
                    show: false
                },
                xAxis: {
                    type: 'category',
                    boundaryGap: false,
                    data: date
                },
                yAxis: [
                    {
                        type: 'value',
                        name: '速度(km/h)',
                        scale: true,
                        min: 0,
                        max: 240,
                        position: 'left',
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine: {
                            show: false
                        }
                    }, {
                        type: 'value',
                        name: "瞬时载重",
                        scale: true,
                        position: 'right',
                        // offset: 60,
                        min: 0,
                        // max: 600,
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine: {
                            show: false
                        }
                    }, {
                        // 隐藏的Y轴, 用于填充背景色, 状态值push0在最底部,如果是1，则在顶部。 series中设置: areaStyle
                        type: 'value',
                        name: '状态',
                        scale: true,
                        show: false,
                        position: 'right',
                        offset: 60,
                        min: 0,
                        max: 1,
                        axisLabel: {
                            formatter: '{value}'
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
                        name: '',
                        type: 'line',
                        yAxisIndex: 2,
                        // smooth: true,
                        symbol: 'none',
                        symbolSize: 15,
                        showSymbol: false,
                        show: false,
                        sampling: 'average',
                        data: date,
                    },
                    {
                        name: '载重状态',
                        type: 'line',
                        yAxisIndex: 2,
                        // smooth: true,
                        symbol: 'none',
                        symbolSize: 15,
                        showSymbol: false,
                        show: false,
                        sampling: 'average',
                        data: loadStatusArr
                    }, {
                        name: '速度',
                        yAxisIndex: 0,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        showSymbol: true,
                        itemStyle: {
                            normal: {
                                color: 'rgb(153, 204, 0)'
                            }
                        },
                        label: {
                            normal: {
                                formatter: '{value}km/h'
                            }
                        },
                        data: speed
                    }, {
                        name: '瞬时载重',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        showSymbol: true,
                        itemStyle: {
                            normal: {
                                color: '#42415a'
                            }
                        },
                        data: instanceWeightArr
                    }, {
                        name: '状态持续时长',
                        type: 'line',
                        yAxisIndex: 2,
                        // smooth: true,
                        symbol: 'none',
                        symbolSize: 15,
                        showSymbol: false,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgba(0, 0, 0, 0)' // 线的颜色是透明的
                            }
                        },
                        data: duration_state,
                    }, {
                        name: "载重相对值",
                        yAxisIndex: 2,
                        type: 'line',
                        symbolSize: 0, // symbol的大小设置为0
                        showSymbol: false, // 不显示symbol
                        itemStyle: {
                            normal: {
                                color: 'rgba(0, 0, 0, 0)' // 线的颜色是透明的
                            }
                        },
                        data: weightAdArr
                    }, {
                        name: 'AD值',
                        type: 'line',
                        yAxisIndex: 2,
                        smooth: true,
                        symbol: 'none',
                        symbolSize: 15,
                        showSymbol: false,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgba(0, 0, 0, 0)' // 线的颜色是透明的
                            }
                        },
                        data: originalAdArr
                    }, {
                        name: '浮动零点',
                        yAxisIndex: 2,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        showSymbol: false,
                        itemStyle: {
                            normal: {
                                color: 'rgba(0, 0, 0, 0)' // 线的颜色是透明的
                            }
                        },
                        label: {
                            normal: {
                                formatter: '{value}km/h'
                            }
                        },
                        data: floatAdArr
                    },
                    /* 用于图标背景图展示*/
                    {
                        name: '空载',
                        type: 'line',
                        step: 'end',
                        yAxisIndex: 2,
                        // smooth: true,
                        symbol: 'none',
                        symbolSize: 15,
                        showSymbol: false,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: '#cdcdcd'
                            }
                        },
                        areaStyle: {
                            normal: {
                                color: '#cdcdcd'
                            }
                        },
                        data: noLoadStatusArr
                    }, {
                        name: '轻载',
                        type: 'line',
                        step: 'end',
                        yAxisIndex: 2,
                        // smooth: true,
                        symbol: 'none',
                        symbolSize: 15,
                        showSymbol: false,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: '#bddbff'
                            }
                        },
                        areaStyle: {
                            normal: {
                                color: '#bddbff'
                            }
                        },
                        data: underLoadStatusArr
                    }, {
                        name: '重载',
                        type: 'line',
                        step: 'end',
                        yAxisIndex: 2,
                        // smooth: true,
                        symbol: 'none',
                        symbolSize: 15,
                        showSymbol: false,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: '#95b6f2'
                            }
                        },
                        areaStyle: {
                            normal: {
                                color: '#95b6f2'
                            }
                        },
                        data: heavyLoadStatusArr
                    }, {
                        name: '满载',
                        type: 'line',
                        step: 'end',
                        yAxisIndex: 2,
                        // smooth: true,
                        symbol: 'none',
                        symbolSize: 15,
                        showSymbol: false,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: '#8b84eb'
                            }
                        },
                        areaStyle: {
                            normal: {
                                color: '#8b84eb'
                            }
                        },
                        data: fullLoadStatusTimeArr
                    }, {
                        name: '超载',
                        type: 'line',
                        step: 'end',
                        yAxisIndex: 2,
                        // smooth: true,
                        symbol: 'none',
                        symbolSize: 15,
                        showSymbol: false,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: '#f8a023'
                            }
                        },
                        areaStyle: {
                            normal: {
                                color: '#f8a023'
                            }
                        },
                        data: overLoadStatusTimeArr
                    }
                ]
            };
            myChart.setOption(option);
            window.onresize = myChart.resize;
        },
        treeInit: function () {
            var setting = {
                async: {
                    url: loadStatistic.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: loadStatistic.ajaxDataFilter
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
                    beforeClick: loadStatistic.beforeClickVehicle,
                    onCheck: loadStatistic.onCheckVehicle,
                    beforeCheck: loadStatistic.zTreeBeforeCheck,
                    onExpand: loadStatistic.zTreeOnExpand,
                    //beforeAsync: oilstatiscal.zTreeBeforeAsync,
                    onAsyncSuccess: loadStatistic.zTreeOnAsyncSuccess,
                    onNodeCreated: loadStatistic.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
            $("[data-toggle='tooltip']").tooltip();
        },
        //模糊查询树
        searchVehicleTree: function (param) {

            ifAllCheck = false;//模糊查询不自动勾选

            crrentSubV = [];
            console.log(param)
            $('#charSelect').val('');
            if (param == null || param == undefined || param == '') {
                bflag = true;
                loadStatistic.treeInit()
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
                        dataFilter: loadStatistic.ajaxQueryDataFilter
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
                        beforeClick: loadStatistic.beforeClickVehicle,
                        onCheck: loadStatistic.onCheckVehicle,
                        onExpand: loadStatistic.zTreeOnExpand,
                        onNodeCreated: loadStatistic.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData))
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
            loadStatistic.getCharSelect(treeObj);

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
                searchFlag = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    loadStatistic.validates();
                }, 600);
            }
            if (treeNode.type != "assignment" || (treeNode.type == "assignment" && treeNode.children != undefined)) {
                loadStatistic.getCharSelect(zTree);
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
            var veh = [];
            var vid = [];
            for (var i = 0; i < nodes.length; i++) {
                if (nodes[i].type == "vehicle" || nodes[i].type == "people" || nodes[i].type == "thing") {
                    veh.push(nodes[i].name)
                    vid.push(nodes[i].id)
                }
            }
            var vehName = loadStatistic.unique(veh);
            var vehId = loadStatistic.unique(vid);
            $("#charSelect").empty();
            var deviceDataList = {value: []};
            for (var j = 0; j < vehName.length; j++) {
                for (var k = 0; k < vehicleList.length; k++) {
                    if (vehId[j] == vehicleList[k].vehicleId) {
                        deviceDataList.value.push({
                            // 避免unique 后id和名称对不上
                            name: vehicleList[k].brand,
                            id: vehId[j]
                        });
                    }
                }
            }
            ;
            $("#charSelect").bsSuggest({
                indexId: 1, //data.value 的第几个数据，作为input输入框的内容
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
            $("#groupSelect,#groupSelectSpan").bind("click", loadStatistic.showMenu);
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
                var inpwidth = $("#groupSelect").width();
                var spwidth = $("#groupSelectSpan").width();
                var allWidth = inpwidth + spwidth + 21;
                if (navigator.appName == "Microsoft Internet Explorer") {
                    $("#menuContent").css("width", (inpwidth + 7) + "px");
                } else {
                    $("#menuContent").css("width", allWidth + "px");
                }
                $(window).resize(function () {
                    var inpwidth = $("#groupSelect").width();
                    var spwidth = $("#groupSelectSpan").width();
                    var allWidth = inpwidth + spwidth + 21;
                    if (navigator.appName == "Microsoft Internet Explorer") {
                        $("#menuContent").css("width", (inpwidth + 7) + "px");
                    } else {
                        $("#menuContent").css("width", allWidth + "px");
                    }
                })
                $("#menuContent").slideDown("fast");
            } else {
                $("#menuContent").is(":hidden");
            }
            $("body").bind("mousedown", loadStatistic.onBodyDown);
        },
        hideMenu: function () {
            $("#menuContent").fadeOut("fast");
            $("body").unbind("mousedown", loadStatistic.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "menuContent" || $(event.target).parents("#menuContent").length > 0)) {
                loadStatistic.hideMenu();
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
                tMonth = loadStatistic.doHandleMonth(tMonth + 1);
                tDate = loadStatistic.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * parseInt(num);
                today.setTime(end_milliseconds); // 注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = loadStatistic.doHandleMonth(endMonth + 1);
                endDate = loadStatistic.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " " + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = loadStatistic.doHandleMonth(vMonth + 1);
                vDate = loadStatistic.doHandleMonth(vDate);
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
                    vendMonth = loadStatistic.doHandleMonth(vendMonth + 1);
                    vendDate = loadStatistic.doHandleMonth(vendDate);
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
        //上一天(此段js可以删除，页面没有这个按钮)
        upDay: function () {
            loadStatistic.startDay(1);
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (charNum != "" && groupValue != "") {
                var dateValue = new Date().getTime();
                var startTimeValue = new Date(startTime.split(" ")[0].replace(/-/g, "/")).getTime();
                if (startTimeValue <= dateValue) {
                    var parme = {
                        vehicleId: charNum,
                        startTimeStr: startTime,
                        endTimeStr: endTime,
                        sensorSequence: sensorSequence
                    }
                    loadStatistic.inquireChart(parme);
                } else {

                    var timeInterval = $('#timeInterval').val().split('--');
                    startTime = timeInterval[0];
                    endTime = timeInterval[1];

                    layer.msg("暂时没办法穿越，明天我再帮您看吧！");
                }
            } else {
                layer.msg("请选择监控对象！", {move: false});
            }
        },
        // 今天
        todayClick: function () {
            loadStatistic.nowDay();
            var charNum = $("#charSelect").attr("data-id");
            _vehicleId = charNum;
            if (!loadStatistic.validates()) return;
            var parme = {
                vehicleId: charNum,
                startTimeStr: startTime,
                endTimeStr: endTime,
                sensorSequence: sensorSequence
            }
            loadStatistic.inquireChart(parme);
        },
        // 前一天
        yesterdayClick: function () {
            loadStatistic.startDay(-1);
            var startValue = $("#startTime");
            var charNum = $("#charSelect").attr("data-id");
            _vehicleId = charNum;
            if (!loadStatistic.validates()) return;
            var parme = {
                vehicleId: charNum,
                startTimeStr: startTime,
                endTimeStr: endTime,
                sensorSequence: sensorSequence
            };
            loadStatistic.inquireChart(parme);
        },
        // 近三天
        nearlyThreeDays: function () {
            loadStatistic.startDay(-3);
            var charNum = $("#charSelect").attr("data-id");
            _vehicleId = charNum;
            if (!loadStatistic.validates()) return;
            var parme = {
                vehicleId: charNum,
                startTimeStr: startTime,
                endTimeStr: endTime,
                sensorSequence: sensorSequence
            }
            loadStatistic.inquireChart(parme);
        },
        // 近七天
        nearlySevenDays: function () {
            loadStatistic.startDay(-7);
            var charNum = $("#charSelect").attr("data-id");
            _vehicleId = charNum;
            if (!loadStatistic.validates()) return;
            var parme = {
                vehicleId: charNum,
                startTimeStr: startTime,
                endTimeStr: endTime,
                sensorSequence: sensorSequence
            }
            loadStatistic.inquireChart(parme);
        },
        // 查询
        inquireClick: function () {
            var groupValue = $("#groupSelect").val();
            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];
            var charNum = $("#charSelect").attr("data-id");
            _vehicleId = charNum;
            if (!loadStatistic.validates()) return;
            var parme = {
                vehicleId: charNum,
                startTimeStr: startTime,
                endTimeStr: endTime,
                sensorSequence: sensorSequence
            }
            loadStatistic.inquireChart(parme);
        },
        inquireChart: function (parme) {
            date = [];
            speed = [];

            var brandName = $("input[name='charSelect']").val();
            if (brandName.length > 8) {
                $("#carName").attr("title", brandName).tooltip('fixTitle');
                brandName = brandName.substring(0, 7) + '...';
            } else {
                $('#carName').removeAttr('data-original-title')
            }
            $("#carName").text(brandName);
            exportFlag = true;
            $(".toopTip-btn-left,.toopTip-btn-right").css("display", "inline-block");
            json_ajax('post', '/clbs/v/statistic/loadManagementStatistic/getLoadChartInfo', 'json', true, parme, loadStatistic.ChartajaxDataCallback)
        },
        clearChartArr: function () {
            date = [];// 时间
            speed = []; // 速度
            duration_state = []; // 持续时长
            instanceWeightArr = []; // 瞬时载重
            loadStatusArr = []; // 载重状态
            weightAdArr = []; //载重相对值
            originalAdArr = [];//  AD 值
            floatAdArr = [];// 浮动零点

            noLoadStatusArr = [];
            underLoadStatusArr = [];
            heavyLoadStatusArr = [];
            fullLoadStatusTimeArr = [];
            overLoadStatusTimeArr = [];
        },
        // 图表数据
        ChartajaxDataCallback: function (data) {
            loadStatistic.clearChartArr();
            ifEmptyData = [];

            var obj = data.obj;
            if (data.success && obj.resultList) {
                var loadInfoList = JSON.parse(ungzip(obj.resultList));

                layer.closeAll('loading');
                $('#timeInterval').val(startTime + '--' + endTime);

                $('#noLoadTime').text(obj.noLoadTime);
                $('#underLoadTime').text(obj.underLoadTime);
                $('#heavyLoadTime').text(obj.heavyLoadTime);
                $('#fullLoadTime').text(obj.fullLoadTime);
                $('#overLoadTime').text(obj.overLoadTime);

                if (loadInfoList.length > 0) {
                    $("#alarmExport").removeAttr("disabled");
                    var len = loadInfoList.length - 1;
                    for (var i = len; i >= 0; i--) {
                        var loadInfo = loadInfoList[i];
                        //如果loadInfo为空,说明这段数据是空数据,啥都不显示
                        if (loadInfo == null || loadInfo.effectiveData == '3') {
                            ifEmptyData.push('empty');
                            date.push("");// 时 间
                            speed.push(""); // 速度
                            duration_state.push(""); // 持续时长
                            instanceWeightArr.push(""); // 瞬时载重
                            loadStatusArr.push(""); // 载重状态
                            weightAdArr.push(""); //载重相对值
                            originalAdArr.push("");//  AD 值
                            floatAdArr.push("");// 浮动零点
                            // loadStatistic.getLoadStatus(-1);
                            noLoadStatusArr.push(0);
                            fullLoadStatusTimeArr.push(0);
                            overLoadStatusTimeArr.push(0);
                            underLoadStatusArr.push(0);
                            heavyLoadStatusArr.push(0);
                        } else {
                            ifEmptyData.push('1');
                            //无效数据, 只展示时间和速度
                            if (loadInfo.effectiveData == '1') {
                                if (loadInfo.speed == null) {
                                    loadInfo.speed = 0;
                                }
                                loadInfo.continueTimeStr = '';
                                // 清空其他值
                                loadInfo.instanceWeight = '';
                                loadInfo.status = '';
                                loadInfo.weightAd = '';
                                loadInfo.originalAd = '';
                                loadInfo.floatAd = '';
                            }

                            date.push(loadInfo.vtimeStr);// 时 间
                            speed.push(loadInfo.speed); // 速度
                            duration_state.push(loadInfo.continueTimeStr); // 持续时长
                            instanceWeightArr.push(loadInfo.instanceWeight); // 瞬时载重
                            loadStatusArr.push(loadStatistic.getLoadStatusChinaName(loadInfo.status)); // 载重状态
                            // loadStatusArr.push(loadInfo.status);
                            weightAdArr.push(loadInfo.weightAd); //载重相对值
                            originalAdArr.push(loadInfo.originalAd);//  AD 值
                            floatAdArr.push(loadInfo.floatAd);// 浮动零点
                            loadStatistic.getLoadStatus(loadInfo.status);
                        }
                    }

                    $("#graphShow").show();
                    $("#showClick").attr("class", "fa fa-chevron-down");
                    searchState = true;
                    loadStatistic.init()
                } else {
                    $("#showClick").attr("class", "fa fa-chevron-up");
                    $("#graphShow").hide();
                    $("#noLoadTime").text("0小时0分");
                    $("#underLoadTime").text("0小时0分");
                    $("#heavyLoadTime").text("0小时0分");
                    $("#fullLoadTime").text("0小时0分");
                    $("#overLoadTime").text("0小时0分");
                    $("#oilTable_wrapper").children("div.row").hide();
                }
                $("#graphShow").show();
                $("#showClick").attr("class", "fa fa-chevron-down");
                searchState = true;
                loadStatistic.init();

                $('.dataTableShow li,.tableFEStyle .tableBox').removeClass('active');
                $('#allReport,#loadTableBox').addClass('active');

                if (loadStatistic.validates()) {
                    $("#loadTable tbody").html("");
                    var parme = {
                        'url': '/clbs/v/statistic/loadManagementStatistic/getTotalLoadInfoList',
                        'vehicleId': $("#charSelect").attr("data-id"),
                        'startTimeStr': startTime,
                        'endTimeStr': endTime,
                        'sensorSequence': sensorSequence
                    }

                    $('#loadTableBox').removeClass('active').addClass('active');
                    $('#classifyTableBox').removeClass('active');

                    loadStatistic.infoinputTab(parme, allTableDataColumns, 'loadTable0');
                }
            } else {
                loadStatistic.clearChartArr();
                loadStatistic.init();
                if (myTable != undefined) {
                    myTable.dataTable.clear();
                    myTable.dataTable.draw();
                }
                layer.msg(data.msg, {move: false});
            }
        },
        // 勾选数据
        //时间戳转换日期 
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
        //创建表格
        infoinputTab: function (parme, columns, tableDiv) {
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var ajaxDataParamFun = function (d) {
                d.vehicleId = parme.vehicleId; //模糊查询
                d.startTimeStr = parme.startTimeStr;
                d.endTimeStr = parme.endTimeStr;
                d.sensorSequence = parme.sensorSequence;
                d.status = parme.status;
            };


            //表格setting
            var setting = {
                listUrl: parme.url,
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: tableDiv, //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                getAddress: true,//是否逆地理编码
                address_index: 8,
                drawCallbackFun: loadStatistic.drawCallbackFun
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);


            myTable.init();
        },
        // 创建工作数据待机数据停机数据表格
        drawCallbackFun: function (e, settings, json) { //给重要的数据加上颜色

        },
        toHHMMSS: function (data) {
            var totalSeconds = data * 60 * 60;
            var hour = Math.floor(totalSeconds / 60 / 60);
            var minute = Math.floor(totalSeconds / 60 % 60);
            var second = Math.floor(totalSeconds % 60);
            return hour != 0 ? hour + "小时" + minute + "分" + second + "秒" : minute != 0 ? minute + "分" + second + "秒" : second != 0 ? second + "秒" : 0
        },
        removeClass: function () {
            var dataList = $(".dataTableShow");
            for (var i = 0; i < 3; i++) {
                dataList.children("li").removeClass("active");
            }
        },
        allReportClick: function (e) {
            console.log($(this), $(e.target).attr('data-index'))
            var dataIndex = $(e.target).attr('data-index');

            loadStatistic.removeClass();
            $(this).addClass("active");
            $('#loadTableBox').removeClass('active').addClass('active'); // ?
            $('#classifyTableBox').removeClass('active'); // ?
            // 载重状态 01: 空载； 02: 满载； 03: 超载； 04: 装载； 05: 卸载；06: 轻载；07: 重载； null:所有状态
            $('.tableBox').removeClass('active');
            $('.tableBox').eq(dataIndex).addClass("active");
            var status;
            var index = $('.allReportq.active').index();
            switch (index) {
                case 0:
                    status = undefined;
                    break
                case 1:
                    status = 3;
                    break
                case 2:
                    status = 2;
                    break
                case 3:
                    status = 7;
                    break
                case 4:
                    status = 6;
                    break
                case 5:
                    status = 1;
                    break
            }
            if (date.length != 0) {
                console.log(date, '时间？')
                if (loadStatistic.validates()) {
                    $("#loadTable tbody").html("");
                    var parme = {
                        'url': '/clbs/v/statistic/loadManagementStatistic/getTotalLoadInfoList',
                        'vehicleId': _vehicleId,
                        'startTimeStr': startTime,
                        'endTimeStr': endTime,
                        'sensorSequence': sensorSequence,
                        'status': status
                    }
                    loadStatistic.infoinputTab(parme, allTableDataColumns, 'loadTable' + index.toString());
                }
            }
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
                // if(value[i]!=0){
                if (value[i] == null || value[i] == "" || typeof(value[i]) == "undefined" || value[i] == 0) {
                    value.splice(i, 1);
                    i = i - 1;
                }
                // }
            }
            return value
        },
        //判断数组是否为空
        arrayIsNull: function (value) {
            if (value === undefined || value.length == 0) {
                return false;
            }
            return true;
        },
        validates: function () {
            return $("#oilist").validate({
                rules: {
                    groupSelect: {
                        zTreeChecked: "treeDemo"
                    },
                    groupId: {
                        required: true
                    },
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval",
                        compareDateDiff: "#timeInterval"
                    },
                    startTime: {
                        required: true
                    },
                    charSelect: {
                        required: true
                    }
                },
                messages: {
                    groupSelect: {
                        zTreeChecked: vehicleSelectBrand
                    },
                    groupId: {
                        required: "不能为空"
                    },
                    endTime: {
                        required: "请选择结束日期！",
                        compareDate: "结束日期必须大于开始日期!",
                        compareDateDiff: "查询的日期必须小于一周"
                    },
                    startTime: {
                        required: "请选择开始日期！",
                    },
                    charSelect: {
                        required: "监控对象不能为空"
                    }
                }
            }).form();
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
            if (nowIndex == 0) {
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
        },
        loadChart: function () {
            $(this).removeClass('active').addClass('active').siblings().removeClass('active');
            sensorSequence = $(this).attr('sensorSequenceVal');
            loadStatistic.inquireClick()
        },
        getLoadStatusChinaName: function (loadStatus) {
            var statusStr = '';
            switch (loadStatus) {
                case 1:
                    statusStr = '空载';
                    break;
                case 2:
                    statusStr = '满载';
                    break;
                case 3:
                    statusStr = '超载';
                    break;
                case 4:
                    statusStr = '装载';
                    break;
                case 5:
                    statusStr = '卸载';
                    break;
                case 6:
                    statusStr = '轻载';
                    break;
                case 7:
                    statusStr = '重载';
                    break;
                default:
                    break
            }
            return statusStr;
        },
        getLoadStatus: function (loadStatus) {
            if (loadStatus == 1) {
                noLoadStatusArr.push(loadStatus);
            } else {
                noLoadStatusArr.push(0);
            }
            if (loadStatus == 2) {
                fullLoadStatusTimeArr.push(loadStatus);
            } else {
                fullLoadStatusTimeArr.push(0);
            }
            if (loadStatus == 3) {
                overLoadStatusTimeArr.push(loadStatus);
            } else {
                overLoadStatusTimeArr.push(0);
            }
            if (loadStatus == 6) {
                underLoadStatusArr.push(loadStatus);
            } else {
                underLoadStatusArr.push(0);
            }
            if (loadStatus == 7) {
                heavyLoadStatusArr.push(loadStatus);
            } else {
                heavyLoadStatusArr.push(0);
            }
        },
        exportAlarm: function () {
            var status;
            var index = $('.allReportq.active').index();
            switch (index) {
                case 0:
                    status = undefined;
                    if(getRecordsNum('loadTable' + index + '_info') > 60000){
                        return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
                    }
                    break
                case 1:
                    status = 3;
                    if(getRecordsNum('loadTable' + index + '_info') > 60000){
                        return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
                    }
                    break
                case 2:
                    status = 2;
                    if(getRecordsNum('loadTable' + index + '_info') > 60000){
                        return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
                    }
                    break
                case 3:
                    status = 7;
                    if(getRecordsNum('loadTable' + index + '_info') > 60000){
                        return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
                    }
                    break
                case 4:
                    status = 6;
                    if(getRecordsNum('loadTable' + index + '_info') > 60000){
                        return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
                    }
                    break
                case 5:
                    status = 1;
                    if(getRecordsNum('loadTable' + index + '_info') > 60000){
                        return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
                    }
                    break
            }
            if (!exportFlag) {
                layer.msg("无数据可以导出");
                return
            }

            var pdata = {"sensorSequence": sensorSequence, "status": status};

            // 请求路径
            var url = "/clbs/v/statistic/loadManagementStatistic/export";

            if (pdata.status === undefined) {
                delete pdata.status;
            }

            // 发送请求给服务器
            exportExcelUseForm(url, pdata);
        },
    }
    $(function () {
        $('input').inputClear();
        loadStatistic.treeInit();
        $("#toggle-left").bind("click", function () {
            setTimeout(function () {
                loadStatistic.init();
            }, 500)
        });
        Array.prototype.isHas = function (a) {
            if (this.length === 0) {
                return false
            }
            ;
            for (var i = 0; i < this.length; i++) {
                if (this[i].seriesName === a) {
                    return true
                }
            }
        };
        loadStatistic.nowDay();
        $('#timeInterval').dateRangePicker({dateLimit: 7});
        $("#todayClick").bind("click", loadStatistic.todayClick);
        $("#yesterdayClick,#right-arrow").bind("click", loadStatistic.yesterdayClick);
        $("#nearlyThreeDays").bind("click", loadStatistic.nearlyThreeDays);
        $("#nearlySevenDays").bind("click", loadStatistic.nearlySevenDays);
        $("#inquireClick").bind("click", loadStatistic.inquireClick);
        $("#showClick").bind("click", loadStatistic.showClick);
        $("#left-arrow").bind("click", loadStatistic.upDay);
        // $('#allReport').bind("click",loadStatistic.allReportClick);
        $('.allReportq').bind("click", loadStatistic.allReportClick);
        $("#endTime").bind("click", loadStatistic.endTimeClick);
        // $("#groupSelectSpan,#groupSelect").bind("click",loadStatistic.showMenu); //组织下拉显示
        $("#groupSelect").bind("click", showMenuContent);
        $('#load1').bind('click', loadStatistic.loadChart) //载重1
        $('#load2').bind('click', loadStatistic.loadChart) //载重2
        $("#alarmExport").bind("click", loadStatistic.exportAlarm);

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                loadStatistic.searchVehicleTree(param);
            }
            ;
        });

        /**
         * 监控对象树模糊查询
         */
        var inputChange;
        $("#groupSelect").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            ;
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                searchFlag = true;
            }
            $("#charSelect").val("").attr("data-id", "").bsSuggest("destroy");
            inputChange = setTimeout(function () {
                if (searchFlag) {
                    var param = $("#groupSelect").val();
                    loadStatistic.searchVehicleTree(param);
                }
                searchFlag = true;
            }, 500);
        });
        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            loadStatistic.searchVehicleTree(param);
        });
    });
}($, window));