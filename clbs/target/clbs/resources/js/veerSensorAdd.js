(function (window, $) {
  addVeerSensor = {
    doSubmits: function () {
      if (!addVeerSensor.validates()) return;
      addVeerSensor.hideErrorMsg();
      if ($.trim($("#sensorNumber").val()) == "") {
        addVeerSensor.showErrorMsg(veerSensorNull, "sensorNumber");
        return;
      }
      var reg = /^[A-Za-z0-9_#\*\u4e00-\u9fa5\-]+$/;
      if (!reg.test($("#sensorNumber").val())) {
        addVeerSensor.showErrorMsg(sensorInputRex,"sensorNumber");
        return;
      }
      if ($("#remark").val().length > 40) {
        addVeerSensor.showErrorMsg(veerSensorRemarkLenth, "remark");
        return;
      }
      addHashCode($("#addForm"));
      $("#addForm").ajaxSubmit(function (data) {
        data = JSON.parse(data);

        if (data.success) {
          $("#commonWin").modal("hide");
          layer.msg("添加成功！", {move: false});
          //关闭弹窗
          myTable.requestData();
        } else {
          if (data.msg.toString().indexOf("型号") > -1) {
            addVeerSensor.showErrorMsg(data.msg, "identId");
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
            required: veerSensorNull,
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
    $("#doSubmitsAdd").bind("click", addVeerSensor.doSubmits);
  })
})(window, $)