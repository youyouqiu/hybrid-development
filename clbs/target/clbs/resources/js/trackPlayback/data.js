var Data = function () {
    this._treeData = null; // 树数据
    this._activeTreeNode = null; // 选中的树节点
    this._eventHandlerList = {}; // 事件执行函数
    // 查询活跃时间，默认本月1号到下月1号
    var nowDate = new Date();
    var fullYear = nowDate.getFullYear();
    var nowMonth = nowDate.getMonth() + 1;
    this._nowMonth = fullYear + "-" + (nowMonth < 10 ? "0" + nowMonth : nowMonth) + "-01"; // 查询开始时间
    this._afterMonth = fullYear + "-" + ((nowMonth + 1) < 10 ? "0" + (nowMonth + 1) : (nowMonth + 1)) + "-01"; // 查询结束时间

    // 轨迹颜色
    this._trackColor; //
    this._trackWidth = 6; // 轨迹宽度

    // 有效信息
    this._validDate = null; // 有效日期数组
    this._validSensorFlag; // 有效日期中是否绑定传感器标识数组
    this._validMile; // 有效日期中的每日里程数组
    this._validClickedIndex; // 点击的有效数据的索引，点击表格时触发

    // 监控对象相关信息
    this._initVid = null; // 从实时监控跳转过来时带来的vid
    this._vid = null; // 监控对象id
    this._pid = null; // 监控对象所属组织或者企业id
    this._sensorType = null; // 传感器设备类型
    this._objType = null; // 人，车，物 类型
    this._group = null; // 所属分组

    // 查询具体轨迹时间
    this._startTime = null;
    this._endTime = null;

    // 轨迹数据 播放控制
    this._positions = null;
    this._positionsTmp = null; // 位置数据临时存储，当用户点击停止行驶数据时存放原始数据，用户点击全部数据后恢复给this._positions
    this._playIndex = 0; // 播放位置索引
    this._speed = 10000; // 播放速度 km/h
    this._sensorFlag = null; // 是否绑定传感器标识
    this._isPlaying = false; // 播放状态，区分是正常播放还是用户手动控制位置
    this._isDraging = false; // 拖拽状态，区分是否在拖拽
    this._allOrRunData = 'all'; // null 'all' 'run' 切换全部数据或者行驶数据
    this._showAlarmPoint = false; // 显示报警点
    this._showStopPoint = false; // 显示停止点
    this._showShowLocation = false; // 显示基站定位
    this._clickShowLocation = false; // 是否点击基站定位触发的刷新页面逻辑（positionsChange）

    // 图表数据
    this._isChartOpen = false; // 图表区域是否显示
    this._paraScale = null; // 图表和原始数据的playIndex比例尺
    this._sensorList = null; // 传感器列表
    this._activeSensorKeys = null; // 当前选中的传感器 key 和 图表实例
    this._mileSpeed = null; // 里程速度数据
    this._stopData = null; // 停止/行驶数据
    this._stopTypeDict = null; // 停驶数据的类型字典，包括各出现了几次，时长多少，起始点
    this._stopDataTmp = null;  // 停驶数据临时存储，当用户点击停止行驶数据时存放原始数据，用户点击全部数据后恢复给this._stopData
    this._oilConsumptionData = null; // 油耗
    this._oilConsumptionSensorNo = null; // 油耗传感器索引 1,2
    this._oilData; // 油量
    this._oilSensorNo = null; // 油耗传感器索引 1,2
    this._temperaturey; // 温度
    this._temperatureySensorNo = null; // 温度传感器索引 1,2
    this._humidity; // 湿度
    this._humiditySensorNo = null; // 湿度传感器索引 1,2
    this._workHour; // 工时
    this._workHourSensorNo = null; // 工时传感器索引 1,2
    this._reverse = null; // 正反转数据
    this._ioData = null; // 开关数据
    this._ioDataSensorNo = null; //开关传感器索引
    this._weight = null; // 载重
    this._weightSensorNo = null; // 载重传感器所以 1,2
    this._tire = null; // 胎压
    this._tireNumList = null; // 轮胎编号列表
    this._tireNum = null; // 当前轮胎编号

    // 表格数据
    this._tableTabIndex = 1; // 表格标签页活跃索引
    this._obdData = null; // OBD 数据
    this._odbDataTmp = null; // odb数据临时存储，当用户点击停止行驶数据时存放原始数据，用户点击全部数据后恢复给this._odbData
    this._runFragmentData = null; // 行驶段数据
    this._stopFragmentData = null; // 停止段数据
    this._alarmData = null; // 报警数据

    // 定时定区域查询
    this._areaTreeCheckVid = true;// 组织树勾选的监控对象
    this._areaTreeAllCheck = true;// 控制定时定区域查询组织树是否可勾选全部监控对象
    this._rangeAreaPos = [];// 绘制的区域经纬度集合
    this._areaIndex = true;// 当前区域索引

    // 围栏
    this._fenceIdList = new mapVehicle();// 围栏Id集合
    this._administrativeRegionsList = new mapVehicle();// 行政区域集合
    this._travelLineList = new mapVehicle();// 导航路线集合
};

Data.prototype.setActiveSensorKeys = function (activeSensorKeys) {
    this._activeSensorKeys = activeSensorKeys;
    this.runEventHandler('activeSensorKeysChange');
}

Data.prototype.getActiveSensorKeys = function () {
    return this._activeSensorKeys;
}

Data.prototype.setSensorList = function (sensorList) {
    this._sensorList = sensorList;
    this.runEventHandler('sensorListChange');
}

Data.prototype.getSensorList = function () {
    return this._sensorList;
}

Data.prototype.getParaScale = function () {
    return this._paraScale;
}

Data.prototype.setIsChartOpen = function (isChartOpen) {
    this._isChartOpen = isChartOpen;
    this.runEventHandler('isChartOpenChange');
}

Data.prototype.getIsChartOpen = function () {
    return this._isChartOpen;
}


Data.prototype.setAlarmData = function (alarmData) {
    this._alarmData = alarmData;
    this.runEventHandler('alarmDataChange');
}

Data.prototype.getAlarmData = function () {
    return this._alarmData;
}

Data.prototype.setStopFragmentData = function (stopFragmentData) {
    this._stopFragmentData = stopFragmentData;
    this.runEventHandler('stopFragmentDataChange');
}

Data.prototype.getStopFragmentData = function () {
    return this._stopFragmentData;
}

Data.prototype.setRunFragmentData = function (runFragmentData) {
    this._runFragmentData = runFragmentData;
    this.runEventHandler('runFragmentDataChange');
}

Data.prototype.getRunFragmentData = function () {
    return this._runFragmentData;
}

Data.prototype.setObdData = function (obdData) {
    this._obdData = obdData;
    this.runEventHandler('obdDataChange');
}

Data.prototype.getObdData = function () {
    return this._obdData;
}

Data.prototype.setOdbDataTmp = function (odbDataTmp) {
    this._odbDataTmp = odbDataTmp;
    this.runEventHandler('odbDataTmpChange');
}

Data.prototype.getOdbDataTmp = function () {
    return this._odbDataTmp;
}

Data.prototype.setMileSpeed = function (mileSpeed) {
    this.supplyMissingPoint(mileSpeed, function (_, time) {
        return {speed: null, mileage: null, time: time, supply: true}
    });
    this._mileSpeed = mileSpeed;
    this.runEventHandler('mileSpeedChange');
}

Data.prototype.getMileSpeed = function () {
    return this._mileSpeed;
}

Data.prototype.setStopData = function (stopData) {
    this.supplyMissingPoint(stopData, function (_, time) {
        return {status: null, time: time, supply: true}
    });
    this._stopData = stopData;
    this.runEventHandler('stopDataChange');
}

Data.prototype.getStopData = function () {
    return this._stopData;
}

Data.prototype.setStopDataTmp = function (stopDataTmp) {
    this._stopDataTmp = stopDataTmp;
}

Data.prototype.getStopDataTmp = function () {
    return this._stopDataTmp;
}

Data.prototype.setStopTypeDict = function (stopTypeDict) {
    this._stopTypeDict = stopTypeDict;
}

Data.prototype.getStopTypeDict = function () {
    return this._stopTypeDict;
}

Data.prototype.setOilData = function (oilData) {
    this.supplyMissingPoint(oilData, function (prevItem, time) {
        return {
            oilTank: prevItem.oilTank.map(function () {
                return null
            }),
            fuelAmount: prevItem.fuelAmount.map(function () {
                return null
            }),
            fuelSpill: prevItem.fuelSpill.map(function () {
                return null
            }),
            time: time,
            supply: true
        };
    });
    this._oilData = oilData;
    this.runEventHandler('oilDataChange');
}

Data.prototype.getOilData = function () {
    return this._oilData;
}

Data.prototype.setOilSensorNo = function (oilSensorNo) {
    this._oilSensorNo = oilSensorNo;
    this.runEventHandler('oilSensorNoChange');
}

Data.prototype.getOilSensorNo = function () {
    return this._oilSensorNo;
}

Data.prototype.setOilConsumptionData = function (oilConsumptionData) {
    this.supplyMissingPoint(oilConsumptionData, function (_, time) {
        return {oilWear: null, time: time, supply: true}
    });
    this._oilConsumptionData = oilConsumptionData;
    this.runEventHandler('oilConsumptionDataChange');
}

Data.prototype.getOilConsumptionData = function () {
    return this._oilConsumptionData;
}

Data.prototype.setOilConsumptionSensorNo = function (oilConsumptionSensorNo) {
    this._oilConsumptionSensorNo = oilConsumptionSensorNo;
    this.runEventHandler('oilConsumptionSensorNoChange');
}

Data.prototype.getOilConsumptionSensorNo = function () {
    return this._oilConsumptionSensorNo;
}

Data.prototype.setTemperaturey = function (temperaturey) {
    this.supplyMissingPoint(temperaturey.sensorDataList, function (_, time) {
        return {temperature: null, time: time, supply: true}
    });
    this._temperaturey = temperaturey;
    this.runEventHandler('temperatureyChange');
}

Data.prototype.getTemperaturey = function () {
    return this._temperaturey;
}

Data.prototype.setTemperatureySensorNo = function (temperatureySensorNo) {
    this._temperatureySensorNo = temperatureySensorNo;
    this.runEventHandler('temperatureySensorNoChange');
}

Data.prototype.getTemperatureySensorNo = function () {
    return this._temperatureySensorNo;
}

Data.prototype.setHumidity = function (humidity) {
    this.supplyMissingPoint(humidity.sensorDataList, function (_, time) {
        return {humidity: null, time: time, supply: true}
    });
    this._humidity = humidity;
    this.runEventHandler('humidityChange');
}

Data.prototype.getHumidity = function () {
    return this._humidity;
}

Data.prototype.setHumiditySensorNo = function (humiditySensorNo) {
    this._humiditySensorNo = humiditySensorNo;
    this.runEventHandler('humiditySensorNoChange');
}

Data.prototype.getHumiditySensorNo = function () {
    return this._humiditySensorNo;
}

Data.prototype.setWorkHour = function (workHour) {
    this.supplyMissingPoint(workHour.workHourInfo, function (_, time) {
        return {checkData: null, type: null, workingPosition: null, time: time, supply: true}
    });
    this._workHour = workHour;
    this.runEventHandler('workHourChange');
}

Data.prototype.getWorkHour = function () {
    return this._workHour;
}

Data.prototype.setWorkHourSensorNo = function (workHourSensorNo) {
    this._workHourSensorNo = workHourSensorNo;
    this.runEventHandler('workHourSensorNoChange');
}

Data.prototype.getWorkHourSensorNo = function () {
    return this._workHourSensorNo;
}

Data.prototype.setReverse = function (reverse) {
    this.supplyMissingPoint(reverse, function (_, time) {
        return {
            orientation: null,
            rotationSpeed: null,
            rotationStatus: null,
            workTime: null,
            pulseCount: null,
            rotationTime: null,
            time: time,
            supply: true
        }
    });
    this._reverse = reverse;
    this.runEventHandler('reverseChange');
}

Data.prototype.getReverse = function () {
    return this._reverse;
}

Data.prototype.setIoData = function (ioData) {
    this.supplyMissingPoint(ioData.data, function (_, time) {
        return {
            statuses: [null, null, null, null],
            time: time,
            supply: true
        };
    });
    this._ioData = ioData;
}

Data.prototype.getIoData = function () {
    return this._ioData;
}

Data.prototype.setIoDataSensorNo = function (ioDataSensorNo) {
    this._ioDataSensorNo = ioDataSensorNo;
    this.runEventHandler('ioDataSensorNoChange');
}

Data.prototype.getIoDataSensorNo = function () {
    return this._ioDataSensorNo;
}

Data.prototype.setWeight = function (weight) {
    this.supplyMissingPoint(weight.sensorDataList, function (_, time) {
        return {
            weight: null,
            time: time,
            supply: true
        };
    });
    this._weight = weight;
    this.runEventHandler('weightChange');
}

Data.prototype.getWeight = function () {
    return this._weight;
}

Data.prototype.setWeightSensorNo = function (weightSensorNo) {
    this._weightSensorNo = weightSensorNo;
    this.runEventHandler('weightSensorNoChange');
}

Data.prototype.getWeightSensorNo = function () {
    return this._weightSensorNo;
}

Data.prototype.setTire = function (tire) {
    this.supplyMissingPoint(tire.sensorDataList, function (_, time) {
        return {
            pressure: null,
            time: time,
            supply: true
        };
    });
    this._tire = tire;
    this.runEventHandler('tireChange');
}

Data.prototype.getTire = function () {
    return this._tire;
}

Data.prototype.setTireNumList = function (tireNumList) {
    this._tireNumList = tireNumList;
}

Data.prototype.getTireNumList = function () {
    return this._tireNumList;
}

Data.prototype.setTireNum = function (tireNum) {
    this._tireNum = tireNum;
    this.runEventHandler('tireNumChange');
}

Data.prototype.getTireNum = function () {
    return this._tireNum;
}

Data.prototype.setTableTabIndex = function (tableTabIndex) {
    var different = this._tableTabIndex !== tableTabIndex;
    this._tableTabIndex = tableTabIndex;
    if (different) {
        this.runEventHandler('tableTabIndexChange');
    }
}

Data.prototype.getTableTabIndex = function () {
    return this._tableTabIndex;
}

Data.prototype.setIsPlaying = function (isPlaying, target) {
    var different = this._isPlaying !== isPlaying;
    // 从播放状态到停止状态
    var play2stop = this._isPlaying === true && isPlaying === false;
    var changeDirection = play2stop ? 'play2Stop' : 'stop2Play';

    this._isPlaying = isPlaying;
    if (different) {
        this.runEventHandler('isPlayingChange', changeDirection, target);
    }
}

Data.prototype.getIsPlaying = function () {
    return this._isPlaying;
}

Data.prototype.setIsDraging = function (isDraging) {
    this._isDraging = isDraging;
}

Data.prototype.getIsDraging = function () {
    return this._isDraging;
}

Data.prototype.setAllOrRunData = function (allOrRunData) {
    var different = this._allOrRunData !== allOrRunData;
    this._allOrRunData = allOrRunData;
    if (different) {
        this.runEventHandler('allOrRunDataChange');
    }
}

Data.prototype.getAllOrRunData = function () {
    return this._allOrRunData;
}

Data.prototype.setShowAlarmPoint = function (showAlarmPoint) {
    this._showAlarmPoint = showAlarmPoint;
    this.runEventHandler('showAlarmPointChange');
}

Data.prototype.getShowAlarmPoint = function () {
    return this._showAlarmPoint;
}

Data.prototype.setShowStopPoint = function (showStopPoint) {
    this._showStopPoint = showStopPoint;
    this.runEventHandler('showStopPointChange');
}

Data.prototype.getShowStopPoint = function () {
    return this._showStopPoint;
}

Data.prototype.setShowLocation = function (showLocation) {
    this._showShowLocation = showLocation;
    this.runEventHandler('showLocationChange');
}

Data.prototype.getShowLocation = function () {
    return this._showShowLocation;
}

Data.prototype.setClickShowLocation = function (clickShowLocation) {
    this._clickShowLocation = clickShowLocation;
}

Data.prototype.getClickShowLocation = function () {
    return this._clickShowLocation;
}

Data.prototype.setSpeed = function (speed) {
    this._speed = speed;
    this.runEventHandler('speedChange');
}

Data.prototype.getSpeed = function () {
    return this._speed;
}

Data.prototype.setSensorFlag = function (sensorFlag) {
    this._sensorFlag = sensorFlag;
}

Data.prototype.getSensorFlag = function () {
    return this._sensorFlag;
}

/**
 * 设置播放索引
 * @param playIndex
 * @param who 谁在设置，如果是图表，使用比例尺转换为原始索引
 */
Data.prototype.setPlayIndex = function (playIndex, who) {
    if (who === 'chart') {
        playIndex = this.getParaScale()(playIndex, 'toOrigin');
    }
    this._playIndex = playIndex;
    this.runEventHandler('playIndexChange');
}

/**
 * 获取播放索引
 * @param who 谁在获取，如果是图表，使用比例尺转换为图表索引
 * @returns {number|*}
 */
Data.prototype.getPlayIndex = function (who) {
    if (who === 'chart') {
        return this.getParaScale()(this._playIndex, 'toChart');
    }
    return this._playIndex;
}

Data.prototype.setPositions = function (positions) {
    this._positions = positions;
    this._paraScale = positions === null ? null : this.buildParaScale(positions);
    this.runEventHandler('positionsChange');
}

Data.prototype.getPositions = function () {
    return this._positions;
}


Data.prototype.setPositionsTmp = function (positionsTmp) {
    this._positionsTmp = positionsTmp;
}

Data.prototype.getPositionsTmp = function () {
    return this._positionsTmp;
}

Data.prototype.setTrackColor = function (trackColor) {
    this._trackColor = trackColor;
    this.runEventHandler('trackColorChange');
}

Data.prototype.getTrackColor = function () {
    return this._trackColor;
}

Data.prototype.setTrackWidth = function (trackWidth) {
    this._trackWidth = trackWidth;
    this.runEventHandler('trackWidthChange');
}

Data.prototype.getTrackWidth = function () {
    return this._trackWidth;
}

Data.prototype.setValidDate = function (activeDate) {
    this._validDate = activeDate;
}

Data.prototype.getValidDate = function () {
    return this._validDate;
}

Data.prototype.setValidSensorFlag = function (activeSensorFlag) {
    this._validSensorFlag = activeSensorFlag;
}

Data.prototype.getValidSensorFlag = function () {
    return this._validSensorFlag;
}

Data.prototype.setValidMile = function (activeMile) {
    this._validMile = activeMile;
}

Data.prototype.getValidMile = function () {
    return this._validMile;
}

Data.prototype.setValidClickedIndex = function (activeClickedIndex) {
    this._validClickedIndex = activeClickedIndex;
}

Data.prototype.getValidClickedIndex = function () {
    return this._validClickedIndex;
}

Data.prototype.setTreeData = function (treeData) {
    this._treeData = treeData;
}

Data.prototype.getTreeData = function () {
    return this._treeData;
}

Data.prototype.setActiveTreeNode = function (treeNode) {
    this._activeTreeNode = treeNode;
    this._sensorType = treeNode.deviceType;
    this._objType = treeNode.type;
    this._vid = treeNode.id;
    this._pid = treeNode.pId;
    this.runEventHandler('activeTreeNodeChange');
}

Data.prototype.getActiveTreeNode = function () {
    return this._activeTreeNode;
}

Data.prototype.getSensorType = function () {
    return this._sensorType;
}

// Data.prototype.setObjType = function(objType){
//     this._objType;
// }

Data.prototype.getObjType = function () {
    return this._objType;
}

Data.prototype.setGroup = function (group) {
    this._group = group;
}

Data.prototype.getGroup = function () {
    return this._group;
}

Data.prototype.setNowMonth = function (nowMonth) {
    this._nowMonth = nowMonth;
}

Data.prototype.getNowMonth = function () {
    return this._nowMonth;
}

Data.prototype.setAfterMonth = function (afterMonth) {
    this._afterMonth = afterMonth;
}

Data.prototype.getAfterMonth = function () {
    return this._afterMonth;
}

Data.prototype.setVid = function (vid) {
    this._vid = vid;
}

Data.prototype.getVid = function () {
    return this._vid;
}

Data.prototype.setPid = function (pid) {
    this._pid = pid;
}

Data.prototype.getPid = function () {
    return this._pid;
}

Data.prototype.setInitVid = function (initVid) {
    this._initVid = initVid;
}

Data.prototype.getInitVid = function () {
    return this._initVid;
}

Data.prototype.setStartEndTime = function (startTime, endTime) {
    if (startTime !== undefined && startTime !== null) {
        this._startTime = startTime;
    }
    if (endTime !== undefined && endTime !== null) {
        this._endTime = endTime;
    }
    this.runEventHandler('startEndTimeChange');
}

Data.prototype.getStartTime = function () {
    return this._startTime;
}

Data.prototype.getEndTime = function () {
    return this._endTime;
}

Data.prototype.setAreaTreeCheckVid = function (areaTreeCheckVid) {
    this._areaTreeCheckVid = areaTreeCheckVid;
}

Data.prototype.getAreaTreeCheckVid = function () {
    return this._areaTreeCheckVid;
}
Data.prototype.setAreaTreeAllCheck = function (areaTreeAllCheck) {
    this._areaTreeAllCheck = areaTreeAllCheck;
}

Data.prototype.getAreaTreeAllCheck = function () {
    return this._areaTreeAllCheck;
}
Data.prototype.setRangeAreaPos = function (rangeAreaPos) {
    this._rangeAreaPos = rangeAreaPos;
}

Data.prototype.getRangeAreaPos = function () {
    return this._rangeAreaPos;
}

Data.prototype.setAreaIndex = function (areaIndex) {
    this._areaIndex = areaIndex;
}

Data.prototype.getAreaIndex = function () {
    return this._areaIndex;
}

Data.prototype.setFenceIdList = function (fenceIdList) {
    this._fenceIdList = fenceIdList;
}

Data.prototype.getFenceIdList = function () {
    return this._fenceIdList;
}
Data.prototype.setAdministrativeRegionsList = function (administrativeRegionsList) {
    this._administrativeRegionsList = administrativeRegionsList;
}

Data.prototype.getAdministrativeRegionsList = function () {
    return this._administrativeRegionsList;
}
Data.prototype.setTravelLineList = function (travelLineList) {
    this._travelLineList = travelLineList;
}

Data.prototype.getTravelLineList = function () {
    return this._travelLineList;
}


// <editor-fold desc="事件 添加，移除，执行">
Data.prototype.on = function (eventName, eventHandler) {
    if (this._eventHandlerList[eventName] === undefined) {
        this._eventHandlerList[eventName] = [eventHandler];
    } else {
        this._eventHandlerList[eventName].push(eventHandler);
    }
}

Data.prototype.off = function (eventName, eventHandler) {
    var eventList = this._eventHandlerList[eventName];
    if (eventList === undefined || eventList.length === undefined || eventList.length === 0) {
        return false;
    }
    for (var i = 0; i < eventList.length; i++) {
        if (eventList[i] === eventHandler) {
            // 删除该 event handler
            eventList.splice(i, 1);
        }
    }
}

Data.prototype.runEventHandler = function (eventName) {
    var eventList = this._eventHandlerList[eventName];
    if (eventList === undefined || eventList.length === undefined || eventList.length === 0) {
        return false;
    }
    for (var i = 0; i < eventList.length; i++) {
        var eventHandler = eventList[i];
        if (typeof eventHandler === 'function') {
            var result = eventHandler.apply(null, Array.prototype.slice.call(arguments, 1));
            // 如果event handler 返回false，阻止剩余的handler的执行
            if (result === false) {
                break;
            }
        }
    }
}
// </editor-fold>


// <editor-fold desc="补点">

/**
 * 补点，如果数组的两个点时间相差大于五分钟，则按30秒一个点来补
 * @param {Array} array 需要补点的数组
 * @param {Function} cloneFunc 提供补点转换函数，接受上一个点和时间
 */
Data.prototype.supplyMissingPoint = function (array, cloneFunc) {
    if (!array) {
        return;
    }
    var len = array.length;
    for (var i = 0; i < len;) {
        var prevItem = array[i - 1];
        var item = array[i];
        if (!prevItem) {
            i += 1;
            continue;
        }
        var difference = item.time - prevItem.time; // 都是以秒为单位
        if (difference <= 300 || difference > 3600 * 24 * 3) { // 超过三天了也不补了，多半是数据有问题
            i += 1;
            continue;
        }

        var second30Times = Math.floor(difference / 30); // 这些秒中包含多少个30秒
        // 如果刚好是30秒间隔整数个，那么最后一个实际上不需要不点，比如10秒到70秒，差了60秒，但只需要补一个点
        if (difference / 30 === second30Times && second30Times > 0) {
            second30Times -= 1;
        }
        for (var j = 0; j < second30Times; j += 1) {
            array.splice(i + j, 0, cloneFunc(prevItem, prevItem.time + (30 * (j + 1))));
        }
        len = array.length;
        i += second30Times + 1;
    }
};

/**
 * 补点，如果数组的两个点时间相差大于五分钟，则按30秒一个点来补
 * 单独写一个函数的原因是位置信息的时间字段不是time，而是vtime，同时省去函数调用提高性能
 * @param {Array} array 需要补点的数组
 */
Data.prototype.supplyLocation = function (array) {
    if (!array) {
        return;
    }
    var len = array.length;
    var toChartIndexObj = {};
    var toOriginIndexObj = {};
    var originIndex = 0;
    var chartIndex = 0;
    for (var i = 0; i < len;) {
        var prevItem = array[i - 1];
        var item = array[i];
        if (!prevItem) {
            // 这四行代码是一个整体操作，将图表和原始索引都向前加1
            toChartIndexObj[originIndex] = chartIndex;
            toOriginIndexObj[chartIndex] = originIndex;
            chartIndex += 1;
            originIndex += 1;
            i += 1;
            continue;
        }
        var difference = item.vtime - prevItem.vtime; // 都是以秒为单位
        if (difference <= 300 || difference > 3600 * 24 * 3) { // 超过三天了也不补了，多半是数据有问题
            toChartIndexObj[originIndex] = chartIndex;
            toOriginIndexObj[chartIndex] = originIndex;
            chartIndex += 1;
            originIndex += 1;
            i += 1;
            continue;
        }

        var second30Times = Math.floor(difference / 30); // 这些秒中包含多少个30秒
        // 如果刚好是30秒间隔整数个，那么最后一个实际上不需要不点，比如10秒到70秒，差了60秒，但只需要补一个点
        if (difference / 30 === second30Times && second30Times > 0) {
            second30Times -= 1;
        }

        for (var j = 0; j < second30Times; j += 1) {
            array.splice(i + j, 0, {
                vtime: prevItem.vtime + (30 * (j + 1))
            });
            chartIndex += 1;
            toOriginIndexObj[chartIndex] = originIndex;
        }

        toChartIndexObj[originIndex] = chartIndex;
        toOriginIndexObj[chartIndex] = originIndex;
        chartIndex += 1;
        originIndex += 1;

        len = array.length;
        i += second30Times + 1;
    }
    return [toChartIndexObj, toOriginIndexObj];
};

/**
 * 构建图表到原始数据的playIndex(播放索引)的比例尺(对应关系)
 * 通过分段比例尺的思想实现，首先取原始数据和填充数据，然后通过传入的index和方向确定vtime
 * 根据vtime确定对应结果索引
 * @param array
 * @returns {Function}
 */
Data.prototype.buildParaScale = function (array) {
    var origin = [];
    var supplied = [];
    for (var i = 0; i < array.length; i++) {
        origin.push(array[i]);
        supplied.push(array[i]);
    }
    // 优化比例尺性能，先将数据计算好，后续直接通过hash取
    var toChartToOrigin = this.supplyLocation(supplied);

    // direction : toChart 映射到图表上的playIndex; toOrigin 映射到原始数据的playIndex
    return function (index, direction) {
        if (direction === 'toChart') {
            // var originItem = origin[index];
            // var suppliedIndex = null;
            // for (var j = 0; j < supplied.length; j++){
            //     if (supplied[j].vtime === originItem.vtime){
            //         suppliedIndex = j;
            //         break;
            //     }
            // }
            // return suppliedIndex;
            return toChartToOrigin[0][index];
        } else {
            // var suppliedItem = supplied[index];
            // var originIndex = null;
            // for (var k = 0; k < origin.length; k++){
            //     if (suppliedItem.vtime >= origin[k].vtime
            //         && (suppliedItem.vtime <= origin[k + 1].vtime || k === origin.length - 1)){
            //         originIndex = k;
            //         break;
            //     }
            // }
            // return originIndex;
            return toChartToOrigin[1][index];
        }
    }
}

// </editor-fold>