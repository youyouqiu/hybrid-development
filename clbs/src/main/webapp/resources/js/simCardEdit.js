(function ($, widnow) {
    var bindId = $("#bindId").val();
    editSimCardManagement = {
        init: function () {
            if (bindId != null && bindId != '') {
                $("#simcardNumber").attr("readonly", true);
                $("#bindMsg").attr("hidden", false);
            }
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
                    dblClickExpand : false,
                    nameIsHTML: true,
                    countClass: "group-number-statistics"
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    beforeClick: editSimCardManagement.beforeClick,
                    onClick: editSimCardManagement.onClick,
                    onAsyncSuccess: editSimCardManagement.zTreeOnAsyncSuccess
                }
            };
            $.fn.zTree.init($("#ztreeDemo"), setting, null);
            laydate.render({elem: '#openCardTimeEdit', theme: '#6dcff6'});
            laydate.render({elem: '#endTimeEdit', theme: '#6dcff6'});
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
            var zTree = $.fn.zTree.getZTreeObj("ztreeDemo"), nodes = zTree
                .getSelectedNodes(), n = "";
            v = "";
            nodes.sort(function compare(a, b) {
                return a.id - b.id;
            });
            for (var i = 0, l = nodes.length; i < l; i++) {
                n += nodes[i].name;
                v += nodes[i].uuid + ",";
            }
            if (v.length > 0)
                {v = v.substring(0, v.length - 1);}
            var cityObj = $("#zTreeCitySel");
            cityObj.attr("value", v);
            cityObj.val(n);
            $("#groupId").val(v);
            $("#zTreeContent").hide();
        },
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
            $("body").bind("mousedown", editSimCardManagement.onBodyDown);
        },
        hideMenu: function () {
            $("#zTreeContent").fadeOut("fast");
            $("body").unbind("mousedown", editSimCardManagement.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "zTreeContent" || $(
                event.target).parents("#zTreeContent").length > 0)) {
                editSimCardManagement.hideMenu();
            }
        },
        validates: function () {
            return $("#editForm").validate({
                rules: {
                    simcardNumber: {
                        required: true,
                        isSim: true,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/equipment/simcard/repetition",
                            dataType: "json",
                            data: {
                                username: function () {
                                    return $("#simcardNumber").val();
                                }
                            },
                            dataFilter: function (data, type) {
                                var oldV = $("#scn").val();
                                var newV = $("#simcardNumber").val();
                                var data2 = data;
                                if (oldV == newV) {
                                    console.log(simNumberError, simNumberExists);
                                    return true;
                                } 
                                    if (data2 == "true") {
                                        return true;
                                    } 
                                        return false;
                                    
                                
                            }
                        }
                    },
                    groupId: {
                        required: true
                    },
                    isStart: {
                        required: false,
                        maxlength: 6
                    },
                    operator: {
                        required: false,
                        maxlength: 50
                    },
                    openCardTime: {
                        date: true
                    },
                    capacity: {
                        required: false,
                        maxlength: 20
                    },
                    simFlow: {
                        required: false,
                        maxlength: 20
                    },
                    useFlow: {
                        maxlength: 20
                    },
                    alertsFlow: {
                        required: false,
                        maxlength: 20
                    },
                    endTime: {
                        required: false,
                        compareDate: "#openCardTimeEdit"
                    },
                    correctionCoefficient: {
                        isRightNumber: true,
                        isInt1tov: 200,
                    },
                    forewarningCoefficient: {
                        isRightNumber: true,
                        isInt1tov: 200,
                    },
                    hourThresholdValue: {
                        range: [0, 6553]
                    },
                    dayThresholdValue: {
                        range: [0, 429496729]
                    },
                    monthThresholdValue: {
                        range: [0, 429496729]
                    },
                    iccid: {
                        required: false,
                        checkICCID:true
                    },
                    imsi: {
                        required: false,
                        maxlength: 50
                    },
                    imei: {
                        required: false,
                        maxlength: 20
                    },
                    realId: {
                        digits: true,
                        minlength: 7,
                        maxlength: 20
                    },
                    remark: {
                        maxlength: 50
                    }
                },
                messages: {
                    realId: {
                        digits: simRealNumberError,
                        minlength: simRealNumberError,
                        maxlength: simRealNumberError
                    },
                    simcardNumber: {
                        required: '请输入数字字母，范围：7~20位',
                        isSim: '请输入数字字母，范围：7~20位',
                        remote: simNumberExists
                    },
                    groupName: {
                        required: publicNull
                    },
                    isStart: {
                        required: publicNull,
                        maxlength: publicSize6
                    },
                    operator: {
                        required: publicNull,
                        maxlength: publicSize50
                    },
                    openCardTime: {
                        required: publicNull,
                    },
                    capacity: {
                        required: publicNull,
                        maxlength: publicSize20
                    },
                    simFlow: {
                        required: publicNull,
                        maxlength: publicSize20
                    },
                    useFlow: {
                        maxlength: publicSize20
                    },
                    alertsFlow: {
                        required: publicNull,
                        maxlength: publicSize20
                    },
                    endTime: {
                        required: publicNull,
                        compareDate: simCompareOpenCardTime
                    },
                    forewarningCoefficient: {
                        isRightNumber: publicNumberInt,
                        isInt1tov: simMax200Length,
                    },
                    correctionCoefficient: {
                        isRightNumber: publicNumberInt,
                        isInt1tov: simMax200Length,
                    },
                    hourThresholdValue: {
                        range: simHourTrafficLength
                    },
                    dayThresholdValue: {
                        range: simDayTrafficLength
                    },
                    monthThresholdValue: {
                        range: simMonthTrafficLength
                    },
                    iccid: {
                    },
                    imsi: {
                        maxlength: publicSize50
                    },
                    imei: {
                        maxlength: publicSize20
                    },
                    remark: {
                        maxlength: publicSize50
                    }
                }
            }).form();
        },
        doSubmit: function () {
            if (editSimCardManagement.validates()) {
                addHashCode1($("#editForm"));
                $("#editForm").ajaxSubmit(function (data) {
                    var json = JSON.parse(data);
                    if (json.success) {
                        $("#commonWin").modal("hide");
                        myTable.refresh();
                    } else {
                        layer.msg(json.msg);
                    }
                });
            }
            ;
        },
    };
    $(function () {
        $('input').inputClear().on('onClearEvent', function (msg) {
            if (msg.target.id === 'monthThresholdValue') {
                $("#alertsFlow").val("");
            }
        });
        editSimCardManagement.init();
        $("#forewarningCoefficient").bind('input oninput', function () {
            $("#alertsFlow").val((Number($("#forewarningCoefficient")[0].value) / 100 * Number($("#monthThresholdValue")[0].value)).toFixed(2))
            if ($('#monthThresholdValue').val() === '') {
                $("#alertsFlow").val('');
            }
            if ($("#alertsFlow").val() == "NaN") {
                $("#alertsFlow").val(0)
            }
        });
        $("#monthThresholdValue").bind('input oninput', function () {
            $("#alertsFlow").val((Number($("#forewarningCoefficient")[0].value) / 100 * Number($("#monthThresholdValue")[0].value)).toFixed(2))
            if ($('#monthThresholdValue').val() === '') {
                $("#alertsFlow").val('');
            }
            if ($("#alertsFlow").val() == "NaN") {
                $("#alertsFlow").val(0)
            }
        });
        $("#zTreeCitySel").bind("click", editSimCardManagement.showMenu);
        $("#doSubmit").bind("click", editSimCardManagement.doSubmit);
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
}($, window))