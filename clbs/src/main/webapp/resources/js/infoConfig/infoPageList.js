(function (window, $) {
    /**
     * 信息列表
     * */
    // myTable;
    //初始化搜索类型
    treeSearchType.init();
    treeSearchType.onChange = function (datas) {
        console.log(datas)
        $("#search_condition").attr('placeholder', datas.placeholder)
        search_ztree('treeDemo', 'search_condition', datas.value);
    }
    addFlag = false;//用于新加数据列表第一行变色效果
    fastBrandsDropdown = null;
    quickBrandsDropdown = null;
    var deleteconfigId = '';
    infoinputList = {
        init: function () {
            $("[data-toggle='tooltip']").tooltip();
            infoinputList.infoTableInit();
        },
        //显示隐藏列
        showMenuText: function () {
            var menu_text = "";
            var table = $("#dataTable tr th:gt(1)");
            menu_text += "<li><label><input type=\"checkbox\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" checked=\"checked\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" checked=\"checked\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text").html(menu_text);
            //显示隐藏列
            $('.toggle-vis').on('change', function (e) {
                var column = myDataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $keepOpen.addClass("open");
            });
        },
        //创建表格
        infoTableInit: function () {
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
            }, {
                //第二列，checkbox
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return '<input type="checkbox" name="subChk" value="' + row.configId + '" /> ';
                }
            }, {//第三列，操作按钮列
                "data": null,
                "class": "text-center", //最后一列，操作按钮
                render: function (data, type, row, meta) {
                    var editUrlPath = myTable.editUrl + row.configId + ".gsp"; //修改地址
                    var detailUrlPath = myTable.detailUrl + row.configId + ".gsp";
                    var result = '';
                    //修改按钮
                    result += '<button href="' + editUrlPath + '" data-target="#commonLgWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                    //详情按钮
                    result += '<button href="' + detailUrlPath + '" data-target="#commonWin" data-toggle="modal" type="button" class=" editBtn editBtn-info"><i class="fa fa-sun-o"></i>详情</button>&ensp;';
                    //解绑按钮
                    result += '<button type="button" onclick="myTable.deleteItem(\'' + row.configId + '\')" class="brokenDeleteButton editBtn brokenDisableClick"><i class="fa fa-chain-broken" style="margin-right:4px; font-size: 14px"></i>解绑</button>&ensp;';
                    //删除按钮
                    result += '<button onclick="infoinputList.confirmDeleteInfo(\'' + row.carLicense + '\',\'' + row.simcardNumber + '\',\'' + row.deviceNumber + '\',\'' + row.configId + '\')" class="deleteButton editBtn disableClick" type="button"><i class="fa fa-trash-o"></i> 删除</button>';
                    return result;
                }
            }, {//第四列，监控对象-车
                "data": "carLicense",
                "class": "text-center"
            }, {//监控对象类型
                "data": "monitorType",
                "class": "text-center",
                render: function (data) {
                    if (data == "0") {
                        return "车";
                    } else if (data == "1") {
                        return "人";
                    } else if (data == "2") {
                        return "物";
                    } else {
                        return "";
                    }
                }
            }, {//车牌颜色
                "data": "plateColor",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (row.monitorType != '0') return '';
                    return getPlateColor(data);
                }
            }, {// 企业
                "data": "groupName",
                "class": "text-center",
            }, {// 分组
                "data": "assignmentName",
                "class": "text-center",
            }, { // 终端手机号
                "data": "simcardNumber",
                "class": "text-center"
            }, {  //真实SIM卡号
                "data": "realId",
                "class": "text-center",
                render: function (data) {
                    if (data == null) return '';
                    return data;
                }
            }, { // 终端编号
                "data": "deviceNumber",
                "class": "text-center",
            }, { // 通讯类型
                "data": "deviceType",
                "class": "text-center",
                render: function (data) {
                    return getProtocolName(data);
                }
            }, { //终端厂商
                "data": "terminalManufacturer",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == null) return '';
                    return data;
                }
            }, { //终端型号
                "data": "terminalType",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == null) return '';
                    return data;
                }
            }, { // 功能类型
                "data": "functionalType",
                "class": "text-center",
                render: function (data) {
                    if (data == "1") {
                        return "简易型车机";
                    } else if (data == "2") {
                        return "行车记录仪";
                    } else if (data == "3") {
                        return "对讲设备";
                    } else if (data == "4") {
                        return "手咪设备";
                    } else if (data == "5") {
                        return "超长待机设备";
                    } else if (data == "6") {
                        return "定位终端";
                    } else {
                        return "";
                    }
                }
            }, { // 计费日期
                "data": "billingDate",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return (data != null && data.length > 10) ? data.substr(0, 10) : "";
                }
            }, { // 到期日期
                "data": "expireDate",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return (data != null && data.length > 10) ? data.substr(0, 10) : "";
                }
            }, { // 从业人员
                "data": "professionalNames",
                "class": "text-center"
            }, { //加车时间
                "data": "createDateTime",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return (data != null && data.length > 10) ? data.substr(0, 10) : "";
                }
            }, { //加车时间
                "data": "updateDateTime",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return (data != null && data.length > 10) ? data.substr(0, 10) : "";
                }
            }];
            //全选
            $("#checkAll").click(function () {
                $("input[name='subChk']").prop("checked", this.checked);
            });
            //单选
            var subChk = $("input[name='subChk']");
            subChk.click(function () {
                $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
            });
            //批量删除
            $("#del_model").click(function () {
                //判断是否至少选择一项
                var chechedNum = $("input[name='subChk']:checked").length;
                if (chechedNum == 0) {
                    layer.msg(selectItem, {move: false});
                    return;
                }
                var checkedList = new Array();
                $("input[name='subChk']:checked").each(function () {
                    checkedList.push($(this).val());
                });
                myTable.deleteItems({'deltems': checkedList.toString()});
            });
            // 导出
            $("#exportId").click(function () {
                var url = '/clbs/m/infoconfig/infoinput/export.gsp';
                var parameter = {
                    simpleQueryParam: $('#simpleQueryParam').val(), //模糊查询
                    groupName: selectTreeId,
                    groupType: selectTreeType,
                };
                // json_ajax("POST", url, "json", true, parameter, infoinputList.exportMsg);
                exportExcelUsePost(url, parameter);
            });
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupName = selectTreeId;
                d.groupType = selectTreeType;
            };
            //表格setting
            var setting = {
                detailUrl: "/clbs/m/infoconfig/infoinput/getConfigDetails_",
                listUrl: "/clbs/m/infoconfig/infoinput/list",
                editUrl: "/clbs/m/infoconfig/infoinput/edit_",
                deleteUrl: "/clbs/m/infoconfig/infoinput/delete_",
                deletemoreUrl: "/clbs/m/infoconfig/infoinput/deletemore",
                enableUrl: "/clbs/c/user/enable_",
                disableUrl: "/clbs/c/user/disable_",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                drawCallbackFun: function () {
                    if (addFlag) {
                        var curTr = $('#dataTable tbody tr:first-child');
                        curTr.addClass('highTr');
                        setTimeout(function () {
                            curTr.removeClass('highTr');
                            addFlag = false;
                        }, 1000)
                    }
                },
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        // 刷新table列表
        refreshTable: function () {
            $('#simpleQueryParam').val("");
            selectTreeId = '';
            selectTreeType = '';
            myTable.requestData();
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.refresh();
        },

        // 列表(外设列)查看详情功能
        showLogContent: function (msg) { // 显示log详情
            var url = "/clbs//m/infoconfig/infoinput/getPeripherals";
            var parameter = {"vehicleId": msg};
            json_ajax("POST", url, "json", true, parameter, infoinputList.getPeripheralsCallback);
        },
        getPeripheralsCallback: function (data) {
            if (data != null && data != undefined && data != "") {
                if (data.success) {
                    $("#detailShow").modal("show");
                    $("#detailContent").html(data.obj.pname == "" ? "该监控对象目前还没有绑定外设哦！" : data.obj.pname);
                } else {

                }
            }
        },
        confirmDeleteInfo: function (carLicense, simcardNumber, deviceNumber, configId) {
            $('#confirmDeleteModal').modal('show');
            $("#carLicense").html(carLicense == 'null' || carLicense == '' ? '' : carLicense);
            $("#simcardNumber").html(simcardNumber == 'null' || simcardNumber == '' ? '' : simcardNumber);
            $("#real").html(deviceNumber == 'null' || deviceNumber == "" ? '' : deviceNumber);
            deleteconfigId = configId;
        },
        // exportMsg: function (data) {
        //     var exportData = data;
        //     console.log(exportData, 'exportData');
        //     // if () {
        //     //
        //     // }
        // }
    };


    /**
     * 公共方法提取
     * */
    var fastInitFlag = true;
    var processInitFlag = true;
    var sweepInitFlag = true;
    var searchFlag = true;
    publicFun = {
        //左侧树
        leftTree: function () {
            var treeSetting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/assignment/assignmentTree",
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {  // 是否可选  Organization
                        "isOrg": "1"
                    },
                    dataFilter: function (treeId, parentNode, responseData) {
                        // var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                        if (responseData) {
                            for (var i = 0; i < responseData.length; i++) {
                                responseData[i].open = true;
                            }
                        }
                        return responseData;
                    },
                },
                view: {
                    selectedMulti: false,
                    nameIsHTML: true,
                    fontCss: setFontCss_ztree
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    onClick: publicFun.leftTreeOnClick
                }
            };
            $.fn.zTree.init($("#treeDemo"), treeSetting, null);
        },
        //左侧树-点击节点
        leftTreeOnClick: function (event, treeId, treeNode) {
            console.log(treeNode)
            if (treeNode.type == "group") {
                selectTreepId = treeNode.id;
                selectTreeId = treeNode.uuid;
            } else {
                selectTreepId = treeNode.pId;
                selectTreeId = treeNode.id;
            }
            selectTreeType = treeNode.type;
            myTable.requestData();
        },
        //分组树
        groupTreeInit: function (type) {
            var setting = {
                async: {
                    url: "/clbs/m/infoconfig/infoinput/tree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    contentType: "application/json",
                    dataType: "json",
                    dataFilter: publicFun.ajaxDataFilter
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
                    dblClickExpand: false
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    beforeClick: publicFun.beforeClick,
                    onClick: publicFun.onClick,
                    onCheck: publicFun.onClick,
                    onAsyncSuccess: publicFun.onAsyncSuccess
                }
            };
            if (type == 'quick') {
                $.fn.zTree.init($("#quickTreeDemo"), setting, null);
            } else if (type == 'fast') {
                $.fn.zTree.init($("#fastTreeDemo"), setting, null);
            } else {
                $.fn.zTree.init($("#sweepTreeDemo"), setting, null);
            }
        },
        onAsyncSuccess: function (event, treeId, treeNode, msg) {
            //初始化快速录入节点选中数组
            var nodes1 = $.fn.zTree.getZTreeObj(treeId).getCheckedNodes(true);
            for (var i = 0; i < nodes1.length; i++) {
                if (nodes1[i].type == "assignment") {
                    if (treeId == 'quickTreeDemo') {
                        checkedAssginment.push(nodes1[i]);
                    } else if (treeId == 'fastTreeDemo') {
                        speedCheckedAssginment.push(nodes1[i]);
                    } else {
                        sweepCheckedAssginment.push(nodes1[i]);
                    }
                }
            }
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var flag = true;
            if (responseData) {
                for (var i = 0; i < responseData.length; i++) {
                    if (flag && responseData[i].type == 'assignment' && searchFlag) {
                        if (responseData[i].canCheck < TREE_MAX_CHILDREN_LENGTH) {
                            responseData[i].checked = true;
                            if (treeId == 'quickTreeDemo') {
                                $("#quickGroupId").val(responseData[i].name);
                                $("#quickCitySelidVal").val(responseData[i].id);
                            } else if (treeId == 'fastTreeDemo') {
                                $("#speedGroupid").val(responseData[i].name);
                                $("#speedCitySelidVal").val(responseData[i].id);
                            } else {
                                responseData[i].checked = true;
                                $("#sweepCodeGroupid").val(responseData[i].name);
                                $("#sweepCodeCitySelidVal").val(responseData[i].id);
                            }
                            flag = false;
                        }
                    }
                    responseData[i].open = true;
                }
            }
            return responseData;
        },
        //树点击之前事件
        beforeClick: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            if (!treeNode.checked && !infoFastInput.checkGroupNum(treeNode.id) && treeNode.type == "assignment") {
                return false;
            }
            zTree.checkNode(treeNode, !treeNode.checked, treeNode, true);
            return false;
        },
        //树点击事件
        onClick: function (e, treeId, treeNode) {
            if (treeId != undefined) {//快速录入与极速录入
                var zTree = $.fn.zTree.getZTreeObj(treeId);
                var v = "";
                var t = "";
                if (treeNode.checked) { //勾选操作才进行验证，取消勾选不进行验证
                    //获取分组和企业ID信息
                    var caId;
                    //定义分组和企业唯一标识
                    var caIdentification;
                    var nodes = [];
                    if (treeNode.type == "assignment") { //为分组节点直接校验
                        caIdentification = 1;
                        caId = treeNode.id;
                        nodes.push(treeNode);
                    } else if (treeNode.type == "group") { //为企业节点获取勾选节点然后去除校验过的节点
                        caIdentification = 2;
                        caId = treeNode.id;
                        nodes = zTree.getCheckedNodes(true);

                        var curArr = [];
                        if (treeId == 'quickTreeDemo') {
                            curArr = checkedAssginment;
                        } else if (treeId == 'fastTreeDemo') {
                            curArr = speedCheckedAssginment;
                        } else {
                            curArr = sweepCheckedAssginment;
                        }
                        for (var i = 0; i < curArr.length; i++) {
                            nodes.remove(curArr[i]);
                        }
                    }
                    //获取还可录入的分组id
                    publicFun.getAllAssignmentVehicleNumber(caId, caIdentification);
                    nodes.sort(function compare(a, b) {
                        return a.id - b.id;
                    });

                    var amtNames = ""; // 车辆数超过100的分组
                    for (var i = 0, l = nodes.length; i < l; i++) {
                        if (nodes[i].type == "assignment") { // 选择的是分组，才组装值
                            if (!publicFun.checkMaxVehicleCountOfAssignment(nodes[i].id)) {
                                nodes[i].checked = false;
                                amtNames += nodes[i].name + ",";
                            }
                        }
                    }
                    // 判断系统是否出问题
                    if (systemErrorFlag) {
                        layer.msg(systemError, {
                            time: 1500,
                        });
                        return;
                    }
                    if (amtNames.length > 0) {
                        amtNames = amtNames.substr(0, amtNames.length - 1);
                        layer.msg("【" + amtNames + "】" + assignmentMaxCarNum);
                    } else {
                        publicFun.clearErrorMsg();
                    }
                }

                //组装校验通过的值，初始化节点选中数组
                if (treeId == 'quickTreeDemo') {
                    checkedAssginment = [];
                } else if (treeId == 'fastTreeDemo') {
                    speedCheckedAssginment = [];
                } else {
                    sweepCheckedAssginment = [];
                }
                var checkedNodes = zTree.getCheckedNodes(true);
                for (var i = 0; i < checkedNodes.length; i++) {
                    if (checkedNodes[i].type == "assignment") {
                        t += checkedNodes[i].name + ",";
                        v += checkedNodes[i].id + ";";
                        if (treeId == 'quickTreeDemo') {
                            checkedAssginment.push(checkedNodes[i]);
                        } else if (treeId == 'fastTreeDemo') {
                            speedCheckedAssginment.push(checkedNodes[i]);
                        } else {
                            sweepCheckedAssginment.push(checkedNodes[i]);
                        }
                    }
                }

                if (v.length > 0) v = v.substring(0, v.length - 1);
                if (t.length > 0) t = t.substring(0, t.length - 1);
                if (treeId == 'quickTreeDemo') {
                    var cityObj = $("#quickGroupId");
                    cityObj.val(t);
                    $("#quickCitySelidVal").val(v);
                } else if (treeId == 'fastTreeDemo') {
                    var cityObj = $("#speedGroupid");
                    cityObj.val(t);
                    $("#speedCitySelidVal").val(v);
                } else if (treeId == 'sweepTreeDemo') {
                    var cityObj = $("#sweepCodeGroupid");
                    cityObj.val(t);
                    $("#sweepCodeCitySelidVal").val(v);
                }
            }
        },
        //获取还可录入的分组id(校验分组车辆上限用)
        getAllAssignmentVehicleNumber: function (cdId, identifier) {
            if (cdId != "" && identifier != "") {
                $.ajax({
                    type: 'POST',
                    url: '/clbs/m/infoconfig/infoinput/getAssignmentCount',
                    dataType: 'json',
                    data: {"id": cdId, "type": identifier},
                    async: false,
                    success: function (data) {
                        if (data.success) {
                            ais = data.obj.ais;
                        } else if (data.msg) {
                            layer.msg(data.msg);
                            systemErrorFlag = true;
                        }
                    },
                    error: function () {
                        layer.msg(systemError, {
                            time: 1500,
                        });
                        systemErrorFlag = true;
                    }
                });
            }
        },
        // 校验当前分组下的车辆数是否已经达到最大值（最大值目前设定为：100台）
        checkSweepMaxVehicleCountOfAssignment: function (assignmentId, assignmentName) {
            var b = true;
            $.ajax({
                type: 'POST',
                url: '/clbs/m/infoconfig/infoinput/checkMaxVehicleCount',
                data: {"assignmentId": assignmentId, "assignmentName": assignmentName},
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (data.success) {
                        b = data.obj.success;
                    } else if (data.msg) {
                        layer.msg(data.msg);
                    }
                },
                error: function () {
                    layer.msg(systemError, {
                        time: 1500,
                    });
                    b = false;
                    systemErrorFlag = true;
                }
            });
            return b;
        },
        // 校验当前分组下的车辆数是否已经达到最大值（最大值目前设定为：100台）
        checkMaxVehicleCountOfAssignment: function (assignmentId) {
            var b = true;
            if ($.inArray(assignmentId, ais) == -1) {
                b = false;
            }
            return b;
        },
        // 校验单个分组下的车辆数是否已经达到最大值（主要用于默认分组勾选校验）
        checkSingleMaxVehicle: function (assignmentId, assignmentName) {
            var b = false;
            $.ajax({
                type: 'POST',
                url: '/clbs/m/infoconfig/infoinput/checkMaxVehicleCount',
                data: {"assignmentId": assignmentId, "assignmentName": assignmentName},
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (data.success) {
                        b = data.obj.success;
                    } else if (data.msg) {
                        layer.msg(data.msg);
                    }
                },
                error: function () {
                    layer.msg(systemError, {
                        time: 1500,
                    });
                    b = false;
                    systemErrorFlag = true;
                }
            });
            return b;
        },
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#error_label").hide();
        },
        // 清除错误信息
        clearErrorMsg: function () {
            $("label.error").hide();
        },
    };


    /**
     * 快速录入
     * */
    var quickMonitorType = 0;//0:选择车,1:选择人,2:选择物
    // 第一次进页面默认查询的数据
    var vehicleInfoList = [];
    var peopleInfoList = [];
    var thingInfoList = [];
    var deviceInfoList = [];//终端信息集合
    var simCardInfoList = [];//终端手机号信息集合
    var speedDeviceInfoList = [];//极速录入终端信息集合
    var ais = [];//还能存入的分组id
    var checkedAssginment = [];//快速录入已被选中的分组
    var speedCheckedAssginment = [];//极速录入已被选中的分组
    var sweepCheckedAssginment = [];//扫码录入已被选中的分组
    var orgId = "";
    var orgName = "";
    var flag1 = false; // 选择还是录入的车牌号
    var flag2 = true; // 选择还是录入的终端号
    var flag3 = true; // 选择还是录入的终端手机号
    var flag4 = false; // 极速 是否是选择的终端号
    var flag5 = true; // 极速 选择还是录入的监控对象
    var flag6 = true; // 极速 选择还是录入的终端手机号
    var hasFlag = true, hasFlag1 = true; // 是否有该唯一标识
    var quickRefresh = true;//快速录入信息是否刷新
    var fastRefresh = true;//极速录入信息是否刷新

    var infoFastInput = {
        //初始化文件树
        init: function () {
            infoFastInput.getInfoData();
        },
        getInfoData: function () {
            var urlList = '/clbs/m/infoconfig/infoFastInput/add';
            var parameterList = {"id": ""};
            json_ajax("POST", urlList, "json", true, parameterList, infoFastInput.InitCallback);
        },
        InitCallback: function (data) {
            if (data.success) {
                datas = data.obj;
                vehicleInfoList = datas.vehicleInfoList;
                peopleInfoList = datas.peopleInfoList;
                thingInfoList = datas.thingInfoList;
                deviceInfoList = datas.deviceInfoList;
                simCardInfoList = datas.simCardInfoList;
                speedDeviceInfoList = datas.speedDeviceInfoList;
                orgId = datas.orgId;
                orgName = datas.orgName;

                infoFastInput.getCallbackList();
            } else if (data.msg) {
                layer.msg(data.msg);
            }
            setTimeout(function () {//请求成功关闭加载动画
                layer.closeAll('loading');
            }, 200);
        },
        inputClick: function () {
            var value = $("#speedDevices").val();
            var flag = false;
            $("#searchDevices-id li").each(function () {
                var name = $(this).text();
                if (name.indexOf(value) == -1) {
                    $(this).hide();
                    $('#searchDevices-id').hide();
                } else {
                    flag = true;
                    $(this).css('display', 'block');
                }
                if (flag) {
                    $('#searchDevices-id').show();
                }
            });
            var width = $("#speedDevices").parent('div').width();
            $('.searchDevices-div ul').css('width', width + 'px');
        },
        menuClick: function () {
            flag4 = true;
            hasFlag = true;
            $('#speedDevices').next('label').hide();
            var device = $(this).data('device');
            var car = $(this).attr('data-car');
            var sim = $(this).attr('data-sim');
            var deviceType = $(this).attr('data-deviceType');
            var manufacturerId = $(this).attr('data-manufacturerId');
            var deviceModelNumber = $(this).attr('data-deviceModelNumber');
            var provinceId = $(this).attr('data-provinceId');
            var cityId = $(this).attr('data-cityId');
            var plateColor = $(this).attr('data-plateColor');
            //限制输入
            var protocolTypeArr = ['0', '1', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '23', '24', '25', '26', '27', '28', '97', '99'];
            if (protocolTypeArr.indexOf(deviceType) !== -1) {
                //设置sim不可修改
                $("#speedSims").prop("disabled", true).css({
                    'cursor': 'not-allowed',
                    'background': 'rgb(238, 238, 238)'
                });
                $('#sim_searchDevice').removeAttr("disabled");
                // $("#speedSims").unbind();//$("#speedSims").prop("disabled", true).css('cursor', 'not-allowed');
                $("#sim_searchDevice").prop("disabled", true);
                //设置终端号可修改
                $("#oneDevices").prop("disabled", false).css({'cursor': 'text', 'background': 'rgb(255, 255, 255)'});
                $("#searchOneDevices").prop("disabled", false);
            } else {
                //设置终端号不可修改
                $("#oneDevices").prop("disabled", true).css({
                    'cursor': 'not-allowed',
                    'background': 'rgb(238, 238, 238)'
                });
                $("#searchOneDevices").prop("disabled", true);

                //设置sim可修改
                $("#speedSims").removeAttr("disabled").css({'cursor': 'text', 'background': 'rgb(255, 255, 255)'});
                // $("#speedSims").bind('click', infoFastInput.searchList);
                $("#sim_searchDevice").prop("disabled", false);
                // infoFastInput.getsiminfoset();
            }
            $('input').inputClear();
            $("#speedDeviceType").val(parseInt(deviceType));
            $("#speedDeviceTypeList").val(deviceType);
            var number = $(this).text();
            var deviceTypename = infoFastInput.commounicationtypedefinite(parseInt(deviceType));

            // 根据选择的注册设备的通讯类型，过滤终端号备选项
            //极速录入终端号
            var filteredValue = [];
            /*var filteredValue = window.deviceDataList.value.filter(function (x) {
                return x.type == deviceType;
            });*/
            json_ajax("GET", '/clbs/m/infoconfig/infoFastInput/fuzzyDevice', "json", false, {"deviceType": deviceType}, function (data) {
                if (data.obj.fuzzyDevice) {// 终端号模糊搜索
                    filteredValue = data.obj.fuzzyDevice.map(function (item) {
                        var newItem = {
                            name: item.deviceNumber,
                            id: item.id,
                        };
                        return newItem;
                    })
                }
            });
            $('#oneDevicesContainer').dropdown({
                data: filteredValue,
                pageCount: 50,
                listItemHeight: 31,
                searchUrl: filteredValue.length >= MAX_MENU_LENGTH ? '/clbs/m/infoconfig/infoFastInput/fuzzyDevice?deviceType=' + deviceType + '' : null,
                onDataRequestSuccess: function (e, result) {
                    $('#speedDevices').removeAttr('disabled');
                },
                onSetSelectValue: function (e, keyword, data) {
                    $("#speedDeviceVal").val(keyword.id);
                    $("#oneDevicesName").val(keyword.name);
                    infoFastInput.hideErrorMsg();
                    infoFastInput.checkIsBound("quickDevices", keyword.name); // 校验当前终端编号是否已经被绑定，两个人同时操作的时候可能会出现
                },
                onUnsetSelectValue: function () {
                    flag5 = false;
                }
            });

            /*$("#oneDevices").bsSuggest("destroy"); // 销毁事件
            $("#oneDevices").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: {value: filteredValue},
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
                $('#speedDevices').removeAttr('disabled');
            }).on('onSetSelectValue', function (e, keyword, data) {
                $("#speedDeviceVal").val(keyword.id);
                $("#oneDevicesName").val(keyword.key);
                infoFastInput.hideErrorMsg();
                infoFastInput.checkIsBound("quickDevices", keyword.name); // 校验当前终端编号是否已经被绑定，两个人同时操作的时候可能会出现
            }).on('onUnsetSelectValue', function () {
            }).on('input propertychange', function () {
                $("#oneDevicesName").val($('#oneDevices').val());
            });*/

            if (infoFastInput.checkIsNull(device)) {
                $("#oneDevices").val(device);
                $("#oneDevicesName").val(device);
            } else {
                $("#oneDevices").val('');
                $("#oneDevicesName").val('');
            }
            $("#messagetype").val(deviceTypename);
            $("#speedDevices").val(number);
            if (infoFastInput.checkIsNull(car)) {
                $("#fastBrands").val(car);
                $("#fastBrandVal").val(car);
            } else {
                $("#fastBrands").val('');
                $("#fastBrandVal").val('');
            }
            if (infoFastInput.checkIsNull(sim)) {
                $("#speedSims").val(sim);
            } else {
                $("#speedSims").val('');
            }

            //制造商id
            if (infoFastInput.checkIsNull(manufacturerId)) {
                $("#manufacturerId").val(manufacturerId);
            } else {
                $("#manufacturerId").val('');
            }

            //终端型号
            if (infoFastInput.checkIsNull(deviceModelNumber)) {
                $("#deviceModelNumber").val(deviceModelNumber);
            } else {
                $("#deviceModelNumber").val('');
            }

            //省市id
            $("#provinceId").val(provinceId);

            //市域id
            $("#cityId").val(cityId);

            //车牌颜色
            $("#fastPlateColor").val(plateColor);

            $("#searchDevices-id").hide();//设置监控对象可修改
            $("#fastBrands").prop("disabled", false).css({'cursor': 'text', 'background': 'rgb(255, 255, 255)'});
            $("#speedBrandsBtn").prop("disabled", false);

            $('.seizeAseat').slideUp();
            $('.terminalAreaDetail').slideDown();
        },

        checkIsNull: function (data) {
            if (data !== 'undefined' && data !== 'null' && data !== '') {
                return true;
            } else {
                return false;
            }
        },

        getCallbackList: function () {
            //监控对象
            var dataList = {
                value: []
            };
            if (quickMonitorType == 0) {
                var i = vehicleInfoList.length;
                while (i--) {
                    dataList.value.push({
                        name: vehicleInfoList[i].brand ? vehicleInfoList[i].brand : '',
                        id: vehicleInfoList[i].id,
                        type: vehicleInfoList[i].plateColor
                    });
                }
            } else if (quickMonitorType == 1) {
                var i = peopleInfoList.length;
                while (i--) {
                    dataList.value.push({
                        name: peopleInfoList[i].brand ? peopleInfoList[i].brand : '',
                        id: peopleInfoList[i].id,
                        type: peopleInfoList[i].monitorType
                    });
                }
            } else if (quickMonitorType == 2) {
                var i = thingInfoList.length;
                while (i--) {
                    dataList.value.push({
                        name: thingInfoList[i].brand ? thingInfoList[i].brand : '',
                        id: thingInfoList[i].id,
                        type: thingInfoList[i].monitorType
                    });
                }
            }
            //终端
            window.deviceDataList = {value: []};
            var j = deviceInfoList.length;
            while (j--) {
                deviceDataList.value.push({
                    name: deviceInfoList[j].deviceNumber,
                    id: deviceInfoList[j].id,
                    type: deviceInfoList[j].deviceType,
                });
            }
            var speedBrandDataList = {value: []}, s = vehicleInfoList.length;
            while (s--) {
                speedBrandDataList.value.push({
                    name: vehicleInfoList[s].brand ? vehicleInfoList[s].brand : '',
                    id: vehicleInfoList[s].id,
                    type: vehicleInfoList[s].monitorType
                });
            }
            //终端手机号
            var simDataList = {value: []}, k = simCardInfoList.length;
            while (k--) {
                simDataList.value.push({
                    name: simCardInfoList[k].simcardNumber,
                    id: simCardInfoList[k].id,
                });
            }

            /**
             * 快速录入(监控对象,终端等信息初始化)
             * */
            if (quickRefresh) {
                if (quickBrandsDropdown) {
                    quickBrandsDropdown.destory();
                }
                quickBrandsDropdown = $('#quickBrandsContainer').dropdown({
                    data: dataList.value,
                    pageCount: 50,
                    listItemHeight: 31,
                    searchUrl: dataList.value.length >= MAX_MENU_LENGTH ? '/clbs/m/infoconfig/infoFastInput/fuzzyMonitor?monitorType=' + quickMonitorType + '' : null,
                    onDataRequestSuccess: function (e, result) {
                        $('#quickBrands').removeAttr('disabled');
                    },
                    onSetSelectValue: function (e, keyword, data) {
                        $("#quickBrandVal").attr("value", keyword.id);
                        infoFastInput.checkIsBound("quickBrands", keyword.name); // 校验当前车牌号是否已经被绑定，两个人同时操作的时候可能会出现
                        infoFastInput.hideErrorMsg();
                        flag1 = true;
                        $('#quickPlateColor').val(keyword.originalItem.type);
                        $('#quickPlateColor').prop('disabled', true);
                        $("#quickBrands").closest('.form-group').find('.dropdown-menu').hide();
                        // $("#quickEntryForm .input-group input").attr("style", "background-color:#ffffff !important;");
                    },
                    onUnsetSelectValue: function () {
                        flag1 = false;
                    }
                });
                $("#quickBrands").on('input propertychange', function () {
                    $('#quickPlateColor').prop('disabled', false);
                });
                /*$("#quickBrands").bsSuggest("destroy"); // 销毁事件
                $("#quickBrands").bsSuggest({
                    indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                    indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                    data: dataList,
                    effectiveFields: ["name"]
                }).on('onDataRequestSuccess', function (e, result) {
                    $('#quickBrands').removeAttr('disabled');
                }).on('onSetSelectValue', function (e, keyword, data) {
                    $("#quickBrandVal").attr("value", keyword.id);
                    infoFastInput.checkIsBound("quickBrands", keyword.name); // 校验当前车牌号是否已经被绑定，两个人同时操作的时候可能会出现
                    infoFastInput.hideErrorMsg();
                    flag1 = true;
                    $('#quickPlateColor').val(keyword.type);
                    $('#quickPlateColor').prop('disabled', true);
                    $("#quickBrands").closest('.form-group').find('.dropdown-menu').hide();
                    $("#quickEntryForm .input-group input").attr("style", "background-color:#ffffff !important;");
                }).on('onUnsetSelectValue', function () {
                    flag1 = false;
                }).on('input propertychange', function () {
                    $('#quickPlateColor').prop('disabled', false);
                });*/

                // $("#quickDevices").bsSuggest("destroy"); // 销毁事件
                // $("#quickDevices").bsSuggest({
                //     indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                //     indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                //     data: deviceDataList,
                //     effectiveFields: ["name"]
                // }).on('onDataRequestSuccess', function (e, result) {
                //     $('#quickDevices').removeAttr('disabled');
                // }).on('onSetSelectValue', function (e, keyword, data) {
                //     $("#quickDeviceVal").attr("value", keyword.id);
                //     infoFastInput.hideErrorMsg();
                //     infoFastInput.checkIsBound("quickDevices", keyword.name); // 校验当前终端编号是否已经被绑定，两个人同时操作的时候可能会出现
                //     flag2 = true;
                //     $("#quickDeviceType").val(keyword.type).prop('disabled', true);
                //     $("#quickDevices").closest('.form-group').find('.dropdown-menu').hide()
                // }).on('onUnsetSelectValue', function () {
                //     flag2 = false;
                //     $("#quickDeviceType").removeAttr('disabled');
                // }).on('input propertychange', function () {
                //     $("#quickDeviceType").removeAttr('disabled');
                // });
                infoFastInput.changeDevice();
                /*$('#quickDevicesContainer').dropdown({
                    data: deviceDataList.value,
                    pageCount: 50,
                    listItemHeight: 31,
                    onDataRequestSuccess: function (e, result) {
                        $('#quickDevices').removeAttr('disabled');
                    },
                    onSetSelectValue: function (e, keyword, data) {
                        $("#quickDeviceVal").attr("value", keyword.id);
                        infoFastInput.hideErrorMsg();
                        infoFastInput.checkIsBound("quickDevices", keyword.name); // 校验当前终端编号是否已经被绑定，两个人同时操作的时候可能会出现
                        flag2 = true;
                        $("#quickDeviceType").val(keyword.originalItem.type).prop('disabled', true);
                        $("#quickDevices").closest('.form-group').find('.dropdown-menu').hide();
                    },
                    onUnsetSelectValue: function () {
                        flag2 = false;
                        $("#quickDeviceType").removeAttr('disabled');
                    }
                });*/

                // $("#quickSims").bsSuggest("destroy"); // 销毁事件
                // $("#quickSims").bsSuggest({
                //     indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                //     indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                //     data: simDataList,
                //     effectiveFields: ["name"]
                // }).on('onDataRequestSuccess', function (e, result) {
                //     $('#quickSims').removeAttr('disabled');
                // }).on('onSetSelectValue', function (e, keyword, data) {
                //     $("#quickSimVal").attr("value", keyword.id);
                //     infoFastInput.hideErrorMsg();
                //     infoFastInput.checkIsBound("quickSims", keyword.name); // 校验当前终端手机号是否已经被绑定，两个人同时操作的时候可能会出现
                //     flag3 = true;
                //     $("#quickSims").closest('.form-group').find('.dropdown-menu').hide();
                // }).on('onUnsetSelectValue', function () {
                //     flag3 = false;
                // });

                $('#quickSimsContainer').dropdown({
                    data: simDataList.value,
                    pageCount: 50,
                    listItemHeight: 31,
                    searchUrl: simDataList.value.length >= MAX_MENU_LENGTH ? '/clbs/m/infoconfig/infoFastInput/fuzzySimCard' : null,
                    onDataRequestSuccess: function (e, result) {
                        $('#quickSims').removeAttr('disabled');
                    },
                    onSetSelectValue: function (e, keyword, data) {
                        $("#quickSimVal").attr("value", keyword.id);
                        infoFastInput.hideErrorMsg();
                        infoFastInput.checkIsBound("quickSims", keyword.name); // 校验当前终端手机号是否已经被绑定，两个人同时操作的时候可能会出现
                        flag3 = true;
                        $("#quickSims").closest('.form-group').find('.dropdown-menu').hide();
                    },
                    onUnsetSelectValue: function () {
                        flag3 = false;
                    }
                });
            }

            /**
             * 极速录入(监控对象,终端等信息初始化)
             * */
            if (fastRefresh) {
                //初始化未注册设备信息
                infoFastInput.loadData(speedDeviceInfoList);
                if (fastBrandsDropdown) {
                    fastBrandsDropdown.destory();
                }
                fastBrandsDropdown = $('#fastBrandsContainer').dropdown({
                    data: dataList.value,
                    pageCount: 50,
                    listItemHeight: 31,
                    searchUrl: dataList.value.length >= MAX_MENU_LENGTH ? '/clbs/m/infoconfig/infoFastInput/fuzzyMonitor?monitorType=' + quickMonitorType + '' : null,
                    onDataRequestSuccess: function (e, result) {

                    },
                    onSetSelectValue: function (e, keyword, data) {
                        $("#speedBrandVal").attr("value", keyword.id);
                        $("#fastBrandVal").attr("value", keyword.name);
                        $('#fastPlateColor').val(keyword.originalItem.type).prop('disabled', true);
                        infoFastInput.checkIsBound("fastBrands", keyword.name); // 校验当前车牌号是否已经被绑定，两个人同时操作的时候可能会出现
                        infoFastInput.hideErrorMsg();
                        flag5 = true;
                        $("#fastBrands").closest('.form-group').find('.dropdown-menu').hide()
                    },
                    onUnsetSelectValue: function () {
                        flag5 = false;
                    }
                });
                $("#fastBrands").on('input propertychange', function () {
                    $('#fastPlateColor').prop('disabled', false);
                });

                /* $("#fastBrands").bsSuggest("destroy"); // 销毁事件
                 $("#fastBrands").bsSuggest({
                     indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                     indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                     data: dataList,
                     effectiveFields: ["name"]
                 }).on('onDataRequestSuccess', function (e, result) {
                 }).on('onSetSelectValue', function (e, keyword, data) {
                     $("#speedBrandVal").attr("value", keyword.id);
                     $("#fastBrandVal").attr("value", keyword.name);
                     $('#fastPlateColor').val(keyword.type).prop('disabled', true);
                     infoFastInput.checkIsBound("fastBrands", keyword.name); // 校验当前车牌号是否已经被绑定，两个人同时操作的时候可能会出现
                     infoFastInput.hideErrorMsg();
                     flag5 = true;
                     $("#fastBrands").closest('.form-group').find('.dropdown-menu').hide()
                 }).on('onUnsetSelectValue', function () {
                     flag5 = false;
                 }).on('input propertychange', function () {
                     $('#fastPlateColor').prop('disabled', false);
                 });*/
                //极速录入终端号
                var deviceType = $("#speedDeviceType").val();
                var _device = [];
                if (deviceType !== null && deviceType.toString().length > 0) {
                    _device = deviceDataList.value.filter(function (x) {
                        return x.type == deviceType;
                    });
                }
                $('#oneDevicesContainer').dropdown({
                    data: _device,
                    pageCount: 50,
                    listItemHeight: 31,
                    searchUrl: _device.length >= MAX_MENU_LENGTH ? '/clbs/m/infoconfig/infoFastInput/fuzzyDevice' : null,
                    onDataRequestSuccess: function (e, result) {
                        $('#speedDevices').removeAttr('disabled');
                    },
                    onSetSelectValue: function (e, keyword, data) {
                        $("#speedDeviceVal").val(keyword.id);
                        $("#oneDevicesName").val(keyword.name);
                        infoFastInput.hideErrorMsg();
                        infoFastInput.checkIsBound("quickDevices", keyword.name); // 校验当前终端编号是否已经被绑定，两个人同时操作的时候可能会出现
                    },
                    onUnsetSelectValue: function () {
                        flag5 = false;
                    }
                });
                $("#oneDevices").on('input propertychange', function () {
                    $("#oneDevicesName").val($('#oneDevices').val());
                });
                /*$("#oneDevices").bsSuggest("destroy"); // 销毁事件
                $("#oneDevices").bsSuggest({
                    indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                    indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                    data: {value: _device},
                    effectiveFields: ["name"]
                }).on('onDataRequestSuccess', function (e, result) {
                    $('#speedDevices').removeAttr('disabled');
                }).on('onSetSelectValue', function (e, keyword, data) {
                    $("#speedDeviceVal").val(keyword.id);
                    $("#oneDevicesName").val(keyword.key);
                    infoFastInput.hideErrorMsg();
                    infoFastInput.checkIsBound("quickDevices", keyword.name); // 校验当前终端编号是否已经被绑定，两个人同时操作的时候可能会出现
                }).on('onUnsetSelectValue', function () {
                }).on('input propertychange', function () {
                    $("#oneDevicesName").val($('#oneDevices').val());
                });
*/
                /*$("#speedSims").bsSuggest("destroy"); // 销毁事件
                $("#speedSims").bsSuggest({
                    indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                    indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                    data: simDataList,
                    effectiveFields: ["name"]
                }).on('onDataRequestSuccess', function (e, result) {
                }).on('onSetSelectValue', function (e, keyword, data) {
                    $("#speedSimVal").attr("value", keyword.id);
                    infoFastInput.hideErrorMsg();
                    infoFastInput.checkIsBound("speedSims", keyword.name); // 校验当前终端手机号是否已经被绑定，两个人同时操作的时候可能会出现
                    flag6 = true;
                    $("#speedSims").closest('.form-group').find('.dropdown-menu').hide();
                }).on('onUnsetSelectValue', function () {
                    flag6 = false;
                });*/

                $('#speedSimsContainer').dropdown({
                    data: simDataList.value,
                    pageCount: 50,
                    listItemHeight: 31,
                    searchUrl: simDataList.value.length >= MAX_MENU_LENGTH ? '/clbs/m/infoconfig/infoFastInput/fuzzySimCard' : null,
                    onDataRequestSuccess: function (e, result) {
                        $('#speedSims').removeAttr('disabled');
                    },
                    onSetSelectValue: function (e, keyword, data) {
                        $("#speedSimVal").attr("value", keyword.id);
                        infoFastInput.hideErrorMsg();
                        infoFastInput.checkIsBound("speedSims", keyword.name); // 校验当前终端手机号是否已经被绑定，两个人同时操作的时候可能会出现
                        flag6 = true;
                        $("#speedSims").closest('.form-group').find('.dropdown-menu').hide();
                    },
                    onUnsetSelectValue: function () {
                        flag6 = false;
                    }
                });

               /* $("#fastBrands").prop('disabled', true);
                $("#oneDevices").prop('disabled', true);
                $("#speedSims").prop('disabled', true);*/
            }

            $('input').inputClear().on('onClearEvent', function (e, data) {
                var id = data.id;
                if (id == 'speedDevices') {
                    infoFastInput.setInputDisabled();
                }
                if (id == 'oneDevices') {
                    $("#oneDevicesName").val('');
                }
                if (id == 'quickBrands') {
                    $("#quickPlateColor").removeAttr('disabled');
                }
                if (id == 'fastBrands') {
                    $("#fastPlateColor").removeAttr('disabled');
                }
                setTimeout(function () {
                    $('#' + id).focus();
                }, 20);
            });
        },
        // 文本框复制事件处理
        inputOnPaste: function (eleId) {
            if (eleId == "quickBrands") {
                flag1 = false;
                $("#quickBrandVal").attr("value", "");
            }
            if (eleId == "quickDevices") {
                flag2 = false;
                $("#quickDeviceVal").attr("value", "");
            }
            if (eleId == "quickSims") {
                flag3 = false;
                $("#quickSimVal").attr("value", "");
            }
            if (eleId == 'fastBrands') {
                flag5 = false;
                $("#speedBrandVal").attr("value", "");
                $("#fastBrandVal").attr("value", "");
            }
            if (eleId == 'speedSims') {
                flag6 = false;
                $("#speedSimVal").attr("value", "");
            }
        },
        //ajax请求回调函数
        getCallback: function (data) {
            if (data.success) {
                for (var i = 0; i < data.obj.vehicleInfoList.length; i++)
                    $("#quickBrands").append("<option value=" + data.obj.vehicleInfoList[i].id + ">" + data.obj.vehicleInfoList[i].brand + "</option>")
                for (var i = 0; i < data.obj.deviceInfoList.length; i++)
                    $("#quickDevices").append("<option value=" + data.obj.deviceInfoList[i].id + ">" + data.obj.deviceInfoList[i].deviceNumber + "</option>")
                for (var i = 0; i < data.obj.simcardInfoList.length; i++)
                    $("#quickSims").append("<option value=" + data.obj.simcardInfoList[i].id + ">" + data.obj.simcardInfoList[i].simcardNumber + "</option>")
                $(".group_select").css("display", "");
            } else if (data.msg) {
                layer.msg(data.msg);
            }
        },
        //提交事件
        doSubmits: function () {
            var str = "";
            if (processInput.checkIsBound("quickBrands", $("#quickBrands").val())) {
                str += "监控对象[" + $("#quickBrands").val() + "]";
            }
            if (processInput.checkIsBound("quickDevices", $("#quickDevices").val())) {
                if (str != '') str += ',';
                str += "终端号[" + $("#quickDevices").val() + "]";
            }
            if (processInput.checkIsBound("quickSims", $("#quickSims").val())) {
                if (str != '') str += ',';
                str += "终端手机号[" + $("#quickSims").val() + "]";
            }
            if (str.length > 0) {
                // str = str.substr(0, str.length - 1);
                layer.msg("不好意思，你来晚了！【" + str + "】已被别人抢先一步绑定了");
                return;
            }

            if (quickMonitorType == 0) {
                if ((!flag1 && !infoFastInput.check_brand()) || !infoFastInput.checkRightBrand("quickBrands")) {
                    return;
                }
            } else if (quickMonitorType == 1) {
                if ((!flag1 && !infoFastInput.check_people_number()) || !infoFastInput.checkRightPeopleNumber()) {
                    return;
                }
            }
            else if (quickMonitorType == 2) {
                if ((!flag1 && !infoFastInput.check_thing()) || !infoFastInput.checkRightBrand("quickBrands")) {
                    return;
                }
            }
            if (infoFastInput.checkIsBound("quickBrands", $("#quickBrands").val())) {
                return;
            }
            if (!infoFastInput.checkIsEmpty("quickDevices", deviceNumberSelect) || infoFastInput.checkIsBound("quickDevices", $("#quickDevices").val())
                || !infoFastInput.checkRightDevice("quickDevices", deviceNumberError) || (!flag2 && !infoFastInput.check_device())) {
                return;
            }
            if ($("#deviceTypeDiv").css("display") != 'none' && !infoFastInput.check_deviceType()) {
                return;
            }
            if (!infoFastInput.checkIsEmpty("quickSims", simNumberNull) || (!flag3 && !infoFastInput.check_sim('quickSims')) || !infoFastInput.checkRightSim('quickSims')) {
                return;
            }
            if (infoFastInput.checkIsBound("quickSims", $("#quickSims").val())) {
                return;
            }
            if (infoFastInput.validate_addForm1()) {
                if ($('#quickCitySelidVal').val() == '') {
                    infoFastInput.showErrorMsg('请选择分组', 'quickGroupId');
                    return;
                }
                infoFastInput.hideErrorMsg();
                var groupIds = $('#quickCitySelidVal').val();
                if (!infoFastInput.checkGroupNum(groupIds)) return;
                $('#quickPlateColor').prop('disabled', false);
                $('#quickSubmits').prop("disabled", true);
                addHashCode1($("#quickEntryForm"));
                $("#quickEntryForm").ajaxSubmit(function (data) {
                    var json = eval("(" + data + ")");
                    if (json.success) {
                        quickRefresh = true;
                        fastRefresh = false;
                        infoFastInput.getInfoData();// 重新加载监控对象,终端,终端手机号等信息
                        infoFastInput.clearQuickInfo();
                        addFlag = true;
                        myTable.requestData();
                    } else if (json.msg) {
                        layer.msg(json.msg);
                    }
                    infoFastInput.refreshToken();
                    $('#quickSubmits').removeAttr("disabled");
                });
            }
        },
        // 校验分组下是否还可录入监控对象
        checkGroupNum: function (groupIds) {
            var submitFlag = false;
            $.ajax({
                type: 'POST',
                url: '/clbs/m/infoconfig/infoinput/getAssignmentCount',
                dataType: 'json',
                data: {"id": groupIds.replace(/\;/g, ','), "type": 1},
                async: false,
                success: function (data) {
                    if (data.success) {
                        var overLimitAssignmentName = data.obj.overLimitAssignmentName;
                        if (overLimitAssignmentName.length === 0) {
                            submitFlag = true;
                        } else {
                            layer.msg("【" + overLimitAssignmentName.join(',') + "】" + assignmentMaxCarNum);
                        }
                    } else if (data.msg) {
                        layer.msg(data.msg);
                    }
                },
            });
            return submitFlag;
        },
        refreshToken: function () {
            var url = '/clbs/m/basicinfo/enterprise/brand/generateFormToken';
            json_ajax("POST", url, "json", false, null, function (data) {
                console.log(data.msg);
                $(".avoidRepeatSubmitToken").val(data.msg);
            });
        },
        // 快速录入完成后,清空相应信息
        clearQuickInfo: function () {
            $("#quickBrands").val('').css('backgroundColor', '#fafafa');
            $("#quickBrandVal").val('');
            $("#quickPlateColor").val('2');
            $("#quickDevices").val('').css('backgroundColor', '#fafafa');
            $("#quickDeviceVal").val('');
            $("#quickSims").val('').css('backgroundColor', '#fafafa');
            $("#quickSimVal").val('');
            $("#quickPlateColor").removeAttr('disabled');
        },
        //快速录入验证
        validate_addForm1: function () {
            return $("#quickEntryForm").validate({
                rules: {
                    deviceType: {
                        required: true
                    },
                    groupid: {
                        required: true
                    },
                },
                messages: {
                    deviceType: {
                        required: deviceDeviceTypeNull
                    },
                    groupid: {
                        required: assignmentNameNull
                    },
                }
            }).form();
        },
        //极速录入验证
        validate_addForm2: function () {
            return $("#fastEntryForm").validate({
                rules: {
                    deviceType: {
                        required: true
                    },
                    groupid: {
                        required: true
                    },
                },
                messages: {
                    deviceType: {
                        required: deviceDeviceTypeNull
                    },
                    groupid: {
                        required: assignmentNameNull
                    },
                }
            }).form();
        },
        // 校验终端手机号是否填写规范或者回车时不小心输入了异常字符
        checkRightSim: function (id) {
            var errorMsg3 = simNumberError;
            /* var reg = /^((13[0-9]{1})|(14[5,7,9]{1})|(15[^4]{1})|(166)|(18[0-9]{1})|(19[8-9]{1})|(17[0,1,3,5,6,7,8]{1}))+\d{8}$/;
             var reg1 = /^((\d{3,4}-\d{7,9})|([1-9]{1}\d{6,12}))$/g;*/
            var reg = /^[0-9a-zA-Z]{7,20}$/g;
            return infoFastInput.checkIsLegal(id, reg, null, '请输入数字字母，范围：7~20位');
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
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#error_label").hide();
        },
        // 校验车牌号是否已存在
        checkBrand: function () {
            var tempFlag = true;
            $.ajax({
                type: 'POST',
                url: '/clbs/m/basicinfo/monitoring/vehicle/repetition',
                data: {"brand": $("#quickBrands").val()},
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (!data) {
                        infoFastInput.showErrorMsg(vehicleBrandExists, "quickBrands");
                        tempFlag = false;
                    } else {
                        infoFastInput.hideErrorMsg();
                        tempFlag = true;
                    }
                },
                error: function () {
                    layer.msg("车牌号校验异常！");
                    tempFlag = false;
                }
            });
            return tempFlag;
        },
        // 校验人员编号是否已存在
        checkPeopleNumber: function () {
            var tempFlag = true;
            $.ajax({
                type: 'POST',
                url: '/clbs/m/basicinfo/monitoring/personnel/repetitionAdd',
                data: {"peopleNumber": $("#quickBrands").val()},
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (!data) {
                        infoFastInput.showErrorMsg(personnelNumberExists, "quickBrands");
                        tempFlag = false;
                    } else {
                        infoFastInput.hideErrorMsg();
                        tempFlag = true;
                    }
                },
                error: function () {
                    layer.msg("人员编号校验异常！", {
                        time: 1500,
                    });
                    tempFlag = false;
                }
            });
            return tempFlag;
        },
        // 校验物品是否已存在
        checkThing: function () {
            var tempFlag = true;
            $.ajax({
                type: 'POST',
                url: '/clbs/m/basicinfo/monitoring/ThingInfo/checkThingNumberSole',
                data: {"thingNumber": $("#quickBrands").val()},
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (!data) {
                        infoFastInput.showErrorMsg(thingExists, "quickBrands");
                        tempFlag = false;
                    } else {
                        infoFastInput.hideErrorMsg();
                        tempFlag = true;
                    }
                },
                error: function () {
                    layer.msg("物品编号校验异常！");
                    tempFlag = false;
                }
            });
            return tempFlag;
        },
        // 校验终端编号是否已存在
        checkDevice: function () {
            var tempFlag = true;
            $.ajax({
                type: 'POST',
                url: '/clbs/m/basicinfo/equipment/device/repetition',
                data: {"deviceNumber": $("#quickDevices").val()},
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (!data) {
                        infoFastInput.showErrorMsg(deviceNumberExists, "quickDevices");
                        tempFlag = false;
                    } else {
                        infoFastInput.hideErrorMsg();
                        tempFlag = true;
                    }
                },
                error: function () {
                    layer.msg("终端号校验异常！");
                    tempFlag = false;
                }
            });
            return tempFlag;
        },
        // 极速录入校验终端编号是否已存在
        checkJsDevice: function () {
            var tempFlag = true;
            $.ajax({
                type: 'POST',
                url: '/clbs/m/basicinfo/equipment/device/repetition',
                data: {"deviceNumber": $("#oneDevices").val()},
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (!data) {
                        infoFastInput.showErrorMsg(deviceNumberExists, "oneDevices");
                        tempFlag = false;
                    } else {
                        infoFastInput.hideErrorMsg();
                        tempFlag = true;
                    }
                },
                error: function () {
                    layer.msg("终端号校验异常！");
                    tempFlag = false;
                }
            });
            return tempFlag;
        },
        // 校验终端手机号是否已存在
        checkSIM: function (id) {
            var tempFlag = true;
            $.ajax({
                type: 'POST',
                url: '/clbs/m/basicinfo/equipment/simcard/repetition',
                data: {"simcardNumber": $("#" + id).val()},
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (!data) {
                        infoFastInput.showErrorMsg(simNumberExists, id);
                        tempFlag = false;
                    } else {
                        infoFastInput.hideErrorMsg();
                        tempFlag = true;
                    }
                },
                error: function () {
                    layer.msg("终端手机号校验异常！");
                    tempFlag = false;
                }
            });
            return tempFlag;
        },
        blurFun: function (id) {
            infoFastInput.hideErrorMsg();
            var inputVal = $("#" + id).val();
            if (id == "quickBrands" && inputVal != "" && !flag1 && !infoFastInput.check_brand()) {
                return;
            } else {
                infoFastInput.hideErrorMsg();
            }
            if (id == "quickDevices" && inputVal != "" && !flag2 && !infoFastInput.check_device()) {
                return;
            } else {
                infoFastInput.hideErrorMsg();
            }
            if (id == "quickSims" && inputVal != "" && !flag3 && !infoFastInput.check_sim('quickSims')) {
                return;
            } else {
                infoFastInput.hideErrorMsg();
            }
            if (id == "fastBrands" && inputVal != "" && !flag5 && !infoFastInput.check_brand()) {
                return;
            } else {
                infoFastInput.hideErrorMsg();
            }
            if (id == "speedSims" && inputVal != "" && !flag6 && !infoFastInput.check_sim('speedSims')) {
                return;
            } else {
                infoFastInput.hideErrorMsg();
            }
        },
        // 校验车牌号是否填写规范或者回车时不小心输入了异常字符
        checkRightBrand: function (id) {
            // var errorMsg3 = vehicleBrandError;
            // wjk
            var errorMsg3 = '请输入汉字、字母、数字或短横杠，长度2-20位';
            if ($("#" + id).val() == '') {
                errorMsg3 = vehicleBrandNull;
            }
            if (checkBrands(id)) {
                infoFastInput.hideErrorMsg();
                return true;
            } else {
                infoFastInput.showErrorMsg(errorMsg3, id);
                return false;
            }
        },
        // 校验人员编号
        checkRightPeopleNumber: function () {
            var reg = /^[A-Za-z0-9\u4e00-\u9fa5_-]{2,20}$/;
            var errorMsg3 = personnelNumberError;
            return infoFastInput.checkIsLegal("quickBrands", reg, null, errorMsg3);
        },
        // 校验终端是否填写规范或者回车时不小心输入了异常字符
        checkRightDevice: function (id, errorMsg) {
            var reg = /^[A-Za-z0-9]{7,30}$/;
            var errorMsg3 = '请输入字母、数字，长度7~30位';

            if (infoFastInput.checkIsLegal(id, reg, null, errorMsg3)) {
                return true;
            } else {
                return false;
            }
        },
        // 校验车辆信息
        check_brand: function () {
            var elementId = "quickBrands";
            var maxLength = 10;
            // var errorMsg1 = vehicleBrandNull;

            // wjk
            var errorMsg1 = '请输入汉字、字母、数字或短横杠，长度2-20位';

            var errorMsg2 = vehicleBrandMaxlength;
            var errorMsg3 = vehicleBrandError;
            var errorMsg4 = vehicleBrandExists;
            //			var reg = /^[\u4eac\u6d25\u5180\u664b\u8499\u8fbd\u5409\u9ed1\u6caa\u82cf\u6d59\u7696\u95fd\u8d63\u9c81\u8c6b\u9102\u6e58\u7ca4\u6842\u743c\u5ddd\u8d35\u4e91\u6e1d\u85cf\u9655\u7518\u9752\u5b81\u65b0\u6d4b]{1}[A-Z]{1}[A-Z_0-9]{5}$/;
            if ($("#quickBrands").val() == '') {
                errorMsg1 = vehicleBrandNull;
            }

            if (infoFastInput.checkIsEmpty(elementId, errorMsg1)
                && infoFastInput.checkRightBrand(elementId)
                && infoFastInput.checkBrand()) {
                return true;
            } else {
                return false;
            }
        },
        // 校验人员信息
        check_people_number: function () {
            var elementId = "quickBrands";
            var maxLength = 8;
            //var errorMsg1 = personnelNumberNull;
            var errorMsg1 = '监控对象不能为空';
            var errorMsg2 = publicSize8Length;
            var errorMsg3 = personnelNumberError;
            var errorMsg4 = personnelNumberExists;
            var reg = /^[A-Za-z0-9\u4e00-\u9fa5_-]{2,20}$/;
            if (infoFastInput.checkIsEmpty(elementId, errorMsg1)
                /* && infoFastInput.checkLength(elementId, maxLength, errorMsg2)*/
                && infoFastInput.checkIsLegal(elementId, reg, null, errorMsg3)
                && infoFastInput.checkPeopleNumber()) {
                return true;
            } else {
                return false;
            }
        },
        // 校验物品信息
        check_thing: function () {
            var elementId = "quickBrands";
            var errorMsg1 = publicMonitorNull;
            if (infoFastInput.checkIsEmpty(elementId, errorMsg1)
                && infoFastInput.checkRightBrand(elementId)
                && infoFastInput.checkThing()) {
                return true;
            } else {
                return false;
            }
        },
        //校验终端信息
        check_device: function () {
            var elementId = "quickDevices";
            var maxLength = 30;
            var errorMsg1 = deviceNumberSelect;
            var errorMsg2 = deviceNumberMaxlength;
            var errorMsg3 = deviceNumberError;
            var errorMsg4 = deviceNumberExists;
            if (infoFastInput.checkIsEmpty(elementId, errorMsg1)
                && infoFastInput.checkLength(elementId, maxLength, errorMsg2)
                && infoFastInput.checkDevice()) {
                return true;
            } else {
                return false;
            }
        },
        // 校验通讯类型
        check_deviceType: function () {
            var elementId = "quickDeviceType";
            var errorMsg1 = deviceDeviceTypeNull;
            if (infoFastInput.checkIsEmpty(elementId, errorMsg1)) {
                infoFastInput.hideErrorMsg();
                return true;
            } else {
                return false;
            }
        },
        //校验终端手机号信息
        check_sim: function (id) {
            var elementId = id;
            var maxLength = 20;
            var errorMsg1 = simNumberNull;
            var errorMsg2 = simNumberMaxlength;
            var errorMsg3 = simNumberError;
            var errorMsg4 = simNumberExists;
            /*var reg = /^((13[0-9]{1})|(14[5,7,9]{1})|(15[^4]{1})|(166)|(18[0-9]{1})|(19[8-9]{1})|(17[0,1,3,5,6,7,8]{1}))+\d{8}$/;
            var reg1 = /^((\d{3,4}-\d{7,9})|([1-9]{1}\d{6,12}))$/g;*/
            var reg = /^[0-9a-zA-Z]{7,20}$/g;
            if (infoFastInput.checkIsEmpty(elementId, errorMsg1)
                && infoFastInput.checkLength(elementId, maxLength, errorMsg2)
                && infoFastInput.checkIsLegal(elementId, reg, null, '请输入数字字母，范围：7~20位')
                && infoFastInput.checkSIM(elementId)) {
                return true;
            } else {
                return false;
            }
        },
        // 校验是否为空
        checkIsEmpty: function (elementId, errorMsg) {
            var value = $("#" + elementId).val() == null || !$("#" + elementId).val() ? "" : $("#" + elementId).val().replace(/(^\s*)|(\s*$)/g, "");
            $("#" + elementId).val(value);
            if (value == "") {
                infoFastInput.hideErrorMsg();
                infoFastInput.showErrorMsg(errorMsg, elementId);
                return false;
            } else {
                infoFastInput.hideErrorMsg();
                return true;
            }
        },
        // 校验是否已存在
        checkIsExists: function (attr, elementId, requestUrl, errorMsg) {
            var tempFlag = true;
            $.ajax({
                type: 'POST',
                url: requestUrl,
                data: {"device": $("#" + elementId).val()},
                dataType: 'json',
                success: function (data) {
                    if (!data.success) {
                        infoFastInput.showErrorMsg(errorMsg, elementId);
                        tempFlag = false;
                    } else {
                        infoFastInput.hideErrorMsg();
                        tempFlag = true;
                    }
                },
                error: function () {
                    infoFastInput.showErrorMsg("校验异常", elementId);
                    tempFlag = false;
                }
            });
            return tempFlag;
        },
        // 校验是否已被绑定
        checkIsBound: function (elementId, elementValue) {
            var tempFlag = false;
            var url = "/clbs/m/infoconfig/infoinput/checkIsBound";
            var data = "";
            if (elementId == "quickBrands") {
                data = {
                    "monitorType": quickMonitorType,
                    "inputId": "quickBrands",
                    "inputValue": $("#" + elementId).val()
                }
            } else if (elementId == "quickDevices" || elementId == "oneDevices") {
                data = {"inputId": "quickDevices", "inputValue": $("#" + elementId).val()}
            } else if (elementId == "quickSims") {
                data = {"inputId": "quickSims", "inputValue": $("#" + elementId).val()}
            } else if (elementId == "fastBrands") {
                data = {
                    "monitorType": quickMonitorType,
                    "inputId": "quickBrands",
                    "inputValue": $("#" + elementId).val()
                }
            } else if (elementId == "speedDevices") {
                data = {"inputId": "quickDevices", "inputValue": $("#" + elementId).val()}
            } else if (elementId == "speedSims") {
                data = {"inputId": "quickSims", "inputValue": $("#" + elementId).val()}
            }
            $.ajax({
                type: 'POST',
                url: url,
                data: data,
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (data.success) {
                        if (null != data && data.obj != null && data.obj.isBound) {
                            layer.msg("不好意思，你来晚了！【" + data.obj.boundName + "】已被别人抢先一步绑定了");
                            tempFlag = true;
                        } else {
                            tempFlag = false;
                        }
                    } else if (data.msg) {
                        layer.msg(data.msg);
                    }
                },
                error: function () {
                    infoFastInput.showErrorMsg("校验异常", elementId);
                    tempFlag = false;
                }
            });
            return tempFlag;
        },
        // 校验填写数据的合法性
        checkIsLegal: function (elementId, reg, reg1, errorMsg) {
            var value = $("#" + elementId).val();
            if (reg1 != null) {
                if (!reg.test(value) && !reg1.test(value)) {
                    infoFastInput.showErrorMsg(errorMsg, elementId);
                    return false;
                } else {
                    infoFastInput.hideErrorMsg();
                    return true;
                }
            } else {
                if (!reg.test(value)) {
                    infoFastInput.showErrorMsg(errorMsg, elementId);
                    return false;
                } else {
                    // wjk
                    // 是否全是'-'或'_'
                    var regIfAllheng = /^[-]*$/;
                    var regIfAllxiahuaxian = /^[_]*$/;

                    if (regIfAllheng.test(value) || regIfAllxiahuaxian.test(value)) {
                        infoFastInput.showErrorMsg('不能全是横杠或下划线', elementId);
                        return false;
                    } else {
                        infoFastInput.hideErrorMsg();
                        return true;
                    }
                }
            }
        },
        checkISdhg: function (elementId) {
            var value = $("#" + elementId).val();
            var regIfAllheng = /^[-]*$/;
            if (regIfAllheng.test(value)) {
                infoFastInput.showErrorMsg('不能全是横杠', elementId);
                return false;
            } else {
                infoFastInput.hideErrorMsg();
                return true;
            }
        },
        // 校验长度
        checkLength: function (elementId, maxLength, errorMsg) {
            var value = $("#" + elementId).val();
            if (value.length > parseInt(maxLength)) {
                infoFastInput.showErrorMsg(errorMsg, elementId);
                return false;
            } else {
                infoFastInput.hideErrorMsg();
                return true;
            }
        },
        // 清除错误信息
        clearErrorMsg: function () {
            $("label.error").hide();
        },
        speedDoSubmits: function () {
            var str = "";
            if (processInput.checkIsBound("fastBrands", $("#fastBrands").val())) {
                str += "监控对象[" + $("#fastBrands").val() + "]";
            }
            if (processInput.checkIsBound("oneDevices", $("#oneDevices").val())) {
                if (str != '') str += ',';
                str += "终端号[" + $("#oneDevices").val() + "]";
            }
            if (processInput.checkIsBound("speedSims", $("#speedSims").val())) {
                if (str != '') str += ',';
                str += "终端手机号[" + $("#speedSims").val() + "]";
            }
            if (str.length > 0) {
                // str = str.substr(0, str.length - 1);
                layer.msg("不好意思，你来晚了！【" + str + "】已被别人抢先一步绑定了");
                return;
            }

            if (!flag4) {
                infoFastInput.showErrorMsg(deviceNumberChoose, 'speedDevices');
                return
            }
            if (!infoFastInput.checkIsEmpty("speedDevices", deviceNumberChoose)) {
                return;
            }
            if (!infoFastInput.checkIsEmpty("fastBrands", vehicleBrandSelect) || !infoFastInput.checkRightBrand("fastBrands")) {
                return;
            }
            if (infoFastInput.checkIsBound("fastBrands", $("#fastBrands").val())) {
                return;
            }
            if (!infoFastInput.checkIsEmpty("oneDevices", '请选择或新增终端号') || infoFastInput.checkIsBound("oneDevices", $("#oneDevices").val())
                || !infoFastInput.checkRightDevice("oneDevices", deviceNumberError)) {
                return;
            }

            if (!infoFastInput.checkIsEmpty("speedSims", simNumberNull) || (!flag6 && !infoFastInput.check_sim('speedSims')) || !infoFastInput.checkRightSim('speedSims')) {
                return;
            }
            if (infoFastInput.checkIsBound("speedSims", $("#speedSims").val())) {
                return;
            }
            if (infoFastInput.validate_addForm2()) {
                if ($('#speedCitySelidVal').val() == '') {
                    infoFastInput.showErrorMsg('请选择分组', 'speedGroupid');
                    return;
                }

                $("#fastBrandVal").val($("#fastBrands").val());
                infoFastInput.hideErrorMsg();
                var groupIds = $('#speedCitySelidVal').val();
                if (!infoFastInput.checkGroupNum(groupIds)) return;

                $('#speedSims').prop('disabled', false);
                $('#fastPlateColor').prop('disabled', false);
                $('#speedSubmits').prop("disabled", true);
                addHashCode1($("#fastEntryForm"));
                $("#fastEntryForm").ajaxSubmit(function (data) {
                    var json = eval("(" + data + ")");
                    if (json.success) {
                        quickRefresh = false;
                        fastRefresh = true;
                        infoFastInput.getInfoData();// 重新加载监控对象,终端,终端手机号等信息
                        infoFastInput.clearFastInfo();
                        infoFastInput.setInputDisabled();
                        addFlag = true;
                        myTable.requestData();
                    } else if (json.msg) {
                        layer.msg(json.msg);
                    }
                    infoFastInput.refreshToken();
                    $("#speedSubmits").removeAttr("disabled");
                });
            }
        },
        // 极速录入完成后,清空相应信息
        clearFastInfo: function () {
            $("#speedDevices").val('');
            $("#fastBrands").val('');
            $("#fastBrandVal").val('');
            $("#fastPlateColor").val('2');
            $("#speedBrandVal").val('');
            $("#fastPlateColor").val(2).prop('disabled', true);
            $("#oneDevices").val('');
            $("#oneDevicesName").val('');
            $("#speedDeviceVal").val('');
            $("#manufacturerId").val('');
            $("#deviceModelNumber").val('');
            $("#provinceId").val('');
            $("#cityId").val('');
            $("#speedSims").val('');
            $("#speedSimVal").val('');
            $("#messagetype").val('');
        },
        GetHttpAddress: function (name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]);
            return null;
        },
        changeInpValDel: function () {
            flag4 = false;
            $("#fastBrands,#speedSims").val('');
        },
        //监听浏览器窗口变化
        windowResize: function () {
            var width = $('#quickGroupId').parent().width();
            var speedWidth = $("#speedGroupid").parent().width();
            $("#menuContent").css('width', width + "px");
            $("#speedMenuContent").css('width', speedWidth + "px");
            setTimeout(function () {
                var width = $("#speedDevices").parent('div').width();
                $('.searchDevices-div ul').css('width', width + 'px');
            }, 200);
        },
        toggleLeft: function () {
            setTimeout(function () {
                var width = $("#speedDevices").parent('div').width();
                $('.searchDevices-div ul').css('width', width + 'px');
            }, 500);
        },
        //急速录入终端查询
        speedSearchDevices: function () {
            var topspeedUrl = "/clbs/m/infoconfig/infoinput/topspeedlist";
            json_ajax("POST", topspeedUrl, "json", true, null, infoFastInput.getTopspeedData1);
        },
        getTopspeedData1: function (data) {
            $("#speedDevices").trigger("focus");
            if (data.success) {
                var list = JSON.parse(data.msg).list;
                //替换静态数据
                speedDeviceInfoList = list;
                //加载数据
                infoFastInput.loadData(list);
                //显示下拉框
                $('.searchDevices-div ul').show();
            } else if (data.msg) {
                layer.msg(data.msg);
            }
        },
        //极速录入终端标识信息、sim标识信息加载数据
        loadData: function (list) {
            var width = $("#speedDevices").parent('div').width();
            var html = '';
            var len = list.length < 50 ? list.length : 50;
            for (var i = 0; i < len; i++) {
                if (list[i].status == 1) {
                    html += '<li id="'+ list[i].id +'" style="background:#dcf5ff;" data-sim="' + list[i].simNumber + '" data-device="' + list[i].deviceId + '" data-car="' + list[i].brand +
                        '" data-deviceType="' + list[i].deviceType + '" data-manufacturerId="' + list[i].manufacturerId + '" data-deviceModelNumber="' + list[i].deviceModelNumber +
                        '" data-provinceId="' + list[i].provinceId + '" data-cityId="' + list[i].cityId + '" data-plateColor="' + list[i].plateColor + '">' + list[i].uniqueNumber + '</li>';
                } else if (list[i].status == 0) {
                    html += '<li id="'+ list[i].id +'" style="background:#fff8b0;" data-sim="' + list[i].simNumber + '" data-device="' + list[i].deviceId + '" data-car="' + list[i].brand +
                        '" data-deviceType="' + list[i].deviceType + '" data-manufacturerId="' + list[i].manufacturerId + '" data-deviceModelNumber="' + list[i].deviceModelNumber +
                        '" data-provinceId="' + list[i].provinceId + '" data-cityId="' + list[i].cityId + '" data-plateColor="' + list[i].plateColor + '">' + list[i].uniqueNumber + '</li>';
                }
                ;
            }
            ;
            $('.searchDevices-div ul').css('width', width + 'px').html(html);
            $("#searchDevices-id li").unbind("click").on("click", infoFastInput.menuClick);
        },
        documentClick: function (event) {
            if (!(event.target.id == 'speedDevices' || event.target.id == 'searchDevices')) {
                $("#searchDevices-id").hide();
            }
            if (!(event.target.id == 'speedSims' || event.target.id == 'sim_searchDevice')) {
                $("#sim-searchDevices-id").hide();
            }
        },

        searchList: function () {
            flag4 = false;
            infoFastInput.setInputDisabled();
            hasFlag = false;
            var flag = false;
            var value = $("#speedDevices").val();
            var len = speedDeviceInfoList.length;
            for(var i = 0; i < len; i++) {
                var uniqueNumber = speedDeviceInfoList[i].uniqueNumber;
                if(uniqueNumber.indexOf(value) == -1){
                    $("#" + speedDeviceInfoList[i].id).hide();
                    $('#searchDevices-id').hide();
                }else{
                    $("#" + speedDeviceInfoList[i].id).css('display', 'block');
                    flag = true;
                }

                if (uniqueNumber == value) {
                    //当有用户输入的标识的时候，默认点击该选项，加载相应标识下的数据
                    hasFlag = true;
                     $("#" + speedDeviceInfoList[i].id).click();
                }
            }

            // $("#searchDevices-id li").each(function () {
            //     var name = $(this).text();
            //     if (name.indexOf(value) == -1) {
            //         $(this).hide();
            //         $('#searchDevices-id').hide();
            //     } else {
            //         $(this).css('display', 'block');
            //         flag = true;
            //     }
            //     if (name == value) {
            //         //当有用户输入的标识的时候，默认点击该选项，加载相应标识下的数据
            //         hasFlag = true;
            //         $(this).click();
            //     }
            // });
            if (flag) {
                $('#searchDevices-id').show();
            }
        },
        //设置极速录入下的选择框是否可用
        setInputDisabled: function () {
            $("#speedSims").val('').prop("disabled", true).css({
                'cursor': 'not-allowed',
                'background': 'rgb(238, 238, 238)'
            });
            // $("#speedSims").unbind();
            $("#sim_searchDevice").prop("disabled", true);
            $("#oneDevices").val('').prop("disabled", true).css({
                'cursor': 'not-allowed',
                'background': 'rgb(238, 238, 238)'
            });
            $("#searchOneDevices").prop("disabled", true);
            $("#fastBrands").val('').prop("disabled", true).css({
                'cursor': 'not-allowed',
                'background': 'rgb(238, 238, 238)'
            });
            $("#speedBrandsBtn").prop("disabled", true);
            $("#messagetype").val('');
        },
        judgehasFlag: function () {
            if (!hasFlag && hasFlag1 && $('#speedDevices').val() != '') {
                layer.msg('请选择已有的未注册设备');
            }
        },
        searchList2: function () {
            var value = $("#speedSims").val();
            var flag = false;
            $("#sim-searchDevices-id li").each(function () {
                var name = $(this).text();
                if (name.indexOf(value) == -1) {
                    $(this).hide();
                    $('#sim-searchDevices-id').hide();
                } else {
                    flag = true;
                    $(this).css('display', 'block');
                }
                ;
                if (flag) {
                    $('#sim-searchDevices-id').show();
                }
            });
            var width = $("#speedSims").parent('div').width();
            $('#sim-searchDevices-id').css('width', width + 'px');
        },
        //车、人、物点击tab切换
        chooseLabClick: function () {
            $("#entryContentBox ul.dropdown-menu").css("display", "none");
            infoFastInput.hideErrorMsg();
            $(this).parents('.form-group').find('input').prop("checked", false);
            $(this).siblings('input').prop("checked", true);
            $(this).parents('.lab-group').find('label.monitoringSelect').removeClass("activeIcon");
            $(this).addClass('activeIcon');
            quickMonitorType = $(this).siblings('input').val();
            var curForm = $(this).closest('form');
            var curFormId = curForm.attr('id');
            if (quickMonitorType == '0') {
                curForm.find('.quickPlateColor').show();
                if (curFormId == 'quickEntryForm') {
                    $('.twoGroupRow').prepend($('.oldDevicesBox'));
                    $('.threeGroupRow').prepend($('.oldGroupBox'));
                } else {
                    $('.threefastGroup').prepend($('.oldsimBox'));
                }
            } else {
                curForm.find('.quickPlateColor').hide();
                if (curFormId == 'quickEntryForm') {
                    $('.oneGroupRow').append($('.oldDevicesBox'));
                    $('.twoGroupRow').append($('.oldGroupBox'));
                } else {
                    $('.twofastGroup').append($('.oldsimBox'));
                }
            }

            $("label.error").hide();//隐藏validate验证错误信息
            $("#quickDevices").val("").attr("style", "background:#FFFFFF");
            $("#quickBrands").val("").attr("style", "background:#FFFFFF");
            $("#deviceTypeDiv").hide(); // 通讯类型选择隐藏
            if (curForm.attr('id') == 'quickEntryForm') {
                quickRefresh = true;
                fastRefresh = false;
            } else {
                quickRefresh = false;
                fastRefresh = true;
            }
            infoFastInput.getCallbackList();
        },
        speedEntryLiClickFn: function () {
            $("#speedDeviceTypeList").removeClass("is-open");
        },
        quickEntryLiClickFn: function () {
            $("#deviceTypeList").removeClass("is-open");
        },
        //根据divecetype确定通讯类型
        commounicationtypedefinite: function (data) {
            return getProtocolName(data);
        },
        //将静态的终端手机号数据放在下拉框
        getsiminfoset: function () {
            //终端手机号
            var simDataList = {value: []}, k = simCardInfoList.length;
            while (k--) {
                simDataList.value.push({
                    name: simCardInfoList[k].simcardNumber,
                    id: simCardInfoList[k].id,
                });
            }
            $("#speedSims").bsSuggest("destroy"); // 销毁事件
            $("#speedSims").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: simDataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                $("#speedSimVal").attr("value", keyword.id);
                infoFastInput.hideErrorMsg();
                infoFastInput.checkIsBound("speedSims", keyword.name); // 校验当前终端手机号是否已经被绑定，两个人同时操作的时候可能会出现
                flag6 = true;
                $("#speedSims").closest('.form-group').find('.dropdown-menu').hide()
            }).on('onUnsetSelectValue', function () {
                flag6 = false;
            });
        },
        //请求的终端数据放入下拉框
        getterminalinfoset: function () {
            //终端
            var devicedataList = {value: []}, k = deviceInfoList.length;
            while (k--) {
                devicedataList.value.push({
                    name: deviceInfoList[k].deviceNumber,
                    id: deviceInfoList[k].id,
                });
            }
            $("#speedDevices").bsSuggest("destroy"); // 销毁事件
            $("#speedDevices").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: devicedataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                $("#speedSimVal").attr("value", keyword.id);
                infoFastInput.hideErrorMsg();
                infoFastInput.checkIsBound("speedDevices", keyword.name); // 校验当前终端手机号是否已经被绑定，两个人同时操作的时候可能会出现
                flag6 = true;
                $("#speedDevices").closest('.form-group').find('.dropdown-menu').hide()
            }).on('onUnsetSelectValue', function () {
                flag6 = false;
            });
        },
        //加载通讯类型下拉框
        loadcommuninput: function () {
            $("#deviceTypeList").bsSuggest("destroy"); // 销毁事件
            $("#quickDeviceType").val("1");
            $("#deviceTypeList").val("交通部JT/T808-2013");
            var dataList_input3 = {
                value: [
                    {"name": "交通部JT/T808-2019(中位)", "id": "21"},
                    {"name": "交通部JT/T808-2019", "id": "11"},
                    {"name": "交通部JT/T808-2013", "id": "1"},
                    {"name": "交通部JT/T808-2013(川标)", "id": "12"},
                    {"name": "交通部JT/T808-2013(冀标)", "id": "13"},
                    {"name": "交通部JT/T808-2011(扩展)", "id": "0"},
                    {"name": "移为", "id": "2"},
                    {"name": "天禾", "id": "3"},
                    {"name": "BDTD-SM", "id": "5"},
                    {"name": "KKS", "id": "6"},
                    {"name": "KKS-EV25", "id": "22"},
                    {"name": "BSJ-A5", "id": "8"},
                    {"name": "ASO", "id": "9"},
                    {"name": "F3超长待机", "id": "10"},
                    {"name": "JT/T808-2011(1078报批稿)", "id": "23"},
                ]
            };
            $("#deviceTypeList").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: dataList_input3,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                $("#deviceTypeList").val(keyword.key);
                $("#quickDeviceType").val(keyword.id);
                $("#deviceTypeList").closest('.form-group').find('.dropdown-menu').hide()
            }).on('onUnsetSelectValue', function () {
            });
        },
        // 数组原型链拓展方法
        arrayExpand: function () {
            // 删除数组指定对象
            Array.prototype.remove = function (obj) {
                for (var i = 0; i < this.length; i++) {
                    var num = this.indexOf(obj);
                    if (num !== -1) {
                        this.splice(num, 1);
                    }
                }
            };
            // 两个数组的差集
            Array.minus = function (a, b) {
                return a.each(function (o) {
                    return b.contains(o) ? null : o
                });
            };
            // 数组功能扩展
            Array.prototype.each = function (fn) {
                fn = fn || Function.K;
                var a = [];
                var args = Array.prototype.slice.call(arguments, 1);
                for (var i = 0, len = this.length; i < len; i++) {
                    var res = fn.apply(this, [this[i], i].concat(args));
                    if (res != null) a.push(res);
                }
                return a;
            };
            // 数组是否包含指定元素
            Array.prototype.contains = function (suArr) {
                for (var i = 0, len = this.length; i < len; i++) {
                    if (this[i] == suArr) {
                        return true;
                    }
                }
                return false;
            }
        },
        // 快速录入,切换通讯类型,联动改变终端号数据
        changeDevice: function (deviceType) {
            var newDeviceArr = deviceDataList.value;
            var data = deviceDataList.value;
            /*for (var i = 0; i < data.length; i++) {
                var item = data[i];
                if (item.type == deviceType) {
                    newDeviceArr.push(item);
                }
            }*/
            $('#quickDevicesContainer').dropdown({
                data: newDeviceArr,
                pageCount: 50,
                listItemHeight: 31,
                searchUrl: newDeviceArr.length >= MAX_MENU_LENGTH ? '/clbs/m/infoconfig/infoFastInput/fuzzyDevice' : null,
                onDataRequestSuccess: function (e, result) {
                    $('#quickDevices').removeAttr('disabled');
                },
                onSetSelectValue: function (e, keyword, data) {
                    $("#quickDeviceVal").attr("value", keyword.id);
                    infoFastInput.hideErrorMsg();
                    infoFastInput.checkIsBound("quickDevices", keyword.name); // 校验当前终端编号是否已经被绑定，两个人同时操作的时候可能会出现
                    flag2 = true;
                    $("#quickDeviceType").val(keyword.originalItem.type).prop('disabled', true);
                    $("#quickDevices").closest('.form-group').find('.dropdown-menu').hide();
                },
                onUnsetSelectValue: function () {
                    flag2 = false;
                    $("#quickDeviceType").removeAttr('disabled');
                }
            });
        }
    };


    /**
     * 扫码录入
     * */
    var selectTreeId = '';
    var selectTreeType = '';
    var $keepOpen = $(".keep-open");
    var keyFlag = true;
    var sweepCodeMonitorType = 0;//监控对象类型
    sweepCodeEntry = {
        //扫码录入执行方法
        PCKeyDownEvent: function () {
            $("#scanSim").on("keydown", function (e) {
                var key = e.which;
                if (key == "13") {
                    if ($("#scanSim").val() == "" || $("#scanSim").val() == null) {
                        setTimeout(function () {
                            $("#scanSim").click().focus();
                        }, 500);
                    } else {
                        setTimeout(function () {
                            $("#terminal").click().focus();
                        }, 500);
                        var sim = $("#scanSim").val();
                        $.ajax({
                            type: 'POST',
                            async: true,
                            data: {"sim": sim, "monitorType": sweepCodeMonitorType},
                            url: '/clbs/m/infoconfig/infoFastInput/getRandomNumbers',
                            dataType: 'json',
                            success: function (data) {
                                if (data == 26) {
                                    layer.msg("你就这么无聊吗?扫同一张卡26次，卡表示已经不行了，请换一张卡或者把没用的监控对象删了吧！");
                                    $("#monitorTheObject").val("");
                                } else if (data == -1) {
                                    layer.msg("系统响应异常，请稍后再试或联系管理员！");
                                } else {
                                    $("#monitorTheObject").val(data);
                                }
                            },
                            error: function () {
                                layer.msg("获取监控对象编号异常!");
                            }
                        });
                        $.ajax({
                            type: 'POST',
                            async: true,
                            data: {"simcardNumber": sim},
                            url: '/clbs/m/infoconfig/infoinput/getSimcardInfoBySimcardNumber',
                            dataType: 'json',
                            success: function (data) {
                                if (data.success) {
                                    if (data != null && data.obj != null && data.obj.simcardInfo != null) {
                                        $("#scanSim").css("background-color", "#fafafa");
                                    } else {
                                        $("#scanSim").css("background-color", "rgba(255, 0, 0, 0.1)");
                                    }
                                } else if (data.msg) {
                                    layer.msg(data.msg);
                                }
                            },
                            error: function () {
                                layer.msg("判断sim信息失败!");
                            }
                        });
                    }
                }
            })
            $("#terminal").on("keydown", function (ev) {
                var key = ev.which;
                //添加一个标记防止用户连续点击回车重复提交数据
                if (key == "13" && keyFlag) {
                    keyFlag = false;
                    if ($("#terminal").val() == "" || $("#terminal").val() == null) {
                        setTimeout(function () {
                            $("#terminal").click().focus();
                            keyFlag = true;
                        }, 500);
                    }
                    else if ($("#scanSim").val() == "" || $("#scanSim").val() == null) {
                        setTimeout(function () {
                            $("#scanSim").click().focus();
                            keyFlag = true;
                        }, 500);
                    }
                    else {
                        var devices = $("#terminal").val();
                        $.ajax({
                            type: 'POST',
                            async: false,
                            data: {"deviceNumber": devices},
                            url: '/clbs/m/infoconfig/infoinput/getDeviceInfoByDeviceNumber',
                            dataType: 'json',
                            success: function (data) {
                                if (data.success) {
                                    if (data != null && data.obj != null && data.obj.deviceInfo != null) {
                                        $("#terminal").css("background-color", "#fafafa");
                                    } else {
                                        $("#terminal").css("background-color", "rgba(255, 0, 0, 0.1)");
                                    }
                                } else if (data.msg) {
                                    layer.msg(data.msg);
                                }
                            },
                            error: function () {
                                layer.msg("判断终端信息失败!");
                            }
                        });
                        if ($('#sweepCodeCitySelidVal').val() == '') {
                            infoFastInput.showErrorMsg('请选择分组', 'sweepCodeGroupid');
                            return;
                        }
                        if (sweepCodeEntry.validate()) {
                            var str = "";
                            var device = $("#terminal").val();
                            /*var deviceType = $("#communicationType").val();
                            if (deviceType == 1 || deviceType == 0) {
                                device = device.substring(device.length - 7, device.length);
                            }*/
                            if (sweepCodeEntry.checkIsBound("brands", $("#monitorTheObject").val())) {
                                str += "监控对象[" + $("#monitorTheObject").val() + "]";
                            }
                            if (sweepCodeEntry.checkIsBound("devices", device)) {
                                if (str != '') str += ',';
                                str += "终端号[" + $("#terminal").val() + "]";
                            }
                            if (sweepCodeEntry.checkIsBound("sims", $("#scanSim").val())) {
                                if (str != '') str += ',';
                                str += "终端手机号[" + $("#scanSim").val() + "]";
                            }
                            if (str.length > 0) {
                                // str = str.substr(0, str.length - 1);
                                layer.closeAll();
                                layer.msg("不好意思，你来晚了！【" + str + "】已被别人抢先一步绑定了");
                                keyFlag = true;
                                return;
                            }
                            var groupIds = $('#sweepCodeCitySelidVal').val();
                            if (!infoFastInput.checkGroupNum(groupIds)) return;
                            $("#addScanCodeEntry").ajaxSubmit(function (message) {
                                var data = JSON.parse(message);
                                if (data.success) {
                                    keyFlag = true;
                                    sweepCodeEntry.scanCodeEntryShow();
                                    if (navigator.userAgent.indexOf('MSIE') >= 0) {
                                        $("#SCEMsgBox").html('<embed id="IEsceMsg" src="../../../file/music/sceMsg.mp3" autostart="true"/>');
                                    } else {
                                        $("#SCEMsgBox").html('<audio id="SCEMsgAutoOff" src="../../../file/music/sceMsg.mp3" autoplay="autoplay"></audio>');
                                    }
                                    addFlag = true;
                                    myTable.requestData();
                                } else {
                                    layer.msg(data.msg);
                                }
                                ;
                            });
                        }
                        else {
                            keyFlag = true;
                        }
                    }
                }
                else {
                    keyFlag = true;
                }
            })
        },
        // 校验是否已被绑定
        checkIsBound: function (elementId, elementValue) {
            var tempFlag = false;
            var url = "/clbs/m/infoconfig/infoinput/checkIsBound";
            var data = "";
            if (elementId == "brands") {
                data = {"inputId": "brands", "inputValue": elementValue}
            } else if (elementId == "devices") {
                data = {"inputId": "devices", "inputValue": elementValue}
            } else if (elementId == "sims") {
                data = {"inputId": "sims", "inputValue": elementValue}
            }
            $.ajax({
                type: 'POST',
                url: url,
                data: data,
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (data.success) {
                        if (null != data && data.obj != null && data.obj.isBound) {
                            layer.msg("不好意思，你来晚了！【" + data.obj.boundName + "】已被别人抢先一步绑定了");
                            tempFlag = true;
                        } else {
                            tempFlag = false;
                        }
                    } else if (data.msg) {
                        layer.msg(data.msg);
                    }
                },
                error: function () {
                    layer.msg("校验异常！");
                    tempFlag = false;
                }
            });
            return tempFlag;
        },
        //扫码校验
        validate: function () {
            return $("#addScanCodeEntry").validate({
                rules: {
                    groupid: {
                        required: true
                    },
                    citySelID: {
                        required: true
                    },
                    brands: {
                        required: true,
                        isBrand: true,
                    },
                    sims: {
                        isNewSim: true
                    },
                    devices: {
                        checkNewDeviceNumber: "#communicationType"
                    }
                },
                messages: {
                    citySelID: {
                        required: "请至少选择一个分组"
                    },
                    groupid: {
                        required: "请至少选择一个分组"
                    },
                    brands: {
                        required: "监控对象不能为空",
                        isBrand: '请重新录入终端手机号',
                    },
                    sims: {
                        isNewSim: '请输入数字字母，范围：7~20位'
                    },
                    devices: {
                        checkNewDeviceNumber: deviceSpeedNumberError
                    }
                }
            }).form();
        },
        //扫码录入显示执行
        scanCodeEntryShow: function () {
            publicFun.onClick();
            //显示
            if ($("#scanCodeEntry").is(":hidden")) {
                $("#scanCodeEntry").show();
                $("#scanSim").val("");
                $("#terminal").val("");
                $("#scanSim").focus();
            } else {
                $("#scanSim").val("");
                $("#terminal").val("");
                $("#monitorTheObject").val("");
                $("#scanSim").focus();
                $("#scanSim").css("background-color", "#fafafa");
                $("#terminal").css("background-color", "#fafafa");
                publicFun.clearErrorMsg();
            }
            //改变标题栏图标
            if ($("#scanCodeEntryContent").is(":hidden")) {
                $("#scanCodeEntryContent").show();
                if ($("#sceFac").hasClass("fa fa-chevron-up")) {
                    $("#sceFac").attr("class", "fa fa-chevron-down");
                }
            }
        },
        //车、人、物点击tab切换
        chooseLabClick: function () {
            $("#entryContentBox ul.dropdown-menu").css("display", "none");
            publicFun.hideErrorMsg();
            $(this).parents('.lab-group').find('input').prop("checked", false);
            $(this).siblings('input').prop("checked", true);
            $(this).parents('.lab-group').find('label.monitoringSelect').removeClass("activeIcon");
            $(this).addClass('activeIcon');
            $("label.error").hide();//隐藏validate验证错误信息
            sweepCodeMonitorType = $(this).siblings('input').val();
            //切换监控对象类型时清空之前的信息
            $("#monitorTheObject").val("");
            $("#scanSim").val("");
            $("#terminal").val("");
            //光标移到终端手机号信息框中
            $("#scanSim").focus();
        },
    };

    $(function () {
        publicFun.groupTreeInit('quick');
        publicFun.leftTree();

        /**
         * 信息录入方式tab切换
         * */
        $(".entryBtn").on('click', function () {
            searchFlag = true;
            $(this).removeClass('btn-default').addClass('btn-primary');
            $(this).siblings().removeClass('btn-primary').addClass('btn-default');
            var curId = $(this).attr('data-target');
            if (curId == 'quickEntry' || curId == 'fastEntry') {
                quickMonitorType = $("#" + curId + ' input[type="radio"]:checked').val();
            }
            if (curId == 'fastEntry') {
                if (fastInitFlag) {
                    publicFun.groupTreeInit('fast');
                    fastInitFlag = false;
                }

            } else if (curId == 'sweepCodeEntry') {
                setTimeout(function () {
                    $('#scanSim').focus();
                }, 20);
                if (sweepInitFlag) {
                    publicFun.groupTreeInit('sweep');
                    sweepInitFlag = false;
                }
            } else if (curId == 'processEntry') {
                if (processInitFlag) {
                    processInput.init();
                    processInitFlag = false;
                }
            }
            $("#" + curId).addClass('active').siblings().removeClass('active');
        });


        /**
         * 信息列表相关方法初始化
         * */
        infoinputList.init();
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'search_condition') {
                search_ztree('treeDemo', id, treeSearchType.value);
            }
        });
        //IE9
        if (navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") == "MSIE9.0") {
            var search;
            $("#search_condition").bind("focus", function () {
                search = setInterval(function () {
                    search_ztree('treeDemo', 'search_condition', treeSearchType.value);
                }, 500);
            }).bind("blur", function () {
                clearInterval(search);
            });
        }
        infoinputList.showMenuText();
        if (navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") == "MSIE9.0") {
            infoinputList.refreshTable();
        }
        $("#refreshTable").on("click", infoinputList.refreshTable);


        /**
         * 快速录入以及极速录入相关方法初始化
         * */
        var datas;
        infoFastInput.arrayExpand();
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            switch (id) {
                case 'speedDevices':
                    infoFastInput.setInputDisabled();
                    break;
                case 'oneDevices':
                    $("#oneDevicesName").val('');
                    break;
                case 'quickBrands':
                    $("#quickPlateColor").removeAttr('disabled');
                    break;
                case 'fastBrands':
                    $("#fastPlateColor").removeAttr('disabled');
                    break;
                case 'quickGroupId':
                    searchFlag = false;
                    $("#quickCitySelidVal").val('');
                    publicFun.groupTreeInit('quick');
                    break;
                case 'speedGroupid':
                    searchFlag = false;
                    $("#speedCitySelidVal").val('');
                    publicFun.groupTreeInit('fast');
                    break;
                case 'sweepCodeGroupid':
                    searchFlag = false;
                    $("#sweepCodeCitySelidVal").val('');
                    publicFun.groupTreeInit('sweep');
                    break;
                case 'quickDevices':
                    $("#quickDeviceVal").attr('value', '');
                    break;
                case 'quickSims':
                    $("#quickSimVal").attr('value', '');
                    break;
                default:
                    break;
            }
            setTimeout(function () {
                $('#' + id).focus();
            }, 20);
        });
        infoFastInput.init();
        $(".groupZtree").on("click", showMenuContent);

        $('#quickSubmits').on("click", infoFastInput.doSubmits);
        $("#speedSubmits").bind("click", infoFastInput.speedDoSubmits);

        /*$("#quickBrands").blur(function () {
            infoFastInput.blurFun("quickBrands")
        });*/
        $("#quickBrands").bind("paste", function () {
            infoFastInput.inputOnPaste("quickBrands")
        });

        $("#quickDevices").blur(function () {
            infoFastInput.blurFun("quickDevices")
        });
        $("#quickDevices").bind("paste", function () {
            infoFastInput.inputOnPaste("quickDevices")
        });

        $("#fastBrands").blur(function () {
            infoFastInput.blurFun("fastBrands")
        });
        $("#fastBrands").bind("paste", function () {
            infoFastInput.inputOnPaste("fastBrands")
        });

        $("#speedSims").blur(function () {
            infoFastInput.blurFun("speedSims")
        });
        $("#speedSims").bind("paste", function () {
            infoFastInput.inputOnPaste("speedSims")
        });

        $("#quickDeviceType").bind("change", function () {
            infoFastInput.check_deviceType();
            // var deviceType = $(this).val();
            // infoFastInput.changeDevice(deviceType);
        });
        $("#quickSims").blur(function () {
            setTimeout(function () {
                infoFastInput.blurFun("quickSims")
            }, 310);
        });
        $("#quickSims").bind("paste", function () {
            infoFastInput.inputOnPaste("quickSims")
        });
        //监听浏览器窗口变化
        $(window).resize(infoFastInput.windowResize);
        $("#toggle-left").on("click", infoFastInput.toggleLeft);

        //极速录入终端为标识查询
        $("#searchDevices").bind('click', infoFastInput.speedSearchDevices);

        $("#speedDevices").bind('click', infoFastInput.inputClick);//键盘回车事件
        $(document).bind('click', infoFastInput.documentClick);
        //模糊匹配
        $("#speedDevices").bind('input onproperchange', infoFastInput.searchList);//判断用户输入的是否是已经有的唯一标识
        $("#speedDevices").bind('blur', infoFastInput.judgehasFlag);
        $("#onlyLogo").on('mouseover ', function () {
            hasFlag1 = false;
        });
        $("#onlyLogo").on('mouseout', function () {
            hasFlag1 = true;
        });

        //新增终端
        $("#oneDevices").bind('input onproperchange change', function () {
            $("#oneDevicesName").val($(this).val());
        });

        //终端手机号数据模糊匹配
        // $("#speedSims").bind('click', infoFastInput.searchList2);
        //车、人、物点击tab切换
        $("#quickEntryForm label.monitoringSelect,#fastEntryForm label.monitoringSelect").on("click", infoFastInput.chooseLabClick);

        $(".select-value,.btn-width-select").buttonGroupPullDown();
        $("#speedEntryLi").on("click", infoFastInput.speedEntryLiClickFn);
        $("#quickEntryLi").on("click", infoFastInput.quickEntryLiClickFn);

        $("#quickGroupId").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("quickTreeDemo");
            treeObj.checkAllNodes(false);
            $("#quickCitySelidVal").val('');
            search_ztree('quickTreeDemo', 'quickGroupId', 'assignment');
        });
        $("#speedGroupid").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("fastTreeDemo");
            treeObj.checkAllNodes(false);
            $("#speedCitySelidVal").val('');
            search_ztree('fastTreeDemo', 'speedGroupid', 'assignment');
        });


        /**
         * 流程录入服务期限日历框初始化
         * */
        var start_data = processInput.CurentTime();
        var end_data = processInput.nextyeartime();
        $('#timeInterval').dateRangePicker({
            'type': 'after',
            'format': 'YYYY-MM-DD',
            'start_date': start_data,
            'end_date': end_data,
            // 'element': '.nextBtnData'
        });

        /**
         * 扫码录入相关方法初始化
         * */
        sweepCodeEntry.PCKeyDownEvent();
        //扫码录入显示执行
        $("#scanCodeEntryShow").on("click", sweepCodeEntry.scanCodeEntryShow);
        // 组织架构模糊搜索
        $("#search_condition").on("input oninput", function () {
            search_ztree("treeDemo", 'search_condition', treeSearchType.value);
        });
        //车、人、物点击tab切换
        $("#addScanCodeEntry label.monitoringSelect").on("click", sweepCodeEntry.chooseLabClick);
        $("#sweepCodeGroupid").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("sweepTreeDemo");
            treeObj.checkAllNodes(false);
            $("#sweepCodeCitySelidVal").val('');
            search_ztree('sweepTreeDemo', 'sweepCodeGroupid', 'assignment');
        });

        /**
         * 控制极速录入未注册设备流程说明图片的显示隐藏
         * */
        var mainWrapper = $('.main-content-wrapper');
        $("#fastStartGuide").on('click', function () {
            $('body').css('overflow-y', 'hidden');
            if (mainWrapper.hasClass('main-content-toggle-left')) {
                $('#fastInstructions').css('padding-left', '15px');
            } else {
                $('#fastInstructions').css('padding-left', '255px');
            }
            $('#fastInstructions').show();
        })
        $('#fastInstructions').on('click', function () {
            $('#fastInstructions').hide();
            $('body').css('overflow-y', 'auto');
        })

        // 输入框输入类型过滤
        inputValueFilter('#simpleQueryParam');

        $("#confirmDeleteSubmit").bind('click', function () {
            var url = '/clbs/m/infoconfig/infoinput/deleteConfig_' + deleteconfigId + '.gsp';
            json_ajax('POST', url, 'json', false, null, function (data) {
                if (data.success) {
                    layer.msg('删除成功');
                    $("#confirmDeleteModal").modal('hide');
                    myTable.requestData()
                } else {
                    layer.msg(data.msg);
                }
            })
        })
    })
})(window, $)