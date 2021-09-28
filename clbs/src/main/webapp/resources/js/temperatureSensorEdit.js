(function (window, $) {
    editTemperatureManagement = {
        doSubmits: function () {
            if (!editTemperatureManagement.validates()) return;
            editTemperatureManagement.hideErrorMsg();
            if ($.trim($("#oilWearNumber").val()) == "") {
                editTemperatureManagement.showErrorMsg(temperatureSensorNull, "oilWearNumber");
                return;
            }
            if ($("#oilWearNumber").val().length > 25) {
                editTemperatureManagement.showErrorMsg(temperatureSensorTypeLenth, "oilWearNumber");
                return;
            }
            addHashCode($("#editForm"));
            $("#editForm").ajaxSubmit(function (data) {
                data = JSON.parse(data);
                if (data.success) {
                    $("#commonWin").modal("hide");
                    layer.msg("设置成功！", {move: false});
                    //关闭弹窗
                    myTable.refresh()
                } else {
                    if (data.msg.toString().indexOf("型号") > -1) {
                        editTemperatureManagement.showErrorMsg(data.msg, "identId");
                        return;
                    } else if (data.msg == null) {
                        layer.msg("设置失败！", {move: false});
                    } else if (data.msg.toString().indexOf("系统错误") > -1) {
                        layer.msg(data.msg, {move: false});
                    }
                }
            });
        },
        showErrorMsg: function (msg, inputId) {
            if ($("#error_label_edit").is(":hidden")) {
                $("#error_label_edit").text(msg);
                $("#error_label_edit").insertAfter($("#" + inputId));
                $("#error_label_edit").show();
            } else {
                $("#error_label_edit").is(":hidden");
            }
        },
        validates: function () {
            return $("#editForm").validate({
                rules: {
                    sensorNumber: {
                        required: true,
                        maxlength: 25,
                        isRightSensorModel: true,
                    },
                },
                messages: {
                    sensorNumber: {
                        maxlength: sensorModelError,
                        required: temperatureSensorNull
                    },
                }
            }).form();
        },
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#error_label_edit").hide();
        },
    }
    $(function () {
        $('input').inputClear();
        $("#doSubmitsEdit").bind("click", editTemperatureManagement.doSubmits);
    })
})(window, $)