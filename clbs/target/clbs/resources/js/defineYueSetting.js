//# sourceURL=defineYueSetting.js
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
    var protocolType = 28;// 粤标协议值

    // 公共参数数据集合
    var commonData = [
        // 0拍照分辨率 1视频分辨率 2提示音量 3主动拍照策略  4定时拍照间隔(s)  5定距拍照间隔(m)  6单次拍照张数  7单次拍照间隔(s) 8速度值(km/h) 9主动拍照策略_2  10驾驶员身份识别策略
        {
            index: 0,
            name: '拍照分辨率',
            tagName: 'select',
            id: 'cameraResolution',
            default: '0x01',
            value: [
                {name: '352×288', value: '0x01'},
                {name: '704×288', value: '0x02'},
                {name: '704×576', value: '0x03'},
                {name: '640×480', value: '0x04'},
                {name: '1280×720', value: '0x05'},
                {name: '1920×1080', value: '0x06'},
            ]
        },
        {
            index: 1,
            name: '视频分辨率',
            tagName: 'select',
            id: 'videoResolution',
            default: '0x01',
            value: [
                {name: 'CIF', value: '0x01'},
                {name: 'HD1', value: '0x02'},
                {name: 'D1', value: '0x03'},
                {name: 'WD1', value: '0x04'},
                {name: 'VGA', value: '0x05'},
                {name: '720P', value: '0x06'},
                {name: '1080P', value: '0x07'},
            ]
        },
        {
            index: 2,
            name: '提示音量',
            tagName: 'select',
            id: 'alarmVolume',
            default: '6',
            value: [0, 1, 2, 3, 4, 5, 6, 7, 8]
        },
        {
            index: 3,
            name: '主动拍照策略',
            tagName: 'radio',
            id: 'touchStatus',
            default: '0',
            value: [
                {name: '不开启', value: '0'},
                {name: '定时拍照', value: '1'},
                {name: '定距拍照', value: '2'},
            ]
        },
        {
            index: 4,
            name: '定时拍照间隔(s)',
            hidden: true,
            id: 'timingPhotoInterval',
            placeholder: '取值范围0~3600之间的整数',
            value: 60
        },
        {
            index: 5,
            name: '定距拍照间隔(km)',
            id: 'distancePhotoInterval',
            placeholder: '小数范围:0~60.00',
            value: '0.20'
        },
        {
            index: 6,
            name: '单次拍照张数',
            id: 'photographNumber',
            placeholder: '整数范围:1~10',
            value: 3
        },
        {
            index: 7,
            name: '单次拍照间隔(s)',
            id: 'photographTime',
            tagName: 'select',
            default: '0.2',
            value: [0.1, 0.2, 0.3, 0.4, 0.5]
        },
        {
            index: 8,
            name: '速度值(km/h)',
            id: 'speedLimit',
            placeholder: '请输入速度值，范围:0~60',
            value: 30
        },
        {
            index: 9,
            name: '主动拍照策略',
            tagName: 'radio',
            id: 'touchStatus',
            default: '0',
            value: [
                {name: '不开启', value: '0'},
                {name: '定时拍照', value: '1'},
                {name: '定距拍照', value: '2'},
                {name: '插卡触发', value: '3'},
            ]
        },
        {
            index: 10,
            name: '驾驶员身份识别策略',
            tagName: 'radio',
            id: 'touchStatus',
            riskFunctionId: '286507',
            default: '0',
            value: [
                {name: '不开启', value: '0'},
                {name: '定时触发', value: '1'},
                {name: '定距触发', value: '2'},
                {name: '插卡开始行驶触发', value: '3'},
            ],

        },
    ]
    // 其它参数设置集合
    var otherData = [
        //0:一级报警  1:二级报警  2时间阈值(s)  3分级速度阈值(km/h)  4视频录制时长(s)  5报警拍照张数  6报警拍照间隔(s)  7判断时间段(s)  8判断次数  9hiddenDiv  10超限报警  11标志识别  12主动拍照
        //13判断时间间隔(s)  14驾驶员更换事件   15主动拍照事件  16后方接近报警时间间隔(s)  17侧后方接近报警时间间隔(s)
        //18胎压单位  19正常胎压值  20胎压不平衡阈值（%） 21慢漏气门限（%）  22低压阈值  23高压阈值  24高温阈值(°C)  25电压阈值（%）  26定时上报时间间隔（s） 27车厢过道行人监测  28轮胎规格型号
        {
            index: 28,
            name: '轮胎规格型号',
            id: 'asdasd',
            contentRender: function () {
                return ' <div class="input-group">' +
                    '     <input type="text"' +
                    '            class="form-control"' +
                    '            placeholder="请选择轮胎规格型号"' +
                    '            autocomplete="off"' +
                    '            id="tyreNumber"' +
                    '            name="tyreNumberName"' +
                    '            value="900R20"' +
                    '     >' +
                    '     <input type="hidden" value="-1" name="tyreNumber_286601" id="tyreNumber_286601"/>' +
                    '     <div class="input-group-btn">' +
                    '         <button type="button" class="btn btn-white dropdown-toggle"' +
                    '                 data-toggle="dropdown">' +
                    '             <span class="caret"></span>' +
                    '         </button>' +
                    '         <ul class="dropdown-menu dropdown-menu-right" role="menu">' +
                    '         </ul>' +
                    '     </div>' +
                    ' </div>'
            },
        },
        {
            index: 0,
            name: '一级报警',
            tagName: 'radio',
            id: 'oneLevelAlarmEnable',
            default: '1',
            value: [{name: '开', value: '1'}, {name: '关', value: '0'}]
        },
        {
            index: 1,
            name: '二级报警',
            tagName: 'radio',
            id: 'twoLevelAlarmEnable',
            default: '1',
            value: [{name: '开', value: '1'}, {name: '关', value: '0'}]
        },
        {
            index: 2,
            name: '时间阈值(s)',
            id: 'timeDistanceThreshold',
            placeholder: '请输入时间阈值，范围：1.0~5.0',
            value: '3.0'
        },
        {
            index: 3,
            name: '分级速度阈值(km/h)',
            id: 'alarmLevelSpeedThreshold',
            placeholder: '请输入分级速度阈值，范围：0~220',
            value: '50'
        },
        {
            index: 4,
            name: '视频录制时长(s)',
            id: 'videoRecordingTime',
            placeholder: '请输入视频录制时长，范围：0~60',
            value: '5'
        },
        {
            index: 5,
            name: '报警拍照张数',
            id: 'photographNumber',
            placeholder: '请输入拍照张数张数，范围：0~10',
            value: '3'
        },
        {
            index: 6,
            name: '报警拍照间隔(s)',
            id: 'photographTime',
            placeholder: '请输入拍照间隔，范围：0.1~1.0',
            value: '0.2'
        },
        {
            index: 7,
            name: '判断时间段(s)',
            id: 'timeSlotThreshold',
            placeholder: '请输入判断时间段，范围：30~120',
            value: '60'
        },
        {
            index: 8,
            name: '判断次数',
            id: 'frequencyThreshold',
            placeholder: '请输入判断次数，范围：3~10',
            value: '5'
        },
        {
            index: 9,
            name: '占位',
        },
        {
            index: 10,
            name: '超限报警',
            id: 'roadSignEnable',
            tagName: 'radio',
            default: '1',
            value: [{name: '开', value: '1'}, {name: '关', value: '0'}]
        },
        {
            index: 11,
            name: '标志识别',
            id: 'roadSignRecognition',
            tagName: 'radio',
            default: '1',
            value: [{name: '开', value: '1'}, {name: '关', value: '0'}]
        },
        {
            index: 12,
            name: '主动拍照',
            id: 'initiativePictureEnable',
            tagName: 'radio',
            default: '1',
            value: [{name: '开', value: '1'}, {name: '关', value: '0'}]
        },
        {
            index: 13,
            name: '判断时间间隔(s)',
            id: 'timeSlotThreshold',
            placeholder: '请输入判断时间间隔，范围：0~3600',
            value: 120
        },
        {
            index: 14,
            name: '驾驶员更换事件',
            id: 'driverChangeEnable',
            riskFunctionId: '286507',
            tagName: 'radio',
            default: '1',
            value: [{name: '开', value: '1'}, {name: '关', value: '0'}]
        },
        {
            index: 15,
            name: '主动拍照事件',
            id: 'initiativePictureEnable',
            riskFunctionId: '286506',
            tagName: 'radio',
            default: '1',
            value: [{name: '开', value: '1'}, {name: '关', value: '0'}]
        },
        {
            index: 16,
            name: '后方接近报警时间阈值(s)',
            id: 'rear',
            placeholder: '请输入后方接近报警时间阈值，范围：1~10',
            value: '5'
        },
        {
            index: 17,
            name: '侧后方接近报警时间阈值(s)',
            id: 'sideRear',
            placeholder: '请输入侧后方接近报警时间阈值，范围：1~10',
            value: '5'
        },
        {
            index: 18,
            name: '胎压单位',
            tagName: 'select',
            id: 'unit',
            default: '3',
            value: [
                {name: 'kg/cm2', value: '0'},
                {name: 'bar', value: '1'},
                {name: 'Kpa', value: '2'},
                {name: 'PSI', value: '3'},
            ]
        },
        {
            index: 18,
            name: '正常胎压值',
            id: 'pressure',
            placeholder: '请输入胎压值，范围：1~500',
            titleRender: function () {
                return ' <i class="fa fa-question-circle fa-lg infoTitle" data-toggle="tooltip" data-placement="top" data-original-title="单位为胎压单位"></i> 正常胎压值：'
            },
            value: '140'
        },
        {
            index: 19,
            name: '胎压不平衡阈值(%)',
            id: 'pressureThreshold',
            placeholder: '请输入胎压不平衡阈值，范围：0~100',
            value: '20'
        },
        {
            index: 20,
            name: '慢漏气门限(%)',
            id: 'slowLeakThreshold',
            placeholder: '请输入慢漏气门限，范围：0~100',
            value: '5'
        },
        {
            index: 21,
            name: '低压阈值',
            id: 'lowPressure',
            titleRender: function () {
                return '<i class="fa fa-question-circle fa-lg infoTitle" data-toggle="tooltip" data-placement="top" data-original-title="单位为胎压单位"></i> 低压阈值：'
            },
            placeholder: '请输入低压阈值，范围：0~500',
            value: '110'
        },
        {
            index: 22,
            name: '高压阈值',
            id: 'highPressure',
            placeholder: '请输入高压阈值，范围：0~500',
            titleRender: function () {
                return '<i class="fa fa-question-circle fa-lg infoTitle" data-toggle="tooltip" data-placement="top" data-original-title="单位为胎压单位"></i> 高压阈值：'
            },
            value: '189'
        },
        {
            index: 23,
            name: '高温阈值(°C)',
            id: 'highTemperature',
            placeholder: '请输入高温阈值，范围：0~150',
            value: '80'
        },
        {
            index: 24,
            name: '电压阈值(%)',
            id: 'electricityThreshold',
            placeholder: '请输入电压阈值，范围：0~100',
            value: '10'
        },
        {
            index: 25,
            name: '定时上报时间间隔(s)',
            id: 'uploadTime',
            placeholder: '请输入定时上报时间间隔，范围：0~3600',
            value: '60'
        },
        {
            index: 26,
            name: '报警开关',
            id: 'pedestrianInspect',
            tagName: 'radio',
            default: '1',
            value: [{name: '开', value: '1'}, {name: '关', value: '0'}]
        },
        {
            index: 27,
            name: '报警拍照间隔(s)', //驾驶员状态监测中的报警拍照间隔和高级驾驶辅助中的取值范围不一样
            id: 'photographTime',
            placeholder: '请输入拍照间隔，范围：0.1~0.5',
            value: '0.2'
        },
    ]
    // 页面结构数据映射
    var domData = [
        {
            id: 'drivingAssistantForm',
            commonSetting: [
                {
                    label: '多媒体参数设置',
                    riskFunctionId: '64',
                    data: settingUtil.selectData([0, 1, 2], commonData),
                },
                {
                    label: '主动拍照',
                    riskFunctionId: '64',
                    data: settingUtil.selectData([3], commonData),
                    itemRender: function (data, id) {
                        var s = ' <div class="col-md-4 noPadding">\n' +
                            '    <label class="col-md-6 control-label">主动拍照策略：</label>\n' +
                            '    <label class="col-md-6 control-label radioLabel">\n' +
                            '        <input type="radio" class="touchStatus touchStatus_64 valid" name="touchStatus_64" value="0" checked=""> 不开启\n' +
                            '    </label>\n' +
                            '</div>\n' +
                            '<div class="col-md-4 noPadding">\n' +
                            '    <label class="col-md-6 control-label radioLabel">\n' +
                            '        <input type="radio" class="touchStatus touchStatus_64 valid" name="touchStatus_64" value="1"> 定时拍照\n' +
                            '    </label>\n' +
                            '    <label class="col-md-6 control-label radioLabel">\n' +
                            '        <input type="radio" class="touchStatus touchStatus_64 valid" name="touchStatus_64" value="2"> 定距拍照\n' +
                            '    </label>\n' +
                            '</div>'
                        return s
                    }
                },
                {
                    label: '',
                    notShow: true,
                    riskFunctionId: '64',
                    data: settingUtil.selectData([4, 5, 6, 7], commonData),
                    threshold: 4
                },
                {
                    label: '报警判断速度阈值',
                    riskFunctionId: '64',
                    data: settingUtil.selectData([8], commonData),
                }
            ],
            otherSetting: [
                {
                    label: '障碍物检测',
                    riskFunctionId: '286407',
                    data: settingUtil.selectData([0, 1, 2, 3, 4, 5, 6], otherData)
                },
                {
                    label: '频繁变道',
                    riskFunctionId: '286405',
                    data: settingUtil.selectData([0, 1, 7, 8, 3, 4, 5, 6], otherData)
                },
                {
                    label: '车道偏离',
                    riskFunctionId: '286402',
                    data: settingUtil.selectData([0, 1, 3, 4, 5, 6], otherData)
                },
                {
                    label: '前向碰撞',
                    riskFunctionId: '286401',
                    data: settingUtil.dataFix(settingUtil.selectData([0, 1, 2, 3, 4, 5, 6], otherData), 2, {
                        name: '时间阈值(s)',
                        id: 'timeDistanceThreshold',
                        placeholder: '请输入时间阈值，范围：1.0~5.0',
                        value: '2.7'
                    })
                },
                {
                    label: '行人碰撞',
                    riskFunctionId: '286404',
                    data: settingUtil.selectData([0, 1, 2, 3, 4, 5, 6], otherData)
                },
                {
                    label: '车距过近',
                    riskFunctionId: '286403',
                    data: settingUtil.dataFix(settingUtil.selectData([0, 1, 2, 3, 4, 5, 6], otherData), 2, {
                        name: '时间阈值(s)',
                        id: 'timeDistanceThreshold',
                        placeholder: '请输入时间阈值，范围：1.0~5.0',
                        value: '1.0'
                    })
                },
                {
                    label: '实线变道',
                    riskFunctionId: '286410',
                    data: settingUtil.selectData([0, 1, 3, 4, 5, 6], otherData)
                },
                {
                    label: '车厢过道行人监测',
                    riskFunctionId: '286411',
                    data: settingUtil.selectData([26, 9, 3, 4, 5, 6], otherData)
                },
                {
                    label: '道路标志',
                    riskFunctionId: '286406',
                    data: settingUtil.selectData([10, 11, 12,], otherData),
                    threshold: 3
                },
                {
                    label: '', //道路标志
                    riskFunctionId: '286406',
                    data: settingUtil.selectData([5, 6], otherData)
                },
            ],
            tips: [
                '该拍照分辨率、视频录制分辨率、提示音量、同时也适用于报警触发多媒体文件。',
                '报警判断速度阈值适用于道路偏离报警、前向碰撞报警，车距过近报警和频繁变道报警表示当车速高于此阈值才使能报警功能。'
            ]
        },
        {
            id: 'driverStatusForm',
            commonSetting: [
                {
                    label: '多媒体参数设置',
                    riskFunctionId: '65',
                    data: settingUtil.selectData([0, 1, 2], commonData),
                },
                {
                    label: '身份识别',
                    riskFunctionId: '65',
                    data: settingUtil.selectData([10], commonData),
                    itemRender: function (data, id) {
                        var s =
                            '<div class="col-md-4 noPadding">\n' +
                            '    <label class="col-md-6 control-label">驾驶员身份识别策略：</label>\n' +
                            '    <label class="col-md-6 control-label radioLabel">\n' +
                            '        <input type="radio" class="touchStatus touchStatus_286507 valid" name="touchStatus_286507" value="0" checked=""> 不开启\n' +
                            '    </label>\n' +
                            '</div>\n' +
                            '<div class="col-md-4 noPadding">\n' +
                            '    <label class="col-md-6 control-label radioLabel">\n' +
                            '        <input type="radio" class="touchStatus touchStatus_286507 valid" name="touchStatus_286507" value="1"> 定时触发\n' +
                            '    </label>\n' +
                            '    <label class="col-md-6 control-label radioLabel">\n' +
                            '        <input type="radio" class="touchStatus touchStatus_286507 valid" name="touchStatus_286507" value="2"> 定距触发\n' +
                            '    </label>\n' +
                            '</div>\n' +
                            '<div class="col-md-4 noPadding">\n' +
                            '    <label class="col-md-6 control-label radioLabel">\n' +
                            '        <input type="radio" class="touchStatus touchStatus_286507 valid" name="touchStatus_286507" value="3"> 插卡开始行驶触发\n' +
                            '    </label>\n' +
                            '</div>\n'
                        return s
                    }
                },
                {
                    label: '主动拍照',
                    riskFunctionId: '65',
                    data: settingUtil.selectData([9], commonData),
                    itemRender: function (data, id) {
                        var s =
                            '<div class="col-md-4 noPadding">\n' +
                            '    <label class="col-md-6 control-label">主动拍照策略：</label>\n' +
                            '    <label class="col-md-6 control-label radioLabel">\n' +
                            '        <input type="radio" class="touchStatus touchStatus_65 valid" name="touchStatus_65" value="0" checked="" data-value="noRes"> 不开启\n' +
                            '    </label>\n' +
                            '</div>\n' +
                            '<div class="col-md-4 noPadding">\n' +
                            '    <label class="col-md-6 control-label radioLabel">\n' +
                            '        <input type="radio" class="touchStatus touchStatus_65 valid" name="touchStatus_65" value="1" data-value="noRes"> 定时拍照\n' +
                            '    </label>\n' +
                            '    <label class="col-md-6 control-label radioLabel">\n' +
                            '        <input type="radio" class="touchStatus touchStatus_65 valid" name="touchStatus_65" value="2" data-value="noRes"> 定距拍照\n' +
                            '    </label>\n' +
                            '</div>\n' +
                            '<div class="col-md-4 noPadding">\n' +
                            '    <label class="col-md-6 control-label radioLabel">\n' +
                            '        <input type="radio" class="touchStatus touchStatus_65 valid" name="touchStatus_65" value="3" data-value="noRes"> 插卡触发\n' +
                            '    </label>\n' +
                            '    <label class="col-md-6 control-label radioLabel">\n' +
                            '        <input type="radio" class="touchStatus touchStatus_65 valid" name="touchStatus_65" value="4" data-value="noRes"> 点火触发\n' +
                            '    </label>\n' +
                            '</div>'
                        return s
                    }
                },
                {
                    label: '',
                    riskFunctionId: '65',
                    data: settingUtil.dataFix(settingUtil.selectData([4, 5, 6, 7], commonData), 0, {
                        name: '定时拍照间隔(s)',
                        id: 'timingPhotoInterval',
                        placeholder: '取值范围60~60000之间的整数',
                        value: 3600
                    }),
                },
                {
                    label: '报警判断速度阈值',
                    riskFunctionId: '65',
                    data: settingUtil.selectData([8], commonData),
                }
            ],
            otherSetting: [
                {
                    label: '疲劳驾驶',
                    riskFunctionId: '286501',
                    data: settingUtil.selectData([0, 1, 3, 4, 5, 27], otherData)
                },
                {
                    label: '接打手持电话',
                    riskFunctionId: '286502',
                    data: settingUtil.selectData([0, 1, 13, 3, 4, 5, 27], otherData),
                },
                {
                    label: '抽烟',
                    riskFunctionId: '286503',
                    data: settingUtil.dataFix(settingUtil.selectData([0, 1, 13, 3, 4, 5, 27], otherData), 2, {
                        name: '判断时间间隔(s)',
                        id: 'timeSlotThreshold',
                        placeholder: '请输入判断时间间隔，范围：0~3600',
                        value: 60
                    }),
                },
                {
                    label: '不目视前方',
                    riskFunctionId: '286504',
                    data: settingUtil.selectData([0, 1, 3, 4, 5, 27], otherData)
                },
                {
                    label: '驾驶员异常',
                    riskFunctionId: '286505',
                    data: settingUtil.selectData([0, 1, 3, 4, 5, 27], otherData)
                },
                {
                    label: '摄像头遮挡',
                    riskFunctionId: '286508',
                    data: settingUtil.selectData([0, 1, 3], otherData)
                },
                {
                    label: '不系安全带',
                    riskFunctionId: '286510',
                    data: settingUtil.selectData([0, 1, 3, 4, 5, 27], otherData)
                },
                {
                    label: '红外墨镜阻断失效',
                    riskFunctionId: '286511',
                    data: settingUtil.selectData([0, 1, 3, 4, 5, 27], otherData)
                },
                {
                    label: '双脱把',
                    riskFunctionId: '286512',
                    data: settingUtil.selectData([0, 1, 3, 4, 5, 27], otherData)
                },
                {
                    label: '玩手机',
                    riskFunctionId: '286513',
                    data: settingUtil.selectData([0, 1, 3, 4, 5, 27], otherData)
                },
                {
                    label: '事件使能',
                    riskFunctionId: '286506',
                    data: settingUtil.selectData([14, 15], otherData)
                },
            ],
            tips: [
                '该拍照分辨率、视频录制分辨率、提示音量、同时也适用于报警触发多媒体文件。',
                '报警判断速度阈值：当车速高于此阈值才使能报警功能。'
            ]
        },
        {
            id: 'tirePressureForm',
            commonSetting: [],
            otherSetting: [
                {
                    label: '胎压监测',
                    riskFunctionId: '286601',
                    data: settingUtil.selectData([28, 18, 19, 20, 21, 22, 23, 24, 25], otherData)
                },
            ]
        },
        {
            id: 'blindSpotForm',
            commonSetting: [],
            otherSetting: [
                {
                    label: '盲区监测',
                    riskFunctionId: '286701',
                    data: settingUtil.selectData([16, 17], otherData)
                },
            ]
        }
    ]
    // 公共参数验证规则集合
    var commonValidateMsg = [
        {
            name: 'photographNumber',
            ruleTitle: ['digits', 'range'],
            rule: [true, [1, 10]],
            message: '取值范围1~10之间的整数'
        },
        {
            name: 'timingPhotoInterval',
            ruleTitle: ['digits', 'range'],
            rule: [true, [0, 3600]],
            message: '取值范围0~3600之间的整数'

        },
        {
            name: 'distancePhotoInterval',
            ruleTitle: ['decimalTwo', 'range'],
            rule: [true, [0, 60.00]],
            message: '取值范围0.00~60.00之间的小数'
        },
        {
            name: 'speedLimit',
            ruleTitle: ['digits', 'range'],
            rule: [true, [0, 60]],
            message: '取值范围0~60之间的整数'
        }
    ]
    // 其它参数验证规则集合
    var otherValidateMsg = [
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
            name: 'speedThreshold', //使能速度阈值(km/h)
            ruleTitle: ['digits', 'range'],
            rule: [true, [0, 220]],
            message: '取值范围0~220之间的整数'
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
            rule: [true, [0, 3600]],
            message: '取值范围0~3600之间整数'
        }, {
            name: 'frequencyThreshold', //判断次数
            ruleTitle: ['digits', 'range'],
            rule: [true, [3, 10]],
            message: '取值范围3~10之间整数'
        }, {
            name: 'uploadTime', //定时上报时间间隔（s）
            ruleTitle: ['digits', 'range'],
            rule: [true, [0, 3600]],
            message: '取值范围0~3600的整数'
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
            name: 'highTemperature', //高温阈值（胎压）
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
    // 平台参数验证规则集合
    var platformValidateMsg = [
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
    // 页面结构数据映射 平台参数
    var platformDomData = [
        {id: 286407, name: '障碍物检测', type: 1},
        {id: 286405, name: '频繁变道', type: 1},
        {id: 286402, name: '车道偏离', type: 1},
        {id: 286401, name: '前向碰撞', type: 1},
        {id: 286404, name: '行人碰撞', type: 1},
        {id: 286403, name: '车距过近', type: 1},
        {id: 286406, name: '道路标志超限', type: 1},
        {id: 286410, name: '实线变道', type: 1},
        {id: 286411, name: '车厢过道行人监测', type: 1},
        {id: 286501, name: '疲劳驾驶', type: 1},
        {id: 286502, name: '接打手持电话', type: 1},
        {id: 286503, name: '抽烟', type: 1},
        {id: 286504, name: '不目视前方', type: 1},
        {id: 286505, name: '驾驶员异常', type: 1},
        {id: 286508, name: '探头遮挡', type: 1},
        {id: 286509, name: '超时驾驶', type: 1},
        {id: 286510, name: '未系安全带', type: 1},
        {id: 286511, name: '红外阻断型墨镜失效', type: 1},
        {id: 286512, name: '双脱把', type: 1},
        {id: 286513, name: '玩手机', type: 1},
        {id: 286605, name: '胎压不平衡', type: 2},
        {id: 286601, name: '胎压过高', type: 2},
        {id: 286602, name: '胎压过低', type: 2},
        {id: 286603, name: '胎温过高', type: 2},
        {id: 286604, name: '传感器异常', type: 2},
        {id: 286606, name: '慢漏气', type: 2},
        {id: 286607, name: '电池电量低', type: 2},
        {id: 286701, name: '后方接近', type: 2},
        {id: 286702, name: '左侧后方接近', type: 2},
        {id: 286703, name: '右侧后方接近', type: 2},
    ]
    var defineYueSetting = {
        init: function () {
            // 其它参数dom生成
            settingRenderer.init(domData)
            // 平台参数dom生成
            settingRenderer.platformInit(platformDomData,'platform_setting')
            $(".modal-body").addClass("modal-body-overflow");
            $(".modal-body").css({"height": "auto", "max-height": ($(window).height() - 194) + "px"});

            defineYueSetting.referenceObjRender(); //参考对象渲染
            defineYueSetting.referenceTireModel(); //轮胎型号渲染

            // 修改界面,已设置的参数渲染
            defineYueSetting.paramValueRender(settingParam);
            defineYueSetting.platformValueRender(settingPlat);

            // 表单验证方法
            defineYueSetting.drivingAssistantValidates(); //高级驾驶辅助
            defineYueSetting.driverStatusValidates(); // 驾驶员状态
            defineYueSetting.tirePressureValidates(); //胎压监测
            defineYueSetting.blindSpotValidates(); //盲点监测
            defineYueSetting.platformParamValidates(); //品台参数
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
                indexId: 1, //data.value 的第几个数据，作为input输入框的内容
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
                            defineYueSetting.platformValueRender(result.platformParam);
                            defineYueSetting.paramValueRender(result.alarmParam);
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
        // 初始化轮胎型号
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
                $('#tyreNumber_286601').val(tireModelId);
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
                defineYueSetting.setParamValue(commonParamSetting, paramType);
                for (var j = 0, adasLen = adasAlarmParamSettings.length; j < adasLen; j++) {
                    var data = adasAlarmParamSettings[j];
                    var alarmId = data.riskFunctionId;
                    defineYueSetting.setParamValue(data, alarmId);
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
                curVal = curVal.toString()
                var curTarget = $('.' + key + '_' + id + '[value="' + curVal + '"]');


                if (key.indexOf('Enable') != -1 || key.indexOf('Status') != -1 || key == 'roadSignRecognition' || key == 'pedestrianInspect') {// 单选按钮渲染
                    defineYueSetting.eventEnableChange(curTarget);
                }
                if (key.indexOf('Status') != -1 && [64, 65].includes(id)) {// 触发状态切换
                    defineYueSetting.touchStatusChange(curTarget);
                }
                if (key == 'tyreNumberName') {
                    $('#tyreNumber').val(curVal);
                }
                if (key.indexOf('AlarmEnable') != -1 || key.indexOf('pedestrianInspect') != -1) {// 一级二级报警切换
                    settingUtil.alarmChangeEvent(curTarget);
                }
                if (key == 'roadSignEnable' || key == 'roadSignRecognition' || key == 'initiativePictureEnable') { // 三个报警切换
                    defineYueSetting.threeSwitchChange(curTarget)
                }
                var targetDomNode = $('#' + key + '_' + id);
                targetDomNode.val(curVal);
            }
        },
        platformValueRender: function (dataList) {
            for (var i = 0; i < dataList.length; i++) {
                var data = dataList[i];
                var alarmId = data.riskFunctionId;
                defineYueSetting.platformSetParamValue(data, alarmId);
            }
        },
        platformSetParamValue: function (data, id) {
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
                if (!defineYueSetting[validates]()) {
                    if ($('#platformSet').hasClass('active')) {
                        layer.msg('设置参数有误');
                    }
                    return;
                }
                var paramType = curForm.find('.paramType').val();
                if (paramType == 0) {
                    var platformParamForm = settingUtil.getPlatformFormData(curForm);
                    for (var key in platformParamForm) {
                        parameter.platformParam.push(platformParamForm[key]);
                    }
                    parameter.sendFlag = false;
                } else {
                    var result = settingUtil.getFormData(curForm, protocolType);
                    parameter.alarmParam.push(result);
                }
                curTabSend.prop('disabled', true);
                var curTabId = $('.nav-tabs li.active').attr('id');
                if (!disabledTabObj[curTabId]) {
                    disabledTabObj[curTabId] = setTimeout(function () {
                        disabledTabObj[curTabId] = null;
                        defineYueSetting.curTabSendDisabled('timeOut');
                    }, 5000)
                }
            } else {
                if (!defineYueSetting.drivingAssistantValidates()
                    || !defineYueSetting.driverStatusValidates()
                    || !defineYueSetting.tirePressureValidates()
                    || !defineYueSetting.blindSpotValidates()
                    || !defineYueSetting.platformParamValidates()) {
                    layer.msg('设置参数有误');
                    return;
                }
                var drivingAssistant = settingUtil.getFormData($("#drivingAssistantForm"), protocolType);
                var driverStatus = settingUtil.getFormData($("#driverStatusForm"), protocolType);
                var tirePressure = settingUtil.getFormData($("#tirePressureForm"), protocolType);
                var blindSpot = settingUtil.getFormData($("#blindSpotForm"), protocolType);
                parameter.alarmParam.push(
                    drivingAssistant,
                    driverStatus,
                    tirePressure,
                    blindSpot
                )

                var platformParamForm = settingUtil.getPlatformFormData($("#platformParamForm"));
                for (var key in platformParamForm) {
                    parameter.platformParam.push(platformParamForm[key]);
                }

            }
            parameter.alarmParam = JSON.stringify(parameter.alarmParam);
            parameter.platformParam = JSON.stringify(parameter.platformParam);
            var url = settingParam.length == 0 ? addSendUrl : editSendUrl;
            json_ajax("POST", url, "json", true, parameter, function (data) {
                defineYueSetting.paramSendCallback(data, flag);
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
        // 高级驾驶辅助参数设置表单验证
        drivingAssistantValidates: function () {
            // 障碍物检测:286407;频繁变道:286405;车辆偏离:286402;前向碰撞:286401;行人碰撞:286404;车距过近:286403;实线变道:286410;车厢过道行人监测:286411;道路标志:286406;
            var validateArr = [286407, 286405, 286402, 286401, 286404, 286403, 286410, 286411, 286406];// 需校验的报警id
            var validateInfo = settingUtil.generateValidateObj(validateArr, otherValidateMsg)
            var commonValidateInfo = settingUtil.generateValidateObj([64], commonValidateMsg)
            // ↓↓↓↓ 修正一些有差异的数据校验 ↓↓↓↓
            settingUtil.fixValidateInfo(validateInfo, {
                dataIndex: 'timeSlotThreshold',
                riskFunctionId: ['286405'],
                data: {
                    name: 'timeSlotThreshold', //判断时间段(s)
                    ruleTitle: ['digits', 'range'],
                    rule: [true, [30, 120]],
                    message: '取值范围30~120之间整数'
                }
            })
            // ↑↑↑↑ 修正一些有差异的数据 ↑↑↑↑
            return $("#drivingAssistantForm").validate({
                ignore: '',
                rules: Object.assign({}, commonValidateInfo.rules, validateInfo.rules),
                messages: Object.assign({}, commonValidateInfo.messages, validateInfo.messages)
            }).form();
        },
        // 驾驶员状态监测参数设置表单验证
        driverStatusValidates: function () {
            //疲劳驾驶:286501; 接打电话:286502; 抽烟:286503; 不目视前方:286504; 驾驶员异常:286505; 摄像头遮挡:286508;
            //不系安全带:286510; 红外墨镜阻断失效:286511; 双脱把:286512; 玩手机:286513; 事件使能:286506;
            var validateArr = [286501, 286502, 286503, 286504, 286505, 286508, 286510, 286511, 286512, 286513];// 需校验的报警id
            var validateInfo = settingUtil.generateValidateObj(validateArr, otherValidateMsg)
            var commonValidateInfo = settingUtil.generateValidateObj([65], commonValidateMsg)
            // ↓↓↓↓ 修正一些有差异的数据校验 ↓↓↓↓
            settingUtil.fixValidateInfo(validateInfo, {
                dataIndex: 'photographTime',
                riskFunctionId: ['286501', '286502', '286503', '286504', '286505', '286510', '286511', '286512', '286513'],
                data: {
                    name: 'photographTime',
                    ruleTitle: ['decimalOne', 'range'],
                    rule: [true, [0.1, 0.5]],
                    message: '取值范围0.1~0.5之间带一位小数的数字'
                }
            })
            settingUtil.fixValidateInfo(commonValidateInfo, {
                dataIndex: 'timingPhotoInterval',
                riskFunctionId: ['65'],
                data: {
                    name: 'timingPhotoInterval',
                    ruleTitle: ['digits', 'range'],
                    rule: [true, [60, 60000]],
                    message: '取值范围60~60000之间的整数'
                }
            })
            // ↑↑↑↑ 修正一些有差异的数据 ↑↑↑↑
            return $("#driverStatusForm").validate({
                ignore: '',
                rules: Object.assign({}, commonValidateInfo.rules, validateInfo.rules),
                messages: Object.assign({}, commonValidateInfo.messages, validateInfo.messages)
            }).form();
        },
        // 胎压监测参数设置表单验证
        tirePressureValidates: function () {
            // 286601:胎压监测
            var validateArr = [286601];// 需校验的报警id
            var validateInfo = settingUtil.generateValidateObj(validateArr, otherValidateMsg)
            return $("#tirePressureForm").validate({
                ignore: '',
                rules: Object.assign({}, validateInfo.rules),
                messages: Object.assign({}, validateInfo.messages)
            }).form();
        },
        // 盲点监测参数设置表单验证
        blindSpotValidates: function () {
            // 286701:盲点监测
            return $("#blindSpotForm").validate({
                ignore: '',
                rules: {
                    rear_286701: {
                        digits: true,
                        range: [1, 10]
                    },
                    sideRear_286701: {
                        digits: true,
                        range: [1, 10]
                    }
                },
                messages: {
                    rear_286701: {
                        digits: '取值范围1~10之间的整数',
                        range: '取值范围1~10之间的整数',
                    },
                    sideRear_286701: {
                        digits: '取值范围1~10之间的整数',
                        range: '取值范围1~10之间的整数',
                    }
                },
            }).form();
        },
        // 平台参数设置表单验证
        platformParamValidates: function () {
            // 障碍物检测 286407 频繁变道 286405 车辆偏离 286402 前向碰撞 286401 行人碰撞 286404; 车距过近 286403;疲劳驾驶 286501;
            // 接打电话 286502; 抽烟 286503; 不目视前方 286504; 驾驶员异常 286505;
            // 道路标志超限 286406、胎压过高 286601、胎压过低 286602、胎温过高 286603、传感器异常 286604、胎压不平衡 286605、慢漏气 286606、
            // 电池电量低 286607、后方接近 286701、左侧后方接近 286702、右侧后方接近 286703、 超时驾驶 286509
            var validateArr = [
                286407, 286405, 286402, 286401, 286404, 286403, 286501, 286502, 286503, 286504,
                286505, 286406, 286601, 286602, 286603, 286604, 286605, 286606, 286607, 286701,
                286702, 286703, 286410, 286411, 286508, 286510, 286511, 286512, 286513, 286509
            ];
            var validateInfo = settingUtil.generateValidateObj(validateArr, platformValidateMsg)
            return $("#platformParamForm").validate({
                ignore: '',
                rules: Object.assign({}, validateInfo.rules),
                messages: Object.assign({}, validateInfo.messages)
            }).form()
        },
        /**
         * 页面交互相关方法
         * */
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

            $('#tyreNumber_286601').val(res);
        },
        // 查看/隐藏更多参数
        hiddenparameterFn: function () {
            var toControllDiv = $(this).next()
            if (!(toControllDiv.is(":hidden"))) {
                toControllDiv.slideUp();
                $(this).find("font").text("显示更多");
                $(this).find("span.fa").removeAttr("class").addClass("fa fa-chevron-down");
            } else {
                toControllDiv.slideDown();
                $(this).find("font").text("隐藏参数");
                $(this).find("span.fa").removeAttr("class").addClass("fa fa-chevron-up");
            }
        },
        // 触发状态单选按钮切换切换
        touchStatusChange: function (target) {
            var _this = target.target ? $(this) : target;
            if (!_this[0] || _this[0].className.indexOf('64') == -1) {
                return
            }
            var curVal = _this.val();
            // var touchStatusInfo = _this.closest('.form-group').siblings('.touchStatusInfo');
            var touchStatusInfo = _this.closest('.form-group').next();
            var timingInfo = $(touchStatusInfo.find('.col-md-4').get(0));// 定时拍照
            var distanceInfo = $(touchStatusInfo.find('.col-md-4').get(1));// 定距拍照
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
        // 三个开关联动
        threeSwitchChange: function (target) {
            var _this = target.target ? $(this) : target
            var isAllClose = true
            var allRadio = _this.closest('.col-md-10').find('input[type="radio"]:checked')
            allRadio.each(function (idx, item) {
                if ($(item).val() == '1') {
                    isAllClose = false
                }
            })
            var next = _this.closest('.form-group').next()
            if (isAllClose) {
                next.slideUp()
                next.find('input,select').prop('disabled', true)
            } else {
                next.slideDown()
                next.find('input,select').prop('disabled', false);
            }
        },
        // 事件使能切换
        eventEnableChange: function (target) {
            var _this = target.target ? $(this) : target;
            var parent = _this.parent().parent()
            parent.find('input').prop('checked', false);
            _this.prop('checked', true)
        }
    };
    $(function () {
        defineYueSetting.init();
        $("input").inputClear();
        $("[data-toggle='tooltip']").tooltip();
        /**
         * 页面交互
         * */
        // 报警开关切换联动
        $('.alarmSwitch').on('change', settingUtil.alarmChangeEvent)
        $('.roadSignEnable_286406, .roadSignRecognition_286406, .initiativePictureEnable_286406').on('change', defineYueSetting.threeSwitchChange)
        // 查看/隐藏更多参数
        $(".hiddenparameter").on("click", defineYueSetting.hiddenparameterFn);
        // 触发状态单选按钮切换切换
        $('input[name="touchStatus_64"]').on('change', defineYueSetting.touchStatusChange);
        // 监听页签切换,控制本页签下发按钮是否禁用
        $('.nav-tabs li').on('click', defineYueSetting.curTabSendDisabled);
        /**
         * 下发及保存
         * */
        $('#allParamSend').on('click', function () {
            defineYueSetting.paramSend('all');
        });
        $('#curTabSend').on('click', function () {
            defineYueSetting.paramSend('curTab');
        });
        $('#doSubmits').on('click', function () {
            defineYueSetting.paramSend('save');
        });
    })
}(window, $));