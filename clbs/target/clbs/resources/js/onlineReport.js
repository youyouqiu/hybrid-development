(function (window, $) {
    var dataListArray = [];
    var startTime;// 当天时间
    var endTime;// 当天时间
    var sTime;
    var eTime;
    var key = true;
    var vid;
    var carLicense = [];
    var activeDays = [];
    var myTable;
    var bflag = true;
    var zTreeIdJson = {};
    var barWidth;
    var number;
    var checkFlag = false; //判断组织节点是否是勾选操作
    var size;//当前权限监控对象数量

    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于100自动勾选
    var searchFlag = true;
    onlineReport = {
        init: function () {
            //车辆树
            var setting = {
                async: {
                    url: onlineReport.getOnlineReportTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    otherParam: {"icoType": "0"},
                    dataFilter: onlineReport.ajaxDataFilter

                },
                check: {
                    enable: true,
                    chkStyle: "checkbox",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    },
                    radioType: "all"
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
                    beforeClick: onlineReport.beforeClickVehicle,
                    beforeCheck: onlineReport.zTreeBeforeCheck,
                    onCheck: onlineReport.onCheckVehicle,
                    onExpand: onlineReport.zTreeOnExpand,
                    //beforeAsync: onlineReport.zTreeBeforeAsync,
                    onAsyncSuccess: onlineReport.zTreeOnAsyncSuccess,
                    onNodeCreated: onlineReport.zTreeOnNodeCreated,
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },

        //模糊查询树
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选

            crrentSubV = [];
            console.log(param)
            if (param == null || param == undefined || param == '') {
                bflag = true;
                onlineReport.init();
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
                        dataFilter: onlineReport.ajaxQueryDataFilter
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
                        beforeClick: onlineReport.beforeClickVehicle,
                        onCheck: onlineReport.onCheckVehicle,
                        onExpand: onlineReport.zTreeOnExpand,
                        onNodeCreated: onlineReport.zTreeOnNodeCreated
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
        getOnlineReportTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
        },
        zTreeBeforeAsync: function () {
            return bflag;
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function () {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (size <= 100 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            onlineReport.getCharSelect(treeObj);
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
            }
            return data;
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;

                    json_ajax("post", "/clbs/m/personalized/ico/getVehicleCount",
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
                if (nodesLength > TREE_MAX_CHILDREN_LENGTH) {
                    layer.msg('最多勾选' + TREE_MAX_CHILDREN_LENGTH + '个监控对象');
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
                    onlineReport.getCheckedNodes();
                    onlineReport.validates();
                }, 600);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            onlineReport.getCharSelect(zTree);
            onlineReport.getCheckedNodes();
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
        },
        //获取到选择的节点
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree.getCheckedNodes(true), v = "", vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].id + ",";
                }
            }
            allVid = vid;
            vnameList = v;
        },

        tableFilter: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            // menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);
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
                tMonth = onlineReport.doHandleMonth(tMonth + 1);
                tDate = onlineReport.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate;
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = onlineReport.doHandleMonth(endMonth + 1);
                endDate = onlineReport.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate;
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = onlineReport.doHandleMonth(vMonth + 1);
                vDate = onlineReport.doHandleMonth(vDate);
                startTime = vYear + "-" + vMonth + "-" + vDate;
                if (day == 1) {
                    endTime = vYear + "-" + vMonth + "-" + vDate;
                } else {
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = onlineReport.doHandleMonth(vendMonth + 1);
                    vendDate = onlineReport.doHandleMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate;
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
        getsTheCurrentTime: function (flag) {
            var nowDate = new Date();
            if (flag) {// 昨天
                nowDate = new Date(nowDate.getTime() - 1000 * 60 * 60 * 24);
            }
            startTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate());
            /* + " " + "00:00:00";*/
            endTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate());
            /* + " "
             + ("23")
             + ":"
             + ("59")
             + ":"
             + ("59");*/
            var atime = $("#atime").val();
            if (atime != undefined && atime != "") {
                startTime = atime;
            }
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
        inquireClick: function (num) {
            number = num;
            if (number == 0) {
                onlineReport.getsTheCurrentTime();
            } else if (number == 'yes') {// 昨天
                onlineReport.getsTheCurrentTime(true);
            } else if (number == -1) {
                onlineReport.startDay(-1)
            } else if (number == -3) {
                onlineReport.startDay(-3)
            } else if (number == -7) {
                onlineReport.startDay(-7)
            }
            ;
            if (num != 1) {
                $('#timeInterval').val(startTime + '--' + endTime);
            }
            if (!onlineReport.validates()) {
                return;
            }
            $(".mileage-Content").css("display", "block");	//显示图表主体
            onlineReport.estimate();
            dataListArray = [];
            var url = "/clbs/m/reportManagement/onlineReport/onlineByF3Pass";
            var data = {"vehicleList": vid, 'startTime': sTime, "endTime": eTime};
            json_ajax("POST", url, "json", true, data, onlineReport.findOnline); //发送请求
        },
        validates: function () {
            return $("#hourslist").validate({
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
                    }
                }
            }).form();
        },
        exportOnline: function () {
            onlineReport.estimate();
            var starTime = sTime;
            var endsTime = eTime;
            var data = {"vehicleList": vid, 'startTime': starTime, "endTime": endsTime};
            var url = "/clbs/m/reportManagement/onlineReport/onlineExport";
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            if (key == true) {
                json_ajax("POST", url, "json", false, data, onlineReport.exportCallback); //发送请求
            }
        },
        estimate: function () {
            var timeInterval = $('#timeInterval').val().split('--');
            sTime = timeInterval[0];
            eTime = timeInterval[1];
            onlineReport.getsTheCurrentTime();
            if (eTime > endTime) {								//查询判断
                layer.msg(endTimeGtNowTime, {move: false});
                key = false
                return;
            }
            if (sTime > eTime) {
                layer.msg(endtimeComStarttime, {move: false});
                key = false;
                return;
            }
            var nowdays = new Date();						// 获取当前时间  计算上个月的第一天
            var year = nowdays.getFullYear();
            var month = nowdays.getMonth();
            if (month == 0) {
                month = 12;
                year = year - 1;
            }
            if (month < 10) {
                month = "0" + month;
            }
            var firstDay = year + "-" + month + "-" + "01 00:00:00";//上个月的第一天
            /* if (sTime < firstDay) {									//查询判断开始时间不能超过  	 上个月的第一天
                 $("#timeInterval-error").html(starTimeExceedOne).show();
                 /!*layer.msg(starTimeExceedOne, {move: false});
                 key = false;*!/
                 return;
             }*/
            $("#timeInterval-error").hide();
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");		//遍历树节点，获取vehicleID 存入集合
            var nodes = treeObj.getCheckedNodes(true);
            vid = "";
            for (var j = 0; j < nodes.length; j++) {
                if (nodes[j].type == "vehicle") {
                    vid += nodes[j].id + ",";
                }
            }
            key = true;
        },
        findOnline: function (data) {//回调函数    数据组装
            var list = [];
            var myChart = echarts.init(document.getElementById('onlineGraphics'));
            var online = "";
            if (data.obj != null && data.obj != "") {
                online = data.obj.online;
            }
            if (data.success == true) {
                carLicense = [];
                activeDays = [];
                if(online != null){
                    for (var i = 0; i < online.length; i++) {
                        list =
                            [
                                i + 1,
                                online[i].carLicense,
                                online[i].color,
                                online[i].activeDays == null ? 0 : online[i].activeDays,
                                online[i].allDays == null ? "0" : online[i].allDays,
                                online[i].onlineDurationStr == null ? "0" : online[i].onlineDurationStr,
                                online[i].onlineCount == null ? "0" : online[i].onlineCount,
                                online[i].ratio == null ? "0" : online[i].ratio,
                                '<span class="detailSpan" data-detail="' + html2Escape(JSON.stringify(online[i].firstDataTimes)) + '">' + (online[i].assignmentName == null ? "无" : online[i].assignmentName) + '</span>',
                                online[i].professionalNames,
                            ]

                        dataListArray.push(list);										//组装完成，传入  表格
                    };
                }
                for (var j = 0; j < dataListArray.length; j++) {// 排序后组装到图表
                    carLicense.push(dataListArray[j][1]);
                    activeDays.push(dataListArray[j][3]);
                }
                onlineReport.reloadData(dataListArray);
                $("#simpleQueryParam").val("");
            } else {
                if (data.msg != null) {
                    layer.msg(data.msg, {move: false});
                }
                carLicense = [];
                activeDays = [];
                carLicense.push("");
                activeDays.push("");
            }
            var start;
            var end;
            var length;
            length = online.length;
            if (length < 4) {
                barWidth = "30%";
            } else if (length < 6) {
                barWidth = "20%";
            } else {
                barWidth = null;
            }
            ;
            if (length <= 20) {
                start = 0;
                end = 100;
            } else {
                start = 0;
                end = 100 * (20 / length);
            }
            ;
            // wjk
            //carLicense = onlineReport.platenumbersplitFun(carLicense);
            var option = {
                tooltip: {
                    trigger: 'axis',
                    textStyle: {
                        fontSize: 20
                    },
                    formatter: function (a) {
                        var relVal = "";
                        //var relValTime = a[0].name;
                        var relValTime = carLicense[a[0].dataIndex];
                        if (a[0].data == 0) {
                            relVal = "无相关数据";
                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[0].color + "'></span>" + a[0].seriesName + "：" + a[0].value + " 天";
                        } else {
                            relVal = relValTime;
                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[0].color + "'></span>" + a[0].seriesName + "：" + a[0].value + " 天";
                        }
                        ;
                        return relVal;
                    }
                },
                legend: {
                    data: ['上线天数'],
                    left: 'auto',
                },
                toolbox: {
                    show: false
                },
                grid: {
                    left: '120',
                    bottom: '100'
                },
                xAxis: {
                    type: 'category',
                    boundaryGap: true,
                    name: "车牌号",
                    axisLabel: {
                        show: true,
                        interval: 0,
                        rotate: 45
                    },
                    data: onlineReport.platenumbersplitFun(carLicense)
                },
                yAxis: [
                    {
                        type: 'value',
                        name: '上线天数',
                        scale: false,
                        position: '',
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine: {
                            show: false
                        }
                    },
                ],
                dataZoom: [{
                    type: 'inside',
                    start: start,
                    end: end
                }, {

                    show: true,
                    height: 20,
                    type: 'slider',
                    top: 'top',
                    xAxisIndex: [0],
                    start: 0,
                    end: 10,
                    showDetail: false,
                }],
                series: [
                    {
                        name: '上线天数',
                        yAxisIndex: 0,
                        type: 'bar',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: '#6dcff6'
                            }
                        },
                        data: activeDays
                    }
                ]
            };
            myChart.setOption(option);
            window.onresize = myChart.resize;
            $("#exportOnline").removeAttr('disabled');
        },
        exportCallback: function (reuslt) {
            if (reuslt == true) {
                var url = "/clbs//m/reportManagement/onlineReport/export";
                window.location.href = url;
            } else {
                layer.msg(exportFail, {move: false});
            }
        },
        //表格初始化
        getTable: function (table) {
            $('.toggle-vis').prop('checked', true);
            myTable = $(table).DataTable({
                "destroy": true,
                "dom": 'tiprl',// 自定义显示项
                "lengthChange": true,// 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true,// 本地搜索
                "ordering": false, // 排序功能
                "Info": true,// 页脚信息
                "autoWidth": true,// 自动宽度
                "stripeClasses": [],
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
                "order": [
                    [0, null]
                ],
                "columnDefs": [{
                    "targets": [0, 2, 3, 4, 5, 6, 7],
                    "searchable": false
                }]
            });
            myTable.on('order.dt search.dt', function () {
                myTable.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
            $('.toggle-vis').off('change').on('change', function (e) {
                var column = myTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            $("#search_button").on("click", function () {
                var tsval = $("#simpleQueryParam").val();
                myTable.column(1).search(tsval, false, false).draw();
            });
        },
        reloadData: function (dataList) {
            var currentPage = myTable.page()
            myTable.clear()
            myTable.rows.add(dataList)
            myTable.page(currentPage).draw(false);
            myTable.column(1).search('', false, false).page(currentPage).draw();
        },
        //刷新列表
        refreshTable: function () {
            onlineReport.inquireClick(1, false);
        },
        // wjk 车牌号太长显示不完截取
        platenumbersplitFun: function (arr) {
            var newArr = [];
            arr.forEach(function (item) {
                if (item.length > 8) {
                    item = item.substring(0, 7) + '...'
                }
                newArr.push(item)
            })
            return newArr
        },
        getYesterDay: function () {
            var nowDate = new Date();
            var date = new Date(nowDate.getTime() - 24 * 60 * 60 * 1000);
            var seperator1 = "-";
            var year = date.getFullYear();
            var month = date.getMonth() + 1;
            var strDate = date.getDate();
            if (month >= 1 && month <= 9) {
                month = "0" + month;
            }
            if (strDate >= 0 && strDate <= 9) {
                strDate = "0" + strDate;
            }
            var yesterdate = year + seperator1 + month + seperator1 + strDate;
            return yesterdate;
        },
        // 展开明细列表
        showTableDetail: function (event) {
            var targetTr = $(event).closest('tr');
            var nextTr = targetTr.next('tr');
            if (nextTr.hasClass('addTr')) {
                if (nextTr.is(':hidden')) {
                    nextTr.show();
                } else {
                    nextTr.hide();
                }
                return;
            }

            var detail = targetTr.find('.detailSpan').attr('data-detail');
            var detailData = (detail != 'null') ? JSON.parse(detail) : [];
            var addTr = '';
            for (var i = 0, len = detailData.length; i < len; i++) {
                addTr += '<tr class="newTr"><td>' + (i + 1) + '</td><td>' + detailData[i] + '</td></tr>';
            }
            var html = '<tr class="addTr">' +
                '        <td colspan="11">' +
                '            <div class="detailTableBox">' +
                '                <table class="table table-striped table-bordered table-hover noCheckTable"' +
                '                 cellspacing="0" width="100%">' +
                '                    <thead>' +
                '                    <th>序号</th>' +
                '                    <th>上线时间</th>' +
                '                    </thead>' +
                '                    <tbody>' + addTr + '</tbody>' +
                '                </table>' +
                '            </div>' +
                '        </td>' +
                '    </tr>';
            targetTr.after(html);
        },
    };
    $(function () {
        $('input').inputClear();
        onlineReport.init();																//初始化页面
        // $('#timeInterval').dateRangePicker({dateLimit:3});
        $('#timeInterval').dateRangePicker({
            dateLimit: 31,
            isOffLineReportFlag: true,
            nowDate: onlineReport.getYesterDay(),
            isShowHMS: false
        });
        onlineReport.tableFilter();
        onlineReport.getTable('#dataTable');
        onlineReport.getsTheCurrentTime();													//当前时间
        $("#groupSelect").bind("click", showMenuContent);				//组织下拉显示
        $(window).resize(onlineReport.windowResize);										//监听窗口变化 重新加载图表
        $("#toggle-left").bind("click", function () {										//左侧菜单切换重新绘制图表
            $("body").css("overflow-x", "hidden");
            setTimeout(function () {
                onlineReport.MileageGraphics();
            }, 500)
        });
        $("#refreshTable").bind("click", onlineReport.refreshTable);

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                onlineReport.searchVehicleTree(param);
            }
            ;
        });

        // 点击列表项查看明细列表
        $("#dataTable tbody").on('click', 'tr', function () {
            if (!$(this).hasClass('addTr') && !$(this).hasClass('newTr')) {
                onlineReport.showTableDetail(this);
            }
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
            ;
            inputChange = setTimeout(function () {
                if (searchFlag) {
                    var param = $("#groupSelect").val();
                    onlineReport.searchVehicleTree(param);
                }
                searchFlag = true;
            }, 500);
        });
        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            onlineReport.searchVehicleTree(param);
        });
    });
}(window, $))