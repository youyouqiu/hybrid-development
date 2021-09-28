(function (window, $) {
  var lastSpeedRatio = "";
  var editMileMonitorSet = {
    init: function () {
      $("#autoUploadTime").find("option[value='" + $("#autoUploadTime_hidden").val() + "']").attr("selected", true);
      var measurementScheme = $("#measurementScheme").val();
      if (measurementScheme == 1 || measurementScheme == "1") {
        $(".hideDiv").show();
      }
      $("#fluxSensor").attr("data-id", $("#fluxSensor_hidden").val());
      $("#tyreSizeId").attr("data-id", $("#tyreSizeId_hidden").val());
      // 初始化车辆数据
      var sensorList = JSON.parse($("#sensorList").attr("value"));
      //里程传感器
      var fluxSensor_hidden = $("#fluxSensor_hidden").val();
      if (fluxSensor_hidden != '') {
        editMileMonitorSet.querySenorInfo(fluxSensor_hidden);
      }
      if (sensorList != null && sensorList.length > 0) {
        var dataList = {value: []};
        for (var i = 0; i < sensorList.length; i++) {
          var obj = {};
          obj.id = sensorList[i].id;
          obj.name = sensorList[i].sensorType;
          dataList.value.push(obj);
        }
        editMileMonitorSet.setSenorList(dataList);
      }
      $("#autoUploadTime").change(function () {
        var ut = $("#autoUploadTime").val();
        $("#autoUploadTime_hidden").val(ut);
      });
      //轮胎信息
      var tyreSizelist = JSON.parse($("#tyreSizelist").attr("value"));
      // 初始化车辆数据
      var dataList = {value: []};
      if (tyreSizelist != null && tyreSizelist.length > 0) {
        for (var i = 0; i < tyreSizelist.length; i++) {
          var obj = {};
          obj.id = tyreSizelist[i].id;
          obj.name = tyreSizelist[i].sizeName;
          dataList.value.push(obj);
        }
        editMileMonitorSet.setTyreList(dataList);
      }
      // 初始化车辆数据
      var referVehicleList = JSON.parse($("#referVehicleList").attr("value"));
      var dataList = {value: []};
      if (referVehicleList != null && referVehicleList.length > 0) {
        //得到车牌id
        var vehicleId = $("#vehicleId").val();
        for (var i = 0; i < referVehicleList.length; i++) {
          var obj = {};
          //删除相同车牌信息
          if (referVehicleList[i].vehicleId == vehicleId) {
            referVehicleList.splice(referVehicleList[i].vehicleId.indexOf(vehicleId), 1);
          }
          //处理参考车牌只存在一条数据时防止浏览器抛出错误信息
          if (referVehicleList[i] == undefined) {
            dataList.value.push(obj);
          } else {
            obj.id = referVehicleList[i].vehicleId;
            obj.name = referVehicleList[i].plate;
            dataList.value.push(obj);
          }
        }
        $("#brands").bsSuggest({
          indexId: 1,
          indexKey: 0,
          idField: "id",
          keyField: "name",
          effectiveFields: ["name"],
          searchFields: ["name"],
          data: dataList
        }).on('onDataRequestSuccess', function (e, result) {
        }).on('onSetSelectValue', function (e, keyword, data) {
          // 当选择参考里程参数编号
          var id = keyword.id;
          $.ajax({
            type: 'GET',
            url: '/clbs/v/meleMonitor/mileMonitorSet/getInfo_' + id,
            async: false,
            dataType: 'json',
            success: function (data) {
              if (data.msg.length != 0) {
                var data = JSON.parse(data.msg);
                editMileMonitorSet.resetForm();
                $("#measurementScheme").val(data.measuringScheme);
                editMileMonitorSet.rotate();
                $("#brands_hidden").val(keyword.id);
                $("#fluxSensor_hidden").val(data.mileageSensorId);
                $("#fluxSensor").val(data.sensorType);
                $("#fluxSensor").attr("data-id", data.mileageSensorId);
                //设置里程传感器信息
                editMileMonitorSet.querySenorInfo($("#fluxSensor_hidden").val());
                //自动上传时间
                $("#autoUploadTime").find("option[value='" + data.uploadTime + "']").attr("selected", true);
                $("#outputCorrectionK").val(data.outputK);
                $("#outputCorrectionB").val(data.outputB);
                //设置轮胎信息
                if (data.tyreSizeId != '' && data.tyreSizeId != null) {
                  $("#tyreSizeId_hidden").val(data.tyreSizeId);
                  $("#tyreSizeId").val(data.tyreName);
                  $("#tyreSizeId").attr("data-id", data.tyreSizeId);
                  editMileMonitorSet.queryTryeInfo($("#tyreSizeId_hidden").val());
                }
                $("#rollingRadiusCorrection").val(10000);
                $("#igRatio").val(data.igRatio);
                $("#pulseRatio").val(data.pulseRatio);
                $("#correctionFactor").val(data.correctionFactor);
                $("#rollingRadius").val(10000);
                if (data.speedRatio == undefined || data.speedRatio == null ||
                    data.speedRatio == '') {
                  $("#speedRatio").val(10);
                } else {
                  $("#speedRatio").val(data.speedRatio);
                }
              }
            },
            error: function () {
              layer.msg(systemError, {move: false});
            }
          });
        }).on('onUnsetSelectValue', function () {
        });
      }
    },
    resetInitForm: function () {//清楚已设置的参数
      $("#editForm")[0].reset();
    },
    resetForm: function () {//清楚已设置的参数
      editMileMonitorSet.clearSensorInput();
      $("#fluxSensor_hidden").val();
      $("#fluxSensor").val("");
      $("#fluxSensor").attr("data-id", "");
      $("#tyreSizeId_hidden").val("");
      $("#tyreSizeId").val("");
      $("#tyreSizeId").attr("data-id", "");
      $("#tyreRollingRadius").val("");
    },
    queryTryeInfo: function (tryeid) {//根据轮胎规格编号查询轮胎信息
      $.ajax({
        type: 'GET',
        url: '/clbs/v/meleMonitor/tyreSpecification/getInfo_' + tryeid,
        async: false,
        dataType: 'json',
        success: function (data) {
          if (data.msg.length != 0) {
            var m = JSON.parse(data.msg);
            $("#tyreRollingRadius").val(m.rollingRadius);
          }
        },
        error: function () {
          layer.msg(systemError, {move: false});
        }
      });
    },
    clearSensorInput: function () {//清除里程传感器信息
      $("#filterFactor").val("");
      $("#filterFactorStr").val("");
      $("#parityCheck").val("");
      $("#parityCheckStr").val("");
      $("#compEn").val("");
      $("#compEnStr").val("");
      $("#baudRate").val("");
      $("#baudRateStr").val("");
    },
    querySenorInfo: function (mileageSensorId) {//根据编号查询里程传感器信息
      $.ajax({
        type: 'GET',
        url: '/clbs/v/meleMonitor/wheelSpeedSensor/getInfo_' + mileageSensorId,
        async: false,
        dataType: 'json',
        success: function (data) {
          if (data.msg.length != 0) {
            var m = JSON.parse(data.msg);
            $("#filterFactor").val(m.filterFactor);
            $("#filterFactorStr").val(m.filterFactorStr);
            $("#parityCheck").val(m.parityCheck);
            $("#parityCheckStr").val(m.parityCheckStr);
            $("#compEn").val(m.compEn);
            $("#compEnStr").val(m.compEnStr);
            $("#baudRate").val(m.baudRate);
            $("#baudRateStr").val(m.baudRateStr);
          }
        },
        error: function () {
          layer.msg(systemError, {move: false});
        }
      });
    },
    setSenorList: function (dataList) {
      setTimeout(function () {
        $("#fluxSensor").bsSuggest({
          indexId: 1,
          indexKey: 0,
          idField: "id",
          keyField: "name",
          effectiveFields: ["name"],
          searchFields: ["id"],
          data: dataList
        }).on('onDataRequestSuccess', function (e, result) {
        }).on('onSetSelectValue', function (e, keyword, data) {
          var id = keyword.id;
          $("#fluxSensor_hidden").val(id);
          $("#filterFactor").val("");
          $("#filterFactorStr").val("");
          $("#parityCheck").val("");
          $("#parityCheckStr").val("");
          $("#compEn").val("");
          $("#compEnStr").val("");
          $("#baudRate").val("");
          $("#baudRateStr").val("");
          $.ajax({
            type: 'GET',
            url: '/clbs/v/meleMonitor/wheelSpeedSensor/getInfo_' + id,
            async: false,
            dataType: 'json',
            success: function (data) {
              if (data.msg.length != 0) {
                var m = JSON.parse(data.msg);
                $("#filterFactor").val(m.filterFactor);
                $("#filterFactorStr").val(m.filterFactorStr);
                $("#parityCheck").val(m.parityCheck);
                $("#parityCheckStr").val(m.parityCheckStr);
                $("#compEn").val(m.compEn);
                $("#compEnStr").val(m.compEnStr);
                $("#baudRate").val(m.baudRate);
                $("#baudRateStr").val(m.baudRateStr);
              }
            },
            error: function () {
              layer.msg(systemError, {move: false});
            }
          });
          $("#error_label_edit").hide();
          //选择里程传感器型号时清空参考车牌
          editMileMonitorSet.resetReferencePlate();
        }).on('onUnsetSelectValue', function () {
        });
      }, 1000);
    },
    setTyreList: function (dataList) {
      $("#tyreSizeId").bsSuggest({
        indexId: 1,
        indexKey: 0,
        idField: "id",
        keyField: "name",
        effectiveFields: ["name"],
        searchFields: ["name"],
        data: dataList
      }).on('onDataRequestSuccess', function (e, result) {
      }).on('onSetSelectValue', function (e, keyword, data) {
        // 当选择参考里程参数编号
        var id = keyword.id;
        $("#tyreSizeId_hidden").val(id);
        $.ajax({
          type: 'GET',
          url: '/clbs/v/meleMonitor/tyreSpecification/getInfo_' + id,
          async: false,
          dataType: 'json',
          success: function (data) {
            if (data.msg.length != 0) {
              //重置表单
              var m = JSON.parse(data.msg);
              $("#tyreRollingRadius").val(m.rollingRadius);
            }
          },
          error: function () {
            layer.msg(systemError, {move: false});
          }
        });
        //选择轮胎规格时清空参考车牌
        editMileMonitorSet.resetReferencePlate();
      }).on('onUnsetSelectValue', function () {
      });
    },
    doSubmits: function () {
      editMileMonitorSet.hideErrorMsg();
      var fluxSensor = $("#fluxSensor").attr("data-id");
      if (fluxSensor == null || fluxSensor == "") {
        editMileMonitorSet.showErrorMsg(sensorNull, "fluxSensor");
        return;
      }
      $("#fluxSensor_hidden").val(fluxSensor);
      if ($.trim($("#outputCorrectionK").val()) != "") {
        if ($.trim($("#outputCorrectionK").val()) < 1 || $.trim($("#outputCorrectionK").val()) > 200) {
          editMileMonitorSet.showErrorMsg(outputCorrectionK, "outputCorrectionK");
          return;
        }
      }
      if ($.trim($("#outputCorrectionB").val()) != "") {
        if ($.trim($("#outputCorrectionB").val()) < 1 || $.trim($("#outputCorrectionB").val()) > 200) {
          editMileMonitorSet.showErrorMsg(outputCorrectionB, "outputCorrectionB");
          return;
        }
      }
      if ($.trim($("#rollingRadiusCorrection").val()) != "") {
        if ($.trim($("#rollingRadiusCorrection").val()) < 5000 || $.trim($("#rollingRadiusCorrection").val()) > 15000) {
          editMileMonitorSet.showErrorMsg(rollingRadiusScope, "rollingRadiusCorrection");
          return;
        }
      }
      var tyreSizeId = $("#tyreSizeId").attr("data-id");
      if (tyreSizeId == null || tyreSizeId == "") {
        editMileMonitorSet.showErrorMsg(publicNull, "tyreSizeId");
        return;
      }
      // 验证里程测量方案原车脉冲
      if ($("#measurementScheme").val() == 1) {
        if (!(editMileMonitorSet.checkIgRatio())) {
          return;
        }
        else if (!(editMileMonitorSet.checkPulseRatio())) {
          return;
        }
        else if (!(editMileMonitorSet.checkCorrectionFactor())) {
          return;
        }
      } else {
        $("#speedRatio").val("10");
        $("#correctionFactor").val("");
        $("#pulseRatio").val("");
        $("#igRatio").val("");
      }
      var tyreSizeId = $("#tyreSizeId").attr("data-id");
      $("#tyreSizeId_hidden").val(tyreSizeId);
      addHashCode($("#editForm"));
      $("#editForm").ajaxSubmit(function (data) {
        var result = JSON.parse(data);
        if (result.success) {
          $("#commonWin").modal("hide");
          layer.msg(result.msg, {move: false});
          myTable.refresh();
        } else {
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
    //错误提示信息隐藏
    hideErrorMsg: function () {
      $("#error_label_edit").is(":hidden");
      $("#error_label_edit").hide();
    },
    //里程测量方案下拉选状态切换
    rotate: function () {
      var measurementScheme = $("#measurementScheme").val();
      if (measurementScheme === "1") {
        $("#speedRatio").val("");
        $(".hideDiv").show();
      } else {
        $(".hideDiv").hide();
        $("#speedRatio").val(lastSpeedRatio);
      }
    },
    resetReferencePlate: function () {
      $("#brands").val("");
      $("#brands").attr("data-id", "");
    },
    //检查IG后桥比例
    checkIgRatio: function () {
      var igRatio = $("#igRatio").val();
      var reg = /^\d+(?:\.\d{2})?$/;
      if (igRatio !== "" && igRatio !== undefined && reg.test(igRatio)) {
        if (igRatio > 20 || igRatio < 1) {
          editMileMonitorSet.showErrorMsg("请输入IG后桥速比(范围:1.00~20.00)", "igRatio");
          $("#speedRatio").val("");
          return false;
        } else {
          editMileMonitorSet.reckonSpeedRatio();
          editMileMonitorSet.hideErrorMsg();
          return true;
        }
      } else {
        editMileMonitorSet.showErrorMsg("请输入IG后桥速比(范围:1.00~20.00)", "igRatio");
        $("#speedRatio").val("");
        return false;
      }
    },
    //脉冲数比例
    checkPulseRatio: function () {
      var pulseRatio = $("#pulseRatio").val();
      var reg = /^([1-9]|1[0-9]|20)$/;
      if (pulseRatio !== "" && pulseRatio !== undefined && reg.test(pulseRatio)) {
        editMileMonitorSet.reckonSpeedRatio();
        editMileMonitorSet.hideErrorMsg();
        return true;
      } else {
        editMileMonitorSet.showErrorMsg("请输入脉冲数比例(范围:1~20)", "pulseRatio");
        $("#speedRatio").val("");
        return false;
      }
    },
    //修正系数
    checkCorrectionFactor: function () {
      var correctionFactor = $("#correctionFactor").val();
      var reg = /^[0-1]+(.[0-9]{1,2})?$/;
      if (correctionFactor !== "" && correctionFactor !== undefined && reg.test(correctionFactor)) {
        if (correctionFactor > 1.01 || correctionFactor < 0.97) {
          editMileMonitorSet.showErrorMsg("请输入修正系数(范围:0.97~1.01)", "correctionFactor");
          $("#speedRatio").val("");
          return false;
        } else {
          editMileMonitorSet.reckonSpeedRatio();
          editMileMonitorSet.hideErrorMsg();
          return true;
        }
      } else {
        editMileMonitorSet.showErrorMsg("请输入修正系数(范围:0.97~1.01)", "correctionFactor");
        $("#speedRatio").val("");
        return false;
      }
    },
    //计算速比
    reckonSpeedRatio: function () {
      var igRatio = $("#igRatio").val();
      var pulseRatio = $("#pulseRatio").val();
      var correctionFactor = $("#correctionFactor").val();
      if (igRatio !== "" && pulseRatio !== "" && correctionFactor !== "") {
        if (igRatio === 0 && pulseRatio === 0 && correctionFactor === 0) {
          var allValue = igRatio * pulseRatio * correctionFactor;
          $("#speedRatio").val(allValue);
        } else {
          if (igRatio === 0) {
            igRatio = 1;
          }
          if (pulseRatio === 0) {
            pulseRatio = 1;
          }
          if (correctionFactor === 0) {
            correctionFactor = 1;
          }
          var allValue = igRatio * pulseRatio * correctionFactor;
          allValue = editMileMonitorSet.fiterNumber(allValue.toFixed(2))
          $("#speedRatio").val(allValue);
        }
      }
    },
    fiterNumber: function (data) {
      if (data == null || data == undefined || data == "") {
        return data;
      } else {
        var data = data.toString();
        data = parseFloat(data);
        return data;
      }
    },
    sendRatio: function () {
      var igRatio = $('#igRatio').val();
      var pulseRatio = $('#pulseRatio').val();
      var correctionFactor = $('#correctionFactor').val();
      var speedRatio = $('#speedRatio').val();
      igRatio = editMileMonitorSet.fiterNumber(igRatio);
      pulseRatio = editMileMonitorSet.fiterNumber(pulseRatio);
      correctionFactor = editMileMonitorSet.fiterNumber(correctionFactor);
      speedRatio = editMileMonitorSet.fiterNumber(speedRatio);
      $('#igRatio').val(igRatio);
      $('#pulseRatio').val(pulseRatio);
      $('#correctionFactor').val(correctionFactor);
      $('#speedRatio').val(speedRatio);
      setTimeout(function () {
        $("#igRatio").bind("on input", editMileMonitorSet.checkIgRatio);
        $("#correctionFactor").bind("on input", editMileMonitorSet.checkCorrectionFactor);
        $("#pulseRatio").bind("on input", editMileMonitorSet.checkPulseRatio);
      }, 1000);
    }
  };
  $(function () {
    $('input').inputClear().on('onClearEvent', function (e, data) {
        $(this).attr('data-id','');
      if (data.id === "brands") {
        editMileMonitorSet.resetForm();
      }
    });
    $('input[type="text"]').on('blur', function () {
      var $this = $(this);
      $this.siblings('i.delIcon').remove();
    });
    editMileMonitorSet.init();
    editMileMonitorSet.sendRatio();
    lastSpeedRatio = $("#speedRatio").val();
    $("#measurementScheme").change(editMileMonitorSet.rotate);
    $("#doSubmits").bind("click", editMileMonitorSet.doSubmits);
  });
})(window, $);