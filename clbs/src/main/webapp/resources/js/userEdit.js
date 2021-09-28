(function ($, window) {
    var isAdminStr = $("#isAdmin").attr("value");//是否是admin
    var isAdmin = isAdminStr === 'true'
    var AuthorizedDeadline = $("#userAuthorizationDate").attr("value");//获取当前用户授权截止日期
    var userName = $("#userName").text();//修改用户窗口弹出时,获取到默认的用户名
    var password = $("#passwordEdit").val();
    var sendDownCommand = $("#sendDownCommand").val();
    var zTreeCitySelEdit = $("#zTreeCitySelEdit");
    var groupName = zTreeCitySelEdit.val();
    var state = $("#state").val();
    var authorizationDate = $("#authorizationDateEdit").val();
    var fullName = $("#fullName").val();
    var gender = $("input[type='radio']:checked").val();
    var mobile = $("#mobile").val();
    var mail = $("#mail").val();
    var identity = $("#identity").val();
    var industry = $("#industry").val();
    var duty = $("#duty").val();
    var administrativeOffice = $("#administrativeOffice").val();
    var flag1 = false;
    var groupIds = $("#groupIds").val;
    var userEdit = {
        //初始化
        init: function () {
            $('#userNameSpan').html($('#userNameHidden').val());
            setTimeout(function () {
                $('#passwordEdit').attr('type', 'password');
                $('#sendDownCommand').attr('type', 'password')
            }, 800);

            var setting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    contentType: "application/json",
                    dataType: "json",
                },
                view: {
                    dblClickExpand: false,
                    nameIsHTML: true,
                    countClass: "group-number-statistics"
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    beforeClick: userEdit.beforeClick,
                    onClick: userEdit.onClick,
                    onAsyncSuccess: userEdit.zTreeOnAsyncSuccess
                }
            };
            $.fn.zTree.init($("#ztreeDemoEdit"), setting, null);
            laydate.render({
                elem: '#authorizationDateEdit',
                theme: '#6dcff6',
                done: function (value) {
                    var stdt = new Date();
                    var etdt = new Date(value.replace("-", "/"));
                    if (stdt <= etdt) {
                        $("#state").val("1");
                        $("#authorizationDate-error").hide();
                    }
                }
            });
            if (!isAdmin) {
                if (AuthorizedDeadline === "null") { // 赋值为空字符串,方便判断
                    AuthorizedDeadline = "";
                }
            } else {
                //为administrator
                if (userName === "admin") {//如果是admin,取消下发口令文本框的隐藏
                    $("#sendAPassWord").removeClass("hidden");
                }
            }
        },
        zTreeOnAsyncSuccess: function (event, treeId) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            treeObj.expandAll(true); // 展开所有节点
        },
        beforeClick: function (treeId, treeNode) {
            return (treeNode);
        },
        onClick: function () {
            var zTree = $.fn.zTree.getZTreeObj("ztreeDemoEdit"), nodes = zTree
                .getSelectedNodes(), v = "";
            var n = "";
            nodes.sort(function compare(a, b) {
                return a.id - b.id;
            });
            for (var i = 0, l = nodes.length; i < l; i++) {
                n += nodes[i].name;
                v += nodes[i].id + ",";
            }
            if (v.length > 0)
                {v = v.substring(0, v.length - 1);}
            var cityObj = zTreeCitySelEdit;
            $("#groupIds").val(v);
            cityObj.val(n);
            $("#zTreeContentEdit").hide();
        },
        showMenu: function (e) {
            // 判断是否是当前用户,不能修改自己的组织 
            var zTreeContentEdit = $("#zTreeContentEdit");
            if (zTreeContentEdit.is(":hidden")) {
                var width = $(e).parent().width();
                zTreeContentEdit.css("width", width + "px");
                $(window).resize(function () {
                    var width = $(e).parent().width();
                    zTreeContentEdit.css("width", width + "px");
                })
                zTreeContentEdit.show();
            } else {
                zTreeContentEdit.hide();
            }
            $("body").bind("mousedown", userEdit.onBodyDown);
        },
        hideMenu: function () {
            $("#zTreeContentEdit").fadeOut("fast");
            $("body").unbind("mousedown", userEdit.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id === "menuBtn" || event.target.id === "zTreeContentEdit" || $(event.target).parents("#zTreeContentEdit").length > 0)) {
                userEdit.hideMenu();
            }
        },
        valueChange: function () { // 判断值是否改变
            var editPassword = $("#passwordEdit").val();
            var editSendDownCommand = $("#sendDownCommand").val();
            var editGroupName = zTreeCitySelEdit.val();
            var editState = $("#state").val();
            var editAuthorizationDate = $("#authorizationDateEdit").val();
            var editFullName = $("#fullName").val();
            var editGender = $("input[type='radio']:checked").val();
            var editMobile = $("#mobile").val();
            var editMail = $("#mail").val();
            var editIdentity = $("#identity").val();
            var editIndustry = $("#industry").val();
            var editDuty = $("#duty").val();
            var editAdministrativeOffice = $("#administrativeOffice").val();
            var editGroupIds = $("#groupIds").val();
            // 值已经发生改变
            if (editIdentity !== identity || editIndustry !== industry || editDuty !== duty || editAdministrativeOffice !== administrativeOffice || password !== editPassword || sendDownCommand !== editSendDownCommand || groupName !== editGroupName || state !== editState
                || authorizationDate !== editAuthorizationDate || fullName !== editFullName || gender !== editGender || mobile !== editMobile || mail !== editMail || editGroupIds !== groupIds) {
                flag1 = true;
            } else { // 表单值没有发生改变
                var timestamp = Date.parse(new Date(editAuthorizationDate));
                timestamp = timestamp / 1000;
                var timestamp2 = Date.parse(new Date(AuthorizedDeadline));
                timestamp2 = timestamp2 / 1000;
                if (timestamp > timestamp2) { // 如果页面获取的授予权截止日期小于等于当前登录用户的授权截止日期,则需要验证
                    flag1 = true;
                    return;
                }
                flag1 = false;
            }
        },
        doSubmit: function () {
            userEdit.valueChange();
            if (flag1) {
                if (userEdit.validates()) {
                    $('#simpleQueryParam').val("");
                    //验证通过后,获取到用户名，与窗口加载时的用户名比较,看用户是否修改过用户名
                    var nowUserName = $("#username").val();
                    if (nowUserName === userName) {
                        //如果没有修改用户名
                        //则重新赋值
                        $("#sign").val("1");
                    }
                    // var password = $("#passwordEdit").val();
                    addHashCode1($("#editForm"));
                    $("#editForm").ajaxSubmit(function (data) {
                        if (data != null) {
                            var result = $.parseJSON(data);
                            if (result.success === true) {
                                // if (password != '') {
                                //     location.href = "/clbs/login?type=changeState";
                                // }
                                if (result.obj.flag === 1) {
                                    // $("#commonWin").modal("hide");
                                    layer.msg(publicEditSuccess, {move: false});
                                    myTable.refresh()
                                } else {
                                    if (date != null) {
                                        layer.msg(publicEditError, {move: false});
                                    }
                                }
                            } else {
                                layer.msg(result.obj.errMsg, {move: false});
                            }
                        }
                        $("#commonLgWin").modal("hide"); // 关闭窗口
                    });
                }
            } else {
                $("#commonLgWin").modal("hide"); // 关闭窗口
            }
        },
        //校验
        validates: function () {
            var editForm = $("#editForm");
            if (isAdmin === true) {
                return editForm.validate({
                    rules: {
                        username: {
                            required: true,
                            stringCheck: true,
                            maxlength: 25
                        },
                        fullName: {
                            maxlength: 20,
                            minlength: 2
                        },
                        password: {
                            minlength: 8,
                            maxlength: 25,
                            checkStrengthEdit: true
                        },
                        sendDownCommand: {
                            minlength: 6,
                            maxlength: 25
                        },
                        groupName: {
                            required: true
                        },
                        authorizationDate: {
                            selectDate: true
                        },
                        mail: {
                            email: true,
                            maxlength: 20
                        },
                        mobile: {
                            isLandline: true,
                            maxlength: 11
                        },
                        socialSecurityNumber: {
                            hasUnderline: true
                        },
                        identityNumber: {
                            isIdCardNo: true
                        }
                    },
                    messages: {
                        socialSecurityNumber: {
                            hasUnderline: '请输入字母、数字或下划线，长度0-20'
                        },
                        username: {
                            required: userNameNull,
                            stringCheck: userNameError,
                            maxlength: publicSize25,
                            /*minSize : userNameMinLength*/
                        },
                        fullName: {
                            required: publicNull,
                            maxlength: publicSize20,
                            minlength: publicMinSize2Length
                        },
                        password: {
                            minlength: passwordMinLength,
                            maxlength: publicSize25,
                            checkStrengthEdit: '密码长度8-25位，且至少包含字母、数字和特殊字符（不含空格）中的两者'
                        },
                        sendDownCommand: {
                            minlength: publicMinSize6Length,
                            maxlength: publicSize25
                        },
                        groupName: {
                            required: publicSelectGroupNull
                        },
                        authorizationDate: {
                            selectDate: usernameAuthorizationToday
                        },
                        mail: {
                            email: emailError,
                            maxlength: publicSize20
                        },
                        mobile: {
                            isLandline: telPhoneError,
                            maxlength:'长度不能超过11位'
                        }
                    }
                }).form();
            }
            return editForm.validate({
                rules: {
                    username: {
                        required: true,
                        stringCheck: true,
                        maxlength: 25
                    },
                    fullName: {
                        // required : true,
                        maxlength: 20,
                        minlength: 2
                    },
                    password: {
                        minlength: 8,
                        maxlength: 25,
                        checkStrengthEdit: true
                    },
                    groupName: {
                        required: true
                    },
                    authorizationDate: {
                        required: true,
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
                    mail: {
                        email: true,
                        maxlength: 20
                    },
                    mobile: {
                        isLandline: true
                    }
                },
                messages: {
                    username: {
                        required: userNameNull,
                        stringCheck: userNameError,
                        maxlength: publicSize25
                    },
                    fullName: {
                        required: publicNull,
                        maxlength: publicSize20,
                        minlength: publicMinSize2Length
                    },
                    password: {
                        minlength: passwordMinLength,
                        maxlength: publicSize25,
                        checkStrengthEdit: '密码长度8-25位，且必须包含大小写字母、数字和特殊字符（不含空格）'
                    },
                    groupName: {
                        required: userGroupSelectNull
                    },
                    authorizationDate: {
                        required: usernameAuthorizationDateNull,
                        selectDate: usernameAuthorizationToday,
                        remote: "该用户的授权截止日期不能大于您自己的授权截止日期(" + AuthorizedDeadline + ")"
                    },
                    mail: {
                        email: emailError,
                        maxlength: publicSize20
                    },
                    mobile: {
                        isLandline: telPhoneError
                    }
                }
            }).form();
        },
        getsTheCurrentTime: function () {
            var nowDate = new Date();
            var month = parseInt(nowDate.getMonth() + 1, 10);
            var startTime = parseInt(nowDate.getFullYear() + 1, 10)
                + "-"
                + (month < 10 ? "0" + month : month)
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate() : nowDate.getDate()) + " ";
            $("#authorizationDateEdit").val(startTime);
        },
    }
    $(function () {
        userEdit.init();
        var input = $('input');
        input.inputClear();
        var userId = $("#currentUserId").val();
        if ($("#userId").val() === userId) {
            zTreeCitySelEdit.attr("disabled", "disabled"); // 禁用选择组织控件
            $("#state").attr("disabled", "disabled"); // 禁用启停状态下拉选
            $("#authorizationDateEdit").attr("disabled", "disabled"); // 禁用选择授权截止日期控件
        } else {
            zTreeCitySelEdit.on("click", function () {
                userEdit.showMenu(this)
            });

            zTreeCitySelEdit.on('input propertychange', function () {
                var treeObj = $.fn.zTree.getZTreeObj("ztreeDemoEdit");
                treeObj.checkAllNodes(false);
                search_ztree('ztreeDemoEdit', 'zTreeCitySelEdit', 'group');
            });

            input.inputClear().on('onClearEvent', function (e, data) {
                var id = data.id;
                var treeObj
                if (id === 'zTreeCitySelEdit') {
                    search_ztree('ztreeDemoEdit', 'zTreeCitySelEdit', 'group');
                    treeObj = $.fn.zTree.getZTreeObj("ztreeDemoEdit");
                }
                treeObj.checkAllNodes(false)
            });
        }
        $("#doSubmitEdit").on("click", userEdit.doSubmit);
    })
}($, window))