(function ($, window) {
    var endTime;
    var date = [];
    var startTime;
    var table;
    var oil = [];
    var dataSet = [];//table数据
    var addressMsg = [];
    var startLoc = [];
    var endLoc = [];
    var mileage = [];
    var speed = [];
    var tmp = [];
    var fuelTemp = [];
    var envTemp = [];
    var chart;
    var myChart;
    var option;
    var firstOilData;
    var firstMileageData;
    // 车辆list
    var vehicleList = JSON.parse($("#vehicleList").attr("value"));
    var zTreeIdJson = {};
    var size;//当前权限监控对象数量
    var checkFlag = false;

    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于100自动勾选
    //用于ie浏览器判断选择节点树时是否需要调用搜索功能
    var isSearch = true;
    var curBrand;
    var getAddressStatus = false;
    var startIndex = null;

    fuelConsumptionStatistics = {
        init: function () {
            myChart = echarts.init(document.getElementById('sjcontainer'));
            option = {
                tooltip: {
                    trigger: 'axis',
                    textStyle: {
                        fontSize: 20
                    },
                    formatter: function (a) {
                        var goString = JSON.stringify(a);
                        var unit = ['L', 'km', 'km/h', '°C', '空调'];
                        var relVal = "";
                        var addRelVal = "";
                        var relValTime = a[0].name;
                        if (a[0].data == null) {
                            relVal = "无相关数据";
                        } else {
                            if (goString.indexOf("总油耗") != -1 && goString.indexOf("总里程") != -1) {
                                addRelVal = "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[0].color + "'></span>油耗：" + parseFloat((a[0].data - firstOilData).toFixed(2)) + " " + unit[a[0].seriesIndex] + "";
                                var forsmile;
                                if (a[1].data - firstMileageData > 0) {
                                    forsmile = a[1].data - firstMileageData;
                                } else {
                                    forsmile = 0;
                                }
                                addRelVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[1].color + "'></span>里程：" + parseFloat((forsmile).toFixed(2)) + " " + unit[a[1].seriesIndex] + ""
                            } else if (goString.indexOf("总油耗") != -1 && goString.indexOf("总里程") == -1) {
                                addRelVal = "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[0].color + "'></span>油耗：" + parseFloat((a[0].data - firstOilData).toFixed(2)) + " " + unit[a[0].seriesIndex] + "";
                            } else if (goString.indexOf("总油耗") == -1 && goString.indexOf("总里程") != -1) {
                                var forsmile;
                                if (a[0].data - firstMileageData > 0) {
                                    forsmile = a[0].data - firstMileageData;
                                } else {
                                    forsmile = 0;
                                }
                                addRelVal = "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[0].color + "'></span>里程：" + parseFloat((forsmile).toFixed(2)) + " " + unit[a[0].seriesIndex] + "";
                            }
                            for (var i = 0; i < a.length; i++) {
                                if (a[i].seriesName != "空调") {
                                    relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + a[i].data + " " + unit[a[i].seriesIndex] + "";
                                } else {
                                    if (a[i].data === "") {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：";
                                    } else if (a[i].data == 0) {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：关闭";
                                    } else {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：开启";
                                    }
                                }
                            }
                        }
                        var allVal = relValTime + addRelVal + relVal;
                        return allVal;
                    }
                },
                grid: {
                    left: 160,
                    right: 190,
                },
                legend: {
                    selected: {
                        '总里程': true
                    },
                    data: ['总油耗', '总里程', '速度', '温度', '空调'],
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
                        name: '空调',
                        scale: true,
                        min: 0,
                        max: 1,
                        splitNumber: 1,
                        position: 'right',
                        offset: 100,
                        axisLabel: {
                            formatter: '{value}',
                            formatter: function (value) {
                                if (value == 0) {
                                    return '关'
                                } else {
                                    return '开'
                                }
                            },
                        },
                        splitLine: {
                            show: false
                        }
                    },
                    {
                        type: 'value',
                        name: '总油耗(L)',
                        scale: true,
                        position: 'left',
                        offset: 60,
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine: {
                            show: false
                        }
                    },
                    {
                        type: 'value',
                        name: '总里程(km)',
                        position: 'left',
                        scale: true,
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
                        name: '总油耗',
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
                        data: oil
                    },
                    {
                        name: '总里程',
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
                        name: '温度',
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
                        data: fuelTemp
                    },
                    {
                        name: '空调',
                        yAxisIndex: 2,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
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
                        data: envTemp,
                    }
                ]
            };
            myChart.setOption(option);
            window.onresize = myChart.resize;
        },
        treeInit: function () {
            var setting = {
                async: {
                    //url: "/clbs/m/functionconfig/fence/bindfence/vehicelTree",
                    url: fuelConsumptionStatistics.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple"},
                    otherParam: {"icoType": "0"},
                    dataFilter: fuelConsumptionStatistics.ajaxDataFilter
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
                    beforeClick: fuelConsumptionStatistics.beforeClickVehicle,
                    onCheck: fuelConsumptionStatistics.onCheckVehicle,
                    beforeCheck: fuelConsumptionStatistics.zTreeBeforeCheck,
                    onExpand: fuelConsumptionStatistics.zTreeOnExpand,
                    //beforeAsync: fuelConsumptionStatistics.zTreeBeforeAsync,
                    onAsyncSuccess: fuelConsumptionStatistics.zTreeOnAsyncSuccess,
                    onNodeCreated: fuelConsumptionStatistics.zTreeOnNodeCreated
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
                fuelConsumptionStatistics.treeInit();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType').val(), "queryParam": param, "queryType": "multiple"},
                        dataFilter: fuelConsumptionStatistics.ajaxQueryDataFilter
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
                        beforeClick: fuelConsumptionStatistics.beforeClickVehicle,
                        onCheck: fuelConsumptionStatistics.onCheckVehicle,
                        onExpand: fuelConsumptionStatistics.zTreeOnExpand,
                        onNodeCreated: fuelConsumptionStatistics.zTreeOnNodeCreated
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
            fuelConsumptionStatistics.getCharSelect(treeObj);
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
                setTimeout(() => {
                    fuelConsumptionStatistics.validates();
                }, 600);
            }
            if (treeNode.type != "assignment" || (treeNode.type == "assignment" && treeNode.children != undefined)) {
                fuelConsumptionStatistics.getCharSelect(zTree);
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
                if (nodes[i].type == "people" || nodes[i].type == "vehicle" || nodes[i].type == "thing") {
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
            var deviceDataList = {value: []};
            for (var index = 0; index < arrays.length; index++) {
                var deviceObj = arrays[index];
                deviceDataList.value.push(deviceObj);
            }
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
            $("#groupSelect,#groupSelectSpan").bind("click", fuelConsumptionStatistics.showMenu);
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
                tMonth = fuelConsumptionStatistics.doHandleMonth(tMonth + 1);
                tDate = fuelConsumptionStatistics.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * parseInt(num);
                today.setTime(end_milliseconds); // 注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = fuelConsumptionStatistics.doHandleMonth(endMonth + 1);
                endDate = fuelConsumptionStatistics.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " " + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = fuelConsumptionStatistics.doHandleMonth(vMonth + 1);
                vDate = fuelConsumptionStatistics.doHandleMonth(vDate);
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
                    vendMonth = fuelConsumptionStatistics.doHandleMonth(vendMonth + 1);
                    vendDate = fuelConsumptionStatistics.doHandleMonth(vendDate);
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
        //判断是否绑定传感器（"true"为绑定,""为非绑定）
        getSensorMessage: function (band) {
            var flog;
            var url = "/clbs/v/oilmassmgt/oilquantitystatistics/getSensorMessage";
            var data = {"band": band};
            json_ajax("POST", url, "json", false, data, function (data) {
                flog = data;
            });
            return flog;
        },
        // ajax请求数据
        ajaxList: function (band, startTime, endTime) {
            curBrand = band;
            var brandName = $("input[name='charSelect']").val();
            if (brandName.length > 8) {
                $("#carName").attr("title", brandName).tooltip('fixTitle');
                brandName = brandName.substring(0, 7) + '...';
            } else {
                $('#carName').removeAttr('data-original-title');
            }
            $("#carName").text(brandName);
            $(".toopTip-btn-left,.toopTip-btn-right").css("display", "inline-block");
            oil = [];
            mileage = [];
            speed = [];
            dataSet = [];
            date = [];
            tmp = [];
            fuelTemp = [];
            envTemp = [];
            var url = "/clbs/v/oilmgt/getOilInfo";
            var parameter = {"band": band, "startTime": startTime, "endTime": endTime};
            json_ajax("POST", url, "json", true, parameter, fuelConsumptionStatistics.getCallback);
        },
        getCallback: function (data) {
            getAddressStatus = false;
            var responseData = JSON.parse(ungzip(data.obj.oilInfo));
            data.obj.oilInfo = responseData;
            $('#timeInterval').val(startTime + '--' + endTime);
            $('#exportBtn').prop('disabled', true);
            if (data.obj.oilInfo.length != 0) {
                $('#exportBtn').prop('disabled', false);
                var flogKey;
                var charNum = $("#charSelect").attr("data-id");
                flogKey = fuelConsumptionStatistics.getSensorMessage(charNum);
                var nullData = 0;
                var travelTime = 0;
                var changeTime = 0;
                var rtime = 0;
                var chenageTimes = 0;
                firstOilData = Number(data.obj.oilInfo[0].totalOilwearOne);
                if (flogKey == "true") {
                    firstMileageData = Number(data.obj.oilInfo[0].mileageTotal === undefined ? 0 : data.obj.oilInfo[0].mileageTotal);
                } else {
                    firstMileageData = Number(data.obj.oilInfo[0].gpsMile);
                }

                for (var i = 0, len = data.obj.oilInfo.length; i < len; i++) {
                    var miles;
                    var speeds;
                    if (flogKey == "true") {
                        miles = Number(data.obj.oilInfo[i].mileageTotal === undefined ? 0 : data.obj.oilInfo[i].mileageTotal);
                        speeds = Number(data.obj.oilInfo[i].mileageSpeed === undefined ? 0 : data.obj.oilInfo[i].mileageSpeed);
                    } else {
                        miles = Number(data.obj.oilInfo[i].gpsMile);
                        speeds = Number(data.obj.oilInfo[i].speed);
                    }
                    if (miles == NaN || miles == 0) {
                        miles = "";
                    }
                    if (speeds == null || miles == NaN || miles == 0) {
                        speeds = "";
                    }
                    if (!(Number(data.obj.oilInfo[i].totalOilwearOne) == 0 && miles == 0 && speeds == 0 && Number(data.obj.oilInfo[i].oiltankTemperatureOne) == 0)) {
                        date.push(fuelConsumptionStatistics.timeStamp2String(fuelConsumptionStatistics.UnixToDate(data.obj.oilInfo[i].vtime, true)));
                        oil.push(Number(data.obj.oilInfo[i].totalOilwearOne));
                        mileage.push(miles);
                        speed.push(speeds);
                        fuelTemp.push(parseFloat((Number(data.obj.oilInfo[i].oiltankTemperatureOne)).toFixed(1)));
                        if (data.obj.oilInfo[i].airConditionStatus == null) {
                            envTemp.push("");
                        } else {
                            envTemp.push(Number(data.obj.oilInfo[i].airConditionStatus));
                        }
                        tmp.push(parseFloat((Number(data.obj.oilInfo[i].oiltankTemperatureOne)).toFixed(1)));
                    }
                    if (i !== data.obj.oilInfo.length - 1) {
                        if (fuelConsumptionStatistics.GetDateDiff(fuelConsumptionStatistics.UnixToDate(data.obj.oilInfo[i].vtime, true), fuelConsumptionStatistics.UnixToDate(data.obj.oilInfo[i + 1].vtime, true), "second") <= 300
                            && fuelConsumptionStatistics.GetDateDiff(fuelConsumptionStatistics.UnixToDate(data.obj.oilInfo[i].vtime, true), fuelConsumptionStatistics.UnixToDate(data.obj.oilInfo[i + 1].vtime, true), "second") >= 5) {
                            changeTime = fuelConsumptionStatistics.GetDateDiff(fuelConsumptionStatistics.UnixToDate(data.obj.oilInfo[i].vtime, true), fuelConsumptionStatistics.UnixToDate(data.obj.oilInfo[i + 1].vtime, true), "second");
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
                            }
                        }
                        if (fuelConsumptionStatistics.GetDateDiff(fuelConsumptionStatistics.UnixToDate(data.obj.oilInfo[i].vtime, true), fuelConsumptionStatistics.UnixToDate(data.obj.oilInfo[i + 1].vtime, true), "second") > 300) {
                            // nullData += fuelConsumptionStatistics.GetDateDiff(fuelConsumptionStatistics.UnixToDate(data.obj.oilInfo[i].vtime, true), fuelConsumptionStatistics.UnixToDate(data.obj.oilInfo[i + 1].vtime, true), "hour");
                            if (rtime == 0) {
                                var ctime = +fuelConsumptionStatistics.timeAdd(fuelConsumptionStatistics.UnixToDate(data.obj.oilInfo[i].vtime, true));
                                for (var n = 0; n < fuelConsumptionStatistics.GetDateDiff(fuelConsumptionStatistics.UnixToDate(data.obj.oilInfo[i].vtime, true), fuelConsumptionStatistics.UnixToDate(data.obj.oilInfo[i + 1].vtime, true), "minute") * 2; n++) {
                                    date.push(fuelConsumptionStatistics.timeStamp2String(new Date(ctime += 30000)));
                                    oil.push(null);
                                    mileage.push(null);
                                    speed.push(null);
                                    fuelTemp.push(null);
                                    envTemp.push(null);
                                }
                            } else {
                                var ctime = +fuelConsumptionStatistics.timeAdd(fuelConsumptionStatistics.UnixToDate(data.obj.oilInfo[i].vtime, true));
                                for (var n = 0; n < fuelConsumptionStatistics.GetDateDiff(fuelConsumptionStatistics.UnixToDate(data.obj.oilInfo[i].vtime, true), fuelConsumptionStatistics.UnixToDate(data.obj.oilInfo[i + 1].vtime, true), "minute") * chenageTimes - 1; n++) {
                                    date.push(fuelConsumptionStatistics.timeStamp2String(new Date(ctime += rtime)));
                                    oil.push(null);
                                    mileage.push(null);
                                    speed.push(null);
                                    fuelTemp.push(null);
                                    envTemp.push(null);
                                }
                            }
                        }
                    }
                }
                if (data.obj.infoDtails.infoDtail.length != 0 && data.obj.infoDtails.infoDtail.length != null) {
                    startLoc = [], endLoc = [];
                    for (var j = data.obj.infoDtails.infoDtail.length - 1, lens = 0; j >= lens; j--) {
                        if (fuelConsumptionStatistics.GetDateDiff(data.obj.infoDtails.infoDtail[j].startTime, data.obj.infoDtails.infoDtail[j].endTime, "minute") >= 0.50) {
                            startLoc.push(data.obj.infoDtails.infoDtail[j].startPositonal);
                            endLoc.push(data.obj.infoDtails.infoDtail[j].endPositonal);
                            dataSet.push([j,
                                data.obj.infoDtails.infoDtail[j].plateNumber,
                                fuelConsumptionStatistics.timeStamp2String(data.obj.infoDtails.infoDtail[j].startTime),
                                fuelConsumptionStatistics.timeStamp2String(data.obj.infoDtails.infoDtail[j].endTime),
                                (fuelConsumptionStatistics.toHHMMSS(fuelConsumptionStatistics.GetDateDiff(data.obj.infoDtails.infoDtail[j].startTime, data.obj.infoDtails.infoDtail[j].endTime, "hour"))),
                                parseFloat(data.obj.infoDtails.infoDtail[j].fuelConsumption.toFixed(2)),
                                parseFloat(data.obj.infoDtails.infoDtail[j].steerMileage.toFixed(1)),
                                parseFloat(data.obj.infoDtails.infoDtail[j].perHundredKilimeters.toFixed(2)),
                                "加载中...",
                                "加载中..."
                            ])
                        }
                        travelTime += fuelConsumptionStatistics.GetDateDiff(data.obj.infoDtails.infoDtail[j].startTime, data.obj.infoDtails.infoDtail[j].endTime, "hour")
                    }
                }
                var str = '';
                var startFuel = Number(data.obj.oilInfo[0].totalOilwearOne);
                var endFuel = Number(data.obj.oilInfo[data.obj.oilInfo.length - 1].totalOilwearOne);
                var startMile;
                var endMile;
                if (flogKey == "true") {
                    startMile = Number(data.obj.oilInfo[0].mileageTotal === undefined ? 0 : data.obj.oilInfo[0].mileageTotal);
                    endMile = Number(data.obj.oilInfo[data.obj.oilInfo.length - 1].mileageTotal === undefined ? 0 : data.obj.oilInfo[data.obj.oilInfo.length - 1].mileageTotal);
                } else {
                    startMile = Number(data.obj.oilInfo[0].gpsMile);
                    endMile = Number(data.obj.oilInfo[data.obj.oilInfo.length - 1].gpsMile);
                }

                // 油耗
                var Fuel = endFuel - startFuel;
                // 里程
                var Mile = endMile - startMile;
                if (Mile < 0) {
                    Mile = 0;
                }
                // 温度差
                var maxTemp = fuelConsumptionStatistics.filterTheNull(tmp);
                var diffTemperature = Math.max.apply(null, maxTemp) - Math.min.apply(null, maxTemp);
                // 平均速度
                var averageVelocity;
                if (travelTime != 0) {
                    averageVelocity = (((Mile) / travelTime)).toFixed(2);
                } else {
                    averageVelocity = 0;
                }
                // 百公里油耗
                var hundredFuel;
                if (Mile != 0) {
                    hundredFuel = (((Fuel) / (Mile)) * 100).toFixed(2)
                } else {
                    hundredFuel = 0;
                }
                $("#travelTime").text(fuelConsumptionStatistics.toHHMMSS(travelTime));
                if (Number(data.obj.infoDtails.totalT) - Number(travelTime) - nullData > 0) {
                    $("#idleTime").text(fuelConsumptionStatistics.toHHMMSS((Number(data.obj.infoDtails.totalT) - Number(travelTime)) - nullData));
                } else if (Number(data.obj.infoDtails.totalT) - Number(travelTime) > 0 && Number(data.obj.infoDtails.totalT) - Number(travelTime) - nullData < 0) {
                    $("#idleTime").text(fuelConsumptionStatistics.toHHMMSS((Number(data.obj.infoDtails.totalT) - Number(travelTime))));
                } else {
                    $("#idleTime").text("0");
                }
                $("#mileage").text(parseFloat(Mile.toFixed(1)) + "km");
                $("#fuel").text(parseFloat((Fuel).toFixed(2)) + "L");
                $("#hundredFuel").text(parseFloat(hundredFuel) + "L/100km");
                $("#averageVelocity").text(parseFloat(averageVelocity) + "km/h");
                $("#difference").text(parseFloat(diffTemperature.toFixed(1)) + "℃");
                $("#detail").empty();
                $("#detail").append(str);
                fuelConsumptionStatistics.draggle();
            } else {
                $("#showClick").attr("class", "fa fa-chevron-up");
                $("#graphShow").hide();
                $("#travelTime").text("0");
                $("#idleTime").text("0");
                $("#mileage").text("0km");
                $("#fuel").text("0L");
                $("#hundredFuel").text("0L/100km");
                $("#averageVelocity").text("0km/h");
                $("#difference").text("0℃");
            }
            if (data.success == false) {
                layer.msg(systemError)
                oil = [];
                mileage = [];
                speed = [];
                dataSet = [];
                date = [];
                tmp = [];
                fuelTemp = [];
                envTemp = [];
            }
            ;
            $("#graphShow").show();
            $("#showClick").attr("class", "fa fa-chevron-down");
            fuelConsumptionStatistics.init();
            if (fuelConsumptionStatistics.validates()) {
                fuelConsumptionStatistics.reloadData(dataSet);
            }
        },
        //上一天(前段没有此按钮)
        upDay: function () {
            fuelConsumptionStatistics.startDay(1);
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (charNum != "" && groupValue != "") {
                var dateValue = new Date().getTime();
                var startTimeValue = new Date(startTime.split(" ")[0].replace(/-/g, "/")).getTime();
                if (startTimeValue <= dateValue) {
                    fuelConsumptionStatistics.ajaxList(charNum, startTime, endTime);
                    fuelConsumptionStatistics.validates()
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
        // 今天
        todayClick: function () {
            fuelConsumptionStatistics.nowDay();
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (!fuelConsumptionStatistics.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            } else {
                $("#charSelect-error").hide();
            }
            fuelConsumptionStatistics.ajaxList(charNum, startTime, endTime);
        },
        // 前一天
        yesterdayClick: function () {
            fuelConsumptionStatistics.startDay(-1);
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (!fuelConsumptionStatistics.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            } else {
                $("#charSelect-error").hide();
            }
            fuelConsumptionStatistics.ajaxList(charNum, startTime, endTime);
        },
        // 近三天
        nearlyThreeDays: function () {
            fuelConsumptionStatistics.startDay(-3);
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (!fuelConsumptionStatistics.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            } else {
                $("#charSelect-error").hide();
            }
            fuelConsumptionStatistics.ajaxList(charNum, startTime, endTime);
        },
        // 近七天
        nearlySevenDays: function () {
            fuelConsumptionStatistics.startDay(-7);
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (!fuelConsumptionStatistics.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            } else {
                $("#charSelect-error").hide();
            }
            fuelConsumptionStatistics.ajaxList(charNum, startTime, endTime);
        },
        // 查询
        inquireClick: function () {
            var groupValue = $("#groupSelect").val();
            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];
            var charNum = $("#charSelect").attr("data-id");
            if (!fuelConsumptionStatistics.validates()) {
                return;
            }
            if (charNum == '') {
                $("#charSelect-error").html('请至少选择一个监控对象').show();
                return;
            } else {
                $("#charSelect-error").hide();
            }
            fuelConsumptionStatistics.ajaxList(charNum, startTime, endTime);
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
        // datatable数据加载
        exampleTable: function (data) {
            table = $('#oilTable').DataTable({
                "destroy": true,
                "dom": 'trlip',// 自定义显示项
                "scrollX": true,
                "data": data,
                "lengthChange": true,// 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true,// 本地搜索
                "ordering": false, // 排序功能
                "Info": true,// 页脚信息
                "autoWidth": true,// 自动宽度
                "pageLength": 10,
                "lengthMenu": [5, 10, 20, 50, 100, 200],
                "pagingType": "simple_numbers", // 分页样式
                "dom": "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
                "oLanguage": {// 国际语言转化
                    "oAria": {
                        "sSortAscending": " - click/return to sort ascending",
                        "sSortDescending": " - click/return to sort descending"
                    },
                    "sLengthMenu": "显示 _MENU_ 记录",
                    "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录。",
                    "sZeroRecords": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sEmptyTable": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sLoadingRecords": "正在加载数据-请等待...",
                    "sInfoEmpty": "当前显示0到0条，共0条记录",
                    "sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
                    "sProcessing": "<img src='../resources/user_share/row_details/select2-spinner.gif'/> 正在加载数据...",
                    "sSearch": "模糊查询：",
                    "sUrl": "",
                    "oPaginate": {
                        "sFirst": "首页",
                        "sPrevious": " 上一页 ",
                        "sNext": " 下一页 ",
                        "sLast": " 尾页 "
                    }
                },
                "drawCallback": function (settings) {
                    var newStartLoc = startLoc;
                    var newEndLoc = endLoc;
                    var aiDisplay = settings.aiDisplay;
                    if (aiDisplay.length != '0' && aiDisplay.length < startLoc.length) {
                        newStartLoc = [], newEndLoc = [];
                        for (var i = 0; i < aiDisplay.length; i++) {
                            newStartLoc.push(startLoc[aiDisplay[i]]);
                            newEndLoc.push(endLoc[aiDisplay[i]]);
                        }
                    }
                    var $dataTableTbody = $("#oilTable tbody");
                    var alarmLocation = $dataTableTbody.children("tr:last-child").children("td:nth-child(9)").text();
                    //报警位置进行逆地址解析
                    if (alarmLocation == "加载中..."&&!getAddressStatus) {
                        fuelConsumptionStatistics.getAddress(newStartLoc, newEndLoc);
                    }
                },
                "order": [
                    [0, null]
                ],// 第一列排序图标改为默认

            });
            table.on('draw', function () {
                setTimeout(function () {
                    getAddressStatus = false;
                }, 300)
            });
            table.on('order.dt search.dt', function () {
                table.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
        },
        reloadData: function (dataList) {
            var currentPage = table.page();
            table.clear();
            table.rows.add(dataList);
            table.page(currentPage).draw(false);
        },
        goBack: function (GeocoderResult) {
            if (startIndex === null) startIndex = 0;
            msgArray = GeocoderResult;
            var $dataTableTbody = $("#oilTable tbody");
            var dataLength = $dataTableTbody.children("tr").length;
            for (var i = 0; i < dataLength; i++) {
                if (msgArray[i] != undefined) {
                    $dataTableTbody.children("tr:nth-child(" + (startIndex + 1) + ")").children("td:nth-child(9)").text(msgArray[i][0]);
                    $dataTableTbody.children("tr:nth-child(" + (startIndex + 1) + ")").children("td:nth-child(10)").text(msgArray[i][1]);
                    startIndex++;
                }
            }
        },
        //对显示的数据进行逆地址解析
        getAddress: function (startStr, endStr) {
            getAddressStatus = true;
            var $dataTableTbody = $("#oilTable tbody");
            var dataLength = $dataTableTbody.children("tr").length;
            var num = 0;
            startIndex = null;
            for (var i = 0; i < dataLength; i++) {
                num++;
                var alarmLocation = $dataTableTbody.children("tr:nth-child(" + (i + 1) + ")").children("td:nth-child(9)").text();
                if (alarmLocation !== '加载中...') continue;
                if (startIndex === null) startIndex = i;
                var n = $dataTableTbody.children("tr:nth-child(" + (i + 1) + ")").children("td:nth-child(1)").text();
                var startMsg = [];
                var endMsg = [];
                //经纬度正则表达式
                /*var Reg = /^(180\.0{4,7}|(\d{1,2}|1([0-7]\d))\.\d{4,20})(,)(90\.0{4,8}|(\d|[1-8]\d)\.\d{4,20})$/;
                if(startLoc[n-1] != null && Reg.test(startLoc[n-1])){
                    startMsg = [startLoc[n-1].split(",")[0],startLoc[n-1].split(",")[1]];
                }else{
                    startMsg = ["124.411991","29.043817"];
                };
                if(endLoc[n-1] != null && Reg.test(endLoc[n-1])){
                    endMsg = [endLoc[n-1].split(",")[0],endLoc[n-1].split(",")[1]];
                }else{
                    endMsg = ["124.411991","29.043817"];
                }*/
                if (startStr[n - 1] != null && 73.33 <= parseFloat(startStr[n - 1].split(",")[0]) <= 135.05 && 3.51 <= parseFloat(startStr[n - 1].split(",")[1]) <= 53.33) {
                    startMsg = [startStr[n - 1].split(",")[0], startStr[n - 1].split(",")[1]];
                }
                else {
                    startMsg = ["124.411991", "29.043817"];
                }
                if (endStr[n - 1] != null && 73.33 <= parseFloat(endStr[n - 1].split(",")[0]) <= 135.05 && 3.51 <= parseFloat(endStr[n - 1].split(",")[1]) <= 53.33) {
                    endMsg = [endStr[n - 1].split(",")[0], endStr[n - 1].split(",")[1]];
                }
                else {
                    endMsg = ["124.411991", "29.043817"];
                }
                addressMsg.push(startMsg);
                addressMsg.push(endMsg);
                if (num == dataLength) {
                    var addressIndex = 0;
                    var addressArray = [];
                    backAddressMsg(addressIndex, addressMsg, fuelConsumptionStatistics.goBack, addressArray);
                    addressMsg = [];
                }
            }
            ;
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
        /*validates: function () {
            return $("#oilist").validate({
                rules: {
                    groupId: {
                        required: true
                    },
                    endTime: {
                        required: true,
                        compareDate: "#startTime",
                        compareDateDiff: "#startTime"
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
        },
        //列表数据导出
        exportFun: function () {
            if ($('.table:visible tbody tr td').hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if(getRecordsNum('oilTable_info') > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var url = "/clbs/v/oilmgt/exportDataList";
            window.location.href = url
        },
    };
    $(function () {
        $("#toggle-left").bind("click", function () {
            setTimeout(function () {
                fuelConsumptionStatistics.init();
            }, 500)
        });
        Array.prototype.isHas = function (a) {
            if (this.length === 0) {
                return false
            }
            for (var i = 0; i < this.length; i++) {
                if (this[i].seriesName === a) {
                    return true
                }
            }
        };
        fuelConsumptionStatistics.treeInit();
        fuelConsumptionStatistics.nowDay();
        $('#timeInterval').dateRangePicker(
            {
                dateLimit: 7
            }
        );

        fuelConsumptionStatistics.exampleTable(dataSet);

        $("#todayClick").bind("click", fuelConsumptionStatistics.todayClick);
        $("#yesterdayClick,#right-arrow").bind("click", fuelConsumptionStatistics.yesterdayClick);
        $("#nearlyThreeDays").bind("click", fuelConsumptionStatistics.nearlyThreeDays);
        $("#nearlySevenDays").bind("click", fuelConsumptionStatistics.nearlySevenDays);
        $("#inquireClick").bind("click", fuelConsumptionStatistics.inquireClick);
        $("#showClick").bind("click", fuelConsumptionStatistics.showClick);
        $("#left-arrow").bind("click", fuelConsumptionStatistics.upDay);
        $("#endTime").bind("click", fuelConsumptionStatistics.endTimeClick);
        $("#groupSelect").bind("click", showMenuContent); //组织下拉显示

        // 导出
        $("#exportBtn").bind("click", fuelConsumptionStatistics.exportFun);

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                fuelConsumptionStatistics.searchVehicleTree(param);
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
                    fuelConsumptionStatistics.searchVehicleTree(param);
                }
                isSearch = true;
            }, 500);
        });
        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            fuelConsumptionStatistics.searchVehicleTree(param);
        });

    });
})($, window);