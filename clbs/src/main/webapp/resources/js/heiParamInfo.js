//# sourceURL=heiParamInfo.js
var paramInfo;
(function ($, window) {
    var vid = $("#vehicleId").val();//车辆id
    var _timeout;
    var isRead = true;
    var timerInterval;

    var isAll = false;
    var paramType = 38,
        protocolType = 25;//黑标
    //获取参数设置信息
    var sensorIDs = [56, 57, 64, 65];//[前向监测, 驾驶员监测, 激烈驾驶监测, 盲区]
    var socketTypes = [57656, 57657, 57664, 57665];//[身份识别参数, 车辆运行参数, 驾驶行为参数, 驾驶行为参数]
    //参数下发是的风险事件id
    var arr0 = ['', 253802, 253801],//身份识别事件id,''表示公共参数
        arr1 = ['', 253901, 253902, 253903, 253904, 253905],//车辆运行事件id
        arr2 = ['', 254004, 254002, 254001, 254003, 254005],//驾驶行为事件id
        arr3 = ['', 254101, 254102, 254103, 254104, 254105, 254106, 254107, 254108, 254109];//驾驶行为参数
    var paramTypes = [38, 39, 40, 41];//[身份识别参数, 车辆运行参数, 驾驶行为参数, 驾驶行为参数]

    heiParamInfo = {
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

                heiParamInfo.subscribeSocket(socketType);//socket订阅
                heiParamInfo.getPeripheralInfo(sensorID);//传感器参数下发
            }
        },
        /**
         * socket订阅
         * @param type
         */
        subscribeSocket: function (type) {
            webSocket.subscribe(headers, "/user/topic/per" + type + "Info", function (data) {
                heiParamInfo.getSensor0104Param(data, type);
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
                "commandType": 225
            }, heiParamInfo.setreadConventionalCall);
        },
        setreadConventionalCall: function (data) {
            if (data.success) {
                if (isRead) {
                    $("#dsmInfoRefresh").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled', true);
                }
                heiParamInfo.createSocket0104InfoMonitor(data.msg);
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
            // console.log('传感器参数', param);
            heiParamInfo.setSenorInputData(param, type);
            $("#dsmInfoRefresh").removeAttr("disabled");
            $("#dsmInfoSend").removeAttr("disabled");
            $("#dsmInfoSend2").removeAttr("disabled");
            $(".sensorBtn").removeAttr("disabled");
            $(".platformBtn").removeAttr("disabled");
        },
        setSenorInputData: function (data, type) {
            console.log('result', data);
            if (!data) return;
            var tabId = '#system' + type;

            for (var key in data) {
                var value = data[key];

                var dom = $(tabId + ' .sensor-' + key);
                dom.attr('data-value', value);
                if (key.indexOf('CameraTime') !== -1) {
                    value = value / 10;
                    dom.val(value);
                }
                switch (key) {
                    case 'collisionWarnTime':
                    case 'collisionAlarmTime':
                        value = value / 10;
                        dom.val(value);
                        break;
                    case 'cameraResolution':
                        var dataValue = '0x0' + parseInt(value, 10).toString(16);
                        value = heiParamInfo.changeCameraResolution(value);
                        dom.val(value).attr('data-value', dataValue);
                        break;
                    case 'videoResolution':
                        var dataValue = '0x0' + parseInt(value, 10).toString(16);
                        value = heiParamInfo.changeVideoResolution(value);
                        dom.val(value).attr('data-value', dataValue);
                        break;
                    default:
                        dom.val(value);
                        break;
                }
            }
        },
        changeCameraResolution: function (data) {//拍照分辨率转换
            switch (data) {
                case 1:
                    return '1280×720';
                    break;
                case 2:
                    return '1920×1080';
                    break;
                case 3:
                    return '最高分辨率';
                    break;
                default:
                    break;
            }
        },
        changeVideoResolution: function (data) {//视频录制分辨率转换
            switch (data) {
                case 1:
                    data = 'D1';
                    break;
                case 2:
                    data = 'WD1';
                    break;
                case 3:
                    data = '720P';
                    break;
                case 4:
                    data = '1080P';
                    break;
                case 5:
                    data = '最高分辨率';
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
                var tab = $('.tab-pane[data-type=' + type + ']');
                heiParamInfo.getCommonParamSetting(item.commonParamSetting, tab);
                heiParamInfo.getAdasAlarmParamSettings(item.adasAlarmParamSettings, tab);
            }
        },
        //全局参数
        getCommonParamSetting: function (data, tab) {
            for (var key in data) {
                var value = data[key],
                    dataValue = value;
                var dom = tab.find('.commonParamSetting .platform-' + key);

                switch (key) {
                    case 'cameraResolution'://拍照分辨率
                        var num = Number(parseInt(value, 16).toString(10));
                        value = heiParamInfo.changeCameraResolution(num);
                        break;
                    case 'videoResolution'://视频分辨率
                        var num = Number(parseInt(value, 16).toString(10));
                        value = heiParamInfo.changeVideoResolution(num);
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
                    case 253802://读卡失败
                    case 253901://前向碰撞
                    case 254004://抽烟
                    case 254101://遮挡失效
                        heiParamInfo.setInputValue(item, riskEvent.eq(0));
                        break;
                    case 253801://人证不符
                    case 253902://车道偏离
                    case 254002://手持接打电话
                    case 254102://红外阻断失效
                        heiParamInfo.setInputValue(item, riskEvent.eq(1));
                        break;
                    case 253903://超速
                    case 254001://疲劳驾驶状态
                    case 254103://主存储器异常
                        heiParamInfo.setInputValue(item, riskEvent.eq(2));
                        break;
                    case 253904://路线偏离
                    case 254003://长时间不目视前方
                    case 254104://备用存储器异常
                        heiParamInfo.setInputValue(item, riskEvent.eq(3));
                        break;
                    case 253905://禁行路段/区域
                    case 254005://驾驶员不在驾驶位置
                    case 254105://卫星型号异常
                        heiParamInfo.setInputValue(item, riskEvent.eq(4));
                        break;
                    case 254106://通讯信号异常
                        heiParamInfo.setInputValue(item, riskEvent.eq(5));
                        break;
                    case 254107://备用电池欠压
                        heiParamInfo.setInputValue(item, riskEvent.eq(6));
                        break;
                    case 254108://备用电池失效
                        heiParamInfo.setInputValue(item, riskEvent.eq(7));
                        break;
                    case 254109://IC卡读卡模块故障
                        heiParamInfo.setInputValue(item, riskEvent.eq(8));
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
                    case 'photographTime':
                    case 'warningTimeThreshold':
                    case 'timeThreshold':
                    case 'timeDistanceThreshold'://时距单位转换
                        if (value != '-1') {
                            value = value / 10;
                        }
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
        preventclick: function (self) {
            self.prop('disabled', true);
            timer = setTimeout(function () {
                self.prop('disabled', false);
            }, 5000);
        },
        paramSendAllClick: function () {
            var self = $(this);
            heiParamInfo.preventclick(self);
            isAll = true;
            var jsonDataArr = [];
            var riskIds = [arr0, arr1, arr2, arr3];

            for (var i = 0; i < riskIds.length; i++) {
                paramType = paramTypes[i];
                var jsonData = heiParamInfo.setJsonData(riskIds[i], $('.tab-pane').eq(i));
                jsonDataArr.push(jsonData[0]);
            }
            // console.log('全部下发参数', jsonDataArr);
            heiParamInfo.dsmInfoSend(jsonDataArr);
        },
        paramSendClick: function () {
            var self = $(this);
            heiParamInfo.preventclick(self);
            isAll = false;
            var inx = $('.tab-pane.active').index();
            var tab = $('.tab-pane').eq(inx);

            var riskIds = [arr0, arr1, arr2, arr3];
            var arr = riskIds[inx];
            paramType = tab.data('type');

            var jsonData = heiParamInfo.setJsonData(arr, tab);
            // console.log('本页签下发参数', jsonData);
            heiParamInfo.dsmInfoSend(jsonData);
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
                    commonParamSetting = heiParamInfo.getCommontData(inputs);
                } else {//风险事件
                    inputs = tab.find('.info-content').eq(i).find('input');
                    adasAlarmParamSettings.push(heiParamInfo.getInputData(item, inputs));
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

                if (curClass.indexOf('platform-') !== -1) {
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
            json_ajax("POST", "/clbs/adas/standard/param/setting.gsp", "json", false, data, heiParamInfo.dsmInfoSendCallback);
        },
        dsmInfoSendCallback: function (data) {
            if (data != null) {
                if (data.success) {
                    layer.load(2);
                    var tab = $('.tab-pane.active').index();
                    $("#dsmPoilSendStatus" + tab).val("参数已下发");

                    //获取下发状态
                    setTimeout(function () {
                        timerInterval = setInterval(heiParamInfo.getStatus(), 5000);
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
                            status.push(heiParamInfo.statusChange(value));
                        }

                        var tab = $('.tab-pane.active').index();
                        $("#dsmPoilSendStatus" + tab).val(status[0]);

                        //下发后再次获取传感器参数信息
                        timer = setTimeout(function () {
                            var sensorID = sensorIDs[tab];
                            heiParamInfo.getPeripheralInfo(sensorID);
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
            heiParamInfo.getPlatformData();

            //传感器参数
            for (var i = 0; i < socketTypes.length; i++) {
                var sensorID = sensorIDs[i];
                heiParamInfo.getPeripheralInfo(sensorID);//传感器参数下发
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
        heiParamInfo.init();
        heiParamInfo.getPlatformData();

        //事件
        $(".info-span").on("click", heiParamInfo.hiddenparameterFn);//点击显示隐藏信息
        $("#dsmInfoSend").on("click", heiParamInfo.paramSendAllClick);//全部修正下发
        $("#dsmInfoSend2").on("click", heiParamInfo.paramSendClick);//本页签修正下发
        $("#dsmInfoRefresh").on("click", heiParamInfo.readConventionalRefreshClick);//刷新
        $(".sensorBtn").on("click", heiParamInfo.sensorBtnClick);//以传感器为准
        $(".platformBtn").on("click", heiParamInfo.platformBtnClick);//以平台设置为准
    })
})($, window)