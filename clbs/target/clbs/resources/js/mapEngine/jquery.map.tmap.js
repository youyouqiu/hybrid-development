;(function ($) {
    var _tmap = function (containerId) {
        this.mapObj = new T.Map(containerId, {minZoom: 4});
        this.mapObj.currentMap = 'tMap';
        this.mapObj.enableDrag();
        this.mapObj.enableScrollWheelZoom();

        /**
         * 天地图转换高德地图经纬度
         * */
        this.mapObj.tianToGaoLnglat = function (lng, lat) {
            var a = 6378245.0; //  a: 卫星椭球坐标投影到平面地图坐标系的投影因子。
            var ee = 0.00669342162296594323; //  ee: 椭球的偏心率。
            var mars_point = [];
            if (this.outOfChina(lng, lat)) {
                mars_point[1] = lat;
                mars_point[0] = lng;
                return mars_point;
            }
            var dLat = this.transformLat(lng - 105.0, lat - 35.0);
            var dLon = this.transformLon(lng - 105.0, lat - 35.0);
            var radLat = lat / 180.0 * PI;
            var magic = Math.sin(radLat);
            magic = 1 - ee * magic * magic;
            var sqrtMagic = Math.sqrt(magic);
            dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * PI);
            dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * PI);
            mars_point[1] = lat + dLat;
            mars_point[0] = lng + dLon;
            return mars_point;
        };
        this.mapObj.lnglatTransToAmap = this.mapObj.tianToGaoLnglat;
        /**
         * 判断是否在国内，不在国内则不做偏移
         * */
        this.mapObj.outOfChina = function (lng, lat) {
            if ((lng < 72.004 || lng > 137.8347) && (lat < 0.8293 || lat > 55.8271)) {
                return true;
            } else {
                return false;
            }
        };
        this.mapObj.transformLat = function (x, y) {
            var ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
            ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
            ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
            ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
            return ret;
        };
        this.mapObj.transformLon = function (x, y) {
            var ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
            ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
            ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
            ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
            return ret;
        };
        /**
         * 高德地图经纬度转换天地图经纬度
         * */
        this.mapObj.gaoToTianLnglat = function (lng, lat) {
            var a = 6378245.0; //  a: 卫星椭球坐标投影到平面地图坐标系的投影因子。
            var ee = 0.00669342162296594323; //  ee: 椭球的偏心率。
            var lat1 = +lat;
            var lng1 = +lng;
            var dlat = this.transformLat(lng1 - 105.0, lat1 - 35.0);
            var dlng = this.transformLon(lng1 - 105.0, lat1 - 35.0);
            var radlat = lat1 / 180.0 * PI;
            var magic = Math.sin(radlat);
            magic = 1 - ee * magic * magic;
            var sqrtmagic = Math.sqrt(magic);
            dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * PI);
            dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * PI);
            var mglat = lat1 + dlat;
            var mglng = lng1 + dlng;
            var latlng = [];
            latlng[0] = lng1 * 2 - mglng;
            latlng[1] = lat1 * 2 - mglat;
            return latlng;
        };
        this.mapObj.lnglatTransFun = this.mapObj.gaoToTianLnglat;

        if (window.initCenterPoint) {
            var point = this.mapObj.gaoToTianLnglat(initCenterPoint.lng, initCenterPoint.lat);
            var point = new T.LngLat(point[0], point[1]);
            this.mapObj.centerAndZoom(point, 18);
        } else {
            this.mapObj.centerAndZoom(new T.LngLat(116.40769, 34.89945), 18);
        }

        /*this.mapObj.oldon = this.mapObj.on;
        this.mapObj.oldoff = this.mapObj.off;
        this.mapObj.on = function (event, callback, t) {
            if (event === 'rightclick') event = 'contextmenu';
            this.oldon(event, callback, t);
        };
        this.mapObj.off = function (event, callback, t) {
            if (event === 'rightclick') event = 'contextmenu';
            this.oldoff(event, callback, t);
        };*/

        /**
         * 标准地图
         */
        this.mapObj.standardMap = function () {
            satellLayer.hide();
            $('.hawkEyeBtn').prop('disabled', false);
            if (overView.isOpen()) {
                $('.hawkEyeBtn').addClass('preBlue')
            }
            if (this.hasDimensionalMap) {
                this.dimensionalMapToggle().hide();
            }
        };
        /**
         * 卫星地图
         */
        this.mapObj.satelliteMap = function () {
            satellLayer.show();
            $('.hawkEyeBtn').prop('disabled', false);
            if (overView.isOpen()) {
                $('.hawkEyeBtn').addClass('preBlue')
            }
            this.dimensionalMapToggle().hide();
        };

        /**
         * 三维地球
         * */
        this.mapObj.dimensionalMap = function () {
            this.dimensionalMapToggle().show();
            $('.hawkEyeBtn').removeClass('preBlue').prop('disabled', true);
        };

        this.mapObj.setCenter = function (point) {
            if (point[0]) {
                var newPoint = this.gaoToTianLnglat(point[0], point[1]);
                point = new T.LngLat(newPoint[0], newPoint[1]);
            } else if (point.lng) {
                point = new T.LngLat(point.lng, point.lat);
            }
            this.goDimensionalPosition(point);
            this.centerAndZoom(point);
        };

        /**
         * 显示覆盖物至地图可视范围
         * */
        this.mapObj.setFitView = function (option) {
            if (option.getLngLats) {
                var arr = option.getLngLats();
                if (arr[0] instanceof Array) {
                    this.setViewport(arr[0]);
                } else {
                    this.setViewport(arr);
                }
            } else if (option.getLngLat) {
                this.setViewport([option.getLngLat()]);
            } else if (option.getCenter) {
                this.setViewport([option.getCenter()]);
            } else if (option[0]) {
                this.setViewport(option[0].getLngLats());
            } else {
                this.setViewport(option);
            }
        };
        /**
         * 输入提示
         */
        this.mapObj.autoComplete = function (config) {
            var listenerList = {
                'select': function () {
                },
            };
            var event = new $.fn.mapEngine.createEvent(listenerList);
            return event;
        };

        /**
         * 关闭信息弹窗
         */
        this.mapObj.clearInfoWindow = function () {
            if (infoWindow && infoWindow.isOpen()) {
                infoWindow.closeInfoWindow();
            }
        };

        /**
         * 标准经纬度创建
         * @param lng
         * @param lat
         * @returns {T.LngLat}
         */
        this.mapObj.lngLat = function (lng, lat) {
            var obj = new T.LngLat(lng, lat);
            obj.distance = obj.distanceTo;
            return obj;
        };

        /**
         * 创建文本标注
         * @param param
         * @returns {T.Label}
         */
        this.mapObj.text = function (param) {
            return new T.Label({
                text: param.text,
                position: param.position,
            });
        };

        /**
         * 创建聚合点
         * @param map
         * @param points
         * @param option
         * @returns {T.MarkerClusterer}
         */
        this.mapObj.markerCluster = function (map, points, option) {
            var arrayObj = [];
            var icon = new T.Icon({
                iconUrl: "/clbs/resources/img/1.png",
                iconAnchor: new T.Point(0, -10)
            });
            for (var i = 0; i < points.length; i++) {
                var marker = new T.Marker(new T.LngLat(points[i].lnglat[0], points[i].lnglat[1]), {
                    title: i,
                    icon: icon
                });
                arrayObj.push(marker);
            }
            var markers = new T.MarkerClusterer(map, {
                markers: arrayObj,
                girdSize: option.gridSize,
                maxZoom: option.maxZoom
            });

            markers.setMap = function (status) {
                if (!status) {
                    markers.clearMarkers();
                }
            };

            return markers;
        };

        /**
         * 设置偏移量
         * @param pix1
         * @param pix2
         * @returns {Point|boolean|t|t}
         */
        this.mapObj.pixel = function (pix1, pix2) {
            return new T.Point(pix1, pix2);
        };

        /**
         * 设置一个矩形区域
         * @param pix1
         * @param pix2
         */
        this.mapObj.bounds = function (pix1, pix2) {
            var map = this;
            var bounds = new T.LngLatBounds(pix1, pix2);
            if (pix1[0]) {
                bounds = new T.LngLatBounds({lng: pix1[0], lat: pix1[1]}, {lng: pix2[0], lat: pix2[1]});
            }
            if (pix1 === 0) {
                bounds.contains = function (lnglat) {
                    return true;
                };
            } else {
                bounds.oldContains = bounds.contains;
                bounds.contains = function (lnglat) {
                    var point = new T.LngLat(0, 0);
                    if (lnglat[0]) {
                        point = new T.LngLat(lnglat[0], lnglat[1]);
                    } else if (lnglat.lng) {
                        point = new T.LngLat(lnglat.lng, lnglat.lat);
                    }
                    // 三维地球显示时,默认都在范围内
                    if (map.dimensionalVisible()) {
                        return true;
                    }
                    return this.oldContains(point);
                };
            }

            return bounds;
        };

        /**
         * 添加谷歌卫星图层
         * @param option
         */
        this.mapObj.createDefaultLayer = function (option) {
            var lay = new T.TileLayer.WMS(option.tileUrl, {zIndex: option.zIndex});
            lay.setMap = function (opt) {
                this.addLayer(lay);
            };
            lay.hide = function () {

            };
            return lay;
        };

        /**
         * 获取三维地球下展示的车辆状态颜色值
         * */
        this.mapObj.stateCallBack = function (stateInfo) {
            var state;
            switch (stateInfo) {
                case 'carStateStop':
                    state = new Cesium.Color(200 / 255, 0, 2 / 255, 1);
                    break;
                case 'carStateRun':
                    state = new Cesium.Color(120 / 255, 170 / 255, 58 / 255, 1);
                    break;
                case 'carStateAlarm':
                    state = new Cesium.Color(1, 171 / 255, 45 / 255, 1);
                    break;
                case 'carStateMiss':
                    state = new Cesium.Color(117 / 255, 72 / 255, 1 / 255, 1);
                    break;
                case 'carStateOffLine':
                    state = new Cesium.Color(182 / 255, 182 / 255, 182 / 255, 1);
                    break;
                case 'carStateOverSpeed':
                    state = new Cesium.Color(150 / 255, 11 / 255, 163 / 255, 1);
                    break;
                case 'carStateheartbeat':
                    state = new Cesium.Color(251 / 255, 140 / 255, 150 / 255, 1);
                    break;
            }
            return state;
        };

        /**
         * 三维地球是否显示
         * */
        this.mapObj.dimensionalVisible = function () {
            var map = this;
            return map.hasDimensionalMap && $('#dimensionalMapContainer').is(':visible');
        };

        /**
         * 三维地球,中心位置跳转
         * */
        this.mapObj.goDimensionalPosition = function (point) {
            var map = this;
            if (map.dimensionalVisible()) {
                map.hasDimensionalMap.camera.flyTo({
                    destination: new Cesium.Cartesian3.fromDegrees(point.lng, point.lat, 1000),
                    duration: 1
                })
            }
        };
        /**
         * 绘制三维地球下的marker图标
         * */
        this.mapObj.dimensionalMarker = function (option) {
            if (map.hasDimensionalMap) {
                var viewer = map.hasDimensionalMap;
                var citizensBankPark = viewer.entities.add({
                    name: option.name,
                    id: option.id,
                    position: Cesium.Cartesian3.fromDegrees(option.position[0], option.position[1]),
                    point: { //点
                        pixelSize: 10,
                        color: map.stateCallBack(option.state),
                        outlineColor: Cesium.Color.WHITE,
                        outlineWidth: 2
                    },
                    label: { //文字标签
                        text: option.name,
                        font: '14pt 微软雅黑',
                        style: Cesium.LabelStyle.FILL,
                        outlineWidth: 2,
                        showBackground: true,
                        fillColor: new Cesium.Color(0, 0, 0, 1),
                        backgroundColor: new Cesium.Color(1, 1, 1, 0.8),
                        backgroundPadding: new Cesium.Cartesian2(6, 6),
                        verticalOrigin: Cesium.VerticalOrigin.BOTTOM, //垂直方向以底部来计算标签的位置
                        pixelOffset: new Cesium.Cartesian2(30, -18)   //偏移量
                    },
                    /*billboard: { //图标
                        image: option.icon,
                        /!*width: 50,
                        height: 30*!/
                    }*/
                });
                viewer.zoomTo(viewer.entities.values);
            }
        };

        /**
         * 创建标注点
         * @param option
         * @returns {T.Marker}
         */
        this.mapObj.marker = function (option) {
            var map = this;
            var marker;
            if (option.icon) {
                var icon = new T.Icon({
                    iconUrl: option.icon,
                    // iconSize: new T.Point(19, 27),
                    iconAnchor: new T.Point(0, -10)
                });
                var pos = option.position;
                var position = map.gaoToTianLnglat(option.position[0], option.position[1]);
                marker = new T.Marker(pos.lng ? pos : new T.LngLat(position[0], position[1]), {
                    icon: icon,
                });
                marker.extData = option.id;
                if (option.label) {
                    var label = new T.Label({
                        text: option.label.content,
                        position: marker.getLngLat(),
                        offset: new T.Point(0, 30)
                    });
                    marker.labelMarker = label;
                    map.addOverLay(label);
                }
                this.dimensionalMarker(option);
            } else if (!option.content) {
                var newPoint = map.gaoToTianLnglat(option.position[0], option.position[1]);
                marker = new T.Marker(new T.LngLat(newPoint[0], newPoint[1]));
            } else {
                var newPoint = map.gaoToTianLnglat(option.monitorPos[0], option.monitorPos[1]);
                var pos = new T.LngLat(newPoint[0], newPoint[1]);
                if (option.monitorPos.lng) {
                    pos = new T.LngLat(option.monitorPos.lng, option.monitorPos.lat);
                }
                marker = new T.Label({
                    text: option.content,
                    position: pos,
                    offset: new T.Point(40, 50),
                });
                $('.tdt-overlay-pane .tdt-label').css({background: 'none', boxShadow: 'none', border: 'none'});
            }
            marker.oldsetIcon = marker.setIcon;
            marker.setIcon = function (url) {
                if (typeof url === 'string') {
                    var icon = new T.Icon({
                        iconUrl: url,
                        iconSize: new T.Point(32, 32),
                    });
                    this.oldsetIcon(icon);
                } else {
                    this.oldsetIcon(url);
                }
            };
            // 点标记可拖拽
            marker.setDraggable = function (status) {
                if (status) {
                    this.enableDragging();
                } else {
                    this.disableDragging();
                }
            };
            marker.show = function () {
                map.addOverLay(this);
            };
            marker.hide = function (status) {
                if (!status) {
                    map.removeOverLay(this);
                }
            };
            marker.setMap = function (map) {
                if (map) {
                    map.addOverLay(this);
                }
            };
            marker.setAngle = function () {

            };
            marker.getAngle = function () {

            };
            marker.stopMove = function () {

            };
            marker.getPosition = function () {
                return marker.getLngLat();
            };
            marker.setOffset = function () {

            };
            var _this = this;
            marker.moveTo = function (point) {
                this.setPosition(point);
                if (amapOperation) {
                    // amapOperation.markerMoveendFun(this, this.extData);
                    // 判断是否为聚焦跟踪监控对象
                    if (markerFocus && this.extData && markerFocus == marker.extData) {
                        var msg = this.getPosition();
                        if (!pathsTwo.contains(msg)) {
                            _this.setCenter(msg);
                            amapOperation.LimitedSizeTwo();
                        }
                    }
                }
            };
            marker.setPosition = function (point) {
                var newPoint;
                if (point.lng) {
                    newPoint = [point.lng, point.lat];
                    marker.setLngLat(new T.LngLat(point.lng, point.lat));
                } else if (point[0]) {
                    newPoint = map.gaoToTianLnglat(point[0], point[1]);
                    marker.setLngLat(new T.LngLat(newPoint[0], newPoint[1]));
                }
                if (this.extData) {
                    amapOperation.markerMoveendFun(this, this.extData);
                }
                // 三维地球移动
                if (marker.extData && map.dimensionalVisible()) {
                    var viewer = map.hasDimensionalMap;
                    viewer.entities.removeById(marker.extData);
                    viewer.entities.add({
                        name: option.name,
                        id: option.id,
                        position: Cesium.Cartesian3.fromDegrees(newPoint[0], newPoint[1]),
                        point: { //点
                            pixelSize: 10,
                            color: map.stateCallBack(option.state),
                            outlineColor: Cesium.Color.WHITE,
                            outlineWidth: 2
                        },
                        label: { //文字标签
                            text: option.name,
                            font: '14pt 微软雅黑',
                            style: Cesium.LabelStyle.FILL,
                            outlineWidth: 2,
                            showBackground: true,
                            fillColor: new Cesium.Color(0, 0, 0, 1),
                            backgroundColor: new Cesium.Color(1, 1, 1, 0.8),
                            backgroundPadding: new Cesium.Cartesian2(6, 6),
                            verticalOrigin: Cesium.VerticalOrigin.BOTTOM, //垂直方向以底部来计算标签的位置
                            pixelOffset: new Cesium.Cartesian2(30, -18)   //偏移量
                        },
                    });
                }
            };
            // 添加文字标签
            marker.setLabel = function (obj) {
                var label = new T.Label({
                    text: obj.content,
                    position: marker.getLngLat(),
                    offset: new T.Point(0, 30)
                });
                if (marker.labelMarker) {
                    map.remove([marker.labelMarker]);
                }
                marker.labelMarker = label;
                map.addOverLay(label);
            };
            this.addOverLay(marker);
            // 修改样式
            return marker;
        };

        /**
         * 行政区域查询
         * @param option
         */
        this.mapObj.districtSearch = function (option) {
            var listenerList = {};
            var event = new $.fn.mapEngine.createEvent(listenerList);

            event.search = function (word, callback) {
                /*var administrative = new T.AdministrativeDivision();
                administrative.search({
                    searchType: 1,
                    needPolygon: true,
                    searchWord: word
                }, function (result) {
                    var pointArr = [];
                    var boundaries = result.data[0].bound.split(',');
                    console.log('bound', result.getData(), boundaries);
                    for (var i = 0; i < boundaries.length; i++) {
                        pointArr.push([boundaries[i], boundaries[i + 1]]);
                        i++;
                    }
                    callback('complete', {
                        districtList: [{
                            boundaries: [pointArr]
                        }]
                    })
                });*/
            };
            event.setLevel = function () {

            };
            event.setExtensions = function () {

            };
            return event;
        };

        this.mapObj.mouseTool = function (map) {
            var handler;
            var listenerList = {
                'draw': function () {
                },
            };
            var event = new $.fn.mapEngine.createEvent(listenerList);
            // event.on = function (event, callback) {
            //     this.onDraw = callback;
            // };
            var _this = this;
            /**
             *  画标注
             */
            event.marker = function () {
                if (handler) handler.close();
                handler = new T.MarkTool(map);
                handler.on('mouseup', function (e) {
                    var currentMarker = e.currentMarker;
                    currentMarker.CLASS_NAME = 'AMap.Marker';
                    currentMarker.getPosition = function () {
                        var lngLat = e.currentLnglat;
                        var newPos = _this.tianToGaoLnglat(lngLat.lng, lngLat.lat);
                        return {
                            lng: newPos[0],
                            lat: newPos[1],
                        };
                    };
                    var result = {
                        obj: currentMarker
                    };
                    event.trigger('draw', result);
                });
                handler.open();
            };

            /**
             * 画多边形
             */
            event.polygon = function () {
                if (handler) handler.close();
                handler = new T.PolygonTool(map);
                handler.on('draw', function (e) {
                    var currentPolygon = e.currentPolygon;
                    currentPolygon.CLASS_NAME = 'Overlay.Polygon';
                    currentPolygon.getPath = function () {
                        var newPath = [];
                        var path = e.currentLnglats;
                        for (var i = 0; i < path.length; i++) {
                            var newPoint = _this.tianToGaoLnglat(path[i].lng, path[i].lat);
                            newPath.push({
                                lng: newPoint[0],
                                lat: newPoint[1]
                            })
                        }
                        return newPath;
                    };
                    var result = {
                        obj: currentPolygon
                    };
                    event.trigger('draw', result);
                });
                handler.open();
            };

            /**
             * 画线
             */
            event.polyline = function () {
                if (handler) handler.close();
                handler = new T.PolylineTool(map);
                handler.on('draw', function (e) {
                    var currentPolyline = e.currentPolyline;
                    currentPolyline.CLASS_NAME = 'Overlay.Polyline';
                    currentPolyline.getPath = function () {
                        var newPath = [];
                        var path = e.currentLnglats;
                        for (var i = 0; i < path.length; i++) {
                            var newPoint = _this.tianToGaoLnglat(path[i].lng, path[i].lat);
                            newPath.push({
                                lng: newPoint[0],
                                lat: newPoint[1]
                            })
                        }
                        return newPath;
                    };
                    var result = {
                        obj: currentPolyline
                    };
                    event.trigger('draw', result);
                });
                handler.open();
            };

            /**
             * 画矩形
             */
            event.rectangle = function () {
                if (handler) handler.close();
                handler = new T.RectangleTool(map);
                handler.open();
                handler.on('draw', function (e) {
                    var currentRectangle = e.currentRectangle;
                    currentRectangle.CLASS_NAME = 'Overlay.Rectangle';
                    var bounds = e.currentBounds;
                    currentRectangle.getPath = function () {
                        var rightTop = bounds.Lq;
                        var leftBottom = bounds.kq;
                        var arr = [
                            {lng: leftBottom.lng, lat: rightTop.lat}, rightTop,
                            {lng: rightTop.lng, lat: leftBottom.lat}, leftBottom
                        ];
                        var newPath = [];
                        for (var i = 0; i < arr.length; i++) {
                            var newPoint = _this.tianToGaoLnglat(arr[i].lng, arr[i].lat);
                            newPath.push({
                                lng: newPoint[0],
                                lat: newPoint[1]
                            })
                        }
                        return newPath;
                    };
                    var curBounds = currentRectangle.getBounds();
                    curBounds.oldcontains = curBounds.contains;
                    curBounds.contains = function (arr) {
                        return curBounds.oldcontains(new T.LngLat(arr[0], arr[1]));
                    };
                    currentRectangle.getBounds = function () {
                        return curBounds;
                    };
                    var result = {
                        obj: currentRectangle
                    };
                    event.trigger('draw', result);
                    // if (event.onDraw) {// 修改矩形围栏
                    //     event.onDraw(result);
                    // }
                });
            };

            /**
             * 画圆
             */
            event.circle = function () {
                if (handler) handler.close();
                handler = new T.CircleTool(map, {
                    color: "blue",
                    weight: 3,
                    opacity: 0.5,
                    fillColor: "#FFFFFF",
                    fillOpacity: 0.5
                });
                handler.open();
                handler.on('drawend', function (e) {
                    var currentCircle = e.currentCircle;
                    currentCircle.CLASS_NAME = 'Overlay.Circle';
                    var center = currentCircle.getCenter();
                    var newPoint = _this.tianToGaoLnglat(center.lng, center.lat);
                    currentCircle.getCenter = function () {
                        return newPoint;
                    };
                    var result = {
                        obj: currentCircle
                    };
                    event.trigger('draw', result);
                    // if (event.onDraw) {// 修改矩形围栏
                    //     event.onDraw(result);
                    // }
                });
            };

            /**
             * 关闭画圆工具
             * status false不清空所画覆盖物，true清空覆盖物
             */
            event.close = function (status) {
                if (!handler) return;
                handler.close();
                if (status && handler.clear) {
                    handler.clear();
                }
            };

            /**
             * 测算距离工具
             */
            event.rule = function (option, callback) {
                if (handler) handler.close();
                handler = new T.PolylineTool(map, Object.assign(option, {showLabel: true}));
                handler.on('draw', function () {
                    if (typeof  callback === 'function') {
                        callback();
                    }
                });
                handler.open();
            };

            /**
             * 拉框放大
             */
            event.rectZoomIn = function () {

            };

            /**
             * 拉框缩小
             */
            event.rectZoomOut = function () {

            };

            return event;
        };

        /**
         * 设置3D楼块图层
         * @param option
         */
        this.mapObj.buildings = function (option) {
            var event = new Object();
            event.setMap = function (opt) {

            };
            return event;
        };

        /**
         * 添加缩放工具
         * @param option
         */
        this.mapObj.toolBar = function () {
            return new T.Control.Zoom();
        };

        /**
         * 添加比例尺工具
         * @returns {T.Control.Scale}
         */
        this.mapObj.scale = function () {
            return new T.Control.Scale();
        };

        /**
         * 三维球面地图
         * */
        this.mapObj.dimensionalMapInit = function () {
            var map = this;
            $('#dimensionalMapContainer').css('height', $('#MapContainer').height() + 'px');
            var token = mapKeys.tian;
            // 服务域名
            var tdtUrl = 'https://t{s}.tianditu.gov.cn/';
            // 服务负载子域
            var subdomains = ['0', '1', '2', '3', '4', '5', '6', '7'];

            // cesium 初始化
            var viewer = new Cesium.Map('dimensionalMapContainer', {
                shouldAnimate: true,
                selectionIndicator: true,
                infoBox: false
            });

            // 叠加影像服务
            var imgMap = new Cesium.UrlTemplateImageryProvider({
                url: tdtUrl + 'DataServer?T=img_w&x={x}&y={y}&l={z}&tk=' + token,
                subdomains: subdomains,
                tilingScheme: new Cesium.WebMercatorTilingScheme(),
                maximumLevel: 18
            });
            viewer.imageryLayers.addImageryProvider(imgMap);

            // 叠加国界服务
            var iboMap = new Cesium.UrlTemplateImageryProvider({
                url: tdtUrl + 'DataServer?T=ibo_w&x={x}&y={y}&l={z}&tk=' + token,
                subdomains: subdomains,
                tilingScheme: new Cesium.WebMercatorTilingScheme(),
                maximumLevel: 10
            });
            viewer.imageryLayers.addImageryProvider(iboMap);

            // 叠加地形服务
            var terrainUrls = new Array();

            for (var i = 0; i < subdomains.length; i++) {
                var url = tdtUrl.replace('{s}', subdomains[i]) + 'DataServer?T=elv_c&tk=' + token;
                terrainUrls.push(url);
            }

            var provider = new Cesium.GeoTerrainProvider({
                urls: terrainUrls
            });

            viewer.terrainProvider = provider;

            // 将三维球定位到中国
            map.dimensionalGoChina(viewer);

            // 叠加三维地名服务
            var wtfs = new Cesium.GeoWTFS({
                viewer,
                subdomains: subdomains,
                metadata: {
                    boundBox: {
                        minX: -180,
                        minY: -90,
                        maxX: 180,
                        maxY: 90
                    },
                    minLevel: 1,
                    maxLevel: 20
                },
                aotuCollide: true, //是否开启避让
                collisionPadding: [5, 10, 8, 5], //开启避让时，标注碰撞增加内边距，上、右、下、左
                serverFirstStyle: true, //服务端样式优先
                labelGraphics: {
                    font: "28px sans-serif",
                    fontSize: 28,
                    fillColor: Cesium.Color.WHITE,
                    scale: 0.5,
                    outlineColor: Cesium.Color.BLACK,
                    outlineWidth: 5,
                    style: Cesium.LabelStyle.FILL_AND_OUTLINE,
                    showBackground: false,
                    backgroundColor: Cesium.Color.RED,
                    backgroundPadding: new Cesium.Cartesian2(10, 10),
                    horizontalOrigin: Cesium.HorizontalOrigin.MIDDLE,
                    verticalOrigin: Cesium.VerticalOrigin.TOP,
                    eyeOffset: Cesium.Cartesian3.ZERO,
                    pixelOffset: new Cesium.Cartesian2(0, 8)
                },
                billboardGraphics: {
                    horizontalOrigin: Cesium.HorizontalOrigin.CENTER,
                    verticalOrigin: Cesium.VerticalOrigin.CENTER,
                    eyeOffset: Cesium.Cartesian3.ZERO,
                    pixelOffset: Cesium.Cartesian2.ZERO,
                    alignedAxis: Cesium.Cartesian3.ZERO,
                    color: Cesium.Color.WHITE,
                    rotation: 0,
                    scale: 1,
                    width: 18,
                    height: 18
                }
            });

            //三维地名服务，使用wtfs服务
            wtfs.getTileUrl = function () {
                return tdtUrl + 'mapservice/GetTiles?lxys={z},{x},{y}&tk=' + token;
            };

            wtfs.getIcoUrl = function () {
                return tdtUrl + 'mapservice/GetIcon?id={id}&tk=' + token;
            };

            wtfs.initTDT([{
                "x": 6,
                "y": 1,
                "level": 2,
                "boundBox": {"minX": 90, "minY": 0, "maxX": 135, "maxY": 45}
            }, {"x": 7, "y": 1, "level": 2, "boundBox": {"minX": 135, "minY": 0, "maxX": 180, "maxY": 45}}, {
                "x": 6,
                "y": 0,
                "level": 2,
                "boundBox": {"minX": 90, "minY": 45, "maxX": 135, "maxY": 90}
            }, {"x": 7, "y": 0, "level": 2, "boundBox": {"minX": 135, "minY": 45, "maxX": 180, "maxY": 90}}, {
                "x": 5,
                "y": 1,
                "level": 2,
                "boundBox": {"minX": 45, "minY": 0, "maxX": 90, "maxY": 45}
            }, {"x": 4, "y": 1, "level": 2, "boundBox": {"minX": 0, "minY": 0, "maxX": 45, "maxY": 45}}, {
                "x": 5,
                "y": 0,
                "level": 2,
                "boundBox": {"minX": 45, "minY": 45, "maxX": 90, "maxY": 90}
            }, {"x": 4, "y": 0, "level": 2, "boundBox": {"minX": 0, "minY": 45, "maxX": 45, "maxY": 90}}, {
                "x": 6,
                "y": 2,
                "level": 2,
                "boundBox": {"minX": 90, "minY": -45, "maxX": 135, "maxY": 0}
            }, {"x": 6, "y": 3, "level": 2, "boundBox": {"minX": 90, "minY": -90, "maxX": 135, "maxY": -45}}, {
                "x": 7,
                "y": 2,
                "level": 2,
                "boundBox": {"minX": 135, "minY": -45, "maxX": 180, "maxY": 0}
            }, {"x": 5, "y": 2, "level": 2, "boundBox": {"minX": 45, "minY": -45, "maxX": 90, "maxY": 0}}, {
                "x": 4,
                "y": 2,
                "level": 2,
                "boundBox": {"minX": 0, "minY": -45, "maxX": 45, "maxY": 0}
            }, {"x": 3, "y": 1, "level": 2, "boundBox": {"minX": -45, "minY": 0, "maxX": 0, "maxY": 45}}, {
                "x": 3,
                "y": 0,
                "level": 2,
                "boundBox": {"minX": -45, "minY": 45, "maxX": 0, "maxY": 90}
            }, {"x": 2, "y": 0, "level": 2, "boundBox": {"minX": -90, "minY": 45, "maxX": -45, "maxY": 90}}, {
                "x": 0,
                "y": 1,
                "level": 2,
                "boundBox": {"minX": -180, "minY": 0, "maxX": -135, "maxY": 45}
            }, {"x": 1, "y": 0, "level": 2, "boundBox": {"minX": -135, "minY": 45, "maxX": -90, "maxY": 90}}, {
                "x": 0,
                "y": 0,
                "level": 2,
                "boundBox": {"minX": -180, "minY": 45, "maxX": -135, "maxY": 90}
            }]);
            $('#dimensionalMapContainer').show();

            this.hasDimensionalMap = viewer;
        };
        /**
         * 三维地球定位至中国
         * */
        this.mapObj.dimensionalGoChina = function (viewer) {
            viewer.camera.flyTo({
                destination: Cesium.Cartesian3.fromDegrees(103.84, 31.15, 17850000),
                orientation: {
                    heading: Cesium.Math.toRadians(348.4202942851978),
                    pitch: Cesium.Math.toRadians(-89.74026687972041),
                    roll: Cesium.Math.toRadians(0)
                },
                complete: function callback() {
                    // 定位完成之后的回调函数
                }
            });
        };
        /**
         * 三维地球显示隐藏
         * */
        this.mapObj.dimensionalMapToggle = function () {
            var map = this;
            var obj = {
                show: function () {
                    if (map.hasDimensionalMap) {
                        // 将三维球定位到中国
                        map.dimensionalGoChina(map.hasDimensionalMap);
                        $('#dimensionalMapContainer').show();
                    } else {
                        map.dimensionalMapInit();
                    }
                },
                hide: function () {
                    $('#dimensionalMapContainer').hide();
                }
            };
            return obj;
        };

        /**
         * 地图添加卫星地图图层
         * @param option
         */
        this.mapObj.satellite = function (option) {
            var _this = this;
            var imageURL = 'http://t0.tianditu.gov.cn/img_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=img&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&tk=5efe61c573d4fd2fccef9fd3381f8986';
            //创建自定义图层对象
            var lay = new T.TileLayer(imageURL, {minZoom: 1, maxZoom: 18});
            var event = new Object();
            event.setMap = function () {

            };
            event.hide = function () {
                _this.removeLayer(lay);
            };
            event.show = function () {
                _this.addLayer(lay);
            };
            return event;
        };

        /**
         * 开启路况
         * @param option
         */
        this.mapObj.traffic = function (option) {
            var event = new Object();
            event.setMap = function () {

            };
            event.hide = function () {

            };
            event.show = function () {

            };
            return event;
        };

        /**
         * 信息弹窗
         * @param option
         * @returns {T.InfoWindow}
         */
        this.mapObj.infoWindow = function (option) {
            var infoWindow = new T.InfoWindow({
                offset: option.offset,
                closeOnClick: option.closeWhenClickMap
            });
            infoWindow.open = function (map, lnglat) {
                map.openInfoWindow(infoWindow, lnglat);
            };
            infoWindow.close = function () {
                this.closeInfoWindow();
            };
            infoWindow.setPosition = function (lnglat) {
                infoWindow.setLngLat(lnglat);
            };
            $('.tdt-infowindow-content').css({overflow: 'auto'});
            return infoWindow;
        };

        /**
         * 标注物所用图标
         * @param option
         */
        this.mapObj.icon = function (option) {
            return new T.Icon({iconSize: option.size, iconUrl: option.image});
        };

        /**
         * 以像素坐标表示的地图上的一个点。
         * @param x
         * @param y
         * @returns {Point|boolean|t|t}
         */
        this.mapObj.size = function (x, y) {
            return new T.Point({x: x, y: y});
        };

        /**
         * 鹰眼
         * @param option
         */
        this.mapObj.hawkEye = function (option) {
            var overview = new T.Control.OverviewMap({
                isOpen: false,
            });
            overview.show = function () {
                if (!this.isOpen()) {
                    this.changeView();
                }
            };
            overview.open = function () {
            };
            overview.hide = function () {
                if (this.isOpen()) {
                    this.changeView();
                }
            };
            overview.close = function () {
            };
            return overview;
        };

        /**
         * 添加自定义图层
         * @param option
         */
        this.mapObj.imageLayer = function (option) {
            var lay = new T.TileLayer(option.url, {
                bounds: option.bounds,
                minZoom: option.zooms[0],
                maxZoom: option.zooms[1],
                opacity: 0,
            });
            //将图层增加到地图上
            option.map.addLayer(lay);
            lay.show = function () {
                lay.setOpacity(1);
            };
            lay.hide = function () {
                lay.setOpacity(0);
            };
            return lay;
        };

        /**
         * 实现在地图上叠加自定义的WMS地图图块层
         * @param option
         */
        this.mapObj.wmts = function (option) {
            var lay = new Object(); // new T.TileLayer.WMS(option.url);
            lay.setMap = function (map) {
                // map.addLayer(lay);
            };
            lay.hide = function () {

            };
            lay.show = function () {

            };
            return lay;
        };

        /**
         * 圆编辑
         * @param map
         * @param option
         */
        this.mapObj.circleEditor = function (map, overView) {
            var lay = overView;

            var listenerList = {
                'move': function () {
                },
                'adjust': function () {
                },
            };
            var event = new $.fn.mapEngine.createEvent(listenerList);

            event.open = function () {
                lay.enableEdit();
            };
            event.close = function () {
                lay.disableEdit();
            };
            // 鼠标在圆释放触发此事件
            overView.on('mouseup', () => {
                event.trigger('move');
            });

            return event;
        };

        /**
         *  创建矩形
         * @param option
         */
        this.mapObj.rectangle = function (option) {
            var rect = new T.Rectangle(option.bounds, {
                fillColor: option.fillColor,
                fillOpacity: option.fillOpacity,
                lineStyle: option.strokeColor,
            });
            rect.enableEdit();
            this.addOverLay(rect);
            rect.setMap = function () {

            };
            return rect;
        };

        /**
         * 编辑矩形
         * @param map
         * @param overView
         * @returns {Object}
         */
        this.mapObj.rectangleEditor = function (map, overView) {
            var lay = overView;

            var listenerList = {
                'move': function () {
                },
                'adjust': function () {
                },
            };
            var event = new $.fn.mapEngine.createEvent(listenerList);

            event.open = function () {
                lay.enableEdit();
            };
            event.close = function () {
                lay.disableEdit();
            };
            // 鼠标在圆释放触发此事件
            overView.on('mouseup', () => {
                event.trigger('adjust');
            });

            return event;
        };

        /**
         * POI搜索
         * @param option
         */
        this.mapObj.placeSearch = function (option) {
            var listenerList = {};
            var event = new $.fn.mapEngine.createEvent(listenerList);
            event.setCity = function (adcode) {

            };
            event.search = function (name, callback) {

            };

            return event;
            /*var localSearchResult;
            var localSearch = new T.LocalSearch(this, {
                pageCapacity: 10,
                onSearchComplete: function (result) {
                    if (typeof localSearchResult === 'function') {
                        localSearchResult(result);
                    }
                }
            });

            var listenerList = {};
            var event = new $.fn.mapEngine.createEvent(listenerList);
            event.setCity = function (adcode) {
                localSearch.setSpecifyAdminCode(adcode);
            };
            event.search = function (name, callback) {
                localSearch.search(name, 7);
            }

            return event;*/
        };

        /**
         * 逆地理编码
         * @param option
         */
        this.mapObj.geocoder = function (option) {
            var geocoder = new T.Geocoder()
            var listenerList = {
                complete: function () {
                }
            };
            var event = new $.fn.mapEngine.createEvent(listenerList);

            event.getAddress = function (lnglat) {
                geocoder.getLocation(new T.LngLat(lnglat[0], lnglat[1]), function (result) {
                    event.trigger('complete', result);
                });
            };

            return event;
        };

        /**
         * 驾车-路线规划
         * @param option
         */
        this.mapObj.truckDriving = function (option) {
            var policy;
            var completeCallback;
            switch (option.policy) {
                case 8:
                    policy = 1;
                    break;
                case 1:
                    policy = 0;
                    break;
                default:
                    policy = 1;
            }
            var drivingRoute = new T.DrivingRoute(option.map, {
                policy: policy,	//驾车策略
                onSearchComplete: function (result) {
                    if (typeof completeCallback === 'function') {
                        completeCallback(result);
                    }
                }	//检索完成后的回调函数
            });
            drivingRoute.search = function (path, callback) {
                completeCallback = callback;
                drivingRoute.search(path.lnglat[0], path.lnglat[1]);
            };
            return drivingRoute;
        };

        /**
         * 地图右键菜单
         * @param option
         */
        this.mapObj.contextMenu = function (option) {
            var menu = new T.ContextMenu({
                width: 100
            });
            var listenerList = {};
            var event = new $.fn.mapEngine.createEvent(listenerList);

            event.addItem = function (item, callback) {
                var menuItem = new T.MenuItem(item, callback);
                menu.addItem(menuItem);
                menu.addSeparator();
            };
            event.addContextMenu = function () {
                map.addContextMenu(menu);
            };
            event.open = function () {

            };
            event.close = function () {
                menu.hide();
            };
            return event;
        };

        /**
         * 编辑线
         * @param map
         * @param option
         * @returns {Object | Object}
         */
        this.mapObj.polylineEditor = function (map, overView) {
            var lay = overView;

            var listenerList = {};
            var event = new $.fn.mapEngine.createEvent(listenerList);

            event.open = function () {
                lay.enableEdit();
            };
            event.close = function () {
                lay.disableEdit();
            };
            return event;
        };

        /**
         * 编辑多边形
         * @param map
         * @param option
         * @returns {Object}
         */
        this.mapObj.polygonEditor = function (map, overView) {
            var lay = overView;

            var listenerList = {};
            var event = new $.fn.mapEngine.createEvent(listenerList);

            event.open = function () {
                lay.enableEdit();
            };
            event.close = function () {
                lay.disableEdit();
            };
            return event;
        };

        /**
         * 获取百度地图经纬度path
         * */
        this.mapObj.getTianPath = function (path) {
            var newPath = [];
            for (var i = 0; i < path.length; i++) {
                var newPoint = this.gaoToTianLnglat(path[i][0] || path[i].lng, path[i][1] || path[i].lat);
                newPath.push(new T.LngLat(newPoint[0], newPoint[1]));
            }
            return newPath;
        };
        /**
         * 画线
         * @param option
         */
        this.mapObj.polyline = function (option) {
            var newPath = this.getTianPath(option.path);
            var line = new T.Polyline(newPath, {
                color: option.strokeColor,
                weight: option.strokeWeight,
                opacity: option.strokeOpacity,
                lineStyle: option.strokeStyle,
            });
            line.getPath = line.getLngLats;
            this.addOverLay(line);
            line.setMap = function () {

            };
            line.setPath = function (path) {
                this.setLngLats(path);
            };
            return line;
        };

        /**
         * 判断点是否在线上
         * @param clickLngLat
         * @param points1
         * @param points2
         */
        this.mapObj.isPointOnSegment = function (clickLngLat, points1, points2) {
            return true;
        };

        /**
         * 画多边形
         * @param option
         */
        this.mapObj.polygon = function (option) {
            var newPath = this.getTianPath(option.path[0][0][0] instanceof Array ? option.path[0] : option.path);
            var polygon = new T.Polygon(newPath, {
                color: option.strokeColor,
                weight: option.strokeWeight,
                opacity: option.strokeOpacity,
                fillColor: option.fillColor,
                fillOpacity: option.fillOpacity,
            });
            polygon.getPath = function () {
                return this.getLngLats()[0];
            };
            polygon.setMap = function () {

            };
            this.addOverLay(polygon);
            return polygon;
        };

        /**
         * 画圆
         * @param option
         */
        this.mapObj.circle = function (option) {
            var newPoint = this.gaoToTianLnglat(option.center.lng, option.center.lat);
            var circle = new T.Circle(new T.LngLat(newPoint[0], newPoint[1]), option.radius, {
                color: option.strokeColor,
                weight: option.strokeWeight,
                opacity: option.strokeOpacity,
                fillColor: option.fillColor,
                fillOpacity: option.fillOpacity
            });
            //向地图上添加圆
            this.addOverLay(circle);
            circle.setMap = function () {

            };
            return circle;
        };

        /**
         * 可拖拽驾车路线
         * @param map
         * @param array
         */
        this.mapObj.dragRoute = function (map, array) {
            if (AMap) {
                return new AMap.DragRoute(map, array, AMap.DrivingPolicy.REAL_TRAFFIC);
            }
            var listenerList = {
                complete: function () {
                }
            };
            var event = new $.fn.mapEngine.createEvent(listenerList);

            event.search = function () {

            };

            event.destroy = function () {

            };

            event.getRoute = function () {

            };

            return event;
        };

        /**
         * 经纬度转像素坐标
         * @param option
         * @returns {{getX: (function(): number), getY: (function(): number), x: number, y: number}}
         */
        this.mapObj.lngLatToContainer = function (lngLat) {
            if (lngLat === 0 || (!lngLat[0] && !lngLat.lng)) return {
                x: 0,
                y: 0,
                getX: function () {
                    return 0;
                },
                getY: function () {
                    return 0;
                },
            };
            var point = lngLat;
            if (lngLat.wgslon) {
                point = {lng: lngLat.wgslon, lat: lngLat.wgslat};
            }
            if (lngLat[0]) {
                var newPoint = this.gaoToTianLnglat(lngLat[0], lngLat[1]);
                point = {lng: newPoint[0], lat: newPoint[1]};
            }
            var obj = this.lngLatToContainerPoint(point);
            return {
                x: obj.x,
                y: obj.y,
                getX: function () {
                    return obj.x;
                },
                getY: function () {
                    return obj.y;
                },
            }
        };

        /**
         * 地图功能-添加覆盖物图层
         */
        this.mapObj.add = function (layer) {

        };

        /**
         * 经纬度转像素
         * @param option
         * @returns {number}
         */
        this.mapObj.containerToLngLat = this.mapObj.containerPointToLngLat;

        /**
         * 删除地图覆盖物
         * @param layer
         */
        this.mapObj.remove = function (layer) {
            var map = this;
            if (layer && Array.isArray(layer)) {
                for (var i = 0; i < layer.length; i += 1) {
                    this.removeOverLay(layer[i]);
                    if (map.hasDimensionalMap && layer[i].extData) {
                        map.hasDimensionalMap.entities.removeById(layer[i].extData);
                    }
                }
            } else {
                this.removeOverLay(layer);
                if (map.hasDimensionalMap && layer.extData) {
                    map.hasDimensionalMap.entities.removeById(layer[i].extData);
                }
            }
        };
        this.mapObj.setZoomAndCenter = function (zoom, point) {
            if (point[0]) {
                var newPoint = this.gaoToTianLnglat(point[0], point[1]);
                point = new T.LngLat(newPoint[0], newPoint[1]);
            } else if (point.lng) {
                point = new T.LngLat(point.lng, point.lat);
            }
            this.goDimensionalPosition(point);
            this.centerAndZoom(point, zoom);
        };

        this.mapObj.destroy = () => {
            // this.mapObj.remove();
            this.mapObj = null;
        }

        return this.mapObj;
    };

    $.extend(true, $.fn.mapEngine.engine, {tmap: _tmap});
}(jQuery));