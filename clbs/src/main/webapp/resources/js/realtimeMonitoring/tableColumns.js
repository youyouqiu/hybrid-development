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
        rStr += day.toString() + '???';
        daySet = true;
    }
    if (daySet === true || hour > 0){
        rStr += hour.toString() + '???';
        hourSet = true;
    }
    if (hourSet === true || minute > 0){
        rStr += minute.toString() + '???';
    }
    rStr += timeInSecond.toString() + '???';
    return rStr;
};

window.statusRender = function(status){
    if (status === '2'){
        return '<span class="color-red">??????</span>'
    } else if (status === '1'){
        return '<span class="color-blue">??????</span>'
    }
    return '';
};

window.accRender = function(status){
    if (status === '1'){
        return '???'
    } else if (status === '0'){
        return '???'
    }
    return '';
};

window.angleRender = function(angle){
    var direction;
    if ((angle >= 0 && angle <= 22.5) || (angle > 337.5 && angle <= 360)) {
        direction = '???';
    } else if (angle > 22.5 && angle <= 67.5) {
        direction = '??????';
    } else if (angle > 67.5 && angle <= 112.5) {
        direction = '???';
    } else if (angle > 112.5 && angle <= 157.5) {
        direction = '??????';
    } else if (angle > 157.5 && angle <= 202.5) {
        direction = '???';
    } else if (angle > 202.5 && angle <= 247.5) {
        direction = '??????';
    } else if (angle > 247.5 && angle <= 292.5) {
        direction = '???';
    } else if (angle > 292.5 && angle <= 337.5) {
        direction = '??????';
    } else {
        direction = '';
    }
    return direction;
};

window.locationTypeRender = function(locationType){
    var locateMode;
    if (locationType == 0) {
        locateMode = "??????+????????????";
    } else if (locationType == 1) {
        locateMode = "????????????";
    } else if (locationType == 2) {
        locateMode = "????????????";
    } else if (locationType == 3) {
        locateMode = "WIFI+????????????";
    } else if (locationType == 4) {
        locateMode = "??????+WIFI+????????????";
    } else {
        locateMode = "-";
    }
    return locateMode;
}

window.locationStatusRender = function(locationType){
    var locateMode;
    if (locationType == 0) {
        locateMode = "?????????";
    } else if (locationType == 1) {
        locateMode = "??????";
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
 * ??????????????????????????????????????????
 * @param data ???????????????null???????????????????????????
 * @param rowIndex
 * @param columnIndex
 * @param row
 * @returns {*}
 */
window.statusTableHandlePositionRender= function (data, rowIndex, columnIndex, row) {
    if (data === null) {
        return '<a href="#" onclick="trackPlayback.table.handleStatusPositionClick(\'' + row.id + '\')">????????????????????????</a>';
    }
    return data;
}

window.alarmFenceTypeRender = function(alarmFenceType){
    if (alarmFenceType == 'zw_m_rectangle') {
        alarmFenceType = "??????";
    } else if (alarmFenceType == 'zw_m_circle') {
        alarmFenceType = "??????";
    } else if (alarmFenceType == 'zw_m_line') {
        alarmFenceType = "???";
    } else if (alarmFenceType == 'zw_m_polygon') {
        alarmFenceType = "?????????";
    }else{
        alarmFenceType = '';
    }
    return alarmFenceType;
};

/**
 * ??????????????????????????????????????????
 * @param data ???????????????null???????????????????????????
 * @param rowIndex
 * @param columnIndex
 * @param row
 * @returns {*}
 */
window.alarmTableStartLocationRender= function (data, rowIndex, columnIndex, row) {
    if (data === null) {
        return '<a href="#" onclick="trackPlayback.table.handleStartLocationClick(\'' + row.id + '\')">????????????????????????</a>';
    }
    return data;
}

/**
 * ??????????????????????????????????????????
 * @param data ???????????????null???????????????????????????
 * @param rowIndex
 * @param columnIndex
 * @param row
 * @returns {*}
 */
window.alarmTableEndLocationRender= function (data, rowIndex, columnIndex, row) {
    if (data === null) {
        return '<a href="#" onclick="trackPlayback.table.handleEndLocationClick(\'' + row.id + '\')">????????????????????????</a>';
    }
    return data;
}

/**
 * ???????????????????????????????????????????????????
 * @param data ???????????????null???????????????????????????
 * @param rowIndex
 * @param columnIndex
 * @param row
 * @returns {*}
 */
window.stopTableHandleStartPositionRender= function (data, rowIndex, columnIndex, row) {
    if (data === null) {
        return '<a href="#" onclick="trackPlayback.table.handleStopTableStartLocationClick(\'' + row.id + '\')">????????????????????????</a>';
    }
    return data;
}

/**
 * ???????????????????????????????????????????????????
 * @param data ???????????????null???????????????????????????
 * @param rowIndex
 * @param columnIndex
 * @param row
 * @returns {*}
 */
window.stopTableHandleEndPositionRender= function (data, rowIndex, columnIndex, row) {
    if (data === null) {
        return '<a href="#" onclick="trackPlayback.table.handleStopTableEndLocationClick(\'' + row.id + '\')">????????????????????????</a>';
    }
    return data;
}


/**
 * ???????????????????????????????????????????????????
 * @param data ???????????????null???????????????????????????
 * @param rowIndex
 * @param columnIndex
 * @param row
 * @returns {*}
 */
window.runTableHandleStartPositionRender= function (data, rowIndex, columnIndex, row) {
    if (data === null) {
        return '<a href="#" onclick="trackPlayback.table.handleRunTableStartLocationClick(\'' + row.id + '\')">????????????????????????</a>';
    }
    return data;
}

/**
 * ???????????????????????????????????????????????????
 * @param data ???????????????null???????????????????????????
 * @param rowIndex
 * @param columnIndex
 * @param row
 * @returns {*}
 */
window.runTableHandleEndPositionRender= function (data, rowIndex, columnIndex, row) {
    if (data === null) {
        return '<a href="#" onclick="trackPlayback.table.handleRunTableEndLocationClick(\'' + row.id + '\')">????????????????????????</a>';
    }
    return data;
}

// ????????????
window.defaultStatusColumns = [
    {
        "title": "??????",
        "name": "sequenceNumber",
        "width": 60,
        "isSequence": true
    },
    {
        "title": "????????????",
        "name": "monitorName",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        /*"sortDirections": ["ascend", "descend"],
        "sorter": true*/
    },
    {
        "title": "????????????",
        "name": "vtime",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render":window.fullTimeRender
    },
    {
        "title": "????????????",
        "name": "intervalTime",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
        "render":window.timeRender
    },
    {
        "title": "????????????",
        "name": "assignmentName",
        "width": 200,
        "maxWidth": 600,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????",
        "name": "deviceNumber",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "simCard",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????? <i class='fa fa-question-circle'></i>",
        "name": "status",
        "width": 80,
        "maxWidth": 200,
        "minWidth": 60,
        "resizable": true,
        "render":window.statusRender
    },
    {
        "title": "ACC??????",
        "name": "acc",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render":window.accRender
    },
    {
        "title": "?????? (km/h)",
        "name": "speed",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render":window.statusTableSpeedRender
    },
    {
        "title": "?????????????????????",
        "name": "recorderSpeed",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render":window.statusTableRecorderSpeedRender
    },
    {
        "title": "??????",
        "name": "angle",
        "width": 80,
        "maxWidth": 100,
        "minWidth": 60,
        "resizable": true,
        "render":window.angleRender
    },
    {
        "title": "????????? (km)",
        "name": "gpsMile",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 60,
        "resizable": true,
        "render":window.statusTableMileageRender
    },
    {
        "title": "????????????",
        "name": "locationType",
        "width": 100,
        "maxWidth": 300,
        "minWidth": 80,
        "resizable": true,
        "render":window.locationTypeRender
    },
    {
        "title": "????????????",
        "name": "satelliteNumber",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "locationStatus",
        "width": 100,
        "maxWidth": 300,
        "minWidth": 80,
        "resizable": true,
        "render":window.locationStatusRender
    },
    {
        "title": "??????",
        "name": "longtitude",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 60,
        "resizable": true,
    },
    {
        "title": "??????",
        "name": "latitude",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 60,
        "resizable": true,
    },
    {
        "title": "??????",
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

// ???????????????
window.defaultRunColumns = [
    {
        "title": "??????",
        "name": "sequenceNumber",
        "width": 60,
        "isSequence": true
    },
    {
        "title": "????????????",
        "name": "monitorName",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        /*"sortDirections": ["ascend", "descend"],
        "sorter": true*/
    },
    {
        "title": "????????????",
        "name": "assignmentName",
        "width": 200,
        "maxWidth": 600,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "runTime",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
        "render":window.timeRender
    },
    {
        "title": "????????????(km)",
        "name": "runMile",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 60,
        "resizable": true,
    },
    {
        "title": "????????????L???",
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
        "title": "????????????L???",
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
        "title": "??????????????????",
        "name": "runStartTime",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
        "render":window.fullTimeRender
    },
    {
        "title": "??????????????????",
        "name": "runStartLocation",
        "width": 250,
        "maxWidth": 800,
        "minWidth": 100,
        "resizable": true,
        "render":window.runTableHandleStartPositionRender
    },
    {
        "title": "??????????????????",
        "name": "runEndTime",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
        "render":window.fullTimeRender
    },
    {
        "title": "??????????????????",
        "name": "runEndLocation",
        "width": 250,
        "maxWidth": 800,
        "minWidth": 100,
        "resizable": true,
        "render":window.runTableHandleEndPositionRender
    },
    {
        "title": "?????????",
        "name": "deviceNumber",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
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

// ???????????????
window.defaultStopColumns = [
    {
        "title": "??????",
        "name": "sequenceNumber",
        "width": 60,
        "isSequence": true
    },
    {
        "title": "????????????",
        "name": "monitorName",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        /*"sortDirections": ["ascend", "descend"],
        "sorter": true*/
    },
    {
        "title": "????????????",
        "name": "assignmentName",
        "width": 200,
        "maxWidth": 600,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "stopTime",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
        "render":window.timeRender
    },
    {
        "title": "??????????????????",
        "name": "stopStartTime",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
        "render":window.fullTimeRender
    },
    {
        "title": "??????????????????",
        "name": "stopStartLocation",
        "width": 250,
        "maxWidth": 800,
        "minWidth": 100,
        "resizable": true,
        "render":window.stopTableHandleStartPositionRender
    },
    {
        "title": "??????????????????",
        "name": "stopEndTime",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
        "render":window.fullTimeRender
    },
    {
        "title": "??????????????????",
        "name": "stopEndLocation",
        "width": 250,
        "maxWidth": 800,
        "minWidth": 100,
        "resizable": true,
        "render":window.stopTableHandleEndPositionRender
    },
    {
        "title": "?????????",
        "name": "deviceNumber",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
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

// ??????
window.defaultAlarmColumns = [
    {
        "title": "??????",
        "name": "sequenceNumber",
        "width": 60,
        "isSequence": true
    },
    {
        "title": "????????????",
        "name": "monitorName",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "assignmentName",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "alarmType",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "status",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
    },
    {
        "title": "?????????",
        "name": "personName",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????",
        "name": "startTime",
        "width": 180,
        "maxWidth": 300,
        "minWidth": 80,
        "resizable": true,
    },
    {
        "title": "??????????????????",
        "name": "startLocation",
        "width": 200,
        "maxWidth": 700,
        "minWidth": 100,
        "resizable": true,
        "render":window.alarmTableStartLocationRender
    },
    {
        "title": "??????????????????",
        "name": "speed",
        "width": 200,
        "maxWidth": 700,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????",
        "name": "recorderSpeed",
        "width": 200,
        "maxWidth": 700,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????",
        "name": "endTime",
        "width": 180,
        "maxWidth": 300,
        "minWidth": 80,
        "resizable": true,

    },
    {
        "title": "??????????????????",
        "name": "endLocation",
        "width": 200,
        "maxWidth": 700,
        "minWidth": 100,
        "resizable": true,
        "render":window.alarmTableEndLocationRender
    },
    {
        "title": "????????????",
        "name": "fenceType",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
    },
    {
        "title": "????????????",
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
        "title": "??????",
        "name": "sequenceNumber",
        "width": 60,
        "isSequence": true
    },
    {
        "title": "????????????",
        "name": "monitorName",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "serviceGpsTime",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render":window.fullTimeRender
    },
    {
        "title": "???????????????",
        "name": "serviceSystemTime",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render":window.fullTimeRender
    },
    {
        "title": "????????????",
        "name": "groupName",
        "width": 200,
        "maxWidth": 600,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdTotalMileage",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdAccumulatedMileage",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdTotalOilConsumption",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdInstantOilConsumption",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdInstrumentSpeed",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????",
        "name": "obdRotationRate",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdOilPressure",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdBatteryVoltage",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????",
        "name": "obdWaterTemperature",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????",
        "name": "obdOilQuantity",
        "width": 130,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????",
        "name": "obdOilTankLevelHeight",
        "width": 130,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????????????????",
        "name": "obdShortDistanceMileage",
        "width": 220,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???/?????????????????????",
        "name": "obdEngineRunningTime",
        "width": 220,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????",
        "name": "obdTorque",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdUreaLevel",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????(??????)",
        "name": "obdFootBrakeStatusStr",
        "width": 130,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdHighBeamStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdDippedHeadlightStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdSmallLampStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdIndicatorLampStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdFogLampStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????",
        "name": "obdLeftTurnLampStatusStr",
        "width": 130,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????",
        "name": "obdRightTurnLampStatusStr",
        "width": 130,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdEmergencyLampStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdLeftFrontDoorStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdRightFrontDoorStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdLeftRearDoorStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdRightRearDoorStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdTailBoxDoorStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????",
        "name": "obdFullVehicleLockStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdLeftFrontDoorLockStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdRightFrontDoorLockStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdLeftRearDoorLockStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdRightRearDoorLockStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "???????????????",
        "name": "obdLeftFrontWindowStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdRightFrontWindowStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdLeftRearWindowStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdRightRearWindowStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????(ECM)",
        "name": "obdFaultSignalECMStr",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????(ABS)",
        "name": "obdFaultSignalABSStr",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????(SRS)",
        "name": "obdFaultSignalSRSStr",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????(??????)",
        "name": "obdAlarmSignalEngineOilStr",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????(??????)",
        "name": "obdAlarmSignalTirePressureStr",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????(??????)",
        "name": "obdAlarmSignalMaintainStr",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????",
        "name": "obdSafetyAirBagStatusStr",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????(??????)",
        "name": "obdHandBrakeStatusStr",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdClutchStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????(?????????)",
        "name": "obdSafetyBeltStatusDriverStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????(??????)",
        "name": "obdSafetyBeltStatusDeputyDrivingStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "ACC??????",
        "name": "obdACCSignalStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdKeyStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdWiperStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdAirConditionerStatusStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????",
        "name": "obdGearPositionStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdAcceleratorPedalStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????",
        "name": "obdSteeringWheelAngleStatusStr",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdEnergyTypeStr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "OBD??????(MIL?????????)",
        "name": "obdMILFaultLampStr",
        "width": 260,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdEnduranceMileage",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdPercentageOfOil",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????",
        "name": "obdInstant100KmOilConsumption",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????",
        "name": "obdAverage100KmOilConsumption",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????",
        "name": "obdEngineIntakeTemperature",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????",
        "name": "obdAirConditioningTemperature",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdMotorTemperature",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdControllerTemperature",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????",
        "name": "obdTernaryCatalystTemperature",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdEngineOilTemperature",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdFuelTemperature",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????",
        "name": "obdSuperchargedAirTemperature",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????",
        "name": "obdSpeedByRotationalSpeedCalculation",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdAirFlowRate",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdIntakePressure",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????",
        "name": "obdFuelInjectionQuantity",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????????????????",
        "name": "obdRelativePositionOfThrottlePedal",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????",
        "name": "obdSteeringWheelAngle",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????",
        "name": "obdBatteryRemainingElectricity",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????",
        "name": "obdVehicleTravelFuelConsumption",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????",
        "name": "obdNumberOfClutchesDuringTravel",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????",
        "name": "obdNumberOfFootBrakesDuringTravel",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????",
        "name": "obdNumberOfHandBrakesDuringTravel",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdEngineLoad",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdTorquePercentage",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdAtmosphericPressure",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????",
        "name": "obdFrontOxygenSensorValue",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????",
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
        "title": "???????????????????????????",
        "name": "obdNitricOxideOverrunAlarmStatus",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "NOx???????????????",
        "name": "obdNOxConcentrationRange",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
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
