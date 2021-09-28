(function (window, $) {
    var selectTreeId = '';
    var selectTreepId = "";
    var selectTreeType = '';
    var $keepOpen = $(".keep-open");
    var keyFlag = true;
    var monitorType = 0;//监控对象类型
    infoinputList = {
        init: function () {
            $("[data-toggle='tooltip']").tooltip();
            var setting = {
                async: {
                    url: "/clbs/m/infoconfig/infoinput/tree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    contentType: "application/json",
                    dataType: "json",
                    dataFilter: infoinputList.ajaxDataFilter
                },
                check: {
                    enable: true,
                    chkStyle: "checkbox",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    },
                    radioType: "all"
                },
                view: {
                    dblClickExpand: false
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    beforeClick: infoinputList.beforeClick,
                    onClick: infoinputList.onClick,
                    onCheck: infoinputList.onClick
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        //分组树点击及勾选
        onClick: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            nodes = zTree.getCheckedNodes(true);
            v = "";
            t = "";
            nodes.sort(function compare(a, b) {
                return a.id - b.id;
            });
            var amtNames = ""; // 车辆数超过100的分组
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "assignment") { // 选择的是分组，才组装值
                    if (infoinputList.checkMaxVehicleCountOfAssignment(nodes[i].id, nodes[i].name)) {
                        t += nodes[i].name + ",";
                        v += nodes[i].id + ";";
                    } else {
                        nodes[i].checked = false;
                        amtNames += nodes[i].name + ",";
                    }
                }
            }
            // 判断系统是否出问题
            if (systemErrorFlag) {
                layer.msg(systemError, {
                    time: 1500,
                });
                return;
            }
            if (amtNames.length > 0) {
                amtNames = amtNames.substr(0, amtNames.length - 1);
                layer.msg("【分组:" + amtNames + "】" + assignmentMaxCarNum);
            } else {
                infoinputList.clearErrorMsg();
            }
            if (v.length > 0) v = v.substring(0, v.length - 1);
            if (t.length > 0) t = t.substring(0, t.length - 1);
            var cityObj = $("#groupid");
            cityObj.attr("value", t);
            $("#citySelidVal").val(v);最多
        },
        // 校验当前分组下的车辆数是否已经达到最大值（最大值目前设定为：100台）
        checkMaxVehicleCountOfAssignment: function (assignmentId, assignmentName) {
            var b = true;
            $.ajax({
                type: 'POST',
                url: '/clbs/m/infoconfig/infoinput/checkMaxVehicleCount',
                data: {"assignmentId": assignmentId, "assignmentName": assignmentName},
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (data.success) {
                        b = data.obj.success;
                    } else {
                        layer.msg(data.msg);
                    }
                },
                error: function () {
                    layer.msg(systemError, {
                        time: 1500,
                    });
                    b = false;
                    systemErrorFlag = true;
                }
            });
            return b;
        },
        // 清除错误信息
        clearErrorMsg: function () {
            $("label.error").hide();
        },
        //树点击之前事件
        beforeClick: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, treeNode, true);
            return false;
        },
        //文件树初始化之前事件
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var flag = true;
            if (responseData) {
                for (var i = 0; i < responseData.length; i++) {
                    if (flag) {
                        if (responseData[i].type == 'assignment') {
                            responseData[i].checked = true;
                            $("#groupid").attr('value', responseData[i].name);
                            $("#citySelidVal").val(responseData[i].id);
                            flag = false;
                        }
                        ;
                    }
                    ;
                    responseData[i].open = true;
                }
            }
            return responseData;
        },
        //显示隐藏列
        showMenuText: function () {
            var menu_text = "";
            var table = $("#dataTable tr th:gt(1)");
            menu_text += "<li><label><input type=\"checkbox\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" checked=\"checked\" disabled />" + table[0].innerHTML + "</label></li>"
            for (var i = 1; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" checked=\"checked\" />" + table[i].innerHTML + "</label></li>"
            }
            ;
            $("#Ul-menu-text").html(menu_text);
            //显示隐藏列
            $('.toggle-vis').on('change', function (e) {
                var column = myDataTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $keepOpen.addClass("open");
            });
        },
        //创建表格
        infoinputTab: function () {
            //表格列定义
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columns = [{
                //第一列，用来显示序号
                "data": null,
                "class": "text-center"
            }, {
                //第二列，checkbox
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return '<input type="checkbox" name="subChk" value="' + row.configId + '" /> ';
                }
            }, {//第三列，操作按钮列
                "data": null,
                "class": "text-center", //最后一列，操作按钮
                render: function (data, type, row, meta) {
                    var editUrlPath = myTable.editUrl + row.configId + ".gsp"; //修改地址
                    var detailUrlPath = myTable.detailUrl + row.configId + ".gsp";
                    var result = '';
                    //修改按钮
                    result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                    //详情按钮
                    result += '<button href="' + detailUrlPath + '" data-target="#commonWin" data-toggle="modal" type="button" class=" editBtn editBtn-info"><i class="fa fa-sun-o"></i>详情</button>&ensp;';
                    //解除按钮
                    result += '<button type="button" onclick="myTable.deleteItem(\'' + row.configId + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>解除</button>';
                    return result;
                }
            }, {//第四列，监控对象-车
                "data": "carLicense",
                "class": "text-center"
            }, {//监控对象类型
                "data": "monitorType",
                "class": "text-center",
                render: function (data) {
                    if (data == "0") {
                        return "车";
                    } else if (data == "1") {
                        return "人";
                    } else if (data == "2") {
                        return "物";
                    } else {
                        return "";
                    }
                }
            }, {// 分组
                "data": "assignmentName",
                "class": "text-center",
            }, { // 终端手机号
                "data": "simcardNumber",
                "class": "text-center"
            }, { // 终端编号
                "data": "deviceNumber",
                "class": "text-center",
            }, { // 通讯类型
                "data": "deviceType",
                "class": "text-center",
                render: function (data) {
                    return getProtocolName(data);
                }
            }, { // 功能类型
                "data": "functionalType",
                "class": "text-center",
                render: function (data) {
                    if (data == "1") {
                        return "简易型车机";
                    } else if (data == "2") {
                        return "行车记录仪";
                    } else if (data == "3") {
                        return "对讲设备";
                    } else if (data == "4") {
                        return "手咪设备";
                    } else if (data == "5") {
                        return "超长待机设备";
                    } else if (data == "6") {
                        return "定位终端";
                    } else {
                        return "";
                    }
                }
            }, { // 外设
                "data": "peripheralsId",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return "<a onclick = 'infoinputList.showLogContent(\"" + row.vehicleId + "\")'>查看详情</a>";
                }
            }, { // 计费日期
                "data": "billingDate",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return (data != null && data.length > 10) ? data.substr(0, 10) : "";
                }
            }, { // 到期日期
                "data": "expireDate",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return (data != null && data.length > 10) ? data.substr(0, 10) : "";
                }
            }, { // 从业人员
                "data": "professionalNames",
                "class": "text-center"
            }, { //加车时间
                "data": "createDateTime",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return (data != null && data.length > 10) ? data.substr(0, 10) : "";
                }
            }, { //加车时间
                "data": "updateDateTime",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return (data != null && data.length > 10) ? data.substr(0, 10) : "";
                }
            }];
            //全选
            $("#checkAll").click(function () {
                $("input[name='subChk']").prop("checked", this.checked);
            });
            //单选
            var subChk = $("input[name='subChk']");
            subChk.click(function () {
                $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
            });
            //批量删除
            $("#del_model").click(function () {
                //判断是否至少选择一项
                var chechedNum = $("input[name='subChk']:checked").length;
                if (chechedNum == 0) {
                    layer.msg(selectItem, {move: false});
                    return;
                }
                var checkedList = new Array();
                $("input[name='subChk']:checked").each(function () {
                    checkedList.push($(this).val());
                });
                myTable.deleteItems({'deltems': checkedList.toString()});
            });
            //ajax参数
            var ajaxDataParamFun = function (d) {
                d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
                d.groupName = selectTreeId;
                d.groupType = selectTreeType;
            };
            //表格setting
            var setting = {
                detailUrl: "/clbs/m/infoconfig/infoinput/getConfigDetails_",
                listUrl: "/clbs/m/infoconfig/infoinput/list",
                editUrl: "/clbs/m/infoconfig/infoinput/edit_",
                deleteUrl: "/clbs/m/infoconfig/infoinput/delete_",
                deletemoreUrl: "/clbs/m/infoconfig/infoinput/deletemore",
                enableUrl: "/clbs/c/user/enable_",
                disableUrl: "/clbs/c/user/disable_",
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        // 刷新table列表
        refreshTable: function () {
            $('#simpleQueryParam').val("");
            selectTreeId = '';
            selectTreeType = '';
            /* var zTree = $.fn.zTree.getZTreeObj("groupTreeDemo");
             zTree.selectNode("");
             zTree.cancelSelectedNode();*/
            myTable.requestData();
        },
        speedEntryBtnClick: function () {
            window.location.href = '/clbs/m/infoconfig/infoFastInput/add?speedFlag=speedTrue';
        },

        scanCodeEntryShow: function () {
            infoinputList.onClick();
            //显示
            if ($("#scanCodeEntry").is(":hidden")) {
                $("#scanCodeEntry").show();
                $("#simCardNumber").val("");
                $("#terminal").val("");
                $("#simCardNumber").focus();
            } else {
                $("#simCardNumber").val("");
                $("#terminal").val("");
                $("#monitorTheObject").val("");
                $("#simCardNumber").focus();
                $("#simCardNumber").css("background-color", "#fafafa");
                $("#terminal").css("background-color", "#fafafa");
                infoinputList.clearErrorMsg();
            }
            //改变标题栏图标
            if ($("#scanCodeEntryContent").is(":hidden")) {
                $("#scanCodeEntryContent").show();
                if ($("#sceFac").hasClass("fa fa-chevron-up")) {
                    $("#sceFac").attr("class", "fa fa-chevron-down");
                }
            }
        },
        //扫码录入执行方法
        PCKeyDownEvent: function () {
            $("#simCardNumber").on("keydown", function (e) {
                var key = e.which;
                if (key == "13") {
                    if ($("#simCardNumber").val() == "" || $("#simCardNumber").val() == null) {
                        setTimeout(function () {
                            $("#simCardNumber").click().focus();
                        }, 500);
                    } else {
                        setTimeout(function () {
                            $("#terminal").click().focus();
                        }, 500);
                        var sim = $("#simCardNumber").val();
                        $.ajax({
                            type: 'POST',
                            async: true,
                            data: {"sim": sim, "monitorType": monitorType},
                            url: '/clbs/m/infoconfig/infoFastInput/getRandomNumbers',
                            dataType: 'json',
                            success: function (data) {
                                if (data == 26) {
                                    layer.msg("你就这么无聊吗?扫同一张卡26次，卡表示已经不行了，请换一张卡或者把没用的监控对象删了吧！");
                                    $("#monitorTheObject").val("");
                                } else if (data == -1) {
                                    layer.msg("系统响应异常，请稍后再试或联系管理员！");
                                } else {
                                    $("#monitorTheObject").val(data);
                                }
                            },
                            error: function () {
                                layer.msg("获取监控对象编号异常!");
                            }
                        });
                        $.ajax({
                            type: 'POST',
                            async: true,
                            data: {"simcardNumber": sim},
                            url: '/clbs/m/infoconfig/infoinput/getSimcardInfoBySimcardNumber',
                            dataType: 'json',
                            success: function (data) {
                                if (data.success) {
                                    if (data != null && data.obj != null && data.obj.simcardInfo != null) {
                                        $("#simCardNumber").css("background-color", "#fafafa");
                                    } else {
                                        $("#simCardNumber").css("background-color", "rgba(255, 0, 0, 0.1)");
                                    }
                                } else {
                                    layer.msg(data.msg);
                                }
                            },
                            error: function () {
                                layer.msg("判断sim信息失败!");
                            }
                        });
                    }
                }
            })
            $("#terminal").on("keydown", function (ev) {
                var key = ev.which;
                //添加一个标记防止用户连续点击回车重复提交数据
                if (key == "13" && keyFlag) {
                    keyFlag = false;
                    if ($("#terminal").val() == "" || $("#terminal").val() == null) {
                        setTimeout(function () {
                            $("#terminal").click().focus();
                            keyFlag = true;
                        }, 500);
                    }
                    else if ($("#simCardNumber").val() == "" || $("#simCardNumber").val() == null) {
                        setTimeout(function () {
                            $("#simCardNumber").click().focus();
                            keyFlag = true;
                        }, 500);
                    }
                    else {
                        var devices = $("#terminal").val();
                        $.ajax({
                            type: 'POST',
                            async: false,
                            data: {"deviceNumber": devices},
                            url: '/clbs/m/infoconfig/infoinput/getDeviceInfoByDeviceNumber',
                            dataType: 'json',
                            success: function (data) {
                                if (data.success) {
                                    if (data != null && data.obj != null && data.obj.deviceInfo != null) {
                                        $("#terminal").css("background-color", "#fafafa");
                                    } else {
                                        $("#terminal").css("background-color", "rgba(255, 0, 0, 0.1)");
                                    }
                                } else {
                                    layer.msg(data.msg);
                                }
                            },
                            error: function () {
                                layer.msg("判断终端信息失败!");
                            }
                        });
                        if (infoinputList.validate()) {
                            var str = "";
                            var device = $("#terminal").val();
                            var deviceType = $("#communicationType").val();
                            if (deviceType == 1 || deviceType == 0) {
                                device = device.substring(device.length - 7, device.length);
                            }
                            if (infoinputList.checkIsBound("brands", $("#monitorTheObject").val())) {
                                str += "车牌号[" + $("#monitorTheObject").val() + "], ";
                            }
                            if (infoinputList.checkIsBound("devices", device)) {
                                str += "终端号[" + $("#terminal").val() + "], ";
                            }
                            if (infoinputList.checkIsBound("sims", $("#simCardNumber").val())) {
                                str += "终端手机号[" + $("#simCardNumber").val() + "], ";
                            }
                            if (str.length > 0) {
                                str = str.substr(0, str.length - 1);
                                layer.closeAll();
                                layer.msg("不好意思，你来晚了！【" + str + "】已被别人抢先一步绑定了");
                                keyFlag = true;
                                return;
                            }
                            $("#addScanCodeEntry").ajaxSubmit(function () {
                                keyFlag = true;
                                infoinputList.scanCodeEntryShow();
                                if (navigator.userAgent.indexOf('MSIE') >= 0) {
                                    $("#SCEMsgBox").html('<embed id="IEsceMsg" src="../../../file/music/sceMsg.mp3" autostart="true"/>');
                                } else {
                                    $("#SCEMsgBox").html('<audio id="SCEMsgAutoOff" src="../../../file/music/sceMsg.mp3" autoplay="autoplay"></audio>');
                                }
                                myTable.requestData();
                            });
                        }
                        else {
                            keyFlag = true;
                        }
                    }
                }
            })
        },
        // 校验是否已被绑定
        checkIsBound: function (elementId, elementValue) {
            var tempFlag = false;
            var url = "/clbs/m/infoconfig/infoinput/checkIsBound";
            var data = "";
            if (elementId == "brands") {
                data = {"inputId": "brands", "inputValue": elementValue}
            } else if (elementId == "devices") {
                data = {"inputId": "devices", "inputValue": elementValue}
            } else if (elementId == "sims") {
                data = {"inputId": "sims", "inputValue": elementValue}
            }
            $.ajax({
                type: 'POST',
                url: url,
                data: data,
                dataType: 'json',
                async: false,
                success: function (data) {
                    if (data.success) {
                        if (null != data && data.obj != null && data.obj.isBound) {
                            layer.msg("不好意思，你来晚了！【" + data.obj.boundName + "】已被别人抢先一步绑定了");
                            tempFlag = true;
                        } else {
                            tempFlag = false;
                        }
                    } else {
                        layer.msg(data.msg);
                    }
                },
                error: function () {
                    layer.msg("校验异常！");
                    tempFlag = false;
                }
            });
            return tempFlag;
        },
        //扫码校验
        validate: function () {
            return $("#addScanCodeEntry").validate({
                rules: {
                    groupid: {
                        required: true
                    },
                    brands: {
                        required: true
                    },
                    sims: {
                        isSim: true
                    },
                    devices: {
                        checkDeviceNumber: "#communicationType"
                    }
                },
                messages: {
                    groupid: {
                        required: "请至少选择一个分组"
                    },
                    brands: {
                        required: "监控对象不能为空"
                    },
                    sims: {
                        isSim: "请输入正确的电话号码"
                    },
                    devices: {
                        checkDeviceNumber: deviceSpeedNumberError
                    }
                }
            }).form();
        },
        showLogContent: function (msg) { // 显示log详情
            var url = "/clbs//m/infoconfig/infoinput/getPeripherals";
            var parameter = {"vehicleId": msg};
            json_ajax("POST", url, "json", true, parameter, infoinputList.getPeripheralsCallback);

        },
        getPeripheralsCallback: function (data) {
            if (data != null && data != undefined && data != "") {
                if (data.success) {
                    $("#detailShow").modal("show");
                    $("#detailContent").html(data.obj.pname == "" ? "该监控对象目前还没有绑定外设哦！" : data.obj.pname);
                } else {

                }
            }
        },
        groupListTree: function () {
            var treeSetting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/assignment/assignmentTree",
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {  // 是否可选  Organization
                        "isOrg": "1"
                    },
                    dataFilter: infoinputList.groupAjaxDataFilter
                },
                view: {
                    selectedMulti: false,
                    nameIsHTML: true,
                    fontCss: setFontCss_ztree
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    onClick: infoinputList.zTreeOnClick
                }
            };
            $.fn.zTree.init($("#groupTreeDemo"), treeSetting, null);
        },
        //组织树预处理函数
        groupAjaxDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj("groupTreeDemo");
            if (responseData) {
                for (var i = 0; i < responseData.length; i++) {
                    responseData[i].open = true;
                }
            }
            return responseData;
        },
        //点击节点
        zTreeOnClick: function (event, treeId, treeNode) {
            if (treeNode.type == "group") {
                selectTreepId = treeNode.id;
                selectTreeId = treeNode.uuid;
            } else {
                selectTreepId = treeNode.pId;
                selectTreeId = treeNode.id;
            }
            selectTreeType = treeNode.type;
            myTable.requestData();
        },
        //车、人、物点击tab切换
        chooseLabClick: function () {
            $("ul.dropdown-menu").css("display", "none");
            infoinputList.hideErrorMsg();
            $(this).parents('.lab-group').find('input').prop("checked", false);
            $(this).siblings('input').prop("checked", true);
            $(this).parents('.lab-group').find('label.monitoringSelect').removeClass("activeIcon");
            $(this).addClass('activeIcon');
            $("label.error").hide();//隐藏validate验证错误信息
            monitorType = $(this).siblings('input').val();
            //切换监控对象类型时清空之前的信息
            $("#monitorTheObject").val("");
            $("#simCardNumber").val("");
            $("#terminal").val("");
            //光标移到终端手机号信息框中
            $("#simCardNumber").focus();
        },
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#error_label").hide();
        },
    };

    $(function () {
        var myTable;
        infoinputList.init();
        infoinputList.groupListTree();
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'search_condition') {
                search_ztree('groupTreeDemo', id, 'assignment');
            }
            ;
        });
        //IE9
        if (navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") == "MSIE9.0") {
            var search;
            $("#search_condition").bind("focus", function () {
                search = setInterval(function () {
                    search_ztree('groupTreeDemo', 'search_condition', 'assignment');
                }, 500);
            }).bind("blur", function () {
                clearInterval(search);
            });
        }
        infoinputList.PCKeyDownEvent();
        $('input').inputClear();
        infoinputList.infoinputTab();
        infoinputList.showMenuText();
        if (navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") == "MSIE9.0") {
            infoinputList.refreshTable();
        }
        // 车辆管理中，车辆删除时，如果已经绑定，则自动跳转到信息配置界面进行解绑后，再回到车辆管理界面删除即可
        // var open = window.opener;
        // if (null != open) {
        // 	var ele = open.document.getElementById("transValue");
        // 	if (null != ele) {
        // 		var param = ele.value;
        // 		$("#simpleQueryParam").val(param);
        // 		$("#search_button").click();
        // 	}
        // }
        $("#refreshTable").on("click", infoinputList.refreshTable);
        $("#informationSpeed").bind("click", infoinputList.speedEntryBtnClick);
        //分组显示
        $("#groupid").on("click", showMenuContent);
        //扫码录入显示执行
        $("#scanCodeEntryShow").on("click", infoinputList.scanCodeEntryShow);
        // 组织架构模糊搜索
        $("#search_condition").on("input oninput", function () {
            search_ztree('groupTreeDemo', 'search_condition', 'assignment');
        });

        //车、人、物点击tab切换
        $("label.monitoringSelect").on("click", infoinputList.chooseLabClick);
    })
})(window, $)