(function (window, $) {
  addSwitchSensorType = {
    //提交
    doSubmits: function () {
      addSwitchSensorType.hideErrorMsg();
      if (addSwitchSensorType.validates()) {
        addHashCode($("#addForm"));
        $("#addForm").ajaxSubmit(function (data) {
          data = JSON.parse(data);
          if (data.success) {
            $("#commonSmWin").modal("hide");
            //关闭弹窗
            myTable.requestData()
          } else {
            if (data.msg.toString().indexOf("ID") > -1) {
              addSwitchSensorType.showErrorMsg(data.msg, "identify");
            } else if (data.msg.toString().indexOf("功能类型") > -1) {
              addSwitchSensorType.showErrorMsg(data.msg, "name");
            } else if (data.msg.toString().indexOf("系统错误") > -1) {
              layer.msg(data.msg, {move: false});
            }
            return;
          }
        });
      }
    },
    validates: function () {
      return $("#addForm").validate({
        rules: {
          identify: {
            required: true,
            identifyValidate: true
          },
          name: {
            required: true,
            maxlength: 25,
            isRightSensorModel: true,
          },
          stateOne: {
            required: true,
            maxlength: 20,
            remote: {
              type: "post",
              async: false,
              url: "/clbs/m/switching/type/repetition",
              data: {
                state: function () {
                  return $("input[name='stateOne']").val();
                },
                flag: 1,
                id: ''
              }
            }
          },
          stateTwo: {
            required: true,
            maxlength: 20,
            remote: {
              type: "post",
              async: false,
              url: "/clbs/m/switching/type/repetition",
              data: {
                state: function () {
                  return $("input[name='stateTwo']").val();
                },
                flag: 2,
                id: ''
              }
            }
          }
        },
        messages: {
          identify: {
            required: '请输入功能ID'
          },
          name: {
            required: '请输入检测功能类型',
            maxlength: sensorModelError,
          },
          stateOne: {
            required: "请输入状态1",
            maxlength: "长度不能大于20",
            remote: "您输入的状态1已经存在"
          },
          stateTwo: {
            required: "请输入状态2",
            maxlength: "长度不能大于20",
            remote: "您输入的状态2已经存在"
          }
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
    }
  }
  $(function () {
    //自定义功能ID验证规则
    jQuery.validator.addMethod("identifyValidate", function (value, element) {
      var reg = /^0x[0-9a-fA-F]{4}$/;
      var flag = this.optional(element) || (reg.test(value));
      if (!flag) {
        return flag;
      }
      var v = parseInt(reg.test(value), 16)
      if (v < 0 || v > 65535) {
        flag = false;
      }
      return flag;
    }, "功能ID格式和范围:0x0000~0xFFFF");

    $('input').inputClear();
    $("#doSubmits").bind("click", addSwitchSensorType.doSubmits);

    //监听功能ID输入框值变化
    $("#identify").on("input propertychange", addSwitchSensorType.hideErrorMsg);
    $("#identify").on("focus", addSwitchSensorType.hideErrorMsg);
  })
})(window, $)