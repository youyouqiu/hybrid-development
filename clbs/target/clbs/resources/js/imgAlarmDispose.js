(function (window, $) {
    //单选
    var subChk = $("input[name='subChk']");
    var menu_text = "";
    var table = $("#dataTable tr th:gt(1)");
    //车辆列表
    var vehicleList = [];
    //车辆id列表
    var vehicleId = [];
    var checkFlag;
    var forwardVehicleInputChange;
    var selectFlag = false;
    imgAlarmDispose = {
        init: function () {
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text").html(menu_text);
            imgAlarmDispose.zTreeinit();
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
                    render: function (data, type, row, meta) {
                        var result = '';
                        result += '<input  type="checkbox" name="subChk"  value="' + row.monitorId + '" />';
                        return result;
                    }
                },
                {
                    "data": null,
                    "class": "text-center", //最后一列，操作按钮
                    render: function (data, type, row, meta) {
                        var result = '';
                        //删除按钮
                        result += '<button type="button" onclick="imgAlarmDispose.deleteOne(\'' + row.monitorId + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>解除</button>';
                        return result;
                    }
                }, {
                    "data": "monitorName",
                    "class": "text-center"
                }, {
                    "data": "orgName", // 所属企业
                    "class": "text-center"
                }, {
                    "data": "groupName",
                    "class": "text-center"
                }, {
                    "data": "vehiclePurposeCategory",
                    "class": "text-center",
                }, {
                    "data": "deviceNumber",
                    "class": "text-center",
                }, {
                    "data": "simcardNumber",
                    "class": "text-center",
                }, {
                    "data": "deviceModelNumber",
                    "class": "text-center",
                }
            ];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/adas/risk/pic-postprocess/list',
                deleteUrl: '/clbs/adas/risk/pic-postprocess/remove',
                deletemoreUrl: '/clbs/adas/risk/pic-postprocess/batchRemove',
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
            var platformListUrl = "/clbs/m/forwardplatform/mf/platformListUrl";
            json_ajax("POST", platformListUrl, "json", true, parameterList, imgAlarmDispose.getPlatformList);

        },
        // 显示错误提示信息
        showErrorMsg: function (msg, inputId) {
            if ($("#error_label").is(":hidden")) {
                $("#error_label").text(msg);
                $("#error_label").insertAfter($("#" + inputId));
                $("#error_label").show();
            } else {
                $("#error_label").is(":hidden");
            }
        },
        zTreeinit: function () {
            //车辆树
            var setting = {
                async: {
                    url: imgAlarmDispose.getParkingReportTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: imgAlarmDispose.ajaxDataFilter
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
                    beforeClick: imgAlarmDispose.beforeClickVehicle,
                    onAsyncSuccess: imgAlarmDispose.zTreeOnAsyncSuccess,
                    beforeCheck: imgAlarmDispose.zTreeBeforeCheck,
                    onCheck: imgAlarmDispose.onCheckVehicle,
                    onExpand: imgAlarmDispose.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        getParkingReportTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree?deviceType=1";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle&deviceType=1";
            }
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            // if(size <= 5000){
            //     treeObj.checkAllNodes(true);
            // }
            imgAlarmDispose.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
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
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
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
                    layer.msg(maxSelectItem);
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


        addConfigCheck: function () {
            imgAlarmDispose.getCheckedNodes();
            if (vehicleId.length <= 0) {
                imgAlarmDispose.showErrorMsg("请选择车辆", "groupSelect");
                return;
            } else {
                imgAlarmDispose.hideErrorMsg();
            }
            var platformListUrl = "/clbs/adas/risk/pic-postprocess/add";
            json_ajax("POST", platformListUrl, "json", true, {monitorIds: vehicleId}, imgAlarmDispose.addConfigCallBack);
        },
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#error_label").hide();
        },
        addConfigCallBack: function (data) {
            if (data.success) {
                $("#thirdPlatformId").val("");
                $("#vehicleIds").val("");
                $("#groupSelect").val("");
                $("#ipAddress").val("");
                $("#groupSelect").val("");
                myTable.refresh();
                imgAlarmDispose.zTreeinit();
                $("#ipAddress").siblings('i').remove();
                layer.msg('添加成功');
            } else if (data.msg) {
                layer.msg(data.msg);
            }
        },
        addressOnChange: function () {
            $("#thirdPlatformId").val("");
        },
        getPlatformList: function (data) {
            var datas = data.obj.platformList;
            var dataList = {value: []}, i = datas.length;
            while (i--) {
                dataList.value.push({
                    name: datas[i].description,
                    id: datas[i].id
                });
            }
            $("#ipAddress").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: dataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {

                imgAlarmDispose.hideErrorMsg();
                $("#thirdPlatformId").val(keyword.id);
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
            }
            //imgAlarmDispose.getCheckedNodes();
            imgAlarmDispose.getCharSelect(zTree);
            imgAlarmDispose.hideErrorMsg();
        },
        checkedVehicleId: {},
        zTreeOnExpand: function (event, treeId, treeNode) {
            //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if (treeNode.type == "group" && !checkFlag) {
                return;
            }
            //初始化勾选操作判断表示
            checkFlag = false;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (treeNode.type == "group") {
                var assign = []; // 当前组织及下级组织的所有分组
                imgAlarmDispose.getGroupChild(treeNode, assign);
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
        },
        getGroupChild: function (node, assign) { // 递归获取组织及下级组织的分组节点
            var nodes = node.children;
            if (nodes != null && nodes != undefined && nodes.length > 0) {
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.type == "assignment") {
                        assign.push(node);
                    } else if (node.type == "group" && node.children != undefined) {
                        imgAlarmDispose.getGroupChild(node.children, assign);
                    }
                }
            }
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                .getCheckedNodes(true), v = "", vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].id + ",";
                }
            }
            vehicleList = v;
            vehicleId = vid;
        },
        getCharSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $("#groupSelect").val(allNodes[0].name);
            } else {
                $("#groupSelect").val("");
            }
        },


        unique: function (arr) {
            var result = [], hash = {};
            for (var i = 0, elem; (elem = arr[i]) != null; i++) {
                if (!hash[elem]) {
                    result.push(elem);
                    hash[elem] = true;
                }
            }
            return result;
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            var obj;
            if (responseData.msg) {
                obj = JSON.parse(ungzip(responseData.msg));
            } else {
                obj = JSON.parse(ungzip(responseData));
            }
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
            // nodesArr = filterQueryResult(data, []);
            return data;
        },
        ajaxSearchDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            var obj;
            if (responseData.msg) {
                obj = JSON.parse(ungzip(responseData.msg));
            } else {
                obj = JSON.parse(ungzip(responseData));
            }
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
            data = filterQueryResult(data, []);
            return data;
        },
        //批量删除
        delModelClick: function () {
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChk']:checked").length;
            if (chechedNum == 0) {
                layer.msg(selectItem, {move: false});
                return

            }
            var checkedList = [];
            $("input[name='subChk']:checked").each(function () {
                checkedList.push($(this).val());
            });
            var param = {'monitorIds': checkedList.toString()}
            console.log('param', param)
            var platformListUrl = "/clbs/adas/risk/pic-postprocess/batchRemove";
            layer.confirm("删掉就没啦，请谨慎下手！", {btn: ["确定", "取消"]}, function () {
                json_ajax("POST", platformListUrl, "json", true, param, imgAlarmDispose.deleteCall);
            });
        },
        deleteCall: function (data) {
            if (data.success) {
                layer.msg("删除成功！", {move: false});
                myTable.refresh();
                imgAlarmDispose.zTreeinit();
            } else {
                layer.closeAll();
                myTable.refresh();
            }
        },
        deleteOne: function (id) {
            var param = {monitorId: id}
            var platformListUrl = "/clbs/adas/risk/pic-postprocess/remove";
            layer.confirm("删掉就没啦，请谨慎下手！", {btn: ["确定", "取消"]}, function () {
                json_ajax("POST", platformListUrl, "json", true, param, imgAlarmDispose.deleteCall);
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
            $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
        },
        //车辆模糊查询
        searchVehicleSearch: function () {
            //search_ztree('treeDemoFence', 'searchVehicle','vehicle');
            if (forwardVehicleInputChange !== undefined) {
                clearTimeout(forwardVehicleInputChange);
            }
            forwardVehicleInputChange = setTimeout(function () {
                var param = $("#groupSelect").val();
                if (param == '') {
                    imgAlarmDispose.zTreeinit();
                } else {
                    imgAlarmDispose.searchVehicleTree(param);
                }
            }, 500);
        },
        //车辆树模糊查询方法
        searchVehicleTree: function (param) {
            var setQueryChar = {
                async: {
                    url: "/clbs/m/personalized/ico/vehicleTreeFuzzy?deviceType=1",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    sync: false,
                    otherParam: {"type": "multiple", "queryParam": param},
                    dataFilter: imgAlarmDispose.ajaxSearchDataFilter
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
                    beforeClick: imgAlarmDispose.beforeClickVehicle,
                    onAsyncSuccess: imgAlarmDispose.fuzzyZTreeOnAsyncSuccess,
                    onCheck: imgAlarmDispose.fuzzyOnCheckVehicle,
                }
            };
            $.fn.zTree.init($("#treeDemo"), setQueryChar, null);
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
                    if (checkedNodes[i].type == "people" || checkedNodes[i].type == "vehicle") {
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
            imgAlarmDispose.getCheckedNodes(); // 记录勾选的节点
        },
    }
    $(function () {
        $('input').inputClear();
        imgAlarmDispose.init();
        $("#checkAll").bind("click", imgAlarmDispose.cleckAll);
        subChk.bind("click", imgAlarmDispose.subChkClick);
        $("#groupSelect").bind("click", showMenuContent);
        $("#del_model").on("click", imgAlarmDispose.delModelClick);
        $("#refreshTable").on("click", imgAlarmDispose.refreshTable);
        $("#groupSelect").bind('input oninput', imgAlarmDispose.searchVehicleSearch);

        $('#simpleQueryParam').on('keyup', function () {
            var value = $(this).val();
            var newVal = value.replace(/[`\s\^\*;'"\\|,/<>\?]/g, '');
            $(this).val(newVal);
        })
    })
})(window, $)