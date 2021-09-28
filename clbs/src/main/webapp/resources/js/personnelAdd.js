(function (window, $) {
    addPersonnelInfo = {
        init: function () {
            var setting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    contentType: "application/json",
                    dataType: "json",
                    dataFilter: addPersonnelInfo.ajaxDataFilter
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
                    beforeClick: addPersonnelInfo.beforeClick,
                    onClick: addPersonnelInfo.onClick,
                    onAsyncSuccess: addPersonnelInfo.zTreeOnAsyncSuccess
                }
            };
            $.fn.zTree.init($("#ztreeDemo"), setting, null);
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            treeObj.expandAll(true); // 展开所有节点
        },
        beforeClickPermission: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("permissionDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        onClick: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("ztreeDemo"),
                nodes = zTree.getSelectedNodes(),
                v = "";
            n = "";
            nodes.sort(function compare(a, b) {
                return a.id - b.id;
            });
            for (var i = 0, l = nodes.length; i < l; i++) {
                n += nodes[i].name;
                v += nodes[i].uuid + ";";
            }
            if (v.length > 0)
                v = v.substring(0, v.length - 1);
            var cityObj = $("#zTreeCitySel");
            cityObj.val(n);
            $("#selectGroup").val(v);
            $("#zTreeContent").hide();
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            addPersonnelInfo.hideErrorMsg();//清除错误提示样式
            var isAdminStr = $("#isAdmin").attr("value");    // 是否是admin
            var isAdmin = isAdminStr == 'true';
            var userGroupId = $("#userGroupId").attr("value");  // 用户所属组织 id
            var userGroupName = $("#userGroupName").attr("value");  // 用户所属组织 name
            //如果根企业下没有节点,就显示错误提示(根企业下不能人员)
            if (responseData != null && responseData != "" && responseData != undefined && responseData.length >= 1) {
                if ($("#selectGroup").val() == "") {
                    $("#selectGroup").val(responseData[0].uuid);
                    $("#zTreeCitySel").val(responseData[0].name);
                }
                return responseData;
            } else {
                addPersonnelInfo.showErrorMsg("您需要先新增一个组织", "zTreeCitySel");
                return;
            }

        },
        showMenu: function (e) {
            if ($("#zTreeContent").is(":hidden")) {
                var width = $(e).parent().width();
                $("#zTreeContent").css("width", width + "px");
                $(window).resize(function () {
                    var width = $(e).parent().width();
                    $("#zTreeContent").css("width", width + "px");
                })
                $("#zTreeContent").show();
            } else {
                $("#zTreeContent").hide();
            }

            $("body").bind("mousedown", addPersonnelInfo.onBodyDown);
        },
        hideMenu: function () {
            $("#zTreeContent").fadeOut("fast");
            $("body").unbind("mousedown", addPersonnelInfo.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "zTreeContent" || $(
                event.target).parents("#zTreeContent").length > 0)) {
                addPersonnelInfo.hideMenu();
            }
        },
        validates: function () {
            var adminFlag = $('#isAdmin').val() == 'true';
            return $("#addForm").validate({
                rules: {
                    peopleNumber: {
                        required: true,
                        minlength: 2,
                        maxlength: 20,
                        checkRightPeopleNumber: true,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/monitoring/personnel/repetitionAdd",
                            data: {
                                peopleNumber: function () {
                                    return $("#peopleNumber").val();
                                }
                            }
                        }
                    },
                    groupName: {
                        isGroupRequired: adminFlag
                    },
                    name: {
                        checkPeopleName: true
                    },
                    identity: {
                        isIdCardNo: true,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/monitoring/personnel/repetitionIdentity",
                            data: {
                                identity: function () {
                                    return $("#identity").val();
                                }
                            },
                        }
                    },
                    phone: {
                        isTel: true
                    },
                    email: {
                        email: true,
                        maxlength: 20//wjk
                    },
                    remark: {
                        maxlength: 50 //wjk
                    }
                },
                messages: {
                    peopleNumber: {
                        required: '监控对象不能为空',
                        minlength: personnelNumberError,
                        maxlength: personnelNumberError,
                        checkRightPeopleNumber: personnelNumberError,
                        remote: personnelNumberExists
                    },
                    groupName: {
                        isGroupRequired: publicSelectGroupNull
                    },
                    name: {
                        checkPeopleName: PersonnelNameNull
                    },
                    identity: {
                        isIdCardNo: identityError,
                        remote: personnelIdentityExists
                    },
                    phone: {
                        isTel: telPhoneError
                    },
                    email: {
                        email: emailError,
                        maxlength: publicSize20,//wjk
                    },
                    remark: {
                        maxlength: publicSize50,//wjk
                    }
                }
            }).form();
        },
        doSubmits: function () {
            if (addPersonnelInfo.validates()) {
                addHashCode1($("#addForm"));
                $("#addForm").ajaxSubmit(function (data) {
                    var json = eval("(" + data + ")");
                    if (json.success) {
                        $("#commonSmWin").modal("hide");
                        myTable.requestData();
                    } else {
                        layer.msg(json.msg);
                    }
                });
            }
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
            $("#error_label_add").is(":hidden");
            $("#error_label_add").hide();
        },
    }
    $(function () {
        $('input').inputClear();
        addPersonnelInfo.init();
        $("#doSubmits").bind("click", addPersonnelInfo.doSubmits);
        $("#zTreeCitySel").on("click", function () {
            addPersonnelInfo.showMenu(this)
        });
        // 组织树input框的模糊搜索
        $("#zTreeCitySel").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("ztreeDemo");
            treeObj.checkAllNodes(false);
            search_ztree('ztreeDemo', 'zTreeCitySel', 'group');
        });
        // 组织树input框快速清空
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            var treeObj;
            if (id == 'zTreeCitySel') {
                search_ztree('ztreeDemo', 'zTreeCitySel', 'group');
                treeObj = $.fn.zTree.getZTreeObj("ztreeDemo");
            }
            treeObj.checkAllNodes(false)
        });
    })
})(window, $)

jQuery.validator.addMethod("email", function (value, element) {
    var tel = /^(\w)+(\.\w+)*@(\w)+((\.\w{2,3}){1,3})$/;
    return this.optional(element) || (tel.test(value));
}, "您输入的邮箱格式不正确");