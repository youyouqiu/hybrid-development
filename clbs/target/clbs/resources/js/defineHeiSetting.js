//# sourceURL=defineHeiSetting.js
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
    var protocolType = 25;// 黑标协议值

    var defineHeiSetting = {
        init: function () {
            $(".modal-body").addClass("modal-body-overflow");
            $(".modal-body").css({"height": "auto", "max-height": ($(window).height() - 194) + "px"});

            // 参考对象渲染
            defineHeiSetting.referenceObjRender();

            // 修改界面,已设置的参数渲染
            if (settingParam.length > 0) {
                defineHeiSetting.paramValueRender(settingParam);
            }
            if (settingPlat.length > 0) {
                defineHeiSetting.platformValueRender(settingPlat);
            }

            // 表单验证方法
            defineHeiSetting.identificationValidates();
            defineHeiSetting.vehicleRunValidates();
            defineHeiSetting.driverBehaviorValidates();
            defineHeiSetting.equipmentFailureValidates();
            defineHeiSetting.platformParamValidates();
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
                            defineHeiSetting.platformValueRender(result.platformParam);
                            defineHeiSetting.paramValueRender(result.alarmParam);
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
        /**
         * 数据渲染
         * @param dataList
         * (dataList:修改界面或者切换参考对象时传递过来的数据)
         * */
        paramValueRender: function (dataList) {
            for (var i = 0, len = dataList.length; i < len; i++) {
                var item = dataList[i];
                var adasAlarmParamSettings = item.adasAlarmParamSettings;// 报警参数
                // console.log('报警参数', adasAlarmParamSettings);
                var commonParamSetting = item.commonParamSetting;// 通用参数
                var paramType = commonParamSetting.paramType;
                defineHeiSetting.setParamValue(commonParamSetting, paramType);
                for (var j = 0, adasLen = adasAlarmParamSettings.length; j < adasLen; j++) {
                    var data = adasAlarmParamSettings[j];
                    var alarmId = data.riskFunctionId;
                    defineHeiSetting.setParamValue(data, alarmId);
                }
            }
        },
        setParamValue: function (data, id) {
            for (key in data) {
                var curVal = (data[key] == '-1' && key != 'tyreNumber') ? '' : data[key];
                if (
                    (key.indexOf('timeDistanceThreshold') != -1
                        || key.indexOf('timeThreshold') != -1
                        || key.indexOf('photographTime') != -1
                        || key.indexOf('warningTimeThreshold') != -1
                    ) && curVal != '') {// 时距阈值需要除以10
                    curVal = parseFloat((curVal / 10).toFixed(1));
                }
                var targetId = $('#' + key + '_' + id);
                targetId.val(curVal);
            }
        },
        platformValueRender: function (dataList) {
            for (var i = 0; i < dataList.length; i++) {
                var data = dataList[i];
                var alarmId = data.riskFunctionId;
                defineHeiSetting.newSetParamValue(data, alarmId);
            }
        },
        newSetParamValue: function (data, id) {
            for (key in data) {
                var curVal = (data[key] == '-1' ? '' : data[key]);
                eventId = curVal;
                var newId = key + '_' + id;
                var targetId = $('#param-media-content input[name="' + newId + '"]');
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
                targetId = $('#param-media-content select[name="' + newId + '"]');
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
                var validates = curForm.attr('id').replace('Form', '') + 'Validates';
                if (!defineHeiSetting[validates]()) {
                    if ($('#platformSet').hasClass('active')) {
                        layer.msg('设置参数有误');
                    }
                    return;
                }
                var paramType = curForm.find('.paramType').val();
                if (paramType == 0) {
                    var platformParamForm = defineHeiSetting.setFormPlatformParam(curForm);
                    for (key in platformParamForm) {
                        parameter.platformParam.push(platformParamForm[key]);
                    }
                    parameter.sendFlag = false;
                } else {
                    var result = defineHeiSetting.setFormParam(curForm);
                    parameter.alarmParam.push(result);
                }
                curTabSend.prop('disabled', true);
                var curTabId = $('.nav-tabs li.active').attr('id');
                if (!disabledTabObj[curTabId]) {
                    disabledTabObj[curTabId] = setTimeout(function () {
                        disabledTabObj[curTabId] = null;
                        defineHeiSetting.curTabSendDisabled('timeOut');
                    }, 5000)
                }
            } else {
                if (!defineHeiSetting.identificationValidates()
                    || !defineHeiSetting.driverBehaviorValidates()
                    || !defineHeiSetting.vehicleRunValidates()
                    || !defineHeiSetting.equipmentFailureValidates()
                    || !defineHeiSetting.platformParamValidates()) {
                    layer.msg('设置参数有误');
                    return;
                }
                var forwardResult = defineHeiSetting.setFormParam($("#identificationForm"));
                var intenseDrivingResult = defineHeiSetting.setFormParam($("#vehicleRunForm"));
                var driverBehaviorResult = defineHeiSetting.setFormParam($("#driverBehaviorForm"));
                var blindSpotResult = defineHeiSetting.setFormParam($("#equipmentFailureForm"));//盲区
                parameter.alarmParam.push(
                    forwardResult,
                    driverBehaviorResult,
                    intenseDrivingResult,
                    blindSpotResult,
                );

                var platformParamForm = defineHeiSetting.setFormPlatformParam($("#platformParamForm"));
                for (key in platformParamForm) {
                    parameter.platformParam.push(platformParamForm[key]);
                }

            }
            var platformParam = JSON.parse(JSON.stringify(parameter.platformParam));
            var hasWarning = ['253901', '253902', '254001'];// 含有预警的报警类型
            for (var i = 0; i < platformParam.length; i++) {
                var item = platformParam[i];
                if (hasWarning.indexOf(item.riskFunctionId) !== -1) {
                    item.riskFunctionId = '-' + item.riskFunctionId;
                    parameter.platformParam.push(item);
                }
                if (item.riskFunctionId === '253801') {
                    item.riskFunctionId = '253803';
                    parameter.platformParam.push(item);
                }
            }
            parameter.alarmParam = JSON.stringify(parameter.alarmParam);
            parameter.platformParam = JSON.stringify(parameter.platformParam);
            var url = settingParam.length == 0 ? addSendUrl : editSendUrl;
            json_ajax("POST", url, "json", true, parameter, function (data) {
                defineHeiSetting.paramSendCallback(data, flag);
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
            var commonParamSettingObj = {// 通用参数
                'vehicleId': $('#vehicleId').val(),
                'protocolType': protocolType,
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
                } else {// 组装报警参数
                    var alarmName = attrName.split('_')[0];
                    var alarmId = attrName.split('_')[1];
                    if (!alarmObj[alarmId]) {
                        alarmObj[alarmId] = {
                            'riskFunctionId': alarmId,
                            'vehicleId': $('#vehicleId').val(),
                            'paramType': paramType,
                        };
                    }
                    if ((alarmName.indexOf('timeDistanceThreshold') != -1
                        || alarmName.indexOf('timeThreshold') != -1
                        || alarmName.indexOf('photographTime') != -1
                    ) && attrValue != '-1') {// 时距阈值需要*10
                        attrValue = attrValue * 10;
                    }
                    if (alarmName.indexOf('warningTimeThreshold') != -1 && attrValue != '-1') {// 预警时间阈值*10
                        attrValue = parseInt(attrValue * 10);
                    }
                    alarmObj[alarmId][alarmName] = attrValue;
                }
            }
            for (key in alarmObj) {
                adasAlarmParamSettings.push(alarmObj[key]);
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
            // console.log('paramList', paramList);
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
                        'protocolType': paramType,
                        'automaticGetOne': $('.automaticGetOne_' + alarmId).val(),
                        'automaticGetTwo': $('.automaticGetTwo_' + alarmId).val(),
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
        // 身份识别参数设置表单验证
        identificationValidates: function () {
            var validateArr = [253801, 253802];// 需校验的报警id
            return $("#identificationForm").validate({
                ignore: '',
                rules: Object.assign({
                    dynamicContrastInterval: {
                        digits: true,
                        range: [30, 3600]
                    },
                    speedLimit: {
                        digits: true,
                        range: [0, 30]
                    },
                }, defineHeiSetting.getValidateInfo(validateArr).validateObj),
                messages: Object.assign({
                    dynamicContrastInterval: {
                        digits: dynamicAlignmentIntervalError,
                        range: dynamicAlignmentIntervalError
                    },
                    speedLimit: {
                        digits: zeroThirtyError,
                        range: zeroThirtyError
                    },
                }, defineHeiSetting.getValidateInfo(validateArr).validateMsg)
            }).form();
        },
        // 车辆运行参数设置表单验证
        vehicleRunValidates: function () {
            var validateArr = [253901, 253902, 253903, 253904, 253905];// 需校验的报警id
            return $("#vehicleRunForm").validate({
                ignore: '',
                rules: Object.assign({
                    speedLimit: {
                        digits: true,
                        range: [0, 60]
                    },
                    speedThreshold_253901: {
                        digits: true,
                        range: [0, 220]
                    },
                    speedThreshold_253902: {
                        digits: true,
                        range: [0, 30]
                    },
                    timeThreshold_253901: {
                        decimalOne: true,
                        range: [1, 5]
                    },
                    warningTimeThreshold_253901: {
                        decimalOne: true,
                        range: [1, 5]
                    }
                }, defineHeiSetting.getValidateInfo(validateArr).validateObj),
                messages: Object.assign({
                    speedLimit: {
                        digits: videoRecordingError,
                        range: videoRecordingError
                    },
                    speedThreshold_253901: {
                        digits: '取值范围0-220之间的整数',
                        range: '取值范围0-220之间的整数',
                    },
                    speedThreshold_253902: {
                        digits: '取值范围0-30之间的整数',
                        range: '取值范围0-30之间的整数',
                    },
                    timeThreshold_253901: {
                        decimalOne: '请输入1位小数，范围：1.0~5.0',
                        range: '请输入1位小数，范围：1.0~5.0'
                    },
                    warningTimeThreshold_253901: {
                        decimalOne: '请输入1位小数，范围：1.0~5.0',
                        range: '请输入1位小数，范围：1.0~5.0'
                    },
                }, defineHeiSetting.getValidateInfo(validateArr).validateMsg)
            }).form()
        },
        // 驾驶员行为参数设置表单验证
        driverBehaviorValidates: function () {
            var validateArr = [254004, 254002, 254001, 254003, 254005];// 需校验的报警id
            return $("#driverBehaviorForm").validate({
                ignore: '',
                rules: Object.assign({
                    speedLimit: {
                        digits: true,
                        range: [0, 30]
                    },
                }, defineHeiSetting.getValidateInfo(validateArr).validateObj),
                messages: Object.assign({
                    speedLimit: {
                        digits: zeroThirtyError,
                        range: zeroThirtyError
                    },
                }, defineHeiSetting.getValidateInfo(validateArr).validateMsg)
            }).form();
        },
        // 设备失效参数设置表单验证
        equipmentFailureValidates: function () {
            var validateArr = [254101, 254102, 254103, 254104, 254105, 254106, 254107, 254108, 254109];// 需校验的报警id
            return $("#equipmentFailureForm").validate({
                ignore: '',
                rules: Object.assign({
                    speedLimit: {
                        digits: true,
                        range: [0, 30]
                    },
                    timeSlotThreshold_254101: {
                        digits: true,
                        range: [0, 3600]
                    },
                    timeSlotThreshold_254102: {
                        digits: true,
                        range: [0, 3600]
                    }
                }, defineHeiSetting.getValidateInfo(validateArr).validateObj),
                messages: Object.assign({
                    speedLimit: {
                        digits: zeroThirtyError,
                        range: zeroThirtyError
                    },
                    timeSlotThreshold_254101: {
                        digits: '取值范围0-3600之间的整数',
                        range: '取值范围0-3600之间的整数',
                    },
                    timeSlotThreshold_254102: {
                        digits: '取值范围0-3600之间的整数',
                        range: '取值范围0-3600之间的整数',
                    }
                }, defineHeiSetting.getValidateInfo(validateArr).validateMsg)
            }).form()
        },
        // 平台参数设置表单验证
        platformParamValidates: function () {
            var validateArr = [
                253801, 253802,
                253901, 253902, 253903, 253904, 253905, 253906,
                254001, 254002, 254003, 254004, 254005,
                254101, 254102, 254103, 254104, 254105, 254106,
                254107, 254108, 254109
            ];// 需校验的报警id
            return $("#platformParamForm").validate({
                ignore: '',
                rules: Object.assign({},
                    defineHeiSetting.getValidateInfo(validateArr, 'platform').validateObj),
                messages: Object.assign({},
                    defineHeiSetting.getValidateInfo(validateArr, 'platform').validateMsg)
            }).form()
        },
        /**
         * 表单公共参数验证方法
         * type 'platform':平台参数设置
         * */
        getValidateInfo: function (validateArr, type) {
            var validateObj = {};
            var validateMsg = {};
            for (var i = 0; i < validateArr.length; i++) {
                var item = validateArr[i];
                if (!type) {
                    // 视频录制时长(s)
                    validateObj['videoRecordingTime_' + item] = {
                        digits: true,
                        range: [5, 60]
                    };
                    var arr = [253802, 253801, 253901, 253902, 253903, 253904, 253905];
                    if (arr.indexOf(item) === -1) {
                        // 报警拍照间隔
                        validateObj['photographTime_' + item] = {
                            decimalOne: true,
                            range: [0.1, 0.5]
                        };
                    } else {
                        // 报警拍照间隔
                        validateObj['photographTime_' + item] = {
                            decimalOne: true,
                            range: [0.1, 1]
                        };
                    }
                    // 报警拍照张数
                    validateObj['photographNumber_' + item] = {
                        digits: true,
                        range: [3, 10]
                    };

                    var timeArr = [254002, 254004];
                    if (timeArr.indexOf(item) !== -1) {
                        // 触发报警时间间隔
                        validateObj['timeSlotThreshold_' + item] = {
                            digits: true,
                            range: [0, 180]
                        };
                        validateMsg['timeSlotThreshold_' + item] = {
                            digits: '取值范围0-180之间的整数',
                            range: '取值范围0-180之间的整数',
                        };
                    }


                    validateMsg['videoRecordingTime_' + item] = {
                        digits: fiveSixtyError,
                        range: fiveSixtyError
                    };
                    if (arr.indexOf(item) === -1) {
                        validateMsg['photographTime_' + item] = {
                            decimalOne: '请输入1位小数，范围：0.1~0.5',
                            range: '请输入1位小数，范围：0.1~0.5'
                        };
                    } else {
                        validateMsg['photographTime_' + item] = {
                            decimalOne: '请输入1位小数，范围：0.1~1.0',
                            range: '请输入1位小数，范围：0.1~1.0'
                        };
                    }
                    validateMsg['photographNumber_' + item] = {
                        digits: frequencyError,
                        range: frequencyError
                    };
                } else {// 平台参数设置
                    // 处理间隔
                    validateObj['processingIntervalOne_' + item] = {
                        digits: true,
                        range: [10, 2880]
                    };
                    validateObj['processingIntervalTwo_' + item] = {
                        digits: true,
                        range: [10, 2880]
                    };
                    // 时间阀值
                    validateObj['timeThreshold_' + item] = {
                        digits: true,
                        range: [1, 1440]
                    };
                    // 报警数量
                    validateObj['timeAlarmNumThreshold_' + item] = {
                        digits: true,
                        range: [1, 2880]
                    };
                    validateObj['distanceAlarmNumThreshold_' + item] = {
                        digits: true,
                        range: [1, 2880]
                    };
                    // 距离阀值
                    validateObj['distanceThreshold_' + item] = {
                        digits: true,
                        range: [1, 960]
                    };

                    validateMsg['processingIntervalOne_' + item] = {
                        digits: '取值范围10-2880之间的整数',
                        range: '取值范围10-2880之间的整数'
                    };
                    validateMsg['processingIntervalTwo_' + item] = {
                        digits: '取值范围10-2880之间的整数',
                        range: '取值范围10-2880之间的整数'
                    };
                    validateMsg['timeThreshold_' + item] = {
                        digits: '取值范围1-1440之间的整数',
                        range: '取值范围1-1440之间的整数'
                    };
                    validateMsg['timeAlarmNumThreshold_' + item] = {
                        digits: '取值范围1-2880之间的整数',
                        range: '取值范围1-2880之间的整数'
                    };
                    validateMsg['distanceAlarmNumThreshold_' + item] = {
                        digits: '取值范围1-2880之间的整数',
                        range: '取值范围1-2880之间的整数'
                    };
                    validateMsg['distanceThreshold_' + item] = {
                        digits: '取值范围1-960之间的整数',
                        range: '取值范围1-960之间的整数'
                    };
                }
            }
            return {validateObj, validateMsg};
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
        // 监听报警速度阈值变化
        speedLimitChange: function () {
            var thisVal = $(this).val();
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
                $('.selectvalue').val('0');
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
                $(this).parent().parent().parent().find('.selectvalue').val('0');
                $(this).parent().parent().parent().find('.col-md-10').slideUp();
            }
        },
        // 控制自动获取附件处理报警check
        inputClickFn: function () {
            var isChecked = $(this).is(":checked");
            if (isChecked) {
                $(this).val(1)
            } else {
                $(this).val(0)
            }
        }
    };
    $(function () {
        defineHeiSetting.init();
        $("input").inputClear();
        /**
         * 页面交互
         * */
        // 查看/隐藏更多参数
        $(".hiddenparameter").bind("click", defineHeiSetting.hiddenparameterFn);
        // 监听报警速度阈值变化
        $('.speedLimit').on('input propertychange', defineHeiSetting.speedLimitChange);
        // 监听页签切换,控制本页签下发按钮是否禁用
        $('.nav-tabs li').on('click', defineHeiSetting.curTabSendDisabled);
        // 滑块选择切换
        defineHeiSetting.selectSwitch();
        defineHeiSetting.topSwitch();
        $(".typeName").bind('click', defineHeiSetting.labelClickFn);
        $('.hiddenparameter').on('click', defineHeiSetting.changeShowAndHide);
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
        $(".typeName").css("cursor", "pointer");
        $(".typeName").hover(
            function () {
                $(this).css('color', '#6dcff6');
            },
            function () {
                $(this).css('color', '#5D5F63')
            }
        );

        // 自动获取附件
        $(".controlCheck").bind('click', defineHeiSetting.inputClickFn);

        /**
         * 下发及保存
         * */
        $('#allParamSend').on('click', function () {
            defineHeiSetting.paramSend('all');
        });
        $('#curTabSend').on('click', function () {
            defineHeiSetting.paramSend('curTab');
        });
        $('#doSubmits').on('click', function () {
            defineHeiSetting.paramSend('save');
        });
    })
})(window, $);