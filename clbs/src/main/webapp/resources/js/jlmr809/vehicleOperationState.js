(function (window, $) {
    var vehicleList = '';// 勾选车辆name
    var vehicleId = '';// 勾选车辆id
    var checkFlag = false; //判断组织节点是否是勾选操作
    var zTreeIdJson = {};

    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var searchFlag = true;
    var exportParam = null;

    var noInfo = $('#osMenuContent .noInfo');// 组织树加载信息提示语
    var messageInfo = $('#osMessage');// 请求返回提示
    var exportBtn = $('#exportOsData');// 导出按钮

    vehicleOperationState = {
        /**
         * 监控对象树相关方法
         * */
        treeInit: function () {
            //车辆树
            var setting = {
                async: {
                    url: '/clbs/jl/vehicle/809/dataInteractiveManage/tree',
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "id": $('#vehicleOsPlatform').val(),
                        "queryType": $('#vehicleOsQueryType').val(),
                        'queryParam': $('#vehicleOsGroupSelect').val()
                    },
                    dataFilter: vehicleOperationState.ajaxDataFilter
                },
                check: {
                    enable: true,
                    chkStyle: "radio",
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
                    beforeClick: vehicleOperationState.beforeClickVehicle,
                    onCheck: vehicleOperationState.onCheckVehicle,
                    onNodeCreated: vehicleOperationState.zTreeOnNodeCreated,
                    onExpand: vehicleOperationState.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#vehicleOsTreeDemo"), setting, null);
        },
        //模糊查询树
        searchVehicleTree: function (param) {
            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                vehicleOperationState.treeInit();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: '/clbs/jl/vehicle/809/dataInteractiveManage/tree',
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {
                            "id": $('#vehicleOsPlatform').val(),
                            "queryType": $('#vehicleOsQueryType').val(),
                            'queryParam': param
                        },
                        dataFilter: vehicleOperationState.ajaxQueryDataFilter
                    },
                    check: {
                        enable: true,
                        chkStyle: "radio",
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
                        beforeClick: vehicleOperationState.beforeClickVehicle,
                        onCheck: vehicleOperationState.onCheckVehicle,
                        onExpand: vehicleOperationState.zTreeOnExpand,
                        onNodeCreated: vehicleOperationState.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#vehicleOsTreeDemo"), querySetting, null);
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            var nodesArr = [];
            if (responseData.success) {
                responseData = JSON.parse(ungzip(responseData.obj));
                if ($('#vehicleOsQueryType').val() == "vehicle") {
                    nodesArr = filterQueryResult(responseData, crrentSubV);
                } else {
                    nodesArr = responseData;
                }
                for (var i = 0; i < nodesArr.length; i++) {
                    nodesArr[i].open = true;
                    if (nodesArr[i].type !== 'vehicle') {
                        nodesArr[i].nocheck = true;
                    }
                }

            }
            if (nodesArr.length === 0) {
                noInfo.html('未查询到匹配项').show();
            } else {
                noInfo.html('未查询到匹配项').hide();
            }
            return nodesArr;
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var data = [];
            if (responseData.success) {
                var data = JSON.parse(ungzip(responseData.obj));
                for (var i = 0; i < data.length; i++) {
                    data[i].open = true;
                    if (data[i].type !== 'vehicle') {
                        data[i].nocheck = true;
                    }
                }
            }
            if (data.length === 0) {
                noInfo.html('未查询到匹配项').show();
            } else {
                noInfo.html('未查询到匹配项').hide();
            }
            return data;
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            if (treeNode.type === 'vehicle') {
                zTree.checkNode(treeNode, !treeNode.checked, true, true);
            }
            return false;
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                searchFlag = false;
                setTimeout(() => {
                    vehicleOperationState.getCheckedNodes();
                    vehicleOperationState.validates();
                }, 600);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            vehicleOperationState.getCheckedNodes();
            vehicleOperationState.getCharSelect(zTree);
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
            if (treeNode.type == "group") {
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
        },
        getCharSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true);
            if (nodes.length > 0) {
                $("#vehicleOsGroupSelect").val(nodes[0].name);
            } else {
                $("#vehicleOsGroupSelect").val("");
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
                        vehicleOperationState.getGroupChild(node.children, assign);
                    }
                }
            }
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("vehicleOsTreeDemo"), nodes = zTree
                .getCheckedNodes(true), v = "", vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle") {
                    v += nodes[i].name;
                    vid += nodes[i].id;
                }
            }
            vehicleList = v;
            vehicleId = vid;
        },

        /**
         * 数据查询
         * */
        operationStateDataSearch: function () {
            vehicleOperationState.getCheckedNodes();
            if (!vehicleOperationState.validates()) {
                return;
            }
            messageInfo.html('数据请求中...');
            exportBtn.prop('disabled', true);
            var url = '/clbs/jl/vehicle/operationStatus/list';
            json_ajax("post", url, "json", false, {vehicleId: vehicleId}, function (data) {
                exportParam = null;
                $('#vehicleOsDataTable td').html('--');
                if (data.success) {
                    var res = data.obj;
                    if (res.result == '1') {
                        var info = res.info;
                        exportParam = info;
                        exportBtn.prop('disabled', false);
                        for (key in info) {
                            $('#osInfo-' + key).html(info[key]);
                        }
                        messageInfo.html(info.returnTimeStr + '下发');
                    } else {
                        messageInfo.html(res.msg);
                    }
                } else {
                    if (data.obj && data.obj.msg) {
                        messageInfo.html(data.msg);
                    }
                }
            });
        },
        validates: function () {
            return $("#vehicleOsFormData").validate({
                rules: {
                    offOperatePlatform: {
                        required: true
                    },
                    groupSelect: {
                        zTreeChecked: "vehicleOsTreeDemo"
                    }
                },
                messages: {
                    offOperatePlatform: {
                        required: '请选择平台名称'
                    },
                    groupSelect: {
                        zTreeChecked: '请选择车辆',
                    }
                }
            }).form();
        },
        exportData: function () {
            if (!exportParam) {
                return;
            }
            var url = '/clbs/jl/vehicle/operationStatus/export';
            exportExcelUseForm(url, exportParam);
        },
    };
    $(function () {
        vehicleOperationState.treeInit();
        $("#vehicleOsDataSearch").bind("click", vehicleOperationState.operationStateDataSearch);
        $("#vehicleOsGroupSelect").bind("click", showMenuContent);
        exportBtn.bind("click", vehicleOperationState.exportData);// 导出

        // 平台名称勾选改变,重新加载相关车辆树
        $('#vehicleOsPlatform').on('change', function () {
            noInfo.html('加载中...');
            vehicleOperationState.treeInit();
        });

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'vehicleOsGroupSelect') {
                var param = $("#vehicleOsGroupSelect").val();
                vehicleOperationState.searchVehicleTree(param);
            }
        });

        // 监控对象树模糊查询
        var inputChange;
        $("#vehicleOsGroupSelect").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                searchFlag = true;
            }
            inputChange = setTimeout(function () {
                if (searchFlag) {
                    var param = $("#vehicleOsGroupSelect").val();
                    vehicleOperationState.searchVehicleTree(param);
                }
                searchFlag = true;
            }, 500);
        });
        // 组织树查询类型切换
        $('#vehicleOsQueryType').on('change', function () {
            var param = $("#vehicleOsGroupSelect").val();
            vehicleOperationState.searchVehicleTree(param);
        });
    })
}(window, $))