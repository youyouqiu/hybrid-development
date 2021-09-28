(function (window, $) {
    addVehicleType = {
        init: function () {
            var url = "/clbs/m/forward/vehicle/manage/findOilSubsidyPlat";
            var data = {
                "vehicleCategory": ""
            }
            json_ajax("POST", url, "json", false, data, addVehicleType.getForwardPlatformCallback);
            var setting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    contentType: "application/json",
                    dataType: "json",
                    dataFilter: addVehicleType.ajaxDataFilter
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
                    beforeClick: addVehicleType.beforeClick,
                    onClick: addVehicleType.onClick,
                    onAsyncSuccess: addVehicleType.zTreeOnAsyncSuccess
                }
            };
            $.fn.zTree.init($("#ztreeDemo"), setting, null);
        },
        // categoryCallBack: function (data) {
        //     var result = data.obj;
        //     var str = "";
        //     for (var i = 0; i < result.length; i++) {
        //         str += '<option  value="' + result[i].id + '">' + result[i].name + '</option>'
        //     }
        //     $("#vehicleCategory12").html(str);
        // },
        validates: function () {
            return $("#addForm").validate({
                rules: {
                    dockingCodeOrgName: {
                        required: true,
                    },
                    category: {
                        required: true,
                    },
                    url: {
                        // positiveInteger: true,
                        maxlength: 200,
                        required: true,
                    },
                    userName: {
                        maxlength: 50,
                        required: true,
                    },
                    password: {
                        maxlength: 50,
                        required: true,
                    },
                    dockingCode: {
                        maxlength: 50,
                        required: true,
                    }
                },
                messages: {
                    dockingCodeOrgName: {
                        required: '请选择对接码组织',
                    },
                    category: {
                        required: '请选择转发平台',
                    },
                    url: {
                        maxlength: publicSize50,
                        required: '请输入webservice地址',
                    },
                    userName: {
                        maxlength: publicSize50,
                        required: '请输入用户名',
                    },
                    password: {
                        maxlength: publicSize50,
                        required: '请输入密码',
                    },
                    dockingCode: {
                        maxlength: publicSize50,
                        required: '请输入对接码',
                    },
                }
            }).form();
        },
        doSubmits: function () {
            if (addVehicleType.validates()) {
                addHashCode1($("#addForm"));
                $("#addForm").ajaxSubmit(function (data) {
                    var json = eval("(" + data + ")");
                    if (json.obj) {
                        $("#commonSmWin").modal("hide");
                        myTable.requestData();
                    } else {
                        layer.msg(json.msg);
                    }
                });
            }
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
            $("#category").bsSuggest("destroy");

            $("#selectGroup").val(v);
            $("#zTreeContent").hide();
            $("#category").val('');
            $("#forwardPlatform").val('');
            var urlData = "/clbs/m/forward/vehicle/manage/findOilSubsidyPlat";
            var dataParm = {
                "orgId": v
            }
            json_ajax("POST", urlData, "json", false, dataParm, addVehicleType.getForwardPlatformCallback);
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            addVehicleType.hideErrorMsg(); //清除错误提示样式
            var isAdminStr = $("#isAdmin").attr("value"); // 是否是admin
            var isAdmin = isAdminStr == 'true';
            var userGroupId = $("#userGroupId").attr("value"); // 用户所属组织 id
            var userGroupName = $("#userGroupName").attr("value"); // 用户所属组织 name
            if (responseData != null && responseData != "" && responseData != undefined && responseData.length >= 1) {
                if ($("#selectGroup").val() == "") {
                    $("#selectGroup").val(responseData[0].uuid);
                    $("#zTreeCitySel").val(responseData[0].name);
                }
                return responseData;
            } else {
                addVehicleType.showErrorMsg("您需要先新增一个组织", "zTreeCitySel");
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

            $("body").bind("mousedown", addVehicleType.onBodyDown);
        },
        hideMenu: function () {
            $("#zTreeContent").fadeOut("fast");
            $("body").unbind("mousedown", addVehicleType.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "zTreeContent" || $(
                    event.target).parents("#zTreeContent").length > 0)) {
                addVehicleType.hideMenu();
            }
        },
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#error_label_add").is(":hidden");
            $("#error_label_add").hide();
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
        //转发平台
        getForwardPlatformCallback: function (data) {
            var datas = data.obj
            var dataList = {
                    value: []
                },
                i = datas.length;
            while (i--) {
                dataList.value.push({
                    name: datas[i].name,
                    id: datas[i].id
                });
            };
            $("#category").bsSuggest({
                indexId: 1, //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: dataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {}).on('onSetSelectValue', function (e, keyword, data) {
                $("#category").val(keyword.key);
                $("#forwardPlatform").val(keyword.id);
            }).on('onUnsetSelectValue', function () {});
        },
    }
    $(function () {
        addVehicleType.init();
        $('input').inputClear();
        $("#doSubmits").bind("click", addVehicleType.doSubmits);
        $("#zTreeCitySel").on("click", function () {
            addVehicleType.showMenu(this)
        });
        // 组织树input框的模糊搜索
        $("#zTreeCitySel").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("ztreeDemo");
            treeObj.checkAllNodes(false);
            search_ztree('ztreeDemo', 'zTreeCitySel', 'group');
        });
        // 组织树input框快速清空
        $('#zTreeCitySel').inputClear().on('onClearEvent', function (e, data) {
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