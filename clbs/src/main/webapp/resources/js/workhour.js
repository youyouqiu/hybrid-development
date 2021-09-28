//# sourceURL=workhour.js
(function () {
    var charType = true; // i have a dream
    var liFlag = true;
    var barWidth;
    var parameter;//查询三个标定值
    var optionALL;
    var allChart;
    var option;
    var myChart;
    var charNum;
    var startTime;
    var endTime;
    var base = +new Date();
    var oneDay = 30000;
    var date = [];
    var dateOneChar = [];
    var table;
    var dataSet = [];//table数据
    for (var i = 1; i < 10; i++) {
        var now = new Date(base += oneDay);
        date.push([now.getFullYear(), now.getMonth() + 1, now.getDate()]
                .join('-')
            + " "
            + [
                now.getHours() < 10 ? '0' + now.getHours() : now
                    .getHours(),
                now.getMinutes() < 10 ? '0' + now.getMinutes() : now
                    .getMinutes(),
                now.getSeconds() < 10 ? '0' + now.getSeconds() : now
                    .getSeconds()].join(':'));
    }
    ;
    var allWorkTime = [];//工作时长
    var oneWorkTime = [7.0, 6.9, 9.5, 14.5, 18.2, 21.5];
    var chart;
    var oneChart
    var dataSet = []
    var dataSet1 = []
    var address = []
    var addressMsg = []
    var dengerFre = [];//报警频率
    var workFre = [];//工作频率
    var idleSpeedFre = [];//怠速频率 
    // 车辆list 
    var vehicleList = JSON.parse($("#vehicleList").attr("value"));
    var zTreeIdJson = {};
    var size;//当前权限监控对象数量
    var checkFlag = false;
    var getAddressStatus = false;
    workhour = {
        init: function () {
            $("[data-toggle='tooltip']").tooltip();
            //车辆树
            var setting = {
                async: {
                    //url: "/clbs/m/functionconfig/fence/bindfence/vehicelTree",
                    url: workhour.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple"},
                    otherParam: {"icoType": "0"},
                    dataFilter: workhour.ajaxDataFilter
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
                    beforeClick: workhour.beforeClickVehicle,
                    onCheck: workhour.onCheckVehicle,
                    beforeCheck: workhour.zTreeBeforeCheck,
                    onExpand: workhour.zTreeOnExpand,
                    //beforeAsync: workhour.zTreeBeforeAsync,
                    onAsyncSuccess: workhour.zTreeOnAsyncSuccess,
                    onNodeCreated: workhour.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
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
        //zTreeBeforeAsync: function () {
        //   return bflag;
        //},
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (size <= 5000) {
                treeObj.checkAllNodes(true);
            }
            workhour.getCharSelect(treeObj);
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
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
            }
            if (treeNode.type != "assignment" || (treeNode.type == "assignment" && treeNode.children != undefined)) {
                workhour.getCharSelect(zTree);
            }
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
                    veh.push(nodes[i].name)
                    vid.push(nodes[i].id)
                }
            }
            var vehName = workhour.unique(veh);
            var vehId = workhour.unique(vid);
            $("#charSelect").empty();
            var deviceDataList = {value: []};
            for (var j = 0; j < vehName.length; j++) {
                for (var k = 0; k < vehicleList.length; k++) {
                    if (vehId[j] == vehicleList[k].vehicleId) {
                        deviceDataList.value.push({
                            name: vehName[j],
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
            //$("#groupSelect").bind("click",showMenuContent);
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
        oneChar: function () {
            myChart = echarts.init(document.getElementById('oneWork'));
            option = {
                tooltip: {
                    trigger: 'axis',
                    textStyle: {
                        fontSize: 20
                    },
                    formatter: function (a) {
                        var relVal = "";
                        relVal = a[0].name;
                        if (relVal == "") {
                            relVal += "暂无相关数据";
                        } else {
                            for (var i = 0; i < a.length; i++) {

                                if (a[i].seriesName == "频率") {

                                    relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + ":" + a[i].data + "Hz";
                                } else {
                                    if (a[i].data === "") {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + ":未设置";
                                    } else {
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + ":" + a[i].data + "Hz";
                                    }
                                }
                            }
                        }
                        return relVal;
                    }
                },
                legend: {
                    data: ['频率', '报警频率', '工作频率', '怠速频率'],
                    left: 'auto'
                },
                toolbox: {
                    show: false
                },
                xAxis: {
                    type: 'category',
                    boundaryGap: false,
                    data: dateOneChar
                },
                yAxis: [
                    {
                        type: 'value',
                        name: '频率(Hz)',
                        scale: true,
                        position: 'left',
                        axisLine: {
                            lineStyle: {}
                        },
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
                        name: '频率',
                        yAxisIndex: 0,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(248, 123, 0)'
                            }
                        },
                        zIndex: 9999999,
                        data: oneWorkTime
                    },
                    {
                        name: '报警频率',
                        yAxisIndex: 0,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgba(255, 0, 0,0.5)'
                            }
                        },
                        zIndex: 9999999,
                        data: dengerFre
                    },
                    {
                        name: '工作频率',
                        yAxisIndex: 0,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgba(27, 172, 42,0.5)'
                            }
                        },

                        zIndex: 9999999,
                        data: workFre
                    },
                    {
                        name: '怠速频率',
                        yAxisIndex: 0,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(204, 204, 204)'
                            }
                        },
                        zIndex: 9999999,
                        data: idleSpeedFre
                    }
                ]
            };
            myChart.setOption(option);
            myChart.on('click', function (params) {
            });
            window.onresize = myChart.resize;
        },
        //开始时间
        startDay: function (day) {
            var timeInterval = $('#timeInterval').val().split('--');
            var startValue = timeInterval[0];
            var endValue = timeInterval[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60
                    * 24 * day;
                today.setTime(targetday_milliseconds); //注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = workhour.doHandleMonth(tMonth + 1);
                tDate = workhour.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " "
                    + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = workhour.doHandleMonth(endMonth + 1);
                endDate = workhour.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " "
                    + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = workhour.doHandleMonth(vMonth + 1);
                vDate = workhour.doHandleMonth(vDate);
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
                    vendMonth = workhour.doHandleMonth(vendMonth + 1);
                    vendDate = workhour.doHandleMonth(vendDate);
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
        //当前时间
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
                + 23
                + ":"
                + 59
                + ":"
                + 59;
        },
        allAreaShow: function (charNum, startTime, endTime) {
            charType = true;
            parameter = {"vehicleId": charNum};
            workhour.ajaxList(0, charNum, startTime, endTime);
            workhour.ajaxList(1, charNum, startTime, endTime);
            getAddressStatus = false;
        },

        //ajax请求数据
        ajaxList: function (type, band, startTime, endTime) {
            //    $("#carName").text($("input[name='charSelect']").val());

            // wjk
            $('#carName').text(workhour.platenumbersplitFun($("input[name='charSelect']").val()));
            //没有截取就不显示tooltip
            if ($("input[name='charSelect']").val() == workhour.platenumbersplitFun($("input[name='charSelect']").val())) {
                $('#carName').removeAttr('data-original-title')
            } else {
                $('#carName').attr('data-original-title', $("input[name='charSelect']").val());
            }

            $(".toopTip-btn-left,.toopTip-btn-right").css("display", "inline-block");
            $.ajax({
                type: "POST",
                url: "/clbs/v/workhourmgt/vbStatistic/getWorkHours",
                data: {
                    "type": type,
                    "band": band,
                    "startTime": startTime,
                    "endTime": endTime
                },
                dataType: "json",
                async: true,
                beforeSend: function () {
                    layer.load(2);
                },
                success: function (data) {
                    layer.closeAll('loading');
                    $("#gsmx").addClass("active");
                    $("#gstj").removeClass("active");
                    $("#profile1").addClass("active");
                    $("#home1").removeClass("active");
                    var msg = $.parseJSON(data.msg);
                    if (data.msg != "{}") {
                        if (msg.statisticses != undefined && msg.statisticses !== null) {
                            var responseData = JSON.parse(ungzip(msg.statisticses));
                            msg.statisticses = responseData;
                        }
                        $("#graphShow").show();
                        if (msg.workHours != undefined && msg.workHours !== null) {
                            allWorkTime = (msg.workHours);
                            if (allWorkTime.length < 5) {
                                barWidth = "10%";
                            } else if (allWorkTime.length < 6) {
                                barWidth = "50%";
                            } else {
                                barWidth = null;
                            }
                        }
                        if (msg.workDates != undefined && msg.workDates !== null) {
                            date = msg.workDates;
                        }
                        if (msg.statisticses != undefined && msg.statisticses !== null) {
                            var t = [];
                            var ttime = 0;
                            dataSet = [];
                            for (var j = 0; j < allWorkTime.length; j++) {
                                ttime += allWorkTime[j]
                            }
                            for (var i = 0; i < msg.statisticses.length; i++) {
                                t = [msg.statisticses[i].no, "(" + msg.statisticses[i].team + ")", msg.statisticses[i].brand, workhour.toHHMMSS(ttime.toFixed(2)), msg.statisticses[i].workTimes]
                                dataSet.push(t)
                            }
                        }
                        if (msg.detail != undefined && msg.detail !== null) {
                            var d = [];
                            dataSet1 = [];
                            for (var j = msg.detail.length - 1; j >= 0; j--) {
                                if (msg.detail[j].workHours != 0) {
                                    d = [msg.detail[j].no, msg.detail[j].brand, msg.detail[j].startTime, msg.detail[j].endTime, workhour.toHHMMSS(msg.detail[j].workHours.toFixed(2)), "加载中..."
                                    ];
                                    dataSet1.push(d)
                                    address.push(msg.detail[j].longtitude + "," + msg.detail[j].latitude)
                                }
                            }
                        }
                        if (msg.detailDates != undefined && msg.detailDates !== null) {
                            dateOneChar = msg.detailDates;
                        }

                        if (msg.workRate != undefined && msg.workRate !== null) {
                            oneWorkTime = msg.workRate;
                            json_ajax("POST", "/clbs/v/workhourmgt/vbStatistic/getThresholds", "json", true, parameter, workhour.getCallBack);
                        }
                    } else {
                        allWorkTime = [];
                        dataSet = [];
                        dataSet1 = [];
                        workhour.exampleTable(dataSet);
                        workhour.exampleTable1(dataSet1);
                        $("#graphShow,#oneWorkTime").show();
                        $("#showClick").attr("class", "fa fa-chevron-down");
                        dengerFre = [];
                        workFre = [];
                        idleSpeedFre = [];
                        dateOneChar = [];
                        oneWorkTime = [];
                        workhour.oneChar();
                    }
                    if (dataSet.length == 0) {
                        return false;
                    }
                    workhour.exampleTable(dataSet);

                    if (dataSet1.length == 0) {
                        return false;
                    }
                    workhour.exampleTable1(dataSet1);
                    $("#oneWorkTime").show();


                }
            });
            $('#timeInterval').val(startTime + '--' + endTime);
        },
        //上一天(页面没有后面一天按钮，这里不需要改，可以删除)
        upDay: function () {
            workhour.startDay(1);
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (charNum != "" && groupValue != "") {
                var dateValue = new Date().getTime();
                var startTimeValue = new Date(startTime.split(" ")[0].replace(/-/g, "/")).getTime();
                if (startTimeValue <= dateValue) {
                    workhour.allAreaShow(charNum, startTime, endTime);
                    workhour.validates();
                } else {
                    layer.msg("暂时没办法穿越，明天我再帮您看吧！");
                }
            } else {
                layer.msg("请选择监控对象！", {move: false});
            }
        },
        // 今天
        todayClick: function () {
            workhour.nowDay();
            charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (charNum != "" && groupValue != "") {
                workhour.allAreaShow(charNum, startTime, endTime);
                workhour.validates();
            } else {
                layer.msg(selectMonitoringObjec, {move: false});
            }
        },
        //前一天
        yesterdayClick: function () {
            workhour.startDay(-1);
            charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (charNum != "" && groupValue != "") {
                workhour.allAreaShow(charNum, startTime, endTime);
                workhour.validates();
            } else {
                layer.msg(selectMonitoringObjec, {move: false});
            }
        },
        //近三天
        nearlyThreeDays: function () {
            workhour.startDay(-3);
            charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (charNum != "" && groupValue != "") {
                workhour.allAreaShow(charNum, startTime, endTime);
                workhour.validates();
            } else {
                layer.msg(selectMonitoringObjec, {move: false});
            }
        },
        //近七天
        nearlySevenDays: function () {
            workhour.startDay(-7);
            charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (charNum != "" && groupValue != "") {
                workhour.allAreaShow(charNum, startTime, endTime);
                workhour.validates();
            } else {
                layer.msg(selectMonitoringObjec);
            }
        },
        //查询
        inquireClick: function () {
            charNum = $("#charSelect").attr("data-id");
            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];
            if (charNum != "") {
                if (workhour.validates()) {
                    workhour.allAreaShow(charNum, startTime, endTime);
                }
            } else {
                layer.msg(selectMonitoringObjec);
            }
        },
        showClick: function () {
            if ($(this).hasClass("fa-chevron-up")) {
                $(this).attr("class", "fa fa-chevron-down");
                $("#graphShow").show();
            } else {
                $(this).attr("class", "fa fa-chevron-up");
                $("#graphShow").hide();
            }
        },
        draggle: function () {
            $("#showClick").attr("class", "fa fa-chevron-down");
            $("#graphShow").show();
        },
        getCallBack: function (data) {
            dengerFre = new Array();
            workFre = new Array();
            idleSpeedFre = new Array();
            var dengerFreData;
            var workFreData;
            var idleSpeedFreData;
            if (data.alarmFrequencyThreshold == null || data.alarmFrequencyThreshold == "") {
                dengerFreData = "--"
            } else {
                dengerFreData = parseInt(data.alarmFrequencyThreshold);
            }

            if (data.workFrequencyThreshold == null || data.workFrequencyThreshold == "") {
                workFreData = "--"
            } else {
                workFreData = parseInt(data.workFrequencyThreshold);
            }
            if (data.idleFrequencyThreshold == null || data.idleFrequencyThreshold == "") {
                idleSpeedFreData = "--"
            } else {
                idleSpeedFreData = parseInt(data.idleFrequencyThreshold);
            }

            for (var i = 0; i < oneWorkTime.length; i++) {
                dengerFre.push(dengerFreData);
                workFre.push(workFreData);
                idleSpeedFre.push(idleSpeedFreData);
            }
            ;
            workhour.oneChar();
        },
        exampleTable: function (data) {
            table = $('#chedui').DataTable({
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
                "order": [
                    [0, null]
                ],// 第一列排序图标改为默认
                "columnDefs": [{
                    "targets": 2,
                    "searchable": false,
                    "class": "text-center",
                }, {
                    "targets": 0,
                    "class": "text-center",
                }, {
                    "targets": 1,
                    "class": "text-center",
                }, {
                    "targets": 3,
                    "class": "text-center",
                }, {
                    "targets": 4,
                    "class": "text-center",
                }
                ],
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
        exampleTable1: function (data) {
            table = $('#chedui1').DataTable({
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
                    var newStopLoc = address;
                    var aiDisplay = settings.aiDisplay;
                    if (aiDisplay.length != '0' && aiDisplay.length < address.length) {
                        newStopLoc = [];
                        for (var i = 0; i < aiDisplay.length; i++) {
                            newStopLoc.push(address[aiDisplay[i]]);
                        }
                    }
                    var $dataTableTbody = $("#chedui1 tbody");
                    var alarmLocation = $dataTableTbody.children("tr:last-child").children("td:nth-child(6)").text();
                    //报警位置进行逆地址解析
                    if (alarmLocation == "加载中..." && !getAddressStatus) {
                        workhour.getAddress(newStopLoc);
                    }
                },
                "order": [
                    [0, null]
                ],
                "columnDefs": [{
                    "targets": 0,
                    "class": "text-center",
                }, {
                    "targets": 1,
                    "class": "text-center",
                }, {
                    "targets": 2,
                    "class": "text-center",
                }, {
                    "targets": 3,
                    "class": "text-center",
                }, {
                    "targets": 4,
                    "class": "text-center",
                }, {
                    "targets": 5,
                    "class": "text-center",
                }
                ],
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
        //对显示的数据进行逆地址解析
        getAddress: function (addressStr) {
            getAddressStatus = true;
            var $dataTableTbody = $("#chedui1 tbody");
            var dataLength = $dataTableTbody.children("tr").length;
            var num = 0;
            var startIndex = null;
            for (var i = 0; i < dataLength; i++) {
                num++;
                var alarmLocation = $dataTableTbody.children("tr:nth-child(" + (i + 1) + ")").children("td:nth-child(6)").text();
                if (alarmLocation !== '加载中...') continue;
                if (startIndex === null) startIndex = i;
                var n = $dataTableTbody.children("tr:nth-child(" + (i + 1) + ")").children("td:nth-child(1)").text();
                var msg = [];
                //经纬度正则表达式
                var Reg = /^(180\.0{4,7}|(\d{1,2}|1([0-7]\d))\.\d{4,20})(,)(90\.0{4,8}|(\d|[1-8]\d)\.\d{4,20})$/;
                if (addressStr[n - 1] != null && Reg.test(addressStr[n - 1])) {
                    msg = [addressStr[n - 1].split(",")[0], addressStr[n - 1].split(",")[1]];
                } else {
                    msg = ["124.411991", "29.043817"];
                }
                addressMsg.push(msg);
                if (num == dataLength) {
                    var addressIndex = 0;
                    var addressArray = [];
                    backAddressMsg1(addressIndex, addressMsg, null, addressArray, "chedui1", 6, startIndex);
                    addressMsg = [];
                }
            }
            ;
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
        toHHMMSS: function (data) {
            var totalSeconds = data * 60 * 60;
            var hour = Math.floor(totalSeconds / 60 / 60);
            var minute = Math.floor(totalSeconds / 60 % 60);
            var second = Math.floor(totalSeconds % 60);
            return hour + "小时" + minute + "分钟" + second + "秒"
        },
        validates: function () {
            return $("#hourslist").validate({
                rules: {
                    groupId: {
                        required: true
                    },
                    charSelect: {
                        required: true
                    },
                    endTime: {
                        required: true,
                        compareDate: "#startTime",
                        compareDateDiff: "#startTime"
                    },
                    startTime: {
                        required: true
                    }
                },
                messages: {
                    groupId: {
                        required: "不能为空"
                    },
                    charSelect: {
                        required: "不能为空"
                    },
                    endTime: {
                        required: "不能为空",
                        compareDate: "结束日期必须大于开始日期!",
                        compareDateDiff: "查询的日期必须小于一周"
                    },
                    startTime: {
                        required: "不能为空",
                    }
                }
            }).form();
        },
        // wjk 车牌号太长显示不完截取
        platenumbersplitFun: function (str) {
            if (str.length > 8) {
                str = str.substring(0, 7) + '...'
            }
            return str
        }
    };

    $(function () {
        workhour.init();
        $('input').inputClear();
        workhour.nowDay();
        $('#timeInterval').dateRangePicker();
        $("#todayClick").bind("click", workhour.todayClick);
        $("#yesterdayClick,#right-arrow").bind("click", workhour.yesterdayClick);
        $("#nearlyThreeDays").bind("click", workhour.nearlyThreeDays);
        $("#nearlySevenDays").bind("click", workhour.nearlySevenDays);
        $("#inquireClick").bind("click", workhour.inquireClick);
        $("#check_nav li").bind("click", workhour.checkIcon);
        $("#showClick").bind("click", workhour.showClick);
        $("#oneWorkTime li").bind("click", workhour.onWorkClick);
        $("#left-arrow").bind("click", workhour.upDay);
        $("#endTime").bind("click", workhour.endTimeClick);
        $("#groupSelect").bind("click", showMenuContent);

    });
}());
