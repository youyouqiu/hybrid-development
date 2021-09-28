(function (window, $) {
    //车辆列表
    var vehicleList = [];
    var vehicleListTwo = [];
    var vehicleListThree = [];
    //车辆id列表
    var vehicleId = [];
    var vehicleIdTwo = [];
    var vehicleIdThree = [];
    //表格
    var myTable;
    var myTableTwo;
    var myTableThree;
    //开始时间
    var startTime;
    var startTimeTwo;
    var startTimeThree;
    //结束时间
    var endTime;
    var endTimeTwo;
    var endTimeThree;
    var checkFlag = false; //判断组织节点是否是勾选操作
    var size; //当前权限监控对象数量
    var groupId = [];

    //逆地址解析
    var startLoc = [];
    var endLoc = [];
    var addressMsg = [];

    // 持续超速车辆统计表
    var panelFlag1 = true;
    // 持续超速车辆明细表
    var panelFlag2 = true;

    var zTreeIdJson = {};
    // 持续超速道路运输企业统计表
    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于100自动勾选
    var isSearch2 = true;
    var isSearch3 = true;
    var getAddressStatus = false;
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
            var type = "";
            if (treeId == "treeDemoTwo") {
                type = $("#queryType").val();
            }
            if (treeId == "treeDemoThree") {
                type = $("#queryType1").val();
            }
            if (param == null || param == undefined || param == '') {
                bflag = true;
                publicFun.init(treeId);
            } else {
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
        getTreeUrl: function (treeId, treeNode) {
            if (treeId == 'treeDemo') {
                return "/clbs/m/basicinfo/enterprise/professionals/tree";
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
                if (treeId !== 'treeDemo') {
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
                    }, false, treeNode);
                    var nodesLength = nodes.length;
                    if (nodesLength > GROUP_MAX_CHECK) {
                        layer.msg('最多勾选' + GROUP_MAX_CHECK + '个企业' + '<br/>双击名称可选中本组织');
                        flag = false;
                    }
                }
                return flag;
            }
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { // 若勾选的为组织或分组
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
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle") {
                            // 查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                            var nd = zTree.getNodeByParam("tId", nodes[i].tId, treeNode);
                            if (nd == null && $.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength += ns.length;
                } else if (treeNode.type == "people" || treeNode.type == "vehicle") { // 若勾选的为监控对象
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
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle") {
                            if ($.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength = ns.length + 1;
                }

                if (treeId == 'treeDemo') {
                    // if (nodesLength > 5000) {
                    //     layer.msg(maxSelectItem);
                    //     // layer.msg('最多勾选100个监控对象');
                    //     flag = false;
                    // }
                } else {
                    if (nodesLength > TREE_MAX_CHILDREN_LENGTH) {
                        layer.msg('最多勾选' + TREE_MAX_CHILDREN_LENGTH + '个监控对象');
                        flag = false;
                    }
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
                }
            }
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            dbValue = false;
            var zTree = $.fn.zTree.getZTreeObj(treeId);
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
                            console.log(roadTransport.validates(), 'roadTransport.validates();')
                            break;
                        case 'treeDemoTwo':
                            overspeedVehicle.validates();
                            console.log(overspeedVehicle.validates(), 'overspeedVehicle.validates();')
                            break;
                        case 'treeDemoThree':
                            vehicleDetail.validates();
                            console.log(vehicleDetail.validates(), 'vehicleDetail.validates();')
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
            console.log(treeId)
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
                vid1 = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (treeId == "treeDemo" && nodes[i].type == "group") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].uuid + ",";
                } else if (treeId != "treeDemo" && nodes[i].type == "vehicle") {
                    v1 += nodes[i].name + ",";
                    vid1 += nodes[i].id + ",";
                }
            }
            groupId = vid;
            vehicleListTwo = v1;
            vehicleIdTwo = vid1;
            vehicleListThree = v1;
            vehicleIdThree = vid1;
        },
        // 两个时间相差天数
        datedifference: function (sDate1, sDate2) {
            var dateSpan, tempDate, iDays;
            sDate1 = Date.parse(sDate1);
            sDate2 = Date.parse(sDate2);
            dateSpan = sDate2 - sDate1;
            dateSpan = Math.abs(dateSpan);
            iDays = Math.floor(dateSpan / (24 * 3600 * 1000));
            // return iDays
            return (iDays + 1) //加一才是正常天数
        }
    }

    //持续超速道路运输企业统计表
    roadTransport = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var makeText = function (index, text) {
                var checkedStr = '';
                checkedStr = 'checked="checked"';
                return "<li><label><input type=\"checkbox\"  " + checkedStr + " class=\"toggle-vis\" data-column=\"" + index + "\" />" + text + "</label></li>"
            }
            menu_text += makeText(1, '道路运输企业');
            menu_text += makeText(2, '5分钟以下');
            menu_text += makeText(3, '5(含)-10分钟');
            menu_text += makeText(4, '10(含)分钟以上');
            menu_text += makeText(5, '5分钟以下');
            menu_text += makeText(6, '5(含)-10分钟');
            menu_text += makeText(7, '10(含)分钟以上');
            menu_text += makeText(8, '5分钟以下');
            menu_text += makeText(9, '5(含)-10分钟');
            menu_text += makeText(10, '10(含)分钟以上');
            menu_text += makeText(11, '合计');
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
                tMonth = roadTransport.doHandleMonth(tMonth + 1);
                tDate = roadTransport.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " +
                    "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 *
                    parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = roadTransport.doHandleMonth(endMonth + 1);
                endDate = roadTransport.doHandleMonth(endDate);
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
                vMonth = roadTransport.doHandleMonth(vMonth + 1);
                vDate = roadTransport.doHandleMonth(vDate);
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
                    vendMonth = roadTransport.doHandleMonth(vendMonth + 1);
                    vendDate = roadTransport.doHandleMonth(vendDate);
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
        inquireClick: function (number) {
            if (number == 0) {
                roadTransport.getsTheCurrentTime();
            } else if (number == -1) {
                roadTransport.startDay(-1)
            } else if (number == -3) {
                roadTransport.startDay(-3)
            } else if (number == -7) {
                roadTransport.startDay(-7)
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
            $('#simpleQueryParam').val('')
            var url = "/clbs/cb/cbReportManagement/continuousSpeed/getOrgReport";
            var parameter = {
                orgIds: groupId,
                "startTime": startTime,
                "endTime": endTime,
            };
            json_ajax("POST", url, "json", true, parameter, roadTransport.getCallback);
        },
        exportAlarm: function () {
            if (getRecordsNum() > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var exportUrl = "/clbs/cb/cbReportManagement/continuousSpeed/exportOrgReport";
            var parameter = {
                orgIds: groupId,
                "startTime": startTime,
                "endTime": endTime,
                simpleQueryParam: $('#simpleQueryParam').val()
            };
            json_ajax("post", exportUrl, "json", false, parameter, function (data) {
                if (data.success) {
                    layer.confirm("已加入到导出队列,请注意查看导出管理消息提醒", {
                        icon: 3,
                        title: '操作确认',
                        btn: ['确定', '导出管理']
                    }, function () {
                        layer.closeAll();
                    }, function () {
                        layer.closeAll();
                        pagesNav.showExportManager();
                    });
                } else if (data.msg) {
                    layer.msg(data.msg);
                }
            });
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
        unique: function (arr) {
            var result = [],
                hash = {};
            for (var i = 0, elem;
                (elem = arr[i]) != null; i++) {
                if (!hash[elem]) {
                    result.push(elem);
                    hash[elem] = true;
                }
            }
            return result;
        },
        getCallback: function (date) {
            getAddressStatus = false;
            if (date.success == true) {
                dataListArray = []; //用来储存显示数据
                if (date.obj != null && date.obj.length != 0) {
                    $("#exportAlarm").removeAttr("disabled");
                    var alarm = date.obj;
                    for (var i = 0; i < alarm.length; i++) {
                        var dateList = [
                            i + 1,
                            alarm[i].orgName,
                            alarm[i].shortForGeneral,
                            alarm[i].middleForGeneral,
                            alarm[i].longForGeneral,
                            alarm[i].shortForRelatively,
                            alarm[i].middleForRelatively,
                            alarm[i].longForRelatively,
                            alarm[i].shortForEspecially,
                            alarm[i].middleForEspecially,
                            alarm[i].longForEspecially,
                            alarm[i].total
                        ];
                        dataListArray.push(dateList);
                    }
                    roadTransport.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                } else {
                    roadTransport.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                    $("#exportAlarm").attr("disabled", "disabled");
                }
            } else {
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
                myTable.column(1).search(tsval, false, false).draw();
                if($('#dataTable .dataTables_empty').length != 0){
                    $('#exportAlarm')[0].disabled = true
                }else {
                    $('#exportAlarm')[0].disabled = false
                }
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
            $("#simpleQueryParam").val("");
            var tsval = $("#simpleQueryParam").val();
            myTable.column(1).search(tsval, false, false).draw();
            if($('#dataTable .dataTables_empty').length > 0){
                $('#exportAlarm').prop('disabled', true)
            }else{
                $('#exportAlarm').prop('disabled', false)
            }
        },
    };


    //持续超速车辆统计表
    overspeedVehicle = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var makeText = function (index, text) {
                var checkedStr = '';
                checkedStr = 'checked="checked"'
                return "<li><label><input type=\"checkbox\"  " + checkedStr + " class=\"toggle-vis2\" data-column=\"" + index + "\" />" + text + "</label></li>"
            }
            menu_text += makeText(1, '车牌号');
            menu_text += makeText(2, '车牌颜色');
            menu_text += makeText(3, '车辆类型');
            menu_text += makeText(4, '5分钟以下');
            menu_text += makeText(5, '5(含)-10分钟');
            menu_text += makeText(6, '10(含)分钟以上');
            menu_text += makeText(7, '5分钟以下');
            menu_text += makeText(8, '5(含)-10分钟');
            menu_text += makeText(9, '10(含)分钟以上');
            menu_text += makeText(10, '5分钟以下');
            menu_text += makeText(11, '5(含)-10分钟');
            menu_text += makeText(12, '10(含)分钟以上');
            menu_text += makeText(13, '合计');
            $("#Ul-menu-textTwo").html(menu_text);
        },
        //开始时间
        startDay: function (day) {
            var timeInterval = $('#timeIntervalTwo').val().split('--');
            var startValue = timeInterval[0];
            var endValue = timeInterval[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * day;
                today.setTime(targetday_milliseconds); //注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = overspeedVehicle.doHandleMonth(tMonth + 1);
                tDate = overspeedVehicle.doHandleMonth(tDate);
                var num = -(day + 1);
                startTimeTwo = tYear + "-" + tMonth + "-" + tDate + " " +
                    "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 *
                    parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = overspeedVehicle.doHandleMonth(endMonth + 1);
                endDate = overspeedVehicle.doHandleMonth(endDate);
                endTimeTwo = endYear + "-" + endMonth + "-" + endDate + " " +
                    "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = overspeedVehicle.doHandleMonth(vMonth + 1);
                vDate = overspeedVehicle.doHandleMonth(vDate);
                startTimeTwo = vYear + "-" + vMonth + "-" + vDate + " " +
                    "00:00:00";
                if (day == 1) {
                    endTimeTwo = vYear + "-" + vMonth + "-" + vDate + " " +
                        "23:59:59";
                } else {
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = overspeedVehicle.doHandleMonth(vendMonth + 1);
                    vendDate = overspeedVehicle.doHandleMonth(vendDate);
                    endTimeTwo = vendYear + "-" + vendMonth + "-" + vendDate + " " +
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
            startTimeTwo = nowDate.getFullYear() +
                "-" +
                (parseInt(nowDate.getMonth() + 1) < 10 ? "0" +
                    parseInt(nowDate.getMonth() + 1) :
                    parseInt(nowDate.getMonth() + 1)) +
                "-" +
                (nowDate.getDate() < 10 ? "0" + nowDate.getDate() :
                    nowDate.getDate()) + " " + "00:00:00";
            endTimeTwo = nowDate.getFullYear() +
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
                startTimeTwo = atime;
            }
        },
        inquireClick: function (number) {
            if (number == 0) {
                overspeedVehicle.getsTheCurrentTime();
            } else if (number == -1) {
                overspeedVehicle.startDay(-1)
            } else if (number == -3) {
                overspeedVehicle.startDay(-3)
            } else if (number == -7) {
                overspeedVehicle.startDay(-7)
            }
            if (number != 1) {
                $('#timeIntervalTwo').val(startTimeTwo + '--' + endTimeTwo);
                startTimeTwo = startTimeTwo;
                endTimeTwo = endTimeTwo;
            } else {
                var timeInterval = $('#timeIntervalTwo').val().split('--');
                startTimeTwo = timeInterval[0];
                endTimeTwo = timeInterval[1];
            };
            publicFun.getCheckedNodes('treeDemoTwo');
            if (!overspeedVehicle.validates()) {
                return;
            }
            $('#simpleQueryParamTwo').val('')
            var url = "/clbs/cb/cbReportManagement/continuousSpeed/getVehicleReport";
            var parameter = {
                monitorIds: vehicleIdTwo,
                "startTime": startTimeTwo,
                "endTime": endTimeTwo,
            };
            json_ajax("POST", url, "json", true, parameter, overspeedVehicle.getCallback);
        },
        exportAlarm: function () {
            if (getRecordsNum('dataTableTwo_info') > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var exportUrl = "/clbs/cb/cbReportManagement/continuousSpeed/exportVehicleReport";
            var parameter = {
                monitorIds: vehicleIdTwo,
                "startTime": startTimeTwo,
                "endTime": endTimeTwo,
                simpleQueryParam: $('#simpleQueryParamTwo').val()
            };
            json_ajax("post", exportUrl, "json", false, parameter, function (data) {
                if (data.success) {
                    layer.confirm("已加入到导出队列,请注意查看导出管理消息提醒", {
                        icon: 3,
                        title: '操作确认',
                        btn: ['确定', '导出管理']
                    }, function () {
                        layer.closeAll();
                    }, function () {
                        layer.closeAll();
                        pagesNav.showExportManager();
                    });
                } else if (data.msg) {
                    layer.msg(data.msg);
                }
            });
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
        unique: function (arr) {
            var result = [],
                hash = {};
            for (var i = 0, elem;
                (elem = arr[i]) != null; i++) {
                if (!hash[elem]) {
                    result.push(elem);
                    hash[elem] = true;
                }
            }
            return result;
        },
        getCallback: function (date) {
            if (date.success == true) {
                dataListArray = []; //用来储存显示数据
                if (date.obj != null && date.obj.length != 0) {
                    $("#exportAlarmTwo").removeAttr("disabled");
                    var alarm = date.obj;
                    for (var i = 0; i < alarm.length; i++) {
                        var dateList = [
                            i + 1,
                            alarm[i].monitorName,
                            alarm[i].plateColor,
                            alarm[i].vehicleType == 'null' ? '其他车辆' : alarm[i].vehicleType,
                            alarm[i].shortForGeneral,
                            alarm[i].middleForGeneral,
                            alarm[i].longForGeneral,
                            alarm[i].shortForRelatively,
                            alarm[i].middleForRelatively,
                            alarm[i].longForRelatively,
                            alarm[i].shortForEspecially,
                            alarm[i].middleForEspecially,
                            alarm[i].longForEspecially,
                            alarm[i].total
                        ];
                        dataListArray.push(dateList);
                    }
                    overspeedVehicle.reloadData(dataListArray);
                    $("#simpleQueryParamTwo").val("");
                } else {
                    overspeedVehicle.reloadData(dataListArray);
                    $("#simpleQueryParamTwo").val("");
                    $("#exportAlarmTwo").attr("disabled", "disabled");
                }
            } else {
                if (date.msg != null) {
                    layer.msg(date.msg, {
                        move: false
                    });
                }
            }
            $("#search_buttonTwo").click();
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
                    "targets": [0, 2, 3, 4, 5, 6, 7, 8, 9],
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
            $('.toggle-vis2').on('change', function (e) {
                var column = myTableTwo.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            $("#search_buttonTwo").on("click", function () {
                var tsval = $("#simpleQueryParamTwo").val();
                myTableTwo.column(1).search(tsval, false, false).draw();
                if($('#dataTableTwo .dataTables_empty').length != 0){
                    $('#exportAlarmTwo')[0].disabled = true
                }else {
                    $('#exportAlarmTwo')[0].disabled = false
                }
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
            if($('#dataTableTwo .dataTables_empty').length > 0){
                $('#exportAlarmTwo').prop('disabled', true)
            }else{
                $('#exportAlarmTwo').prop('disabled', false)
            }
        }
    };


    //持续超速车辆明细表
    vehicleDetail = {
        init: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTableThree tr th:gt(0)");
            // menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(1) + "\" disabled />" + table[0].innerHTML + "</label></li>";
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis3\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-textThree").html(menu_text);
        },
        //开始时间
        startDay: function (day) {
            var timeInterval = $('#timeIntervalThree').val().split('--');
            var startValue = timeInterval[0];
            var endValue = timeInterval[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * day;
                today.setTime(targetday_milliseconds); //注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = vehicleDetail.doHandleMonth(tMonth + 1);
                tDate = vehicleDetail.doHandleMonth(tDate);
                var num = -(day + 1);
                startTimeThree = tYear + "-" + tMonth + "-" + tDate + " " +
                    "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 *
                    parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = vehicleDetail.doHandleMonth(endMonth + 1);
                endDate = vehicleDetail.doHandleMonth(endDate);
                endTimeThree = endYear + "-" + endMonth + "-" + endDate + " " +
                    "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = vehicleDetail.doHandleMonth(vMonth + 1);
                vDate = vehicleDetail.doHandleMonth(vDate);
                startTimeThree = vYear + "-" + vMonth + "-" + vDate + " " +
                    "00:00:00";
                if (day == 1) {
                    endTimeThree = vYear + "-" + vMonth + "-" + vDate + " " +
                        "23:59:59";
                } else {
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = vehicleDetail.doHandleMonth(vendMonth + 1);
                    vendDate = vehicleDetail.doHandleMonth(vendDate);
                    endTimeThree = vendYear + "-" + vendMonth + "-" + vendDate + " " +
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
            startTimeThree = nowDate.getFullYear() +
                "-" +
                (parseInt(nowDate.getMonth() + 1) < 10 ? "0" +
                    parseInt(nowDate.getMonth() + 1) :
                    parseInt(nowDate.getMonth() + 1)) +
                "-" +
                (nowDate.getDate() < 10 ? "0" + nowDate.getDate() :
                    nowDate.getDate()) + " " + "00:00:00";
            endTimeThree = nowDate.getFullYear() +
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
                startTimeThree = atime;
            }
        },
        inquireClick: function (number, searchFlag) {
            if (number == 0) {
                vehicleDetail.getsTheCurrentTime();
            } else if (number == -1) {
                vehicleDetail.startDay(-1)
            } else if (number == -3) {
                vehicleDetail.startDay(-3)
            } else if (number == -7) {
                vehicleDetail.startDay(-7)
            }
            if (number != 1) {
                $('#timeIntervalThree').val(startTimeThree + '--' + endTimeThree);
                startTimeThree = startTimeThree;
                endTimeThree = endTimeThree;
            } else {
                var timeInterval = $('#timeIntervalThree').val().split('--');
                startTimeThree = timeInterval[0];
                endTimeThree = timeInterval[1];
            };
            publicFun.getCheckedNodes('treeDemoThree');
            if (!vehicleDetail.validates()) {
                return;
            }
            if(!searchFlag){
                $('#simpleQueryParamThree').val('')
            }
            if(!myTableThree){
                vehicleDetail.getTable()
            }else {
                myTableThree.requestData();
            }
        },
        exportAlarm: function () {
            if (getRecordsNum('dataTableThree_info') > 60000) {
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var exportUrl = "/clbs/cb/cbReportManagement/continuousSpeed/exportVehicleDetailReport";
            var parameter = {
                monitorIds: vehicleIdThree,
                "startTime": startTimeThree,
                "endTime": endTimeThree,
                simpleQueryParam: $('#simpleQueryParamThree').val()
            };
            json_ajax("post", exportUrl, "json", false, parameter, function (data) {
                if (data.success) {
                    layer.confirm("已加入到导出队列,请注意查看导出管理消息提醒", {
                        icon: 3,
                        title: '操作确认',
                        btn: ['确定', '导出管理']
                    }, function () {
                        layer.closeAll();
                    }, function () {
                        layer.closeAll();
                        pagesNav.showExportManager();
                    });
                } else if (data.msg) {
                    layer.msg(data.msg);
                }
            });
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
        unique: function (arr) {
            var result = [],
                hash = {};
            for (var i = 0, elem;
                (elem = arr[i]) != null; i++) {
                if (!hash[elem]) {
                    result.push(elem);
                    hash[elem] = true;
                }
            }
            return result;
        },
        getCallback: function (date) {
            if (date.success == true) {
                dataListArray = []; //用来储存显示数据
                if (date.records != null && date.records.length != 0) {
                    $("#exportAlarmThree").removeAttr("disabled");
                    var alarm = date.records;
                    startLoc = [], endLoc = [];
                    for (var i = 0; i < alarm.length; i++) {
                        var dateList = [
                            i + 1,
                            alarm[i].plateNumber,
                            alarm[i].plateColor,
                            alarm[i].vehicleType,
                            alarm[i].groupName,
                            alarm[i].alarmStartTime,
                            alarm[i].alarmEndTime,
                            alarm[i].maxSpeed,
                            alarm[i].speedTime,
                            alarm[i].alarmStartLocation,
                            alarm[i].alarmEndLocation,
                            // '加载中...',
                            // '加载中...'
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
                    $("#exportAlarmThree").attr("disabled", "disabled");
                }
            } else {
                if (date.msg != null) {
                    layer.msg(date.msg, {
                        move: false
                    });
                }
            }
        },
        //逆地址解析回调方法
        goBack: function (GeocoderResult) {
            msgArray = GeocoderResult;
            var $dataTableTbody = $("#dataTableThree tbody");
            var dataLength = $dataTableTbody.children("tr").length;
            for (var i = 0; i < dataLength; i++) {
                if (msgArray[i] != undefined) {
                    $dataTableTbody.children("tr:nth-child(" + (i + 1) + ")").children("td:nth-child(10)").text(msgArray[i][0]);
                    $dataTableTbody.children("tr:nth-child(" + (i + 1) + ")").children("td:nth-child(11)").text(msgArray[i][1]);
                }
            }
        },
        //对显示的数据进行逆地址解析
        getAddress: function (startStr, endStr) {
            getAddressStatus = true;
            var $dataTableTbody = $("#dataTableThree tbody");
            var dataLength = $dataTableTbody.children("tr").length;
            var num = 0;
            for (var i = 0; i < dataLength; i++) {
                num++;
                var n = $dataTableTbody.children("tr:nth-child(" + (i + 1) + ")").children("td:nth-child(1)").text();
                var startMsg = [];
                var endMsg = [];
                //经纬度正则表达式
                if (startStr[n - 1] != null && parseFloat(startStr[n - 1].split(",")[0]) >= 73.33 <= 135.05 && parseFloat(startStr[n - 1].split(",")[1]) >= 3.51 <= 53.33) {
                    startMsg = [startStr[n - 1].split(",")[0], startStr[n - 1].split(",")[1]];
                } else {
                    startMsg = ["124.411991", "29.043817"];
                }
                if (endStr[n - 1] != null && parseFloat(endStr[n - 1].split(",")[0]) >= 73.33 <= 135.05 && parseFloat(endStr[n - 1].split(",")[1]) >= 3.51 <= 53.33) {
                    endMsg = [endStr[n - 1].split(",")[0], endStr[n - 1].split(",")[1]];
                } else {
                    endMsg = ["124.411991", "29.043817"];
                }
                addressMsg.push(startMsg);
                addressMsg.push(endMsg);
                if (num == dataLength) {
                    var addressIndex = 0;
                    var addressArray = [];
                    backAddressMsg(addressIndex, addressMsg, vehicleDetail.goBack, addressArray);
                    addressMsg = [];
                }
            };
        },
        getTable: function (table) {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTableThree tr th:gt(1)");
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis3\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis3\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-textThree").html(menu_text);
            //表格列定义
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columns = [
                {
                    "data": null,
                    "class": "text-center",
                },
                {
                    "data": "plateNumber",
                    "class": "text-center",
                },
                {
                    "data": "plateColor",
                    "class": "text-center",
                },
                {
                    "data": "vehicleType",
                    "class": "text-center",
                },
                {
                    "data": "groupName",
                    "class": "text-center",
                },
                {
                    "data": "alarmStartTime",
                    "class": "text-center",
                },
                {
                    "data": "alarmEndTime",
                    "class": "text-center",
                },
                {
                    "data": "maxSpeed",
                    "class": "text-center",
                },
                {
                    "data": "speedTime",
                    "class": "text-center",
                },
                {
                    "data": "alarmStartLocation",
                    "class": "text-center",
                },
                {
                    "data": "alarmEndLocation",
                    "class": "text-center",
                },
            ]
            var ajaxDataParamFun = function (d) {
                d.monitorIds= vehicleIdThree
                d.startTime= startTimeThree
                d.endTime= endTimeThree
                d.simpleQueryParam= $('#simpleQueryParamThree').val()
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/cb/cbReportManagement/continuousSpeed/getVehicleDetailReport',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTableThree', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                ajaxCallBack: function (data) {
                    if(data.records.length == 0){
                        $('#exportAlarmThree')[0].disabled = true
                    }else {
                        $('#exportAlarmThree')[0].disabled = false
                    }
                }
            };
            myTableThree = new TG_Tabel.createNew(setting);
            myTableThree.init();
            //显示隐藏列
            $('.toggle-vis3').on('change', function (e) {
                var column = myTableThree.dataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
            $('#search_buttonThree').on('click',function () {
                vehicleDetail.inquireClick(1,true)
            })
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
            $('#simpleQueryParamThree').val('')
            vehicleDetail.inquireClick(1)
        }
    };


    $(function () {
        $('input').inputClear();
        publicFun.init();

        /***持续超速道路运输企业统计表***/
        //初始化页面
        roadTransport.init();
        roadTransport.getTable('#dataTable');

        //当前时间
        roadTransport.getsTheCurrentTime();
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

        /***持续超速车辆统计表***/
        overspeedVehicle.init();
        overspeedVehicle.getTable('#dataTableTwo');

        //当前时间
        overspeedVehicle.getsTheCurrentTime();
        $('#timeIntervalTwo').dateRangePicker({
            dateLimit: 31,
            inquireBtn: '#inquireClickTwo'
        });
        $("#groupSelectTwo").bind("click", showMenuContent);
        //导出
        $("#exportAlarmTwo").bind("click", overspeedVehicle.exportAlarm);
        $("#refreshTableTwo").bind("click", overspeedVehicle.refreshTable);


        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                search_ztree('treeDemo', 'groupSelect', 'group');
                treeObj.checkAllNodes(false);
            };
            if (id == 'groupSelectTwo') {
                var param = $("#groupSelectTwo").val();
                publicFun.searchVehicleTree(param, "treeDemoTwo");
            };
            if (id == 'groupSelectThree') {
                var param = $("#groupSelectThree").val();
                publicFun.searchVehicleTree(param, "treeDemoThree");
            }
        });

        /**
         * 监控对象树模糊查询
         */
        var inputChange;
        $("#groupSelectTwo").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            };
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
            };
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                isSearch3 = true;
            };
            inputChangeThree = setTimeout(function () {
                if (isSearch3) {
                    var param = $("#groupSelectThree").val();
                    publicFun.searchVehicleTree(param, "treeDemoThree");
                }
                isSearch3 = true;
            }, 500);
        });


        /***持续超速车辆明细表***/
        vehicleDetail.init();

        //当前时间
        vehicleDetail.getsTheCurrentTime();
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
            };
        });
        $('#simpleQueryParamThree').keyup(function (event) {
            if (event.keyCode == 13) {
                $("#search_buttonThree").click();
            };
        });
        $('#queryType').on('change', function () {
            var param = $("#groupSelectTwo").val();
            publicFun.searchVehicleTree(param, "treeDemoTwo");
        });
        $('#queryType1').on('change', function () {
            var param = $("#groupSelectThree").val();
            publicFun.searchVehicleTree(param, "treeDemoThree");
        });
    })
}(window, $))