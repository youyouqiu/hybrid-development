(function($,window){
    var userVehicleNodes=[];      //存放选择的分组   用于提交
    var userVehicleNodesOld=[];   //存放初始数据  用于判断是否改变分组
    var submitFlag = true;

    var userVehiclePer = {
        init: function(){
            // 车组权限 
            var setVehicleGroup = {
                async : {
                    type : "post",
                    enable : true,
                    autoParam : [ "id" ],
                    dataType : "json",
                },
                check : {
                    enable : true,
                    chkStyle : "checkbox",
                    chkboxType : {
                        "Y" : "s",
                        "N" : "s"
                    },
                    radioType : "all"
                },
                view : {
                    dblClickExpand : false,
                    fontCss : userVehiclePer.setFontCss
                },
                data : {
                    simpleData : {
                        enable : true
                    }
                },
                callback : {
                    beforeClick : userVehiclePer.beforeClickVehicleGroup,
                    onCheck : userVehiclePer.onCheckVehicleGroup,
                }
            }
            var treeData = $("#vehicleTreeData").attr("value");
            $.fn.zTree.init($("#vehicleGroupDemo"), setVehicleGroup, JSON.parse(treeData));
            // 去重
            Array.prototype.unique1 = function() {
                var res = [ this[0] ];
                for (var i = 1; i < this.length; i++) {
                    var repeat = false;
                    for (var j = 0; j < res.length; j++) {
                        if (this[i] == res[j]) {
                            repeat = true;
                            break;
                        }
                    }
                    if (!repeat) {
                        res.push(this[i]);
                    }
                }
                return res;
            }
            var wHeight = $(window).height();
            $("#treeAuto").css({"max-height":(wHeight-284)+"px","overflow":"auto"});
        },
        beforeClickVehicleGroup: function(treeId, treeNode){
            var zTree = $.fn.zTree.getZTreeObj("vehicleGroupDemo");
            zTree.checkNode(treeNode, !treeNode.checked, null, true);
            return false;
        },
        onCheckVehicleGroup: function(e, treeId, treeNode){
            var zTree = $.fn.zTree.getZTreeObj("vehicleGroupDemo");
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
            };
            userVehiclePer.loopcheck(treeNode);
        },
        setFontCss: function(treeId, treeNode){
            return treeNode.vehicleType == "out" ? {
                color : "red"
            } : {};
        },
        doSubmit: function(){
            var userVehicleTree = $.fn.zTree.getZTreeObj("vehicleGroupDemo");
            var userVehicleNode = userVehicleNodes;
            if(userVehicleNode.length == userVehicleNodesOld.length && userVehiclePer.equalsArray(userVehicleNode,userVehicleNodesOld)){//如果初始化窗口时获取的节点信息与提交时获取的节点信息相同,说明用户并没有做新的操作,直接关闭窗口
                $("#commonSmWin").modal("hide");//关闭弹窗
                return;
            }else{
                var arr = [];
                if (userVehicleNode != null && userVehicleNode.length > 0) {
                    for (var i = 0; i < userVehicleNode.length; i++) {
                        if (userVehicleNode[i].type == "assignment") {
                            arr.push(userVehicleNode[i].id);
                        }
                    }
                    if (arr != null && arr.length > 0) {
                        arr = arr.unique1(); // 去重
                        $("#userVehicle").val(JSON.stringify(arr));
                        // $("#vehiclePerForm").submit();
                        if (submitFlag) { // 防止重复提交
                    		submitFlag = false;
	                        $("#vehiclePerForm").ajaxSubmit(function(data) {
	                            if(typeof(data) == "object" &&
	                                Object.prototype.toString.call(data).toLowerCase() == "[object object]" && !data.length){//如果后台返回的数据是json数据,则直接过去msg
	                                layer.msg(data.msg,{move:false});
	                            }else{
	                                var dataset = $.parseJSON(data);//转为json对象
	                                layer.msg(dataset.msg,{move:false});
	                            }
	                            submitFlag = true;
	                            $("#commonSmWin").modal("hide");//关闭弹窗
	                            myTable.refresh();//刷新列表
	                        });
                        }
                    }else{
                        $("#userVehicle").val("");
                        layer.msg("请至少勾选一个分组授权查看！");
                    }
                }else{
                    //验证授权是否勾选分组
                    layer.msg("请至少勾选一个分组授权查看！");
                }
            }
        },
        equalsArray:function(arr1,arr2) {//比较两个数组的值是否相同
            arr1.sort();
            arr2.sort();
            for(var i=0;i<arr1.length;i++){
                if(typeof arr1[i] != typeof arr2[i]){
                    return false;
                }else if(arr1[i] !== arr2[i]){
                    return false;
                }
            }
            return true;
        },
        /**
         * 模拟查询
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
            allNodes = treeObj.getNodesByParam("type","assignment", null); // 所有type型nodes
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
                            userVehiclePer.loopTree(newHighlightNodes[i], treeId, showNodes, newHighlightNodes);
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
        loopTree:function (node,treeId,showNodes,newHighlightNodes) {
            var childrens = node.children;
            if (newHighlightNodes && newHighlightNodes.indexOf(node) == -1) return;
            if (childrens && childrens.length > 0) {
                for (var j = 0; j < childrens.length; j++) {
                    if (childrens[j].type == 'group') {
                        userVehiclePer.loopTree(childrens[j], treeId, showNodes, newHighlightNodes);
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
        loopcheck:function (node) {
            var childrens = node.children;
            //如果有子节点  子节点递归
            if(childrens && childrens.length>0){
                for(var i=0;i<childrens.length;i++){
                    userVehiclePer.loopcheck(childrens[i]);
                }
            }else {
                if(userVehicleNodes.indexOf(node) > -1){
                    if(!node.checked){
                        userVehicleNodes.splice(userVehicleNodes.indexOf(node),1);
                    }
                }else{
                    if(node.checked){
                        userVehicleNodes.push(node);
                    }
                }
            }
        }
    }
    $(function(){
        var flag = true;
        userVehiclePer.init();
        $("#allocationGrouping").inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'allocationGrouping') {
                userVehiclePer.fuzzyquery('vehicleGroupDemo', 'allocationGrouping');
            }
            ;
        });
        myTable.add('commonSmWin', 'vehiclePerForm', null, null);
        var treeObj = $.fn.zTree.getZTreeObj("vehicleGroupDemo");
        treeObj.expandAll(true);
        userVehicleNodesOld = treeObj.getCheckedNodes();//初始化页面时用户选择的组织（如果为空，说明是新增的用户.不为空,就跟提交时过去的比较，判断用户有没有勾选组织）
        userVehicleNodes = treeObj.getCheckedNodes();
        $("#doSubmitVehiclePer").on("click",userVehiclePer.doSubmit);
        $("#allocationGrouping").on("input oninput", function () {
            userVehiclePer.fuzzyquery('vehicleGroupDemo', 'allocationGrouping', $("#queryType").val());
        });
        $("#queryType").on("change", function () {
            userVehiclePer.fuzzyquery('vehicleGroupDemo', 'allocationGrouping', $(this).val());
        });
    })
})($,window)