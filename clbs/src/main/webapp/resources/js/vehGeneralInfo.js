(function (window, $) {
    //车辆列表
    var vehicleList = [];
    //车辆id列表
    var vehicleId = 'hahahhaha';
    var checkFlag = false; //判断组织节点是否是勾选操作
    var size;//当前权限监控对象数量
    var zTreeIdJson = {};
    var ifAllCheck = false; // 进入页面不勾选组织树(@version4.0.0 2019/6/18 与需求确认,因该报表的特殊性,进入报表默认不勾选树结构)
    var crrentSubV = []; //模糊查询？
    var bflag = true; //模糊查询
    var isSearch = true;
    var searchFlag = false;// 列表查询标识

    vehGeneralInfo = {
        init: function () {
            vehGeneralInfo.treeInit();
            vehGeneralInfo.tableInit();
            vehGeneralInfo.getVehicleTypes();
            $("[data-toggle='tooltip']").tooltip();
        },
        // 获取车辆类型
        getVehicleTypes: function () {
            var url = '/clbs/m/reportManagement/vehGeneralInfo/getVehTypes';
            json_ajax("POST", url, "json", true, null, function (data) {
                var result = data;
                var html = '<option value="">-- 请选择车辆类型 --</option>';
                for (var i = 0, len = result.length; i < len; i++) {
                    html += '<option value="' + result[i].id + '">' + result[i].type + '</option>'
                }
                $('#vehicleType').html(html);
            });
        },
        inquireClick: function () {
            vehGeneralInfo.getCheckedNodes();
            var simCard = $('#simCard').val();
            var deviceNumber = $('#deviceNumber').val();
            var vehType = $('#vehicleType').val();
            var professional = $('#professional').val();
            // if (vehicleId == '' && simCard == '' && deviceNumber == '' && vehType == '' && professional == '') {
            //     layer.msg('请至少选择一项');
            //     return;
            // }
            vehGeneralInfo.getCheckedNodes();
            if (!vehGeneralInfo.validates()) return;
            searchFlag = true;
            vehGeneralInfo.tableInit();
            $("#exportData").removeAttr("disabled");
        },
        //创建表格
        tableInit: function () {
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr th:gt(0)");
            // menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>";
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $("#Ul-menu-text").html(menu_text);

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
            }, {
                "data": "brand",
                "class": "text-center"
            }, {
                "data": "assignmentName",
                "class": "text-center"
            }, {
                "data": "groupName",
                "class": "text-center"
            }, {
                "data": "groupArea",
                "class": "text-center"
            }, {
                "data": "businessScope",
                "class": "text-center"
            }, {
                "data": "license",
                "class": "text-center"
            }, {
                "data": "issuingOrgan",
                "class": "text-center"
            }, {
                "data": "principal",
                "class": "text-center"
            }, {
                "data": "phone",
                "class": "text-center"
            }, {
                "data": "orgAddress",
                "class": "text-center"
            }, {
                "data": "deviceNumber",
                "class": "text-center"
            }, {
                "data": "simCard",
                "class": "text-center"
            }, {
                "data": "professionalNames",
                "class": "text-center"
            }];
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.vehicleIds = vehicleId;
                d.simpleQueryParam = $('#simpleQueryParam').val();
                d.simCard = $('#simCard').val();
                d.deviceNumber = $('#deviceNumber').val();
                d.vehType = $('#vehicleType').val();
                d.professional = $('#professional').val();
            };
            //表格setting
            var setting = {
                listUrl: '/clbs/m/reportManagement/vehGeneralInfo/list',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                getAddress: false,//是否逆地理编码
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        //刷新列表
        refreshTable: function () {
            $("#simpleQueryParam").val("");
            myTable.requestData();
        },
        validates: function () {
            return $("#fuellist").validate({
                rules: {
                    groupSelect: {
                        zTreeChecked: "treeDemo"
                    },
                    deviceNumber: {
                        checkDeviceNumber: '#deviceNumber'
                    },
                    simCard: {
                        isNewTel: true
                    }
                },
                messages: {
                    groupSelect: {
                        zTreeChecked: vehicleSelectBrand
                    },
                    deviceNumber: {
                        checkDeviceNumber: '请输入字母、数字，长度7-20位'
                    },
                }
            }).form();
        },

        /**
         * 监控对象树相关方法
         * */
        treeInit: function () {
            var setting = {
                async: {
                    url: vehGeneralInfo.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: vehGeneralInfo.ajaxDataFilter
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
                    beforeClick: vehGeneralInfo.beforeClickVehicle,
                    onCheck: vehGeneralInfo.onCheckVehicle,
                    beforeCheck: vehGeneralInfo.zTreeBeforeCheck,
                    onExpand: vehGeneralInfo.zTreeOnExpand,
                    onAsyncSuccess: vehGeneralInfo.zTreeOnAsyncSuccess,
                    onNodeCreated: vehGeneralInfo.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            vehGeneralInfo.getCharSelect(treeObj);
            bflag = false;
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
                        if (nodes[i].type == "vehicle") {
                            //查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                            var nd = zTree.getNodeByParam("tId", nodes[i].tId, treeNode);
                            if (nd == null && $.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength += ns.length;
                } else if (treeNode.type == "vehicle") { //若勾选的为监控对象
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;
                    //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    //节点id
                    var nodeId;
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type == "vehicle") {
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
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                isSearch = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    vehGeneralInfo.getCheckedNodes();
                    vehGeneralInfo.validates();
                }, 600);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            vehGeneralInfo.getCharSelect(zTree);
            vehGeneralInfo.getCheckedNodes();
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
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
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
                $("#groupSelect").val(allNodes[0].name);
            } else {
                $("#groupSelect").val("");
            }
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                .getCheckedNodes(true), v = "", vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "people" || nodes[i].type == "vehicle" || nodes[i].type == "thing") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].id + ",";
                }
            }
            vehicleList = v;
            vehicleId = vid;
        },
        //模糊查询树
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选
            crrentSubV = [];
            $('#charSelect').val('');
            if (param == null || param == undefined || param == '') {
                bflag = true;
                vehGeneralInfo.treeInit()
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType').val(), "queryParam": param, "queryType": "vehicle"},
                        dataFilter: vehGeneralInfo.ajaxQueryDataFilter
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
                        beforeClick: vehGeneralInfo.beforeClickVehicle,
                        onCheck: vehGeneralInfo.onCheckVehicle,
                        onExpand: vehGeneralInfo.zTreeOnExpand,
                        onNodeCreated: vehGeneralInfo.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },
        getTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var rData;
            if (!responseData.msg) {
                rData = responseData;
            } else {
                rData = responseData.msg;
            }
            var obj = JSON.parse(ungzip(rData));
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
            return data;
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData))
            var nodesArr;
            if ($('#queryType').val() == "vehicle") {
                nodesArr = filterQueryResult(responseData, crrentSubV);
            } else {
                nodesArr = responseData;
            }
            for (var i = 0; i < nodesArr.length; i++) {
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        exportDataFun: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            window.location.href = "/clbs/m/reportManagement/vehGeneralInfo/exportVehicleGeneralInfo";
        }
    }
    $(function () {
        $('input').inputClear();
        //初始化页面
        vehGeneralInfo.init();

        $("#groupSelect").bind("click", showMenuContent); //组织下拉显示
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                vehGeneralInfo.searchVehicleTree(param);
            }
        });
        $('#search_button').on('click', function () {
            if (searchFlag) myTable.requestData();
        });
        /**
         * 监控对象树模糊查询
         */
        var inputChange;
        $("#groupSelect").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                isSearch = true;
            }
            inputChange = setTimeout(function () {
                if (isSearch) {
                    var param = $("#groupSelect").val();
                    vehGeneralInfo.searchVehicleTree(param);
                }
                isSearch = true;
            }, 500);
        });
        $("#refreshTable").bind("click", vehGeneralInfo.refreshTable);
        $('#queryType').on('change', function () {
            $("#groupSelect").val('');
            vehGeneralInfo.searchVehicleTree('');
        });

        /**
         * 导出数据
         */
        $('#exportData').on('click', vehGeneralInfo.exportDataFun);
    })
}(window, $))