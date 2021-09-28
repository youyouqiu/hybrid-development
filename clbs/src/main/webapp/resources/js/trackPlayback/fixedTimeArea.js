var FixedTimeArea = function (options, dependency) {
    this.dependency = dependency;
    this.map = dependency.get('map').map;
    this.areaListStr = '';
    this.groupName = '';

    this.init();
};

FixedTimeArea.prototype.init = function () {
    var dataDependency = this.dependency.get('data');
    this.initLayDate();

    // 绑定事件
    $("#trackPlayQuery").on("click", this.trackDataQuery.bind(this));
    $('.areaTool').on('click', this.showAreaTool.bind(this));
    $('#groupSelect,#groupSelectSpan').on('click', showMenuContent);
    $('#specialTimePlayBack').on('click', this.fixedTimeAreaShow.bind(this));
    $("#addWayToPoint").on("click", this.addTimeBox.bind(this));
    $("#addMapArea").on("click", this.addArea.bind(this));
    $(".areaInput").on("click", this.dependency.get('map').toolClickList.bind(this.dependency.get('map')));
    $('#areaTimeSearch').on('click', this.searchTimeArea.bind(this));
    $("#searchCarExport").on("click", this.fixAreaExport.bind(this));
    $("#fixAreaClose").on("click", this.fixAreaClose.bind(this));
};
//定时定区域初始化
FixedTimeArea.prototype.initLayDate = function(){
    var todayDate = this.getToDay();
    var todayDateArr = todayDate.split('--');
    $('#areaSearchTimeInterval').val(todayDate);

    laydate.render({
        elem: '#searchTimeIntervalStart'
        ,type: 'datetime'
        ,value: todayDateArr[0]
        ,btns: ['now', 'confirm']
        ,done: function(value, date, endDate){
            var endTime = $('#searchTimeIntervalEnd').val();
            var rangeTime = value + '--' + endTime;

            $('#areaSearchTimeInterval').val(rangeTime);
        }
    });//开始时间
    laydate.render({
        elem: '#searchTimeIntervalEnd'
        ,type: 'datetime'
        ,value: todayDateArr[1]
        ,btns: ['now', 'confirm']
        ,done: function(value, date, endDate){
            var startTime = $('#searchTimeIntervalStart').val();
            var rangeTime = startTime + '--' + value;

            $('#areaSearchTimeInterval').val(rangeTime);
        }
    });//结束时间
}

FixedTimeArea.prototype.showAreaTool = function () {
    if ($("#realTimeCanArea").hasClass("realTimeCanAreaShow")) {
        $("#realTimeCanArea").removeClass("realTimeCanAreaShow");
    } else {
        $("#realTimeCanArea").addClass("realTimeCanAreaShow");
    }
};

FixedTimeArea.prototype.fixedTimeAreaShow = function () {
    $(".areaInput:eq(0)").click();
    var dataDependency = this.dependency.get('data');
    var vehicleId = dataDependency.getVid();
    if (vehicleId) {
        var zTreeObj = $.fn.zTree.getZTreeObj('areaTreeDemo');
        var nodes = zTreeObj.getNodesByParam("id", vehicleId, null);
        if (nodes[0]) {
            zTreeObj.checkNode(nodes[0], true, true);
            this.dependency.get('areaMonitorTree').setMenuCrrentSubV(vehicleId);
            this.dependency.get('areaMonitorTree').getCharSelect(zTreeObj);
            this.dependency.get('areaMonitorTree').getCheckedNodes();
        }
    }

    // $("#searchTimeInterval").dateRangePicker({
    //     'dateLimit': 1,
    //     start_time:dataDependency.getStartTime(),
    //     end_time:dataDependency.getEndTime()
    // });
    /*if (dataDependency.getStartTime()) {
        $("#searchTimeInterval").val(dataDependency.getStartTime() + '--' + dataDependency.getEndTime())
    }*/

    $('#fixedTimeArea').show();
    $('#realTimeCanArea').removeClass('realTimeCanAreaShow');
};

//查询
FixedTimeArea.prototype.trackDataQuery = function () {
    var vehicleId = this.dependency.get('data').getVid();
    if (!vehicleId) {
        layer.msg('请选择监控对象');
        return;
    }
    var deviceType = this.dependency.get('data').getSensorType();
    var chooseDate = $("#timeInterval").val().split("--");
    var ssdate = chooseDate[0];
    var sstimestamp = new Date(ssdate).getTime();
    var eedate = chooseDate[1];
    var eetimestamp = new Date(eedate).getTime();
    if (eetimestamp < sstimestamp) {
        layer.msg(trackDateError, {move: false});
        return false;
    } else if (eetimestamp - sstimestamp > 604799000 && deviceType != "5") {
        layer.msg(trackVehicleDateError, {move: false});
        return false;
    } else if (eetimestamp - sstimestamp > 259199000 && deviceType == "5") {
        layer.msg(trackPeopleDateError, {move: false});
        return false;
    }
    this.dependency.get('data').setStartEndTime(chooseDate[0], chooseDate[1]);
    $('#realTimeCanArea').removeClass('realTimeCanAreaShow');
};

// 添加及删除时间选择器
FixedTimeArea.prototype.addTimeBox = function (e) {
    var _this = $(e.target);
    var html = '<div class="form-group addItem">' +
        '<div class="col-md-10" style="padding-right:0;">' +
        '      <input style="cursor: pointer; background-color: #fafafa;float:left;width:45.5%;"' +
        '             class="form-control layer-date laydate-icon"' +
        '             id="searchTimeIntervalStart1" readonly>' +
        '      <span style="float: left;width:5%;text-align: center;margin-top:8px;">~</span>' +
        '           <input style="cursor: pointer; background-color: #fafafa;float:left;width:45.5%;"' +
        '              class="form-control layer-date laydate-icon"' +
        '              id="searchTimeIntervalEnd1" readonly>' +
        '<input type="hidden" id="searchTimeInterval1" name="timeInterval">'+
        '</div>'+
        '      <button id="removeWayToPoint" type="button"' +
        '       class="btn btn-danger padBottom">' +
        '        <span class="glyphicon glyphicon-trash"' +
        '         aria-hidden="true"></span>' +
        '      </button>' +
        '    </div>';

    if ($('#removeWayToPoint').length == 0) {
        _this.closest('.form-group').after(html);
        var yesterDay = this.getYesterDay();
        var yesterDayArr = yesterDay.split('--');
        var searchTimeInterval1 = $("#searchTimeInterval1");
        searchTimeInterval1.val(yesterDay);

        laydate.render({
            elem: '#searchTimeIntervalStart1'
            ,type: 'datetime'
            ,isInitValue: true
            ,value: yesterDayArr[0]
            ,btns: ['now', 'confirm']
            ,done: function(value, date, endDate){
                var endTime = $('#searchTimeIntervalEnd1').val();
                var rangeTime = value + '--' + endTime;

                searchTimeInterval1.val(rangeTime);
            }
        });
        laydate.render({
            elem: '#searchTimeIntervalEnd1'
            ,type: 'datetime'
            ,isInitValue: true
            ,value: yesterDayArr[1]
            ,btns: ['now', 'confirm']
            ,done: function(value, date, endDate){
                var startTime = $('#searchTimeIntervalStart1').val();
                var rangeTime = startTime + '--' + value;

                searchTimeInterval1.val(rangeTime);
            }
        });

        $("#removeWayToPoint").on("click", this.removeBox.bind(this));
    }
};
FixedTimeArea.prototype.removeBox = function (e) {
    var timeGroup = $(e.target).closest('.form-group');
    timeGroup.remove();
};

FixedTimeArea.prototype.getYesterDay = function () {
    var nowDate = new Date();
    var date = new Date(nowDate.getTime() - 24 * 60 * 60 * 1000);
    var seperator1 = "-";
    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    var strDate = date.getDate();
    if (month >= 1 && month <= 9) {
        month = "0" + month;
    }
    if (strDate >= 0 && strDate <= 9) {
        strDate = "0" + strDate;
    }
    var yesterdate = year + seperator1 + month + seperator1 + strDate;
    return yesterdate + ' 00:00:00' + '--' + yesterdate + ' 23:59:59';
};
FixedTimeArea.prototype.getToDay = function () {
    var date = new Date();
    var seperator1 = "-";
    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    var strDate = date.getDate();
    if (month >= 1 && month <= 9) {
        month = "0" + month;
    }
    if (strDate >= 0 && strDate <= 9) {
        strDate = "0" + strDate;
    }
    var today = year + seperator1 + month + seperator1 + strDate;
    return today + ' 00:00:00' + '--' + today + ' 23:59:59';
};

// 添加及删除区域
FixedTimeArea.prototype.addArea = function (e) {
    var _this = $(e.target);
    var html = '<div class="form-group addItem">' +
        '       <div class="col-md-10">' +
        '        <input type="text" name="areaTwo" class="form-control areaInput" placeholder="点击后在地图上选择区域" readonly>' +
        '       </div>' +
        '       <button id="removeMapArea" type="button"' +
        '        class="btn btn-danger padBottom">' +
        '         <span class="glyphicon glyphicon-trash"' +
        '          aria-hidden="true"></span>' +
        '       </button>' +
        '      </div>';
    if ($('#removeMapArea').length == 0) {
        _this.closest('.form-group').after(html);
        $("#removeMapArea").on("click", this.dependency.get('map').delArea.bind(this.dependency.get('map')));
        $(".areaInput").on("click", this.dependency.get('map').toolClickList.bind(this.dependency.get('map')));
        $(".areaInput:eq(1)").click();
    }
};


FixedTimeArea.prototype.validatesArea = function () {
    return $("#timeAreaForm").validate({
        ignore: '',
        onfocusout: false,
        rules: {
            groupSelect: {
                zTreeChecked: "areaTreeDemo"
            },
            area: {
                required: true,
            },
            areaTwo: {
                required: true
            },
            timeInterval: {
                isRange24: true,
                isDateTimeCompare: true
            }
        },
        messages: {
            groupSelect: {
                zTreeChecked: vehicleSelectBrand,
            },
            area: {
                required: '请在地图上选择区域'
            },
            areaTwo: {
                required: '请在地图上选择区域'
            }
        }
    }).form();
};

// 组装区域查询的开始结束时间
FixedTimeArea.prototype.getAreaTime = function (target) {
    if ($(target).length == 0) return null;
    var dateOne = $(target).val().split("--");
    var startTime = dateOne[0];
    var endTime = dateOne[1];
    startTime = new Date(Date.parse(startTime.replace(/-/g, "/")));
    startTime = startTime.getTime() / 1000;
    endTime = new Date(Date.parse(endTime.replace(/-/g, "/")));
    endTime = endTime.getTime() / 1000;
    return [startTime, endTime]
};

// 组装区域查询的经纬度
FixedTimeArea.prototype.getAreaLonglat = function (changeArray, areaName) {
    if (!changeArray) return null;
    var leftToplongtitude = changeArray.getSouthWest().getLng();
    var leftTopLatitude = changeArray.getSouthWest().getLat();
    var rightFloorlongtitude = changeArray.getNorthEast().getLng();
    var rightFloorLatitude = changeArray.getNorthEast().getLat();
    var obj = {
        'areaId': areaName,
        'leftTopLongitude': leftToplongtitude,
        'leftTopLatitude': leftTopLatitude,
        'rightFloorLongitude': rightFloorlongtitude,
        'rightFloorLatitude': rightFloorLatitude,
    };
    return obj;
};
// 多时段多区域查询
FixedTimeArea.prototype.searchTimeArea = function () {
    var map = this.dependency.get('map');
    this.dependency.get('areaMonitorTree').getCheckedNodes();
    if (this.validatesArea()) {
        var searchTimeInterval1 = $('#searchTimeInterval1');
        if (searchTimeInterval1 && searchTimeInterval1.val() == $('#areaSearchTimeInterval').val()) {
            layer.msg('请勿选择两个相同时间');
            return;
        }
        layer.load(2);
        var timeOne = this.getAreaTime('#areaSearchTimeInterval');
        var searchTimeTwo = this.getAreaTime('#searchTimeInterval1');

        var rangeAreaPos = this.dependency.get('data').getRangeAreaPos();
        var changeArray = rangeAreaPos[0];// 区域1
        var changeArrayTwo = rangeAreaPos[1];// 区域2
        var areaArr = [];
        var obj = this.getAreaLonglat(changeArray, 'areaOne');
        areaArr.push(obj);
        if (changeArrayTwo) {
            var obj = this.getAreaLonglat(changeArrayTwo, 'areaTwo');
            areaArr.push(obj);
        }

        var allVid = this.dependency.get('data').getAreaTreeCheckVid();
        var param = {
            "monitorIds": allVid,
            "areaListStr": JSON.stringify(areaArr),
            "startTimeOne": timeOne[0],
            "endTimeOne": timeOne[1],
            "startTimeTwo": searchTimeTwo ? searchTimeTwo[0] : undefined,
            "endTimeTwo": searchTimeTwo ? searchTimeTwo[1] : undefined,
        };
        this.areaListStr = areaArr;
        this.groupName = $('#groupSelect').val();
        var url = "/clbs/v/monitoring/findHistoryByTimeAndAddress";
        json_ajax("POST", url, "json", false, param, this.dependency.get('map').areaTableInit.bind(map));
    }
};
//时间戳转换为指定格式
FixedTimeArea.prototype.turnTimeFormat = function (time) {
    var value;
    var date = new Date(time * 1000);
    var Y = date.getFullYear() + '-';
    var M = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1) + '-';
    var D = (date.getDate() < 10 ? "0" + date.getDate() : date.getDate()) + ' ';
    var h = (date.getHours() < 10 ? "0" + date.getHours() : date.getHours()) + ':';
    var m = (date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes()) + ':';
    var s = (date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds());
    value = Y + M + D + h + m + s;
    return value;
};
//进出区域tr点击事件
FixedTimeArea.prototype.fenceTurnoverTime = function (e, _this) {
    var vehicleId = $(e).attr('data-id');
    var index = $(e).attr('data-index');
    var rangeAreaPos = _this.dependency.get('data').getRangeAreaPos();
    var changeArray = rangeAreaPos[index];

    // 将日期转换为时间戳
    var thisTime = $(e).attr('data-time').split('--');
    var startTime = thisTime[0];
    var endTime = thisTime[1];
    startTime = new Date(Date.parse(startTime.replace(/-/g, "/")));
    startTime = startTime.getTime() / 1000;
    endTime = new Date(Date.parse(endTime.replace(/-/g, "/")));
    endTime = endTime.getTime() / 1000;
    thisTime = [startTime, endTime];

    var leftToplongtitude = changeArray.getSouthWest().getLng();
    var leftTopLatitude = changeArray.getSouthWest().getLat();
    var rightFloorlongtitude = changeArray.getNorthEast().getLng();
    var rightFloorLatitude = changeArray.getNorthEast().getLat();
    var url = "/clbs/v/monitoring/getQueryDetails";
    var turnoverClickID = $(e).next('tr').children('td').children('table').children('tbody').attr('id');
    if ($("#" + turnoverClickID).html() == '') {
        var data = {
            "leftTopLongitude": leftToplongtitude,
            "leftTopLatitude": leftTopLatitude,
            "rightFloorLongitude": rightFloorlongtitude,
            "rightFloorLatitude": rightFloorLatitude,
            "vehicleId": vehicleId,
            "startTime": thisTime[0],
            "endTime": thisTime[1]
        };
        ajax_submit("POST", url, "json", true, data, true, function (data) {
            if (data.success) {
                var html = '';
                var obj = data.obj;
                for (var i = 0; i < obj.length; i++) {
                    if (obj[i][0] != '') {
                        var time = obj[i][0] == '已在区域内'
                            ? '已在区域内'
                            : _this.dependency.get('fixedTimeArea').turnTimeFormat(obj[i][0]);
                        html += '<tr><td>' + (i + 1) + '</td><td>' + time + '</td>';
                    } else {
                        html += '<tr><td>' + (i + 1) + '</td><td>未进区域</td>';
                    }
                    ;
                    if (obj[i][1] != '') {
                        var time = _this.dependency.get('fixedTimeArea').turnTimeFormat(obj[i][1]);
                        html += '<td>' + time + '</td></tr>';
                    } else {
                        html += '<td>未出区域</td></tr>';
                    }
                }
                $("#" + turnoverClickID).html(html);
                $("#" + turnoverClickID).parents("td.areaSearchTable").slideDown(200);
            }
        });
    } else {
        var $td = $("#" + turnoverClickID).parents("td.areaSearchTable");
        if ($td.is(":hidden")) {
            $td.slideDown(200);
        } else {
            $td.slideUp(200);
        }
    }
};


// 导出
FixedTimeArea.prototype.fixAreaExport = function () {
    var areaOneLength = $('#areaSearchCar .areaTrOne:visible').length;
    var areaTwoLength = $('#areaSearchCar .areaTrTwo:visible').length;
    if (areaOneLength === 0 && areaTwoLength === 0) {
        layer.msg('无数据导出');
        return;
    }
    var areaListArr = [];
    for (var i = 0; i < this.areaListStr.length; i++) {
        var item = this.areaListStr[i];
        var areaName = item.areaId;
        if (areaName == 'areaOne' && areaOneLength > 0) {
            areaListArr.push(item)
        }
        if (areaName == 'areaTwo' && areaTwoLength > 0) {
            areaListArr.push(item);
        }
    }
    var param = {
        'groupName': this.groupName,
        'areaListStr': JSON.stringify(areaListArr).replace(/\"/g,"'")
    };
    var url = '/clbs/v/monitoring/timeZoneExport';
    exportExcelUseForm(url, param);
};

// 关闭定时定区域查询弹窗
FixedTimeArea.prototype.fixAreaClose = function () {
    $('#fixedTimeArea').hide();
    $('.areaInput').val('');
    $('.addItem').remove();
    $('.delArea').remove();
    $('#fixedTimeArea label.error').remove();
    $('#groupSelect').val('');
    this.dependency.get('data').setAreaTreeAllCheck(false);
    $('#queryType').val('vehicle');
    this.dependency.get('areaMonitorTree').treeInit();
    var map = this.dependency.get('map');
    map.areaDestory.bind(map)();

    this.initLayDate();
};
