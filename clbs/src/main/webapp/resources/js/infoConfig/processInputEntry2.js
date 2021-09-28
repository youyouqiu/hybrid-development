(function (window, $) {

  require.config({
    // baseUrl: "/clbs/resources/js/infoConfig/process",
    paths: {
      "processInput": "process/processInput",
      "revise": "process/revise",
      "processPublicFun": "process/processPublicFun",
      "verification": "process/verification",
    },
  });

  // 加载模块
  require(['processInput', 'revise', 'processPublicFun', 'verification'], function (processInputA, reviseA, processPublicFun, verification) {
    var processInput = processInputA.processInput;
    var objType = processInput.getObjType();
    var revise = reviseA.revise;

    $(function () {
      processInput.agreementType();
      processInput.getVehicleCategory();
      //获取车辆类型
      var curId = $("#category").val();
      processInput.getVehicleType(curId);
      $("#category").on("change", function () {
        var curId = $(this).val();
        processInput.getVehicleType(curId);
      });
      renderPlateColorSelect();
      //默认展示车相关联的信息
      processInput.showType(objType);
      $('input').inputClear().on('onClearEvent', function (e, data) {
        var curInput = $(e.target);
        var id = data.id;
        curInput.attr('value', '');
        if (curInput.hasClass('zTreeCommon')) {
          curInput.siblings('label.error').hide();
          curInput.siblings('.groupPid').val('');
          search_ztree('ztreeDemo1', id, 'assignment', '');
        }
        if (curInput.hasClass('processGroup')) {
          curInput.siblings('label.error').hide();
          curInput.siblings('.processGroupId').val('0');
          search_ztree('ztreeDemo', id, 'group', '');
        }
        if (id == 'devices') {
          $('.i-list-item.active').removeClass('active');
          $("#devDetailBtn").hide();
          $("#terminalBtn").show(); // 手动填写的值，显示新增模块
          $(".equipmentMessage").hide();
          $("#deviceVal").attr("value", "");
          processInput.setFlag2(false);
        }
      })
      $("#charBtn").on("click", processInput.charBtn);
      $("#backSelect").on("click", processInput.backSelect);
      $("#terminalBtn").on("click", processInput.terminalBtn);
      $("#backSelect2").on("click", processInput.backSelect2);
      $("#peopleBtn").on("click", function () {
        $(".peopleNew").show()
      });
      $("#thingsBtn").on("click", function () {
        $(".thingsNew").show()
      });
      $(".optionalListMessage").on("click", function () {
        $(this).parent().parent().next().toggle()
      });
      $("#grouping-add-btn").on("click", processInput.groupingAdd);
      $("#people-add-btn").on("click", processInput.peopleAdd);
      $("#SIMBtn").on("click", processInput.SIMBtn);
      $("#backSelect3").on("click", processInput.backSelect3);
      $("#addRight").on("click", processInput.addRight);
      $("#addLeft").on("click", processInput.addLeft);
      $("#numberBtn").on("click", processInput.numberBtn);
      //车辆验证上一步、下一步和取消
      $(".nextBtnBrand").on("click", revise.nextBtnBrand);
      //人员验证上一步、下一步和取消
      $(".nextBtnPeople").on("click", revise.nextBtnPeople);
      //物品验证上一步、下一步和取消
      $(".nextBtnThing").on("click", revise.nextBtnThing);
      //组织验证上一步、下一步和取消
      $(".nextBtnGroup").on("click", revise.nextBtnGroup);
      //终端验证上一步、下一步和取消
      $(".nextBtnDevice").on("click", revise.nextBtnDevice);
      //Sim验证上一步、下一步和取消
      $(".nextBtnSim").on("click", revise.nextBtnSim);
      //外设验证
      $(".nextBtnPeripheral").on("click", revise.nextBtnPeripheral);
      //日期验证上一步、下一步和取消
      $(".nextBtnData").on("click", revise.nextBtnData);
      //从业人员
      $(".nextBtnPeoList").on("click", revise.nextBtnPeoList);
      $(".upBtn").on("click", revise.upBtn);
      $("#myWizard ul li").on("click", processInput.wizard);
      $('.completionList').on("click", 'ul li', processInput.completionList);
      //详情
      $("#detailBtn").on("click", function () {
        processInput.detailToggle("charNew", "detailMessage")
      });
      $("#devDetailBtn").on("click", function () {
        processInput.detailToggle("terminalNew", "equipmentMessage")
      });
      $("#SIMDetailBtn").on("click", function () {
        processInput.detailToggle("SIMNew", "SIMMessage")
      });
      $("#gender").change(function () {
        $("#peopleGender").text($("#gender").find("option:selected").text());
      });
      $("#deviceTypeIn").change(function () {
        $("#deviceType").text($("#deviceTypeIn").val());
      });
      $("#deviceFunctionalIn").change(function () {
        $("#functionalType").text($("#deviceFunctionalIn").val());
      });
      $('#terminalManufacturers').on("change", function () {
        var terminalManufacturerName = $(this).find("option:selected").attr("value");
        processInput.getTerminalType(terminalManufacturerName);
      });
      $('#terminalType').on("change", function () {
        var terminalTypeId = $(this).find("option:selected").attr("value");
        $("#terminalTypeId").val(terminalTypeId);
      });
      $("#manuFacturerIn").bind('input oninput', function () {
        $("#manuFacturer").text($("#manuFacturerIn").val());
      });
      $("#operatorIn").change(function () {
        $("#operator").text($("#operatorIn").val());
      });
      $("#simFlowIn").bind('input oninput', function () {
        $("#simFlow").text($("#simFlowIn").val());
      });
      $("#useFlowIn").bind('input oninput', function () {
        $("#useFlow").text($("#useFlowIn").val());
      });
      $("#brands,#devices,#sims").blur(processInput.inputBlur);
      $("#car_groupName,#device_pgroupName,#sim_groupName,#monitoringObjPeopleAdd,#monitoringObjThingAdd").on("click", function () {
        processInput.showMenu(this)
      });
      $("#citySel").on("click", function () {
        processInput.showMenu1(this)
      });
      $("#citySel").siblings(".form-control-feedback").on("click", function () {
        $("#citySel").click()
      });
      $("#submits,#billingDateSubmits").on("click", processInput.submits);
      $("#forewarningCoefficient").bind('input oninput', function () {
        $("#alertsFlow").val((Number($("#forewarningCoefficient")[0].value) / 100 * Number($("#monthThresholdValue")[0].value)).toFixed(2))
        if ($("#alertsFlow").val() == "NaN") {
          $("#alertsFlow").val(0)
        }
      });
      $("#monthThresholdValue").bind('input oninput', function () {
        $("#alertsFlow").val((Number($("#forewarningCoefficient")[0].value) / 100 * Number($("#monthThresholdValue")[0].value)).toFixed(2))
        if ($("#alertsFlow").val() == "NaN") {
          $("#alertsFlow").val(0)
        }
      });
      $(document).bind('keydown', processInput.keydownEvent);
      $('#objectMonitoring').bind('click', processInput.objectMonitoring);
      // 终端新增时，给"是否视频"赋值
      $("#isVideos").val(1); // 默认1
      $("#isVideo_yes").bind("click", function () {
        $("#isVideos").val(1);
      });
      $("#isVideo_no").bind("click", function () {
        $("#isVideos").val(0);
      });
      // 终端手机号新增时，给"是否启用"赋值
      $("#isStart_sim").val(1); // 默认1
      $("#isRadio").bind("click", function () {
        $("input[name='isStart_sim']").val(1);
      });
      $("#noRadio").bind("click", function () {
        $("input[name='isStart_sim']").val(0);
      });
      $("#correctionCoefficient").val("100");
      $("#forewarningCoefficient").val("90");
      $("#monthlyStatement").val("01");
      // 默认值
      $("#monitorType").val("0");
      $(".zTreeCommon").on('input propertychange', function (e) {
        var treeObj = $.fn.zTree.getZTreeObj("ztreeDemo1");
        treeObj.checkAllNodes(false);
        $(this).siblings(".groupPid").val('');
        search_ztree('ztreeDemo1', e.target.id, 'assignment');
      });
      $(".processGroup").on('input propertychange', function (e) {
        var treeObj = $.fn.zTree.getZTreeObj("ztreeDemo");
        treeObj.checkAllNodes(false);
        $(this).siblings('label.error').hide();
        if ($(this).val() != '') {
          $(this).siblings(".processGroupId").val('');
        } else {
          $(this).siblings(".processGroupId").val('0');
        }
        search_ztree('ztreeDemo', e.target.id, 'group');
      });
    })
  })
})(window, $);
