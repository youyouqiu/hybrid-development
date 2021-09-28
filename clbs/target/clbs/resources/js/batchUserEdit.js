(function ($, window) {
    var flag1 = true;
    var AuthorizedDeadline = $("#userAuthorizationDate").attr("value");//获取当前用户授权截止日期
    var batchUserEdit = {
        //初始化
        init: function () {
            var currentName = checkUserObj.name.join(',');
            var userNames = checkUserObj.name.join(';');
            var userIds = checkUserObj.id.join(';');
            console.log('checkUserObj', currentName, checkUserObj);
            $('#currentName').val(currentName).attr('title', currentName);
            $('#userIds').val(userIds);
            $('#userNames').val(userNames);

            laydate.render({
                elem: '#authorizationDateEdit',
                theme: '#6dcff6',
                done: function (value, date, endDate) {
                    var stdt = new Date();
                    var etdt = new Date(value.replace("-", "/"));
                    if (stdt <= etdt) {
                        $("#state").val("1");
                        $("#authorizationDate-error").hide();
                    }
                }
            });
        },
        doSubmit: function () {
            if (flag1) {
                if (batchUserEdit.validates()) {
                    $("#batchEditForm").ajaxSubmit(function (data) {
                        flag1 = false;
                        var result = JSON.parse(data);
                        if (result.success) {
                            layer.msg('修改成功');
                            myTable.refresh();
                        } else if (result.msg) {
                            flag1 = true;
                            layer.msg(result.msg);
                        }
                    });
                    $("#commonLgWin").modal("hide"); // 关闭窗口
                }
            }
        },
        //校验
        validates: function () {
            return $("#batchEditForm").validate({
                rules: {
                    password: {
                        minlength: 8,
                        maxlength: 25,
                        checkStrengthEdit: true
                    },
                    authorizationDate: {
                        selectDate: true,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/c/user/verification",
                            data: {
                                authorizationDate: function () {
                                    return $("#authorizationDateEdit").val();
                                }
                            }
                        }
                    },
                },
                messages: {
                    password: {
                        minlength: passwordMinLength,
                        maxlength: publicSize25,
                        checkStrengthEdit: '密码必须包含字母、数字和特殊字符（不含空格）中的两者'
                    },
                    authorizationDate: {
                        selectDate: '授权截止日期必须大于/等于今天',
                        remote: "该用户的授权截止日期不能大于您自己的授权截止日期(" + AuthorizedDeadline + ")"
                    },
                }
            }).form();
        },
    };
    $(function () {
        batchUserEdit.init();
        $('input').inputClear();
        $("#doSubmitEdit").on("click", batchUserEdit.doSubmit);
    })
}($, window));