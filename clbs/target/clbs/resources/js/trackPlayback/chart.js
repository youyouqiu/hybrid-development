var TrackPlaybackUtil = {};

TrackPlaybackUtil.getLocale = function (key) {
    var obj = {
        runAndStop: '行驶/停止',
        driveTime: '时长',
        stopTimeLength: '停止时长',
        mileage: '里程',
        speed: '速度',
        oilDataText: '油量',
        leftOil: '剩油',
        addOil: '加油',
        reduceOil: '漏油',
        oilConsumption: '油耗',
        tempText: '温度(℃)',
        humidityText: '湿度(%)',
        voltage: '电压',
        secondFlow: '瞬时流量',
        reverseText: '正反转',
        noData: '无数据',
        upRotate: '正转',
        downRotate: '反转',
        stopRorate: '停转',
        io: '开关',
        hour: 'h'
    }
    return obj[key];
}

/**
 * 秒折合多少小时，包含一位小数
 * @param {Number} second 秒
 */
TrackPlaybackUtil.getHourFromSecond = function (second) {
    var hourSecond = 60 * 60;
    var hour = second / hourSecond;
    hour = TrackPlaybackUtil.toFixed(hour, 1, true);
    return hour;
}

/**
 * 秒的人性化显示，大于等于十分钟显示 多少h
 * 小于十分钟显示几分几秒
 * @param {String} 完整的时间文本
 */
TrackPlaybackUtil.getHTMLFromSecond = function (second) {
    if (second >= 10 * 60) {
        return '<span class="color-blue">' + TrackPlaybackUtil.getHourFromSecond(second).toString() + '</span> h';
    }
    var minute = Math.floor(second / 60);
    var remainSecond = second - (minute * 60);
    return '<span class="color-blue">' + minute.toString() + '</span> m ' +
        '<span class="color-blue">' + remainSecond.toString() + '</span> s';
}

/**
 * 秒的人性化显示，大于等于十分钟显示 多少h
 * 小于十分钟显示几分几秒
 * @param {String} 完整的时间文本
 */
TrackPlaybackUtil.getTextFromSecond = function (second) {
    if (second >= 10 * 60) {
        return TrackPlaybackUtil.getHourFromSecond(second).toString() + ' h';
    }
    var minute = Math.floor(second / 60);
    var remainSecond = second - (minute * 60);
    return minute.toString() + ' m ' + remainSecond.toString() + ' s';
}

/**
 * 保留指定小数位
 * @param {*} source 要转换的对象
 * @param {Number} digit 保留的小数位
 * @param {Boolean}} omitZero 是否省略最末尾的0
 */
TrackPlaybackUtil.toFixed = function (source, digit, omitZero) {
    var sourceIn = source;
    if (typeof sourceIn !== 'number') {
        try {
            sourceIn = parseFloat(sourceIn);
        } catch (error) {
            return 0;
        }
    }
    if (sourceIn === null || sourceIn === undefined || isNaN(sourceIn)) {
        return 0;
    }
    var afterFixed = sourceIn.toFixed(digit); // 此时 afterFixed 为string类型
    if (omitZero) {
        afterFixed = parseFloat(afterFixed);
    }
    return afterFixed;
}

/**
 * 判断对象是否为空
 * @param {Object} obj 需要判断的对象，可以是日期，对象，数组，Immutalbe类型的
 */
TrackPlaybackUtil.isEmpty = function (obj) {
    if (obj instanceof Date) {
        return false;
    }
    if (obj === undefined || obj === null) {
        return true;
    }
    if (typeof obj === 'object') {
        if (obj.size !== undefined) {
            return obj.size === 0;
        }
        return Object.keys(obj).length === 0 || obj.length === 0;
    }
    return false;
}

/**
 * @param {*} source 要转换的对象
 * @param {Number} defaultValue  转换不成功时的默认返回值
 */
TrackPlaybackUtil.tryParseFloat = function (source, defaultValue) {
    if (defaultValue === undefined) {
        defaultValue = 0;
    }
    var r = parseFloat(source);
    if (!isNaN(r)) {
        return r;
    }
    return defaultValue;
}

var Chart = function (options, dependency) {
    this.dependency = dependency;

}

Chart.prototype.sensorMeta = {
    mileSpeed: {
        icon: 'icon-mile',
        text: '里程速度',
        ids: ['0x53'],
    },
    stopData: {
        icon: 'icon-runAndStop',
        text: '行驶/停止',
        ids: ['0x00'],
    },
    oilData: {
        icon: 'icon-oil',
        text: '油量数据',
        ids: ['0x41', '0x42', '0x43', '0x44'],
    },
    oilConsumptionData: {
        icon: 'icon-oilConsume',
        text: '油耗数据',
        ids: ['0x45', '0x46'],
    },
    temperaturey: {
        icon: 'icon-temperature',
        text: '温度数据',
        ids: ['0x21', '0x22', '0x23', '0x24', '0x25'],
    },
    humidity: {
        icon: 'icon-humiture',
        text: '湿度数据',
        ids: ['0x26', '0x27', '0x28', '0x29', '0x2A'],
    },
    workHour: {
        icon: 'icon-workhour',
        text: '工时数据',
        ids: ['0x80', '0x81'],
    },
    reverse: {
        icon: 'icon-reverse',
        text: '正反转',
        ids: ['0x51'],
    },
    weight: {
        icon: 'icon-weight',
        text: '载重数据',
        ids: ['0x70', '0x71'],
    },
    tire: {
        icon: 'icon-tire',
        text: '胎压数据',
        ids: ['0xE3'],
    },
    ioData: {
        icon: 'icon-io',
        text: '开关数据',
        ids: ['0x90', '0x91', '0x92'],
    }
}

Chart.prototype.chartTmpl = {
    mileSpeed: '<div class="chart-item" id="mileSpeedItem">\n' +
    '                                <div class="chart-title">里程速度</div>\n' +
    '                                <div class="chart-line">\n' +
    '                                    <div id="mileSpeedChart" class="chart-wraper"></div>\n' +
    '                                </div>\n' +
    '                                <table class="chart-text">\n' +
    '                                    <tr>\n' +
    '                                        <td>里程 <span  class="color-blue" id="mileText">--</span> km</td>\n' +
    '                                        <td>最高速度 <span  class="color-blue" id="maxSpeedText">--</span> km/h</td>\n' +
    '                                    </tr>\n' +
    '                                </table>\n' +
    '                            </div>',
    stopData: '<div class="chart-item" id="stopDataItem">\n' +
    '                                <div class="chart-title">行驶/停止</div>\n' +
    '                                <div class="chart-line">\n' +
    '                                    <div id="stopDataChart" class="chart-wraper"></div>\n' +
    '                                </div>\n' +
    '                                <table class="chart-text">\n' +
    '                                    <tr>\n' +
    '                                        <td>行驶次数 <span  class="color-blue" id="runTimesText">--</span> 次</td>\n' +
    '                                        <td>行驶里程 <span  class="color-blue" id="runMilesText">--</span> km</td>\n' +
    '                                        <td>行驶时长 <span  id="runTimeText">--</span></td>\n' +
    '                                    </tr>\n' +
    '                                    <tr>\n' +
    '                                        <td>停止次数 <span  class="color-blue" id="stopTimesText">--</span> 次</td>\n' +
    '                                        <td>怠速里程 <span  class="color-blue" id="stopMilesText">--</span> km</td>\n' +
    '                                        <td>停止时长 <span id="stopTimeText">--</span></td>\n' +
    '                                    </tr>\n' +
    '                                </table>\n' +
    '                            </div>',
    oilData: '<div class="chart-item" id="oilDataItem">\n' +
    '                                <div class="chart-title">油量数据 <select class="chart-select" id="oilDataSelect"></select></div>\n' +
    '                                <div class="chart-line">\n' +
    '                                    <div id="oilDataChart" class="chart-wraper"></div>\n' +
    '                                </div>\n' +
    '                                <table class="chart-text">\n' +
    '                                    <tr>\n' +
    '                                        <td>加油 <span  class="color-blue" id="addOilText">--</span> L</td>\n' +
    '                                        <td>漏油 <span  class="color-blue" id="spillOilText">--</span> L</td>\n' +
    '                                        <td>用油 <span  class="color-blue" id="useOilText">--</span> L</td>\n' +
    '                                    </tr>\n' +
    '                                </table>\n' +
    '                            </div>',
    oilConsumptionData: '<div class="chart-item" id="oilConsumptionDataItem">\n' +
    '                                <div class="chart-title">油耗数据 <select class="chart-select" id="oilConsumptionDataSelect"></select></div>\n' +
    '                                <div class="chart-line">\n' +
    '                                    <div id="oilConsumptionDataChart" class="chart-wraper"></div>\n' +
    '                                </div>\n' +
    '                                <table class="chart-text">\n' +
    '                                    <tr>\n' +
    '                                        <td>油耗 <span  class="color-blue" id="oilConsumptionText">--</span> L</td>\n' +
    '                                        <td>百公里油耗 <span  class="color-blue" id="oilConsumption100Text">--</span> L/100km</td>\n' +
    '                                    </tr>\n' +
    '                                </table>\n' +
    '                            </div>',
    temperaturey: '<div class="chart-item" id="temperatureyItem">\n' +
    '                                <div class="chart-title">温度数据 <select class="chart-select" id="temperatureySelect"></select></div>\n' +
    '                                <div class="chart-line">\n' +
    '                                    <div id="temperatureyChart" class="chart-wraper"></div>\n' +
    '                                </div>\n' +
    '                                <table class="chart-text">\n' +
    '                                    <tr>\n' +
    '                                        <td>最高温度 <span  class="color-blue" id="highestTempText">--</span> ℃</td>\n' +
    '                                        <td>最低温度 <span  class="color-blue" id="lowestTempText">--</span> ℃</td>\n' +
    '                                    </tr>\n' +
    '                                </table>\n' +
    '                            </div>',
    humidity: '<div class="chart-item" id="humidityItem">\n' +
    '                                <div class="chart-title">湿度数据 <select class="chart-select" id="humiditySelect"></select></div>\n' +
    '                                <div class="chart-line">\n' +
    '                                    <div id="humidityChart" class="chart-wraper"></div>\n' +
    '                                </div>\n' +
    '                                <table class="chart-text">\n' +
    '                                    <tr>\n' +
    '                                        <td>最高湿度 <span  class="color-blue" id="highestHumiText">--</span>%</td>\n' +
    '                                        <td>最低湿度 <span  class="color-blue" id="lowestHumiText">--</span>%</td>\n' +
    '                                    </tr>\n' +
    '                                </table>\n' +
    '                            </div>',
    workHour: '<div class="chart-item" id="workHourItem">\n' +
    '                                <div class="chart-title">工时数据 <select class="chart-select" id="workHourSelect"></select></div>\n' +
    '                                <div class="chart-line">\n' +
    '                                    <div id="workHourChart" class="chart-wraper"></div>\n' +
    '                                </div>\n' +
    '                                <table class="chart-text">\n' +
    '                                    <tr>\n' +
    '                                        <td>有效工时 <span  id="validWorkHourText">--</span></td>\n' +
    '                                        <td>待机工时 <span  id="waitWorkHourText">--</span></td>\n' +
    '                                        <td>停机工时 <span  id="stopWorkHourText">--</span></td>\n' +
    '                                    </tr>\n' +
    '                                </table>\n' +
    '                            </div>',
    reverse: '<div class="chart-item" id="reverseItem">\n' +
    '                                <div class="chart-title">正反转数据</div>\n' +
    '                                <div class="chart-line">\n' +
    '                                    <div id="reverseChart" class="chart-wraper"></div>\n' +
    '                                </div>\n' +
    '                                <table class="chart-text">\n' +
    '                                    <tr>\n' +
    '                                        <td>正转时长 <span  id="totalUpTimeText">--</span></td>\n' +
    '                                        <td>反转时长 <span  id="totalDownTimeText">--</span></td>\n' +
    '                                    </tr>\n' +
    '                                </table>\n' +
    '                            </div>',
    ioData: '<div class="chart-item" id="ioDataItem">\n' +
    '                                <div class="chart-title">I/O数据 <select class="chart-select" id="ioDataSelect"></select></div>\n' +
    '                                <div class="chart-line">\n' +
    '                                    <div id="ioDataChart" class="chart-wraper"></div>\n' +
    '                                </div>\n' +
    '                                <table class="chart-text">\n' +
    '                                    <tr>\n' +
    '                                        <td><span class="io-rect" id="ioUpRect"></span><span id="ioUpText"></span> <span  id="ioTotalUpText">--</span></td>\n' +
    '                                        <td><span class="io-rect" id="ioDownRect"></span><span id="ioDownText"></span> <span  id="ioTotalDownText">--</span></td>\n' +
    '                                    </tr>\n' +
    '                                </table>\n' +
    '                            </div>',
    weight: '<div class="chart-item" id="weightItem">\n' +
    '                                <div class="chart-title">载重数据 <select class="chart-select" id="weightSelect"></select></div>\n' +
    '                                <div class="chart-line">\n' +
    '                                    <div id="weightChart" class="chart-wraper"></div>\n' +
    '                                </div>\n' +
    '                                <table class="chart-text">\n' +
    '                                    <tr>\n' +
    '                                        <td>空载 <span  id="emptyText">--</span></td>\n' +
    '                                        <td>轻载 <span  id="lightText">--</span></td>\n' +
    '                                        <td>重载 <span  id="heavyText">--</span></td>\n' +
    '                                    </tr>\n' +
    '                                    <tr>\n' +
    '                                        <td>满载 <span  id="fullText">--</span></td>\n' +
    '                                        <td>超载 <span  id="overText">--</span></td>\n' +
    '                                    </tr>\n' +
    '                                </table>\n' +
    '                            </div>',
    tire: '<div class="chart-item" id="tireItem">\n' +
    '                                <div class="chart-title">胎压数据 <select class="chart-select" id="tireSelect"></select></div>\n' +
    '                                <div class="chart-line">\n' +
    '                                    <div id="tireChart" class="chart-wraper"></div>\n' +
    '                                </div>\n' +
    '                                <table class="chart-text">\n' +
    '                                    <tr>\n' +
    '                                        <td>最高胎压 <span  class="color-blue" id="highestTireText">--</span> bar</td>\n' +
    '                                        <td>最低胎压 <span  class="color-blue" id="lowestTireText">--</span> bar</td>\n' +
    '                                    </tr>\n' +
    '                                </table>\n' +
    '                            </div>',
}

Chart.prototype.reRender = function () {
    var activeSensorKeys = this.dependency.get('data').getActiveSensorKeys();

    if (activeSensorKeys !== null) {
        for (var i = 0; i < activeSensorKeys.length; i++) {
            if (activeSensorKeys[i].chart) {
                activeSensorKeys[i].chart.initHtml();
            }
        }
    }
}

Chart.prototype.onSensorItemClick = function (index) {
    var dataDependency = this.dependency.get('data');
    var activeSensorKeys = dataDependency.getActiveSensorKeys();
    var sensorList = dataDependency.getSensorList();

    if (activeSensorKeys !== null) {
        dataDependency.setActiveSensorKeys([activeSensorKeys[1], {
            key: sensorList[index].key,
            chart: null
        }]);
    }
}

Chart.prototype.setChartOpen = function () {
    var isChartOpen = this.dependency.get('data').getIsChartOpen();
    var $topPart = $('#topPart');
    var $toggleChartText = $('#toggleChartText');
    if (isChartOpen) {
        $topPart.addClass('chart-open');
        $toggleChartText.html('隐<br/>藏')
    } else {
        $topPart.removeClass('chart-open');
        $toggleChartText.html('曲<br/>线<br/>报<br/>表')
    }
}

Chart.prototype.onSensorPageClick = function () {
    var $this = $(this);
    var $sensorContainer = $('#sensorContainer');
    var scrollLeft = $sensorContainer.scrollLeft();
    var childCount = $sensorContainer.find('.sensor-item').length;
    var maxPx = (childCount - 9) * 60;

    if ($this.hasClass('prev')) {
        scrollLeft = Math.max(0, scrollLeft - 60);
    } else {
        scrollLeft = Math.min(maxPx, scrollLeft + 60);
    }
    $sensorContainer.animate({
        scrollLeft: scrollLeft + 'px'
    }, 200);

    if (scrollLeft >= maxPx) {
        $('#nextPageIcon').hide();
    } else {
        $('#nextPageIcon').show();
    }
    if (scrollLeft > 0) {
        $('#prevPageIcon').show();
    } else {
        $('#prevPageIcon').hide();
    }
}

Chart.prototype.onDrag = function (v) {
    if (v && v.playIndex !== undefined) {
        this.dependency.get('data').setIsDraging(true);
        if (this.dependency.get('data').getIsPlaying()) {
            this.dependency.get('map').pause();
        }
        this.dependency.get('data').setPlayIndex(v.playIndex, 'chart');
    }
}

Chart.prototype.onDragEnd = function (v) {
    if (v && v.playIndex !== undefined) {
        this.dependency.get('data').setIsDraging(false);
        if (this.dependency.get('data').getIsPlaying()) {
            this.dependency.get('map').pause();
        }
        this.dependency.get('data').setPlayIndex(v.playIndex, 'chart');
    }
}

Chart.prototype.updatePlayIndex = function () {
    var playIndex = this.dependency.get('data').getPlayIndex('chart');
    var activeSensorKeys = this.dependency.get('data').getActiveSensorKeys();
    if (activeSensorKeys !== null) {
        for (var i = 0; i < activeSensorKeys.length; i++) {
            if (activeSensorKeys[i].chart) {
                activeSensorKeys[i].chart.updatePositionByPlayIndex(playIndex);
            }
        }
    }
}

Chart.prototype.getSensorList = function () {
    var dataDependency = this.dependency.get('data');
    var vehicleId = dataDependency.getVid();

    // 更新停止/行驶数据
    json_ajax('GET', '/clbs/v/monitoring/getPollingList', 'json', true, {
        "monitorId": vehicleId
    }, function (data) {
        if (data.success) {
            var mileStopSensor = ['0x53', '0x00'];
            var serverList = [];
            if (data.obj.sensorPollingList) {
                serverList = data.obj.sensorPollingList;
            }
            var list = mileStopSensor.concat(serverList);
            // 组装 key icon 文本 等信息
            var sensorInfoList = [];
            var keys = Object.keys(this.sensorMeta);
            for (var i = 0; i < keys.length; i++) {
                var key = keys[i];
                var element = this.sensorMeta[key];
                var ids = element.ids;
                var toPush = {
                    key: key,
                    text: element.text,
                    icon: element.icon,
                    ids: []
                }
                for (var j = 0; j < ids.length; j++) {
                    var id = ids[j];
                    if (list.indexOf(id) > -1) {
                        toPush.ids.push(id);
                    }
                }
                if (toPush.ids.length > 0) {
                    sensorInfoList.push(toPush);
                }
            }

            dataDependency.setSensorList(sensorInfoList);
            dataDependency.setActiveSensorKeys(null);
        }
    }.bind(this));
}

Chart.prototype.initSensorHtml = function () {
    var dataDependency = this.dependency.get('data');
    var sensorList = dataDependency.getSensorList();
    var $sensorWraper = $('#sensorWraper');
    $sensorWraper.empty();
    if (sensorList !== null) {
        var sensorTmpl = '<div class="sensor-item un-active">\n' +
            '                                        <div class="sensor-icon {{icon}}"></div>\n' +
            '                                        <div class="sensor-text">{{text}}</div>\n' +
            '                                    </div>';
        for (var i = 0; i < sensorList.length; i++) {
            var item = sensorList[i];
            $sensorWraper.append($(sensorTmpl
                .replace('{{icon}}', item.icon)
                .replace('{{text}}', item.text))
            );
        }

        // 初始化左右箭头的显示和隐藏
        $('#prevPageIcon').hide();
        if (sensorList.length > 9) {
            $('#nextPageIcon').show();
        } else {
            $('#nextPageIcon').hide();
        }
    }
}

Chart.prototype.renderChartLine = function () {
    var dataDependency = this.dependency.get('data');
    var activeSensorKeys = dataDependency.getActiveSensorKeys();

    // 处理传感器图标高亮
    $('.sensor-item').addClass('un-active');
    if (activeSensorKeys !== null) {
        for (var i = 0; i < activeSensorKeys.length; i++) {
            var key = activeSensorKeys[i].key;
            var element = this.sensorMeta[key];
            if (key) {
                $('.' + element.icon).closest('.sensor-item').removeClass('un-active');
            }
        }
    }

    // 如果dom里一个都没有，说明是第一次，直接获取两个传感器的曲线
    // 反之，这说明是用户切换底部传感器触发，将上面的一个dom删除，新增第二个传感器的曲线

    var domIds = $('.chart-item');
    if (activeSensorKeys === null) {
        $('.chart-item').remove();
        return;
    }

    var getData = function (key) {
        switch (key) {
            case 'mileSpeed':
                this.getMileSpeed();
                break;
            case 'stopData':
                this.getStopData();
                break;
            case 'oilConsumptionData':
                this.initOilConsumptionHTML();
                break;
            case 'oilData':
                this.initOilHTML();
                break;
            case 'temperaturey':
                this.initTemperatureyHTML();
                break;
            case 'humidity':
                this.initHumidityHTML();
                break;
            case 'workHour':
                this.initWorkHourHTML();
                break;
            case 'reverse':
                this.getReverse();
                break;
            case 'ioData':
                this.getIoData();
                break;
            case 'weight':
                this.initWeightHTML();
                break;
            case 'tire':
                this.initTireHTML();
                break;
            default:
                break;
        }
    }

    if (domIds.length === 0) {
        for (var i = 0; i < activeSensorKeys.length; i++) {
            var key = activeSensorKeys[i].key;
            getData.call(this, key);
        }
    } else {
        var $upperItem = $('.chart-item:eq(0)');
        var self = this;
        $upperItem.slideUp(function () {
            $(this).remove();
            var key = activeSensorKeys[1].key;
            getData.call(self, key);
        });
    }
}

// <editor-fold desc="里程速度">
Chart.prototype.getMileSpeed = function () {
    $('#chartContainer').append($(this.chartTmpl.mileSpeed));

    var dataDependency = this.dependency.get('data');
    var vehicleId = dataDependency.getVid();
    var startTime = dataDependency.getStartTime();
    var endTime = dataDependency.getEndTime();

    var validSensorFlag = dataDependency.getValidSensorFlag();
    var validClickedIndex = dataDependency.getValidClickedIndex();
    var sensorFlag = validSensorFlag && (validClickedIndex !== undefined && validClickedIndex !== null)
        ? validSensorFlag[validSensorFlag] : undefined;

    json_ajax('GET', '/clbs/v/monitoring/getMileAndSpeed', 'json', true, {
        monitorId: vehicleId,
        startTime: startTime,
        endTime: endTime,
        sensorFlag: sensorFlag
    }, function (data) {
        if (data.success) {
            dataDependency.setMileSpeed(data.obj.mileAndSpeed ? data.obj.mileAndSpeed : [])
        } else {
            layer.msg(data.exceptionDetailMsg || data.msg);
        }
    })
}

Chart.prototype.mileSpeedChange = function () {
    var dataDependency = this.dependency.get('data');
    var activeSensorKeys = dataDependency.getActiveSensorKeys();
    var data = dataDependency.getMileSpeed();
    var playIndex = dataDependency.getPlayIndex('chart');
    var dataSize = data.length;

    var maxMileage = 0;
    var maxSpeed = 0;

    if (dataSize > 0) {
        maxSpeed = parseFloat(data[0].speed);
    }

    var mileageSerieData = [];
    var speedSerieData = [];
    for (var i = 0; i < dataSize; i += 1) {
        var element = data[i];
        var mileage = element.mileage;
        mileage = mileage === null ? null : parseFloat(mileage);
        var speed = element.speed;
        speed = speed === null ? null : parseFloat(speed);

        var time = new Date(element.time * 1000);
        /** 如果一个点的里程小于0，则向前向后寻找大于0的点来替换自己, 向前优先 */
        if (mileage !== null && mileage <= 0) {
            var toReplace = null;

            for (var j = 0, max = Math.max(i, dataSize - i - 1); j < max; j += 1) {
                var prev = data[i - j];
                var next = data[i + j];
                if (prev && (prev.mileage > 0)) {
                    toReplace = prev;
                } else if (next && (next.mileage > 0)) {
                    toReplace = next;
                }
                if (toReplace !== null) {
                    break;
                }
            }

            if (toReplace !== null) {
                data[i] = toReplace;
                mileage = toReplace.mileage;
                mileage = parseFloat(mileage);
                // console.warn(i, toReplace);
            }
        }

        mileageSerieData.push({
            index: i,
            date: time,
            value: mileage,
            supply: element.supply,
            color: mileage === null ? 'transparent' : '#86cdf3',
        });

        speedSerieData.push({
            index: i,
            date: time,
            value: speed,
            supply: element.supply,
            color: speed === null ? 'transparent' : '#a3d843',
        });

        if (speed >= maxSpeed) {
            maxSpeed = speed;
        }
    }

    if (dataSize > 0) {
        maxMileage = data[dataSize - 1].mileage - data[0].mileage;
        maxMileage = TrackPlaybackUtil.toFixed(maxMileage, 1, true);
    }

    var mileageText = TrackPlaybackUtil.getLocale('mileage');
    var series = [
        {
            data: mileageSerieData,
            width: 1,
            label: mileageText,
            unit: 'km',
            yValueFunc: function (item, serie) {
                var yValue = item.yValue;
                if (!yValue) {
                    return '---';
                }

                var index = yValue.index;
                var value = yValue.value;
                var supply = yValue.supply;
                if (supply) {
                    return '无数据';
                }
                if (value === null) {
                    return '无传感器数据';
                }
                var startMileage = serie.data[index].value - serie.data[0].value;
                startMileage = TrackPlaybackUtil.toFixed(startMileage, 2, true);
                return mileageText + ': ' + startMileage + ' km';
            },
        },
        {
            data: speedSerieData,
            width: 1,
            label: TrackPlaybackUtil.getLocale('speed'),
            unit: 'km/h',
            yMinValue: 0,
            yMaxValue: 240,
        },
    ];

    $('#mileText').html(maxMileage < 0 ? '-' : maxMileage);
    $('#maxSpeedText').html(maxSpeed);

    var chart = new LineChart('#mileSpeedChart', {
        series: series,
        playIndex: playIndex,
        snap: true,
        onDrag: this.onDrag.bind(this),
        onDragEnd: this.onDragEnd.bind(this)
    });
    // 添加到 activeSensorKeys 中的chart
    for (var i = 0; i < activeSensorKeys.length; i++) {
        var key = activeSensorKeys[i].key;
        if (key === 'mileSpeed') {
            activeSensorKeys[i].chart = chart;
        }
    }
}
// </editor-fold>

// <editor-fold desc="停止数据">
Chart.prototype.getStopData = function () {
    $('#chartContainer').append($(this.chartTmpl.stopData));

    var dataDependency = this.dependency.get('data');
    var vehicleId = dataDependency.getVid();
    var startTime = dataDependency.getStartTime();
    var endTime = dataDependency.getEndTime();
    var validSensorFlag = dataDependency.getValidSensorFlag();
    var validClickedIndex = dataDependency.getValidClickedIndex();
    var sensorFlag = validSensorFlag && (validClickedIndex !== undefined && validClickedIndex !== null)
        ? validSensorFlag[validSensorFlag] : undefined;

    json_ajax('GET', '/clbs/v/monitoring/getTravelAndStop', 'json', true, {
        monitorId: vehicleId,
        startTime: startTime,
        endTime: endTime,
        sensorFlag: sensorFlag
    }, function (data) {
        if (data.success) {
            dataDependency.setStopData(data.obj.travelAndStop ? data.obj.travelAndStop : [])
        } else {
            layer.msg(data.exceptionDetailMsg || data.msg);
        }
    })
}

Chart.prototype.stopDataChange = function () {
    var dataDependency = this.dependency.get('data');
    var activeSensorKeys = dataDependency.getActiveSensorKeys();
    var data = dataDependency.getStopData();
    var allOrRunData = dataDependency.getAllOrRunData();
    var positions = dataDependency.getPositions();
    var positionsTmp = dataDependency.getPositionsTmp();
    var playIndex = dataDependency.getPlayIndex('chart');
    var paraScale = dataDependency.getParaScale();
    var padEndData = data.concat([{
        x: Math.random()
    }]);

    var positionsArray = allOrRunData === 'run' ? positionsTmp : positions;

    var getColor = function (type) {
        if (type === null) {
            return 'lightgray';
        }
        if (type === '1') {
            return '#a3d843';
        }
        return '#ff9999';
    };

    var padEndDataLength = padEndData.length;
    var dataArr = [];

    var stopTotalTimeLength = 0; // 以秒为单位
    var stopTimes = 0;
    var stopTimeArr = [];
    var stopTotalMileageLength = 0;
    var runTotalTimeLength = 0; // 以秒为单位
    var runTimes = 0;
    var runTimeArr = [];
    var runTotalMileageLength = 0;

    if (padEndDataLength > 0) {
        var typeDict = {};
        var prevType = padEndData[0].status;
        var prevIndex = 0;
        var notNullIndex = 0;

        for (var i = 0; i < padEndDataLength; i += 1) {
            var element = padEndData[i];
            var currentType = element.status; // 	状态(1:行驶 2:停止)
            var time = element.time;

            if (i < padEndDataLength - 1) {
                var newItem = {};
                newItem.date = new Date(time * 1000);
                newItem.value = 1;
                newItem.supply = element.supply,
                    newItem.index = i;
                newItem.color = getColor(currentType);
                newItem.type = currentType;

                dataArr.push(newItem);
            }

            // 1.补点数据不参与计算
            // 2.两个点就算是相同状态，但时间差大于5分钟，也属于两个状态段
            // 3.如果两个状态之间时间差大于5分钟，则按状态段首尾点计算
            // 4.不然按下一个状态段第一个点 - 上一个状态段最后一点计算

            if (currentType === null) {
                continue;
            }
            var timeDiff = padEndData[i].time - padEndData[notNullIndex].time;
            if (currentType !== prevType || timeDiff > 300) {
                var item = typeDict[prevType];
                var endIndex = notNullIndex;
                // 如果两个状态之间时间差大于5分钟，则按状态段首尾点计算
                // 不然按下一个状态段第一个点 - 上一个状态段最后一点计算
                var timeLength = padEndData[endIndex].time - padEndData[prevIndex].time;
                var mileageLength = padEndData[endIndex].mileage - padEndData[prevIndex].mileage;
                if (timeDiff <= 300) {
                    endIndex = i;
                    timeLength = padEndData[endIndex].time - padEndData[prevIndex].time;
                    mileageLength = padEndData[endIndex].mileage - padEndData[prevIndex].mileage;
                }
                var newIndex = endIndex;
                if (newIndex > positionsArray.length) {
                    newIndex = positionsArray.length - 1;
                }
                var originStartIndex = paraScale(prevIndex, 'toOrigin');
                var originEndIndex = paraScale(endIndex, 'toOrigin');
                if (originStartIndex === undefined || originEndIndex === undefined) continue;
                var segment = {
                    startIndex: prevIndex,
                    endIndex: endIndex,
                    timeLength: timeLength,
                    mileageLength: mileageLength,
                    startTime: padEndData[prevIndex].time,
                    endTime: padEndData[endIndex] ? padEndData[endIndex].time : padEndData[newIndex].time,
                    originStartIndex: originStartIndex,
                    originEndIndex: originEndIndex,
                    monitorName: positionsArray[originStartIndex].plateNumber,
                    originStartTime: positionsArray[originStartIndex].vtime * 1000,
                    startLongtitude: positionsArray[originStartIndex].longtitude,
                    startLatitude: positionsArray[originStartIndex].latitude,
                    originEndTime: positionsArray[originEndIndex].vtime * 1000, // vtime 单位为秒，需要转为毫秒
                    endLongtitude: positionsArray[originEndIndex].longtitude,
                    endLatitude: positionsArray[originEndIndex].latitude,
                    deviceNumber: positionsArray[originStartIndex].deviceNumber,
                    simcardNumber: positionsArray[originStartIndex].simCard,
                    useOil: this.getUseOil(originStartIndex, originEndIndex, positionsArray),
                    consumeOil: this.getConsumeOil(originStartIndex, originEndIndex, positionsArray),
                };
                if (item) {
                    item.occurrence += 1;
                    item.totalTimeLength += timeLength;
                    item.totalMileageLength += mileageLength;
                    item.segment.push(segment);
                } else {
                    typeDict[prevType] = {
                        type: prevType,
                        occurrence: 1,
                        totalTimeLength: timeLength,
                        totalMileageLength: mileageLength,
                        segment: [segment],
                    };
                }
                prevType = currentType;
                prevIndex = i;
            }
            notNullIndex = i;

        }

        if (typeDict['2']) {
            stopTotalTimeLength = typeDict['2'].totalTimeLength;
            stopTotalMileageLength = typeDict['2'].totalMileageLength;
            stopTimes = typeDict['2'].occurrence;
            stopTimeArr = typeDict['2'].segment;
        }
        if (typeDict['1']) {
            runTotalTimeLength = typeDict['1'].totalTimeLength;
            runTotalMileageLength = typeDict['1'].totalMileageLength;
            runTimes = typeDict['1'].occurrence;
            runTimeArr = typeDict['1'].segment;
        }

    }

    if (allOrRunData !== 'run') {
        dataDependency.setStopTypeDict(typeDict);
    }


    // 渲染进度条背景
    new SimpleLine('#progressBack', {
        series: [{
            width: 8,
            autoConnectPartition: 'AFTER',
            data: dataArr
        }]
    });
    this.dependency.get('progressBar').setOptions({
        min: 0,
        max: data === null ? 0 : data.length - 1
    });

    if (activeSensorKeys === null) {
        return;
    }

    // 渲染图表
    var series = [
        {
            data: dataArr,
            width: 3,
            label: TrackPlaybackUtil.getLocale('runAndStop'),
            unit: 'h',
            stopTimeArr: stopTimeArr,
            runTimeArr: runTimeArr,
            autoConnectPartition: 'AFTER',
            yValueFunc: function (item, serie) {
                var yValue = item.yValue;
                if (!yValue) {
                    return '---';
                }
                var index = yValue.index;
                var type = yValue.type;

                var supply = yValue.supply;
                if (supply) {
                    return '无数据';
                }
                if (type === null) {
                    return '无传感器数据';
                }

                var stopTimeArrInSerie = serie.stopTimeArr;
                var runTimeArrInSerie = serie.runTimeArr;

                if (type == '1') {
                    // 行驶状态
                    for (var j = 0; j < runTimeArrInSerie.length; j += 1) {
                        var ele = runTimeArrInSerie[j];
                        if (ele.startIndex <= index && index <= ele.endIndex) {
                            return '行驶: ' + TrackPlaybackUtil.getTextFromSecond(ele.timeLength) + '\n' +
                                '里程: ' + TrackPlaybackUtil.toFixed(ele.mileageLength, 1, true) + ' km';
                        }
                    }
                }
                for (var ii = 0; ii < stopTimeArrInSerie.length; ii += 1) {
                    var ele = stopTimeArrInSerie[ii];
                    if (ele.startIndex <= index && index <= ele.endIndex) {
                        return '停止: ' + TrackPlaybackUtil.getTextFromSecond(ele.timeLength) + '\n' +
                            '怠速: ' + TrackPlaybackUtil.toFixed(ele.mileageLength, 1, true) + ' km';
                    }
                }
                return '---';
            },
            yMinValue: 0,
            yMaxValue: 1.8,
        },
    ];

    $('#runTimesText').html(runTimes);
    $('#runMilesText').html(TrackPlaybackUtil.toFixed(runTotalMileageLength, 1, true));
    $('#runTimeText').html(TrackPlaybackUtil.getHTMLFromSecond(runTotalTimeLength));
    $('#stopTimesText').html(stopTimes);
    $('#stopMilesText').html(TrackPlaybackUtil.toFixed(stopTotalMileageLength, 1, true));
    $('#stopTimeText').html(TrackPlaybackUtil.getHTMLFromSecond(stopTotalTimeLength));

    var chart = new LineChart('#stopDataChart', {
        series: series,
        playIndex: playIndex,
        snap: true,
        onDrag: this.onDrag.bind(this),
        onDragEnd: this.onDragEnd.bind(this)
    });
    // 添加到 activeSensorKeys 中的chart
    for (var i = 0; i < activeSensorKeys.length; i++) {
        var key = activeSensorKeys[i].key;
        if (key === 'stopData') {
            activeSensorKeys[i].chart = chart;
        }
    }
}

Chart.prototype.getUseOil = function (originalStartIndex, originalEndIndex, positionArray) {
    // 计算用油量 该行驶段内第一个点的油量 + 行驶段内的加油量 – 行驶段内漏油量 – 行驶段内最后一个点油量，（主油箱和副油箱之和）
    var useOil = 0;
    if (!TrackPlaybackUtil.isEmpty(positionArray[originalStartIndex].oilTankOne)) {
        useOil += TrackPlaybackUtil.tryParseFloat(positionArray[originalStartIndex].oilTankOne);
    }
    if (!TrackPlaybackUtil.isEmpty(positionArray[originalStartIndex].oilTankTwo)) {
        useOil += TrackPlaybackUtil.tryParseFloat(positionArray[originalStartIndex].oilTankTwo);
    }

    if (!TrackPlaybackUtil.isEmpty(positionArray[originalEndIndex].oilTankOne)) {
        useOil -= TrackPlaybackUtil.tryParseFloat(positionArray[originalEndIndex].oilTankOne);
    }
    if (!TrackPlaybackUtil.isEmpty(positionArray[originalEndIndex].oilTankTwo)) {
        useOil -= TrackPlaybackUtil.tryParseFloat(positionArray[originalEndIndex].oilTankTwo);
    }

    for (var j = originalStartIndex; j <= originalEndIndex; j++) {
        if (!TrackPlaybackUtil.isEmpty(positionArray[j].fuelAmountOne)) {
            useOil += TrackPlaybackUtil.tryParseFloat(positionArray[j].fuelAmountOne);
        }
        if (!TrackPlaybackUtil.isEmpty(positionArray[j].fuelAmountTwo)) {
            useOil += TrackPlaybackUtil.tryParseFloat(positionArray[j].fuelAmountTwo);
        }

        if (!TrackPlaybackUtil.isEmpty(positionArray[j].fuelSpillOne)) {
            useOil -= TrackPlaybackUtil.tryParseFloat(positionArray[j].fuelSpillOne);
        }
        if (!TrackPlaybackUtil.isEmpty(positionArray[j].fuelSpillTwo)) {
            useOil -= TrackPlaybackUtil.tryParseFloat(positionArray[j].fuelSpillTwo);
        }
    }

    return useOil;
}

Chart.prototype.getConsumeOil = function (originalStartIndex, originalEndIndex, positionArray) {
    // 计算用油量 该行驶段内第一个点的油量 + 行驶段内的加油量 – 行驶段内漏油量 – 行驶段内最后一个点油量，（主油箱和副油箱之和）
    var useOil = 0;
    if (!TrackPlaybackUtil.isEmpty(positionArray[originalStartIndex].totalOilwearOne)) {
        useOil -= TrackPlaybackUtil.tryParseFloat(positionArray[originalStartIndex].totalOilwearOne);
    }
    if (!TrackPlaybackUtil.isEmpty(positionArray[originalStartIndex].totalOilwearTwo)) {
        useOil -= TrackPlaybackUtil.tryParseFloat(positionArray[originalStartIndex].totalOilwearTwo);
    }

    if (!TrackPlaybackUtil.isEmpty(positionArray[originalEndIndex].totalOilwearOne)) {
        useOil += TrackPlaybackUtil.tryParseFloat(positionArray[originalEndIndex].totalOilwearOne);
    }
    if (!TrackPlaybackUtil.isEmpty(positionArray[originalEndIndex].totalOilwearTwo)) {
        useOil += TrackPlaybackUtil.tryParseFloat(positionArray[originalEndIndex].totalOilwearTwo);
    }

    return useOil;
}

// </editor-fold>

// <editor-fold desc="油量数据">

Chart.prototype.initOilHTML = function () {
    $('#chartContainer').append($(this.chartTmpl.oilData));

    var dataDependency = this.dependency.get('data');
    var sensorList = dataDependency.getSensorList();

    var $oilDataSelect = $('#oilDataSelect');

    for (var i = 0; i < sensorList.length; i++) {
        var sensor = sensorList[i];
        if (sensor.key === 'oilData') {
            for (var j = 0; j < sensor.ids.length; j++) {
                var index = sensor.ids[j] - 0x40;
                $oilDataSelect.append($('<option value="' + (j + 1) + '">' + index + '#</option>'));
            }
        }
    }
    $oilDataSelect.on('change', function () {
        dataDependency.setOilSensorNo($(this).val());
    });
    dataDependency.setOilSensorNo(1);
}

Chart.prototype.getOilData = function () {
    var dataDependency = this.dependency.get('data');
    var vehicleId = dataDependency.getVid();
    var startTime = dataDependency.getStartTime();
    var endTime = dataDependency.getEndTime();
    var validSensorFlag = dataDependency.getValidSensorFlag();
    var validClickedIndex = dataDependency.getValidClickedIndex();
    var sensorFlag = validSensorFlag && (validClickedIndex !== undefined && validClickedIndex !== null)
        ? validSensorFlag[validSensorFlag] : undefined;

    json_ajax('GET', '/clbs/v/monitoring/getOilMass', 'json', true, {
        monitorId: vehicleId,
        startTime: startTime,
        endTime: endTime,
        sensorFlag: sensorFlag
    }, function (data) {
        if (data.success) {
            dataDependency.setOilData(data.obj.oilMass ? data.obj.oilMass : [])
        } else {
            layer.msg(data.exceptionDetailMsg || data.msg);
        }
    })
}

Chart.prototype.oilDataChange = function () {
    var dataDependency = this.dependency.get('data');
    var activeSensorKeys = dataDependency.getActiveSensorKeys();
    var oilData = dataDependency.getOilData();
    var attachIndex = dataDependency.getOilSensorNo() - 1; // 传感器显示是从1开始，数组索引从0开始
    var playIndex = dataDependency.getPlayIndex('chart');
    var dataSize = oilData.length;

    var totalAddOil = 0;
    var totalReduceOil = 0;
    var totalUseOil = 0;
    var serieData = [];
    var dotts = [];
    var maxOil = 0;

    for (var i = 0; i < dataSize; i += 1) {
        var element = oilData[i];

        var oil = element.oilTank[attachIndex];
        oil = oil === null || oil === undefined ? null : parseFloat(oil);

        /** 如果一个点的油量小于等于0.5，则向前向后寻找大于0.5的点来替换自己, 向前优先
         * 只有第一个和最后一个点这样循环寻找，中间的点向前找一个点就可以了
         * */
        if (oil === null || oil <= 0.5) {
            var toReplace = null;

            if (i === 0 || i === dataSize - 1) {
                for (var j = 0, max = Math.max(i, dataSize - i - 1); j < max; j += 1) {
                    var prev = oilData[i - j];
                    var next = oilData[i + j];
                    if (prev && (prev.oilTank[attachIndex] > 0.5)) {
                        toReplace = prev;
                    } else if (next && (next.oilTank[attachIndex] > 0.5)) {
                        toReplace = next;
                    }
                    if (toReplace !== null && toReplace !== undefined) {
                        break;
                    }
                }
            } else if (oilData[i - 1] && (oilData[i - 1].oilTank[attachIndex] > 0.5)) {
                toReplace = oilData[i - 1];
            }
            if (toReplace !== null && toReplace !== undefined) {
                oilData[i] = toReplace;
                oil = toReplace.oilTank[attachIndex];
                oil = TrackPlaybackUtil.isEmpty(oil) ? 0 : parseFloat(oil);
            }
        }
        if (oil > maxOil) {
            maxOil = oil;
        }

        var color = oil === null || oil === undefined ? 'transparent' : '#e88031';
        var fuelAmount = element.fuelAmount[attachIndex];
        var fuelSpill = element.fuelSpill[attachIndex];
        if (fuelAmount !== null && fuelAmount !== undefined) {
            fuelAmount = parseFloat(fuelAmount);
            totalAddOil += fuelAmount;
        }
        if (fuelSpill !== null && fuelSpill !== undefined) {
            fuelSpill = parseFloat(fuelSpill);
            totalReduceOil += fuelSpill;
        }

        var newElement = {
            index: i,
            date: new Date(element.time * 1000),
            value: oil,
            supply: element.supply,
            color: color,
            fuelAmount: fuelAmount,
            fuelSpill: fuelSpill,
        };
        serieData.push(newElement);
        if (fuelAmount > 0) {
            dotts.push({
                x: i,
                color: 'green',
            });
        }
        if (fuelSpill > 0) {
            dotts.push({
                x: i,
                color: 'red',
            });
        }
    }
    var series = [{
        data: serieData,
        width: 1,
        label: TrackPlaybackUtil.getLocale('oilDataText'),
        dott: dotts,
        yValueFunc: function (rItem, serie) {
            if (!rItem.yValue) {
                return '---';
            }
            var yValue = rItem.yValue;
            var supply = yValue.supply;
            if (supply) {
                return '无数据';
            }
            if (rItem.yValue.value === null) {
                return '无传感器数据';
            }

            var fuelAmount = rItem.yValue.fuelAmount;
            var fuelSpill = rItem.yValue.fuelSpill;

            if (fuelAmount > 0) {
                return rItem.text + '\n' + TrackPlaybackUtil.getLocale('addOil') + fuelAmount + serie.unit;
            }
            if (fuelSpill > 0) {
                return rItem.text + '\n' + TrackPlaybackUtil.getLocale('reduceOil') + fuelSpill + serie.unit;
            }
            return TrackPlaybackUtil.getLocale('leftOil') + ' ' + rItem.text;
        },
        unit: 'L',
        yMinValue: 0,
        yMaxValue: maxOil > 350 ? maxOil : 350,
    }];
    if (oilData.length > 0) {
        // 用油量 = 曲线第一个点油量 + 所有加油量 – 所有漏油量 – 曲线最后一个点油量
        var firstOil = oilData[0].oilTank[attachIndex];
        var lastOil = oilData[oilData.length - 1].oilTank[attachIndex];
        var useOil = firstOil - lastOil;

        if (firstOil === null || firstOil === undefined || lastOil === null || lastOil === undefined) {
            useOil = 0;
        }

        totalUseOil = useOil + totalAddOil - totalReduceOil;
    }

    $('#addOilText').html(TrackPlaybackUtil.toFixed(totalAddOil, 1, true));
    $('#spillOilText').html(TrackPlaybackUtil.toFixed(totalReduceOil, 1, true));
    $('#useOilText').html(TrackPlaybackUtil.toFixed(totalUseOil, 1, true));

    var chart = new LineChart('#oilDataChart', {
        series: series,
        playIndex: playIndex,
        snap: true,
        onDrag: this.onDrag.bind(this),
        onDragEnd: this.onDragEnd.bind(this)
    });
    // 添加到 activeSensorKeys 中的chart
    for (var i = 0; i < activeSensorKeys.length; i++) {
        var key = activeSensorKeys[i].key;
        if (key === 'oilData') {
            activeSensorKeys[i].chart = chart;
        }
    }
}
// </editor-fold>

// <editor-fold desc="油耗数据">

Chart.prototype.initOilConsumptionHTML = function () {
    $('#chartContainer').append($(this.chartTmpl.oilConsumptionData));

    var dataDependency = this.dependency.get('data');
    var sensorList = dataDependency.getSensorList();

    var $oilConsumptionDataSelect = $('#oilConsumptionDataSelect');

    for (var i = 0; i < sensorList.length; i++) {
        var sensor = sensorList[i];
        if (sensor.key === 'oilConsumptionData') {
            for (var j = 0; j < sensor.ids.length; j++) {
                var index = sensor.ids[j] - 0x44;
                $oilConsumptionDataSelect.append($('<option value="' + index + '">' + index + '#</option>'));
            }
        }
    }
    $oilConsumptionDataSelect.on('change', function () {
        dataDependency.setOilConsumptionSensorNo($(this).val());
    });
    dataDependency.setOilConsumptionSensorNo(1);
}

Chart.prototype.getOilConsumptionData = function () {
    var dataDependency = this.dependency.get('data');
    var vehicleId = dataDependency.getVid();
    var startTime = dataDependency.getStartTime();
    var endTime = dataDependency.getEndTime();
    var validSensorFlag = dataDependency.getValidSensorFlag();
    var validClickedIndex = dataDependency.getValidClickedIndex();
    var sensorFlag = validSensorFlag && (validClickedIndex !== undefined && validClickedIndex !== null)
        ? validSensorFlag[validSensorFlag] : undefined;
    var sensorNo = dataDependency.getOilConsumptionSensorNo();

    json_ajax('POST', '/clbs/v/monitoring/getOilConsumptionChartData', 'json', true, {
        vehicleId: vehicleId,
        startTime: startTime,
        endTime: endTime,
        sensorFlag: sensorFlag,
        sensorNo: sensorNo
    }, function (data) {
        if (data.success) {
            dataDependency.setOilConsumptionData(data.obj.sensorDataList ? data.obj.sensorDataList : [])
        } else {
            layer.msg(data.exceptionDetailMsg || data.msg);
        }
    })
}

Chart.prototype.oilConsumptionDataChange = function () {
    var dataDependency = this.dependency.get('data');
    var activeSensorKeys = dataDependency.getActiveSensorKeys();
    var data = dataDependency.getOilConsumptionData();
    var playIndex = dataDependency.getPlayIndex('chart');
    var dataSize = data.length;

    var totalOilConsumption = 0;
    var totalOilConsumption100 = 0;
    var serieData = [];

    for (var i = 0; i < dataSize; i += 1) {
        var color = '#e88031';
        var x = data[i];
        var amount = x.oilWear;

        if (amount === null) {
            color = 'transparent';
        } else {
            amount = parseFloat(amount);
            /** 如果一个点的油耗小于等于0，则向前向后寻找大于等于0的点来替换自己, 向前优先
             * 只有第一个和最后一个点这样循环寻找，中间的点向前找一个点就可以了
             */
            if (amount <= 0) {
                var toReplace = null;

                if (i === 0 || i === dataSize - 1) {
                    for (var j = 1, max = Math.max(i, dataSize - i - 1); j < max; j += 1) {
                        var prev = data[i - j];
                        var next = data[i + j];
                        // console.warn(prev, next);

                        if (prev && (prev.oilWear > 0)) {
                            toReplace = prev;
                        } else if (next && (next.oilWear > 0)) {
                            toReplace = next;
                        }
                        if (toReplace !== null) {
                            break;
                        }
                    }
                } else if (data[i - 1] && (data[i - 1].oilWear > 0)) {
                    toReplace = data[i - 1];
                }

                if (toReplace !== null) {
                    data[i] = toReplace;
                    amount = toReplace.oilWear;
                    amount = TrackPlaybackUtil.isEmpty(amount) ? 0 : parseFloat(amount);
                }
            }
        }


        serieData.push({
            index: i,
            date: new Date(x.time * 1000),
            value: amount,
            supply: x.supply,
            color: color,
        });
    }

    if (dataSize > 0) {
        var firstIndex = null;
        var lastIndex = null;

        for (var i = 0; i < dataSize; i += 1) {
            if (firstIndex === null && !TrackPlaybackUtil.isEmpty(data[i].oilWear)) {
                firstIndex = i;
            }
            if (lastIndex === null && !TrackPlaybackUtil.isEmpty(data[dataSize - i - 1].oilWear)) {
                lastIndex = dataSize - i - 1;
            }
            if (firstIndex !== null && lastIndex !== null) {
                break;
            }
        }

        if (firstIndex !== null && lastIndex !== null) {
            var firstAmount = TrackPlaybackUtil.tryParseFloat(data[firstIndex].oilWear, 0);
            var lastAmount = TrackPlaybackUtil.tryParseFloat(data[lastIndex].oilWear, 0);
            var firstMileage = TrackPlaybackUtil.tryParseFloat(data[firstIndex].mileage, 0);
            var lastMileage = TrackPlaybackUtil.tryParseFloat(data[lastIndex].mileage, 0);

            totalOilConsumption = lastAmount - firstAmount;
            if (lastMileage - firstMileage === 0) {
                totalOilConsumption100 = '--';
            } else {
                totalOilConsumption100 = (lastAmount - firstAmount)
                    / (lastMileage - firstMileage) * 100;
                totalOilConsumption100 = TrackPlaybackUtil.toFixed(totalOilConsumption100, 1, true);
            }
        }
    }
    var series = [
        {
            data: serieData,
            width: 1,
            label: TrackPlaybackUtil.getLocale('oilConsumption'),
            unit: 'L',
            yValueFunc: function (item) {
                var yValue = item.yValue;
                if (!yValue) {
                    return '---';
                }
                var supply = yValue.supply;
                if (supply) {
                    return '无数据';
                }
                if (yValue.value === null) {
                    return '无传感器数据';
                }
                var value = yValue.value;
                return value + ' L';
                // let usedOil = serie.data[index].value - serie.data[0].value;
                // usedOil = TrackPlaybackUtil.toFixed(usedOil, 2, true);
                // return `${TrackPlaybackUtil.getLocale('useOil')}:${usedOil} L`;
            },
        },
    ];

    $('#oilConsumptionText').html(TrackPlaybackUtil.toFixed(totalOilConsumption, 1, true));
    $('#oilConsumption100Text').html(TrackPlaybackUtil.toFixed(totalOilConsumption100, 1, true));

    var chart = new LineChart('#oilConsumptionDataChart', {
        series: series,
        playIndex: playIndex,
        snap: true,
        rectHeight: 22,
        onDrag: this.onDrag.bind(this),
        onDragEnd: this.onDragEnd.bind(this)
    });
    // 添加到 activeSensorKeys 中的chart
    for (var i = 0; i < activeSensorKeys.length; i++) {
        var key = activeSensorKeys[i].key;
        if (key === 'oilConsumptionData') {
            activeSensorKeys[i].chart = chart;
        }
    }
}
// </editor-fold>

// <editor-fold desc="温度数据">

Chart.prototype.initTemperatureyHTML = function () {
    $('#chartContainer').append($(this.chartTmpl.temperaturey));

    var dataDependency = this.dependency.get('data');
    var sensorList = dataDependency.getSensorList();

    var $temperatureySelect = $('#temperatureySelect');

    for (var i = 0; i < sensorList.length; i++) {
        var sensor = sensorList[i];
        if (sensor.key === 'temperaturey') {
            for (var j = 0; j < sensor.ids.length; j++) {
                var index = sensor.ids[j] - 0x20;
                $temperatureySelect.append($('<option value="' + index + '">' + index + '#</option>'));
            }
        }
    }
    $temperatureySelect.on('change', function () {
        dataDependency.setTemperatureySensorNo($(this).val());
    });
    dataDependency.setTemperatureySensorNo(1);
}

Chart.prototype.getTemperaturey = function () {
    var dataDependency = this.dependency.get('data');
    var vehicleId = dataDependency.getVid();
    var startTime = dataDependency.getStartTime();
    var endTime = dataDependency.getEndTime();
    var validSensorFlag = dataDependency.getValidSensorFlag();
    var validClickedIndex = dataDependency.getValidClickedIndex();
    var sensorFlag = validSensorFlag && (validClickedIndex !== undefined && validClickedIndex !== null)
        ? validSensorFlag[validSensorFlag] : undefined;
    var sensorNo = dataDependency.getTemperatureySensorNo();

    json_ajax('POST', '/clbs/v/monitoring/getTemperatureChartData', 'json', true, {
        vehicleId: vehicleId,
        startTime: startTime,
        endTime: endTime,
        sensorFlag: sensorFlag,
        sensorNo: sensorNo
    }, function (data) {
        if (data.success) {
            dataDependency.setTemperaturey(data.obj ? data.obj : {})
        } else {
            layer.msg(data.exceptionDetailMsg || data.msg);
        }
    })
}

Chart.prototype.temperatureyChange = function () {
    var dataDependency = this.dependency.get('data');
    var activeSensorKeys = dataDependency.getActiveSensorKeys();
    var data = dataDependency.getTemperaturey();
    var playIndex = dataDependency.getPlayIndex('chart');

    var temprature = data.sensorDataList;
    var dataSize = temprature.length;

    var highestTemp = null;
    var lowestTemp = null;
    var series;

    if (temprature) {
        var line = [];
        if (data.highTemperatureThreshold) {
            line.push({
                value: parseFloat(data.highTemperatureThreshold),
                color: 'red',
            })
        }
        if (data.lowTemperatureThreshold) {
            line.push({
                value: parseFloat(data.lowTemperatureThreshold),
                color: 'red',
            });
        }

        var serieData = [];


        for (var i = 0; i < dataSize; i += 1) {
            var element = temprature[i];
            var value = element.temperature;

            var newElement = {
                index: i,
                date: new Date(element.time * 1000),
                value: value,
                supply: element.supply,
                color: value !== null ? '#962219' : 'transparent',
            };
            serieData.push(newElement);

            if (value !== null) {
                if (highestTemp === null) {
                    highestTemp = value;
                    lowestTemp = value;
                } else {
                    if (value >= highestTemp) {
                        highestTemp = value;
                    }
                    if (value <= lowestTemp) {
                        lowestTemp = value;
                    }
                }
            }
        }
        series = [{
            data: serieData,
            width: 1,
            label: TrackPlaybackUtil.getLocale('tempText'),
            unit: '℃',
            yValueFunc: function (rItem) {
                if (!rItem.yValue) {
                    return '---';
                }
                var yValue = rItem.yValue;
                var supply = yValue.supply;
                if (supply) {
                    return '无数据';
                }
                if (rItem.yValue.value === null) {
                    return '无传感器数据';
                }
                return rItem.text;
            },
            line: line,
            yMinValue: Math.min(-55, data.minTemperature),
            yMaxValue: Math.max(125, data.maxTemperature),
        }];
        highestTemp = highestTemp === null ? '--' : TrackPlaybackUtil.toFixed(highestTemp, 1, true);
        lowestTemp = lowestTemp === null ? '--' : TrackPlaybackUtil.toFixed(lowestTemp, 1, true);
    } else {
        series = null;
        highestTemp = '--';
        lowestTemp = '--';
    }

    $('#highestTempText').html(data.maxTemperature == null ? '--' : data.maxTemperature);
    $('#lowestTempText').html(data.minTemperature == null ? '--' : data.minTemperature);

    var chart = new LineChart('#temperatureyChart', {
        series: series,
        playIndex: playIndex,
        snap: true,
        rectHeight: 22,
        onDrag: this.onDrag.bind(this),
        onDragEnd: this.onDragEnd.bind(this)
    });
    // 添加到 activeSensorKeys 中的chart
    for (var i = 0; i < activeSensorKeys.length; i++) {
        var key = activeSensorKeys[i].key;
        if (key === 'temperaturey') {
            activeSensorKeys[i].chart = chart;
        }
    }
}
// </editor-fold>

// <editor-fold desc="湿度数据">

Chart.prototype.initHumidityHTML = function () {
    $('#chartContainer').append($(this.chartTmpl.humidity));

    var dataDependency = this.dependency.get('data');
    var sensorList = dataDependency.getSensorList();

    var $humiditySelect = $('#humiditySelect');

    for (var i = 0; i < sensorList.length; i++) {
        var sensor = sensorList[i];
        if (sensor.key === 'humidity') {
            for (var j = 0; j < sensor.ids.length; j++) {
                var index = sensor.ids[j] - 0x25;
                $humiditySelect.append($('<option value="' + index + '">' + index + '#</option>'));
            }
        }
    }
    $humiditySelect.on('change', function () {
        dataDependency.setHumiditySensorNo($(this).val());
    });
    dataDependency.setHumiditySensorNo(1);
}

Chart.prototype.getHumidity = function () {
    var dataDependency = this.dependency.get('data');
    var vehicleId = dataDependency.getVid();
    var startTime = dataDependency.getStartTime();
    var endTime = dataDependency.getEndTime();
    var validSensorFlag = dataDependency.getValidSensorFlag();
    var validClickedIndex = dataDependency.getValidClickedIndex();
    var sensorFlag = validSensorFlag && (validClickedIndex !== undefined && validClickedIndex !== null)
        ? validSensorFlag[validSensorFlag] : undefined;
    var sensorNo = dataDependency.getHumiditySensorNo();

    json_ajax('POST', '/clbs/v/monitoring/getHumidityChartData', 'json', true, {
        vehicleId: vehicleId,
        startTime: startTime,
        endTime: endTime,
        sensorFlag: sensorFlag,
        sensorNo: sensorNo
    }, function (data) {
        if (data.success) {
            dataDependency.setHumidity(data.obj ? data.obj : {})
        } else {
            layer.msg(data.exceptionDetailMsg || data.msg);
        }
    })
}

Chart.prototype.humidityChange = function () {
    var dataDependency = this.dependency.get('data');
    var activeSensorKeys = dataDependency.getActiveSensorKeys();
    var data = dataDependency.getHumidity();
    var playIndex = dataDependency.getPlayIndex('chart');

    var humidity = data.sensorDataList;
    var dataSize = humidity.length;

    var highestHumi = null;
    var lowestHumi = null;
    var series;

    if (humidity) {
        var line = [];
        if (data.highHumidityThreshold !== null) {
            line.push({
                value: data.highHumidityThreshold,
                color: 'red',
            })
        }
        if (data.lowHumidityThreshold !== null) {
            line.push({
                value: data.lowHumidityThreshold,
                color: 'red',
            })
        }

        var serieData = [];

        for (var i = 0; i < dataSize; i += 1) {
            var element = humidity[i];
            var value = element.humidity;

            var newElement = {
                index: i,
                date: new Date(element.time * 1000),
                value: value,
                supply: element.supply,
                color: value !== null ? '#962219' : 'transparent',
            };
            serieData.push(newElement);

            if (value !== null) {
                if (highestHumi === null) {
                    highestHumi = value;
                    lowestHumi = value;
                } else if (value >= highestHumi) {
                    highestHumi = value;
                } else {
                    lowestHumi = value;
                }
            }
        }
        series = [{
            data: serieData,
            color: 'skyblue',
            width: 1,
            label: TrackPlaybackUtil.getLocale('humidityText'),
            unit: '%',
            yValueFunc: function (rItem) {
                if (!rItem.yValue) {
                    return '---';
                }
                var yValue = rItem.yValue;
                var supply = yValue.supply;
                if (supply) {
                    return '无数据';
                }
                if (rItem.yValue.value === null) {
                    return '无传感器数据';
                }
                return rItem.text;
            },
            line: line,
            yMinValue: Math.min(0, data.minHumidity),
            yMaxValue: Math.min(100, data.maxHumidity),
        }];

        highestHumi = highestHumi === null ? '--' : TrackPlaybackUtil.toFixed(highestHumi, 1, true);
        lowestHumi = lowestHumi === null ? '--' : TrackPlaybackUtil.toFixed(lowestHumi, 1, true);
    } else {
        series = null;
        highestHumi = '--';
        lowestHumi = '--';
    }

    $('#highestHumiText').html(data.maxHumidity == null ? '--' : data.maxHumidity);
    $('#lowestHumiText').html(data.minHumidity == null ? '--' : data.minHumidity);

    var chart = new LineChart('#humidityChart', {
        series: series,
        playIndex: playIndex,
        snap: true,
        rectHeight: 22,
        onDrag: this.onDrag.bind(this),
        onDragEnd: this.onDragEnd.bind(this)
    });
    // 添加到 activeSensorKeys 中的chart
    for (var i = 0; i < activeSensorKeys.length; i++) {
        var key = activeSensorKeys[i].key;
        if (key === 'humidity') {
            activeSensorKeys[i].chart = chart;
        }
    }
}
// </editor-fold>

// <editor-fold desc="工时数据">

Chart.prototype.initWorkHourHTML = function () {
    $('#chartContainer').append($(this.chartTmpl.workHour));

    var dataDependency = this.dependency.get('data');
    var sensorList = dataDependency.getSensorList();

    var $workHourSelect = $('#workHourSelect');

    for (var i = 0; i < sensorList.length; i++) {
        var sensor = sensorList[i];
        if (sensor.key === 'workHour') {
            for (var j = 0; j < sensor.ids.length; j++) {
                var index = sensor.ids[j] - 0x7f;
                $workHourSelect.append($('<option value="' + index + '">' + index + '#</option>'));
            }
        }
    }
    $workHourSelect.on('change', function () {
        dataDependency.setWorkHourSensorNo($(this).val());
    });
    dataDependency.setWorkHourSensorNo(1);
}

Chart.prototype.getWorkHour = function () {
    var dataDependency = this.dependency.get('data');
    var vehicleId = dataDependency.getVid();
    var startTime = dataDependency.getStartTime();
    var endTime = dataDependency.getEndTime();
    var validSensorFlag = dataDependency.getValidSensorFlag();
    var validClickedIndex = dataDependency.getValidClickedIndex();
    var sensorFlag = validSensorFlag && (validClickedIndex !== undefined && validClickedIndex !== null)
        ? validSensorFlag[validSensorFlag] : undefined;
    var sensorNo = dataDependency.getWorkHourSensorNo();

    json_ajax('POST', '/clbs/v/monitoring/getWorkHourChartData', 'json', true, {
        vehicleId: vehicleId,
        startTime: startTime,
        endTime: endTime,
        sensorFlag: sensorFlag,
        sensorNo: sensorNo
    }, function (data) {
        if (data.success) {
            dataDependency.setWorkHour(data.obj ? data.obj : {})
        } else {
            layer.msg(data.exceptionDetailMsg || data.msg);
        }
    })
}

Chart.prototype.workHourChange = function () {
    var dataDependency = this.dependency.get('data');
    var activeSensorKeys = dataDependency.getActiveSensorKeys();
    var mileageData = dataDependency.getMileSpeed();
    var data = dataDependency.getWorkHour();
    var playIndex = dataDependency.getPlayIndex('chart');

    var workHourInfo = data.workHourInfo;
    var dataSize = workHourInfo.length;

    var workInspectionMethod = data.workInspectionMethod;

    var series = null;
    var workDuration = '--';
    var standByDuration = '--';
    var haltDuration = '--';

    if (workHourInfo) {
        workDuration = data.workDuration;
        standByDuration = data.standByDuration;
        haltDuration = data.haltDuration;

        var color = {
            0: '#5a5a5a',
            1: '#606dcf',
            2: '#deb741',
            null: 'transparent',
        };
        var getColor = function (x) {
            // 状态 1:正常数据; 2:无传感器数据; 3:无数据;
            if (x.type === 1) {
                // 工作状态   0:停机 1:工作 2:待机
                return color[x.workingPosition];
            }
            return 'transparent';
        };
        var getValue = function (x) {
            var value = x.checkData;
            if (x.type == 2 || value === undefined || value === null) {
                return null;
            }
            return parseFloat(value);
        };

        var maxValue = 0;
        var serieData = [];
        for (var i = 0; i < dataSize; i += 1) {
            var x = workHourInfo[i];
            var value = getValue(x);
            serieData.push({
                index: i,
                date: new Date(x.time * 1000),
                value: value,
                supply: x.supply,
                color: getColor(x),
                validate: x.type, // 1有效,2显示横线,3显示空
            });
            if (value > maxValue) {
                maxValue = value;
            }
        }

        if (workInspectionMethod === 1) {
            // 电压比较式
            series = [{
                data: serieData,
                width: 1,
                label: TrackPlaybackUtil.getLocale('voltage'),
                unit: 'V',
                autoConnectPartition: 'AFTER',
                yValueFunc: function (rItem) {
                    if (!rItem.yValue) {
                        return '---';
                    }
                    if (rItem.yValue.supply) {
                        return '无数据';
                    }
                    if (rItem.yValue.value === null) {
                        return '无传感器数据';
                    }
                    return rItem.text;
                },
                yMinValue: 0,
                yMaxValue: maxValue + 10,
            }];
            var thresholdValue = data.thresholdValue;

            if (thresholdValue !== null && thresholdValue !== undefined) {
                series[0].line = [{
                    value: parseFloat(thresholdValue),
                    color: 'darkgray',
                }];
                series[0].yMaxValue = Math.max(series[0].yMaxValue, parseFloat(thresholdValue) + 10);
            }
        } else if (workInspectionMethod === 2) {
            // 油耗阈值式
            series = [{
                data: serieData,
                width: 1,
                label: TrackPlaybackUtil.getLocale('secondFlow'),
                unit: 'L/h',
                autoConnectPartition: 'AFTER',
                yValueFunc: function (rItem) {
                    if (!rItem.yValue) {
                        return '---';
                    }
                    if (rItem.yValue.supply) {
                        return '无数据';
                    }
                    if (rItem.yValue.value === null) {
                        return '无传感器数据';
                    }
                    return rItem.text;
                },
                yMinValue: 0,
                yMaxValue: maxValue + 10,
            }];
            var thresholdValue = data.thresholdValue;

            if (thresholdValue !== null && thresholdValue !== undefined) {
                series[0].line = [{
                    value: parseFloat(thresholdValue),
                    color: 'darkgray',
                }];
                series[0].yMaxValue = Math.max(series[0].yMaxValue, parseFloat(thresholdValue) + 10);
            }
        } else {
            // 油耗波动式
            series = [
                {
                    data: serieData,
                    width: 1,
                    label: TrackPlaybackUtil.getLocale('secondFlow'),
                    unit: 'L/h',
                    autoConnectPartition: 'AFTER',
                    yMinValue: 0,
                    yMaxValue: maxValue + 10,
                    yValueFunc: function (rItem) {
                        if (!rItem.yValue) {
                            return '---';
                        }
                        if (rItem.yValue.supply) {
                            return '无数据';
                        }
                        if (rItem.yValue.value === null) {
                            return '无传感器数据';
                        }
                        return rItem.text;
                    },
                },
                {
                    data: mileageData.map(function (x, i) {
                        var value = x.speed;
                        return {
                            index: i,
                            date: new Date(x.time * 1000),
                            value: value === null ? null : parseFloat(value),
                            color: value === null ? 'transparent' : '#a3d843',
                        };
                    }),
                    width: 1,
                    label: TrackPlaybackUtil.getLocale('speed'),
                    unit: 'km/h',
                    yMinValue: 0,
                    yMaxValue: 240,
                },
            ];
        }

        workDuration = TrackPlaybackUtil.getHTMLFromSecond(workDuration);
        standByDuration = TrackPlaybackUtil.getHTMLFromSecond(standByDuration);
        haltDuration = TrackPlaybackUtil.getHTMLFromSecond(haltDuration);
    }

    $('#validWorkHourText').html(workDuration);
    if (workInspectionMethod === 2 || workInspectionMethod === 3) {
        $('#waitWorkHourText').html(standByDuration);
    }
    $('#stopWorkHourText').html(haltDuration);

    var chart = new LineChart('#workHourChart', {
        series: series,
        playIndex: playIndex,
        snap: true,
        onDrag: this.onDrag.bind(this),
        onDragEnd: this.onDragEnd.bind(this)
    });
    // 添加到 activeSensorKeys 中的chart
    for (var i = 0; i < activeSensorKeys.length; i++) {
        var key = activeSensorKeys[i].key;
        if (key === 'workHour') {
            activeSensorKeys[i].chart = chart;
        }
    }
}
// </editor-fold>

// <editor-fold desc="正反转">
Chart.prototype.getReverse = function () {
    $('#chartContainer').append($(this.chartTmpl.reverse));

    var dataDependency = this.dependency.get('data');
    var vehicleId = dataDependency.getVid();
    var startTime = dataDependency.getStartTime();
    var endTime = dataDependency.getEndTime();
    var validSensorFlag = dataDependency.getValidSensorFlag();
    var validClickedIndex = dataDependency.getValidClickedIndex();
    var sensorFlag = validSensorFlag && (validClickedIndex !== undefined && validClickedIndex !== null)
        ? validSensorFlag[validSensorFlag] : undefined;

    json_ajax('POST', '/clbs/v/monitoring/getPositiveInversionDate', 'json', true, {
        monitorId: vehicleId,
        startTime: startTime,
        endTime: endTime,
        sensorFlag: sensorFlag
    }, function (data) {
        if (data.success) {
            dataDependency.setReverse(data.obj ? data.obj : [])
        } else {
            layer.msg(data.exceptionDetailMsg || data.msg);
        }
    })
}

Chart.prototype.reverseChange = function () {
    var dataDependency = this.dependency.get('data');
    var activeSensorKeys = dataDependency.getActiveSensorKeys();
    var data = dataDependency.getReverse();
    var playIndex = dataDependency.getPlayIndex('chart');
    var dataSize = data.length;

    var color = {1: '#2a5eee', 2: '#f9cc42', 3: '#e14877', null: 'transparent'};
    var totalUpTime = 0; // 正转时长，以秒为单位
    var totalDownTime = 0; // 反转时长，以秒为单位
    var upTimeLength = 0; // 以秒为单位
    var downTimeLength = 0; // 以秒为单位
    var prevType = null;
    var upStartTime = null;
    var downStartTime = null;
    var upTimeArr = [];
    var prevUpIndex = 0;
    var downTimeArr = [];
    var prevDownIndex = 0;
    var dataArr = [];
    var i = 0;

    for (; i < dataSize; i += 1) {
        var item = data[i];
        var newItem = {};
        var type = item.orientation; // 1正转，2反转
        var time = item.time;
        var rotationStatus = item.rotationStatus;// 1停转,2运行
        if (rotationStatus === '1') {
            type = 3;
        }
        if (type === null || type === 3 || i === dataSize - 1) {
            // 停止
            if (prevType === 1) {
                upTimeLength = data[(type === 1 ? i : i - 1)].time - upStartTime;
                totalUpTime += upTimeLength;
                upTimeArr.push({
                    startIndex: prevUpIndex,
                    endIndex: (type === 1 ? i : i - 1),
                    timeLength: upTimeLength,
                });
            } else if (prevType === 2) {
                downTimeLength = data[(type === 2 ? i : i - 1)].time - downStartTime;
                totalDownTime += downTimeLength;
                downTimeArr.push({
                    startIndex: prevDownIndex,
                    endIndex: (type === 2 ? i : i - 1),
                    timeLength: downTimeLength,
                });
            }
        } else if (type === 1) {
            // 正转
            if (prevType === 2 || prevType === null || prevType === 3) {
                upStartTime = time;
                prevUpIndex = i;
                if (prevType === 2) {
                    downTimeLength = data[i - 1].time - downStartTime;
                    totalDownTime += downTimeLength;
                    downTimeArr.push({
                        startIndex: prevDownIndex,
                        endIndex: i - 1,
                        timeLength: downTimeLength,
                    });
                }
            }
        } else if (type === 2) {
            // 反转
            if (prevType === 1 || prevType === null || prevType === 3) {
                downStartTime = time;
                prevDownIndex = i;
                if (prevType === 1) {
                    upTimeLength = data[i - 1].time - upStartTime;
                    totalUpTime += upTimeLength;
                    upTimeArr.push({
                        startIndex: prevUpIndex,
                        endIndex: i - 1,
                        timeLength: upTimeLength,
                    });
                }
            }
        }
        prevType = type;

        newItem.date = time === 0 ? null : new Date(time * 1000);
        newItem.value = 1;
        newItem.supply = item.supply,
            newItem.rotationStatus = rotationStatus;
        newItem.index = i;
        newItem.color = color[type];
        newItem.type = type;

        dataArr.push(newItem);
    }

    var series = [
        {
            data: dataArr,
            width: 3,
            label: TrackPlaybackUtil.getLocale('reverseText'),
            unit: 'h',
            upTimeArr: upTimeArr,
            downTimeArr: downTimeArr,
            autoConnectPartition: 'AFTER',
            yValueFunc: function (item, serie) {
                var yValue = item.yValue;
                if (!yValue) {
                    return '---';
                }
                var supply = yValue.supply;
                if (supply) {
                    return '无数据';
                }
                if (yValue.rotationStatus === null) {
                    return '无传感器数据';
                }

                var index = yValue.index;
                var type = yValue.type;
                var date = yValue.date;

                if (date === null) {
                    return TrackPlaybackUtil.getLocale('noData');
                }

                var upTimeArrInSerie = serie.upTimeArr;
                var downTimeArrInSerie = serie.downTimeArr;

                if (type === 1) {
                    // 正转
                    for (var ii = 0; ii < upTimeArrInSerie.length; ii += 1) {
                        var ele = upTimeArrInSerie[ii];
                        if (ele.startIndex <= index && index <= ele.endIndex) {
                            return TrackPlaybackUtil.getLocale('upRotate') + '\n' + TrackPlaybackUtil.getTextFromSecond(ele.timeLength);
                        }
                    }
                } else if (type === 2) {
                    for (var ii = 0; ii < downTimeArrInSerie.length; ii += 1) {
                        var ele = downTimeArrInSerie[ii];
                        if (ele.startIndex <= index && index <= ele.endIndex) {
                            return TrackPlaybackUtil.getLocale('downRotate') + '\n' + TrackPlaybackUtil.getTextFromSecond(ele.timeLength);
                        }
                    }
                } else if (type === 3) {
                    return TrackPlaybackUtil.getLocale('stopRorate');
                } else if (type === null) {
                    return '无传感器数据'
                }

                return '---';
            },
            yMinValue: 0,
            yMaxValue: 1.8,
        },
    ];

    $('#totalUpTimeText').html(TrackPlaybackUtil.getHTMLFromSecond(totalUpTime));
    $('#totalDownTimeText').html(TrackPlaybackUtil.getHTMLFromSecond(totalDownTime));

    var chart = new LineChart('#reverseChart', {
        series: series,
        playIndex: playIndex,
        snap: true,
        onDrag: this.onDrag.bind(this),
        onDragEnd: this.onDragEnd.bind(this)
    });
    // 添加到 activeSensorKeys 中的chart
    for (var i = 0; i < activeSensorKeys.length; i++) {
        var key = activeSensorKeys[i].key;
        if (key === 'reverse') {
            activeSensorKeys[i].chart = chart;
        }
    }
}
// </editor-fold>

// <editor-fold desc="IO数据">


Chart.prototype.getIoData = function () {
    $('#chartContainer').append($(this.chartTmpl.ioData));

    var dataDependency = this.dependency.get('data');
    var vehicleId = dataDependency.getVid();
    var startTime = dataDependency.getStartTime();
    var endTime = dataDependency.getEndTime();
    var validSensorFlag = dataDependency.getValidSensorFlag();
    var validClickedIndex = dataDependency.getValidClickedIndex();
    var sensorFlag = validSensorFlag && (validClickedIndex !== undefined && validClickedIndex !== null)
        ? validSensorFlag[validSensorFlag] : undefined;
    var sensorList = dataDependency.getSensorList();

    json_ajax('GET', '/clbs/v/monitoring/getSwitchData', 'json', true, {
        monitorId: vehicleId,
        startTime: startTime,
        endTime: endTime,
        sensorFlag: sensorFlag
    }, function (data) {
        if (data.success) {
            var $ioDataSelect = $('#ioDataSelect');
            for (var j = 0; j < data.obj.names.length; j++) {
                $ioDataSelect.append($('<option value="' + j + '">' + data.obj.names[j] + '</option>'));
            }
            $ioDataSelect.on('change', function () {
                dataDependency.setIoDataSensorNo($(this).val());
            });
            dataDependency.setIoData(data.obj ? data.obj : {});
            dataDependency.setIoDataSensorNo(0);
        } else {
            layer.msg(data.exceptionDetailMsg || data.msg);
        }
    })
}

Chart.prototype.ioDataChange = function () {
    var dataDependency = this.dependency.get('data');
    var activeSensorKeys = dataDependency.getActiveSensorKeys();
    var ioData = dataDependency.getIoData();
    var attachIndex = dataDependency.getIoDataSensorNo();
    var attachIndex = dataDependency.getIoDataSensorNo();
    var playIndex = dataDependency.getPlayIndex('chart');

    var upColor = '#709fa8';
    var downColor = '#000000';
    var alarmColor = '#f40505';

    // 开关报警的状态(2:低电平 1 高电平)
    var alarmStatus = ioData.alarmStatuses[attachIndex]; // 报警的值，如果data的值为此，则用红色表示

    var upText = ioData.IOStatus[attachIndex]['1'];
    var downText = ioData.IOStatus[attachIndex]['0'];

    var upColorItem = alarmStatus === '2' ? alarmColor : upColor;
    var downColorItem = alarmStatus === '1' ? alarmColor : downColor;

    var series;
    var totalUpTime = 0; // 开时长，以秒为单位
    var totalDownTime = 0; // 关时长，以秒为单位

    var getColor = function (type) {
        //  0-低电平 down  1-高电平 up
        if (type === null) {
            return 'transparent';
        }
        if (alarmStatus.length > 0 && type !== null) {
            if (type === 0 && alarmStatus === '2') { // 低电平
                return alarmColor;
            }
            if (type === 1 && alarmStatus === '1') { // 高电平
                return alarmColor;
            }
        }
        if (type === 0) {
            return downColor;
        }
        return upColor;
    };
    var upTimeLength = 0; // 以秒为单位
    var downTimeLength = 0; // 以秒为单位
    var prevType = null;
    var upStartTime = null;
    var downStartTime = null;
    var upTimeArr = [];
    var prevUpIndex = 0;
    var downTimeArr = [];
    var prevDownIndex = 0;
    var dataArr = [];

    if (!TrackPlaybackUtil.isEmpty(ioData) && ioData.data) {
        var data = ioData.data;
        var dataSize = ioData.data.length;

        var i = 0;
        for (; i < dataSize; i += 1) {
            var item = data[i];
            var newItem = {};
            var type = item.statuses[attachIndex]; // 开关状态 0-低电平 1-高电平 2-IO异常 null-无数据
            if (type === 2) {
                type = null;
            }
            var time = item.time;

            if (type === null || i === dataSize - 1) {
                // 空数据
                if (prevType === 1) {
                    upTimeLength = data[(type === 1 ? i : i - 1)].time - upStartTime;
                    totalUpTime += upTimeLength;
                    upTimeArr.push({
                        startIndex: prevUpIndex,
                        endIndex: (type === 1 ? i : i - 1),
                        timeLength: upTimeLength,
                    });
                } else if (prevType === 0) {
                    downTimeLength = data[(type === 0 ? i : i - 1)].time - downStartTime;
                    totalDownTime += downTimeLength;
                    downTimeArr.push({
                        startIndex: prevDownIndex,
                        endIndex: (type === 0 ? i : i - 1),
                        timeLength: downTimeLength,
                    });
                }
            } else if (type === 1) {
                // 高电平 up
                if (prevType === 0 || prevType === null) {
                    upStartTime = time;
                    prevUpIndex = i;
                    if (prevType === 0) {
                        downTimeLength = data[i - 1].time - downStartTime;
                        totalDownTime += downTimeLength;
                        downTimeArr.push({
                            startIndex: prevDownIndex,
                            endIndex: i - 1,
                            timeLength: downTimeLength,
                        });
                    }
                }
            } else if (type === 0) {
                // 低电平 down
                if (prevType === 1 || prevType === null) {
                    downStartTime = time;
                    prevDownIndex = i;
                    if (prevType === 1) {
                        upTimeLength = data[i - 1].time - upStartTime;
                        totalUpTime += upTimeLength;
                        upTimeArr.push({
                            startIndex: prevUpIndex,
                            endIndex: i - 1,
                            timeLength: upTimeLength,
                        });
                    }
                }
            }
            prevType = type;

            newItem.date = time === 0 ? null : new Date(time * 1000);
            newItem.value = 1;
            newItem.supply = item.supply;
            newItem.index = i;
            newItem.color = getColor(type);
            newItem.type = type;

            dataArr.push(newItem);
        }
        totalUpTime = TrackPlaybackUtil.getHTMLFromSecond(totalUpTime, 1, true);
        totalDownTime = TrackPlaybackUtil.getHTMLFromSecond(totalDownTime, 1, true);
    } else {
        series = null;
        totalUpTime = '--';
        totalDownTime = '--';
    }


    series = [
        {
            data: dataArr,
            width: 3,
            label: TrackPlaybackUtil.getLocale('io'),
            unit: 'h',
            upTimeArr: upTimeArr,
            downTimeArr: downTimeArr,
            autoConnectPartition: 'AFTER',
            yValueFunc: function (item, serie) {
                var yValue = item.yValue;
                if (!yValue) {
                    return '---';
                }
                var index = yValue.index;
                var type = yValue.type; // 1 up ; 0 down
                var date = yValue.date;

                var supply = yValue.supply;
                if (supply) {
                    return '无数据';
                }
                if (type === null) {
                    return '无传感器数据';
                }

                var upTimeArr = serie.upTimeArr;
                var downTimeArr = serie.downTimeArr;

                if (date === null) {
                    return TrackPlaybackUtil.getLocale('noData');
                }

                if (type == 1) {
                    // 1 高电平 up
                    for (var j = 0; j < upTimeArr.length; j += 1) {
                        var ele = upTimeArr[j];
                        if (ele.startIndex <= index && index <= ele.endIndex) {
                            return upText + '\n' + TrackPlaybackUtil.getTextFromSecond(ele.timeLength);
                        }
                    }
                }
                for (var ii = 0; ii < downTimeArr.length; ii += 1) {
                    var ele = downTimeArr[ii];
                    if (ele.startIndex <= index && index <= ele.endIndex) {
                        return downText + '\n' + TrackPlaybackUtil.getTextFromSecond(ele.timeLength);
                    }
                }
                return '---';
            },
        },
    ];

    $('#ioUpText').html(upText);
    $('#ioDownText').html(downText);
    $('#ioTotalUpText').html(totalUpTime);
    $('#ioTotalDownText').html(totalDownTime);
    $('#ioUpRect').css('backgroundColor', upColor);
    $('#ioDownRect').css('backgroundColor', downColor);

    var chart = new LineChart('#ioDataChart', {
        series: series,
        playIndex: playIndex,
        snap: true,
        rectColorFunc: function (xValue) {
            var color = xValue.color;
            if (color === 'transparent') {
                return null;
            }
            return color;
        },
        // rectHeight:22,
        onDrag: this.onDrag.bind(this),
        onDragEnd: this.onDragEnd.bind(this)
    });
    // 添加到 activeSensorKeys 中的chart
    for (var i = 0; i < activeSensorKeys.length; i++) {
        var key = activeSensorKeys[i].key;
        if (key === 'ioData') {
            activeSensorKeys[i].chart = chart;
        }
    }
}
// </editor-fold>

// <editor-fold desc="载重数据">

Chart.prototype.initWeightHTML = function () {
    $('#chartContainer').append($(this.chartTmpl.weight));

    var dataDependency = this.dependency.get('data');
    var sensorList = dataDependency.getSensorList();

    var $weightSelect = $('#weightSelect');

    var indexArr = [];
    for (var i = 0; i < sensorList.length; i++) {
        var sensor = sensorList[i];
        if (sensor.key === 'weight') {
            for (var j = 0; j < sensor.ids.length; j++) {
                var index = sensor.ids[j] - 0x6f;
                indexArr.push(index);
                $weightSelect.append($('<option value="' + index + '">' + index + '#</option>'));
            }
        }
    }
    $weightSelect.on('change', function () {
        dataDependency.setWeightSensorNo($(this).val());
    });
    if (indexArr.length > 0) {
        dataDependency.setWeightSensorNo(indexArr[0]);
    }
}

Chart.prototype.getWeight = function () {
    var dataDependency = this.dependency.get('data');
    var vehicleId = dataDependency.getVid();
    var startTime = dataDependency.getStartTime();
    var endTime = dataDependency.getEndTime();
    var validSensorFlag = dataDependency.getValidSensorFlag();
    var validClickedIndex = dataDependency.getValidClickedIndex();
    var sensorFlag = validSensorFlag && (validClickedIndex !== undefined && validClickedIndex !== null)
        ? validSensorFlag[validSensorFlag] : undefined;
    var sensorNo = dataDependency.getWeightSensorNo();

    json_ajax('POST', '/clbs/v/monitoring/getLoadWeight', 'json', true, {
        vehicleId: vehicleId,
        startTime: startTime,
        endTime: endTime,
        sensorFlag: sensorFlag,
        sensorNo: sensorNo
    }, function (data) {
        if (data.success) {
            dataDependency.setWeight(data.obj ? data.obj : {})
        } else {
            layer.msg(data.exceptionDetailMsg || data.msg);
        }
    })
}

Chart.prototype.weightChange = function () {
    var dataDependency = this.dependency.get('data');
    var activeSensorKeys = dataDependency.getActiveSensorKeys();
    var data = dataDependency.getWeight();
    var playIndex = dataDependency.getPlayIndex('chart');

    var weight = data.sensorDataList;

    var padEndData = weight.concat([{
        x: Math.random()
    }]);

    var getColor = function (type) {
        if (type === null) {
            return 'lightgray';
        }
        if (type === '1') { // 空载
            return '#cdcdcd';
        } else if (type === '6') { // 轻载
            return '#bddbff';
        } else if (type === '2') { // 满载
            return '#8b84eb';
        } else if (type === '7') { // 重载
            return '#95b6f2';
        } else if (type === '3') { // 超载
            return '#f8a023';
        }
        return '#ffffff';
    };

    var padEndDataLength = padEndData.length;
    var dataArr = [];

    var emptyTime = 0; // 以秒为单位
    var fullTime = 0;
    var overTime = 0;
    var lightTime = 0;
    var heavyTime = 0;

    var maxValue = data.maxLoadWeight;
    if (data.overLoadValue > maxValue) {
        maxValue = data.overLoadValue;
    }
    var unit = maxValue > 1000.0 ? 'T' : 'kg';

    if (padEndDataLength > 0) {
        var typeDict = {};
        var prevType = padEndData[0].status;
        var prevIndex = 0;
        var notNullIndex = 0;

        for (var i = 0; i < padEndDataLength; i += 1) {
            var element = padEndData[i];
            var currentType = element.status; // 	载重状态 “1”：空载， “2”：满载， “3”：超载 ，“4”：装载 ，“5”：卸载，“6”：轻载 ，“7”：重载
            var time = element.time;

            if (i < padEndDataLength - 1) {
                var newItem = {};
                newItem.date = new Date(time * 1000);
                newItem.value = unit === 'T' ? TrackPlaybackUtil.toFixed(element.weight / 1000.0, 1, true) : element.weight;
                newItem.supply = element.supply;
                newItem.index = i;
                newItem.color = getColor(currentType);
                newItem.type = currentType;

                dataArr.push(newItem);
            }

            // 1.补点数据不参与计算
            // 2.两个点就算是相同状态，但时间差大于5分钟，也属于两个状态段
            // 3.如果两个状态之间时间差大于5分钟，则按状态段首尾点计算
            // 4.不然按下一个状态段第一个点 - 上一个状态段最后一点计算

            if (currentType === null) {
                continue;
            }

            var timeDiff = padEndData[i].time - padEndData[notNullIndex].time;


            if (currentType !== prevType || timeDiff > 300) {
                var item = typeDict[prevType];
                var endIndex = notNullIndex;
                // 如果两个状态之间时间差大于5分钟，则按状态段首尾点计算
                // 不然按下一个状态段第一个点 - 上一个状态段最后一点计算
                var timeLength = padEndData[endIndex].time - padEndData[prevIndex].time;
                if (timeDiff <= 300) {
                    endIndex = i;
                    timeLength = padEndData[endIndex].time - padEndData[prevIndex].time;
                }
                var segment = {
                    startIndex: prevIndex,
                    endIndex: endIndex,
                    timeLength: timeLength,
                    startTime: padEndData[prevIndex].time,
                    endTime: padEndData[endIndex].time,
                };
                if (item) {
                    item.occurrence += 1;
                    item.totalTimeLength += timeLength;
                    item.segment.push(segment);
                } else {
                    typeDict[prevType] = {
                        type: prevType,
                        occurrence: 1,
                        totalTimeLength: timeLength,
                        segment: [segment],
                    };
                }
                prevType = currentType;
                prevIndex = i;
            }
            notNullIndex = i;

        }

        if (typeDict['1']) {
            emptyTime = typeDict['1'].totalTimeLength;
        }
        if (typeDict['2']) {
            fullTime = typeDict['2'].totalTimeLength;
        }
        if (typeDict['3']) {
            overTime = typeDict['3'].totalTimeLength;
        }
        if (typeDict['6']) {
            lightTime = typeDict['6'].totalTimeLength;
        }
        if (typeDict['7']) {
            heavyTime = typeDict['7'].totalTimeLength;
        }
    }

    var line = [];
    var maxPrepare = [];
    if (data.noLoadValue !== null && data.noLoadValue !== undefined) {
        var v = unit === 'T' ? TrackPlaybackUtil.toFixed(data.noLoadValue / 1000.0, 1, true) : data.noLoadValue;
        line.push({
            value: parseFloat(v),
            color: 'red',
        });
        maxPrepare.push(v);
    }
    if (data.lightLoadValue !== null && data.lightLoadValue !== undefined) {
        var v = unit === 'T' ? TrackPlaybackUtil.toFixed(data.lightLoadValue / 1000.0, 1, true) : data.lightLoadValue;
        line.push({
            value: parseFloat(v),
            color: 'red',
        });
        maxPrepare.push(v);
    }
    if (data.fullLoadValue !== null && data.fullLoadValue !== undefined) {
        var v = unit === 'T' ? TrackPlaybackUtil.toFixed(data.fullLoadValue / 1000.0, 1, true) : data.fullLoadValue;
        line.push({
            value: parseFloat(v),
            color: 'red',
        });
        maxPrepare.push(v);
    }
    if (data.overLoadValue !== null && data.overLoadValue !== undefined) {
        var v = unit === 'T' ? TrackPlaybackUtil.toFixed(data.overLoadValue / 1000.0, 1, true) : data.overLoadValue;
        line.push({
            value: parseFloat(v),
            color: 'red',
        });
        maxPrepare.push(v);
    }

    var max = unit === 'T' ? TrackPlaybackUtil.toFixed(data.maxLoadWeight / 1000.0, 1, true) : data.maxLoadWeight;
    for (var i = 0; i < maxPrepare.length; i += 1) {
        var element = maxPrepare[i];
        if (element > max) {
            max = element;
        }
    }

    var series = [{
        data: dataArr,
        width: 1,
        label: '重量(' + unit + ')',
        unit: unit,
        autoConnectPartition: 'BEFORE',
        yValueFunc: function (rItem) {
            if (!rItem.yValue) {
                return '---';
            }
            var yValue = rItem.yValue;
            var supply = yValue.supply;
            if (supply) {
                return '无数据';
            }
            if (rItem.yValue.value === null) {
                return '无传感器数据';
            }
            return rItem.text;
        },
        line: line,
        yMinValue: 0,
        yMaxValue: max,
    }];

    var chart = new LineChart('#weightChart', {
        series: series,
        playIndex: playIndex,
        snap: true,
        rectHeight: 22,
        onDrag: this.onDrag.bind(this),
        onDragEnd: this.onDragEnd.bind(this)
    });
    $('#emptyText').html(emptyTime == null ? '--' : TrackPlaybackUtil.getHTMLFromSecond(emptyTime));
    $('#lightText').html(lightTime == null ? '--' : TrackPlaybackUtil.getHTMLFromSecond(lightTime));
    $('#heavyText').html(heavyTime == null ? '--' : TrackPlaybackUtil.getHTMLFromSecond(heavyTime));
    $('#fullText').html(fullTime == null ? '--' : TrackPlaybackUtil.getHTMLFromSecond(fullTime));
    $('#overText').html(overTime == null ? '--' : TrackPlaybackUtil.getHTMLFromSecond(overTime));

    // 添加到 activeSensorKeys 中的chart
    for (var i = 0; i < activeSensorKeys.length; i++) {
        var key = activeSensorKeys[i].key;
        if (key === 'weight') {
            activeSensorKeys[i].chart = chart;
        }
    }
}
// </editor-fold>

// <editor-fold desc="胎压数据">

Chart.prototype.initTireHTML = function () {
    $('#chartContainer').append($(this.chartTmpl.tire));

    var dataDependency = this.dependency.get('data');
    var sensorList = dataDependency.getTireNumList();

    var $tireSelect = $('#tireSelect');

    for (var i = 0; i < sensorList.length; i++) {
        var index = sensorList[i];
        $tireSelect.append($('<option value="' + index + '">' + index + '#</option>'));
    }
    $tireSelect.on('change', function () {
        dataDependency.setTireNum($(this).val());
    });
    dataDependency.setTireNum(1);
}

Chart.prototype.getTire = function () {
    var dataDependency = this.dependency.get('data');
    var vehicleId = dataDependency.getVid();
    var startTime = dataDependency.getStartTime();
    var endTime = dataDependency.getEndTime();
    var validSensorFlag = dataDependency.getValidSensorFlag();
    var validClickedIndex = dataDependency.getValidClickedIndex();
    var sensorFlag = validSensorFlag && (validClickedIndex !== undefined && validClickedIndex !== null)
        ? validSensorFlag[validSensorFlag] : undefined;
    var sensorNo = dataDependency.getTireNum();

    json_ajax('POST', '/clbs/v/monitoring/getTirePressureData', 'json', true, {
        vehicleId: vehicleId,
        startTime: startTime,
        endTime: endTime,
        sensorFlag: sensorFlag,
        tireNum: sensorNo
    }, function (data) {
        if (data.success) {
            dataDependency.setTire(data.obj ? data.obj : {})
        } else {
            layer.msg(data.exceptionDetailMsg || data.msg);
        }
    })
}

Chart.prototype.tireChange = function () {
    var dataDependency = this.dependency.get('data');
    var activeSensorKeys = dataDependency.getActiveSensorKeys();
    var data = dataDependency.getTire();
    var playIndex = dataDependency.getPlayIndex('chart');

    var tire = data.sensorDataList;
    var dataSize = tire.length;

    var highestTire = null;
    var lowestTire = null;
    var series;

    if (tire) {
        var line = [];
        if (data.heighPressure) {
            line.push({
                value: parseFloat(data.heighPressure),
                color: 'red',
            })
        }
        if (data.lowPressure) {
            line.push({
                value: parseFloat(data.lowPressure),
                color: 'red',
            });
        }

        var serieData = [];


        for (var i = 0; i < dataSize; i += 1) {
            var element = tire[i];
            var value = element.pressure;

            var newElement = {
                index: i,
                date: new Date(element.time * 1000),
                value: value,
                supply: element.supply,
                color: value !== null ? '#5fcc97' : 'transparent',
            };
            serieData.push(newElement);

        }

        var max = data.maxTirePressure;
        if (data.heighPressure > max) {
            max = data.heighPressure;
        }
        if (max === null || max === undefined) {
            max = 100;
        }

        series = [{
            data: serieData,
            width: 1,
            label: '胎压(bar)',
            unit: 'bar',
            yValueFunc: function (rItem) {
                if (!rItem.yValue) {
                    return '---';
                }
                var yValue = rItem.yValue;
                var supply = yValue.supply;
                if (supply) {
                    return '无数据';
                }
                if (rItem.yValue.value === null) {
                    return '无传感器数据';
                }
                return rItem.text;
            },
            line: line,
            yMinValue: 0,
            yMaxValue: max,
        }];
        highestTire = data.maxTirePressure === null ? '--' : TrackPlaybackUtil.toFixed(data.maxTirePressure, 1, true);
        lowestTire = data.minTirePressure === null ? '--' : TrackPlaybackUtil.toFixed(data.minTirePressure, 1, true);
    } else {
        series = null;
        highestTire = '--';
        lowestTire = '--';
    }

    $('#highestTireText').html(highestTire);
    $('#lowestTireText').html(lowestTire);

    var chart = new LineChart('#tireChart', {
        series: series,
        playIndex: playIndex,
        snap: true,
        rectHeight: 22,
        onDrag: this.onDrag.bind(this),
        onDragEnd: this.onDragEnd.bind(this)
    });
    // 添加到 activeSensorKeys 中的chart
    for (var i = 0; i < activeSensorKeys.length; i++) {
        var key = activeSensorKeys[i].key;
        if (key === 'tire') {
            activeSensorKeys[i].chart = chart;
        }
    }
}
// </editor-fold>