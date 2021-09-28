(function ($, window) {
    var startTime;
    var endTime;
    // var base = +new Date(2016, 5, 1, 00, 00, 00);
    var date = [];//图形表时间
    var oil = [];//总油量
    var oilOne = [];//油量1
    var oilTwo = [];//油量2
    var dataSets = [];//table数据
    var amountDataSets = [];//
    var spillDataSets = [];//
    var mileage = [];//里程
    var speed = [];//速度
    var tmp = [];//燃油温差1
    var tmp2 = [];//燃油温差2
    var etmp = [];//环境温差
    var oilOneTemp = [];//燃油温度1
    var oilTwoTemp = [];//燃油温度2
    var ENVOneTemp = [];//环境温度1
    var ENVTwoTemp = [];//环境温度2
    var envTemp = [];//空调
    var accTemp = [];//acc
    var oilHeightOne = []; // 主油箱
    var oilHeightTwo = []; // 副油箱
    var chart;
    var myChart;
    var option;
    var oilMax;
    var flogKey;//判断当前车辆是否绑定传感器
    // 车辆list
    var vehicleList = JSON.parse($("#vehicleList").attr("value"));
    var zTreeIdJson = {};
    var size;//当前权限监控对象数量
    var checkFlag = false;
    var url_oil = "/clbs/v/oilmassmgt/oilquantitystatistics/getOilPagInfo";
    var url_amount = "/clbs/v/oilmassmgt/oilquantitystatistics/getOilAmountPagiInfo";
    var url_spill = "/clbs/v/oilmassmgt/oilquantitystatistics/getOilSpillPagiInfo";
    var searchSate = true;

    var vehicleId = '';
    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于100自动勾选
    //用于ie浏览器判断选择节点树时是否需要调用搜索功能
    var isSearch = true;
    var legendSelected = {
        '主油箱': false,
        '副油箱': false,
        '环境温度1': false,
        '燃油温度2': false,
        '环境温度2': false
    };

    oilstatiscal = {
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
                    formatter: function (a) {
                        var unit = ['L', 'km', 'km/h', 'L', 'L', '°C', '°C', '°C', '°C', '空调', 'ACC', 'mm', 'mm'];
                        var relVal = "";
                        if (a[0]) {
                            relVal = a[0].name;
                            if (a[0].data == null) {
                                relVal = "无相关数据";
                            } else {
                                for (var i = 0; i < a.length; i++) {
                                    if (a[i].seriesName == "空调") {
                                        if (a[i].data === "") {
                                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：";
                                        } else if (a[i].data == 0) {
                                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：关闭";
                                        } else {
                                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：开启";
                                        }
                                    } else if (a[i].seriesName == "ACC") {
                                        if (a[i].data == 1) {
                                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：关闭";
                                        } else {
                                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：开启";
                                        }
                                    } else {
                                        if (a[i].data === "" || a[i].data == null) {
                                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：- " + unit[a[i].seriesIndex] + "";
                                        } else {
                                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + a[i].data + " " + unit[a[i].seriesIndex] + "";
                                        }
                                    }
                                }
                            }
                            return relVal;
                        }
                    }
                },
                grid: {
                    left: 190,
                    right: 200,
                },
                legend: {
                    data: [{name: '总油量'},
                        {name: '里程'},
                        {name: '速度'},
                        {name: '主油箱'},
                        {name: '副油箱'},
                        {name: '燃油温度1'},
                        {name: '环境温度1'},
                        {name: '燃油温度2'},
                        {name: '环境温度2'},
                        {name: '空调'},
                        {name: 'ACC'},
                        {name: '主油箱液位高度'},
                        {name: '副油箱液位高度'}],
                    left: 'auto',
                    selected: legendSelected,
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
                        name: '温度(°C)',
                        scale: true,
                        position: 'right',
                        offset: 60,
                        min: -30,
                        max: 100,
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine: {
                            show: false
                        }
                    },
                    {
                        type: 'value',
                        name: '高度(mm)',
                        scale: true,
                        position: 'right',
                        offset: 120,
                        min: 0,
                        max: 2000,
                        interval: 100,
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine: {
                            show: false
                        }
                    },
                    {
                        type: 'value',
                        name: '油量(L)',
                        scale: true,
                        position: 'left',
                        offset: 60,
                        min: 0,
                        max: oilMax > 350 ? oilMax : 350,
                        interval: 50,
                        axisLabel: {
                            /*formatter: '{value}'*/
                            formatter: function (value, index) {
                                return value.toFixed(0);
                            }
                        },
                        splitLine: {
                            show: false
                        },
                    },
                    {
                        type: 'value',
                        name: '里程(km)',
                        position: 'left',
                        scale: true,
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
                        position: 'left',
                        offset: 100,
                        axisLabel: {
                            formatter: '{value}',
                            formatter: function (value) {
                                if (value == 0) {
                                    return '开'
                                }
                                return '关'

                            },
                        },
                        splitLine: {
                            show: false
                        }
                    },
                    {
                        type: 'value',
                        name: '空调',
                        scale: true,
                        min: 0,
                        max: 1,
                        splitNumber: 1,
                        position: 'left',
                        offset: 140,
                        axisLabel: {
                            formatter: '{value}',
                            formatter: function (value) {
                                if (value == 0) {
                                    return '关'
                                }
                                return '开'

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
                        name: '总油量',
                        yAxisIndex: 3,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(248, 123, 0)'
                            }
                        },
                        label: {
                            normal: {
                                formatter: '{value}L'
                            }
                        },
                        data: oil
                    },
                    {
                        name: '里程',
                        yAxisIndex: 4,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(109, 207, 246)'
                            }
                        },
                        data: mileage
                    },
                    {
                        name: '速度',
                        yAxisIndex: 0,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(145, 218, 0)'
                            }
                        },
                        data: speed
                    },
                    {
                        name: '主油箱',
                        yAxisIndex: 3,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        symbolSize: 15,
                        showSymbol: false,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(240, 182, 125)'
                            }
                        },
                        data: oilOne
                    },
                    {
                        name: '副油箱',
                        yAxisIndex: 3,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        symbolSize: 15,
                        showSymbol: false,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(150, 78, 7)'
                            }
                        },
                        data: oilTwo
                    },
                    {
                        name: '燃油温度1',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(245, 0, 0)'
                            }
                        },
                        data: oilOneTemp
                    },
                    {
                        name: '环境温度1',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(244, 168, 177)'
                            }
                        },
                        data: ENVOneTemp
                    },
                    {
                        name: '燃油温度2',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(240, 89, 89)'
                            }
                        },
                        data: oilTwoTemp
                    },
                    {
                        name: '环境温度2',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(168, 4, 23)'
                            }
                        },
                        data: ENVTwoTemp
                    },
                    {
                        name: '空调',
                        yAxisIndex: 6,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(168, 228, 251)'
                            }
                        },
                        areaStyle: {
                            normal: {
                                color: 'rgba(168, 228, 251,0.9)'
                            }
                        },
                        data: envTemp,
                    },
                    {
                        name: 'ACC',
                        yAxisIndex: 6,
                        type: 'line',
                        smooth: true,
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
                        data: accTemp,
                    },
                    {
                        name: '主油箱液位高度',
                        yAxisIndex: 2,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(0, 0, 255)'
                            }
                        },
                        data: oilHeightOne,
                    },
                    {
                        name: '副油箱液位高度',
                        yAxisIndex: 2,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(128, 128, 255)'
                            }
                        },
                        data: oilHeightTwo,
                    }
                ]
            };
            myChart.setOption(option);
            myChart.on('legendselectchanged', function (obj) {// 监听legend点击事件
                var selected = obj.selected;
                legendSelected = selected;
            });

            window.onresize = myChart.resize;
        },
        treeInit: function () {
            var setting = {
                async: {
                    //url: "/clbs/m/functionconfig/fence/bindfence/vehicelTree",
                    url: oilstatiscal.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: oilstatiscal.ajaxDataFilter
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
                    beforeClick: oilstatiscal.beforeClickVehicle,
                    onCheck: oilstatiscal.onCheckVehicle,
                    beforeCheck: oilstatiscal.zTreeBeforeCheck,
                    onExpand: oilstatiscal.zTreeOnExpand,
                    //beforeAsync: oilstatiscal.zTreeBeforeAsync,
                    onAsyncSuccess: oilstatiscal.zTreeOnAsyncSuccess,
                    onNodeCreated: oilstatiscal.zTreeOnNodeCreated
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
                oilstatiscal.treeInit();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType').val(), "queryParam": param, 'queryType': 'multiple'},
                        dataFilter: oilstatiscal.ajaxQueryDataFilter
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
                        beforeClick: oilstatiscal.beforeClickVehicle,
                        onCheck: oilstatiscal.onCheckVehicle,
                        onExpand: oilstatiscal.zTreeOnExpand,
                        onNodeCreated: oilstatiscal.zTreeOnNodeCreated
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
                console.log(nodesArr, 'nodesArr');
                console.log(crrentSubV, 'crrentSubV');
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
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=monitor";
            }
        },
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
            oilstatiscal.getCharSelect(treeObj);
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

                    oilstatiscal.validates();
                }, 600);
            }
            if (treeNode.type != "assignment" || (treeNode.type == "assignment" && treeNode.children != undefined)) {
                oilstatiscal.getCharSelect(zTree);
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
            if (arrays.length > 0) {
                $("#charSelect").val(arrays[0].name).attr('data-id', arrays[0].id);
            }
            // 去掉数组中id相同的元素
            arrays = objArrRemoveRepeat(arrays);
            var deviceDataList = {value: arrays};
            $("#charSelect").empty();
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
            if (deviceDataList.length > 0) {
                $("#charSelect").val(deviceDataList[0].name).attr("data-id", deviceDataList[0].id);
            }
            $("#groupSelect,#groupSelectSpan").bind("click", oilstatiscal.showMenu);
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
            $("body").bind("mousedown", oilstatiscal.onBodyDown);
        },
        hideMenu: function () {
            $("#menuContent").fadeOut("fast");
            $("body").unbind("mousedown", oilstatiscal.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "menuContent" || $(event.target).parents("#menuContent").length > 0)) {
                oilstatiscal.hideMenu();
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
                tMonth = oilstatiscal.doHandleMonth(tMonth + 1);
                tDate = oilstatiscal.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * parseInt(num);
                today.setTime(end_milliseconds); // 注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = oilstatiscal.doHandleMonth(endMonth + 1);
                endDate = oilstatiscal.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " " + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = oilstatiscal.doHandleMonth(vMonth + 1);
                vDate = oilstatiscal.doHandleMonth(vDate);
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
                    vendMonth = oilstatiscal.doHandleMonth(vendMonth + 1);
                    vendDate = oilstatiscal.doHandleMonth(vendDate);
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
        getSensorMessage: function (band) {
            var flog;
            var url = "/clbs/v/oilmassmgt/oilquantitystatistics/getSensorMessage";
            var data = {"band": band};
            json_ajax("POST", url, "json", false, data, function (data) {
                flog = data;
            });
            return flog;
        },
        ajaxList: function (band, startTime, endTime) {
            oilstatiscal.removeClass();
            $("#allReport").addClass("active");
            // $("#carName").text($("input[name='charSelect']").val());

            // wjk
            $('#carName').text(oilstatiscal.platenumbersplitFun($("input[name='charSelect']").val()));
            //没有截取就不显示tooltip
            if ($("input[name='charSelect']").val() == oilstatiscal.platenumbersplitFun($("input[name='charSelect']").val())) {
                $('#carName').removeAttr('data-original-title')
            } else {
                $('#carName').attr('data-original-title', $("input[name='charSelect']").val());
            }

            $(".toopTip-btn-left,.toopTip-btn-right").css("display", "inline-block");
            oil = [];
            oilOne = [];
            oilTwo = [];
            mileage = [];
            speed = [];
            dataSets = [];
            amountDataSets = [];
            spillDataSets = [];
            date = [];
            tmp = [];
            tmp2 = [];
            etmp = [];
            oilOneTemp = [];
            oilTwoTemp = [];
            ENVOneTemp = [];
            ENVTwoTemp = [];
            envTemp = [];
            accTemp = [];
            oilHeightOne= [];
            oilHeightTwo= [];
            $.ajax({
                type: "POST",
                url: "/clbs/v/oilmassmgt/oilquantitystatistics/getOilInfo",
                data: {"band": band, "startTime": startTime, "endTime": endTime, "userName": $("#userName").text()},
                dataType: "json",
                async: true,
                timeout: 30000, //超时时间设置，单位毫秒
                beforeSend: function () {
                    //异步请求时spinner出现
                    layer.load(2);
                },
                success: function (data) {
                    searchSate = true;
                    layer.closeAll('loading');
                    if (data.success) {
                        var responseData = JSON.parse(ungzip(data.obj.oilInfo));
                        var signal = data.obj.signal;
                        var ioStatus = data.obj.ioStatus;
                        data.obj.oilInfo = responseData;
                        $('#timeInterval').val(startTime + '--' + endTime);
                        var staOil = null;//开始时间油箱油量
                        var endOil = null;//结束时间油箱油量
                        var Amount = 0;
                        var leak = 0;
                        if (data.obj.oilInfo.length != 0) {
                            $('#exportBtn').prop('disabled', false);
                            var nullData = 0;
                            var travelTime = 0;
                            var changeTime = 0;
                            var rtime = 0;
                            var chenageTimes = 0;
                            /*========================查询是否绑定外设轮询==============================================*/
                            flogKey = oilstatiscal.getSensorMessage(band);
                            /*======================================================================================*/

                            /** 过滤油量小于等于0.5的数据 **/
                            var oilTankOneFilterd = data.obj.oilInfo.filter(function (ele) {
                                return Number(ele.oilTankOne) > 0.5;
                            });
                            var oilTankTwoFilterd = data.obj.oilInfo.filter(function (ele) {
                                return Number(ele.oilTankTwo) > 0.5;
                            });
                            staOil = Number(oilTankOneFilterd.length > 0 ? oilTankOneFilterd[0].oilTankOne : 0)
                                + Number(oilTankTwoFilterd.length > 0 ? oilTankTwoFilterd[0].oilTankTwo : 0);
                            endOil = Number(oilTankOneFilterd.length > 0 ? oilTankOneFilterd[oilTankOneFilterd.length - 1].oilTankOne : 0)
                                + Number(oilTankTwoFilterd.length > 0 ? oilTankTwoFilterd[oilTankTwoFilterd.length - 1].oilTankTwo : 0);
                            for (var i = 0, len = data.obj.oilInfo.length - 1; i <= len; i++) {
                                // if (i == 0) {
                                //     staOil = Number(data.obj.oilInfo[i].oilTankOne) + Number(data.obj.oilInfo[i].oilTankTwo);
                                // }
                                if (i != len) { //如果不是最后一条数据
                                    var miles;
                                    var speeds;
                                    if (flogKey == "true") {
                                        miles = Number(data.obj.oilInfo[i].mileageTotal === undefined ? 0 : data.obj.oilInfo[i].mileageTotal);
                                        speeds = Number(data.obj.oilInfo[i].mileageSpeed === undefined ? 0 : data.obj.oilInfo[i].mileageSpeed);
                                    } else {
                                        miles = Number(data.obj.oilInfo[i].gpsMile === undefined ? 0 : data.obj.oilInfo[i].gpsMile);
                                        speeds = Number(data.obj.oilInfo[i].speed === undefined ? 0 : data.obj.oilInfo[i].speed);
                                    }
                                    if (isNaN(miles) || miles == 0) {
                                        miles = "";
                                    }
                                    if (speeds == null || miles == "") {
                                        speeds = "";
                                    }
                                    if (!(Number(data.obj.oilInfo[i].totalOilwearOne) == 0 && miles == 0 && speeds == 0 && Number(data.obj.oilInfo[i].oiltankTemperatureOne) == 0)) {
                                        if (data.obj.oilInfo[i].vtime != data.obj.oilInfo[i + 1].vtime) {//就与下一体数据的vtime字段比较,如果不相同,则进行数据组装
                                            date.push(oilstatiscal.timeStamp2String(oilstatiscal.UnixToDate(data.obj.oilInfo[i].vtime, true)));

                                            var oilTatal = (Number(data.obj.oilInfo[i].oilTankOne) + Number(data.obj.oilInfo[i].oilTankTwo)).toFixed(2);
                                            if (oilTatal >= 0.5) {
                                                oil.push(oilTatal);
                                            } else {
                                                oil.push("-");
                                            }
                                            if (Number(data.obj.oilInfo[i].oilTankOne) >= 5) {
                                                var oilTankOne = data.obj.oilInfo[i].oilTankOne;
                                                oilOne.push(parseFloat(oilTankOne).toFixed(2));
                                            } else {
                                                oilOne.push("-");
                                            }
                                            if (Number(data.obj.oilInfo[i].oilTankTwo) >= 5) {
                                                oilTwo.push(parseFloat(data.obj.oilInfo[i].oilTankTwo).toFixed(2));
                                            } else {
                                                oilTwo.push("-")
                                            }
                                            /* if(flogKey == "true"){

                                             }*/
                                            mileage.push(miles);
                                            speed.push(speeds);
                                            if ((Number(data.obj.oilInfo[i].fuelTemOne)).toFixed(2) < 80 && (Number(data.obj.oilInfo[i].fuelTemOne)).toFixed(2) > 0) {
                                                oilOneTemp.push(parseFloat((Number(data.obj.oilInfo[i].fuelTemOne)).toFixed(1)));
                                                tmp.push(Number(data.obj.oilInfo[i].fuelTemOne));
                                            } else {
                                                oilOneTemp.push("-");
                                                tmp.push(0);
                                            }
                                            if ((Number(data.obj.oilInfo[i].fuelTemTwo)).toFixed(2) < 80 && (Number(data.obj.oilInfo[i].fuelTemTwo)).toFixed(2) > 0) {
                                                oilTwoTemp.push(parseFloat((Number(data.obj.oilInfo[i].fuelTemTwo)).toFixed(1)));
                                            } else {
                                                oilTwoTemp.push("-")
                                            }
                                            if ((Number(data.obj.oilInfo[i].environmentTemOne)).toFixed(2) < 80 && (Number(data.obj.oilInfo[i].environmentTemOne)).toFixed(2) > 0) {
                                                ENVOneTemp.push(parseFloat((Number(data.obj.oilInfo[i].environmentTemOne)).toFixed(1)));
                                                etmp.push(Number(data.obj.oilInfo[i].environmentTemOne));
                                            } else {
                                                ENVOneTemp.push("-")
                                                etmp.push(0);
                                            }
                                            if ((Number(data.obj.oilInfo[i].environmentTemTwo)).toFixed(2) < 80 && (Number(data.obj.oilInfo[i].environmentTemTwo)).toFixed(2) > 0) {
                                                ENVTwoTemp.push(parseFloat((Number(data.obj.oilInfo[i].environmentTemTwo)).toFixed(1)));
                                            } else {
                                                ENVTwoTemp.push("-");
                                            }

                                            var airState = oilstatiscal.airConditionStatus(signal, data.obj.oilInfo[i]);

                                            if (ioStatus != "0" || airState == 2) {

                                                if (data.obj.oilInfo[i].airConditionStatus == null) {
                                                    envTemp.push("");
                                                } else {
                                                    envTemp.push(Number(data.obj.oilInfo[i].airConditionStatus));
                                                }
                                            } else {
                                                envTemp.push(airState);
                                            }
                                            if (data.obj.oilInfo[i].fuelAmountOne != null || data.obj.oilInfo[i].fuelAmountTwo != null) {
                                                Amount += (Number(data.obj.oilInfo[i].fuelAmountOne) + Number(data.obj.oilInfo[i].fuelAmountTwo))
                                            }
                                            if (data.obj.oilInfo[i].fuelSpillOne != null || data.obj.oilInfo[i].fuelSpillTwo != null) {
                                                leak += (Number(data.obj.oilInfo[i].fuelSpillOne) + Number(data.obj.oilInfo[i].fuelSpillTwo))
                                            }
                                            accTemp.push(Number(data.obj.oilInfo[i].acc));
                                            oilHeightOne.push(data.obj.oilInfo[i].oilHeightOne ? Number(data.obj.oilInfo[i].oilHeightOne) : null);
                                            oilHeightTwo.push(data.obj.oilInfo[i].oilHeightTwo ? Number(data.obj.oilInfo[i].oilHeightTwo) : null);
                                        }
                                    }
                                    if (oilstatiscal.GetDateDiff(oilstatiscal.UnixToDate(data.obj.oilInfo[i].vtime, true), oilstatiscal.UnixToDate(data.obj.oilInfo[i + 1].vtime, true), "second") <= 300
                                        && oilstatiscal.GetDateDiff(oilstatiscal.UnixToDate(data.obj.oilInfo[i].vtime, true), oilstatiscal.UnixToDate(data.obj.oilInfo[i + 1].vtime, true), "second") >= 5) {
                                        changeTime = oilstatiscal.GetDateDiff(oilstatiscal.UnixToDate(data.obj.oilInfo[i].vtime, true), oilstatiscal.UnixToDate(data.obj.oilInfo[i + 1].vtime, true), "second");
                                        switch (changeTime) {
                                            case 5:
                                                rtime = 5000;
                                                break;
                                            case 10:
                                                rtime = 10000;
                                                break;
                                            case 15:
                                                rtime = 15000;
                                                break;
                                            case 20:
                                                rtime = 20000;
                                                break;
                                            case 25:
                                                rtime = 25000;
                                                break;
                                            case 30:
                                                rtime = 30000;
                                                break;
                                            case 60:
                                                rtime = 60000;
                                                break;
                                            case 300:
                                                rtime = 300000;
                                                break;
                                            default:
                                                rtime = 0;
                                                break
                                        }
                                    }
                                    if (rtime == 5000 || rtime == 10000 || rtime == 15000 || rtime == 20000 || rtime == 25000 || rtime == 30000 || rtime == 60000 || rtime == 300000) {
                                        switch (rtime) {
                                            case 5000:
                                                chenageTimes = 12;
                                                break;
                                            case 10000:
                                                chenageTimes = 6;
                                                break;
                                            case 15000:
                                                chenageTimes = 4;
                                                break;
                                            case 20000:
                                                chenageTimes = 3;
                                                break;
                                            case 25000:
                                                chenageTimes = 2.4;
                                                break;
                                            case 30000:
                                                chenageTimes = 2;
                                                break;
                                            case 60000:
                                                chenageTimes = 1;
                                                break;
                                            case 300000:
                                                chenageTimes = 1 / 5;
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                    if (oilstatiscal.GetDateDiff(oilstatiscal.UnixToDate(data.obj.oilInfo[i].vtime, true), oilstatiscal.UnixToDate(data.obj.oilInfo[i + 1].vtime, true), "second") > 300) {
                                        nullData += oilstatiscal.GetDateDiff(oilstatiscal.UnixToDate(data.obj.oilInfo[i].vtime, true), oilstatiscal.UnixToDate(data.obj.oilInfo[i + 1].vtime, true), "hour");
                                        if (rtime == 0) {
                                            var ctime = +oilstatiscal.timeAdd(oilstatiscal.UnixToDate(data.obj.oilInfo[i].vtime, true));
                                            for (var n = 0; n < oilstatiscal.GetDateDiff(oilstatiscal.UnixToDate(data.obj.oilInfo[i].vtime, true), oilstatiscal.UnixToDate(data.obj.oilInfo[i + 1].vtime, true), "minute") * 2; n++) {
                                                date.push(oilstatiscal.timeStamp2String(new Date(ctime += 30000)));
                                                oil.push(null);
                                                oilOne.push(null);
                                                oilTwo.push(null);
                                                mileage.push(null);
                                                speed.push(null);
                                                oilOneTemp.push(null);
                                                oilTwoTemp.push(null);
                                                ENVOneTemp.push(null);
                                                ENVTwoTemp.push(null);
                                                envTemp.push(null);
                                                accTemp.push(null);
                                                oilHeightOne.push(null);
                                                oilHeightTwo.push(null);
                                            }
                                        } else {
                                            var ctime = +oilstatiscal.timeAdd(oilstatiscal.UnixToDate(data.obj.oilInfo[i].vtime, true));
                                            for (var n = 0; n < oilstatiscal.GetDateDiff(oilstatiscal.UnixToDate(data.obj.oilInfo[i].vtime, true), oilstatiscal.UnixToDate(data.obj.oilInfo[i + 1].vtime, true), "minute") * chenageTimes - 1; n++) {
                                                date.push(oilstatiscal.timeStamp2String(new Date(ctime += rtime)));
                                                oil.push(null);
                                                oilOne.push(null);
                                                oilTwo.push(null);
                                                mileage.push(null);
                                                speed.push(null);
                                                oilOneTemp.push(null);
                                                oilTwoTemp.push(null);
                                                ENVOneTemp.push(null);
                                                ENVTwoTemp.push(null);
                                                envTemp.push(null);
                                                accTemp.push(null);
                                                oilHeightOne.push(null);
                                                oilHeightTwo.push(null);
                                            }
                                        }
                                    }
                                } else {
                                    // endOil = Number(data.obj.oilInfo[len].oilTankOne) + Number(data.obj.oilInfo[len].oilTankTwo);
                                    date.push(oilstatiscal.timeStamp2String(oilstatiscal.UnixToDate(data.obj.oilInfo[len].vtime, true)));
                                    var oilTatal = (Number(data.obj.oilInfo[len].oilTankOne) + Number(data.obj.oilInfo[len].oilTankTwo)).toFixed(2);
                                    if (oilTatal >= 0.5) {
                                        oil.push(oilTatal);
                                    } else {
                                        oil.push("-");
                                    }
                                    if (Number(data.obj.oilInfo[len].oilTankOne) != 0) {
                                        oilOne.push(parseFloat(data.obj.oilInfo[len].oilTankOne).toFixed(2));
                                    } else {
                                        oilOne.push("-");
                                    }
                                    if (Number(data.obj.oilInfo[len].oilTankTwo) != 0) {
                                        oilTwo.push(parseFloat(data.obj.oilInfo[len].oilTankTwo).toFixed(2));
                                    } else {
                                        oilTwo.push("-")
                                    }
                                    var miles;
                                    var speeds;
                                    if (flogKey == "true") {
                                        miles = Number(data.obj.oilInfo[len].mileageTotal === undefined ? 0 : data.obj.oilInfo[len].mileageTotal)
                                        speeds = Number(data.obj.oilInfo[len].mileageSpeed === undefined ? 0 : data.obj.oilInfo[len].mileageSpeed);
                                    } else {
                                        miles = Number(data.obj.oilInfo[len].gpsMile);
                                        speeds = Number(data.obj.oilInfo[len].speed);
                                    }
                                    if (miles == NaN || miles == 0) {
                                        miles = "";
                                    }
                                    if (speeds == null || miles == NaN || miles == 0) {
                                        speeds = "";
                                    }
                                    mileage.push(miles);
                                    speed.push(speeds);
                                    if ((Number(data.obj.oilInfo[len].fuelTemOne)).toFixed(2) < 80 && (Number(data.obj.oilInfo[len].fuelTemOne)).toFixed(2) > 0) {
                                        oilOneTemp.push(parseFloat((Number(data.obj.oilInfo[len].fuelTemOne)).toFixed(2)));
                                        tmp.push(Number(data.obj.oilInfo[len].fuelTemOne));
                                    } else {
                                        oilOneTemp.push("-");
                                        tmp.push(0);
                                    }
                                    if ((Number(data.obj.oilInfo[i].fuelTemTwo)).toFixed(2) < 80 && (Number(data.obj.oilInfo[i].fuelTemTwo)).toFixed(2) > 0) {
                                        oilTwoTemp.push(parseFloat((Number(data.obj.oilInfo[i].fuelTemTwo)).toFixed(2)));
                                    } else {
                                        oilTwoTemp.push("-")
                                    }
                                    if ((Number(data.obj.oilInfo[len].environmentTemOne)).toFixed(2) < 80 && (Number(data.obj.oilInfo[len].environmentTemOne)).toFixed(2) > 0) {
                                        ENVOneTemp.push(parseFloat((Number(data.obj.oilInfo[len].environmentTemOne)).toFixed(2)));
                                        etmp.push(Number(data.obj.oilInfo[len].environmentTemOne));
                                    } else {
                                        ENVOneTemp.push("-")
                                        etmp.push(0);
                                    }
                                    if ((Number(data.obj.oilInfo[len].environmentTemTwo)).toFixed(2) < 80 && (Number(data.obj.oilInfo[len].environmentTemTwo)).toFixed(2) > 0) {
                                        ENVTwoTemp.push(parseFloat((Number(data.obj.oilInfo[len].environmentTemTwo)).toFixed(2)));
                                    } else {
                                        ENVTwoTemp.push("-");
                                    }
                                    var airState = oilstatiscal.airConditionStatus(signal, data.obj.oilInfo[len])
                                    if (ioStatus != "0" || airState == 2) {
                                        if (data.obj.oilInfo[len].airConditionStatus == null) {
                                            envTemp.push("");
                                        } else {
                                            envTemp.push(Number(data.obj.oilInfo[len].airConditionStatus));
                                        }
                                    } else {
                                        envTemp.push(Number(airState));
                                    }
                                    if (data.obj.oilInfo[len].fuelAmountOne != null || data.obj.oilInfo[len].fuelAmountTwo != null) {
                                        Amount += (Number(data.obj.oilInfo[len].fuelAmountOne) + Number(data.obj.oilInfo[len].fuelAmountTwo))
                                    }
                                    if (data.obj.oilInfo[len].fuelSpillOne != null || data.obj.oilInfo[len].fuelSpillTwo != null) {
                                        leak += (Number(data.obj.oilInfo[len].fuelSpillOne) + Number(data.obj.oilInfo[len].fuelSpillTwo))
                                    }
                                    accTemp.push(Number(data.obj.oilInfo[len].acc));
                                    oilHeightOne.push(data.obj.oilInfo[len].oilHeightOne ? Number(data.obj.oilInfo[len].oilHeightOne) : null);
                                    oilHeightTwo.push(data.obj.oilInfo[len].oilHeightTwo ? Number(data.obj.oilInfo[len].oilHeightTwo) : null);
                                }
                            }
                            oilMax = parseInt(Math.max.apply(null, oil)) + 20;
                            if (data.obj.infoDtails.infoDtail.length != 0 && data.obj.infoDtails.infoDtail.length != null) {
                                for (var j = data.obj.infoDtails.infoDtail.length - 1, lens = 0; j >= lens; j--) {
                                    travelTime += (Number(data.obj.infoDtails.infoDtail[j].steerTime) / 60 / 60)
                                }
                            }
                            var str = '';
                            var startFuel = Number(data.obj.oilInfo[0].totalOilwearOne);
                            var endFuel = Number(data.obj.oilInfo[data.obj.oilInfo.length - 1].totalOilwearOne);
                            var Mile = oilstatiscal.drivingMile(data.obj.oilInfo);
                            var mileageV = parseFloat(Mile.toFixed(1));
                            $("#mileage").text((mileageV ? mileageV : 0) + "km");
                            // 油耗
                            var Fuel = endFuel - startFuel;
                            // 温度差
                            var maxTemp = oilstatiscal.filterTheNull(tmp);
                            // 温度差2
                            var maxTemp2 = oilstatiscal.filterTheNull(oilTwoTemp);
                            var temp11, temp22;
                            if (oilstatiscal.arrayIsNull(maxTemp)) {
                                temp11 = Math.max.apply(null, maxTemp) - Math.min.apply(null, maxTemp);
                            } else {
                                temp11 = 0;
                            }
                            if (oilstatiscal.arrayIsNull(maxTemp2)) {
                                temp22 = Math.max.apply(null, maxTemp2) - Math.min.apply(null, maxTemp2);
                            } else {
                                temp22 = 0;
                            }
                            temp11 = temp11 != 0 ? parseFloat(temp11.toFixed(2)) : 0;
                            temp22 = !isNaN(temp22) && temp22 != 0 ? parseFloat(temp22.toFixed(2)) : 0;
                            var temp111;
                            if (parseFloat(temp11) > parseFloat(temp22)) {
                                temp111 = temp11;
                            } else {
                                temp111 = temp22;
                            }
                            var diffTemperature = Math.max.apply(null, maxTemp) - Math.min.apply(null, maxTemp);
                            var diffTemperature2 = Math.max.apply(null, maxTemp2) - Math.min.apply(null, maxTemp2);
                            diffTemperature = maxTemp.length != 0 ? parseFloat(diffTemperature.toFixed(2)) : 0;
                            //环境温差
                            var maxETemp = oilstatiscal.filterTheNull(etmp);
                            var diffETemperature = Math.max.apply(null, maxETemp) - Math.min.apply(null, maxETemp);
                            diffETemperature = maxETemp.length != 0 ? parseFloat(diffETemperature.toFixed(2)) : 0;
                            // 百公里油耗
                            $("#travelTime").text(oilstatiscal.toHHMMSS(Number(travelTime)));
                            if (Number(data.obj.infoDtails.totalT) - Number(travelTime) - nullData > 0) {
                                $("#idleTime").text(oilstatiscal.toHHMMSS((Number(data.obj.infoDtails.totalT) - Number(travelTime)) - nullData));
                            } else if (Number(data.obj.infoDtails.totalT) - Number(travelTime) > 0 && Number(data.obj.infoDtails.totalT) - Number(travelTime) - nullData < 0) {
                                $("#idleTime").text(oilstatiscal.toHHMMSS((Number(data.obj.infoDtails.totalT) - Number(travelTime))));
                            } else {
                                $("#idleTime").text("0");
                            }
                            $("#fuel").text(parseFloat((Amount).toFixed(2)) + "L");
                            var oilAll = parseFloat(((staOil - endOil + Amount - leak)).toFixed(2));
                            $("#userFuel").text(oilAll > 0 ? oilAll + 'L' : '-');
                            $("#userFuel100").text((oilAll > 0 && mileageV > 0) ? parseFloat((oilAll / mileageV) * 100).toFixed(2).replace('.00', '') + 'L/100km' : '-');
                            //$("#userFuel").text(Math.max(parseFloat(((staOil - endOil + Amount - leak) / 10).toFixed(2)), 0) + "L");

                            $("#hundredFuel").text(parseFloat((leak).toFixed(2)) + "L");
                            $("#averageVelocity").text(diffETemperature + "℃");
                            $("#difference").text((temp111 ? temp111 : 0) + "℃");
                            $("#airTime").text(oilstatiscal.toHHMMSS(Number(data.obj.infoDtails.totalAirTime)));
                            $("#detail").empty();
                            $("#detail").append(str);
                            oilstatiscal.draggle();
                        } else {
                            $('#exportBtn').prop('disabled', true);
                            $("#showClick").attr("class", "fa fa-chevron-up");
                            $("#graphShow").hide();
                            $("#travelTime").text("0");
                            $("#idleTime").text("0");
                            $("#mileage").text("0km");
                            $("#fuel").text("0L");
                            $("#userFuel100").text('-');
                            $("#hundredFuel").text("0L");
                            var oilAll = parseFloat(((staOil - endOil + Amount - leak)).toFixed(2));
                            $("#userFuel").text(oilAll > 0 ? oilAll + 'L' : '-');
                            //$("#userFuel").text(Math.max(parseFloat(((staOil - endOil + Amount - leak) / 10).toFixed(2)), 0) + "L");
                            $("#averageVelocity").text("0℃");
                            $("#difference").text("0℃");
                            $("#airTime").text("0");
                            $("#oilTable_wrapper").children("div.row").hide();
                        }
                        if (data.success == false) {
                            layer.msg(publicError);
                            oil = [];
                            oilOne = [];
                            oilTwo = [];
                            mileage = [];
                            speed = [];
                            dataSets = [];
                            date = [];
                            tmp = [];
                            oilOneTemp = [];
                            oilTwoTemp = [];
                            ENVOneTemp = [];
                            ENVTwoTemp = [];
                            envTemp = [];
                            accTemp = [];
                            oilHeightOne = [];
                            oilHeightTwo = [];
                        }
                        ;
                        $("#graphShow").show();
                        $("#showClick").attr("class", "fa fa-chevron-down");
                        searchState = true;
                        oilstatiscal.init();
                        if (oilstatiscal.validates()) {
                            $("#oilTable tbody").html("");
                            if (date.length != 0) {
                                if (flogKey == "true") {
                                    oilstatiscal.infoinputTab(url_oil, "mileageSpeed", "mileageTotal");
                                    //oilstatiscal.infoinputTab(url_oil,"speed","gpsMile");
                                } else {
                                    oilstatiscal.infoinputTab(url_oil, "speed", "gpsMile");
                                }
                            }
                        }
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    layer.closeAll('loading');
                    if (textStatus == "timeout") {
                        layer.msg(systemLoadingTimout);
                    }
                },
            });
        },
        drivingMile: function (data) {
            var startMile;
            var endMile;
            var length = data.length;
            if (flogKey == "true") {
                startMile = Number(data[0].mileageTotal === undefined ? 0 : data[0].mileageTotal);
                for (var i = length - 1; i >= 0; i--) {
                    var mileageTotal = Number(data[i].mileageTotal === undefined ? 0 : data[i].mileageTotal);
                    if (mileageTotal != 0) {
                        endMile = mileageTotal;
                        break;
                    }
                }
            } else {
                startMile = Number(data[0].gpsMile);
                for (var i = length - 1; i >= 0; i--) {
                    var gpsMile = Number(data[i].gpsMile);
                    if (gpsMile != 0) {
                        endMile = gpsMile;
                        break;
                    }
                }
            }
            // 里程
            return endMile - startMile;
        },
        fiterNumber: function (data) {
            if (data == null || data == undefined || data == "") {
                return data;
            }
            var data = data.toString();
            data = parseFloat(data);
            return data;

        },
        airConditionStatus: function (io, data) {
            // 此方法，IO[0]对应空调状态IO口，IO[1],对应 1 常开或是 2 常关，0也是常关，
            // return 返回值，0（关闭）/ 1（开启） / 2（未设置传感器）,
            if (io[1] == 0) {
                return 2;
            }
            var airStatus = 0;
            if (io[0] == 1) {
                if (data.ioOne != undefined) {
                    airStatus = data.ioOne;
                } else {
                    return "";
                }
            } else if (io[0] == 2) {
                if (data.ioOne != undefined) {
                    airStatus = data.ioTwo;
                } else {
                    return "";
                }
            } else if (io[0] == 3) {
                if (data.ioThree != undefined) {
                    airStatus = data.ioThree;
                } else {
                    return "";
                }
            } else if (io[0] == 4) {
                if (data.ioFour != undefined) {
                    airStatus = data.ioFour;
                } else {
                    return "";
                }
            } else {
                return 2;
            }
            if (io[1] == 1) {
                if (airStatus == 0) {
                    airStatus = 1;
                } else {
                    airStatus = 0;
                }
            }
            return airStatus;
        },
        //上一天(此段js可以删除，页面没有这个按钮)
        upDay: function () {
            oilstatiscal.startDay(1);
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (charNum != "" && groupValue != "") {
                var dateValue = new Date().getTime();
                var startTimeValue = new Date(startTime.split(" ")[0].replace(/-/g, "/")).getTime();
                if (startTimeValue <= dateValue) {
                    oilstatiscal.ajaxList(charNum, startTime, endTime);
                    oilstatiscal.validates()
                } else {
                    layer.msg("暂时没办法穿越，明天我再帮您看吧！");
                }
            } else {
                layer.msg("请选择监控对象！", {move: false});
            }
        },
        // 今天
        todayClick: function () {
            oilstatiscal.nowDay();
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (!oilstatiscal.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            }
            $("#charSelect-error").hide();

            oilstatiscal.ajaxList(charNum, startTime, endTime);
        },
        // 前一天
        yesterdayClick: function () {
            oilstatiscal.startDay(-1);
            var startValue = $("#startTime")
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (!oilstatiscal.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            }
            $("#charSelect-error").hide();

            oilstatiscal.ajaxList(charNum, startTime, endTime);
        },
        // 近三天
        nearlyThreeDays: function () {
            oilstatiscal.startDay(-3);
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (!oilstatiscal.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            }
            $("#charSelect-error").hide();

            oilstatiscal.ajaxList(charNum, startTime, endTime);
        },
        // 近七天
        nearlySevenDays: function () {
            oilstatiscal.startDay(-7);
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (!oilstatiscal.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            }
            $("#charSelect-error").hide();

            oilstatiscal.ajaxList(charNum, startTime, endTime);
        },
        // 查询
        inquireClick: function () {
            var groupValue = $("#groupSelect").val();
            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];
            var charNum = $("#charSelect").attr("data-id");
            if (!oilstatiscal.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            }
            $("#charSelect-error").hide();
            vehicleId = charNum;
            oilstatiscal.ajaxList(charNum, startTime, endTime);
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
        infoinputTab: function (url, speed, mail) {
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
                "data": "vtime",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return oilstatiscal.timeStamp2String(oilstatiscal.UnixToDate(data, true));
                }
            }, {
                "data": "acc",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "1") {
                        return "开";
                    }
                    return "关";

                }
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return parseFloat((Number(data.oilTankOne) + Number(data.oilTankTwo)).toFixed(2))
                }
            }, {
                "data": "oilTankOne",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return parseFloat((Number(data)).toFixed(2))
                }
            }, {
                "data": "oilTankTwo",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return parseFloat((Number(data)).toFixed(2))
                }
            }, {
                "data": "oilHeightOne", // 主油箱液位高度
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (!row.oilHeightOne) {
                        return '-';
                    }
                    return parseFloat((Number(data)).toFixed(2))
                }
            },  {
                "data": "oilHeightTwo",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (!row.oilHeightTwo) {
                        return '-';
                    }
                    return parseFloat((Number(data)).toFixed(2))
                }
            },  {
                "data": "fuelAmountOne",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return parseFloat((Number(data)).toFixed(2))
                }
            },  {
                "data": "fuelAmountTwo",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return parseFloat((Number(data)).toFixed(2))
                }
            },  {
                "data": "fuelSpillOne",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return parseFloat((Number(data)).toFixed(2))
                }
            },  {
                "data": "fuelSpillTwo",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return parseFloat((Number(data)).toFixed(2))
                }
            },  {
                "data": "fuelTemOne",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return (Number(data)) < 80 && (Number(data)) >= 0 ? parseFloat((Number(data)).toFixed(1)) : "-"
                }
            },  {
                "data": "environmentTemOne",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return (Number(data)) < 80 && (Number(data)) >= 0 ? parseFloat((Number(data)).toFixed(1)) : "-"
                }
            },  {
                "data": "fuelTemTwo",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return (Number(data)) < 80 && (Number(data)) >= 0 ? parseFloat((Number(data)).toFixed(1)) : "-"
                }
            },  {
                "data": "environmentTemTwo",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return (Number(data)) < 80 && (Number(data)) >= 0 ? parseFloat((Number(data)).toFixed(1)) : "-"
                }
            },  {
                "data": mail + "",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return data == '' ? '' : parseFloat(data)
                }
            },  {
                "data": speed + "",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return data == '' ? '' : parseFloat(data)
                }
            },  {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    //return data.longtitude+","+data.latitude
                    return "加载中..."
                }
            }
            ];
            //表格setting
            var setting = {
                listUrl: url,
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'oilTable', //表格
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                getAddress: true,//是否逆地理编码
                address_index: 20
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
        removeClass: function () {
            var dataList = $(".dataTableShow");
            for (var i = 0; i < 3; i++) {
                dataList.children("li").removeClass("active");
            }
        },
        allReportClick: function () {
            oilstatiscal.removeClass();
            $(this).addClass("active");
            if (date.length != 0) {
                if (flogKey == "true") {
                    oilstatiscal.infoinputTab(url_oil, "mileageSpeed", "mileageTotal");
                } else {
                    oilstatiscal.infoinputTab(url_oil, "speed", "gpsMile");
                }
            }
            if (option) {
                delete option.series[0].markPoint;
                myChart.clear();
                myChart.setOption(option);
            }

        },
        amountReportClick: function () {
            oilstatiscal.removeClass();
            $(this).addClass("active");
            if (date.length != 0) {
                if (flogKey == "true") {
                    oilstatiscal.infoinputTab(url_amount, "mileageSpeed", "mileageTotal");
                } else {
                    oilstatiscal.infoinputTab(url_amount, "speed", "gpsMile");
                }
            }
            if (option) {
                delete option.series[0].markPoint;
                myChart.clear();
                option.legend.selected = legendSelected;
                myChart.setOption(option);
            }

            //点击显示标注
            $('#oilTable tbody').on('click', 'tr', function () {
                $(this).siblings(".odd").find("td").css('background-color', "#f9f9f9");
                $(this).siblings(".even").find("td").css('background-color', "#fff");
                var backgroundcolor = $(this).find("td").css('background-color');
                if (backgroundcolor == "rgb(220, 245, 255)") {
                    $("#dataTable tbody tr").siblings(".odd").find("td").css('background-color', "#f9f9f9");
                    $("#dataTable tbody tr").siblings(".even").find("td").css('background-color', "#fff");
                    /*    delete  option.series[0].markPoint;
                        myChart.clear();*/
                } else {
                    $(this).find("td").css('background-color', "#DCF5FF");
                    var datainfo = $(this).find("td").eq("2").text();
                    var addoil = $(this).find("td").eq("7").text(); //加油量
                    var alloil = $(this).find("td").eq("4").text(); //总油量
                    var val = "总："+alloil+ '\n' + '加：'+addoil;
                    option.series[0].markPoint = {
                        symbolSize: [90, 93],
                        silent: true,
                        data: [
                            {
                                yAxis: alloil,
                                xAxis: datainfo,
                                label: {
                                    normal: {
                                        show: true,
                                        formatter: val
                                    }
                                }},
                        ]
                    };
                }
                myChart.setOption(option);
            });
        },
        spillReportClick: function () {
            oilstatiscal.removeClass();
            $(this).addClass("active");
            if (date.length != 0) {
                if (flogKey == "true") {
                    oilstatiscal.infoinputTab(url_spill, "mileageSpeed", "mileageTotal");
                } else {
                    oilstatiscal.infoinputTab(url_spill, "speed", "gpsMile");
                }
            }
            if (option) {
                delete option.series[0].markPoint;
                myChart.clear();
                myChart.setOption(option);
            }

            //点击显示标注
            $('#oilTable tbody').on('click', 'tr', function () {
                $(this).siblings(".odd").find("td").css('background-color', "#f9f9f9");
                $(this).siblings(".even").find("td").css('background-color', "#fff");
                var backgroundcolor = $(this).find("td").css('background-color');
                if (backgroundcolor == "rgb(220, 245, 255)") {
                    $("#dataTable tbody tr").siblings(".odd").find("td").css('background-color', "#f9f9f9");
                    $("#dataTable tbody tr").siblings(".even").find("td").css('background-color', "#fff");
                    /* delete  option.series[0].markPoint;
                     myChart.clear();*/
                } else {
                    $(this).find("td").css('background-color', "#DCF5FF");
                    var datainfo = $(this).find("td").eq("2").text();
                    var loseoil = $(this).find("td").eq("9").text();
                    var alloil = $(this).find("td").eq("4").text();
                    option.series[0].markPoint = {
                        symbolSize: [60, 63],
                        data: [
                            {name: '', value: loseoil, xAxis: datainfo, yAxis: alloil}
                        ]
                    };
                }
                myChart.setOption(option);
            });
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

        /*validates: function () {
            return $("#oilist").validate({
                rules: {
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
                    groupId: {
                        required: "不能为空"
                    },
                    endTime: {
                        required: "不能为空",
                        compareDate: "结束日期必须大于开始日期!",
                        compareDateDiff: "查询的日期必须小于一周"
                    },
                    startTime: {
                        required: "不能为空",
                    },
                    charSelect: {
                        required: "不能为空"
                    }
                }
            }).form();
        },*/
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
        // wjk 车牌号太长显示不完截取
        platenumbersplitFun: function (str) {
            if (str.length > 56) {
                str = str.substring(0, 55) + '...'
            }
            return str
        },
        //列表数据导出
        exportFun: function () {
            var index = $('.nav-tabs li.active').attr('data-index');
            if ($('.table:visible tbody tr td').hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if(getRecordsNum('oilTable_info') > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var url = "/clbs/v/oilmassmgt/oilquantitystatistics/exportDataList?type=" + index + '&vehicleId=' + vehicleId;
            window.location.href = url
        },
    };
    $(function () {
        oilstatiscal.treeInit();
        $("#toggle-left").bind("click", function () {
            setTimeout(function () {
                oilstatiscal.init();
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
        oilstatiscal.nowDay();
        $('#timeInterval').dateRangePicker({
            dateLimit: 7
        });
        $("#todayClick").bind("click", oilstatiscal.todayClick);
        $("#yesterdayClick,#right-arrow").bind("click", oilstatiscal.yesterdayClick);
        $("#nearlyThreeDays").bind("click", oilstatiscal.nearlyThreeDays);
        $("#nearlySevenDays").bind("click", oilstatiscal.nearlySevenDays);
        $("#inquireClick").bind("click", oilstatiscal.inquireClick);
        $("#showClick").bind("click", oilstatiscal.showClick);
        $("#left-arrow").bind("click", oilstatiscal.upDay);
        $('#allReport').bind("click", oilstatiscal.allReportClick);
        $('#amountReport').bind("click", oilstatiscal.amountReportClick);
        $('#spillReport').bind("click", oilstatiscal.spillReportClick);
        $("#endTime").bind("click", oilstatiscal.endTimeClick);
        $("#groupSelectSpan,#groupSelect").bind("click", oilstatiscal.showMenu); //组织下拉显示

        // 导出
        $("#exportBtn").bind("click", oilstatiscal.exportFun);

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                oilstatiscal.searchVehicleTree(param);
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
                isSearch = true;
            }
            $("#charSelect").val("").attr("data-id", "").bsSuggest("destroy");
            inputChange = setTimeout(function () {
                if (isSearch) {
                    var param = $("#groupSelect").val();
                    oilstatiscal.searchVehicleTree(param);
                }
                isSearch = true;
            }, 500);
        });
        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            oilstatiscal.searchVehicleTree(param);
        });
    });
}($, window));
