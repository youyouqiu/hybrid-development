//# sourceURL=guiparamInfo.js
var guiparamInfo;
(function ($, window) {
    var vid = $("#vehicleId").val();//车辆id
    var _timeout;
    var isRead = true;
    var timerInterval;

    var paramType = 64,
        protocolType = 14;//桂标
    var arr0 = ['', '', 146401, 146402, 146403, 146404, 146405, 146407, 146409],//前向事件id
        arr1 = ['', '', 146513, 146503, 146502, 146508, 146510, 146515],//驾驶员事件id
        arr2 = [1464081, 1464082, 1464083, 147001, 147002, 147003, 147004, ''],//激烈驾驶事件id
        arr3 = [146705, ''], //盲区
        arr4 = [146608, '']; //胎压
    var paramTypes = [64, 65, 70, 67, 66];
    var isAll = false;
    var sensorIDs = [100, 101, 112, 103, 102]; //[前向监测, 驾驶员监测, 激烈驾驶监测, 盲区, 胎压]
    var paessureUnit = [1/0.010307, 1/0.068947, 6.894757, 1];

    guiparamInfo = {
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

            //socket订阅

            guiparamInfo.subscribeSocket(62308);//前向监测
            guiparamInfo.subscribeSocket(62309);//驾驶员监测
            guiparamInfo.subscribeSocket(62320);//激烈驾驶监测
            guiparamInfo.subscribeSocket(62310);//胎压
            guiparamInfo.subscribeSocket(62311);//盲区


            //传感器参数下发
            guiparamInfo.getPeripheralInfo(100);//前向监测
            guiparamInfo.getPeripheralInfo(101);//驾驶员监测
            guiparamInfo.getPeripheralInfo(112);//激烈驾驶
            guiparamInfo.getPeripheralInfo(102);//胎压
            guiparamInfo.getPeripheralInfo(103);//盲区
        },
        /**
         * socket订阅
         * @param type
         */
        subscribeSocket: function (type) {
            webSocket.subscribe(headers, "/user/topic/per" + type + "Info", function (data) {
                guiparamInfo.getSensor0104Param(data, type);
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
                "commandType": 243
            }, guiparamInfo.setreadConventionalCall);
        },
        setreadConventionalCall: function (data) {
            if (data.success) {
                if (isRead) {
                    $("#dsmInfoRefresh").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled', true);
                }
                guiparamInfo.createSocket0104InfoMonitor(data.msg);
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
            $("#dsmInfoRefresh").html("刷新").prop('disabled', false);
            var param = result.data.msgBody.params[0].value;
            console.log('设备上传数据', param);
            guiparamInfo.setSenorInputData(param, type);
            $("#dsmInfoRefresh").removeAttr("disabled");
            $("#dsmInfoSend").removeAttr("disabled");
            $("#dsmInfoSend2").removeAttr("disabled");
            $(".sensorBtn").removeAttr("disabled");
            $(".platformBtn").removeAttr("disabled");
        },
        setSenorInputData: function (result, type) {
            var data = '';
            var tabId = '';

            if (type == 62308) {//前向
                data = result.vehicleWarning;
                tabId = '#forwardSystem';
            } else if (type == 62309) {//驾驶员
                data = result.driverVehicleWarning;
                tabId = '#driverSystem';
            } else if (type == 62320) {//激烈驾驶
                data = result.intenseDriver;
                tabId = '#drivingIntense';
            } else if (type == 62311) {//盲区
                data = result.deadZoneDetection;
                tabId = '#BlindSpotSystem';
            } else {
                data = result.tyre1;
                tabId = '#TirePressureSystem';//胎压
            }

            for (var key in data) {
                var value = data[key];

                var dom = $(tabId + ' .sensor-' + key);
                dom.attr('data-value', value);

                switch (key) {
                    case 'driveDeedType':
                    case 'cameraStrategy':
                        value = guiparamInfo.getCaptureType(value);
                        dom.val(value);
                        break;
                    case 'eventEnable':
                    case 'alarmEnable':
                        guiparamInfo.getBinaryValue(value, tabId, key);
                        break;
                    case 'cameraResolution':
                        var dataValue = '0x0' + parseInt(value, 10).toString(16);
                        value = guiparamInfo.changeCameraResolution(value);
                        dom.val(value).attr('data-value', dataValue);
                        break;
                    case 'videoResolution':
                        var dataValue = '0x0' + parseInt(value, 10).toString(16);
                        value = guiparamInfo.changeVideoResolution(value);
                        dom.val(value).attr('data-value', dataValue);
                        break;
                    case 'fixedCamera':
                        value = value / 1000;
                        dom.val(value);
                        break;
                    case 'cameraTime'://主动抓拍策略时间间隔
                    case 'vehicleCollisionCameraTime'://拍照间隔
                    case 'deviateCameraTime':
                    case 'distanceCameraTime':
                    case 'pedestrianCollisionCameraTime':
                    case 'obstacleCameraTime':
                    case 'speedLimitCameraTime':
                    case 'fatigueCameraTime':
                    case 'smokingCameraTime':
                    case 'pickUpCameraTime':
                    case 'attentionCameraTime':
                    case 'driveDeedCameraTime':
                    case 'laneChangeCameraTime':
                    case 'vehicleCollisionTime'://时距
                    case 'obstacleDistance':
                    case 'distanceMail':
                    case 'pedestrianCollisionTime'://时距单位转换
                        value = value / 10;
                        dom.val(value);
                        break;
                    case 'unit':
                        value = 'psi';
                        dom.val(value);
                        break;
                    case 'pressure':
                    case 'lowPressure':
                    case 'heighPressure':
                        var inx = data['unit'];
                        value = Math.round(value * paessureUnit[inx]);
                        // dom.attr('data-value',value);
                        dom.val(value);
                        break;
                    case 'tyreNumber':
                        dom.val(value).attr('data-value',guiparamInfo.tireModel(value));
                        break;
                    /*case 'eventEnable':
                        guiparamInfo.getBinaryValue(value, tabId, key);
                        break;
                    case 'driveDeedType':
                        value = guiparamInfo.getCaptureType(value);
                        dom.val(value);
                        break;*/
                    default:
                        dom.val(value);
                        break;
                }
            }
        },
        tireModel:function (value){
            var tireModelMap = JSON.parse($("#tireModelMap").val());
            var resultArr = [];
            for(var item in tireModelMap){
                var objTmp = {};
                objTmp.name = item;
                objTmp.value = tireModelMap[item];
                resultArr.push(objTmp);
            }
            for(var i =0; i<resultArr.length; i++){
                if(value == resultArr[i].name){
                    return resultArr[i].value
                }
            }
            if(resultArr.indexOf(value) == -1){
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
                    break;
            }
        },
        changeVideoResolution: function (data) {//视频录制分辨率转换
            switch (data) {
                case 1:
                    data = 'CIF';
                    break;
                case 2:
                    data = 'HD1';
                    break;
                case 3:
                    data = 'D1';
                    break;
                case 4:
                    data = 'WD1';
                    break;
                case 5:
                    data = 'VGA';
                    break;
                case 6:
                    data = '720P';
                    break;
                case 7:
                    data = '1080P';
                    break;
                default:
                    data = '';
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
                var tab = $('.tab-pane[data-type='+type+']');

                if (i < 2) {//激烈驾驶没有公共参数
                    guiparamInfo.getCommonParamSetting(item.commonParamSetting, tab);
                }
                guiparamInfo.getAdasAlarmParamSettings(item.adasAlarmParamSettings, tab);
            }
        },
        //全局参数
        getCommonParamSetting: function (data, tab) {
            for (var key in data) {
                var value = data[key],
                    dataValue = value;
                var dom = tab.find('.commonParamSetting .platform-' + key);

                switch (key) {
                    case 'touchStatus'://报警
                        value = guiparamInfo.getCaptureType(value)
                        break;
                    case 'cameraResolution'://拍照分辨率
                        var num = Number(parseInt(value, 16).toString(10));
                        value = guiparamInfo.changeCameraResolution(num);
                        break;
                    case 'videoResolution'://视频分辨率
                        var num = Number(parseInt(value, 16).toString(10));
                        value = guiparamInfo.changeVideoResolution(num);
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

                var riskEvent = tab.find('.dsm-content .info-content');

                switch (riskId) {
                    case 146705: //盲区
                        guiparamInfo.setInputValue(item,riskEvent.eq(0));
                        break;
                    case 146608: //胎压
                        guiparamInfo.setInputValue(item,riskEvent.eq(0));
                    case 1464081://急加速
                        guiparamInfo.setInputValue(item, riskEvent.eq(0));
                        break;
                    case 146401://前向碰撞
                    case 146513://疲劳驾驶
                    case 1464082://急减速
                        guiparamInfo.setInputValue(item, riskEvent.eq(1));
                        break;
                    case 146402://车道偏离
                    case 146503://抽烟
                    case 1464083://急转弯
                        guiparamInfo.setInputValue(item, riskEvent.eq(2));
                        break;
                    case 146403://车距过近
                    case 146502://接打手持电话
                    case 147001://怠速
                        guiparamInfo.setInputValue(item, riskEvent.eq(3));
                        break;
                    case 146404://行人碰撞
                    case 146508://长时间不目视前方
                    case 147002://异常熄火
                        guiparamInfo.setInputValue(item, riskEvent.eq(4));
                        break;
                    case 146405://频繁变道
                    case 146510://未检测到驾驶员
                    case 147003://空挡滑行
                        guiparamInfo.setInputValue(item, riskEvent.eq(5));
                        break;
                    case 146407://障碍物
                    case 146515://驾驶员身份识别
                    case 147004://空挡滑行
                        guiparamInfo.setInputValue(item, riskEvent.eq(6));
                        break;
                    case 146409://道路标识超限
                        guiparamInfo.setInputValue(item, riskEvent.eq(7));
                        break;
                    default:
                        break;
                }
            }
        },
        setInputValue: function (data, riskEvent) {
            for (var key in data) {
                var value = data[key],
                    dataValue = value;
                var dom = riskEvent.find('.platform-' + key);

                switch (key) {
                    case 'oneLevelAlarmEnable':
                    case 'twoLevelAlarmEnable':
                    case 'roadSignRecognition':
                    case 'roadSignEnable':
                    case 'alarmEnable'://报警
                        value = value == 1 ? '打开' : '关闭';
                        break;
                    case 'touchStatus'://触发状态
                        value = guiparamInfo.getCaptureType(value);
                        break;
                    case 'photographTime':
                    case 'timeDistanceThreshold'://时距单位转换
                        value = value / 10;
                        break;
                    case 'unit':
                        value = 'psi';
                        break;
                    case 'tyreNumberName':
                        dataValue = data['tyreNumber'];
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
        //主动拍照触发状态
        getCaptureType: function (type) {
            // console.log(type);
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
            guiparamInfo.preventclick(self);
            isAll = true;
            var jsonDataArr = [];
            var riskIds = [arr0, arr1, arr2 ,arr3, arr4];//前向,驾驶员.激烈驾驶风险事件id

            for (var i = 0; i < riskIds.length; i++) {
                paramType = paramTypes[i];
                var jsonData = guiparamInfo.setJsonData(riskIds[i], $('.tab-pane').eq(i));
                jsonDataArr.push(jsonData[0]);
            }
            // console.log('全部下发参数', jsonDataArr);
            guiparamInfo.dsmInfoSend(jsonDataArr);
        },
        paramSendClick: function () {
            var self = $(this);
            guiparamInfo.preventclick(self);
            isAll = false;
            var inx = $('.tab-pane.active').index();
            var tab = $('.tab-pane').eq(inx);

            var riskIds = [arr0, arr1, arr2, arr3, arr4];
            var arr = riskIds[inx];
            paramType = tab.data('type');

            var jsonData = guiparamInfo.setJsonData(arr, tab);
            // console.log('本页签下发参数', jsonData);
            guiparamInfo.dsmInfoSend(jsonData);
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
                    commonParamSetting = guiparamInfo.getCommontData(inputs);
                } else {//风险事件
                    inputs = tab.find('.info-content').eq(i).find('input');
                    adasAlarmParamSettings.push(guiparamInfo.getInputData(item, inputs));
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
                    obj[key] = $(input).data('value');
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
            json_ajax("POST", "/clbs/adas/standard/param/setting.gsp", "json", false, data, guiparamInfo.dsmInfoSendCallback);
        },
        dsmInfoSendCallback: function (data) {
            if (data != null) {
                if (data.success) {
                    layer.load(2);
                    var tab = $('.tab-pane.active').index();
                    $("#dsmPoilSendStatus" + tab).val("参数已下发");

                    //获取下发状态
                    setTimeout(function () {
                        timerInterval = setInterval(guiparamInfo.getStatus(), 5000);
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
                            status.push(guiparamInfo.statusChange(value));
                        }

                        var tab = $('.tab-pane.active').index();
                        $("#dsmPoilSendStatus" + tab).val(status[0]);

                        //下发后再次获取传感器参数信息
                        timer = setTimeout(function () {
                            var sensorID = sensorIDs[tab];
                            guiparamInfo.getPeripheralInfo(sensorID);
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
            guiparamInfo.getPlatformData();

            //传感器参数
            guiparamInfo.getPeripheralInfo(100);//前向监测
            guiparamInfo.getPeripheralInfo(101);//驾驶员监测
            guiparamInfo.getPeripheralInfo(112);//激烈驾驶
            guiparamInfo.getPeripheralInfo(102);//胎压
            guiparamInfo.getPeripheralInfo(103);//盲区
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
        guiparamInfo.init();
        guiparamInfo.getPlatformData();

        //事件
        $(".info-span").on("click", guiparamInfo.hiddenparameterFn);//点击显示隐藏信息
        $("#dsmInfoSend").on("click", guiparamInfo.paramSendAllClick);//全部修正下发
        $("#dsmInfoSend2").on("click", guiparamInfo.paramSendClick);//本页签修正下发
        $("#dsmInfoRefresh").on("click", guiparamInfo.readConventionalRefreshClick);//刷新
        $(".sensorBtn").on("click", guiparamInfo.sensorBtnClick);//以传感器为准
        $(".platformBtn").on("click", guiparamInfo.platformBtnClick);//以平台设置为准
    })
})($, window)