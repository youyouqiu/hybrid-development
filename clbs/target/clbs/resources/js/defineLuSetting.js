//# sourceURL=defineLuSetting.js
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
    var protocolType = 26;// 鲁标协议值

    var defineLuSetting = {
        init: function () {
            $(".modal-body").addClass("modal-body-overflow");
            $(".modal-body").css({"height": "auto", "max-height": ($(window).height() - 194) + "px"});

            defineLuSetting.referenceObjRender(); //参考对象渲染
            defineLuSetting.referenceTireModel(); //轮胎型号渲染

            // 修改界面,已设置的参数渲染
            if (settingParam.length > 0) {
                defineLuSetting.paramValueRender(settingParam);
            }
            if (settingPlat.length > 0) {
                defineLuSetting.platformValueRender(settingPlat);
            }

            // 表单验证方法
            defineLuSetting.drivingAssistantValidates(); //高级驾驶辅助
            defineLuSetting.driverStatusValidates(); // 驾驶员状态
            defineLuSetting.tirePressureValidates(); //胎压
            defineLuSetting.blindSpotValidates(); //盲点
            defineLuSetting.driverCompareValidates(); //驾驶员比对
            defineLuSetting.platformParamValidates(); //品台参数
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
                            defineLuSetting.platformValueRender(result.platformParam);
                            defineLuSetting.paramValueRender(result.alarmParam);
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
                var commonParamSetting = item.commonParamSetting;// 通用参数
                var paramType = commonParamSetting.paramType;
                defineLuSetting.setParamValue(commonParamSetting, paramType);
                for (var j = 0, adasLen = adasAlarmParamSettings.length; j < adasLen; j++) {
                    var data = adasAlarmParamSettings[j];
                    var alarmId = data.riskFunctionId;
                    defineLuSetting.setParamValue(data, alarmId);
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
                if (key.indexOf('Status') != -1 || key.indexOf('larmEnable') != -1 || key.indexOf('roadSign') != -1) {
                    var curTarget = $('.' + key + '_' + id + '[value=' + curVal + ']');
                    curTarget.prop('checked', true);
                    if (key.indexOf('Status') != -1) {// 触发状态切换
                        defineLuSetting.touchStatusChange(curTarget);
                    }
                    if (key.indexOf('AlarmEnable') != -1) {// 一级二级报警切换
                        defineLuSetting.oneTwoLevelAlarmChange(curTarget);
                    }
                    if (key.indexOf('roadSign') != -1) {// 道路标识识别切换
                        defineLuSetting.roadSignRecognitionChange(curTarget);
                    }
                    if (key.indexOf('alarmEnable') != -1) {// 激烈驾驶下的报警开关切换
                        defineLuSetting.alarmEnableChange(curTarget);
                    }
                }
                if (key.indexOf('PictureEnable') != -1 || key.indexOf('ChangeEnable') != -1 || key.indexOf('CompareEnable') != -1) {// 驾驶员状态监测 事件使能
                    if (id == 266511 || id == 266510 || id == 262331 || id == 266406) {
                        var curTarget = $('.' + key + '_' + id + '[value=' + curVal + ']');
                        defineLuSetting.eventEnableChange(curTarget);
                    }
                }
                if (key == 'tyreNumberName') {
                    $('#tyreNumber').val(curVal);
                }
                if (key.indexOf('Percent') != -1) {
                    curVal = curVal ? curVal + "%" : ""
                }
                if(key == 'offlineFaceCompareEnable'){
                    defineLuSetting.dsmFaceSwitch(curTarget)
                }
                if(key == 'roadSignEnable' || key == 'roadSignRecognition' || key == 'initiativePictureEnable') {
                    defineLuSetting.threeSwitchChange(curTarget)
                }
                var targetId = $('#' + key + '_' + id);
                targetId.val(curVal);
            }
        },
        platformValueRender: function (dataList) {
            for (var i = 0; i < dataList.length; i++) {
                var data = dataList[i];
                var alarmId = data.riskFunctionId;
                defineLuSetting.newSetParamValue(data, alarmId);
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
                if (!defineLuSetting[validates]()) {
                    if ($('#platformSet').hasClass('active')) {
                        layer.msg('设置参数有误');
                    }
                    return;
                }
                var paramType = curForm.find('.paramType').val();
                if (paramType == 0) {
                    var platformParamForm = defineLuSetting.setFormPlatformParam(curForm);
                    for (key in platformParamForm) {
                        parameter.platformParam.push(platformParamForm[key]);
                    }
                    parameter.sendFlag = false;
                } else {
                    var result = defineLuSetting.setFormParam(curForm);
                    parameter.alarmParam.push(result);
                }
                curTabSend.prop('disabled', true);
                var curTabId = $('.nav-tabs li.active').attr('id');
                if (!disabledTabObj[curTabId]) {
                    disabledTabObj[curTabId] = setTimeout(function () {
                        disabledTabObj[curTabId] = null;
                        defineLuSetting.curTabSendDisabled('timeOut');
                    }, 5000)
                }
            } else {
                if (!defineLuSetting.drivingAssistantValidates()
                    || !defineLuSetting.driverStatusValidates()
                    || !defineLuSetting.tirePressureValidates()
                    || !defineLuSetting.blindSpotValidates()
                    || !defineLuSetting.driverCompareValidates()
                    || !defineLuSetting.platformParamValidates()) {
                    layer.msg('设置参数有误');
                    return;
                }
                var drivingAssistant = defineLuSetting.setFormParam($("#drivingAssistantForm"));
                var driverStatus = defineLuSetting.setFormParam($("#driverStatusForm"));
                var tirePressure = defineLuSetting.setFormParam($("#tirePressureForm"));
                var blindSpot = defineLuSetting.setFormParam($("#blindSpotForm"));
                var driverCompare = defineLuSetting.setFormParam($("#driverCompareForm"));
                parameter.alarmParam.push(
                    drivingAssistant,
                    driverStatus,
                    tirePressure,
                    blindSpot,
                    driverCompare,
                );

                var platformParamForm = defineLuSetting.setFormPlatformParam($("#platformParamForm"));
                for (key in platformParamForm) {
                    parameter.platformParam.push(platformParamForm[key]);
                }

            }
            parameter.alarmParam = JSON.stringify(parameter.alarmParam);
            parameter.platformParam = JSON.stringify(parameter.platformParam);
            var url = settingParam.length == 0 ? addSendUrl : editSendUrl;
            console.log(JSON.parse(parameter.alarmParam))
            // return
            json_ajax("POST", url, "json", true, parameter, function (data) {
                defineLuSetting.paramSendCallback(data, flag);
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
                    if ((attrName.indexOf('timeDistanceThreshold') != -1
                        || attrName.indexOf('timeThreshold') != -1
                        || attrName.indexOf('photographTime') != -1
                    ) && attrValue != '-1') {// 时距阈值需要*10
                        attrValue = attrValue * 10;
                    }
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
                        attrValue = parseInt(attrValue) * 10;
                    }
                    if (alarmName.indexOf('Percent') != -1 && attrValue != '-1') {// 预警时间阈值*10
                        attrValue = parseInt(attrValue);
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
         * 有些表单验证规则和公共的有出入，这里提供修改函数
         * @param origin 原始对象
         * @param fixMsg {dataIndex:字段名  [riskFunctionId: id]  data:数据}
         */
        fixValidateInfo: function (origin, fixMsg) {
            var riskFunctionId = ''
            var key = ''
            var res = null
            if(typeof fixMsg.dataIndex == 'string'){
                riskFunctionId = fixMsg.riskFunctionId || ''
                key = fixMsg.dataIndex
                res = defineLuSetting.generateRuleAndMsg(fixMsg.data)
                if(riskFunctionId){
                    origin.messages[key + '_' + riskFunctionId] = res.message
                    origin.rules[key + '_' + riskFunctionId] = res.rule
                }else {
                    origin.messages[key] = res.message
                    origin.rules[key] = res.rule
                }
            }else if(Object.prototype.toString.call(fixMsg.dataIndex) == '[object Array]'){
                fixMsg.dataIndex.forEach(function (item,index) {
                    riskFunctionId = fixMsg.riskFunctionId[index]
                    res = defineLuSetting.generateRuleAndMsg(fixMsg.data)
                    if(riskFunctionId){
                        origin.messages[item + '_' + riskFunctionId] = res.message
                        origin.rules[item + '_' + riskFunctionId] = res.rule
                    }else {
                        origin.messages[item] = res.message
                        origin.rules[item] = res.rule
                    }
                })
            }
        },
        /**
         * 表单验证方法
         * */
        // 高级驾驶辅助参数设置表单验证
        drivingAssistantValidates: function () {
            // 266407：障碍物检测;266405:频繁变道;车道偏离:266402;前向碰撞:266401;行人碰撞:266404;车距过近:266403;道路标志:266406
            var validateArr = [266407, 266405, 266402, 266401, 266404, 266403, 266406];// 需校验的报警id
            var validateInfo = defineLuSetting.getValidateInfo(validateArr)
            var commonValidateInfo = defineLuSetting.getCommonValidateInfo()
            return $("#drivingAssistantForm").validate({
                ignore: '',
                rules: Object.assign({}, commonValidateInfo.rules, validateInfo.rules),
                messages: Object.assign({}, commonValidateInfo.messages, validateInfo.messages)
            }).form();
        },
        // 驾驶员状态监测参数设置表单验证
        driverStatusValidates: function () {
            // 266407：障碍物检测 ;266405:频繁变道; 车道偏离:266402; 前向碰撞:266401; 行人碰撞:266404; 车距过近:266403; 道路标志:266406
            // 266501:疲劳驾驶; 266502:接打电话; 抽烟;266503:分神驾驶; 266504:驾驶员异常;
            var validateArr = [266501, 266502, 266503, 266504, 266505];// 需校验的报警id
            var validateInfo = defineLuSetting.getValidateInfo(validateArr) //timingPhotoInterval_65
            var commonValidateInfo = defineLuSetting.getCommonValidateInfo()
            defineLuSetting.fixValidateInfo(validateInfo, {
                dataIndex: ['timeSlotThreshold','timeSlotThreshold'],
                riskFunctionId: ['266502','266503'],
                data: {
                    name: 'timeSlotThreshold',
                    ruleTitle: ['digits', 'range'],
                    rule: [true, [0, 3600]],
                    message: '取值范围0~3600之间的整数'
                }
            })
            defineLuSetting.fixValidateInfo(commonValidateInfo, {
                dataIndex: 'timingPhotoInterval',
                data: {
                    name: 'timingPhotoInterval',
                    ruleTitle: ['digits', 'range'],
                    rule: [true, [60, 60000]],
                    message: '取值范围60~60000之间的整数'
                }
            })
            return $("#driverStatusForm").validate({
                ignore: '',
                rules: Object.assign({}, commonValidateInfo.rules, validateInfo.rules),
                messages: Object.assign({}, commonValidateInfo.messages, validateInfo.messages)
            }).form();
        },
        // 胎压监测参数设置表单验证
        tirePressureValidates: function () {
            // 266407：障碍物检测 ;266405:频繁变道; 车道偏离:266402; 前向碰撞:266401; 行人碰撞:266404; 车距过近:266403; 道路标志:266406
            // 266501:疲劳驾驶; 266502:接打电话; 抽烟;266503:分神驾驶; 266504:驾驶员异常;
            // 266601:胎压监测
            var validateArr = [266601];// 需校验的报警id
            var validateInfo = defineLuSetting.getValidateInfo(validateArr)
            var commonValidateInfo = defineLuSetting.getCommonValidateInfo()
            return $("#tirePressureForm").validate({
                ignore: '',
                rules: Object.assign({}, commonValidateInfo.rules, validateInfo.rules),
                messages: Object.assign({}, commonValidateInfo.messages, validateInfo.messages)
            }).form();
        },
        // 盲点监测参数设置表单验证
        blindSpotValidates: function () {
            // 266407：障碍物检测 ;266405:频繁变道; 车道偏离:266402; 前向碰撞:266401; 行人碰撞:266404; 车距过近:266403; 道路标志:266406
            // 266501:疲劳驾驶; 266502:接打电话; 抽烟;266503:分神驾驶; 266504:驾驶员异常;
            // 266601:胎压监测
            // 266701:盲区监测
            return $("#blindSpotForm").validate({
                ignore: '',
                rules: {
                    rear_266701: {
                        digits: true,
                        range: [1, 10]
                    },
                    sideRear_266701: {
                        digits: true,
                        range: [1, 10]
                    }
                },
                messages: {
                    rear_266701: {
                        digits: '取值范围1~10之间的整数',
                        range: '取值范围1~10之间的整数',
                    },
                    sideRear_266701: {
                        digits: '取值范围1~10之间的整数',
                        range: '取值范围1~10之间的整数',
                    }
                },
            }).form();
        },
        // 驾驶员比对参数设置表单验证
        driverCompareValidates: function () {
            // 266407：障碍物检测 ;266405:频繁变道; 车道偏离:266402; 前向碰撞:266401; 行人碰撞:266404; 车距过近:266403; 道路标志:266406
            // 266501:疲劳驾驶; 266502:接打电话; 抽烟;266503:分神驾驶; 266504:驾驶员异常;
            // 266601:胎压监测
            // 266701:盲区监测
            // 262331:驾驶员比对
            return $("#driverCompareForm").validate({
                ignore: '',
                rules: {
                    dsmCompareSuccessPercent_262331: {
                        percent: true,
                    },
                    phoneCompareSuccessPercent_262331: {
                        percent: true,
                    }
                },
                messages: {
                    dsmCompareSuccessPercent_262331: {
                        percent: '取值范围1%~100%之间的百分数',
                    },
                    phoneCompareSuccessPercent_262331: {
                        percent: '取值范围1%~100%之间的百分数',
                    }
                },
            }).form();
        },
        // 平台参数设置表单验证
        platformParamValidates: function () {
            var validateArr = [
                266407, 266405, 266402, 266401, 266404, 266403, 266406, 266501, 266502, 266503,
                266504, 266505, 266506, 266508, 266601, 266602, 266603, 266604, 266605, 266606,
                266607, 266701, 266702, 266703,] // 需校验的报警id
            var validateInfo = defineLuSetting.getValidateInfo(validateArr, 'platform')
            return $("#platformParamForm").validate({
                ignore: '',
                rules: Object.assign({}, validateInfo.rules),
                messages: Object.assign({}, validateInfo.messages)
            }).form()
        },
        /**
         * 表单公共参数验证方法
         * type 'platform':平台参数设置
         * */
        generateRuleAndMsg: function (ruleObj) {
            var message = {}
            var rule = {}
            ruleObj.ruleTitle.forEach(function (item, index) {
                message[item] = ruleObj.message
                rule[item] = ruleObj.rule[index]
            })
            return {
                rule: rule,
                message: message
            }
        },
        getValidateInfo: function (validateArr, type) {
            var validateObj = {};
            var validateMsg = {};
            for (var i = 0; i < validateArr.length; i++) {
                var item = validateArr[i];
                var res
                if (!type) {
                    // decimalOne  range  digits require maxlength
                    res = [
                        {
                            name: 'timeDistanceThreshold', //时间阈值(s)
                            ruleTitle: ['decimalOne', 'range'],
                            rule: [true, [1.0, 5.0]],
                            message: '取值范围1.0~5.0之间带一位小数的数字'
                        }, {
                            name: 'alarmLevelSpeedThreshold', //分级速度阈值(km/h)
                            ruleTitle: ['digits', 'range'],
                            rule: [true, [0, 220]],
                            message: '取值范围0~220之间整数'
                        }, {
                            name: 'videoRecordingTime', //视频录制时长(s)
                            ruleTitle: ['digits', 'range'],
                            rule: [true, [0, 60]],
                            message: '取值范围0~60之间整数'
                        }, {
                            name: 'photographNumber', //报警拍照张数
                            ruleTitle: ['digits', 'range'],
                            rule: [true, [0, 10]],
                            message: '取值范围0~10之间整数'
                        }, {
                            name: 'photographTime', //报警拍照间隔(s)
                            ruleTitle: ['decimalOne', 'range'],
                            rule: [true, [0.1, 1.0]],
                            message: '取值范围0.1~1.0之间带一位小数数字'
                        },
                        {
                            name: 'timeSlotThreshold', //判断时间段(s)
                            ruleTitle: ['digits', 'range'],
                            rule: [true, [30, 120]],
                            message: '取值范围30~120之间整数'
                        }, {
                            name: 'frequencyThreshold', //判断次数
                            ruleTitle: ['digits', 'range'],
                            rule: [true, [3, 10]],
                            message: '取值范围3~10之间整数'
                        },

                        {
                            name: 'tyreNumberName', //轮胎型号（胎压）
                            ruleTitle: ['required', 'maxlength'],
                            rule: [true, 12],
                            message: '请输入或选择轮胎规格型号，长度12'
                        }, {
                            name: 'unit', //胎压单位（胎压）
                            ruleTitle: ['required'],
                            rule: [true],
                            message: '请选择胎压单位'
                        }, {
                            name: 'pressure', //正常胎压值（胎压）
                            ruleTitle: ['digits', 'range'],
                            rule: [true, [1, 500]],
                            message: '取值范围1~500的整数'
                        }, {
                            name: 'pressureThreshold', //胎压不平衡门限（胎压）
                            ruleTitle: ['digits', 'range'],
                            rule: [true, [0, 100]],
                            message: '取值范围0~100的整数'
                        }, {
                            name: 'slowLeakThreshold', //慢漏气门限（胎压）
                            ruleTitle: ['digits', 'range'],
                            rule: [true, [0, 100]],
                            message: '取值范围0~100的整数'
                        }, {
                            name: 'lowPressure', //低压阈值（胎压）
                            ruleTitle: ['digits', 'range'],
                            rule: [true, [0, 500]],
                            message: '取值范围0~500的整数'
                        }, {
                            name: 'highPressure', //高压阈值（胎压）
                            ruleTitle: ['digits', 'range'],
                            rule: [true, [0, 500]],
                            message: '取值范围0~500的整数'
                        }, {
                            name: 'highTemperature',  //高温阈值（胎压）
                            ruleTitle: ['digits', 'range'],
                            rule: [true, [0, 150]],
                            message: '取值范围0~150的整数'
                        }, {
                            name: 'electricityThreshold', //电压阈值（%）
                            ruleTitle: ['digits', 'range'],
                            rule: [true, [0, 100]],
                            message: '取值范围0~100的整数'
                        }, {
                            name: 'uploadTime', //定时上报时间间隔（s）
                            ruleTitle: ['digits', 'range'],
                            rule: [true, [0, 3600]],
                            message: '取值范围0~3600的整数'
                        },
                    ]
                } else {// 平台参数设置
                    // 处理间隔
                    res = [
                        {
                            name: 'processingIntervalOne', //一级报警处理间隔(min)
                            ruleTitle: ['digits', 'range'],
                            rule: [true, [10, 2880]],
                            message: '取值范围10-2880之间的整数'
                        }, {
                            name: 'processingIntervalTwo', //二级报警处理间隔(min)
                            ruleTitle: ['digits', 'range'],
                            rule: [true, [10, 2880]],
                            message: '取值范围10-2880之间的整数'
                        }, {
                            name: 'timeThreshold', //时间阈值(s)
                            ruleTitle: ['digits', 'range'],
                            rule: [true, [1, 1440]],
                            message: '取值范围1-1440之间的整数'
                        }, {
                            name: 'distanceThreshold', //距离阈值(s)
                            ruleTitle: ['digits', 'range'],
                            rule: [true, [1, 960]],
                            message: '取值范围1-960之间的整数'
                        }, {
                            name: 'timeAlarmNumThreshold', //时间报警数量
                            ruleTitle: ['digits', 'range'],
                            rule: [true, [1, 2880]],
                            message: '取值范围1-2880之间的整数'
                        }, {
                            name: 'distanceAlarmNumThreshold', //距离报警阈值
                            ruleTitle: ['digits', 'range'],
                            rule: [true, [1, 2880]],
                            message: '取值范围1-2880之间的整数'
                        },
                    ]
                }
                res.forEach(function (item2) {
                    var ruleObj = defineLuSetting.generateRuleAndMsg(item2)
                    validateObj[item2.name + '_' + item] = ruleObj.rule
                    validateMsg[item2.name + '_' + item] = ruleObj.message
                })
            }
            return {rules: validateObj, messages: validateMsg};
        },
        getCommonValidateInfo: function () {
            var rules = {
                //单次拍照张数
                photographNumber: {
                    digits: true,
                    range: [1, 10]
                },
                //定时拍照间隔（s）
                timingPhotoInterval: {
                    digits: true,
                    range: [0, 3600]
                },
                //定距拍照间隔（m）
                distancePhotoInterval: {
                    decimalTwo: true,
                    range: [0, 60.00]
                },
                speedLimit: {
                    digits: true,
                    range: [0, 60]
                },
            }
            var messages = {
                photographNumber: {
                    digits: "取值范围1~10之间的整数",
                    range: "取值范围1~10之间的整数",
                },
                timingPhotoInterval: {
                    digits: "取值范围0~3600之间的整数",
                    range: "取值范围0~3600之间的整数",
                },
                distancePhotoInterval: {
                    decimalTwo: '取值范围0.00~60.00之间的小数',
                    range: '取值范围0.00~60.00之间的小数',
                },
                speedLimit: {
                    digits: '取值范围0-60之间的整数',
                    range: '取值范围0-60之间的整数',
                },
            }
            return {rules: rules, messages: messages}
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
        // 激烈驾驶下的报警开关单选按钮切换切换
        alarmEnableChange: function (target) {
            var _this = target.target ? $(this) : target;
            var curVal = _this.val();
            var alarmEnableInfo = _this.closest('.form-group').next('.alarmEnableInfo');
            if (curVal == '0') {
                alarmEnableInfo.slideUp();
                alarmEnableInfo.find('input,select').prop('disabled', true);
            } else {
                alarmEnableInfo.slideDown();
                alarmEnableInfo.find('input,select').prop('disabled', false);
            }
        },
        // 监听报警速度阈值变化
        speedLimitChange: function () {
            return
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
                $('#tyreNumber_266601').val(tireModelId);
            }).on('onUnsetSelectValue', function () {
            });
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

            $('#tyreNumber_266601').val(res);
        },
        // 触发状态单选按钮切换切换
        touchStatusChange: function (target) {
            var _this = target.target ? $(this) : target;
            if(_this.data('value') == 'noRes') return
            var curVal = _this.val();
            // var touchStatusInfo = _this.closest('.form-group').siblings('.touchStatusInfo');
            var touchStatusInfo = _this.closest('.form-group').next();
            if (!touchStatusInfo.hasClass('touchStatusInfo')) return
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
            var sibVal = _this.closest('.col-md-6').siblings('.col-md-6').find('input[type="radio"]:checked').val();
            var levelAlarmInfo = _this.closest('.form-group').next('.levelAlarmInfo');
            if (_this.val() == '0' && sibVal == '0') {// 一级报警与二级报警都为关闭状态,隐藏相关参数
                levelAlarmInfo.slideUp();
                levelAlarmInfo.find('input,select').prop('disabled', true);
            } else {
                levelAlarmInfo.slideDown();
                levelAlarmInfo.find('input,select').prop('disabled', false);
            }
        },
        //人脸开关事件
        dsmFaceSwitch: function(target){
            var _this = target.target ? $(this) : target;
            var next = _this.closest('.form-group').next()
            if (_this.val() == '0'){
                next.slideUp()
                next.find('input,select').prop('disabled', true)
            }else {
                next.slideDown()
                next.find('input,select').prop('disabled', false);
            }
        },
        // 三个开关联动
        threeSwitchChange: function(target){
            var _this = target.target ? $(this) : target
            var isAllClose = true
            var allRadio = _this.closest('.col-md-10').find('input[type="radio"]:checked')
            allRadio.each(function (idx, item) {
                if($(item).val() == '1'){
                    isAllClose = false
                }
            })
            var next = _this.closest('.form-group').next()
            if (isAllClose){
                next.slideUp()
                next.find('input,select').prop('disabled', true)
            }else {
                next.slideDown()
                next.find('input,select').prop('disabled', false);
            }
        },
        // 事件使能切换
        eventEnableChange: function (target) {
            var _this = target.target ? $(this) : target;
            _this.closest('.col-md-4').find('input').prop('checked', false);
            _this.prop('checked', true)
        }
    };
    $(function () {
        jQuery.validator.addMethod("percent", function(value, element) {
            var percent = /^(([1-9][0-9]{0,1})%?|100%?)$/;
            // /^([1-9][0-9]?)?(100)?%?$/;
            return this.optional(element) || (percent.test(value));
        }, "取值范围1%~100%之间的百分数");
        defineLuSetting.init();
        $("input").inputClear();
        /**
         * 页面交互
         * */
        // 查看/隐藏更多参数
        $(".hiddenparameter").bind("click", defineLuSetting.hiddenparameterFn);
        // 监听报警速度阈值变化
        $('.speedLimit').on('input propertychange', defineLuSetting.speedLimitChange);
        // 触发状态单选按钮切换切换
        $('.touchStatus').on('change', defineLuSetting.touchStatusChange);
        // 监听页签切换,控制本页签下发按钮是否禁用
        $('.nav-tabs li').on('click', defineLuSetting.curTabSendDisabled);
        // 滑块选择切换
        defineLuSetting.selectSwitch();
        defineLuSetting.topSwitch();
        $(".typeName").bind('click', defineLuSetting.labelClickFn);
        $('.hiddenparameter').on('click', defineLuSetting.changeShowAndHide);
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
        $(".controlCheck").bind('click', defineLuSetting.inputClickFn);

        /**
         * 下发及保存
         * */
        $('#allParamSend').on('click', function () {
            defineLuSetting.paramSend('all');
        });
        $('#curTabSend').on('click', function () {
            defineLuSetting.paramSend('curTab');
        });
        $('#doSubmits').on('click', function () {
            defineLuSetting.paramSend('save');
        });
        $('.oneLevelAlarm,.twoLevelAlarm').on('change',defineLuSetting.oneTwoLevelAlarmChange)
        $('.dsmFaceSwitch').on('change',defineLuSetting.dsmFaceSwitch)
        $('.roadSignEnable_266406, .roadSignRecognition_266406, .initiativePictureEnable_266406').on('change',defineLuSetting.threeSwitchChange)
        $('#dsmCompareSuccessPercent_262331, #phoneCompareSuccessPercent_262331').on('blur',function (e) {
            var oldValue = $(this).val()
            if(oldValue && oldValue.indexOf('%') == -1 ){
                if(parseInt(oldValue) === parseInt(oldValue)){
                    $(this).val(parseInt(oldValue) + '%')
                }
            }
        })
    })
})(window, $);