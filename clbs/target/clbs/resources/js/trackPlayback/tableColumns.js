var TrackPlaybackColumn = {};


TrackPlaybackColumn.numberSortFunction = function (a, b) {
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

TrackPlaybackColumn.fullTimeRender = function (date) {
    if (typeof date === 'number') {
        date = new Date(date);
    }
    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    var day = date.getDate();
    var hour = date.getHours();
    var minute = date.getMinutes();
    var second = date.getSeconds();

    var M = month >= 10 ? month.toString(10) : '0' + month.toString(10);
    var d = day >= 10 ? day.toString(10) : '0' + day.toString(10);
    var h = hour >= 10 ? hour.toString(10) : '0' + hour.toString(10);
    var m = minute >= 10 ? minute.toString(10) : '0' + minute.toString(10);
    var s = second >= 10 ? second.toString(10) : '0' + second.toString(10);
    return [year.toString(), M, d].join('-') + ' ' + [h, m, s].join(':');
};

TrackPlaybackColumn.monthTimeRender = function (date) {
    if (typeof date === 'number') {
        date = new Date(date);
    }
    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    var day = date.getDate();
    var hour = date.getHours();
    var minute = date.getMinutes();
    var second = date.getSeconds();

    var M = month >= 10 ? month.toString(10) : '0' + month.toString(10);
    var d = day >= 10 ? day.toString(10) : '0' + day.toString(10);
    var h = hour >= 10 ? hour.toString(10) : '0' + hour.toString(10);
    var m = minute >= 10 ? minute.toString(10) : '0' + minute.toString(10);
    var s = second >= 10 ? second.toString(10) : '0' + second.toString(10);
    return [M, d].join('-') + ' ' + [h, m, s].join(':');
};

TrackPlaybackColumn.timeRender = function (timeInSecond) {
    if (timeInSecond === undefined || timeInSecond === null || isNaN(timeInSecond)) {
        return '--';
    }
    var minuteRule = 60;
    var hourRule = minuteRule * 60;
    var dayRule = hourRule * 24;
    var day = Math.floor(timeInSecond / dayRule);
    timeInSecond -= day * dayRule;
    var hour = Math.floor(timeInSecond / hourRule);
    timeInSecond -= hour * hourRule;
    var minute = Math.floor(timeInSecond / minuteRule);
    timeInSecond -= minute * minuteRule;

    var rStr = '';
    var daySet = false;
    var hourSet = false;
    var minuteset = false;
    if (day > 0) {
        rStr += day.toString() + '天';
        daySet = true;
    }
    if (daySet === true || hour > 0) {
        rStr += hour.toString() + '时';
        hourSet = true;
    }
    if (hourSet === true || minute > 0) {
        rStr += minute.toString() + '分';
    }
    rStr += timeInSecond.toString() + '秒';
    return rStr;
};

TrackPlaybackColumn.statusRender = function (status) {
    if (status === '2') {
        return '<span class="color-red">停止</span>'
    } else if (status === '1') {
        return '<span class="color-blue">行驶</span>'
    }
    return '';
};

TrackPlaybackColumn.accRender = function (status) {
    if (status === '1') {
        return '<span class="color-blue">开</span>'
    } else if (status === '0') {
        return '<span class="color-red">关</span>'
    }
    return '';
};

TrackPlaybackColumn.angleRender = function (angle) {
    var direction;
    if ((angle >= 0 && angle <= 22.5) || (angle > 337.5 && angle <= 360)) {
        direction = '北';
    } else if (angle > 22.5 && angle <= 67.5) {
        direction = '东北';
    } else if (angle > 67.5 && angle <= 112.5) {
        direction = '东';
    } else if (angle > 112.5 && angle <= 157.5) {
        direction = '东南';
    } else if (angle > 157.5 && angle <= 202.5) {
        direction = '南';
    } else if (angle > 202.5 && angle <= 247.5) {
        direction = '西南';
    } else if (angle > 247.5 && angle <= 292.5) {
        direction = '西';
    } else if (angle > 292.5 && angle <= 337.5) {
        direction = '西北';
    } else {
        direction = '';
    }
    return direction;
};

TrackPlaybackColumn.locationTypeRender = function (locationType) {
    var locateMode;
    if (locationType == 0) {
        locateMode = "卫星+基站定位";
    } else if (locationType == 1) {
        locateMode = "基站定位";
    } else if (locationType == 2) {
        locateMode = "卫星定位";
    } else if (locationType == 3) {
        locateMode = "WIFI+基站定位";
    } else if (locationType == 4) {
        locateMode = "卫星+WIFI+基站定位";
    } else {
        locateMode = "-";
    }
    return locateMode;
}

TrackPlaybackColumn.locationStatusRender = function (locationType) {
    var locateMode;
    if (locationType == 0) {
        locateMode = "未定位";
    } else if (locationType == 1) {
        locateMode = "定位";
    } else {
        locateMode = "-";
    }
    return locateMode;
}

TrackPlaybackColumn.statusTableSpeedRender = function (data, rowIndex, columnIndex, row) {
    if (row.sensorFlag) {
        return (row.mileageSpeed ? row.mileageSpeed : '-');
    }
    return data;
}

TrackPlaybackColumn.statusTableRecorderSpeedRender = function (data, rowIndex, columnIndex, row) {
    return row.recorderSpeed ? row.recorderSpeed : '-';
}

TrackPlaybackColumn.statusTableMileageRender = function (data, rowIndex, columnIndex, row) {
    if (row.sensorFlag) {
        return (row.mileageTotal ? row.mileageTotal : '-');
    }
    return data;
}

TrackPlaybackColumn.fillStatusRender = function (data) {
    return data === 1 ? "补传" : "非补传";
};
/**
 * 全部数据表格位置信息渲染函数
 * @param data 位置信息，null代表一开始没有位置
 * @param rowIndex
 * @param columnIndex
 * @param row
 * @returns {*}
 */
TrackPlaybackColumn.statusTableHandlePositionRender = function (data, rowIndex, columnIndex, row) {
    if (data === null) {
        return '<a href="#" onclick="trackPlayback.table.handleStatusPositionClick(\'' + row.id + '\')">点击获取位置信息</a>';
    }
    return data;
}

TrackPlaybackColumn.alarmFenceTypeRender = function (alarmFenceType) {
    if (alarmFenceType == 'zw_m_rectangle') {
        alarmFenceType = "矩形";
    } else if (alarmFenceType == 'zw_m_circle') {
        alarmFenceType = "圆形";
    } else if (alarmFenceType == 'zw_m_line') {
        alarmFenceType = "线";
    } else if (alarmFenceType == 'zw_m_polygon') {
        alarmFenceType = "多边形";
    } else {
        alarmFenceType = '';
    }
    return alarmFenceType;
};

/**
 * 报警数据表格位置信息渲染函数
 * @param data 位置信息，null代表一开始没有位置
 * @param rowIndex
 * @param columnIndex
 * @param row
 * @returns {*}
 */
TrackPlaybackColumn.alarmTableStartLocationRender = function (data, rowIndex, columnIndex, row) {
    if (data === null || data === undefined || data === "") {
        return '<a href="#" onclick="trackPlayback.table.handleStartLocationClick(\'' + row.id + '\')">点击获取位置信息</a>';
    }
    return data;
}

/**
 * 报警数据表格位置信息渲染函数
 * @param data 位置信息，null代表一开始没有位置
 * @param rowIndex
 * @param columnIndex
 * @param row
 * @returns {*}
 */
TrackPlaybackColumn.alarmTableEndLocationRender = function (data, rowIndex, columnIndex, row) {
    if (data === null || data === undefined || data === "") {
        return '<a href="#" onclick="trackPlayback.table.handleEndLocationClick(\'' + row.id + '\')">点击获取位置信息</a>';
    }
    return data;
}

/**
 * 停止段数据表格开始位置信息渲染函数
 * @param data 位置信息，null代表一开始没有位置
 * @param rowIndex
 * @param columnIndex
 * @param row
 * @returns {*}
 */
TrackPlaybackColumn.stopTableHandleStartPositionRender = function (data, rowIndex, columnIndex, row) {
    if (data === null) {
        return '<a href="#" onclick="trackPlayback.table.handleStopTableStartLocationClick(\'' + row.id + '\')">点击获取位置信息</a>';
    }
    return data;
}

/**
 * 停止段数据表格结束位置信息渲染函数
 * @param data 位置信息，null代表一开始没有位置
 * @param rowIndex
 * @param columnIndex
 * @param row
 * @returns {*}
 */
TrackPlaybackColumn.stopTableHandleEndPositionRender = function (data, rowIndex, columnIndex, row) {
    if (data === null) {
        return '<a href="#" onclick="trackPlayback.table.handleStopTableEndLocationClick(\'' + row.id + '\')">点击获取位置信息</a>';
    }
    return data;
}


/**
 * 行驶段数据表格开始位置信息渲染函数
 * @param data 位置信息，null代表一开始没有位置
 * @param rowIndex
 * @param columnIndex
 * @param row
 * @returns {*}
 */
TrackPlaybackColumn.runTableHandleStartPositionRender = function (data, rowIndex, columnIndex, row) {
    if (data === null) {
        return '<a href="#" onclick="trackPlayback.table.handleRunTableStartLocationClick(\'' + row.id + '\')">点击获取位置信息</a>';
    }
    return data;
}

/**
 * 行驶段数据表格结束位置信息渲染函数
 * @param data 位置信息，null代表一开始没有位置
 * @param rowIndex
 * @param columnIndex
 * @param row
 * @returns {*}
 */
TrackPlaybackColumn.runTableHandleEndPositionRender = function (data, rowIndex, columnIndex, row) {
    if (data === null) {
        return '<a href="#" onclick="trackPlayback.table.handleRunTableEndLocationClick(\'' + row.id + '\')">点击获取位置信息</a>';
    }
    return data;
}

// 状态信息
TrackPlaybackColumn.defaultStatusColumns = [
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
        /*"sortDirections": ["ascend", "descend"],
        "sorter": true*/
    },
    {
        "title": "定位时间",
        "name": "vtime",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.fullTimeRender
    },
    {
        "title": "间隔时间",
        "name": "intervalTime",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
        "render": TrackPlaybackColumn.timeRender
    },
    {
        "title": "所属分组",
        "name": "assignmentName",
        "width": 200,
        "maxWidth": 600,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "终端号",
        "name": "deviceNumber",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "终端手机号",
        "name": "simCard",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "状态 <i class='fa fa-question-circle'></i>",
        "name": "status",
        "width": 80,
        "maxWidth": 200,
        "minWidth": 60,
        "resizable": true,
        "render": TrackPlaybackColumn.statusRender
    },
    {
        "title": "ACC状态",
        "name": "acc",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.accRender
    },
    {
        "title": "速度 (km/h)",
        "name": "speed",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.statusTableSpeedRender
    },
    {
        "title": "行车记录仪速度",
        "name": "recorderSpeed",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.statusTableRecorderSpeedRender
    },
    {
        "title": "方向",
        "name": "angle",
        "width": 80,
        "maxWidth": 100,
        "minWidth": 60,
        "resizable": true,
        "render": TrackPlaybackColumn.angleRender
    },
    {
        "title": "总里程 (km)",
        "name": "gpsMile",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 60,
        "resizable": true,
        "render": TrackPlaybackColumn.statusTableMileageRender
    },
    {
        "title": "定位方式",
        "name": "locationType",
        "width": 100,
        "maxWidth": 300,
        "minWidth": 80,
        "resizable": true,
        "render": TrackPlaybackColumn.locationTypeRender
    },
    {
        "title": "卫星颗数",
        "name": "satelliteNumber",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "定位状态",
        "name": "locationStatus",
        "width": 100,
        "maxWidth": 300,
        "minWidth": 80,
        "resizable": true,
        "render": TrackPlaybackColumn.locationStatusRender
    }, {
        "title": "是否补传",
        "name": "reissue",
        "width": 100,
        "maxWidth": 300,
        "minWidth": 80,
        "resizable": true,
        "render": TrackPlaybackColumn.fillStatusRender
    },
    {
        "title": "经度",
        "name": "longtitude",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 60,
        "resizable": true,
    },
    {
        "title": "纬度",
        "name": "latitude",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 60,
        "resizable": true,
    },
    {
        "title": "位置",
        "name": "location",
        "width": 200,
        "maxWidth": 800,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.statusTableHandlePositionRender
    },
    {
        "title": "",
        "name": "empty",
        "width": 40
    }
];

// 行驶段数据
TrackPlaybackColumn.defaultRunColumns = [
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
        /*"sortDirections": ["ascend", "descend"],
        "sorter": true*/
    },
    {
        "title": "所属分组",
        "name": "assignmentName",
        "width": 200,
        "maxWidth": 600,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "行驶时长",
        "name": "runTime",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
        "render": TrackPlaybackColumn.timeRender
    },
    {
        "title": "行驶里程(km)",
        "name": "runMile",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 60,
        "resizable": true,
    },
    {
        "title": "用油量（L）",
        "name": "useOil",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 60,
        "resizable": true,
        render: function (data) {
            if (isEmpty(data)) {
                return '';
            }
            return toFixed(data, 1, true);
        }
    },
    {
        "title": "耗油量（L）",
        "name": "consumeOil",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 60,
        "resizable": true,
        render: function (data) {
            if (isEmpty(data)) {
                return '';
            }
            return toFixed(data, 1, true);
        }
    },
    {
        "title": "行驶开始时间",
        "name": "runStartTime",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.fullTimeRender
    },
    {
        "title": "行驶开始位置",
        "name": "runStartLocation",
        "width": 250,
        "maxWidth": 800,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.runTableHandleStartPositionRender
    },
    {
        "title": "行驶结束时间",
        "name": "runEndTime",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.fullTimeRender
    },
    {
        "title": "行驶结束位置",
        "name": "runEndLocation",
        "width": 250,
        "maxWidth": 800,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.runTableHandleEndPositionRender
    },
    {
        "title": "终端号",
        "name": "deviceNumber",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "终端手机号",
        "name": "simcardNumber",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "",
        "name": "empty",
        "width": 40
    }
];

// 停止段数据
TrackPlaybackColumn.defaultStopColumns = [
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
        /*"sortDirections": ["ascend", "descend"],
        "sorter": true*/
    },
    {
        "title": "所属分组",
        "name": "assignmentName",
        "width": 200,
        "maxWidth": 600,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "停止时长",
        "name": "stopTime",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
        "render": TrackPlaybackColumn.timeRender
    },
    {
        "title": "停止开始时间",
        "name": "stopStartTime",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.fullTimeRender
    },
    {
        "title": "停止开始位置",
        "name": "stopStartLocation",
        "width": 250,
        "maxWidth": 800,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.stopTableHandleStartPositionRender
    },
    {
        "title": "停止结束时间",
        "name": "stopEndTime",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.fullTimeRender
    },
    {
        "title": "停止结束位置",
        "name": "stopEndLocation",
        "width": 250,
        "maxWidth": 800,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.stopTableHandleEndPositionRender
    },
    {
        "title": "终端号",
        "name": "deviceNumber",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "终端手机号",
        "name": "simcardNumber",
        "width": 120,
        "maxWidth": 200,
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
TrackPlaybackColumn.defaultAlarmColumns = [
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
    },
    {
        "title": "所属分组",
        "name": "assignmentName",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "报警类型",
        "name": "alarmType",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "处理状态",
        "name": "status",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
    },
    {
        "title": "处理人",
        "name": "personName",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "报警开始时间",
        "name": "startTime",
        "width": 180,
        "maxWidth": 300,
        "minWidth": 80,
        "resizable": true,
    },
    {
        "title": "报警开始位置",
        "name": "startLocation",
        "width": 200,
        "maxWidth": 700,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.alarmTableStartLocationRender
    },
    {
        "title": "报警开始速度",
        "name": "speed",
        "width": 200,
        "maxWidth": 700,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "行车记录仪速度",
        "name": "recorderSpeed",
        "width": 200,
        "maxWidth": 700,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "报警结束时间",
        "name": "endTime",
        "width": 180,
        "maxWidth": 300,
        "minWidth": 80,
        "resizable": true,

    },
    {
        "title": "报警结束位置",
        "name": "endLocation",
        "width": 200,
        "maxWidth": 700,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.alarmTableEndLocationRender
    },
    {
        "title": "围栏类型",
        "name": "fenceType",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
    },
    {
        "title": "围栏名称",
        "name": "fenceName",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
    },
    {
        "title": "",
        "name": "empty",
        "width": 40
    }
];

// OBD
TrackPlaybackColumn.defaultOBDColumns = [
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
    },
    {
        "title": "定位时间",
        "name": "serviceGpsTime",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.fullTimeRender
    },
    {
        "title": "服务器时间",
        "name": "serviceSystemTime",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.fullTimeRender
    },
    {
        "title": "所属企业",
        "name": "groupName",
        "width": 200,
        "maxWidth": 600,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "仪表总里程（km）",
        "name": "obdTotalMileage",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "累计里程（km）",
        "name": "obdAccumulatedMileage",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "累计总油耗（l）",
        "name": "obdTotalOilConsumption",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "瞬时油耗（l/h）",
        "name": "obdInstantOilConsumption",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "仪表车速（km/h）",
        "name": "obdInstrumentSpeed",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "转速（rpm）",
        "name": "obdRotationRate",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "机油压力（kPa）",
        "name": "obdOilPressure",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "电池电压（v）",
        "name": "obdBatteryVoltage",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "水温（℃）",
        "name": "obdWaterTemperature",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "车辆油箱油量（l）",
        "name": "obdOilQuantity",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "油箱液位高度（mm）",
        "name": "obdOilTankLevelHeight",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "仪表记录的短途行驶里程（km）",
        "name": "obdShortDistanceMileage",
        "width": 260,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "发/电动机运行时间（h）",
        "name": "obdEngineRunningTime",
        "width": 220,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "扭矩（n.m）",
        "name": "obdTorque",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "尿素液位（%）",
        "name": "obdUreaLevel",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "刹车状态(脚刹)",
        "name": "obdFootBrakeStatus",
        "width": 130,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "远光灯状态",
        "name": "obdHighBeamStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "近光灯状态",
        "name": "obdDippedHeadlightStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "小灯状态",
        "name": "obdSmallLampStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "示宽灯状态",
        "name": "obdIndicatorLampStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "雾灯状态",
        "name": "obdFogLampStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "左转向灯状态",
        "name": "obdLeftTurnLampStatus",
        "width": 130,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "右转向灯状态",
        "name": "obdRightTurnLampStatus",
        "width": 130,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "应急灯状态",
        "name": "obdEmergencyLampStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "左前门状态",
        "name": "obdLeftFrontDoorStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "右前门状态",
        "name": "obdRightFrontDoorStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "左后门状态",
        "name": "obdLeftRearDoorStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "右后门状态",
        "name": "obdRightRearDoorStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "尾箱门状态",
        "name": "obdTailBoxDoorStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "全车锁",
        "name": "obdFullVehicleLock",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "左前门锁",
        "name": "obdLeftFrontDoorLock",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "右前门锁",
        "name": "obdRightFrontDoorLock",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "左后门锁",
        "name": "obdLeftRearDoorLock",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
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
    },
    {
        "title": "右前窗状态",
        "name": "obdRightFrontWindowStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "左后窗状态",
        "name": "obdLeftRearWindowStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "右后窗状态",
        "name": "obdRightRearWindowStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "故障信号(ECM)",
        "name": "obdFaultSignalECM",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "故障信号(ABS)",
        "name": "obdFaultSignalABS",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "故障信号(SRS)",
        "name": "obdFaultSignalSRS",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "报警信号(机油)",
        "name": "obdAlarmSignalEngineOil",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "报警信号(胎压)",
        "name": "obdAlarmSignalTirePressure",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "报警信号(保养)",
        "name": "obdAlarmSignalMaintain",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "安全气囊状态",
        "name": "obdSafetyAirBagStatus",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "刹车状态(手刹)",
        "name": "obdHandBrakeStatus",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "离合状态",
        "name": "obdClutchStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "安全带(驾驶员)",
        "name": "obdSafetyBeltStatusDriver",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "安全带(副驾)",
        "name": "obdSafetyBeltStatusDeputyDriving",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "ACC信号",
        "name": "obdACCSignal",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "钥匙状态",
        "name": "obdKeyStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "雨刮状态",
        "name": "obdWiperStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "空调开关",
        "name": "obdAirConditionerStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "档位",
        "name": "obdGearPositionStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "油门踏板",
        "name": "obdAcceleratorPedal",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "方向盘转角状态",
        "name": "obdSteeringWheelAngleStatus",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "能源类型",
        "name": "obdEnergyType",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "OBD状态(MIL故障灯)",
        "name": "obdMILFaultLamp",
        "width": 260,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "油量百分比（%）",
        "name": "obdPercentageOfOil",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "瞬时百公里油耗（l/100km）",
        "name": "obdInstant100KmOilConsumption",
        "width": 220,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "平均百公里油耗（l/100km）",
        "name": "obdAverage100KmOilConsumption",
        "width": 220,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "发动机进气温度（℃）",
        "name": "obdEngineIntakeTemperature",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "车内空调温度（℃）",
        "name": "obdAirConditioningTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "电机温度（℃）",
        "name": "obdMotorTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "控制器温度（℃）",
        "name": "obdControllerTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "三元催化器温度（℃）",
        "name": "obdTernaryCatalystTemperature",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "机油温度（℃）",
        "name": "obdEngineOilTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "燃油温度（℃）",
        "name": "obdFuelTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "增压空气温度（℃）",
        "name": "obdSuperchargedAirTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "转速计算车速（km/h）",
        "name": "obdSpeedByRotationalSpeedCalculation",
        "width": 200,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "空气流量（g/s）",
        "name": "obdAirFlowRate",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "进气压力（kPa）",
        "name": "obdIntakePressure",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "喷油量（ml/s）",
        "name": "obdFuelInjectionQuantity",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "油门踏板相对位置（%）",
        "name": "obdRelativePositionOfThrottlePedal",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "方向盘转角角度（度）",
        "name": "obdSteeringWheelAngle",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "电池剩余电量（%）",
        "name": "obdBatteryRemainingElectricity",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "车辆行程耗油量（l）",
        "name": "obdVehicleTravelFuelConsumption",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "行程内离合次数（次）",
        "name": "obdNumberOfClutchesDuringTravel",
        "width": 220,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "行程内脚刹次数（次）",
        "name": "obdNumberOfFootBrakesDuringTravel",
        "width": 220,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "行程内手刹次数（次）",
        "name": "obdNumberOfHandBrakesDuringTravel",
        "width": 220,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "发动机负荷（%）",
        "name": "obdEngineLoad",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "扭矩百分比（%）",
        "name": "obdTorquePercentage",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "大气压力（kPa）",
        "name": "obdAtmosphericPressure",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "前氧传感器示值",
        "name": "obdFrontOxygenSensorValue",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "后氧传感器示值",
        "name": "obdRearOxygenSensorValue",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
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
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "SCR出口温度（℃）",
        "name": "obdScrOutletTemperature",
        "width": 160,
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
        "width": 180,
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
        "width": 160,
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
            return `<a href='javascript:void(0);' onclick='trackPlayback.gjhfObdDiagnosticSupportStateClick(${dataTypeArr})'>查看</a>`
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
            return `<a href='javascript:void(0);' onclick='trackPlayback.gjhfObdDiagnosticReadyStateClick(${dataTypeArr})'>查看</a>`
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
