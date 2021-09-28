(function (window, $) {
  var dataformInit = $("#editForm").serializeArray();
  var jsonTextInit = JSON.stringify({dataform: dataformInit});
  editVeerSensor = {
    doSubmits: function () {
      if (!editVeerSensor.validates()) return;
      editVeerSensor.hideErrorMsg();
      if ($.trim($("#sensorNumber").val()) == "") {
        editVeerSensor.showErrorMsg(veerSensorNull, "sensorNumber");
        return;
      }
      var reg = /^[A-Za-z0-9_#\*\u4e00-\u9fa5\-]+$/;
      if (!reg.test($("#sensorNumber").val())) {
        editVeerSensor.showErrorMsg(sensorInputRex, "sensorNumber");
        return;
      }
      if ($("#remark").val().length > 40) {
        editVeerSensor.showErrorMsg(veerSensorRemarkLenth, "remark");
        return;
      }
      var dataform = $("#editForm").serializeArray();
      var jsonText = JSON.stringify({dataform: dataform});
      if (jsonTextInit == jsonText) {
        $("#commonWin").modal("hide");
        return;
      }
      addHashCode($("#editForm"));
      $("#editForm").ajaxSubmit(function (data) {
        data = JSON.parse(data);
        if (data.success) {
          $("#commonWin").modal("hide");
          layer.msg("修改成功！", {move: false});
          //关闭弹窗
          myTable.refresh();
        } else {
          if (data.msg.toString().indexOf("型号") > -1) {
            editVeerSensor.showErrorMsg(data.msg, "identId");
            return;
          } else if (data.msg == null) {
            layer.msg("修改失败！", {move: false});
          } else if (data.msg.toString().indexOf("系统错误") > -1) {
            layer.msg(data.msg, {move: false});
          }
        }
      });
    }, showErrorMsg: function (msg, inputId) {
      if ($("#error_label_edit").is(":hidden")) {
        $("#error_label_edit").text(msg);
        $("#error_label_edit").insertAfter($("#" + inputId));
        $("#error_label_edit").show();
      } else {
        $("#error_label_edit").is(":hidden");
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
            required: veerSensorNull,
            maxlength: sensorModelError,
          },
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
    $("#doSubmitsEdit").bind("click", editVeerSensor.doSubmits);
  })
})(window, $)