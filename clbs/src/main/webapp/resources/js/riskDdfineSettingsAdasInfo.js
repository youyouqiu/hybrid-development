//# sourceURL=riskDdfineSettingsAdasInfo.js
(function ($, window) {
    var vid = $("#vehicleId").val();//车辆id
    var brand = $("#brand").val();//车牌号
    var _timeout;
    //var layer_time;
    isRead = true;
    var timerInterval;

    adasInfoObj = {
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
            //连接webSocket,获取传感器上传参数信息
            webSocket.subscribe(headers, "/user/topic/per62433Info", adasInfoObj.getSensor0104Param, null, null);
            //传感器参数信息
            adasInfoObj.getPeripheralInfo();
        },
        //处理获取设备上传数据
        getSensor0104Param: function (msg) {
            if (msg == null) {
                return;
            }
            var result = $.parseJSON(msg.body);
            /*var msgSNAck = result.data.msgBody.msgSNAck;
            if (msgSNAck != $("#msgId").val()) {
                return;
            }*/
            isRead = false;
            layer.closeAll();
            if (layer_time) {
                clearTimeout(layer_time);
            }
            clearTimeout(_timeout);
            $("#adasInfoSend").removeAttr("disabled");
            var id = result.data.msgBody.params[0].id;
            if (id == "62433" || id == 62433) {//常规参数
                adasInfoObj.queryF4ParamCall(result);
            }
        },
        //传感器-常规参数-设备上报数据
        queryF4ParamCall: function (result) {
            $("#adasInfoRefresh").removeAttr("disabled");
            $("#adasInfoSend").removeAttr("disabled");
            $(".sensorBtn").removeAttr("disabled");
            $(".platformBtn").removeAttr("disabled");
            var param = result.data.msgBody.params[0].value.assist;
            //渲染传感器上传参数
            adasInfoObj.setSensorData(param);
        },
        //渲染传感器上传参数
        setSensorData: function (data) {
            if (data != null) {
                adasInfoObj.setInputDataCallBack(data, 'sensor');

                //障碍物写死
                adasInfoObj.setObstacleInputData();
            }
        },
        //传感器常规参数-获取设备数据返回
        setreadConventionalCall: function (data) {
            if (data.success) {
                layer_time = setTimeout(function () {
                    if (isRead) {
                        layer.load(2);
                    }
                }, 0);
                adasInfoObj.createSocket0104InfoMonitor(data.msg);
            } else {
                if (data.msg) {
                    layer.msg(data.msg);
                }
            }
        },
        //创建消息监听
        createSocket0104InfoMonitor: function (msg) {
            var msg = $.parseJSON(msg);
            $("#msgId").val(msg.msgId);
            headers = {"UserName": msg.userName};
            clearTimeout(_timeout);
            _timeout = setTimeout(function () {
                if (isRead) {
                    isRead = false;
                    clearTimeout(layer_time);
                    layer.closeAll();
                    layer.msg("获取设备数据失败!");
                }
            }, 60000);
        },
        //传感器常规参数-获取信息-刷新
        readConventionalRefreshClick: function () {
            isRead = true;
            $("#adasPoilSendStatus").val("").removeAttr("data-id");//下发状态
            $("#adasInfoSend").attr("disabled", "disabled");
            $(".sensorBtn").attr("disabled", "disabled");
            $(".platformBtn").attr("disabled", "disabled");
            //平台设置参数字段赋值
            adasInfoObj.getPlatformData();
            //传感器参数信息
            adasInfoObj.getPeripheralInfo();
        },
        //传感器参数信息
        getPeripheralInfo: function () {
            url = '/clbs/r/riskManagement/DefineSettings/getPeripheralInfo';
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "sensorID": 225,
                "commandType": 243
            }, adasInfoObj.setreadConventionalCall);
        },
        //创建消息监听
        createSocket0900StatusMonitor: function (msg) {
            var requestStrS = {
                "desc": {
                    "cmsgSN": msg.msgId,
                    "UserName": msg.userName
                },
                "data": []
            };
            headers = {"UserName": msg.userName};
            webSocket.subscribe(headers, "/user/topic/t808_currency_response", adasInfoObj.currencyResponse, "/app/vehicle/oil/setting", requestStrS);
            webSocket.subscribe(headers, "/user/topic/deviceReportLog", adasInfoObj.deviceReportLog, "/app/vehicle/oil/setting", requestStrS);
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
        //传感器常规参数对比后赋值(以传感器为准)
        sensorBtnClick: function () {
            $("#dealType").val("sb");
            var allInput = $(this).parents('.info-content').find('input');
            var len = allInput.length;
            for (var i = 0; i < len; i++) {
                var curId = $(allInput[i]).attr('id');
                if (curId.indexOf('sensor') != '-1') {
                    var targetInput = $(allInput[i]).parent().siblings().find('input');
                    if (targetInput.attr('id').indexOf('platform') != '-1' && targetInput.attr('id') != 'platform-vehicleCollisionTimeInterval') {
                        targetInput.val($(allInput[i]).val());
                        if ($(allInput[i]).attr('data-val')) {
                            targetInput.attr('data-val', $(allInput[i]).attr('data-val'));
                        }
                    }
                }
            }
        },
        //传感器常规参数对比后赋值(以平台设置为准)
        platformBtnClick: function () {
            $("#dealType").val("pt");
            var allInput = $(this).parents('.info-content').find('input');
            var len = allInput.length;
            for (var i = 0; i < len; i++) {
                var curId = $(allInput[i]).attr('id');
                if (curId.indexOf('platform') != '-1') {
                    var targetInput = $(allInput[i]).parent().siblings().find('input');
                    if (targetInput.attr('id').indexOf('sensor') != '-1' && targetInput.attr('id') != 'platform-vehicleCollisionTimeInterval') {
                        targetInput.val($(allInput[i]).val());
                        if ($(allInput[i]).attr('data-val')) {
                            targetInput.attr('data-val', $(allInput[i]).attr('data-val'));
                        }
                    }
                }
            }
        },
        /**
         * 点击显示隐藏信息
         */
        hiddenparameterFn: function () {
            var clickId = $(this).attr('id');
            if (!($("#" + clickId + "-content").is(":hidden"))) {
                $("#" + clickId + "-content").slideUp();
                $("#" + clickId).children("font").text("显示更多");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-down");
            } else {
                $("#" + clickId + "-content").slideDown();
                $("#" + clickId).children("font").text("隐藏信息");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-up");
            }
        },
        //获取平台设置参数
        getPlatformData: function () {
            var url = '/clbs/r/riskManagement/DefineSettings/refresh_' + vid + '_' + brand + '.gsp';
            json_ajax("GET", url, "json", false, null, adasInfoObj.setInputData);
        },
        /**
         * 为平台设置参数字段赋值
         */
        setInputData: function (msg) {
            if (msg.success) {
                var data = JSON.parse(msg.obj.riskSettingList);
                var len = data.length;
                for (var i = 0; i < len; i++) {
                    var riskId = data[i].riskId;
                    switch (riskId) {
                        case '6401'://前车碰撞
                            adasInfoObj.setInputDataCallBack(data[i], 'platform', 'vehicleCollision');
                            break;
                        case '6402'://车道偏离
                            adasInfoObj.setInputDataCallBack(data[i], 'platform', 'deviate');
                            break;
                        case '6403'://车距过近
                            adasInfoObj.setInputDataCallBack(data[i], 'platform', 'distance');
                            break;
                        case '6404'://行人碰撞
                            adasInfoObj.setInputDataCallBack(data[i], 'platform', 'pedestrian');
                            break;
                        case '6405'://频繁变道
                            adasInfoObj.setInputDataCallBack(data[i], 'platform', 'laneChange');
                            break;
                        case '6407'://障碍物
                            adasInfoObj.setInputDataCallBack(data[i], 'platform', 'obstacle');
                            break;
                        case '6408'://急加/急减/急转弯
                            adasInfoObj.setInputDataCallBack(data[i], 'platform', 'quick');
                            break;
                        case '6409'://道路标识超限
                            adasInfoObj.setInputDataCallBack(data[i], 'platform', 'speedLimit');
                            break;
                        case '6410'://道路标识识别
                            adasInfoObj.setInputDataCallBack(data[i], 'platform', 'roadMarking');
                            break;
                    }
                }

            }
        },
        setObstacleInputData: function () {
            $('#sensor-obstacleVideoTime').val($('#platform-obstacleVideoRecordingTime').val());
            $('#sensor-obstacleCameraNum').val($('#platform-obstaclePhotographNumber').val());
            $('#sensor-obstacleCameraTime').val($('#platform-obstaclePhotographTime').val());
            $('#sensor-obstacleOneLevelAlarmEnable').val($('#platform-obstacleOneLevelAlarmEnable').val());
            $('#sensor-obstacleTwoLevelAlarmEnable').val($('#platform-obstacleTwoLevelAlarmEnable').val());
            $('#sensor-obstacleOneLevelVoiceEnable').val($('#platform-obstacleOneLevelVoiceEnable').val());
            $('#sensor-obstacleTwoLevelVoiceEnable').val($('#platform-obstacleTwoLevelVoiceEnable').val());
            $('#sensor-obstacleDistance').val($('#platform-obstacleTimeInterval').val());
            $('#sensor-obstacleSpeed').val($('#platform-obstacleHighSpeed').val());
        },
        setInputDataCallBack: function (data, type, target) {
            for (key in data) {
                var preId = "#" + type + "-";
                if (target) {
                    var $newId = $(preId + target + key.substr(0, 1).toUpperCase() + key.substr(1));//组装目标字段id
                }
                switch (key) {
                    case 'cameraResolution'://拍照分辨率
                        var keyVal = adasInfoObj.formatChange(data[key]);
                        $(preId + key).val(adasInfoObj.changeCameraResolution(keyVal)).attr('data-val', keyVal);
                        break;
                    case 'videoResolution'://视频录制分辨率
                        var keyVal = adasInfoObj.formatChange(data[key]);
                        $(preId + key).val(adasInfoObj.changeVideoResolution(keyVal)).attr('data-val', keyVal);
                        break;
                    case 'eventVoiceEnable'://语音提醒
                        adasInfoObj.voiceChange(data[key], key);
                        break;
                    case 'cameraStrategy'://拍照策略
                        adasInfoObj.strategyChange(data[key], key);
                        break;
                    case 'timingCapture'://定时
                    case 'distanceCapture'://定距
                        $(preId + key).val(adasInfoObj.onOrOffReverse(data[key])).attr('data-val', data[key]);//全局参数
                        if ($newId) {
                            $newId.val(adasInfoObj.onOrOffReverse(data[key])).attr('data-val', data[key]);
                        }
                        break;
                    case 'eventEnable'://主动抓拍/道路标识识别
                        adasInfoObj.eventEnableChange(data[key], key);
                        break;
                    case 'twoLevelVoiceEnable'://二级语音提醒
                        if (type == 'sensor') {
                            adasInfoObj.twoLevelVoiceChange(data[key], key, type);
                        } else {
                            $(preId + key).val(adasInfoObj.onOrOff(data[key])).attr('data-val', data[key]);//全局参数
                            if ($newId) {
                                $newId.val(adasInfoObj.onOrOff(data[key])).attr('data-val', data[key]);
                            }
                        }
                        break;
                    case 'cameraTime'://拍照间隔
                    case 'deviateCameraTime'://拍照间隔
                    case 'distanceCameraTime'://拍照间隔
                    case 'laneChangeCameraTime'://拍照间隔
                    case 'obstacleCameraTime'://拍照间隔
                    case 'pedestrianCollisionCameraTime'://拍照间隔
                    case 'quickCameraTime'://拍照间隔
                    case 'roadMarkingCameraTime'://拍照间隔
                    case 'roadMarkingCameraNum'://拍照间隔
                    case 'speedLimitCameraTime'://拍照间隔
                    case 'vehicleCollisionCameraTime'://拍照间隔
                        if ($newId) {
                            $newId.val(data[key] / 10);
                        }
                        $(preId + key).val(data[key] / 10);
                        if (key == 'roadMarkingCameraTime') {
                            $(preId + key).val(data[key]);
                        }
                        break;
                    /*case 'lowSpeedLevel'://低速阈值
                    case 'highSpeedLevel'://高速阈值
                        $(preId + key).val(adasInfoObj.changeLowThreshold(data[key])).attr('data-val', data[key]);
                        $newId.val(adasInfoObj.changeLowThreshold(data[key])).attr('data-val', data[key]);
                        break;*/
                    case 'initiativeCaptureAlarmEnable':
                    case 'initiativeCaptureVoiceEnable':
                    case 'oneLevelAlarmEnable':
                    case 'twoLevelAlarmEnable':
                    case 'oneLevelVoiceEnable':
                    case 'voiceEnable':
                    case 'roadMarkAlarmEnable':
                        $(preId + key).val(adasInfoObj.onOrOff(data[key])).attr('data-val', data[key]);//全局参数
                        if ($newId) {
                            $newId.val(adasInfoObj.onOrOff(data[key])).attr('data-val', data[key]);
                        }
                        if (type == 'sensor') {
                            if (key == 'oneLevelAlarmEnable' || key == 'twoLevelAlarmEnable' || key == 'oneLevelVoiceEnable') {
                                adasInfoObj.alarmChange(data[key], key);
                            }
                        }
                        break;
                    case 'timeInterval'://平台设置的时间阈值
                        if ($newId) {
                            $newId.val(data[key]);
                        }
                        $(preId + key).val(data[key]);
                        break;
                    //传感器时间阈值
                    case 'vehicleCollisionTime':
                    case 'pedestrianCollisionTime':
                    case 'obstacleDistance':
                        $(preId + key).val(data[key] / 10);
                        break;
                    default:
                        if ($newId) {
                            $newId.val(data[key]);
                        }
                        $(preId + key).val(data[key]);//全局参数
                        break;
                }
            }
        },
        //十六进制转换
        formatChange: function (data) {
            switch (data) {
                case 1:
                    data = '0x01';
                    break;
                case 2:
                    data = '0x02';
                    break;
                case 3:
                    data = '0x03';
                    break;
                case 4:
                    data = '0x04';
                    break;
                case 5:
                    data = '0x05';
                    break;
                case 6:
                    data = '0x06';
                    break;
                case 7:
                    data = '0x07';
                    break;
            }
            return data;
        }

        ,
        //一级二级报警,语音报警提醒转换
        alarmChange: function (data, key) {
            var oneLevel = data.toString(2);//一级报警
            oneLevel = (Array(11).join(0) + oneLevel).slice(-11);//高位补零
            var vehicleCollision = oneLevel.substr(-1, 1);//前车碰撞
            var deviate = oneLevel.substr(-2, 1);//车道左偏离
            var distance = oneLevel.substr(-4, 1);//车距过近
            var pedestrian = oneLevel.substr(-5, 1);//行人碰撞
            var laneChange = oneLevel.substr(-6, 1);//频繁变道
            var speedLimit = oneLevel.substr(-7, 1);//道路标识超限
            var obstacle = oneLevel.substr(-8, 1);//障碍物
            var quick = oneLevel.substr(-9, 1);//急加速

            var dataArr = [{
                'name': 'vehicleCollision',
                'data': vehicleCollision
            }, {
                'name': 'deviate',
                'data': deviate
            }, {
                'name': 'distance',
                'data': distance
            }, {
                'name': 'pedestrian',
                'data': pedestrian
            }, {
                'name': 'laneChange',
                'data': laneChange
            }, {
                'name': 'speedLimit',
                'data': speedLimit
            }, {
                'name': 'obstacle',
                'data': obstacle
            }, {
                'name': 'quick',
                'data': quick
            }];
            var len = dataArr.length;
            for (var i = 0; i < len; i++) {
                $("#sensor-" + dataArr[i].name + key.substr(0, 1).toUpperCase() + key.substr(1)).val(adasInfoObj.onOrOff(dataArr[i].data)).attr('data-val', dataArr[i].data);
            }
        }
        ,
        //二级语音提醒
        twoLevelVoiceChange: function (data, key, type) {
            var oneLevel = data.toString(2);//一级报警
            oneLevel = (Array(11).join(0) + oneLevel).slice(-11);//高位补零
            var vehicleCollision = oneLevel.substr(-1, 1);//前车碰撞
            var deviate = oneLevel.substr(-2, 1);//车道左偏离
            var distance = oneLevel.substr(-4, 1);//车距过近
            var pedestrian = oneLevel.substr(-5, 1);//行人碰撞
            var laneChange = oneLevel.substr(-6, 1);//频繁变道
            var speedLimit = oneLevel.substr(-7, 1);//道路标识超限
            var obstacle = oneLevel.substr(-8, 1);//障碍物
            var quick = oneLevel.substr(-9, 1);//急加速

            var dataArr = [{
                'name': 'vehicleCollision',
                'data': vehicleCollision
            }, {
                'name': 'deviate',
                'data': deviate
            }, {
                'name': 'distance',
                'data': distance
            }, {
                'name': 'pedestrian',
                'data': pedestrian
            }, {
                'name': 'laneChange',
                'data': laneChange
            }, {
                'name': 'speedLimit',
                'data': speedLimit
            }, {
                'name': 'obstacle',
                'data': obstacle
            }, {
                'name': 'quick',
                'data': quick
            }];
            var len = dataArr.length;
            for (var i = 0; i < len; i++) {
                var val = adasInfoObj.onOrOff(dataArr[i].data);
                $("#" + type + "-" + dataArr[i].name + key.substr(0, 1).toUpperCase() + key.substr(1)).val(val).attr('data-val', dataArr[i].data);
            }
        }
        ,
        //语音提醒转换
        voiceChange: function (data, key) {
            var eventVoice = data.toString(2);
            eventVoice = (Array(4).join(0) + eventVoice).slice(-4);//高位补零
            var roadMarking = eventVoice.substr(-1, 1);//道路标识识别
            var snap = eventVoice.substr(-2, 1);//主动抓拍

            var dataArr = [{
                'name': 'roadMarking',
                'data': roadMarking
            }, {
                'name': 'snap',
                'data': snap
            }];
            var len = dataArr.length;
            for (var i = 0; i < len; i++) {
                $("#sensor-" + dataArr[i].name + key.substr(0, 1).toUpperCase() + key.substr(1)).val(adasInfoObj.onOrOff(dataArr[i].data)).attr('data-val', dataArr[i].data);
            }
        }
        ,
        //定时抓拍,定距抓拍转换
        strategyChange: function (data, key) {
            var strategy = data.toString(2);
            strategy = (Array(4).join(0) + strategy).slice(-4);//高位补零
            var timingCapture = strategy.substr(-1, 1);//定时抓拍
            var distanceCapture = strategy.substr(-2, 1);//定距抓拍

            var dataArr = [{
                'name': 'timingCapture',
                'data': timingCapture
            }, {
                'name': 'distanceCapture',
                'data': distanceCapture
            }];
            var len = dataArr.length;
            for (var i = 0; i < len; i++) {
                $("#sensor-" + dataArr[i].name).val(adasInfoObj.onOrOffReverse(dataArr[i].data)).attr('data-val', dataArr[i].data);
            }
        }
        ,
        //主动抓拍,道路标识识别转换
        eventEnableChange: function (data, key) {
            var eventEnable = data.toString(2);
            eventEnable = (Array(4).join(0) + eventEnable).slice(-4);//高位补零
            var roadMarking = eventEnable.substr(-1, 1);//道路标识识别
            var snap = eventEnable.substr(-2, 1);//主动抓拍

            var dataArr = [{
                'name': 'roadMarking',
                'data': roadMarking
            }, {
                'name': 'snap',
                'data': snap
            }];
            var len = dataArr.length;
            for (var i = 0; i < len; i++) {
                $("#sensor-" + dataArr[i].name + key.substr(0, 1).toUpperCase() + key.substr(1)).val(adasInfoObj.onOrOff(dataArr[i].data)).attr('data-val', dataArr[i].data);
            }
        }
        ,
        /**
         * 拍照分辨率转换
         * @param data : 数据字段值
         * @returns {string}
         */
        changeCameraResolution: function (data) {
            switch (data) {
                case '0x01':
                    data = '352×288';
                    break;
                case '0x02':
                    data = '704×288';
                    break;
                case '0x03':
                    data = '704×576';
                    break;
                case '0x04':
                    data = '640×480';
                    break;
                case '0x05':
                    data = '1280×720';
                    break;
                case '0x06':
                    data = '1920×1080';
                    break;
                default:
                    data = '';
            }
            return data;
        }
        ,
        /**
         * 视频录制分辨率转换
         * @param data : 数据字段值
         * @returns {string}
         */
        changeVideoResolution: function (data) {
            switch (data) {
                case '0x01':
                    data = 'CIF';
                    break;
                case '0x02':
                    data = 'WCIF';
                    break;
                case '0x03':
                    data = 'D1';
                    break;
                case '0x04':
                    data = 'WD1';
                    break;
                case '0x05':
                    data = '720P';
                    break;
                case '0x06':
                    data = '1080P';
                    break;
                default:
                    data = '';
            }
            return data;
        }
        ,
        /**
         * 低速阈值/高速阈值转换
         * @param data : 数据字段值
         * @returns {string}
         */
        /*changeLowThreshold: function (data) {
            switch (data) {
                case '1':
                    data = '一般（低）';
                    break;
                case '2':
                    data = '一般（中）';
                    break;
                case '3':
                    data = '一般（高）';
                    break;
                case '4':
                    data = '较重（低）';
                    break;
                case '5':
                    data = '较重（中）';
                    break;
                case '6':
                    data = '较重（高）';
                    break;
                case '7':
                    data = '严重（低）';
                    break;
                case '8':
                    data = '严重（中）';
                    break;
                case '9':
                    data = '严重（高）';
                    break;
                case '10':
                    data = '特重（低）';
                    break;
                case '11':
                    data = '特重（中）';
                    break;
                case '12':
                    data = '特重（高）';
                    break;
                default:
                    data = '';
            }
            return data;
        },*/
        /**
         * 开关状态转换
         * @param data : 数据字段值
         * @returns {string}
         */
        onOrOff: function (data) {
            if (data == '1') {
                data = '开';
            } else {
                data = '关';
            }
            return data;
        }
        ,
        /**
         * 开关状态转换(反向)
         * @returns {string}
         */
        onOrOffReverse: function (data) {
            if (data == '1') {
                data = '开';
            } else {
                data = '关';
            }
            return data;
        }
        ,
        /**
         * 参数修正下发
         */
        adasInfoSendClick: function () {
            var jsonData = adasInfoObj.setJsonData();//获取到修正后的数据值
            var data = {
                "checkedParams": JSON.stringify(jsonData),
                "id": vid,
                'flag': true
            };
            json_ajax("POST", "/clbs/r/riskManagement/DefineSettings/settingFlag.gsp", "json", false, data, adasInfoObj.adasInfoSendCallback);
        }
        ,
        adasInfoSendCallback: function (data) {
            if (data != null) {
                if (data.success) {
                    //下发ADAS外设参数
                    adasInfoObj.sendAdasParameter();
                    layer.load(2);
                } else {
                    if (data.msg) {
                        layer.msg(data.msg, {move: false});
                    }
                }
            }
        }
        ,
        //下发ADAS外设参数
        sendAdasParameter: function () {
            var url = '/clbs/adas/paramSetting/sendAdasParameter';
            json_ajax("POST", url, "json", false, {'vehicleId': vid}, function (data) {
                if (data.success) {
                    if (data.obj) {
                        var msgid = data.obj.msgId;
                        var sendstatusname = "send_status_" + msgid;
                        $("#adasPoilSendStatus").attr("data-id", sendstatusname).addClass(sendstatusname).val("参数已下发");
                        adasInfoObj.createSocket0900StatusMonitor(data.obj);
                        setTimeout(function () {
                            timerInterval = setInterval(adasInfoObj.getStatus(msgid), 5000);
                        }, 2000);
                        setTimeout(function () {
                            clearInterval(timerInterval);
                        }, 60000);
                    }
                }
            });
        }
        ,
        //获取参数下发状态
        getStatus: function (msgId) {
            url = '/clbs/r/riskManagement/DefineSettings/getStatus';
            json_ajax("POST", url, "json", false, {
                "vehicleId": vid,
                "swiftNumber": msgId,
                'flag': true
            }, function (data) {
                if (data.success) {
                    if (data.obj) {
                        var status = data.obj.status;
                        $("#adasPoilSendStatus").val(adasInfoObj.statusChange(status));
                        //传感器参数信息
                        adasInfoObj.getPeripheralInfo();
                        if (status != 7) {
                            clearInterval(timerInterval);
                        }
                    }
                }
            });
        }
        ,
        //下发状态转换
        statusChange: function (data) {
            switch (data) {
                case 0:
                    data = '参数已生效';
                    break;
                case 1:
                    data = '参数未生效';
                    break;
                case 2:
                    data = '参数消息有误';
                    break;
                case 3:
                    data = '参数不支持';
                    break;
                case 4:
                    data = '参数下发中';
                    break;
                case 5:
                    data = '终端离线，未下发';
                    break;
                case 7:
                    data = '终端处理中';
                    break;
                case 8:
                    data = '终端接收失败';
                    break;
                default:
                    data = '';
                    break;
            }
            return data;
        }
        ,
        /**
         * 组装下发参数
         * @returns {any[]}
         */
        setJsonData: function () {
            var dataArray = new Array();

            var jsonStr6401 = adasInfoObj.setParam("6401", "vehicleCollision");//前车碰撞
            var jsonStr6402 = adasInfoObj.setParam("6402", "deviate"); //车道偏离
            var jsonStr6403 = adasInfoObj.setParam("6403", "distance"); // 车距过近
            var jsonStr6404 = adasInfoObj.setParam("6404", "pedestrian"); // 行人碰撞
            var jsonStr6405 = adasInfoObj.setParam("6405", "laneChange");//频繁变道
            var jsonStr6407 = adasInfoObj.setParam("6407", "obstacle");//障碍物
            var jsonStr6408 = adasInfoObj.setParam("6408", "quick");//急加/急减/急转弯
            var jsonStr6409 = adasInfoObj.setParam("6409", "speedLimit");//道路标识超限
            var jsonStr6410 = adasInfoObj.setParam("6410", "roadMarking");//道路标识识别

            dataArray.push(jsonStr6401, jsonStr6402, jsonStr6403, jsonStr6404, jsonStr6405, jsonStr6407, jsonStr6408, jsonStr6409, jsonStr6410);
            return dataArray;
        }
        ,
        setParam: function (riskId, targetName) {
            var param = {};
            param.riskId = riskId;

            var allInput = $('#' + targetName + '-content').find('input');
            adasInfoObj.getInputData(param, allInput, targetName);

            //全局参数
            var globalInput = $("#globalParameters-content").find('input');
            adasInfoObj.getInputData(param, globalInput);

            //主动抓拍
            var snapInput = $("#snap-content").find('input');
            adasInfoObj.getInputData(param, snapInput);
            return param;
        }
        ,
        getInputData: function (param, data, targetName) {
            var len = data.length;
            for (var i = 0; i < len; i++) {
                var curId = $(data[i]).attr('id');
                if (curId.indexOf('platform') != '-1') {
                    if (targetName) {
                        var keyName = curId.replace('platform-' + targetName, '').toLowerCase();//转换字段key值,例如:platform-deviateVideoRecordingTime 替换成 videoRecordingTime
                    } else {
                        var keyName = curId.replace('platform-', '');
                    }
                    var keyVal = $(data[i]).val();
                    if ($(data[i]).attr('data-val')) {
                        keyVal = $(data[i]).attr('data-val');
                    }
                    param[keyName] = keyVal;
                }
            }
        }
    };
    $(function () {
        $('input').inputClear();
        adasInfoObj.init();

        //平台设置参数字段赋值
        adasInfoObj.getPlatformData();

        //点击事件
        $(".info-span").on("click", adasInfoObj.hiddenparameterFn);//显示隐藏信息
        $("#adasInfoSend").on("click", adasInfoObj.adasInfoSendClick);//修正下发
        $("#adasInfoRefresh").on("click", adasInfoObj.readConventionalRefreshClick);//刷新
        $(".sensorBtn").on("click", adasInfoObj.sensorBtnClick);//以传感器为准
        $(".platformBtn").on("click", adasInfoObj.platformBtnClick);//以平台设置为准
    })
})
($, window)