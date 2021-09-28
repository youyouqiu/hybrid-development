window.numberSortFunction = function (a, b) {
    var aValue = parseFloat(a);
    var bValue = parseFloat(b);
    if (isNaN(aValue)) {
        aValue = 0;
    }
    if (isNaN(bValue)) {
        bValue = 0;
    }
    return aValue - bValue;
};

// 状态信息
window.defaultStatusColumns = [
    {
        "title": "序号",
        "name": "sequenceNumber",
        "width": 60,
        "isSequence": true
    },
    {
        "title": "监控对象",
        "name": "monitorName",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "定位时间",
        "name": "gpsTime",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "服务器时间",
        "name": "uploadTime",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sorter": true,
        "sortDirections": [
            "ascend",
            "descend"
        ]
    },
    {
        "title": "所属分组",
        "name": "assignmentName",
        "width": 200,
        "maxWidth": 600,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "所属企业",
        "name": "groupName",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "对象类型",
        "name": "vehicleType",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "标识颜色",
        "name": "plateColorName",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "终端号",
        "name": "deviceNumber",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "终端手机号",
        "name": "simcardNumber",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "从业人员",
        "name": "professionalsName",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true,
        render: function (data) {
            if (data === '-') data = '';
            return '<img src="/clbs/resources/img/search-list.svg" class="search-list" onclick="newTableOperation.professionalsDetail(this)"/> ' + data;
        }
    },
    {
        "title": "ACC",
        "name": "acc",
        "width": 80,
        "maxWidth": 200,
        "minWidth": 60,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true,
        render:function (data) {
            if(data == '关'){
                return "<span style='color:#ff0000'>"+ data +"</span>";
            }else{
                return "<span style='color:#00bfff'>"+ data +"</span>";
            }
        }
    },
    {
        "title": "对象状态",
        "name": "objectState",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 200,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "信号状态",
        "name": "signalState",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "速度(km/h)",
        "name": "gpsSpeed",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 60,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "方向",
        "name": "direction",
        "width": 80,
        "maxWidth": 200,
        "minWidth": 60,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "电池电压",
        "name": "batteryVoltage",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "信号强度",
        "name": "signalStrength",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "定位方式",
        "name": "locationType",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "卫星颗数",
        "name": "satellitesNumber",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "终端当日里程(km)",
        "name": "terminalDayMileage",
        "width": 170,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "终端总里程(km)",
        "name": "terminalTotalMileage",
        "width": 170,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    // {
    //     "title": "当日油耗",
    //     "name": "dayOilWear",
    //     "width": 80,
    //     "maxWidth": 200,
    //     "minWidth": 80,
    //     "resizable": true,
    //     "sortDirections":["ascend", "descend"],
    //     "sorter":true
    // },
    // {
    //     "title": "总油耗",
    //     "name": "totalFuelConsumption",
    //     "width": 80,
    //     "maxWidth": 200,
    //     "minWidth": 80,
    //     "resizable": true,
    //     "sortDirections":["ascend", "descend"],
    //     "sorter":true
    // },
    {
        "title": "CAN油量(L)",
        "name": "gpsOil",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "高程",
        "name": "altitude",
        "width": 80,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "路网限速",
        "name": "roadLimitSpeed",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "道路类型",
        "name": "roadType",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "记录仪速度",
        "name": "grapherSpeed",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction,
        "render": function (data) {
            if (!data) {
                return '-';
            }
            return data;
        }
    },
    {
        "title": "经度",
        "name": "longitude",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "纬度",
        "name": "latitude",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "位置",
        "name": "positionDescription",
        "width": 600,
        "maxWidth": 700,
        "minWidth": 200,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true,
        render: function (data, rowIndex, columnIndex, row) {
            if (data === undefined || data === null) {
                return '';
            }
            return '<div style="text-align: left">'+data+'</div>'
        }
    },
    {
        "title": "传感器液体量(L)",
        "name": "oilMass",
        "width": 150,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true,
        render: function (data, rowIndex, columnIndex, row) {
            if (data === undefined || data === null) {
                return '';
            }
            var str = '';
            for (var i = 0; i < data.length; i++) {
                if (data[i].unusual === 1) {
                    str += '异常';
                } else if (data[i].oilMass !== null && data[i].oilMass !== undefined) {
                    str += data[i].oilMass.toString();
                }
                if (i !== data.length - 1) {
                    str += ',';
                }
            }

            return str;
        }
    },
    {
        "title": "传感器当日油耗(L)",
        "name": "dayOilWear",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true,
    },
    {
        "title": "传感器总油耗(L)",
        "name": "oilExpend",
        "width": 150,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true,
        render: function (data, rowIndex, columnIndex, row) {
            if (row.oilExpend === undefined || row.oilExpend === null) {
                return '';
            }
            var str = '';
            row.oilExpend = row.oilExpend.sort(function (x, x2) {
                return x.id - x2.id;
            });
            for (var i = 0; i < row.oilExpend.length; i++) {
                if (data[i].unusual === 1) {
                    str += '异常';
                } else if (row.oilExpend[i].allExpend !== null && row.oilExpend[i].allExpend !== undefined) {
                    str += row.oilExpend[i].allExpend.toString();
                }

                if (i !== row.oilExpend.length - 1) {
                    str += ',';
                }
            }

            return str;
        }
    },
    {
        "title": "电量电压",
        "name": "elecData",
        "width": 150,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true,
        render: function (data, rowIndex, columnIndex, row) {
            if (data === undefined || data === null) {
                return '';
            }
            var str = '';
            if (data.deviceElectricity !== undefined && data.deviceElectricity !== null) { // 设备电量
                if (data.unusual === 1) {
                    str += '异常';
                } else {
                    if ((data.type === 0 && data.deviceElectricity <= 100) || // 电量
                        (data.type !== 0 && data.deviceElectricity <= 6000) // 电压
                    ) {
                        str += data.deviceElectricity.toString() + ',';
                    }
                }

            }
            if (data.drivingElectricity !== undefined && data.drivingElectricity !== null) { // 行车电量
                if (data.unusual === 1) {
                    str += '异常';
                } else {
                    if ((data.type === 0 && data.drivingElectricity <= 100) || // 电量
                        (data.type !== 0 && data.drivingElectricity <= 6000) // 电压
                    ) {
                        str += data.drivingElectricity.toString() + ',';
                    }
                }

            }
            if (data.coldStorageElectricity !== undefined && data.coldStorageElectricity !== null) { // 冷藏电量
                if (data.unusual === 1) {
                    str += '异常';
                } else {
                    if ((data.type === 0 && data.coldStorageElectricity <= 100) || // 电量
                        (data.type !== 0 && data.coldStorageElectricity <= 6000) // 电压
                    ) {
                        str += data.coldStorageElectricity.toString() + ',';
                    }
                }
            }

            if (str.lastIndexOf(',') === str.length - 1) {
                str = str.substr(0, str.length - 1)
            }
            return str;
        }
    },
    {
        "title": "传感器温度(℃)",
        "name": "temperatureSensor",
        "width": 150,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true,
        render: function (data, rowIndex, columnIndex, row) {
            if (data === undefined || data === null) {
                return '';
            }
            var str = '';
            for (var i = 0; i < data.length; i++) {
                if (data[i].unusual === 1) {
                    str += '异常';
                } else if (data[i].temperature !== null && data[i].temperature !== undefined) {
                    str += data[i].temperature.toString();
                }
                if (i !== data.length - 1) {
                    str += ',';
                }
            }

            return str;
        }
    },
    {
        "title": "传感器湿度(%)",
        "name": "temphumiditySensor",
        "width": 150,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true,
        render: function (data, rowIndex, columnIndex, row) {
            if (data === undefined || data === null) {
                return '';
            }
            var str = '';
            for (var i = 0; i < data.length; i++) {
                if (data[i].unusual === 1) {
                    str += '异常';
                } else if (data[i].temperature !== null && data[i].temperature !== undefined) {
                    str += data[i].temperature.toString();
                }
                if (i !== data.length - 1) {
                    str += ',';
                }
            }

            return str;
        }
    },
    {
        "title": "传感器正反转状态",
        "name": "positiveNegative",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true,
        render: function (data, rowIndex, columnIndex, row) {
            if (data === undefined || data === null) {
                return '';
            }
            var str = '';
            for (var i = 0; i < data.length; i++) {
                var status = '';
                if (data[i].spinState === 1) { // 1 停止， 2 运行
                    status = '停转';
                } else if (data[i].spinDirection === 1) { // 1 顺时针，2 逆时针
                    status = '正转';
                } else {
                    status = '反转';
                }
                if (data[i].unusual === 1) {
                    str += '异常';
                } else {
                    str += status;
                }

                if (i !== data.length - 1) {
                    str += ',';
                }
            }

            return str;
        }
    },
    {
        "title": "传感器当日里程(km)",
        "name": "sensorDayMileage",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "传感器总里程(km)",
        "name": "sensorTotalMileage",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "传感器载荷重量(kg)",
        "name": "loadInfos",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true,
        render: function (data, rowIndex, columnIndex, row) {
            if (data === undefined || data === null) {
                return '';
            }

            var str = '';
            for (var i = 0; i < data.length; i++) {
                // var status = '';
                // if (data[i].status === 1){ // 01-空载； 02-满载； 03-超载； 04-装载； 05-卸载；06- 轻载；07-重载
                //     status = '空载';
                // } else if (data[i].status === 2) {
                //     status = '满载';
                // } else if (data[i].status === 3) {
                //     status = '超载';
                // }else if (data[i].status === 4) {
                //     status = '装载';
                // }else if (data[i].status === 5) {
                //     status = '卸载';
                // }else if (data[i].status === 6) {
                //     status = '轻载';
                // }else if (data[i].status === 7) {
                //     status = '重载';
                // }
                // str += status;
                if (data[i].unusual === 1) {
                    str += '异常';
                } else if (data[i].loadWeight !== null && data[i].loadWeight !== undefined) {
                    str += data[i].loadWeight.toString();
                }
                if (i !== data.length - 1) {
                    str += ',';
                }
            }

            return str;
        }
    },
    {
        "title": "传感器工时状态",
        "name": "workHourSensor",
        "width": 150,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true,
        render: function (data, rowIndex, columnIndex, row) {
            if (data === undefined || data === null) {
                return '';
            }
            var str = '';
            for (var i = 0; i < data.length; i++) {
                var status = '';
                if (data[i].workInspectionMethod === 0 || data[i].workInspectionMethod === 1) {
                    if (data[i].workingPosition === 0) {
                        status = '停机';
                    } else if (data[i].workingPosition === 1) {
                        status = '工作';
                    } else if (data[i].workingPosition === 2 && data[i].workInspectionMethod === 1) {
                        status = '待机'
                    }
                } else if (data[i].workInspectionMethod === 2) {
                    if (data[i].workingPosition === 0) {
                        status = '停机';
                    } else if (data[i].workingPosition === 1) {
                        status = '非停机';
                    }
                }
                if (data[i].unusual === 1) {
                    str += '异常';
                } else {
                    str += status;
                }

                if (i !== data.length - 1) {
                    str += ',';
                }
            }

            return str;
        }
    },
    {
        "title": "终端IO状态",
        "name": "terminalIOStatus",
        // "className":"ope-buttons",
        "width": 150,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        // "sortDirections":["ascend", "descend"],
        // "sorter":true
        render: function () {
            return '<img src="/clbs/resources/img/search-list.svg" class="search-list"/>';
        }
    },
    {
        "title": "传感器IO状态",
        "name": "sensorIOStatus",
        // "className":"ope-buttons",
        "width": 150,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        // "sortDirections":["ascend", "descend"],
        // "sorter":true,
        render: function () {
            return '<img src="/clbs/resources/img/search-list.svg" class="search-list" onclick="newTableOperation.sensorIOClick(this)"/>';
        }
    },
    {
        "title": "传感器轮胎气压(bar)",
        "name": "tyreInfos",
        "width": 180,
        "maxWidth": 700,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true,
        render: function (data, rowIndex, columnIndex, row) {
            if (data === undefined || data === null) {
                return '';
            }
            if (data.unusual === 1) {
                return '异常';
            }

            if (data.list === undefined || data.list === null) {
                return '';
            }

            var str = '';
            var list = data.list;

            for (var i = 0; i < list.length; i++) {
                str += list[i].pressure.toString();

                if (i !== list.length - 1) {
                    str += ',';
                }
            }
            return str;
        }
    },
    {
        "title": "车架号",
        "name": "frameNumber",
        "width": 180,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
    },{
        "title": "车架号(终端上传)",
        "name": "frameNumberFromDevice",
        "width": 180,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "",
        "name": "empty",
        "width": 40
    }
];

// 报警
window.defaultAlarmColumns = [{
        "title": "序号",
        "name": "sequenceNumber",
        "width": 60,
        "isSequence": true
    },
    {
        "title": "监控对象",
        "name": "monitorName",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "报警开始时间",
        "name": "alarmTime",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "报警结束时间",
        "name": 'alarmEndTime',
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "处理状态",
        "name": "handleStatus",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true,
        render: function (data, rowIndex, columnIndex, row) {
            return newTableOperation.alarmTableHandleStatusRender(data, rowIndex, columnIndex, row);
        }
    },
    {
        "title": "所属分组",
        "name": "assignmentName",
        "width": 200,
        "maxWidth": 600,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "对象类型",
        "name": "vehicleType",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "标识颜色",
        "name": "plateColorName",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "报警类型",
        "name": "alarmType",
        "width": 200,
        "maxWidth": 700,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "报警开始速度",
        "name": "speed",
        "width": 200,
        "maxWidth": 700,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "行车记录仪速度",
        "name": "recorderSpeed",
        "width": 200,
        "maxWidth": 700,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "驾驶员",
        "name": "professionalsName",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "驾驶证号",
        "name": "drivingLicenseNo",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "围栏类型",
        "name": "defenceType",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "围栏名称",
        "name": "defenceName",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 200,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "路网限速",
        "name": "roadLimitSpeed",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "道路类型",
        "name": "roadType",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "经度",
        "name": "longtitude",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "纬度",
        "name": "latitude",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "报警位置",
        "name": "alarmPosition",
        "width": 400,
        "maxWidth": 700,
        "minWidth": 200,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true,
        render: function (data, rowIndex, columnIndex, row) {
            return newTableOperation.alarmTableHandlePositionRender(data, rowIndex, columnIndex, row);
        }
    },
    {
        "title": "车主",
        "name": "vehicleOwner",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "车主电话",
        "name": "vehicleOwnerPhone",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 160,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "从业人员姓名",
        "name": "alarmProfessionalsName",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 160,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "从业人员手机1",
        "name": "phone",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 160,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "从业人员手机2",
        "name": "phoneTwo",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 160,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "从业人员手机3",
        "name": "phoneThree",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 160,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "",
        "name": "empty",
        "width": 40
    }
];

// 主动安全
window.defaultSecurityColumns = [{
        "title": "序号",
        "name": "sequenceNumber",
        "width": 60,
        "isSequence": true
    },
    {
        "title": "操作",
        "name": "ope",
        "width": 250,
        "className": "ope-buttons",
        render: function (data, rowIndex, columnIndex, row) {
            return '<button type="button" class="editBtn editBtn-security" ' +
                'onclick="dataTableOperation.ifdealEventPop(\'' + row.id + '\', \'' + row.vehicleId + '\', \'' + row.brand + '\', \'' + row.driverName + '\', \'' + row.warningTime + '\',  \'' + row.riskType + '\' , event)">处理</button>' +
                '<button style="margin-left: 10px;" type="button" class="editBtn editBtn-security" onclick="activeSafety.inquiryDriverInfo(\'' + row.id + '\',\'' + row.vehicleId + '\',this,\'' + row.brand + '\',event)">实时视频</button>';
        }
    },
    {
        "title": "监控对象",
        "name": "brand",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 120,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "驾驶员",
        "name": "driverName",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 120,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "驾驶证号",
        "name": "drivingLicenseNo",
        "width": 220,
        "maxWidth": 200,
        "minWidth": 120,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "报警类型",
        "name": "riskType",
        "width": 200,
        "maxWidth": 700,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "报警等级",
        "name": "riskLevel",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true,
        render: function (data) {
            if (!data) {
                return '';
            }
            var d = Number(data);
            switch (d) {
                case 1:
                    return '一般(低)';
                case 2:
                    return '一般(中)';
                case 3:
                    return '一般(高)';
                case 4:
                    return '较重(低)';
                case 5:
                    return '较重(中)';
                case 6:
                    return '较重(高)';
                case 7:
                    return '严重(低)';
                case 8:
                    return '严重(中)';
                case 9:
                    return '严重(高)';
                case 10:
                    return '特重(低)';
                case 11:
                    return '特重(中)';
                case 12:
                    return '特重(高)';
                default:
                    return '';
            }
        }
    },
    {
        "title": "速度",
        "name": "speed",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 60,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction,
        render: function (data) {
            return data + 'km/h';
        }
    },
    {
        "title": "报警位置",
        "name": "address",
        "width": 500,
        "maxWidth": 700,
        "minWidth": 200,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true,
        render: function (data, rowIndex, columnIndex, row) {
            return data ? data : '未定位';
        }
    },
    {
        "title": "开始报警时间",
        "name": "warningTime",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 160,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "结束报警时间",
        "name": "warningEndTime",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 160,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "视频/图片",
        "name": "picVideo",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 160,
        "resizable": true,
        "render": function (data, rowIndex, columnIndex, row) {
            var spanHtml = '';
            if (row.picFlag == 1) {
                spanHtml += '<img class="hasInfo" src="/clbs/resources/img/previewimg-blue.svg" onclick="dataTableOperation.getMediaInfo(\'' + row.id + '\',\'0\',event)" width="20" alt="图片"> ';
            } else {
                spanHtml += '<img class="hasNotInfo" src="/clbs/resources/img/previewimg-grey.svg" width="20" alt="图片"> ';
            }
            if (row.videoFlag == 1) {
                spanHtml += '<img class="hasInfo" src="/clbs/resources/img/video-blue.svg" onclick="dataTableOperation.getMediaInfo(\'' + row.id + '\',\'2\',event)" width="20" alt="视频">';
            } else {
                spanHtml += '<img class="hasNotInfo" src="/clbs/resources/img/video-grey.svg" width="20" alt="视频">';
            }
            return spanHtml;
        }
    },
    {
        "title": "",
        "name": "empty",
        "width": 40
    }
]

// OBD
window.defaultOBDColumns = [{
        "title": "序号",
        "name": "sequenceNumber",
        "width": 60,
        "isSequence": true
    },
    {
        "title": "监控对象",
        "name": "monitorName",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "定位时间",
        "name": "serviceGpsTime",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "服务器时间",
        "name": "serviceSystemTime",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "所属企业",
        "name": "groupName",
        "width": 200,
        "maxWidth": 600,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "仪表总里程（km）",
        "name": "obdTotalMileage",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "累计里程（km）",
        "name": "obdAccumulatedMileage",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "累计总油耗（l）",
        "name": "obdTotalOilConsumption",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "瞬时油耗（l/h）",
        "name": "obdInstantOilConsumption",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "仪表车速（km/h）",
        "name": "obdInstrumentSpeed",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "转速（rpm）",
        "name": "obdRotationRate",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "机油压力（kPa）",
        "name": "obdOilPressure",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "电池电压（v）",
        "name": "obdBatteryVoltage",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "水温（℃）",
        "name": "obdWaterTemperature",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "车辆油箱油量（l）",
        "name": "obdOilQuantity",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "油箱液位高度（mm）",
        "name": "obdOilTankLevelHeight",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "仪表记录的短途行驶里程（km）",
        "name": "obdShortDistanceMileage",
        "width": 260,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "发/电动机运行时间（h）",
        "name": "obdEngineRunningTime",
        "width": 220,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "扭矩（n.m）",
        "name": "obdTorque",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "尿素液位（%）",
        "name": "obdUreaLevel",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "刹车状态(脚刹)",
        "name": "obdFootBrakeStatus",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "远光灯状态",
        "name": "obdHighBeamStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "近光灯状态",
        "name": "obdDippedHeadlightStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "小灯状态",
        "name": "obdSmallLampStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "示宽灯状态",
        "name": "obdIndicatorLampStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "雾灯状态",
        "name": "obdFogLampStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "左转向灯状态",
        "name": "obdLeftTurnLampStatus",
        "width": 130,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "右转向灯状态",
        "name": "obdRightTurnLampStatus",
        "width": 130,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "应急灯状态",
        "name": "obdEmergencyLampStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "左前门状态",
        "name": "obdLeftFrontDoorStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "右前门状态",
        "name": "obdRightFrontDoorStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "左后门状态",
        "name": "obdLeftRearDoorStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "右后门状态",
        "name": "obdRightRearDoorStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "尾箱门状态",
        "name": "obdTailBoxDoorStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "全车锁",
        "name": "obdFullVehicleLock",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "左前门锁",
        "name": "obdLeftFrontDoorLock",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "右前门锁",
        "name": "obdRightFrontDoorLock",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "左后门锁",
        "name": "obdLeftRearDoorLock",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "右后门锁",
        "name": "obdRightRearDoorLock",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "左前窗状态",
        "name": "obdLeftFrontWindowStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "右前窗状态",
        "name": "obdRightFrontWindowStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "左后窗状态",
        "name": "obdLeftRearWindowStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "右后窗状态",
        "name": "obdRightRearWindowStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "故障信号(ECM)",
        "name": "obdFaultSignalECM",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "故障信号(ABS)",
        "name": "obdFaultSignalABS",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "故障信号(SRS)",
        "name": "obdFaultSignalSRS",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "报警信号(机油)",
        "name": "obdAlarmSignalEngineOil",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "报警信号(胎压)",
        "name": "obdAlarmSignalTirePressure",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "报警信号(保养)",
        "name": "obdAlarmSignalMaintain",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "安全气囊状态",
        "name": "obdSafetyAirBagStatus",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "刹车状态(手刹)",
        "name": "obdHandBrakeStatus",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "离合状态",
        "name": "obdClutchStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "安全带(驾驶员)",
        "name": "obdSafetyBeltStatusDriver",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "安全带(副驾)",
        "name": "obdSafetyBeltStatusDeputyDriving",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "ACC信号",
        "name": "obdACCSignal",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "钥匙状态",
        "name": "obdKeyStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "雨刮状态",
        "name": "obdWiperStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "空调开关",
        "name": "obdAirConditionerStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "档位",
        "name": "obdGearPosition",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "油门踏板",
        "name": "obdAcceleratorPedal",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "方向盘转角状态",
        "name": "obdSteeringWheelAngleStatus",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "能源类型",
        "name": "obdEnergyType",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "OBD状态(MIL故障灯)",
        "name": "obdMILFaultLamp",
        "width": 260,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "油量百分比（%）",
        "name": "obdPercentageOfOil",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "瞬时百公里油耗（l/100km）",
        "name": "obdInstant100KmOilConsumption",
        "width": 220,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "平均百公里油耗（l/100km）",
        "name": "obdAverage100KmOilConsumption",
        "width": 220,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "发动机进气温度（℃）",
        "name": "obdEngineIntakeTemperature",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "车内空调温度（℃）",
        "name": "obdAirConditioningTemperature",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "电机温度（℃）",
        "name": "obdMotorTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "控制器温度（℃）",
        "name": "obdControllerTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "三元催化器温度（℃）",
        "name": "obdTernaryCatalystTemperature",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "机油温度（℃）",
        "name": "obdEngineOilTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "燃油温度（℃）",
        "name": "obdFuelTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "增压空气温度（℃）",
        "name": "obdSuperchargedAirTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "转速计算车速（km/h）",
        "name": "obdSpeedByRotationalSpeedCalculation",
        "width": 200,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "空气流量（g/s）",
        "name": "obdAirFlowRate",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "进气压力（kPa）",
        "name": "obdIntakePressure",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "喷油量（ml/s）",
        "name": "obdFuelInjectionQuantity",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "油门踏板相对位置（%）",
        "name": "obdRelativePositionOfThrottlePedal",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "方向盘转角角度（度）",
        "name": "obdSteeringWheelAngle",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "电池剩余电量（%）",
        "name": "obdBatteryRemainingElectricity",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "车辆行程耗油量（l）",
        "name": "obdVehicleTravelFuelConsumption",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "行程内离合次数（次）",
        "name": "obdNumberOfClutchesDuringTravel",
        "width": 220,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "行程内脚刹次数（次）",
        "name": "obdNumberOfFootBrakesDuringTravel",
        "width": 220,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "行程内手刹次数（次）",
        "name": "obdNumberOfHandBrakesDuringTravel",
        "width": 220,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "发动机负荷（%）",
        "name": "obdEngineLoad",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "扭矩百分比（%）",
        "name": "obdTorquePercentage",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "大气压力（kPa）",
        "name": "obdAtmosphericPressure",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "前氧传感器示值",
        "name": "obdFrontOxygenSensorValue",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "后氧传感器示值",
        "name": "obdRearOxygenSensorValue",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": window.numberSortFunction
    },
    {
        "title": "NOx浓度值范围",
        "name": "obdNOxConcentrationRange",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "报警信息",
        "name": "obdAlarmInfo",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "摩擦扭矩百分比（%）",
        "name": "obdFrictionTorque",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "发动机燃料流量（l/h）",
        "name": "obdEngineFuelFlow",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "SCR上游NOx传感器输出值（ppm）",
        "name": "obdScrUpNoxOutput",
        "width": 260,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "SCR下游NOx传感器输出值（ppm）",
        "name": "obdScrDownNoxOutput",
        "width": 260,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "进气量（kg/h）",
        "name": "obdIntakeVolume",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "SCR入口温度（℃）",
        "name": "obdScrInletTemperature",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "SCR出口温度（℃）",
        "name": "obdScrOutletTemperature",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "DPF压差（kPa）",
        "name": "obdDpfDifferentialPressure",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "发动机冷却液温度（℃）",
        "name": "obdEngineCoolantTemperature",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "DPF排气温度（℃）",
        "name": "obdDpfExhaustTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "发动机扭矩模式",
        "name": "obdEngineTorqueMode",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "尿素箱温度（℃）",
        "name": "obdUreaTankTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "实际尿素喷射量（ml/h）",
        "name": "obdActualUreaInjection",
        "width": 220,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "累计尿素消耗（g）",
        "name": "obdCumulativeUreaConsumption",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "OBD诊断协议",
        "name": "obdDiagnostic",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "诊断支持状态",
        "name": "obdDiagnosticSupportState",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render": function (data, rowIndex, columnIndex, row) {
            if (data == null) return '';
            var newData = parseInt(data).toString(2);
            newData = newData.split('').reverse().join('');
            var dataLength = newData.length;
            var dataTypeArr = [false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false];
            for (var i = 0; i < dataLength; i++) {
                if (newData[i] == 1) {
                    dataTypeArr[i] = true;
                }
            }
            // return `
            //     1.Catalyst monitoring Status 催化转化器监控 - ${dataTypeArr[0] === true ? '支持' : '不支持'}；
            //     2.Heated catalyst monitoring Status 加热催化转化器监控 - ${dataTypeArr[1] === true ? '支持' : '不支持'}；
            //     3.Evaporative system monitoring Status 蒸发系统监控 - ${dataTypeArr[2] === true ? '支持' : '不支持'}；
            //     4.Secondary air system monitoring Status 二次空气系统监控 - ${dataTypeArr[3] === true ? '支持' : '不支持'}；
            // `
            return `<a href='javascript:void(0);' onclick='newTableOperation.obdDiagnosticSupportStateClick(${dataTypeArr})'>查看</a>`
        }
    },
    {
        "title": "诊断就绪状态",
        "name": "obdDiagnosticReadyState",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render": function (data, rowIndex, columnIndex, row) {
            if (data == null) return '';
            var newData = parseInt(data).toString(2);
            newData = newData.split('').reverse().join('');
            var dataLength = newData.length;
            var dataTypeArr = [false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false];
            for (var i = 0; i < dataLength; i++) {
                if (newData[i] == 1) {
                    dataTypeArr[i] = true;
                }
            }
            // return `
            //     1.Catalyst monitoring Status 催化转化器监控 - ${dataTypeArr[0] === true ? '支持' : '不支持'}；
            //     2.Heated catalyst monitoring Status 加热催化转化器监控 - ${dataTypeArr[1] === true ? '支持' : '不支持'}；
            //     3.Evaporative system monitoring Status 蒸发系统监控 - ${dataTypeArr[2] === true ? '支持' : '不支持'}；
            //     4.Secondary air system monitoring Status 二次空气系统监控 - ${dataTypeArr[3] === true ? '支持' : '不支持'}；
            // `
            return `<a href='javascript:void(0);' onclick='newTableOperation.obdDiagnosticReadyStateClick(${dataTypeArr})'>查看</a>`
        }
    },
    {
        "title": "车辆识别码（VIN码）",
        "name": "obdVin",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "软件标定识别号",
        "name": "obdVersion",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "标定验证码（cvn）",
        "name": "obdCvn",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "IUPR值",
        "name": "obdIupr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "故障码总数",
        "name": "obdTroubleCodeNum",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "故障码信息列表",
        "name": "obdTroubleCodes",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "",
        "name": "empty",
        "width": 40
    }
];

// 日志记录
window.defaultLogColumns = [{
        "title": "序号",
        "name": "sequenceNumber",
        "width": 60,
        "isSequence": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "操作时间",
        "name": "eventDate",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 160,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "IP地址",
        "name": "ipAddress",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 160,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "操作人",
        "name": "username",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 160,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "监控对象",
        "name": "brand",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 160,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "标识颜色",
        "name": "plateColorStr",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 160,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "操作内容",
        "name": "content",
        "width": 400,
        "maxWidth": 700,
        "minWidth": 300,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "日志来源",
        "name": "logType",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "",
        "name": "empty",
        "width": 40
    }
];

// 气泡弹窗
window.defaultPopupColumns = [{
        "name": "gpsTime",
        "title": "时间",
    },
    {
        "name": "monitorName",
        "title": "监控对象",
    },
    {
        "name": "deviceNumber",
        "title": "终端号",
    },
    {
        "name": "simcardNumber",
        "title": "终端手机号",
    },
    {
        "name": "travelState",
        "title": "行驶状态",
    },
    {
        "name": "gpsSpeed",
        "title": "行驶速度",
    },
    {
        "name": "formattedAddress",
        "title": "位置",
    },
    {
        "name": "groupName",
        "title": "所属企业",
    },
    {
        "name": "assignmentName",
        "title": "所属分组",
    },
    {
        "name": "monitorType",
        "title": "对象类型",
    },
    {
        "name": "acc",
        "title": "ACC状态",
    },
    {
        "name": "todayMileage",
        "title": "当日里程",
    },
    {
        "name": "longitudeAndLatitude",
        "title": "经纬度",
    },
    {
        "name": "allMileage",
        "title": "总里程",
    },
    {
        "name": "direction",
        "title": "方向",
    },
    {
        "name": "grapherSpeed",
        "title": "记录仪速度",
    },
    {
        "name": "altitude",
        "title": "高程",
    },
    {
        "name": "electronicWaybill",
        "title": "电子运单",
    },
    {
        "name": "professionalsName",
        "title": "从业人员",
    },
    {
        "name": "idCardNo",
        "title": "身份证号",
    },
    {
        "name": "identity",
        "title": "从业资格证号",
    },
    {
        "name": "tcb",
        "title": "发证机构",
    }, {
        "name": "operatingState",
        "title": "运营状态",
    }, {
        "name": "alarmType",
        "title": "报警类型",
    }, {
        "name": "roadType",
        "title": "道路类型",
    }, {
        "name": "roadLimitSpeed",
        "title": "路网限速",
    },
    {
        "name": "oilStatus",
        "title": "油路状态",
    }, {
        "name": "circuitStatus",
        "title": "电路状态",
    }, {
        "name": "carDoorStatus",
        "title": "车门状态",
    },
];