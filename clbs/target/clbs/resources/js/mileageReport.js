(function (window, $) {
    //车辆id列表
    var allVid;
    var curVehicleId;
    var starTimeStr;
    var endTimeStr;
    var vnameList = [];
    var startime;
    var endtime;
    var number;
    var mileageList;
    var mileList;
    var milcName;
    var barWidth;
    var myTable;
    var myTableTwo;
    var myTableThree;
    var mileage;
    var zTreeIdJson = {};
    var checkFlag = false; //判断组织节点是否是勾选操作
    var size;//当前权限监控对象数量


    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于100自动勾选
    var searchFlag = true;
    var inputFlag = false;// 组织树输入框是否输入过标识(用于解决IE浏览器组织树重复加载,导致的闪烁问题)

    mileageReport = {
        init: function () {
            $("[data-toggle='tooltip']").tooltip();
            var setting = {
                async: {
                    //url: "/clbs/m/functionconfig/fence/bindfence/vehicelTree",
                    url: mileageReport.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple"},
                    // otherParam: {"icoType": "0"},
                    dataFilter: mileageReport.ajaxDataFilter
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
                    beforeClick: mileageReport.beforeClickVehicle,
                    onCheck: mileageReport.onCheckVehicle,
                    beforeCheck: mileageReport.zTreeBeforeCheck,
                    onExpand: mileageReport.zTreeOnExpand,
                    //beforeAsync: mileageReport.zTreeBeforeAsync,
                    onAsyncSuccess: mileageReport.zTreeOnAsyncSuccess,
                    onNodeCreated: mileageReport.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        //模糊查询树
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选

            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                if (inputFlag) {
                    inputFlag = false;
                    mileageReport.init();
                }
            } else {
                bflag = true;
                inputFlag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType').val(), "queryParam": param, 'queryType': 'multiple'},
                        dataFilter: mileageReport.ajaxQueryDataFilter
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
                        beforeClick: mileageReport.beforeClickVehicle,
                        onCheck: mileageReport.onCheckVehicle,
                        onExpand: mileageReport.zTreeOnExpand,
                        // beforeCheck: mileageReport.zTreeBeforeCheck,
                        onNodeCreated: mileageReport.zTreeOnNodeCreated
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
                // return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=allMonitor";
            }
        },
        //zTreeBeforeAsync: function () {
        //   return bflag;
        //},
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (size <= 100 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            mileageReport.getCharSelect(treeObj);

            bflag = false;
        },
        zTreeOnNodeCreated: function (event, treeId, treeNode) {
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
                            var chNodes = result[i];//获取对应的value
                            if (zTreeIdJson[pid] != undefined) {
                                var parentTid = zTreeIdJson[pid][0];
                                var parentNode = treeObj.getNodeByTId(parentTid);
                                if (parentNode.children === undefined) {
                                    treeObj.addNodes(parentNode, []);
                                }
                            }
                        });
                    }
                })
            }
            //mileageReport.getCharSelect(treeObj);
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
        tableFilter: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTableOne tr th:gt(0)");
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(1) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
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
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * day;
                today.setTime(targetday_milliseconds); //注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = mileageReport.doHandleMonth(tMonth + 1);
                tDate = mileageReport.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate;
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = mileageReport.doHandleMonth(endMonth + 1);
                endDate = mileageReport.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate;
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = mileageReport.doHandleMonth(vMonth + 1);
                vDate = mileageReport.doHandleMonth(vDate);
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
                    vendMonth = mileageReport.doHandleMonth(vendMonth + 1);
                    vendDate = mileageReport.doHandleMonth(vendDate);
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
        getsTheCurrentTime: function () {
            var nowDate = new Date();
            startTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate());
            endTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate());
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
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                searchFlag = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    mileageReport.getCheckedNodes();
                    mileageReport.validates();
                }, 600);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            mileageReport.getCharSelect(zTree);
            mileageReport.getCheckedNodes();
        },
        //获取到选择的节点
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree.getCheckedNodes(true), v = "", vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle" || nodes[i].type == "people" || nodes[i].type == "thing") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].id + ",";
                }
            }
            allVid = vid;
            vnameList = v;
        },
        inquireClick: function (num) { //查询按钮的单击事件
            number = num;
            if (number == 0) {
                mileageReport.getsTheCurrentTime();
            } else if (number == -1) {
                mileageReport.startDay(-1)
            } else if (number == -3) {

                mileageReport.startDay(-3)
            } else if (number == -7) {
                mileageReport.startDay(-7)
            }
            if (number != 1) {
                $('#timeInterval').val(startTime + '--' + endTime);
                //从页面获取到开始时间
                startime = startTime;
                //从页面获取到结束时间
                endtime = endTime;
            } else {
                var timeInterval = $('#timeInterval').val().split('--');
                startime = timeInterval[0];
                endtime = timeInterval[1];
            }
            ;
            if (!mileageReport.validates()) {
                return;
            }
            mileageReport.getCheckedNodes();
            //请求路径
            // var url = "/clbs/m/reportManagement/mileageReport/getMileageBigData";
            var url = "/clbs/m/reportManagement/mileageReport/getDrivingMileageStatistics";
            //请求参数
            // var pdata = {"allVid": allVid, "startTime": startime, "endTime": endtime};
            var pdata = {"monitorIds": allVid, "startTime": startime, "endTime": endtime};
            //发送请求给服务器
            json_ajax("POST", url, "json", true, pdata, mileageReport.MileageBody);
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
        MileageBody: function (data) {
            if (data.success == true) {
                mileageList = [];//用来存储服务器返回的数据，以便于将数据加载到页面上
                mileList = [];//用来存放里程的数值
                milcName = [];//用来存放查出来的数据的车牌号
                $("#panelTab1").addClass('active').siblings().removeClass('active');
                $("#panel1").addClass('active').siblings().removeClass('active');

                $(".mileage-Content,.ToolPanel").css("display", "block");
                $("#simpleQueryParame").val("");
                mileageReport.getCheckedNodes();
                if (data.obj != null && data.obj.length != 0) {
                    mileage = data.obj;
                    for (var i = 0; i < mileage.length; i++) {
                        var mileageData = (mileage[i].totalMile);
                        var totalOilWearOne = (mileage[i].oilWear);
                        var list =
                            [
                                i + 1,
                                '<button type="button" class="editBtn editBtn-info" onclick=mileageReport.goDrivingDetail("#panelTab2","#panel2",\'' + mileage[i].monitorId + '\')>行驶明细</button>',
                                mileage[i].monitorName,//车牌号
                                mileage[i].groupName,//所属企业
                                mileage[i].assignmentName,//分组
                                mileage[i].signColor,//车牌颜色
                                mileage[i].monitorType,//对象类型
                                mileage[i].durationStr,//行驶时长
                                mileageData,//行驶里程
                                totalOilWearOne,//油耗
                                mileage[i].travelNum//行驶次数
                            ];
                        mileageList.push(list); // 图表
                        milcName.push(mileage[i].monitorName); // 监控对象名字
                        mileList.push(mileageData ? mileageData : ''); // 里程数据
                    }
                    mileageReport.reloadData(mileageList, myTable);
                    $("#simpleQueryParame").val("");
                    $("#search_button").click();
                    mileageReport.MileageGraphics(mileList, milcName);
                } else {
                    mileage = "";
                    $("#simpleQueryParame").val("");
                    $("#search_button").click();
                    mileageReport.reloadData(mileageList, myTable);
                    mileageReport.MileageGraphics(mileList, milcName);
                }
                $("#exportMileage").removeAttr("disabled");
            } else {
                layer.msg(data.msg, {move: false});
            }
        },
        // 展开分组详情
        showGroupDetail: function (event) {
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
            var url = '/clbs/m/reportManagement/mileageReport/getAssignMileageData';
            var param = {
                "assignNames": targetTr.find('td').eq(4).text()
            };
            json_ajax("POST", url, "json", true, param, function (data) {
                if (data.success) {
                    var addStr = '';
                    var assignNames = data.obj.assignNames;
                    var mileages = data.obj.mileages;
                    for (var i = 0, len = assignNames.length; i < len; i++) {
                        addStr += '<tr class="newTr"><td>' + (i + 1) + '</td><td>' + assignNames[i] + '</td><td>' + mileages[i] + '</td></tr>'
                    }
                    var html = '<tr class="addTr">' +
                        '        <td colspan="11">' +
                        '            <div class="groupDetailBox">' +
                        '                <table class="table table-striped table-bordered table-hover noCheckTable"' +
                        '                 cellspacing="0" width="100%">' +
                        '                    <thead>' +
                        '                       <tr class="addTr">' +
                        '                    <th>序号</th>' +
                        '                    <th>分组</th>' +
                        '                    <th>总里程</th>' +
                        '                       </tr>' +
                        '                    </thead>' +
                        '                    <tbody>' + addStr + '</tbody>' +
                        '                </table>' +
                        '            </div>' +
                        '        </td>' +
                        '    </tr>';
                    targetTr.after(html);
                } else if (data.msg) {
                    layer.msg(data.msg);
                }
            });
        },
        MileageGraphics: function (mileList, milcName) {
            // wjk
            //milcName = mileageReport.platenumbersplitFun(milcName);
            if (mileage != undefined) {
                var start;
                var end;
                var length = mileage.length;
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
                var myChart = echarts.init(document.getElementById('MileageGraphics'));
                var option = {
                    tooltip: {
                        trigger: 'axis',
                        textStyle: {
                            fontSize: 20
                        },
                        formatter: function (a) {
                            var relVal = "";
                            relVal = milcName[a[0].dataIndex];
                            if (relVal == null || relVal === '' || relVal == undefined) {
                                relVal = "无相关数据";
                                return relVal;
                            }
                            var data = a[0].data;
                            if (data == null || data === '' || data == undefined) {
                                relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[0].color + "'></span>" + a[0].seriesName + "：" + "无相关数据" + "";
                            } else {
                                relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[0].color + "'></span>" + a[0].seriesName + "：" + a[0].value + " km";
                            }
                            return relVal;
                        }
                    },
                    legend: {
                        data: ['行驶里程'],
                        left: 'auto',
                    },
                    toolbox: {
                        show: false
                    },
                    grid: {
                        left: '100',
                        bottom: '100'
                    },
                    xAxis: {
                        type: 'category',
                        boundaryGap: true,
                        name: "监控对象",
                        axisLabel: {
                            show: true,
                            interval: 0,
                            rotate: 45
                        },
                        data: mileageReport.platenumbersplitFun(milcName)//dataCname
                    },
                    yAxis: [
                        {
                            type: 'value',
                            name: '行驶里程(km)',
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
                        showDetail: false
                    }],
                    series: [
                        {
                            name: '行驶里程',
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
                            data: mileList
                        }
                    ]
                };
                myChart.setOption(option);
                window.onresize = myChart.resize;

                myChart.on('click', function (data) {
                    $("#panelTab1").addClass('active').siblings().removeClass('active');
                    $("#panel1").addClass('active').siblings().removeClass('active');
                    $("#simpleQueryParame").val(milcName[data.dataIndex]);
                    $("#search_button").click();
                });
            }
        },
        getTable: function (table) {
            $('.toggle-vis').prop('checked', 'true');
            var tableName = $(table).DataTable({
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
                    "sInfoFiltered": "",
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
                "columnDefs": [{
                    "targets": [0, 3, 4],
                    "searchable": false
                }],
                "order": [
                    [0, null]
                ],// 第一列排序图标改为默认

            });
            if (myTable) {
                myTable.off('order.dt search.dt').on('order.dt search.dt', function () {
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
                $("#search_button").off('click').on("click", function () {
                    var tsval = $("#simpleQueryParame").val();
                    $("#hiddentext").val(tsval);
                    myTable.column(2).search(tsval, false, false).draw();
                });
            }
            return tableName;
        },
        reloadData: function (dataList, table) {
            var currentPage = table.page();
            table.clear();
            table.rows.add(dataList);
            table.page(currentPage).draw(false);
            // myTable.column(2).search('', false, false).page(currentPage).draw();
        },
        windowResize: function () {
            mileageReport.MileageGraphics(mileList, milcName);
        },
        toHHMMSS: function (data) {
            var totalSeconds = data * 60 * 60;
            var hour = Math.floor(totalSeconds / 60 / 60);
            var minute = Math.floor(totalSeconds / 60 % 60);
            var second = Math.floor(totalSeconds % 60);
            return hour + "小时" + minute + "分钟" + second + "秒"
        },
        exportMileage: function () {
            var curTab = $('.nav-tabs li.active').attr('id');
            var curTable = $('.tab-pane.active').find('td.dataTables_empty');
            if (curTable.length == 1) {
                layer.msg(notDataExport, {move: false});
                return;
            }
            var curType = Number(curTab.substr(-1, 1)) + 1;// 2:行驶统计;3:行驶明细;4:位置明细
            switch (curType + '') {
                case '2':
                    if(getRecordsNum('dataTableOne_info') > 60000){
                        return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
                    }
                    break;
                case '3':
                    if(getRecordsNum('dataTableTwo_info') > 60000){
                        return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
                    }
                    break;
                default :
                    if(getRecordsNum('dataTableThree_info') > 60000){
                        return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
                    }
            }
            var timeInterval = $('#timeInterval').val().split('--');
            var st = timeInterval[0];
            var et = timeInterval[1];
            var hiddentext = $("#hiddentext").val();
            var param = {
                "startTime": curType != 4 ? st : starTimeStr,
                "endTime": curType != 4 ? et : endTimeStr,
                "monitorId": curType == 2 ? allVid : curVehicleId,
                "exportType": curType,
                "queryParam": hiddentext
            };
            var url = "/clbs/m/reportManagement/mileageReport/exportDrivingMileage";
            exportExcelUsePost(url,param)
            // var url = "/clbs/m/reportManagement/mileageReport/exportDrivingMileage?monitorId=" + param.monitorId + "&startTime=" + param.startTime + "&endTime=" + param.endTime + "&exportType=" + param.exportType + "&queryParam=" + param.queryParam;
            // window.location.href = url;
        },
        CallBackExport: function (data, param, curType) {
            if (data == true) {
                var url = "";
                if (curType == 2) {
                    url = "/clbs/m/reportManagement/mileageReport/export?allVid=&startTime=&endTime=&exportType=" + curType + "&fuzzyQuery=" + param.fuzzyQuery;
                } else {
                    url = "/clbs/m/reportManagement/mileageReport/export?allVid=" + param.allVid + "&startTime=" + param.startTime + "&endTime=" + param.endTime + "&exportType=" + param.exportType + "&fuzzyQuery=" + param.fuzzyQuery;
                }
                window.location.href = url;
            }
            else {
                layer.msg(exportFail, {move: false});
                return;
            }
        },
        //刷新列表
        refreshTable: function () {
            mileageReport.inquireClick(1, false);
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
        getToday: function () {
            var nowDate = new Date();
            var date = new Date(nowDate.getTime());
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
        //解析位置信息
        getDrivingAddress: function (event, latitude, longitude) {
            var url = '/clbs/v/monitoring/address';
            var param = {addressReverse: [latitude, longitude, '', "", 'vehicle']};
            var _this = $(event);
            $.ajax({
                type: "POST",//通常会用到两种：GET,POST。默认是：GET
                url: url,//(默认: 当前页地址) 发送请求的地址
                dataType: "json", //预期服务器返回的数据类型。"json"
                async: true, // 异步同步，true  false
                data: param,
                traditional: true,
                timeout: 8000, //超时时间设置，单位毫秒
                success: function (data) {//请求成功
                    _this.closest('td').html(data == '[]' ? '未定位' : data);
                },
            });
        },
        //跳转行驶明细
        goDrivingDetail: function (tabName, panelName, vId, newStratTime, newEndTime) {
            $(tabName).addClass('active').siblings().removeClass('active');
            $(panelName).addClass('active').siblings().removeClass('active');

            curVehicleId = vId;
            if (newStratTime) {
                starTimeStr = newStratTime.replace('|', ' ');
                endTimeStr = newEndTime.replace('|', ' ');
            }
            var url = '/clbs/m/reportManagement/mileageReport/getDrivingMileageDetails';// 行驶明细
            var flag = 0;
            if (tabName == '#panelTab3') {//位置明细
                url = '/clbs/m/reportManagement/mileageReport/getDrivingMileageLocationDetails';
                flag = 1;
            }
            if (!vId) return;
            var param = {
                monitorId: vId,
                startTime: flag == 0 ? startime : newStratTime.replace('|', ' '),
                endTime: flag == 0 ? endtime : newEndTime.replace('|', ' '),
            };
            json_ajax("post", url, "json", false, param, function (data) {
                if (data.success) {
                    if (flag == 0) {// 行驶明细
                        var mileageList = [];
                        mileage = data.obj ? data.obj : [];
                        for (var i = 0; i < mileage.length; i++) {
                            var list =
                                [
                                    i + 1,
                                    '<button type="button" class="editBtn editBtn-info" onclick=mileageReport.goDrivingDetail("#panelTab3","#panel3",\"' + vId + '\",\"' + mileage[i].startDateTime.replace(' ', '|') + '\",\"' + mileage[i].endDateTime.replace(' ', '|') + '\")>位置明细</button>',
                                    mileage[i].monitorName,//监控对象
                                    mileage[i].startDateTime,//开始时间
                                    mileage[i].endDateTime,//结束时间
                                    mileage[i].duration,//行驶时长
                                    mileage[i].totalMile,//行驶里程
                                    mileage[i].accumulatedMile,//总里程
                                    mileage[i].totalOilWear,//油耗
                                    mileage[i].startAddress,//开始位置
                                    mileage[i].endAddress//结束位置
                                ];
                            mileageList.push(list);
                        }
                        mileageReport.reloadData(mileageList, myTableTwo);
                    } else {//位置明细
                        var mileageList = [];
                        mileage = data.obj ? data.obj : [];
                        for (var i = 0; i < mileage.length; i++) {
                            var list =
                                [
                                    i + 1,
                                    mileage[i].monitorName,//监控对象
                                    mileage[i].timeStr,//定位时间
                                    mileage[i].accStatusStr,//acc
                                    // mileage[i].speed,//速度
                                    mileage[i].totalOilWearOne,//油量
                                    '<a onclick="mileageReport.getDrivingAddress(this,' + mileage[i].latitude + ',' + mileage[i].longitude + ')">点击获取位置信息</a>'//经纬度位置
                                    // mileage[i].address//位置
                                ];
                            mileageList.push(list);
                        }
                        mileageReport.reloadData(mileageList, myTableThree);
                    }
                } else {
                    layer.msg(data.msg);
                }
            });
        }
    }
    $(function () {														//初始化页面
        $('input').inputClear();
        mileageReport.init();
        $('#timeInterval').dateRangePicker({
            dateLimit: 31,
            isOffLineReportFlag: true,
            nowDate: mileageReport.getToday(),
            isShowHMS: false,
            greater: true,
        });
        $('#simpleQueryParame').on('keydown',function (e) {
            if(e.keyCode == 13){
                $('#search_button').click()
            }
        })
        mileageReport.tableFilter();
        myTable = mileageReport.getTable('#dataTableOne', 'myTable');
        myTableTwo = mileageReport.getTable('#dataTableTwo', 'myTableTwo');
        myTableThree = mileageReport.getTable('#dataTableThree', 'myTableThree');
        mileageReport.getsTheCurrentTime();													//当前时间
        $("#groupSelect").bind("click", showMenuContent);			//组织下拉显示
        $("#exportMileage").bind("click", mileageReport.exportMileage);
        $(window).resize(mileageReport.windowResize);										//监听窗口变化 重新加载图表
        $("#toggle-left").bind("click", function () {										//左侧菜单切换重新绘制图表
            setTimeout(function () {
                onlineReport.MileageGraphics();
            }, 500)
        });
        $("#refreshTable").bind("click", mileageReport.refreshTable);

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                inputFlag = true;
                var param = $("#groupSelect").val();
                mileageReport.searchVehicleTree(param);
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
            ;
            inputChange = setTimeout(function () {
                if (searchFlag) {
                    var param = $("#groupSelect").val();
                    mileageReport.searchVehicleTree(param);
                }
                searchFlag = true;
            }, 500);
        });
        // 点击行驶统计下的列表项查看明细列表
        $("#dataTableOne tbody").on('click', 'tr', function () {
            if (!$(this).hasClass('addTr') && !$(this).hasClass('newTr')) {
                mileageReport.showGroupDetail(this);
            }
        });
        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            mileageReport.searchVehicleTree(param);
        });
    })
}(window, $))