//@ sourceURL=infoinputEdit.js
(function ($, window) {
    var inputIdArray;
    var datas;
    var group = 1;
    var people = 1;
    var monitorType = $("#monitorType").val();
    var monitorId = $("#vehicleid").val();
    var brandName = $("#brandName").val();
    var deviceId = $("#deviceid").val();
    var deviceName = $("#editDevices").val();
    var simCardId = $("#simid").val();
    var simCardName = $("#editSims").val();
    var simCard = $("#simCard").val();
    var peopleId = $("#peopleIds").val();
    var peopleName = $("#peopleNames").val();
    var brandInput = $("#editBrands");
    var deviceInput = $("#editDevices");
    var simInput = $("#editSims");
    var employeeInput = $(".peopleSelect");
    var btnMonitor = $("#btnMonitor");
    var vehicleFlag = false; //监控对象是否新增标识
    var deviceFlag = false; //终端是否新增标识
    var simFlag = false; //终端手机号是否新增标识
    var terminalChangeFlag = false; //终端厂商改变
    var firstClickFlag = true; // 第一次查看分组树结构
    var triggerInputNum = 0;
    var simInputFlag = false;
    var brandsInputFlag = false;
    var devicesInputFlag = false;
    var vehicleInfoList = [];
    var peopleInfoList = [];
    var thingInfoList = [];
    var deviceInfoList = []; //终端信息集合
    var simCardInfoList = []; //终端手机号信息集合

    var msgEdit = {
        //初始化
        init: function () {
            if (monitorType === "1") { //人
                $('.monitoring-people').hide();
                $('.monitoring-thing').hide();
                // msgEdit.InitCallback1();
                msgEdit.deviceTypeMenu(monitorType);
            } else if (monitorType === "0") { //车
                $('.monitoring-thing').hide();
                // msgEdit.InitCallback();
                msgEdit.deviceTypeMenu(monitorType);
            } else if (monitorType === "2") { //物
                $('.monitoring-people').hide();
                $('.monitoring-thing').show();
                /*$('#people-work-area').show();*/
                // msgEdit.InitCallback();
                msgEdit.deviceTypeMenu(monitorType);
            }

            // 终端厂商下拉框、终端型号下拉框值组装
            msgEdit.getTerminalManufacturer();

            msgEdit.treeInit();
            msgEdit.getInfoData();
            var editForm = $("#editForm");
            editForm.parent().parent().css("width", "80%");
            laydate.render({
                elem: '#editBillingDate',
                theme: '#6dcff6'
            });
            laydate.render({
                elem: '#editDueDate',
                theme: '#6dcff6'
            });
        },
        getInfoData: function () {
            var urlList = '/clbs/m/infoconfig/infoFastInput/add';
            var parameterList = {
                "id": ""
            };
            json_ajax("POST", urlList, "json", true, parameterList, msgEdit.infoCallback);
        },
        infoCallback: function (data) {
            if (data.success) {
                datas = data.obj;
                /*vehicleInfoList = datas.vehicleInfoList;
                peopleInfoList = datas.peopleInfoList;
                thingInfoList = datas.thingInfoList;
                deviceInfoList = datas.deviceInfoList;
                simCardInfoList = datas.simCardInfoList;*/
                vehicleInfoList = datas.vehicleInfoList.map(function (item) {
                    var newItem = {
                        name: item.brand,
                        id: item.id,
                        type: item.plateColor
                    };
                    return newItem;
                });
                peopleInfoList = datas.peopleInfoList.map(function (item) {
                    var newItem = {
                        name: item.brand,
                        id: item.id,
                    };
                    return newItem;
                });
                thingInfoList = datas.thingInfoList.map(function (item) {
                    var newItem = {
                        name: item.brand,
                        id: item.id,
                    };
                    return newItem;
                });
                deviceInfoList = datas.deviceInfoList.map(function (item) {
                    var newItem = {
                        name: item.deviceNumber,
                        id: item.id,
                    };
                    return newItem;
                });
                simCardInfoList = datas.simCardInfoList.map(function (item) {
                    var newItem = {
                        name: item.simcardNumber,
                        id: item.id,
                    };
                    return newItem;
                })
            } else if (data.msg) {
                layer.msg(data.msg);
            }
            setTimeout(function () { //请求成功关闭加载动画
                layer.closeAll('loading');
            }, 200);
            if (monitorType === "1") { //人
                msgEdit.InitCallback1();
            } else { //车 物
                msgEdit.InitCallback();
            }
        },
        treeInit: function () {
            var setting = {
                async: {
                    url: "/clbs/m/infoconfig/infoinput/tree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    contentType: "application/json",
                    dataType: "json",
                    dataFilter: msgEdit.ajaxDataFilter
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
                    beforeClick: msgEdit.beforeClick,
                    onClick: msgEdit.onClick
                }
            };
            //分组树初始化
            $.fn.zTree.init($("#ztreeDemo"), setting, null);
            var setting2 = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    contentType: "application/json",
                    dataType: "json",
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
                    beforeClick: msgEdit.beforeClick,
                    onClick: msgEdit.onClick2,
                }
            };
            //组织树初始化
            $.fn.zTree.init($("#ztreeDemo2"), setting2, null);
        },
        initMonitor: function (url) {
            var list;
            if (monitorType == 0) { //车
                list = vehicleInfoList
            } else if (monitorType == 1) { //人
                list = peopleInfoList
            } else { //物
                list = thingInfoList
            }
            msgEdit.initDataList(brandInput, list, monitorId, msgEdit.brandsChange);
        },
        initDevice: function (url) {
            msgEdit.initDataList(deviceInput, deviceInfoList, deviceId, msgEdit.devicesChange);
        },
        initSimCard: function (url) {
            msgEdit.initDataList(simInput, simCardInfoList, simCardId, msgEdit.simsChange);
        },
        initEmployee: function (url) {
            msgEdit.initDataList(employeeInput, url, peopleId,
                function (keyword) {
                    msgEdit.checkDoubleChoosePro(keyword.id, "editProfessionals");
                },
                msgEdit.initProfessional);
        },
        initDataList: function (dataInput, urlString, id, callback, moreCallback) {
            if (id.indexOf('#') < 0) {
                dataInput.attr('data-id', id);
            }
            // 如果urlString是对象,直接取该值,不再发送请求
            var sendFlag = (typeof urlString) === 'object';
            var itemList = sendFlag ? urlString : [];
            var inputId = dataInput.attr('id');
            if (!sendFlag) {
                $.ajax({
                    type: "POST",
                    url: urlString,
                    async: false,
                    data: {
                        configId: $("#configId").val(),
                        monitorType: monitorType
                    },
                    dataType: "json",
                    success: function (data) {
                        itemList = data.obj;
                    }
                });
            }
            var arr = ['editBrands', 'editSims', 'editDevices', 'editProfessionals'];
            if (arr.indexOf(inputId) !== -1 || inputId.indexOf('editProfessionals') !== -1) {
                var container = '';
                var selectedValue = '';
                var searchUrl = null;
                switch (inputId) {
                    case 'editBrands':
                        container = "#editBrandsContainer";
                        selectedValue = $('#vehicleid').attr('value');
                        searchUrl = itemList.length >= MAX_MENU_LENGTH ? '/clbs/m/infoconfig/infoFastInput/fuzzyMonitor?monitorType=' + monitorType + '' : null;
                        break;
                    case 'editSims':
                        container = "#editSimsContainer";
                        selectedValue = $('#simid').attr('value');
                        searchUrl = itemList.length >= MAX_MENU_LENGTH ? '/clbs/m/infoconfig/infoFastInput/fuzzySimCard' : null;
                        break;
                    case 'editDevices':
                        container = "#editDevicesContainer";
                        selectedValue = $('#deviceid').attr('value');
                        searchUrl = itemList.length >= MAX_MENU_LENGTH ? '/clbs/m/infoconfig/infoFastInput/fuzzyDevice' : null;
                        break;
                    default: // 从业人员
                        var index = inputId.split('editProfessionals')[1];
                        container = "#editProfessionalsContainer" + index;
                        var arrIndex = $(container).index('#people-work-area .i-dropdown-container');
                        var perArr = $('#peopleIds').attr('value').split('#');
                        selectedValue = perArr[arrIndex] || '';
                        searchUrl = itemList.length >= MAX_MENU_LENGTH ? '/clbs/m/infoconfig/infoinput/getProfessionalSelect' : null;
                        itemList.map((it, index) => {
                            itemList[index] = {
                                ...it,
                                name: it.identity ? it.name + "(" + it.identity + ")" : it.name
                            }
                        })
                        break;
                }
                $(container).dropdown({
                    data: itemList,
                    pageCount: 50,
                    listItemHeight: 31,
                    selectedValue: selectedValue,
                    searchUrl: searchUrl,
                    onDataRequestSuccess: function (e, result) {},
                    onSetSelectValue: function (e, keyword, data) {
                        if (callback) {
                            dataInput.closest('.form-group').find('.dropdown-menu').hide();
                            callback(keyword)
                        }
                        //限制输入
                        msgEdit.showHideValueCase(0, dataInput);
                        msgEdit.hideErrorMsg();
                    },
                    onUnsetSelectValue: function () {
                        //放开输入
                        msgEdit.showHideValueCase(1, dataInput);
                    }
                });
            } else {
                dataInput.bsSuggest({
                    idField: "id",
                    keyField: "name",
                    /*indexId: 0,  //data.value 的第几个数据，作为input输入框的内容
                    indexKey: 1, //data.value 的第几个数据，作为input输入框的内容*/
                    data: {
                        value: itemList
                    },
                    effectiveFields: ["name"]
                }).on('onDataRequestSuccess', function (e, result) {}).on("click", function () {}).on('onSetSelectValue', function (e, keyword, data) {
                    if (callback) {
                        dataInput.closest('.form-group').find('.dropdown-menu').hide();
                        callback(keyword)
                    }
                    //限制输入
                    msgEdit.showHideValueCase(0, dataInput);
                    msgEdit.hideErrorMsg();
                }).on('onUnsetSelectValue', function () {
                    //放开输入
                    msgEdit.showHideValueCase(1, dataInput);
                });
            }
            dataInput.next().find('button').removeClass('disabled loading-state-button').find('i').attr("class", 'caret');
            if (moreCallback) {
                moreCallback()
            }
        },
        //显示或隐藏输入框(4.2.4版本调整新增逻辑,用户手动输入的监控对象名、sim卡号、终端号不再是新增，而是修改原有的信息)
        showHideValueCase: function (type, dataInput) {
            var dataInputType = dataInput.selector;
            if (type == 0) { //限制输入
                if ("#editBrands" == dataInputType) { //车辆限制
                    $(".vehicleList").attr("readonly", true);
                    $("#vehicleTypeDiv").css("display", "block");
                    $("#plateColorDiv").css("display", "block");
                    $("#carGroupName").css("background-color", "").addClass('readBack');
                    $("#vehicleGroupDiv").css("display", "block");
                    $(".thingList").attr("readonly", true);
                    $("#thingTypeDiv").css("display", "block");
                    $("#thingType").css("background-color", "");
                    vehicleFlag = false;
                }
                if ("#editDevices" == dataInputType) { //终端限制
                    $(".deviceList").attr("readonly", true);
                    $("#deviceTypeDiv").css("display", "block");
                    $("#terminalManufacturerDiv").css("display", "block");
                    $("#editTerminalType").prop("disabled", true);
                    $("#editTerminalButton").prop("disabled", true);
                    $("#deviceGroupName").css("background-color", "").addClass('readBack');
                    $("#deviceGroupDiv").css("display", "block");
                    deviceFlag = false;
                }
                if ("#editSims" == dataInputType) { //终端手机号限制
                    $(".simsList").attr("readonly", true);
                    $("#simParentGroupName").css("background-color", "").addClass('readBack');
                    $("#simGroupDiv").css("display", "block");
                    $("#operatorTypeDiv").css("display", "block");
                    simFlag = false;
                }
            } else if (type == 1) { //放开输入
                if ("#editBrands" == dataInputType) { //车辆放开
                    if (!vehicleFlag) {
                        msgEdit.brandsChange({
                            id: monitorId
                        });
                    }
                    vehicleFlag = true;
                    brandsInputFlag = true;
                    /* $(".vehicleList").removeAttr("readonly");
                     $("#vehicleTypeDiv").css("display", "none");
                     $("#plateColorDiv").css("display", "none");
                     $("#carGroupName").css("background-color", "#fafafa").removeClass('readBack');
                     $("#vehicleGroupDiv").css("display", "none");
                     $(".thingList").removeAttr("readonly");
                     $("#thingTypeDiv").css("display", "none");
                     $("#thingType").css("background-color", "#fafafa");*/
                }
                if ("#editDevices" == dataInputType) { //终端放开
                    if (!deviceFlag) {
                        msgEdit.devicesChange({
                            id: deviceId,
                            name: deviceName
                        });
                    }
                    deviceFlag = true;
                    devicesInputFlag = true;
                    /* $(".deviceList").removeAttr("readonly");
                     $("#deviceTypeDiv").css("display", "none");
                     $("#terminalManufacturerDiv").css("display", "none");
                     $("#editTerminalType").removeAttr("disabled");
                     $("#editTerminalButton").removeAttr("disabled");
                     $("#deviceGroupName").css("background-color", "#fafafa").removeClass('readBack');
                     $("#deviceGroupDiv").css("display", "none");*/
                }
                if ("#editSims" == dataInputType) { //终端手机号放开
                    if (!simFlag) {
                        msgEdit.simsChange({
                            id: simCardId,
                            name: simCardName
                        });
                    }
                    simFlag = true;
                    simInputFlag = true;
                    /*$(".simsList").removeAttr("readonly");
                    $("#simParentGroupName").css("background-color", "#fafafa");
                    $("#simGroupDiv").css("display", "none").removeClass('readBack');
                    $("#operatorTypeDiv").css("display", "none");*/
                }
            }
        },

        getTerminalManufacturer: function () {
            var url = "/clbs/m/basicinfo/equipment/device/TerminalManufacturer";
            json_ajax("GET", url, "json", false, null, msgEdit.terminalManufacturerCallBack);
        },

        terminalManufacturerCallBack: function (data) {
            var result = data.obj.result;
            var str = "";
            var terminalManufacturerName = $("#terminalManufacturerName").val();
            for (var i = 0; i < result.length; i++) {
                if (terminalManufacturerName == result[i]) {
                    str += '<option selected="selected" value="' + result[i] + '">' + html2Escape(result[i]) + '</option>'
                } else {
                    str += '<option  value="' + result[i] + '">' + html2Escape(result[i]) + '</option>'
                }
            }
            $("#terminalManufacturer").html(str);
            msgEdit.getTerminalType($("#terminalManufacturer").val());
        },

        getTerminalType: function (name) {
            var url = "/clbs/m/basicinfo/equipment/device/getTerminalTypeByName";
            json_ajax("POST", url, "json", false, {
                'name': name
            }, msgEdit.getTerminalTypeCallback);
        },

        getTerminalTypeCallback: function (data) {
            var result = data.obj.result;
            if (terminalChangeFlag) {
                $("#editTerminalType").val('');
                $("#editTerminalTypeId").val('');
                $("#editTerminalTypeName").val('');
            }
            var infoList = {
                'value': []
            };
            if (result != null && result != '') {
                for (var i = 0; i < result.length; i++) {
                    var obj = {
                        'id': result[i].id,
                        'name': html2Escape(result[i].terminalType)
                    };
                    infoList.value.push(obj);
                }
            }
            $("#editTerminalType").bsSuggest("destroy"); // 销毁事件
            $('#editTerminalType').bsSuggest({
                indexId: 1, //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: infoList
            }).on('onDataRequestSuccess', function (e, result) {}).on('onSetSelectValue', function (e, keyword, data) {
                $("#editTerminalTypeName").val(keyword.key);
                $("#editTerminalTypeId").val(keyword.id);
                $('#editTerminalType').siblings('label#error_label').hide();
            }).on('onUnsetSelectValue', function () {
                $("#editTerminalTypeId").val('');
                $("#editTerminalTypeName").val('');
            }).on('input propertychange', function () {
                $("#editTerminalTypeId").val('');
                $("#editTerminalTypeName").val('');
                msgEdit.showErrorMsg("请选择终端型号", "editTerminalType");
            });
            if (!deviceFlag) {
                $("#editTerminalType").prop('disabled', true);
            }
        },

        //查询车辆类型回调方法
        vehicleTypeCallback: function () {
            json_ajax("POST", "/clbs/m/basicinfo/monitoring/vehicle/addList", "json", false, {}, function (data) {
                var dataLength = data.obj.VehicleTypeList.length;
                for (var i = 0; i < dataLength; i++) {
                    $("#editVehicleType").append("<option value=" + data.obj.VehicleTypeList[i].id + ">" + data.obj.VehicleTypeList[i].vehicleType + "</option>")
                }
            });

            var fvt = $("#firstVehicleType").val();
            $("#editVehicleType").val(fvt);
        },
        //根据不同的监控对象类型加载不同的协议类型和功能类型
        deviceTypeMenu: function (type) {
            var url = '/clbs/m/connectionparamsset/protocolList';
            var param = {
                "type": 808
            };
            json_ajax("POST", url, "json", false, param, function (data) {
                var data = data.obj;
                agreementType = data;
                for (var i = 0; i < data.length; i++) {
                    $('#deviceType').append(
                        "<option value='" + data[i].protocolCode + "'>" + data[i].protocolName + "</option>"
                    );
                }
            });
            // $("#functionalType").append(
            //     "<option value='1'>简易型车机</option>" +
            //     "<option value='2'>行车记录仪</option>" +
            //     "<option value='4'>手咪设备</option>" +
            //     "<option value='3'>对讲设备</option>" +
            //     "<option value='5'>超长待机设备</option>" +
            //     "<option value='6'>定位终端</option>"
            // );
            //装入值
            var dt = $("#firstDeviceType").val();
            // var ft = $("#firstFunctionalType").val();
            $("#deviceType").val(dt);
            // $("#functionalType").val(ft);
        },
        processData: function (data) {
            var dataList = data.obj;
            var itemList = {
                value: []
            };
            for (var i = 0; i < dataList.length; i++) {
                itemList.value.push({
                    id: dataList[i].id,
                    name: dataList[i].name
                });
            }
            return itemList;
        },
        InitCallback: function () {
            //监控对象
            msgEdit.initMonitor("/clbs/m/infoconfig/infoinput/getMonitorSelect");
            //终端编号
            msgEdit.initDevice("/clbs/m/infoconfig/infoinput/getVDeviceSelect");
            //终端手机号
            msgEdit.initSimCard("/clbs/m/infoconfig/infoinput/getSimcardSelect");
            //从业人员
            msgEdit.initEmployee("/clbs/m/infoconfig/infoinput/getProfessionalSelect");
            //组装车辆类型
            msgEdit.vehicleTypeCallback();
        },
        InitCallback1: function () {
            //监控对象
            msgEdit.initMonitor("/clbs/m/infoconfig/infoinput/getMonitorSelect");
            //终端编号
            msgEdit.initDevice("/clbs/m/infoconfig/infoinput/getVDeviceSelect");
            //终端手机号
            msgEdit.initSimCard("/clbs/m/infoconfig/infoinput/getSimcardSelect");
        },
        //显示错误提示信息
        showErrorMsg: function (msg, inputId) {
            if ($("#error_label").is(":hidden")) {
                $("#error_label").text(msg);
                $("#error_label").insertAfter($("#" + inputId));
                $("#error_label").show();
            } else {
                $("#error_label").is(":hidden");
            }
        },
        hideErrorMsg: function () {
            $("#error_label").hide();
        },
        brandsChange: function (keyword) {
            datas = keyword.id;
            $("#editBrands").attr("data-id", keyword.id);
            if (monitorType == 0) {
                json_ajax("POST", "/clbs/m/infoconfig/infoinput/getVehicleInfoById", "json", true, {
                        "vehicleId": datas
                    },
                    msgEdit.brandsChangeCallback);
            } else if (monitorType == 1) {
                json_ajax("POST", "/clbs/m/infoconfig/infoinput/getPeopleInfoById", "json", true, {
                        "peopleId": datas
                    },
                    msgEdit.brandsPeopleChangeCallback);
            } else {
                json_ajax("POST", "/clbs/m/infoconfig/infoinput/getThingInfoById", "json", true, {
                        "thingId": datas
                    },
                    msgEdit.brandsThingChangeCallback);
            }

        },
        brandsChangeCallback: function (data) {
            if (data.success) {
                if (data !== null && data.obj !== null && data.obj.vehicleInfo !== null) {
                    $("#carGroupName").val(data.obj.vehicleInfo.groupName);
                    $("#plateColor").val(data.obj.vehicleInfo.plateColor);
                    $("#editVehicleType").val(data.obj.vehicleInfo.vehicleType);
                } else if (data.msg) {
                    layer.msg(data.msg);
                }
            }
        },
        brandsPeopleChangeCallback: function (data) {
            if (data.success) {
                if (data !== null && data.obj !== null && data.obj.vehicleInfo !== null) {
                    $("#carGroupName").val(data.obj.peopleInfo.groupName);
                } else if (data.msg) {
                    layer.msg(data.msg);
                }
            }
        },
        brandsThingChangeCallback: function (data) {
            if (data.success) {
                if (data !== null && data.obj !== null && data.obj.thingInfo !== null) {
                    $("#carGroupName").val(data.obj.thingInfo.groupName);
                    $("#thingNameEdit").val(data.obj.thingInfo.name);
                    $("#thingType").val(data.obj.thingInfo.type);
                } else if (data.msg) {
                    layer.msg(data.msg);
                }
            }
        },
        devicesChange: function (keyword) {
            datas = keyword.name;
            $("#deviceid").val(keyword.id);
            $("#editDevices").attr("data-id", keyword.id);
            json_ajax("POST", "/clbs/m/infoconfig/infoinput/getDeviceInfoByDeviceNumber", "json", true, {
                "deviceNumber": datas
            }, msgEdit.devicesChangeCallback);
        },
        devicesChangeCallback: function (data) {
            if (data.success) {
                if (data != null && data.obj != null && data.obj.deviceInfo != null) {
                    $("#deviceGroupName").val(data.obj.deviceInfo.groupName);
                    $("#deviceType").val(data.obj.deviceInfo.deviceType);
                    $("#terminalManufacturer").val(data.obj.deviceInfo.terminalManufacturer);
                    // $("#functionalType").val(data.obj.deviceInfo.functionalType);
                    $("#manuFacturer").val(data.obj.deviceInfo.manuFacturer);
                    terminalChangeFlag = false;
                    $("#editTerminalType").val(data.obj.deviceInfo.terminalType);
                    $("#editTerminalTypeId").val(data.obj.deviceInfo.terminalTypeId);
                    $("#editTerminalTypeName").val(data.obj.deviceInfo.terminalType);
                    msgEdit.getTerminalType(data.obj.deviceInfo.terminalManufacturer);
                }
            } else if (data.msg) {
                layer.msg(data.msg);
            }
        },
        simsChange: function (keyword) {
            datas = keyword.name;
            $("#simid").val(keyword.id);
            $("#editSims").attr("data-id", keyword.id);
            json_ajax("POST", "/clbs/m/infoconfig/infoinput/getSimcardInfoBySimcardNumber", "json", true, {
                simcardNumber: datas
            }, msgEdit.simsChangeCallback);
        },
        simsChangeCallback: function (data) {
            if (data.success) {
                if (data.obj.simcardInfo) {
                    $("#iccidSim").val(data.obj.simcardInfo.iCCID);
                    $("#simParentGroupName").val(data.obj.simcardInfo.groupName);
                    $("#operator").val(data.obj.simcardInfo.operator);
                    $("#simFlow").val(data.obj.simcardInfo.simFlow);
                    $("#openCardTime").val(data.obj.simcardInfo.openCardTime);
                    $("#realSimCard").val(data.obj.simcardInfo.realId);
                }
            } else if (data.msg) {
                layer.msg(data.msg);
            }
        },
        // 校验是否重复选择从业人员
        checkDoubleChoosePro: function (proId, selectId) {
            var flag = true;
            $('#' + selectId).attr('data-id', proId);
            var selects = $("[name='professionalsId__']");
            if (selects !== null && selects !== undefined && selects !== 'undefined' && selects.length > 0) {
                selects.each(function (i) {
                    if (proId === $(this).attr("data-id") && selectId !== $(this).attr("id")) {
                        layer.msg(repeateChooseProfession);
                        $("#" + selectId).val("");
                        flag = false;
                    }
                });
            }
            return flag;
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            if (responseData) {
                for (var i = 0, responseDataLength = responseData.length; i < responseDataLength; i++) {
                    responseData[i].open = true;
                }
            }
            return responseData;
        },
        beforeClick: function (treeId, treeNode) {
            return treeNode;
        },
        onClick: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("ztreeDemo"),
                nodes = zTree.getSelectedNodes(),
                name = "",
                id = "";
            nodes.sort(function compare(a, b) {
                return a.id - b.id;
            });
            var type = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type === "assignment") { // 选择的是分组，才组装值
                    type = nodes[i].type;
                    name += nodes[i].name;
                    id += nodes[i].id + ",";
                }
            }
            if (id.length > 0) {
                id = id.substring(0, id.length - 1);
            }
            if (type === "assignment" && msgEdit.checkDoubleChooseAssignment(id) &&
                msgEdit.checkMaxVehicleCountOfAssignment(id, name)) { // 点击的是分组，才往下执行
                var cityObj = $("#" + inputIdArray);
                cityObj.attr("value", id);
                cityObj.val(name);
                cityObj.siblings("input:last").val(id);
                $("#zTreeContent").hide();
                msgEdit.hideErrorMsg();
            }
        },
        onClick2: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("ztreeDemo2"),
                nodes = zTree.getSelectedNodes(),
                v = "";
            var t = "";
            nodes.sort(function compare(a, b) {
                return a.id - b.id;
            });
            for (var i = 0, l = nodes.length; i < l; i++) {
                t += nodes[i].name;
                v += nodes[i].uuid + ",";
            }
            if (v.length > 0) v = v.substring(0, v.length - 1);
            var cityObj = $("#" + inputIdArray);
            cityObj.attr("value", v);
            cityObj.val(t);
            cityObj.siblings().val(v);
            $("#groupTree").hide();
        },
        // 校验当前分组下的车辆数是否已经达到最大值（最大值目前设定为：100台）
        checkMaxVehicleCountOfAssignment: function (assignmentId, assignmentName) {
            var b = true;
            $.ajax({
                type: 'POST',
                url: '/clbs/m/infoconfig/infoinput/checkMaxVehicleCount',
                data: {
                    "assignmentId": assignmentId,
                    "assignmentName": assignmentName
                },
                dataType: 'json',
                async: false,
                success: function (data) {
                    b = data.obj.success;
                    if (!data.obj.success) {
                        layer.msg("【分组:" + assignmentName + "】" + assignmentMaxCarNum, {
                            time: 1500,
                        });
                    }
                },
                error: function () {
                    layer.msg(systemError, {
                        time: 1500,
                    });
                    b = false;
                }
            });
            return b;
        },
        // 校验是否重复选择
        checkDoubleChooseAssignment: function (curValue) {
            var model = $("#edit-area").children("div");
            var edit = $("#edit-list").children("div");
            var added = $("#edit-add-area").children("div");
            var gid = '';
            var flag = true;
            if (model != null && model != undefined && model != 'undefined' && model.length > 0) {
                if (curValue == model.children("input:last-child").val()) {
                    layer.msg(repeateChooseAssignment);
                    flag = false;
                }
            }
            if (edit != null && edit != undefined && edit != 'undefined' && edit.length > 0) {
                edit.each(function (i) {
                    if (curValue == $(this).children("div").children("div").children("input:last-child").val()) {
                        layer.msg(repeateChooseAssignment);
                        flag = false;
                    }
                });
            }
            if (added != null && added != undefined && added != 'undefined' && added.length > 0) {
                added.each(function (i) {
                    if (curValue == $(this).children("div").children("input:last-child").val()) {
                        layer.msg(repeateChooseAssignment);
                        flag = false;
                    }
                });
            }
            return flag;
        },
        showMenu: function (e, zTreeId) {
            var inputID = e.id;
            if ($(e).hasClass('form-control-feedback')) {
                inputID = $(e).siblings('input.groupCitySel').attr('id');
            }
            inputIdArray = inputID;
            if ($(zTreeId).is(":hidden")) {
                var type = 'assignment';
                if (zTreeId == '#groupTree') {
                    type = 'group';
                }
                var treeUlId = $(zTreeId).find('ul.ztree').attr('id');
                var zTree = $.fn.zTree.getZTreeObj(treeUlId);
                if (zTree) {
                    search_ztree(treeUlId, inputID, type, '');
                }

                var width = $(e).parent().width();
                $(zTreeId).css("width", width + "px");
                $(window).resize(function () {
                    var width = $(e).parent().width();
                    $(zTreeId).css("width", width + "px");
                })
                $(zTreeId).insertAfter($("#" + inputID));
                $(zTreeId).show();
            } else {
                $(zTreeId).hide();
            }
            $("body").bind("mousedown", msgEdit.onBodyDown);
        },
        hideMenu: function (zTreeId) {
            $(zTreeId).fadeOut("fast");
            // $("body").unbind("mousedown", msgEdit.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "zTreeContent" || $(
                    event.target).parents("#zTreeContent").length > 0)) {
                msgEdit.hideMenu("#zTreeContent");
            }
            if (!(event.target.id == "menuBtn" || event.target.id == "groupTree" || $(
                    event.target).parents("#groupTree").length > 0)) {
                msgEdit.hideMenu("#groupTree");
            }
        },
        editAdd: function () {
            $("#zTreeContent").hide().appendTo($("#zTreeArea"));
            var obj = $("#edit-area").clone(true);
            var html = obj.html() + '<button type="button" class="btn btn-danger edit_Delete"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button>';
            obj.attr("id", "edit-area" + group).attr("class", "form-group edit-area-marbottom").html(html);
            obj.find(".delIcon").remove();
            obj.children("div").children("input").attr("id", "zTreeCitySel" + group).val("");
            obj.children("div").children("input").next().attr("id", "assignmentId" + group).attr("value", "");
            obj.appendTo($("#edit-add-area"));
            group++;
            $(".zTreeCitySel").unbind("click").on("click", function () {
                msgEdit.showMenu(this, "#zTreeContent")
            });
            var scrollOrNot = function () {
                if ($('#edit-object-area .edit-area-marbottom').length > 4) {
                    $('#edit-object-area').css('overflow-y', 'scroll')
                } else {
                    $('#edit-object-area').css('overflow-y', 'visible')
                }
            }
            scrollOrNot();
            $(".edit_Delete").click(function () {
                $("#zTreeContent").hide().appendTo($("#zTreeArea"));
                $(this).parent().remove();
                scrollOrNot();
            });

            $('input').inputClear().on('onClearEvent', function (e, data) {
                var curInput = $(e.target);
                if (curInput.hasClass('zTreeCitySel')) {
                    curInput.attr('value', '');
                    curInput.siblings('label.error').hide();
                    curInput.siblings('.assignmentId').val('');
                    search_ztree('ztreeDemo', id, 'assignment', '');
                }
                if (curInput.hasClass('groupCitySel')) {
                    curInput.siblings('label.error').hide();
                    curInput.siblings('.editGroupId').val('0');
                    search_ztree('ztreeDemo2', id, 'group', '');
                }
            });
            $(".zTreeCitySel").on('input propertychange', function (e) {
                var treeObj = $.fn.zTree.getZTreeObj("ztreeDemo");
                treeObj.checkAllNodes(false);
                $(this).siblings(".assignmentId").val('');
                search_ztree('ztreeDemo', e.target.id, 'assignment');
            });
        },
        peopleAdd: function () {
            var obj = $("#people-area").clone(true);
            var html = obj.html() + '<button type="button" class="btn btn-danger editpeople_Delete"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button>';
            obj.attr("id", "people-area" + people).html(html);
            obj.attr("class", "form-group");
            var bs = obj.find('.peopleSelect');
            bs.attr("id", "editProfessionals" + people);
            var container = obj.find('.i-dropdown-container');
            container.attr("id", "editProfessionalsContainer" + people);
            bs.val("");
            obj.appendTo($("#editpeople-add-area"));
            var tmpPeople = people;
            msgEdit.initDataList(bs, '/clbs/m/infoconfig/infoinput/getProfessionalSelect', '',
                function (keyword) {
                    msgEdit.checkDoubleChoosePro(keyword.id, "editProfessionals" + tmpPeople);
                });
            people++;
            var scrollOrNot = function () {
                if ($('#editpeople-add-area>div').length > 3) {
                    $('#people-object-area').css('overflow-y', 'scroll')
                } else {
                    $('#people-object-area').css('overflow-y', 'visible')
                }
            }
            scrollOrNot();
            $(".editpeople_Delete").click(function () {
                $(this).parent().remove();
                scrollOrNot()
            });
        },
        // 初始化分组信息
        initGroup: function () {
            var groupid = $("#groupID").val();
            var groupName = $("#groupName").val();
            var groupids = groupid.split("#");
            var groupNames = groupName.split("#");
            $("#edit-area").children("div").children("input").attr("value", groupNames.length > 0 ? groupNames[0] : "");
            $("#edit-area").children("div").children("input").next().attr("value", groupids.length > 0 ? groupids[0] : "");
            var editHtml = "";
            if (groupNames != null && groupNames.length > 1) {
                for (var i = 1; i < groupNames.length; i++) {
                    editHtml += '<div class="form-group edit-area-marbottom" style="margin-bottom: 0px;"><div id="edit-list-area_' + i + '"><label class="col-sm-3 col-md-3 control-label">分组：</label><div class="col-sm-7 col-md-6 form-group assignment-div"><input type="text" name="groupName" value="' + groupNames[i] + '" class="form-control groupName zTreeCitySel" style="background-color: #fafafa; cursor: pointer;" id="zTree-list_' + i + '" placeholder="请选择组织" /><input id="assignmentId_' + i + '" value="' + groupids[i] + '" type="hidden" /></div></div><button type="button" class="btn btn-danger edit_Delete_default btnPaddLeft"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button></div>'
                }
            }
            var scrollOrNot = function () {
                if ($('#edit-object-area .edit-area-marbottom').length > 4) {
                    $('#edit-object-area').css('overflow-y', 'scroll')
                } else {
                    $('#edit-object-area').css('overflow-y', 'visible')
                }
            }

            $("#edit-list").html(editHtml);
            scrollOrNot()
            $(".groupName").unbind("click").on("click", function () {
                msgEdit.showMenu(this, "#zTreeContent")
            });
            $(".edit_Delete_default").click(function () {
                $("#zTreeContent").appendTo($("#zTreeArea"));
                $(this).parent().remove();
                scrollOrNot()
            });

            $('input').inputClear().on('onClearEvent', function (e, data) {
                var curInput = $(e.target);
                if (curInput.hasClass('zTreeCitySel')) {
                    curInput.attr('value', '');
                    curInput.siblings('label.error').hide();
                    curInput.siblings('.assignmentId').val('');
                    search_ztree('ztreeDemo', id, 'assignment', '');
                }
                if (curInput.hasClass('groupCitySel')) {
                    curInput.siblings('label.error').hide();
                    curInput.siblings('.editGroupId').val('0');
                    search_ztree('ztreeDemo2', id, 'group', '');
                }
            });
            $(".zTreeCitySel").on('input propertychange', function (e) {
                var treeObj = $.fn.zTree.getZTreeObj("ztreeDemo");
                treeObj.checkAllNodes(false);
                $(this).siblings(".assignmentId").val('');
                search_ztree('ztreeDemo', e.target.id, 'assignment');
            });
        },
        // 初始化从业人员信息
        initProfessional: function () {
            var peopleIds = peopleId.length > 0 ? peopleId.split("#") : null;
            var peopleArea = $("#people-area");
            var obj;
            var html;
            if (null !== peopleIds && peopleIds.length > 0) {
                for (var i = 1, peopleIdsLength = peopleIds.length; i < peopleIdsLength; i++) {
                    obj = peopleArea.clone(true);
                    html = obj.html() + '<button type="button" class="btn btn-danger editpeople_Delete"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button>';
                    obj.attr("id", "people-area-" + i).html(html);
                    obj.attr("class", "form-group");
                    var container = obj.find('.i-dropdown-container');
                    container.attr("id", "editProfessionalsContainer" + people);

                    var bs = obj.find('.peopleSelect');
                    bs.attr("id", "editProfessionals" + people);
                    bs.attr('data-id', peopleIds[i]);
                    obj.appendTo($("#editpeople-add-area"));
                    var tmpPeople = people;
                    msgEdit.initDataList(bs, '/clbs/m/infoconfig/infoinput/getProfessionalSelect', peopleId,
                        function (keyword) {
                            msgEdit.checkDoubleChoosePro(keyword.id, "editProfessionals" + tmpPeople);
                        });
                    people++;

                    var scrollOrNot = function () {
                        if ($('#editpeople-add-area>div').length > 3) {
                            $('#people-object-area').css('overflow-y', 'scroll')
                        } else {
                            $('#people-object-area').css('overflow-y', 'visible')
                        }
                    }
                    scrollOrNot()
                    $(".editpeople_Delete").click(function () {
                        $(this).parent().remove();
                        scrollOrNot()
                    });
                }
            }

        },
        validates: function () {
            return $("#editForm").validate({
                rules: {
                    carGroupName: {
                        required: true
                    },
                    carGroupId: {
                        required: true
                    },
                    deviceGroupId: {
                        required: true
                    },
                    simParentGroupId: {
                        required: true
                    },
                    vehiclePassword: {
                        isABCNumber: true,
                    },
                    plateColor: {
                        required: true
                    },
                    editVehicleType: {
                        required: true
                    },
                    deviceGroupName: {
                        required: true
                    },
                    deviceType: {
                        required: true
                    },
                    terminalManufacturer: {
                        required: true
                    },
                    editTerminalType: {
                        required: true
                    },
                    iccidSim: {
                        checkICCID: true
                    },
                    simParentGroupName: {
                        required: true
                    },
                    operator: {
                        required: false,
                        maxlength: 50
                    },
                    durDateStr: {
                        compareDate: "#editBillingDate"
                    },
                    realId: {
                        digits: true,
                        minlength: 7,
                        maxlength: 20
                    },
                },
                messages: {
                    carGroupName: {
                        required: '所属企业不能为空'
                    },
                    carGroupId: {
                        required: '请选择所属企业'
                    },
                    deviceGroupId: {
                        required: '请选择所属企业'
                    },
                    vehiclePassword: {
                        isABCNumber: '请输入字母和数字'
                    },
                    simParentGroupId: {
                        required: '请选择所属企业'
                    },
                    plateColor: {
                        required: vehiclePlateColorNull
                    },
                    editVehicleType: {
                        required: vehicleTypeNull
                    },
                    deviceGroupName: {
                        required: '所属企业不能为空'
                    },
                    deviceType: {
                        required: deviceTypeNull
                    },
                    terminalManufacturer: {
                        required: "请选择终端厂商"
                    },
                    editTerminalType: {
                        required: "请选择终端型号"
                    },
                    iccidSim: {},
                    simParentGroupName: {
                        required: '所属企业不能为空'
                    },
                    operator: {
                        maxlength: publicSize50
                    },
                    durDateStr: {
                        compareDate: dueDateCompareBillingDate
                    },
                    realId: {
                        digits: '请输入数字，范围：7~20位',
                        minlength: '请输入数字，范围：7~20位',
                        maxlength: '请输入数字，范围：7~20位'
                    }
                }
            }).form();
        },
        doSubmits: function () {
            if (vehicleFlag) { //判断是否新增车辆
                if (monitorType == 0) { //车
                    if (!msgEdit.check_brand()) {
                        return;
                    }
                } else if (monitorType == 1) { //人
                    if (!msgEdit.check_people_number()) {
                        return;
                    }
                } else if (monitorType == 2) { //物
                    if (!msgEdit.check_thing_number()) {
                        return;
                    }
                }
            } else { //没有新增，校验是否为空
                if (monitorType == 0) { //车
                    //wjk
                    if (!msgEdit.checkIsEmpty("editBrands", publicMonitorNull)) {
                        return;
                    }
                } else if (monitorType == 1) { //人
                    if (!msgEdit.checkIsEmpty("editBrands", publicMonitorNull)) {
                        return;
                    }
                } else if (monitorType == 2) { //物
                    if (!msgEdit.checkIsEmpty("editBrands", publicMonitorNull)) {
                        return;
                    }
                }
            }

            if (deviceFlag) { //判断是否新增终端
                if (!msgEdit.check_device()) {
                    return;
                }
            } else { //没有新增，校验是否为空
                if (!msgEdit.checkIsEmpty("editDevices", deviceNumberSelect)) {
                    return;
                }
            }

            if (simFlag) { //判断是否新增终端手机号
                if (!msgEdit.check_sim()) {
                    return;
                }
            } else { //没有新增，校验是否为空
                if (!msgEdit.checkIsEmpty("editSims", simNumberNull)) {
                    return;
                }
            }
            if (msgEdit.validates()) {
                var assignmentIdList = $('.assignmentId');
                for (var i = 0, len = assignmentIdList.length; i < len; i++) {
                    if ($(assignmentIdList[i]).val() == '') {
                        msgEdit.showErrorMsg('请选择分组', $(assignmentIdList[i]).siblings('.zTreeCitySel').attr('id'));
                        return;
                    }
                }

                $("#simpleQueryParam").val("");
                msgEdit.setAssignmentIds();
                if ($("#groupID").val() == '') {
                    msgEdit.showErrorMsg("请选择分组", "zTreeCitySel");
                    return;
                }
                if ($("#editTerminalTypeName").val() == null || $("#editTerminalTypeName").val() == '') {
                    msgEdit.showErrorMsg("请选择终端型号", "editTerminalType");
                    return;
                }
                //                if ($("#editBrands").attr('data-id') == '') {
                //                	layer.msg("请选择或新增监控对象");
                //                    return;
                //                }
                //                if ($("#editDevices").attr('data-id') == '') {
                //                	layer.msg("请选择或新增终端");
                //                    return;
                //                }
                //                if ($("#editSims").attr('data-id') == '') {
                //                	layer.msg("请选择或新增终端手机号");
                //                    return;
                //                }
                layer.confirm('您确定本次操作吗？', {
                    btn: ['确定', '取消'] //按钮
                }, function () {
                    //组装检查是否新增标识值
                    /*if (vehicleFlag || deviceFlag || simFlag) { //新增
                        $("#checkEdit").val(0);
                        //名称为值
                        $("#hideBrands").val($("#editBrands").val());
                        $("#hideDevices").val($("#editDevices").val());
                        $("#hideSims").val($("#editSims").val());
                    } else { //修改*/
                    $("#isEditBrands").val('').prop('disabled', true);
                    $("#isEditDevices").val('').prop('disabled', true);
                    $("#isEditSims").val('').prop('disabled', true);
                    if (vehicleFlag) {
                        $("#editBrands").attr('data-id', $("#vehicleid").val());
                        $("#isEditBrands").val($("#editBrands").val()).prop('disabled', false);
                    }
                    if (deviceFlag) {
                        $("#isEditDevices").val($("#editDevices").val()).prop('disabled', false);
                    }
                    if (simFlag) {
                        $("#isEditSims").val($("#editSims").val()).prop('disabled', false);
                    }


                    $("#checkEdit").val(1);
                    //id为值
                    $("#hideBrands").val($("#editBrands").attr("data-id"));
                    $("#hideDevices").val($("#editDevices").attr("data-id"));
                    $("#hideSims").val($("#editSims").attr("data-id"));
                    // }

                    //              		var form=$("#editForm");
                    //              		form.find('.hidenSubmitControl').each(function(index,ele){
                    //              			debugger;
                    //                    	var $this=$(ele);
                    //                    	var $prev=$this.parent().prev();
                    //                    	if($prev.attr('data-id')!=undefined&&$prev.attr('data-id')!=null&&$prev.attr('data-id').length>0){
                    //                    		if ($("#checkEdit").val() == 0) {
                    //                    			$this.attr('name',$prev.attr('name').replace('__','')).val($prev.val())
                    //                    		} else {
                    //                    			$this.attr('name',$prev.attr('name').replace('__','')).val($prev.attr('data-id'))
                    //                    		}
                    //                    	} else {
                    //                    		$this.attr('name',$prev.attr('name').replace('__','')).val($prev.val())
                    //                    	}
                    //                    	
                    //                    })

                    //组装从业人员id值
                    var proIds = "";
                    $(".peopleSelect").each(function (index, ele) {
                        if ($(ele).attr("data-id")) {
                            proIds += $(ele).attr("data-id") + ",";
                        }
                    });
                    proIds = proIds.substring(0, proIds.lastIndexOf(','));
                    if (monitorType == 1) {
                        //若为人，则把从业人员标签值赋值为空，为了避免传入后台为undefined
                        $("#professionalIds").val("");
                    } else {
                        $("#professionalIds").val(proIds);
                    }

                    //开始旋转
                    // setTimeout(function(){
                    layer.load(2);
                    //},200);
                    if ($('#vehiclePassword').val() === '') {
                        $('#vehiclePassword').val($('#hidePwd').attr('value'));
                    }
                    $("#editForm").ajaxSubmit(function (message) {
                        var json = eval("(" + message + ")");
                        if (!json.success) {
                            layer.msg(json.msg);
                            $("#commonLgWin").modal("hide");
                        } else {
                            $("#commonLgWin").modal("hide");
                            myTable.refresh()
                        }
                        setTimeout(function () {
                            layer.closeAll();
                            myTable.refresh()
                        }, 1000);
                    });
                });
            }
        },
        // 校验车辆信息(是否为空、是否输入正确、是否已经绑定)
        check_brand: function () {
            var elementId = "editBrands";
            var maxLength = 10;
            // var errorMsg1 = vehicleBrandNull;

            // wjk
            var errorMsg1 = '监控对象不能为空';
            if (msgEdit.checkIsEmpty(elementId, errorMsg1) &&
                msgEdit.checkRightBrand(elementId) &&
                msgEdit.checkBrand()) {
                return true;
            } else {
                return false;
            }
        },
        // 校验人员信息(是否为空、是否输入正确、是否已经绑定)
        check_people_number: function () {
            var elementId = "editBrands";
            var maxLength = 8;
            var errorMsg1 = personnelNumberNull;
            var errorMsg2 = publicSize8Length;
            var errorMsg3 = personnelNumberError;
            var errorMsg4 = personnelNumberExists;
            var reg = /^[A-Za-z0-9\u4e00-\u9fa5_-]{2,20}$/;
            if (msgEdit.checkIsEmpty(elementId, errorMsg1)
                //&& msgEdit.checkLength(elementId, maxLength, errorMsg2)
                &&
                msgEdit.checkIsLegal(elementId, reg, null, errorMsg3) &&
                msgEdit.checkPeopleNumber()) {
                return true;
            } else {
                return false;
            }
        },

        // 校验物品信息(是否为空、是否输入正确、是否已经绑定)
        check_thing_number: function () {
            var elementId = "editBrands";
            var errorMsg1 = publicMonitorNull;
            var errorMsg3 = personnelNumberError;
            var reg = /^[A-Za-z0-9\u4e00-\u9fa5_-]{2,20}$/;
            if (msgEdit.checkIsEmpty(elementId, errorMsg1) &&
                msgEdit.checkIsLegal(elementId, reg, null, errorMsg3) &&
                msgEdit.checkThing()) {
                return true;
            } else {
                return false;
            }
        },

        //校验终端信息
        check_device: function () {
            var elementId = "editDevices";
            var maxLength = 30;
            var errorMsg1 = deviceNumberSelect;
            var errorMsg2 = deviceNumberMaxlength;
            var errorMsg3 = deviceNumberError;
            var errorMsg4 = deviceNumberExists;
            var reg = /^[A-Za-z0-9_-]{7,30}$/;
            // if (monitorType == 0) {
            //     reg = /^[A-Za-z0-9_-]{7,15}$/;
            // } else {
            //     reg = /^[0-9a-zA-Z]{1,20}$/;
            // }
            if (msgEdit.checkIsEmpty(elementId, errorMsg1)
                //&& msgEdit.checkLength(elementId, maxLength, errorMsg2)
                &&
                msgEdit.checkIsLegal(elementId, reg, null, errorMsg3) &&
                msgEdit.checkDevice()) {
                return true;
            } else {
                return false;
            }

        },
        //校验终端手机号信息
        check_sim: function () {
            var elementId = "editSims";
            var maxLength = 20;
            var errorMsg1 = simNumberNull;
            var errorMsg2 = simNumberMaxlength;
            var errorMsg3 = simNumberError;
            var errorMsg4 = simNumberExists;
            /*  var reg = /^((13[0-9]{1})|(14[5,7,9]{1})|(15[^4]{1})|(166)|(18[0-9]{1})|(19[8-9]{1})|(17[0,1,3,5,6,7,8]{1}))+\d{8}$/;
              var reg1 = /^((\d{3,4}-\d{7,9})|([1-9]{1}\d{6,12}))$/g;*/
            var reg = /^[0-9a-zA-Z]{7,20}$/g;
            if (msgEdit.checkIsEmpty(elementId, errorMsg1)
                //&& msgEdit.checkLength(elementId, maxLength, errorMsg2)
                &&
                msgEdit.checkIsLegal(elementId, reg, null, '请输入数字字母，范围：7~20位') &&
                msgEdit.checkSIM()) {
                return true;
            } else {
                return false;
            }
        },
        // 校验是否为空
        checkIsEmpty: function (elementId, errorMsg) {
            var value = $("#" + elementId).val();
            if (value == "") {
                msgEdit.hideErrorMsg();
                msgEdit.showErrorMsg(errorMsg, elementId);
                return false;
            } else {
                msgEdit.hideErrorMsg();
                return true;
            }
        },
        // 校验车牌号是否填写规范或者回车时不小心输入了异常字符
        checkRightBrand: function (id) {
            // var errorMsg3 = vehicleBrandError;
            // wjk
            var errorMsg3 = "请输入汉字、字母、数字或短横杠，长度2-20位"
            if (checkBrands(id)) {
                msgEdit.hideErrorMsg();
                return true;
            } else {
                msgEdit.showErrorMsg(errorMsg3, id);
                return false;
            }
        },
        // 校验填写数据的合法性
        checkIsLegal: function (elementId, reg, reg1, errorMsg) {
            var value = $("#" + elementId).val();
            if (reg1 != null) {
                if (!reg.test(value) && !reg1.test(value)) {
                    msgEdit.showErrorMsg(errorMsg, elementId);
                    return false;
                } else {
                    msgEdit.hideErrorMsg();
                    return true;
                }
            } else {
                if (!reg.test(value)) {
                    msgEdit.showErrorMsg(errorMsg, elementId);
                    return false;
                } else {
                    msgEdit.hideErrorMsg();
                    return true;
                }
            }
        },
        // 校验车牌号是否已存在
        checkBrand: function () {
            var tempFlag = true;
            $.ajax({
                type: 'POST',
                url: '/clbs/m/basicinfo/monitoring/vehicle/repetition',
                data: {
                    "brand": $("#editBrands").val()
                },
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (!data) {
                        msgEdit.showErrorMsg(vehicleBrandExists, "editBrands");
                        tempFlag = false;
                    } else {
                        msgEdit.hideErrorMsg();
                        tempFlag = true;
                    }
                },
                error: function () {
                    layer.msg("车牌号校验异常！", {
                        time: 1500,
                    });
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
                data: {
                    "peopleNumber": $("#editBrands").val()
                },
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (!data) {
                        msgEdit.showErrorMsg(personnelNumberExists, "editBrands");
                        tempFlag = false;
                    } else {
                        msgEdit.hideErrorMsg();
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
                data: {
                    "thingNumber": $("#editBrands").val()
                },
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (!data) {
                        msgEdit.showErrorMsg(thingExists, "editBrands");
                        tempFlag = false;
                    } else {
                        msgEdit.hideErrorMsg();
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
                data: {
                    "deviceNumber": $("#editDevices").val()
                },
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (!data) {
                        msgEdit.showErrorMsg(deviceNumberExists, "editDevices");
                        tempFlag = false;
                    } else {
                        msgEdit.hideErrorMsg();
                        tempFlag = true;
                    }
                },
                error: function () {
                    layer.msg("终端号校验异常！", {
                        time: 1500,
                    });
                    tempFlag = false;
                }
            });
            return tempFlag;
        },
        // 校验终端手机号是否已存在
        checkSIM: function () {
            var tempFlag = true;
            $.ajax({
                type: 'POST',
                url: '/clbs/m/basicinfo/equipment/simcard/repetition',
                data: {
                    "simcardNumber": $("#editSims").val()
                },
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (!data) {
                        msgEdit.showErrorMsg(simNumberExists, "editSims");
                        tempFlag = false;
                    } else {
                        msgEdit.hideErrorMsg();
                        tempFlag = true;
                    }
                },
                error: function () {
                    layer.msg("终端手机号校验异常！", {
                        time: 1500,
                    });
                    tempFlag = false;
                }
            });
            return tempFlag;
        },
        // 读取页面选择的分组id
        setAssignmentIds: function () {
            var model = $("#edit-area").children("div");
            var edit = $("#edit-list").children("div");
            var added = $("#edit-add-area").children("div");
            var gid = '';
            if (model != null && model != undefined && model != 'undefined' && model.length > 0) {
                var modelV = model.children("input:last-child").val();
                if (modelV != "") {
                    gid += modelV + "#";
                }
            }
            if (edit != null && edit != undefined && edit != 'undefined' && edit.length > 0) {
                edit.each(function (i) {
                    var editV = $(this).children("div").children("div").children("input:last-child").val();
                    if (editV != "") {
                        gid += editV + "#";
                    }
                });
            }
            if (added != null && added != undefined && added != 'undefined' && added.length > 0) {
                added.each(function (i) {
                    var addedV = $(this).children("div").children("input:last-child").val();
                    if (addedV != "") {
                        gid += addedV + "#";
                    }
                });
            }
            if (gid.length > 0) {
                gid = gid.substr(0, gid.length - 1);
            }
            $("#groupID").val(gid);
        },
        // 通讯类型
        getDeviceTypeValue: function (deviceTypeIntVal) {
            return getProtocolName(deviceTypeIntVal);
        },
        // 功能类型
        getFunctionalTypeValue: function (functionalTypeIntVal) {
            if (functionalTypeIntVal == 1) {
                return "简易型车机";
            } else if (functionalTypeIntVal == 2) {
                return "行车记录仪";
            } else if (functionalTypeIntVal == 3) {
                return "对讲设备";
            } else if (functionalTypeIntVal == 4) {
                return "手咪设备";
            } else if (functionalTypeIntVal == 5) {
                return "超长待机设备";
            } else if (functionalTypeIntVal == 6) {
                return "定位终端";
            } else {
                return "";
            }
        }
    }
    $(function () {
        msgEdit.init();
        msgEdit.initGroup();
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'editTerminalType') {
                $("#editTerminalTypeId").val('');
                $("#editTerminalTypeName").val('');
            }
            var curInput = $(e.target);
            if (curInput.hasClass('zTreeCitySel')) {
                curInput.attr('value', '');
                curInput.siblings('label.error').hide();
                curInput.siblings('.assignmentId').val('');
                search_ztree('ztreeDemo', id, 'assignment', '');
            }
            if (curInput.hasClass('groupCitySel')) {
                curInput.siblings('label.error').hide();
                curInput.siblings('.editGroupId').val('0');
                search_ztree('ztreeDemo2', id, 'group', '');
            }
        });

        $('#terminalManufacturer').on("change", function () {
            var terminalManufacturerName = $(this).find("option:selected").attr("value");
            terminalChangeFlag = true;
            msgEdit.getTerminalType(terminalManufacturerName);
        });

        $('#editTerminalType').on("change", function () {
            var terminalTypeId = $(this).find("option:selected").attr("value");
            $("#editTerminalTypeId").val(terminalTypeId);
        });

        $("#edit-add-btn").on("click", msgEdit.editAdd);
        $("#editpeople-add-btn").on("click", msgEdit.peopleAdd);
        $("#doSubmit").on("click", msgEdit.doSubmits);
        $("#zTreeCitySel").on("click", function () {
            msgEdit.showMenu(this, "#zTreeContent")
        });
        $(".groupCitySel").on("click", function () {
            msgEdit.showMenu(this, "#groupTree")
        });

        $(".zTreeCitySel").on('input propertychange', function (e) {
            var treeObj = $.fn.zTree.getZTreeObj("ztreeDemo");
            treeObj.checkAllNodes(false);
            triggerInputNum++;
            if ((!!window.ActiveXObject || "ActiveXObject" in window) && triggerInputNum < 5) return; // 解决IE赋值频繁触发input事件兼容问题
            $(this).siblings(".assignmentId").val('');
            search_ztree('ztreeDemo', e.target.id, 'assignment');
        });
        $("input.groupCitySel").on('input propertychange', function (e) {
            var treeObj = $.fn.zTree.getZTreeObj("ztreeDemo2");
            treeObj.checkAllNodes(false);
            $(this).siblings('label.error').hide();
            triggerInputNum++;
            if ((!!window.ActiveXObject || "ActiveXObject" in window) && triggerInputNum < 5) return; // 解决IE赋值频繁触发input事件兼容问题
            if ($(this).val() != '') {
                $(this).siblings(".editGroupId").val('').attr('value', '');
            } else {
                $(this).siblings(".editGroupId").val('0');
            }
            search_ztree('ztreeDemo2', e.target.id, 'group');
        });

        $("#editSims").on('change', function () {
            if (!simInputFlag) {
                msgEdit.showHideValueCase(1, simInput);

            }
        });

        $("#editBrands").on('change', function () {
            if (!brandsInputFlag) {
                msgEdit.showHideValueCase(1, brandInput);
            }
        });

        $("#editDevices").on('change', function () {
            if (!brandsInputFlag) {
                msgEdit.showHideValueCase(1, deviceInput);
            }
        });
        $("#editSims").on('input', function () {
            msgEdit.check_sim()
        });
        $("#editBrands").on('input', function () {
            msgEdit.check_brand()
        });
        $("#editDevices").on('input', function () {
            msgEdit.check_device()
        });

    })
})($, window);