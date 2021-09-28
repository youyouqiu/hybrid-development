(function (window, $) {
    baseValueUpdate = {
        init: function () {
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
        },
        validates: function () {
            return $("#baseValue").validate({
                rules: {
                    totalWorkBaseValue: {
                        required: true,
                        digits: true,
                        range: [0, 600000]
                    },
                    totalAwaitBaseValue: {
                        required: true,
                        digits: true,
                        range: [0, 600000]
                    },
                    totalHaltBaseValue: {
                        required: true,
                        digits: true,
                        range: [0, 600000]
                    },
                    totalWorkSecond: {
                        required: true,
                        digits: true,
                        range: [0, 59]
                    },
                    totalAwaitSecond: {
                        required: true,
                        digits: true,
                        range: [0, 59]
                    },
                    totalHaltSecond: {
                        required: true,
                        digits: true,
                        range: [0, 59]
                    }
                },
                messages: {
                    totalWorkBaseValue: {
                        required: hourNull,
                        digits: hourError,
                        range: hourError
                    },
                    totalAwaitBaseValue: {
                        required: hourNull,
                        digits: hourError,
                        range: hourError
                    },
                    totalHaltBaseValue: {
                        required: hourNull,
                        digits: hourError,
                        range: hourError
                    },
                    totalWorkSecond: {
                        required: secondNull,
                        digits: secondError,
                        range: secondError
                    },
                    totalAwaitSecond: {
                        required: secondNull,
                        digits: secondError,
                        range: secondError
                    },
                    totalHaltSecond: {
                        required: secondNull,
                        digits: secondError,
                        range: secondError
                    }
                }
            }).form();
        },
        doSubmits: function () {
            if (baseValueUpdate.validates()) {
                layer.open({
                    closeBtn: false,
                    offset:'t',
                    title: '提示',
                    content: '当前操作影响重大，请再次确认操作',
                    btn:['确定','返回'],
                    btn1:function(index,layero){
                        var url = "/clbs/v/workhourmgt/workhoursetting/updateWorkHourBaseValue";
                        var parameter = {
                            "id": $("#bid").val(),
                            "vehicleId": $("#vehicleId").val(),
                            "sensorId": $("#sensorId").val(),
                            "totalWorkBaseValue": parseInt($("#totalWorkBaseValue").val() * 60) + parseInt($("#totalWorkSecond").val()),
                            "totalAwaitBaseValue": parseInt($("#totalAwaitBaseValue").val() * 60) + parseInt($("#totalAwaitSecond").val()),
                            "totalHaltBaseValue": parseInt($("#totalHaltBaseValue").val() * 60) + parseInt($("#totalHaltSecond").val())
                        };
                        json_ajax("POST", url, "json", true, parameter, baseValueUpdate.doSubmitCallback);
                        layer.close(index);
                    }
                });
            }
        },
        doSubmitCallback: function (data) {
            var json = data;
            if (json.success) {
                $("#sendStatus").val("参数已下发");
                baseValueUpdate.createSocketStatusMonitor(json.msg);
            } else {
                if (json.msg != null) {
                    layer.msg(json.msg);
                }
            }
        },
        //创建消息监听
        createSocketStatusMonitor: function (msg) {
            var msg = $.parseJSON(msg);
            var requestStrS = {
                "desc": {
                    "cmsgSN": msg.msgId,
                    "UserName": msg.userName
                },
                "data": []
            };
            headers = {"UserName": msg.userName};
            webSocket.subscribe(headers, "/user/topic/t808_currency_response", baseValueUpdate.currencyResponse, "/app/vehicle/oil/setting", requestStrS);
            webSocket.subscribe(headers, "/user/topic/deviceReportLog", baseValueUpdate.deviceReportLog, "/app/vehicle/oil/setting", requestStrS);
        },
        //处理获取设备下发的结果
        currencyResponse: function (msg) {
            if (msg == null)
                return;
            var result = $.parseJSON(msg.body);
            var msgid = result.data.msgBody.msgSNACK;
            var status = result.data.msgBody.result;
            if (status == 0) {
                $("#sendStatus").val("终端处理中");
            }
            if (status == 1 || status == 2 || status == 3) {
                layer.closeAll();
                $("#sendStatus").val("参数下发失败");
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
                    $("#sendStatus").val("参数生效");
                } else {
                    $("#sendStatus").val("参数未生效");
                }
                return;
            }
            myTable.refresh();
            return;
        }
    };
    $(function () {
        baseValueUpdate.init();
        $('input').inputClear();
        $("#paramDataSend").bind("click", baseValueUpdate.doSubmits);
    })
})(window, $)