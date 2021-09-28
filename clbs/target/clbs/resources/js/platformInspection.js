(function (window, $) {
    var inspectionSendTime = new Date().getTime();
    var myTable
    var size;//当前权限监控对象数量
    var zTreeIdJson = {};
    var checking = false  //是否有一个分组在勾选中，如果为true, 那么在这个分组请求完成之间将不能勾选其它分组
    var checkFlag = false; //判断组织节点是否是勾选操作
    var inputChangeTimer
    var searchTimeout
    var simpleTable
    var tableData = []
    var currentClickedRow
    var swiper1,swiper2
    var firstRender = true
    var isClickSubmitButton = false
    platformInspection = {
        init:function () {
            _this.createTable()
            _this.createTree1()
            _this.getVehicleTypes();
        },
        // 创建表格
        createTable:function(){
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);
            //表格列定义
            var columns = [
                {
                    data: null
                },
                {
                    "data": "orgName",
                    "class": "text-center",
                    render: function (data) {
                        return data || '-';
                    }
                }, {
                    "data": "brand",
                    "class": "text-center",
                    render: function (data) {
                        return data || '-';
                    }
                }, {
                    "data": "plateColorStr",
                    "class": "text-center",
                    render: function (data) {
                        return data || '-';
                    }
                },
                {
                    "data": "inspectionType",
                    "class": "text-center",
                    render: function (data) {
                        switch (data) {
                            case 1:
                                return '车辆运行监测巡检'
                            case 2:
                                return '驾驶行为监测巡检'
                            case 3:
                                return '驾驶员身份识别巡检'
                            default:
                                return '-'
                        }
                    }
                },
                {
                    "data": "inspector",
                    "class": "text-center",
                    render: function (data) {
                        return data || '-';
                    }
                }, {
                    "data": "inspectionTime",
                    "class": "text-center",
                    render: function (data) {
                        if (data == 0) return 0
                        return data || '-';
                    }
                }, {
                    "data": "inspectionStatus",
                    "class": "text-center",
                    render: function (data) {
                        switch (data) {
                            case 1:
                                return '下发中'
                            case 2:
                                return '下发成功'
                            case 3:
                                return '终端响应超时'
                            case 4:
                                return '终端离线，未下发'
                            default:
                                return '-'
                        }
                    }
                }, {
                    "data": null,
                    "class": "text-center",
                    render: function (data, type, row, meta) {
                        return '<button class="editBtn editBtn-info" onclick="platformInspection.showResultModal(\'' + row.id + '\')" style="padding: 3px 8px !important;"><img src="/clbs/resources/img/lookUpWhite.svg" style="width:14px;margin-top:-2px;margin-right: 2px;">查看</button>'
                    }
                },
            ];
            var ajaxDataParamFun = function(params){
                var searchParams = _this.getSearchParams()
                params.keyword = searchParams.keyword
                params.startTime = searchParams.startTime
                params.endTime = searchParams.endTime
                params.inspectionType = searchParams.inspectionType
                params.vehicleIds = searchParams.vehicleIds
            }
            var handleRowData = function (json) {
                tableData = json.records
            }
            //表格setting
            var setting = {
                // /clbs/a/search/alarmPageList
                listUrl: '/clbs/adas/platformInspection/list',
                columns: columns,
                dataTableDiv: 'dataTable',
                ajaxDataParamFun: ajaxDataParamFun,
                ajaxCallBack: handleRowData, //服务器数据预处理
                pageable: true,
                showIndexColumn: true,
                enabledChange: true,
            };
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        createTree:function(){
            var value = $("#groupSelect").val();
            var queryType = $('#queryType').val();
            var otherParam = {'webType': 2,'queryType': 'monitor', 'type': 1, 'queryType': queryType,'devType': 25};
            if (value != '') {
                otherParam.queryParam = value;
            }
            if (queryType == 'vehType') {
                otherParam.queryParam = $('#vehicleType').val();
            }
            var setting = {
                async: {
                    url: '/clbs/m/basicinfo/monitoring/vehicle/treeStateInfos',
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"webType":2,queryType:'monitor',type:1,devType:25},
                    dataFilter:function (treeId, parentNode, responseData) {
                        var data = responseData;
                        if ($('#queryType').val() == "monitor" || $('#queryType').val() == "vehType") {
                            data = filterQueryResult(responseData, []);
                        }
                        for (var i = 0; i < data.length; i++) {
                            data[i].open = true;
                            if (data[i].type == 'vehicle' || data[i].type == 'people' || data[i].type == 'thing') {
                                data[i].isParent = false;
                            }
                        }
                        return data;
                    }
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
                    // beforeClick: _this.beforeClickVehicle,
                    // beforeCheck: _this.zTreeBeforeCheck,
                    onCheck: _this.onCheckVehicle,
                    onExpand: _this.zTreeOnExpand,
                    onNodeCreated: _this.zTreeOnNodeCreated,
                    beforeCheck: function (treeId, treeNode) {
                        if (!treeNode.checked) {
                            var zTree = $.fn.zTree.getZTreeObj("treeDemo")
                            var nodes = zTree.getCheckedNodes(true);
                            var res = []
                            if (treeNode.type == "group" || treeNode.type == "assignment") {
                                nodes = nodes.concat(_this.getChildNodes(treeNode))
                            } else{ //勾选车辆的话需要把当前勾选的行推入 res 中，否则判断时会少一行
                                res.push('fakeMsg')
                            }
                            nodes.forEach(function (node) {
                                if ((node.type == "people" || node.type == "vehicle") && res.indexOf(node.id) == -1) {
                                    res.push(node.id)
                                }
                            })
                            if (res.length > 500) {
                                layer.msg('最多勾选' + 500 + '个监控对象');
                                return false;
                            }
                        }
                        return true;
                    }
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        // 获取节点的所有子节点(包当前节点) return nodes[]
        getChildNodes: function(treeNode) {
            var arr = [];
            var loop = function(treeNode){
                arr.push(treeNode);
                if (treeNode.isParent) {
                    for (var obj in treeNode.children) {
                        loop(treeNode.children[obj]);
                    }
                }
            }
            loop(treeNode)
            return arr;
        },
        createSearchTree: function (param) {
            var querySetting = {
                async: {
                    url: "/clbs/a/search/reportFuzzySearch",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": $('#queryType').val(), "queryParam": param, 'queryType': 'multiple'},
                    dataFilter: _this.ajaxQueryDataFilter
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
                    beforeClick: _this.beforeClickVehicle,
                    onCheck: _this.onCheckVehicle,
                    onExpand: _this.zTreeOnExpand,
                    onAsyncSuccess: _this.zTreeOnAsyncSuccess,
                    // beforeCheck: _this.zTreeBeforeCheck,
                    // onNodeCreated: _this.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), querySetting, null);
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
        createSimpleTable:function(){
            var columnDefine = [
                {
                    name: 'brand',
                },
                {
                    name: 'inspectionType',
                    render:function (data) {
                        switch (data) {
                            case 1:
                                return '车辆运行监测巡检'
                            case 2:
                                return '驾驶行为监测巡检'
                            default :
                                return '-'
                        }
                    }
                },
                {
                    name: 'inspectionStatusStr'
                }
            ]
            simpleTable = new SimpleTable('simpleDataBox', {
                columnDefine:columnDefine,
                rowKey:function (data) {
                    return data.vehicleId + '_' +data.inspectionType
                }
            })
            simpleTable.render()
        },

        createTree1:function(){
            var setting = {
                async: {
                    url: _this.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: _this.ajaxDataFilter
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
                    beforeClick: _this.beforeClickVehicle11,
                    beforeCheck: _this.zTreeBeforeCheck11,
                    onAsyncSuccess: _this.zTreeOnAsyncSuccess11,
                    onCheck: _this.onCheckVehicle11,
                    onExpand: _this.zTreeOnExpand11,
                    onNodeCreated: _this.zTreeOnNodeCreated,
                }
            };
            $.fn.zTree.init($("#treeDemo_11"), setting, null);
        },
        createSearchTree1: function (param) {
            var querySetting = {
                async: {
                    url: "/clbs/a/search/reportFuzzySearch",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    // otherParam: {"type": $('#queryType_11').val(), "queryParam": param, 'queryType': 'multiple'},
                    otherParam: {"type": 'vehicle', "queryParam": param, 'queryType': 'multiple'},
                    dataFilter: _this.ajaxQueryDataFilter
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
                    beforeClick: _this.beforeClickVehicle11,
                    onCheck: _this.onCheckVehicle11,
                    onExpand: _this.zTreeOnExpand11,
                    onAsyncSuccess: _this.zTreeOnAsyncSuccess11,
                }
            };
            $.fn.zTree.init($("#treeDemo_11"), querySetting, null);
        },

        zTreeBeforeCheck11: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo_11"), nodes = zTree
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
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo_11"), nodes = zTree.getCheckedNodes(true);
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
                    layer.msg('最多勾选' + 5000 + '个监控对象');
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
        zTreeOnExpand11: function (event, treeId, treeNode) {
            //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if (treeNode.type == "group" && !checkFlag) {
                return;
            }
            //初始化勾选操作判断表示
            checkFlag = false;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo_11");
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
            //_this.getCharSelect(treeObj);
        },
        beforeClickVehicle11: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo_11");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        onCheckVehicle11: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo_11");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
            }
            _this.getCharSelect(zTree);
            // _this.validate22()
        },
        zTreeOnAsyncSuccess11: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo_11");
            if(size <= 5000){
                treeObj.expandAll(true);
                if(!firstRender) return
                firstRender = false
                treeObj.checkAllNodes(true);
                var nodes = treeObj.getCheckedNodes(true);
                var h = '';
                treeObj.getCheckedNodes(true);
                for (var i = 0; i < nodes.length; i++) {
                        if(nodes[i].type == 'vehicle'){
                            h += nodes[i].name + '、'
                        }
                }

                $("#groupSelect_11").val(h);
                $("#inquireClick").click()
            }
        },
        fuzzyZTreeOnAsyncSuccess11: function (event, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo_11");
            zTree.expandAll(true);
        },
        fuzzyOnCheckVehicle11: function (e, treeId, treeNode) {
            //获取树结构
            var zTree = $.fn.zTree.getZTreeObj("treeDemo_11");
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
            _this.vehicleListId(); // 记录勾选的节点
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
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree.getCheckedNodes(true);
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
                if (nodesLength > 500) {
                    layer.msg('最多勾选' + 500 + '个监控对象');
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
            //_this.getCharSelect(treeObj);
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
            }
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.expandAll(true);
        },
        getTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
        },
        //组织树预处理加载函数
        ajaxDataFilter: function (treeId, parentNode, responseData) {
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
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData));
            var nodesArr = filterQueryResult(responseData);
            for (var i = 0; i < nodesArr.length; i++) {
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        fuzzyZTreeOnAsyncSuccess: function (event, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.expandAll(true);
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
            _this.vehicleListId(); // 记录勾选的节点
        },

        getCheckedVehicles: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo_11")
            if(!zTree) return
            var nodes = zTree.getCheckedNodes(true), v = "", vid = "";
            return nodes.filter(function (item, index, arr) {
                return item.type == "vehicle" && arr.indexOf(item) === index
            })
        },
        getCharSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $("#groupSelect_11").val(allNodes[0].name);
            } else {
                $("#groupSelect_11").val("");
            }
        },


        inquireClick: function (number) {
            var referDate = $('#timeRange').val().split('--')
            var endDate = _this.getDate(-1,referDate[0].split(' ')[0])
            var startDate = _this.getDate(number + 1,endDate)
            var startTime = number == 0 ? referDate[0].split(' ')[1] : '00:00:00'
            var endTime = number == 0 ? referDate[1].split(' ')[1] : '23:59:59'
            if(number == 'today'){
                startDate = _this.getDate(0,new Date())
                endDate = _this.getDate(0,new Date())
            }
            if(number != 0){
                $('#timeRange').val(startDate  + ' ' + startTime + '--' + endDate  + ' ' + endTime);
            }
            _this.query()
        },
        getDate:function(number,referDate){
            if(referDate){
                referDate = new Date(referDate).Format('yyyy-MM-dd')
            }else {
                referDate = new Date().Format('yyyy-MM-dd')
            }
            var targetDate = new Date(referDate).getTime() + 24*60*60*1000*number
            return new Date(targetDate).Format('yyyy-MM-dd')
        },
        exportAlarm: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if(!_this.validate22()) return
            var params = _this.getSearchParams()
            exportExcelUsePost('/clbs/adas/platformInspection/export',params)
        },


        // 获取查询参数
        getSearchParams:function(){
            var startTime,endTime,simpleQueryParam = $('#simpleQueryParam').val(),inspectionType = $('#inspectionType').val()
            var timeRange = $('#timeRange').val()
            if(timeRange){
                startTime = timeRange.split('--')[0]
                endTime = timeRange.split('--')[1]
            }
            var vehicleIds = platformInspection.getCheckedVehicles()
            return {
                keyword:simpleQueryParam,
                startTime:startTime || new Date().Format('yyyy-MM-dd') + ' ' + '00:00:00',
                endTime:endTime  || new Date().Format('yyyy-MM-dd') + ' ' +'23:59:59',
                inspectionType:inspectionType,
                vehicleIds:vehicleIds && vehicleIds.map(function (item) { return item.id }).join(',')
            }
        },
        // 刷新
        refreshTable:function () {
            $('#simpleQueryParam').val('')
            _this.search()
        },
        // 展示模态框
        showInspectionModal:function () {
            $("#inspectionModal").modal('show')
            _this.createTree()
            _this.createSimpleTable()
        },
        // 下发指令
        doSubmit:function(){
            simpleTable.empty()
            webSocket.unsubscribealarm(headers, "/app/unsubscribe/inspection", {time: inspectionSendTime});
            var vehicleIdAndBrands = util.getUniqueCheckedNodes().map(function (item) {
                return {
                    id:item.id,
                    brand:item.name
                }
            })
            var inspectionType = []
            $('#inspectionAction input').each(function(){
                if($(this).prop('checked')){
                    inspectionType.push($(this).val())
                }
            })
            var params = []
            inspectionSendTime = new Date().getTime();
            vehicleIdAndBrands.forEach(function (item) {
                inspectionType.forEach(function (item2) {
                    var temp = {}
                    temp.vehicleId = item.id
                    temp.inspectionType = item2
                    temp.brand = item.brand
                    temp.time = inspectionSendTime
                    params.push(temp)
                })
            })
            var updataTable = function (msg) {
                if (msg != null) {
                    var result = $.parseJSON(msg.body);
                    if (result != null) {
                        var id = result.vehicleId + '_' + result.inspectionType
                        simpleTable.modifyOrCreate(id,result)
                    }
                }
            }
            isClickSubmitButton = true
            webSocket.subscribe(headers, "/user/topic/inspection", updataTable, "/app/inspection", params);
        },
        // 下发指令前验证
        validate:function(){
            var isAnyoneChecked = false
            $('#inspectionAction input').each(function(){
                if($(this).prop('checked')){
                    isAnyoneChecked = true
                }
            })
            if(!isAnyoneChecked){
                layer.msg('至少选择一个巡检外设')
                return false
            }
            if(util.getUniqueCheckedNodes().length == 0){
                layer.msg('至少选择一个监控对象')
                return false
            }
            return true
        },
        // 搜索查询前验证
        validate22: function () {
            var checkedVehicles = _this.getCheckedVehicles()
            if (checkedVehicles.length == 0) {
                $('#groupSelect_11').after('<p class="random_jhsdkjahs" style="color: #fff;background: #b94a48;border: solid thin #fff;padding: 3px 5px;position: absolute;font-weight: bold;z-index: 10;">请选择监控对象</p>')
                return false
            } else {
                $('#groupSelect_11').parent().find('.random_jhsdkjahs').remove()
                return true
            }
        },
        // 搜索
        search:function () {
            myTable.requestData()
        },
        // 查询
        query:function () {
            if(!_this.validate22()) return
            myTable.requestData()
        },
        //重置
        resetSearchParam:function () {
            $('#inspectionType').val(0)
            var today = new Date().Format('yyyy-MM-dd')
            $('#timeRange').val(today + ' 00:00:00' + '--' + today + ' 23:59:59');
            myTable.requestData()
        },
        // 显示查询结果弹窗
        showResultModal:function(id){
            var data = tableData.find(function (item) {
                return item.id == id
            })
            if(!data) return
            // 1 "下发中", 2 "下发成功 ",3  "终端响应超时", 4 "终端离线"
            switch (data.inspectionStatus) {
                case 3:
                    return layer.msg('终端响应超时，无结果')
                case 4:
                    return layer.msg('终端离线，未下发')
                case 1:
                    return layer.msg('下发中，请稍后')
            }
            currentClickedRow = data
            $("#resultModal").modal('show')
            // 1 "车辆运行监测巡检", 2 "驾驶员驾驶行为监测巡检 ", 3  "驾驶员身份识别巡检"
            if(data.inspectionStatus == 2 && data.inspectionType == 3){
                _this.renderResult(1,data.inspectionResultId,data.inspectionType)
                return
            } else if(data.inspectionStatus == 2 && data.inspectionType == 1){
                _this.renderResult(2,data.inspectionResultId,data.inspectionType)
                return
            } else if(data.inspectionStatus == 2 && data.inspectionType == 2){
                _this.renderResult(3,data.inspectionResultId,data.inspectionType)
            }
        },
        // 渲染查询结果弹窗
        renderResult:function (type,inspectionResultId,inspectionType) {
            // 驾驶员身份识别巡检结果
            var setting1 = [
                {
                    title: '所属组织',
                    dataIndex: 'orgName',
                },
                {
                    title: '车牌号',
                    dataIndex: 'brand',
                },
                {
                    title: '驾驶员',
                    dataIndex: 'driverName',
                },
                {
                    title: '从业资格证编号',
                    dataIndex: 'cardNumber',
                },
                {
                    title: '驾驶员人脸信息ID',
                    dataIndex: 'faceId',
                },
                {
                    title: '比对结果',
                    dataIndex: 'identificationResult',
                },
                {
                    title: '比对相似度',
                    dataIndex: 'matchRate',
                },
                {
                    title: '比对类型',
                    dataIndex: 'identificationType',
                },
                {
                    title: '时间',
                    dataIndex: 'time',
                },
            ]
            // 车辆运行监测巡检结果
            var setting2 = [
                {
                    title: '所属组织',
                    dataIndex: 'orgName',
                },
                {
                    title: '车牌号',
                    dataIndex: 'brand',
                },
                {
                    title: '报警类型',
                    dataIndex: 'alarmType',
                },
                {
                    title: '预警类型',
                    dataIndex: 'warnType',
                },
                {
                    title: '有效提醒驾驶员',
                    dataIndex: 'remindFlag',
                    render:function (data) {
                        switch (data) {
                            case 0:
                                return '否'
                            case 1:
                                return '是'
                            default :
                                return '--'
                        }
                    }
                },
                {
                    title: '道路偏离类型',
                    dataIndex: 'departureType',
                },
                {
                    title: '报警开始时间',
                    dataIndex: 'time',
                },
                {
                    title: '超速报警标志',
                    dataIndex: 'speedStatus',
                },
                {
                    title: '线路偏离报警标志',
                    dataIndex: 'departureStatus',
                },
                {
                    title: '线路/区域',
                    dataIndex: 'route',
                },
                {
                    title: '禁行路段/区域报警标志',
                    dataIndex: 'pohibitedStatus',
                },
                {
                    title: '禁行类型',
                    dataIndex: 'pohibitedType',
                },
            ]
            // 驾驶行为监测巡检结果
            var setting3 = [
                {
                    title: '所属组织',
                    dataIndex: 'orgName',
                },
                {
                    title: '车牌号',
                    dataIndex: 'brand',
                },
                {
                    title: '驾驶员',
                    dataIndex: 'driverName',
                },
                {
                    title: '报警类型',
                    dataIndex: 'alarmType',
                },
                {
                    title: '预警类型',
                    dataIndex: 'warnType',
                },
                {
                    title: '有效提醒驾驶员',
                    dataIndex: 'remindFlag',
                    render:function (data) {
                        switch (data) {
                            case 0:
                                return '否'
                            case 1:
                                return '是'
                            default :
                                return '--'
                        }
                    }
                },
                {
                    title: '时间',
                    dataIndex: 'time',
                },
            ]
            var setting = null
            var $modalTitle = $('#modalTitle')
            switch (type) {
                case 1:
                    setting = setting1
                    $modalTitle.html('驾驶员身份识别巡检结果')
                    break
                case 2:
                    setting = setting2
                    $modalTitle.html('车辆运行监测巡检结果')
                    break
                default:
                    setting = setting3
                    $modalTitle.html('驾驶行为监测巡检结果')
            }
            var params = {
                inspectionResultId:inspectionResultId,
                inspectionType:inspectionType,
            }
            json_ajax("GET", '/clbs/adas/platformInspection/getInspectionResult', "json", true, params, function (result) {
                if(result.obj){
                    var data = result.obj
                    _this.renderPart1(data,setting)
                    _this.renderPart2(data.videoInfo.videoList)
                    _this.renderPart3(data.imageInfo.imageList)
                }
            });
        },
        renderPart1:function (obj,setting) {
            var html = ''
            setting.forEach(function (item) {
                var dataHtml = ''
                if(typeof item.render == 'function'){
                    dataHtml = item.render(obj[item.dataIndex],obj)
                }else {
                    dataHtml = obj[item.dataIndex] || '--'
                }
                html += '<div class="col-md-4" style="padding: 0">\n' +
                    '<strong class="col-md-6 text-right" style="padding: 0;padding-right: 9px;">' + item.title +': </strong>\n' +
                    '<p class="col-md-6" style="padding-left: 0;padding: 0">' + dataHtml +'</p>\n' +
                    '</div>'
            })
            $('#part1').html(html)
        },
        renderPart2:function (srcArr) {
            swiper1 = new Swiper('#vedioSwiperWrapper',{
                srcArr:srcArr,
                indicatorType:'number',
                height:'300',
                widthRatio:0.75,
                tagName:'video',
                empty:'<p>暂无视频</p>'
            })
        },
        renderPart3:function (srcArr) {
            swiper2 = new Swiper('#imgSwiperWrapper',{
                srcArr:srcArr,
                indicatorType:'number',
                height:'300',
                widthRatio:0.75,
                empty:'<p>暂无图片</p>'
            })
        },

        ztreeSearch: function () {
            if (searchTimeout !== undefined) {
                clearTimeout(searchTimeout);
            }
            searchTimeout = setTimeout(function () {
                _this.search_condition();
            }, 500);
        },
        search_condition: function (event) {
            var value = $("#groupSelect").val();
            var url = '/clbs/m/basicinfo/monitoring/vehicle/treeStateInfos';
            var queryType = $('#queryType').val();
            var otherParam = {'webType': 2,'queryType': 'monitor', 'type': 1, 'queryType': queryType,'devType': 25};
            if (value != '') {
                otherParam.queryParam = value;
            }
            if (queryType == 'vehType') {
                otherParam.queryParam = $('#vehicleType').val();
            }
            _this.initTree(url, otherParam);
        },
        initTree: function (url, otherParam) {
            var setting = {
                async: {
                    url: url,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: otherParam,
                    dataFilter:function (treeId, parentNode, responseData) {
                        var data = responseData;
                        if ($('#queryType').val() == "monitor" || $('#queryType').val() == "vehType") {
                            data = filterQueryResult(responseData, []);
                        }
                        for (var i = 0; i < data.length; i++) {
                            data[i].open = true;
                            if (data[i].type == 'vehicle' || data[i].type == 'people' || data[i].type == 'thing') {
                                data[i].isParent = false;
                            }
                        }
                        return data;
                    }
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
                    onAsyncSuccess: _this.zTreeOnAsyncSuccess,
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        getVehicleTypes: function () {
            var url = '/clbs/m/reportManagement/vehGeneralInfo/getVehTypes';
            json_ajax("POST", url, "json", true, null, function (data) {
                var result = data;
                var html = '';
                for (var i = 0, len = result.length; i < len; i++) {
                    html += '<option value="' + result[i].type + '">' + result[i].type + '</option>'
                }
                $('#vehicleType').html(html);
            });
        },
    };
    var _this = platformInspection
    var util = {
        unique: function (arr) {
            var result = [];
            var tempIds = []
            for(var i = 0; i < arr.length; i++){
                if(tempIds.indexOf(arr[i].id) == -1){
                    tempIds.push(arr[i].id);
                    result.push(arr[i])
                }
            }
            return result;
        },
        getUniqueCheckedNodes:function () {
            var treeObj = $.fn.zTree.getZTreeObj('treeDemo');
            var nodes = treeObj.getCheckedNodes(true);
            nodes = nodes.filter(function (item) {
                return item.type == 'vehicle'
            })
            if(nodes instanceof Array){
                return util.unique(nodes)
            }else {
                return []
            }
        },
    }
    //简易表格 v1.1
    var SimpleTable = function(divId,options){
        if(!divId) throw '请传入divId'
        if(!options) throw '请传入配置项'
        if(!(options.columnDefine instanceof Array)) throw '请传入columnDefine: [{name: string,render?: Function}]'
        options = options || {}
        this.options = {
            withIndex:options.withIndex === undefined ? true: options.withIndex, //带上序号
            rowKey: options.rowKey || 'id',
            columnDefine: options.columnDefine
        }
        this._table = $("#" + divId);
        this.withIndex = function (data){
            if(!data) return
            var newData = data.map(function (item,index) {
                var temp = {}
                for(var key in item){
                    temp[key] = item[key]
                }
                temp.index = index + 1
                return temp
            })
            return newData
        }
        // 渲染
        this.render=function(data){
            if(!data || !this._table) return;
            if(this.options.withIndex){
                data = this.withIndex(data)
            }
            var $body = this._table.find('tbody')
            var bodyHtml = ''
            var that = this
            data.map(function(item){
                bodyHtml += that.renderTr(item)
            })
            $body.html(bodyHtml)
        };
        this.renderTr=function(item){
            var res = '';
            var allTd = ''
            if(this.options.columnDefine){
                this.options.columnDefine.forEach(function (column) {
                    if(column.render){
                        allTd += "<td>" + column.render(item[column.name],item) + "</td>"
                    }else if(item[column.name]) {
                        allTd += "<td>" + item[column.name] + "</td>"
                    }else {
                        allTd += "<td>" + "--" + "</td>"
                    }
                })
            }
            var rowKey = this.options.rowKey
            if(typeof this.options.rowKey == 'function'){
                rowKey = this.options.rowKey(item)
            }
            if(this.options.withIndex){
                var index = Number($("#simpleDataBox tbody tr:last td:first").html()) || 0
                res = "<tr id="+ rowKey +">"
                    +"<td>"+ (item.index ? item.index : index+1) +"</td>"
                    + allTd
                    +"</tr>";
            }else {
                res = "<tr id="+ rowKey +">"
                    + allTd
                    +"</tr>";
            }

            return res
        };
        // 修改
        this.modifyTr= function(id,tdIndex,html){
            this._table.find("#" + id + " " + "td").eq(tdIndex).html(html)
        };
        // 修改某一行
        this.updateRow= function(id,data){
            var that = this
            var keys = Object.keys(data)
            keys.forEach(function (keyItem) {
                var tdIndex = 0
                var column = that.options.columnDefine.find(function (_item,index) {
                    if(that.options.withIndex){
                        tdIndex = index + 1
                    }else {
                        tdIndex = index
                    }
                    return _item.name ==  keyItem
                })
                var td = ''
                if(column){
                    td = column.render ? column.render(data[keyItem]) : data[keyItem]
                    that._table.find("#" + id + " " + "td").eq(tdIndex).html(td)
                }
            })
        };
        // 新增
        this.addItem=function (data,index) {
            var res = this.renderTr(data)
            if(index){
                this._table.find('tr').eq(index).after(res)
            }else {
                this._table.find("tbody").append(res)
            }
        };
        // 删除
        this.del=function (id) {

        };
        // 销毁
        this.destory=function () {
            this._table.remove()
        };
        // 刷新
        this.refresh=function (data) {
            this.render(data,this.columnDefine)
        };
        // 修改或者新增某一行 （有当前行则修改，否则就新增）
        this.modifyOrCreate = function (id,data) {
            if(this._table.find("#" + id).length != 0){
                this.updateRow(id,data)
            }else {
                this.addItem(data)
            }

        }
        // 清空数据
        this.empty = function () {
            this._table.find('tbody').empty()
        }
    }
    $(function () {
        _this.init()

        // 图片视频附件切换
        $('#imgAttached').on('click',function () {
            $('#imgAttached').css('background','#20b5f0')
            $('#vedioAttached').css('background','#6dcff6')
            $('#vedioSwiperWrapper').hide()
            $('#imgSwiperWrapper').show()
        })
        $('#vedioAttached').on('click',function () {
            $('#vedioAttached').css('background','#20b5f0')
            $('#imgAttached').css('background','#6dcff6')
            $('#vedioSwiperWrapper').show()
            $('#imgSwiperWrapper').hide()
        })
        // 高级搜索展示/隐藏
        $('#advanced_search').on('click',function () {
            $('#advanced_content').slideToggle()
        })
        // 刷新按钮
        $('#refreshTable').on('click',_this.refreshTable)
        // 展示模态框
        $('#inspection').on('click',_this.showInspectionModal)
        // 搜索
        $('#search_button').on('click',_this.search)
        // 查询
        $('#query_button').on('click',_this.query)
        // 重置
        $('#resetBtn').on('click',_this.resetSearchParam)
        // 回车搜索
        $('#simpleQueryParam').on('keydown',function (e) {
            if(e.which === 13){
                _this.search()
                e.preventDefault()
            }
        })
        // 模糊搜索
        // $("#groupSelect").on('input', function (value) {
        //     clearTimeout(inputChangeTimer);
        //     inputChangeTimer = setTimeout(function () {
        //         var param = $("#groupSelect").val();
        //         if (param == '') {
        //             _this.createTree();
        //         } else {
        //             _this.createSearchTree(param);
        //         }
        //     }, 500);
        // });
        $('#groupSelect').on('input propertychange', _this.ztreeSearch);
        // 下发巡检
        $('#submitBtn').on('click',function () {
            if(!_this.validate()) return
            $('#submitBtn').attr('disabled', true)
            setTimeout(function () {
                $('#submitBtn').attr('disabled', false)
            },5000)
            _this.doSubmit()
        })
        // 输入框增强快速清空
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                _this.ztreeSearch();
            }
            if (id == 'groupSelect_11') {
                _this.createTree1('');
            }
        });
        var today = new Date().Format('yyyy-MM-dd')
        // 时间选择控件
        $('#timeRange').dateRangePicker({
            dateLimit: 60,
        })
        // 导出
        $('#exportAlarm').on('click',platformInspection.exportAlarm)
        //
        $('#groupSelect_11').on('click',function (e) {
            e.stopPropagation()
            $('#menuContent_11').slideToggle()
            var $box = $("#menuContent_11")
            $(document).on("click", function(e){
                if(!$box.parent().get(0).contains(e.target)){
                    $box.hide()
                }
            });
        })
        // 模糊搜索
        $("#groupSelect_11").on('input', function (value) {
            clearTimeout(inputChangeTimer);
            inputChangeTimer = setTimeout(function () {
                var param = $("#groupSelect_11").val();
                if (param == '') {
                    _this.createTree1();
                } else {
                    _this.createSearchTree1(param);
                }
            }, 500);
        });

        $('#queryType').on('change', function () {
            var curVal = $(this).val();
            if (curVal == 'vehType') {
                $('#vehicleType').show();
            } else {
                $('#vehicleType').hide();
            }
            _this.ztreeSearch();
        });
        $('#vehicleType').on('change', function () {
            _this.ztreeSearch();
        });

        $("#inspectionModal, #resultModal").on('hide.bs.modal',function (e) {
            $('#groupSelect').val('')
            $('#queryType').val('monitor')
            $('#vehicleType').hide()
            $('#inspectionAction input').prop('checked','checked')
            $('#imgAttached').click()
            simpleTable && simpleTable.empty()
            webSocket.unsubscribealarm(headers, "/app/unsubscribe/inspection", {time: inspectionSendTime});
            swiper1 && swiper1.destroy()
            swiper2 && swiper2.destroy()
            if(isClickSubmitButton){
                myTable.requestData()
                isClickSubmitButton = false
            }
        })
    })
})(window, $)