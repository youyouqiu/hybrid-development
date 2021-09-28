(function ($, window) {
    var vid = $("#vehicleId").val();//车辆id
    var brand = $("#brand").val();//车牌号
    var _timeout;
    isRead = true;
    /* var layer_time;
     */
    var timerInterval;

    dsmInfoObj = {
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
            webSocket.subscribe(headers, "/user/topic/per62434Info", dsmInfoObj.getSensor0104Param, null, null);

            //传感器参数信息
            dsmInfoObj.getPeripheralInfo();
        },
        //传感器参数信息
        getPeripheralInfo: function () {
            url = '/clbs/r/riskManagement/DefineSettings/getPeripheralInfo';
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "sensorID": 226,
                "commandType": 243
            }, dsmInfoObj.setreadConventionalCall);
        },
        //处理获取设备上传数据
        getSensor0104Param: function (msg) {
            if (msg == null)
                return;
            var result = $.parseJSON(msg.body);
            /*var msgSNAck = result.data.msgBody.msgSNAck;
            if (msgSNAck != $("#msgId").val()) {
                return;
            }*/
            isRead = false;
            clearTimeout(_timeout);
            layer.closeAll();
            if (layer_time) {
                clearTimeout(layer_time);
            }
            $("#dsmInfoSend").removeAttr("disabled");
            var id = result.data.msgBody.params[0].id;
            if (id == "62434" || id == 62434) {//常规参数
                dsmInfoObj.queryF4ParamCall(result);
                return;
            }
        },
        //传感器-常规参数-设备上报数据
        queryF4ParamCall: function (result) {
            $("#dsmInfoRefresh").removeAttr("disabled");
            $("#dsmInfoSend").removeAttr("disabled");
            $(".sensorBtn").removeAttr("disabled");
            $(".platformBtn").removeAttr("disabled");
            var param = result.data.msgBody.params[0].value.surveyInfo;
            //渲染传感器上传参数
            dsmInfoObj.setSensorData(param);
        },
        //渲染传感器上传参数
        setSensorData: function (data) {
            if (data != null) {
                dsmInfoObj.setInputDataCallBack(data, 'sensor');
            }
        },
        //传感器常规参数-获取设备数据返回
        setreadConventionalCall: function (data) {
            if (data.success) {
                layer_time = window.setTimeout(function () {
                    if (isRead) {
                        layer.load(2);
                    }
                }, 0);
                dsmInfoObj.createSocket0104InfoMonitor(data.msg);
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
            $("#dsmPoilSendStatus").val("").removeAttr("data-id");//下发状态
            var vid = $("#vehicleId").val();
            $("#dsmInfoSend").attr("disabled", "disabled");
            $(".sensorBtn").attr("disabled", "disabled");
            $(".platformBtn").attr("disabled", "disabled");
            //为平台设置参数字段赋值
            dsmInfoObj.getPlatformData();

            //传感器参数信息
            dsmInfoObj.getPeripheralInfo();
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
            webSocket.subscribe(headers, "/user/topic/t808_currency_response", dsmInfoObj.currencyResponse, "/app/vehicle/oil/setting", requestStrS);
            webSocket.subscribe(headers, "/user/topic/deviceReportLog", dsmInfoObj.deviceReportLog, "/app/vehicle/oil/setting", requestStrS);
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
                    if (targetInput.attr('id').indexOf('platform') != '-1') {
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
                    if (targetInput.attr('id').indexOf('sensor') != '-1') {
                        targetInput.val($(allInput[i]).val());
                        if ($(allInput[i]).attr('data-val')) {
                            targetInput.attr('data-val', $(allInput[i]).attr('data-val'));
                        }
                    }
                }
            }
        },
        //点击显示隐藏信息
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
            json_ajax("GET", url, "json", false, null, dsmInfoObj.setInputData);
        },
        //为平台设置参数字段赋值
        setInputData: function (msg) {
            if (msg.success) {
                var data = JSON.parse(msg.obj.riskSettingList);
                var len = data.length;
                for (var i = 0; i < len; i++) {
                    var riskId = data[i].riskId;
                    switch (riskId) {
                        case '6506'://闭眼
                            dsmInfoObj.setInputDataCallBack(data[i], 'platform', 'closeEyes');
                            break;
                        case '6507'://打哈欠
                            dsmInfoObj.setInputDataCallBack(data[i], 'platform', 'yawn');
                            break;
                        case '6503'://抽烟
                            dsmInfoObj.setInputDataCallBack(data[i], 'platform', 'smoking');
                            break;
                        case '6502'://接打电话
                            dsmInfoObj.setInputDataCallBack(data[i], 'platform', 'answerThephone');
                            break;
                        case '6508'://姿态异常
                            dsmInfoObj.setInputDataCallBack(data[i], 'platform', 'driveDeed');
                            break;
                        case '6509'://驾驶员人证不符
                            dsmInfoObj.setInputDataCallBack(data[i], 'platform', 'driverNotMatch');
                            break;
                        case '6510'://未检测到驾驶员
                            dsmInfoObj.setInputDataCallBack(data[i], 'platform', 'checkIdent');
                            break;
                        case '6511'://遮挡
                            console.log('6511', data[i])
                            dsmInfoObj.setInputDataCallBack(data[i], 'platform', 'cover');
                            break;
                        case '6512'://遮挡
                            console.log('6512', data[i])
                            dsmInfoObj.setInputDataCallBack(data[i], 'platform', 'infrared');
                            break;
                    }
                }
            }
        },
        setInputDataCallBack: function (data, type, target) {
            for (key in data) {
                var preId = "#" + type + "-";
                if (target) {
                    var $newId = $(preId + target + key.substr(0, 1).toUpperCase() + key.substr(1));//组装目标字段id
                }
                switch (key) {
                    case 'cameraResolution'://拍照分辨率
                        var keyVal = dsmInfoObj.formatChange(data[key]);
                        $(preId + key).val(dsmInfoObj.changeCameraResolution(keyVal)).attr('data-val', keyVal);
                        break;
                    case 'videoResolution'://视频录制分辨率
                        var keyVal = dsmInfoObj.formatChange(data[key]);
                        $(preId + key).val(dsmInfoObj.changeVideoResolution(keyVal)).attr('data-val', keyVal);
                        break;
                    case 'eventVoiceEnable'://语音提醒
                        dsmInfoObj.voiceChange(data[key], key);
                        break;
                    case 'cameraStrategy'://拍照策略
                        dsmInfoObj.strategyChange(data[key], key);
                        break;
                    case 'timingCapture'://定时
                    case 'distanceCapture'://定距
                        $(preId + key).val(dsmInfoObj.onOrOff(data[key])).attr('data-val', data[key]);//全局参数
                        if ($newId) {
                            $newId.val(dsmInfoObj.onOrOff(data[key])).attr('data-val', data[key]);
                        }
                        break;
                    case 'eventEnable'://主动抓拍,人证不符,未检测到驾驶员
                        dsmInfoObj.eventEnableChange(data[key], key);
                        break;
                    case 'cameraTime'://拍照间隔
                    case 'checkIdentCameraTime'://拍照间隔
                    case 'closeEyesCameraTime'://拍照间隔
                    case 'identCameraTime'://拍照间隔
                    case 'pickUpCameraTime'://拍照间隔
                    case 'postureCameraTime'://拍照间隔
                    case 'smokingCameraTime'://拍照间隔
                    case 'yawnCameraTime'://拍照间隔
                    case 'shutterCameraTime':
                        if ($newId) {
                            $newId.val(data[key] / 10);
                        }
                        $(preId + key).val(data[key] / 10);
                        break;
                    /*case 'lowSpeedLevel'://低速阈值
                    case 'highSpeedLevel'://高速阈值
                        $(preId + key).val(dsmInfoObj.changeLowThreshold(data[key])).attr('data-val', data[key]);
                        $newId.val(dsmInfoObj.changeLowThreshold(data[key])).attr('data-val', data[key]);
                        break;*/
                    case 'initiativeCaptureAlarmEnable':
                    case 'timingCapture':
                    case 'distanceCapture':
                    case 'initiativeCaptureVoiceEnable':
                    case 'oneLevelAlarmEnable':
                    case 'twoLevelAlarmEnable':
                    case 'oneLevelVoiceEnable':
                    case 'twoLevelVoiceEnable':
                    case 'voiceEnable':
                    case 'checkSwitch':
                        $(preId + key).val(dsmInfoObj.onOrOff(data[key])).attr('data-val', data[key]);
                        if ($newId) {
                            $newId.val(dsmInfoObj.onOrOff(data[key])).attr('data-val', data[key]);
                        }
                        if (type == 'sensor') {
                            if (key == 'oneLevelAlarmEnable' || key == 'twoLevelAlarmEnable' || key == 'oneLevelVoiceEnable' || key == 'twoLevelVoiceEnable') {
                                dsmInfoObj.alarmChange(data[key], key);
                            }
                        }
                        break;
                    default:
                        if ($newId) {
                            $newId.val(data[key]);
                        }
                        $(preId + key).val(data[key]);
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
        },
        //一级二级报警,语音报警提醒转换
        alarmChange: function (data, key) {
            var oneLevel = data.toString(2);//一级报警
            oneLevel = (Array(8).join(0) + oneLevel).slice(-8);//高位补零
            var answerThephone = oneLevel.substr(-1, 1);//接打电话
            var smoking = oneLevel.substr(-2, 1);//抽烟
            var closeEyes = oneLevel.substr(-3, 1);//闭眼
            var yawn = oneLevel.substr(-4, 1);//打哈欠
            var driveDeed = oneLevel.substr(-5, 1);//：姿态异常
            var cover = oneLevel.substr(-6, 1);//：姿态异常
            var infrared = oneLevel.substr(-7, 1);//：姿态异常

            var dataArr = [{
                'name': 'answerThephone',
                'data': answerThephone
            }, {
                'name': 'smoking',
                'data': smoking
            }, {
                'name': 'closeEyes',
                'data': closeEyes
            }, {
                'name': 'yawn',
                'data': yawn
            }, {
                'name': 'driveDeed',
                'data': driveDeed
            }, {
                'name': 'cover',
                'data': cover
            }, {
                'name': 'infrared',
                'data': infrared
            }];
            var len = dataArr.length;
            for (var i = 0; i < len; i++) {
                $("#sensor-" + dataArr[i].name + key.substr(0, 1).toUpperCase() + key.substr(1)).val(dsmInfoObj.onOrOff(dataArr[i].data)).attr('data-val', dataArr[i].data);
            }
        },
        //语音提醒转换
        voiceChange: function (data, key) {
            var eventVoice = data.toString(2);
            eventVoice = (Array(8).join(0) + eventVoice).slice(-8);//高位补零

            var snap = eventVoice.substr(-1, 1);//主动抓拍
            var driverNotMatch = eventVoice.substr(-2, 1);//人证不符
            var checkIdent = eventVoice.substr(-3, 1);//未检测到驾驶员

            var dataArr = [{
                'name': 'snap',
                'data': snap
            }, {
                'name': 'driverNotMatch',
                'data': driverNotMatch
            }, {
                'name': 'checkIdent',
                'data': checkIdent
            }];
            var len = dataArr.length;
            for (var i = 0; i < len; i++) {
                $("#sensor-" + dataArr[i].name + key.substr(0, 1).toUpperCase() + key.substr(1)).val(dsmInfoObj.onOrOff(dataArr[i].data)).attr('data-val', dataArr[i].data);
            }
        },
        //定时抓拍,定距抓拍转换
        strategyChange: function (data, key) {
            var strategy = data.toString(2);
            strategy = (Array(8).join(0) + strategy).slice(-8);//高位补零

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
                $("#sensor-" + dataArr[i].name).val(dsmInfoObj.onOrOff(dataArr[i].data)).attr('data-val', dataArr[i].data);
            }
        },
        //主动抓拍,人证不符,未检测到驾驶员转换
        eventEnableChange: function (data, key) {
            var eventEnable = data.toString(2);
            eventEnable = (Array(8).join(0) + eventEnable).slice(-8);//高位补零

            var snap = eventEnable.substr(-1, 1);//主动抓拍
            var driverNotMatch = eventEnable.substr(-2, 1);//人证不符
            var checkIdent = eventEnable.substr(-3, 1);//未检测到驾驶员

            var dataArr = [{
                'name': 'snap',
                'data': snap
            }, {
                'name': 'driverNotMatch',
                'data': driverNotMatch
            }, {
                'name': 'checkIdent',
                'data': checkIdent
            }];
            var len = dataArr.length;
            for (var i = 0; i < len; i++) {
                $("#sensor-" + dataArr[i].name + key.substr(0, 1).toUpperCase() + key.substr(1)).val(dsmInfoObj.onOrOff(dataArr[i].data)).attr('data-val', dataArr[i].data);
            }
        },
        //拍照分辨率转换
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
        },
        //视频录制分辨率转换
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
        },
        //低速阈值/高速阈值转换
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
        //开关状态切换
        onOrOff: function (data) {
            if (data == '1') {
                data = '开';
            } else {
                data = '关';
            }
            return data;
        },
        //开关状态切换(反向)
        onOrOffReverse: function (data) {
            if (data == '1') {
                data = '开';
            } else {
                data = '关';
            }
            return data;
        },
        //参数修正下发
        dsmInfoSendClick: function () {
            var jsonData = dsmInfoObj.setJsonData();
            var data = {"checkedParams": JSON.stringify(jsonData), "id": $("#vehicleId").val(), 'flag': false};
            json_ajax("POST", "/clbs/r/riskManagement/DefineSettings/settingFlag.gsp", "json", false, data, dsmInfoObj.dsmInfoSendCallback);
        },
        dsmInfoSendCallback: function (data) {
            if (data != null) {
                if (data.success) {
                    //下发dsm外设参数
                    dsmInfoObj.sendDsmParameter();
                    layer.load(2);
                } else {
                    if (data.msg) {
                        layer.msg(data.msg, {move: false});
                    }
                }
            }
        },
        //下发dsm外设参数
        sendDsmParameter: function () {
            var url = '/clbs/adas/paramSetting/sendDsmParameter';
            json_ajax("POST", url, "json", false, {'vehicleId': vid}, function (data) {
                if (data.success) {
                    if (data.obj) {
                        var msgid = data.obj.msgId;
                        var sendstatusname = "send_status_" + msgid;
                        $("#dsmPoilSendStatus").attr("data-id", sendstatusname).addClass(sendstatusname).val("参数已下发");
                        dsmInfoObj.createSocket0900StatusMonitor(data.obj);
                        setTimeout(function () {
                            timerInterval = setInterval(dsmInfoObj.getStatus(msgid), 5000);
                        }, 2000);
                        setTimeout(function () {
                            clearInterval(timerInterval);
                        }, 60000);
                    }
                }
            });
        },
        //获取参数下发状态
        getStatus: function (msgId) {
            url = '/clbs/r/riskManagement/DefineSettings/getStatus';
            json_ajax("POST", url, "json", false, {
                "vehicleId": vid,
                "swiftNumber": msgId,
                'flag': false
            }, function (data) {
                if (data.success) {
                    if (data.obj) {
                        var status = data.obj.status;
                        $("#dsmPoilSendStatus").val(dsmInfoObj.statusChange(status));
                        //传感器参数信息
                        dsmInfoObj.getPeripheralInfo();
                        if (status != 7) {
                            clearInterval(timerInterval);
                        }
                    }
                }
            });
        },
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
        },
        //组装下发参数
        setJsonData: function () {
            var dataArray = new Array();
            var jsonStr6506 = dsmInfoObj.setParam("6506", "closeEyes");//闭眼
            var jsonStr6507 = dsmInfoObj.setParam("6507", "yawn"); //打哈欠
            var jsonStr6503 = dsmInfoObj.setParam("6503", "smoking"); // 抽烟
            var jsonStr6502 = dsmInfoObj.setParam("6502", "answerThephone"); // 接打电话
            var jsonStr6509 = dsmInfoObj.setParam("6509", "driverNotMatch");//人证不符
            var jsonStr6510 = dsmInfoObj.setParam("6510", "checkIdent");//未检测到驾驶员
            var jsonStr6508 = dsmInfoObj.setParam("6508", "driveDeed");//姿态异常
            var jsonStr6511 = dsmInfoObj.setParam("6511", "cover");//未检测到驾驶员
            var jsonStr6512 = dsmInfoObj.setParam("6512", "infrared");//姿态异常
            dataArray.push(jsonStr6502, jsonStr6503, jsonStr6506, jsonStr6507, jsonStr6508, jsonStr6509, jsonStr6510, jsonStr6511, jsonStr6512);
            return dataArray;
        },
        setParam: function (riskId, targetName) {
            var param = {};
            param.riskId = riskId;
            var allInput = $('#' + targetName + '-content').find('input');
            dsmInfoObj.getInputData(param, allInput, targetName);
            //全局参数
            var globalInput = $("#globalParameters-content").find('input');
            dsmInfoObj.getInputData(param, globalInput);
            //主动抓拍
            var snapInput = $("#snap-content").find('input');
            dsmInfoObj.getInputData(param, snapInput);
            return param;
        },
        getInputData: function (param, data, targetName) {
            var len = data.length;
            for (var i = 0; i < len; i++) {
                var curId = $(data[i]).attr('id');
                if (curId.indexOf('platform') != '-1') {
                    if (targetName) {
                        var keyName = curId.replace('platform-' + targetName, '').toLowerCase();
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
        dsmInfoObj.init();
        //为平台设置参数字段赋值
        dsmInfoObj.getPlatformData();

        $(".info-span").on("click", dsmInfoObj.hiddenparameterFn);//点击显示隐藏信息
        $("#dsmInfoSend").on("click", dsmInfoObj.dsmInfoSendClick);//修正下发
        $("#dsmInfoRefresh").on("click", dsmInfoObj.readConventionalRefreshClick);//刷新
        $(".sensorBtn").on("click", dsmInfoObj.sensorBtnClick);//以传感器为准
        $(".platformBtn").on("click", dsmInfoObj.platformBtnClick);//以平台设置为准
    })
})($, window)