;(function ($) {
    var AMapHot = function AMapHot($this, options) {
        var defaults = {
            mapViewId: null,
            zoom: 5,
            center: [126.969383, 47.460428],
            districtArea: '黑龙江',
            mapInit: null,
            mapZoomChange: null
        };
        this.options = $.extend(defaults, options);
        this.state = {
            mapView: null,
            adcodeProvinceObj: [],
            // 省级adcode
            adcodeCityObj: [],
            // 地级市adcode
            adcodeDistrictObj: [],
            // 区县adcode
            adcodeAll: [],
            // 所有的adcode
            district: null,
            average: null,
            defaultColor: 0.6 // 0xff6600,

        };
        this.$this = $this;

        this._initMap();

        this._initDistrict();
    };
    // 返回地级市adcode
    AMapHot.prototype.returnCityAdcode = function () {
        var state = this.state;
        return state.adcodeCityObj.map(function (key) {
            return key.adcode;
        });
    };
    // 判断可视区域范围内的区县adcode
    AMapHot.prototype.returnDistrictAdcode = function () {
        var state = this.state;
        var southwest = state.mapView.getBounds().getSouthWest(); // 获取西南角坐标

        var northeast = state.mapView.getBounds().getNorthEast(); // 获取东北角坐标

        var path = new AMap.Bounds(southwest, northeast);
        var arr = state.adcodeDistrictObj.map(function (key) {
            var lnglat = [key.lng, key.lat];

            if (path.contains(lnglat)) {
                return key.adcode;
            }

            return null;
        });
        return arr.filter(this._isAdcode);
    };
    // 行政区域查询
    AMapHot.prototype.districtSearch = function (data, value) {
        var state = this.state;
        var $this = this; // 清空地图覆盖物

        state.mapView.clearMap();

        this._average(data);

        for (var i = 0; i < data.length; i++) {
            var info = data[i];
            state.district.setLevel(value); //行政区级别

            state.district.setExtensions('all'); //按照adcode进行查询可以保证数据返回的唯一性

            state.district.search(info.adcode.toString(), function (info) {
                return function (status, result) {
                    if (status === 'complete') {
                        $this._fileArea(result.districtList[0], info.num);
                    }
                };
            }(info));
        }
    };
    // 根据地级市adcode查询出对应的区县的adcode
    AMapHot.prototype.searchDistrictAdcode = function (adcode) {
        var state = this.state;
        var adcodeStr = adcode.toString();
        var arr;

        for (var i = 0; i < state.adcodeAll.length; i++) {
            var obj = state.adcodeAll[i];

            if (obj.adcode === adcodeStr) {
                arr = obj.arr;
                break;
            }
        }

        console.log('arr',arr)

        return arr;
    };
    // 设置地图层级
    AMapHot.prototype.setMapZoom = function (zoomIndex) {
        var state = this.state;
        state.mapView.setZoom(zoomIndex);
    };
    // 初始化地图
    AMapHot.prototype._initMap = function () {
        var state = this.state;
        var options = this.options;
        var $this = this.$this;
        var mapViewId = $this[0].id;

        if (mapViewId !== undefined) {
            state.mapView = new AMap.Map(mapViewId, {
                resizeEnable: true,
                center: options.center,
                zoom: options.zoom
            }); // 地图层级改变后触发事件

            state.mapView.on('zoomchange', function () {
                if (options.mapZoomChange) {
                    options.mapZoomChange(state.mapView.getZoom());
                }
            });
        }
    };
    // 获取指定区域一系列的adcode
    AMapHot.prototype._initDistrict = function () {
        var options = this.options;
        var state = this.state; //行政区划查询

        var opts = {
            subdistrict: 2,
            // 返回下一级行政区
            showbiz: false // 最后一级返回街道信息

        };
        state.district = new AMap.DistrictSearch(opts); //注意：需要使用插件同步下发功能才能这样直接使用

        state.district.search(options.districtArea, function (status, result) {
            if (status == 'complete') {
                var optionsFirst = result.districtList;

                if (optionsFirst !== undefined) {
                    for (var i = 0; i < optionsFirst.length; i++) {
                        var firstInfo = optionsFirst[i];
                        state.adcodeProvinceObj.push({
                            adcode: firstInfo.adcode,
                            level: firstInfo.level,
                            lng: firstInfo.center.lng,
                            lat: firstInfo.center.lat,
                            name: firstInfo.name
                        });
                        var optionsSecond = firstInfo.districtList;

                        if (optionsSecond !== undefined) {
                            for (var j = 0; j < optionsSecond.length; j++) {
                                var secondInfo = optionsSecond[j];
                                state.adcodeCityObj.push({
                                    adcode: secondInfo.adcode,
                                    level: secondInfo.level,
                                    lng: secondInfo.center.lng,
                                    lat: secondInfo.center.lat,
                                    name: secondInfo.name
                                });
                                var optionsThird = secondInfo.districtList;

                                if (optionsSecond !== undefined) {
                                    var adcodeArr = [];

                                    for (var s = 0; s < optionsThird.length; s++) {
                                        var thirdInfo = optionsThird[s];
                                        state.adcodeDistrictObj.push({
                                            adcode: thirdInfo.adcode,
                                            level: thirdInfo.level,
                                            lng: thirdInfo.center.lng,
                                            lat: thirdInfo.center.lat,
                                            name: thirdInfo.name
                                        });
                                        adcodeArr.push(thirdInfo.adcode);
                                    }

                                    state.adcodeAll.push({
                                        adcode: secondInfo.adcode,
                                        arr: adcodeArr
                                    });
                                }
                            }
                        }
                    }
                }


                if (options.mapInit) options.mapInit(); // console.log('state.adcodeProvinceObj', state.adcodeProvinceObj);
                // console.log('state.adcodeCityObj', state.adcodeCityObj);
                // console.log('state.adcodeDistrictObj', state.adcodeDistrictObj);
            }
        });
    };

    AMapHot.prototype._isAdcode = function (value) {
        return value !== null;
    };
    // 计算传入数量的平均值
    AMapHot.prototype._average = function (data) {
        var state = this.state;
        var arr = [];

        for (var i = 0; i < data.length; i++) {
            var info = data[i];
            arr.push(info.num);
        }

        state.average = Math.max.apply(null, arr);
    };
    // 计算颜色值
    AMapHot.prototype._colorValue = function (num) {
        var state = this.state;
        var times = num / state.average;
        var rand = times * state.defaultColor + 0.2; // var rand = Math.floor(times * 0x0000ff  + state.defaultColor).toString(16);

        return 'rgba(255, 102, 0, ' + rand + ')';
    };
    // 区域画图和填充颜色
    AMapHot.prototype._fileArea = function (data, num) {
        var state = this.state;

        var colorValue = this._colorValue(num);

        var bounds = data.boundaries;

        if (bounds) {
            for (var i = 0, l = bounds.length; i < l; i++) {
                new AMap.Polygon({
                    map: state.mapView,
                    strokeWeight: 1,
                    strokeColor: colorValue,
                    fillColor: colorValue,
                    fillOpacity: 0.6,
                    path: bounds[i]
                });
            } // state.mapView.setFitView();//地图自适应

        }
    };

    $.fn.amapHot = function (options) {
        return new AMapHot(this, options);
    };
})(jQuery);