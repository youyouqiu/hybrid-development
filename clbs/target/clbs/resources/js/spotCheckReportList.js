(function (window, $) {
    //车辆列表
    var vehicleListTwo = [];
    var vehicleListThree = [];
    var vehicleListFour = [];
    //车辆id列表
    var vehicleIdTwo = [];
    var vehicleIdThree = [];
    var vehicleIdFour = [];
    //表格
    var myTable;
    var myTableTwo;
    var myTableThree;
    var myTableFour;
    //开始时间
    var startTime;
    //结束时间
    var endTime;
    var checkFlag = false; //判断组织节点是否是勾选操作
    var size; //当前权限监控对象数量
    var groupId = [];

    //逆地址解析
    var startLoc = [];
    var endLoc = [];
    var addressMsg = [];

    // 车辆抽查数量统计表
    var panelFlag1 = true;
    var fuzzyParm1 = ''; //模糊搜索参数
    // 车辆抽查明细表
    var panelFlag2 = true;
    var fuzzyParm2 = ''; //模糊搜索参数
    // 用户抽查车辆数量及百分比统计报表
    var panelFlag3 = true;
    var fuzzyParm3 = ''; //模糊搜索参数

    var zTreeIdJson = {};
    // 道路运输企业抽查车辆数量统计报表
    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于100自动勾选
    var isSearch2 = true;
    var isSearch3 = true;
    var fuzzyParm = ''; //模糊搜索参数
    var dbValue = false //树双击判断参数
    //公共方法提取
    publicFun = {
        init: function (treeId) {
            var setting = {
                async: {
                    url: publicFun.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": "multiple"
                    },
                    otherParam: {
                        "icoType": "0"
                    },
                    dataFilter: publicFun.ajaxDataFilter
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
                    beforeClick: publicFun.beforeClickVehicle,
                    onCheck: publicFun.onCheckVehicle,
                    beforeCheck: publicFun.zTreeBeforeCheck,
                    onExpand: publicFun.zTreeOnExpand,
                    onNodeCreated: publicFun.zTreeOnNodeCreated,
                    onAsyncSuccess: publicFun.zTreeOnAsyncSuccess,
                    onDblClick: publicFun.onDblClickVehicle,

                }
            };

            if (treeId == 'treeDemoTwo') {
                $.fn.zTree.init($("#treeDemoTwo"), setting, null);
                panelFlag1 = false;
            } else if (treeId == 'treeDemoThree') {
                $.fn.zTree.init($("#treeDemoThree"), setting, null);
                panelFlag2 = false;
            } else if (treeId == 'treeDemoFour') {
                $.fn.zTree.init($("#treeDemoFour"), setting, null);
                panelFlag3 = false;
            } else {
                $.fn.zTree.init($("#treeDemo"), setting, null);
            }

            $("#panelTab2").on("click", function () {
                if (panelFlag1) {
                    $.fn.zTree.init($("#treeDemoTwo"), setting, null);
                    panelFlag1 = false;
                }
            });
            $("#panelTab3").on("click", function () {
                if (panelFlag2) {
                    $.fn.zTree.init($("#treeDemoThree"), setting, null);
                    panelFlag2 = false;
                }
            });
            $("#panelTab4").on("click", function () {
                if (panelFlag3) {
                    $.fn.zTree.init($("#treeDemoFour"), setting, null);
                    panelFlag3 = false;
                }
            });
        },
        onDblClickVehicle: function (e, treeId, treeNode) {
            if (!treeNode) return
            dbValue = true
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            publicFun.getCheckedNodes("treeDemo");
            // $("#groupSelect").val(treeNode.name);
            // search_ztree('treeDemo', 'groupSelect', 'group');
            treeObj.checkNode(treeNode, !treeNode.checked, false, true);
        },
        //模糊查询树
        searchVehicleTree: function (param, treeId) {
            ifAllCheck = false; //模糊查询不自动勾选

            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                publicFun.init(treeId);
            } else {
                var type = "";
                if (treeId == "treeDemoTwo") {
                    type = $('#queryType').val();
                }
                if (treeId == "treeDemoThree") {
                    type = $("#queryType1").val();
                }
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {
                            "type": type,
                            "queryParam": param
                        },
                        dataFilter: publicFun.ajaxQueryDataFilter
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
                        beforeClick: publicFun.beforeClickVehicle,
                        onCheck: publicFun.onCheckVehicleFuzzy,
                        onExpand: publicFun.zTreeOnExpand,
                        onNodeCreated: publicFun.zTreeOnNodeCreated
                    }
                };
                if (treeId == 'treeDemoTwo') {
                    $.fn.zTree.init($("#treeDemoTwo"), querySetting, null);
                }
                if (treeId == 'treeDemoThree') {
                    $.fn.zTree.init($("#treeDemoThree"), querySetting, null);
                }
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
            if (treeId == 'treeDemo') {
                return "/clbs/m/basicinfo/enterprise/professionals/tree";
            } else if (treeId == 'treeDemoFour') {
                return "/clbs/c/user/groupTree?type=multiple";
            }

            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }

        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            if (treeId == "treeDemo") {
                size = responseData.length;
                for (var i = 0; i < responseData.length; i++) {
                    responseData[i].open = true;
                }
                return responseData;
            }

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
                    if (treeId == 'treeDemoFour' && data.length > 100) {
                        data[i].checked = false;
                    }
                }
            }
            return data;

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
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            if (treeNode.type === "group") {
                if (treeId != 'treeDemo' && treeId != 'treeDemoFour') {
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
                                var chNodes = result[i]; //获取对应的value
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
                } else {
                    var assign = []; // 当前组织及下级组织的所有分组
                    publicFun.getGroupChild(treeNode, assign);
                    if (assign != null && assign.length > 0) {
                        for (var i = 0; i < assign.length; i++) {
                            var node = assign[i];
                            if (node.type == "assignment" && node.children === undefined) {
                                if (!node.zAsync) { // 判断节点是否进行异步加载，若没有，则先异步加载，避免添加重复节点
                                    treeObj.reAsyncChildNodes(node, "refresh");
                                }
                            }
                        }
                    }
                }
            }
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            var maxSize = 100;
            if (treeId === 'treeDemo') {
                maxSize = GROUP_MAX_CHECK;
            }
            if (size <= maxSize && ifAllCheck) {
                treeObj.checkAllNodes(true);
                var nodes = treeObj.getNodes();
                for (var i = 0; i < nodes.length; i++) { //设置节点展开
                    treeObj.expandNode(nodes[i], true, false, true);
                }
            }
            publicFun.getCharSelect(treeObj);
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (treeId === 'treeDemo') {
                if (!treeNode.checked && !dbValue) {
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo");
                    var nodes = zTree.getNodesByFilter(function (node) {
                        return node;
                    }, false, treeNode); // 仅查找一个节点
                    var nodesLength = nodes.length;
                    if (nodesLength > GROUP_MAX_CHECK) {
                        layer.msg('最多勾选' + GROUP_MAX_CHECK + '个企业' + '<br/>双击名称可选中本组织');
                        flag = false;
                    }
                }
                return flag;
            }
            if (!treeNode.checked) {
                var curType = 'vehicle';
                if (treeId === 'treeDemoFour') {
                    curType = 'people';
                }
                if (treeId != 'treeDemoFour' && (treeNode.type == "group" || treeNode.type == "assignment")) { // 若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj(treeId),
                        nodes = zTree
                        .getCheckedNodes(true),
                        v = "";
                    var nodesLength = 0;
                    json_ajax("post", "/clbs/m/personalized/ico/getVehicleCount",
                        "json", false, {
                            "parentId": treeNode.id,
                            "type": treeNode.type
                        },
                        function (data) {
                            if (data.success) {
                                nodesLength += data.obj;
                            } else {
                                layer.msg(data.msg);
                            }
                        });

                    // 存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    // 节点id
                    var nodeId;
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type == curType) {
                            // 查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                            var nd = zTree.getNodeByParam("tId", nodes[i].tId, treeNode);
                            if (nd == null && $.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength += ns.length;
                } else if (treeNode.type == curType) { // 若勾选的为监控对象
                    var zTree = $.fn.zTree.getZTreeObj(treeId),
                        nodes = zTree
                        .getCheckedNodes(true),
                        v = "";
                    var nodesLength = 0;
                    // 存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    // 节点id
                    var nodeId;
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type == curType) {
                            if ($.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength = ns.length + 1;
                }
                if (nodesLength > TREE_MAX_CHILDREN_LENGTH) {
                    if (treeId == 'treeDemoFour') {
                        layer.msg('最多勾选' + TREE_MAX_CHILDREN_LENGTH + '个用户');
                    } else {
                        layer.msg('最多勾选' + TREE_MAX_CHILDREN_LENGTH + '个监控对象');
                    }
                    flag = false;
                }
            }
            if (flag) {
                // 若组织节点已被勾选，则是勾选操作，改变勾选操作标识
                if (treeNode.type == "group" && !treeNode.checked) {
                    checkFlag = true;
                }
            }
            return flag;
        },
        getGroupChild: function (node, assign) { // 递归获取组织及下级组织的分组节点
            var nodes = node.children;
            if (nodes != null && nodes != undefined && nodes.length > 0) {
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.type == "assignment") {
                        assign.push(node);
                    } else if (node.type == "group" && node.children != undefined) {
                        publicFun.getGroupChild(node.children, assign);
                    }
                }
            }
        },
        getCharSelect: function (treeObj) {
            var treeId = treeObj.setting.treeId;
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                switch (treeId) {
                    case 'treeDemo':
                        $("#groupSelect").val(allNodes[0].name);
                        break;
                    case 'treeDemoTwo':
                        $("#groupSelectTwo").val(allNodes[0].name);
                        break;
                    case 'treeDemoThree':
                        $("#groupSelectThree").val(allNodes[0].name);
                        break;
                    case 'treeDemoFour':
                        $("#groupSelectFour").val(allNodes[0].name);
                        break;
                }
            } else {
                switch (treeId) {
                    case 'treeDemo':
                        $("#groupSelect").val("");
                        break;
                    case 'treeDemoTwo':
                        $("#groupSelectTwo").val("");
                        break;
                    case 'treeDemoThree':
                        $("#groupSelectThree").val("");
                        break;
                    case 'treeDemoFour':
                        $("#groupSelectFour").val("");
                        break;
                }
            }
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            dbValue = false
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            console.log(treeId, 'treeId');
            // 若为取消勾选则不展开节点
            if (treeNode.checked) {
                // setTimeout(function () {
                    zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                // }, 1200);
                setTimeout(() => {
                    publicFun.getCheckedNodes(treeId);
                    switch (treeId) {
                        case 'treeDemo':
                            roadTransport.validates();
                            break;
                        case 'treeDemoTwo':
                            vehicleRandomNum.validates();
                            break;
                        case 'treeDemoThree':
                            vehicleDetail.validates();
                            break;
                        case 'treeDemoFour':
                            userRendomVehicle.validates();
                            break;
                        default: console.log('default');
                    }
                }, 600);
            }
            publicFun.getCharSelect(zTree);
            publicFun.getCheckedNodes(treeId);
        },
        onCheckVehicleFuzzy: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            // 若为取消勾选则不展开节点
            if (treeNode.checked) {
                if (treeId === 'treeDemoTwo') {
                    isSearch2 = false;
                }
                if (treeId === 'treeDemoThree') {
                    isSearch3 = false;
                }
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
            }

            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            publicFun.getCharSelect(zTree);
            publicFun.getCheckedNodes(treeId);
        },
        // 获取到选择的节点
        getCheckedNodes: function (treeId) {
            var index = treeId.substring(8);
            var zTree = $.fn.zTree.getZTreeObj(treeId),
                nodes = zTree
                .getCheckedNodes(true),
                v = "",
                vid = "",
                v1 = "",
                vid1 = "",
                v2 = "",
                vid2 = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (treeId == "treeDemo" && nodes[i].type == "group") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].uuid + ",";
                } else if (treeId != "treeDemo" && nodes[i].type == "vehicle") {
                    if (vid1.indexOf(nodes[i].id) == -1) {
                        v1 += nodes[i].name + ",";
                        vid1 += nodes[i].id + ",";
                    }
                } else if (treeId == "treeDemoFour" && nodes[i].type == "user") {
                    v2 += nodes[i].name + ",";
                    vid2 += nodes[i].id + ",";
                }
            }
            groupId = vid;
            vehicleListTwo = v1;
            vehicleIdTwo = vid1;
            vehicleListThree = v1;
            vehicleIdThree = vid1;
            vehicleListFour = v2;
            vehicleIdFour = vid2;
        },
        //开始时间
        startDay: function (day) {
            // var timeInterval = $('#timeInterval').val().split('--');
            var timeInterval = $('.tab-pane.active .layer-date').val().split('--');
            var startValue = timeInterval[0];
            var endValue = timeInterval[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * day;
                today.setTime(targetday_milliseconds); //注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = publicFun.doHandleMonth(tMonth + 1);
                tDate = publicFun.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " +
                    "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 *
                    parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = publicFun.doHandleMonth(endMonth + 1);
                endDate = publicFun.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " " +
                    "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = publicFun.doHandleMonth(vMonth + 1);
                vDate = publicFun.doHandleMonth(vDate);
                startTime = vYear + "-" + vMonth + "-" + vDate + " " +
                    "00:00:00";
                if (day == 1) {
                    endTime = vYear + "-" + vMonth + "-" + vDate + " " +
                        "23:59:59";
                } else {
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = publicFun.doHandleMonth(vendMonth + 1);
                    vendDate = publicFun.doHandleMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate + " " +
                        "23:59:59";
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
            startTime = nowDate.getFullYear() +
                "-" +
                (parseInt(nowDate.getMonth() + 1) < 10 ? "0" +
                    parseInt(nowDate.getMonth() + 1) :
                    parseInt(nowDate.getMonth() + 1)) +
                "-" +
                (nowDate.getDate() < 10 ? "0" + nowDate.getDate() :
                    nowDate.getDate()) + " " + "00:00:00";
            endTime = nowDate.getFullYear() +
                "-" +
                (parseInt(nowDate.getMonth() + 1) < 10 ? "0" +
                    parseInt(nowDate.getMonth() + 1) :
                    parseInt(nowDate.getMonth() + 1)) +
                "-" +
                (nowDate.getDate() < 10 ? "0" + nowDate.getDate() :
                    nowDate.getDate()) +
                " " +
                ("23") +
                ":" +
                ("59") +
                ":" +
                ("59");
            var atime = $("#atime").val();
            if (atime != undefined && atime != "") {
                startTime = atime;
            }
        },
    };


    //道路运输企业抽查车辆数量统计报表
    roadTransport = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var makeText = function (index, text) {
                var checkedStr = '';
                checkedStr = 'checked="checked"';
                return "<li><label><input type=\"checkbox\"  " + checkedStr + " class=\"toggle-vis\" data-column=\"" + index + "\" />" + text + "</label></li>"
            };
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(1) + "\" />道路运输企业</label></li>";

            // menu_text += makeText(1, '道路运输企业');
            menu_text += makeText(2, '查看定位信息');
            menu_text += makeText(3, '查看历史轨迹');
            menu_text += makeText(4, '查看视频');
            menu_text += makeText(5, '违章处理');
            menu_text += makeText(6, '车辆总数');
            menu_text += makeText(7, '合计');
            menu_text += makeText(8, '查看定位信息');
            menu_text += makeText(9, '查看历史轨迹');
            menu_text += makeText(10, '查看视频');
            menu_text += makeText(11, '违章处理');
            menu_text += makeText(12, '合计');
            $("#Ul-menu-text").html(menu_text);
        },
        inquireClick: function (number) {
            if (number == 0) {
                publicFun.getsTheCurrentTime();
            } else if (number == -1) {
                publicFun.startDay(-1)
            } else if (number == -3) {
                publicFun.startDay(-3)
            } else if (number == -7) {
                publicFun.startDay(-7)
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
            publicFun.getCheckedNodes('treeDemo');
            if (!roadTransport.validates()) {
                return;
            }
            myTable.column(1).search('', false, false).draw();

            var url = "/clbs/cb/cbReportManagement/spotCheckReport/groupSpotCheck";
            var parameter = {
                "groupId": groupId,
                "startTime": startTime,
                "endTime": endTime
            };
            json_ajax("POST", url, "json", true, parameter, roadTransport.getCallback);
        },
        exportAlarm: function () {
            if (getRecordsNum() > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var exportUrl = "/clbs/cb/cbReportManagement/spotCheckReport/exportGroupData?fuzzyParm=" + fuzzyParm;
            window.location.href = exportUrl;
        },
        validates: function () {
            return $("#speedlist").validate({
                rules: {
                    startTime: {
                        required: true
                    },
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval"
                    },
                    groupSelect: {
                        required: true,
                        /*zTreeChecked: "treeDemo"*/
                    }
                },
                messages: {
                    startTime: {
                        required: "请选择开始日期！"
                    },
                    endTime: {
                        required: "请选择结束时间！",
                        compareDate: endtimeComStarttime
                    },
                    groupSelect: {
                        required: vehicleSelectGroup
                        /*zTreeChecked: vehicleSelectBrand*/
                    }
                }
            }).form();
        },
        getCallback: function (date) {
            if (date.success == true) {
                dataListArray = []; //用来储存显示数据
                $("#exportAlarm").removeAttr("disabled");
                if (date.obj != null && date.obj.length != 0) {
                    var groupCheckDate = date.obj.groupData;
                    for (var i = 0; i < groupCheckDate.length; i++) {
                        var dateList = [
                            i + 1,
                            groupCheckDate[i].groupName,
                            groupCheckDate[i].groupCheckPositionNumber,
                            groupCheckDate[i].groupCheckHistoricalTrackNumber,
                            groupCheckDate[i].groupCheckVideoNumber,
                            groupCheckDate[i].groupViolationHandingNumber,
                            groupCheckDate[i].groupVehicleSum,
                            groupCheckDate[i].groupSpotCheckVehicleSummation,
                            groupCheckDate[i].groupCheckPositionInfoPercentage,
                            groupCheckDate[i].groupCheckHistoricalTrackPercentage,
                            groupCheckDate[i].groupCheckVideoPercentage,
                            groupCheckDate[i].groupViolationHandlingPercentage,
                            groupCheckDate[i].groupTotalPercentage
                        ];
                        dataListArray.push(dateList);
                    }
                    roadTransport.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                } else {
                    roadTransport.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                }
            } else {
                $("#exportAlarm").attr("disabled", "disabled");
                if (date.msg != null) {
                    layer.msg(date.msg, {
                        move: false
                    });
                }
            }
        },
        getTable: function (table) {
            myTable = $(table).DataTable({
                "destroy": true,
                /*"dom": 'tiprl',// 自定义显示项*/
                "lengthChange": true, // 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true, // 本地搜索
                "ordering": false, // 排序功能
                "Info": true, // 页脚信息
                "autoWidth": true, // 自动宽度
                "stripeClasses": [],
                "pageLength": 10,
                "lengthMenu": [5, 10, 20, 50, 100, 200],
                "pagingType": "simple_numbers", // 分页样式
                "dom": "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
                "oLanguage": { // 国际语言转化
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
                "columnDefs": [{
                    "targets": [0, 2, 3, 4, 5, 6, 7, 8, 9],
                    "searchable": false
                }],
                "order": [
                    [0, null]
                ], // 第一列排序图标改为默认

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
            $('.toggle-vis').on('change', function (e) {
                var column = myTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            $("#search_button").on("click", function () {
                var tsval = $("#simpleQueryParam").val();
                fuzzyParm = tsval;
                myTable.column(1).search(tsval, false, false).draw();
            });
        },
        reloadData: function (dataList) {
            var currentPage = myTable.page();
            myTable.clear();
            myTable.rows.add(dataList);
            myTable.page(currentPage).draw(false);
            myTable.column(1).search('', false, false).page(currentPage).draw();
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            var tsval = $("#simpleQueryParam").val();
            myTable.column(1).search(tsval, false, false).draw();
        },
    };


    //车辆抽查数量统计表
    vehicleRandomNum = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var makeText = function (index, text) {
                var checkedStr = '';
                checkedStr = 'checked="checked"';
                return "<li><label><input type=\"checkbox\"  " + checkedStr + " class=\"toggle-vis\" data-column=\"" + index + "\" />" + text + "</label></li>"
            };
            var menu_text = "";
            var table = $("#dataTableThree tr th:gt(0)");
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(1) + "\" />车牌号</label></li>";
            menu_text += makeText(2, '车牌颜色');
            menu_text += makeText(3, '车辆类型');
            menu_text += makeText(4, '所属道路运输企业');
            menu_text += makeText(5, '查看定位信息');
            menu_text += makeText(6, '查看历史轨迹');
            menu_text += makeText(7, '查看视频');
            menu_text += makeText(8, '违章处理');
            menu_text += makeText(9, '合计');
            $("#Ul-menu-textTwo").html(menu_text);
        },
        inquireClick: function (number) {
            if (number == 0) {
                publicFun.getsTheCurrentTime();
            } else if (number == -1) {
                publicFun.startDay(-1)
            } else if (number == -3) {
                publicFun.startDay(-3)
            } else if (number == -7) {
                publicFun.startDay(-7)
            }
            if (number != 1) {
                $('#timeIntervalTwo').val(startTime + '--' + endTime);
            } else {
                var timeInterval = $('#timeIntervalTwo').val().split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
            }
            publicFun.getCheckedNodes('treeDemoTwo');
            if (!vehicleRandomNum.validates()) {
                return;
            }
            myTableTwo.column(1).search('', false, false).draw();
            var url = "/clbs/cb/cbReportManagement/spotCheckReport/getVehicleSpotCheckNumberCountList";
            var parameter = {
                "vehicleIds": vehicleIdTwo,
                "startTime": startTime,
                "endTime": endTime
            };
            json_ajax("POST", url, "json", true, parameter, vehicleRandomNum.getCallback);
        },
        exportAlarm: function () {
            if (getRecordsNum('dataTableTwo_info') > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var exportUrl = "/clbs/cb/cbReportManagement/spotCheckReport/exportVehicleSpotCheckNumberCountList?simpleQueryParam=" + fuzzyParm1;
            console.log(exportUrl);
            window.location.href = exportUrl;
        },
        validates: function () {
            return $("#speedlistTwo").validate({
                rules: {
                    startTimeTwo: {
                        required: true
                    },
                    endTimeTwo: {
                        required: true,
                        compareDate: "#timeIntervalTwo"
                    },
                    groupSelectTwo: {
                        zTreeChecked: "treeDemoTwo"
                    }
                },
                messages: {
                    startTimeTwo: {
                        required: "请选择开始日期！"
                    },
                    endTimeTwo: {
                        required: "请选择结束时间！",
                        compareDate: endtimeComStarttime
                    },
                    groupSelectTwo: {
                        zTreeChecked: vehicleSelectBrand
                    }
                }
            }).form();
        },
        getCallback: function (date) {
            if (date.success == true) {
                dataListArray = []; //用来储存显示数据
                $("#exportAlarmTwo").removeAttr("disabled");
                if (date.obj != null && date.obj.length != 0) {
                    var alarm = date.obj;
                    for (var i = 0; i < alarm.length; i++) {
                        var dateList = [
                            i + 1,
                            alarm[i].plateNumber,
                            alarm[i].plateColor,
                            alarm[i].vehicleType == 'null' ? '其他车辆' : alarm[i].vehicleType,
                            alarm[i].groupName,
                            alarm[i].checkPositionInfoNum,
                            alarm[i].checkHistoricalTrackNum,
                            alarm[i].checkVideoNum,
                            alarm[i].violationHandlingNum,
                            alarm[i].totalNum,
                        ];
                        dataListArray.push(dateList);
                    }
                    vehicleRandomNum.reloadData(dataListArray);
                    $("#simpleQueryParamTwo").val("");
                } else {
                    vehicleRandomNum.reloadData(dataListArray);
                    $("#simpleQueryParamTwo").val("");
                }
            } else {
                $("#exportAlarmTwo").attr("disabled", "disabled");
                if (date.msg != null) {
                    layer.msg(date.msg, {
                        move: false
                    });
                }
            }
        },
        getTable: function (table) {
            myTableTwo = $(table).DataTable({
                "destroy": true,
                /*"dom": 'tiprl',// 自定义显示项*/
                "lengthChange": true, // 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true, // 本地搜索
                "ordering": false, // 排序功能
                "Info": true, // 页脚信息
                "autoWidth": true, // 自动宽度
                "stripeClasses": [],
                "pageLength": 10,
                "lengthMenu": [5, 10, 20, 50, 100, 200],
                "pagingType": "simple_numbers", // 分页样式
                "dom": "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
                "oLanguage": { // 国际语言转化
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
                "columnDefs": [{
                    "targets": [0, 2, 3, 4, 5, 6, 7, 8],
                    "searchable": false
                }],
                "order": [
                    [0, null]
                ], // 第一列排序图标改为默认

            });
            myTableTwo.on('order.dt search.dt', function () {
                myTableTwo.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
            //显示隐藏列
            $('.toggle-vis').on('change', function (e) {
                var column = myTableTwo.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            $("#search_buttonTwo").on("click", function () {
                var tsval = $("#simpleQueryParamTwo").val();
                fuzzyParm1 = tsval;
                myTableTwo.column(1).search(tsval, false, false).draw();
            });
        },
        reloadData: function (dataList) {
            var currentPage = myTableTwo.page();
            myTableTwo.clear();
            myTableTwo.rows.add(dataList);
            myTableTwo.page(currentPage).draw(false);
            myTable.column(1).search('', false, false).page(currentPage).draw();
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParamTwo").val("");
            var tsval = $("#simpleQueryParamTwo").val();
            myTableTwo.column(1).search(tsval, false, false).draw();
        }
    };


    //车辆抽查明细表
    vehicleDetail = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTableThree tr th:gt(0)");
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(1) + "\" />" + table[0].innerHTML + "</label></li>";
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-textThree").html(menu_text);
        },
        inquireClick: function (number) {
            if (number == 0) {
                publicFun.getsTheCurrentTime();
            } else if (number == -1) {
                publicFun.startDay(-1)
            } else if (number == -3) {
                publicFun.startDay(-3)
            } else if (number == -7) {
                publicFun.startDay(-7)
            }
            if (number != 1) {
                $('#timeIntervalThree').val(startTime + '--' + endTime);
            } else {
                var timeInterval = $('#timeIntervalThree').val().split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
            }
            publicFun.getCheckedNodes('treeDemoThree');
            if (!vehicleDetail.validates()) {
                return;
            }
            myTableThree.column(1).search('', false, false).draw();

            var url = "/clbs/cb/cbReportManagement/spotCheckReport/getVehicleSpotCheckDetailList";
            var parameter = {
                "vehicleIds": vehicleIdThree,
                "startTime": startTime,
                "endTime": endTime
            };
            json_ajax("POST", url, "json", true, parameter, vehicleDetail.getCallback);
        },
        exportAlarm: function () {
            if (getRecordsNum('dataTableFour_info') > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var exportUrl = "/clbs/cb/cbReportManagement/spotCheckReport/exportVehicleSpotCheckDetail?simpleQueryParam=" + fuzzyParm2;
            window.location.href = exportUrl;
        },
        validates: function () {
            return $("#speedlistThree").validate({
                rules: {
                    startTimeThree: {
                        required: true
                    },
                    endTimeThree: {
                        required: true,
                        compareDate: "#timeIntervalThree"
                    },
                    groupSelectThree: {
                        zTreeChecked: "treeDemoThree"
                    }
                },
                messages: {
                    startTimeThree: {
                        required: "请选择开始日期！"
                    },
                    endTimeThree: {
                        required: "请选择结束时间！",
                        compareDate: endtimeComStarttime
                    },
                    groupSelectThree: {
                        zTreeChecked: vehicleSelectBrand
                    }
                }
            }).form();
        },
        // 获取抽查内容
        getSpotCheckContent: function (data) {
            var returnVal = '';
            switch (data) {
                case 0:
                    returnVal = '查看定位信息';
                    break;
                case 1:
                    returnVal = '查看历史轨迹';
                    break;
                case 2:
                    returnVal = '查看视频';
                    break;
                case 3:
                    returnVal = '违章处理';
                    break;
                default:
                    returnVal = '违章处理';
                    break;
            }
            return returnVal;
        },
        //解析位置信息
        getAlarmAddress: function (target, latitude, longitude) {
            if (latitude === 'null' && longitude === 'null') {
                $(target).closest('td').html('');
                return;
            }
            var url = '/clbs/v/monitoring/address';
            var param = {
                addressReverse: [latitude, longitude, '', "", 'vehicle']
            };
            $.ajax({
                type: "POST", //通常会用到两种：GET,POST。默认是：GET
                url: url, //(默认: 当前页地址) 发送请求的地址
                dataType: "json", //预期服务器返回的数据类型。"json"
                async: true, // 异步同步，true  false
                data: param,
                traditional: true,
                timeout: 8000, //超时时间设置，单位毫秒
                success: function (data) { //请求成功
                    $(target).closest('td').html($.isPlainObject(data) || data === '[]' ? '未定位' : data);
                },
            });
        },
        getCallback: function (date) {
            if (date.success == true) {
                dataListArray = []; //用来储存显示数据
                $("#exportAlarmThree").removeAttr("disabled");
                if (date.obj != null && date.obj.length != 0) {
                    var alarm = date.obj;
                    startLoc = [], endLoc = [];
                    for (var i = 0; i < alarm.length; i++) {
                        var dateList = [
                            i + 1,
                            alarm[i].plateNumber,
                            alarm[i].plateColor,
                            alarm[i].vehicleType,
                            alarm[i].groupName,
                            alarm[i].locationTimeStr,
                            alarm[i].speed,
                            alarm[i].speedLimit,
                            '<a onclick="vehicleDetail.getAlarmAddress(this,\'' + alarm[i].latitude + '\',\'' + alarm[i].longtitude + '\')">点击获取位置信息</a>',
                            vehicleDetail.getSpotCheckContent(alarm[i].spotCheckContent),
                            alarm[i].spotCheckUser,
                            alarm[i].spotCheckTimeStr,
                        ];
                        dataListArray.push(dateList);
                        startLoc.push(alarm[i].alarmStartLocation);
                        endLoc.push(alarm[i].alarmEndLocation);
                    }
                    vehicleDetail.reloadData(dataListArray);
                    $("#simpleQueryParamThree").val("");
                } else {
                    vehicleDetail.reloadData(dataListArray);
                    $("#simpleQueryParamThree").val("");
                }
            } else {
                $("#exportAlarmThree").attr("disabled", "disabled");
                if (date.msg != null) {
                    layer.msg(date.msg, {
                        move: false
                    });
                }
            }
        },
        getTable: function (table) {
            myTableThree = $(table).DataTable({
                "destroy": true,
                /*"dom": 'tiprl',// 自定义显示项*/
                "lengthChange": true, // 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true, // 本地搜索
                "ordering": false, // 排序功能
                "Info": true, // 页脚信息
                "autoWidth": true, // 自动宽度
                "stripeClasses": [],
                "pageLength": 10,
                "lengthMenu": [5, 10, 20, 50, 100, 200],
                "pagingType": "simple_numbers", // 分页样式
                "dom": "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
                "oLanguage": { // 国际语言转化
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
                "columnDefs": [{
                    "targets": [0, 2, 3, 4, 5, 6, 7, 8, 9],
                    "searchable": false
                }],
                "order": [
                    [0, null]
                ], // 第一列排序图标改为默认

            });
            myTableThree.on('order.dt search.dt', function () {
                myTableThree.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
            //显示隐藏列
            $('.toggle-vis').on('change', function (e) {
                var column = myTableThree.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            $("#search_buttonThree").on("click", function () {
                var tsval = $("#simpleQueryParamThree").val();
                fuzzyParm2 = tsval;
                myTableThree.column(1).search(tsval, false, false).draw();
            });
        },
        reloadData: function (dataList) {
            var currentPage = myTableThree.page();
            myTableThree.clear();
            myTableThree.rows.add(dataList);
            myTableThree.page(currentPage).draw(false);
            myTable.column(1).search('', false, false).page(currentPage).draw();
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParamThree").val("");
            var tsval = $("#simpleQueryParamThree").val();
            myTableThree.column(1).search(tsval, false, false).draw();
        }
    };


    //用户抽查车辆数量及百分比统计报表
    userRendomVehicle = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var makeText = function (index, text) {
                var checkedStr = '';
                checkedStr = 'checked="checked"';
                return "<li><label><input type=\"checkbox\"  " + checkedStr + " class=\"toggle-vis\" data-column=\"" + index + "\" />" + text + "</label></li>"
            }
            menu_text += makeText(1, '用户');
            menu_text += makeText(2, '在岗时段');
            menu_text += makeText(3, '所属道路运输企业');
            menu_text += makeText(4, '查看定位信息');
            menu_text += makeText(5, '查看历史轨迹');
            menu_text += makeText(6, '查看视频');
            menu_text += makeText(7, '违章处理');
            menu_text += makeText(8, '车辆总数');
            menu_text += makeText(9, '合计');
            menu_text += makeText(10, '查看定位信息');
            menu_text += makeText(11, '查看历史轨迹');
            menu_text += makeText(12, '查看视频');
            menu_text += makeText(13, '违章处理');
            menu_text += makeText(14, '合计');
            $("#Ul-menu-textFour").html(menu_text);
        },
        getTable: function (table) {
            myTableFour = $(table).DataTable({
                "destroy": true,
                /*"dom": 'tiprl',// 自定义显示项*/
                "lengthChange": true, // 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true, // 本地搜索
                "ordering": false, // 排序功能
                "Info": true, // 页脚信息
                "autoWidth": true, // 自动宽度
                "stripeClasses": [],
                "pageLength": 10,
                "lengthMenu": [5, 10, 20, 50, 100, 200],
                "pagingType": "simple_numbers", // 分页样式
                "dom": "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
                "oLanguage": { // 国际语言转化
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
                "columnDefs": [{
                    "targets": [0, 2, 3, 4, 5, 6, 7, 8, 9],
                    "searchable": false
                }],
                "order": [
                    [0, null]
                ], // 第一列排序图标改为默认

            });
            myTableFour.on('order.dt search.dt', function () {
                myTableFour.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();
            //显示隐藏列
            $('.toggle-vis').on('change', function (e) {
                var column = myTableFour.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            $("#search_buttonFour").on("click", function () {
                var tsval = $("#simpleQueryParamFour").val();
                fuzzyParm3 = tsval;
                myTableFour.column(1).search(tsval, false, false).draw();
            });
        },
        inquireClick: function (number) {
            if (number == 0) {
                publicFun.getsTheCurrentTime();
            } else if (number == -1) {
                publicFun.startDay(-1)
            } else if (number == -3) {
                publicFun.startDay(-3)
            } else if (number == -7) {
                publicFun.startDay(-7)
            }
            if (number != 1) {
                $('#timeIntervalFour').val(startTime + '--' + endTime);
            } else {
                var timeInterval = $('#timeIntervalFour').val().split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
            }
            publicFun.getCheckedNodes('treeDemoFour');
            if (!userRendomVehicle.validates()) {
                return;
            }
            myTableFour.column(1).search('', false, false).draw();

            var url = "/clbs/cb/cbReportManagement/spotCheckReport/getUserSpotCheckNumberAndPercentageList";
            var parameter = {
                "userIds": vehicleIdFour,
                "startTime": startTime,
                "endTime": endTime
            };
            json_ajax("POST", url, "json", true, parameter, userRendomVehicle.getCallback);
        },
        reloadData: function (dataList) {
            var currentPage = myTableFour.page();
            myTableFour.clear();
            myTableFour.rows.add(dataList);
            myTableFour.page(currentPage).draw(false);
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParamFour").val("");
            var tsval = $("#simpleQueryParamFour").val();
            myTableFour.column(1).search(tsval, false, false).draw();
        },
        exportAlarm: function () {
            if (getRecordsNum('dataTableThree_info') > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var exportUrl = "/clbs/cb/cbReportManagement/spotCheckReport/exportUserSpotCheckNumberAndPercentage?simpleQueryParam=" + fuzzyParm3;
            window.location.href = exportUrl;
        },
        validates: function () {
            return $("#speedlistFour").validate({
                rules: {
                    startTimeFour: {
                        required: true
                    },
                    endTimeFour: {
                        required: true,
                        compareDate: "#timeIntervalFour"
                    },
                    groupSelectFour: {
                        zTreePeopleChecked: "treeDemoFour"
                    }
                },
                messages: {
                    startTimeFour: {
                        required: "请选择开始日期！"
                    },
                    endTimeFour: {
                        required: "请选择结束时间！",
                        compareDate: endtimeComStarttime
                    },
                    groupSelectFour: {
                        zTreePeopleChecked: vehicleSelectUser
                    }
                }
            }).form();
        },
        getCallback: function (date) {
            if (date.success == true) {
                dataListArray = []; //用来储存显示数据
                $("#exportAlarmFour").removeAttr("disabled");
                if (date.obj != null && date.obj.length != 0) {
                    var alarm = date.obj;
                    startLoc = [], endLoc = [];
                    for (var i = 0; i < alarm.length; i++) {
                        var dateList = [
                            i + 1,
                            alarm[i].userName,
                            alarm[i].onDutyTime,
                            alarm[i].userGroupName,
                            alarm[i].checkPositionInfoNum,
                            alarm[i].checkHistoricalTrackNum,
                            alarm[i].checkVideoNum,
                            alarm[i].violationHandlingNum,
                            alarm[i].vehicleCount,
                            alarm[i].totalNum,
                            alarm[i].checkPositionInfoPercentage,
                            alarm[i].checkHistoricalTrackPercentage,
                            alarm[i].checkVideoPercentage,
                            alarm[i].violationHandlingPercentage,
                            alarm[i].totalPercentage,
                        ];
                        dataListArray.push(dateList);
                        startLoc.push(alarm[i].alarmStartLocation);
                        endLoc.push(alarm[i].alarmEndLocation);
                    }
                    userRendomVehicle.reloadData(dataListArray);
                    $("#simpleQueryParamFour").val("");
                } else {
                    userRendomVehicle.reloadData(dataListArray);
                    $("#simpleQueryParamFour").val("");
                }
            } else {
                $("#exportAlarmFour").attr("disabled", "disabled");
                if (date.msg != null) {
                    layer.msg(date.msg, {
                        move: false
                    });
                }
            }
        },
    };


    $(function () {
        $('input').inputClear();
        publicFun.init();
        //当前时间
        publicFun.getsTheCurrentTime();

        /***道路运输企业抽查车辆数量统计报表***/
        //初始化页面
        roadTransport.init();
        roadTransport.getTable('#dataTable');

        //当前时间
        $('#timeInterval').dateRangePicker({
            dateLimit: 31,
            inquireBtn: '#inquireClickOne'
        });
        $("#groupSelect").bind("click", showMenuContent);
        //导出
        $("#exportAlarm").bind("click", roadTransport.exportAlarm);
        $("#refreshTable").bind("click", roadTransport.refreshTable);

        //
        $("#groupSelect").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkAllNodes(false);
            search_ztree('treeDemo', 'groupSelect', 'group');
        });

        /***车辆抽查数量统计表***/
        vehicleRandomNum.init();
        vehicleRandomNum.getTable('#dataTableTwo');

        $('#timeIntervalTwo').dateRangePicker({
            dateLimit: 31,
            inquireBtn: '#inquireClickTwo'
        });
        $("#groupSelectTwo").bind("click", showMenuContent);
        //导出
        $("#exportAlarmTwo").bind("click", vehicleRandomNum.exportAlarm);
        $("#refreshTableTwo").bind("click", vehicleRandomNum.refreshTable);


        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                search_ztree('treeDemo', 'groupSelect', 'group');
                treeObj.checkAllNodes(false);
            }
            if (id == 'groupSelectTwo') {
                var param = $("#groupSelectTwo").val();
                publicFun.searchVehicleTree(param, "treeDemoTwo");
            }
            if (id == 'groupSelectThree') {
                var param = $("#groupSelectThree").val();
                publicFun.searchVehicleTree(param, "treeDemoThree");
            }
            if (id == 'groupSelectFour') {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemoFour");
                treeObj.checkAllNodes(false);
                vehicleListFour = [];
                vehicleIdFour = [];
                search_ztree('treeDemoFour', 'groupSelectFour', 'user');
            }
        });

        /**
         * 监控对象树模糊查询
         */
        var inputChange;
        $("#groupSelectTwo").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                isSearch2 = true;
            };
            inputChange = setTimeout(function () {
                if (isSearch2) {
                    var param = $("#groupSelectTwo").val();
                    publicFun.searchVehicleTree(param, "treeDemoTwo");
                }
                isSearch2 = true;
            }, 500);
        });

        var inputChangeThree;
        $("#groupSelectThree").on('input propertychange', function (value) {
            if (inputChangeThree !== undefined) {
                clearTimeout(inputChangeThree);
            }
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                isSearch3 = true;
            }
            inputChangeThree = setTimeout(function () {
                if (isSearch3) {
                    var param = $("#groupSelectThree").val();
                    publicFun.searchVehicleTree(param, "treeDemoThree");
                }
                isSearch3 = true;
            }, 500);
        });

        $("#groupSelectFour").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemoFour");
            treeObj.checkAllNodes(false);
            vehicleListFour = [];
            vehicleIdFour = [];
            search_ztree('treeDemoFour', 'groupSelectFour', 'user');
        });

        /***用户抽查车辆数量及百分比统计报表***/
        userRendomVehicle.init();
        userRendomVehicle.getTable('#dataTableFour');

        $('#timeIntervalFour').dateRangePicker({
            dateLimit: 31,
            inquireBtn: '#inquireClickFour'
        });
        $("#groupSelectFour").bind("click", showMenuContent);
        //导出
        $("#exportAlarmFour").bind("click", userRendomVehicle.exportAlarm);
        $("#refreshTableFour").bind("click", userRendomVehicle.refreshTable);


        /***车辆抽查明细表***/
        vehicleDetail.init();
        vehicleDetail.getTable('#dataTableThree');

        $('#timeIntervalThree').dateRangePicker({
            dateLimit: 31,
            inquireBtn: '#inquireClickThree'
        });
        $("#groupSelectThree").bind("click", showMenuContent);
        //导出
        $("#exportAlarmThree").bind("click", vehicleDetail.exportAlarm);
        $("#refreshTableThree").bind("click", vehicleDetail.refreshTable);

        // 模糊查询
        $('#simpleQueryParamTwo').keyup(function (event) {
            if (event.keyCode == 13) {
                $("#search_buttonTwo").click();
            }
        });
        $('#simpleQueryParamThree').keyup(function (event) {
            if (event.keyCode == 13) {
                $("#search_buttonThree").click();
            }
        });
        $('#simpleQueryParamFour').keyup(function (event) {
            if (event.keyCode == 13) {
                $("#search_buttonFour").click();
            }
        });
        $('#queryType').on('change', function () {
            var param = $("#groupSelectTwo").val();
            publicFun.searchVehicleTree(param);
        });
        $('#queryType1').on('change', function () {
            var param = $("#groupSelectThree").val();
            publicFun.searchVehicleTree(param);
        });
    })
}(window, $))