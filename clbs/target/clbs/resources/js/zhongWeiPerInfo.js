//# sourceURL=zhongWeiPerInfo.js
(function ($, window) {
    var isRead = true;
    var _timeout;
    var vid = $("#vehicleId").val();
    var sensorIDArr = [225, 226, 227, 228, 231];//[前向监测, 驾驶员行为监测, 胎压监测, 盲区监测, 路网]
    var socketTypeArr = [62177, 62178, 62179, 62180, 62183];//[前向监测, 驾驶员行为监测, 胎压监测, 盲区监测, 路网]

    zhongWeiPerInfo = {
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

            for (var i = 0; i < socketTypeArr.length; i++) {
                var type = socketTypeArr[i];
                var sensorID = sensorIDArr[i];

                zhongWeiPerInfo.subscribeParam(type);//socket订阅
                zhongWeiPerInfo.getDatas(sensorID);//下发指令
            }
        },
        //websocket订阅
        subscribeParam: function (type) {
            /* webSocket.subscribe(headers, "/topic/per" + type + '_' + vid + "_Info", function (data) {
                 zhongWeiPerInfo.getSensor0104Param(data, type);
             }, null, null);*/
            webSocket.subscribe(headers, "/user/topic/per" + type + "Info", function (data) {
                zhongWeiPerInfo.getSensor0104Param(data, type);
            }, null, null);
        },
        getSensor0104Param: function (data, type) {
            if (data == null) {
                return;
            }
            clearTimeout(_timeout);
            isRead = false;
            layer.closeAll();
            $("#readPerStateRefresh").html("刷新").prop('disabled', false);

            var result = $.parseJSON(data.body);
            console.log('外设基本信息', result);
            zhongWeiPerInfo.queryF3BaseParamCall(result, type);
        },
        //input赋值
        queryF3BaseParamCall: function (result, type) {
            var data = result.data.msgBody.params[0].value;
            if (data) {
                for (var key in data) {
                    var value = data[key];
                    $('#' + type + '-' + key).val(value);
                }
            }
        },
        //下发指令给终端
        getDatas: function (sensorID) {
            json_ajax("POST", '/clbs/r/riskManagement/DefineSettings/getPeripheralInfo', "json", false, {
                "vid": vid,
                "sensorID": sensorID,
                "commandType": 242,
            }, zhongWeiPerInfo.getF3BaseParamCall);
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
                zhongWeiPerInfo.createSocket0104InfoMonitor();
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
            for (var i = 0; i < sensorIDArr.length; i++) {
                var sensorid = sensorIDArr[i];
                zhongWeiPerInfo.getDatas(sensorid);
            }
        },
    }

    $(function () {
        zhongWeiPerInfo.init();
        //刷新
        $("#readPerStateRefresh").on("click", zhongWeiPerInfo.readInformationRefreshClick);
    })
})($, window)