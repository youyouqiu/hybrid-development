(function ($, window) {
    var flag = true;
    var treeData;
    var userVehicleNodes = [];      //存放选择的分组   用于提交
    var userVehicleNodesOld = [];   //存放初始数据  用于判断是否改变分组
    var editUser = {
        //初始化
        init: function () {
            //人权限
            var setVehicleGroup = {
                async: {
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
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
                    beforeClick: editUser.beforeClickVehicleGroup,
                    onCheck: editUser.onCheckVehicleGroup,
                }
            };
            treeData = $("#userTreeData").attr("value");
            var treeDataVal = JSON.parse(treeData);
            for (var i = 0; i < treeDataVal.length; i++) {
                if (treeDataVal[i].count == null || treeDataVal[i].count == "null" || treeDataVal[i].count == "") {
                    delete treeDataVal[i].count;
                }
            }
            $.fn.zTree.init($("#userGroupDemo"), setVehicleGroup, treeDataVal);
            var wHeight = $(window).height();
            $("#treeAuto").css({"max-height": (wHeight - 284) + "px", "overflow": "auto"});
        },
        beforeClickVehicleGroup: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("userGroupDemo");
            zTree.checkNode(treeNode, !treeNode.checked, null, true);
            return false;
        },
        onCheckVehicleGroup: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("userGroupDemo");
            var param = treeNode.id;
            var nodes = zTree.getNodesByParam("id", param, null);
            if (treeNode.checked) {
                for (var i = 0; i < nodes.length; i++) {
                    zTree.checkNode(nodes[i], true, true);
                }
                flag = false;
            } else {
                for (var i = 0; i < nodes.length; i++) {
                    zTree.checkNode(nodes[i], false, true);
                }
                flag = true;
            }
            editUser.loopcheck(treeNode);
        },
        doSubmit: function () {
            var userVehicleNode = userVehicleNodes;
            var arr = [];
            if (userVehicleNode != null && userVehicleNode.length > 0) {
                for (var i = 0; i < userVehicleNode.length; i++) {
                    if (userVehicleNode[i].type == "user") {
                        arr.push(userVehicleNode[i].uuid);
                    }
                }
                if (arr != null && arr.length > 0) {
                    $("#userIds").val(arr.join(","));
                }
            }
            var url = '/clbs/c/role/updateUserByRole.gsp';
            var value = $("#userIds").val() + '_' + $("#roleId").val();
            var resubmitToken = value.split("").reduce(function (a, b) { a = ((a << 5) - a) + b.charCodeAt(0); return a & a }, 0);

            var param = {
                'userIds': $("#userIds").val(),
                'roleId': $("#roleId").val(),
                resubmitToken
            };
            json_ajax("POST", url, "json", false, param, function (data) {
                if (data.success) {
                    $("#commonSmWin").modal("hide");
                }
                if (data.msg) {
                    layer.msg(data.msg);
                }
            });
        },
        /**
         * 模糊查询
         * @param treeId
         * @param searchConditionId
         */
        fuzzyquery: function (treeId, searchConditionId, type) {
            //<1>.搜索条件
            var searchCondition = $('#' + searchConditionId).val();
            var highlightNodes = [], newHighlightNodes = [];
            var allNodes = [];
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            searchParam = searchCondition;
            highlightNodes = treeObj.getNodesByParamFuzzy("name", searchCondition, null); // 满足搜索条件的节点
            allNodes = treeObj.getNodesByParam("type", "user", null); // 所有type型nodes
            var groupNodes = treeObj.getNodesByParam("type", "group", null);
            if (groupNodes.length > 0) {
                for (var i = 0; i < groupNodes.length; i++) {
                    allNodes.push(groupNodes[i]);
                }
            }

            for (var i = 0, len = highlightNodes.length; i < len; i++) {
                if (highlightNodes[i].type == type) {
                    newHighlightNodes.push(highlightNodes[i]);
                }
            }

            if (searchCondition != "") {
                // 需要显示是节点（包含父节点）
                var showNodes = [];
                if (newHighlightNodes != null) {
                    for (var i = 0; i < newHighlightNodes.length; i++) {
                        //组装显示节点的父节点的父节点....直到根节点，并展示
                        getParentShowNodes_ztree(treeId, newHighlightNodes[i], showNodes, newHighlightNodes);
                        if (newHighlightNodes[i].type == 'group') {
                            editUser.loopTree(newHighlightNodes[i], treeId, showNodes, newHighlightNodes);
                        }
                    }
                    treeObj.hideNodes(allNodes);
                    treeObj.showNodes(showNodes);
                    treeObj.expandAll(true);
                }
            } else {
                treeObj.showNodes(allNodes);
                treeObj.expandAll(true);
            }
        },
        /**
         * 循环获取子节点中子元素
         */
        loopTree: function (node, treeId, showNodes, newHighlightNodes) {
            var childrens = node.children;
            if (newHighlightNodes && newHighlightNodes.indexOf(node) == -1) return;
            if (childrens && childrens.length > 0) {
                for (var j = 0; j < childrens.length; j++) {
                    if (childrens[j].type == 'group') {
                        editUser.loopTree(childrens[j], treeId, showNodes, newHighlightNodes);
                    }
                    if (newHighlightNodes && newHighlightNodes.indexOf(childrens[j]) == -1 && childrens[j].type == 'group') return;
                    getParentShowNodes_ztree(treeId, childrens[j], showNodes);
                }
            }
        },

        /**
         * 勾选或取消复选框时 组装提交数组
         * @param node
         */
        loopcheck: function (node) {
            var childrens = node.children;
            //如果有子节点  子节点递归
            if (childrens && childrens.length > 0) {
                for (var i = 0; i < childrens.length; i++) {
                    editUser.loopcheck(childrens[i]);
                }
            } else {
                if (userVehicleNodes.indexOf(node) > -1) {
                    if (!node.checked) {
                        userVehicleNodes.splice(userVehicleNodes.indexOf(node), 1);
                    }
                } else {
                    if (node.checked) {
                        userVehicleNodes.push(node);
                    }
                }
            }
        }
    }
    $(function () {
        editUser.init();
        $("#allocationPeople").inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'allocationPeople') {
                editUser.fuzzyquery('userGroupDemo', 'allocationPeople', $("#queryType").val());
            }
        });
        myTable.edit('commonSmWin', 'editUserForm', null, null);
        var treeObj = $.fn.zTree.getZTreeObj("userGroupDemo");
        treeObj.expandAll(true);
        userVehicleNodesOld = treeObj.getCheckedNodes();//初始化页面时用户选择的组织（如果为空，说明是新增的用户.不为空,就跟提交时过去的比较，判断用户有没有勾选组织）
        userVehicleNodes = treeObj.getCheckedNodes();
        $("#doSubmitPer").on("click", editUser.doSubmit);
        $("#allocationPeople").on("input oninput", function () {
            editUser.fuzzyquery('userGroupDemo', 'allocationPeople', $("#queryType").val());
        });
        $("#queryType").on("change", function () {
            editUser.fuzzyquery('userGroupDemo', 'allocationPeople', $(this).val());
        });
    })
})($, window)