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
        rStr += day.toString() + '???';
        daySet = true;
    }
    if (daySet === true || hour > 0) {
        rStr += hour.toString() + '???';
        hourSet = true;
    }
    if (hourSet === true || minute > 0) {
        rStr += minute.toString() + '???';
    }
    rStr += timeInSecond.toString() + '???';
    return rStr;
};

TrackPlaybackColumn.statusRender = function (status) {
    if (status === '2') {
        return '<span class="color-red">??????</span>'
    } else if (status === '1') {
        return '<span class="color-blue">??????</span>'
    }
    return '';
};

TrackPlaybackColumn.accRender = function (status) {
    if (status === '1') {
        return '<span class="color-blue">???</span>'
    } else if (status === '0') {
        return '<span class="color-red">???</span>'
    }
    return '';
};

TrackPlaybackColumn.angleRender = function (angle) {
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

TrackPlaybackColumn.locationTypeRender = function (locationType) {
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

TrackPlaybackColumn.locationStatusRender = function (locationType) {
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
    return data === 1 ? "??????" : "?????????";
};
/**
 * ??????????????????????????????????????????
 * @param data ???????????????null???????????????????????????
 * @param rowIndex
 * @param columnIndex
 * @param row
 * @returns {*}
 */
TrackPlaybackColumn.statusTableHandlePositionRender = function (data, rowIndex, columnIndex, row) {
    if (data === null) {
        return '<a href="#" onclick="trackPlayback.table.handleStatusPositionClick(\'' + row.id + '\')">????????????????????????</a>';
    }
    return data;
}

TrackPlaybackColumn.alarmFenceTypeRender = function (alarmFenceType) {
    if (alarmFenceType == 'zw_m_rectangle') {
        alarmFenceType = "??????";
    } else if (alarmFenceType == 'zw_m_circle') {
        alarmFenceType = "??????";
    } else if (alarmFenceType == 'zw_m_line') {
        alarmFenceType = "???";
    } else if (alarmFenceType == 'zw_m_polygon') {
        alarmFenceType = "?????????";
    } else {
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
TrackPlaybackColumn.alarmTableStartLocationRender = function (data, rowIndex, columnIndex, row) {
    if (data === null || data === undefined || data === "") {
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
TrackPlaybackColumn.alarmTableEndLocationRender = function (data, rowIndex, columnIndex, row) {
    if (data === null || data === undefined || data === "") {
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
TrackPlaybackColumn.stopTableHandleStartPositionRender = function (data, rowIndex, columnIndex, row) {
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
TrackPlaybackColumn.stopTableHandleEndPositionRender = function (data, rowIndex, columnIndex, row) {
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
TrackPlaybackColumn.runTableHandleStartPositionRender = function (data, rowIndex, columnIndex, row) {
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
TrackPlaybackColumn.runTableHandleEndPositionRender = function (data, rowIndex, columnIndex, row) {
    if (data === null) {
        return '<a href="#" onclick="trackPlayback.table.handleRunTableEndLocationClick(\'' + row.id + '\')">????????????????????????</a>';
    }
    return data;
}

// ????????????
TrackPlaybackColumn.defaultStatusColumns = [
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
        "render": TrackPlaybackColumn.fullTimeRender
    },
    {
        "title": "????????????",
        "name": "intervalTime",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 80,
        "resizable": true,
        "render": TrackPlaybackColumn.timeRender
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
        "render": TrackPlaybackColumn.statusRender
    },
    {
        "title": "ACC??????",
        "name": "acc",
        "width": 100,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.accRender
    },
    {
        "title": "?????? (km/h)",
        "name": "speed",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.statusTableSpeedRender
    },
    {
        "title": "?????????????????????",
        "name": "recorderSpeed",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.statusTableRecorderSpeedRender
    },
    {
        "title": "??????",
        "name": "angle",
        "width": 80,
        "maxWidth": 100,
        "minWidth": 60,
        "resizable": true,
        "render": TrackPlaybackColumn.angleRender
    },
    {
        "title": "????????? (km)",
        "name": "gpsMile",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 60,
        "resizable": true,
        "render": TrackPlaybackColumn.statusTableMileageRender
    },
    {
        "title": "????????????",
        "name": "locationType",
        "width": 100,
        "maxWidth": 300,
        "minWidth": 80,
        "resizable": true,
        "render": TrackPlaybackColumn.locationTypeRender
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
        "render": TrackPlaybackColumn.locationStatusRender
    }, {
        "title": "????????????",
        "name": "reissue",
        "width": 100,
        "maxWidth": 300,
        "minWidth": 80,
        "resizable": true,
        "render": TrackPlaybackColumn.fillStatusRender
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
        "render": TrackPlaybackColumn.statusTableHandlePositionRender
    },
    {
        "title": "",
        "name": "empty",
        "width": 40
    }
];

// ???????????????
TrackPlaybackColumn.defaultRunColumns = [
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
        "render": TrackPlaybackColumn.timeRender
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
        render: function (data) {
            if (isEmpty(data)) {
                return '';
            }
            return toFixed(data, 1, true);
        }
    },
    {
        "title": "????????????L???",
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
        "title": "??????????????????",
        "name": "runStartTime",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.fullTimeRender
    },
    {
        "title": "??????????????????",
        "name": "runStartLocation",
        "width": 250,
        "maxWidth": 800,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.runTableHandleStartPositionRender
    },
    {
        "title": "??????????????????",
        "name": "runEndTime",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.fullTimeRender
    },
    {
        "title": "??????????????????",
        "name": "runEndLocation",
        "width": 250,
        "maxWidth": 800,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.runTableHandleEndPositionRender
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
TrackPlaybackColumn.defaultStopColumns = [
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
        "render": TrackPlaybackColumn.timeRender
    },
    {
        "title": "??????????????????",
        "name": "stopStartTime",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.fullTimeRender
    },
    {
        "title": "??????????????????",
        "name": "stopStartLocation",
        "width": 250,
        "maxWidth": 800,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.stopTableHandleStartPositionRender
    },
    {
        "title": "??????????????????",
        "name": "stopEndTime",
        "width": 200,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.fullTimeRender
    },
    {
        "title": "??????????????????",
        "name": "stopEndLocation",
        "width": 250,
        "maxWidth": 800,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.stopTableHandleEndPositionRender
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
TrackPlaybackColumn.defaultAlarmColumns = [
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
        "render": TrackPlaybackColumn.alarmTableStartLocationRender
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
        "render": TrackPlaybackColumn.alarmTableEndLocationRender
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
TrackPlaybackColumn.defaultOBDColumns = [
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
        "render": TrackPlaybackColumn.fullTimeRender
    },
    {
        "title": "???????????????",
        "name": "serviceSystemTime",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "render": TrackPlaybackColumn.fullTimeRender
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
        "title": "??????????????????km???",
        "name": "obdTotalMileage",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????km???",
        "name": "obdAccumulatedMileage",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????l???",
        "name": "obdTotalOilConsumption",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????l/h???",
        "name": "obdInstantOilConsumption",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????km/h???",
        "name": "obdInstrumentSpeed",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????rpm???",
        "name": "obdRotationRate",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????kPa???",
        "name": "obdOilPressure",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????v???",
        "name": "obdBatteryVoltage",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdWaterTemperature",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????l???",
        "name": "obdOilQuantity",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????mm???",
        "name": "obdOilTankLevelHeight",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????????????????????????????km???",
        "name": "obdShortDistanceMileage",
        "width": 260,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???/????????????????????????h???",
        "name": "obdEngineRunningTime",
        "width": 220,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????n.m???",
        "name": "obdTorque",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????%???",
        "name": "obdUreaLevel",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????(??????)",
        "name": "obdFootBrakeStatus",
        "width": 130,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdHighBeamStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdDippedHeadlightStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdSmallLampStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdIndicatorLampStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdFogLampStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????",
        "name": "obdLeftTurnLampStatus",
        "width": 130,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????",
        "name": "obdRightTurnLampStatus",
        "width": 130,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdEmergencyLampStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdLeftFrontDoorStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdRightFrontDoorStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdLeftRearDoorStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdRightRearDoorStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdTailBoxDoorStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????",
        "name": "obdFullVehicleLock",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdLeftFrontDoorLock",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdRightFrontDoorLock",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdLeftRearDoorLock",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdRightRearDoorLock",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
        "sortDirections": ["ascend", "descend"],
        "sorter": true
    },
    {
        "title": "???????????????",
        "name": "obdLeftFrontWindowStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdRightFrontWindowStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdLeftRearWindowStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????",
        "name": "obdRightRearWindowStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????(ECM)",
        "name": "obdFaultSignalECM",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????(ABS)",
        "name": "obdFaultSignalABS",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????(SRS)",
        "name": "obdFaultSignalSRS",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????(??????)",
        "name": "obdAlarmSignalEngineOil",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????(??????)",
        "name": "obdAlarmSignalTirePressure",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????(??????)",
        "name": "obdAlarmSignalMaintain",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????",
        "name": "obdSafetyAirBagStatus",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????(??????)",
        "name": "obdHandBrakeStatus",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdClutchStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????(?????????)",
        "name": "obdSafetyBeltStatusDriver",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????(??????)",
        "name": "obdSafetyBeltStatusDeputyDriving",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "ACC??????",
        "name": "obdACCSignal",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdKeyStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdWiperStatus",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdAirConditionerStatus",
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
        "name": "obdAcceleratorPedal",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????",
        "name": "obdSteeringWheelAngleStatus",
        "width": 140,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????",
        "name": "obdEnergyType",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "OBD??????(MIL?????????)",
        "name": "obdMILFaultLamp",
        "width": 260,
        "maxWidth": 300,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????%???",
        "name": "obdPercentageOfOil",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????????????????l/100km???",
        "name": "obdInstant100KmOilConsumption",
        "width": 220,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????????????????l/100km???",
        "name": "obdAverage100KmOilConsumption",
        "width": 220,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????????????????",
        "name": "obdEngineIntakeTemperature",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????????????????",
        "name": "obdAirConditioningTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????",
        "name": "obdMotorTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????????????????",
        "name": "obdControllerTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????????????????",
        "name": "obdTernaryCatalystTemperature",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????",
        "name": "obdEngineOilTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????",
        "name": "obdFuelTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????????????????",
        "name": "obdSuperchargedAirTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????km/h???",
        "name": "obdSpeedByRotationalSpeedCalculation",
        "width": 200,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????g/s???",
        "name": "obdAirFlowRate",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????kPa???",
        "name": "obdIntakePressure",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????ml/s???",
        "name": "obdFuelInjectionQuantity",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????????????????%???",
        "name": "obdRelativePositionOfThrottlePedal",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????????????????",
        "name": "obdSteeringWheelAngle",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "?????????????????????%???",
        "name": "obdBatteryRemainingElectricity",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "????????????????????????l???",
        "name": "obdVehicleTravelFuelConsumption",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????????????????",
        "name": "obdNumberOfClutchesDuringTravel",
        "width": 220,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????????????????",
        "name": "obdNumberOfFootBrakesDuringTravel",
        "width": 220,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????????????????",
        "name": "obdNumberOfHandBrakesDuringTravel",
        "width": 220,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????%???",
        "name": "obdEngineLoad",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "??????????????????%???",
        "name": "obdTorquePercentage",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true,
    },
    {
        "title": "???????????????kPa???",
        "name": "obdAtmosphericPressure",
        "width": 160,
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
        "title": "????????????????????????%???",
        "name": "obdFrictionTorque",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "????????????????????????l/h???",
        "name": "obdEngineFuelFlow",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "SCR??????NOx?????????????????????ppm???",
        "name": "obdScrUpNoxOutput",
        "width": 260,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "SCR??????NOx?????????????????????ppm???",
        "name": "obdScrDownNoxOutput",
        "width": 260,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "????????????kg/h???",
        "name": "obdIntakeVolume",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "SCR?????????????????????",
        "name": "obdScrInletTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "SCR?????????????????????",
        "name": "obdScrOutletTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "DPF?????????kPa???",
        "name": "obdDpfDifferentialPressure",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "?????????????????????????????????",
        "name": "obdEngineCoolantTemperature",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "DPF?????????????????????",
        "name": "obdDpfExhaustTemperature",
        "width": 180,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "?????????????????????",
        "name": "obdEngineTorqueMode",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "????????????????????????",
        "name": "obdUreaTankTemperature",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "????????????????????????ml/h???",
        "name": "obdActualUreaInjection",
        "width": 220,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "?????????????????????g???",
        "name": "obdCumulativeUreaConsumption",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "OBD????????????",
        "name": "obdDiagnostic",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "??????????????????",
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
            //     1.Catalyst monitoring Status ????????????????????? - ${dataTypeArr[0] === true ? '??????' : '?????????'}???
            //     2.Heated catalyst monitoring Status ??????????????????????????? - ${dataTypeArr[1] === true ? '??????' : '?????????'}???
            //     3.Evaporative system monitoring Status ?????????????????? - ${dataTypeArr[2] === true ? '??????' : '?????????'}???
            //     4.Secondary air system monitoring Status ???????????????????????? - ${dataTypeArr[3] === true ? '??????' : '?????????'}???
            // `
            return `<a href='javascript:void(0);' onclick='trackPlayback.gjhfObdDiagnosticSupportStateClick(${dataTypeArr})'>??????</a>`
        }
    },
    {
        "title": "??????????????????",
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
            //     1.Catalyst monitoring Status ????????????????????? - ${dataTypeArr[0] === true ? '??????' : '?????????'}???
            //     2.Heated catalyst monitoring Status ??????????????????????????? - ${dataTypeArr[1] === true ? '??????' : '?????????'}???
            //     3.Evaporative system monitoring Status ?????????????????? - ${dataTypeArr[2] === true ? '??????' : '?????????'}???
            //     4.Secondary air system monitoring Status ???????????????????????? - ${dataTypeArr[3] === true ? '??????' : '?????????'}???
            // `
            return `<a href='javascript:void(0);' onclick='trackPlayback.gjhfObdDiagnosticReadyStateClick(${dataTypeArr})'>??????</a>`
        }
    },
    {
        "title": "??????????????????VIN??????",
        "name": "obdVin",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "?????????????????????",
        "name": "obdVersion",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "??????????????????cvn???",
        "name": "obdCvn",
        "width": 160,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "IUPR???",
        "name": "obdIupr",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "???????????????",
        "name": "obdTroubleCodeNum",
        "width": 120,
        "maxWidth": 200,
        "minWidth": 100,
        "resizable": true
    },
    {
        "title": "?????????????????????",
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
