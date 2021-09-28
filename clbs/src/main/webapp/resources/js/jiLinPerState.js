//# sourceURL=jiLinPerState.js
var jiLinPerState;

(function ($, window) {
    var isRead = true;
    var _timeout;
    var vid = $("#vehicleId").val();

    //socket订阅、下发指令
    var socketTypeArr = ['F764', 'F765']; //[前向监测, 驾驶员行为监测]
    var sensorIDArr = [100, 101]; //[前向监测, 驾驶员行为监测]

    jiLinPerState = {
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
            };

            for (var i=0; i<socketTypeArr.length; i++){
                var type = socketTypeArr[i];
                var sensorId = sensorIDArr[i];
                //socket订阅
                jiLinPerState.subscribeParam(type);
                // //下发指令
                jiLinPerState.getDatas(sensorId);
            }
        },
        //websocket订阅
        subscribeParam: function (type) {
            webSocket.subscribe(headers, "/topic/per" + type + '_' + vid + "_Info", function (data) {
                jiLinPerState.getSensor0104Param(data, type);
            }, null, null);
        },
        getSensor0104Param: function (data, type) {
            if (data == null) return;
            var result = $.parseJSON(data.body);

            clearTimeout(_timeout);
            isRead = false;
            layer.closeAll();
            $("#readPerStateRefresh").html("刷新").prop('disabled', false);
            jiLinPerState.queryF3BaseParamCall(result, type);
        },
        queryF3BaseParamCall: function (result, type) {
            var data = result.data.msgBody.sensorF7;

            var workStatus = jiLinPerState.getWorkStatus(data.workStatus),//工作状态
                alarmStatus = data.alarmStatus.toString(2);//报警状态
            alarmStatus = (Array(32).join(0) + alarmStatus).slice(-32).split('').reverse();//高位补零
            var alarmData = jiLinPerState.setAlarmData(alarmStatus);

            var pre = '';
            //工作状态
            switch (type) {
                case 'F764':   //前向监测
                    $('#adas-workState').val(workStatus);
                    pre = '#adas-';
                    break;
                case 'F765':  //驾驶员行为监测
                    $('#dsm-workState').val(workStatus);
                    pre = '#dsm-';
                    break;
                default:
                    break;
            }

            //报警状态
            for (var i = 0; i < alarmData.length; i++) {
                var item = alarmData[i];
                var value = jiLinPerState.getAlarmStatus(item.value);
                $(pre + item.name).val(value);
            }
        },
        getWorkStatus: function (status) {
            switch (status) {
                case 1:
                    return '正常工作';
                    break;
                case 2:
                    return '待机状态';
                    break;
                case 3:
                    return '升级维护';
                    break;
                case 4:
                    return '设备异常';
                    break;
                case 16:
                    return '断开连接';
                    break;
                default:
                    break;
            }
        },
        setAlarmData: function (alarmStatus) {
            var stateArr = [{
                'name': 'cameraState',
                'value': alarmStatus[0]
            }, {
                'name': 'speakerState',
                'value': alarmStatus[4]
            }, {
                'name': 'mainMemory',
                'value': alarmStatus[1]
            }, {
                'name': 'auxiliaryStorage',
                'value': alarmStatus[2]
            }, {
                'name': 'communicationModule',
                'value': alarmStatus[10]
            }, {
                'name': 'positioningModule',
                'value': alarmStatus[11]
            }, {
                'name': 'battery',
                'value': alarmStatus[5]
            }, {
                'name': 'infraredFinish',
                'value': alarmStatus[3]
            }];
            return stateArr;
        },
        getAlarmStatus: function (status) {
            switch (status) {
                case '0':
                    return '正常';
                    break;
                case '1':
                    return '异常';
                    break;
                default:
                    break;
            }
        },
        //下发指令给终端
        getDatas: function (sensorID) {
            json_ajax("POST", '/clbs/adas/standard/param/getJiPeripheralInfo', "json", false, {
                "vehicleId": vid,
                "sensorID": sensorID,
                "commandType": 247,
            }, jiLinPerState.getF3BaseParamCall);
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
                jiLinPerState.createSocket0104InfoMonitor();
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

            for ( var i=0; i<socketTypeArr.length; i++){
                var sensorId = sensorIDArr[i];
                //下发指令
                jiLinPerState.getDatas(sensorId);
            }
        },
    };

    $(function () {
        jiLinPerState.init();
        //刷新
        $("#readPerStateRefresh").on("click", jiLinPerState.readInformationRefreshClick);
    })
})($, window);