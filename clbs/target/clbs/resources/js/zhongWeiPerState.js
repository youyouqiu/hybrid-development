//# sourceURL=zhongWeiPerState.js.js
(function ($, window) {
    var isRead = true;
    var _timeout;
    var vid = $("#vehicleId").val();
    var socketTypes = [61921, 61922, 61924, 61923];//[前向监测, 驾驶员监测, 盲区, 胎压]

    zhongWeiPerState = {
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
            for (var i = 0; i < socketTypes.length; i++) {
                var socketType = socketTypes[i];
                zhongWeiPerState.subscribeParam(socketType);//socket订阅
            }

            //下发指令
            zhongWeiPerState.getDatas(225);//驾驶辅助设备
            zhongWeiPerState.getDatas(226);//驾驶员行为监测设备
            zhongWeiPerState.getDatas(227);//胎压
            zhongWeiPerState.getDatas(228);//盲区
        },
        //websocket订阅
        subscribeParam: function (type) {
            webSocket.subscribe(headers, "/user/topic/per" + type + "Info", function (data) {
                zhongWeiPerState.getSensor0104Param(data, type);
            }, null, null);
        },
        getSensor0104Param: function (data, type) {
            if (data == null) return;
            var result = $.parseJSON(data.body);

            clearTimeout(_timeout);
            isRead = false;
            layer.closeAll();
            console.log('外设状态',result);
            $("#readPerStateRefresh").html("刷新").prop('disabled', false);
            zhongWeiPerState.queryF3BaseParamCall(result, type);
        },
        queryF3BaseParamCall: function (result, type) {
            var data = result.data.msgBody.params[0].value.sensorStatus;
            var workStatus = zhongWeiPerState.getWorkStatus(data.workStatus),//工作状态
                alarmStatus = data.alarmStatus.toString(2);//报警状态
            alarmStatus = (Array(32).join(0) + alarmStatus).slice(-32).split('').reverse();//高位补零
            var alarmData = zhongWeiPerState.setAlarmData(alarmStatus);

            var pre = '';
            //工作状态
            if (type == '61921') {//前向监测系统
                $('#adas-workState').val(workStatus);
                pre = '#adas-';
            } else if(type == '61922'){//驾驶员监测系统
                $('#dsm-workState').val(workStatus);
                pre = '#dsm-';
            }else if(type == '61923'){//胎压
                $('#tps-workState').val(workStatus);
                pre = '#tps-';
            }else if(type == '61924'){//盲区
                $('#bsm-workState').val(workStatus);
                pre = '#bsm-';
            }

            //报警状态
            for (var i = 0; i < alarmData.length; i++) {
                var item = alarmData[i];
                var value = zhongWeiPerState.getAlarmStatus(item.value);
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
                case 5:
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
            json_ajax("POST", '/clbs/r/riskManagement/DefineSettings/getPeripheralInfo', "json", false, {
                "vid": vid,
                "sensorID": sensorID,
                "commandType": 241,
            }, zhongWeiPerState.getF3BaseParamCall);
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
                zhongWeiPerState.createSocket0104InfoMonitor();
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
            zhongWeiPerState.getDatas(225);
            zhongWeiPerState.getDatas(226);
            zhongWeiPerState.getDatas(227);
            zhongWeiPerState.getDatas(228);
        },
    }

    $(function () {
        zhongWeiPerState.init();
        //刷新
        $("#readPerStateRefresh").on("click", zhongWeiPerState.readInformationRefreshClick);
    })
}($, window))