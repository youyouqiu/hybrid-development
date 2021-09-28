var isTableFirst = true;
(function (window, $) {

    humitureAlarmTable = {
        //解析位置信息
        getAlarmAddress: function (latitude, longitude, e) {
            var url = '/clbs/v/monitoring/address';
            var param = {addressReverse: [latitude, longitude, '', "", 'vehicle']};
            if (e) e.stopPropagation();
            $.ajax({
                type: "POST",//通常会用到两种：GET,POST。默认是：GET
                url: url,//(默认: 当前页地址) 发送请求的地址
                dataType: "json", //预期服务器返回的数据类型。"json"
                async: true, // 异步同步，true  false
                data: param,
                traditional: true,
                timeout: 8000, //超时时间设置，单位毫秒
                success: function (data) {//请求成功
                    if (e) {
                        $(e.target).closest('td').html($.isPlainObject(data) ? '未定位' : data);
                    }
                },
            });
        },
        // 处理报警
        updataRealAlarmMessage: function (data) {
            if (isTableFirst) {
                $("#alarmTable tbody").empty();
                isTableFirst = false;
            }
            data = JSON.parse(data.body).data;
            // 只显示温湿度报警
            var alarmSet = data.msgBody.pushAlarmSet;
            if (alarmSet && alarmSet.indexOf('66') == -1 && alarmSet.indexOf('65') == -1) {
                return;
            }
            var alarmTime;
            var time = data.msgBody.gpsTime;
            if (time.length == 12) {
                alarmTime = 20 + time.substring(0, 2) + "-" + time.substring(2, 4) + "-" + time.substring(4, 6) + " " +
                    time.substring(6, 8) + ":" + time.substring(8, 10) + ":" + time.substring(10, 12);
            } else {
                alarmTime = "时间异常";
            }
            var wenduInfo = '', wenduArray = [];
            var shiduInfo = '', shiduArray = [];
            var wenduNumber = '', wenduNumberArray = [];
            var shiduNumber = '', shiduNumberArray = [];

            var longitude = data.msgBody.longitude;
            var latitude = data.msgBody.latitude;

            if (data.msgBody.alarmTempList != null) {
                for (var i = 0; i < data.msgBody.alarmTempList.length; i++) {
                    var item = data.msgBody.alarmTempList[i];
                    // if(item.highLow != 0){
                    // var index = parseInt(item.id.toString(16)) - 20;
                    // wenduArray.push( '温度传感' + index + ': ' + (item.temperature) + ' ℃  ' );
                    // wenduNumberArray.push(  (item.temperature) + ' ℃  ' );
                    wenduArray.push('温度传感' + (i + 1) + ': ' + (item / 10).toFixed(1) + '℃  ');
                    wenduNumberArray.push((item / 10).toFixed(1) + '℃  ');
                    // }
                }
            }
            if (data.msgBody.alarmWetnessList != null) {
                for (var i = 0; i < data.msgBody.alarmWetnessList.length; i++) {
                    var item = data.msgBody.alarmWetnessList[i];
                    // if(item.highLow != 0){
                    // var index = parseInt(item.id.toString(16)) - 25;
                    // shiduArray.push( '湿度传感' + index + ': ' + (item.temperature) + ' %RH  ' );
                    // shiduNumberArray.push(  (item.temperature) + ' %RH  ' );
                    shiduArray.push('湿度传感' + (i + 1) + ': ' + (item.toString()) + '%RH  ');
                    shiduNumberArray.push((item.toString()) + '%RH  ');
                    // }
                }
            }
            wenduInfo = wenduArray.join(', ');
            shiduInfo = shiduArray.join(', ');
            wenduNumber = wenduNumberArray.join(', ');
            shiduNumber = shiduNumberArray.join(', ');

            // var alarmAddress = (data.msgBody.positionDescription === null ||
            // data.msgBody.positionDescription === undefined) ?
            // '未定位' : data.msgBody.positionDescription;

            /* var addressStr = humitureAlarmTable.getAlarmAddress(latitude, longitude);
             var addr;
             if (addressStr.length > 10) {
                 addr = addressStr.substr(0, 10) + '...';
             } else {
                 addr = addressStr;
             }*/

            var listDates = '<tr>'
                + '<td class="nowrap">' + data.msgBody.monitorInfo.monitorName + '</td>'
                + '<td  class="demo demoUp" alt="' + data.msgBody.alarmName + '">' + data.msgBody.alarmName.substr(0, 10) + '...</td>'
                + '<td>' + alarmTime + '</td>'
                + '<td><a href="#" onclick="humitureAlarmTable.getAlarmAddress(\'' + latitude + '\',\'' + longitude + '\',event)">点击获取位置信息</a></td>'
                + '<td  class="demo demoUp" alt="' + wenduInfo + '">' + wenduNumber + '</td>'
                + '<td  class="demo demoUp" alt="' + shiduInfo + '">' + shiduNumber + '</td>'
                + '<td>220v</td>'
                + '</tr>';
            $("#alarmTable tbody").prepend(listDates);
            // 报警列表数据过多时,只保留最新的500条数据
            if ($("#alarmTable tbody tr").length > 600) {
                $("#alarmTable tbody tr:gt(499)").remove();
            }
            setTimeout(function () {
                $(".demoUp").mouseover(function () {

                    var _this = $(this);
                    if (_this.attr("alt")) {
                        _this.justToolsTip({
                            animation: "moveInTop",
                            width: "auto",
                            contents: _this.attr("alt"),
                            gravity: 'top'
                        });
                    }
                })
            }, 1000)
        },
    };
    $(function () {
    })
})(window, $);