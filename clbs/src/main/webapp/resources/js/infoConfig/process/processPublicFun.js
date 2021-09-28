// 公共方法

define(function() {
  var publicFun = {
    //将null转成空字符串
    converterNullToBlank: function (nullValue) {
      if (nullValue == null || nullValue == undefined || nullValue == "null" || nullValue == "")
        return "";
      else
        return nullValue;
    },
    //新增详情切换公用方法
    switchBtn: function (inputID, tableID, charID, detailID) {
      var value = $("#" + inputID).val();
      //取得当前下拉框里的所有值
      var tableValue = $("#" + tableID).children("table").children("tbody").children("tr").children("td");
      $("#" + detailID).hide();
      $("#" + charID).show();
    },
    //IE9 -- setTimeout(监听input内容变化)
    switchBtnIE9: function (inputID, tableID, charID, detailID) {
      var sameFlag = false;
      var interval = setInterval(function () {
        if (intervalFlag == false) {
          clearInterval(interval);
        };
        var value = $("#" + inputID).val();
        $("#" + tableID).children("table").children("tbody").children("tr").children("td").each(function () {
          if ($(this).text() == value) {
            sameFlag = true;
          }
        });
        if (sameFlag) {
          $("#" + charID).hide();
          $("#" + detailID).show();
          sameFlag = false;
        } else {
          $("#" + detailID).hide();
          $("#" + charID).show();
        };
      }, 300);
    },
    //燃料类型
    getFuelTypeCallback: function (data) {
      for (var i = 0; i < data.obj.FuelTypeList.length; i++) {
        if ("柴油" == data.obj.FuelTypeList[i].fuelCategory) {
          $("#dieselOil").append("<option value=" + data.obj.FuelTypeList[i].id + ">" + data.obj.FuelTypeList[i].fuelType + "</option>");
        } else if ("汽油" == data.obj.FuelTypeList[i].fuelCategory) {
          $("#gasoline").append("<option value=" + data.obj.FuelTypeList[i].id + ">" + data.obj.FuelTypeList[i].fuelType + "</option>");
        } else if ("天然气" == data.obj.FuelTypeList[i].fuelCategory) {
          $("#naturalGas").append("<option value=" + data.obj.FuelTypeList[i].id + ">" + data.obj.FuelTypeList[i].fuelType + "</option>");
        }
      }
    },
  };
  return {
    publicFun: publicFun,
  }
})

