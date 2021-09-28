var params = [];
//显示隐藏列
var menu_text = "";
var table = $("#dataTable tr th:gt(1)");
var selectGroupId = '';
var selectAssignmentId = '';
var alasendflag = true;
var checkedId = "";
var subCheckedId = "";
//单选
var subChk = $("input[name='subChk']:checkbox");
var dtext = "";
//	var dn = new Array("交通部JT/T808","移为GV320","天禾","北斗天地协议","康凯斯有线","康凯斯无线","博实结A5","艾赛欧超长待机","F3超长待机");
//	var dv = new Array("-1","2","3","5","6","7","8","9","10");
var dn = new Array(" 交通部JT/T808-2013", " 交通部JT/T808-2019", " BDTD-SM", " ZYM", " F3超长待机");
var dv = new Array("-1", "11", "5", "9", "10");
alarmSetting = {
    init: function () {
        //websocket 连接
        // webSocket.init('/clbs/vehicle');
        // 接收到消息
        // setTimeout(function () {
        //    webSocket.subscribe(headers, '/user/' + $("#userName").text() +'/fencestatus', alarmSetting.updataFenceData,null, null);
        // },500);
        menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>"
        for (var i = 1; i < table.length; i++) {
            menu_text += "<li><label ><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
        }
        ;
        $("#Ul-menu-text").html(menu_text);
        //生成定制显示列
        for (var i = 0; i < dn.length; i++) {
            dtext += "<label class='radio-inline' style='margin-left:10px;'><input name=\"deviceCheck\" value=\"" + dv[i] + "\" type=\"radio\" class=\"device\" />" + dn[i] + "</label>";
        }
        ;
        $("#Ul-menu-text-v").html(dtext);
        $("input[value='-1']").prop("checked", true);
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
        }
        var treeSetting = {
            async: {
//					url : "/clbs/m/functionconfig/fence/bindfence/vehicleTreeByDeviceType",
                url: "/clbs/m/basicinfo/enterprise/assignment/assignmentTree",
                type: "post",
                enable: true,
                sync: false,
                autoParam: ["id"],
                dataType: "json",
                otherParam: { // 是否可选  Organization
                    "isOrg": "1"
                },
                dataFilter: alarmSetting.ajaxDataFilter
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
                onClick: alarmSetting.zTreeOnClick
            }
        };
        $.fn.zTree.init($("#treeDemo"), treeSetting, null);

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
                    var result = '';
                    var obj = {};
                    obj.alarmVehicleId = row.id;
                    if (row.paramId != null && row.paramId != "") {
                        obj.paramId = row.paramId;
                    }
                    obj.vehicleId = row.vehicleId;
                    obj.monitorName = row.brand;
                    obj.alarmTypeId = row.alarmTypeId;
                    var jsonStr = JSON.stringify(obj)
                    result += "<input  type='checkbox' name='subChk'  value='" + jsonStr + "' onclick='alarmSetting.subChkChange(this)'/>";
                    return result;
                }
            },
            {
                "data": null,
                "class": "text-center", //最后一列，操作按钮
                render: function (data, type, row, meta) {
                    var deviceType = $("input[name='deviceCheck']:checked").val();
                    var editUrlPath = myTable.editUrl + row.vehicleId + ".gsp?deviceType=" + deviceType + "";
                    var bindUrlPre = /*[[@{/v/oilmgt/fluxsensorbind/bind_{id}.gsp}]]*/'url';
                    var detailUrlPre = /*[[@{/v/oilmgt/fluxsensorbind/detail_{id}.gsp}]]*/'url';
                    var linkagePath = '/clbs/a/alarmSetting/linkage_' + row.vehicleId + "?deviceType=" + deviceType;
                    var result = '';
                    var paramId = "";
                    if (row.paramId != null && row.paramId != "") {
                        paramId = row.paramId;
                    }
                    if (row.settingUp == true) {
                        result += '<button type="button" onclick="alarmSettingUtil.showModal(\'' + row.vehicleId + '\',\'1\',\'' + row.brand + '\')" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;'
                        //删除按钮
                        result += '<button type="button" onclick="myTable.deleteAlarmSettingsItem(\''
                            + row.vehicleId
                            + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>清空设置</button>&ensp;';
                        if (row.deviceType == '5' || row.deviceType == '9' || row.deviceType == '10') {
                            // 禁用下发参数
                            result += '<button disabled type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
                        } else {
                            result += '<button onclick="alarmSetting.sendfuelOne(\'' + row.id + '\',\'' + paramId + '\',\'' + row.vehicleId + '\',\'' + row.alarmTypeId + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';
                        }
                    } else {
                        // 设置
                        result += '<button type="button" onclick="alarmSettingUtil.showModal(\'' + row.vehicleId + '\',\'2\',\'' + row.brand + '\')" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>设置</button>&ensp;'
                        // 禁用删除按钮
                        result += '<button disabled type="button" onclick="myTable.deleteAlarmSettingsItem(\''
                            + row.id
                            + '\')" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>清空设置</button>&ensp;';
                        // 禁用下发参数
                        result += '<button disabled type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
                    }
                    // deviceType 为1代表交通部协议，联动策略按钮可用，否则不可点击
                    var protocolTypeArr = ['1', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24', '25', '26', '27', '28'];
                    if (protocolTypeArr.indexOf(row.deviceType) !== -1) {
                        result += '<button href="'
                            + linkagePath
                            + '" data-toggle="modal" data-target="#commonWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>智能联动</button>&ensp;';
                    } else {
                        result += '<button disabled  type="button" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>智能联动</button>&ensp;';
                    }
                    return result;
                }
            },
            {
                "data": "status",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "0") {
                        return '参数已生效';
                    } else if (data == "1") {
                        return '参数未生效';
                    } else if (data == "2") {
                        return "参数消息有误";
                    } else if (data == "3") {
                        return "参数不支持";
                    } else if (data == "4") {
                        return "参数下发中";
                    } else if (data == "5") {
                        return "终端离线，未下发";
                    } else if (data == "7") {
                        return "终端处理中";
                    } else if (data == "8") {
                        return "终端接收失败";
                    }
                    return "";

                }
            },
            {
                "data": "brand",
                "class": "text-center",
            },
            {
                "data": "deviceType",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return getProtocolName(data);
                }
            },
            {
                "data": "groups",
                "class": "text-center"
            },
            {
                "data": "vehicleType",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == null || data == "" || data == undefined) {
                        return "-";
                    }
                    return data;
                }
            }
        ];
        //ajax参数
        var ajaxDataParamFun = function (d) {
            d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
            d.groupId = selectGroupId;
            d.assignmentId = selectAssignmentId;
            d.deviceType = $("input[name='deviceCheck']:checked").val();
        };
        //表格setting
        var setting = {
            listUrl: '/clbs/a/alarmSetting/list',
            editUrl: '/clbs/a/alarmSetting/setting_',
            deleteUrl: '/clbs/a/alarmSetting/delete_',
            deletemoreUrl: '/clbs/a/alarmSetting/deletemore',
//				enableUrl : '/clbs/c/user/enable_',
//				disableUrl : '/clbs/c/user/disable_',
            columnDefs: columnDefs, //表格列定义
            columns: columns, //表格列
            dataTableDiv: 'dataTable', //表格
            ajaxDataParamFun: ajaxDataParamFun, //ajax参数
            drawCallbackFun: function () {
                var api = myTable.dataTable;
                var startIndex = api.context[0]._iDisplayStart; //获取到本页开始的条数
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
    //组织树预处理函数
    ajaxDataFilter: function (treeId, parentNode, responseData) {
//			responseData = JSON.parse(ungzip(responseData));
        var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
        if (responseData) {
            for (var i = 0; i < responseData.length; i++) {
                responseData[i].open = true;
            }
        }
        return responseData;
    },
    //点击节点
    zTreeOnClick: function (event, treeId, treeNode) {
        if (treeNode.type == "assignment") {
            selectAssignmentId = treeNode.id;
            selectGroupId = '';
        } else {
            selectGroupId = treeNode.uuid;
            selectAssignmentId = '';
        }
        myTable.requestData();
    },
    updataFenceData: function (msg) {
        if (msg != null) {
            var result = $.parseJSON(msg.body);
            if (result != null) {
                myTable.refresh();
            }
        }
    },
    //获取第一个根子节点
    getFirstNodes: function (node) {
        if (node.isParent == true) {
            getFirstNodes(node.children[0]);
        } else {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            treeObj.checkNode(node);
            selectVehicleId = node.id;
            var settingUrl = myTable.editUrl + selectVehicleId + ".gsp"
            $("#settingBtn").attr("href", settingUrl);
        }
        ;
    },
    //全选
    checkAllClickFn: function () {
        $("input[name='subChk']").prop("checked", this.checked);
        var checkedList = new Array();
        $("input[name='subChk']:checked").each(function () {
            var jsonStr = $(this).val();
            var jsonObj = $.parseJSON(jsonStr);
            checkedList.push(jsonObj.vehicleId);
        });
        checkedId = checkedList;
        var deviceType = $("input[name='deviceCheck']:checked").val();
        var settingUrl = settingMoreUrl + "?deviceType=" + deviceType + "&vehicleIds=" + checkedList.toString();
        // $("#settingMoreBtn").attr("href", settingUrl);

        //批量联动
        var deviceType = $("input[name='deviceCheck']:checked").val();
        var url = '/clbs/a/alarmSetting/linkage_' + checkedList.toString() + "?deviceType=" + deviceType;
        $("#batchTactics").attr("href", url);
    },
    subChkChange: function (obj) {
        $("#checkAll").prop(
            "checked",
            $("input[name='subChk']:checkbox").length == $("input[name='subChk']:checked").length ? true
                : false);
        var checkedList = new Array();
        $("input[name='subChk']:checked").each(function () {
            var jsonStr = $(this).val();
            var jsonObj = $.parseJSON(jsonStr);
            checkedList.push(jsonObj.vehicleId);
        });
        subCheckedId = checkedList;
        var deviceType = $("input[name='deviceCheck']:checked").val();
        var settingUrl = settingMoreUrl + "?deviceType=" + deviceType + "&vehicleIds=" + checkedList.toString();
        // $("#settingMoreBtn").attr("href", settingUrl);

        //批量联动
        var deviceType = $("input[name='deviceCheck']:checked").val();
        var url = '/clbs/a/alarmSetting/linkage_' + checkedList.toString() + '?deviceType=' + deviceType;
        $("#batchTactics").attr("href", url);
    },
    // 下发参数 （单个）
    sendfuelOne: function (id, paramId, vehicleId, alarmTypeId) {
        var arr = [];
        var obj = {};
        obj.alarmVehicleId = id;
        obj.paramId = paramId;
        obj.vehicleId = vehicleId;
        obj.alarmTypeId = alarmTypeId
        arr.push(obj);
        var jsonStr = JSON.stringify(arr);
        alarmSetting.sendFuel(jsonStr);
    },
    //批量下发
    sendModelFn: function () {
        //判断是否至少选择一项
        var chechedNum = $("input[name='subChk']:checked").length;
        if (chechedNum == 0) {
            layer.msg("请至少选择一条数据");
            return
        }
        var checkedList = new Array();
        $("input[name='subChk']:checked").each(function () {
            var jsonStr = $(this).val();
            var jsonObj = $.parseJSON(jsonStr);
            checkedList.push(jsonObj);
        });
        // 下发
        alarmSetting.sendFuel(JSON.stringify(checkedList));
    },
    // 下发参数
    sendFuel: function (sendParam) {
        var url = "/clbs/a/alarmSetting/sendAlarm";
        var parameter = {"sendParam": sendParam};
        json_ajax("POST", url, "json", true, parameter, alarmSetting.sendFuelCallback);
    },
    // 下发流量回调方法
    sendFuelCallback: function (data) {
        if (alasendflag) {
            webSocket.subscribe(headers, "/user/topic/alarm_parameter_setting", alarmSetting.updataFenceData, "", null);
            alasendflag = false;
        }
        if (data != null && data != undefined && data != "") {
            if (data.success) {
                layer.msg(sendCommandComplete, {time: 2000}, function (refresh) {
                    //取消全选勾
                    $("#checkAll").prop('checked', false);
                    $("input[name=subChk]").prop("checked", false);
                    myTable.refresh(); //执行的刷新语句
                    layer.close(refresh);
                });
            } else {
                if (!data.msg) data.msg = '请勾选已设置参数的记录';
                layer.msg(data.msg, {time: 2000}, function (refresh) {
                    //取消全选勾
                    $("#checkAll").prop('checked', false);
                    $("input[name=subChk]").prop("checked", false);
                    myTable.refresh(); //执行的刷新语句
                    layer.close(refresh);
                });
            }
        }
    },
    //批量联动
    BatchLinkage: function (e) {
        //判断是否至少选择一项
        var checkNumber = $("input[name='subChk']:checked").length;
        var deviceType = $("input[name='deviceCheck']:checked").val();
        if (checkNumber == 0) {
            var url = '/clbs/a/alarmSetting/settingmore_.gsp';
            $("#batchTactics").attr('href', url);
        } else {
            if ($("#checkAll ").is(":checked") == true) {
                alarmSetting.checkAllClickFn();
            } else {
                alarmSetting.subChkChange();
            }
        }
    },
    //批量删除
    delModelFn: function () {
        //判断是否至少选择一项
        var chechedNum = $("input[name='subChk']:checked").length;
        if (chechedNum == 0) {
            layer.msg("请至少选择一条数据");
            return
        }
        var checkedList = new Array();
        $("input[name='subChk']:checked").each(function () {
            var jsonStr = $(this).val();
            var jsonObj = $.parseJSON(jsonStr);
            checkedList.push(jsonObj.vehicleId);
        });
        myTable.deleteAlarmSettingsItems({
            'deltems': checkedList.toString()
        }, function () {
            layer.msg('请勾选已设置参数的记录');
        });
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
    // 应答
    responseSocket: function () {
        alarmSetting.isGetSocketLayout();
    },
    isGetSocketLayout: function () {
        setTimeout(function () {
            if (webSocket.conFlag) {
                webSocket.subscribe(headers, '/user/topic/check', alarmSetting.updateTable, "/app/vehicle/inspect", null);
            } else {
                alarmSetting.isGetSocketLayout();
            }
        }, 2000);
    },
    // 应答socket回掉函数
    updateTable: function (msg) {
        if (msg != null) {
            var json = $.parseJSON(msg.body);
            var msgData = json.data;
            if (msgData != undefined) {
                var msgId = msgData.msgHead.msgID;
                // if (msgId == 0x9300) {
                //     var dataType = msgData.msgBody.dataType;
                //     $("#msgDataType").val(dataType);
                //     $("#infoId").val(msgData.msgBody.data.infoId);
                //     $("#objectType").val(msgData.msgBody.data.objectType);
                //     $("#objectId").val(msgData.msgBody.data.objectId);
                //     $("#question").text(msgData.msgBody.data.infoContent);
                //     if (dataType == 0x9301) {
                //         $("#answer").val("");
                //         $("#msgTitle").text("平台查岗");
                //         $("#goTraceResponse").modal('show');
                //         $("#error_label").hide();
                //     }
                //     if (dataType == 0x9302) {
                //         $("#answer").val("");
                //         $("#msgTitle").text("下发平台间报文");
                //         $("#goTraceResponse").modal('show');
                //     }
                // }
            }
        }
    },
    // 应答确定
    platformMsgAck: function () {
        var answer = $("#answer").val();
        if (answer == "") {
            alarmSetting.showErrorMsg("应答不能为空", "answer");
            return;
        }
        $("#error_label").hide();
        $("#goTraceResponse").modal('hide');
        var msgDataType = $("#msgDataType").val();
        var infoId = $("#infoId").val();
        var objectType = $("#objectType").val();
        var objectId = $("#objectId").val();
        var url = "/clbs/m/connectionparamsset/platformMsgAck";
        json_ajax("POST", url, "json", false, {
            "infoId": infoId,
            "answer": answer,
            "msgDataType": msgDataType,
            "objectType": objectType,
            "objectId": objectId
        });
    },
    showErrorMsg: function (msg, inputId) {
        if ($("#error_label").is(":hidden")) {
            $("#error_label").text(msg);
            $("#error_label").insertAfter($("#" + inputId));
            $("#error_label").show();
        } else {
            $("#error_label").is(":hidden");
        }
    },
}
$(function () {

    $('input').inputClear().on('onClearEvent', function (e, data) {
        var id = data.id;
        if (id == 'search_condition') {
            search_ztree('treeDemo', id, 'assignment');
        }
        ;
    });
    alarmSetting.init();
    alarmSetting.responseSocket();
    // 组织架构模糊搜索
    $("#search_condition").on("input oninput", function () {
        search_ztree('treeDemo', 'search_condition', 'assignment');
    });
    //改变勾选框
    $(".device").change(function () {
        //取消全选
        $("#checkAll").prop('checked', false);
        //还原批量设置的地址
        var settingUrl = settingMoreUrl;
        // $("#settingMoreBtn").attr("href", settingUrl);
        //刷新表格
        myTable.dataTable.draw(true);
    });
    //IE9
    if (navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") == "MSIE9.0") {
        var search;
        $("#search_condition").bind("focus", function () {
            search = setInterval(function () {
                search_ztree('treeDemo', 'search_condition', 'assignment');
            }, 500);
        }).bind("blur", function () {
            clearInterval(search);
        });
    }
    //IE9 end
    //批量设置
    $('#settingMoreBtn').on('click', function (e) {
        e.preventDefault()
        var checkedListId = [];
        var checkedListName = [];
        $("input[name='subChk']:checked").each(function () {
            var jsonStr = $(this).val();
            var jsonObj = $.parseJSON(jsonStr);
            checkedListId.push(jsonObj.vehicleId);
            checkedListName.push(jsonObj.monitorName)
        });
        if (checkedListId.length == 0) {
            return layer.msg('至少选择一项')
        }
        alarmSettingUtil.showModal(checkedListId.toString(), 2, checkedListName.toString())
    })
    //全选
    $("#checkAll").on("click", alarmSetting.checkAllClickFn);
    //批量下发
    $("#send_model").on("click", alarmSetting.sendModelFn);
    //批量联动
    $("#batchTactics").on("click", alarmSetting.BatchLinkage);
    //批量删除
    $("#del_model").on("click", alarmSetting.delModelFn);
    //查询全部
    $('#refreshTable').on("click", alarmSetting.refreshTableFn);
    // 应答确定
    $('#parametersResponse').on('click', alarmSetting.platformMsgAck);
})