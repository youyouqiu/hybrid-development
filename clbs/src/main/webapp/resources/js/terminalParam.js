//# sourceURL=terminalParam.js
var terminalParam;

(function ($, window) {
    var isRead = true;
    var _timeout;
    var vid = $("#vehicleId").val();
    terminalParam = {
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
            terminalParam.subscribeParam();//socket订阅
            terminalParam.getDatas();//下发指令
        },
        //websocket订阅
        subscribeParam: function (type) {
            webSocket.subscribe(headers, "/user/topic/terminal_" + vid + "_Info", terminalParam.getSensor0104Param, null, null);
        },
        getSensor0104Param: function (data) {
            if (data == null) {return;}
            clearTimeout(_timeout);
            isRead = false;
            layer.closeAll();
            $("#readPerStateRefresh").html("刷新").prop('disabled', false);

            var result = $.parseJSON(data.body);
            terminalParam.queryF3BaseParamCall(result);
        },
        //input赋值
        queryF3BaseParamCall: function (result) {
            var data = result.data.msgBody.params;
            for (var key in data) {
                var value = data[key].value;
                console.log(value);
                if(value['macAddress'] != undefined){
                    $("#macAddress").val(value.macAddress);
                }

                if(value['manufacturerId'] != undefined){
                    $("#manufacturerId").val(value.manufacturerId);
                }

                if(value['deviceModelNumber'] != undefined){
                    $("#deviceModelNumber").val(value.deviceModelNumber);
                }

            }
        },
        //下发指令给终端
        getDatas: function (sensorID) {
            json_ajax("POST", '/clbs/adas/standard/param/getJingPeripheralInfo', "json", false, {
                "vid": vid,
                "paramType": 53
            }, terminalParam.getF3BaseParamCall);
        },
        getF3BaseParamCall: function (data) {
            if (!data.success) {
                if (data.msg) {
                    layer.msg(data.msg);
                }
            } else {
                if (isRead) {
                    $("#readPerStateRefresh").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled', true);
                }
                terminalParam.createSocket0104InfoMonitor();
            }
        },
        createSocket0104InfoMonitor: function () {
            clearTimeout(_timeout);
            _timeout = setTimeout(function () {
                if (isRead) {
                    isRead = false;
                    layer.closeAll();
                    $("#readPerStateRefresh").html("刷新").prop('disabled', false);
                    layer.msg("获取设备数据失败!");
                }
            }, 60000);
        },
        //基本信息-刷新
        readInformationRefreshClick: function () {
            isRead = true;
            terminalParam.getDatas();
        },
    }

    $(function () {
        terminalParam.init();
        //刷新
        $("#readPerStateRefresh").on("click", terminalParam.readInformationRefreshClick);
    })
})($, window);