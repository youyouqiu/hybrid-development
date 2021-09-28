var Util = {
    formateGpsTime: function (gpsTime) {
        return 20 + gpsTime.substring(0, 2)
            + "-" + gpsTime.substring(2, 4)
            + "-" + gpsTime.substring(4, 6)
            + " " + gpsTime.substring(6, 8)
            + ":" + gpsTime.substring(8, 10)
            + ":" + gpsTime.substring(10, 12);
    },
    formateLocateType: function (locationType) {
        if (locationType == 0) {
            locationType = "卫星+基站定位";
        } else if (locationType == 1) {
            locationType = "基站定位";
        } else if (locationType == 2) {
            locationType = "卫星定位";
        } else if (locationType == 3) {
            locationType = "WIFI+基站定位";
        } else if (locationType == 4) {
            locationType = "卫星+WIFI+基站定位";
        } else {
            locationType = "-";
        }
        return locationType;
    },
    formateLocation: function (address) {
        return (address == 'null' || address == null) ? '未定位' : address;
    },
    status2ColorClass: function (stateInfo) {
        var state;
        switch (stateInfo) {
            case 'stop':
                state = 'carStateStop';
                break;
            case 'run':
                state = 'carStateRun';
                break;
            case 'alarm':
                state = 'carStateAlarm';
                break;
            case 'notPosition':
                state = 'carStateMiss';
                break;
            case 'offline':
                state = 'carStateOffLine';
                break;
            case 'overSpeed':
                state = 'carStateOverSpeed';
                break;
            case 'heartBeat':
                state = 'carStateheartbeat';
                break;
        }
        ;
        return state;
    },
    statusNumber2Text: function (status) {
        switch (status) {
            case 11: // 心跳
                return 'heartBeat';
            case 2: // 未定位
                return 'notPosition';
            case 5: // 报警
                return 'alarm';
            case 10: // 行驶
                return 'run';
            case 4: // 停止
                return 'stop';
            case 9: // 超速
                return 'overSpeed';
            case 3: // 离线
                return 'offline';
        }
    },
    status2Html: function (status) {
        switch (status) {
            case 'heartBeat': // 心跳
                return '<span class="heartbeatArea"><span class="heartbeat_ico small-window-icon"></span></span>';
            case 'notPosition': // 未定位
                return '<span class="onlineNotPositioningArea"><span class="onlineNotPositioning_ico small-window-icon"></span></span>';
            case 'alarm': // 报警
                return '<span class="warningArea"><span class="warning_ico small-window-icon"></span></span>';
            case 'run': // 行驶
                return '<span class="onlineDrivingArea"><span class="onlineDriving_ico small-window-icon"></span></span>';
            case 'stop': // 停止
                return '<span class="onlineParkingArea"><span class="onlineParking_ico small-window-icon"></span></span>';
            case 'overSpeed': // 超速
                return '<span class="speedLimitWarningArea"><span class="speedLimitWarning_ico small-window-icon"></span></span>';
            case 'offline': // 离线
                return '<span class="offlineIconArea"><span class="offlineIcon_ico small-window-icon"></span></span>';
        }
    },
    formatDuring: function (mss) {
        var days = parseInt(mss / (1000 * 60 * 60 * 24));
        var hours = parseInt((mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        var minutes = parseInt((mss % (1000 * 60 * 60)) / (1000 * 60));
        var seconds = parseInt((mss % (1000 * 60)) / 1000);
        if (days === 0 && hours === 0 && minutes == 0) {
            return seconds + " 秒 ";
        } else if (days === 0 && hours === 0 && minutes !== 0) {
            return minutes + " 分 " + seconds + " 秒 ";
        } else if (days === 0 && hours !== 0) {
            return hours + " 小时 " + minutes + " 分 " + seconds + " 秒 ";
        } else if (days !== 0) {
            return '>' + days + " 天";
        }
    },
    getIconPath: function (type, ico) {
        var icons, picWidth, picHeight;
        if (type == 0) {
            if (ico == "null" || ico == undefined || ico == null) {
                icons = "../../resources/img/vehicle.png";
            } else {
                icons = "../../resources/img/vico/" + ico;
            }
            picWidth = 58 / 2;
            picHeight = 26 / 2;
        } else if (type == 1) {
            if (ico == "null" || ico == undefined || ico == null) {
                icons = "../../resources/img/123.png";
            } else {
                icons = "../../resources/img/vico/" + ico;
            }
            picWidth = 30 / 2;
            picHeight = 30 / 2;
        } else if (type == 2) {
            if (ico == "null" || ico == undefined || ico == null) {
                icons = "../../resources/img/thing.png";
            } else {
                icons = "../../resources/img/vico/" + ico;
            }
            picWidth = 40 / 2;
            picHeight = 40 / 2;
        }
        return {
            icon: icons,
            picWidth: picWidth,
            picHeight: picHeight
        }
    },
    // 计算marker移动速度
    markerMoveSpeed: function (distance, time) {
        var speed;
        if (distance != null && distance != 0) {
            distance = distance / 1000;
            var markerTime = (time[time.length - 1].getTime() - time[time.length - 2].getTime()) / 1000 / 60 / 60;
            if (markerTime == 0) {
                speed = 50;
            } else {
                speed = Number((distance / markerTime).toFixed(2));
            }
        } else {
            speed = 300;
        }
        return speed == 0 ? 100 : speed;
    },
    findByPath: function (source, pathArray, replacer) {
        if (source === undefined || source === null) {
            return replacer;
        }
        var sourceRef = source;
        var i, item;
        for (i = 0; i < pathArray.length; i++) {
            item = sourceRef[pathArray[i]];
            if (item === undefined || item === null) {
                return replacer;
            }
            sourceRef = item;
        }
        return item;
    },
    noUndefineOrNull: function (value) {
        return value !== undefined && value !== null;
    },
    formateStationType: function (type) {
        switch (type) {
            case 4:
                return '2G';
            case 5:
                return '3G';
            case 6:
                return '4G';
            case 7:
                return '5G';
            case 8:
                return 'E';
            default:
                return '';
        }
    },
    formateWorkingPosition: function (status) {
        switch (status) {
            case 0:
                return '停机';
            case 1:
                return '工作';
            case 2:
                return '待机';
            default:
                return '';
        }
    },
    getHourFromSecond: function (second) {
        var hourSecond = 60 * 60;
        var hour = second / hourSecond;
        hour = Util.toFixed(hour, 1, true);
        return hour;
    },
    toFixed: function (source, digit, omitZero) {
        var sourceIn = source;
        if (typeof sourceIn !== 'number') {
            try {
                sourceIn = parseFloat(sourceIn);
            } catch (error) {
                return 0;
            }
        }
        if (sourceIn === null || sourceIn === undefined || isNaN(sourceIn)) {
            return 0;
        }
        var afterFixed = sourceIn.toFixed(digit); // 此时 afterFixed 为string类型
        if (omitZero) {
            afterFixed = parseFloat(afterFixed);
        }
        return afterFixed;
    },
    formateReverseState: function (state, direction) {
        if (state === 1) {
            return '停转';
        } else {
            if (direction === 1) {
                return '顺时针';
            } else {
                return '逆时针';
            }
        }
    },
    formateWeightStatus: function (type) {
        if (type === 1) { // 空载
            return '空载';
        } else if (type === 6) { // 轻载
            return '轻载';
        } else if (type === 2) { // 满载
            return '满载';
        } else if (type === 7) { // 重载
            return '重载';
        } else if (type === 3) { // 超载
            return '超载';
        }
        return '';
    },
    formateWeight: function (weight) {
        if (weight < 1000) {
            return Util.toFixed(weight, 1, true);
        } else {
            return Util.toFixed(weight / 1000, 1, true);
        }
    },
    swapElement: function (a, b) {
        // create a temporary marker div
        var aNext = $('<div>').insertAfter(a);
        a.insertAfter(b);
        b.insertBefore(aNext);
        // remove marker div
        aNext.remove();
    },
    swapArrayElement:function (array, indexA, indexB) {
        var temp = array[indexA];
        array[indexA] = array[indexB];
        array[indexB] = temp;
        return array;
    },
    nvl:function (source,replacer) {
        if (Util.noUndefineOrNull(source)){
            return source;
        }
        return replacer;
    },
    arrayLast:function (array, offset) {
        if (!offset){
            offset = 1;
        }
        return array[array.length - offset];
    },
    calcDistance : function(presentPoint, moveToPoint){
        var presentLngLat = presentPoint;
        var moveLngLat = moveToPoint;
        if (presentPoint instanceof  Array){
            presentLngLat = new AMap.LngLat(presentPoint[0], presentPoint[1]);
        }
        if (moveToPoint instanceof  Array){
            moveLngLat = new AMap.LngLat(moveToPoint[0], moveToPoint[1]);
        }

        // 单位：米
        var distance = presentLngLat.distance(moveLngLat);
        return distance;
    },
    replaceUnusual: function (source, field, excessor) {
        if (source.unusual === 1){
            return '--';
        }
        if (Util.noUndefineOrNull(source[field])){
            if (excessor === undefined || excessor === null){
                return source[field];
            }
            if (typeof excessor === 'function'){
                return excessor.call(null,source[field]);
            }
            if (excessor instanceof Array){
                var tmp = source[field];
                for (var i = 0; i < excessor.length; i++){
                    var result = excessor.call(null,tmp);
                    if (result !== false){
                        tmp = result;
                    }
                }
                return tmp;
            }
            return source[field];
        }
        return '--';
    },
    shadowCopyArray:function(source){
        var result = [];
        for (var i = 0, len = source.length; i < len; i++){
          result.push(source[i]);
        }
        return result;
    }
}