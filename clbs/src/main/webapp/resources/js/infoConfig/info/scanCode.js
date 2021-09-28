// 扫码录入

define(function() {
  var keyFlag = true;
  var sweepCodeMonitorType = 0;//监控对象类型

  var sweepCodeEntry = {
    //扫码录入执行方法
    PCKeyDownEvent: function () {
      $("#scanSim").on("keydown", function (e) {
        var key = e.which;
        if (key == "13") {
          if ($("#scanSim").val() == "" || $("#scanSim").val() == null) {
            setTimeout(function () {
              $("#scanSim").click().focus();
            }, 500);
          } else {
            setTimeout(function () {
              $("#terminal").click().focus();
            }, 500);
            var sim = $("#scanSim").val();
            $.ajax({
              type: 'POST',
              async: true,
              data: { "sim": sim, "monitorType": sweepCodeMonitorType },
              url: '/clbs/m/infoconfig/infoFastInput/getRandomNumbers',
              dataType: 'json',
              success: function (data) {
                if (data == 26) {
                  layer.msg("你就这么无聊吗?扫同一张卡26次，卡表示已经不行了，请换一张卡或者把没用的监控对象删了吧！");
                  $("#monitorTheObject").val("");
                } else if (data == -1) {
                  layer.msg("系统响应异常，请稍后再试或联系管理员！");
                } else {
                  $("#monitorTheObject").val(data);
                }
              },
              error: function () {
                layer.msg("获取监控对象编号异常!");
              }
            });
            $.ajax({
              type: 'POST',
              async: true,
              data: { "simcardNumber": sim },
              url: '/clbs/m/infoconfig/infoinput/getSimcardInfoBySimcardNumber',
              dataType: 'json',
              success: function (data) {
                if (data.success) {
                  if (data != null && data.obj != null && data.obj.simcardInfo != null) {
                    $("#scanSim").css("background-color", "#fafafa");
                  } else {
                    $("#scanSim").css("background-color", "rgba(255, 0, 0, 0.1)");
                  }
                } else if (data.msg) {
                  layer.msg(data.msg);
                }
              },
              error: function () {
                layer.msg("判断sim信息失败!");
              }
            });
          }
        }
      })
      $("#terminal").on("keydown", function (ev) {
        var key = ev.which;
        //添加一个标记防止用户连续点击回车重复提交数据
        if (key == "13" && keyFlag) {
          keyFlag = false;
          if ($("#terminal").val() == "" || $("#terminal").val() == null) {
            setTimeout(function () {
              $("#terminal").click().focus();
              keyFlag = true;
            }, 500);
          }
          else if ($("#scanSim").val() == "" || $("#scanSim").val() == null) {
            setTimeout(function () {
              $("#scanSim").click().focus();
              keyFlag = true;
            }, 500);
          }
          else {
            var devices = $("#terminal").val();
            $.ajax({
              type: 'POST',
              async: false,
              data: { "deviceNumber": devices },
              url: '/clbs/m/infoconfig/infoinput/getDeviceInfoByDeviceNumber',
              dataType: 'json',
              success: function (data) {
                if (data.success) {
                  if (data != null && data.obj != null && data.obj.deviceInfo != null) {
                    $("#terminal").css("background-color", "#fafafa");
                  } else {
                    $("#terminal").css("background-color", "rgba(255, 0, 0, 0.1)");
                  }
                } else if (data.msg) {
                  layer.msg(data.msg);
                }
              },
              error: function () {
                layer.msg("判断终端信息失败!");
              }
            });
            if ($('#sweepCodeCitySelidVal').val() == '') {
              infoFastInput.showErrorMsg('请选择分组', 'sweepCodeGroupid');
              return;
            }
            if (sweepCodeEntry.validate()) {
              var str = "";
              var device = $("#terminal").val();
              if (sweepCodeEntry.checkIsBound("brands", $("#monitorTheObject").val())) {
                str += "监控对象[" + $("#monitorTheObject").val() + "]";
              }
              if (sweepCodeEntry.checkIsBound("devices", device)) {
                if (str != '') str += ',';
                str += "终端号[" + $("#terminal").val() + "]";
              }
              if (sweepCodeEntry.checkIsBound("sims", $("#scanSim").val())) {
                if (str != '') str += ',';
                str += "终端手机号[" + $("#scanSim").val() + "]";
              }
              if (str.length > 0) {
                // str = str.substr(0, str.length - 1);
                layer.closeAll();
                layer.msg("不好意思，你来晚了！【" + str + "】已被别人抢先一步绑定了");
                keyFlag = true;
                return;
              }
              var groupIds = $('#sweepCodeCitySelidVal').val();
              if (!infoFastInput.checkGroupNum(groupIds)) return;
              $("#addScanCodeEntry").ajaxSubmit(function (message) {
                var data = JSON.parse(message);
                if(data.success){
                  keyFlag = true;
                  sweepCodeEntry.scanCodeEntryShow();
                  if (navigator.userAgent.indexOf('MSIE') >= 0) {
                    $("#SCEMsgBox").html('<embed id="IEsceMsg" src="../../../file/music/sceMsg.mp3" autostart="true"/>');
                  } else {
                    $("#SCEMsgBox").html('<audio id="SCEMsgAutoOff" src="../../../file/music/sceMsg.mp3" autoplay="autoplay"></audio>');
                  }
                  addFlag = true;
                  myTable.requestData();
                }else{
                  layer.msg(data.msg);
                };
              });
            }
            else {
              keyFlag = true;
            }
          }
        }
        else {
          keyFlag = true;
        }
      })
    },
    // 校验是否已被绑定
    checkIsBound: function (elementId, elementValue) {
      var tempFlag = false;
      var url = "/clbs/m/infoconfig/infoinput/checkIsBound";
      var data = "";
      if (elementId == "brands") {
        data = { "inputId": "brands", "inputValue": elementValue }
      } else if (elementId == "devices") {
        data = { "inputId": "devices", "inputValue": elementValue }
      } else if (elementId == "sims") {
        data = { "inputId": "sims", "inputValue": elementValue }
      }
      $.ajax({
        type: 'POST',
        url: url,
        data: data,
        dataType: 'json',
        async: false,
        success: function (data) {
          if (data.success) {
            if (null != data && data.obj != null && data.obj.isBound) {
              layer.msg("不好意思，你来晚了！【" + data.obj.boundName + "】已被别人抢先一步绑定了");
              tempFlag = true;
            } else {
              tempFlag = false;
            }
          } else if (data.msg) {
            layer.msg(data.msg);
          }
        },
        error: function () {
          layer.msg("校验异常！");
          tempFlag = false;
        }
      });
      return tempFlag;
    },
    //扫码校验
    validate: function () {
      return $("#addScanCodeEntry").validate({
        rules: {
          groupid: {
            required: true
          },
          citySelID: {
            required: true
          },
          brands: {
            required: true,
            isBrand: true,
          },
          sims: {
            isNewSim: true
          },
          devices: {
            checkNewDeviceNumber: "#communicationType"
          }
        },
        messages: {
          citySelID: {
            required: "请至少选择一个分组"
          },
          groupid: {
            required: "请至少选择一个分组"
          },
          brands: {
            required: "监控对象不能为空",
            isBrand: '请重新录入终端手机号',
          },
          sims: {
            isNewSim: '请输入数字，范围：7~20位'
          },
          devices: {
            checkNewDeviceNumber: deviceSpeedNumberError
          }
        }
      }).form();
    },
    //扫码录入显示执行
    scanCodeEntryShow: function () {
      publicFun.onClick();
      //显示
      if ($("#scanCodeEntry").is(":hidden")) {
        $("#scanCodeEntry").show();
        $("#scanSim").val("");
        $("#terminal").val("");
        $("#scanSim").focus();
      } else {
        $("#scanSim").val("");
        $("#terminal").val("");
        $("#monitorTheObject").val("");
        $("#scanSim").focus();
        $("#scanSim").css("background-color", "#fafafa");
        $("#terminal").css("background-color", "#fafafa");
        publicFun.clearErrorMsg();
      }
      //改变标题栏图标
      if ($("#scanCodeEntryContent").is(":hidden")) {
        $("#scanCodeEntryContent").show();
        if ($("#sceFac").hasClass("fa fa-chevron-up")) {
          $("#sceFac").attr("class", "fa fa-chevron-down");
        }
      }
    },
    //车、人、物点击tab切换
    chooseLabClick: function () {
      $("#entryContentBox ul.dropdown-menu").css("display", "none");
      publicFun.hideErrorMsg();
      $(this).parents('.lab-group').find('input').prop("checked", false);
      $(this).siblings('input').prop("checked", true);
      $(this).parents('.lab-group').find('label.monitoringSelect').removeClass("activeIcon");
      $(this).addClass('activeIcon');
      $("label.error").hide();//隐藏validate验证错误信息
      sweepCodeMonitorType = $(this).siblings('input').val();
      //切换监控对象类型时清空之前的信息
      $("#monitorTheObject").val("");
      $("#scanSim").val("");
      $("#terminal").val("");
      //光标移到终端手机号信息框中
      $("#scanSim").focus();
    },
  };
  return {
    sweepCodeEntry: sweepCodeEntry,
  }
})
