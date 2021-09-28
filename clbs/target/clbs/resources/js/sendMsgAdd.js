var sendMsgAdd;
(function (window, $) {
    var processSubmitFlag = true;// 防止表单重复提交

    sendMsgAdd = {
        doSubmits: function () {
            if (!processSubmitFlag || !sendMsgAdd.validates()) return;
            sendMsgAdd.hideErrorMsg();
            processSubmitFlag = false;
            $("#addForm").ajaxSubmit(function (data) {
                data = JSON.parse(data);
                if (data.success) {
                    $("#commonSmWin").modal("hide");
                    //关闭弹窗
                    myTable.requestData();
                } else {
                    if(data.msg){
                        layer.msg(data.msg);
                    }
                    return;
                }
            });
        },
        /*showErrorMsg: function (msg, inputId) {
            if ($("#error_label_add").is(":hidden")) {
                $("#error_label_add").text(msg);
                $("#error_label_add").insertAfter($("#" + inputId));
                $("#error_label_add").show();
            } else {
                $("#error_label_add").is(":hidden");
            }
        },*/
        validates: function () {
            return $("#addForm").validate({
                rules: {
                    content: {
                        required: true,
                    }
                },
                messages: {
                    content: {
                        required: '消息内容不能为空',
                    }
                }
            }).form();
        },
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#error_label_add").hide();
        },
        //去除textarea输入框首尾空格
        valueTrim: function(){
            var val = $(this).val().trim();
            $(this).val(val);
        }
    }
    $(function () {
        $('input').inputClear();
        $('#addArea').blur(sendMsgAdd.valueTrim);
        $("#doSubmits").bind("click", sendMsgAdd.doSubmits);
    })
})(window, $)