(function (window, $) {
    editTirePressureManagement = {
        doSubmits: function () {
            if (!editTirePressureManagement.validates()) return;
            editTirePressureManagement.hideErrorMsg();
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
                        addTirePressureManagement.showErrorMsg(data.msg, "identId");
                        return;
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
                        isRightSensorModel: true,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/v/tyrepressure/sensor/check",
                            data: {
                                name: function(){
                                    return $("#sensorNumber").val();
                                },
                                id: function(){
                                    return $("#sensorTypeId").val();
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
            $("#error_label_edit").hide();
        },
    }
    $(function () {
        $('input').inputClear();
        $("#doSubmitsEdit").bind("click", editTirePressureManagement.doSubmits);
    })
})(window, $)