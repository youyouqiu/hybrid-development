var currentPage = 0
function connectHandle(handleType, plantformId) {
    var url = "/clbs/m/connectionparamsset/handle";
    json_ajax("POST", url, "json", false, {'serverCommand': handleType, 'platformId': plantformId}, callback());
}

/**
 * 开启/关闭过滤
 * @param serverCommand    7:关闭过滤; 8:开启过滤
 * */
function filterHandle(serverCommand, plantformId) {
    var url = "/clbs/m/connectionparamsset/handle";
    json_ajax("POST", url, "json", false, {'serverCommand': serverCommand, 'platformId': plantformId}, callback());
}

function callback(data) {

}

function vehicleHandle(type) {
    hideErrorMsg();
    var brand = $("#brand").val();
    var timeInterval = $('#timeInterval').val().split('--');
    var startTime = timeInterval[0];
    var endTime = timeInterval[1];
    // var startTime = $("#startTime").val();
    // var endTime = $("#endTime").val();
    if ($("#applyPlatform").val() == '') {
        showErrorMsg("请选择平台", "applyPlatform");
        return;
    }
    if (brand == "") {
        showErrorMsg("车牌号不能为空", "brand");
        return;
    }
    if (startTime == "" || endTime == "") {
        showErrorMsg("开始时间不能为空", "timeInterval");
        return;
    }
    var applyPlatformId = $("#applyPlatformId").val();
    /*if (endTime == "") {
        showErrorMsg("结束时间不能为空", "endTime");
        return;
    }*/
    var ip = $("#ip").val();
    var centerId = $("#centerId").val();
    var platFormId = $("#platFormId").val();
    var url = "/clbs/m/connectionparamsset/vehicleHandle";
    json_ajax("POST", url, "json", false, {
        'serverCommand': type,
        "brand": brand,
        "startTime": startTime,
        "endTime": endTime,
        "ip": ip,
        "centerId": centerId,
        "platFormId": platFormId
    }, callback());
}

function platformMsgAck(type) {
    hideErrorMsg();
    var answer = $("#answer" + type).val();
    if (answer == "") {
        showErrorMsg("应答不能为空", "answer" + type);
        return;
    }
    $("#goTrace" + type).modal('hide');
    var msgDataType = $("#msgDataType" + type).val();
    var infoId = $("#infoId" + type).val();
    var objectType = $("#objectType" + type).val();
    var objectId = $("#objectId" + type).val();
    var serverIp = $("#serverIp" + type).val();
    var url = "/clbs/m/connectionparamsset/platformMsgAck";
    json_ajax("POST", url, "json", false, {
        "infoId": infoId,
        "answer": answer,
        "msgDataType": msgDataType,
        "objectType": objectType,
        "objectId": objectId,
        "serverIp": serverIp
    }, callback());
}

var params = [];
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

/*function platformAlarmAck() {
    $("#goTrace_alarm").modal('hide');
    var url = "/clbs/m/connectionparamsset/platformAlarmAck";
    json_ajax("POST", url, "json", false, {
        "alarm_handle": $("#alarm_handle").val(),
        "brand": $("#alarm_brand").val()
    }, callback());
}*/
function showErrorMsg(msg, inputId) {
    if ($("#error_label").is(":hidden")) {
        $("#error_label").text(msg);
        $("#error_label").insertAfter($("#" + inputId));
        $("#error_label").show();
    } else {
        $("#error_label").is(":hidden");
    }
}

//错误提示信息隐藏
function hideErrorMsg() {
    $("#error_label").hide();
}


(function ($, window) {

    // 判断整数value是否等于0
    $.validator.addMethod("ifInputRequired", function (value, element, parma) {

        if ($(parma).val() == '' || $(element).val() !== '') {
            return true;
        } 
            return false;
        
    }, "请输入上报间隔时间");

    var $subChk = $("input[name='subChk']");
    var setResource;
    var selectServerIp = "0";//选择的平台ip

    var typePos = '-1';
    var typeTree = [];
    var checkExpand = false;//判断是否展开过车辆树（只用于全局报警、实时监控判断）
    var isCheckedTreeNode = false; // 是否勾选报警查询树中的"全部"节点

    var hasCheckOBJ = [];//保存已选的报警类型
    var alarmTypeName = null;//保存点击的设置报警类型
    var alarmSettingId = '';
    var alarmProtocolType = '';
    var tableData = '';

    platformCheck = {
        //初始化
        init: function (dataTableId) {
            myTable = $(dataTableId).DataTable({
                // "columnDefs": [{
                //    "targets": [0, 1, 2, 4, 5, 6, 7, 8, 9, 10],
                //    "searchable": false
                // }],
                "destroy": true,
                "dom": 'tiprl',// 自定义显示项
                "lengthChange": true,// 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true,// 本地搜索
                "ordering": false, // 排序功能
                "Info": true,// 页脚信息
                "autoWidth": true,// 自动宽度
                "stripeClasses": [],
                "pageLength": 10,
                "lengthMenu": [10, 20, 50, 100, 200],
                "pagingType": "simple_numbers", // 分页样式
                "dom": "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
                "oLanguage": {// 国际语言转化
                    "oAria": {
                        "sSortAscending": " - click/return to sort ascending",
                        "sSortDescending": " - click/return to sort descending"
                    },
                    "sLengthMenu": "显示 _MENU_ 记录",
                    "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录。",
                    "sZeroRecords": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sEmptyTable": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sLoadingRecords": "正在加载数据-请等待...",
                    "sInfoEmpty": "当前显示0到0条，共0条记录",
                    "sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
                    "sProcessing": "<img src='../resources/user_share/row_details/select2-spinner.gif'/> 正在加载数据...",
                    "sSearch": "模糊查询：",
                    "sUrl": "",
                    "oPaginate": {
                        "sFirst": "首页",
                        "sPrevious": " 上一页 ",
                        "sNext": " 下一页 ",
                        "sLast": " 尾页 "
                    }
                },
                "order": [
                    [0, null]
                ],// 第一列排序图标改为默认

            });
            $("#search_button").on("click", function () {
                var tsval = $("#simpleQueryParam").val()
                myTable.search(tsval, false, false).draw();
            });
            platformCheck.getTable("#journalListTable", []);
            // webSocket.init('/clbs/vehicle');
            var str = {position: ["53c09659-2fd4-4e7e-8741-993202a7f333", "53c09659-2fd4-4e7e-8741-993202a7f332", "f5e99fec-f146-415e-a695-404a2422de29"]};
            var url = "/clbs/m/connectionparamsset/list";
            json_ajax("POST", url, "json", true, null, platformCheck.getCallback);
            setTimeout(function () {
                webSocket.subscribe(headers, '/user/topic/check', platformCheck.updateTable, "/app/vehicle/check", null);
            }, 1000)
            //laydate.render({elem: '#startTime',type: 'datetime',theme: '#6dcff6'});
            //laydate.render({elem: '#endTime',type: 'datetime',theme: '#6dcff6'});
        },
        tableDate: [],
        putServerStatus: function (data) {
            $("#serverStatus").text(data.serverStatus);
            $("#mainStatus").text(data.mainStatus);
            $("#branchStatus").text(data.branchStatus);
            $("#mainClient").text(data.mainClient);
            $("#branchServer").text(data.branchServer);
        },
        updateTable: function (msg) {
            if (msg != null) {
                var json = $.parseJSON(msg.body);
                var msgData = json.data;
                if (msgData == null || msgData == undefined) {
                    return;
                }

                //转发平台状态改变则刷新列表
                if (msgData.msgHead != undefined && msgData.msgHead.msgID == 0x0D06) {
                    platformCheck.refreshTableRow(json);
                    // platformCheck.refreshTable();
                }

                var msgId = msgData.msgHead.msgID;
                var serverIp = msgData.msgHead.serverIp;//转发平台地址
                /*  if (msgId == 0x0200) {
                      var alarm = msgData.msgBody.alarm;
                      $("#msgTitle_alarm").text("终端报警");
                      var brand = json.desc.vehicleNo;
                      $("#question_alarm").text(brand + "报警，报警标识：" + alarm);
                      $("#alarm_brand").val(brand);
                      $("#goTrace_alarm").modal('show');
                      return;
                  }*/
                // if (msgId == 0x9300) {
                //     var dataType = msgData.msgBody.dataType;
                //     if (dataType == 0x9301) {
                //    	 $("#msgDataType9301").val(dataType);
                //         $("#infoId9301").val(msgData.msgBody.data.infoId);
                //         $("#objectType9301").val(msgData.msgBody.data.objectType);
                //         $("#objectId9301").val(msgData.msgBody.data.objectId);
                //         $("#question9301").text(msgData.msgBody.data.infoContent);
                //         $("#serverIp9301").val(serverIp);
                //         $("#answer9301").val("");
                //         $("#msgTitle9301").text("平台查岗");
                //         $("#goTrace9301").modal('show');
                //     }
                //     if (dataType == 0x9302) {
                //     	 $("#msgDataType9302").val(dataType);
                //          $("#infoId9302").val(msgData.msgBody.data.infoId);
                //          $("#objectType9302").val(msgData.msgBody.data.objectType);
                //          $("#objectId9302").val(msgData.msgBody.data.objectId);
                //          $("#question9302").text(msgData.msgBody.data.infoContent);
                //          $("#serverIp9302").val(serverIp);
                //          $("#answer9302").val("");
                //          $("#msgTitle9302").text("下发平台间报文");
                //          $("#goTrace9302").modal('show');
                //     }
                // }

                if (serverIp == selectServerIp) { //若数据的平台ip和选择的平台ip一致则做日志处理
                    if (msgData.msgHead.msgID == 37121) {
                        var startTime = new Date(msgData.msgBody.startTime * 1000);
                        msgData.msgBody.startTime = platformCheck.formateDate(startTime);
                        var endTime = new Date(msgData.msgBody.endTime * 1000);
                        msgData.msgBody.endTime = platformCheck.formateDate(endTime);
                    }
                    if (msgData.msgHead.msgID == 37376) {
                        var dataType = msgData.msgBody.dataType;
                        if (dataType != null || dataType !== '') {
                            var result = msgData.msgBody.data.result;
                            if (dataType == 37385) {
                                if (result == 1) {
                                    msgData.msgBody.data.result = "成功,上级平台择机补发";
                                } else if (result == 2) {
                                    msgData.msgBody.data.result = "失败,上级平台无对应申请的定位数据";
                                } else if (result == 3) {
                                    msgData.msgBody.data.result = "失败,申请内容不正确";
                                } else if (result == 4) {
                                    msgData.msgBody.data.result = "其他原因";
                                } else if (result == 0) {
                                    msgData.msgBody.data.result = "成功,上级平台即刻补发";
                                }
                            } else if (dataType == 37383) {
                                if (result == 1) {
                                    msgData.msgBody.data.result = "上级平台没有该车数据";
                                } else if (result == 2) {
                                    msgData.msgBody.data.result = "申请时间段错误";
                                } else if (result == 3) {
                                    msgData.msgBody.data.result = "其他";
                                } else if (result == 0) {
                                    msgData.msgBody.data.result = "申请成功";
                                }
                            } else if (dataType == 37384) {
                                if (result == 1) {
                                    msgData.msgBody.data.result = "之前没有对应申请信息";
                                } else if (result == 2) {
                                    msgData.msgBody.data.result = "失其他原因";
                                } else if (result == 0) {
                                    msgData.msgBody.data.result = "取消申请成功";
                                }
                            }
                            msgId = dataType;
                        }
                    }
                    var bodyStr = JSON.stringify(msgData.msgBody);
                    if (bodyStr != undefined && bodyStr != null) {
                        bodyStr = bodyStr.replace(/"vehicleColor"/g, '"车牌颜色"');
                        bodyStr = bodyStr.replace(/"vehicleNo"/g, '"车牌号"');
                        bodyStr = bodyStr.replace(/"vin"/g, '"车牌号"');
                        bodyStr = bodyStr.replace(/,"dataLength":\d*,"dataType":\d*/g, '');
                        bodyStr = bodyStr.replace(/"dataLength":\d*,"dataType":\d*,/g, '');
                        bodyStr = bodyStr.replace(/"downLinkIp"/g, '"从链路IP"');
                        bodyStr = bodyStr.replace(/"downLinkPort"/g, '"从链路端口"');
                        bodyStr = bodyStr.replace(/"password"/g, '"密码"');
                        bodyStr = bodyStr.replace(/"userID"/g, '"用户ID"');
                        bodyStr = bodyStr.replace(/"ia":20000000,"ic":30000000,"m":10000000,/g, '');
                        bodyStr = bodyStr.replace(/,"warnSrc":\d*":\d*/g, '');
                        bodyStr = bodyStr.replace(/,"warnSrc":\d*"/g, '');
                        bodyStr = bodyStr.replace(/"result"/g, '"结果"');
                        bodyStr = bodyStr.replace(/"verifyCode"/g, '"校验码"');
                        bodyStr = bodyStr.replace(/"startTime"/g, '"开始时间"');
                        bodyStr = bodyStr.replace(/"endTime"/g, '"结束时间"');
                        bodyStr = bodyStr.replace(/"data"/g, '"数据体"');
                        bodyStr = bodyStr.replace(/"licence"/g, '"从业资格证号"');
                        bodyStr = bodyStr.replace(/"orgName"/g, '"发证机构名称"');
                        bodyStr = bodyStr.replace(/"driverId"/g, '"身份证编号"');
                        bodyStr = bodyStr.replace(/"driverName"/g, '"驾驶员姓名"');
                        bodyStr = bodyStr.replace(/,"ewaybillLength":\d*/g, '');
                        bodyStr = bodyStr.replace(/"ewaybillLength":\d*,/g, '');
                        bodyStr = bodyStr.replace(/,"infoLength":\d*/g, '');
                        bodyStr = bodyStr.replace(/"infoLength":\d*,/g, '');
                        bodyStr = bodyStr.replace(/,"warnSrc":\d*/g, '');
                        bodyStr = bodyStr.replace(/"warnSrc":\d*,/g, '');
                        bodyStr = bodyStr.replace(/,"year":\d*/g, '');
                        bodyStr = bodyStr.replace(/"year":\d*,/g, '');
                        bodyStr = bodyStr.replace(/,"minute":\d*/g, '');
                        bodyStr = bodyStr.replace(/"minute":\d*,/g, '');
                        bodyStr = bodyStr.replace(/,"second":\d*/g, '');
                        bodyStr = bodyStr.replace(/"second":\d*,/g, '');
                        bodyStr = bodyStr.replace(/,"hour":\d*/g, '');
                        bodyStr = bodyStr.replace(/"hour":\d*,/g, '');
                        bodyStr = bodyStr.replace(/,"month":\d*/g, '');
                        bodyStr = bodyStr.replace(/"month":\d*,/g, '');
                        bodyStr = bodyStr.replace(/,"day":\d*/g, '');
                        bodyStr = bodyStr.replace(/"day":\d*,/g, '');
                        bodyStr = bodyStr.replace(/,"excrypt":\d*/g, '');
                        bodyStr = bodyStr.replace(/"excrypt":\d*,/g, '');
                        bodyStr = bodyStr.replace(/,"warnLength":\d*/g, '');
                        bodyStr = bodyStr.replace(/"warnLength":\d*,/g, '');
                        bodyStr = bodyStr.replace(/,"objectType":\d*/g, '');
                        bodyStr = bodyStr.replace(/"objectType":\d*,/g, '');
                        bodyStr = bodyStr.replace(/,"msgSequence":\d*/g, '');
                        bodyStr = bodyStr.replace(/"msgSequence":\d*,/g, '');
                        bodyStr = bodyStr.replace(/,"msgLength":\d*/g, '');
                        bodyStr = bodyStr.replace(/"msgLength":\d*,/g, '');
                        bodyStr = bodyStr.replace(/,"traveldataLength":\d*/g, '');
                        bodyStr = bodyStr.replace(/"traveldataLength":\d*,/g, '');
                        bodyStr = bodyStr.replace(/,"photoLen":\d*/g, '');
                        bodyStr = bodyStr.replace(/"photoLen":\d*,/g, '');
                        bodyStr = bodyStr.replace(/,"lensId":\d*/g, '');
                        bodyStr = bodyStr.replace(/"lensId":\d*,/g, '');
                        bodyStr = bodyStr.replace(/,"sizeType":\d*/g, '');
                        bodyStr = bodyStr.replace(/"sizeType":\d*,/g, '');
                        bodyStr = bodyStr.replace(/,"photoRspFlag":\d*/g, '');
                        bodyStr = bodyStr.replace(/"photoRspFlag":\d*,/g, '');
                        bodyStr = bodyStr.replace(/,"type":\d*/g, '');
                        bodyStr = bodyStr.replace(/"type":\d*,/g, '');
                        bodyStr = bodyStr.replace(/"ewaybillInfo"/g, '"电子运单数据体"');
                        bodyStr = bodyStr.replace(/"objectId"/g, '"对象ID"');
                        bodyStr = bodyStr.replace(/"infoId"/g, '"信息ID"');
                        bodyStr = bodyStr.replace(/"supervisionId"/g, '"信息ID"');
                        bodyStr = bodyStr.replace(/"warnTime"/g, '"报警时间"');
                        bodyStr = bodyStr.replace(/"warnType"/g, '"报警类型"');
                        bodyStr = bodyStr.replace(/"infoContent"/g, '"信息内容"');
                        bodyStr = bodyStr.replace(/"plateformId"/g, '"平台唯一编码"');
                        bodyStr = bodyStr.replace(/"terminalSimcode"/g, '"终端手机号"');
                        bodyStr = bodyStr.replace(/"producerId"/g, '"制造商ID"');
                        bodyStr = bodyStr.replace(/"terminalModelType"/g, '"终端型号"');
                        bodyStr = bodyStr.replace(/"terminalId"/g, '"终端号"');
                        bodyStr = bodyStr.replace(/"altitude"/g, '"海拔"');
                        bodyStr = bodyStr.replace(/"time"/g, '"时间"');
                        bodyStr = bodyStr.replace(/"alarm"/g, '"报警"');
                        bodyStr = bodyStr.replace(/"state"/g, '"状态"');
                        bodyStr = bodyStr.replace(/"lat"/g, '"纬度"');
                        bodyStr = bodyStr.replace(/"lon"/g, '"经度"');
                        bodyStr = bodyStr.replace(/"direction"/g, '"方向"');
                        bodyStr = bodyStr.replace(/"vec1"/g, '"GPS速度"');
                        bodyStr = bodyStr.replace(/"vec2"/g, '"终端速度"');
                        bodyStr = bodyStr.replace(/"vec3"/g, '"里程"');
                        bodyStr = bodyStr.replace(/"gnssCnt"/g, '"补报GPS信息数量"');
                        bodyStr = bodyStr.replace(/"vehicleStatic"/g, '"车辆静态数据"');
                        bodyStr = bodyStr.replace(/"transType"/g, '"运输行业编码"');
                        bodyStr = bodyStr.replace(/"reasonCode"/g, '"返回码"');
                        bodyStr = bodyStr.replace(/"warnContent"/g, '"报警内容"');
                        bodyStr = bodyStr.replace(/"gpsList"/g, '"GPS数据项"');
                        bodyStr = bodyStr.replace(/"errorCode"/g, '"错误码"');
                        bodyStr = bodyStr.replace(/"supervisionEndTime"/g, '"结束时间"');
                        bodyStr = bodyStr.replace(/"supervisionEndTime"/g, '"结束时间"');
                        bodyStr = bodyStr.replace(/"supervisionTel"/g, '"电话"');
                        bodyStr = bodyStr.replace(/"supervisionEmal"/g, '"Email"');
                        bodyStr = bodyStr.replace(/"supervisor"/g, '"督办人"');
                        bodyStr = bodyStr.replace(/"supervisionLevel"/g, '"督办级别"');
                        bodyStr = bodyStr.replace(/"monitorTel"/g, '"电话"');
                        bodyStr = bodyStr.replace(/"msgPriority"/g, '"报文优先级"');
                        bodyStr = bodyStr.replace(/"msgContent"/g, '"报文信息内容"');
                        bodyStr = bodyStr.replace(/"msgID"/g, '"消息ID"');
                        bodyStr = bodyStr.replace(/"commandType"/g, '"命令字"');
                        bodyStr = bodyStr.replace(/"traveldataInfo"/g, '"车辆行驶记录仪信息"');
                        bodyStr = bodyStr.replace(/"passWord"/g, '"拨号密码"');
                        bodyStr = bodyStr.replace(/"tcpPort"/g, '"服务器TCP端口"');
                        bodyStr = bodyStr.replace(/"udpPort"/g, '"服务器UDP端口"');
                        bodyStr = bodyStr.replace(/"userName"/g, '"用户名"');
                        bodyStr = bodyStr.replace(/"serverIp"/g, '"服务器IP"');
                        bodyStr = bodyStr.replace(/"accessPointName"/g, '"拨号名称"');
                        bodyStr = bodyStr.replace(/"authenicationCode"/g, '"鉴权码"');
                        bodyStr = bodyStr.replace(/"gnssData"/g, '"GPS数据"');
                    }
                    var time = json.desc.sysTime;
                    var timeStr = "20" + time.substring(0, 2) + "-" + time.substring(2, 4) + "-" + time.substring(4, 6) + " " + time.substring(6, 8) + ":"
                        + time.substring(8, 10) + ":" + time.substring(10, 12);
                    if (platformCheck.tableDate.length >= 20) {
                        platformCheck.tableDate.splice(platformCheck.tableDate.length - 1, 1);
                    }
                    platformCheck.tableDate.unshift([platformCheck.tableDate.length + 1, timeStr, "0x" + msgId.toString(16), msgData.msgHead.msgSn, bodyStr, ""])
                }
            }
            platformCheck.getTable("#journalListTable", platformCheck.tableDate);
        },

        refreshTableRow: function (data) {
            /* var url = "/clbs/m/connectionparamsset/list";
             json_ajax("POST", url, "json", true, null, platformCheck.getCallback);*/
            var id = data.desc.t809PlatId;
            var url = "/clbs/m/connectionparamsset/list";
            var rowObj = '';
            json_ajax("POST", url, "json", true, null, function (message) {
                var data = message.obj;
                for (var i = 0; i < data.length; i++) {
                    if (data[i].id == id) {
                        rowObj = data[i]
                    }
                }
                // console.log('更新对象', rowObj);
                setTimeout(function () {
                    var permission = $('#permission').val();
                    for (var i = 0; i < tableData.length; i++) {
                        if (tableData[i].id == id && rowObj != '') {
                            // console.log('更新');
                            var rowData = myTable.row(i).data();//获取改行的数据对象
                            if (permission === 'true') {
                                rowData[10] = rowObj.branchStatusName;
                                rowData[9] = rowObj.mainStatusName;
                                rowData[8] = rowObj.branchServerName;
                                rowData[7] = rowObj.mainClientName;
                                rowData[6] = rowObj.dataFilterStatus === 1 ? '开启' : '关闭';
                                rowData[5] = rowObj.serverStatusName;
                                rowData[4] = rowObj.protocolTypeName;
                                rowData[3] = rowObj.platformName;
                                rowData[2] = platformCheck.handleButton(rowObj);
                            } else {
                                rowData[8] = rowObj.branchStatusName;
                                rowData[7] = rowObj.mainStatusName;
                                rowData[6] = rowObj.branchServerName;
                                rowData[5] = rowObj.mainClientName;
                                rowData[4] = rowObj.dataFilterStatus === 1 ? '开启' : '关闭';
                                rowData[3] = rowObj.serverStatusName;
                                rowData[2] = rowObj.protocolTypeName;
                                rowData[1] = rowObj.platformName;
                            }
                            myTable.row(i).data(rowData);
                        }
                    }
                }, 500)
            });
        },

        formateDate: function (dateTime) {
            return dateTime.getFullYear() + "-"
                + ((dateTime.getMonth() + 1) < 10 ? "0" + (dateTime.getMonth() + 1) : (dateTime.getMonth() + 1))
                + "-" + (dateTime.getDate() < 10 ? "0" + dateTime.getDate() : dateTime.getDate())
                + " "
                + (dateTime.getHours() < 10 ? "0" + dateTime.getHours() : dateTime.getHours()) + ":"
                + (dateTime.getMinutes() < 10 ? "0" + dateTime.getMinutes() : dateTime.getMinutes()) + ":"
                + (dateTime.getSeconds() < 10 ? "0" + dateTime.getSeconds() : dateTime.getSeconds())
        },
        getTable: function (table, data) {
            // var thisData = [];
            if (data != '') {
                for (var i = 0, t = data.length; i < t; i++) {
                    // thisData.push(data[i]);
                    data[i][0] = i + 1;
                }
                ;
            }
            ;
            table = $(table).DataTable(
                {
                    "destroy": true,
                    "data": data,
                    "scrollY": '250px',
                    "scrollX": true,
                    "lengthChange": false,// 是否允许用户自定义显示数量
                    "bPaginate": false, // 翻页功能
                    "bFilter": false, // 列筛序功能
                    "searching": false,// 本地搜索
                    "ordering": false, // 排序功能
                    "info": false,// 页脚信息
                    "autoWidth": true,// 自动宽度
                    "stripeClasses": [],
                    "pageLength": 10,
                    "lengthMenu": [5, 10, 20, 50, 100, 200],
                    "pagingType": "simple_numbers", // 分页样式
                    "oLanguage": {// 国际语言转化
                        "oAria": {
                            "sSortAscending": " - click/return to sort ascending",
                            "sSortDescending": " - click/return to sort descending"
                        },
                        "sLengthMenu": "显示 _MENU_ 记录",
                        "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录。",
                        "sZeroRecords": "对不起，查询不到任何相关数据",
                        "sEmptyTable": "对不起，查询不到任何相关数据",
                        "sLoadingRecords": "正在加载数据-请等待...",
                        "sInfoEmpty": "当前显示0到0条，共0条记录",
                        "sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
                        "sProcessing": "<img src='../resources/user_share/row_details/select2-spinner.gif'/> 正在加载数据...",
                        "sSearch": "模糊查询：",
                        "sUrl": "",
                        "oPaginate": {
                            "sFirst": "首页",
                            "sPrevious": " 上一页 ",
                            "sNext": " 下一页 ",
                            "sLast": " 尾页 "
                        }
                    },
                });
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
            $('.toggle-vis').off('change').on('change', function (e) {
                var column = myTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
        },
        //加载列表数据
        reloadData: function (dataList) {
            var currentPage = myTable.page()
            myTable.clear()
            myTable.rows.add(dataList)
            myTable.page(currentPage).draw(true);
            myTable.search('', false, false).page(currentPage).draw();
        },
        getCallback: function (data) {
            currentPage = myTable.page.info().page
            if (data.success) {
                tableData = data.obj;
                dataListArray = [];//用来储存显示数据
                if (data.obj != null && data.obj.length != 0) {
                    var value = data.obj;
                    var permission = $("#permission").val();
                    for (var i = 0; i < value.length; i++) {
                        if (permission == "true") {
                            var dataList =
                                [
                                    i + 1,
                                    '<input type="checkbox" name="subChk" value="' + value[i].id + '" /> ',
                                    platformCheck.handleButton(value[i]),
                                    value[i].platformName,
                                    value[i].protocolTypeName,
                                    value[i].serverStatusName,
                                    value[i].dataFilterStatus === 1 ? '开启' : '关闭',
                                    value[i].mainClientName,
                                    value[i].branchServerName,
                                    value[i].mainStatusName,
                                    value[i].branchStatusName
                                ];
                        } else {
                            var dataList =
                                [
                                    i + 1,
                                    value[i].platformName,
                                    value[i].protocolTypeName,
                                    value[i].serverStatusName,
                                    value[i].dataFilterStatus === 1 ? '开启' : '关闭',
                                    value[i].mainClientName,
                                    value[i].branchServerName,
                                    value[i].mainStatusName,
                                    value[i].branchStatusName
                                ];
                        }
                        dataListArray.push(dataList);
                    }
                    platformCheck.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                } else {
                    platformCheck.reloadData(dataListArray);
                    $("#simpleQueryParam").val("");
                }
                //组装两个平台名称下拉框值
                var datas = data.obj;
                var dataList = {value: []}, i = datas.length;
                while (i--) {
                    dataList.value.push({
                        name: datas[i].platformName,
                        id: datas[i].id
                    });
                }
                $("#applyPlatform").bsSuggest("destroy"); // 销毁事件
                $("#applyPlatform").bsSuggest({
                    indexId: 1, //data.value 的第几个数据，作为input输入框的内容
                    indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                    data: dataList,
                    effectiveFields: ["name"]
                }).on('onDataRequestSuccess', function (e, result) {
                }).on('onSetSelectValue', function (e, keyword, data) {
                    $("#applyPlatformId").val(keyword.id);
                    platformCheck.findPlantParamById(keyword.id);
                }).on('onUnsetSelectValue', function () {
                });

                var logDataList = {value: []}, i = datas.length;
                while (i--) {
                    logDataList.value.push({
                        name: datas[i].platformName,
                        id: datas[i].ip
                    });
                }
                $("#logPlatform").bsSuggest("destroy"); // 销毁事件
                $("#logPlatform").bsSuggest({
                    indexId: 1, //data.value 的第几个数据，作为input输入框的内容
                    indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                    data: logDataList,
                    effectiveFields: ["name"]
                }).on('onDataRequestSuccess', function (e, result) {
                }).on('onSetSelectValue', function (e, keyword, data) {
                    $("#logPlatformIp").val(keyword.id);
                    var pip = keyword.id;
                    if (pip != selectServerIp) {
                        //清除原有日志记录
                        platformCheck.getTable("#journalListTable", []);
                        //初始化日志数组
                        platformCheck.tableDate = [];
                        selectServerIp = pip;
                    }
                }).on('onUnsetSelectValue', function () {
                });
                // 尝试通过配置iDisplayStart来解决,未果,因此用下面这个hack方法
                myTable.jumpTo(currentPage)
            } else {
                layer.msg(data.msg, {move: false});
            }
        },
        //获取平台信息
        findPlantParamById: function (id) {
            var url = "/clbs/m/connectionparamsset/findPlantParamById";
            var data = {"id": id};
            json_ajax("POST", url, "json", true, data, function (data) {
                if (data.success) {
                    $("#ip").val(data.obj.ip);
                    $("#centerId").val(data.obj.centerId);
                    $("#platFormId").val(data.obj.id);
                } else {
                    layer.msg(data.msg);
                }
            });
        },
        //操作设置按钮组装
        handleButton: function (row) {
            var editUrlPath = '/clbs/m/connectionparamsset/edit_' + row.id; //修改地址
            var detailUrlPath = '/clbs/m/connectionparamsset/detail_' + row.id;//详情地址
            var result = '';
            if (row.serverStatus == 1) {
                //修改按钮
                result += '<button disabled href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>修改</button>&ensp;';
                //删除按钮
                result += '<button disabled type="button" onclick="platformCheck.deleteParamSet(\'' + row.id + '\')" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>删除</button>&ensp;';
            } else {
                //修改按钮
                result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                //删除按钮
                result += '<button type="button" onclick="platformCheck.deleteParamSet(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>&ensp;';
            }

//            //开启服务
//            result += '<button onclick="connectHandle(1)" type="button" class="editBtn editBtn-info default-icon"><i class="fa fa-retweet"></i>开启服务</button>&ensp;';
//            //关闭服务
//            result += '<button onclick="connectHandle(0)" type="button" class="editBtn editBtn-info default-icon"><i class="fa fa-retweet"></i>关闭服务</button>&ensp;';
//            //主链路连接
//            result += '<button onclick="connectHandle(2)" type="button" class="editBtn editBtn-info default-icon"><i class="fa fa-retweet"></i>主链路连接</button>&ensp;'
//            //主链路注销
//            result += '<button onclick="connectHandle(4)" type="button" class="deleteButton editBtn disableClick default-icon"><i class="fa fa-chain-broken"></i>主链路注销</button>&ensp;';
//			//详情按钮
//            result += '<button href="' + detailUrlPath + '" data-target="#commonWin" data-toggle="modal" type="button" class=" editBtn editBtn-info"><i class="fa fa-sun-o"></i>详情</button>&ensp;';

            result += '<div class="btn-group"><button class="editBtn editBtn-info" type="button" id="dropdownMenuOther" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">其他<span class="caret"></span></button>' +
                '<ul class="dropdown-menu" aria-labelledby="dropdownMenuOther" id="dropdownMenu809Param">' +
                '<li><a href="' + detailUrlPath + '" data-target="#commonWin" data-toggle="modal">详情</button></li>';
            // '<li><a onclick="connectHandle(1,\'' + row.id + '\')">开启服务</a></li>' +
            // '<li><a onclick="connectHandle(0,\'' + row.id + '\')">关闭服务</a></li>' +
            // '<li><a onclick="connectHandle(2,\'' + row.id + '\')">主链路连接</a></li>' +
            // '<li><a onclick="connectHandle(4,\'' + row.id + '\')">主链路注销</a></li>' +
            // '<li><a onclick="connectHandle(5,\'' + row.id + '\')">从链路注销</a></li>' +
            // '<li><a onclick="connectHandle(6,\'' + row.id + '\')">发送时效口令</a></li>';
            if (row.serverStatusName == '关闭') {
                result += '<li><a onclick="connectHandle(1,\'' + row.id + '\')">开启服务</a></li>';
                if (row.dataFilterStatus === 1) {
                    result += '<li><a onclick="filterHandle(7,\'' + row.id + '\')">关闭过滤</a></li>';
                } else {
                    result += '<li><a onclick="filterHandle(8,\'' + row.id + '\')">开启过滤</a></li>';
                }
            } else {
                result += '<li><a onclick="connectHandle(0,\'' + row.id + '\')">关闭服务</a></li>';
                if (row.dataFilterStatus === 1) {
                    result += '<li><a onclick="filterHandle(7,\'' + row.id + '\')">关闭过滤</a></li>';
                } else {
                    result += '<li><a onclick="filterHandle(8,\'' + row.id + '\')">开启过滤</a></li>';
                }
                var mainStatus = row.mainStatus;//主链路状态
                var branchStatus = row.branchStatus;//从链路状态

                if ((mainStatus == 0 || mainStatus == 2 || mainStatus == 3 || mainStatus == 4 || mainStatus == 5) && branchStatus == 1) {
                    result += '<li><a onclick="connectHandle(2,\'' + row.id + '\')">主链路连接</a></li>' +
                        '<li><a onclick="connectHandle(5,\'' + row.id + '\')">从链路注销</a></li>';
                }

                if ((mainStatus == 0 || mainStatus == 2 || mainStatus == 3 || mainStatus == 4 || mainStatus == 5) && (branchStatus == 0 || branchStatus == 2 || branchStatus == 3 || branchStatus == 4)) {
                    result += '<li><a onclick="connectHandle(2,\'' + row.id + '\')">主链路连接</a></li>';
                }

                if ((mainStatus == 6 || mainStatus == 7 || mainStatus == 8 || mainStatus == 9) && branchStatus == 1) {
                    result += '<li><a onclick="connectHandle(4,\'' + row.id + '\')">主链路注销</a></li>' +
                        '<li><a onclick="connectHandle(5,\'' + row.id + '\')">从链路注销</a></li>';
                }

                if ((mainStatus == 6 || mainStatus == 7 || mainStatus == 8 || mainStatus == 9) && (branchStatus == 0 || branchStatus == 2 || branchStatus == 3 || branchStatus == 4)) {
                    result += '<li><a onclick="connectHandle(4,\'' + row.id + '\')">主链路注销</a></li>';
                }
                if (mainStatus == 1 && branchStatus == 1) {
                    result += '<li><a onclick="connectHandle(4,\'' + row.id + '\')">主链路注销</a></li>' +
                        '<li><a onclick="connectHandle(5,\'' + row.id + '\')">从链路注销</a></li>' +
                        '<li><a onclick="connectHandle(6,\'' + row.id + '\')">发送时效口令</a></li>';
                }
                if (mainStatus == 1 && (branchStatus == 0 || branchStatus == 2 || branchStatus == 3 || branchStatus == 4)) {
                    result += '<li><a onclick="connectHandle(4,\'' + row.id + '\')">主链路注销</a></li>' +
                        '<li><a onclick="connectHandle(6,\'' + row.id + '\')">发送时效口令</a></li>';
                }
            }
            var hasAlarmBtn = [0, 1];// 796-809和1078-809显示报警设置按钮
            if (hasAlarmBtn.indexOf(row.protocolType) !== -1) {// 协议类型,去除报警设置按钮
                result += '<li><a onclick="platformCheck.alarmSet(\'' + row.protocolType + '\',\'' + row.id + '\',\'' + row.protocolTypeName + '\')">报警设置</a></li>';
            }

            result += '</ul></div>';
            return result;
        },
        alarmSet: function (protocolType, id, typename) {
            alarmTypeName = typename;
            alarmSettingId = id;
            alarmProtocolType = protocolType;

            hasCheckOBJ = [];
            $('#alarmSetmodal input').val('');
            for (var i = 1; i < 29; i++) {
                $('#alarmTime' + i).val('0');
            }


            var typename = typename;
            isCheckedTreeNode = false;
            json_ajax('post', '/clbs/m/connectionparamsset/getAlarmType', 'json', true, {"protocolType": protocolType}, function (res) {
                if (res.success) {
                    var typeVal = JSON.parse(res.msg);
                    for (var i = 1; i < 29; i++) {
                        platformCheck.initAlarmTree(typeVal, 'treeTypeDemo' + i, 'groupSelect' + i)
                    }

                    if (typename == '35658-809' || typename == 'JTT-809' || typename == "796-809" || typename == "黑龙江-809") {
                        $('.xzAlarm,.sxAlarm,.Alarm-1078').hide();
                    }
                    if (typename == '西藏-809') {
                        $('.sxAlarm,.Alarm-1078').hide();
                        $('.xzAlarm').show();
                    }
                    if (typename == '山西-809' || typename == "四川-809") {
                        $('.xzAlarm,.Alarm-1078').hide();
                        $('.sxAlarm').show();
                    }
                    if (typename == '1078-809') {
                        $('.xzAlarm,.sxAlarm').hide();
                        $('.Alarm-1078').show();
                    }


                    var parm = {
                        settingId: id,
                        protocolType: protocolType
                    }
                    json_ajax('post', '/clbs/m/connectionparamsset/get809AlarmMapping', 'json', true, parm, function (data) {
                        // console.log('data',data)

                        if (data.success) {
                            if (data.obj.flag == '0') {
                                platformCheck.checkTreeNode('treeTypeDemo1', '1') //超速报警默认超速报警pos
                                platformCheck.checkTreeNode('treeTypeDemo2', '2') //疲劳驾驶报警默认疲劳驾驶
                                platformCheck.checkTreeNode('treeTypeDemo3', '0') //紧急报警默认紧急报警
                                platformCheck.checkTreeNode('treeTypeDemo4', '2011,2111')
                                platformCheck.checkTreeNode('treeTypeDemo5', '2012,2112')
                                platformCheck.checkTreeNode('treeTypeDemo9', '26')
                                platformCheck.checkTreeNode('treeTypeDemo11', '23')
                                platformCheck.checkTreeNode('treeTypeDemo12', '28')
                                platformCheck.checkTreeNode('treeTypeDemo13', '18')
                                if (typename == '1078-809') {
                                    platformCheck.checkTreeNode('treeTypeDemo22', '125')
                                    platformCheck.checkTreeNode('treeTypeDemo23', '126')
                                    platformCheck.checkTreeNode('treeTypeDemo24', '1271,1272')
                                    platformCheck.checkTreeNode('treeTypeDemo25', '128')
                                    platformCheck.checkTreeNode('treeTypeDemo26', '129')
                                    platformCheck.checkTreeNode('treeTypeDemo27', '130')
                                    platformCheck.checkTreeNode('treeTypeDemo28', '131')
                                }
                                if (typename == '山西-809') {
                                    platformCheck.checkTreeNode('treeTypeDemo18', '76')
                                    platformCheck.checkTreeNode('treeTypeDemo19', '77,7702,7703')
                                }
                                if (typename == '四川-809') {
                                    platformCheck.checkTreeNode('treeTypeDemo11', '23,147')
                                    platformCheck.checkTreeNode('treeTypeDemo18', '76')
                                    platformCheck.checkTreeNode('treeTypeDemo19', '77,7702,7703')
                                }
                            }

                            if (data.obj.flag == '1') {
                                var res = JSON.parse(data.obj.result);
                                for (var i = 0; i < res.length; i++) {
                                    var pos809 = res[i].pos809;
                                    var pos808s = res[i].pos808.split(",");
                                    var time = res[i].time;
                                    var pos808 = [];
                                    if (pos809 == '0x0024') {
                                        if (typename == '西藏-809') {
                                            pos809 = '0x0024_xz';
                                        }
                                        if (typename == '山西-809' || typename == '四川-809') {
                                            pos809 = '0x0024_sx';
                                        }
                                    }
                                    for (var j = 0; j < pos808s.length; j++) {
                                        if (pos808s[j].indexOf("125") != -1) {
                                            if ($.inArray("125", pos808) == -1) {
                                                pos808.push("125");
                                            }
                                            continue;
                                        } else if (pos808s[j].indexOf("126") != -1) {
                                            if ($.inArray("126", pos808) == -1) {
                                                pos808.push("126");
                                            }
                                            continue;
                                        } else if (pos808s[j].indexOf("1271") != -1) {
                                            if ($.inArray("1271", pos808) == -1) {
                                                pos808.push("1271");
                                            }
                                            continue;
                                        } else if (pos808s[j].indexOf("1272") != -1) {
                                            if ($.inArray("1272", pos808) == -1) {
                                                pos808.push("1272");
                                            }
                                            continue;
                                        } else if (pos808s[j].indexOf("130") != -1) {
                                            if ($.inArray("130", pos808) == -1) {
                                                pos808.push("130");
                                            }
                                            continue;
                                        } else {
                                            pos808.push(pos808s[j]);
                                        }
                                    }
                                    var check = "";
                                    for (var j = 0; j < pos808.length; j++) {
                                        if (j != pos808.length - 1) {
                                            check = check + pos808[j] + ",";
                                        } else {
                                            check = check + pos808[j];
                                        }
                                    }

                                    if ($("input[pos809 = '" + pos809 + "']").length > 0) {
                                        var inputId = $("input[pos809 = '" + pos809 + "']").attr('id').replace('groupSelect', '');
                                        $("#alarmTime" + inputId).val(time);
                                        platformCheck.checkTreeNode('treeTypeDemo' + inputId, check)
                                    }
                                }
                            }
                        }

                        $('#alarmSetmodal').modal('show')
                    })
                }
            })
        },
        initAlarmTree: function (typeVal, id, inputId) {
            typeTree = [{
                name: '全部',
                open: true,
                pos: '-1',
                isParent: true,
                checked: false,
                children: [
                    {
                        name: '预警',
                        isParent: true,
                        checked: false,
                        children: [],
                        pos: '-3'
                    }, {
                        name: '驾驶员引起报警',
                        isParent: true,
                        checked: false,
                        children: [],
                        pos: '-4'
                    }, {
                        name: '车辆报警',
                        isParent: true,
                        checked: false,
                        children: [],
                        pos: '-5'
                    }, {
                        name: '故障报警',
                        isParent: true,
                        checked: false,
                        children: [],
                        pos: '-6'
                    }, {
                        name: '视频报警',
                        isParent: true,
                        checked: false,
                        children: [],
                        pos: '-7'
                    }, {
                        name: '平台报警',
                        isParent: true,
                        checked: false,
                        children: [],
                        pos: '-8'
                    }
                ]
            }];
            for (var i = 0; i < typeVal.length; i++) {
                var sFlag = typeVal[i].type;
                var ty = typeVal[i];
                ty.checked = false;

                var isNeedChecked = false;
                switch (sFlag) {
                    case "alert":
                        typeTree[0].children[0].children.push(ty);
                        platformCheck.alarmTypeTreeChecked(isNeedChecked, 0);
                        break;
                    case "driverAlarm":
                        typeTree[0].children[1].children.push(ty);
                        platformCheck.alarmTypeTreeChecked(isNeedChecked, 1);
                        break;
                    case "vehicleAlarm":
                        typeTree[0].children[2].children.push(ty);
                        platformCheck.alarmTypeTreeChecked(isNeedChecked, 2);
                        break;
                    case "faultAlarm":
                        typeTree[0].children[3].children.push(ty);
                        platformCheck.alarmTypeTreeChecked(isNeedChecked, 3);
                        break;
                    case "videoAlarm":
                        typeTree[0].children[4].children.push(ty);
                        platformCheck.alarmTypeTreeChecked(isNeedChecked, 4);
                        break;
                    case "platAlarm":
                        typeTree[0].children[5].children.push(ty);
                        platformCheck.alarmTypeTreeChecked(isNeedChecked, 5);
                        break;

                }
            }

            if (isCheckedTreeNode) {
                typeTree[0].checked = true;
            }

            var setting = {
                async: {
                    type: "post",
                    enable: true,
                    autoParam: ["type"],
                    dataType: "json",
                    icon: false,
                    otherParam: {"type": "multiple", "icoType": "0"},
                },
                check: {
                    enable: true,
                    chkStyle: "checkbox",
                    radioType: "all"
                },
                view: {
                    dblClickExpand: false,
                    nameIsHTML: true,
                    showIcon: false
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    onCheck: function (e, treeId, treeNode) {
                        platformCheck.onCheckType(e, treeId, treeNode, id, inputId);
                    },
                    onClick: function (e, treeId, treeNode) {
                        platformCheck.onClickBack(e, treeId, treeNode, id, inputId);
                    }
                }
            };
            var typeTreeObj = $.fn.zTree.init($('#' + id), setting, typeTree);
        },
        //勾选报警类型
        checkTreeNode: function (id, pos) {
            var zTree = $.fn.zTree.getZTreeObj(id);


            var pos = pos.indexOf(',') === -1 ? [pos] : pos.split(',');

            //勾选对应的
            var nodes = [];
            for (var n = 0; n < pos.length; n++) {
                var nodesArr = zTree.getNodesByParam("pos", pos[n], null);
                nodes = nodes.concat(nodesArr)
            }

            for (var i = 0; i < nodes.length; i++) {
                zTree.checkNode(nodes[i], true, true, true, true);
                platformCheck.saveHasCheckOBJ(id, nodes[i].pos);
            }

            //展开勾选的
            var nodesExpand = zTree.getNodesByParam("checked", true, null);
            for (var j = 0; j < nodesExpand.length; j++) {
                if (nodesExpand[j].name != '全部') {
                    zTree.expandNode(nodesExpand[j], true, true, true);
                }
            }

        },
        //保存不同对象下勾选的报警类型
        saveHasCheckOBJ: function (id, pos) {
            if (hasCheckOBJ.length) {
                var ifexist = false;
                for (var i = 0; i < hasCheckOBJ.length; i++) {
                    if (hasCheckOBJ[i].id == id) {
                        if (hasCheckOBJ[i].posArr.indexOf(pos) == -1) {
                            hasCheckOBJ[i].posArr.push(pos)
                        }
                        ifexist = true;
                    }
                }
                if (!ifexist) {
                    var obj = {
                        id: id,
                        posArr: [pos]
                    }
                    hasCheckOBJ.push(obj)
                }
            } else {
                var obj = {
                    id: id,
                    posArr: [pos]
                }
                hasCheckOBJ.push(obj)
            }


            platformCheck.setTreeChkDisabled(id, pos);

        },
        //设置其他不可勾选
        setTreeChkDisabled: function (id, pos) {
            for (var j = 1; j < 29; j++) {
                if (id != 'treeTypeDemo' + j) {
                    var zTree = $.fn.zTree.getZTreeObj('treeTypeDemo' + j);
                    // var nodesArr = zTree.getNodesByParam("pos", pos, null);
                    // for (var n=0;n<nodesArr.length;n++){
                    //     if (!nodesArr[n].checked){
                    //         zTree.setChkDisabled(nodesArr[n],true)
                    //     }
                    // }

                    if (pos == '-1') {
                        var nodesArrNOCheck = zTree.getNodesByParam("checked", false, null);
                        for (var n = 0; n < nodesArrNOCheck.length; n++) {
                            if (!nodesArrNOCheck[n].checked) {
                                zTree.setChkDisabled(nodesArrNOCheck[n], true)
                            }
                        }
                    } else {
                        var nodesArr = zTree.getNodesByParam("pos", pos, null)[0];
                        var allNodeDisabled = true;
                        var parentTId = nodesArr.parentTId;
                        // for (var n=0;n<nodesArr.length;n++){
                        //     if (!nodesArr[n].checked){
                        //         zTree.setChkDisabled(nodesArr[n],true)
                        //     }
                        // }
                        if (!nodesArr.checked) {
                            zTree.setChkDisabled(nodesArr, true)
                        }

                        //设置它的父级不可勾选
                        var parentNode = zTree.getNodeByTId(parentTId);
                        var nodes = zTree.getNodesByFilter(function (node) {
                            return node.parentTId == parentTId
                        });
                        for (var i = 0; i < nodes.length; i++) {
                            if (!nodes[i].chkDisabled) {
                                allNodeDisabled = false;
                            }
                        }
                        if (allNodeDisabled) {
                            zTree.setChkDisabled(parentNode, true)
                        }
                    }

                }
            }
        },
        deleteHasCheckOBJ: function (id, pos) {

            var cancelData = null;
            if (hasCheckOBJ.length) {
                for (var i = 0; i < hasCheckOBJ.length; i++) {
                    if (hasCheckOBJ[i].id == id) {
                        if (pos == '-1') {
                            cancelData = hasCheckOBJ[i];
                            hasCheckOBJ.splice(i, 1);
                        } else if (hasCheckOBJ[i].posArr.indexOf(pos) >= 1) {
                            // hasCheckOBJ[i].posArr.remove(pos)
                            var index = hasCheckOBJ[i].posArr.indexOf(pos);
                            hasCheckOBJ[i].posArr.splice(index, 1);
                        }
                    }
                }
            }

            //解禁
            if (pos == '-1') {
                for (var j = 1; j < 29; j++) {
                    var zTree = $.fn.zTree.getZTreeObj('treeTypeDemo' + j);

                    var nodesArr = [];
                    for (var n = 0; n < cancelData.posArr.length; n++) {
                        var arr = zTree.getNodesByParam("pos", cancelData.posArr[n], null);
                        nodesArr = nodesArr.concat(arr)
                    }

                    for (var n = 0; n < nodesArr.length; n++) {
                        zTree.setChkDisabled(nodesArr[n], false)
                    }

                    //二级树是否解禁
                    var nodesParent = zTree.getNodes()[0].children;
                    for (var i = 0; i < nodesParent.length; i++) {
                        var nodesChildren = nodesParent[i].children;
                        var allDisabled = true;
                        for (var ind = 0; ind < nodesChildren.length; ind++) {
                            if (nodesChildren[ind].chkDisabled == false) {
                                allDisabled = false;
                            }
                        }
                        if (allDisabled == false) {
                            zTree.setChkDisabled(nodesParent[i], false)
                        }
                    }

                    zTree.setChkDisabled(zTree.getNodes()[0], false)
                }
            } else {

                for (var j = 1; j < 29; j++) {
                    var zTree = $.fn.zTree.getZTreeObj('treeTypeDemo' + j);
                    var nodesArr = zTree.getNodesByParam("pos", pos, null);
                    for (var n = 0; n < nodesArr.length; n++) {
                        if (id != 'treeTypeDemo' + j) {//添加判断解决当前组织树父节点取消勾选时子节点解除了禁用的bug
                            zTree.setChkDisabled(nodesArr[n], false);
                        }

                        var nodesParent = zTree.getNodeByTId(nodesArr[n].parentTId);
                        zTree.setChkDisabled(nodesParent, false)
                    }

                    // var nodesParent = zTree.getNodesByParam('TId',parentTId,null);
                    // zTree.setChkDisabled(nodesParent,false)
                }
            }

        },
        judgeHasCheckOBJ: function (id, pos) {
            var hasChecked = false;
            for (var i = 0; i < hasCheckOBJ.length; i++) {
                if (hasCheckOBJ[i].posArr.indexOf(pos) != -1 && id != hasCheckOBJ[i].id) {
                    hasChecked = true;
                }
            }
            return hasChecked;
        },
        alarmTypeTreeChecked: function (isNeedChecked, index) {
            if (isNeedChecked) {
                typeTree[0].children[index].checked = true;
                isCheckedTreeNode = true;
            }
        },
        onCheckType: function (e, treeId, treeNode, id, inputId) {
            var zTree = $.fn.zTree.getZTreeObj(id);
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点

                //将选中的保存到全局对象

                if (treeNode.pos == '-1' || treeNode.pos < -2) {
                    var typezTree = $.fn.zTree.getZTreeObj(id),
                        nodes = typezTree.getCheckedNodes(true);
                    for (var i = 0; i < nodes.length; i++) {
                        if (nodes[i].pos != "" && nodes[i].pos != undefined && nodes[i].pos != null && nodes[i].pos != '-1' && nodes[i].pos > -2) {
                            platformCheck.saveHasCheckOBJ(id, nodes[i].pos);
                        }
                    }

                    // if (treeNode.pos < -2){
                    platformCheck.setTreeChkDisabled(id, treeNode.pos);
                    // }

                } else {
                    platformCheck.saveHasCheckOBJ(id, treeNode.pos);
                }


            } else {

                // if (treeNode.pos == '-1' || treeNode.pos < -2){
                //     var typezTree = $.fn.zTree.getZTreeObj(id),
                //         nodes = typezTree.getCheckedNodes(true);
                //     for (var i=0;i<nodes.length;i++){
                //         if(nodes[i].pos != "" && nodes[i].pos != undefined && nodes[i].pos != null && nodes[i].pos != '-1' && nodes[i].pos > -2){
                //             platformCheck.saveHasCheckOBJ(id,nodes[i].pos);
                //         }
                //     }
                // } else {
                //     platformCheck.saveHasCheckOBJ(id,treeNode.pos);
                // }

                if (treeNode.pos < -2) {

                    for (var j = 1; j < 29; j++) {
                        var zTree = $.fn.zTree.getZTreeObj('treeTypeDemo' + j);
                        var nodesArr = zTree.getNodesByParam("pos", treeNode.pos, null);
                        for (var n = 0; n < nodesArr.length; n++) {
                            zTree.setChkDisabled(nodesArr[n], false)
                        }

                        //设置它的父级不可勾选
                        var allNodeDisabled = true;
                        var parentTId = nodesArr[0].parentTId;
                        var parentNode = zTree.getNodeByTId(parentTId);
                        var nodes = zTree.getNodesByFilter(function (node) {
                            return node.parentTId == parentTId
                        });
                        for (var i = 0; i < nodes.length; i++) {
                            if (!nodes[i].chkDisabled) {
                                allNodeDisabled = false;
                            }
                        }
                        if (allNodeDisabled) {
                            zTree.setChkDisabled(parentNode, true)
                        }
                    }

                    var typezTree = $.fn.zTree.getZTreeObj(id);
                    var nodes = typezTree.getNodesByParam("pos", treeNode.pos, null);
                    var nodesChildren = nodes[0].children;
                    for (var i = 0; i < nodesChildren.length; i++) {
                        platformCheck.deleteHasCheckOBJ(id, nodesChildren[i].pos);
                    }


                } else {
                    //将取消勾选后全局对象删除对应的
                    platformCheck.deleteHasCheckOBJ(id, treeNode.pos)
                }


                // //将取消勾选后全局对象删除对应的
                // platformCheck.deleteHasCheckOBJ(id,treeNode.pos)
            }
            platformCheck.getTypeSelect(zTree, inputId);
            platformCheck.getTypeCheckedNodes(id, inputId);
        },
        getTypeSelect: function (treeObj, inputId) {
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $('#' + inputId).val(allNodes[0].name);
            } else {
                $('#' + inputId).val("");
            }
        },
        getTypeCheckedNodes: function (id, inputId) {
            typePos = [];
            var typezTree = $.fn.zTree.getZTreeObj(id),
                nodes = typezTree.getCheckedNodes(true),
                v = "", typeMsg = "";
            var noCheckLen = typezTree.getCheckedNodes(false).length;
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].pos != "" && nodes[i].pos != undefined && nodes[i].pos != null && nodes[i].pos > -2) {
                    if (i == 0) {
                        v += nodes[i].pos;
                        typeMsg += nodes[i].name;
                    }
                    else {
                        v += "," + nodes[i].pos;
                        typeMsg += "," + nodes[i].name;
                    }
                }
            }
            typePos = v;
            if (noCheckLen != 0) {
                typePos = typePos.replace("-1,", '');
                typeMsg = typeMsg.replace("全部,", '');
            }
            else {
                typePos = '-1';
                typeMsg = '全部'
            }
            $('#' + inputId).val(typeMsg);
        },
        onClickBack: function (e, treeId, treeNode, id, inputId) {
            if (!treeNode.chkDisabled) {
                var zTreeObj = $.fn.zTree.getZTreeObj(id);
                zTreeObj.checkNode(treeNode, !treeNode.checked, true);
                platformCheck.onCheckType(e, treeId, treeNode, id, inputId);
            }

        },
        deleteParamSet: function (id) {
            var url = '/clbs/m/connectionparamsset/delete';//删除地址
            layer.confirm(publicDelete, {btn: ["确定", "取消"], icon: 3, title: "操作确认"}, function () {
                json_ajax("POST", url, "json", true, {"id": id}, function (data) {
                    if (data.success) {
                        var url = "/clbs/m/connectionparamsset/list";
                        json_ajax("POST", url, "json", true, null, platformCheck.getCallback);
                        layer.closeAll();
                    } else {
                        layer.msg(data.msg);
                    }
                });
            });
        },
        // 刷新table列表
        refreshTable: function () {
            //$('#simpleQueryParam').val("");
            //myTable.requestData();
            var url = "/clbs/m/connectionparamsset/list";
            json_ajax("POST", url, "json", true, null, platformCheck.getCallback);
        },
        //批量删除
        deleteMuch: function () {
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChk']:checked").length;
            if (chechedNum == 0) {
                layer.msg(selectItem, {move: false});
                return
            }
            var checkedList = new Array();
            $("input[name='subChk']:checked").each(function () {
                checkedList.push($(this).val());
            });
            var url = "/clbs/m/connectionparamsset/delete";
            var data = {"id": checkedList.toString()};
            layer.confirm(publicDelete, {btn: ["确定", "取消"], icon: 3, title: "操作确认"}, function () {
                json_ajax("POST", url, "json", true, data, function (data) {
                    if (data.success) {
                        var url = "/clbs/m/connectionparamsset/list";
                        json_ajax("POST", url, "json", true, null, platformCheck.getCallback);
                        layer.closeAll();
                    } else {
                        layer.msg(data.msg);
                    }
                });
            });
        },
        getAlarmTreeData: function (id) {
            var typePos = '';
            var typezTree = $.fn.zTree.getZTreeObj(id),
                nodes = typezTree.getCheckedNodes(true),
                v = "";
            var noCheckLen = typezTree.getCheckedNodes(false).length;
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].pos != "" && nodes[i].pos != undefined && nodes[i].pos != null && nodes[i].pos != -1 && nodes[i].pos > -2) {
                    if (v == '') {
                        v += nodes[i].pos;
                    }
                    else {
                        v += "," + nodes[i].pos;
                    }
                }
            }
            typePos = v;

            return typePos;
        },
        validate1078: function () {
            return $("#alarmSetForm").validate({
                ignore: '',
                rules: {
                    alarmTime1: {
                        ifInputRequired: '#groupSelect1',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime2: {
                        ifInputRequired: '#groupSelect2',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime3: {
                        ifInputRequired: '#groupSelect3',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime4: {
                        ifInputRequired: '#groupSelect4',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime5: {
                        ifInputRequired: '#groupSelect5',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime6: {
                        ifInputRequired: '#groupSelect6',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime7: {
                        ifInputRequired: '#groupSelect7',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime8: {
                        ifInputRequired: '#groupSelect8',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime9: {
                        ifInputRequired: '#groupSelect9',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime10: {
                        ifInputRequired: '#groupSelect10',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime11: {
                        ifInputRequired: '#groupSelect11',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime12: {
                        ifInputRequired: '#groupSelect12',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime13: {
                        ifInputRequired: '#groupSelect13',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime14: {
                        ifInputRequired: '#groupSelect14',
                        digits: true,
                        range: [0, 1800]
                    },

                    alarmTime22: {
                        ifInputRequired: '#groupSelect22',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime23: {
                        ifInputRequired: '#groupSelect23',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime24: {
                        ifInputRequired: '#groupSelect24',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime25: {
                        ifInputRequired: '#groupSelect25',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime26: {
                        ifInputRequired: '#groupSelect26',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime27: {
                        ifInputRequired: '#groupSelect27',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime28: {
                        ifInputRequired: '#groupSelect28',
                        digits: true,
                        range: [0, 1800]
                    },
                },
                messages: {
                    alarmTime1: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime2: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime3: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime4: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime5: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime6: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime7: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime8: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime9: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime10: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime11: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime12: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime13: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime14: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },

                    alarmTime22: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime23: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime24: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime25: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime26: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime27: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime28: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                }

            }).form();
        },
        validateXZ: function () {
            return $("#alarmSetForm").validate({
                ignore: '',
                rules: {
                    alarmTime1: {
                        ifInputRequired: '#groupSelect1',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime2: {
                        ifInputRequired: '#groupSelect2',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime3: {
                        ifInputRequired: '#groupSelect3',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime4: {
                        ifInputRequired: '#groupSelect4',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime5: {
                        ifInputRequired: '#groupSelect5',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime6: {
                        ifInputRequired: '#groupSelect6',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime7: {
                        ifInputRequired: '#groupSelect7',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime8: {
                        ifInputRequired: '#groupSelect8',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime9: {
                        ifInputRequired: '#groupSelect9',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime10: {
                        ifInputRequired: '#groupSelect10',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime11: {
                        ifInputRequired: '#groupSelect11',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime12: {
                        ifInputRequired: '#groupSelect12',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime13: {
                        ifInputRequired: '#groupSelect13',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime14: {
                        ifInputRequired: '#groupSelect14',
                        digits: true,
                        range: [0, 1800]
                    },

                    alarmTime15: {
                        ifInputRequired: '#groupSelect15',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime16: {
                        ifInputRequired: '#groupSelect16',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime17: {
                        ifInputRequired: '#groupSelect17',
                        digits: true,
                        range: [0, 1800]
                    },

                },
                messages: {
                    alarmTime1: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime2: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime3: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime4: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime5: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime6: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime7: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime8: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime9: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime10: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime11: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime12: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime13: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime14: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },

                    alarmTime15: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime16: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime17: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                }

            }).form();
        },
        validateSX: function () {
            return $("#alarmSetForm").validate({
                ignore: '',
                rules: {
                    alarmTime1: {
                        ifInputRequired: '#groupSelect1',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime2: {
                        ifInputRequired: '#groupSelect2',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime3: {
                        ifInputRequired: '#groupSelect3',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime4: {
                        ifInputRequired: '#groupSelect4',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime5: {
                        ifInputRequired: '#groupSelect5',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime6: {
                        ifInputRequired: '#groupSelect6',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime7: {
                        ifInputRequired: '#groupSelect7',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime8: {
                        ifInputRequired: '#groupSelect8',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime9: {
                        ifInputRequired: '#groupSelect9',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime10: {
                        ifInputRequired: '#groupSelect10',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime11: {
                        ifInputRequired: '#groupSelect11',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime12: {
                        ifInputRequired: '#groupSelect12',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime13: {
                        ifInputRequired: '#groupSelect13',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime14: {
                        ifInputRequired: '#groupSelect14',
                        digits: true,
                        range: [0, 1800]
                    },

                    alarmTime11: {
                        ifInputRequired: '#groupSelect11',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime12: {
                        ifInputRequired: '#groupSelect12',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime13: {
                        ifInputRequired: '#groupSelect13',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime14: {
                        ifInputRequired: '#groupSelect14',
                        digits: true,
                        range: [0, 1800]
                    },

                    alarmTime18: {
                        ifInputRequired: '#groupSelect18',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime19: {
                        ifInputRequired: '#groupSelect19',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime20: {
                        ifInputRequired: '#groupSelect20',
                        digits: true,
                        range: [0, 1800]
                    },

                    alarmTime21: {
                        ifInputRequired: '#groupSelect21',
                        digits: true,
                        range: [0, 1800]
                    },

                },
                messages: {
                    alarmTime1: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime2: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime3: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime4: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime5: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime6: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime7: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime1: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime8: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime9: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime10: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime11: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime12: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime13: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime14: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },

                    alarmTime18: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime19: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime20: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime21: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                }

            }).form();
        },
        validateJTT: function () {
            return $("#alarmSetForm").validate({
                ignore: '',
                rules: {
                    alarmTime1: {
                        ifInputRequired: '#groupSelect1',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime2: {
                        ifInputRequired: '#groupSelect2',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime3: {
                        ifInputRequired: '#groupSelect3',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime4: {
                        ifInputRequired: '#groupSelect4',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime5: {
                        ifInputRequired: '#groupSelect5',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime6: {
                        ifInputRequired: '#groupSelect6',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime7: {
                        ifInputRequired: '#groupSelect7',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime8: {
                        ifInputRequired: '#groupSelect8',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime9: {
                        ifInputRequired: '#groupSelect9',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime10: {
                        ifInputRequired: '#groupSelect10',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime11: {
                        ifInputRequired: '#groupSelect11',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime12: {
                        ifInputRequired: '#groupSelect12',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime13: {
                        ifInputRequired: '#groupSelect13',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime14: {
                        ifInputRequired: '#groupSelect14',
                        digits: true,
                        range: [0, 1800]
                    },

                    alarmTime11: {
                        ifInputRequired: '#groupSelect11',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime12: {
                        ifInputRequired: '#groupSelect12',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime13: {
                        ifInputRequired: '#groupSelect13',
                        digits: true,
                        range: [0, 1800]
                    },
                    alarmTime14: {
                        ifInputRequired: '#groupSelect14',
                        digits: true,
                        range: [0, 1800]
                    },
                },
                messages: {
                    alarmTime1: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime2: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime3: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime4: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime5: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime6: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime7: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime1: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime8: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime9: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime10: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime11: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime12: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime13: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                    alarmTime14: {
                        digits: "请输入0-1800之间的整数",
                        range: "请输入0-1800之间的整数",
                    },
                }

            }).form();
        },
        doSubmit: function () {
            // 15-17 西藏报警  18-21山西报警 22-28 1078报警
            var alarmData = [];
            if (alarmTypeName == '1078-809') {

                if (!platformCheck.validate1078()) {
                    return;
                }

                for (var i = 1; i < 29; i++) {
                    if (i >= 15 && i <= 21) {
                        continue;
                    }
                    var data = {
                        'pos808': platformCheck.getAlarmTreeData('treeTypeDemo' + i),
                        'pos809': $('#groupSelect' + i).attr('pos809'),
                        'time': $('#alarmTime' + i).val()
                    }
                    alarmData.push(data)
                }

            }

            if (alarmTypeName == "山西-809" || alarmTypeName == "四川-809") {

                if (!platformCheck.validateSX()) {
                    return;
                }

                for (var i = 1; i < 22; i++) {
                    if (i >= 15 && i <= 17) {
                        continue;
                    }

                    var pos809;
                    // 山西西藏都有一个0024
                    if (i == 21) {
                        pos809 = $('#groupSelect' + i).attr('pos809').split('_')[0]
                    } else {
                        pos809 = $('#groupSelect' + i).attr('pos809');
                    }

                    var data = {
                        'pos808': platformCheck.getAlarmTreeData('treeTypeDemo' + i),
                        'pos809': pos809,
                        'time': $('#alarmTime' + i).val()
                    }
                    alarmData.push(data)
                }
            }

            if (alarmTypeName == "西藏-809") {

                if (!platformCheck.validateXZ()) {
                    return;
                }

                for (var i = 1; i < 18; i++) {


                    var pos809;
                    // 山西西藏都有一个0024
                    if (i == 15) {
                        pos809 = $('#groupSelect' + i).attr('pos809').split('_')[0]
                    } else {
                        pos809 = $('#groupSelect' + i).attr('pos809');
                    }

                    var data = {
                        'pos808': platformCheck.getAlarmTreeData('treeTypeDemo' + i),
                        'pos809': pos809,
                        'time': $('#alarmTime' + i).val()
                    }
                    alarmData.push(data)
                }
            }

            if (alarmTypeName == "JTT-809" || alarmTypeName == "796-809" || alarmTypeName == "黑龙江-809") {

                if (!platformCheck.validateJTT()) {
                    return;
                }

                for (var i = 1; i < 15; i++) {

                    pos809 = $('#groupSelect' + i).attr('pos809');
                    var data = {
                        'pos808': platformCheck.getAlarmTreeData('treeTypeDemo' + i),
                        'pos809': pos809,
                        'time': $('#alarmTime' + i).val()
                    }
                    alarmData.push(data)
                }
            }

            var param = {
                settingId: alarmSettingId,
                protocolType: alarmProtocolType,
                alarmJson: JSON.stringify(alarmData)
            }

            json_ajax('post', '/clbs/m/connectionparamsset/setAlarm', 'json', true, param, function (data) {
                if (data.success) {
                    layer.msg('设置成功');
                    $('#alarmSetmodal').modal('hide');
                } else {
                    layer.msg(data.msg);
                }
            })


        }
    }
    $(function () {

        $("[data-toggle='tooltip']").tooltip();

        $('input').inputClear();
        platformCheck.showMenuText();
        var permission = $("#permission").val();
        if (permission == "true") {
            $('#timeInterval').dateRangePicker();
        }
        platformCheck.init("#dataTable");
        $("#del_model").on("click", platformCheck.deleteMuch);
        // $("#refreshTable").on("click", platformCheck.refreshTable);
        $("#refreshTable").on('click', function () {
            // $("#simpleQueryParam").val("");
            platformCheck.refreshTable();
        });

        $(".groupSelectClass").bind("click", showMenuContent);

        $("#doSubmit").on("click", platformCheck.doSubmit)
    })
    $("#checkAll").click(function () {
        $("input[name='subChk']").prop("checked", this.checked);
    });
    $('#alarmSetmodal').on('hidden.bs.modal', function (e) {
        $('label.error').hide();
    })
}($, window))