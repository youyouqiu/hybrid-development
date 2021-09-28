/**
 * Created by PengFeng on 2017/8/18  9:07
 */
(function (window, $) {
    riskLeveEdit = {
        doSubmit: function () {
            riskLeveEdit.hideErrorMsg();
            if ($.trim($("#riskLevelInput").val()) == "") {
                riskLeveEdit.showErrorMsg(riskTypeNull, "riskLevelInput");
                return;
            }
            if ($.trim($("#riskLevelInput").val()).length > 40) {
                riskLeveEdit.showErrorMsg(riskTypeSize40, "riskLevelInput");
                return;
            }
            if ($("#descInput").val().length > 40) {
                riskLeveEdit.showErrorMsg(riskTypeRemark40, "descInput");
                return;
            }
            $("#editForm").ajaxSubmit(function (data) {
                data = JSON.parse(data);
                if (data.success) {
                    $("#commonWin").modal("hide");
                    layer.msg("修改成功！", {move: false});
                    levelTable.refresh();
                } else {
                    if (data.msg.toString().indexOf("系统错误") > -1) {
                        layer.msg(data.msg, {move: false});
                    } else if (data.msg.toString().indexOf("风险等级已存在") > -1) {
                        riskLeveEdit.showErrorMsg(data.msg, "riskLevelInput");
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
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#error_label_add").hide();
        }
    }
    $(function () {
        $('input').inputClear();
        $("#submitEdit").click(riskLeveEdit.doSubmit);
    })
})(window, $)