(function (window, $) {
    //单选
    var subChk = $("input[name='subChk']");
    var menu_text = "";
    var table = $("#dataTable tr th:gt(1)");
    //车辆id列表
    var vehicleId = [];
    var checkFlag;
    var forwardVehicleInputChange;
    var groupSelect = $("#groupSelect");
    var platformItems = $("button[class*='list-group-item']");
    var selectedPlatform = 0;
    var zTreeIdJson = {};
    var forwardMgr = {
        init: function () {
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"2\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + (i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);
            forwardMgr.zTreeInit();
            forwardMgr.initTable();
            platformItems.click(function () {
                platformItems.removeClass("active");
                $(this).addClass("active");
                selectedPlatform = parseInt(this.getAttribute("data-value"), 10);
                forwardMgr.myTable.refresh();
            });
        },
        initTable: function () {
            //表格列定义
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columns = [
                {
                    //第一列，用来显示序号
                    "data": null,
                    "class": "text-center"
                },
                {
                    "data": null,
                    "class": "text-center",
                    render: function (data, type, row) {
                        return '<input type="checkbox" name="subChk"  value="' + row.id + '" data-platform-id="' + row.plantFormId + '" />';
                    }
                },
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row) {
                        //删除按钮
                        return '<button type="button" class="deleteButton editBtn disableClick" data-row-id="' + row.id +
                            '" data-platform-id="' + row.plantFormId + '"><i class="fa fa-trash-o"></i>解除</button>';
                    }
                }, {
                    "data": "brand",
                    "class": "text-center"
                }, {
                    "data": "plantFormName",
                    "class": "text-center",
                    render: function (data, type, row) {
                        if (row.plantFormId === "1") {
                            return "四川网约车";
                        } 
                            return "广西农垦"
                        
                    },
                }, {
                    "data": "plantFormIp",
                    "class": "text-center",
                    render: function (data, type, row) {
                        if (row.plantFormId === "1") {
                            return "171.221.172.42";
                        } 
                            return "111.59.154.47"
                        
                    },
                }, {
                    "data": "plantFormPort",
                    "class": "text-center",
                    render: function (data, type, row) {
                        if (row.plantFormId === "1") {
                            return "90";
                        } 
                            return "1433"
                        
                    },
                }
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.intercomPlatformId = selectedPlatform;
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/m/forward-platform/net-car/list',
                editUrl: '',
                deleteUrl: '',
                deletemoreUrl: '',
                enableUrl: '/clbs/c/user/enable_',
                disableUrl: '/clbs/c/user/disable_',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                drawCallbackFun: function () {
                    $(".deleteButton").bind('click', forwardMgr.deleteOne);
                }
            };
            forwardMgr.myTable = new TG_Tabel.createNew(setting);
            forwardMgr.myTable.init();
        },
        // 显示错误提示信息
        showErrorMsg: function (msg, inputId) {
            var errorLabel = $("#error_label");
            if (errorLabel.is(":hidden")) {
                errorLabel.text(msg);
                errorLabel.insertAfter($("#" + inputId));
                errorLabel.show();
            } else {
                errorLabel.is(":hidden");
            }
        },
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#error_label").hide();
        },
        zTreeInit: function () {
            //车辆树
            var setting = {
                async: {
                    url: forwardMgr.getParkingReportTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: forwardMgr.ajaxDataFilter
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
                    beforeClick: forwardMgr.beforeClickVehicle,
                    onAsyncSuccess: forwardMgr.zTreeOnAsyncSuccess,
                    beforeCheck: forwardMgr.zTreeBeforeCheck,
                    onCheck: forwardMgr.onCheckVehicle,
                    onExpand: forwardMgr.zTreeOnExpand,
                    onNodeCreated: forwardMgr.zTreeOnNodeCreated,
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
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
        getParkingReportTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/connectionparamsConfig/t809ForwardTree";
            } else if (treeNode.type === "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle&webType=3";
            }
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function () {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            forwardMgr.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                    nodes = zTree.getCheckedNodes(true);
                var nodesLength = 0;
                //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                var ns = [];
                //节点id
                var nodeId;
                if (treeNode.type === "group" || treeNode.type === "assignment") { //若勾选的为组织或分组
                    json_ajax("post", "/clbs/m/personalized/ico/getVehicleCount",
                        "json", false, {"parentId": treeNode.id, "type": treeNode.type, "webType": 3}, function (data) {
                            if (data.success) {
                                nodesLength += data.obj;
                            } else {
                                layer.msg(data.msg);
                            }
                        });

                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type === "people" || nodes[i].type === "vehicle") {
                            //查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                            var nd = zTree.getNodeByParam("tId", nodes[i].tId, treeNode);
                            if (nd == null && $.inArray(nodeId, ns) === -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength += ns.length;
                } else if (treeNode.type === "people" || treeNode.type === "vehicle") { //若勾选的为监控对象
                    for (i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type === "people" || nodes[i].type === "vehicle") {
                            if ($.inArray(nodeId, ns) === -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength = ns.length + 1;
                }
                if (nodesLength > 5000) {
                    layer.msg('最多勾选5000个监控对象');
                    flag = false;
                }
            }
            if (flag) {
                //若组织节点已被勾选，则是勾选操作，改变勾选操作标识
                if (treeNode.type === "group" && !treeNode.checked) {
                    checkFlag = true;
                }
            }
            return flag;
        },
        add: function () {
            forwardMgr.getCheckedNodes();
            if (vehicleId.length <= 0) {
                forwardMgr.showErrorMsg("请选择车辆", "groupSelect");
                return;
            }
            forwardMgr.hideErrorMsg();
            var platform = $("#platform").val();
            var param = {
                vehicleIds: vehicleId,
                platformId: platform,
            };
            json_ajax("POST", "/clbs/m/forward-platform/net-car/add", "json", true, param, forwardMgr.addConfigCallBack);
        },
        addConfigCallBack: function (data) {
            if (data.success) {
                groupSelect.val("");
                forwardMgr.myTable.refresh();
                forwardMgr.zTreeInit();
            } else {
                layer.msg(data.msg);
            }
        },
        addressOnChange: function () {
            $("#platformId").val("");
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
            }
            forwardMgr.getCharSelect(zTree);
            forwardMgr.hideErrorMsg();
        },
        zTreeOnExpand: function (event, treeId, treeNode) {
            //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if (treeNode.type === "group" && !checkFlag) {
                return;
            }
            //初始化勾选操作判断表示
            checkFlag = false;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            /*if (treeNode.type === "group") {
              var assign = []; // 当前组织及下级组织的所有分组
              forwardMgr.getGroupChild(treeNode, assign);
              if (assign.length === 0) {
                return;
              }
              for (var i = 0; i < assign.length; i++) {
                var node = assign[i];
                if (node.type === "assignment" && node.children === undefined && !node.zAsync) {
                  // 判断节点是否进行异步加载，若没有，则先异步加载，避免添加重复节点
                  treeObj.reAsyncChildNodes(node, "refresh");
                }
              }
            }*/
            if (treeNode.type == "group") {
                var url = "/clbs/m/functionconfig/fence/bindfence/putMonitorByGroup";
                json_ajax("post", url, "json", false, {
                    "groupId": treeNode.id,
                    "isChecked": treeNode.checked,
                    "monitorType": "vehicle"
                }, function (data) {
                    var result = data.obj;
                    var parentNode;
                    if (result != null && result != undefined) {
                        $.each(result, function (i) {
                            var pid = i; //获取键值
                            var chNodes = result[i];//获取对应的value
                            if (zTreeIdJson[pid] != undefined) {
                                var parentTid = zTreeIdJson[pid][0];
                                parentNode = treeObj.getNodeByTId(parentTid);
                                if (parentNode.children === undefined) {
                                    treeObj.addNodes(parentNode, []);
                                }
                            }
                        });
                    }
                })
            }
        },
        getGroupChild: function (parentNode, assign) { // 递归获取组织及下级组织的分组节点
            var nodes = parentNode.children;
            if (nodes !== null && nodes !== undefined && nodes.length > 0) {
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.type === "assignment") {
                        assign.push(node);
                    } else if (node.type === "group" && node.children !== undefined) {
                        forwardMgr.getGroupChild(node.children, assign);
                    }
                }
            }
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                .getCheckedNodes(true), v = "", vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type === "vehicle") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].id + ",";
                }
            }
            vehicleId = vid.slice(0, vid.length - 1);
        },
        getCharSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            allNodes = allNodes ? allNodes : [];
            if (nodes.length > 0) {
                groupSelect.val(allNodes[0].name);
            } else {
                groupSelect.val("");
            }
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var obj;
            if (responseData.msg) {
                obj = JSON.parse(ungzip(responseData.msg));
            } else {
                obj = JSON.parse(ungzip(responseData));
            }
            var data;
            if (obj.tree !== null && obj.tree !== undefined) {
                data = obj.tree;
            } else {
                data = obj
            }
            for (var i = 0; i < data.length; i++) {
                data[i].open = true;
            }
            return data;
        },
        ajaxSearchDataFilter: function (treeId, parentNode, responseData) {
            var data = forwardMgr.ajaxDataFilter(treeId, parentNode, responseData);
            data = filterQueryResult(data, []);
            return data;
        },
        //批量删除
        delModelClick: function () {
            //判断是否至少选择一项
            var checkedMonitors = $("input[name='subChk']:checked");
            var chechedNum = checkedMonitors.length;
            if (chechedNum === 0) {
                layer.msg(selectItem, {move: false});
                return

            }
            var checkedList = [];
            checkedMonitors.each(function () {
                checkedList.push($(this).val());
                checkedList.push(this.attributes['data-platform-id'].value);
            });
            var param = {'id': checkedList.toString()}
            var platformListUrl = "/clbs/m/forward-platform/net-car/delete";
            layer.confirm("删掉就没啦，请谨慎下手！", {btn: ["确定", "取消"]}, function () {
                json_ajax("POST", platformListUrl, "json", true, param, forwardMgr.deleteCall);
            });
        },
        deleteCall: function (data) {
            if (data.success) {
                layer.msg("删除成功！", {move: false});
                forwardMgr.myTable.refresh();
                forwardMgr.zTreeInit();
            } else {
                layer.msg("删除失败！请联系平台管理员", {move: false});
            }
        },
        deleteOne: function () {
            var id = this.attributes['data-row-id'].value;
            var platformId = this.attributes['data-platform-id'].value;
            var param = {
                'id': [id, platformId].toString(),
            };
            var platformListUrl = "/clbs/m/forward-platform/net-car/delete";
            layer.confirm("删掉就没啦，请谨慎下手！", {btn: ["确定", "取消"]}, function () {
                json_ajax("POST", platformListUrl, "json", true, param, forwardMgr.deleteCall);
            });
        },
        //加载完成后执行
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            forwardMgr.myTable.requestData();
        },
        //全选
        checkAll: function () {
            $("input[name='subChk']").prop("checked", this.checked);
        },
        //单选
        subChkClick: function () {
            $("#checkAll").prop("checked", subChk.length === subChk.filter(":checked").length);
        },
        //车辆模糊查询
        searchVehicleSearch: function () {
            if (forwardVehicleInputChange !== undefined) {
                clearTimeout(forwardVehicleInputChange);
            }
            forwardVehicleInputChange = setTimeout(function () {
                var param = groupSelect.val();
                if (param === '') {
                    forwardMgr.zTreeInit();
                } else {
                    forwardMgr.searchVehicleTree(param);
                }
            }, 500);
        },
        //车辆树模糊查询方法
        searchVehicleTree: function (param) {
            var setQueryChar = {
                async: {
                    url: "/clbs/m/personalized/ico/vehicleTreeFuzzy",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    sync: false,
                    otherParam: {"type": "multiple", "queryParam": param, "webType": 3},
                    dataFilter: forwardMgr.ajaxSearchDataFilter
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
                    beforeClick: forwardMgr.beforeClickVehicle,
                    onAsyncSuccess: forwardMgr.fuzzyZTreeOnAsyncSuccess,
                    onCheck: forwardMgr.fuzzyOnCheckVehicle,
                }
            };
            $.fn.zTree.init($("#treeDemo"), setQueryChar, null);
        },
        fuzzyZTreeOnAsyncSuccess: function () {
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
                    if (checkedNodes[i].type === "people" || checkedNodes[i].type === "vehicle") {
                        nodesLength += 1;
                    }
                }
            }
            //获取勾选状态被改变的节点并改变其原来勾选状态（用于5000准确校验）
            for (i = 0; i < changeNodes.length; i++) {
                changeNodes[i].checkedOld = changeNodes[i].checked;
            }
            forwardMgr.getCheckedNodes(); // 记录勾选的节点
        },
    };
    $(function () {
        $('input').inputClear();
        forwardMgr.init();
        $("#btnAdd").bind('click', forwardMgr.add);
        $("#checkAll").bind("click", forwardMgr.checkAll);
        subChk.bind("click", forwardMgr.subChkClick);
        groupSelect.bind("click", showMenuContent);
        $("#del_model").on("click", forwardMgr.delModelClick);
        $("#refreshTable").on("click", forwardMgr.refreshTable);
        groupSelect.bind('input oninput', forwardMgr.searchVehicleSearch);
        if (forwardMgr.myTable) {
            $("#search_button").bind('click', forwardMgr.myTable.requestData);
        }
        $('#simpleQueryParam').on('keydown',function (e) {
            if(e.keyCode == 13){
                forwardMgr.myTable.requestData()
                return false
            }
        })
    });
}(window, $))