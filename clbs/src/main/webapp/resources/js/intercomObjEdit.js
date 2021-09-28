(function (window, $) {
    var submissionFlag = false;
    intercomObjEdit = {
        validates: function () {
            return $("#editIntercomForm").validate({
                rules: {
                    devicePassword: {
                        required: true,
                        devicePwd: true,
                    },
                },
                messages: {
                    devicePassword: {
                        required: '请输入设备密码',
                    },
                }
            }).form();
        },
        doSubmit: function () {
            if (intercomObjEdit.validates() && !submissionFlag) {
                submissionFlag = true;
                $("#editIntercomForm").ajaxSubmit(function (data) {
                    submissionFlag = false;
                    var json = eval("(" + data + ")");
                    if (json.success) {
                        $("#commonSmWin").modal("hide");
                        myTable.refresh();
                    } else {
                        layer.msg('修改对讲终端失败');
                    }
                });
            }
        },
    };
    $(function () {
        $('input').inputClear();
        $('#doSubmit').on('click', intercomObjEdit.doSubmit);
    })
})(window, $);