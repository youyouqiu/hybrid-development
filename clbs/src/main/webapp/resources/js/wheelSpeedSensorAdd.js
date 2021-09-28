(function (window, $) {
  addSpeedSensor = {
    doSubmits: function () {
      if (!addSpeedSensor.formValidate()) return;
      addSpeedSensor.hideErrorMsg();
      if ($.trim($("#oilWearNumber").val()) == "") {
        addSpeedSensor.showErrorMsg(speedSensorNull, "oilWearNumber");
        return;
      }
      if ($("#oilWearNumber").val().length > 25) {
        addSpeedSensor.showErrorMsg(speedSensorLenth, "oilWearNumber");
        return;
      }
      addHashCode($("#addForm"));
      $("#addForm").ajaxSubmit(function (data) {
        data = JSON.parse(data);
        if (data.success) {
          $("#commonWin").modal("hide");
          layer.msg(data.msg, {move: false});
          //关闭弹窗
          myTable.requestData();
        } else if (!data.success && data.msg.toString().indexOf(speedSensorExist) > -1) {
          addSpeedSensor.showErrorMsg(data.msg, "identId");
          return;
        } else if (!data.success && data.msg.toString().indexOf(publicError) > -1) {
          layer.msg(data.msg, {move: false});
        }
      });
    },
    showErrorMsg: function (msg, inputId) {
      if ($("#error_label_add").is(":hidden")) {
        $("#error_label_add").text(msg);
        $("#error_label_add").insertAfter($("#" + inputId));
        $("#error_label_add").show();
      } else {
        $("#error_label_add").is(":hidden");
      }
    },
    formValidate: function () {
      return $("#addForm").validate({
        rules: {
          sensorType: {
            required: true,
            maxlength:25,
            isRightSensorModel: true,
          },
        },
        messages: {
          sensorType: {
            required: speedSensorNull,
            maxlength: sensorModelError,
          }
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
    $("#addSubmits").bind("click", addSpeedSensor.doSubmits);
  })
})(window, $)