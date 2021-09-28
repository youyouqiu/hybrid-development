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
    var riskEventId = ''; //事件处理
    var Time = '';

    monitorForward = {
        //组织树
        init: function () {
            //组织树
            setChar = {
                async: {
                    url: monitorForward.getIcoTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    sync: false,
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: monitorForward.ajaxDataFilter
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
                    beforeClick: monitorForward.beforeClickVehicle,
                    onAsyncSuccess: monitorForward.zTreeOnAsyncSuccess,
                    beforeCheck: monitorForward.zTreeBeforeCheck,
                    onCheck: monitorForward.onCheckVehicle,
                    onExpand: monitorForward.zTreeOnExpand,
                    //onNodeCreated: monitorForward.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), setChar, null);
        },
        getIcoTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/connectionparamsConfig/t809ForwardTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=monitor&webType=3";
            }
        },
        //报警类型树结构配置(报警查询)
        getlAlarmType: function () {
            isCheckedTreeNode = false;
            var typeVal = $.parseJSON($("#type").val());
            // var deviceType = $("#deviceType").val();
            var deviceType = '0'
            // var alarmSource = $("#alarmSource").val();
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
                            },
                            {
                                name: '视频报警',
                                isParent: true,
                                checked: false,
                                children: []
                            }, {
                                name: '平台报警',
                                isParent: true,
                                checked: false,
                                children: []
                            },
                            {
                                name: '主动安全报警',
                                isParent: true,
                                checked: false,
                                children: []
                            },
                        ]
                    }];
                    for (var i = 0; i < typeVal.length; i++) {
                        var sFlag = typeVal[i].type;
                        var ty = typeVal[i];
                        var isNeedChecked = ty.checked;
                        switch (sFlag) {
                            case "alert":
                                typeTree[0].children[0].children.push(ty);
                                monitorForward.alarmTypeTreeChecked(isNeedChecked, 0);
                                break;
                            case "driverAlarm":
                                typeTree[0].children[1].children.push(ty);
                                monitorForward.alarmTypeTreeChecked(isNeedChecked, 1);
                                break;
                            case "vehicleAlarm":
                                typeTree[0].children[2].children.push(ty);
                                monitorForward.alarmTypeTreeChecked(isNeedChecked, 2);
                                break;
                            case "faultAlarm":
                                typeTree[0].children[3].children.push(ty);
                                monitorForward.alarmTypeTreeChecked(isNeedChecked, 3);
                                break;
                            case "videoAlarm":
                                typeTree[0].children[4].children.push(ty);
                                monitorForward.alarmTypeTreeChecked(isNeedChecked, 4);
                                break;
                            case "platAlarm":
                                if (ty.name != "长时间下线") {
                                    typeTree[0].children[5].children.push(ty);
                                    monitorForward.alarmTypeTreeChecked(isNeedChecked, 5);
                                }
                                break;
                            case "adasAlarm":
                                typeTree[0].children[6].children.push(ty);
                                monitorForward.alarmTypeTreeChecked(isNeedChecked, 6);
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
                    onCheck: monitorForward.onCheckType,
                    onExpand: monitorForward.zTreeOnExpand,
                    onClick: monitorForward.onClickBack
                }
            };
            var typeTreeObj = $.fn.zTree.init($("#treeTypeDemo"), setting, typeTree);
            // typeTreeObj.checkAllNodes(true);
            monitorForward.getTypeCheckedNodes();
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
                    dataFilter: monitorForward.ajaxDataFilter
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
                    fontCss: setFontCss_ztree,
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
                    onCheck: monitorForward.onCheckType_switchSignal,
                    onExpand: monitorForward.zTreeOnExpand,
                    onClick: monitorForward.onClickBack_switchSignal
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
            data.tree.push({name: "全部", isParent: true, id: 0});
            $.fn.zTree.init($("#treeTypeDemo_switchSignal"), setChar, data.tree);
            var treeObj = $.fn.zTree.getZTreeObj("treeTypeDemo_switchSignal"); //得到该tree
            treeObj.checkAllNodes(true);
            var node = treeObj.getNodeByTId("treeTypeDemo_switchSignal_1"); //选中第一个节点
            treeObj.expandNode(node, true, false, true); //打开节点
        },
        onClickBack: function (e, treeId, treeNode, clickFlag) {
            var zTreeObj = $.fn.zTree.getZTreeObj("treeTypeDemo");
            zTreeObj.checkNode(treeNode, !treeNode.checked, true);
            monitorForward.onCheckType(e, treeId, treeNode);
        },
        onClickBack_switchSignal: function (e, treeId, treeNode, clickFlag) {
            var zTreeObj = $.fn.zTree.getZTreeObj("treeTypeDemo_switchSignal");
            zTreeObj.checkNode(treeNode, !treeNode.checked, true);
            monitorForward.onCheckType_switchSignal(e, treeId, treeNode);
        },
        onCheckType: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeTypeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
            }
            monitorForward.getTypeSelect(zTree);
            monitorForward.getTypeCheckedNodes();
        },
        onCheckType_switchSignal: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeTypeDemo_switchSignal");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
            }
            monitorForward.getTypeSelect_switchSignal(zTree);
            monitorForward.getTypeCheckedNodes_switchSignal();
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
            var obj;
            if (responseData.msg) {
                obj = JSON.parse(ungzip(responseData.msg));
            } else {
                obj = JSON.parse(ungzip(responseData));
            }
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
        //组织树预处理加载函数
        ajaxSearchDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            var obj;
            if (responseData.msg) {
                obj = JSON.parse(ungzip(responseData.msg));
            } else {
                obj = JSON.parse(ungzip(responseData));
            }
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
            data = filterQueryResult(data, []);
            return data;
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            /*var treeObj = $.fn.zTree.getZTreeObj(treeId);
            var type = $("#atype").val();
            if (type != "" && (type == 0 || type == 2) && !checkExpand) { //若没有进行树展开操作则执行（只用于全局报警、实时监控）
                monitorForward.findTree();
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
                monitorForward.inquireClick(type);
            }
            listSize--;*/
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (size <= 5000) {
                treeObj.checkAllNodes(true);
            }
            // monitorForward.getCharSelect(treeObj);
        },
        getCharSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $("#search_condition").val(allNodes[0].name);
            } else {
                $("#search_condition").val("");
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
                var parameter = {
                    "groupId": treeNode.id,
                    "isChecked": treeNode.checked,
                    "monitorType": "0"
                }
                json_ajax("post", url, "json", false, parameter, function (data) {
                    var result = data.obj;
                    if (!result) return
                    for(var key in result){
                        if(result.hasOwnProperty(key)){
                            var parentNode = treeObj.getNodeByParam("id", key, null);
                            if (parentNode && !parentNode.children) {
                                parentNode.zAsync = true;
                                treeObj.addNodes(parentNode, 0, result[key]);
                            }
                        }
                    }
                })
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
                        monitorForward.getGroupChild(node, assign);
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

                    json_ajax("post", "/clbs/m/personalized/ico/getVehicleCount",
                        "json", false, {"parentId": treeNode.id, "type": treeNode.type, "webType": 3}, function (data) {
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
            monitorForward.vehicleListId(); // 记录勾选的节点
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
            var list = monitorForward.sortList(test);
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
            return $("#treeForm").validate({
                rules: {
                    search: {
                        regularChar: true,
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
            // var deviceType = $("#deviceType").val();
            var deviceType = '0';
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
                        if (nodes[i].deviceType == "0" || nodes[i].deviceType == "1") {
                            v += nodes[i].id + ",";
                        }
                    } else if (deviceType == nodes[i].deviceType) {
                        v += nodes[i].id + ",";
                    }
                }
            }
            vehicleList = v;
        },
        inquireClick: function (pushType) {
            monitorForward.vehicleListId();
            if (checked) {
                layer.msg("请选择监控对象！");
                return;
            }
            if (!monitorForward.validates()) {
                return;
            }
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
            // statusValue = $('#status').val();
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
            // oldStatusValue = statusValue;
            oldSalarmSource = alarmSource;
            oldSalarmStartTime = alarmStartTime;
            oldAlarmEndTime = alarmEndTime;
            oldVehicleList = vehicleList;
            if (pushType == 0) {
                pushType = -1;
            }
            oldPushType = pushType;
            monitorForward.tableInit();
            /*//发送查询请求
            var url = "/clbs/m/monitorForwardingAlarmSearch/find809Alarms";
            var parameter = {
                "alarmSource": alarmSource,
                "alarmType": typePos,
                "alarmStartTime": alarmStartTime,
                "alarmEndTime": alarmEndTime,
                "vehicleIds": vehicleList,
            };
            json_ajax("POST", url, "json", true, parameter, function (data) {
                monitorForward.getCallback(data, parameter)
            });*/
        },
        // switchSignalAlarmSearchClick: function (pushType) {
        //     monitorForward.vehicleListId();
        //     if (checked) {
        //         layer.msg("请选择监控对象！");
        //         return;
        //     }
        //     if (!monitorForward.switchSignalAlarmValidates()) {
        //         return;
        //     }
        //     if (typePos_switchSignal == '' || typePos_switchSignal == null) {
        //         layer.msg("请选择报警类型！");
        //         return;
        //     }
        //     statusValue_switchSignal = $('#status_switchSignal').val();
        //     var timeInterval = $('#switchSignaltimeInterval').val().split('--');
        //     alarmStartTime_switchSignal = timeInterval[0];
        //     alarmEndTime_switchSignal = timeInterval[1];
        //     oldVehicleList_switchSignal = vehicleList;
        //     if (pushType == 0) {
        //         pushType = -1;
        //     }
        //     oldPushType = pushType;
        //     //发送查询请求
        //     var url = "/clbs/v/switchSignalAlarm/list";
        //     var parameter = {
        //         "alarmTypeNames": typePos_switchSignal,
        //         "status": statusValue_switchSignal,
        //         "startTime": alarmStartTime_switchSignal,
        //         "endTime": alarmEndTime_switchSignal,
        //         "vehicleIds": vehicleList,
        //         "pushType": pushType
        //     };
        //     json_ajax("POST", url, "json", true, parameter, monitorForward.getSwitchSignalAlarmCallback);
        //
        // },
        alarmDataTable: function () {
            var url = "/clbs/m/monitorForwardingAlarmSearch/find809Alarms";
            var parameter = {
                "alarmSource": oldSalarmSource,
                "alarmType": oldAlarmType,
                // "status": oldStatusValue,
                "alarmStartTime": oldSalarmStartTime,
                "alarmEndTime": oldAlarmEndTime,
                "vehicleIds": oldVehicleList,
                "pushType": oldPushType
            };
            json_ajax("POST", url, "json", true, parameter, function (data) {
                if (data.success == true) {
                    myTable.refresh();
                    //myTable.requestData();
                }
            });
        },
        // alarmDataTable_switchSignal: function () {
        //     var url = "/clbs/v/switchSignalAlarm/list";
        //     var parameter = {
        //         "alarmTypeNames": typePos_switchSignal,
        //         "status": statusValue_switchSignal,
        //         "startTime": alarmStartTime_switchSignal,
        //         "endTime": alarmEndTime_switchSignal,
        //         "vehicleIds": oldVehicleList_switchSignal,
        //         "pushType": oldPushType
        //     };
        //     json_ajax("POST", url, "json", true, parameter, function (data) {
        //         if (data.success == true) {
        //             SwitchSignalTable.refresh();
        //             // myTable.requestData();
        //         }
        //     });
        // },
        exportAlarm: function () {
            var length = $("#dataTable tbody tr").find("td").length;
            if (length > 1) {
                if(getRecordsNum() > 60000){
                    return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
                }
                //layer.msg('正在处理导出数据,请耐心等待', {icon: 16, time: false, shade: [0.1, true], skin: "layui-layer-border layui-layer-hui"});
                var paramer = {
                    alarmSource : alarmSource,
                    alarmType : typePos,
                    alarmStartTime : alarmStartTime,
                    alarmEndTime : alarmEndTime,
                    vehicleIds : vehicleList
                };
                var url = "/clbs/m/monitorForwardingAlarmSearch/export";
                json_ajax("post", url, "json", true, paramer, function (result) {
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
            } else {
                layer.msg('无数据可以导出')
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
                            monitorForward.getStatue(url);
                        }, 1000);
                    }
                }
            });
        },
        getTable: function (table) {
            myTable = $(table).DataTable({
                "destroy": true,
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
                "dom": "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
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
        },
        tableInit: function () {
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
                    "data": "monitorName",
                    "class": "text-center"
                }, {
                    "data": "plateColor",
                    "class": "text-center",
                    render: function (data) {
                        if(!data) return '-'
                        if(typeof data == 'number'){
                            return getPlateColor(data)
                        }
                        return data
                    }
                }, {
                    "data": "groupName",
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
                    "data": "professionalsName",
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
                        if (row.professionalsName != null) {
                            str = row.professionalsName;
                        }
                        var dataString = "" + row.startTimeStr + "|" + row.alarmType + "|" + row.description + "|" + row.id + "|" + row.monitorName + "|" + row.swiftNumber + "|" + row.monitorType + "|" + row.monitorId + "|" + str + "|" + row.assignmentName + "|" + row.alarmSource + "|" + row.alarmEndTime + "|" + row.protocolType + "|" + row.riskEventId + "|" + row.time + "|" + row.alarmStartTimeStr + "";
                        if (row.status == 0) {
                            if ($("#alarmRole").val() == 'true') {
                                return '<a onclick="monitorForward.warningManage(\'' + dataString + '\')">未处理</a>'
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
                        if (row.riskEventId != null) {
                            return row.severityName;
                        }
                        return '-';
                    }
                },
                    {
                        "data": "alarmSourceStr",
                        "class": "text-center"
                        // render: function (data, type, row, meta) {
                        //     if (data == 1) {
                        //         return "平台报警";
                        //     } else {
                        //         return "终端报警";
                        //     }
                        // }
                    }, {
                        "data": "speed",
                        "class": "text-center",
                        render: function (data, type, row, meta) {
                            data = monitorForward.fiterNumber(data);
                            return data;
                        }

                    }, { //道路类型
                        "data": "roadTypeStr",
                        "class": "text-center",
                        render: function (data, type, row, meta) {
                            var roadType = row.roadTypeStr;
                            if (roadType == "" || roadType == null) {
                                return '-';
                            }
                                return roadType;

                        }

                    }, { //平台限速
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
                    }
                    , { //4、超速时长
                        "data": "overSpeedTime",
                        "class": "text-center",
                        render: function (data, type, row, meta) {
                            if ((row.alarmType == 76 || row.alarmType == 164) && row.overSpeedTime != null && row.overSpeedTime != '') {
                                return row.overSpeedTime;
                            }
                            return '-';
                        }

                    },
                    // {
                    //     "data": "startTime",
                    //     "class": "text-center",
                    // }, {
                    //     "data": "endTime",
                    //     "class": "text-center",
                    // },
                    {
                        "data": "startTimeStr",
                        "class": "text-center",
                    },
                    // {
                    //     "data": "alarmStartLocation",
                    //     "class": "text-center",
                    //     render: function (data, type, row, meta) {
                    //         if (data != null) {
                    //             var str = data.split(",");
                    //             var coord = str[0];
                    //             var indexOf = coord.indexOf(".") + 4;
                    //             var startLong = coord.substring(0, indexOf);
                    //             return startLong;
                    //         } else {
                    //             return "-";
                    //         }
                    //     }
                    //
                    // }, {
                    //     "data": "alarmStartLocation",
                    //     "class": "text-center",
                    //     render: function (data, type, row, meta) {
                    //         if (data != null) {
                    //             var str = data.split(",");
                    //             var coord = str[1];
                    //             var indexOf = coord.indexOf(".") + 4;
                    //             var startLong = coord.substring(0, indexOf);
                    //             return startLong;
                    //         } else {
                    //             return "-";
                    //         }
                    //     }
                    //
                    // },
                    {
                        "data": "alarmLatitude",
                        "class": "text-center",
                        render: function (data, type, row, meta) {
                            if (data != null) {
                                // var str = data.split(",");
                                // var coord = str[0];
                                // var indexOf = coord.indexOf(".") + 4;
                                // var startLong = coord.substring(0, indexOf);
                                return data;
                            }
                                return "-";

                        }

                    }, {
                        "data": "alarmLongitude",
                        "class": "text-center",
                        render: function (data, type, row, meta) {
                            if (data != null) {
                                // var str = data.split(",");
                                // var coord = str[1];
                                // var indexOf = coord.indexOf(".") + 4;
                                // var startLong = coord.substring(0, indexOf);
                                return data;
                            }
                                return "-";

                        }

                    },
                    // {
                    //     "data": "alarmStartSpecificLocation",
                    //     "class": "text-center"
                    // }, {
                    //     "data": "alarmEndSpecificLocation",
                    //     "class": "text-center"
                    // },
                    {
                        "data": "alarmAddress",
                        "class": "text-center"
                    },
                    {
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
                        "data": "omissionAlarmType",
                        "class": "text-center",
                        render: function (data, type, row, meta) {
                            if (data) {
                                return data;
                            }
                            return '';
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
                var ajaxDataParamFun = function (d) {
                    d.alarmSource = alarmSource;
                    d.alarmType = typePos;
                    d.alarmStartTime = alarmStartTime;
                    d.alarmEndTime = alarmEndTime;
                    d.vehicleIds = vehicleList;
                };
                //表格setting
                var url = "/clbs/m/monitorForwardingAlarmSearch/getAlarmPage";
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
                        var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
                        api.column(0).nodes().each(function (cell, i) {
                            cell.innerHTML = startIndex + i + 1;
                        });

                    },
                };
                //创建表格
                myTable = new TG_Tabel.createNew(setting);
                myTable.init();
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
            $('#timeInterval').val(monitorForward.getQueryStartTime() + '--' + monitorForward.getQueryEndTime());
        },
        //当前时间
        getSwitchSignalTheCurrentTime: function () {
            $('#switchSignaltimeInterval').val(monitorForward.getQueryStartTime() + '--' + monitorForward.getQueryEndTime());
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
            // var deviceType = $("#deviceType").val();

            var deviceType = 0;

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
                monitorForward.showFuelType();
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
//         	monitorForward.vehicleListId();
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
                    url: "/clbs/m/personalized/ico/vehicleTreeFuzzy",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    sync: false,
                    otherParam: {"type": "multiple", "queryParam": param, "webType": 3, "queryType": $('#queryType ').val()},
                    dataFilter: monitorForward.ajaxSearchDataFilter
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
                    beforeClick: monitorForward.beforeClickVehicle,
                    onAsyncSuccess: monitorForward.fuzzyZTreeOnAsyncSuccess,
                    //beforeCheck: monitorForward.fuzzyZTreeBeforeCheck,
                    onCheck: monitorForward.fuzzyOnCheckVehicle,
                    //onExpand: monitorForward.zTreeOnExpand,
                    //onNodeCreated: monitorForward.zTreeOnNodeCreated,
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
            monitorForward.vehicleListId(); // 记录勾选的节点
        },
        initSearch: function () {
            var type = $("#atype").val();
            if (type == "2" || type == "0") {
                // $("#status").val(0);
                setTimeout("monitorForward.inquireClick(" + type + ")", 1500);
            }
        },
        // 以下报警处理功能
        warningManage: function (data) {
            $("#warningManage").val("alarm");
            $("#alarmRemark").val("");
            $("#alarm-remark").show();
            $("#smsTxt").val("");
            $("#time").val("");
            $("#warningDescription").text("");
            var dataArray = data.split('|');

            riskEventId = dataArray[13];
            Time = dataArray[14];
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
            if (dataArray[12] == '2301') {
                $('#warningManageAffirm').hide();
            } else {
                $('#warningManageAffirm').show();
            }
            if (riskEventId == "" || riskEventId == "null") {
                $('#warningManageAffirm').show();
            } else {
                $('#warningManageAffirm').hide();
            }
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
                if (dataArray[13] != null || dataArray[13] != "") {
                    var str = dataArray[5];
                    var swiftNumber = str.split(",");
                    $("#sno").val(swiftNumber[0]);
                }

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
            $("#alarmStartTime_809").text(dataArray[15]);
            $("#warningDescription").text(dataArray[2]);
            $("#vUuid").val(dataArray[7]);
            $("#simcard").val(sim);
            $("#device").val(device);
            $("#warningType").val(dataArray[1]);
            $('#eventId').val(dataArray[3]);
        },
        // 监听下发
        listenForAlarm: function () {
            if (riskEventId == "null") {
                riskEventId = ""
            }
            if (monitorForward.listenValidate()) {
                // 为车id赋值
                var vehicleId = $("#vUuid").val();
                $("#vidforAlarmListen").val(vehicleId);
                $("#brandListen").val($("#warningCarName").text());
                $("#alarmListen").val($("#warningType").val());
                $("#startTimeListen").val($("#alarmStartTime_809").text());

                $("#simcardListen").val($('#simcard').val());
                $("#deviceListen").val($("#device").val());
                $("#snoListen").val($("#sno").val());
                $("#handleTypeListen").val("监听");
                $("#descriptionListen").val($("#warningDescription").text());
                $("#remarkListen").val($("#alarmRemark").val());
                $("#riskEventIdListen").val(riskEventId);
                $("#goListeningForAlarm").attr("disabled", "disabled");

                var isSwitchSignal = $("#warningManage").val();
                $("#listeningAlarm").ajaxSubmit(function (data) {
                    if (JSON.parse(data).success) {
                        layer.msg(publicIssuedSuccess);
                        if (isSwitchSignal == 'alarm_switchSignal') {
                            monitorForward.alarmDataTable_switchSignal();
                        } else {
                            monitorForward.alarmDataTable();
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
            if (riskEventId == "null") {
                riskEventId = "";
            }
            if (monitorForward.photoValidateForAlarm()) {
                $("#vidforAlarm").val($("#vUuid").val());
                $("#alarmPhoto").val($("#warningType").val());
                $("#startTimePhoto").val($("#alarmStartTime_809").text());
                $("#brandPhoto").val($("#warningCarName").text());

                $("#simcardPhoto").val($('#simcard').val());
                $("#devicePhoto").val($("#device").val());
                $("#snoPhoto").val("12");
                $("#handleTypePhoto").val("拍照");
                $("#description-photo").val($("#warningDescription").text());
                $("#remark-photo").val($("#alarmRemark").val());
                $("#riskEventId-photo").val(riskEventId);
                var isSwitchSignal = $("#warningManage").val();
                $("#goPhotographsForAlarm").attr("disabled", "disabled");
                $("#takePhotoForAlarm").ajaxSubmit(function (data) {
                    if (JSON.parse(data).success) {
                        layer.msg(publicIssuedSuccess);
                        if (isSwitchSignal == 'alarm_switchSignal') {
                            monitorForward.alarmDataTable_switchSignal();
                        } else {
                            monitorForward.alarmDataTable();
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
            if (riskEventId == "null") {
                riskEventId = ""
            }
            // 为车id赋值
            $("#vidSendTxtForAlarm").val($("#vUuid").val());
            $("#brandTxt").val($("#warningCarName").text());
            $("#alarmTxt").val($("#warningType").val());
            $("#startTimeTxt").val($("#alarmStartTime_809").text());

            $("#simcardTxt").val($('#simcard').val());
            $("#deviceTxt").val($("#device").val());
            $("#snoTxt").val($("#sno").val());
            $("#description-Txt").val($("#warningDescription").text());
            $("#remark-Txt").val($("#alarmRemark").val());
            $("#handleTypeTxt").val("下发短信");
            $("#deviceTypeTxt").val(deviceTypeTxt);
            $("#riskEventId-Txt").val(riskEventId);
            var isSwitchSignal = $("#warningManage").val();
            var smsTxt = $("#smsTxt").val();
            if (smsTxt == null || smsTxt.length == 0) {
                monitorForward.showErrorMsg("下发内容不能为空", "smsTxt");
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
                        monitorForward.alarmDataTable_switchSignal();
                    } else {
                        monitorForward.alarmDataTable();
                    }
                    $("#warningManage").modal('hide')
                } else {
                    layer.msg(publicIssuedFailure);
                }
            });
            $("#goTxtSendForAlarm").removeAttr("disabled");
        },
        handleAlarm: function (handleType) {
            var startTime = $("#alarmStartTime_809").text();
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

            if (riskEventId !== "" && riskEventId !== "null" && riskEventId != null) {
                if (typeof Time == "number") {
                    startTime = formatDateAll(Time);
                }
            }
            var data = {
                'id': eventId,
                "vehicleId": vehicleId,
                "plateNumber": plateNumber,
                "alarm": alarm,
                "handleType": handleType,
                "startTime": startTime,
                "description": description,
                "simcard": simcard,
                "device": device,
                "sno": sno,
                "remark": remark,
                "riskEventId": riskEventId == "null" ? "" : riskEventId
            };
            json_ajax("POST", url, "json", false, data, null);
            // 报警处理完毕后，延迟3秒进行结果查询
            setTimeout(pagesNav.gethistoryno, 3000);
            if (isSwitchSignal == 'alarm_switchSignal') {
                monitorForward.alarmDataTable_switchSignal();
            } else {
                monitorForward.alarmDataTable();
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
                return data;
            }
                var data = data.toString();
                data = parseFloat(data);
                return data;

        },
        // 应答
        responseSocket: function () {
            monitorForward.isGetSocketLayout();
        },
        isGetSocketLayout: function () {
            setTimeout(function () {
                if (webSocket.conFlag) {
                    webSocket.subscribe(headers, '/user/topic/check', monitorForward.updateTable, "/app/vehicle/inspect", null);
                } else {
                    monitorForward.isGetSocketLayout();
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
                monitorForward.showErrorMsg("应答不能为空", "answer");
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
            monitorForward.removeClass();
            $(this).addClass("active");
            $('#alarmSearchTableBox').removeClass('active').addClass('active');
            $('#switchSignalAlarmTableBox').removeClass('active');
        },
        switchSignalAlarmTabClick: function () {
            monitorForward.removeClass();
            $('#switchSignalAlarmTableBox').removeClass('active').addClass('active');
            $('#alarmSearchTableBox').removeClass('active');

        },
        removeClass: function () {
            var dataList = $(".dataTableShow");
            for (var i = 0; i < 2; i++) {
                dataList.children("li").removeClass("active");
            }
        },
    }
    $(function () {
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'search_condition') {
                monitorForward.init();
            }
        });
        //初始化组织结构
        monitorForward.init();
        monitorForward.getTable('#dataTable');
        monitorForward.responseSocket();
        monitorForward.validates();
        //报警类型树结构
        monitorForward.getlAlarmType();
        // monitorForward.alarmTreeInit();
        //报警类型
        monitorForward.showFuelType();
        //设置当前时间显示
        $('#timeInterval').dateRangePicker({
            dateLimit: 31,
            element: '#inquireClick'
        });
        // $('#switchSignaltimeInterval').dateRangePicker({
        //     element:'#switchSignalAlarmSearchClick'
        // });
        //当前时间
        monitorForward.getsTheCurrentTime();
        // 开关信号位查询时间
        monitorForward.getSwitchSignalTheCurrentTime();
        //改变勾选框
        // $("#deviceType").change(function () {
        //     monitorForward.getlAlarmType();
        //     $("#groupSelect").val("全部");
        //     typePos = "-1";
        //     /*monitorForward.alarmListChange();*/
        // });
        // 判断报警来源是否被修改
        $("#alarmSource").change(function () {
            $("#groupSelect").val("全部");
            typePos = "-1";
            monitorForward.getlAlarmType();
            /*monitorForward.alarmListChange();*/
        });
        //monitorForward.initSearch();

        $("#warningManageClose").click(function () {
            $("#warningManage").modal('hide')
        });
        $("#warningManageListening").bind("click", function () {
            monitorForward.showOrhide(3)
        });
        $("#warningManagePhoto").bind("click", function () {
            monitorForward.showOrhide(0)
        });
        $("#warningManageSend").bind("click", function () {
            monitorForward.showOrhide(1)
        });
        $("#goListeningForAlarm").bind("click", function () {
            monitorForward.listenForAlarm()
        });
        $("#goPhotographsForAlarm").bind("click", function () {
            monitorForward.takePhotoForAlarm()
        });
        $("#goTxtSendForAlarm").bind("click", function () {
            monitorForward.goTxtSendForAlarm()
        });
        $("#warningManageAffirm").bind("click", function () {
            monitorForward.handleAlarm("人工确认报警")
        });
        $("#warningManageCancel").bind("click", function () {
            monitorForward.handleAlarm("不做处理")
        });
        $("#warningManageFuture").bind("click", function () {
            monitorForward.handleAlarm("将来处理")
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
                    monitorForward.init();
                } else {
                    monitorForward.searchVehicleTree(param);
                }
            }, 500);
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
                        monitorForward.init();
                    } else {
                        monitorForward.searchVehicleTree(param);
                    }
                }, 500);
            }).bind("blur", function () {
                clearInterval(search);
            });
        }
        //IE9 end
        //点击查询执行
        $("#inquireClick").bind("click", function () {
            monitorForward.inquireClick(0)
        });
        //点击查询执行
        // $("#switchSignalAlarmSearchClick").bind("click", function () {
        //     monitorForward.switchSignalAlarmSearchClick(0)
        // });
        //点击导出执行
        $("#alarmExport").bind("click", monitorForward.exportAlarm);
        // $("#alarmExport_switchSignal").bind("click", monitorForward.exportAlarm_swithcSignal);
        $("#endTime").on('click', monitorForward.endTimeStyle);
        // 应答确定
        $('#parametersResponse').on('click', monitorForward.platformMsgAck);
        $("#groupSelect").bind("click", showMenuContent);
        $("#groupSelect_switchSignal").bind("click", showMenuContent);

        $('#alarmSearchTab').bind("click", monitorForward.alarmSearchTabClick);
        $('#switchSignalAlarmTab').bind("click", monitorForward.switchSignalAlarmTabClick);

        $('#queryType').on('change', function () {
            if ($('#search_condition').val() != '') {
                $('#search_condition').val('');
                monitorForward.init();
            }
            // alarmSearchPages.searchVehicleTree('');
        });
    })
}(window, $))