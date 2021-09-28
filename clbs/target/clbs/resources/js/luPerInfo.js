//# sourceURL=definesettingedit.js
var luPerInfo;

(function ($, window) {
    var isRead = true;
    var _timeout;
    var vid = $("#vehicleId").val();
    var socketTypeArr = ['F864', 'F865', 'F866', 'F867']; //[高级驾驶辅助, 驾驶员状态监测, 胎压监测, 盲区监测]
    var sensorIDArr = [100, 101, 102, 103]; //[高级驾驶辅助, 驾驶员状态监测, 胎压监测, 盲区监测]

    luPerInfo = {
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

            for (var i=0; i<socketTypeArr.length; i++) {
                var type = socketTypeArr[i],
                    sensorId = sensorIDArr[i];

                luPerInfo.subscribeParam(type); //socket订阅
                luPerInfo.getDatas(sensorId); //下发指令
            }
        },
        //websocket订阅
        subscribeParam: function (type) {
            webSocket.subscribe(headers, "/topic/per" + type + '_' + vid + "_Info", function (data) {
                luPerInfo.updateDom(data, type);
            }, null, null);
        },
        updateDom: function (data, type) {
            if (!data) return;
            var result = $.parseJSON(data.body);
            // console.log('外设基本信息', result);

            clearTimeout(_timeout);
            isRead = false;
            layer.closeAll();
            $("#readPerStateRefresh").html("刷新").prop('disabled', false);
            luPerInfo.updateInput(result, type);
        },
        //input赋值
        updateInput: function (result, type) {
            var data = result.data.msgBody.sensorF8;
            var justDoIt = function(idPrefix){
                for (var key in data) {
                    var value = data[key];
                    $('#' + idPrefix + '-' + key).val(value);
                }
            }
            switch (type) {
                case 'F864':
                    justDoIt('driverAssistant')
                    break;
                case 'F865':
                    justDoIt('driverStatus')
                    break;
                case 'F866':
                    justDoIt('tirePressure')
                    break;
                case 'F867':
                    justDoIt('blindSpot')
                    break;
            }
        },
        //下发指令给终端
        getDatas: function (sensorID) {
            json_ajax("POST", '/clbs/adas/standard/param/getJiPeripheralInfo', "json", false, {
                "vehicleId": vid,
                "sensorID": sensorID,
                "commandType": 248,
            }, luPerInfo.getF3BaseParamCall);
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
                luPerInfo.createSocket0104InfoMonitor();
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

            for (var i=0; i<sensorIDArr.length; i++) {
                var sensorId = sensorIDArr[i];
                luPerInfo.getDatas(sensorId); //下发指令
            }
        },
    }

    $(function () {
        luPerInfo.init();
        //刷新
        $("#readPerStateRefresh").on("click", luPerInfo.readInformationRefreshClick);
    })
}($, window))