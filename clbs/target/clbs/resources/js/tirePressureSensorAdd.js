(function (window, $) {
    addTirePressureManagement = {
        doSubmits: function () {
            if (!addTirePressureManagement.validates()) return;
            addTirePressureManagement.hideErrorMsg();
            addHashCode($("#addForm"));
            $("#addForm").ajaxSubmit(function (data) {
                data = JSON.parse(data);
                if (data.success) {
                    $("#commonWin").modal("hide");
                    layer.msg("添加成功！", {move: false});
                    //关闭弹窗
                    myTable.requestData()
                } else {
                    if (data.msg.toString().indexOf("型号") > -1) {
                        addTirePressureManagement.showErrorMsg(data.msg, "identId");
                        return;
                    } else if (data.msg.toString().indexOf("系统错误") > -1) {
                        layer.msg(data.msg, {move: false});
                    }
                }
            });
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
        validates: function () {
            return $("#addForm").validate({
                rules: {
                    sensorNumber: {
                        required: true,
                        isRightSensorModel: true,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/v/tyrepressure/sensor/check",
                            data: {
                                name: function () {
                                    return $("#sensorNumber").val();
                                }
                            },
                        }
                    },
                },
                messages: {
                    sensorNumber: {
                        required: pressureSensorNull,
                        remote: pressureSensorExit
                    },
                }
            }).form();
        },
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#error_label_add").hide();
        }
    }
    $(function () {
        $('input').inputClear();
        $("#doSubmitsAdd").bind("click", addTirePressureManagement.doSubmits);
    })
})(window, $)