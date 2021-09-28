var params = [];
//显示隐藏列
var menu_text = "";
var table = $("#dataTable tr th:gt(1)");
var commandType = '11';
//单选
var subChk = $("input[name='subChk']:checkbox");
var dtext = "";

var settingMoreUrl = '/clbs/v/monitoring/commandParam/settingMore_{vid}_{commandType}_{deviceType}.gsp';

var checkedList = [];
var settingParamIdList = [];
var hiddenNodes = '';
window.flusendflag = true;
var treeFlag = false;// 标识是否是2019协议
var self = "";
var multiFlag = "";

window.commandParam = {
    init: function () {
        menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
        for (var i = 1; i < table.length; i++) {
            menu_text += "<li><label ><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
        }
        $("#Ul-menu-text").html(menu_text);

        //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
        window.onbeforeunload = function () {
            var cancelStrS = {
                "desc": {
                    "MsgId": 40964,
                    "UserName": $("#userName").text()
                },
                "data": params
            };
            webSocket.unsubscribealarm(headers, "/app/vehicle/unsubscribestatus", cancelStrS);
        };
        commandParam.treeInit(treeFlag);
        $('.deviceCheck[value="1"]').prop('checked', true);
        commandParam.tableInit();
    },
    treeInit: function (flag) {
        var treeSetting = {
            view: {
                showIcon: false,
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
                onClick: commandParam.zTreeOnClick,
                onNodeCreated: commandParam.zTreeOnNodeCreated,
            }
        };
        var zNodes = [
            {id: 1, pId: 0, name: "交通部JT/T808", open: true},
            {id: 11, pId: 1, name: "通讯参数",},
            {id: 12, pId: 1, name: "终端参数"},

            {id: 13, pId: 1, name: "终端控制", open: true},
            {id: 131, pId: 13, name: "无线升级"},
            {id: 138, pId: 13, name: "下发升级包"},
            {id: 132, pId: 13, name: "控制终端连接指定服务器"},
            {id: 133, pId: 13, name: "终端关机"},
            {id: 134, pId: 13, name: "终端复位"},
            {id: 135, pId: 13, name: "恢复出厂设置"},
            {id: 136, pId: 13, name: "关闭数据通信"},
            {id: 137, pId: 13, name: "关闭所有无线通信"},

            {id: 14, pId: 1, name: "位置汇报参数"},
            {id: 16, pId: 1, name: "电话参数"},
            {id: 17, pId: 1, name: "视频拍照参数"},
            {id: 18, pId: 1, name: "GNSS参数"},
            {id: 19, pId: 1, name: "事件设置"},
            {id: 20, pId: 1, name: "电话本设置"},
            {id: 21, pId: 1, name: "信息点播菜单"},
            {id: 22, pId: 1, name: "基站参数设置"},
            {id: 23, pId: 1, name: "终端属性查询"},
            {id: 24, pId: 1, name: "RS232串口参数"},
            {id: 25, pId: 1, name: "RS485串口参数"},
            {id: 26, pId: 1, name: "CAN总线参数"},
            {id: 27, pId: 1, name: "GNSS及无线网络状态查询"},
            {id: 28, pId: 1, name: "视频通道状态查询"},
            {id: 29, pId: 1, name: "录像存储状态查询"},
            {id: 31, pId: 1, name: "删除终端围栏"}
        ];
        if (flag) {// T808-2019协议(移除部分指令)
            zNodes = [
                {id: 1, pId: 0, name: "交通部JT/T808", open: true},
                {id: 11, pId: 1, name: "通讯参数",},
                {id: 12, pId: 1, name: "终端参数"},

                {id: 13, pId: 1, name: "终端控制", open: true},
                {id: 138, pId: 13, name: "下发升级包"},
                {id: 132, pId: 13, name: "控制终端连接指定服务器"},
                {id: 134, pId: 13, name: "终端复位"},
                {id: 135, pId: 13, name: "恢复出厂设置"},

                {id: 14, pId: 1, name: "位置汇报参数"},
                {id: 16, pId: 1, name: "电话参数"},
                {id: 17, pId: 1, name: "视频拍照参数"},
                {id: 18, pId: 1, name: "GNSS参数"},
                {id: 20, pId: 1, name: "电话本设置"},
                {id: 22, pId: 1, name: "基站参数设置"},
                {id: 23, pId: 1, name: "终端属性查询"},
                {id: 24, pId: 1, name: "RS232串口参数"},
                {id: 25, pId: 1, name: "RS485串口参数"},
                {id: 26, pId: 1, name: "CAN总线参数"},
                {id: 27, pId: 1, name: "GNSS及无线网络状态查询"},
                {id: 28, pId: 1, name: "视频通道状态查询"},
                {id: 29, pId: 1, name: "录像存储状态查询"},
                {id: 30, pId: 1, name: "多媒体检索上传"},
                {id: 31, pId: 1, name: "删除终端围栏"}
            ];
        }
        $.fn.zTree.init($("#treeDemo"), treeSetting, zNodes);
    },
    zTreeOnNodeCreated: function (event, treeId, treeNode) {
        var id = treeNode.id;
        if (id === 11) {
            commandType = id;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.selectNode(treeNode, true, true);
        }
    },
    //点击节点
    zTreeOnClick: function (event, treeId, treeNode) {

        if (treeNode.id === 1 || treeNode.id === 13) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.cancelSelectedNode(treeNode);
            return;
        }
        commandType = treeNode.id;
        flusendflag = true;
        $("#dropdownMenu1").show();
        $("#settingMoreBtn").show();
        $("#send_model").show();
        var targetInput = $("#Ul-menu-text li:nth-child(2)").find('input');
        $('#settingMoreBtn span').text('批量设置');
        //隐藏批量设置和下发按钮
        if (commandType == 31) {
            $("#send_model").hide();
            $('#settingMoreBtn span').text('批量删除');
        } else if (commandParam.isQueryCommand()) {
            $("#dropdownMenu1").hide();
            $("#settingMoreBtn").hide();
            $("#send_model").hide();

            if (targetInput.is(':checked')) {
                targetInput.click();
            }
        } else if (commandType === 138) {
            $("#settingMoreBtn span").html('批量升级').show();
            $("#send_model").hide();
        } else {
            $("#send_model").show();
            if (!commandParam.isControlCommand()) {
                $("#settingMoreBtn").show();
            } else {
                $("#settingMoreBtn").hide();
            }
            if (!targetInput.is(':checked')) {
                targetInput.click();
            }
        }
        checkedList = [];
        //隐藏23 27 28 29下发状态栏
        myTable.requestData();
    },
    isControlCommand: function () {
        // 是否是终端控制指令
        return commandType === 133 || commandType === 134 || commandType === 135 || commandType === 136
            || commandType === 137;
    },
    isQueryCommand: function () {
        // 是否是终端参数查询指令
        return commandType === 23 || commandType === 27 || commandType === 28 || commandType === 29;
    },
    isFenceCommand: function () {
        // 删除终端围栏
        return commandType === 31;
    },
    // 树结构模糊搜索
    searchTree: function (txtObj) {
        if (txtObj.value.length > 0) {
            var zTreeObj = $.fn.zTree.getZTreeObj("treeDemo");
            //显示上次搜索后背隐藏的结点
            if (hiddenNodes !== '')
                zTreeObj.showNodes(hiddenNodes);

            //查找不符合条件的叶子节点
            var parentArr = [];

            function filterFunc(node) {
                var _keywords = txtObj.value;
                if (node.isParent) {
                    parentArr.push(node);
                    return false
                }
                return node.name.indexOf(_keywords.toUpperCase()) === -1;
            }

            //获取不符合条件的叶子结点
            hiddenNodes = zTreeObj.getNodesByFilter(filterFunc);
            var hideLen = hiddenNodes.length;
            if ((hideLen === 26 && !treeFlag) || (hideLen === 21 && treeFlag)) {
                hiddenNodes = hiddenNodes.concat(parentArr);
            }
            var num = 0;
            for (var i = 0; i < hideLen; i++) {
                if (hiddenNodes[i].pId === 13) {
                    num++
                }
            }
            if ((num === 8 && hideLen !== 26 && !treeFlag) || (num === 4 && hideLen !== 21 && treeFlag)) {// 隐藏终端控制节点
                hiddenNodes.push(parentArr[1]);
            }

            //隐藏不符合条件的叶子结点
            zTreeObj.hideNodes(hiddenNodes);
        } else {
            commandParam.treeInit(treeFlag);
            commandType = 11;
            myTable.requestData();
        }
    },
    //表格初始化
    tableInit: function () {
        //表格列定义
        var columnDefs = [{
            //第一列，用来显示序号
            "searchable": false,
            "orderable": false,
            "targets": 0
        }];
        var columns = [
            {
                //第一列，用来显示序号
                "data": null,
                "class": "text-center"
            },
            {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    var obj = {};
                    var result = "";
                    obj.settingParamId = row.settingParamId;
                    if (row.paramId != null && row.paramId !== "") {
                        obj.paramId = row.paramId;
                    }
                    obj.vehicleId = row.vehicleId;
                    var jsonStr = JSON.stringify(obj);
                    result += "<input  type='checkbox' name='subChk'  value='" + jsonStr + "' onclick='commandParam.subChkChange(this)'/>";
                    return result;
                }
            },
            {
                "data": null,
                "class": "text-center", //最后一列，操作按钮
                render: function (data, type, row, meta) {
                    var deviceType = $("input[name='deviceCheck']:checked").val();
                    var editUrlPath = "/clbs/v/monitoring/commandParam/edit_" + row.vehicleId + "_" + commandType + "_" + deviceType;
                    var bindUrl = "/clbs/v/monitoring/commandParam/bind_" + row.vehicleId + "_" + commandType + "_" + deviceType;
                    var result = '';
                    if (commandParam.isFenceCommand()) {
                        result += '<button href="'
                            + bindUrl
                            + '" data-toggle="modal" data-target="#commonWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>删除</button>&ensp;';
                        return result;
                    }
                    if (row.settingParamId != null) {
                        if (!commandParam.isControlCommand()) {
                            result += '<button href="'
                                + editUrlPath
                                + '" data-toggle="modal" data-target="#commonWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                            if (commandParam.isQueryCommand()) {
                                // 恢复默认
                                result += '<button disabled type="button" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>恢复默认</button>&ensp;';
                            } else {
                                //恢复默认
                                result += '<button type="button" onclick="commandParam.deleteCommand(\'' + row.vehicleId + '\',\'' + row.commandType + '\',\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>恢复默认</button>&ensp;';
                            }
                        }
                        // 下发参数
                        result += '<button onclick="commandParam.sendfuelOne(this, \'' + row.vehicleId + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';
                    } else { // 设置
                        if (!commandParam.isControlCommand()) {
                            if (commandParam.isQueryCommand()) {
                                result += '<button href="'
                                    + bindUrl
                                    + '" data-toggle="modal" data-target="#commonWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>读取</button>&ensp;';

                            } else {
                                result += '<button href="'
                                    + bindUrl
                                    + '" data-toggle="modal" data-target="#commonWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>设置</button>&ensp;';
                                result += '<button disabled type="button" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>恢复默认</button>&ensp;';
                            }
                        }
                        // 禁用下发参数
                        if (commandParam.isControlCommand()) {
                            result += '<button onclick="commandParam.sendfuelOne(this, \'' + row.vehicleId + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';
                        } else {
                            if (!commandParam.isQueryCommand()) {
                                result += '<button disabled type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
                            }
                        }
                    }
                    if (commandType === 30 || commandType === 138) {
                        result = '<button href="'
                            + bindUrl
                            + '" data-toggle="modal" data-target="#commonWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>设置</button>&ensp;';
                    }

                    return result;
                }
            },
            {
                "data": "status",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    var statusStr = '';
                    if (data === 0) {
                        statusStr = '参数已生效';
                    } else if (data === 1) {
                        statusStr = '参数未生效';
                    } else if (data === 2) {
                        statusStr = "参数消息有误";
                    } else if (data === 3) {
                        statusStr = "参数不支持";
                    } else if (data === 4) {
                        statusStr = "参数下发中";
                    } else if (data === 5) {
                        statusStr = "终端离线，未下发";
                    } else if (data === 7) {
                        statusStr = "终端处理中";
                    } else if (data === 8) {
                        statusStr = "参数下发失败";
                    }
                    return '<span id="' + row.vehicleId + '_status">' + statusStr + '</span>'
                    /* if (data === 0) {
                        return '参数已生效';
                    } else if (data === 1) {
                        return '参数未生效';
                    } else if (data === 2) {
                        return "参数消息有误";
                    } else if (data === 3) {
                        return "参数不支持";
                    } else if (data === 4) {
                        return "参数下发中";
                    } else if (data === 5) {
                        return "终端离线，未下发";
                    } else if (data === 7) {
                        return "终端处理中";
                    } else if (data === 8) {
                        return "参数下发失败";
                    } else {
                        return "";
                    }*/
                }
            },
            {
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (commandType === 11) {
                        return '通讯参数';
                    } else if (commandType === 12) {
                        return '终端参数';
                    } else if (commandType === 131) {
                        return "无线升级";
                    } else if (commandType === 132) {
                        return "控制终端连接指定服务器";
                    } else if (commandType === 133) {
                        return "终端关机";
                    } else if (commandType === 134) {
                        return "终端复位";
                    } else if (commandType === 135) {
                        return "恢复出厂设置";
                    } else if (commandType === 136) {
                        return "关闭数据通信";
                    } else if (commandType === 137) {
                        return "关闭所有无线通信";
                    } else if (commandType === 138) {
                        return "下发升级包";
                    } else if (commandType === 14) {
                        return "位置汇报参数";
                    } else if (commandType === 16) {
                        return "电话参数";
                    } else if (commandType === 17) {
                        return "视频拍照参数";
                    } else if (commandType === 18) {
                        return "GNSS参数";
                    } else if (commandType === 19) {
                        return "事件设置";
                    } else if (commandType === 20) {
                        return "电话本设置";
                    } else if (commandType === 21) {
                        return "信息点播菜单";
                    } else if (commandType === 22) {
                        return "基站参数设置";
                    } else if (commandType === 23) {
                        return "终端属性查询";
                    } else if (commandType === 24) {
                        return "RS232串口参数";
                    } else if (commandType === 25) {
                        return "RS485串口参数";
                    } else if (commandType === 26) {
                        return "CAN总线参数";
                    } else if (commandType === 27) {
                        return "GNSS及无线网络状态";
                    } else if (commandType === 28) {
                        return "视频通道状态";
                    } else if (commandType === 29) {
                        return "录像存储状态";
                    } else if (commandType === 30) {
                        return "多媒体检索上传";
                    } else if (commandType === 31) {
                        return "删除终端围栏";
                    } else {
                        return "";
                    }
                }
            },
            {
                "data": "brand",
                "class": "text-center"
            },
            {
                "data": "groupName",
                "class": "text-center"
            },
            {
                "data": "assignmentName",
                "class": "text-center"
            }
        ];
        //ajax参数
        var ajaxDataParamFun = function (d) {
            d.queryType = $('#queryType').val();
            d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
            d.commandType = commandType;
            d.deviceType = $("input[name='deviceCheck']:checked").val();
        };
        //表格setting
        var setting = {
            listUrl: '/clbs/v/monitoring/commandParam/list',
            editUrl: '/clbs/v/monitoring/commandParam/bind',
            deleteUrl: '/clbs/v/monitoring/commandParam/repristination_',
            deletemoreUrl: '/clbs/v/monitoring/commandParam/deletemore',
            columnDefs: columnDefs, //表格列定义
            columns: columns, //表格列
            dataTableDiv: 'dataTable', //表格
            ajaxDataParamFun: ajaxDataParamFun, //ajax参数
            drawCallbackFun: function () {
                var api = myTable.dataTable;
                var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
                api.column(0).nodes().each(function (cell, i) {
                    cell.innerHTML = startIndex + i + 1;
                });
            },
            pageable: true, //是否分页
            showIndexColumn: true, //是否显示第一列的索引列
            enabledChange: true,
            lengthMenu: [5, 10, 20, 50, 100],
        };
        //创建表格
        myTable = new TG_Tabel.createNew(setting);
        //表格初始化
        myTable.init();
    },
    deleteCommand: function (vid, type, id) {
        layer.confirm('是否恢复默认信息', {
            title: '操作确认',
            icon: 3, // 问号图标
            btn: ['确定', '取消'] //按钮
        }, function () {
            var param = {
                "vid": vid,
                "commandType": type,
                "id": id
            };
            var url = '/clbs/v/monitoring/commandParam/delete';
            json_ajax("POST", url, "json", true, param, function (data) {
                if (data.success) {
                    myTable.refresh();
                }
            });
            layer.closeAll('dialog');
        });
    },
    //全选
    checkAllClickFn: function () {
        $("input[name='subChk']").prop("checked", this.checked);
        checkedList = [];
        settingParamIdList = [];
        $("input[name='subChk']:checked").each(function () {
            var jsonStr = $(this).val();
            var jsonObj = $.parseJSON(jsonStr);
            checkedList.push(jsonObj.vehicleId);
            settingParamIdList.push(jsonObj.settingParamId);
        });
        var settingUrl = '/clbs/v/monitoring/commandParam/settingMore___.gsp';
        if (checkedList.length > 0) {
            var deviceType = $("input[name='deviceCheck']:checked").val();
            settingUrl = settingMoreUrl.replace("{vid}_{commandType}_{deviceType}", checkedList.toString() + "_" + commandType + "_" + deviceType);
        }
        $("#settingMoreBtn").attr("href", settingUrl);
    },
    subChkChange: function (obj) {
        $("#checkAll").prop("checked", subChk.length === subChk.filter(":checked").length);
        checkedList = [];
        settingParamIdList = [];
        $("input[name='subChk']:checked").each(function () {
            var jsonStr = $(this).val();
            var jsonObj = $.parseJSON(jsonStr);
            checkedList.push(jsonObj.vehicleId);
            settingParamIdList.push(jsonObj.settingParamId);
        });
        var settingUrl = '/clbs/v/monitoring/commandParam/settingMore___.gsp';
        if (checkedList.length > 0) {
            var deviceType = $("input[name='deviceCheck']:checked").val();
            settingUrl = settingMoreUrl.replace("{vid}_{commandType}_{deviceType}", checkedList.toString() + "_" + commandType + "_" + deviceType);
        } else {
            $("#checkAll").prop('checked', false);
        }
        $("#settingMoreBtn").attr("href", settingUrl);
    },
    // 下发参数 （单个）
    sendfuelOne: function (event, vehicleId) {
        multiFlag = true;
        // self = $(event).closest('td').next('td');
        self = vehicleId + '_status';
        commandParam.sendFuel(vehicleId);
    },
    //批量下发
    sendfuelMore: function (vehicleId) {
        commandParam.sendFuel(vehicleId);
    },
    // 下发参数
    sendFuel: function (vehicleId) {
        var url = "/clbs/v/monitoring/commandParam/sendParamByCommandType";
        var parameter = {"monitorIds": vehicleId, 'commandType': commandType};
        json_ajax("POST", url, "json", true, parameter, commandParam.sendFuelCallback);
    },
    // 下发流量回调方法
    sendFuelCallback: function (data) {
        if (flusendflag) {
            webSocket.subscribe(headers, "/user/topic/directive_parameter", paramter.updataFenceData, "", null);
            flusendflag = false;
        }
        if (data != null && data.success) {
            layer.msg(sendCommandComplete, {closeBtn: 0}, function (refresh) {
                myTable.requestData(); //执行的刷新语句
                checkedList = [];
                layer.close(refresh);
            });
            return;
        }
        layer.msg(data.msg);
    },

    //查询全部
    refreshTableFn: function () {
        selectGroupId = "";
        selectAssignmentId = "";
        $('#simpleQueryParam').val("");
        var zTree = $.fn.zTree.getZTreeObj("treeDemo");
        zTree.selectNode("");
        zTree.cancelSelectedNode();
        myTable.requestData();
    },

};
$(function () {
    $('input').inputClear().on('onClearEvent', function (e, data) {
        var id = data.id;
        if (id === 'search_condition') {
            commandParam.treeInit(treeFlag);
            commandType = '11';
            myTable.requestData();
        }
    });
    commandParam.init();
    // 组织架构模糊搜索
    $("#search_condition").on("input oninput", function () {
        search_ztree('treeDemo', 'search_condition', 'assignment');
    });
    //IE9
    if (navigator.appName === "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") === "MSIE9.0") {
        var search;
        $("#search_condition").bind("focus", function () {
            search = setInterval(function () {
                search_ztree('treeDemo', 'search_condition', 'assignment');
            }, 500);
        }).bind("blur", function () {
            clearInterval(search);
        });
    }
    // 监听协议类型切换,重新加载指令树
    $('.deviceCheck').on('change', function () {
        var curVal = $(this).val();
        $('#search_condition').val('');
        if (curVal == '1') {
            treeFlag = false;
            commandParam.treeInit(treeFlag);
        } else {
            treeFlag = true;
            commandParam.treeInit(treeFlag);
        }
        $('#dropdownMenu1').show();
        var targetInput = $("#Ul-menu-text li:nth-child(2)").find('input');
        if (!targetInput.is(':checked')) {
            targetInput.click();
        }
        $('.keep-open.btn-group').removeClass('open');
        myTable.requestData();
    });

    //IE9 end
    //全选
    $("#checkAll").on("click", commandParam.checkAllClickFn);
    //查询全部
    $('#refreshTable').on("click", commandParam.refreshTableFn);

    //批量下发
    $('#send_model').on("click", function () {
        multiFlag = false;
        if (checkedList.length === 0) {
            layer.msg(selectItem);
            return;
        }
        var flag = false;
        if (!commandParam.isControlCommand()) {
            for (var i = 0; i < settingParamIdList.length; i++) {
                if (settingParamIdList[i] != null && settingParamIdList[i] !== '') {
                    flag = true;
                }
            }
        } else {
            flag = true;
        }
        if (!flag) {
            layer.msg('请勾选已设置参数的记录');
            /* var settingUrl = '/clbs/v/monitoring/commandParam/settingMore__.gsp';
             $("#settingMoreBtn").attr("href", settingUrl);*/
            return;
        }
        //取消全选勾
        $("#checkAll").prop('checked', false);
        $("input[name=subChk]").prop("checked", false);
        commandParam.sendfuelMore(checkedList.toString());
    });
});
