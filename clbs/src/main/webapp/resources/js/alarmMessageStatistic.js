(function (window, $) {
    //车辆列表
    var vehicleList = [];
    //车辆id列表
    var vehicleId = [];
    //开始时间
    var startTime;
    var myTable;
    //结束时间
    var endTime;
    var checkFlag = false; //判断组织节点是否是勾选操作
    var size;//当前权限监控对象数量
    var zTreeIdJson = {};

    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于100自动勾选
    var searchFlag = true;

    var typePos = '-1';
    var typeTree = [];
    var isCheckedTreeNode = false; // 是否勾选报警查询树中的"全部"节点
    var simpleQueryParam = '';


    alarmMessageStatistic = {
        init: function () {
            //车辆树
            var setting = {
                async: {
                    url: alarmMessageStatistic.getAlarmReportTreeUrl,//"/clbs/m/functionconfig/fence/bindfence/vehicelTree"
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: alarmMessageStatistic.ajaxDataFilter
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
                    beforeClick: alarmMessageStatistic.beforeClickVehicle,
                    onAsyncSuccess: alarmMessageStatistic.zTreeOnAsyncSuccess,
                    beforeCheck: alarmMessageStatistic.zTreeBeforeCheck,
                    onCheck: alarmMessageStatistic.onCheckVehicle,
                    onNodeCreated: alarmMessageStatistic.zTreeOnNodeCreated,
                    onExpand: alarmMessageStatistic.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);

            alarmMessageStatistic.getlAlarmType();
        },
        //报警类型树结构配置(报警查询)
        getlAlarmType: function () {
            isCheckedTreeNode = false;
            var typeVal = $.parseJSON($("#type").val());

            typeTree = [{
                name: '全部',
                open: true,
                pos: '-1',
                isParent: true,
                checked: false, // 从实时视频or监控跳转过来,一定会带入alarmType,因此默认勾选全部
                children: [
                    {
                        name: '预警',
                        isParent: true,
                        checked: false,
                        children: []
                    }, {
                        name: '驾驶员引起报警',
                        isParent: true,
                        checked: false,
                        children: []
                    }, {
                        name: '车辆报警',
                        isParent: true,
                        checked: false,
                        children: []
                    }, {
                        name: '故障报警',
                        isParent: true,
                        checked: false,
                        children: []
                    }, {
                        name: 'F3传感器报警',
                        isParent: true,
                        checked: false,
                        children: []
                    }, {
                        name: '视频报警',
                        isParent: true,
                        checked: false,
                        children: []
                    }, {
                        name: '平台报警',
                        isParent: true,
                        checked: false,
                        children: []
                    }, {
                        name: 'BDTD-SM',
                        isParent: true,
                        checked: false,
                        children: []
                    }, {
                        name: 'BDTD-SM报警',
                        isParent: true,
                        checked: false,
                        children: []
                    }, {
                        name: 'ASO',
                        isParent: true,
                        checked: false,
                        children: []
                    }, {
                        name: 'ASO报警',
                        isParent: true,
                        checked: false,
                        children: []
                    }, {
                        name: 'F3超长待机',
                        isParent: true,
                        checked: false,
                        children: []
                    }, {
                        name: 'F3超待平台报警',
                        isParent: true,
                        checked: false,
                        children: []
                    }
                ]
            }];
            for (var i = 0; i < typeVal.length; i++) {
                var sFlag = typeVal[i].type;
                var ty = typeVal[i];
                var isNeedChecked = ty.checked;
                switch (sFlag) {
                    case "alert":
                        typeTree[0].children[0].children.push(ty);
                        alarmMessageStatistic.alarmTypeTreeChecked(isNeedChecked, 0);
                        break;
                    case "driverAlarm":
                        typeTree[0].children[1].children.push(ty);
                        alarmMessageStatistic.alarmTypeTreeChecked(isNeedChecked, 1);
                        break;
                    case "vehicleAlarm":
                        typeTree[0].children[2].children.push(ty);
                        alarmMessageStatistic.alarmTypeTreeChecked(isNeedChecked, 2);
                        break;
                    case "faultAlarm":
                        typeTree[0].children[3].children.push(ty);
                        alarmMessageStatistic.alarmTypeTreeChecked(isNeedChecked, 3);
                        break;
                    case "sensorAlarm":
                        typeTree[0].children[4].children.push(ty);
                        alarmMessageStatistic.alarmTypeTreeChecked(isNeedChecked, 4);
                        break;
                    case "videoAlarm":
                        typeTree[0].children[5].children.push(ty);
                        alarmMessageStatistic.alarmTypeTreeChecked(isNeedChecked, 5);
                        break;
                    case "platAlarm":
                        typeTree[0].children[6].children.push(ty);
                        alarmMessageStatistic.alarmTypeTreeChecked(isNeedChecked, 6);
                        break;
                    case "peopleAlarm":
                        typeTree[0].children[7].children.push(ty);
                        alarmMessageStatistic.alarmTypeTreeChecked(isNeedChecked, 7);
                        break;
                    case "peoplePlatAlarm":
                        typeTree[0].children[8].children.push(ty);
                        alarmMessageStatistic.alarmTypeTreeChecked(isNeedChecked, 8);
                        break;
                    case "asolongAlarm":
                        typeTree[0].children[9].children.push(ty);
                        alarmMessageStatistic.alarmTypeTreeChecked(isNeedChecked, 9);
                        break;
                    case "asolongPlatAlarm":
                        typeTree[0].children[10].children.push(ty);
                        alarmMessageStatistic.alarmTypeTreeChecked(isNeedChecked, 10);
                        break;
                    case "f3longAlarm":
                        typeTree[0].children[11].children.push(ty);
                        alarmMessageStatistic.alarmTypeTreeChecked(isNeedChecked, 11);
                        break;
                    case "f3longPlatAlarm":
                        typeTree[0].children[12].children.push(ty);
                        alarmMessageStatistic.alarmTypeTreeChecked(isNeedChecked, 12);
                        break;
                }
            }

            if (isCheckedTreeNode) {
                typeTree[0].checked = true;
            }
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
                    onCheck: alarmMessageStatistic.onCheckType,
                    onExpand: alarmMessageStatistic.typeTreeOnExpand,
                    onClick: alarmMessageStatistic.onClickBack
                }
            };
            var typeTreeObj = $.fn.zTree.init($("#treeTypeDemo"), setting, typeTree);
            alarmMessageStatistic.getTypeCheckedNodes();
        },
        alarmTypeTreeChecked: function (isNeedChecked, index) {
            if (isNeedChecked) {
                typeTree[0].children[index].checked = true;
                isCheckedTreeNode = true;
            }
        },
        onCheckType: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeTypeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
            }
            alarmMessageStatistic.getTypeSelect(zTree);
            alarmMessageStatistic.getTypeCheckedNodes();
        },
        onClickBack: function (e, treeId, treeNode, clickFlag) {
            var zTreeObj = $.fn.zTree.getZTreeObj("treeTypeDemo");
            zTreeObj.checkNode(treeNode, !treeNode.checked, true);
            alarmMessageStatistic.onCheckType(e, treeId, treeNode);
        },
        getTypeSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $("#typeGroupSelect").val(allNodes[0].name);
            } else {
                $("#typeGroupSelect").val("");
            }
        },
        typeTreeOnExpand: function (event, treeId, treeNode) {
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
            if (vehicleList.length > 0) {
                alarmMessageStatistic.checkCurrentNodes(treeNode);
            }
        },
        /**
         * 选中已选的节点
         */
        checkCurrentNodes: function (treeNode) {
            if (vehicleList.length != 0) {
                var crrentSubV = vehicleList.split(",");
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                if (treeNode != undefined && treeNode != null && treeNode.type === "assignment" && treeNode.children != undefined) {
                    var list = treeNode.children;
                    if (list != null && list.length > 0) {
                        for (var j = 0; j < list.length; j++) {
                            var znode = list[j];
                            if (crrentSubV != null && crrentSubV != undefined && crrentSubV.length !== 0 && $.inArray(znode.id, crrentSubV) != -1) {
                                treeObj.checkNode(znode, true, true);
                            }
                        }
                    }
                }
            }
        },
        getTypeCheckedNodes: function () {
            typePos = [];
            var typezTree = $.fn.zTree.getZTreeObj("treeTypeDemo"),
                nodes = typezTree.getCheckedNodes(true),
                v = "", typeMsg = "";
            var noCheckLen = typezTree.getCheckedNodes(false).length;
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].pos != "" && nodes[i].pos != undefined && nodes[i].pos != null) {
                    if (i == 0) {
                        v += nodes[i].pos;
                        typeMsg += nodes[i].name;
                    }
                    else {
                        v += "," + nodes[i].pos;
                        typeMsg += "," + nodes[i].name;
                    }
                }
            }
            typePos = v;
            if (noCheckLen != 0) {
                typePos = typePos.replace("-1,", '');
                typeMsg = typeMsg.replace("全部,", '');
            }
            else {
                typePos = '-1';
                typeMsg = '全部'
            }
            $("#typeGroupSelect").val(typeMsg);
        },
        //模糊查询树
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选

            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                alarmMessageStatistic.init();
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
                        dataFilter: alarmMessageStatistic.ajaxQueryDataFilter
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
                        beforeClick: alarmMessageStatistic.beforeClickVehicle,
                        onCheck: alarmMessageStatistic.onCheckVehicle,
                        onExpand: alarmMessageStatistic.zTreeOnExpand,
                        onNodeCreated: alarmMessageStatistic.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData));
            var nodesArr;
            if ($('#queryType').val() == "vehicle" || $('#queryType').val() == "trade") {
                nodesArr = filterQueryResult(responseData, crrentSubV);
            } else {
                nodesArr = responseData;
            }
            for (var i = 0; i < nodesArr.length; i++) {
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        tableFilter: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(1) + "\" />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text").html(menu_text);
        },
        getAlarmReportTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
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
                tMonth = alarmMessageStatistic.doHandleMonth(tMonth + 1);
                tDate = alarmMessageStatistic.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate;
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = alarmMessageStatistic.doHandleMonth(endMonth + 1);
                endDate = alarmMessageStatistic.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate;
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = alarmMessageStatistic.doHandleMonth(vMonth + 1);
                vDate = alarmMessageStatistic.doHandleMonth(vDate);
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
                    vendMonth = alarmMessageStatistic.doHandleMonth(vendMonth + 1);
                    vendDate = alarmMessageStatistic.doHandleMonth(vendDate);
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
        //判断日期是否合法,是否选中车辆
        validates: function () {
            return $("#hourslist").validate({
                rules: {
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval"
                    },
                    startTime: {
                        required: true
                    },
                    groupSelect: {
                        zTreeChecked: "treeDemo"
                    },
                    typeGroupSelect: {
                        required: true,
                    }
                },
                messages: {
                    endTime: {
                        required: "请选择结束日期!",
                        compareDate: endtimeComStarttime
                    },
                    startTime: {
                        required: "请选择开始日期!",
                    },
                    groupSelect: {
                        zTreeChecked: vehicleSelectBrand,
                    },
                    typeGroupSelect: {
                        required: '请选择报警类型',
                    }
                }
            }).form();
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (size <= 100 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            alarmMessageStatistic.getCharSelect(treeObj);
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
                    alarmMessageStatistic.getCheckedNodes();
                    alarmMessageStatistic.validates();
                }, 600);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            alarmMessageStatistic.getCharSelect(zTree);
            alarmMessageStatistic.getCheckedNodes();
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
        getGroupChild: function (node, assign) { // 递归获取组织及下级组织的分组节点
            var nodes = node.children;
            if (nodes != null && nodes != undefined && nodes.length > 0) {
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.type == "assignment") {
                        assign.push(node);
                    } else if (node.type == "group" && node.children != undefined) {
                        alarmMessageStatistic.getGroupChild(node.children, assign);
                    }
                }
            }
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                .getCheckedNodes(true), v = "", vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].id + ",";
                }
            }
            vehicleList = v;
            vehicleId = vid;
        },
        inquireClick: function (number) {
            $(".ToolPanel").css("display", "block");
            if (number == 0) {
                alarmMessageStatistic.getsTheCurrentTime();
                $('#timeInterval').val(startTime + '--' + endTime);
            } else if (number == -1) {
                alarmMessageStatistic.startDay(-1);
                $('#timeInterval').val(startTime + '--' + endTime);
            } else if (number == -3) {
                alarmMessageStatistic.startDay(-3);
                $('#timeInterval').val(startTime + '--' + endTime);
            } else if (number == -7) {
                alarmMessageStatistic.startDay(-7);
                $('#timeInterval').val(startTime + '--' + endTime);
            }
            alarmMessageStatistic.getCheckedNodes();
            if (!alarmMessageStatistic.validates()) {
                return;
            }

            if (typePos == '-1') {
                var typezTree = $.fn.zTree.getZTreeObj("treeTypeDemo");
                var nodes = typezTree.getCheckedNodes(true);
                var v = "";
                var index = 0;
                for (var i = 0, l = nodes.length; i < l; i++) {
                    if (nodes[i].pos != "" && nodes[i].pos != undefined && nodes[i].pos != null && nodes[i].pos != '-1') {
                        v += (index == 0 ? nodes[i].pos : ("," + nodes[i].pos));
                        index++;
                    }
                }
                typePos = v;
            }

            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];

            var url = "/clbs/m/reportManagement/alarmMessageStatistic/findAlarmMessageListFromPaas";
            var parameter = {
                "vehicleIds": vehicleId,
                "startTime": startTime,
                "endTime": endTime,
                "alarmTypes": typePos
            };
            json_ajax("POST", url, "json", true, parameter, alarmMessageStatistic.getCallback);
        },
        exportAlarm: function () {
            alarmMessageStatistic.getCheckedNodes();
            if (!alarmMessageStatistic.validates()) {
                return;
            }
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var url = "/clbs/m/reportManagement/alarmMessageStatistic/getExportAlarmMessageList?fuzzyQuery=" + simpleQueryParam;
            window.location.href = url;
        },
        getCallback: function (date) {
            if (date.success == true) {
                //用来储存显示数据
                dataListArray = [];
                if (date.obj != null && date.obj.length != 0) {
                    var alarm = date.obj;
                    for (var i = 0; i < alarm.length; i++) {
                        var dateList =
                            [
                                i + 1,
                                alarm[i].monitorName,
                                alarm[i].assignmentName,
                                '<span class="detailSpan" data-type="' + alarm[i].alarmType + '" data-vehicleId="' + alarm[i].monitorId + '">' + alarm[i].plateColorStr + '</span>',
                                alarm[i].description,
                                alarm[i].alarmNumber,
                                alarm[i].handleNumber,
                            ];
                        dataListArray.push(dateList);
                    }
                    alarmMessageStatistic.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                } else {
                    alarmMessageStatistic.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                }
                $("#exportAlarm").removeAttr("disabled");
            } else {
                layer.msg(date.msg, {move: false});
            }
        },
        exportCallback: function (data) {
            if (data == true) {
                var url = "/clbs/m/reportManagement/alarmReport/export";
                window.location.href = url;
            } else {
                layer.msg(exportFail, {move: false});
            }
        },
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
                ],// 第一列排序图标改为默认

            });
            myTable.on('order.dt search.dt', function () {
                myTable.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
            //显示隐藏列
            $('.toggle-vis').off('change').on('change', function (e) {
                var column = myTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            $("#search_button").on("click", function () {
                var tsval = $("#simpleQueryParam").val();
                simpleQueryParam = tsval;
                myTable.search(tsval, false, false).draw();
            });
        },
        reloadData: function (dataList) {
            var currentPage = myTable.page();
            myTable.clear()
            myTable.rows.add(dataList);
            myTable.page(currentPage).draw(false);
            myTable.search('', false, false).page(currentPage).draw();
        },
        //刷新列表
        refreshTable: function () {
            alarmMessageStatistic.inquireClick(1, false);
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
            var timeInterval = $('#timeInterval').val().split('--');
            var url = '/clbs/m/reportManagement/alarmMessageStatistic/findAlarmDetailMessageListNew';
            var detailSpan = targetTr.find('.detailSpan');
            var param = {
                "alarmType": detailSpan.attr('data-type'),
                "vehicleId": detailSpan.attr('data-vehicleId'),
                "startTime": timeInterval[0],
                "endTime": timeInterval[1],
            };
            json_ajax("POST", url, "json", true, param, function (data) {
                if (data.success) {
                    var addStr = '';
                    var result = data.obj;
                    for (var i = 0, len = result.length; i < len; i++) {
                        addStr += '<tr class="newTr">' +
                            '<td>' + (i + 1) + '</td>' +
                            '<td>' + result[i].alarmStartTimeStr + '</td>' +
                            '<td>' + result[i].speed + '</td>' +
                            '<td><a onclick="alarmMessageStatistic.getPosAddress(this,\'' + result[i].alarmStartLocation + '\')">点击获取位置信息</a></td>' +
                            '<td>' + (result[i].status == "1" ? "已处理" : "未处理") + '</td>' +
                            '</tr>'
                    }
                    var html = '<tr class="addTr">' +
                        '        <td colspan="11">' +
                        '            <div class="detailTableBox">' +
                        '                <table class="table table-striped table-bordered table-hover noCheckTable"' +
                        '                 cellspacing="0" width="100%">' +
                        '                    <thead>' +
                        '                       <tr class="addTr">' +
                        '                    <th>序号</th>' +
                        '                    <th>报警时间</th>' +
                        '                    <th>速度</th>' +
                        '                    <th>报警地点</th>' +
                        '                    <th>处理状态</th>' +
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
        //解析位置信息
        getPosAddress: function (event, pos) {
            var posData = pos.split(',');
            var url = '/clbs/v/monitoring/address';
            var param = {addressReverse: [posData[1], posData[0], '', "", 'vehicle']};

            $.ajax({
                type: "POST",//通常会用到两种：GET,POST。默认是：GET
                url: url,//(默认: 当前页地址) 发送请求的地址
                dataType: "json", //预期服务器返回的数据类型。"json"
                async: true, // 异步同步，true  false
                data: param,
                traditional: true,
                timeout: 8000, //超时时间设置，单位毫秒
                success: function (data) {//请求成功
                    $(event).closest('td').html($.isPlainObject(data) ? '未定位' : data);
                },
            });
        }
    };

    $(function () {
        //初始化页面
        alarmMessageStatistic.init();
        $('input').inputClear();
        $('#timeInterval').dateRangePicker({
            dateLimit: 31,
            /*isOffLineReportFlag: true,
            nowDate: alarmMessageStatistic.getYesterDay(),*/
            isShowHMS: false
        });
        alarmMessageStatistic.tableFilter();
        alarmMessageStatistic.getTable('#dataTable');
        //当前时间
        alarmMessageStatistic.getsTheCurrentTime();
        //组织下拉显示
        $("#groupSelect,#typeGroupSelect").bind("click", showMenuContent);
        //导出
        $("#exportAlarm").bind("click", alarmMessageStatistic.exportAlarm);
        $("#refreshTable").bind("click", alarmMessageStatistic.refreshTable);

        // 点击列表项查看明细列表
        $("#dataTable tbody").on('click', 'tr', function () {
            if (!$(this).hasClass('addTr') && !$(this).hasClass('newTr')) {
                alarmMessageStatistic.showTableDetail(this);
            }
        });

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                alarmMessageStatistic.searchVehicleTree(param);
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
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                searchFlag = true;
            }
            inputChange = setTimeout(function () {
                if (searchFlag) {
                    var param = $("#groupSelect").val();
                    alarmMessageStatistic.searchVehicleTree(param);
                }
                searchFlag = true;
            }, 500);
        });
        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            alarmMessageStatistic.searchVehicleTree(param);
        });
    })
}(window, $))