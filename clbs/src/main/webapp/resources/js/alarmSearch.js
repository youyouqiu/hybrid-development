(function (window, $) {
    var oldAlarmType;
    var oldStatusValue;
    var oldSalarmSource;
    var oldSalarmStartTime;
    var oldAlarmEndTime;
    var oldPushType;
    var checked = true;
    var oldVehicleList;
    var oldVehicleList_switchSignal;
    var startTime, endTime;//开始时间，结束时间
    var vehicleList = [];
    var alarmSource; // 报警来源
    var setChar; // 树设置
    var hasBegun = [];
    var checkFlag = false; //判断组织节点是否是勾选操作
    var zTreeIdJson = {};
    var size;//当前权限监控对象数量
    var typePos = '-1';
    var typePos_switchSignal = '';
    var typeTree = [];
    var checkExpand = false;//判断是否展开过车辆树（只用于全局报警、实时监控判断）
    var listSize = -1;//记录有多少个分组需要展开（只用于全局报警、实时监控）
    //var fuzzyParam;//模糊查询参数（暂时不用）
    var isCheckedTreeNode = false; // 是否勾选报警查询树中的"全部"节点
    var deviceTypeTxt = '';// 协议类型
    var IOdeviceTypeTxt = "";
    var isDeviceType = '';
    var tableAllData = [];// 存储表格数据
    var tableCheckData = [];// 表格勾选行数据
    var searchParameter = {};// 表格查询条件
    var initStatus = false;

    alarmSearchPages = {
        init: function () {

            // 定制显示列;
            var menu_text = '';
            var table = $("#dataTable tr th:gt(1)");
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);

            // 定制显示列;
            var menu_text1 = '';
            var table1 = $("#switchSignalAlarmTable tr th:gt(0)");
            for (var i = 0; i < table1.length; i++) {
                menu_text1 += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text1").html(menu_text1);


            //组织树
            setChar = {
                async: {
                    url: alarmSearchPages.getIcoTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    sync: false,
                    otherParam: {"type": "multiple"},
                    dataFilter: alarmSearchPages.ajaxDataFilter
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
                    fontCss: setFontCss_ztree,
                    countClass: "group-number-statistics"
                },
                data: {
                    simpleData: {
                        enable: true,
                    }
                },
                callback: {
                    beforeClick: alarmSearchPages.beforeClickVehicle,
                    onAsyncSuccess: alarmSearchPages.zTreeOnAsyncSuccess,
                    beforeCheck: alarmSearchPages.zTreeBeforeCheck,
                    onCheck: alarmSearchPages.onCheckVehicle,
                    onExpand: alarmSearchPages.zTreeOnExpand,
                    onNodeCreated: alarmSearchPages.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), setChar, null);
        },
        getIcoTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/functionconfig/fence/bindfence/alarmSearchTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=monitor";
            }
        },
        //报警类型树结构配置(报警查询)
        getlAlarmType: function () {
            isCheckedTreeNode = false;
            var typeVal = $.parseJSON($("#type").val());
            var deviceType = $("#deviceType").val();
            var alarmSource = $("#alarmSource").val();
            switch (deviceType) {
                case '0': {
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
                            }, {
                                name: 'F3高精度报警',
                                isParent: true,
                                checked: false,
                                children: []
                            }, {
                                name: 'KKS-EV25',
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
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 0);
                                break;
                            case "driverAlarm":
                                typeTree[0].children[1].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 1);
                                break;
                            case "vehicleAlarm":
                                typeTree[0].children[2].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 2);
                                break;
                            case "faultAlarm":
                                typeTree[0].children[3].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 3);
                                break;
                            case "sensorAlarm":
                                typeTree[0].children[4].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 4);
                                break;
                            case "videoAlarm":
                                typeTree[0].children[5].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 5);
                                break;
                            case "platAlarm":
                                typeTree[0].children[6].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 6);
                                break;
                            case "peopleAlarm":
                                typeTree[0].children[7].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 7);
                                break;
                            case "peoplePlatAlarm":
                                typeTree[0].children[8].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 8);
                                break;
                            case "asolongAlarm":
                                typeTree[0].children[9].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 9);
                                break;
                            case "asolongPlatAlarm":
                                typeTree[0].children[10].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 10);
                                break;
                            case "f3longAlarm":
                                typeTree[0].children[11].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 11);
                                break;
                            case "f3longPlatAlarm":
                                typeTree[0].children[12].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 12);
                                break;
                            case "highPrecisionAlarm":
                                typeTree[0].children[13].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 13);
                                break;
                            case "kkslongAlarm":// KKS-EV25报警
                                typeTree[0].children[14].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 14);
                                break;
                        }
                    }
                    break;
                }
                case '1':
                case '11': {
                    typeTree = [{
                        name: '全部',
                        open: true,
                        pos: '-1',
                        isParent: true,
                        checked: true, // 从实时视频or监控跳转过来,一定会带入alarmType,因此默认勾选全部
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
                            }
                        ]
                    }];
                    for (var i = 0; i < typeVal.length; i++) {
                        var sFlag = typeVal[i].type;
                        var ty = typeVal[i];
                        switch (sFlag) {
                            case "alert":
                                typeTree[0].children[0].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 0);
                                break;
                            case "driverAlarm":
                                typeTree[0].children[1].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 1);
                                break;
                            case "vehicleAlarm":
                                typeTree[0].children[2].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 2);
                                break;
                            case "faultAlarm":
                                typeTree[0].children[3].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 3);
                                break;
                            case "sensorAlarm":
                                typeTree[0].children[4].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 4);
                                break;
                            case "videoAlarm":
                                typeTree[0].children[5].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 5);
                                break;
                            case "platAlarm":
                                typeTree[0].children[6].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 6);
                                break;
                        }

                    }
                    break;
                }
                case '5': {
                    typeTree = [{
                        name: '全部',
                        open: true,
                        pos: '-1',
                        isParent: true,
                        checked: true,
                        children: [
                            {
                                name: 'BDTD-SM',
                                isParent: true,
                                checked: false,
                                children: []
                            }, {
                                name: 'BDTD-SM报警',
                                isParent: true,
                                checked: false,
                                children: []
                            }
                        ]
                    }];
                    for (var i = 0; i < typeVal.length; i++) {
                        var sFlag = typeVal[i].type;
                        var ty = typeVal[i];
                        switch (sFlag) {
                            case "peopleAlarm":
                                typeTree[0].children[0].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 0);
                                break;
                            case "peoplePlatAlarm":
                                typeTree[0].children[1].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 1);
                                break;
                        }
                    }
                    break;
                }
                case '9': {
                    typeTree = [{
                        name: '全部',
                        open: true,
                        pos: '-1',
                        isParent: true,
                        checked: true,
                        children: [
                            {
                                name: 'ASO',
                                isParent: true,
                                checked: false,
                                children: []
                            }, {
                                name: 'ASO报警',
                                isParent: true,
                                checked: false,
                                children: []
                            }
                        ]
                    }];
                    for (var i = 0; i < typeVal.length; i++) {
                        var sFlag = typeVal[i].type;
                        var ty = typeVal[i];
                        switch (sFlag) {
                            case "asolongAlarm":
                                typeTree[0].children[0].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 0);
                                break;
                            case "asolongPlatAlarm":
                                typeTree[0].children[1].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 1);
                                break;
                        }

                    }
                    break;
                }
                case '10': {
                    typeTree = [{
                        name: '全部',
                        open: true,
                        pos: '-1',
                        isParent: true,
                        checked: true,
                        children: [
                            {
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
                        switch (sFlag) {
                            case "f3longAlarm":
                                typeTree[0].children[0].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 0);
                                break;
                            case "f3longPlatAlarm":
                                typeTree[0].children[1].children.push(ty);
                                alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 1);
                                break;
                        }
                    }
                    break;
                }
                case '22': {
                    typeTree = [{
                        name: '全部',
                        open: true,
                        pos: '-1',
                        isParent: true,
                        checked: true,
                        children: [
                            {
                                name: 'KKS-EV25',
                                isParent: true,
                                checked: false,
                                children: []
                            }
                        ]
                    }];
                    for (var i = 0; i < typeVal.length; i++) {
                        var sFlag = typeVal[i].type;
                        var ty = typeVal[i];
                        if (sFlag === 'kkslongAlarm') {
                            typeTree[0].children[0].children.push(ty);
                            alarmSearchPages.alarmTypeTreeChecked(isNeedChecked, 0);
                            break;
                        }
                    }
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
                    onCheck: alarmSearchPages.onCheckType,
                    onExpand: alarmSearchPages.zTreeOnExpand,
                    onClick: alarmSearchPages.onClickBack
                }
            };
            var typeTreeObj = $.fn.zTree.init($("#treeTypeDemo"), setting, typeTree);
            // typeTreeObj.checkAllNodes(true);
            alarmSearchPages.getTypeCheckedNodes();
        },
        alarmTypeTreeChecked: function (isNeedChecked, index) {
            if (isNeedChecked) {
                typeTree[0].children[index].checked = true;
                isCheckedTreeNode = true;
            }
        }
        ,
        //报警类型树结构(开关信号报警查询)
        alarmTreeInit: function () {
            //组织树
            setChar = {
                async: {
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    sync: false,
                    otherParam: {"type": "multiple"},
                    dataFilter: alarmSearchPages.ajaxDataFilter
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
                    showIcon: false,
                    dblClickExpand: false,
                    nameIsHTML: true,
                    // fontCss: setFontCss_ztree,
                    countClass: "group-number-statistics"
                },
                data: {
                    simpleData: {
                        enable: true,
                        idKey: 'id',
                        pidKey: 'pId',
                    },
                    key: {
                        name: 'name'
                    }
                },
                callback: {
                    onCheck: alarmSearchPages.onCheckType_switchSignal,
                    onExpand: alarmSearchPages.zTreeOnExpand,
                    onClick: alarmSearchPages.onClickBack_switchSignal
                }
            };
            var data = JSON.parse($("#alarmTypeName").val());
            for (var i = 0; i < data.tree.length; i++) {
                if (data.tree[i].isCondition) {
                    typePos_switchSignal += data.tree[i].name + ",";
                }
            }
            if (typePos_switchSignal.length > 0) {
                typePos_switchSignal = typePos_switchSignal.substring(0, typePos_switchSignal.length - 1);
            }
            data.tree.push({name: "全部", isParent: true, id: -1});
            $.fn.zTree.init($("#treeTypeDemo_switchSignal"), setChar, data.tree);
            var treeObj = $.fn.zTree.getZTreeObj("treeTypeDemo_switchSignal"); //得到该tree
            treeObj.checkAllNodes(true);
            var node = treeObj.getNodeByTId("treeTypeDemo_switchSignal_1"); //选中第一个节点
            treeObj.expandNode(node, true, false, true); //打开节点
        },
        onClickBack: function (e, treeId, treeNode, clickFlag) {
            var zTreeObj = $.fn.zTree.getZTreeObj("treeTypeDemo");
            zTreeObj.checkNode(treeNode, !treeNode.checked, true);
            alarmSearchPages.onCheckType(e, treeId, treeNode);
        },
        onClickBack_switchSignal: function (e, treeId, treeNode, clickFlag) {
            var zTreeObj = $.fn.zTree.getZTreeObj("treeTypeDemo_switchSignal");
            zTreeObj.checkNode(treeNode, !treeNode.checked, true);
            alarmSearchPages.onCheckType_switchSignal(e, treeId, treeNode);
        },
        onCheckType: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeTypeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
            }
            alarmSearchPages.getTypeSelect(zTree);
            alarmSearchPages.getTypeCheckedNodes();
        },
        onCheckType_switchSignal: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeTypeDemo_switchSignal");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
            }
            alarmSearchPages.getTypeSelect_switchSignal(zTree);
            alarmSearchPages.getTypeCheckedNodes_switchSignal();
        },
        getTypeSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $("#groupSelect").val(allNodes[0].name);
            } else {
                $("#groupSelect").val("");
            }
        },
        getTypeSelect_switchSignal: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $("#groupSelect_switchSignal").val(allNodes[0].name);
            } else {
                $("#groupSelect_switchSignal").val("");
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
            $("#groupSelect").val(typeMsg);
        },
        getTypeCheckedNodes_switchSignal: function () {
            typePos_switchSignal = [];
            var typezTree = $.fn.zTree.getZTreeObj("treeTypeDemo_switchSignal"),
                nodes = typezTree.getCheckedNodes(true),
                v = "", typeMsg = "";
            var noCheckLen = typezTree.getCheckedNodes(false).length;
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].id != "" && nodes[i].id != undefined && nodes[i].id != null) {
                    if (nodes[i].isCondition) {
                        v += nodes[i].name + ",";
                        typeMsg += nodes[i].name + ",";
                    }
                }
            }
            if (typeMsg.length > 0) {
                typeMsg = typeMsg.substring(0, v.length - 1);
            }
            if (v.length > 0) {
                v = v.substring(0, v.length - 1);
            }
            typePos_switchSignal = v;
            if (noCheckLen != 0) {
                typePos_switchSignal = typePos_switchSignal.replace("-1,", '');
                typeMsg = typeMsg.replace("全部,", '');
            } else {
                typeMsg = "全部";
            }
            $("#groupSelect_switchSignal").val(typeMsg);
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
                    if (data[i].type == "group") {
                        data[i].open = true;
                    }
                    if (data[i].count == '0') {
                        data[i].isParent = false;
                    }
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
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            var type = $("#atype").val();
            if (type != "" && (type == 0 || type == 2) && !checkExpand) { //若没有进行树展开操作则执行（只用于全局报警、实时监控）
                alarmSearchPages.findTree();
                checkExpand = true;
            }
            var avid = $("#avid").val();
            if ((size <= 5000 || listSize == 0) && avid != undefined && avid != "") {
                var alarmVid = avid.split(",");
                for (var j = 0; j < alarmVid.length; j++) {
                    var node = treeObj.getNodesByParam("id", alarmVid[j], null);
                    for (var i = 0; i < node.length; i++) {
                        if (node != undefined && node.length > 0) {
                            treeObj.checkNode(node[i], true, true);
                        }
                    }

                }
                $("#status").val(0);
                alarmSearchPages.inquireClick(type);
                if (type == 2) {
                    $("#status_switchSignal").val(0);
                    alarmSearchPages.switchSignalAlarmSearchClick(type);
                }

            }
            listSize--;

//        	// 更新节点数量
//            treeObj.updateNodeCount(treeNode);
//            // 默认展开200个节点
//            var initLen = 0;
//            notExpandNodeInit = treeObj.getNodesByFilter(assignmentNotExpandFilter);
//            for (i = 0; i < notExpandNodeInit.length; i++) {
//            	treeObj.expandNode(notExpandNodeInit[i], true, true, false, true);
//                initLen += notExpandNodeInit[i].children.length;
//                if (initLen >= 200) {
//                    break;
//                }
//            }
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
            if (vehicleList.length > 0) {
                alarmSearchPages.checkCurrentNodes(treeNode);
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
                        alarmSearchPages.getGroupChild(node, assign);
                    }
                }
            }
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;

                    json_ajax("post", "/clbs/a/search/getMonitorNum",
                        "json", false, {"id": treeNode.id, "type": treeNode.type}, function (data) {
                            if (data.success) {
                                nodesLength += data.obj;
                            } else {
                                layer.msg("限制校验异常！");
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
//                var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
//                    .getCheckedNodes(true), v = "";
//                var nodesLength = 0;
//                for (var i=0;i<nodes.length;i++) {
//                    if(nodes[i].type == "people" || nodes[i].type == "vehicle"){
//                        nodesLength += 1;
//                    }
//                }
//                if (treeNode.type == "group" || treeNode.type == "assignment"){ // 判断若勾选节点数大于5000，提示
//                    var zTree = $.fn.zTree.getZTreeObj("treeDemo")
//                    json_ajax("post", "/clbs/a/search/getMonitorNum",
//                        "json", false, {"id": treeNode.id,"type": treeNode.type}, function (data) {
//                            nodesLength += data;
//                        })
//                } else if (treeNode.type == "people" || treeNode.type == "vehicle") {
//                    nodesLength += 1;
//                }
                if (nodesLength > 5000) {
                    layer.msg("最多勾选5000个监控对象！");
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
            alarmSearchPages.vehicleListId(); // 记录勾选的节点
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
        //模糊查询
        inputTextAutoSearch: function () {
            search_ztree('treeDemo', 'search_condition', 'vehicle');
        },
        findTree: function () {
            var assignIds = $("#assignIds").val();
            var test = assignIds.split(",");
            test.splice(test.length - 1, 1); //删除最后一个空元素
            var list = alarmSearchPages.sortList(test);
            listSize = list.length;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            for (var i = 0; i < list.length; i++) {
                var node = treeObj.getNodeByParam("id", list[i]);
                if (node != null) {
                    treeObj.expandNode(node, true, false);//指定选中ID节点展开
                } else {
                    listSize--;
                }
            }
        },
        sortList: function (data) {
            var res = [];
            var json = {};
            for (var i = 0; i < data.length; i++) {
                if (!json[data[i]]) {
                    res.push(data[i]);
                    json[data[i]] = 1;
                }
            }
            return res;
        },

        //报警类型
        showFuelType: function () {
            var type = $.parseJSON($("#type").val());
            alarmSource = $("#alarmSource").val();
            for (var i = 0, n = type.length; i < n; i++) {
                var ty = type[i];
                var sFlag = ty.type;
                if (alarmSource == "-1" || (alarmSource == "1" && sFlag.toLowerCase().indexOf("plat") > -1) || (alarmSource == "0" && sFlag.toLowerCase().indexOf("plat") == -1)) {
                    switch (sFlag) {
                        case "alert":
                            $("#alarmType optgroup:nth-child(2)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                            break;
                        case "driverAlarm":
                            $("#alarmType optgroup:nth-child(3)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                            break;
                        case "vehicleAlarm":
                            $("#alarmType optgroup:nth-child(4)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                            break;
                        case "faultAlarm":
                            $("#alarmType optgroup:nth-child(5)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                            break;
                        case "sensorAlarm":
                            $("#alarmType optgroup:nth-child(6)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                            break;
                        case "videoAlarm":
                            $("#alarmType optgroup:nth-child(7)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                            break;
                        case "platAlarm":
                            $("#alarmType optgroup:nth-child(8)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                            break;
                        case "peopleAlarm":
                            $("#alarmType optgroup:nth-child(9)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                            break;
                        case "peoplePlatAlarm":
                            $("#alarmType optgroup:nth-child(10)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                            break;
                        case "asolongAlarm":
                            $("#alarmType optgroup:nth-child(11)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                            break;
                        case "asolongPlatAlarm":
                            $("#alarmType optgroup:nth-child(12)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                            break;
                        case "f3longAlarm":
                            $("#alarmType optgroup:nth-child(13)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                            break;
                        case "f3longPlatAlarm":
                            $("#alarmType optgroup:nth-child(14)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                            break;
                        case "highPrecisionAlarm":
                            $("#alarmType optgroup:nth-child(15)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                            break;
                    }
                }
            }
        },
        /**              
          * 时间戳转换日期              
          * @param <int> unixTime    待时间戳(秒)              
          * @param <bool> isFull    返回完整时间(Y-m-d 或者 Y-m-d H:i:s)              
          * @param <int>  timeZone   时区              
          */
        UnixToDate: function (unixTime, isFull, timeZone) {
            if (unixTime == 0 || unixTime == null) {
                return "";
            }
            if (typeof (timeZone) == 'number') {
                unixTime = parseInt(unixTime) + parseInt(timeZone) * 60 * 60;
            }
            var time = new Date(unixTime * 1000);
            var ymdhis = "";
            ymdhis += time.getFullYear() + "-";
            ymdhis += (time.getMonth() + 1) < 10 ? ("0" + (time.getMonth() + 1) + "-") : ((time.getMonth() + 1) + "-");
            ymdhis += time.getDate() < 10 ? ("0" + time.getDate()) : (time.getDate());
            ;
            if (isFull === true) {
                ymdhis += " " + (time.getHours() < 10 ? ("0" + time.getHours()) : time.getHours()) + ":";
                ymdhis += (time.getMinutes() < 10 ? ("0" + time.getMinutes()) : time.getMinutes()) + ":";
                ymdhis += (time.getSeconds() < 10 ? ("0" + time.getSeconds()) : time.getSeconds());
            }
            return ymdhis;
        },
        validates: function () {
            return $("#alarmForm").validate({
                rules: {
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval"
                    },
                    startTime: {
                        required: true
                    }
                },
                messages: {
                    endTime: {
                        required: "请选择结束日期！",
                        compareDate: "结束日期必须大于开始日期!"
                    },
                    startTime: {
                        required: "请选择开始日期！",
                    }
                }
            }).form();
        },
        switchSignalAlarmValidates: function () {
            return $("#switchSignalAlarmForm").validate({
                rules: {
                    endTime: {
                        required: true,
                        compareDate: "#switchSignaltimeInterval"
                    },
                    startTime: {
                        required: true
                    }
                },
                messages: {
                    endTime: {
                        required: "请选择结束日期！",
                        compareDate: "结束日期必须大于开始日期!"
                    },
                    startTime: {
                        required: "请选择开始日期！",
                    }
                }
            }).form();
        },
        vehicleListId: function () {
            var deviceType = $("#deviceType").val();
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            var nodes = zTree.getCheckedNodes(true);
            if (nodes.length == 0) {
                checked = true;
            }
            v = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle" || nodes[i].type == "people" || nodes[i].type == "thing") {
                    checked = false;
                    if (deviceType == "0") {
                        v += nodes[i].id + ",";
                    } else if (deviceType == "1") {
                        if (nodes[i].deviceType == "0" || nodes[i].deviceType == "1" || nodes[i].deviceType == "12" || nodes[i].deviceType == "13" || nodes[i].deviceType == "14" || nodes[i].deviceType == "15" || nodes[i].deviceType == "16" || nodes[i].deviceType == "17" || nodes[i].deviceType == "18") {
                            v += nodes[i].id + ",";
                        }
                    } else if (deviceType == nodes[i].deviceType) {
                        v += nodes[i].id + ",";
                    } else if (deviceType == '11') {
                        if (nodes[i].deviceType == "20" || deviceType == nodes[i].deviceType) {
                            v += nodes[i].id + ",";
                        }
                    }
                }
            }
            vehicleList = v;
        },
        inquireClick: function (pushType) {
            alarmSearchPages.vehicleListId();
            if (checked) {
                layer.msg("请选择监控对象！");
                return;
            }
            if (!alarmSearchPages.validates()) {
                return;
            }
            ;
            //查询参数
            /* alarmType = $('#alarmType').val();
             if ($(".filter-option").text() != "还没有选择哦" && alarmType == null) {
                 alarmType = ["-1"];
             }
             if (alarmType == "" || alarmType == null) {
                 layer.msg("请选择报警类型！");
                 return;
             }*/
            if (typePos == '' || typePos == null) {
                layer.msg("请选择报警类型！");
                return;
            }

            statusValue = $('#status').val();
            alarmSource = $("#alarmSource").val();
            var timeInterval = $('#timeInterval').val().split('--');
            alarmStartTime = timeInterval[0];
            alarmEndTime = timeInterval[1];
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
            //获取查询前参数
            oldAlarmType = typePos/*alarmType*/;
            oldStatusValue = statusValue;
            oldSalarmSource = alarmSource;
            oldSalarmStartTime = alarmStartTime;
            oldAlarmEndTime = alarmEndTime;
            oldVehicleList = vehicleList;
            if (pushType == 0) {
                pushType = -1;
            }
            oldPushType = pushType;
            searchParameter = {
                "alarmSource": alarmSource,
                "alarmTypes": typePos,
                "status": statusValue,
                "alarmStartTime": alarmStartTime,
                "alarmEndTime": alarmEndTime,
                "vehicleIds": vehicleList,
                // "pushType": pushType
            };
            if (!initStatus) {
                alarmSearchPages.getCallback(searchParameter);
            } else {
                myTable.requestData();
            }

            // json_ajax("POST", url, "json", true, parameter, alarmSearchPages.getCallback);

        },
        switchSignalAlarmSearchClick: function (pushType) {
            alarmSearchPages.vehicleListId();
            if (checked) {
                layer.msg("请选择监控对象！");
                return;
            }
            if (!alarmSearchPages.switchSignalAlarmValidates()) {
                return;
            }
            if (typePos_switchSignal == '' || typePos_switchSignal == null) {
                layer.msg("请选择报警类型！");
                return;
            }
            statusValue_switchSignal = $('#status_switchSignal').val();
            var timeInterval = $('#switchSignaltimeInterval').val().split('--');
            alarmStartTime_switchSignal = timeInterval[0];
            alarmEndTime_switchSignal = timeInterval[1];
            oldVehicleList_switchSignal = vehicleList;
            if (pushType == 0) {
                pushType = -1;
            }
            oldPushType = pushType;
            //发送查询请求
            var url = "/clbs/v/switchSignalAlarm/list";
            var parameter = {
                "alarmTypeNames": typePos_switchSignal,
                "status": statusValue_switchSignal,
                "startTime": alarmStartTime_switchSignal,
                "endTime": alarmEndTime_switchSignal,
                "vehicleIds": vehicleList,
                // "pushType": pushType
            };
            json_ajax("POST", url, "json", true, parameter, alarmSearchPages.getSwitchSignalAlarmCallback);

        },
        alarmDataTable: function () {
            myTable.refresh();
        },
        alarmDataTable_switchSignal: function () {
            var url = "/clbs/v/switchSignalAlarm/list";
            var parameter = {
                "alarmTypeNames": typePos_switchSignal,
                "status": statusValue_switchSignal,
                "startTime": alarmStartTime_switchSignal,
                "endTime": alarmEndTime_switchSignal,
                "vehicleIds": oldVehicleList_switchSignal,
                // "pushType": oldPushType
            };
            json_ajax("POST", url, "json", true, parameter, function (data) {
                if (data.success == true) {
                    SwitchSignalTable.refresh();
                    // myTable.requestData();
                }
            });
        },
        /* exportAlarm: function () {
             var length = $("#dataTable tbody tr").find("td").length;
             if (length > 1) {
                 //layer.msg('正在处理导出数据,请耐心等待', {icon: 16, time: false, shade: [0.1, true], skin: "layui-layer-border layui-layer-hui"});
                 var url = "/clbs/a/search/export";
                 exportExcelUseFormGet(url, []);
             }
         },*/
        exportAlarm: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据,无法导出");
                return;
            }
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }

            var url = "/clbs/a/search/exportAlarmList";
            json_ajax("post", url, "json", true, searchParameter, function (result) {
                if (result.success) {
                    layer.confirm(exportTitle, {
                        title: '操作确认',
                        icon: 3, // 问号图标
                        btn: ['确定', '导出管理'] //按钮
                    }, function () {
                        layer.closeAll();
                    }, function () {
                        layer.closeAll();
                        // 打开导出管理弹窗
                        pagesNav.showExportManager();
                    });
                } else if (result.msg) {
                    layer.msg(result.msg);
                }
            });
        },
        exportAlarm_swithcSignal: function () {
            var length = $("#switchSignalAlarmTable tbody tr").find("td").length;

            if(getRecordsNum('switchSignalAlarmTable_info') > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }

            if (length > 1) {
                var url = "/clbs/v/switchSignalAlarm/export";
                exportExcelUseFormGet(url, []);
            }
        },
        getStatue: function (url) {
            $.ajax({
                type: "POST",
                url: url,
                dataType: "json",
                async: true,
                success: function (data) {
                    if (data.success == true) {
                        layer.closeAll();
                    } else {
                        setTimeout(function () {
                            alarmSearchPages.getStatue(url);
                        }, 1000);
                    }
                }
            });
        },
        getCallback: function (data) {
            initStatus = true;
            // $('#Ul-menu-text .toggle-vis').prop('checked', true);
            tableAllData = [];

            // if (data.success == true) {
            $("#alarmExport").removeAttr("disabled");

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
                    var result;
                    if (row.status == 0) {// 未处理
                        result = "<input class='subChk enabledSubChk' type='checkbox' name='subChk'  value='" + row.monitorId + "'/>";
                    } else {// 已处理
                        result = "<input class='subChk' type='checkbox' name='subChk' disabled/>";
                    }
                    return result;
                }
            }, {
                "data": "monitorName",
                "class": "text-center"
            }, {
                "data": "plateColor",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return getPlateColor(data);
                }
            }, {
                "data": "name",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == null || data == undefined) {
                        return '';
                    }

                    return data;

                }
            }, {
                "data": "assignmentName",
                "class": "text-center"
            }, {
                "data": "employeeName",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == null || data == undefined) {
                        return ''
                    }

                    return data;

                }
            }, {
                "data": "status",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    var str = "-";
                    if (row.employeeName != null) {
                        str = row.employeeName;
                    }
                    var dataString = "" + row.startTime + "|" + row.alarmType + "|" + row.description + "|" + row.id + "|" + row.monitorName + "|" + row.swiftNumber + "|" + row.monitorType + "|" + row.monitorId + "|" + str + "|" + row.assignmentName + "|" + row.alarmSource + "|" + row.alarmEndTime + "";
                    if (row.status == 0) {
                        if (row.deviceType != '22' && $("#alarmRole").val() == 'true') {
                            return '<a onclick="alarmSearchPages.warningManage(\'' + dataString + '\',\'' + row.alarmStartTime + '\')">未处理</a>'
                        }
                        return '未处理'

                    }
                    return "已处理";

                }
            }, {
                "data": "description",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == null || data == undefined) {
                        return '';
                    }

                    return data;

                }
            }, {
                //严重程度 --新添加的
                "data": "severity",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (row.alarmType == 76) {
                        if (row.calStandard == 2) {
                            if (row.severity == null || row.severity == '') {
                                return '';
                            } else if (row.severity < 0.2) {
                                return '一般严重';
                            } else if (row.severity >= 0.2 && row.severity < 0.5) {
                                return '比较严重';
                            } else if (row.severity >= 0.5) {
                                return '特别严重';
                            }
                        }
                    }
                    return '-';
                }
            },
                {
                    "data": "alarmSource",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == 1) {
                            return "平台报警";
                        }
                        return "终端报警";

                    }
                }, {
                    "data": "speed",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        data = alarmSearchPages.fiterNumber(data);
                        return data;
                    }

                }, {
                    "data": "roadType",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        var roadType = row.roadType;
                        if (roadType == 1) {
                            return "高速路";
                        } else if (roadType == 2) {
                            return "都市高速路";
                        } else if (roadType == 3) {
                            return "国道";
                        } else if (roadType == 4) {
                            return "省道";
                        } else if (roadType == 5) {
                            return "县道";
                        } else if (roadType == 6) {
                            return "乡村道路";
                        } else if (roadType == 7) {
                            return "其他道路";
                        }
                        return "-";

                    }
                }, {
                    "data": "recorderSpeed",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        var recorderSpeed = row.recorderSpeed;
                        if (recorderSpeed == "" || recorderSpeed == null) {
                            return "-";
                        }
                        return parseInt(recorderSpeed);

                    }
                }, { //限速
                    "data": "speedLimit",
                    "class": "text-center",
                    render: function (data, type, row, meta) {

                        if (row.alarmType == 76 && row.speedLimit != null && row.speedLimit != '') {
                            return row.speedLimit;
                        }
                        return '-';

                    }

                }, { //路网限速
                    "data": "roadNetSpeedLimit",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if ((row.alarmType == 76 || row.alarmType == 164) && row.roadNetSpeedLimit != null && row.roadNetSpeedLimit != '') {
                            return row.roadNetSpeedLimit;
                        }
                        return '-';
                    }
                }, { //4、超速时长
                    "data": "speedTime",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if ((row.alarmType == 76 || row.alarmType == 164 || row.alarmType == 1) && row.speedTime != null && row.speedTime != '') {
                            var secondTime = parseInt(row.speedTime) / 1000;
                            var minuteTime = 0;
                            var hourTime = 0;
                            if (secondTime > 60) {
                                minuteTime = parseInt(secondTime / 60);
                                secondTime = parseInt(secondTime % 60);
                                if (minuteTime > 60) {
                                    hourTime = parseInt(minuteTime / 60);
                                    minuteTime = parseInt(minuteTime % 60);
                                }
                            }
                            var result = parseInt(secondTime) + "秒";

                            if (minuteTime > 0) {
                                result = parseInt(minuteTime) + "分" + result;
                            }
                            if (hourTime > 0) {
                                result = parseInt(hourTime) + "小时" + result;
                            }
                            return result;
                        }
                        return '-';
                    }
                }, { //持续报警时长
                    "data": "startTime",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (row.speedTime != null && row.speedTime != '') {
                            var secondTime = parseInt(row.speedTime) / 1000;
                            var minuteTime = 0;
                            var hourTime = 0;
                            if (secondTime > 60) {
                                minuteTime = parseInt(secondTime / 60);
                                secondTime = parseInt(secondTime % 60);
                                if (minuteTime > 60) {
                                    hourTime = parseInt(minuteTime / 60);
                                    minuteTime = parseInt(minuteTime % 60);
                                }
                            }
                            var result = parseInt(secondTime) + "秒";

                            if (minuteTime > 0) {
                                result = parseInt(minuteTime) + "分" + result;
                            }
                            if (hourTime > 0) {
                                result = parseInt(hourTime) + "小时" + result;
                            }
                            return result;
                        }
                        return '-';
                    }
                }, {
                    "data": "startTime",
                    "class": "text-center",
                }, {
                    "data": "endTime",
                    "class": "text-center",
                }, {
                    "data": "alarmStartLocation",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            var str = data.split(",");
                            var coord = str[0];
                            var indexOf = coord.indexOf(".") + 4;
                            var startLong = coord.substring(0, indexOf);
                            return startLong;
                        }
                        return "-";

                    }

                }, {
                    "data": "alarmStartLocation",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            var str = data.split(",");
                            var coord = str[1];
                            var indexOf = coord.indexOf(".") + 4;
                            var startLong = coord.substring(0, indexOf);
                            return startLong;
                        }
                        return "-";

                    }

                }, {
                    "data": "alarmEndLocation",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            var str = data.split(",");
                            var coord = str[0];
                            var indexOf = coord.indexOf(".") + 4;
                            var startLong = coord.substring(0, indexOf);
                            return startLong;
                        }
                        return "-";

                    }

                }, {
                    "data": "alarmEndLocation",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            var str = data.split(",");
                            var coord = str[1];
                            var indexOf = coord.indexOf(".") + 4;
                            var startLong = coord.substring(0, indexOf);
                            return startLong;
                        }
                        return "-";

                    }

                }, {
                    "data": "alarmStartSpecificLocation",
                    "class": "text-center"
                }, {
                    "data": "alarmEndSpecificLocation",
                    "class": "text-center"
                }, {
                    "data": "fenceType",
                    "class": "text-center",
                    render: function (data, type, row, mets) {
                        if (data == null || data == undefined) {
                            return ''
                        }
                        else if (data == 'zw_m_rectangle') {
                            return "矩形"
                        } else if (data == 'zw_m_circle') {
                            return "圆形"
                        } else if (data == 'zw_m_line') {
                            return "线"
                        } else if (data == 'zw_m_polygon') {
                            return "多边形"
                        }
                        return data;

                    }
                }, {
                    "data": "fenceName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined) {
                            return ''
                        }

                        return data;

                    }
                }, {
                    "data": "personName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined) {
                            return ''
                        }

                        return data;

                    }
                }, {
                    "data": "handleTimeStr",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined || row.status == "0") {
                            return ''
                        }

                        return data;

                    }
                }, {
                    "data": "handleType",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined) {
                            return ''
                        }

                        return data;

                    }
                }, {
                    "data": "remark",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined) {
                            return ''
                        }

                        return data;

                    }
                }
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                // $.extend(d, data);
                d.alarmSource = alarmSource;
                d.alarmTypes = typePos;
                d.status = statusValue;
                d.alarmStartTime = alarmStartTime;
                d.alarmEndTime = alarmEndTime;
                d.vehicleIds = vehicleList;
            };
            //表格setting
            var url = "/clbs/a/search/alarmPageList";
            var setting = {
                listUrl: url,
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                getAddress: false,//是否逆地理编码
                address_index: 16,
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                drawCallbackFun: function () {
                    var api = myTable.dataTable;
                    tableAllData = api.nodes().data();
                    console.log(api.context, 'api.context');
                    var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
                    api.column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = startIndex + i + 1;
                        $(cell).closest('tr').find('.subChk').attr('data-index', startIndex + i);
                    });

                },
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
            //显示隐藏列
            $('#Ul-menu-text .toggle-vis').off('change').on('change', function (e) {
                var column = myTable.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });

            // }
            //全选
            $('#dataTable').on('click', '#checkAllAlarm', function () {
                $('.enabledSubChk').prop('checked', this.checked);
            });
            $('#dataTable').on('click', '.enabledSubChk', function () {
                var status = $('.enabledSubChk').length == $('.enabledSubChk').filter(':checked').length ? true : false;
                $('#checkAllAlarm').prop('checked', status);
            });
        },
        getSwitchSignalAlarmCallback: function (data) {
            // 定制显示列;
            var menu_text1 = '';
            var table1 = $("#switchSignalAlarmTable tr th:gt(0)");
            for (var i = 0; i < table1.length; i++) {
                menu_text1 += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table1[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text1").html(menu_text1);

            if (data.success == true) {
                $("#alarmExport_switchSignal").removeAttr("disabled");

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
                    "data": "monitorName",
                    "class": "text-center"
                }, {
                    "data": "plateColor",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return getPlateColor(data);
                    }
                }, {
                    "data": "name",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined) {
                            return '';
                        }

                        return data;

                    }
                }, {
                    "data": "assignmentName",
                    "class": "text-center"
                }, {
                    "data": "employeeName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined) {
                            return ''
                        }

                        return data;

                    }
                }, {
                    "data": "status",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        var str = "-";
                        if (row.employeeName != null) {
                            str = row.employeeName;
                        }
                        var dataString = "" + row.startTime + "|" + row.alarmType + "|" + row.description + "|" + row.id + "|" + row.monitorName + "|" + row.swiftNumber + "|" + row.monitorType + "|" + row.monitorId + "|" + str + "|" + row.assignmentName + "|" + row.alarmSource + "|" + row.alarmEndTime + "|" + true + "";
                        if (row.status == 0) {
                            if ($("#alarmRole").val() == 'true') {
                                return '<a onclick="alarmSearchPages.warningManage_switchSignal(\'' + dataString + '\')">未处理</a>'
                            }
                            return "未处理"

                        }
                        return "已处理";

                    }
                }, {
                    "data": "description",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined) {
                            return '';
                        }

                        return data;

                    }
                }, {
                    "data": "speed",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        data = alarmSearchPages.fiterNumber(data);
                        return data;
                    }
                }, {
                    "data": "recorderSpeed",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        var recorderSpeed = row.recorderSpeed;
                        if (recorderSpeed == "" || recorderSpeed == null) {
                            return "-";
                        }
                        return recorderSpeed;

                    }
                }, {
                    "data": "startTime",
                    "class": "text-center",
                }, {
                    "data": "endTime",
                    "class": "text-center",
                }, {
                    "data": "alarmStartLocation",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            var str = data.split(",");
                            var coord = str[0];
                            var indexOf = coord.indexOf(".") + 4;
                            var startLong = coord.substring(0, indexOf);
                            return startLong;
                        }
                        return "-";

                    }

                }, {
                    "data": "alarmStartLocation",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            var str = data.split(",");
                            var coord = str[1];
                            var indexOf = coord.indexOf(".") + 4;
                            var startLong = coord.substring(0, indexOf);
                            return startLong;
                        }
                        return "-";

                    }

                }, {
                    "data": "alarmEndLocation",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            var str = data.split(",");
                            var coord = str[0];
                            var indexOf = coord.indexOf(".") + 4;
                            var startLong = coord.substring(0, indexOf);
                            return startLong;
                        }
                        return "-";

                    }

                }, {
                    "data": "alarmEndLocation",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data != null) {
                            var str = data.split(",");
                            var coord = str[1];
                            var indexOf = coord.indexOf(".") + 4;
                            var startLong = coord.substring(0, indexOf);
                            return startLong;
                        }
                        return "-";

                    }

                }, {
                    "data": "alarmStartSpecificLocation",
                    "class": "text-center"
                }, {
                    "data": "alarmEndSpecificLocation",
                    "class": "text-center"
                }, {
                    "data": "personName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined) {
                            return ''
                        }

                        return data;

                    }
                }, {
                    "data": "handleTimeStr",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined || row.status == "0") {
                            return ''
                        }

                        return data;

                    }
                }, {
                    "data": "handleType",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined) {
                            return ''
                        }

                        return data;

                    }
                }, {
                    "data": "remark",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined) {
                            return ''
                        }

                        return data;

                    }
                }];
                //表格setting
                var url = "/clbs/v/switchSignalAlarm/getIoAlarmList";
                var setting = {
                    listUrl: url,
                    columnDefs: columnDefs, //表格列定义
                    columns: columns, //表格列
                    dataTableDiv: 'switchSignalAlarmTable', //表格
                    pageable: true, //是否分页
                    showIndexColumn: true, //是否显示第一列的索引列
                    enabledChange: true,
                    getAddress: false,//是否逆地理编码
                    address_index: 16,
                    drawCallbackFun: function () {
                        var api = SwitchSignalTable.dataTable;
                        var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
                        api.column(0).nodes().each(function (cell, i) {
                            cell.innerHTML = startIndex + i + 1;
                        });

                    },
                };
                //创建表格
                SwitchSignalTable = new TG_Tabel.createNew(setting);
                SwitchSignalTable.init();
            }
        },

        updateState: function (data) {
            json_ajax("POST", "/clbs/a/search/updateState", "json", false, {"id": data}, function (result) {
                if (result.success) {
                    $("#" + data + "").parent().text("已处理");
                    $("#personName" + data + "").text(result.obj.msg.personName);
                    $("#handleTime" + data + "").text(result.obj.msg.handleTime);
                    $("#handleType" + data + "").text(result.obj.msg.handleType);
                }
            })
        },
        //当前时间
        getsTheCurrentTime: function () {
            $('#timeInterval').val(alarmSearchPages.getQueryStartTime() + '--' + alarmSearchPages.getQueryEndTime());
        },
        //当前时间
        getSwitchSignalTheCurrentTime: function () {
            $('#switchSignaltimeInterval').val(alarmSearchPages.getQueryStartTime() + '--' + alarmSearchPages.getQueryEndTime());
        }
        ,
        getQueryStartTime: function () {
            var nowDate = new Date();
            startTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate()) + " " + "00:00:00";
            var atime = $("#atime").val();
            if (atime != undefined && atime != "") {
                startTime = atime;
            }
            return startTime;
        },
        getQueryEndTime: function () {
            var nowDate = new Date();
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
            return endTime
        }
        ,
        changeTime: function (time) {
            var secondTime = parseInt(time);
            var minuteTime = 0;
            var hourTime = 0;
            if (secondTime > 60) {
                minuteTime = parseInt(secondTime / 60);
                secondTime = parseInt(secondTime % 60);
                if (minuteTime > 60) {
                    hourTime = parseInt(minuteTime / 60);
                    minuteTime = parseInt(minuteTime % 60);
                }
            }
            var result = "" + parseInt(secondTime) + "秒";

            if (minuteTime > 0) {
                result = "" + parseInt(minuteTime) + "分" + result;
            }
            if (hourTime > 0) {
                result = "" + parseInt(hourTime) + "小时" + result;
            }
        },
        //模糊查询
        inputTextAutoSearch: function () {
            search_ztree('treeDemo', 'search_condition', 'vehicle');
        },
        endTimeStyle: function () {
            var width = $(this).width();
            var offset = $(this).offset();
            var left = offset.left - (207 - width);
            $("#laydate_box").css("left", left + "px");
        },
        //改变报警类型下拉框
        alarmListChange: function () {
            var deviceType = $("#deviceType").val();
            if (deviceType == 0) {
                var atext =
                    "<optgroup label='全部'><option value='-1'>全部</option></optgroup>" +
                    "<optgroup label='预警'></optgroup>" +
                    "<optgroup label='驾驶员引起报警'></optgroup>" +
                    "<optgroup label='车辆报警'></optgroup>" +
                    "<optgroup label='故障报警'></optgroup>" +
                    "<optgroup label='F3传感器报警'></optgroup>" +
                    "<optgroup label='视频报警'></optgroup>" +
                    "<optgroup label='平台报警'></optgroup>" +
                    "<optgroup label='BDTD-SM'></optgroup>" +
                    "<optgroup label='BDTD-SM报警'></optgroup>" +
                    "<optgroup label='ASO'></optgroup>" +
                    "<optgroup label='ASO报警'></optgroup>" +
                    "<optgroup label='F3超长待机'></optgroup>" +
                    "<optgroup label='F3超待平台报警'></optgroup>";
                $("#alarmType").html(atext);
                alarmSearchPages.showFuelType();
            } else if (deviceType == 1) {
                var btext =
                    "<optgroup label='全部'><option value='-1'>全部</option></optgroup>" +
                    "<optgroup label='预警'></optgroup>" +
                    "<optgroup label='驾驶员引起报警'></optgroup>" +
                    "<optgroup label='车辆报警'></optgroup>" +
                    "<optgroup label='故障报警'></optgroup>" +
                    "<optgroup label='F3传感器报警'></optgroup>" +
                    "<optgroup label='视频报警'></optgroup>" +
                    "<optgroup label='平台报警'></optgroup>";
                $("#alarmType").html(btext);
                var type = $.parseJSON($("#type").val());
                alarmSource = $("#alarmSource").val();
                for (var i = 0, n = type.length; i < n; i++) {
                    var ty = type[i];
                    var sFlag = ty.type;
                    if (alarmSource == "-1" || (alarmSource == "1" && sFlag.toLowerCase().indexOf("plat") > -1) || (alarmSource == "0" && sFlag.toLowerCase().indexOf("plat") == -1)) {
                        switch (sFlag) {
                            case "alert":
                                $("#alarmType optgroup:nth-child(2)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                                break;
                            case "driverAlarm":
                                $("#alarmType optgroup:nth-child(3)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                                break;
                            case "vehicleAlarm":
                                $("#alarmType optgroup:nth-child(4)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                                break;
                            case "faultAlarm":
                                $("#alarmType optgroup:nth-child(5)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                                break;
                            case "sensorAlarm":
                                $("#alarmType optgroup:nth-child(6)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                                break;
                            case "videoAlarm":
                                $("#alarmType optgroup:nth-child(7)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                                break;
                            case "platAlarm":
                                $("#alarmType optgroup:nth-child(8)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                                break;
                        }
                    }
                }
            } else if (deviceType == 5) {
                var btext =
                    "<optgroup label='全部'><option value='-1'>全部</option></optgroup>" +
                    "<optgroup label='BDTD-SM'></optgroup>" +
                    "<optgroup label='BDTD-SM报警'></optgroup>";
                $("#alarmType").html(btext);
                var type = $.parseJSON($("#type").val());
                alarmSource = $("#alarmSource").val();
                for (var i = 0, n = type.length; i < n; i++) {
                    var ty = type[i];
                    var sFlag = ty.type;
                    if (alarmSource == "-1" || (alarmSource == "1" && sFlag.toLowerCase().indexOf("plat") > -1) || (alarmSource == "0" && sFlag.toLowerCase().indexOf("plat") == -1)) {
                        switch (sFlag) {
                            case "peopleAlarm":
                                $("#alarmType optgroup:nth-child(2)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                                break;
                            case "peoplePlatAlarm":
                                $("#alarmType optgroup:nth-child(3)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                                break;
                        }
                    }
                }
            } else if (deviceType == 9) {
                var btext =
                    "<optgroup label='全部'><option value='-1'>全部</option></optgroup>" +
                    "<optgroup label='ASO'></optgroup>" +
                    "<optgroup label='ASO报警'></optgroup>";
                $("#alarmType").html(btext);
                var type = $.parseJSON($("#type").val());
                alarmSource = $("#alarmSource").val();
                for (var i = 0, n = type.length; i < n; i++) {
                    var ty = type[i];
                    var sFlag = ty.type;
                    if (alarmSource == "-1" || (alarmSource == "1" && sFlag.toLowerCase().indexOf("plat") > -1) || (alarmSource == "0" && sFlag.toLowerCase().indexOf("plat") == -1)) {
                        switch (sFlag) {
                            case "asolongAlarm":
                                $("#alarmType optgroup:nth-child(2)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                                break;
                            case "asolongPlatAlarm":
                                $("#alarmType optgroup:nth-child(3)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                                break;
                        }
                    }
                }
            } else if (deviceType == 10) {
                var btext =
                    "<optgroup label='全部'><option value='-1'>全部</option></optgroup>" +
                    "<optgroup label='F3超长待机'></optgroup>" +
                    "<optgroup label='F3超待平台报警'></optgroup>";
                $("#alarmType").html(btext);
                var type = $.parseJSON($("#type").val());
                alarmSource = $("#alarmSource").val();
                for (var i = 0, n = type.length; i < n; i++) {
                    var ty = type[i];
                    var sFlag = ty.type;
                    if (alarmSource == "-1" || (alarmSource == "1" && sFlag.toLowerCase().indexOf("plat") > -1) || (alarmSource == "0" && sFlag.toLowerCase().indexOf("plat") == -1)) {
                        switch (sFlag) {
                            case "f3longAlarm":
                                $("#alarmType optgroup:nth-child(2)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                                break;
                            case "f3longPlatAlarm":
                                $("#alarmType optgroup:nth-child(3)").append("<option value=\"" + ty.pos + "\">" + ty.name + "</option>");
                                break;
                        }
                    }
                }
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData));
//         	alarmSearchPages.vehicleListId();
            var list = [];
            if (vehicleList != null && vehicleList != undefined && vehicleList != "") {
                var str = (vehicleList.slice(vehicleList.length - 1) == ',') ? vehicleList.slice(0, -1) : vehicleList;
                list = str.split(",");
            }
            return filterQueryResult(responseData, list);
        },
        searchVehicleTree: function (param) {
            var setQueryChar = {
                async: {
                    url: "/clbs/a/search/monitorTreeFuzzy",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    sync: false,
                    otherParam: {
                        "queryType": $('#queryType ').val(),
                        "type": "multiple",
                        "queryParam": param,
                        "webType": "1"
                    },
                    dataFilter: alarmSearchPages.ajaxQueryDataFilter
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
                    fontCss: setFontCss_ztree,
                    countClass: "group-number-statistics"
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    beforeClick: alarmSearchPages.beforeClickVehicle,
                    onAsyncSuccess: alarmSearchPages.fuzzyZTreeOnAsyncSuccess,
                    //beforeCheck: alarmSearchPages.fuzzyZTreeBeforeCheck,
                    onCheck: alarmSearchPages.fuzzyOnCheckVehicle,
                    //onExpand: alarmSearchPages.zTreeOnExpand,
                    //onNodeCreated: alarmSearchPages.zTreeOnNodeCreated,
                }
            };
            $.fn.zTree.init($("#treeDemo"), setQueryChar, null);
            //fuzzyParam = param;
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
        fuzzyZTreeOnAsyncSuccess: function (event, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.expandAll(true);
        },
        fuzzyZTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                    .getCheckedNodes(true), v = "";
                var nodesLength = 0;
                for (var i = 0; i < nodes.length; i++) {
                    if (nodes[i].type == "people" || nodes[i].type == "vehicle" || nodes[i].type == "thing") {
                        nodesLength += 1;
                    }
                }
                if (treeNode.type == "group" || treeNode.type == "assignment") { // 判断若勾选节点数大于5000，提示
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo")
                    json_ajax("post", "/clbs/a/search/monitorTreeFuzzyCount",
                        "json", false, {"type": "multiple", "queryParam": fuzzyParam}, function (data) {
                            nodesLength += data;
                        })
                } else if (treeNode.type == "people" || treeNode.type == "vehicle" || treeNode.type == "thing") {
                    nodesLength += 1;
                }
                if (nodesLength > 5000) {
                    layer.msg("最多勾选5000个监控对象！");
                    flag = false;
                }
            }
            return flag;
        },
        fuzzyOnCheckVehicle: function (e, treeId, treeNode) {
            //获取树结构
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //获取勾选状态改变的节点
            var changeNodes = zTree.getChangeCheckedNodes();
            if (treeNode.checked) { //若是取消勾选事件则不触发5000判断
                var checkedNodes = zTree.getCheckedNodes(true);
                var nodesLength = 0;
                for (var i = 0; i < checkedNodes.length; i++) {
                    if (checkedNodes[i].type == "people" || checkedNodes[i].type == "vehicle" || checkedNodes[i].type == "thing") {
                        nodesLength += 1;
                    }
                }

                if (nodesLength > 5000) {
                    //zTree.checkNode(treeNode,false,true);
                    layer.msg("最多勾选5000个监控对象！");
                    for (var i = 0; i < changeNodes.length; i++) {
                        changeNodes[i].checked = false;
                        zTree.updateNode(changeNodes[i]);
                    }
                }
            }
            //获取勾选状态被改变的节点并改变其原来勾选状态（用于5000准确校验）
            for (var i = 0; i < changeNodes.length; i++) {
                changeNodes[i].checkedOld = changeNodes[i].checked;
            }
            alarmSearchPages.vehicleListId(); // 记录勾选的节点
        },
        initSearch: function () {
            var type = $("#atype").val();
            if (type == "2" || type == "0") {
                $("#status").val(0);
                setTimeout("alarmSearchPages.inquireClick(" + type + ")", 1500);
            }
        },
        // 以下报警处理功能
        warningManage: function (data, alarmStartTime) {
            $("#warningManage").val("alarm");
            $("#alarmRemark").val("");
            $("#alarm-remark").show();
            $("#smsTxt").val("");
            $("#time").val("");
            $("#warningDescription").text("");
            var dataArray = data.split('|');
            $('#warningManage').modal('show');
            $("#listeningContent,#takePicturesContent,#sendTextMessages,.listenFooter,.takePicturesFooter,.sendTextFooter").hide();
            var dataArray = data.split('|');
            var url = "/clbs/a/search/alarmDeal";
            var data = {"vid": dataArray[7], "type": dataArray[6]};
            var warningType = "";
            var device = "";
            var sim = "";
            json_ajax("POST", url, "json", false, data, function (result) {
                if (result.success) {
                    deviceTypeTxt = result.obj.type;
                    warningType = result.obj.type;
                    device = result.obj.device;
                    sim = result.obj.sim;
                }
            });
            if (deviceTypeTxt == '11' || deviceTypeTxt == '20' || deviceTypeTxt == '21' || deviceTypeTxt == '24' || deviceTypeTxt == '25' || deviceTypeTxt == '26' || deviceTypeTxt == '28') {
                $('.newDeviceInfo').show();
                $('.oldDeviceInfo').hide();
                $('#minResolution, #maxResolution').show();
                $('#defaultValue').attr('selected', false);
            } else {
                $('.newDeviceInfo').hide();
                $('.oldDeviceInfo').show();
                $('#minResolution, #maxResolution').hide();
                $('#defaultValue').attr('selected', true);
            }
            if (warningType == "9" || warningType == "10" || warningType == "5" || dataArray[10] == "1") {
                $("#warningHiden").removeAttr("style");
                $("#warningManageListening").hide();
                $("#warningManagePhoto").hide();
                $("#warningManageSend").hide();
                $("#sno").val("0");
            } else {
                $("#warningHiden").attr("style", "text-align:center");
                $("#warningManageListening").show();
                $("#warningManagePhoto").show();
                $("#warningManageSend").show();
                $("#sno").val(dataArray[5]);
            }
            if (dataArray[11] == "0") {
                $("#warningManageListening").attr("disabled", "disabled");
                $("#warningManagePhoto").attr("disabled", "disabled");
                $("#warningManageSend").attr("disabled", "disabled");
                $("#warningManageAffirm").attr("disabled", "disabled");
                $("#warningManageFuture").attr("disabled", "disabled");
                $("#warningManageCancel").attr("disabled", "disabled");
                $("#color").show();
            } else {
                $("#warningManageListening").removeAttr("disabled");
                $("#warningManagePhoto").removeAttr("disabled");
                $("#warningManageSend").removeAttr("disabled");
                $("#warningManageAffirm").removeAttr("disabled");
                $("#warningManageFuture").removeAttr("disabled");
                $("#warningManageCancel").removeAttr("disabled");
                $("#color").hide();
            }
            $("#warningCarName").text(dataArray[4]);
            $("#warningPeo").text(dataArray[8]);
            $("#warningGroup").text(dataArray[9]);
            $("#warningTime").text(dataArray[0]);
            $("#alarmStartTimeStr").text(alarmStartTime);
            $("#warningDescription").text(dataArray[2]);
            $("#vUuid").val(dataArray[7]);
            $("#simcard").val(sim);
            $("#device").val(device);
            $("#warningType").val(dataArray[1]);
            $('#eventId').val(dataArray[3]);
        },
        warningManage_switchSignal: function (data) {
            $("#warningManage").val("alarm_switchSignal");
            $("#alarmRemark").val("");
            $("#alarm-remark").show();
            $("#smsTxt").val("");
            $("#time").val("");
            $('#warningManage').modal('show');
            $("#warningDescription").text("");
            $("#listeningContent,#takePicturesContent,#sendTextMessages,.listenFooter,.takePicturesFooter,.sendTextFooter").hide();
            var dataArray = data.split('|');
            var url = "/clbs/a/search/alarmDeal";
            var data = {"vid": dataArray[7], "type": 0};
            var warningType = "";
            var device = "";
            var sim = "";
            isDeviceType = dataArray[12];
            json_ajax("POST", url, "json", false, data, function (result) {
                if (result.success) {
                    warningType = result.obj.type;
                    device = result.obj.device;
                    sim = result.obj.sim;
                }
            });

            IOdeviceTypeTxt = warningType;

            if (warningType == '11' || warningType == '20' || warningType == '21' || warningType == '24' || warningType == '25' || warningType == '26' || warningType == '28') {
                $('.newDeviceInfo').show();
                $('.oldDeviceInfo').hide();
                $('#minResolution, #maxResolution').show();
                $('#defaultValue').attr('selected', false);
            } else {
                $('.newDeviceInfo').hide();
                $('.oldDeviceInfo').show();
                $('#minResolution, #maxResolution').hide();
                $('#defaultValue').attr('selected', true);
            }
            //io报警都可以下发短信和拍照
            if (warningType == "9" || warningType == "10" || warningType == "5") {
                $("#warningHiden").removeAttr("style");
                $("#warningManageListening").hide();
                $("#warningManagePhoto").hide();
                $("#warningManageSend").hide();
                $("#sno").val("0");
            } else {
                $("#warningHiden").attr("style", "text-align:center");
                $("#warningManageListening").show();
                $("#warningManagePhoto").show();
                $("#warningManageSend").show();
                $("#sno").val(dataArray[5]);
            }
            if (dataArray[11] == "0") {
                $("#warningManagePhoto").attr("disabled", "disabled");
                $("#warningManageSend").attr("disabled", "disabled");
                $("#warningManageAffirm").attr("disabled", "disabled");
                $("#warningManageFuture").attr("disabled", "disabled");
                $("#warningManageCancel").attr("disabled", "disabled");
                $("#color").show();
            } else {
                $("#warningManagePhoto").removeAttr("disabled");
                $("#warningManageSend").removeAttr("disabled");
                $("#warningManageAffirm").removeAttr("disabled");
                $("#warningManageFuture").removeAttr("disabled");
                $("#warningManageCancel").removeAttr("disabled");
                $("#color").hide();
            }
            $("#warningCarName").text(dataArray[4]);
            $("#warningPeo").text(dataArray[8]);
            $("#warningGroup").text(dataArray[9]);
            $("#warningTime").text(dataArray[0]);
            $("#warningDescription").text(dataArray[2]);
            $("#vUuid").val(dataArray[7]);
            $("#simcard").val(sim);
            $("#device").val(device);
            $("#warningType").val(dataArray[1]);
            $('#eventId').val(dataArray[3]);
        },
        // 监听下发
        listenForAlarm: function () {
            if (alarmSearchPages.listenValidate()) {
                // 为车id赋值
                var vehicleId = $("#vUuid").val();
                $("#vidforAlarmListen").val(vehicleId);
                $("#brandListen").val($("#warningCarName").text());
                $("#alarmListen").val($("#warningType").val());
                $("#startTimeListen").val($("#warningTime").text());

                $("#simcardListen").val($('#simcard').val());
                $("#deviceListen").val($("#device").val());
                $("#snoListen").val($("#sno").val());
                $("#handleTypeListen").val("监听");
                $("#descriptionListen").val($("#warningDescription").text());
                $("#remarkListen").val($("#alarmRemark").val());
                $("#goListeningForAlarm").attr("disabled", "disabled");
                var isSwitchSignal = $("#warningManage").val();
                $("#listeningAlarm").ajaxSubmit(function (data) {
                    if (JSON.parse(data).success) {
                        layer.msg(publicIssuedSuccess);
                        if (isSwitchSignal == 'alarm_switchSignal') {
                            alarmSearchPages.alarmDataTable_switchSignal();
                        } else {
                            alarmSearchPages.alarmDataTable();
                        }
                        $("#warningManage").modal('hide');
                    } else {
                        layer.msg(publicIssuedFailure);
                    }
                    $("#warningManage").modal('hide');
                    $("#goListeningForAlarm").removeAttr("disabled");
                });
            }
            $("#goListeningForAlarm").removeAttr("disabled");
        },
        listenValidate: function () {
            return $("#listeningAlarm").validate({
                rules: {
                    monitorPhone: {
                        isNewTel: true,
                        required: true
                    },
                },
                messages: {
                    monitorPhone: {
                        required: '请输入电话号码'
                    },
                }
            }).form();
        },
        takePhotoForAlarm: function () {
            if (alarmSearchPages.photoValidateForAlarm()) {
                $("#vidforAlarm").val($("#vUuid").val());
                $("#alarmPhoto").val($("#warningType").val());
                $("#startTimePhoto").val($("#warningTime").text());
                $("#brandPhoto").val($("#warningCarName").text());

                $("#simcardPhoto").val($('#simcard').val());
                $("#devicePhoto").val($("#device").val());
                $("#snoPhoto").val($("#sno").val());
                $("#handleTypePhoto").val("拍照");
                $("#description-photo").val($("#warningDescription").text());
                $("#remark-photo").val($("#alarmRemark").val());
                var isSwitchSignal = $("#warningManage").val();
                $("#goPhotographsForAlarm").attr("disabled", "disabled");
                $("#takePhotoForAlarm").ajaxSubmit(function (data) {
                    if (JSON.parse(data).success) {
                        layer.msg(publicIssuedSuccess);
                        if (isSwitchSignal == 'alarm_switchSignal') {
                            alarmSearchPages.alarmDataTable_switchSignal();
                        } else {
                            alarmSearchPages.alarmDataTable();
                        }
                        $("#warningManage").modal('hide');
                    } else {
                        layer.msg(publicIssuedFailure);
                    }
                });
            }
            $("#goPhotographsForAlarm").removeAttr("disabled");
        },
        goTxtSendForAlarm: function () {
            // 为车id赋值
            $("#vidSendTxtForAlarm").val($("#vUuid").val());
            $("#brandTxt").val($("#warningCarName").text());
            $("#alarmTxt").val($("#warningType").val());
            $("#startTimeTxt").val($("#warningTime").text());

            $("#simcardTxt").val($('#simcard').val());
            $("#deviceTxt").val($("#device").val());
            $("#snoTxt").val($("#sno").val());
            $("#description-Txt").val($("#warningDescription").text());

            console.log(isDeviceType);
            if (isDeviceType) {
                $("#deviceTypeTxt").val(IOdeviceTypeTxt);
            } else {
                $("#deviceTypeTxt").val(deviceTypeTxt);
            }
            $("#remark-Txt").val($("#alarmRemark").val());
            $("#handleTypeTxt").val("下发短信");
            var isSwitchSignal = $("#warningManage").val();
            var smsTxt = $("#smsTxt").val();
            if (smsTxt == null || smsTxt.length == 0) {
                alarmSearchPages.showErrorMsg("下发内容不能为空", "smsTxt");
                return;
            }
            if (smsTxt.length > 512) {
                layer.msg("下发内容不能超过512个字符");
                return;
            }
            $("#goTxtSendForAlarm").attr("disabled", "disabled");
            $("#txtSendForAlarm").ajaxSubmit(function (data) {
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess);
                    if (isSwitchSignal == 'alarm_switchSignal') {
                        alarmSearchPages.alarmDataTable_switchSignal();
                    } else {
                        alarmSearchPages.alarmDataTable();
                    }
                    $("#warningManage").modal('hide')
                } else {
                    layer.msg(publicIssuedFailure);
                }
            });
            $("#goTxtSendForAlarm").removeAttr("disabled");
        },
        handleAlarm: function (handleType) {
            var startTime = $("#warningTime").text();
            var alarmStartTimeStr = $("#alarmStartTimeStr").text();
            var plateNumber = $("#warningCarName").text();
            var vehicleId = $("#vUuid").val();
            var simcard = $("#simcard").val();
            var device = $("#device").val();
            var sno = $("#sno").val();
            var alarm = $("#warningType").val();
            var eventId = $("#eventId").val();
            var isSwitchSignal = $("#warningManage").val();
            var remark = $("#alarmRemark").val();
            var description = $("#warningDescription").text();
            var url = "/clbs/v/monitoring/handleAlarm";
            if (isSwitchSignal == 'alarm_switchSignal') {
                url = "/clbs/v/switchSignalAlarm/updateIOAlarm";
            }
            var data = {
                'id': eventId,
                "vehicleId": vehicleId,
                "plateNumber": plateNumber,
                "alarm": alarm,
                "handleType": handleType,
                "startTime": startTime,
                "alarmStartTimeStr": alarmStartTimeStr,
                "description": description,
                "simcard": simcard,
                "device": device,
                "sno": sno,
                "remark": remark
            };
            json_ajax("POST", url, "json", false, data, null);
            // 报警处理完毕后，延迟3秒进行结果查询
            setTimeout(pagesNav.gethistoryno, 3000);
            if (isSwitchSignal == 'alarm_switchSignal') {
                alarmSearchPages.alarmDataTable_switchSignal();
            } else {
                alarmSearchPages.alarmDataTable();
            }
            //myTable.refresh();
            $("#warningManage").modal('hide');
        },
        showOrhide: function (data) {
            if (data == 0) {
                var displays = $('#takePicturesContent').css('display');
                $('#listeningContent').hide();
                $('.listenFooter').hide();
                $('#sendTextMessages').hide();
                $('.sendTextFooter').hide();
                if (displays == 'none') {
                    $('#takePicturesContent').show();
                    $('.takePicturesFooter').show();
                } else {
                    $('#takePicturesContent').hide();
                    $('.takePicturesFooter').hide();
                }
            } else if (data == 1) {
                $('#listeningContent').hide();
                $('.listenFooter').hide();
                $('#takePicturesContent').hide();
                $('.takePicturesFooter').hide();
                var display = $('#sendTextMessages').css('display');
                if (display == 'none') {
                    $('#sendTextMessages').show();
                    $('.sendTextFooter').show();
                } else {
                    $('#sendTextMessages').hide();
                    $('.sendTextFooter').hide();
                }
            } else {
                $('#takePicturesContent').hide();
                $('.takePicturesFooter').hide();
                $('#sendTextMessages').hide();
                $('.sendTextFooter').hide();
                var display = $('#listeningContent').css('display');
                if (display == 'none') {
                    $('#listeningContent').show();
                    $('.listenFooter').show();
                } else {
                    $('#listeningContent').hide();
                    $('.listenFooter').hide();
                }
            }
        },
        photoValidateForAlarm: function () {
            return $("#takePhotoForAlarm").validate({
                rules: {
                    wayID: {
                        required: true
                    },
                    time: {
                        required: true,
                        digits: true,
                        range: [0, 65535]
                    },
                    command: {
                        range: [0, 10],
                        required: true
                    },
                    saveSign: {
                        required: true
                    },
                    distinguishability: {
                        required: true
                    },
                    quality: {
                        range: [1, 10],
                        required: true
                    },
                    luminance: {
                        range: [0, 255],
                        required: true
                    },
                    contrast: {
                        range: [0, 127],
                        required: true
                    },
                    saturability: {
                        range: [0, 127],
                        required: true
                    },
                    chroma: {
                        range: [0, 255],
                        required: true
                    },
                },
                messages: {
                    wayID: {
                        required: alarmSearchChannelID
                    },
                    time: {
                        required: alarmSearchIntervalTime,
                        digits: alarmSearchIntervalError,
                        range: alarmSearchIntervalSize
                    },
                    command: {
                        range: alarmSearchPhotoSize,
                        required: alarmSearchPhotoNull
                    },
                    saveSign: {
                        required: alarmSearchSaveNull
                    },
                    distinguishability: {
                        required: alarmSearchResolutionNull
                    },
                    quality: {
                        range: alarmSearchMovieSize,
                        required: alarmSearchMovieNull
                    },
                    luminance: {
                        range: alarmSearchBrightnessSize,
                        required: alarmSearchBrightnessNull
                    },
                    contrast: {
                        range: alarmSearchContrastSize,
                        required: alarmSearchContrastNull
                    },
                    saturability: {
                        range: alarmSearchSaturatedSize,
                        required: alarmSearchSaturatedNull
                    },
                    chroma: {
                        range: alarmSearchColorSize,
                        required: alarmSearchColorNull
                    }
                }
            }).form();
        },
        fiterNumber: function (data) {
            if (data == null || data == undefined || data == "") {
                return '-';
            }
            /* var data = data.toString();
             data = parseFloat(data);*/
            return data;

        },
        // 应答
        responseSocket: function () {
            alarmSearchPages.isGetSocketLayout();
        },
        isGetSocketLayout: function () {
            setTimeout(function () {
                if (webSocket.conFlag) {
                    webSocket.subscribe(headers, '/user/topic/check', alarmSearchPages.updateTable, "/app/vehicle/inspect", null);
                } else {
                    alarmSearchPages.isGetSocketLayout();
                }
            }, 2000);
        },
        // 应答socket回掉函数
        updateTable: function (msg) {
            if (msg != null) {
                var json = $.parseJSON(msg.body);
                var msgData = json.data;
                if (msgData != undefined) {
                    var msgId = msgData.msgHead.msgID;
                    // if (msgId == 0x9300) {
                    //     var dataType = msgData.msgBody.dataType;
                    //     $("#msgDataType").val(dataType);
                    //     $("#infoId").val(msgData.msgBody.data.infoId);
                    //     $("#objectType").val(msgData.msgBody.data.objectType);
                    //     $("#objectId").val(msgData.msgBody.data.objectId);
                    //     $("#question").text(msgData.msgBody.data.infoContent);
                    //     if (dataType == 0x9301) {
                    //         $("#answer").val("");
                    //         $("#msgTitle").text("平台查岗");
                    //         $("#goTraceResponse").modal('show');
                    //         $("#error_label").hidden();
                    //     }
                    //     if (dataType == 0x9302) {
                    //         $("#answer").val("");
                    //         $("#msgTitle").text("下发平台间报文");
                    //         $("#goTraceResponse").modal('show');
                    //     }
                    // }
                }
            }
        },
        // 应答确定
        platformMsgAck: function () {
            var answer = $("#answer").val();
            if (answer == "") {
                alarmSearchPages.showErrorMsg("应答不能为空", "answer");
                return;
            }
            $("#goTraceResponse").modal('hide');
            var msgDataType = $("#msgDataType").val();
            var infoId = $("#infoId").val();
            var objectType = $("#objectType").val();
            var objectId = $("#objectId").val();
            var url = "/clbs/m/connectionparamsset/platformMsgAck";
            json_ajax("POST", url, "json", false, {
                "infoId": infoId,
                "answer": answer,
                "msgDataType": msgDataType,
                "objectType": objectType,
                "objectId": objectId
            });
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
        alarmSearchTabClick: function () {
            alarmSearchPages.removeClass();
            $(this).addClass("active");
            $('#alarmSearchTableBox').removeClass('active').addClass('active');
            $('#switchSignalAlarmTableBox').removeClass('active');
        },
        switchSignalAlarmTabClick: function () {
            alarmSearchPages.removeClass();
            $('#switchSignalAlarmTableBox').removeClass('active').addClass('active');
            $('#alarmSearchTableBox').removeClass('active');

        },
        removeClass: function () {
            var dataList = $(".dataTableShow");
            for (var i = 0; i < 2; i++) {
                dataList.children("li").removeClass("active");
            }
        },
        getTable: function (table) {
            $('.toggle-vis').prop('checked', true);
            myTables = $(table).DataTable({
                "columnDefs": [{
                    "targets": [0, 2, 3, 4, 5, 6],
                    "searchable": false
                }],
                "destroy": true,
                /*"dom": 'tiprl',// 自定义显示项*/
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
                    //"sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
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
                "order": [
                    [0, null]
                ],// 第一列排序图标改为默认

            });
            myTables.on('order.dt search.dt', function () {
                myTables.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
            //显示隐藏列
            $('#Ul-menu-text .toggle-vis').off('change').on('change', function (e) {
                var column = myTables.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
        },
        getIOTable: function (table) {
            $('#Ul-menu-text1 .toggle-vis').prop('checked', true);
            myTable1 = $(table).DataTable({
                "columnDefs": [{
                    "targets": [0, 2, 3, 4, 5, 6],
                    "searchable": false
                }],
                "destroy": true,
                /*"dom": 'tiprl',// 自定义显示项*/
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
                    //"sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
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
                "order": [
                    [0, null]
                ],// 第一列排序图标改为默认

            });
            myTable1.on('order.dt search.dt', function () {
                myTable1.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
            //显示隐藏列
            $('#Ul-menu-text1 .toggle-vis').off('change').on('change', function (e) {
                var column = myTable1.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
        },
        /**
         * 报警批量处理
         * */
        batchDeal: function () {
            var enabledSubChk = $('.enabledSubChk:checked');
            if (enabledSubChk.length === 0) {
                layer.msg('请至少勾选一项');
                return;
            }
            var tableReault = '';
            tableCheckData = [];
            for (var i = 0; i < enabledSubChk.length; i++) {
                // var index = $(enabledSubChk[i]).attr('data-index');
                var index = $(enabledSubChk[i]).closest('tr').index();
                var item = tableAllData[index];
                console.log('index', $(enabledSubChk[i]).closest('tr').index());

                tableCheckData.push({
                    alarm: item.alarmType,
                    endTime: item.endTime,
                    plateNumber: item.monitorName,
                    startTime: item.startTime,
                    vehicleId: item.monitorId,
                    primaryKey: item.primaryKey,
                });
                tableReault += '<tr><td>' + (i + 1) + '</td><td>' + item.monitorName + '</td><td>' + item.description + '</td><td>' + item.startTime + '</td></tr>'
            }
            $('#batchAlarmTbody').html(tableReault);
            $('#batchAlarmRemark').val('');
            $("#batchWarningManageBody").css({"height": "auto", "max-height": ($(window).height() - 300) + "px"});
            $('#batchWarningManage').modal('show');
        },
        // 报警批量处理
        batchHandleAlarm: function (handletype) {
            var newData = JSON.parse(JSON.stringify(tableCheckData));
            var primaryKey = [];
            newData.forEach(function (item) {
                primaryKey.push(item.primaryKey);
            })
            var alarmInfoJsonArr = {
                dealOfMsg: '',
                remark: $('#batchAlarmRemark').val(),
                handleType: handletype,
                monitorPhone: "",
                primaryKeyStr: primaryKey.join(','),
                recordsStr: JSON.stringify(newData)
            }
            json_ajax("post", '/clbs/a/search/batch/handleAlarm', "json", false, alarmInfoJsonArr, function (data) {
                // 报警处理完毕后，延迟3秒进行结果查询
                setTimeout(pagesNav.gethistoryno, 3000);
                alarmSearchPages.alarmDataTable();
                $('#batchWarningManage').modal('hide');
                $('#checkAllAlarm').prop('checked', false);
                if (!data.success && data.msg) {
                    layer.msg(data.msg);
                }
            })
        }
    };
    $(function () {
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'search_condition') {
                alarmSearchPages.init();
            }
        });
        //初始化组织结构
        alarmSearchPages.init();
        alarmSearchPages.responseSocket();
        alarmSearchPages.getTable("#dataTable");
        alarmSearchPages.getIOTable("#switchSignalAlarmTable");

        /**
         * 批量处理报警相关
         * */
        $('#batchDeal').on('click', alarmSearchPages.batchDeal);
        $("#batchWarningManageClose").click(function () {
            $("#batchWarningManage").modal('hide')
        });
        $("#batchWarningManageAffirm").bind("click", function () {
            alarmSearchPages.batchHandleAlarm("人工确认报警")
        });
        $("#batchWarningManageCancel").bind("click", function () {
            alarmSearchPages.batchHandleAlarm("不做处理")
        });
        $("#batchWarningManageFuture").bind("click", function () {
            alarmSearchPages.batchHandleAlarm("将来处理")
        });


        //报警类型树结构
        alarmSearchPages.getlAlarmType();
        alarmSearchPages.alarmTreeInit();
        //报警类型
        alarmSearchPages.showFuelType();
        //设置当前时间显示
        $('#timeInterval').dateRangePicker({
            element: '#inquireClick',
            dateLimit: 31
        });
        $('#switchSignaltimeInterval').dateRangePicker({
            dateLimit: 31,
            element: '#switchSignalAlarmSearchClick'
        });
        //当前时间
        alarmSearchPages.getsTheCurrentTime();
        // 开关信号位查询时间
        alarmSearchPages.getSwitchSignalTheCurrentTime();
        //改变勾选框
        $("#deviceType").change(function () {
            alarmSearchPages.getlAlarmType();
            $("#groupSelect").val("全部");
            typePos = "-1";
            /*alarmSearchPages.alarmListChange();*/
        });
        // 判断报警来源是否被修改
        $("#alarmSource").change(function () {
            $("#groupSelect").val("全部");
            typePos = "-1";
            alarmSearchPages.getlAlarmType();
            /*alarmSearchPages.alarmListChange();*/
        });
        //alarmSearchPages.initSearch();

        $("#warningManageClose").click(function () {
            $("#warningManage").modal('hide')
        });
        $("#warningManageListening").bind("click", function () {
            alarmSearchPages.showOrhide(3)
        });
        $("#warningManagePhoto").bind("click", function () {
            alarmSearchPages.showOrhide(0)
        });
        $("#warningManageSend").bind("click", function () {
            alarmSearchPages.showOrhide(1)
        });
        $("#goListeningForAlarm").bind("click", function () {
            alarmSearchPages.listenForAlarm()
        });
        $("#goPhotographsForAlarm").bind("click", function () {
            alarmSearchPages.takePhotoForAlarm()
        });
        $("#goTxtSendForAlarm").bind("click", function () {
            alarmSearchPages.goTxtSendForAlarm()
        });
        $("#warningManageAffirm").bind("click", function () {
            alarmSearchPages.handleAlarm("人工确认报警")
        });
        $("#warningManageCancel").bind("click", function () {
            alarmSearchPages.handleAlarm("不做处理")
        });
        $("#warningManageFuture").bind("click", function () {
            alarmSearchPages.handleAlarm("将来处理")
        });

        // 模糊搜索
        var inputChange;
        $("#search_condition").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            ;
            inputChange = setTimeout(function () {
                var param = $("#search_condition").val();
                if (param == '') {
                    alarmSearchPages.init();
                } else {
                    alarmSearchPages.searchVehicleTree(param);
                }
            }, 500);
        });
        $('#queryType').on('change', function () {
            if ($('#search_condition').val() != '') {
                $('#search_condition').val('');
                alarmSearchPages.init();
            }
            // alarmSearchPages.searchVehicleTree('');
        });
        // 滚动展开
//		$("#treeDemo").scroll(function () {
//            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
//            zTreeScroll(zTree, this);
//        });
        //IE9
        if (navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") == "MSIE9.0") {
            var search;
            $("#search_condition").bind("focus", function () {
                search = setInterval(function () {
//                    search_ztree('treeDemo', 'search_condition','vehicle');
                    var param = $("#search_condition").val();
                    if (param == '') {
                        alarmSearchPages.init();
                    } else {
                        alarmSearchPages.searchVehicleTree(param);
                    }
                }, 500);
            }).bind("blur", function () {
                clearInterval(search);
            });
        }
        //IE9 end
        //点击查询执行
        $("#inquireClick").bind("click", function () {
            alarmSearchPages.inquireClick(0)
        });
        //点击查询执行
        $("#switchSignalAlarmSearchClick").bind("click", function () {
            alarmSearchPages.switchSignalAlarmSearchClick(0)
        });
        //点击导出执行
        $("#alarmExport").bind("click", alarmSearchPages.exportAlarm);
        $("#alarmExport_switchSignal").bind("click", alarmSearchPages.exportAlarm_swithcSignal);
        $("#endTime").on('click', alarmSearchPages.endTimeStyle);
        // 应答确定
        $('#parametersResponse').on('click', alarmSearchPages.platformMsgAck);
        $("#groupSelect").bind("click", showMenuContent);
        $("#groupSelect_switchSignal").bind("click", showMenuContent);

        $('#alarmSearchTab').bind("click", alarmSearchPages.alarmSearchTabClick);
        $('#switchSignalAlarmTab').bind("click", alarmSearchPages.switchSignalAlarmTabClick);
    })
}(window, $))