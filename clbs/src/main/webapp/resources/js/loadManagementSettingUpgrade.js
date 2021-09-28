(function ($, window) {
    var vid = $("#readConventionalId").val();
    var sensorID = $("#poilSensorType").val();
    var sensorType = 0x80;
    var commandType = 128;
    //最后一次获取信息想MSGID
    var temp_send_vehicle_msg_id = "";
    var onLineStatus = false; // 标识车辆是否在线
    upgrade = {
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
            if (sensorID == "1") {
                sensorType = 0x81;
                commandType = 129;
            }
            $("#UUpgradeSendStatus").val("");
            var data_id = $("#UUpgradeSendStatus").attr("data-id");
            $("#UUpgradeSendStatus").removeClass(data_id);
            $("#UUpgradeSendStatus").removeAttr("data-id");
            upgrade.checkVehicleOnlineStatus();
        }// 判断车辆是否在线
        , checkVehicleOnlineStatus: function () {
            var url = "/clbs/v/oilmassmgt/oilcalibration/checkVehicleOnlineStatus";
            var data = {"vehicleId": vid};
            json_ajax("POST", url, "json", false, data, upgrade.checkVehicleOnlineStatusCallBack);
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
            var sensorType = 0x80;
            var commandType = 128;
            if (sensorSequence == "1") {
                sensorType = 0x81;
                commandType = 129;
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
            var url = '/clbs/v/workhourmgt/workhoursetting/getF3Param';
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "sensorID": sensorType,
                "commandType": 0xF8
            }, upgrade.sensorRemoteUpgradeCall);
            var url = "/clbs/v/monitoring/command/getCommandParam";
            var parameter = {"vid": vid, "commandType": commandType, "isRefer": true};
            json_ajax("POST", url, "json", true, parameter, upgrade.getSensorRemoteUpgradeCall);
        },
        //远程升级-升级指令
        getSensorRemoteUpgradeCall: function (data) {
            if (data.success == true) {
                if (data.msg == null && data.obj.wirelessUpdateParam != null) {
                    var wirelessUpdateParam = data.obj.wirelessUpdateParam;
                    $("#UUpgradeDialName").val(wirelessUpdateParam.wDailUserName);
                    $("#UUpgradeDialPwd").val(wirelessUpdateParam.wDailPwd);
                    $("#UUpgradeAddress").val(wirelessUpdateParam.wAddress);
                    $("#UUpgradeTcpTort").val(wirelessUpdateParam.wTcpPort);
                    $("#UUpgradeFirmware").val(wirelessUpdateParam.firmwareVersion);
                }
            } else {
                layer.msg(data.msg, {move: false});
            }
        },
        //远程升级-升级指令-下发
        sendRemoteUpgrade: function () {
            if (!onLineStatus) {
                layer.msg("终端离线，暂不支持远程升级");
                return;
            }
            var UUpgradeDialName = $("#UUpgradeDialName").val();
            console.log(UUpgradeDialName);
            if (UUpgradeDialName == null || UUpgradeDialName == "") {
                layer.msg("请输入拨号用户名");
                return;
            }
            var UUpgradeDialPwd = $("#UUpgradeDialPwd").val();
            if (UUpgradeDialPwd == null || UUpgradeDialPwd == "") {
                layer.msg("请输入拨号密码");
                return;
            }
            var UUpgradeAddress = $("#UUpgradeAddress").val();
            if (UUpgradeAddress == null || UUpgradeAddress == "") {
                layer.msg("请输入服务器地址IP或域名");
                return;
            }
            var UUpgradeTcpTort = $("#UUpgradeTcpTort").val();
            var UUpgradeUdpTort = $("#UUpgradeUdpTort").val();
            if ((UUpgradeUdpTort == null || UUpgradeUdpTort == "") && (UUpgradeTcpTort == null || UUpgradeTcpTort == "")) {
                layer.msg("请输入UDP端口或TCP端口");
                return;
            }
            var UUpgradeFirmware = $("#UUpgradeFirmware").val();
            if (UUpgradeFirmware == null || UUpgradeFirmware == "") {
                layer.msg("请输入固件版本");
                return;
            }
            $("#wirelessUpdate").ajaxSubmit(function (data) {
                data = $.parseJSON(data);
                if (data.success) {
                    var msgid = $.parseJSON(data.msg).msgId;
                    var sendstatusname = "send_status_" + msgid;
                    $("#UUpgradeSendStatus").attr("data-id", sendstatusname);
                    $("#UUpgradeSendStatus").addClass(sendstatusname);
                    $("#UUpgradeSendStatus").val("参数下发中");//下发状态
                    upgrade.createSocket0900StatusMonitor(data.msg);
                    setTimeout(function () {
                        layer.load(2);
                    }, 0);
                } else {
                    layer.msg(data.msg, {move: false});
                }
            });
        }, //创建消息监听
        createSocket0900StatusMonitor: function (msg) {
            var msg = $.parseJSON(msg);
            var requestStrS = {
                "desc": {
                    "cmsgSN": msg.msgId,
                    "UserName": msg.userName
                },
                "data": []
            };
            headers = {"UserName": msg.userName};
            webSocket.subscribe(headers, "/user/topic/t808_currency_response", upgrade.currencyResponse, "/app/vehicle/oil/setting", requestStrS);
            webSocket.subscribe(headers, "/user/topic/deviceReportLog", upgrade.deviceReportLog, "/app/vehicle/oil/setting", requestStrS);
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
        //处理获取设备下发的结果
        deviceReportLog: function (msg) {
            if (msg == null)
                return;
            var result = $.parseJSON(msg.body);
            var type = result.data.msgBody.type;
            if (type == 243) {
                layer.closeAll();
                var msgid = result.data.msgBody.ackMSN;
                var result = result.data.msgBody.result;
                if (result == 0 || result == "0") {
                    $(".send_status_" + msgid).val("参数生效");
                } else {
                    $(".send_status_" + msgid).val("参数未生效");
                }
                return;
            }
            $("#private_parameter_result_str").val(result.data_result);
            myTable.refresh();
            return;
        },
    }
    $(function () {
        $('input').inputClear();
        upgrade.init();
    })
})($, window)