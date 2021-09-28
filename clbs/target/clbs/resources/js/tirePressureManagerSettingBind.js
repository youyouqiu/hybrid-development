(function (window, $) {
    var vehicleList = JSON.parse($("#referenceList").attr("value"));
    var transduserManage = JSON.parse($("#allSensor").attr("value"));

    //初始化参考车牌
    var dataList = {value: []};
    var TransduserList = {value: []};
    tirePressureSettingBind = {
        init: function () {
            //参考对象下拉
            if (vehicleList != null && vehicleList.length > 0) {
                var vehicleId = $("#vehicleId").attr("value");
                for (var i = 0; i < vehicleList.length; i++) {
                    var obj = {};
                    obj.id = vehicleList[i].vehicleId;
                    obj.name = vehicleList[i].brand;
                    dataList.value.push(obj);
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
                var vId = keyword.id;
                var url = "/clbs/v/tyrepressure/setting/getReferenceInfo";
                var data = {"vehicleId": vId};
                json_ajax("POST", url, "json", false, data, tirePressureSettingBind.consult);     //发送请求
            }).on('onUnsetSelectValue', function () {
            });

            //传感器型号下拉选
            if (transduserManage != null && transduserManage.length > 0) {
                for (var i = 0; i < transduserManage.length; i++) {
                    var sensor = {};
                    sensor.id = transduserManage[i].id;
                    sensor.name = transduserManage[i].sensorNumber;
                    TransduserList.value.push(sensor);
                }
            }
            $("#sensorNumber").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: TransduserList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                var transduserId = keyword.id;
                $("#brands").val("");
                for (var i = 0; i < transduserManage.length; i++) {
                    if (transduserId == transduserManage[i].id) {
                        $("#errorMsg").hide();
                        $("#sensorId").val(transduserManage[i].id);
                        $("#compensate").val(transduserManage[i].compensate);//补偿使能
                        $("#filterFactor").val(transduserManage[i].filterFactor);//滤波系数
                    }
                }
            }).on('onUnsetSelectValue', function () {
            });
        },
        //参考对象
        consult: function (data) {
            var datas = data.obj;
            // $("#vehicleId").val(datas.vehicleId);
            $("#numberOfTires").val(datas.numberOfTires);
            for (var i = 0; i < transduserManage.length; i++) {
                if (datas.sensorId == transduserManage[i].id) {
                    $("#sensorId").val(transduserManage[i].id);
                    $("#sensorNumber").val(transduserManage[i].sensorNumber);
                    $("#compensate").val(transduserManage[i].compensate);//补偿使能
                    $("#filterFactor").val(transduserManage[i].filterFactor);//滤波系数
                }
            }

            var tyrePressureParameter = data.obj.tyrePressureParameter;
            if (tyrePressureParameter != null) {
                $("#normalTirePressure").val(tyrePressureParameter.pressure);
                $("#pressureImbalanceThreshold").val(tyrePressureParameter.pressureThreshold);
                $("#slowLeakThreshold").val(tyrePressureParameter.slowLeakThreshold);
                $("#highTemperatureThreshold").val(tyrePressureParameter.highTemperature);
                $("#lowVoltageThreshold").val(tyrePressureParameter.lowPressure);
                $("#highVoltageThreshold").val(tyrePressureParameter.heighPressure);
                $("#powerAlarmThreshold").val(tyrePressureParameter.electricityThreshold);
                $("#autoTime").val(tyrePressureParameter.automaticUploadTime);
                $("#correctionFactorK").val(tyrePressureParameter.compensationFactorK);
                $("#correctionFactorB").val(tyrePressureParameter.compensationFactorB);
            }
        },
        //提交数据前的效验
        validates: function () {
            return $("#bindForm").validate({
                rules: {
                    sensorNumber: {
                        required: true,
                    },
                    correctionFactorK: {
                        required: true,
                        range: [1, 200],
                        digits: true
                    },
                    correctionFactorB: {
                        required: true,
                        range: [0, 200],
                        digits: true
                    },
                    normalTirePressure: {
                        range: [1.5, 20],
                        number: true
                    },
                    pressureImbalanceThreshold: {
                        range: [0, 100],
                        digits: true
                    },
                    slowLeakThreshold: {
                        range: [0, 100],
                        digits: true
                    },
                    highTemperatureThreshold: {
                        range: [0, 100],
                        digits: true
                    },
                    lowVoltageThreshold: {
                        range: [1.5, 20],
                        number: true
                    },
                    highVoltageThreshold: {
                        range: [1.5, 20],
                        number: true
                    },
                    powerAlarmThreshold: {
                        range: [1, 100],
                        digits: true
                    },
                },
                messages: {
                    sensorNumber: {
                        required: pressureSensorNumberNull,
                    },
                    correctionFactorK: {
                        required: pressureSettingNull,
                        range: pressureSettingNull,
                        digits: pressureSensorNumberNull
                    },
                    correctionFactorB: {
                        required: pressureSettingNull1,
                        range: pressureSettingNull1,
                        digits: pressureSettingNull1
                    },
                    normalTirePressure: {
                        range: pressureSettingNull4,
                        number: pressureSettingNull4,
                    },
                    pressureImbalanceThreshold: {
                        range: pressureSettingNull2,
                        digits: pressureSettingNull2
                    },
                    slowLeakThreshold: {
                        range: pressureSettingNull2,
                        digits: pressureSettingNull2
                    },
                    highTemperatureThreshold: {
                        range: pressureSettingNull2,
                        digits: pressureSettingNull2
                    },
                    lowVoltageThreshold: {
                        range: pressureSettingNull4,
                        number: pressureSettingNull4,
                    },
                    highVoltageThreshold: {
                        range: pressureSettingNull4,
                        number: pressureSettingNull4,
                    },
                    powerAlarmThreshold: {
                        range: pressureSettingNull3,
                        digits: pressureSettingNull3
                    },
                }
            }).form();
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
            $("#error_label_add").hide();
        },
        doSubmit: function () {
            if (!tirePressureSettingBind.validates()) return;
            tirePressureSettingBind.hideErrorMsg();

            var normalTirePressure = parseFloat($('#normalTirePressure').val());// 正常胎压值
            var lowVoltageThreshold = parseFloat($('#lowVoltageThreshold').val());// 低压阈值
            var highVoltageThreshold = parseFloat($('#highVoltageThreshold').val());// 高压阈值
            if (lowVoltageThreshold > normalTirePressure) {
                tirePressureSettingBind.showErrorMsg("低压阈值不能大于正常胎压值，请确认！", 'lowVoltageThreshold');
                return;
            }
            if (highVoltageThreshold < normalTirePressure) {
                tirePressureSettingBind.showErrorMsg("高压阈值不能小于正常胎压值，请确认！", 'highVoltageThreshold');
                return;
            }

            var url = '/clbs/v/tyrepressure/setting/bind';
            var normalTirePressureVal = $("#normalTirePressure").val();
            var lowVoltageThresholdVal = $("#lowVoltageThreshold").val();
            var highVoltageThresholdVal = $("#highVoltageThreshold").val();
            var normalTirePressure = normalTirePressureVal !== undefined && normalTirePressureVal !== null && normalTirePressureVal !== '' ? parseFloat(normalTirePressureVal).toFixed(1) : '',
                lowVoltageThreshold = lowVoltageThresholdVal !== undefined && lowVoltageThresholdVal !== null && lowVoltageThresholdVal !== '' ? parseFloat(lowVoltageThresholdVal).toFixed(1) : '',
                highVoltageThreshold = highVoltageThresholdVal !== undefined && highVoltageThresholdVal !== null && highVoltageThresholdVal !== '' ? parseFloat(highVoltageThresholdVal).toFixed(1) : '';

            var tyrePressureParameterStr = {
                "pressure": normalTirePressure,
                "pressureThreshold": $("#pressureImbalanceThreshold").val(),
                "slowLeakThreshold": $("#slowLeakThreshold").val(),
                "highTemperature": $("#highTemperatureThreshold").val(),
                "lowPressure": lowVoltageThreshold,
                "heighPressure": highVoltageThreshold,
                "electricityThreshold": $("#powerAlarmThreshold").val(),
                "automaticUploadTime": $('#autoTime').val(),
                "compensationFactorK": $('#correctionFactorK').val(),
                "compensationFactorB": $('#correctionFactorB').val(),
            };

            var value = '';
            for(var key in tyrePressureParameterStr){
                value += tyrePressureParameterStr[key];
            }
            value = value.split("").reduce(function (a, b) { a = ((a << 5) - a) + b.charCodeAt(0); return a & a }, 0);

            var data = {
                "vehicleId": $("#vehicleId").val(),
                "numberOfTires": $("#numberOfTires").val(),
                "sensorId": $("#sensorId").val(),
                "tyrePressureParameterStr": JSON.stringify(tyrePressureParameterStr),
                "resubmitToken": value
            };
            address_submit("POST", url, "json", false, data, true, tirePressureSettingBind.callBack);     //发送请求
        },
        callBack: function (data) {
            if (data.success) {
                layer.msg("绑定成功！");
                $("#commonWin").modal("hide");
                myTable.requestData();
            } else {
                layer.msg(data.msg, {move: false});
            }
        },

    }
    $(function () {
        $('input').inputClear();
        tirePressureSettingBind.init();
        $("#doSubmit").bind("click", tirePressureSettingBind.doSubmit);
        $("#lowVoltageThreshold").on("input propertychange", function () {
            $(this).siblings("#error_label_add").hide();
        });
        $("#highVoltageThreshold").on("input propertychange", function () {
            $(this).siblings("#error_label_add").hide();
        });
    })
})(window, $)