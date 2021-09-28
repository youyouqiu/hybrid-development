//# sourceURL=zhongWeiSetting.js
// ps: 设置、修改、批量设置共用本页面,settingParam不为空时表示是修改界面
// ps: 页面中的参数id基本上都是以参数名+'_'+报警id命名(便于数据组装与赋值)
(function (window, $) {
    var platformParamSetting = $("#platformParamSetting").val();
    var settingPlat = JSON.parse(platformParamSetting ? platformParamSetting : '[]');
    var riskSettingList = $('#riskSettingList').val();
    var settingParam = JSON.parse(riskSettingList ? riskSettingList : '[]');
    var addSendUrl = '/clbs/adas/standard/param/batch/config';
    var editSendUrl = '/clbs/adas/standard/param/setting.gsp';
    var disabledTabObj = {};// 存储禁用本页签下发按钮的页签
    var curTabSend = $('#curTabSend');// 本页签下发按钮
    // 提交参数需进行10倍转换的字段(提交*10,渲染/10)
    var conversionTen = ['rear', 'timeDistanceThreshold', 'leftDistance', 'rightDistance',
        'pressure', 'lowPressure', 'highPressure', 'highTemperature'];
    // 提交参数需进行100倍转换的字段(提交*100,渲染/100)
    var conversionOneHundred = ['compensationFactorK', 'compensationFactorB'];
    var radioKey = ['oneLevelAlarmEnable', 'oneLevelVoiceReminder', 'oneLevelAuxiliaryMultimedia', 'twoLevelAlarmEnable',
        'twoLevelVoiceReminder', 'twoLevelAuxiliaryMultimedia', 'roadSignRecognition', 'voiceReminderEnable', 'auxiliaryEnable',
        'voiceReminderEnable'];
    var validateArr = [2164081, 2164082, 2164083, 2164091, 216406, 216501, 216504, 216503, 216502, 216508, 216516, 216510,
        216514, 216518, 216515, 216517, 216601, 216602, 216603, 216604, 216605, 216606, 216607, 216701, 216702, 216703, 216705];

    var zhongWeiSetting = {
        init: function () {
            $(".modal-body").addClass("modal-body-overflow");
            $(".modal-body").css({"height": "auto", "max-height": ($(window).height() - 194) + "px"});
            // 参考对象渲染
            zhongWeiSetting.referenceObjRender();
            zhongWeiSetting.referenceTireModel();//轮胎型号渲染
            // 修改界面,已设置的参数渲染
            if (settingParam.length > 0) {
                zhongWeiSetting.paramValueRender(settingParam, true);
            }
            if (settingPlat.length > 0) {
                zhongWeiSetting.platformValueRender(settingPlat);
            }
            // 表单验证方法
            zhongWeiSetting.forwardValidates();//前向参数设置
            zhongWeiSetting.driverBehaviorValidates();//驾驶员参数设置
            zhongWeiSetting.platformParamValidates();//平台参数设置
        },
        getValidateInfo: function () {
            var validateObj = {};
            var validateMsg = {};
            for (var i = 0; i < validateArr.length; i++) {
                var item = validateArr[i];
                validateObj['processingIntervalOne_' + item] = {
                    digits: true,
                    range: [30, 60]
                };
                validateObj['processingIntervalTwo_' + item] = {
                    digits: true,
                    range: [30, 60]
                };
                validateObj['timeThreshold_' + item] = {
                    digits: true,
                    range: [1, 60]
                };
                validateObj['timeAlarmNumThreshold_' + item] = {
                    digits: true,
                    range: [5, 100]
                };
                validateObj['distanceThreshold_' + item] = {
                    digits: true,
                    range: [1, 100]
                };
                validateObj['distanceAlarmNumThreshold_' + item] = {
                    digits: true,
                    range: [5, 100]
                };


                validateMsg['processingIntervalOne_' + item] = {
                    digits: "输入范围30-60之间的整数",
                    range: "输入范围30-60之间的整数"
                };
                validateMsg['processingIntervalTwo_' + item] = {
                    digits: "输入范围30-60之间的整数",
                    range: "输入范围30-60之间的整数"
                };
                validateMsg['timeThreshold_' + item] = {
                    digits: "输入范围1-60之间的整数",
                    range: "输入范围1-60之间的整数"
                };
                validateMsg['timeAlarmNumThreshold_' + item] = {
                    digits: '取值范围5-100之间的整数',
                    range: '取值范围5-100之间的整数'
                };
                validateMsg['distanceThreshold_' + item] = {
                    digits: '取值范围1-100之间的整数',
                    range: '取值范围1-100之间的整数'
                };
                validateMsg['distanceAlarmNumThreshold_' + item] = {
                    digits: '取值范围5-100之间的整数',
                    range: '取值范围5-100之间的整数'
                };
            }
            return {validateObj, validateMsg};
        },
        // 初始化参考对象下拉框数据
        referenceObjRender: function () {
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
                        obj.id = referVehicleList[i].vehicle_id;
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
            }).on('onSetSelectValue', function (e, keyword, data) {// 选择参考对象
                var vehicleId = keyword.id;
                $.ajax({
                    type: 'GET',
                    url: '/clbs/adas/standard/param/get_' + vehicleId + '.gsp',
                    async: false,
                    dataType: 'json',
                    success: function (data) {
                        if (data.success) {
                            $("#riskReference").val(keyword.key);
                            var result = JSON.parse(data.msg);
                            zhongWeiSetting.paramValueRender(result.alarmParam);
                            zhongWeiSetting.platformValueRender(result.platformParam);
                        } else {
                            layer.msg("获取参考对象数据失败");
                        }
                    },
                    error: function () {
                        layer.msg(systemError, {move: false});
                    }
                });
            }).on('onUnsetSelectValue', function () {
            });
        },
        //初始化轮胎型号
        referenceTireModel: function () {
            var tyreNumberList = JSON.parse($('#tireModel').val());

            // 初始化车辆数据
            var dataList = {value: []};
            if (tyreNumberList != null && tyreNumberList.length > 0) {
                for (var i = 0; i < tyreNumberList.length; i++) {
                    var item = tyreNumberList[i];
                    var obj = {};

                    obj.id = item.tireModelId;
                    obj.name = item.name;
                    dataList.value.push(obj);
                }
            }
            $("#tyreNumber").bsSuggest({
                indexId: 1, //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0,
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: dataList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {// 选择参考对象
                var tireModelId = keyword.id;
                $('#tyreNumber_156608').val(tireModelId);
            }).on('onUnsetSelectValue', function () {
            });
        },
        /**
         * 数据渲染
         * @param dataList
         * (dataList:修改界面或者切换参考对象时传递过来的数据)
         * */
        paramValueRender: function (dataList) {
            for (var i = 0, len = dataList.length; i < len; i++) {
                var item = dataList[i];
                var adasAlarmParamSettings = item.adasAlarmParamSettings;// 报警参数
                var commonParamSetting = item.commonParamSetting;// 通用参数
                var paramType = commonParamSetting.paramType;
                zhongWeiSetting.setParamValue(commonParamSetting, paramType);

                // console.log('报警参数', adasAlarmParamSettings);
                for (var j = 0, adasLen = adasAlarmParamSettings.length; j < adasLen; j++) {
                    var data = adasAlarmParamSettings[j];
                    var alarmId = data.riskFunctionId;

                    zhongWeiSetting.setParamValue(data, alarmId);
                }
            }
        },
        setParamValue: function (data, id) {
            for (key in data) {
                var curVal = (data[key] == '-1' && key != 'tyreNumber') ? '' : data[key];
                if (conversionTen.indexOf(key) > -1 && curVal != '') {// 时距阈值需要除以10
                    curVal = parseFloat((curVal / 10).toFixed(1));
                }
                if (conversionOneHundred.indexOf(key) > -1 && curVal != '') {// 时距阈值需要除以10
                    curVal = parseFloat((curVal / 100).toFixed(2));
                }
                if (radioKey.indexOf(key) !== -1) {
                    var curTarget = $('.' + key + '_' + id + '[value=' + curVal + ']');
                    curTarget.prop('checked', true);
                    if (key.indexOf('Status') != -1) {// 触发状态切换
                        zhongWeiSetting.touchStatusChange(curTarget);
                    }
                    if (key.indexOf('larmEnable') != -1) {// 一级二级报警切换
                        zhongWeiSetting.oneTwoLevelAlarmChange(curTarget);
                    }
                    if (key.indexOf('auxiliaryEnable') != -1 || key.indexOf('voiceReminderEnable') != -1 || key.indexOf('roadSign') != -1) {// 道路标识识别切换
                        zhongWeiSetting.roadSignRecognitionChange(curTarget);
                    }
                }
                var targetId = $('#' + key + '_' + id);
                targetId.val(curVal);

                var newId = '';
                if (id.toString().indexOf('216402') !== -1) {// 左偏离/右偏离
                    newId = 216402;
                }
                if (id.toString().indexOf('216408') !== -1) {// 急加速、急减速、急转弯
                    newId = 216408;
                }
                if (id === 216510 || id === 216514) {// 遮挡/红外阻断
                    newId = '2165104';
                }
                var targetId = $('#' + key + '_' + newId);
                targetId.val(curVal);
            }
        },
        platformValueRender: function (dataList) {
            for (var i = 0; i < dataList.length; i++) {
                var data = dataList[i];
                var alarmId = data.riskFunctionId;
                zhongWeiSetting.newSetParamValue(data, alarmId);
            }
        },
        newSetParamValue: function (data, id) {
            for (key in data) {
                var curVal = (data[key] == '-1' ? '' : data[key]);
                var newId = key + '_' + id;
                var targetId = $('input[name="' + newId + '"]');
                if (key.indexOf('alarmSwitch') != -1) {
                    if (data[key] == 1) {
                        var blockChange = '.' + newId;
                        $('' + blockChange + '').parent().parent().find('.selectbutton').css("left", "9px");
                    } else {
                        $('' + blockChange + '').parent().parent().find('.selectbutton').css("left", "55px");
                    }
                }
                if (key.indexOf('processingIntervalOne') != -1 && curVal != '') {
                    curVal = curVal / 60;
                }
                if (key.indexOf('processingIntervalTwo') != -1 && curVal != '') {
                    curVal = curVal / 60;
                }
                if (key.indexOf('timeThreshold') != -1 && curVal != '') {
                    curVal = curVal / 60;
                }
                if (key.indexOf('alarmSwitch') != -1) {
                    var alarmSwitch = $('input[name=alarmSwitch_' + id + ']');
                    if (curVal == 0) {
                        alarmSwitch.parent().parent().parent().find('.col-md-10').css('display', 'none');
                    }
                    if (curVal == 1) {
                        alarmSwitch.parent().parent().parent().find('.col-md-10').css('display', 'block');
                    }
                }
                targetId.val(curVal);
                targetId = $('select[name="' + newId + '"]');
                targetId.val(curVal);
            }
        },

        /**
         * 提交方法
         * @param:flag
         * ('all':全部下发;'curTab':本页签下发;'save':保存)
         * */
        paramSend: function (flag) {
            var parameter = {
                'vehicleIds': $('#vehicleId').val(),
                'alarmParam': [],
                'platformParam': [],
                'sendFlag': flag == 'save' ? false : true,
            };
            if (flag == 'curTab') {
                var curForm = $('.tab-pane.active').find('form');
                var paramType = curForm.find('.paramType').val();
                if (paramType == 0) {
                    var platformParamForm = zhongWeiSetting.setFormPlatformParam(curForm);
                    for (key in platformParamForm) {
                        parameter.platformParam.push(platformParamForm[key]);
                    }
                } else {
                    var result = zhongWeiSetting.setFormParam(curForm);
                    parameter.alarmParam.push(result);
                }

                var validates = curForm.attr('id').replace('Form', '') + 'Validates';
                if (!zhongWeiSetting[validates]()) {
                    if ($('#platformSet').hasClass('active')) {
                        layer.msg('设置参数有误');
                    }
                    return;
                }
                curTabSend.prop('disabled', true);
                var curTabId = $('.nav-tabs li.active').attr('id');
                if (!disabledTabObj[curTabId]) {
                    disabledTabObj[curTabId] = setTimeout(function () {
                        disabledTabObj[curTabId] = null;
                        zhongWeiSetting.curTabSendDisabled('timeOut');
                    }, 5000)
                }
            } else {
                if (!zhongWeiSetting.forwardValidates()
                    || !zhongWeiSetting.driverBehaviorValidates()
                    || !zhongWeiSetting.blindSpotValidates()
                    || !zhongWeiSetting.tirePressureValidates()
                    || !zhongWeiSetting.platformParamValidates()
                ) {
                    layer.msg('设置参数有误');
                    return;
                }
                var forwardResult = zhongWeiSetting.setFormParam($("#forwardForm"));
                var driverBehaviorResult = zhongWeiSetting.setFormParam($("#driverBehaviorForm"));
                var blindSpotResult = zhongWeiSetting.setFormParam($("#blindSpotForm"));//盲区
                var tirePressureResult = zhongWeiSetting.setFormParam($("#tirePressureForm"));//胎压
                parameter.alarmParam.push(
                    forwardResult,
                    driverBehaviorResult,
                    blindSpotResult,
                    tirePressureResult
                );

                var platformParamForm = zhongWeiSetting.setFormPlatformParam($("#platformParamForm"));
                for (key in platformParamForm) {
                    parameter.platformParam.push(platformParamForm[key]);
                }
            }
            // console.log( parameter.platformParam);
            parameter.alarmParam = JSON.stringify(parameter.alarmParam);
            parameter.platformParam = JSON.stringify(parameter.platformParam);
            var url = settingParam.length == 0 ? addSendUrl : editSendUrl;
            json_ajax("POST", url, "json", true, parameter, function (data) {
                zhongWeiSetting.paramSendCallback(data, flag);
            });

            $('.checkAll').prop('checked', false);
        },
        paramSendCallback: function (data, flag) {
            if (data.success) {
                var msgTitle = '下发成功';
                if (flag != 'curTab') {
                    $('#commonWin').modal('hide');
                    if (flag == 'save') {
                        msgTitle = '保存成功';
                    }
                } else {
                    var curForm = $('.tab-pane.active').find('form');
                    var paramType = curForm.find('.paramType').val();
                    if (paramType == 0) {
                        var activeName = $('.nav-tabs li.active').text().replace('设置', '');
                        msgTitle = activeName + ' 保存成功';
                    } else {
                        var activeName = $('.nav-tabs li.active').text().replace('设置', '');
                        msgTitle = activeName + ' 下发成功';
                    }
                }
                layer.msg(msgTitle);
                myTable.requestData();
            } else if (data.msg) {
                layer.msg(data.msg);
            }
        },
        // 表单提交数据组装(ps:因功能需要:输入框值为空时,设置该值为-1再传递到后端)
        setFormParam: function (curForm) {
            var paramList = curForm.serializeArray();
            var vehicleId = $('#vehicleId').val();
            var commonParamSettingObj = {// 通用参数
                'vehicleId': vehicleId,
                'protocolType': 21,
            };
            var paramType = curForm.find('.paramType').val();
            var adasAlarmParamSettings = [];// 报警参数
            var alarmObj = {};
            for (var i = 0, len = paramList.length; i < len; i++) {
                var item = paramList[i];
                var attrName = item.name;
                var attrValue = (item.value == '' ? '-1' : item.value);
                if (attrName.indexOf('_') == -1) {// 组装通用参数
                    commonParamSettingObj[attrName] = attrValue;
                } else if (attrName.indexOf('forward') != -1 || attrName.indexOf('driverBehavior') != -1) {
                    commonParamSettingObj['touchStatus'] = attrValue;
                } else {// 组装报警参数
                    var alarmName = attrName.split('_')[0];
                    var alarmId = attrName.split('_')[1];
                    if (!alarmObj[alarmId]) {
                        alarmObj[alarmId] = {
                            'riskFunctionId': alarmId,
                            'vehicleId': vehicleId,
                            'paramType': paramType,
                        };
                    }
                    if (conversionTen.indexOf(alarmName) !== -1 && attrValue !== '-1') {
                        attrValue = attrValue * 10;
                    }
                    if (conversionOneHundred.indexOf(alarmName) !== -1 && attrValue !== '-1') {
                        attrValue = attrValue * 100;
                    }
                    alarmObj[alarmId][alarmName] = attrValue;
                }
            }
            var noNeedKey = ['216402', '216408', '2165104'];
            for (key in alarmObj) {
                // 部分报警有子报警类型,组装数据时需携带父报警参数
                if (key.length > 6 || key === '216510' || key === '216514') {
                    var newKey = key.substring(0, 6);
                    if (key === '216510' || key === '216514') {// 遮挡/红外阻断报警共用部分参数
                        newKey = '2165104';
                    }
                    if (alarmObj[newKey]) {
                        var obj = alarmObj[newKey];
                        alarmObj[key] = $.extend({}, obj, alarmObj[key]);
                    }
                }

                if (noNeedKey.indexOf(key) === -1) {
                    adasAlarmParamSettings.push(alarmObj[key]);
                }
            }
            var result = {
                'commonParamSetting': commonParamSettingObj,
                'adasAlarmParamSettings': adasAlarmParamSettings
            };
            return result;
        },
        // 平台参数设置组装数据
        setFormPlatformParam: function (curForm) {
            var paramList = curForm.serializeArray();
            var alarmObj = {};
            var paramType = curForm.find('.paramType').val();
            for (var i = 1; i < paramList.length; i++) {
                var item = paramList[i];
                var attrName = item.name;
                var attrValue = (item.value == '' ? '-1' : item.value);
                var alarmName = attrName.split('_')[0];
                var alarmId = attrName.split('_')[1];
                if (!alarmObj[alarmId]) {
                    alarmObj[alarmId] = {
                        'riskFunctionId': alarmId,
                        'vehicleId': $('#vehicleId').val(),
                        'protocolType': 21
                    };
                }
                if (alarmName.indexOf('processingIntervalOne') != -1 && attrValue != '-1') {
                    attrValue = attrValue * 60;
                }
                if (alarmName.indexOf('processingIntervalTwo') != -1 && attrValue != '-1') {
                    attrValue = attrValue * 60;
                }
                if (alarmName.indexOf('timeThreshold') != -1 && attrValue != '-1') {
                    attrValue = attrValue * 60;
                }

                alarmObj[alarmId][alarmName] = attrValue;
            }
            return alarmObj;

        },
        // 控制本页签下发按钮是否禁用
        curTabSendDisabled: function (flag) {
            var curTabId = $('.nav-tabs li.active').attr('id');
            if (flag != 'timeOut') {
                curTabId = $(this).attr('id');
            }
            if (!disabledTabObj[curTabId]) {
                curTabSend.prop('disabled', false);
            } else {
                curTabSend.prop('disabled', true);
            }

            if (curTabId == 'platformSet') {
                $("#doSubmits").hide();
                $("#curTabSend").text('保存本页签');
            } else {
                $("#doSubmits").show();
                $("#curTabSend").text('本页签下发');
            }
        },

        /**
         * 表单验证方法
         * */
        // 前向参数设置表单验证
        forwardValidates: function () {
            return $("#forwardForm").validate({
                ignore: '',
                rules: {
                    photographNumber: {
                        digits: true,
                        range: [0, 10]
                    },
                    timingPhotoInterval: {
                        digits: true,
                        range: [0, 3600]
                    },
                    channelOne: {
                        repeatSelect: '.priorChannel'
                    },
                    channelTwo: {
                        repeatSelect: '.priorChannel'
                    },
                    channelThree: {
                        repeatSelect: '.priorChannel'
                    },
                    distancePhotoInterval: {
                        decimalOne: true,
                        range: [0, 60]
                    },
                    recordingTime: {
                        digits: true,
                        range: [0, 60]
                    },
                    speedLimit: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_216401: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_216402: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_216403: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_216404: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_216405: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_216408: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_2164091: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_2164092: {
                        digits: true,
                        range: [0, 60]
                    },
                    photographNumber_216401: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_216402: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_216403: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_216404: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_216405: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_216408: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_2164091: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_2164092: {
                        digits: true,
                        range: [0, 10]
                    },
                    timeDistanceThreshold_216401: {
                        decimalOne: true,
                        range: [1, 5]
                    },
                    timeDistanceThreshold_216403: {
                        decimalOne: true,
                        range: [1, 5]
                    },
                    timeDistanceThreshold_216404: {
                        decimalOne: true,
                        range: [1, 5]
                    },
                    timeDistanceThreshold_216405: {
                        decimalOne: true,
                        range: [1, 5]
                    },
                    alarmLevelSpeedThreshold_216401: {
                        digits: true,
                        range: [1, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_216402: {
                        digits: true,
                        range: [1, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_216403: {
                        digits: true,
                        range: [1, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_216404: {
                        digits: true,
                        range: [1, 220],
                        speedLimitCompare: true
                    },
                    timeSlotThreshold_216405: {
                        digits: true,
                        range: [30, 120],
                    },
                    alarmLevelSpeedThreshold_216405: {
                        digits: true,
                        range: [1, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_216408: {
                        digits: true,
                        range: [1, 220],
                        speedLimitCompare: true
                    },
                    timeSlotThreshold_216405: {
                        digits: true,
                        range: [30, 120]
                    },
                    frequencyThreshold_216405: {
                        digits: true,
                        range: [3, 10]
                    },
                },
                messages: {
                    frequencyThreshold_216405: {
                        digits: frequencyError,
                        range: frequencyError
                    },
                    timeSlotThreshold_216405: {
                        digits: timeSlotError,
                        range: timeSlotError
                    },
                    channelOne: {
                        repeatSelect: '请勿重复选择通道'
                    },
                    channelTwo: {
                        repeatSelect: '请勿重复选择通道'
                    },
                    channelThree: {
                        repeatSelect: '请勿重复选择通道'
                    },
                    alarmLevelSpeedThreshold_216401: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_216402: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_216403: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_216404: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    frequencyThreshold_216405: {
                        digits: '输入类型正整数，取值范围30-120',
                        range: '输入类型正整数，取值范围30-120',
                    },
                    timeSlotThreshold_216405: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_216408: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    timeDistanceThreshold_216405: {
                        decimalOne: timeDistanceError,
                        range: timeDistanceError
                    },
                    timeDistanceThreshold_216404: {
                        decimalOne: timeDistanceError,
                        range: timeDistanceError
                    },
                    timeDistanceThreshold_216403: {
                        decimalOne: timeDistanceError,
                        range: timeDistanceError
                    },
                    timeDistanceThreshold_216401: {
                        decimalOne: timeDistanceError,
                        range: timeDistanceError
                    },
                    photographNumber_216401: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_216402: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_216403: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_216404: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_216405: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_216408: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_2164091: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_2164092: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    videoRecordingTime_216405: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_216404: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_216403: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_216402: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_216401: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_216408: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_2164091: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_2164092: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    photographNumber: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    timingPhotoInterval: {
                        digits: timingPhoto1Error,
                        range: timingPhoto1Error
                    },
                    distancePhotoInterval: {
                        decimalOne: distancePhotoError,
                        range: distancePhotoError
                    },
                    recordingTime: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    speedLimit: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                }
            }).form();
        },
        // 驾驶员行为参数设置表单验证
        driverBehaviorValidates: function () {
            return $("#driverBehaviorForm").validate({
                ignore: '',
                rules: {
                    photographNumber: {
                        digits: true,
                        range: [0, 10]
                    },
                    timingPhotoInterval: {
                        digits: true,
                        range: [60, 60000]
                    },
                    channelOne: {
                        repeatSelect: '.driveChannel'
                    },
                    channelTwo: {
                        repeatSelect: '.driveChannel'
                    },
                    channelThree: {
                        repeatSelect: '.driveChannel'
                    },
                    distancePhotoInterval: {
                        decimalOne: true,
                        range: [0, 60]
                    },
                    recordingTime: {
                        digits: true,
                        range: [0, 60]
                    },
                    timeSlotThreshold_216502: {
                        digits: true,
                        range: [1, 3600]
                    },
                    timeSlotThreshold_216503: {
                        digits: true,
                        range: [1, 3600]
                    },
                    speedLimit: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_216503: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_216502: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_216501: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_216504: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_216508: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_216515: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_216516: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_216518: {
                        digits: true,
                        range: [0, 60]
                    },
                    videoRecordingTime_2165104: {
                        digits: true,
                        range: [0, 60]
                    },
                    photographNumber_216501: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_216502: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_216503: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_216504: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_216508: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_216515: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_216516: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_216518: {
                        digits: true,
                        range: [0, 10]
                    },
                    photographNumber_2165104: {
                        digits: true,
                        range: [0, 10]
                    },
                    alarmLevelSpeedThreshold_216501: {
                        digits: true,
                        range: [1, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_216502: {
                        digits: true,
                        range: [1, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_216503: {
                        digits: true,
                        range: [1, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_216504: {
                        digits: true,
                        range: [1, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_216508: {
                        digits: true,
                        range: [1, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_216515: {
                        digits: true,
                        range: [1, 240],
                    },
                    alarmLevelSpeedThreshold_216518: {
                        digits: true,
                        range: [1, 220],
                        speedLimitCompare: true
                    },
                    alarmLevelSpeedThreshold_2165104: {
                        digits: true,
                        range: [1, 220],
                        speedLimitCompare: true
                    },
                },
                messages: {
                    timeSlotThreshold_216502: {
                        digits: '输入类型正整数，取值范围 1-3600',
                        range: '输入类型正整数，取值范围 1-3600'
                    },
                    timeSlotThreshold_216503: {
                        digits: '输入类型正整数，取值范围 1-3600',
                        range: '输入类型正整数，取值范围 1-3600'
                    },
                    photographNumber_216501: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_216502: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    channelOne: {
                        repeatSelect: '请勿重复选择通道'
                    },
                    channelTwo: {
                        repeatSelect: '请勿重复选择通道'
                    },
                    channelThree: {
                        repeatSelect: '请勿重复选择通道'
                    },
                    photographNumber_216503: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_216504: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_216508: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_216515: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_216516: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_216518: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    photographNumber_2165104: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    alarmLevelSpeedThreshold_216501: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_216502: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_216503: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_216504: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_216508: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_216515: {
                        digits: alarmLevelError,
                        range: '取值范围1-240之间的整数'
                    },
                    alarmLevelSpeedThreshold_216518: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    alarmLevelSpeedThreshold_2165104: {
                        digits: alarmLevelError,
                        range: alarmLevelError
                    },
                    videoRecordingTime_216503: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_216502: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_216501: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_216504: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_216508: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_216515: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_216516: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_216518: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    videoRecordingTime_2165104: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    photographNumber: {
                        digits: photographNumberError,
                        range: photographNumberError
                    },
                    timingPhotoInterval: {
                        digits: timingPhotoError,
                        range: timingPhotoError
                    },
                    distancePhotoInterval: {
                        decimalOne: distancePhotoError,
                        range: distancePhotoError
                    },
                    recordingTime: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    speedLimit: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                }

            }).form();
        },
        //盲区表单验证
        blindSpotValidates: function () {
            return $("#blindSpotForm").validate({
                ignore: '',
                rules: {
                    rear_216704: {
                        decimalOne: true,
                        range: [0.1, 20]
                    },
                    leftDistance_216704: {
                        decimalOne: true,
                        range: [0.1, 10]
                    },
                    rightDistance_216704: {
                        decimalOne: true,
                        range: [0.1, 20]
                    },
                },
                messages: {
                    rear_216704: {
                        decimalOne: '取值范围0.1-20，允许一位小数',
                        range: '取值范围0.1-20，允许一位小数'
                    },
                    leftDistance_216704: {
                        decimalOne: '取值范围0.1-10，允许一位小数',
                        range: '取值范围0.1-10，允许一位小数'
                    },
                    rightDistance_216704: {
                        decimalOne: '取值范围0.1-20，允许一位小数',
                        range: '取值范围0.1-20，允许一位小数'
                    },
                }
            }).form()
        },
        //胎压表单验证
        tirePressureValidates: function () {
            return $("#tirePressureForm").validate({
                ignore: '',
                rules: {
                    compensationFactorK_216608: {
                        decimalTwo: true,
                        range: [1, 200]
                    },
                    compensationFactorB_216608: {
                        decimalTwo: true,
                        range: [0, 200]
                    },
                    pressure_216608: {
                        decimalOne: true,
                        range: [1, 10]
                    },
                    pressureThreshold_216608: {
                        digits: true,
                        range: [0, 100]
                    },
                    slowLeakThreshold_216608: {
                        digits: true,
                        range: [0, 100]
                    },
                    highTemperature_216608: {
                        digits: true,
                        range: [0, 100]
                    },
                    lowPressure_216608: {
                        decimalOne: true,
                        range: [1, 10]
                    },
                    highPressure_216608: {
                        decimalOne: true,
                        range: [1, 10]
                    },
                    electricityThreshold_216608: {
                        digits: true,
                        range: [1, 100]
                    },
                },
                messages: {
                    compensationFactorK_216608: {
                        decimalTwo: '取值范围1-200，允许两位小数',
                        range: '取值范围1-200，允许两位小数'
                    },
                    compensationFactorB_216608: {
                        decimalTwo: '取值范围0-200，允许两位小数',
                        range: '取值范围0-200，允许两位小数'
                    },
                    pressure_216608: {
                        decimalOne: '取值范围1-10，允许一位小数',
                        range: '取值范围1-10，允许一位小数'
                    },
                    pressureThreshold_216608: {
                        digits: '输入类型正整数，取值范围0-100',
                        range: '输入类型正整数，取值范围0-100'
                    },
                    slowLeakThreshold_216608: {
                        digits: '输入类型正整数，取值范围0-100',
                        range: '输入类型正整数，取值范围0-100'
                    },
                    highTemperature_216608: {
                        digits: '输入类型正整数，取值范围0-100',
                        range: '输入类型正整数，取值范围0-100'
                    },
                    lowPressure_216608: {
                        decimalOne: '取值范围1-10，允许一位小数',
                        range: '取值范围1-10，允许一位小数'
                    },
                    highPressure_216608: {
                        decimalOne: '取值范围1-10，允许一位小数',
                        range: '取值范围1-10，允许一位小数'
                    },
                    electricityThreshold_216608: {
                        digits: '输入类型正整数，取值范围1-100',
                        range: '输入类型正整数，取值范围1-100'
                    },
                }
            }).form()
        },
        // 平台参数设置表单验证
        platformParamValidates: function () {
            return $("#platformParamForm").validate({
                ignore: '',
                rules: Object.assign({
                    //抽烟
                    processingIntervalOne_216503: {
                        digits: true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_216503: {
                        digits: true,
                        range: [30, 60]
                    },
                    timeThreshold_216503: {
                        digits: true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_216503: {
                        digits: true,
                        range: [5, 100]
                    },
                    distanceThreshold_216503: {
                        digits: true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_216503: {
                        digits: true,
                        range: [5, 100]
                    },
                    //接打电话
                    processingIntervalOne_216502: {
                        digits: true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_216502: {
                        digits: true,
                        range: [30, 60]
                    },
                    timeThreshold_216502: {
                        digits: true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_216502: {
                        digits: true,
                        range: [5, 100]
                    },
                    distanceThreshold_216502: {
                        digits: true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_216502: {
                        digits: true,
                        range: [5, 100]
                    },
                    //分神驾驶
                    processingIntervalOne_156505: {
                        digits: true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156505: {
                        digits: true,
                        range: [30, 60]
                    },
                    timeThreshold_156505: {
                        digits: true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156505: {
                        digits: true,
                        range: [5, 100]
                    },
                    distanceThreshold_156505: {
                        digits: true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156505: {
                        digits: true,
                        range: [5, 100]
                    },
                    //驾驶员异常
                    processingIntervalOne_156504: {
                        digits: true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156504: {
                        digits: true,
                        range: [30, 60]
                    },
                    timeThreshold_156504: {
                        digits: true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156504: {
                        digits: true,
                        range: [5, 100]
                    },
                    distanceThreshold_156504: {
                        digits: true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156504: {
                        digits: true,
                        range: [5, 100]
                    },
                    //驾驶员变更
                    processingIntervalOne_156516: {
                        digits: true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156516: {
                        digits: true,
                        range: [30, 60]
                    },
                    timeThreshold_156516: {
                        digits: true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156516: {
                        digits: true,
                        range: [5, 100]
                    },
                    distanceThreshold_156516: {
                        digits: true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156516: {
                        digits: true,
                        range: [5, 100]
                    },
                    //前向碰撞
                    processingIntervalOne_216401: {
                        digits: true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_216401: {
                        digits: true,
                        range: [30, 60]
                    },
                    timeThreshold_216401: {
                        digits: true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_216401: {
                        digits: true,
                        range: [5, 100]
                    },
                    distanceThreshold_216401: {
                        digits: true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_216401: {
                        digits: true,
                        range: [5, 100]
                    },
                    //车道偏离
                    processingIntervalOne_216402: {
                        digits: true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_216402: {
                        digits: true,
                        range: [30, 60]
                    },
                    timeThreshold_216402: {
                        digits: true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_216402: {
                        digits: true,
                        range: [5, 100]
                    },
                    distanceThreshold_216402: {
                        digits: true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_216402: {
                        digits: true,
                        range: [5, 100]
                    },
                    //车距过近
                    processingIntervalOne_216403: {
                        digits: true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_216403: {
                        digits: true,
                        range: [30, 60]
                    },
                    timeThreshold_216403: {
                        digits: true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_216403: {
                        digits: true,
                        range: [5, 100]
                    },
                    distanceThreshold_216403: {
                        digits: true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_216403: {
                        digits: true,
                        range: [5, 100]
                    },
                    //行人碰撞
                    processingIntervalOne_216404: {
                        digits: true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_216404: {
                        digits: true,
                        range: [30, 60]
                    },
                    timeThreshold_216404: {
                        digits: true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_216404: {
                        digits: true,
                        range: [5, 100]
                    },
                    distanceThreshold_216404: {
                        digits: true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_216404: {
                        digits: true,
                        range: [5, 100]
                    },
                    //频繁变道
                    processingIntervalOne_216405: {
                        digits: true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_216405: {
                        digits: true,
                        range: [30, 60]
                    },
                    timeThreshold_216405: {
                        digits: true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_216405: {
                        digits: true,
                        range: [5, 100]
                    },
                    distanceThreshold_216405: {
                        digits: true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_216405: {
                        digits: true,
                        range: [5, 100]
                    },
                    //道路标识超限
                    processingIntervalOne_216409: {
                        digits: true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_216409: {
                        digits: true,
                        range: [30, 60]
                    },
                    timeThreshold_216409: {
                        digits: true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_216409: {
                        digits: true,
                        range: [5, 100]
                    },
                    distanceThreshold_216409: {
                        digits: true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_216409: {
                        digits: true,
                        range: [5, 100]
                    },
                    //胎压过高
                    processingIntervalOne_156601: {
                        digits: true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156601: {
                        digits: true,
                        range: [30, 60]
                    },
                    timeThreshold_156601: {
                        digits: true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156601: {
                        digits: true,
                        range: [5, 100]
                    },
                    distanceThreshold_156601: {
                        digits: true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156601: {
                        digits: true,
                        range: [5, 100]
                    },
                    //胎压过低
                    processingIntervalOne_156602: {
                        digits: true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156602: {
                        digits: true,
                        range: [30, 60]
                    },
                    timeThreshold_156602: {
                        digits: true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156602: {
                        digits: true,
                        range: [5, 100]
                    },
                    distanceThreshold_156602: {
                        digits: true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156602: {
                        digits: true,
                        range: [5, 100]
                    },
                    //胎温过高
                    processingIntervalOne_156603: {
                        digits: true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156603: {
                        digits: true,
                        range: [30, 60]
                    },
                    timeThreshold_156603: {
                        digits: true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156603: {
                        digits: true,
                        range: [5, 100]
                    },
                    distanceThreshold_156603: {
                        digits: true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156603: {
                        digits: true,
                        range: [5, 100]
                    },
                    //传感器异常
                    processingIntervalOne_156604: {
                        digits: true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156604: {
                        digits: true,
                        range: [30, 60]
                    },
                    timeThreshold_156604: {
                        digits: true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156604: {
                        digits: true,
                        range: [5, 100]
                    },
                    distanceThreshold_156604: {
                        digits: true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156604: {
                        digits: true,
                        range: [5, 100]
                    },
                    //胎压不平衡
                    processingIntervalOne_156605: {
                        digits: true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156605: {
                        digits: true,
                        range: [30, 60]
                    },
                    timeThreshold_156605: {
                        digits: true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156605: {
                        digits: true,
                        range: [5, 100]
                    },
                    distanceThreshold_156605: {
                        digits: true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156605: {
                        digits: true,
                        range: [5, 100]
                    },
                    //慢漏气
                    processingIntervalOne_156606: {
                        digits: true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156606: {
                        digits: true,
                        range: [30, 60]
                    },
                    timeThreshold_156606: {
                        digits: true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156606: {
                        digits: true,
                        range: [5, 100]
                    },
                    distanceThreshold_156606: {
                        digits: true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156606: {
                        digits: true,
                        range: [5, 100]
                    },
                    //电池电量低
                    processingIntervalOne_156607: {
                        digits: true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156607: {
                        digits: true,
                        range: [30, 60]
                    },
                    timeThreshold_156607: {
                        digits: true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156607: {
                        digits: true,
                        range: [5, 100]
                    },
                    distanceThreshold_156607: {
                        digits: true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156607: {
                        digits: true,
                        range: [5, 100]
                    },
                    //后方接近
                    processingIntervalOne_156701: {
                        digits: true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156701: {
                        digits: true,
                        range: [30, 60]
                    },
                    timeThreshold_156701: {
                        digits: true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156701: {
                        digits: true,
                        range: [5, 100]
                    },
                    distanceThreshold_156701: {
                        digits: true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156701: {
                        digits: true,
                        range: [5, 100]
                    },
                    //左侧后方接近
                    processingIntervalOne_156702: {
                        digits: true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156702: {
                        digits: true,
                        range: [30, 60]
                    },
                    timeThreshold_156702: {
                        digits: true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156702: {
                        digits: true,
                        range: [5, 100]
                    },
                    distanceThreshold_156702: {
                        digits: true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156702: {
                        digits: true,
                        range: [5, 100]
                    },
                    //右侧后方接近
                    processingIntervalOne_156703: {
                        digits: true,
                        range: [30, 60]
                    },
                    processingIntervalTwo_156703: {
                        digits: true,
                        range: [30, 60]
                    },
                    timeThreshold_156703: {
                        digits: true,
                        range: [1, 60]
                    },
                    timeAlarmNumThreshold_156703: {
                        digits: true,
                        range: [5, 100]
                    },
                    distanceThreshold_156703: {
                        digits: true,
                        range: [1, 100]
                    },
                    distanceAlarmNumThreshold_156703: {
                        digits: true,
                        range: [5, 100]
                    },
                }, zhongWeiSetting.getValidateInfo().validateObj),
                messages: Object.assign({
                    //抽烟
                    processingIntervalOne_216503: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_216503: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    timeThreshold_216503: {
                        digits: "输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_216503: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    distanceThreshold_216503: {
                        digits: '取值范围1-100之间的整数',
                        range: '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_216503: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    //接打电话
                    processingIntervalOne_216502: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_216502: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    timeThreshold_216502: {
                        digits: "输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_216502: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    distanceThreshold_216502: {
                        digits: '取值范围1-100之间的整数',
                        range: '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_216502: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    //分神驾驶
                    processingIntervalOne_156505: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156505: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    timeThreshold_156505: {
                        digits: "输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156505: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156505: {
                        digits: '取值范围1-100之间的整数',
                        range: '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156505: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    //驾驶员异常
                    processingIntervalOne_156504: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156504: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    timeThreshold_156504: {
                        digits: "输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156504: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156504: {
                        digits: '取值范围1-100之间的整数',
                        range: '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156504: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    //驾驶员变更
                    processingIntervalOne_156516: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156516: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    timeThreshold_156516: {
                        digits: "输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156516: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156516: {
                        digits: '取值范围1-100之间的整数',
                        range: '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156516: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    //前向碰撞
                    processingIntervalOne_216401: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_216401: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    timeThreshold_216401: {
                        digits: "输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_216401: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    distanceThreshold_216401: {
                        digits: '取值范围1-100之间的整数',
                        range: '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_216401: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    //车道偏离
                    processingIntervalOne_216402: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_216402: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    timeThreshold_216402: {
                        digits: "输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_216402: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    distanceThreshold_216402: {
                        digits: '取值范围1-100之间的整数',
                        range: '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_216402: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    //车距过近
                    processingIntervalOne_216403: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_216403: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    timeThreshold_216403: {
                        digits: "输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_216403: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    distanceThreshold_216403: {
                        digits: '取值范围1-100之间的整数',
                        range: '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_216403: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    //行人碰撞
                    processingIntervalOne_216404: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_216404: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    timeThreshold_216404: {
                        digits: "输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_216404: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    distanceThreshold_216404: {
                        digits: '取值范围1-100之间的整数',
                        range: '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_216404: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    //频繁变道
                    processingIntervalOne_216405: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_216405: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    timeThreshold_216405: {
                        digits: "输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_216405: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    distanceThreshold_216405: {
                        digits: '取值范围1-100之间的整数',
                        range: '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_216405: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    //道路标识超限
                    processingIntervalOne_216409: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_216409: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    timeThreshold_216409: {
                        digits: "输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_216409: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    distanceThreshold_216409: {
                        digits: '取值范围1-100之间的整数',
                        range: '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_216409: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    //胎压过高
                    processingIntervalOne_156601: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156601: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    timeThreshold_156601: {
                        digits: "输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156601: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156601: {
                        digits: '取值范围1-100之间的整数',
                        range: '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156601: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    //胎压过低
                    processingIntervalOne_156602: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156602: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    timeThreshold_156602: {
                        digits: "输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156602: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156602: {
                        digits: '取值范围1-100之间的整数',
                        range: '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156602: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    //胎温过高
                    processingIntervalOne_156603: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156603: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    timeThreshold_156603: {
                        digits: "输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156603: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156603: {
                        digits: '取值范围1-100之间的整数',
                        range: '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156603: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    //传感器异常
                    processingIntervalOne_156604: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156604: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    timeThreshold_156604: {
                        digits: "输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156604: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156604: {
                        digits: '取值范围1-100之间的整数',
                        range: '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156604: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    //胎压不平衡
                    processingIntervalOne_156605: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156605: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    timeThreshold_156605: {
                        digits: "输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156605: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156605: {
                        digits: '取值范围1-100之间的整数',
                        range: '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156605: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    //慢漏气
                    processingIntervalOne_156606: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156606: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    timeThreshold_156606: {
                        digits: "输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156606: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156606: {
                        digits: '取值范围1-100之间的整数',
                        range: '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156606: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    //电池电量低
                    processingIntervalOne_156607: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156607: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    timeThreshold_156607: {
                        digits: "输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156607: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156607: {
                        digits: '取值范围1-100之间的整数',
                        range: '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156607: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    //后方接近
                    processingIntervalOne_156701: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156701: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    timeThreshold_156701: {
                        digits: "输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156701: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156701: {
                        digits: '取值范围1-100之间的整数',
                        range: '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156701: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    //左侧后方接近
                    processingIntervalOne_156702: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156702: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    timeThreshold_156702: {
                        digits: "输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156702: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156702: {
                        digits: '取值范围1-100之间的整数',
                        range: '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156702: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    //右侧后方接近
                    processingIntervalOne_156703: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    processingIntervalTwo_156703: {
                        digits: "输入范围30-60之间的整数",
                        range: "输入范围30-60之间的整数"
                    },
                    timeThreshold_156703: {
                        digits: "输入范围1-60之间的整数",
                        range: "输入范围1-60之间的整数"
                    },
                    timeAlarmNumThreshold_156703: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                    distanceThreshold_156703: {
                        digits: '取值范围1-100之间的整数',
                        range: '取值范围1-100之间的整数'
                    },
                    distanceAlarmNumThreshold_156703: {
                        digits: '取值范围5-100之间的整数',
                        range: '取值范围5-100之间的整数'
                    },
                }, zhongWeiSetting.getValidateInfo().validateMsg)
            }).form();
        },

        /**
         * 页面交互
         * 相关方法
         * */
        // 查看/隐藏更多参数
        hiddenparameterFn: function () {
            var clickId = $(this).attr('id');
            if (!($("#" + clickId + "-content").is(":hidden"))) {
                $("#" + clickId + "-content").slideUp();
                $("#" + clickId).find("font").text("显示更多");
                $("#" + clickId).find("span.fa").removeAttr("class").addClass("fa fa-chevron-down");
            } else {
                $("#" + clickId + "-content").slideDown();
                $("#" + clickId).find("font").text("隐藏参数");
                $("#" + clickId).find("span.fa").removeAttr("class").addClass("fa fa-chevron-up");
            }
        },
        // 触发状态单选按钮切换切换
        touchStatusChange: function (target) {
            var _this = target.target ? $(this) : target;
            var curVal = _this.val();
            var touchStatusInfo = _this.closest('.form-group').siblings('.touchStatusInfo');
            var timingInfo = touchStatusInfo.find('.timingInfo');// 定时拍照
            var distanceInfo = touchStatusInfo.find('.distanceInfo');// 定距拍照
            switch (curVal) {
                case '0':
                    touchStatusInfo.slideUp();
                    touchStatusInfo.find('input,select').prop('disabled', true);
                    break;
                case '1':
                    timingInfo.show();
                    distanceInfo.hide();
                    touchStatusInfo.find('input,select').prop('disabled', false);
                    distanceInfo.find('input,select').prop('disabled', true);
                    touchStatusInfo.slideDown();
                    break;
                case '2':
                    timingInfo.hide();
                    distanceInfo.show();
                    touchStatusInfo.find('input,select').prop('disabled', false);
                    timingInfo.find('input,select').prop('disabled', true);
                    touchStatusInfo.slideDown();
                    break;
                case '3':
                    timingInfo.hide();
                    distanceInfo.hide();
                    touchStatusInfo.find('input,select').prop('disabled', false);
                    timingInfo.find('input,select').prop('disabled', true);
                    distanceInfo.find('input,select').prop('disabled', true);
                    touchStatusInfo.slideDown();
                    break;
            }
        },
        // 一级报警与二级报警状态切换
        oneTwoLevelAlarmChange: function (target) {
            var _this = target.target ? $(this) : target;
            var sibVal = _this.closest('.col-md-4').siblings('.col-md-4').find('input[type="radio"]:checked').val();
            var levelAlarmInfo = _this.closest('.form-group').next('.levelAlarmInfo');
            if (_this.val() == '0' && sibVal == '0') {// 一级报警与二级报警都为关闭状态,隐藏相关参数
                levelAlarmInfo.slideUp();
                levelAlarmInfo.find('input,select').prop('disabled', true);
            } else {
                levelAlarmInfo.slideDown();
                levelAlarmInfo.find('input,select').prop('disabled', false);
            }

            // 左右偏离一级报警都被关闭时,隐藏相关参数设置选项
            var secondGroup = _this.closest('.secondGroup');
            if (secondGroup.length > 0) {
                var curLevelAlarmInfo = secondGroup.next('.levelAlarmInfo');
                var oneLevelAlarm = secondGroup.find('.levelAlarm:checked');
                var status = false;
                for (var i = 0; i < oneLevelAlarm.length; i++) {
                    if ($(oneLevelAlarm[i]).val() == '1') {
                        status = true;
                        break;
                    }
                }
                if (!status) {
                    curLevelAlarmInfo.slideUp();
                    curLevelAlarmInfo.find('input,select').prop('disabled', true);
                } else {
                    curLevelAlarmInfo.slideDown();
                    curLevelAlarmInfo.find('input,select').prop('disabled', false);
                }
            }
        },
        // 道路标识识别单选按钮切换切换
        roadSignRecognitionChange: function (target) {
            var _this = target.target ? $(this) : target;
            var curVal = _this.val();
            var roadSignInfo = _this.closest('.form-group').siblings('.roadSignInfo');
            if (curVal == '0') {
                roadSignInfo.slideUp();
                roadSignInfo.find('input,select').prop('disabled', true);
            } else {
                roadSignInfo.slideDown();
                roadSignInfo.find('input,select').prop('disabled', false);
            }
        },
        // 监听报警速度阈值变化
        speedLimitChange: function () {
            var thisVal = $(this).val();
            // var newThisVal = thisVal == '' ? '0' : thisVal;
            var speedList = $(this).closest('.tab-pane').find('.alarmLevelSpeedThreshold');
            for (var i = 0, len = speedList.length; i < len; i++) {
                var item = $(speedList[i]);
                var itemId = $(speedList[i]).attr('id');
                var itemVal = item.val();
                var itemError = item.siblings('label.error');
                if ((thisVal == '' && itemVal == '') || ((thisVal != '' && itemVal != '') && (Number(thisVal) < Number(itemVal)))) {
                    itemError.hide();
                } else {
                    if (itemError.length == 0) {
                        item.after('<label id="' + itemId + '-error" class="error" for="' + itemId + '" style="display: inline-block;">必须大于报警判断速度阈值</label>');
                    } else {
                        itemError.show();
                    }
                }
            }
        },
        // 滑块选择切换
        selectSwitch: function () {
            $(".leftselectbutton span").on('click', function () {
                var leftButton = $(this).siblings('.selectbutton').css('left');
                var left = $(this).css('left');
                var leveAlarmInfo = $(this).parent().parent().parent().find('.col-md-10');
                var className = $(this).attr('class');
                if (className == 'button0 button0flag') {
                    $(this).parent().find('.selectvalue').val('1');
                    leveAlarmInfo.slideDown();
                }
                if (className == 'button1 button1flag') {
                    $(this).parent().find('.selectvalue').val('0');
                    leveAlarmInfo.slideUp();
                }

                $(this).siblings('.selectbutton').animate({left: left}, 'fast');
            });
        },

        // 点击开关滑块切换
        topSwitch: function () {
            $('.open').on('click', function () {
                $(this).parent().parent().find('.selectbutton').animate({left: '9px'}, 'fast');
                $(".selectvalue").val('1');
                $('.clearfix .col-md-10').slideDown();
            });
            $('.off').on('click', function () {
                $(this).parent().parent().find('.selectbutton').animate({left: '55px'}, 'fast');
                $('.selectvalue').val('0')
                $('.clearfix .col-md-10').slideUp();
            })
        },
        // lable点击移动滑块
        labelClickFn: function () {
            var left = $(this).parent().parent().parent().find(".selectbutton").css('left');
            if (left == '55px') {
                $(this).parent().parent().parent().find('.selectbutton').animate({left: "9px"}, "fast");
                $(this).parent().parent().parent().find('.selectvalue').val('1');
                $(this).parent().parent().parent().find('.col-md-10').slideDown();
            } else if (left == '9px') {
                $(this).parent().parent().parent().find('.selectbutton').animate({left: "55px"}, "fast");
                $(this).parent().parent().parent().find('.selectvalue').val('0')
                $(this).parent().parent().parent().find('.col-md-10').slideUp();
            }
        },
        //匹配轮胎型号名称
        tireModel: function () {
            var value = $(this).val();
            var tyreNumberList = JSON.parse($("#tireModel").val());
            var res = -1;

            for (var i = 0; i < tyreNumberList.length; i++) {
                var item = tyreNumberList[i];
                if (value == item.name) {
                    res = item.tireModelId;
                    break;
                }
            }

            $('#tyreNumber_156608').val(res);
        },
    };
    $(function () {
        /**
         * 初始化
         */
        zhongWeiSetting.init();
        $("input").inputClear();

        /**
         * 页面交互
         * */
        // 查看/隐藏更多参数
        $(".hiddenparameter").bind("click", zhongWeiSetting.hiddenparameterFn);
        // 触发状态单选按钮切换切换
        $('.touchStatus').on('change', zhongWeiSetting.touchStatusChange);
        // 一级报警与二级报警状态切换
        $('.oneLevelAlarm,.twoLevelAlarm').on('change', zhongWeiSetting.oneTwoLevelAlarmChange);
        // 道路标识识别单选按钮切换
        $('.roadSignRecognition').on('change', zhongWeiSetting.roadSignRecognitionChange);
        // 监听报警速度阈值变化
        $('.speedLimit').on('input propertychange', zhongWeiSetting.speedLimitChange);
        // 监听页签切换,控制本页签下发按钮是否禁用
        $('.nav-tabs li').on('click', zhongWeiSetting.curTabSendDisabled);
        // 滑块选择切换
        zhongWeiSetting.selectSwitch();
        zhongWeiSetting.topSwitch();
        $(".typeName").bind('click', zhongWeiSetting.labelClickFn);
        // 悬停
        $("[data-toggle='tooltip']").tooltip();
        $("#textinfo span").hover(
            function () {
                $(this).css('color', '#6dcff6');
            },
            function () {
                $(this).css('color', '#5D5F63')
            }
        );
        $(".typeName").css("cursor", "pointer")
        $(".typeName").hover(
            function () {
                $(this).css('color', '#6dcff6');
            },
            function () {
                $(this).css('color', '#5D5F63')
            }
        );

        /**
         * 下发及保存
         * */
        $('#allParamSend').on('click', function () {
            zhongWeiSetting.paramSend('all');
        });
        $('#curTabSend').on('click', function () {
            zhongWeiSetting.paramSend('curTab');
        });
        $('#doSubmits').on('click', function () {
            zhongWeiSetting.paramSend('save');
        });
        $('#tyreNumber').on('blur', zhongWeiSetting.tireModel);
    })
})(window, $);