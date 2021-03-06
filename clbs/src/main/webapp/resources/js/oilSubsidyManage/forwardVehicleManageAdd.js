(function (window, $) {
    addForwardVehicleType = {
        init: function () {
            var url = "/clbs/m/forward/vehicle/manage/findOilSubsidyPlat";
            var data = {
                "orgId": ""
            }
            json_ajax("POST", url, "json", false, data, addForwardVehicleType.getForwardPlatformCallback);
            var setting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    contentType: "application/json",
                    dataType: "json",
                    dataFilter: addForwardVehicleType.ajaxDataFilter
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
                    beforeClick: addForwardVehicleType.beforeClick,
                    onClick: addForwardVehicleType.onClick,
                    onAsyncSuccess: addForwardVehicleType.zTreeOnAsyncSuccess
                }
            };
            $.fn.zTree.init($("#ztreeDemo"), setting, null);
        },
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
                        required: '????????????????????????',
                    },
                    category: {
                        required: '?????????????????????',
                    },
                    url: {
                        maxlength: publicSize50,
                        required: '?????????webservice??????',
                    },
                    userName: {
                        maxlength: publicSize50,
                        required: '??????????????????',
                    },
                    password: {
                        maxlength: publicSize50,
                        required: '???????????????',
                    },
                    dockingCode: {
                        maxlength: publicSize50,
                        required: '??????????????????',
                    },
                }
            }).form();
        },
        doSubmits: function () {
            if (addForwardVehicleType.validates()) {
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
            treeObj.expandAll(true); // ??????????????????
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
            $("#selectGroup").val(v);
            $("#zTreeContent").hide();
            $("#category").bsSuggest("destroy");
            $("#category").val('');
            $("#forwardPlatform").val('');
            var urlData = "/clbs/m/forward/vehicle/manage/findOilSubsidyPlat";
            var dataParm = {
                "orgId": v
            }
            json_ajax("POST", urlData, "json", false, dataParm, addForwardVehicleType.getForwardPlatformCallback);
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            addForwardVehicleType.hideErrorMsg(); //????????????????????????
            var isAdminStr = $("#isAdmin").attr("value"); // ?????????admin
            // var isAdmin = isAdminStr == 'true';
            // var userGroupId = $("#userGroupId").attr("value"); // ?????????????????? id
            // var userGroupName = $("#userGroupName").attr("value"); // ?????????????????? name
            if (responseData != null && responseData != "" && responseData != undefined && responseData.length >= 1) {
                if ($("#selectGroup").val() == "") {
                    $("#selectGroup").val(responseData[0].uuid);
                    $("#zTreeCitySel").val(responseData[0].name);
                }
                return responseData;
            } else {
                addForwardVehicleType.showErrorMsg("??????????????????????????????", "zTreeCitySel");
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

            $("body").bind("mousedown", addForwardVehicleType.onBodyDown);
        },
        hideMenu: function () {
            $("#zTreeContent").fadeOut("fast");
            $("body").unbind("mousedown", addForwardVehicleType.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "zTreeContent" || $(
                    event.target).parents("#zTreeContent").length > 0)) {
                addForwardVehicleType.hideMenu();
            }
        },
        //????????????????????????
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
        //????????????
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
                // if (datas[i].codeNum != null) {
                //     $("#category").val(datas[i].name);
                //     $("#forwardPlatform").val(datas[i].id);
                // }
            };
            let newList = JSON.parse(JSON.stringify(dataList))
            $("#category").bsSuggest({
                indexId: 1, //data.value ???????????????????????????input??????????????????
                indexKey: 0, //data.value ???????????????????????????input??????????????????
                data: newList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {}).on('onSetSelectValue', function (e, keyword, data) {
                $("#category").val(keyword.key);
                $("#forwardPlatform").val(keyword.id);
            }).on('onUnsetSelectValue', function () {});
        },
    }
    $(function () {
        addForwardVehicleType.init();
        $('input').inputClear();
        $("#doSubmits").bind("click", addForwardVehicleType.doSubmits);
        $("#zTreeCitySel").on("click", function () {
            addForwardVehicleType.showMenu(this)
        });
        // ?????????input??????????????????
        $("#zTreeCitySel").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("ztreeDemo");
            treeObj.checkAllNodes(false);
            search_ztree('ztreeDemo', 'zTreeCitySel', 'group');
        });
        // ?????????input???????????????
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