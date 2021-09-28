(function (window, $) {
  addTemperatureManagement = {
    doSubmits: function () {
      if (!addTemperatureManagement.validates()) return;
      addTemperatureManagement.hideErrorMsg();
      if ($.trim($("#sensorNumber").val()) == "") {
        addTemperatureManagement.showErrorMsg(temperatureSensorNull, "sensorNumber");
        return;
      }
      if ($("#sensorNumber").val().length > 25) {
        addTemperatureManagement.showErrorMsg(temperatureSensorTypeLenth, "sensorNumber");
        return;
      }
      addHashCode($("#addForm"));
      $("#addForm").ajaxSubmit(function (data) {
        data = JSON.parse(data);
        if (data.success) {
          $("#commonWin").modal("hide");
          layer.msg("添加成功！", {move: false});
          //关闭弹窗
          myTable.requestData()
        } else {
          if (data.msg.toString().indexOf("型号") > -1) {
            addTemperatureManagement.showErrorMsg(data.msg, "identId");
            return;
          } else if (data.msg.toString().indexOf("系统错误") > -1) {
            layer.msg(data.msg, {move: false});
          }
        }
      });
    }, showErrorMsg: function (msg, inputId) {
      if ($("#error_label_add").is(":hidden")) {
        $("#error_label_add").text(msg);
        $("#error_label_add").insertAfter($("#" + inputId));
        $("#error_label_add").show();
      } else {
        $("#error_label_add").is(":hidden");
      }
    },
    validates: function () {
      return $("#addForm").validate({
        rules: {
          sensorNumber: {
            required: true,
            maxlength: 25,
            isRightSensorModel: true,
          },
        },
        messages: {
          sensorNumber: {
            required: temperatureSensorNull,
            maxlength: sensorModelError,
          },
        }
      }).form();
    },
    //错误提示信息隐藏
    hideErrorMsg: function () {
      $("#error_label_add").hide();
    }
  }
  $(function () {
    $('input').inputClear();
    $("#doSubmitsAdd").bind("click", addTemperatureManagement.doSubmits);
  })
})(window, $)