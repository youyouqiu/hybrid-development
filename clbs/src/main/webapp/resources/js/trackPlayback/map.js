var TrackMap = function (mapId, options, dependency) {
    this.dependency = dependency;
    this.isMapThreeDFlag = true;
    this.isTrafficDisplay = true;
    this.map = new AMap.Map(mapId, {
        resizeEnable: true,
        scrollWheel: true,
        zoom: 18
    });
    this.map.on('moveend', TrackMap.prototype.onMapAreaChange.bind(this));
    this.map.on('complete', TrackMap.prototype.onMapAreaChange.bind(this));
    this.map.on('mousemove', TrackMap.prototype.handleMapMouseMove.bind(this));
    this.map.on('click', function () {
        $('.menuContent').slideUp();
    });
    // 西南角和东北角的坐标
    this.paths = null;
    // 轨迹polyline
    this.line = null;
    // 播放过的轨迹
    this.passedLine = null;
    // 行驶段轨迹
    this.runLine = null;
    // 轨迹经纬度数据
    this.lineArr = null;
    // 监控对象
    this.marker = null;
    // 监控对象图标
    this.markerIcon = null;
    // 移动结束监听函数
    this.listenToMoveend = null;
    // 移动监听函数
    this.listenToMoving = null;
    // 监听点击监控对象图标
    this.listenToClick = null;
    // 停止点标注数组
    this.stopMarkers = null;
    // 不在停止段两端的停止点标注数组
    this.middleStopMarkers = null;
    // 基站定位标注数组
    this.locationMarkers = null;
    // 报警点标注数组
    this.alarmMarkers = null;
    // 行驶段 行驶里程 标注
    this.runMileMarker = null;
    // 详情弹窗
    this.infoWindow = null;
    // 报警详情弹窗
    this.alarmInfoWindow = null;
    // 停止详情弹窗
    this.stopInfoWindow = null;
    //实例化3D楼块图层
    this.buildings = new AMap.Buildings();
    // 边距，西南角和东北角
    this.bounds = null;
    //鼠标移入路径时最近的一个位置点图标
    this.closestMarker = null;
    //鼠标移入路径时最近的一个位置点时间
    this.closestLabel = null;
    //鼠标移入路径时最近的一个位置点索引
    this.closestIndex = null;
    // 在map中添加3D楼块图层
    // this.buildings.setMap(this.map);
    // this.map.add(this.buildings);
    this.map.getCity(function (result) {
        var html = '' + result.province + '<span class="caret"></span>';
        $("#placeChoose").html(html);
    });
    AMap.plugin(['AMap.ToolBar', 'AMap.Scale'], function () {
        this.map.addControl(new AMap.ToolBar({
            "direction": false,
        }));
        this.map.addControl(new AMap.Scale());
    }.bind(this));
    //卫星地图
    this.satellLayer = new AMap.TileLayer.Satellite();
    this.satellLayer.setMap(this.map);
    this.satellLayer.hide();
    // 路网
    // this.roadNet = new AMap.TileLayer.RoadNet();
    // this.roadNet.setMap(this.map);
    // this.roadNet.hide();
    //实时路况
    this.realTimeTraffic = new AMap.TileLayer.Traffic();
    this.realTimeTraffic.setMap(this.map);
    this.realTimeTraffic.hide();


    //起点
    this.markerStart = null;
    //终点
    this.markerEnd = null;

    /**
     * 定时定区域查询
     * */
    // 用于多区域绘制(区域1)
    this.mouseTool = new AMap.MouseTool(this.map);
    this.mouseTool.on("draw", TrackMap.prototype.createRangeSuccess.bind(this));
    // 用于多区域绘制(区域2)
    this.mouseTool1 = new AMap.MouseTool(this.map);
    this.mouseTool1.on("draw", TrackMap.prototype.createRangeSuccess.bind(this));
    this.RegionalQuerymarker1 = null;// 区域1上的marker
    this.RegionalQuerymarker2 = null;// 区域2上的marker
    this.areaTitle1 = null;// 区域1title
    this.areaTitle2 = null;// 区域2title

    // 绑定事件
    $(".amap-logo").attr("href", "javascript:void(0)").attr("target", "");
    $("#setMap").on("click", TrackMap.prototype.satelliteMapSwitching.bind(this));
    //谷歌地图
    $("#googleMap").on('click', TrackMap.prototype.showGoogleMapLayers.bind(this));
    //地图设置
    $("#mapDropSetting").on("click", TrackMap.prototype.showMapView.bind(this));
    $("#realTimeRC").on("click", TrackMap.prototype.realTimeRC.bind(this));
}

TrackMap.prototype.onMapAreaChange = function () {
    var bounds = this.map.getBounds();
    var southwest = bounds.getSouthWest();//获取西南角坐标
    var northeast = bounds.getNorthEast();//获取东北角坐标
    var possa = southwest.lat;//纬度（小）
    var possn = southwest.lng;
    var posna = northeast.lat;
    var posnn = northeast.lng;
    this.bounds = [
        [possn, possa], //西南角坐标
        [posnn, posna]//东北角坐标
    ];
};

/**
 * 围栏相关方法
 * */
//显示当前勾选对象围栏到地图
TrackMap.prototype.showZtreeCheckedToMap = function (treeNode, zTree) {
    //判断选中属性
    if (treeNode.checked == true) {
        //获取勾选状态被改变的节点集合
        var changeNodes = zTree.getChangeCheckedNodes();
        for (var i = 0, len = changeNodes.length; i < len; i++) {
            changeNodes[i].checkedOld = true;
        }
        for (var j = 0; j < changeNodes.length; j++) {
            var nodesId = changeNodes[j].id;
            this.showFenceInfo(nodesId, changeNodes[j]);
        }
    } else {
        var changeNodes = zTree.getChangeCheckedNodes();
        for (var i = 0, len = changeNodes.length; i < len; i++) {
            changeNodes[i].checkedOld = false;
            zTree.cancelSelectedNode(changeNodes[i]);
            var nodesId = changeNodes[i].id;
            this.hideFenceInfo(nodesId);
        }
    }
};
//显示行政区域
TrackMap.prototype.drawAdministrationToMap = function (data, aId, showMap) {
    var polygonAarry = [];
    var AdministrativeRegionsList = this.dependency.get('data').getAdministrativeRegionsList();
    if (AdministrativeRegionsList.containsKey(aId)) {
        var this_fence = AdministrativeRegionsList.get(aId);
        this.map.remove(this_fence);
        AdministrativeRegionsList.remove(aId);
    }
    for (var i = 0, l = data.length; i < 1; i++) {
        var polygon = new AMap.Polygon({
            map: this.map,
            strokeWeight: 1,
            strokeColor: '#CC66CC',
            fillColor: '#CCF3FF',
            fillOpacity: 0.5,
            path: data
        });
        polygonAarry.push(polygon);
    }
    AdministrativeRegionsList.put(aId, polygonAarry);
    this.map.setFitView(polygon);//地图自适应
    this.dependency.get('data').setAdministrativeRegionsList(AdministrativeRegionsList);
};
//标注
TrackMap.prototype.drawMarkToMap = function (mark, thisMap) {
    var markId = mark.id;
    var fenceIdList = this.dependency.get('data').getFenceIdList();
    //判断集合中是否含有指定的元素
    if (fenceIdList.containsKey(markId)) {
        var markerObj = fenceIdList.get(markId);
        thisMap.remove(markerObj);
        fenceIdList.remove(markId);
    }
    var dataArr = [];
    dataArr.push(mark.longitude);
    dataArr.push(mark.latitude);
    var polyFence = new AMap.Marker({
        position: dataArr,
        offset: new AMap.Pixel(-9, -23)
    });

    if (mark.markIcon == 1) {
        polyFence.setIcon('../../resources/img/circleIcon.png');
    }

    polyFence.setMap(thisMap);
    thisMap.setFitView(polyFence);
    fenceIdList.put(markId, polyFence);
    this.dependency.get('data').setFenceIdList(fenceIdList);
};
//矩形
TrackMap.prototype.drawRectangleToMap = function (rectangle, thisMap) {
    var rectangleId = rectangle.id;
    var fenceIdList = this.dependency.get('data').getFenceIdList();
    if (fenceIdList.containsKey(rectangleId)) {
        var thisFence = fenceIdList.get(rectangleId);
        thisFence.show();
        this.map.setFitView(thisFence);
    }
    else {
        var dataArr = new Array();
        if (rectangle != null) {
            dataArr.push([rectangle.leftLongitude, rectangle.leftLatitude]); // 左上角
            dataArr.push([rectangle.rightLongitude, rectangle.leftLatitude]); // 右上角
            dataArr.push([rectangle.rightLongitude, rectangle.rightLatitude]); // 右下角
            dataArr.push([rectangle.leftLongitude, rectangle.rightLatitude]); // 左下角
        }
        var polyFence = new AMap.Polygon({
            path: dataArr,//设置多边形边界路径
            strokeColor: "#FF33FF", //线颜色
            strokeOpacity: 0.2, //线透明度
            strokeWeight: 3, //线宽
            fillColor: "#1791fc", //填充色
            fillOpacity: 0.35
            //填充透明度
        });
        polyFence.setMap(thisMap);
        thisMap.setFitView(polyFence);
        fenceIdList.put(rectangleId, polyFence);
        this.dependency.get('data').setFenceIdList(fenceIdList);
    }
};
//多边形
TrackMap.prototype.drawPolygonToMap = function (polygon, thisMap) {
    var polygonId = polygon[0].polygonId;
    var fenceIdList = this.dependency.get('data').getFenceIdList();
    if (fenceIdList.containsKey(polygonId)) {
        var thisFence = fenceIdList.get(polygonId);
        thisFence.hide();
        fenceIdList.remove(polygonId);
    }
    var dataArr = new Array();
    if (polygon != null && polygon.length > 0) {
        for (var i = 0; i < polygon.length; i++) {
            dataArr.push([polygon[i].longitude, polygon[i].latitude]);
        }
    }
    var polyFence = new AMap.Polygon({
        path: dataArr,//设置多边形边界路径
        strokeColor: "#FF33FF", //线颜色
        strokeOpacity: 0.2, //线透明度
        strokeWeight: 3, //线宽
        fillColor: "#1791fc", //填充色
        fillOpacity: 0.35
        //填充透明度
    });
    polyFence.setMap(thisMap);
    thisMap.setFitView(polyFence);
    fenceIdList.put(polygonId, polyFence);
    this.dependency.get('data').setFenceIdList(fenceIdList);
};
//圆形
TrackMap.prototype.drawCircleToMap = function (circle, thisMap) {
    var circleId = circle.id;
    var fenceIdList = this.dependency.get('data').getFenceIdList();
    if (fenceIdList.containsKey(circleId)) {
        var thisFence = fenceIdList.get(circleId);
        thisFence.hide();
        fenceIdList.remove(circleId);
    }
    var polyFence = new AMap.Circle({
        center: new AMap.LngLat(circle.longitude, circle.latitude),// 圆心位置
        radius: circle.radius, //半径
        strokeColor: "#F33", //线颜色
        strokeOpacity: 1, //线透明度
        strokeWeight: 3, //线粗细度
        fillColor: "#ee2200", //填充颜色
        fillOpacity: 0.35
        //填充透明度
    });
    polyFence.setMap(thisMap);
    thisMap.setFitView(polyFence);
    fenceIdList.put(circleId, polyFence);
    this.dependency.get('data').setFenceIdList(fenceIdList);
};
//行驶路线
TrackMap.prototype.drawTravelLineToMap = function (data, thisMap, travelLine, wayPointArray) {
    var lineID = travelLine.id;
    var path = [];
    var start_point_value = [travelLine.startlongtitude, travelLine.startLatitude];
    var end_point_value = [travelLine.endlongtitude, travelLine.endLatitude];
    var wayValue = [];
    if (wayPointArray != undefined) {
        for (var j = 0, len = wayPointArray.length; j < len; j++) {
            wayValue.push([wayPointArray[j].longtitude, wayPointArray[j].latitude]);
        }
    }
    for (var i = 0, len = data.length; i < len; i++) {
        path.push([data[i].longitude, data[i].latitude]);
    }
    var travelLineList = this.dependency.get('data').getTravelLineList();
    if (travelLineList.containsKey(lineID)) {
        var this_line = travelLineList.get(lineID);
        this.map.remove([this_line]);
        travelLineList.remove(lineID);
    }
    var polyFencec = new AMap.Polyline({
        path: path, //设置线覆盖物路径
        strokeColor: "#3366FF", //线颜色
        strokeOpacity: 1, //线透明度
        strokeWeight: 5, //线宽
        strokeStyle: "solid", //线样式
        strokeDasharray: [10, 5],
        zIndex: 51
    });
    polyFencec.setMap(this.map);
    this.map.setFitView(polyFencec);
    travelLineList.put(lineID, polyFencec);
    this.dependency.get('data').setTravelLineList(travelLineList);
};
//线
TrackMap.prototype.drawLineToMap = function (line, lineSpot, lineSegment, thisMap) {
    var lineId = line[0].lineId;
    //是否存在线
    var fenceIdList = this.dependency.get('data').getFenceIdList();
    if (fenceIdList.containsKey(lineId)) {
        var thisFence = fenceIdList.get(lineId);
        if (Array.isArray(thisFence)) {
            for (var i = 0; i < thisFence.length; i++) {
                thisFence[i].hide();
            }
        } else {
            thisFence.hide();
        }
        fenceIdList.remove(lineId);
    }
    //线数据
    var dataArr = new Array();
    var lineSectionArray = [];
    if (line != null && line.length > 0) {
        for (var i in line) {
            if (line[i].type == "0") {
                dataArr[i] = [line[i].longitude, line[i].latitude];
            }
        }
    }
    //地图画线
    var polyFencec = new AMap.Polyline({
        path: dataArr, //设置线覆盖物路径
        strokeColor: "#3366FF", //线颜色
        strokeOpacity: 1, //线透明度
        strokeWeight: 5, //线宽
        strokeStyle: "solid", //线样式
        strokeDasharray: [10, 5],
        zIndex: 51
        //补充线样式
    });
    lineSectionArray.push(polyFencec);
    fenceIdList.put(lineId, polyFencec);
    polyFencec.setMap(thisMap);
    thisMap.setFitView(polyFencec);
    this.dependency.get('data').setFenceIdList(fenceIdList);
};
//围栏隐藏
TrackMap.prototype.hideFenceInfo = function (nodesId) {
    var fenceIdList = this.dependency.get('data').getFenceIdList();
    if (fenceIdList.containsKey(nodesId)) {
        var thisFence = fenceIdList.get(nodesId);
        if (Array.isArray(thisFence)) {
            for (var i = 0; i < thisFence.length; i++) {
                thisFence[i].hide();
            }
        } else {
            thisFence.hide();
        }
    }
    this.hideRegionsOrTravel(nodesId);
};
//隐藏行政区划及行驶路线
TrackMap.prototype.hideRegionsOrTravel = function (id) {
    //行政区划
    var AdministrativeRegionsList = this.dependency.get('data').getAdministrativeRegionsList();
    if (AdministrativeRegionsList.containsKey(id)) {
        var this_fence = AdministrativeRegionsList.get(id);
        this.map.remove(this_fence);
        AdministrativeRegionsList.remove(id);
        this.dependency.get('data').setAdministrativeRegionsList(AdministrativeRegionsList);
    }
    //行驶路线
    var travelLineList = this.dependency.get('data').getTravelLineList();
    if (travelLineList.containsKey(id)) {
        var this_fence = travelLineList.get(id);
        this.map.remove(this_fence);
        travelLineList.remove(id);
    }
    this.dependency.get('data').setTravelLineList(travelLineList);
};
//围栏显示
TrackMap.prototype.showFenceInfo = function (nodesId, node) {
    //判断集合中是否含有指定的元素
    var fenceIdList = this.dependency.get('data').getFenceIdList();
    if (fenceIdList.containsKey(nodesId)) {
        var thisFence = fenceIdList.get(nodesId);
        if (thisFence != undefined) {
            if (Array.isArray(thisFence)) {
                for (var s = 0; s < thisFence.length; s++) {
                    thisFence[s].show();
                    this.map.setFitView(thisFence[s]);
                }
            } else {
                thisFence.show();
                this.map.setFitView(thisFence);
            }
        }
    } else {
        var fenceTree = this.dependency.get('fenceTree');
        fenceTree.getFenceDetailInfo([node], this.map);
    }
};
//围栏集合数据清除及切换后初始化
TrackMap.prototype.delFenceListAndMapClear = function () {
    //清除根据监控对象查询的围栏勾选
    var zTree = $.fn.zTree.getZTreeObj("vFenceTree");
    var fenceIdList = this.dependency.get('data').getFenceIdList();
    //处理判断不勾选围栏直接切换至电子围栏后错误问题
    if (zTree != null) {
        var nodes = zTree.getCheckedNodes(true);
        //获取已勾选的节点结合  变换为不勾选
        for (var i = 0, l = nodes.length; i < l; i++) {
            zTree.checkNode(nodes[i], false, false);
        }
        //改变勾选状态checkedOld
        var allNodes = zTree.getChangeCheckedNodes();
        for (var i = 0; i < allNodes.length; i++) {
            allNodes[i].checkedOld = false;
        }
        //删除 标注、线、矩形、圆形、多边形 （集合fenceIdList）
        if (fenceIdList.elements.length > 0) {
            var fLength = fenceIdList.elements.length;
            //遍历当前勾选围栏
            for (var i = 0; i < fLength; i++) {
                //获取围栏Id
                var felId = fenceIdList.elements[i].key;
                //隐藏围栏及删除数组数据
                var felGs = fenceIdList.get(felId);
                //AMap.Marker标注    AMap.Polyline线    AMap.Polygon矩形   AMap.Circle圆形
                var nameArr = ["AMap.Marker", "AMap.Polyline", "AMap.Polygon", "AMap.Circle", "Overlay.Marker", "Overlay.Polyline", "Overlay.Polygon", "Overlay.Circle"];
                if (nameArr.indexOf(felGs.CLASS_NAME) !== -1) {
                    felGs.hide();
                }
            }
            //清空数组
            fenceIdList.clear();
        }
        //删除行政区域 （集合AdministrativeRegionsList）
        var AdministrativeRegionsList = this.dependency.get('data').getAdministrativeRegionsList();
        if (AdministrativeRegionsList.elements.length > 0) {
            var aLength = AdministrativeRegionsList.elements.length;
            for (var i = 0; i < aLength; i++) {
                var admId = AdministrativeRegionsList.elements[i].key;
                var admGs = AdministrativeRegionsList.get(admId);
                this.map.remove(admGs);
            }
            AdministrativeRegionsList.clear();
            this.dependency.get('data').setAdministrativeRegionsList(AdministrativeRegionsList);
        }
        //删除导航路线 （集合travelLineList）
        var travelLineList = this.dependency.get('data').getTravelLineList();
        if (travelLineList.elements.length > 0) {
            var tLength = travelLineList.elements.length;
            for (var i = 0; i < tLength; i++) {
                var travelId = travelLineList.elements[i].key;
                var travelGs = travelLineList.get(travelId);
                this.map.remove([travelGs]);
            }
            travelLineList.clear();
        }
        this.dependency.get('data').setFenceIdList(fenceIdList);
        this.dependency.get('data').getTravelLineList(travelLineList);
    }
};

/**
 * 定时定区域查询相关方法
 * */
//区域画完回调函数
TrackMap.prototype.createRangeSuccess = function (data) {
    var southWest = data.obj.getBounds().getSouthWest();
    var northEast = data.obj.getBounds().getNorthEast();
    if (southWest.getLng() === northEast.getLng() && southWest.getLat() === northEast.getLat()) {
        layer.msg('所绘区域过小,请重新绘制');
        return;
    }

    var rangeAreaPos = this.dependency.get('data').getRangeAreaPos();
    var areaIndex = this.dependency.get('data').getAreaIndex();
    rangeAreaPos[areaIndex] = data.obj.getBounds();
    this.dependency.get('data').setRangeAreaPos(rangeAreaPos);
    var curInput = $('.areaInput').eq(areaIndex);
    curInput.val('区域' + parseInt(areaIndex + 1));
    curInput.after('<i class="delArea"></i>');
    $('.delArea').on('click', this.removeArea.bind(this));


    var rightFloorlongtitude = data.obj.getBounds().getNorthEast().getLng();
    var rightFloorLatitude = data.obj.getBounds().getNorthEast().getLat();
    if (areaIndex == '0') {// 区域1
        this.areaTitle1 = new AMap.Marker({
            map: this.map,
            position: [rightFloorlongtitude, rightFloorLatitude],//基点位置
            offset: new AMap.Pixel(-47, 1), //相对于基点的位置
            content: '<div class="areaTitle">区域1</div>',
            zIndex: 99998,
            autoRotation: true
        });
        this.mouseTool.close(false);
        this.mouseTool1.close(false);
        $('#area-error').remove();
    } else {// 区域2
        this.areaTitle2 = new AMap.Marker({
            map: this.map,
            position: [rightFloorlongtitude, rightFloorLatitude],//基点位置
            offset: new AMap.Pixel(-47, 1), //相对于基点的位置
            content: '<div class="areaTitle">区域2</div>',
            zIndex: 99998,
            autoRotation: true
        });
        this.mouseTool.close(false);
        this.mouseTool1.close(false);
        $('#areaTwo-error').remove();
    }
};

// 多时段多区域查询结果数据组装
TrackMap.prototype.areaTableInit = function (data) {
    var html = '';
    var isHasCar = false;
    var rangeAreaPos = this.dependency.get('data').getRangeAreaPos();
    if (data.success) {
        if (data.obj != null) {
            isHasCar = true;
            var areaOne = data.obj.areaOne;
            var areaTwo = data.obj.areaTwo;
            var vIdsOne = [], vIdsTwo = [];
            var turnoverId = "turnoverId";
            if (areaOne) {
                var sum = 0;
                for (i = 0; i < areaOne.length; i++) {
                    var carVehicleId = areaOne[i].vehicleIdStr;
                    if (vIdsOne.indexOf(carVehicleId) == -1) {
                        sum++;
                        vIdsOne.push(carVehicleId)
                    }
                    turnoverId = turnoverId + "_" + i;
                    var carName = areaOne[i].monitorNumber;
                    var time = areaOne[i].startTime + '--' + areaOne[i].endTime;
                    html += '<tr data-id="' + carVehicleId + '" class="fenceTurnoverTime areaTrOne" data-index="0" data-time="' + time + '">' +
                        '<td><div class="imgBox"><img class="dropIcon" src="/clbs/resources/img/dropDown.svg">' + carName + '</div></td>' +
                        '<td>' + time + '</td>' +
                        '<td>区域1</td>' +
                        '</tr><tr><td class="areaSearchTable" colspan="3">' +
                        '<table class="table table-striped table-bordered" cellspacing="0" width="100%"><thead>' +
                        '<tr><th>序号</th><th>进区域时间</th><th>出区域时间</th></tr></thead>' +
                        '<tbody id="' + turnoverId + '"></tbody></table></td>' +
                        '</tr>';
                }
                if (this.RegionalQuerymarker1) this.map.remove(this.RegionalQuerymarker1);
                if (rangeAreaPos[0]) {
                    this.RegionalQuerymarker1 = new AMap.Marker({
                        map: this.map,
                        position: rangeAreaPos[0].getCenter(),//基点位置
                        offset: new AMap.Pixel(-26, -13), //相对于基点的位置
                        content: '<div class="marker-route marker-marker-bus-from">' + sum + '</div>',
                        zIndex: 99999,
                        autoRotation: true
                    });
                    this.RegionalQuerymarker1.on('click', function () {
                        $('#areaSearchCar .areaSearchTable').hide();
                        $('#areaSearchCar .areaTrOne').show();
                        $('#areaSearchCar .areaTrTwo').hide();
                        $('#areaSearchCar .modal-body').scrollTop(0);
                        $("#areaSearchCar").modal('show');
                    });
                }
            }
            if (areaTwo) {
                var sum = 0;
                for (i = 0; i < areaTwo.length; i++) {
                    var carVehicleId = areaTwo[i].vehicleIdStr;
                    if (vIdsTwo.indexOf(carVehicleId) == -1) {
                        sum++;
                        vIdsTwo.push(carVehicleId)
                    }
                    turnoverId = turnoverId + "_" + i;
                    var carName = areaTwo[i].monitorNumber;
                    var time = areaTwo[i].startTime + '--' + areaTwo[i].endTime;
                    html += '<tr data-id="' + carVehicleId + '" class="fenceTurnoverTime areaTrTwo" data-index="1" data-time="' + time + '">' +
                        '<td><div class="imgBox"><img class="dropIcon" src="/clbs/resources/img/dropDown.svg">' + carName + '</div></td>' +
                        '<td>' + time + '</td>' +
                        '<td>区域2</td>' +
                        '</tr><tr><td class="areaSearchTable" colspan="3">' +
                        '<table class="table table-striped table-bordered" cellspacing="0" width="100%"><thead>' +
                        '<tr><th>序号</th><th>进区域时间</th><th>出区域时间</th></tr></thead>' +
                        '<tbody id="' + turnoverId + '"></tbody></table></td>' +
                        '</tr>';
                }
                if (this.RegionalQuerymarker2) this.map.remove(this.RegionalQuerymarker2);
                if (rangeAreaPos[1]) {
                    this.RegionalQuerymarker2 = new AMap.Marker({
                        map: this.map,
                        position: rangeAreaPos[1].getCenter(),//基点位置
                        offset: new AMap.Pixel(-26, -13), //相对于基点的位置
                        content: '<div class="marker-route marker-marker-bus-from">' + sum + '</div>',
                        zIndex: 99999,
                        autoRotation: true
                    });
                    this.RegionalQuerymarker2.on('click', function () {
                        $('#areaSearchCar .areaSearchTable').hide();
                        $('#areaSearchCar .areaTrOne').hide();
                        $('#areaSearchCar .areaTrTwo').show();
                        $('#areaSearchCar .modal-body').scrollTop(0);
                        $("#areaSearchCar").modal('show');
                    });
                }
            }
        }
        $("#dataTable tbody.monitoringObj").html(html);
        var _this = this;
        $(".fenceTurnoverTime").unbind("click").bind("click", function () {
            _this.dependency.get('fixedTimeArea').fenceTurnoverTime(this, _this);
        });
        if (isHasCar) {
            $("#areaSearchCar").modal('show');
        } else {
            layer.msg(trackAreaMonitorNull);
        }
    } else if (data.msg) {
        layer.msg(data.msg);
    }
    layer.closeAll('loading');
};

// 删除区域(点击删除按钮)
TrackMap.prototype.delArea = function (e) {
    var rangeAreaPos = this.dependency.get('data').getRangeAreaPos();
    rangeAreaPos[1] = null;
    this.dependency.get('data').setRangeAreaPos(rangeAreaPos);
    if (this.areaTitle2) {
        this.map.remove(this.areaTitle2);
        if (this.RegionalQuerymarker2) {
            this.map.remove(this.RegionalQuerymarker2);
            this.RegionalQuerymarker2 = null;
        }
        this.mouseTool1.close(true);
        this.areaTitle2 = null;
    }
    var timeGroup = $(e.target).closest('.form-group');
    timeGroup.remove();
};

// 删除区域(点击删除图标)
TrackMap.prototype.removeArea = function (e) {
    e.stopPropagation();
    var curInput = $(e.target).siblings('.areaInput');
    curInput.val('');
    var curIndex = curInput.index('.areaInput');
    var rangeAreaPos = this.dependency.get('data').getRangeAreaPos();
    rangeAreaPos[curIndex] = null;
    this.dependency.get('data').setRangeAreaPos(rangeAreaPos);
    if (curIndex == '0' && this.areaTitle1) {// 区域1
        this.map.remove(this.areaTitle1);
        this.areaTitle1 = null;
        if (this.RegionalQuerymarker1) {
            this.map.remove(this.RegionalQuerymarker1);
            this.RegionalQuerymarker1 = null;
        }
        this.mouseTool.close(true);
    } else if (curIndex == '1' && this.areaTitle2) {// 区域2
        this.map.remove(this.areaTitle2);
        this.areaTitle2 = null;
        if (this.RegionalQuerymarker2) {
            this.map.remove(this.RegionalQuerymarker2);
            this.RegionalQuerymarker2 = null;
        }
        this.mouseTool1.close(true);
    }
    $(e.target).remove();
};

TrackMap.prototype.toolClickList = function (e) {
    this.mouseTool.close(false);
    this.mouseTool1.close(false);
    var areaIndex = $(e.target).index('.areaInput');
    var rangeAreaPos = this.dependency.get('data').getRangeAreaPos();
    this.dependency.get('data').setAreaIndex(areaIndex);
    if (rangeAreaPos[areaIndex]) {
        var center = rangeAreaPos[areaIndex].getCenter();
        this.map.setZoomAndCenter(18, center);
    } else if (areaIndex == '0') {
        this.mouseTool.rectangle();
    } else {
        this.mouseTool1.rectangle();
    }
};
// 清除地图上定时定区域查询的相关信息
TrackMap.prototype.areaDestory = function () {
    if (this.RegionalQuerymarker1) {
        this.map.remove(this.RegionalQuerymarker1);
        this.RegionalQuerymarker1 = null;
    }
    if (this.areaTitle1) {
        this.map.remove(this.areaTitle1);
        this.areaTitle1 = null;
    }
    if (this.RegionalQuerymarker2) {
        this.map.remove(this.RegionalQuerymarker2);
        this.RegionalQuerymarker2 = null;
    }
    if (this.areaTitle2) {
        this.map.remove(this.areaTitle2);
        this.areaTitle2 = null;
    }
    this.mouseTool.close(true);
    this.mouseTool1.close(true);
    this.dependency.get('data').setRangeAreaPos([]);
};

// <editor-fold desc="地图设置，卫星地图，谷歌地图">
//实时路况切换
TrackMap.prototype.realTimeRC = function () {
    if (this.isTrafficDisplay) {
        this.realTimeTraffic.show();
        $("#realTimeRC").addClass("map-active");
        $("#realTimeRCLab").addClass('preBlue');
        this.isTrafficDisplay = false;
        if (this.googleMapLayer) {
            this.googleMapLayer.setMap(null);
        }
        $("#googleMap").attr("checked", false);
        $("#googleMapLab").removeClass("preBlue");
    } else {
        this.realTimeTraffic.hide();
        $("#realTimeRC").removeClass("map-active");
        $("#realTimeRCLab").removeClass('preBlue');
        this.isTrafficDisplay = true;
    }
},

//卫星地图及3D地图切换
    TrackMap.prototype.satelliteMapSwitching = function () {
        if (this.isMapThreeDFlag) {
            $("#setMap").addClass("map-active");
            $("#defaultMapLab").addClass('preBlue');
            // this.satellLayer.setMap(this.map);
            this.satellLayer.show();
            // this.roadNet.show();
            this.buildings.setMap(null);
            this.isMapThreeDFlag = false;
            if (this.googleMapLayer) {
                this.googleMapLayer.setMap(null);
            }
            $("#googleMap").attr("checked", false);
            $("#googleMapLab").removeClass("preBlue");
        } else {
            $("#setMap").removeClass("map-active");
            $("#defaultMapLab").removeClass('preBlue');
            // this.buildings.setMap(this.map);
            // this.satellLayer.setMap(null);
            this.satellLayer.hide();
            // this.roadNet.hide();
            this.isMapThreeDFlag = true;
        }
    },
//GOOGLE地图
    TrackMap.prototype.showGoogleMapLayers = function () {
        if ($("#googleMap").attr("checked")) {
            this.googleMapLayer.setMap(null);
            $("#googleMap").attr("checked", false);
            $("#googleMapLab").removeClass("preBlue");
        } else {
            this.googleMapLayer = new AMap.TileLayer({
                tileUrl: 'http://mt{1,2,3,0}.google.cn/vt/lyrs=m@142&hl=zh-CN&gl=cn&x=[x]&y=[y]&z=[z]&s=Galil', // 图块取图地址
                zIndex: 100 //设置Google层级与高德相同  避免高德路况及卫星被Google图层覆盖
            });
            this.googleMapLayer.setMap(this.map);
            $("#googleMap").attr("checked", true);
            $("#googleMapLab").addClass("preBlue");

            //取消路况与卫星选中状态
            $("#realTimeRC").attr("checked", false);
            $("#realTimeRCLab").removeClass("preBlue");
            this.realTimeTraffic.hide();
            this.isTrafficDisplay = true;
            this.isMapThreeDFlag = true;
            $("#setMap").attr("checked", false);
            $("#defaultMapLab").removeClass("preBlue");
            this.satellLayer.hide();
            // this.roadNet.hide();
            // this.buildings.setMap(this.map);
        }
    }
TrackMap.prototype.showMapView = function () {
    if (!($("#mapDropSettingMenu").is(":hidden"))) {
        $("#mapDropSettingMenu").slideUp();
    } else {
        $("#mapDropSettingMenu").slideDown();
    }
}
// </editor-fold>

TrackMap.prototype.initColorSetting = function (cb) {
    var dataDependency = this.dependency.get('data');
    $.ajax({
        type: "POST",
        url: "/clbs/core/uum/custom/findCustomColumnInfoByMark",
        data: {marks: 'TRACKPLAY_SPEED'},
        dataType: "json",
        async: true,
        success: function (data) {
            if (data.success) {
                // 在添加了线条宽度后，colors变成一个三位数组，前两位是颜色，第三位是宽度
                var colors = data.obj.TRACKPLAY_SPEED.map(function (x) {
                    return parseInt(x.initValue);
                });
                var onlyColors = colors.slice(0, 2);
                var lineSize = colors[2];

                if (lineSize) {
                    dataDependency.setTrackWidth(lineSize);
                }
                dataDependency.setTrackColor(onlyColors);
                if (typeof cb === 'function') {
                    cb();
                }
            }
        }
    });
}

TrackMap.prototype.getParaColor = function (positions, speeds, trackWidth) {
    var newspeeds = speeds || [];
    var lowColor = '#ffd045';
    var normalColor = '#00cc00';
    var highColor = '#960ba3';
    // 考虑到轨迹特别细的时候，无法跟地图原有的道路区分开来，故加深颜色
    if (trackWidth <= 4) {
        lowColor = '#fff90c';
        normalColor = '#008700';
        highColor = '#860A92';
    }
    var getType = function (speed) {
        if (speed === undefined) {
            return '';
        }
        speed = parseInt(speed);
        if (speed >= newspeeds[1]) {
            return 'high';
        } else if (speed >= newspeeds[0] && speed < newspeeds[1]) {
            return 'normal';
        }
        return 'low';

    }

    var padEndData = positions.concat([{
        x: Math.random()
    }]);
    var padEndDataLength = padEndData.length;
    var typeDict = {};

    if (padEndDataLength > 0) {
        var prevType = getType(padEndData[0].speed);
        var prevIndex = 0;

        for (var i = 0; i < padEndDataLength; i += 1) {
            var element = padEndData[i];
            var currentType = getType(element.speed);
            if (currentType !== prevType) {
                var item = typeDict[prevType];
                var segment = {
                    startIndex: prevIndex,
                    endIndex: i,
                };
                if (item) {
                    item.occurrence += 1;
                    item.segment.push(segment);
                } else {
                    typeDict[prevType] = {
                        type: prevType,
                        occurrence: 1,
                        segment: [segment],
                    };
                }
                prevType = currentType;
                prevIndex = i - 1; // 通过设置i-1，将后一段的起点连接到上一段的终点
            }
        }
    }
    if (typeDict.high) {
        typeDict.high.color = highColor;
    }
    if (typeDict.normal) {
        typeDict.normal.color = normalColor;
    }
    if (typeDict.low) {
        typeDict.low.color = lowColor;
    }
    return typeDict;
}

TrackMap.prototype.getNearDistance = function () {
    var zoom = this.map.getZoom();
    var mouseToLineDistance;
    if (zoom < 5) {
        mouseToLineDistance = (30 - zoom) * 400;
    } else if (zoom < 8) {
        mouseToLineDistance = (30 - zoom) * 300;
    } else if (zoom < 11) {
        mouseToLineDistance = (30 - zoom) * 150;
    } else if (zoom < 14) {
        mouseToLineDistance = (30 - zoom) * 30;
    } else if (zoom < 15) {
        mouseToLineDistance = (30 - zoom) * 20;
    } else if (zoom < 17) {
        mouseToLineDistance = (30 - zoom) * 6;
    } else {
        mouseToLineDistance = (30 - zoom) * 2;
    }
    return mouseToLineDistance * 2;
}

TrackMap.prototype.handleMapMouseMove = function (e) {
    var lnglat = e.lnglat;
    var cloesest = null, index = null;
    if (!this.lineArr) {
        return;
    }
    for (var i = 0, len = this.lineArr.length; i < len; i++) {
        var item = this.lineArr[i];
        if (this.isOutOfBounds(item)) {
            continue;
        }
        var distance = lnglat.distance(item);
        if (cloesest === null || cloesest > distance) {
            cloesest = distance;
            index = i;
        }
    }

    var nearDistance = this.getNearDistance();
    if (cloesest !== null && cloesest < nearDistance) {
        if (this.closestIndex != index) {
            if (this.closestMarker) {
                this.map.remove(this.closestMarker);
                this.map.remove(this.closestLabel);
            }
            this.closestMarker = new AMap.Marker({
                map: this.map,
                position: this.lineArr[index],
                offset: new AMap.Pixel(-10, -10), //相对于基点的位置
                icon: new AMap.Icon({
                    size: new AMap.Size(20, 20), //图标大小
                    image: "../../resources/img/circle.svg",
                    imageOffset: new AMap.Pixel(0, 0)
                }),
                extData: index,
                zIndex: 70
            });
            this.closestMarker.on('click', function (ee) {
                var markerIndex = ee.target.getExtData();
                var dataDependency = this.dependency.get('data');
                if (this.marker) {
                    this.marker.stopMove();
                }
                dataDependency.setIsPlaying(false);
                dataDependency.setPlayIndex(markerIndex);
            }.bind(this))
            var positions = this.dependency.get('data').getPositions();
            this.closestLabel = new AMap.Text({
                map: this.map,
                position: this.lineArr[index],
                offset: new AMap.Pixel(-20, -25), //相对于基点的位置
                text: TrackPlaybackColumn.monthTimeRender(positions[index].vtime * 1000),
                anchor: 'top-right',
                zIndex: 99998
            });
            this.closestIndex = index;
        }
    } else if (this.closestMarker) {
        this.map.remove(this.closestMarker);
        this.map.remove(this.closestLabel);
        this.closestMarker = null;
        this.closestIndex = null;
    }
}

TrackMap.prototype.handleLineMouseClick = function (lnglat) {

}


/**
 * 删除地图上所有的覆盖物
 */
TrackMap.prototype.clearMap = function () {
    // this.map.clearMap();
    if (this.marker != null) {
        this.map.remove(this.marker);
    }

    if (this.markerEnd != null) {
        this.map.remove(this.markerEnd)
    }

    if (this.markerStart != null) {
        this.map.remove(this.markerStart)
    }

    if (this.infoWindow != null) {
        this.infoWindow.close();
    }

    if (this.passedLine) {
        // 清空播放过的线段
        this.map.remove(this.passedLine);
    }

    // 清空之前的线段
    if (this.line && this.line.length > 0) {
        for (var i = 0; i < this.line.length; i++) {
            this.map.remove(this.line[i]);
        }
    }
    this.lineArr = null;
    this.marker = null;
    this.listenToMoveend = null;
    this.listenToMoving = null;
    this.listenToClick = null;
    this.stopMarkers = null;
    this.middleStopMarkers = null;
    this.locationMarkers = null;
    this.alarmMarkers = null;
    this.infoWindow = null;
}

TrackMap.prototype.drawLine = function (isChartOpen, type = false) {
    this.map.clearMap();
    var self = this;
    var data = this.dependency.get('data').getPositions() || [];
    var newData = this.bwLongitudeAndLatitude(data);
    var positions = [];
    if (type) {
        // positions = newData.filter(item => item.longtitude !== '0.0' && item.latitude !== '0.0');
        positions = newData;
    } else {
        positions = newData.filter(item => item.stationEnabled === false);
    }
    var speeds = this.dependency.get('data').getTrackColor();
    var trackWidth = this.dependency.get('data').getTrackWidth();
    if (positions === null || positions.length === 0) {
        return;
    }
    this.lineArr = this.formateLngLatFromPosition(positions);
    var colors = this.getParaColor(positions, speeds, trackWidth);
    var colorKeys = Object.keys(colors);
    var linePath = [];
    for (var i = 0; i < colorKeys.length; i++) {
        var colorItem = colors[colorKeys[i]];
        for (var j = 0; j < colorItem.segment.length; j++) {
            var lineSegment = new AMap.Polyline({
                map: this.map,
                path: this.lineArr.slice(colorItem.segment[j].startIndex, colorItem.segment[j].endIndex),
                strokeColor: colorItem.color, //线颜色
                strokeOpacity: 0.9, //线透明度
                strokeWeight: trackWidth, //线宽
                strokeStyle: "solid", //线样式
                zIndex: 50,
                showDir: true
            });
            linePath.push(lineSegment);
        }
    }
    this.line = linePath;
    this.passedLine = new AMap.Polyline({
        map: this.map,
        strokeColor: "#000", //线颜色
        strokeOpacity: 0.5, //线透明度
        strokeWeight: trackWidth, //线宽
        strokeStyle: "solid", //线样式
        zIndex: 60
    });
    //创建起点和终点图标
    this.markerStart = new AMap.Marker({
        // map: this.map,
        position: this.lineArr[0],
        offset: new AMap.Pixel(-12, -40), //相对于基点的位置
        icon: new AMap.Icon({
            size: new AMap.Size(40, 40), //图标大小
            image: "../../resources/img/start.svg",
            imageOffset: new AMap.Pixel(0, 0)
        }),
        zIndex: 70
    });
    this.markerEnd = new AMap.Marker({
        // map: this.map,
        position: this.lineArr[this.lineArr.length - 1],
        offset: new AMap.Pixel(-12, -40), //相对于基点的位置
        icon: new AMap.Icon({
            size: new AMap.Size(40, 40), //图标大小
            image: "../../resources/img/end.svg",
            imageOffset: new AMap.Pixel(0, 0)
        }),
        zIndex: 70
    });
    var leftRightPadding = isChartOpen ? 0 : 290;
    this.map.add([this.markerStart, this.markerEnd]);
    // 设置500毫秒延迟，等页面UI调整完毕后进行可视区域范围显示
    setTimeout(() => {
        this.map.setFitView(this.line.concat([this.passedLine, this.markerStart, this.markerEnd]));
    }, 500)
}

// 上下补位算法，经纬度为零时，值变为下一个不为零的经纬度值
TrackMap.prototype.bwLongitudeAndLatitude = function (data) {
    var llData = JSON.parse(JSON.stringify(data));
    var llYou = {longtitude: '0.0', latitude: '0.0', longitude: '0.0'};
    data.map((item, index) => {
        if (item.longtitude !== '0.0' || item.latitude !== '0.0') {
            llYou.longtitude = item.longtitude;
            llYou.latitude = item.latitude;
            llYou.longitude = item.longitude;
            for (var i = 0; i < llData.length; i++) {
                if (llData[i].longtitude === '0.0' && llData[i].latitude === '0.0') {
                    llData[i].longtitude = llYou.longtitude;
                    llData[i].latitude = llYou.latitude;
                    llData[i].longitude = llYou.longitude;
                } else {
                    llYou.longtitude = llData[i].longtitude;
                    llYou.latitude = llData[i].latitude;
                    llYou.longitude = llData[i].longitude;
                };
            }
        }
    });
    return llData;
}

TrackMap.prototype.redrawLine = function () {
    var dataDependency = this.dependency.get('data');
    var positions = dataDependency.getPositions();
    var speeds = dataDependency.getTrackColor();
    var playIndex = dataDependency.getPlayIndex();
    var trackWidth = dataDependency.getTrackWidth();
    if (positions === null) {
        return;
    }
    // 清空之前的线段
    if (this.line && this.line.length > 0) {
        for (var i = 0; i < this.line.length; i++) {
            this.map.remove(this.line[i]);
        }
    }
    // 清空播放过的线段
    this.map.remove(this.passedLine);
    var colors = this.getParaColor(positions, speeds, trackWidth);
    var colorKeys = Object.keys(colors);
    var lineArr = [];
    for (var i = 0; i < colorKeys.length; i++) {
        var colorItem = colors[colorKeys[i]];
        for (var j = 0; j < colorItem.segment.length; j++) {
            lineArr.push(new AMap.Polyline({
                map: this.map,
                path: this.lineArr.slice(colorItem.segment[j].startIndex, colorItem.segment[j].endIndex),
                strokeColor: colorItem.color, //线颜色
                strokeOpacity: 0.9, //线透明度
                strokeWeight: trackWidth, //线宽
                strokeStyle: "solid", //线样式
                zIndex: 50,
                showDir: true
            }));
        }
    }
    this.line = lineArr;
    this.passedLine = new AMap.Polyline({
        map: this.map,
        strokeColor: "#000", //线颜色
        strokeOpacity: 0.5, //线透明度
        strokeWeight: trackWidth, //线宽
        strokeStyle: "solid", //线样式
        zIndex: 60
    });
    this.passedLine.setPath(this.lineArr.slice(0, playIndex + 1));
    // 重新设置行驶段的宽度
    if (this.runLine) {
        this.runLine.setOptions({
            strokeWeight: trackWidth
        });
    }
}

TrackMap.prototype.drawMarker = function () {
    var dataDependency = this.dependency.get('data');
    var positions = dataDependency.getPositions();
    var objType = dataDependency.getObjType();
    var playIndex = dataDependency.getPlayIndex();
    if (!this.lineArr) {
        return;
    }
    var angle = parseInt(positions[playIndex].angle) + 270;
    var lnglat = this.lineArr[playIndex];
    var position = positions[playIndex];
    if (this.marker === undefined || this.marker === null) {
        var icon = "../../resources/img/vehicle.png";
        if (objType == "people") {
            icon = "/clbs/resources/img/123.png";
        } else if (objType == "thing") {
            icon = "/clbs/resources/img/thing.png";
        } else {
            icon = "/clbs/resources/img/vehicle.png";
        }
        var icons = positions[0].ico;
        if (icons != null && icons != '') {
            icon = "/clbs/resources/img/vico/" + icons;
        }
        this.markerIcon = icon;
        this.marker = new AMap.Marker({
            map: this.map,
            position: lnglat,//基点位置
            icon: this.markerIcon, //marker图标，直接传递地址url
            offset: new AMap.Pixel(-20, -15), //相对于基点的位置
            zIndex: 99997,
            autoRotation: true,
            angle: angle
        });
        var _this = this;
        this.marker.on('click', function () {
            _this.toggleInfoWindow();
        });
    } else {
        // this.marker.setAngle(angle);
        // 如果监控对象跑出了可视区域，重新定位地图中心为监控对象
        if (this.isOutOfBounds(lnglat)) {
            this.map.setCenter(lnglat);
        }
        if (dataDependency.getIsPlaying()) {
            // 当前经纬度和之前的经纬度如果距离小于5米，则无法触发 moveend 事件
            // 我们采用直接跳点的方案，跳点后还需手动触发play方法
            // 历史数据经纬度为0的点, 不移动位置
            var currentPosition = this.marker.getPosition();
            // var dis=AMap.GeometryUtil.distance(currentPosition, lnglat);
            if (this.shouldJump(currentPosition.lng, currentPosition.lat, lnglat[0], lnglat[1])) {
                this.marker.stopMove();
                this.marker.setPosition(lnglat);
                requestAnimationFrame(function () {
                    this.continue();
                }.bind(this));
            } else {
                var angle = this.calcAngle(currentPosition, lnglat);
                var _this = this;
                setTimeout(function () {
                    _this.marker.setAngle(angle);
                    _this.marker.moveTo(lnglat, {
                        duration: dataDependency.getSpeed() / 100,
                        delay: 0,
                        speed: dataDependency.getSpeed(),
                        autoRotation: false
                    });
                }, 0)
            }
        } else {
            this.marker.setPosition(lnglat);
            this.passedLine.setPath(this.lineArr.slice(0, playIndex + 1));
        }
    }
}

/**
 * 获取两点间旋转角度
 * @param start
 * @param end
 */
TrackMap.prototype.calcAngle = function (start, end) {
    if (!this.map) return 0;
    const p_start = this.map.lngLatToContainer(start);
    const p_end = this.map.lngLatToContainer(end);
    const diff_x = p_end.x - p_start.x;
    const diff_y = p_end.y - p_start.y;
    return 360 * Math.atan2(diff_y, diff_x) / (2 * Math.PI);
}

TrackMap.prototype.isOutOfBounds = function (lnglat) {
    var bounds = this.bounds;
    if (!bounds) return false;
    if (
        lnglat[0] <= bounds[0][0]
        || lnglat[1] <= bounds[0][1]
        || lnglat[0] >= bounds[1][0]
        || lnglat[1] >= bounds[1][1]
    ) {
        return true;
    }
    return false;
}

TrackMap.prototype.shouldJump = function (x1, y1, x2, y2) {
    var speed = this.dependency.get('data').getSpeed();
    if (x2 == 0 && y2 == 0 || (x2 == '114.059264' && y2 == '22.612487')) {
        return true;
    }
    var distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    var thresholdValue = 0.00002;
    if (speed === 10000) {
        thresholdValue = 0.00004;
    } else if (speed === 20000) {
        thresholdValue = 0.00008;
    }
    if (distance <= thresholdValue) {
        return true;
    }
    return false;

}

TrackMap.prototype.prev = function () {
    var dataDependency = this.dependency.get('data');
    if (this.marker) {
        this.marker.stopMove();
    }
    dataDependency.setIsPlaying(false);
    var positions = dataDependency.getPositions();
    var playIndex = dataDependency.getPlayIndex();
    if (playIndex > 0) {
        dataDependency.setPlayIndex(playIndex - 1);
    } else {
        layer.msg('已经是第一个点了')
    }
}

TrackMap.prototype.play = function () {
    var dataDependency = this.dependency.get('data');

    if (!this.listenToMoveend && this.marker) {
        this.listenToMoveend = true;
        var _this = this;
        this.marker.on('moveend', function () {
            if (dataDependency.getIsPlaying()) {
                _this.continue();
            }
        });
        this.listenToMoving = true;
        this.marker.on('moving', function () {
            var playIndexMoving = dataDependency.getPlayIndex();
            _this.passedLine.setPath(_this.lineArr.slice(0, playIndexMoving + 1));
        });
    }
    dataDependency.setIsPlaying(true);

    this.continue();
}

TrackMap.prototype.continue = function () {
    var dataDependency = this.dependency.get('data');
    var positions = dataDependency.getPositions();

    var playIndex = dataDependency.getPlayIndex();
    if (playIndex + 1 < positions.length) {
        dataDependency.setPlayIndex(playIndex + 1);
    } else {
        dataDependency.setIsPlaying(false);
        this.dependency.get('table').autoGetStatusPosition();
    }
}

TrackMap.prototype.pause = function (target) {
    var dataDependency = this.dependency.get('data');
    if (this.marker) {
        this.marker.stopMove();
    }
    dataDependency.setIsPlaying(false, target);
    this.dependency.get('table').autoGetStatusPosition();
}

TrackMap.prototype.reset = function () {
    var dataDependency = this.dependency.get('data');
    if (this.marker) {
        this.marker.stopMove();
    }
    dataDependency.setIsPlaying(false);
    setTimeout(function () {
        dataDependency.setPlayIndex(0);
    }, 10)
}

TrackMap.prototype.next = function () {
    var dataDependency = this.dependency.get('data');
    if (this.marker) {
        this.marker.stopMove();
    }
    dataDependency.setIsPlaying(false);
    var positions = dataDependency.getPositions();
    var playIndex = dataDependency.getPlayIndex();
    if (playIndex + 1 < positions.length) {
        dataDependency.setPlayIndex(playIndex + 1);
    } else {
        layer.msg('已经是最后一个点了');
    }
}

/**
 * 过滤全部数据为停止段第一个点
 * 算法为行驶状态过渡到停止的第一个点整个数据首第一个停止点点
 * @param array
 * @returns {*}
 */
TrackMap.prototype.filterStopData = function (array) {
    var r = [];
    if (array === null) {
        return null;
    }
    // 2 代表停止
    for (var i = 0; i < array.length; i++) {
        var item = undefined;
        if (array[i - 1] && array[i - 1].drivingState === '1' && array[i].drivingState === '2') { // 行驶过渡到停止
            item = array[i];
        } else if (i === 0 && array[i].drivingState === '2') { // 首尾的停止点
            item = array[i];
        }
        if (item !== undefined) {
            r.push({
                item: item,
                index: i
            })
        }
    }
    return r;
};

TrackMap.prototype.toggleStopPoint = function () {
    var dataDependency = this.dependency.get('data');
    var positions = dataDependency.getPositions();
    var show = dataDependency.getShowStopPoint();
    if (positions === null) {
        return;
    }
    if (show) {
        layer.msg('显示停止点');
        var stopPoints = this.filterStopData(positions);
        var markerArr = [];
        var icon = icon = "../../resources/img/stop.svg";
        for (var i = 0; i < stopPoints.length; i++) {
            var stop = stopPoints[i];
            var stopMarker = new AMap.Marker({
                map: this.map,
                position: new AMap.LngLat(stop.item.longtitude, stop.item.latitude), //基点位置
                icon: icon, //marker图标，直接传递地址url
                offset: new AMap.Pixel(-16, -13), //相对于基点的位置
                zIndex: 99999,
                extData: {
                    stopIndex: i,
                    originIndex: stop.index
                }
            });
            stopMarker.on('click', this.onStopMarkerClick.bind(this));
            markerArr.push(stopMarker);
        }
        this.stopMarkers = markerArr;
        this.map.setFitView(this.stopMarkers);
    } else {
        layer.msg('移除停止点');
        if (this.stopMarkers !== null) {
            for (var i = 0; i < this.stopMarkers.length; i++) {
                this.map.remove(this.stopMarkers[i]);
            }
        }
        if (this.middleStopMarkers !== null) {
            for (var i = 0; i < this.middleStopMarkers.length; i++) {
                this.map.remove(this.middleStopMarkers[i]);
            }
        }
    }
}

TrackMap.prototype.toggleAlarmPoint = function () {
    var dataDependency = this.dependency.get('data');
    var show = dataDependency.getShowAlarmPoint();
    var vehicleId = dataDependency.getVid();
    var startTime = dataDependency.getStartTime();
    var endTime = dataDependency.getEndTime();
    if (show) {
        layer.msg('显示报警点');
        json_ajax('POST', '/clbs/v/monitoring/getAlarmData', 'json', true, {
            "vehicleId": vehicleId,
            "startTime": parseInt(new Date(startTime.replace(/\-/g, '/')).getTime() / 1000),
            "endTime": parseInt(new Date(endTime.replace(/\-/g, '/')).getTime() / 1000),
        }, function (data) {
            if (data.success) {
                dataDependency.setAlarmData(data.obj);
                var alarmPoints = data.obj;
                var markerArr = [];
                var icon = icon = "../../resources/img/al.svg";
                if (!alarmPoints || !alarmPoints.length) return false;
                for (var i = 0; i < alarmPoints.length; i++) {
                    var sLocation = alarmPoints[i].alarmStartLocation.split(',');
                    var alarmMarker = new AMap.Marker({
                        map: this.map,
                        position: new AMap.LngLat(sLocation[0], sLocation[1]), //基点位置
                        icon: icon, //marker图标，直接传递地址url
                        offset: new AMap.Pixel(-16, -13), //相对于基点的位置
                        zIndex: 99999,
                        extData: i
                    });
                    alarmMarker.on('click', this.onAlarmMarkerClick.bind(this));
                    markerArr.push(alarmMarker);
                }
                this.alarmMarkers = markerArr;
                this.map.setFitView(this.alarmMarkers);
            } else {
                layer.msg(data.msg);
            }
        }.bind(this));
    } else {
        layer.msg('移除报警点');
        if (this.alarmMarkers !== null) {
            for (var i = 0; i < this.alarmMarkers.length; i++) {
                this.map.remove(this.alarmMarkers[i]);
            }
        }
    }
}

// 基站定位点
TrackMap.prototype.toggleLocation = function () {
    var dataDependency = this.dependency.get('data');
    var show = dataDependency.getShowLocation(); // 是否显示基站定位
    if (show) {
        setTimeout(function () {
            layer.msg('切换显示成功，显示基站定位');
        }, 1000);
        this.drawLine(true, show);
    } else {
        setTimeout(function () {
            layer.msg('切换显示成功，移除基站定位');
        }, 1000);
        this.drawLine(false, show);
    }
}

// 基站定位筛选
TrackMap.prototype.filterLocationData = function (array) {
    var r = [];
    if (array === null) {
        return null;
    }
    // 基站定位信息 stationEnabled
    for (var i = 0; i < array.length; i++) {
        var item = undefined;
        if (array[i] && array[i].stationEnabled === false) { // 无基站定位信息数据
            item = array[i];
        }
        if (item !== undefined) {
            r.push({
                item: item,
                index: i
            })
        }
    }
    return r;
};

TrackMap.prototype.danceStopMarker = function (index) {
    var dataDependency = this.dependency.get('data');
    var positions = dataDependency.getPositions();
    var stopPoints = this.filterStopData(positions);

    var marker;

    for (var i = 0; i < stopPoints.length; i++) {
        var item = stopPoints[i];
        if (item.index === index) {
            if (!this.stopMarkers) {
                return;
            }
            marker = this.stopMarkers[i];
            if (!marker) {
                return;
            }
        }
    }

    if (marker === undefined) {
        marker = new AMap.Marker({
            map: this.map,
            position: new AMap.LngLat(positions[index].longtitude, positions[index].latitude),//基点位置
            icon: "../../resources/img/stop.svg", //marker图标，直接传递地址url
            offset: new AMap.Pixel(-16, -13), //相对于基点的位置
            zIndex: 99999
        });
        if (this.middleStopMarkers === null) {
            this.middleStopMarkers = [marker];
        } else {
            this.middleStopMarkers.push(marker);
        }
    }

    if (marker) {
        // console.log(marker, 'marker');
        // marker.setAnimation('AMAP_ANIMATION_BOUNCE');
        // setTimeout(function () {
        //     marker.setAnimation('AMAP_ANIMATION_NONE');
        // }, 5300)
    }
}

TrackMap.prototype.danceAlarmMarker = function (index) {
    if (!this.alarmMarkers) {
        return;
    }
    var marker = this.alarmMarkers[index];
    if (marker) {
        marker.setAnimation('AMAP_ANIMATION_BOUNCE');
        setTimeout(function () {
            marker.setAnimation('AMAP_ANIMATION_NONE');
        }, 5300)
    }
}

TrackMap.prototype.highlightRunSegment = function (startIndex, endIndex, runMile) {
    var dataDependency = this.dependency.get('data');
    var allOrRunData = dataDependency.getAllOrRunData();
    var positions = dataDependency.getPositions();
    var positionsTmp = dataDependency.getPositionsTmp();
    var trackWidth = dataDependency.getTrackWidth();

    var positionsArray = allOrRunData === 'run' ? positionsTmp : positions;

    positionsArray = positionsArray.slice(startIndex, endIndex + 1);
    var lnglatArr = this.formateLngLatFromPosition(positionsArray);
    var lastPoint = [lnglatArr[lnglatArr.length - 1][0], lnglatArr[lnglatArr.length - 1][1]];

    this.removeHighlightRunSegment();

    this.runLine = new AMap.Polyline({
        map: this.map,
        path: lnglatArr,
        isOutline: false,
        strokeColor: "#0072d4", //线颜色
        strokeOpacity: 1, //线透明度
        strokeWeight: trackWidth, //线宽
        strokeStyle: "solid", //线样式
        outlineColor: '#0000ff',
        borderWeight: 2,
        zIndex: 60
    });
    this.runMileMarker = new AMap.Text({
        map: this.map,
        position: lastPoint,
        offset: new AMap.Pixel(-20, -25), //相对于基点的位置
        text: '行驶里程：' + toFixed(runMile, 1, true) + 'km',
        anchor: 'top-right',
        zIndex: 99998
    });
}

TrackMap.prototype.removeHighlightRunSegment = function () {
    if (this.runLine) {
        this.map.remove(this.runLine);
        this.runLine = null;
    }
    if (this.runMileMarker) {
        this.map.remove(this.runMileMarker);
        this.runMileMarker = null;
    }
}

TrackMap.prototype.focusRunSegment = function () {
    this.map.setFitView(this.runLine);
}

TrackMap.prototype.onAlarmMarkerClick = function (e) {
    var index = e.target.getExtData();
    var dataDependency = this.dependency.get('data');
    var alarmPoints = dataDependency.getAlarmData();
    var positions = dataDependency.getPositions();

    if (!this.alarmMarkers
        || this.alarmMarkers.length === 0
        || index === undefined
        || alarmPoints === null
        || !alarmPoints[index]
        || !this.alarmMarkers[index]
    ) {
        return;
    }
    var item = alarmPoints[index];
    var vcolour = '';
    var alarmSIM = '';
    var alarmTopic = '';
    if (positions && positions[0]) {
        vcolour = positions[0].plateColor;
        alarmSIM = positions[0].simCard;
        alarmTopic = positions[0].deviceNumber;
    }
    var arr = [];
    arr.push("监控对象:" + item.monitorName);
    arr.push("车牌颜色:" + vcolour);
    arr.push("所属分组:" + item.assignmentName);
    arr.push("高程:" + (item.height === null ? "" : item.height));
    arr.push("终端手机号:" + alarmSIM);
    arr.push("终端号:" + alarmTopic);
    arr.push("记录仪速度:" + (item.recorderSpeed === null ? "" : item.recorderSpeed));
    arr.push("报警信息:" + item.description);
    arr.push("处理状态:" + item.alarmStatus);
    arr.push("处理人:" + (item.personName === null ? "无" : item.personName));
    arr.push("报警开始时间:" + (item.startTime === null ? "" : item.startTime));
    arr.push("报警开始坐标:" + (item.alarmStartLocation === undefined ? "位置描述获取失败" : item.alarmStartLocation));
    arr.push("报警结束时间:" + (item.endTime === null ? "" : item.endTime));
    arr.push("报警结束坐标:" + (item.alarmEndLocation === undefined ? "位置描述获取失败" : item.alarmEndLocation));

    var content = arr.join("<br/>");
    if (this.alarmInfoWindow) {
        this.alarmInfoWindow.setContent(content);
        this.alarmInfoWindow.open(this.map, this.alarmMarkers[index].getPosition());
    } else {
        this.alarmInfoWindow = new AMap.InfoWindow({
            content: content,
            offset: new AMap.Pixel(-20, -13),
            closeWhenClickMap: true
        });
        this.alarmInfoWindow.open(this.map, this.alarmMarkers[index].getPosition());
    }
}

TrackMap.prototype.onStopMarkerClick = function (e) {
    var dataDependency = this.dependency.get('data');
    var tableDependency = this.dependency.get('table');

    var stopTypeDict = dataDependency.getStopTypeDict();
    var positions = dataDependency.getPositions();

    var extData = e.target.getExtData();
    var index = extData.stopIndex;
    var originIndex = extData.originIndex;
    // 此时的 originIndex 为position中的index，没有补点，要想获取 stopTypeDict
    // 中对应的数据，需要通过 Data.getParaScale 的分段比例尺转换为图表索引
    originIndex = dataDependency.getParaScale()(originIndex, 'toChart');

    if (!this.stopMarkers
        || this.stopMarkers.length === 0
        || index === undefined
        || !this.stopMarkers[index]
        || !stopTypeDict
    ) {
        return;
    }

    var segments = stopTypeDict['2'].segment;
    var segment;
    if (segments) {
        for (var i = 0; i < segments.length; i++) {
            if (segments[i].startIndex <= originIndex && originIndex <= segments[i].endIndex) {
                segment = segments[i];
                break;
            }
        }
    }

    if (!segment) {
        return;
    }

    var arr = [];
    arr.push("停止点:" + (index + 1));
    arr.push("停止时长:" + TrackPlaybackColumn.timeRender(segment.timeLength));
    arr.push("开始时间:" + TrackPlaybackColumn.fullTimeRender(segment.startTime * 1000));
    arr.push("结束时间:" + TrackPlaybackColumn.fullTimeRender(segment.endTime * 1000));
    arr.push("停止位置:" + '<span class="stopMarkerInfoWindowLocation">...</span>');

    var content = arr.join("<br/>");
    if (this.stopInfoWindow) {
        this.stopInfoWindow.setContent(content);
        this.stopInfoWindow.open(this.map, this.stopMarkers[index].getPosition());
    } else {
        this.stopInfoWindow = new AMap.InfoWindow({
            content: content,
            offset: new AMap.Pixel(-20, -13),
            closeWhenClickMap: true
        });
        this.stopInfoWindow.open(this.map, this.stopMarkers[index].getPosition());
    }
    tableDependency.getAddress(null, positions[extData.originIndex].latitude, positions[extData.originIndex].longtitude, function (data) {
        $('.stopMarkerInfoWindowLocation').html(($.isPlainObject(data) ? '未定位' : data));
    });
}

TrackMap.prototype.onStopMarkerClick = function (e) {
    var dataDependency = this.dependency.get('data');
    var tableDependency = this.dependency.get('table');

    var stopTypeDict = dataDependency.getStopTypeDict();
    var positions = dataDependency.getPositions();

    var extData = e.target.getExtData();
    var index = extData.stopIndex;
    var originIndex = extData.originIndex;
    // 此时的 originIndex 为position中的index，没有补点，要想获取 stopTypeDict
    // 中对应的数据，需要通过 Data.getParaScale 的分段比例尺转换为图表索引
    originIndex = dataDependency.getParaScale()(originIndex, 'toChart');

    if (!this.stopMarkers
        || this.stopMarkers.length === 0
        || index === undefined
        || !this.stopMarkers[index]
        || !stopTypeDict
    ) {
        return;
    }

    var segments = stopTypeDict['2'].segment;
    var segment;
    if (segments) {
        for (var i = 0; i < segments.length; i++) {
            if (segments[i].startIndex <= originIndex && originIndex <= segments[i].endIndex) {
                segment = segments[i];
                break;
            }
        }
    }

    if (!segment) {
        return;
    }

    var arr = [];
    arr.push("停止点:" + (index + 1));
    arr.push("停止时长:" + TrackPlaybackColumn.timeRender(segment.timeLength));
    arr.push("开始时间:" + TrackPlaybackColumn.fullTimeRender(segment.startTime * 1000));
    arr.push("结束时间:" + TrackPlaybackColumn.fullTimeRender(segment.endTime * 1000));
    arr.push("停止位置:" + '<span class="stopMarkerInfoWindowLocation">...</span>');

    var content = arr.join("<br/>");
    if (this.stopInfoWindow) {
        this.stopInfoWindow.setContent(content);
        this.stopInfoWindow.open(this.map, this.stopMarkers[index].getPosition());
    } else {
        this.stopInfoWindow = new AMap.InfoWindow({
            content: content,
            offset: new AMap.Pixel(-20, -13),
            closeWhenClickMap: true
        });
        this.stopInfoWindow.open(this.map, this.stopMarkers[index].getPosition());
    }
    tableDependency.getAddress(null, positions[extData.originIndex].latitude, positions[extData.originIndex].longtitude, function (data) {
        $('.stopMarkerInfoWindowLocation').html(($.isPlainObject(data) ? '未定位' : data));
    });
}

TrackMap.prototype.closeInfoWindow = function () {
    if (this.infoWindow && this.infoWindow.getIsOpen()) {
        this.infoWindow.close();
    }
}

TrackMap.prototype.toggleInfoWindow = function () {
    var dataDependency = this.dependency.get('data');
    var isPlaying = dataDependency.getIsPlaying();
    var isDragging = dataDependency.getIsDraging();
    var positions = dataDependency.getPositions();
    var sensorFlag = dataDependency.getSensorFlag();
    var playIndex = dataDependency.getPlayIndex();
    var groups = dataDependency.getGroup();

    if (!positions || playIndex === null || playIndex === undefined) {
        return;
    }

    var position = positions[playIndex];

    if (!position) {
        return;
    }

    if (isPlaying || isDragging) {
        if (this.infoWindow) {
            this.infoWindow.close();
        }
    } else {
        if (!this.marker) {
            return;
        }
        var speed = '-';
        if (position.status !== '2') {
            if (sensorFlag) {
                speed = position.mileageSpeed;
            } else {
                speed = position.speed;
            }
            if (speed === undefined || speed === null) {
                speed = '';
            }
        }
        var arr = [];
        arr.push("定位时间：" + TrackPlaybackColumn.fullTimeRender(position.vtime * 1000));
        arr.push("监控对象：" + position.plateNumber);
        arr.push("车牌颜色：" + position.plateColor);
        arr.push("终端号：" + position.deviceNumber);
        arr.push("终端手机号：" + position.simCard);
        arr.push("所属分组：" + groups);
        arr.push("经度：" + position.longtitude);
        arr.push("纬度：" + position.latitude);
        arr.push("高程：" + position.height);
        arr.push("速度(km/h)：" + speed);
        arr.push('<a type="button" id="addFence" onclick="trackPlayback.map.showAddFencePage()" style="cursor:pointer;display:inline-block;margin: 8px 0px 0px 0px;">轨迹生成路线</a>');
        var content = arr.join("<br/>");
        if (this.infoWindow) {
            this.infoWindow.setContent(content);
            this.infoWindow.open(this.map, this.marker.getPosition());
        } else {
            this.infoWindow = new AMap.InfoWindow({
                content: content,
                offset: new AMap.Pixel(-20, -13),
                closeWhenClickMap: true
            });
            this.infoWindow.open(this.map, this.marker.getPosition());
        }
    }
}

TrackMap.prototype.formateLngLatFromPosition = function (positions) {
    var lineArr = [];
    for (var i = 0; i < positions.length; i++) {
        var x = positions[i];
        var lnglat = [x.longtitude ? x.longtitude : '0.0', x.latitude ? x.latitude : '0.0'];
        if (
            (!lnglat[0] || !lnglat[1]) ||
            (lnglat[0] == '0.0' && lnglat[1] == '0.0') ||
            (lnglat[0] == '114.059264' && lnglat[1] == '22.612487')
        ) {
            // 循环向前查找替换
            for (var j = i - 1; j >= 0; j--) {
                if ((positions[j].longtitude !== '114.059264' && positions[j].latitude !== '22.612487') &&
                    (positions[j].longtitude != '0.0' && positions[j].latitude != '0.0')) {
                    lnglat[0] = positions[j].longtitude;
                    lnglat[1] = positions[j].latitude;
                    break;
                }
            }
        }
        lineArr.push(lnglat);
    }
    return lineArr;
}

TrackMap.prototype.showAddFencePage = function () {
    setTimeout(function () {
        $("#addFencePage").modal("show");
    }, 200);
}
//轨迹生成围栏，围栏名称唯一性验证
TrackMap.prototype.addLineFence = function () {
    var name = $("#lineName1").val();
    var url = "/clbs/m/functionconfig/fence/managefence/addLine";
    var data = {"name": name};
    json_ajax("POST", url, "json", false, data, this.lineCallback.bind(this));
}
//轨迹生成围栏验证
TrackMap.prototype.lineCallback = function (data) {
    if (data.success == true) {
        this.trackLineAdded();
    } else {
        if (data.msg == null) {
            layer.msg(trackFenceExists);
        } else if (data.msg.toString().indexOf("系统错误") > -1) {
            layer.msg(data.msg, {move: false});
        }
    }
}
//历史轨迹数据查询
TrackMap.prototype.getHistory1 = function () {
    var dataDependency = this.dependency.get('data');
    var positions = dataDependency.getPositions();

    var pointSeqs = ""; // 点序号
    var longtitudes = ""; // 所有的经度
    var latitudes = ""; // 所有的纬度
    var position;
    var latitude;
    var longtitude;
    var m = 0;
    for (var i = 0; i < positions.length; i++) {
        position = positions[i];
        latitude = position.latitude;//纬度
        longtitude = position.longtitude;//经度

        pointSeqs += (m++) + ",";
        longtitudes += Math.formatFloat(longtitude, 6) + ",";
        latitudes += Math.formatFloat(latitude, 6) + ",";
    }
    if (pointSeqs.length > 0) {
        pointSeqs = pointSeqs.substr(0, pointSeqs.length - 1);
    }
    if (longtitudes.length > 0) {
        longtitudes = longtitudes.substr(0, longtitudes.length - 1);
    }
    if (latitudes.length > 0) {
        latitudes = latitudes.substr(0, latitudes.length - 1);
    }
    $("#pointSeqs").val(pointSeqs);
    $("#longitudes").val(longtitudes);
    $("#latitudes").val(latitudes);
}

//轨迹线路添加
TrackMap.prototype.trackLineAdded = function () {
    if (this.validate_line()) {
        layer.load(2);
        $("#addFenceBtn").attr("disabled", true);
        $("#hideDialog").attr("disabled", true);
        this.getHistory1();
        var pointSeqs = $("#pointSeqs").val();
        var longtitudes = $("#longtitudes").val();
        var latitudes = $("#latitudes").val();
        if (pointSeqs == "" || longtitudes == "" || latitudes == "") {
            layer.msg(trackHistoryDataNull);
            return;
        }
        $("#addLineForm").ajaxSubmit(function (data) {
            data = JSON.parse(data);
            if (data.success) {
                $(".cancle").click();
                $("#addFenceBtn").attr("disabled", false);
                $("#hideDialog").attr("disabled", false);
                $("#hideDialog").click();
                layer.closeAll();
                layer.msg(publicSaveSuccess);
            } else {
                if (data.msg == null) {
                    layer.msg(publicSaveError);
                } else if (data.msg.toString().indexOf("系统错误") > -1) {
                    layer.msg(data.msg, {move: false});
                }
            }
        });
    }
}
//线路添加时验证
TrackMap.prototype.validate_line = function () {
    return $("#addLineForm").validate({
        rules: {
            name: {
                required: true,
                maxlength: 20
            },
            width: {
                required: true,
                maxlength: 10
            },
            description: {
                maxlength: 100
            }
        },
        messages: {
            name: {
                required: publicNull,
                maxlength: publicSize20
            },
            width: {
                required: publicNull,
                maxlength: publicSize10
            },
            description: {
                maxlength: publicSize100
            }
        }
    }).form();
}
