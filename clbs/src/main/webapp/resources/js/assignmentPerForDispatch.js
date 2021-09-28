(function ($, window) {
    var flag = true;
    var treeData;
    var userVehicleNodes = [];      //存放选择的分组   用于提交
    var userVehicleNodesOld = [];   //存放初始数据  用于判断是否改变分组
    var assignmentPer = {
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
                    beforeClick: assignmentPer.beforeClickVehicleGroup,
                    onCheck: assignmentPer.onCheckVehicleGroup,
                }
            }
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
                ;
                flag = false;
            } else {
                for (var i = 0; i < nodes.length; i++) {
                    zTree.checkNode(nodes[i], false, true);
                }
                ;
                flag = true;
            }
            ;
            assignmentPer.loopcheck(treeNode);
        },
        doSubmit: function () {
            var userVehicleTree = $.fn.zTree.getZTreeObj("userGroupDemo");
            var userVehicleNode = userVehicleNodes;
            var arr = [];
            if (userVehicleNode != null && userVehicleNode.length > 0) {
                for (var i = 0; i < userVehicleNode.length; i++) {
                    if (userVehicleNode[i].type == "user") {
                        arr.push(userVehicleNode[i].uuid);
                    }
                }
                if (arr != null && arr.length > 0) {
                    $("#userVehicle").val(arr.join(";"));
                }
            }
            $("#assignmentPerForm").submit();
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
                        getParentShowNodes_ztree(treeId, newHighlightNodes[i], showNodes);
                        if (newHighlightNodes[i].type == 'group') {
                            assignmentPer.loopTree(newHighlightNodes[i], treeId, showNodes);
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
        loopTree: function (node, treeId, showNodes) {
            var childrens = node.children;
            if (childrens && childrens.length > 0) {
                for (var j = 0; j < childrens.length; j++) {
                    getParentShowNodes_ztree(treeId, childrens[j], showNodes);
                    if (childrens[j].type == 'group') {
                        assignmentPer.loopTree(childrens[j], treeId, showNodes);
                    }
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
                    assignmentPer.loopcheck(childrens[i]);
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
        assignmentPer.init();
        $("#allocationPeople").inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'allocationPeople') {
                assignmentPer.fuzzyquery('userGroupDemo', 'allocationPeople', $("#queryType").val());
            }
        });
        myTable.edit('commonSmWin', 'assignmentPerForm', null, null);
        var treeObj = $.fn.zTree.getZTreeObj("userGroupDemo");
        treeObj.expandAll(true);
        userVehicleNodesOld = treeObj.getCheckedNodes();//初始化页面时用户选择的组织（如果为空，说明是新增的用户.不为空,就跟提交时过去的比较，判断用户有没有勾选组织）
        userVehicleNodes = treeObj.getCheckedNodes();
        $("#doSubmitPer").on("click", assignmentPer.doSubmit);
        $("#allocationPeople").on("input oninput", function () {
            assignmentPer.fuzzyquery('userGroupDemo', 'allocationPeople', $("#queryType").val());
        });
        $("#queryType").on("change", function () {
            assignmentPer.fuzzyquery('userGroupDemo', 'allocationPeople', $(this).val());
        });
    })
})($, window);