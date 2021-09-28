var humitureWatch;
var myChart;
(function ($, window) {
    var startTime;
    var endTime;
    var base = +new Date(2016, 5, 1, 00, 00, 00);
    var date = [];//图形表时间
    var nullData=[];
    var tempOne=[];//一号温度传感器温度
    var tempTwo=[];//二号温度传感器温度
    var tempThree=[];//三号温度传感器温度
    var tempFour=[];//四号温度传感器温度
    var tempFive=[];//五号温度传感器温度
    var humidityOne = [];//一号湿度传感器湿度
    var humidityTwo = [];//二号湿度传感器湿度
    var humidityThree = [];//三号湿度传感器湿度
    var humidityFour = [];//四号湿度传感器湿度
    
    var tempList=[];
    var dataTemp=[];
//    var option={};
    var vehicleId;
    var message = [];
    var tempMin = -30;
    var tempMax = 0;
    humitureWatch = {
        init: function () {
            myChart = echarts.init(document.getElementById('temAndHumDataShow'));
            myChart.clear();
            var option = {
                tooltip: {
                    trigger: 'axis',
                    textStyle: {
                        fontSize: 20
                    },
                    formatter: function (a) {
                        var unit = ['Km','Km/h','°C'];
                        var relVal = "";
                        relVal = a[0].name;
                        var allNull = true;
                        for(var i = 0; i < a.length; i++){
                            if (a[i].data !== null){
                                allNull = false;
                                break;
                            }
                        }
                        if(allNull){
                            relVal = "无相关数据";
                        }else{
                            for(var i = 0; i < a.length; i++){
                                if(a[i].seriesName == "温度传感器1"){
                                    if(a[i].data == null){
                                    }else{
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:"+ a[i].color +"'></span>"+ a[i].seriesName +"："+a[i].data+"℃";
                                    }
                                }else if(a[i].seriesName == "温度传感器2"){
                                    if(a[i].data == null){
                                    }else{
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:"+ a[i].color +"'></span>"+ a[i].seriesName +"："+a[i].data+"℃";
                                    }
                                }else if(a[i].seriesName == "温度传感器3"){
                                    if(a[i].data == null){
                                    }else{
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:"+ a[i].color +"'></span>"+ a[i].seriesName +"："+a[i].data+"℃";
                                    }
                                }else if(a[i].seriesName == "温度传感器4"){
                                    if(a[i].data == null){
                                    }else{
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:"+ a[i].color +"'></span>"+ a[i].seriesName +"："+a[i].data+"℃";
                                    }
                                }else if(a[i].seriesName == "温度传感器5"){
                                    if(a[i].data == null){
                                    }else{
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:"+ a[i].color +"'></span>"+ a[i].seriesName +"："+a[i].data+"℃";
                                    }
                                } else if(a[i].seriesName == "湿度传感器1"){
                                    if(a[i].data == null ){
                                    }else{
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:"+ a[i].color +"'></span>"+ a[i].seriesName +"："+a[i].data+"%";
                                    }
                                }else if(a[i].seriesName == "湿度传感器2"){
                                    if(a[i].data == null ){
                                    }else{
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:"+ a[i].color +"'></span>"+ a[i].seriesName +"："+a[i].data+"%";
                                    }
                                }else if(a[i].seriesName == "湿度传感器3"){
                                    if(a[i].data == null ){
                                    }else{
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:"+ a[i].color +"'></span>"+ a[i].seriesName +"："+a[i].data+"%";
                                    }
                                }else if(a[i].seriesName == "湿度传感器4"){
                                    if(a[i].data == null){
                                    }else{
                                        relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:"+ a[i].color +"'></span>"+ a[i].seriesName +"："+a[i].data+"%";
                                    }
                                }
                            }
                        }
                        return relVal;
                    }
                },
                toolbox: {
                    show: false,
                },
                xAxis: {
                    type: 'category',
                    boundaryGap: false,
                    data: date
                },
                yAxis: [
                    {
                        type: 'value',
                        name: '湿度(%RH)',
                        scale: true,
                        position: 'right',
                        min: 0,
                        max: 100,
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine:{
                            show:false
                        }
                    },
                    {
                        type: 'value',
                        name: '温度(°C)',
                        scale: true,
                        position: 'left',
                        min: tempMin,
                        max: tempMax,
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine:{
                            show:false
                        }
                    },
                ],
                dataZoom: [{
                    type: 'inside',

                }, {
                    start: 0,
                    end: 10,
                    handleIcon: 'M10.7,11.9v-1.3H9.3v1.3c-4.9,0.3-8.8,4.4-8.8,9.4c0,5,3.9,9.1,8.8,9.4v1.3h1.3v-1.3c4.9-0.3,8.8-4.4,8.8-9.4C19.5,16.3,15.6,12.2,10.7,11.9z M13.3,24.4H6.7V23h6.6V24.4z M13.3,19.6H6.7v-1.4h6.6V19.6z',
                    handleSize: '80%',
                    handleStyle: {
                        color: '#fff',
                        shadowBlur: 3,
                        shadowColor: 'rgba(0, 0, 0, 0.6)',
                        shadowOffsetX: 2,
                        shadowOffsetY: 2
                    }
                }],
                legend: {
                    data:globalLineLegend
                },
                series: [
                    {
                        name: '温度传感器1',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: true,
//                        symbol: 'none',
//                        symbolSize :15,
//                        showSymbol: false,
//                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: '#f4b5bd'
                            }
                        },
//                        areaStyle: {
//                        	opacity:0.03
//                        },
                        data: tempOne,
                    },
                    {
                        name: '温度传感器2',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: false,
//                        symbol: 'none',
//                        symbolSize :15,
//                        showSymbol: false,
                        itemStyle: {
                            normal: {
                                color: '#e47c8c'
                            }
                        },
//                        areaStyle: {
//                        	opacity:0.03
//                        },
                        data: tempTwo
                    },
                    {
                        name: '温度传感器3',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: false,
//                        symbol: 'none',
//                        symbolSize :15,
//                        showSymbol: false,
                        itemStyle: {
                            normal: {
                                color: '#e35052'
                            }
                        },
//                        areaStyle: {
//                        	opacity:0.03
//                        },
                        data: tempThree
                    },
                    {
                        name: '温度传感器4',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: false,
//                        symbol: 'none',
//                        symbolSize :15,
//                        showSymbol: false,
                        itemStyle: {
                            normal: {
                                color: '#f33168'
                            }
                        },
                        data: tempFour
                    },
                    {
                        name: '温度传感器5',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: false,
//                        symbol: 'none',
//                        symbolSize :15,
//                        showSymbol: false,
                        itemStyle: {
                            normal: {
                                color: '#a40c0d'
                            }
                        },
                        data: tempFive
                    },
                    {
                        name: '湿度传感器1',
                        yAxisIndex: 0,
                        type: 'line',
                        smooth: false,
//                        symbol: 'none',
//                        symbolSize :15,
//                        showSymbol: false,
                        itemStyle: {
                            normal: {
                                color: '#bae7ff'
                            }
                        },
//                        areaStyle: {
//                        	opacity:0.03
//                        },
                        data: humidityOne
                    },
                    {
                        name: '湿度传感器2',
                        yAxisIndex: 0,
                        type: 'line',
                        smooth: false,
//                        symbol: 'none',
//                        symbolSize :15,
//                        showSymbol: false,
                        itemStyle: {
                            normal: {
                                color: '#40a9ff'
                            }
                        },
//                        areaStyle: {
//                        	opacity:0.03
//                        },
                        data: humidityTwo
                    },
                    {
                        name: '湿度传感器3',
                        yAxisIndex: 0,
                        type: 'line',
                        smooth: false,
//                        symbol: 'none',
//                        symbolSize :15,
//                        showSymbol: false,
                        itemStyle: {
                            normal: {
                                color: '#0050b3'
                            }
                        },
//                        areaStyle: {
//                        	opacity:0.03
//                        },
                        data: humidityThree
                    },
                    {
                        name: '湿度传感器4',
                        yAxisIndex: 0,
                        type: 'line',
                        smooth: false,
//                        symbol: 'none',
//                        symbolSize :15,
//                        showSymbol: false,
                        itemStyle: {
                            normal: {
                                color: '#002766'
                            }
                        },
                        data: humidityFour
                    },
                ]
            };
//            option.series=option.series.filter(function(ele){
//                if(option.legend.data && option.legend.data.indexOf(ele.name) > -1 ){
//                    return true;
//                }else{
//                    return false;
//                }
//            });
            myChart.setOption(option);
            //  myChart.setOption(option);
            myChart.on('click', function (params) {
            });
//            window.onresize = ;
            
            //console.log(date);


        },
        // 开始时间
        startDay: function (day) {
            var timeInterval = $('#timeInterval').val().split('--');
            var startValue = timeInterval[0];
            var endValue = timeInterval[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * day;
                today.setTime(targetday_milliseconds); // 注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = humitureWatch.doHandleMonth(tMonth + 1);
                tDate = humitureWatch.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * parseInt(num);
                today.setTime(end_milliseconds); // 注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = humitureWatch.doHandleMonth(endMonth + 1);
                endDate = humitureWatch.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " " + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0,10).replace("-","/").replace("-","/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = humitureWatch.doHandleMonth(vMonth + 1);
                vDate = humitureWatch.doHandleMonth(vDate);
                startTime = vYear + "-" + vMonth + "-" + vDate + " "
                    + "00:00:00";
                if(day == 1){
                    endTime = vYear + "-" + vMonth + "-" + vDate + " "
                        + "23:59:59";
                }else{
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = humitureWatch.doHandleMonth(vendMonth + 1);
                    vendDate = humitureWatch.doHandleMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate + " "
                        + "23:59:59";
                }
            }
        },
        doHandleMonth: function (month) {
            var m = month;
            if (month.toString().length == 1) {
                m = "0" + month;
            }
            return m;
        },
        // 当前时间
        nowDay: function () {
            var nowDate = new Date();
            startTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate()) + " " + "00:00:00";
            endTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate())
                + " "
                + ("23")
                + ":"
                + ("59")
                + ":"
                + ("59");

        },
        // ajax请求数据
        ajaxList: function (band, startTime, endTime,flag) {
//            humitureWatch.removeClass();
//            $("#allReport").addClass("active");
//            $("#carName").text($("input[name='charSelect']").val());
//            $(".toopTip-btn-left,.toopTip-btn-right").css("display","inline-block");
            date = [];
            tempOne = [];
            tempTwo=[];
            tempThree=[];
            tempFour=[];
            tempFive=[];
            tempList=[];
            humidityOne = [];
            humidityTwo = [];
            humidityThree = [];
            humidityFour = [];
            dataTemp=[];
            vehicleId = band;
            $.ajax({
                type: "POST",
                url: "/clbs/v/monitoring/humiture/tempHum",
                data: {"startTime": startTime, "endTime": endTime,"vehicleId": band,"flag":flag},
                dataType: "json",
                async: true,
                timeout : 30000, //超时时间设置，单位毫秒
                beforeSend: function () {
                    //异步请求时spinner出现
                    layer.load(2);
                },
                success: function (data) {
                    layer.closeAll('loading');
                    $('#timeInterval').val(startTime + '--' + endTime);
                    if (data.success==true) {
                        if (data.obj.humitureWatch != null && data.obj.humitureWatch != undefined && data.obj.humitureWatch.length != 0) {
                        	var responseData = JSON.parse(ungzip(data.obj.humitureWatch));
                            data.obj.humitureWatch = responseData;
                            dataTemp=data.obj.humitureWatch;
                            var rtime = 0;
                            var chenageTimes=0;
                            for (var i = 0, len = data.obj.humitureWatch.length-1; i <= len; i++) {
                                if(!(Number(data.obj.humitureWatch[i].speed)==0&&Number(data.obj.humitureWatch[i].vTime)==0)){
                                    date.push(humitureWatch.timeStamp2String(humitureWatch.UnixToDate(data.obj.humitureWatch[i].vTime,true)));

                                    tempOne.push((data.obj.humitureWatch[i].tempValueOne));
                                    tempTwo.push((data.obj.humitureWatch[i].tempValueTwo));
                                    tempThree.push((data.obj.humitureWatch[i].tempValueThree));
                                    tempFour.push((data.obj.humitureWatch[i].tempValueFour));
                                    tempFive.push((data.obj.humitureWatch[i].tempValueFive));
                                    humidityOne.push((data.obj.humitureWatch[i].wetnessValueOne));//一号传感器湿度
                                    humidityTwo.push((data.obj.humitureWatch[i].wetnessValueTwo));//二号传感器湿度
                                    humidityThree.push((data.obj.humitureWatch[i].wetnessValueThree));//三号传感器湿度
                                    humidityFour.push((data.obj.humitureWatch[i].wetnessValueFour));//四号传感器湿度
                                }
                                if(i != data.obj.humitureWatch.length-1) {
                                    if (humitureWatch.GetDateDiff(humitureWatch.UnixToDate(data.obj.humitureWatch[i].vtime,true), humitureWatch.UnixToDate(data.obj.humitureWatch[i + 1].vtime,true), "second") <= 300
                                        && humitureWatch.GetDateDiff(humitureWatch.UnixToDate(data.obj.humitureWatch[i].vtime,true), humitureWatch.UnixToDate(data.obj.humitureWatch[i + 1].vtime,true), "second") >= 5) {
                                        changeTime = humitureWatch.GetDateDiff(humitureWatch.UnixToDate(data.obj.humitureWatch[i].vtime,true), humitureWatch.UnixToDate(data.obj.humitureWatch[i + 1].vtime,true), "second");
                                        switch (changeTime) {
                                            case 5:
                                                rtime = 5000;
                                                break;
                                            case 10:
                                                rtime = 10000;
                                                break;
                                            case 15:
                                                rtime = 15000;
                                                break;
                                            case 20:
                                                rtime = 20000;
                                                break;
                                            case 25:
                                                rtime = 25000;
                                                break;
                                            case 30:
                                                rtime = 30000;
                                                break;
                                            case 60:
                                                rtime = 60000;
                                                break;
                                            case 300:
                                                rtime = 300000;
                                                break;
                                            default:
                                                rtime = 0;
                                                break
                                        }
                                    }
                                    if (rtime == 5000 || rtime == 10000 || rtime == 15000 || rtime == 20000 || rtime == 25000 || rtime == 30000 || rtime == 60000 || rtime == 300000) {
                                        switch (rtime) {
                                            case 5000:
                                                chenageTimes = 12;
                                                break;
                                            case 10000:
                                                chenageTimes = 6;
                                                break;
                                            case 15000:
                                                chenageTimes = 4;
                                                break;
                                            case 20000:
                                                chenageTimes = 3;
                                                break;
                                            case 25000:
                                                chenageTimes = 2.4;
                                                break;
                                            case 30000:
                                                chenageTimes = 2;
                                                break;
                                            case 60000:
                                                chenageTimes = 1;
                                                break;
                                            case 300000:
                                                chenageTimes = 1 / 5;
                                                break;
                                        }
                                    }
                                    if (humitureWatch.GetDateDiff(humitureWatch.UnixToDate(data.obj.humitureWatch[i].vTime, true), humitureWatch.UnixToDate(data.obj.humitureWatch[i + 1].vTime, true), "second") > 300) {
                                        nullData += humitureWatch.GetDateDiff(humitureWatch.UnixToDate(data.obj.humitureWatch[i].vTime, true), humitureWatch.UnixToDate(data.obj.humitureWatch[i + 1].vTime, true), "hour");
                                        if (rtime == 0) {
                                            var ctime = +humitureWatch.timeAdd(humitureWatch.UnixToDate(data.obj.humitureWatch[i].vTime, true));
                                            for (var n = 0; n < humitureWatch.GetDateDiff(humitureWatch.UnixToDate(data.obj.humitureWatch[i].vTime, true), humitureWatch.UnixToDate(data.obj.humitureWatch[i + 1].vTime, true), "minute") * 2; n++) {
                                                date.push(humitureWatch.timeStamp2String(new Date(ctime += 30000)));
                                                tempOne.push(null);
                                                tempTwo.push(null);
                                                tempThree.push(null);
                                                tempFour.push(null);
                                                tempFive.push(null);
                                                humidityOne.push(null);//一号传感器湿度
                                                humidityTwo.push(null);//二号传感器湿度
                                                humidityThree.push(null);//三号传感器湿度
                                                humidityFour.push(null);//四号传感器湿度

                                            }
                                        } else {
                                            var ctime = +humitureWatch.timeAdd(humitureWatch.UnixToDate(data.obj.humitureWatch[i].vTime, true));
                                            for (var n = 0; n < humitureWatch.GetDateDiff(humitureWatch.UnixToDate(data.obj.humitureWatch[i].vTime, true), humitureWatch.UnixToDate(data.obj.humitureWatch[i + 1].vTime, true), "minute") * chenageTimes - 1; n++) {
                                                date.push(humitureWatch.timeStamp2String(new Date(ctime += rtime)));
                                                tempOne.push(null);
                                                tempTwo.push(null);
                                                tempThree.push(null);
                                                tempFour.push(null);
                                                tempFive.push(null);
                                                humidityOne.push(null);//一号传感器湿度
                                                humidityTwo.push(null);//二号传感器湿度
                                                humidityThree.push(null);//三号传感器湿度
                                                humidityFour.push(null);//四号传感器湿度
                                            }
                                        }
                                    }
                                }
                            }
                            //humitureWatch.infoinputTab("/clbs/v/temperatureDetection/temperatureStatistics/list");
                        }
                    } else if (data.success==false){
                        layer.msg(data.msg,{move:false});
                        tempOne=[];
                        tempTwo=[];
                        tempThree=[];
                        tempFour=[];
                        tempFive=[];
                        humidityOne=[];
                        humidityTwo=[];
                        humidityThree=[];
                        humidityFour=[];
                    }
                    $("#graphShow").show();
                    $("#showClick").attr("class","fa fa-chevron-down");

                    var temps = [tempOne,tempTwo,tempThree,tempFour,tempFive];
                    for (var j = 0; j < temps.length; j++){
                        var temp = temps[j];
                        for (var i = 0; i < temp.length; i++){
                            if (temp[i] > tempMax){
                                tempMax = temp[i];
                            }
                            if (temp[i] < tempMin){
                                tempMin = temp[i];
                            }
                        }
                    }

                    humitureWatch.init();
                },
                error:function(jqXHR, textStatus, errorThrown){
                    layer.closeAll('loading');
                    if(textStatus=="timeout"){
                        layer.msg("加载超时，请重试");
                    }
                },

            });
        },
        //后一天
        upDay: function(){
            humitureWatch.startDay(1);
            if (vehicleId != "" && vehicleId != null) {
                var dateValue = new Date().getTime();
                var startTimeValue = new Date(startTime.split(" ")[0].replace(/-/g,"/")).getTime();
                if(startTimeValue <= dateValue){
                    humitureWatch.ajaxList(vehicleId, startTime, endTime,0);
                }else{
                    layer.msg("暂时没办法穿越，明天我再帮您看吧！");
                }
            } else {
                layer.msg("请先订阅监控对象", {move: false});
            }
        },
        // 查询
        query:function(){
        	var timeInterval = $('#timeInterval').val().split('--');
        	startTime = timeInterval[0];
        	endTime = timeInterval[1];
        	vehicleId = $("#vehicleId").val();
            // 获取车辆id 开始时间和结束时间发送请求
            if (vehicleId != null && vehicleId != "") {
                humitureWatch.ajaxList(vehicleId, startTime, endTime,0);
            } else {
                layer.msg("请先订阅监控对象", {move: false});
            }
        },
        // 今天
        todayClick: function () {
        	vehicleId = $("#vehicleId").val();
            humitureWatch.nowDay();
            // 获取车辆id 开始时间和结束时间发送请求
            if (vehicleId != null && vehicleId != "") {
                humitureWatch.ajaxList(vehicleId, startTime, endTime,0);
            } else {
                layer.msg("请先订阅监控对象", {move: false});
            }
        },
        // 前一天
        yesterdayClick: function () {
            humitureWatch.startDay(-1);
            if (vehicleId != "" && vehicleId != null) {
                humitureWatch.ajaxList(vehicleId, startTime, endTime,0);
            } else {
                layer.msg("请先订阅监控对象", {move: false});
            }
        },
        // 勾选数据
        //时间戳转换日期 
        UnixToDate: function(unixTime, isFull, timeZone) {
            if (typeof (timeZone) == 'number') {
                unixTime = parseInt(unixTime) + parseInt(timeZone) * 60 * 60;
            }
            var time = new Date(unixTime * 1000);
            var ymdhis = "";
            ymdhis += time.getFullYear() + "-";
            ymdhis += (time.getMonth() + 1) + "-";
            ymdhis += time.getDate();
            if (isFull === true) {
                ymdhis += " " + time.getHours() + ":";
                ymdhis += time.getMinutes() + ":";
                ymdhis += time.getSeconds();
            }
            return ymdhis;
        },
        showClick: function () {
            if ($(this).hasClass("fa-chevron-up")) {
                $(this).attr("class", "fa fa-chevron-down");
                $("#graphShow").show();
            } else {
                $(this).attr("class", "fa fa-chevron-up");
                $("#graphShow").hide('300');
            }
        },
        draggle: function () {
            $("#showClick").attr("class", "fa fa-chevron-down");
            $("#graphShow").show();
        },
        toHHMMSS: function(data){
            var totalSeconds=data*60*60;
            var  hour =  Math.floor(totalSeconds/60/60);
            var minute = Math.floor(totalSeconds/60%60);
            var second = Math.floor(totalSeconds%60);
            return hour+"小时"+minute+"分"+second+"秒"
        },
        removeClass: function(){
            var dataList = $(".dataTableShow");
            for(var i = 0; i < 3; i++){
                dataList.children("li").removeClass("active");
            }
        },
        allReportClick: function(){
            humitureWatch.removeClass();
            $(this).addClass("active");
            if(date.length!=0){
                humitureWatch.infoinputTab(url_oil,0);
            }
        },
        timeStamp2String: function(time){
            var time = time.toString();
            var startTimeIndex = time.replace("-","/").replace("-","/");
            var val = Date.parse(startTimeIndex);
            var datetime = new Date();
            datetime.setTime(val);
            var year = datetime.getFullYear();
            var month = datetime.getMonth() + 1 < 10 ? "0" + (datetime.getMonth() + 1) : datetime.getMonth() + 1;
            var date = datetime.getDate() < 10 ? "0" + datetime.getDate() : datetime.getDate();
            var hour = datetime.getHours() < 10 ? "0" + datetime.getHours() : datetime.getHours();
            var minute = datetime.getMinutes() < 10 ? "0" + datetime.getMinutes() : datetime.getMinutes();
            var second = datetime.getSeconds() < 10 ? "0" + datetime.getSeconds() : datetime.getSeconds();
            return year + "-" + month + "-" + date + " " + hour + ":" + minute + ":" + second;
        },
        timeAdd: function(time){
            var str = time.toString();
            str = str.replace(/-/g, "/");
            return new Date(str);
        },
        GetDateDiff: function(startTime, endTime, diffType){
            // 将xxxx-xx-xx的时间格式，转换为 xxxx/xx/xx的格式
            startTime = startTime.replace(/-/g, "/");
            endTime = endTime.replace(/-/g, "/");
            // 将计算间隔类性字符转换为小写
            diffType = diffType.toLowerCase();
            var sTime = new Date(startTime); // 开始时间
            var eTime = new Date(endTime); // 结束时间
            // 作为除数的数字
            var divNum = 1;
            switch (diffType) {
                case "second":
                    divNum = 1000;
                    break;
                case "minute":
                    divNum = 1000 * 60;
                    break;
                case "hour":
                    divNum = 1000 * 3600;
                    break;
                case "day":
                    divNum = 1000 * 3600 * 24;
                    break;
                default:
                    break;
            }
            return parseFloat((eTime.getTime() - sTime.getTime()) / parseInt(divNum)); //
        },
        //过滤数组空值
        filterTheNull: function(value){
            for (var i = 0; i < value.length; i++) {
                if(value[i]!=0){
                    if (value[i] == null || value[i] == "" || typeof(value[i]) == "undefined") {
                        value.splice(i, 1);
                        i = i - 1;
                    }
                }
            }
            return value
        },
        nullValueJudge:function(arr){//将数组中为null的数据替换为-
            if(arr != null && arr.length != 0)
            {
                for(var i=0;i<arr.length-1;i++){
                    if(i==2){
                        var time=arr[i];
                        var times=humitureWatch.timeStamp2String(humitureWatch.UnixToDate(time,true));
                        arr[i]=times;
                    }
                    if(arr[i]==null){
                        arr[i]="-";
                    }
                }
            }
        },
    }
    $(function () {
        humitureWatch.nowDay();
        $('#timeInterval').dateRangePicker({
            dateLimit: 31,
        });
//        humitureWatch.ajaxList("ca00dba8-baed-475b-83d1-e43f56e8435w", "2018-01-25 00:00:00", "2018-01-25 23:59:59",1);
        humitureWatch.init();
        $("#todayClick").bind("click", humitureWatch.todayClick);
        $("#left-arrow").bind("click", humitureWatch.yesterdayClick);
        $("#right-arrow").bind("click", humitureWatch.upDay);
        $("#dayquery").bind("click", humitureWatch.query);
        $("#stretch").unbind();

    });
})($, window);