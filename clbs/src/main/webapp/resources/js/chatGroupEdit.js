(function (window, $) {
    var zTreeIdJson = {};
    var checkFlag = false; // 判断组织节点是否是勾选操作
    var allVid;
    var vnameList=[];
    editChatGroup = {
        //提交
        doSubmits: function () {
            editChatGroup.hideErrorMsg();
            $("#input_user_ids").val(allVid);
            if (!editChatGroup.validates()) {
                return false;
            }
            if(allVid==undefined||allVid.toString().length==0){
                editChatGroup.showErrorMsg(chatGroupUserNull, "groupSelect3");
                return false;
            }
            addHashCode1($("#editForm"));
            $("#editForm").ajaxSubmit(function (data) {
                data = JSON.parse(data);
                if (data.success) {
                    $("#commonSmWin").modal("hide");
                    //关闭弹窗
                    myTable.refresh();
                    return false;
                }
                if (data.msg.toString().indexOf("名称") > -1) {
                    editChatGroup.showErrorMsg(data.msg, "groupName");
                } else if (data.msg.toString().indexOf("系统错误") > -1) {
                    layer.msg(data.msg, {move: false});
                }
                return false;
            });
        },
        init:function () {
            var setting = {
                async: {
                    url: '/clbs/c/user/chatUserTree?type=multiple',
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"groupId": $("#input_groupId").val()},
                    dataFilter: editChatGroup.ajaxDataFilter
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
                    beforeClick: editChatGroup.beforeClickVehicle,
                    onCheck: editChatGroup.onCheckVehicle,
                    onExpand: editChatGroup.zTreeOnExpand,
                    onNodeCreated: editChatGroup.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo3"), setting, null);
        },
        onCheckVehicle: function(e, treeId, treeNode){
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            // 若为取消勾选则不展开节点
            if(treeNode.checked){
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
            }
            editChatGroup.getCheckedNodes();
            var userid = editChatGroup.getCharSelect(zTree);
            console.log("userid:"+userid);
        },
        // 获取到选择的节点
        getCheckedNodes : function(){
            var zTree = $.fn.zTree.getZTreeObj("treeDemo3"), nodes = zTree.getCheckedNodes(true), v = "",vid="";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type !== "user") {
                    continue;
                }
                v += nodes[i].name + ",";
                vid+=nodes[i].id+",";
            }
            allVid=vid;
            vnameList=v;
        },
        zTreeOnNodeCreated: function (event, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            var id = treeNode.id.toString();
            var list = [];
            if (zTreeIdJson[id] == undefined || zTreeIdJson[id] == null) {
                list = [treeNode.tId];
                zTreeIdJson[id] = list;
            } else {
                zTreeIdJson[id].push(treeNode.tId)
            }
            editChatGroup.getCheckedNodes();
        },
        // 组织树预处理加载函数
        ajaxDataFilter: function(treeId, parentNode, responseData){
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            if (responseData.msg) {
                var obj = JSON.parse(ungzip(responseData.msg)); // 解压
                var data;
                if (obj.tree != null && obj.tree != undefined) {
                    data = obj.tree;
                    size = obj.size;
                }else{
                    data = obj
                }
                var groupName="";
                for (var i = 0; i < data.length; i++) {
                    data[i].open = true;
                    if (data[i].count == null || data[i].count == "null" || data[i].count == "") {
                        delete data[i].count;
                    }
                    if(data[i].iconSkin=='groupSkin'&&groupName==""){
                        groupName=data[i].name;
                    }
                }
            }
            $("#groupSelect3").val(groupName);
            return data;
        },
        beforeClickVehicle: function(treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeBeforeCheck : function(treeId, treeNode){
            var flag = true;
            if (!treeNode.checked) {
                if(treeNode.type == "group" || treeNode.type == "assignment"){ // 若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj(treeId), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;
                    json_ajax("post", "/clbs/c/user/chatUserTree",
                        "json", false, {"groupId": treeNode.id}, function (data) {
                            data = JSON.parse(data);
                            nodesLength += data.length;
                        });

                    // 存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    // 节点id
                    var nodeId;
                    for (var i=0;i<nodes.length;i++) {
                        nodeId = nodes[i].id;
                        if(nodes[i].type == "people" || nodes[i].type == "vehicle"){
                            // 查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                            var nd = zTree.getNodeByParam("tId",nodes[i].tId,treeNode);
                            if(nd == null && $.inArray(nodeId,ns) == -1){
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength += ns.length;
                }
                if(nodesLength > 5000){
                    layer.msg(maxSelectItem);
                    flag = false;
                }
            }
            if(flag){
                // 若组织节点已被勾选，则是勾选操作，改变勾选操作标识
                if(treeNode.type == "group" && !treeNode.checked){
                    checkFlag = true;
                }
            }
            return flag;
        },
        zTreeOnExpand : function (event, treeId, treeNode) {
            // 判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if(treeNode.type == "group" && !checkFlag){
                return;
            }
            // 初始化勾选操作判断表示
            checkFlag = false;
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            if(treeId != 'treeDemo3'){
                return;
            }

            var url = "/clbs/c/user/chatUserTree";
            json_ajax("post", url, "json", false, {"groupId": treeNode.id,"isChecked":treeNode.checked,"monitorType":"vehicle"}, function (data) {
                var result = data.obj;
                if (result != null && result != undefined){
                    $.each(result, function(i) {
                        var pid = i; // 获取键值
                        var chNodes = result[i] // 获取对应的value
                        var parentTid = zTreeIdJson[pid][0];
                        var parentNode = treeObj.getNodeByTId(parentTid);
                        if (parentNode.children === undefined) {
                            treeObj.addNodes(parentNode, []);
                        }
                    });
                }
            })
        },
        getCharSelect: function (treeObj) {
            var treeId = treeObj.setting.treeId;
            var groupSelectId = '#groupSelect3';
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $(groupSelectId).val(allNodes[0].name);
                if(treeId == 'treeDemo3'){
                    $(groupSelectId).data('groupids', nodes.filter(function(ele){
                        return ele.type == 'user';
                    }).map(function(ele){
                        if(treeId == 'treeDemo3'){
                            if(ele.type == 'user'){
                                return ele.id;
                            }
                        }else{
                            return ele.uuid;
                        }

                    }).join(','))
                }else{
                    $(groupSelectId).data('groupids', nodes.map(function(ele){
                        return ele.uuid;
                    }).join(','))
                }

            } else {
                $(groupSelectId).val("");
            }
        },
        validates: function () {
            return $("#editForm").validate({
                rules: {
                    groupName: {
                        required: true
                    }
                },
                messages: {
                    groupName: {
                        required: chatGroupNameNull
                    }
                }
            }).form();
        },
        showErrorMsg: function (msg, inputId) {
            if ($("#error_label_add").is(":hidden")) {
                $("#error_label_add").text(msg);
                $("#error_label_add").insertAfter($("#" + inputId));
                $("#error_label_add").show();
            } else {
                $("#error_label_add").is(":hidden");
            }
        },
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#error_label_add").hide();
        },
    }
    $(function () {
        $('input').inputClear();
        editChatGroup.init();
        $("#groupSelect3").bind("click",showMenuContent);			// 组织下拉显示
        $("#doSubmits").bind("click", editChatGroup.doSubmits);
    })
})(window, $)