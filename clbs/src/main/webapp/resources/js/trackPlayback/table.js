// 去掉小数点后多余的0
var noZero = function (data) {
    var regexp = /(?:\.0*|(\.\d+?)0+)$/;
    return data.replace(regexp, '$1');
};

var Table = function (options, dependency) {
    this.dependency = dependency;
    this.tableStatus = null;
    this.tableObd = null;
    this.tableRun = null;
    this.tableStop = null;
    this.tableAlarm = null;
    this.defaultStatusColumns = TrackPlaybackColumn.defaultStatusColumns;
    this.defaultAlarmColumns = TrackPlaybackColumn.defaultAlarmColumns;
    this.defaultRunColumns = TrackPlaybackColumn.defaultRunColumns;
    this.defaultStopColumns = TrackPlaybackColumn.defaultStopColumns;
    this.defaultOBDColumns = TrackPlaybackColumn.defaultOBDColumns;
    this.statusColumns = [];
    this.alarmColumns = [];
    this.obdColumns = [];
    this.stopColumns = [];
    this.columnKeys = ['TRACKPLAY_DATA', 'TRACKPLAY_ALARM', 'TRACKPLAY_OBD_LIST', 'TRACKPLAY_RUN', 'TRACKPLAY_STOP'];
    this.tableStatusClickIndex = undefined;
    this.tableRunClickId = undefined;
    this.tableRunClickTimestamp = undefined;
    this.tableObdClickIndex = undefined;
    this.initSetting(false);
    this.lastObdHoverRowId = undefined; // obd表上次悬浮行ID
    this.lastObdHoverField = undefined; // obd表上次悬浮字段
}

/**
 * 初始化用户保存的设置，用于期望显示的列和顺序
 */
Table.prototype.initSetting = function (isUpdate, callback) {
    var dataDependency = this.dependency.get('data');

    $.ajax({
        type: "POST",
        url: "/clbs/core/uum/custom/findCustomColumnInfoByMark",
        dataType: "json",
        async: true,
        data: {marks: this.columnKeys.join(',') + ',TRACKPLAY_SPEED'},
        success: function (data) {
            var statusColumns = [$.extend({}, TrackPlaybackColumn.defaultStatusColumns[0])];
            var alarmColumns = [$.extend({}, TrackPlaybackColumn.defaultAlarmColumns[0])];
            var obdColumns = [$.extend({}, TrackPlaybackColumn.defaultOBDColumns[0])];
            var runColumns = [$.extend({}, TrackPlaybackColumn.defaultRunColumns[0])];
            var stopColumns = [$.extend({}, TrackPlaybackColumn.defaultStopColumns[0])];
            if (data.success && data.obj) {
                // 在添加了线条宽度后，colors变成一个三位数组，前两位是颜色，第三位是宽度
                var colors = data.obj.TRACKPLAY_SPEED.map(function (x) {
                    return parseInt(x.initValue);
                });
                var onlyColors = colors.slice(0, 2);
                var lineSize = colors[2];

                if (lineSize) {
                    dataDependency.setTrackWidth(lineSize);
                }
                dataDependency.setTrackColor(onlyColors);


                var columnArray = [statusColumns, alarmColumns, obdColumns, runColumns, stopColumns];
                var defaultColumnArray = [
                    TrackPlaybackColumn.defaultStatusColumns,
                    TrackPlaybackColumn.defaultAlarmColumns,
                    TrackPlaybackColumn.defaultOBDColumns,
                    TrackPlaybackColumn.defaultRunColumns,
                    TrackPlaybackColumn.defaultStopColumns
                ];
                for (var i = 0; i < this.columnKeys.length; i++) {
                    var key = this.columnKeys[i];
                    var serverColumns = data.obj[key];
                    if (serverColumns !== undefined && serverColumns !== null) {
                        for (var j = 0; j < serverColumns.length; j++) {
                            var serverColumn = serverColumns[j];
                            var jsColumn = defaultColumnArray[i].find(function (x) {
                                return x.name === serverColumn.columnName;
                            });
                            if (jsColumn !== undefined) {
                                var columnCopy = $.extend({}, jsColumn);
                                if (serverColumn.isFix === 1) { //是否是固定列(0:否;1:是)
                                    columnCopy.isFrozen = true;
                                }
                                columnArray[i].push(columnCopy);
                            }
                        }
                    }
                }
                statusColumns.push(TrackPlaybackColumn.defaultStatusColumns[TrackPlaybackColumn.defaultStatusColumns.length - 1]);
                alarmColumns.push(TrackPlaybackColumn.defaultAlarmColumns[TrackPlaybackColumn.defaultAlarmColumns.length - 1]);
                obdColumns.push(TrackPlaybackColumn.defaultOBDColumns[TrackPlaybackColumn.defaultOBDColumns.length - 1]);
                runColumns.push(TrackPlaybackColumn.defaultRunColumns[TrackPlaybackColumn.defaultRunColumns.length - 1]);
                stopColumns.push(TrackPlaybackColumn.defaultStopColumns[TrackPlaybackColumn.defaultStopColumns.length - 1]);
            }
            this.statusColumns = statusColumns;
            this.alarmColumns = alarmColumns;
            this.obdColumns = obdColumns;
            this.runColumns = runColumns;
            this.stopColumns = stopColumns;
            this.initOrUpdateSetting(isUpdate);
            if (typeof callback === 'function') {
                callback();
            }
        }.bind(this)
    });
};

/**
 * 初始化或者更新表格定义
 * @param isUpdate {boolean} 是否是更新
 */
Table.prototype.initOrUpdateSetting = function (isUpdate) {
    // 状态列表
    var statusWidth = 0;
    var statusFreezeColumn = false;
    this.statusColumns.forEach(function (x) {
        statusWidth += x.width;
        if (x.isFrozen && !statusFreezeColumn) {
            statusFreezeColumn = true;
        }
        if (x.isFrozen) {
            x.resizable = false;
        }
    });
    if (statusFreezeColumn) {
        this.statusColumns[0].isFrozen = true;
    }
    var statusOption = {
        tableId: 'tableStatus',
        data: isUpdate ? this.tableStatus.getOption().data : [],
        columns: this.statusColumns,
        freezeHead: true,
        freezeColumn: statusFreezeColumn,
        getUniqueId: 'id',
        width: statusWidth,
        cancelActiveRow: false,
        clickMeansActive: true,
        dblClickMeansLock: false,
        // flashWhenUpdate:true,
        // scrollWhenAppend:true,
        activeScrollDuration: 0,
        striped: false,
        virtualRender: true,
        handleTdClick: this.handleStatusTableClick.bind(this),
        handleThHover: this.handleStatusMouseOver.bind(this),
        // handleTdDblClick: this.handleStatusTableDblClick,
        // handleTdHover: this.handleStatusTableHover
    };

    // 报警列表
    var alarmWidth = 0;
    var alarmFreezeColumn = false;
    this.alarmColumns.forEach(function (x) {
        alarmWidth += x.width;
        if (x.isFrozen && !alarmFreezeColumn) {
            alarmFreezeColumn = true;
        }
        if (x.isFrozen) {
            x.resizable = false;
        }
    });
    if (alarmFreezeColumn) {
        this.alarmColumns[0].isFrozen = true;
    }

    var alarmOption = {
        tableId: 'tableAlarm',
        data: isUpdate ? this.tableAlarm.getOption().data : [],
        columns: this.alarmColumns,
        freezeHead: true,
        freezeColumn: alarmFreezeColumn,
        getUniqueId: 'id',
        width: alarmWidth,
        clickMeansActive: true,
        // flashWhenUpdate: true,
        scrollWhenAppend: true,
        virtualRender: true,
        handleTdClick: this.handleAlarmTableDblClick.bind(this),
        // handleTdHover: this.handleAlarmTableHover
    };

    // OBD列表
    var obdWidth = 0;
    var obdFreezeColumn = false;
    this.obdColumns.forEach(function (x) {
        obdWidth += x.width;
        if (x.isFrozen && !obdFreezeColumn) {
            obdFreezeColumn = true;
        }
        if (x.isFrozen) {
            x.resizable = false;
        }
    });
    if (obdFreezeColumn) {
        this.obdColumns[0].isFrozen = true;
    }
    var obdOption = {
        tableId: 'tableOBD',
        data: isUpdate ? this.tableObd.getOption().data : [],
        columns: this.obdColumns,
        freezeHead: true,
        freezeColumn: obdFreezeColumn,
        getUniqueId: 'id',
        width: obdWidth,
        cancelActiveRow: false,
        clickMeansActive: true,
        dblClickMeansLock: false,
        // flashWhenUpdate: true,
        // scrollWhenAppend: true,
        activeScrollDuration: 0,
        striped: false,
        virtualRender: true,
        handleTdClick: this.handleObdTableClick.bind(this),
        // handleTdDblClick: this.handleObdTableDblClick
        handleTdHover: this.gjhfHandleObdTableHover.bind(this),
    };
    // 行驶段列表
    var runWidth = 0;
    var runFreezeColumn = false;
    this.runColumns.forEach(function (x) {
        runWidth += x.width;
        if (x.isFrozen && !runFreezeColumn) {
            runFreezeColumn = true;
        }
        if (x.isFrozen) {
            x.resizable = false;
        }
    });
    if (runFreezeColumn) {
        this.runColumns[0].isFrozen = true;
    }
    var runOption = {
        tableId: 'tableRun',
        data: isUpdate ? this.tableRun.getOption().data : [],
        columns: this.runColumns,
        freezeHead: true,
        freezeColumn: runFreezeColumn,
        getUniqueId: 'id',
        width: runWidth,
        cancelActiveRow: false,
        clickMeansActive: true,
        dblClickMeansLock: false,
        activeScrollDuration: 0,
        striped: false,
        virtualRender: true,
        handleTdClick: this.handleRunTableClick.bind(this),
        handleTdDblClick: this.handleRunTableDblClick.bind(this)
    };

    // 停止段列表
    var stopWidth = 0;
    var stopFreezeColumn = false;
    this.stopColumns.forEach(function (x) {
        stopWidth += x.width;
        if (x.isFrozen && !stopFreezeColumn) {
            stopFreezeColumn = true;
        }
        if (x.isFrozen) {
            x.resizable = false;
        }
    });
    if (stopFreezeColumn) {
        this.stopColumns[0].isFrozen = true;
    }
    var stopOption = {
        tableId: 'tableStop',
        data: isUpdate ? this.tableStop.getOption().data : [],
        columns: this.stopColumns,
        freezeHead: true,
        freezeColumn: stopFreezeColumn,
        getUniqueId: 'id',
        width: stopWidth,
        cancelActiveRow: false,
        clickMeansActive: true,
        dblClickMeansLock: false,
        activeScrollDuration: 0,
        striped: false,
        virtualRender: true,
        handleTdClick: this.handleStopTableClick.bind(this),
    };
    this.tableStatus = $('#allDataWraper-div').itable(statusOption);
    this.tableObd = $('#obdDataWraper-div').itable(obdOption);
    this.tableAlarm = $('#alarmDataWraper-div').itable(alarmOption);
    this.tableRun = $('#runDataWraper-div').itable(runOption);
    this.tableStop = $('#stopDataWraper-div').itable(stopOption);
    if (isUpdate) {
        this.tableStatus.render();
        this.tableAlarm.render();
        this.tableObd.render();
        this.tableRun.render();
        this.tableStop.render();
    }
    this.setActiveRow();
};

Table.prototype.gjhfHandleObdTableHover = function (rowIndex, cellIndex, $td) {
    var field = $td.data('field');
    var rowId = $td.attr('id').split('_')[1] + '_' + $td.attr('id').split('_')[2];
    var rowInTable = this.tableObd.findRow(this.tableObd.getState().data, this.tableObd.getOption(), rowId);
    if (rowInTable === null) {
        return;
    }
    var row = rowInTable[0];
    var content = '';
    switch (field) {
        case 'obdVin':
            content = this.obdDataString(row.obdVin); // 车辆识别码（VIN码）
            break;
        case 'obdVersion':
            content = this.obdDataString(row.obdVersion); // 软件标定识别号
            break;
        case 'obdCvn':
            content = this.obdDataString(row.obdCvn); // 标定验证码（cvn）
            break;
        case 'obdIupr':
            content = this.obdDataString(row.obdIupr); // IUPR值
            break;
        case 'obdTroubleCodes':
            content = this.obdDataString(row.obdTroubleCodes); // 故障码信息列表
            break;
        default:
            return;
    }
    if (content.length > 0) {
        this.lastObdHoverRowId = rowId;
        this.lastObdHoverField = field;
        $td.justToolsTip({
            animation: "moveInTop",
            width: "auto",
            contents: content,
            gravity: 'top',
            events: 'mouseover',
            onRemove: function () {
                this.lastObdHoverRowId = undefined;
                this.lastObdHoverField = undefined;
            }
        });
    }
}

Table.prototype.obdDataString = function (data) {
    if (data == null) return;
    return data;
},

    Table.prototype.handleStatusTableClick = function (rowId, cellIndex, $td) {
        var rawRow = this.tableStatus.findRow(this.tableStatus.getState().data, this.tableStatus.getOption(), rowId);
        if (rawRow === null) {
            return;
        }
        var index = rawRow[1];
        var row = rawRow[0];

        this.tableStatusClickIndex = index;

        var dataDependency = this.dependency.get('data');
        if (dataDependency.getIsPlaying()) {
            this.dependency.get('map').pause();
        }
        dataDependency.setPlayIndex(index);

        var showStopPoint = dataDependency.getShowStopPoint();
        if (showStopPoint && row && row.status === '2') {
            // 停止点，地图图标需要弹跳
            this.dependency.get('map').danceStopMarker(index);
        }
    }

Table.prototype.handleObdTableClick = function (rowId, cellIndex, $td) {
    var rawRow = this.tableObd.findRow(this.tableObd.getState().data, this.tableObd.getOption(), rowId);
    if (rawRow === null) {
        return;
    }
    var index = rawRow[1];

    this.tableObdClickIndex = index;

    var dataDependency = this.dependency.get('data');
    if (dataDependency.getIsPlaying()) {
        this.dependency.get('map').pause();
    }
    dataDependency.setPlayIndex(index);
}

Table.prototype.handleRunTableClick = function (rowId, cellIndex, $td) {
    var mapDependency = this.dependency.get('map');

    var lastTimeStamp = this.tableRunClickTimestamp;
    this.tableRunClickTimestamp = new Date();
    if (lastTimeStamp && (this.tableRunClickTimestamp.getTime() - lastTimeStamp.getTime()) < 600) {
        return;
    }
    if (rowId === this.tableRunClickId) {
        // this.tableRun.setActiveRow(this.tableRunClickId);
        this.tableRunClickId = undefined;
        mapDependency.removeHighlightRunSegment();
        var table = this.tableRun;
        var clickId = this.tableRunClickId;
        requestAnimationFrame(function () {
            table.setActiveRow(clickId);
        });
        return;
    }

    var rawRow = this.tableRun.findRow(this.tableRun.getState().data, this.tableRun.getOption(), rowId);
    if (rawRow === null) {
        return;
    }
    var row = rawRow[0];
    this.tableRunClickId = rowId;

    if (row) {
        // 停止点，地图图标需要弹跳
        mapDependency.highlightRunSegment(row.originStartIndex, row.originEndIndex, row.runMile);
    }
}

Table.prototype.handleRunTableDblClick = function (rowId, cellIndex, $td) {
    var mapDependency = this.dependency.get('map');
    var dataDependency = this.dependency.get('data');

    var isPlaying = dataDependency.getIsPlaying();

    if (isPlaying) {
        mapDependency.pause('runTableDblClick');
    }
    setTimeout(function () {
        mapDependency.focusRunSegment();
    }, 800);
}

Table.prototype.removeHighlightRunSegment = function () {
    var mapDependency = this.dependency.get('map');
    var dataDependency = this.dependency.get('data');

    var isPlaying = trackPlayback.data.getIsPlaying();

    if (isPlaying && this.tableRunClickId) {
        this.tableRunClickId = undefined;
        this.tableRun.setActiveRow(undefined);
        mapDependency.removeHighlightRunSegment();
        return;
    }
}

Table.prototype.handleStopTableClick = function (rowId, cellIndex, $td) {
    var rawRow = this.tableStop.findRow(this.tableStop.getState().data, this.tableStop.getOption(), rowId);
    if (rawRow === null) {
        return;
    }
    var row = rawRow[0];

    var dataDependency = this.dependency.get('data');
    var showStopPoint = dataDependency.getShowStopPoint();

    if (showStopPoint && row) {
        // 停止点，地图图标需要弹跳
        this.dependency.get('map').danceStopMarker(row.originStartIndex);
    }
}

Table.prototype.handleAlarmTableDblClick = function (rowId, cellIndex, $td) {
    var rawRow = this.tableAlarm.findRow(this.tableAlarm.getState().data, this.tableAlarm.getOption(), rowId);
    if (rawRow === null) {
        return;
    }
    var index = rawRow[1];
    this.dependency.get('map').danceAlarmMarker(index);
}

Table.prototype.setActiveRow = function () {
    var dataDependency = this.dependency.get('data');

    var positions = dataDependency.getPositions();
    var playIndex = dataDependency.getPlayIndex();

    if (!positions || playIndex === null || !positions[playIndex]) {
        return;
    }

    var position = positions[playIndex];
    var rowId = this.getRowId(position);
    var tabIndex = dataDependency.getTableTabIndex();

    if (tabIndex === 1) {
        if (playIndex !== this.tableStatusClickIndex
            && this.tableStatus.getState().lastClickRowId !== rowId) {
            this.tableStatus.setActiveRow(rowId);
        }
    } else if (tabIndex === 2) {
        if (playIndex !== this.tableObdClickIndex
            && this.tableObd.getState().lastClickRowId !== rowId) {
            this.tableObd.setActiveRow(rowId);
        }
    }
}

// 切换表格tab
Table.prototype.setActiveTab = function () {
    var tabIndex = this.dependency.get('data').getTableTabIndex();
    if (tabIndex === 1 && !$('#allDataWraper').hasClass('active')) {
        $('#myTab a:eq(0)').tab('show')
    } else if (tabIndex === 2 && !$('#obdDataWraper').hasClass('active')) {
        $('#myTab a:eq(1)').tab('show')
    } else if (tabIndex === 3 && !$('#runDataWraper').hasClass('active')) {
        $('#myTab a:eq(2)').tab('show')
    } else if (tabIndex === 4 && !$('#stopDataWraper').hasClass('active')) {
        $('#myTab a:eq(3)').tab('show')
    } else if (tabIndex === 5 && !$('#alarmDataWraper').hasClass('active')) {
        $('#myTab a:eq(4)').tab('show')
    }
    this.setActiveRow();
}

Table.prototype.replaceAlarmTable = function () {
    var dataDependency = this.dependency.get('data');
    var alarmData = dataDependency.getAlarmData();

    var tableData = [];
    if (alarmData !== null) {
        console.log(alarmData)
        for (var i = 0; i < alarmData.length; i++) {
            var item = alarmData[i];

            tableData.push({
                sequenceNumber: 0,
                id: item.id,
                monitorName: item.monitorName,
                assignmentName: item.assignmentName,
                alarmType: item.description,
                status: item.alarmStatus,
                personName: item.personName,
                startTime: item.startTime,
                startLocation: item.startLocation,
                speed: item.speed ? noZero(item.speed.toString()) : "-",
                recorderSpeed: item.recorderSpeed ? noZero(item.recorderSpeed.toString()) : "-",
                alarmStartLocation: item.alarmStartLocation,
                endTime: item.endTime,
                endLocation: item.endLocation,
                alarmEndLocation: item.alarmEndLocation,
                fenceType: item.fenceType,
                fenceName: item.fenceName
            })
        }
    }
    if (this.tableAlarm) this.tableAlarm.replaceOptionData(tableData);
}

Table.prototype.replaceRunTable = function () {
    var dataDependency = this.dependency.get('data');
    var runFragmentData = dataDependency.getRunFragmentData();
    if (runFragmentData !== null) {
        if (this.tableRun) this.tableRun.replaceOptionData(runFragmentData);
    } else {
        if (this.tableRun) this.tableRun.replaceOptionData([]);
    }
}

Table.prototype.replaceStopTable = function () {
    var dataDependency = this.dependency.get('data');
    var stopFragmentData = dataDependency.getStopFragmentData();
    if (stopFragmentData !== null) {
        if (this.tableStop) this.tableStop.replaceOptionData(stopFragmentData);
    } else {
        if (this.tableStop) this.tableStop.replaceOptionData([]);
    }
}

Table.prototype.replaceObdTable = function () {
    var dataDependency = this.dependency.get('data');
    var obdData = dataDependency.getObdData();
    var positions = dataDependency.getPositions();
    var positionsTmp = dataDependency.getPositionsTmp();
    var allOrRunData = dataDependency.getAllOrRunData();

    var tableData = [];
    var positionsArray = allOrRunData === 'run' ? positionsTmp : positions;
    if (obdData !== null) {
        for (var i = 0; i < obdData.length; i++) {
            var item = obdData[i];

            var obdRow = {
                sequenceNumber: 0,
                id: this.getRowId(item),
                monitorName: item.plateNumber,
                serviceGpsTime: item.vtime * 1000,
                serviceSystemTime: parseInt(item.uploadtime) * 1000,
                groupName: item.groupName,
                drivingState: positionsArray[i].drivingState
            };
            for (var j = 0; j < this.obdColumns.length; j++) {
                var key = this.obdColumns[j].name;
                if (obdRow[key] === undefined) {
                    obdRow[key] = item[key];
                }
            }
            tableData.push(obdRow);
        }
        if (allOrRunData === 'run') {
            tableData = trackPlayback.filterRunData(tableData);
        }
        if (this.tableObd) this.tableObd.replaceOptionData(tableData);
        this.setActiveRow();
    } else {
        if (this.tableObd) this.tableObd.replaceOptionData([]);
    }
}

Table.prototype.replaceTable = function () {
    var dataDependency = this.dependency.get('data');
    var group = dataDependency.getGroup();
    var sensorFlag = dataDependency.getSensorFlag();
    var positions = dataDependency.getPositions();
    var tableData = [];

    if (positions === null) {
        this.dependency.get('dragBar').setDelta(0);
        $('#scalingBtn').removeClass('fa-chevron-down').addClass('fa-chevron-up');
        if (this.tableStatus) this.tableStatus.replaceOptionData([]);
    } else {
        for (var i = 0; i < positions.length; i++) {
            var item = positions[i];
            var prevItem = positions[i - 1];
            tableData.push({
                sequenceNumber: 0,
                id: this.getRowId(item),
                monitorName: item.plateNumber,
                vtime: item.vtime * 1000, // vtime 单位为秒，需要转为毫秒
                intervalTime: prevItem === undefined ? null : (item.vtime - prevItem.vtime),
                assignmentName: group,
                deviceNumber: item.deviceNumber,
                simCard: item.simCard,
                status: item.drivingState,
                acc: item.acc,
                speed: item.speed,
                recorderSpeed: item.recorderSpeed,
                angle: item.angle,
                gpsMile: item.gpsMile,
                locationType: item.locationType,
                satelliteNumber: item.satelliteNumber,
                locationStatus: item.locationStatus,
                longtitude: item.longtitude,
                latitude: item.latitude,
                mileageSpeed: item.mileageSpeed,
                mileageTotal: item.mileageTotal,
                sensorFlag: sensorFlag,
                location: null,
                reissue: item.reissue
            })
        }
        if (this.tableStatus) this.tableStatus.replaceOptionData(tableData);
        this.setActiveRow();
        this.dependency.get('dragBar').setDelta(-250);
        $('#scalingBtn').removeClass('fa-chevron-up').addClass('fa-chevron-down');
    }
    dataDependency.setTableTabIndex(1);
    dataDependency.setObdData(null);
    dataDependency.setAlarmData(null);
    dataDependency.setStopFragmentData(null);
    dataDependency.setRunFragmentData(null);
}

Table.prototype.getRowId = function (item) {
    return item.plateNumber + '_' + item.vtime;
}

Table.prototype.getRestTableData = function () {
    var dataDependency = this.dependency.get('data');
    var tabIndex = dataDependency.getTableTabIndex();
    var vehicleId = dataDependency.getVid();
    var startTime = dataDependency.getStartTime();
    var endTime = dataDependency.getEndTime();
    var sensorFlag = dataDependency.getSensorFlag();
    var sensorArray = dataDependency.getValidSensorFlag();
    var sensorFlag = null;
    if (sensorArray) {
        sensorFlag = sensorArray[dataDependency.getValidClickedIndex()];
    }
    if (tabIndex === 2) { // obd
        var obdData = dataDependency.getObdData();
        if (obdData === null) {
            json_ajax('POST', '/clbs/v/monitoring/getMonitorObdDate', 'json', true, {
                "monitorId": vehicleId,
                "startTime": startTime,
                "endTime": endTime,
                "sensorFlag": sensorFlag
            }, function (data) {
                if (data.success) {
                    var list = JSON.parse(ungzip(data.msg));
                    var isObdSet = list.isObdSet;
                    if (isObdSet === 'yes') {
                        console.log(list.result, 'list.result');
                        dataDependency.setObdData(list.result);
                    } else {
                        dataDependency.setObdData(null);
                    }
                } else {
                    layer.msg(data.msg);
                }
            });
        }
    } else if (tabIndex === 3) { // 行驶段数据
        var runFragmentData = dataDependency.getRunFragmentData();
        if (runFragmentData === null) {
            var positions = dataDependency.getPositions();
            var positionsTmp = dataDependency.getPositionsTmp();
            var stopTypeDict = dataDependency.getStopTypeDict(); // 行驶段，停止段数据是同时计算得出的，行驶key为：1，停止：2
            var allOrRunData = dataDependency.getAllOrRunData();
            var group = dataDependency.getGroup();

            var positionsArray = allOrRunData === 'run' ? positionsTmp : positions;

            var runFragment = [];

            // console.log(stopTypeDict)

            if (stopTypeDict['1']) {

                var segments = stopTypeDict['1'].segment;
                var segment, originStartIndex, originEndIndex, item, endItem;

                for (var i = 0; i < segments.length; i++) {
                    segment = segments[i];
                    item = positionsArray[segment.originStartIndex];
                    endItem = positionsArray[segment.originEndIndex];

                    var runMile = '-';
                    if (sensorFlag && !isEmpty(endItem.mileageTotal) && !isEmpty(item.mileageTotal)) {
                        runMile = endItem.mileageTotal - item.mileageTotal;
                    } else if (!isEmpty(endItem.gpsMile) && !isEmpty(item.gpsMile)) {
                        runMile = endItem.gpsMile - item.gpsMile;
                    }
                    if (runMile !== '-') {
                        runMile = toFixed(runMile, 1, true);
                    }

                    runFragment.push({
                        sequenceNumber: 0,
                        originStartIndex: segment.originStartIndex,
                        originEndIndex: segment.originEndIndex,
                        id: this.getRowId(item),
                        monitorName: segment.monitorName,
                        assignmentName: group,
                        runTime: segment.timeLength,
                        runMile: runMile,
                        useOil: segment.useOil,
                        consumeOil: segment.consumeOil,
                        runStartTime: segment.originStartTime, // 毫秒
                        runStartLocation: null,
                        startLongtitude: segment.startLongtitude,
                        startLatitude: segment.startLatitude,
                        runEndTime: segment.originEndTime, // 毫秒
                        runEndLocation: null,
                        endLongtitude: segment.endLongtitude,
                        endLatitude: segment.endLatitude,
                        deviceNumber: segment.deviceNumber,
                        simcardNumber: segment.simcardNumber,
                    });
                }
            }
            dataDependency.setRunFragmentData(runFragment);
        }

    } else if (tabIndex === 4) { // 停止段数据
        var stopFragmentData = dataDependency.getStopFragmentData();
        if (stopFragmentData === null) {
            var positions = dataDependency.getPositions();
            var positionsTmp = dataDependency.getPositionsTmp();
            var stopTypeDict = dataDependency.getStopTypeDict();
            var allOrRunData = dataDependency.getAllOrRunData();
            var group = dataDependency.getGroup();

            var positionsArray = allOrRunData === 'run' ? positionsTmp : positions;

            var stopFragment = [];

            if (stopTypeDict['2']) {

                var segments = stopTypeDict['2'].segment;
                var segment, originStartIndex, originEndIndex, item, endItem;

                for (var i = 0; i < segments.length; i++) {
                    segment = segments[i];
                    item = positionsArray[segment.originStartIndex];
                    endItem = positionsArray[segment.originEndIndex];

                    stopFragment.push({
                        sequenceNumber: 0,
                        originStartIndex: segment.originStartIndex,
                        originEndIndex: segment.originEndIndex,
                        id: this.getRowId(item),
                        monitorName: segment.monitorName,
                        assignmentName: group,
                        stopTime: segment.timeLength,
                        stopStartTime: segment.originStartTime, // 毫秒
                        stopStartLocation: null,
                        startLongtitude: segment.startLongtitude,
                        startLatitude: segment.startLatitude,
                        stopEndTime: segment.originEndTime, // 毫秒
                        stopEndLocation: null,
                        endLongtitude: segment.endLongtitude,
                        endLatitude: segment.endLatitude,
                        deviceNumber: segment.deviceNumber,
                        simcardNumber: segment.simcardNumber,
                    });
                }
            }
            dataDependency.setStopFragmentData(stopFragment);
        }

    } else if (tabIndex === 5) { // 报警数据
        var alarmData = dataDependency.getAlarmData();
        if (alarmData === null) {
            json_ajax('POST', '/clbs/v/monitoring/getAlarmData', 'json', true, {
                "vehicleId": vehicleId,
                "startTime": parseInt(new Date(startTime.replace(/\-/g, '/')).getTime() / 1000),
                "endTime": parseInt(new Date(endTime.replace(/\-/g, '/')).getTime() / 1000),
            }, function (data) {
                if (data.success) {
                    dataDependency.setAlarmData(data.obj);
                } else {
                    layer.msg(data.msg);
                }
            });
        }

    }
}

Table.prototype.autoGetStatusPosition = function () {
    var dataDependency = this.dependency.get('data');
    var isPlaying = dataDependency.getIsPlaying();
    var isDraging = dataDependency.getIsDraging();
    if (!isPlaying && !isDraging) {
        var table = this.tableStatus;
        var getAddressFun = this.getAddress;
        setTimeout(function () {
            var playIndex = dataDependency.getPlayIndex();
            var d = table.getState().data[playIndex];
            if (d !== undefined && d !== null && d.location === null) {
                getAddressFun(d.id, d.latitude, d.longtitude, function (data) {
                    table.updateOptionData({
                        id: d.id,
                        location: $.isPlainObject(data) ? '未定位' : data
                    });
                });
            }
        }, 500)
    }
}

/**
 * 处理全部数据表格位置点击
 * @param rowId
 */
Table.prototype.handleStatusPositionClick = function (rowId) {
    var table = this.tableStatus;
    var rowInTable = table.findRow(table.getState().data, table.getOption(), rowId);
    if (rowInTable !== null) {
        var d = rowInTable[0];
        this.getAddress(d.id, d.latitude, d.longtitude, function (data) {
            table.updateOptionData({
                id: rowId,
                location: $.isPlainObject(data) ? '未定位' : data
            });
        });
    }
}
//解析位置信息
Table.prototype.getAddress = function (id, latitude, longitude, cb) {
    var url = '/clbs/v/monitoring/address';
    var param = {addressReverse: [latitude, longitude, '', "", 'vehicle']};

    $.ajax({
        type: "POST",//通常会用到两种：GET,POST。默认是：GET
        url: url,//(默认: 当前页地址) 发送请求的地址
        dataType: "json", //预期服务器返回的数据类型。"json"
        async: true, // 异步同步，true  false
        data: param,
        traditional: true,
        timeout: 8000, //超时时间设置，单位毫秒
        success: function (data) {//请求成功
            cb(data)
        },
    });
}
Table.prototype.handleStartLocationClick = function (rowId) {
    var table = this.tableAlarm;
    var rowInTable = table.findRow(table.getState().data, table.getOption(), rowId);
    if (rowInTable !== null) {
        var d = rowInTable[0];
        var startLocation = d.alarmStartLocation.split(',')
        this.getAddress(d.id, startLocation[1], startLocation[0], function (data) {
            table.updateOptionData({
                id: rowId,
                startLocation: $.isPlainObject(data) ? '未定位' : data
            });
        });
    }
}
Table.prototype.handleEndLocationClick = function (rowId) {
    var table = this.tableAlarm;
    var rowInTable = table.findRow(table.getState().data, table.getOption(), rowId);
    if (rowInTable !== null) {
        var d = rowInTable[0];
        var endLocation = d.alarmEndLocation.split(',')
        this.getAddress(d.id, endLocation[1], endLocation[0], function (data) {
            table.updateOptionData({
                id: rowId,
                endLocation: $.isPlainObject(data) ? '未定位' : data
            });
        });
    }
}

Table.prototype.handleStopTableStartLocationClick = function (rowId) {
    var table = this.tableStop;
    var rowInTable = table.findRow(table.getState().data, table.getOption(), rowId);
    if (rowInTable !== null) {
        var d = rowInTable[0];
        this.getAddress(d.id, d.startLatitude, d.startLongtitude, function (data) {
            table.updateOptionData({
                id: rowId,
                stopStartLocation: $.isPlainObject(data) ? '未定位' : data
            });
        });
    }
}

Table.prototype.handleStopTableEndLocationClick = function (rowId) {
    var table = this.tableStop;
    var rowInTable = table.findRow(table.getState().data, table.getOption(), rowId);
    if (rowInTable !== null) {
        var d = rowInTable[0];
        this.getAddress(d.id, d.endLatitude, d.endLongtitude, function (data) {
            table.updateOptionData({
                id: rowId,
                stopEndLocation: $.isPlainObject(data) ? '未定位' : data
            });
        });
    }
}

Table.prototype.handleRunTableStartLocationClick = function (rowId) {
    var table = this.tableRun;
    var rowInTable = table.findRow(table.getState().data, table.getOption(), rowId);
    if (rowInTable !== null) {
        var d = rowInTable[0];
        this.getAddress(d.id, d.startLatitude, d.startLongtitude, function (data) {
            table.updateOptionData({
                id: rowId,
                runStartLocation: $.isPlainObject(data) ? '未定位' : data
            });
        });
    }
}

Table.prototype.handleRunTableEndLocationClick = function (rowId) {
    var table = this.tableRun;
    var rowInTable = table.findRow(table.getState().data, table.getOption(), rowId);
    if (rowInTable !== null) {
        var d = rowInTable[0];
        this.getAddress(d.id, d.endLatitude, d.endLongtitude, function (data) {
            table.updateOptionData({
                id: rowId,
                runEndLocation: $.isPlainObject(data) ? '未定位' : data
            });
        });
    }
}

Table.prototype.handleStatusMouseOver = function (cellIndex, $th) {
    var name = $th.data('name');
    if (name === 'status') {
        $th.justToolsTip({
            animation: "moveInTop",
            width: "auto",
            contents: '连续5个点及以上，且速度都小于等于5km/h，为停止； 连续3个点及以上，且速度都大于5km/h，为行驶',
            gravity: 'top',
            events: 'mouseover',
        });
    }
}

Table.prototype.isTableEmpty = function () {
    var dataDependency = this.dependency.get('data');
    var tabIndex = dataDependency.getTableTabIndex();
    var table;
    if (tabIndex === 1) {
        table = this.tableStatus;
    } else if (tabIndex === 2) {
        table = this.tableObd;
    } else if (tabIndex === 3) {
        table = this.tableRun;
    } else if (tabIndex === 4) {
        table = this.tableStop;
    } else {
        table = this.tableAlarm;
    }
    return table.getState().data.length === 0;
}