/************ humitureWatch.map.js start *************/

var hwMap, map, marker1, marker2;
var infoWindow;
var beforeId = null;
var timer = null;

(function (window, $) {

    hwMap = {
        // 处理报警
        init: function () {
            map = new AMap.Map("combatMap", {
                resizeEnable: true,
                scrollWheel: true,
                zoom: 18,
//				center : [ 116.405467, 39.907761 ],
            });
        },
        // 获取两个点旋转角度
        calcAngle: function (startPos, endPos) {
            const p_start = map.lngLatToContainer(startPos);
            const p_end = map.lngLatToContainer(endPos);
            const diff_x = p_end.x - p_start.x;
            const diff_y = p_end.y - p_start.y;
            return 360 * Math.atan2(diff_y, diff_x) / (2 * Math.PI);
        },
        //计算车牌号四个定点的像素坐标
        countAnglePX: function (angle, pixel, centerPX, num, picWidth, picHeight) {
            var thisPX;
            var thisX;
            var thisY;
            if ((angle <= 45 && angle > 0) || (angle > 180 && angle <= 225) || (angle >= 135 && angle < 180) || (angle >= 315 && angle < 360)) {
                angle = 0;
            }
            if ((angle < 90 && angle > 45) || (angle < 270 && angle > 225) || (angle > 90 && angle < 135) || (angle > 270 && angle < 315)) {
                angle = 90;
            }
            if (angle == 90 || angle == 270) {
                if (num == 1) {
                    thisX = centerPX[0] + picHeight;
                    thisY = centerPX[1] + picWidth;
                }
                if (num == 2) {
                    thisX = centerPX[0] + picHeight;
                    thisY = centerPX[1] - picWidth;
                }
                if (num == 3) {
                    thisX = centerPX[0] - picHeight;
                    thisY = centerPX[1] - picWidth;
                }
                if (num == 4) {
                    thisX = centerPX[0] - picHeight;
                    thisY = centerPX[1] + picWidth;
                }
            }
            if (angle == 0 || angle == 180 || angle == 360) {
                thisX = pixel[0];
                thisY = pixel[1];
            }
            thisPX = [thisX, thisY];
            return thisPX;
        },
        update: function (data) {
            if (timer) {
                clearTimeout(timer);
                timer = null;
            }
            if (marker1) {
                map.remove(marker1);
            }
            if (marker2) {
                map.remove(marker2);
            }
            if (infoWindow) {
                map.remove(infoWindow);
            }
            var lnglat = new AMap.LngLat(data.data.msgBody.longitude, data.data.msgBody.latitude);
            var position = [data.data.msgBody.longitude, data.data.msgBody.latitude];
            map.setZoomAndCenter(19, lnglat);
            var state = '';
            switch (data.data.msgBody.stateInfo) {
                case 4:
                case 10:
                case 2:
                case 9:
                case 11:
                    state = 'carStateRun';
                    break;
                case 5:
                    state = 'carStateAlarm';
                    break;
                case 3:
                    state = 'carStateOffLine';
                    break;
            }
            var carContent = "<p class='carNameShowRD'><i class='" + state + "'></i>&ensp;<span class='monitorNameBox'>" + data.data.msgBody.monitorInfo.monitorName + "</span></p>";
            var currentId = data.data.msgBody.monitorInfo.monitorId;
            var angle = Number(data.data.msgBody.direction) + 270; // 角度
            if (beforeId === currentId) {
                angle = hwMap.calcAngle(marker2.getPosition(), position) + 360;
            }
            marker2 = new AMap.Marker({
                icon: "/clbs/resources/img/vico/" + data.data.msgBody.monitorInfo.monitorIcon,
                position: position,
                clickable: true, //是否可点击
                offset: new AMap.Pixel(-29, -13),
                map: map,
            });
            marker2.setAngle(angle);
            beforeId = currentId;
            timer = setTimeout(function () {
                //将经纬度转为像素
                var pixel = map.lngLatToContainer(position);
                var pixelX = pixel.getX();
                var pixelY = pixel.getY();
                var pixelPX = [pixelX, pixelY];

                //判断是否第一个创建
                var markerAngle = 0; //图标旋转角度
                //得到车辆图标四个角的像素点(假设车图标永远正显示)58*26
                var defaultRD = [pixelX + 29, pixelY + 13];//右下
                //计算后PX
                var pixelRD = hwMap.countAnglePX(markerAngle, defaultRD, pixelPX, 1, 29, 13);
                var llRD = map.containTolnglat(new AMap.Pixel(pixelRD[0], pixelRD[1]));

                marker1 = new AMap.Marker({
                    // icon: "/clbs/resources/img/vico/v_1_1.png",
                    content: carContent,
                    clickable: true, //是否可点击
                    autoRotation: true,//自动调节图片角度
                    offset: new AMap.Pixel(0, 0),
                    position: llRD,
                    map: map,
                    zIndex: 100,
                });
            }, 310);


            var wenshidu = '';
            if (data.data.msgBody.temperatureSensor != null) {
                for (var i = 0; i < data.data.msgBody.temperatureSensor.length; i++) {
                    var item = data.data.msgBody.temperatureSensor[i];
                    var index = parseInt(item.id.toString(16)) - 20;
                    wenshidu += '<div>温度传感' + index + ': ' + (item.temperature === undefined ? '-' : (item.temperature)) + ' ℃</div>'
                }
            }
            if (data.data.msgBody.temphumiditySensor != null) {
                for (var i = 0; i < data.data.msgBody.temphumiditySensor.length; i++) {
                    var item = data.data.msgBody.temphumiditySensor[i];
                    var index = parseInt(item.id.toString(16)) - 25;
                    wenshidu += '<div>湿度传感' + index + ': ' + (item.temperature === undefined ? '-' : (item.temperature)) + ' %RH</div>'
                }
            }
            var alarmTime;
            var time = data.data.msgBody.uploadtime;
            if (time.length == 12) {
                alarmTime = 20 + time.substring(0, 2) + "-" + time.substring(2, 4) + "-" + time.substring(4, 6) + " " +
                    time.substring(6, 8) + ":" + time.substring(8, 10) + ":" + time.substring(10, 12);
            } else {
                alarmTime = "时间异常";
            }
            ;
            var dianya = '-';
            if (sensorInfo) {
                dianya = '220v'
            }
            var address = (data.data.msgBody.positionDescription === null || data.data.msgBody.positionDescription === undefined) ? '未定位' : data.data.msgBody.positionDescription
            var content = '<div>'
                + '<div class="amap-info-content amap-info-outer">'
                + '<div class="col-md-12" id="basicStatusInformation" style="padding:0px;">'
                + '<div>时间：' + alarmTime + '</div>'
                + '<div>车牌号：' + data.data.msgBody.monitorInfo.monitorName + '</div>'
                + '<div>终端号：' + data.data.msgBody.monitorInfo.deviceNumber + '</div>'
                + '<div>终端手机号：' + data.data.msgBody.monitorInfo.simcardNumber + '</div>'
                + wenshidu
                + '<div>供电电压: ' + dianya + '</div>'
                + '<div>位置：' + address + '</div>'
                + '</div></div>'
                + '<div class="amap-info-sharp" style="height: 23px;"></div>'
                + '</div>'
            //marker.content=content;

            marker2.on('click', openInfo);

            map.setFitView();

            function openInfo(e) {
                infoWindow = new AMap.InfoWindow({
                    isCustom: true, //使用自定义窗体
                    offset: new AMap.Pixel(16, -25),
                    closeWhenClickMap: true,//-113, -140
                    // content:content,
                    map: map
                });
                infoWindow.setContent(content);
                infoWindow.open(map, e.target.getPosition());
                map.setFitView();
            }
        }
    };
    $(function () {
        hwMap.init();
    })
})(window, $);

/************ humitureWatch.map.js end *************/
