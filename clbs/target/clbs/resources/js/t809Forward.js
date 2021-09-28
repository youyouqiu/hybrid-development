;(function (window, $) {
    //单选
    var subChk = $("input[name='subChk']");
    var menu_text = "";
    var table = $("#dataTable tr th:gt(1)");
    //车辆列表
    //车辆id列表
    var vehicleId = [];
    var checkFlag;
    var forwardVehicleInputChange;
    var selectFlag = false;
    var groupSelect = $("#groupSelect");
    var zTreeIdJson = {};
    var submitStatus = true;
    t809ForwardManagement = {
        init: function () {
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"2\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + (i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);
            t809ForwardManagement.zTreeinit();
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
            },
                {
                    "data": null,
                    "class": "text-center",
                    render: function (data, type, row) {
                        var result = '';
                        result += '<input  type="checkbox" name="subChk"  value="' + row.id + '" />';
                        return result;
                    }
                },
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row) {
                        var result = '';
                        //删除按钮
                        if (row.protocolType === '1603') {
                            result += '<button disabled type="button"  class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>解除</button>&ensp;';

                        } else {
                            result += '<button type="button" onclick="t809ForwardManagement.deleteOne(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>解除</button>';

                        }
                        return result;
                    }
                }, {
                    "data": "brand",
                    "class": "text-center"
                }, {
                    "data": "orgName", // 所属企业
                    "class": "text-center"
                }, {
                    "data": "plantFormName",
                    "class": "text-center"
                }, {
                    "data": "plantFormIp",
                    "class": "text-center",
                }, {
                    "data": "plantFormPort",
                    "class": "text-center",
                }
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/m/connectionparamsConfig/list',
                editUrl: '',
                deleteUrl: '/clbs/m/connectionparamsConfig/delete_',
                deletemoreUrl: '/clbs/m/connectionparamsConfig/deletemore',
                enableUrl: '/clbs/c/user/enable_',
                disableUrl: '/clbs/c/user/disable_',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
            var parameterList = '{"id" : ""}';
            var platformListUrl = "/clbs/m/connectionparamsset/list";
            json_ajax("POST", platformListUrl, "json", true, parameterList, t809ForwardManagement.getPlatformList);

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
        zTreeinit: function () {
            //车辆树
            var setting = {
                async: {
                    url: t809ForwardManagement.getParkingReportTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "type": "multiple",
                        "icoType": "0"
                    },
                    dataFilter: t809ForwardManagement.ajaxDataFilter
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
                    beforeClick: t809ForwardManagement.beforeClickVehicle,
                    onAsyncSuccess: t809ForwardManagement.zTreeOnAsyncSuccess,
                    beforeCheck: t809ForwardManagement.zTreeBeforeCheck,
                    onCheck: t809ForwardManagement.onCheckVehicle,
                    onExpand: t809ForwardManagement.zTreeOnExpand,
                    onNodeCreated: t809ForwardManagement.zTreeOnNodeCreated,
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
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
            if (treeNode == null) {
                return "/clbs/m/connectionparamsConfig/t809ForwardTree";
            } else if (treeNode.type === "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle&webType=3";
            }
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function () {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            t809ForwardManagement.getCharSelect(treeObj);
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
                        "json", false, {
                            "parentId": treeNode.id,
                            "type": treeNode.type,
                            "webType": 3
                        },
                        function (data) {
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

        addConfigCheck: function () {
            if (!submitStatus) return;
            submitStatus = false;
            t809ForwardManagement.getCheckedNodes();
            var platFormId = $("#platformId").val();
            var ipAddress = $("#ipAddress").val();
            var protocolType = $("#protocolType").val();
            if (!t809ForwardManagement.validates()) {
                submitStatus = true;
                return
            }
            // if (!selectFlag || platFormId === "" || ipAddress === "" || protocolType === "") {
            //     t809ForwardManagement.showErrorMsg("请选择正确的平台", "ipAddress");
            //     return;
            // } else {
            //     t809ForwardManagement.hideErrorMsg();
            // }
            // if (vehicleId.length <= 0) {
            //     t809ForwardManagement.showErrorMsg("请选择车辆", "groupSelect");
            //     return;
            // } else {
            //     t809ForwardManagement.hideErrorMsg();
            // }
            var param = {
                platFormId: platFormId,
                vehicleIds: vehicleId,
                platFormName: ipAddress,
                protocolType: protocolType
            };
            var platformListUrl = "/clbs/m/connectionparamsConfig/add";
            json_ajax("POST", platformListUrl, "json", true, param, t809ForwardManagement.addConfigCallBack);
        },
        validates: function () {
            return $("#objectForwarding").validate({
                rules: {
                    ipAddress: {
                        required: true
                    },
                    groupSelect: {
                        regularChar: true,
                        zTreeChecked: "treeDemo"
                    },
                },
                messages: {
                    ipAddress: {
                        required: "请选择正确的平台",
                    },
                    groupSelect: {
                        zTreeChecked: "请选择车辆"
                    },
                }
            }).form();
        },
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#error_label").hide();
        },
        addConfigCallBack: function (data) {
            if (data.success) {
                $("#platformId").val("");
                $("#protocolType").val("");
                $("#vehicleIds").val("");
                groupSelect.val("");
                var ipAddress = $("#ipAddress");
                ipAddress.val("");
                groupSelect.val("");
                myTable.refresh();
                t809ForwardManagement.zTreeinit();
                ipAddress.siblings('i').remove();
            } else if (data.msg) {
                layer.msg(data.msg);
            }
            submitStatus = true;
        },
        addressOnChange: function () {
            $("#platformId").val("");
        },
        getPlatformList: function (data) {
            var datas = data.obj;
            var dataList = {
                    value: []
                },
                i = datas.length;
            while (i--) {
                if (datas[i].protocolType !== 1603) {
                    dataList.value.push({
                        name: datas[i].platformName,
                        id: datas[i].id,
                        type: datas[i].protocolType
                    });
                }
            }
            $("#ipAddress").bsSuggest({
                indexId: 1, //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: dataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword) {
                t809ForwardManagement.hideErrorMsg();
                $("#platformId").val(keyword.id);
                $("#protocolType").val(keyword.type);
                selectFlag = true;
            }).on('onUnsetSelectValue', function () {
                selectFlag = false;
            });
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    t809ForwardManagement.validates();
                }, 600);
            }
            t809ForwardManagement.getCharSelect(zTree);
            t809ForwardManagement.hideErrorMsg();
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
                t809ForwardManagement.getGroupChild(treeNode, assign);
                if (assign.length > 0) {
                    for (var i = 0; i < assign.length; i++) {
                        var node = assign[i];
                        if (node.type === "assignment" && node.children === undefined) {
                            if (!node.zAsync) { // 判断节点是否进行异步加载，若没有，则先异步加载，避免添加重复节点
                                treeObj.reAsyncChildNodes(node, "refresh");
                            }
                        }
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
                        t809ForwardManagement.getGroupChild(node.children, assign);
                    }
                }
            }
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                nodes = zTree
                    .getCheckedNodes(true),
                v = "",
                vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type === "vehicle") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].id + ",";
                }
            }
            vehicleId = vid;
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
                size = obj.size;
            } else {
                data = obj
            }
            for (var i = 0; i < data.length; i++) {
                data[i].open = true;
            }
            return data;
        },
        ajaxSearchDataFilter: function (treeId, parentNode, responseData) {
            var data = t809ForwardManagement.ajaxDataFilter(treeId, parentNode, responseData);
            data = filterQueryResult(data, []);
            return data;
        },
        //批量删除
        delModelClick: function () {
            //判断是否至少选择一项
            var checkedMonitors = $("input[name='subChk']:checked");
            var chechedNum = checkedMonitors.length;
            if (chechedNum === 0) {
                layer.msg(selectItem, {
                    move: false
                });
                return

            }
            var checkedList = [];
            checkedMonitors.each(function () {
                checkedList.push($(this).val());
            });
            var param = {
                'id': checkedList.toString()
            }
            var platformListUrl = "/clbs/m/connectionparamsConfig/delete";
            layer.confirm("删掉就没啦，请谨慎下手！", {
                btn: ["确定", "取消"]
            }, function () {
                json_ajax("POST", platformListUrl, "json", true, param, t809ForwardManagement.deleteCall);
            });
        },
        deleteCall: function (data) {
            if (data.success) {
                layer.msg("删除成功！", {
                    move: false
                });
                myTable.refresh();
                t809ForwardManagement.zTreeinit();
            } else {
                layer.closeAll();
                myTable.refresh();
            }
        },
        deleteOne: function (id) {
            var param = {
                id: id
            }
            var platformListUrl = "/clbs/m/connectionparamsConfig/delete";
            layer.confirm("删掉就没啦，请谨慎下手！", {
                btn: ["确定", "取消"]
            }, function () {
                json_ajax("POST", platformListUrl, "json", true, param, t809ForwardManagement.deleteCall);
            });
        },
        //加载完成后执行
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            myTable.requestData();
        },
        //全选
        cleckAll: function () {
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
                    t809ForwardManagement.zTreeinit();
                } else {
                    t809ForwardManagement.searchVehicleTree(param);
                }
            }, 500);
        },
        //车辆树模糊查询方法
        searchVehicleTree: function (param) {
            var setQueryChar = {
                async: {
                    // url: "/clbs/m/personalized/ico/vehicleTreeFuzzy",
                    url: "/clbs/a/search/reportFuzzySearch",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    sync: false,
                    // otherParam: {
                    //     "type": "multiple",
                    //     "queryParam": param,
                    //     "webType": 3
                    // },
                    otherParam: {"type": $('#queryType').val(), "queryParam": param, "queryType": "vehicle"},
                    dataFilter: t809ForwardManagement.ajaxSearchDataFilter
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
                    beforeClick: t809ForwardManagement.beforeClickVehicle,
                    onAsyncSuccess: t809ForwardManagement.fuzzyZTreeOnAsyncSuccess,
                    onCheck: t809ForwardManagement.fuzzyOnCheckVehicle,
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
            t809ForwardManagement.getCheckedNodes(); // 记录勾选的节点
        },
        keyup_submit: function (e) {
            if (e.keyCode == 13) {
                return false;
            }
        }
    }
    $(function () {
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                t809ForwardManagement.zTreeinit();
            }
        });
        t809ForwardManagement.init();
        // t809ForwardManagement.validates()
        $("#checkAll").bind("click", t809ForwardManagement.cleckAll);
        subChk.bind("click", t809ForwardManagement.subChkClick);
        groupSelect.bind("click", showMenuContent);
        $("#del_model").on("click", t809ForwardManagement.delModelClick);
        $("#refreshTable").on("click", t809ForwardManagement.refreshTable);
        groupSelect.bind('input oninput', t809ForwardManagement.searchVehicleSearch);
        $('#simpleQueryParam').on('keydown', t809ForwardManagement.keyup_submit);
    })
}(window, $))