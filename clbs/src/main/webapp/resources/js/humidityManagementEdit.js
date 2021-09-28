(function (window, $) {
  humidityManagementEdit = {
    doSubmits: function () {
      if (!humidityManagementEdit.validates()) return;
      humidityManagementEdit.hideErrorMsg();
      if ($.trim($("#sensorNumber").val()) == "") {
        humidityManagementEdit.showErrorMsg(humiditySensorNull, "sensorNumber");
        return;
      }
      if ($("#sensorNumber").val().length > 25) {
        humidityManagementEdit.showErrorMsg(humiditySensorTypeLenth, "sensorNumber");
        return;
      }
      addHashCode($("#editForm"));
      $("#editForm").ajaxSubmit(function (data) {
        data = JSON.parse(data);
        if (data.success) {
          $("#commonWin").modal("hide");
          layer.msg("设置成功！", {move: false});
          //关闭弹窗
          myTable.refresh();
        } else {
          if (data.msg.toString().indexOf("型号") > -1) {
            humidityManagementEdit.showErrorMsg(data.msg, "identId");
            return;
          } else if (data.msg == null) {
            layer.msg("设置失败！", {move: false});
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
      return $("#editForm").validate({
        rules: {
          sensorNumber: {
            required: true,
            maxlength: 25,
            isRightSensorModel: true,
          },
        },
        messages: {
          sensorNumber: {
            required: humiditySensorNull,
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
    $("#doSubmitsEdit").bind("click", humidityManagementEdit.doSubmits);
  })
})(window, $)