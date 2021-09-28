var sendMsgEdit;
(function (window, $) {
    var status = $('#status').val();
    var processSubmitFlag = true;// 防止表单重复提交

    sendMsgEdit = {
        getStatus:function(){
            var statusRadio = $('input[name="status"]');
            statusRadio.each(function(index, ele){
                var self = $(this);
                self.prop('checked', self.val() == status);
            })
        },
        doSubmits: function () {
            if (!processSubmitFlag || !sendMsgEdit.validates()) return;
            sendMsgEdit.hideErrorMsg();
            processSubmitFlag = false;
            $("#editForm").ajaxSubmit(function (data) {
                data = JSON.parse(data);
                if (data.success) {
                    $("#commonSmWin").modal("hide");
                    if(data.msg){
                        layer.msg(data.msg, {move: false});
                    }
                    //关闭弹窗
                    myTable.refresh();
                } else {
                    if(data.msg){
                        layer.msg(data.msg, {move: false});
                    }
                    return;
                }
            });
        },
        validates: function () {
            return $("#editForm").validate({
                rules: {
                    content: {
                        required: true,
                    },
                },
                messages: {
                    content: {
                        required: '消息内容不能为空',
                    },
                }
            }).form();
        },
        /*showErrorMsg: function (msg, inputId) {
            if ($("#error_label_edit").is(":hidden")) {
                $("#error_label_edit").text(msg);
                $("#error_label_edit").insertAfter($("#" + inputId));
                $("#error_label_edit").show();
            } else {
                $("#error_label_edit").is(":hidden");
            }
        },*/
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#error_label_edit").hide();
        },
        //去除textarea输入框首尾空格
        valueTrim: function(){
            var val = $(this).val().trim();
            $(this).val(val);
        }
    }
    $(function () {
        sendMsgEdit.getStatus();
        $('input').inputClear();
        $('#editArea').blur(sendMsgEdit.valueTrim);
        $("#doSubmits").bind("click", sendMsgEdit.doSubmits);
        setTimeout('$(".delIcon").hide()', 100)
    })
})(window, $)