(function (window, $) {
    var objType = 0;//0:选择车,1:选择人,2:选择物
    // 第一次进页面默认查询的数据
    var vehicleInfoList = [];
    var peopleInfoList = [];
    var thingInfoList = [];
    var deviceInfoList = [];//终端信息集合
    var deviceInfoListForPeople = [];
    var simCardInfoList = [];//终端手机号信息集合
    var professionalsInfoList = [];
    var speedDeviceInfoList = [];//极速录入终端信息集合
    var speedSimInfoList = [];//极速录入终端手机号信息集合
    var ais = [];//还能存入的分组id
    var checkedAssginment = [];//快速录入已被选中的分组
    var speedCheckedAssginment = [];//极速录入已被选中的分组
    var orgId = "";
    var orgName = "";
    var flag1 = false; // 选择还是录入的车牌号
    var flag2 = true; // 选择还是录入的终端号
    var flag3 = true; // 选择还是录入的终端手机号
    var flag4 = false; // 极速 是否是选择的终端号
    var flag5 = true; // 极速 选择还是录入的监控对象
    var flag6 = true; // 极速 选择还是录入的终端手机号
    var hasFlag = true, hasFlag1 = true; // 是否有该唯一标识

    var infoFastInput = {
        //初始化文件树
        init: function () {
            //判断用户进来的路径是快速录入还是极速录入
            var speedUrl = infoFastInput.GetHttpAddress("speedFlag");
            if (speedUrl != null && speedUrl.toString().length > 1) {//极速录入
                $("#quickEntryLi").removeClass("active");
                $("#speedEntryLi").attr("class", "active");
                $("#quickEntry").removeClass("active");
                $("#speedEntry").attr("class", "tab-pane active");
                $("#quickEntry").bind("click", function () {
                    $("#speedEntry").removeClass("active");
                    $(".hideGroup").show();
                });
                $("#quickEntryLi").bind("click", function () {
                    $("#addForm1").show();
                });
                $("#speedEntryLi").bind("click", function () {
                    $("#addForm1").hide();
                });
            } else {//快速录入
                $("#addForm1").show();
            }

            $("[data-toggle='tooltip']").tooltip();
            var setting = {
                async: {
                    url: "/clbs/m/infoconfig/infoinput/tree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    contentType: "application/json",
                    dataType: "json",
                    dataFilter: infoFastInput.ajaxDataFilter
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
                    beforeClick: infoFastInput.beforeClick,
                    onClick: infoFastInput.onClick,
                    onCheck: infoFastInput.onClick,
                    onAsyncSuccess: infoFastInput.onAsyncSuccess
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
            var speedSetting = {
                async: {
                    url: "/clbs/m/infoconfig/infoinput/tree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    contentType: "application/json",
                    dataType: "json",
                    dataFilter: infoFastInput.ajaxDataFilter2
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
                    beforeClick: infoFastInput.speedBeforeClick,
                    onClick: infoFastInput.speedOnClick,
                    onCheck: infoFastInput.speedOnClick,
                    onAsyncSuccess: infoFastInput.speedOnAsyncSuccess
                }
            };
            $.fn.zTree.init($("#speedTreeDemo"), speedSetting, null);
            //获得列表数据
            setTimeout(function () {//加载动画
                layer.load(2);
            }, 200);
            var urlList = '/clbs/m/infoconfig/infoFastInput/add';
            var parameterList = {"id": ""};
            json_ajax("POST", urlList, "json", true, parameterList, infoFastInput.InitCallback);

        },
        onAsyncSuccess: function () {
            //初始化快速录入节点选中数组
            var nodes1 = $.fn.zTree.getZTreeObj("treeDemo").getCheckedNodes(true);
            for (var i = 0; i < nodes1.length; i++) {
                if (nodes1[i].type == "assignment") {
                    checkedAssginment.push(nodes1[i]);
                }
            }
        },
        speedOnAsyncSuccess: function () {
            //初始化极速录入节点选中数组
            var nodes2 = $.fn.zTree.getZTreeObj("speedTreeDemo").getCheckedNodes(true);
            for (var i = 0; i < nodes2.length; i++) {
                if (nodes2[i].type == "assignment") {
                    speedCheckedAssginment.push(nodes2[i]);
                }
            }
        },
        InitCallback: function (data) {
            if (data.success) {
                datas = data.obj;
                vehicleInfoList = datas.vehicleInfoList;
                peopleInfoList = datas.peopleInfoList;
                thingInfoList = datas.thingInfoList;
                deviceInfoList = datas.deviceInfoList;
                //deviceInfoListForPeople = datas.deviceInfoListForPeople;
                simCardInfoList = datas.simCardInfoList;
                speedDeviceInfoList = datas.speedDeviceInfoList;
                orgId = datas.orgId;
                orgName = datas.orgName;
                infoFastInput.getCallbackList();
            } else {
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
            //限制输入
            if (deviceType == '0' || deviceType == '1' || deviceType == '8' || deviceType == '9' || deviceType == '10') {
                //设置sim不可修改
                $("#speedSims").attr("readonly", "readonly").css({
                    'cursor': 'not-allowed',
                    'background': 'rgb(238, 238, 238)'
                });
                $("#speedSims").unbind();//$("#speedSims").prop("disabled", true).css('cursor', 'not-allowed');
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
                $("#speedSims").attr("readonly", null).css({'cursor': 'text', 'background': 'rgb(255, 255, 255)'});
                $("#speedSims").bind('click', infoFastInput.searchList);
                $("#sim_searchDevice").prop("disabled", false);
                infoFastInput.getsiminfoset();
            }
            $('input').inputClear();
            $("#speedDeviceType").val(parseInt(deviceType));
            $("#speedDeviceTypeList").val(deviceType);
            var number = $(this).text();
            var deviceTypename = infoFastInput.commounicationtypedefinite(parseInt(deviceType));
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
                $("#speedBrands").val(car);
            } else {
                $("#speedBrands").val('');
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

            $("#searchDevices-id").hide();//设置监控对象可修改
            $("#speedBrands").prop("disabled", false).css('cursor', 'text');
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
            $("#brands").bsSuggest("destroy"); // 销毁事件
            if (objType == 0) {
                var i = vehicleInfoList.length;
                while (i--) {
                    dataList.value.push({
                        name: vehicleInfoList[i].brand ? vehicleInfoList[i].brand : '',
                        id: vehicleInfoList[i].id,
                        type: vehicleInfoList[i].monitorType,
                    });
                }
            } else if (objType == 1) {
                var i = peopleInfoList.length;
                while (i--) {
                    dataList.value.push({
                        name: peopleInfoList[i].brand ? peopleInfoList[i].brand : '',
                        id: peopleInfoList[i].id,
                        type: peopleInfoList[i].monitorType,
                    });
                }
            } else if (objType == 2) {
                var i = thingInfoList.length;
                while (i--) {
                    dataList.value.push({
                        name: thingInfoList[i].brand ? thingInfoList[i].brand : '',
                        id: thingInfoList[i].id,
                        type: thingInfoList[i].monitorType,
                    });
                }
            }
            $("#brands").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: dataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
                $('#brands').removeAttr('disabled');
            }).on('onSetSelectValue', function (e, keyword, data) {
                $("#brandVal").attr("value", keyword.id);
                infoFastInput.checkIsBound("brands", keyword.name); // 校验当前车牌号是否已经被绑定，两个人同时操作的时候可能会出现
                infoFastInput.hideErrorMsg();
                flag1 = true;
                $("#brands").closest('.form-group').find('.dropdown-menu').hide();
                $(".input-group input").attr("style", "background-color:#ffffff !important;");
            }).on('onUnsetSelectValue', function () {
                flag1 = false;
            });

            var speedBrandDataList = {value: []}, s = vehicleInfoList.length;
            while (s--) {
                speedBrandDataList.value.push({
                    name: vehicleInfoList[s].brand ? vehicleInfoList[s].brand : '',
                    id: vehicleInfoList[s].id,
                    type: vehicleInfoList[s].monitorType,
                });
            }
            $("#speedBrands").bsSuggest("destroy"); // 销毁事件
            $("#speedBrands").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: dataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                $("#speedBrandVal").attr("value", keyword.id);
                infoFastInput.checkIsBound("speedBrands", keyword.name); // 校验当前车牌号是否已经被绑定，两个人同时操作的时候可能会出现
                infoFastInput.hideErrorMsg();
                flag5 = true;
                $("#speedBrands").closest('.form-group').find('.dropdown-menu').hide()
            }).on('onUnsetSelectValue', function () {
                flag5 = false;
            });
            //终端
            var deviceDataList = {value: []};
            $("#devices").bsSuggest("destroy"); // 销毁事件
            //$("#speedDevices").bsSuggest("destroy"); //销毁事件
            /*if (objType == 0) {*/
            var j = deviceInfoList.length;
            while (j--) {
                deviceDataList.value.push({
                    name: deviceInfoList[j].deviceNumber,
                    id: deviceInfoList[j].id,
                });
            }
            /*} else if (objType == 1){
                var j = deviceInfoListForPeople.length;
                while (j--) {
                    deviceDataList.value.push({
                        name : deviceInfoListForPeople[j].deviceNumber,
                        id : deviceInfoListForPeople[j].id,
                    });
                }
            }*/

            //初始化极速录入终端信息
            infoFastInput.loadData(speedDeviceInfoList);

            $("#devices").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: deviceDataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
                $('#devices').removeAttr('disabled');
            }).on('onSetSelectValue', function (e, keyword, data) {
                $("#deviceVal").attr("value", keyword.id);
                infoFastInput.hideErrorMsg();
                infoFastInput.checkIsBound("devices", keyword.name); // 校验当前终端编号是否已经被绑定，两个人同时操作的时候可能会出现
                flag2 = true;
                $("#deviceTypeDiv").hide(); // 通讯类型选择隐藏
                $("#devices").closest('.form-group').find('.dropdown-menu').hide()
            }).on('onUnsetSelectValue', function () {
                flag2 = false;
                $("#deviceTypeDiv").show(); // 通讯类型选择显示
            }).on('input propertychange', function () {
                $("#deviceTypeDiv").show();
            });

            //极速录入终端号
            $("#oneDevices").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: deviceDataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
                $('#speedDevices').removeAttr('disabled');
            }).on('onSetSelectValue', function (e, keyword, data) {
                $("#speedDeviceVal").val(keyword.id);
                $("#oneDevicesName").val(keyword.key);
                infoFastInput.hideErrorMsg();
                infoFastInput.checkIsBound("devices", keyword.name); // 校验当前终端编号是否已经被绑定，两个人同时操作的时候可能会出现
            }).on('onUnsetSelectValue', function () {
            }).on('input propertychange', function () {
            });
            //终端手机号
            var simDataList = {value: []}, k = simCardInfoList.length;
            while (k--) {
                simDataList.value.push({
                    name: simCardInfoList[k].simcardNumber,
                    id: simCardInfoList[k].id,
                });
            }
            //$("#sims").bsSuggest("destroy"); // 销毁事件
            $("#sims").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: simDataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
                $('#sims').removeAttr('disabled');
            }).on('onSetSelectValue', function (e, keyword, data) {
                $("#simVal").attr("value", keyword.id);
                infoFastInput.hideErrorMsg();
                infoFastInput.checkIsBound("sims", keyword.name); // 校验当前终端手机号是否已经被绑定，两个人同时操作的时候可能会出现
                flag3 = true;
                $("#sims").closest('.form-group').find('.dropdown-menu').hide();
            }).on('onUnsetSelectValue', function () {
                flag3 = false;
            });
            //$("#speedSims").bsSuggest("destroy"); // 销毁事件
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
            });
        },
        // 文本框复制事件处理
        inputOnPaste: function (eleId) {
            if (eleId == "brands") {
                flag1 = false;
                $("#brandVal").attr("value", "");
            }
            if (eleId == "devices") {
                flag2 = false;
                $("#deviceVal").attr("value", "");
            }
            if (eleId == "sims") {
                flag3 = false;
                $("#simVal").attr("value", "");
            }
            if (eleId == 'speedBrands') {
                flag5 = false;
                $("#speedBrandVal").attr("value", "");
            }
            if (eleId == 'speedSims') {
                flag6 = false;
                $("#speedSimVal").attr("value", "");
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
                        } else {
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
                    } else {
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
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var flag = true;
            if (responseData) {
                for (var i = 0; i < responseData.length; i++) {
                    if (flag && responseData[i].type == 'assignment') {
                        if (infoFastInput.checkSingleMaxVehicle(responseData[i].id, responseData[i].name)) {
                            responseData[i].checked = true;
                            $("#groupid").attr('value', responseData[i].name);
                            $("#citySelidVal").val(responseData[i].id);
                        }
                        flag = false;
                    }
                    ;
                    responseData[i].open = true;
                }
            }
            return responseData;
        },
        //文件树初始化之前事件
        ajaxDataFilter2: function (treeId, parentNode, responseData) {
            var flag = true;
            if (responseData) {
                for (var i = 0; i < responseData.length; i++) {
                    if (flag && responseData[i].type == 'assignment') {
                        if (infoFastInput.checkSingleMaxVehicle(responseData[i].id, responseData[i].name)) {
                            responseData[i].checked = true;
                            $("#speedGroupid").attr('value', responseData[i].name);
                            $("#speedCitySelidVal").val(responseData[i].id);
                        }
                        ;
                        flag = false;
                    }
                    ;
                    responseData[i].open = true;
                }
            }
            return responseData;
        },
        //树点击之前事件
        beforeClick: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, treeNode, true);
            return false;
        },
        //树点击事件
        onClick: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
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
                    for (var i = 0; i < checkedAssginment.length; i++) {
                        nodes.remove(checkedAssginment[i]);
                    }
                }
                //获取还可录入的分组id
                infoFastInput.getAllAssignmentVehicleNumber(caId, caIdentification);
                nodes.sort(function compare(a, b) {
                    return a.id - b.id;
                });

                var amtNames = ""; // 车辆数超过100的分组
                for (var i = 0, l = nodes.length; i < l; i++) {
                    if (nodes[i].type == "assignment") { // 选择的是分组，才组装值
                        if (!infoFastInput.checkMaxVehicleCountOfAssignment(nodes[i].id)) {
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
                    layer.msg("【分组:" + amtNames + "】" + assignmentMaxCarNum);
                } else {
                    infoFastInput.clearErrorMsg();
                }
            }

            //组装校验通过的值，初始化节点选中数组
            checkedAssginment = [];
            var checkedNodes = zTree.getCheckedNodes(true);
            for (var i = 0; i < checkedNodes.length; i++) {
                if (checkedNodes[i].type == "assignment") {
                    t += checkedNodes[i].name + ",";
                    v += checkedNodes[i].id + ";";
                    checkedAssginment.push(checkedNodes[i]);
                }
            }

            if (v.length > 0) v = v.substring(0, v.length - 1);
            if (t.length > 0) t = t.substring(0, t.length - 1);
            var cityObj = $("#groupid");
            cityObj.attr("value", t);
            $("#citySelidVal").val(v);
        },
        //树点击之前事件
        speedBeforeClick: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("speedTreeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, treeNode, true);
            return false;
        },
        //树点击事件
        speedOnClick: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("speedTreeDemo");
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
                    for (var i = 0; i < speedCheckedAssginment.length; i++) {
                        nodes.remove(speedCheckedAssginment[i]);
                    }
                }
                //获取还可录入的分组id
                infoFastInput.getAllAssignmentVehicleNumber(caId, caIdentification);
                nodes.sort(function compare(a, b) {
                    return a.id - b.id;
                });

                var amtNames = ""; // 车辆数超过100的分组
                for (var i = 0, l = nodes.length; i < l; i++) {
                    if (nodes[i].type == "assignment") { // 选择的是分组，才组装值
                        if (!infoFastInput.checkMaxVehicleCountOfAssignment(nodes[i].id)) {
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
                    layer.msg("【分组:" + amtNames + "】" + assignmentMaxCarNum);
                } else {
                    infoFastInput.clearErrorMsg();
                }
            }

            //组装校验通过的值，初始化节点选中数组
            speedCheckedAssginment = [];
            var checkedNodes = zTree.getCheckedNodes(true);
            for (var i = 0; i < checkedNodes.length; i++) {
                if (checkedNodes[i].type == "assignment") {
                    t += checkedNodes[i].name + ",";
                    v += checkedNodes[i].id + ";";
                    speedCheckedAssginment.push(checkedNodes[i]);
                }
            }

            if (v.length > 0) v = v.substring(0, v.length - 1);
            if (t.length > 0) t = t.substring(0, t.length - 1);
            var cityObj = $("#speedGroupid");
            cityObj.attr("value", t);
            $("#speedCitySelidVal").val(v);
        },
        //树显示事件
        showMenu: function () {
            if ($("#menuContent").is(":hidden")) {
                var width = $(this).parent().width();
                $("#menuContent").css('width', width + "px");
                $(window).resize(function () {
                    var width = $(this).parent().width();
                    $("#menuContent").css('width', width + "px");
                })
                $("#menuContent").slideDown("fast");
                //触发focus时间让边框变蓝
                $("#groupid").trigger("focus");
            } else {
                $("#menuContent").is(":hidden");
            }
            $("body").bind("mousedown", infoFastInput.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "menuContent" || $(event.target).parents("#menuContent").length > 0)) {
                infoFastInput.hideMenu();
            }
        },
        hideMenu: function () {
            $("#menuContent").fadeOut("fast");
            $("body").unbind("mousedown", infoFastInput.onBodyDown);
        },
        //极速录入树显示事件
        speedShowMenu: function () {
            if ($("#speedMenuContent").is(":hidden")) {
                var width = $(this).parent().width();
                $("#speedMenuContent").css('width', width + "px");
                $(window).resize(function () {
                    var width = $(this).parent().width();
                    $("#speedMenuContent").css('width', width + "px");
                })
                $("#speedMenuContent").slideDown("fast");
                //触发focus时间让边框变蓝
                $("#speedGroupid").trigger("focus");
            } else {
                $("#speedMenuContent").is(":hidden");
            }
            $("body").bind("mousedown", infoFastInput.speedOnBodyDown);
        },
        speedOnBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "speedMenuContent" || $(event.target).parents("#speedMenuContent").length > 0)) {
                infoFastInput.speedHideMenu();
            }
        },
        speedHideMenu: function () {
            $("#speedMenuContent").fadeOut("fast");
            $("body").unbind("mousedown", infoFastInput.speedOnBodyDown);
        },
        //ajax请求回调函数
        getCallback: function (data) {
            if (data.success) {
                for (var i = 0; i < data.obj.vehicleInfoList.length; i++)
                    $("#brands").append("<option value=" + data.obj.vehicleInfoList[i].id + ">" + data.obj.vehicleInfoList[i].brand + "</option>")
                for (var i = 0; i < data.obj.deviceInfoList.length; i++)
                    $("#devices").append("<option value=" + data.obj.deviceInfoList[i].id + ">" + data.obj.deviceInfoList[i].deviceNumber + "</option>")
                for (var i = 0; i < data.obj.simcardInfoList.length; i++)
                    $("#sims").append("<option value=" + data.obj.simcardInfoList[i].id + ">" + data.obj.simcardInfoList[i].simcardNumber + "</option>")
                $(".group_select").css("display", "");
            } else {
                layer.msg(data.msg);
            }
        },
        //提交事件
        doSubmits: function () {
            if (objType == 0) {
                if ((!flag1 && !infoFastInput.check_brand()) || !infoFastInput.checkRightBrand("brands")) {
                    return;
                }
            } else if (objType == 1) {
                if ((!flag1 && !infoFastInput.check_people_number()) || !infoFastInput.checkRightPeopleNumber()) {
                    return;
                }
            }
            else if (objType == 2) {
                if ((!flag1 && !infoFastInput.check_thing()) || !infoFastInput.checkRightBrand("brands")) {
                    return;
                }
            }
            if (infoFastInput.checkIsBound("brands", $("#brands").val())) {
                return;
            }
            if (!infoFastInput.checkIsEmpty("devices", deviceNumberSelect) || infoFastInput.checkIsBound("devices", $("#devices").val())
                || !infoFastInput.checkRightDevice("devices", deviceNumberError) || (!flag2 && !infoFastInput.check_device())) {
                return;
            }
            if ($("#deviceTypeDiv").css("display") != 'none' && !infoFastInput.check_deviceType()) {
                return;
            }
            if (!infoFastInput.checkIsEmpty("sims", simNumberNull) || (!flag3 && !infoFastInput.check_sim('sims')) || !infoFastInput.checkRightSim('sims')) {
                return;
            }
            if (infoFastInput.checkIsBound("sims", $("#sims").val())) {
                return;
            }
            if (infoFastInput.validate_addForm1()) {
                infoFastInput.hideErrorMsg();
                $("#submits").attr("disabled", true);
                $("#addForm1").ajaxSubmit(function (data) {
                    try {
                        var json = eval("(" + data + ")");
                    } catch (e) {
                        window.location.reload();
                    }
                    if (json.success) {
                        $("#submits").attr("disabled", false);
                        window.location.href = '/clbs/m/infoconfig/infoinput/list';
                    } else {
                        layer.msg(json.msg);
                    }
                });
            }
        },
        //快速录入验证
        validate_addForm1: function () {
            return $("#addForm1").validate({
                rules: {
                    deviceType: {
                        required: true
                    },
                    groupid: {
                        required: true
                    },
                    //devices : {
                    //	checkDeviceNumber : "#deviceType"
                    //}
                },
                messages: {
                    deviceType: {
                        required: deviceDeviceTypeNull
                    },
                    groupid: {
                        required: assignmentNameNull
                    },
                    //devices : {
                    //	checkDeviceNumber : deviceNumberError
                    //}
                }
            }).form();
        },
        //极速录入验证
        validate_addForm2: function () {
            return $("#speedAddForm1").validate({
                rules: {
                    deviceType: {
                        required: true
                    },
                    groupid: {
                        required: true
                    },
                    //devices : {
                    //	checkDeviceNumber : "#speedDeviceType"
                    //}
                },
                messages: {
                    deviceType: {
                        required: deviceDeviceTypeNull
                    },
                    groupid: {
                        required: assignmentNameNull
                    },
                    //devices : {
                    //	checkDeviceNumber : deviceNumberError
                    //}
                }
            }).form();
        },
        // 校验终端手机号是否填写规范或者回车时不小心输入了异常字符
        checkRightSim: function (id) {
            var errorMsg3 = simNumberError;
            var reg = /^((13[0-9]{1})|(14[5,7,9]{1})|(15[^4]{1})|(166)|(18[0-9]{1})|(19[8-9]{1})|(17[0,1,3,5,6,7,8]{1}))+\d{8}$/;
            var reg1 = /^((\d{3,4}-\d{7,9})|([1-9]{1}\d{6,12}))$/g;
            return infoFastInput.checkIsLegal(id, reg, reg1, errorMsg3);
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
                data: {"brand": $("#brands").val()},
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (!data) {
                        infoFastInput.showErrorMsg(vehicleBrandExists, "brands");
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
                data: {"peopleNumber": $("#brands").val()},
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (!data) {
                        infoFastInput.showErrorMsg(personnelNumberExists, "brands");
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
                data: {"thingNumber": $("#brands").val()},
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (!data) {
                        infoFastInput.showErrorMsg(thingExists, "brands");
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
                data: {"deviceNumber": $("#devices").val()},
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (!data) {
                        infoFastInput.showErrorMsg(deviceNumberExists, "devices");
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
            if (id == "brands" && inputVal != "" && !flag1 && !infoFastInput.check_brand()) {
                return;
            } else {
                infoFastInput.hideErrorMsg();
            }
            if (id == "devices" && inputVal != "" && !flag2 && !infoFastInput.check_device()) {
                return;
            } else {
                infoFastInput.hideErrorMsg();
            }
            if (id == "sims" && inputVal != "" && !flag3 && !infoFastInput.check_sim('sims')) {
                return;
            } else {
                infoFastInput.hideErrorMsg();
            }
            if (id == "speedBrands" && inputVal != "" && !flag5 && !infoFastInput.check_brand()) {
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
            return infoFastInput.checkIsLegal("brands", reg, null, errorMsg3);
        },
        // 校验终端是否填写规范或者回车时不小心输入了异常字符
        checkRightDevice: function (id, errorMsg) {
            var reg = /^[A-Za-z0-9]{7,20}$/;
            // if (objType == 0) {
            //     // reg = /^[A-Za-z0-9_-]{7,15}$/;
            //
            //     //wjk
            //     reg = /^[A-Za-z0-9]{7,15}$/;
            // } else {
            //     // reg = /^[0-9a-zA-Z_-]{1,20}$/;
            //
            //     //wjk
            //     reg = /^[0-9a-zA-Z]{1,20}$/;
            //
            //     // errorMsg3 = '请输入字母/数字，范围（车）7~15（人）1~20'
            // }

            var errorMsg3 = '请输入字母、数字，长度7~20位';

            if (infoFastInput.checkIsLegal(id, reg, null, errorMsg3)) {
                return true;
            } else {
                return false;
            }
        },
        // 校验车辆信息
        check_brand: function () {
            var elementId = "brands";
            var maxLength = 10;
            // var errorMsg1 = vehicleBrandNull;

            // wjk
            var errorMsg1 = '请输入汉字、字母、数字或短横杠，长度2-20位';

            var errorMsg2 = vehicleBrandMaxlength;
            var errorMsg3 = vehicleBrandError;
            var errorMsg4 = vehicleBrandExists;
//			var reg = /^[\u4eac\u6d25\u5180\u664b\u8499\u8fbd\u5409\u9ed1\u6caa\u82cf\u6d59\u7696\u95fd\u8d63\u9c81\u8c6b\u9102\u6e58\u7ca4\u6842\u743c\u5ddd\u8d35\u4e91\u6e1d\u85cf\u9655\u7518\u9752\u5b81\u65b0\u6d4b]{1}[A-Z]{1}[A-Z_0-9]{5}$/;
            if ($("#brands").val() == '') {
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
            var elementId = "brands";
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
            var elementId = "brands";
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
            var elementId = "devices";
            var maxLength = 20;
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
            var elementId = "deviceType";
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
            var maxLength = 13;
            var errorMsg1 = simNumberNull;
            var errorMsg2 = simNumberMaxlength;
            var errorMsg3 = simNumberError;
            var errorMsg4 = simNumberExists;
            var reg = /^((13[0-9]{1})|(14[5,7,9]{1})|(15[^4]{1})|(166)|(18[0-9]{1})|(19[8-9]{1})|(17[0,1,3,5,6,7,8]{1}))+\d{8}$/;
            var reg1 = /^((\d{3,4}-\d{7,9})|([1-9]{1}\d{6,12}))$/g;
            if (infoFastInput.checkIsEmpty(elementId, errorMsg1)
                && infoFastInput.checkLength(elementId, maxLength, errorMsg2)
                && infoFastInput.checkIsLegal(elementId, reg, reg1, errorMsg3)
                && infoFastInput.checkSIM(elementId)) {
                return true;
            } else {
                return false;
            }
        },
        // 校验是否为空
        checkIsEmpty: function (elementId, errorMsg) {
            var value = $("#" + elementId).val().replace(/(^\s*)|(\s*$)/g, "");
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
            if (elementId == "brands") {
                data = {"monitorType": objType, "inputId": "brands", "inputValue": $("#" + elementId).val()}
            } else if (elementId == "devices" || elementId == "oneDevices") {
                data = {"inputId": "devices", "inputValue": $("#" + elementId).val()}
            } else if (elementId == "sims") {
                data = {"inputId": "sims", "inputValue": $("#" + elementId).val()}
            } else if (elementId == "speedBrands") {
                data = {"monitorType": objType, "inputId": "brands", "inputValue": $("#" + elementId).val()}
            } else if (elementId == "speedDevices") {
                data = {"inputId": "devices", "inputValue": $("#" + elementId).val()}
            } else if (elementId == "speedSims") {
                data = {"inputId": "sims", "inputValue": $("#" + elementId).val()}
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
                    } else {
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

                    // infoFastInput.hideErrorMsg();
                    // return true;
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
            if (!flag4) {
                infoFastInput.showErrorMsg(deviceNumberChoose, 'speedDevices');
                return
            }
            if (!infoFastInput.checkIsEmpty("speedDevices", deviceNumberChoose)) {
                return;
            }
            /* if (!infoFastInput.checkIsEmpty("oneDevices", '请选择或新增终端号') || !infoFastInput.checkRightDevice("oneDevices", deviceNumberError)
                 || infoFastInput.checkIsBound("speedDevices", $("#speedDevices").val()) || !infoFastInput.checkJsDevice()) {
                 return;
             }*/
            if (!infoFastInput.checkIsEmpty("oneDevices", '请选择或新增终端号') || infoFastInput.checkIsBound("oneDevices", $("#oneDevices").val())
                || !infoFastInput.checkRightDevice("oneDevices", deviceNumberError)) {
                return;
            }

            if (!infoFastInput.checkIsEmpty("speedSims", simNumberNull) || (!flag6 && !infoFastInput.check_sim('speedSims')) || !infoFastInput.checkRightSim('speedSims')) {
                return;
            }
            if (!infoFastInput.checkIsEmpty("speedBrands", vehicleBrandSelect) || !infoFastInput.checkRightBrand("speedBrands")) {
                return;
            }
            if (infoFastInput.checkIsBound("speedBrands", $("#speedBrands").val())) {
                return;
            }
            if (infoFastInput.checkIsBound("speedSims", $("#speedSims").val())) {
                return;
            }
            if (infoFastInput.validate_addForm2()) {
                infoFastInput.hideErrorMsg();
                $("#speedSubmits").attr("disabled", true);
                $("#speedAddForm1").ajaxSubmit(function (data) {
                    try {
                        var json = eval("(" + data + ")");
                    } catch (e) {
                        window.location.reload();
                    }
                    if (json.success) {
                        $("#speedSubmits").attr("disabled", false);
                        window.location.href = '/clbs/m/infoconfig/infoinput/list';
                    } else {
                        layer.msg(json.msg);
                    }
                });
            }
        },
        GetHttpAddress: function (name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]);
            return null;
        },
        changeInpValDel: function () {
            flag4 = false;
            $("#speedBrands,#speedSims").val('');
        },
        //监听浏览器窗口变化
        windowResize: function () {
            var width = $('#groupid').parent().width();
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
        //enter监听事件
        keydownEvent: function (e) {
            var key = e.which;
            if (key == 13) {
                if ($("#quickEntryLi").hasClass('active')) {
                    var brandsValue = $('#brands').val();
                    if (brandsValue == '') {
                        $('#brands').click().focus();
                    } else {
                        var devicesValue = $('#devices').val();
                        if (devicesValue == '') {
                            $('#devices').click().focus();
                        } else {
                            var simsValue = $('#sims').val();
                            if (simsValue == '') {
                                $('#sims').click().focus();
                            } else {
                                var groupidValue = $('#groupid').val();
                                if (groupidValue == '') {
                                    $('#groupid').click();
                                } else {
                                    infoFastInput.doSubmits();
                                }
                                ;
                            }
                            ;
                        }
                        ;
                    }
                    ;
                } else {
                    var speedDevicesValue = $('#speedDevices').val();
                    if (speedDevicesValue == '') {
                        $('#speedDevices').click().focus();
                    } else {
                        infoFastInput.speedDoSubmits();
                    }
                    ;
                }
                ;
            }
            ;
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
            } else {
                layer.msg(data.msg);
            }
        },
        //极速录入终端标识信息、sim标识信息加载数据
        loadData: function (list) {
            var width = $("#speedDevices").parent('div').width();
            var html = '';
            for (var i = 0, len = list.length; i < len; i++) {
                if (list[i].status == 1) {
                    html += '<li style="background:#dcf5ff;" data-sim="' + list[i].simNumber + '" data-device="' + list[i].deviceId + '" data-car="' + list[i].brand +
                        '" data-deviceType="' + list[i].deviceType + '" data-manufacturerId="' + list[i].manufacturerId + '" data-deviceModelNumber="' + list[i].deviceModelNumber +
                        '" data-provinceId="' + list[i].provinceId + '" data-cityId="' + list[i].cityId + '">' + list[i].uniqueNumber + '</li>';
                } else if (list[i].status == 0) {
                    html += '<li style="background:#fff8b0;" data-sim="' + list[i].simNumber + '" data-device="' + list[i].deviceId + '" data-car="' + list[i].brand +
                        '" data-deviceType="' + list[i].deviceType + '" data-manufacturerId="' + list[i].manufacturerId + '" data-deviceModelNumber="' + list[i].deviceModelNumber +
                        '" data-provinceId="' + list[i].provinceId + '" data-cityId="' + list[i].cityId + '">' + list[i].uniqueNumber + '</li>';
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
            $("#searchDevices-id li").each(function () {
                var name = $(this).text();
                if (name.indexOf(value) == -1) {
                    $(this).hide();
                    $('#searchDevices-id').hide();
                } else {
                    $(this).css('display', 'block');
                    flag = true;
                }
                if (name == value) {
                    //当有用户输入的标识的时候，默认点击该选项，加载相应标识下的数据
                    hasFlag = true;
                    $(this).click();
                }
            });
            if (flag) {
                $('#searchDevices-id').show();
            }
        },
        //设置极速录入下的选择框是否可用
        setInputDisabled: function () {
            //$("#speedSims").val('').prop("disabled", true).css('cursor', 'not-allowed');
            $("#speedSims").val('').attr("readonly", "readonly").css({
                'cursor': 'not-allowed',
                'background': 'rgb(238, 238, 238)'
            });
            $("#speedSims").unbind();
            $("#sim_searchDevice").prop("disabled", true);
            $("#oneDevices").val('').prop("disabled", true).css({
                'cursor': 'not-allowed',
                'background': 'rgb(238, 238, 238)'
            });
            $("#searchOneDevices").prop("disabled", true);
            $("#speedBrands").val('').prop("disabled", true).css('cursor', 'not-allowed');
            $("#speedBrandsBtn").prop("disabled", true);
            $("#messagetype").val('');
        },
        judgehasFlag: function () {
            if (!hasFlag && hasFlag1) {
                layer.msg('请选择已有的未注册设备');
            }
        },
        searchList2: function () {
            /*		var flag = false;
                    var value = $("#speedSims").val();
                    $("#sim-searchDevices-id li").each(function(){
                        var name = $(this).text();
                        if(name.indexOf(value) == -1){
                            $(this).hide();
                            $('#sim-searchDevices-id').hide();
                        }else{
                            $(this).css('display','block');
                            flag = true;
                        };
                    });
                    if(flag){
                        $('#sim-searchDevices-id').show();
                    };*/
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
        //根据监控对象类型改变终端类型及功能类型
        DeviceChange: function (type) {
            $("#deviceTypeList").bsSuggest("destroy"); // 销毁事件
            $("#deviceTypeList").html("");
            /*if (objType == 0) {*/
            $("#deviceTypeList").removeAttr("readonly", "readonly");
            /*$("#deviceTypeList").html(
                '<li value="1">交通部JT/T808-2013</li>'+
               '<li value="0">交通部JT/T808-2011(扩展)</li>'+
            '<li value="2">移为</li>'+
            '<li value="3">天禾</li>'+
            '<li value="6">KKS</li>'+
            '<li value="8">BSJ-A5</li>'+
            '<li  value="9">ASO</li>'+
            '<li  value="10">F3超长待机</li>'
            );*/
            $("#deviceType").val("1");
            $("#deviceTypeList").val("交通部JT/T808-2013");
            var dataList_input = {
                value: [
                    {"name": "交通部JT/T808-2013", "id": "1"},
                    {"name": "交通部JT/T808-2011(扩展)", "id": "0"},
                    {"name": "移为", "id": "2"},
                    {"name": "天禾", "id": "3"},
                    {"name": "BDTD-SM", "id": "5"},
                    {"name": "KKS", "id": "6"},
                    {"name": "BSJ-A5", "id": "8"},
                    {"name": "ASO", "id": "9"},
                    {"name": "F3超长待机", "id": "10"}
                ]
            };
            $("#deviceTypeList").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: dataList_input,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                $("#deviceTypeList").val(keyword.key);
                $("#deviceType").val(keyword.id)
                $("#deviceTypeList").closest('.form-group').find('.dropdown-menu').hide()
            }).on('onUnsetSelectValue', function () {
            });

            /*} else {
                $("#deviceType").val("5");
                $("#deviceTypeList").val("BDTD-SM");
                $("#deviceTypeList").attr("readonly", "readonly");
                /!*	 var dataList_input2 = {
                          value : [
                                {"name":"BDTD-SM","id":"5"},
                                   ]
                      };
                    $("#deviceTypeList").bsSuggest({
                          indexId: 0,  //data.value 的第几个数据，作为input输入框的内容
                          indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                          data: dataList_input2,
                          effectiveFields: ["name"]
                      }).on('onDataRequestSuccess', function (e, result) {
                          debugger
                      }).on('onSetSelectValue', function (e, keyword, data) {
                          $("#communicatype").val(keyword.key);
                          $("#deviceType").val(keyword.id)
                      }).on('onUnsetSelectValue', function () {
                      });*!/

            }*/
            //重新绑定下拉框事件
            //	$(".select-value,.btn-width-select").buttonGroupPullDown();
        },
        //车、人、物点击tab切换
        chooseLabClick: function () {
            $(".entry-content ul.dropdown-menu").css("display", "none");
            infoFastInput.hideErrorMsg();
            $(this).parents('.form-group').find('input').prop("checked", false);
            $(this).siblings('input').prop("checked", true);
            $(this).parents('.form-group').find('label.monitoringSelect').removeClass("activeIcon");
            $(this).addClass('activeIcon');
            objType = $(this).siblings('input').val();
            $("label.error").hide();//隐藏validate验证错误信息
            $("#devices").val("").attr("style", "background:#FFFFFF");
            $("#brands").val("").attr("style", "background:#FFFFFF");
            $("#deviceTypeDiv").hide(); // 通讯类型选择隐藏
            //infoFastInput.DeviceChange(objType);
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
            switch (data) {
                case 0:
                    return "交通部JT/T808-2011(扩展)";
                    break;
                case 1:
                    return "交通部JT/T808-2013";
                    break;
                    case 11:
                    return "交通部JT/T808-2019";
                    break;
                case 2:
                    return "移为";
                    break;
                case 3:
                    return "天禾";
                    break;
                case 5:
                    return "BDTD-SM";
                    break;
                case 6:
                    return "KKS";
                    break;
                case 7:
                    return "";
                    break;
                case 8:
                    return "BSJ-A5";
                    break;
                case 9:
                    return "ASO";
                    break;
                case 10:
                    return "F3超长待机";
                    break;
            }
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
            $("#deviceType").val("1");
            $("#deviceTypeList").val("交通部JT/T808-2013");
            var dataList_input3 = {
                value: [
                    {"name": "交通部JT/T808-2013", "id": "1"},
                    {"name": "交通部JT/T808-2011(扩展)", "id": "0"},
                    {"name": "移为", "id": "2"},
                    {"name": "天禾", "id": "3"},
                    {"name": "BDTD-SM", "id": "5"},
                    {"name": "KKS", "id": "6"},
                    {"name": "BSJ-A5", "id": "8"},
                    {"name": "ASO", "id": "9"},
                    {"name": "F3超长待机", "id": "10"}
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
                $("#deviceType").val(keyword.id);
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
    };
    $(function () {
        var datas;
        infoFastInput.arrayExpand();
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'speedDevices') {
                infoFastInput.setInputDisabled();
            }
            if (id == 'oneDevices') {
                $("#oneDevicesName").val('');
            }
            console.log(11);
            setTimeout(function () {
                $('#' + id).focus();
            }, 20);
        });
        infoFastInput.init();
        //infoFastInput.loadcommuninput();
        /*	$("#deviceTypeList").on("click",function(){
                infoFastInput.loadcommuninput();
            })*/
        $(".groupZtree").on("click", infoFastInput.showMenu);
        $(".speedGroupZtree").on("click", infoFastInput.speedShowMenu);

        $('#submits').on("click", infoFastInput.doSubmits);
        $("#speedSubmits").bind("click", infoFastInput.speedDoSubmits);

        $("#brands").blur(function () {
            infoFastInput.blurFun("brands")
        });
        $("#brands").bind("paste", function () {
            infoFastInput.inputOnPaste("brands")
        });

        $("#devices").blur(function () {
            infoFastInput.blurFun("devices")
        });
        $("#devices").bind("paste", function () {
            infoFastInput.inputOnPaste("devices")
        });

        $("#speedBrands").blur(function () {
            infoFastInput.blurFun("speedBrands")
        });
        $("#speedBrands").bind("paste", function () {
            infoFastInput.inputOnPaste("speedBrands")
        });

        $("#speedSims").blur(function () {
            infoFastInput.blurFun("speedSims")
        });
        $("#speedSims").bind("paste", function () {
            infoFastInput.inputOnPaste("speedSims")
        });

        $("#deviceType").bind("change", function () {
            infoFastInput.check_deviceType()
        });
        $("#sims").blur(function () {
            infoFastInput.blurFun("sims")
        });
        $("#sims").bind("paste", function () {
            infoFastInput.inputOnPaste("sims")
        });
        // $("#speedDevices").bind("input oninput",infoFastInput.changeInpValDel);
        //监听浏览器窗口变化
        $(window).resize(infoFastInput.windowResize);
        $("#toggle-left").on("click", infoFastInput.toggleLeft);

        //极速录入终端为标识查询
        $("#searchDevices").bind('click', infoFastInput.speedSearchDevices);

        $("#speedDevices").bind('click', infoFastInput.inputClick);//键盘回车事件
        $(document).bind('keydown', infoFastInput.keydownEvent);
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
        $("#speedSims").bind('click', infoFastInput.searchList2);

        //车、人、物点击tab切换
        $("label.monitoringSelect").on("click", infoFastInput.chooseLabClick);

        $(".select-value,.btn-width-select").buttonGroupPullDown();
        $("#speedEntryLi").on("click", infoFastInput.speedEntryLiClickFn);
        $("#quickEntryLi").on("click", infoFastInput.quickEntryLiClickFn);
    })
})(window, $)