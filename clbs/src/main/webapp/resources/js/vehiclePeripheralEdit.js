(function (window, $) {
    editPeripheralsManagement = {
        doSubmits: function () {
            if (!editPeripheralsManagement.validates()) return;
            editPeripheralsManagement.hideErrorMsg();
            if ($.trim($("#peripheralsName").val()) == "") {
                editPeripheralsManagement.showErrorMsg(peripheralNameNull, "peripheralsName");
                return;
            }
            if ($("#peripheralsName").val().length > 25) {
                editPeripheralsManagement.showErrorMsg(peripheralNameLenth, "peripheralsName");
                return;
            }

            if ($.trim($("#peripheralsId").val()) == "") {
                editPeripheralsManagement.showErrorMsg(peripheralIdNull, "peripheralsId");
                return;
            }
            var identId = $("#peripheralsId").val();
            var fdStart = identId.indexOf("0x");
            var fdStart1 = identId.indexOf("0X");
            if (fdStart == 0 || fdStart1 == 0) {

            } else if (fdStart == -1) {
                editPeripheralsManagement.showErrorMsg(inputFormatError, "peripheralsId");
                return;
            }
            if (identId.length < 4 || identId.length > 30) {
                editPeripheralsManagement.showErrorMsg(inputFormatError, "peripheralsId");
                return;
            }
            if (isNaN(identId)) {
                editPeripheralsManagement.showErrorMsg(inputFormatError, "peripheralsId");
                return;
            }
            if ($.trim($("#peripheralsMsgLength").val()) != "") {
                if (parseInt($("#peripheralsMsgLength").val()) != $("#peripheralsMsgLength").val()) {
                    editPeripheralsManagement.showErrorMsg(peripheralMessageLenthInt, "peripheralsMsgLength");
                    return;
                }
                if ($("#peripheralsMsgLength").val() > 255 || $("#peripheralsMsgLength").val() < 0) {
                    editPeripheralsManagement.showErrorMsg(peripheralMessageLenth, "peripheralsMsgLength");
                    return;
                }
            }
            addHashCode($("#editForm"));
            $("#editForm").ajaxSubmit(function (data) {
                data = JSON.parse(data);
                if (data.success) {
                    $("#commonSmWin").modal("hide");
                    layer.msg(data.msg, {move: false});
                    //关闭弹窗
                    myTable.refresh();
                } else {
                    if (data.msg.toString().indexOf("编号") > -1
                        || data.msg.toString().indexOf("ID") > -1) {
                        editPeripheralsManagement.showErrorMsg(data.msg, "peripheralsId");
                    } else if (data.msg.toString().indexOf("名称") > -1) {
                        editPeripheralsManagement.showErrorMsg(data.msg, "peripheralsName");
                    } else {
                        layer.msg(data.msg, {move: false});
                    }
                    return;
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
            $("#error_label_edit").hide();
        },
    }
    $(function () {
        $('input').inputClear();
        $("#doSubmits").bind("click", editPeripheralsManagement.doSubmits);
        setTimeout('$(".delIcon").hide()', 100)

    })
})(window, $)