// https://tc39.github.io/ecma262/#sec-array.prototype.find
if (!Array.prototype.find) {
    Object.defineProperty(Array.prototype, 'find', {
        value: function (predicate) {
            // 1. Let O be ? ToObject(this value).
            if (this == null) {
                throw new TypeError('"this" is null or not defined');
            }

            var o = Object(this);

            // 2. Let len be ? ToLength(? Get(O, "length")).
            var len = o.length >>> 0;

            // 3. If IsCallable(predicate) is false, throw a TypeError exception.
            if (typeof predicate !== 'function') {
                throw new TypeError('predicate must be a function');
            }

            // 4. If thisArg was supplied, let T be thisArg; else let T be undefined.
            var thisArg = arguments[1];

            // 5. Let k be 0.
            var k = 0;

            // 6. Repeat, while k < len
            while (k < len) {
                // a. Let Pk be ! ToString(k).
                // b. Let kValue be ? Get(O, Pk).
                // c. Let testResult be ToBoolean(? Call(predicate, T, « kValue, k, O »)).
                // d. If testResult is true, return kValue.
                var kValue = o[k];
                if (predicate.call(thisArg, kValue, k, o)) {
                    return kValue;
                }
                // e. Increase k by 1.
                k++;
            }

            // 7. Return undefined.
            return undefined;
        }
    });
}
// 去掉小数点后多余的0
var noZero = function (data) {
    var regexp = /(?:\.0*|(\.\d+?)0+)$/;
    return data.replace(regexp, '$1');
};

var removeDuplicate = function (str) {
    var arr = str.split(',');
    var retArr = [];
    for (var i = 0; i < arr.length; i++) {
        var element = arr[i];
        if (retArr.indexOf(element) < 0) {
            retArr.push(element)
        }
    }
    return retArr.join(',')
};
var logFlag = $("#logFlag").val();
var alarmFanceId = null;
var alarmFanceType = null;
var sendFlag = false;
var alarmRole = false;
var activePageNum = 1;
var ifLoadPageData = true;
var timer;
var alarmTypeList = ['1', '2', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '29', '30', '18', '19', '23', '24', '25', '26'];
// 持续性报警pos,用于实时监控判断报警是否可处理
var continueAlarmsPosList = ["67", "6511", "6512", "6511", "6521", "6522", "6523", "6531", "6532", "6533", "6541", "6542", "6543", "6551", "6552", "6553", "6611", "6612", "6613", "6621", "6622", "6623", "6631", "6632", "6633", "6642", "6642", "6643", "6651", "6652", "6654", "6811", "6812", "6813", "6821", "6822", "6823", "6831", "6832", "6834", "6841", "6842", "6843", "18177", "18178", "18180", "18433", "18434", "18435", "18689", "18691", "18691", "18945", "18946", "18947", "19201", "19202", "19203", "19457", "19458", "19459", "19713", "19714", "19715", "19969", "19970", "19971", "12411", "124", "7012", "7021", "14000", "14001", "14002", "14003", "14004", "14100", "14101", "14102", "14103", "14104", "14105", "14106", "14107", "14108", "14109", "14110", "14112", "14112", "14113", "14114", "14115", "14116", "14117", "14118", "14119", "14120", "14122", "14122", "14123", "14124", "14125", "14126", "14127", "14128", "14129", "14130", "14131", "141000", "14200", "14201", "14202", "14203", "14204", "14205", "14206", "14207", "14208", "14209", "14210", "14211", "14212", "14213", "14214", "14215", "14217", "14217", "14218", "14219", "14220", "14221", "14222", "14223", "14224", "14225", "14226", "14227", "14228", "14229", "14230", "14231", "142000", "14311", "14511", "14521", "14411",
    "12511", "12512", "12513", "12514", "12515", "12516", "12517", "12518",
    "12519", "12520", "12521", "12521", "12523", "12524", "12525", "12526", "12527", "12528", "12529",
    "12530", "12531", "12532", "12533", "12534", "12535", "12536", "12537", "12538", "12539",
    "12539", "12541", "12542", "12611", "12612", "12613", "12614", "12615", "12616", "12616",
    "12618", "12619", "12620", "12621", "12622", "12623", "12624",
    "12625", "12626", "12627", "12628", "12629", "12630", "12631", "12632", "12633", "12634",
    "12635", "12636", "12637", "12638", "12639", "12639", "12641", "12642", "12711", "12712", "12713",
    "12714", "12715", "12716", "12717", "12718", "12719",
    "12720", "12721", "12722", "12723", "12724", "12725", "12726", "13011", "13012", "13013", "13211", "13212", "13213", "13214"];
//io报警
var ioAlarmTypeList = ['14100', '14101', '14102', '14103', '14104', '14105', '14106', '14107', '14108', '14109', '14110', '14111', '14112', '14113', '14114',
    '14115', '14116', '14117', '14118', '14119', '14120', '14121', '14122', '14123', '14124', '14125', '14126', '14127', '14128', '14129', '14130', '14131', '14200',
    '14201', '14202', '14203', '14204', '14205', '14206', '14207', '14208', '14209', '14210', '14211', '14212', '14213', '14214', '14215', '14216', '14217', '14218',
    '14219', '14220', '14221', '14222', '14223', '14224', '14225', '14226', '14227', '14228', '14229', '14230', '14231', '14000', '14001', '14002', '14003', '14004',
    '141000', '142000']
//创建报警信息集合信息
var alarmInfoList = new pageLayout.mapVehicle();
var toFixed = function (source, digit, omitZero) {
    if (typeof source === 'string') {
        source = parseFloat(source)
    }
    if (typeof source === 'number') {
        var afterFixed = source.toFixed(digit) //此时 afterFixed 为string类型
        if (omitZero) {
            afterFixed = parseFloat(afterFixed)
        }
        return afterFixed
    }
}
var riskIdGloabal = null;

//主动安全变量组
var eventMediaPicArr = [];
var eventMediaVideoArr = [];
var eventMediaVideoSingNum = 0;
var eventMediaPicSingNum = 0;
var ifPicOrVideo;

var TimeFn = null;

// 表格全局变量
var tableStatus;
var tableAlarm;
var tableSecurity;
var tableObd;
var tableLog;

var dataTableColumns;

//多媒体弹框
var multimediaFlag = "";
var MediaInfoId = "";
var EventMediaInfoId = "";

// 从业人员详情
var realScrollNum = 3;
var realScrollIndex = 0;
var realInfoScrollBoxW = 641;
var realLockType = false;//判断是否为插卡司机(true:插卡司机,禁止切换司机)

/**
 * 空替换函数，如果value为空，就连unit都不显示
 * @param value 值
 * @param unit 单位
 */
var nvl = function (value, unit) {
    if (value === null || value === undefined) {
        return '';
    }
    return value.toString() + '&ensp;' + unit;
};

var riskDeviceTypeTxt;
var riskId;

var newTableOperation = {
    defaultStatusColumns: window.defaultStatusColumns,
    defaultAlarmColumns: window.defaultAlarmColumns,
    defaultSecurityColumns: window.defaultSecurityColumns,
    defaultOBDColumns: window.defaultOBDColumns,
    defaultLogColumns: window.defaultLogColumns,
    defaultPopupColumns: window.defaultPopupColumns,
    statusColumns: [],
    alarmColumns: [],
    securityColumns: [],
    obdColumns: [],
    logColumns: [],
    popupColumns: [],
    columnKeys: ['REALTIME_MONITORING_LIST', 'REALTIME_MONITORING_ALARM_LIST', 'REALTIME_MONITORING_ACTIVE_SAFETY_LIST', 'REALTIME_MONITORING_OBD_LIST', 'REALTIME_MONITORING_LOG_LIST', 'AlERT_WINDOW_REALTIME_DATA_LIST'],
    lastLockedRowId: undefined,
    lastClickRowId: undefined,
    lastStatusHoverRowId: undefined, // 状态表上次悬浮行ID
    lastStatusHoverField: undefined, // 状态表上次悬浮字段
    lastAlarmHoverRowId: undefined, // 报警表上次悬浮行ID
    lastAlarmHoverField: undefined, // 报警表上次悬浮字段
    lastObdHoverRowId: undefined, // obd表上次悬浮行ID
    lastObdHoverField: undefined, // obd表上次悬浮字段
    /**
     * 初始化用户保存的设置，用于期望显示的列和顺序
     */
    initSetting: function (isUpdate, callback) {
        $.ajax({
            type: "POST",
            url: "/clbs/core/uum/custom/findCustomColumnInfoByMark",
            dataType: "json",
            async: true,
            data: {marks: newTableOperation.columnKeys.join(',')},
            success: function (data) {
                var statusColumns = [$.extend({}, window.defaultStatusColumns[0])];
                var alarmColumns = [$.extend({}, window.defaultAlarmColumns[0])];
                var securityColumns = [$.extend({}, window.defaultSecurityColumns[0])];
                var obdColumns = [$.extend({}, window.defaultOBDColumns[0])];
                var logColumns = [$.extend({}, window.defaultLogColumns[0])];
                var popupColumns = [];
                if (data.success && data.obj) {
                    var columnArray = [statusColumns, alarmColumns, securityColumns, obdColumns, logColumns, popupColumns];
                    var defaultColumnArray = [
                        window.defaultStatusColumns,
                        window.defaultAlarmColumns,
                        window.defaultSecurityColumns,
                        window.defaultOBDColumns,
                        window.defaultLogColumns,
                        window.defaultPopupColumns
                    ];
                    for (var i = 0; i < newTableOperation.columnKeys.length; i++) {
                        var key = newTableOperation.columnKeys[i];
                        var serverColumns = data.obj[key];
                        if (serverColumns !== undefined && serverColumns !== null) {
                            for (var j = 0; j < serverColumns.length; j++) {
                                (function (j) {
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
                                }(j));
                            }
                        }
                    }
                    statusColumns.push(window.defaultStatusColumns[window.defaultStatusColumns.length - 1]);
                    alarmColumns.push(window.defaultAlarmColumns[window.defaultAlarmColumns.length - 1]);
                    securityColumns.push(window.defaultSecurityColumns[window.defaultSecurityColumns.length - 1]);
                    obdColumns.push(window.defaultOBDColumns[window.defaultOBDColumns.length - 1]);
                    logColumns.push(window.defaultLogColumns[window.defaultLogColumns.length - 1]);
                }
                newTableOperation.statusColumns = statusColumns;
                newTableOperation.alarmColumns = alarmColumns;
                newTableOperation.securityColumns = securityColumns;
                newTableOperation.obdColumns = obdColumns;
                newTableOperation.logColumns = logColumns;
                newTableOperation.popupColumns = popupColumns;
                newTableOperation.initOrUpdateSetting(isUpdate);
                var adasSwitch = $('#adasSwitch').val();
                if (typeof callback === 'function' && adasSwitch === 'true') {
                    callback();
                }
            }
        });
    },
    /**
     * 初始化或者更新表格定义
     * @param isUpdate {boolean} 是否是更新
     */
    initOrUpdateSetting: function (isUpdate) {
        // 状态列表
        var statusWidth = 0;
        var statusFreezeColumn = false;
        var setData = {};
        newTableOperation.statusColumns.forEach(function (x) {
            statusWidth += x.width;
            if (x.isFrozen && !statusFreezeColumn) {
                statusFreezeColumn = true;
            }
            if (x.isFrozen) {
                x.resizable = false;
            }
        });
        if (statusFreezeColumn) {
            newTableOperation.statusColumns[0].isFrozen = true;
        }
        setData = {
            defaultPage: 1,
            page: 1,
            size: 15,
        }
        // isUpdate ? tableStatus.setPagination(setData) : null;
        var statusOption = {
            tableId: 'tableStatus',
            data: isUpdate ? tableStatus.getOption().data : [],
            columns: newTableOperation.statusColumns,
            freezeHead: true,
            freezeColumn: statusFreezeColumn,
            getUniqueId: 'id',
            width: statusWidth,
            cancelActiveRow: true,
            clickMeansActive: true,
            dblClickMeansLock: true,
            // flashWhenUpdate:true,
            // scrollWhenAppend:true,
            activeScrollDuration: 0,
            handleTdClick: newTableOperation.handleStatusTableClick,
            handleTdDblClick: newTableOperation.handleStatusTableDblClick,
            handleTdHover: newTableOperation.handleStatusTableHover,
            // setData: setData,
            virtualRender: true,
        };

        // 报警列表
        var alarmWidth = 0;
        var alarmFreezeColumn = false;
        newTableOperation.alarmColumns.forEach(function (x) {
            alarmWidth += x.width;
            if (x.isFrozen && !alarmFreezeColumn) {
                alarmFreezeColumn = true;
            }
            if (x.isFrozen) {
                x.resizable = false;
            }
        });
        if (alarmFreezeColumn) {
            newTableOperation.alarmColumns[0].isFrozen = true;
        }

        var alarmOption = {
            tableId: 'tableAlarm',
            data: isUpdate ? tableAlarm.getOption().data : [],
            columns: newTableOperation.alarmColumns,
            freezeHead: true,
            freezeColumn: alarmFreezeColumn,
            getUniqueId: 'id',
            width: alarmWidth,
            clickMeansActive: true,
            // flashWhenUpdate: true,
            // scrollWhenAppend: true, // 自动滚动
            handleTdDblClick: newTableOperation.handleAlarmTableDblClick,
            handleTdHover: newTableOperation.handleAlarmTableHover
        };

        // 主动安全列表
        var securityWidth = 0;
        var securityFreezeColumn = false;
        newTableOperation.securityColumns.forEach(function (x) {
            securityWidth += x.width;
            if (x.isFrozen && !securityFreezeColumn) {
                securityFreezeColumn = true;
            }
            if (x.isFrozen) {
                x.resizable = false;
            }
        });
        if (securityFreezeColumn) {
            newTableOperation.securityColumns[0].isFrozen = true;
        }

        var securityOption = {
            tableId: 'tableSecurity',
            data: isUpdate ? tableSecurity.getOption().data : [],
            columns: newTableOperation.securityColumns,
            freezeHead: true,
            freezeColumn: securityFreezeColumn,
            getUniqueId: 'id',
            width: securityWidth,
            clickMeansActive: true,
            // flashWhenUpdate: true,
            handleTdDblClick: newTableOperation.handleSecurityTableDblClick,
            handleTdClick: newTableOperation.handleSecurityTableClick,
            handleScroll: dataTableOperation.activeScrollPage
        };

        // OBD列表
        var obdWidth = 0;
        var obdFreezeColumn = false;
        newTableOperation.obdColumns.forEach(function (x) {
            obdWidth += x.width;
            if (x.isFrozen && !obdFreezeColumn) {
                obdFreezeColumn = true;
            }
            if (x.isFrozen) {
                x.resizable = false;
            }
        });
        if (obdFreezeColumn) {
            newTableOperation.obdColumns[0].isFrozen = true;
        }

        var obdOption = {
            tableId: 'tableOBD',
            data: isUpdate ? tableObd.getOption().data : [],
            columns: newTableOperation.obdColumns,
            freezeHead: true,
            freezeColumn: obdFreezeColumn,
            getUniqueId: 'id',
            width: obdWidth,
            cancelActiveRow: true,
            clickMeansActive: true,
            dblClickMeansLock: true,
            // flashWhenUpdate: true,
            // scrollWhenAppend: true,
            activeScrollDuration: 0,
            handleTdClick: newTableOperation.handleObdTableClick,
            handleTdDblClick: newTableOperation.handleObdTableDblClick,
            handleTdHover: newTableOperation.handleObdTableHover,
            virtualRender: true,
        };

        // log列表
        var logWidth = 0;
        var logFreezeColumn = false;
        newTableOperation.logColumns.forEach(function (x) {
            logWidth += x.width;
            if (x.isFrozen && !logFreezeColumn) {
                logFreezeColumn = true;
            }
            if (x.isFrozen) {
                x.resizable = false;
            }
        });
        if (logFreezeColumn) {
            newTableOperation.logColumns[0].isFrozen = true;
        }

        var logOption = {
            tableId: 'tableLog',
            data: isUpdate ? tableLog.getOption().data : [],
            columns: newTableOperation.logColumns,
            freezeHead: true,
            // flashWhenUpdate: true,
            // scrollWhenAppend: true,
            freezeColumn: logFreezeColumn,
            getUniqueId: 'id',
            width: logWidth
        };

        tableStatus = $('#realTimeStateTable-div').itable(statusOption);
        tableAlarm = $('#alarmTable-div').itable(alarmOption);
        tableSecurity = $('#securityTable-div').itable(securityOption);
        tableObd = $('#obdTable-div').itable(obdOption);
        tableLog = $('#logTable-div').itable(logOption);

        if (isUpdate) {
            tableStatus.render();
            tableAlarm.render();
            tableSecurity.render();
            tableObd.render();
            tableLog.render();
        }
    },
    /**
     * 报警表格处理状态渲染函数
     * @param data 0 代表未处理
     * @param rowIndex
     * @param columnIndex
     * @param row
     * @returns {string}
     */
    alarmTableHandleStatusRender: function (data, rowIndex, columnIndex, row) {

        if (data === 0) {
            if (row.protocolType === '22') return '未处理';
            return '<a href="#" onclick="newTableOperation.handleHandleStatusClick(\'' + row.id + '\')">未处理</a>';
        }
        return '已处理';
    },
    /**
     * 报警表格位置信息渲染函数
     * @param data 位置信息，0代表一开始没有位置
     * @param rowIndex
     * @param columnIndex
     * @param row
     * @returns {*}
     */
    alarmTableHandlePositionRender: function (data, rowIndex, columnIndex, row) {
        if (data === 0) {
            return '<a href="#" onclick="newTableOperation.handlePositionClick(\'' + row.id + '\')">点击获取位置信息</a>';
        }
        return data;
    },
    /**
     * 处理 “处理状态” 单击事件
     * @param rowId 行ID
     */
    handleHandleStatusClick: function (rowId) {
        var rowInTable = tableAlarm.findRow(tableAlarm.getState().data, tableAlarm.getOption(), rowId);
        if (rowInTable !== null) {
            var d = rowInTable[0];
            var dataString =
                d.monitorName
                + "|" + d.alarmTime
                + "|" + d.assignmentName
                + "|" + d.vehicleType
                + "|" + d.plateColorName
                + "|" + d.alarmType
                + "|" + d.professionalsName
                + "|" + d.field11
                + "|" + d.simcardNumber
                + "|" + d.deviceNumber
                + "|" + d.msgSN
                + "|" + d.alarmNumber
                + "|" + d.alarmSource
                + "|" + d.id
                + "|" + d.earlyAlarmStartTime
                + "|" + d.startTime;
            dataTableOperation.warningManage(dataString)
        }
    },
    /**
     * 处理报警表格位置点击
     * @param rowId
     */
    handlePositionClick: function (rowId) {
        var rowInTable = tableAlarm.findRow(tableAlarm.getState().data, tableAlarm.getOption(), rowId);
        if (rowInTable !== null) {
            var d = rowInTable[0];
            var address = d.address.split('|');
            dataTableOperation.getAlarmAddress(d.id, address[0], address[1]);
        }
    },
    /**
     * 处理报警表格双击事件
     * @param rowId 行ID
     * @param cellIndex
     * @param td
     */
    handleAlarmTableDblClick: function (rowId, cellIndex, td) {
        var rowInTable = tableAlarm.findRow(tableAlarm.getState().data, tableAlarm.getOption(), rowId);
        if (rowInTable !== null) {
            var d = rowInTable[0];

            var alarmVid = d.id;
            var timeFormat = d.alarmTime;
            var alarmStr = d.alarmType;
            var earlyAlarmStartTime = d.earlyAlarmStartTime;
            var alarmTypeArr = d.alarmNumber;
            // 判断是否有报警查询的菜单权限
            var alarmFlag = false;
            var permissionUrls = $("#permissionUrls").val();
            if (permissionUrls !== null && permissionUrls !== undefined) {
                var urllist = permissionUrls.split(",");
                if (urllist.indexOf("/a/search/list") > -1) {
                    alarmFlag = true;
                    //跳转
                    if (earlyAlarmStartTime == null || typeof(earlyAlarmStartTime) == "undefined" || earlyAlarmStartTime === 0) {
                        earlyAlarmStartTime = timeFormat;
                    }
                    location.href = "/clbs/a/search/list?avid=" + alarmVid + "&atype=0" + "&atime=" + earlyAlarmStartTime + "&alarmTypeArr=" + alarmTypeArr;
                }
            }
            if (!alarmFlag) {
                layer.msg("无操作权限，请联系管理员");
            }
        }
    },
    /**
     * 处理OBD表单击事件
     * @param rowId 行ID
     * @param cellIndex
     * @param td
     */
    handleObdTableClick: function (rowId, cellIndex, td) {
        tableStatus.setActiveRow(rowId);
        newTableOperation.handleStatusObdClick(rowId);
    },
    /**
     * 处理状态表的鼠标悬浮事件，显示气泡
     * @param rowIndex 行索引
     * @param cellIndex 单元格索引
     * @param $td 单元格jQuery引用
     */
    handleStatusTableHover: function (rowIndex, cellIndex, $td) {
        var field = $td.data('field');
        var rowId = $td.attr('id').split('_')[1];
        var rowInTable = tableStatus.findRow(tableStatus.getState().data, tableStatus.getOption(), rowId);
        if (rowInTable === null) {
            return;
        }
        var row = rowInTable[0];
        var content = '';

        switch (field) {
            case 'signalState':
                content = row.signalState && row.signalState !== '-' ? row.signalState : ''; // 信号状态
                break;
            case 'positionDescription':
                content = newTableOperation.buildPositionDescriptionTable(row.positionDescription); // 位置
                break;
            case 'oilMass':
                content = newTableOperation.buildOilMassTable(row.oilMass); // 传感器液体量
                break;
            /* case 'dayOilWear':
                 content = newTableOperation.buildOilExpendTable(row.oilExpend); // 传感器当日油耗
                 break;*/
            case 'oilExpend':
                content = newTableOperation.buildOilExpendTable(row.oilExpend, true); // 传感器总油耗
                break;
            case 'elecData':
                content = newTableOperation.buildElecDataTable(row.elecData); // 电量电压
                break;
            case 'temperatureSensor':
                content = newTableOperation.buildTemperatureSensorTable(row.temperatureSensor); // 温度
                break;
            case 'temphumiditySensor':
                content = newTableOperation.buildTemphumiditySensorTable(row.temphumiditySensor); // 湿度
                break;
            case 'positiveNegative':
                content = newTableOperation.buildPositiveNegativeTable(row.positiveNegative); // 正反转
                break;
            case 'loadInfos':
                content = newTableOperation.buildLoadInfosTable(row.loadInfos); // 载重 buildWorkHourTable
                break;
            case 'workHourSensor':
                content = newTableOperation.buildWorkHourTable(row.workHourSensor); // 工时
                break;
            case 'tyreInfos':
                content = newTableOperation.buildTyreInfosTable(row.tyreInfos); // 胎压
                break;
            case 'terminalIOStatus':
                content = newTableOperation.buildTerminalIOStatus(rowId); // 终端IO状态
                break;
            default:
                return;
        }
        if (content.length > 0) {
            newTableOperation.lastStatusHoverRowId = rowId;
            newTableOperation.lastStatusHoverField = field;
            $td.justToolsTip({
                animation: "moveInTop",
                width: "auto",
                contents: content,
                gravity: 'top',
                events: 'mouseover',
                onRemove: function () {
                    newTableOperation.lastStatusHoverRowId = undefined;
                    newTableOperation.lastStatusHoverField = undefined;
                }
            });

            //ie兼容
            // $td.mouseover(function (event){
            //     var oPoint = document. elementFromPoint(event.clientX,event.clientY);
            //     if(oPoint.tagName.toLowerCase() == 'div'){
            //         $td.justToolsTip({
            //             animation: "moveInTop",
            //             width: "auto",
            //             contents: content,
            //             gravity: 'top',
            //             events: 'mouseover',
            //             onRemove: function () {
            //                 newTableOperation.lastStatusHoverRowId = undefined;
            //                 newTableOperation.lastStatusHoverField = undefined;
            //             }
            //         });
            //     }
            // });
        }
    },
    /**
     * 处理报警表的鼠标悬浮事件，显示气泡
     * @param rowIndex 行索引
     * @param cellIndex 单元格索引
     * @param $td 单元格jQuery引用
     */
    handleAlarmTableHover: function (rowIndex, cellIndex, $td) {
        var field = $td.data('field');
        var rowId = $td.attr('id').split('_')[1];
        var rowInTable = tableAlarm.findRow(tableAlarm.getState().data, tableAlarm.getOption(), rowId);
        if (rowInTable === null) {
            return;
        }
        var row = rowInTable[0];
        var content = '';
        switch (field) {
            case 'alarmType':
                content = newTableOperation.buildAlarmTypeTable(row.alarmType); // 报警类型
                break;
            default:
                return;
        }
        if (content.length > 0) {
            newTableOperation.lastAlarmHoverRowId = rowId;
            newTableOperation.lastAlarmHoverField = field;
            $td.justToolsTip({
                animation: "moveInTop",
                width: "auto",
                contents: content,
                gravity: 'top',
                events: 'mouseover',
                onRemove: function () {
                    newTableOperation.lastAlarmHoverRowId = undefined;
                    newTableOperation.lastAlarmHoverField = undefined;
                }
            });
        }
    },
    /**
     * 处理obd的鼠标悬浮事件，显示气泡
     * @param rowIndex 行索引
     * @param cellIndex 单元格索引
     * @param $td 单元格jQuery引用
     */
    handleObdTableHover: function (rowIndex, cellIndex, $td) {
        var field = $td.data('field');
        var rowId = $td.attr('id').split('_')[1];
        var rowInTable = tableObd.findRow(tableObd.getState().data, tableObd.getOption(), rowId);
        if (rowInTable === null) {
            return;
        }
        var row = rowInTable[0];
        var content = '';
        switch (field) {
            case 'obdVin':
                content = newTableOperation.obdDataString(row.obdVin); // 车辆识别码（VIN码）
                break;
            case 'obdVersion':
                content = newTableOperation.obdDataString(row.obdVersion); // 软件标定识别号
                break;
            case 'obdCvn':
                content = newTableOperation.obdDataString(row.obdCvn); // 标定验证码（cvn）
                break;
            case 'obdIupr':
                content = newTableOperation.obdDataString(row.obdIupr); // IUPR值
                break;
            case 'obdTroubleCodes':
                content = newTableOperation.obdDataString(row.obdTroubleCodes); // 故障码信息列表
                break;
            default:
                return;
        }
        if (content && content.length > 0) {
            newTableOperation.lastObdHoverRowId = rowId;
            newTableOperation.lastObdHoverField = field;
            $td.justToolsTip({
                animation: "moveInTop",
                width: "auto",
                contents: content,
                gravity: 'top',
                events: 'mouseover',
                onRemove: function () {
                    newTableOperation.lastObdHoverRowId = undefined;
                    newTableOperation.lastObdHoverField = undefined;
                }
            });
        }
    },
    obdDataString: function (data) {
        if (data == null) return;
        return data;
    },
    obdDiagnosticSupportStateClick: function (...data) {
        $('#obdDiagnosticSupportStateClick').hide();
        $('#DiagnosticSupport-1').text(data[0] ? '支持' : '不支持');
        $('#DiagnosticSupport-2').text(data[1] ? '支持' : '不支持');
        $('#DiagnosticSupport-3').text(data[2] ? '支持' : '不支持');
        $('#DiagnosticSupport-4').text(data[3] ? '支持' : '不支持');
        $('#DiagnosticSupport-5').text(data[4] ? '支持' : '不支持');
        $('#DiagnosticSupport-6').text(data[5] ? '支持' : '不支持');
        $('#DiagnosticSupport-7').text(data[6] ? '支持' : '不支持');
        $('#DiagnosticSupport-8').text(data[7] ? '支持' : '不支持');
        $('#DiagnosticSupport-9').text(data[8] ? '支持' : '不支持');
        $('#DiagnosticSupport-10').text(data[9] ? '支持' : '不支持');
        $('#DiagnosticSupport-11').text(data[10] ? '支持' : '不支持');
        $('#DiagnosticSupport-12').text(data[11] ? '支持' : '不支持');
        $('#DiagnosticSupport-13').text(data[12] ? '支持' : '不支持');
        $('#DiagnosticSupport-14').text(data[13] ? '支持' : '不支持');
        $('#DiagnosticSupport-15').text(data[14] ? '支持' : '不支持');
        $('#DiagnosticSupport-16').text(data[15] ? '支持' : '不支持');
        setTimeout(function () {
            $('#obdDiagnosticSupportStateClick').show();
        }, 300);
    },
    obdDiagnosticSupportStateOpen: function () {
        $('#obdDiagnosticSupportStateClick').hide();
    },
    obdDiagnosticReadyStateClick: function (...data) {
        $('#obdDiagnosticSupportStateClick').hide();
        $('#DiagnosticSupport-1').text(data[0] ? '支持' : '不支持');
        $('#DiagnosticSupport-2').text(data[1] ? '支持' : '不支持');
        $('#DiagnosticSupport-3').text(data[2] ? '支持' : '不支持');
        $('#DiagnosticSupport-4').text(data[3] ? '支持' : '不支持');
        $('#DiagnosticSupport-5').text(data[4] ? '支持' : '不支持');
        $('#DiagnosticSupport-6').text(data[5] ? '支持' : '不支持');
        $('#DiagnosticSupport-7').text(data[6] ? '支持' : '不支持');
        $('#DiagnosticSupport-8').text(data[7] ? '支持' : '不支持');
        $('#DiagnosticSupport-9').text(data[8] ? '支持' : '不支持');
        $('#DiagnosticSupport-10').text(data[9] ? '支持' : '不支持');
        $('#DiagnosticSupport-11').text(data[10] ? '支持' : '不支持');
        $('#DiagnosticSupport-12').text(data[11] ? '支持' : '不支持');
        $('#DiagnosticSupport-13').text(data[12] ? '支持' : '不支持');
        $('#DiagnosticSupport-14').text(data[13] ? '支持' : '不支持');
        $('#DiagnosticSupport-15').text(data[14] ? '支持' : '不支持');
        $('#DiagnosticSupport-16').text(data[15] ? '支持' : '不支持');
        setTimeout(function () {
            $('#obdDiagnosticSupportStateClick').show();
        }, 300);
    },
    /**
     * 处理状态表的单击事件
     * @param rowId 行ID
     * @param cellIndex
     * @param td
     */
    handleStatusTableClick: function (rowId, cellIndex, td) {
        tableObd.setActiveRow(rowId);
        newTableOperation.handleStatusObdClick(rowId);
    },
    /**
     * 处理状态表和OBD表的单击事件，联动两个表格和树
     * @param rowId 行ID
     */
    handleStatusObdClick: function (rowId) {
        //判断当前单击后的信息是否高亮
        var treeNodeItem = $(".ztree li a");
        if (newTableOperation.lastClickRowId === rowId || newTableOperation.lastLockedRowId === rowId) {
            //清除车辆树高亮效果
            if (licensePlateInformation === rowId) {
                treeNodeItem.removeAttr("class", "curSelectedNode");
                $("#" + dbclickCheckedId).parent().removeAttr("class", "curSelectedNode_dbClick");
            }
            if (groupIconSkin === "assignmentSkin" || groupIconSkin === "groupSkin") {
                treeNodeItem.removeAttr("class", "curSelectedNode");
                $("#" + dbclickCheckedId).parent().removeAttr("class", "curSelectedNode_dbClick");
            }

            treeNodeItem.removeClass("curSelectedNode_dbClick");
            treeNodeItem.removeClass("curSelectedNode");
            //取消聚焦跟踪
            treeMonitoring.centerMarkerNo();
            newTableOperation.lastClickRowId = undefined;
            newTableOperation.lastLockedRowId = undefined;
        } else {
            treeNodeItem.removeClass("curSelectedNode_dbClick");
            treeNodeItem.removeClass("curSelectedNode");
            //为车辆树添加高亮
            var zTreeDataTables = $.fn.zTree.getZTreeObj("treeDemo");
            var dataTabCheckedNum = zTreeDataTables.getCheckedNodes(true);
            for (var i = 0; i < dataTabCheckedNum.length; i++) {
                if (dataTabCheckedNum[i].id === rowId) {
                    ztreeStyleDbclick = dataTabCheckedNum[i].tId;
                    $("#" + ztreeStyleDbclick).children("a").addClass("curSelectedNode");
                }
            }
            TimeFn = setTimeout(function () {
                //聚焦跟踪执行方法
                // dataTableOperation.centerMarkerBands(objID);
                treeMonitoring.centerMarker(rowId, 'DBLCLICK');
            }, 300);
            newTableOperation.lastClickRowId = rowId;
            newTableOperation.lastLockedRowId = undefined;
        }
    },
    /**
     * 处理ODB表的双击事件
     * @param rowId 行ID
     * @param cellIndex
     * @param td
     */
    handleObdTableDblClick: function (rowId, cellIndex, td) {
        if (rowId === undefined) {
            return;
        }
        tableStatus.setLockedRow(rowId);
        treeMonitoring.vehicleTreeClickGetFenceInfo(true, rowId);
        newTableOperation.handleStatusObdDblClick(rowId);
    },
    /**
     * 处理状态表的双击事件
     * @param rowId 行ID
     * @param cellIndex
     * @param td
     */
    handleStatusTableDblClick: function (rowId, cellIndex, td) {
        if (rowId === undefined) {
            return;
        }
        tableObd.setLockedRow(rowId);
        treeMonitoring.vehicleTreeClickGetFenceInfo(true, rowId);
        newTableOperation.handleStatusObdDblClick(rowId);
    },
    /**
     * 处理状态表和OBD表的双击事件，联动两个表格和树
     * @param rowId 行ID
     */
    handleStatusObdDblClick: function (rowId) {
        var treeNodeItem = $(".ztree li a");
        treeNodeItem.removeClass("curSelectedNode_dbClick");
        treeNodeItem.removeClass("curSelectedNode");
        //为车辆树添加高亮
        var zTreeDataTables = $.fn.zTree.getZTreeObj("treeDemo");
        var dataTabCheckedNum = zTreeDataTables.getCheckedNodes(true);
        for (var i = 0; i < dataTabCheckedNum.length; i++) {
            if (dataTabCheckedNum[i].id === rowId) {
                var treeNode = dataTabCheckedNum[i];
                var vehicleInfo = {
                    vid: treeNode.id,
                    brand: treeNode.name,
                    deviceNumber: treeNode.deviceNumber,
                    plateColor: treeNode.plateColor,
                    isVideo: treeNode.isVideo,
                    simcardNumber: treeNode.simcardNumber,
                };

                subscribeVehicleInfo = vehicleInfo; //订阅的车辆信息全局变量

                realTimeVideo.setVehicleInfo(vehicleInfo);

                activeSafety.riskInformationDetails(treeNode.id);

                ztreeStyleDbclick = dataTabCheckedNum[i].tId;
                oldDbclickCheckedId = ztreeStyleDbclick;
                $("#" + ztreeStyleDbclick).children("a").addClass("curSelectedNode_dbClick");
            }
        }
        newTableOperation.lastLockedRowId = rowId;
        newTableOperation.lastClickRowId = undefined;
        //聚焦跟踪执行方法
        treeMonitoring.centerMarker(rowId, 'DBLCLICK');
        clearTimeout(TimeFn);
    },
    /**
     * 处理主动安全表格单击事件
     * @param rowId 行ID
     * @param cellIndex 单元格索引
     * @param td 单元格jQuery引用
     */
    handleSecurityTableClick: function (rowId, cellIndex, td) {
        var tr = td.closest('tr');
        var id = tr.attr('id').split('_')[0];
        var rowIndex = tr.index() + 1;
        var offsetTop = rowIndex * 41;

        var row = tableSecurity.findRow(tableSecurity.getState().data, tableSecurity.getOption(), id);
        var vehicleId = row[0].vehicleId;
        dataTableOperation.ifshowActiveSafetyPop(id, vehicleId, offsetTop);
    },
    /**
     * 处理主动安全表格双击事件
     * @param rowId
     * @param cellIndex
     * @param td
     */
    handleSecurityTableDblClick: function (rowId, cellIndex, td) {
        clearTimeout(timer);
        if ($('#activeSafetyPop').hasClass("in show")) {
            return;
        }
        var params = {
            moduleName: '主动安全处置报表'
        };
        json_ajax("POST", "/clbs/adas/lb/guide/isPermissions", "json", true, params, function (data) {
            if (data) {
                window.location.href = '/clbs/r/riskManagement/disposeReport/list';
            } else {
                layer.msg("无操作权限，请联系管理员");
                return false;
            }
        });
    },
    /**
     * 构建传感器液体量表格HTML内容
     * @param arr 传感器液体量数据
     * @returns {string} 完整的表格HTML内容
     */
    buildOilMassTable: function (arr) {
        var thArr = [];
        var tdArr = [];
        var content = '';

        // 传感器只有1个外设ID且列表显示值为异常时，列表中该传感器数据直接显示异常，不弹框
        if (arr && arr.length === 1 && arr[0].unusual === 1) {
            return '';
        }

        if (arr && arr.length > 0) {
            arr = arr.sort(function (x, x2) {
                return x.id - x2.id;
            });

            for (var i = 0; i < arr.length; i++) {
                tdArr.push([]);
                var item = arr[i];
                thArr.push(item.id.toString(16) + '液位传感器');
                // 如果温度小于-50，则显示-
                var oilTem = item.oilTem;
                if (item.oilTem === undefined || item.oilTem === null || item.oilTem < -50) {
                    oilTem = '-';
                } else {
                    oilTem = item.oilTem;
                }
                var envTem = item.envTem;
                if (item.envTem === undefined || item.envTem === null || item.envTem < -50) {
                    envTem = '-';
                } else {
                    envTem = item.envTem;
                }
                tdArr[i].push('液位高度:', arr[i].unusual === 1 ? '异常' : nvl(item.oilHeight, 'mm'));
                tdArr[i].push('AD值:', arr[i].unusual === 1 ? '异常' : nvl(item.aDHeight, ''));
                tdArr[i].push('液体温度:', arr[i].unusual === 1 ? '异常' : nvl(oilTem, '°C'));
                tdArr[i].push('环境温度:', arr[i].unusual === 1 ? '异常' : nvl(envTem, '°C'));
                tdArr[i].push('加液量:', arr[i].unusual === 1 ? '异常' : nvl(item.add, 'L'));
                tdArr[i].push('漏液量:', arr[i].unusual === 1 ? '异常' : nvl(item.del, 'L'));
                tdArr[i].push('液体量:', arr[i].unusual === 1 ? '异常' : nvl(item.oilMass, 'L'));
                tdArr[i].push('液位百分比:', arr[i].unusual === 1 ? '异常' : nvl(item.percentage, '%'));
            }
            if (thArr.length > 0 && tdArr.length > 0) {
                content = newTableOperation.buildHoverTable(thArr, tdArr);
            }
        }

        return content
    },
    /**
     * 构建传感器当日油耗和总油耗表格HTML内容
     * @param arr 传感器油耗数据
     * @param isTotal {boolean} 是否是总油耗
     * @returns {string} 完整的表格HTML内容
     */
    buildOilExpendTable: function (arr, isTotal) {
        var thArr = [];
        var tdArr = [];
        var content = '';

        // 传感器只有1个外设ID且列表显示值为异常时，列表中该传感器数据直接显示异常，不弹框
        if (arr && arr.length === 1 && arr[0].unusual === 1) {
            return '';
        }

        if (arr && arr.length > 0) {
            arr = arr.sort(function (x, x2) {
                return x.id - x2.id;
            });

            for (var i = 0; i < arr.length; i++) {
                tdArr.push([]);
                var item = arr[i];
                thArr.push(item.id.toString(16) + '油耗传感器');
                tdArr[i].push('瞬时流量:', arr[i].unusual === 1 ? '异常' : nvl(item.momentExpend, 'L'));
                tdArr[i].push('温度:', arr[i].unusual === 1 ? '异常' : nvl(item.oilTem, '°C'));
                tdArr[i].push('累计时间:', arr[i].unusual === 1 ? '异常' : nvl(item.deltaTime, 'h'));
                if (isTotal) {
                    tdArr[i].push('总流量:', arr[i].unusual === 1 ? '异常' : nvl(item.allExpend, 'L'));
                } else {
                    tdArr[i].push('当日流量:', arr[i].unusual === 1 ? '异常' : nvl(item.dayOilWear, 'L'));
                }
            }
            if (thArr.length > 0 && tdArr.length > 0) {
                content = newTableOperation.buildHoverTable(thArr, tdArr);
            }
        }

        return content
    },
    /**
     * 构建电量电压表格HTML内容
     * @param elecData 电量电压数据
     * @returns {string} 完整的表格HTML内容
     */
    buildElecDataTable: function (elecData) {
        var thArr = [];
        var tdArr = [[]];
        var content = '';

        // 传感器只有1个外设ID且列表显示值为异常时，列表中该传感器数据直接显示异常，不弹框
        if (elecData && elecData.unusual === 1) {
            return '';
        }

        var i = 0;
        if (elecData !== undefined && elecData !== null) {
            thArr.push(elecData.id.toString(16) + '电量电压');
            if (elecData.deviceElectricity !== undefined && elecData.deviceElectricity !== null) { // 设备
                // if (elecData.type === 0) { // 电量
                if (elecData.deviceElectricity <= 100) {
                    tdArr[i].push('设备电量:', elecData.unusual === 1 ? '异常' : nvl(elecData.deviceElectricity, '%'));
                }
                /* } else {
                     if (elecData.deviceElectricity < 6000) {
                         tdArr[i].push('设备电压:', elecData.unusual === 1 ? '异常' : nvl(elecData.deviceElectricity, 'V'));
                     }
                 }*/
            }
            if (elecData.drivingElectricity !== undefined && elecData.drivingElectricity !== null) { // 行车
                if (elecData.type === 0) { // 电量
                    if (elecData.drivingElectricity <= 100) {
                        tdArr[i].push('行车电量:', elecData.unusual === 1 ? '异常' : nvl(elecData.drivingElectricity, '%'));
                    }
                } else {
                    if (elecData.drivingElectricity < 6000) {
                        tdArr[i].push('行车电压:', elecData.unusual === 1 ? '异常' : nvl(elecData.drivingElectricity, 'V'));
                    }

                }
            }
            if (elecData.coldStorageElectricity !== undefined && elecData.coldStorageElectricity !== null) { // 冷藏
                if (elecData.type === 0) { // 电量
                    if (elecData.coldStorageElectricity <= 100) {
                        tdArr[i].push('冷藏电量:', elecData.unusual === 1 ? '异常' : nvl(elecData.coldStorageElectricity, '%'));
                    }
                } else {
                    if (elecData.coldStorageElectricity <= 6000) {
                        tdArr[i].push('冷藏电压:', elecData.unusual === 1 ? '异常' : nvl(elecData.coldStorageElectricity, 'V'));
                    }
                }
            }
            if (thArr.length > 0 && tdArr.length > 0) {
                content = newTableOperation.buildHoverTable(thArr, tdArr);
            }
        }

        return content
    },
    /**
     * 构建温度传感器表格HTML内容
     * @param arr 传感器数据
     * @returns {string} 完整的表格HTML内容
     */
    buildTemperatureSensorTable: function (arr) {
        var thArr = [];
        var tdArr = [];
        var content = '';

        // 传感器只有1个外设ID且列表显示值为异常时，列表中该传感器数据直接显示异常，不弹框
        if (arr && arr.length === 1 && arr[0].unusual === 1) {
            return '';
        }

        if (arr && arr.length > 0) {
            arr = arr.sort(function (x, x2) {
                return x.id - x2.id;
            });

            for (var i = 0; i < arr.length; i++) {
                tdArr.push([]);
                var item = arr[i];
                thArr.push(item.id.toString(16) + '温度传感器');
                tdArr[i].push('温度值:', arr[i].unusual === 1 ? '异常' : nvl(item.temperature, '°C'));
                tdArr[i].push('状态持续时间:', arr[i].unusual === 1 ? '异常' : nvl(item.outTime, 's'));
            }
            if (thArr.length > 0 && tdArr.length > 0) {
                content = newTableOperation.buildHoverTable(thArr, tdArr);
            }
        }
        return content
    },
    /**
     * 构建湿度传感器表格HTML内容
     * @param arr 传感器数据
     * @returns {string} 完整的表格HTML内容
     */
    buildTemphumiditySensorTable: function (arr) {
        var thArr = [];
        var tdArr = [];
        var content = '';

        // 传感器只有1个外设ID且列表显示值为异常时，列表中该传感器数据直接显示异常，不弹框
        if (arr && arr.length === 1 && arr[0].unusual === 1) {
            return '';
        }

        if (arr && arr.length > 0) {
            arr = arr.sort(function (x, x2) {
                return x.id - x2.id;
            });

            for (var i = 0; i < arr.length; i++) {
                tdArr.push([]);
                var item = arr[i];
                thArr.push(item.id.toString(16) + '湿度传感器');
                tdArr[i].push('湿度值:', arr[i].unusual === 1 ? '异常' : nvl(item.temperature, '%'));
                tdArr[i].push('状态持续时间:', arr[i].unusual === 1 ? '异常' : nvl(item.outTime, 's'));
            }
            if (thArr.length > 0 && tdArr.length > 0) {
                content = newTableOperation.buildHoverTable(thArr, tdArr);
            }
        }
        return content
    },
    /**
     * 构建正反转传感器表格HTML内容
     * @param arr 传感器数据
     * @returns {string} 完整的表格HTML内容
     */
    buildPositiveNegativeTable: function (arr) {
        var thArr = [];
        var tdArr = [];
        var content = '';

        // 传感器只有1个外设ID且列表显示值为异常时，列表中该传感器数据直接显示异常，不弹框
        if (arr && arr.length === 1 && arr[0].unusual === 1) {
            return '';
        }

        if (arr && arr.length > 0) {
            arr = arr.sort(function (x, x2) {
                return x.id - x2.id;
            });

            for (var i = 0; i < arr.length; i++) {
                tdArr.push([]);
                var item = arr[i];

                var spinState = item.spinState === 1 ? '停转' : '运行';
                var spinDirection = '';
                if (spinState === '运行') {
                    if (item.spinDirection === 1) {
                        spinDirection = '正转';
                    } else {
                        spinDirection = '反转';
                    }
                }

                thArr.push(item.id.toString(16) + '正反转传感器');
                tdArr[i].push('旋转状态:', arr[i].unusual === 1 ? '异常' : spinState);
                tdArr[i].push('旋转方向:', arr[i].unusual === 1 ? '异常' : spinDirection);
                tdArr[i].push('旋转速度:', arr[i].unusual === 1 ? '异常' : nvl(item.spinSpeed, '转/min'));
                tdArr[i].push('累计运行时间:', arr[i].unusual === 1 ? '异常' : nvl(item.winchTime, 'h'));
                tdArr[i].push('累计脉冲数量:', arr[i].unusual === 1 ? '异常' : nvl(item.winchCounter, ''));
                tdArr[i].push('旋转方向持续时间:', arr[i].unusual === 1 ? '异常' : nvl(item.winchRotateTime, 'min'));
            }
            if (thArr.length > 0 && tdArr.length > 0) {
                content = newTableOperation.buildHoverTable(thArr, tdArr);
            }
        }
        return content
    },
    /**
     * 构建重量传感器表格HTML内容
     * @param arr 传感器数据
     * @returns {string} 完整的表格HTML内容
     */
    buildLoadInfosTable: function (arr) {
        var thArr = [];
        var tdArr = [];
        var content = '';

        // 传感器只有1个外设ID且列表显示值为异常时，列表中该传感器数据直接显示异常，不弹框
        if (arr && arr.length === 1 && arr[0].unusual === 1) {
            return '';
        }

        if (arr && arr.length > 0) {
            arr = arr.sort(function (x, x2) {
                return x.id - x2.id;
            });

            for (var i = 0; i < arr.length; i++) {
                tdArr.push([]);
                var item = arr[i];

                var status = '';
                if (item.status === 1) { // 01-空载； 02-满载； 03-超载； 04-装载； 05-卸载；06- 轻载；07-重载
                    status = '空载';
                } else if (item.status === 2) {
                    status = '满载';
                } else if (item.status === 3) {
                    status = '超载';
                } else if (item.status === 4) {
                    status = '装载';
                } else if (item.status === 5) {
                    status = '卸载';
                } else if (item.status === 6) {
                    status = '轻载';
                } else if (item.status === 7) {
                    status = '重载';
                }

                thArr.push(item.id.toString(16) + '载重传感器');
                tdArr[i].push('载重状态:', arr[i].unusual === 1 ? '异常' : status);
                if (status === '装载') {
                    tdArr[i].push('装载次数:', arr[i].unusual === 1 ? '异常' : nvl(item.countNum, ''));
                } else if (status === '卸载') {
                    tdArr[i].push('卸载次数:', arr[i].unusual === 1 ? '异常' : nvl(item.countNum, ''));
                }
                tdArr[i].push('载荷重量:', arr[i].unusual === 1 ? '异常' : nvl(item.loadWeight, 'kg'));
                if (status === '装载') {
                    tdArr[i].push('装载重量:', arr[i].unusual === 1 ? '异常' : nvl(item.weight, 'kg'));
                } else if (status === '卸载') {
                    tdArr[i].push('卸载重量:', arr[i].unusual === 1 ? '异常' : nvl(item.weight, 'kg'));
                }
                tdArr[i].push('载重相对值:', arr[i].unusual === 1 ? '异常' : nvl(item.weightAd, ''));
                tdArr[i].push('原始AD值:', arr[i].unusual === 1 ? '异常' : nvl(item.originalAd, ''));
                tdArr[i].push('浮动零点:', arr[i].unusual === 1 ? '异常' : nvl(item.floatAd, ''));
            }
            if (thArr.length > 0 && tdArr.length > 0) {
                content = newTableOperation.buildHoverTable(thArr, tdArr);
            }
        }
        return content
    },
    /**
     * 构建鼠标悬浮单元格生成的气泡的HTML内容
     * @param thArr 表头数组
     * @param tdArr 单元格数组
     * @returns {string} 完整的表格HTML内容
     */
    /**
     * 构建工时传感器表格HTML内容
     * @param arr 传感器数据
     * @returns {string} 完整的表格HTML内容
     */
    buildWorkHourTable: function (arr) {
        var thArr = [];
        var tdArr = [];
        var content = '';

        // 传感器只有1个外设ID且列表显示值为异常时，列表中该传感器数据直接显示异常，不弹框
        if (arr && arr.length === 1 && arr[0].unusual === 1) {
            return '';
        }

        if (arr && arr.length > 0) {
            arr = arr.sort(function (x, x2) {
                return x.id - x2.id;
            });

            for (var i = 0; i < arr.length; i++) {
                tdArr.push([]);
                var item = arr[i];

                var detectWay = '';
                if (item.workInspectionMethod === 0) {
                    detectWay = '电压比较式';
                } else if (item.workInspectionMethod === 1) {
                    detectWay = '油耗阈值式';
                } else if (item.workInspectionMethod === 2) {
                    detectWay = '油耗波动式';
                }

                var status = '';
                if (item.workInspectionMethod === 0 || item.workInspectionMethod === 1) {
                    if (item.workingPosition === 0) {
                        status = '停机';
                    } else if (item.workingPosition === 1) {
                        status = '工作';
                    } else if (item.workingPosition === 2 && item.workInspectionMethod === 1) {
                        status = '待机'
                    }
                } else if (item.workInspectionMethod === 2) {
                    if (item.workingPosition === 0) {
                        status = '停机';
                    } else {
                        status = '非停机';
                    }
                }

                thArr.push(item.id.toString(16) + '工时传感器');
                tdArr[i].push('工时检测方式:', arr[i].unusual === 1 ? '异常' : detectWay);
                tdArr[i].push('工作状态:', arr[i].unusual === 1 ? '异常' : status);
                tdArr[i].push('状态持续时长:', arr[i].unusual === 1 ? '异常' : nvl(item.continueTime, 's'));
                tdArr[i].push('波动值:', arr[i].unusual === 1 ? '异常' : nvl(item.waveValue, item.workInspectionMethod === 0 ? 'V' : 'L/h'));
                tdArr[i].push('检测数据:', arr[i].unusual === 1 ? '异常' : nvl(item.checkData, ''));
            }
            if (thArr.length > 0 && tdArr.length > 0) {
                content = newTableOperation.buildHoverTable(thArr, tdArr);
            }
        }
        return content
    },
    /**
     * 构建位置气泡内容
     * @param str 位置字符串
     * @returns {*}
     */
    buildPositionDescriptionTable: function (str) {
        return str;
    },
    /**
     * 构建报警类型气泡内容
     * @param str 位置字符串
     * @returns {*}
     */
    buildAlarmTypeTable: function (str) {
        return str;
    },
    /**
     * 构建胎压传感器表格HTML内容
     * @param obj 传感器数据
     * @returns {string} 完整的表格HTML内容
     */
    buildTyreInfosTable: function (obj) {
        var thArr;
        var tdArr;
        var content = '';

        // 传感器只有1个外设ID且列表显示值为异常时，列表中该传感器数据直接显示异常，不弹框
        if (obj && obj.unusual === 1) {
            return '';
        }

        if (obj !== null && obj !== undefined && obj.list !== undefined && obj.list !== null) {
            var arr = obj.list.sort(function (x, x2) {
                return x.number - x2.number;
            });
            if (arr && arr.length > 0) {
                for (var j = 0; j < arr.length; j += 5) {
                    var rightIndex = Math.min(j + 5, arr.length);
                    thArr = [];
                    tdArr = [];
                    for (var i = j; i < rightIndex; i++) {
                        tdArr.push([]);
                        var item = arr[i];
                        thArr.push((item.number + 1).toString() + '号轮胎');
                        tdArr[i - j].push('轮胎气压:', obj.unusual === 1 ? '异常' : nvl(item.pressure, 'bar'));
                        tdArr[i - j].push('轮胎胎温:', obj.unusual === 1 ? '异常' : nvl(item.temperature, '°C'));
                        tdArr[i - j].push('电池电量:', obj.unusual === 1 ? '异常' : nvl(item.electric, '%'));

                    }
                    if (thArr.length > 0 && tdArr.length > 0) {
                        content += newTableOperation.buildHoverTable(thArr, tdArr);
                    }
                }
            }
        }
        return content
    },

    /**
     * 终端IO状态表格HTML内容
     * @param id 监控对象id
     * @returns {string} 完整的表格HTML内容
     * */
    buildTerminalIOStatus: function (id) {
        let content = '';
        $.ajax({
            type: "POST",
            url: "/clbs/v/monitoring/getIoSignalInfo",
            dataType: "json",
            data: {monitorId: id, type: 'terminalIo'}, //terminalIo:终端IO; sensorIo:传感器IO,
            async: false,
            success: function (data) {
                if(!data.success){
                    content = '获取终端IO状态失败'
                }
                if (data.obj && data.obj.length === 1 && data.obj[0].ioData && data.obj[0].ioData.length > 0) {
                    for (var i = 0; i < data.obj[0].ioData.length; i++) {
                        var item = data.obj[0].ioData[i];
                        if(item.ioStatusName !== '无接口'){
                            content += `<p style="margin: 0">${item.ioName}:  ${item.ioStatusName}</p>`
                        }
                    }
                }
                if(data.obj && data.obj.length === 0){
                    content = '暂无数据'
                }
            }
        });

        return `<div style="padding: 0 5px">${content}</div>`;
    },

    /**
     * 查看从业人员详情内容
     */
    professionalsDetail: function (ele) {
        var $td = $(ele).closest('td');
        var monitorId = $td.attr('id').split('_')[1];
        json_ajax("POST", '/clbs/v/monitoring/getRiskProfessionalsInfo', "json", true, {vehicleId: monitorId}, function (value) {
            realScrollIndex = 0;
            realScrollNum = 0;
            realLockType = false;
            var html = '';
            $('#professionalsInfoScrollCont').removeAttr('style');
            if (value.success && value.obj.length) {
                var data = value.obj;
                driverLists = data;

                $('#professionalsInfoScrollCont').css('width', realInfoScrollBoxW * data.length + 'px');
                realScrollNum = data.length;
                var d = new Date();
                var radomNum = d.getTime();
                var icCardEndDateStr = '';
                for (var i = 0; i < data.length; i++) {
                    if (data[i]) {
                        if (data[i].icCardEndDateStr) {
                            icCardEndDateStr = data[i].icCardEndDateStr;
                        }
                        html += '<div class="col-md-12 singleInfoCont" style="width:' + realInfoScrollBoxW + 'px;">' +
                            '<div class="col-md-2 driverPhoto">' +
                            '<img src=' + activeSafety.handleImgSrc(data[i].photograph) + '?t=' + radomNum + '>' +
                            '</div>' +
                            '<div class="col-md-10 infoCont">' +
                            '<div class="col-md-5">司机姓名：' + activeSafety.handleHtml(data[i].name) + '</div>' +
                            '<div class="col-md-7">岗位类型：' + activeSafety.handleHtml(data[i].type) + '</div>' +
                            '<div class="col-md-5">手机号：' + activeSafety.handleHtml(data[i].phone) + '</div>' +
                            '<div class="col-md-7">身份证号：' + activeSafety.handleHtml(data[i].identity) + '</div>' +
                            '<div class="col-md-12">所属企业：' + activeSafety.handleHtml(data[i].groupName) + '</div>' +
                            '<div class="col-md-12">从业资格证号：' + activeSafety.handleHtml(data[i].cardNumber) + '</div>' +
                            '<div class="col-md-12">从业资格类别：' + activeSafety.handleHtml(data[i].qualificationCategory) + '</div>' +
                            '<div class="col-md-12">发证机关：' + activeSafety.handleHtml(data[i].icCardAgencies) + '</div>' +
                            '<div class="col-md-12">有效期至：' + activeSafety.handleHtml(data[i].icCardEndDateStr) + '</div>' +
                            '</div>' +
                            '</div>';
                    } else {
                        html += '<div class="col-md-12 singleInfoCont" style="width:' + realInfoScrollBoxW + 'px;">' +
                            '<div class="col-md-2 driverPhoto">' +
                            '<img src="/clbs/resources/img/peoplems.png"/>' +
                            '</div>' +
                            '<div class="col-md-10 infoCont">' +
                            '<div class="col-md-5">司机姓名：未知</div>' +
                            '<div class="col-md-7">岗位类型：未知</div>' +
                            '<div class="col-md-5">手机号：未知</div>' +
                            '<div class="col-md-7">身份证号：未知</div>' +
                            '<div class="col-md-12">所属企业：未知</div>' +
                            '<div class="col-md-12">从业资格证号：未知</div>' +
                            '<div class="col-md-12">从业资格类别：未知</div>' +
                            '<div class="col-md-12">发证机关：未知</div>' +
                            '<div class="col-md-12">有效期至：未知</div>' +
                            '</div>' +
                            '</div>';
                    }
                }
            } else {
                layer.msg('未绑定从业人员');
                return;
            }
            //司机ic卡信息
            if (data.length > 0 && data[0] && data[0].lockType === 1) {//realLockType:1代表插卡
                realLockType = true;
            }

            $('#professionalsDetail .nextBtnBox').off().click(function () {
                // if (realLockType) return;
                newTableOperation.scrollLeftAnimate()
            });
            $('#professionalsDetail .prevBtnBox').off().click(function () {
                // if (realLockType) return;
                newTableOperation.scrollRightAnimate()
            });

            $('#professionalsInfoScrollCont').html(html);
            $('#professionalsDetail').modal('show');
        })
    },
    scrollLeftAnimate: function () {
        if (realScrollIndex < realScrollNum - 1) {
            realScrollIndex++;
            $('#professionalsInfoScrollCont').css('left', '-' + realScrollIndex * realInfoScrollBoxW + 'px')
        }

    },
    scrollRightAnimate: function () {
        if (realScrollIndex > 0) {
            realScrollIndex--;
            $('#professionalsInfoScrollCont').css('left', '-' + realScrollIndex * realInfoScrollBoxW + 'px');
        }
    },
    /**
     * 构建气泡表格内容
     * @param thArr 表头数组
     * @param tdArr 单元格数组
     * @returns {string} 表格HTML
     */
    buildHoverTable: function (thArr, tdArr) {
        var table = '<table class="hover-table">$0$1</table>';
        var thead = '<thead><tr>$0</tr></thead>';
        var tbody = '<tbody>$0</tbody>';
        var th = '<th colspan="2">$0</th>';
        var td = '<td>$0</td>';

        thead = thead.replace('$0', thArr.map(function (x) {
            return th.replace('$0', x);
        }).join(''));
        var tbodyStr = '';
        if (tdArr.length === 0) {
            return '';
        }
        for (var j = 0; j < tdArr[0].length; j += 2) {
            var trStr = '<tr>';
            for (var k = 0; k < tdArr.length; k++) {
                trStr += td.replace('$0', tdArr[k][j]);
                trStr += td.replace('$0', tdArr[k][j + 1]);
            }
            trStr += '</tr>';
            tbodyStr += trStr;
        }
        tbody = tbody.replace('$0', tbodyStr);
        table = table.replace('$0', thead).replace('$1', tbody);

        return table;
    },
    /**
     * 传感器IO图标点击
     */
    sensorIOClick: function (ele) {
        var $td = $(ele).closest('td');
        var monitorId = $td.attr('id').split('_')[1];
        $.ajax({
            type: "POST",
            url: "/clbs/v/monitoring/getIoSignalInfo",
            dataType: "json",
            async: true,
            data: {monitorId: monitorId, type: 'sensorIo'},
            success: function (data) {
                if (!data.success) {
                    layer.msg('获取传感器IO状态失败');
                    return;
                }
                // 分别针对IO采集1和IO采集2
                var $content1 = $('#ioModalSensorContent1');
                $content1.empty();
                var $content2 = $('#ioModalSensorContent2');
                $content2.empty();
                $('#sensorIOModalLi1').addClass('active');
                $('#sensorIOModalLi2').removeClass('active');
                $('#sensorIOModalTab1').addClass('active');
                $('#sensorIOModalTab2').removeClass('active');
                if (data.obj && data.obj.length > 0) {
                    var trTmpl = '<tr><td>$0</td><td>$1</td></tr>';
                    var sensor1 = data.obj.find(function (x) {
                        return x.id === 0x91;
                    });
                    var sensor2 = data.obj.find(function (x) {
                        return x.id === 0x92;
                    });
                    if (sensor1 !== undefined && sensor1.ioData !== undefined && sensor1.ioData !== null) {
                        for (var i = 0; i < sensor1.ioData.length; i++) {
                            var tr = '';
                            var item = sensor1.ioData[i];
                            tr = trTmpl.replace('$0', item.ioName + '：');
                            tr = tr.replace('$1', item.ioStatusName);
                            $content1.append($(tr));
                        }
                    }

                    if (sensor2 !== undefined && sensor2.ioData !== undefined && sensor2.ioData !== null) {
                        for (var j = 0; j < sensor2.ioData.length; j++) {
                            var tr2 = '';
                            var item2 = sensor2.ioData[j];
                            tr2 = trTmpl.replace('$0', item2.ioName + '：');
                            tr2 = tr2.replace('$1', item2.ioStatusName);
                            $content2.append($(tr2));
                        }
                    }
                }

                $('#ioModalSensor').modal('show');
            }
        });
    },
};

var dataTableOperation = {
    rowUpdateState: {'state': true, 'alarm': false, 'obd': false},
    //车辆上下线(声音,闪烁)
    onlineVoiceFlash: function () {
        //声音
        if (navigator.userAgent.indexOf('MSIE') >= 0) {
            if ($('#defaultVoice').is(":checked")) {
                document.querySelector('#IEalarmMsg').play();
                if (onlineVoiceSettimeOut) clearTimeout(onlineVoiceSettimeOut);
                onlineVoiceSettimeOut = setTimeout(function () {
                    $("#IEalarmMsg")[0].pause();
                }, 1000)
            } else {
                if ($("#IEalarmMsg").length > 0) {
                    $("#IEalarmMsg")[0].pause();
                }
            }
        } else {
            if ($('#defaultVoice').is(":checked")) {
                $alarmMsgBox.html('<audio id="alarmMsgAutoOff" src="../../file/music/alarm.wav"></audio>');
                document.querySelector('#alarmMsgAutoOff').play();
                if (onlineVoiceSettimeOut) clearTimeout(onlineVoiceSettimeOut);
                onlineVoiceSettimeOut = setTimeout(function () {
                    $("#alarmMsgAutoOff")[0].pause();
                }, 1000)
            } else {
                if ($("#alarmMsgAutoOff").length > 0) {
                    $("#alarmMsgAutoOff")[0].pause();
                }
            }
        }
        //闪烁
        if ($('#flashing').is(":checked")) {
            $('#onlineWaves').show();
            if (onlineSettimeOut) clearTimeout(onlineSettimeOut);
            onlineSettimeOut = setTimeout(function () {
                $('#onlineWaves').hide();
            }, 1000)
        } else {
            $('#onlineWaves').hide();
        }
    },
    //报警信息(数量显示  声音  闪烁)
    realTimeAlarmInfoCalcFn: function () {
        alarmNum++;
        alarmNum = tableAlarm.getState().data.length;
        $("#showAlarmNum").text(alarmNum);
        if (alarmNum > 0) {
            //声音
            if (navigator.userAgent.indexOf('MSIE') >= 0) {
                if ($alarmSoundSpan.hasClass("soundOpen")) {
                    // $alarmMsgBox.html('<embed id="IEalarmMsg" src="../../file/music/alarm.wav" autostart="true"/>');
                    document.querySelector('#IEalarmMsg').play()
                } else {
                    // $alarmMsgBox.html('<embed id="IEalarmMsg" src=""/>');
                }
            } else {
                if ($alarmSoundSpan.hasClass("soundOpen")) {
                    // $alarmMsgBox.html('<audio id="alarmMsgAutoOff" src="../../file/music/alarm.wav" autoplay="autoplay"></audio>');
                    document.querySelector('#alarmMsgAutoOff').play()
                } else {
                    // $alarmMsgBox.html('<audio id="alarmMsgAutoOff" src="../../file/music/alarm.wav"></audio>');
                }
            }
            //闪烁
            if ($alarmFlashesSpan.hasClass("flashesOpen")) {
                $showAlarmWinMark.css("background-position", "0px -134px");
                setTimeout(function () {
                    $showAlarmWinMark.css("background-position", "0px 0px");
                }, 1500)
            } else {
                $showAlarmWinMark.css("background-position", "0px 0px");
            }
            pageLayout.showAlarmWindow();
        }
    },
    //闪烁、提示音、弹窗提醒
    testWebSocket: function () {
        setTimeout(function () {
            if(webSocket.conFlag) {
                webSocket.subscribe(headers, "/user/topic/monitor/platformRemind", function (msg) {
                    var data = JSON.parse(msg.body),
                        brand = data.brand,
                        vehicleId = data.vehicleId,
                        time = new Date(data.warmTime),
                        year = time.getFullYear(),
                        month = time.getMonth() + 1,
                        day = time.getDate(),
                        hours = time.getHours(),
                        minutes = time.getMinutes(),
                        seconds = time.getSeconds();

                    if (month >= 1 && month <= 9) {
                        month = '0' + month;
                    }
                    if (day >= 1 && day <= 9) {
                        day = '0' + day;
                    }
                    if (hours >= 1 && hours <= 9) {
                        hours = '0' + hours;
                    }
                    if (minutes >= 1 && minutes <= 9) {
                        minutes = '0' + minutes;
                    }
                    if (seconds >= 1 && seconds <= 9) {
                        seconds = '0' + seconds;
                    }
                    if (minutes === 0) {
                        minutes = '00';
                    }
                    if (seconds === 0) {
                        seconds = '00';
                    }
                    var warmTime = year + '-' + month + '-' + day + ' ' + hours + ':' + minutes + ':' + seconds;
                    var content = '监控对象：' + brand + ' 在' + warmTime + '产生的报警请及时处理，谢谢！';
                    if (data.blinkingPrompt) { //闪烁
                        $showAlarmWinMark.show();
                        $("#showAlarmWin").hide();
                        $("#callPolice").show();
                        $("#ActiveSafetybtn").show();
                        if ($(".alarmFlashes span").hasClass('flashesOpen-off')) {
                            $showAlarmWinMark.css("background-position", "0px 0px");
                        } else {
                            $showAlarmWinMark.css("background-position", "0px -134px");
                            setTimeout(function () {
                                $showAlarmWinMark.css("background-position", "0px 0px");
                            }, 1500)
                        }
                    }
                    if (data.promptTone) { //提示音
                        if ($(".alarmSound span").hasClass('soundOpen')) {
                            if (navigator.userAgent.indexOf('MSIE') >= 0) {
                                if ($alarmSoundSpan.hasClass("soundOpen")) {
                                    // $alarmMsgBox.html('<embed id="IEalarmMsg" src="../../file/music/alarm.wav" autostart="true"/>');
                                    document.querySelector('#IEalarmMsg').play()
                                } else {
                                    // $alarmMsgBox.html('<embed id="IEalarmMsg" src=""/>');
                                }
                            } else {
                                if ($alarmSoundSpan.hasClass("soundOpen")) {
                                    // $alarmMsgBox.html('<audio id="alarmMsgAutoOff" src="../../file/music/alarm.wav" autoplay="autoplay"></audio>');
                                    document.querySelector('#alarmMsgAutoOff').play()
                                } else {
                                    // $alarmMsgBox.html('<audio id="alarmMsgAutoOff" src="../../file/music/alarm.wav"></audio>');
                                }
                            }
                        } else {
                            document.querySelector('#alarmMsgAutoOff').pause();
                        }
                    }
                    if (data.popupPrompt) { //提示框
                        alarmUrlType = 3;
                        PlatformAlarmUrl3 = '/clbs/r/riskManagement/disposeReport/list?vehicleId=' + vehicleId + '&status=1' + '&brand=' + brand;
                        $('#bomMsgBoxContent').text(content);
                        pagesNav.bomMsgShow();
                    }
                }, "/app/risk/security/subVehicleUser", "a");

            }else{
                dataTableOperation.testWebSocket();
            }
        },1000);
    },
    // 实时更新
    updateRealLocation: function (msg) {
        var data = $.parseJSON(msg.body);
        if (data.desc !== "neverOnline") {
            if (data.desc.msgID === 513) {
                var obj = {};
                obj.desc = data.desc;
                var da = {};
                da.msgHead = data.data.msgHead;
                da.msgBody = data.data.msgBody;
                obj.data = da;
                // 状态信息
                dataTableOperation.updateVehicleStatusInfoTable(obj);
            } else {
                var cid = data.data.msgBody.monitorInfo.monitorId;
                if (crrentSubV.isHas(cid)) {
                    dataTableOperation.updateVehicleStatusInfoTable(data);
                }
            }
        } else {
            var objInfo = treeMonitoring.searchNeverOnline("treeDemo", data.vid)[0];
            if (!objInfo) return;
            var brand = objInfo.name;
            var objType = objInfo.type;
            if (!cancelList.isHas(brand) && crrentSubName.isHas(brand)) {
                if (objType === "vehicle" || objType === "people" || objType === "thing") {
                    var business = (data.business === undefined || data.business === null) ? '' : data.business // 所属企业
                        , assignmentName = data.assignmentName // 所属分组
                        , groupName = data.groupName // 所属企业
                        , objectType = data.objectType == 'default' ? '-' : data.objectType // 对象类型
                        , plateColor = (data.plateColor == 'null' || data.plateColor === null) ? '-' : data.plateColor // 车牌颜色
                        , deviceNumber = data.deviceNumber // 终端号
                        , simNumber = data.simNumber // 终端手机号
                        , professionals = data.professionals == 'null' ? '-' : data.professionals; // 从业人员

                    var row = {
                        sequenceNumber: 0,
                        monitorName: brand,
                        gpsTime: '未上线',
                        uploadTime: '-',
                        assignmentName: assignmentName,
                        groupName: groupName,
                        vehicleType: (objectType == null || objectType === 'null') ? '-' : objectType,
                        plateColorName: plateColor,
                        deviceNumber: deviceNumber,
                        simcardNumber: simNumber,
                        professionalsName: professionals,
                        acc: '',
                        objectState: '',
                        signalState: '',
                        gpsSpeed: '',
                        direction: '',
                        batteryVoltage: '',
                        signalStrength: '',
                        locationType: '',
                        terminalDayMileage: '',
                        sensorDayMileage: '',
                        terminalTotalMileage: '',
                        sensorTotalMileage: '',
                        dayOilWear: '',
                        totalFuelConsumption: '',
                        gpsOil: '',
                        altitude: '',
                        grapherSpeed: '',
                        positionDescription: '',
                        satellitesNumber: '',
                        roadLimitSpeed: '',
                        roadType: '',
                        elecData: null,
                        oilExpend: null,
                        oilMass: null,
                        temperatureSensor: null,
                        temphumiditySensor: null,
                        positiveNegative: null,
                        loadInfos: null,
                        workHourSensor: null,
                        tyreInfos: null,
                        drivingLicenseNo: '',
                        longitude: "",
                        latitude: "",
                        frameNumber: '',// 车架号
                        frameNumberFromDevice: '',// 车架号(终端上传)
                        empty: '',
                        id: objInfo.id,
                    };
                    dataTableOperation.updateRow('#realTimeStateTable', realTimeSet, row, 'state', null);
                    // dataTableOperation.dataTableList(stateName, row, "realTimeStateTable", 'state', null);
                    //更新obd列表

                    var obdRow = {
                        sequenceNumber: 0,
                        id: objInfo.id,
                        monitorName: brand,
                        serviceGpsTime: '未上线',
                        serviceSystemTime: '-',
                        groupName: groupName
                    };
                    for (var i = 0; i < newTableOperation.obdColumns.length; i++) {
                        var key = newTableOperation.obdColumns[i].name;
                        if (obdRow[key] === undefined) {
                            obdRow[key] = '';
                        }
                    }

                    dataTableOperation.dataTableList(obdName, obdRow, "obdInfoTable", 'obd', null);
                }
            }
        }
    },
    // 状态信息数据更新
    updateVehicleStatusInfoTable: function (position) {
        var msgBody = position.data.msgBody;
        var msgDesc = position.desc;
        var monitorInfo = msgBody.monitorInfo;

        var vid = monitorInfo.monitorId;

        // 监控对象名称
        var mObjectName = monitorInfo.monitorName;
        // 定位时间
        var gpsTime = msgBody.gpsTime;
        var serviceGpsTime = 20 + gpsTime.substring(0, 2)
            + "-" + gpsTime.substring(2, 4)
            + "-" + gpsTime.substring(4, 6)
            + " " + gpsTime.substring(6, 8)
            + ":" + gpsTime.substring(8, 10)
            + ":" + gpsTime.substring(10, 12);
        // 服务器时间
        var uploadtime = msgBody.uploadtime;
        var serviceSystemTime = 20 + uploadtime.substring(0, 2)
            + "-" + uploadtime.substring(2, 4)
            + "-" + uploadtime.substring(4, 6)
            + " " + uploadtime.substring(6, 8)
            + ":" + uploadtime.substring(8, 10)
            + ":" + uploadtime.substring(10, 12);
        // 所属分组
        var groupName = monitorInfo.assignmentName;
        // 所属企业
        var business = monitorInfo.groupName;
        // 对象类型
        var vehicleType = monitorInfo.vehicleType;
        // 车牌颜色
        var plateColor = monitorInfo.plateColorName;
        if (plateColor === "null" || plateColor == null) {
            plateColor = '-';
        }
        // 终端号
        var deviceId = monitorInfo.deviceNumber;
        // 终端手机号
        var sNumber = monitorInfo.simcardNumber;
        // 从业人员
        var professionalsName = monitorInfo.professionalsName ? monitorInfo.professionalsName : '';
        // ACC
        var acc = msgBody.acc;
        if ((acc + "").length === 1) {
            acc = (acc === 0 ? "关" : "开");
        } else if (acc === 21) {
            acc = "点火静止";
        } else if (acc === 16) {
            acc = "熄火拖车";
        } else if (acc === 0x1A) {
            acc = "熄火假拖车";
        } else if (acc === 11) {
            acc = "熄火静止";
        } else if (acc === 12) {
            acc = "熄火移动";
        } else if (acc === 22) {
            acc = "点火移动";
        } else if (acc === 41) {
            acc = "无点火静止";
        } else if (acc === 42) {
            acc = "无点火移动";
        } else {
            layer.msg("ACC状态异常");
            return;
        }
        // 信号状态
        var signalStateFlag = msgBody.signalState;
        var signalStateFlagDetails;
        if (signalStateFlag != null && signalStateFlag !== 0) {
            signalStateFlagDetails = dataTableOperation.signalStateFlagAnalysis(signalStateFlag);//获取详情信息
            if (signalStateFlagDetails !== "" && signalStateFlagDetails.endsWith(",")) {
                signalStateFlagDetails = signalStateFlagDetails.substring(0, signalStateFlagDetails.length - 1);//删除字符串最后的“,”
            }
        } else {
            signalStateFlagDetails = '-';
        }

        // 速度
        var speed = msgBody.gpsSpeed;
        // 方向
        var angle = msgBody.direction;
        // 电池电压
        var batteryVoltage = msgBody.batteryVoltage;
        // 信号强度
        var signalStrength = msgBody.signalStrength;
        // 定位方式
        var locationType = msgBody.locationPattern;
        if (locationType === 0) {
            locationType = "卫星+基站定位";
        } else if (locationType === 1) {
            locationType = "基站定位";
        } else if (locationType === 2) {
            locationType = "卫星定位";
            // 当定位方式为“卫星定位”时，信号强度使用 gpsAttachInfoList 中 signalIntensity 字段
            signalStrength = '-';
            if (msgBody.gpsAttachInfoList) {
                for (var i = 0; i < msgBody.gpsAttachInfoList.length; i++) {
                    var item = msgBody.gpsAttachInfoList[i];
                    if (item.signalIntensity !== undefined) {
                        signalStrength = item.signalIntensity;
                        break;
                    }
                }
            }
        } else if (locationType === 3) {
            locationType = "WIFI+基站定位";
        } else if (locationType === 4) {
            locationType = "卫星+WIFI+基站定位";
        } else {
            locationType = "-";
        }
        // 总里程
        var allMileage;
        if (msgBody.mileageSensor !== undefined && msgBody.mileageSensor !== null) {
            allMileage = msgBody.mileageSensor.mileage == null ? 0.0 : msgBody.mileageSensor.mileage;
        } else {
            allMileage = msgBody.gpsMileage == null ? 0.0 : msgBody.gpsMileage;
        }
        // 当日油耗
        var todayFuelConsumption = msgBody.dayOilWear;
        // 修改单位p
        todayFuelConsumption = parseFloat(todayFuelConsumption);
        todayFuelConsumption = todayFuelConsumption.toFixed(2);
        // 总油耗
        var allFuelConsumption;
        if (msgBody.oilExpend !== undefined && msgBody.oilExpend != null && msgBody.oilExpend.length > 0) {
            allFuelConsumption = msgBody.oilExpend[0].allExpend == null ? 0.0 : msgBody.oilExpend[0].allExpend;
        } else {
            allFuelConsumption = msgBody.gpsOil == null ? 0.0 : msgBody.gpsOil;
        }
        // 修改单位p
        if (typeof allFuelConsumption === 'number') {
            allFuelConsumption = allFuelConsumption.toFixed(2);
        }
        // 油量
        var gpsOil = msgBody.gpsOil;
        // 高程
        var altitude = msgBody.altitude;
        // 记录仪速度
        var grapherSpeed = msgBody.grapherSpeed;
        // 位置信息
        var address = msgBody.positionDescription;
        // 监控对象ID
        var mObjectId = msgBody.monitorInfo.monitorId;
        //路网限速
        var roadLimitSpeed = msgBody.roadLimitSpeed;
        // 道路类型
        var roadType = msgBody.roadType;
        var roadTypeStr;
        if (roadType === undefined || roadType === null || roadType === "") {
            roadTypeStr = "";
        } else {
            roadTypeStr = dataTableOperation.roadTypeJudge(roadType);
        }

        if (msgBody.durationTime !== undefined && msgBody.durationTime !== null) {
            var speeds = Number(msgBody.gpsSpeed) <= 1 ? "停止(" + dataTableOperation.formatDuring(msgBody.durationTime) + ")" : "行驶(" + dataTableOperation.formatDuring(msgBody.durationTime) + ")"
        } else {
            var speeds = Number(msgBody.gpsSpeed) <= 1 ? "停止" : "行驶"
        }
        //添加行驶状态
        var drivingStateValue = drivingState.get(msgDesc.deviceId);
        if (drivingStateValue !== null && drivingStateValue !== undefined) {
            speeds = drivingStateValue + speeds;
            drivingState.remove(msgDesc.deviceId);
        }

        /*更新OBD数据列表*/
        var obdObjData = JSON.parse(msgBody.obdObjStr);

        var obdRow = {
            sequenceNumber: 0,
            id: vid,
            monitorName: mObjectName,
            serviceGpsTime: serviceGpsTime,
            serviceSystemTime: serviceSystemTime,
            groupName: ((business === 'null' || business === null) ? '-' : business)
        };
        for (var i = 0; i < newTableOperation.obdColumns.length; i++) {
            var key = newTableOperation.obdColumns[i].name;
            if (obdRow[key] === undefined) {
                obdRow[key] = obdObjData[key];
            }
        }
        dataTableOperation.updateRow('#obdInfoTable', realTimeSet, obdRow, 'obd', null);

        var udf = function (value) {
            if (value === undefined) {
                return null;
            }
            return value;
        };
        // 更新状态信息
        var row = {
            sequenceNumber: 0,
            monitorName: mObjectName,
            gpsTime: serviceGpsTime,
            uploadTime: serviceSystemTime,
            assignmentName: ((groupName === 'null' || groupName === null) ? '-' : groupName),
            groupName: ((business === 'null' || business === null) ? '-' : business),
            vehicleType: ((vehicleType === 'null' || vehicleType == null) ? '-' : vehicleType),
            plateColorName: ((plateColor === 'null' || plateColor === null) ? '-' : plateColor),
            deviceNumber: deviceId,
            simcardNumber: sNumber,
            professionalsName: ((professionalsName === 'null' || !professionalsName) ? '' : professionalsName),
            acc: acc,
            objectState: speeds,
            signalState: signalStateFlagDetails,

            gpsSpeed: speed,
            direction: dataTableOperation.toDirectionStr(angle),
            batteryVoltage: ((batteryVoltage === 'null' || batteryVoltage === null || batteryVoltage === undefined) ? '-' : batteryVoltage),
            signalStrength: ((signalStrength === 'null' || signalStrength === null || signalStrength === undefined || signalStrength === -1) ? '-' : signalStrength),
            locationType: locationType,
            terminalDayMileage: msgBody.dayMileage < 0 ? '-' : msgBody.dayMileage,
            sensorDayMileage: msgBody.dayMileageSensor,
            terminalTotalMileage: msgBody.gpsMileage,
            sensorTotalMileage: msgBody.mileageSensor && msgBody.mileageSensor.mileage,
            dayOilWear: todayFuelConsumption,
            totalFuelConsumption: allFuelConsumption,
            gpsOil: gpsOil,
            altitude: altitude,
            grapherSpeed: grapherSpeed,
            positionDescription: ((address === 'null' || address === null) ? '未定位' : address),
            elecData: udf(msgBody.elecData),
            oilExpend: udf(msgBody.oilExpend),
            oilMass: udf(msgBody.oilMass),
            temperatureSensor: udf(msgBody.temperatureSensor),
            temphumiditySensor: udf(msgBody.temphumiditySensor),
            positiveNegative: udf(msgBody.positiveNegative),
            loadInfos: udf(msgBody.loadInfos),
            workHourSensor: udf(msgBody.workHourSensor),
            tyreInfos: udf(msgBody.tyreInfos),
            satellitesNumber: msgBody.satellitesNumber,
            longitude: msgBody.longitude,
            latitude: msgBody.latitude,
            roadLimitSpeed: roadLimitSpeed,
            roadType: roadTypeStr,
            frameNumber: msgBody.frameNumber,// 车架号
            frameNumberFromDevice: msgBody.frameNumberFromDevice,// 车架号(终端上传)
            empty: '',
            id: mObjectId,
        };
        dataTableOperation.updateRow('#realTimeStateTable', realTimeSet, row, 'state', null);
        // 更新信息弹窗信息
        var testInfo = [];//初始标注数据
        if (msgBody.protocolType === '5') {//北斗天地
            testInfo.push(parseDate2Str(msgBody.gpsTime));//时间
            testInfo.push(monitorInfo.monitorName);
            testInfo.push(((monitorInfo.assignmentName === 'null' || monitorInfo.assignmentName == null) ? '-' : monitorInfo.assignmentName));
            testInfo.push(monitorInfo.deviceNumber);
            testInfo.push(monitorInfo.simcardNumber);
            testInfo.push(msgBody.batteryVoltage);
            testInfo.push(msgBody.signalStrength);
            testInfo.push(msgBody.gpsSpeed);
            testInfo.push(msgBody.altitude);
            testInfo.push(msgBody.latitude);
            testInfo.push(msgBody.longitude);
            testInfo.push("people");
            testInfo.push(monitorInfo.monitorId);

            carAddress = msgBody.formattedAddress;
            testInfo.push(angle);//角度
            testInfo.push(msgBody.stateInfo);//状态信息
            testInfo.push((monitorInfo.monitorType == null || monitorInfo.monitorType === 'null' || monitorInfo.monitorType === undefined) ? '0' : monitorInfo.monitorType);//监控对象类型
            testInfo.push(monitorInfo.monitorIcon);//监控对象图标
            testInfo.push(msgBody.dayMileage);//当日里程
            // 道路类型
            if (roadType == null || roadType === "") {
                testInfo.push("");
            } else {
                testInfo.push(dataTableOperation.roadTypeJudge(roadType));
            }
            //路网限速
            testInfo.push(msgBody.roadLimitSpeed);
            //信息框数据调用
            amapOperation.completeEventHandler(testInfo);
        } else {//车和物
            var monitorId = monitorInfo.monitorId;

            var status = +msgBody.status;
            var longitudeAndLatitude = [];
            longitudeAndLatitude.push(((status & (1 << 2)) === 0 ? "北纬：" : "南纬：") + msgBody.latitude);
            longitudeAndLatitude.push(((status & (1 << 3)) === 0 ? "东经：" : "西经：") + msgBody.longitude);
            var operatingState = (status & ((1 << 4))) === 0 ? '运营' : '停运';// 运营状态
            var oilStatus = (status & ((1 << 10))) === 0 ? '车辆油路正常' : '车辆油路断开';//车辆油路
            var circuitStatus = (status & ((1 << 11))) === 0 ? '车辆电路正常' : '车辆电路断开';//车辆电路
            var carDoorStatus = (status & ((1 << 12))) === 0 ? '车门解锁' : '车门加锁';//车门状态

            var waybill = '';// 电子运单
            var people = '';// 从业人员
            var cardID = '';// 身份证
            var cAName = '';// 发证机关
            var peopleIDcard = '';// 从业资格证号
            if (waybillAndPractitionersInfo.containsKey(monitorId)) {
                var aboutInfo = waybillAndPractitionersInfo.get(monitorId);
                waybill = aboutInfo.waybill;
                if (aboutInfo.practitioners) {
                    cardID = aboutInfo.practitioners.driverIdentity;
                    peopleIDcard = aboutInfo.practitioners.certificationID;
                    cAName = aboutInfo.practitioners.cAName;
                }
            }
            var mapPopupInfo = {
                'gpsTime': parseDate2Str(msgBody.gpsTime),// 更新时间
                'monitorName': monitorInfo.monitorName,// 监控对象名称
                'vehicleType': monitorInfo.vehicleType,// 对象类型
                'assignmentName': ((monitorInfo.assignmentName === 'null' || monitorInfo.assignmentName == null) ? '-' : monitorInfo.assignmentName),// 所属分组
                'deviceNumber': monitorInfo.deviceNumber,// 终端号
                'simcardNumber': monitorInfo.simcardNumber,// 终端手机号
                'travelState': (msgBody.gpsSpeed <= 1 ? '停止' : '行驶'),// 行驶状态
                'gpsSpeed': msgBody.gpsSpeed,// GPS速度
                'formattedAddress': msgBody.positionDescription,// 位置
                'groupName': monitorInfo.groupName,// 所属企业
                'monitorType': (monitorInfo.monitorType == null || monitorInfo.monitorType === 'null' || monitorInfo.monitorType === undefined) ? '0' : monitorInfo.monitorType,// 监控对象类型
                'acc': acc,// ACC状态
                'todayMileage': parseFloat(msgBody.dayMileage),// 当日里程
                'longitudeAndLatitude': longitudeAndLatitude,// 经纬度(纬度,经度)
                'allMileage': allMileage,// 总里程
                'direction': msgBody.direction,// 方向
                'grapherSpeed': ((msgBody.grapherSpeed === 'null' || msgBody.grapherSpeed == null) ? 0 : msgBody.grapherSpeed),// 记录仪速度
                'altitude': msgBody.altitude,// 高程
                'electronicWaybill': waybill,// 电子运单
                'professionalsName': ((!monitorInfo.professionalsName || monitorInfo.professionalsName === 'null') ? '' : monitorInfo.professionalsName),// 从业人员
                'idCardNo': cardID,// 身份证号
                'identity': peopleIDcard,// 从业资格证号
                // 'identity': msgBody.identity,// 驾驶证号
                'tcb': cAName,// 发证机构
                'batteryVoltage': msgBody.batteryVoltage,// 电池电压
                'signalStrength': msgBody.signalStrength,// 信号强度
                'monitorTypeName': 'vehicle',// 监控对象类型名
                'monitorId': monitorId,// 监控对象id
                'angle': angle,// 角度
                'roadLimitSpeed': msgBody.roadLimitSpeed,// 路网限速
                'monitorIcon': monitorInfo.monitorIcon,// 监控对象图标
                'roadType': msgBody.roadType ? dataTableOperation.roadTypeJudge(msgBody.roadType) : '',// 道路类型
                'msgID': (msgDesc.msgID === 0x0201 ? 1 : 0),//
                'status': msgBody.status,//
                'alarmTypeId': '报警类型',// 报警类型
                'deviceType': monitorInfo.deviceType,// 终端类型
                'signalStateFlagDetails': signalStateFlagDetails === undefined ? "-" : signalStateFlagDetails,// 信号状态
                'stateInfo': msgBody.stateInfo,// 状态
                'protocolType': msgBody.protocolType,// 协议类型
                'plateColor': plateColor,// 车牌颜色
                'alarmType': alarmInfoList.get(monitorId) ? alarmInfoList.get(monitorId) : '',// 报警名称
                'operatingState': operatingState,// 运营状态
                'oilStatus': oilStatus,// 油路状态
                'circuitStatus': circuitStatus,// 电路状态
                'carDoorStatus': carDoorStatus,// 车门状态
            };

            testInfo.push(monitorInfo.monitorName);//监控对象
            testInfo.push(monitorInfo.vehicleType);//对象类型
            testInfo.push(monitorInfo.assignmentName);//分组
            testInfo.push(monitorInfo.deviceNumber);//终端号
            testInfo.push(monitorInfo.simcardNumber);//终端手机号
            testInfo.push(msgBody.dayMileage);//当日里程
            testInfo.push((msgBody.gpsMileage === '' || msgBody.gpsMileage === null || msgBody.gpsMileage === undefined) ? 0 : msgBody.gpsMileage);//总里程
            testInfo.push(msgBody.gpsSpeed);//速度
            testInfo.push(acc);//acc
            testInfo.push((msgBody.gpsSpeed <= 1 ? '停止' : '行驶'));//行驶状态
            testInfo.push(parseDate2Str(msgBody.gpsTime));//时间
            testInfo.push(msgBody.latitude);//纬度
            testInfo.push(msgBody.longitude);//经度
            testInfo.push(monitorInfo.monitorId);//监控对象id
            testInfo.push((!monitorInfo.professionalsName || monitorInfo.professionalsName === 'null') ? '' : monitorInfo.professionalsName);//从业人员
            testInfo.push(plateColor);//车辆颜色
            testInfo.push(dataTableOperation.toDirectionStr(msgBody.direction));//方向(东南西北等)
            testInfo.push(msgBody.positionDescription);//位置
            testInfo.push((msgDesc.msgID === 0x0201 ? 1 : 0));
            testInfo.push(msgBody.status);
            testInfo.push('报警类型');//报警类型
            testInfo.push('');
            testInfo.push(msgBody.altitude);//高程
            testInfo.push(((msgBody.grapherSpeed === 'null' || msgBody.grapherSpeed == null) ? 0 : msgBody.grapherSpeed));//记录仪速度
            testInfo.push(msgBody.direction);//角度
            testInfo.push(monitorInfo.monitorIcon);//图标
            testInfo.push(((monitorInfo.groupName === 'null' || monitorInfo.groupName == null) ? '-' : monitorInfo.groupName));//所属企业
            testInfo.push(monitorInfo.deviceType);//终端类型
            testInfo.push(signalStateFlagDetails === undefined ? "-" : signalStateFlagDetails);
            testInfo.push(msgBody.stateInfo);//状态
            testInfo.push((monitorInfo.monitorType == null || monitorInfo.monitorType === 'null' || monitorInfo.monitorType === undefined) ? '0' : monitorInfo.monitorType);//监控对象类型
            testInfo.push(msgBody.protocolType);//协议类型
            // 道路类型
            if (roadType == null || roadType === "") {
                testInfo.push("");
            } else {
                testInfo.push(dataTableOperation.roadTypeJudge(roadType));
            }
            //路网限速
            testInfo.push(msgBody.roadLimitSpeed);
            //信息框数据调用
            // console.warn(testInfo, mapPopupInfo, 'testInfo, mapPopupInfo');
            // dataTableOperation.throttle(amapOperation.completeEventHandler(testInfo, mapPopupInfo), 1000);
            amapOperation.completeEventHandler(testInfo, mapPopupInfo)
        }
    },

    // 节流
    throttle: function (fn, wait) {
        var pre = Date.now();
        return function () {
            var context = this;
            var args = arguments;
            var now = Date.now();
            if (now - pre >= wait) {
                fn.apply(context, args);
                pre = Date.now();
            }
        }
    },

    //道路类型判断
    roadTypeJudge: function (num) {
        var roadTypeStr;
        switch (num) {
            case 1:
                roadTypeStr = "高速路";
                break;
            case 2:
                roadTypeStr = "都市高速路";
                break;
            case 3:
                roadTypeStr = "国道";
                break;
            case 4:
                roadTypeStr = "省道";
                break;
            case 5:
                roadTypeStr = "县道";
                break;
            case 6:
                roadTypeStr = "乡村道路";
                break;
            case 7:
                roadTypeStr = "其他道路";
                break;
        }
        return roadTypeStr;
    },
    dateFormat: function (inputTime) {
        var date = new Date(inputTime * 1000);
        var y = date.getFullYear();
        var m = date.getMonth() + 1;
        m = m < 10 ? ('0' + m) : m;
        var d = date.getDate();
        d = d < 10 ? ('0' + d) : d;
        var h = date.getHours();
        h = h < 10 ? ('0' + h) : h;
        var minute = date.getMinutes();
        var second = date.getSeconds();
        minute = minute < 10 ? ('0' + minute) : minute;
        second = second < 10 ? ('0' + second) : second;
        return y + '-' + m + '-' + d + ' ' + h + ':' + minute + ':' + second;
    },
    formatDuring: function (mss) {
        var days = parseInt(mss / (1000 * 60 * 60 * 24));
        var hours = parseInt((mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        var minutes = parseInt((mss % (1000 * 60 * 60)) / (1000 * 60));
        var seconds = parseInt((mss % (1000 * 60)) / 1000);
        if (days === 0 && hours === 0 && minutes === 0) {
            return seconds + " 秒 ";
        } else if (days === 0 && hours === 0 && minutes !== 0) {
            return minutes + " 分 " + seconds + " 秒 ";
        } else if (days === 0 && hours !== 0) {
            return hours + " 小时 " + minutes + " 分 " + seconds + " 秒 ";
        } else if (days !== 0) {
            return days + " 天 " + hours + " 小时 " + minutes + " 分 " + seconds + " 秒 ";
        }
    },
    signalStateFlagAnalysis: function (signalStateFlag) {
        if (signalStateFlag === undefined || signalStateFlag === null) {
            return "";
        }
        var signals = ["近光灯", "远光灯", "右转向灯", "左转向灯", "制动", "倒挡", "雾灯", "示廓灯", "喇叭", "空调开",
            "空挡", "缓速器工作", "ABS工作", "加热器工作", "离合器状态"];
        var details = "";
        var testBit = 1;
        for (var i = 0; i < signals.length; i++) {
            if ((signalStateFlag & testBit) === testBit) {
                if (details.length > 0) {
                    details += ",";
                }
                details += signals[i]
            }
            testBit = testBit << 1;
        }
        return details;
    },
    //若有超速报警、异动报警标识则更新状态列表中的行驶状态
    updateStateInfoByAlarm: function (position) {
        var msgBody = position.data.msgBody;
        var msgDesc = position.desc;
        //超速报警状态名称
        var speedAlarmName = "";
        //超速报警状态值
        var speedAlarmFlag = msgBody.speedAlarmFlag;
        if (speedAlarmFlag != null) {
            switch (speedAlarmFlag) {
                case 0:
                    speedAlarmName = "开始超速,";
                    break;
                case 1:
                    speedAlarmName = "持续超速,";
                    break;
                case 2:
                case -2:
                    speedAlarmName = "结束超速,";
                    break;
                case 10:
                    speedAlarmName = "开始夜间超速,";
                    break;
                case 11:
                    speedAlarmName = "夜间持续超速,";
                    break;
                case 12:
                case -12:
                    speedAlarmName = "夜间超速结束,";
                    break;
            }
        }
        //异动报警状态名称
        var exceptionMoveName = "";
        var exceptionMoveFlag = msgBody.exceptionMoveFlag;
        if (exceptionMoveFlag != null) {
            switch (exceptionMoveFlag) {
                case 0:
                    exceptionMoveName = "开始异动,";
                    break;
                case 1:
                    exceptionMoveName = "持续异动,";
                    break;
                case 2:
                    exceptionMoveName = "结束异动,";
                    break;
            }
        }
        if (speedAlarmName === "" && exceptionMoveName === "") { //若没有相关报警信息则删除行驶状态map中数据
            drivingState.remove(msgDesc.deviceId);
        } else { //若有相关报警数据则储存至行驶状态map中并更新状态信息中的行驶状态
            var drivingStateValue = speedAlarmName + exceptionMoveName;
            //先删除原有数据再存储
            drivingState.remove(msgDesc.deviceId);
            drivingState.put(msgDesc.deviceId, drivingStateValue);
            //获取车辆id
            var checkVehicleId = msgDesc.monitorId;
            dataTableOperation.updateDrivingState(drivingStateValue, checkVehicleId);
            if (speedAlarmFlag < 0 || exceptionMoveFlag < 0) {
                return true;
            }
        }
        return false;
    },
    //更新状态信息中的行驶状态
    updateDrivingState: function (drivingStateValue, vid) {
        if (drivingStateValue != null && drivingStateValue !== "" && drivingStateValue !== undefined) {
            if (stateName.indexOf(vid) !== -1) {
                $("#realTimeStateTable").children("tbody").children("tr").each(function () {
                    if ($(this).children("td:nth-child(2)").attr('data-id') === vid) {
                        var existValue = $(this).children("td:nth-child(13)").text().split(",");
                        //获取最后一个元素，及行驶/停止时长，并组装更新行驶状态
                        drivingStateValue = drivingStateValue + existValue[existValue.length - 1];
                        $(this).children("td:nth-child(13)").text(drivingStateValue);
                    }
                });
            }
        }
    },
    //解析报警位置信息
    getAlarmAddress: function (vId, latitude, longitude) {
        var vid = vId;
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
                tableAlarm.updateOptionData({
                    id: vid,
                    alarmPosition: $.isPlainObject(data) ? '未定位' : data
                });
            },
        });
    },
    // 报警记录数据更新
    updateAlarmInfoTable: function (position) {
        var msgBody = position.data.msgBody;
        var msgDesc = position.desc;
        var monitorInfo = position.data.msgBody.monitorInfo;
        var alarmSource = msgBody.alarmSource;
        //更新行驶状态
        var updateAlarmFlag = dataTableOperation.updateStateInfoByAlarm(position);
        if (updateAlarmFlag) {
            return;
        }
        var monitorId = monitorInfo.monitorId;//监控对象id
        var alarmName = msgBody.alarmName;//报警名称
        //判断集合是否为空 (此方法用于地图显示监控对象信息框)
        if (alarmInfoList.isEmpty()) {
            alarmInfoList.put(monitorId, alarmName);
        } else {
            if (alarmInfoList.containsKey(monitorId)) {
                alarmInfoList.remove(monitorId);
                alarmInfoList.put(monitorId, alarmName);
            } else {
                alarmInfoList.put(monitorId, alarmName);
            }
        }

        var monitorName = monitorInfo.monitorName;//监控对象
        var groupName = monitorInfo.assignmentName;//分组名称
        var deviceNumber = monitorInfo.deviceNumber;//终端编号
        var simcardNumber = monitorInfo.simcardNumber;//终端手机号
        var alarmNumber = msgBody.globalAlarmSet;//报警编号
        var msgSN = msgBody.swiftNumber;//流水号
        var earlyAlarmStartTime = msgBody.earlyAlarmStartTimeStr;// 报警最早开始时间: 实时监控跳转查询页面使用

        var plateColor = getPlateColor(monitorInfo.plateColor);//车牌颜色
        var monitorType = monitorInfo.monitorType;//监控对象类型
        switch (monitorType) {
            case 0:
                monitorType = '车';
                break;
            case 1:
                monitorType = '人';
                break;
            case 2:
                monitorType = '物';
                break;
            default:
                monitorType = '';
                break;
        }
        var professionalsName = monitorInfo.professionalsName ? monitorInfo.professionalsName : '';//从业人员
        var fenceType = msgBody.fenceType;//围栏类型
        var fenceName = msgBody.fenceName;//围栏名称
        //报警时间
        var alarmTime;
        var time = msgBody.gpsTime;
        if (time.length === 12) {
            alarmTime = 20 + time.substring(0, 2) + "-" + time.substring(2, 4) + "-" + time.substring(4, 6) + " " +
                time.substring(6, 8) + ":" + time.substring(8, 10) + ":" + time.substring(10, 12);
        } else {
            var alarmPeopleTime = msgDesc.sysTime;
            alarmTime = dataTableOperation.dateFormat(Number(alarmPeopleTime));
        }
        var roadTypeStr = msgBody.roadTypeStr; //道路类型
        var roadLimitSpeed = msgBody.roadLimitSpeed; //路网限速
        var longitude = msgBody.longitude; //经度
        var latitude = msgBody.latitude; //纬度
        var identity = msgBody.identity; //驾驶证号
        var alarmEndTime = msgBody.alarmEndTime; //报警结束时间
        var speed = msgBody.speed ? noZero(msgBody.speed.toString()) : "-"; //报警开始速度
        var recorderSpeed = msgBody.recorderSpeed ? noZero(msgBody.recorderSpeed.toString()) : "-"; //行车记录仪速度

        var vehicleOwner = monitorInfo.vehicleOwner || '-';
        var vehicleOwnerPhone = monitorInfo.vehicleOwnerPhone || '-';
        var alarmProfessionalsName = monitorInfo.alarmProfessionalsName || '-';
        var phone = monitorInfo.phone || '-';
        var phoneTwo = monitorInfo.phoneTwo || '-';
        var phoneThree = monitorInfo.phoneThree || '-';
        var protocolType = msgBody.protocolType;
        var time = msgBody.alarmStartTimeList[0].split('_');
        var startTime = dataTableOperation.formatDate(Number(time[1]));


        // var startTime = time[1];

        //拼装报警记录表格数据
        var alarm = [
            0,
            monitorName === "" ? "-" : monitorName,
            alarmTime,
            (groupName === "" || groupName === undefined) ? '未绑定分组' : groupName,
            monitorType,
            plateColor,
            alarmName,
            (professionalsName === "null" || !professionalsName) ? "-" : professionalsName,
            (fenceType === "null" || fenceType === null || fenceType === undefined) ? "-" : fenceType,
            (fenceName === "null" || fenceName === null || fenceName === undefined) ? "-" : fenceName,
            msgBody.latitude + '|' + msgBody.longitude,
            "",
            simcardNumber,
            deviceNumber,
            msgSN,
            alarmNumber,
            alarmSource,
            identity,
            roadTypeStr,
            roadLimitSpeed,
            longitude,
            latitude,
            identity,
            alarmEndTime,
            speed,
            recorderSpeed,
            vehicleOwner,
            vehicleOwnerPhone,
            alarmProfessionalsName,
            phone,
            phoneTwo,
            phoneThree,
            protocolType,
            startTime,
            monitorId,
        ];
        dataTableOperation.updateRow('#alarmTable', alarmSet, alarm, 'alarm', earlyAlarmStartTime);
        //地图右下角报警提示
        dataTableOperation.realTimeAlarmInfoCalcFn();

    },

    /**
     * 时间戳转化日期
     * */
    formatDate: function (date) {
        var date = new Date(date);
        var YY = date.getFullYear() + '-';
        var MM = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1) + '-';
        var DD = (date.getDate() < 10 ? '0' + (date.getDate()) : date.getDate());
        var hh = (date.getHours() < 10 ? '0' + date.getHours() : date.getHours()) + ':';
        var mm = (date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes()) + ':';
        var ss = (date.getSeconds() < 10 ? '0' + date.getSeconds() : date.getSeconds());
        return YY + MM + DD + " " + hh + mm + ss;
    },


    /**
     * 主动安全数据更新
     * author wanjikun
     */
    updataActiveSafetyMessage: function () {
        $.ajax({
            type: "POST",
            url: "/clbs/adas/v/monitoring/getRiskList",
            dataType: "json",
            async: true,
            data: {pageNum: 1, pageSize: 30, status: 0},
            success: function (data) {
                activeSafetyHasRiskIds = [];
                dataTableOperation.updateActiveSafetyHtml(data);
            }
        });
        dataTableOperation.timeRequest();

    },
    /**
     * 组装主动安全html
     * author wanjikun
     */
    updateActiveSafetyHtml: function (res) {
        tableSecurity.updateScrollTop(0);
        tableSecurity.replaceOptionData([]);

        if (res.success) {
            var data = res.obj;
            if (data != null) {
                var rows = [];
                for (var i = 0; i < data.length; i++) {
                    var row = {
                        sequenceNumber: 0,
                        ope: '',
                        brand: data[i].brand,
                        driverName: dataTableOperation.handleShow(data[i].driverName),
                        drivingLicenseNo: dataTableOperation.handleShow(data[i].driverLicenseNo),
                        riskType: data[i].riskType,
                        riskLevel: data[i].riskLevel,
                        address: data[i].address,
                        warningTime: data[i].warningTime,
                        warningEndTime: data[i].warningEndTime,
                        picVideo: data[i].picFlag + '|' + data[i].videoFlag,
                        picFlag: data[i].picFlag,
                        videoFlag: data[i].videoFlag,
                        id: data[i].id,
                        vehicleId: data[i].vehicleId,
                        speed: data[i].speed
                    };
                    rows.push(row);
                    activeSafetyHasRiskIds.push(data[i].id);
                }
                tableSecurity.appendOptionData(rows);
            }
        }

        activePageNum = 1;


        // wjk 报警记录更新也为隐藏按钮添加方法
        $("#scalingBtn").unbind("click").bind("click", treeMonitoring.hideDataClick);

        if ($('#activeSafety').hasClass('active')) {
            dataTableOperation.carStateAdapt(5)
        }

    },
    //滚动加载
    activeScrollPage: function () {
        var $table = tableSecurity.rightTable ? tableSecurity.rightTable.getState().$dom.$table : tableSecurity.getState().$dom.$table;
        var $inner = tableSecurity.rightTable ? tableSecurity.rightTable.getState().$dom.$inner : tableSecurity.getState().$dom.$inner;
        //真实内容的高度
        var pageH = $table.height();
        //视窗的高度
        var viewportHeight = $inner.innerHeight();
        //隐藏的高度
        var scrollHeight = $inner.scrollTop();
        //判断加载视频，文章，回答，医生
        if (pageH - viewportHeight - scrollHeight <= 0) {
            if (!ifLoadPageData) {
                return;
            }
            ifLoadPageData = false;
            activePageNum++;
            json_ajax('post', '/clbs/adas/v/monitoring/getRiskList', 'json', true, {
                pageNum: activePageNum,
                pageSize: 20,
                status: 0,
                riskIds: activeSafetyHasRiskIds.join(','),// 主动安全列表已有事件ID集合
            }, function (res) {
                if (res.success) {
                    var data = res.obj;
                    if (data != null) {
                        var rows = [];
                        for (var i = 0; i < data.length; i++) {
                            var row = {
                                sequenceNumber: 0,
                                ope: '',
                                brand: data[i].brand,
                                driverName: dataTableOperation.handleShow(data[i].driverName),
                                drivingLicenseNo: dataTableOperation.handleShow(data[i].driverLicenseNo),
                                riskType: data[i].riskType,
                                riskLevel: data[i].riskLevel,
                                address: data[i].address,
                                warningTime: data[i].warningTime,
                                warningEndTime: data[i].warningEndTime,
                                picVideo: data[i].picFlag + '|' + data[i].videoFlag,
                                picFlag: data[i].picFlag,
                                videoFlag: data[i].videoFlag,
                                id: data[i].id,
                                vehicleId: data[i].vehicleId,
                                speed: data[i].speed
                            };
                            rows.push(row);
                            activeSafetyHasRiskIds.push(data[i].id);
                        }
                        tableSecurity.appendOptionData(rows);
                    }
                }

                setTimeout(function () {
                    ifLoadPageData = true;
                }, 100)
            })
        }
    },
    ifPicAndVideo: function (picFlag, videoFlag, id) {
        var spanHtml = '';
        if (picFlag === 1) {
            spanHtml += '<span class="risk_img" onclick="dataTableOperation.getMediaInfo(\'' + id + '\',0,event)"></span>';
        }
        if (videoFlag === 1) {
            spanHtml += '<span class="risk_video" onclick="dataTableOperation.getMediaInfo(\'' + id + '\',2,event)"></span>';
        }
        return spanHtml;
    },
    //获取风险列表媒体资源
    getMediaInfo: function (id, type, e) {
        multimediaFlag = true;
        MediaInfoId = id;
        window.event ? window.event.cancelBubble = true : e.stopPropagation();
        if (e) {
            if ($(e.target).siblings().hasClass('hasInfo')) {
                if (type === '0') {
                    $("#video").prop("disabled", false);
                    $("#img").prop("disabled", false);
                } else {
                    $("#img").prop("disabled", false);
                }
            } else {
                if (type === '0') {
                    $("#video").prop("disabled", true);
                } else {
                    $("#img").prop("disabled", true);
                }
            }
        }
        json_ajax('post', '/clbs/r/riskManagement/disposeReport/getRiskMedia', 'json', true, {
            "riskId": id,
            "mediaType": type
        }, function (data) {
            if (data.success) {
                if (type === '0') {
                    eventMediaPicArr = data.obj;
                    eventMediaPicSingNum = 0;
                    ifPicOrVideo = 'pic';

                    $('#allNum').html(eventMediaPicArr.length);
                    $('#whichNum').html(1);

                    $('#eventMediaVideo').hide();
                    $('#videoBox').hide();
                    $('#eventMediaPic').show();

                    $('.Video').removeClass('btn-primary').addClass('btn-default');
                    $('.Img').removeClass('btn-default').addClass('btn-primary');
                    $('#eventMediaModalLabel').html(eventMediaPicArr[0].riskType + ' -- ' + eventMediaPicArr[0].riskEventType);
                    $('#eventMediaPic').attr('src', eventMediaPicArr[0].mediaUrl);
                    $('#eventMediaModal').modal('show');
                }
                if (type === '2') {

                    eventMediaVideoArr = data.obj;
                    eventMediaVideoSingNum = 0;
                    ifPicOrVideo = 'video';

                    $('#allNum').html(eventMediaVideoArr.length);
                    $('#whichNum').html(1);

                    $('#eventMediaVideo').show();
                    $('#videoBox').show();
                    $('#eventMediaPic').hide();

                    $('.Video').removeClass('btn-default').addClass('btn-primary');
                    $('.Img').removeClass('btn-primary').addClass('btn-default');
                    $('#eventMediaModalLabel').html(eventMediaVideoArr[0].riskType + ' -- ' + eventMediaVideoArr[0].riskEventType);

                    if (window.navigator.userAgent.toLowerCase().indexOf('ie') >= 0) {
                        $('#videoBox').empty().append('<embed  src=' + eventMediaVideoArr[0].mediaUrl + ' autostart=false id="eventMediaVideo" style="width: 100%;height:370px; "  windowlessVideo="true" wmode="transparent" align="middle" type="application/x-shockwave-flash"></embed>');
                    } else {
                        $('#eventMediaVideo').attr('src', eventMediaVideoArr[0].mediaUrl)
                    }

                    $('#eventMediaModal').modal('show')
                }
            }
        })
    },
    //获取事件列表媒体资源
    getEventMediaInfo: function (id, type, e) {
        multimediaFlag = false;
        EventMediaInfoId = id;
        window.event ? window.event.cancelBubble = true : e.stopPropagation();
        if (e) {
            if ($(e.target).siblings().hasClass('eventhasInfo')) {
                if (type === '0') {
                    $("#video").prop("disabled", false);
                    $("#img").prop("disabled", false);
                } else {
                    $("#img").prop("disabled", false);
                }
            } else {
                if (type === '0') {
                    $("#video").prop("disabled", true);
                } else {
                    $("#img").prop("disabled", true);
                }
            }
        }
        json_ajax('post', '/clbs/r/riskManagement/disposeReport/getEventMedia', 'json', true, {
            "eventId": id,
            "mediaType": type
        }, function (data) {
            if (data.success) {
                if (type === '0') {
                    eventMediaPicArr = data.obj;
                    eventMediaPicSingNum = 0;
                    ifPicOrVideo = 'pic';

                    $('#allNum').html(eventMediaPicArr.length);
                    $('#whichNum').html(1);

                    $('#eventMediaVideo').hide();
                    $('#videoBox').hide();
                    $('#eventMediaPic').show();

                    $('.Video').removeClass('btn-primary').addClass('btn-default');
                    $('.Img').removeClass('btn-default').addClass('btn-primary');
                    $('#eventMediaModalLabel').html(eventMediaPicArr[0].riskType + ' -- ' + eventMediaPicArr[0].riskEventType);
                    $('#eventMediaPic').attr('src', eventMediaPicArr[0].mediaUrl);
                    $('#eventMediaModal').modal('show');
                }
                if (type === '2') {

                    eventMediaVideoArr = data.obj;
                    eventMediaVideoSingNum = 0;
                    ifPicOrVideo = 'video';

                    $('#allNum').html(eventMediaVideoArr.length);
                    $('#whichNum').html(1);

                    $('#eventMediaVideo').show();
                    $('#videoBox').show();
                    $('#eventMediaPic').hide();

                    $('.Video').removeClass('btn-default').addClass('btn-primary');
                    $('.Img').removeClass('btn-primary').addClass('btn-default');
                    $('#eventMediaModalLabel').html(eventMediaVideoArr[0].riskType + ' -- ' + eventMediaVideoArr[0].riskEventType);

                    if (window.navigator.userAgent.toLowerCase().indexOf('ie') >= 0) {
                        $('#videoBox').empty().append('<embed  src=' + eventMediaVideoArr[0].mediaUrl + ' autostart=false id="eventMediaVideo" style="width: 100%;height:370px; "  windowlessVideo="true" wmode="transparent" align="middle" type="application/x-shockwave-flash"></embed>');
                    } else {
                        $('#eventMediaVideo').attr('src', eventMediaVideoArr[0].mediaUrl)
                    }

                    $('#eventMediaModal').modal('show')
                }
            }
        })
    },
    nextMedia: function () {
        if (ifPicOrVideo === 'pic') {
            if (eventMediaPicSingNum < eventMediaPicArr.length - 1) {
                eventMediaPicSingNum++;

                $('#whichNum').html(eventMediaPicSingNum + 1);

                $('#eventMediaModalLabel').html(eventMediaPicArr[eventMediaPicSingNum].riskType + ' -- ' + eventMediaPicArr[eventMediaPicSingNum].riskEventType)
                $('#eventMediaPic').attr('src', eventMediaPicArr[eventMediaPicSingNum].mediaUrl)
            }
        }

        if (ifPicOrVideo === 'video') {
            if (eventMediaVideoSingNum < eventMediaVideoArr.length - 1) {
                eventMediaVideoSingNum++;

                $('#whichNum').html(eventMediaVideoSingNum + 1);

                $('#eventMediaModalLabel').html(eventMediaVideoArr[eventMediaVideoSingNum].riskType + ' -- ' + eventMediaVideoArr[eventMediaVideoSingNum].riskEventType);

                if (window.navigator.userAgent.toLowerCase().indexOf('ie') >= 0) {
                    $('#videoBox').empty().append('<embed  src=' + eventMediaVideoArr[eventMediaVideoSingNum].mediaUrl + ' autostart=false id="eventMediaVideo" style="width: 100%;height:350px; "  windowlessVideo="true" wmode="transparent" align="middle" type="application/x-shockwave-flash"></embed>');
                } else {
                    $('#eventMediaVideo').attr('src', eventMediaVideoArr[eventMediaVideoSingNum].mediaUrl)
                }
            }
        }

    },
    prevMedia: function () {
        if (ifPicOrVideo === 'pic') {
            if (eventMediaPicSingNum > 0) {
                eventMediaPicSingNum--;

                $('#whichNum').html(eventMediaPicSingNum + 1);

                $('#eventMediaModalLabel').html(eventMediaPicArr[eventMediaPicSingNum].riskType + ' -- ' + eventMediaPicArr[eventMediaPicSingNum].riskEventType)
                $('#eventMediaPic').attr('src', eventMediaPicArr[eventMediaPicSingNum].mediaUrl)
            }
        }
        if (ifPicOrVideo === 'video') {
            if (eventMediaVideoSingNum > 0) {
                eventMediaVideoSingNum--;

                $('#whichNum').html(eventMediaVideoSingNum + 1);

                $('#eventMediaModalLabel').html(eventMediaVideoArr[eventMediaVideoSingNum].riskType + ' -- ' + eventMediaVideoArr[eventMediaVideoSingNum].riskEventType)
                $('#eventMediaPic').attr('src', eventMediaVideoArr[eventMediaVideoSingNum].mediaUrl)
            }
        }

    },
    videoChange: function (e) {
        var btnType = $(e.target).attr('data-value');
        if (btnType === '0') {
            $('.Video').removeClass('btn-primary').addClass('btn-default');
            $('.Img').removeClass('btn-default').addClass('btn-primary');
        } else if (btnType === '2') {
            $('.Video').removeClass('btn-default').addClass('btn-primary');
            $('.Img').removeClass('btn-primary').addClass('btn-default');
        }

        if (multimediaFlag) {
            dataTableOperation.getMediaInfo(MediaInfoId, btnType);
        } else {
            dataTableOperation.getEventMediaInfo(EventMediaInfoId, btnType);
        }
    }

    ,
    handleAdressHtml: function (data) {
        return data ? data : '未定位';
    },
    handleHtml: function (data) {
        return data ? data : '';
    },
    handleShow: function (data) {
        return data ? data : '-';
    },
    /**
     * 是否显示弹窗
     * author wanjikun
     */
    ifshowActiveSafetyPop: function (riskId, vehicleId, offsetTop) {
        if (timer) clearTimeout(timer);
        var scrollT = $('#securityTable-div .i-table-content-container .i-table-inner').scrollTop();
        timer = setTimeout(function () {
            var id = riskId;
            if (riskIdGloabal === riskId && $('#activeSafetyPop').hasClass('show')) {
                dataTableOperation.closePopover();
            } else {
                var paramer = {
                    'riskId': id,
                    'vehicleId': vehicleId,
                };

                json_ajax('post', '/clbs/r/riskManagement/disposeReport/eventList', 'json', true, paramer, function (data) {
                    if (data.success) {
                        var obj = data.obj;
                        var html = '';
                        var monitoring = "";
                        var roadTypeStr;
                        var roadLimitSpeed = '';
                        for (var i = 0; i < obj.length; i++) {
                            var roadType = obj[i].roadType;
                            if (roadType == null || roadType === "") {
                                roadTypeStr = "-"
                            } else {
                                roadTypeStr = dataTableOperation.roadTypeJudge(roadType)
                            }
                            if (obj[i].roadLimitSpeed === '' || obj[i].roadLimitSpeed === 'null') {
                                roadLimitSpeed = '';
                            } else {
                                roadLimitSpeed = obj[i].roadLimitSpeed + 'km/h';
                            }
                            monitoring = obj[i].brand + '(' + obj[i].plateColor + ')';
                            html += '<tr>' +
                                '<td>' + dataTableOperation.handleHtml(obj[i].riskType) + '</td>' +
                                '<td>' + dataTableOperation.handleHtml(obj[i].riskEvent) + '</td>' +
                                '<td>' + dataTableOperation.handleHtml(obj[i].level) + '</td>' +
                                '<td>' + dataTableOperation.handleHtml(obj[i].eventTime) + '</td>' +
                                '<td>' + dataTableOperation.handleShow(obj[i].warningEndTime) + '</td>' +
                                '<td>' + (obj[i].speed ? obj[i].speed : '0.0') + 'km/h' + '</td>' +
                                '<td>' + dataTableOperation.handleHtml(obj[i].originalLongitude) + '</td>' +
                                '<td>' + dataTableOperation.handleHtml(obj[i].originalLatitude) + '</td>' +
                                '<td>' + roadTypeStr + '</td>' +
                                '<td>' + roadLimitSpeed + '</td>' +
                                '<td>' + dataTableOperation.handleHtml(obj[i].eventNumber) + '</td>' +
                                '<td>' + '<a href="javascript:void(0)" class="stateInfoTd" ' +
                                ' data-id="' + monitoring + ',' + obj[i].riskEvent + ',' + obj[i].vehicleStatus + '">状态信息</a>' + '</td>' +
                                '<td>' + dataTableOperation.setImgAndVideo(obj[i]) + '</td>' +
                                '<td>' + dataTableOperation.adjunctDownload(obj[i], vehicleId) + '</td>' +
                                '</tr>'
                        }
                        $('#dataTablePopBody').html(html);

                        if (obj.length > 0 && obj[0].eventProtocolType == '25') { //黑标车辆隐藏 状态信息 字段
                            $('#dataTablePopBody').parent().find('tr').each(function (index, item) {
                                $($(item).children()[11]).hide()
                            })
                        }

                        //状态信息
                        $(".stateInfoTd").mouseover(function (e) {
                            var datas = $(e.target).attr('data-id').split(',');
                            var vehicleStatus = datas.splice(2);
                            var result = [];
                            for (var i = 0; i < vehicleStatus.length; i++) {
                                result.push(vehicleStatus[i].split(":")[1])
                            }

                            var content = '<div><ul style="padding:0; margin:0;">' +
                                '<li>监控对象：' + '<span>' + dataTableOperation.handleShow(datas[0]) + '</span></li>' +
                                '<li>报警事件：' + '<span>' + dataTableOperation.handleShow(datas[1]) + '</span></li>' +
                                '<li>ACC状态：' + '<span>' + dataTableOperation.handleShow(result[0]) + '</span></li>' +
                                '<li>左转向灯：' + '<span>' + dataTableOperation.handleShow(result[1]) + '</span></li>' +
                                '<li>右转向灯：' + '<span>' + dataTableOperation.handleShow(result[2]) + '</span></li>' +
                                '<li>制动状态：' + '<span>' + dataTableOperation.handleShow(result[4]) + '</span></li>' +
                                '<li>插卡状态：' + '<span>' + dataTableOperation.handleShow(result[5]) + '</span></li>' +
                                '</ul></div>'
                            var _this = $(this);
                            _this.justToolsTip({
                                animation: "moveInTop",
                                width: "auto",
                                contents: content,
                                gravity: 'top',
                                events: 'mouseover',
                            });
                        });

                        riskIdGloabal = id;

                        var h = $('#activeSafetyPop').height();
                        // var scrollT = $('#activeSafety').scrollTop();
                        var rowInTable = tableSecurity.findRow(tableSecurity.getState().data, tableSecurity.getOption(), riskId);
                        if (rowInTable != null) {
                            var vid = rowInTable[0].vehicleId;
                            var brand = rowInTable[0].brand;
                            dataTableOperation.vehicleCheck(vid, brand);
                        }
                        $('#activeSafetyPop').css({
                            'top': (offsetTop - h - scrollT + 45) + 'px',
                            "max-width": "1500px",
                        });
                        dataTableOperation.showActiveSafetyPop()
                    }
                })
            }
        }, 500);
    },
    vehicleCheck: function (vehicleId, brand) {
        activeSafety.riskInformationDetails(vehicleId);
        $("#search_condition").val(brand);

        treeMonitoring.search_condition();
        setTimeout(function () {
            // 左侧组织树
            //为表格添加高亮
            var numberPlate = vehicleId;
            $(".ztree li a").removeClass("curSelectedNode_dbClick");
            $(".ztree li a").removeClass("curSelectedNode");
            //为车辆树添加高亮
            var zTreeDataTables = $.fn.zTree.getZTreeObj("treeDemo");

            var nodes = zTreeDataTables.getNodesByParam("id", numberPlate, null);
            for (var i = 0; i < nodes.length; i++) {
                (function (index) {
                    var ztreeStyleDbclick = nodes[index].tId;
                    var $span = $("#" + ztreeStyleDbclick).find("a span:eq(1)");
                    setTimeout(function () {
                        $span.dblclick();
                    }, 1000);
                }(i));
            }
            realtimeMonitoringVideoSeparate.closeTerminalVideo();
            if (nodes.length > 0) {
                //创建车辆对象参数信息(用于实时视频)
                var treeNode = nodes[0];
                var vehicleInfo = {
                    vid: treeNode.id,
                    brand: treeNode.name,
                    deviceNumber: treeNode.deviceNumber,
                    plateColor: treeNode.plateColor,
                    isVideo: treeNode.isVideo,
                    simcardNumber: treeNode.simcardNumber,
                };
                subscribeVehicleInfo = vehicleInfo; //订阅的车辆信息全局变量
                realTimeVideo.setVehicleInfo(vehicleInfo);
            } else {
                return;
            }
            TimeFn = setTimeout(function () {
                treeMonitoring.centerMarker(vehicleId, 'DBLCLICK');
            }, 300);
        }, 1000)
    },
    // 主动安全视频播放控制
    videoBtnClick: function () {
        if (!subscribeVehicleInfo) {
            layer.msg('请选择要处理的信息后再点击');
            return;
        }
        if ($(this).hasClass('active')) {
            $(this).removeClass('active');
            realtimeMonitoringVideoSeparate.closeTerminalVideo()
        } else {
            var vid = subscribeVehicleInfo.vid;
            if (!dataTableOperation.checkVehicleOnlineStatus(vid)) return;
            realtimeMonitoringVideoSeparate.closeTerminalVideo();

            setTimeout(function () {
                realtimeMonitoringVideoSeparate.sendParamByBatch();
            }, 500)

        }
    },
    // 主动安全视频对讲功能
    callSelectFun: function () {
        if (subscribeVehicleInfo) {
            var vid = subscribeVehicleInfo.vid;
            if (!dataTableOperation.checkVehicleOnlineStatus(vid)) return;
            realtimeMonitoringVideoSeparate.callOrder();
        } else {
            layer.msg('请选择要处理的信息再点击');
        }
    },
    //弹出框附件下载
    adjunctDownload: function (item, vehicleId) {
        //附件下载
        var eventDownLoad = '<a href="javascript:void(0)"  class="editBtn eventTerminalEvidence btn-primary"' +
            ' onclick="dataTableOperation.eventDownLoad(\'' + item.eventId + '\',\'' + item.eventNumber + '\')">附件下载</a>';
        if (!item.hasMedia) {
            eventDownLoad = '<a href="javascript:void(0)" style="cursor: default" class="editBtn btn-default" onclick="event.stopPropagation()">附件下载</a>'
        }

        //获取附件
        if (item.attachmentStatus === 0) {
            eventDownLoad += '<a href="javascript:void(0)" onclick="dataTableOperation.eventRequest(this,\'' + item.eventId + '\',\'' + vehicleId + '\')"  class="editBtn eventTerminalEvidence btn-primary" style="margin-left:10px;">获取附件</a>';
        } else if (item.attachmentStatus === 1) {
            eventDownLoad += '<a href="javascript:void(0)"  class="editBtn eventTerminalEvidence btn-default" style="margin-left:10px;">附件失效</a>';
        } else if (item.attachmentStatus === 2) {
            eventDownLoad += '<a href="javascript:void(0)"  class="editBtn eventTerminalEvidence btn-default btn-defaultr" style="margin-left:10px;">获取中···</a>';

        }
        return eventDownLoad;
    },
    //获取附件
    eventRequest: function (e, eventId, vehicleId) {
        var paramer = {
            'riskEventId': eventId,
            'vehicleId': vehicleId
        };
        json_ajax("POST", "/clbs/v/oilmassmgt/oilcalibration/checkVehicleOnlineStatus", 'json', true, paramer, function (data) {
            if (!data.success) {
                layer.msg("终端离线")
            } else {
                json_ajax('POST', "/clbs/adas/v/monitoring/getAdasMedia", 'json', true, paramer, function (result) {
                    if (result.success) {
                        layer.msg("下发成功");
                        $(e).text("获取中···");
                        $(e).removeClass("eventTerminalEvidence btn-primary").addClass("btn-default btn-defaultr");
                        $(e).attr("disabled", true).css("pointer-events", "none");
                    }
                });
            }
        })
    },
    //事件附件下载
    eventDownLoad: function (riskId, riskNumber) {
        var paramer = {
            'downLoadId': riskId,
            'isEvent': true,
            'number': riskNumber
        };
        json_ajax('GET', "/clbs/r/riskManagement/disposeReport/terminalEvidence", 'json', true, paramer, function (data) {
            if (data.success) {
                if (data.obj.hasNotFile) {
                    layer.msg(data.obj.msg);
                } else {
                    window.location.href = "/clbs/r/riskManagement/disposeReport/downloadFile?filePath=" + data.obj.filePath + "&fileName=" + data.obj.fileName + "&isRiskEvidence=" + data.obj.isRiskEvidence;
                }
            }
        });
    },
    setImgAndVideo: function (row) {
        var spanHtml = '';
        if (row.hasPic) {
            spanHtml += '<img class="eventhasInfo" src="/clbs/resources/img/previewimg-blue.svg" style="cursor: pointer" onclick="dataTableOperation.getEventMediaInfo(\'' + row.eventId + '\',\'0\',event)" width="20" alt="图片"> ';
        } else {
            spanHtml += '<img class="eventhasNotInfo" src="/clbs/resources/img/previewimg-grey.svg" width="20" alt="图片"> ';
        }
        if (row.hasVideo) {
            spanHtml += '<img class="eventhasInfo" src="/clbs/resources/img/video-blue.svg" style="cursor: pointer" onclick="dataTableOperation.getEventMediaInfo(\'' + row.eventId + '\',\'2\',event)" width="20" alt="视频">';
        } else {
            spanHtml += '<img class="eventhasNotInfo" src="/clbs/resources/img/video-grey.svg" width="20" alt="视频">';
        }
        return spanHtml;
    },
    /**
     * 显示主动安全弹窗
     * author wanjikun
     */
    showActiveSafetyPop: function () {
        $('#activeSafetyPop').addClass("in show")
    },
    /**
     * 关闭
     * author wanjikun
     */
    closePopover: function () {
        $('#activeSafetyPop').removeClass("in show")
    },
    /**
     * 主动安全处理风险弹窗
     */
    ifdealEventPop: function (riskid, vehicleId, brand, driverName, warningTime, riskType, e) {
        window.event ? window.event.cancelBubble = true : e.stopPropagation();
        tableSecurity.setActiveRow(undefined);
        dataTableOperation.closePopover();

        riskId = riskid;

        $("#riskWarningManage").modal('show');

        $("#riskWarningManage").val("alarm");
        $("#riskAlarmRemark").val("");
        $("#riskAlarm-remark").show();
        $("#riskSmsTxt").val("");
        $("#riskTime").val("");
        $("#riskWarningDescription").text("");
        // var dataArray = data.split('|');
        // $('#warningManage').modal('show');
        $("#riskListeningContent,#riskTakePicturesContent,#riskSendTextMessages,.riskListenFooter,.riskTakePicturesFooter,.riskSendTextFooter").hide();
        // var dataArray = data.split('|');
        var url = "/clbs/a/search/alarmDeal";
        var data = {"vid": vehicleId};
        var warningType = "";
        var device = "";
        var sim = "";
        var type = '';
        var assignmentName = '';
        json_ajax("POST", url, "json", false, data, function (result) {
            if (result.success) {
                type = result.obj.type;
                warningType = result.obj.type;
                device = result.obj.device;
                sim = result.obj.sim;
                assignmentName = result.obj.assignmentName;
            }
        });

        riskDeviceTypeTxt = type;

        if (type === '11' || type === '20' || type === '21' || type === '24' || type === '25' || type === '26' || type === '28') {
            $('.riskNewDeviceInfo').show();
            $('.riskOldDeviceInfo').hide();
            $('#riskMinResolution, #riskMaxResolution').show();
            $('#riskDefaultValue').attr('selected', false);
        } else {
            $('.riskNewDeviceInfo').hide();
            $('.riskOldDeviceInfo').show();
            $('#riskMinResolution, #riskMaxResolution').hide();
            $('#riskDefaultValue').attr('selected', true);
        }

        if (warningType === "9" || warningType === "10" || warningType === "5") {
            $("#riskWarningHiden").removeAttr("style");
            $("#riskWarningManageListening").hide();
            $("#riskWarningManagePhoto").hide();
            $("#riskWarningManageSend").hide();
            $("#sno").val("0");
        } else {
            $("#riskWarningHiden").attr("style", "text-align:center");
            $("#riskWarningManageListening").show();
            $("#riskWarningManagePhoto").show();
            $("#riskWarningManageSend").show();
        }


        $("#riskWarningCarName").text(brand);
        $("#riskWarningPeo").text(driverName);
        $("#riskWarningGroup").text(assignmentName);
        $("#riskWarningTime").text(warningTime);
        $("#riskWarningDescription").text(riskType);
        $("#riskvUuid").val(vehicleId);
        $("#riskSimcard").val(sim);
        $("#riskDevice").val(device);
        $("#riskWarningType").val(warningType);
        $('#riskEventId').val(riskid);
    },
    // 处理风险
    deleteEvevt: function (id) {
        var riskResult = $("input[name='accident']:checked").val();
        json_ajax('post', '/clbs/r/riskManagement/disposeReport/dealRisk', 'json', true, {
            riskId: id,
            status: 6,
            riskResult: riskResult
        }, function (data) {
            if (data.success) {
                layer.msg('处理成功');
                dataTableOperation.updataActiveSafetyMessage();
            } else {
                layer.msg(data.msg);
                dataTableOperation.updataActiveSafetyMessage();
            }
        })
    },
    distinguishPushAlarmSet: function (alarmSetType, msgBody) {
        //报警数据 围栏名称及类型
        var gpsAttachInfoList = msgBody.gpsAttachInfos;
        if (gpsAttachInfoList !== undefined) {
            for (var i = 0; i < gpsAttachInfoList.length; i++) {
                var gpsAttachInfoID = gpsAttachInfoList[i].gpsAttachInfoID;
                // 17 围栏内超速
                if (gpsAttachInfoID === 17) {
                    if (gpsAttachInfoList[i].speedAlarm !== undefined) {
                        var stype = gpsAttachInfoList[i].speedAlarm.type;
                        alarmFanceType = dataTableOperation.getAlarmFanceIdAndType(stype);
                        var alarmFanceIds = gpsAttachInfoList[i].speedAlarm.lineID;
                        if (alarmFanceIds !== null && alarmFanceIds !== undefined && alarmFanceIds !== "") {
                            alarmFanceId = dataTableOperation.getFanceNameByFanceIdAndVid(msgBody.vehicleInfo.id, alarmFanceIds);
                        }
                    }
                }
                // 18 进出围栏
                else if (gpsAttachInfoID === 18) {
                    if (gpsAttachInfoList[i].lineOutAlarm !== undefined) {
                        var ltype = gpsAttachInfoList[i].lineOutAlarm.type;
                        alarmFanceType = dataTableOperation.getAlarmFanceIdAndType(ltype);
                        var alarmFanceIds = gpsAttachInfoList[i].lineOutAlarm.lineID;
                        if (alarmFanceIds !== null && alarmFanceIds !== undefined && alarmFanceIds !== "") {
                            alarmFanceId = dataTableOperation.getFanceNameByFanceIdAndVid(msgBody.vehicleInfo.id, alarmFanceIds);
                        }
                    }
                }
                // 19 过长 不足
                else if (gpsAttachInfoID === 19) {
                    if (gpsAttachInfoList[i].timeOutAlarm !== undefined) {
                        var ttype = gpsAttachInfoList[i].timeOutAlarm.type;
                        alarmFanceType = dataTableOperation.getAlarmFanceIdAndType(ttype);
                        var alarmFanceIds = gpsAttachInfoList[i].timeOutAlarm.lineID;
                        if (alarmFanceIds !== null && alarmFanceIds !== undefined && alarmFanceIds !== "") {
                            alarmFanceId = dataTableOperation.getFanceNameByFanceIdAndVid(msgBody.vehicleInfo.id, alarmFanceIds);
                        }
                    }
                }
            }

        }
    },
    // 围栏类型判断
    getAlarmFanceIdAndType: function (types) {
        if (types !== undefined) {
            if (types === 1) {
                alarmFanceType = "圆形";
            } else if (types === 2) {
                alarmFanceType = "矩形";
            } else if (types === 3) {
                alarmFanceType = "多边形";
            } else if (types === 4) {
                alarmFanceType = "路线";
            }
            return alarmFanceType;
        }
    },
    //根据围栏Id及车Id查询围栏名称
    getFanceNameByFanceIdAndVid: function (vid, fcid) {
        var fenceName;
        $.ajax({
            type: "POST",
            url: "/clbs/v/monitoring/getFenceInfo",
            dataType: "json",
            async: false,
            data: {"vehicleId": vid, "sendDownId": fcid},
            success: function (data) {
                if (data.success) {
                    if (data.obj != null) {
                        fenceName = data.obj.name;
                    } else {
                        fenceName = null;
                    }
                }
            }
        });
        return fenceName;
    },
    //数据表格html组装
    tableListHtml: function (dataMsg, type, dataString, earlyAlarmStartTime) {
        var html = '';
        var this_id = dataMsg[dataMsg.length - 1];
        if (type === 'state') {
            for (var i = 0; i < dataMsg.length - 1; i++) {
                if (i === 12) {
                    var allinfo = dataMsg[i];
                    if (dataMsg[i] != null) {
                        if (dataMsg[i] !== "" && dataMsg[i].length > 20) {
                            dataMsg[i] = dataMsg[i].substring(0, 20) + "...";
                            html += '<td class="demo demoUp" alt="' + allinfo + '"  data-id="' + this_id + '">' + dataMsg[i] + '</td>';
                        } else {
                            html += '<td  data-id="' + this_id + '">' + dataMsg[i] + '</td>';
                        }
                    } else {
                        html += '<td  data-id="' + this_id + '"> </td>';
                    }
                } else {
                    html += '<td data-id="' + this_id + '">' + dataMsg[i] + '</td>';
                }
            }
        }
        if (type === 'obd') {
            for (var i = 0; i < dataMsg.length - 1; i++) {
                html += '<td data-id="' + this_id + '">' + dataMsg[i] + '</td>';
            }
        }
        if (type === 'alarm') {
            if (dataMsg[3] === '人') {
                if (alarmRole) {
                    html += "<td>" + dataMsg[0] + "</td><td data-id='" + this_id + "'>" + dataMsg[1] + "</td><td>" + dataMsg[7] + "</td><td onClick='dataTableOperation.warningManage(" + dataString + ")' style='color:#2ca2d1;'>未处理</td><td>" + dataMsg[2] + "</td><td>" + dataMsg[3] + "</td><td>" + dataMsg[4] + "</td><td>" + dataMsg[5] + "</td><td>" + dataMsg[6] + "</td><td>" + dataMsg[8] + "</td><td>" + dataMsg[9] + "</td><td>" + dataMsg[10] + "</td>";
                } else {
                    html += "<td>" + dataMsg[0] + "</td><td data-id='" + this_id + "'>" + dataMsg[1] + "</td><td>" + dataMsg[7] + "</td><td>未处理</td><td>" + dataMsg[2] + "</td><td>" + dataMsg[3] + "</td><td>" + dataMsg[4] + "</td><td>" + dataMsg[5] + "</td><td>" + dataMsg[6] + "</td><td>" + dataMsg[8] + "</td><td>" + dataMsg[9] + "</td><td>" + dataMsg[10] + "</td>";
                }
            } else {
                if (alarmRole) {
                    html += "<td>" + dataMsg[0] + "</td><td data-id='" + this_id + "'>" + dataMsg[1] + "</td><td data-type='" + earlyAlarmStartTime + "'>" + dataMsg[2] + "</td><td onClick='dataTableOperation.warningManage(" + dataString + ")' style='color:#2ca2d1;'>未处理</td><td>" + dataMsg[3] + "</td><td>" + dataMsg[4] + "</td><td>" + dataMsg[5] + "</td><td data-alarmType='" + (dataTableOperation.endsWith(dataMsg[15], ",") ? dataMsg[15] : dataMsg[15] + ",") + "'>" + dataMsg[6] + "</td><td>" + dataMsg[7] + "</td><td>" + dataMsg[8] + "</td><td>" + dataMsg[9] + "</td><td>" + dataMsg[10] + "</td>";
                } else {
                    html += "<td>" + dataMsg[0] + "</td><td data-id='" + this_id + "'>" + dataMsg[1] + "</td><td data-type='" + earlyAlarmStartTime + "'>" + dataMsg[2] + "</td><td>未处理</td><td>" + dataMsg[3] + "</td><td>" + dataMsg[4] + "</td><td>" + dataMsg[5] + "</td><td data-alarmType='" + (dataTableOperation.endsWith(dataMsg[15], ",") ? dataMsg[15] : dataMsg[15] + ",") + "'>" + dataMsg[6] + "</td><td>" + dataMsg[7] + "</td><td>" + dataMsg[8] + "</td><td>" + dataMsg[9] + "</td><td>" + dataMsg[10] + "</td>";
                }
            }
        }
        setTimeout(function () {
            $(".demoUp").mouseover(function () {

                var _this = $(this);
                if (_this.attr("alt")) {
                    _this.justToolsTip({
                        animation: "moveInTop",
                        width: "auto",
                        contents: _this.attr("alt"),
                        gravity: 'top'
                    });
                }
            })
        }, 1000)

        return html;

    },
    endsWith: function (val, str) {
        var reg = new RegExp(str + "$");
        return reg.test(val);
    },
    //表格插入数据
    dataTableList: function (array, data, id, type, earlyAlarmStartTime) {
        var rowId = data[data.length - 1];
        var row;
        // 为状态信息重写，使用itable插件实现
        if (type === 'state') {
            row = data;
            rowId = row.id;
            // 新增或者更新
            if (array.indexOf(rowId) === -1) {
                tableStatus.appendOptionData([row]);
                dataTableOperation.typeGroup(type, rowId);
            } else if (dataTableOperation.rowUpdateState[type]) {
                tableStatus.updateOptionData(row);
                // 如果有气泡展示，更新气泡内容
                if (newTableOperation.lastStatusHoverRowId === rowId) {
                    var content = '';
                    switch (newTableOperation.lastStatusHoverField) {
                        case 'signalState':
                            content = row.signalState && row.signalState !== '-' ? row.signalState : ''; // 信号状态
                            break;
                        case 'positionDescription':
                            content = newTableOperation.buildPositionDescriptionTable(row.positionDescription); // 位置
                            break;
                        case 'oilMass':
                            content = newTableOperation.buildOilMassTable(row.oilMass); // 传感器液体量
                            break;
                        // case 'dayOilWear':
                        //     content = newTableOperation.buildOilExpendTable(row.oilExpend); // 传感器当日油耗
                        //     break;
                        case 'oilExpend':
                            content = newTableOperation.buildOilExpendTable(row.oilExpend, true); // 传感器总油耗
                            break;
                        case 'elecData':
                            content = newTableOperation.buildElecDataTable(row.elecData); // 电量电压
                            break;
                        case 'temperatureSensor':
                            content = newTableOperation.buildTemperatureSensorTable(row.temperatureSensor); // 温度
                            break;
                        case 'temphumiditySensor':
                            content = newTableOperation.buildTemphumiditySensorTable(row.temphumiditySensor); // 湿度
                            break;
                        case 'positiveNegative':
                            content = newTableOperation.buildPositiveNegativeTable(row.positiveNegative); // 正反转
                            break;
                        case 'loadInfos':
                            content = newTableOperation.buildLoadInfosTable(row.loadInfos); // 载重
                            break;
                        case 'workHourSensor':
                            content = newTableOperation.buildWorkHourTable(row.workHourSensor); // 工时
                            break;
                        case 'tyreInfos':
                            content = newTableOperation.buildTyreInfosTable(row.tyreInfos); // 胎压
                            break;
                        default:
                            return;
                    }
                    if (content.length > 0) {
                        $('.just-con').html(content);
                    }
                }
            }
            if (curDbSubscribeMOnitor !== '' && curDbSubscribeMOnitor !== undefined && curDbSubscribeMOnitor !== null) {// 直接双击的监控对象置顶显示
                if (rowId !== tableStatus.getState().lastLockedRowId) {
                    tableStatus.setLockedRow(rowId);
                    curDbSubscribeMOnitor = '';
                }
            }
            // return;
        } else if (type === 'alarm') {
            row = {
                sequenceNumber: 0,
                monitorName: data[1],
                alarmTime: data[2],
                handleStatus: 0,
                assignmentName: data[3],
                vehicleType: data[4],
                plateColorName: data[5],
                alarmType: data[6], // 文本形式的报警类型
                speed: data[24],
                recorderSpeed: data[25],
                professionalsName: data[7],
                defenceType: data[8],
                defenceName: data[9],
                alarmPosition: 0,
                empty: '',
                earlyAlarmStartTime: earlyAlarmStartTime,
                alarmRole: alarmRole,
                alarmNumber: data[15], // 数字形式的报警类型
                field11: data[11],
                simcardNumber: data[12],
                deviceNumber: data[13],
                msgSN: data[14],
                alarmSource: data[16],
                address: data[10],
                driving_license_no: data[17],
                roadType: data[18],
                roadLimitSpeed: data[19],
                longtitude: data[20],
                latitude: data[21],
                identity: data[22],
                alarmEndTime: data[23],
                vehicleOwner: data[26],
                vehicleOwnerPhone: data[27],
                alarmProfessionalsName: data[28],
                phone: data[29],
                phoneTwo: data[30],
                phoneThree: data[31],
                protocolType: data[32],
                startTime: data[33],
                id: rowId,
            };
            // 新增或者更新
            if (array.indexOf(rowId) === -1) {
                tableAlarm.appendOptionData([row]);
                dataTableOperation.typeGroup(type, rowId);
            } else if (dataTableOperation.rowUpdateState[type]) {
                // 如果同一条位置同一时间信息上传的报警，拼接报警类型
                var rowInTable = tableAlarm.findRow(tableAlarm.getState().data, tableAlarm.getOption(), rowId);
                if (rowInTable !== null && rowInTable[0].alarmTime === row.alarmTime) {
                    var rowDataInTable = rowInTable[0];
                    if (rowDataInTable.alarmType.indexOf(row.alarmType) === -1) {
                        row.alarmType = removeDuplicate(rowDataInTable.alarmType + ',' + row.alarmType);
                        row.alarmNumber = removeDuplicate(rowDataInTable.alarmNumber + ',' + row.alarmNumber);
                    }
                }
                tableAlarm.updateOptionData(row);
            }
        } else if (type === 'obd') {
            row = data;
            rowId = row.id;
            // 新增或者更新
            if (array.indexOf(rowId) === -1) {
                tableObd.appendOptionData([row]);
                dataTableOperation.typeGroup(type, rowId);
            } else if (dataTableOperation.rowUpdateState[type]) {
                tableObd.updateOptionData(row);
            }
            if (curDbSubscribeMOnitor !== '' && curDbSubscribeMOnitor !== undefined && curDbSubscribeMOnitor !== null) {// 直接双击的监控对象置顶显示
                if (rowId !== tableObd.getState().lastLockedRowId) {
                    tableObd.setLockedRow(rowId);
                    curDbSubscribeMOnitor = '';
                }
            }
        }
        if (uptFlag !== false) {
            dataTableOperation.carStateAdapt(activeIndex);
        }
        dataTableOperation.dataTableColumnsDatas(row);
    },
    dataTableColumnsDatas: function (row) {
        // console.log(row, 'dataTableOperationdataTableOperation');
        if (!row.id) return;
        dataTableOperation.dataTableColumnsDatass = row;
    },
    dataTableColumnsDatass: {},
    getAddressback: function (data) {
        carAddress = data;
    },
    getaddressParticulars: function (AddressNew, longitude, latitude) {
        var addressParticulars = {
            "longitude": Number(longitude).toFixed(3),
            "latitude": Number(latitude).toFixed(3),
            "adcode": AddressNew.regeocode.addressComponent.adcode,//区域编码
            "building": AddressNew.regeocode.addressComponent.building,//所在楼/大厦
            "buildingType": AddressNew.regeocode.addressComponent.buildingType,
            "city": AddressNew.regeocode.addressComponent.city,
            "cityCode": AddressNew.regeocode.addressComponent.citycode,
            "district": AddressNew.regeocode.addressComponent.district,//所在区
            "neighborhood": AddressNew.regeocode.addressComponent.neighborhood,//所在社区
            "neighborhoodType": AddressNew.regeocode.addressComponent.neighborhoodType,//社区类型
            "province": AddressNew.regeocode.addressComponent.province,//省
            "street": AddressNew.regeocode.addressComponent.street,//所在街道
            "streetNumber": AddressNew.regeocode.addressComponent.streetNumber,//门牌号
            "township": AddressNew.regeocode.addressComponent.township,//所在乡镇
            "crosses": "",
            "pois": "",
            "roads": "",//道路名称
            "formattedAddress": AddressNew.regeocode.formattedAddress,//格式化地址
        };
        return JSON.stringify(addressParticulars);
    },
    toDirectionStr: function (angle) {
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
            direction = '未知数据';
        }
        return direction;
    },
    //点击页面隐藏相应的ul下拉列表
    updateRow: function (table, dataSet, obj, type, earlyAlarmStartTime) {
        if (type === 'state') {//状态信息(车)
            dataTableOperation.dataTableList(stateName, obj, "realTimeStateTable", "state", null);
        }
        else if (type === 'obd') {//obd数据
            dataTableOperation.dataTableList(obdName, obj, "obdInfoTable", "obd", null);
        }
        else if (type === 'alarm') {//报警记录(车)
            dataTableOperation.dataTableList(alarmName, obj, "alarmTable", "alarm", earlyAlarmStartTime);
        }
    },
    //报警处理
    warningManage: function (data) {
        $('.sendTextFooter').hide();
        $('.takePicturesFooter').hide();
        $("#alarm-remark").show();
        $("#smsTxt").val("");
        $("#time").val("");
        $("#alarmRemark").val("");
        pageLayout.closeVideo();
        layer.closeAll();
        $('#warningManage').modal('show');
        var dataArray = data.split('|');
        var url = "/clbs/v/monitoring/getDeviceTypeByVid";
        var params = {"vehicleId": dataArray[13]};
        var warningType = "";
        json_ajax("POST", url, "json", false, params, function (result) {
            warningType = result.obj.warningType;
            deviceTypeTxt = result.obj.deviceType;
        });

        if (deviceTypeTxt === '11' || deviceTypeTxt === '20' || deviceTypeTxt === '21' || deviceTypeTxt === '24' || deviceTypeTxt === '25' || deviceTypeTxt === '26' || deviceTypeTxt === '28') {
            $('.newDeviceInfo').show();
            $('.oldDeviceInfo').hide();
            $('.minResolution, .maxResolution').show();
            $('.defaultValue').attr('selected', false);
        } else {
            $('.newDeviceInfo').hide();
            $('.oldDeviceInfo').show();
            $('.minResolution, .maxResolution').hide();
            $('.defaultValue').attr('selected', true);
        }
        var alarmType = dataArray[11].split(',');
        for (var i = 0; i < alarmType.length; i++) {
            var flag = $.inArray(alarmType[i], alarmTypeList);
            var flag1 = $.inArray(alarmType[i], continueAlarmsPosList);
            var flag2 = $.inArray(alarmType[i], ioAlarmTypeList);

            if (flag !== -1 || flag1 !== -1 || flag2 !== -1) {
                $("#warningManagePhoto").attr("disabled", "disabled");
                $("#warningManageSend").attr("disabled", "disabled");
                $("#warningManageAffirm").attr("disabled", "disabled");
                $("#warningManageFuture").attr("disabled", "disabled");
                $("#warningManageCancel").attr("disabled", "disabled");
                $("#color").show();
                $("#color").text(alarmDisabled);
                break;
            }
        }
        // 持续性报警结束时间不会为"0", 因此如果是持续性报警无需判断此逻辑
        var url1 = "/clbs/a/search/findEndTime";
        var data1 = {"vehicleId": dataArray[13], "type": dataArray[11], "startTime": dataArray[1]};
        layer.load(2);
        json_ajax("POST", url1, "json", true, data1, function (result) {
            if (result.success === true) {
                if (result.msg === "0") {
                    $("#color").show();
                    $("#color").text(alarmError);
                    $("#warningManageListening").attr("disabled", "disabled");
                    $("#warningManagePhoto").attr("disabled", "disabled");
                    $("#warningManageSend").attr("disabled", "disabled");
                    $("#warningManageAffirm").attr("disabled", "disabled");
                    $("#warningManageFuture").attr("disabled", "disabled");
                    $("#warningManageCancel").attr("disabled", "disabled");
                } else {
                    $("#color").show();
                    $("#color").text(alarmDisabled);
                }
            } else {
                $("#warningManageListening").removeAttr("disabled");
                $("#warningManagePhoto").removeAttr("disabled");
                $("#warningManageSend").removeAttr("disabled");
                $("#warningManageAffirm").removeAttr("disabled");
                $("#warningManageFuture").removeAttr("disabled");
                $("#warningManageCancel").removeAttr("disabled");
                $("#color").hide();
                $("#colorMore").hide();
                layer.closeAll();
            }
        });
        $("#listeningContent,#takePicturesContent,#sendTextMessages").hide();
        if (warningType === true || dataArray[12] === "1") {
            // $("#warningHiden").removeAttr("style");
            $("#warningManageListening").hide();
            $("#warningManagePhoto").hide();
            $("#warningManageSend").hide();
            $("#sno").val("0");
        } else {
            $("#warningHiden").attr("style", "text-align:center");
            $("#warningManageListening").show();
            $("#warningManagePhoto").show();
            $("#warningManageSend").show();
            $("#sno").val(dataArray[10]);
        }


        $("#warningCarName").text(dataArray[0]);
        $("#warningTime").text(dataArray[1]);
        $("#warningGroup").text(dataArray[2]);
        $("#warningDescription").text(dataArray[5]);
        $("#earlyAlarmStartTime").val(dataArray[14]);
        $("#startTime").val(dataArray[15]);
        // 只有是车的类型才显示从业人员
        if (dataArray[3] !== '车') {
            $("#professionLabel").hide();
            $("#professionValue").hide();
        } else {
            $("#professionLabel").show();
            $("#professionValue").show();
        }
        $("#warningPeo").text(dataArray[6]);
        $("#simcard").val(dataArray[8]);
        $("#device").val(dataArray[9]);
        $("#warningType").val(dataArray[11]);
        $("#vUuid").val(dataArray[13]);
        var parameter = {"vehicleId": dataArray[13], "alarm": dataArray[5]};
        json_ajax("POST", "/clbs/v/monitoring/getAlarmParam", "json", true, parameter, dataTableOperation.getAlarmParam);
    },
    getAlarmParam: function (data) {
        $(".warningDeal").hide();
        var len = data.obj.length;
        var valueList = data.obj;
        if (len !== 0) {
            for (var i = 0; i < len; i++) {
                var valueListElement = valueList[i];
                var name = valueListElement.name;
                var value = valueListElement.parameterValue;
                var paramCode = valueListElement.paramCode;
                var type = valueListElement.type;
                if (name === "超速预警") {
                    $("#overSpeedGap").show();
                    $("#overSpeedGapValue").text(value);
                }
                if (name === "疲劳驾驶预警") {
                    $("#tiredDriveGap").show();
                    $("#tiredDriveGapValue").text(value);
                }
                if (name === "碰撞预警") {
                    $("#crashWarning").show();
                    if (paramCode === "param1") {
                        $("#crashTime").text(value);
                    } else if (paramCode === "param2") {
                        $("#crashSpeed").text(value);
                    }
                }
                if (name === "侧翻预警") {
                    $("#turnOnWarning").show();
                    $("#turnOnValue").text(value);
                }
                // 此处仅展示终端超速报警
                if (name === "超速报警" && type === 'driverAlarm') {
                    $("#overSpeeds").show();
                    if (paramCode === "param1") {
                        $("#warningSpeed").text(value);
                    } else if (paramCode === "param2") {
                        $("#warningAllTime").text(value);
                    }
                }
                if (name === "疲劳驾驶") {
                    $("#tiredDrive").show();
                    if (paramCode === "param1") {
                        $("#continuousDriveTime").text((value && value !== "null") ? value : "");
                    } else if (paramCode === "param2") {
                        $("#breakTime").text(value);
                    }
                }
                if (name === "当天累积驾驶超时") {
                    $("#addUpDrive").show();
                    $("#addUpDriveTime").text(value);
                }
                if (name === "超时停车") {
                    $("#overTimeStop").show();
                    $("#overTimeStopTime").text(value);
                }
                if (name === "凌晨2-5点行驶报警") {
                    $("#earlyRun").show();
                    $("#earlyRunValue").text(value);
                }
                if (name === "车辆非法位移") {
                    $("#displacementCar").show();
                    $("#displacementCarDistance").text(value);
                }
                if (name === "车机疑似屏蔽报警") {
                    $("#shieldWarning").show();
                    if (paramCode === "param1") {
                        $("#offLineTime").text(value);
                    } else if (paramCode === "param2") {
                        $("#offLineStartTime").text(value);
                    } else if (paramCode === "param3") {
                        $("#offLineEndTime").text(value);
                    }
                }
            }
        }
    },
    // 切换监听视图
    getListen: function (data) {
        //拍照参数显示隐藏
        if ($("#listeningContent").is(":hidden")) {
            $("#listeningContent").slideDown();
            $('.listenFooter').show();
            $("#takePicturesContent").hide();
            $('.takePicturesFooter').hide();
            $("#sendTextMessages").hide();
            $('.sendTextFooter').hide();
        } else {
            $("#listeningContent").slideUp();
            $('.listenFooter').hide();
        }
        setTimeout("dataTableOperation.logFindCilck()", 500);
    },
    // 拍照
    photo: function () {
        dataTableOperation.getPhoto();
    },
    getPhoto: function (data) {
        //拍照参数显示隐藏
        if ($("#takePicturesContent").is(":hidden")) {
            $("#takePicturesContent").slideDown();
            $('.takePicturesFooter').show();
            $("#sendTextMessages").hide();
            $('.sendTextFooter').hide();
            $("#listeningContent").hide();
            $('.listenFooter').hide();
        } else {
            $("#takePicturesContent").slideUp();
            $('.takePicturesFooter').hide();
        }
        setTimeout("dataTableOperation.logFindCilck()", 500);
    },
    send: function () {
        if ($("#sendTextMessages").is(":hidden")) {
            $("#sendTextMessages").slideDown();
            $('.sendTextFooter').show();
            $("#takePicturesContent").hide();
            $('.takePicturesFooter').hide();
            $("#listeningContent").hide();
            $('.listenFooter').hide();
        } else {
            $("#sendTextMessages").slideUp();
            $('.sendTextFooter').hide();
        }
        setTimeout("dataTableOperation.logFindCilck()", 500);
    },
    handleAlarm: function (handleType) {
        var startTime = $("#startTime").val();
        var plateNumber = $("#warningCarName").text();
        var description = $("#warningDescription").text();
        var vehicleId = $("#vUuid").val();
        var simcard = $('#simcard').val();
        var device = $("#device").val();
        var sno = $("#sno").val();
        var alarm = $("#warningType").val();
        var remark = $("#alarmRemark").val();
        var url = "/clbs/v/monitoring/handleAlarm";
        var data = {
            "vehicleId": vehicleId,
            "plateNumber": plateNumber,
            "alarm": alarm,
            "description": description,
            "handleType": handleType,
            "startTime": startTime,
            "simcard": simcard,
            "device": device,
            "sno": sno,
            "remark": remark
        };
        json_ajax("POST", url, "json", true, data, function () {
            // 报警处理完毕后，延迟3秒进行结果查询
            setTimeout(pagesNav.gethistoryno, 3000);
        });
        $("#warningManage").modal('hide');
        dataTableOperation.updateHandleStatus(vehicleId);
        setTimeout("dataTableOperation.logFindCilck()", 500);
    },
    // 更新报警处理状态
    updateHandleStatus: function (vid) {
        tableAlarm.updateOptionData({
            id: vid,
            handleStatus: 1
        });
    },
    // 取消订阅后删除对应表格信息
    deleteRowByRealTime: function (plateNumber) {
        var stateArr = [], obdArr = [];
        for (var i = 0; i < plateNumber.length; i++) {
            //车辆状态信息
            if (stateName.indexOf(plateNumber[i]) !== -1) {
                stateArr.push(plateNumber[i]);
                stateIndex--;
                stateName.splice(stateName.indexOf(plateNumber[i]), 1);
            }
            if (obdName.indexOf(plateNumber[i]) !== -1) {
                obdArr.push(plateNumber[i]);
                obdIndex--;
                obdName.splice(obdName.indexOf(plateNumber[i]), 1);
            }
        }
        tableStatus.deleteOptionData(stateArr);
        tableObd.deleteOptionData(obdArr);
        dataTableOperation.carStateAdapt(activeIndex);
        dataTableOperation.tableRank('realTimeStateTable');
        dataTableOperation.tableRank('obdInfoTable');
        dataTableOperation.tableRank('alarmTable');
    },
    // 监控对象列表单双击
    dataTableDbclick: function (type) {
        var thisID = dataTableOperation.confirmID(type);

        $("#" + thisID).children("tbody").children("tr").unbind("click").bind("click", function () {
            //判断当前单击后的信息是否高亮
            if ($(this).hasClass("tableHighlight") || $(this).hasClass("tableHighlight-blue")) {
                //清除车辆树高亮效果
                var plateInformationName = $(this).children("td:nth-child(2)").attr('id').split('_')[0];
                if (licensePlateInformation === plateInformationName) {
                    $(".ztree li a").removeAttr("class", "curSelectedNode");
                    $("#" + dbclickCheckedId).parent().removeAttr("class", "curSelectedNode_dbClick");
                }
                if (groupIconSkin === "assignmentSkin" || groupIconSkin === "groupSkin") {
                    $(".ztree li a").removeAttr("class", "curSelectedNode");
                    $("#" + dbclickCheckedId).parent().removeAttr("class", "curSelectedNode_dbClick");
                }
                //清除数据表高亮效果
                $(this).removeClass("tableHighlight");
                $(this).removeClass("tableHighlight-blue");
                $(".ztree li a").removeClass("curSelectedNode_dbClick");
                $(".ztree li a").removeClass("curSelectedNode");
                //取消聚焦跟踪
                treeMonitoring.centerMarkerNo();
            } else {
                $("#" + thisID).children("tbody").children("tr").removeClass("tableHighlight");
                $("#" + thisID).children("tbody").children("tr").removeClass("tableHighlight-blue");
                //为表格添加高亮
                var numberPlate = $(this).children("td:nth-child(2)").attr('id').split('_')[0];
                var realTimeDataTableTrNum = $("#realTimeStateTable").find("tr").length;
                for (var i = 0; i < realTimeDataTableTrNum; i++) {
                    $(this).addClass("tableHighlight");
                }
                $(".ztree li a").removeClass("curSelectedNode_dbClick");
                $(".ztree li a").removeClass("curSelectedNode");
                //为车辆树添加高亮
                var zTreeDataTables = $.fn.zTree.getZTreeObj("treeDemo");
                var dataTabCheckedNum = zTreeDataTables.getCheckedNodes(true);
                for (var i = 0; i < dataTabCheckedNum.length; i++) {
                    if (dataTabCheckedNum[i].id === numberPlate) {
                        ztreeStyleDbclick = dataTabCheckedNum[i].tId;
                        $("#" + ztreeStyleDbclick).children("a").addClass("curSelectedNode_dbClick");
                    }
                }
                var $this = $(this);
                TimeFn = setTimeout(function () {
                    var objID = $this.children("td:nth-child(2)").attr('data-id');
                    //聚焦跟踪执行方法
                    // dataTableOperation.centerMarkerBands(objID);
                    treeMonitoring.centerMarker(objID, 'DBLCLICK');
                }, 300);
            }
        });
        $("#" + thisID).children("tbody").children("tr").unbind("dblclick").bind("dblclick", function () {
            var nodeName = $(this).children("td:nth-child(2)").attr('id').split('_')[0];
            dataTableOperation.tableHighlight(type, nodeName);
            //为表格添加高亮
            var numberPlate = $(this).children("td:nth-child(2)").attr('id').split('_')[0];
            var realTimeDataTableTrNum = $("#realTimeStateTable").find("tr").length;
            for (var i = 0; i < realTimeDataTableTrNum; i++) {
                $(this).addClass("tableHighlight");
            }
            $(".ztree li a").removeClass("curSelectedNode_dbClick");
            $(".ztree li a").removeClass("curSelectedNode");
            //为车辆树添加高亮
            var zTreeDataTables = $.fn.zTree.getZTreeObj("treeDemo");
            var dataTabCheckedNum = zTreeDataTables.getCheckedNodes(true);
            for (var i = 0; i < dataTabCheckedNum.length; i++) {
                if (dataTabCheckedNum[i].id === numberPlate) {
                    ztreeStyleDbclick = dataTabCheckedNum[i].tId;
                    $("#" + ztreeStyleDbclick).children("a").addClass("curSelectedNode_dbClick");
                }
            }
            //聚焦跟踪执行方法
            var objID = $(this).children("td:nth-child(2)").attr('data-id');
            // dataTableOperation.centerMarkerBands(objID);
            treeMonitoring.centerMarker(objID, 'DBLCLICK');
            clearTimeout(TimeFn);
        });
    },
    //报警记录单双击
    alarmInfoDataDbclick: function (type) {
        var thisID = dataTableOperation.confirmID(type);
        var alarmTimeFn = null;
        $("#" + thisID).children("tbody").children("tr").unbind("click").bind("click", function () {
            clearTimeout(alarmTimeFn);
            $("#" + thisID).children("tbody").children("tr").removeClass("tableHighlight-blue");
            var alarmDataTableTrNum = $("#" + thisID).find("tr").length;
            for (var i = 0; i < alarmDataTableTrNum; i++) {
                $(this).addClass("tableHighlight-blue");
            }
            alarmTimeFn = setTimeout(function () {
            }, 300);
        });
        $("#" + thisID).children("tbody").children("tr").unbind("dblclick").bind("dblclick", function () {
            clearTimeout(alarmTimeFn);
            var alarmVid;
            var timeFormat;
            var alarmStr;
            var earlyAlarmStartTime;
            var alarmTypeArr;
            //获取当前点击行相对应的值
            if (type === 'alarm') {
                alarmVid = $(this).children("td:nth-child(2)").attr("data-id");
                timeFormat = $(this).children("td:nth-child(3)").text();
                alarmStr = $(this).children("td:nth-child(8)").text();
                earlyAlarmStartTime = $(this).children("td:nth-child(3)").attr("data-type");
                alarmTypeArr = $(this).children("td:nth-child(8)").attr("data-alarmtype");
            } else if (type === 'peopleAlarm') {
                alarmVid = $(this).children("td:nth-child(2)").attr("data-id");
                timeFormat = $(this).children("td:nth-child(3)").text();
                alarmStr = $(this).children("td:nth-child(6)").text();
                earlyAlarmStartTime = $(this).children("td:nth-child(3)").attr("data-type");
                alarmTypeArr = $(this).children("td:nth-child(8)").attr("data-alarmtype");
            }
            // 判断是否有报警查询的菜单权限
            var alarmFlag = false;
            var permissionUrls = $("#permissionUrls").val();
            if (permissionUrls !== null && permissionUrls !== undefined) {
                var urllist = permissionUrls.split(",");
                if (urllist.indexOf("/a/search/list") > -1) {
                    alarmFlag = true;
                    //跳转
                    if (earlyAlarmStartTime == null || typeof(earlyAlarmStartTime) == "undefined" || earlyAlarmStartTime === 0) {
                        earlyAlarmStartTime = timeFormat;
                    }
                    location.href = "/clbs/a/search/list?avid=" + alarmVid + "&atype=0" + "&atime=" + earlyAlarmStartTime + "&alarmTypeArr=" + alarmTypeArr;
                }
            }
            if (!alarmFlag) {
                layer.msg("无操作权限，请联系管理员");
            }
        });
    },
    //列表高亮
    tableHighlight: function (type, name) {
        var thisID = dataTableOperation.confirmID(type);
        if (thisID === 'realTimeStateTable') {
            if (tableStatus.getState().lastLockedRowId !== name) {
                dbClickHeighlight = false;
                tableStatus.setActiveRow(undefined);
                tableStatus.setLockedRow(name);
                newTableOperation.lastLockedRowId = name;
                newTableOperation.lastClickRowId = undefined;
            }
            if (tableObd.getState().lastLockedRowId !== name) {
                dbClickHeighlight = false;
                tableObd.setActiveRow(undefined);
                tableObd.setLockedRow(name);
                newTableOperation.lastLockedRowId = name;
                newTableOperation.lastClickRowId = undefined;
            }
        }
    },
    cancelActiveAndLockRow: function () {
        if (newTableOperation.lastLockedRowId) {
            tableStatus.setLockedRow(undefined);
            newTableOperation.lastLockedRowId = undefined;
        }
        if (newTableOperation.lastClickRowId) {
            tableStatus.setActiveRow(undefined);
            newTableOperation.lastClickRowId = undefined;
        }
    },
    //列表高亮-蓝色
    tableHighlightBlue: function (type, name) {
        var thisID = dataTableOperation.confirmID(type);
        if (thisID === 'realTimeStateTable') {
            if (tableStatus.getState().lastClickRowId !== name) {
                dbClickHeighlight = false;
                tableStatus.setLockedRow(undefined);
                tableStatus.setActiveRow(name);
                newTableOperation.lastClickRowId = name;
                newTableOperation.lastLockedRowId = undefined;
            }
            if (tableObd.getState().lastClickRowId !== name) {
                dbClickHeighlight = false;
                tableObd.setLockedRow(undefined);
                tableObd.setActiveRow(name);
                newTableOperation.lastClickRowId = name;
                newTableOperation.lastLockedRowId = undefined;
            }
        }
    },
    freezeHeight: false,
    logFindCilck: function (freezeHeight) {
        dataTableOperation.freezeHeight = !!freezeHeight;
        if (clickLogCount === 0) {
            // 终端上报日志updataFenceData
            webSocket.subscribe(headers, '/topic/deviceReportLog', function () {
                if (logFlag === "true") {
                    var data = {"eventDate": logTime};
                    address_submit("POST", '/clbs/m/reportManagement/logSearch/findLog', "json", false, data, true, dataTableOperation.logFind);
                }
            }, null, null);
            webSocket.subscribe(headers, '/user/topic/deviceReportLog', function () {
                if (logFlag === "true") {
                    var data = {"eventDate": logTime};
                    address_submit("POST", '/clbs/m/reportManagement/logSearch/findLog', "json", false, data, true, dataTableOperation.logFind);
                }
            }, null, null);
            clickLogCount = 1;
        }
        var data = {"eventDate": logTime};
        address_submit("POST", '/clbs/m/reportManagement/logSearch/findLog', "json", false, data, true, dataTableOperation.logFind);
    },
    logFind: function (data) {
        var logType = "";
        var content = "";
        var optionData;
        var appendFlag = false;
        // for (var i = 0; i < data.length; i++) {
        for (var i = data.length - 1; i >= 0; i--) {
            if (data[i].logSource === "1") {
                logType = '终端上传';
                content = "<a href='#' onclick='dataTableOperation.showLogContent(\"" + data[i].message + "\")'>" + data[i].monitoringOperation + "</a>";
            } else if (data[i].logSource === "2") {
                logType = '平台下发';
                content = data[i].message;
            } else {
                logType = '平台操作';
                content = data[i].message;
            }
            optionData = tableLog.getOption().data;
            if (optionData != null && optionData.length > 0) {
                for (var index = 0; index < optionData.length; index++) {
                    // 去掉重复的记录
                    if (optionData[index].eventDate === data[i].eventDate && (optionData[index].username === data[i].username || data[i].username == null) && optionData[index].content === content) {
                        appendFlag = true;
                        break;
                    }
                    appendFlag = false;
                }
            }
            if (!appendFlag) {
                var row = {
                    sequenceNumber: 0,
                    eventDate: data[i].eventDate,
                    ipAddress: data[i].ipAddress != null ? data[i].ipAddress : "",
                    username: data[i].username != null ? data[i].username : "",
                    brand: data[i].brand,
                    plateColorStr: data[i].plateColorStr,
                    content: content,
                    logType: logType,
                    id: Math.random().toString()
                };
                tableLog.prependOptionData([row]);
            }
            if (!dataTableOperation.freezeHeight) {
                dataTableOperation.carStateAdapt(activeIndex);
            }
        }
    },
    showLogContent: function (content) { // 显示log详情
        pageLayout.closeVideo();
        $("#logDetailDiv").modal("show");
        $("#logContent").html(content);
    },
    takePhoto: function () {
        if (dataTableOperation.photoValidate()) {
            $("#takePhoto").ajaxSubmit(function (data) {
                $("#goPhotograph").modal("hide");
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess);
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                } else {
                    layer.msg(JSON.parse(data).msg);
                }
            });
        }
    },
    // 监听下发
    listenForAlarm: function () {
        if (dataTableOperation.listenValidate()) {
            // 为车id赋值
            var vehicleId = $("#vUuid").val();
            $("#vidforAlarmListen").val(vehicleId);
            $("#brandListen").val($("#warningCarName").text());
            $("#alarmListen").val($("#warningType").val());
            $("#startTimeListen").val($('#startTime').val());

            $("#simcardListen").val($('#simcard').val());
            $("#deviceListen").val($("#device").val());
            $("#snoListen").val($("#sno").val());
            $("#handleTypeListen").val("监听");
            $("#descriptionListen").val($("#warningDescription").text());
            $("#remarkListen").val($("#alarmRemark").val());
            $("#goListeningForAlarm").attr("disabled", "disabled");
            $("#listeningAlarm").ajaxSubmit(function (data) {
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess);
                    dataTableOperation.updateHandleStatus(vehicleId);
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                } else {
                    layer.msg(publicIssuedFailure);
                }
                $("#warningManage").modal('hide');
                $("#goListeningForAlarm").removeAttr("disabled");
            });
        }
        $("#goListeningForAlarm").removeAttr("disabled");
    },
    listenValidate: function () {
        return $("#listeningAlarm").validate({
            rules: {
                monitorPhone: {
                    isNewTel: true,
                    required: true
                },
            },
            messages: {
                monitorPhone: {
                    required: '请输入电话号码'
                },
            }
        }).form();
    },
    //
    takePhotoForAlarm: function () {
        if (dataTableOperation.photoValidateForAlarm()) {
            // 为车id赋值
            var vehicleId = $("#vUuid").val();
            $("#vidforAlarm").val(vehicleId);
            $("#brandPhoto").val($("#warningCarName").text());
            $("#alarmPhoto").val($("#warningType").val());
            $("#startTimePhoto").val($('#startTime').val());

            $("#simcardPhoto").val($('#simcard').val());
            $("#devicePhoto").val($("#device").val());
            $("#snoPhoto").val($("#sno").val());
            $("#handleTypePhoto").val("拍照");
            $("#description-photo").val($("#warningDescription").text());
            $("#remark-photo").val($("#alarmRemark").val());
            $("#goPhotographsForAlarm").attr("disabled", "disabled");
            $("#takePhotoForAlarm").ajaxSubmit(function (data) {
                $("#warningManage").modal('hide');
                $("#goPhotographsForAlarm").removeAttr("disabled");
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess);
                    dataTableOperation.updateHandleStatus(vehicleId);
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                } else {
                    layer.msg(publicIssuedFailure);
                }

            });
        }
        $("#goPhotographsForAlarm").removeAttr("disabled");
    },
    getVideo: function () {
        if ($("#vtime").val() == "0") {
            $(".recording-timeline").show();
            $("#videoPlay").attr("class", "pause");
            videoTimeIndex = 1;
            videoPlay.src = "../../resources/img/pause.png";
            time = setInterval(function () {
                $("#videoTime").html((videoTimeIndex++) + "秒");
            }, 1000);
        }
        if (dataTableOperation.videoValidate()) {
            $("#getVideo").ajaxSubmit(function (data) {
                $("#goVideotape").modal("hide");
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess)
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                } else {
                    layer.msg(JSON.parse(data).msg)
                }
            });
        }
    },
    goRegularReport: function () {
        if (dataTableOperation.regularReportValidate()) {
            $("#regularReport").ajaxSubmit(function (data) {
                $("#continuousReturnTiming").modal("hide");
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess)
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                }
            });
        }
    },
    regularReportValidate: function () {
        return $("#timeInterval0").validate({
            rules: {
                driverLoggingOutUpTimeSpace: {
                    required: true,
                    digits: true,
                    max: 4294967295,
                    min: 1
                },
                dormancyUpTimeSpace: {
                    required: true,
                    digits: true,
                    max: 4294967295,
                    min: 1
                },
                emergencyAlarmUpTimeSpace: {
                    required: true,
                    digits: true,
                    max: 4294967295,
                    min: 1
                },
                defaultTimeUpSpace: {
                    required: true,
                    digits: true,
                    max: 4294967295,
                    min: 1
                }
            },
            messages: {
                driverLoggingOutUpTimeSpace: {
                    required: drivingTimeNull,
                    digits: publicNumberInt,
                    max: publicSizeMoreLength
                },
                dormancyUpTimeSpace: {
                    required: sleepTimeNull,
                    digits: publicNumberInt,
                    max: publicSizeMoreLength,
                },
                emergencyAlarmUpTimeSpace: {
                    required: sosTimeNull,
                    digits: publicNumberInt,
                    max: publicSizeMoreLength,
                },
                defaultTimeUpSpace: {
                    required: defaultTimeNull,
                    digits: publicNumberInt,
                    max: publicSizeMoreLength,
                }
            }
        }).form();
    },
    goDistanceReport: function () {
        if (dataTableOperation.distanceReportValidate()) {
            $("#distanceReport").ajaxSubmit(function (data) {
                $("#continuousReturnFixedDistance").modal("hide");
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess)
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                }
            });
        }
    },
    distanceReportValidate: function () {
        return $("#timeInterval1").validate({
            rules: {
                driverLoggingOutUpDistanceSpace: {
                    required: true,
                    digits: true,
                    max: 4294967295,
                    min: 1
                },
                dormancyUpDistanceSpace: {
                    required: true,
                    digits: true,
                    max: 4294967295,
                    min: 1
                },
                emergencyAlarmUpDistanceSpace: {
                    required: true,
                    digits: true,
                    max: 4294967295,
                    min: 1
                },
                defaultDistanceUpSpace: {
                    required: true,
                    digits: true,
                    max: 4294967295,
                    min: 1
                }
            },
            messages: {
                driverLoggingOutUpDistanceSpace: {
                    required: drivingTimeNull,
                    digits: publicNumberInt,
                    max: publicSizeMoreLength
                },
                dormancyUpDistanceSpace: {
                    required: sleepTimeNull,
                    digits: publicNumberInt,
                    max: publicSizeMoreLength,
                },
                emergencyAlarmUpDistanceSpace: {
                    required: sosTimeNull,
                    digits: publicNumberInt,
                    max: publicSizeMoreLength,
                },
                defaultDistanceUpSpace: {
                    required: defaultTimeNull,
                    digits: publicNumberInt,
                    max: publicSizeMoreLength,
                }
            }
        }).form();
    },
    goTimeInterval: function () {
        var continuousReturnValue = Number($("#continuousReturnValue").val());
        if (continuousReturnValue === 0) {
            if (!dataTableOperation.regularReportValidate()) {
                return;
            }
            $("#timeInterval0").ajaxSubmit(function (data) {
                $("#continuousReturnTimingDistance").modal("hide");
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess)
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                }
            });

        } else if (continuousReturnValue === 1) {
            if (!dataTableOperation.distanceReportValidate()) {
                return;
            }
            $("#timeInterval1").ajaxSubmit(function (data) {
                $("#continuousReturnTimingDistance").modal("hide");
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess)
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                }
            });

        } else if (continuousReturnValue === 2) {
            if (!dataTableOperation.timeIntervalValidate()) {
                return;
            }
            $("#timeInterval2").ajaxSubmit(function (data) {
                $("#continuousReturnTimingDistance").modal("hide");
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess)
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                }
            });

        } else {
            layer.msg("请选择回报类型");
        }

    },
    timeIntervalValidate: function () {
        return $("#timeInterval2").validate({
            rules: {
                driverLoggingOutUpTimeSpace: {
                    required: true,
                    digits: true,
                    max: 4294967295,
                    min: 1
                },
                dormancyUpTimeSpace: {
                    required: true,
                    digits: true,
                    max: 4294967295,
                    min: 1
                },
                emergencyAlarmUpTimeSpace: {
                    required: true,
                    digits: true,
                    max: 4294967295,
                    min: 1
                },
                defaultTimeUpSpace: {
                    required: true,
                    digits: true,
                    max: 4294967295,
                    min: 1
                },
                driverLoggingOutUpDistanceSpace: {
                    required: true,
                    digits: true,
                    max: 4294967295,
                    min: 1
                },
                dormancyUpDistanceSpace: {
                    required: true,
                    digits: true,
                    max: 4294967295,
                    min: 1
                },
                emergencyAlarmUpDistanceSpace: {
                    required: true,
                    digits: true,
                    max: 4294967295,
                    min: 1
                },
                defaultDistanceUpSpace: {
                    required: true,
                    digits: true,
                    max: 4294967295,
                    min: 1
                }
            },
            messages: {
                driverLoggingOutUpTimeSpace: {
                    required: drivingTimeNull,
                    digits: publicNumberInt,
                    max: publicSizeMoreLength
                },
                dormancyUpTimeSpace: {
                    required: sleepTimeNull,
                    digits: publicNumberInt,
                    max: publicSizeMoreLength,
                },
                emergencyAlarmUpTimeSpace: {
                    required: sosTimeNull,
                    digits: publicNumberInt,
                    max: publicSizeMoreLength,
                },
                defaultTimeUpSpace: {
                    required: defaultTimeNull,
                    digits: publicNumberInt,
                    max: publicSizeMoreLength,
                },
                driverLoggingOutUpDistanceSpace: {
                    required: drivingTimeNull,
                    digits: publicNumberInt,
                    max: publicSizeMoreLength
                },
                dormancyUpDistanceSpace: {
                    required: sleepTimeNull,
                    digits: publicNumberInt,
                    max: publicSizeMoreLength,
                },
                emergencyAlarmUpDistanceSpace: {
                    required: sosTimeNull,
                    digits: publicNumberInt,
                    max: publicSizeMoreLength,
                },
                defaultDistanceUpSpace: {
                    required: defaultTimeNull,
                    digits: publicNumberInt,
                    max: publicSizeMoreLength,
                }
            }
        }).form();
    },
    gpListening: function () {
        if (dataTableOperation.listeningValidate()) {
            $("#listening").ajaxSubmit(function (data) {
                $("#monitoringObjectListening").modal("hide");
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess)
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                } else {
                    layer.msg(JSON.parse(data).msg)
                }
            });
        }
    },
    reportSet: function () {
        var ht = $("#hours").val();
        var mt = $("#minute").val();
        if ((ht !== "" && !/^[0-9]+$/.test(ht)) || (mt !== "" && !/^[0-9]+$/.test(mt))) {//输入了非法字符
            layer.msg("上报频率时间只能输入正整数");
        } else {//正常校验
            var hours = parseInt(ht);
            var minute = parseInt(mt);
            if (isNaN(hours) && isNaN(minute)) {
                $("#locationNumber").val(86400);
            } else if (isNaN(hours) && !isNaN(minute)) {
                $("#locationNumber").val(minute * 60);
            } else if (!isNaN(hours) && isNaN(minute)) {
                $("#locationNumber").val(hours * 60 * 60);
            } else if (!isNaN(hours) && !isNaN(minute)) {
                $("#locationNumber").val(hours * 60 * 60 + minute * 60);
            }
            var reportSetFalg = false;
            var locationNumber = $("#locationNumber").val();
            if (locationNumber !== 0 && locationNumber < 300) {
                layer.msg("上报间隔最小为5分钟");
            } else {
                reportSetFalg = true;
            }
            if (reportSetFalg && dataTableOperation.reportSetValidate()) {
                $("#reportFrequency").ajaxSubmit(function (data) {
                    $("#reportFrequencySet").modal("hide");
                    if (JSON.parse(data).obj.type) {
                        layer.msg(publicIssuedSuccess);
                        setTimeout("dataTableOperation.logFindCilck()", 500);
                    } else {
                        layer.msg(publicIssuedError);
                    }
                });
            }
        }
    },
    goInfofixedPointAndTiming: function () {
        if (dataTableOperation.goInfofixedValidate()) {
            $("#fixedPointTimingList").ajaxSubmit(function (data) {
                if (JSON.parse(data).obj.type) {
                    $("#fixedPointAndTiming").modal("hide");
                    layer.msg(publicIssuedSuccess);
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                } else {
                    layer.msg("指令发送失败,相邻时间点的间隔必须大于等于300秒");
                }
            });
        }
    },
    positionTrailing: function () {
        if (dataTableOperation.positionTrailingValidate()) {
            $("#locationTailAfterList").ajaxSubmit(function (data) {
                $("#locationTailAfter").modal("hide");
                if (JSON.parse(data).obj.type) {
                    layer.msg(publicIssuedSuccess);
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                } else {
                    layer.msg(publicIssuedError);
                }
            });
        }
    },
    listeningValidate: function () {
        return $("#listening").validate({
            rules: {
                regRet: {
                    isTel: true,
                    required: true
                }
            },
            messages: {
                regRet: {
                    isTel: phoneError,
                    required: phoneNull
                }
            }
        }).form();
    },
    reportSetValidate: function () {
        return $("#reportFrequency").validate({
            rules: {
                locationPattern: {
                    required: true
                },
                requiteTime: {
                    checkRequiteTime: longDeviceType
                }
            },
            messages: {
                locationPattern: {
                    required: positionNull
                },
                requiteTime: {
                    checkRequiteTime: reportNull
                }
            }
        }).form();
    },
    goInfofixedValidate: function () {
        return $("#fixedPointTimingList").validate({
            rules: {
                locationTimes: {
                    checkLocationTimes: true
                }
            },
            messages: {
                locationTimes: {
                    checkLocationTimes: fixedPointNull
                }
            }
        }).form();
    },
    positionTrailingValidate: function () {
        return $("#locationTailAfterList").validate({
            rules: {
                longValidity: {
                    required: true,
                    maxlength: 5
                },
                longInterval: {
                    required: true,
                    maxlength: 5
                }
            },
            messages: {
                longValidity: {
                    required: trackingNull,
                    maxlength: intervalTimeNull
                },
                longInterval: {
                    required: trackingIntervalNull,
                    maxlength: intervalTimeNull
                }
            }
        }).form();
    },
    goOverspeedSettings: function () {
        if (dataTableOperation.speedLimitValidate()) {
            $("#speedLimit").ajaxSubmit(function (data) {
                $("#goOverspeedSetting").modal("hide");
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess)
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                } else {
                    layer.msg(JSON.parse(data).msg)
                }
            });
        }
    },
    speedLimitValidate: function () {
        return $("#speedLimit").validate({
            rules: {
                masSpeed: {
                    required: true,
                    digits: true,
                    max: 2147483647,
                    min: 1
                },
                speedTime: {
                    required: true,
                    digits: true,
                    max: 2147483647,
                    min: 1
                }
            },
            messages: {
                masSpeed: {
                    required: maxSpeedNull,
                    digits: maxSpeedError,
                    max: maxSpeedErrorScope
                },
                speedTime: {
                    required: speedTimeNull,
                    digits: timeError,
                    max: maxSpeedErrorScope
                }
            }
        }).form();
    },
    emergency: function () {
        if ($("#emergency").is(':checked')) {
            $("#emergency").val(1)
        } else {
            $("#emergency").val(0)
        }
    },
    emergency1: function () {
        if ($("#emergency1").is(':checked')) {
            $("#emergency1").val(1)
        } else {
            $("#emergency1").val(0)
        }
    },
    displayTerminalDisplay: function () {
        if ($("#displayTerminalDisplay").is(':checked')) {
            $("#displayTerminalDisplay").val(3)
        } else {
            $("#displayTerminalDisplay").val(0)
        }
    },
    tts: function () {
        if ($("#tts").is(':checked')) {
            $("#tts").val(4)
        } else {
            $("#tts").val(0)
        }
    },
    tts1: function () {
        if ($("#tts1").is(':checked')) {
            $("#tts1").val(4)
        } else {
            $("#tts1").val(0)
        }
    },
    advertisingDisplay: function () {
        if ($("#advertisingDisplay").is(':checked')) {
            $("#advertisingDisplay").val(5)
        } else {
            $("#advertisingDisplay").val(0)
        }
    },
    advertisingDisplay1: function () {
        if ($("#advertisingDisplay1").is(':checked')) {
            $("#advertisingDisplay1").val(5)
        } else {
            $("#advertisingDisplay1").val(0)
        }
    },
    deleteSign: function () {
        if ($("#deleteSign").is(':checked')) {
            $("#deleteSign").val(1)
        } else {
            $("#deleteSign").val(0)
        }
    },
    // 判断车辆是否在线
    checkVehicleOnlineStatus: function (vid) {
        var url = "/clbs/v/oilmassmgt/oilcalibration/checkVehicleOnlineStatus";
        var data = {"vehicleId": vid};
        var flag = true;
        json_ajax("POST", url, "json", false, data, function (data) {
            if (!data.success) {
                flag = false;
                layer.msg('监控对象离线');
            }
        });
        return flag;
    },
    goTxtSend: function () {
        if (dataTableOperation.txtSendValidate()) {
            $("#txtSend").ajaxSubmit(function (data) {
                $("#textInfoSend").modal("hide");
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess);
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                } else {
                    layer.msg(JSON.parse(data).msg)
                }
            });
        }
    },
    txtSendValidate: function () {
        return $("#txtSend").validate({
            rules: {
                sendTextContent: {
                    required: true,
                    maxlength: 512
                }
            },
            messages: {
                sendTextContent: {
                    required: textNull,
                    maxlength: '最多输入512个字符'
                }
            }
        }).form();
    },
    txtSendForAlarmValidate: function () {
        return $("#txtSendForAlarm").validate({
            rules: {
                txt: {
                    required: true,
                    maxlength: 512
                }
            },
            messages: {
                txt: {
                    required: '下发内容不能为空',
                    maxlength: '最多输入512个字符'
                }
            }
        }).form();
    },
    goTxtSendForAlarm: function () {
        // 为车id赋值
        var vehicleId = $("#vUuid").val();
        $("#vidSendTxtForAlarm").val(vehicleId);
        $("#brandTxt").val($("#warningCarName").text());
        $("#alarmTxt").val($("#warningType").val());
        $("#startTimeTxt").val($('#startTime').val());

        $("#simcardTxt").val($('#simcard').val());
        $("#deviceTxt").val($("#device").val());
        $("#snoTxt").val($("#sno").val());
        $("#handleTypeTxt").val("下发短信");
        $("#deviceTypeTxt").val(deviceTypeTxt);
        $("#description-Txt").val($("#warningDescription").text());
        $("#remark-Txt").val($("#alarmRemark").val());

        if (!dataTableOperation.txtSendForAlarmValidate()) return;
        $("#goTxtSendForAlarm").attr("disabled", "disabled");
        $("#txtSendForAlarm").ajaxSubmit(function (data) {
            $("#goTxtSendForAlarm").removeAttr("disabled");
            $("#warningManage").modal('hide');
            if (JSON.parse(data).success) {
                layer.msg(publicIssuedSuccess);
                dataTableOperation.updateHandleStatus(vehicleId);
                setTimeout("dataTableOperation.logFindCilck()", 500);
            } else {
                layer.msg(publicIssuedFailure);
            }
        });
    },
    goSendQuestion: function () {
        dataTableOperation.sendQuestionValidateTwo();
        if (sendFlag) {
            $("#sendQuestion").ajaxSubmit(function (data) {
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess);

                    //关闭并还原提问下发表单
                    $("#askQuestionsIssued").modal("hide");
                    $("#askQuestionsIssued input[type='text']").val('');
                    $("#askQuestionsIssued .error").hide();
                    $('#answer-add-content div[id^="answer-add"]').remove();

                    setTimeout("dataTableOperation.logFindCilck()", 500);
                } else {
                    layer.msg(JSON.parse(data).msg)
                }
            });
        }
    },
    goInfoService: function () {
        if (dataTableOperation.infoServiceValidate()) {
            $("#infoService").ajaxSubmit(function (data) {
                $("#informationService").modal("hide");
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess)
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                } else {
                    layer.msg(JSON.parse(data).msg)
                }
            });
        }
    },
    goThroughOrder: function () {
        if (dataTableOperation.throughOrderValidate()) {
            $("#throughOrder").ajaxSubmit(function (data) {
                $("#throughInstruction").modal("hide");
                if (JSON.parse(data).obj.type) {
                    layer.msg(publicIssuedSuccess);
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                } else {
                    layer.msg(publicIssuedError);
                }
            });
        }
    },
    throughOrderValidate: function () {
        return $("#throughOrder").validate({
            rules: {
                longData: {
                    required: true
                }
            },
            messages: {
                longData: {
                    required: instructionError
                }
            }
        }).form();
    },
    sendQuestionValidateTwo: function () {
        var inputArr = $("#askQuestionsIssued input[type='text']");
        var inpLen = 0;
        for (var i = 0; i < inputArr.length; i++) {
            var thisInput = inputArr[i];
            var inputVal = thisInput.value;
            if (inputVal === "") {
                $(thisInput).siblings(".error").show();
                inpLen = 1;
            }
            else {
                $(thisInput).siblings(".error").hide();
            }
        }
        sendFlag = inpLen === 0;
    },
    sendQuestionValidate: function () {
        return $("#sendQuestion").validate({
            rules: {
                question: {
                    required: true
                },
                value: {
                    required: true
                }
            },
            messages: {
                question: {
                    required: questionsNull
                },
                value: {
                    required: answerNull
                }
            }
        }).form();
    },
    infoServiceValidate: function () {
        return $("#infoService").validate({
            rules: {
                value: {
                    required: true
                }
            },
            messages: {
                value: {
                    required: textNull
                }
            }
        }).form();
    },
    goTelBack: function () {
        if (dataTableOperation.telBackValidate()) {
            $("#telBack").ajaxSubmit(function (data) {
                $("#reantimeCallBack").modal("hide");
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess)
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                } else {
                    layer.msg(JSON.parse(data).msg)
                }
            });
        }
    },
    telBackValidate: function () {
        return $("#telBack").validate({
            rules: {
                regRet: {
                    isTel: true,
                    required: true
                }
            },
            messages: {
                regRet: {
                    isTel: phoneError,
                    required: phoneNull
                }
            }
        }).form();
    },
    goMultimediaRetrieval: function () {
        if (dataTableOperation.multimediaRetrievalValidate()) {
            var startTime = $("#multimediaRetrieval input[name='startTime']").val();
            var endTime = $("#multimediaRetrieval input[name='endTime']").val();
            startTime = new Date(startTime.replace(/-/, "/"));
            endTime = new Date(endTime.replace(/-/, "/"));
            if (startTime > endTime) {
                layer.msg("开始时间不能大于结束时间");
                return;
            }
            $("#multimediaSearch").modal("hide");
            $("#multimediaRetrieval").ajaxSubmit(function (data) {
                $("#multimediaSearch").modal("hide");
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess)
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                } else {
                    layer.msg(JSON.parse(data).msg)
                }
            });
        }
    },
    multimediaRetrievalValidate: function () {
        return $("#multimediaRetrieval").validate({
            rules: {
                startTime: {
                    required: true
                },
                endTime: {
                    required: true
                }
            },
            messages: {
                startTime: {
                    required: publicInputStartTime
                },
                endTime: {
                    required: publicInputEndTime
                }
            }
        }).form();
    },
    goMultimediaUploads: function () {
        if (dataTableOperation.multimediaUploadsValidate()) {
            $("#multimediaUploads").ajaxSubmit(function (data) {
                $("#multimediaUpload").modal("hide");
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess)
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                } else {
                    layer.msg(JSON.parse(data).msg)
                }
            });
        }
    },
    multimediaUploadsValidate: function () {
        return $("#multimediaUploads").validate({
            rules: {
                startTime: {
                    required: true
                },
                endTime: {
                    required: true
                }
            },
            messages: {
                startTime: {
                    required: publicInputStartTime
                },
                endTime: {
                    required: publicInputEndTime
                }
            }
        }).form();
    },
    //录音上传参数下发
    goRecordUpload: function () {
        if (dataTableOperation.recordUploadValidate()) {
            $("#voiceCommand").val("1");
            $("#recordUpload").ajaxSubmit(function (data) {
                $("#recordingUpload").modal("hide");
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess)
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                } else {
                    layer.msg(JSON.parse(data).msg)
                }
            });
            if ($("#tapingTime").val() === "0") {
                $(".taping-timeline").show();
                $("#voicePlay").attr("class", "pause");
                voiceTimeIndex = 1;
                $("#voicePlay").attr("src", "../../resources/img/pause.png");
                tapingTime = setInterval(function () {
                    $("#voiceTime").html((voiceTimeIndex++) + "秒");
                }, 1000);
            }
        }
    },
    //录音上传停止参数下发
    tapingTimelinePlay: function () {
        if ($("#voicePlay").hasClass("pause")) {
            $("#voicePlay").attr("src", "../../resources/img/play.png");
            voiceTimeIndex = 0;
            clearInterval(tapingTime);
            $("#voiceTime").html(voiceTimeIndex + "秒");
            $("#voiceCommand").val("0");
            //下发
            $("#recordUpload").ajaxSubmit(function (data) {
                if (JSON.parse(data).success) {
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                } else {
                    layer.msg(publicIssuedError);
                }
            });
            //隐藏
            $(".taping-timeline").hide(500);
        }
    },
    recordUploadValidate: function () {
        return $("#recordUpload").validate({
            rules: {
                time: {
                    required: true,
                    max: 65535
                }
            },
            messages: {
                time: {
                    required: recordingNull
                }
            }
        }).form();
    },
    goOriginalOrder: function () {
        if (!dataTableOperation.originalValidate()) return;
        $("#originalOrder").ajaxSubmit(function (data) {
            $("#sendOriginalCommand").modal("hide");
            if (JSON.parse(data).success) {
                layer.msg(publicIssuedSuccess)
                setTimeout("dataTableOperation.logFindCilck()", 500);
            } else {
                layer.msg(JSON.parse(data).msg)
            }
        });
    },
    originalValidate: function () {
        return $("#originalOrder").validate({
            rules: {
                param: {
                    required: true
                },
                data: {
                    required: true
                },
            },
            messages: {
                param: {
                    required: '请输入原始命令类型'
                },
                data: {
                    required: '请输入原始命令内容'
                },
            }
        }).form();
    },
    photoValidate: function () {
        return $("#takePhoto").validate({
            rules: {
                wayID: {
                    required: true
                },
                time: {
                    required: true,
                    digits: true,
                    range: [0, 65535]
                },
                command: {
                    range: [1, 10],
                    required: true
                },
                saveSign: {
                    required: true
                },
                distinguishability: {
                    required: true
                },
                quality: {
                    range: [1, 10],
                    required: true
                },
                luminance: {
                    range: [0, 255],
                    required: true
                },
                contrast: {
                    range: [0, 127],
                    required: true
                },
                saturability: {
                    range: [0, 127],
                    required: true
                },
                chroma: {
                    range: [0, 255],
                    required: true
                },
            },
            messages: {
                wayID: {
                    required: alarmSearchChannelID
                },
                time: {
                    required: alarmSearchIntervalTime,
                    digits: alarmSearchIntervalError,
                    range: alarmSearchIntervalSize
                },
                command: {
                    range: alarmSearchPhotoSize,
                    required: alarmSearchPhotoNull
                },
                saveSign: {
                    required: alarmSearchSaveNull
                },
                distinguishability: {
                    required: alarmSearchResolutionNull
                },
                quality: {
                    range: alarmSearchMovieSize,
                    required: alarmSearchMovieNull
                },
                luminance: {
                    range: alarmSearchBrightnessSize,
                    required: alarmSearchBrightnessNull
                },
                contrast: {
                    range: alarmSearchContrastSize,
                    required: alarmSearchContrastNull
                },
                saturability: {
                    range: alarmSearchSaturatedSize,
                    required: alarmSearchSaturatedNull
                },
                chroma: {
                    range: alarmSearchColorSize,
                    required: alarmSearchColorNull
                }
            }
        }).form();
    },
    videoValidate: function () {
        return $("#getVideo").validate({
            rules: {
                wayID: {
                    required: true
                },
                time: {
                    required: true,
                    digits: true,
                    range: [0, 65535]
                },
                command: {
                    range: [0, 10],
                    required: true
                },
                saveSign: {
                    required: true
                },
                distinguishability: {
                    required: true
                },
                quality: {
                    range: [1, 10],
                    required: true
                },
                luminance: {
                    range: [0, 255],
                    required: true
                },
                contrast: {
                    range: [0, 127],
                    required: true
                },
                saturability: {
                    range: [0, 127],
                    required: true
                },
                chroma: {
                    range: [0, 255],
                    required: true
                },
            },
            messages: {
                wayID: {
                    required: alarmSearchChannelID
                },
                time: {
                    required: alarmSearchIntervalTime,
                    digits: alarmSearchIntervalError,
                    range: alarmSearchIntervalSize
                },
                command: {
                    range: alarmSearchPhotoSize,
                    required: alarmSearchPhotoNull
                },
                saveSign: {
                    required: alarmSearchSaveNull
                },
                distinguishability: {
                    required: alarmSearchResolutionNull
                },
                quality: {
                    range: alarmSearchMovieSize,
                    required: alarmSearchMovieNull
                },
                luminance: {
                    range: alarmSearchBrightnessSize,
                    required: alarmSearchBrightnessNull
                },
                contrast: {
                    range: alarmSearchContrastSize,
                    required: alarmSearchContrastNull
                },
                saturability: {
                    range: alarmSearchSaturatedSize,
                    required: alarmSearchSaturatedNull
                },
                chroma: {
                    range: alarmSearchColorSize,
                    required: alarmSearchColorNull
                }
            }
        }).form();
    },
    photoValidateForAlarm: function () {
        return $("#takePhotoForAlarm").validate({
            rules: {
                wayID: {
                    required: true
                },
                time: {
                    required: true,
                    digits: true,
                    range: [0, 65535]
                },
                command: {
                    range: [0, 10],
                    required: true
                },
                saveSign: {
                    required: true
                },
                distinguishability: {
                    required: true
                },
                quality: {
                    range: [1, 10],
                    required: true
                },
                luminance: {
                    range: [0, 255],
                    required: true
                },
                contrast: {
                    range: [0, 127],
                    required: true
                },
                saturability: {
                    range: [0, 127],
                    required: true
                },
                chroma: {
                    range: [0, 255],
                    required: true
                },
            },
            messages: {
                wayID: {
                    required: alarmSearchChannelID
                },
                time: {
                    required: alarmSearchIntervalTime,
                    digits: alarmSearchIntervalError,
                    range: alarmSearchIntervalSize
                },
                command: {
                    range: alarmSearchPhotoSize,
                    required: alarmSearchPhotoNull
                },
                saveSign: {
                    required: alarmSearchSaveNull
                },
                distinguishability: {
                    required: alarmSearchResolutionNull
                },
                quality: {
                    range: alarmSearchMovieSize,
                    required: alarmSearchMovieNull
                },
                luminance: {
                    range: alarmSearchBrightnessSize,
                    required: alarmSearchBrightnessNull
                },
                contrast: {
                    range: alarmSearchContrastSize,
                    required: alarmSearchContrastNull
                },
                saturability: {
                    range: alarmSearchSaturatedSize,
                    required: alarmSearchSaturatedNull
                },
                chroma: {
                    range: alarmSearchColorSize,
                    required: alarmSearchColorNull
                }
            }
        }).form();
    },
    //录像下发播放器隐藏
    recordingTimelinePlay: function () {
        if ($("#videoPlay").hasClass("pause")) {
            videoPlay.src = "../../resources/img/play.png";
            videoTimeIndex = 0;
            clearInterval(time);
            $("#videoTime").html(videoTimeIndex + "秒");
            //隐藏
            $(".recording-timeline").hide(500);
        }
    },
    //上传数据类型判断
    typeGroup: function (type, data) {
        if (type === 'state') {
            stateName.push(data);
            stateIndex++;
        } else if (type === 'obd') {
            obdName.push(data);
            obdIndex++;
        } else if (type === 'alarm') {
            alarmName.push(data);
            alarmIndex++;
        }
    },
    //所有table重新排序
    tableRank: function (id) {
        var index = 1;
        $("#" + id).children("tbody").children("tr").each(function () {
            $(this).children("td:nth-child(1)").text(index);
            index++;
        });
    },
    //通过table条数计算显示高度
    realTtimeAlarmClick: function () {
        //从报警标识点击切换至报警记录时改变列表状态
        if ($("#scalingBtn").hasClass("fa fa-chevron-up")) {
            $("#scalingBtn").removeAttr("class");
            $("#scalingBtn").addClass("fa fa-chevron-down");
        }
        if (isDragFlag) return;
        //日志记录及报警记录 状态信息
        if (alarmNum === 0) {
            $MapContainer.css({
                "height": newMapHeight + 'px'
            });
        } else if (alarmNum === 1) {
            $MapContainer.css({
                "height": (newMapHeight - 102) + 'px'
            });
        } else if (alarmNum === 2) {
            $MapContainer.css({
                "height": (newMapHeight - (100 + 42)) + 'px'
            });
        } else if (alarmNum === 3) {
            $MapContainer.css({
                "height": (newMapHeight - (100 + 42 * 2)) + 'px'
            });
        } else if (alarmNum === 4) {
            $MapContainer.css({
                "height": (newMapHeight - (100 + 42 * 3)) + 'px'
            });
        } else if (alarmNum >= 5) {
            $MapContainer.css({
                "height": (newMapHeight - 266) + 'px'
            });
        }
        $("#dimensionalMapContainer").css({
            "height": $MapContainer.height() + 'px'
        });
    },
    // 信息列表自适应显示
    carStateAdapt: function (type) {
        if (!($("#scalingBtn").hasClass("fa fa-chevron-up"))) {
            var listLength;
            var id;
            var trHeight;
            if (type === 1) {//状态信息车
                listLength = stateName.length;
            } else if (type === 3) { //报警信息车
                listLength = alarmName.length;
            }
            if (type === 2) {//OBD数据
                listLength = obdName.length;
            }
            if (type === 4) {//日志
                listLength = tableLog.getState().data.length;
            }
            if (type === 5) {//主动安全
                listLength = tableSecurity.getState().data.length;
                trHeight = 41;
            } else {
                trHeight = 41;
            }
            id = "#myTabContent";
            var $div = $(id);
            isCarStateAdapt = true;
            if ($('#TabFenceBox').hasClass('active')) {
                if (isDragFlag) return;
                if (listLength <= 5) {
                    if (listLength === 5) {
                        $div.css({
                            "height": "266px",
                            "overflow": "auto",
                        });
                    } else {
                        $div.css({
                            "height": (trHeight * listLength + 60) + "px",
                            "overflow": "auto",
                        });
                    }
                    if (listLength === 0) {
                        $MapContainer.css({'height': newMapHeight + 'px'});
                        $("#fourMapContainer").css({'height': newMapHeight + 'px'})
                    } else {
                        $MapContainer.css({'height': (newMapHeight - (trHeight * listLength + 60)) + 'px'});
                        $("#fourMapContainer").css({'height': (newMapHeight - (trHeight * listLength + 60)) + 'px'})
                    }
                } else {
                    $div.css({
                        "height": "266px",
                        "overflow": "auto",
                    });
                    $MapContainer.css({'height': (newMapHeight - (trHeight * 5 + 60)) + 'px'});
                    $("#fourMapContainer").css({'height': (newMapHeight - (trHeight * 5 + 60)) + 'px'});
                }
                winHeight = $(window).height();//可视区域高度
                headerHeight = $("#header").height();//头部高度
                tabHeight = $myTab.height();//信息列表table选项卡高度
                tabContHeight = $("#myTabContent").height();//table表头高度
                fenceTreeHeight = winHeight - 193;//围栏树高度
                $("#fenceZtree").css('height', fenceTreeHeight + "px");//电子围栏树高度
                //地图高度
                newMapHeight = winHeight - headerHeight - tabHeight - tabContHeight - 10;
                if (listLength === 0) {
                    newMapHeight = newMapHeight + tabContHeight;
                }
                $MapContainer.css({
                    "height": newMapHeight + 'px'
                });
                $("#dimensionalMapContainer").css({
                    "height": newMapHeight + 'px'
                });
            }
        }
    },
    //确定ID
    confirmID: function (type) {
        var id;
        if (type === 'vehicle' || type === 'people' || type === 'thing' || type === 'state') {
            id = 'realTimeStateTable';
        }
        if (type === 'alarm') {
            id = 'alarmTable';
        }
        if (type === 'obd') {
            id = 'obdInfoTable';
        }
        return id;
    },
    //格式时间得到时间差
    gettimestamp: function (time) {
        if (time.length === 12) {
            time = 20 + time.substring(0, 2) + "-" + time.substring(2, 4) + "-" + time.substring(4, 6) + " " +
                time.substring(6, 8) + ":" + time.substring(8, 10) + ":" + time.substring(10, 12);
        } else if (time.length === 14) {
            time = time.substring(0, 4) + "-" + time.substring(4, 6) + "-" + time.substring(6, 8) + " " +
                time.substring(8, 10) + ":" + time.substring(10, 12) + ":" + time.substring(12, 14);
        }
        var timestamp = Date.parse(new Date(time));
        return timestamp / 1000;
    },
    //保留一位小数
    saveonedecimal: function (str) {
        var str = Number(str).toFixed(1);
        var strlast = str.substr(str.lenght - 1, 1);
        if (strlast === "0" || strlast === 0) {
            str = Math.round(str);
        }
        return str;
    },
    //过滤小数点为0
    fiterNumber: function (data) {
        if (data === null || data === undefined || data === "") {
            return data;
        }
        data = data.toString();
        data = parseFloat(data);
        return data;

    },
    /**
     * 设置终端车牌号
     */
    parametersPlate: function () {
        var brand = $("#brand").val();
        var plateColor = $("#plateColor").val();
        var provinceId = $("#provinceId").val();
        var cityId = $("#cityId").val();
        var provinceIdIsNull = dataTableOperation.isNull(provinceId);
        var cityIdIsNull = dataTableOperation.isNull(cityId);
        if (dataTableOperation.isNull(brand) && provinceIdIsNull && cityIdIsNull && plateColor === -1) {
            layer.msg("请至少下发一项");
            return;
        }
        if (dataTableOperation.isBrand()) {
            $("#setTerminalPlate").ajaxSubmit(function (data) {
                $("#setPlateNumber").modal("hide");
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess)
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                } else {
                    layer.msg(JSON.parse(data).msg);
                }
            });
        }
    },
    isNull: function (param) {
        return param === null || param === undefined || param === '';


    }
    ,
    isBrand: function () {
        return $("#setTerminalPlate").validate({
            rules: {
                brand: {
                    //required: true,
                    minlength: 2,
                    maxlength: 20,
                    isBrandCanNull: true,
                    remote: {
                        type: "post",
                        async: false,
                        url: "/clbs/m/basicinfo/monitoring/vehicle/repetition",
                        dataType: "json",
                        data: {
                            username: function () {
                                return $("#brand").val();
                            }
                        },
                        dataFilter: function (data, type) {
                            var oldV = $("#oldNumber").val();
                            var newV = $("#brand").val();
                            var data2 = data;
                            if (oldV === newV) {
                                return true;
                            }
                            return data2 === "true";
                        }
                    }
                },
                provinceId: {
                    maxlength: 2,
                    minlength: 2,
                    digits: true
                },
                cityId: {
                    maxlength: 4,
                    minlength: 4,
                    digits: true
                }
            },
            messages: {
                brand: {
                    //required: vehicleBrandNull,
                    maxlength: vehicleBrandError,
                    minlength: vehicleBrandError,
                    isBrandCanNull: vehicleBrandError,
                    remote: vehicleBrandExists
                },
                provinceId: {
                    maxlength: "请输入2位数字",
                    minlength: "请输入2位数字",
                    digits: "请输入2位数字"
                },
                cityId: {
                    maxlength: "请输入4位数字",
                    minlength: "请输入4位数字",
                    digits: "请输入4位数字"
                }
            }

        }).form();
    },

    /**
     * 下发OBD
     */
    toSetOBD: function () {
        if (dataTableOperation.OBDValidate()) {
            $("#setOBD").ajaxSubmit(function (data) {
                $("#OBD").modal("hide");
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess)
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                } else {
                    layer.msg(JSON.parse(data).msg);
                }
            });
        }
    },


    OBDValidate: function () {
        return $("#setOBD").validate({
            rules: {
                classification: {
                    required: true
                },
                model: {
                    required: true
                },
                uploadTime: {
                    isRightNumber: true,
                    range: [1, 10]
                }
            },
            messages: {
                classification: {
                    required: '请选择车型分类'
                },
                model: {
                    required: '请选择类型'
                },
                uploadTime: {
                    isRightNumber: '请输入正确的数字',
                    range: '请输入1-10之间的整数'
                }
            }

        }).form();
    },

    // 检测是否有处理报警的权限
    checkAlarmRole: function () {
        var surl = '/clbs/v/monitoring/checkAlarmRole';
        json_ajax("POST", surl, "json", true, {}, function (data) {
            if (data.success) {
                alarmRole = true;
            }
        })
    },
    timeRequest: function () {
        var url = '/clbs/v/monitoring/getRiskSize';
        json_ajax("POST", url, 'json', true, {}, function (data) {
            $("#callPoliceNum").text(data);
        });
    },

    showOrhide: function (data) {
        if (data === 0) {
            var displays = $('#riskTakePicturesContent').css('display');
            $('#riskListeningContent').hide();
            $('.riskListenFooter').hide();
            $('#riskSendTextMessages').hide();
            $('.riskSendTextFooter').hide();
            if (displays === 'none') {
                $('#riskTakePicturesContent').show();
                $('.riskTakePicturesFooter').show();
            } else {
                $('#riskTakePicturesContent').hide();
                $('.riskTakePicturesFooter').hide();
            }
        } else if (data === 1) {
            $('#riskListeningContent').hide();
            $('.riskListenFooter').hide();
            $('#riskTakePicturesContent').hide();
            $('.riskTakePicturesFooter').hide();
            var display = $('#riskSendTextMessages').css('display');
            if (display === 'none') {
                $('#riskSendTextMessages').show();
                $('.riskSendTextFooter').show();
            } else {
                $('#riskSendTextMessages').hide();
                $('.riskSendTextFooter').hide();
            }
        } else {
            $('#riskTakePicturesContent').hide();
            $('.riskTakePicturesFooter').hide();
            $('#riskSendTextMessages').hide();
            $('.riskSendTextFooter').hide();
            var display = $('#riskListeningContent').css('display');
            if (display === 'none') {
                $('#riskListeningContent').show();
                $('.riskListenFooter').show();
            } else {
                $('#riskListeningContent').hide();
                $('.riskListenFooter').hide();
            }
        }
    },

    // 监听下发
    riskListenForAlarm: function () {
        if (dataTableOperation.riskListenValidate()) {
            // 为车id赋值
            var vehicleId = $("#riskvUuid").val();
            $("#riskVidforAlarmListen").val(vehicleId);
            $("#riskBrandListen").val($("#riskWarningCarName").text());
            $("#riskAlarmListen").val($("#riskWarningType").val());
            $("#riskStartTimeListen").val($("#riskWarningTime").text());
            $("#riskId_listening").val(riskId);
            $("#riskSimcardListen").val($('#riskSimcard').val());
            $("#riskDeviceListen").val($("#riskDevice").val());
            $("#riskSnoListen").val($("#riskSno").val());
            $("#riskHandleTypeListen").val("监听");
            $("#riskDescriptionListen").val($("#riskWarningDescription").text());
            $("#riskRemarkListen").val($("#riskAlarmRemark").val());
            $("#riskGoListeningForAlarm").attr("disabled", "disabled");
            $("#riskId").val();
            var isSwitchSignal = $("#warningManage").val();
            $("#riskListeningAlarm").ajaxSubmit(function (data) {
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess);
                    tableSecurity.deleteOptionData([riskId]);
                    $("#riskWarningManage").modal('hide');
                } else {
                    layer.msg(publicIssuedFailure);
                }
                $("#riskWarningManage").modal('hide');
                $("#riskGoListeningForAlarm").removeAttr("disabled");
            });
        }
        $("#riskGoListeningForAlarm").removeAttr("disabled");
    },

    //拍照下发
    riskTakePhotoForAlarm: function () {
        if (dataTableOperation.riskPhotoValidateForAlarm()) {
            $("#riskVidforAlarm").val($("#riskvUuid").val());
            $("#riskAlarmPhoto").val($("#riskWarningType").val());
            $("#riskStartTimePhoto").val($("#riskWarningTime").text());
            $("#riskBrandPhoto").val($("#riskWarningCarName").text());
            $("#riskId_photo").val(riskId);
            $("#riskSimcardPhoto").val($('#riskSimcard').val());
            $("#riskDevicePhoto").val($("#riskDevice").val());
            $("#riskSnoPhoto").val($("#riskSno").val());
            $("#riskHandleTypePhoto").val("拍照");
            $("#riskDescription-photo").val($("#riskWarningDescription").text());
            $("#riskRemark-photo").val($("#riskAlarmRemark").val());
            // var isSwitchSignal = $("#warningManage").val();
            $("#riskGoPhotographsForAlarm").attr("disabled", "disabled");
            $("#riskTakePhotoForAlarm").ajaxSubmit(function (data) {
                if (JSON.parse(data).success) {
                    layer.msg(publicIssuedSuccess);
                    tableSecurity.deleteOptionData([riskId]);
                    $("#riskWarningManage").modal('hide');
                } else {
                    layer.msg(publicIssuedFailure);
                }
            });
        }
        $("#riskGoPhotographsForAlarm").removeAttr("disabled");
    },

    //下发短信
    riskGoTxtSendForAlarm: function () {
        // 为车id赋值
        $("#riskVidSendTxtForAlarm").val($("#riskvUuid").val());
        $("#riskBrandTxt").val($("#riskWarningCarName").text());
        $("#riskAlarmTxt").val($("#riskWarningType").val());
        $("#riskStartTimeTxt").val($("#riskWarningTime").text());

        $("#riskSimcardTxt").val($('#riskSimcard').val());
        $("#riskDeviceTxt").val($("#riskDevice").val());
        $("#riskSnoTxt").val($("#riskSno").val());
        $("#riskDescription").val($("#riskWarningDescription").text());
        $("#riskId_sendText").val(riskId);

        $("#riskDeviceTypeTxt").val(riskDeviceTypeTxt);
        $("#riskRemark-Txt").val($("#alarmRemark").val());
        $("#riskHandleTypeTxt").val("下发短信");
        // var isSwitchSignal = $("#riskWarningManage").val();
        var riskSmsTxt = $("#riskSmsTxt").val();
        if (riskSmsTxt == null || riskSmsTxt.length === 0) {
            dataTableOperation.showErrorMsg("下发内容不能为空", "riskSmsTxt");
            return;
        }
        if (riskSmsTxt.length > 512) {
            layer.msg("下发内容不能超过512个字符");
            return;
        }
        $("#riskGoTxtSendForAlarm").attr("disabled", "disabled");
        $("#riskTxtSendForAlarm").ajaxSubmit(function (data) {
            if (JSON.parse(data).success) {
                layer.msg(publicIssuedSuccess);
                tableSecurity.deleteOptionData([riskId]);
                $("#riskWarningManage").modal('hide')
            } else {
                layer.msg(publicIssuedFailure);
            }
        });
        $("#riskGoTxtSendForAlarm").removeAttr("disabled");
    },

    //监听下发校验
    riskListenValidate: function () {
        return $("#riskListeningAlarm").validate({
            rules: {
                monitorPhone: {
                    isNewTel: true,
                    required: true
                },
            },
            messages: {
                monitorPhone: {
                    required: '请输入电话号码'
                },
            }
        }).form();
    },

    //拍照下发校验
    riskPhotoValidateForAlarm: function () {
        return $("#riskTakePhotoForAlarm").validate({
            rules: {
                wayID: {
                    required: true
                },
                time: {
                    required: true,
                    digits: true,
                    range: [0, 65535]
                },
                command: {
                    range: [0, 10],
                    required: true
                },
                saveSign: {
                    required: true
                },
                distinguishability: {
                    required: true
                },
                quality: {
                    range: [1, 10],
                    required: true
                },
                luminance: {
                    range: [0, 255],
                    required: true
                },
                contrast: {
                    range: [0, 127],
                    required: true
                },
                saturability: {
                    range: [0, 127],
                    required: true
                },
                chroma: {
                    range: [0, 255],
                    required: true
                },
            },
            messages: {
                wayID: {
                    required: alarmSearchChannelID
                },
                time: {
                    required: alarmSearchIntervalTime,
                    digits: alarmSearchIntervalError,
                    range: alarmSearchIntervalSize
                },
                command: {
                    range: alarmSearchPhotoSize,
                    required: alarmSearchPhotoNull
                },
                saveSign: {
                    required: alarmSearchSaveNull
                },
                distinguishability: {
                    required: alarmSearchResolutionNull
                },
                quality: {
                    range: alarmSearchMovieSize,
                    required: alarmSearchMovieNull
                },
                luminance: {
                    range: alarmSearchBrightnessSize,
                    required: alarmSearchBrightnessNull
                },
                contrast: {
                    range: alarmSearchContrastSize,
                    required: alarmSearchContrastNull
                },
                saturability: {
                    range: alarmSearchSaturatedSize,
                    required: alarmSearchSaturatedNull
                },
                chroma: {
                    range: alarmSearchColorSize,
                    required: alarmSearchColorNull
                }
            }
        }).form();
    },

    showErrorMsg: function (msg, inputId) {
        if ($("#riskError_label").is(":hidden")) {
            $("#riskError_label").text(msg);
            $("#riskError_label").insertAfter($("#" + inputId));
            $("#riskError_label").show();
        } else {
            $("#riskError_label").is(":hidden");
        }
    },

    riskHandleAlarm: function (handleType) {
        var startTime = $("#riskWarningTime").text();
        var plateNumber = $("#riskWarningCarName").text();
        var vehicleId = $("#riskvUuid").val();
        var simcard = $("#riskSimcard").val();
        var device = $("#riskDevice").val();
        var sno = $("#riskSno").val();
        var alarm = $("#riskWarningType").val();
        var eventId = $("#riskEventId").val();
        // var isSwitchSignal = $("#warningManage").val();
        var remark = $("#riskAlarmRemark").val();
        var description = $("#riskWarningDescription").text();
        var url = "/clbs/v/monitoring/handleAlarm";

        var data = {
            'id': eventId,
            "vehicleId": vehicleId,
            "plateNumber": plateNumber,
            "alarm": alarm,
            "handleType": handleType,
            "startTime": startTime,
            "description": description,
            "simcard": simcard,
            "device": device,
            "sno": sno,
            "remark": remark,
            "riskId": riskId,
            'isAdas': '1'
        };
        json_ajax("POST", url, "json", true, data, function (data) {
            if (data.success) {
                tableSecurity.deleteOptionData([riskId]);
                // 报警处理完毕后，延迟3秒进行结果查询
                setTimeout(pagesNav.gethistoryno, 3000);
            }
        });
        $("#riskWarningManage").modal('hide');
    },
    enableRowUpdate: function (type) {
        Object.keys(dataTableOperation.rowUpdateState).forEach(function (key) {
            if (key === type) {
                dataTableOperation.rowUpdateState[key] = true;
                return;
            }
            dataTableOperation.rowUpdateState[key] = false;
        })
    },
};

$(function () {
    newTableOperation.initSetting(false, dataTableOperation.updataActiveSafetyMessage);
    // dataTableOperation.updateActiveSafetyHtml()
    $('#closePopover').click(function () {
        dataTableOperation.closePopover();
    })
    $('.nextMedia').click(dataTableOperation.nextMedia);
    $('.prevMedia').click(dataTableOperation.prevMedia);
    $('.media_tab').on('click', '.btn', dataTableOperation.videoChange);


    if (window.navigator.userAgent.toLowerCase().indexOf('ie') >= 0) {
        $('#videoBox').empty().append('<embed  src="" autostart=false id="eventMediaVideo" style="width: 100%;height:350px; "  windowlessVideo="true" wmode="transparent" align="middle" type="application/x-shockwave-flash"></embed>');
    } else {
        $('#videoBox').empty().append('<video width="100%" id="eventMediaVideo" height="370" controls="true" controlslist="nodownload" src=""></video>');
    }

    if (navigator.userAgent.indexOf('MSIE') >= 0) {
        $alarmMsgBox.html('<embed id="IEalarmMsg" src="../../file/music/alarm.wav" autostart="false"/>');
    } else {
        $alarmMsgBox.html('<audio id="alarmMsgAutoOff" src="../../file/music/alarm.wav"></audio>');
    }


    //socket连接
    dataTableOperation.testWebSocket();
   /* var adasSwitch = $('#adasSwitch').val();
    if (adasSwitch === 'true') {
        dataTableOperation.timeRequest();
    }*/

    //监听
    $("#riskWarningManageListening").bind("click", function () {
        dataTableOperation.showOrhide(3)
    });

    //拍照
    $("#riskWarningManagePhoto").bind("click", function () {
        dataTableOperation.showOrhide(0)
    });

    //下发短信
    $("#riskWarningManageSend").bind("click", function () {
        dataTableOperation.showOrhide(1)
    });

    //监听下发
    $("#riskGoListeningForAlarm").bind("click", function () {
        dataTableOperation.riskListenForAlarm();
    });

    //拍照下发
    $("#riskGoPhotographsForAlarm").bind("click", function () {
        dataTableOperation.riskTakePhotoForAlarm()
    });

    //下发短信下发
    $("#riskGoTxtSendForAlarm").bind("click", function () {
        dataTableOperation.riskGoTxtSendForAlarm()
    });

    $("#riskWarningManageAffirm").bind("click", function () {
        dataTableOperation.riskHandleAlarm("人工确认报警")
    });
    $("#riskWarningManageCancel").bind("click", function () {
        dataTableOperation.riskHandleAlarm("不做处理")
    });
    $("#riskWarningManageFuture").bind("click", function () {
        dataTableOperation.riskHandleAlarm("将来处理")
    });

    $("#riskWarningManageClose").click(function () {
        $("#riskWarningManage").modal('hide')
    });
    $("#obdDiagnosticSupportStateClick").click(function (e) {
        e.preventDefault();
        e.stopPropagation();
        console.log('obdDiagnosticSupportStateClick');
    });
});
