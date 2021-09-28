/**
 * Created by PengFeng on 2017/8/18  9:07
 */
(function (window, $) {
    riskLeveAdd = {
        doSubmit: function () {
            riskLeveAdd.hideErrorMsg();
            if ($.trim($("#riskLevelInput").val()) == "") {
                riskLeveAdd.showErrorMsg("风险等级不能为空！", "riskLevelInput");
                return;
            }
            if ($.trim($("#riskLevelInput").val()) > 40) {
                riskLeveAdd.showErrorMsg("风险等级请控制在40字以内！", "riskLevelInput");
                return;
            }
            if ($("#descInput").val().length > 40) {
                riskLeveAdd.showErrorMsg("说明请控制在40字以内！", "descInput");
                return;
            }
            $("#addForm").ajaxSubmit(function (data) {
                data = JSON.parse(data);
                if (data.success) {
                    $("#commonWin").modal("hide");
                    layer.msg("添加成功！", {move: false});
                    levelTable.requestData();
                } else {
                    if (data.msg.toString().indexOf("系统错误") > -1) {
                        layer.msg(data.msg, {move: false});
                    } else if (data.msg.toString().indexOf("风险等级已存在") > -1) {
                        riskLeveAdd.showErrorMsg(data.msg, "riskLevelInput");
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
        $("#submitAdd").click(riskLeveAdd.doSubmit);
    })
})(window, $)