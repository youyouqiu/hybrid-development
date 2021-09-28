//# sourceURL=luParamInfo.js
(function ($, window) {
    var vid = $("#vehicleId").val();//车辆id
    var _timeout;
    var isRead = true;
    var timerInterval;

    var isAll = false;
    var paramType = 64,
        protocolType = 28;//粤标
    //获取参数设置信息
    var sensorIDs = [100, 101, 102, 103];//[高级驾驶辅助, 驾驶员状态监测, 胎压监测, 盲区监测,  ]
    var socketTypes = [62308, 62309, 62310, 62311];//[高级驾驶辅助, 驾驶员状态监测, 胎压监测, 盲区监测,  ]
    var paramTypes = [64, 65, 66, 67];//[高级驾驶辅助, 驾驶员状态监测, 胎压监测, 盲区监测,  ]

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
        {index: 13, name: '定时间隔(s)', id: 'timingPhotoInterval'},
        {index: 15, name: '定时拍照间隔(s)', id: 'timingPhotoInterval'},
        {index: 14, name: '驾驶员身份识别策略', id: 'touchStatus', riskFunctionId: '286507'},

        {index: 9, name: '离线人脸比对开关', id: 'offlineFaceCompareEnable'},
        {index: 10, name: 'DSM人脸比对成功阈值(1%)', id: 'dsmCompareSuccessPercent'},
        {index: 11, name: '手机人脸比对成功阈值(1%)', id: 'phoneCompareSuccessPercent'},
    ]
    // 其它参数集合
    var adasData = [
        {index: 1, name: '一级报警开关', id: 'oneLevelAlarmEnable'},
        {index: 2, name: '二级报警开关', id: 'twoLevelAlarmEnable'},
        {index: 3, name: '时间阈值(s)', id: 'timeDistanceThreshold'},
        {index: 4, name: '分级速度阈值(km/h)', id: 'alarmLevelSpeedThreshold'},
        {index: 5, name: '视频录制时长(s)', id: 'videoRecordingTime'},
        {index: 6, name: '报警拍照间隔(s)', id: 'photographTime'},
        {index: 7, name: '报警拍照张数', id: 'photographNumber'},
        {index: 8, name: '判断时间段(s)', id: 'timeSlotThreshold'},
        {index: 9, name: '判断次数', id: 'frequencyThreshold'},
        {index: 10, name: '报警开关', id: 'roadSignEnable'},
        {index: 11, name: '识别开关', id: 'roadSignRecognition'},
        {index: 12, name: '主动拍照开关', id: 'initiativePictureEnable'},
        {index: 13, name: '驾驶员更换事件', id: 'driverChangeEnable', riskFunctionId: '286507'},

        {index: 14, name: '轮胎规格型号', id: 'tyreNumberName'},
        {index: 15, name: '胎压单位', id: 'unit'},
        {index: 16, name: '正常胎压值', id: 'pressure'},
        {index: 17, name: '胎压不平衡阈值(%)', id: 'pressureThreshold'},
        {index: 18, name: '慢漏气门限(%)', id: 'slowLeakThreshold'},
        {index: 19, name: '低压阈值', id: 'lowPressure'},
        {index: 20, name: '高压阈值', id: 'highPressure'},
        {index: 21, name: '高温阈值(℃)', id: 'highTemperature'},
        {index: 22, name: '电压阈值(%)', id: 'electricityThreshold'},
        {index: 23, name: '定时上报时间间隔(s)', id: 'uploadTime'},

        {index: 24, name: '后方接近报警时间阈值(s)', id: 'rear'},
        {index: 25, name: '侧后方接近报警时间阈值(s)', id: 'sideRear'},

        {index: 26, name: '判断时间间隔(s)', id: 'timeSlotThreshold'},
        {index: 27, name: '主动拍照事件', id: 'initiativePictureEnable', riskFunctionId: '286506'},
        {index: 28, name: '报警开关', id: 'pedestrianInspect'},

    ]

    var domData = [
        {
            domId: 'driverAssistant',
            allData: [
                {
                    label: '全局参数',
                    riskFunctionId: '64',
                    data: readParamUtil.selectData([1, 2, 3, 4, 5, 15, 6, 7, 8], commonData)
                },
                {
                    label: '障碍物检测',
                    riskFunctionId: '286407',
                    data: readParamUtil.selectData([1, 2, 3, 4, 5, 6, 7], adasData)
                },
                {
                    label: '频繁变道',
                    riskFunctionId: '286405',
                    data: readParamUtil.selectData([1, 2, 8, 9, 4, 5, 6, 7], adasData)
                },
                {
                    label: '车道偏离',
                    riskFunctionId: '286402',
                    data: readParamUtil.selectData([1, 2, 4, 5, 6, 7], adasData)
                },
                {
                    label: '向前碰撞',
                    riskFunctionId: '286401',
                    data: readParamUtil.selectData([1, 2, 3, 4, 5, 6, 7], adasData)
                },
                {
                    label: '行人碰撞',
                    riskFunctionId: '286404',
                    data: readParamUtil.selectData([1, 2, 3, 4, 5, 6, 7], adasData)
                },
                {
                    label: '车距过近',
                    riskFunctionId: '286403',
                    data: readParamUtil.selectData([1, 2, 3, 4, 5, 6, 7], adasData)
                },
                {
                    label: '实线变道',
                    riskFunctionId: '286410',
                    data: readParamUtil.selectData([1, 2, 4, 5, 6, 7], adasData)
                },
                {
                    label: '车厢过道行人监测',
                    riskFunctionId: '286411',
                    data: readParamUtil.selectData([28, 4, 5, 6, 7], adasData)
                },
                {
                    label: '道路标志',
                    riskFunctionId: '286406',
                    data: readParamUtil.selectData([10, 11, 12, 6, 7], adasData)
                },
            ]
        },
        {
            domId: 'driverStatus',
            allData: [
                {
                    label: '全局参数',
                    riskFunctionId: '65',
                    data: readParamUtil.selectData([1, 2, 3, 14, 4, 13, 12, 7, 6, 8], commonData)
                },
                {
                    label: '疲劳驾驶',
                    riskFunctionId: '286501',
                    data: readParamUtil.selectData([1, 2, 4, 5, 6, 7], adasData)
                },
                {
                    label: '接打手持电话',
                    riskFunctionId: '286502',
                    data: readParamUtil.selectData([1, 2, 26, 4, 5, 6, 7], adasData)
                },
                {
                    label: '抽烟',
                    riskFunctionId: '286503',
                    data: readParamUtil.selectData([1, 2, 26, 4, 5, 6, 7], adasData)
                },
                {
                    label: '不目视前方',
                    riskFunctionId: '286504',
                    data: readParamUtil.selectData([1, 2, 4, 5, 6, 7], adasData)
                },
                {
                    label: '驾驶员异常',
                    riskFunctionId: '286505',
                    data: readParamUtil.selectData([1, 2, 4, 5, 6, 7], adasData)
                },
                {
                    label: '摄像头遮挡',
                    riskFunctionId: '286508',
                    data: readParamUtil.selectData([1, 2, 4], adasData)
                },
                {
                    label: '不系安全带',
                    riskFunctionId: '286510',
                    data: readParamUtil.selectData([1, 2, 4, 5, 6, 7], adasData)
                },
                {
                    label: '红外阻断型墨镜失效',
                    riskFunctionId: '286511',
                    data: readParamUtil.selectData([1, 2, 4, 5, 6, 7], adasData)
                },
                {
                    label: '双脱把',
                    riskFunctionId: '286512',
                    data: readParamUtil.selectData([1, 2, 4, 5, 6, 7], adasData)
                },
                {
                    label: '玩手机',
                    riskFunctionId: '286513',
                    data: readParamUtil.selectData([1, 2, 4, 5, 6, 7], adasData)
                },
                {
                    label: '事件使能',
                    data: readParamUtil.selectData([13, 27], adasData)
                },
            ]
        },
        {
            domId: 'tirePressure',
            allData: [
                {
                    label: '胎压监测',
                    riskFunctionId: '286601',
                    data: readParamUtil.selectData([14, 15, 16, 17, 18, 19, 20, 21, 22, 23], adasData)
                }
            ]
        },
        {
            domId: 'blindSpot',
            allData: [
                {
                    label: '盲点监测',
                    riskFunctionId: '286701',
                    data: readParamUtil.selectData([24, 25], adasData)
                }
            ]
        },
    ]

    luParamInfo = {
        init: function () {
            readParamUtil.initDom(domData)
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

                luParamInfo.subscribeSocket(socketType);//socket订阅
                luParamInfo.getPeripheralInfo(sensorID);//传感器参数下发
            }
        },
        /**
         * socket订阅
         * @param type
         */
        subscribeSocket: function (type) {
            webSocket.subscribe(headers, "/user/topic/per" + type + "Info", function (data) {
                luParamInfo.updateSensorInputValue(data);
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
            }, luParamInfo.setreadConventionalCall);
        },
        setreadConventionalCall: function (data) {
            if (data.success) {
                if (isRead) {
                    $("#dsmInfoRefresh").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled', true);
                }
                luParamInfo.createSocket0104InfoMonitor(data.msg);
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
            luParamInfo.setSenorInputData(data);
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
                62310: 'tyre1',
                62311: 'deadZoneDetection',
            }
            var result = data.value[mapIds[data.id]]
            var type = data.id
            for (var key in result) {
                if (result.hasOwnProperty(key)) {
                    var value = result[key];
                    var domId = luParamInfo.getDomId(key, type)
                    if (!domId) {
                        continue
                    }
                    var dom = $(domId);
                    var parserValue = luParamInfo.handleSensorValue(value, key, type)
                    if (key == 'tyreNumber') {
                        value = luParamInfo.tireModel(value)
                    }
                    if (key == 'videoResolution' || key == 'cameraResolution') {
                        value = '0x0' + Number(value).toString(16)
                    }
                    if(key == 'fixedCamera'){ //【定距拍照距离间隔】终端上传的数据单位是米，平台数据单位是千米，这里需要进行装换
                        value = parserValue
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
                    // 障碍物预警
                    obstacleDistance: 'timeDistanceThreshold' + '_' + 286407,// 时间阈值
                    obstacleSpeed: 'alarmLevelSpeedThreshold' + '_' + 286407, // 障碍物预警分级速度阈值
                    obstacleVideoTime: 'videoRecordingTime' + '_' + 286407, // 障碍物预警前后视频录制时间
                    obstacleCameraNum: 'photographNumber' + '_' + 286407, // 障碍物预警拍照张数
                    obstacleCameraTime: 'photographTime' + '_' + 286407, // 障碍物预警拍照间隔时间
                    //频繁变道
                    laneChangeTime: 'timeSlotThreshold' + '_' + 286405, //频繁变道预警判断时间段
                    laneChangeNum: 'frequencyThreshold' + '_' + 286405, //频繁变道预警判断次数
                    laneChangeSpeed: 'alarmLevelSpeedThreshold' + '_' + 286405, //频繁变道预警分级速度阈值
                    laneChangeVideoTime: 'videoRecordingTime' + '_' + 286405, //繁变道预警前后视频录制时间
                    laneChangeCameraNum: 'photographNumber' + '_' + 286405, //频繁变道预警拍照片张数
                    laneChangeCameraTime: 'photographTime' + '_' + 286405, //频繁变道预警拍照间隔
                    //车道偏离
                    deviateTime: 'timeDistanceThreshold' + '_' + 286402, //
                    deviateSpeed: 'alarmLevelSpeedThreshold' + '_' + 286402, //车道偏离预警分级速度阈值
                    deviateVideoTime: 'videoRecordingTime' + '_' + 286402, //车道偏离预警前后视频录制时间
                    deviateCameraNum: 'photographNumber' + '_' + 286402, //车道偏离预警拍照片张数
                    deviateCameraTime: 'photographTime' + '_' + 286402, //车道偏离预警拍照间隔
                    //前车碰撞
                    vehicleCollisionTime: 'timeDistanceThreshold' + '_' + 286401, //前车碰撞预警时间阈值
                    vehicleCollisionSpeed: 'alarmLevelSpeedThreshold' + '_' + 286401, //前车碰撞报警分级速度阈值
                    vehicleCollisionVideoTime: 'videoRecordingTime' + '_' + 286401, //前车碰撞预警前后视频录制时间
                    vehicleCollisionCameraNum: 'photographNumber' + '_' + 286401, //前车碰撞预警拍照片张数
                    vehicleCollisionCameraTime: 'photographTime' + '_' + 286401, //行人碰撞预警拍照间隔
                    //行人碰撞
                    pedestrianCollisionTime: 'timeDistanceThreshold' + '_' + 286404, //行人碰撞预警时间阈值
                    pedestrianCollisionSpeed: 'alarmLevelSpeedThreshold' + '_' + 286404, //行人碰撞预警分级速度阈值
                    pedestrianCollisionVideoTime: 'videoRecordingTime' + '_' + 286404, //行人碰撞预警前后视频录制时间
                    pedestrianCollisionCameraNum: 'photographNumber' + '_' + 286404, //行人碰撞预警拍照片张数
                    pedestrianCollisionCameraTime: 'photographTime' + '_' + 286404, //行人碰撞预警拍照间隔
                    // 车距过近
                    distanceMail: 'timeDistanceThreshold' + '_' + 286403, //车距监控报警距离阈值
                    distanceSpeed: 'alarmLevelSpeedThreshold' + '_' + 286403, //车距监控报警分级速度阈值
                    distanceVideoTime: 'videoRecordingTime' + '_' + 286403, //车距过近报警前后视频录制时间
                    distanceCameraNum: 'photographNumber' + '_' + 286403, //车距过近报警拍照张数
                    distanceCameraTime: 'photographTime' + '_' + 286403, //车距过近报警拍照间隔
                    //实线变道
                    fullTimeSpeed: 'alarmLevelSpeedThreshold_286410',//实线变道报警分级速度
                    fullTimeVideoTime: 'videoRecordingTime_286410',//实线变道前后视频录制时间
                    fullTimeCameraNum: 'photographNumber_286410',//实线变道报警拍照张数
                    fullTimeCameraTime: 'photographTime_286410',//实线变道报警拍照间隔
                    //车厢过道行人监测
                    pedestrianInspectSpeed: 'alarmLevelSpeedThreshold_286411', //车厢过道行人检测报警分级速度阈值
                    pedestrianInspectVideoTime: 'videoRecordingTime_286411', //车厢过道行人检测报警前后视频录制时间
                    pedestrianInspectCameraNum: 'photographNumber_286411', //车厢过道行人检测报警拍照张数
                    pedestrianInspectCameraTime: 'photographTime_286411', //车厢过道行人检测报警拍照间隔
                    // 道路标志
                    speedLimitCameraNum: 'photographNumber' + '_' + 286406, //道路标志识别拍照张数
                    speedLimitCameraTime: 'photographTime' + '_' + 286406, //道路标志识别拍照间隔
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
                    fatigueSpeed: 'alarmLevelSpeedThreshold' + '_' + 286501, //疲劳驾驶报警分级速度阈值
                    fatigueVideoTime: 'videoRecordingTime' + '_' + 286501, //疲劳驾驶报警前后视频录制时
                    fatigueCameraNum: 'photographNumber' + '_' + 286501, //疲劳驾驶报警拍照张数
                    fatigueCameraTime: 'photographTime' + '_' + 286501, //疲劳驾驶报警拍照间隔时间
                    // 接打电话
                    pickUpDecideTime: 'timeSlotThreshold' + '_' + 286502,
                    pickUpSpeed: 'alarmLevelSpeedThreshold' + '_' + 286502, //接打电话报警分级速度阈值改
                    pickUpVideoTime: 'videoRecordingTime' + '_' + 286502, //接打电话报警前后视频录制时间
                    pickUpCameraNum: 'photographNumber' + '_' + 286502, //接打电话报警拍驾驶员完整面部特征照片张数
                    pickUpCameraTime: 'photographTime' + '_' + 286502, //接打电话报警拍驾驶员完整面部特征照片间隔时间
                    // 抽烟
                    smokingDecideTime: 'timeSlotThreshold' + '_' + 286503,
                    smokingSpeed: 'alarmLevelSpeedThreshold' + '_' + 286503, //抽烟报警分级车速阈值
                    smokingVideoTime: 'videoRecordingTime' + '_' + 286503, //抽烟报警前后视频录制时间
                    smokingCameraNum: 'photographNumber' + '_' + 286503, //抽烟报警拍驾驶员完整面部特征照片张数
                    smokingCameraTime: 'photographTime' + '_' + 286503, //抽烟报警拍驾驶员完整面部特征照片间隔时间
                    //分神驾驶
                    attentionSpeed: 'alarmLevelSpeedThreshold' + '_' + 286504, //报警分级车速阈值
                    attentionVideoTime: 'videoRecordingTime' + '_' + 286504, //前后视频录制时间
                    attentionCameraNum: 'photographNumber' + '_' + 286504, //报警拍照张数
                    attentionCameraTime: 'photographTime' + '_' + 286504, //报警拍照间隔时间
                    //驾驶员异常
                    driveDeedSpeed: 'alarmLevelSpeedThreshold' + '_' + 286505, //驾驶行为异常分级速度阈值
                    driveDeedVideoTime: 'videoRecordingTime' + '_' + 286505, //视频录制时间
                    driveDeedCameraNum: 'photographNumber' + '_' + 286505, //抓拍照片张数
                    driveDeedCameraTime: 'photographTime' + '_' + 286505, //拍照间隔
                    driveDeedType: 'touchStatus' + '_' + 286507, //驾驶员身份识别触发
                    // 摄像头遮挡
                    occlusionSpeed: 'alarmLevelSpeedThreshold' + '_' + 286508, //摄像头遮挡驾驶报警分级车速阈值
                    occlusionVideoTime: 'videoRecordingTime' + '_' + 286508, //摄像头遮挡驾驶报警前后视频录制时间
                    occlusionCameraNum: 'photographNumber' + '_' + 286508, //摄像头遮挡驾驶报警拍照张数
                    occlusionCameraTime: 'photographTime' + '_' + 286508, //摄像头遮挡驾驶报警拍照时间间隔
                    // 不系安全带
                    safetyBeltSpeed: 'alarmLevelSpeedThreshold' + '_' + 286510, //未系安全带驾驶报警分级车速阈值
                    safetyBeltVideoTime: 'videoRecordingTime' + '_' + 286510, //未系安全带驾驶报警前后视频录制时间
                    safetyBeltCameraNum: 'photographNumber' + '_' + 286510, //未系安全带驾驶报警拍照张数
                    safetyBeltCameraTime: 'photographTime' + '_' + 286510, //未系安全带驾驶报警拍照时间间隔
                    // 红外阻断型墨镜失效
                    blockingSpeed: 'alarmLevelSpeedThreshold' + '_' + 286511, //红外墨镜阻断失效报警触发车速阈值
                    blockingVideoTime: 'videoRecordingTime' + '_' + 286511, //红外墨镜阻断失效报警前后视频录制时间
                    blockingCameraNum: 'photographNumber' + '_' + 286511, //红外墨镜阻断失效报警拍照张数
                    blockingCameraTime: 'photographTime' + '_' + 286511, //红外墨镜阻断失效拍照间隔
                    // 双脱把
                    steeringSpeed: 'alarmLevelSpeedThreshold' + '_' + 286512, //双手离把驾驶报警分级车速阈值
                    steeringtVideoTime: 'videoRecordingTime' + '_' + 286512, //双手离把驾驶报警前后视频录制时间
                    steeringCameraNum: 'photographNumber' + '_' + 286512, //双手离把驾驶报警拍照张数
                    steeringCameraTime: 'photographTime' + '_' + 286512, //双手离把驾驶报警拍照时间间隔
                    // 玩手机
                    playPhoneSpeed: 'alarmLevelSpeedThreshold' + '_' + 286513, //玩手机报警触发车速阈值
                    playPhoneVideoTime: 'videoRecordingTime' + '_' + 286513, //玩手机报警前后视频录制时间
                    playPhoneCameraNum: 'photographNumber' + '_' + 286513, //玩手机报警拍照张数
                    playPhoneCameraTime: 'photographTime' + '_' + 286513, //玩手机报警拍照间隔时间
                },
                62310: {
                    tyreNumber: 'tyreNumberName' + '_' + 286601,
                    unit: 'unit' + '_' + 286601, // 轮胎单位
                    pressure: 'pressure' + '_' + 286601, // 正常胎压值
                    pressureThreshold: 'pressureThreshold' + '_' + 286601, // 胎压不平衡门限
                    slowLeakThreshold: 'slowLeakThreshold' + '_' + 286601, // 慢漏气门限
                    lowPressure: 'lowPressure' + '_' + 286601, // 低压阈值
                    heighPressure: 'highPressure' + '_' + 286601, // 高压阈值
                    highTemperature: 'highTemperature' + '_' + 286601, // 高温阈值
                    electricityThreshold: 'electricityThreshold' + '_' + 286601, // 传感器电量报警阈值
                    uploadTime: 'uploadTime' + '_' + 286601, // 定时上报时间
                },
                62311: {
                    rear: 'rear' + '_' + 286701, //后侧距离阈值
                    sideRear: 'sideRear' + '_' + 286701, //侧后方距离阈值
                },
                61673: {
                    dsmSimilarityThreshold: 'dsmCompareSuccessPercent' + '_' + 282331,
                    phoneSimilarityThreshold: 'phoneCompareSuccessPercent' + '_' + 282331,
                    onOff: 'offlineFaceCompareEnable' + '_' + 282331,
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
            switch (key) {
                case 'onOff':
                    return value == 1 ? '开' : '关'
                case 'alarmEnable':
                    return luParamInfo.parserAlarmValue(value, type);
                case 'videoResolution':
                    return luParamInfo.mapVideoResolutionValue(value);
                case 'cameraResolution':
                    return luParamInfo.mapCameraResolutionValue(value);
                case 'cameraStrategy':
                    return luParamInfo.mapTouchStatusValue(value);
                case 'eventEnable':
                    return luParamInfo.parserEventValue(value, type);
                // case 'tyreNumber':
                //     return luParamInfo.tireModel(value)
                case 'driveDeedType':
                    return luParamInfo.mapDriverStrategy(value);
                case 'unit':
                    return luParamInfo.mapPressureUnit(value);
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
                    0: 'oneLevelAlarmEnable_286407_sensor', // bit0:障碍检测一级报警
                    1: 'twoLevelAlarmEnable_286407_sensor',// bit1:障碍检测二级报警
                    2: 'oneLevelAlarmEnable_286405_sensor',// bit2:频繁变道一级报警
                    3: 'twoLevelAlarmEnable_286405_sensor',// bit3:频繁变道二级报警
                    4: 'oneLevelAlarmEnable_286402_sensor',// bit4:车道偏离一级报警
                    5: 'twoLevelAlarmEnable_286402_sensor',// bit5:车道偏离二级报警
                    6: 'oneLevelAlarmEnable_286401_sensor',// bit6:前向碰撞一级报警
                    7: 'twoLevelAlarmEnable_286401_sensor',// bit7:前向碰撞二级报警
                    8: 'oneLevelAlarmEnable_286404_sensor',// bit8:行人碰撞一级报警
                    9: 'twoLevelAlarmEnable_286404_sensor',// bit9:行人碰撞er级报警
                    10: 'oneLevelAlarmEnable_286403_sensor',// bit10:车距监控一级报警
                    11: 'twoLevelAlarmEnable_286403_sensor',// bit11:车距监控二级报警
                    16: 'roadSignEnable_286406_sensor',// bit16:道路标识超限报警
                    17: 'oneLevelAlarmEnable_286410_sensor',// bit17:实线变道一级报警
                    18: 'twoLevelAlarmEnable_286410_sensor',// bit18:实线变道二级报警
                    19: 'pedestrianInspect_286411_sensor',// bit19:车厢过道行人监测报警
                },
                62309: {
                    0: 'oneLevelAlarmEnable_286501_sensor',// bit0:疲劳一级报警
                    1: 'twoLevelAlarmEnable_286501_sensor',// bit1:疲劳一级报警
                    2: 'oneLevelAlarmEnable_286502_sensor',// bit2:接打手持电话一级报警
                    3: 'twoLevelAlarmEnable_286502_sensor',// bit3:接打手持电话二级报警
                    4: 'oneLevelAlarmEnable_286503_sensor',// bit4:抽烟一级报警
                    5: 'twoLevelAlarmEnable_286503_sensor',// bit5:抽烟二级报警
                    6: 'oneLevelAlarmEnable_286504_sensor',// bit6:注意力分散一级报警
                    7: 'twoLevelAlarmEnable_286504_sensor',// bit7:注意力分散二级报警
                    8: 'oneLevelAlarmEnable_286505_sensor',// bit8:驾驶员状态异常一级报警
                    9: 'twoLevelAlarmEnable_286505_sensor',// bit9:驾驶员状态异常二级报警

                    10: 'oneLevelAlarmEnable_286508_sensor',// bit10：摄像头遮挡一级报警
                    11: 'twoLevelAlarmEnable_286508_sensor',// bit11：摄像头遮挡二级报警
                    12: 'oneLevelAlarmEnable_286510_sensor',// bit12：不系安全带一级报警
                    13: 'twoLevelAlarmEnable_286510_sensor',// bit13：不系安全带二级报警
                    14: 'oneLevelAlarmEnable_286511_sensor',// bit14： 红外墨镜阻断失效一级报警
                    15: 'twoLevelAlarmEnable_286511_sensor',// bit15： 红外墨镜阻断失效二级报警
                    16: 'oneLevelAlarmEnable_286512_sensor',// bit16：双脱把一级报警（双手同时脱离方向盘）
                    17: 'twoLevelAlarmEnable_286512_sensor',// bit17：双脱把二级报警（双手同时脱离方向盘）
                    18: 'oneLevelAlarmEnable_286513_sensor',// bit18： 玩手机一级报警
                    19: 'twoLevelAlarmEnable_286513_sensor',// bit19：玩手机二级报警

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
                    0: 'roadSignRecognition_286406_sensor',
                    1: 'initiativePictureEnable_286406_sensor',
                },
                62309: {
                    0: 'driverChangeEnable_286507_sensor',
                    1: 'initiativePictureEnable_286506_sensor',
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
        mapPressureUnit: function (value) {
            var map = {
                0: 'kg/cm²',
                1: 'bar',
                2: 'Kpa',
                3: 'PSI',
            }
            return map[value] || value
        },
        /**
         * 初始化平台设置参数
         */
        initPlatformData: function () {
            var platformData = JSON.parse($('#platformData').val());
            platformData.forEach(function (item) {
                var commonSetting = item.commonParamSetting
                var adasSetting = item.adasAlarmParamSettings
                var paramType = commonSetting.paramType
                for (var key in commonSetting) {
                    if (commonSetting.hasOwnProperty(key)) {
                        var id = '#' + key + '_' + paramType + '_platform'
                        var dataValue = commonSetting[key]
                        if (key == 'videoResolution' || key == 'cameraResolution') {
                            dataValue = '0x0' + Number(dataValue).toString(16)
                        }
                        $(id).attr('data-value', dataValue)
                        var value = luParamInfo.handleValue(key, commonSetting[key])
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
                            var value = luParamInfo.handleValue(key, item[key])
                            if (key == 'touchStatus') {
                                value = luParamInfo.mapDriverStrategy(item[key])
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
            if (key.indexOf('Enable') != -1 || key.indexOf('Recognition') != -1 || key == 'pedestrianInspect') return value == 1 ? '开' : '关'
            switch (key) {
                case 'videoResolution':
                    return luParamInfo.mapVideoResolutionValue(value)
                case 'cameraResolution':
                    return luParamInfo.mapCameraResolutionValue(value)
                case 'touchStatus':
                    return luParamInfo.mapTouchStatusValue(value)
                case 'timeDistanceThreshold':
                case 'photographTime':
                    return value / 10;
                case 'tyreNumber':
                    return luParamInfo.tireModel(value)
                case 'unit':
                    return luParamInfo.mapPressureUnit(value)
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
            luParamInfo.preventClick($(this));
            isAll = true;
            var data = readParamUtil.getAllData(protocolType);
            luParamInfo.dsmInfoSend(data);
        },
        paramSendClick: function () {
            luParamInfo.preventClick($(this));
            isAll = false;
            var tab = $('.tab-pane.active')
            paramType = tab.data('type');
            var data = readParamUtil.getAllData(protocolType);
            luParamInfo.dsmInfoSend(data.filter(function (item) {
                return item.commonParamSetting.paramType == paramType
            }));
        },


        //下发
        dsmInfoSend: function (jsonData) {
            // 报警为关时不传递其它参数
            jsonData.forEach(function (setting) {
                if (setting.adasAlarmParamSettings) {
                    setting.adasAlarmParamSettings = setting.adasAlarmParamSettings.map(function (item) {
                        if (item.oneLevelAlarmEnable == 0 && item.twoLevelAlarmEnable == 0) {
                            return {
                                oneLevelAlarmEnable: 0,
                                twoLevelAlarmEnable: 0,
                                riskFunctionId: item.riskFunctionId,
                                vehicleId: item.vehicleId,
                            }
                        }
                        if (item.pedestrianInspect == 0) {
                            return {
                                riskFunctionId: item.riskFunctionId,
                                vehicleId: item.vehicleId,
                                pedestrianInspect: 0,
                            }
                        }
                        if (item.roadSignEnable == 0 && item.roadSignRecognition == 0 && item.initiativePictureEnable == 0) {
                            return {
                                riskFunctionId: item.riskFunctionId,
                                vehicleId: item.vehicleId,
                                roadSignEnable: 0,
                                roadSignRecognition: 0,
                                initiativePictureEnable: 0,
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
            json_ajax("POST", "/clbs/adas/standard/param/setting.gsp", "json", false, data, luParamInfo.dsmInfoSendCallback);
        },
        dsmInfoSendCallback: function (data) {
            if (data != null) {
                if (data.success) {
                    layer.load(2);
                    var tab = $('.tab-pane.active').index();
                    $("#dsmPoilSendStatus" + tab).val("参数已下发");

                    //获取下发状态
                    setTimeout(function () {
                        timerInterval = setInterval(luParamInfo.getStatus(), 5000);
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
                                $(item).find('.sendStatus').val(luParamInfo.statusChange(obj[key]))
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
                            luParamInfo.getPeripheralInfo(sensorID);
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
            luParamInfo.initPlatformData();

            //传感器参数
            for (var i = 0; i < socketTypes.length; i++) {
                var sensorID = sensorIDs[i];
                luParamInfo.getPeripheralInfo(sensorID);//传感器参数下发
            }
        },

    };
    $(function () {
        $('input').inputClear();
        luParamInfo.init();
        luParamInfo.initPlatformData();

        //事件
        $("#dsmInfoSend").on("click", luParamInfo.paramSendAllClick);//全部修正下发
        $("#dsmInfoSend2").on("click", luParamInfo.paramSendClick);//本页签修正下发
        $("#dsmInfoRefresh").on("click", luParamInfo.readConventionalRefreshClick);//刷新
    })
}($, window))