(function (window, $) {
    var default_outputCorrectionCoefficientK = 100; // 输出修正系数K默认值
    var default_outputCorrectionCoefficientB = 100; // 输出修正系数B默认值
    var vehicleList = JSON.parse($("#vehicleList").attr("value"));// 车辆list
    var fluxSensorList = JSON.parse($("#fluxSensorList").attr("value"));// 流量传感器list
    var selectFluxSensor = $("#selectFluxSensor").val(); // 选中值
    var dataList = {value: []};// 初始化车辆数据 
    // 初始化流量动传感器 
    var dataFluxList = {value: []};
    editFuelConsumptionVehicle = {
        init: function () {
            // 初始化流量动传感器
            if (fluxSensorList != null && fluxSensorList.length > 0) {
                for (var i = 0; i < fluxSensorList.length; i++) {
                    var flux = {};
                    flux.id = fluxSensorList[i].id;
                    flux.name = fluxSensorList[i].oilWearNumber;
                    dataFluxList.value.push(flux);

                }
            }
            // select change事件
            $("#fluxSensor").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: dataFluxList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                // 当选择参考车牌
                var fluxId = keyword.id;
                for (var i = 0; i < fluxSensorList.length; i++) {
                    if (fluxId == fluxSensorList[i].id) {
                        $("#formFluxSensor").val(fluxSensorList[i].id);
                        $("#compensate").val(fluxSensorList[i].inertiaCompEn);
                        $("#baudRate").val(fluxSensorList[i].baudRate);
                        $("#parity").val(fluxSensorList[i].parity);
                        $("#compensate").val(fluxSensorList[i].inertiaCompEn);
                        $("#filterFactor").val(fluxSensorList[i].filterFactor);
                    }
                }
                $("#error_label_add").hide();
            }).on('onUnsetSelectValue', function () {
            });
            $("#outputCorrectionK").blur(function () {
                editFuelConsumptionVehicle.checkOutputCorrectionCoefficientKModify(this); // 判断当前修正系数是否被修改
            });
            $("#outputCorrectionB").blur(function () {
                editFuelConsumptionVehicle.checkOutputCorrectionCoefficientBModify(this); // 判断当前修正系数是否被修改
            });
            if (vehicleList != null && vehicleList.length > 0) {
                var vehicleId = $("#vehicleId").val();
                for (var i = 0; i < vehicleList.length; i++) {
                    var obj = {};
                    //删除相同车牌信息
                    if (vehicleList[i].vehicleId == vehicleId) {
                        vehicleList.splice(vehicleList[i].vehicleId.indexOf(vehicleId), 1);
                    }
                    //处理参考车牌只存在一条数据时防止浏览器抛出错误信息
                    if (vehicleList[i] == undefined) {
                        dataList.value.push(obj);
                    } else {
                        obj.id = vehicleList[i].id;
                        obj.name = vehicleList[i].brand;
                        dataList.value.push(obj);
                    }
                }
            }
            $("#brands").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: dataList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                // 当选择参考车牌
                var vehicleId = keyword.id;
                for (var i = 0; i < vehicleList.length; i++) {
                    if (vehicleId == vehicleList[i].id) {
                        $("#fluxSensor").attr('data-id', vehicleList[i].oilWearId);
                        $("#fluxSensor").val(vehicleList[i].oilWearNumber);
                        $("#formFluxSensor").val(vehicleList[i].oilWearId);
                        $("#baudRate").val(vehicleList[i].baudRate);
                        $("#parity").val(vehicleList[i].parity);
                        $("#compensate").val(vehicleList[i].inertiaCompEn);
                        $("#filterFactor").val(fluxSensorList[i].filterFactor);
                        $("#autoUploadTime").val(vehicleList[i].autoUploadTime);
                        $("#outputCorrectionK").val(vehicleList[i].outputCorrectionK);
                        $("#outputCorrectionB").val(vehicleList[i].outputCorrectionB);
                    }
                }
            }).on('onUnsetSelectValue', function () {
            });
        },
        // 判断修正系数K是否被修改
        checkOutputCorrectionCoefficientKModify: function (ele) {
            if (ele.value != '' && !isNaN(ele.value) && parseFloat(ele.value) != default_outputCorrectionCoefficientK) {
                layer.confirm("修正系数K值一经修改可能导致传感器工作异常，是否确认修改？",
                    {btn: ['确定', '取消']}, function () {
                        layer.closeAll();
                    }, function () {
                        ele.value = default_outputCorrectionCoefficientK;
                    });
            }
        },
        // 判断修正系数B是否被修改
        checkOutputCorrectionCoefficientBModify: function (ele) {
            if (ele.value != '' && !isNaN(ele.value) && parseFloat(ele.value) != default_outputCorrectionCoefficientB) {
                layer.confirm("修正系数B值一经修改可能导致传感器工作异常，是否确认修改？",
                    {btn: ['确定', '取消']},
                    function () {
                        layer.closeAll();
                    }, function () {
                        ele.value = default_outputCorrectionCoefficientB;
                    });
            }
        },
        // 提交
        doSubmit: function () {
            editFuelConsumptionVehicle.hideErrorMsg();
            var sensorType = $("#fluxSensor").attr("data-id");
            if (sensorType === undefined || sensorType === "") {
                //tg_confirmDialog("系统提示", "传感器型号不能为空！", null, null);
                editFuelConsumptionVehicle.showErrorMsg(sensorNull, "fluxSensor");
            } else {
                if (editFuelConsumptionVehicle.validates()) {
                    addHashCode($("#editForm"));
                    $("#editForm").ajaxSubmit(function (data) {
                        $("#commonWin").modal("hide");
                        if (data != null) {
                            var result = $.parseJSON(data);
                            if (result.success) {
                                layer.msg("修改成功！", {move: false});
                            } else if (result.msg) {
                                layer.msg(result.msg, {move: false});
                            } else {
                                layer.msg('修改失败', {move: false});
                            }
                        }
                        myTable.refresh()
                    });
                }
            }
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
        validates: function () {
            return $("#editForm").validate({
                rules: {
                    vehicleBrand: {
                        required: true,
                    },
                    oilWearId: {
                        required: true,
                    },
                    outputCorrectionK: {
                        isInteger: true,
                        range: [1, 200]
                    },
                    outputCorrectionB: {
                        isInteger: true,
                        range: [0, 200]
                    }
                },
                messages: {
                    vehicleBrand: {
                        required: "不能为空",
                    },
                    oilWearId: {
                        required: "不能为空",
                    },
                    outputCorrectionK: {
                        isInteger: "必须为整数 ",
                        range: "输入值必须介于1到200之间"
                    },
                    outputCorrectionB: {
                        isInteger: "必须为整数 ",
                        range: "输入值必须介于0到200之间"
                    }
                }
            }).form();
        },
    }
    $(function () {
        $('input').inputClear();
        editFuelConsumptionVehicle.init();
        $("#doSubmit").bind("click", editFuelConsumptionVehicle.doSubmit);
    })
})(window, $)