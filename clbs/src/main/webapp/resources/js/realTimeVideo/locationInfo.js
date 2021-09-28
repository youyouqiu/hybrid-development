define(function () {
    var subscribeInfo = {
        setInfo: function (data) {
            if (data) {
                var msgBody = data.data.msgBody;
                var info = {
                    vehicleName: msgBody.monitorInfo.monitorName, // 监控对象名称
                    time: this.timeTransform(msgBody.gpsTime), // 定位时间
                    deviceNumber: msgBody.monitorInfo.deviceNumber, // 终端号
                    simcardNumber: msgBody.monitorInfo.simcardNumber, // 终端手机号
                    professionalsName: msgBody.monitorInfo.professionalsName === '' ||
                    msgBody.monitorInfo.professionalsName === 'null' ||
                    msgBody.monitorInfo.professionalsName === null ||
                    msgBody.monitorInfo.professionalsName === undefined ? '-' : msgBody.monitorInfo.professionalsName, // 从业人员
                    acc: msgBody.acc === 1 ? '开' : '关',
                    speed: msgBody.gpsSpeed, // 速度
                    positionDescription: msgBody.positionDescription === '' ||
                    msgBody.positionDescription == null ||
                    msgBody.positionDescription === 'null' ? '未定位' : msgBody.positionDescription // 位置信息
                };
                this.updateSubscribeInfo(info);
            } else {
                this.clearSubscribeInfo();
            }
        },
        /**
         * 时间格式转换
         */
        timeTransform: function (time) {
            if (time) {
                return 20 + time.substring(0, 2) + "-" + time.substring(2, 4) + "-" + time.substring(4, 6) + " " +
                    time.substring(6, 8) + ":" + time.substring(8, 10) + ":" + time.substring(10, 12);
            }
            return '';
        },
        /**
         * 更新订阅信息
         */
        updateSubscribeInfo: function (data) {
            $('#vehicleName').text(data.vehicleName);
            $('#updateTime').text(data.time);
            $('#deviceNo').text(data.deviceNumber);
            $('#simNo').text(data.simcardNumber);
            $('#driverName').text(data.professionalsName);
            $('#accStatus').text(data.acc);
            $('#updateSpeed').text(data.speed + 'km/h');
            $('#formattedAddress').text(data.positionDescription);
        },
        /**
         * 清除当前聚焦监控对象显示的订阅信息
         */
        clearSubscribeInfo: function () {
            $('#vehicleName').text('');
            $('#updateTime').text('');
            $('#deviceNo').text('');
            $('#simNo').text('');
            $('#driverName').text('');
            $('#accStatus').text('');
            $('#updateSpeed').text('');
            $('#formattedAddress').text('');
        }
    };

    return {
        subscribeInfo: subscribeInfo,
    };
});