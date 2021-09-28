window.alarmSettingConfig = {
    // 交通部JT/T808-2013 -1
    jt2013: [
        {
            name: '预警',
            divId: 'alertList',
            data: [
                {
                    name: '危险预警',
                    alarmPush: 0,
                    alarmSettingType: '3',
                },
                {
                    name: '超速预警',
                    alarmPush: 0,
                    alarmSettingType: '13',
                    right: [
                        {
                            title: '超速预警差值，单位：km/h',
                            validate: {
                                rule: {
                                    max: 255,
                                    min: 1
                                }
                            }
                        },
                    ]
                },
                {
                    name: '疲劳驾驶预警',
                    alarmPush: 0,
                    alarmSettingType: '14',
                    right: [
                        {
                            title: '疲劳驾驶预警差值，单位：秒',
                            validate: {
                                rule: {
                                    max: 65535,
                                    min: 1
                                }
                            }
                        },
                    ]
                },
                {
                    name: '碰撞预警',
                    alarmPush: 0,
                    alarmSettingType: '29',
                    right: [
                        {
                            title: '碰撞时间，单位：毫秒',
                            validate: {
                                rule: {
                                    max: 1020,
                                    min: 1
                                }
                            }
                        },
                        {
                            title: '碰撞加速度，单位：0.1g，范围：0-79',
                            validate: {
                                rule: {max: 79},
                            }
                        },
                    ]
                },
                {
                    name: '侧翻预警',
                    alarmPush: 0,
                    alarmSettingType: '30',
                    right: [
                        {
                            title: '侧翻角度，单位：度',
                            validate: {
                                rule: {
                                    max: 90,
                                    min: 1
                                }
                            }},
                    ]
                }
            ]
        },
        {
            name: '驾驶员引起报警',
            divId: 'driverAlarmList',
            data: [
                {
                    name: "紧急报警",
                    alarmPush: 1,
                    alarmSettingType: '0',
                },
                {
                    name: "超速报警（路网）",
                    alarmPush: 1,
                    alarmSettingType: '164',
                },
                {
                    name: "超速报警",
                    alarmPush: 1,
                    alarmSettingType: '1',
                    right: [
                        {
                            title: '最高速度，单位：km/h',
                            validate: {
                                rule: {max: 255, min:1},
                            }
                        },
                        {
                            title: '超速持续时间，单位：秒',
                            validate: {
                                rule: {max: 79},
                            }
                        },
                    ]
                },
                {
                    name: "疲劳驾驶",
                    alarmPush: 1,
                    alarmSettingType: '2',
                    right: [
                        {
                            title: '连续驾驶时间门限，单位：秒',
                            validate: {
                                rule: {max: 86400, min:1},
                            }
                        },
                        {
                            title: '最小休息时间，单位：秒',
                            validate: {
                                rule: {max: 21600, min:1},
                            }
                        },
                    ]
                },
                {
                    name: "当天累积驾驶超时",
                    alarmPush: 0,
                    alarmSettingType: '18',
                    right: [
                        {
                            title: '当天累计驾驶时间门限，单位：秒',
                            validate: {
                                rule: {max: 86400, min:1},
                            }
                        },
                    ]
                },
                {
                    name: "超时停车",
                    alarmPush: 0,
                    alarmSettingType: '19',
                    right: [
                        {
                            title: '最长停车时间，单位：秒',
                            validate: {
                                rule: {max: 86400, min:1},
                            }
                        },
                    ]
                },
                {
                    name: "路线偏离报警",
                    alarmPush: 1,
                    alarmSettingType: '23',
                },
                {
                    name: "进出区域",
                    alarmPush: 1,
                    alarmSettingType: '20',
                },
                {
                    name: "进出线路",
                    alarmPush: 1,
                    alarmSettingType: '21',
                },
                {
                    name: "路段行驶时间不足/过长",
                    alarmPush: 0,
                    alarmSettingType: '22',
                }
            ]
        },
        {
            name: '车辆报警',
            divId: 'vehicleAlarmList',
            data: [
                {
                    name: "车辆VSS故障",
                    alarmPush: 0,
                    alarmSettingType: '24',
                },
                {
                    name: "车辆油量异常",
                    alarmPush: 0,
                    alarmSettingType: '25',
                },
                {
                    name: "车辆被盗",
                    alarmPush: 0,
                    alarmSettingType: '26',
                },
                {
                    name: "车辆非法点火",
                    alarmPush: 1,
                    alarmSettingType: '27',
                },
                {
                    name: "车辆非法位移",
                    alarmPush: 0,
                    alarmSettingType: '28',
                    right: [
                        {
                            title: '车辆非法位移（位移半径），单位：米',
                            validate: {
                                rule: {max: 1000, min:1},
                            }
                        },
                    ]
                },
                {
                    name: "非法开门报警",
                    alarmPush: 0,
                    alarmSettingType: '31',
                }
            ]
        },
        {
            name: '故障报警',
            divId: 'faultAlarmList',
            data: [
                {
                    name: "GNSS模块发生故障",
                    alarmPush: 0,
                    alarmSettingType: '4',
                },
                {
                    name: "GNSS天线未接或被剪断",
                    alarmPush: 0,
                    alarmSettingType: '5',
                },
                {
                    name: "GNSS天线短路",
                    alarmPush: 0,
                    alarmSettingType: '6',
                },
                {
                    name: "终端主电源欠压",
                    alarmPush: 0,
                    alarmSettingType: '7',
                },
                {
                    name: "终端主电源掉电",
                    alarmPush: 0,
                    alarmSettingType: '8',
                },
                {
                    name: "终端LCD或显示器故障",
                    alarmPush: 0,
                    alarmSettingType: '9',
                },
                {
                    name: "TTS模块故障",
                    alarmPush: 0,
                    alarmSettingType: '10',
                },
                {
                    name: "摄像头故障",
                    alarmPush: 0,
                    alarmSettingType: '11',
                },
                {
                    name: "道路运输证IC卡模块故障",
                    alarmPush: 0,
                    alarmSettingType: '12',
                }
            ]
        },
        {
            name: 'F3高精度报警',
            divId: 'highPrecisionAlarmList',
            sliderNum: 3,
            data: [
                {
                    name: "设备电量报警",
                    alarmPush: 1,
                    alarmSettingType: '18811',
                    right: [
                        {
                            title: '电量报警阈值，单位：%',
                            validate: {
                                rule: {max: 100},
                            }
                        },
                    ]
                },
                {
                    name: "急加速报警",
                    alarmPush: 1,
                    alarmSettingType: '18711',
                    right: [
                        {
                            title: '急加速报警阈值，单位：m/s²',
                            validate: {
                                rule: /^([0-1](\.\d)?$|^2)$|^(2\.[0-5])$/,
                                message: '请输入0-2.5范围的数字且保留一位小数'
                            }
                        },
                    ]
                },
                {
                    name: "急减速报警",
                    alarmPush: 1,
                    alarmSettingType: '18712',
                    right: [
                        {
                            title: '急减速报警阈值，单位：m/s²',
                            validate: {
                                rule: /^([0-1](\.\d)?$|^2)$|^(2\.[0-5])$/,
                                message: '请输入0-2.5范围的数字且保留一位小数'
                            }
                        },
                    ]
                },
                {
                    name: "急转弯报警",
                    alarmPush: 1,
                    alarmSettingType: '18713',
                    right: [
                        {
                            title: '急转弯报警阈值，单位：1°/s²',
                            validate: {
                                rule: {max: 100},
                            }
                        },
                    ]
                },
                {
                    name: "异常移动报警",
                    alarmPush: 1,
                    alarmSettingType: '18714',
                },
                {
                    name: "加速度传感器异常报警",
                    alarmPush: 1,
                    alarmSettingType: '18715',
                },
                {
                    name: "ACC接线异常报警",
                    alarmPush: 1,
                    alarmSettingType: '18716',
                },
                {
                    name: "车辆电池馈电报警",
                    alarmPush: 1,
                    alarmSettingType: '18717',
                },
                {
                    name: "碰撞报警",
                    alarmPush: 1,
                    alarmSettingType: '18719',
                    right: [
                        {
                            title: '碰撞报警阈值，单位：m/s²',
                            validate: {
                                rule: /^[0-4](\.\d)?$|^5$/,
                                message: '请输入0-5范围的数字且保留一位小数'
                            }
                        },
                    ]
                }
            ]
        },
        {
            name: 'F3传感器报警',
            divId: 'sensorAlarmList',
            data: [
                {
                    name: "温度报警",
                    alarmPush: 1,
                    alarmSettingType: '6511',
                },
                {
                    name: "湿度报警",
                    alarmPush: 1,
                    alarmSettingType: '6611',
                },
                {
                    name: "超速报警(F3)",
                    alarmPush: 1,
                    alarmSettingType: '67',
                    right: [
                        {
                            title: '最高速度，单位：km/h',
                            validate: {
                                rule: {max: 255, min:1},
                            }
                        },
                    ]
                },
                {
                    name: "加漏油报警",
                    alarmPush: 1,
                    alarmSettingType: '6811',
                },
                {
                    name: "胎压报警",
                    alarmPush: 1,
                    alarmSettingType: '14300',
                },
                {
                    name: "反转报警",
                    alarmPush: 1,
                    alarmSettingType: '124',
                },
                {
                    name: "工时报警",
                    alarmPush: 1,
                    alarmSettingType: '13213',
                },
                {
                    name: "载重报警",
                    alarmPush: 1,
                    alarmSettingType: '7012',
                }
            ]
        },
        {
            name: '终端I/O',
            divId: 'deviceIoAlarmList',
            data: [
                // 该项为动态生成项，后端返回数据格式如下
                // {
                //     name: "I/O0",
                //     alarmPush: 0,
                //     alarmSettingName: "I/O0",
                //     alarmSettingType: "14000",
                //     highSignalType: 2,
                //     lowSignalType: 1,
                //     parameterValue: null,
                //     stateOne: "门开11",
                //     stateTwo: "门关11",
                // }
            ]
        },
        {
            name: 'I/O采集1',
            divId: 'ioCollectionOneAlarmList',
            data: []
        },
        {
            name: 'I/O采集2',
            divId: 'ioCollectionTwoAlarmList',
            data: []
        },
        {
            name: '平台报警',
            divId: 'platAlarmList',
            data: [
                {
                    name: "进出区域",
                    alarmPush: 1,
                    alarmSettingType: '7211',
                },
                {
                    name: "进出线路",
                    alarmPush: 1,
                    alarmSettingType: '7311',
                },
                {
                    name: "关键点报警",
                    alarmPush: 1,
                    alarmSettingType: '11911',
                },
                {
                    name: "长时间下线",
                    alarmPush: 0,
                    alarmSettingType: '82',
                    right: [
                        {
                            title: '单位：分钟',
                            validate: {
                                rule: {max: 1440, min:10},
                            }
                        },
                    ]
                },
                {
                    name: "路线偏离报警",
                    alarmPush: 1,
                    alarmSettingType: '75',
                    right: [
                        {
                            title: '',
                            type: 'option',
                            data: [
                                { value: 0, name: '通用标准' },
                                { value: 2, name: '四川标准' },
                            ]
                        },
                    ]
                },
                {
                    name: "疲劳驾驶",
                    alarmPush: 1,
                    alarmSettingType: '79',
                },
                {
                    name: "24H累计疲劳驾驶",
                    alarmPush: 1,
                    alarmSettingType: '203',
                },
                {
                    name: "异动报警",
                    alarmPush: 1,
                    alarmSettingType: '77',
                    right: [
                        {
                            title: '',
                            type: 'option',
                            data: [
                                { value: 0, name: '通用标准' },
                                { value: 1, name: '山西标准' },
                                { value: 2, name: '四川标准' },
                            ],
                        },
                        {
                            title: '禁行时段，单位：小时',
                            type: 'date-hms',
                        },
                        {
                            title: '禁行日期',
                            type: 'date-mm',
                        },
                    ]
                },
                {
                    name: "超速预警",
                    alarmPush: 1,
                    alarmSettingType: '81',
                    right: [
                        {
                            title: '超速预警差值，单位：km/h',
                            validate: {
                                rule: {max: 100},
                            }
                        },
                    ]
                },
                {
                    name: "超速报警",
                    alarmPush: 1,
                    alarmSettingType: '76',
                    right: [
                        {
                            title: '路网限速',
                            type: 'option',
                            paramName: 'param1',
                            data: [
                                { value: 0, name: '通用标准' },
                                { value: 1, name: '山西标准' },
                                { value: 2, name: '四川标准' },
                            ],
                            titleRender: function () {
                                return `
                                <label >
                                    <input type="checkbox" id="roadNetSpeedLimit" value="0">
                                    <i class="fa fa-question-circle fa-lg infoTitle" data-toggle="tooltip" data-placement="top" data-original-title="勾选后调用第三方道路限速地图接口获取道路级别的路网限速值，平台根据道路级别可设置平台限速值，报警时以两者中限速值小的为准"></i>
                                    路网限速
                                </label>
                            `
                            }
                        },
                        {
                            title: '最高速度，单位：km/h',
                            paramName: 'param2',
                            validate: {
                                rule: {max: 160},
                            }
                        },
                        {
                            title: '高速限速，单位：km/h',
                            paramName: 'param6',
                            hidden: true,
                            validate: {
                                rule: {max: 160},
                            }
                        },
                        {
                            title: '国道限速，单位：km/h',
                            paramName: 'param7',
                            hidden: true,
                            validate: {
                                rule: {max: 160},
                            }
                        },
                        {
                            title: '省道限速，单位：km/h',
                            paramName: 'param8',
                            hidden: true,
                            validate: {
                                rule: {max: 160},
                            }
                        },
                        {
                            title: '县道限速，单位：km/h',
                            paramName: 'param9',
                            hidden: true,
                            validate: {
                                rule: {max: 160},
                            }
                        },
                        {
                            title: '其他道限速，单位：km/h',
                            paramName: 'param10',
                            hidden: true,
                            validate: {
                                rule: {max: 160},
                            }
                        },

                        {
                            title: '夜间限速百分比，单位：%' ,
                            paramName: 'param3',
                            validate: {
                                rule: {max: 100,min: 1},
                            },
                            titleRender: function () {
                                return `
                                <label style="width: 35%">
                                    <i class="fa fa-question-circle fa-lg infoTitle" data-toggle="tooltip" data-placement="top" data-original-title="夜间速度达到日间限速*设置百分比时触发报警"></i>
                                    夜间限速百分比，单位：%
                                    <span id="overSpeedHide" style="cursor: pointer;position: absolute;right: 0;">显示更多<span class="fa fa-chevron-down" aria-hidden="true"></span></span>
                                </label>
                            `
                            }
                        },
                        {
                            title: '夜间限速时段，单位：小时',
                            paramName: 'param4',
                            type: 'date-hms',
                            hidden: true
                        },
                        {
                            title: '夜间限速日期',
                            paramName: 'param5',
                            type: 'date-mm',
                            hidden: true
                        },
                    ]
                },
                {
                    name: "ACC信号异常报警",
                    alarmPush: 1,
                    alarmSettingType: '150',
                    right: [
                        {
                            title: '异常持续时间，单位：分钟',
                            validate: {
                                rule: {max: 1440, min: 1},
                            }
                        },
                        {
                            title: '最高速度，单位：km/h',
                            validate: {
                                rule: {max: 160, min: 10},
                            }
                        },
                    ]
                },
                {
                    name: "位置信息异常报警",
                    alarmPush: 1,
                    alarmSettingType: '151',
                    right: [
                        {
                            title: '异常持续时间，单位：分钟',
                            validate: {
                                rule: {max: 1440, min: 1},
                            }
                        },
                    ]
                },
                {
                    name: "疑似人为屏蔽报警",
                    alarmPush: -1,
                    alarmSettingType: '209',
                    right: [
                        {
                            title: '点位信息间隔时间，单位：分钟',
                            validate: {
                                rule: {max: 20, min: 5},
                            }
                        },
                        {
                            title: '点位信息间隔里程，单位：km',
                            validate: {
                                rule: {max: 60, min: 5},
                            }
                        },
                        {
                            title: '连续次数阈值，大于等于此阈值时触发报警',
                            validate: {
                                rule: {max: 60, min: 1},
                            }
                        },
                    ]
                },
            ]
        },
    ],
    jt2013Default: [
        {
            name: '预警',
            divId: 'alertList',
            data: [
                {
                    name: '危险预警',
                    alarmPush: 0,
                    alarmSettingType: '3',
                },
                {
                    name: '超速预警',
                    alarmPush: 0,
                    alarmSettingType: '13',
                    right: [
                        {
                            title: '超速预警差值，单位：km/h',
                            value: 5,
                            validate: {
                                rule: {max: 255,min: 1}
                            }
                        },
                    ]
                },
                {
                    name: '疲劳驾驶预警',
                    alarmPush: 0,
                    alarmSettingType: '14',
                    right: [
                        {
                            title: '疲劳驾驶预警差值，单位：秒',
                            value: 1800,
                            validate: {
                                rule: {max: 65535,min: 1}
                            }
                        },
                    ]
                },
                {
                    name: '碰撞预警',
                    alarmPush: 0,
                    alarmSettingType: '29',
                    right: [
                        {
                            title: '碰撞时间，单位：毫秒',
                            value: 200,
                            validate: {
                                rule: {max: 1020,min: 1}
                            }
                        },
                        {
                            title: '碰撞加速度，单位：0.1g，范围：0-79',
                            value: 10,
                            validate: {
                                rule: {max: 79},
                            }
                        },
                    ]
                },
                {
                    name: '侧翻预警',
                    alarmPush: 0,
                    alarmSettingType: '30',
                    right: [
                        {
                            title: '侧翻角度，单位：度',
                            value: 30,
                            validate: {
                                rule: {max: 90,min: 1},
                            }},
                    ]
                }
            ]
        },
        {
            name: '驾驶员引起报警',
            divId: 'driverAlarmList',
            data: [
                {
                    name: "紧急报警",
                    alarmPush: 1,
                    alarmSettingType: '0',
                },
                {
                    name: "超速报警（路网）",
                    alarmPush: 1,
                    alarmSettingType: '164',
                },
                {
                    name: "超速报警",
                    alarmPush: 1,
                    alarmSettingType: '1',
                    right: [
                        {
                            title: '最高速度，单位：km/h',
                            value: '100',
                            validate: {
                                rule: {max: 255,min: 1},
                            }
                        },
                        {
                            title: '超速持续时间，单位：秒',
                            value: '10',
                            validate: {
                                rule: {max: 79},
                            }
                        },
                    ]
                },
                {
                    name: "疲劳驾驶",
                    alarmPush: 1,
                    alarmSettingType: '2',
                    right: [
                        {
                            title: '连续驾驶时间门限，单位：秒',
                            value: '14400',
                            validate: {
                                rule: {max: 86400,min: 1},
                            }
                        },
                        {
                            title: '最小休息时间，单位：秒',
                            value: '1200',
                            validate: {
                                rule: {max: 21600,min: 1},
                            }
                        },
                    ]
                },
                {
                    name: "当天累积驾驶超时",
                    alarmPush: 0,
                    alarmSettingType: '18',
                    right: [
                        {
                            title: '当天累计驾驶时间门限，单位：秒',
                            value: '28800',
                            validate: {
                                rule: {max: 86400,min: 1},
                            }
                        },
                    ]
                },
                {
                    name: "超时停车",
                    alarmPush: 0,
                    alarmSettingType: '19',
                    right: [
                        {
                            title: '最长停车时间，单位：秒',
                            value: '600',
                            validate: {
                                rule: {max: 86400,min: 1},
                            }
                        },
                    ]
                },
                {
                    name: "路线偏离报警",
                    alarmPush: 1,
                    alarmSettingType: '23',
                },
                {
                    name: "进出区域",
                    alarmPush: 1,
                    alarmSettingType: '20',
                },
                {
                    name: "进出线路",
                    alarmPush: 1,
                    alarmSettingType: '21',
                },
                {
                    name: "路段行驶时间不足/过长",
                    alarmPush: 0,
                    alarmSettingType: '22',
                }
            ]
        },
        {
            name: '车辆报警',
            divId: 'vehicleAlarmList',
            data: [
                {
                    name: "车辆VSS故障",
                    alarmPush: 0,
                    alarmSettingType: '24',
                },
                {
                    name: "车辆油量异常",
                    alarmPush: 0,
                    alarmSettingType: '25',
                },
                {
                    name: "车辆被盗",
                    alarmPush: 0,
                    alarmSettingType: '26',
                },
                {
                    name: "车辆非法点火",
                    alarmPush: 1,
                    alarmSettingType: '27',
                },
                {
                    name: "车辆非法位移",
                    alarmPush: 0,
                    alarmSettingType: '28',
                    right: [
                        {
                            title: '车辆非法位移（位移半径），单位：米',
                            value: '20',
                            validate: {
                                rule: {max: 1000,min: 1},
                            }
                        },
                    ]
                },
                {
                    name: "非法开门报警",
                    alarmPush: 0,
                    alarmSettingType: '31',
                }
            ]
        },
        {
            name: '故障报警',
            divId: 'faultAlarmList',
            data: [
                {
                    name: "GNSS模块发生故障",
                    alarmPush: 0,
                    alarmSettingType: '4',
                },
                {
                    name: "GNSS天线未接或被剪断",
                    alarmPush: 0,
                    alarmSettingType: '5',
                },
                {
                    name: "GNSS天线短路",
                    alarmPush: 0,
                    alarmSettingType: '6',
                },
                {
                    name: "终端主电源欠压",
                    alarmPush: 0,
                    alarmSettingType: '7',
                },
                {
                    name: "终端主电源掉电",
                    alarmPush: 0,
                    alarmSettingType: '8',
                },
                {
                    name: "终端LCD或显示器故障",
                    alarmPush: 0,
                    alarmSettingType: '9',
                },
                {
                    name: "TTS模块故障",
                    alarmPush: 0,
                    alarmSettingType: '10',
                },
                {
                    name: "摄像头故障",
                    alarmPush: 0,
                    alarmSettingType: '11',
                },
                {
                    name: "道路运输证IC卡模块故障",
                    alarmPush: 0,
                    alarmSettingType: '12',
                }
            ]
        },
        {
            name: 'F3高精度报警',
            divId: 'highPrecisionAlarmList',
            sliderNum: 3,
            data: [
                {
                    name: "设备电量报警",
                    alarmPush: 1,
                    alarmSettingType: '18811',
                    right: [
                        {
                            title: '电量报警阈值，单位：%',
                            value: '20',
                            validate: {
                                rule: {max: 100},
                            }
                        },
                    ]
                },
                {
                    name: "急加速报警",
                    alarmPush: 1,
                    alarmSettingType: '18711',
                    right: [
                        {
                            title: '急加速报警阈值，单位：m/s²',
                            value: '0.5',
                            validate: {
                                rule: /^([0-1](\.\d)?$|^2)$|^(2\.[0-5])$/,
                                message: '请输入0-2.5范围的数字且保留一位小数'
                            }
                        },
                    ]
                },
                {
                    name: "急减速报警",
                    alarmPush: 1,
                    alarmSettingType: '18712',
                    right: [
                        {
                            title: '急减速报警阈值，单位：m/s²',
                            value: '0.5',
                            validate: {
                                rule: /^([0-1](\.\d)?$|^2)$|^(2\.[0-5])$/,
                                message: '请输入0-2.5范围的数字且保留一位小数'
                            }
                        },
                    ]
                },
                {
                    name: "急转弯报警",
                    alarmPush: 1,
                    alarmSettingType: '18713',
                    right: [
                        {
                            title: '急转弯报警阈值，单位：1°/s²',
                            value: '10',
                            validate: {
                                rule: {max: 100},
                            }
                        },
                    ]
                },
                {
                    name: "异常移动报警",
                    alarmPush: 1,
                    alarmSettingType: '18714',
                },
                {
                    name: "加速度传感器异常报警",
                    alarmPush: 1,
                    alarmSettingType: '18715',
                },
                {
                    name: "ACC接线异常报警",
                    alarmPush: 1,
                    alarmSettingType: '18716',
                },
                {
                    name: "车辆电池馈电报警",
                    alarmPush: 1,
                    alarmSettingType: '18717',
                },
                {
                    name: "碰撞报警",
                    alarmPush: 1,
                    alarmSettingType: '18719',
                    right: [
                        {
                            title: '碰撞报警阈值，单位：m/s²',
                            value: '1',
                            validate: {
                                rule: /^[0-4](\.\d)?$|^5$/,
                                message: '请输入0-5范围的数字且保留一位小数'
                            }
                        },
                    ]
                }
            ]
        },
        {
            name: 'F3传感器报警',
            divId: 'sensorAlarmList',
            data: [
                {
                    name: "温度报警",
                    alarmPush: 1,
                    alarmSettingType: '6511',
                },
                {
                    name: "湿度报警",
                    alarmPush: 1,
                    alarmSettingType: '6611',
                },
                {
                    name: "超速报警(F3)",
                    alarmPush: 1,
                    alarmSettingType: '67',
                    right: [
                        {
                            title: '最高速度，单位：km/h',
                            value: '120',
                            validate: {
                                rule: {max: 255,min: 1},
                            }
                        },
                    ]
                },
                {
                    name: "加漏油报警",
                    alarmPush: 1,
                    alarmSettingType: '6811',
                },
                {
                    name: "胎压报警",
                    alarmPush: 1,
                    alarmSettingType: '14300',
                },
                {
                    name: "反转报警",
                    alarmPush: 1,
                    alarmSettingType: '124',
                },
                {
                    name: "工时报警",
                    alarmPush: 1,
                    alarmSettingType: '13213',
                },
                {
                    name: "载重报警",
                    alarmPush: 1,
                    alarmSettingType: '7012',
                }
            ]
        },
        {
            name: '终端I/O',
            divId: 'deviceIoAlarmList',
            data: [
                // 该项为动态生成项，后端返回数据格式如下
                // {
                //     name: "I/O0",
                //     alarmPush: 0,
                //     alarmSettingName: "I/O0",
                //     alarmSettingType: "14000",
                //     highSignalType: 2,
                //     lowSignalType: 1,
                //     parameterValue: null,
                //     stateOne: "门开11",
                //     stateTwo: "门关11",
                // }
            ]
        },
        {
            name: 'I/O采集1',
            divId: 'ioCollectionOneAlarmList',
            data: []
        },
        {
            name: 'I/O采集2',
            divId: 'ioCollectionTwoAlarmList',
            data: []
        },
        {
            name: '平台报警',
            divId: 'platAlarmList',
            data: [
                {
                    name: "进出区域",
                    alarmPush: 1,
                    alarmSettingType: '7211',
                },
                {
                    name: "进出线路",
                    alarmPush: 1,
                    alarmSettingType: '7311',
                },
                {
                    name: "关键点报警",
                    alarmPush: 1,
                    alarmSettingType: '11911',
                },
                {
                    name: "长时间下线",
                    alarmPush: 0,
                    alarmSettingType: '82',
                    right: [
                        {
                            title: '单位：分钟',
                            value: '10',
                            validate: {
                                rule: {max: 1440,min: 10},
                            }
                        },
                    ]
                },
                {
                    name: "路线偏离报警",
                    alarmPush: 1,
                    alarmSettingType: '75',
                    right: [
                        {
                            title: '',
                            type: 'option',
                            value: 0,
                            data: [
                                { value: 0, name: '通用标准' },
                                { value: 2, name: '四川标准' },
                            ]
                        },
                    ]
                },
                {
                    name: "疲劳驾驶",
                    alarmPush: 1,
                    alarmSettingType: '79',
                },
                {
                    name: "24H累计疲劳驾驶",
                    alarmPush: 1,
                    alarmSettingType: '203',
                },
                {
                    name: "异动报警",
                    alarmPush: 1,
                    alarmSettingType: '77',
                    right: [
                        {
                            title: '',
                            type: 'option',
                            value: 0,
                            data: [
                                { value: 0, name: '通用标准' },
                                { value: 1, name: '山西标准' },
                                { value: 2, name: '四川标准' },
                            ],
                        },
                        {
                            title: '禁行时段，单位：小时',
                            value: '02:00 -- 05:00',
                            type: 'date-hms',
                        },
                        {
                            title: '禁行日期',
                            type: 'date-mm',
                        },
                    ]
                },
                {
                    name: "超速预警",
                    alarmPush: 1,
                    alarmSettingType: '81',
                    right: [
                        {
                            title: '超速预警差值，单位：km/h',
                            value: '5',
                            validate: {
                                rule: {max: 100},
                            }
                        },
                    ]
                },
                {
                    name: "超速报警",
                    alarmPush: 1,
                    alarmSettingType: '76',
                    right: [
                        {
                            title: '路网限速',
                            type: 'option',
                            value: 0,
                            paramName: 'param1',
                            data: [
                                { value: 0, name: '通用标准' },
                                { value: 1, name: '山西标准' },
                                { value: 2, name: '四川标准' },
                            ],
                            titleRender: function () {
                                return `
                                <label >
                                    <input type="checkbox" id="roadNetSpeedLimit" value="0">
                                    <i class="fa fa-question-circle fa-lg infoTitle" data-toggle="tooltip" data-placement="top" data-original-title="勾选后调用第三方道路限速地图接口获取道路级别的路网限速值，平台根据道路级别可设置平台限速值，报警时以两者中限速值小的为准"></i>
                                    路网限速
                                </label>
                            `
                            }
                        },
                        {
                            title: '最高速度，单位：km/h',
                            paramName: 'param2',
                            value: '120',
                            validate: {
                                rule: {max: 160},
                            }
                        },
                        {
                            title: '高速限速，单位：km/h',
                            paramName: 'param6',
                            hidden: true,
                            validate: {
                                rule: {max: 160},
                            }
                        },
                        {
                            title: '国道限速，单位：km/h',
                            paramName: 'param7',
                            hidden: true,
                            validate: {
                                rule: {max: 160},
                            }
                        },
                        {
                            title: '省道限速，单位：km/h',
                            paramName: 'param8',
                            hidden: true,
                            validate: {
                                rule: {max: 160},
                            }
                        },
                        {
                            title: '县道限速，单位：km/h',
                            paramName: 'param9',
                            hidden: true,
                            validate: {
                                rule: {max: 160},
                            }
                        },
                        {
                            title: '其他道限速，单位：km/h',
                            paramName: 'param10',
                            hidden: true,
                            validate: {
                                rule: {max: 160}
                            }
                        },

                        {
                            title: '夜间限速百分比，单位：%' ,
                            paramName: 'param3',
                            value: '80',
                            validate: {
                                rule: {max: 100,min: 1},
                            },
                            titleRender: function () {
                                return `
                                <label style="width: 35%">
                                    <i class="fa fa-question-circle fa-lg infoTitle" data-toggle="tooltip" data-placement="top" data-original-title="夜间速度达到日间限速*设置百分比时触发报警"></i>
                                    夜间限速百分比，单位：%
                                    <span id="overSpeedHide" style="cursor: pointer;position: absolute;right: 0;">显示更多<span class="fa fa-chevron-down" aria-hidden="true"></span></span>
                                </label>
                            `
                            }
                        },
                        {
                            title: '夜间限速时段，单位：小时',
                            paramName: 'param4',
                            type: 'date-hms',
                            hidden: true
                        },
                        {
                            title: '夜间限速日期',
                            paramName: 'param5',
                            type: 'date-mm',
                            hidden: true
                        },
                    ]
                },
                {
                    name: "ACC信号异常报警",
                    alarmPush: 1,
                    alarmSettingType: '150',
                    right: [
                        {
                            title: '异常持续时间，单位：分钟',
                            value: '30',
                            validate: {
                                rule: {max: 1440,min: 1},
                            }
                        },
                        {
                            title: '最高速度，单位：km/h',
                            value: '30',
                            validate: {
                                rule: {max: 160,min: 10},
                            }
                        },
                    ]
                },
                {
                    name: "位置信息异常报警",
                    alarmPush: 1,
                    alarmSettingType: '151',
                    right: [
                        {
                            title: '异常持续时间，单位：分钟',
                            value: '30',
                            validate: {
                                rule: {max: 1440,min: 1},
                            }
                        },
                    ]
                },
                {
                    name: "疑似人为屏蔽报警",
                    alarmPush: -1,
                    alarmSettingType: '209',
                    right: [
                        {
                            title: '点位信息间隔时间，单位：分钟',
                            value: '10',
                            validate: {
                                rule: {max: 20,min: 5},
                            }
                        },
                        {
                            title: '点位信息间隔里程，单位：km',
                            value: '10',
                            validate: {
                                rule: {max: 60,min: 5},
                            }
                        },
                        {
                            title: '连续次数阈值，大于等于此阈值时触发报警',
                            value: '1',
                            validate: {
                                rule: {max: 60,min: 1},
                            }
                        },
                    ]
                },
            ]
        },
    ],
    // 交通部JT/T808-2019 11
    jt2019: [
        {
            name: '预警',
            divId: 'alertList',
            data: [
                {
                    name: '超速预警',
                    alarmPush: 0,
                    alarmSettingType: '13',
                    right: [
                        {
                            title: '超速预警差值，单位：km/h',
                            validate: {
                                rule: { max: 255, min: 1 }
                            }
                        },
                    ]
                },
                {
                    name: '疲劳驾驶预警',
                    alarmPush: 0,
                    alarmSettingType: '14',
                    right: [
                        {
                            title: '疲劳驾驶预警差值，单位：秒',
                            validate: {
                                rule: { max: 65535, min: 1 },
                            }
                        },
                    ]
                },
                {
                    name: '侧翻预警',
                    alarmPush: 0,
                    alarmSettingType: '30',
                    right: [
                        {
                            title: '侧翻角度，单位：度',
                            validate: {
                                rule: { max: 90, min: 1 },
                            }},
                    ]
                },
                {
                    name: '危险驾驶行为报警',
                    alarmPush: 0,
                    alarmSettingType: '3',
                },
                {
                    name: '碰撞侧翻报警',
                    alarmPush: 0,
                    alarmSettingType: '158',
                    right: [
                        {
                            title: '碰撞时间，单位：毫秒',
                            validate: {
                                rule: { max: 1020, min: 1 },
                            }
                        },
                        {
                            title: '碰撞加速度，单位：0.1g，范围：0-79',
                            validate: {
                                rule: {max: 79},
                            }
                        },
                    ]
                },
            ]
        },
        {
            name: '驾驶员引起报警',
            divId: 'driverAlarmList',
            data: [
                {
                    name: "紧急报警",
                    alarmPush: 1,
                    alarmSettingType: '0',
                },
                {
                    name: "超速报警（路网）",
                    alarmPush: 1,
                    alarmSettingType: '164',
                },
                {
                    name: "超速报警",
                    alarmPush: 1,
                    alarmSettingType: '1',
                    right: [
                        {
                            title: '最高速度，单位：km/h',
                            validate: {
                                rule: {max: 255,min: 1},
                            }
                        },
                        {
                            title: '超速持续时间，单位：秒',
                            validate: {
                                rule: {max: 79},
                            }
                        },
                    ]
                },
                {
                    name: "疲劳驾驶",
                    alarmPush: 1,
                    alarmSettingType: '2',
                    right: [
                        {
                            title: '连续驾驶时间门限，单位：秒',
                            validate: {
                                rule: {max: 86400,min: 1},
                            }
                        },
                        {
                            title: '最小休息时间，单位：秒',
                            validate: {
                                rule: {max: 21600,min: 1},
                            }
                        },
                    ]
                },
                {
                    name: "当天累积驾驶超时",
                    alarmPush: 0,
                    alarmSettingType: '18',
                    right: [
                        {
                            title: '当天累计驾驶时间门限，单位：秒',
                            validate: {
                                rule: {max: 86400,min: 1},
                            }
                        },
                    ]
                },
                {
                    name: "超时停车",
                    alarmPush: 0,
                    alarmSettingType: '19',
                    right: [
                        {
                            title: '最长停车时间，单位：秒',
                            validate: {
                                rule: {max: 86400,min: 1},
                            }
                        },
                    ]
                },
                {
                    name: "路线偏离报警",
                    alarmPush: 1,
                    alarmSettingType: '23',
                },
                {
                    name: "车道偏离报警",
                    alarmPush: 1,
                    alarmSettingType: '149',
                },
                {
                    name: "前撞报警",
                    alarmPush: 1,
                    alarmSettingType: '148',
                },
                {
                    name: "违规行驶报警",
                    alarmPush: 1,
                    alarmSettingType: '15',
                    right: [
                        {
                            title: '违规行驶时段',
                            type: 'date-hms',
                        },
                    ]
                },
                {
                    name: "进出区域",
                    alarmPush: 1,
                    alarmSettingType: '20',
                },
                {
                    name: "进出线路",
                    alarmPush: 1,
                    alarmSettingType: '21',
                },
                {
                    name: "路段行驶时间不足/过长",
                    alarmPush: 0,
                    alarmSettingType: '22',
                }
            ]
        },
        {
            name: '车辆报警',
            divId: 'vehicleAlarmList',
            data: [
                {
                    name: "车辆VSS故障",
                    alarmPush: 0,
                    alarmSettingType: '24',
                },
                {
                    name: "车辆油量异常",
                    alarmPush: 0,
                    alarmSettingType: '25',
                },
                {
                    name: "车辆被盗",
                    alarmPush: 0,
                    alarmSettingType: '26',
                },
                {
                    name: "车辆非法点火",
                    alarmPush: 1,
                    alarmSettingType: '27',
                },
                {
                    name: "车辆非法位移",
                    alarmPush: 0,
                    alarmSettingType: '28',
                    right: [
                        {
                            title: '车辆非法位移（位移半径），单位：米',
                            validate: {
                                rule: {max: 1000,min: 1},
                            }
                        },
                    ]
                }
            ]
        },
        {
            name: '故障报警',
            divId: 'faultAlarmList',
            data: [
                {
                    name: "GNSS模块发生故障",
                    alarmPush: 0,
                    alarmSettingType: '4',
                },
                {
                    name: "GNSS天线未接或被剪断",
                    alarmPush: 0,
                    alarmSettingType: '5',
                },
                {
                    name: "GNSS天线短路",
                    alarmPush: 0,
                    alarmSettingType: '6',
                },
                {
                    name: "终端主电源欠压",
                    alarmPush: 0,
                    alarmSettingType: '7',
                },
                {
                    name: "终端主电源掉电",
                    alarmPush: 0,
                    alarmSettingType: '8',
                },
                {
                    name: "终端LCD或显示器故障",
                    alarmPush: 0,
                    alarmSettingType: '9',
                },
                {
                    name: "TTS模块故障",
                    alarmPush: 0,
                    alarmSettingType: '10',
                },
                {
                    name: "摄像头故障",
                    alarmPush: 0,
                    alarmSettingType: '11',
                },
                {
                    name: "道路运输证IC卡模块故障",
                    alarmPush: 0,
                    alarmSettingType: '12',
                },
                {
                    name: "右转盲区异常报警",
                    alarmPush: 1,
                    alarmSettingType: '17',
                },
                {
                    name: "胎压异常报警",
                    alarmPush: 1,
                    alarmSettingType: '16',
                }
            ]
        },
        {
            name: 'F3高精度报警',
            divId: 'highPrecisionAlarmList',
            sliderNum: 3,
            data: [
                {
                    name: "设备电量报警",
                    alarmPush: 1,
                    alarmSettingType: '18811',
                    right: [
                        {
                            title: '电量报警阈值，单位：%',
                            validate: {
                                rule: {max: 100},
                            }
                        },
                    ]
                },
                {
                    name: "急加速报警",
                    alarmPush: 1,
                    alarmSettingType: '18711',
                    right: [
                        {
                            title: '急加速报警阈值，单位：m/s²',
                            validate: {
                                rule: /^([0-1](\.\d)?$|^2)$|^(2\.[0-5])$/,
                                message: '请输入0-2.5范围的数字且保留一位小数'
                            }
                        },
                    ]
                },
                {
                    name: "急减速报警",
                    alarmPush: 1,
                    alarmSettingType: '18712',
                    right: [
                        {
                            title: '急减速报警阈值，单位：m/s²',
                            validate: {
                                rule: /^([0-1](\.\d)?$|^2)$|^(2\.[0-5])$/,
                                message: '请输入0-2.5范围的数字且保留一位小数'
                            }
                        },
                    ]
                },
                {
                    name: "急转弯报警",
                    alarmPush: 1,
                    alarmSettingType: '18713',
                    right: [
                        {
                            title: '急转弯报警阈值，单位：1°/s²',
                            validate: {
                                rule: {max: 100},
                            }
                        },
                    ]
                },
                {
                    name: "异常移动报警",
                    alarmPush: 1,
                    alarmSettingType: '18714',
                },
                {
                    name: "加速度传感器异常报警",
                    alarmPush: 1,
                    alarmSettingType: '18715',
                },
                {
                    name: "ACC接线异常报警",
                    alarmPush: 1,
                    alarmSettingType: '18716',
                },
                {
                    name: "车辆电池馈电报警",
                    alarmPush: 1,
                    alarmSettingType: '18717',
                },
                {
                    name: "碰撞报警",
                    alarmPush: 1,
                    alarmSettingType: '18719',
                    right: [
                        {
                            title: '碰撞报警阈值，单位：m/s²',
                            validate: {
                                rule: /^[0-4](\.\d)?$|^5$/,
                                message: '请输入0-5范围的数字且保留一位小数'
                            }
                        },
                    ]
                }
            ]
        },
        {
            name: 'F3传感器报警',
            divId: 'sensorAlarmList',
            data: [
                {
                    name: "温度报警",
                    alarmPush: 1,
                    alarmSettingType: '6511',
                },
                {
                    name: "湿度报警",
                    alarmPush: 1,
                    alarmSettingType: '6611',
                },
                {
                    name: "超速报警(F3)",
                    alarmPush: 1,
                    alarmSettingType: '67',
                    right: [
                        {
                            title: '最高速度，单位：km/h',
                            validate: {
                                rule: {max: 255,min: 1},
                            }
                        },
                    ]
                },
                {
                    name: "加漏油报警",
                    alarmPush: 1,
                    alarmSettingType: '6811',
                },
                {
                    name: "胎压报警",
                    alarmPush: 1,
                    alarmSettingType: '14300',
                },
                {
                    name: "反转报警",
                    alarmPush: 1,
                    alarmSettingType: '124',
                },
                {
                    name: "工时报警",
                    alarmPush: 1,
                    alarmSettingType: '13213',
                },
                {
                    name: "载重报警",
                    alarmPush: 1,
                    alarmSettingType: '7012',
                }
            ]
        },
        {
            name: '终端I/O',
            divId: 'deviceIoAlarmList',
            data: [
                // 该项为动态生成项，后端返回数据格式如下
                // {
                //     name: "I/O0",
                //     alarmPush: 0,
                //     alarmSettingName: "I/O0",
                //     alarmSettingType: "14000",
                //     highSignalType: 2,
                //     lowSignalType: 1,
                //     parameterValue: null,
                //     stateOne: "门开11",
                //     stateTwo: "门关11",
                // }
            ]
        },
        {
            name: 'I/O采集1',
            divId: 'ioCollectionOneAlarmList',
            data: []
        },
        {
            name: 'I/O采集2',
            divId: 'ioCollectionTwoAlarmList',
            data: []
        },
        {
            name: '平台报警',
            divId: 'platAlarmList',
            data: [
                {
                    name: "进出区域",
                    alarmPush: 1,
                    alarmSettingType: '7211',
                },
                {
                    name: "进出线路",
                    alarmPush: 1,
                    alarmSettingType: '7311',
                },
                {
                    name: "关键点报警",
                    alarmPush: 1,
                    alarmSettingType: '11911',
                },
                {
                    name: "长时间下线",
                    alarmPush: 0,
                    alarmSettingType: '82',
                    right: [
                        {
                            title: '单位：分钟',
                            validate: {
                                rule: {max: 1440,min: 10},
                            }
                        },
                    ]
                },
                {
                    name: "路线偏离报警",
                    alarmPush: 1,
                    alarmSettingType: '75',
                    right: [
                        {
                            title: '',
                            type: 'option',
                            data: [
                                { value: 0, name: '通用标准' },
                                { value: 2, name: '四川标准' },
                            ]
                        },
                    ]
                },
                {
                    name: "疲劳驾驶",
                    alarmPush: 1,
                    alarmSettingType: '79',
                },
                {
                    name: "24H累计疲劳驾驶",
                    alarmPush: 1,
                    alarmSettingType: '203',
                },
                {
                    name: "异动报警",
                    alarmPush: 1,
                    alarmSettingType: '77',
                    right: [
                        {
                            title: '',
                            type: 'option',
                            value: 0,
                            data: [
                                { value: 0, name: '通用标准' },
                                { value: 1, name: '山西标准' },
                                { value: 2, name: '四川标准' },
                            ],
                        },
                        {
                            title: '禁行时段，单位：小时',
                            type: 'date-hms',
                        },
                        {
                            title: '禁行日期',
                            type: 'date-mm',
                        },
                    ]
                },
                {
                    name: "超速预警",
                    alarmPush: 1,
                    alarmSettingType: '81',
                    right: [
                        {
                            title: '超速预警差值，单位：km/h',
                            validate: {
                                rule: {max: 100},
                            }
                        },
                    ]
                },
                {
                    name: "超速报警",
                    alarmPush: 1,
                    alarmSettingType: '76',
                    right: [
                        {
                            title: '路网限速',
                            type: 'option',
                            value: 0,
                            paramName: 'param1',
                            data: [
                                { value: 0, name: '通用标准' },
                                { value: 1, name: '山西标准' },
                                { value: 2, name: '四川标准' },
                            ],
                            titleRender: function () {
                                return `
                                <label >
                                    <input type="checkbox" id="roadNetSpeedLimit" value="0">
                                    <i class="fa fa-question-circle fa-lg infoTitle" data-toggle="tooltip" data-placement="top" data-original-title="勾选后调用第三方道路限速地图接口获取道路级别的路网限速值，平台根据道路级别可设置平台限速值，报警时以两者中限速值小的为准"></i>
                                    路网限速
                                </label>
                            `
                            }
                        },
                        {
                            title: '最高速度，单位：km/h',
                            paramName: 'param2',
                            validate: {
                                rule: {max: 160},
                            }
                        },
                        {
                            title: '高速限速，单位：km/h',
                            paramName: 'param6',
                            hidden: true,
                            validate: {
                                rule: {max: 160},
                            }
                        },
                        {
                            title: '国道限速，单位：km/h',
                            paramName: 'param7',
                            hidden: true,
                            validate: {
                                rule: {max: 160},
                            }
                        },
                        {
                            title: '省道限速，单位：km/h',
                            paramName: 'param8',
                            hidden: true,
                            validate: {
                                rule: {max: 160},
                            }
                        },
                        {
                            title: '县道限速，单位：km/h',
                            paramName: 'param9',
                            hidden: true,
                            validate: {
                                rule: {max: 160},
                            }
                        },
                        {
                            title: '其他道限速，单位：km/h',
                            paramName: 'param10',
                            hidden: true,
                            validate: {
                                rule: {max: 160},
                            }
                        },

                        {
                            title: '夜间限速百分比，单位：%' ,
                            paramName: 'param3',
                            validate: {
                                rule: {max: 100,min: 1},
                            },
                            titleRender: function () {
                                return `
                                <label style="width: 35%">
                                    <i class="fa fa-question-circle fa-lg infoTitle" data-toggle="tooltip" data-placement="top" data-original-title="夜间速度达到日间限速*设置百分比时触发报警"></i>
                                    夜间限速百分比，单位：%
                                    <span id="overSpeedHide" style="cursor: pointer;position: absolute;right: 0;">显示更多<span class="fa fa-chevron-down" aria-hidden="true"></span></span>
                                </label>
                            `
                            }
                        },
                        {
                            title: '夜间限速时段，单位：小时',
                            paramName: 'param4',
                            type: 'date-hms',
                            hidden: true
                        },
                    ]
                },
                {
                    name: "ACC信号异常报警",
                    alarmPush: 1,
                    alarmSettingType: '150',
                    right: [
                        {
                            title: '异常持续时间，单位：分钟',
                            validate: {
                                rule: {max: 1440,min: 1},
                            }
                        },
                        {
                            title: '最高速度，单位：km/h',
                            validate: {
                                rule: {max: 160,min: 10},
                            }
                        },
                    ]
                },
                {
                    name: "位置信息异常报警",
                    alarmPush: 1,
                    alarmSettingType: '151',
                    right: [
                        {
                            title: '异常持续时间，单位：分钟',
                            validate: {
                                rule: {max: 1440,min: 1},
                            }
                        },
                    ]
                },
                {
                    name: "疑似人为屏蔽报警",
                    alarmPush: -1,
                    alarmSettingType: '209',
                    right: [
                        {
                            title: '点位信息间隔时间，单位：分钟',
                            validate: {
                                rule: {max: 20,min: 5},
                            }
                        },
                        {
                            title: '点位信息间隔里程，单位：km',
                            validate: {
                                rule: {max: 60,min: 5},
                            }
                        },
                        {
                            title: '连续次数阈值，大于等于此阈值时触发报警',
                            validate: {
                                rule: {max: 60,min: 1},
                            }
                        },
                    ]
                },
            ]
        },
    ],
    jt2019Default: [
        {
            name: '预警',
            divId: 'alertList',
            data: [
                {
                    name: '超速预警',
                    alarmPush: 0,
                    alarmSettingType: '13',
                    right: [
                        {
                            title: '超速预警差值，单位：km/h',
                            value: 5,
                            validate: {
                                rule: { max: 255, min: 1 },
                            }
                        },
                    ]
                },
                {
                    name: '疲劳驾驶预警',
                    alarmPush: 0,
                    alarmSettingType: '14',
                    right: [
                        {
                            title: '疲劳驾驶预警差值，单位：秒',
                            value: 1800,
                            validate: {
                                rule: { max: 65535, min: 1 },
                            }
                        },
                    ]
                },
                {
                    name: '侧翻预警',
                    alarmPush: 0,
                    alarmSettingType: '30',
                    right: [
                        {
                            title: '侧翻角度，单位：度',
                            value: 30,
                            validate: {
                                rule: { max: 90, min: 1 },
                            }},
                    ]
                },
                {
                    name: '危险驾驶行为报警',
                    alarmPush: 0,
                    alarmSettingType: '3',
                },
                {
                    name: '碰撞侧翻报警',
                    alarmPush: 0,
                    alarmSettingType: '158',
                    right: [
                        {
                            title: '碰撞时间，单位：毫秒',
                            value: 200,
                            validate: {
                                rule: { max: 1020, min: 1 },
                            }
                        },
                        {
                            title: '碰撞加速度，单位：0.1g，范围：0-79',
                            value: 10,
                            validate: {
                                rule: {max: 79},
                            }
                        },
                    ]
                },
            ]
        },
        {
            name: '驾驶员引起报警',
            divId: 'driverAlarmList',
            data: [
                {
                    name: "紧急报警",
                    alarmPush: 1,
                    alarmSettingType: '0',
                },
                {
                    name: "超速报警（路网）",
                    alarmPush: 1,
                    alarmSettingType: '164',
                },
                {
                    name: "超速报警",
                    alarmPush: 1,
                    alarmSettingType: '1',
                    right: [
                        {
                            title: '最高速度，单位：km/h',
                            value: '100',
                            validate: {
                                rule: {max: 255, min: 1},
                            }
                        },
                        {
                            title: '超速持续时间，单位：秒',
                            value: '10',
                            validate: {
                                rule: {max: 79},
                            }
                        },
                    ]
                },
                {
                    name: "疲劳驾驶",
                    alarmPush: 1,
                    alarmSettingType: '2',
                    right: [
                        {
                            title: '连续驾驶时间门限，单位：秒',
                            value: '14400',
                            validate: {
                                rule: {max: 86400, min: 1},
                            }
                        },
                        {
                            title: '最小休息时间，单位：秒',
                            value: '1200',
                            validate: {
                                rule: {max: 21600, min: 1},
                            }
                        },
                    ]
                },
                {
                    name: "当天累积驾驶超时",
                    alarmPush: 0,
                    alarmSettingType: '18',
                    right: [
                        {
                            title: '当天累计驾驶时间门限，单位：秒',
                            value: '28800',
                            validate: {
                                rule: {max: 86400, min: 1},
                            }
                        },
                    ]
                },
                {
                    name: "超时停车",
                    alarmPush: 0,
                    alarmSettingType: '19',
                    right: [
                        {
                            title: '最长停车时间，单位：秒',
                            value: '600',
                            validate: {
                                rule: {max: 86400, min: 1},
                            }
                        },
                    ]
                },
                {
                    name: "路线偏离报警",
                    alarmPush: 1,
                    alarmSettingType: '23',
                },

                {
                    name: "车道偏离报警",
                    alarmPush: 1,
                    alarmSettingType: '149',
                },
                {
                    name: "前撞报警",
                    alarmPush: 1,
                    alarmSettingType: '148',
                },
                {
                    name: "违规行驶报警",
                    alarmPush: 1,
                    alarmSettingType: '15',
                    right: [
                        {
                            title: '违规行驶时段',
                            type: 'date-hms',
                        },
                    ]
                },
                {
                    name: "进出区域",
                    alarmPush: 1,
                    alarmSettingType: '20',
                },
                {
                    name: "进出线路",
                    alarmPush: 1,
                    alarmSettingType: '21',
                },
                {
                    name: "路段行驶时间不足/过长",
                    alarmPush: 0,
                    alarmSettingType: '22',
                }
            ]
        },
        {
            name: '车辆报警',
            divId: 'vehicleAlarmList',
            data: [
                {
                    name: "车辆VSS故障",
                    alarmPush: 0,
                    alarmSettingType: '24',
                },
                {
                    name: "车辆油量异常",
                    alarmPush: 0,
                    alarmSettingType: '25',
                },
                {
                    name: "车辆被盗",
                    alarmPush: 0,
                    alarmSettingType: '26',
                },
                {
                    name: "车辆非法点火",
                    alarmPush: 1,
                    alarmSettingType: '27',
                },
                {
                    name: "车辆非法位移",
                    alarmPush: 0,
                    alarmSettingType: '28',
                    right: [
                        {
                            title: '车辆非法位移（位移半径），单位：米',
                            value: '20',
                            validate: {
                                rule: {max: 1000, min: 1},
                            }
                        },
                    ]
                }
            ]
        },
        {
            name: '故障报警',
            divId: 'faultAlarmList',
            data: [
                {
                    name: "GNSS模块发生故障",
                    alarmPush: 0,
                    alarmSettingType: '4',
                },
                {
                    name: "GNSS天线未接或被剪断",
                    alarmPush: 0,
                    alarmSettingType: '5',
                },
                {
                    name: "GNSS天线短路",
                    alarmPush: 0,
                    alarmSettingType: '6',
                },
                {
                    name: "终端主电源欠压",
                    alarmPush: 0,
                    alarmSettingType: '7',
                },
                {
                    name: "终端主电源掉电",
                    alarmPush: 0,
                    alarmSettingType: '8',
                },
                {
                    name: "终端LCD或显示器故障",
                    alarmPush: 0,
                    alarmSettingType: '9',
                },
                {
                    name: "TTS模块故障",
                    alarmPush: 0,
                    alarmSettingType: '10',
                },
                {
                    name: "摄像头故障",
                    alarmPush: 0,
                    alarmSettingType: '11',
                },
                {
                    name: "道路运输证IC卡模块故障",
                    alarmPush: 0,
                    alarmSettingType: '12',
                },
                {
                    name: "右转盲区异常报警",
                    alarmPush: 1,
                    alarmSettingType: '17',
                },
                {
                    name: "胎压异常报警",
                    alarmPush: 1,
                    alarmSettingType: '16',
                }
            ]
        },
        {
            name: 'F3高精度报警',
            divId: 'highPrecisionAlarmList',
            sliderNum: 3,
            data: [
                {
                    name: "设备电量报警",
                    alarmPush: 1,
                    alarmSettingType: '18811',
                    right: [
                        {
                            title: '电量报警阈值，单位：%',
                            value: '20',
                            validate: {
                                rule: {max: 100},
                            }
                        },
                    ]
                },
                {
                    name: "急加速报警",
                    alarmPush: 1,
                    alarmSettingType: '18711',
                    right: [
                        {
                            title: '急加速报警阈值，单位：m/s²',
                            value: '0.5',
                            validate: {
                                rule: /^([0-1](\.\d)?$|^2)$|^(2\.[0-5])$/,
                                message: '请输入0-2.5范围的数字且保留一位小数'
                            }
                        },
                    ]
                },
                {
                    name: "急减速报警",
                    alarmPush: 1,
                    alarmSettingType: '18712',
                    right: [
                        {
                            title: '急减速报警阈值，单位：m/s²',
                            value: '0.5',
                            validate: {
                                rule: /^([0-1](\.\d)?$|^2)$|^(2\.[0-5])$/,
                                message: '请输入0-2.5范围的数字且保留一位小数'
                            }
                        },
                    ]
                },
                {
                    name: "急转弯报警",
                    alarmPush: 1,
                    alarmSettingType: '18713',
                    right: [
                        {
                            title: '急转弯报警阈值，单位：1°/s²',
                            value: '10',
                            validate: {
                                rule: {max: 100},
                            }
                        },
                    ]
                },
                {
                    name: "异常移动报警",
                    alarmPush: 1,
                    alarmSettingType: '18714',
                },
                {
                    name: "加速度传感器异常报警",
                    alarmPush: 1,
                    alarmSettingType: '18715',
                },
                {
                    name: "ACC接线异常报警",
                    alarmPush: 1,
                    alarmSettingType: '18716',
                },
                {
                    name: "车辆电池馈电报警",
                    alarmPush: 1,
                    alarmSettingType: '18717',
                },
                {
                    name: "碰撞报警",
                    alarmPush: 1,
                    alarmSettingType: '18719',
                    right: [
                        {
                            title: '碰撞报警阈值，单位：m/s²',
                            value: '1',
                            validate: {
                                rule: /^[0-4](\.\d)?$|^5$/,
                                message: '请输入0-5范围的数字且保留一位小数'
                            }
                        },
                    ]
                }
            ]
        },
        {
            name: 'F3传感器报警',
            divId: 'sensorAlarmList',
            data: [
                {
                    name: "温度报警",
                    alarmPush: 1,
                    alarmSettingType: '6511',
                },
                {
                    name: "湿度报警",
                    alarmPush: 1,
                    alarmSettingType: '6611',
                },
                {
                    name: "超速报警(F3)",
                    alarmPush: 1,
                    alarmSettingType: '67',
                    right: [
                        {
                            title: '最高速度，单位：km/h',
                            value: '120',
                            validate: {
                                rule: {max: 255, min: 1},
                            }
                        },
                    ]
                },
                {
                    name: "加漏油报警",
                    alarmPush: 1,
                    alarmSettingType: '6811',
                },
                {
                    name: "胎压报警",
                    alarmPush: 1,
                    alarmSettingType: '14300',
                },
                {
                    name: "反转报警",
                    alarmPush: 1,
                    alarmSettingType: '124',
                },
                {
                    name: "工时报警",
                    alarmPush: 1,
                    alarmSettingType: '13213',
                },
                {
                    name: "载重报警",
                    alarmPush: 1,
                    alarmSettingType: '7012',
                }
            ]
        },
        {
            name: '终端I/O',
            divId: 'deviceIoAlarmList',
            data: [
                // 该项为动态生成项，后端返回数据格式如下
                // {
                //     name: "I/O0",
                //     alarmPush: 0,
                //     alarmSettingName: "I/O0",
                //     alarmSettingType: "14000",
                //     highSignalType: 2,
                //     lowSignalType: 1,
                //     parameterValue: null,
                //     stateOne: "门开11",
                //     stateTwo: "门关11",
                // }
            ]
        },
        {
            name: 'I/O采集1',
            divId: 'ioCollectionOneAlarmList',
            data: []
        },
        {
            name: 'I/O采集2',
            divId: 'ioCollectionTwoAlarmList',
            data: []
        },
        {
            name: '平台报警',
            divId: 'platAlarmList',
            data: [
                {
                    name: "进出区域",
                    alarmPush: 1,
                    alarmSettingType: '7211',
                },
                {
                    name: "进出线路",
                    alarmPush: 1,
                    alarmSettingType: '7311',
                },
                {
                    name: "关键点报警",
                    alarmPush: 1,
                    alarmSettingType: '11911',
                },
                {
                    name: "长时间下线",
                    alarmPush: 0,
                    alarmSettingType: '82',
                    right: [
                        {
                            title: '单位：分钟',
                            value: '10',
                            validate: {
                                rule: {max: 1440, min: 10},
                            }
                        },
                    ]
                },
                {
                    name: "路线偏离报警",
                    alarmPush: 1,
                    alarmSettingType: '75',
                    right: [
                        {
                            title: '',
                            type: 'option',
                            value: 0,
                            data: [
                                { value: 0, name: '通用标准' },
                                { value: 2, name: '四川标准' },
                            ]
                        },
                    ]
                },
                {
                    name: "疲劳驾驶",
                    alarmPush: 1,
                    alarmSettingType: '79',
                },
                {
                    name: "24H累计疲劳驾驶",
                    alarmPush: 1,
                    alarmSettingType: '203',
                },
                {
                    name: "异动报警",
                    alarmPush: 1,
                    alarmSettingType: '77',
                    right: [
                        {
                            title: '',
                            type: 'option',
                            value: 0,
                            data: [
                                { value: 0, name: '通用标准' },
                                { value: 1, name: '山西标准' },
                                { value: 2, name: '四川标准' },
                            ],
                        },
                        {
                            title: '禁行时段，单位：小时',
                            value: '02:00 -- 05:00',
                            type: 'date-hms',
                        },
                        {
                            title: '禁行日期',
                            type: 'date-mm',
                        },
                    ]
                },
                {
                    name: "超速预警",
                    alarmPush: 1,
                    alarmSettingType: '81',
                    right: [
                        {
                            title: '超速预警差值，单位：km/h',
                            value: '5',
                            validate: {
                                rule: {max: 100},
                            }
                        },
                    ]
                },
                {
                    name: "超速报警",
                    alarmPush: 1,
                    alarmSettingType: '76',
                    right: [
                        {
                            title: '路网限速',
                            type: 'option',
                            value: 0,
                            paramName: 'param1',
                            data: [
                                { value: 0, name: '通用标准' },
                                { value: 1, name: '山西标准' },
                                { value: 2, name: '四川标准' },
                            ],
                            titleRender: function () {
                                return `
                                <label >
                                    <input type="checkbox" id="roadNetSpeedLimit" value="0">
                                    <i class="fa fa-question-circle fa-lg infoTitle" data-toggle="tooltip" data-placement="top" data-original-title="勾选后调用第三方道路限速地图接口获取道路级别的路网限速值，平台根据道路级别可设置平台限速值，报警时以两者中限速值小的为准"></i>
                                    路网限速
                                </label>
                            `
                            }
                        },
                        {
                            title: '最高速度，单位：km/h',
                            paramName: 'param2',
                            value: '120',
                            validate: {
                                rule: {max: 160},
                            }
                        },
                        {
                            title: '高速限速，单位：km/h',
                            paramName: 'param6',
                            hidden: true,
                            validate: {
                                rule: {max: 160},
                            }
                        },
                        {
                            title: '国道限速，单位：km/h',
                            paramName: 'param7',
                            hidden: true,
                            validate: {
                                rule: {max: 160},
                            }
                        },
                        {
                            title: '省道限速，单位：km/h',
                            paramName: 'param8',
                            hidden: true,
                            validate: {
                                rule: {max: 160},
                            }
                        },
                        {
                            title: '县道限速，单位：km/h',
                            paramName: 'param9',
                            hidden: true,
                            validate: {
                                rule: {max: 160},
                            }
                        },
                        {
                            title: '其他道限速，单位：km/h',
                            paramName: 'param10',
                            hidden: true,
                            validate: {
                                rule: {max: 160},
                            }
                        },

                        {
                            title: '夜间限速百分比，单位：%' ,
                            paramName: 'param3',
                            value: '80',
                            validate: {
                                rule: {max: 100, min: 1},
                            },
                            titleRender: function () {
                                return `
                                <label style="width: 35%">
                                    <i class="fa fa-question-circle fa-lg infoTitle" data-toggle="tooltip" data-placement="top" data-original-title="夜间速度达到日间限速*设置百分比时触发报警"></i>
                                    夜间限速百分比，单位：%
                                    <span id="overSpeedHide" style="cursor: pointer;position: absolute;right: 0;">显示更多<span class="fa fa-chevron-down" aria-hidden="true"></span></span>
                                </label>
                            `
                            }
                        },
                        {
                            title: '夜间限速时段，单位：小时',
                            paramName: 'param4',
                            type: 'date-hms',
                            hidden: true
                        },
                    ]
                },
                {
                    name: "ACC信号异常报警",
                    alarmPush: 1,
                    alarmSettingType: '150',
                    right: [
                        {
                            title: '异常持续时间，单位：分钟',
                            value: '30',
                            validate: {
                                rule: {max: 1440, min: 1},
                            }
                        },
                        {
                            title: '最高速度，单位：km/h',
                            value: '30',
                            validate: {
                                rule: {max: 160, min: 10},
                            }
                        },
                    ]
                },
                {
                    name: "位置信息异常报警",
                    alarmPush: 1,
                    alarmSettingType: '151',
                    right: [
                        {
                            title: '异常持续时间，单位：分钟',
                            value: '30',
                            validate: {
                                rule: {max: 1440, min: 1},
                            }
                        },
                    ]
                },
                {
                    name: "疑似人为屏蔽报警",
                    alarmPush: -1,
                    alarmSettingType: '209',
                    right: [
                        {
                            title: '点位信息间隔时间，单位：分钟',
                            value: '10',
                            validate: {
                                rule: {max: 20,min: 5},
                            }
                        },
                        {
                            title: '点位信息间隔里程，单位：km',
                            value: '10',
                            validate: {
                                rule: {max: 60,min: 1},
                            }
                        },
                        {
                            title: '连续次数阈值，大于等于此阈值时触发报警',
                            value: '1',
                            validate: {
                                rule: {max: 60,min: 1},
                            }
                        },
                    ]
                },
            ]
        },
    ],
    // BDTD-SM  5
    bdtd: [
        {
            name: 'BDTD-SM',
            divId: 'peopleAlarmList',
            sliderNum: 3,
            data: [
                {
                    name: "SOS报警",
                    alarmPush: 1,
                    alarmSettingType: '32',
                },
            ]
        },
        {
            name: '平台报警',
            divId: 'peoplePlatAlarmList',
            sliderNum: 3,
            data: [
                {
                    name: "进出区域",
                    alarmPush: 1,
                    alarmSettingType: '11511',
                },
                {
                    name: "进出线路",
                    alarmPush: 1,
                    alarmSettingType: '11611',
                },
                {
                    name: "路线偏离报警",
                    alarmPush: 1,
                    alarmSettingType: '118',
                },
            ]
        }
    ],
    bdtdDefault: [],
    // ASO 9
    aso: [
        {
            name: 'ASO',
            divId: 'asolongAlarmList',
            sliderNum: 3,
            data: [
                {
                    name: "拆机报警",
                    alarmPush: 1,
                    alarmSettingType: '1111',
                },
                {
                    name: "伪基站报警",
                    alarmPush: 1,
                    alarmSettingType: '123',
                },
            ]
        },
        {
            name: '平台报警',
            divId: 'asolongPlatAlarmList',
            sliderNum: 3,
            data: [
                {
                    name: "电量不足报警",
                    alarmPush: 1,
                    alarmSettingType: '113',
                    right: [
                        {
                            title: '%',
                            validate: {
                                rule: {max: 50,min: 10}
                            }
                        },
                    ]
                },
                {
                    name: "风控点报警",
                    alarmPush: 1,
                    alarmSettingType: '114',
                }
            ]
        }
    ],
    asoDefault: [],
    // F3超长待机 10
    f3Long: [
        {
            name: 'F3超长待机',
            divId: 'f3longAlarmList',
            sliderNum: 3,
            data: [
                {
                    name: "车机被拆除",
                    alarmPush: 1,
                    alarmSettingType: '104',
                },
                {
                    name: "RTC异常",
                    alarmPush: 1,
                    alarmSettingType: '106',
                },
                {
                    name: "安装按钮未闭合",
                    alarmPush: 1,
                    alarmSettingType: '107',
                },
                {
                    name: "设备暴露",
                    alarmPush: 1,
                    alarmSettingType: '105',
                },
            ]
        },
        {
            name: '平台报警',
            divId: 'f3longPlatAlarmList',
            sliderNum: 3,
            data: [
                {
                    name: "电量不足报警",
                    alarmPush: 1,
                    alarmSettingType: '108',
                    right: [
                        {
                            title: '%',
                            validate: {
                                rule: {max: 50,min: 10}
                            }
                        },
                    ]
                },
                {
                    name: "风控点报警",
                    alarmPush: 1,
                    alarmSettingType: '109',
                }
            ]
        }
    ],
    f3LongDefault: [],
}