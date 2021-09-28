(function (window, $) {
  var timeOutId;
  var onLineStatus = false; // 标识车辆是否在线
  setCalibrationObj = {
    init: function () {
      $("#dataList").on('blur', '.adValue',setCalibrationObj.editAd);
      $("#dataList").on('blur', '.adActualValue', setCalibrationObj.editActual);
      $("#getADVal").bind("click", setCalibrationObj.getADVal);
      $("#addadActualVal").bind("click", setCalibrationObj.addadActualVal);
      $("#check").bind("click", setCalibrationObj.checked);
      $("#del").bind("click", setCalibrationObj.del);
      $("#adActualVal").focus(setCalibrationObj.calculationLoad); //实际载重获焦
      $(".calibrationDetails tbody tr").css("background-color", "#f9f9f9");
      $(".calibrationDetails tbody tr td input").mouseover(function () {
        $(".calibrationDetails").attr("class", "table table-striped table-bordered");
        $(this).css({
          "border": "1px solid #e0e0e0",
          "border-radius": "3px",
          "background": "#fff",
          "-webkit-box-shadow": "inset 0 1px 2px rgba(0,0,0,.1)",
          "-moz-box-shadow": "inset 0 1px 2px rgba(0,0,0,.1)",
          "-box-shadow": "inset 0 1px 2px rgba(0,0,0,.1)"
        });
      });
      $(".calibrationDetails tbody tr td input").mouseout(function () {
        $(this).css({
          "border": "1px solid #f9f9f9",
          "background": "#f9f9f9",
          "-webkit-box-shadow": "none",
          "-moz-box-shadow": "none",
          "-box-shadow": "none"
        });
      });

      if ($("#curBox").val() == "1") { // 载重1
        var listValue = $("#listValue").val();
        $("#dataList").empty();
        if (listValue != null && listValue != '' && listValue != '[]') {
          var list = JSON.parse(listValue);
          if (list != null) {
            for (var i = 0; i < list.length; i++) {
              var str = "";
              str += "<tr class='odd'>";
              str += "<td class='text-center'>" + (i + 1) + "</td>"; // 序号
              if (i == 0) {
                //第一行不能删除 没有复选框
                str += "<td class='text-center'><input class='number text-center' name='number' style='border:none;background-color:transparent;' readonly='readonly' /></td>";
                str += "<td class='text-center'><input class='adValue text-center' name='adValue' value='" + parseInt(list[i].adValue == '' ? 0 : list[i].adValue) + "' onkeyup='value=value.replace(/[^0-9]/g,\"\")' maxlength='5' style='border:1px solid #f9f9f9;background-color:transparent;' /></td>";
                str += "<td class='text-center'><input class='adActualValue text-center' name='adActualValue' readonly='readonly'  value='" + parseFloat(list[i].adActualValue == '' ? 0 : list[i].adActualValue).toFixed(1) + "' onkeyup='value=value.replace(/[^0-9.]/g,\"\")' maxlength='10' style='border:1px solid #f9f9f9;background-color:transparent;' /></td>";
              } else {
                str += "<td class='text-center'><input class='number text-center' name='number'  type='checkbox' style='border:none;background-color:transparent;'  /></td>";
                str += "<td class='text-center'><input class='adValue text-center' name='adValue' value='" + parseInt(list[i].adValue == '' ? 0 : list[i].adValue) + "' onkeyup='value=value.replace(/[^0-9]/g,\"\")' maxlength='5' style='border:1px solid #f9f9f9;background-color:transparent;' /></td>";
                str += "<td class='text-center'><input class='adActualValue text-center' name='adActualValue' value='" + parseFloat(list[i].adActualValue == '' ? 0 : list[i].adActualValue).toFixed(1) + "' onkeyup='value=value.replace(/[^0-9.]/g,\"\")' maxlength='10' style='border:1px solid #f9f9f9;background-color:transparent;' /></td>";
              }
              str += "</tr>";
              $("#dataList").append(str);
            }

          }

        } else {
          var str = "";
          str += "<tr class='odd'>";
          str += "<td class='text-center'>" + 1 + "</td>"; // 序号
          str += "<td class='text-center'><input class='number text-center' name='number' style='border:none;background-color:transparent;' readonly='readonly'  /></td>";
          str += "<td class='text-center'><input class='adValue text-center' placeholder='请先输入起始值再新增' name='adValue' onkeyup='value=value.replace(/[^0-9]/g,\"\")' maxlength='5' style='border:1px solid #f9f9f9;background-color:transparent;' /></td>";
          str += "<td class='text-center'><input class='adActualValue text-center' name='adActualValue' readonly='readonly'  value='" + 0.0 + "' onkeyup='value=value.replace(/[^0-9.]/g,\"\")' maxlength='10' style='border:1px solid #f9f9f9;background-color:transparent;' /></td>";
          str += "</tr>";
          $("#dataList").append(str);
        }
      } else { // 载重2
        var listValue = $("#listValue2").val();
        $("#dataList").empty();
        if (listValue != null && listValue != '' && listValue != '[]') {
          var list = JSON.parse(listValue);
          if (list != null) {
            for (var i = 0; i < list.length; i++) {
              var str = "";
              str += "<tr class='odd'>";
              str += "<td class='text-center'>" + (i + 1) + "</td>"; // 序号
              if (i == 0) {
                str += "<td class='text-center'><input class='number text-center' name='number'  style='border:none;background-color:transparent;' readonly='readonly' /></td>";
                str += "<td class='text-center'><input class='adValue text-center' name='adValue' value='" + parseInt(list[i].adValue == '' ? 0 : list[i].adValue) + "' onkeyup='value=value.replace(/[^0-9]/g,\"\")' maxlength='5' style='border:1px solid #f9f9f9;background-color:transparent;' /></td>";
                str += "<td class='text-center'><input class='adActualValue text-center' name='adActualValue'  readonly='readonly' value='" + parseFloat(list[i].adActualValue == '' ? 0 : list[i].adActualValue).toFixed(1) + "' onkeyup='value=value.replace(/[^0-9.]/g,\"\")' maxlength='10' style='border:1px solid #f9f9f9;background-color:transparent;' /></td>";
              } else {
                str += "<td class='text-center'><input class='number text-center' name='number' type='checkbox' value='" + (i + 1) + "' style='border:none;background-color:transparent;'  /></td>";
                str += "<td class='text-center'><input class='adValue text-center' name='adValue' value='" + parseInt(list[i].adValue == '' ? 0 : list[i].adValue) + "' onkeyup='value=value.replace(/[^0-9]/g,\"\")' maxlength='5' style='border:1px solid #f9f9f9;background-color:transparent;' /></td>";
                str += "<td class='text-center'><input class='adActualValue text-center' name='adActualValue' value='" + parseFloat(list[i].adActualValue == '' ? 0 : list[i].adActualValue).toFixed(1) + "' onkeyup='value=value.replace(/[^0-9.]/g,\"\")' maxlength='10' style='border:1px solid #f9f9f9;background-color:transparent;' /></td>";
              }
              str += "</tr>";
              $("#dataList").append(str);
            }

          }

        } else {
          var str = "";
          str += "<tr class='odd'>";
          str += "<td class='text-center'>" + 1 + "</td>"; // 序号
          str += "<td class='text-center'><input class='number text-center' name='number' style='border:none;background-color:transparent;' readonly='readonly'  /></td>";
          str += "<td class='text-center'><input class='adValue text-center' placeholder='请先输入起始值再新增' name='adValue' onkeyup='value=value.replace(/[^0-9]/g,\"\")' maxlength='5' style='border:1px solid #f9f9f9;background-color:transparent;' /></td>";
          str += "<td class='text-center'><input class='adActualValue text-center'  readonly='readonly' name='adActualValue' value='" + 0.0 + "' onkeyup='value=value.replace(/[^0-9.]/g,\"\")' maxlength='10' style='border:1px solid #f9f9f9;background-color:transparent;' /></td>";
          str += "</tr>";
          $("#dataList").append(str);
        }
      }
      $($("#dataList tr input")[1]).on('input',setCalibrationObj.validateFirstValue)
    },

    // 判断车辆是否在线
    checkVehicleOnlineStatus: function () {
      if ($("#vehicleId").val() != null && $("#vehicleId").val() != "") {
        var url = "/clbs/v/oilmassmgt/oilcalibration/checkVehicleOnlineStatus";
        var data = {"vehicleId": $("#vehicleId").val()};
        json_ajax("POST", url, "json", false, data, setCalibrationObj.checkVehicleOnlineStatusCallBack);
      }
    },

    // 判断车辆是否在线回调
    checkVehicleOnlineStatusCallBack: function (data) {
      if (!data.success && data.msg == null) {
        onLineStatus = false; // 不在线
        layer.msg(isOnlineMsg);
        return false;
      } else if (data.success) {
        onLineStatus = true; // 在线
        return true;
      } else if (!data.success && data.msg != null) {
        layer.msg(data.msg, {move: false});
      }

    },

    subscribeLatestLocation: function () {
      // 订阅车辆消息
      var params = [];
      var requestStrS = {
        "desc": {
          "MsgId": 40964,
          "UserName": $("#userName").text()
        },
        "data": params
      };
      setTimeout(function () {
        webSocket.subscribe(headers, "/user/topic/realLocation", setCalibrationObj.getLastOilDataCallBack, "/app/vehicle/realLocation", requestStrS);
      });
    },

    getLastOilDataCallBack: function (msg) {
      var sensorPid;
      if ($("#curBox").val() == 1) {
        sensorPid = 112;
      }
      if ($("#curBox").val() == 2) {
        sensorPid = 113;
      }
      // 解析和处理msg中的数据
      var obj = $.parseJSON(msg.body);
      if (obj != null) {
        // 消息头
        var msgBody = obj.data.msgBody;
        if (msgBody != null) {
          var loadInfos = msgBody.loadInfos;
          if (loadInfos != null && loadInfos != '') {
            for (var i = 0; i < loadInfos.length; i++) {
              var test = loadInfos[i];
              var pid = test.id;
              var originalAd = test.originalAd;
              if (sensorPid == pid) {
                $("#adVal").val(originalAd);
                window.clearTimeout(timeOutId);
                return;
              }
            }
          }
        }
      }
      layer.msg("获取标定数据失败");
    },

    loadcallBack1: function (data) {
      if (data.success) {
        if (data != null && data.obj != null && data.obj.msgSN != null && data.obj.msgSN != "") {
          //  $("#msgSN").val(data.obj.msgSN);
        }
      } else {
        layer.msg(data.msg, {move: false});
      }
    },

      checkERROR: function(){
          var tr = $("#dataList tr");
          var adValue = "";
          var adActualValue = "";
          var previousAd = ""; //上一个ad值  用于判断AD值是否逐渐增加
          var previousActual = ""; //上一个实际值  用于判断AD值是否逐渐增加
          for (var i = 0; i < tr.length; i++) {
              adValue = parseInt($(tr[i]).find(".adValue").val());
              adActualValue = parseFloat($(tr[i]).find(".adActualValue").val());
              if(isNaN(adValue) || isNaN(adActualValue)){
                  return false;
              }
              if(i == 0){
                  if(adValue == null || adValue == '' ) {
                      return false;
                  }
              }else {
                  if(adValue == null || adValue == '' || adActualValue == null || adActualValue =='') {
                      return false;
                  }
                  if (adValue <= previousAd) {
                      return false;
                  }
                  if (adActualValue <= previousActual) {
                      return false;
                  }
              }
              previousAd = adValue;
              previousActual = adActualValue;
          }
          return true;
      },
    validateFirstValue: function(){
      if( $("#dataList tr").length == 1){
        var target = $("#dataList tr input")[1]
        if(!$(target).val()){
          if(target.nextElementSibling && target.nextElementSibling.tagName.toLowerCase() == 'p'){
            return
          }
          var p = document.createElement('p')
          p.innerHTML = '请先输入起始值再新增'
          p.style.position = "absolute"
          p.style.bottom = "-35px"
          p.style.left = "0"
          p.style.color = "#ff1d1d"
          p.style.right = "0"
          $(target).parent().css('position','relative')
          $(target).parent().append(p)
          return false
        }
        $(target).next().remove()
        return true
      }
      return true
    },

    addadActualVal: function () {
      if(!setCalibrationObj.validateFirstValue()){
        return
      }
        if(!setCalibrationObj.checkERROR()){
            layer.msg("AD值或实际载重值存在矛盾，请确认", {move: false});
            return;
        }
      if ($("#adVal").val() == null || $("#adVal").val() == '') {
        layer.msg("请输入AD值", {move: false});
        return;
      }
      if ($("#adActualVal").val() == null || $("#adActualVal").val() == '') {
        layer.msg("请输入载重值", {move: false});
        return;
      }
      if (parseInt($("#adVal").val()) != $("#adVal").val() || $("#adVal").val().indexOf('.') != -1) {
        layer.msg("AD值请输入整数", {move: false});
        return;
      }
      if (parseInt($("#adVal").val()) < 0 || parseInt($("#adVal").val()) > 65535) {
        layer.msg("AD值输入范围为：0~65535", {move: false});
        return;
      }
      if (isNaN($("#adActualVal").val())) {
        layer.msg("实际载重值输入错误", {move: false});
        return;
      }
      if (parseFloat($("#adActualVal").val()) < 0) {
        layer.msg("实际载重值请输入正数", {move: false});
        return;
      }
      var adVal = parseInt($("#adVal").val());
      var adActualVal = parseFloat($("#adActualVal").val());
      var length = $("#dataList tr").length;
      if (length >= 50) {
        layer.msg("标定数组已达到最大，请确认", {move: false});
        return;
      }
      var tr = $("#dataList tr");
      var oneAD = $(tr[0]).find(".adValue").val();
      if (oneAD == null || oneAD == '') {
        layer.msg("请输入序号1的AD值", {move: false});
        return;
      }
      if (adVal <= parseFloat($(tr[0]).find(".adValue").val())) {
        layer.msg("新增的AD值不能<=已有标定数组中最小的AD值", {move: false});
        return;
      }
      for (var i = 0; i < tr.length; i++) {
        var adValue = parseInt($(tr[i]).find(".adValue").val());
        var adActualValue = parseFloat($(tr[i]).find(".adActualValue").val());
        if (adValue == adVal) {
          layer.msg("AD值已存在，请确认", {move: false});
          return;
        }
        if (adActualVal == adActualValue) {
          layer.msg("实际载重值已存在，请确认", {move: false});
          return;
        }
      }
      for (var i = 0; i < tr.length; i++) {
        var adValue = parseInt($(tr[i]).find(".adValue").val());
        if (adValue > adVal) {
          var adActualValueNext = parseFloat($(tr[i]).find(".adActualValue").val());
          var adActualValuePre = parseFloat($(tr[i - 1]).find(".adActualValue").val());
          //新增的ad  其实际载重也应该比前1个大  后一个小
          if (!(adActualVal < adActualValueNext && adActualVal > adActualValuePre)) {
            layer.msg("实际载重值前后矛盾，请确认", {move: false});
            return;
          }
          break;
        }
      }
      if (adVal > parseInt($(tr[tr.length - 1]).find(".adValue").val())) {
        if (!adActualVal > parseFloat($(tr[tr.length - 1]).find(".adActualValue").val())) {
          layer.msg("新增的AD值是最大值，则新增的实际载重也需要是最大值", {move: false});
          return;
        }
      }

      var str = "";
      str += "<tr class='odd'>";
      str += "<td class='text-center'>" + (length + 1) + "</td>"; // 序号
      str += "<td class='text-center'><input class='number text-center' name='number' type='checkbox' style='border:none;background-color:transparent;' readonly='readonly' /></td>";
      str += "<td class='text-center'><input class='adValue text-center' name='adValue' value='" + adVal + "' onkeyup='value=value.replace(/[^0-9]/g,\"\")' maxlength='5' style='border:1px solid #f9f9f9;background-color:transparent;' /></td>";
      str += "<td class='text-center'><input class='adActualValue text-center' name='adActualValue' value='" + adActualVal.toFixed(1) + "' onkeyup='value=value.replace(/[^0-9.]/g,\"\")' maxlength='10' style='border:1px solid #f9f9f9;background-color:transparent;' /></td>";
      str += "</tr>";
      for (var i = 0; i < tr.length; i++) {
        var adValue = parseInt($(tr[i]).find(".adValue").val());
        if (adVal < adValue) {
          $(str).insertBefore($(tr[i]));
          setCalibrationObj.order();
          $("#adVal").val("");
          $("#adActualVal").val("");

          return;
        }
      }
      $("#dataList").append(str);
      setCalibrationObj.order();
      $("#adVal").val("");
      $("#adActualVal").val("");
    },

    getADVal: function () {
      var topost = {};
      topost.vid = $("#vehicleId").val();
      topost.type = $("#curBox").val();
      setCalibrationObj.checkVehicleOnlineStatus();
      if (onLineStatus) { // 如果车辆在线，才执行如下的操作
        // 订阅车辆位置信息
        setCalibrationObj.subscribeLatestLocation();
        var url = "/clbs/v/loadmgt/loadvehiclesetting/getLatestOilData";
        var data = {"vehicleId": $("#vehicleId").val()};
        json_ajax("POST", url, "json", true, data, setCalibrationObj.loadcallBack1);
        timeOutId = window.setTimeout(function () {
          layer.msg('获取数据超时')
        }, 20000);
      }
    },

    /**
     * 排序
     **/
    order: function () {
      var tr = $("#dataList tr");
      for (var i = 0; i < tr.length; i++) {
        $(tr[i]).find('td:eq(0)').text(i + 1);
      }
    },

    submitOilCal: function () {
      var tr = $("#dataList tr");
      if (tr.length < 2 || tr.length > 50) {
        layer.msg("标定数组至少要大于等于2组小于等于50组", {move: false});
        return;
      }
      var listValue = new Array();
      var adValue = "";
      var adActualValue = "";
      var previousAd = ""; //上一个ad值  用于判断AD值是否逐渐增加
      var previousActual = ""; //上一个实际值  用于判断AD值是否逐渐增加
      for (var i = 0; i < tr.length; i++) {
        var values = {};
        adValue = parseInt($(tr[i]).find(".adValue").val());
        adActualValue = parseFloat($(tr[i]).find(".adActualValue").val());
        if(isNaN(adValue) || isNaN(adActualValue)){
            layer.msg("AD值或实际载重不能为空", {move: false});
            return;
        }
        if (i == 0) {
          if (adActualValue != 0 ) {
            layer.msg("序号为1的实际载重值必须为0", {move: false});
            return;
          }
        } else {
          if (adValue <= previousAd) {
            layer.msg("AD值不合要求，请确认", {move: false});
            return;
          }
          if (adActualValue <= previousActual) {
            layer.msg("实际载重值不合要求，请确认", {move: false});
            return;
          }
        }
        previousAd = adValue;
        previousActual = adActualValue;
        values.adValue = adValue;
        values.adActualValue = adActualValue;
        listValue.push(values);
      }
      listValue = JSON.stringify(listValue);
      if ($("#curBox").val() == "1") {
        $("#listValue").val(listValue);
      } else {
        $("#listValue2").val(listValue);
      }
      $("#commonLgWin").modal("hide");
    },

    /**
     * 修改校验AD
     **/
    editAd: function () {
      var $tr = $(this).closest('tr');
      var $prevTr = $tr.prev();
      var $nextTr = $tr.next();
      var adValue = parseInt($tr.find(".adValue").val());
      if ($prevTr.length > 0) {
        var preValue = parseInt($prevTr.find(".adValue").val());
        if (adValue <= preValue) {
          layer.msg("AD值与前后数据矛盾，请确认", {move: false});
          return;
        }
      }
      if ($nextTr.length > 0) {
        var nextValue = parseInt($nextTr.find(".adValue").val());
        if (adValue >= nextValue) {
          layer.msg("AD值与前后数据矛盾，请确认", {move: false});
          return;
        }
      }
    },

    /**
     * 修改时校验实际载重
     **/
    editActual: function () {
      var $tr = $(this).closest('tr');
      var $prevTr = $tr.prev();
      var $nextTr = $tr.next();
      var actualValue = parseFloat($tr.find(".adActualValue").val());
      if ($prevTr.length > 0) {
        var preValue = parseFloat($prevTr.find(".adActualValue").val());
        if (actualValue <= preValue) {
          layer.msg("你修改实际载重与前后数据矛盾，请确认", {move: false});
          return;
        }
      }
      if ($nextTr.length > 0) {
        var nextValue = parseFloat($nextTr.find(".adActualValue").val());
        if (actualValue >= nextValue) {
          layer.msg("你修改实际载重与前后数据矛盾，请确认", {move: false});
          return;
        }
      }
    },

    checked: function () {
      if (this.checked) {
        var tr = $("#dataList tr");
        for (var i = 0; i < tr.length; i++) {
          $(tr[i]).find(".number").prop("checked", true);
        }
      } else {
        var tr = $("#dataList tr");
        for (var i = 0; i < tr.length; i++) {
          $(tr[i]).find(".number").prop("checked", false);
        }
      }
    },

    del: function () {
      var checkInput = $("#dataList tr input:checked");
      var len = checkInput.length;
      if (len == 0) {
        layer.msg('请先勾选！')
      } else {
        for (var i = 0; i < len; i++) {
          $(checkInput[i]).closest('tr').remove();
        }
        var tr = $("#dataList tr");
        for (var i = 0; i < tr.length; i++) {
          $(tr[i]).find('td:eq(0)').text(i + 1);
        }
      }
    },

    /**
     * 实际载重获焦时计算实际载重值
     */
    calculationLoad: function () {
      var adVal = parseFloat($("#adVal").val());
      var tr = $("#dataList tr");
      var minAdValue = parseFloat($(tr[0]).find(".adValue").val());
      var maxAdValue = parseFloat($(tr[tr.length - 1]).find(".adValue").val());
      var maxAdActualValue = parseFloat($(tr[tr.length - 1]).find(".adActualValue").val());
      if (adVal <= minAdValue) {
        //当输入或获取的AD值<=标定数组中最小的AD值时，不用计算
        return;
      }
      if (tr.length == 1) {
        //当只有一组实际载重为0的标定数据，不用计算；
        return;
      }
      if (maxAdValue == adVal) {
        //等于最大值时不计算
        return;
      }
      if (adVal > maxAdValue) {
        //当输入或获取的AD值在已有的标定数组中最大，则根据已有数组AD值最大的两个数组进行比例算出对应的实际载重
        var twoAdValue = parseFloat($(tr[tr.length - 2]).find(".adValue").val());
        var twoAdActualValue = parseFloat($(tr[tr.length - 2]).find(".adActualValue").val());
        var m = (adVal - maxAdValue) / (maxAdValue - twoAdValue);
        var adActualValue = m * (maxAdActualValue - twoAdActualValue) + maxAdActualValue;
        $("#adActualVal").val(adActualValue.toFixed(1));
        return;
      }
      //剩下的是输入或获取的AD值在已有的标定数组之间  通过前后的ad值来确认实际载重
      for (var i = 0; i < tr.length; i++) {
        var adValue = parseFloat($(tr[i]).find(".adValue").val());
        var adActualValue = parseFloat($(tr[i]).find(".adActualValue").val());
        if (adVal < adValue) {
          var twoAdValue = parseFloat($(tr[i - 1]).find(".adValue").val());
          var twoAdActualValue = parseFloat($(tr[i - 1]).find(".adActualValue").val());
          var m = (adVal - adValue) / (adValue - twoAdValue);
          var adActualValue = m * (adActualValue - twoAdActualValue) + adActualValue;
          $("#adActualVal").val(adActualValue.toFixed(1));
          return;
        }
      }
    },
  }
  $(function () {
    $('input').inputClear();
    setCalibrationObj.init();
    $("#submitBtn").bind("click", setCalibrationObj.submitOilCal);
  })
}(window, $));