//# sourceURL=vehicleBatchEdit.js
(function ($, window) {
    var submissionFlag = false;
    vehicleBatchEdit = {
        //初始化、
        init: function () {
            console.log('checkMonitorObj', checkMonitorObj);
            var monitorName = checkMonitorObj.name.join(',');
            var monitorId = checkMonitorObj.id.join(',');
            $('#brand').val(monitorName).attr('title', monitorName);
            $('#vehicleIds').val(monitorId);
            $('#batchEditForm input[type="checkbox"]').on('change', function () {
                var isCheck = $(this).is(':checked');
                $(this).closest('label').next('div').find('input').prop('disabled', !isCheck);
                $(this).closest('label').next('div').find('select').prop('disabled', !isCheck);
                var dataEdit = $(this).attr('data-edit');
                if (dataEdit) {
                    var curClass = $(this).attr('class');
                    $('.' + curClass + '').prop('checked', isCheck);
                    $('.' + dataEdit + '').prop('disabled', !isCheck);
                }
            });

            //操作权限
            var setpermission = {
                async: {
                    url: "/clbs/m/basicinfo/monitoring/vehicle/userTree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
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
                    beforeClick: vehicleBatchEdit.beforeClickPermission,
                    onCheck: vehicleBatchEdit.onCheckPermission
                }
            }
            var setting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    contentType: "application/json",
                    dataType: "json",
                    dataFilter: vehicleBatchEdit.ajaxDataFilter
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
                    beforeClick: vehicleBatchEdit.beforeClick,
                    onClick: vehicleBatchEdit.onClick,
                    onAsyncSuccess: vehicleBatchEdit.zTreeOnAsyncSuccess
                }
            };
            vehicleBatchEdit.getOperation();
            //车辆用途
            var urlPurposeCategory = "/clbs/m/basicinfo/monitoring/vehicle/findAllPurposeCategory";
            json_ajax("POST", urlPurposeCategory, "json", false, null, vehicleBatchEdit.getVehiclePurposeCallback);
            //燃料类型
            var urlFuelType = "/clbs/m/basicinfo/monitoring/vehicle/findAllFuelType";
            json_ajax("POST", urlFuelType, "json", false, null, vehicleBatchEdit.getFuelTypeCallback);

            $.fn.zTree.init($("#permissionDemo"), setpermission, null);
            $.fn.zTree.init($("#ztreeDemo"), setting, null);
            laydate.render({elem: '#transportValidity', theme: '#6dcff6'});
            laydate.render({elem: '#technologyValidity', theme: '#6dcff6'});
            laydate.render({elem: '#maintainValidity', theme: '#6dcff6'});
            laydate.render({elem: '#vehiclePlatformInstallDate', theme: '#6dcff6'});
            laydate.render({elem: '#roadTransportValidityStart', theme: '#6dcff6'});
            laydate.render({elem: '#roadTransportValidity', theme: '#6dcff6'});
            laydate.render({elem: '#registrationStartDate', theme: '#6dcff6'});
            laydate.render({elem: '#registrationEndDate', theme: '#6dcff6'});
            laydate.render({elem: '#licenseIssuanceDate', theme: '#6dcff6'});
            laydate.render({elem: '#registrationDate', theme: '#6dcff6'});
            laydate.render({elem: '#vehicleProductionDate', theme: '#6dcff6'});
            laydate.render({elem: '#firstOnlineTime', theme: '#6dcff6', type: 'datetime'});
            laydate.render({elem: '#validEndDate', theme: '#6dcff6', type: 'month'});
            laydate.render({elem: '#machineAge', theme: '#6dcff6', type: 'month'});
        },
        // 获取所属行业数据
        getOperation: function () {
            var url = "/clbs/c/group/findOperations";
            json_ajax("POST", url, "json", false, null, function (data) {
                if (data.success == true) {
                    var html = '<option  value = "">' + "请选择所属行业" + '</option>';
                    if (data.obj.operation != null && data.obj.operation.length > 0) {
                        var calldata = data.obj.operation;
                        for (var i = 0; i < calldata.length; i++) {
                            html += '<option  value="' + calldata[i].operationType + '">' + calldata[i].operationType + '</option>';
                        }
                    }
                    $("#operation").html(html);
                }
            });
        },
        getVehiclePurposeCallback: function (data) {
            var datas = data.obj.VehicleCategoryList;
            var dataList = {value: []}, i = datas.length;
            while (i--) {
                dataList.value.push({
                    name: datas[i].purposeCategory,
                    id: datas[i].id
                });
                if (datas[i].codeNum != null) {
                    $("#category").val(datas[i].purposeCategory);
                    $("#vehiclePurpose").val(datas[i].id);
                }
            }
            $("#category").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: dataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                $("#category").val(keyword.key);
                $("#vehiclePurpose").val(keyword.id);
            }).on('onUnsetSelectValue', function () {
            });
            $("#category").prop('disabled', true);
        },
        getFuelTypeCallback: function (data) {
            for (var i = 0; i < data.obj.FuelTypeList.length; i++) {
                if ("柴油" == data.obj.FuelTypeList[i].fuelCategory) {
                    $("#dieselOil").append("<option value=" + data.obj.FuelTypeList[i].id + ">" + data.obj.FuelTypeList[i].fuelType + "</option>");
                } else if ("汽油" == data.obj.FuelTypeList[i].fuelCategory) {
                    $("#gasoline").append("<option value=" + data.obj.FuelTypeList[i].id + ">" + data.obj.FuelTypeList[i].fuelType + "</option>");
                } else if ("天然气" == data.obj.FuelTypeList[i].fuelCategory) {
                    $("#naturalGas").append("<option value=" + data.obj.FuelTypeList[i].id + ">" + data.obj.FuelTypeList[i].fuelType + "</option>");
                }
            }
        },
        beforeClickPermission: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("permissionDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        onCheckPermission: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("permissionDemo"), nodes = zTree
                .getCheckedNodes(true), v = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                v += nodes[i].name + ",";
            }
        },
        //提交
        doSubmit: function () {
            if (submissionFlag) {  // 防止重复提交
                return;
            } else {
                if (vehicleBatchEdit.validates()) {
                    submissionFlag = true;
                    setTimeout(function () {
                        layer.load(2);
                    }, 0);
                    $("#batchEditForm").ajaxSubmit(function (data) {
                        var json = eval("(" + data + ")");
                        layer.closeAll();
                        if (json.success) {
                            $("#commonWin").modal("hide");
                            layer.msg('修改成功！');
                            myTable.requestData();
                        } else {
                            layer.msg(json.msg);
                        }
                    });
                }
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
                v = "";
            n = "";
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
            cityObj.val(n);
            $("#selectGroup").val(v);
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

            $("body").bind("mousedown", vehicleBatchEdit.onBodyDown);
        },
        hideMenu: function () {
            $("#zTreeContent").fadeOut("fast");
            $("body").unbind("mousedown", vehicleBatchEdit.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "zTreeContent" || $(
                event.target).parents("#zTreeContent").length > 0)) {
                vehicleBatchEdit.hideMenu();
            }
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            vehicleBatchEdit.hideErrorMsg();//清除错误提示样式
            if (responseData != null && responseData != "" && responseData != undefined && responseData.length >= 1) {
                if ($("#selectGroup").val() == "") {
                    $("#selectGroup").val(responseData[0].uuid);
                    $("#zTreeCitySel").val(responseData[0].name);
                }
                return responseData;
            } else {
                vehicleBatchEdit.showErrorMsg("您需要先新增一个组织", "zTreeCitySel");
                return;
            }
        },
        isRadio: function () {
            var radioOn = $("#radionOn").val();
            if (radioOn == "false") {
                layer.confirm("您是否确认启用该车辆？",
                    {btn: ['确定', '取消'], closeBtn: 0}, function () {
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
            var radionOff = $("#radionOff").val();
            if (radionOff == "false") {
                layer.confirm("您是否确认禁用该车辆？",
                    {btn: ['确定', '取消'], closeBtn: 0}, function () {
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
        validates: function () {
            var adminFlag = $('#isAdmin').val() == 'true';
            return $("#batchEditForm").validate({
                    ignore: '',
                    rules: {
                        vehicleOwner: {
                            checkPeopleName: true
                        },
                        aliases: {
                            maxlength: 20
                        },
                        vehicleType: {
                            maxlength: 20
                        },
                        chassisNumber: {
                            maxlength: 17,
                            isRightfulStr: true,
                        },
                        engineNumber: {
                            maxlength: 15
                        },
                        plateColor: {
                            maxlength: 6
                        },
                        areaAttribute: {
                            maxlength: 20
                        },
                        province: {
                            maxlength: 20
                        },
                        fuelType: {
                            maxlength: 20
                        },
                        vehicleOwnerPhone: {
                            isLandline: true
                        },
                        groupName: {
                            isGroupRequired: adminFlag
                        },
                        vehiclePurpose: {
                            maxlength: 50
                        },
                        vehiclOperationNumber: {
                            maxlength: 20
                        },
                        roadTransportNumber: {
                            maxlength: 24
                        },
                        roadTransportValidity: {
                            compareDate: "#roadTransportValidityStart"
                        },
                        registrationEndDate: {
                            compareDate: "#registrationStartDate"
                        },
                        vehicleInsuranceNumber: {
                            maxlength: 50
                        },
                        vehicleColor: {
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
                        scopeBusiness: {
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
                            maxlength: 50
                        },
                        registrationRemark: {
                            maxlength: 50
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
                            // digits: true,
                            min: 0,
                            maxlength: 8,
                            number: true,
                            // max: 999999
                        },
                        profileSizeWide: {
                            // digits: true,
                            min: 0,
                            number: true,
                            maxlength: 8,
                            // max: 999999

                        },
                        profileSizeHigh: {
                            // digits: true,
                            min: 0,
                            number: true,
                            maxlength: 8,
                            // max: 999999
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
                            checkPeopleName: true
                        },
                        ownerPhoneOne: {
                            mobilePhone: true,
                        },
                        ownerPhoneTwo: {
                            mobilePhone: true
                        },
                        ownerPhoneThree: {
                            mobilePhone: true
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
                            isRequire: "请选择市区",
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
                        initialWorkHours: {
                            range: '请输入非负数，范围0-9999999.9'
                        }
                        ,
                        initialMileage: {
                            range: '请输入非负数，范围0-9999999.9'
                        }
                        ,
                        workingRadius: {
                            range: '请输入非负数，范围0-999.9'
                        }
                        ,
                        abilityWork: {
                            range: '请输入非负数，范围0-9999.9'
                        }
                        ,
                        selfRespect: {
                            range: '请输入非负数，范围0-9999.9'
                        }
                        ,
                        ownerPhoneThree: {
                            mobilePhone: phoneError
                        }
                        ,
                        ownerPhoneTwo: {
                            mobilePhone: phoneError
                        }
                        ,
                        ownerPhoneOne: {
                            mobilePhone: phoneError,
                        }
                        ,
                        tireSize: {
                            maxlength: lengthTwenty
                        }
                        ,
                        tiresNumber: {
                            min: integerFour,
                            max:
                            integerFour,
                            digits:
                            naturalNumber
                        }
                        ,
                        shaftNumber: {
                            min: integerFour,
                            max:
                            integerFour,
                            digits:
                            naturalNumber
                        }
                        ,
                        internalSizeHigh: {
                            min: integerSix,
                            max:
                            integerSix,
                            digits:
                            naturalNumber
                        }
                        ,
                        internalSizeWide: {
                            min: integerSix,
                            max:
                            integerSix,
                            digits:
                            naturalNumber
                        }
                        ,
                        internalSizeLong: {
                            min: integerSix,
                            max:
                            integerSix,
                            digits:
                            naturalNumber
                        }
                        ,
                        profileSizeHigh: {
                            // min: integerSix,
                            // max:
                            // integerSix,
                            // digits:
                            // naturalNumber
                            min: minZero,
                            maxlength: lengthEight,
                            number: naturalNumber,
                        }
                        ,
                        profileSizeWide: {
                            // min: integerSix,
                            // max:
                            // integerSix,
                            // digits:
                            // naturalNumber
                            min: minZero,
                            maxlength: lengthEight,
                            number: naturalNumber,
                        }
                        ,
                        profileSizeLong: {
                            min: minZero,
                            maxlength: lengthEight,
                            number: naturalNumber,
                            // max: integerSix,
                            // digits: naturalNumber
                        }
                        ,
                        tractionTotalMass: {
                            min: minZero,
                            maxlength:
                            lengthTen
                        }
                        ,
                        totalQuality: {
                            min: minZero,
                            maxlength: lengthEight,
                            number: naturalNumber,
                        }
                        ,
                        licenseNumbers: {
                            min: integerTwo,
                            max:
                            integerTwo,
                            digits:
                            naturalNumber
                        }
                        ,
                        vehicleModel: {
                            maxlength: lengthTwenty
                        }
                        ,
                        vehicleBrand: {
                            maxlength: lengthTwenty
                        }
                        ,
                        remark: {
                            maxlength: publicSize50
                        },
                        registrationRemark: {
                            maxlength: publicSize50
                        }
                        ,
                        registrationRemindDays: {
                            max: integerFour,
                            min:
                            integerFour,
                            digits:
                            naturalNumber
                        }
                        ,
                        brandModel: {
                            maxlength: publicSize20,
                            zysCheck: chEnNumberErr
                        }
                        ,
                        usingNature: {
                            maxlength: publicSize10
                        }
                        ,
                        licenseNo: {
                            maxlength: lengthTwenty
                        }
                        ,
                        managementRemindDays: {
                            max: integerFour,
                            min:
                            integerFour,
                            digits:
                            naturalNumber
                        }
                        ,
                        dailyNumber: {
                            max: integerFour,
                            min:
                            integerFour,
                            digits:
                            naturalNumber
                        }
                        ,
                        destinationStation: {
                            maxlength: lengthTwenty
                        }
                        ,
                        routeEntry: {
                            maxlength: lengthTwenty
                        }
                        ,
                        departure: {
                            maxlength: lengthTwenty
                        }
                        ,
                        destination: {
                            maxlength: lengthTwenty
                        }
                        ,
                        viaName: {
                            maxlength: lengthTwenty
                        }
                        ,
                        provenance: {
                            maxlength: lengthTwenty
                        }
                        ,
                        lineNumber: {
                            maxlength: lengthTwenty
                        }
                        ,
                        issuedAuthority: {
                            maxlength: lengthTwenty
                        }
                        ,
                        scopeBusiness: {
                            maxlength: lengthTwenty
                        }
                        ,
                        vehicleLevel: {
                            maxlength: lengthTwenty
                        }
                        ,
                        loadingQuality: {
                            min: minZero,
                            maxlength:
                            lengthTen
                        }
                        ,
                        numberLoad: {
                            min: integerFour,
                            max:
                            integerFour,
                            digits:
                            naturalNumber
                        }
                        ,
                        vehicleNumber: {
                            maxlength: publicSize20
                        }
                        ,
                        aliases: {
                            maxlength: publicSize20
                        }
                        ,
                        vehicleType: {
                            maxlength: publicSize20
                        }
                        ,
                        chassisNumber: {
                            maxlength: publicSize17,
                            isRightfulStr: enNumberErr
                        }
                        ,
                        engineNumber: {
                            maxlength: publicSize15
                        }
                        ,
                        plateColor: {
                            maxlength: publicSize6
                        }
                        ,
                        areaAttribute: {
                            maxlength: publicSize20
                        }
                        ,
                        province: {
                            maxlength: publicSize20
                        }
                        ,
                        fuelType: {
                            maxlength: publicSize20
                        }
                        ,
                        vehicleOwnerPhone: {
                            isLandline: telPhoneError
                        }
                        ,
                        vehiclePurpose: {
                            maxlength: publicSize50
                        }
                        ,
                        vehiclOperationNumber: {
                            maxlength: publicSize20
                        }
                        ,
                        roadTransportNumber: {
                            maxlength: publicSize24
                        }
                        ,
                        vehicleInsuranceNumber: {
                            maxlength: publicSize50
                        }
                        ,
                        vehicleColor: {
                            maxlength: publicSize6
                        }
                        ,
                        roadTransportValidity: {
                            compareDate: "结束时间必须大于开始时间！"
                        }
                        ,
                        registrationEndDate: {
                            compareDate: "结束时间必须大于开始时间！"
                        }
                    }
                }
            ).form();
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
            $("#error_label_add").is(":hidden");
            $("#error_label_add").hide();
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
            var data = {"vehicleCategory": ""};
            json_ajax("GET", url, "json", false, null, vehicleBatchEdit.categoryCallBack);
        },
        categoryCallBack: function (data) {
            var result = data.obj.result;
            var str = "";
            for (var i = 0; i < result.length; i++) {
                if (result[i].vehicleCategory == '其他车辆') {
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
            }
            else if (standard == '1') {
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
            json_ajax("GET", url, "json", false, null, vehicleBatchEdit.getTypeCallback);
        },
        getTypeCallback: function (data) {
            var vt = '其他车辆';
            var result = data.obj.vehicleTypeList;
            var str = "";
            if (result != null && result != '') {
                for (var i = 0; i < result.length; i++) {
                    if (vt == result[i].vehicleType) {
                        str += '<option selected="selected" value="' + result[i].id + '">' + html2Escape(result[i].vehicleType) + '</option>'
                    } else {
                        str += '<option  value="' + result[i].id + '">' + html2Escape(result[i].vehicleType) + '</option>'
                    }
                }
            }
            $("#vehicleType").html(str);
            vehicleBatchEdit.getVehicleSubType($("#vehicleType").val());
        },
        //获取车辆子类型
        getVehicleSubType: function (id) {
            var url = "/clbs/m/basicinfo/monitoring/vehicle/type/findTypeIsBindingSubType_" + id + ".gsp";
            json_ajax("GET", url, "json", false, null, vehicleBatchEdit.getSubTypeCallback);
        },
        getSubTypeCallback: function (data) {
            var result = data.obj.result;
            var str = "<option value=''>- 请选择车辆子类型 -</option>";
            if (result != null && result != '') {
                for (var i = 0; i < result.length; i++) {
                    str += '<option  value="' + result[i].id + '" driving="' + result[i].drivingWay + '">' + html2Escape(result[i].vehicleSubtypes) + '</option>'
                }
            }
            $("#vehicleSubtypes").html(str);
        },
    };
    $(function () {
        vehicleBatchEdit.init();
        renderPlateColorSelect();
        vehicleBatchEdit.getVehicleCategory();
        $('input').inputClear();
        $("#doSubmitAdd").on("click", vehicleBatchEdit.doSubmit);
        $("#zTreeCitySel").on("click", function () {
            vehicleBatchEdit.showMenu(this)
        });
        $("#isRadio").on("click", vehicleBatchEdit.isRadio);
        $("#noRadio").on("click", vehicleBatchEdit.noRadio);
        $("#radionOn").val($("#isRadio").prop("checked"));
        $("#radionOff").val($("#noRadio").prop("checked"));

        $(".info-span").on("click", vehicleBatchEdit.hiddenparameterFn);


        //按车辆类别对应的标准动态显示信息
        var curStandard = $("#vehicleCategory option:selected").attr("standard");
        vehicleBatchEdit.checkCategoryStandard(curStandard);
        $('#vehicleCategory').change(function () {
            var selectStandard = $(this).find("option:selected").attr("standard");
            vehicleBatchEdit.checkCategoryStandard(selectStandard);
        })

        //获取车辆类型
        var curId = $("#vehicleCategory").val();
        vehicleBatchEdit.getVehicleType(curId);
        $("#vehicleCategory").on("change", function () {
            var curId = $(this).val();
            vehicleBatchEdit.getVehicleType(curId);
            vehicleBatchEdit.selfRespectRequired();
            vehicleBatchEdit.abilityWorkRequired();

            $("#selfRespect-error").hide();
            $("#abilityWork-error").hide();
        })

        //获取车辆子类型
        var curId1 = $("#vehicleType").val();
        vehicleBatchEdit.getVehicleSubType(curId1);
        $("#vehicleType").on("change", function () {
            var curId1 = $(this).val();
            vehicleBatchEdit.getVehicleSubType(curId1);
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
            }
            treeObj.checkAllNodes(false)
        });

    })
})
($, window);