;(function ($) {
    var mapConst = null;
    window.mapKeys = null;// 地图key值
    var protocol = document.location.protocol;// 获取当前协议类型
    var baiduTimer = null;

    var _loadMapJSFile = [
        'webapi.amap.com',
        'api.map.baidu.com',
        'mapopen.cdn.bcebos.com',
        'a.qqearth.com',
        'api.tianditu.gov.cn',
        'maps.googleapis.com',
    ];

    var _loadMapCSSFile = [
        'cache.amap.com',
        'api.tianditu.gov.cn',
        'mapopen.cdn.bcebos.com',
        'api.map.baidu.com',
    ];

    $.fn.mapEngine = {
        /**
         * 加载地图引擎
         * AMap 高德地图
         * NglpMap 四维地图
         * @constructor
         */
        loadMap: function (mapEngine, containerId) {
            var $this = this;
            if (!mapConst) {// 获取地图key值
                json_ajax("GET", '/clbs/mapKeys', "json", false, null, function (msg) {
                    mapKeys = msg;
                    mapConst = {
                        AMap: 'https://webapi.amap.com/maps?v=2.0&key=' + mapKeys.gaode + '&plugin=AMap.DistrictSearch,AMap.MouseTool,AMap.AutoComplete,AMap.Geocoder,AMap.PlaceSearch,AMap.PolygonEditor,AMap.PolylineEditor,AMap.CircleEditor,AMap.HawkEye,AMap.RectangleEditor,AMap.MoveAnimation,AMap.MarkerCluster,AMap.TruckDriving,AMap.Driving,AMap.DragRoute,AMap.ToolBar,AMap.Scale,AMap.ControlBar,AMap.Weather,AMap.ElasticMarker',
                        NglpMap: 'http://a.qqearth.com:81/SE_JSAPI?uid=' + mapKeys.siwei,
                        TMap: 'https://api.tianditu.gov.cn/api?v=4.0&tk=' + mapKeys.tian,
                        BMap: 'https://api.map.baidu.com/api?v=2.0&type=webgl&ak=' + mapKeys.baidu + '&callback=mapInitCallBack',
                        GMap: 'https://maps.googleapis.com/maps/api/js?key=' + mapKeys.google + '&libraries=drawing',
                    };
                });
            }
            switch (mapEngine) {
                // 高德地图
                case 'AMap':
                    if (!mapKeys.gaode) {
                        layer.msg('未找到地图key，请联系管理员');
                    }
                    return new Promise((resolve, reject) => {
                        $this._loadScript(mapConst.AMap, mapEngine, function () {
                            $this._removeMapArea(containerId);
                            var newmap = new $this.engine.amap(containerId);
                            map = null;
                            resolve(newmap);
                        });
                    });
                // 四维地图
                case 'NglpMap':
                    if (!mapKeys.siwei) {
                        layer.msg('未找到地图key，请联系管理员');
                    }
                    return new Promise((resolve, reject) => {
                        $this._loadScript(mapConst.NglpMap, mapEngine, function () {
                            $this._removeMapArea(containerId);
                            var newmap = new $this.engine.nglpMap(containerId);
                            map = null;
                            resolve(newmap);
                        });
                    });
                case 'TMap':
                    if (!mapKeys.tian) {
                        layer.msg('未找到地图key，请联系管理员');
                    }
                    return new Promise((resolve, reject) => {
                        $this._loadScript(mapConst.TMap, mapEngine, function () {
                            $this._removeMapArea(containerId);
                            var newmap = new $this.engine.tmap(containerId);
                            map = null;
                            resolve(newmap);
                        });
                    });
                case 'BMap':
                    if (!mapKeys.baidu) {
                        layer.msg('未找到地图key，请联系管理员');
                    }
                    return new Promise((resolve, reject) => {
                        window.BMapGL_loadScriptTime = (new Date).getTime();
                        $this._loadScript(mapConst.BMap, mapEngine, function () {
                            $this._removeMapArea(containerId);
                            var newmap = new $this.engine.bmap(containerId);
                            map = null;
                            resolve(newmap);
                        });
                    });
                case 'GMap':
                    if (!mapKeys.google) {
                        layer.msg('未找到地图key，请联系管理员');
                    }
                    return new Promise((resolve, reject) => {
                        $this._loadScript(mapConst.GMap, mapEngine, function () {
                            $this._removeMapArea(containerId);
                            var newmap = new $this.engine.gmap(containerId);
                            map = null;
                            resolve(newmap);
                        });
                    });
                default:
                    break;
            }
        },
        /**
         *  动态添加script
         */
        _loadScript: function (url, type, callback) {
            if (baiduTimer) {
                clearTimeout(baiduTimer);
                baiduTimer = null;
            }
            var $this = this;
            this._removeMapCSSResources();
            this._removeMapJSResources();
            var script = document.createElement('script');
            script.type = 'text/javascript';
            if (type === 'BMap') {
                window.mapInitCallBack = function () {
                    window.BMap = window.BMapGL;
                    // 图形绘制功能
                    // _this.addLabel('css', '' + protocol + '//mapopen.cdn.bcebos.com/github/BMapGLLib/DrawingManager/src/DrawingManager.min.css');
                    $this.addLabel('script', '' + protocol + '//mapopen.cdn.bcebos.com/github/BMapGLLib/DrawingManager/src/DrawingManager.min.js');
                    /*// 点标记聚合功能
                    _this.addLabel('script', '' + protocol + '//api.map.baidu.com/library/TextIconOverlay/1.2/src/TextIconOverlay_min.js');
                    _this.addLabel('script', '' + protocol + '//api.map.baidu.com/library/MarkerClusterer/1.2/src/MarkerClusterer_min.js');
                    */
                    // 测距功能
                    $this.addLabel('script', '' + protocol + '//mapopen.cdn.bcebos.com/github/BMapGLLib/DistanceTool/src/DistanceTool.min.js');
                    // 鼠标拉框放大功能
                    $this.addLabel('script', '' + protocol + '//api.map.baidu.com/library/RectangleZoom/1.2/src/RectangleZoom_min.js');
                    baiduTimer = setTimeout(() => {
                        callback();
                    }, 1000)
                }
            } else {
                if (script.readyState) {
                    // IE浏览器
                    script.onreadystatechange = function () {
                        if (script.readyState == 'loaded' || script.readyState == 'complete') {
                            script.onreadystatechange = null;
                            callback();
                        }
                    };
                } else {
                    //标准浏览器
                    script.onload = function () {
                        callback();
                    };
                }
            }
            script.src = url;
            document.getElementsByTagName('head')[0].appendChild(script);
        },
        /**
         * 页面添加依赖标签
         * */
        addLabel: function (type, url, status) {
            var label;
            if (type === 'script') {
                label = document.createElement('script');
                label.type = 'text/javascript';
                if (!status) {
                    label.className = 'baidumap';
                }
                label.src = url;
            } else {
                label = document.createElement('link');
                label.rel = 'stylesheet';
                if (!status) {
                    label.className = 'baidumap';
                }
                label.href = url;
            }
            document.getElementsByTagName('head')[0].appendChild(label);
        },
        /**
         * 清空地图容器，避免重复渲染
         */
        _removeMapArea: function (containerId) {
            var MapEngineContant = document.getElementById(containerId);
            var childs = MapEngineContant.childNodes;
            for (var i = childs.length - 1; i >= 0; i--) {
                MapEngineContant.removeChild(childs[i]);
            }
            ;
        },
        /**
         * 判断js文件是否已经加载过
         */
        _isReload: function (path) {
            var script = $("script[src='" + path + "']");
            return script.length > 0;
        },
        /**
         * 加载地图js前，先请空之前加载的其他地图css
         */
        _removeMapCSSResources: function () {
            var links = $('link');
            for (var i = links.length - 1; i >= 0; i -= 1) {
                var href = links[i].href;
                var type = $(links[i]).attr('data-type');
                if (href && !type) {
                    for (var j = 0; j < _loadMapCSSFile.length; j += 1) {
                        if (href.indexOf(_loadMapCSSFile[j]) !== -1) {
                            links[i].remove();
                            break;
                        }
                    }
                }
            }
            ;
        },
        /**
         * 加载地图js前，先清空之前的加载的其他地图js
         */
        _removeMapJSResources: function () {
            var scripts = $('script');
            for (var i = scripts.length - 1; i >= 0; i -= 1) {
                var src = scripts[i].src;
                var type = $(scripts[i]).attr('data-type');
                if (src && !type) {
                    for (var j = 0; j < _loadMapJSFile.length; j += 1) {
                        if (src.indexOf(_loadMapJSFile[j]) !== -1) {
                            scripts[i].remove();
                            break;
                        }
                    }
                }
            }
            ;
        },
        engine: {
            amap: null,
            nglpMap: null,
            tmap: null,
            bmap: null,
            gmap: null,
        },
        /**
         * 自定义事件
         * 用于统一不同地图api的事件名称
         * @param {*} listenerList
         * @returns
         */
        createEvent: function (listenerList) {
            var ev = {
                dispatch: function (eventKey) {
                    var args = Array.prototype.slice.call(arguments, 1);
                    listenerList[eventKey].apply(this, args);
                    return this;
                },
                listener: function (eventKey, callback) {
                    if (typeof eventKey === "string" && typeof callback === "function") {
                        listenerList[eventKey] = callback;
                    }
                    return this;
                }
            }
            var event = new Object();
            event.on = ev.listener;
            event.trigger = ev.dispatch;
            return event;
        }
    };
}(jQuery))