//# sourceURL=definesettingedit.js
var luPerState;

(function ($, window) {
    var isRead = true;
    var _timeout;
    var vid = $("#vehicleId").val();
    var socketTypeArr = ['F764', 'F765', 'F766', 'F767']; //[高级驾驶辅助, 驾驶员状态监测, 胎压监测, 盲区监测]
    var sensorIDArr = [100, 101, 102, 103]; //[高级驾驶辅助, 驾驶员状态监测, 胎压监测, 盲区监测]

    luPerState = {
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

            for (var i = 0; i < socketTypeArr.length; i++) {
                var type = socketTypeArr[i],
                    sensorId = sensorIDArr[i];

                luPerState.subscribeParam(type);
                luPerState.getDatas(sensorId);
            }
        },
        //websocket订阅
        subscribeParam: function (type) {
            webSocket.subscribe(headers, "/topic/per" + type + '_' + vid + "_Info", function (data) {
                luPerState.getSensor0104Param(data, type);
            }, null, null);
        },
        getSensor0104Param: function (data, type) {
            if (data == null) return;
            var result = $.parseJSON(data.body);

            clearTimeout(_timeout);
            isRead = false;
            layer.closeAll();
            $("#readPerStateRefresh").html("刷新").prop('disabled', false);
            luPerState.queryF3BaseParamCall(result, type);
        },
        binaryConvert: function (num, from, to) {
            from = from || 10
            to = to || 2
            var temp = num + '';
            var result = parseInt(temp, from).toString(to);
            return result;
        },
        queryF3BaseParamCall: function (result, type) {
            var data = result.data.msgBody.sensorF7;

            var workStatus = luPerState.getWorkStatus(data.workStatus)//工作状态
            var alarmStatus = luPerState.binaryConvert(data.alarmStatus, 10, 2).split('').reverse();//报警状态
            for (var i = 12 - alarmStatus.length; i > 0; i--) {
                alarmStatus.push('0')
            }
            var justDoIt = function (idPrefix) {
                $('#' + idPrefix + '-workState').val(workStatus)
                var idMaps = {
                    0: 'cameraState',
                    1: 'mainMemory',
                    2: 'auxiliaryStorage',
                    3: 'infraredFinish',
                    4: 'speakerState',
                    5: 'battery',
                    10: 'communicationModule',
                    11: 'positioningModule'
                }
                alarmStatus.forEach(function (value, idx) {
                    var value = luPerState.getAlarmStatus(value);
                    var id = idMaps[idx]
                    if (id) {
                        $('#' + idPrefix + '-' + id).val(value);
                    }
                })
            }
            switch (type) {
                case 'F764':
                    justDoIt('driverAssistant')
                    break;
                case 'F765':
                    justDoIt('driverStatus')
                    break;
                case 'F766':
                    justDoIt('tirePressure')
                    break;
                case 'F767':
                    justDoIt('blindSpot')
                    break;
                default :
                    break;
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
            }, luPerState.getF3BaseParamCall);
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
                luPerState.createSocket0104InfoMonitor();
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
                var sensorId = sensorIDArr[i];
                luPerState.getDatas(sensorId);
            }
        },
    }

    $(function () {
        luPerState.init();
        //刷新
        $("#readPerStateRefresh").on("click", luPerState.readInformationRefreshClick);
    })
}($, window))