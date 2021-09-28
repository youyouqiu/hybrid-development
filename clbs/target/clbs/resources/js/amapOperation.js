// wjk 实时视频时间定时器
var computingTimeInt;

var amapOperation = {
        // 地图初始化
        init: async function (mapType) {
            if (mapType) {// 地图类型切换时触发
                // 创建地图
                map = await $.fn.mapEngine.loadMap(mapType, 'MapContainer');
                mapaddoperation.init();
                amapFunCollection.init();
            } else {
                // 根据用户缓存,获取默认初始化的地图类型,并修改相应的地图功能启用禁用
                var userMapKey = $('#userName').text() + '_defaultMap';
                var userDefaultMap = window.localStorage.getItem(userMapKey);
                if (userDefaultMap) {
                    userDefaultMap = JSON.parse(userDefaultMap);
                }
                pageLayout.mapTypeChange('', userDefaultMap ? userDefaultMap.mapId : 'baiduMap');
                // 创建地图
                map = await $.fn.mapEngine.loadMap(userDefaultMap ? userDefaultMap.type : 'BMap', 'MapContainer');
                // 默认显示百度地图时,三维地球需要先获取下地图视图范围
                if (!userDefaultMap || userDefaultMap.mapType === 'BMap') {
                    amapOperation.LimitedSize(6);
                }
            }
            // 输入提示
            var startPoint = map.autoComplete({
                input: "startPoint"
            });
            startPoint.on('select', fenceOperation.dragRoute);
            var endPoint = map.autoComplete({
                input: "endPoint"
            });
            endPoint.on('select', fenceOperation.dragRoute);

            // 行政区划查询
            var opts = {
                subdistrict: 1,   //返回下一级行政区
                level: 'city',
                showbiz: false  //查询行政级别为 市
            };
            district = map.districtSearch(opts);//注意：需要使用插件同步下发功能才能这样直接使用
            district.search('中国', function (status, result) {
                if (status == 'complete') {
                    fenceOperation.getData(result.districtList[0]);
                }
            });
            // 地图移动结束后触发，包括平移和缩放
            mouseTool = map.mouseTool(map);
            mouseTool.on("draw", fenceOperation.createSuccess);
            mouseToolEdit = map.mouseTool(map);
            // 实例化3D楼块图层
            buildings = map.buildings(map);
            // 在map中添加3D楼块图层
            // map.add(buildings);
            // 地图标尺
            map.addControl(map.toolBar());
            map.addControl(map.scale());
            // 谷歌卫星地图
            satellLayer = map.satellite();
            // satellLayer = map.tileLayer({
            //     tileUrl: 'https://mt{1,2,3,0}.google.cn/maps/vt?lyrs=s@194&hl=zh-CN&gl=cn&x=[x]&y=[y]&z=[z]'
            // });
            satellLayer.setMap(map);
            // 路网
            // roadNet = new AMap.TileLayer.RoadNet();
            // map.add([satellLayer, roadNet]);
            satellLayer.hide();
            // roadNet.hide();
            // 实时路况
            realTimeTraffic = map.traffic();
            realTimeTraffic.setMap(map);
            realTimeTraffic.hide();
            // 当范围缩小时触发该方法
            map.on('zoomend', amapOperation.markerStateListening);
            // var clickEventListener = map.on('zoomend', amapOperation.clickEventListener);
            // 当拖拽结束时触发该方法
            map.on('dragend', amapOperation.markerStateListening);
            // var clickEventListener2 = map.on('dragend', amapOperation.clickEventListener2);
            // 地图点击隐藏车辆树右键菜单
            map.on("click", function () {
                $("#rMenu").css("visibility", "hidden");
                $("#disSetMenu").slideUp();
                $("#mapDropSettingMenu").slideUp();
                $('#measurementMenu').slideUp();
                $("#fenceTool>.dropdown-menu").hide();
            });
            infoWindow = map.infoWindow({offset: map.pixel(0, -10), closeWhenClickMap: true});
            // 地图移动结束后触发，包括平移，以及中心点变化的缩放
            map.on('moveend', amapOperation.mapMoveendFun);
            document.addEventListener('visibilitychange', amapOperation.markerStateListening);
            mapTypeSwitch = false;
            // 判断是否为四维地图或天地图
            if (mapType === "TMap" || mapType === "NglpMap") {
                mapaddoperation.setMapNationalRoom();
            }
        },
        // 地图setcenter完成后触发事件
        mapMoveendFun: function () {
            amapOperation.pathsChangeFun();
            amapOperation.LimitedSizeTwo();
        },
        getDCallBack: function (data) {
            msgSNAck = data.obj.msgSN;
        },
        // 订阅最后位置信息
        subscribeLatestLocation: function (param) {
            var requestStrS = {
                "desc": {
                    "MsgId": 40964,
                    "UserName": $("#userName").text(),
                    "cmsgSN": msgSNAck
                },
                "data": param
            };
            webSocket.subscribe(headers, "/user/topic/realLocationS", amapOperation.getLastOilDataCallBack, "/app/vehicle/realLocationS", requestStrS, 'callTheRoll');
        },
        // 对象点名传递数据
        getLastOilDataCallBack: function (data) {
            var data = $.parseJSON(data.body);
            if (data.desc.msgID === 513) {
                if (msgSNAck == data.data.msgBody.msgSNAck) {
                    var obj = {};
                    obj.desc = data.desc;
                    var da = {};
                    da.msgHead = data.data.msgHead;
                    da.msgBody = data.data.msgBody;
                    obj.data = da;
                    // 状态信息
                    dataTableOperation.updateVehicleStatusInfoTable(obj);
                }
            }
        },
        // 渲染地图弹窗显示内容
        renderContent: function (item, mapPopupInfo, content) {
            var columnValue = (mapPopupInfo[item.name] !== null && mapPopupInfo[item.name] !== undefined) ? mapPopupInfo[item.name] : '';
            switch (item.title) {
                case '监控对象':
                    if (mapPopupInfo.plateColor == "") {
                        content.push("<div>监控对象：" + columnValue + "</div>");
                    } else {
                        content.push("<div>监控对象：" + columnValue + "(" + mapPopupInfo.plateColor + ")</div>");
                    }
                    break;
                case '行驶状态':
                    if (columnValue == "行驶") {
                        content.push("<div>行驶状态：" + "<font color='#78af3a'>" + columnValue + "</font>" + "</div>");
                    } else if (columnValue == "停止") {
                        content.push("<div>行驶状态：" + "<font color='#c80002'>" + columnValue + "</font>" + "</div>");
                    }
                    break;
                case 'ACC状态':
                    if (columnValue == "开" || (columnValue.indexOf("无") == -1 && columnValue.indexOf("点火") > -1)) {
                        content.push("<div>ACC：" + columnValue + " <img src='../../resources/img/acc_on.svg' style='margin: -3px 0px 0px 0px;height:24px;'/></div>");
                    } else {
                        content.push("<div>ACC：" + columnValue + " <img src='../../resources/img/acc_off.svg' style='margin: -3px 0px 0px 0px;height:24px;'/></div>");
                    }
                    break;
                case '经纬度':
                    content.push("<div>" + columnValue[0] + "</div>");
                    content.push("<div>" + columnValue[1] + "</div>");
                    break;
                case '当日里程':
                case '总里程':
                    content.push('<div class="popupColumns popup_' + item.name + '">' + item.title + '：' + Number(columnValue).toFixed(1) + '公里</div>');
                    break;
                case '方向':
                    content.push('<div class="popupColumns popup_' + item.name + '">' + item.title + '：' + dataTableOperation.toDirectionStr(columnValue) + '</div>');
                    break;
                case '对象类型':
                    content.push('<div class="popupColumns popup_' + item.name + '">' + item.title + '：' + (mapPopupInfo.vehicleType ? mapPopupInfo.vehicleType : "") + '</div>');
                    break;
                default:
                    content.push('<div class="popupColumns popup_' + item.name + '">' + item.title + '：' + columnValue + '</div>');
                    break;
            }
        },
        completeEventHandler: function (vehicle, mapPopupInfo) {//1
            if (mapTypeSwitch) return;
            var zoomLevel = 18;
            if (map) {
                zoomLevel = map.getZoom()
            }
            if (vehicle[11] == "people") {
                // 判断位置信息的经纬度是否正确
                if (vehicle[9] == 0 && vehicle[10] == 0) {
                    if (objAddressIsTrue.indexOf(vehicle[12]) == -1) {
                        objAddressIsTrue.push(vehicle[12]);
                    }
                    return;
                } else {
                    var index = objAddressIsTrue.indexOf(vehicle[12]);
                    if (index != -1) {
                        objAddressIsTrue.splice(index, 1);
                    }
                }
                /******************************************/
                var coordinateNew = [];
                var x = vehicle[9];
                var y = vehicle[10];
                coordinateNew.push(y);
                coordinateNew.push(x);
                var content = [];
                content.push("<div>时间：" + vehicle[0] + "</div>");
                content.push("<div>监控对象：" + vehicle[1] + "</div>");
                content.push("<div>所属分组：" + vehicle[2] + "</div>");
                content.push("<div>终端号：" + vehicle[3] + "</div>");
                content.push("<div>终端手机号：" + vehicle[4] + "</div>");
                content.push("<div>电池电压：" + vehicle[5] + "</div>");
                content.push("<div>信号强度：" + vehicle[6] + "</div>");
                content.push("<div>速度：" + vehicle[7] + "</div>");
                content.push("<div>海拔：" + vehicle[8] + "</div>");
                content.push(
                    '<div class="infoWindowSetting">' +
                    '<a class="col-md-3" id="jumpTo" onClick="window.amapOperation.jumpToTrackPlayer(\'' + vehicle[12] + '\')">' +
                    '<img src="../../resources/img/v-track.svg" style="height:28px;width:28px;"/>轨迹' +
                    '</a>' +
                    '</div>'
                );
                // 获取现在的订阅对象数据长度
                var subscribeObjOldLength = markerAllUpdateData.values().length;

                // 删除对应监控对象以前的数据
                if (markerAllUpdateData.containsKey(vehicle[12])) {
                    markerAllUpdateData.remove(vehicle[12]);
                }
                // if(icoUpFlag){
                //     markerInside.setAngle(0);
                // }
                // 组装监控对象需要保存的信息
                var objSaveInfo = [
                    vehicle[12], // 监控对象ID
                    vehicle[1], // 监控对象名称
                    vehicle[10], // 经度
                    vehicle[9], // 纬度
                    vehicle[13], // 角度
                    vehicle[14], // 状态
                    vehicle[16], // 图标
                    vehicle[0], // 时间
                    vehicle[17], // 里程
                    vehicle[15],//监控对象类型
                    vehicle[7], // 速度
                ];


                var updateInfo = [
                    objSaveInfo,
                    content
                ];
                // if (!vehicle[14]) return;// 监控对象状态异常
                markerAllUpdateData.put(vehicle[12], updateInfo);

                // 获取现在的订阅对象数据长度
                var subscribeObjNowLength = markerAllUpdateData.values().length;

                // 针对区域查询后，监控对象的聚合显示
                if (map && zoomLevel < 11 && subscribeObjNowLength != subscribeObjOldLength) {
                    amapOperation.markerStateListening();
                }

                // var angleVehicle = Number(vehicle[24]) + 270;
                // 判断是否是订阅的第一个对象
                if (map && markerViewingArea.size() == 0 && zoomLevel >= 11 && markerAllUpdateData.size() == 1) {
                    amapOperation.createMarker(objSaveInfo, content, !isAreaSearch);
                    isAreaSearch = false;
                } else {
                    // 判断当前位置点是否在可视区域内且层级大于11
                    if ((paths.contains(coordinateNew) || markerFocus == vehicle[12]) && zoomLevel >= 11) {
                        if (markerViewingArea.containsKey(vehicle[12])) { // 判断是否含有该id数据
                            var value = markerViewingArea.get(vehicle[12]);

                            var marker = value[0];
                            marker.extData = vehicle[12]; // 监控对象id
                            marker.stateInfo = vehicle[14]; // 监控对象状态
                            marker.content = content.join(""); // 监控对象信息弹窗

                            var markerLngLat = [vehicle[10], vehicle[9]];
                            markerViewingArea.remove(vehicle[12]);
                            value[0] = marker;
                            value[1].push(markerLngLat);
                            value[2] = content;
                            value[3].push(null); // 里程
                            value[4].push(null); // 时间
                            value[6] = vehicle[14];
                            value[8].push(vehicle[13]);
                            value[9].push(vehicle[7]);
                            markerViewingArea.put(vehicle[12], value);
                            amapOperation.carNameEvade(vehicle[12], vehicle[1], marker.getPosition(), null, "1", null, false, vehicle[14]);// 监控对象进行移动
                            amapOperation.markerMoveFun(objSaveInfo);
                        } else { // 创建监控对象图标
                            amapOperation.createMarker(objSaveInfo, content, false);
                        }
                    } else {
                        amapOperation.saveMarkerOutsideInfo(objSaveInfo, content);
                    }
                }

            } else if (vehicle[11] != "people") {
                //获取车Id
                var vehicleId = vehicle[13];
                var monitorName = vehicle[0];
                /**************************************/
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                var nodes = treeObj.getCheckedNodes(true);
                var uuids = [];
                var type;
                var pid;
                for (var j = 0; j < nodes.length; j++) {
                    var vObj = {};
                    vObj.id = nodes[j].id;
                    vObj.pid = nodes[j].pId;
                    uuids.push(vObj);
                    if (nodes[j].id == vehicleId) {
                        type = nodes[j].type;
                        pid = nodes[j].pId;
                    }
                }
                /******************************************/
                //取出报警集合中等同于当前车Id的报警信息  赋值到当前车辆信息集合
                if (alarmInfoList.get(vehicleId) == undefined) {
                    vehicle[20] = "";
                    mapPopupInfo.alarmType = '';
                } else {
                    vehicle[20] = alarmInfoList.get(vehicleId);
                    mapPopupInfo.alarmType = alarmInfoList.get(vehicleId);
                }

                // 判断位置信息传过来的经纬度是否正确
                if (vehicle[11] == 0 && vehicle[12] == 0) {
                    if (objAddressIsTrue.indexOf(vehicleId) == -1) {
                        objAddressIsTrue.push(vehicleId);
                    }
                    return;
                } else {
                    var index = objAddressIsTrue.indexOf(vehicleId);
                    if (index != -1) {
                        objAddressIsTrue.splice(index, 1);
                    }
                }
                var coordinateNew = [];
                var x = vehicle[11];
                var y = vehicle[12];
                var vStatusInfoShows = [];
                for (var i = 0; i < vehicle.length; i++) {
                    if (i != 2 && i != 14 && i != 20) {
                        vStatusInfoShows.push(vehicle[i]);
                    }
                }
                coordinateNew.push(y);
                coordinateNew.push(x);

                /*组装地图弹窗显示内容begin*/
                var mapShowColumns = newTableOperation.popupColumns;
                var content = [];
                var ONE_COLUMN_LENGTH = 11;// 地图弹窗第一列显示字段数量
                var TWO_COLUMN_LENGTH = 14;// 地图弹窗第二列显示字段数量
                var showHtml = "<div id='basicStatusInformation' style='padding:0px;'><input id='infowindowShowVid' value='" + mapPopupInfo.monitorId + "' hidden>";
                content.push(showHtml);
                for (var i = 0; i < mapShowColumns.length; i++) {
                    var item = mapShowColumns[i];
                    if (i < ONE_COLUMN_LENGTH) {
                        amapOperation.renderContent(item, mapPopupInfo, content);
                    }
                }
                //轨迹跟踪点名
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                var deviceType = vehicle[27] ? vehicle[27] : vehicle[31];
                var protocolType = vehicle[31];//协议类型
                var state = vehicle[29];//在线状态
                var protocolTypeArr = ['0', '1', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24', '25', '26', '27', '28'];
                if (typeof deviceType === 'number') {
                    deviceType = deviceType.toString();
                }
                if (protocolTypeArr.indexOf(deviceType) !== -1) {
                    var html = '<div class="infoWindowSetting">' +
                        '<a class="col-md-2" id="jumpTo" onClick="window.amapOperation.jumpToTrackPlayer(\'' + vehicleId + '\')">' +
                        '<img src="../../resources/img/v-track.svg" style="height:28px;width:28px;"/>轨迹' +
                        '</a>';
                    var protocolTypeArr = ['1', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24', '25', '26', '27', '28'];
                    if ((protocolTypeArr.indexOf(protocolType) !== -1) && state != '3') {//交通部JT/808-2013/2019协议且车辆在线
                        html += '<a class="col-md-2 traceTo" onClick="fenceOperation.goTrace(\'' + vehicle[13] + '\')">' +
                            '<img src="../../resources/img/whereabouts.svg" style="height:28px;width:28px;"/>跟踪' +
                            '</a>' +
                            '<a class="col-md-2 callName" onClick="treeMonitoring.callName_(\'' + vehicle[13] + '\')">' +
                            '<img src="../../resources/img/v-named.svg" style="height:28px;width:28px;"/>点名' +
                            '</a>' +
                            '<a class="col-md-2 callName" onClick="treeMonitoring.jumpToRealTimeVideoPage(\'' + vehicleId + '\',\'' + monitorName + '\')">' +
                            '<img src="../../resources/img/video_info_jump.svg" style="height:28px;width:28px;"/>视频' +
                            '</a>';
                    }
                    if (mapShowColumns.length > 11) {
                        html += '<a class="col-md-2 text-right pull-right" style="padding-top:24px;">' +
                            '<i class="fa fa-chevron-circle-right fa-2x vStatusInfoShowMore" id="vStatusInfoShowMore" onclick="amapOperation.vStatusInfoShow(\'' + vStatusInfoShows + '\',\'' + vehicle[2] + '\',\'' + vehicle[14] + '\',\'' + vehicle[20] + '\')"></i>' +
                            '</a>';
                    }
                    html += '</div>';
                    content.push(html);
                } else if (deviceType == "8" || deviceType == "9" || deviceType == "10") {
                    var html = '<div class="infoWindowSetting">' +
                        '<a class="col-md-3" id="jumpTo" onClick="window.amapOperation.jumpToTrackPlayer(\'' + vehicleId + '\')">' +
                        '<img src="../../resources/img/v-track.svg" style="height:28px;width:28px;"/>轨迹' +
                        '</a>' +
                        '<a class="col-md-3 traceTo" onClick="fenceOperation.goF3Trace(\'' + vehicle[13] + '\')">' +
                        '<img src="../../resources/img/whereabouts.svg" style="height:28px;width:28px;"/>跟踪' +
                        '</a>';

                    if (protocolType == '1' && state != '3') {//交通部JT/808-2013协议
                        html += '<a class="col-md-2 callName" onClick="treeMonitoring.jumpToRealTimeVideoPage(\'' + vehicleId + '\',\'' + monitorName + '\')">' +
                            '<img src="../../resources/img/video_info_jump.svg" style="height:28px;width:28px;"/>视频' +
                            '</a>';
                    }
                    if (mapShowColumns.length > 11) {
                        html += '<a class="col-md-3 text-right pull-right" style="padding-top:24px;">' +
                            '<i class="fa fa-chevron-circle-right fa-2x vStatusInfoShowMore" id="vStatusInfoShowMore" onclick="amapOperation.vStatusInfoShow(\'' + vStatusInfoShows + '\',\'' + vehicle[2] + '\',\'' + vehicle[14] + '\',\'' + vehicle[20] + '\')"></i>' +
                            '</a>';
                    }
                    html += '</div>';
                    content.push(html);
                } else {
                    var html = '<div class="infoWindowSetting">' +
                        '<a class="col-md-3" id="jumpTo" onClick="window.amapOperation.jumpToTrackPlayer(\'' + vehicleId + '\')">' +
                        '<img src="../../resources/img/v-track.svg" style="height:28px;width:28px;"/>轨迹' +
                        '</a>';
                    if (protocolType == '1' && state != '3') {//交通部JT/808-2013协议
                        html += '<a class="col-md-2 callName" onClick="treeMonitoring.jumpToRealTimeVideoPage(\'' + vehicleId + '\',\'' + monitorName + '\')">' +
                            '<img src="../../resources/img/video_info_jump.svg" style="height:28px;width:28px;"/>视频' +
                            '</a>';
                    }
                    if (mapShowColumns.length > 11) {
                        html += '<a class="col-md-3 text-right pull-right" style="padding-top:24px;">' +
                            '<i class="fa fa-chevron-circle-right fa-2x vStatusInfoShowMore" id="vStatusInfoShowMore" onclick="amapOperation.vStatusInfoShow(\'' + vStatusInfoShows + '\',\'' + vehicle[2] + '\',\'' + vehicle[14] + '\',\'' + vehicle[20] + '\')"></i>' +
                            '</a>';
                    }
                    html += '</div>';
                    content.push(html);
                }
                content.push("</div>");
                var TWO_START_INDEX = ONE_COLUMN_LENGTH + TWO_COLUMN_LENGTH;// 第二列结束索引
                if (mapShowColumns.length > ONE_COLUMN_LENGTH) {
                    content.push("<div class='mapInfoDetail centerMapInfoDetail'>");
                    for (var i = ONE_COLUMN_LENGTH; i < mapShowColumns.length; i++) {
                        var item = mapShowColumns[i];
                        if (i < TWO_START_INDEX) {
                            amapOperation.renderContent(item, mapPopupInfo, content);
                        }
                    }
                    content.push("<div><span id='bombBox0'></span></div></div>");
                }
                if (mapShowColumns.length > TWO_START_INDEX) {
                    content.push("<div class='mapInfoDetail rightMapInfoDetail'>");
                    for (var i = TWO_START_INDEX; i < mapShowColumns.length; i++) {
                        var item = mapShowColumns[i];
                        amapOperation.renderContent(item, mapPopupInfo, content);
                    }
                    content.push('</div>');
                }
                /*组装地图弹窗显示内容end*/


                // 获取之前的订阅对象数据长度
                var subscribeObjOldLength = markerAllUpdateData.values().length;
                // 删除对应监控对象以前的数据
                if (markerAllUpdateData.containsKey(vehicle[13])) {
                    markerAllUpdateData.remove(vehicle[13]);
                }

                // 组装监控对象需要保存的信息
                var objSaveInfo = [
                    vehicle[13], // 监控对象ID
                    vehicle[0], // 监控对象名称
                    vehicle[12], // 经度
                    vehicle[11], // 纬度
                    vehicle[24], // 角度
                    vehicle[29], // 状态
                    vehicle[25], // 图标
                    vehicle[10], // 时间
                    vehicle[5], // 里程
                    vehicle[30], //监控对象类型
                    vehicle[7], // 速度
                ];

                var updateInfo = [
                    objSaveInfo,
                    content
                ];

                markerAllUpdateData.put(vehicle[13], updateInfo);

                // 获取现在的订阅对象数据长度
                var subscribeObjNowLength = markerAllUpdateData.values().length;

                // 针对区域查询后，监控对象的聚合显示
                if (zoomLevel < 11 && subscribeObjNowLength != subscribeObjOldLength) {
                    amapOperation.markerStateListening();
                }

                // 判断是否是订阅的第一个对象
                if (markerViewingArea.size() == 0 && (zoomLevel >= 11 || map.currentMap === 'baidu') && markerAllUpdateData.size() == 1) {
                    amapOperation.createMarker(objSaveInfo, content, !isAreaSearch);
                    isAreaSearch = false;
                } else {
                    // 判断当前位置点是否在可视区域内且层级大于11
                    if (paths && (paths.contains(coordinateNew) || vehicle[13] == markerFocus || vehicle[13] == centerMarkerId) && zoomLevel >= 11) {
                        if (markerViewingArea.containsKey(vehicle[13])) { // 判断是否含有该id数据
                            var value = markerViewingArea.get(vehicle[13]);
                            var marker = value[0];
                            marker.extData = vehicle[13]; // 监控对象id
                            marker.stateInfo = vehicle[29]; // 监控对象状态
                            marker.content = content.join(""); // 监控对象信息弹窗

                            var markerLngLat = [vehicle[12], vehicle[11]];
                            var timeOld = (new Date(vehicle[10].replace(/-/g, '/'))).getTime();//获得时间（毫秒）
                            markerViewingArea.remove(vehicle[13]);
                            value[0] = marker;
                            value[1].push(markerLngLat);
                            value[2] = content;
                            value[3].push(vehicle[5]);
                            value[4].push(timeOld);
                            value[6] = vehicle[29];
                            value[8].push(vehicle[24]);
                            value[9].push(vehicle[7]);
                            amapOperation.carNameEvade(vehicle[13], vehicle[0], marker.getPosition(), null, '0', null, false, vehicle[29]);
                            markerViewingArea.put(vehicle[13], value);
                            // 监控对象进行移动
                            amapOperation.markerMoveFun(objSaveInfo);
                        } else { // 创建监控对象图标
                            amapOperation.createMarker(objSaveInfo, content, false);
                        }
                    } else {
                        amapOperation.saveMarkerOutsideInfo(objSaveInfo, content);
                    }
                }
            }
        },//1
        // 点名操作
        callTheRollFun: function () {
            if (markerViewingArea.containsKey(callTheRollId)) {
                var value = markerViewingArea.get(callTheRollId);
                var positions = value[1];
                if (positions.length > 1) {
                    markerViewingArea.remove(callTheRollId);
                    var marker = value[0];
                    marker.stopMove();
                    value[1].splice(1, value[1].length - 2);
                    value[3].splice(1, value[3].length - 2);
                    value[4].splice(1, value[4].length - 2);
                    value[8].splice(1, value[8].length - 2);
                    value[9].splice(1, value[9].length - 2);
                    markerViewingArea.put(callTheRollId, value);

                    marker.moveTo(value[1][1], 10000);
                    // 判断监控对象是否已经绑定了移动监听事件
                    // if (!marker.af.moving) {
                    // 绑定移动监听事件
                    marker.on('moving', function (e) {
                        amapOperation.markerMovingFun(e, callTheRollId)
                    });
                    // }
                    // 判断监控对象是否已经绑定了移动结束事件
                    // if (!marker.af.moveend) {
                    // 绑定移动监听事件
                    marker.on('moveend', function (e) {
                        amapOperation.markerMoveendFun(e, callTheRollId)
                    });
                    // }
                }
                callTheRollId = null;
            }
        },
        // 创建监控对象图标
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


            //创建监控对象图标
            var marker = amapOperation.carNameEvade(
                info[0],
                info[1],
                markerLngLat,
                true,
                info[9],
                info[6],
                false,
                info[5],
            );
            // 监控对象添加字段
            marker.setAngle(angle);
            marker.extData = info[0]; // 监控对象id
            marker.stateInfo = info[5]; // 监控对象状态
            marker.content = content.join(""); // 监控对象信息弹窗
            marker.on('click', amapOperation.markerClick);
            if (markerViewingArea.size() == 0 && isFocus) {
                map.setZoomAndCenter(18, markerLngLat, true);//将这个点设置为中心点和缩放级别
                amapOperation.LimitedSize(6);// 第一个点限制范围
            }
            var timeOld = (new Date(info[7].replace(/-/g, '/'))).getTime();//获得时间（毫秒）
            var markerList = [
                marker, // marker
                [markerLngLat], // 坐标
                content, // 信息弹窗信息
                [info[8]], // 里程
                [timeOld], // 时间
                '0', // ?
                info[5], // 车辆状态
                info[6], // 车辆图标
                [info[4]], // 角度
                [info[10]], // 速度
            ];
            markerViewingArea.put(info[0], markerList);
        },
        // 保存可以区域外的监控对象信息
        saveMarkerOutsideInfo: function (info, content) {
            var id = info[0];
            // 删除可视区域内的信息
            if (markerViewingArea.containsKey(id)) {
                var marker = markerViewingArea.get(id)[0];
                var carNameMarker = carNameMarkerMap.get(id);
                var coordinateNew = [info[2], info[3]];
                if (map.currentMap !== 'baidu' || (map.currentMap === 'baidu' && paths && !paths.contains(coordinateNew))) {
                    marker.stopMove();
                    map.remove([marker, carNameMarker]);
                    markerViewingArea.remove(id);
                    carNameMarkerMap.remove(id);
                }

            }

            var markerLngLat = [info[2], info[3]]; // 经纬度
            // var angle = Number(info[24]) + 270; // 角度
            var timeOld = info[7] == null ? info[7] : (new Date(info[7].replace(/-/g, '/'))).getTime();//获得时间（毫秒）

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
        shouldJump: function (distance, timeDiff) {
            // 判断如果网页为隐藏状态，跳点
            if (document.hidden) {
                return true;
            }
            // 判断时间差如果大于等于2分钟，跳点
            if (timeDiff > 2) {
                return true;
            }
            var zoom = Math.floor(map.getZoom());
            zoom = parseInt(zoom);
            var threshold;
            switch (zoom) {
                case 17:
                    threshold = 50;
                    break;
                case 16:
                    threshold = 100;
                    break;
                case 15:
                    threshold = 200;
                    break;
                case 14:
                    threshold = 300;
                    break;
                case 13:
                    threshold = 500;
                    break;
                case 12:
                    threshold = 1000;
                    break;
                case 11:
                    threshold = 2500;
                    break;
                case 10:
                    threshold = 5000;
                    break;
                case 9:
                    threshold = 10000;
                    break;
                case 8:
                    threshold = 25000;
                    break;
                case 7:
                    threshold = 50000;
                    break;
                case 6:
                    threshold = 80000;
                    break;
                case 5:
                    threshold = 100000;
                    break;
                case 4:
                    threshold = 250000;
                    break;
                case 3:
                    threshold = 500000;
                    break;
                case 2:
                    threshold = 500000;
                    break;
                case 1:
                    threshold = 500000;
                    break;
                default:
                    threshold = 0;
                    break;
            }
            if (distance < threshold) {
                return true;
            } else {
                return false;
            }
        },
        // 监控对象进行移动
        markerMoveFun: function (info) {
            var id = info[0];
            var value = markerViewingArea.get(id);
            // 判断监控对象存储了多少个经纬度坐标，超过2个就暂时不移动
            var time = value[4][value[4].length - 1];
            var now = new Date();
            var nowStr = now.getHours() + ':' + now.getMinutes() + ':' + now.getSeconds();
            var timeDate = new Date(time);
            var timeStr = timeDate.getHours() + ':' + timeDate.getMinutes() + ':' + timeDate.getSeconds();
            if (window.openLog) {
                var text = map.text({
                    map: map,
                    position: value[1][value[1].length - 1],
                    text: nowStr + '-' + timeStr,
                    zIndex: 9999
                })
            }
            if (document.hidden) {
                value[0].stopMove();
                for (var i = 0; i < value[1].length - 1; i++) {
                    // 更新尾迹
                    var p = map.lngLat(value[1][i][0], value[1][i][1]);
                    if (map.lnglatTransFun) {// 经纬度转换
                        var newPoint = map.lnglatTransFun(p.lng, p.lat);
                        p = {
                            lng: newPoint[0],
                            lat: newPoint[1]
                        }
                    }

                    fenceOperation.updateFollowPath(id, p);
                }
                value[1] = [value[1][value[1].length - 2], value[1][value[1].length - 1]];
                value[3] = [value[3][value[3].length - 2], value[3][value[3].length - 1]];
                value[4] = [value[4][value[4].length - 2], value[4][value[4].length - 1]];
                value[8] = [value[8][value[8].length - 2], value[8][value[8].length - 1]];
                amapOperation.markerJumpPoint(id, value);
                return;
            }
            if (value[1].length > 2 && flagSwitching) {
                // 压点数大于1个,判断当前点与最后一个点时间间隔大于等于设置时间（1-31秒）,则直接跳到最后一个点Z位置
                var presentTime = value[4][1] / 1000;
                var lastTime = value[4][value[4].length - 1] / 1000;
                var jumpSecond = 31;
                var jumpPointSetting = window.localStorage.getItem('jumpPointSetting');
                var username = $('#userName').text();
                if (jumpPointSetting) {
                    jumpPointSetting = JSON.parse(jumpPointSetting);
                    jumpSecond = jumpPointSetting[username] || 31;
                }
                if (lastTime - presentTime >= Number(jumpSecond)) {
                    markerViewingArea.remove(id);
                    var len = value[1].length - 2;
                    value[1].splice(1, len);
                    value[3].splice(1, len);
                    value[4].splice(1, len);
                    value[8].splice(1, len);
                    value[9].splice(1, len);
                    markerViewingArea.put(id, value);
                    value[0].stopMove();
                    amapOperation.markerJumpPoint(id, value);
                }
            } else if (value[1].length === 2) {
                // 平滑移动
                if (flagSwitching) {
                    var presentPoint = value[1][0];
                    var moveToPoint = value[1][1];
                    // 判断如果两个点的经纬度相等，不执行移动事件且删除经纬度点
                    if (presentPoint[0] == moveToPoint[0] && presentPoint[1] == moveToPoint[1]) {
                        markerViewingArea.remove(id);
                        value[1].splice(0, 1);
                        value[3].splice(0, 1);
                        value[4].splice(0, 1);
                        value[8].splice(0, 1);
                        value[9].splice(0, 1);
                        markerViewingArea.put(id, value);
                    } else if (amapOperation.getMinuteDiff(value[4][0], value[4][1]) > 2) { //  两点时间间隔大于2分钟
                        amapOperation.markerJumpPoint(id, value);
                    } else {
                        var moveMarker = value[0];
                        var distance = amapOperation.calcDistance(presentPoint, moveToPoint);
                        // var speed = amapOperation.markerMoveSpeedByLngLat(distance, value[4]); // marker移动速度
                        var timeDiff = (Number(value[4][1]) - Number(value[4][0])) / 1000 / 60; // 分钟
                        // if (isNaN(speed)) {
                        //     speed = 50;
                        // }
                        var speed = value[9][1];
                        if (amapOperation.shouldJump(distance, timeDiff)) { // 两点间距离是否达到跳点要求
                            amapOperation.markerJumpPoint(id, value);
                        } else {
                            // 速度小于5或者大于230时，进行跳点
                            // 速度大于等于5时，监控对象图标方向为移动方向，否则方向不变
                            if (speed < 5 || speed > 230) {
                                amapOperation.markerJumpPoint(id, value);
                            } else {
                                var time = value[4];
                                var markerTime = time ? (Number(time[1]) - Number(time[0])) : 0;
                                var startPos = moveMarker.getPosition();
                                var angle = amapOperation.calcAngle(startPos, moveToPoint) + 360;
                                moveMarker.setAngle(angle);
                                moveMarker.moveTo(moveToPoint, {
                                    duration: markerTime,
                                    delay: 100,
                                    autoRotation: true
                                });
                            }
                        }
                        // 绑定移动监听事件
                        moveMarker.on('moving', function (e) {
                            amapOperation.markerMovingFun(e, id)
                        });
                        // 绑定移动监听事件
                        moveMarker.on('moveend', function (e) {
                            amapOperation.markerMoveendFun(e, id)
                        });
                    }
                } else { // 跳点
                    amapOperation.markerJumpPoint(id, value);
                }
            }
        },
        // 获取两个点旋转角度
        calcAngle: function (startPos, endPos) {
            const p_start = map.lngLatToContainer(startPos);
            const p_end = map.lngLatToContainer(endPos);
            const diff_x = p_end.x - p_start.x;
            const diff_y = p_end.y - p_start.y;
            return 360 * Math.atan2(diff_y, diff_x) / (2 * Math.PI);
        },
        // 跳点运动
        markerJumpPoint: function (id, value) {
            var marker = value[0];
            var movePosition = value[1][1];
            var startPos = marker.getPosition();

            // var distance = amapOperation.calcDistance([startPos.lng, startPos.lat], movePosition);
            // var speed = amapOperation.markerMoveSpeedByLngLat(distance, value[4]); // marker移动速度

            // 设置图标角度
            var angle = amapOperation.calcAngle(startPos, movePosition) + 360;
            if (icoUpFlag) {
                angle = 0;
            }
            // 速度大于等于5，设置方向
            if (value[9][1] >= 5) {
                marker.setAngle(angle);
            }
            marker.setPosition(movePosition);
            // 判断跳到指定点是否超出范围
            if (!pathsTwo.contains(movePosition) && markerFocus == marker.extData) {
                map.setCenter(movePosition, true);
                amapOperation.pathsChangeFun();
                amapOperation.LimitedSizeTwo();
            }
            // 四维地图特殊处理聚焦跟踪
            if (markerFocus == marker.extData && map.currentMap === 'nglpMap') {
                var newStartPos = marker.getPosition();
                var mapZoom = Math.floor(map.getZoom());
                // 四维地图使用始终强制地图聚焦方法
                map.setZoomAndCenter(mapZoom, newStartPos, true); // 将这个点设置为中心点和缩放级别
            }
            // 删除集合中已经走完的点
            markerViewingArea.remove(id);
            value[1].splice(0, 1);
            value[3].splice(0, 1);
            value[4].splice(0, 1);
            value[8].splice(0, 1);
            value[9].splice(0, 1);
            markerViewingArea.put(id, value);
            //车牌避让
            amapOperation.carNameEvade(
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
                amapOperation.markerJumpPoint(id, newValue);
            }
        },
        // 监控对象移动监听事件
        markerMovingFun: function (e, id) {
            if (!id || !markerViewingArea.get(id)) return;
            var marker = markerViewingArea.get(id)[0];
            // 车牌向上
            if (icoUpFlag) {
                marker.setAngle(0);
            }
            amapOperation.carNameEvade(
                marker.extData,
                marker.name,
                marker.getPosition(),
                null,
                "0",
                null,
                false,
                marker.stateInfo
            );
            // 判断是否为聚焦跟踪监控对象
            if (markerFocus == marker.extData) {
                var msg = marker.getPosition();
                if (!pathsTwo.contains(msg)) {
                    map.setCenter(msg, true);
                    amapOperation.LimitedSizeTwo();
                }
            }
        },
        // 监控对象移动结束事件
        markerMoveendFun: function (e, id) {
            if (!id) return;
            var marker = markerViewingArea.get(id)[0];
            var position = marker.getPosition();
            // 更新尾迹
            fenceOperation.updateFollowPath(id, position);
            amapOperation.carNameEvade(
                marker.extData,
                marker.name,
                position,
                false,
                '0',
                null,
                false,
                marker.stateInfo
            );

            var value = markerViewingArea.get(id);
            if (value[1].length < 2) {
                return;
            }

            markerViewingArea.remove(id);
            value[1].splice(0, 1);
            value[3].splice(0, 1);
            value[4].splice(0, 1);
            value[8].splice(0, 1);
            value[9].splice(0, 1);
            markerViewingArea.put(id, value);
            // 判断行驶结束后
            if (value[1].length > 1) {
                if (flagSwitching) {
                    /*// 如果移动结束后还剩余3个点及以上，直接跳到最后一个点
                    if (value[1].length >= 3 || document.hidden) {
                        for (var i = 0; i < value[1].length - 1; i++) {
                            // 更新尾迹
                            var p = map.lngLat(value[1][i][0], value[1][i][1]);
                            if (map.lnglatTransFun) {// 经纬度转换
                                var newPoint = map.lnglatTransFun(p.lng, p.lat);
                                p = {
                                    lng: newPoint[0],
                                    lat: newPoint[1]
                                }
                            }
                            fenceOperation.updateFollowPath(id, p);
                        }
                        value[1] = [value[1][value[1].length - 2], value[1][value[1].length - 1]];
                        value[3] = [value[3][value[3].length - 2], value[3][value[3].length - 1]];
                        value[4] = [value[4][value[4].length - 2], value[4][value[4].length - 1]];
                        value[8] = [value[8][value[8].length - 2], value[8][value[8].length - 1]];
                        amapOperation.markerJumpPoint(id, value);
                    } else {*/
                    // 监控对象图标持续移动
                    amapOperation.markerContinueMoving(id);
                    // }
                } else {
                    // 跳点
                    var newValue = markerViewingArea.get(id);
                    amapOperation.markerJumpPoint(id, newValue);
                }
            }
        },
        // 监控对象图标持续移动
        markerContinueMoving: function (id) {
            var value = markerViewingArea.get(id);

            var presentPoint = value[1][0];
            var moveToPoint = value[1][1];

            // 判断如果两个点的经纬度相等，不执行移动事件且删除经纬度点
            if (presentPoint[0] == moveToPoint[0] && presentPoint[1] == moveToPoint[1]) {
                markerViewingArea.remove(id);
                value[1].splice(0, 1);
                value[3].splice(0, 1);
                value[4].splice(0, 1);
                value[8].splice(0, 1);
                value[9].splice(0, 1);
                markerViewingArea.put(id, value);
            } else {
                var moveMarker = value[0];
                var distance = amapOperation.calcDistance(presentPoint, moveToPoint);
                // var speed = amapOperation.markerMoveSpeedByLngLat(distance, value[4]); // marker移动速度
                var timeDiff = (Number(value[4][1]) - Number(value[4][0])) / 1000 / 60; // 分钟
                if (amapOperation.shouldJump(distance, timeDiff)) {
                    amapOperation.markerJumpPoint(id, value);
                } else {
                    var speed = value[9][1];
                    // 判断如果速度大于等于 230 km/h，不执行移动事件且删除经纬度点
                    if (speed > 230 || speed < 5) {
                        amapOperation.markerJumpPoint(id, value);
                    } else {
                        var time = value[4];
                        var markerTime = time ? (Number(time[1]) - Number(time[0])) : 0;
                        var angle = amapOperation.calcAngle(presentPoint, moveToPoint) + 360;
                        moveMarker.setAngle(angle);
                        moveMarker.moveTo(moveToPoint, {
                            duration: markerTime,
                            delay: 100,
                            autoRotation: true
                        });
                    }
                }
            }
        },
        // 计算两点相差分钟，毫秒
        getMinuteDiff: function (before, later) {
            return (later - before) / 1000 / 60;
        },
        // 计算marker移动速度
        markerMoveSpeed: function (mileage, time) {
            var speed;
            if (mileage != null) {
                var markerMileage = Number(mileage[1]) - Number(mileage[0]);
                var markerTime = (Number(time[1]) - Number(time[0])) / 1000 / 60 / 60;
                if (markerTime == 0) {
                    speed = 50;
                } else {
                    speed = Number((markerMileage / markerTime).toFixed(2));
                }
            } else {
                speed = 300;
            }
            return speed == 0 ? 100 : speed;
        },
        calcDistance: function (presentPoint, moveToPoint) {
            var presentLngLat = map.lngLat(presentPoint[0], presentPoint[1]);
            var moveLngLat = map.lngLat(moveToPoint[0], moveToPoint[1]);

            var distance = presentLngLat.distance(moveLngLat);
            return distance;
        },
        /**
         * 通过两个经纬度和时间计算速度
         * @param distance 距离 米
         * @param time 时间戳数组
         * @returns {number}
         */
        markerMoveSpeedByLngLat: function (distance, time) {
            var speed;
            if (distance != null) {
                var markerMileage = distance / 1000;
                var markerTime = (Number(time[1]) - Number(time[0])) / 1000 / 60 / 60;
                if (markerTime == 0) {
                    speed = 50;
                } else {
                    speed = Number((markerMileage / markerTime).toFixed(2));
                }
            } else {
                speed = 300;
            }
            return speed == 0 ? 100 : speed;
        },
        // 监控对象在地图层级改变或拖拽后状态更新
        markerStateListening: function () {
            var center = map.getCenter();
            window.initCenterPoint = center;
            var mapZoom = map.getZoom();
            // 根据地图层级变化相应改变paths
            amapOperation.pathsChangeFun();
            amapOperation.LimitedSizeTwo();
            // 判断地图层级是否大于等于11
            // 大于等于11：重新计算地图上哪些监控对象在可视区域内||区域外
            // 小于11：进行聚合
            if (map.currentMap === 'baidu') {// 百度聚合有问题,不聚合了
                amapOperation.clusterToCreateMarker();
            } else {
                if (mapZoom >= 11) {
                    // 判断是否是刚从聚合状态切换过来
                    // 如果是就把最新点集合的数据进行创建marker
                    amapOperation.clusterToCreateMarker();
                    if (isCluster) {
                        if (cluster !== undefined) {
                            // cluster.clearMarkers();
                            setTimeout(() => {
                                if (cluster) {
                                    cluster.setMap(null);
                                    cluster = undefined;
                                }
                            }, 0)
                        }
                        isCluster = false;
                    }

                } else {
                    // 刚进入聚合状态，进行清空聚焦车辆
                    if (!isCluster) {
                        isCluster = true;
                        amapOperation.clearFocusObj();
                    }
                    // 清空地图上已创建监控对象图标
                    amapOperation.clearMapForMarker();
                    // 创建地图可视区域聚合点
                    amapOperation.createMarkerClusterer();
                }
            }
        },
        // 清空聚焦车辆
        clearFocusObj: function () {
            markerFocus = null;
            $('#treeDemo li a').removeClass('curSelectedNode_dbClick');
            $('#treeDemo li a').removeClass('curSelectedNode');
            $('#realTimeStateTable tbody tr').removeClass('tableHighlight');
            $('#realTimeStateTable tbody tr').removeClass('tableHighlight-blue');
            dataTableOperation.cancelActiveAndLockRow();

        },
        // 根据地图层级变化相应改变paths
        pathsChangeFun: function () {
            var mapZoom = Math.floor(map.getZoom());

            if (mapZoom === 18) {
                amapOperation.LimitedSize(6);
            } else if (mapZoom === 17) {
                amapOperation.LimitedSize(5);
            } else if (mapZoom === 16) {
                amapOperation.LimitedSize(4);
            } else if (mapZoom === 15) {
                amapOperation.LimitedSize(3);
            } else if (mapZoom === 14) {
                amapOperation.LimitedSize(2);
            } else if (mapZoom <= 13 && mapZoom >= 1) {
                amapOperation.LimitedSize(1);
            }
        },
        // 清空地图上已创建监控对象图标
        clearMapForMarker: function () {
            var values = markerViewingArea.values();
            for (var i = 0, len = values.length; i < len; i++) {
                var marker = values[i][0];
                marker.stopMove();
                map.remove([marker]);
            }
            // 清空map对象
            carNameMarkerMap.clear();
            // 清空可视区域内经纬度集合
            markerViewingArea.clear();
            // 清空可视区域外经纬度集合
            markerOutside.clear();
            // 清空地图上的监控对象名称
            var nameValues = carNameMarkerContentMap.values();
            map.remove(nameValues);
            carNameMarkerContentMap.clear();
        },
        // 创建地图可视区域聚合点
        createMarkerClusterer: function () {
            if (cluster != undefined) {
                // cluster.clearMarkers();
                cluster.setMap(null);
                // cluster.off('click', amapOperation.clusterClickFun);
                cluster = undefined;
            }
            var values = markerAllUpdateData.values();
            if (values.length === 0) return;
            var markerList = [];
            var points = [];
            for (var i = 0, len = values.length; i < len; i++) {
                var markerLngLat = [values[i][0][2], values[i][0][3]];
                var id = values[i][0][0];
                var content = values[i][1];
                /* if (paths.contains(markerLngLat)) {
                     var marker = {
                         position: markerLngLat,
                         icon: "../../resources/img/1.png",
                         offset: map.pixel(-26, -13), //相对于基点的位置
                         autoRotation: true
                     };
                 }
                 marker.lnglat = markerLngLat;
                 marker.extData = id;
                 marker.content = content.join("");
                 // marker.on('click', amapOperation.markerClick);
                 markerList.push(marker);*/
                points.push({
                    lnglat: markerLngLat
                })
            }
            var count = values.length;
            var _renderClusterMarker = function (context) {
                var factor = Math.pow(context.count / count, 1 / 18);
                var div = document.createElement('div');
                var Hue = 180 - factor * 180;
                var bgColor = 'hsla(' + Hue + ',100%,40%,0.7)';
                var fontColor = 'hsla(' + Hue + ',100%,90%,1)';
                var borderColor = 'hsla(' + Hue + ',100%,40%,1)';
                var shadowColor = 'hsla(' + Hue + ',100%,90%,1)';
                div.style.backgroundColor = bgColor;
                var size = Math.round(30 + Math.pow(context.count / count, 1 / 5) * 20);
                div.style.width = div.style.height = size + 'px';
                div.style.border = 'solid 1px ' + borderColor;
                div.style.borderRadius = size / 2 + 'px';
                div.style.boxShadow = '0 0 5px ' + shadowColor;
                div.innerHTML = context.count;
                div.style.lineHeight = size + 'px';
                div.style.color = fontColor;
                div.style.fontSize = '14px';
                div.style.textAlign = 'center';
                context.marker.setOffset(map.pixel(-size / 2, -size / 2));
                context.marker.setContent(div)
            };
            var _renderMarker = function (context) {
                var content = '<div class="markerOneItem">1</div>';
                var offset = map.pixel(-9, -9);
                context.marker.setContent(content);
                context.marker.setOffset(offset)
            };
            cluster = map.markerCluster(map, points, {
                gridSize: 80,
                renderClusterMarker: _renderClusterMarker,
                renderMarker: _renderMarker, // 自定义非聚合点样式
            });
            cluster.on('click', amapOperation.clusterClickFun);
        },
        renderMarker: function (context) {
            context.marker.setIcon("/clbs/resources/img/1.png");
        },
        // 聚合点击事件
        clusterClickFun: function (data) {
            var position = data.lnglat;
            var zoom = map.getZoom();
            if (zoom < 6) {
                map.setZoomAndCenter(6, position);
            } else {
                map.setZoomAndCenter(11, position);
            }

            // return false;
            // amapOperation.markerStateListening();
        },
        // 聚合状态刚消失创建marker
        clusterToCreateMarker: function () {
            var values = markerAllUpdateData.values();
            var zoom = map.getZoom();
            for (var i = 0, len = values.length; i < len; i++) {
                var id = values[i][0][0];
                var markerLngLat = [values[i][0][2], values[i][0][3]];

                if (paths.contains(markerLngLat) || markerFocus == id || centerMarkerId == id || zoom < 6) {
                    if (markerViewingArea.containsKey(id)) {
                        var marker = markerViewingArea.get(id)[0];

                        var info = values[i][0];
                        var carName = info[1];
                        var stateInfo = info[5];
                        if (zoom > 6) {
                            amapOperation.carNameEvade(id, carName, marker.getPosition(), false, '0', null, false, stateInfo);
                        }
                    } else {
                        amapOperation.createMarker(values[i][0], values[i][1], false);
                    }
                }
                else {
                    amapOperation.saveMarkerOutsideInfo(values[i][0], values[i][1]);
                }
            }
        },
        LimitedSizeTwo: function () {
            if (!map) return;
            var southwest = map.getBounds().getSouthWest();
            var northeast = map.getBounds().getNorthEast();
            var mcenter = map.getCenter();                  //获取中心坐标
            if (isNaN(mcenter.lng) && map.currentMap !== 'nglpMap') return;
            var pixel2 = map.lngLatToContainer(mcenter);//根据坐标获得中心点像素
            var mcx = pixel2.getX();                    //获取中心坐标经度像素
            var mcy = pixel2.getY();                    //获取中心坐标纬度像素
            var southwestx = mcx + (mcx * 0.8);
            var southwesty = mcy * 0.2;
            var northeastx = mcx * 0.2;
            var northeasty = mcy + (mcy * 0.8);
            var ll = map.containerToLngLat(map.pixel(southwestx, southwesty));
            var lll = map.containerToLngLat(map.pixel(northeastx, northeasty));
            pathsTwo = map.bounds(
                lll,//东北角坐标
                ll //西南角坐标
            );
        }

        ,
        LimitedSize: function (size) {
            paths = null;
            if (!map) return;
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

            paths = map.bounds(
                [psn, psa], //西南角坐标
                [pnn, pna]//东北角坐标
            )
        }
        ,
//车辆标注点击
        markerClick: function (e) {
            // 终端上报日志updataFenceData
            webSocket.subscribe(headers, '/topic/deviceReportLog', function (data) {
                var msg = JSON.parse(data.body);
                var msgId = msg.desc.msgID;
                var vId = msg.desc.monitorId;
                var curShowVid = $('#infowindowShowVid').val();
                var value = {
                    "waybill": '',
                    "practitioners": null
                };
                if (waybillAndPractitionersInfo.containsKey(vId)) {
                    value = waybillAndPractitionersInfo.get(vId);
                }
                if (msgId == 0x0701) {// 电子运单
                    value.waybill = msg.data.msgBody.data;
                    if (vId == curShowVid) {
                        $("#bombBox7").text("电子运单：" + (value.waybill ? value.waybill : ''));
                    }
                } else if (msgId == 0x0702) {// 从业人员
                    value.practitioners = msg.data.msgBody;
                    if (vId == curShowVid) {
                        $("#bombBox8").text("从业人员：" + (value.practitioners.driverName ? value.practitioners.driverName : ''));
                        $("#cardIDBox").text("身份证号：" + (value.practitioners.driverIdentity ? value.practitioners.driverIdentity : ''));
                        $("#bombBox9").text("从业资格证号：" + (value.practitioners.certificationID ? value.practitioners.certificationID : ''));
                        $("#cANameBox").text("发证机构：" + (value.practitioners.cAName ? value.practitioners.cAName : ''));
                    }
                }
                waybillAndPractitionersInfo.put(vId, value);
            }, null, null);
            var markerLngLat = e.target.getPosition();
            vinfoWindwosClickVid = e.target.extData;
            infoWindow.setContent(e.target.content);
            infoWindow.open(map, markerLngLat, e.target.content, e.target.curMarker);
            markerClickLngLat = markerLngLat;
        },
        jumpToTrackPlayer: function (vid) {
            var jumpFlag = false;
            var permissionUrls = $("#permissionUrls").val();
            if (typeof permissionUrls !== "undefined" && permissionUrls !== null) {
                var urllist = permissionUrls.split(",");
                if (urllist.indexOf("/v/monitoring/trackPlayback") > -1) {
                    var url = "/clbs/v/monitoring/trackPlayBackLog";
                    var data = {"vehicleId": vid, "type": ''};
                    json_ajax("POST", url, "json", false, data, null);
                    setTimeout("dataTableOperation.logFindCilck()", 500);
                    jumpFlag = true;
                    location.href = "/clbs/v/monitoring/trackPlayback?vid=" + vid;
                }
            }
            if (!jumpFlag) {
                layer.msg("无操作权限，请联系管理员");
            }
        }
        ,
// 实时路况点击
        realTimeRC: function () {
            if ($realTimeRC.hasClass("preBlue")) {
                realTimeTraffic.hide();
                $realTimeRC.removeClass("preBlue");
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
                $realTimeRC.addClass("preBlue");
            }
        }
        ,
//卫星地图及3D地图切换
        satelliteMapSwitching: function () {
            if (!window.amapFlag) {
                $('#fourMapContainer').hide()
                $('#MapContainer').show()
                window.amapFlag = true
            }
            if ($("#defaultMap").attr("checked")) {
                satellLayer.hide();
                // buildings.setMap(map);
                // if (googleMapLayer) {
                //     googleMapLayer.setMap(null);
                // }
                roadNet.hide();
                // buildings.setMap(map);
                // if (googleMapLayer) {
                //     googleMapLayer.setMap(null);
                // }
                $("#defaultMap").attr("checked", false);
                $("#defaultMapLab").removeClass("preBlue");
            } else {
                // 判断未切换到谷歌地图直接选择卫星地图时 未初始化问题
                /*if (googleMapLayer) {
                    //取消谷歌地图选中状态
                    googleMapLayer.setMap(null);
                }
                $("#googleMap").attr("checked", false);
                $("#googleMapLab").removeClass("preBlue");*/

                satellLayer.show();
                // buildings.setMap(null);
                roadNet.show();
                // buildings.setMap(null);
                $("#defaultMap").attr("checked", true);
                $("#defaultMapLab").addClass("preBlue");
                //取消四维地图选中状态
                /*$("#fourMap").attr("checked", false);
                $("#fourMapLab").removeClass("preBlue");*/
            }
        }
        ,
//GOOGLE地图
        showGoogleMapLayers: function () {
            if (!window.amapFlag) {
                $('#fourMapContainer').hide()
                $('#MapContainer').show()
                window.amapFlag = true
            }
            if ($("#googleMap").attr("checked")) {
                // googleMapLayer.setMap(null);
                $("#googleMap").attr("checked", false);
                $("#googleMapLab").removeClass("preBlue");
            } else {
                googleMapLayer = map.createDefaultLayer({
                    tileUrl: 'https://mt{1,2,3,0}.google.cn/vt/lyrs=m@142&hl=zh-CN&gl=cn&x=[x]&y=[y]&z=[z]&s=Galil', // 图块取图地址
                    zIndex: 100 //设置Google层级与高德相同  避免高德路况及卫星被Google图层覆盖
                });
                // googleMapLayer.setMap(map);
                $("#googleMap").attr("checked", true);
                $("#googleMapLab").addClass("preBlue");

                //取消路况与卫星选中状态
                $realTimeRC.attr("checked", false);
                $("#realTimeRCLab").removeClass("preBlue");
                realTimeTraffic.hide();
                $("#defaultMap").attr("checked", false);
                $("#defaultMapLab").removeClass("preBlue");
                satellLayer.hide();
                roadNet.hide();
                // buildings.setMap(map);
                //取消四维地图选中状态
                $("#fourMap").attr("checked", false);
                $("#fourMapLab").removeClass("preBlue");
            }
        }
        ,
//四维地图选中
        fourMapClick: async function () {
            //派发缩放事件 解决四维地图下车牌图标没有底色的bug
            var event2 = document.createEvent("HTMLEvents");
            event2.initEvent("resize", false, true);
            window.dispatchEvent(event2);
            if ($("#fourMap").attr("checked")) {
                $("#fourMap").attr("checked", false);
                $("#fourMapLab").removeClass("preBlue");
            } else {
                $("#fourMap").attr("checked", true);
                $("#fourMapLab").addClass("preBlue");

                //取消路况与卫星选中状态
                $realTimeRC.attr("checked", false);
                $("#realTimeRCLab").removeClass("preBlue");
                realTimeTraffic.hide();
                $("#defaultMap").attr("checked", false);
                $("#defaultMapLab").removeClass("preBlue");
                satellLayer.hide();
                // buildings.setMap(map);
                roadNet.hide();
                // buildings.setMap(map);

                if (googleMapLayer) {
                    //取消谷歌地图选中状态
                    googleMapLayer.hide()
                    // googleMapLayer.setMap(null);
                }
                $("#googleMap").attr("checked", false);
                $("#googleMapLab").removeClass("preBlue");
            }
        }
        ,
//工具操作
        toolClickList: function () {
            var id = $(this).attr('id');
            var i = $("#" + id).children('i');
            if (id != 'countClick') {
                mouseTool.close(true);
                amapFunCollection.removeCircleTool();
                $('#measurementMenu .preBlue').removeClass('preBlue');
            }

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
                $('#measurementMenu').slideUp();
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
                $('#measurementMenu').slideUp();
            }
            else if (id == "countClick") {
                if ($('#measurementMenu').is(':hidden')) {
                    $('#measurementMenu').slideDown();
                } else {
                    $('#measurementMenu').slideUp();
                }
                $("#disSetMenu").slideUp();
                $("#mapDropSettingMenu").slideUp();
            }
            else {

                // wjk 加一个通话功能 通话与视频可以同时存在
                // var phoneCall_i = $('#phoneCall').find('i');
                // var video_i = $('#btn-videoRealTime-show').find('i');
                // if (!i.hasClass("active") && id == 'phoneCall' && video_i.hasClass('active') ||
                //     !i.hasClass("active") && id == 'btn-videoRealTime-show' && phoneCall_i.hasClass('active')) {
                //
                //     i.addClass('active');
                //     $("#" + id).children('span.mapToolClick').css('color', '#6dcff6');
                //     mouseTool.close(true);
                // }
                // else
                //end

                if (id == 'btn-videoRealTime-show') {
                    if (!subscribeVehicleInfo) {
                        layer.msg('请双击选择监控对象');
                        return;
                    } else {
                        if (!dataTableOperation.checkVehicleOnlineStatus(subscribeVehicleInfo.vid)) {
                            layer.msg('监控对象离线');
                            return;
                        }
                    }
                }
                if (i.hasClass("active")) {
                    i.removeClass('active');
                    $("#" + id).children('span.mapToolClick').css('color', '#5c5e62');
                    // mouseTool.close(true);

                    if (!$("#toolOperateClick a:not(#btn-videoRealTime-show) i").hasClass('active')) {
                        mouseTool.close(true);
                        amapFunCollection.removeCircleTool();
                        if (map.setStatus) {// 设置高德地图可通过鼠标拖拽平移
                            map.setStatus({
                                dragEnable: true
                            });
                        }
                    }

                } else {
                    $("#toolOperateClick a:not(#btn-videoRealTime-show) i").removeClass('active');
                    $("#toolOperateClick a:not(#btn-videoRealTime-show) span.mapToolClick").css('color', '#5c5e62');
                    i.addClass('active');
                    $("#" + id).children('span.mapToolClick').css('color', '#6dcff6');


                    mouseTool.close(true);
                    amapFunCollection.removeCircleTool();
                }
                if (i.hasClass("active")) {
                    if (id === "magnifyClick") {
                        //拉框放大
                        mouseTool.rectZoomIn();
                    } else if (id == "shrinkClick") {
                        //拉框放小
                        mouseTool.rectZoomOut();
                    }
                    else if (id === "queryClick") {//区域查车
                        isAreaSearchFlag = true;
                        mouseTool.rectangle();
                    }
                }
                if (id == 'btn-videoRealTime-show') {
                    pageLayout.videoRealTimeShow();
                }
            }
        }
        ,
        //车牌号规避
        carNameEvade: function (id, name, lnglat, flag, type, ico, showFlag, stateInfo) {
            //监控对象图片大小
            var value = lnglat;
            var picWidth = 0;
            var picHeight = 0;
            var icons;
            if (type == "0") {
                if (ico == "null" || ico == undefined || ico == null) {
                    icons = "/clbs/resources/img/vehicle.png";
                } else {
                    icons = "/clbs/resources/img/vico/" + ico;
                }
                picWidth = 58 / 2;
                picHeight = 26 / 2;
            } else if (type == "1") {
                if (ico == "null" || ico == undefined || ico == null) {
                    icons = "/clbs/resources/img/123.png";
                } else {
                    icons = "/clbs/resources/img/vico/" + ico;
                }
                picWidth = 30 / 2;
                picHeight = 30 / 2;
            } else if (type == "2") {
                if (ico == "null" || ico == undefined || ico == null) {
                    icons = "/clbs/resources/img/thing.png";
                } else {
                    icons = "/clbs/resources/img/vico/" + ico;
                }
                picWidth = 40 / 2;
                picHeight = 40 / 2;
            }
            if (isCarNameShow) {
                //显示对象姓名区域大小
                var nameAreaWidth = 90;
                var nameAreaHeight = 38;
                //车辆状态没判断
                var carState = amapOperation.stateCallBack(stateInfo);
                var id = id;
                var name = name;
                //判断是否第一个创建
                var markerAngle = 0; //图标旋转角度
                if (carNameMarkerMap.containsKey(id)) {
                    var thisCarMarker = carNameMarkerMap.get(id);
                    /* var ssmarker = map.marker({
                         icon: "https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png",
                         position: [116.41, 39.91]
                     });*/
                    markerAngle = thisCarMarker.getAngle();
                    // var s = ssmarker.getAngle();
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
                var pixelRD = amapOperation.countAnglePX(markerAngle, defaultRD, pixelPX, 1, picWidth, picHeight);
                var pixelRU = amapOperation.countAnglePX(markerAngle, defaultRU, pixelPX, 2, picWidth, picHeight);
                var pixelLU = amapOperation.countAnglePX(markerAngle, defaultLU, pixelPX, 3, picWidth, picHeight);
                var pixelLD = amapOperation.countAnglePX(markerAngle, defaultLD, pixelPX, 4, picWidth, picHeight);
                //四点像素转为经纬度
                var llLU = map.containerToLngLat(map.pixel(pixelLU[0], pixelLU[1]));
                var llRU = map.containerToLngLat(map.pixel(pixelRU[0], pixelRU[1]));
                var llLD = map.containerToLngLat(map.pixel(pixelLD[0], pixelLD[1]));
                var llRD = map.containerToLngLat(map.pixel(pixelRD[0], pixelRD[1]));
                //车牌显示位置左上角PX
                var nameRD_LU = [pixelRD[0], pixelRD[1]];
                var nameRU_LU = [pixelRU[0], pixelRU[1] - nameAreaHeight];
                var nameLU_LU = [pixelLU[0] - nameAreaWidth, pixelLU[1] - nameAreaHeight];
                var nameLD_LU = [pixelLD[0] - nameAreaWidth, pixelLD[1]];
                //分别将上面四点转为经纬度
                var llNameRD_LU = map.containerToLngLat(map.pixel(nameRD_LU[0], nameRD_LU[1]));
                var llNameRU_LU = map.containerToLngLat(map.pixel(nameRU_LU[0], nameRU_LU[1]));
                var llNameLU_LU = map.containerToLngLat(map.pixel(nameLU_LU[0], nameLU_LU[1]));
                var llNameLD_LU = map.containerToLngLat(map.pixel(nameLD_LU[0], nameLD_LU[1]));
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
                var isConfirm = map.currentMap === 'aMap' ? true : false;
                var mapPixel;
                var LUPX;
                var showLocation;
                if (isOneArea) {
                    mapPixel = llRD;
                    LUPX = llNameRD_LU;
                    offsetCarName = map.pixel(0, 0);
                    isConfirm = false;
                    showLocation = "carNameShowRD";
                } else if (isConfirm && isTwoArea) {
                    mapPixel = llRU;
                    LUPX = llNameRU_LU;
                    offsetCarName = map.pixel(0, -nameAreaHeight);
                    isConfirm = false;
                    showLocation = "carNameShowRU";
                } else if (isThreeArea && isConfirm) {
                    mapPixel = llLU;
                    LUPX = llNameLU_LU;
                    offsetCarName = map.pixel(-nameAreaWidth, -nameAreaHeight);
                    isConfirm = false;
                    showLocation = "carNameShowLU";
                } else if (isFourArea && isConfirm) {
                    mapPixel = llLD;
                    LUPX = llNameLD_LU;
                    offsetCarName = map.pixel(-nameAreaWidth, 0);
                    isConfirm = false;
                    showLocation = "carNameShowLD";
                }
                ;
                if (mapPixel == undefined) {
                    mapPixel = llRD;
                    LUPX = llNameRD_LU;
                    offsetCarName = map.pixel(0, 0);
                    showLocation = "carNameShowRD";
                }
                if (!mapPixel) {
                    showLocation = "carNameShowRU";
                }
            }
            ;
            if (flag != null) {
                if (flag) {//创建marker
                    //车辆
                    var markerLocation = carNameMarkerMap.get(id);
                    if (!showFlag && !markerLocation) {

                        // var startIcon = map.icon({
                        //     // 图标尺寸
                        //     size: map.size(50, 35),
                        //     image: icons,
                        //     // 图标所用图片大小
                        //     imageSize: map.size(48, 30),
                        //     // 图标取图偏移量
                        //     // imageOffset: map.pixel(0, 0)
                        // });

                        markerLocation = map.marker({
                            position: value,
                            icon: icons,
                            offset: map.pixel(-picWidth, -picHeight), //相对于基点的位置
                            autoRotation: true,//自动调节图片角度
                            map: map,
                            name: name,
                            id: id,
                            state: carState
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
                            carNameContentLUMap.remove(id);
                        }
                        ;
                        var markerContent = map.marker({
                            position: mapPixel && !isNaN(mapPixel.lng) ? mapPixel : value,
                            monitorPos: value,
                            content: carContent,
                            offset: offsetCarName,
                            autoRotation: true,//自动调节图片角度
                            map: map,
                            zIndex: 999,
                            id: id
                        });
                        carNameMarkerContentMap.put(id, markerContent);
                        carNameContentLUMap.put(id, LUPX);
                        if (isConfirm) {
                            markerContent.hide(true);
                            carNameContentLUMap.remove(id);
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
                                carContent.hide(true);
                                carNameContentLUMap.remove(id);
                            } else {
                                // map.remove([carContent]);
                                carContent.show();
//                            carNameMarkerContentMap.remove(id);
//                            var markerContent = map.marker({
//                                position: mapPixel,
//                                content: carContentHtml,
//                                offset: offsetCarName,
//                                autoRotation: true,//自动调节图片角度
//                                map: map,
//                                zIndex: 999
//                            });
//                            markerContent.setMap(map);
//                            carNameMarkerContentMap.put(id, markerContent);

                                carContent.setContent(carContentHtml);
                                carContent.setPosition(mapPixel && !isNaN(mapPixel.lng) ? mapPixel : value);
                                carContent.setOffset(offsetCarName);
                                carNameContentLUMap.put(id, LUPX);
                            }
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
                            thisMoveMarker.hide(true);
                        } else {
                            thisMoveMarker.show();
                            thisMoveMarker.setContent(carContentHtml);
                            thisMoveMarker.setPosition(mapPixel && !isNaN(mapPixel.lng) ? mapPixel : value);
                            thisMoveMarker.setOffset(offsetCarName);
                        }
                        carNameContentLUMap.put(id, LUPX);
                    }
                    ;
                }
                ;
            }
            ;
        }
        ,
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
        }
        ,
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
        }
        ,
//重新设置区域
        setCarNameCircle: function () {
            var markerMapValue = markerMap.values();
            if (markerMapValue != undefined) {
                //清空车牌号显示位置信息
                carNameContentLUMap.clear();
                for (var i = 0; i < markerMapValue.length; i++) {
                    var carId = markerMapValue[i][0].extData;
                    var carName = markerMapValue[i][0].name;
                    var stateInfo = markerMapValue[i][0].stateInfo;
                    var lngLatValue = markerMapValue[i][0].getPosition();
                    //
                    if (isCarNameShow) {
                        if (markerMapValue[i][5] == "1") {
                            amapOperation.carNameEvade(carId, carName, lngLatValue, false, "1", null, false, stateInfo);
                        } else {
                            amapOperation.carNameEvade(carId, carName, lngLatValue, false, "0", null, false, stateInfo);
                        }
                    }
                    ;
                }
                ;
            }
            ;
        }
        ,
//清空所有content marker的value值
        clearContentValue: function () {
            if (!carNameMarkerContentMap.isEmpty()) {
                var contentValue = carNameMarkerContentMap.values();
                map.remove(contentValue);
                carNameMarkerContentMap.clear();
            }
            ;
        }
        ,
        vStatusInfoShow: function (data, group, people, alam) {
            //获取当前车辆点击的经纬度
            var currentCarCoordinate = "";
            if (map.getZoom() >= 11) {
                currentCarCoordinate = (markerViewingArea.get(vinfoWindwosClickVid))[0].getPosition();
            } else {
                currentCarCoordinate = markerClickLngLat;
            }
            //点击时判断是否显示信息框
            if ($(".mapInfoDetail").is(":hidden")) {
                //执行显示
                // $("#basicStatusInformation").removeAttr("class");
                // $("#basicStatusInformation").addClass("col-md-4");
                var len = $('.amap-info-contentContainer .mapInfoDetail').length;
                var parentWidth = len === 1 ? '424px' : '620px';
                if (len === 2) {
                    $(".centerMapInfoDetail").addClass('centerMapInfo');
                    $(".rightMapInfoDetail").css("border", "none");
                }
                $(".amap-info-contentContainer .amap-info-content").css("width", parentWidth);
                $(".tdt-infowindow-content").css("width", parentWidth);
                if (infoWindow.setWidth) {
                    infoWindow.setWidth(len === 1 ? 420 : 615);
                } else {
                    infoWindow.setPosition(currentCarCoordinate);
                }
                $("#vStatusInfoShowMore").removeClass("fa-chevron-circle-right").addClass("fa-chevron-circle-left");
                $(".mapInfoDetail").show();
                //执行信息框底部移动方法
                amapOperation.amapInfoSharpAdaptiveFn();
                //执行信息框整体基点位置方法
                $("#basicStatusInformation").css({"width": "158px", "margin-right": "20px"});
                //加入数据
                var dataList = data.split(",");

                var num = +dataList[17];
                var dataa = num.toString(2);
                dataa = (Array(32).join(0) + dataa).slice(-32);//高位补零
                if (dataList[16] == 1) {
                    $("#bombBox0").text("单次回报应答");
                }

                // 取出报警集合中等同于当前车Id的报警信息,赋值到当前车辆信息集合
                var newAlarmName = alam;
                if (alarmInfoList.get(dataList[12]) != undefined) {
                    newAlarmName = alarmInfoList.get(dataList[12]);
                }
                $(".popup_alarmType").text("报警类型：" + newAlarmName);

                if (dataa.substring(29, 30) == 0) {
                    $("#bombBox2").text("北纬：" + dataList[10]);
                } else if (dataa.substring(30, 31) == 1) {
                    $("#bombBox2").text("南纬：" + dataList[10]);
                }
                ;
                if (dataa.substring(28, 29) == 0) {
                    $("#bombBox3").text("东经：" + dataList[11]);
                } else if (dataa.substring(28, 29) == 1) {
                    $("#bombBox3").text("西经：" + dataList[11]);
                }

                $("#bombBox4").text("方向：" + dataTableOperation.toDirectionStr(dataList[21]));
                $("#bombBox5").text("记录仪速度：" + dataList[20]);
                $("#bombBox6").text("高程：" + dataList[19]);
                $("#rateLimiting").text('路网限速：' + dataList[30]);
                $("#roadType").text("道路类型：" + dataList[29]);

                if (people == "null") {
                    people = "";
                }
                var peopleIDcard = "";
                if (dataList[18] == "null") {
                    peopleIDcard = "";
                } else {
                    peopleIDcard = dataList[18];
                }

                var waybill = '';// 电子运单
                var cardID = '';// 身份证
                var cAName = '';// 发证机关
                var vehicleId = dataList[12];//车辆id
                if (waybillAndPractitionersInfo.containsKey(vehicleId)) {
                    var aboutInfo = waybillAndPractitionersInfo.get(vehicleId);
                    waybill = aboutInfo.waybill;
                    if (aboutInfo.practitioners) {
                        people = aboutInfo.practitioners.driverName;
                        cardID = aboutInfo.practitioners.driverIdentity;
                        peopleIDcard = aboutInfo.practitioners.certificationID;
                        cAName = aboutInfo.practitioners.cAName;
                    }
                }

                $('#infowindowShowVid').val(vehicleId);// 用于存放地图上正在显示信息详情窗体的车辆id(便于更新电子运单与从业人员信息)
                $(".popup_electronicWaybill").text("电子运单：" + (waybill ? waybill : ''));
                $(".popup_professionalsName").text("从业人员：" + (people ? people : ''));
                $(".popup_idCardNo").text("身份证号：" + (cardID ? cardID : ''));
                $(".popup_identity").text("从业资格证号：" + (peopleIDcard ? peopleIDcard : ''));
                $(".popup_tcb").text("发证机构：" + (cAName ? cAName : ''));
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
                $(".mapInfoDetail").hide();
                //执行信息框底部移动方法
                amapOperation.amapInfoSharpAdaptiveFn();
                //执行信息框整体基点位置方法
                if (infoWindow.setWidth) {
                    infoWindow.setWidth(240);
                } else {
                    infoWindow.setPosition(currentCarCoordinate);
                }
                $("#basicStatusInformation").css("width", "none");
            }
        }
        ,
//车牌号标注是否显示
        carNameState: function (flag) {
            var carNameMarkerValue;
            if (!carNameMarkerContentMap.isEmpty()) {
                carNameMarkerValue = carNameMarkerContentMap.values();
            }
            ;
            if (flag) {
                //重新计算对象名称位置
                amapOperation.carNameShow();
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
        }
        ,
// 重新计算对象名称位置
        carNameShow: function () {
            //清空车牌号显示位置信息
            if (map.getZoom() > 10 || map.currentMap === 'baidu') {
                var values = markerViewingArea.values();
                for (var i = 0, len = values.length; i < len; i++) {
                    var marker = values[i][0]; // [7] 图标
                    var id = marker.extData;
                    var name = marker.name;
                    var markerLngLat = marker.getPosition();
                    var icon = values[i][7];
                    var stateInfo = marker.stateInfo;
                    amapOperation.carNameEvade(id, name, markerLngLat, true, "1", icon, true, stateInfo);
                }
            }
            ;
        }
        ,
//手动清除label错误提示语
        clearLabel: function () {
            $('label.error').remove();
        }
        ,
//监控对象信息框更多显示方法
        amapInfoSharpAdaptiveFn: function () {
            /* //点击时判断是否显示信息框
             var curWidth = $(".amap-info-contentContainer .amap-info-content").outerWidth();
             var amapInfo = $(".amap-info");
             var curLeft = parseInt(amapInfo.css('left'));
             var allWidth = curWidth > 300 ? curWidth : $(".amap-info-contentContainer .amap-info-content").outerWidth();
             var reduceLeft = Math.ceil((allWidth - 196) / 2);*/
            if ($(".mapInfoDetail").is(":hidden")) {
                /*if (!isNaN(curLeft)) {
                    amapInfo.css('left', curLeft + reduceLeft + 'px');
                }*/
                $(".amap-info-sharp").removeClass("amap-info-sharp-marleft-hide");
                $(".amap-info-sharp").removeClass("amap-info-sharp-marleft-show");
                $(".amap-info-sharp").addClass("amap-info-sharp-marleft-hide");
            } else {
                /* if (!isNaN(curLeft)) {
                     amapInfo.css('left', curLeft - reduceLeft + 'px');
                 }*/
                $(".amap-info-sharp").removeClass("amap-info-sharp-marleft-hide");
                $(".amap-info-sharp").removeClass("amap-info-sharp-marleft-show");
                $(".amap-info-sharp").addClass("amap-info-sharp-marleft-show");
            }
        }
        ,
// 距离量算
        distanceMeasuremenEvent: function () {
            $('#toolOperateClick a i').removeClass('active');
            $('#toolOperateClick a span').css('color', 'rgb(92, 94, 98)');
            $('#areaMeasurementLab').removeClass('preBlue');
            $('#drawGraphics p.active').removeClass('active');
            if ($('#distanceMeasuremenLab').hasClass('preBlue')) {
                $('#distanceMeasuremenLab').removeClass('preBlue');
                mouseTool.close(true);
            } else {
                $('#distanceMeasuremenLab').addClass('preBlue');
                mouseTool.rule({zIndex: 50000, bubble: true, map: map});
            }
            isDistanceCount = true;
            amapFunCollection.removeCircleTool();
        }
        ,
    }
;
