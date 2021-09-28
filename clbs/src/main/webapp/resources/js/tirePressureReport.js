(function ($, window) {
    /**
     * 组织树与监控对象相关参数
     */
    var vehicleList = JSON.parse($("#vehicleList").attr("value")); //绑定传感器的车辆数组
    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询？
    var zTreeIdJson = {};
    var size;//当前权限监控对象数量
    var checkFlag = false;
    var ifAllCheck = true; //刚进入页面小于5000自动勾选
    var isSearch = true; //用于ie浏览器判断选择节点树时是否需要调用搜索功能

    /**
     * 查询参数
     */
    var _vehicleId; //监控对象id
    var startTime;
    var endTime;
    var tireNumber = '1'; //当前是第几个轮胎

    /**
     * 图表显示数据
     */
    var myChart;
    var option;
    var date = []; //x轴显示时间
    var totalMileage = []; //总里程
    var speed = []; //速度
    var tirePressure = []; //轮胎气压
    var tireTemperature = []; //轮胎温度
    var battery = []; //电池电量

    /**
     * 表格
     */
    var myTable;


    tirePressureReport = {
        /**
         * 组织树相关方法
         */
        treeInit: function () {
            var setting = {
                async: {
                    url: tirePressureReport.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: tirePressureReport.ajaxDataFilter
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
                    beforeClick: tirePressureReport.beforeClickVehicle,
                    onCheck: tirePressureReport.onCheckVehicle,
                    beforeCheck: tirePressureReport.zTreeBeforeCheck,
                    onExpand: tirePressureReport.zTreeOnExpand,
                    //beforeAsync: tirePressureReport.zTreeBeforeAsync,
                    onAsyncSuccess: tirePressureReport.zTreeOnAsyncSuccess,
                    onNodeCreated: tirePressureReport.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
            $("[data-toggle='tooltip']").tooltip();
        },
        getTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=monitor";
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
            if (size <= 5000 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            tirePressureReport.getCharSelect(treeObj);

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
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle" || treeNode.type == "thing") {
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
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle" || treeNode.type == "thing") {
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
                    tirePressureReport.validates();
                }, 600);
            }
            if (treeNode.type != "assignment" || (treeNode.type == "assignment" && treeNode.children != undefined)) {
                tirePressureReport.getCharSelect(zTree);
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
                if (nodes[i].type == "vehicle" || nodes[i].type == "people" || nodes[i].type == "thing") {
                    var isNull = nodes[i].name != null && nodes[i].name != undefined && nodes[i].name != "" && nodes[i].id != null && nodes[i].id != undefined && nodes[i].id != "";
                    if (isNull && $.inArray(nodes[i].id, vid) == -1) {
                        veh.push(nodes[i].name)
                        vid.push(nodes[i].id)
                    }
                }
            }
            var vehName = veh;
            var vehId = vid;
            $("#charSelect").empty();
            var deviceDataList = {value: []};
            for (var j = 0; j < vehName.length; j++) {
                for (var k = 0; k < vehicleList.length; k++) {
                    if (vehId[j] == vehicleList[k].vehicleId) {
                        deviceDataList.value.push({
                            name: vehicleList[k].brand,
                            id: vehId[j]
                        });
                    }
                }
            }
            ;
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
            $("#groupSelect,#groupSelectSpan").bind("click", tirePressureReport.showMenu);
            $("#button").removeClass('disabled loading-state-button').find('i').attr("class", 'caret');
        },
        //模糊查询树
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选
            crrentSubV = [];
            $('#charSelect').val('');
            if (param == null || param == undefined || param == '') {
                bflag = true;
                tirePressureReport.treeInit()
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
                        dataFilter: tirePressureReport.ajaxQueryDataFilter
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
                        beforeClick: tirePressureReport.beforeClickVehicle,
                        onCheck: tirePressureReport.onCheckVehicle,
                        // beforeCheck: tirePressureReport.zTreeBeforeCheck,
                        onExpand: tirePressureReport.zTreeOnExpand,
                        //beforeAsync: tirePressureReport.zTreeBeforeAsync,
                        // onAsyncSuccess: tirePressureReport.zTreeOnAsyncSuccess,
                        onNodeCreated: tirePressureReport.zTreeOnNodeCreated
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

        /**
         * 查询数据相关方法
         */
        inquireClick: function (number) {
            var groupValue = $("#groupSelect").val();
            var charNum = $("#charSelect").attr("data-id");
            _vehicleId = charNum;

            if (number == 0) {
                tirePressureReport.nowDay();
            } else if (number != 1) {
                tirePressureReport.startDay(number)
            }
            if (number == 1) {
                var timeInterval = $('#timeInterval').val().split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
            }
            if (!tirePressureReport.validates()) return;
            var param = {
                vehicleId: _vehicleId,
                startTimeStr: startTime,
                endTimeStr: endTime,
            };
            var url = "/clbs/v/statistic/tirePressureReport/getTotalInfo";
            json_ajax("POST", url, "json", true, param, function (data) {
                if (data.success) {
                    if (data.obj < parseInt(tireNumber)) {
                        tireNumber = 1;
                    }
                    tirePressureReport.tireTabInit(data.obj);
                    tirePressureReport.getChartInfo();
                    tirePressureReport.tableInit();
                } else if (data.msg) {
                    layer.msg(data.msg);
                }
            });
        },
        getChartInfo: function () {
            var brandName = $("input[name='charSelect']").val();
            if (brandName.length > 8) {
                $("#carName").attr("title", brandName).tooltip('fixTitle');
                brandName = brandName.substring(0, 7) + '...';
            } else {
                $('#carName').removeAttr('data-original-title')
            }
            $("#carName").text(brandName);

            $(".toopTip-btn-left,.toopTip-btn-right").css("display", "inline-block");
            var param = {
                'tyreNumber': tireNumber,
            }
            json_ajax('post', '/clbs/v/statistic/tirePressureReport/getChartInfo', 'json', true, param, tirePressureReport.chartAjaxDataCallback)
        },
        validates: function () {
            return $("#tireForm").validate({
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
        tireChart: function () {
            $(this).addClass('active').siblings().removeClass('active');
            tireNumber = $(this).attr('tireNumber') - 1;
            tirePressureReport.getChartInfo();
            tirePressureReport.tableInit();
        },

        /**
         * 数据渲染
         * (图表与表格显示)
         */
        // 图表数据
        chartAjaxDataCallback: function (data) {
            date = []; //x轴显示时间
            totalMileage = []; //总里程
            speed = []; //速度
            tirePressure = []; //轮胎气压
            tireTemperature = []; //轮胎温度
            battery = []; //电池电量
            if (data.success) {
                var tireInfo = JSON.parse(ungzip(data.msg));
                var infoLen = tireInfo.length;
                layer.closeAll('loading');
                $('#timeInterval').val(startTime + '--' + endTime);
                if (infoLen > 0) {
                    for (var i = 0; i < infoLen; i++) {
                        date.push(tireInfo[i].vtimeStr);
                        totalMileage.push(tireInfo[i].totalMileage ? tireInfo[i].totalMileage : '');
                        speed.push(tireInfo[i].speed);
                        tirePressure.push(tireInfo[i].pressure);
                        tireTemperature.push(tireInfo[i].temperature);
                        battery.push(tireInfo[i].electric);
                    }
                    $("#graphShow").show();
                    $("#showClick").attr("class", "fa fa-chevron-down");
                }
            } else {
                if (myTable != undefined) {
                    myTable.dataTable.clear();
                    myTable.dataTable.draw();
                }
                layer.msg(data.msg);
            }
            tirePressureReport.echartInit();
        },
        echartInit: function () {
            myChart = echarts.init(document.getElementById('sjcontainer'));
            option = {
                tooltip: {
                    trigger: 'axis',
                    textStyle: {
                        fontSize: 20
                    },
                    formatter: function (a) {
                        var unit = ['km', 'km/h', 'bar', '℃', '%'];
                        var relVal = "";
                        var addRelVal = "";
                        var relValTime = a[0].name;
                        if (a[0].data == null || a[0].data == undefined || a[0].data == '') {
                            relVal = "无相关数据";
                        } else {
                            for (var i = 0; i < a.length; i++) {
                                relVal += "<br/><span style='display:inline-block;vertical-align:3px;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + a[i].data + ' ' + unit[a[i].seriesIndex];
                            }
                        }
                        var allVal = relValTime + addRelVal + relVal;
                        return allVal;
                    }
                },
                legend: {
                    data: ['总里程', '速度', '轮胎气压', '轮胎温度', '电池电量'],
                    left: 'auto'
                },
                color: ['rgb(109,207,246)', 'rgb(145,218,0)', 'rgb(248,123,0)', 'red', '#8a6d3b'],
                grid: {
                    left: 180,
                    right: 240,
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
                        name: '轮胎气压(bar)',
                        scale: true,
                        position: 'left',
                        offset: 80,
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine: {
                            show: false
                        }
                    }, {
                        type: 'value',
                        name: '轮胎温度(℃)',
                        scale: true,
                        position: 'left',
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine: {
                            show: false
                        }
                    },
                    {
                        type: 'value',
                        name: '电池电量(%)',
                        scale: true,
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
                        name: '总里程',
                        scale: true,
                        position: 'right',
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
                        name: '速度',
                        scale: true,
                        position: 'right',
                        offset: 120,
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
                        name: '总里程',
                        type: 'line',
                        yAxisIndex: 3,
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        data: totalMileage
                    }, {
                        name: '速度',
                        type: 'line',
                        yAxisIndex: 4,
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        data: speed
                    },
                    {
                        name: '轮胎气压',
                        type: 'line',
                        yAxisIndex: 0,
                        symbol: 'none',
                        showSymbol: false,
                        sampling: 'average',
                        data: tirePressure,
                    },
                    {
                        name: '轮胎温度',
                        type: 'line',
                        yAxisIndex: 1,
                        symbol: 'none',
                        showSymbol: false,
                        sampling: 'average',
                        data: tireTemperature,
                    },
                    {
                        name: '电池电量',
                        type: 'line',
                        yAxisIndex: 2,
                        symbol: 'none',
                        showSymbol: false,
                        sampling: 'average',
                        data: battery,
                    }
                ]
            };
            myChart.setOption(option);
            window.onresize = myChart.resize;
        },
        //创建表格
        tableInit: function () {
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
                "data": "plateNumber", //监控对象
                "class": "text-center"
            }, {
                "data": "vtimeStr", //时间
                "class": "text-center"
            }, {
                "data": "pressure", //轮胎气压(bar)
                "class": "text-center"
            }, {
                "data": "temperature", //轮胎温度(℃)
                "class": "text-center"
            }, {
                "data": "electric", //电池电量(%)
                "class": "text-center"
            }, {
                "data": "totalMileage", //总里程
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == null) return '';
                    else return data;
                }
            }, {
                "data": "speed", //速度
                "class": "text-center"
            }, {
                "data": null,// 位置
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return '<a class="importantTr" onclick="tirePressureReport.getAlarmAddress(this,\'' + row.latitude + '\',\'' + row.longtitude + '\')">点击获取位置信息</a>'
                }
            }];
            var ajaxDataParamFun = function (d) {
                d.tyreNumber = tireNumber;
            };

            //表格setting
            var setting = {
                listUrl: '/clbs/v/statistic/tirePressureReport/getFormInfo',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'tireTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                getAddress: false,//是否逆地理编码
                // address_index: 9,
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        //解析位置信息
        getAlarmAddress: function (target, latitude, longitude) {
            var url = '/clbs/v/monitoring/address';
            var param = {addressReverse: [latitude, longitude, '', "", 'vehicle']};

            $.ajax({
                type: "POST",//通常会用到两种：GET,POST。默认是：GET
                url: url,//(默认: 当前页地址) 发送请求的地址
                dataType: "json", //预期服务器返回的数据类型。"json"
                async: true, // 异步同步，true  false
                data: param,
                traditional: true,
                timeout: 8000, //超时时间设置，单位毫秒
                success: function (data) {//请求成功
                    $(target).closest('td').html($.isPlainObject(data) ? '未定位' : data);
                },
            });
        },
        // 轮胎tab渲染
        tireTabInit: function (tireNum) {
            var html = '';
            for (var i = 1; i <= tireNum; i++) {
                if (i == parseInt(tireNumber)) {
                    html += '<li class="active tireTab" tireNumber="' + i + '">' +
                        '<a href="javascript:void(0);" data-toggle="tab">轮胎' + i + '</a>' +
                        '</li>'
                } else {
                    html += '<li class="tireTab" tireNumber="' + i + '">' +
                        '<a href="javascript:void(0);" data-toggle="tab">轮胎' + i + '</a>' +
                        '</li>'
                }
            }
            $(".tireTabBox").html(html);
            if (tireNum > 10) {
                $(".tireTabBox").css('marginTop', '25px');
            } else {
                $(".tireTabBox").css('marginTop', '0');
            }
            $('.tireTab').bind('click', tirePressureReport.tireChart); //轮胎tab切换
        },

        /**
         * 时间控制相关方法
         */
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
                tMonth = tirePressureReport.doHandleMonth(tMonth + 1);
                tDate = tirePressureReport.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * parseInt(num);
                today.setTime(end_milliseconds); // 注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = tirePressureReport.doHandleMonth(endMonth + 1);
                endDate = tirePressureReport.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " " + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = tirePressureReport.doHandleMonth(vMonth + 1);
                vDate = tirePressureReport.doHandleMonth(vDate);
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
                    vendMonth = tirePressureReport.doHandleMonth(vendMonth + 1);
                    vendDate = tirePressureReport.doHandleMonth(vendDate);
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
            tirePressureReport.startDay(1);
            var groupValue = $("#groupSelect").val();
            var charNum = $("#charSelect").attr("data-id");
            if (charNum != "" && groupValue != "") {
                var dateValue = new Date().getTime();
                var startTimeValue = new Date(startTime.split(" ")[0].replace(/-/g, "/")).getTime();
                if (startTimeValue <= dateValue) {
                    _vehicleId = charNum;
                    if (tirePressureReport.validates()) {
                        var param = {
                            vehicleId: _vehicleId,
                            startTimeStr: startTime,
                            endTimeStr: endTime,
                        };
                        var url = "/clbs/v/statistic/tirePressureReport/getTotalInfo";
                        json_ajax("POST", url, "json", false, param, function (data) {
                            if (data.success) {
                                var curLen = $('.tireTab').length;
                                if (data.obj < curLen) {
                                    tireNumber = 1;
                                }
                                tirePressureReport.tireTabInit(data.obj);
                                tirePressureReport.getChartInfo();
                                tirePressureReport.tableInit();
                            } else if (data.msg) {
                                layer.msg(data.msg);
                            }
                        });
                    }
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
    };
    $(function () {
        /**
         * 初始配置
         */
        tirePressureReport.treeInit();
        tirePressureReport.nowDay();
        $("#toggle-left").bind("click", function () {
            setTimeout(function () {
                tirePressureReport.echartInit();
            }, 500)
        });
        $('#timeInterval').dateRangePicker({dateLimit: 7});
        $("#groupSelect").bind("click", showMenuContent); //组织下拉显示
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                tirePressureReport.searchVehicleTree(param);
            }
        });


        /**
         * 绑定查询方法
         */
        $("#left-arrow").bind("click", function () {
            tirePressureReport.inquireClick(-1);
        });
        $("#right-arrow").bind("click", function () {
            tirePressureReport.upDay();
        });
        $('.tireTab').bind('click', tirePressureReport.tireChart); //轮胎tab切换


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
                    tirePressureReport.searchVehicleTree(param);
                }
                isSearch = true;
            }, 500);
        });
        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            tirePressureReport.searchVehicleTree(param);
        });
    });
})($, window);