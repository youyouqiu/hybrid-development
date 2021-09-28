(function (window, $) {
    //车辆id列表
    var vehicleIds = "";
    //开始时间
    var startTime;
    //结束时间
    var endTime;
    var checkFlag = false; //判断组织节点是否是勾选操作
    var size;//当前权限监控对象数量

    var prevOrnext; //wjk 定义点击上一页还是下一页
    var pageSearchAfterArr = [];
    var prevSearchAfter = '';
    var nextSearchAfter = '';

    //模糊查询
    var zTreeIdJson = {};
    var bflag = true;
    var crrentSubV = [];
    var ifAllCheck = true;
    var searchFlag = true;

    //多媒体
    var mediaIndex = 0;
    var title = [],
        mediaUrl = [],
        mediaType,
        mediaId;

    //组合复选下拉框
    var vehicleArr = [],
        riskTypeValue = '';

    //查询
    var riskNumber="",
        riskLevel="",
        brand = "",
        driver = "",
        status = "",
        dealUser = "",
        visitTime = "",
        riskResult = "";

    var advanceFlag=false;
    //车id
    var vehicleId="";
    var untreated="";

    var multimediaFlag="";
    //实时监控跳转参数
    var statusUrl = "",
        brandUrl = "",
        vehicleidUrl = "";



    riskDisposeRecord = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" disabled data-column=\"" + parseInt(1) + "\"/>" + table[0].innerHTML + "</label></li>";
            // menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\"  data-column=\"" + parseInt(3) + "\"/>" + table[1].innerHTML + "</label></li>";
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }

            $("#Ul-menu-text").html(menu_text);

            riskDisposeRecord.initTree();
            riskDisposeRecord.alarmTypeTreeInit();
        },
        // 组织树
        initTree: function () {
            //车辆树
            var setting = {
                async: {
                    url: riskDisposeRecord.getRiskDisposeTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: riskDisposeRecord.ajaxDataFilter
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
                    beforeClick: riskDisposeRecord.beforeClickVehicle,
                    onAsyncSuccess: riskDisposeRecord.zTreeOnAsyncSuccess,
                    beforeCheck: riskDisposeRecord.zTreeBeforeCheck,
                    onCheck: riskDisposeRecord.onCheckVehicle,
                    onExpand: riskDisposeRecord.zTreeOnExpand,
                    onNodeCreated: riskDisposeRecord.zTreeOnNodeCreated,
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
                riskDisposeRecord.initTree();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/m/functionconfig/fence/bindfence/vehicleTreeFuzzy",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType').val(), "queryParam": param, "queryType": $('#queryType').val()},
                        dataFilter: riskDisposeRecord.ajaxQueryDataFilter
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
                        beforeClick: riskDisposeRecord.beforeClickVehicle,
                        onCheck: riskDisposeRecord.onCheckVehicle,
                        onExpand: riskDisposeRecord.zTreeOnExpand,
                        onNodeCreated: riskDisposeRecord.zTreeOnNodeCreated,
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
            for (var i=0;i<nodesArr.length;i++){
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        // 风险类型树
        alarmTypeTreeInit: function () {
            var comChildren = [
                {
                    name: "疑似疲劳",
                    value: '1',
                },
                {
                    name: "注意力分散",
                    value: '2',
                },
                {
                    name: "违规异常",
                    value: '3',
                },
                {
                    name: "碰撞危险",
                    value: '4',
                },
                {
                    name:"激烈驾驶",
                    value:'6',
                }
            ];
            var zNodes = [
                {
                    name: "所有",
                    value: null,
                    open: true,
                    children: comChildren
                }
            ];
            var alarmTypeSetting = {
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
                    countClass: "group-number-statistics",
                    showIcon: false
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    onCheck: riskDisposeRecord.onCheckChangeValue
                }
            };

            $.fn.zTree.init($("#alarmTypeTree"), alarmTypeSetting, zNodes);
        },
        getRiskDisposeTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
        },
        testWebSocket: function () {
            webSocket.subscribe(headers, "/user/topic/securityRiskRingBell", function (data) {
                alert("报警推送过来了" + data);
            }, "/app/risk/security/subscribeRisk", "a");
        },
        distestWebSocket: function () {
            webSocket.send("/app/risk/security/unsubscribeRisk", headers, "");
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
            }
            return data;
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (size <= 5000 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            riskDisposeRecord.getCharSelect(treeObj);
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
                searchFlag = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    riskDisposeRecord.getCheckedNodes();
                    riskDisposeRecord.validates();
                }, 600);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            riskDisposeRecord.getCharSelect(zTree);
            riskDisposeRecord.getCheckedNodes();
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
        zTreeOnExpand: function (event, treeId, treeNode) {
            //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if (treeNode.type == "group" && !checkFlag) {
                return;
            }
            //初始化勾选操作判断表示
            checkFlag = false;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (treeNode.type == "group") {
                var url = "/clbs/m/functionconfig/fence/bindfence/getOrgAssignmentVehicle";
                json_ajax("post", url, "json", false, {
                    "groupId": treeNode.id,
                    "isChecked": treeNode.checked,
                    "monitorType": "0"
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
                                    parentNode.zAsync = true;
                                    treeObj.addNodes(parentNode, 0, chNodes);
                                }
                            }
                        });
                    }
                })
            }
        },
        getCharSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            var groupSelect = new Array();
            if (nodes.length > 0) {
                for (var i = 0; i < nodes.length; i++) {
                    if (nodes[i].type == "assignment") {
                        var n = 0
                        var l = groupSelect.length;
                        if (groupSelect.length > 0) {
                            for (n; n < groupSelect.length; n++) {
                                if (nodes[i].pName == groupSelect[n]) {
                                    break;
                                }
                            }
                            if (n == groupSelect.length) {
                                groupSelect[l] = nodes[i].pName;
                            }
                        } else {
                            groupSelect[0] = nodes[i].pName;
                        }
                    }

                    if (nodes[i].type == "group") {
                        var n = 0
                        var l = groupSelect.length;
                        if (groupSelect.length > 0) {
                            for (n; n < groupSelect.length; n++) {
                                if (nodes[i].name == groupSelect[n]) {
                                    break;
                                }
                            }
                            if (n == groupSelect.length) {
                                groupSelect[l] = nodes[i].name;
                            }
                        } else {
                            groupSelect[0] = nodes[i].name;
                        }
                    }
                    if (nodes[i].type == "vehicle") {
                        var n = 0
                        var l = groupSelect.length;
                        if (groupSelect.length > 0) {
                            for (n; n < groupSelect.length; n++) {
                                if (nodes[i].getParentNode().getParentNode().name == groupSelect[n]) {
                                    break;
                                }
                            }
                            if (n == groupSelect.length) {
                                groupSelect[l] = nodes[i].getParentNode().getParentNode().name;
                            }
                        } else {
                            groupSelect[0] = nodes[i].getParentNode().getParentNode().name;
                        }
                    }
                }
            }
            var h = '';
            if (groupSelect.length > 0) {
                for (var a = 0; a < groupSelect.length; a++) {
                    if (a == groupSelect.length - 1) {
                        h += groupSelect[a];
                    } else {
                        h += groupSelect[a] + ",";
                    }
                }
            }
            $("#groupSelect").val(h);
        },
        getGroupChild: function (node, assign) { // 递归获取组织及下级组织的分组节点
            var nodes = node.children;
            if (nodes != null && nodes != undefined && nodes.length > 0) {
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.type == "assignment") {
                        assign.push(node);
                    } else if (node.type == "group" && node.children != undefined) {
                        riskDisposeRecord.getGroupChild(node.children, assign);
                    }
                }
            }
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                nodes = zTree.getCheckedNodes(true),
                vid = "";
            for (var i = 0, len = nodes.length; i < len; i++) {
                if (nodes[i].type == "vehicle") {
                    vid += nodes[i].id + ",";
                }
            }
            vehicleIds = vid;
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
                tMonth = riskDisposeRecord.doHandleMonth(tMonth + 1);
                tDate = riskDisposeRecord.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " "
                    + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = riskDisposeRecord.doHandleMonth(endMonth + 1);
                endDate = riskDisposeRecord.doHandleMonth(endDate);
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
                vMonth = riskDisposeRecord.doHandleMonth(vMonth + 1);
                vDate = riskDisposeRecord.doHandleMonth(vDate);
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
                    vendMonth = riskDisposeRecord.doHandleMonth(vendMonth + 1);
                    vendDate = riskDisposeRecord.doHandleMonth(vendDate);
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
            var atime = $("#atime").val();
            if (atime != undefined && atime != "") {
                startTime = atime;
            }
        },
        //查询
        inquireClick: function (number, isFirst) {
            if (number == 0) {
                riskDisposeRecord.getsTheCurrentTime();
            } else if (number == -1) {
                riskDisposeRecord.startDay(-1)
            } else if (number == -3) {
                riskDisposeRecord.startDay(-3)
            } else if (number == -7) {
                riskDisposeRecord.startDay(-7)
            }
            if (number != 1) {
                $('#timeInterval').val(startTime + '--' + endTime);
                startTime = startTime;
                endTime = endTime;
            } else {
                var timeInterval = $('#timeInterval').val().split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
            }

            // wjk 开始时间大于结束时间
            if (startTime > endTime) {
                layer.msg('结束日期必须大于开始日期', {move: false});
                return;
            }
            riskDisposeRecord.getCheckedNodes();
            if (!riskDisposeRecord.validates()) {
                return;
            }
            riskDisposeRecord.showRiskRecordTable();
            riskDisposeRecord.hideEventInfoWindow();
        },
        /**
         * 风险处理
         */
        showRiskRecordTable: function () {
            $('.toggle-vis').prop('checked', 'true');
            //表格列定义
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
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    untreated = row.status;
                    var result="";
                    result += '<input value=' + row.id + ' type="hidden" name="disabled" id="subChk"/>';
                    if (row.status == '未处理' || row.status == null) {
                         result += '<a href="javascript:void(0)" onclick="riskDisposeRecord.riskDeal(event,\'' + data.id + '\')">未处理</a>';
                    }else{
                         result += '已处理'
                    }
                    return result;
                }
            }, {
                "data":"riskResult",
                "class":"text-center",
                render: function (data) {
                    if(untreated == "未处理"){
                        return "<div id='edit'></div>"
                    }else if(data == 0){
                        return "事故未发生"
                    }else if(data == 1){
                        return "事故已发生"
                    }
                }
            },{
                    "data": "null",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        var riskType = row.riskType;
                        var riskId = row.id;
                        var result = '';
                        if (row.hasPic) {
                            result += '<div onclick="riskDisposeRecord.getMedia(\'' + riskId + '\',0,event, false, \'' + riskType + '\')" class="media' +
                                ' risk_img" id="risk_img"></div>';
                        }else{
                            result +='<div class="media risk_grey" id="risk_img"></div>';
                        }

                        if (row.hasVideo) {
                            result += '<div onclick="riskDisposeRecord.getMedia(\'' + riskId + '\',2,event, false, \'' + riskType + '\')" class="media' +
                                ' risk_video" id="risk_video"></div>';
                        }else{
                            result +='<div class="media risk_blue" id="risk_img"></div>';
                        }

                        return result;
                    }
                }, {
                "data": "brand",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return '<input type="hidden" class="riskInput" data-riskid="' + row.id + '"  data-vehicleId = "'+ row.vehicleId+'"/>' + data;
                }
            }, {
                "data": "riskNumber",
                "class": "text-center"
            }, {
                "data": "driver",
                "class": "text-center"
            }, {
               "data": "driverNo",
               "class": "text-center",
                render:function (data, type, row, meta){
                    if(row.driverNo == "" || row.driverNo == null){
                        return '-';
                    }
                        return row.driverNo;
                    
                }
            }, {
                "data": "groupName",
                "class": "text-center"
            }, {
                "data": "riskType",
                "class": "text-center",
            }, {
                "data": "riskLevel",
                "class": "text-center",
                // render: function (data, type, row, meta) {
                //     if (!data) {
                //         return '';
                //     }
                //     var d = Number(data);
                //     switch (d) {
                //         case 1:
                //             return '一般(低)';
                //         case 2:
                //             return '一般(中)';
                //         case 3:
                //             return '一般(高)';
                //         case 4:
                //             return '较重(低)';
                //         case 5:
                //             return '较重(中)';
                //         case 6:
                //             return '较重(高)';
                //         case 7:
                //             return '严重(低)';
                //         case 8:
                //             return '严重(中)';
                //         case 9:
                //             return '严重(高)';
                //         case 10:
                //             return '特重(低)';
                //         case 11:
                //             return '特重(中)';
                //         case 12:
                //             return '特重(高)';
                //         default:
                //             return '';
                //     }
                //}
            }, {
                "data": "speed",
                "class": "text-center",
                render:function(result) {
                    return result + "km/h"
                }
            }, {
                "data": "formattedAddress",
                "class": "text-center",
            }, {
                "data": "weather",
                "class": "text-center",
            }, {
                "data": "warTime",
                "class": "text-center",
            }, {
               "data": "overTime",
               "class": "text-center"
            }, {
                "data": "dealUser",
                "class": "text-center",
            }, {
                "data": "dealTime",
                "class": "text-center",
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    var downLoadId = row.id;
                    var riskNumber = row.riskNumber;
                    var result="";
                    if(row.hasMedia){
                        result +='<a href="javascript:void(0)" id="riskDown" class="editBtn btn-primary"' +
                            ' style="padding-left: 12px!important;"' +
                            ' onclick="riskDisposeRecord.annexDownLoad(\'' + downLoadId + '\',\'' + riskNumber+ '\',event)"' +
                            ' >附件下载</a>';
                    }else{
                        result += '<a href="javascript:void(0)" id="riskDown" onclick="event.stopPropagation()"' +
                            ' class="editBtn' +
                            ' btn-default">附件下载</a>';
                    }
                    return result;
                /*    var result = '<a href="javascript:void(0)" id="riskDown" class="editBtn btn-default"
                 risknumber="' + data.riskNumber + '" terminalEvidence_flag ="' + downLoadId + '" style="padding-left: 12px!important;" onclick="event.stopPropagation()" >附件下载</a>';
                    json_ajax('GET', "/clbs/r/riskManagement/disposeReport/hasTerminalEvidence", 'json', true, {
                        "downLoadId": downLoadId,
                        "isEvent": false
                    }, riskDisposeRecord.checkRiskEvidenceCallBack);
                    return result;*/
                }
            }];

            //ajax参数
            var ajaxDataParamFun = function (d) {
                // wjk 传参给后台
                if (prevOrnext && d.start == 0) { //点击首页
                    prevSearchAfter = '';
                    prevSearchAfter = '';
                    pageSearchAfterArr = [];
                    d.searchAfter = '';
                    prevOrnext = 0;
                }
                else if (!prevOrnext && prevOrnext != '0') { // prevOrnext没有值的时候是点击下一页
                    prevOrnext = d.start;
                } else {
                    if (prevOrnext < d.start) { //点击下一页
                        d.searchAfter = nextSearchAfter;
                        prevOrnext = d.start;
                    } else if (prevOrnext > d.start) { //点击上一页

                        var arr = pageSearchAfterArr;
                        arr.pop();
                        pageSearchAfterArr = arr;
                        if (pageSearchAfterArr.length) {
                            prevSearchAfter = pageSearchAfterArr[pageSearchAfterArr.length - 2]
                            nextSearchAfter = pageSearchAfterArr[pageSearchAfterArr.length - 1]
                        } else {
                            prevSearchAfter = ''
                        }

                        d.searchAfter = prevSearchAfter;

                        prevOrnext = d.start;
                    } else {//刷新当前页
                        d.searchAfter = pageSearchAfterArr[pageSearchAfterArr.length - 2];
                    }
                }

                d.vehicleIds = vehicleIds;
                d.startTime = startTime;
                d.endTime = endTime;
                advanceFlag=false;
                // if ($("#highsearch").css('display') == "block") {
                    advanceFlag=true;
                    riskNumber=$("#riskNumber").val();
                    riskLevel=$('#riskLevel').val();
                    brand = $("#brand").val();
                    driver = $("#driver").val();
                    status = $('#status').val();
                    dealUser = $("#dealUser").val();
                   // visitTime = $("#visitTime option:selected").text();
                   // riskResult = $("#riskResult option:selected").text();
                    d.riskNumber = riskNumber; //模糊查询
                    d.riskType = riskTypeValue;
                    d.riskLevel = riskLevel;
                    d.brand = brand;
                    d.driver = driver;
                  //  d.status = $('#status option:selected').text();
                    d.status = status;
                    d.dealUser = dealUser;
                    d.visitTime = visitTime;
                    d.riskResult = riskResult;
                // }
            };
            //表格setting
            var setting = {
                listUrl: "/clbs/r/riskManagement/disposeReport/list",
                enableUrl: "/clbs/c/user/enable_",
                disableUrl: "/clbs/c/user/disable_",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                getAddress: true,
                address_index: 12,
                sync_address: true,
                ajaxCallBack: riskDisposeRecord.tableCallBack,
                lengthChange: false //不允许用户改变表格每页显示的记录数
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();

            $('#exportRisk').prop('disabled',false);

            $('.toggle-vis').off().on('change', function (e) {
                var visible = myTable.dataTable.column($(this).attr('data-column')).visible();
                if (visible) {
                    myTable.dataTable.column($(this).attr('data-column')).visible(false);
                } else {
                    myTable.dataTable.column($(this).attr('data-column')).visible(true);
                }
                $(".keep-open").addClass("open");
            });
            //数据表格单击TR
            $("#dataTable tbody").on("click", "tr", function (event) {
                vehicleId = $(this).find('.riskInput').data('vehicleid');
                if ($(this).hasClass("active-tablebg")) {
                    $("#riskTypePop").removeClass("in show");
                    $(this).removeClass("active-tablebg");
                } else {
                   $(this).siblings(".odd").find("td").css('background-color', "#f9f9f9");
                   $(this).siblings(".even").find("td").css('background-color', "#fff");
                   $("#dataTable tbody").find("tr").removeClass("active-tablebg");
                   $(this).find("td").css('background-color', "#e4e4e4");
                   $(this).addClass("active-tablebg");
                   var riskId = $(this).find('.riskInput').data('riskid');
                   json_ajax('POST', "/clbs/r/riskManagement/disposeReport/eventList", 'json', false, {"riskId":
                    riskId,"vehicleId":vehicleId}, riskDisposeRecord.initEventTable);

                   var height = $("#riskTypePop").height();
                    $(".popover").css({
                        "max-width": "1800px",
                        "min-width":"1620px",
                        "top": ($(this).position().top - height) + "px",
                        "left": '50%',
                        'transform': 'translateX(-50%)'
                    });
                    $("#riskTypePop").addClass("in show");
                }
            });
        },
        //事件附件下载
        eventDownLoad:function (riskId,riskNumber){
          var paramer = {
              'downLoadId':riskId,
              'isEvent':true,
              'number':riskNumber
          };
          json_ajax('GET',"/clbs/r/riskManagement/disposeReport/terminalEvidence",'json',true,paramer, function(data){
            if(data.success){
                if(data.obj.hasNotFile){
                    layer.msg(data.obj.msg);
                }else{
                    window.location.href = "/clbs/r/riskManagement/disposeReport/downloadFile?filePath=" + data.obj.filePath + "&fileName=" + data.obj.fileName + "&isRiskEvidence=" + data.obj.isRiskEvidence;
                }
            }
          });
        },

        //获取附件事件
        eventGetdata:function (e,vehicleId,riskEventId){
            var paramer = {
                "vehicleId":vehicleId,
                'riskEventId':riskEventId
            };
            json_ajax("POST","/clbs/v/oilmassmgt/oilcalibration/checkVehicleOnlineStatus","json",true,paramer,function(data){
                if(!data.success){
                    layer.msg("终端离线")
                }else{
                    json_ajax('POST',"/clbs/adas/v/monitoring/getAdasMedia",'json',true,paramer, function(result){
                        if(result.success){
                            layer.msg("下发成功");
                            $(e).text("获取中···");
                            $(e).removeClass("eventTerminalEvidence btn-primary").addClass("btn-default btn-defaultr");
                            $(e).attr("disabled",true).css("pointer-events","none");
                        }
                    });
                }
            });
        },
        //附件下载
        annexDownLoad: function (riskId,riskNumber, e) {
            e.stopPropagation()
            json_ajax('GET', "/clbs/r/riskManagement/disposeReport/terminalEvidence", 'json', true, {
                "downLoadId": riskId,
                "isEvent":false,
                "number":riskNumber

            },riskDisposeRecord.downLoadCallBack);
        },
        //多媒体
        getMedia: function (riskId, type, e,flag) {
            e.stopPropagation();
            multimediaFlag = flag;
            mediaType = type;
            mediaId = riskId;
            var mediaContent = $('.media-content');
            type == 0 ? mediaContent.removeClass('video_show') : mediaContent.addClass('video_show');
             if(flag){
                 riskDisposeRecord.getEventMediaAjax();
             }else {
                 riskDisposeRecord.getMediaAjax();
             }
        },
        //事件预览方法
        getEventMediaAjax: function () {
            json_ajax('POST', "/clbs/r/riskManagement/disposeReport/getEventMedia", 'json', true, {
                "eventId": mediaId,
                "mediaType": mediaType
            }, function (datas) {
                var data = datas.obj;
                //重置
                mediaIndex = 0;
                title = [];
                mediaUrl = [];
                if (datas.success) {
                    for (var i = 0, len = data.length; i < len; i++) {
                        var item = data[i];
                        title.push(item.riskType + '--' + item.riskEventType);
                        mediaUrl.push(item.mediaUrl);
                    }
                } else {
                    layer.msg('获取数据失败');
                }

                if(mediaType == 0){
                    if(mediaUrl.length == 0){
                        $("#img").attr("disabled",true);
                        $("#video").attr("disabled",false);
                        return;
                    }
                        $("#img").attr("disabled",false);
                        $("#video").attr("disabled",false);
                    
                }else if(mediaType == 2) {
                    if (mediaUrl.length == 0) {
                        $("#video").attr("disabled", true);
                        $("#img").attr("disabled",false);
                        return;
                    }
                        $("#video").attr("disabled", false);
                        $("#img").attr("disabled",false);
                    
                }

                // if (mediaUrl.length == 0) {
                //     layer.msg('暂无媒体资源');
                //     return;
                // }
                $('#myModal').modal('show');
                riskDisposeRecord.getMediaDom();
            });
        },
        //多媒体预览方法
        getMediaAjax: function () {
            json_ajax('POST', "/clbs/r/riskManagement/disposeReport/getRiskMedia", 'json', true, {
                "riskId": mediaId,
                "mediaType": mediaType
            }, function (datas) {
                var data = datas.obj;
                //重置
                mediaIndex = 0;
                title = [];
                mediaUrl = [];
                if (datas.success) {
                    for (var i = 0, len = data.length; i < len; i++) {
                        var item = data[i];
                        title.push(item.riskType +' -- '+ item.riskEventType);
                        mediaUrl.push(item.mediaUrl);
                    }
                } else {
                    layer.msg('获取数据失败');
                }

                if(mediaType == 0){
                    if(mediaUrl.length == 0){
                        $("#img").attr("disabled",true);
                        $("#video").attr("disabled",false);
                        return;
                    }
                        $("#img").attr("disabled",false);
                        $("#video").attr("disabled",false);
                    
                }else if(mediaType == 2) {
                    if (mediaUrl.length == 0) {
                        $("#video").attr("disabled", true);
                        $("#img").attr("disabled",false);
                        return;
                    }
                        $("#video").attr("disabled", false);
                        $("#img").attr("disabled",false);
                    
                }
                // if (mediaUrl.length == 0) {
                //     layer.msg('暂无媒体资源');
                //     return;
                // }
                $('#myModal').modal('show');
                riskDisposeRecord.getMediaDom();
            });
        },
        getMediaDom: function () {
            var html = '',
                src = mediaUrl[mediaIndex];
            if(mediaType == 0){
                $(".Video button").removeClass("btn-primary").addClass("btn-default");
                $(".Img button").addClass("btn-primary").removeClass("btn-default");
            }
            if(mediaType == 2){
                $(".Img button").removeClass("btn-primary").addClass("btn-default");
                $(".Video button").addClass("btn-primary").removeClass("btn-default");
            }

            if (mediaType == 2) {//视频
                if (window.navigator.userAgent.toLowerCase().indexOf('ie') >= 0) {
                    html += '<embed id="videoDom" src="' + src + '" autostart=false style="height:100%;"></embed>';
                } else {
                    html = '<video id="videoDom" src="' + src + '" controls="controls" controlsList="nodownload" style="height:100%;"></video>'
                }
             } else if (mediaType == 0) {//图片
                 html = '<img id="imgDom" src="' + src + '" style="height:100%"/>';
             }

            $('#media').html(html);
            riskDisposeRecord.getMediaMsg(true);
        },
        getMediaMsg: function (flag) {
            if(flag){
                $('#myModalLabel').text(title[0]);
            }
            $('#current').text(mediaIndex + 1);
            $('#count').text(mediaUrl.length);
        },
        videoChange: function () {
            var self = $(this);
            mediaType = self.data('value');

            // if (self.hasClass('active')) {
            //     return;
            // }
            //
            // self.addClass('active').siblings('.btn').removeClass('active');
            if(multimediaFlag){
                riskDisposeRecord.getEventMediaAjax();
            }else{
                riskDisposeRecord.getMediaAjax();
            }

        },
        mediaChange: function () {
            var id = $(this).attr('id');
            var len = mediaUrl.length == 0 ? 0 : mediaUrl.length - 1;

            if (id == 'arrowsLeft') {
                if (mediaIndex == 0) {
                    mediaIndex = 0;
                    layer.msg('已经到头了');
                    return;
                }
                mediaIndex--;
            } else if (id == 'arrowsRight') {
                if (mediaIndex == len) {
                    mediaIndex = len;
                    layer.msg('已经到头了');
                    return;
                }
                mediaIndex++;
            }
            $('#myModalLabel').html(title[mediaIndex]);
            if (mediaType == 2) {
                $('#videoDom').attr('src', mediaUrl[mediaIndex]);
            } else if (mediaType == 0) {
                $('#imgDom').attr('src', mediaUrl[mediaIndex]);
            }
            riskDisposeRecord.getMediaMsg();
        },
        downloadMedia: function () {
            var url = mediaUrl[mediaIndex];
            if (!url) {
                return;
            }
            $.post('/clbs/r/riskManagement/disposeReport/canDownload', {
                "mediaUrl": url,
            }, function (data) {
                if (data.success) {
                    window.location.href = url;
                } else {
                    if(data.msg){
                        layer.msg(data.msg);
                    }
                }
            }, 'json');
            return false;
        },


        //
        riskDeal: function (event, riskId) {
            event.stopPropagation();
            // var id = $(this).parents('tr').find('.riskInput').data('riskid');
            //关闭事件弹窗
            $("#riskTypePop").removeClass("in show");
            $("#dataTable tbody .odd").find("td").css('background-color', "#f9f9f9");
            $("#dataTable tbody .even").find("td").css('background-color', "#fff");
            $("#dataTable tbody").find("tr").removeClass("active-tablebg");

            var platformListUrl = "/clbs/r/riskManagement/disposeReport/dealRisk";
            layer.confirm("<label>确认处理此条报警信息？</label>" +
                "    <div>" +
                "      <label class='radioLabel'><input type='radio' name='accident' value='0' checked>事故未发生</label>" +
                "      <label class='radioLabel' style='margin-left: 20px'><input type='radio' name='accident' value='1'>事故已发生</label>" +
                "    </div>", {btn: ["确定", "取消"]}, function () {
                var getRadioval=$("input[name='accident']:checked").val();
                var param = {
                    status: 6,
                    riskId: riskId,
                    riskResult:getRadioval
                };
                json_ajax("POST", platformListUrl, "json", true, param, riskDisposeRecord.riskDealCallBack);
            });
            return false;
        },
        riskDealCallBack: function (data) {
            if (data.success == true) {
                // layer.msg('处理成功!');
                var getRadioval=$("input[name='accident']:checked").val();
                if(getRadioval == 0){
                    $("#edit").text("事故未发生")
                }else if(getRadioval == 1){
                    $("#edit").text("事故已发生")
                }
                myTable.refresh();
                layer.closeAll();
            } else {
                if(data.msg){
                    layer.msg(data.msg);
                }
                return;
            }
        },
        initEventTable: function (data) {
            var event = data.obj;
            var tableData = [];
            for (var i = 0; i < event.length; i++) {
                var item = event[i];
                var riskId=item.eventId;
                var riskEventId=item.eventId;
                var attachmentStatus=item.attachmentStatus;
                var preview="";
                var monitoring = item.brand + '('+item.plateColor+')';
                //车辆状态
                var vehicleStatus = '<a href="javascript:void(0)" data-id="'+ item.riskEvent +','+ monitoring+','+ item.vehicleStatus +'" class="stateInfoTd">状态信息</a>';

                //图片视频按钮弹框
                if(item.hasPic){
                    preview +='<div onclick="riskDisposeRecord.getMedia(\'' + riskId + '\',0, event,true)" class="media' +
                        ' risk_img" id="risk_img"></div>';
                }else{
                    preview +='<div class="media risk_grey" id="risk_img"></div>';
                }
                if(item.hasVideo){
                    preview += '<div onclick="riskDisposeRecord.getMedia(\'' + riskId + '\',2,event,true)" class="media' +
                        ' risk_video" id="risk_video"></div>';
                }else{
                    preview +='<div class="media risk_blue" id="risk_img"></div>';
                }

                //多媒体附件下载
                var eventDownLoad = '<a href="javascript:void(0)" style="margin-right:10px;" class="editBtn eventTerminalEvidence btn-primary"' +
                    ' onclick="riskDisposeRecord.eventDownLoad(\'' + item.eventId + '\',\'' + item.eventNumber + '\')">附件下载</a>';
                if(!item.hasMedia){
                    eventDownLoad = '<a href="javascript:void(0)" style="margin-right:10px" class="editBtn btn-default" onclick="event.stopPropagation()">附件下载</a>'
                }

                if(attachmentStatus == 0){
                     var eventGetdata = '<a href="javascript:void(0)" class="editBtn eventTerminalEvidence btn-primary"' +
                          ' onclick="riskDisposeRecord.eventGetdata(this,\'' + vehicleId + '\',\'' + riskEventId + '\')">获取附件</a>';
                }else if(attachmentStatus == 1){
                    var eventGetdata = '<a href="javascript:void(0)" class="editBtn btn-default" onclick="event.stopPropagation()">附件失效</a>'
                }else if(attachmentStatus == 2){
                    var eventGetdata = '<a href="javascript:void(0)" class="editBtn btn-default btn-defaultr" onclick="event.stopPropagation()">获取中···</a>'
                }

                //道路类型
                var roadType = item.roadType;
                var roadTypeStr;
                if(roadType == null || roadType == ""){
                    roadTypeStr = "-";
                }else{
                    switch (roadType){
                        case 1:
                            roadTypeStr = '高速路';
                            break;
                        case 2:
                            roadTypeStr = '都市高速路';
                            break;
                        case 3:
                            roadTypeStr = '国道';
                            break;
                        case 4:
                            roadTypeStr = '省道';
                            break;
                        case 5:
                            roadTypeStr = '县道';
                            break;
                        case 6:
                            roadTypeStr = '乡村道路';
                            break;
                        case 7:
                            roadTypeStr = '其他道路';
                            break;
                    }
                }
                var roadLimitSpeed = item.roadLimitSpeed;
                if(roadLimitSpeed == 'null' || roadLimitSpeed == ""){
                    roadLimitSpeed = "-";
                }else{
                    roadLimitSpeed = roadLimitSpeed + "km/h";
                }
                var overTime = "";
                if(item.overTime == null || item.overTime == ""){
                    overTime = "-";
                }else{
                    overTime = item.overTime;
                }

                eventDownLoad+= eventGetdata;
                var record = [item.riskType, item.riskEvent, item.level, item.eventTime, overTime, item.speed+'km/h', roadLimitSpeed, roadTypeStr, item.originalLongitude, item.originalLatitude, item.eventNumber, vehicleStatus, preview, eventDownLoad];
                tableData.push(record);
            }

            var table = $("#dataTableRiskType").DataTable({
                "destroy": true,
                "dom": 't',// 自定义显示项
                "data": tableData,
                "lengthChange": false,// 是否允许用户自定义显示数量
                "bPaginate": false, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true,// 本地搜索
                "ordering": false, // 排序功能
                "Info": false,// 页脚信息
                "bInfo": false,
                "autoWidth": true,// 自动宽度
                "stripeClasses": [],
                "pagingType": "simple_numbers", // 分页样式
                "oLanguage": {// 国际语言转化
                    "oAria": {
                        "sSortAscending": " - click/return to sort ascending",
                        "sSortDescending": " - click/return to sort descending"
                    },
                    "sLengthMenu": "显示 _MENU_ 记录",
                    "sZeroRecords": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sEmptyTable": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sLoadingRecords": "正在加载数据-请等待...",
                    "sProcessing": "<img src='../resources/user_share/row_details/select2-spinner.gif'/> 正在加载数据...",
                    "sUrl": "",
                },
            });
            if(event.length > 0 && event[0].eventProtocolType == '25'){ //黑标车辆隐藏 状态信息 字段
                table.column(11).visible(false);
            }
            //状态信息弹框
            $(".stateInfoTd").mouseover(function (e) {
                var datas = $(e.target).attr('data-id').split(',');
                var vehicleInfo = datas.splice(2);
                var result =new Array();
                for(var i=0; i<vehicleInfo.length; i++){
                    result.push(vehicleInfo[i].split(':')[1]);
                }

                var content = '<div><ul style="padding:0; margin:0;">'+
                    '<li>监控对象：'+ '<span>'+ riskDisposeRecord.handleShow(datas[1]) +'</span></li>'+
                    '<li>报警事件：'+ '<span>'+ riskDisposeRecord.handleShow(datas[0]) +'</span></li>'+
                    '<li>ACC状态：'+ '<span>'+ riskDisposeRecord.handleShow(result[0]) +'</span></li>'+
                    '<li>左转向灯：'+ '<span>'+ riskDisposeRecord.handleShow(result[1]) +'</span></li>'+
                    '<li>右转向灯：'+ '<span>'+ riskDisposeRecord.handleShow(result[2]) +'</span></li>'+
                    '<li>制动状态：'+ '<span>'+ riskDisposeRecord.handleShow(result[4]) +'</span></li>'+
                    '<li>插卡状态：'+ '<span>'+ riskDisposeRecord.handleShow(result[5]) +'</span></li>'+
                    '</ul></div>'

                var _this = $(this);
                _this.justToolsTip({
                    animation: "moveInTop",
                    width: "auto",
                    contents: content,
                    gravity: 'top',
                    events: 'mouseover',
                });
            });
        },
        handleShow: function (data){
            return data ? data : '—';
        },

        /*downLoadRiskEvidence: function (riskId) {
            json_ajax("GET", "/clbs/r/riskManagement/disposeReport/riskEvidence", 'json', false, {"riskId": riskId}, riskDisposeRecord.downLoadCallBack)
        },
        downLoadTerminalEvidence: function (downLoadId, number, isEvent) {
            json_ajax("GET", "/clbs/r/riskManagement/disposeReport/terminalEvidence", 'json', false, {
                "downLoadId": downLoadId,
                "isEvent": isEvent,
                "number": number
            }, riskDisposeRecord.downLoadCallBack)
        },*/
        downLoadCallBack: function (data) {
            if (data.obj.hasNotFile) {
                layer.msg(data.obj.msg);
            } else {
                window.location.href = "/clbs/r/riskManagement/disposeReport/downloadFile?filePath=" + data.obj.filePath + "&fileName=" + data.obj.fileName + "&isRiskEvidence=" + data.obj.isRiskEvidence;
            }
        },
        createIndexColumn: function () {
            var info = myTable.dataTable.page.info();
            myTable.dataTable.column(0, {
                search: 'applied',
                order: 'applied'
            }).nodes().each(function (cell, i) {
                cell.innerHTML = info.start + i + 1;
            });
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
        /*showMenu: function (e) {
            if ($("#menuContent").is(":hidden")) {
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
            $("body").bind("mousedown", riskDisposeRecord.onBodyDown);
        },*/
        hideMenu: function () {
            $("#menuContent").fadeOut("fast");
            $("body").unbind("mousedown", riskDisposeRecord.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "menuContent" || $(
                event.target).parents("#menuContent").length > 0)) {
                riskDisposeRecord.hideMenu();
            }
        },
        exportRisk: function () {
            riskDisposeRecord.getCheckedNodes();
            if (!riskDisposeRecord.validates()) {
                return;
            }
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];
            var url = "/clbs/r/riskManagement/disposeReport/export";
            var parameter;
            if (advanceFlag) {
               /* var riskNumberQ = $("#riskNumber").val(); //模糊查询
                var riskTypeQ = $('#alarmTypeSelect').val();
                var riskLevelQ = $('#riskLevel option:selected').text();
                var brandQ = $("#brand").val();
                var driverQ = $("#driver").val();
                var statusQ = $('#status option:selected').text();
                var dealUserQ = $("#dealUser").val();
                var visitTimeQ = $("#visitTime option:selected").text();
                var riskResultQ = $("#riskResult option:selected").text();*/
                parameter = {
                    "vehicleIds": vehicleIds,
                    "startTime": startTime,
                    "endTime": endTime,
                  //  "riskNumber": riskNumberQ,
                    "riskType": riskTypeValue,
                    "riskLevel": riskLevel,
                    "brand": brand,
                    "driver": driver,
                    "status": status,
                    "dealUser": dealUser,
                    "visitTime": visitTime,
                    "riskResult": riskResult
                };
            } else {
                parameter = {"vehicleIds": vehicleIds, "startTime": startTime, "endTime": endTime};
            }
            json_ajax("POST", '/clbs/r/riskManagement/disposeReport/getExportEventSize', "json", false, parameter, function (datas) {
                var length = datas.obj;
                if (length > 20000) {
                    layer.msg("导出的风险事件超过20000条，平台只会导出20000条")
                }
                exportExcelUseForm(url, parameter)

            });
            // ;
        },
        validates: function () {
            return $("#hourslist").validate({
                rules: {
                    startTime: {
                        required: true
                    },
                    endTime: {
                        required: true,
                        compareDate: "#startTime",
                    },
                    groupSelect: {
                        // regularChar: true,
                        zTreeChecked: "treeDemo"
                    }
                },
                messages: {
                    startTime: {
                        required: "请选择开始日期！",
                    },
                    endTime: {
                        required: "请选择结束时间！",
                        compareDate: "结束日期必须大于开始日期！",
                    },
                    groupSelect: {
                        zTreeChecked: "请至少选择一辆车！",
                    }
                }
            }).form();
        },
        exportCallback: function (date) {

            /* if (date == true) {
                 var url = "/clbs/r/riskManagement/disposeReport/export";
                 window.location.href = url;
             } else {
                 alert("导出失败！");
             }*/
        },
        refreshTableClick: function () {
            //组织列表重置
            // var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //zTree.checkAllNodes(true);
            //riskDisposeRecord.init();
            //时间重置
            //$("#timeInterval").dateRangePicker();
            //字段搜索重置
            // riskTypeValue='';
            // $("#riskNumber").val("");
            // $("#brand").val("");
            // $("#dealUser").val("");
            // $("#driver").val("");
            // $("#riskType").val("所有");
            // $("#riskLevel").val("1");
            // $("#status").val("所有");
            // $("#visitTime").val("所有");
            // $("#riskResult").val("所有");
            //刷新表格
            riskDisposeRecord.inquireClick(1, false);
        },
        hightgrade: function () {
            $(".highsearch").slideToggle("slow");
            if ($(this).find("span").hasClass("fa-caret-down")) {
                $(this).find("span").addClass("fa-caret-up").removeClass("fa-caret-down")
            } else {
                $(this).find("span").removeClass("fa-caret-up").addClass("fa-caret-down");
            }
            riskDisposeRecord.hideEventInfoWindow();
        },
        riskPopoverSHFn: function (e) {
            if (!(e.target.className.indexOf('popover') != -1 || $(e.target).parents('.popover').length == 0 || $(e.target).parents('#dataTableRiskType').length > 0 || e.target.className === ' text-center')) {
                $("#riskTypePop").removeClass("in show");
            }
        },
        hideEventInfoWindow: function () {
            if ($("#dataTable tbody tr").hasClass("active-tablebg")) {
                $("#riskTypePop").removeClass("in show");
                $("#dataTable tbody").find("tr").removeClass("active-tablebg");
            }
        },
        showAlarmType: function () {
            if ($("#alarmTypeContent").is(":hidden")) {
                var inpwidth = $("#alarmTypeSelect").width();
                var spwidth = $("#alarmTypeSelectSpan").width();
                var allWidth = inpwidth + spwidth + 21;
                if (navigator.appName == "Microsoft Internet Explorer") {
                    $("#alarmTypeContent").css("width", (inpwidth + 7) + "px");
                } else {
                    $("#alarmTypeContent").css("width", allWidth + "px");
                }
                $("#alarmTypeContent").slideDown("fast");
            } else {
                $("#alarmTypeContent").attr("hidden", "true");
            }
            $("body").bind("mousedown", riskDisposeRecord.onBodyDownAlarmType);
        },
        onBodyDownAlarmType: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "alarmTypeContent" || $(event.target).parents("#alarmTypeContent").length > 0)) {
                riskDisposeRecord.hideMenuAlarmType();
            }
        },
        onCheckChangeValue: function () {

            var comTree = $.fn.zTree.getZTreeObj('alarmTypeTree'),
                nodes = comTree.getCheckedNodes(true),
                nameList = "",
                valueList = [];

            for (var i = 0, l = nodes.length; i < l; i++) {
                var name = nodes[i].name;
                var value = nodes[i].value;
                if (name != "所有") {
                    if (nameList != "") {
                        nameList = nameList + "、" + name;
                    } else {
                        nameList += name;
                    }
                    valueList.push(value)
                }
                if(valueList.length==5){
                    valueList =""
                }
            }
            $('#alarmTypeSelect').val(nameList);
            riskTypeValue = riskDisposeRecord.getGroup(valueList, 0, []);//风险类型排列组合
        },
        getGroup: function (data, index, group) {
            var need_apply = new Array();
            need_apply.push(data[index]);
            for (var i = 0; i < group.length; i++) {
                need_apply.push(group[i] + ',' + data[index]);
            }
            group.push.apply(group, need_apply);

            if (index + 1 >= data.length) {
                return group.join('+');
            }
            return riskDisposeRecord.getGroup(data, index + 1, group);

        },

        hideMenuAlarmType: function () {
            $("#alarmTypeContent").fadeOut("fast");
            $("body").unbind("mousedown", riskDisposeRecord.onBodyDownAlarmType);
        },

        checkRiskEvidenceCallBack: function (data) {
            /*var $tr = $("#" + this_id).children("tbody").children("tr:nth-child(" + (index+1) + ")").children("td:nth-child(" + risk_evidence_index + ")").find("a");
            $tr.addClass("editBtn "+classFlag+" btn-primary").attr("riskid",riskId);*/
            //绑定下载事件
            if (data.success && !data.obj.hasNotFile) {
                var riskId = data.obj.riskId;
                var classFlag = data.obj.classFlag;
                var select = classFlag + "_flag";
                var $a = $('a[' + select + '=' + riskId + ']');
                var riskNumber = $a.attr('risknumber');
                $a.addClass("editBtn " + classFlag + " btn-primary").attr("riskid", riskId);
                if (classFlag == 'riskEvidence') {
                    $a.unbind("click").bind("click", function () {
                        json_ajax("GET", "/clbs/r/riskManagement/disposeReport/riskEvidence", 'json', true,
                            {"riskId": riskId, "riskNumber": riskNumber}, riskDisposeRecord.downLoadCallBack)
                    });
                } else if (classFlag == 'terminalEvidence') {
                    $a.unbind("click").bind("click", function () {
                            json_ajax("GET", "/clbs/r/riskManagement/disposeReport/terminalEvidence", 'json', true,
                                {
                                    "downLoadId": riskId,
                                    "isEvent": false,
                                    "number": riskNumber
                                }, riskDisposeRecord.downLoadCallBack)
                        }
                    );
                }
            }

        },
        tableCallBack: function (data) { //ajax回调操作油标数组 wjk
            //弹出后台返回的消息
            if (data.message) {
                layer.msg(data.message);
                return false;
            }
            if (prevOrnext == '0') { //第一页
                if (data.searchAfter) {
                    nextSearchAfter = data.searchAfter.join(',');
                    pageSearchAfterArr = [];
                    pageSearchAfterArr.push(nextSearchAfter)
                }
            } else {
                if (pageSearchAfterArr.indexOf(data.searchAfter.join(',')) >= 0) {
                    var n = pageSearchAfterArr.indexOf(data.searchAfter.join(','));
                    var arr = pageSearchAfterArr
                    arr.splice(n + 1, arr.length)
                    pageSearchAfterArr = arr

                    nextSearchAfter = pageSearchAfterArr[pageSearchAfterArr.length - 1]
                } else {
                    nextSearchAfter = data.searchAfter.join(',');
                    pageSearchAfterArr.push(nextSearchAfter)
                }
            }
        },
        monitoringJump: function () {
            var search = location.search;
            var param = {};
                if(search != ""){ //截取url参数
                    search.slice(1).split('&').forEach(function (val){
                        var arr = val.split('=');
                        param[arr[0]] = arr[1]
                    });
                    // var th = '%20';
                    // var time = param.timeUrl;
                    // timeUrl = time.replace(new RegExp(th,'gm'),' ');
                    vehicleidUrl = param.vehicleId;
                    statusUrl = param.status;
                    brandUrl = decodeURI(param.brand);
                    //状态默认选择未处理
                    $('#status').val('1');
                    //勾选车辆
                    riskDisposeRecord.searchVehicleTree(brandUrl);
                    setTimeout(function () {
                        var zTree = $.fn.zTree.getZTreeObj("treeDemo");
                        var treeNode =zTree.getNodesByParam("id", vehicleidUrl, null);
                        for(var i=0;i<treeNode.length;i++){
                            zTree.checkNode(treeNode[i], true, true, true);
                        }
                        riskDisposeRecord.inquireClick(1);
                    },2000);


                }
        },
    }
    $(function () {
        //初始化页面
        riskDisposeRecord.init();
        riskDisposeRecord.monitoringJump();
        //添加快速删除
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                riskDisposeRecord.searchVehicleTree(param);
            }
        });
        ;
        /**
         * 监控对象树模糊查询
         */
        var inputChange;
        $("#groupSelect").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            };
            if (!(window.ActiveXObject || "ActiveXObject" in window)){
                searchFlag = true;
            };
            inputChange = setTimeout(function () {
                if(searchFlag) {
                    var param = $("#groupSelect").val();
                    riskDisposeRecord.searchVehicleTree(param);
                }
                searchFlag = true;
            }, 500);
        });
        //当前时间
        riskDisposeRecord.getsTheCurrentTime();
        $("#timeInterval").dateRangePicker({
            dateLimit: 31
        });
        $("#groupSelect").bind("click", showMenuContent);
        $("#refreshTable").bind("click", riskDisposeRecord.refreshTableClick);
        $("#highlever").on("click", riskDisposeRecord.hightgrade);
        //导出
        $("#websocketId").bind("click", riskDisposeRecord.testWebSocket);
        $("#unwebsocketId").bind("click", riskDisposeRecord.distestWebSocket);
        $("#exportRisk").bind("click", riskDisposeRecord.exportRisk);
        $("body").on("click", riskDisposeRecord.riskPopoverSHFn);
        $("#alarmTypeSelectSpan,#alarmTypeSelect").bind("click", riskDisposeRecord.showAlarmType); //车辆树下拉显示
        //多媒体
        $('#download').on('click', riskDisposeRecord.downloadMedia);
        $('#showMedia .arrows').on('click',riskDisposeRecord.mediaChange);
        $('.media_tab').on('click','.btn',riskDisposeRecord.videoChange);
        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            riskDisposeRecord.searchVehicleTree(param);
        });
    })
}(window, $));