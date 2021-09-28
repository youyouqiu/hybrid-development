var SmallWindow = function (selector, options, dependency) {
    this.dependency = dependency;
    this.$selector = $(selector);

    this.isDragging = false;
    this.containerRect = null;
    this.sourceWindowIndex = null;
}

SmallWindow.prototype.init = function () {
    var dataDependency = this.dependency.get('data');
    dataDependency.setWindowCount(6);
    this.initSetting();
}

/**
 * 初始化用户保存的设置，用于期望显示的传感器信息
 */
SmallWindow.prototype.initSetting = function () {
    $.ajax({
        type: "POST",
        url: "/clbs/core/uum/custom/findCustomColumnInfoByMark",
        dataType: "json",
        async: true,
        data: {marks: 'MULTI_WINDOW_REALTIME_DATA_LIST'},
        success: function (data) {
            if (data.success && data.obj) {
                var serverColumns = data.obj['MULTI_WINDOW_REALTIME_DATA_LIST'];
                var dataDependency = this.dependency.get('data');
                dataDependency.setVisibleSensorKeyArray(serverColumns.map(function (x) {
                    return x.columnName;
                }));
            }
        }.bind(this)
    });
};

SmallWindow.prototype.entireUpdateSubscribe = function () {
    var dataDependency = this.dependency.get('data');
    var subscribObjArray = dataDependency.getSubscribObjArray();
    for (var i = 0; i < subscribObjArray.length; i++) {
        var vidInfo = subscribObjArray[i];
        dataDependency.updateSubscribVid(vidInfo.vid, vidInfo);
    }
}

SmallWindow.prototype.renderWindow = function () {
    var dataDependency = this.dependency.get('data');

    var windowCount = dataDependency.getWindowCount();
    var mapInstanceArray = dataDependency.getMapInstanceArray();
    var activeWindowIndex = dataDependency.getActiveWindowIndex();
    var subscribeObjArray = dataDependency.getSubscribObjArray();

    var windows = this.$selector.find('.window');
    var windowsLength = windows.length;

    windows.each(function (idx, ele) {
        var className = ele.className;
        var updateClass = 'window window-' + windowCount.toString();
        if (idx === activeWindowIndex) {
            updateClass += ' focus';
        }
        if (className.indexOf('disabled') > -1) {
            updateClass += ' disabled';
        }
        ele.className = updateClass;
    });

    if (windowsLength >= windowCount) {
        for (var i = windowsLength - 1; i >= windowCount; i--) {
            $(windows[i]).remove();
            mapInstanceArray.pop();
        }
        for (var i = 0; i < subscribeObjArray.length; i++) {
            var vidInfo = subscribeObjArray[i];
            if (vidInfo.windowIndex >= windowCount) {
                vidInfo.followPath = undefined;
                if (vidInfo.marker) {
                    vidInfo.marker.stopMove();
                    vidInfo.marker = undefined;
                    vidInfo.contentMarker.stopMove();
                    vidInfo.contentMarker = undefined;
                }
            }
        }
    } else {
        for (var j = windowsLength; j < windowCount; j++) {
            var id = 'amapContainer-' + Math.random().toString();
            var $wrapper = $('<div class="disabled window window-' + windowCount.toString() + '"></div>');
            var $default = $($('#windowDefaultTmpl').html());
            $default.find('.map-bag').attr('id', id);
            $wrapper.append($default);
            this.$selector.append($wrapper);
            var map = 'not initialed yet';
            mapInstanceArray.push(map);
        }
        for (var i = 0; i < subscribeObjArray.length; i++) {
            var vidInfo = subscribeObjArray[i];
            if (vidInfo.windowIndex < windowCount && vidInfo.windowIndex > (windowsLength - 1)) {
                dataDependency.addSubscribVid(vidInfo.vid, vidInfo);
                dataDependency.updateSubscribVid(vidInfo.vid, vidInfo);
            }
        }
    }

    dataDependency.setMapInstanceArray(mapInstanceArray);
    // 如果窗口数比聚焦的还小，取消聚焦
    if (windowCount - 1 < activeWindowIndex) {
        dataDependency.setActiveWindowIndex(null);
    }
    // 左下角数字高亮
    var $target = $('.number-icon:eq(' + (windowCount - 1) + ')');
    if ($target.hasClass('active')) {
        return;
    }
    $target.siblings().removeClass('active');
    $target.addClass('active');
}

SmallWindow.prototype.changeWindowCount = function (event) {
    var dataDependency = this.dependency.get('data');

    var $target = $(event.currentTarget);
    var type = $target.data('type');

    dataDependency.setWindowCount(parseInt(type));
}

SmallWindow.prototype.createMarker = function (map, position, type, icon, angle, name, status) {
    var iconInfo = Util.getIconPath(type, icon);
    var marker = new AMap.Marker({
        map: map,
        position: position,//基点位置
        icon: iconInfo.icon, //marker图标，直接传递地址url
        offset: new AMap.Pixel(-iconInfo.picWidth, -iconInfo.picHeight), //相对于基点的位置
        zIndex: 99,
        autoRotation: true,
        angle: angle
    });

    var carState = Util.status2ColorClass(status);
    var carContent = "<p class='carNameShowRD'><i class='" + carState + "'></i>&ensp;<span class='monitorNameBox'>" + name + "</span></p>";

    var markerContent = new AMap.Marker({
        position: position,
        content: carContent,
        offset: new AMap.Pixel(iconInfo.picWidth, iconInfo.picHeight), //相对于基点的位置
        autoRotation: false,//自动调节图片角度
        map: map,
        zIndex: 999

    });

    return {
        marker: marker,
        contentMarker: markerContent
    }
}

SmallWindow.prototype.updateStatusColor = function ($window, status) {
    var $i = $window.find('.carNameShowRD i');
    if ($i.length === 0) {
        return;
    }
    var carState = Util.status2ColorClass(status);
    if (carState !== $i[0].className) {
        $i[0].className = carState;
    }
}

SmallWindow.prototype.addSubscribe = function () {
    var dataDependency = this.dependency.get('data');

    var mapInstanceArray = dataDependency.getMapInstanceArray();
    var mapType = dataDependency.getMapType();
    var showTraffic = dataDependency.getShowTraffic();
    var latestUpdateVid = dataDependency.getLatestUpdateVid();
    var vidInfo = dataDependency.getSubscribVidInfo(latestUpdateVid);
    var windowIndex = vidInfo.windowIndex;

    var $window = $('.window:eq(' + windowIndex.toString() + ')');
    var id = $window.find('.map-bag').attr('id');
    var map = new AMap.Map(id, {
        resizeEnable: false,
        scrollWheel: true,
        zoom: 18
    });

    var mapScale = AMap.plugin(['AMap.Scale'], function () {
        map.addControl(new AMap.Scale());
    });

    map.on('dragstart', function (vid, ddp) {
        var id = vid;
        var dataDependency = ddp;
        return function () {
            var vidInfo = dataDependency.getSubscribVidInfo(id);
            if (vidInfo && vidInfo.keepCenter) {
                vidInfo.keepCenter = false;
                dataDependency.updateSubscribVid(id, vidInfo);
            }
        }
    }(latestUpdateVid, dataDependency));

    var mapObj = {map: map};

    if (mapType === 'satellite') {
        var satelliteLayer = new AMap.TileLayer.Satellite();
        satelliteLayer.setMap(map);
        mapObj.satellite = satelliteLayer;
        // 路网
        var roadNet = new AMap.TileLayer.RoadNet();
        roadNet.setMap(map);
        mapObj.roadNet = roadNet;
    } else if (mapType === 'google') {
        var googleMapLayer = new AMap.TileLayer({
            tileUrl: 'http://mt{1,2,3,0}.google.cn/vt/lyrs=m@142&hl=zh-CN&gl=cn&x=[x]&y=[y]&z=[z]&s=Galil', // 图块取图地址
            zIndex: 100 //设置Google层级与高德相同  避免高德路况及卫星被Google图层覆盖
        });
        googleMapLayer.setMap(map);
        mapObj.google = googleMapLayer;
    }

    if (showTraffic) {
        var realTimeTraffic = new AMap.TileLayer.Traffic({zIndex: 102});
        realTimeTraffic.setMap(map);
        mapObj.traffic = realTimeTraffic;
    }

    mapInstanceArray[windowIndex] = mapObj;
    $window.removeClass('disabled');
    dataDependency.setMapInstanceArray(mapInstanceArray);
}

SmallWindow.prototype.updateBasicInfo = function ($window, vidInfo) {
    var statusHTML = Util.status2Html(vidInfo.basicInfo.status);

    $window.find('.status-text').html(statusHTML + '(' + vidInfo.basicInfo.duration + ')');
    $window.find('.status-signal').html(vidInfo.basicInfo.locateType);
    $window.find('.window-time').html(vidInfo.basicInfo.locateTime);
    $window.find('.window-location').html(vidInfo.basicInfo.location);
}

SmallWindow.prototype.updateSignalInfo = function ($window, vidInfo) {
    // 电量
    if (Util.noUndefineOrNull(vidInfo.sensorInfo.elec)) {
        var $battery = $window.find('.battery-box');
        $battery.show();
        $battery.find('.battery-bag').css('width', vidInfo.sensorInfo.elec + '%');
        $battery.find('.battery-text').html(vidInfo.sensorInfo.elec);
    } else {
        $window.find('.battery-box').hide();
    }
    // 卫星
    if (vidInfo.basicInfo.locateType.indexOf('卫星') > -1 &&
        Util.noUndefineOrNull(vidInfo.sensorInfo.satellitesNumber)) {
        var $satellite = $window.find('.satellite-box');
        $satellite.show();
        $satellite.find('.satellite-text').html(vidInfo.sensorInfo.satellitesNumber);
    } else {
        $window.find('.satellite-box').hide();
    }
    // wifi
    if (Util.noUndefineOrNull(vidInfo.sensorInfo.wifi) && vidInfo.sensorInfo.wifi > 0) {
        var $wifi = $window.find('.wifi-box');
        $wifi.show().get(0).className = 'wifi-box wifi-box-' + vidInfo.sensorInfo.wifi;

    } else {
        $window.find('.wifi-box').hide();
    }
    // 基站
    if (Util.noUndefineOrNull(vidInfo.sensorInfo.signalType)
        && Util.noUndefineOrNull(vidInfo.sensorInfo.signalStrength)
    // && vidInfo.sensorInfo.signalStrength > -1
    ) {
        var $gps = $window.find('.gps-box');
        $gps.show().get(0).className = 'gps-box gps-box-' + vidInfo.sensorInfo.signalStrength;
        $gps.find('.gps-text').html(Util.formateStationType(vidInfo.sensorInfo.signalType));
        if (vidInfo.sensorInfo.signalStrength === -1) {
            $gps.find('.gps-text').css('marginLeft', 0);
        } else {
            $gps.find('.gps-text').removeAttr('style');
        }
    } else {
        $window.find('.gps-box').hide();
    }
}

SmallWindow.prototype.updateMapInfo = function ($window, vidInfo, map) {
    if (!Util.noUndefineOrNull(vidInfo.basicInfo.longitude)
        || !Util.noUndefineOrNull(vidInfo.basicInfo.latitude)) {
        return;
    }
    // 创建地图图标
    var position = new AMap.LngLat(vidInfo.basicInfo.longitude, vidInfo.basicInfo.latitude);

    var type = vidInfo.basicInfo.type;
    var icon = vidInfo.basicInfo.icon;
    var angle = vidInfo.basicInfo.angle;
    var status = vidInfo.basicInfo.status;

    if (!vidInfo.marker) {
        var markerAndName = this.createMarker(map, position, type, icon, angle, vidInfo.brandName, status);
        vidInfo.marker = markerAndName.marker;
        vidInfo.contentMarker = markerAndName.contentMarker;
        vidInfo.marker.on('moving', function (e) {
            this.resetCenter(map, vidInfo);
        }.bind(this));
        vidInfo.marker.on('moveend', function (e) {
            this.pointEnd(map, vidInfo);
        }.bind(this));
        this.resetCenter(map, vidInfo, true);
        this.drawFollowPath(map, vidInfo);
    } else {
        if (this.shouldMove(vidInfo)) {
            this.moveMarker(map, vidInfo);
        }
        this.updateStatusColor($window, status);
    }
}
SmallWindow.prototype.shouldMove = function (vidInfo) {
    var positionArray = vidInfo.basicInfo.positionArray;
    // positionIndex point to the position where we are going to
    var positionIndex = vidInfo.positionIndex;
    if (vidInfo.shouldMove
        && !vidInfo.isMoving
        && positionArray.length >= 2
        && positionIndex < positionArray.length) {
        return true;
    }
    return false;
}

SmallWindow.prototype.moveMarker = function (map, vidInfo) {
    if (vidInfo.shouldMove) {
        vidInfo.isMoving = true;
    } else {
        return;
    }

    var positionArray = vidInfo.basicInfo.positionArray;
    var gpsTimeArray = vidInfo.basicInfo.gpsTimeArray;
    // positionIndex point to the position where we are going to
    var positionIndex = vidInfo.positionIndex;
    positionIndex += 1;
    vidInfo.positionIndex = positionIndex;

    var position = new AMap.LngLat(positionArray[positionIndex][0], positionArray[positionIndex][1]);
    var distance = Util.calcDistance(positionArray[positionIndex], positionArray[positionIndex - 1]);
    var timeDiff = (gpsTimeArray[positionIndex].getTime() - gpsTimeArray[positionIndex - 1].getTime()) / 1000 / 60; // 分钟

    if (this.shouldJump(distance, timeDiff, map)) {
        if (distance > 0) {
            vidInfo.marker.setPosition(position);
            vidInfo.marker.setAngle(vidInfo.basicInfo.angle);
            vidInfo.contentMarker.setPosition(position);

            if (vidInfo.keepCenter) {
                map.panTo(position);
            }
        }
        this.pointEnd(map, vidInfo);
    } else {
        // 如果速度大于230 km/h, 那么不执行移动，为了保证数据的完整性，程序上通过替换为上一个点来实现
        if (vidInfo.basicInfo.speed >= 230) {
            gpsTimeArray[positionIndex] = gpsTimeArray[positionIndex - 1]
            positionArray[positionIndex] = positionArray[positionIndex - 1];
            this.pointEnd(map, vidInfo);
        } else {
            var gpsTimeArray = vidInfo.basicInfo.gpsTimeArray;
            var time = (new Date(gpsTimeArray[1]).getTime()) - (new Date(gpsTimeArray[0]).getTime());
            var angle = this.calcAngle(map, positionArray[positionIndex-1], positionArray[positionIndex]) + 360;
            setTimeout(function () {
                vidInfo.contentMarker.moveTo(position, {
                    duration: time,
                    delay: 100,
                    autoRotation: false,
                });
                vidInfo.marker.setAngle(angle);
                vidInfo.marker.moveTo(position, {
                    duration: time,
                    delay: 100,
                    autoRotation: false,
                });
            }, 0);
        }
    }
}

// 获取两个点旋转角度
SmallWindow.prototype.calcAngle = function (map, startPos, endPos) {
    const p_start = map.lngLatToContainer(startPos);
    const p_end = map.lngLatToContainer(endPos);
    const diff_x = p_end.x - p_start.x;
    const diff_y = p_end.y - p_start.y;
    return 360 * Math.atan2(diff_y, diff_x) / (2 * Math.PI);
}

SmallWindow.prototype.pointEnd = function (map, vidInfo) {
    vidInfo.isMoving = false;

    var positionArray = vidInfo.basicInfo.positionArray;
    // positionIndex point to the position which we are going to
    var positionIndex = vidInfo.positionIndex;

    if (positionIndex == positionArray.length - 1) {
        vidInfo.shouldMove = false;
    }

    this.drawFollowPath(map, vidInfo);

    if (positionIndex < positionArray.length - 1) {
        this.moveMarker(map, vidInfo);
    }
}

SmallWindow.prototype.drawFollowPath = function (map, vidInfo) {
    // 绘制尾迹
    if (vidInfo.openFollowPath) {
        var positionIndex = vidInfo.positionIndex;
        var positionArrayCopy = vidInfo.basicInfo.positionArray.slice(0, positionIndex + 1).map(function (x) {
            return [x[0], x[1]];
        });
        if (!vidInfo.followPath) {
            vidInfo.followPath = new AMap.Polyline({
                map: map,
                path: positionArrayCopy,
                strokeColor: "#428bca",  //线颜色
                strokeOpacity: 0.9,     //线透明度
                strokeWeight: 6,      //线宽
                strokeStyle: "solid",  //线样式
                zIndex: 60,
                showDir: true
            });
        } else {
            vidInfo.followPath.setPath(positionArrayCopy);
        }
    } else if (vidInfo.followPath) {
        map.remove(vidInfo.followPath);
        vidInfo.followPath = undefined;
    }
}


SmallWindow.prototype.resetCenter = function (map, vidInfo, immediate) {
    if (vidInfo.keepCenter) {
        var bounds = map.getBounds();
        var currentPosition = vidInfo.marker.getPosition();
        if (!bounds.contains(currentPosition) || immediate) {
            map.setCenter(currentPosition);
        }
    }
}

SmallWindow.prototype.shouldJump = function (distance, timeDiff, map) {
    // 判断时间差如果大于等于5分钟，跳点
    if (timeDiff >= 5) {
        return true;
    }

    var zoom = map.getZoom();
    var threshold;
    switch (zoom) {
        case 18:
            threshold = 25;
            break;
        case 17:
            threshold = 50;
            break;
        case 16:
            threshold = 100;
            break;
        case 15:
            threshold = 200;
            break;
        case 14:
            threshold = 300;
            break;
        case 13:
            threshold = 500;
            break;
        case 12:
            threshold = 1000;
            break;
        case 11:
            threshold = 2500;
            break;
        case 10:
            threshold = 5000;
            break;
        case 9:
            threshold = 10000;
            break;
        case 8:
            threshold = 25000;
            break;
        case 7:
            threshold = 50000;
            break;
        case 6:
            threshold = 80000;
            break;
        case 5:
            threshold = 100000;
            break;
        case 4:
            threshold = 250000;
            break;
        case 3:
            threshold = 500000;
            break;
        default:
            threshold = 500000;
            break;
    }

    if (distance < threshold) {
        return true;
    } else {
        return false;
    }
}

SmallWindow.prototype.updateSensorInfo = function ($window, vidInfo) {
    var dataDependency = this.dependency.get('data');

    var visibleSensorKeyArray = dataDependency.getVisibleSensorKeyArray();

    var toAppendHtml = [];
    var $sensorBag = $window.find('.sensor-bag');

    var currentSpeed = vidInfo.sensorInfo.currentSpeed;
    var todayMileage = vidInfo.sensorInfo.todayMileage;
    var speedValueSource = vidInfo.sensorInfo.speedValueSource;
    var acc = vidInfo.sensorInfo.acc;
    var oilMass = vidInfo.sensorInfo.oilMass;
    var oilExpend = vidInfo.sensorInfo.oilExpend;
    var temp = vidInfo.sensorInfo.temp;
    var humi = vidInfo.sensorInfo.humi;
    var workhour = vidInfo.sensorInfo.workhour;
    var reverse = vidInfo.sensorInfo.reverse;
    var weight = vidInfo.sensorInfo.weight;
    var tire = vidInfo.sensorInfo.tire;

    // 里程
    if (visibleSensorKeyArray.indexOf('mileData') > -1) {
        var $mileage = $($('#sensorMileageTmpl').html());
        $mileage.find('.sensor-value').html(Util.nvl(todayMileage, '--'));
        toAppendHtml.push($mileage);
    }
    // 速度
    if (visibleSensorKeyArray.indexOf('speedData') > -1) {
        var $speed = $($('#sensorSpeedTmpl').html());
        $speed.find('.sensor-value').html(Util.nvl(currentSpeed, '--'));
        if (Util.noUndefineOrNull(speedValueSource) && speedValueSource === 1) {
            $speed.find('.sensor-number').html('1#');
        }
        toAppendHtml.push($speed);
    }
    // acc
    if (visibleSensorKeyArray.indexOf('ACCData') > -1) {
        var $acc = $($('#sensorAccTmpl').html());
        $acc.find('.sensor-value').html(acc === 0 ? '关' : '开');
        toAppendHtml.push($acc);
    }
    // 油量
    if (visibleSensorKeyArray.indexOf('oilMassData') > -1 && Util.noUndefineOrNull(oilMass)) {
        for (var i = 0; i < oilMass.length; i++) {
            var oil = oilMass[i];
            var $oilMass = $($('#sensorOilTmpl').html());
            $oilMass.find('.sensor-value').html(Util.replaceUnusual(oil, 'oilMass'));
            $oilMass.find('.sensor-number').html((oil.id - 0x40).toString() + '#');
            toAppendHtml.push($oilMass);
        }
    }
    // 油耗
    if (visibleSensorKeyArray.indexOf('oilConsumptionData') > -1 && Util.noUndefineOrNull(oilExpend)) {
        for (var i = 0; i < oilExpend.length; i++) {
            var oe = oilExpend[i];
            var $oilExpend = $($('#sensoroilExpendTmpl').html());
            $oilExpend.find('.sensor-value').html(Util.replaceUnusual(oe, 'dayOilWear'));
            $oilExpend.find('.sensor-number').html((oe.id - 0x44).toString() + '#');
            toAppendHtml.push($oilExpend);
        }
    }
    // 温度
    if (visibleSensorKeyArray.indexOf('teperatureData') > -1 && Util.noUndefineOrNull(temp)) {
        for (var i = 0; i < temp.length; i++) {
            var t = temp[i];
            var $temp = $($('#sensorTempTmpl').html());
            $temp.find('.sensor-value').html(Util.replaceUnusual(t, 'temperature'));
            $temp.find('.sensor-number').html((t.id - 0x20).toString() + '#');
            toAppendHtml.push($temp);
        }
    }
    // 湿度
    if (visibleSensorKeyArray.indexOf('humidityData') > -1 && Util.noUndefineOrNull(humi)) {
        for (var i = 0; i < humi.length; i++) {
            var h = humi[i];
            var $humi = $($('#sensorHumiTmpl').html());
            $humi.find('.sensor-value').html(Util.replaceUnusual(h, 'temperature'));
            $humi.find('.sensor-number').html((h.id - 0x25).toString() + '#');
            toAppendHtml.push($humi);
        }
    }
    // 工时
    if (visibleSensorKeyArray.indexOf('laborHourData') > -1 && Util.noUndefineOrNull(workhour)) {
        for (var i = 0; i < workhour.length; i++) {
            var w = workhour[i];
            var $workhour = $($('#sensorWorkhourTmpl').html());
            var continueTime = Util.replaceUnusual(w, 'continueTime');
            if (continueTime !== '--') {
                continueTime = Util.getHourFromSecond(continueTime);
            }
            $workhour.find('.sensor-value').html(continueTime);
            $workhour.find('.sensor-title-text').html(Util.formateWorkingPosition(w.workingPosition));
            $workhour.find('.sensor-number').html((w.id - 0x7f).toString() + '#');
            toAppendHtml.push($workhour);
        }
    }
    // 正反转
    if (visibleSensorKeyArray.indexOf('positiveNegative') > -1 && Util.noUndefineOrNull(reverse)) {
        for (var i = 0; i < reverse.length; i++) {
            var r = reverse[i];
            var $reverse = $($('#sensorReverseTmpl').html());
            var winchRotateTime = Util.replaceUnusual(r, 'winchRotateTime');
            if (winchRotateTime !== '--') {
                winchRotateTime = Util.getHourFromSecond(winchRotateTime * 60);
            }
            $reverse.find('.sensor-value').html(winchRotateTime);
            $reverse.find('.sensor-title-text').html(Util.formateReverseState(r.spinState, r.spinDirection));
            toAppendHtml.push($reverse);
        }
    }
    // 载重
    if (visibleSensorKeyArray.indexOf('loadData') > -1 && Util.noUndefineOrNull(weight)) {
        for (var i = 0; i < weight.length; i++) {
            var ww = weight[i];
            var $weight = $($('#sensorWeightTmpl').html());
            var loadWeight = Util.replaceUnusual(ww, 'loadWeight', function (value) {
                if (value >= 1000) {
                    return Util.toFixed(value / 1000, 1, true);
                } else {
                    return Util.toFixed(value, 1, true);
                }
            });
            $weight.find('.sensor-value').html(loadWeight);
            $weight.find('.sensor-title-text').html(Util.formateWeightStatus(ww.status));
            $weight.find('.sensor-number').html((ww.id - 0x6f).toString() + '#');
            $weight.find('.sensor-unit').html(ww.loadWeight >= 1000 ? 'T' : 'kg');
            toAppendHtml.push($weight);
        }
    }
    // 胎压
    if (visibleSensorKeyArray.indexOf('tirePressure') > -1 && Util.noUndefineOrNull(tire)) {
        for (var i = 0; i < tire.length; i++) {
            var t = tire[i];
            var $tire = $($('#sensorTireTmpl').html());
            $tire.find('.sensor-value').html(Util.replaceUnusual(t, 'pressure'));
            $tire.find('.sensor-number').html((t.number + 1).toString() + '#');
            toAppendHtml.push($tire);
        }
    }
    // IO 到IO 需要异步请求后才更新界面
    if (visibleSensorKeyArray.indexOf('switchStatus') > -1) {
        json_ajax('POST', '/clbs/v/monitoring/getIoSignalInfo', 'json', true, {
            monitorId: vidInfo.vid,
            type: 'allIo'
        }, function (ioData) {
            $sensorBag.empty();
            for (var i = 0; i < toAppendHtml.length; i++) {
                $sensorBag.append(toAppendHtml[i]);
            }
            if (ioData.success) {
                var obj = ioData.obj;
                for (var i = 0; i < obj.length; i++) {
                    var ioSensor = obj[i];
                    if (ioSensor.sensorStatus === 0) {
                        for (var j = 0; j < ioSensor.ioData.length; j++) {
                            var io = ioSensor.ioData[j];
                            var $io = $($('#sensorIOTmpl').html());
                            $io.find('.sensor-value').html(io.ioStatusName);
                            $io.find('.sensor-title-text').html(io.ioName);
                            if (ioSensor.id === 0x91) {
                                $io.find('.sensor-number').html('1#');
                            } else if (ioSensor.id === 0x92) {
                                $io.find('.sensor-number').html('2#');
                            }
                            $sensorBag.append($io);
                        }
                    }
                }
            }
        });
    } else {
        $sensorBag.empty();
        for (var i = 0; i < toAppendHtml.length; i++) {
            $sensorBag.append(toAppendHtml[i]);
        }
    }
}

SmallWindow.prototype.updateSubscribe = function () {
    var dataDependency = this.dependency.get('data');

    var latestUpdateVid = dataDependency.getLatestUpdateVid();
    var mapInstanceArray = dataDependency.getMapInstanceArray();
    var vidInfo = dataDependency.getSubscribVidInfo(latestUpdateVid);
    var windowCount = dataDependency.getWindowCount();

    if (windowIndex > windowCount - 1) {
        return;
    }

    var windowIndex = vidInfo.windowIndex;
    var mapObj = mapInstanceArray[windowIndex];
    if (!mapObj) {
        vidInfo.positionIndex = vidInfo.basicInfo.positionArray.length - 1;
        return;
    }
    var map = mapInstanceArray[windowIndex].map;
    var $window = $('.window:eq(' + windowIndex.toString() + ')');

    this.updateBasicInfo($window, vidInfo);
    this.updateSignalInfo($window, vidInfo);
    this.updateMapInfo($window, vidInfo, map);
    this.updateSensorInfo($window, vidInfo);
}

SmallWindow.prototype.removeSubscribe = function () {
    var dataDependency = this.dependency.get('data');

    var latestUpdateVid = dataDependency.getLatestUpdateVid();
    var vidInfo = dataDependency.getSubscribVidInfo(latestUpdateVid);
    var mapInstanceArray = dataDependency.getMapInstanceArray();
    var windowIndex = vidInfo.windowIndex;
    var mapObj = mapInstanceArray[windowIndex];

    if (!mapObj) {
        return;
    }

    var map = mapObj.map;

    if (vidInfo.marker) {
        vidInfo.marker.stopMove();
        vidInfo.contentMarker.stopMove();
    }

    map.destroy();
    mapInstanceArray[windowIndex] = 'not initialed yet';
    dataDependency.setMapInstanceArray(mapInstanceArray);

    var $window = $('.window:eq(' + windowIndex.toString() + ')');
    $window.addClass('disabled');
    $window.find('.status-box').removeClass('open');
    $window.find('.status-text').html('状态：--');
    $window.find('.status-signal').html('');
    $window.find('.window-time').html('--');
    $window.find('.window-location').html('--');
}

SmallWindow.prototype.toggleFocus = function (event) {
    var dataDependency = this.dependency.get('data');
    var activeWindowIndex = dataDependency.getActiveWindowIndex();

    var $target = $(event.currentTarget);
    var index = $target.index();

    if (index !== activeWindowIndex) {
        dataDependency.setActiveWindowIndex(index);
    }
}

SmallWindow.prototype.onRightClick = function (event) {
    event.preventDefault();
    event.stopPropagation();
    var dataDependency = this.dependency.get('data');
    var activeWindowIndex = dataDependency.getActiveWindowIndex();

    var $target = $(event.currentTarget);
    var index = $target.index();

    if (index === activeWindowIndex) {
        dataDependency.setActiveWindowIndex(null);
    }
}

SmallWindow.prototype.activeWindowIndexChange = function () {
    var dataDependency = this.dependency.get('data');
    var activeWindowIndex = dataDependency.getActiveWindowIndex();

    $('.window').removeClass('focus');
    if (activeWindowIndex !== null) {
        $('.window:eq(' + activeWindowIndex + ')').addClass('focus');
    }
}

SmallWindow.prototype.toggleStatusBox = function (event) {
    var dataDependency = this.dependency.get('data');
    var $target = $(event.currentTarget);
    var $statusBox = $target.closest('.status-box');

    if ($statusBox.hasClass('open')) {
        $statusBox.removeClass('open');
    } else {
        var sensorBagHeight = $statusBox.find('.sensor-bag').height();
        $statusBox.addClass('open');
    }
}

SmallWindow.prototype.toggleFullScreen = function (event) {
    var $target = $(event.currentTarget);
    var $window = $target.closest('.window');
    if ($window.hasClass('full-screen')) {
        $window.removeClass('full-screen');
        $window.siblings().removeClass('hidden');
    } else {
        $window.addClass('full-screen');
        $window.siblings().addClass('hidden');
    }
}

SmallWindow.prototype.toggleTrack = function (event) {
    var dataDependency = this.dependency.get('data');

    var subscribObjArray = dataDependency.getSubscribObjArray();

    var $target = $(event.currentTarget);
    var $window = $target.closest('.window');
    var windowIndex = $window.index();


    var vid;
    for (var j = 0; j < subscribObjArray.length; j++) {
        if (subscribObjArray[j].windowIndex === windowIndex) {
            vid = subscribObjArray[j].vid;
            break;
        }
    }

    var vidInfo = dataDependency.getSubscribVidInfo(vid);

    if ($target.hasClass('track-icon-on')) {
        vidInfo.openFollowPath = false;
        $target.removeClass('track-icon-on');
    } else {
        vidInfo.openFollowPath = true;
        $target.addClass('track-icon-on');
    }

    dataDependency.updateSubscribVid(vid, vidInfo);
}

SmallWindow.prototype.dragMouseDown = function (event) {
    if (!this.isDragging) {
        event.preventDefault();
        event.stopPropagation();
        var clientX = event.clientX;
        var clientY = event.clientY;
        var $target = $(event.currentTarget);
        var $placeholder = $('.drag-placeholder');

        $placeholder.css({
            top: clientY + 5,
            left: clientX + 5
        }).show();
        this.isDragging = true;
        this.containerRect = document.getElementById('multiWindowContainer').getBoundingClientRect();
        this.sourceWindowIndex = $target.closest('.window').index();
    }
}

SmallWindow.prototype.dragMouseMove = function (event) {
    if (this.isDragging) {
        event.preventDefault();
        event.stopPropagation();

        var clientX = event.clientX;
        var clientY = event.clientY;

        if (clientX < this.containerRect.left) {
            clientX = this.containerRect.left;
        }
        if (clientY < this.containerRect.top) {
            clientY = this.containerRect.top;
        }

        var $placeholder = $('.drag-placeholder');

        $placeholder.css({
            top: clientY + 5,
            left: clientX + 5
        });
    }
}

SmallWindow.prototype.dragMouseUp = function (event) {
    if (this.isDragging) {
        var clientX = event.clientX;
        var clientY = event.clientY;

        $('.drag-placeholder').hide();

        this.isDragging = false;

        // 判定当前鼠标落在哪个窗口
        var $windows = $('.window');
        var index;
        for (var i = 0; i < $windows.length; i++) {
            var rect = $windows[i].getBoundingClientRect();
            if (clientX > rect.left &&
                clientX < rect.right &&
                clientY > rect.top &&
                clientY < rect.bottom
            ) {
                index = i;
                break;
            }
        }

        if (index === undefined || this.sourceWindowIndex === null) {
            return;
        }
        if (index === this.sourceWindowIndex) {
            return;
        }

        var $sourceWindow = $('.window:eq(' + this.sourceWindowIndex + ')');
        var $targetWindow = $('.window:eq(' + index + ')');
        Util.swapElement($sourceWindow, $targetWindow);


        var dataDependency = this.dependency.get('data');
        var activeWindowIndex = dataDependency.getActiveWindowIndex();
        var subscribeVidArray = dataDependency.getSubscribVidArray();
        var mapInstanceArray = dataDependency.getMapInstanceArray();

        for (var i = 0; i < subscribeVidArray.length; i++) {
            var vidInfo = dataDependency.getSubscribVidInfo(subscribeVidArray[i]);
            var changed = false;
            if (vidInfo.windowIndex === this.sourceWindowIndex) {
                vidInfo.windowIndex = index;
                changed = true;
            } else if (vidInfo.windowIndex === index) {
                vidInfo.windowIndex = this.sourceWindowIndex;
                changed = true;
            }
            if (changed) {
                dataDependency.updateSubscribVid(vidInfo.vid, vidInfo, true);
            }
        }
        mapInstanceArray = Util.swapArrayElement(mapInstanceArray, index, this.sourceWindowIndex);
        dataDependency.setMapInstanceArray(mapInstanceArray);

        if (activeWindowIndex === this.sourceWindowIndex) {
            dataDependency.setActiveWindowIndex(index);
        }
        if (activeWindowIndex === index) {
            dataDependency.setActiveWindowIndex(this.sourceWindowIndex);
        }

        this.sourceWindowIndex = null;
    }
}

SmallWindow.prototype.toggleMapSettingVisible = function (event) {
    var $setting = $('.map-setting-container');
    $setting.toggleClass('visible');
    event.stopPropagation();

    $(document).one('click', function () {
        $setting.removeClass('visible');
    })
}

SmallWindow.prototype.onMapTypeClick = function (event) {
    var value = $(event.target).val();

    var dataDependency = this.dependency.get('data');
    dataDependency.setMapType(value);
}

SmallWindow.prototype.onMapTypeChange = function () {
    var dataDependency = this.dependency.get('data');

    var mapInstanceArray = dataDependency.getMapInstanceArray();
    var mapType = dataDependency.getMapType();
    var showTraffic = dataDependency.getShowTraffic();

    for (var i = 0; i < mapInstanceArray.length; i++) {
        var mapObj = mapInstanceArray[i];
        var map = mapObj.map;
        if (mapType === 'amap') {
            if (mapObj.satellite) {
                map.remove([mapObj.satellite,mapObj.roadNet]);
                mapObj.satellite = undefined;
                mapObj.roadNet = undefined;
            }
            if (mapObj.google) {
                map.remove(mapObj.google);
                mapObj.google = undefined;
            }
        } else if (mapType === 'satellite') {
            /* var satelliteLayer = AMap.createDefaultLayer({
                 tileUrl: 'https://mt{1,2,3,0}.google.cn/maps/vt?lyrs=s@194&hl=zh-CN&gl=cn&x=[x]&y=[y]&z=[z]', // 图块取图地址
                 zIndex: 100 //设置Google层级与高德相同  避免高德路况及卫星被Google图层覆盖
             });*/
            var satelliteLayer = new AMap.TileLayer.Satellite();
            satelliteLayer.setMap(map);
            mapObj.satellite = satelliteLayer;
            // 路网
            var roadNet = new AMap.TileLayer.RoadNet();
            roadNet.setMap(map);
            mapObj.roadNet = roadNet;
            if (mapObj.google) {
                map.remove(mapObj.google);
                mapObj.google = undefined;
            }
        } else if (mapType === 'google') {
            var googleMapLayer = new AMap.createDefaultLayer({
                tileUrl: 'http://mt{1,2,3,0}.google.cn/vt/lyrs=m@142&hl=zh-CN&gl=cn&x=[x]&y=[y]&z=[z]&s=Galil', // 图块取图地址
                zIndex: 100 //设置Google层级与高德相同  避免高德路况及卫星被Google图层覆盖
            });
            googleMapLayer.setMap(map);
            mapObj.google = googleMapLayer;
            if (mapObj.satellite) {
                map.remove([mapObj.satellite,mapObj.roadNet]);
                mapObj.satellite = undefined;
                mapObj.roadNet = undefined;
            }
        }

        mapInstanceArray[i] = mapObj;
    }

    dataDependency.setMapInstanceArray(mapInstanceArray);
}

SmallWindow.prototype.onTrafficChange = function () {
    var dataDependency = this.dependency.get('data');

    var mapInstanceArray = dataDependency.getMapInstanceArray();
    var showTraffic = dataDependency.getShowTraffic();

    for (var i = 0; i < mapInstanceArray.length; i++) {
        var mapObj = mapInstanceArray[i];
        var map = mapObj.map;

        if (showTraffic) {
            var realTimeTraffic = new AMap.TileLayer.Traffic({zIndex: 102});
            realTimeTraffic.setMap(map);
            mapObj.traffic = realTimeTraffic;
        } else if (mapObj.traffic) {
            map.remove(mapObj.traffic);
            mapObj.traffic = undefined;
        }
        mapInstanceArray[i] = mapObj;
    }

    dataDependency.setMapInstanceArray(mapInstanceArray);
}

SmallWindow.prototype.onShowTrafficClick = function (event) {
    var value = $(event.target).prop('checked');

    var dataDependency = this.dependency.get('data');
    dataDependency.setShowTraffic(value);
}

SmallWindow.prototype.removeSubscribeByButton = function (event) {
    event.preventDefault();
    event.stopPropagation();

    var dataDependency = this.dependency.get('data');
    var index = $(event.currentTarget).closest('.window').index();
    var subscribObjArray = dataDependency.getSubscribObjArray();
    var preiousVid;
    for (var j = 0; j < subscribObjArray.length; j++) {
        if (subscribObjArray[j].windowIndex === index) {
            preiousVid = subscribObjArray[j].vid;
            break;
        }
    }
    dataDependency.removeSubscribVid(preiousVid);
    var activeWindowIndex = dataDependency.getActiveWindowIndex();
    if (activeWindowIndex === index) {
        dataDependency.setActiveWindowIndex(null);
    }
}

SmallWindow.prototype.relocateAndTrack = function (event) {
    var dataDependency = this.dependency.get('data');

    var $target = $(event.currentTarget);
    var $window = $target.closest('.window');
    var index = $window.index();

    var subscribObjArray = dataDependency.getSubscribObjArray();
    var vidInfo;
    for (var j = 0; j < subscribObjArray.length; j++) {
        if (subscribObjArray[j].windowIndex === index) {
            vidInfo = subscribObjArray[j];
            break;
        }
    }
    if (vidInfo) {
        vidInfo.keepCenter = true;
        this.relocate(vidInfo.vid);
        // dataDependency.updateSubscribVid(vidInfo.vid, vidInfo);
    }
}

SmallWindow.prototype.relocate = function (vid) {
    var dataDependency = this.dependency.get('data');

    var vidInfo = dataDependency.getSubscribVidInfo(vid);
    if (!vidInfo) {
        return;
    }
    var mapInstanceArray = dataDependency.getMapInstanceArray();

    var windowIndex = vidInfo.windowIndex;
    var map = mapInstanceArray[windowIndex].map;
    if (Util.noUndefineOrNull(vidInfo.basicInfo.longitude) && Util.noUndefineOrNull(vidInfo.basicInfo.latitude)) {
        var position = new AMap.LngLat(vidInfo.basicInfo.longitude, vidInfo.basicInfo.latitude);
        map.setCenter(position);
    }
}