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

Math.formatFloat = function (f, digit) {
    var m = Math.pow(10, digit);
    return parseInt(f * m, 10) / m;
};

$(function () {
    window.trackPlayback = {
        dragBar: undefined,
        progressBar: undefined,
        calendar: undefined,
        monitorTree: undefined,
        areaMonitorTree: undefined,
        fixedTimeArea: undefined,
        fenceTree: undefined,
        map: undefined,
        data: undefined,
        flags: {
            isFlag: false,// 不知道这个变量的具体意义，但calender中有几个地方用到了
            bFlag: true,
            bFlag1: true,
            firstFlag: true,
            worldType: '',
            objType: '',
            changeFlag: false,
        },
        init: function () {
            var dependency = new Dependency({
                flags: trackPlayback.flags
            });

            trackPlayback.data = new Data();
            dependency.set('data', trackPlayback.data);

            var rightPartHeight = $('#rightPart').height() - 50;
            trackPlayback.dragBar = new DragBar('#tableMapDrag', {
                min: -rightPartHeight,
                max: 0,
                onDragEnd: trackPlayback.onDragBarDragEnd
            });
            dependency.set('dragBar', trackPlayback.dragBar);

            trackPlayback.progressBar = new ProgressBar('#progressLine', {
                onDrag: trackPlayback.onProgressBarDrag,
                onDragEnd: trackPlayback.onProgressBarDragEnd
            });
            dependency.set('progressBar', trackPlayback.progressBar);

            trackPlayback.calendar = new Calendar('.calendar3', null, dependency);
            dependency.set('calendar', trackPlayback.calendar);

            trackPlayback.monitorTree = new MonitorTree('#treeDemoTrackPlayback', null, dependency);
            dependency.set('monitorTree', trackPlayback.monitorTree);

            trackPlayback.areaMonitorTree = new AreaMonitorTree('#areaTreeDemo', null, dependency);
            dependency.set('areaMonitorTree', trackPlayback.areaMonitorTree);

            trackPlayback.map = new TrackMap('mapContainer', null, dependency);
            dependency.set('map', trackPlayback.map);

            trackPlayback.fixedTimeArea = new FixedTimeArea(null, dependency);
            dependency.set('fixedTimeArea', trackPlayback.fixedTimeArea);

            trackPlayback.fenceTree = new FenceTree(null, dependency);
            dependency.set('fenceTree', trackPlayback.fenceTree);

            trackPlayback.table = new Table(null, dependency);
            dependency.set('table', trackPlayback.table);

            trackPlayback.chart = new Chart(null, dependency);
            dependency.set('chart', trackPlayback.chart);

            //获取url参数
            var vid = trackPlayback.getAddressUrl("vid");
            var pid = trackPlayback.getAddressUrl("pid");
            //判断参数不为空 防止发生错误
            if (vid !== undefined && vid !== null && vid.length > 0) {
                // trackPlayback.monitorTree.crrentSubV.push(vid);
                trackPlayback.data.setInitVid(vid);
                trackPlayback.data.setPid(pid);
                trackPlayback.monitorTree.search(vid, 'monitorId');
            } else {
                trackPlayback.monitorTree.init();
            }
            trackPlayback.bindDataEvent();
            trackPlayback.initLayout();
            trackPlayback.videoPlayEvent();
            trackPlayback.exportEvent();
            // trackPlayback.handleNodeClcik(dependency);
        },
        //导出
        exportEvent: function () {
            $('#tableDataExport').on('click', trackPlayback.tableDataExport);
        },
        tableDataExport: function () {
            var vehicleId = trackPlayback.data.getVid();
            var isEmpty = trackPlayback.table.isTableEmpty();
            var tab = trackPlayback.data.getTableTabIndex() - 1;
            if (!vehicleId) {
                layer.msg('请选择监控对象');
                return;
            }
            if (isEmpty) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if (tab == 1) {//OBD数据直接导出
                trackPlayback.exportFunc();
            } else {
                trackPlayback.showExportModal();
                //事件
                $('input[name="exportAddress"]').on('change', trackPlayback.isShowAddress);
                $('#exportBtn').on('click', trackPlayback.exportFunc);
            }
        },
        showExportModal: function () {
            var flag = $('input[name="videoSet"]:checked').val();
            var tab = trackPlayback.data.getTableTabIndex() - 1;
            var title = '';
            if (tab == 0) {
                title = flag == 'all' ? '导出全部数据' : '导出行驶数据';
            } else if (tab == 4) {
                title = "导出报警数据";
            } else if (tab == 3) {
                title = "导出停止段数据";
            } else if (tab == 2) {
                title = "导出行驶段数据";
            }
            $('#exportTitle').text(title);
            $('#noExportPosition').prop('checked', true);
            $('#yesExportPosition').prop('checked', false);
            $('#modalTips').hide();
            $('#exportModal').modal('show');
        },
        isShowAddress: function () {
            var value = $(this).val();
            var tips = $('#modalTips');

            if (value == '0') {
                tips.slideUp();
            } else {
                tips.slideDown();
            }
        },
        exportFunc: function () {
            var vehicleId = trackPlayback.data.getVid();
            var locationType = trackPlayback.data.getShowLocation();
            var locationTypeNum = locationType ? 1 : 0;
            var tab = trackPlayback.data.getTableTabIndex() - 1;
            var mark = '';
            var url = '';
            switch (tab) {
                case 1://obd数据
                    mark = 'TRACKPLAY_OBD_LIST';
                    break;
                case 2://行驶段数据
                    mark = 'TRACKPLAY_RUN';
                    break;
                case 3://停止段数据
                    mark = 'TRACKPLAY_STOP';
                    break;
                case 4://报警数据
                    mark = 'TRACKPLAY_ALARM';
                    break;
                default://位置数据
                    mark = 'TRACKPLAY_DATA';
                    break;
            }
            if (tab == 1) {//OBD数据导出
                url = '/clbs/v/monitoring/exportTrackPlay?tab=' + tab
                    + '&mark=' + mark + '&vehicleId=' + vehicleId + '&isStationEnabled=' + locationTypeNum;
            } else {
                var flag = $('input[name="videoSet"]:checked').val();
                var isExportLocation = $('input[name="exportAddress"]:checked').val();
                // 这是因为接口设计的顺序和页面显示的顺序不一致
                var realTab = tab;
                if (tab === 3) {
                    realTab = 3;
                } else if (tab === 4) {
                    realTab = 2;
                } else if (tab === 2) {
                    realTab = 4;
                }
                tab = realTab;
                url = '/clbs/v/monitoring/exportTrackPlay?isExportLocation=' + isExportLocation
                    + '&flag=' + (flag === 'all' ? 0 : 1)
                    + '&tab=' + tab
                    + '&mark=' + mark
                    + '&vehicleId=' + vehicleId
                    + '&isStationEnabled=' + locationTypeNum;
            }
            $('#exportModal').modal('hide');
            console.log(url)
            window.location.href = url;
        },
        //播放设置
        videoPlayEvent: function () {
            $('#paly-more').on('click', trackPlayback.playMore);
            $('.speed-control').on('click', trackPlayback.playSpeed);
        },
        playMore: function () {
            var checkbox = $('.checkbox-control');
            checkbox.toggleClass('active');
        },
        playSpeed: function () {
            var numDom = $(this).find('#speed-num');
            var num = parseFloat(numDom.text());
            num = num == 4 ? 0.5 : num * 2;
            numDom.text(num);
            var speed = 50;
            switch (num) {
                case 0.5:
                    speed = 50000;
                    break;
                case 1:
                    speed = 5000;
                    break;
                case 2:
                    speed = 2000;
                    break;
                case 4:
                    speed = 500;
                    break;
                default:
                    break;
            }
            trackPlayback.data.setSpeed(speed);
        },
        bindDataEvent: function () {
            // 树和日期选择
            trackPlayback.data.on('activeTreeNodeChange', trackPlayback.activeTreeNodeChange);
            trackPlayback.data.on('startEndTimeChange', trackPlayback.startEndTimeChange);
            // 位置改变
            trackPlayback.data.on('positionsChange', trackPlayback.positionsChange);
            trackPlayback.data.on('positionsChange', trackPlayback.table.replaceTable.bind(trackPlayback.table));
            trackPlayback.data.on('allOrRunDataChange', trackPlayback.allOrRunDataChange)
            // 地图显示停止点，报警点，基站
            trackPlayback.data.on('showStopPointChange', trackPlayback.map.toggleStopPoint.bind(trackPlayback.map));
            trackPlayback.data.on('showAlarmPointChange', trackPlayback.map.toggleAlarmPoint.bind(trackPlayback.map));
            trackPlayback.data.on('showLocationChange', trackPlayback.map.toggleLocation.bind(trackPlayback.map));
            // 播放控制
            trackPlayback.data.on('playIndexChange', trackPlayback.playIndexChange);
            trackPlayback.data.on('isPlayingChange', trackPlayback.isPlayingChange);
            trackPlayback.data.on('isPlayingChange', trackPlayback.table.removeHighlightRunSegment.bind(trackPlayback.table));
            // 图表区域
            trackPlayback.data.on('isChartOpenChange', trackPlayback.chart.setChartOpen.bind(trackPlayback.chart));
            trackPlayback.data.on('sensorListChange', trackPlayback.chart.initSensorHtml.bind(trackPlayback.chart));
            trackPlayback.data.on('activeSensorKeysChange', trackPlayback.chart.renderChartLine.bind(trackPlayback.chart));
            // 停止数据
            trackPlayback.data.on('stopDataChange', trackPlayback.chart.stopDataChange.bind(trackPlayback.chart));
            // 里程速度
            trackPlayback.data.on('mileSpeedChange', trackPlayback.chart.mileSpeedChange.bind(trackPlayback.chart));
            // 油耗
            trackPlayback.data.on('oilConsumptionSensorNoChange', trackPlayback.chart.getOilConsumptionData.bind(trackPlayback.chart));
            trackPlayback.data.on('oilConsumptionDataChange', trackPlayback.chart.oilConsumptionDataChange.bind(trackPlayback.chart));
            // 油量
            trackPlayback.data.on('oilSensorNoChange', trackPlayback.chart.getOilData.bind(trackPlayback.chart));
            trackPlayback.data.on('oilDataChange', trackPlayback.chart.oilDataChange.bind(trackPlayback.chart));
            // 温度
            trackPlayback.data.on('temperatureySensorNoChange', trackPlayback.chart.getTemperaturey.bind(trackPlayback.chart));
            trackPlayback.data.on('temperatureyChange', trackPlayback.chart.temperatureyChange.bind(trackPlayback.chart));
            // 湿度
            trackPlayback.data.on('humiditySensorNoChange', trackPlayback.chart.getHumidity.bind(trackPlayback.chart));
            trackPlayback.data.on('humidityChange', trackPlayback.chart.humidityChange.bind(trackPlayback.chart));
            // 工时
            trackPlayback.data.on('workHourSensorNoChange', trackPlayback.chart.getWorkHour.bind(trackPlayback.chart));
            trackPlayback.data.on('workHourChange', trackPlayback.chart.workHourChange.bind(trackPlayback.chart));
            // 正反转
            trackPlayback.data.on('reverseChange', trackPlayback.chart.reverseChange.bind(trackPlayback.chart));
            // 载重
            trackPlayback.data.on('weightSensorNoChange', trackPlayback.chart.getWeight.bind(trackPlayback.chart));
            trackPlayback.data.on('weightChange', trackPlayback.chart.weightChange.bind(trackPlayback.chart));
            // 胎压
            trackPlayback.data.on('tireNumChange', trackPlayback.chart.getTire.bind(trackPlayback.chart));
            trackPlayback.data.on('tireChange', trackPlayback.chart.tireChange.bind(trackPlayback.chart));
            // 开关数据
            trackPlayback.data.on('ioDataSensorNoChange', trackPlayback.chart.ioDataChange.bind(trackPlayback.chart));
            //表格区域
            trackPlayback.data.on('tableTabIndexChange', trackPlayback.table.setActiveTab.bind(trackPlayback.table));
            trackPlayback.data.on('tableTabIndexChange', trackPlayback.table.getRestTableData.bind(trackPlayback.table));
            trackPlayback.data.on('stopFragmentDataChange', trackPlayback.table.replaceStopTable.bind(trackPlayback.table));
            trackPlayback.data.on('runFragmentDataChange', trackPlayback.table.replaceRunTable.bind(trackPlayback.table));
            trackPlayback.data.on('alarmDataChange', trackPlayback.table.replaceAlarmTable.bind(trackPlayback.table));
            trackPlayback.data.on('obdDataChange', trackPlayback.table.replaceObdTable.bind(trackPlayback.table));
        },
        activeTreeNodeChange: function () {
            var treeNode = trackPlayback.data.getActiveTreeNode();
            var isPlaying = trackPlayback.data.getIsPlaying();

            if (isPlaying) {
                trackPlayback.map.pause();
            }
            if (treeNode.type != 'assignment' && treeNode.type != 'group') {
                $("#citySel").val(treeNode.name);
                trackPlayback.calendar.getValidDate(treeNode.id, trackPlayback.data.getNowMonth(), trackPlayback.data.getAfterMonth());
                trackPlayback.chart.getSensorList();
                $('.checkbox-control').removeClass('active');
                trackPlayback.data.setPositions(null);
            } else {
                $("#citySel").val('');
            }
        },
        startEndTimeChange: function () {
            var vehicleId = trackPlayback.data.getVid();
            var startTime = trackPlayback.data.getStartTime();
            var endTime = trackPlayback.data.getEndTime();
            var sensorType = trackPlayback.data.getSensorType();
            var validSensorFlag = trackPlayback.data.getValidSensorFlag();
            var validClickedIndex = trackPlayback.data.getValidClickedIndex();
            var locationType = trackPlayback.data.getShowLocation();
            var clickLocationType = trackPlayback.data.getClickShowLocation();
            var sensorFlag = validSensorFlag && (validClickedIndex !== undefined && validClickedIndex !== null)
                ? validSensorFlag[validSensorFlag] : undefined;

            $("#timeInterval").val(startTime + "--" + endTime);
            var dataStatus0 = $('#dataStatus0').is(':checked');// 非补传
            var dataStatus1 = $('#dataStatus1').is(':checked');// 补传
            var reissue = dataStatus0 ? 0 : 1;
            if (dataStatus0 === dataStatus1 || !$('#realTimeCanArea').hasClass('realTimeCanAreaShow')) reissue = undefined;

            $.ajax({
                type: "POST",
                url: "/clbs/v/monitoring/getMonitorHistoryData",
                data: {
                    "vehicleId": vehicleId,
                    "startTime": startTime,
                    "endTime": endTime,
                    "type": sensorType,
                    "sensorFlag": sensorFlag,
                    "reissue": reissue
                },
                dataType: "json",
                async: true,
                beforeSend: function (XMLHttpRequest) {
                    layer.load(2);
                },
                complete: function (XMLHttpRequest, textStatus) {
                    layer.closeAll('loading');
                },
                success: function (data) {
                    if (data.success) {
                        var positionalData = ungzip(data.obj.allData);
                        var positionals = $.parseJSON(positionalData);
                        if (!locationType || !clickLocationType) {
                            positionals = (positionals || []).filter(item => item.stationEnabled === false);
                        }
                        var groups = data.obj.groups;
                        var type = data.obj.type;
                        var sensorFlag = data.obj.nowFlogKey;
                        positionals = positionals && positionals.length > 0 ? positionals : null;
                        if (positionals && positionals[0]) {
                            var totalTireNum = positionals[0].totalTireNum;
                            if (totalTireNum !== undefined && totalTireNum !== null) {
                                var tireNumList = [];
                                for (var i = 0; i < totalTireNum; i++) {
                                    tireNumList.push(i + 1);
                                }
                                trackPlayback.data.setTireNumList(tireNumList);
                            }
                        } else {
                            layer.msg('未查询到数据');
                        }
                            trackPlayback.data.setGroup(groups);
                            trackPlayback.data.setSensorFlag(sensorFlag);
                            trackPlayback.data.setAllOrRunData(null);
                            trackPlayback.data.setPositions(positionals);
                    } else {
                        trackPlayback.data.setPositions(null);
                        layer.msg(data.msg || data.exceptionDetailMsg);
                    }
                }
            });
        },
        positionsChange: function () {
            var positions = trackPlayback.data.getPositions();
            var allOrRunData = trackPlayback.data.getAllOrRunData();
            var isPlaying = trackPlayback.data.getIsPlaying();
            var isChartOpen = trackPlayback.data.getIsChartOpen();
            // var fenceIdList = trackPlayback.data.getFenceIdList();
            var showStopPoint = trackPlayback.data.getShowStopPoint();
            var showAlarmPoint = trackPlayback.data.getShowAlarmPoint();
            var locationType = trackPlayback.data.getShowLocation();
            var clickLocationType = trackPlayback.data.getClickShowLocation();
            if (isPlaying) {
                trackPlayback.map.pause();
            }
            // 重置显示行驶点
            $('#checkboxShowStop').prop('checked', false);
            $('#checkboxShowAlarm').prop('checked', false);
            if (showStopPoint) {
                trackPlayback.data.setShowStopPoint(false);
            }
            if (showAlarmPoint) {
                trackPlayback.data.setShowAlarmPoint(false);
            }
            if (!clickLocationType && locationType) {
                $('#checkboxShowLocation').prop('checked', false);
                trackPlayback.data.setShowLocation(false);
                locationType = false;
            }
            // 控制图表区域的隐藏和显示
            trackPlayback.data.setIsChartOpen(positions === null || allOrRunData === 'run' ? false : true);
            //清除围栏
            // trackPlayback.map.delFenceListAndMapClear();
            // 绘制地图轨迹，重置播放索引
            trackPlayback.map.clearMap();
            trackPlayback.map.drawLine(isChartOpen, locationType);
            if (positions === null) {
                $('.play-control-container').removeClass('active');
                $('.toggle-chart-part').hide();
            } else {
                $('.play-control-container').addClass('active');
                if (allOrRunData !== 'run') {
                    $('.toggle-chart-part').show();
                }
            }
            trackPlayback.data.setPlayIndex(0);
            trackPlayback.data.setActiveSensorKeys(null); // 先置空，清空dom
            if (positions !== null && allOrRunData !== 'run') {
                trackPlayback.data.setActiveSensorKeys([{key: 'mileSpeed', chart: null}, {
                    key: 'stopData',
                    chart: null
                }]); // 显示里程和停驶曲线
            }
        },
        playIndexChange: function () {
            var positions = trackPlayback.data.getPositions();
            var stopData = trackPlayback.data.getStopData();
            if (positions === null) {
                return;
            }
            var playIndex = trackPlayback.data.getPlayIndex('chart');
            if (playIndex === undefined || playIndex === null) {
                return;
            }
            trackPlayback.progressBar.setOptions({
                value: playIndex
            });
            trackPlayback.table.setActiveRow();
            trackPlayback.table.autoGetStatusPosition();
            trackPlayback.chart.updatePlayIndex();
            trackPlayback.map.closeInfoWindow();
            // 这个方法内部有动画，动画和停止点之间会有时间上的先后重叠，所以要放到最后执行
            trackPlayback.map.drawMarker();
        },
        playOrPause: function () {
            if ($('#playOrPause').hasClass('pause')) {
                trackPlayback.map.pause();
            } else {
                trackPlayback.map.play();
            }
        },
        isPlayingChange: function (changeDirection, target) {
            var isPlaying = trackPlayback.data.getIsPlaying();
            var $playOrPause = $('#playOrPause');
            if (isPlaying) {
                $playOrPause.removeClass('play').addClass('pause');
            } else {
                $playOrPause.removeClass('pause').addClass('play');
            }
            // 如果是通过双击行驶段表格来暂停的，不触发显示弹框操作
            if (target !== 'runTableDblClick') {
                setTimeout(function () {
                    trackPlayback.map.toggleInfoWindow();
                }, 800);
            }
        },
        initLayout: function () {
            var $toggleLeftPart = $('#toggleLeftPart');
            var $leftPart = $('#leftPart');
            var $rightPart = $('#rightPart');
            var $i = $toggleLeftPart.find('i');
            $toggleLeftPart.on('click', function () {
                var isLeft = $i.hasClass('fa-chevron-left');
                if (isLeft) {
                    $leftPart.addClass('collapse');
                    $rightPart.addClass('extend');
                    $i.removeClass('fa-chevron-left').addClass('fa-chevron-right');
                } else {
                    $leftPart.removeClass('collapse');
                    $rightPart.removeClass('extend');
                    $i.removeClass('fa-chevron-right').addClass('fa-chevron-left');
                }
            });
            $('#myTab li.table-tab').on('click', function () {
                var dataDependency = trackPlayback.data;
                var positions = dataDependency.getPositions();
                if (!positions) {
                    return;
                }
                trackPlayback.data.setTableTabIndex($(this).index() + 1);
            });
            $('#scalingBtn').on('click', function () {
                var $this = $(this);
                if ($this.hasClass('fa-chevron-down')) {
                    trackPlayback.dragBar.setDelta(0);
                    $this.removeClass('fa-chevron-down').addClass('fa-chevron-up');
                } else {
                    trackPlayback.dragBar.setDelta(-250);
                    $this.removeClass('fa-chevron-up').addClass('fa-chevron-down');
                }
            });
            $('.toggle-chart-part').on('click', function () {
                if (trackPlayback.data.getPositions() === null) {
                    layer.msg('请先查询历史数据');
                    return;
                }
                trackPlayback.data.setIsChartOpen(!trackPlayback.data.getIsChartOpen());
            });
            $('.page-icon').on('click', trackPlayback.chart.onSensorPageClick);
            $('#sensorWraper').on('click', '.sensor-item', function () {
                var $this = $(this);
                if ($this.hasClass('un-active')) {
                    var index = $this.index();
                    trackPlayback.chart.onSensorItemClick(index);
                }
            });
            $('input[name="videoSet"]').on('change', function () {
                var value = $(this).val();
                trackPlayback.data.setAllOrRunData(value);
            });
            $('#checkboxShowLocation').on('change', function () {
                trackPlayback.data.setShowLocation($(this).prop('checked'));
                trackPlayback.data.setClickShowLocation(true);
                trackPlayback.startEndTimeChange();
            });
            $('#checkboxShowStop').on('change', function () {
                trackPlayback.data.setShowStopPoint($(this).prop('checked'));
            });
            $('#checkboxShowAlarm').on('change', function () {
                trackPlayback.data.setShowAlarmPoint($(this).prop('checked'));
            });
            // trackPlayback.map.initColorSetting();
            $("#addFenceBtn").on("click", trackPlayback.map.addLineFence.bind(trackPlayback.map));

            $("#timeInterval").dateRangePicker({
                'element': '#trackPlayQuery',
                'dateLimit': 7
            });
            $('#customColumnA').on('click', function () {
                if (trackPlayback.data.getIsPlaying()) {
                    trackPlayback.map.pause();
                }
            })
        },
        onDragBarDragEnd: function (delta) {
            var $topPart = $('#topPart');
            var $bottomPart = $('#bottomPart');
            var bottom = parseFloat($topPart.css('bottom'));
            $topPart.css('bottom', (50 - delta) + 'px');
            $bottomPart.css('height', (50 - delta) + 'px');
            if (delta === 0) {
                $('#scalingBtn').removeClass('fa-chevron-down').addClass('fa-chevron-up');
            }
            trackPlayback.chart.reRender();
        },
        onProgressBarDrag: function (value) {
            trackPlayback.data.setIsDraging(true);
            if (trackPlayback.data.getIsPlaying()) {
                trackPlayback.map.pause();
            }
            trackPlayback.data.setPlayIndex(value, 'chart');
        },
        onProgressBarDragEnd: function (value) {
            trackPlayback.data.setIsDraging(false);
            if (trackPlayback.data.getIsPlaying()) {
                trackPlayback.map.pause();
            }
            trackPlayback.data.setPlayIndex(value, 'chart');
        },
        //获取地址栏参数
        getAddressUrl: function (name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]);
            return null;
        },
        /**
         * 过滤全部数据为行驶数据，同时保留停止数据两端的点
         * 算法为状态段过渡的停止点+整个数据首尾的停止点
         * @param array
         * @returns {*}
         */
        filterRunData: function (array) {
            var r = [];
            if (array === null) {
                return null;
            }
            // 2 代表停止
            for (var i = 0; i < array.length; i++) {
                if (array[i].drivingState === '1') {
                    r.push(array[i]);
                } else {
                    if (array[i - 1] && array[i - 1].drivingState === '1' && array[i].drivingState === '2') { // 行驶过渡到停止
                        r.push(array[i]);
                    } else if (array[i + 1] && array[i + 1].drivingState === '1' && array[i].drivingState === '2') { // 停止过渡到行驶
                        r.push(array[i]);
                    } else if ((i === 0 || i === array.length - 1) && array[i].drivingState === '2') { // 首尾的停止点
                        r.push(array[i]);
                    }
                }
            }
            return r;
        },
        /**
         * 过滤停止数据中的停止点，同时保留行驶数据两端的停止点
         * 算法为状态段过渡的停止点+整个数据首尾的停止点
         * @param array
         * @returns {*}
         */
        filterStopData: function (array) {
            var r = [];
            if (array === null) {
                return null;
            }
            for (var i = 0; i < array.length; i++) {
                if (array[i].status === '1') {
                    r.push(array[i]);
                } else {
                    if (array[i - 1] && array[i - 1].status === '1' && array[i].status === '2') { // 行驶过渡到停止
                        r.push(array[i]);
                    } else if (array[i + 1] && array[i + 1].status === '1' && array[i].status === '2') { // 停止过渡到行驶
                        r.push(array[i]);
                    } else if ((i === 0 || i === array.length - 1) && array[i].status === '2') { // 首尾的停止点
                        r.push(array[i]);
                    }
                }
            }
            return r;
        },
        allOrRunDataChange: function () {
            var showStopPoint = trackPlayback.data.getShowStopPoint();
            var showAlarmPoint = trackPlayback.data.getShowAlarmPoint();
            if (showStopPoint) {
                trackPlayback.data.setShowStopPoint(false);
            }
            if (showAlarmPoint) {
                trackPlayback.data.setShowAlarmPoint(false);
            }
            var allOrRunData = trackPlayback.data.getAllOrRunData();
            var positions = trackPlayback.data.getPositions();
            var positionsTmp = trackPlayback.data.getPositionsTmp();
            var stopData = trackPlayback.data.getStopData();
            var stopDataTmp = trackPlayback.data.getStopDataTmp();
            var $allRadio = $('#allRadio');
            var $runRadio = $('#runRadio');
            var $allRunData = $('#allRunDataA');
            if (allOrRunData === null) {
                $allRadio.prop('checked', true);
                $runRadio.prop('checked', false);
                $allRunData.html('全部数据');
            } else if (allOrRunData === 'run') {
                $allRadio.prop('checked', false);
                $runRadio.prop('checked', true);
                $allRunData.html('行驶数据');
                $('.toggle-chart-part').hide();
                trackPlayback.data.setTableTabIndex(1);
                // 全部/行驶数据
                var filteredData = trackPlayback.filterRunData(positions);
                trackPlayback.data.setPositionsTmp(positions);
                trackPlayback.data.setPositions(filteredData);
                // 停止数据
                var filteredStopData = trackPlayback.filterStopData(stopData);
                trackPlayback.data.setStopDataTmp(stopData);
                trackPlayback.data.setStopData(filteredStopData);
            } else if (allOrRunData === 'all') {
                $allRadio.prop('checked', true);
                $runRadio.prop('checked', false);
                $allRunData.html('全部数据');
                $('.toggle-chart-part').show();
                trackPlayback.data.setTableTabIndex(1);
                trackPlayback.data.setPositions(positionsTmp);
            }
        },
        handleNodeClcik: function (dependency) {
            setTimeout(function () {
                var treeObj = $.fn.zTree.getZTreeObj("treeDemoTrackPlayback");
                var nodes = treeObj.getCheckedNodes(true);
                if (nodes.length > 0) {
                    var fenceTree = dependency.get('fenceTree');
                    fenceTree.vehicleTreeClickGetFenceInfo.bind(fenceTree)(nodes[0].checked, nodes[0].id);
                }
            }, 1500);
        },
        gjhfObdDiagnosticSupportStateClick: function (...data) {
            $('#gjhfObdDiagnosticSupportStateClick').hide();
            $('#gjhfDiagnosticSupport-1').text(data[0] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-2').text(data[1] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-3').text(data[2] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-4').text(data[3] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-5').text(data[4] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-6').text(data[5] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-7').text(data[6] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-8').text(data[7] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-9').text(data[8] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-10').text(data[9] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-11').text(data[10] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-12').text(data[11] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-13').text(data[12] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-14').text(data[13] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-15').text(data[14] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-16').text(data[15] ? '支持' : '不支持');
            setTimeout(function () {
                $('#gjhfObdDiagnosticSupportStateClick').show();
            }, 300);
        },
        gjhfObdDiagnosticSupportStateOpen: function () {
            $('#gjhfObdDiagnosticSupportStateClick').hide();
        },
        gjhfObdDiagnosticReadyStateClick: function (...data) {
            $('#gjhfObdDiagnosticSupportStateClick').hide();
            $('#gjhfDiagnosticSupport-1').text(data[0] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-2').text(data[1] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-3').text(data[2] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-4').text(data[3] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-5').text(data[4] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-6').text(data[5] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-7').text(data[6] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-8').text(data[7] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-9').text(data[8] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-10').text(data[9] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-11').text(data[10] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-12').text(data[11] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-13').text(data[12] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-14').text(data[13] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-15').text(data[14] ? '支持' : '不支持');
            $('#gjhfDiagnosticSupport-16').text(data[15] ? '支持' : '不支持');
            setTimeout(function () {
                $('#gjhfObdDiagnosticSupportStateClick').show();
            }, 300);
        },
    };
    trackPlayback.init();
})