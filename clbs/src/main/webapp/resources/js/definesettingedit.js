//# sourceURL=definesettingedit.js

(function (window, $) {
    var allLowSpeed = ""; //统一的低速
    var fatiguePValue = "";//疲劳报警时间间隔
    var fatigueTValue = "";//疲劳报警-持续时长
    var distractPValue = "";//分心报警时间间隔
    var distractTValue = ""; //分心报警-持续时长
    var collisionPValue = ""; //碰撞危险-时间间隔
    var collisionTValue = "";	// 碰撞危险-持续时长
    var abnormalPValue = "";//违规异常-时间间隔
    var abnormalTValue = ""; //违规异常-持续时长
    var collisionRiskValue = "";//碰撞危险的值
    var checkspeedvalue = true;


    var riskSize0To10Length = '必须为0~10的数字';
    var riskSize05To2Length = '必须为0.5~2的数字';
    var riskSize0To220Length = '必须为0~220的数字';
    riskdefineSet = {
        init: function () {
            $(".modal-body").addClass("modal-body-overflow");

            // 初始化低速阈值，高速阈值等级下拉框
            var riskLevelList = JSON.parse($("#riskLevelList").attr("value")); // 风险等级
            if (riskLevelList != null && riskLevelList != undefined && riskLevelList.length > 0) {
                var option = '';
                var lowSpeedOption = '';
                for (var i = 0; i < riskLevelList.length; i++) {
                    var level = riskLevelList[i];

                    if (level.riskValue == '7') { //高速阈值有默认值
                        option += "<option value =" + level.riskValue + " selected>" + level.riskLevel + "</option>";
                    } else {
                        option += "<option value =" + level.riskValue + ">" + level.riskLevel + "</option>";
                    }

                    lowSpeedOption += "<option value =" + level.riskValue + ">" + level.riskLevel + "</option>";
                }
                // $('#closeEyes_low_select,#closeEyes_high_select,#yawn_low_select,#yawn_high_select,#smoking_low_select,' +
                //     '#smoking_high_select,#answerThephone_low_select,#answerThephone_high_select,#posture-low-speed-limit,'+
                // '#posture-high-speed-limit,#forwardCollision_low_select,#forwardCollision_high_select,#laneDeparture_low_select,'+
                // '#laneDeparture_high_select,#tooClose_low_select,#tooClose_high_select,#pedestrianCollision_low_select,'+
                // '#pedestrianCollision_high_select,#frequentLaneChange_low_select,#frequentLaneChange_high_select,#block_low_select,'+
                // '#block_high_select,#hurrytoadd-speed-limit,#hurrytoadd-high-speed-limit').html(option);

                $('#closeEyes_low_select,#yawn_low_select,#smoking_low_select,' +
                    '#answerThephone_low_select,#posture_low_select,' +
                    '#forwardCollision_low_select,#laneDeparture_low_select,' +
                    '#tooClose_low_select,#pedestrianCollision_low_select,' +
                    '#frequentLaneChange_low_select,#block_low_select,#cover_low_select,#infrared_low_select,' +
                    '#hurry_low_select').html(lowSpeedOption);

                $('#closeEyes_high_select,#yawn_high_select,' +
                    '#smoking_high_select,#answerThephone_high_select,' +
                    '#posture_high_select,#forwardCollision_high_select,' +
                    '#laneDeparture_high_select,#tooClose_high_select,' +
                    '#pedestrianCollision_high_select,#frequentLaneChange_high_select,#cover_high_select,#infrared_high_select,' +
                    '#block_high_select,#hurry_high_select').html(option);

            }

            //是否勾选
            var riskSettingList = $("#riskSettingList").val();
            if (riskSettingList != null && riskSettingList != undefined && riskSettingList != "[]") {
                riskdefineSet.initCheckbox(riskSettingList);
                // riskdefineSet.timeIntervalAndLowSpeedValue();
            }


            var referVehicleList = JSON.parse($("#referVehicleList").attr("value"));
            // 初始化车辆数据
            var dataList = {value: []};
            if (referVehicleList != null && referVehicleList.length > 0) {
                var brands = $("#brand").val();
                for (var i = 0; i < referVehicleList.length; i++) {
                    var obj = {};
                    //删除相同车牌信息
                    if (referVehicleList[i].brand == brands) {
                        referVehicleList.splice(referVehicleList[i].brand.indexOf(brands), 1);
                    }
                    //处理参考车牌只存在一条数据时防止浏览器抛出错误信息
                    if (referVehicleList[i] == undefined) {
                        dataList.value.push(obj);
                    } else {
                        obj.id = referVehicleList[i].vehicleId;
                        obj.name = referVehicleList[i].brand;
                        dataList.value.push(obj);
                    }
                }
            }
            $("#riskReference").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: dataList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                // 当选择参考车牌
                var vehicleId = keyword.id;
                $.ajax({
                    type: 'GET',
                    url: '/clbs/r/riskManagement/DefineSettings/getParameter_' + vehicleId + '.gsp',
                    async: false,
                    dataType: 'json',
                    success: function (data) {
                        if (data.success) {
                            riskdefineSet.clearValues();
                            $("#riskReference").val(keyword.key);
                            //将全局参数置为空
                            riskdefineSet.emptyParameters();
                            riskdefineSet.initCheckbox(data.msg);
                            // riskdefineSet.timeIntervalAndLowSpeedValue();
                        } else {
                            layer.msg("获取参考车牌设置数据失败");
                        }
                    },
                    error: function () {
                        layer.msg(systemError, {move: false});
                    }
                });
            }).on('onUnsetSelectValue', function () {
            });
        },
        emptyParameters: function () {
            allLowSpeed = ""; //统一的低速
            fatiguePValue = "";//疲劳报警时间间隔
            fatigueTValue = "";//疲劳报警-持续时长
            distractPValue = "";//分心报警时间间隔
            distractTValue = ""; //分心报警-持续时长
            collisionPValue = ""; //碰撞危险-时间间隔
            collisionTValue = "";	// 碰撞危险-持续时长
            abnormalPValue = "";//违规异常-时间间隔
            abnormalTValue = ""; //违规异常-持续时长
            collisionRiskValue = "";//碰撞危险的值
        },
        hiddenparameterFn: function () {
            var clickId = $(this).context.id;
            if (!($("#" + clickId + "-content").is(":hidden"))) {
                $("#" + clickId + "-content").slideUp();
                $("#" + clickId).children("font").text("显示更多");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-down");
            } else {
                $("#" + clickId + "-content").slideDown();
                $("#" + clickId).children("font").text("隐藏参数");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-up");
            }
        },
        combinationJsonDate: function () {//组合json数据
            var dataArray = new Array();
            var jsonStr6506 = riskdefineSet.checkboxEncapsulationParam("6506", "closeEyes", '65');//闭眼
            var jsonStr6507 = riskdefineSet.checkboxEncapsulationParam("6507", "yawn", '65'); //打哈欠
            var jsonStr6503 = riskdefineSet.checkboxEncapsulationParam("6503", "smoking", '65'); // 抽烟
            var jsonStr6502 = riskdefineSet.checkboxEncapsulationParam("6502", "answerThephone", '65'); // 接打手持电话
            var jsonStr6509 = riskdefineSet.checkboxEncapsulationParam("6509", "witness", '65');//人证不符
            var jsonStr6510 = riskdefineSet.checkboxEncapsulationParam("6510", "driver", '65');//未检测到驾驶员
            var jsonStr6508 = riskdefineSet.checkboxEncapsulationParam("6508", "posture", '65');//姿态异常

            var jsonStr6511 = riskdefineSet.checkboxEncapsulationParam("6511", "cover", '65');//遮挡
            var jsonStr6512 = riskdefineSet.checkboxEncapsulationParam("6512", "infrared", '65');//红外阻断


            var jsonStr6401 = riskdefineSet.checkboxEncapsulationParam("6401", "forwardCollision", '64');//前向碰撞
            var jsonStr6402 = riskdefineSet.checkboxEncapsulationParam("6402", "laneDeparture", '64');//车道偏移
            var jsonStr6403 = riskdefineSet.checkboxEncapsulationParam("6403", "tooClose", '64');//车距过近
            var jsonStr6404 = riskdefineSet.checkboxEncapsulationParam("6404", "pedestrianCollision", '64');//行人碰撞
            var jsonStr6405 = riskdefineSet.checkboxEncapsulationParam("6405", "frequentLaneChange", '64');//频繁变道
            var jsonStr6407 = riskdefineSet.checkboxEncapsulationParam("6407", "block", '64');//障碍物
            var jsonStr6408 = riskdefineSet.checkboxEncapsulationParam("6408", "hurry", '64');//急加急减急转弯
            var jsonStr6409 = riskdefineSet.checkboxEncapsulationParam("6409", "roadIdentifying", '64');//道路标识超限
            var jsonStr6410 = riskdefineSet.checkboxEncapsulationParam("6410", "roadrecognition", '64');//道路标识识别

            var arrayi = 0;
            if (!riskdefineSet.isEmptyObject(jsonStr6510)) {
                dataArray[arrayi++] = jsonStr6510;
            }
            if (!riskdefineSet.isEmptyObject(jsonStr6408)) {
                dataArray[arrayi++] = jsonStr6408;
            }
            if (!riskdefineSet.isEmptyObject(jsonStr6409)) {
                dataArray[arrayi++] = jsonStr6409;
            }
            if (!riskdefineSet.isEmptyObject(jsonStr6410)) {
                dataArray[arrayi++] = jsonStr6410;
            }
            if (!riskdefineSet.isEmptyObject(jsonStr6506)) {
                dataArray[arrayi++] = jsonStr6506;
            }
            if (!riskdefineSet.isEmptyObject(jsonStr6509)) {
                dataArray[arrayi++] = jsonStr6509;
            }
            if (!riskdefineSet.isEmptyObject(jsonStr6507)) {
                dataArray[arrayi++] = jsonStr6507;
            }
            if (!riskdefineSet.isEmptyObject(jsonStr6508)) {
                dataArray[arrayi++] = jsonStr6508;
            }
            if (!riskdefineSet.isEmptyObject(jsonStr6503)) {
                dataArray[arrayi++] = jsonStr6503;
            }
            if (!riskdefineSet.isEmptyObject(jsonStr6502)) {
                dataArray[arrayi++] = jsonStr6502;
            }
            if (!riskdefineSet.isEmptyObject(jsonStr6401)) {
                dataArray[arrayi++] = jsonStr6401;
            }
            if (!riskdefineSet.isEmptyObject(jsonStr6404)) {
                dataArray[arrayi++] = jsonStr6404;
            }
            if (!riskdefineSet.isEmptyObject(jsonStr6403)) {
                dataArray[arrayi++] = jsonStr6403;
            }
            if (!riskdefineSet.isEmptyObject(jsonStr6405)) {
                dataArray[arrayi++] = jsonStr6405;
            }
            if (!riskdefineSet.isEmptyObject(jsonStr6402)) {
                dataArray[arrayi++] = jsonStr6402;
            }
            if (!riskdefineSet.isEmptyObject(jsonStr6407)) {
                dataArray[arrayi++] = jsonStr6407;
            }

            if (!riskdefineSet.isEmptyObject(jsonStr6511)) {
                dataArray[arrayi++] = jsonStr6511;
            }
            if (!riskdefineSet.isEmptyObject(jsonStr6512)) {
                dataArray[arrayi++] = jsonStr6512;
            }


            return dataArray;
        },
        isEmptyObject: function (obj) {
            for (var name in obj) {
                return false;//返回false，不为空对象
            }
            return true;//返回true，为空对象
        },
        checkboxEncapsulationParam: function (id, name, adasOrdsm) {//封装参数
            var param = {};
            var _name = name;
            var checkBox = "#" + name + "_multiMediaSet"

            //每条都传拍照分辨率，视频分辨率，提示音量，灵敏度
            if ($('#photo_video_volume_sensitivity').is(":checked")) {
                param.cameraResolution = $('#camera_resolution_select_065').val()  //拍照分辨率
                param.videoResolution = $('#video_resolution_select_065').val()  //视频分辨率
                param.alarmVolume = $('#video_volume_select').val()   //提示音量
                param.sensitivity = $('#video_sensitivity_select').val() //灵敏度
            }

            if (adasOrdsm == '64') { //碰撞危险相关
                // adas多媒体参数设置
                if ($('#adas-check').is(":checked")) {
                    param.timingPhotoInterval = $('#capture_time_text').val()  //定时拍照时间间隔
                    param.distancePhotoInterval = $('#distancePhotoInterval').val()  //定距拍照距离间隔
                    param.timingPhoto = $('#timingPhoto').val()   //拍照张数
                    param.dsmAdasTimeInterval = $('#adastimeInterval').val() //拍照间隔
                }
                // adas主动抓拍设置
                if ($('#captureAdasSetting').is(":checked")) {
                    param.initiativeCaptureAlarmEnable = $('#capture-select').val()  //主动抓拍
                    param.initiativeCaptureVoiceEnable = $('#adas-voice-select').val()  //语音提醒
                    param.timingCapture = $('#timingCapture').val()   //定时抓拍
                    param.distanceCapture = $('#distanceCapture').val() //定距抓拍
                }
            } else if (adasOrdsm == '65') { //疲劳驾驶，注意力分散，违规异常
                // dsm多媒体参数设置
                if ($('#dsm-check').is(":checked")) {
                    param.timingPhotoInterval = $('#dsm_capture_time_text').val()  //定时拍照时间间隔
                    param.distancePhotoInterval = $('#dsm_distancePhotoInterval').val()  //定距拍照距离间隔
                    param.timingPhoto = $('#dsm_timingPhoto').val()   //拍照张数
                    param.dsmAdasTimeInterval = $('#dsm_timeInterval').val() //拍照间隔
                }
                // dsm主动抓拍设置
                if ($('#dsm_captureCheck').is(":checked")) {
                    param.initiativeCaptureAlarmEnable = $('#capture-select-dsm').val()  //主动抓拍
                    param.initiativeCaptureVoiceEnable = $('#adas-voice-select-dsm').val()  //语音提醒
                    param.timingCapture = $('#timingCapture-dsm').val()   //定时抓拍
                    param.distanceCapture = $('#distanceCapture-dsm').val() //定距抓拍
                }
            }
            param.vehicleId = $('#vehicleId').val();
            param.riskId = id;

            //所有事件的低速
            if ($('#allLowSpeed_checkbox').is(":checked")) {
                param.lowSpeed = $('#all_lowSpeed').val();
            }
            // 多媒体参数设置
            if ($(checkBox).is(":checked")) {
                param.videoRecordingTime = $('#' + _name + '_video_text').val(); //录制时间
                param.photographNumber = $('#' + _name + '_photo_text').val(); //拍照张数
                param.photographTime = $('#' + _name + '_photo_time_text').val(); //拍照间隔
            }
            var riskLevelCheckbox = "#" + name + "_RiskLevelSet";

            if ($(riskLevelCheckbox).is(":checked")) {
                //闭眼，打哈欠   抽烟，接打电话  姿态异常 前车碰撞 车道偏离 车距过近 行人碰撞 频繁变道 障碍物 急加急减急转弯
                if (id == '6506' || id == '6507' || id == '6503' || id == '6502' || id == '6508' || id == '6511' || id == '6512'
                    || id == '6401' || id == '6402' || id == '6403' || id == '6404' || id == '6405' || id == '6407' || id == '6408') {
                    param.oneLevelAlarmEnable = $('#' + name + '_oneLevelAlarmEnable').val(); //一级报警
                    param.twoLevelAlarmEnable = $('#' + name + '_twoLevelAlarmEnable').val(); //二级报警
                    param.lowSpeedLevel = $('#' + name + '_low_select').val(); //低速阈值
                    // param.lowSpeedLevel = $('#'+name+'_lowSpeedLevel').val(); //低速阈值
                    param.highSpeedLevel = $('#' + name + '_high_select').val(); //高速阈值
                    // param.highSpeedLevel = $('#'+name+'_highSpeedLevel').val(); //高速阈值
                    param.lowSpeedRecording = $('#' + name + '_lowSpeedRecording').val(); //低速录制视频
                    param.highSpeedRecording = $('#' + name + '_highSpeedRecording').val(); //高速录制视频
                    param.oneLevelVoiceEnable = $('#' + name + '_oneLevelVoiceEnable').val(); //一级语音提醒
                    param.twoLevelVoiceEnable = $('#' + name + '_twoLevelVoiceEnable').val(); //二级语音提醒
                }
                // 闭眼，打哈欠
                if (id == '6506' || id == '6507') {
                    param.fatigueP = $('#time-interval').val(); //时间间隔
                    param.fatigueT = $('#time-continue').val(); //持续时长
                }
                // 抽烟，接打手持电话
                if (id == '6503' || id == '6502' || id == '6508') {
                    param.timeInterval = $('#' + name + '_timeInterval').val(); //抽烟,接打手持电话的时间间隔

                    param.distractP = $('#drive_time_interval').val(); //时间间隔
                    param.distractT = $('#drive_continue_time').val(); //持续时长
                }
                //异常报警时间间隔和持续时长
                if (id == '6509' || id == '6510' || id == '6511' || id == '6512') {
                    param.abnormalP = $('#driver-detection_time_text').val(); //时间间隔
                    param.abnormalT = $('#driver-detection_time_duration').val(); //持续时长
                }

                if (id == '6401' || id == '6402' || id == '6403' || id == '6404' || id == '6405' || id == '6407' || id == '6408' || id == '6409' || id == '6410') {

                    param.collisionP = $('#collisionPValue').val(); //时间间隔
                    param.collisionT = $('#collisionTValue').val(); //持续时长
                }

                // 人证不符,驾驶员不在驾驶位置
                if (id == '6509' || id == '6510') {
                    param.checkSwitch = $('#' + name + '_check_switch').val(); //定时检查
                    param.voiceEnable = $('#' + name + '_voiceEnable').val(); //人证不符语音提醒

                    param.lowSpeedRecording = $('#' + name + '_lowSpeedRecording').val(); //低速录制视频
                    param.highSpeedRecording = $('#' + name + '_highSpeedRecording').val(); //高速录制视频
                }

                // 时间阈值
                if (id == '6509' || id == '6401' || id == '6403' || id == '6404' || id == '6405' || id == '6407') {
                    param.timeInterval = $('#' + name + '_timeInterval').val();//时间阈值
                }
                //道路标识超限
                if (id == '6409') {
                    param.oneLevelAlarmEnable = $('#' + name + '_oneLevelAlarmEnable').val(); //一级报警
                    param.twoLevelAlarmEnable = $('#' + name + '_twoLevelAlarmEnable').val(); //二级报警
                    param.oneLevelVoiceEnable = $('#' + name + '_oneLevelVoiceEnable').val(); //一级语音提醒
                    param.twoLevelVoiceEnable = $('#' + name + '_twoLevelVoiceEnable').val(); //二级语音提醒

                    param.lowSpeedRecording = $('#' + name + '_lowSpeedRecording').val(); //低速录制视频
                    param.highSpeedRecording = $('#' + name + '_highSpeedRecording').val(); //高速录制视频
                }
                //道路标识识别
                if (id == '6410') {
                    param.roadMarkAlarmEnable = $('#' + name + '_roadMarkAlarmEnable').val(); //道路标识识别
                    param.voiceEnable = $('#' + name + '_voiceEnable').val(); //语音提醒
                }

                // 次数阈值
                if (id == '6405') {
                    param.numberThreshold = $('#changelane_frequency').val()
                }
            }

            if (param.lowSpeedRecording === undefined) {
                param.lowSpeedRecording = '1'
                param.highSpeedRecording = '1'
            }

            // 风险参数设置高速

            var riskParamSetCheckbox = "#" + _name + "_RiskParamSet";
            if (document.querySelectorAll(riskParamSetCheckbox).length == 1) {
                if ($(riskParamSetCheckbox).is(":checked")) {
                    param.highSpeed = $('#' + _name + '_highSpeed').val();
                }
            }
            if (id == '6512') {
                param.videoRecordingTime = $('#cover_video_text').val(); //录制时间
                param.photographNumber = $('#cover_photo_text').val(); //拍照张数
                param.photographTime = $('#cover_photo_time_text').val(); //拍照间隔
                param.highSpeed = $('#cover_highSpeed').val();// 风险参数设置高速
            }
            return param;
        },
        checkboxItems: function (inputCheck, obj, value) {
            var _value;

            //初始化多媒体参数设置的值
            if (!riskdefineSet.isEmpty(obj.videoRecordingTime) || !riskdefineSet.isEmpty(obj.photographNumber) || !riskdefineSet.isEmpty(obj.photographTime)) {
                if (value != 'infrared') {
                    $('#' + value + '_multiMediaSet').prop("checked", true);
                    $("#" + value + "_video_text").val(obj.videoRecordingTime);
                    $("#" + value + "_photo_text").val(obj.photographNumber);
                    $("#" + value + "_photo_time_text").val(obj.photographTime);
                }

            } else {
                $('#' + value + '_multiMediaSet').prop("checked", false);
            }


            // 初始化主动抓拍设置的值
            var idStr = obj.riskId.substring(0, 2);
            if (idStr == '64') { //前向监测系统(FMS)
                if (!riskdefineSet.isEmpty(obj.dsmAdasTimeInterval)) {
                    $('#adas-check').prop("checked", true)

                    $('#capture_time_text').val(obj.timingPhotoInterval)
                    $('#distancePhotoInterval').val(obj.distancePhotoInterval)
                    $('#timingPhoto').val(obj.timingPhoto)
                    $('#adastimeInterval').val(obj.dsmAdasTimeInterval)
                } else {
                    $('#adas-check').prop("checked", false)
                }

                if (!riskdefineSet.isEmpty(obj.initiativeCaptureVoiceEnable)) {
                    $('#captureAdasSetting').prop("checked", true)

                    $('#capture-select').val(obj.initiativeCaptureAlarmEnable)
                    $('#adas-voice-select').val(obj.initiativeCaptureVoiceEnable)
                    $('#timingCapture').val(obj.timingCapture)
                    $('#distanceCapture').val(obj.distanceCapture)
                } else {
                    $('#captureAdasSetting').prop("checked", false)
                }

            }

            if (idStr == '65') { //dsm
                if (!riskdefineSet.isEmpty(obj.dsmAdasTimeInterval)) {
                    $('#dsm-check').prop("checked", true)

                    $('#dsm_capture_time_text').val(obj.timingPhotoInterval)
                    $('#dsm_distancePhotoInterval').val(obj.distancePhotoInterval)
                    $('#dsm_timingPhoto').val(obj.timingPhoto)
                    $('#dsm_timeInterval').val(obj.dsmAdasTimeInterval)
                } else {
                    $('#dsm-check').prop("checked", false)
                }

                if (!riskdefineSet.isEmpty(obj.initiativeCaptureVoiceEnable)) {
                    $('#dsm_captureCheck').prop("checked", true)

                    $('#capture-select-dsm').val(obj.initiativeCaptureAlarmEnable)
                    $('#adas-voice-select-dsm').val(obj.initiativeCaptureVoiceEnable)
                    $('#timingCapture-dsm').val(obj.timingCapture)
                    $('#distanceCapture-dsm').val(obj.distanceCapture)
                } else {
                    $('#dsm_captureCheck').prop("checked", false)
                }

            }


            //初始化风险等级设置的值
            var id = obj.riskId;
            if (id == '6506' || id == '6507' || id == '6503' || id == '6502' || id == '6508' || id == '6511' || id == '6512'
                || id == '6401' || id == '6402' || id == '6403' || id == '6404' || id == '6405' || id == '6407' || id == '6408') {
                if (!riskdefineSet.isEmpty(obj.oneLevelAlarmEnable)) {
                    $('#' + value + '_RiskLevelSet').prop("checked", true);

                    if (!riskdefineSet.isEmpty(obj.oneLevelAlarmEnable)) {
                        $('#' + value + '_oneLevelAlarmEnable').val(obj.oneLevelAlarmEnable)
                    }
                    if (!riskdefineSet.isEmpty(obj.twoLevelAlarmEnable)) {
                        $('#' + value + '_twoLevelAlarmEnable').val(obj.twoLevelAlarmEnable)
                    }
                    if (!riskdefineSet.isEmpty(obj.lowSpeedLevel)) {
                        // $('#'+value+'_lowSpeedLevel').val(obj.lowSpeedLevel)
                        $('#' + value + '_low_select').val(obj.lowSpeedLevel)
                    }
                    if (!riskdefineSet.isEmpty(obj.highSpeedLevel)) {
                        // $('#'+value+'_highSpeedLevel').val(obj.highSpeedLevel)
                        $('#' + value + '_high_select').val(obj.highSpeedLevel)
                    }
                    if (!riskdefineSet.isEmpty(obj.lowSpeedRecording)) {
                        $('#' + value + '_lowSpeedRecording').val(obj.lowSpeedRecording)
                    }
                    if (!riskdefineSet.isEmpty(obj.highSpeedRecording)) {
                        $('#' + value + '_highSpeedRecording').val(obj.highSpeedRecording)
                    }
                    if (!riskdefineSet.isEmpty(obj.oneLevelVoiceEnable)) {
                        $('#' + value + '_oneLevelVoiceEnable').val(obj.oneLevelVoiceEnable)
                    }
                    if (!riskdefineSet.isEmpty(obj.twoLevelVoiceEnable)) {
                        $('#' + value + '_twoLevelVoiceEnable').val(obj.twoLevelVoiceEnable)
                    }


                    // 抽烟，接打手持电话
                    if (id == '6503' || id == '6502') {
                        if (!riskdefineSet.isEmpty(obj.timeInterval)) {
                            $('#' + value + '_timeInterval').val(obj.timeInterval); //时间间隔
                        }
                    }

                } else {
                    $('#' + value + '_RiskLevelSet').prop("checked", false);
                }
            }

            // 道路标识超限”、“驾驶员人证不符”、“驾驶员不在驾驶位置阈值设置
            if (id == '6409' || id == '6509' || id == '6510') {
                if (!riskdefineSet.isEmpty(obj.lowSpeedRecording)) {
                    $('#' + value + '_lowSpeedRecording').val(obj.lowSpeedRecording)
                }
                if (id == '6409') {
                    if (!riskdefineSet.isEmpty(obj.highSpeedRecording)) {
                        $('#' + value + '_highSpeedRecording').val(obj.highSpeedRecording)
                    }
                }
            }

            // 时间阈值
            if (id == '6509' || id == '6401' || id == '6403' || id == '6404' || id == '6405' || id == '6407') {
                if (!riskdefineSet.isEmpty(obj.timeInterval)) {
                    $('#' + value + '_timeInterval').val(obj.timeInterval); //时间阈值
                }

                if (id == '6405') {
                    if (!riskdefineSet.isEmpty(obj.numberThreshold)) {
                        $('#changelane_frequency').val(obj.numberThreshold); //时间阈值
                    }
                }
            }
            // wjk

            //道路标识超限
            if (id == '6409') {
                if (!riskdefineSet.isEmpty(obj.oneLevelAlarmEnable)) {
                    $('#' + value + '_RiskLevelSet').prop("checked", true);
                    if (!riskdefineSet.isEmpty(obj.oneLevelAlarmEnable)) {
                        $('#' + value + '_oneLevelAlarmEnable').val(obj.oneLevelAlarmEnable)
                    }
                    if (!riskdefineSet.isEmpty(obj.twoLevelAlarmEnable)) {
                        $('#' + value + '_twoLevelAlarmEnable').val(obj.twoLevelAlarmEnable)
                    }
                    if (!riskdefineSet.isEmpty(obj.oneLevelVoiceEnable)) {
                        $('#' + value + '_oneLevelVoiceEnable').val(obj.oneLevelVoiceEnable)
                    }
                    if (!riskdefineSet.isEmpty(obj.twoLevelVoiceEnable)) {
                        $('#' + value + '_twoLevelVoiceEnable').val(obj.twoLevelVoiceEnable)
                    }
                } else {
                    $('#' + value + '_RiskLevelSet').prop("checked", false);
                }
            }
            // 道路标识识别
            if (id == '6410') {
                if (!riskdefineSet.isEmpty(obj.roadMarkAlarmEnable)) {
                    $('#' + value + '_RiskLevelSet').prop("checked", true);
                    if (!riskdefineSet.isEmpty(obj.roadMarkAlarmEnable)) {
                        $('#' + value + '_roadMarkAlarmEnable').val(obj.roadMarkAlarmEnable)
                    }
                    if (!riskdefineSet.isEmpty(obj.voiceEnable)) {
                        $('#' + value + '_voiceEnable').val(obj.voiceEnable)
                    }
                } else {
                    $('#' + value + '_RiskLevelSet').prop("checked", false);
                }
            }

            if (id == '6509' || id == '6510') {
                if (!riskdefineSet.isEmpty(obj.checkSwitch)) {
                    $('#' + value + '_RiskLevelSet').prop("checked", true);

                    if (!riskdefineSet.isEmpty(obj.checkSwitch)) {
                        $('#' + value + '_check_switch').val(obj.checkSwitch);
                    }
                    if (!riskdefineSet.isEmpty(obj.voiceEnable)) {
                        $('#' + value + '_voiceEnable').val(obj.voiceEnable);
                    }

                    if (id == '6509') {
                        if (!riskdefineSet.isEmpty(obj.timeInterval)) {
                            $('#' + value + '_timeInterval').val(obj.timeInterval);
                        }
                    }

                } else {
                    $('#' + value + '_RiskLevelSet').prop("checked", false);
                }
            }

            //底部时间间隔和持续时长
            // 闭眼，打哈欠
            if (id == '6506' || id == '6507') {
                if (!riskdefineSet.isEmpty(obj.fatigueP)) {
                    $('#time-interval').val(obj.fatigueP); //时间间隔
                }
                if (!riskdefineSet.isEmpty(obj.fatigueT)) {
                    $('#time-continue').val(obj.fatigueT); //持续时长
                }


            }

            // 抽烟，接打手持电话
            if (id == '6503' || id == '6502') {
                if (!riskdefineSet.isEmpty(obj.distractP)) {
                    $('#drive_time_interval').val(obj.distractP); //时间间隔
                }
                if (!riskdefineSet.isEmpty(obj.distractT)) {
                    $('#drive_continue_time').val(obj.distractT); //持续时长
                }
            }

            //违规异常时间间隔和持续时长
            if (id == '6508' || id == '6509' || id == '6510') {
                if (!riskdefineSet.isEmpty(obj.abnormalP)) {
                    $('#driver-detection_time_text').val(obj.abnormalP); //时间间隔
                }
                if (!riskdefineSet.isEmpty(obj.abnormalT)) {
                    $('#driver-detection_time_duration').val(obj.abnormalT); //持续时长
                }
            }
            //碰撞危险
            if (id == '6401' || id == '6402' || id == '6403' || id == '6404' || id == '6405' || id == '6407' || id == '6408' || id == '6409' || id == '6410') {
                if (!riskdefineSet.isEmpty(obj.collisionP)) {
                    $('#collisionPValue').val(obj.collisionP); //时间间隔
                }
                if (!riskdefineSet.isEmpty(obj.collisionT)) {
                    $('#collisionTValue').val(obj.collisionT); //持续时长
                }
            }

            // 人证不符,驾驶员不在驾驶位置
            // if (id == '6509' || id == '6510') {
            //     param.check_switch =$('#'+name+'_check_switch').val(); //定时检查
            //     param.voiceEnable = $('#'+name+'_voiceEnable').val(); //人证不符语音提醒
            // }
            //
            // // 人证不符
            // if (id == '6509' || id == '6401'|| id == '6403'|| id == '6404'|| id == '6405'|| id == '6407') {
            //     param.timeInterval = $('#'+name+'_timeInterval').val();//人证不符间隔时间
            // }
            //
            // // 次数阈值
            // if(id == '6405'){
            //     param.numberThreshold=$('#changelane_frequency').val()
            // }

            // 底部高速设置
            if (!riskdefineSet.isEmpty(obj.highSpeed)) {
                $('#' + value + '_RiskParamSet').prop("checked", true);
                $('#' + value + '_highSpeed').val(obj.highSpeed);
            } else {
                $('#' + value + '_RiskParamSet').prop("checked", false);
            }
            // 底部统一低速设置
            if (!riskdefineSet.isEmpty(obj.lowSpeed)) {
                $('#allLowSpeed_checkbox').prop("checked", true);
                $('#all_lowSpeed').val(obj.lowSpeed);
            } else {
                $('#allLowSpeed_checkbox').prop("checked", false);
            }


        },
        getItem: function (value) {//得到这个id的全部节点
            var inputCheck = $("input[value='risk_" + value + "']");
            return inputCheck;
        },
        initSelect: function (obj) {


            // 其他类
            if (!riskdefineSet.isEmpty(obj.cameraResolution) || !riskdefineSet.isEmpty(obj.videoResolution) || !riskdefineSet.isEmpty(obj.alarmVolume) || !riskdefineSet.isEmpty(obj.sensitivity)) {
                $('#photo_video_volume_sensitivity').prop("checked", true);
            }


            // 初始化全局拍照分辨率
            if (obj.cameraResolution && obj.cameraResolution != '0x01') {
                $("#camera_resolution_select_065").val(obj.cameraResolution);
            }
            // 初始化全局录制视频分辨率
            if (obj.videoResolution && obj.videoResolution != '0x01') {
                $("#video_resolution_select_065").val(obj.videoResolution);
            }
            //初始化全局提示音量
            if (obj.alarmVolume) {
                $("#video_volume_select").val(obj.alarmVolume);
            }
            //初始化全局灵敏度
            if (obj.sensitivity) {
                $("#video_sensitivity_select").val(obj.sensitivity);
            }


        },
        initCheckbox: function (riskSettingList) {
            if (riskSettingList == "[]")
                return;
            var riskSettingListJsons = JSON.parse(riskSettingList);
            //checkbox 默认不勾选
            $("#editForm input[type='checkbox']").prop('checked', false);

            $.each(riskSettingListJsons, function (index, obj) {
                // riskdefineSet.initGlobalVariable(obj);
                //回显分辨率
                riskdefineSet.initSelect(obj);

                var inputCheck = riskdefineSet.getItem(obj.riskId);


                var inputCheck = riskdefineSet.getItem(obj.riskId);

                if ("6506" == obj.riskId) {
                    riskdefineSet.checkboxItems(inputCheck, obj, "closeEyes");//闭眼
                } else if ("6507" == obj.riskId) {
                    riskdefineSet.checkboxItems(inputCheck, obj, 'yawn');//打哈欠
                }
                else if ("6503" == obj.riskId) { //抽烟
                    riskdefineSet.checkboxItems(inputCheck, obj, "smoking");
                }
                else if ("6502" == obj.riskId) {//接打手持电话
                    riskdefineSet.checkboxItems(inputCheck, obj, "answerThephone");
                }
                else if ("6504" == obj.riskId) {//注意力分散
                    riskdefineSet.checkboxItems(inputCheck, obj, "distraction");
                } else if ("6505" == obj.riskId) {//违规异常
                    riskdefineSet.checkboxItems(inputCheck, obj, "abnormalAlarm");
                } else if ("6401" == obj.riskId) {//前向碰撞
                    riskdefineSet.checkboxItems(inputCheck, obj, "forwardCollision");
                    // riskdefineSet.getCollisionRiskValue(obj);
                }

                else if ("6508" == obj.riskId) { //长时间不目视前方
                    riskdefineSet.checkboxItems(inputCheck, obj, "posture");
                }

                else if ("6511" == obj.riskId) { //遮挡
                    riskdefineSet.checkboxItems(inputCheck, obj, 'cover');
                }

                else if ("6512" == obj.riskId) { //红外阻断
                    riskdefineSet.checkboxItems(inputCheck, obj, 'infrared');
                }


                else if ("6509" == obj.riskId) { //人证不符
                    riskdefineSet.checkboxItems(inputCheck, obj, "witness");
                }

                else if ("6510" == obj.riskId) { //驾驶员不在驾驶位置
                    riskdefineSet.checkboxItems(inputCheck, obj, 'driver');
                }

                else if ("6404" == obj.riskId) {//行人碰撞
                    riskdefineSet.checkboxItems(inputCheck, obj, "pedestrianCollision");
                    // riskdefineSet.getCollisionRiskValue(obj);
                } else if ("6403" == obj.riskId) {//车距过近
                    riskdefineSet.checkboxItems(inputCheck, obj, "tooClose");
                    // riskdefineSet.getCollisionRiskValue(obj);
                } else if ("6405" == obj.riskId) {//频繁变道
                    riskdefineSet.checkboxItems(inputCheck, obj, "frequentLaneChange");
                    // riskdefineSet.getCollisionRiskValue(obj);
                } else if ("6402" == obj.riskId) {//车道偏离
                    riskdefineSet.checkboxItems(inputCheck, obj, "laneDeparture");
                    // riskdefineSet.getCollisionRiskValue(obj);
                } else if ("6407" == obj.riskId) {//障碍物
                    riskdefineSet.checkboxItems(inputCheck, obj, "block");
                    riskdefineSet.getCollisionRiskValue(obj);
                } else if ("6408" == obj.riskId) {//障碍物
                    riskdefineSet.checkboxItems(inputCheck, obj, "hurry");
                    // riskdefineSet.getCollisionRiskValue(obj);
                } else if ("6409" == obj.riskId) {//道路标识超限
                    riskdefineSet.checkboxItems(inputCheck, obj, 'roadIdentifying');
                } else if ("6410" == obj.riskId) {//道路标识识别
                    riskdefineSet.checkboxItems(inputCheck, obj, 'roadrecognition');
                }
            });
        },
        isEmpty: function (value) {
            if (typeof(value) == "undefined" || null == value || riskdefineSet.trim(value) == "") {
                return true;
            }
            return false;
        },
        trim: function trim(str) { //删除左右两端的空格
            try {
                if (typeof(str) == "undefined") {
                    return "";
                }
                if (typeof(str) == "number") {
                    return str.toString();
                }
                return str.replace(/(^\s*)|(\s*$)/g, "");
            } catch (error) {
                return str;
            }
        },
        getCollisionRiskValue: function (obj) {//得到碰撞危险的值
            if (riskdefineSet.isEmpty(collisionRiskValue)) {
                if (!riskdefineSet.isEmpty(obj.lowSpeed)) {
                    collisionRiskValue = obj.lowSpeed;
                }
            }
            if (!riskdefineSet.isEmpty(collisionRiskValue)) {
                $("#collisionRiskValue_checkbox").prop("checked", true);
                $("#collisionRiskValue_text").val(collisionRiskValue);
            } else {
                $("#collisionRiskValue_checkbox").prop("checked", false);
            }
        },
        timeIntervalAndLowSpeedValue: function () {//时间间隔
            if (!riskdefineSet.isEmpty(abnormalPValue)) {
                $("#abnormalPValue").val(abnormalPValue);
            }
            if (!riskdefineSet.isEmpty(abnormalTValue)) {
                $("#abnormalTValue").val(abnormalTValue);
            }
            if (!riskdefineSet.isEmpty(collisionTValue)) {
                $("#collisionTValue").val(collisionTValue);
            }
            if (!riskdefineSet.isEmpty(collisionPValue)) {
                $("#collisionPValue").val(collisionPValue);
            }
            if (!riskdefineSet.isEmpty(distractTValue)) {
                $("#distractTValue").val(distractTValue);
            }
            if (!riskdefineSet.isEmpty(distractPValue)) {
                $("#distractPValue").val(distractPValue);
            }
            if (!riskdefineSet.isEmpty(fatigueTValue)) {
                $("#fatigueTValue").val(fatigueTValue);
            }
            if (!riskdefineSet.isEmpty(fatiguePValue)) {
                $("#fatiguePValue").val(fatiguePValue);
            }
            if (!riskdefineSet.isEmpty(allLowSpeed)) {
                $("#allLowSpeed_checkbox").prop("checked", true);
                $("#all_LowSpeed_text").val(allLowSpeed);
            } else {
                $("#allLowSpeed_checkbox").prop("checked", false);
            }
        },
        lowSpeedAndHighspeed: function () {
            var lowspeed = Number($('#all_lowSpeed').val());
            var closeEyes_highSpeed = Number($('#closeEyes_highSpeed').val());
            var yawn_highSpeed = Number($('#yawn_highSpeed').val());
            var smoking_highSpeed = Number($('#smoking_highSpeed').val());
            var answerThephone_highSpeed = Number($('#answerThephone_highSpeed').val());
            var posture_highSpeed = Number($('#posture_highSpeed').val());
            var forwardCollision_highSpeed = Number($('#forwardCollision_highSpeed').val());
            var laneDeparture_highSpeed = Number($('#laneDeparture_highSpeed').val());
            var tooClose_highSpeed = Number($('#tooClose_highSpeed').val());
            var pedestrianCollision_highSpeed = Number($('#pedestrianCollision_highSpeed').val());
            // var block_highSpeed = Number($('#block_highSpeed').val());
            var hurry_highSpeed = Number($('#hurry_highSpeed').val());

            var cover_highSpeed = Number($('#cover_highSpeed').val());

            if (lowspeed >= closeEyes_highSpeed || lowspeed >= yawn_highSpeed || lowspeed >= smoking_highSpeed || lowspeed >= answerThephone_highSpeed || lowspeed >= posture_highSpeed || lowspeed >= forwardCollision_highSpeed || lowspeed >= laneDeparture_highSpeed
                || lowspeed >= tooClose_highSpeed || lowspeed >= pedestrianCollision_highSpeed || lowspeed >= cover_highSpeed || lowspeed >= hurry_highSpeed) {

                layer.msg('低速值必须小于参数设置中所有高速值');
                return false;
            } else {
                return true;
            }
        },
        submitFrom: function () {
            if (checkspeedvalue) {
                if (riskdefineSet.validates() && riskdefineSet.lowSpeedAndHighspeed()) {
                    var data = '';
                    var jsonData = riskdefineSet.combinationJsonDate();
                    data = {"checkedParams": JSON.stringify(jsonData), "id": $("#vehicleId").val()};
                    json_ajax("POST", "/clbs/r/riskManagement/DefineSettings/setting.gsp", "json", false, data, riskdefineSet.callback);
                }
            }
        },
        callback: function (result) {
            if (result != null) {
                if (result.success) {
                    layer.msg("修改成功！", {move: false});
                    $("#commonWin").modal("hide");
                    myTable.refresh();
                    $("#settingMoreBtn").attr("href", "/clbs/r/riskManagement/DefineSettings/settingmore_.gsp");
                } else {
                    layer.msg(result.msg, {move: false});
                }
            }
        },
        clearValues: function () {
            $('#editForm')[0].reset();
        },
        validates: function () {
            return $("#editForm").validate({
                ignore: '',
                rules: {
                    closeEyes_video_text: {
                        isCheckedRequested: "#closeEyes_multiMediaSet",
                        isCheckedNumber: "#closeEyes_multiMediaSet,0,60"
                    },
                    closeEyes_photo_text: {
                        isCheckedRequested: "#closeEyes_multiMediaSet",
                        isCheckedNumber: "#closeEyes_multiMediaSet,0,10"
                    },
                    closeEyes_photo_time_text: {
                        isCheckedRequested: "#closeEyes_multiMediaSet",
                        isCheckedNumber2: "#closeEyes_multiMediaSet,0.5,2",
                        decimalOne: true
                    },
                    yawn_video_text: {
                        isCheckedRequested: "#yawn_multiMediaSet",
                        isCheckedNumber: "#yawn_multiMediaSet,0,60"
                    },
                    yawn_photo_text: {
                        isCheckedRequested: "#yawn_multiMediaSet",
                        isCheckedNumber: "#yawn_multiMediaSet,0,10"
                    },
                    yawn_photo_time_text: {
                        isCheckedRequested: "#yawn_multiMediaSet",
                        isCheckedNumber2: "#yawn_multiMediaSet,0.5,2",
                        decimalOne: true
                    },
                    //疲劳驾驶风险等级设置
                    fatiguePValue: {
                        required: true,
                        digits: true,
                        range: [1, 60]
                    },
                    fatigueTValue: {
                        required: true,
                        digits: true,
                        range: [1, 600]
                    },
                    //疲劳驾驶风险参数设置
                    closeEyes_risk_high_text: {
                        isCheckedRequested: "#closeEyes_RiskParamSet",
                        isCheckedNumber2: "#closeEyes_RiskParamSet,0,220",
                        isIntGteZero: true
                    },
                    yawn_risk_high_text: {
                        isCheckedRequested: "#yawn_RiskParamSet",
                        isCheckedNumber2: "#yawn_RiskParamSet,0,220",
                        isIntGteZero: true
                    },
                    all_LowSpeed_text: {
                        isCheckedRequested: "#allLowSpeed_checkbox",
                        isCheckedNumber2: "#allLowSpeed_checkbox,0,220",
                        isIntGteZero: true
                    },
                    //分心驾驶多媒体参数设置
                    smoking_video_text: {
                        isCheckedRequested: "#smoking_multiMediaSet",
                        isCheckedNumber: "#smoking_multiMediaSet,0,60"
                    },
                    smoking_photo_text: {
                        isCheckedRequested: "#smoking_multiMediaSet",
                        isCheckedNumber: "#smoking_multiMediaSet,0,10"
                    },
                    smoking_photo_time_text: {
                        isCheckedRequested: "#smoking_multiMediaSet",
                        isCheckedNumber2: "#smoking_multiMediaSet,0.5,2",
                        decimalOne: true
                    },
                    answerThephone_video_text: {
                        isCheckedRequested: "#answerThephone_multiMediaSet",
                        isCheckedNumber: "#answerThephone_multiMediaSet,0,60"
                    },
                    answerThephone_photo_text: {
                        isCheckedRequested: "#answerThephone_multiMediaSet",
                        isCheckedNumber: "#answerThephone_multiMediaSet,0,10"
                    },
                    answerThephone_photo_time_text: {
                        isCheckedRequested: "#answerThephone_multiMediaSet",
                        isCheckedNumber2: "#answerThephone_multiMediaSet,0.5,2",
                        decimalOne: true
                    },
                    distraction_video_text: {
                        isCheckedRequested: "#distraction_multiMediaSet",
                        isCheckedNumber: "#distraction_multiMediaSet,0,60"
                    },
                    distraction_photo_text: {
                        isCheckedRequested: "#distraction_multiMediaSet",
                        isCheckedNumber: "#distraction_multiMediaSet,0,10"
                    },
                    distraction_photo_time_text: {
                        isCheckedRequested: "#distraction_multiMediaSet",
                        isCheckedNumber2: "#distraction_multiMediaSet,0.5,2",
                        decimalOne: true
                    },
                    //注意力分散风险等级设置
                    distractPValue: {
                        required: true,
                        digits: true,
                        range: [1, 60]
                    },
                    distractTValue: {
                        required: true,
                        digits: true,
                        range: [1, 600]
                    },
                    //注意力分散风险参数设置
                    smoking_risk_high_text: {
                        isCheckedRequested: "#smoking_RiskParamSet",
                        isCheckedNumber2: "#smoking_RiskParamSet,0,220",
                        isIntGteZero: true
                    },
                    answerThephone_highSpeed: {
                        isCheckedRequested: "#answerThephone_RiskParamSet",
                        isCheckedNumber: "#answerThephone_RiskParamSet,0,220",
                        isIntGteZero: true
                    },
                    distraction_risk_high_text: {
                        isCheckedRequested: "#distraction_RiskParamSet",
                        isCheckedNumber2: "#distraction_RiskParamSet,0,220",
                        isIntGteZero: true
                    },
                    //长时间不目视前方多媒体参数设置
                    posture_video_text: {
                        isCheckedRequested: "#posture_multiMediaSet",
                        isCheckedNumber: "#posture_multiMediaSet,0,60"
                    },
                    posture_photo_text: {
                        isCheckedRequested: "#posture_multiMediaSet",
                        isCheckedNumber: "#posture_multiMediaSet,0,10"
                    },
                    posture_photo_time_text: {
                        isCheckedRequested: "#posture_multiMediaSet",
                        isCheckedNumber2: "#posture_multiMediaSet,0.5,2",
                        decimalOne: true
                    },
                    //遮挡/红外阻断
                    cover_video_text: {
                        isCheckedRequested: "#cover_multiMediaSet",
                        isCheckedNumber: "#cover_multiMediaSet,0,60"
                    },
                    cover_photo_text: {
                        isCheckedRequested: "#cover_multiMediaSet",
                        isCheckedNumber: "#cover_multiMediaSet,0,10"
                    },
                    cover_photo_time_text: {
                        isCheckedRequested: "#cover_multiMediaSet",
                        isCheckedNumber2: "#cover_multiMediaSet,0.5,2",
                        decimalOne: true
                    },

                    //人证不符多媒体参数设置
                    witness_video_text: {
                        isCheckedRequested: "#witness_multiMediaSet",
                        isCheckedNumber: "#witness_multiMediaSet,0,60"
                    },
                    witness_photo_text: {
                        isCheckedRequested: "#witness_multiMediaSet",
                        isCheckedNumber: "#witness_multiMediaSet,0,10"
                    },
                    witness_photo_time_text: {
                        isCheckedRequested: "#witness_multiMediaSet",
                        isCheckedNumber2: "#witness_multiMediaSet,0.5,2",
                        decimalOne: true
                    },

                    //驾驶员不在驾驶位置多媒体参数设置
                    driver_video_text: {
                        isCheckedRequested: "#driver_multiMediaSet",
                        isCheckedNumber: "#driver_multiMediaSet,0,60"
                    },
                    driver_photo_text: {
                        isCheckedRequested: "#driver_multiMediaSet",
                        isCheckedNumber: "#driver_multiMediaSet,0,10"
                    },
                    driver_photo_time_text: {
                        isCheckedRequested: "#driver_multiMediaSet",
                        isCheckedNumber2: "#driver_multiMediaSet,0.5,2",
                        decimalOne: true
                    },

                    //道路标识超限多媒体参数设置
                    roadIdentifying_video_text: {
                        isCheckedRequested: "#roadIdentifying_multiMediaSet",
                        isCheckedNumber: "#roadIdentifying_multiMediaSet,0,60"
                    },
                    roadIdentifying_photo_text: {
                        isCheckedRequested: "#roadIdentifying_multiMediaSet",
                        isCheckedNumber: "#roadIdentifying_multiMediaSet,0,10"
                    },
                    roadIdentifying_photo_time_text: {
                        isCheckedRequested: "#roadIdentifying_multiMediaSet",
                        isCheckedNumber2: "#roadIdentifying_multiMediaSet,0.5,2",
                        decimalOne: true
                    },

                    //道路标识识别多媒体参数设置
                    laneDeparture_video_text: {
                        isCheckedRequested: "#roadrecognition_multiMediaSet",
                        isCheckedNumber: "#roadrecognition_multiMediaSet,0,60"
                    },
                    roadrecognition_photo_text: {
                        isCheckedRequested: "#roadrecognition_multiMediaSet",
                        isCheckedNumber: "#roadrecognition_multiMediaSet,0,10"
                    },
                    laneDeparture_photo_time_text: {
                        isCheckedRequested: "#roadrecognition_multiMediaSet",
                        isCheckedNumber2: "#roadrecognition_multiMediaSet,0.5,2",
                        decimalOne: true
                    },

                    //违规异常风险等级设置
                    abnormalPValue: {
                        required: true,
                        digits: true,
                        range: [1, 60]
                    },
                    abnormalTValue: {
                        required: true,
                        digits: true,
                        range: [1, 600]
                    },
                    //违规异常风险参数设置
                    abnormalAlarm_risk_high_text: {
                        isCheckedRequested: "#abnormalAlarm_RiskParamSet",
                        isCheckedNumber: "#posture_RiskParamSet,0,220",
                        isIntGteZero: true
                    },
                    //遮挡/红外阻断
                    cover_risk_high_text: {
                        isCheckedRequested: "#cover_RiskParamSet",
                        isCheckedNumber: "#cover_RiskParamSet,0,220",
                        isIntGteZero: true
                    },
                    //碰撞危险多媒体参数设置
                    forwardCollision_video_text: {
                        isCheckedRequested: "#forwardCollision_multiMediaSet",
                        isCheckedNumber: "#forwardCollision_multiMediaSet,0,60"
                    },
                    forwardCollision_photo_text: {
                        isCheckedRequested: "#forwardCollision_multiMediaSet",
                        isCheckedNumber: "#forwardCollision_multiMediaSet,0,10"
                    },
                    forwardCollision_photo_time_text: {
                        isCheckedRequested: "#forwardCollision_multiMediaSet",
                        isCheckedNumber2: "#forwardCollision_multiMediaSet,0.5,2",
                        decimalOne: true
                    },
                    pedestrianCollision_video_text: {
                        isCheckedRequested: "#pedestrianCollision_multiMediaSet",
                        isCheckedNumber: "#forwardCollision_multiMediaSet,0,60"
                    },
                    pedestrianCollision_photo_text: {
                        isCheckedRequested: "#pedestrianCollision_multiMediaSet",
                        isCheckedNumber: "#forwardCollision_multiMediaSet,0,10"
                    },
                    pedestrianCollision_photo_time_text: {
                        isCheckedRequested: "#pedestrianCollision_multiMediaSet",
                        isCheckedNumber2: "#forwardCollision_multiMediaSet,0.5,2",
                        decimalOne: true
                    },
                    tooClose_video_text: {
                        isCheckedRequested: "#tooClose_multiMediaSet",
                        isCheckedNumber: "#tooClose_multiMediaSet,0,60"
                    },
                    tooClose_photo_text: {
                        isCheckedRequested: "#tooClose_multiMediaSet",
                        isCheckedNumber: "#tooClose_multiMediaSet,0,10"
                    },
                    tooClose_photo_time_text: {
                        isCheckedRequested: "#tooClose_multiMediaSet",
                        isCheckedNumber2: "#tooClose_multiMediaSet,0.5,2",
                        decimalOne: true
                    },
                    frequentLaneChange_video_text: {
                        isCheckedRequested: "#frequentLaneChange_multiMediaSet",
                        isCheckedNumber: "#frequentLaneChange_multiMediaSet,0,60"
                    },
                    frequentLaneChange_photo_text: {
                        isCheckedRequested: "#frequentLaneChange_multiMediaSet",
                        isCheckedNumber: "#frequentLaneChange_multiMediaSet,0,10"
                    },
                    frequentLaneChange_photo_time_text: {
                        isCheckedRequested: "#frequentLaneChange_multiMediaSet",
                        isCheckedNumber2: "#frequentLaneChange_multiMediaSet,0.5,2",
                        decimalOne: true
                    },
                    laneDeparture_video_text: {
                        isCheckedRequested: "#laneDeparture_multiMediaSet",
                        isCheckedNumber: "#laneDeparture_multiMediaSet,0,60"
                    },
                    laneDeparture_photo_text: {
                        isCheckedRequested: "#laneDeparture_multiMediaSet",
                        isCheckedNumber: "#frequentLaneChange_multiMediaSet,0,10"
                    },
                    laneDeparture_photo_time_text: {
                        isCheckedRequested: "#laneDeparture_multiMediaSet",
                        isCheckedNumber2: "#laneDeparture_multiMediaSet,0.5,2",
                        decimalOne: true
                    },

                    // 急加急减急转弯风险参数设置
                    hurry_video_text: {
                        isCheckedRequested: "#hurry_multiMediaSet",
                        isCheckedNumber: "#hurry_multiMediaSet,0,60"
                    },
                    hurry_photo_text: {
                        isCheckedRequested: "#hurry_multiMediaSet",
                        isCheckedNumber: "#hurry_multiMediaSet,0,10"
                    },
                    hurry_photo_time_text: {
                        isCheckedRequested: "#hurry_multiMediaSet",
                        isCheckedNumber2: "#hurry_multiMediaSet,0.5,2",
                        decimalOne: true
                    },

                    // 道路标识超限风险参数设置
                    roadIdentifying_video_text: {
                        isCheckedRequested: "#roadIdentifying_multiMediaSet",
                        isCheckedNumber: "#roadIdentifying_multiMediaSet,0,60"
                    },
                    roadIdentifying_photo_text: {
                        isCheckedRequested: "#roadIdentifying_multiMediaSet",
                        isCheckedNumber: "#roadIdentifying_multiMediaSet,0,10"
                    },
                    roadIdentifying_photo_time_text: {
                        isCheckedRequested: "#roadIdentifying_multiMediaSet",
                        isCheckedNumber2: "#roadIdentifying_multiMediaSet,0.5,2",
                        decimalOne: true
                    },

                    // 道路标识识别风险参数设置
                    roadrecognition_video_text: {
                        isCheckedRequested: "#roadrecognition_multiMediaSet",
                        isCheckedNumber: "#roadrecognition_multiMediaSet,0,60"
                    },
                    roadrecognition_photo_text: {
                        isCheckedRequested: "#roadrecognition_multiMediaSet",
                        isCheckedNumber: "#roadrecognition_multiMediaSet,0,10"
                    },
                    roadrecognition_photo_time_text: {
                        isCheckedRequested: "#roadrecognition_multiMediaSet",
                        isCheckedNumber2: "#roadrecognition_multiMediaSet,0.5,2",
                        decimalOne: true
                    },

                    // 前向碰撞时间阈值
                    // forwardCollision_interval: {
                    //     isCheckedRequested: "#forwardCollision_RiskLevelSet",
                    //     isCheckedNumber: "#forwardCollision_RiskLevelSet,1,5"
                    // },

                    // 行人碰撞
                    peoplecrash_interval: {
                        isCheckedRequested: '#pedestrianCollision_RiskLevelSet',
                        isCheckedNumber: "#pedestrianCollision_RiskLevelSet,1,10"
                    },

                    // 车距过近距离阈值
                    nearlyDistance_interval: {
                        isCheckedRequested: '#tooClose_RiskLevelSet',
                        isCheckedNumber: "#tooClose_RiskLevelSet,10,50"
                    },

                    // 频繁变道时间阈值
                    changelane_interval: {
                        isCheckedRequested: '#frequentLaneChange_RiskLevelSet',
                        isCheckedNumber: "#frequentLaneChange_RiskLevelSet,30,120"
                    },
                    // 频繁变道次数阈值
                    changelane_frequency: {
                        isCheckedRequested: '#frequentLaneChange_RiskLevelSet',
                        isCheckedNumber: "#frequentLaneChange_RiskLevelSet,3,10"
                    },
                    //驾驶员人证不符
                    witness_timeInterval: {
                        isCheckedRequested: '#witness_RiskLevelSet',
                        isCheckedNumber: "#witness_RiskLevelSet,0,240"
                    },
                    // 抽烟时间间隔
                    smoking_timeInterval: {
                        isCheckedRequested: '#smoking_RiskLevelSet',
                        isCheckedNumber: "#smoking_RiskLevelSet,0,3600"
                    },
                    //接打手持电话时间间隔
                    answerThephone_timeInterval: {
                        isCheckedRequested: '#answerThephone_RiskLevelSet',
                        isCheckedNumber: "#answerThephone_RiskLevelSet,0,3600"
                    },

                    //障碍物时间阈值
                    barrier_interval: {
                        isCheckedRequested: '#block_RiskLevelSet',
                        isCheckedNumber: "#block_RiskLevelSet,1,10"
                    },

                    // adas多媒体参数设置
                    capture_time_text: {
                        isCheckedRequested: '#adas-check',
                        isCheckedNumber: "#adas-check,5,240"
                    },
                    distancePhotoInterval: {
                        isCheckedRequested: '#adas-check',
                        isCheckedNumber: "#adas-check,1,250"
                    },
                    timingPhoto: {
                        isCheckedRequested: '#adas-check',
                        isCheckedNumber: "#adas-check,1,10"
                    },
                    adastimeInterval: {
                        isCheckedRequested: '#adas-check',
                        isCheckedNumber2: "#adas-check,0.5,2",
                        decimalOne: true
                    },

                    // dsm多媒体参数设置
                    dsm_capture_time_text: {
                        isCheckedRequested: '#dsm-check',
                        isCheckedNumber: "#dsm-check,5,240"
                    },
                    dsm_distancePhotoInterval: {
                        isCheckedRequested: '#dsm-check',
                        isCheckedNumber: "#dsm-check,1,250"
                    },
                    dsm_timingPhoto: {
                        isCheckedRequested: '#dsm-check',
                        isCheckedNumber: "#dsm-check,1,10"
                    },
                    dsm_timeInterval: {
                        isCheckedRequested: '#dsm-check',
                        isCheckedNumber2: "#dsm-check,0.5,2",
                        decimalOne: true
                    },

                    //碰撞危险风险等级设置
                    collisionPValue: {
                        required: true,
                        digits: true,
                        range: [1, 60]
                    },
                    collisionTValue: {
                        required: true,
                        digits: true,
                        range: [1, 600]
                    },
                    // 抽烟高速
                    smoking_highSpeed: {
                        isCheckedRequested: "#smoking_RiskParamSet",
                        isCheckedNumber: "#smoking_RiskParamSet,0,220",
                        isIntGteZero: true
                    },
                    // answerThephone_highSpeed:{
                    //     isCheckedRequested: "#smoking_RiskParamSet",
                    //     isCheckedNumber: "#smoking_RiskParamSet,0,220",
                    //     isIntGteZero: true
                    // },
                    //碰撞危险风险参数设置
                    forwardCollision_risk_high_text: {
                        isCheckedRequested: "#forwardCollision_RiskParamSet",
                        isCheckedNumber2: "#forwardCollision_RiskParamSet,0,220",
                        isIntGteZero: true
                    },
                    pedestrianCollision_risk_high_text: {
                        isCheckedRequested: "#pedestrianCollision_RiskParamSet",
                        isCheckedNumber2: "#pedestrianCollision_RiskParamSet,0,220",
                        isIntGteZero: true
                    },
                    tooClose_risk_high_text: {
                        isCheckedRequested: "#tooClose_RiskParamSet",
                        isCheckedNumber2: "#tooClose_RiskParamSet,0,220",
                        isIntGteZero: true
                    },
                    frequentLaneChange_risk_high_text: {
                        isCheckedRequested: "#frequentLaneChange_RiskParamSet",
                        isCheckedNumber2: "#frequentLaneChange_RiskParamSet,0,220",
                        isIntGteZero: true
                    },
                    laneDeparture_risk_high_text: {
                        isCheckedRequested: "#laneDeparture_RiskParamSet",
                        isCheckedNumber2: "#laneDeparture_RiskParamSet,0,220",
                        isIntGteZero: true
                    },
                    block_risk_high_text: {
                        isCheckedRequested: "#block_RiskParamSet",
                        isCheckedNumber2: "#block_RiskParamSet,0,220",
                        isIntGteZero: true
                    },
                    collisionRiskValue_text: {
                        isCheckedRequested: "#collisionRiskValue_checkbox",
                        isCheckedNumber2: "#collisionRiskValue_checkbox,0,220",
                        isIntGteZero: true
                    },

                    frequent_risk_high_text: {
                        isCheckedRequested: "#frequentLaneChange_RiskParamSet",
                        isCheckedNumber2: "#frequentLaneChange_RiskParamSet,0,220",
                        isIntGteZero: true
                    },
                    //急加急减急转弯
                    hurry_risk_high_text: {
                        isCheckedRequested: "#hurry_RiskParamSet",
                        isCheckedNumber2: "#hurry_RiskParamSet,0,220",
                        isIntGteZero: true
                    },

                    block_video_text: {
                        isCheckedRequested: "#block_multiMediaSet",
                        isCheckedNumber: "#block_multiMediaSet,0,60"

                    },
                    block_photo_text: {
                        isCheckedRequested: "#block_multiMediaSet",
                        isCheckedNumber: "#block_multiMediaSet,0,10"
                    },

                    block_photo_time_text: {
                        isCheckedRequested: "#block_multiMediaSet",
                        isCheckedNumber2: "#block_multiMediaSet,0.5,2",
                        decimalOne: true
                    }
                },
                messages: {
                    block_video_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize60Length

                    },
                    block_photo_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To10Length
                    },

                    block_photo_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize05To2Length,
                        decimalOne: '只能输入1位小数'
                    },
                    closeEyes_video_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize60Length
                    },
                    closeEyes_photo_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To10Length
                    },
                    closeEyes_photo_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize05To2Length,
                        decimalOne: '只能输入1位小数'
                    },
                    yawn_video_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize60Length
                    },
                    yawn_photo_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To10Length
                    },
                    yawn_photo_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize05To2Length,
                        decimalOne: '只能输入1位小数'
                    },
                    //疲劳驾驶风险等级设置
                    fatiguePValue: {
                        required: publicNull,
                        digits: '必须输入整数',
                        range: riskSize60
                    },
                    fatigueTValue: {
                        required: publicNull,
                        digits: '必须输入整数',
                        range: riskSize600
                    },
                    //疲劳驾驶风险参数设置
                    closeEyes_risk_high_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize0To220Length,
                        isIntGteZero: "必须是正整数"
                    },
                    yawn_risk_high_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize0To220Length,
                        isIntGteZero: "必须是正整数"
                    },
                    all_LowSpeed_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize0To220Length,
                        isIntGteZero: "必须是正整数"
                    },
                    //注意力分散多媒体参数设置
                    smoking_video_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize60Length
                    },
                    smoking_photo_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To10Length
                    },
                    smoking_photo_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize05To2Length,
                        decimalOne: '只能输入1位小数'
                    },
                    answerThephone_video_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize60Length
                    },
                    answerThephone_photo_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To10Length
                    },
                    answerThephone_photo_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize05To2Length,
                        decimalOne: '只能输入1位小数'
                    },
                    distraction_video_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize60Length
                    },
                    distraction_photo_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To10Length
                    },
                    distraction_photo_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize05To2Length,
                        decimalOne: '只能输入1位小数'
                    },
                    //注意力分散风险等级设置
                    distractPValue: {
                        required: publicNull,
                        digits: '必须输入整数',
                        range: riskSize60
                    },
                    distractTValue: {
                        required: publicNull,
                        digits: '必须输入整数',
                        range: riskSize600
                    },
                    //注意力分散风险参数设置
                    smoking_risk_high_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize0To220Length,
                        isIntGteZero: "必须是正整数",
                    },
                    answerThephone_highSpeed: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To220Length,
                        isIntGteZero: "必须是正整数",
                    },
                    distraction_risk_high_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize0To220Length,
                        isIntGteZero: "必须是正整数",
                    },
                    //违规异常多媒体参数设置
                    posture_video_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize60Length
                    },
                    posture_photo_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To10Length
                    },
                    abnormalAlarm_photo_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize05To2Length,
                        decimalOne: '只能输入1位小数'
                    },
                    // 长时间不目视前方时间间隔
                    posture_photo_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize05To2Length,
                        decimalOne: '只能输入1位小数'
                    },
                    //遮挡/红外阻断
                    cover_video_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize60Length
                    },
                    cover_photo_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To10Length
                    },
                    cover_photo_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize05To2Length,
                        decimalOne: true
                    },
                    //人证不符多媒体参数设置
                    witness_video_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize60Length
                    },
                    witness_photo_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To10Length
                    },
                    witness_photo_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize05To2Length,
                        decimalOne: '只能输入1位小数'
                    },

                    //驾驶员不在驾驶位置多媒体参数设置
                    driver_video_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize60Length
                    },
                    driver_photo_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To10Length
                    },
                    driver_photo_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize05To2Length,
                        decimalOne: '只能输入1位小数'
                    },

                    //道路标识超限多媒体参数设置
                    roadIdentifying_video_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize60Length
                    },
                    roadIdentifying_photo_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To10Length
                    },
                    roadIdentifying_photo_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize05To2Length,
                        decimalOne: '只能输入1位小数'
                    },

                    //道路标识识别多媒体参数设置
                    laneDeparture_video_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize60Length
                    },
                    roadrecognition_photo_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To10Length
                    },
                    laneDeparture_photo_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize05To2Length,
                        decimalOne: '只能输入1位小数'
                    },

                    //违规异常风险等级设置
                    abnormalPValue: {
                        required: publicNull,
                        digits: '必须输入整数',
                        range: riskSize60
                    },
                    abnormalTValue: {
                        required: publicNull,
                        digits: '必须输入整数',
                        range: riskSize600
                    },
                    // 抽烟高速
                    smoking_highSpeed: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To220Length,
                        isIntGteZero: "必须是正整数"
                    },
                    //违规异常风险参数设置
                    abnormalAlarm_risk_high_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To220Length,
                        isIntGteZero: "必须是正整数",
                    },
                    //遮挡/红外阻断
                    cover_risk_high_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To220Length,
                        isIntGteZero: "必须是正整数"
                    },

                    //碰撞危险多媒体参数设置
                    forwardCollision_video_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize60Length
                    },
                    forwardCollision_photo_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To10Length
                    },
                    forwardCollision_photo_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize05To2Length,
                        decimalOne: '只能输入1位小数'
                    },
                    pedestrianCollision_video_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize60Length
                    },
                    pedestrianCollision_photo_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To10Length
                    },
                    pedestrianCollision_photo_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize05To2Length,
                        decimalOne: '只能输入1位小数'
                    },
                    tooClose_video_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize60Length
                    },
                    tooClose_photo_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To10Length
                    },
                    tooClose_photo_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize05To2Length,
                        decimalOne: '只能输入1位小数'
                    },
                    frequentLaneChange_video_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize60Length
                    },
                    frequentLaneChange_photo_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To10Length
                    },
                    frequentLaneChange_photo_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize05To2Length,
                        decimalOne: '只能输入1位小数'
                    },
                    laneDeparture_video_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize60Length
                    },
                    laneDeparture_photo_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To10Length
                    },
                    laneDeparture_photo_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize05To2Length,
                        decimalOne: '只能输入1位小数'
                    },

                    // 急加急减急转弯风险参数设置
                    hurry_video_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize60Length
                    },
                    hurry_photo_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To10Length
                    },
                    hurry_photo_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize05To2Length,
                        decimalOne: '只能输入1位小数'
                    },

                    // 道路标识超限风险参数设置
                    roadIdentifying_video_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize60Length
                    },
                    roadIdentifying_photo_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To10Length
                    },
                    roadIdentifying_photo_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize05To2Length,
                        decimalOne: '只能输入1位小数'
                    },

                    // 道路标识识别风险参数设置
                    roadrecognition_video_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize60Length
                    },
                    roadrecognition_photo_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: riskSize0To10Length
                    },
                    roadrecognition_photo_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize05To2Length,
                        decimalOne: '只能输入1位小数'
                    },

                    // 前向碰撞时间阈值
                    // forwardCollision_interval: {
                    //     isCheckedRequested: publicNull,
                    //     isCheckedNumber: "范围1~5"
                    // },

                    // 行人碰撞
                    peoplecrash_interval: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: "范围1~10"
                    },

                    // 车距过近距离阈值
                    nearlyDistance_interval: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: "范围10~50"
                    },

                    // 频繁变道时间阈值
                    changelane_interval: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: "范围30~120"
                    },
                    // 频繁变道次数阈值
                    changelane_frequency: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: "范围3~10"
                    },

                    //驾驶员人证不符
                    witness_timeInterval: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: "必须为0~240的数字"
                    },
                    //抽烟时间间隔
                    smoking_timeInterval: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: "必须为0~3600的数字"
                    },
                    //接打手持电话时间间隔
                    answerThephone_timeInterval: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: "必须为0~3600的数字"
                    },

                    //障碍物时间阈值
                    barrier_interval: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: "范围1~10"
                    },

                    // adas多媒体参数设置
                    capture_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: "范围5~240"
                    },
                    distancePhotoInterval: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: "范围1~250"
                    },
                    timingPhoto: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: "范围1~10"
                    },
                    adastimeInterval: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: "范围0.5~2",
                        decimalOne: '只能输入1位小数'
                    },

                    // dsm多媒体参数设置
                    dsm_capture_time_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: "范围5~240"
                    },
                    dsm_distancePhotoInterval: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: "范围1~250"
                    },
                    dsm_timingPhoto: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber: "范围1~10"
                    },
                    dsm_timeInterval: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: "范围0.5~2",
                        decimalOne: '只能输入1位小数'
                    },

                    //碰撞危险风险等级设置
                    collisionPValue: {
                        required: publicNull,
                        digits: '必须输入整数',
                        range: riskSize60
                    },
                    collisionTValue: {
                        required: publicNull,
                        digits: '必须输入整数',
                        range: riskSize600
                    },
                    //碰撞危险风险参数设置
                    forwardCollision_risk_high_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize0To220Length,
                        isIntGteZero: "必须是正整数",
                    },
                    pedestrianCollision_risk_high_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize0To220Length,
                        isIntGteZero: "必须是正整数",
                    },
                    tooClose_risk_high_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize0To220Length,
                        isIntGteZero: "必须是正整数",
                    },
                    frequentLaneChange_risk_high_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize0To220Length,
                        isIntGteZero: "必须是正整数",
                    },
                    laneDeparture_risk_high_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize0To220Length,
                        isIntGteZero: "必须是正整数",
                    },
                    block_risk_high_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize0To220Length,
                        isIntGteZero: "必须是正整数",
                    },
                    collisionRiskValue_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize0To220Length,
                        isIntGteZero: "必须是正整数",
                    },
                    // 频繁变道
                    frequent_risk_high_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize0To220Length,
                        isIntGteZero: "必须是正整数"
                    },
                    //急加急减急转弯
                    hurry_risk_high_text: {
                        isCheckedRequested: publicNull,
                        isCheckedNumber2: riskSize0To220Length,
                        isIntGteZero: "必须是正整数"
                    }
                }

            }).form();
        },
        checkspeed: function () {
            //疲劳驾驶 注意力分散 违规异常速度校验
            var hightspeed = ["#closeEyes_risk_high_text", "#yawn_risk_high_text", "#smoking_risk_high_text", "#answerThephone_risk_high_text", "#distraction_risk_high_text", "#abnormalAlarm_risk_high_text"];
            var allspeed = ["#closeEyes_risk_high_text", "#yawn_risk_high_text", "#all_LowSpeed_text", "#smoking_risk_high_text", "#answerThephone_risk_high_text", "#distraction_risk_high_text", "#abnormalAlarm_risk_high_text"];
            //空值检测
            for (var i = 0; i < allspeed.length; i++) {
                $(allspeed[i]).on("focus", function () {
                    var Currentinputbox = $(this).val();
                    if (Currentinputbox == "") {
                        $(this).nextAll().remove();
                        for (var j = 0; j < allspeed.length; j++) {
                            var idname = $(this).attr("id");
                            if (allspeed[j] !== "#" + idname) {
                                $(allspeed[j]).attr("disabled", "disabled");
                            }
                        }
                    } else {
                        for (var j = 0; j < allspeed.length; j++) {
                            $(allspeed[j]).removeAttr("disabled");

                        }

                    }

                })
            }

            //疲劳驾驶 注意力分散 违规异常高速校验(高速值必须大于低速值)
            for (var i = 0; i < hightspeed.length; i++) {
                $(hightspeed[i]).on("input", function () {
                    var hightspeed1 = $(this).val();
                    var lowspeed = $("#all_LowSpeed_text").val();
                    //为空时，屏蔽其他输入框
                    if (!hightspeed1) {
                        $(this).nextAll().remove();
                        checkspeedvalue = false;
                        for (var j = 0; j < allspeed.length; j++) {
                            var idname = $(this).attr("id");
                            if (allspeed[j] !== "#" + idname) {
                                $(allspeed[j]).attr("disabled", "disabled");
                            }
                        }
                    } else if (parseInt(lowspeed) > parseInt(hightspeed1)) {
                        $(this).nextAll().remove();
                        var t = '<label  class="error2" >高速值必须大于低速值</label>';
                        $(this).after(t);
                        checkspeedvalue = false;
                        for (var j = 0; j < allspeed.length; j++) {
                            var idname = $(this).attr("id");
                            if (allspeed[j] !== "#" + idname) {
                                $(allspeed[j]).attr("disabled", "disabled");
                            }
                        }
                    } else {
                        $(this).nextAll().remove();
                        for (var j = 0; j < allspeed.length; j++) {
                            $(allspeed[j]).removeAttr("disabled");
                        }
                        checkspeedvalue = true;

                    }
                })
            }
            //疲劳驾驶 注意力分散 违规异常低速校验
            $("#all_LowSpeed_text").on("input", function () {
                var allhightspeedvalue = [];
                for (var i = 0; i < hightspeed.length; i++) {
                    var hightspeedvalue = $(hightspeed[i]).val();
                    allhightspeedvalue.push(parseInt(hightspeedvalue));
                }
                for (var i = 0; i < allhightspeedvalue.length; i++) {
                    if (isNaN(allhightspeedvalue[i])) {
                        allhightspeedvalue.splice(i, 1);
                        i = i - 1;

                    }

                }
                var hightspeedmin = Math.min.apply(null, allhightspeedvalue);
                var lowspeed = $("#all_LowSpeed_text").val();
                //为空时，屏蔽其他输入框
                if (!lowspeed) {
                    $(this).nextAll().remove();
                    checkspeedvalue = false;
                    for (var j = 0; j < allspeed.length; j++) {
                        var idname = $(this).attr("id");
                        if (allspeed[j] !== "#" + idname) {
                            $(allspeed[j]).attr("disabled", "disabled");
                        }
                    }
                } else if (parseInt(lowspeed) > parseInt(hightspeedmin)) {
                    $(this).nextAll().remove();
                    var t = '<label  class="error2" >高速值必须大于低速值</label>';
                    $(this).after(t);
                    checkspeedvalue = false;
                    for (var j = 0; j < allspeed.length; j++) {
                        var idname = $(this).attr("id");
                        if (allspeed[j] !== "#" + idname) {
                            $(allspeed[j]).attr("disabled", "disabled");
                        }
                    }
                } else {
                    $(this).nextAll().remove();
                    for (var j = 0; j < allspeed.length; j++) {
                        $(allspeed[j]).removeAttr("disabled");
                    }
                    checkspeedvalue = true;

                }

            })
            //碰撞危险高速校验
            var hightspeed_collision = ["#forwardCollision_risk_high_text", "#pedestrianCollision_risk_high_text", "#tooClose_risk_high_text", "#frequentLaneChange_risk_high_text", "#laneDeparture_risk_high_text", "#block_risk_high_text"];
            var allspeed_collision = ["#forwardCollision_risk_high_text", "#pedestrianCollision_risk_high_text", "#tooClose_risk_high_text", "#frequentLaneChange_risk_high_text", "#laneDeparture_risk_high_text", "#block_risk_high_text", "#collisionRiskValue_text"];
            //空值检测
            for (var i = 0; i < allspeed_collision.length; i++) {
                $(allspeed_collision[i]).on("focus", function () {
                    var Currentinputbox2 = $(this).val();
                    if (Currentinputbox2 == "") {
                        $(this).nextAll().remove();
                        for (var j = 0; j < allspeed_collision.length; j++) {
                            var idname = $(this).attr("id");
                            if (allspeed_collision[j] !== "#" + idname) {
                                $(allspeed_collision[j]).attr("disabled", "disabled");
                            }
                        }
                    } else {
                        for (var j = 0; j < allspeed_collision.length; j++) {
                            $(allspeed_collision[j]).removeAttr("disabled");

                        }

                    }

                })
            }


            for (var i = 0; i < hightspeed_collision.length; i++) {
                $(hightspeed_collision[i]).on("input", function () {
                    var hightspeed1 = $(this).val();
                    var lowspeed = $("#collisionRiskValue_text").val();
                    //为空时，屏蔽其他输入框
                    if (!hightspeed1) {
                        checkspeedvalue = false;
                        for (var j = 0; j < allspeed_collision.length; j++) {
                            var idname = $(this).attr("id");
                            if (allspeed_collision[j] !== "#" + idname) {
                                $(allspeed_collision[j]).attr("disabled", "disabled");
                            }
                        }
                    } else if (parseInt(lowspeed) > parseInt(hightspeed1)) {
                        $(this).nextAll().remove();
                        var t = '<label  class="error2" >高速值必须大于低速值</label>';
                        $(this).after(t);
                        checkspeedvalue = false;
                        for (var j = 0; j < allspeed_collision.length; j++) {
                            var idname = $(this).attr("id");
                            if (allspeed_collision[j] !== "#" + idname) {
                                $(allspeed_collision[j]).attr("disabled", "disabled");
                            }
                        }

                    } else {
                        $(this).nextAll().remove();
                        for (var j = 0; j < allspeed_collision.length; j++) {
                            $(allspeed_collision[j]).removeAttr("disabled");
                        }
                        checkspeedvalue = true;

                    }
                })
            }
            //碰撞危险低速校验
            $("#collisionRiskValue_text").on("input", function () {
                var allhightspeedvalue = [];
                for (var i = 0; i < hightspeed_collision.length; i++) {
                    var hightspeedvalue = $(hightspeed_collision[i]).val();
                    allhightspeedvalue.push(parseInt(hightspeedvalue));
                }
                for (var i = 0; i < allhightspeedvalue.length; i++) {
                    if (isNaN(allhightspeedvalue[i])) {
                        allhightspeedvalue.splice(i, 1);
                        i = i - 1;

                    }

                }
                var hightspeedmin = Math.min.apply(null, allhightspeedvalue);
                var lowspeed = $("#collisionRiskValue_text").val();
                //为空时，屏蔽其他输入框
                if (!lowspeed) {
                    checkspeedvalue = false;
                    for (var j = 0; j < allspeed_collision.length; j++) {
                        var idname = $(this).attr("id");
                        if (allspeed_collision[j] !== "#" + idname) {
                            $(allspeed_collision[j]).attr("disabled", "disabled");
                        }
                    }
                } else if (parseInt(lowspeed) > parseInt(hightspeedmin) || lowspeed == "") {
                    $(this).nextAll().remove();
                    var t = '<label  class="error2" >高速值必须大于低速值</label>';
                    $(this).after(t);
                    checkspeedvalue = false;
                    for (var j = 0; j < allspeed_collision.length; j++) {
                        var idname = $(this).attr("id");
                        if (allspeed_collision[j] !== "#" + idname) {
                            $(allspeed_collision[j]).attr("disabled", "disabled");
                        }
                    }
                } else {
                    $(this).nextAll().remove();
                    for (var j = 0; j < allspeed_collision.length; j++) {
                        $(allspeed_collision[j]).removeAttr("disabled");
                    }
                    checkspeedvalue = true;

                }
            })
        }
    }
    $(function () {
        $(".modal-body").css({"height": "auto", "max-height": ($(window).height() - 194) + "px"});
        $("#editForm input[type='checkbox']").prop('checked', true).hide();
        riskdefineSet.init();
        riskdefineSet.checkspeed();
        $("input").inputClear();
        riskdefineSet.validates();
        $(".hiddenparameter").bind("click", riskdefineSet.hiddenparameterFn);
        $("#doSubmits").bind("click", riskdefineSet.submitFrom);
    })
})(window, $);