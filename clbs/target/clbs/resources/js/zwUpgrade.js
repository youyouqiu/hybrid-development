//# sourceURL=zwUpgrade.js
(function ($, window) {
    var vid = $("#readConventionalId").val();
    var sensorID = $("#poilSensorType").val();
    var sensorType = 0x41;
    var commandType = 13141;
    var setting_id = $("#setting_id").val();
    //最后一次获取信息想MSGID
    var temp_send_vehicle_msg_id = "";
    var onLineStatus = false; // 标识车辆是否在线
    zwUpgrade = {
        init: function () {
            temp_send_vehicle_msg_id = "";
            // 请求后台，获取所有订阅的车
            // webSocket.init('/clbs/vehicle');
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
            if (sensorID == "2") {
                sensorType = 0x42;
                commandType = 13142;
            }
            $("#UUpgradeSendStatus").val("");
            var data_id = $("#UUpgradeSendStatus").attr("data-id");
            $("#UUpgradeSendStatus").removeClass(data_id);
            $("#UUpgradeSendStatus").removeAttr("data-id");
            zwUpgrade.checkVehicleOnlineStatus();

            zwUpgrade.createSocket0900StatusMonitor();
        }// 判断车辆是否在线
        , checkVehicleOnlineStatus: function () {
            var url = "/clbs/v/oilmassmgt/oilcalibration/checkVehicleOnlineStatus";
            var data = {"vehicleId": vid};
            json_ajax("POST", url, "json", false, data, zwUpgrade.checkVehicleOnlineStatusCallBack);
        },
        // 判断车辆是否在线回调
        checkVehicleOnlineStatusCallBack: function (data) {
            if (data.success) {
                onLineStatus = true; // 在线
                return true;
            } else if (!data.success && data.msg == null) {
                onLineStatus = false; // 不在线
                layer.msg("终端离线");
                return false;
            } else if (!data.success && data.msg != null) {
                layer.msg(data.msg, {move: false});
            }
        },
        //远程升级
        sensorRemoteUpgradeClick: function (vid, sensorID, brand, type) {
            var sensorType = 0x41;
            var commandType = 13141;
            if (sensorID == "2") {
                sensorType = 0x42;
                commandType = 13142;
            }
            $("#remoteUpgradecommandType").val(commandType);
            $("#remoteUpgradeVehicleid").val(vid);
            $("#remoteUpgradeBrand").val(brand);
            $("#remoteUpgradetype ").val(type);
            $("#remoteUpgradeSensorType").val(sensorType);
            $("#UUpgradeSendStatus").val("");
            var data_id = $("#UUpgradeSendStatus").attr("data-id");
            $("#UUpgradeSendStatus").removeClass(data_id);
            $("#UUpgradeSendStatus").removeAttr("data-id");
            var url = '/clbs/v/oilmassmgt/oilvehiclesetting/getF3Param';
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "sensorID": sensorType,
                "commandType": 0xF8
            }, zwUpgrade.sensorRemoteUpgradeCall);
            var url = "/clbs/v/monitoring/command/getCommandParam";
            var parameter = {"vid": vid, "commandType": commandType, "isRefer": true};
            json_ajax("POST", url, "json", true, parameter, zwUpgrade.getSensorRemoteUpgradeCall);
        },
        //远程升级-升级指令
        getSensorRemoteUpgradeCall: function (data) {
            if (data.success == true) {
                if (data.msg == null && data.obj.wirelessUpdateParam != null) {
                    var wirelessUpdateParam = data.obj.wirelessUpdateParam;
                    for (key in wirelessUpdateParam) {
                        $('#' + key).val(wirelessUpdateParam[key]);
                    }
                }
            } else {
                layer.msg(data.msg, {move: false});
            }
        },
        validates: function () {
            return $('#saveDefineUpgradeForm').validate({
                rules: {
                    controlType: {
                        required: true
                    },
                    restoreType: {
                        required: true
                    },
                    wDailUserName: {
                        required: true
                    },
                    wDailPwd: {
                        required: true
                    },
                    wAddress: {
                        required: true
                    },
                    wTcpPort: {
                        required: true,
                        integerRange: [1, 65535]
                    },
                    firmwareVersion: {
                        required: true,
                    },
                },
                messages: {
                    controlType: {
                        required: '请选择控制类型'
                    },
                    restoreType: {
                        required: '请选择升级/恢复类型'
                    },
                    wDailUserName: {
                        required: '请输入拨号用户名称'
                    },
                    wDailPwd: {
                        required: '请输入拨号密码'
                    },
                    wAddress: {
                        required: '请输入服务器地址IP或域名'
                    },
                    wTcpPort: {
                        required: '请输入端口',
                        integerRange: '输入范围1-65535'
                    },
                    firmwareVersion: {
                        required: '请输入固件版本',
                    },
                }
            }).form();
        },
        //远程升级-升级指令-下发
        sendRemoteUpgrade: function () {
            if (!onLineStatus) {
                layer.msg("终端离线，暂不支持远程升级");
                return;
            }
            if (zwUpgrade.validates()) {
                $("#UUpgradeSendStatus").val("参数下发中");//下发状态
                $("#saveDefineUpgradeForm").ajaxSubmit(function (data) {
                    data = $.parseJSON(data);
                    if (data.success) {
                        var msgid = $.parseJSON(data.msg).msgId;
                        var sendstatusname = "send_status_" + msgid;
                        $("#UUpgradeSendStatus").attr("data-id", sendstatusname);
                        $("#UUpgradeSendStatus").addClass(sendstatusname);
                        setTimeout(function () {
                            layer.load(2);
                        }, 0);
                    } else {
                        layer.msg(data.msg, {move: false});
                    }
                });
            }
        }, //创建消息监听
        createSocket0900StatusMonitor: function () {
            var userName = $("#userName").text();
            var requestStrS = {
                "desc": {
                    "cmsgSN": '',
                    "UserName": userName
                },
                "data": []
            };
            headers = {"UserName": userName};
            webSocket.subscribe(headers, "/user/topic/t808_currency_response", zwUpgrade.currencyResponse, "/app/vehicle/oil/setting", requestStrS);
            webSocket.subscribe(headers, "/user/topic/remoteUpgrade", zwUpgrade.deviceReportLog, "/app/vehicle/oil/setting", requestStrS);
        },
        //远程升级-判断设备是否在线
        sensorRemoteUpgradeCall: function (data) {
            if (!data.success) {
                layer.msg(data.msg, {move: false});
                return;
            }
        },
        //处理获取设备下发的结果
        currencyResponse: function (msg) {
            if (msg == null)
                return;
            var result = $.parseJSON(msg.body);
            var msgid = result.data.msgBody.msgSNACK;
            var status = result.data.msgBody.result;
            if (status == 0) {
                $(".send_status_" + msgid).val("终端处理中");
            }
            if (status == 1 || status == 2 || status == 3) {
                layer.closeAll();
                $(".send_status_" + msgid).val("参数下发失败");
            }
            return;
        },
        getStatus: function (status) {
            switch (status) {
                case 0:
                    return '成功';
                case 1:
                    return '失败';
                case 2:
                    return '取消';
                case 10:
                    return '未找到目标设备';
                case 11:
                    return '硬件型号不支持';
                case 12:
                    return '软件版本相同';
                case 13:
                    return '软件版本不支持';
                default:
                    return '失败';
            }
        },
        //处理获取设备下发的结果
        deviceReportLog: function (msg) {
            if (msg == null) return;
            var result = $.parseJSON(msg.body);
            console.log('result', result);
            layer.closeAll();
            var status = result.data.msgBody.result;
            var statusName = zwUpgrade.getStatus(status);
            $('#UUpgradeSendStatus').val(statusName);
            myTable.refresh();
        },
    };
    $(function () {
        $('input').inputClear();
        zwUpgrade.init();
    })
})($, window)