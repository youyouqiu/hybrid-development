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

window.fullTimeRender = function (date) {
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
    return [year.toString(),M,d].join('-') + ' ' + [h,m,s].join(':');
};

window.monthTimeRender = function (date) {
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
    return [M,d].join('-') + ' ' + [h,m,s].join(':');
};

window.timeRender = function (timeInSecond) {
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
    if (day > 0){
        rStr += day.toString() + '天';
        daySet = true;
    }
    if (daySet === true || hour > 0){
        rStr += hour.toString() + '时';
        hourSet = true;
    }
    if (hourSet === true || minute > 0){
        rStr += minute.toString() + '分';
    }
    rStr += timeInSecond.toString() + '秒';
    return rStr;
};

window.statusRender = function(status){
    if (status === '2'){
        return '<span class="color-red">停止</span>'
    } else if (status === '1'){
        return '<span class="color-blue">行驶</span>'
    }
    return '';
};

window.accRender = function(status){
    if (status === '1'){
        return '开'
    } else if (status === '0'){
        return '关'
    }
    return '';
};

window.angleRender = function(angle){
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

window.locationTypeRender = function(locationType){
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

window.locationStatusRender = function(locationType){
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

window.statusTableSpeedRender = function(data,rowIndex,columnIndex,row){
    if (row.sensorFlag){
        return (row.mileageSpeed ? row.mileageSpeed : '-');
    }
    return data;
}

window.statusTableRecorderSpeedRender = function(data,rowIndex,columnIndex,row){
    return row.recorderSpeed ? row.recorderSpeed : '-';
}

window.statusTableMileageRender = function(data,rowIndex,columnIndex,row){
    if (row.sensorFlag){
        return (row.mileageTotal ? row.mileageTotal : '-');
    }
    return data;
}

/**
 * 全部数据表格位置信息渲染函数
 * @param data 位置信息，null代表一开始没有位置
 * @param rowIndex
 * @param columnIndex
 * @param row
 * @returns {*}
 */
window.statusTableHandlePositionRender= function (data, rowIndex, columnIndex, row) {
    if (data === null) {
        return '<a href="#" onclick="trackPlayback.table.handleStatusPositionClick(\'' + row.id + '\')">点击获取位置信息</a>';
    }
    return data;
}

window.alarmFenceTypeRender = function(alarmFenceType){
    if (alarmFenceType == 'zw_m_rectangle') {
        alarmFenceType = "矩形";
    } else if (alarmFenceType == 'zw_m_circle') {
        alarmFenceType = "圆形";
    } else if (alarmFenceType == 'zw_m_line') {
        alarmFenceType = "线";
    } else if (alarmFenceType == 'zw_m_polygon') {
        alarmFenceType = "多边形";
    }else{
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
window.alarmTableStartLocationRender= function (data, rowIndex, columnIndex, row) {
    if (data === null) {
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
window.alarmTableEndLocationRender= function (data, rowIndex, columnIndex, row) {
    if (data === null) {
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
window.stopTableHandleStartPositionRender= function (data, rowIndex, columnIndex, row) {
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
window.stopTableHandleEndPositionRender= function (data, rowIndex, columnIndex, row) {
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
window.runTableHandleStartPositionRender= function (data, rowIndex, columnIndex, row) {
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
window.runTableHandleEndPositionRender= function (data, rowIndex, columnIndex, row) {
    if (data === null) {
        return '<a href="#" onclick="trackPlayback.table.handleRunTableEndLocationClick(\'' + row.id + '\')">点击获取位置信息</a>';
    }
    return data;
}

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
        "render":window.fullTimeRender
    },
    {
        "title": "间隔时间",
        "name": "intervalTime",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
        "render":window.timeRender
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
        "render":window.statusRender
    },
    {
        "title": "ACC状态",
        "name": "acc",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render":window.accRender
    },
    {
        "title": "速度 (km/h)",
        "name": "speed",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render":window.statusTableSpeedRender
    },
    {
        "title": "行车记录仪速度",
        "name": "recorderSpeed",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render":window.statusTableRecorderSpeedRender
    },
    {
        "title": "方向",
        "name": "angle",
        "width": 80,
        "maxWidth": 100,
        "minWidth": 60,
        "resizable": true,
        "render":window.angleRender
    },
    {
        "title": "总里程 (km)",
        "name": "gpsMile",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 60,
        "resizable": true,
        "render":window.statusTableMileageRender
    },
    {
        "title": "定位方式",
        "name": "locationType",
        "width": 100,
        "maxWidth": 300,
        "minWidth": 80,
        "resizable": true,
        "render":window.locationTypeRender
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
        "render":window.locationStatusRender
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
        "render":window.statusTableHandlePositionRender
    },
    {
        "title": "",
        "name": "empty",
        "width": 40
    }
];

// 行驶段数据
window.defaultRunColumns = [
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
        "render":window.timeRender
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
        render:function(data){
            if (isEmpty(data)){
                return '';
            }
            return toFixed(data,1,true);
        }
    },
    {
        "title": "耗油量（L）",
        "name": "consumeOil",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 60,
        "resizable": true,
        render:function(data){
            if (isEmpty(data)){
                return '';
            }
            return toFixed(data,1,true);
        }
    },
    {
        "title": "行驶开始时间",
        "name": "runStartTime",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
        "render":window.fullTimeRender
    },
    {
        "title": "行驶开始位置",
        "name": "runStartLocation",
        "width": 250,
        "maxWidth": 800,
        "minWidth": 100,
        "resizable": true,
        "render":window.runTableHandleStartPositionRender
    },
    {
        "title": "行驶结束时间",
        "name": "runEndTime",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
        "render":window.fullTimeRender
    },
    {
        "title": "行驶结束位置",
        "name": "runEndLocation",
        "width": 250,
        "maxWidth": 800,
        "minWidth": 100,
        "resizable": true,
        "render":window.runTableHandleEndPositionRender
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
window.defaultStopColumns = [
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
        "render":window.timeRender
    },
    {
        "title": "停止开始时间",
        "name": "stopStartTime",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
        "render":window.fullTimeRender
    },
    {
        "title": "停止开始位置",
        "name": "stopStartLocation",
        "width": 250,
        "maxWidth": 800,
        "minWidth": 100,
        "resizable": true,
        "render":window.stopTableHandleStartPositionRender
    },
    {
        "title": "停止结束时间",
        "name": "stopEndTime",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
        "render":window.fullTimeRender
    },
    {
        "title": "停止结束位置",
        "name": "stopEndLocation",
        "width": 250,
        "maxWidth": 800,
        "minWidth": 100,
        "resizable": true,
        "render":window.stopTableHandleEndPositionRender
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
window.defaultAlarmColumns = [
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
        "render":window.alarmTableStartLocationRender
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
        "render":window.alarmTableEndLocationRender
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
window.defaultOBDColumns = [
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
        "render":window.fullTimeRender
    },
    {
        "title": "服务器时间",
        "name": "serviceSystemTime",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render":window.fullTimeRender
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
        "title": "仪表总里程",
        "name": "obdTotalMileage",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "累计里程",
        "name": "obdAccumulatedMileage",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "累计总油耗",
        "name": "obdTotalOilConsumption",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "瞬时油耗",
        "name": "obdInstantOilConsumption",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "仪表车速",
        "name": "obdInstrumentSpeed",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "转速",
        "name": "obdRotationRate",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "机油压力",
        "name": "obdOilPressure",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "电池电压",
        "name": "obdBatteryVoltage",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "水温",
        "name": "obdWaterTemperature",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "车辆油箱油量",
        "name": "obdOilQuantity",
        "width": 130,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "油箱液位高度",
        "name": "obdOilTankLevelHeight",
        "width": 130,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "仪表记录的短途行驶里程",
        "name": "obdShortDistanceMileage",
        "width": 220,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "发/电动机运行时间",
        "name": "obdEngineRunningTime",
        "width": 220,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "扭矩",
        "name": "obdTorque",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "尿素液位",
        "name": "obdUreaLevel",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "刹车状态(脚刹)",
        "name": "obdFootBrakeStatusStr",
        "width": 130,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "远光灯状态",
        "name": "obdHighBeamStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "近光灯状态",
        "name": "obdDippedHeadlightStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "小灯状态",
        "name": "obdSmallLampStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "示宽灯状态",
        "name": "obdIndicatorLampStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "雾灯状态",
        "name": "obdFogLampStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "左转向灯状态",
        "name": "obdLeftTurnLampStatusStr",
        "width": 130,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "右转向灯状态",
        "name": "obdRightTurnLampStatusStr",
        "width": 130,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "应急灯状态",
        "name": "obdEmergencyLampStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "左前门状态",
        "name": "obdLeftFrontDoorStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "右前门状态",
        "name": "obdRightFrontDoorStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "左后门状态",
        "name": "obdLeftRearDoorStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "右后门状态",
        "name": "obdRightRearDoorStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "尾箱门状态",
        "name": "obdTailBoxDoorStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "全车锁",
        "name": "obdFullVehicleLockStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "左前门锁",
        "name": "obdLeftFrontDoorLockStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "右前门锁",
        "name": "obdRightFrontDoorLockStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "左后门锁",
        "name": "obdLeftRearDoorLockStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "右后门锁",
        "name": "obdRightRearDoorLockStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "左前窗状态",
        "name": "obdLeftFrontWindowStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "右前窗状态",
        "name": "obdRightFrontWindowStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "左后窗状态",
        "name": "obdLeftRearWindowStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "右后窗状态",
        "name": "obdRightRearWindowStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "故障信号(ECM)",
        "name": "obdFaultSignalECMStr",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "故障信号(ABS)",
        "name": "obdFaultSignalABSStr",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "故障信号(SRS)",
        "name": "obdFaultSignalSRSStr",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "报警信号(机油)",
        "name": "obdAlarmSignalEngineOilStr",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "报警信号(胎压)",
        "name": "obdAlarmSignalTirePressureStr",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "报警信号(保养)",
        "name": "obdAlarmSignalMaintainStr",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "安全气囊状态",
        "name": "obdSafetyAirBagStatusStr",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "刹车状态(手刹)",
        "name": "obdHandBrakeStatusStr",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "离合状态",
        "name": "obdClutchStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "安全带(驾驶员)",
        "name": "obdSafetyBeltStatusDriverStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "安全带(副驾)",
        "name": "obdSafetyBeltStatusDeputyDrivingStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "ACC信号",
        "name": "obdACCSignalStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "钥匙状态",
        "name": "obdKeyStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "雨刮状态",
        "name": "obdWiperStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "空调开关",
        "name": "obdAirConditionerStatusStr",
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
        "name": "obdAcceleratorPedalStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "方向盘转角状态",
        "name": "obdSteeringWheelAngleStatusStr",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "能源类型",
        "name": "obdEnergyTypeStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "OBD状态(MIL故障灯)",
        "name": "obdMILFaultLampStr",
        "width": 260,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "续航里程",
        "name": "obdEnduranceMileage",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "油量百分比",
        "name": "obdPercentageOfOil",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "瞬时百公里油耗",
        "name": "obdInstant100KmOilConsumption",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "平均百公里油耗",
        "name": "obdAverage100KmOilConsumption",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "发动机进气温度",
        "name": "obdEngineIntakeTemperature",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "车内空调温度",
        "name": "obdAirConditioningTemperature",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "电机温度",
        "name": "obdMotorTemperature",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "控制器温度",
        "name": "obdControllerTemperature",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "三元催化器温度",
        "name": "obdTernaryCatalystTemperature",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "机油温度",
        "name": "obdEngineOilTemperature",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "燃油温度",
        "name": "obdFuelTemperature",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "增压空气温度",
        "name": "obdSuperchargedAirTemperature",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "转速计算车速",
        "name": "obdSpeedByRotationalSpeedCalculation",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "空气流量",
        "name": "obdAirFlowRate",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "进气压力",
        "name": "obdIntakePressure",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "喷油量",
        "name": "obdFuelInjectionQuantity",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "油门踏板相对位置",
        "name": "obdRelativePositionOfThrottlePedal",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "方向盘转角角度",
        "name": "obdSteeringWheelAngle",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "电池剩余电量",
        "name": "obdBatteryRemainingElectricity",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "车辆行程耗油量",
        "name": "obdVehicleTravelFuelConsumption",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "行程内离合次数",
        "name": "obdNumberOfClutchesDuringTravel",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "行程内脚刹次数",
        "name": "obdNumberOfFootBrakesDuringTravel",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "行程内手刹次数",
        "name": "obdNumberOfHandBrakesDuringTravel",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "发动机负荷",
        "name": "obdEngineLoad",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "扭矩百分比",
        "name": "obdTorquePercentage",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "大气压力",
        "name": "obdAtmosphericPressure",
        "width": 120,
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
        "title": "SCR",
        "name": "obdSCR",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "氧化氮超限报警状态",
        "name": "obdNitricOxideOverrunAlarmStatus",
        "width": 180,
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
        "title": "",
        "name": "empty",
        "width": 40
    }
];
