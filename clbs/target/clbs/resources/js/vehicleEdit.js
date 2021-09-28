//# sourceURL=vehicleEdit.js
(function ($, window) {
    var vehicleId;
    var selectVehicleType;
    var selfRespectFlag = false;
    var abilityWorkFlag = false;
    var scopeOfOperationData = []; // 经营范围数据
    vehicleEdit = {
        //初始化
        init: function () {
            selectVehicleType = $("#vehType").attr("value");
            vehicleId = $("#idStr").val();
            //操作权限 
            var setpermissionEdit = {
                async: {
                    url: "/clbs/m/basicinfo/monitoring/vehicle/userEditTree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "vehicleId": vehicleId
                    }
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
                    beforeClick: vehicleEdit.beforeClickPermissionEdit,
                    onCheck: vehicleEdit.onCheckPermissionEdit
                }
            }
            var setting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "vid": vehicleId
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
                    beforeClick: vehicleEdit.beforeClick,
                    onClick: vehicleEdit.onClick,
                    onAsyncSuccess: vehicleEdit.zTreeOnAsyncSuccess
                }
            };
            json_ajax("POST", '/clbs/m/dictionary/businessScope', "json", false, null, vehicleEdit.renderScope);
            $.fn.zTree.init($("#permissionEditDemo"), setpermissionEdit, null);
            $.fn.zTree.init($("#ztreeDemo"), setting, null);
            vehicleEdit.setProvinceCity();
            laydate.render({
                elem: '#roadTransportValidityDate',
                theme: '#6dcff6'
            });
            laydate.render({
                elem: '#vehicleTechnologyValidityDate',
                theme: '#6dcff6'
            });

            laydate.render({
                elem: '#transportValidity',
                theme: '#6dcff6'
            });
            laydate.render({
                elem: '#technologyValidity',
                theme: '#6dcff6'
            });
            laydate.render({
                elem: '#maintainValidity',
                theme: '#6dcff6'
            });
            laydate.render({
                elem: '#vehiclePlatformInstallDate',
                theme: '#6dcff6'
            });
            laydate.render({
                elem: '#roadTransportValidityStart',
                theme: '#6dcff6'
            });
            laydate.render({
                elem: '#roadTransportValidity',
                theme: '#6dcff6'
            });
            laydate.render({
                elem: '#registrationStartDate',
                theme: '#6dcff6'
            });
            laydate.render({
                elem: '#registrationEndDate',
                theme: '#6dcff6'
            });
            laydate.render({
                elem: '#licenseIssuanceDate',
                theme: '#6dcff6'
            });
            laydate.render({
                elem: '#registrationDate',
                theme: '#6dcff6'
            });
            laydate.render({
                elem: '#vehicleProductionDate',
                theme: '#6dcff6'
            });
            laydate.render({
                elem: '#firstOnlineTime',
                theme: '#6dcff6',
                type: 'datetime'
            });
            laydate.render({
                elem: '#validEndDate',
                theme: '#6dcff6',
                type: 'month'
            });
            laydate.render({
                elem: '#machineAge',
                theme: '#6dcff6',
                type: 'month'
            });

            vehicleEdit.getOperation();
        },
        /**
         * 渲染经营范围字段选项
         * @param data
         */
        renderScope: function (data) {
            if (data.success) {
                var result = data.obj;
                scopeOfOperationData = result;
                var operationValue = [];
                var scopeOfOperation = $('#scopeOfOperationInput').val().split(',');
                var optionHtml = '';
                for (var i = 0; i < result.length; i++) {
                    var item = result[i];
                    var index = scopeOfOperation.indexOf(item.id);
                    if (index !== -1) {
                        operationValue.push(i);
                    }
                    optionHtml += '<option value="' + i + '">' + item.value + '</option>'
                }
                $('#scopeBusiness').html(optionHtml);
                $('#scopeBusiness').selectpicker({
                    'selectedText': 'cat',
                });
                $('#scopeBusiness').selectpicker("val", operationValue).trigger("change");
                $('#scopeOfOperationInput').val(operationValue.join(','));
                $("#scopeBusiness").change(function () {
                    $('#scopeOfOperationInput').val($("#scopeBusiness").val())
                });
            }
        },
        // 获取所属行业数据
        getOperation: function () {
            var url = "/clbs/c/group/findOperations";
            json_ajax("POST", url, "json", false, null, function (data) {
                if (data.success == true) {
                    var html = '<option  value = "">' + "请选择所属行业" + '</option>';
                    var selectVal = $('#tradeName').val();
                    if (data.obj.operation != null && data.obj.operation.length > 0) {
                        var calldata = data.obj.operation;
                        for (var i = 0; i < calldata.length; i++) {
                            if (calldata[i].operationType == selectVal) {
                                html += '<option  value="' + calldata[i].operationType + '" selected>' + calldata[i].operationType + '</option>';
                            } else {
                                html += '<option  value="' + calldata[i].operationType + '">' + calldata[i].operationType + '</option>';
                            }
                        }
                    }
                    $("#operation").html(html);
                }
            });
        },
        //车辆用途
        vehiclePurpose: function () {
            var url = "/clbs/m/basicinfo/monitoring/vehicle/findAllPurposeCategory";
            json_ajax("POST", url, "json", true, null, vehicleEdit.findPurposeList);
        },
        findPurposeList: function (data) {
            var datas = data.obj.VehicleCategoryList;
            var dataList = {
                    value: []
                },
                i = datas.length;
            while (i--) {
                dataList.value.push({
                    name: datas[i].purposeCategory,
                    id: datas[i].id
                });
            }
            console.log(dataList);
            $("#category").bsSuggest({
                indexId: 1, //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: dataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {}).on('onSetSelectValue', function (e, keyword, data) {
                $("#category").val(keyword.key);
                $("#vehiclePurpose").val(keyword.id);
            }).on('onUnsetSelectValue', function () {});
        },
        //燃料类型
        fuelTypes: function () {
            var url = "/clbs/m/basicinfo/monitoring/vehicle/findAllFuelType";
            json_ajax("POST", url, "json", false, null, vehicleEdit.findFuelTypeList);
        },
        findFuelTypeList: function (data) {
            var selectFuelType = $("#fuelType").attr("value");
            for (var i = 0; i < data.obj.FuelTypeList.length; i++) {
                if ("柴油" == data.obj.FuelTypeList[i].fuelCategory) {
                    if (data.obj.FuelTypeList[i].fuelType == selectFuelType) {
                        $("#dieselOil").append("<option selected value=" + data.obj.FuelTypeList[i].id + ">" + data.obj.FuelTypeList[i].fuelType + "</option>");
                    } else {
                        $("#dieselOil").append("<option value=" + data.obj.FuelTypeList[i].id + ">" + data.obj.FuelTypeList[i].fuelType + "</option>");
                    }
                } else if ("汽油" == data.obj.FuelTypeList[i].fuelCategory) {
                    if (data.obj.FuelTypeList[i].fuelType == selectFuelType) {
                        $("#gasoline").append("<option selected value=" + data.obj.FuelTypeList[i].id + ">" + data.obj.FuelTypeList[i].fuelType + "</option>");
                    } else {
                        $("#gasoline").append("<option value=" + data.obj.FuelTypeList[i].id + ">" + data.obj.FuelTypeList[i].fuelType + "</option>");
                    }
                } else if ("天然气" == data.obj.FuelTypeList[i].fuelCategory) {
                    if (data.obj.FuelTypeList[i].fuelType == selectFuelType) {
                        $("#naturalGas").append("<option selected value=" + data.obj.FuelTypeList[i].id + ">" + data.obj.FuelTypeList[i].fuelType + "</option>");
                    } else {
                        $("#naturalGas").append("<option value=" + data.obj.FuelTypeList[i].id + ">" + data.obj.FuelTypeList[i].fuelType + "</option>");
                    }
                }
            }
        },
        beforeClickPermissionEdit: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("permissionEditDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        onCheckPermissionEdit: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("permissionEditDemo"),
                nodes = zTree
                .getCheckedNodes(true),
                v = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                v += nodes[i].name + ",";
            }
        },
        //提交
        doSubmit: function () {
            if (vehicleEdit.validates()) {
                //判断自重是否必填
                if (selfRespectFlag) {
                    vehicleEdit.selfRespectShow();
                    if ($("#selfRespect").val() == '') {
                        vehicleEdit.goErrorPos();
                        return;
                    }
                }
                //判断工作能力是否必填
                if (abilityWorkFlag) {
                    vehicleEdit.abilityWorkShow();
                    if ($("#abilityWork").val() == '') {
                        vehicleEdit.goErrorPos();
                        return;
                    }
                }
                // 组装经营范围提交数据
                var scopeOfOperationVal = [];
                var scopeOfOperationIds = [];
                var scopeOfOperationCodes = [];
                if ($('#scopeOfOperationInput').val() !== '') {
                    var scopeOfOperationIndex = $('#scopeOfOperationInput').val().split(',');
                    for (var i = 0; i < scopeOfOperationIndex.length; i++) {
                        var item = scopeOfOperationData[scopeOfOperationIndex[i]];
                        scopeOfOperationVal.push(item.value);
                        scopeOfOperationIds.push(item.id);
                        scopeOfOperationCodes.push(item.code);
                    }
                }
                $("#scopeOfOperationVal").val(scopeOfOperationVal.join(','));
                $("#scopeOfOperationIds").val(scopeOfOperationIds.join(','));
                $("#scopeOfOperationCodes").val(scopeOfOperationCodes.join(','));
                addHashCode1($("#editForm"));
                $("#editForm").ajaxSubmit(function (data) {
                    var json = eval("(" + data + ")");
                    if (json.success) {
                        $("#commonWin").modal("hide");
                        layer.msg('修改成功！');
                        myTable.refresh();
                    } else {
                        layer.msg(json.msg);
                    }
                });
            } else {
                vehicleEdit.goErrorPos();
                vehicleEdit.selfRespectShow();
                vehicleEdit.abilityWorkShow();
            }
        },
        //跳转至报错位置
        goErrorPos: function () {
            var errorArr = $("#editForm label.error");
            if (errorArr.length > 0) {
                var newArr = [];
                for (var i = 0; i < errorArr.length; i++) {
                    if ($(errorArr[i]).html() != '') {
                        newArr.push($(errorArr[i]));
                    }
                }
                var firstErr = newArr[0];
                var thisParent = firstErr.closest('.content');
                if (thisParent.length > 0) {
                    var conHeader = thisParent.siblings('.contentHeader');
                    if (thisParent.is(":hidden")) {
                        conHeader.find('.info-span').click();
                    }
                } else {
                    thisParent = firstErr.closest('.col-md-12');
                }
                setTimeout(function () {
                    thisParent[0].scrollIntoView();
                }, 300);
            }
        },
        // 校验车牌号是否填写规范或者回车时不小心输入了异常字符
        checkRightBrand: function (id) {
            // var errorMsg3 = vehicleBrandError;

            // wjk
            var errorMsg3 = '请输入汉字、字母、数字或短横杠，长度2-20位';
            if (checkBrands(id)) {
                vehicleEdit.hideErrorMsg();
                return true;
            } else {
                vehicleEdit.hideErrorMsg(); //wjk,先隐藏
                vehicleEdit.showErrorMsg(errorMsg3, id);
                return false;
            }
        },
        //不能全是横杠
        checkISdhg: function (elementId) {
            var value = $("#" + elementId).val();
            var regIfAllheng = /^[-]*$/;
            if (regIfAllheng.test(value)) {
                vehicleEdit.hideErrorMsg(); //wjk,先隐藏
                vehicleEdit.showErrorMsg('不能全是横杠', elementId);
                return false;
            } else {
                vehicleEdit.hideErrorMsg();
                return true;
            }
        },
        //错误提示信息显示
        showErrorMsg: function (msg, inputId) {
            if ($("#error_label_edit").is(":hidden")) {
                $("#error_label_edit").text(msg);
                $("#error_label_edit").insertAfter($("#" + inputId));
                $("#error_label_edit").show();
            } else {
                $("#error_label_edit").is(":hidden");
            }
        },
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#error_label_edit").is(":hidden");
            $("#error_label_edit").hide();
        },
        isRadio: function () {
            var radioOn = $("#radionOn").val();
            var radionOff = $("#radionOff").val();
            if (radioOn == "false") {
                layer.confirm("您是否确认启用该车辆？", {
                    btn: ['确定', '取消'],
                    closeBtn: 0
                }, function () {
                    $("#radionOn").val($("#isRadio").prop("checked"));
                    $("#radionOff").val($("#noRadio").prop("checked"));
                    layer.closeAll();
                }, function () {
                    $("#isRadio").prop("checked", false);
                    $("#isRadio").removeAttr("checked");
                    $("#noRadio").prop("checked", true);
                    $("#radionOn").val($("#isRadio").prop("checked"));
                    $("#radionOff").val($("#noRadio").prop("checked"));
                });
            }
        },
        noRadio: function () {
            var radioOn = $("#radionOn").val();
            var radionOff = $("#radionOff").val();
            if (radionOff == "false") {
                layer.confirm("您是否确认禁用该车辆？", {
                    btn: ['确定', '取消'],
                    closeBtn: 0
                }, function () {
                    $("#radionOn").val($("#isRadio").prop("checked"));
                    $("#radionOff").val($("#noRadio").prop("checked"));
                    layer.closeAll();
                }, function () {
                    $("#noRadio").prop("checked", false);
                    $("#noRadio").removeAttr("checked");
                    $("#isRadio").prop("checked", true);
                    $("#radionOn").val($("#isRadio").prop("checked"));
                    $("#radionOff").val($("#noRadio").prop("checked"));
                });
            }
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            treeObj.expandAll(true); // 展开所有节点
        },
        beforeClick: function (treeId, treeNode) {
            var check = (treeNode);
            return check;
        },
        onClick: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("ztreeDemo"),
                nodes = zTree.getSelectedNodes(),
                n = "";
            v = "";
            nodes.sort(function compare(a, b) {
                return a.id - b.id;
            });
            for (var i = 0, l = nodes.length; i < l; i++) {
                n += nodes[i].name;
                v += nodes[i].uuid + ";";
            }
            if (v.length > 0)
                v = v.substring(0, v.length - 1);
            var cityObj = $("#zTreeCitySel");
            cityObj.attr("value", v);
            cityObj.val(n);
            $("#groupId").val(v);
            $("#zTreeContent").hide();
        },
        showMenu: function (e) {
            if ($("#zTreeContent").is(":hidden")) {
                var width = $(e).parent().width();
                $("#zTreeContent").css("width", width + "px");
                $(window).resize(function () {
                    var width = $(e).parent().width();
                    $("#zTreeContent").css("width", width + "px");
                })
                $("#zTreeContent").show();
            } else {
                $("#zTreeContent").hide();
            }

            $("body").bind("mousedown", vehicleEdit.onBodyDown);
        },
        hideMenu: function () {
            $("#zTreeContent").fadeOut("fast");
            $("body").unbind("mousedown", vehicleEdit.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "zTreeContent" || $(
                    event.target).parents("#zTreeContent").length > 0)) {
                vehicleEdit.hideMenu();
            }
        },
        validates: function () {
            var adminFlag = $('#isAdmin').val() == 'true';
            return $("#editForm").validate({
                ignore: '',
                rules: {
                    brand: {
                        required: true,
                        minlength: 2,
                        maxlength: 20,
                        isBrand: true,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/monitoring/vehicle/repetition",
                            dataType: "json",
                            data: {
                                username: function () {
                                    return $("#editBrand").val();
                                }
                            },
                            dataFilter: function (data, type) {
                                var oldV = $("#scn").val().trim();
                                var newV = $("#editBrand").val().trim();
                                var data2 = data;
                                if (oldV == newV) {
                                    return true;
                                } else {
                                    if (data2 == "true") {
                                        return true;
                                    } else {
                                        return false;
                                    }
                                }
                            }
                        }
                    },
                    vehicleOwner: {
                        checkPeopleName: true
                    },
                    aliases: {
                        maxlength: 20
                    },
                    vehicleType: {
                        required: true,
                        maxlength: 20
                    },
                    chassisNumber: {
                        // required: false,
                        // maxlength: 50
                        required: false,
                        maxlength: 17,
                        isRightfulStr: true,
                    },
                    engineNumber: {
                        required: false,
                        maxlength: 15
                    },
                    plateColor: {
                        required: false,
                        maxlength: 6
                    },
                    areaAttribute: {
                        required: false,
                        maxlength: 20
                    },
                    province: {
                        maxlength: 20
                    },
                    fuelType: {
                        required: false,
                        maxlength: 20
                    },
                    vehicleOwnerPhone: {
                        isLandline: true
                    },
                    groupName: {
                        isGroupRequired: adminFlag
                    },

                    vehiclePurpose: {
                        required: false,
                        maxlength: 50
                    },
                    vehiclOperationNumber: {
                        required: false,
                        maxlength: 20
                    },
                    roadTransportNumber: {
                        required: false,
                        maxlength: 24
                    },
                    roadTransportValidity: {
                        compareDate: "#roadTransportValidityStart"
                    },
                    registrationEndDate: {
                        compareDate: "#registrationStartDate"
                    },
                    vehicleInsuranceNumber: {
                        required: false,
                        maxlength: 50
                    },
                    vehicleColor: {
                        required: false,
                        maxlength: 6
                    },
                    numberLoad: {
                        digits: true,
                        min: 0,
                        max: 9999
                    },
                    loadingQuality: {
                        min: 0,
                        maxlength: 10,
                        decimalOne: true
                    },
                    vehicleLevel: {
                        maxlength: 20
                    },
                    issuedAuthority: {
                        maxlength: 20
                    },
                    lineNumber: {
                        maxlength: 20
                    },
                    provenance: {
                        maxlength: 20
                    },
                    viaName: {
                        maxlength: 20
                    },
                    destination: {
                        maxlength: 20
                    },
                    departure: {
                        maxlength: 20
                    },
                    routeEntry: {
                        maxlength: 20
                    },
                    destinationStation: {
                        maxlength: 20
                    },
                    dailyNumber: {
                        digits: true,
                        min: 0,
                        max: 9999
                    },
                    managementRemindDays: {
                        digits: true,
                        min: 0,
                        max: 9999
                    },
                    licenseNo: {
                        maxlength: 20
                    },
                    usingNature: {
                        maxlength: 10
                    },
                    brandModel: {
                        maxlength: 20,
                        zysCheck: true,
                    },
                    registrationRemindDays: {
                        digits: true,
                        max: 9999,
                        min: 0
                    },
                    remark: {
                        maxlength: 150
                    },
                    registrationRemark: {
                        maxlength: 150
                    },
                    vehicleBrand: {
                        maxlength: 20
                    },
                    vehicleModel: {
                        maxlength: 20
                    },
                    licenseNumbers: {
                        digits: true,
                        min: 0,
                        max: 99
                    },
                    totalQuality: {
                        min: 0,
                        maxlength: 8,
                        // decimalOne: true
                        number: true
                    },
                    tractionTotalMass: {
                        min: 0,
                        maxlength: 10,
                        decimalOne: true
                    },
                    profileSizeLong: {
                        min: 0,
                        maxlength: 8,
                        number: true,
                    },
                    profileSizeWide: {
                        min: 0,
                        number: true,
                        maxlength: 8,

                    },
                    profileSizeHigh: {
                        min: 0,
                        number: true,
                        maxlength: 8,
                    },
                    internalSizeLong: {
                        digits: true,
                        min: 0,
                        max: 999999
                    },
                    internalSizeWide: {
                        digits: true,
                        min: 0,
                        max: 999999
                    },
                    internalSizeHigh: {
                        digits: true,
                        min: 0,
                        max: 999999
                    },
                    shaftNumber: {
                        digits: true,
                        min: 0,
                        max: 9999
                    },
                    tiresNumber: {
                        digits: true,
                        min: 0,
                        max: 9999
                    },
                    tireSize: {
                        maxlength: 20
                    },
                    vehicleOwnerName: {
                        required: true,
                        checkPeopleName: true
                    },
                    ownerPhoneOne: {
                        mobilePhone: true,
                        required: true
                    },
                    ownerPhoneTwo: {
                        mobilePhone: true
                    },
                    ownerPhoneThree: {
                        mobilePhone: true
                    },
                    vehicleSubTypeId: {
                        required: true
                    },
                    selfRespect: {
                        range: [0, 9999.9],
                        decimalFour: true
                    },
                    abilityWork: {
                        range: [0, 9999.9],
                        decimalFour: true
                    },
                    workingRadius: {
                        range: [0, 999.9],
                        decimalThree: true
                    },
                    initialMileage: {
                        range: [0, 9999999.9],
                        decimalSeven: true
                    },
                    initialWorkHours: {
                        range: [0, 9999999.9],
                        decimalSeven: true
                    },
                    ownerLandline: {
                        isLandline: true
                    },
                    exportRoute: {
                        maxlength: 20
                    },
                    maintainMileage: {
                        digits: true,
                        maxlength: 6,
                    },
                    city: {
                        isRequire: "#province1"
                    },
                    county: {
                        isRequire: "#province1,#city1"
                    },
                },
                messages: {
                    city: {
                        isRequire: "请选择市区"
                    },
                    county: {
                        isRequire: "请选择县"
                    },
                    maintainMileage: {
                        digits: '请输入0-6位整数',
                        maxlength: '请输入0-6位整数',
                    },
                    exportRoute: {
                        maxlength: lengthTwenty
                    },
                    brand: {
                        required: vehicleBrandNull,
                        maxlength: vehicleBrandError,
                        minlength: vehicleBrandError,
                        isBrand: vehicleBrandError,
                        remote: vehicleBrandExists
                    },
                    initialWorkHours: {
                        range: '请输入非负数，范围0-9999999.9'
                    },
                    initialMileage: {
                        range: '请输入非负数，范围0-9999999.9'
                    },
                    workingRadius: {
                        range: '请输入非负数，范围0-999.9'
                    },
                    abilityWork: {
                        range: '请输入非负数，范围0-9999.9'
                    },
                    selfRespect: {
                        range: '请输入非负数，范围0-9999.9'
                    },
                    vehicleSubTypeId: {
                        required: '请选择车辆子类型'
                    },
                    ownerPhoneThree: {
                        mobilePhone: phoneError
                    },
                    ownerPhoneTwo: {
                        mobilePhone: phoneError
                    },
                    ownerPhoneOne: {
                        mobilePhone: phoneError,
                        required: '请输入车主手机1'
                    },
                    vehicleOwnerName: {
                        required: '请输入车主姓名'
                    },
                    tireSize: {
                        maxlength: lengthTwenty
                    },
                    tiresNumber: {
                        min: integerFour,
                        max: integerFour,
                        digits: naturalNumber
                    },
                    shaftNumber: {
                        min: integerFour,
                        max: integerFour,
                        digits: naturalNumber
                    },
                    internalSizeHigh: {
                        min: integerSix,
                        max: integerSix,
                        digits: naturalNumber
                    },
                    internalSizeWide: {
                        min: integerSix,
                        max: integerSix,
                        digits: naturalNumber
                    },
                    internalSizeLong: {
                        min: integerSix,
                        max: integerSix,
                        digits: naturalNumber
                    },
                    profileSizeHigh: {
                        min: minZero,
                        maxlength: lengthEight,
                        number: naturalNumber,
                    },
                    profileSizeWide: {
                        min: minZero,
                        maxlength: lengthEight,
                        number: naturalNumber,
                    },
                    profileSizeLong: {
                        min: minZero,
                        maxlength: lengthEight,
                        number: naturalNumber,
                    },
                    tractionTotalMass: {
                        min: minZero,
                        maxlength: lengthTen
                    },
                    totalQuality: {
                        min: minZero,
                        maxlength: lengthEight,
                        number: naturalNumber,
                    },
                    licenseNumbers: {
                        min: integerTwo,
                        max: integerTwo,
                        digits: naturalNumber
                    },
                    vehicleModel: {
                        maxlength: lengthTwenty
                    },
                    vehicleBrand: {
                        maxlength: lengthTwenty
                    },
                    remark: {
                        maxlength: '最大长度不能超过150位'
                    },
                    registrationRemark: {
                        maxlength: '长度不超过150位'
                    },
                    registrationRemindDays: {
                        max: integerFour,
                        min: integerFour,
                        digits: naturalNumber
                    },
                    brandModel: {
                        maxlength: publicSize20,
                        zysCheck: chEnNumberErr
                    },
                    usingNature: {
                        maxlength: publicSize10
                    },
                    licenseNo: {
                        maxlength: lengthTwenty
                    },
                    managementRemindDays: {
                        max: integerFour,
                        min: integerFour,
                        digits: naturalNumber
                    },
                    dailyNumber: {
                        max: integerFour,
                        min: integerFour,
                        digits: naturalNumber
                    },
                    destinationStation: {
                        maxlength: lengthTwenty
                    },
                    routeEntry: {
                        maxlength: lengthTwenty
                    },
                    departure: {
                        maxlength: lengthTwenty
                    },
                    destination: {
                        maxlength: lengthTwenty
                    },
                    viaName: {
                        maxlength: lengthTwenty
                    },
                    provenance: {
                        maxlength: lengthTwenty
                    },
                    lineNumber: {
                        maxlength: lengthTwenty
                    },
                    issuedAuthority: {
                        maxlength: lengthTwenty
                    },
                    vehicleLevel: {
                        maxlength: lengthTwenty
                    },
                    loadingQuality: {
                        min: minZero,
                        maxlength: lengthTen
                    },
                    numberLoad: {
                        min: integerFour,
                        max: integerFour,
                        digits: naturalNumber
                    },
                    vehicleNumber: {
                        required: publicNull,
                        maxlength: publicSize20
                    },
                    aliases: {
                        maxlength: publicSize20
                    },
                    vehicleType: {
                        required: '请选择车辆类型',
                        maxlength: publicSize20
                    },
                    chassisNumber: {
                        // required: publicNull,
                        // maxlength: publicSize50
                        required: publicNull,
                        maxlength: publicSize17,
                        isRightfulStr: enNumberErr
                    },
                    engineNumber: {
                        // required: publicNull,
                        // maxlength: publicSize20
                        required: publicNull,
                        maxlength: publicSize15
                    },
                    plateColor: {
                        required: publicNull,
                        maxlength: publicSize6
                    },
                    areaAttribute: {
                        required: publicNull,
                        maxlength: publicSize20
                    },
                    province: {
                        maxlength: publicSize20
                    },
                    fuelType: {
                        required: publicNull,
                        maxlength: publicSize20
                    },
                    vehicleOwnerPhone: {
                        isLandline: telPhoneError
                    },
                    groupName: {
                        isGroupRequired: "请选择所属企业！"
                    },
                    vehiclePurpose: {
                        required: publicNull,
                        maxlength: publicSize50
                    },
                    vehiclOperationNumber: {
                        required: publicNull,
                        maxlength: publicSize20
                    },
                    roadTransportNumber: {
                        required: publicNull,
                        maxlength: publicSize24
                    },
                    vehicleInsuranceNumber: {
                        required: publicNull,
                        maxlength: publicSize50
                    },
                    vehicleColor: {
                        required: publicNull,
                        maxlength: publicSize6
                    },
                    roadTransportValidity: {
                        compareDate: "结束时间必须大于开始时间！"
                    },
                    registrationEndDate: {
                        compareDate: "结束时间必须大于开始时间！"
                    }
                }
            }).form();
        },
        setProvinceCity: function () {
            var strProvince = $("#hiddenProvince").attr("value");
            $("#province1").val(strProvince);
            $("#province1").trigger("change");
            var strCity = $("#hiddenCity").attr("value");
            $("#city1").val(strCity);
            $("#city1").trigger("change");
            var county = $("#county").attr("value");
            $("#county").val(county);
        },

        //图片上传预览功能
        setImagePreview: function (avalue) {
            vehicleEdit.uploadImage(); // 上传图片到服务器
            var docObj = document.getElementById("doc");
            var imgObjPreview = document.getElementById("preview");
            if (docObj.files && docObj.files[0]) {
                //火狐下，直接设img属性
                imgObjPreview.style.display = 'block';
                imgObjPreview.style.width = '200px';
                imgObjPreview.style.height = '200px';
                //火狐7以上版本不能用上面的getAsDataURL()方式获取，需要一下方式
                if (window.navigator.userAgent.indexOf("Chrome") >= 1 || window.navigator.userAgent.indexOf("Safari") >= 1) {
                    imgObjPreview.src = window.webkitURL.createObjectURL(docObj.files[0]);
                } else {
                    imgObjPreview.src = window.URL.createObjectURL(docObj.files[0]);
                }
            } else {
                //IE下，使用滤镜
                docObj.select();
                var imgSrc = document.selection.createRange().text;
                var localImagId = document.getElementById("localImag");
                //必须设置初始大小
                localImagId.style.width = "200px";
                localImagId.style.height = "200px";
                //图片异常的捕捉，防止用户修改后缀来伪造图片
                try {
                    localImagId.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale)";
                    localImagId.filters.item("DXImageTransform.Microsoft.AlphaImageLoader").src = imgSrc;
                } catch (e) {
                    layer.msg("不支持的图片格式文件，<br/>支持格式（png，jpg，gif，jpeg）");
                    return false;
                }
                imgObjPreview.style.display = 'none';
                document.selection.empty();
            }
            return true;
        },
        //上传图片
        uploadImage: function () {
            var docObj = document.getElementById("doc");
            if (docObj.files && docObj.files[0]) {
                var formData = new FormData();
                formData.append("file", docObj.files[0]);
                $.ajax({
                    url: '/clbs/m/basicinfo/enterprise/professionals/upload_img',
                    type: 'POST',
                    data: formData,
                    async: false,
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function (data) {
                        data = $.parseJSON(data);
                        if (data.imgName == "0") {
                            layer.msg("不支持的图片格式文件，<br/>支持格式（png，jpg，gif，jpeg）");
                            $("#preview").src("");
                        } else {
                            $("#vehiclephoto").val(data.imgName);
                        }
                    },
                    error: function (data) {
                        layer.msg("上传失败！");
                    }
                });
            }
        },

        //点击显示隐藏信息
        hiddenparameterFn: function () {
            var clickId = $(this).attr('id');
            if (!($("#" + clickId + "-content").is(":hidden"))) {
                $("#" + clickId + "-content").slideUp();
                $("#" + clickId).children("font").text("显示更多");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-down");
            } else {
                $("#" + clickId + "-content").slideDown();
                $("#" + clickId).children("font").text("隐藏信息");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-up");
            }
        },
        //获取车辆类别
        getVehicleCategory: function () {
            var url = "/clbs/m/basicinfo/monitoring/vehicle/type/findAllVehicleCategoryHasBindingVehicleType.gsp";
            var data = {
                "vehicleCategory": ""
            }
            json_ajax("GET", url, "json", false, data, vehicleEdit.categoryCallBack);
        },
        categoryCallBack: function (data) {
            var vc = $("#vehicleCategory").attr("value");
            var result = data.obj.result;
            var str = "";
            for (var i = 0; i < result.length; i++) {
                if (vc == result[i].vehicleCategory) {
                    str += '<option selected="selected" value="' + result[i].id + '" standard="' + result[i].standard + '">' + html2Escape(result[i].vehicleCategory) + '</option>'
                } else {
                    str += '<option  value="' + result[i].id + '" standard="' + result[i].standard + '">' + html2Escape(result[i].vehicleCategory) + '</option>'
                }
            }

            $("#vehicleCategory").html(str);
        },
        //判断用户所选车辆类别标准
        checkCategoryStandard: function (standard) {
            var freightTransportBox = $(".freightTransport-box");
            var constructionMachineryBox = $(".constructionMachinery-box");
            if (standard == '0' || standard == null || standard == 'null' || standard == undefined) {
                freightTransportBox.find('input').attr("disabled", true);
                freightTransportBox.find('select').attr("disabled", true);
                freightTransportBox.hide();
                constructionMachineryBox.find('input').attr("disabled", true);
                constructionMachineryBox.find('select').attr("disabled", true);
                constructionMachineryBox.hide();
            } else if (standard == '1') {
                freightTransportBox.find('input').attr("disabled", false);
                freightTransportBox.find('select').attr("disabled", false);
                freightTransportBox.show();
                constructionMachineryBox.find('input').attr("disabled", true);
                constructionMachineryBox.find('select').attr("disabled", true);
                constructionMachineryBox.hide();
            } else if (standard == '2') {
                freightTransportBox.find('input').attr("disabled", true);
                freightTransportBox.find('select').attr("disabled", true);
                freightTransportBox.hide();
                constructionMachineryBox.find('input').attr("disabled", false);
                constructionMachineryBox.find('select').attr("disabled", false);
                constructionMachineryBox.show();
            }
        },

        //获取车辆类型
        getVehicleType: function (id) {
            var url = "/clbs/m/basicinfo/monitoring/vehicle/type/findCategoryById_" + id + ".gsp";
            json_ajax("GET", url, "json", false, null, vehicleEdit.getTypeCallback);
        },
        getTypeCallback: function (data) {
            var vt = $("#vehicleType").attr("value");
            var result = data.obj.vehicleTypeList;
            var str = "";
            if (result != null && result != '') {
                for (var i = 0; i < result.length; i++) {
                    if (vt == result[i].id) {
                        str += '<option selected="selected" value="' + result[i].id + '">' + html2Escape(result[i].vehicleType) + '</option>'
                    } else {
                        str += '<option  value="' + result[i].id + '">' + html2Escape(result[i].vehicleType) + '</option>'
                    }
                }
            }
            $("#vehicleType").html(str);
            vehicleEdit.getVehicleSubType($("#vehicleType").val());
        },

        //获取车辆子类型
        getVehicleSubType: function (id) {
            var url = "/clbs/m/basicinfo/monitoring/vehicle/type/findTypeIsBindingSubType_" + id + ".gsp";
            json_ajax("GET", url, "json", false, null, vehicleEdit.getSubTypeCallback);
        },
        getSubTypeCallback: function (data) {
            var st = $("#vehicleSubTypeId").attr("value");
            var result = data.obj.result;
            var str = "<option value=''>- 请选择车辆子类型 -</option>";
            if (result != null && result != '') {
                for (var i = 0; i < result.length; i++) {
                    if (st == result[i].id) {
                        str += '<option selected="selected" value="' + result[i].id + '" driving="' + result[i].drivingWay + '">' + html2Escape(result[i].vehicleSubtypes) + '</option>'
                    } else {
                        str += '<option  value="' + result[i].id + '" driving="' + result[i].drivingWay + '">' + html2Escape(result[i].vehicleSubtypes) + '</option>'
                    }
                }
            }
            $("#vehicleSubTypeId").html(str);
        },

        //自重是否必填
        selfRespectRequired: function () {
            var vehicleCategory = $("#vehicleCategory option:selected").html();
            var drivingWay = $("#vehicleSubTypeId option:selected").attr('driving');
            if (vehicleCategory == '工程车辆' && drivingWay == '1') {
                selfRespectFlag = true;
                $('.selfRespect-required').show();
            } else {
                selfRespectFlag = false;
                $('.selfRespect-required').hide();
            }
        },
        selfRespectShow: function () {
            if ($("#selfRespect").val() == '') {
                if (selfRespectFlag) {
                    $("#selfRespect-error").html('请输入车辆自重');
                    $("#selfRespect-error").show();
                } else {
                    $("#selfRespect-error").hide();
                }
            }
        },

        //工作能力是否必填
        abilityWorkRequired: function () {
            var vehicleCategory = $("#vehicleCategory option:selected").html();
            var vehicleType = $("#vehicleType option:selected").html();
            if (vehicleCategory == '运输车辆' && vehicleType == '拖车') {
                abilityWorkFlag = true;
                $('.abilityWork-required').show();
            } else {
                abilityWorkFlag = false;
                $('.abilityWork-required').hide();
            }
        },
        abilityWorkShow: function () {
            var abilityWorkError = $("#abilityWork-error");
            if ($("#abilityWork").val() == '') {
                if (abilityWorkFlag) {
                    abilityWorkError.html('请输入车辆工作能力');
                    abilityWorkError.show();
                } else {
                    abilityWorkError.hide();
                }
            }
        },


        //获取品牌
        getBrand: function () {
            var url = "/clbs/m/basicinfo/monitoring/vehicle/findBrand";
            json_ajax("GET", url, "json", false, null, vehicleEdit.getBrandCallback);
        },
        getBrandCallback: function (data) {
            var br = $("#brandName").attr("value");
            var result = data.obj.brandList;
            var str = "<option value=''>- 请选择车辆品牌 -</option>";
            if (result != null && result != '') {
                for (var i = 0; i < result.length; i++) {
                    if (br == result[i].brandName) {
                        str += '<option selected="selected" value="' + result[i].id + '">' + html2Escape(result[i].brandName) + '</option>'
                    } else {
                        str += '<option value="' + result[i].id + '">' + html2Escape(result[i].brandName) + '</option>'
                    }
                }
            }
            $("#brandName").html(str);
        },

        //获取机型
        getModel: function (id) {
            var url = "/clbs/m/basicinfo/monitoring/vehicle/findBrandModelsByBrandId_" + id + ".gsp";
            json_ajax("GET", url, "json", false, null, vehicleEdit.getModelCallback);
        },
        getModelCallback: function (data) {
            var bm = $("#brandModelsId").attr("value");
            var result = data.obj.brandModelList;
            var str = "<option value=''>- 请选择车辆机型 -</option>";
            if (result != null && result != '') {
                for (var i = 0; i < result.length; i++) {
                    if (bm == result[i].id) {
                        str += '<option selected="selected" value="' + result[i].id + '">' + html2Escape(result[i].modelName) + '</option>'
                    } else {
                        str += '<option  value="' + result[i].id + '">' + html2Escape(result[i].modelName) + '</option>'
                    }
                }
            }
            $("#brandModelsId").html(str);
        },
        // 运输证和行驶证正反面照片显示
        carInfoPhoto: function () {
            var transportNumberPhotoUrl = $("#transportNumberPhoto").val();
            var drivingLicenseFrontPhotoUrl = $("#drivingLicenseFrontPhoto").val();
            var drivingLicenseDuplicatePhotoUrl = $("#drivingLicenseDuplicatePhoto").val();
            var index = 4;
            if (transportNumberPhotoUrl != '') {
                transportNumberPhotoUrl = transportNumberPhotoUrl;
                $('#transportNumberImageUrl').attr('src', transportNumberPhotoUrl);
            } else {
                $('#transportNumberImage').hide();
                $('#transportNumberText').hide();
                $('#carInfoPhotoTable').css('width', '75%');
                index -= 1;
            }
            if (drivingLicenseFrontPhotoUrl != '') {
                drivingLicenseFrontPhotoUrl = drivingLicenseFrontPhotoUrl;
                $('#drivingLicenseFrontImageUrl').attr('src', drivingLicenseFrontPhotoUrl);
            } else {
                $('#drivingLicenseFrontImage').hide();
                $('#drivingLicenseFrontText').hide();
                $('#carInfoPhotoTable').css('width', '50%');
                index -= 1;
            }
            if (drivingLicenseDuplicatePhotoUrl != '') {
                drivingLicenseDuplicatePhotoUrl = drivingLicenseDuplicatePhotoUrl;
                $('#drivingLicenseDuplicateImageUrl').attr('src', drivingLicenseDuplicatePhotoUrl);
            } else {
                $('#drivingLicenseDuplicateImage').hide();
                $('#drivingLicenseDuplicateText').hide();
                $('#carInfoPhotoTable').css('width', '100%');
                index -= 1;
            }

            $('#carInfoPhotoTableFirstTr').css('width', 100 / index + '%');
        }
    }
    $(function () {
        var vehiclephoto = $("#vehiclephoto").val();
        // var src;
        if (vehiclephoto == '') {
            // src = "/clbs/resources/img/showMedia_img.png";
            $("#preview").attr("style", "width:0px");
        } else {
            // src = "/clbs/upload/" + vehiclephoto
            $("#preview").attr("src", vehiclephoto);
        }
        vehicleEdit.carInfoPhoto();
        vehicleEdit.init();
        vehicleEdit.vehiclePurpose();
        vehicleEdit.fuelTypes();

        //初始化del按钮，延迟20ms使IE浏览器去掉初始化del按钮
        setTimeout(function () {
            $('input').inputClear();
        }, 20);


        $("#zTreeCitySel").on("click", function () {
            vehicleEdit.showMenu(this)
        });
        $("#doSubmitEdit").on("click", vehicleEdit.doSubmit);
        $("#isRadio").on("click", vehicleEdit.isRadio);
        $("#noRadio").on("click", vehicleEdit.noRadio);
        $("#radionOn").val($("#isRadio").prop("checked"));
        $("#radionOff").val($("#noRadio").prop("checked"));
        setTimeout('$(".delIcon").hide()', 100)

        //点击显示隐藏信息
        $(".info-span").on("click", vehicleEdit.hiddenparameterFn);

        vehicleEdit.getVehicleCategory();
        //按车辆类别对应的标准动态显示信息
        var curStandard = $("#vehicleCategory option:selected").attr("standard");
        vehicleEdit.checkCategoryStandard(curStandard);
        $('#vehicleCategory').change(function () {
            var selectStandard = $(this).find("option:selected").attr("standard");
            vehicleEdit.checkCategoryStandard(selectStandard);
        })

        //获取车辆类型
        var curId = $("#vehicleCategory option:selected").val();
        vehicleEdit.getVehicleType(curId);
        $("#vehicleCategory").on("change", function () {
            var curId = $(this).val();
            vehicleEdit.getVehicleType(curId);
            vehicleEdit.selfRespectRequired();
            vehicleEdit.abilityWorkRequired();

            $("#selfRespect-error").hide();
            $("#abilityWork-error").hide();
        })

        //获取车辆子类型
        var curId1 = $("#vehicleType").val();
        vehicleEdit.getVehicleSubType(curId1);
        $("#vehicleType").on("change", function () {
            var curId1 = $(this).val();
            vehicleEdit.getVehicleSubType(curId1);

            vehicleEdit.selfRespectRequired();
            vehicleEdit.abilityWorkRequired();

            $("#selfRespect-error").hide();
            $("#abilityWork-error").hide();
        });

        //判断自重是否必填
        vehicleEdit.selfRespectRequired();
        $("#vehicleSubTypeId").on("change", function () {
            vehicleEdit.selfRespectRequired();
            $("#selfRespect-error").hide();
        });
        $("#selfRespect").on("input propertychange change", function () {
            vehicleEdit.selfRespectShow();
        })

        //工作能力是否必填
        vehicleEdit.abilityWorkRequired();
        $("#abilityWork").on("input propertychange change", function () {
            vehicleEdit.abilityWorkShow();
        })
        //获取品牌
        vehicleEdit.getBrand();
        //获取机型
        var brandId = $("#brandName").val();
        vehicleEdit.getModel(brandId);
        $("#brandName").on("change", function () {
            var brandId = $(this).val();
            vehicleEdit.getModel(brandId);
        });
        // 组织树input框的模糊搜索
        $("#zTreeCitySel").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("ztreeDemo");
            treeObj.checkAllNodes(false);
            search_ztree('ztreeDemo', 'zTreeCitySel', 'group');
        });
        // 组织树input框快速清空
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            var treeObj;
            if (id == 'zTreeCitySel') {
                search_ztree('ztreeDemo', 'zTreeCitySel', 'group');
                treeObj = $.fn.zTree.getZTreeObj("ztreeDemo");
                treeObj.checkAllNodes(false);
            }
        });
    })
})($, window)