//# sourceURL=definesettingedit.js
var guiPerInfo;

(function ($, window) {
    var isRead = true;
    var _timeout;
    var vid = $("#vehicleId").val();

    guiPerInfo = {
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

            //socket订阅
            guiPerInfo.subscribeParam('F864');//前向监测
            guiPerInfo.subscribeParam('F865');//驾驶员行为监测
            guiPerInfo.subscribeParam('F866');//胎压检测系统
            guiPerInfo.subscribeParam('F867');//盲区监测系统


            //下发指令
            guiPerInfo.getDatas(100);//驾驶辅助设备
            guiPerInfo.getDatas(101);//驾驶员行为监测设备
            guiPerInfo.getDatas(102);//胎压监测系统
            guiPerInfo.getDatas(103);//盲区监测系统

        },
        //websocket订阅
        subscribeParam: function (type) {
            webSocket.subscribe(headers, "/topic/per" + type + '_' + vid + "_Info", function (data) {
                guiPerInfo.getSensor0104Param(data, type);
            }, null, null);
        },
        getSensor0104Param: function (data, type) {
            if (data == null) {
                return;
            }
            var result = $.parseJSON(data.body);
            // console.log('外设基本信息', result);

            clearTimeout(_timeout);
            isRead = false;
            layer.closeAll();
            $("#readPerStateRefresh").html("刷新").prop('disabled', false);
            guiPerInfo.queryF3BaseParamCall(result, type);
        },
        //input赋值
        queryF3BaseParamCall: function (result, type) {
            var data = result.data.msgBody.sensorF8;

            // if (type == 'F864') {//前向监测
            //     for (var key in data) {
            //         var value = data[key];
            //         $('#adas-' + key).val(value);
            //     }
            // } else {//驾驶员行为监测
            //     for (var key in data) {
            //         var value = data[key];
            //         $('#dsm-' + key).val(value);
            //     }
            // }
            if(type == 'F864') {
                for(var key in data) {
                    var value = data[key];
                    $("#adas-" + key).val(value);
                }
            }else if (type == 'F865'){
                for(var key in data) {
                    var value = data[key];
                    $("#dsm-" + key).val(value);
                }
            }else if (type == 'F867') {
                for(var key in data) {
                    var value = data[key];
                    $("#Blind-" + key).val(value);
                }
            }else {
                for(var key in data) {
                    var value = data[key];
                    $("#Tire-" + key).val(value);
                }
            }
        },
        //下发指令给终端
        getDatas: function (sensorID) {
            json_ajax("POST", '/clbs/adas/standard/param/getJiPeripheralInfo', "json", false, {
                "vehicleId": vid,
                "sensorID": sensorID,
                "commandType": 248,
            }, guiPerInfo.getF3BaseParamCall);
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
                guiPerInfo.createSocket0104InfoMonitor();
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
            guiPerInfo.getDatas(100);
            guiPerInfo.getDatas(101);
            guiPerInfo.getDatas(102);
            guiPerInfo.getDatas(103);
        },
    }

    $(function () {
        guiPerInfo.init();
        //刷新
        $("#readPerStateRefresh").on("click", guiPerInfo.readInformationRefreshClick);
    })
})($, window)