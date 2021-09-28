;(function ($) {
    var _amap = function (containerId) {
        var amap = new AMap.Map(containerId, {
            resizeEnable: true,
            zoom: 18,
            viewMode: '3D',
            defaultCursor: 'grab',
        });
        window.amap = amap;
        this.mapObj = amap;
        this.mapObj.currentMap = 'aMap';

        // 鼠标在地图上单击按下时触发
        this.mapObj.on('mousedown', () => {
            this.mapObj.setDefaultCursor('grabbing');
        });

        // 鼠标在地图上单击抬起时触发
        this.mapObj.on('mouseup', () => {
            this.mapObj.setDefaultCursor('grab');
        });

        /**天气*/
        var weather = new AMap.Weather();

        /**3D罗盘*/
        var controlBar = new AMap.ControlBar({
            position: {
                left: '10px',
                bottom: '140px'
            }
        });
        controlBar.addTo(this.mapObj);
        controlBar.hide();

        /**
         * 获取地图当前中心位置城市信息
         * 并展示其天气情况
         * */
        this.mapObj.getMapCenterCity = function () {
            this.getCity(function (info) {
                $('#currentCity').html(info.province + info.city + info.district);
                //查询实时天气信息, 查询的城市到行政级别的城市，如朝阳区、杭州市
                weather.getLive(info.district || '', function (err, data) {
                    if (!err) {
                        $('#weatherSapn').html(data.weather);
                        $('#temperatureSapn').html(data.temperature + '℃');
                        $('#windDirection').html(data.windDirection);
                        $('#windPower').html(data.windPower + ' 级');
                    } else {
                        $('#weatherSapn').html('--');
                        $('#temperatureSapn').html('--');
                        $('#windDirection').html('--');
                        $('#windPower').html('--');
                    }
                });
                //未来4天天气预报
                weather.getForecast(info.district || '', function (err, data) {
                    if (err) {
                        $('.forecastContent').html('');
                        return;
                    }
                    var str = [];
                    for (var i = 0, dayWeather; i < data.forecasts.length; i++) {
                        dayWeather = data.forecasts[i];
                        str.push(dayWeather.date + ' <span class="weather">' + dayWeather.dayWeather + '</span> ' + dayWeather.nightTemp + '~' + dayWeather.dayTemp + '℃');
                    }
                    $('.forecastContent').html(str.join('<br>'));
                });
            });
        };

        /**
         * 输入提示
         */
        this.mapObj.autoComplete = function (config) {
            $('#' + config.input).unbind();
            $('.tangram-suggestion-main').remove();
            $('input').inputClear();
            var autocomplete = new AMap.AutoComplete(config);
            return autocomplete;
        };

        /**
         * 标准地图
         */
        this.mapObj.standardMap = function () {
            var layer = new AMap.createDefaultLayer();
            this.setLayers([layer]);
            satellLayer.hide();
            controlBar.hide();
            this.setPitch(0);
            this.setRotation(0);
        };

        /**
         * 3D地图
         */
        this.mapObj.threeDimensionalMap = function () {
            var layer = new AMap.createDefaultLayer();
            this.setLayers([layer]);
            controlBar.show();
            this.setPitch(90);
            this.setRotation(45);
        };
        /**
         * 卫星地图
         */
        this.mapObj.satelliteMap = function () {
            controlBar.hide();
            this.setPitch(0);
            this.setRotation(0);
            var layer = new AMap.TileLayer.Satellite();
            this.setLayers([layer]);
        };

        /**
         * 卫星路网
         */
        this.mapObj.satelliteRoadMap = function () {
            controlBar.hide();
            this.setPitch(0);
            this.setRotation(0);
            this.setLayers([
                // 卫星
                new AMap.TileLayer.Satellite(),
                // 路网
                new AMap.TileLayer.RoadNet()
            ]);
        };

        this.mapObj.oldLngLatToContainer = this.mapObj.lngLatToContainer;
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
                point = [lngLat.wgslon, lngLat.wgslat];
            } else if (lngLat.lng) {
                point = [lngLat.lng, lngLat.lat];
            }
            return this.oldLngLatToContainer(point);
        };

        this.mapObj.lngLat = function (lng, lat) {
            return new AMap.LngLat(lng, lat);
        };

        this.mapObj.text = function (param) {
            return new AMap.Text(param);
        };

        this.mapObj.markerCluster = function (map, points, option) {
            return new AMap.MarkerCluster(map, points, option);
        };

        this.mapObj.pixel = function (pix1, pix2) {
            return new AMap.Pixel(pix1 || 0, pix2 || 0);
        };

        this.mapObj.bounds = function (pix1, pix2) {
            return new AMap.Bounds(pix1, pix2);
        };

        this.mapObj.createDefaultLayer = function (option) {
            return new AMap.createDefaultLayer(option);
        };

        this.mapObj.marker = function (option) {
            var marker;
            if (option.icon && typeof option.icon === "string" && option.icon.indexOf('sectionPoint') === -1 && !option.label) {
                var zoomStyleMapping1 = {
                    11: 0,
                    12: 0,
                    13: 0,
                    14: 0,
                    15: 0,
                    16: 0,
                    17: 0,
                    18: 0,
                    19: 0,
                    20: 0
                };
                // 灵活点标记，一种可以随着地图级别变化而改变图标和大小的点标记
                marker = new AMap.ElasticMarker({
                    map: this,
                    position: option.position,
                    zooms: [2, 20],
                    styles: [{
                        icon: {
                            img: option.icon,
                            anchor: 'center',
                            fitZoom: 18,
                            scaleFactor: 1.06,
                            maxScale: 2,
                            minScale: 0.2
                        },
                    }],
                    zoomStyleMapping: zoomStyleMapping1
                });
            } else {
                marker = new AMap.Marker(option);
            }
            return marker;
        };

        this.mapObj.districtSearch = function (option) {
            return new AMap.DistrictSearch(option);
        };

        this.mapObj.mouseTool = function (option) {
            return new AMap.MouseTool(option);
        };

        this.mapObj.buildings = function (option) {
            return new AMap.Buildings(option);
        };

        this.mapObj.plugin = function (plugnArr, callback) {
            return new AMap.plugin(plugnArr, callback);
        };

        this.mapObj.toolBar = function (option) {
            return new AMap.ToolBar(option);
        };

        this.mapObj.scale = function (option) {
            return new AMap.Scale(option);
        };

        this.mapObj.tileLayer = function (option) {
            return new AMap.TileLayer(option);
        };

        this.mapObj.satellite = function (option) {
            return new AMap.TileLayer.Satellite(option);
        };

        this.mapObj.traffic = function (option) {
            var map = this;
            var layer = new AMap.TileLayer.Traffic(option);
            var obj = {
                show: function () {
                    map.add([layer]);
                },
                hide: function () {
                    map.removeLayer(layer);
                },
                setMap: function () {

                }
            };
            return obj;
        };

        this.mapObj.infoWindow = function (option) {
            return new AMap.InfoWindow(option);
        };

        this.mapObj.icon = function (option) {
            return new AMap.Icon(option);
        };

        this.mapObj.size = function (x, y) {
            return new AMap.Size(x, y);
        };

        this.mapObj.hawkEye = function (option) {
            return new AMap.HawkEye(option);
        };

        this.mapObj.imageLayer = function (option) {
            return new AMap.ImageLayer(option);
        };

        this.mapObj.wmts = function (option) {
            return new AMap.TileLayer.WMTS(option);
        };

        this.mapObj.circleEditor = function (map, option) {
            return new AMap.CircleEditor(map, option);
        };

        this.mapObj.rectangle = function (option) {
            return new AMap.Rectangle(option);
        };

        this.mapObj.rectangleEditor = function (map, option) {
            return new AMap.RectangleEditor(map, option);
        };

        this.mapObj.placeSearch = function (option) {
            return new AMap.PlaceSearch(option);
        };

        this.mapObj.geocoder = function (option) {
            return new AMap.Geocoder(option);
        };

        this.mapObj.truckDriving = function (option) {
            return new AMap.TruckDriving(option);
        };

        this.mapObj.driving = function (option) {
            return new AMap.Driving(option);
        };

        this.mapObj.contextMenu = function (option) {
            return new AMap.ContextMenu(option);
        };

        this.mapObj.polylineEditor = function (map, option) {
            return new AMap.PolylineEditor(map, option);
        };

        this.mapObj.polygonEditor = function (map, option) {
            return new AMap.PolygonEditor(map, option);
        };

        this.mapObj.polyline = function (option) {
            return new AMap.Polyline(option);
        };

        this.mapObj.isPointOnSegment = function (clickLngLat, points1, points2) {
            return AMap.GeometryUtil.isPointOnSegment(clickLngLat, points1, points2);
        };

        this.mapObj.polygon = function (option) {
            return new AMap.Polygon(option);
        };

        this.mapObj.circle = function (option) {
            return new AMap.Circle(option);
        };

        this.mapObj.dragRoute = function (map, array) {
            return new AMap.DragRoute(map, array, AMap.DrivingPolicy.REAL_TRAFFIC);
        };

        return this.mapObj;
    };

    $.extend(true, $.fn.mapEngine.engine, {amap: _amap});
}(jQuery));