;(function ($) {
    // 谷歌地图
    var _gmap = function (containerId) {
        this.mapObj = new google.maps.Map(document.getElementById(containerId), {
            center: {lat: 39.915, lng: 116.404},
            zoom: 18,
            gestureHandling: 'greedy'
        });
        this.mapObj.currentMap = 'google';

        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition((position) => {
                var lat = position.coords.latitude;
                var lng = position.coords.longitude;
                this.mapObj.setCenter(new google.maps.LatLng(lat, lng));
            });
        }

        /**
         * 标准地图
         */
        this.mapObj.standardMap = function () {
            this.setMapTypeId(google.maps.MapTypeId.ROADMAP);
        };

        /**
         * 3D地图
         */
        this.mapObj.threeDimensionalMap = function () {
            this.setMapTypeId(google.maps.MapTypeId.HYBRID);
            this.setZoom(18);
            this.setHeading(90);
            this.setTilt(45);
        };

        /**
         * 卫星地图
         */
        this.mapObj.satelliteMap = function () {
            this.setTilt(0);
            this.setMapTypeId(google.maps.MapTypeId.HYBRID);
        };

        /**
         * 显示覆盖物至地图可视范围
         * */
        this.mapObj.setFitView = function (option) {
            if (option.getPaths) {
                if (option.path && option.path[0]) {
                    this.setCenter(option.path[0])
                } else {
                    this.setCenter(option.getPaths().getArray()[0].je[0]);
                }
            } else if (option.getPath) {
                this.setCenter(option.getPath().je[0]);
            } else if (option.getCenter) {
                this.setCenter(option.getCenter());
            } else if (option[0]) {
                this.setCenter(option[0].getPaths());
            } else {
                this.setCenter(option.getPosition());
            }
            this.setZoom(18);
        };

        /**
         * 清除信息弹窗
         * */
        this.mapObj.clearInfoWindow = function () {
            if (infoWindow) {
                infoWindow.close();
            }
        };

        this.mapObj.on = function (event, callback) {
            if (event === 'zoomend') event = 'zoom_changed';
            this.addListener(event, callback);
        };
        this.mapObj.off = function (event) {
            this.unbind(event);
        };

        this.mapObj.add = function (event, callback) {
        };

        /**
         * 移除地图上的覆盖物
         * */
        this.mapObj.remove = function (opt) {
            if (opt instanceof Array) {
                for (var i = 0; i < opt.length; i++) {
                    if (opt[i].setMap) {
                        opt[i].setMap(null);
                    }
                }
            } else {
                opt.setMap(null);
            }
        };

        this.mapObj.oldSetCenter = this.mapObj.setCenter;
        this.mapObj.setCenter = function (point) {
            if (!point) return;
            if (point.lng) {
                this.oldSetCenter(point);
            } else {
                this.oldSetCenter({lng: point[0], lat: point[1]});
            }
        };


        /**
         * 输入提示
         * @param config
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
         * 生成标准经纬度
         * @param lng
         * @param lat
         * @returns {LngLat}
         */
        this.mapObj.lngLat = function (lng, lat) {
            var obj = new google.maps.LatLng({lng: lng, lat: lat});
            obj.distance = function (end) {
                var start = {
                    lng: this.lng(),
                    lat: this.lat(),
                };
                var lat1 = (Math.PI / 180) * start.lat;
                var lat2 = (Math.PI / 180) * end.lat();
                var lon1 = (Math.PI / 180) * start.lng;
                var lon2 = (Math.PI / 180) * end.lng();

                //地球半径
                var R = 6371;

                //两点间距离 km，如果想要米的话，结果*1000就可以了
                var d = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)) * R;

                return d * 1000;
            };
            return obj;
        };

        this.mapObj.text = function (param) {
            return null;
        };

        /**
         * 点聚合功能
         * */
        this.mapObj.markerCluster = function (map, points, option) {
            var arrayObj = [];
            for (var i = 0; i < points.length; i++) {
                var marker = new google.maps.Marker({
                    position: {lng: points[i].lnglat[0], lat: points[i].lnglat[1]}
                });
                arrayObj.push(marker);
            }
            var markers = new MarkerClusterer(map, arrayObj, {
                imagePath: "https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/m",
            });

            markers.setMap = function (status) {
                if (!status) {
                    markers.clearMarkers();
                }
            };
            markers.on = function (event, callback) {

            };

            return markers;
        };

        /**
         * 创建自定义图层
         * */
        this.mapObj.createDefaultLayer = function (option) {
            var lay = {};
            lay.setMap = function (opt) {

            };
            lay.hide = function () {

            };
            return lay;
        };

        /**
         * 点标记绘制
         * */
        this.mapObj.marker = function (option) {
            var logoShow = $('#logoDisplay').is(':checked');
            var point = option.content ? option.monitorPos : option.position;
            if (point[0]) {
                point = {
                    lng: point[0],
                    lat: point[1]
                }
            }
            var _this = this;
            var marker = new google.maps.Marker({
                position: point,
                icon: option.icon || '/no.img',
                map: _this,
                label: logoShow && option.name ? {
                    text: option.name,
                    className: 'googleMonitorName',
                } : "",
            });
            marker.name = option.name;
            marker.extData = option.id;
            marker.hide = function () {
                this.setMap(null);
            };
            if (option.content) {
                if (logoShow) {
                    var id = marker.extData;
                    var curMarker = carNameMarkerMap.get(id);
                    if (curMarker) {
                        curMarker.setLabel({
                            text: curMarker.name,
                            className: 'googleMonitorName',
                        });
                    }
                }
                marker.hide = function (status) {// 隐藏车牌号
                    if (!status) {
                        var id = marker.extData;
                        var curMarker = carNameMarkerMap.get(id);
                        if (curMarker) curMarker.setLabel('');
                    }
                }
            } else if (!option.icon) {
                marker = new google.maps.Marker({
                    position: point,
                    map: _this,
                    label: "",
                });
                marker.hide = function () {
                    this.setMap(null);
                };
            }
            marker.setAngle = function (angle) {
                var oldIcon = marker.getIcon();
                /*var busSvg = {
                    // Bus body
                    // 282 wide... 322 wide with the sides added
                    top : 'c-4,-20,-18,-30,-38,-38 c-20,-8,-68,-18,-113,-19 c-35,1,-83,11,-113,19 c-20,8,-34,10,-38,38',
                    left : ' l-20,150 v170',
                    bottom : ' h26 v25 c0,30,45,30,45,0 v-25 h200 v25 c0,30,45,30,45,0 v-25 h26',
                    right : ' v-170 l-20,-150z',
                    // Marquee
                    marquee : 'm-60,10 h-182 c-20,0,-20,-25,0,-25 h182 c20,0,20,25,0,25z',
                    // Windshield
                    windshield : 'm-220,150 c-11,0,-14,-8,-12,-16 l12,-85 c2,-10,5,-17,18,-17 h220 c13,0,17,7,18,17 l12,85, c1,8,-1,16,-12,16 h-235z',
                    // Tires
                    tire_left : 'm15,100 c0,30,45,30,45,0 c0,-30,-45,-30,-45,0',
                    tire_right : 'm180,0 c0,30,45,30,45,0 c0,-30,-45,-30,-45,0',
                };

                var busIcon = {
                    path: 'M0,-100 '+busSvg['top']+busSvg['left']+busSvg['bottom']+busSvg['right']+busSvg['marquee']+busSvg['windshield']+busSvg['tire_left']+busSvg['tire_right'],
                    fillColor: "red",
                    fillOpacity: 1,
                    scale: .3, //.05,
                    strokeColor: "black",
                    strokeWeight: .5,
                    rotation: angle
                };
                this.setIcon(busIcon)*/
            };
            marker.on = function (event, callback) {
                marker.addListener(event, (e) => {
                    var obj = {
                        target: {
                            getPosition: function () {
                                return e.latLng
                            },
                            extData: marker.extData,
                            content: marker.content,
                            curMarker: marker
                        }
                    };
                    callback(obj);
                });
            };
            marker.stopMove = function () {
            };
            marker.getAngle = function () {
                return 0;
            };
            marker.moveTo = function (lnglat) {
                marker.setPosition({lng: lnglat[0], lat: lnglat[1]});
            };
            marker.oldsetPosition = marker.setPosition;
            marker.setPosition = function (lnglat) {
                if (lnglat[0]) {
                    this.oldsetPosition({lng: lnglat[0], lat: lnglat[1]});
                } else {
                    this.oldsetPosition(lnglat);
                }
            };
            marker.setContent = function () {

            };
            marker.setOffset = function () {

            };
            marker.show = function () {
                this.setMap(_this);
            };
            return marker;
        };

        /**
         * 行政区域查询
         * @param option
         * @returns {{search: search}}
         */
        this.mapObj.districtSearch = function (option) {
            var obj = {
                search: function (type, callback) {

                },
                setLevel: function (type, callback) {

                },
                setExtensions: function (type, callback) {

                }
            };
            return obj;
        };

        /**
         * 鼠标工具
         * @param option
         * @returns {MouseTool}
         */
        this.mapObj.mouseTool = function (option) {
            var map = this;
            var styleOptions = {
                strokeColor: '#5E87DB',   // 边线颜色
                fillColor: '#5E87DB',     // 填充颜色。当参数为空时，圆形没有填充颜色
                strokeWeight: 2,          // 边线宽度，以像素为单位
                strokeOpacity: 1,         // 边线透明度，取值范围0-1
                fillOpacity: 0.2          // 填充透明度，取值范围0-1
            };
            var labelOptions = {
                borderRadius: '2px',
                background: '#FFFBCC',
                border: '1px solid #E1E1E1',
                color: '#703A04',
                fontSize: '12px',
                letterSpacing: '0',
                padding: '5px'
            };

            // 实例化鼠标绘制工具
            var drawingManager = new google.maps.drawing.DrawingManager({
                enableCalculate: false, // 绘制是否进行测距测面
                enableSorption: true,   // 是否开启边界吸附功能
                sorptiondistance: 20,   // 边界吸附距离
                circleOptions: styleOptions,     // 圆的样式
                polylineOptions: styleOptions,   // 线的样式
                polygonOptions: styleOptions,    // 多边形的样式
                rectangleOptions: styleOptions,  // 矩形的样式
                labelOptions: labelOptions,      // label样式
            });


            var handler;
            var listenerList = {
                'draw': function () {
                    console.log('draw');
                },
            };
            var event = new $.fn.mapEngine.createEvent(listenerList);
            event.on = function (event, callback) {
                drawingManager.onDraw = callback;
            };

            /**
             *  画标注
             */
            event.marker = function () {
                drawingManager.setDrawingMode('marker');
                drawingManager.setMap(map);
                google.maps.event.addListener(drawingManager, "markercomplete", function (overlay) {
                    overlay.CLASS_NAME = 'AMap.Marker';
                    var result = {
                        obj: overlay
                    };
                    window.prevFence = overlay;
                    fenceOperation.createSuccess(result);
                });
            };

            /**
             * 画多边形
             */
            event.polygon = function (opt) {
                drawingManager.setDrawingMode('polygon');
                drawingManager.setMap(map);
                if (!opt) {
                    google.maps.event.addListener(drawingManager, "polygoncomplete", function (overlay) {
                        overlay.CLASS_NAME = 'Overlay.Polygon';
                        var result = {
                            obj: overlay
                        };
                        var path = overlay.getPath().je;
                        overlay.getPath = function () {
                            var arr = [];
                            for (var i = 0; i < path.length; i++) {
                                arr.push({
                                    lng: path[i].lng(),
                                    lat: path[i].lat()
                                })
                            }
                            return arr;
                        };
                        window.prevFence = overlay;
                        fenceOperation.createSuccess(result);
                    });
                }
            };

            /**
             * 画线
             */
            event.polyline = function (opt) {
                drawingManager.setDrawingMode('polyline');
                drawingManager.setMap(map);
                if (!opt) {
                    google.maps.event.addListener(drawingManager, "polylinecomplete", function (overlay) {
                        overlay.CLASS_NAME = 'Overlay.Polyline';
                        var result = {
                            obj: overlay
                        };
                        var path = overlay.getPath().je;
                        overlay.getPath = function () {
                            var arr = [];
                            for (var i = 0; i < path.length; i++) {
                                arr.push({
                                    lng: path[i].lng(),
                                    lat: path[i].lat()
                                })
                            }
                            return arr;
                        };
                        window.prevFence = overlay;
                        fenceOperation.createSuccess(result);
                    });
                }
            };
            /**
             * 画矩形
             */
            event.rectangle = function (opt) {
                drawingManager.setDrawingMode('rectangle');
                drawingManager.setMap(map);
                if (!opt) {
                    google.maps.event.addListener(drawingManager, "rectanglecomplete", function (overlay) {
                        overlay.CLASS_NAME = 'Overlay.Polygon';
                        var bounds = overlay.getBounds();
                        overlay.getPath = function () {
                            var eb = bounds.Eb;
                            var mc = bounds.mc;
                            var point1 = {lng: eb.g, lat: mc.i};
                            var point2 = {lng: eb.i, lat: mc.i};
                            var point3 = {lng: eb.i, lat: mc.g};
                            var point4 = {lng: eb.g, lat: mc.g};
                            var arr = [point1, point2, point3, point4];

                            return arr;
                        };
                        if (!bounds.oldcontains) {
                            bounds.oldcontains = bounds.contains;
                            bounds.contains = function (arr) {
                                var newPoint = {lng: Number(arr[0]), lat: Number(arr[1])};
                                return bounds.oldcontains(newPoint);
                            };
                        }
                        overlay.getBounds = function () {
                            return bounds;
                        };

                        var result = {
                            obj: overlay
                        };
                        window.prevFence = overlay;
                        fenceOperation.createSuccess(result);
                        if (drawingManager.onDraw) {// 修改矩形围栏
                            drawingManager.onDraw(result);
                        }
                    });
                }
            };

            /**
             * 画圆
             */
            event.circle = function (opt) {
                drawingManager.setDrawingMode('circle');
                drawingManager.setMap(map);
                if (!opt) {
                    google.maps.event.addListener(drawingManager, "circlecomplete", function (overlay) {
                        overlay.CLASS_NAME = 'Overlay.Circle';
                        var result = {
                            obj: overlay
                        };
                        window.prevFence = overlay;
                        fenceOperation.createSuccess(result);
                    });
                }
            };

            /**
             * 关闭画图工具
             * status false不清空所画覆盖物，true清空覆盖物
             */
            event.close = function (status) {
                drawingManager.setMap(null);
                if (status) {
                    if (handler) handler.close();
                    if (window.prevFence && window.prevFence.setMap) {
                        window.prevFence.setMap(null);
                        window.prevFence = null;
                    }
                }
            };

            /**
             * 测算距离工具
             */
            event.rule = function (option) {

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

        this.mapObj.plugin = function (plugnArr, callback) {
            return {};
        };
        this.mapObj.addControl = function (option) {

        };
        this.mapObj.toolBar = function (option) {
            return {};
        };

        this.mapObj.scale = function (option) {
            return {};
        };

        this.mapObj.setZoomAndCenter = function (zoom, point) {
            if (point[0]) {
                this.setCenter({lng: point[0], lat: point[1]});
            } else {
                this.setCenter(point);
            }
            this.setZoom(zoom);
        };

        /**
         * 经纬度转像素坐标
         * @param option
         * @returns {{getX: (function(): number), getY: (function(): number), x: number, y: number}}
         */
        this.mapObj.lngLatToContainer = function (option) {
            return {
                x: 0,
                y: 0,
                getX: function () {
                    return 0;
                },
                getY: function () {
                    return 0;
                },
            }
        };

        /**
         * 地图可视区域范围
         * */
        this.mapObj.bounds = function (arr1, arr2) {
            // var bounds = new google.maps.LatLngBounds(arr1, arr2);
            var obj = this.getBounds();
            if (!obj) return {
                contains: function () {

                }
            };
            obj.oldcontains = obj.contains;
            obj.contains = function (point) {
                if (point[0]) {
                    return this.oldcontains({lng: point[0], lat: point[1]})
                } else {
                    return this.oldcontains(point);
                }
            };
            return obj;
        };

        /**
         * 经纬度转换
         * */
        this.mapObj.containerToLngLat = function (option) {
            return 0;
        };

        this.mapObj.pixel = function (pix1, pix2) {
            return {
                x: 0,
                y: 0,
                getX: function () {
                    return 0;
                },
                getY: function () {
                    return 0;
                },
            }
        };

        this.mapObj.tileLayer = function (option) {
            return null;
        };

        /**
         * 卫星地图
         * */
        this.mapObj.satellite = function (option) {
            var map = this;
            var event = new Object();
            event.setMap = function () {

            };
            event.hide = function () {
                map.setMapTypeId(google.maps.MapTypeId.ROADMAP);
            };
            event.show = function () {
                map.setMapTypeId(google.maps.MapTypeId.HYBRID);
            };
            return event;
        };

        /**
         * 路况
         * */
        this.mapObj.traffic = function (option) {
            var map = this;
            var trafficLayer = new google.maps.TrafficLayer();
            trafficLayer.hide = function () {
                this.setMap(null);
            };
            trafficLayer.show = function () {
                this.setMap(map);
            };
            return trafficLayer;
        };

        /**
         * 信息弹窗
         * */
        this.mapObj.infoWindow = function (option) {
            var obj = new google.maps.InfoWindow(option);
            obj.oldOpen = obj.open;
            obj.open = function (map, point, content, marker) {
                obj.oldOpen({
                    anchor: marker,
                    map,
                    content: content
                });
            };
            return obj;
        };

        /*this.mapObj.icon = function (option) {
            return new AMap.Icon(option);
        };

        this.mapObj.size = function (option) {
            return new AMap.Size(option);
        };*/

        /**
         * 鹰眼功能
         * */
        this.mapObj.hawkEye = function (option) {
            var overview = {};
            overview.show = function () {

            };
            overview.open = function () {

            };
            overview.hide = function () {

            };
            overview.close = function () {
            };
            return overview;
        };

        this.mapObj.imageLayer = function (option) {
            var lay = {
                show: function () {

                },
                hide: function () {

                },
            };
            return lay;
        };

        this.mapObj.wmts = function (option) {
            var lay = new Object(); // new BMapGL.TileLayer.WMS(option.url);
            lay.setMap = function (map) {
            };
            lay.hide = function () {

            };
            lay.show = function () {

            };
            return lay;
        };

        this.mapObj.circleEditor = function (map, option) {
            var event = {};
            event.open = function () {
            };
            event.close = function () {
            };
            return event;
        };

        this.mapObj.rectangle = function (option) {
            var map = this;
            var rect = new google.maps.Rectangle({
                bounds: option.bounds,
                map: map,
                fillColor: option.fillColor,
                fillOpacity: option.fillOpacity,
                lineStyle: option.strokeColor,
            });
            return rect;
        };

        this.mapObj.rectangleEditor = function (map, option) {
            var event = {};
            event.open = function () {
            };
            event.close = function () {
            };
            return event;
        };

        this.mapObj.placeSearch = function (option) {
            var listenerList = {};
            var event = new $.fn.mapEngine.createEvent(listenerList);
            event.setCity = function (adcode) {

            };
            event.search = function (name, callback) {

            };

            return event;
        };

        this.mapObj.geocoder = function (option) {
            var listenerList = {
                complete: function () {}
            };
            var event = new $.fn.mapEngine.createEvent(listenerList);

            event.getAddress = function (lnglat) {
            };
            return event;
        };

        this.mapObj.truckDriving = function (option) {
            var drivingRoute = {};
            drivingRoute.search = function (path, callback) {
            };
            return drivingRoute;
        };

        this.mapObj.driving = function (option) {
            return {};
        };

        this.mapObj.contextMenu = function (option) {
            var listenerList = {};
            var event = new $.fn.mapEngine.createEvent(listenerList);
            event.addItem = function (item, callback) {
            };
            event.addContextMenu = function () {
            };
            event.open = function () {
            };
            event.close = function () {
            };
            return event;
        };

        this.mapObj.polylineEditor = function (map, option) {
            var event = {};
            event.open = function () {
            };
            event.close = function () {
            };
            return event;
        };

        this.mapObj.polygonEditor = function (map, option) {
            var event = {};
            event.open = function () {
            };
            event.close = function () {
            };
            return event;
        };

        this.mapObj.polyline = function (option) {
            var map = this;
            var path = option.path;
            var newPoint = [];
            for (var i = 0; i < path.length; i++) {
                newPoint.push({lng: path[i][0], lat: path[i][1]})
            }
            var line = new google.maps.Polyline({
                path: newPoint,
                map: map,
                strokeColor: option.strokeColor,
                strokeWeight: option.strokeWeight,
                strokeOpacity: option.strokeOpacity,
                strokeStyle: option.strokeStyle,
            });
            line.on = function (event, callback) {

            };
            line.show = function () {
                this.setMap(map);
            };
            line.hide = function () {
                this.setMap(null);
            };
            return line;
        };

        this.mapObj.isPointOnSegment = function (clickLngLat, points1, points2) {

        };

        this.mapObj.polygon = function (option) {
            var map = this;
            var path = option.path[0][0][0] instanceof Array ? option.path[0] : option.path;
            var newPoint = [];
            for (var i = 0; i < path.length; i++) {
                newPoint.push({lng: Number(path[i][0]), lat: Number(path[i][1])})
            }

            var polygon = new google.maps.Polygon({
                paths: newPoint,
                map: map,
                fillColor: option.fillColor,
                fillOpacity: option.fillOpacity,
                strokeColor: option.strokeColor,
                strokeWeight: 1,
            });
            polygon.path = newPoint;
            polygon.show = function () {
                this.setMap(map);
            };
            polygon.hide = function () {
                this.setMap(null);
            };
            return polygon;
        };

        this.mapObj.circle = function (option) {
            var map = this;
            const circle = new google.maps.Circle({
                strokeColor: option.strokeColor,
                strokeWeight: option.strokeWeight,
                strokeOpacity: option.strokeOpacity,
                fillColor: option.fillColor,
                fillOpacity: option.fillOpacity,
                map,
                center: option.center,
                radius: option.radius,
            });
            circle.show = function () {
                this.setMap(map);
            };
            circle.hide = function () {
                this.setMap(null);
            };
            return circle;
        };

        /**
         * 可拖拽路径
         * */
        this.mapObj.dragRoute = function (map, array) {
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

        return this.mapObj;
    };

    $.extend(true, $.fn.mapEngine.engine, {gmap: _gmap});
}(jQuery));