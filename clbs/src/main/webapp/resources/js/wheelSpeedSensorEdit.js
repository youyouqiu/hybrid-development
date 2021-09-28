(function (window, $) {
  editSpeedSensor = {
    doSubmits: function () {
      if (!editSpeedSensor.formValidate()) return;
      editSpeedSensor.hideErrorMsg();
      if ($.trim($("#oilWearNumber").val()) == "") {
        editSpeedSensor.showErrorMsg(speedSensorNull, "oilWearNumber");
        return;
      }
      if ($("#oilWearNumber").val().length > 25) {
        editSpeedSensor.showErrorMsg(speedSensorLenth, "oilWearNumber");
        return;
      }
      addHashCode($("#editForm"));
      $("#editForm").ajaxSubmit(function (data) {
        data = JSON.parse(data);
        if (data.success) {
          $("#commonWin").modal("hide");
          layer.msg(data.msg, {move: false});
          //关闭弹窗
          myTable.refresh();
        } else if (!data.success && data.msg.toString().indexOf(speedSensorExist) > -1) {
          editSpeedSensor.showErrorMsg(data.msg, "identId");
          return;
        } else if (!data.success && data.msg.toString().indexOf(publicError) > -1) {
          layer.msg(data.msg, {move: false});
        }
      });
    },
    showErrorMsg: function (msg, inputId) {
      if ($("#error_label_edit").is(":hidden")) {
        $("#error_label_edit").text(msg);
        $("#error_label_edit").insertAfter($("#" + inputId));
        $("#error_label_edit").show();
      } else {
        $("#error_label_edit").is(":hidden");
      }
    },
    formValidate: function () {
      return $("#editForm").validate({
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
      $("#error_label_edit").hide();
    },
  }
  $(function () {
    $('input').inputClear();
    $("#editSubmits").bind("click", editSpeedSensor.doSubmits);
  })
})(window, $)