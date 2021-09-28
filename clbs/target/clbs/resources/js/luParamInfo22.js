//# sourceURL=luParamInfo.js
(function ($, window) {
    var vid = $("#vehicleId").val();//车辆id
    var _timeout;
    var isRead = true;
    var timerInterval;

    var isAll = false;
    var paramType = 64,
        protocolType = 26;//中位
    //获取参数设置信息
    var sensorIDs = [100, 101, 102, 103, 233];//[高级驾驶辅助, 驾驶员状态监测, 胎压监测, 盲区监测, 驾驶员比对]
    var socketTypes = [62308, 62309, 62310, 62311, 61673];//[高级驾驶辅助, 驾驶员状态监测, 胎压监测, 盲区监测, 驾驶员比对]
    var paramTypes = [64, 65, 66, 67, 233];//[高级驾驶辅助, 驾驶员状态监测, 胎压监测, 盲区监测, 驾驶员比对]

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
        {index: 14, name: '驾驶员身份识别策略', id: 'touchStatus', riskFunctionId: '266511'},

        {index: 9, name: '离线人脸比对开关', id: 'offlineFaceCompareEnable'},
        {index: 10, name: 'DSM人脸比对成功阈值(%)', id: 'dsmCompareSuccessPercent'},
        {index: 11, name: '手机人脸比对成功阈值(%)', id: 'phoneCompareSuccessPercent'},
        {index: 15, name: '定时拍照间隔(s)', id: 'timingPhotoInterval'},
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
        {index: 13, name: '驾驶员更换事件', id: 'driverChangeEnable', riskFunctionId: '266511'},

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
        {index: 27, name: '主动拍照事件', id: 'initiativePictureEnable', riskFunctionId: '266510'},

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
                    data: util.selectData([1, 2, 3, 4, 5, 15, 6, 7, 8], commonData)
                },
                {
                    label: '障碍物检测',
                    riskFunctionId: '266407',
                    data: util.selectData([1, 2, 3, 4, 5, 6, 7], adasData)
                },
                {
                    label: '频繁变道',
                    riskFunctionId: '266405',
                    data: util.selectData([1, 2, 8, 9, 4, 5, 6, 7], adasData)
                },
                {
                    label: '车道偏离',
                    riskFunctionId: '266402',
                    data: util.selectData([1, 2, 4, 5, 6, 7], adasData)
                },
                {
                    label: '向前碰撞',
                    riskFunctionId: '266401',
                    data: util.selectData([1, 2, 3, 4, 5, 6, 7], adasData)
                },
                {
                    label: '行人碰撞',
                    riskFunctionId: '266404',
                    data: util.selectData([1, 2, 3, 4, 5, 6, 7], adasData)
                },
                {
                    label: '车距过近',
                    riskFunctionId: '266403',
                    data: util.selectData([1, 2, 3, 4, 5, 6, 7], adasData)
                },
                {
                    label: '道路标志',
                    riskFunctionId: '266406',
                    data: util.selectData([10, 11, 12, 6, 7], adasData)
                },
            ]
        },
        {
            domId: 'driverStatus',
            allData: [
                {
                    label: '全局参数',
                    riskFunctionId: '65',
                    data: util.selectData([1, 2, 3, 14, 4, 13, 12, 7, 6, 8], commonData)
                },
                {
                    label: '疲劳驾驶',
                    riskFunctionId: '266501',
                    data: util.selectData([1, 2, 4, 5, 6, 7], adasData)
                },
                {
                    label: '接打手持电话',
                    riskFunctionId: '266502',
                    data: util.selectData([1, 2, 26, 4, 5, 6, 7], adasData)
                },
                {
                    label: '抽烟',
                    riskFunctionId: '266503',
                    data: util.selectData([1, 2, 26, 4, 5, 6, 7], adasData)
                },
                {
                    label: '分神驾驶',
                    riskFunctionId: '266504',
                    data: util.selectData([1, 2, 4, 5, 6, 7], adasData)
                },
                {
                    label: '驾驶员异常',
                    riskFunctionId: '266505',
                    data: util.selectData([1, 2, 4, 5, 6, 7], adasData)
                },
                {
                    label: '事件使能',
                    data: util.selectData([13, 27], adasData)
                },
            ]
        },
        {
            domId: 'tirePressure',
            allData: [
                {
                    label: '胎压监测',
                    riskFunctionId: '266601',
                    data: util.selectData([14, 15, 16, 17, 18, 19, 20, 21, 22, 23], adasData)
                }
            ]
        },
        {
            domId: 'blindSpot',
            allData: [
                {
                    label: '盲点监测',
                    riskFunctionId: '266701',
                    data: util.selectData([24, 25], adasData)
                }
            ]
        },
        {
            domId: 'driverCompare',
            allData: [
                {
                    label: '驾驶员比对',
                    riskFunctionId: '262331',
                    data: util.selectData([9, 10, 11], commonData)
                }
            ]
        },
    ]

    luParamInfo = {
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
                61673: 'param'
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
                    obstacleDistance: 'timeDistanceThreshold' + '_' + 266407,// 时间阈值
                    obstacleSpeed: 'alarmLevelSpeedThreshold' + '_' + 266407, // 障碍物预警分级速度阈值
                    obstacleVideoTime: 'videoRecordingTime' + '_' + 266407, // 障碍物预警前后视频录制时间
                    obstacleCameraNum: 'photographNumber' + '_' + 266407, // 障碍物预警拍照张数
                    obstacleCameraTime: 'photographTime' + '_' + 266407, // 障碍物预警拍照间隔时间
                    //频繁变道
                    laneChangeTime: 'timeSlotThreshold' + '_' + 266405, //频繁变道预警判断时间段
                    laneChangeNum: 'frequencyThreshold' + '_' + 266405, //频繁变道预警判断次数
                    laneChangeSpeed: 'alarmLevelSpeedThreshold' + '_' + 266405, //频繁变道预警分级速度阈值
                    laneChangeVideoTime: 'videoRecordingTime' + '_' + 266405, //繁变道预警前后视频录制时间
                    laneChangeCameraNum: 'photographNumber' + '_' + 266405, //频繁变道预警拍照片张数
                    laneChangeCameraTime: 'photographTime' + '_' + 266405, //频繁变道预警拍照间隔
                    //车道偏离
                    deviateSpeed: 'alarmLevelSpeedThreshold' + '_' + 266402, //车道偏离预警分级速度阈值
                    deviateVideoTime: 'videoRecordingTime' + '_' + 266402, //车道偏离预警前后视频录制时间
                    deviateCameraNum: 'photographNumber' + '_' + 266402, //车道偏离预警拍照片张数
                    deviateCameraTime: 'photographTime' + '_' + 266402, //车道偏离预警拍照间隔
                    //前车碰撞
                    vehicleCollisionTime: 'timeDistanceThreshold' + '_' + 266401, //前车碰撞预警时间阈值
                    vehicleCollisionSpeed: 'alarmLevelSpeedThreshold' + '_' + 266401, //前车碰撞报警分级速度阈值
                    vehicleCollisionVideoTime: 'videoRecordingTime' + '_' + 266401, //前车碰撞预警前后视频录制时间
                    vehicleCollisionCameraNum: 'photographNumber' + '_' + 266401, //前车碰撞预警拍照片张数
                    vehicleCollisionCameraTime: 'photographTime' + '_' + 266401, //行人碰撞预警拍照间隔
                    //行人碰撞
                    pedestrianCollisionTime: 'timeDistanceThreshold' + '_' + 266404, //行人碰撞预警时间阈值
                    pedestrianCollisionSpeed: 'alarmLevelSpeedThreshold' + '_' + 266404, //行人碰撞预警分级速度阈值
                    pedestrianCollisionVideoTime: 'videoRecordingTime' + '_' + 266404, //行人碰撞预警前后视频录制时间
                    pedestrianCollisionCameraNum: 'photographNumber' + '_' + 266404, //行人碰撞预警拍照片张数
                    pedestrianCollisionCameraTime: 'photographTime' + '_' + 266404, //行人碰撞预警拍照间隔
                    // 车距过近
                    distanceMail: 'timeDistanceThreshold' + '_' + 266403, //车距监控报警距离阈值
                    distanceSpeed: 'alarmLevelSpeedThreshold' + '_' + 266403, //车距监控报警分级速度阈值
                    distanceVideoTime: 'videoRecordingTime' + '_' + 266403, //车距过近报警前后视频录制时间
                    distanceCameraNum: 'photographNumber' + '_' + 266403, //车距过近报警拍照张数
                    distanceCameraTime: 'photographTime' + '_' + 266403, //车距过近报警拍照间隔
                    // 道路标志
                    speedLimitCameraNum: 'photographNumber' + '_' + 266406, //道路标志识别拍照张数
                    speedLimitCameraTime: 'photographTime' + '_' + 266406, //道路标志识别拍照间隔
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
                    fatigueSpeed: 'alarmLevelSpeedThreshold' + '_' + 266501, //疲劳驾驶报警分级速度阈值
                    fatigueVideoTime: 'videoRecordingTime' + '_' + 266501, //疲劳驾驶报警前后视频录制时
                    fatigueCameraNum: 'photographNumber' + '_' + 266501, //疲劳驾驶报警拍照张数
                    fatigueCameraTime: 'photographTime' + '_' + 266501, //疲劳驾驶报警拍照间隔时间
                    // 接打电话
                    pickUpDecideTime: 'timeSlotThreshold' + '_' + 266502, //
                    pickUpSpeed: 'alarmLevelSpeedThreshold' + '_' + 266502, //接打电话报警分级速度阈值改
                    pickUpVideoTime: 'videoRecordingTime' + '_' + 266502, //接打电话报警前后视频录制时间
                    pickUpCameraNum: 'photographNumber' + '_' + 266502, //接打电话报警拍驾驶员完整面部特征照片张数
                    pickUpCameraTime: 'photographTime' + '_' + 266502, //接打电话报警拍驾驶员完整面部特征照片间隔时间
                    // 抽烟
                    smokingDecideTime: 'timeSlotThreshold' + '_' + 266503, //
                    smokingSpeed: 'alarmLevelSpeedThreshold' + '_' + 266503, //抽烟报警分级车速阈值
                    smokingVideoTime: 'videoRecordingTime' + '_' + 266503, //抽烟报警前后视频录制时间
                    smokingCameraNum: 'photographNumber' + '_' + 266503, //抽烟报警拍驾驶员完整面部特征照片张数
                    smokingCameraTime: 'photographTime' + '_' + 266503, //抽烟报警拍驾驶员完整面部特征照片间隔时间
                    //分神驾驶
                    attentionSpeed: 'alarmLevelSpeedThreshold' + '_' + 266504, //报警分级车速阈值
                    attentionVideoTime: 'videoRecordingTime' + '_' + 266504, //前后视频录制时间
                    attentionCameraNum: 'photographNumber' + '_' + 266504, //报警拍照张数
                    attentionCameraTime: 'photographTime' + '_' + 266504, //报警拍照间隔时间
                    //驾驶员异常
                    driveDeedSpeed: 'alarmLevelSpeedThreshold' + '_' + 266505, //驾驶行为异常分级速度阈值
                    driveDeedVideoTime: 'videoRecordingTime' + '_' + 266505, //视频录制时间
                    driveDeedCameraNum: 'photographNumber' + '_' + 266505, //抓拍照片张数
                    driveDeedCameraTime: 'photographTime' + '_' + 266505, //拍照间隔
                    driveDeedType: 'touchStatus' + '_' + 266511, //驾驶员身份识别触发
                },
                62310: {
                    tyreNumber: 'tyreNumberName' + '_' + 266601,
                    unit: 'unit' + '_' + 266601, // 轮胎单位
                    pressure: 'pressure' + '_' + 266601, // 正常胎压值
                    pressureThreshold: 'pressureThreshold' + '_' + 266601, // 胎压不平衡门限
                    slowLeakThreshold: 'slowLeakThreshold' + '_' + 266601, // 慢漏气门限
                    lowPressure: 'lowPressure' + '_' + 266601, // 低压阈值
                    heighPressure: 'highPressure' + '_' + 266601, // 高压阈值
                    highTemperature: 'highTemperature' + '_' + 266601, // 高温阈值
                    electricityThreshold: 'electricityThreshold' + '_' + 266601, // 传感器电量报警阈值
                    uploadTime: 'uploadTime' + '_' + 266601, // 定时上报时间
                },
                62311: {
                    rear: 'rear' + '_' + 266701, //后侧距离阈值
                    sideRear: 'sideRear' + '_' + 266701, //侧后方距离阈值
                },
                61673: {
                    dsmSimilarityThreshold: 'dsmCompareSuccessPercent' + '_' + 262331,
                    phoneSimilarityThreshold: 'phoneCompareSuccessPercent' + '_' + 262331,
                    onOff: 'offlineFaceCompareEnable' + '_' + 262331,
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
                    0: 'oneLevelAlarmEnable_266407_sensor', // bit0:障碍检测一级报警
                    1: 'twoLevelAlarmEnable_266407_sensor',// bit1:障碍检测二级报警
                    2: 'oneLevelAlarmEnable_266405_sensor',// bit2:频繁变道一级报警
                    3: 'twoLevelAlarmEnable_266405_sensor',// bit3:频繁变道二级报警
                    4: 'oneLevelAlarmEnable_266402_sensor',// bit4:车道偏离一级报警
                    5: 'twoLevelAlarmEnable_266402_sensor',// bit5:车道偏离二级报警
                    6: 'oneLevelAlarmEnable_266401_sensor',// bit6:前向碰撞一级报警
                    7: 'twoLevelAlarmEnable_266401_sensor',// bit7:前向碰撞二级报警
                    8: 'oneLevelAlarmEnable_266404_sensor',// bit2:行人碰撞二级报警
                    9: 'twoLevelAlarmEnable_266404_sensor',// bit9:行人碰撞二级报警
                    10: 'oneLevelAlarmEnable_266403_sensor',// bit10:车距监控一级报警
                    11: 'twoLevelAlarmEnable_266403_sensor',// bit11:车距监控二级报警
                    16: 'roadSignEnable_266406_sensor',// bit16:道路标识超限报警
                },
                62309: {
                    0: 'oneLevelAlarmEnable_266501_sensor',// bit0:疲劳一级报警
                    1: 'twoLevelAlarmEnable_266501_sensor',// bit1:疲劳一级报警
                    2: 'oneLevelAlarmEnable_266502_sensor',// bit2:接打手持电话一级报警
                    3: 'twoLevelAlarmEnable_266502_sensor',// bit3:接打手持电话二级报警
                    4: 'oneLevelAlarmEnable_266503_sensor',// bit4:抽烟一级报警
                    5: 'twoLevelAlarmEnable_266503_sensor',// bit5:抽烟二级报警
                    6: 'oneLevelAlarmEnable_266504_sensor',// bit6:注意力分散一级报警
                    7: 'twoLevelAlarmEnable_266504_sensor',// bit7:注意力分散二级报警
                    8: 'oneLevelAlarmEnable_266505_sensor',// bit8:驾驶员状态异常一级报警
                    9: 'twoLevelAlarmEnable_266505_sensor',// bit9:驾驶员状态异常二级报警
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
                    0: 'roadSignRecognition_266406_sensor',
                    1: 'initiativePictureEnable_266406_sensor',
                },
                62309: {
                    0: 'driverChangeEnable_266511_sensor',
                    1: 'initiativePictureEnable_266510_sensor',
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
        mapPressureUnit: function (value) {
            var map = {
                0: 'kg/cm²',
                1: 'bar',
                2: 'Kpa',
                3: 'PSI',
            }
            return map[value] || value
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
            if (key.indexOf('Enable') != -1 || key.indexOf('Recognition') != -1) return value == 1 ? '开' : '关'
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
            var data = luParamInfo.getAllData();
            luParamInfo.dsmInfoSend(data);
        },
        paramSendClick: function () {
            luParamInfo.preventClick($(this));
            isAll = false;
            var tab = $('.tab-pane.active')
            paramType = tab.data('type');
            var data = luParamInfo.getAllData();
            luParamInfo.dsmInfoSend(data.filter(function (item) {
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

            return luParamInfo.parserData(tempObj)
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
                        return item.commonParamSetting && item.commonParamSetting.paramType == key.substr(2, 2)
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
                        if (item.oneLevelAlarmEnable == 0 && item.twoLevelAlarmEnable == 0) {
                            return {
                                oneLevelAlarmEnable: 0,
                                twoLevelAlarmEnable: 0,
                                riskFunctionId: item.riskFunctionId,
                                vehicleId: item.vehicleId,
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
                        if (item.offlineFaceCompareEnable == 0) {
                            return {
                                riskFunctionId: item.riskFunctionId,
                                vehicleId: item.vehicleId,
                                offlineFaceCompareEnable: 0,
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
            // luParamInfo.initPlatformData();

            //传感器参数
            for (var i = 0; i < socketTypes.length; i++) {
                var sensorID = sensorIDs[i];
                luParamInfo.getPeripheralInfo(sensorID);//传感器参数下发
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
        luParamInfo.init();
        luParamInfo.initPlatformData();

        //事件
        $(".info-span").on("click", luParamInfo.hiddenparameterFn);//点击显示隐藏信息
        $("#dsmInfoSend").on("click", luParamInfo.paramSendAllClick);//全部修正下发
        $("#dsmInfoSend2").on("click", luParamInfo.paramSendClick);//本页签修正下发
        $("#dsmInfoRefresh").on("click", luParamInfo.readConventionalRefreshClick);//刷新
        $(".sensorBtn").on("click", luParamInfo.sensorBtnClick);//以传感器为准
        $(".platformBtn").on("click", luParamInfo.platformBtnClick);//以平台设置为准
    })
}($, window))