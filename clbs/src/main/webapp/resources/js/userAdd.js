(function ($, window) {
    var isAdminStr = $("#isAdmin").attr("value"); // 是否是admin
    var AuthorizedDeadline = $("#userAuthorizationDate").attr("value");//获取当前用户授权截止日期
    var isAdmin = isAdminStr === 'true';
    var zTreeCitySelAdd = $("#zTreeCitySelAdd");
    var userAdd = {
        init: function () {
            setTimeout(function () {
                $('#passwordAdd').attr('type', 'password');
                $('#password1').attr('type', 'password');
            }, 800);
            var setting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    dataFilter: userAdd.ajaxDataFilter
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
                    beforeClick: userAdd.beforeClick,
                    onClick: userAdd.onClick,
                    onAsyncSuccess: userAdd.zTreeOnAsyncSuccess,
                }
            };
            $.fn.zTree.init($("#ztreeDemoAdd"), setting, null);
            laydate.render({
                elem: '#authorizationDateAdd',
                theme: '#6dcff6',
                done: function () {
                    $("#authorizationDate-error").hide();
                }
            });
            if (!isAdmin) {
                if (AuthorizedDeadline === "null") {
                    AuthorizedDeadline = "";
                }
                if (AuthorizedDeadline != null && AuthorizedDeadline !== "" && AuthorizedDeadline !== "null") {//如果用户有授权截止日器,改变页面日期的值为当前用户的授权截止日期
                    $("#authorizationDateAdd").val(AuthorizedDeadline);
                } else {//如果用户没有授权截止日期,则更改页面时间为当天日期+1年
                    userAdd.getsTheCurrentTime();
                }
                userAdd.fulatAdminValidates();
            } else {
                userAdd.getsTheCurrentTime();
                userAdd.validates();
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
            var zTree = $.fn.zTree.getZTreeObj("ztreeDemoAdd"), nodes = zTree
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
            var cityObj = zTreeCitySelAdd;

            $("#groupId").val(v);
            cityObj.val(n);
            $("#zTreeContentAdd").hide();
        },
        showMenu: function (e) {
            var zTreeContentAdd = $("#zTreeContentAdd");
            if (zTreeContentAdd.is(":hidden")) {
                var width = $(e).parent().width();
                zTreeContentAdd.css("width", width + "px");
                $(window).resize(function () {
                    var width = $(e).parent().width();
                    zTreeContentAdd.css("width", width + "px");
                })
                zTreeContentAdd.show();
            } else {
                zTreeContentAdd.hide();
            }

            $("body").bind("mousedown", userAdd.onBodyDown);
        },
        hideMenu: function () {
            $("#zTreeContentAdd").fadeOut("fast");
            $("body").unbind("mousedown", userAdd.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id === "menuBtn" || event.target.id === "zTreeContentAdd" || $(event.target).parents("#zTreeContentAdd").length > 0)) {
                userAdd.hideMenu();
            }
        },
        //组织树预处理函数
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            userAdd.hideErrorMsg();//清除错误提示样式
            //如果根企业下没有节点,就显示错误提示(根企业下不能新建用户)
            if (responseData !== null && responseData !== undefined && responseData !== "" && responseData.length >= 1) {
                var groupId = $("#groupId");
                if (groupId.val() === "") {
                    groupId.val(responseData[0].id);
                    zTreeCitySelAdd.val(responseData[0].name);
                }
                return responseData;
            }
            userAdd.showErrorMsg(userGroupNull, "zTreeCitySelAdd");
            return {};
        },
        // 提交
        doSubmit: function () {
            var sexvalue = $("input[name='sex']:checked").val();
            $("#gender").val(sexvalue);
            if (isAdmin === true) {
                if (userAdd.validates()) {
                    addHashCode1($("#addForm"));
                    $("#addForm").ajaxSubmit(function (data) {
                        if (data != null) {
                            var result = $.parseJSON(data);
                            if (result.success) {
                                if (result.obj.flag === 1) {
                                    $("#commonLgWin").modal("hide");
                                    layer.msg(publicAddSuccess, {move: false});
                                    myTable.requestData();
                                } else {
                                    layer.msg(result.msg, {move: false});
                                }
                            } else {
                                layer.msg(result.msg, {move: false});
                            }
                        }
                    });
                }
            } else {
                if (userAdd.fulatAdminValidates()) {
                    addHashCode1($("#addForm"));
                    $("#addForm").ajaxSubmit(function (data) {
                        if (data != null) {
                            var result = $.parseJSON(data);
                            if (result.success) {
                                if (result.obj.flag === 1) {
                                    $("#commonLgWin").modal("hide");
                                    layer.msg(publicAddSuccess, {move: false});
                                    myTable.requestData();
                                } else {
                                    layer.msg(result.msg, {move: false});
                                }
                            } else {
                                layer.msg(result.msg, {move: false});//result.obj.errMsg
                            }
                        }
                    });
                }
            }
        },
        //校验
        validates: function () {
            return $("#addForm").validate({
                rules: {
                    username: {
                        required: true,
                        stringCheck: true,
                        maxlength: 25,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/c/user/verifyUserName",
                            data: {
                                userName: function () {
                                    return $("#usernameAdd").val();
                                }
                            }
                        }
                    },
                    fullName: {
                        maxlength: 20,
                        minlength: 2
                    },
                    password: {
                        required: true,
                        minlength: 8,
                        maxlength: 25,
                        checkStrength: true
                    },
                    password1: {
                        required: true,
                        minlength: 8,
                        maxlength: 25,
                        equalTo: "#passwordAdd",
                        checkStrength: true
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
                        isLandline: true
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
                        /*minlength: userNameMinLength,*/
                        remote: usernameExists
                    },
                    fullName: {
                        maxlength: publicSize20,
                        minlength: userNameMixlength
                    },
                    password: {
                        required: passWordNull,
                        maxlength: publicSize25,
                        minlength: passwordMinLength,
                        checkStrength: '密码必须包含字母、数字和特殊字符（不含空格）中的两者'
                    },
                    password1: {
                        required: passWordNull,
                        minlength: passwordMinLength,
                        maxlength: publicSize25,
                        equalTo: passwordCompareNull,
                        checkStrength: '密码必须包含字母、数字和特殊字符（不含空格）中的两者'
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
                        isLandline: telPhoneError
                    }
                }
            }).form();
        },
        fulatAdminValidates: function () {
            return $("#addForm").validate({
                rules: {
                    username: {
                        required: true,
                        stringCheck: true,
                        maxlength: 25,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/c/user/verifyUserName",
                            data: {
                                userName: function () {
                                    return $("#usernameAdd").val();
                                }
                            },
                        }
                    },
                    fullName: {
                        maxlength: 20,
                        minlength: 2
                    },
                    password: {
                        required: true,
                        minlength: 8,
                        maxlength: 25,
                        checkStrength: true
                    },
                    password1: {
                        required: true,
                        minlength: 8,
                        equalTo: "#passwordAdd",
                        checkStrength: true
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
                                    return $("#authorizationDateAdd").val();
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
                        /*minlength: userNameMinLength,*/
                        remote: usernameExists
                    },
                    fullName: {
                        maxlength: publicSize20,
                        minlength: publicMinSize2Length
                    },
                    password: {
                        required: passWordNull,
                        maxlength: publicSize25,
                        minlength: passwordMinLength,
                        checkStrength: '密码必须包含字母、数字和特殊字符（不含空格）中的两者'
                    },
                    password1: {
                        required: passWordNull,
                        minlength: passwordMinLength,
                        maxlength: publicSize25,
                        equalTo: passwordCompareNull,
                        checkStrength: '密码必须包含字母、数字和特殊字符（不含空格）中的两者'
                    },
                    groupName: {
                        required: publicSelectGroupNull
                    },
                    authorizationDate: {
                        required: usernameAuthorizationDateNull,
                        selectDate: usernameAuthorizationToday,
                        remote: "新建用户的授权截止日期不能大于您自己的授权截止日期(" + AuthorizedDeadline + ")"
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
            var authorizationDateAdd = $("#authorizationDateAdd");
            var time = authorizationDateAdd.val();
            if (time == null || time === "" || time === "null") {
                var nowDate = new Date();
                var month = parseInt(nowDate.getMonth() + 1, 10);
                var date = nowDate.getDate();
                var startTime = parseInt(nowDate.getFullYear() + 1, 10)
                    + "-"
                    + (month < 10 ? "0" + month : month)
                    + "-"
                    + (date < 10 ? "0" + date : date) + " ";
                authorizationDateAdd.val(startTime);
            }
        },
        showErrorMsg: function (msg, inputId) {
            var errorLabelAdd = $("#error_label_add");
            if (errorLabelAdd.is(":hidden")) {
                errorLabelAdd.text(msg);
                errorLabelAdd.insertAfter($("#" + inputId));
                errorLabelAdd.show();
            } else {
                errorLabelAdd.is(":hidden");
            }
        },
        //错误提示信息隐藏
        hideErrorMsg: function () {
            var errorLabelAdd = $("#error_label_add");
            errorLabelAdd.is(":hidden");
            errorLabelAdd.hide();
        },
        closeErrClass: function () {
            userAdd.hideErrorMsg();
        },
        clearPreviousValue: function () { //清除Validates验证缓存
            var preValue = $(".remote").data("previousValue");
            if (preValue) {
                preValue.old = null;
            }
        },
    }
    $(function () {
        userAdd.init();
        $("#doSubmitAdd").on("click", userAdd.doSubmit);
        $("#closeAdd").on("click", userAdd.closeErrClass);
        $("#usernameAdd").on("change", userAdd.clearPreviousValue);
        zTreeCitySelAdd.on("click", function () {
            userAdd.showMenu(this)
        });
        zTreeCitySelAdd.on('input propertychange', function () {
            var treeObj = $.fn.zTree.getZTreeObj("ztreeDemoAdd");
            treeObj.checkAllNodes(false)
            search_ztree('ztreeDemoAdd', 'zTreeCitySelAdd', 'group');
        });

        var input = $('input');
        input.inputClear();
        input.inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            var treeObj
            if (id === 'zTreeCitySelAdd') {
                search_ztree('ztreeDemoAdd', 'zTreeCitySelAdd', 'group');
                treeObj = $.fn.zTree.getZTreeObj("ztreeDemoAdd");
            }
            treeObj.checkAllNodes(false)
        });
    })
}($, window))