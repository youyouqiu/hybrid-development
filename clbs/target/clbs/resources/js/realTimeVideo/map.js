define(['common'], function (Common) {
    var mapModule; // 地图实例化对象
    var subscribeInfoMap = new Common.map(); // 订阅信息集合
    var monitorMarkerMap = new Common.map(); // 监控对象marker集合
    var monitorNameMarkerMap = new Common.map(); // 监控对象名称marker集合
    var currentMonitorId; // 当前显示监控对象id
    var mapBounds; // 地图可视区域范围
    var map = {
        /**
         * 地图初始化
         */
        init: function () {
            mapModule = new AMap.Map("map-module", {
                resizeEnable: true,		// 是否监控地图容器尺寸变化
                zoom: 18				// 地图显示的缩放级别
            });
            mapModule.on('click', this.onMapClick);
            mapModule.on('moveend', this.getMapArea);
            mapModule.on('zoomchange', this.mapZoomChange.bind(this));
        },
        /**
         * 地图点击事件
         */
        onMapClick: function () {

        },
        /**
         * 获取地图可视区域范围
         */
        getMapArea: function () {
            var southWest = mapModule.getBounds().getSouthWest(); // 西南角
            var northEast = mapModule.getBounds().getNorthEast(); // 东北角
            var neLng = northEast.lng - ((northEast.lng - southWest.lng) * 0.2); // 东北角经度
            var neLat = northEast.lat - ((northEast.lat - southWest.lat) * 0.2); // 东北角纬度
            var swLng = southWest.lng + ((northEast.lng - southWest.lng) * 0.2); // 西南角经度
            var swLat = southWest.lat + ((northEast.lat - southWest.lat) * 0.2); // 西南角纬度
            var southWestValue = [swLng, swLat];
            var northEastValue = [neLng, neLat];
            mapBounds = new AMap.Bounds(southWestValue, northEastValue);
        },
        /**
         * 地图层级改变事件
         */
        mapZoomChange: function () {
            if (monitorMarkerMap.has(currentMonitorId) && subscribeInfoMap.has(currentMonitorId)) {
                var marker = monitorMarkerMap.get(currentMonitorId);
                var subscribeInfo = subscribeInfoMap.get(currentMonitorId);
                var location = marker.getPosition();
                var data = subscribeInfo[subscribeInfo.length - 1];
                data.longitude = location.lng;
                data.latitude = location.lat;
                this.createMonitorName(data);
            }
            this.getMapArea();
        },
        /**
         * 高德地图-创建marker前数据组装
         */
        setMarkersData: function (data) {
            var msgBody = data.data.msgBody;
            var info = {
                monitorId: msgBody.monitorInfo.monitorId, // 监控对象id
                vehicleName: msgBody.monitorInfo.monitorName, // 监控对象名称
                longitude: msgBody.longitude, // 经度
                latitude: msgBody.latitude, // 纬度
                monitorIcon: msgBody.monitorInfo.monitorIcon, // 监控对象图标
                stateInfo: msgBody.stateInfo, // 监控对象状态
                time: this.timeTransform(msgBody.gpsTime), // 定位时间
                direction: Number(msgBody.direction) + 270, // 角度
            };
            this.updateMarkerData(info);
        },
        /**
         * 时间格式转换
         */
        timeTransform: function (time) {
            if (time) {
                return 20 + time.substring(0, 2) + "-" + time.substring(2, 4) + "-" + time.substring(4, 6) + " " +
                    time.substring(6, 8) + ":" + time.substring(8, 10) + ":" + time.substring(10, 12);
            }
            return '';
        },
        /**
         * 更新位置点数据
         */
        updateMarkerData: function (data) {
            var monitorId = data.monitorId;

            if (subscribeInfoMap.has(monitorId)) {
                var value = subscribeInfoMap.get(monitorId);
                value.push(data);
                subscribeInfoMap.set(monitorId, value);
            } else {
                subscribeInfoMap.set(monitorId, [data]);
            }

            if (!monitorMarkerMap.has(monitorId)) {
                this.createMonitorMarker(data);
                this.createMonitorName(data);
            }

            var monitorInfo = subscribeInfoMap.get(monitorId);
            if (monitorInfo.length === 2) {
                this.markerMove(monitorId);
            }
        },
        /**
         * 创建监控对象marker图标
         */
        createMonitorMarker: function (data) {
            var $this = this;
            var icons; // 图片marker路劲
            if (data.monitorIcon === 'null' || data.monitorIcon === undefined || data.monitorIcon === null) {
                icons = '../../resources/img/vehicle.png';
            } else {
                icons = '../../resources/img/vico/' + data.monitorIcon;
            }
            var marker = new AMap.Marker({
                position: [data.longitude, data.latitude],
                icon: icons,
                offset: new AMap.Pixel(-29, -13), //相对于基点的位置
                autoRotation: true,//自动调节图片角度
                map: mapModule,
                angle: Number(data.direction),
            });
            marker.on('moving', function (info) {
                $this.markerMoving(data.monitorId, info);
            });
            // marker 移动结束事件
            marker.on('moveend', function () {
                $this.markerMoveend(data.monitorId);
            });
            mapModule.setCenter([data.longitude, data.latitude]);
            monitorMarkerMap.set(data.monitorId, marker);
        },
        /**
         * 创建监控对象名称marker
         */
        createMonitorName: function (data) {
            var picWidth = 29;
            var picHeight = 13;
            var state = this.monitorStateClass(data.stateInfo);

            /**
             * 监控对象旋转角度
             */
            var angle = 0;
            if (monitorMarkerMap.has(data.monitorInfo)) {
                var marker = monitorMarkerMap.get(data.monitorId);
                angle = marker.getAngle();
                if (angle > 360) {
                    var i = Math.floor(angle / 360);
                    angle = angle - 360 * i;
                }
            }

            /**
             * 将经纬度转为像素
             */
            var pixel = mapModule.lngLatToContainer([data.longitude, data.latitude]);
            var pixelX = pixel.getX();
            var pixelY = pixel.getY();
            var pixelPX = [pixelX, pixelY];

            /**
             * 得到车辆图标四个角的像素点(假设车图标永远正显示)58*26
             */
            var defaultRD = [pixelX + picWidth, pixelY + picHeight];//右下

            /**
             * 计算后PX
             */
            var pixelRD = this.countAnglePX(angle, defaultRD, pixelPX, picWidth, picHeight);

            /**
             * 像素转为经纬度
             */
            var llRD = mapModule.containerToLngLat(new AMap.Pixel(pixelRD[0], pixelRD[1]));

            var content = "<p class='carNameShowRD'><i class='" + state + "'></i>&ensp;<span class='monitorNameBox'>" + data.vehicleName + "</span></p>";
            if (monitorNameMarkerMap.has(data.monitorId)) {
                /**
                 * 更新监控对象名称标注物
                 */
                var marker = monitorNameMarkerMap.get(data.monitorId);
                marker.setContent(content);
                marker.setPosition(llRD);
            } else {
                /**
                 * 创建监控对象名称标注物
                 */
                var monitorNameMarker = new AMap.Marker({
                    position: llRD,
                    content: content,
                    offset: new AMap.Pixel(0, 0),
                    autoRotation: true,
                    map: mapModule,
                    zIndex: 999
                });
                monitorNameMarkerMap.set(data.monitorId, monitorNameMarker);
            }
        },
        /**
         * 根据车辆状态返回对应的类名
         */
        monitorStateClass: function (state) {
            switch (state) {
                case 4:
                    return 'carStateRun';
                    break;
                case 10:
                    return 'carStateRun';
                    break;
                case 5:
                    return 'carStateRun';
                    break;
                case 2:
                    return 'carStateRun';
                    break;
                case 3:
                    return 'carStateOffLine';
                    break;
                case 9:
                    return 'carStateRun';
                    break;
                case 11:
                    return 'carStateheartBeat';
                    break;
            }
            ;
        },
        /**
         * 计算车牌号四个定点的像素坐标
         */
        countAnglePX: function (angle, pixel, centerPX, picWidth, picHeight) {
            var thisPX;
            var thisX;
            var thisY;
            if ((angle <= 45 && angle > 0) || (angle > 180 && angle <= 225) || (angle >= 135 && angle < 180) || (angle >= 315 && angle < 360)) {
                angle = 0;
            }
            if ((angle < 90 && angle > 45) || (angle < 270 && angle > 225) || (angle > 90 && angle < 135) || (angle > 270 && angle < 315)) {
                angle = 90;
            }
            if (angle === 90 || angle === 270) {
                thisX = centerPX[0] + picHeight;
                thisY = centerPX[1] + picWidth;
            }
            if (angle === 0 || angle === 180 || angle === 360) {
                thisX = pixel[0];
                thisY = pixel[1];
            }
            thisPX = [thisX, thisY];
            return thisPX;
        },
        /**
         * 监控对象-marker移动
         */
        markerMove: function (monitorId) {
            /**
             * 地图只显示当前监控对象，非当前监控对象不做平滑移动和调点处理
             */
            if (currentMonitorId !== monitorId) return false;
            var monitorInfo = subscribeInfoMap.get(monitorId);
            /**
             * 判断目标位置点经纬度与当前位置经纬度是否相等
             * 若相等直接将相同点移除
             * 然后重新进行位置移动
             */
            var currentData = monitorInfo[0];
            var targetData = monitorInfo[1];
            var lastPointData = monitorInfo[monitorInfo.length - 1];
            var currentTime = new Date(currentData.time).getTime();
            var targetTime = new Date(targetData.time).getTime();
            var lastPointTime = new Date(lastPointData.time).getTime();
            var timeDiff = (lastPointTime - currentTime) / 1000 / 60;
            /**
             * 首先判断当前点和最后一个点的时间间隔是否小于一分钟
             * 小于一分钟正常执行移动，大于等于一分钟就跳转到最后一个点
             */
            if (timeDiff < 1) {
                if (currentData.longitude === targetData.longitude && currentData.latitude === targetData.latitude) {
                    monitorInfo.splice(0, 1);
                    subscribeInfoMap.set(monitorId, monitorInfo);
                    if (monitorInfo.length >= 2) {
                        this.markerMove(monitorId);
                    }
                } else {
                    var marker = monitorMarkerMap.get(monitorId);
                    var distance = this.getDistance(currentData, targetData);
                    var speed = this.getMoveSpeed(distance, targetTime - currentTime);
                    /**
                     * 速度大于等于230删除目标位置点，marker保持不变
                     */
                    if (speed >= 230) {
                        monitorInfo.splice(0, 1);
                        subscribeInfoMap.set(monitorId, monitorInfo);
                    } else {
                        var angle = this.calcAngle([currentData.longitude, currentData.latitude], [targetData.longitude, targetData.latitude]) + 360;
                        marker.setAngle(angle);
                        var markerTime = (targetTime - currentTime) || 0;
                        marker.moveTo([targetData.longitude, targetData.latitude], {
                            duration: markerTime,
                            delay: 100,
                            autoRotation: false
                        });

                    }
                }
            } else {
                var data = monitorInfo.pop();
                subscribeInfoMap.set(monitorId, [data]);
                this.monitorJumpPoint(monitorId);
            }
        },
        // 获取两个点旋转角度
        calcAngle: function (startPos, endPos) {
            const p_start = mapModule.lngLatToContainer(startPos);
            const p_end = mapModule.lngLatToContainer(endPos);
            const diff_x = p_end.x - p_start.x;
            const diff_y = p_end.y - p_start.y;
            return 360 * Math.atan2(diff_y, diff_x) / (2 * Math.PI);
        },
        /**
         * 监控对象-marker移动监听事件
         */
        markerMoving: function (id, info) {
            if (subscribeInfoMap.has(id)) {
                var subscribeInfo = subscribeInfoMap.get(id);
                var data = subscribeInfo[1];
                data.longitude = info.passedPath[1].lng;
                data.latitude = info.passedPath[1].lat;
                /**
                 * 判断当前点是否在地图可以范围内
                 * 不在就重新获取
                 */
                var movelnglat = [info.passedPath[1].lng, info.passedPath[1].lat];
                if (mapBounds && !mapBounds.contains(movelnglat)) {
                    mapModule.setCenter(movelnglat);
                    this.getMapArea();
                }
                this.createMonitorName(data)
            }
        },
        /**
         * 监控对象-marker移动结束事件
         */
        markerMoveend: function (id) {
            var monitorInfo = subscribeInfoMap.get(id);
            monitorInfo.splice(0, 1);
            subscribeInfoMap.set(id, monitorInfo);
            /**
             * 积压点大于3个,直接跳到最新点
             */
            if (monitorInfo.length > 3) {
                monitorInfo.splice(0, monitorInfo.length - 1);
                subscribeInfoMap.set(id, monitorInfo);
                this.monitorJumpPoint(id);
            } else if (monitorInfo.length >= 2) {
                this.markerMove(id);
            }
        },
        /**
         * 监控对象-获取两位置点间的距离
         */
        getDistance: function (currentPoint, targetPoint) {
            var current = new AMap.LngLat(currentPoint.longitude, currentPoint.latitude);
            var target = new AMap.LngLat(targetPoint.longitude, targetPoint.latitude);
            return current.distance(target);
        },
        /**
         * 监控对象-获取两位置点间的速度
         */
        getMoveSpeed: function (distance, time) {
            var speed = 300;
            if (distance != null) {
                var mileage = distance / 1000;
                var mTime = time / 1000 / 60 / 60;
                if (mTime === 0) {
                    speed = 50;
                } else {
                    speed = Number((mileage / mTime).toFixed(2));
                }
            }
            return speed === 0 ? 100 : speed;
        },
        /**
         * 监控对象-跳点
         */
        monitorJumpPoint: function (id) {
            var marker = monitorMarkerMap.get(id);
            var nameMarker = monitorNameMarkerMap.get(id);
            var monitorInfo = subscribeInfoMap.get(id);
            marker.setPosition([monitorInfo[0].longitude, monitorInfo[0].latitude]);
            nameMarker.setPosition([monitorInfo[0].longitude, monitorInfo[0].latitude]);
            this.getMapArea();
        },
        /**
         * 监控对象、监控对象名称marker隐藏
         */
        monitorHide: function (id) {
            /**
             * 隐藏监控对象
             */
            if (monitorMarkerMap.has(id)) {
                var marker = monitorMarkerMap.get(id);
                marker.stopMove();
                marker.hide();
            }
            /**
             * 隐藏监控对象名称
             */
            if (monitorNameMarkerMap.has(id)) {
                var nameMarker = monitorNameMarkerMap.get(id);
                nameMarker.hide();
            }
        },
        /**
         * 监控对象、监控对象名称marker显示
         */
        monitorShow: function (id) {
            if (monitorMarkerMap.has(id) && subscribeInfoMap.has(id) && monitorNameMarkerMap.has(id)) {
                var marker = monitorMarkerMap.get(id);
                var data = subscribeInfoMap.get(id);
                var nameMarker = monitorNameMarkerMap.get(id);
                /**
                 * 显示监控对象
                 */
                marker.show();
                marker.setPosition([data[data.length - 1].longitude, data[data.length - 1].latitude]);
                mapModule.setCenter([data[data.length - 1].longitude, data[data.length - 1].latitude]);
                /**
                 * 显示监控对象名称
                 */
                nameMarker.show();
                this.createMonitorName(data[data.length - 1]);
                /**
                 * 只保留最后一个位置数据
                 */
                subscribeInfoMap.set(id, [data[data.length - 1]]);
            }
        },
        /**
         * 设置当前地图显示监控对象id
         */
        setCurrentMonitorId: function (id) {
            currentMonitorId = id;
        },
        /**
         * 获取当前地图显示监控对象id
         */
        getCurrentMonitorId: function () {
            return currentMonitorId;
        },
        /**
         * 判断指定id是否在订阅集合中
         */
        isSubscribe: function (id) {
            return subscribeInfoMap.has(id);
        },
        /**
         * 监控对象取消订阅后对数据进行清空
         */
        clearData: function (id) {
            /**
             * 清空监控对象标注物（数据集合和地图显示）
             */
            if (monitorMarkerMap.has(id)) {
                var marker = monitorMarkerMap.get(id);
                marker.stopMove();
                mapModule.remove([marker]);
                monitorMarkerMap.remove(id);
            }

            /**
             * 清空监控对象名称标注物（数据集合和地图显示）
             */
            if (monitorNameMarkerMap.has(id)) {
                var monitorNameMarker = monitorNameMarkerMap.get(id);
                mapModule.remove([monitorNameMarker]);
                monitorNameMarkerMap.remove(id);
            }

            /**
             * 清空位置信息点数据
             */
            subscribeInfoMap.remove(id);

            /**
             *  重置当前监控对象id
             */
            if (currentMonitorId === id) {
                currentMonitorId = null;
            }
        },
        /**
         * 返回订阅集合
         */
        getSubscribeInfoMap: function () {
            return subscribeInfoMap;
        },
    };
    return {
        map: map,
    }
})