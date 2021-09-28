var lbOrgList;
(function (window, $) {
    var $realTimeRC = $("#realTimeRC"),
        googleMapLayer,//google地图
        realTimeTraffic,//实时路况
        satellLayer,// 卫星地图
        buildings,
        isWeatherShow = false,
        map;
    var isCluster = false; // 是否集合
    var cluster;//点集合
    var vinfoWindwosClickVid, infoWindow;
    var markerClickLngLat = null; // 点击监控对象图标后，获取经纬度
    var paths = null;
    var markerFocus;
    var icoUpFlag;
    var markerViewingArea;
    var isCarNameShow = true;
    var pathsTwo = null;
    var markerOutside;
    var markerAllUpdateData;
    var carNameMarkerContentMap;
    var markerListMap; // 地图所有点集合
    var websocketUpdateMap;//每次websocket推送的点信息
    var websocketUpdateobj;

    //树结构
    var lbOrgZTree;
    var initTreeName = '';//根节点名称
    var groupId = '';
    var groupInput = $('#groupSelect');
    var isAllExpand = true;//企业全展开
    var isTreeInit = true;

    var hasSubscribeVicArr = [];

    //图表
    var timer = null, timer2 = null;
    var riskWarnChart,
        riskLevelChart,
        riskDealChart,
        riskEventChart,
        riskTrendChart,
        riskDistributedChart;

    var eventTrend = [],
        customerServiceTrend = [],
        customerServiceRate = [],
        vehOnlineTrend = [],
        vehOnlineTrendRate = [];

    var getRiskProportionData=[],
        getRisksDealInfoData=[];

    var riskcluster = [],//极速风险
        riskcrash = [],//碰撞危险
        riskdistraction = [],//注意力分散
        riskexception = [],//违规异常
        risktotal = [],//总数
        riskintenseDriving = [],//激烈驾驶
        risktired = [];//疑似疲劳

    var chartDate,
        isToday = true;//图表日期

    var riskWarnNum = 0,
        riskLevelNum = 6,
        riskDealNum = 0;

    var dayHour = ['00:00', '01:00', '02:00', '03:00', '04:00', '05:00', '06:00', '07:00', '08:00', '09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00', '20:00', '21:00', '22:00', '23:00'],
        H = ['00', '01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23'];
    var mids = [];

    lbOrgList = {
        // 初始化地图
        initMap: function () {
            // 创建地图
            map = new AMap.Map('mapContainer', {
                zoom: 5,//级别
                center: [116.397428, 39.90923],//中心点坐标
                resizeEnable: true,
            });

            // 实例化3D楼块图层
            buildings = new AMap.Buildings();
            // 在map中添加3D楼块图层
            buildings.setMap(map);

            // 地图标尺
            map.plugin(["AMap.ToolBar", "AMap.Scale"], function () {
                //加载工具条
                var tool = new AMap.ToolBar({
                    offset: new AMap.Pixel(0, 200)
                });
                map.addControl(tool);
                map.addControl(new AMap.Scale());
            });

            // 地图移动结束后触发，包括平移和缩放
            mouseTool = new AMap.MouseTool(map);
            // mouseTool.on("draw", lbOrgList.createSuccess);

            // 实时路况
            realTimeTraffic = new AMap.TileLayer.Traffic({zIndex: 10});
            realTimeTraffic.setMap(map);
            realTimeTraffic.hide();

            // 卫星地图
            satellLayer = new AMap.TileLayer.Satellite();
            satellLayer.setMap(map);
            satellLayer.hide();

            infoWindow = new AMap.InfoWindow({offset: new AMap.Pixel(0, -10), closeWhenClickMap: true});

            map.on("click", function () {
                $("#rMenu").css("visibility", "hidden");
                $("#disSetMenu").slideUp();
                $("#mapDropSettingMenu").slideUp();
                $("#fenceTool>.dropdown-menu").hide();
            });

            // 当范围缩小时触发该方法
            map.on('zoomend', lbOrgList.markerStateListening);
            // lbOrgList.websocketRequest();
        },
        websocketRequest: function (mids) {
            setTimeout(function () {
                if (webSocket.conFlag) {
                    lbOrgList.markerSocket(mids);//上下线实时更新
                    setTimeout(function () {
                        lbOrgList.markerStateSocket(mids);//状态改变实时更新
                    }, 1000)
                } else {
                    lbOrgList.websocketRequest(mids);
                }
            }, 1500);
        },
        /**
         * 初始化图标数据
         */
        initData: function () {
            vehOnlineTrend = [];
            vehOnlineTrendRate = [];
            eventTrend = [];
            customerServiceTrend = [];
            customerServiceRate = [];
            riskcluster = [];
            riskcrash = [];
            riskdistraction = [];
            riskexception = [];
            risktotal = [];
            risktired = [];
            riskintenseDriving = [];

            var date = new Date();
            var len = isToday ? date.getHours() : 24;
            for (var i = 0; i <= len; i++) {
                vehOnlineTrend.push(0);
                vehOnlineTrendRate.push(0);
                eventTrend.push(0);
                customerServiceTrend.push(0);
                customerServiceRate.push(0);
                riskcluster.push(0);
                riskcrash.push(0);
                riskdistraction.push(0);
                riskexception.push(0);
                risktotal.push(0);
                risktired.push(0);
                riskintenseDriving.push(0);
            }
        },
        markerSocket: function (mids) {
            // var param = [];
            // var vehicleIds = eval ( $("#vehicleIds").val() );
            // $(vehicleIds).each(function(i,id){
            //     param.push({'vehicleID':id});
            // })
            var requestStrS = {
                "desc": {
                    "MsgId": 40964,
                    "UserName": $("#userName").text()
                },
                "data": mids
            };
            webSocket.subscribe(headers, "/user/topic/location", function (data) {
                if (data.body) {
                    var info = JSON.parse(data.body);
                    // lbOrgList.getRealTimeData(info);
                    lbOrgList.updateWebsocketMap(info);
                }
            }, "/app/location/subscribe", requestStrS);
        },
        /**
         * 每次推送点信息更新 markerAllUpdateData
         */
        updateWebsocketMap: function (info) {

            if (markerAllUpdateData.containsKey(info.vehicleId)) {
                var info2 = markerAllUpdateData.get(info.vehicleId);
                var markerlatitude = info2[0][2];
                var markerlongitude = info2[0][3];

                var x = info.longitude;
                var y = info.latitude;

                if (markerlatitude != x || markerlongitude != y) {
                    websocketUpdateMap.put(info.vehicleId, info); //存在车并且位置变化才put
                }

            } else {
                websocketUpdateMap.put(info.vehicleId, info); //不存在车直接put
            }

            websocketUpdateobj = info;
        },
        /**
         * 每30秒更新地图
         */
        intervalUpdateMarker: function () {
            var setInterval = function () {
                setTimeout(function () {
                    if (websocketUpdateobj) {
                        // var info = websocketUpdateobj;
                        // lbOrgList.getRealTimeData(info);

                        // 判断地图层级
                        if (map.getZoom() >= 16) { //非聚合跳点
                            var values = websocketUpdateMap.values();
                            for (var i = 0; i < values.length; i++) {
                                lbOrgList.getRealTimeData(values[i])
                            }
                        } else {
                            var info = websocketUpdateobj;
                            lbOrgList.getRealTimeData(info);
                        }
                        websocketUpdateMap.clear()
                    }
                    setInterval();
                }, 3000)
            }
            setInterval();
        },
        /**
         * 获取实时监控对象(状态改变)
         */
        markerStateSocket: function (mids) {
            // var param = [];
            // var vehicleIds = eval ("(" + $("#vehicleIds").val()+ ")");
            // $(vehicleIds).each(function(i,id){
            //     param.push({'vehicleID':id});
            // });

            var requestStrS = {
                "desc": {
                    "MsgId": 40964,
                    "UserName": $("#userName").text()
                },
                "data": mids
            };

            webSocket.subscribe(headers, '/user/topic/cachestatus', function (data) {
                data = JSON.parse(data.body).data;
                lbOrgList.UpdateMarkerState(data);
            }, "/app/vehicle/subVehicleStatusNew", requestStrS);
        },
        /**
         * 监控对象状态更新
         */
        UpdateMarkerState: function (data) {

            data.forEach(function (item) {
                var id = item.vehicleId;

                //所有监控对象
                if (markerAllUpdateData.containsKey(id) && map.getZoom() >= 16) {
                    var info = markerAllUpdateData.get(id);
                    // info[5] = item.vehicleStatus;

                    //可视区域内
                    if (markerViewingArea.containsKey(id)) {
                        var info2 = markerViewingArea.get(id);
                        info2[6] = item.vehicleStatus;

                        // var carNum = info2[1],
                        var carNum = info2[0].name,
                            // markerLngLat = info2[1],
                            // markerLngLat = [info2[2], info2[3]], // 经纬度
                            markerLngLat = [info2[1][0][0], info2[1][0][1]], // 经纬度
                            // icon = info2[6],
                            icon = info2[7],
                            carState = item.vehicleStatus;

                        // var marker = info2[0];
                        // marker.carState = item.vehicleStatus;

                        lbOrgList.carNameEvade(id, carNum, markerLngLat, null, '0', icon, false, carState);
                    }
                }
            })
        },
        // 工具条点击
        toolClick: function () {
            var $toolOperateClick = $("#toolOperateClick");

            if ($toolOperateClick.css("margin-right") == "-600px") {
                $toolOperateClick.animate({marginRight: "7px"});
                $('.tool-box').css('width', '570px');
            } else {
                isFlag = true;
                $("#disSetMenu,#mapDropSettingMenu").hide();
                $('#measurementMenu').hide();
                $toolOperateClick.animate({marginRight: "-600px"},function(){
                    $('.tool-box').css('width', '0px');
                });
                $("#toolOperateClick i").removeClass('active');
                $("#toolOperateClick span").css('color', '#5c5e62');
                mouseTool.close(true);
            }
            ;
        },
        // 工具操作
        toolClickList: function () {
            var id = $(this).attr('id');
            var i = $("#" + id).children('i');

            //显示设置
            if (id == 'displayClick') {
                if (!($("#mapDropSettingMenu").is(":hidden"))) {
                    $("#mapDropSettingMenu").slideUp();
                    $("#disSetMenu").slideDown();
                } else {
                    if ($("#disSetMenu").is(":hidden")) {
                        $("#disSetMenu").slideDown();
                    } else {
                        $("#disSetMenu").slideUp();
                    }
                }
            }
            //地图设置
            else if (id == "mapDropSetting") {
                if (!($("#disSetMenu").is(":hidden"))) {
                    $("#disSetMenu").slideUp();
                    $("#mapDropSettingMenu").slideDown();
                } else {
                    if ($("#mapDropSettingMenu").is(":hidden")) {
                        $("#mapDropSettingMenu").slideDown();
                    } else {
                        $("#mapDropSettingMenu").slideUp();
                    }
                }
            } else {

                if (i.hasClass("active")) {
                    i.removeClass('active');
                    $("#" + id).children('span.mapToolClick').css('color', '#5c5e62');
                    mouseTool.close(true);

                } else {
                    $("#toolOperateClick i").removeClass('active');
                    $("#toolOperateClick span.mapToolClick").css('color', '#5c5e62');
                    i.addClass('active');
                    $("#" + id).children('span.mapToolClick').css('color', '#6dcff6');
                    mouseTool.close(true);
                }
                ;
                if (i.hasClass("active")) {
                    if (id == "magnifyClick") {
                        //拉框放大
                        mouseTool.rectZoomIn();
                    } else if (id == "shrinkClick") {
                        //拉框放小
                        mouseTool.rectZoomOut();
                    } else if (id == "countClick") {
                        //距离量算
                        isDistanceCount = true;
                        mouseTool.rule();
                    } else if (id == "queryClick") {
                        //区域查车
                        isAreaSearchFlag = true;
                        mouseTool.rectangle();
                    }
                    ;
                }


            }
        },
        //地图设置
        mapDropdownSettingClickFn: function () {
            var id = $(this).attr("id");
            //路况开关
            if (id == "realTimeRC") {
                lbOrgList.realTimeRC();
            }
            //卫星地图
            else if (id == "defaultMap") {
                lbOrgList.satelliteMapSwitching();
            }
            //谷歌地图
            else if (id == "googleMap") {
                lbOrgList.showGoogleMapLayers();
            }
        },
        // 实时路况点击
        realTimeRC: function () {
            if ($realTimeRC.attr("checked")) {
                realTimeTraffic.hide();
                $realTimeRC.attr("checked", false);
                $("#realTimeRCLab").removeClass("preBlue");
            } else {
                //取消谷歌地图选中状态
                if (googleMapLayer) {
                    googleMapLayer.setMap(null);
                }
                $("#googleMap").attr("checked", false);
                $("#googleMapLab").removeClass("preBlue");
                /* if ($("#googleMap").attr("checked")) {
                     realTimeTraffic = new AMap.TileLayer.Traffic({zIndex: 100});
                     realTimeTraffic.setMap(map);
                 }*/
                realTimeTraffic.show();
                $realTimeRC.attr("checked", true);
                $("#realTimeRCLab").addClass("preBlue");
            }
        },
        //卫星地图及3D地图切换
        satelliteMapSwitching: function () {
            if ($("#defaultMap").attr("checked")) {
                satellLayer.hide();
                buildings.setMap(map);
                if (googleMapLayer) {
                    googleMapLayer.setMap(null);
                }
                $("#defaultMap").attr("checked", false);
                $("#defaultMapLab").removeClass("preBlue");
            } else {
                // 判断未切换到谷歌地图直接选择卫星地图时 未初始化问题
                if (googleMapLayer) {
                    //取消谷歌地图选中状态
                    googleMapLayer.setMap(null);
                }
                $("#googleMap").attr("checked", false);
                $("#googleMapLab").removeClass("preBlue");

                satellLayer.show();
                buildings.setMap(null);
                $("#defaultMap").attr("checked", true);
                $("#defaultMapLab").addClass("preBlue");
            }
        },
        //GOOGLE地图
        showGoogleMapLayers: function () {
            if ($("#googleMap").attr("checked")) {
                googleMapLayer.setMap(null);
                $("#googleMap").attr("checked", false);
                $("#googleMapLab").removeClass("preBlue");
            } else {
                googleMapLayer = new AMap.TileLayer({
                    tileUrl: 'http://mt{1,2,3,0}.google.cn/vt/lyrs=m@142&hl=zh-CN&gl=cn&x=[x]&y=[y]&z=[z]&s=Galil', // 图块取图地址
                    zIndex: 100 //设置Google层级与高德相同  避免高德路况及卫星被Google图层覆盖
                });
                googleMapLayer.setMap(map);
                $("#googleMap").attr("checked", true);
                $("#googleMapLab").addClass("preBlue");

                //取消路况与卫星选中状态
                $realTimeRC.attr("checked", false);
                $("#realTimeRCLab").removeClass("preBlue");
                realTimeTraffic.hide();
                $("#defaultMap").attr("checked", false);
                $("#defaultMapLab").removeClass("preBlue");
                satellLayer.hide();
                buildings.setMap(map);
            }
        },
        /**
         * 获取地图打点信息
         */
        getVehiclesInfo: function () {
            json_ajax('post', '/clbs/adas/lb/guide/getVehiclePositional', 'json', true, {}, function (data) {
                if (data.success && data.obj.length) {
                    data.obj.forEach(function (val) {
                        // lbOrgList.getRealTimeData(val)
                        var vehicleInfo = val;
                        var vehicleId = vehicleInfo.vehicleId; //车id
                        var status = vehicleInfo.status; //车状态
                        var brand = vehicleInfo.brand; //车名称
                        var longitude = vehicleInfo.longitude; //车经度
                        var latitude = vehicleInfo.latitude; //车纬度
                        var direction = vehicleInfo.direction; //车图标角度
                        var vehicleIcon = vehicleInfo.vehicleIcon; //车图标

                        // var subscribeObjOldLength = markerAllUpdateData.values().length;

                        // 删除对应监控对象以前的数据
                        if (markerAllUpdateData.containsKey(vehicleId)) {
                            markerAllUpdateData.remove(vehicleId);
                        }

                        // 组装监控对象需要保存的信息
                        var objSaveInfo = [
                            vehicleId, // 监控对象ID
                            brand, // 监控对象名称
                            longitude, // 经度
                            latitude, // 纬度
                            direction, // 角度
                            status, // 状态
                            vehicleIcon, // 图标
                            '', // 时间
                            '', // 里程
                        ];

                        var coordinateNew = [];
                        var x = latitude;
                        var y = longitude;
                        coordinateNew.push(y);
                        coordinateNew.push(x);

                        var content = [];

                        var updateInfo = [
                            objSaveInfo,
                            content // content为信息弹窗数据
                        ];

                        markerAllUpdateData.put(vehicleId, updateInfo);

                    })
                    // 创建地图可视区域聚合点
                    lbOrgList.createMarkerClusterer();
                }
            })
        },
        /**
         * 获取实时数据
         * @param data : 最新数据
         */
        getRealTimeData: function (vehicleInfo) {

            var vehicleInfo = vehicleInfo;
            var vehicleId = vehicleInfo.vehicleId; //车id
            var status = vehicleInfo.status; //车状态
            var brand = vehicleInfo.brand; //车名称
            var longitude = vehicleInfo.longitude; //车经度
            var latitude = vehicleInfo.latitude; //车纬度
            var direction = vehicleInfo.direction; //车图标角度
            var vehicleIcon = vehicleInfo.vehicleIcon; //车图标


            var subscribeObjOldLength = markerAllUpdateData.values().length;

            // 删除对应监控对象以前的数据
            if (markerAllUpdateData.containsKey(vehicleId)) {
                markerAllUpdateData.remove(vehicleId);
            }

            // 组装监控对象需要保存的信息
            var objSaveInfo = [
                vehicleId, // 监控对象ID
                brand, // 监控对象名称
                longitude, // 经度
                latitude, // 纬度
                direction, // 角度
                status, // 状态
                vehicleIcon, // 图标
                '', // 时间
                '', // 里程
            ];

            var coordinateNew = [];
            var x = latitude;
            var y = longitude;
            coordinateNew.push(y);
            coordinateNew.push(x);

            var content = [];

            var updateInfo = [
                objSaveInfo,
                content // content为信息弹窗数据
            ];

            markerAllUpdateData.put(vehicleId, updateInfo);

            lbOrgList.updateMarkerInfo(vehicleId, coordinateNew)

            // 获取现在的订阅对象数据长度
            var subscribeObjNowLength = markerAllUpdateData.values().length;

            // 针对区域查询后，监控对象的聚合显示
            if (map.getZoom() < 16 && subscribeObjNowLength != subscribeObjOldLength) {
                lbOrgList.markerStateListening();
            }

            // var angleVehicle = Number(direction) + 270;
            // 判断是否是订阅的第一个对象
            if (markerViewingArea.size() == 0 && map.getZoom() >= 16 && markerAllUpdateData.size() == 1) {
                lbOrgList.createMarker(objSaveInfo, content, false);
            } else {
                // 判断当前位置点是否在可视区域内且层级大于11
                if (paths && (paths.contains(coordinateNew) || vehicleId == markerFocus) && map.getZoom() >= 16) {
                    if (markerViewingArea.containsKey(vehicleId)) { // 判断是否含有该id数据
                        var value = markerViewingArea.get(vehicleId);

                        var marker = value[0];
                        marker.extData = vehicleId; // 监控对象id
                        marker.stateInfo = status; // 监控对象状态
                        marker.content = content.join(""); // 监控对象信息弹窗

                        var markerLngLat = [longitude, latitude];
                        // var timeOld = (new Date(vehicle[10])).getTime();//获得时间（毫秒）
                        markerViewingArea.remove(vehicleId);
                        value[0] = marker;
                        value[1].push(markerLngLat);
                        value[2] = content;
                        value[3].push('');
                        // value[4].push(timeOld);
                        value[6] = status;
                        value[8].push(direction);
                        markerViewingArea.put(vehicleId, value);
                        // 监控对象进行跳点
                        lbOrgList.markerJumpPoint(objSaveInfo);
                    } else { // 创建监控对象图标
                        lbOrgList.createMarker(objSaveInfo, content, false);
                    }
                } else {
                    lbOrgList.saveMarkerOutsideInfo(objSaveInfo, content);
                }
            }
        },
        /**
         * 更新地图上位置信息变化的点
         */
        updateMarkerInfo: function (vehicleId, coordinateNew) {
            if (markerListMap) {
                if (markerListMap.containsKey(vehicleId)) {
                    var marker = markerListMap.get(vehicleId);
                    marker.setPosition(coordinateNew)
                }
            }
        },
        /**
         * 根据地图层级变化相应改变paths
         * @param data :
            */
        pathsChangeFun: function () {
            var mapZoom = map.getZoom();

            if (mapZoom == 18) {
                lbOrgList.LimitedSize(6);
            } else if (mapZoom == 17) {
                lbOrgList.LimitedSize(5);
            } else if (mapZoom == 16) {
                lbOrgList.LimitedSize(4);
            } else if (mapZoom == 15) {
                lbOrgList.LimitedSize(3);
            } else if (mapZoom == 14) {
                lbOrgList.LimitedSize(2);
            } else if (mapZoom <= 13 && mapZoom >= 5) {
                lbOrgList.LimitedSize(1);
            }
            ;
        },
        /**
         * 判断点有没有在地图可视区域内
         * @param
         */
        LimitedSizeTwo: function () {
            var southwest = map.getBounds().getSouthWest();
            var northeast = map.getBounds().getNorthEast();
            var mcenter = map.getCenter();                  //获取中心坐标
            var pixel2 = map.lnglatTocontainer(mcenter);//根据坐标获得中心点像素
            var mcx = pixel2.getX();                    //获取中心坐标经度像素
            var mcy = pixel2.getY();                    //获取中心坐标纬度像素
            var southwestx = mcx + (mcx * 0.8);
            var southwesty = mcy * 0.2;
            var northeastx = mcx * 0.2;
            var northeasty = mcy + (mcy * 0.8);
            var ll = map.containTolnglat(new AMap.Pixel(southwestx, southwesty));
            var lll = map.containTolnglat(new AMap.Pixel(northeastx, northeasty));
            pathsTwo = new AMap.Bounds(
                lll,//东北角坐标
                ll //西南角坐标
            );
        },
        LimitedSize: function (size) {
            paths = null;
            var southwest = map.getBounds().getSouthWest();//获取西南角坐标
            var northeast = map.getBounds().getNorthEast();//获取东北角坐标
            var possa = southwest.lat;//纬度（小）
            var possn = southwest.lng;
            var posna = northeast.lat;
            var posnn = northeast.lng;
            var psa = possa - ((posna - possa) * size);
            var psn = possn - ((posnn - possn) * size);
            var pna = posna + ((posna - possa) * size);
            var pnn = posnn + ((posnn - possn) * size);
            paths = new AMap.Bounds(
                [psn, psa], //西南角坐标
                [pnn, pna]//东北角坐标
            );
        },
        /**
         * 监控对象在地图层级改变或拖拽后状态更新
         * @param
         */
        markerStateListening: function () {
            // 根据地图层级变化相应改变paths
            lbOrgList.pathsChangeFun();
            lbOrgList.LimitedSizeTwo();

            var mapZoom = map.getZoom();
            // 判断地图层级是否大于等于11
            // 大于等于11：重新计算地图上哪些监控对象在可视区域内||区域外
            // 小于11：进行聚合
            mapWeather.showWeather(map, mapZoom, isWeatherShow);//天气
            if (mapZoom >= 16) {
                // 判断是否是刚从聚合状态切换过来
                // 如果是就把最新点集合的数据进行创建marker
                if (isCluster) {
                    if (cluster != undefined) {
                        cluster.clearMarkers();
                    }
                    isCluster = false;
                }
                lbOrgList.clusterToCreateMarker();
                var values = markerViewingArea.values(), mids = [];
                for (var i = 0, len = values.length; i < len; i++) {
                    var marker = values[i][0];
                    var mid = marker.extData;

                    if (hasSubscribeVicArr.indexOf(mid) == -1) {
                        hasSubscribeVicArr.push(mid)
                        mids.push(mid);
                    }
                }
                setTimeout(function () {
                    if (mids.length) {
                        if (webSocket.conFlag) {
                            lbOrgList.markerSocket(mids);//上下线实时更新
                            setTimeout(function () {
                                lbOrgList.markerStateSocket(mids);//状态改变实时更新
                            }, 1000)
                        } else {
                            lbOrgList.websocketRequest(mids);
                        }
                    }
                }, 1500);
            } else {
                // 刚进入聚合状态，进行清空聚焦车辆
                // if (!isCluster) {
                //     isCluster = true;
                //     lbOrgList.clearFocusObj();
                // }
                // // 清空地图上已创建监控对象图标
                // lbOrgList.clearMapForMarker();
                // // 创建地图可视区域聚合点
                // lbOrgList.createMarkerClusterer();


                if (!isCluster && isCluster != undefined) {
                    isCluster = true;
                    lbOrgList.clearFocusObj();
                    lbOrgList.clearMapForMarker();// 清空地图上已创建监控对象图标
                    lbOrgList.createMarkerClusterer();//创建聚合点
                }
                var mids = [];
                for (var i = 0; i < hasSubscribeVicArr.length; i++) {
                    mids.push({'vehicleID': hasSubscribeVicArr[i]})
                }

                if (mids.length) {
                    var cancelStrS = {
                        "desc": {
                            "MsgId": 40964,
                            "UserName": $("#userName").text()
                        },
                        "data": mids
                    };
                    webSocket.unsubscribealarm(headers, "/app/vehicle/unsubscribestatusNew", cancelStrS);

                    setTimeout(function () {
                        webSocket.unsubscribealarm(headers, "/app/location/unsubscribe", cancelStrS);
                    }, 1000)

                }

                hasSubscribeVicArr = [];
            }
        },
        /**
         * 清空聚焦车辆
         */
        clearFocusObj: function () {
            markerFocus = null;
        },
        /**
         * 聚合状态刚消失创建marker
         * @param
         */
        clusterToCreateMarker: function () {
            var values = markerAllUpdateData.values();

            for (var i = 0, len = values.length; i < len; i++) {
                var id = values[i][0][0];
                var markerLngLat = [values[i][0][2], values[i][0][3]];

                if (paths.contains(markerLngLat) || markerFocus == id) {
                    if (markerViewingArea.containsKey(id)) {
                        var marker = markerViewingArea.get(id)[0];

                        var info = values[i][0];
                        var carName = info[1];
                        var stateInfo = info[5];
                        lbOrgList.carNameEvade(id, carName, marker.getPosition(), false, '0', null, false, stateInfo);
                    } else {
                        lbOrgList.createMarker(values[i][0], values[i][1], false);
                    }
                } else {
                    lbOrgList.saveMarkerOutsideInfo(values[i][0], values[i][1]);
                }
            }
        },
        /**
         * 创建监控对象图标
         * @param
         */
        createMarker: function (info, content, isFocus) {
            var markerLngLat = [info[2], info[3]]; // 经纬度
            var angle;
            if (icoUpFlag) {
                angle = 0;
            } else {
                angle = Number(info[4]) + 270; // 角度
            }

            // 删除已经存在的marker图标
            if (markerViewingArea.containsKey(info[0])) {
                markerViewingArea.remove(info[0]);
            }


            // 创建监控对象图标
            var marker = lbOrgList.carNameEvade(
                info[0],
                info[1],
                markerLngLat,
                true,
                "0",
                info[6],
                false,
                info[5]
            );
            // 监控对象添加字段
            marker.setAngle(angle);
            marker.extData = info[0]; // 监控对象id
            marker.stateInfo = info[5]; // 监控对象状态
            // marker.content = content.join(""); // 监控对象信息弹窗
            marker.on('click', function (event) {
                lbOrgList.markerClick(event, marker.extData)
            });
            if (markerViewingArea.size() == 0 && isFocus) {
                map.setZoomAndCenter(18, markerLngLat);//将这个点设置为中心点和缩放级别
                // lbOrgList.LimitedSize(6);// 第一个点限制范围
            }
            // var timeOld = (new Date(info[7])).getTime();//获得时间（毫秒）
            var markerList = [
                marker, // marker
                [markerLngLat], // 坐标
                content, // 信息弹窗信息
                [info[8]], // 里程
                [], // 时间
                '0', // ?
                info[5], // 车辆状态
                info[6], // 车辆图标
                [info[4]], // 角度
            ];
            markerViewingArea.put(info[0], markerList);
        },
        /**
         * 保存可以区域外的监控对象信息
         * @param
         */
        saveMarkerOutsideInfo: function (info, content) {
            var id = info[0];
            // 删除可视区域内的信息
            if (markerViewingArea.containsKey(id)) {
                var marker = markerViewingArea.get(id)[0];
                marker.stopMove();
                map.remove([marker]);
                markerViewingArea.remove(id);
            }

            var markerLngLat = [info[2], info[3]]; // 经纬度
            // var angle = Number(info[24]) + 270; // 角度
            var timeOld = info[7] == null ? info[7] : (new Date(info[7])).getTime();//获得时间（毫秒）

            var markerList = [
                // markerRealTime, // marker
                [markerLngLat], // 坐标
                content, // 信息弹窗信息
                [info[8]], // 里程
                [timeOld], // 时间
                '0', // ?
                info[5], // ?
                [info[4]] // 角度
            ];

            if (markerOutside.containsKey(id)) {
                markerOutside.remove(id);
            }
            markerOutside.put(id, markerList);
        },
        /**
         * 车牌号规避
         * @param
         */
        carNameEvade: function (id, name, lnglat, flag, type, ico, showFlag, stateInfo) {
            //监控对象图片大小
            var value = lnglat;
            var picWidth;
            var picHeight;
            var icons;
            /*if (name.length > 8) {
                name = name.substring(0, 7) + '...';
            }
            var num = 0;
            for (var i = 0; i < name.length; i++) {//判断车牌号含有汉字数量
                if (name[i].match(/^[\u4E00-\u9FA5]{1,}$/)) {
                    num++;
                }
            }
            if (num > 3) {
                name = name.substring(0, 4) + '...';
            }*/
            if (type == "0") {
                if (ico == "null" || ico == undefined || ico == null) {
                    icons = "/clbs/resources/img/vehicle.png";
                } else {
                    icons = "/clbs/resources/img/vico/" + ico;
                }
                picWidth = 58 / 2;
                picHeight = 26 / 2;
            } else if (type == "1") {
                icons = "/clbs/resources/img/123.png";
                picWidth = 30 / 2;
                picHeight = 30 / 2;
            }
            if (isCarNameShow) {
                //显示对象姓名区域大小
                var nameAreaWidth = 90;
                var nameAreaHeight = 38;
                //车辆状态没判断
                var carState = lbOrgList.stateCallBack(stateInfo);
                var id = id;
                var name = name;
                //判断是否第一个创建
                var markerAngle = 0; //图标旋转角度
                if (carNameMarkerMap.containsKey(id)) {
                    var thisCarMarker = carNameMarkerMap.get(id);
                    var ssmarker = new AMap.Marker({
                        icon: "https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png",
                        position: [116.41, 39.91]
                    });
                    markerAngle = thisCarMarker.getAngle();
                    var s = ssmarker.getAngle();
                    if (markerAngle > 360) {
                        var i = Math.floor(markerAngle / 360);
                        markerAngle = markerAngle - 360 * i;
                    }
                    ;
                }
                //将经纬度转为像素
                var pixel = map.lngLatToContainer(value);
                var pixelX = pixel.getX();
                var pixelY = pixel.getY();
                var pixelPX = [pixelX, pixelY];
                //得到车辆图标四个角的像素点(假设车图标永远正显示)58*26
                var defaultLU = [pixelX - picWidth, pixelY - picHeight];//左上
                var defaultRU = [pixelX + picWidth, pixelY - picHeight];//右上
                var defaultLD = [pixelX - picWidth, pixelY + picHeight];//左下
                var defaultRD = [pixelX + picWidth, pixelY + picHeight];//右下
                //计算后PX
                var pixelRD = lbOrgList.countAnglePX(markerAngle, defaultRD, pixelPX, 1, picWidth, picHeight);
                var pixelRU = lbOrgList.countAnglePX(markerAngle, defaultRU, pixelPX, 2, picWidth, picHeight);
                var pixelLU = lbOrgList.countAnglePX(markerAngle, defaultLU, pixelPX, 3, picWidth, picHeight);
                var pixelLD = lbOrgList.countAnglePX(markerAngle, defaultLD, pixelPX, 4, picWidth, picHeight);
                //四点像素转为经纬度
                var llLU = map.containTolnglat(new AMap.Pixel(pixelLU[0], pixelLU[1]));
                var llRU = map.containTolnglat(new AMap.Pixel(pixelRU[0], pixelRU[1]));
                var llLD = map.containTolnglat(new AMap.Pixel(pixelLD[0], pixelLD[1]));
                var llRD = map.containTolnglat(new AMap.Pixel(pixelRD[0], pixelRD[1]));
                //车牌显示位置左上角PX
                var nameRD_LU = [pixelRD[0], pixelRD[1]];
                var nameRU_LU = [pixelRU[0], pixelRU[1] - nameAreaHeight];
                var nameLU_LU = [pixelLU[0] - nameAreaWidth, pixelLU[1] - nameAreaHeight];
                var nameLD_LU = [pixelLD[0] - nameAreaWidth, pixelLD[1]];
                //分别将上面四点转为经纬度
                var llNameRD_LU = map.containTolnglat(new AMap.Pixel(nameRD_LU[0], nameRD_LU[1]));
                var llNameRU_LU = map.containTolnglat(new AMap.Pixel(nameRU_LU[0], nameRU_LU[1]));
                var llNameLU_LU = map.containTolnglat(new AMap.Pixel(nameLU_LU[0], nameLU_LU[1]));
                var llNameLD_LU = map.containTolnglat(new AMap.Pixel(nameLD_LU[0], nameLD_LU[1]));
                //判断车牌号该显示的区域
                var isOneArea = true;
                var isTwoArea = true;
                var isThreeArea = true;
                var isFourArea = true;
                //取出所有的左上角的经纬度并转为像素
                var contentArray = [];
                if (!carNameContentLUMap.isEmpty()) {
                    carNameContentLUMap.remove(id);
                    var carContent = carNameContentLUMap.values();
                    for (var i = 0; i < carContent.length; i++) {
                        var contentPixel = map.lngLatToContainer(carContent[i]);
                        contentArray.push([contentPixel.getX(), contentPixel.getY()]);
                    }
                    ;
                }
                ;
                if (contentArray.length != 0) {
                    for (var i = 0; i < contentArray.length; i++) {
                        if (!((contentArray[i][0] + nameAreaWidth) <= nameRD_LU[0] || (contentArray[i][1] + nameAreaHeight) <= nameRD_LU[1] || (nameRD_LU[0] + nameAreaWidth) <= contentArray[i][0] || (nameRD_LU[1] + nameAreaHeight) <= contentArray[i][1])) {
                            isOneArea = false;
                        }
                        ;
                        if (!((contentArray[i][0] + nameAreaWidth) <= nameRU_LU[0] || (contentArray[i][1] + nameAreaHeight) <= nameRU_LU[1] || (nameRU_LU[0] + nameAreaWidth) <= contentArray[i][0] || (nameRU_LU[1] + nameAreaHeight) <= contentArray[i][1])) {
                            isTwoArea = false;
                        }
                        ;
                        if (!((contentArray[i][0] + nameAreaWidth) <= nameLU_LU[0] || (contentArray[i][1] + nameAreaHeight) <= nameLU_LU[1] || (nameLU_LU[0] + nameAreaWidth) <= contentArray[i][0] || (nameLU_LU[1] + nameAreaHeight) <= contentArray[i][1])) {
                            isThreeArea = false;
                        }
                        ;
                        if (!((contentArray[i][0] + nameAreaWidth) <= nameLD_LU[0] || (contentArray[i][1] + nameAreaHeight) <= nameLD_LU[1] || (nameLD_LU[0] + nameAreaWidth) <= contentArray[i][0] || (nameLD_LU[1] + nameAreaHeight) <= contentArray[i][1])) {
                            isFourArea = false;
                        }
                        ;
                    }
                    ;
                }
                ;
                var isConfirm = true;
                var mapPixel;
                var LUPX;
                var showLocation;
                if (isOneArea) {
                    mapPixel = llRD;
                    LUPX = llNameRD_LU;
                    offsetCarName = new AMap.Pixel(0, 0);
                    isConfirm = false;
                    showLocation = "carNameShowRD";
                } else if (isConfirm && isTwoArea) {
                    mapPixel = llRU;
                    LUPX = llNameRU_LU;
                    offsetCarName = new AMap.Pixel(0, -nameAreaHeight);
                    isConfirm = false;
                    showLocation = "carNameShowRU";
                } else if (isThreeArea && isConfirm) {
                    mapPixel = llLU;
                    LUPX = llNameLU_LU;
                    offsetCarName = new AMap.Pixel(-nameAreaWidth, -nameAreaHeight);
                    isConfirm = false;
                    showLocation = "carNameShowLU";
                } else if (isFourArea && isConfirm) {
                    mapPixel = llLD;
                    LUPX = llNameLD_LU;
                    offsetCarName = new AMap.Pixel(-nameAreaWidth, 0);
                    isConfirm = false;
                    showLocation = "carNameShowLD";
                }
                ;
                if (mapPixel == undefined) {
                    mapPixel = llRD;
                    LUPX = llNameRD_LU;
                    offsetCarName = new AMap.Pixel(0, 0);
                    showLocation = "carNameShowRD";
                }
                ;
            }
            ;
            if (flag != null) {
                if (flag) {//创建marker
                    //车辆
                    if (!showFlag) {
                        var markerLocation = new AMap.Marker({
                            position: value,
                            icon: icons,
                            offset: new AMap.Pixel(-picWidth, -picHeight), //相对于基点的位置
                            autoRotation: true,//自动调节图片角度
                            map: map,
                        });
                        markerLocation.name = name;
                        //车辆名
                        carNameMarkerMap.put(id, markerLocation);
                    }
                    ;
                    if (isCarNameShow) {
                        var carContent = "<p class='" + showLocation + "'><i class='" + carState + "'></i>&ensp;<span class='monitorNameBox'>" + name + "</span></p>";
                        if (carNameMarkerContentMap.containsKey(id)) {
                            var nameValue = carNameMarkerContentMap.get(id);
                            map.remove([nameValue]);
                            carNameMarkerContentMap.remove(id);
                        }
                        ;
                        var markerContent = new AMap.Marker({
                            position: mapPixel,
                            content: carContent,
                            offset: offsetCarName,
                            autoRotation: true,//自动调节图片角度
                            map: map,
                            zIndex: 999

                        });
                        markerContent.setMap(map);
                        carNameMarkerContentMap.put(id, markerContent);
                        carNameContentLUMap.put(id, LUPX);
                        if (isConfirm) {
                            markerContent.hide();
                        } else {
                            markerContent.show();
                        }
                        ;
                    }
                    ;
                    if (!showFlag) {
                        return markerLocation;
                    }
                    ;
                } else {//改变位置
                    if (isCarNameShow) {
                        var carContentHtml = "<p class='" + showLocation + "'><i class='" + carState + "'></i>&ensp;<span class='monitorNameBox'>" + name + "</span></p>";
                        if (carNameMarkerContentMap.containsKey(id)) {
                            var carContent = carNameMarkerContentMap.get(id);
                            if (isConfirm) {
                                carContent.hide();
                            } else {
                                carContent.show();
                                carContent.setContent(carContentHtml);
                                carContent.setPosition(mapPixel);
                                carContent.setOffset(offsetCarName);
                            }
                            carNameContentLUMap.put(id, LUPX);
                        }
                        ;
                    }
                    ;
                }
                ;
            } else {
                if (isCarNameShow) {
                    var carContentHtml = "<p class='" + showLocation + "'><i class='" + carState + "'></i>&ensp;<span class='monitorNameBox'>" + name + "</span></p>";
                    if (carNameMarkerContentMap.containsKey(id)) {
                        var thisMoveMarker = carNameMarkerContentMap.get(id);
                        if (isConfirm) {
                            thisMoveMarker.hide();
                        } else {
                            thisMoveMarker.show();
                            thisMoveMarker.setContent(carContentHtml);
                            thisMoveMarker.setPosition(mapPixel);
                            thisMoveMarker.setOffset(offsetCarName);
                        }
                        carNameContentLUMap.put(id, LUPX);
                    }
                    ;
                }
                ;
            }
            ;
        },
        // 监控对象状态返回
        stateCallBack: function (stateInfo) {
            var state;
            switch (stateInfo) {
                case 4:
                    state = 'carStateStop';
                    break;
                case 10:
                    state = 'carStateRun';
                    break;
                case 5:
                    state = 'carStateAlarm';
                    break;
                case 2:
                    state = 'carStateMiss';
                    break;
                case 3:
                    state = 'carStateOffLine';
                    break;
                case 9:
                    state = 'carStateOverSpeed';
                    break;
                case 11:
                    state = 'carStateheartbeat';
                    break;
            }
            ;
            return state;
        },
        //计算车牌号四个定点的像素坐标
        countAnglePX: function (angle, pixel, centerPX, num, picWidth, picHeight) {
            var thisPX;
            var thisX;
            var thisY;
            if ((angle <= 45 && angle > 0) || (angle > 180 && angle <= 225) || (angle >= 135 && angle < 180) || (angle >= 315 && angle < 360)) {
                angle = 0;
            }
            ;
            if ((angle < 90 && angle > 45) || (angle < 270 && angle > 225) || (angle > 90 && angle < 135) || (angle > 270 && angle < 315)) {
                angle = 90;
            }
            ;
            if (angle == 90 || angle == 270) {
                if (num == 1) {
                    thisX = centerPX[0] + picHeight;
                    thisY = centerPX[1] + picWidth;
                }
                ;
                if (num == 2) {
                    thisX = centerPX[0] + picHeight;
                    thisY = centerPX[1] - picWidth;
                }
                ;
                if (num == 3) {
                    thisX = centerPX[0] - picHeight;
                    thisY = centerPX[1] - picWidth;
                }
                ;
                if (num == 4) {
                    thisX = centerPX[0] - picHeight;
                    thisY = centerPX[1] + picWidth;
                }
                ;
            }
            ;
            if (angle == 0 || angle == 180 || angle == 360) {
                thisX = pixel[0];
                thisY = pixel[1];
            }
            ;
            thisPX = [thisX, thisY];
            return thisPX;
        },
        // 清空地图上已创建监控对象图标
        clearMapForMarker: function () {
            var values = markerViewingArea.values();
            for (var i = 0, len = values.length; i < len; i++) {
                var marker = values[i][0];
                marker.stopMove();
                map.remove([marker]);
            }
            // 清空可视区域内经纬度集合
            markerViewingArea.clear();
            // 清空可视区域外经纬度集合
            markerOutside.clear();
            // 清空地图上的监控对象名称
            var nameValues = carNameMarkerContentMap.values();
            map.remove(nameValues);
        },
        // 创建地图可视区域聚合点
        createMarkerClusterer: function () {
            if (cluster != undefined) {
                cluster.clearMarkers();
                cluster.off('click', lbOrgList.clusterClickFun);
            }
            // var values = markerAllUpdateData.values();

            // var markerList = [];
            // for (var i = 0, len = values.length; i < len; i++) {
            //     var markerLngLat = [values[i][0][2], values[i][0][3]];
            //     var id = values[i][0][0];
            //     var content = values[i][1];
            //     var marker = new AMap.Marker({
            //         position: markerLngLat,
            //         icon: "/clbs/resources/img/1.png",
            //         offset: new AMap.Pixel(-26, -13), //相对于基点的位置
            //         autoRotation: true
            //     });
            //     marker.extData = id;
            //     marker.content = content.join("");
            //     // marker.on('click', lbOrgList.markerClick);
            //     markerList.push(marker);
            // }

            lbOrgList.createAllmarker();

            var markerListvalues = markerListMap.values();
            // var markerList = [];
            // for(var i = 0, len = markerListvalues.length; i < len; i++){
            //     markerList.push(markerListvalues[i]);
            // }

            cluster = new AMap.MarkerClusterer(map, markerListvalues, {zoomOnClick: false});
            cluster.on('click', lbOrgList.clusterClickFun);
        },
        /**
         * 创建所有聚合点map集合
         */
        createAllmarker: function () {
            var values = markerAllUpdateData.values();
            for (var i = 0, len = values.length; i < len; i++) {

                if (markerListMap.containsKey(values[i][0][0])) {
                    continue;
                }

                var markerLngLat = [values[i][0][2], values[i][0][3]];
                var id = values[i][0][0];
                var content = values[i][1];
                var marker = new AMap.Marker({
                    position: markerLngLat,
                    icon: "/clbs/resources/img/1.png",
                    offset: new AMap.Pixel(-26, -13), //相对于基点的位置
                    autoRotation: true
                });
                marker.extData = id;
                marker.content = content.join("");

                markerListMap.put(id, marker)
            }
        },
        //车辆标注点击
        markerClick: function (e, id) {
            var vehicleId = id;
            json_ajax('post', '/clbs/adas/lb/show/getVehicleDetails', 'json', true, {id: id}, function (data) {
                if (data.success) {
                    var vehicleInfo = data.obj;
                    // var vehicleId = vehicleInfo.vehicleId; //车id
                    // var status = vehicleInfo.status; //车状态
                    var brand = vehicleInfo.brand; //车名称
                    var longitude = vehicleInfo.longitude; //车经度
                    var latitude = vehicleInfo.latitude; //车纬度
                    var direction = vehicleInfo.direction; //车图标角度
                    var vehicleIcon = vehicleInfo.vehicleIcon; //车图标

                    var timeInfo = vehicleInfo.time; //时间
                    var plateColor = vehicleInfo.plateColor; //车牌颜色
                    var deviceNo = vehicleInfo.deviceNo; //终端号
                    var simcardNumber = vehicleInfo.simcardNumber; //终端手机号
                    var stateInfo = vehicleInfo.stateInfo; //行驶状态
                    var groupName = vehicleInfo.groupName; //所属企业
                    var speed = vehicleInfo.speed; //速度
                    var address = vehicleInfo.address; //位置
                    var assign = vehicleInfo.assign; //分组
                    var vehicleType = vehicleInfo.vehicleType; //对象类型
                    // var status = vehicleInfo.status; //ACC
                    var todayDistance = vehicleInfo.todayDistance; //当日里程
                    var mileage = vehicleInfo.mileage; //总里程

                    var professionalName = vehicleInfo.professionalName //从业人员
                    var peopleIDcard = vehicleInfo.cardNumber
                    var msgId = vehicleInfo.msgId //msgId


                    var vehicleInfoStr = JSON.stringify(vehicleInfo).replace(/"/g, "'");

                    var content = [];
                    //begin-1
                    content.push("<div class='col-md-12' id='basicStatusInformation' style='padding:0px;'>");
                    content.push("<div>时间：" + timeInfo + "</div>");
                    if (plateColor == "") {
                        content.push("<div>监控对象：" + brand + "</div>");
                    } else {
                        content.push("<div>监控对象：" + brand + "(" + plateColor + ")</div>");
                    }
                    content.push("<div>终端号：" + (deviceNo === undefined ? "" : deviceNo) + "</div>");
                    content.push("<div>终端手机号：" + simcardNumber + "</div>");
                    if (stateInfo == "行驶") {
                        content.push("<div>行驶状态：" + "<font color='#78af3a'>" + stateInfo + "</font>" + "</div>");
                    } else if (stateInfo == "停止") {
                        content.push("<div>行驶状态：" + "<font color='#c80002'>" + stateInfo + "</font>" + "</div>");
                    }
                    var speed7 = speed;
                    content.push("<div>行驶速度：" + speed7 + "</div>");
                    content.push("<div>位置：" + address + "</div>");
                    //轨迹跟踪点名

                    var type = '', pid = '', uuids = '';

                    var str1 = professionalName, str2 = '';

                    content.push(
                        '<div class="infoWindowSetting">' +
                        '<a class="col-md-3" id="jumpTo" onClick="lbOrgList.jumpToTrackPlayer(\'' + vehicleId + '\',\'' + type + '\',\'' + pid + '\',\'' + uuids + '\')">' +
                        '<img src="/clbs/resources/img/v-track.svg" style="height:28px;width:28px;"/>轨迹' +
                        '</a>' +

                        '<a class="col-md-2 callName" onClick="lbOrgList.jumpToRealTimeVideoPage(\'' + vehicleId + '\')">' +
                        '<img src="/clbs/resources/img/video_info_jump.svg" style="height:28px;width:28px;"/>视频' +
                        '</a>' +

                        '<a class="col-md-3 text-right pull-right" style="padding-top:24px;">' +
                        '<i class="fa fa-chevron-circle-right fa-2x vStatusInfoShowMore" id="vStatusInfoShowMore" onclick="lbOrgList.vStatusInfoShow(' + vehicleInfoStr + ' ,\'' + assign + '\',\'' + str1 + '\',\'' + str2 + '\',\'' + peopleIDcard + '\')"></i>' +
                        '</a>' +
                        '</div>'
                    );
                    content.push("</div>");
                    //begin-2


                    // ACC
                    var acc = vehicleInfo.status == undefined ? 0 : vehicleInfo.status;

                    acc = acc.toString(2);
                    acc = (Array(32).join(0) + acc).slice(-32);//高位补零

                    if (acc.length == 32) {
                        acc = acc.substring(18, 19);
                    } else if (acc == 0) {
                        acc = 0;
                    } else {
                        acc = vehicleInfo.status & 1;
                    }


                    if ((acc + "").length == 1) {
                        acc = (acc == 0 ? "关" : "开");
                    } else if (acc == "21") {
                        acc = "点火静止";
                    } else if (acc == "16") {
                        acc = "熄火拖车";
                    } else if (acc == "1A") {
                        acc = "熄火假拖车";
                    } else if (acc == "11") {
                        acc = "熄火静止";
                    } else if (acc == "12") {
                        acc = "熄火移动";
                    } else if (acc == "22") {
                        acc = "点火移动";
                    } else if (acc == "41") {
                        acc = "无点火静止";
                    } else if (acc == "42") {
                        acc = "无点火移动";
                    }

                    content.push("<div class='col-md-8' id='v-statusInfo-show'>");
                    content.push("<div class='col-md-6' style=''>");
                    content.push("<div>所属企业：" + groupName + "</div>");
                    content.push("<div>所属分组：" + assign + "</div>");
                    content.push("<div>对象类型：" + vehicleType + "</div>");
                    if (status == "开" || (acc.indexOf("无") == -1 && acc.indexOf("点火") > -1)) {
                        content.push("<div>ACC：" + acc + " <img src='/clbs/resources/img/acc_on.svg' style='margin: -3px 0px 0px 0px;height:24px;'/></div>");
                    } else {
                        content.push("<div>ACC：" + acc + " <img src='/clbs/resources/img/acc_off.svg' style='margin: -3px 0px 0px 0px;height:24px;'/></div>");
                    }
                    content.push("<div>当日里程：" + Number(todayDistance).toFixed(1) + "公里</div>");
                    content.push("<div><span id='bombBox2'></span></div>");
                    content.push("<div><span id='bombBox3'></span></div>");
                    content.push("<div>总里程：" + Number(mileage).toFixed(1) + "公里</div>");

                    content.push("<div><span id='bombBox0'></span></div>");
                    content.push("<div><span id='bombBox1'></span></div>");
                    content.push("</div>");
                    //begin-3
                    content.push(
                        '<div class="col-md-6" style="">' +
                        '<div class="arrow"></div>' +
                        '<div><span id="bombBox4"></span></div>' +
                        '<div><span id="bombBox5"></span></div>' +
                        '<div><span id="bombBox6"></span></div>' +
                        '<div><span id="bombBox7"></span></div>' +
                        '<div><span id="bombBox8"></span></div>' +
                        '<div><span id="bombBox9"></span></div>' +
                        '<div><span id="bombBox10"></span></div>' +
                        '<div><span id="bombBox11"></span></div>' +
                        '<div><span id="bombBox12"></span></div>' +
                        '<div><span id="bombBox13"></span></div>' +
                        '<div><span id="bombBox14"></span></div>' +
                        '<div><span id="bombBox15"></span></div>' +
                        '<div><span id="bombBox16"></span></div>' +
                        '</div>' +
                        '</div>'
                    );
                    content.push("</div>");
                    content = content.join("");


                    var markerLngLat = e.target.getPosition();
                    vinfoWindwosClickVid = e.target.extData;
                    // infoWindow.setContent(e.target.content);
                    infoWindow.setContent(content);
                    infoWindow.open(map, markerLngLat);
                    markerClickLngLat = markerLngLat;
                }

            })


        },
        vStatusInfoShow: function (data, group, people, alam, peopleIDcard) {
            //获取当前车辆点击的经纬度
            var currentCarCoordinate = "";
            if (map.getZoom() >= 16) {
                currentCarCoordinate = (markerViewingArea.get(vinfoWindwosClickVid))[0].getPosition();
            } else {
                currentCarCoordinate = markerClickLngLat;
            }
            //点击时判断是否显示信息框
            if ($("#v-statusInfo-show").is(":hidden")) {
                //执行显示
                $("#basicStatusInformation").removeAttr("class");
                $("#basicStatusInformation").addClass("col-md-4");
                $("#basicStatusInformation").parent().css("width", "574px");
                $("#vStatusInfoShowMore").removeClass("fa-chevron-circle-right").addClass("fa-chevron-circle-left");
                $("#v-statusInfo-show").show();
                //执行信息框底部移动方法
                lbOrgList.amapInfoSharpAdaptiveFn();
                //执行信息框整体基点位置方法
                infoWindow.setPosition(currentCarCoordinate);
                $("#basicStatusInformation").css({"width": "158px", "margin-right": "20px"});
                //加入数据
                var num = +data.status;
                var dataa = num.toString(2);
                dataa = (Array(32).join(0) + dataa).slice(-32);//高位补零

                //获取记录仪速度
                var gpsAttachInfoList = data.gpsAttachInfoList;
                var speedX = 0;
                if (gpsAttachInfoList != undefined) {
                    if (Array.isArray(gpsAttachInfoList)) {
                        for (var i = 0; i < gpsAttachInfoList.length; i++) {
                            var gpsAttachInfoID = gpsAttachInfoList[i].gpsAttachInfoID;
                            if (gpsAttachInfoID == 3) {
                                speedX = gpsAttachInfoList[i].speed;
                            }
                            ;
                        }
                        ;
                    }
                    ;
                }


                if (data.msgId == 513) {
                    $("#bombBox0").text("单次回报应答");
                }


                $("#bombBox1").text(alam);
                if (dataa.substring(29, 30) == 0) {
                    $("#bombBox2").text("北纬：" + data.latitude);
                } else if (dataa.substring(30, 31) == 1) {
                    $("#bombBox2").text("南纬：" + data.latitude);
                }
                if (dataa.substring(28, 29) == 0) {
                    $("#bombBox3").text("东经：" + data.longitude);
                } else if (dataa.substring(28, 29) == 1) {
                    $("#bombBox3").text("西经：" + data.longitude);
                }

                $("#bombBox4").text("方向：" + lbOrgList.toDirectionStr(data.direction));
                $("#bombBox5").text("记录仪速度：" + speedX);
                $("#bombBox6").text("高程：" + data.altitude);
                $("#bombBox7").text("电子运单：");
                if (people == "null") {
                    people = "";
                }
                $("#bombBox8").text("从业人员：" + people);
                $("#bombBox9").text("从业资格证号：" + peopleIDcard);
                if (dataa.substring(27, 28) == 0) {
                    $("#bombBox10").text("运营状态");
                } else if (dataa.substring(27, 28) == 1) {
                    $("#bombBox10").text("停运状态");
                }
                ;
                if (dataa.substring(21, 22) == 0) {
                    $("#bombBox11").text("车辆油路正常");
                } else if (dataa.substring(21, 22) == 1) {
                    $("#bombBox11").text("车辆油路断开");
                }
                ;
                if (dataa.substring(20, 21) == 0) {
                    $("#bombBox12").text("车辆电路正常");
                } else if (dataa.substring(20, 21) == 1) {
                    $("#bombBox12").text("车辆电路断开");
                }
                ;
                if (dataa.substring(19, 20) == 0) {
                    $("#bombBox13").text("车门解锁");
                } else if (dataa.substring(19, 20) == 1) {
                    $("#bombBox13").text("车门加锁");
                }
                ;
            } else {
                //执行显示
                $("#basicStatusInformation").removeAttr("class");
                $("#basicStatusInformation").addClass("col-md-12");
                $("#basicStatusInformation").parent().css("width", "196px");
                $("#vStatusInfoShowMore").removeClass("fa-chevron-circle-left").addClass("fa-chevron-circle-right");
                $("#v-statusInfo-show").hide();
                //执行信息框底部移动方法
                lbOrgList.amapInfoSharpAdaptiveFn();
                //执行信息框整体基点位置方法
                infoWindow.setPosition(currentCarCoordinate);
                $("#basicStatusInformation").css("width", "none");
            }
        },
        // 聚合点击事件
        clusterClickFun: function (data) {
            var position = data.lnglat;
            var zoom = map.getZoom();

            if (zoom < 6) {
                map.setZoomAndCenter(6, position);
            }
            else if (zoom < 11) {
                map.setZoomAndCenter(11, position);
            }
            else if (zoom < 15) {
                map.setZoomAndCenter(15, position);
            }
            else if (zoom < 16) {
                map.setZoomAndCenter(16, position);
            }
        },
        //跳转至轨迹回放
        jumpToTrackPlayer: function (sid, type, pid, uuids) {
            var jumpFlag = false;
            var permissionUrls = $("#permissionUrls").val();
            if (permissionUrls != null && permissionUrls != undefined) {
                var urllist = permissionUrls.split(",");
                if (urllist.indexOf("/v/monitoring/trackPlayback") > -1) {
                    var uuidStr = JSON.stringify(uuids);
                    sessionStorage.setItem('uuid', uuidStr);
                    var url = "/clbs/v/monitoring/trackPlayBackLog";
                    var data = {"vehicleId": sid, "type": type};
                    json_ajax("POST", url, "json", false, data, null);
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                    jumpFlag = true;
                    location.href = "/clbs/v/monitoring/trackPlayback?vid=" + sid + "&type=" + type + "&pid=" + pid;
                }
            }
            if (!jumpFlag) {
                layer.msg("无操作权限，请联系管理员");
            }
        },
        //跳转到实时视频页面
        jumpToRealTimeVideoPage: function (sid) {
            var jumpFlag = false;
            var permissionUrls = $("#permissionUrls").val();
            if (permissionUrls != null && permissionUrls != undefined) {
                var urllist = permissionUrls.split(",");
                if (urllist.indexOf("/realTimeVideo/video/list") > -1) {
                    jumpFlag = true;
                    location.href = "/clbs/realTimeVideo/video/list?videoId=" + sid;
                }
            }
            if (!jumpFlag) {
                layer.msg("无操作权限，请联系管理员");
            }
        },
        //监控对象信息框更多显示方法
        amapInfoSharpAdaptiveFn: function () {
            if ($("#v-statusInfo-show").is(":hidden")) {
                $(".amap-info-sharp").removeClass("amap-info-sharp-marleft-hide");
                $(".amap-info-sharp").removeClass("amap-info-sharp-marleft-show");
                $(".amap-info-sharp").addClass("amap-info-sharp-marleft-hide");
            } else {
                $(".amap-info-sharp").removeClass("amap-info-sharp-marleft-hide");
                $(".amap-info-sharp").removeClass("amap-info-sharp-marleft-show");
                $(".amap-info-sharp").addClass("amap-info-sharp-marleft-show");
            }
        },
        /**
         * 车牌号标注是否显示
         * @param flag : 车牌显示开关
         */
        carNameState: function (flag) {
            var carNameMarkerValue;
            if (!carNameMarkerContentMap.isEmpty()) {
                carNameMarkerValue = carNameMarkerContentMap.values();
            }
            ;
            if (flag) {
                //重新计算对象名称位置
                lbOrgList.carNameShow();
            } else {
                if (carNameMarkerValue != undefined) {
                    for (var i = 0, len = carNameMarkerValue.length; i < len; i++) {
                        carNameMarkerValue[i].hide();
                    }
                    ;
                }
                ;
            }
            ;
        },
        // 跳点运动
        markerJumpPoint: function (info) {

            var id = info[0];
            var value = markerViewingArea.get(id);

            var marker = value[0];
            var movePosition = value[1][1];

            // marker设置经纬度
            marker.setPosition(movePosition);
            // 设置图标角度
            var angle;
            if (icoUpFlag) {
                angle = 0;
            } else {
                angle = Number(value[8][1]) + 270;
            }
            marker.setAngle(angle);

            // 判断跳到指定点是否超出范围
            // if (!pathsTwo.contains(movePosition)) {
            //     map.setCenter(movePosition);
            //     lbOrgList.pathsChangeFun();
            //     lbOrgList.LimitedSizeTwo();
            // };

            // 删除集合中已经走完的点
            markerViewingArea.remove(id);
            value[1].splice(0, 1);
            value[3].splice(0, 1);
            value[4].splice(0, 1);
            value[8].splice(0, 1);
            markerViewingArea.put(id, value);
            //车牌避让
            lbOrgList.carNameEvade(
                id,
                marker.name,
                movePosition,
                null,
                "0",
                null,
                false,
                marker.stateInfo
            );
            // 跳点完成后，判断经纬度数据长度是否有堆积
            if (value[1].length > 1) {
                var newValue = markerViewingArea.get(id);
                lbOrgList.markerJumpPoint(id, newValue);
            }
        },
        /**
         * 重新计算对象名称位置
         */
        carNameShow: function () {
            //清空车牌号显示位置信息
            if (map.getZoom() > 15) {
                var values = markerViewingArea.values();

                for (var i = 0, len = values.length; i < len; i++) {
                    var marker = values[i][0]; // [7] 图标
                    var id = marker.extData;
                    var name = marker.name;
                    var markerLngLat = marker.getPosition();
                    var icon = values[i][7];
                    var stateInfo = marker.stateInfo;
                    lbOrgList.carNameEvade(id, name, markerLngLat, true, "1", icon, true, stateInfo);
                }
            }
            ;
        },
        /**
         * 车牌号标注是否显示
         */
        carNameState: function (flag) {
            var carNameMarkerValue;
            if (!carNameMarkerContentMap.isEmpty()) {
                carNameMarkerValue = carNameMarkerContentMap.values();
            }
            ;
            if (flag) {
                //重新计算对象名称位置
                lbOrgList.carNameShow();
            } else {
                if (carNameMarkerValue != undefined) {
                    for (var i = 0, len = carNameMarkerValue.length; i < len; i++) {
                        carNameMarkerValue[i].hide();
                    }
                    ;
                }
                ;
            }
            ;
        },
        /**
         * 车辆方向处理
         * @param angle
         * @returns {*}
         */
        toDirectionStr: function (angle) {
            if ((0 <= angle && 22.5 >= angle) || (337.5 < angle && angle <= 360)) {
                direction = '北';
            } else if (22.5 < angle && 67.5 >= angle) {
                direction = '东北';
            } else if (67.5 < angle && 112.5 >= angle) {
                direction = '东';
            } else if (112.5 < angle && 157.5 >= angle) {
                direction = '东南';
            } else if (157.5 < angle && 202.5 >= angle) {
                direction = '南';
            } else if (202.5 < angle && 247.5 >= angle) {
                direction = '西南';
            } else if (247.5 < angle && 292.5 >= angle) {
                direction = '西';
            } else if (292.5 < angle && 337.5 >= angle) {
                direction = '西北';
            } else {
                direction = '未知数据';
            }
            return direction;
        },
        /**
         * 显示设置
         */
        smoothMoveOrlogoDisplayClickFn: function () {
            var id = $(this).attr("id");

            var self = $(this);

            var id = self.attr('id'),
                checked = self.prop('checked'),
                label = self.siblings('label');

            //标识显示
            if (id == "logoDisplay") {
                if (!checked) {
                    isCarNameShow = false;
                    label.removeClass("preBlue");
                } else {
                    isCarNameShow = true;
                    label.addClass("preBlue");
                }

                lbOrgList.carNameState(isCarNameShow);
            }
            //天气
            if (id == 'weather') {
                var mapZoom = map.getZoom();
                if (!checked) {
                    isWeatherShow = false;
                    label.removeClass("preBlue");
                } else {
                    isWeatherShow = true;
                    label.addClass("preBlue");
                }
                mapWeather.showWeather(map, mapZoom, isWeatherShow);
            }
        },
        /**
         * 创建map集合
         */
        createMap: function () {

            markerMap = new lbOrgList.mapVehicle();
            carNameMarkerMap = new lbOrgList.mapVehicle();
            carNameMarkerContentMap = new lbOrgList.mapVehicle();
            carNameContentLUMap = new lbOrgList.mapVehicle();

            markerViewingArea = new lbOrgList.mapVehicle();
            markerOutside = new lbOrgList.mapVehicle();
            markerAllUpdateData = new lbOrgList.mapVehicle();


            markerListMap = new lbOrgList.mapVehicle(); //创建所有地图绘制聚合点集合
            websocketUpdateMap = new lbOrgList.mapVehicle();

        },
        // 封装map集合
        mapVehicle: function () {

            this.elements = {};
            //获取MAP元素个数
            this.size = function () {
                return Object.keys(this.elements).length;
            };
            //判断MAP是否为空
            this.isEmpty = function () {
                return (Object.keys(this.elements).length < 1);
            };
            //删除MAP所有元素
            this.clear = function () {
                this.elements = {};
            };
            //向MAP中增加元素（key, value)
            this.put = function (_key, _value) {
                this.elements[_key] = _value;
            };
            //删除指定KEY的元素，成功返回True，失败返回False
            this.remove = function (_key) {
                delete this.elements[_key];
            };
            //获取指定KEY的元素值VALUE，失败返回NULL
            this.get = function (_key) {
                return this.elements[_key];
            };
            //获取指定索引的元素（使用element.key，element.value获取KEY和VALUE），失败返回NULL
            this.element = function (_index) {
                var keys = Object.keys(this.elements);
                var key = keys[_index];
                return this.elements[key];
            };
            //判断MAP中是否含有指定KEY的元素
            this.containsKey = function (_key) {
                if (this.elements[_key]) {
                    return true;
                } else {
                    return false;
                }
            };
            //判断MAP中是否含有指定VALUE的元素
            this.containsValue = function (_value) {
                var bln = false;
                try {
                    for (var i = 0, len = this.elements.length; i < len; i++) {
                        if (this.elements[i].value == _value) {
                            bln = true;
                        }
                    }
                } catch (e) {
                    bln = false;
                }
                return bln;
            };
            //获取MAP中所有VALUE的数组（ARRAY）
            this.values = function () {
                var arr = new Array();
                var keys = Object.keys(this.elements);
                for (var i = 0, len = keys.length; i < len; i++) {
                    arr.push(this.elements[keys[i]]);
                }
                return arr;
            };
            //获取MAP中所有KEY的数组（ARRAY）
            this.keys = function () {
                return Object.keys(this.elements);
            };
        },
        /**
         * 获取实时风控预警数
         */
        getNowRisk: function () {
            json_ajax('post', '/clbs/adas/lbOrg/show/getNowRisk', 'json', true, '', function (data) {
                var str = data.toString()
                var html = '';
                for (var i = 0; i < str.length; i++) {
                    html += '<span class="span">' + str[i] + '</span>'
                }
                html += '件';

                $('#warning-number').html(html);
            })
        },
        /**
         * 获取车辆在线数
         */
        getOnlineInfo: function () {
            json_ajax('post', '/clbs/adas/lbOrg/show/getVehicleOnlie', 'json', true, '', function (data) {
                var html = '';
                var arr = data.toString().split('');
                arr.forEach(function (item) {
                    html += '<span class="span">' + item + '</span>';
                });
                html += '<span>辆</span>';
                $('#online-vehicle-number').html(html);
            })
        },
        /**
         * 获取昨日风控预警数
         */
        getYesterdayRisk: function () {

            json_ajax('post', '/clbs/adas/lbOrg/show/getYesterdayRisk', 'json', true, '', function (data) {
                $('#yesterDay-warning-number').html(data + '件')
            })
        },
        /**
         * 获取今日此时上线率和昨日上线率
         */
        getLineRate: function () {
            json_ajax('post', '/clbs/adas/lbOrg/show/getLineRate', 'json', true, '', function (data) {
                if (data.success) {
                    data = data.obj;
                    var nowNum = '0',
                        yesNum = '0',
                        nowRate = '0',
                        yesRate = '0';

                    if (data) {
                        nowNum = data.todayNumber
                        yesNum = data.yesterdayNumber
                        nowRate = data.todayOnLineRate
                        yesRate = data.yesterdayOnLineRate
                    }

                    $('#online-now').text(nowNum);
                    $('#online-yesterday').text(yesNum);
                    $('#onlinePercent-now').text(nowRate);
                    $('#onlinePercent-yesterday').text(yesRate);
                }
            })
        },
        /**
         * 昨日此时环比增长
         */
        getRingRatioRiskEvent: function () {
            json_ajax('post', '/clbs/adas/lbOrg/show/getRingRatioRiskEvent', 'json', true, '', function (data) {
                if (data.success) {
                    data = data.obj;
                    $('#yesterDay-ratio-up').text(data.ringRatio);
                    var tend = $('#yesterdayWarningNum .chart-icon');
                    if (data.trend == 0) {
                        tend.addClass('normal');
                    } else if (data.trend == 1) {
                        tend.addClass('up');
                    } else if (data.trend == -1) {
                        tend.addClass('down');
                    }
                }
            })
        },
        /**
         * 风险报警占比
         */
        riskWarnChart: function () {
            var option = {
                backgroundColor: 'rgba(128, 128, 128, 0)',
                color: ['#79d5ff', '#807ae3', '#9ebffa', '#56a1d5', '#fadb71', '#FF8C00'],
                legend: {
                    itemWidth: 10,
                    itemHeight: 10,
                    orient: 'vertical',
                    left: 0,
                    top: '20%',
                    selectedMode: false
                },
                title: {
                    text: '风险报警占比',
                    textStyle: {
                        fontSize: 16,
                        fontWeight: 'normal',
                        color: '#767676'
                    },
                    left: 'left',
                    top: '0px'
                },
                grid: {
                    bottom: '40px'
                },
                series: [{
                    name: '来源',
                    type: 'pie',
                    radius: ['55%', '65%'],
                    center: ['65%', '50%'],
                    avoidLabelOverlap: false,
                    hoverAnimation: false,
                    label: {
                        normal: {
                            show: false,
                            position: 'center',
                            textStyle: {
                                fontSize: 30,
                                color: '#767676'
                            },
                            formatter: ['{c}%', '{title|{b}}'].join('\n'),
                            rich: {
                                title: {
                                    fontSize: 16,
                                    color: '#767676',
                                    height: 30,
                                    lineHeight: 30,
                                }
                            }
                        }
                    },
                }]
            };

            riskWarnChart = echarts.init(document.getElementById('riskWarnChart'));
            riskWarnChart.setOption(option);
        },
        riskWarnData: function () {
            var params = {
                groupId: groupId,
                isToday: isToday,
            };
            json_ajax("POST", "/clbs/adas/lbOrg/show/getRiskProportion", "json", true, params, function (data) {
                if (data.success) {
                    getRiskProportionData = data.obj;
                    lbOrgList.getRiskProportion();
                }
            });
        },
        getRiskProportion: function(){
            var num = 6;
            var riskWarnDatas = [],
                riskWarnLegend = [],
                riskLevelDatas = [],
                riskLevelLegend = [];

            var show = false,
                show2 = false;

            if (riskWarnNum == num) {
                riskWarnNum = 0;
            }
            if (riskLevelNum == getRiskProportionData.length) {
                riskLevelNum = num;
            }
            lbOrgList.riskWarnBottom(getRiskProportionData);//设置底部数据
            lbOrgList.riskLevelBottom(getRiskProportionData);//设置底部数据

            getRiskProportionData.forEach(function (item, index) {
                if (index < num) {//前5个是风险报警占比
                    if (index == riskWarnNum) {
                        show = true;
                    } else {
                        show = false;
                    }

                    var obj = {
                        value: parseFloat(item.proportion).toFixed(2),
                        name: item.name,
                        label: {
                            normal: {
                                show: show,
                            }
                        }
                    }

                    riskWarnDatas.push(obj);
                    riskWarnLegend.push(item.name);
                } else if (index >= num) {//风险等级占比
                    if (index == riskLevelNum) {
                        show2 = true;
                    } else {
                        show2 = false;
                    }

                    var obj = {
                        value: parseFloat(item.proportion).toFixed(2),
                        name: item.name,
                        label: {
                            normal: {
                                show: show2,
                            }
                        }
                    };
                    riskLevelDatas.push(obj);
                    riskLevelLegend.push(item.name);
                }
            });
            riskWarnChart.setOption({
                legend: {
                    data: riskWarnLegend,
                },
                series: [{
                    data: riskWarnDatas,
                }]
            });
            riskLevelChart.setOption({
                legend: {
                    data: riskLevelLegend,
                },
                series: [{
                    data: riskLevelDatas,
                }]
            });
            riskWarnNum++;
            riskLevelNum++;
        },
        riskWarnBottom: function (data) {
            var icon = $('#radioIcon'),
                riskWarnTxt = $('#riskWarnTxt'),
                riskWarnTxt2 = $('#riskWarnTxt2');

            var ringRatio = '';
            ringRatio = data[riskWarnNum].ringRatio;
            riskWarnTxt.text(data[riskWarnNum].name + ': ' + data[riskWarnNum].total);
            lbOrgList.getBottomIcon(riskWarnTxt2, ringRatio, icon);
        },
        riskLevelBottom: function (data) {
            var icon = $('#radioIcon2'),
                riskWarnTxt = $('#riskLevelTxt'),
                riskWarnTxt2 = $('#riskLevelTxt2');
            var ringRatio = '';
            ringRatio = data[riskLevelNum].ringRatio;
            riskWarnTxt.text(data[riskLevelNum].name + ': ' + data[riskLevelNum].total);
            lbOrgList.getBottomIcon(riskWarnTxt2, ringRatio, icon);
        },
        /**
         * 风险等级占比
         */
        riskLevelChart: function () {
            var option = {
                backgroundColor: 'rgba(128, 128, 128, 0)',
                color: ['#79d5ff', '#807ae3', '#9ebffa', '#56a1d5'],
                legend: {
                    itemWidth: 10,
                    itemHeight: 10,
                    orient: 'vertical',
                    left: 0,
                    top: '25%',
                    selectedMode: false
                },
                title: {
                    text: '风险等级占比',
                    textStyle: {
                        fontSize: 16,
                        fontWeight: 'normal',
                        color: '#767676'
                    },
                    left: 'left',
                    top: '0px'
                },
                grid: {
                    bottom: '40px'
                },
                series: [{
                    name: '来源',
                    type: 'pie',
                    radius: ['55%', '65%'],
                    center: ['65%', '50%'],
                    avoidLabelOverlap: false,
                    hoverAnimation: false,
                    label: {
                        normal: {
                            show: false,
                            position: 'center',
                            textStyle: {
                                fontSize: 30,
                                color: '#767676'
                            },
                            formatter: ['{c}%', '{title|{b}}'].join('\n'),
                            rich: {
                                title: {
                                    fontSize: 16,
                                    color: '#767676',
                                    height: 30,
                                    lineHeight: 30,
                                }
                            }
                        }
                    },
                }]
            };

            riskLevelChart = echarts.init(document.getElementById('riskLevelChart'));
            riskLevelChart.setOption(option);
        },
        /**
         * 风险处置情况
         */
        riskDealChart: function () {
            var option = {
                backgroundColor: 'rgba(128, 128, 128, 0)',
                color: ['#79d5ff', '#807ae3', '#9ebffa', '#56a1d5'],
                legend: {
                    itemWidth: 10,
                    itemHeight: 10,
                    orient: 'vertical',
                    left: 0,
                    top: '25%',
                    selectedMode: false
                },
                title: {
                    text: '风险处置情况',
                    textStyle: {
                        fontSize: 16,
                        fontWeight: 'normal',
                        color: '#767676'
                    },
                    left: 'left',
                    top: '0px'
                },
                grid: {
                    bottom: '40px'
                },
                series: [{
                    type: 'pie',
                    radius: ['55%', '65%'],
                    center: ['65%', '50%'],
                    avoidLabelOverlap: false,
                    hoverAnimation: false,
                    label: {
                        normal: {
                            show: false,
                            position: 'center',
                            textStyle: {
                                fontSize: 30,
                                color: '#767676'
                            },
                            formatter: ['{c}%', '{title|{b}}'].join('\n'),
                            rich: {
                                title: {
                                    fontSize: 16,
                                    color: '#767676',
                                    height: 30,
                                    lineHeight: 30,
                                }
                            }
                        }
                    },
                }]
            };

            riskDealChart = echarts.init(document.getElementById('riskDealChart'));
            riskDealChart.setOption(option);
        },
        riskDealData: function () {
            var params = {
                groupId: groupId,
                isToday: isToday,
            };

            json_ajax('post', '/clbs/adas/lbOrg/show/getRisksDealInfo', 'json', true, params, function (data) {
                if (data.success) {
                    getRisksDealInfoData = data.obj;
                    lbOrgList.getRisksDealInfo();
                }
            })
        },
        getRisksDealInfo: function(){
            var show = false;
            var riskDealDatas = [],
                riskDealLegend = [];

            if (riskDealNum >= getRisksDealInfoData.length) {
                riskDealNum = 0;
            }
            lbOrgList.riskDealBottom(getRisksDealInfoData);

            getRisksDealInfoData.forEach(function (item, index) {
                if (index == riskDealNum) {
                    show = true;
                } else {
                    show = false;
                }

                var obj = {
                    value: parseFloat(item.proportion).toFixed(2),
                    name: item.name,
                    label: {
                        normal: {
                            show: show,
                        }
                    }
                }

                riskDealDatas.push(obj);
                riskDealLegend.push(item.name);
            });

            riskDealChart.setOption({
                legend: {
                    data: riskDealLegend,
                },
                series: [{
                    data: riskDealDatas,
                }]
            });
            riskDealNum++;
        },
        riskDealBottom: function (data) {
            var icon = $('#radioIcon3'),
                riskWarnTxt = $('#riskDealTxt'),
                riskWarnTxt2 = $('#riskDealTxt2');

            var ringRatio = '';
            if (data[riskDealNum].ringRatio) {
                ringRatio = data[riskDealNum].ringRatio
            }

            riskWarnTxt.text(data[riskDealNum].name + ': ' + data[riskDealNum].number);
            lbOrgList.getBottomIcon(riskWarnTxt2, ringRatio, icon);
        },
        getBottomIcon: function (riskWarnTxt2, ringRatio, icon) {
            var radio = Math.abs(ringRatio);
            icon.removeClass('up');
            icon.removeClass('down');
            icon.removeClass('normal');
            if (ringRatio == '') {
                riskWarnTxt2.text('');
                return;
            }

            if (ringRatio == 0) {
                riskWarnTxt2.text('环比不变: ' + radio);
                icon.addClass('normal');
            } else if (ringRatio > 0) {
                riskWarnTxt2.text('环比增长: ' + radio);
                icon.addClass('up');
            } else if (ringRatio < 0) {
                riskWarnTxt2.text('环比下降: ' + radio);
                icon.addClass('down');
            }
        },
        /**
         * 报警类型排行
         */
        riskEventChart: function (riskEventX, riskEventY) {
            var option = {
                backgroundColor: 'rgba(128, 128, 128, 0)',
                title: {
                    text: '报警类型排行',
                    textStyle: {
                        fontSize: 16,
                        fontWeight: 'normal',
                        color: '#767676'
                    },
                    left: 'left',
                    top: '0px',
                },
                grid: {
                    containLabel: true,
                    width: '100%',
                    left: -15,
                    right: 100,
                    bottom: 0,
                },
                /*tooltip:{
                    trigger: 'axis',
                },*/
                /*dataZoom: [
                    {
                        // type: 'inside',
                        show: true,
                        start: 0,
                        end: 80,
                        top: 0,
                    },
                ],*/
                xAxis: {
                    type: 'category',
                    data: riskEventX,
                    axisLabel: { //坐标轴刻度标签
                        show: true,
                        interval: 0,
                        // rotate: 305,
                        textStyle: {
                            color: "#2f2f2f",
                            fontSize: 12,
                            fontWeight: 'lighter',
                        },
                        formatter: function (value, index) {
                            return value.split("").join("\n");
                        }
                    },
                    axisLine: { //坐标轴轴线
                        show: false
                    },
                    axisTick: { //坐标轴刻度
                        show: false
                    }
                },
                yAxis: {
                    type: 'value',
                    show: false
                },
                series: [{
                    data: riskEventY,
                    type: 'bar',
                    barMaxWidth: 20,//最大宽度
                    barMinWidth: 10,//最大宽度
                    itemStyle: {
                        normal: {
                            color: '#198ef0'
                        }
                    },
                    label: {
                        normal: {
                            show: true,
                            rotate: 45,
                            distance: 15,
                            position: [0, -10],
                        }
                    }
                }]
            };
            riskEventChart = echarts.init(document.getElementById('riskEventChart'));
            riskEventChart.setOption(option);
        },
        riskEventData: function () {
            var params = {
                groupId: groupId,
                isToday: isToday,
            };
            json_ajax("POST", "/clbs/adas/lbOrg/show/getEventRanking", "json", true, params, function (data) {
                if (data.success) {
                    var riskEventX = [],
                        riskEventY = [];

                    data = data.obj;
                    data.forEach(function (item,index) {
                        if(index >= 20) return
                        riskEventX.push(item.name.trim());
                        riskEventY.push(parseFloat(item.value));
                    });

                    lbOrgList.riskEventChart(riskEventX, riskEventY)
                }
            });
        },
        /**
         * 风险报警趋势
         */
        riskTrendChart: function (riskTrendDatas) {
            var option = {
                color: ['#79d5ff', '#807ae3', '#9ebffa', '#56a1d5', '#fadb71', '#FF8C00'],
                title: {
                    text: '风险报警趋势',
                    textStyle: {
                        fontSize: 16,
                        fontWeight: 'normal',
                        color: '#767676'
                    },
                    left: 'left',
                    top: '0px'
                },
                tooltip: {
                    trigger: 'axis',
                    formatter: function (params) {
                        var dom = chartDate + ' ' + params[0].name + '<br />';
                        var total = riskTrendDatas.total;
                        params.forEach(function (item) {
                            if (total[item.dataIndex] == 0) {
                                var rate = '0.00%';
                            } else {
                                var rate = parseFloat((item.value / total[item.dataIndex]) * 100).toFixed(2) + '%';
                            }

                            dom += '<span class="chart-dot" style="background: ' + item.color + '"></span>' + item.seriesName + ' ' + item.value + ' ' + rate + '<br />';
                        });
                        return dom;
                    },
                },
                legend: {
                    right: 0,
                    data: ['疑似疲劳', '碰撞危险', '违规异常', '注意力分散', '组合风险', '激烈驾驶']
                },
                grid: {
                    containLabel: true,
                    left: 20,
                    right: 30,
                    bottom: 10,
                },
                xAxis: {
                    type: 'category',
                    boundaryGap: false,
                    data: dayHour,
                },
                yAxis: {
                    // max:2000,
                    min: 0,
                    type: 'value',
                },
                series: [
                    {
                        name: '疑似疲劳',
                        type: 'line',
                        data: riskTrendDatas.tired,
                    },
                    {
                        name: '碰撞危险',
                        type: 'line',
                        data: riskTrendDatas.crash,
                    },
                    {
                        name: '违规异常',
                        type: 'line',
                        data: riskTrendDatas.exception,
                    },
                    {
                        name: '注意力分散',
                        type: 'line',
                        data: riskTrendDatas.distraction,
                    },
                    {
                        name: '组合风险',
                        type: 'line',
                        data: riskTrendDatas.cluster,
                    },
                    {
                        name: '激烈驾驶',
                        type: 'line',
                        data: riskTrendDatas.intenseDriving,
                    }
                ]
            };
            riskTrendChart = echarts.init(document.getElementById('riskTrendChart'));
            riskTrendChart.setOption(option, true);
        },
        riskTrendData: function () {
            var params = {
                groupId: groupId,
                isToday: isToday,
            };
            json_ajax("POST", "/clbs/adas/lbOrg/show/getRiskTypeTrend", "json", true, params, function (data) {
                if (data.success) {
                    var riskTrendDatas = null;//疑似疲劳

                    data = data.obj;

                    for (var i = 0; i < data.length; i++) {
                        var item = data[i];
                        var time = item.time.toString().substr(-2);

                        if (H.indexOf(time) >= 0) {
                            var inx = H.indexOf(time);
                            riskcluster[inx] = item.cluster;
                            riskcrash[inx] = item.crash;
                            riskdistraction[inx] = item.distraction;
                            riskexception[inx] = item.exception;
                            risktired[inx] = item.tired;
                            risktotal[inx] = item.total;
                            riskintenseDriving[inx] = item.intenseDriving;
                        }
                    }

                    riskTrendDatas = {
                        cluster: riskcluster,
                        crash: riskcrash,
                        distraction: riskdistraction,
                        exception: riskexception,
                        tired: risktired,
                        total: risktotal,
                        intenseDriving: riskintenseDriving,
                    };

                    lbOrgList.riskTrendChart(riskTrendDatas);
                }
            });
        },
        /**
         * 监控对象在线率、报警数分布情况及客服人员
         */
        riskDistributedChart: function (riskDistributedDatas) {
            var option = {
                color: ['#79d5ff', '#807ae3'],
                title: {
                    text: '监控对象在线率、报警数分布情况及客服人员',
                    textStyle: {
                        fontSize: 16,
                        fontWeight: 'normal',
                        color: '#767676',
                    },
                    left: 'left',
                    top: '0px'
                },
                grid: {
                    containLabel: true,
                    left: 0,
                    right: 0,
                    bottom: 10,
                },
                tooltip: {
                    trigger: 'axis',
                    formatter: function (params) {
                        var dom = chartDate + ' ' + params[0].name + '<br />';
                        var num = riskDistributedDatas.customerServiceTrend;//客服在线率
                        var num2 = riskDistributedDatas.vehOnlineTrend;//监控对象在线率

                        params.forEach(function (item) {
                            if (item.seriesName == '报警数') {
                                dom += '<span class="chart-dot" style="background: ' + item.color + '"></span>' + item.seriesName + ' ' + item.value + '<br />';
                            } else if (item.seriesName == '客服') {
                                dom += '<span class="chart-dot" style="background: ' + item.color + '"></span>' + item.seriesName + ' ' + num[item.dataIndex] + ' ' + item.value + '%<br />';
                            } else {
                                dom += '<span class="chart-dot" style="background: ' + item.color + '"></span>' + item.seriesName + ' ' + num2[item.dataIndex] + ' ' + item.value + '%<br />';
                            }
                        });
                        return dom;
                    },
                },
                legend: {
                    right: 0,
                    data: ['报警数', '监控对象在线率', '客服']
                },
                xAxis: {
                    type: 'category',
                    data: dayHour,
                    axisTick: {
                        alignWithLabel: true,
                    }
                },
                yAxis: [
                    {
                        type: 'value',
                        name: '',
                        min: 0,
                        max: 100,
                        interval: 25,
                        axisLabel: {
                            formatter: '{value}%'
                        },
                    },
                    {
                        type: 'value',
                        name: '',
                        min: 0,
                        axisLabel: {
                            formatter: '{value}'
                        },
                    }
                ],
                series: [
                    {
                        name: '监控对象在线率',
                        type: 'line',
                        yAxisIndex: 0,
                        data: riskDistributedDatas.vehOnlineTrendRate,
                    },
                    {
                        name: '客服',
                        type: 'line',
                        yAxisIndex: 0,
                        data: riskDistributedDatas.customerServiceRate,
                    },
                    {
                        name: '报警数',
                        type: 'bar',
                        // barWidth:150,
                        // barCategoryGap: '100%',
                        yAxisIndex: 1,
                        itemStyle: {
                            normal: {
                                color: '#198ef0'
                            }
                        },
                        label: {
                            normal: {
                                rotate: 45,
                                show: true,
                                distance: 15,
                                position: [0, -10],
                            }
                        },
                        data: riskDistributedDatas.eventTrend,
                    },
                ]
            };
            riskDistributedChart = echarts.init(document.getElementById('riskDistributedChart'));
            riskDistributedChart.setOption(option);
        },
        riskDistributedData: function () {
            var riskDistributedDatas = null;
            var params = {
                groupId: groupId,
                isToday: isToday,
            };
            json_ajax("POST", "/clbs/adas/lbOrg/show/getVehOnlineTrend", "json", false, params, function (data) {
                if (data.success) {
                    data = data.obj;

                    for (var i = 0; i < data.length; i++) {
                        var item = data[i];
                        var time = item.time.toString().substr(-2);

                        if (H.indexOf(time) >= 0) {
                            var inx = H.indexOf(time);
                            vehOnlineTrend[inx] = item.online;
                            vehOnlineTrendRate[inx] = item.rate;
                        }
                    }
                }
            });
            json_ajax("POST", "/clbs/adas/lbOrg/show/getEventTrend", "json", false, params, function (data) {
                if (data.success) {
                    data = data.obj;

                    for (var i = 0; i < data.length; i++) {
                        var item = data[i];
                        var time = item.time.toString().substr(-2);

                        if (H.indexOf(time) >= 0) {
                            var inx = H.indexOf(time);
                            eventTrend[inx] = item.total;
                        }
                    }
                }
            });

            var params2 = {
                isToday: isToday,
            };
            json_ajax("POST", "/clbs/adas/lbOrg/show/getCustomerServiceTrend", "json", false, params2, function (data) {
                if (data.success) {
                    data = data.obj;

                    for (var i = 0; i < data.length; i++) {
                        var item = data[i];
                        var time = item.time.toString().substr(-2);

                        if (H.indexOf(time) >= 0) {
                            var inx = H.indexOf(time);
                            customerServiceTrend[inx] = item.online;
                            customerServiceRate[inx] = item.rate;
                        }
                    }
                }
            });

            riskDistributedDatas = {
                vehOnlineTrend: vehOnlineTrend,//在线率
                eventTrend: eventTrend,//报警数
                customerServiceTrend: customerServiceTrend,//客服人员
                customerServiceRate: customerServiceRate,//客服在线率
                vehOnlineTrendRate: vehOnlineTrendRate,//监控对象在线率
            }
            lbOrgList.riskDistributedChart(riskDistributedDatas);
        },
        /**
         * 日期切换
         */
        changeDate: function () {
            var inx = $(this).index();
            var now = new Date();
            var year,
                month,
                day;

            $(this).addClass('btn-primary').siblings().removeClass('btn-primary');

            if (inx == 1) {
                now = new Date(now.getTime() - 86400000);
                year = now.getFullYear(),
                    month = (now.getMonth() + 1),
                    day = now.getDate();
                isToday = false;
            } else {
                year = now.getFullYear(),
                    month = (now.getMonth() + 1),
                    day = now.getDate();
                isToday = true;
            }

            chartDate = year + '-' + lbOrgList.formateDate(month) + '-' + lbOrgList.formateDate(day);
            lbOrgList.initData();
            lbOrgList.chartInit();
        },
        chartInit: function () {
            riskWarnNum = 0, riskLevelNum = 6, riskDealNum = 0;//重置

            lbOrgList.riskEventData();
            lbOrgList.riskTrendData();
            lbOrgList.riskDistributedData();
            lbOrgList.riskWarnData();
            lbOrgList.riskDealData();

            if (timer2) {
                clearInterval(timer2);
                timer2 = null;
            }
            timer2 = setInterval(function () {
                lbOrgList.riskEventData();
                lbOrgList.riskTrendData();
                lbOrgList.riskDistributedData();
                lbOrgList.riskWarnData();
                lbOrgList.riskDealData();
            }, 300000);

            lbOrgList.riskWarnChart();
            lbOrgList.riskLevelChart();
            lbOrgList.riskDealChart();
            if (timer) {
                clearInterval(timer);
                timer = null;
            }
            timer = setInterval(function () {
                lbOrgList.getRiskProportion();
                lbOrgList.getRisksDealInfo();
            }, 5000);
        },
        formateDate: function (value) {
            return value < 10 ? '0' + value : value;
        },
        /**
         * 模糊查询树
         */
        treeInit: function () {
            var treeSetting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {
                        "isOrg": "1"
                    },
                    dataFilter: lbOrgList.ajaxDataFilter
                },
                check: {
                    enable: true,
                    chkStyle: "radio",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    },
                    radioType: "all"
                },
                view: {
                    selectedMulti: false,
                    nameIsHTML: true,
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    onCheck: lbOrgList.zTreeOnCheck,
                    onAsyncSuccess: lbOrgList.zTreeOnAsyncSuccess
                }
            };

            lbOrgZTree = $.fn.zTree.init($("#treeDemo"), treeSetting, null);
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            if (responseData) {
                responseData.forEach(function (item) {
                    item.open = isAllExpand;
                });
            }
            return responseData;
        },
        zTreeOnAsyncSuccess: function () {
            var nodes = lbOrgZTree.getNodes();
            lbOrgZTree.checkNode(nodes[0], true, true);

            var checkGroupNode = lbOrgZTree.getCheckedNodes(true);
            if (isTreeInit && checkGroupNode && checkGroupNode.length > 0) {
                groupInput.val(checkGroupNode[0].name);
                initTreeName = checkGroupNode[0].name;
            }
            isTreeInit = false;
        },
        zTreeOnCheck: function (event, treeId, treeNode) {
            if (!treeNode.checked) {
                var treeObj = $.fn.zTree.getZTreeObj(treeId);
                treeObj.checkNode(treeNode, true, true);
                return;
            }
            groupInput.val(treeNode.name);
            groupId = treeNode.uuid;
            lbOrgList.initData();
            lbOrgList.chartInit();
        },
        /**
         * 图表resize
         */
        chartResize: function () {
            riskWarnChart.resize();
            riskLevelChart.resize();
            riskDealChart.resize();
            riskEventChart.resize();
            riskTrendChart.resize();
            riskDistributedChart.resize();
        },
        /**
         * 企业下拉
         * @param e
         */
        showMenu: function () {
            var menuContent = $('#menuContent');
            if (menuContent.is(":hidden")) {
                menuContent.slideDown("fast");
            } else {
                menuContent.is(":hidden");
            }
            $("body").bind("mousedown", lbOrgList.onBodyDown);
        },
        hideMenu: function () {
            var menuContent = $('#menuContent');
            menuContent.fadeOut("fast");
            $("body").unbind("mousedown", lbOrgList.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "menuContent" || $(
                event.target).parents("#menuContent").length > 0)) {
                lbOrgList.hideMenu();
            }
        },
    }

    $(function () {
        lbOrgList.initMap();
        lbOrgList.createMap() // 创建map集合
        mapWeather.getWeatherDatas();//天气初始化
        $('#toolClick').on('click', lbOrgList.toolClick);
        //显示设置
        $("#logoDisplay,#weather").on("click", lbOrgList.smoothMoveOrlogoDisplayClickFn);
        $("#magnifyClick, #shrinkClick, #countClick, #queryClick, #defaultMap, #realTimeRC, #btn-videoRealTime-show, #displayClick,#mapDropSetting").on("click", lbOrgList.toolClickList);
        //地图设置
        $("#realTimeRC,#defaultMap,#googleMap").on("click", lbOrgList.mapDropdownSettingClickFn);
        $('.table-left-arrow').on('click', lbOrgList.toggletableshow);
        // 获取地图上的聚合点信息
        lbOrgList.getVehiclesInfo();
        //提示语
        $('[data-toggle="tooltip"]').tooltip();

        // 获取地图统计数据
        lbOrgList.getNowRisk();
        lbOrgList.getOnlineInfo();
        setInterval(function () {
            lbOrgList.getOnlineInfo();
            lbOrgList.getNowRisk();
        }, 30000);
        lbOrgList.getYesterdayRisk();
        lbOrgList.getLineRate();
        lbOrgList.getRingRatioRiskEvent();

        //树节点
        lbOrgList.treeInit();
        groupInput.inputClear().on('onClearEvent', function () {
            groupId = '';
            lbOrgList.treeInit();
            // $('.btn.btn-primary').click();
        });
        $('#container').on('click', function (e) {
            var curId = $(e.target).attr('id');
            if (groupInput.val() == '' && curId != 'groupSelect') {
                groupInput.val(initTreeName);
            }
        });
        fuzzySearch('treeDemo', '#groupSelect', false, true); //初始化模糊搜索方法
        $("#groupSelectSpan,#groupSelect").bind("click", lbOrgList.showMenu);

        //菜单显示
        $('#toggle-left-button').on('click', function () {
            setTimeout(function () {
                lbOrgList.chartResize();
            }, 500)
        });
        $(window).resize(function () {
            lbOrgList.chartResize();
        })
        lbOrgList.intervalUpdateMarker();

        //日期切换(今日、昨日)
        $('.panel-tab .btn').bind('click', lbOrgList.changeDate);
        $('.panel-tab .btn').eq(0).trigger('click');
    })
})(window, $)