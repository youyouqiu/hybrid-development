(function (window, $) {
    var submissionFlag = false;
    var deviceNumber = $("#deviceNumber").val();
    var deviceType = $("#deviceType").val();
    var deviceNumberError = $("#deviceNumber-error");
    var deviceFlag = false;
    addTerminalManagement = {
        init: function () {
            var setting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    contentType: "application/json",
                    dataType: "json",
                    dataFilter: addTerminalManagement.ajaxDataFilter
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
                    beforeClick: addTerminalManagement.beforeClick,
                    onClick: addTerminalManagement.onClick,
                    onAsyncSuccess: addTerminalManagement.zTreeOnAsyncSuccess
                }
            };
            $.fn.zTree.init($("#ztreeDemo"), setting, null);
            laydate.render({elem: '#installDate', theme: '#6dcff6'});
            laydate.render({elem: '#procurementDate', theme: '#6dcff6'});
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            treeObj.expandAll(true); // 展开所有节点
        },
        beforeClick: function (treeId, treeNode) {
            var check = (treeNode);
            return check;
        },
        onClick: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("ztreeDemo"), nodes = zTree
                .getSelectedNodes(), v = "";
            n = "";
            nodes.sort(function compare(a, b) {
                return a.id - b.id;
            });
            for (var i = 0, l = nodes.length; i < l; i++) {
                n += nodes[i].name;
                v += nodes[i].uuid + ",";
            }
            if (v.length > 0)
                v = v.substring(0, v.length - 1);
            var cityObj = $("#zTreeCitySel");
            cityObj.val(n);
            $("#groupId").val(v);
            $("#zTreeContent").hide();
        },
        //显示菜单
        showMenu: function () {
            if ($("#zTreeContent").is(":hidden")) {
                var inpwidth = $("#zTreeCitySel").width();
                var spwidth = $("#zTreeCitySelSpan").width();
                var allWidth = inpwidth + spwidth + 21;
                if (navigator.appName == "Microsoft Internet Explorer") {
                    $("#zTreeContent").css("width", (inpwidth + 7) + "px");
                } else {
                    $("#zTreeContent").css("width", allWidth + "px");
                }
                $(window).resize(function () {
                    var inpwidth = $("#zTreeCitySel").width();
                    var spwidth = $("#zTreeCitySelSpan").width();
                    var allWidth = inpwidth + spwidth + 21;
                    if (navigator.appName == "Microsoft Internet Explorer") {
                        $("#zTreeContent").css("width", (inpwidth + 7) + "px");
                    } else {
                        $("#zTreeContent").css("width", allWidth + "px");
                    }
                })
                $("#zTreeContent").show();
            } else {
                $("#zTreeContent").hide();
            }
            $("body").bind("mousedown", addTerminalManagement.onBodyDown);
        },
        //隐藏菜单
        hideMenu: function () {
            $("#zTreeContent").fadeOut("fast");
            $("body").unbind("mousedown", addTerminalManagement.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "zTreeContent" || $(
                event.target).parents("#zTreeContent").length > 0)) {
                addTerminalManagement.hideMenu();
            }
        },
        //组织树预处理函数
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            addTerminalManagement.hideErrorMsg();//隐藏错误提示样式
            var isAdminStr = $("#isAdmin").attr("value");    // 是否是admin
            var isAdmin = isAdminStr == 'true';
            var userGroupId = $("#userGroupId").attr("value");  // 用户所属组织 id
            var userGroupName = $("#userGroupName").attr("value");  // 用户所属组织 name
            //如果根企业下没有节点,就显示错误提示(根企业下不能创建终端)
            if (responseData != null && responseData != "" && responseData != undefined && responseData.length >= 1) {
                if (!isAdmin) { // 不是admin，默认组织为当前组织
                    $("#groupId").val(userGroupId);
                    $("#zTreeCitySel").val(userGroupName);
                } else { // admin，默认组织为树结构第一个组织
                    $("#groupId").val(responseData[0].uuid);
                    $("#zTreeCitySel").val(responseData[0].name);
                }
                return responseData;
            } else {
                addTerminalManagement.showErrorMsg("您需要先新增一个组织", "zTreeCitySel");
                return;
            }
        },
        doSubmit: function () {
            if (submissionFlag) {  // 防止重复提交
                return;
            } else {
                deviceType = $("#deviceType").val();
                deviceNumber = $("#deviceNumber").val();
                // addTerminalManagement.deviceNumberValidates();
                if (addTerminalManagement.validates()) {
                    submissionFlag = true;
                    addHashCode1($("#addForm"));
                    $("#addForm").ajaxSubmit(function (data) {
                        var json = eval("(" + data + ")");
                        if (json.success) {
                            $("#commonWin").modal("hide");
                            myTable.requestData();
                        } else {
                            layer.msg(json.msg);
                        }
                    });
                }
            }
        },
        deviceNumberValidates: function () {
            // if (deviceType == "5") {
            //     var regName = /^(?=.*[0-9a-zA-Z])[0-9a-zA-Z]{1,20}$/;
            //     if (deviceNumber != "" && !regName.test(deviceNumber)) {
            //         deviceNumberError.html("请输入字母/数字，范围（车）7~15（人）1~20");
            //         deviceNumberError.show();
            //         deviceFlag = false;
            //     }
            //     else if (deviceNumber == "") {
            //         deviceNumberError.html("请输入终端号，范围：1~20");
            //         deviceNumberError.show();
            //         deviceFlag = false;
            //     }
            //     else {
            //         addTerminalManagement.deviceAjax();
            //     }
            // }
            // else {
            //     var regName = /^(?=.*[0-9a-zA-Z])[0-9a-zA-Z]{7,15}$/;
            //     if (deviceNumber != "" && !regName.test(deviceNumber)) {
            //         deviceNumberError.html("请输入字母/数字，范围（车）7~15（人）1~20");
            //         deviceNumberError.show();
            //         deviceFlag = false;
            //     }
            //     else if (deviceNumber == "") {
            //         deviceNumberError.html("请输入终端号，范围：7~15");
            //         deviceNumberError.show();
            //         deviceFlag = false;
            //     }
            //     else {
            //         addTerminalManagement.deviceAjax();
            //     }
            // }
            var regName = /^(?=.*[0-9a-zA-Z])[0-9a-zA-Z]{7,30}$/;
            if (deviceNumber != "" && !regName.test(deviceNumber)) {
                deviceNumberError.html(deviceNumberError2);
                deviceNumberError.show();
                deviceFlag = false;
            } else if (deviceNumber == "") {
                deviceNumberError.html(deviceNumberNull);
                deviceNumberError.show();
                deviceFlag = false;
            } else {
                addTerminalManagement.deviceAjax();
            }
        },
        validates: function () {
            return $("#addForm").validate({
                rules: {
                    deviceNumber: {
                        required: true,
                        checkDeviceNumber: "#deviceType",
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/equipment/device/repetition",
                            data: {
                                username: function () {
                                    return $("#deviceNumber").val();
                                }
                            }
                        }
                    },
                    groupName: {
                        required: true
                    },
                    deviceType: {
                        required: true,
                        maxlength: 50
                    },
                    terminalManufacturer: {
                        required: true,
                    },
                    terminalType: {
                        required: true,
                    },
                    functionalType: {
                        required: true,
                        maxlength: 50
                    },
                    deviceName: {
                        required: false,
                        maxlength: 50
                    },
                    barCode: {
                        maxlength: 64
                    },
                    isStart: {
                        required: false,
                        maxlength: 6
                    },
                    manuFacturer: {
                        maxlength: 100
                    },
                    remark: {
                        required: false,
                        maxlength: 50
                    },
                    manufacturerId: {
                        maxlength: 11
                    },
                    deviceModelNumber: {
                        maxlength: 30
                    },
                    macAddress: {
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/equipment/device/repetitionMacAddress",
                            data: {
                                macAddress: function () {
                                    return $("#macAddress").val();
                                }
                            },
                            dataFilter: function (data) {
                                return JSON.parse(data).success;
                            }
                        },
                        isMacAddress: true
                    },
                },
                messages: {
                    macAddress: {
                        remote: '该MAC地址已存在'
                    },
                    deviceNumber: {
                        required: deviceNumberNull,
                        checkDeviceNumber: deviceNumberError2,
                        remote: deviceNumberExists
                    },
                    groupName: {
                        required: publicNull
                    },
                    deviceType: {
                        required: deviceTypeNull,
                        maxlength: publicSize50
                    },
                    terminalManufacturer: {
                        required: "请选择终端厂商",
                    },
                    terminalType: {
                        required: "请选择终端型号",
                    },
                    functionalType: {
                        required: publicNull,
                        maxlength: publicSize50
                    },
                    deviceName: {
                        required: publicNull,
                        maxlength: publicSize50
                    },
                    barCode: {
                        maxlength: publicSize64
                    },
                    isStart: {
                        required: publicNull,
                        maxlength: publicSize6
                    },
                    manuFacturer: {
                        required: publicNull,
                        maxlength: publicSize100
                    },
                    remark: {
                        maxlength: publicSize50
                    },
                    manufacturerId: {
                        maxlength: '长度不超过11位'
                    },
                    deviceModelNumber: {
                        maxlength: publicSize30
                    }
                },
                submitHandler: function (form) {
                    var typeVal = $("#deviceType").val();
                    var deviceNumber = $("#deviceNumber").val();
                    if (typeVal == "5") {
                        $("#deviceNumber-error").html("请输入终端号，范围：1~20");
                        var reg = /^(?=.*[0-9a-zA-Z])[0-9a-zA-Z-]{1,20}$/;
                        if (!reg.test(deviceNumber)) {
                            alert("请输入终端号，范围：1~20");
                            return;
                        }
                    }
                }
            }).form();
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
        deviceAjax: function () {
            $.ajax({
                    type: "post",
                    url: "/clbs/m/basicinfo/equipment/device/repetition",
                    data: {deviceNumber: deviceNumber},
                    success: function (d) {
                        var result = $.parseJSON(d);
                        if (!result) {
                            deviceNumberError.html("终端号已存在！");
                            deviceNumberError.show();
                            deviceFlag = false;
                        }
                        else {
                            deviceNumberError.hide();
                            deviceFlag = true;
                        }
                    }
                }
            )
        },
        getTerminalManufacturer: function () {
            var url = "/clbs/m/basicinfo/equipment/device/TerminalManufacturer";
            json_ajax("GET", url, "json", false, null, addTerminalManagement.TerminalManufacturerCallBack);
        }
        ,
        TerminalManufacturerCallBack: function (data) {
            var result = data.obj.result;
            var str = "";
            for (var i = 0; i < result.length; i++) {
                if (result[i] == '[f]F3') {
                    str += '<option selected="selected" value="' + result[i] + '">' + html2Escape(result[i]) + '</option>'
                } else {
                    str += '<option  value="' + result[i] + '">' + html2Escape(result[i]) + '</option>'
                }
            }
            $("#terminalManufacturer").html(str);
            addTerminalManagement.getTerminalType($("#terminalManufacturer").val());
        },

        getTerminalType: function (name) {
            var url = "/clbs/m/basicinfo/equipment/device/getTerminalTypeByName";
            json_ajax("POST", url, "json", false, {'name': name}, addTerminalManagement.getTerminalTypeCallback);
        }
        ,
        getTerminalTypeCallback: function (data) {
            var vt = 'F3-default';
            var result = data.obj.result;
            var str = "";
            if (result != null && result != '') {
                for (var i = 0; i < result.length; i++) {
                    if (vt == result[i].terminalType) {
                        str += '<option selected="selected" value="' + result[i].id + '">' + html2Escape(result[i].terminalType) + '</option>'
                    } else {
                        str += '<option  value="' + result[i].id + '">' + html2Escape(result[i].terminalType) + '</option>'
                    }
                }
            }
            $("#terminalType").html(str);
            $("#terminalTypeId").val($("#terminalType").val());
        }
        ,

        // 通讯类型初始化
        agreementType(){
            var url = '/clbs/m/connectionparamsset/protocolList';
            var param={"type":808}
            json_ajax("POST", url, "json", false, param, function (data) {
                var data = data.obj;
                for(var i=0; i<data.length; i++){
                    $('#deviceType').append(
                        "<option value='"+data[i].protocolCode+"'>"+data[i].protocolName+"</option>"
                    );
                }
            })
        }
    }
    $(function () {
        $('input').inputClear();
        //初始化
        addTerminalManagement.init();
        addTerminalManagement.agreementType();
        addTerminalManagement.getTerminalManufacturer();
        $('#terminalManufacturer').on("change", function () {
            var terminalManufacturerName = $(this).find("option:selected").attr("value");
            addTerminalManagement.getTerminalType(terminalManufacturerName);
        });

        $("#deviceType").on("change", function () {
            deviceType = $(this).val();
            deviceNumber = $("#deviceNumber").val();
            addTerminalManagement.deviceNumberValidates();
        });

        $('#terminalType').on("change", function () {
            var terminalTypeId = $(this).find("option:selected").attr("value");
            $("#terminalTypeId").val(terminalTypeId);
        });

        //显示菜单
        $("#zTreeCitySel").bind("click", addTerminalManagement.showMenu);
        //提交
        $("#doSubmit").bind("click", addTerminalManagement.doSubmit);
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
})(window, $);