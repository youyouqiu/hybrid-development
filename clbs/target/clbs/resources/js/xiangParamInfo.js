//# sourceURL=xiangParamInfo.js
(function ($, window) {
    var vid = $("#vehicleId").val();//车辆id
    var _timeout;
    var isRead = true;
    var timerInterval;

    var isAll = false;
    var paramType = 64,
        protocolType = 27;//湘标
    //获取参数设置信息
    var sensorIDs = [100, 101, 104, 233];//[高级驾驶辅助, 驾驶员状态监测, 车辆监测, 驾驶员比对]
    var socketTypes = [62308, 62309, 62312, 61673];//[高级驾驶辅助, 驾驶员状态监测, 车辆监测, 驾驶员比对]
    var paramTypes = [64, 65, 68, 233];//[高级驾驶辅助, 驾驶员状态监测, 车辆监测, 驾驶员比对]

    // 全局参数集合
    var commonData = [
        {index: 1, name: '拍照分辨率', id: 'cameraResolution'},
        {index: 2, name: '视频分辨率', id: 'videoResolution'},
        {index: 3, name: '提示音量', id: 'alarmVolume'},
        {index: 4, name: '主动拍照策略', id: 'touchStatus'},
        {index: 5, name: '定距拍照间隔(km)', id: 'distancePhotoInterval'},
        {index: 6, name: '拍照张数', id: 'photographNumber'},
        {index: 7, name: '拍照间隔(s)', id: 'photographTime'},
        {index: 8, name: '速度值(km/h)', id: 'speedLimit'},

        {index: 12, name: '定距间隔(km)', id: 'distancePhotoInterval'},
        {index: 13, name: '定时拍照间隔(s)', id: 'timingPhotoInterval'},
        {index: 14, name: '驾驶员身份识别策略', id: 'touchStatus', riskFunctionId: '276509'},
        {index: 15, name: '定时间隔(s)', id: 'timingPhotoInterval'},

        {index: 9, name: '离线人脸比对开关', id: 'offlineFaceCompareEnable'},
        {index: 10, name: 'DSM人脸比对成功阈值(%)', id: 'dsmCompareSuccessPercent'},
        {index: 11, name: '手机人脸比对成功阈值(%)', id: 'phoneCompareSuccessPercent'},
        {index: 16, name: '语音播报比对结果使能', id: 'voiceBroadcastComparisonEnable'},
    ]
    // 其它参数集合
    var adasData = [
        {index: 1, name: '报警开关', id: 'alarmEnable'},
        {index: 2, name: '时间阈值(s)', id: 'timeDistanceThreshold'},
        {index: 3, name: '使能速度阈值（km/h）', id: 'speedThreshold'},
        {index: 4, name: '视频录制时长(s)', id: 'videoRecordingTime'},
        {index: 5, name: '报警拍照间隔(s)', id: 'photographTime'},
        {index: 6, name: '报警拍照张数', id: 'photographNumber'},
        {index: 7, name: '判断时间段(s)', id: 'timeSlotThreshold'},
        {index: 8, name: '判断次数', id: 'frequencyThreshold'},
        {index: 9, name: '识别开关', id: 'roadSignRecognition'},
        {index: 10, name: '主动拍照开关', id: 'initiativePictureEnable'},
        {index: 11, name: '驾驶员更换事件', id: 'driverChangeEnable', riskFunctionId: '276509'},
        {index: 12, name: '主动拍照事件', id: 'initiativePictureEnable', riskFunctionId: '276508'},
        {index: 13, name: '上传抓拍通道', id: 'captureChannel'},
        {index: 14, name: '上传主码流视频通道', id: 'primaryChannel'},
        {index: 15, name: '上传子码流视频通道', id: 'subcodeChannel'},
        {index: 16, name: '判断时间间隔(s)', id: 'timeSlotThreshold'},
        {index: 17, name: '拍照分辨率', id: 'cameraResolution'},
        {index: 18, name: '拍照分辨率', id: 'cameraResolution', riskFunctionId: '68'},
    ]

    //按钮模板
    var template =
        '<div class="col-md-3 col-md-offset-2">\n' +
        '    <botton class="btn btn-primary btn-oc-width sensorBtn" disabled="">以传感器为准</botton>\n' +
        '</div>\n' +
        '<div class="col-md-3 col-md-offset-2">\n' +
        '    <botton class="btn btn-primary btn-oc-width platformBtn" disabled="">以平台设置为准</botton>\n' +
        '</div>\n'

    var util = {
        initDom: function () {
            mockData.forEach(function (item) {
                util.renderDom(item.allData, item.domId)
            })
        },
        renderDom: function (data, domId) {
            data.forEach(function (item) {
                var target = $('#' + domId + ' ' + '.dsm-content')
                if (item.label == '全局参数') {
                    target = $('#' + domId)
                    target.prepend(util.renderContent(item.data, true, item.riskFunctionId))
                    target.prepend(util.renderTitle(item.label, true))
                } else {
                    target.append(util.renderTitle(item.label))
                    target.append(util.renderContent(item.data, false, item.riskFunctionId))
                }
            })
        },
        renderContent: function (data, show, riskFunctionId) {
            var infoContent = util.create('div', 'info-content commonParamSetting')
            if (!show) infoContent.setAttribute('style', 'display:none')
            data.forEach(function (detail) {
                var lastId = detail.riskFunctionId || riskFunctionId
                var formGroup = util.create('div', 'form-group')
                //传感器
                var label = util.create('label', 'col-md-2 control-label')
                label.innerText = detail.name + '：'
                var div = util.create('div', 'col-md-3')
                var sensorInputId = detail.id + '_' + lastId + '_sensor'
                div.innerHTML = '<input class="form-control sensor-' + detail.id + '" id="' + sensorInputId + '" readonly>'
                //平台
                var label2 = util.create('label', 'col-md-2 control-label')
                label2.innerText = detail.name + '：'
                var div2 = util.create('div', 'col-md-3')
                var platformInputId = detail.id + '_' + lastId + '_platform'
                div2.innerHTML = '<input class="form-control platform-' + detail.id + '" id="' + platformInputId + '" readonly>'
                formGroup.appendChild(label)
                formGroup.appendChild(div)
                formGroup.appendChild(label2)
                formGroup.appendChild(div2)
                infoContent.appendChild(formGroup)
            })
            var btns = util.create('div', 'form-group')
            btns.innerHTML = template
            infoContent.appendChild(btns)
            return infoContent
        },
        renderTitle: function (name, show) {
            var title = util.create('div', 'form-group')
            if (show) {
                title.innerHTML =
                    ' <h4 class="col-md-6">' + name + '</h4>\n' +
                    ' <div class="col-md-6 control-label text-right">\n' +
                    '     <div class="info-span">\n' +
                    '         <font>隐藏信息</font>\n' +
                    '         <span aria-hidden="true" class="fa fa-chevron-up"></span>\n' +
                    '     </div>\n' +
                    ' </div>'
            } else {
                title.innerHTML =
                    ' <h4 class="col-md-6">' + name + '</h4>\n' +
                    ' <div class="col-md-6 control-label text-right">\n' +
                    '     <div class="info-span">\n' +
                    '         <font>显示更多</font>\n' +
                    '         <span aria-hidden="true" class="fa fa-chevron-down"></span>\n' +
                    '     </div>\n' +
                    ' </div>'
            }
            return title
        },
        selectData: function (arr, origin) {
            if (!arr) return origin
            return origin
                .filter(function (item) {
                    return arr.indexOf(item.index) != -1
                })
                .sort(function (a, b) {
                    return arr.indexOf(a.index) - arr.indexOf(b.index)
                })
        },
        create: function (tagName, className) {
            var node = document.createElement(tagName ? tagName : 'div')
            if (className) {
                util.setC(node, className)
            }
            return node
        },
        setC: function (node, className) {
            node.setAttribute('class', className)
        },
    }

    var mockData = [
        {
            domId: 'driverAssistant',
            allData: [
                {
                    label: '全局参数',
                    riskFunctionId: '64',
                    data: util.selectData([1, 2, 3, 4, 5, 13, 6, 7, 8], commonData)
                },
                {
                    label: '车道偏离',
                    riskFunctionId: '-276402',
                    data: util.selectData([1, 4, 5, 6, 13, 14, 15], adasData)
                },
                {
                    label: '前向碰撞',
                    riskFunctionId: '-276401',
                    data: util.selectData([1, 2, 4, 5, 6, 13, 14, 15], adasData)
                },
                {
                    label: '行人碰撞',
                    riskFunctionId: '-276404',
                    data: util.selectData([1, 2, 3, 4, 5, 6, 13, 14, 15], adasData)
                },
                {
                    label: '车距过近',
                    riskFunctionId: '-276403',
                    data: util.selectData([1, 2, 4, 5, 6, 13, 14, 15], adasData)
                },
                {
                    label: '道路标志',
                    riskFunctionId: '276405',
                    data: util.selectData([9, 10, 6], adasData)
                },
            ]
        },
        {
            domId: 'driverStatus',
            allData: [
                {
                    label: '全局参数',
                    riskFunctionId: '65',
                    data: util.selectData([1, 2, 3, 14, 4, 15, 12, 7, 6, 8], commonData)
                },
                {
                    label: '生理疲劳驾驶',
                    riskFunctionId: '276501',
                    data: util.selectData([1, 4, 5, 6, 13, 14, 15], adasData)
                },
                {
                    label: '接打手持电话',
                    riskFunctionId: '276502',
                    data: util.selectData([1, 16, 4, 5, 6, 13, 14, 15], adasData)
                },
                {
                    label: '抽烟',
                    riskFunctionId: '276503',
                    data: util.selectData([1, 16, 4, 5, 6, 13, 14, 15], adasData)
                },
                {
                    label: '不目视前方',
                    riskFunctionId: '276504',
                    data: util.selectData([1, 4, 5, 6, 13, 14, 15], adasData)
                },
                {
                    label: '摄像头偏离驾驶位',
                    riskFunctionId: '276505',
                    data: util.selectData([1, 4, 5, 6, 13, 14, 15], adasData)
                },
                {
                    label: '未系安全带',
                    riskFunctionId: '276507',
                    data: util.selectData([1, 4, 5, 6, 13, 14, 15], adasData)
                },

                {
                    label: '玩手机',
                    riskFunctionId: '276506',
                    data: util.selectData([1, 13, 14, 15], adasData)
                },
                {
                    label: '设备失效',
                    riskFunctionId: '276512',
                    data: util.selectData([1], adasData)
                },
                {
                    label: '红外线阻断墨镜失效',
                    riskFunctionId: '276510',
                    data: util.selectData([1], adasData)
                },
                {
                    label: '事件使能',
                    data: util.selectData([11, 12], adasData)
                },
            ]
        },
        {
            domId: 'vehicleMonitor',
            allData: [
                {
                    label: '超员',
                    riskFunctionId: '276801',
                    data: util.selectData([1, 18, 4, 5, 6, 13, 14, 15], adasData)
                }
            ]
        },
        {
            domId: 'driverCompare',
            allData: [
                {
                    label: '驾驶员比对',
                    riskFunctionId: '272331',
                    data: util.selectData([9, 10, 11, 16], commonData)
                }
            ]
        },
    ]

    xiangParamInfo = {
        init: function () {
            util.initDom()
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

                xiangParamInfo.subscribeSocket(socketType);//socket订阅
                xiangParamInfo.getPeripheralInfo(sensorID);//传感器参数下发
            }
        },
        /**
         * socket订阅
         * @param type
         */
        subscribeSocket: function (type) {
            webSocket.subscribe(headers, "/user/topic/per" + type + "Info", function (data) {
                xiangParamInfo.updateSensorInputValue(data);
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
                "commandType": sensorID != 233 ? 243 : 240
            }, xiangParamInfo.setreadConventionalCall);
        },
        setreadConventionalCall: function (data) {
            if (data.success) {
                if (isRead) {
                    $("#dsmInfoRefresh").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled', true);
                }
                xiangParamInfo.createSocket0104InfoMonitor(data.msg);
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
        updateSensorInputValue: function (msg) {
            if (msg == null) {
                return;
            }
            var result = $.parseJSON(msg.body);
            if (result.desc.monitorId != vid) return
            isRead = false;
            clearTimeout(_timeout);
            layer.closeAll();
            $("#dsmInfoRefresh").html("刷新").prop('disabled', false);
            var data = result.data.msgBody.params[0];
            xiangParamInfo.setSenorInputData(data);
            $("#dsmInfoRefresh").removeAttr("disabled");
            $("#dsmInfoSend").removeAttr("disabled");
            $("#dsmInfoSend2").removeAttr("disabled");
            $(".sensorBtn").removeAttr("disabled");
            $(".platformBtn").removeAttr("disabled");
        },
        setSenorInputData: function (data) {
            if (!data.value) {
                return
            }
            var mapIds = {
                62308: 'vehicleWarning',
                62309: 'driverVehicleWarning',
                62312: 'overMan',
                61673: 'param'
            }
            var result = data.value[mapIds[data.id]]
            var type = data.id
            for (var key in result) {
                if (result.hasOwnProperty(key)) {
                    var value = result[key];
                    var domId = xiangParamInfo.getDomId(key, type)
                    if (!domId) {
                        continue
                    }
                    var dom = $(domId);
                    var parserValue = xiangParamInfo.handleSensorValue(value, key, type) + ''
                    if (key.indexOf('ChannelNum') != -1) {
                        var temp = value.toString(2).split('').reverse()
                        var idx = temp.findIndex(function (item) {
                            return item == 1
                        })
                        if (idx != -1) {
                            value = idx
                        }
                    }
                    if (key == 'videoResolution' || key == 'cameraResolution') {
                        value = '0x0' + Number(value).toString(16)
                    }
                    dom.attr('data-value', value);
                    if (parserValue != undefined) dom.val(parserValue);
                }
            }
        },
        getDomId: function (serverId, type) {
            // 62308:高级驾驶辅助 62309:驾驶员状态监测 62310:胎压监测 62311:盲区监测 61673:驾驶员比对
            var ids = {
                62308: {
                    speedThreshold: 'speedLimit' + '_' + 64, //报警判断速度阈值
                    alarmVolume: 'alarmVolume' + '_' + 64, //报警提示音量
                    cameraStrategy: 'touchStatus' + '_' + 64, //主动拍照策略
                    timingCamera: 'timingPhotoInterval' + '_' + 64, //主动定时拍照时间间隔
                    fixedCamera: 'distancePhotoInterval' + '_' + 64, //主动定距拍照距离间隔
                    cameraNum: 'photographNumber' + '_' + 64, //单次次主动拍照张数
                    cameraTime: 'photographTime' + '_' + 64, //单次次主动拍照时间间隔
                    cameraResolution: 'cameraResolution' + '_' + 64, //拍照分辨率
                    videoResolution: 'videoResolution' + '_' + 64, //视频录制分辨率
                    //车道偏离
                    deviateSpeed: 'speedThreshold' + '_' + -276402, //车道偏离预警分级速度阈值
                    deviateVideoTime: 'videoRecordingTime' + '_' + -276402, //车道偏离预警前后视频录制时间
                    deviateCameraNum: 'photographNumber' + '_' + -276402, //车道偏离预警拍照片张数
                    deviateCameraTime: 'photographTime' + '_' + -276402, //车道偏离预警拍照间隔
                    deviateCaptureChannelNum: 'captureChannel' + '_' + -276402, //抓拍通道号
                    deviateStreamChannelNum: 'primaryChannel' + '_' + -276402, //主通道号
                    deviateSubStreamChannelNum: 'subcodeChannel' + '_' + -276402, //子通道号
                    //前车碰撞
                    vehicleCollisionTime: 'timeDistanceThreshold' + '_' + -276401, //前车碰撞预警时间阈值
                    vehicleCollisionSpeed: 'speedThreshold' + '_' + -276401, //前车碰撞报警分级速度阈值
                    vehicleCollisionVideoTime: 'videoRecordingTime' + '_' + -276401, //前车碰撞预警前后视频录制时间
                    vehicleCollisionCameraNum: 'photographNumber' + '_' + -276401, //前车碰撞预警拍照片张数
                    vehicleCollisionCameraTime: 'photographTime' + '_' + -276401, //行人碰撞预警拍照间隔
                    vehicleCaptureChannelNum: 'captureChannel' + '_' + -276401, //抓拍通道号
                    vehicleStreamChannelNum: 'primaryChannel' + '_' + -276401, //主通道号
                    vehicleSubStreamChannelNum: 'subcodeChannel' + '_' + -276401, //子通道号
                    //行人碰撞
                    pedestrianCollisionTime: 'timeDistanceThreshold' + '_' + -276404, //行人碰撞预警时间阈值
                    pedestrianCollisionSpeed: 'speedThreshold' + '_' + -276404, //行人碰撞预警分级速度阈值
                    pedestrianCollisionVideoTime: 'videoRecordingTime' + '_' + -276404, //行人碰撞预警前后视频录制时间
                    pedestrianCollisionCameraNum: 'photographNumber' + '_' + -276404, //行人碰撞预警拍照片张数
                    pedestrianCollisionCameraTime: 'photographTime' + '_' + -276404, //行人碰撞预警拍照间隔
                    pedestrianCaptureChannelNum: 'captureChannel' + '_' + -276404, //抓拍通道号
                    pedestrianStreamChannelNum: 'primaryChannel' + '_' + -276404, //主通道号
                    pedestrianSubStreamChannelNum: 'subcodeChannel' + '_' + -276404, //子通道号
                    // 车距过近
                    distanceMail: 'timeDistanceThreshold' + '_' + -276403, //车距监控报警距离阈值
                    distanceSpeed: 'speedThreshold' + '_' + -276403, //车距监控报警分级速度阈值
                    distanceVideoTime: 'videoRecordingTime' + '_' + -276403, //车距过近报警前后视频录制时间
                    distanceCameraNum: 'photographNumber' + '_' + -276403, //车距过近报警拍照张数
                    distanceCameraTime: 'photographTime' + '_' + -276403, //车距过近报警拍照间隔
                    distanceCaptureChannelNum: 'captureChannel' + '_' + -276403, //抓拍通道号
                    distanceStreamChannelNum: 'primaryChannel' + '_' + -276403, //主通道号
                    distanceSubStreamChannelNum: 'subcodeChannel' + '_' + -276403, //子通道号
                    // 道路标志
                    speedLimitCameraNum: 'photographNumber' + '_' + 276405, //道路标志识别拍照张数
                    speedLimitCameraTime: 'photographTime' + '_' + 276405, //道路标志识别拍照间隔
                },
                62309: {
                    speedThreshold: 'speedLimit' + '_' + 65, //报警判断速度阈值
                    alarmVolume: 'alarmVolume' + '_' + 65, //报警提示音量
                    cameraStrategy: 'touchStatus' + '_' + 65, //主动拍照策略
                    timingCamera: 'timingPhotoInterval' + '_' + 65, //主动定时拍照时间间隔
                    fixedCamera: 'distancePhotoInterval' + '_' + 65, //主动定距拍照距离间隔
                    cameraNum: 'photographNumber' + '_' + 65, //单次次主动拍照张数
                    cameraTime: 'photographTime' + '_' + 65, //单次次主动拍照时间间隔
                    cameraResolution: 'cameraResolution' + '_' + 65, //拍照分辨率
                    videoResolution: 'videoResolution' + '_' + 65, //视频录制分辨率
                    // 疲劳驾驶
                    fatigueSpeed: 'speedThreshold' + '_' + 276501, //疲劳驾驶报警分级速度阈值
                    fatigueVideoTime: 'videoRecordingTime' + '_' + 276501, //疲劳驾驶报警前后视频录制时
                    fatigueCameraNum: 'photographNumber' + '_' + 276501, //疲劳驾驶报警拍照张数
                    fatigueCameraTime: 'photographTime' + '_' + 276501, //疲劳驾驶报警拍照间隔时间
                    fatigueCaptureChannelNum: 'captureChannel' + '_' + 276501, //抓拍通道号
                    fatigueStreamChannelNum: 'primaryChannel' + '_' + 276501, //主通道号
                    fatigueSubStreamChannelNum: 'subcodeChannel' + '_' + 276501, //子通道号
                    // 接打电话
                    pickUpDecideTime: 'timeSlotThreshold' + '_' + 276502, //时间阈值
                    pickUpSpeed: 'speedThreshold' + '_' + 276502, //接打电话报警分级速度阈值改
                    pickUpVideoTime: 'videoRecordingTime' + '_' + 276502, //接打电话报警前后视频录制时间
                    pickUpCameraNum: 'photographNumber' + '_' + 276502, //接打电话报警拍驾驶员完整面部特征照片张数
                    pickUpCameraTime: 'photographTime' + '_' + 276502, //接打电话报警拍驾驶员完整面部特征照片间隔时间
                    pickUpCaptureChannelNum: 'captureChannel' + '_' + 276502, //抓拍通道号
                    pickUpStreamChannelNum: 'primaryChannel' + '_' + 276502, //主通道号
                    pickUpSubStreamChannelNum: 'subcodeChannel' + '_' + 276502, //子通道号
                    // 抽烟
                    smokingDecideTime: 'timeSlotThreshold' + '_' + 276503, //时间阈值
                    smokingSpeed: 'speedThreshold' + '_' + 276503, //抽烟报警分级车速阈值
                    smokingVideoTime: 'videoRecordingTime' + '_' + 276503, //抽烟报警前后视频录制时间
                    smokingCameraNum: 'photographNumber' + '_' + 276503, //抽烟报警拍驾驶员完整面部特征照片张数
                    smokingCameraTime: 'photographTime' + '_' + 276503, //抽烟报警拍驾驶员完整面部特征照片间隔时间
                    smokingCaptureChannelNum: 'captureChannel' + '_' + 276503, //抓拍通道号
                    smokingStreamChannelNum: 'primaryChannel' + '_' + 276503, //主通道号
                    smokingSubStreamChannelNum: 'subcodeChannel' + '_' + 276503, //子通道号
                    // 不目视前方
                    attentionSpeed: 'speedThreshold' + '_' + 276504, //报警分级车速阈值
                    attentionVideoTime: 'videoRecordingTime' + '_' + 276504, //前后视频录制时间
                    attentionCameraNum: 'photographNumber' + '_' + 276504, //报警拍照张数
                    attentionCameraTime: 'photographTime' + '_' + 276504, //报警拍照间隔时间
                    attentionCaptureChannelNum: 'captureChannel' + '_' + 276504, //抓拍通道号
                    attentionStreamChannelNum: 'primaryChannel' + '_' + 276504, //主通道号
                    attentionSubStreamChannelNum: 'subcodeChannel' + '_' + 276504, //子通道号
                    // 摄像头偏离驾驶位
                    driveDeedVideoTime: 'videoRecordingTime' + '_' + 276505, //视频录制时间
                    driveDeedCameraNum: 'photographNumber' + '_' + 276505, //照片张数
                    driveDeedCameraTime: 'photographTime' + '_' + 276505, //照片间隔时间
                    driveCaptureChannelNum: 'captureChannel' + '_' + 276505, //抓拍通道号
                    driveStreamChannelNum: 'primaryChannel' + '_' + 276505, //主通道号
                    driveSubStreamChannelNum: 'subcodeChannel' + '_' + 276505, //子通道号
                    // 未系安全带
                    safetyBeltVideoTime: 'videoRecordingTime' + '_' + 276507, //视频录制时间
                    safetyBeltCameraNum: 'photographNumber' + '_' + 276507, //照片张数
                    safetyBeltCameraTime: 'photographTime' + '_' + 276507, //照片间隔时间
                    safetyBeltCaptureChannelNum: 'captureChannel' + '_' + 276507, //抓拍通道号
                    safetyBeltStreamChannelNum: 'primaryChannel' + '_' + 276507, //主通道号
                    safetyBeltSubStreamChannelNum: 'subcodeChannel' + '_' + 276507, //子通道号
                    // 玩手机
                    playPhoneVideoTime: 'videoRecordingTime' + '_' + 276506, //视频录制时间
                    playPhoneCameraNum: 'photographNumber' + '_' + 276506, //照片张数
                    playPhoneCameraTime: 'photographTime' + '_' + 276506, //照片间隔时间
                    playPhoneCaptureChannelNum: 'captureChannel' + '_' + 276506, //抓拍通道号
                    playPhoneStreamChannelNum: 'primaryChannel' + '_' + 276506, //主通道号
                    playPhoneSubStreamChannelNum: 'subcodeChannel' + '_' + 276506, //子通道号

                    driveDeedType: 'touchStatus' + '_' + 276509, //驾驶员身份识别触发
                },
                62312: {
                    cameraResolution: 'cameraResolution' + '_' + 68, //拍照分辨率
                    overManVideoTime: 'videoRecordingTime' + '_' + 276801, //视频录制时
                    overManCameraNum: 'photographNumber' + '_' + 276801, //拍照张数
                    overManCameraTime: 'photographTime' + '_' + 276801, //拍照间隔时间
                    overManCaptureChannelNum: 'captureChannel' + '_' + 276801, //抓拍通道号
                    overManStreamChannelNum: 'primaryChannel' + '_' + 276801, //主通道号
                    overManSubStreamChannelNum: 'subcodeChannel' + '_' + 276801, //子通道号
                },
                61673: {
                    dsmSimilarityThreshold: 'dsmCompareSuccessPercent' + '_' + 272331,
                    phoneSimilarityThreshold: 'phoneCompareSuccessPercent' + '_' + 272331,
                    onOff: 'offlineFaceCompareEnable' + '_' + 272331,
                    voiceEnable: 'voiceBroadcastComparisonEnable' + '_' + 272331
                }
            }
            if (ids[type]) {
                return '#' + ids[type][serverId] + '_sensor'
            }
            return ''

        },
        handleSensorValue: function (value, key, type) {
            if (key.toLowerCase().indexOf('cameratime') != -1) {
                return value / 10;
            }
            if (['obstacleDistance', 'vehicleCollisionTime', 'pedestrianCollisionTime', 'distanceMail'].includes(key)) {
                return value / 10;
            }
            if (key.indexOf('ChannelNum') != -1) {
                var temp = value.toString(2).split('').reverse()
                var idx = temp.findIndex(function (item) {
                    return item == 1
                })
                if (idx != -1) {
                    return '通道' + (idx + 1)
                }
            }
            switch (key) {
                case 'alarmEnable':
                    return xiangParamInfo.parserAlarmValue(value, type);
                case 'videoResolution':
                    return xiangParamInfo.mapVideoResolutionValue(value);
                case 'cameraResolution':
                    return xiangParamInfo.mapCameraResolutionValue(value);
                case 'cameraStrategy':
                    return xiangParamInfo.mapTouchStatusValue(value);
                case 'eventEnable':
                    return xiangParamInfo.parserEventValue(value, type);
                case 'driveDeedType':
                    return xiangParamInfo.mapDriverStrategy(value);
                case 'onOff':
                    return value == 1 ? '开' : '关'
                case 'voiceEnable':
                    return value == 2 ? '开' : '关'
                case 'fixedCamera':
                    return (Number(value) / 1000).toFixed(2)
                default :
                    return value
            }
        },
        tireModel: function (value) {
            var tireModelMap = JSON.parse($("#tireModelMap").val());
            var resultArr = [];
            for (var item in tireModelMap) {
                if (tireModelMap.hasOwnProperty(item)) {
                    var objTmp = {};
                    objTmp.name = item;
                    objTmp.value = tireModelMap[item];
                    resultArr.push(objTmp);
                }
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
        parserAlarmValue: function (data, type) {//报警使能(二进制)
            var alarm = data.toString(2).split('').reverse();
            for (var i = 32 - alarm.length; i > 0; i--) {
                alarm.push(0)
            }
            var ids = {
                62308: {
                    4: 'alarmEnable_-276402_sensor',
                    6: 'alarmEnable_-276401_sensor',
                    8: 'alarmEnable_-276404_sensor',
                    10: 'alarmEnable_-276403_sensor',
                },
                62309: {
                    0: 'alarmEnable_276501_sensor',
                    2: 'alarmEnable_276502_sensor',
                    4: 'alarmEnable_276503_sensor',
                    6: 'alarmEnable_276504_sensor',
                    8: 'alarmEnable_276505_sensor',
                    16: 'alarmEnable_276506_sensor',
                    18: 'alarmEnable_276507_sensor',
                    24: 'alarmEnable_276512_sensor',
                    25: 'alarmEnable_276510_sensor',
                },
                62312: {
                    0: 'alarmEnable_276801_sensor',
                }
            }
            if (!ids[type]) {
                return
            }
            alarm.forEach(function (value, index) {
                $('#' + ids[type][index]).attr('data-value', value)
                $('#' + ids[type][index]).val(value == 1 ? '开' : '关')
            })
        },
        parserEventValue: function (value, type) {
            var alarm = value.toString(2).split('').reverse();
            for (var i = 2 - alarm.length; i > 0; i--) {
                alarm.push(0)
            }
            var ids = {
                62308: {
                    0: 'roadSignRecognition_276405_sensor',
                    1: 'initiativePictureEnable_276405_sensor',
                },
                62309: {
                    0: 'driverChangeEnable_276509_sensor',
                    1: 'initiativePictureEnable_276508_sensor',
                },
            }
            if (!ids[type]) {
                return
            }
            alarm.forEach(function (value, index) {
                $('#' + ids[type][index]).attr('data-value', value)
                $('#' + ids[type][index]).val(value == 1 ? '开' : '关')
            })
        },
        mapDriverStrategy: function (value) {
            switch (value) {
                case 1:
                    return '定时触发'
                case 2:
                    return '定距触发'
                case 3:
                    return '插卡开始行驶触发'
                default :
                    return '不开启'
            }
        },
        mapCameraResolutionValue: function (data) {//拍照分辨率转换
            data = Number(data)
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
        mapVideoResolutionValue: function (data) {//视频录制分辨率转换
            data = Number(data)
            switch (data) {
                case 0:
                    data = '最低分辨率';
                    break;
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
                    data = '最高分辨率';
            }
            return data;
        },
        /**
         * 初始化平台设置参数
         */
        initPlatformData: function () {
            var platformData = JSON.parse($('#platformData').val());
            platformData.forEach(function (item) {
                var commonSetting = item.commonParamSetting
                var adasSetting = item.riskFunctionId || item.adasAlarmParamSettings
                var paramType = commonSetting.paramType
                for (var key in commonSetting) {
                    if (commonSetting.hasOwnProperty(key)) {
                        var id = '#' + key + '_' + paramType + '_platform'
                        var dataValue = commonSetting[key]
                        if (key == 'videoResolution' || key == 'cameraResolution') {
                            dataValue = '0x0' + Number(dataValue).toString(16)
                        }
                        $(id).attr('data-value', dataValue)
                        var value = xiangParamInfo.handleValue(key, commonSetting[key])
                        if (value != '-1' && value != '-0.1') {
                            $(id).val(value)
                        }
                    }
                }
                adasSetting.forEach(function (item) {
                    for (var key in item) {
                        if (item.hasOwnProperty(key)) {
                            var id = '#' + key + '_' + item.riskFunctionId + '_platform'
                            var dataValue = item[key]
                            if (key == 'videoResolution' || key == 'cameraResolution') {
                                dataValue = '0x0' + Number(dataValue).toString(16)
                            }
                            $(id).attr('data-value', dataValue)
                            var value = xiangParamInfo.handleValue(key, item[key])
                            if (key == 'touchStatus') {
                                value = xiangParamInfo.mapDriverStrategy(item[key])
                            }
                            if (value != '-1' && value != '-0.1') {
                                $(id).val(value)
                            }
                        }
                    }
                })
            })
        },
        handleValue: function (key, value) {
            if (key == 'voiceBroadcastComparisonEnable') {
                return value == 2 ? '开' : '关'
            }
            if (key.indexOf('Enable') != -1 || key.indexOf('Recognition') != -1) return value == 1 ? '开' : '关'
            if (key.indexOf('Channel') != '-1') {
                return '通道' + (value + 1)
            }
            switch (key) {
                case 'videoResolution':
                    return xiangParamInfo.mapVideoResolutionValue(value)
                case 'cameraResolution':
                    return xiangParamInfo.mapCameraResolutionValue(value)
                case 'touchStatus':
                    return xiangParamInfo.mapTouchStatusValue(value)
                case 'timeDistanceThreshold':
                case 'photographTime':
                    return value / 10;
                case 'tyreNumber':
                    return xiangParamInfo.tireModel(value)
                default :
                    return value
            }
        },
        //主动拍照触发状态
        mapTouchStatusValue: function (type) {
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
                case '4':
                case '0x04':
                    return '点火触发';
                    break;
                default:
                    break;
            }
        },
        /**
         * 参数修正下发
         */
        preventClick: function (self) {
            self.prop('disabled', true);
            timer = setTimeout(function () {
                self.prop('disabled', false);
            }, 5000);
        },
        paramSendAllClick: function () {
            xiangParamInfo.preventClick($(this));
            isAll = true;
            var data = xiangParamInfo.getAllData();
            xiangParamInfo.dsmInfoSend(data);
        },
        paramSendClick: function () {
            xiangParamInfo.preventClick($(this));
            isAll = false;
            var tab = $('.tab-pane.active')
            paramType = tab.data('type');
            var data = xiangParamInfo.getAllData();
            xiangParamInfo.dsmInfoSend(data.filter(function (item) {
                return item.commonParamSetting.paramType == paramType
            }));
        },
        //组装下发参数
        getAllData: function () {
            var tempObj = {}
            var allInput = $('.tab-content input')
            allInput.each(function (index, item) {
                // id格式： 字段名 + paramType/riskFunctionId + 'platform' eg:cameraResolution_64_platform
                var id = $(item).attr('id')
                if (!id) return
                if (id.indexOf('platform') == -1) return
                // tempObj[id] = $(item).val()
                tempObj[id] = $(item).attr('data-value')
            })

            return xiangParamInfo.parserData(tempObj)
        },

        // 解析id中的数据
        parserData: function (obj) {
            var res = []
            var allCommonParam = {}
            var allAdasParam = {}
            for (var key in obj) {
                if (obj.hasOwnProperty(key)) {
                    var s = key.split('_')
                    var id = s[1]
                    var dataIndex = s[0]
                    var value = obj[key]
                    // if (dataIndex.indexOf('Resolution') != -1) {
                    //     value = Number(value)
                    // }
                    if (id.length == 2) {
                        if (allCommonParam[id]) {
                            allCommonParam[id][dataIndex] = value
                        } else {
                            allCommonParam[id] = {}
                            allCommonParam[id][dataIndex] = value
                        }
                    } else {
                        if (allAdasParam[id]) {
                            allAdasParam[id][dataIndex] = value
                        } else {
                            allAdasParam[id] = {}
                            allAdasParam[id][dataIndex] = value
                        }
                    }
                }
            }
            var findItem = function (item, key) {
                return item.commonParamSetting && item.commonParamSetting.paramType == key.substr(2, 2)
            }
            for (var key in allAdasParam) {
                if (allAdasParam.hasOwnProperty(key)) {
                    var target = res.find(function (item) {
                        var paramType = key.substr(2, 2)
                        if (key.substr(0, 1) == '-') {
                            paramType = key.substr(3, 2)
                        }
                        return item.commonParamSetting && item.commonParamSetting.paramType == paramType
                    })
                    if (target) {
                        target.adasAlarmParamSettings.push(Object.assign({}, allAdasParam[key], {
                            riskFunctionId: key,
                            vehicleId: vid,

                        }))
                    } else {
                        var temp = {
                            adasAlarmParamSettings: [],
                            commonParamSetting: {}
                        }
                        temp.adasAlarmParamSettings.push(Object.assign({}, allAdasParam[key], {
                            riskFunctionId: key,
                            vehicleId: vid,

                        }))
                        var paramType = key.substr(2, 2) == '23' ? '233' : key.substr(2, 2)
                        if (key.substr(0, 1) == '-') {
                            paramType = key.substr(3, 2)
                        }
                        temp.commonParamSetting = Object.assign({}, allCommonParam[key.substr(2, 2)], {
                            paramType: paramType,
                            protocolType: protocolType,
                            vehicleId: vid
                        })
                        res.push(temp)
                    }
                }
            }
            return res
        },

        //下发
        dsmInfoSend: function (jsonData) {
            // 报警为关时不传递其它参数
            jsonData.forEach(function (setting) {
                if (setting.adasAlarmParamSettings) {
                    setting.adasAlarmParamSettings = setting.adasAlarmParamSettings.map(function (item) {
                        if (item.alarmEnable == 0) {
                            return {
                                alarmEnable: 0,
                                riskFunctionId: item.riskFunctionId,
                                vehicleId: item.vehicleId,
                            }
                        }
                        if (item.roadSignRecognition == 0 && item.initiativePictureEnable == 0) {
                            return {
                                riskFunctionId: item.riskFunctionId,
                                vehicleId: item.vehicleId,
                                roadSignRecognition: 0,
                                initiativePictureEnable: 0
                            }
                        }
                        if (item.riskFunctionId == 276508 && item.initiativePictureEnable == 0) {
                            return {
                                riskFunctionId: item.riskFunctionId,
                                vehicleId: item.vehicleId,
                                initiativePictureEnable: 0,
                            }
                        }
                        if (item.offlineFaceCompareEnable == 0) {
                            return {
                                riskFunctionId: item.riskFunctionId,
                                vehicleId: item.vehicleId,
                                offlineFaceCompareEnable: 0,
                                voiceBroadcastComparisonEnable: item.voiceBroadcastComparisonEnable
                            }
                        }
                        if (item.driverChangeEnable == 0) {
                            return {
                                riskFunctionId: item.riskFunctionId,
                                vehicleId: item.vehicleId,
                                driverChangeEnable: 0,
                            }
                        }
                        return item
                    })
                }
            })
            var data = {
                "alarmParam": JSON.stringify(jsonData),
                "vehicleIds": vid,
                "sendFlag": true
            };
            json_ajax("POST", "/clbs/adas/standard/param/setting.gsp", "json", false, data, xiangParamInfo.dsmInfoSendCallback);
        },
        dsmInfoSendCallback: function (data) {
            if (data != null) {
                if (data.success) {
                    layer.load(2);
                    var tab = $('.tab-pane.active').index();
                    $("#dsmPoilSendStatus" + tab).val("参数已下发");

                    //获取下发状态
                    setTimeout(function () {
                        timerInterval = setInterval(xiangParamInfo.getStatus(), 5000);
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
                        var timer = null;
                        var handler = function (index, item) {
                            if ($(item).data('type') == key) {
                                $(item).find('.sendStatus').val(xiangParamInfo.statusChange(obj[key]))
                            }
                        }
                        clearTimeout(timer);
                        for (var key in obj) {
                            if (obj.hasOwnProperty(key)) {
                                $('.tab-pane').each(handler)
                            }
                        }
                        //下发后再次获取传感器参数信息
                        var tab = $('.tab-pane.active').index();
                        timer = setTimeout(function () {
                            var sensorID = sensorIDs[tab];
                            xiangParamInfo.getPeripheralInfo(sensorID);
                        }, 1000);
                    }
                    clearInterval(timerInterval);
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
            xiangParamInfo.initPlatformData();

            //传感器参数
            for (var i = 0; i < socketTypes.length; i++) {
                var sensorID = sensorIDs[i];
                xiangParamInfo.getPeripheralInfo(sensorID);//传感器参数下发
            }
        },

        //传感器常规参数对比后赋值(以传感器为准)
        sensorBtnClick: function () {
            var allInput = $(this).parents('.info-content').find('input');
            allInput.each(function (index, input) {
                // 单数为传感器参数 双数为平台参数
                if ((index + 1) % 2 == 1) {
                    var sensorValue = $(input).val()
                    var sensorDataValue = $(input).attr('data-value')
                    var platformInput = $(input).parents('.form-group').find('input')[1]
                    $(platformInput).val(sensorValue)
                    $(platformInput).attr('data-value', sensorDataValue)
                }
            })
        },
        //传感器常规参数对比后赋值(以平台设置为准)
        platformBtnClick: function () {
            $("#dealType").val("pt");
            var allInput = $(this).parents('.info-content').find('input');
            allInput.each(function (index, input) {
                // 单数为传感器参数 双数为平台参数
                if ((index + 1) % 2 == 0) {
                    var sensorValue = $(input).val()
                    $($(input).parents('.form-group').find('input')[0]).val(sensorValue)
                }
            })
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
        xiangParamInfo.init();
        xiangParamInfo.initPlatformData();

        //事件
        $(".info-span").on("click", xiangParamInfo.hiddenparameterFn);//点击显示隐藏信息
        $("#dsmInfoSend").on("click", xiangParamInfo.paramSendAllClick);//全部修正下发
        $("#dsmInfoSend2").on("click", xiangParamInfo.paramSendClick);//本页签修正下发
        $("#dsmInfoRefresh").on("click", xiangParamInfo.readConventionalRefreshClick);//刷新
        $(".sensorBtn").on("click", xiangParamInfo.sensorBtnClick);//以传感器为准
        $(".platformBtn").on("click", xiangParamInfo.platformBtnClick);//以平台设置为准
    })
}($, window))