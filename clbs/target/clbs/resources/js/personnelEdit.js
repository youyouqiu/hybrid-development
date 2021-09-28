(function (window, $) {
    editPersonnelInfo = {
        init: function () {
            var bindId = $("#bindId").val();
            if (bindId != null && bindId != '') {
                $("#peopleNumber").attr("readonly", true);
                $("#bindMsg").attr("hidden", false);
            }
            var setting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "vid": ""
                    }
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
                    beforeClick: editPersonnelInfo.beforeClick,
                    onClick: editPersonnelInfo.onClick,
                    onAsyncSuccess: editPersonnelInfo.zTreeOnAsyncSuccess
                }
            };
            $.fn.zTree.init($("#ztreeDemo"), setting, null);
        },
        zTreeOnAsyncSuccess:function(event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            treeObj.expandAll(true); // 展开所有节点
        },
        beforeClick: function (treeId, treeNode) {
            var check = (treeNode);
            return check;
        },
        onClick: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("ztreeDemo"),
                nodes = zTree.getSelectedNodes(),
                n = "";
            v = "";
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
            cityObj.attr("value", v);
            cityObj.val(n);
            $("#groupId").val(v);
            $("#zTreeContent").hide();
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

            $("body").bind("mousedown", editPersonnelInfo.onBodyDown);
        },
        hideMenu: function () {
            $("#zTreeContent").fadeOut("fast");
            $("body").unbind("mousedown", editPersonnelInfo.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "zTreeContent" || $(
                event.target).parents("#zTreeContent").length > 0)) {
                editPersonnelInfo.hideMenu();
            }
        },
        validates: function () {
            var adminFlag = $('#isAdmin').val() == 'true';
            return $("#editForm").validate({
                rules: {
                    peopleNumber: {
                        required: true,
                        minlength: 2,
                        maxlength: 20,
                        checkRightPeopleNumber: true,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/monitoring/personnel/repetitionEdit",
                            dataType: "json",
                            data: {
                                peopleNumber: function () {
                                    return $("#peopleNumber").val();
                                },
                                id: function () {
                                    return $("#id").val();
                                }
                            },
                            dataFilter: function (data, type) {

                                var data2 = data;
                                if (data2 == "true") {
                                    return true;
                                } else {
                                    return false;
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
                            url: "/clbs/m/basicinfo/monitoring/personnel/repetitionIdentityEdit",
                            data: {
                                identity: function () {
                                    return $("#identity").val();
                                },
                                id: function () {
                                    return $("#id").val();
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
            if (editPersonnelInfo.validates()) {
                addHashCode1($("#editForm"));
                $("#editForm").ajaxSubmit(function (data) {
                    var json = eval("(" + data + ")");
                    if (json.success) {
                        $("#commonSmWin").modal("hide");
                        myTable.refresh();
                    } else {
                        layer.msg(json.msg);
                    }
                });
            }
        },
        // 身份证照片显示控制
        identityCardPhotoShow: function () {
          var identityCardPhotoUrl = $('#identityCardPhoto').val();
          if (identityCardPhotoUrl != '') {
              $('#identityCardPhotoGroup').show();
              $('#identityCardImg').attr('src', identityCardPhotoUrl);
          } else {
              $('#identityCardPhotoGroup').hide();
          }
        }
    }
    $(function () {
        $('input').inputClear();
        editPersonnelInfo.identityCardPhotoShow();
        editPersonnelInfo.init();
        $("#zTreeCitySel").on("click", function () {
            editPersonnelInfo.showMenu(this)
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
        $("#doSubmits").bind("click", editPersonnelInfo.doSubmits);
    })
})(window, $)