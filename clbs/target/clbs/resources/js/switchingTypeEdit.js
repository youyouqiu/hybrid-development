(function (window, $) {
  editSwitchSensorType = {
    //提交
    doSubmits: function () {
      if (!editSwitchSensorType.validates()) return;
      editSwitchSensorType.hideErrorMsg();
      if ($.trim($("#identify").val()) == null || $.trim($("#identify").val()) == "") {
        editSwitchSensorType.showErrorMsg(identifyNull, "identify");
        return;
      }
      if ($.trim($("#name").val()) == "") {
        editSwitchSensorType.showErrorMsg(identifyTypeNull, "name");
        return;
      }
      if ($.trim($("#stateOne").val()) == "") {
        editSwitchSensorType.showErrorMsg("请输入状态1", "stateOne");
        return;
      } else {
        var state = editSwitchSensorType.repetition($("#stateOne").val(), 1);
        if (!state) {
          editSwitchSensorType.showErrorMsg("您输入的状态1已经存在", "stateOne");
          return;
        }
      }
      if ($.trim($("#stateTwo").val()) == "") {
        editSwitchSensorType.showErrorMsg("请输入状态2", "stateTwo");
        return;
      } else {
        var state = editSwitchSensorType.repetition($("#stateTwo").val(), 2);
        if (!state) {
          editSwitchSensorType.showErrorMsg("您输入的状态2已经存在", "stateTwo");
          return;
        }
      }
      addHashCode($("#editForm"));
      $("#editForm").ajaxSubmit(function (data) {
        data = JSON.parse(data);
        if (data.success) {
          $("#commonSmWin").modal("hide");
          //关闭弹窗
          myTable.refresh()
        } else {
          if (data.msg.toString().indexOf("ID") > -1) {
            editSwitchSensorType.showErrorMsg(data.msg, "identify");
          } else if (data.msg.toString().indexOf("功能类型") > -1) {
            editSwitchSensorType.showErrorMsg(data.msg, "name");
          } else if (data.msg.toString().indexOf("系统错误") > -1) {
            layer.msg(data.msg, {move: false});
          }
          return;
        }
      });
    },
    validates: function () {
      return $("#editForm").validate({
        rules: {
          name: {
            required: true,
            maxlength: 25,
            isRightSensorModel: true,
          },
        },
        messages: {
          name: {
            required: identifyTypeNull,
            maxlength: sensorModelError,
          },
        }
      }).form();
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
    //错误提示信息隐藏
    hideErrorMsg: function () {
      $("#error_label_add").hide();
    },
    repetition: function (state, flag) {
      var result = null;
      $.ajax({
        type: "POST",
        url: "/clbs/m/switching/type/repetition",
        data: {
          "id": $("input[name='id']").val(),
          "state": state,
          "flag": flag
        },
        dataType: "json",
        async: false,
        success: function (data) {
          result = data;
        }
      });
      return result;
    }
  }
  $(function () {
    $('input').inputClear();
    $("#doSubmits").bind("click", editSwitchSensorType.doSubmits);
  })
})(window, $)