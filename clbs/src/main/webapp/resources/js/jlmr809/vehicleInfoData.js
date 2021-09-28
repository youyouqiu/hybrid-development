(function (window, $) {
    var vehicleList = '';// 勾选车辆name
    var vehicleId = '';// 勾选车辆id
    var checkFlag = false; //判断组织节点是否是勾选操作
    var zTreeIdJson = {};

    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var searchFlag = true;
    var exportParam = null;

    vehicleInfoData = {
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
                        "id": $('#vehiclePlatform').val(),
                        "queryType": $('#vehicleQueryType').val(),
                        'queryParam': $('#vehicleGroupSelect').val()
                    },
                    dataFilter: vehicleInfoData.ajaxDataFilter
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
                    beforeClick: vehicleInfoData.beforeClickVehicle,
                    onCheck: vehicleInfoData.onCheckVehicle,
                    onNodeCreated: vehicleInfoData.zTreeOnNodeCreated,
                    onExpand: vehicleInfoData.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#vehicleTreeDemo"), setting, null);
        },
        //模糊查询树
        searchVehicleTree: function (param) {
            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                vehicleInfoData.treeInit();
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
                            "id": $('#vehiclePlatform').val(),
                            "queryType": $('#vehicleQueryType').val(),
                            'queryParam': param
                        },
                        dataFilter: vehicleInfoData.ajaxQueryDataFilter
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
                        beforeClick: vehicleInfoData.beforeClickVehicle,
                        onCheck: vehicleInfoData.onCheckVehicle,
                        onExpand: vehicleInfoData.zTreeOnExpand,
                        onNodeCreated: vehicleInfoData.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#vehicleTreeDemo"), querySetting, null);
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            var nodesArr = [];
            if (responseData.success) {
                responseData = JSON.parse(ungzip(responseData.obj));
                if ($('#vehicleQueryType').val() == "vehicle") {
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
                $('#noInfo').html('未查询到匹配项').show();
            } else {
                $('#noInfo').html('未查询到匹配项').hide();
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
                $('#noInfo').html('未查询到匹配项').show();
            } else {
                $('#noInfo').html('未查询到匹配项').hide();
            }
            return data;
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("vehicleTreeDemo");
            if (treeNode.type === 'vehicle') {
                zTree.checkNode(treeNode, !treeNode.checked, true, true);
            }
            return false;
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("vehicleTreeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                searchFlag = false;
                setTimeout(() => {
                    vehicleInfoData.getCheckedNodes();
                    vehicleInfoData.validates();
                }, 600);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            vehicleInfoData.getCheckedNodes();
            vehicleInfoData.getCharSelect(zTree);
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
            var treeObj = $.fn.zTree.getZTreeObj("vehicleTreeDemo");
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
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $("#vehicleGroupSelect").val(nodes[0].name);
            } else {
                $("#vehicleGroupSelect").val("");
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
                        vehicleInfoData.getGroupChild(node.children, assign);
                    }
                }
            }
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("vehicleTreeDemo"), nodes = zTree
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
        vehicleInfoDataSearch: function (number) {
            vehicleInfoData.getCheckedNodes();
            if (!vehicleInfoData.validates()) {
                return;
            }
            $('#message').html('数据请求中...');
            $('#info-vehicleId').val(vehicleId);
            $('#exportData').prop('disabled', true);
            $('#vehicleFormData').ajaxSubmit(function (res) {
                var data = JSON.parse(res);
                exportParam = null;
                $('#vehicleDataTable td').html('--');
                if (data.success) {
                    var info = data.obj.info;
                    if (info) {
                        exportParam = info;
                        $('#exportData').prop('disabled', false);
                        for (key in info) {
                            $('#vinfo-' + key).html(info[key]);
                        }
                        $('#message').html(info.sendTime + '下发');
                    } else {
                        $('#message').html(data.obj.msg);
                    }
                } else {
                    if (data.obj && data.obj.msg) {
                        $('#message').html(data.obj.msg);
                    } else {
                        $('#message').html('请求失败');
                    }
                    if (data.msg) {
                        layer.msg(data.msg);
                    }
                }
            });
        },
        validates: function () {
            return $("#vehicleFormData").validate({
                rules: {
                    offOperatePlatform: {
                        required: true
                    },
                    groupSelect: {
                        zTreeChecked: "vehicleTreeDemo"
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
            var url = '/clbs/jl/vehicle/export/vehicle/info';
            exportExcelUseForm(url, exportParam);
        },
        // 高级搜索展开收起功能
        highSearch: function () {
            var icon = $(this).find('span');
            var searchBox = $(this).closest('form').find('.highsearch');
            if (icon.hasClass('fa-caret-down')) {
                icon.attr('class', 'fa fa-caret-up');
                searchBox.find('input').prop('disabled', false);
                searchBox.slideDown();
            } else {
                icon.attr('class', 'fa fa-caret-down');
                searchBox.slideUp();
                searchBox.find('input').prop('disabled', true);
            }
        }
    };
    $(function () {
        vehicleInfoData.treeInit();
        $("#vehicleInfoDataSearch").bind("click", vehicleInfoData.vehicleInfoDataSearch);
        $("#vehicleGroupSelect").bind("click", showMenuContent);
        $("#exportData").bind("click", vehicleInfoData.exportData);// 导出

        // 平台名称勾选改变,重新加载相关车辆树
        $('#vehiclePlatform').on('change', function () {
            $('#noInfo').html('加载中...');
            vehicleInfoData.treeInit();
        });

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'vehicleGroupSelect') {
                var param = $("#vehicleGroupSelect").val();
                vehicleInfoData.searchVehicleTree(param);
            }
        });

        // 高级搜索
        $('.highlever').on('click', vehicleInfoData.highSearch);

        /**
         * 监控对象树模糊查询
         */
        var inputChange;
        $("#vehicleGroupSelect").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                searchFlag = true;
            }
            inputChange = setTimeout(function () {
                if (searchFlag) {
                    var param = $("#vehicleGroupSelect").val();
                    vehicleInfoData.searchVehicleTree(param);
                }
                searchFlag = true;
            }, 500);
        });
        $('#vehicleQueryType').on('change', function () {
            var param = $("#vehicleGroupSelect").val();
            vehicleInfoData.searchVehicleTree(param);
        });
    })
}(window, $))