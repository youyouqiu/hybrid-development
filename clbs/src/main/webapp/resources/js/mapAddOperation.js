(function ($, window) {
    var pathContextMenu;
    var isPathAddDragRoute = false;
    // var lineDragRoute;
    var drivingTraffic;
    var drivingDistance;
    var pathPointMarkerMap; // 路径规划的marker点
    window.mapaddoperation = {
        init: function () {

            mapaddoperation.createMapObj();

            //输入提示
            var autoOptions = {
                input: "tipinput"
            };
            var auto = map.autoComplete(autoOptions);
            var placeSearch = map.placeSearch({
                map: map
            });  //构造地点查询类
            auto.on('select', select);

            // AMap.event.addListener(auto, "select", select);//注册监听，当选中某条记录时会触发
            function select(e) {
                placeSearch.setCity(e.poi.adcode);
                placeSearch.search(e.poi.name, function (status, result) {
                    try {
                        var pos = result.poiList.pois[0].location;
                    }
                    catch (err) {
                        console.log('设置四维地图地点查询出错')
                    }

                });  //关键字查询查询
            }


            // 路径规划
            var startingPoint = map.autoComplete({
                input: "startingPoint"
            });
            startingPoint.on('select', mapaddoperation.setPathPlanning);
            var endingPoint = map.autoComplete({
                input: "endingPoint"
            });
            endingPoint.on('select', mapaddoperation.setPathPlanning);
        },
        /**
         * 创建map集合对象
         */
        createMapObj: function () {
            pathPointMarkerMap = new pageLayout.mapVehicle();
        },
        /**
         *  下载或者打印图片
         * @param param
         */
        htmlTocanvas: function () {
            var obj = document.querySelector(map.currentMap === 'aMap' ? ".amap-maps" : "#MapContainer");
            if (!window.Promise) {
                layer.msg('本浏览器暂不支持该功能!');
                return;
            }
            layer.load(2);

            setTimeout(function () {
                html2canvas(obj, {
                    useCORS: true,
                    backgroundColor: null,
                }).then(function (canvas) {
                    console.log('截图成功');
                    layer.closeAll('loading');

                    // $('#mapPic').append(canvas)

                    // 将整个页面图片转成Base64位
                    var dataURL = canvas.toDataURL("image/jpg");
                    $('#picImg').attr('src', dataURL);
                    localStorage.setItem('printPicBase64', dataURL);
                    $('#mapPicModal').modal('show')

                    // if (param === 'download'){ //下载图片
                    //
                    //     var saveLink = document.createElement('a');
                    //     saveLink.href = dataURL;
                    //     saveLink.download = 'downLoad.jpg';
                    //     saveLink.click();
                    // }
                    //
                    // if (param === 'print'){ // 打印图片
                    //
                    // }

                });
            }, 10)


        },
        /**
         * 下载图片
         */
        downloadPic: function () {
            var saveLink = document.createElement('a');
            saveLink.href = $('#picImg').attr('src');
            saveLink.download = 'downLoad.jpg';
            var evt = document.createEvent("MouseEvents");
            evt.initEvent("click", true, true);
            saveLink.dispatchEvent(evt);
        },
        /**
         * 打印图片
         */
        printMapPic: function () {
            // var printHtml=document.getElementById('mapPic').innerHTML,
            //     newWindow=window.open("",'_blank');
            //     newWindow.document.body.innerHTML = printHtml;
            //
            // var script = document.createElement("script");
            // script.type = "text/javascript";
            // script.innerHTML = 'window.print()'
            // newWindow.document.body.appendChild(script)
            // newWindow.print();
            window.newWindow = window.open("/clbs/m/web/print/list.html", '_blank');
        },
        /**
         * 设置全国范围的层级
         */
        setMapNationalRoom: function () {
            map.setZoomAndCenter(4, [116.397428, 39.90923]);
        },
        /**
         * 设置路径规划
         * 这一步获取到设置的起点终点和经过点
         */
        setPathPlanning: function (data) {
            drivingTraffic && drivingTraffic.clear();
            drivingDistance && drivingDistance.clear();

            var addressArray = [];
            if (data != null && data != 'drag') {
                var this_input_id = $(this)[0].id || $(this)[0].input.id;
                $("#" + this_input_id).attr('data-address', data.poi.district + data.poi.name).removeAttr('data-lnglat');
            }
            ;
            var startAddress = $('#startingPoint').attr('data-address');
            var start_lnglat = $('#startingPoint').attr('data-lnglat');
            var endAddress = $('#endingPoint').attr('data-address');
            var end_lnglat = $('#endingPoint').attr('data-lnglat');
            if (startAddress != '' && endAddress != '' && startAddress != undefined && endAddress != undefined) {
                if (start_lnglat != undefined) {
                    addressArray.push(start_lnglat);
                } else {
                    addressArray.push(startAddress);
                }
                ;
                $('#byWayOfArea input').each(function () {
                    var this_value = $(this).val();
                    if (this_value != '') {
                        var value = $(this).attr('data-address');
                        var lnglat = $(this).attr('data-lnglat');
                        if (lnglat != undefined) {
                            addressArray.push(lnglat);
                        } else {
                            addressArray.push(value);
                        }
                        ;
                    } else {
                        $(this).parent('div').parent('div').remove();
                    }
                    ;
                });
                if (end_lnglat != undefined) {
                    addressArray.push(end_lnglat);
                } else {
                    addressArray.push(endAddress);
                }
                ;
                var pathLngLatArray = [];
                mapaddoperation.getPathAddressLngLat(addressArray, 0, pathLngLatArray);
            }
            ;
        },
        /**
         * 获取地理编码
         * 并且开始画路径
         */
        getPathAddressLngLat: function (addressArray, index, pathLngLatArray) {
            var this_address = addressArray[index];
            if (mapaddoperation.isChineseChar(this_address)) {
                var geocoder = map.geocoder({
                    city: "全国", //城市，默认：“全国”
                    radius: 500 //范围，默认：500
                });
                geocoder.getLocation(this_address);
                geocoder.on('complete', function (GeocoderResult) {
                    if (GeocoderResult.type == 'complete') {
                        var this_lng = GeocoderResult.geocodes[0].location.lng;
                        var this_lat = GeocoderResult.geocodes[0].location.lat;
                        pathLngLatArray.push([this_lng, this_lat]);
                        index++;
                        if (index == addressArray.length) {
                            mapaddoperation.startDrawRoute(pathLngLatArray);
                        } else {
                            mapaddoperation.getPathAddressLngLat(addressArray, index, pathLngLatArray);
                        }
                    }
                });
            } else {
                index++;
                pathLngLatArray.push(this_address.split(';'));
                if (index == addressArray.length) {
                    mapaddoperation.startDrawRoute(pathLngLatArray);
                } else {
                    mapaddoperation.getPathAddressLngLat(addressArray, index, pathLngLatArray);
                }
                ;
            }
            ;
        },
        //开始路径规划
        startDrawRoute: function (array) {
            var len = array.length;
            var startPoint = map.lngLat(array[0][0], array[0][1]);
            var endPoint = map.lngLat(array[len - 1][0], array[len - 1][1]);

            if (array[0][0] === array[len - 1][0] && array[0][1] === array[len - 1][1]) {
                $('#drawErrCont p').html('起点和终点不能相同');
                $('#drawErrCont').show();
                $('#routesCont').hide();
                return;
            } else {
                $('#drawErrCont').hide();
            }


            var wayPoints = [];
            if (len > 2) {
                for (var i = 1; i < len - 1; i++) {
                    wayPoints.push(map.lngLat(array[i][0], array[i][1]))
                }
            }

            var path = [];
            for (var j = 0; j < array.length; j++) {
                path.push({lnglat: [array[j][0], array[j][1]]});
            }
            //最短路径
            var drivingDistanceOption = {
                policy: 8, // 0，1避免拥堵 2，不走高速 3，"避免收费" ，4 "避免拥堵&不走高速" 5 "避免收费&不走高速" 6 "避免拥堵&避免收费" 7"避免拥堵&避免收费&不走高速" 8 "高速优先" 9 "避免拥堵&高速优先"
                size: 3,
                map: map,
            }
            drivingDistance = map.truckDriving(drivingDistanceOption)
            drivingDistance.search(path, function (status, result) {
                // result 即是对应的驾车导航信息，相关数据结构文档请参考  https://lbs.amap.com/api/javascript-api/reference/route-search#m_DrivingResult
                if (status === 'complete') {
                    console.log('最短路径绘制驾车路线完成', result);
                    $('#routesCont').show();
                    mapaddoperation.renderRoutesData('shortestRoute', result);

                    mapaddoperation.clearPathPointMarker()
                } else {
                    console.log('最短路径获取驾车数据失败：' + result)
                }
            });
            //最佳路况
            var drivingTrafficOption = {
                policy: 1,
                size: 3,
                map: map,
            };
            drivingTraffic = map.truckDriving(drivingTrafficOption);
            drivingTraffic.search(path, function (status, result) {
                if (status === 'complete') {
                    console.log('最佳路况绘制驾车路线完成', result)
                    mapaddoperation.renderRoutesData('bestCondition', result)
                } else {
                    console.log('最佳路况获取驾车数据失败：' + result)
                }
            });


            //最短路径
            // var drivingDistanceOption = {
            //     policy: AMap.DrivingPolicy.LEAST_DISTANCE, // 其它policy参数请参考 https://lbs.amap.com/api/javascript-api/reference/route-search#m_DrivingPolicy
            //     extensions:'all',
            //     // showTraffic:false,
            //     map: map,
            // }
            // drivingDistance = map.driving(drivingDistanceOption)
            // drivingDistance.search(startPoint, endPoint,{waypoints:wayPoints}, function(status, result) {
            //     // result 即是对应的驾车导航信息，相关数据结构文档请参考  https://lbs.amap.com/api/javascript-api/reference/route-search#m_DrivingResult
            //     if (status === 'complete') {
            //         console.log('最短路径绘制驾车路线完成');
            //         console.log('result',result);
            //         $('#routesCont').show();
            //         mapaddoperation.renderRoutesData('shortestRoute',result)
            //     } else {
            //         console.log('最短路径获取驾车数据失败：' + result)
            //     }
            // });
            //
            // // 最佳路况
            // var drivingTrafficOption = {
            //     policy: AMap.DrivingPolicy.REAL_TRAFFIC,
            //     map: map,
            // }
            // drivingTraffic = map.driving(drivingTrafficOption)
            // drivingTraffic.search(startPoint, endPoint,{waypoints:wayPoints}, function(status, result) {
            //     if (status === 'complete') {
            //         console.log('最佳路况绘制驾车路线完成')
            //         mapaddoperation.renderRoutesData('bestCondition',result)
            //     } else {
            //         console.log('最佳路况获取驾车数据失败：' + result)
            //     }
            // });
        },
        /**
         * 渲染路径数据列表
         */
        renderRoutesData: function (id, res) {
            var distance = (res.routes[0].distance / 1000).toFixed(1);
            var time = parseInt(res.routes[0].time / 60);
            var html = '';
            var wayPointsHtml = "";
            var wayPointsArr = [];
            var list = res.routes[0].steps;
            html += '<li class="start road-name"><p>从 ' + $('#startingPoint').val() + '出发</p></li>';
            for (var i = 0; i < list.length; i++) {
                html += '<li class="route ' + mapaddoperation.dealRouteClass(list[i].action) + '">' +
                    '<div class="route-item">' +
                    '<p class="road-name">' + mapaddoperation.dealRoadName(list[i].road) + '</p>' +
                    '<p class="route-desc">' + list[i].distance + '米&ensp;' + Math.ceil(list[i].time / 60) + '分钟</p>' +
                    '<i class="fa chevron-down"></i>' +
                    '<p class="routeseg" style="display: none;">' + list[i].instruction + '</p>' +
                    '</div>' +
                    '</li>'

                if (list[i].road != "") {
                    wayPointsArr.push(list[i].road)
                }
            }
            html += '<li class="end road-name"><p>到达 ' + $('#endingPoint').val() + '</p></li>';
            for (var j = 0; j < wayPointsArr.length; j++) {
                if (j === wayPointsArr.length - 1) {
                    wayPointsHtml += wayPointsArr[j]
                } else {
                    if (j < 3) {
                        wayPointsHtml += wayPointsArr[j] + '>'
                    } else {
                        if (j === 3) {
                            wayPointsHtml += '...>'
                        } else {
                            continue;
                        }
                    }
                }
            }

            $('#' + id + ' .routesTit-desc .time').html(time);
            $('#' + id + ' .routesTit-desc .distance').html(distance);
            $('#' + id + ' .bywayroad').html(wayPointsHtml);
            $('#' + id + ' ul.routes-list').html(html);
        },
        dealRouteClass: function (action) {
            switch (action) {
                case "右转":
                    return 'turn-right';
                case "左转":
                    return 'turn-left';
                case "直行":
                    return 'turn-advance';
                case "向右前方行驶":
                    return "turn-rightup";
                case "向左前方行驶":
                    return "turn-leftup";
                case "靠左":
                    return "turn-keepleft";
                case "靠右":
                    return "turn-keepright";
                default:
                    return 'turn-advance'

            }

        },
        dealRoadName: function (data) {
            return data ? data : '未知道路'
        },
        /**
         * 切换显示路径具体信息
         */
        toggleRoutelist: function () {
            if ($(this).hasClass('chevron-down')) {
                $(this).removeClass('chevron-down').addClass('chevron-up').siblings('.routeseg').show()
            } else {
                $(this).removeClass('chevron-up').addClass('chevron-down').siblings('.routeseg').hide()
            }
        },
        //判断是否还有中文
        isChineseChar: function (str) {
            var reg = /[\u4E00-\u9FA5\uF900-\uFA2D]/;
            return reg.test(str);
        },
        //添加途经点
        addWayToPoint: function (msg) {
            var length = $('#byWayOfArea').children('div').length;
            var searchId = 'byWayOf' + (length + 1);
            var html = '<div class="form-group">'
                + '<div class="col-md-10">'
                + '<input type="text" id="' + searchId + '" placeholder="请输入途经点(或右键地图)" class="form-control wayPoint" name="byWayOf" />'
                + '</div>'
                + '<button type="button" class="btn btn-danger padBottom deletebyWayOf"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button>'
                + '</div>';
            $(html).appendTo($('#byWayOfArea'));
            $('#' + searchId).inputClear().on('onClearEvent', mapaddoperation.byWayOfInputClear);
            if (Array.isArray(msg)) {
                $('#' + searchId).val(msg[0]).attr('data-address', msg[0]).attr('data-lnglat', msg[2]);
            }
            // ;
            var byWay = map.autoComplete({
                input: searchId
            });
            byWay.on('select', mapaddoperation.setPathPlanning);
            $('.deletebyWayOf').off('click').on('click', mapaddoperation.deleteWay);
        },
        //途经点删除
        deleteWay: function () {
            $(this).parent('div.form-group').remove();
            mapaddoperation.setPathPlanning(null);
        },
        //途经点文本框清除事件
        byWayOfInputClear: function (e, data) {
            var id = data.id;
            $('#' + id).attr('data-address', '').removeAttr('data-lnglat');
        },
        /**
         * 显示路径规划
         */
        showRoadPlan: function () {
            // isPathAddDragRoute = true;
            $('#pathAnalysis').show();
            mapaddoperation.addMapRightItem()
        },
        /**
         * 关闭路径规划
         */
        closePathAnalysis: function () {
            isPathAddDragRoute = false;
            // $('#pathAnalysis').hide();
            // pathContextMenu && pathContextMenu.hide();

            mapaddoperation.clearLinePathRoute()
        },
        //添加右键菜单
        addMapRightItem: function () {
            // $('#addOrUpdateTravelFlag').val('0');
            isPathAddDragRoute = true;
            //创建右键菜单
            var this_path_point_lnglat;
            pathContextMenu = map.contextMenu();
            pathContextMenu.addItem("<i class='menu-icon menu-icon-from'></i>&ensp;&ensp;&ensp;<span>起点</span>", function (e) {
                mapaddoperation.rightItemCallBack(this_path_point_lnglat, 0);
            }, 0);
            pathContextMenu.addItem("<i class='menu-icon menu-icon-via'></i>&ensp;&ensp;&ensp;<span>途经点</span>", function () {
                mapaddoperation.rightItemCallBack(this_path_point_lnglat, 1);
            }, 1);
            pathContextMenu.addItem("<i class='menu-icon menu-icon-to'></i>&ensp;&ensp;&ensp;<span>终点</span>", function () {
                mapaddoperation.rightItemCallBack(this_path_point_lnglat, 2);
            }, 2);
            pathContextMenu.addItem("<i class='icon-clearmap'></i>&ensp;&ensp;&ensp;<span>清除路线</span>", function () {
                mapaddoperation.rightItemCallBack(this_path_point_lnglat, 3);
            }, 3);
            if (typeof pathContextMenu.addContextMenu === 'function') {
                pathContextMenu.addContextMenu();
            }
            //地图绑定鼠标右击事件——弹出右键菜单
            map.on('rightclick', function (e) {
                if (isPathAddDragRoute) {
                    this_path_point_lnglat = [e.lnglat.lng, e.lnglat.lat];
                    pathContextMenu.open(map, e.lnglat);
                    // contextMenuPositon = e.lnglat;
                }
                // ;
            });
        },
        /**
         * 右键菜单选择回调函数
         * @param lnglat
         * @param type 0起点 1途经点 2终点 3清除路线
         */
        rightItemCallBack: function (lnglat, type) {
            if (type != 3) {
                var iconType;
                if (type == 0) { // 起点
                    iconType = '../../resources/img/start_point.png';
                } else if (type == 1) {// 途经
                    iconType = '../../resources/img/mid_point.png';
                } else if (type == 2) {// 终点
                    iconType = '../../resources/img/end_point.png';
                }
                ;
                var dragRouteMarker = map.marker({
                    map: map,
                    position: lnglat,
                    icon: map.icon({
                        size: map.size(40, 40),  //图标大小
                        image: iconType
                    })
                });
                if (type == 0) {
                    if (pathPointMarkerMap.containsKey(type)) {
                        var this_marker = pathPointMarkerMap.get(type);
                        map.remove(this_marker);
                        pathPointMarkerMap.remove(type);
                    }
                    ;
                    pathPointMarkerMap.put(type, dragRouteMarker);
                } else if (type == 2) {
                    if (pathPointMarkerMap.containsKey(type)) {
                        var this_marker = pathPointMarkerMap.get(type);
                        map.remove(this_marker);
                        pathPointMarkerMap.remove(type);
                    }
                    ;
                    pathPointMarkerMap.put(type, dragRouteMarker);
                } else if (type == 1) {
                    var this_marker_array = [];
                    if (pathPointMarkerMap.containsKey(type)) {
                        this_marker_array = pathPointMarkerMap.get(type);
                        pathPointMarkerMap.remove(type);
                    }
                    ;
                    this_marker_array.push(dragRouteMarker);
                    pathPointMarkerMap.put(type, this_marker_array);
                }
                ;
                mapaddoperation.getPathAddressOneInfo(lnglat, type);
            } else {
                isPathAddDragRoute = false;
                mapaddoperation.clearLinePathRoute();
                mapaddoperation.addMapRightItem();
                // if (drivingTraffic != undefined) {
                //     drivingTraffic.clear();
                //     drivingDistance.clear();
                // }
                $('#pathAnalysis').show();
            }
            ;
        },
        //单独一条信息逆地理编码
        getPathAddressOneInfo: function (array, type) {
            var arrayString = array[0] + ';' + array[1];
            var geocoder = map.geocoder({
                city: "全国", //城市，默认：“全国”
                radius: 500 //范围，默认：500
            });
            geocoder.getAddress(array);
            geocoder.on('complete', function (GeocoderResult) {
                if (GeocoderResult.type == 'complete') {
                    var this_address_value = GeocoderResult.regeocode.addressComponent.township;
                    var this_address = GeocoderResult.regeocode.formattedAddress;
                    if (type == 0) {
                        $('#startingPoint').val(this_address).attr('data-address', this_address).attr('data-lnglat', arrayString);
                    }
                    ;
                    if (type == 2) {
                        $('#endingPoint').val(this_address).attr('data-address', this_address).attr('data-lnglat', arrayString);
                    }
                    ;
                    if (type == 1) {
                        mapaddoperation.addWayToPoint([this_address, this_address_value, arrayString]);
                    }
                    ;
                    mapaddoperation.setPathPlanning('drag');
                }
                ;
            });
        },
        //清空行驶路线input
        clearLinePathRoute: function () {
            $('#pathAnalysis').hide();
            $('#pathAnalysis input').each(function () {
                $(this).val('').attr('data-address', '').removeAttr('data-lnglat');
            });
            $('#byWayOfArea').html('');
            // $('#dragRouteDescription').val('');
            var start_point = pathPointMarkerMap.get('0');
            var end_point = pathPointMarkerMap.get('2');
            var wayPoint = pathPointMarkerMap.get('1');
            if (start_point != undefined) {
                map.remove([start_point]);
            }
            ;
            if (end_point != undefined) {
                map.remove([end_point]);
            }
            ;
            if (wayPoint != undefined && wayPoint.length) {
                for (var i = 0; i < wayPoint.length; i++) {
                    map.remove([wayPoint[i]]);
                }
            }
            ;
            pathPointMarkerMap.clear();

            if (drivingTraffic != undefined) {
                drivingTraffic.clear();
                drivingDistance.clear();
            }
            $('#routesCont').hide();
        },
        //清空右键规划的marker
        clearPathPointMarker: function () {
            if (pathPointMarkerMap != undefined) {
                if (pathPointMarkerMap.containsKey('0')) {
                    var this_marker = pathPointMarkerMap.get('0');
                    map.remove([this_marker]);
                }
                ;
                if (pathPointMarkerMap.containsKey('2')) {
                    var this_marker = pathPointMarkerMap.get('2');
                    map.remove([this_marker]);
                }
                ;
                if (pathPointMarkerMap.containsKey('1')) {
                    var this_marker_array = pathPointMarkerMap.get('1');
                    map.remove(this_marker_array);
                }
                ;
                pathPointMarkerMap.clear();
            }
            ;
        },
    }
    $(function () {
        var timer = setInterval(function () {
            if (map) {
                clearInterval(timer);
                mapaddoperation.init();
            }
        }, 300);

        $('#previewImage').on('click', mapaddoperation.htmlTocanvas)

        $('#printMapPic').on('click', function () {
            $('#mapPicModal').modal('show')
        })
        $('#national').on('click', mapaddoperation.setMapNationalRoom)
        $('#pathplanning').on('click', mapaddoperation.setPathPlanning)

        $('#print').on('click', mapaddoperation.printMapPic);

        $('#downloadPic').on('click', mapaddoperation.downloadPic);

        // 路径规划start
        $('#roadPlan').on('click', mapaddoperation.showRoadPlan);
        $('#closePath').on('click', mapaddoperation.closePathAnalysis);
        $('#addByWayOf').on('click', mapaddoperation.addWayToPoint);

        $('.routes-list').on('click', 'i.fa', mapaddoperation.toggleRoutelist)
        // $('.deletebyWayOf').on('click',mapaddoperation.deleteWay);
        //路径规划end

        $(window).unload(function () {
            if (window.newWindow) {
                window.newWindow.close();
            }
        })
        window.onbeforeunload = function () {
            if (window.newWindow) {
                window.newWindow.close();
            }
        }
    })
})(jQuery, window)