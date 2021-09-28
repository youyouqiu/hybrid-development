(function (window, $) {
    var checked = true;
    var startTime, endTime;//开始时间，结束时间
    var vehicleList = [];
    var setChar; // 树设置
    var checkFlag = false; //判断组织节点是否是勾选操作
    var zTreeIdJson = {};
    var size;//当前权限监控对象数量
    var typePos = '-1';
    var checkExpand = false;//判断是否展开过车辆树（只用于全局报警、实时监控判断）
    var listSize = -1;//记录有多少个分组需要展开（只用于全局报警、实时监控）
    var isIncludeQuitPeople = 1; // 是否包含离职 0:不包含; 1:包含;

    alarmSearchPages = {
        //组织树
        init: function () {
            //组织树
            setChar = {
                async: {
                    url: alarmSearchPages.getIcoTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    sync: false,
                    otherParam: {"type": "multiple", "isIncludeQuitPeople": isIncludeQuitPeople},
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
                return "/clbs/a/businessReport/alarmSearch/alarmSearchTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/a/businessReport/alarmSearch/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=monitor";
            }
        },
        //报警类型
        getlAlarmType: function () {
            var typeTree = [{
                name: '全部',
                open: true,
                pos: '-1',
                isParent: true,
                checked: true, // 从实时视频or监控跳转过来,一定会带入alarmType,因此默认勾选全部
                children:[
                    {
                        name: '上班未到岗',
                        isParent: false,
                        checked: true,
                        pos:152
                    },{
                        name: '上班离岗',
                        isParent: false,
                        checked: true,
                        pos:153
                    },{
                        name: '超时长停留',
                        isParent: false,
                        checked: true,
                        pos:154
                    },{
                        name: '任务未到岗',
                        isParent: false,
                        checked: true,
                        pos:155
                    },{
                        name: '任务离岗',
                        isParent: false,
                        checked: true,
                        pos:156
                    },{
                        name: 'SOS报警',
                        isParent: false,
                        checked: true,
                        pos:0
                    },
                ]
            }];
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
                    beforeClick: alarmSearchPages.beforeClickType,
                    onCheck: alarmSearchPages.onCheckType,
                    onExpand: alarmSearchPages.zTreeOnExpand,
                    onClick: alarmSearchPages.onClickBack
                }
            };
            var typeTreeObj = $.fn.zTree.init($("#treeTypeDemo"), setting, typeTree);
            alarmSearchPages.getTypeCheckedNodes();
        },
        beforeClickType: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeTypeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
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
        getTypeSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $("#groupSelect").val(allNodes[0].name);
            } else {
                $("#groupSelect").val("");
            }
        },
        getTypeCheckedNodes: function () {
            typePos = [];
            var typezTree = $.fn.zTree.getZTreeObj("treeTypeDemo"),
                nodes = typezTree.getCheckedNodes(true),
                v = "", typeMsg = "";
            var noCheckLen = typezTree.getCheckedNodes(false).length;
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].pos !== "" && nodes[i].pos !== undefined && nodes[i].pos != null) {
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
        fuzzyZTreeOnAsyncSuccess: function (event, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.expandAll(true);
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
                var alarmTypeTree = $.fn.zTree.getZTreeObj("treeTypeDemo");
                alarmTypeTree.checkAllNodes(true);
                alarmSearchPages.getTypeSelect(alarmTypeTree);
                alarmSearchPages.getTypeCheckedNodes();
                alarmSearchPages.inquireClick(type);
            }
            listSize--;
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
                            var zTreeIdJsonElement = zTreeIdJson[pid];
                            if (zTreeIdJsonElement !== undefined && zTreeIdJsonElement !== null) {
                                var parentTid = zTreeIdJsonElement[0];
                                var parentNode = treeObj.getNodeByTId(parentTid);
                                if (parentNode.children === undefined) {
                                    treeObj.addNodes(parentNode, []);
                                }
                            }

                        });
                    }
                })
            }
            if (vehicleList.length > 0) {
                alarmSearchPages.checkCurrentNodes(treeNode);
            }
        },
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

                console.log(nodesLength);
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
                    v += nodes[i].id + ",";
                }
            }
            vehicleList = v;
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
        inquireClick: function (pushType) {
            alarmSearchPages.vehicleListId();
            if (checked) {
                layer.msg("请选择监控对象！");
                return;
            }
            if (!alarmSearchPages.validates()) {
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
            statusValue = $('#status').val();
            alarmSource = $("#alarmSource").val();
            var timeInterval = $('#timeInterval').val().split('--');
            alarmStartTime = timeInterval[0];
            alarmEndTime = timeInterval[1];
            if(typePos == '-1'){
                var typezTree = $.fn.zTree.getZTreeObj("treeTypeDemo");
                var nodes = typezTree.getCheckedNodes(true);
                var v = "";
                var index = 0;
                for (var i = 0, l = nodes.length; i < l; i++) {
                    if (nodes[i].pos !== "" && nodes[i].pos !== undefined && nodes[i].pos !== null && nodes[i].pos != '-1') {
                        v += ( index == 0 ? nodes[i].pos : ("," + nodes[i].pos) );
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
            //发送查询请求
            var url = "/clbs/a/businessReport/alarmSearch/queryDispatchAlarm";
            var parameter = {
                "alarmSource": alarmSource,
                "alarmType": typePos,
                "status": statusValue,
                "alarmStartTime": alarmStartTime,
                "alarmEndTime": alarmEndTime,
                "monitorIds": vehicleList,
                "pushType": pushType
            };
            json_ajax("POST", url, "json", true, parameter, alarmSearchPages.initTable);

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
        //模糊查询
        inputTextAutoSearch: function () {
            search_ztree('treeDemo', 'search_condition', 'vehicle');
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData));
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
                    url: "/clbs/a/businessReport/alarmSearch/monitorTreeFuzzy",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    sync: false,
                    otherParam: {"type": "multiple", "queryParam": param, "webType": "1","isIncludeQuitPeople": isIncludeQuitPeople},
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
        //当前时间
        getsTheCurrentTime: function () {
            $('#timeInterval').val(alarmSearchPages.getQueryStartTime()+ '--' + alarmSearchPages.getQueryEndTime());
        },
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
        },
        chooseAlarmPointChange: function(){
            if ($('#chooseAlarmPoint').prop('checked')){
                isIncludeQuitPeople = 1;
            }   else{
                isIncludeQuitPeople = 0;
            }
            alarmSearchPages.init();
        },
        //table
        initTable: function (data) {
            if (data.success == true) {
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
                    "data": "name",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined) {
                            return '';
                        }
                        else {
                            return data;
                        }
                    }
                }, {
                    "data": "assignmentName",
                    "class": "text-center"
                }, {
                    "data": "status",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        var dataString = "" + row.monitorId + "|" + row.monitorName + "|" + row.alarmType + "|" + row.startTime + "";
                        if (row.status == 0) {
                            if ($("#alarmRole").val() == 'true') {
                                return '<a onclick="alarmSearchPages.warningManage(\'' + dataString + '\')">未处理</a>'
                            } else {
                                return '未处理'
                            }
                        } else {
                            return "已处理";
                        }
                    }
                }, {
                    "data": "description",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined) {
                            return '';
                        }
                        else {
                            return data;
                        }
                    }
                },  {
                    "data": "startTime",
                    "class": "text-center",
                }, {
                    "data": "endTime",
                    "class": "text-center",
                }, {
                    "data": "alarmDuration",
                    "class": "text-center",
                },{
                    "data": "alarmStartSpecificLocation",
                    "class": "text-center",
                }, {
                    "data": "alarmEndSpecificLocation",
                    "class": "text-center",
                },  {
                    "data": "fenceType",
                    "class": "text-center",
                }, {
                    "data": "fenceName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined) {
                            return ''
                        }
                        else {
                            return data;
                        }
                    }
                }, {
                    "data": "personName",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined) {
                            return ''
                        }
                        else {
                            return data;
                        }
                    }
                }, {
                    "data": "handleTimeStr",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined || row.status == "0") {
                            return ''
                        }
                        else {
                            return data;
                        }
                    }
                }, {
                    "data": "remark",
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        if (data == null || data == undefined) {
                            return ''
                        }
                        else {
                            return data;
                        }
                    }
                }
                ];
                //表格setting
                var url = "/clbs/a/businessReport/alarmSearch/getDispatchAlarmList";
                var setting = {
                    listUrl: url,
                    columnDefs: columnDefs, //表格列定义
                    columns: columns, //表格列
                    dataTableDiv: 'dataTable', //表格
                    pageable: true, //是否分页
                    showIndexColumn: true, //是否显示第一列的索引列
                    // enabledChange: true,
                    getAddress: false,//是否逆地理编码
                    address_index: 16,
                    // drawCallbackFun: function () {
                    //     var api = myTable.dataTable;
                    //     var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
                    //     api.column(0).nodes().each(function (cell, i) {
                    //         cell.innerHTML = startIndex + i + 1;
                    //     });
                    //
                    // },
                };
                //创建表格
                myTable = new TG_Tabel.createNew(setting);
                myTable.init();
            }
        },
        warningManage: function (data) {
            var dataArray = data.split('|');
            $("#monitorId").val("");
            $("#monitorName").val("");
            $("#alarmType").val("");
            $("#alarmStartTime").val("");
            $('#handleDescription').val('');
            $('#warningManage').modal('show');
            $("#monitorId").val(dataArray[0]);
            $("#monitorName").val(dataArray[1]);
            $("#alarmType").val(dataArray[2]);
            $("#alarmStartTime").val(dataArray[3]);
        },
        exportAlarm: function () {
            var length = $("#dataTable tbody tr").find("td").length;
            if (length > 1) {
                //layer.msg('正在处理导出数据,请耐心等待', {icon: 16, time: false, shade: [0.1, true], skin: "layui-layer-border layui-layer-hui"});
                var url = "/clbs/a/businessReport/alarmSearch/exportDispatchAlarm";
                exportExcelUseFormGet(url,[]);
            }
        },
        handleAlarm: function () {
            var monitorId = $("#monitorId").val();
            var monitorName = $("#monitorName").val();
            var alarmType = $("#alarmType").val();
            var alarmStartTime = $("#alarmStartTime").val();
            var handleDescription = $('#handleDescription').val();
            var data = {
                "vehicleId": monitorId,
                "plateNumber": monitorName,
                "alarm": alarmType,
                "startTime": alarmStartTime,
                "remark":handleDescription
            };
            json_ajax("POST", "/clbs/a/businessReport/alarmSearch/handleDispatchAlarm", "json", false, data, null);
            // 报警处理完毕后，延迟3秒进行结果查询
            setTimeout(pagesNav.gethistoryno, 3000);
            alarmSearchPages.alarmDataTable();
            $('#warningManage').modal('hide');
        },
        alarmDataTable: function () {
            var timeInterval = $('#timeInterval').val().split('--');
            var url = "/clbs/a/businessReport/alarmSearch/queryDispatchAlarm";
            var parameter = {
                "alarmType": typePos,
                "status": $('#status').val(),
                "alarmStartTime": timeInterval[0],
                "alarmEndTime": timeInterval[1],
                "monitorIds": vehicleList
            };
            json_ajax("POST", url, "json", true, parameter, function (data) {
                if (data.success == true) {
                    myTable.refresh();
                }
            });
        },
    }
    $(function () {
        //初始化组织结构
        alarmSearchPages.init();
        //报警类型
        alarmSearchPages.getlAlarmType();
        //设置当前时间显示
        $('#timeInterval').dateRangePicker({
            element:'#inquireClick'
        });
        alarmSearchPages.getsTheCurrentTime();
        //点击导出执行
        $("#alarmExport").bind("click", alarmSearchPages.exportAlarm);
        $("#handleAlarm").bind("click", alarmSearchPages.handleAlarm);
        //输入框清空
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'search_condition') {
                alarmSearchPages.init();
            }
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
        //IE9
        if (navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") == "MSIE9.0") {
            var search;
            $("#search_condition").bind("focus", function () {
                search = setInterval(function () {
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
        $('#groupSelect').on('click',showMenuContent);
        //查询
        $("#inquireClick").bind("click", function () {
            alarmSearchPages.inquireClick(0)
        });
        //导出
        $("#alarmExport").bind("click", alarmSearchPages.exportAlarm);

        $('#chooseAlarmPoint').bind('change', alarmSearchPages.chooseAlarmPointChange)
    })
})(window, $)