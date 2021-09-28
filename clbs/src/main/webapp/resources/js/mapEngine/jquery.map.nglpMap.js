;(function ($) {
    var _nglpMap = function (containerId) {
        this.mapObj = new EV.Map(containerId);
        this.mapObj.currentMap = 'nglpMap';
        if (window.initCenterPoint) {
            var point = new EV.LngLat(initCenterPoint.lng, initCenterPoint.lat);
            this.mapObj.setCenter(point, 18);
        } else {
            var point = new EV.LngLat(116.404, 39.915);
            this.mapObj.setCenter(point, 18);
        }
        var mapctrl = new EV.MapControl();
        this.mapObj.addControl(mapctrl);
        this.mapObj.addControl(new EV.ScaleControl());

        /**
         * 标准地图
         */
        this.mapObj.standardMap = function () {
            this.setMapType(0);
        };
        /**
         * 卫星地图
         */
        this.mapObj.satelliteMap = function () {
            this.setMapType(2);
        };

        this.mapObj.clearInfoWindow = function () {
        };
        this.mapObj.off = function (event) {
            EV.Event.clearListeners(this, event);
        };
        /**
         * 显示覆盖物至地图可视范围
         * */
        this.mapObj.setFitView = function (option) {
            if (option.getLngLats) {
                this.getBestMap(option.getLngLats());
            } else if (option.getLngLat) {
                this.getBestMap([option.getLngLat()]);
            } else if (option.getCenter) {
                this.getBestMap([option.getCenter()]);
            } else if (option[option.length - 1]) {
                this.getBestMap(option[option.length - 1].getLngLats());
            } else {
                this.getBestMap(option);
            }
        };
        this.mapObj.oldSetCenter = this.mapObj.setCenter;
        this.mapObj.setCenter = function (point, zoom) {
            if (point[0]) {
                point = new EV.LngLat(point[0], point[1]);
            } else if (point.lng) {
                point = new EV.LngLat(point.lng, point.lat);
            }
            this.oldSetCenter(point, zoom || 18);
            amapOperation.markerStateListening();
        };
        this.mapObj.on = function (event, callback) {
            this.addEventListener(event, callback);
        };

        this.mapObj.remove = function (opt) {
            if (opt && Array.isArray(opt)) {
                for (var i = 0; i < opt.length; i++) {
                    if (opt[i]) {
                        this.removeOverlay(opt[i]);
                    }
                }
            } else if (opt) {
                this.removeOverlay(opt);
            }
        };
        this.mapObj.add = function () {
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
         * @returns {EV.LngLat}
         */
        this.mapObj.lngLat = function (lng, lat) {
            var obj = new EV.LngLat(lng, lat);
            obj.distance = obj.distanceFrom;
            return obj;
        };

        this.mapObj.text = function (param) {
            return null;
        };

        this.mapObj.markerCluster = function (map, points, option) {
            /*var listenerList = {
                click: function () {
                },
            };

            var event = new $.fn.mapEngine.createEvent(listenerList);
            event.setMap = function () {

            };
            return event;*/
            //配置聚合参数
            var config = {};
            config.clusterStyle = [];
            var c_u = 'http://map.yiweihang.com/api/cluster/m';
            for (var i = 0; i < 5; i++) {
                config.clusterStyle.push({url: c_u + i + ".png", number: 100 * (i + 1)});
            }
            //构造聚合坐标点
            var _cluster_data = [];
            for (var i = 0; i < points.length; i++) {
                var lng = parseFloat(points[i].lnglat[0]);
                var lat = parseFloat(points[i].lnglat[1]);
                if (lng && lat) {
                    _cluster_data.push(new EV.LngLat(lng, lat));
                }
            }
            var markerClusterer = new EV.MarkerClusterer(map, config);
            markerClusterer.cluster(_cluster_data);
            markerClusterer.setMap = function (status) {
                if (!status) {
                    this.clearClusters();
                }
            };
            markerClusterer.on = function () {

            };
            return markerClusterer;
        };

        this.mapObj.createDefaultLayer = function (option) {
            var lay = {};
            lay.setMap = function (opt) {
            };
            lay.hide = function () {
            };
            return lay;
        };

        this.mapObj.marker = function (option) {
            var logoShow = $('#logoDisplay').is(':checked');
            var point = option.content ? option.monitorPos ? option.monitorPos : option.position : option.position;
            if (point[0]) {
                point = new EV.LngLat(point[0], point[1]);
            } else if (point.lng) {
                point = new EV.LngLat(point.lng, point.lat);
            }
            if (option.monitorPos && option.content) {
                var match = option.content.match((/'monitorNameBox'>(.*)<\/span>/));
                if (!match) return 0;
                if (!match[1]) return 0;
                option.content = match[1];
            }
            var w = 48, h = 30;
            if (option.size) {
                h = option.size.h;
                w = option.size.w;
            }
            var marker = new EV.Marker(point, {
                externalGraphic: option.icon || undefined,
                graphicHeight: h,
                graphicWidth: w,
                fontColor: "#000",
                fontSize: "12px",
                labelYOffset: -38,
                labelAlign: "l",
                labelXOffset: 18,
                label: logoShow && option.name ? option.name : '',
            });
            marker.name = option.name;
            marker.extData = option.id;
            if (option.content && option.monitorPos) {
                marker.setGraphic(false);
                if (logoShow) {
                    var id = marker.extData;
                    var curMarker = carNameMarkerMap.get(id);
                    if (curMarker) {
                        curMarker.setLabel(curMarker.name);
                    }
                }
                marker.hide = function (status) {// 隐藏车牌号
                    if (!status) {
                        var id = marker.extData;
                        var curMarker = carNameMarkerMap.get(id);
                        if (curMarker) curMarker.setLabel('');
                    }
                }
            }
            marker.setMap = function (map) {
                map.addOverlay(this);
            };
            marker.setAngle = function () {
            };
            marker.on = function (event, callback) {
                marker.addEventListener(event, (e) => {
                    callback({
                        target: e.object
                    });
                });
            };
            marker.stopMove = function () {
            };
            marker.getAngle = function () {
                return 0;
            };
            marker.getPosition = function () {
                return this.getLngLat();
            };
            var _this = this;
            marker.moveTo = function (lnglat) {
                this.setPosition(lnglat);
                if (amapOperation) {
                    amapOperation.markerMoveendFun(this, this.extData);
                    // 判断是否为聚焦跟踪监控对象
                    if (markerFocus && marker.extData && markerFocus == marker.extData) {
                        var msg = marker.getPosition();
                        if (!pathsTwo.contains(msg)) {
                            _this.setCenter(msg);
                            amapOperation.LimitedSizeTwo();
                        }
                    }
                }
            };
            marker.setPosition = function (lnglat) {
                if (lnglat.lng) {
                    marker.setLngLat(new EV.LngLat(lnglat.lng, lnglat.lat));
                } else if (lnglat[0]) {
                    marker.setLngLat(new EV.LngLat(lnglat[0], lnglat[1]));
                }
            };
            marker.setContent = function () {
            };
            marker.setOffset = function () {
            };
            // 点标记可拖拽
            marker.setDraggable = function (status) {
                marker.addEventListener('click', function(e) {
                    console.log('四维地图不支持拖拽事件', e);
                    alert('四维地图不支持拖拽事件');
                })
                if (status) { } else { }
            };
            this.addOverlay(marker);
            return marker;
        };
        /**
         * 行政区域查询
         * @param option
         * @returns {{search: search}}
         */
        this.mapObj.districtSearch = function (option) {
            var as = new EV.ServiceRAC(this.mapObj);
            var obj = {
                search: function (name, callback) {
                    as.getBounday({
                        adminname: name,
                    },function (data){
                        if(!data.error){
                            var pointArr = [];
                            var geo = data.admin.geo;
                            var regex = /\((.+?)\)/g;
                            var list = geo.match(regex)[0];
                            list = [list.substring(3, list.length -1)];

                            for(let i = 0; i < list.length; i++) {
                                var item = list[i].split(',');
                                var add = [];
                                for(let j = 0; j < item.length; j++) {
                                    var point = item[j].split(' ');
                                    add.push([point[0], point[1]])
                                }
                                pointArr.push(add)
                            }
                            callback('complete',{
                                districtList:[{
                                    boundaries: pointArr
                                }]
                            });
                        }
                    })
                },
                setLevel: function (type, callback) {},
                setExtensions: function (type, callback) {}
            };
            return obj;
        };
        /**
         * 鼠标工具
         * @param option
         */
        this.mapObj.mouseTool = function (map, measure) {
            var curMap = this;
            var toolbar = this.toolbar;
            var listenerList = {
                'draw': function () {
                    console.log('draw');
                },
            };
            var event = new $.fn.mapEngine.createEvent(listenerList);
            var option = {
                strokeColor: '#5E87DB',   // 边线颜色
                fillColor: '#5E87DB',     // 填充颜色。当参数为空时，圆形没有填充颜色
                strokeWeight: 2,          // 边线宽度，以像素为单位
                strokeOpacity: 1,         // 边线透明度，取值范围0-1
            };
            /**
             *  画标注
             */
            event.marker = function (opt) {
                var marker = toolbar.addTool("addpoint", {
                    text: "",  //显示的文字
                    name: "iaddpoint",
                    map: curMap
                }, opt);
                marker.addEventListener("done", function (e) {
                    var overlay = e.overlay;
                    overlay.CLASS_NAME = 'AMap.Marker';
                    overlay.getPosition = function () {
                        var arr = this.getLngLat();
                        arr.lng = arr.wgslon;
                        arr.lat = arr.wgslat;
                        return arr;
                    };
                    var result = {
                        obj: overlay
                    };
                    window.prevFence = overlay;
                    fenceOperation.createSuccess(result);
                    this.deactivate();
                    return true;
                });
                marker.activate();
            };
            /**
             * 画多边形
             */
            event.polygon = function (opt) {
                var addpoly = toolbar.addTool("addpoly", {
                    text: "",  //显示的文字
                    name: "iaddpoly",
                    map: curMap
                }, option);
                addpoly.addEventListener("done", function (e) {
                    var overlay = e.overlay;
                    overlay.CLASS_NAME = 'Overlay.Polygon';
                    overlay.getPath = function () {
                        var arr = this.getLngLats();
                        for (var i = 0; i < arr.length; i++) {
                            arr[i].lng = arr[i].wgslon;
                            arr[i].lat = arr[i].wgslat;
                        }
                        return arr;
                    };
                    var result = {
                        obj: overlay
                    };
                    window.prevFence = overlay;
                    fenceOperation.createSuccess(result);
                    this.deactivate();
                    return true;
                });
                addpoly.activate();
            };
            /**
             * 画线
             */
            event.polyline = function (opt) {
                var tline = toolbar.addTool("addline", {
                    text: "",  //显示的文字
                    name: "iaddline",
                    map: curMap
                }, option);
                tline.addEventListener("done", function (e) {
                    var overlay = e.overlay;
                    overlay.CLASS_NAME = 'Overlay.Polyline';
                    overlay.getPath = function () {
                        var arr = this.getLngLats();
                        for (var i = 0; i < arr.length; i++) {
                            arr[i].lng = arr[i].wgslon;
                            arr[i].lat = arr[i].wgslat;
                        }
                        return arr;
                    };
                    var result = {
                        obj: overlay
                    };
                    window.prevFence = overlay;
                    fenceOperation.createSuccess(result);
                    this.deactivate();
                    return true;
                });
                tline.activate();
            };
            if (measure) {// 用于测量面积
                event.rectangle = function () {
                    map.t_marea();
                };
                event.circle = function () {
                    map.t_marea();
                }
            } else {
                /**
                 * 画矩形
                 */
                event.rectangle = function (opt) {
                    var addrect = toolbar.addTool("addrect", {
                        text: "",  //显示的文字
                        name: "iaddrect",
                        map: curMap
                    }, option);
                    addrect.addEventListener("done", function (e) {
                        var overlay = e.overlay;
                        overlay.CLASS_NAME = 'Overlay.Rectangle';
                        var boundsObj = overlay.getBounds();
                        boundsObj.oldcontains = boundsObj.contains;
                        boundsObj.contains = function (arr) {
                            var newPoint = new EV.LngLat(arr[0], arr[1]);
                            return boundsObj.oldcontains(newPoint.lon, newPoint.lat);
                        };
                        overlay.getBounds = function () {
                            return boundsObj;
                        };

                        var bounds = overlay.bounds;
                        overlay.getPath = function () {
                            var point1 = bounds.getWN();
                            var point2 = bounds.getEN();
                            var point3 = bounds.getES();
                            var point4 = bounds.getWS();
                            var arr = [point1, point2, point3, point4];
                            for (var i = 0; i < arr.length; i++) {
                                arr[i].lng = arr[i].wgslon;
                                arr[i].lat = arr[i].wgslat;
                            }
                            return arr;
                        };
                        var result = {
                            obj: overlay
                        };
                        window.prevFence = overlay;
                        fenceOperation.createSuccess(result);
                        if (event.onDraw) {// 修改矩形围栏
                            event.onDraw(result);
                        }
                        this.deactivate();
                        return true;
                    });
                    addrect.activate();
                };

                /**
                 * 画圆
                 */
                event.circle = function (opt) {
                    var addcircle = toolbar.addTool("addcircle", {
                        text: "",  //显示的文字
                        name: "iaddcircle",
                        map: curMap
                    }, option);
                    addcircle.addEventListener("done", function (e) {
                        var overlay = e.overlay;
                        overlay.CLASS_NAME = 'Overlay.Circle';
                        overlay.oldGetCenter = overlay.getCenter;
                        overlay.getCenter = function () {
                            var arr = this.oldGetCenter();
                            arr.lng = arr.wgslon;
                            arr.lat = arr.wgslat;
                            return arr;
                        };
                        var result = {
                            obj: overlay
                        };
                        window.prevFence = overlay;
                        fenceOperation.createSuccess(result);
                        this.deactivate();
                        return true;
                    });
                    addcircle.activate();
                };
            }


            /**
             * 测算距离工具
             */
            event.rule = function (option) {
                var mlength = toolbar.addTool("mlength", {
                    text: "",  //显示的文字
                    name: "imlength",
                    map: curMap
                }, option);
                mlength.addEventListener("done", function (event) {
                    var overlay = event.overlay;
                    window.prevFence = overlay;
                    this.deactivate();
                });
                mlength.activate();
            };

            /**
             * 拉框放大
             */
            event.rectZoomIn = function () {
                curMap.t_zoomin();
            };

            /**
             * 拉框缩小
             */
            event.rectZoomOut = function () {
                curMap.t_zoomout();
            };
            event.on = function (event, callback) {
                this.onDraw = callback;
            };
            /**
             * 关闭画图工具
             * status false不清空所画覆盖物，true清空覆盖物
             */
            event.close = function (status) {
                curMap.t_nav();
                if (status) {
                    if (window.prevFence) {
                        window.prevFence.hide();
                        curMap.remove(window.prevFence);
                        window.prevFence = null;
                    }
                }
            };

            return event;
        };

        this.mapObj.buildings = function (option) {
            var event = new Object();
            event.setMap = function (opt) {

            };
            return event;
        };

        this.mapObj.plugin = function (plugnArr, callback) {
            return null;
        };
        this.mapObj.addControl = function (option) {

        };
        this.mapObj.toolBar = function (option) {
            return null;
        };

        this.mapObj.scale = function (option) {
            return null;
        };

        this.mapObj.setZoomAndCenter = function (zoom, point) {
            if (point[0]) {
                point = new EV.LngLat(point[0], point[1]);
            } else if (point.lng) {
                point = new EV.LngLat(point.lng, point.lat);
            }
            this.setLonLatZoom(point.wgslon, point.wgslat, zoom);
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

        this.mapObj.bounds = function (arr1, arr2) {
            var bounds = {};
            bounds.contains = function (lnglat) {
                return true;
            };

            return bounds;
        };

        this.mapObj.containerToLngLat = function (option) {
            return 0;
        };

        this.mapObj.getBounds = function (option) {
            return {
                getSouthWest: function () {
                    return {
                        lng: 0,
                        lat: 0,
                    };
                },
                getNorthEast: function () {
                    return {
                        lng: 0,
                        lat: 0,
                    };
                }
            };
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
                map.setMapType(0);
            };
            event.show = function () {
                map.setMapType(2);
            };
            return event;
        };

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

        this.mapObj.infoWindow = function (option) {
            var infoWindow = new EV.InfoWindow('nglpWindow', new EV.LngLat(0, 0), '', '', new EV.Size(200, 370));

            infoWindow.open = function (map, lngLat) {
                this.setLngLat(lngLat);
                this.setSize(new EV.Size(200, 370));
                this.setVisible(true);
                map.addPopup(this);
            };
            infoWindow.close = function () {
                this.hide();
            };
            infoWindow.setPosition = function (point) {
                this.setLngLat(point);
            };
            infoWindow.setWidth = function (width) {
                this.setSize(new EV.Size(width > 300 ? width : 200, 370));
                $("#basicStatusInformation").parent().css("width", "auto");
            };
            return infoWindow;
        };

        /*this.mapObj.icon = function (option) {
            return new AMap.Icon(option);
        };

        this.mapObj.size = function (option) {
            return new AMap.Size(option);
        };*/

        this.mapObj.hawkEye = function (option) {
            var map = this;
            var control;
            var overview = {};
            overview.show = function () {
                control = new EV.OverviewMapControl({
                    map: map,
                    isOpen: true,
                    visible: true,
                });
                map.addControl(control);
            };
            overview.open = function () {
            };
            overview.hide = function () {
                map.removeControl(control);
                control = null;
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

        this.mapObj.circleEditor = function (map, overView) {
            var listenerList = {
                'move': function () {
                },
                'adjust': function () {
                },
            };
            var event = new $.fn.mapEngine.createEvent(listenerList);

            event.open = function () {
            };
            event.close = function () {
            };
            return event;
        };

        this.mapObj.rectangle = function (option) {
            return null;
        };

        this.mapObj.rectangleEditor = function (map, overView) {
            var listenerList = {
                'move': function () {
                },
                'adjust': function () {
                },
            };
            var event = new $.fn.mapEngine.createEvent(listenerList);

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
                complete: function () {
                }
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

        this.mapObj.polylineEditor = function (map, overView) {
            var listenerList = {};
            var event = new $.fn.mapEngine.createEvent(listenerList);

            event.open = function () {
            };
            event.close = function () {
            };
            return event;
        };

        this.mapObj.polygonEditor = function (map, overView) {
            var listenerList = {};
            var event = new $.fn.mapEngine.createEvent(listenerList);

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
                newPoint.push(new EV.LngLat(path[i][0], path[i][1]))
            }
            var line = new EV.PolyLine(newPoint, option);
            line.on = function (event, callback) {
                if (event === 'click') {
                    EV.Event.addListener(map.vectors, "featurevertexclick", function (e) {
                        callback(e);
                    });
                }
            };
            line.setMap = function () {

            };
            line.setPath = function (path) {
                this.setLngLats(path);
            };
            this.addOverlay(line);
            return line;
        };

        this.mapObj.isPointOnSegment = function (clickLngLat, points1, points2) {

        };

        this.mapObj.polygon = function (option) {
            var thisMap = this;
            var path = option.path[0][0][0] instanceof Array ? option.path[0] : option.path;
            var newPoint = [];
            for (var i = 0; i < path.length; i++) {
                newPoint.push(new EV.LngLat(path[i][0], path[i][1]))
            }
            var polygon = new EV.Polygon(newPoint, option);
            polygon.on = function (event, callback) {
                if (event === 'click') {
                    EV.Event.addListener(thisMap.vectors, "featurevertexclick", function (e) {
                        callback(e);
                    });
                }
            };
            polygon.setMap = function (map) {
                if(map){
                    map.removeOverLay(this);
                }else{
                    thisMap.removeOverLay(this);
                }
            };
            this.addOverlay(polygon);
            return polygon;
        };

        this.mapObj.circle = function (option) {
            var circle = new EV.Circle(option.center, option.radius, option);
            this.addOverlay(circle);
            circle.setMap = function () {

            };
            return circle;
        };

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

        this.mapObj.threeDimensionalMap = function () {

        };

        this.mapObj.destroy = () => {
            this.mapObj = null;
        };

        return this.mapObj;
    };

    $.extend(true, $.fn.mapEngine.engine, {nglpMap: _nglpMap});
}(jQuery));