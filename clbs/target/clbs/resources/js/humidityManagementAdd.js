(function (window, $) {
  humidityManagementAdd = {
    doSubmits: function () {
      if (!humidityManagementAdd.validates()) return;
      humidityManagementAdd.hideErrorMsg();
      if ($.trim($("#sensorNumber").val()) == "") {
        humidityManagementAdd.showErrorMsg(humiditySensorNull, "sensorNumber");
        return;
      }
      if ($("#sensorNumber").val().length > 25) {
        humidityManagementAdd.showErrorMsg(humiditySensorTypeLenth, "sensorNumber");
        return;
      }
      addHashCode($("#addForm"));
      $("#addForm").ajaxSubmit(function (data) {
        data = JSON.parse(data);
        if (data.success) {
          $("#commonWin").modal("hide");
          layer.msg("添加成功！", {move: false});
          myTable.requestData();
        } else {
          if (data.msg.toString().indexOf("型号") > -1) {
            humidityManagementAdd.showErrorMsg(data.msg, "identId");
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
    $("#doSubmitsAdd").bind("click", humidityManagementAdd.doSubmits);
  })
})(window, $)