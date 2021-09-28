(function ($, window) {
  var startTime;
  var endTime;
  var date = [];//图形表时间
  var dataSets = [];//table数据
  var nullData = [];
  var mileage = [];//里程
  var speed = [];//速度
  var humidityOne = [];//一号湿度传感器湿度
  var humidityTwo = [];//二号湿度传感器湿度
  var humidityThree = [];//三号湿度传感器湿度
  var humidityFour = [];//四号湿度传感器湿度
  var myChart;
  var option;
  var humidityList = [];
  var dataHumidity = [];

  var addressMsg = [];

  humidityStatis = {
    init: function () {
      myChart = echarts.init(document.getElementById('sjcontainer'));
      option = {
        tooltip: {
          trigger: 'axis',
          textStyle: {
            fontSize: 20
          },
          formatter: function (a) {
            var unit = ['Km', 'Km/h', '%'];
            var relVal = "";
            relVal = a[0].name;
            if (a[0].data == null) {
              relVal = "无相关数据";
            } else {
              for (var i = 0; i < a.length; i++) {
                if (a[i].seriesName == "里程") {
                  if (a[i].data === "" || a[i].data == null) {
                    relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：- km";
                  } else {
                    relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + a[i].data + "km";
                  }
                } else if (a[i].seriesName == "速度") {
                  if (a[i].data === "" || a[i].data == null) {
                    relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：- km/h";
                  } else {
                    relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + a[i].data + "km/h";
                  }
                } else if (a[i].seriesName == "湿度传感器湿度1") {
                  if (a[i].data == null) {
                    relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：- %";
                  } else {
                    relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + a[i].data + "%";
                  }
                } else if (a[i].seriesName == "湿度传感器湿度2") {
                  if (a[i].data == null) {
                    relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：- %";
                  } else {
                    relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + a[i].data + "%";
                  }
                } else if (a[i].seriesName == "湿度传感器湿度3") {
                  if (a[i].data == null) {
                    relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：- %";
                  } else {
                    relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + a[i].data + "%";
                  }
                } else if (a[i].seriesName == "湿度传感器湿度4") {
                  if (a[i].data == null) {
                    relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：- %";
                  } else {
                    relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[i].color + "'></span>" + a[i].seriesName + "：" + a[i].data + "%";
                  }
                }
              }
            }
            return relVal;
          }
        },
        legend: {
          data: [{name: '里程'},
            {name: '速度'},
            {name: '湿度传感器湿度1'},
            {name: '湿度传感器湿度2'},
            {name: '湿度传感器湿度3'},
            {name: '湿度传感器湿度4'}],
          left: 'auto',
        },
        toolbox: {
          show: false
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: date
        },
        yAxis: [
          {
            type: 'value',
            name: '速度(km/h)',
            scale: true,
            min: 0,
            max: 240,
            position: 'right',
            axisLabel: {
              formatter: '{value}'
            },
            splitLine: {
              show: false
            }
          },
          {
            type: 'value',
            name: '湿度(%)',
            scale: true,
            position: 'right',
            offset: 60,
            min: 0,
            max: 100,
            axisLabel: {
              formatter: '{value}'
            },
            splitLine: {
              show: false
            }
          },
          {
            type: 'value',
            name: '里程(km)',
            position: 'left',
            precision: 1,
            scale: true,
            axisLabel: {
              formatter: '{value}'
            },
            splitLine: {
              show: false
            }
          }
        ],
        dataZoom: [{
          type: 'inside'

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
        series: [
          {
            name: '里程',
            yAxisIndex: 2,
            type: 'line',
            smooth: true,
            symbol: 'none',
            sampling: 'average',
            itemStyle: {
              normal: {
                color: 'rgb(109, 207, 246)'
              }
            },
            data: mileage
          },
          {
            name: '速度',
            yAxisIndex: 0,
            type: 'line',
            smooth: true,
            symbol: 'none',
            sampling: 'average',
            itemStyle: {
              normal: {
                color: 'rgb(145, 218, 0)'
              }
            },
            data: speed
          },
          {
            name: '湿度传感器湿度1',
            yAxisIndex: 1,
            type: 'line',
            smooth: true,
            symbol: 'none',
            symbolSize: 15,
            showSymbol: false,
            sampling: 'average',
            itemStyle: {
              normal: {
                color: '#f4b5bd'
              }
            },
            data: humidityOne
          },
          {
            name: '湿度传感器湿度2',
            yAxisIndex: 1,
            type: 'line',
            smooth: true,
            symbol: 'none',
            symbolSize: 15,
            showSymbol: false,
            sampling: 'average',
            itemStyle: {
              normal: {
                color: '#e47c8c'
              }
            },
            data: humidityTwo
          }, {
            name: '湿度传感器湿度3',
            yAxisIndex: 1,
            type: 'line',
            smooth: true,
            symbol: 'none',
            symbolSize: 15,
            showSymbol: false,
            sampling: 'average',
            itemStyle: {
              normal: {
                color: '#e35052'
              }
            },
            data: humidityThree
          },
          {
            name: '湿度传感器湿度4',
            yAxisIndex: 1,
            type: 'line',
            smooth: true,
            symbol: 'none',
            symbolSize: 15,
            showSymbol: false,
            sampling: 'average',
            itemStyle: {
              normal: {
                color: '#f33168'
              }
            },
            data: humidityFour
          }
        ]
      };
      myChart.setOption(option);
      myChart.on('click', function (params) {
      });
      window.onresize = myChart.resize;
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
        tMonth = humidityStatis.doHandleMonth(tMonth + 1);
        tDate = humidityStatis.doHandleMonth(tDate);
        var num = -(day + 1);
        startTime = tYear + "-" + tMonth + "-" + tDate + " " + "00:00:00";
        var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * parseInt(num);
        today.setTime(end_milliseconds); // 注意，这行是关键代码
        var endYear = today.getFullYear();
        var endMonth = today.getMonth();
        var endDate = today.getDate();
        endMonth = humidityStatis.doHandleMonth(endMonth + 1);
        endDate = humidityStatis.doHandleMonth(endDate);
        endTime = endYear + "-" + endMonth + "-" + endDate + " " + "23:59:59";
      } else {
        var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
        var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
        var dateList = new Date();
        dateList.setTime(vtoday_milliseconds);
        var vYear = dateList.getFullYear();
        var vMonth = dateList.getMonth();
        var vDate = dateList.getDate();
        vMonth = humidityStatis.doHandleMonth(vMonth + 1);
        vDate = humidityStatis.doHandleMonth(vDate);
        startTime = vYear + "-" + vMonth + "-" + vDate + " "
            + "00:00:00";
        if (day == 1) {
          endTime = vYear + "-" + vMonth + "-" + vDate + " "
              + "23:59:59";
        } else {
          var endNum = -1;
          var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
          var dateEnd = new Date();
          dateEnd.setTime(vendtoday_milliseconds);
          var vendYear = dateEnd.getFullYear();
          var vendMonth = dateEnd.getMonth();
          var vendDate = dateEnd.getDate();
          vendMonth = humidityStatis.doHandleMonth(vendMonth + 1);
          vendDate = humidityStatis.doHandleMonth(vendDate);
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
    ajaxList: function (band, startTime, endTime) {
      humidityStatis.removeClass();
      $("#allReport").addClass("active");
      var brandName = $("input[name='charSelect']").val();
      if (brandName.length > 8) {
        $("#carName").attr("title", brandName).tooltip('fixTitle');
        brandName = brandName.substring(0, 7) + '...';
      } else {
        $('#carName').removeAttr('data-original-title');
      }
      $("#carName").text(brandName);
      $(".toopTip-btn-left,.toopTip-btn-right").css("display", "inline-block");
      mileage = [];
      speed = [];
      date = [];
      humidityOne = [];
      humidityTwo = [];
      humidityThree = [];
      humidityFour = [];
      humidityList = [];
      dataHumidity = [];
      $.ajax({
        type: "POST",
        url: "/clbs/v/humidity/statistical/humidityStatisics",
        data: {"startTime": startTime, "endTime": endTime, "band": band},
        dataType: "json",
        async: true,
        timeout: 30000, //超时时间设置，单位毫秒
        beforeSend: function () {
          //异步请求时spinner出现
          layer.load(2);
        },
        success: function (data) {
          layer.closeAll('loading');
          $('#timeInterval').val(startTime + '--' + endTime);
          if (data.success == true) {
            var responseData = JSON.parse(ungzip(data.obj.humidityStatisicsList));
            data.obj.humidityStatisicsList = responseData;
            if (data.obj.humidityStatisicsList != null && data.obj.humidityStatisicsList.length != 0) {
              dataHumidity = data.obj.humidityStatisicsList;//后台返回的数据
              var rtime = 0;
              var chenageTimes = 0;
              var miles;
              var speeds;
              for (var i = 0, len = data.obj.humidityStatisicsList.length - 1; i <= len; i++) {
                if (!(Number(data.obj.humidityStatisicsList[i].speed == 0 && Number(data.obj.humidityStatisicsList[i].vTime) == 0))) {
                  date.push(humidityStatis.timeStamp2String(humidityStatis.UnixToDate(data.obj.humidityStatisicsList[i].vTime, true)));
                  miles = data.obj.humidityStatisicsList[i].gpsMile;
                  speeds = data.obj.humidityStatisicsList[i].speed;
                  console.log(miles,isNaN(miles));
                  if (miles == undefined || miles == null || miles == "null") {
                    miles = "";
                  }
                  if (speeds == null || speeds == "null") {
                    speeds = "";
                  }
                  mileage.push(miles);
                  speed.push(speeds);
                  humidityOne.push((data.obj.humidityStatisicsList[i].wetnessValueOne));//一号传感器湿度
                  humidityTwo.push((data.obj.humidityStatisicsList[i].wetnessValueTwo));//二号传感器湿度
                  humidityThree.push((data.obj.humidityStatisicsList[i].wetnessValueThree));//三号传感器湿度
                  humidityFour.push((data.obj.humidityStatisicsList[i].wetnessValueFour));//四号传感器湿度
                }
                if (i != data.obj.humidityStatisicsList.length - 1) {
                  if (humidityStatis.GetDateDiff(humidityStatis.UnixToDate(data.obj.humidityStatisicsList[i].vTime, true), humidityStatis.UnixToDate(data.obj.humidityStatisicsList[i + 1].vTime, true), "second") <= 300
                      && humidityStatis.GetDateDiff(humidityStatis.UnixToDate(data.obj.humidityStatisicsList[i].vTime, true), humidityStatis.UnixToDate(data.obj.humidityStatisicsList[i + 1].vTime, true), "second") >= 5) {
                    changeTime = humidityStatis.GetDateDiff(humidityStatis.UnixToDate(data.obj.humidityStatisicsList[i].vTime, true), humidityStatis.UnixToDate(data.obj.humidityStatisicsList[i + 1].vTime, true), "second");
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
                  if (humidityStatis.GetDateDiff(humidityStatis.UnixToDate(data.obj.humidityStatisicsList[i].vTime, true), humidityStatis.UnixToDate(data.obj.humidityStatisicsList[i + 1].vTime, true), "second") > 300) {
                    if (rtime == 0) {
                      var ctime = +humidityStatis.timeAdd(humidityStatis.UnixToDate(data.obj.humidityStatisicsList[i].vTime, true));
                      for (var n = 0; n < humidityStatis.GetDateDiff(humidityStatis.UnixToDate(data.obj.humidityStatisicsList[i].vTime, true), humidityStatis.UnixToDate(data.obj.humidityStatisicsList[i + 1].vTime, true), "minute") * 2; n++) {
                        date.push(humidityStatis.timeStamp2String(new Date(ctime += 30000)));
                        mileage.push(null);
                        speed.push(null);
                        humidityOne.push(null);
                        humidityTwo.push(null);
                        humidityThree.push(null);
                        humidityFour.push(null);
                      }
                    } else {
                      var ctime = +humidityStatis.timeAdd(humidityStatis.UnixToDate(data.obj.humidityStatisicsList[i].vTime, true));
                      for (var n = 0; n < humidityStatis.GetDateDiff(humidityStatis.UnixToDate(data.obj.humidityStatisicsList[i].vTime, true), humidityStatis.UnixToDate(data.obj.humidityStatisicsList[i + 1].vTime, true), "minute") * chenageTimes - 1; n++) {
                        date.push(humidityStatis.timeStamp2String(new Date(ctime += rtime)));
                        mileage.push(null);
                        speed.push(null);
                        humidityOne.push(null);
                        humidityTwo.push(null);
                        humidityThree.push(null);
                        humidityFour.push(null);
                      }
                    }
                  }
                }
              }
              //humidityStatis.infoinputTab("/clbs/v/humidity/statistical/list");//把数据组装到table
            }
            console.log(mileage);
          } else if (data.success == false) {
            layer.msg(data.msg, {move: false});//错误信息
            mileage = [];
            speed = [];
            dataSets = [];
            date = [];
            humidityOne = [];//一号湿度传感器湿度
            humidityTwo = [];//二号湿度传感器湿度
            humidityThree = [];//三号湿度传感器湿度
            humidityFour = [];//四号湿度传感器湿度
          }
          $("#graphShow").show();
          $("#showClick").attr("class", "fa fa-chevron-down");
          humidityStatis.init();
          humidityStatis.infoinputTab("/clbs/v/humidity/statistical/list", 0);
        },
        error: function (jqXHR, textStatus, errorThrown) {
          layer.closeAll('loading');
          if (textStatus == "timeout") {
            layer.msg("加载超时，请重试");
          }
        },
      });
    },
    //上一天
    upDay: function () {
      humidityStatis.startDay(1);
      var charNum = $("#charSelect").attr("data-id");
      var groupValue = $("#groupSelect").val();
      if (charNum != "" && groupValue != "") {
        var dateValue = new Date().getTime();
        var startTimeValue = new Date(startTime.split(" ")[0].replace(/-/g, "/")).getTime();
        if (startTimeValue <= dateValue) {
          humidityStatis.ajaxList(charNum, startTime, endTime);
          humidityStatis.validates()
        } else {
          layer.msg("暂时没办法穿越，明天我再帮您看吧！");
        }
      } else {
        layer.msg("请选择监控对象！", {move: false});
      }
    },
    validates: function () {
      return $("#humidList").validate({
        rules: {
          startTime: {
            required: true
          },
          endTime: {
            required: true,
            compareDate: "#timeInterval",
          },
          groupSelect: {
            zTreeChecked: "treeDemo"
          },
          charSelect: {
            required: true
          }
        },
        messages: {
          startTime: {
            required: "请选择开始日期！",
          },
          endTime: {
            required: "请选择结束时间！",
            compareDate: endtimeComStarttime,
          },
          groupSelect: {
            zTreeChecked: vehicleSelectBrand,
          },
          charSelect: {
            required: vehicleSelectBrand,
          }
        }
      }).form();
    },
    // 今天
    todayClick: function () {
      humidityStatis.nowDay();
      var charNum = $("#charSelect").attr("data-id");
      var groupValue = $("#groupSelect").val();
      if (!humidityStatis.validates()) {
        return;
      }
      if (charNum == '') {
        $("#charSelect-error").html('请至少选择一个监控对象').show();
        return;
      } else {
        $("#charSelect-error").hide();
      }
      humidityStatis.ajaxList(charNum, startTime, endTime);
    },
    // 前一天
    yesterdayClick: function () {
      humidityStatis.startDay(-1);
      var startValue = $("#startTime")
      var charNum = $("#charSelect").attr("data-id");
      var groupValue = $("#groupSelect").val();
      if (!humidityStatis.validates()) {
        return;
      }
      if (charNum == '') {
        $("#charSelect-error").html('请至少选择一个监控对象').show();
        return;
      } else {
        $("#charSelect-error").hide();
      }
      humidityStatis.ajaxList(charNum, startTime, endTime);
    },
    // 近三天
    nearlyThreeDays: function () {
      humidityStatis.startDay(-3);
      var charNum = $("#charSelect").attr("data-id");
      var groupValue = $("#groupSelect").val();
      if (!humidityStatis.validates()) {
        return;
      }
      if (charNum == '') {
        $("#charSelect-error").html('请至少选择一个监控对象').show();
        return;
      } else {
        $("#charSelect-error").hide();
      }
      humidityStatis.ajaxList(charNum, startTime, endTime);
    },
    // 近七天
    nearlySevenDays: function () {
      humidityStatis.startDay(-7);
      var charNum = $("#charSelect").attr("data-id");
      var groupValue = $("#groupSelect").val();
      if (!humidityStatis.validates()) {
        return;
      }
      if (charNum == '') {
        $("#charSelect-error").html('请至少选择一个监控对象').show();
        return;
      } else {
        $("#charSelect-error").hide();
      }
      humidityStatis.ajaxList(charNum, startTime, endTime);
    },
    // 查询
    inquireClick: function () {
      var groupValue = $("#groupSelect").val();
      var timeInterval = $('#timeInterval').val().split('--');
      startTime = timeInterval[0];
      endTime = timeInterval[1];
      var charNum = $("#charSelect").attr("data-id");
      if (!humidityStatis.validates()) {
        return;
      }
      if (charNum == '') {
        $("#charSelect-error").html('请至少选择一个监控对象').show();
        return;
      } else {
        $("#charSelect-error").hide();
      }
      humidityStatis.ajaxList(charNum, startTime, endTime);
    },
    // 勾选数据
    //时间戳转换日期 
    UnixToDate: function (unixTime, isFull, timeZone) {
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

    //创建表格
    infoinputTab: function (url, state) {
      var columnDefs = [{
        //第一列，用来显示序号
        "searchable": false,
        "orderable": false,
        "targets": 0
      }];
      var columns = [{
        //第一列，用来显示序号
        "data": null,
        "class": "text-center"
      }, {
        "data": "monitorName",
        "class": "text-center"
      }, {
        "data": "vTime",
        "class": "text-center",
        render: function (data, type, row, meta) {
          return humidityStatis.timeStamp2String(humidityStatis.UnixToDate(data, true));
        }
      }, {
        "data": "gpsMile",
        "class": "text-center",
        render: function (data, type, row, meta) {
          if (data == 'null' || data == null) {
            return '-';
          } else {
            return data;
          }
        }
      }, {
        "data": "speed",
        "class": "text-center",
        render: function (data, type, row, meta) {
          if (data == 'null' || data == null) {
            return '-';
          } else {
            return data;
          }
        }
      }, {
        "data": "wetnessValueOne",
        "class": "text-center",
        render: function (data, type, row, meta) {
          if (state == 0) {
            if (data == "" || data == null || data == undefined) {
              return "-";
            } else {
              return data;
            }
          } else if (state == 1) {
            if (row.wetnessHighLowOne == 1) {
              return data;
            } else {
              return "-";
            }
          } else if (state == 2) {
            if (row.wetnessHighLowOne == 2) {
              return data;
            } else {
              return "-";
            }
          }
        }
      }, {
        "data": "wetnessValueTwo",
        "class": "text-center",
        render: function (data, type, row, meta) {
          if (state == 0) {
            if (data == "" || data == null || data == undefined) {
              return "-";
            } else {
              return data;
            }
          } else if (state == 1) {
            if (row.wetnessHighLowTwo == 1) {
              return data;
            } else {
              return "-";
            }
          } else if (state == 2) {
            if (row.wetnessHighLowTwo == 2) {
              return data;
            } else {
              return "-";
            }
          }
        }
      }, {
        "data": "wetnessValueThree",
        "class": "text-center",
        render: function (data, type, row, meta) {
          if (state == 0) {
            if (data == "" || data == null || data == undefined) {
              return "-";
            } else {
              return data;
            }
          } else if (state == 1) {
            if (row.wetnessHighLowThree == 1) {
              return data;
            } else {
              return "-";
            }
          } else if (state == 2) {
            if (row.wetnessHighLowThree == 2) {
              return data;
            } else {
              return "-";
            }
          }
        }
      }, {
        "data": "wetnessValueFour",
        "class": "text-center",
        render: function (data, type, row, meta) {
          if (state == 0) {
            if (data == "" || data == null || data == undefined) {
              return "-";
            } else {
              return data;
            }
          } else if (state == 1) {
            if (row.wetnessHighLowFour == 1) {
              return data;
            } else {
              return "-";
            }
          } else if (state == 2) {
            if (row.wetnessHighLowFour == 2) {
              return data;
            } else {
              return "-";
            }
          }
        }
      }, {
        "data": null,
        "class": "text-center",
        render: function (data, type, row, meta) {
          //return data.longtitude+","+data.latitude
          return "加载中...";
        }
      }];
      var ajaxDataParamFun = function (d) {
        d.band = $("#charSelect").attr("data-id"); //模糊查询
        var timeInterval = $('#timeInterval').val().split('--');
        startTime = timeInterval[0];
        endTime = timeInterval[1];
        d.startTime = startTime;
        d.endTime = endTime;
      };
      //表格setting
      var setting = {
        listUrl: url,
        columnDefs: columnDefs, //表格列定义
        columns: columns, //表格列
        dataTableDiv: 'dataTable', //表格
        ajaxDataParamFun: ajaxDataParamFun, //ajax参数
        pageable: true, //是否分页
        showIndexColumn: true, //是否显示第一列的索引列
        enabledChange: true,
        getAddress: true,//是否逆地理编码
        address_index: 10
      };
      //创建表格
      myTable = new TG_Tabel.createNew(setting);
      myTable.init();
    },
    toHHMMSS: function (data) {
      var totalSeconds = data * 60 * 60;
      var hour = Math.floor(totalSeconds / 60 / 60);
      var minute = Math.floor(totalSeconds / 60 % 60);
      var second = Math.floor(totalSeconds % 60);
      return hour + "小时" + minute + "分钟" + second + "秒"
    },
    removeClass: function () {
      var dataList = $(".dataTableShow");
      for (var i = 0; i < 3; i++) {
        dataList.children("li").removeClass("active");
      }
    },
    allReportClick: function () {
      humidityStatis.removeClass();
      $(this).addClass("active");
      if (date.length != 0) {
        humidityStatis.infoinputTab("/clbs/v/humidity/statistical/list", 0);
      }
      delete  option.series[2].markPoint;
      delete  option.series[3].markPoint;
      delete  option.series[4].markPoint;
      delete  option.series[5].markPoint;
      myChart.setOption(option);
    },
    timeStamp2String: function (time) {
      var time = time.toString();
      var startTimeIndex = time.replace("-", "/").replace("-", "/");
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
    timeAdd: function (time) {
      var str = time.toString();
      str = str.replace(/-/g, "/");
      return new Date(str);
    },
    GetDateDiff: function (startTime, endTime, diffType) {
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
    filterTheNull: function (value) {
      for (var i = 0; i < value.length; i++) {
        if (value[i] != 0) {
          if (value[i] == null || value[i] == "" || typeof(value[i]) == "undefined") {
            value.splice(i, 1);
            i = i - 1;
          }
        }
      }
      return value
    },
    /*validates: function(){
        return $("#humidList").validate({
            rules: {
                groupId: {
                    required: true
                },
                endTime: {
                    required: true,
                    compareDate: "#timeInterval",
                    compareDateDiff: "#timeInterval"
                },
                startTime: {
                    required: true
                },
                charSelect:{
                    required: true
                }
            },
            messages: {
                groupId: {
                    required: "不能为空"
                },
                endTime: {
                    required: "不能为空",
                    compareDate: "结束日期必须大于开始日期!",
                    compareDateDiff: "查询的日期必须小于一周"
                },
                startTime: {
                    required: "不能为空",
                },
                charSelect:{
                    required: "不能为空"
                }
            }
        }).form();
    },*/
    nullValueJudge: function (arr) {//将数组中为null的数据替换为-
      if (arr != null && arr.length != 0) {
        for (var i = 0; i < arr.length; i++) {
          if (arr[i] == null) {
            arr[i] = "-";
          }
          if (i == 2) {
            var time = arr[i];
            var times = humidityStatis.timeStamp2String(humidityStatis.UnixToDate(time, true));
            arr[i] = times;
          }
        }
      }
    },
    highthumidityinfo: function () {
      $("#highhumidityReport").addClass("active").siblings().removeClass("active");
      url = "/clbs/v/humidity/statistical/highList";
      //alert("高湿度数据显示");
      humidityStatis.infoinputTab(url, 1);
      delete  option.series[2].markPoint;
      delete  option.series[3].markPoint;
      delete  option.series[4].markPoint;
      delete  option.series[5].markPoint;
      myChart.setOption(option);
      //点击显示标注
      $('#dataTable tbody').on('click', 'tr', function () {
        $(this).siblings(".odd").find("td").css('background-color', "#f9f9f9");
        $(this).siblings(".even").find("td").css('background-color', "#fff");
        var backgroundcolor = $(this).find("td").css('background-color');
        if (backgroundcolor == "rgb(220, 245, 255)") {
          $("#dataTable tbody").find(".odd").find("td").css('background-color', "#f9f9f9");
          $("#dataTable tbody").find(".even").find("td").css('background-color', "#fff");
        } else {
          $(this).find("td").css('background-color', "#DCF5FF");
          var datainfo = $(this).find("td").eq("2").text();
          var humiditysensor1 = $(this).find("td").eq("5").text();
          var humiditysensor2 = $(this).find("td").eq("6").text();
          var humiditysensor3 = $(this).find("td").eq("7").text();
          var humiditysensor4 = $(this).find("td").eq("8").text();
          option.series[2].markPoint = {
            symbolSize: [60, 63],
            silent: true,
            data: [
              {
                name: '', value: humiditysensor1, xAxis: datainfo, yAxis: humiditysensor1, label: {
                  normal: {
                    show: true,
                  }
                },
                itemStyle: {
                  normal: {
                    color: '#f4b5bd',
                  },
                },
              },]
          }
          option.series[3].markPoint = {
            symbolSize: [60, 63],
            silent: true,
            data: [
              {
                name: '', value: humiditysensor2, xAxis: datainfo, yAxis: humiditysensor2, label: {
                  normal: {
                    show: true,
                  }
                },
                itemStyle: {
                  normal: {
                    color: '#e47c8c',
                  },
                },
              },]
          }
          option.series[4].markPoint = {
            symbolSize: [60, 63],
            silent: true,
            data: [
              {
                name: '', value: humiditysensor3, xAxis: datainfo, yAxis: humiditysensor3, label: {
                  normal: {
                    show: true,
                  }
                },
                itemStyle: {
                  normal: {
                    color: '#e35052',
                  },
                },
              },]
          }
          option.series[5].markPoint = {
            symbolSize: [60, 63],
            silent: true,
            data: [
              {
                name: '', value: humiditysensor4, xAxis: datainfo, yAxis: humiditysensor4, label: {
                  normal: {
                    show: true,
                  }
                },
                itemStyle: {
                  normal: {
                    color: '#f33168',
                  },
                },
              },]

          };
        }
        myChart.setOption(option);
      });
    },
    lowhumidityinfo: function () {
      $("#lowhumidityReport").addClass("active").siblings().removeClass("active");
      url = "/clbs/v/humidity/statistical/lowList";
      //alert("低湿度数据显示");
      humidityStatis.infoinputTab(url, 2);
      delete  option.series[2].markPoint;
      delete  option.series[3].markPoint;
      delete  option.series[4].markPoint;
      delete  option.series[5].markPoint;
      myChart.setOption(option);
      //点击显示标注
      $('#dataTable tbody').on('click', 'tr', function () {
        $(this).siblings(".odd").find("td").css('background-color', "#f9f9f9");
        $(this).siblings(".even").find("td").css('background-color', "#fff");
        var backgroundcolor = $(this).find("td").css('background-color');
        if (backgroundcolor == "rgb(220, 245, 255)") {
          $("#dataTable tbody").find(".odd").find("td").css('background-color', "#f9f9f9");
          $("#dataTable tbody").find(".even").find("td").css('background-color', "#fff");
        } else {
          $(this).find("td").css('background-color', "#DCF5FF");
          var datainfo = $(this).find("td").eq("2").text();
          var humiditysensor1 = $(this).find("td").eq("5").text();
          var humiditysensor2 = $(this).find("td").eq("6").text();
          var humiditysensor3 = $(this).find("td").eq("7").text();
          var humiditysensor4 = $(this).find("td").eq("8").text();
          option.series[2].markPoint = {
            symbolSize: [60, 63],
            silent: true,
            data: [
              {
                name: '', value: humiditysensor1, xAxis: datainfo, yAxis: humiditysensor1, label: {
                  normal: {
                    show: true,
                  }
                },
                itemStyle: {
                  normal: {
                    color: '#f4b5bd',
                  },
                },
              },]
          }
          option.series[3].markPoint = {
            symbolSize: [60, 63],
            silent: true,
            data: [
              {
                name: '', value: humiditysensor2, xAxis: datainfo, yAxis: humiditysensor2, label: {
                  normal: {
                    show: true,
                  }
                },
                itemStyle: {
                  normal: {
                    color: '#e47c8c',
                  },
                },
              },]
          }
          option.series[4].markPoint = {
            symbolSize: [60, 63],
            silent: true,
            data: [
              {
                name: '', value: humiditysensor3, xAxis: datainfo, yAxis: humiditysensor3, label: {
                  normal: {
                    show: true,
                  }
                },
                itemStyle: {
                  normal: {
                    color: '#e35052',
                  },
                },
              },]
          }
          option.series[5].markPoint = {
            symbolSize: [60, 63],
            silent: true,
            data: [
              {
                name: '', value: humiditysensor4, xAxis: datainfo, yAxis: humiditysensor4, label: {
                  normal: {
                    show: true,
                  }
                },
                itemStyle: {
                  normal: {
                    color: '#f33168',
                  },
                },
              },]

          };
        }
        myChart.setOption(option);
      });
    }
    // /**              
    //   * 时间戳转换日期              
    //   * @param <int> unixTime    待时间戳(秒)              
    //   * @param <bool> isFull    返回完整时间(Y-m-d 或者 Y-m-d H:i:s)              
    //   * @param <int>  timeZone   时区              
    //   */
    // callbackUnixToDate: function(unixTime, isFull, timeZone) {
    //     if(unixTime==0 || unixTime==null){
    //         return "";
    //     }
    //     if (typeof (timeZone) == 'number') {
    //         unixTime = parseInt(unixTime) + parseInt(timeZone) * 60 * 60;
    //     }
    //     var time = new Date(unixTime * 1000);
    //     var ymdhis = "";
    //     ymdhis += time.getFullYear() + "-";
    //     ymdhis += (time.getMonth() + 1)<10? ("0"+(time.getMonth() + 1)+ "-"):((time.getMonth() + 1)+ "-");
    //     ymdhis += time.getDate()<10?("0"+time.getDate()):(time.getDate());;
    //     if (isFull === true) {
    //         ymdhis += " " + (time.getHours()<10?("0"+time.getHours()):time.getHours()) + ":";
    //         ymdhis += (time.getMinutes()<10?("0"+time.getMinutes()):time.getMinutes()) + ":";
    //         ymdhis += (time.getSeconds()<10?("0"+time.getSeconds()):time.getSeconds());
    //     }
    //     return ymdhis;
    // },
  }
  $(function () {
    $("#toggle-left").bind("click", function () {
      setTimeout(function () {
        humidityStatis.init();
      }, 500)
    });
    Array.prototype.isHas = function (a) {
      if (this.length === 0) {
        return false
      }
      ;
      for (var i = 0; i < this.length; i++) {
        if (this[i].seriesName === a) {
          return true
        }
      }
    };
    humidityStatis.nowDay();
    $('#timeInterval').dateRangePicker({
      dateLimit: 7
    });
    $("#todayClick").bind("click", humidityStatis.todayClick);
    $("#yesterdayClick,#right-arrow").bind("click", humidityStatis.yesterdayClick);
    $("#nearlyThreeDays").bind("click", humidityStatis.nearlyThreeDays);
    $("#nearlySevenDays").bind("click", humidityStatis.nearlySevenDays);
    $("#inquireClick").bind("click", humidityStatis.inquireClick);
    $("#showClick").bind("click", humidityStatis.showClick);
    $("#left-arrow").bind("click", humidityStatis.upDay);
    $("#allReport").bind("click", humidityStatis.inquireClick);
    $("#highhumidityReport").bind("click", humidityStatis.highthumidityinfo);
    $("#lowhumidityReport").bind("click", humidityStatis.lowhumidityinfo);
    $("#stretch").unbind("click");
  });
})($, window);