//# sourceURL=jingParamInfo
//便于数据组装html中字段命名规则:
//传感器——sensor + "-" + 接口数据字段名
//平台——platform + "-" + 接口数据字段名

var jingParamInfo;
(function ($, window) {
    var vid = $("#vehicleId").val();//车辆id
    var _timeout;
    var isRead = true;
    var timerInterval;

    var protocolType = 24; //京标
    var paramTypes = [51, 52]; //驾驶员行为参数,车辆运行监测参数
    var arr0 = [246501, 246502, 246504, 246503, 246518, 246506, 246505],//驾驶员行为参数id
        arr1 = [246401, 246403, 246402, 246404, 246405, 246406, 246407, 246408, 246409];//车辆运行监测参数id
    var isAll = false;//是否为全部页签下发

    var paramType = [51, 52]; //[驾驶员行为参数, 驾驶员行为参数]
    var tabType = '';

    jingParamInfo = {
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

            for (var i=0; i<paramType.length; i++) {
                var type = paramType[i];
                jingParamInfo.subscribeSocket(type); //socket订阅
                jingParamInfo.getPeripheralInfo(type); //传感器参数下发
            }
        },
        /**
         * socket订阅
         * @param type
         */
        subscribeSocket: function (type) {
            if(type == 51){
                webSocket.subscribe(headers, "/user/topic/behavior_" + vid + "_Info", jingParamInfo.getSensor0104Param, null, null);
            }else if(type == 52) {
                webSocket.subscribe(headers, "/user/topic/operation_" + vid + "_Info", jingParamInfo.getSensor0104Param, null, null);
            }
        },
        /**
         * 传感器参数指令下发
         * @param sensorID
         */
        getPeripheralInfo: function (type) {
            var url = '/clbs/adas/standard/param/getJingPeripheralInfo';
            json_ajax("POST", url, "json", false, {
                "vid": vid,
                "paramType": type
            }, jingParamInfo.setreadConventionalCall);
        },
        setreadConventionalCall: function (data) {
            if (data.success) {
                if (isRead) {
                    $("#dsmInfoRefresh").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled', true);
                }

                jingParamInfo.createSocket0104InfoMonitor(data.msg);
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
            var param = result.data.msgBody
            console.log('设备上传数据', param);
            jingParamInfo.setSenorInputData(param);
            $("#dsmInfoRefresh").removeAttr("disabled");
            $("#dsmInfoSend").removeAttr("disabled");
            $("#dsmInfoSend2").removeAttr("disabled");
            $(".sensorBtn").removeAttr("disabled");
            $(".platformBtn").removeAttr("disabled");
        },

        setSenorInputData: function (result) {
            var list = result.params;
            for(var i = 0; i < list.length; i++) {
                var item = list[i];
                var value = item.value;

               if(value['level'] != undefined){
                   $(".sensor-level" + item.id).val(jingParamInfo.getAlarmLevel(value.level)).attr('data-value',value.level);
               }

                if(value['alarmVolume'] != undefined){
                    $(".sensor-alarmVolume" + item.id).val(value.alarmVolume).attr('data-value', value.alarmVolume);
                }

                if(value['voiceBroadcast'] != undefined){
                    $(".sensor-voiceBroadcast" + item.id).val(value.voiceBroadcast == 1 ? '打开' : '关闭').attr('data-value',value.voiceBroadcast);
                }

                if(value['videoTime'] != undefined){
                    $(".sensor-videoTime" + item.id).val(value.videoTime).attr('data-value',value.videoTime);
                }

                if(value['videoResolution'] != undefined){
                    $(".sensor-videoResolution" + item.id).val(jingParamInfo.changeVideoResolution(value.videoResolution)).attr('data-value',jingParamInfo.VideoResolutionDataValue(value.videoResolution));
                }

                if(value['cameraNum'] != undefined){
                    $(".sensor-cameraNum" + item.id).val(value.cameraNum).attr('data-value',value.cameraNum);
                }

                if(value['cameraResolution'] != undefined){
                    $(".sensor-cameraResolution" + item.id).val(jingParamInfo.changeCameraResolution(value.cameraResolution)).attr('data-value',jingParamInfo.cameraResolutionDataValue(value.cameraResolution));
                }

                if(value['cameraTime'] != undefined){
                    $(".sensor-cameraTime" + item.id).val(value.cameraTime / 10).attr('data-value',value.cameraTime);
                }

                if(value['speedThreshold'] != undefined){
                    $(".sensor-speedThreshold" + item.id).val(value.speedThreshold).attr('data-value',value.speedThreshold);
                }

                if(value['durationThreshold'] != undefined){
                    $(".sensor-durationThreshold" + item.id).val(value.durationThreshold).attr('data-value',value.durationThreshold);
                }
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
        VideoResolutionDataValue: function (data) {
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
              default:
                  data = '';
          }
          return data;
        },
        cameraResolutionDataValue: function (data) {
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

            for (var i =0; i < platformData.length; i++) {
                var item = platformData[i];
                if(item.paramType == 51) {
                    var tab = $('.tab-pane').eq(0);
                    jingParamInfo.getAdasAlarmParamSettings(item,tab);
                }

                if(item.paramType == 52) {
                    var tab = $('.tab-pane').eq(1);
                    jingParamInfo.getAdasAlarmParamSettings(item,tab)
                }
            }
        },

        //报警事件参数
        getAdasAlarmParamSettings: function (data, tab) {
            var riskId = data.riskFunctionId;
            var riskEvent =  tab.find('.dsm-content .info-content');
                switch (riskId) {
                    case 246501:
                    case 246401:
                        jingParamInfo.setInputValue(data, riskEvent.eq(0));
                        break;
                    case 246502:
                    case 246403:
                        jingParamInfo.setInputValue(data, riskEvent.eq(1));
                        break;
                    case 246504:
                    case 246402:
                        jingParamInfo.setInputValue(data, riskEvent.eq(2));
                        break;
                    case 246503:
                    case 246404:
                        jingParamInfo.setInputValue(data, riskEvent.eq(3));
                        break;
                    case 246518:
                    case 246405:
                        jingParamInfo.setInputValue(data, riskEvent.eq(4));
                        break;
                    case 246506:
                    case 246406:
                        jingParamInfo.setInputValue(data, riskEvent.eq(5));
                        break;
                    case 246505:
                    case 246407:
                        jingParamInfo.setInputValue(data, riskEvent.eq(6));
                        break;
                    case 246408:
                        jingParamInfo.setInputValue(data, riskEvent.eq(7));
                        break;
                    case 246409:
                        jingParamInfo.setInputValue(data, riskEvent.eq(8));
                        break;
                    default:
                        break;
                }
        },
        setInputValue: function (data, riskEvent) {
            for (var key in data) {
                var value = data[key],
                    dataValue = value;
                var dom = riskEvent.find('.platform-' + key);

                switch (key) {
                    case 'speech':
                        value = value == 1 ? '打开' : '关闭';
                        break;
                    case 'videoResolution':
                        value = jingParamInfo.getVideoResolution(value);
                        break;
                    case 'cameraResolution':
                        value = jingParamInfo.getCameraResolution(value);
                        break;
                    case 'alarmLevel':
                        value = jingParamInfo.getAlarmLevel(value);
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

        //视频分辨率数据转换
        getVideoResolution: function(value) {
            switch (value) {
                case '0x01':
                    return 'CIF';
                    break;
                case '0x02':
                    return 'HD1';
                    break;
                case '0x03':
                    return 'D1';
                    break;
                case '0x04':
                    return 'WD1';
                    break;
                case '0x05':
                    return 'VGA';
                    break;
                case '0x06':
                    return '720P';
                    break;
                case '0x07':
                    return '1080P';
                    break;
                default:
                    return '';
            }
        },

        //拍照分辨率数据转换
        getCameraResolution: function(value) {
            switch (value) {
                case '0x01':
                    return '352x288';
                    break;
                case '0x02':
                    return '704x288';
                    break;
                case '0x03':
                    return '704x576';
                    break;
                case '0x04':
                    return '640x480';
                    break;
                case '0x05':
                    return '1280x720';
                    break;
                case '0x06':
                    return '1920x1080';
                    break;
                default:
                    return '';
            }
        },

        //报警级别
        getAlarmLevel: function (value){
            if(value == '1') {
                return '低'
            }else if(value == '2'){
                return '一般'
            }else if(value == '3') {
                return '高'
            }else{
                return ''
            }}
        ,
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
            jingParamInfo.preventclick(self);

            isAll = true;
            var jsonDataArr = [];
            var riskIds = [arr0, arr1];

            for (var i = 0; i < riskIds.length; i++) {
                tabType = paramTypes[i];
                var jsonData = jingParamInfo.setJsonData(riskIds[i], $('.tab-pane').eq(i), tabType);
                jsonDataArr.push(jsonData)
            }

            jingParamInfo.dsmInfoSend(jsonDataArr);
        },
        paramSendClick: function () {//本页签下发
            var self = $(this);
            var jsonData = [];
            jingParamInfo.preventclick(self);

            isAll = false;
            var inx = $('.tab-pane.active').index();
            var tab = $('.tab-pane').eq(inx);

            var riskIds = [arr0, arr1];
            var arr = riskIds[inx];
            tabType = tab.data('type');

            var data = jingParamInfo.setJsonData(arr, tab, tabType);
            jsonData.push(data);
            jingParamInfo.dsmInfoSend(jsonData);
        },
        //组装下发参数
        setJsonData: function (arr, tab, tabType) {
            var list = [];
            for (var i = 0; i < arr.length; i++) {
                var item = arr[i];
                var inputs;
                inputs = tab.find('.info-content').eq(i).find('input');
                list.push(jingParamInfo.getInputData(item, inputs, tabType));
            }
            return list;
        },
        getInputData: function (riskFunctionId, inputs, tabType) {
            var obj = {
                'paramType': tabType,
                'protocolType': 24
            };

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

                if (curClass.indexOf('parameterId') != -1) {
                    curClass = $(input).attr('class');
                    obj[curClass] = $(input).attr('value');
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
            json_ajax("POST", "/clbs/adas/standard/param/batch/upsert", "json", false, data, jingParamInfo.dsmInfoSendCallback);
        },
        dsmInfoSendCallback: function (data) {
            if (data.success) {
                var tab = $('.tab-pane.active').index();
                $("#dsmPoilSendStatus" + tab).val("参数已下发");

                //获取下发状态
                setTimeout(function () {
                    timerInterval = setInterval(jingParamInfo.getStatus(), 5000);
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
                'paramTypes': isAll ? paramTypes.toString() : tabType
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
                            status.push(jingParamInfo.statusChange(value));
                        }

                        var tab = $('.tab-pane.active').index();
                        $("#dsmPoilSendStatus" + tab).val(status[0]);

                        //下发后再次获取传感器参数信息
                        timer = setTimeout(function () {
                            var type = tabType
                            jingParamInfo.getPeripheralInfo(type);
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
            jingParamInfo.getPlatformData();

            //传感器参数
            for (var i=0; i<paramType.length; i++) {
                var type = paramType[i];
                jingParamInfo.getPeripheralInfo(type); //传感器参数下发
            }
        },
        /**
         * 以传感器为准
         */
        sensorBtnClick: function () {
            var allInput = $(this).parents('.info-content').find('input');
            var len = allInput.length;
            for (var i = 1; i < len; i++) {
                var curClass = $(allInput[i]).attr('class');
                if (curClass.indexOf('sensor') !== '-1') {
                    var targetInput = $(allInput[i]).parent().siblings().find('input');
                    if (targetInput.attr('class').indexOf('platform') != '-1') {
                        targetInput.val($(allInput[i]).val());
                        targetInput.attr('data-value', $(allInput[i]).attr('data-value'));
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
        jingParamInfo.init();
        jingParamInfo.getPlatformData();

        //事件
        $(".info-span").on("click", jingParamInfo.hiddenparameterFn);//点击显示隐藏信息
        $("#dsmInfoSend").on("click", jingParamInfo.paramSendAllClick);//全部修正下发
        $("#dsmInfoSend2").on("click", jingParamInfo.paramSendClick);//本页签修正下发
        $("#dsmInfoRefresh").on("click", jingParamInfo.readConventionalRefreshClick);//刷新
        $(".sensorBtn").on("click", jingParamInfo.sensorBtnClick);//以传感器为准
        $(".platformBtn").on("click", jingParamInfo.platformBtnClick);//以平台设置为准
    })
})($, window);