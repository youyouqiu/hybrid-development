 (function (window, $) {
    addIntercomplatform = {
        doSubmit: function () {
            addIntercomplatform.hideErrorMsg();
            if ($.trim($("#platformName_add").val()) == "") {
                addIntercomplatform.showErrorMsg("平台名称不能为空！", "platformName_add");
                return;
            }
            if ($("#platformName_add").val().length > 20) {
                addIntercomplatform.showErrorMsg("平台名称请控制在20个字符以内！", "platformName_add");
                return;
            }
            if ($.trim($("#platformIp_add").val()) == "") {
                addIntercomplatform.showErrorMsg("IP地址不能为空！", "platformIp_add");
                return;
            }
            var reg = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/

            if (!reg.test($("#platformIp_add").val())) {
                addIntercomplatform.showErrorMsg("请输入正确的IP地址！", "platformIp_add");
                return;
            }
            if ($.trim($("#platformPort_add").val()) == "") {
                addIntercomplatform.showErrorMsg("端口不能为空！", "platformPort_add");
                return;
            }
            reg = /^[0-9]*$/;
            if (!reg.test($("#platformPort_add").val()) || $("#platformPort_add").val() > 99999) {
                addIntercomplatform.showErrorMsg("请输入正确的端口！", "platformPort_add");
                return;
            }
            if ($("#description_add").val().length > 20) {
                addIntercomplatform.showErrorMsg("描述请控制在20个字符以内！", "description_add");
                return;
            }
            addHashCode1($("#addForm"));
            $("#addForm").ajaxSubmit(function () {
                $("#commonSmWin").modal("hide");
                myTable.requestData();
            });
        },showErrorMsg: function(msg, inputId){
            if ($("#error_label_add").is(":hidden")) {
                $("#error_label_add").text(msg);
                $("#error_label_add").insertAfter($("#" + inputId));
                $("#error_label_add").show();
            } else {
                $("#error_label_add").is(":hidden");
            }
        },
        //错误提示信息隐藏
        hideErrorMsg: function(){
            $("#error_label_add").hide();
        },
    }
    $(function(){
        $('input').inputClear();
        $("#doSubmit").bind("click", addIntercomplatform.doSubmit);
    })
})(window, $)