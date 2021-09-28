(function (window, $) {
    addPeripheralsManagement = {
        doSubmits: function () {
            if (!addPeripheralsManagement.validates()) return;
            addPeripheralsManagement.hideErrorMsg();
            if ($.trim($("#name").val()) == "") {
                addPeripheralsManagement.showErrorMsg(peripheralNameNull, "name");
                return;
            }
            if ($("#name").val().length > 25) {
                addPeripheralsManagement.showErrorMsg(peripheralNameLenth, "name");
                return;
            }

            if ($.trim($("#identId").val()) == "") {
                addPeripheralsManagement.showErrorMsg(peripheralIdNull, "identId");
                return;
            }
            if ($("#identId").val().length > 30) {
                addPeripheralsManagement.showErrorMsg(inputFormatError, "identId");
                return;
            }
            var identId = $("#identId").val();
            var fdStart = identId.indexOf("0x");
            var fdStart1 = identId.indexOf("0X");
            if (fdStart == 0 || fdStart1 == 0) {

            } else if (fdStart == -1) {
                addPeripheralsManagement.showErrorMsg(inputFormatError, "identId");
                return;
            }
            if (identId.length < 4 || identId.length > 30) {
                addPeripheralsManagement.showErrorMsg(inputFormatError, "identId");
                return;
            }
            if (isNaN(identId)) {
                addPeripheralsManagement.showErrorMsg(inputFormatError, "identId");
                return;
            }
            if ($.trim($("#msgLength").val()) != "") {
                if (parseInt($("#msgLength").val()) != $("#msgLength").val()) {
                    addPeripheralsManagement.showErrorMsg(peripheralMessageLenthInt, "msgLength");
                    return;
                }
                if ($("#msgLength").val() > 255 || $("#msgLength").val() < 0) {
                    addPeripheralsManagement.showErrorMsg(peripheralMessageLenth, "msgLength");
                    return;
                }
            }

            addHashCode($("#addForm"));
            $("#addForm").ajaxSubmit(function (data) {
                data = JSON.parse(data);
                if (data.success) {
                    $("#commonSmWin").modal("hide");
                    layer.msg(data.msg, {move: false});
                    //关闭弹窗
                    myTable.requestData();
                } else {
                    if (data.msg.toString().indexOf("编号") > -1
                        || data.msg.toString().indexOf("ID") > -1) {
                        addPeripheralsManagement.showErrorMsg(data.msg, "identId");
                    } else if (data.msg.toString().indexOf("名称") > -1) {
                        addPeripheralsManagement.showErrorMsg(data.msg, "name");
                    } else {
                        layer.msg(data.msg, {move: false});
                    }
                    return;
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
                    name: {
                        required: true,
                        maxlength: 25,
                        isRightSensorModel: true,
                    }
                },
                messages: {
                    name: {
                        required: peripheralNameNull,
                        maxlength: sensorModelError,
                    }
                }
            }).form();
        },
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#error_label_add").hide();
        },
    }
    $(function () {
        $('input').inputClear();
        $("#doSubmits").bind("click", addPeripheralsManagement.doSubmits);
    })
})(window, $)