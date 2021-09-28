//# sourceURL=zhongWeiParamInfo.js
(function ($, window) {
    var vid = $("#vehicleId").val();//车辆id
    var _timeout;
    var isRead = true;
    var timerInterval;

    var isAll = false;
    var paramType = 64,
        protocolType = 21;//中位
    //获取参数设置信息
    var sensorIDs = [225, 226, 228, 227];//[前向监测, 驾驶员监测, 盲区, 胎压]
    var socketTypes = [61665, 61666, 61668, 61667];//[前向监测, 驾驶员监测, 盲区, 胎压]
    //参数下发是的风险事件id
    var arr0 = ['', '', 216401, 216402, 216403, 216404, 216405, 216408, 216409, 216406],//前向事件id,''表示公共参数
        arr1 = ['', '', 216501, 216504, 216503, 216502, 216508, 216516, 2165104, 216518, 216515, 216517],//驾驶员事件id
        arr3 = [216704, ''],//盲区
        arr4 = [216608, ''];//胎压
    var paramTypes = [64, 65, 67, 66];//[前向监测, 驾驶员监测, 盲区, 胎压]
    var pressureUnit = [1 / 0.010307, 1 / 0.068947, 6.894757, 1];

    // 需进行10倍转换的字段(提交*10,渲染/10)
    var conversionTen = ['rear', 'timeDistanceThreshold', 'leftDistance', 'rightDistance',
        'pressure', 'lowPressure', 'highPressure', 'highTemperature'];
    // 需进行100倍转换的字段(提交*100,渲染/100)
    var conversionOneHundred = ['compensationFactorK', 'compensationFactorB'];
    // 需进行开关状态转换的字段
    var conversionSwitch = ['oneLevelAlarmEnable', 'oneLevelVoiceReminder', 'oneLevelAuxiliaryMultimedia',
        'twoLevelAlarmEnable', 'twoLevelVoiceReminder', 'twoLevelAuxiliaryMultimedia', 'roadSignRecognition',
        'oneLevelVoiceReminder', 'oneLevelAlarmEnable'];

    zhongWeiParamInfo = {
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

                //关闭弹窗的时候清除定时器
                isRead = false;
                clearTimeout(_timeout);
                layer.closeAll();


            };

            for (var i = 0; i < socketTypes.length; i++) {
                var socketType = socketTypes[i];
                var sensorID = sensorIDs[i];

                zhongWeiParamInfo.subscribeSocket(socketType);//socket订阅
                zhongWeiParamInfo.getPeripheralInfo(sensorID);//传感器参数下发
            }
        },
        /**
         * socket订阅
         * @param type
         */
        subscribeSocket: function (type) {
            webSocket.subscribe(headers, "/user/topic/per" + type + "Info", function (data) {
                zhongWeiParamInfo.getSensor0104Param(data, type);
            }, null, null);
        },
        /**
         * 传感器参数指令下发
         * @param sensorID
         */
        getPeripheralInfo: function (sensorID) {
            //清除上一个定时器
            if (_timeout) {
                clearTimeout(_timeout);
            }
            layer.closeAll();
            $("#dsmInfoRefresh").html("刷新").prop('disabled', false);
            var url = '/clbs/r/riskManagement/DefineSettings/getPeripheralInfo';
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "sensorID": sensorID,
                "commandType": 240
            }, zhongWeiParamInfo.setreadConventionalCall);
        },
        setreadConventionalCall: function (data) {
            if (data.success) {
                if (isRead) {
                    $("#dsmInfoRefresh").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled', true);
                }
                zhongWeiParamInfo.createSocket0104InfoMonitor(data.msg);
            } else {
                if (data.msg) {
                    layer.msg(data.msg);
                }
            }
        },
        createSocket0104InfoMonitor: function (msg) {
            var msg = $.parseJSON(msg);
            temp_send_vehicle_msg_id = msg.msgId;
            headers = {"UserName": msg.userName};

            clearTimeout(_timeout);
            _timeout = setTimeout(function () {
                if (isRead) {
                    isRead = false;
                    $("#dsmInfoRefresh").html("刷新").prop('disabled', false);
                    layer.closeAll();
                    layer.msg("获取设备数据失败!");
                }
            }, 60000);
        },
        /**
         * 获取设备上传数据
         * @param msg
         * @param type: 当前tab标签
         */
        getSensor0104Param: function (msg, type) {
            if (msg == null)
                return;
            var result = $.parseJSON(msg.body);

            isRead = false;
            clearTimeout(_timeout);
            layer.closeAll();
            console.log('上报数据', result);
            $("#dsmInfoRefresh").html("刷新").prop('disabled', false);
            var param = result.data.msgBody.params[0].value;
            // console.log('传感器参数', param);
            zhongWeiParamInfo.setSenorInputData(param, type);
            $("#dsmInfoRefresh").removeAttr("disabled");
            $("#dsmInfoSend").removeAttr("disabled");
            $("#dsmInfoSend2").removeAttr("disabled");
            $(".sensorBtn").removeAttr("disabled");
            $(".platformBtn").removeAttr("disabled");
        },
        setSenorInputData: function (result, type) {
            var dataTypeMap = {
                61665: result.assist,//前向
                61666: result.surveyInfo,//驾驶员
                61668: result.deadZoneDetection,//盲区
                61667: result.tyreParam,//胎压
            };
            var tabId = '#system' + type;
            var data = dataTypeMap[type];

            for (var key in data) {
                var value = data[key];

                var dom = $(tabId + ' .sensor-' + key);
                dom.attr('data-value', value);

                switch (key) {
                    case 'driveDeedType':
                    case 'cameraStrategy':
                        value = zhongWeiParamInfo.getCaptureType(value);
                        dom.val(value);
                        break;
                    case 'eventEnable':// 报警使能
                    case 'eventVoiceEnable':// 语音提醒
                    case 'oneLevelAlarmEnable':// 一级报警
                    case 'oneLevelVoiceEnable':// 一级语音提醒
                    case 'multimediaStrategy':// 辅助多媒体
                    case 'twoLevelAlarmEnable':// 二级报警
                    case 'twoLevelVoiceEnable':// 二级语音提醒
                        zhongWeiParamInfo.getBinaryValue(value, tabId, key);
                        break;
                    case 'cameraResolution':
                        var dataValue = '0x0' + parseInt(value, 10).toString(16);
                        value = zhongWeiParamInfo.changeCameraResolution(value);
                        dom.val(value).attr('data-value', dataValue);
                        break;
                    case 'videoResolution':
                        var dataValue = '0x0' + parseInt(value, 10).toString(16);
                        value = zhongWeiParamInfo.changeVideoResolution(value);
                        dom.val(value).attr('data-value', dataValue);
                        break;
                    case 'compensationFactorK':// 输出修正系数K值
                    case 'compensationFactorB':// 输出修正系数B值
                        value = value / 100;
                        dom.val(value);
                        break;
                    case 'rear'://后方距离阈值
                    case 'left'://左后方距离阈值
                    case 'right'://右后方距离阈值
                    case 'distanceMail'://距离阈值
                    case 'vehicleCollisionTime'://时距阈值
                    case 'distanceMail'://距离阈值
                    case 'pressure':
                    case 'heighPressure':
                    case 'lowPressure':
                    case 'highTemperature':
                        value = value / 10;
                        dom.val(value);
                        break;
                    case 'tyreNumber':
                        dom.val(value).attr('data-value', zhongWeiParamInfo.tireModel(value));
                        break;
                    default:
                        dom.val(value);
                        break;
                }
            }
        },
        tireModel: function (value) {
            var tireModelMap = JSON.parse($("#tireModelMap").val());
            var resultArr = [];
            for (var item in tireModelMap) {
                var objTmp = {};
                objTmp.name = item;
                objTmp.value = tireModelMap[item];
                resultArr.push(objTmp);
            }
            for (var i = 0; i < resultArr.length; i++) {
                if (value == resultArr[i].name) {
                    return resultArr[i].value
                }
            }
            if (resultArr.indexOf(value) == -1) {
                return '-1';
            }
        },
        getBinaryValue: function (data, tabId, key) {//报警使能(二进制)
            var alarm = data.toString(2);
            alarm = (Array(32).join(0) + alarm).slice(-32).split('').reverse();

            for (var i = 0; i < alarm.length; i++) {
                var dom = $(tabId + ' .sensor-' + key + i);
                var value = alarm[i] == 0 ? '关闭' : '打开';
                dom.val(value).attr('data-value', alarm[i]);
            }
        },
        changeCameraResolution: function (data) {//拍照分辨率转换
            switch (data) {
                case 0:
                    return '最低分辨率';
                    break;
                case 1:
                    return '352×288';
                    break;
                case 2:
                    return '704×288';
                    break;
                case 3:
                    return '704×576';
                    break;
                case 4:
                    return '640×480';
                    break;
                case 5:
                    return '1280×720';
                    break;
                case 6:
                    return '1920×1080';
                    break;
                default:
                    return '最高分辨率';
                    break;
            }
        },
        changeVideoResolution: function (data) {//视频录制分辨率转换
            switch (data) {
                case 0:
                    data = '最低分辨率';
                    break;
                case 1:
                    data = 'CIF';
                    break;
                case 2:
                    data = 'WCIF';
                    break;
                case 3:
                    data = 'D1';
                    break;
                case 4:
                    data = 'WD1';
                    break;
                case 6:
                    data = '720P';
                    break;
                case 7:
                    data = '1080P';
                    break;
                default:
                    data = '最高分辨率';
            }
            return data;
        },
        /**
         * 获取平台设置参数
         */
        getPlatformData: function () {
            var platformData = JSON.parse($('#platformData').val());
            console.log('平台参数', platformData);
            for (var i = 0; i < platformData.length; i++) {
                var item = platformData[i];
                var type = item.commonParamSetting.paramType;
                var tab = $('.tab-pane[data-type=' + type + ']');

                if (i < 2) {//有公共参数的页签
                    zhongWeiParamInfo.getCommonParamSetting(item.commonParamSetting, tab);
                }
                zhongWeiParamInfo.getAdasAlarmParamSettings(item.adasAlarmParamSettings, tab);
            }
        },
        //全局参数
        getCommonParamSetting: function (data, tab) {
            for (var key in data) {
                var value = data[key] === '0x00' ? '' : data[key],
                    dataValue = data[key];
                var dom = tab.find('.commonParamSetting .platform-' + key);

                switch (key) {
                    case 'touchStatus'://报警
                        value = zhongWeiParamInfo.getCaptureType(value)
                        break;
                    case 'cameraResolution'://拍照分辨率
                        var num = Number(parseInt(value, 16).toString(10));
                        value = zhongWeiParamInfo.changeCameraResolution(num);
                        break;
                    case 'videoResolution'://视频分辨率
                        var num = Number(parseInt(value, 16).toString(10));
                        value = zhongWeiParamInfo.changeVideoResolution(num);
                        break;
                    case 'photographTime':
                        value = value / 10;
                        break;
                    default:
                        break;
                }
                if (dom) {
                    value = value == -1 ? '' : value;
                    dom.val(value).attr('data-value', dataValue);
                }
            }

        },
        //报警事件参数
        getAdasAlarmParamSettings: function (data, tab) {
            for (var i = 0; i < data.length; i++) {
                var item = data[i];
                var riskId = item.riskFunctionId;

                var riskEvent = tab.find('.dsm-content .info-content');//当前页签

                switch (riskId) {
                    case 216704://盲区监测
                    case 216608://胎压监测
                        zhongWeiParamInfo.setInputValue(item, riskEvent.eq(0));
                        break;
                    case 216401://前向碰撞
                    case 216501://闭眼
                        zhongWeiParamInfo.setInputValue(item, riskEvent.eq(1));
                        break;
                    case 2164021://车道左偏离
                    case 2164022://车道右偏离
                    case 216504://打哈欠
                        zhongWeiParamInfo.setInputValue(item, riskEvent.eq(2));
                        break;
                    case 216403://车距过近
                    case 216503://抽烟
                        zhongWeiParamInfo.setInputValue(item, riskEvent.eq(3));
                        break;
                    case 216404://行人碰撞
                    case 216502://接打手持电话
                        zhongWeiParamInfo.setInputValue(item, riskEvent.eq(4));
                        break;
                    case 216405://频繁变道
                    case 216508://长时间不目视前方
                        zhongWeiParamInfo.setInputValue(item, riskEvent.eq(5));
                        break;
                    case 2164081://急加速
                    case 2164082://急减速
                    case 2164083://急转弯
                    case 216516://驾驶员不在驾驶位置
                        zhongWeiParamInfo.setInputValue(item, riskEvent.eq(6));
                        break;
                    case 2164091://道路标识超限
                    case 2164092://道路标识超限
                    case 216510://遮挡
                    case 216514://红外阻断
                        zhongWeiParamInfo.setInputValue(item, riskEvent.eq(7));
                        break;
                    case 216406://外设状态异常(前向)
                    case 216518://双手同时脱离方向盘
                        zhongWeiParamInfo.setInputValue(item, riskEvent.eq(8));
                        break;
                    case 216515://人证不符
                        zhongWeiParamInfo.setInputValue(item, riskEvent.eq(9));
                        break;
                    case 216517://外设状态异常(驾驶员)
                        zhongWeiParamInfo.setInputValue(item, riskEvent.eq(10));
                        break;
                    default:
                        break;
                }
            }
        },
        setInputValue: function (data, riskEvent) {
            var riskFunctionId = data.riskFunctionId;
            for (var key in data) {
                var value = data[key] === "0x00" ? '' : data[key],
                    dataValue = data[key];
                var dom = riskEvent.find('.platform-' + key);

                switch (key) {
                    /*case 'oneLevelAlarmEnable':
                    case 'twoLevelAlarmEnable':
                    case 'roadSignRecognition':
                    case 'roadSignEnable':
                    case 'alarmEnable'://报警
                        value = value == 1 ? '打开' : '关闭';
                        break;*/
                    case 'touchStatus'://触发状态
                        value = zhongWeiParamInfo.getCaptureType(value);
                        break;
                    /*  case 'photographTime':
                      case 'timeDistanceThreshold'://时距单位转换
                          value = value / 10;
                          break;
                      case 'unit':
                          value = 'psi';
                          break;
                      case "tyreNumber":
                          value = data['tyreNumberName'];
                          break;*/
                    case "compensatingEnable":// 补偿使能
                        value = value === '1' ? '使用' : '禁用';
                        break;
                    case "smoothing":// 滤波系数
                        if (value === 0) {
                            value = '实时';
                        } else if (value === 1) {
                            value = '平滑';
                        } else if (value === 2) {
                            value = '平稳';
                        }
                        break;
                    case "automaticUploadTime":// 自动上传时间
                        if (value === 0) {
                            value = '被动';
                        }
                        break;
                    default:
                        break;
                }
                if (conversionSwitch.indexOf(key) > -1) {
                    value = value == 1 ? '打开' : '关闭';
                }
                if (conversionTen.indexOf(key) > -1) {
                    value = value / 10;
                }
                if (conversionOneHundred.indexOf(key) > -1) {
                    value = value / 100;
                }

                if (dom) {
                    value = value == -1 ? '' : value;
                    if (riskFunctionId == '2164021' || riskFunctionId == '2164091' || riskFunctionId == '216510') {
                        dom = dom.eq(0);
                    } else if (riskFunctionId == '2164022' || riskFunctionId == '2164092' || riskFunctionId == '216514') {
                        if (dom.eq(1).length > 0) {
                            dom = dom.eq(1);
                        }
                    }

                    dom.val(value).attr('data-value', dataValue);
                }
            }
        },
        //主动拍照触发状态
        getCaptureType: function (type) {
            switch (type.toString()) {
                case '0':
                case '0x00':
                    return '不开启';
                    break;
                case '1':
                case '0x01':
                    return '定时拍照';
                    break;
                case '2':
                case '0x02':
                    return '定距拍照';
                    break;
                case '3':
                case '0x03':
                    return '插卡触发';
                    break;
                default:
                    break;
            }
        },
        /**
         * 参数修正下发
         */
        preventclick: function (self) {
            self.prop('disabled', true);
            timer = setTimeout(function () {
                self.prop('disabled', false);
            }, 5000);
        },
        paramSendAllClick: function () {
            var self = $(this);
            zhongWeiParamInfo.preventclick(self);
            isAll = true;
            var jsonDataArr = [];
            var riskIds = [arr0, arr1, arr3, arr4];//前向,驾驶员.激烈驾驶,盲区,胎压风险事件id

            for (var i = 0; i < riskIds.length; i++) {
                paramType = paramTypes[i];
                var jsonData = zhongWeiParamInfo.setJsonData(riskIds[i], $('.tab-pane').eq(i));
                jsonDataArr.push(jsonData[0]);
            }
            // console.log('全部下发参数', jsonDataArr);
            zhongWeiParamInfo.dsmInfoSend(jsonDataArr);
        },
        paramSendClick: function () {
            var self = $(this);
            zhongWeiParamInfo.preventclick(self);
            isAll = false;
            var inx = $('.tab-pane.active').index();
            var tab = $('.tab-pane').eq(inx);

            var riskIds = [arr0, arr1, arr3, arr4];
            var arr = riskIds[inx];
            paramType = tab.data('type');

            var jsonData = zhongWeiParamInfo.setJsonData(arr, tab);
            // console.log('本页签下发参数', jsonData);
            zhongWeiParamInfo.dsmInfoSend(jsonData);
        },
        //组装下发参数
        setJsonData: function (arr, tab) {
            var objArr = [];
            var adasAlarmParamSettings = [],
                commonParamSetting = '';

            for (var i = 0; i < arr.length; i++) {
                var item = arr[i];
                var inputs;

                if (item == '') {//全局参数/主动抓拍策略
                    inputs = tab.find('.commonParamSetting').find('input');
                    commonParamSetting = zhongWeiParamInfo.getCommontData(inputs);
                } else {//风险事件
                    inputs = tab.find('.info-content').eq(i).find('input');
                    switch (item) {
                        case 216402:// 车道偏离
                            adasAlarmParamSettings.push(zhongWeiParamInfo.getInputData('2164021', inputs));
                            adasAlarmParamSettings.push(zhongWeiParamInfo.getInputData('2164022', inputs));
                            break;
                        case 216408:// 加减速、转弯
                            adasAlarmParamSettings.push(zhongWeiParamInfo.getInputData('2164081', inputs));
                            adasAlarmParamSettings.push(zhongWeiParamInfo.getInputData('2164082', inputs));
                            adasAlarmParamSettings.push(zhongWeiParamInfo.getInputData('2164083', inputs));
                            break;
                        case 216409:// 道路标识超限
                            adasAlarmParamSettings.push(zhongWeiParamInfo.getInputData('2164091', inputs));
                            adasAlarmParamSettings.push(zhongWeiParamInfo.getInputData('2164092', inputs));
                            break;
                        case 2165104:// 红外、遮挡
                            adasAlarmParamSettings.push(zhongWeiParamInfo.getInputData('216510', inputs));
                            adasAlarmParamSettings.push(zhongWeiParamInfo.getInputData('216514', inputs));
                            break;
                        default:
                            adasAlarmParamSettings.push(zhongWeiParamInfo.getInputData(item, inputs));
                            break;
                    }
                }
            }

            var obj = {
                'adasAlarmParamSettings': adasAlarmParamSettings,
                'commonParamSetting': commonParamSetting
            };

            objArr.push(obj);
            return objArr;
        },
        getInputData: function (riskFunctionId, inputs) {
            var obj = {};

            if (riskFunctionId !== '') {
                obj.riskFunctionId = riskFunctionId;
            }

            for (var i = 0; i < inputs.length; i++) {
                var input = inputs[i];
                var curClass = $(input).attr('class');

                if (curClass.indexOf('platform-') != -1) {
                    curClass = $(input).attr('class').split('platform-');
                    var key = curClass[1];
                    if ($(input).length > 1) {// 多个报警公用表单,组装数据时需进行区分
                        if (riskFunctionId == '2164021' || riskFunctionId == '2164081' || riskFunctionId == '2164091' || riskFunctionId == '216510') {
                            obj[key] = $(input).eq(0).data('value');
                        }
                        if (riskFunctionId == '2164022' || riskFunctionId == '2164082' || riskFunctionId == '2164092' || riskFunctionId == '216514') {
                            obj[key] = $(input).eq(1).data('value');
                        }
                        if (riskFunctionId == '2164083') {
                            obj[key] = $(input).eq(2).data('value');
                        }
                    } else {
                        obj[key] = $(input).data('value');
                    }
                }
            }

            return obj;
        },
        getCommontData: function (inputs) {
            var obj = {
                "vehicleId": vid,
                "protocolType": protocolType,
                "paramType": paramType,
            };

            if (inputs) {
                for (var i = 0; i < inputs.length; i++) {
                    var input = inputs[i];
                    var curClass = $(input).attr('class');

                    if (curClass.indexOf('platform-') != -1) {
                        curClass = $(input).attr('class').split('platform-');
                        var key = curClass[1];
                        obj[key] = $(input).data('value');
                    }
                }
            }

            return obj;
        },
        //下发
        dsmInfoSend: function (jsonData) {
            var data = {
                "alarmParam": JSON.stringify(jsonData),
                "vehicleIds": vid,
                "sendFlag": true
            };
            json_ajax("POST", "/clbs/adas/standard/param/setting.gsp", "json", false, data, zhongWeiParamInfo.dsmInfoSendCallback);
        },
        dsmInfoSendCallback: function (data) {
            if (data != null) {
                if (data.success) {
                    layer.load(2);
                    var tab = $('.tab-pane.active').index();
                    $("#dsmPoilSendStatus" + tab).val("参数已下发");

                    //获取下发状态
                    setTimeout(function () {
                        timerInterval = setInterval(zhongWeiParamInfo.getStatus(), 5000);
                    }, 2000);
                    setTimeout(function () {
                        clearInterval(timerInterval);
                    }, 60000);
                } else {
                    if (data.msg) {
                        layer.msg(data.msg, {move: false});
                    }
                }
            }
        },
        //获取参数下发状态
        getStatus: function () {
            var url = '/clbs/adas/standard/param/getStatus';

            json_ajax("POST", url, "json", false, {
                "vehicleId": vid,
                "protocolType": protocolType,
                'paramTypes': isAll ? paramTypes.toString() : paramType
            }, function (data) {
                if (data.success) {
                    layer.msg('下发成功', {move: false});
                    myTable.requestData();
                    if (isAll) {//全部参数下发
                        $('#commonWin').modal('hide');
                        return;
                    }

                    var obj = data.obj;
                    if (obj) {
                        var status = [];
                        var timer = null;
                        clearTimeout(timer);

                        for (var key in obj) {
                            var value = obj[key];
                            status.push(zhongWeiParamInfo.statusChange(value));
                        }

                        var tab = $('.tab-pane.active').index();
                        $("#dsmPoilSendStatus" + tab).val(status[0]);

                        //下发后再次获取传感器参数信息
                        timer = setTimeout(function () {
                            var sensorID = sensorIDs[tab];
                            zhongWeiParamInfo.getPeripheralInfo(sensorID);
                        }, 1000);
                    }

                    if (status != 7) {
                        clearInterval(timerInterval);
                    }
                } else {
                    layer.msg(data.msg);
                }
            });
        },
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
        //传感器常规参数-获取信息-刷新
        readConventionalRefreshClick: function () {
            isRead = true;
            $("#dsmPoilSendStatus").val("").removeAttr("data-id");//下发状态
            var vid = $("#vehicleId").val();
            $("#dsmInfoSend").attr("disabled", "disabled");
            $(".sensorBtn").attr("disabled", "disabled");
            $(".platformBtn").attr("disabled", "disabled");

            //为平台设置参数字段赋值
            zhongWeiParamInfo.getPlatformData();

            //传感器参数
            for (var i = 0; i < socketTypes.length; i++) {
                var sensorID = sensorIDs[i];
                zhongWeiParamInfo.getPeripheralInfo(sensorID);//传感器参数下发
            }
        },
        //传感器常规参数对比后赋值(以传感器为准)
        sensorBtnClick: function () {
            var allInput = $(this).parents('.info-content').find('input');
            var len = allInput.length;
            for (var i = 0; i < len; i++) {
                var curClass = $(allInput[i]).attr('class');

                if (curClass.indexOf('sensor') != '-1') {
                    var targetInput = $(allInput[i]).parent().siblings().find('input');
                    if (targetInput.attr('class').indexOf('platform') != '-1') {
                        targetInput.val($(allInput[i]).val());
                        if ($(allInput[i]).attr('data-value')) {
                            if (curClass.indexOf('sensor-fixedCamera') != -1) {
                                targetInput.attr('data-value', $(allInput[i]).attr('data-value') / 1000);
                            } else {
                                targetInput.attr('data-value', $(allInput[i]).attr('data-value'));
                            }
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
                var curId = $(allInput[i]).attr('class');

                if (curId.indexOf('platform') != '-1') {
                    var targetInput = $(allInput[i]).parent().siblings().find('input');
                    if (targetInput.attr('class').indexOf('sensor') != '-1') {
                        targetInput.val($(allInput[i]).val());
                        if ($(allInput[i]).attr('data-value')) {
                            targetInput.attr('data-value', $(allInput[i]).attr('data-value'));
                        }
                    }
                }
            }
        },
        //信息隐藏显示
        hiddenparameterFn: function () {
            var self = $(this);
            var content = $(this).parents('.form-group').next('.info-content');
            if (!content.is(":hidden")) {
                content.slideUp();
                self.children("font").text("显示更多");
                self.children("span").removeAttr("class").addClass("fa fa-chevron-down");
            } else {
                content.slideDown();
                self.children("font").text("隐藏信息");
                self.children("span").removeAttr("class").addClass("fa fa-chevron-up");
            }
        },
    };
    $(function () {
        $('input').inputClear();
        zhongWeiParamInfo.init();
        zhongWeiParamInfo.getPlatformData();

        //事件
        $(".info-span").on("click", zhongWeiParamInfo.hiddenparameterFn);//点击显示隐藏信息
        $("#dsmInfoSend").on("click", zhongWeiParamInfo.paramSendAllClick);//全部修正下发
        $("#dsmInfoSend2").on("click", zhongWeiParamInfo.paramSendClick);//本页签修正下发
        $("#dsmInfoRefresh").on("click", zhongWeiParamInfo.readConventionalRefreshClick);//刷新
        $(".sensorBtn").on("click", zhongWeiParamInfo.sensorBtnClick);//以传感器为准
        $(".platformBtn").on("click", zhongWeiParamInfo.platformBtnClick);//以平台设置为准
    })
})($, window)