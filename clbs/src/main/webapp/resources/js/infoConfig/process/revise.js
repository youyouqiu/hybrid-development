// 修改录入/上一步/下一步/取消

define(['processInput' ,'verification'], function(processInputA, verificationA) {
  var processInput = processInputA.processInput
  var objType = processInput.getObjType();
  var flag1 = processInput.getFlag1();
  var flag2 = processInput.getFlag2();
  var flag3 = processInput.getFlag3();
  var verification = verificationA.verification;

  var revise = {
    //车辆验证上一步、下一步和取消
    nextBtnBrand: function () {
      $("#brandNumber").text($("#brands").val());
      $("#people_Number").text($("#brands").val());
      $("#thing_Number").text($("#brands").val());
      processInput.monitoringObjToShow();
      if ($("#addNew").is(":hidden")) {
        if (objType == 0) { // 车
          if ((!flag1 && !processInput.check_brand()) || !processInput.checkRightBrand("brands")
              || processInput.checkIsBound("brands", $("#brands").val()) || !verification.validates()) {
            return;
          } else {
            $("#vehicleType_show").text($("#vehicleType").find("option:selected").text());
          }
        } else if (objType == 1) { // 人
          if ((!flag1 && !processInput.check_people_number()) || !processInput.checkRightPeopleNumber()
              || processInput.checkIsBound("brands", $("#brands").val()) || !verification.validates()) {
            return;
          } else {
            $("#peopleGender").text($("#gender").find("option:selected").text());
          }
        } else if (objType == 2) { // 物
          if ((!flag1 && !processInput.check_thing()) || !processInput.checkRightBrand("brands")
              || processInput.checkIsBound("brands", $("#brands").val()) || !verification.validates()) {
            return;
          } else {
            $("#thing_type_value").text($("#thing_type").text());
            $("#thing_name1").text($("#thing_name").text());
          }
        }
      } else {
        if (objType == 0) {//车
          if ((!flag1 && !processInput.check_brand()) || !processInput.checkRightBrand("brands")
              || processInput.checkIsBound("brands", $("#brands").val()) || !verification.validates()) {
            return;
          } else {
            $("#vehicleType_show").text($("#vehicleType").find("option:selected").text());
            datas = $('#brandVal').val();
            json_ajax("POST", "/clbs/m/infoconfig/infoinput/getVehicleInfoById", "json", true, {"vehicleId": datas}, revise.carCallback);
          }
        } else if (objType == 1) { // 人
          if ((!flag1 && !processInput.check_people_number()) || !processInput.checkRightPeopleNumber()
              || processInput.checkIsBound("brands", $("#brands").val()) || !verification.validates()) {
            return;
          } else {
            $("#peopleGender").text($("#gender").find("option:selected").text());
            datas = $('#brandVal').val();
            json_ajax("POST", "/clbs/m/infoconfig/infoinput/getPeopleInfoById", "json", true, {"peopleId": datas}, revise.peopleCallback);
          }
        } else if (objType == 2) { // 物
          if ((!flag1 && !processInput.check_thing()) || !processInput.checkRightBrand("brands")
              || processInput.checkIsBound("brands", $("#brands").val()) || !verification.validates()) {
            return;
          } else {
            $("#thing_type_value").text($("#thingType").find("option:selected").text());
            $("#thing_name1").text($("#thingName").val());
            datas = $('#brandVal').val();
            json_ajax("POST", "/clbs/m/infoconfig/infoinput/getThingInfoById", "json", true, {"thingId": datas}, revise.thingCallback);
          }
        }
      }
      $(this).parents(".step-pane").removeClass("active").next().addClass("active");
      $(this).parents(".step-content").siblings(".wizard").children(".steps").children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
      $(".complete").children(".badge").attr("class", "badge badge-success");
    },
    carCallback: function (data) {
      if (data != null && data.obj != null && data.obj.vehicleInfo != null) {
        $("#vehicleOwner").text(processInput.converterToBlank(data.obj.vehicleInfo.vehicleOwner));
        $("#vehicleOwnerPhone").text(processInput.converterToBlank(data.obj.vehicleInfo.vehicleOwnerPhone));
        $("#vehicleType_show").text(processInput.converterToBlank(data.obj.vehicleInfo.vehiclet));
        $("#vehicle_province").text(processInput.converterToBlank(data.obj.vehicleInfo.province));
        $("#vehicle_city").text(processInput.converterToBlank(data.obj.vehicleInfo.city));
        $("#vehicle_county").text(processInput.converterToBlank(data.obj.vehicleInfo.county));
      }
    },
    peopleCallback: function (data) {
      if (data != null && data.obj != null && data.obj.peopleInfo != null) {
        $("#peopleIdentity").text(processInput.converterToBlank(data.obj.peopleInfo.identity));
        var checkGender = data.obj.peopleInfo.gender;
        if (checkGender == "1") {
          checkGender = "男";
        } else if (checkGender == "2") {
          checkGender = "女";
        } else {
          return "";
        }
        $("#peopleGender").text(processInput.converterToBlank(checkGender));
        $("#peoplePhone").text(processInput.converterToBlank(data.obj.peopleInfo.phone));
      }
    },
    thingCallback: function (data) {
      if (data != null && data.obj != null && data.obj.thingInfo != null) {
        var thing = data.obj.thingInfo;
        $("#thing_groupName").text(processInput.converterNullToBlank(thing.groupName));
        $("#thing_name").text(processInput.converterNullToBlank(thing.name));
        $("#thing_type").text(processInput.converterNullToBlank(thing.type));
      }
    },
    peoples: function () {
      return $("#addForm2").validate({
        debug: true,
        rules: {
          peoples: {
            required: true,
          },
          name: {
            required: true,
            minlength: 2,
            maxlength: 30
          },
          birthday: {
            required: true,
          },
          identity: {
            required: true,
            isIdCardNo: true
          },
          gender: {
            required: true
          },
          phone: {
            required: true,
            isPhone: true
          },
          email: {
            email: true,
            maxlength: 20
          }
        },
        messages: {
          peoples: {
            required: "不能为空",
          },
          name: {
            required: "不能为空",
            minlength: "至少两个字符",
            maxlength: "最多30个字符"
          },
          birthday: "不能为空",
          identity: {
            required: "不能为空",
            isIdCardNo: "请输入正确的身份证号码！"
          },
          phone: {
            required: "不能为空",
            isPhone: "请输入正确的电话号码！"
          },
          email: {
            email: emailError,
            maxlength: publicSize20
          }
        }
      }).form();
    },
    //人员验证上一步、下一步和取消
    nextBtnPeople: function () {
      if (revise.peoples()) {
        $(this).parents(".step-pane").removeClass("active").next().addClass("active");
        $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
        $(".complete").children(".badge").attr("class", "badge badge-success");
      }
    },
    things: function () {
      return $("#addForm3").validate({
        debug: true,
        rules: {
          things: {
            required: true,
          },
          name: {
            required: true,
          },
          thingNumber: {
            required: true,
            maxlength: 20
          },
          weight: {
            required: true,
            maxlength: 20
          },
          volume: {
            required: true
          }
        },
        messages: {
          things: {
            required: "不能为空"
          },
          name: "不能为空",
          thingNumber: "不能为空",
          weight: "不能为空"
        }
      }).form();
    },
    //物品验证上一步、下一步和取消
    nextBtnThing: function () {
      if (revise.things()) {
        $(this).parents(".step-pane").removeClass("active").next().addClass("active");
        $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
        $(".complete").children(".badge").attr("class", "badge badge-success");
      }
    },
    //组织验证上一步、下一步和取消
    nextBtnGroup: function () {
      var groupids = '';
      var groupnames = '';
      if (verification.validates()) {
        var groupPidList = $('.groupPid');
        for (var i = 0, len = groupPidList.length; i < len; i++) {
          if ($(groupPidList[i]).val() == '') {
            processInput.showErrorMsg('请选择分组', $(groupPidList[i]).siblings('.zTreeCommon').attr('id'));
            return;
          }
        }
        var groups = $("#step2").find("input[type='text']");
        groups.each(function (i) {
          groupids += $(this).attr("value") + ";"
          if ($(this).val() != "") {
            groupnames += $(this).val() + "（" + $(this).siblings(".groupPid").val() + "）" + "<br/>";
          }
        });
        if (groupids.length > 0) groupids = groupids.substr(0, groupids.length - 1);
        $("#citySelidVal").val(groupids);
        $("#comName").html(groupnames);
        $(this).parents(".step-pane").removeClass("active").next().addClass("active");
        $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
        $(".complete").children(".badge").attr("class", "badge badge-success");
      }
    },
    //终端验证上一步、下一步和取消
    nextBtnDevice: function () {
      $("#deviceNumber").text($("#devices").val());
      processInput.terminalToShow();
      if ($("#addNew2").is(":hidden")) {
        if ((!flag2 && !processInput.check_device())
            || !processInput.checkIsEmpty("deviceTypeIn", deviceNumberSelect) || !processInput.checkRightDevice()
            || processInput.checkIsBound("devices", $("#devices").val())
            || !verification.validates()) {
          return;
        }
        $("#deviceType").text($('#devices').val());
        $("#functionalType").text($("#functionalTypeIn").find("option:selected").text());
        $(this).parents(".step-pane").removeClass("active").next().addClass("active");
        $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
        $(".complete").children(".badge").attr("class", "badge badge-success");
      } else {
        if ((!flag2 && !processInput.check_device())
            || !processInput.checkIsEmpty("deviceTypeIn", deviceNumberSelect) || !processInput.checkRightDevice()
            || processInput.checkIsBound("devices", $("#devices").val())
            || !verification.validates()) {
          return;
        }
        $("#deviceType").text($("#deviceTypeIn").find("option:selected").text());
        $("#functionalType").text($("#functionalTypeIn").find("option:selected").text());
        datas = $('#devices').val();
        json_ajax("POST", "/clbs/m/infoconfig/infoinput/getDeviceInfoByDeviceNumber", "json", true, {"deviceNumber": datas}, revise.terminalCallback);
        $(this).parents(".step-pane").removeClass("active").next().addClass("active");
        $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
        $(".complete").children(".badge").attr("class", "badge badge-success");
      }
    },
    terminalCallback: function (data) {
      if (data != null && data.obj != null && data.obj.deviceInfo != null) {
        $("#deviceNumber").text(processInput.converterToBlank(data.obj.deviceInfo.deviceNumber));
        $("#deviceName").text(processInput.converterToBlank(data.obj.deviceInfo.deviceName));
        $("#deviceType").text(processInput.getDeviceTypeValue(processInput.converterToBlank(data.obj.deviceInfo.deviceType)));
        $("#functionalType").text(processInput.getFunctionalTypeValue(processInput.converterToBlank(data.obj.deviceInfo.functionalType)));
        $("#manuFacturer").text(processInput.converterToBlank(data.obj.deviceInfo.manuFacturer));
      }
    },
    //Sim验证上一步、下一步和取消
    nextBtnSim: function () {
      processInput.SIMToShow();
      if ($("#addNew3").is(":hidden")) {
        if ((!flag3 && !processInput.check_sim())
            || !processInput.checkIsEmpty("sims", simNumberNull) || !processInput.checkRightSim()
            || processInput.checkIsBound("sims", $("#sims").val())
            || !verification.validates()) {
          return;
        } else {
          $("#simcardNumber").text($("#sims").val());
          $("#iccid_show").text($("#iccid").text());
          $("#groupName_show").text($("#simcard_groupName").text());
          $("#operator").text($("#simcard_operator").text());
          $(this).parents(".step-pane").removeClass("active").next().addClass("active");
          $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
          $(".complete").children(".badge").attr("class", "badge badge-success");
        }
      } else {
        if ((!flag3 && !processInput.check_sim())
            || !processInput.checkIsEmpty("sims", simNumberNull) || !processInput.checkRightSim()
            || processInput.checkIsBound("sims", $("#sims").val())
            || !verification.validates()) {
          return;
        } else {
          var data_id = $("#sims").attr("data-id");
          if (data_id != undefined && data_id != "") { // 是选择的终端手机号
            $("#simcardNumber").text($("#sims").val());
            $("#iccid_show").text($("#iccid").text());
            $("#groupName_show").text($("#simcard_groupName").text());
            $("#operator").text($("#simcard_operator").text());
          } else { // 新增终端手机号
            $("#simcardNumber").text($("#sims").val());
            $("#iccid_show").text($("#iccid_add").val());
            $("#groupName_show").text($("#sim_groupName").val());
            $("#operator").text($("#operatorIn").find("option:selected").text());
          }
          $(this).parents(".step-pane").removeClass("active").next().addClass("active");
          $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
          $(".complete").children(".badge").attr("class", "badge badge-success");
        }
      }
    },
    //外设验证
    nextBtnPeripheral: function () {
      var peripherals = $("#select2");
      var spans = peripherals.find("span");
      var pValue = "";
      var li = "";
      if (spans != null && spans.length > 0) {
        for (var i = 0; i < spans.length; i++) {
          pValue += peripherals.find("span:eq(" + i + ")").text() + ",";
          li += "<li>" + peripherals.find("span:eq(" + i + ")").text() + "</li>";
        }
      }
      if (pValue.length > 0)
        pValue = pValue.substr(0, pValue.length - 1);
      $("#peripheralsId").val(pValue);
      var ulHtml = $(".object-area-select");
      ulHtml.html(li);
      $(this).parents(".step-pane").removeClass("active").next().addClass("active");
      $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
      $(".complete").children(".badge").attr("class", "badge badge-success");
    },
    //日期验证上一步、下一步和取消
    nextBtnData: function () {
      var timeInterval = $("#timeInterval").val();
      var serveData = timeInterval.split("--");
      var sTime = serveData[0].substr(0, 10);
      var eTime = serveData[1].substr(0, 10);
      $("#billingDate").val(sTime);
      $("#dueDate").val(eTime);
      if (verification.validates()) {
        $("#jiData").text($("#billingDate").val());
        $("#daoData").text($("#dueDate").val());
        if (objType == 0) {
          $(this).parents(".step-pane").removeClass("active").next().addClass("active");
          $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
          $(".complete").children(".badge").attr("class", "badge badge-success");
        } else if (objType == 1 || objType == 2) {
          $(this).parents(".step-pane").removeClass("active");
          $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete")
          $(".complete").children(".badge").attr("class", "badge badge-success");
          $("#allShowList").addClass('active').children('span.badge').attr('class', 'badge badge-info');
          $("#step7").addClass("active");
        }
      }
    },
    //从业人员
    nextBtnPeoList: function () {
      var inputList = $("#step6 input[name='professionals']");
      var peopleList = [];
      if (inputList != null && inputList.length > 0) {
        for (var i = 0; i < inputList.length; i++) {
          if (inputList[i].value != "") {
            peopleList.push(inputList[i].value);
          }
        }
      }
      //判断第一个从业人员是否有值，没有则将其name置空，字段不提交
      if ($("#professionals").val() == '') {
        $("#professionals").attr("name", '');
        $("#professionalsID").attr("name", '');
      }
      var html = "";
      if (peopleList.length > 0 && peopleList != null) {
        for (var i = 0; i < peopleList.length; i++) {
          html += '<div><label class="col-sm-3 col-md-3 control-label conLabLeft">从业人员：</label><div class="col-sm-9 col-md-9 textShow"><span>' + peopleList[i] + '</span></div></div>';
        };
      }
      $("#showPeopleList").html(html);
      $(this).parents(".step-pane").removeClass("active").next().addClass("active");
      $(this).parents(".step-content").siblings(".wizard").children().children(".active").attr("class", "complete").next().addClass("active").children(".badge").attr("class", "badge badge-info");
      $(".complete").children(".badge").attr("class", "badge badge-success");
    },
    commits: function () {
      return $("#addForm8").validate({
        rules: {
          professionals: {
            required: true,
          }
        },
        messages: {
          professionals: {
            required: "不能为空"
          }
        }
      }).form();
    },
    upBtn: function () {
      //点击上一步时将第一个从业人员的name值还原
      $("#professionals").attr("name", 'professionals');
      $("#professionalsID").attr("name", 'professionalsID');
      if (objType == 0) {
        $(this).parents(".step-pane").removeClass("active").prev().addClass("active");
        $(this).parents(".step-content").siblings(".wizard").children(".steps").children(".active").removeClass("active").prev().attr("class", "active").children(".badge").attr("class", "badge badge-info");
        $(".steps").children(".active").next().children(".badge").removeClass("badge-info");
      } else if (objType == 1 || objType == 2) {
        var $this = $(this).parents(".step-content").siblings(".wizard").children(".steps").children(".active");
        if ($this.prev().hasClass('cancelChoose')) {
          $this.removeClass("active").prev().prev().attr("class", "active").children(".badge").attr("class", "badge badge-info");
          $(".steps").children(".active").next().next().children(".badge").removeClass("badge-info");
          $(this).parents(".step-pane").removeClass("active").prev().prev().addClass("active");
        } else {
          $(this).parents(".step-pane").removeClass("active").prev().addClass("active");
          $(this).parents(".step-content").siblings(".wizard").children(".steps").children(".active").removeClass("active").prev().attr("class", "active").children(".badge").attr("class", "badge badge-info");
          $(".steps").children(".active").next().children(".badge").removeClass("badge-info");
        }
      }
    },
  };
  return {
    revise: revise,
  }
})
