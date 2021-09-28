//# sourceURL=jiParamInfo
//便于数据组装html中字段命名规则:
//传感器——sensor + "-" + 接口数据字段名
//平台——platform + "-" + 接口数据字段名

var jiParamInfo;
(function ($, window) {
    var vid = $("#vehicleId").val();//车辆id
    var _timeout;
    var isRead = true;
    var timerInterval;

    var paramType = 64,
        protocolType = 13;//冀标
    var paramTypes = [64, 65];//前向,驾驶员
    var arr0 = ['', '', 136401, 136402, 136403, 136404, 136405, 136407, 136409],//前向事件id,''表示全局参数和主动拍照参数
        arr1 = ['', '', 136513, 136503, 136502, 136508, 136510, 136515];//驾驶员事件id
    var isAll = false;//是否为全部页签下发

    var socketTypes = [62308, 62309]; //[前向监测, 驾驶员行为监测]
    var sensorIDs = [100, 101];

    jiParamInfo = {
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
                $("#dsmInfoRefresh").html("刷新").prop('disabled', false);
            };

            for (var i=0; i<socketTypes.length; i++) {
                var type = socketTypes[i],
                    sensorId = sensorIDs[i];

                jiParamInfo.subscribeSocket(type); //socket订阅
                jiParamInfo.getPeripheralInfo(sensorId); //传感器参数下发
            }
        },
        /**
         * socket订阅
         * @param type
         */
        subscribeSocket: function (type) {
            webSocket.subscribe(headers, "/user/topic/per" + type + "Info", function (data) {
                jiParamInfo.getSensor0104Param(data, type);
            }, null, null);
        },
        /**
         * 传感器参数指令下发
         * @param sensorID
         */
        getPeripheralInfo: function (sensorID) {
            var url = '/clbs/r/riskManagement/DefineSettings/getPeripheralInfo';
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "sensorID": sensorID,
                "commandType": 243
            }, jiParamInfo.setreadConventionalCall);
        },
        setreadConventionalCall: function (data) {
            if (data.success) {
                if (isRead) {
                    $("#dsmInfoRefresh").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled', true);
                }
                jiParamInfo.createSocket0104InfoMonitor(data.msg);
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
            if (msg == null) return;
            var result = $.parseJSON(msg.body);

            isRead = false;
            clearTimeout(_timeout);
            layer.closeAll();
            $("#dsmInfoRefresh").html("刷新").prop('disabled', false);
            var param = result.data.msgBody.params[0].value;
            // console.log('设备上传数据', param);
            jiParamInfo.setSenorInputData(param, type);
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
            }

            for (var key in data) {
                var value = data[key];

                var dom = $(tabId + ' .sensor-' + key);
                dom.attr('data-value', value);

                switch (key) {
                    case 'driveDeedType':
                    case 'cameraStrategy':
                        value = jiParamInfo.getCaptureType(value);
                        dom.val(value);
                        break;
                    case 'eventEnable':
                    case 'alarmEnable':
                        jiParamInfo.getBinaryValue(value, tabId, key);
                        break;
                    case 'cameraResolution':
                        var dataValue = '0x0' + parseInt(value, 10).toString(16);
                        value = jiParamInfo.changeCameraResolution(value);
                        dom.val(value).attr('data-value', dataValue);
                        break;
                    case 'videoResolution':
                        var dataValue = '0x0' + parseInt(value, 10).toString(16);
                        value = jiParamInfo.changeVideoResolution(value);
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
                    default:
                        dom.val(value);
                        break;
                }
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
            // console.log('平台参数', platformData);
            for (var i = 0; i < platformData.length; i++) {
                var item = platformData[i];
                var tab = $('.tab-pane').eq(i);

                jiParamInfo.getCommonParamSetting(item.commonParamSetting, tab);
                jiParamInfo.getAdasAlarmParamSettings(item.adasAlarmParamSettings, tab);
            }
        },
        //全局参数
        getCommonParamSetting: function (data, tab) {
            for (var key in data) {
                var value = data[key],
                    dataValue = value;
                var dom = tab.find('.commonParamSetting .platform-' + key);

                switch (key) {
                    case 'touchStatus'://主动拍照触发状态
                        value = jiParamInfo.getCaptureType(value)
                        break;
                    case 'cameraResolution'://拍照分辨率
                        var num = Number(parseInt(value, 16).toString(10));
                        value = jiParamInfo.changeCameraResolution(num);
                        break;
                    case 'videoResolution'://视频分辨率
                        var num = Number(parseInt(value, 16).toString(10));
                        value = jiParamInfo.changeVideoResolution(num);
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
                    case 136401://前向碰撞
                    case 136513://疲劳驾驶
                        jiParamInfo.setInputValue(item, riskEvent.eq(1));
                        break;
                    case 136402://车道偏离
                    case 136503://抽烟
                        jiParamInfo.setInputValue(item, riskEvent.eq(2));
                        break;
                    case 136403://车距过近
                    case 136502://接打手持电话
                        jiParamInfo.setInputValue(item, riskEvent.eq(3));
                        break;
                    case 136404://行人碰撞
                    case 136508://分神驾驶
                        jiParamInfo.setInputValue(item, riskEvent.eq(4));
                        break;
                    case 136405://频繁变道
                    case 136510://驾驶员异常行为
                        jiParamInfo.setInputValue(item, riskEvent.eq(5));
                        break;
                    case 136407://障碍物
                    case 136515://驾驶员身份识别
                        jiParamInfo.setInputValue(item, riskEvent.eq(6));
                        break;
                    case 136409://道路标识超限
                        jiParamInfo.setInputValue(item, riskEvent.eq(7));
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
                        value = jiParamInfo.getCaptureType(value);
                        break;
                    case 'photographTime':
                    case 'timeDistanceThreshold'://时距单位转换
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
        preventclick: function (self) {//5s内禁止再次提交表单
            self.prop('disabled', true);
            timer = setTimeout(function () {
                self.prop('disabled', false);
            }, 5000);
        },
        paramSendAllClick: function () {//全部下发
            var self = $(this);
            jiParamInfo.preventclick(self);

            isAll = true;
            var jsonDataArr = [];
            var riskIds = [arr0, arr1];

            for (var i = 0; i < riskIds.length; i++) {
                paramType = paramTypes[i];
                var jsonData = jiParamInfo.setJsonData(riskIds[i], $('.tab-pane').eq(i));
                jsonDataArr.push(jsonData[0]);
            }

            jiParamInfo.dsmInfoSend(jsonDataArr);
        },
        paramSendClick: function () {//本页签下发
            var self = $(this);
            jiParamInfo.preventclick(self);

            isAll = false;
            var inx = $('.tab-pane.active').index();
            var tab = $('.tab-pane').eq(inx);

            var riskIds = [arr0, arr1];
            var arr = riskIds[inx];
            paramType = tab.data('type');

            var jsonData = jiParamInfo.setJsonData(arr, tab);
            jiParamInfo.dsmInfoSend(jsonData);
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
                    commonParamSetting = jiParamInfo.getCommontData(inputs);
                } else {//风险事件
                    inputs = tab.find('.info-content').eq(i).find('input');
                    adasAlarmParamSettings.push(jiParamInfo.getInputData(item, inputs));
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
            json_ajax("POST", "/clbs/adas/standard/param/setting.gsp", "json", false, data, jiParamInfo.dsmInfoSendCallback);
        },
        dsmInfoSendCallback: function (data) {
            if (data.success) {
                var tab = $('.tab-pane.active').index();
                $("#dsmPoilSendStatus" + tab).val("参数已下发");

                //获取下发状态
                setTimeout(function () {
                    timerInterval = setInterval(jiParamInfo.getStatus(), 5000);
                }, 2000);
                setTimeout(function () {
                    clearInterval(timerInterval);
                }, 60000);
            } else {
                if (data.msg) {
                    layer.msg(data.msg, {move: false});
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
                            status.push(jiParamInfo.statusChange(value));
                        }

                        var tab = $('.tab-pane.active').index();
                        $("#dsmPoilSendStatus" + tab).val(status[0]);

                        //下发后再次获取传感器参数信息
                        timer = setTimeout(function () {
                            var sensorID = sensorIDs[tab];
                            jiParamInfo.getPeripheralInfo(sensorID);
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
        /**
         * 刷新
         */
        readConventionalRefreshClick: function () {
            isRead = true;
            $("#dsmPoilSendStatus").val("").removeAttr("data-id");//下发状态
            var vid = $("#vehicleId").val();
            $("#dsmInfoSend").attr("disabled", "disabled");
            $(".sensorBtn").attr("disabled", "disabled");
            $(".platformBtn").attr("disabled", "disabled");

            //为平台参数
            jiParamInfo.getPlatformData();

            //传感器参数
            for (var i=0; i<socketTypes.length; i++) {
                var sensorId = sensorIDs[i];
                jiParamInfo.getPeripheralInfo(sensorId); //传感器参数下发
            }
        },
        /**
         * 以传感器为准
         */
        sensorBtnClick: function () {
            var allInput = $(this).parents('.info-content').find('input');
            var len = allInput.length;
            for (var i = 0; i < len; i++) {
                var curClass = $(allInput[i]).attr('class');

                if (curClass.indexOf('sensor') != '-1') {
                    var targetInput = $(allInput[i]).parent().siblings().find('input');
                    if (targetInput.attr('class').indexOf('platform') != '-1') {
                        targetInput.val($(allInput[i]).val());
                        /*if(curClass.indexOf('sensor-fixedCamera') != -1){
                            targetInput.attr('data-value', $(allInput[i]).attr('data-value') / 1000);
                        }else{*/
                        targetInput.attr('data-value', $(allInput[i]).attr('data-value'));
                        // }
                    }
                }
            }
        },
        /**
         * 以平台为准
         */
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
        /**
         * 隐藏信息
         */
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
        jiParamInfo.init();
        jiParamInfo.getPlatformData();

        //事件
        $(".info-span").on("click", jiParamInfo.hiddenparameterFn);//点击显示隐藏信息
        $("#dsmInfoSend").on("click", jiParamInfo.paramSendAllClick);//全部修正下发
        $("#dsmInfoSend2").on("click", jiParamInfo.paramSendClick);//本页签修正下发
        $("#dsmInfoRefresh").on("click", jiParamInfo.readConventionalRefreshClick);//刷新
        $(".sensorBtn").on("click", jiParamInfo.sensorBtnClick);//以传感器为准
        $(".platformBtn").on("click", jiParamInfo.platformBtnClick);//以平台设置为准
    })
})($, window);