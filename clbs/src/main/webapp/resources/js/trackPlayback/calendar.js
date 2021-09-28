var Calendar = function (selector, options, dependency) {
    this.options = options;
    this.dependency = dependency;
    this.$origin = $(selector);
    var nowDate = new Date();
    this.nowMonth = nowDate.getFullYear() + "-" + (parseInt(nowDate.getMonth() + 1) < 10 ? "0" + parseInt(nowDate.getMonth() + 1) : parseInt(nowDate.getMonth() + 1)) + "-01"
    this.afterMonth = nowDate.getFullYear() + "-" + (parseInt(nowDate.getMonth() + 2) < 10 ? "0" + parseInt(nowDate.getMonth() + 2) : parseInt(nowDate.getMonth() + 2)) + "-01";
    this.nowYear = nowDate.getFullYear();
    this.calendar = this.$origin.calendarTrack();
}

//轨迹数据添加到日历
Calendar.prototype.getValidDate = function () {
    var dataDependency = this.dependency.get('data');
    if (!dataDependency.getActiveTreeNode()) return;
    var vehicleId = dataDependency.getActiveTreeNode().id;
    var nowMonth = dataDependency.getNowMonth();
    var afterMonth = dataDependency.getAfterMonth();

    var dataTime = nowMonth.split("-")[0] + nowMonth.split("-")[1];
    $.ajax({
        type: "POST",
        url: "/clbs/v/monitoring/getActiveDate",
        data: {
            "vehicleId": vehicleId,
            "nowMonth": nowMonth,
            "nextMonth": afterMonth,
            "type": dataDependency.getSensorType(),
            "bigDataFlag": 1
        },
        dataType: "json",
        async: true,
        beforeSend: function () {
            layer.load(2);
        },
        success: function (data) {
            layer.closeAll('loading');
            timeArray = [];
            stopArray = [];
            peopleArray = [];
            thingArray = [];
            if (data.success) {
                //车的详细信息
                var msg = $.parseJSON(data.msg);
                var activeDate = msg.date;
                sensorTypeArr = msg.dailySensorFlag;
                var mileage = msg.dailyMile;
                dataDependency.setValidDate(activeDate);
                dataDependency.setValidSensorFlag(sensorTypeArr);
                dataDependency.setValidMile(mileage);
                for (var i = 0; i < activeDate.length; i++) {
                    var time = dataTime + (parseInt(activeDate[i] + 1) < 10 ? "0" + parseInt(activeDate[i] + 1) : parseInt(activeDate[i] + 1));
                    var mileagei = parseFloat(mileage[i]);
                    if (mileagei.toString().indexOf('.') > 0) {
                        mileagei = mileagei.toFixed(1);
                    }
                    switch (msg.type) {
                        case "5" : // BDTD-SM
                            peopleArray.push([time, time, ""]);
                            break;
                        case "9" : // ASO
                        case "10" : // F3超长待机
                            cdWorldType = msg.type;
                            stopArray.push([time, time, mileagei]);
                            break;
                        default:
                            timeArray.push([time, time, mileagei]);
                            break;
                    }
                }
            }
            if (this.dependency.get('flags').objType == 'thing') {
                thingArray = timeArray.concat(peopleArray);
            }
            // var zTreeDemoHeight = $("#treeDemo").height();
            var oldLength = $(".track-playback-content .calendar3 tbody tr").length;
            this.$origin.html("");
            this.$origin.calendarTrack({
                highlightRange: timeArray,
                stopHighlightRange: stopArray,
                peopleHighlightRange: peopleArray,
                thingHighlightRange: thingArray
            });
            // var trBtnLength = $(".track-playback-content .calendar3 tbody tr").length;
            // if (trBtnLength > oldLength) {
            //     $("#treeDemo").css("height", (zTreeDemoHeight - 34) + "px");
            // } else if (trBtnLength < oldLength) {
            //     $("#treeDemo").css("height", (zTreeDemoHeight + 54) + "px");
            // }
            $('.track-playback-content .calendar3 tbody td').each(function () {
                if ($(this).hasClass("widget-disabled")) {
                    $(this).removeClass("widget-highlight").removeClass("widget-stopHighlight");
                    $(this).children("span").children("span.mileageList").text("-");
                }
            })
            this.dependency.get('flags').isFlag = false;
        }.bind(this)
    });
}