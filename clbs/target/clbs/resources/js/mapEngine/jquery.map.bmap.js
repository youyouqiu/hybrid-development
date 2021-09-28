;(function ($) {
    // 百度地图
    var _bmap = function (containerId) {
        this.mapObj = new BMapGL.Map(containerId, {
            mapType: BMAP_EARTH_MAP
        });
        $('#POISearch').hide();
        this.mapObj.enableScrollWheelZoom();
        this.mapObj.currentMap = 'baidu';
        window.BMap = window.BMapGL || {};
        this.mapObj.centerAndZoom(new BMapGL.Point(116.404, 39.915), 4);
        /**3D罗盘*/
        var navi3DCtrl = new BMapGL.NavigationControl3D();
        this.mapObj.addControl(navi3DCtrl);
        navi3DCtrl.hide();
        var _this = this.mapObj;
        /**
         * 标准地图
         */
        this.mapObj.standardMap = function () {
            $('.trafficBtn').prop('disabled', false);
            this.setMapType(BMAP_NORMAL_MAP);
            this.setZoom(18);
            this.setHeading(0);
            this.setTilt(0);
            navi3DCtrl.show();
        };

        /**
         * 3D地图
         */
        this.mapObj.threeDimensionalMap = function () {
            $('.trafficBtn').prop('disabled', false);
            this.setMapType(BMAP_NORMAL_MAP);
            this.setZoom(18);
            this.setHeading(64.5);
            this.setTilt(73);
            navi3DCtrl.show();
        };

        /**
         * 三维地球
         * */
        this.mapObj.dimensionalMap = function () {
            $('.trafficBtn').removeClass('preBlue').prop('disabled', true);
            this.setTrafficOff();
            this.setHeading(0);
            this.setTilt(0);
            navi3DCtrl.hide();
            this.setMapType(BMAP_EARTH_MAP);
            $('.BMap_stdMpZoom').hide();
        };

        /**
         * 全景地图
         * */
        this.mapObj.panoramicMap = function () {
            this.addTileLayer(new BMap.PanoramaCoverageLayer());
            var panorama = new BMap.Panorama('panorama');
            panorama.setPov({heading: -40, pitch: 6});
            if (markerFocus) {
                var infoArr = markerAllUpdateData.get(markerFocus);
                var point = this.aMapTransBMap(infoArr[2], infoArr[3]);
                panorama.setPosition(new BMap.Point(point[0], point[1])); //根据经纬度坐标展示全景图
            } else {
                layer.msg('请双击选择监控对象');
            }
        };

        /**
         * 输入提示
         */
        this.mapObj.autoComplete = function (config) {
            $('#' + config.input).unbind();
            var ac = new BMapGL.Autocomplete(config);
            $('.amap-sug-result').remove();
            var map = this;
            var myValue;
            var isDrawMarker = [];
            ac.addEventListener("onconfirm", function (e) {    //鼠标点击下拉列表后的事件
                var _value = e.item.value;
                myValue = _value.province + _value.city + _value.district + _value.street + _value.business;
                setPlace();
            });
            $('input').inputClear().on('onClearEvent', function (e, data) {
                var id = data.id;
                if (id == config.input) {
                    for (var i = 0; i < isDrawMarker.length; i++) {
                        map.removeOverlay(isDrawMarker[i]);    //清除地图上覆盖物
                    }
                    isDrawMarker = [];
                }
            });
            function setPlace() {
                if (config.input === 'tipinput') {
                    for (var i = 0; i < isDrawMarker.length; i++) {
                        map.removeOverlay(isDrawMarker[i]);    //清除地图上覆盖物
                    }
                    isDrawMarker = [];
                }
                function myFun() {
                    var pp = local.getResults().getPoi(0).point;    //获取第一个智能搜索的结果
                    if (config.input === 'tipinput') {
                        map.centerAndZoom(pp, 18);
                        var curMarker = new BMap.Marker(pp);
                        isDrawMarker.push(curMarker);
                        map.addOverlay(curMarker);    //添加标注
                    } else {
                        var selectObj = local.getResults().getPoi(0);
                        selectObj.district = selectObj.province + selectObj.city;
                        selectObj.name = selectObj.title;
                        ac.selectBack.bind($('#' + config.input))({poi: selectObj});
                    }
                }
                var local = new BMap.LocalSearch(map, { //智能搜索
                    onSearchComplete: myFun
                });
                local.search(myValue);
            }
            if (config.input !== 'tipinput') {
                ac.on = function (event, callback) {
                    ac.selectBack = callback;
                }
            }
            return ac;
        };

        /**
         * 清除彈窗
         */
        this.mapObj.clearInfoWindow = function (config) {
            var infoWindow = this.getInfoWindow();
            if (infoWindow) {
                this.removeOverlay(infoWindow);
            }
            return infoWindow;
        };

        /**
         * 标准经纬度创建
         * @param lng
         * @param lat
         * @returns {BMapGL.Point}
         */
        this.mapObj.lngLat = function (lng, lat) {
            var obj = new BMapGL.Point(lng, lat);
            var _this = this;
            obj.distance = function (pos) {
                return _this.getDistance(obj, new BMapGL.Point(pos.lng, pos.lat));
            };
            return obj;
        };

        /**
         * 创建文本标注
         * @param param
         * @returns {BMapGL.Label}
         */
        this.mapObj.text = function (param) {
            return new BMapGL.Label(param.text, {
                position: param.position,
            });
        };

        /**
         * 创建聚合点
         * @param map
         * @param points
         * @param option
         * @returns {BMapGL.MarkerClusterer}
         */
        this.mapObj.markerCluster = function (map, points, option) {
            /*var arrayObj = [];
            for (var i = 0; i < points.length; i++) {
                var newPoint = _this.aMapTransBMap(points[i].lnglat[0], points[i].lnglat[1]);
                var marker = new BMapGL.Marker(new BMapGL.Point(newPoint[0], newPoint[1]));
                arrayObj.push(marker);
            }
            var markers = {};
            try {
                markers = new BMapLib.MarkerClusterer(map, {
                    markers: arrayObj,
                    maxZoom: 11,
                    minClusterSize: 1
                });
            } catch (error) {
                map.remove(arrayObj);
            }*/
            var markers = {};
            markers.setMap = function (status) {
                /*if (!status) {
                    if (this.clearMarkers) {
                        this.clearMarkers();
                    } else {
                        map.remove(arrayObj);
                    }
                }*/
            };
            markers.on = function (event, callback) {};
            return markers;
        };

        /**
         * 设置偏移量
         * @param pix1
         * @param pix2
         * @returns {Point|boolean|t|t}
         */
        this.mapObj.pixel = function (pix1, pix2) {
            return new BMapGL.Pixel(pix1, pix2);
        };

        /**
         * 设置一个矩形区域
         * @param pix1
         * @param pix2
         */
        this.mapObj.bounds = function (pix1, pix2) {
            var boundsObj = new BMapGL.Bounds(new BMapGL.Point(pix1[0], pix1[1]), new BMapGL.Point(pix2[0], pix2[1]));
            var _this = this;
            boundsObj.contains = function (pos) {
                var newPos = _this.aMapTransBMap(pos[0], pos[1]);
                return _this.getBounds().containsPoint(new BMapGL.Point(newPos[0], newPos[1]));
            };
            return boundsObj;
        };

        /**
         * 添加图层
         * @param option
         */
        this.mapObj.createDefaultLayer = function (option) {
            var lay = new BMapGL.TileLayer.WMS(option.tileUrl, {zIndex: option.zIndex});
            lay.setMap = function (opt) {
                this.addTileLayer(lay);
            };
            lay.hide = function () {};
            return lay;
        };

        /**
         * 将腾讯/高德地图经纬度转换为百度地图经纬度
         * */
        this.mapObj.aMapTransBMap = function (lng, lat) {
            var X_PI = Math.PI * 3000.0 / 180.0;
            var x = lng, y = lat;
            var z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * X_PI);
            var theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * X_PI);
            var bd_lng = (z * Math.cos(theta) + 0.0065).toFixed(8);
            var bd_lat = (z * Math.sin(theta) + 0.006).toFixed(8);
            return [bd_lng, bd_lat];
        };
        this.mapObj.lnglatTransFun = this.mapObj.aMapTransBMap;

        /**
         * 将百度地图经纬度转换为腾讯/高德地图经纬度
         * */
        this.mapObj.bMapTransAMap = function (lng, lat) {
            let x_pi = 3.14159265358979324 * 3000.0 / 180.0;
            let x = lng - 0.0065;
            let y = lat - 0.006;
            let z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
            let theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
            let lngs = z * Math.cos(theta);
            let lats = z * Math.sin(theta);
            return [lngs.toFixed(6), lats.toFixed(6)];
        };
        this.mapObj.lnglatTransToAmap = this.mapObj.bMapTransAMap;

        /**
         * 获取图片宽高信息
         * */
        this.mapObj.getImgInfo = function (imgUrl) {
            var dtd = $.Deferred();
            // 创建对象
            var img = new Image();
            // 改变图片的src
            img.src = imgUrl;
            //加载完成执行
            img.onload = function () {
                dtd.resolve({width: img.width, height: img.height});
            };
            img.onerror = function () {
                dtd.reject({width: 58, height: 30});
            };
            return dtd.promise();
        };

        /**
         * 创建标注点
         * @param option
         * @returns {BMapGL.Marker}
         */
        this.mapObj.marker = function (option) {
            var marker;
            var pos = option.position;
            var position = this.aMapTransBMap(option.position[0], option.position[1]);
            if (option.icon || !option.content) {
                if (option.icon) {
                    if (!option.label) {// 监控对象图标
                        var size = new BMapGL.Size(19, 31);
                        if (typeof option.icon === "string" && option.icon.indexOf('mark_b') === -1 && option.icon.indexOf('sectionPoint') === -1) {
                            // var result = await this.getImgInfo(option.icon);
                            // console.log('result', result);
                            size = new BMapGL.Size(58, 30);
                        }
                        var icon = option.icon;
                        if (typeof option.icon === "string") {
                            icon = new BMapGL.Icon(option.icon, size);
                        }
                        if (typeof option.icon === "string" && option.icon.indexOf('sectionPoint') !== -1) {// 用于绘制电子围栏路线分段marker
                            marker = new BMapGL.Marker(pos.lng ? pos : new BMapGL.Point(position[0], position[1]), {
                                icon: icon,
                                title: option.number,
                                offset: new BMapGL.Size(0, -15) // 设置文本偏移量
                            });
                        } else {
                            marker = new BMapGL.Marker(pos.lng ? pos : new BMapGL.Point(position[0], position[1]), {
                                icon: icon,
                            });
                        }
                        marker.extData = option.id;
                    } else {
                        var point = new BMapGL.Point(pos.lng, pos.lat);
                        var icon = new BMapGL.Icon(option.icon, new BMapGL.Size(10, 10));
                        marker = new BMapGL.Marker(point, {
                            icon: icon,
                        });
                        var markerLabel = new BMapGL.Label(option.label.content, {
                            position: point,
                            offset: new BMapGL.Size(-2, 6) // 设置文本偏移量
                        });
                        this.addOverlay(markerLabel);
                        marker.markerLabel = markerLabel;
                    }
                } else {
                    marker = new BMapGL.Marker(pos.lng ? pos : new BMapGL.Point(position[0], position[1]));
                }
            } else {
                marker = new BMapGL.Label(option.content, {
                    position: pos.lng ? pos : new BMapGL.Point(position[0], position[1]),
                    offset: new BMapGL.Size(30, -40) // 设置文本偏移量
                });
                // 自定义文本标注样式
                marker.setStyle({
                    borderColor: 'transparent',
                    padding: '0px',
                    backgroundColor: 'transparent'
                });
                marker.setOffset = function () {};
                marker.monitorId = option.id;
            }
            marker.oldsetIcon = marker.setIcon;
            marker.setIcon = function (url) {
                if (typeof url === 'string') {
                    var icon = new BMapGL.Icon(url, new BMapGL.Size(32, 32));
                    this.oldsetIcon(icon);
                } else {
                    this.oldsetIcon(url);
                }
            };
            marker.setAngle = function (angle) {
                /* console.log('angle', angle, _this.getZoom());
                 if (_this.getZoom() < 11) return;*/
                this.setRotation(angle)
            };
            marker.getAngle = function () {
                return this.getRotation() || 0;
            };
            marker.stopMove = function () {
                if (this._intervalFlag) {
                    clearInterval(this._intervalFlag);
                    this._intervalFlag = null;
                }
            };
            marker.setMap = function (map) {
                if (map) {
                    map.addOverlay(this);
                } else {
                    if (this._intervalFlag) {
                        clearInterval(this._intervalFlag);
                        this._intervalFlag = null;
                    }
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
            var _this = this;
            /**
             *缓动效果
             *初始坐标，目标坐标，当前的步长，总的步长
             *@param{BMap.Pixel} initPos 初始平面坐标
             *@parm{BMap.Pixel} targetPos 目标平面坐标
             *@param{number} 当前帧数
             *@param {number} count 总帧数
             * */
            marker.effect = function (initPos, targetPos, currentCount, count) {
                var b = initPos, c = targetPos - initPos, t = currentCount,
                    d = count;
                return c * t / d + b;
            };

            /**
             *监控对象移动
             *@param {Point} prevPoint 开始坐标(prevPoint)
             *@param {Point} newPoint 目标点坐标
             *@param {Function} 动画效果
             *@return  无返回值
             */
            marker.oldPosition = marker.setPosition;
            marker.moveTo = function (point, opt) {
                if (_this.getZoom() < 11) return;
                var prevPoint = marker.getPosition();
                var pos = _this.aMapTransBMap(point[0], point[1]);
                var newPoint = new BMapGL.Point(pos[0], pos[1]);
                var runTime = opt ? opt.duration : 1000;
                var intervalTimer = 10;
                var me = this,
                    //当前帧数
                    currentCount = 0,
                    //初始坐标
                    _prevPoint = _this.lnglatToMercator(prevPoint.lng, prevPoint.lat),//将球面坐标转换为平面坐标
                    //获取结束点的(x,y)坐标
                    _newPoint = _this.lnglatToMercator(newPoint.lng, newPoint.lat),
                    //两点之间要循环定位的次数
                    count = runTime / intervalTimer;
                // count = count > 500 ? 500 : count;
                //两点之间匀速移动
                me._intervalFlag = setInterval(function () {
                    //两点之间当前帧数大于总帧数的时候，则说明已经完成移动
                    if (currentCount >= count) {
                        clearInterval(me._intervalFlag);
                        me._intervalFlag = null;
                        // 判断是否为聚焦跟踪监控对象
                        if (markerFocus && me.extData && markerFocus == me.extData) {
                            if (!pathsTwo.contains(newPoint)) {
                                _this.oldSetCenter(newPoint);
                                amapOperation.LimitedSizeTwo();
                            }
                        }
                        amapOperation.markerMoveendFun(me, me.extData);
                    } else {
                        //动画移动
                        currentCount++;//计数
                        var x = me.effect(_prevPoint[0], _newPoint[0], currentCount, count),
                            y = me.effect(_prevPoint[1], _newPoint[1], currentCount, count);
                        //根据平面坐标转化为球面坐标
                        var pos = _this.mercatorToLnglat(x, y);
                        //设置marker角度(两点之间的距离车的角度保持一致)
                        if (currentCount == 1) {
                            //转换角度
                            // var angle = amapOperation.calcAngle(prevPoint, newPoint);
                            // console.log('angle', angle);
                            // me.setRotation(angle + 360);
                        }
                        //正在移动
                        var curPos = new BMapGL.Point(pos[0], pos[1]);
                        me.oldPosition(curPos);
                        var carContent = carNameMarkerContentMap.get(me.extData);
                        if (carContent) {
                            carContent.oldPosition(curPos);
                        }
                    }
                }, intervalTimer);
                me._prevPoint = newPoint;
            };
            marker.setPosition = function (value) {
                if (this._intervalFlag) {
                    clearInterval(this._intervalFlag);
                    this._intervalFlag = null;
                }
                if (_this.getZoom() < 11) return;
                if (marker.monitorId && markerViewingArea) {
                    var carMarker = markerViewingArea.get(marker.monitorId);
                    if (carMarker && carMarker[0]._intervalFlag) return;
                }
                if (value.lng) {
                    this.oldPosition(value);
                } else {
                    var newPos = _this.aMapTransBMap(value[0], value[1]);
                    this.oldPosition(new BMapGL.Point(newPos[0], newPos[1]));
                }
            };
            /* marker.moveTo = function (pos, opt) {
                 var newPos = _this.aMapTransBMap(pos[0], pos[1]);
                 marker.oldPosition(new BMapGL.Point(newPos[0], newPos[1]));
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
             };*/
            this.addOverlay(marker);
            return marker;
        };

        /**
         * 行政区域查询
         * @param option
         */
        this.mapObj.districtSearch = function (option) {
            var _this = this;
            var bdary = new BMapGL.Boundary();
            bdary.search = function (name, callback) {
                bdary.get(name, function (rs) {
                    var pointArr = [];
                    var boundaries = rs.boundaries;
                    for (var i = 0; i < boundaries.length; i++) {
                        var item = boundaries[i].split(';');
                        var add = [];
                        for (var j = 0; j < item.length; j++) {
                            var point = item[j].split(',');
                            var newPoint = _this.bMapTransAMap(point[0], point[1]);
                            add.push([newPoint[0], newPoint[1]]);
                        }
                        pointArr.push(add);
                    }
                    callback('complete', {
                        districtList: [{
                            boundaries: pointArr
                        }]
                    })
                })
            };
            bdary.setLevel = function () {};
            bdary.setExtensions = function () {};
            return bdary;
        };

        this.mapObj.mouseTool = function (map) {
            var _this = this;
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
            var drawingManager = new BMapGLLib.DrawingManager(map, {
                // isOpen: true,        // 是否开启绘制模式
                enableCalculate: false, // 绘制是否进行测距测面
                enableSorption: true,   // 是否开启边界吸附功能
                sorptiondistance: 20,   // 边界吸附距离
                circleOptions: styleOptions,     // 圆的样式
                polylineOptions: styleOptions,   // 线的样式
                polygonOptions: styleOptions,    // 多边形的样式
                rectangleOptions: styleOptions,  // 矩形的样式
                labelOptions: labelOptions,      // label样式
            });
            drawingManager.addEventListener("markercomplete", function (e, overlay) {
                var lngLat = overlay.getPosition();
                overlay.CLASS_NAME = 'AMap.Marker';
                overlay.getPosition = function () {
                    var newPos = _this.bMapTransAMap(lngLat.lng, lngLat.lat);
                    return {
                        lng: newPos[0],
                        lat: newPos[1],
                    }
                };
                var result = {
                    obj: overlay
                };
                window.prevFence = overlay;
                fenceOperation.createSuccess(result);
            });
            drawingManager.addEventListener("rectanglecomplete", function (e, overlay) {
                overlay.CLASS_NAME = 'Overlay.Rectangle';
                var bounds = overlay.getBounds();
                bounds.contains = function (arr) {
                    var newPoint = _this.aMapTransBMap(arr[0], arr[1]);
                    return bounds.containsPoint(new BMapGL.Point(newPoint[0], newPoint[1]));
                };
                bounds.northEast = bounds.ne;
                bounds.southWest = bounds.sw;
                var northEast = _this.lnglatToMercator(bounds.ne.lng, bounds.ne.lat);
                var southWest = _this.lnglatToMercator(bounds.sw.lng, bounds.sw.lat);
                bounds.northEast.pos = [northEast[0], northEast[1]];
                bounds.southWest.pos = [southWest[0], southWest[1]];
                overlay.getArea = function () {
                };
                overlay.getBounds = function () {
                    return bounds;
                };
                var rectangle = $('.areaMeasurementList p[data-type="rectangle"]').hasClass('active');
                if (!rectangle) {
                    var path = overlay.getPath();
                    var newPath = [];
                    for (var i = 0; i < path.length; i++) {
                        var newPoint = _this.bMapTransAMap(path[i].lng, path[i].lat);
                        newPath.push({
                            lng: newPoint[0],
                            lat: newPoint[1]
                        })
                    }
                    overlay.getPath = function () {
                        return newPath;
                    };
                    var result = {
                        obj: overlay
                    };
                    window.prevFence = overlay;
                    fenceOperation.createSuccess(result);
                    if (drawingManager.onDraw) {// 修改矩形围栏
                        drawingManager.onDraw(result);
                    }
                } else {
                    areaOverlay = overlay;
                    amapFunCollection.createRectangleText(overlay);
                    // drawingManager.removeEventListener("rectanglecomplete");
                }
            });
            drawingManager.addEventListener("circlecomplete", function (e, overlay) {
                overlay.CLASS_NAME = 'Overlay.Circle';
                var center = overlay.getCenter();
                var newPoint = _this.bMapTransAMap(center.lng, center.lat);
                overlay.getCenter = function () {
                    return newPoint;
                };
                var result = {
                    obj: overlay
                };
                var circle = $('.areaMeasurementList p[data-type="circle"]').hasClass('active');
                if (!circle) {
                    window.prevFence = overlay;
                    fenceOperation.createSuccess(result);
                } else {
                    areaOverlay = overlay;
                    amapFunCollection.createCircleText(overlay);
                }
            });
            drawingManager.addEventListener("polygoncomplete", function (e, overlay) {
                overlay.CLASS_NAME = 'Overlay.Polygon';
                var path = overlay.getPath();
                var newPath = [];
                for (var i = 0; i < path.length; i++) {
                    var newPoint = _this.bMapTransAMap(path[i].lng, path[i].lat);
                    newPath.push({
                        lng: newPoint[0],
                        lat: newPoint[1]
                    })
                }
                overlay.getPath = function () {
                    return newPath;
                };
                var result = {
                    obj: overlay
                };
                window.prevFence = overlay;
                fenceOperation.createSuccess(result);
            });
            drawingManager.addEventListener("polylinecomplete", function (e, overlay) {
                overlay.CLASS_NAME = 'Overlay.Polyline';
                var path = overlay.getPath();
                var newPath = [];
                for (var i = 0; i < path.length; i++) {
                    var newPoint = _this.bMapTransAMap(path[i].lng, path[i].lat);
                    newPath.push({
                        lng: newPoint[0],
                        lat: newPoint[1]
                    })
                }
                overlay.getPath = function () {
                    return newPath;
                };
                var result = {
                    obj: overlay
                };
                window.prevFence = overlay;
                fenceOperation.createSuccess(result);
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
                if (drawingManager) drawingManager.close();
                drawingManager.setDrawingMode(BMAP_DRAWING_MARKER);
                drawingManager.open();
            };

            /**
             * 画多边形
             */
            event.polygon = function (opt) {
                if (drawingManager) drawingManager.close();
                drawingManager.setDrawingMode(BMAP_DRAWING_POLYGON);
                drawingManager.open();
            };

            /**
             * 画线
             */
            event.polyline = function (opt) {
                if (drawingManager) drawingManager.close();
                drawingManager.setDrawingMode(BMAP_DRAWING_POLYLINE);
                drawingManager.open();
            };
            /**
             * 画矩形
             */
            event.rectangle = function (opt) {
                if (drawingManager) drawingManager.close();
                drawingManager.setDrawingMode(BMAP_DRAWING_RECTANGLE);
                drawingManager.open();
            };

            /**
             * 画圆
             */
            event.circle = function (opt) {
                if (drawingManager) drawingManager.close();
                drawingManager.setDrawingMode(BMAP_DRAWING_CIRCLE);
                drawingManager.open();
            };

            /**
             * 关闭画图工具
             * status false不清空所画覆盖物，true清空覆盖物
             */
            event.close = function (status) {
                drawingManager.close();
                if (status) {
                    if (handler) handler.close();
                    if (window.prevFence) {
                        _this.remove(window.prevFence);
                        window.prevFence = null;
                    }
                    if (window.areaOverlay) {
                        _this.remove([window.areaOverlay, textMarker]);
                        window.areaOverlay = null;
                    }
                }
            };

            /**
             * 测算距离工具
             */
            event.rule = function (option) {
                if (handler) handler.close();
                handler = new BMapGLLib.DistanceTool(map);
                handler.open();
            };

            /**
             * 拉框放大
             */
            event.rectZoomIn = function () {
                handler = new BMapLib.RectangleZoom(map, {
                    followText: "拖拽鼠标进行操作"
                });
                handler.open();
            };

            /**
             * 拉框缩小
             */
            event.rectZoomOut = function () {
                handler = new BMapLib.RectangleZoom(map, {
                    followText: "拖拽鼠标进行操作",
                    zoomType: 1
                });
                handler.open();
            };
            return event;
        };

        /**
         * 设置3D楼块图层
         * @param option
         */
        this.mapObj.buildings = function (option) {
            var event = new Object();
            event.setMap = function (opt) {};
            return event;
        };

        /**
         * 添加缩放工具
         * @param option
         */
        this.mapObj.toolBar = function () {
            var zoomCtrl = new BMapGL.ZoomControl();  // 添加比例尺控件
            this.addControl(zoomCtrl);
            return zoomCtrl;
        };

        /**
         * 添加比例尺工具
         * @returns {BMapGL.Control.Scale}
         */
        this.mapObj.scale = function () {
            var scaleCtrl = new BMapGL.ScaleControl();  // 添加比例尺控件
            this.addControl(scaleCtrl);
            return scaleCtrl;
        };

        /**
         * 地图添加卫星图层
         * @param option
         */
        this.mapObj.satellite = function (option) {
            var _this = this;
            var event = new Object();
            event.setMap = function () {};
            event.hide = function () {
                _this.setMapType(BMAP_NORMAL_MAP);
            };
            event.show = function () {
                _this.setMapType(BMAP_EARTH_MAP);
            };
            return event;
        };

        /**
         * 开启路况
         * @param option
         */
        this.mapObj.traffic = function (option) {
            var _this = this;
            var event = new Object();
            event.setMap = function () {};
            event.hide = function () {
                _this.setTrafficOff();
            };
            event.show = function () {
                _this.setTrafficOn();
            };
            return event;
        };

        /**
         * 信息弹窗
         * @param option
         * @returns {BMapGL.InfoWindow}
         */
        this.mapObj.infoWindow = function (option) {
            var currentWindow = null;// 当前显示的信息窗口
            var infoWindow = new BMapGL.InfoWindow('', {
                width: 240,
                height: 0,
                closeOnClick: true
            });
            var _this = this;
            infoWindow.open = function (map, lnglat, content) {
                currentWindow = new BMapGL.InfoWindow(content, {
                    width: 240,
                    height: 0,
                    closeOnClick: true
                });
                if (lnglat.lng) {
                    _this.openInfoWindow(currentWindow, lnglat);
                } else {
                    var newPoint = _this.aMapTransBMap(lnglat.lng, lnglat.lat);
                    _this.openInfoWindow(currentWindow, new BMapGL.Point(newPoint[0], newPoint[1]));
                }
            };
            infoWindow.setWidth = function (width) {
                if (currentWindow) {
                    currentWindow.setWidth(width);
                }
            };
            infoWindow.setPosition = function (lnglat) {
                if (!lnglat || !currentWindow) return;
                if (lnglat.lng) {
                    _this.openInfoWindow(currentWindow, lnglat);
                } else {
                    var newPoint = _this.aMapTransBMap(lnglat.lng, lnglat.lat);
                    _this.openInfoWindow(currentWindow, new BMapGL.Point(newPoint[0], newPoint[1]));
                }
            };
            return infoWindow;
        };

        /**
         * 标注物所用图标
         * @param option
         */
        this.mapObj.icon = function (option) {
            return new BMapGL.Icon(option.image, option.size);
        };

        /**
         * 以像素坐标表示的地图上的一个点。
         * @param x
         * @param y
         * @returns {Point|boolean|t|t}
         */
        this.mapObj.size = function (x, y) {
            return new BMapGL.Size(x, y);
        };

        /**
         * 鹰眼
         * @param option
         */
        this.mapObj.hawkEye = function (option) {
            /*var overview = new BMapGL.Control.OverviewMap();
            overview.show = function () {
                overview.changeView();
            };
            overview.open = function () {};
            overview.hide = function () {
                overview.changeView();
            };
            overview.close = function () {};
            return overview;*/
        };

        /**
         * 添加自定义图层
         * @param option
         */
        this.mapObj.imageLayer = function (option) {
            return {
                show: function () {},
                hide: function () {}
            };
        };

        /**
         * 实现在地图上叠加自定义的WMS地图图块层
         * @param option
         */
        this.mapObj.wmts = function (option) {
            var lay = new Object(); // new BMapGL.TileLayer.WMS(option.url);
            lay.setMap = function (map) {};
            lay.hide = function () {};
            lay.show = function () {};
            return lay;
        };

        /**
         * 圆编辑
         * @param map
         * @param option
         */
        this.mapObj.circleEditor = function (map, circle) {
            circle.enableEditing();
            circle.open = circle.enableEditing;
            circle.close = circle.disableEditing;
            return circle;
        };

        /**
         *  创建矩形
         * @param option
         */
        this.mapObj.rectangle = function (option) {
            var thisMap = this;
            var rect = new BMapGL.Rectangle(option.bounds, {
                fillColor: option.fillColor,
                fillOpacity: option.fillOpacity,
                lineStyle: option.strokeColor,
            });
            this.addOverlay(rect);
            rect.setMap = function (map) {
                if (map) {
                    map.addOverlay(this);
                } else {
                    thisMap.removeOverlay(this);
                }
            };
            return rect;
        };

        /**
         * 编辑矩形
         * @param map
         * @param overView
         * @returns {Object}
         */
        this.mapObj.rectangleEditor = function (map, rectangle) {
            rectangle.enableEditing();
            rectangle.open = rectangle.enableEditing;
            rectangle.close = rectangle.disableEditing;
            return rectangle;
        };

        /**
         * POI搜索
         * @param option
         */
        this.mapObj.placeSearch = function (option) {
            var localSearchResult;
            var localSearch = new BMapGL.LocalSearch(this, {
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
            return event;
        };

        /**
         * 逆地理编码
         * @param option
         */
        this.mapObj.geocoder = function (option) {
            var geocoder = new BMapGL.Geocoder();
            geocoder.oldgetLocation = geocoder.getLocation;
            geocoder.getAddress = function (lnglat) {
                var point = new BMapGL.Point(lnglat[0], lnglat[1]);
                geocoder.oldgetLocation(point, function (result) {
                    var obj = {
                        type: 'complete',
                        geocodes: [{
                            location: result
                        }],
                        regeocode: {
                            formattedAddress: result.address,
                            addressComponent: {
                                township: result.addressComponents.street || result.addressComponents.district
                            },
                        }
                    };
                    geocoder.oncomplete(obj);
                });
            };
            geocoder.getLocation = function (address) {
                geocoder.getPoint(address, function (result) {
                    var obj = {
                        type: 'complete',
                        geocodes: [{
                            location: result
                        }],
                        regeocode: {
                            formattedAddress: '',
                            addressComponent: {
                                township: null
                            },
                        }
                    };
                    geocoder.oncomplete(obj);
                });
            };
            geocoder.on = function (event, callback) {
                geocoder.oncomplete = callback;
            };
            return geocoder;
        };

        /**
         * 驾车-路线规划
         * @param option
         */
        this.mapObj.truckDriving = function (option) {
            var policy;
            var completeCallback;
            switch (option.policy) {
                case 8:// 高速优先
                    policy = BMAP_DRIVING_POLICY_FIRST_HIGHWAYS;
                    break;
                case 1:// 避免拥堵
                    policy = BMAP_DRIVING_POLICY_AVOID_CONGESTION;
                    break;
                default:// 避开高速
                    policy = BMAP_DRIVING_POLICY_AVOID_HIGHWAYS;
            }
            var _this = this;
            var drivingRoute = new BMapGL.DrivingRoute(option.map, {
                policy: policy,	//驾车策略
                renderOptions: {
                    // panel: "bestCondition",
                    map: _this,
                    autoViewport: true
                },
                onSearchComplete: function (result) {
                    if (typeof completeCallback === 'function') {
                        result.routes = result._plans;
                        result.routes[0].distance = result.routes[0]._distance;
                        result.routes[0].time = result.routes[0]._duration;
                        result.routes[0].steps = [];
                        completeCallback('complete', result);
                    }
                }	//检索完成后的回调函数
            });
            drivingRoute.oldsearch = drivingRoute.search;
            drivingRoute.clear = drivingRoute.clearResults;
            drivingRoute.search = function (path, callback) {
                if (!path) return;
                completeCallback = callback;
                // this.setSearchCompleteCallback(callback);
                if (path.lnglat) {
                    this.oldsearch(new BMapGL.Point(path.lnglat[0][0], path.lnglat[0][1]), new BMapGL.Point(path.lnglat[1][0], path.lnglat[1][1]));
                } else if (path[0]) {
                    this.oldsearch(new BMapGL.Point(path[0].lnglat[0], path[0].lnglat[1]), new BMapGL.Point(path[1].lnglat[0], path[1].lnglat[1]));
                }
            };
            return drivingRoute;
        };

        /**
         * 地图右键菜单
         * @param option
         */
        this.mapObj.contextMenu = function (option) {
            var map = this;
            if (map.rightMenu) {
                map.removeContextMenu(map.rightMenu);
            }
            var menu = new BMapGL.ContextMenu();
            var listenerList = {};
            var event = new $.fn.mapEngine.createEvent(listenerList);

            event.addItem = function (item, callback) {
                var menuItem = new BMapGL.MenuItem(item, callback, {width: 120});
                menu.addItem(menuItem);
                menu.addSeparator();
            };
            event.addContextMenu = function () {
                map.addContextMenu(menu);
            };
            event.open = function () {
                map.addContextMenu(menu);
            };
            event.close = function () {
                menu.hide();
                map.removeContextMenu(menu);
            };
            map.rightMenu = menu;
            return event;
        };

        /**
         * 编辑线
         * @param map
         * @param option
         * @returns {Object | Object}
         */
        this.mapObj.polylineEditor = function (map, polyline) {
            polyline.enableEditing();
            polyline.open = polyline.enableEditing;
            polyline.close = polyline.disableEditing;
            return polyline;
        };

        /**
         * 编辑多边形
         * @param map
         * @param option
         * @returns {Object}
         */
        this.mapObj.polygonEditor = function (map, polygon) {
            polygon.enableEditing();
            polygon.open = polygon.enableEditing;
            polygon.close = polygon.disableEditing;
            return polygon;
        };

        /**
         * 获取百度地图经纬度path
         * */
        this.mapObj.getBaiPath = function (path) {
            var newPath = [];
            for (var i = 0; i < path.length; i++) {
                var newPoint = this.aMapTransBMap(path[i][0] || path[i].lng, path[i][1] || path[i].lat);
                newPath.push(new BMapGL.Point(newPoint[0], newPoint[1]));
            }
            return newPath;
        };

        /**
         * 画线
         * @param option
         */
        this.mapObj.polyline = function (option) {
            var thisMap = this;
            var newPath = this.getBaiPath(option.path);
            var line = new BMapGL.Polyline(newPath, {
                strokeColor: option.strokeColor,
                strokeWeight: option.strokeWeight,
                strokeOpacity: option.strokeOpacity,
                strokeStyle: option.strokeStyle,
            });
            this.addOverlay(line);
            line.setMap = function (map) {
                if (map) {
                    map.addOverlay(this);
                } else {
                    thisMap.removeOverlay(this);
                }
            };
            return line;
        };

        /**
         * 显示覆盖物至地图可视范围
         * */
        this.mapObj.setFitView = function (option) {
            if (option.getPath) {
                this.setViewport(option.getPath());
            } else if (option.getPosition) {
                this.setViewport([option.getPosition()]);
            } else if (option[0]) {
                this.setViewport(option[0].getPath());
            } else {
                this.setViewport(option);
            }
        };

        /**
         * 判断点是否在线上
         * @param clickLngLat
         * @param points1
         * @param points2
         */
        this.mapObj.isPointOnSegment = function (clickLngLat, points1, points2) {
            if (window.AMap) {
                return AMap.GeometryUtil.isPointOnSegment(clickLngLat, points1, points2);
            }
            return true;
        };

        /**
         * 画多边形
         * @param option
         */
        this.mapObj.polygon = function (option) {
            var thisMap = this;
            var newPath = this.getBaiPath(option.path[0][0][0] instanceof Array ? option.path[0] : option.path);
            var polygon = new BMapGL.Polygon(newPath, {
                strokeColor: option.strokeColor,
                strokeWeight: option.strokeWeight,
                strokeOpacity: option.strokeOpacity,
                fillColor: option.fillColor,
                fillOpacity: option.fillOpacity,
            });
            polygon.setMap = function (map) {
                if (map) {
                    map.addOverlay(this);
                } else {
                    thisMap.removeOverlay(this);
                }
            };
            thisMap.addOverlay(polygon);
            return polygon;
        };

        /**
         * 画圆
         * @param option
         */
        this.mapObj.circle = function (option) {
            var thisMap = this;
            var newPoint = this.aMapTransBMap(option.center.lng, option.center.lat);
            var circle = new BMapGL.Circle(new BMapGL.Point(newPoint[0], newPoint[1]), option.radius, {
                strokeColor: option.strokeColor,
                strokeWeight: option.strokeWeight,
                strokeOpacity: option.strokeOpacity,
                fillColor: option.fillColor,
                fillOpacity: option.fillOpacity
            });
            circle.setMap = function (map) {
                if (map) {
                    map.addOverlay(this);
                } else {
                    thisMap.removeOverlay(this);
                }
            };
            //向地图上添加圆
            thisMap.addOverlay(circle);
            return circle;
        };

        /**
         * 可拖拽驾车路线
         * @param map
         * @param array
         */
        this.mapObj.dragRoute = function (map, array) {
            if (window.AMap) {
                return new AMap.DragRoute(map, array, AMap.DrivingPolicy.REAL_TRAFFIC);
            }
            var listenerList = {
                complete: function () {
                }
            };
            var event = new $.fn.mapEngine.createEvent(listenerList);
            event.search = function () {};
            event.destroy = function () {};
            event.getRoute = function () {};
            return event;
        };

        /**
         * 经纬度转像素坐标
         * @param option
         * @returns {{getX: (function(): number), getY: (function(): number), x: number, y: number}}
         */
        this.mapObj.lngLatToContainer = function (point) {
            if (point === 0 || (!point[0] && !point.lng)) return {
                x: 0,
                y: 0,
                getX: function () {
                    return 0;
                },
                getY: function () {
                    return 0;
                },
            };
            if (point.wgslon) {
                point = [point.wgslon, point.wgslat];
            }
            var p = point;
            if (Array.isArray(point)) {
                var newPoint = this.aMapTransBMap(point[0], point[1]);
                p = {lng: newPoint[0], lat: newPoint[1]};
            }
            var pixel = this.pointToPixel(p);
            return {
                x: pixel.x,
                y: pixel.y,
                getX: function () {
                    return pixel.x;
                },
                getY: function () {
                    return pixel.y;
                },
            }
        };

        /**
         * 地图功能-添加覆盖物图层
         */
        this.mapObj.add = function (layer) {};

        /**
         * 经纬度转像素
         * @param option
         * @returns {number}
         */
        this.mapObj.containerToLngLat = function (option) {
            return 0;
            // return this.mercatorToLnglat(option);
        };

        /**
         * 删除地图覆盖物
         * @param layer
         */
        this.mapObj.remove = function (layer) {
            console.log(layer, 'layer');
            if (layer && Array.isArray(layer)) {// MapContainer
                for (var i = 0; i < layer.length; i += 1) {
                    if (layer[i]) {
                        if (layer[i].hide) {
                            layer[i].hide();
                        }
                        if (layer[i].content.substring(1,2) === 'p') {
                            var mapLabels = $('#MapContainer').find('label');
                            for (var j = 0; j < mapLabels.length; j++) {
                                var labelText = mapLabels[j].innerText.substring(mapLabels[j].innerText.length - 5,6);;
                                var oldLabelText = layer[i].content.split("");
                                var newLabelText = "";
                                for(let n = 0; n < 6; n++) { newLabelText+=oldLabelText[oldLabelText.length - 17 + i] }
                                if (labelText === newLabelText) {
                                    mapLabels[j].hide();
                                }
                            }
                            console.log(mapLabels, 'mapLabels');
                        }
                        if (layer[i]._intervalFlag) {
                            clearInterval(layer[i]._intervalFlag);
                        }
                        this.removeOverlay(layer[i]);
                        if (layer[i].markerLabel) {
                            this.removeOverlay(layer[i].markerLabel);
                        }
                        if (layer[i].extData) {
                            markerViewingArea.remove(layer[i].extData);
                        }
                    }
                }
            } else if (layer) {
                layer.hide();
                this.removeOverlay(layer);
                if (layer.markerLabel) {
                    this.removeOverlay(layer.markerLabel);
                }
                if (layer.extData) {
                    markerViewingArea.remove(layer[i].extData);
                }
            }
        };

        this.mapObj.setZoomAndCenter = function (zoom, point) {
            if (map.getMapType() === "B_EARTH_MAP" && zoom > 17) zoom = 17;
            if (point.lng) {
                this.centerAndZoom(point, zoom === 4 ? 5 : zoom);
            } else {
                var newPoint = this.aMapTransBMap(point[0], point[1]);
                this.centerAndZoom(new BMapGL.Point(newPoint[0], newPoint[1]), zoom === 4 ? 5 : zoom);
            }
            if (amapOperation) {
                amapOperation.markerStateListening();
            }
            if ($('#threeDimensionalMap').is(':checked')) {// 3D地图
                this.setHeading(64.5);
                this.setTilt(73);
            }
        };
        this.mapObj.oldSetCenter = this.mapObj.setCenter;
        this.mapObj.setCenter = function (point) {
            if (point.lng) {
                this.oldSetCenter(point);
            } else {
                var newPoint = this.aMapTransBMap(point[0], point[1]);
                this.oldSetCenter(new BMapGL.Point(newPoint[0], newPoint[1]));
            }
        };
        return this.mapObj;
    };
    $.extend(true, $.fn.mapEngine.engine, {bmap: _bmap});
}(jQuery));