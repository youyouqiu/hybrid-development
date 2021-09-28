// 快速录入/极速录入

define(['processInput'], function(processInputA) {
  var quickMonitorType = 0;//0:选择车,1:选择人,2:选择物
  // 第一次进页面默认查询的数据
  var vehicleInfoList = [];
  var peopleInfoList = [];
  var thingInfoList = [];
  var deviceInfoList = [];//终端信息集合
  var simCardInfoList = [];//终端手机号信息集合
  var speedDeviceInfoList = [];//极速录入终端信息集合
  var ais = [];//还能存入的分组id
  var orgId = "";
  var orgName = "";
  var flag1 = false; // 选择还是录入的车牌号
  var flag2 = true; // 选择还是录入的终端号
  var flag3 = true; // 选择还是录入的终端手机号
  var flag4 = false; // 极速 是否是选择的终端号
  var flag5 = true; // 极速 选择还是录入的监控对象
  var flag6 = true; // 极速 选择还是录入的终端手机号
  var hasFlag = true, hasFlag1 = true; // 是否有该唯一标识
  var quickRefresh = true;//快速录入信息是否刷新
  var fastRefresh = true;//极速录入信息是否刷新
  var processInput = processInputA.processInput;

  var infoFastInput = {
    //初始化文件树
    init: function () {
      infoFastInput.getInfoData();
    },
    getInfoData: function () {
      var urlList = '/clbs/m/infoconfig/infoFastInput/add';
      var parameterList = { "id": "" };
      json_ajax("POST", urlList, "json", true, parameterList, infoFastInput.InitCallback);
    },
    InitCallback: function (data) {
      if (data.success) {
        datas = data.obj;
        vehicleInfoList = datas.vehicleInfoList;
        peopleInfoList = datas.peopleInfoList;
        thingInfoList = datas.thingInfoList;
        deviceInfoList = datas.deviceInfoList;
        simCardInfoList = datas.simCardInfoList;
        speedDeviceInfoList = datas.speedDeviceInfoList;
        orgId = datas.orgId;
        orgName = datas.orgName;
        infoFastInput.getCallbackList();
      } else if (data.msg) {
        layer.msg(data.msg);
      }
      setTimeout(function () {//请求成功关闭加载动画
        layer.closeAll('loading');
      }, 200);
    },
    inputClick: function () {
      var value = $("#speedDevices").val();
      var flag = false;
      $("#searchDevices-id li").each(function () {
        var name = $(this).text();
        if (name.indexOf(value) == -1) {
          $(this).hide();
          $('#searchDevices-id').hide();
        } else {
          flag = true;
          $(this).css('display', 'block');
        }
        if (flag) {
          $('#searchDevices-id').show();
        }
      });
      var width = $("#speedDevices").parent('div').width();
      $('.searchDevices-div ul').css('width', width + 'px');
    },
    menuClick: function () {
      flag4 = true;
      hasFlag = true;
      $('#speedDevices').next('label').hide();
      var device = $(this).data('device');
      var car = $(this).attr('data-car');
      var sim = $(this).attr('data-sim');
      var deviceType = $(this).attr('data-deviceType');
      var manufacturerId = $(this).attr('data-manufacturerId');
      var deviceModelNumber = $(this).attr('data-deviceModelNumber');
      var provinceId = $(this).attr('data-provinceId');
      var cityId = $(this).attr('data-cityId');
      var plateColor = $(this).attr('data-plateColor');
      //限制输入
      var protocolTypeArr = ['0', '1', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '23'];
      if (protocolTypeArr.indexOf(deviceType) !== -1) {
        //设置sim不可修改
        $("#speedSims").prop("disabled", true).css({
          'cursor': 'not-allowed',
          'background': 'rgb(238, 238, 238)'
        });
        $('#sim_searchDevice').removeAttr("disabled");
        // $("#speedSims").unbind();//$("#speedSims").prop("disabled", true).css('cursor', 'not-allowed');
        $("#sim_searchDevice").prop("disabled", true);
        //设置终端号可修改
        $("#oneDevices").prop("disabled", false).css({ 'cursor': 'text', 'background': 'rgb(255, 255, 255)' });
        $("#searchOneDevices").prop("disabled", false);
      } else {
        //设置终端号不可修改
        $("#oneDevices").prop("disabled", true).css({
          'cursor': 'not-allowed',
          'background': 'rgb(238, 238, 238)'
        });
        $("#searchOneDevices").prop("disabled", true);
        //设置sim可修改
        $("#speedSims").removeAttr("disabled").css({ 'cursor': 'text', 'background': 'rgb(255, 255, 255)' });
        // $("#speedSims").bind('click', infoFastInput.searchList);
        $("#sim_searchDevice").prop("disabled", false);
        // infoFastInput.getsiminfoset();
      }
      $('input').inputClear();
      $("#speedDeviceType").val(parseInt(deviceType));
      $("#speedDeviceTypeList").val(deviceType);
      var number = $(this).text();
      var deviceTypename = infoFastInput.commounicationtypedefinite(parseInt(deviceType));
      // 根据选择的注册设备的通讯类型，过滤终端号备选项
      //极速录入终端号
      var filteredValue = window.deviceDataList.value.filter(function (x) {
        return x.type == deviceType;
      });
      $("#oneDevices").bsSuggest("destroy"); // 销毁事件
      $("#oneDevices").bsSuggest({
        indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
        indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
        data: { value: filteredValue },
        effectiveFields: ["name"]
      }).on('onDataRequestSuccess', function (e, result) {
        $('#speedDevices').removeAttr('disabled');
      }).on('onSetSelectValue', function (e, keyword, data) {
        $("#speedDeviceVal").val(keyword.id);
        $("#oneDevicesName").val(keyword.key);
        infoFastInput.hideErrorMsg();
        infoFastInput.checkIsBound("quickDevices", keyword.name); // 校验当前终端编号是否已经被绑定，两个人同时操作的时候可能会出现
      }).on('onUnsetSelectValue', function () {
      }).on('input propertychange', function () {
        $("#oneDevicesName").val($('#oneDevices').val());
      });
      if (infoFastInput.checkIsNull(device)) {
        $("#oneDevices").val(device);
        $("#oneDevicesName").val(device);
      } else {
        $("#oneDevices").val('');
        $("#oneDevicesName").val('');
      }
      $("#messagetype").val(deviceTypename);
      $("#speedDevices").val(number);
      if (infoFastInput.checkIsNull(car)) {
        $("#fastBrands").val(car);
        $("#fastBrandVal").val(car);
      } else {
        $("#fastBrands").val('');
        $("#fastBrandVal").val('');
      }
      if (infoFastInput.checkIsNull(sim)) {
        $("#speedSims").val(sim);
      } else {
        $("#speedSims").val('');
      }
      //制造商id
      if (infoFastInput.checkIsNull(manufacturerId)) {
        $("#manufacturerId").val(manufacturerId);
      } else {
        $("#manufacturerId").val('');
      }
      //终端型号
      if (infoFastInput.checkIsNull(deviceModelNumber)) {
        $("#deviceModelNumber").val(deviceModelNumber);
      } else {
        $("#deviceModelNumber").val('');
      }
      //省市id
      $("#provinceId").val(provinceId);
      //市域id
      $("#cityId").val(cityId);
      //车牌颜色
      $("#fastPlateColor").val(plateColor);
      $("#searchDevices-id").hide();//设置监控对象可修改
      $("#fastBrands").prop("disabled", false).css({ 'cursor': 'text', 'background': 'rgb(255, 255, 255)' });
      $("#speedBrandsBtn").prop("disabled", false);
      $('.seizeAseat').slideUp();
      $('.terminalAreaDetail').slideDown();
    },
    checkIsNull: function (data) {
      if (data !== 'undefined' && data !== 'null' && data !== '') {
        return true;
      } else {
        return false;
      }
    },
    getCallbackList: function () {
      //监控对象
      var dataList = {
        value: []
      };
      if (quickMonitorType == 0) {
        var i = vehicleInfoList.length;
        while (i--) {
          dataList.value.push({
            name: vehicleInfoList[i].brand ? vehicleInfoList[i].brand : '',
            id: vehicleInfoList[i].id,
            type: vehicleInfoList[i].plateColor
          });
        }
      } else if (quickMonitorType == 1) {
        var i = peopleInfoList.length;
        while (i--) {
          dataList.value.push({
            name: peopleInfoList[i].brand ? peopleInfoList[i].brand : '',
            id: peopleInfoList[i].id,
            type: peopleInfoList[i].monitorType
          });
        }
      } else if (quickMonitorType == 2) {
        var i = thingInfoList.length;
        while (i--) {
          dataList.value.push({
            name: thingInfoList[i].brand ? thingInfoList[i].brand : '',
            id: thingInfoList[i].id,
            type: thingInfoList[i].monitorType
          });
        }
      }
      //终端
      window.deviceDataList = { value: [] };
      var j = deviceInfoList.length;
      while (j--) {
        deviceDataList.value.push({
          name: deviceInfoList[j].deviceNumber,
          id: deviceInfoList[j].id,
          type: deviceInfoList[j].deviceType,
        });
      }
      var speedBrandDataList = { value: [] }, s = vehicleInfoList.length;
      while (s--) {
        speedBrandDataList.value.push({
          name: vehicleInfoList[s].brand ? vehicleInfoList[s].brand : '',
          id: vehicleInfoList[s].id,
          type: vehicleInfoList[s].monitorType
        });
      }
      //终端手机号
      var simDataList = { value: [] }, k = simCardInfoList.length;
      while (k--) {
        simDataList.value.push({
          name: simCardInfoList[k].simcardNumber,
          id: simCardInfoList[k].id,
        });
      }
      /**
       * 快速录入(监控对象,终端等信息初始化)
       * */
      if (quickRefresh) {
        $("#quickBrands").bsSuggest("destroy"); // 销毁事件
        $("#quickBrands").bsSuggest({
          indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
          indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
          data: dataList,
          effectiveFields: ["name"]
        }).on('onDataRequestSuccess', function (e, result) {
          $('#quickBrands').removeAttr('disabled');
        }).on('onSetSelectValue', function (e, keyword, data) {
          $("#quickBrandVal").attr("value", keyword.id);
          infoFastInput.checkIsBound("quickBrands", keyword.name); // 校验当前车牌号是否已经被绑定，两个人同时操作的时候可能会出现
          infoFastInput.hideErrorMsg();
          flag1 = true;
          $('#quickPlateColor').val(keyword.type);
          $('#quickPlateColor').prop('disabled', true);
          $("#quickBrands").closest('.form-group').find('.dropdown-menu').hide();
          $("#quickEntryForm .input-group input").attr("style", "background-color:#ffffff !important;");
        }).on('onUnsetSelectValue', function () {
          flag1 = false;
        }).on('input propertychange', function () {
          $('#quickPlateColor').prop('disabled', false);
        });
        infoFastInput.changeDevice();
        $('#quickSimsContainer').dropdown({
          data: simDataList.value,
          pageCount: 50,
          listItemHeight: 31,
          onDataRequestSuccess: function (e, result) {
            $('#quickSims').removeAttr('disabled');
          },
          onSetSelectValue: function (e, keyword, data) {
            $("#quickSimVal").attr("value", keyword.id);
            infoFastInput.hideErrorMsg();
            infoFastInput.checkIsBound("quickSims", keyword.name); // 校验当前终端手机号是否已经被绑定，两个人同时操作的时候可能会出现
            flag3 = true;
            $("#quickSims").closest('.form-group').find('.dropdown-menu').hide();
          },
          onUnsetSelectValue: function () {
            flag3 = false;
          }
        });
      }
      /**
       * 极速录入(监控对象,终端等信息初始化)
       * */
      if (fastRefresh) {
        //初始化未注册设备信息
        infoFastInput.loadData(speedDeviceInfoList);
        $("#fastBrands").bsSuggest("destroy"); // 销毁事件
        $("#fastBrands").bsSuggest({
          indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
          indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
          data: dataList,
          effectiveFields: ["name"]
        }).on('onDataRequestSuccess', function (e, result) {
        }).on('onSetSelectValue', function (e, keyword, data) {
          $("#speedBrandVal").attr("value", keyword.id);
          $("#fastBrandVal").attr("value", keyword.name);
          $('#fastPlateColor').val(keyword.type).prop('disabled', true);
          infoFastInput.checkIsBound("fastBrands", keyword.name); // 校验当前车牌号是否已经被绑定，两个人同时操作的时候可能会出现
          infoFastInput.hideErrorMsg();
          flag5 = true;
          $("#fastBrands").closest('.form-group').find('.dropdown-menu').hide()
        }).on('onUnsetSelectValue', function () {
          flag5 = false;
        }).on('input propertychange', function () {
          $('#fastPlateColor').prop('disabled', false);
        });
        //极速录入终端号
        var deviceType = $("#speedDeviceType").val();
        var _device
        if (deviceType !== null && deviceType.toString().length > 0) {
          _device = deviceDataList.value.filter(function (x) {
            return x.type == deviceType;
          });
        }
        $("#oneDevices").bsSuggest("destroy"); // 销毁事件
        $("#oneDevices").bsSuggest({
          indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
          indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
          data: { value: _device },
          effectiveFields: ["name"]
        }).on('onDataRequestSuccess', function (e, result) {
          $('#speedDevices').removeAttr('disabled');
        }).on('onSetSelectValue', function (e, keyword, data) {
          $("#speedDeviceVal").val(keyword.id);
          $("#oneDevicesName").val(keyword.key);
          infoFastInput.hideErrorMsg();
          infoFastInput.checkIsBound("quickDevices", keyword.name); // 校验当前终端编号是否已经被绑定，两个人同时操作的时候可能会出现
        }).on('onUnsetSelectValue', function () {
        }).on('input propertychange', function () {
          $("#oneDevicesName").val($('#oneDevices').val());
        });
        $('#speedSimsContainer').dropdown({
          data: simDataList.value,
          pageCount: 50,
          listItemHeight: 31,
          onDataRequestSuccess: function (e, result) {
            $('#speedSims').removeAttr('disabled');
          },
          onSetSelectValue: function (e, keyword, data) {
            $("#speedSimVal").attr("value", keyword.id);
            infoFastInput.hideErrorMsg();
            infoFastInput.checkIsBound("speedSims", keyword.name); // 校验当前终端手机号是否已经被绑定，两个人同时操作的时候可能会出现
            flag6 = true;
            $("#speedSims").closest('.form-group').find('.dropdown-menu').hide();
          },
          onUnsetSelectValue: function () {
            flag6 = false;
          }
        });
        $("#fastBrands").prop('disabled', true);
        $("#oneDevices").prop('disabled', true);
        $("#speedSims").prop('disabled', true);
      }
      $('input').inputClear().on('onClearEvent', function (e, data) {
        var id = data.id;
        if (id == 'speedDevices') {
          infoFastInput.setInputDisabled();
        }
        if (id == 'oneDevices') {
          $("#oneDevicesName").val('');
        }
        if (id == 'quickBrands') {
          $("#quickPlateColor").removeAttr('disabled');
        }
        if (id == 'fastBrands') {
          $("#fastPlateColor").removeAttr('disabled');
        }
        setTimeout(function () {
          $('#' + id).focus();
        }, 20);
      });
    },
    // 文本框复制事件处理
    inputOnPaste: function (eleId) {
      if (eleId == "quickBrands") {
        flag1 = false;
        $("#quickBrandVal").attr("value", "");
      }
      if (eleId == "quickDevices") {
        flag2 = false;
        $("#quickDeviceVal").attr("value", "");
      }
      if (eleId == "quickSims") {
        flag3 = false;
        $("#quickSimVal").attr("value", "");
      }
      if (eleId == 'fastBrands') {
        flag5 = false;
        $("#speedBrandVal").attr("value", "");
        $("#fastBrandVal").attr("value", "");
      }
      if (eleId == 'speedSims') {
        flag6 = false;
        $("#speedSimVal").attr("value", "");
      }
    },
    //ajax请求回调函数
    getCallback: function (data) {
      if (data.success) {
        for (var i = 0; i < data.obj.vehicleInfoList.length; i++)
          $("#quickBrands").append("<option value=" + data.obj.vehicleInfoList[i].id + ">" + data.obj.vehicleInfoList[i].brand + "</option>")
        for (var i = 0; i < data.obj.deviceInfoList.length; i++)
          $("#quickDevices").append("<option value=" + data.obj.deviceInfoList[i].id + ">" + data.obj.deviceInfoList[i].deviceNumber + "</option>")
        for (var i = 0; i < data.obj.simcardInfoList.length; i++)
          $("#quickSims").append("<option value=" + data.obj.simcardInfoList[i].id + ">" + data.obj.simcardInfoList[i].simcardNumber + "</option>")
        $(".group_select").css("display", "");
      } else if (data.msg) {
        layer.msg(data.msg);
      }
    },
    //提交事件
    doSubmits: function () {
      var str = "";
      if (processInput.checkIsBound("quickBrands", $("#quickBrands").val())) {
        str += "监控对象[" + $("#quickBrands").val() + "]";
      }
      if (processInput.checkIsBound("quickDevices", $("#quickDevices").val())) {
        if (str != '') str += ',';
        str += "终端号[" + $("#quickDevices").val() + "]";
      }
      if (processInput.checkIsBound("quickSims", $("#quickSims").val())) {
        if (str != '') str += ',';
        str += "终端手机号[" + $("#quickSims").val() + "]";
      }
      if (str.length > 0) {
        // str = str.substr(0, str.length - 1);
        layer.msg("不好意思，你来晚了！【" + str + "】已被别人抢先一步绑定了");
        return;
      }
      if (quickMonitorType == 0) {
        if ((!flag1 && !infoFastInput.check_brand()) || !infoFastInput.checkRightBrand("quickBrands")) {
          return;
        }
      } else if (quickMonitorType == 1) {
        if ((!flag1 && !infoFastInput.check_people_number()) || !infoFastInput.checkRightPeopleNumber()) {
          return;
        }
      }
      else if (quickMonitorType == 2) {
        if ((!flag1 && !infoFastInput.check_thing()) || !infoFastInput.checkRightBrand("quickBrands")) {
          return;
        }
      }
      if (infoFastInput.checkIsBound("quickBrands", $("#quickBrands").val())) {
        return;
      }
      if (!infoFastInput.checkIsEmpty("quickDevices", deviceNumberSelect) || infoFastInput.checkIsBound("quickDevices", $("#quickDevices").val())
          || !infoFastInput.checkRightDevice("quickDevices", deviceNumberError) || (!flag2 && !infoFastInput.check_device())) {
        return;
      }
      if ($("#deviceTypeDiv").css("display") != 'none' && !infoFastInput.check_deviceType()) {
        return;
      }
      if (!infoFastInput.checkIsEmpty("quickSims", simNumberNull) || (!flag3 && !infoFastInput.check_sim('quickSims')) || !infoFastInput.checkRightSim('quickSims')) {
        return;
      }
      if (infoFastInput.checkIsBound("quickSims", $("#quickSims").val())) {
        return;
      }
      if (infoFastInput.validate_addForm1()) {
        if ($('#quickCitySelidVal').val() == '') {
          infoFastInput.showErrorMsg('请选择分组', 'quickGroupId');
          return;
        }
        infoFastInput.hideErrorMsg();
        var groupIds = $('#quickCitySelidVal').val();
        if (!infoFastInput.checkGroupNum(groupIds)) return;
        $('#quickPlateColor').prop('disabled', false);
        $('#quickSubmits').prop("disabled", true);
        $("#quickEntryForm").ajaxSubmit(function (data) {
          var json = eval("(" + data + ")");
          if (json.success) {
            quickRefresh = true;
            fastRefresh = false;
            infoFastInput.getInfoData();// 重新加载监控对象,终端,终端手机号等信息
            infoFastInput.clearQuickInfo();
            addFlag = true;
            myTable.requestData();
          } else if (json.msg) {
            layer.msg(json.msg);
          }
          infoFastInput.refreshToken();
          $('#quickSubmits').removeAttr("disabled");
        });
      }
    },
    // 校验分组下是否还可录入监控对象
    checkGroupNum: function (groupIds) {
      var submitFlag = false;
      $.ajax({
        type: 'POST',
        url: '/clbs/m/infoconfig/infoinput/getAssignmentCount',
        dataType: 'json',
        data: { "id": groupIds.replace(/\;/g, ','), "type": 1 },
        async: false,
        success: function (data) {
          if (data.success) {
            var overLimitAssignmentName = data.obj.overLimitAssignmentName;
            if (overLimitAssignmentName.length === 0) {
              submitFlag = true;
            } else {
              layer.msg("【" + overLimitAssignmentName.join(',') + "】" + assignmentMaxCarNum);
            }
          } else if (data.msg) {
            layer.msg(data.msg);
          }
        },
      });
      return submitFlag;
    },
    refreshToken: function () {
      var url = '/clbs/m/basicinfo/enterprise/brand/generateFormToken';
      json_ajax("POST", url, "json", false, null, function (data) {
        console.log(data.msg);
        $(".avoidRepeatSubmitToken").val(data.msg);
      });
    },
    // 快速录入完成后,清空相应信息
    clearQuickInfo: function () {
      $("#quickBrands").val('').css('backgroundColor', '#fafafa');
      $("#quickBrandVal").val('');
      $("#quickPlateColor").val('2');
      $("#quickDevices").val('').css('backgroundColor', '#fafafa');
      $("#quickDeviceVal").val('');
      $("#quickSims").val('').css('backgroundColor', '#fafafa');
      $("#quickSimVal").val('');
      $("#quickPlateColor").removeAttr('disabled');
    },
    //快速录入验证
    validate_addForm1: function () {
      return $("#quickEntryForm").validate({
        rules: {
          deviceType: {
            required: true
          },
          groupid: {
            required: true
          },
        },
        messages: {
          deviceType: {
            required: deviceDeviceTypeNull
          },
          groupid: {
            required: assignmentNameNull
          },
        }
      }).form();
    },
    //极速录入验证
    validate_addForm2: function () {
      return $("#fastEntryForm").validate({
        rules: {
          deviceType: {
            required: true
          },
          groupid: {
            required: true
          },
        },
        messages: {
          deviceType: {
            required: deviceDeviceTypeNull
          },
          groupid: {
            required: assignmentNameNull
          },
        }
      }).form();
    },
    // 校验终端手机号是否填写规范或者回车时不小心输入了异常字符
    checkRightSim: function (id) {
      var errorMsg3 = simNumberError;
      var reg = /^\d{7,20}$/g;
      return infoFastInput.checkIsLegal(id, reg, null, errorMsg3);
    },
    // 显示错误提示信息
    showErrorMsg: function (msg, inputId) {
      if ($("#error_label").is(":hidden")) {
        $("#error_label").text(msg);
        $("#error_label").insertAfter($("#" + inputId));
        $("#error_label").show();
      } else {
        $("#error_label").is(":hidden");
      }
    },
    //错误提示信息隐藏
    hideErrorMsg: function () {
      $("#error_label").hide();
    },
    // 校验车牌号是否已存在
    checkBrand: function () {
      var tempFlag = true;
      $.ajax({
        type: 'POST',
        url: '/clbs/m/basicinfo/monitoring/vehicle/repetition',
        data: { "brand": $("#quickBrands").val() },
        dataType: 'json',
        async: false,
        success: function (data) {
          if (!data) {
            infoFastInput.showErrorMsg(vehicleBrandExists, "quickBrands");
            tempFlag = false;
          } else {
            infoFastInput.hideErrorMsg();
            tempFlag = true;
          }
        },
        error: function () {
          layer.msg("车牌号校验异常！");
          tempFlag = false;
        }
      });
      return tempFlag;
    },
    // 校验人员编号是否已存在
    checkPeopleNumber: function () {
      var tempFlag = true;
      $.ajax({
        type: 'POST',
        url: '/clbs/m/basicinfo/monitoring/personnel/repetitionAdd',
        data: { "peopleNumber": $("#quickBrands").val() },
        dataType: 'json',
        async: false,
        success: function (data) {
          if (!data) {
            infoFastInput.showErrorMsg(personnelNumberExists, "quickBrands");
            tempFlag = false;
          } else {
            infoFastInput.hideErrorMsg();
            tempFlag = true;
          }
        },
        error: function () {
          layer.msg("人员编号校验异常！", {
            time: 1500,
          });
          tempFlag = false;
        }
      });
      return tempFlag;
    },
    // 校验物品是否已存在
    checkThing: function () {
      var tempFlag = true;
      $.ajax({
        type: 'POST',
        url: '/clbs/m/basicinfo/monitoring/ThingInfo/checkThingNumberSole',
        data: { "thingNumber": $("#quickBrands").val() },
        dataType: 'json',
        async: false,
        success: function (data) {
          if (!data) {
            infoFastInput.showErrorMsg(thingExists, "quickBrands");
            tempFlag = false;
          } else {
            infoFastInput.hideErrorMsg();
            tempFlag = true;
          }
        },
        error: function () {
          layer.msg("物品编号校验异常！");
          tempFlag = false;
        }
      });
      return tempFlag;
    },
    // 校验终端编号是否已存在
    checkDevice: function () {
      var tempFlag = true;
      $.ajax({
        type: 'POST',
        url: '/clbs/m/basicinfo/equipment/device/repetition',
        data: { "deviceNumber": $("#quickDevices").val() },
        dataType: 'json',
        async: false,
        success: function (data) {
          if (!data) {
            infoFastInput.showErrorMsg(deviceNumberExists, "quickDevices");
            tempFlag = false;
          } else {
            infoFastInput.hideErrorMsg();
            tempFlag = true;
          }
        },
        error: function () {
          layer.msg("终端号校验异常！");
          tempFlag = false;
        }
      });
      return tempFlag;
    },
    // 极速录入校验终端编号是否已存在
    checkJsDevice: function () {
      var tempFlag = true;
      $.ajax({
        type: 'POST',
        url: '/clbs/m/basicinfo/equipment/device/repetition',
        data: { "deviceNumber": $("#oneDevices").val() },
        dataType: 'json',
        async: false,
        success: function (data) {
          if (!data) {
            infoFastInput.showErrorMsg(deviceNumberExists, "oneDevices");
            tempFlag = false;
          } else {
            infoFastInput.hideErrorMsg();
            tempFlag = true;
          }
        },
        error: function () {
          layer.msg("终端号校验异常！");
          tempFlag = false;
        }
      });
      return tempFlag;
    },
    // 校验终端手机号是否已存在
    checkSIM: function (id) {
      var tempFlag = true;
      $.ajax({
        type: 'POST',
        url: '/clbs/m/basicinfo/equipment/simcard/repetition',
        data: { "simcardNumber": $("#" + id).val() },
        dataType: 'json',
        async: false,
        success: function (data) {
          if (!data) {
            infoFastInput.showErrorMsg(simNumberExists, id);
            tempFlag = false;
          } else {
            infoFastInput.hideErrorMsg();
            tempFlag = true;
          }
        },
        error: function () {
          layer.msg("终端手机号校验异常！");
          tempFlag = false;
        }
      });
      return tempFlag;
    },
    blurFun: function (id) {
      infoFastInput.hideErrorMsg();
      var inputVal = $("#" + id).val();
      if (id == "quickBrands" && inputVal != "" && !flag1 && !infoFastInput.check_brand()) {
        return;
      } else {
        infoFastInput.hideErrorMsg();
      }
      if (id == "quickDevices" && inputVal != "" && !flag2 && !infoFastInput.check_device()) {
        return;
      } else {
        infoFastInput.hideErrorMsg();
      }
      if (id == "quickSims" && inputVal != "" && !flag3 && !infoFastInput.check_sim('quickSims')) {
        return;
      } else {
        infoFastInput.hideErrorMsg();
      }
      if (id == "fastBrands" && inputVal != "" && !flag5 && !infoFastInput.check_brand()) {
        return;
      } else {
        infoFastInput.hideErrorMsg();
      }
      if (id == "speedSims" && inputVal != "" && !flag6 && !infoFastInput.check_sim('speedSims')) {
        return;
      } else {
        infoFastInput.hideErrorMsg();
      }
    },
    // 校验车牌号是否填写规范或者回车时不小心输入了异常字符
    checkRightBrand: function (id) {
      // var errorMsg3 = vehicleBrandError;
      // wjk
      var errorMsg3 = '请输入汉字、字母、数字或短横杠，长度2-20位';
      if ($("#" + id).val() == '') {
        errorMsg3 = vehicleBrandNull;
      }
      if (checkBrands(id)) {
        infoFastInput.hideErrorMsg();
        return true;
      } else {
        infoFastInput.showErrorMsg(errorMsg3, id);
        return false;
      }
    },
    // 校验人员编号
    checkRightPeopleNumber: function () {
      var reg = /^[A-Za-z0-9\u4e00-\u9fa5_-]{2,20}$/;
      var errorMsg3 = personnelNumberError;
      return infoFastInput.checkIsLegal("quickBrands", reg, null, errorMsg3);
    },
    // 校验终端是否填写规范或者回车时不小心输入了异常字符
    checkRightDevice: function (id, errorMsg) {
      var reg = /^[A-Za-z0-9]{7,30}$/;
      var errorMsg3 = '请输入字母、数字，长度7~30位';

      if (infoFastInput.checkIsLegal(id, reg, null, errorMsg3)) {
        return true;
      } else {
        return false;
      }
    },
    // 校验车辆信息
    check_brand: function () {
      var elementId = "quickBrands";
      var maxLength = 10;
      // wjk
      var errorMsg1 = '请输入汉字、字母、数字或短横杠，长度2-20位';
      var errorMsg2 = vehicleBrandMaxlength;
      var errorMsg3 = vehicleBrandError;
      var errorMsg4 = vehicleBrandExists;
      if ($("#quickBrands").val() == '') {
        errorMsg1 = vehicleBrandNull;
      }
      if (infoFastInput.checkIsEmpty(elementId, errorMsg1)
          && infoFastInput.checkRightBrand(elementId)
          && infoFastInput.checkBrand()) {
        return true;
      } else {
        return false;
      }
    },
    // 校验人员信息
    check_people_number: function () {
      var elementId = "quickBrands";
      var maxLength = 8;
      var errorMsg1 = '监控对象不能为空';
      var errorMsg2 = publicSize8Length;
      var errorMsg3 = personnelNumberError;
      var errorMsg4 = personnelNumberExists;
      var reg = /^[A-Za-z0-9\u4e00-\u9fa5_-]{2,20}$/;
      if (infoFastInput.checkIsEmpty(elementId, errorMsg1)
          && infoFastInput.checkIsLegal(elementId, reg, null, errorMsg3)
          && infoFastInput.checkPeopleNumber()) {
        return true;
      } else {
        return false;
      }
    },
    // 校验物品信息
    check_thing: function () {
      var elementId = "quickBrands";
      var errorMsg1 = publicMonitorNull;
      if (infoFastInput.checkIsEmpty(elementId, errorMsg1)
          && infoFastInput.checkRightBrand(elementId)
          && infoFastInput.checkThing()) {
        return true;
      } else {
        return false;
      }
    },
    //校验终端信息
    check_device: function () {
      var elementId = "quickDevices";
      var maxLength = 30;
      var errorMsg1 = deviceNumberSelect;
      var errorMsg2 = deviceNumberMaxlength;
      var errorMsg3 = deviceNumberError;
      var errorMsg4 = deviceNumberExists;
      if (infoFastInput.checkIsEmpty(elementId, errorMsg1)
          && infoFastInput.checkLength(elementId, maxLength, errorMsg2)
          && infoFastInput.checkDevice()) {
        return true;
      } else {
        return false;
      }
    },
    // 校验通讯类型
    check_deviceType: function () {
      var elementId = "quickDeviceType";
      var errorMsg1 = deviceDeviceTypeNull;
      if (infoFastInput.checkIsEmpty(elementId, errorMsg1)) {
        infoFastInput.hideErrorMsg();
        return true;
      } else {
        return false;
      }
    },
    //校验终端手机号信息
    check_sim: function (id) {
      var elementId = id;
      var maxLength = 20;
      var errorMsg1 = simNumberNull;
      var errorMsg2 = simNumberMaxlength;
      var errorMsg3 = simNumberError;
      var errorMsg4 = simNumberExists;
      var reg = /^\d{7,20}$/g;
      if (infoFastInput.checkIsEmpty(elementId, errorMsg1)
          && infoFastInput.checkLength(elementId, maxLength, errorMsg2)
          && infoFastInput.checkIsLegal(elementId, reg, null, errorMsg3)
          && infoFastInput.checkSIM(elementId)) {
        return true;
      } else {
        return false;
      }
    },
    // 校验是否为空
    checkIsEmpty: function (elementId, errorMsg) {
      var value = $("#" + elementId).val() == null || !$("#" + elementId).val() ? "" : $("#" + elementId).val().replace(/(^\s*)|(\s*$)/g, "");
      $("#" + elementId).val(value);
      if (value == "") {
        infoFastInput.hideErrorMsg();
        infoFastInput.showErrorMsg(errorMsg, elementId);
        return false;
      } else {
        infoFastInput.hideErrorMsg();
        return true;
      }
    },
    // 校验是否已存在
    checkIsExists: function (attr, elementId, requestUrl, errorMsg) {
      var tempFlag = true;
      $.ajax({
        type: 'POST',
        url: requestUrl,
        data: { "device": $("#" + elementId).val() },
        dataType: 'json',
        success: function (data) {
          if (!data.success) {
            infoFastInput.showErrorMsg(errorMsg, elementId);
            tempFlag = false;
          } else {
            infoFastInput.hideErrorMsg();
            tempFlag = true;
          }
        },
        error: function () {
          infoFastInput.showErrorMsg("校验异常", elementId);
          tempFlag = false;
        }
      });
      return tempFlag;
    },
    // 校验是否已被绑定
    checkIsBound: function (elementId, elementValue) {
      var tempFlag = false;
      var url = "/clbs/m/infoconfig/infoinput/checkIsBound";
      var data = "";
      if (elementId == "quickBrands") {
        data = {
          "monitorType": quickMonitorType,
          "inputId": "quickBrands",
          "inputValue": $("#" + elementId).val()
        }
      } else if (elementId == "quickDevices" || elementId == "oneDevices") {
        data = { "inputId": "quickDevices", "inputValue": $("#" + elementId).val() }
      } else if (elementId == "quickSims") {
        data = { "inputId": "quickSims", "inputValue": $("#" + elementId).val() }
      } else if (elementId == "fastBrands") {
        data = {
          "monitorType": quickMonitorType,
          "inputId": "quickBrands",
          "inputValue": $("#" + elementId).val()
        }
      } else if (elementId == "speedDevices") {
        data = { "inputId": "quickDevices", "inputValue": $("#" + elementId).val() }
      } else if (elementId == "speedSims") {
        data = { "inputId": "quickSims", "inputValue": $("#" + elementId).val() }
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
          infoFastInput.showErrorMsg("校验异常", elementId);
          tempFlag = false;
        }
      });
      return tempFlag;
    },
    // 校验填写数据的合法性
    checkIsLegal: function (elementId, reg, reg1, errorMsg) {
      var value = $("#" + elementId).val();
      if (reg1 != null) {
        if (!reg.test(value) && !reg1.test(value)) {
          infoFastInput.showErrorMsg(errorMsg, elementId);
          return false;
        } else {
          infoFastInput.hideErrorMsg();
          return true;
        }
      } else {
        if (!reg.test(value)) {
          infoFastInput.showErrorMsg(errorMsg, elementId);
          return false;
        } else {
          // wjk
          // 是否全是'-'或'_'
          var regIfAllheng = /^[-]*$/;
          var regIfAllxiahuaxian = /^[_]*$/;

          if (regIfAllheng.test(value) || regIfAllxiahuaxian.test(value)) {
            infoFastInput.showErrorMsg('不能全是横杠或下划线', elementId);
            return false;
          } else {
            infoFastInput.hideErrorMsg();
            return true;
          }
        }
      }
    },
    checkISdhg: function (elementId) {
      var value = $("#" + elementId).val();
      var regIfAllheng = /^[-]*$/;
      if (regIfAllheng.test(value)) {
        infoFastInput.showErrorMsg('不能全是横杠', elementId);
        return false;
      } else {
        infoFastInput.hideErrorMsg();
        return true;
      }
    },
    // 校验长度
    checkLength: function (elementId, maxLength, errorMsg) {
      var value = $("#" + elementId).val();
      if (value.length > parseInt(maxLength)) {
        infoFastInput.showErrorMsg(errorMsg, elementId);
        return false;
      } else {
        infoFastInput.hideErrorMsg();
        return true;
      }
    },
    // 清除错误信息
    clearErrorMsg: function () {
      $("label.error").hide();
    },
    speedDoSubmits: function () {
      var str = "";
      if (processInput.checkIsBound("fastBrands", $("#fastBrands").val())) {
        str += "监控对象[" + $("#fastBrands").val() + "]";
      }
      if (processInput.checkIsBound("oneDevices", $("#oneDevices").val())) {
        if (str != '') str += ',';
        str += "终端号[" + $("#oneDevices").val() + "]";
      }
      if (processInput.checkIsBound("speedSims", $("#speedSims").val())) {
        if (str != '') str += ',';
        str += "终端手机号[" + $("#speedSims").val() + "]";
      }
      if (str.length > 0) {
        // str = str.substr(0, str.length - 1);
        layer.msg("不好意思，你来晚了！【" + str + "】已被别人抢先一步绑定了");
        return;
      }
      if (!flag4) {
        infoFastInput.showErrorMsg(deviceNumberChoose, 'speedDevices');
        return
      }
      if (!infoFastInput.checkIsEmpty("speedDevices", deviceNumberChoose)) {
        return;
      }
      if (!infoFastInput.checkIsEmpty("fastBrands", vehicleBrandSelect) || !infoFastInput.checkRightBrand("fastBrands")) {
        return;
      }
      if (infoFastInput.checkIsBound("fastBrands", $("#fastBrands").val())) {
        return;
      }
      if (!infoFastInput.checkIsEmpty("oneDevices", '请选择或新增终端号') || infoFastInput.checkIsBound("oneDevices", $("#oneDevices").val())
          || !infoFastInput.checkRightDevice("oneDevices", deviceNumberError)) {
        return;
      }
      if (!infoFastInput.checkIsEmpty("speedSims", simNumberNull) || (!flag6 && !infoFastInput.check_sim('speedSims')) || !infoFastInput.checkRightSim('speedSims')) {
        return;
      }
      if (infoFastInput.checkIsBound("speedSims", $("#speedSims").val())) {
        return;
      }
      if (infoFastInput.validate_addForm2()) {
        if ($('#speedCitySelidVal').val() == '') {
          infoFastInput.showErrorMsg('请选择分组', 'speedGroupid');
          return;
        }
        $("#fastBrandVal").val($("#fastBrands").val());
        infoFastInput.hideErrorMsg();
        var groupIds = $('#speedCitySelidVal').val();
        if (!infoFastInput.checkGroupNum(groupIds)) return;
        $('#speedSims').prop('disabled', false);
        $('#fastPlateColor').prop('disabled', false);
        $('#speedSubmits').prop("disabled", true);
        $("#fastEntryForm").ajaxSubmit(function (data) {
          var json = eval("(" + data + ")");
          if (json.success) {
            quickRefresh = false;
            fastRefresh = true;
            infoFastInput.getInfoData();// 重新加载监控对象,终端,终端手机号等信息
            infoFastInput.clearFastInfo();
            infoFastInput.setInputDisabled();
            addFlag = true;
            myTable.requestData();
          } else if (json.msg) {
            layer.msg(json.msg);
          }
          infoFastInput.refreshToken();
          $("#speedSubmits").removeAttr("disabled");
        });
      }
    },
    // 极速录入完成后,清空相应信息
    clearFastInfo: function () {
      $("#speedDevices").val('');
      $("#fastBrands").val('');
      $("#fastBrandVal").val('');
      $("#fastPlateColor").val('2');
      $("#speedBrandVal").val('');
      $("#fastPlateColor").val(2).prop('disabled', true);
      $("#oneDevices").val('');
      $("#oneDevicesName").val('');
      $("#speedDeviceVal").val('');
      $("#manufacturerId").val('');
      $("#deviceModelNumber").val('');
      $("#provinceId").val('');
      $("#cityId").val('');
      $("#speedSims").val('');
      $("#speedSimVal").val('');
      $("#messagetype").val('');
    },
    GetHttpAddress: function (name) {
      var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
      var r = window.location.search.substr(1).match(reg);
      if (r != null) return unescape(r[2]);
      return null;
    },
    changeInpValDel: function () {
      flag4 = false;
      $("#fastBrands,#speedSims").val('');
    },
    //监听浏览器窗口变化
    windowResize: function () {
      var width = $('#quickGroupId').parent().width();
      var speedWidth = $("#speedGroupid").parent().width();
      $("#menuContent").css('width', width + "px");
      $("#speedMenuContent").css('width', speedWidth + "px");
      setTimeout(function () {
        var width = $("#speedDevices").parent('div').width();
        $('.searchDevices-div ul').css('width', width + 'px');
      }, 200);
    },
    toggleLeft: function () {
      setTimeout(function () {
        var width = $("#speedDevices").parent('div').width();
        $('.searchDevices-div ul').css('width', width + 'px');
      }, 500);
    },
    //急速录入终端查询
    speedSearchDevices: function () {
      var topspeedUrl = "/clbs/m/infoconfig/infoinput/topspeedlist";
      json_ajax("POST", topspeedUrl, "json", true, null, infoFastInput.getTopspeedData1);
    },
    getTopspeedData1: function (data) {
      $("#speedDevices").trigger("focus");
      if (data.success) {
        var list = JSON.parse(data.msg).list;
        //替换静态数据
        speedDeviceInfoList = list;
        //加载数据
        infoFastInput.loadData(list);
        //显示下拉框
        $('.searchDevices-div ul').show();
      } else if (data.msg) {
        layer.msg(data.msg);
      }
    },
    //极速录入终端标识信息、sim标识信息加载数据
    loadData: function (list) {
      var width = $("#speedDevices").parent('div').width();
      var html = '';
      for (var i = 0, len = list.length; i < len; i++) {
        if (list[i].status == 1) {
          html += '<li style="background:#dcf5ff;" data-sim="' + list[i].simNumber + '" data-device="' + list[i].deviceId + '" data-car="' + list[i].brand +
              '" data-deviceType="' + list[i].deviceType + '" data-manufacturerId="' + list[i].manufacturerId + '" data-deviceModelNumber="' + list[i].deviceModelNumber +
              '" data-provinceId="' + list[i].provinceId + '" data-cityId="' + list[i].cityId + '" data-plateColor="' + list[i].plateColor + '">' + list[i].uniqueNumber + '</li>';
        } else if (list[i].status == 0) {
          html += '<li style="background:#fff8b0;" data-sim="' + list[i].simNumber + '" data-device="' + list[i].deviceId + '" data-car="' + list[i].brand +
              '" data-deviceType="' + list[i].deviceType + '" data-manufacturerId="' + list[i].manufacturerId + '" data-deviceModelNumber="' + list[i].deviceModelNumber +
              '" data-provinceId="' + list[i].provinceId + '" data-cityId="' + list[i].cityId + '" data-plateColor="' + list[i].plateColor + '">' + list[i].uniqueNumber + '</li>';
        };
      };
      $('.searchDevices-div ul').css('width', width + 'px').html(html);
      $("#searchDevices-id li").unbind("click").on("click", infoFastInput.menuClick);
    },
    documentClick: function (event) {
      if (!(event.target.id == 'speedDevices' || event.target.id == 'searchDevices')) {
        $("#searchDevices-id").hide();
      }
      if (!(event.target.id == 'speedSims' || event.target.id == 'sim_searchDevice')) {
        $("#sim-searchDevices-id").hide();
      }
    },
    searchList: function () {
      flag4 = false;
      infoFastInput.setInputDisabled();
      hasFlag = false;
      var flag = false;
      var value = $("#speedDevices").val();
      $("#searchDevices-id li").each(function () {
        var name = $(this).text();
        if (name.indexOf(value) == -1) {
          $(this).hide();
          $('#searchDevices-id').hide();
        } else {
          $(this).css('display', 'block');
          flag = true;
        }
        if (name == value) {
          //当有用户输入的标识的时候，默认点击该选项，加载相应标识下的数据
          hasFlag = true;
          $(this).click();
        }
      });
      if (flag) {
        $('#searchDevices-id').show();
      }
    },
    //设置极速录入下的选择框是否可用
    setInputDisabled: function () {
      $("#speedSims").val('').prop("disabled", true).css({
        'cursor': 'not-allowed',
        'background': 'rgb(238, 238, 238)'
      });
      // $("#speedSims").unbind();
      $("#sim_searchDevice").prop("disabled", true);
      $("#oneDevices").val('').prop("disabled", true).css({
        'cursor': 'not-allowed',
        'background': 'rgb(238, 238, 238)'
      });
      $("#searchOneDevices").prop("disabled", true);
      $("#fastBrands").val('').prop("disabled", true).css({
        'cursor': 'not-allowed',
        'background': 'rgb(238, 238, 238)'
      });
      $("#speedBrandsBtn").prop("disabled", true);
      $("#messagetype").val('');
    },
    judgehasFlag: function () {
      if (!hasFlag && hasFlag1 && $('#speedDevices').val() != '') {
        layer.msg('请选择已有的未注册设备');
      }
    },
    searchList2: function () {
      var value = $("#speedSims").val();
      var flag = false;
      $("#sim-searchDevices-id li").each(function () {
        var name = $(this).text();
        if (name.indexOf(value) == -1) {
          $(this).hide();
          $('#sim-searchDevices-id').hide();
        } else {
          flag = true;
          $(this).css('display', 'block');
        };
        if (flag) {
          $('#sim-searchDevices-id').show();
        }
      });
      var width = $("#speedSims").parent('div').width();
      $('#sim-searchDevices-id').css('width', width + 'px');
    },
    //车、人、物点击tab切换
    chooseLabClick: function () {
      $("#entryContentBox ul.dropdown-menu").css("display", "none");
      infoFastInput.hideErrorMsg();
      $(this).parents('.form-group').find('input').prop("checked", false);
      $(this).siblings('input').prop("checked", true);
      $(this).parents('.lab-group').find('label.monitoringSelect').removeClass("activeIcon");
      $(this).addClass('activeIcon');
      quickMonitorType = $(this).siblings('input').val();
      var curForm = $(this).closest('form');
      var curFormId = curForm.attr('id');
      if (quickMonitorType == '0') {
        curForm.find('.quickPlateColor').show();
        if (curFormId == 'quickEntryForm') {
          $('.twoGroupRow').prepend($('.oldDevicesBox'));
          $('.threeGroupRow').prepend($('.oldGroupBox'));
        } else {
          $('.threefastGroup').prepend($('.oldsimBox'));
        }
      } else {
        curForm.find('.quickPlateColor').hide();
        if (curFormId == 'quickEntryForm') {
          $('.oneGroupRow').append($('.oldDevicesBox'));
          $('.twoGroupRow').append($('.oldGroupBox'));
        } else {
          $('.twofastGroup').append($('.oldsimBox'));
        }
      }
      $("label.error").hide();//隐藏validate验证错误信息
      $("#quickDevices").val("").attr("style", "background:#FFFFFF");
      $("#quickBrands").val("").attr("style", "background:#FFFFFF");
      $("#deviceTypeDiv").hide(); // 通讯类型选择隐藏
      if (curForm.attr('id') == 'quickEntryForm') {
        quickRefresh = true;
        fastRefresh = false;
      } else {
        quickRefresh = false;
        fastRefresh = true;
      }
      infoFastInput.getCallbackList();
    },
    speedEntryLiClickFn: function () {
      $("#speedDeviceTypeList").removeClass("is-open");
    },
    quickEntryLiClickFn: function () {
      $("#deviceTypeList").removeClass("is-open");
    },
    //根据divecetype确定通讯类型
    commounicationtypedefinite: function (data) {
      return getProtocolName(data);
    },
    //将静态的终端手机号数据放在下拉框
    getsiminfoset: function () {
      //终端手机号
      var simDataList = { value: [] }, k = simCardInfoList.length;
      while (k--) {
        simDataList.value.push({
          name: simCardInfoList[k].simcardNumber,
          id: simCardInfoList[k].id,
        });
      }
      $("#speedSims").bsSuggest("destroy"); // 销毁事件
      $("#speedSims").bsSuggest({
        indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
        indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
        data: simDataList,
        effectiveFields: ["name"]
      }).on('onDataRequestSuccess', function (e, result) {
      }).on('onSetSelectValue', function (e, keyword, data) {
        $("#speedSimVal").attr("value", keyword.id);
        infoFastInput.hideErrorMsg();
        infoFastInput.checkIsBound("speedSims", keyword.name); // 校验当前终端手机号是否已经被绑定，两个人同时操作的时候可能会出现
        flag6 = true;
        $("#speedSims").closest('.form-group').find('.dropdown-menu').hide()
      }).on('onUnsetSelectValue', function () {
        flag6 = false;
      });
    },
    //请求的终端数据放入下拉框
    getterminalinfoset: function () {
      //终端
      var devicedataList = { value: [] }, k = deviceInfoList.length;
      while (k--) {
        devicedataList.value.push({
          name: deviceInfoList[k].deviceNumber,
          id: deviceInfoList[k].id,
        });
      }
      $("#speedDevices").bsSuggest("destroy"); // 销毁事件
      $("#speedDevices").bsSuggest({
        indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
        indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
        data: devicedataList,
        effectiveFields: ["name"]
      }).on('onDataRequestSuccess', function (e, result) {
      }).on('onSetSelectValue', function (e, keyword, data) {
        $("#speedSimVal").attr("value", keyword.id);
        infoFastInput.hideErrorMsg();
        infoFastInput.checkIsBound("speedDevices", keyword.name); // 校验当前终端手机号是否已经被绑定，两个人同时操作的时候可能会出现
        flag6 = true;
        $("#speedDevices").closest('.form-group').find('.dropdown-menu').hide()
      }).on('onUnsetSelectValue', function () {
        flag6 = false;
      });
    },
    //加载通讯类型下拉框
    loadcommuninput: function () {
      $("#deviceTypeList").bsSuggest("destroy"); // 销毁事件
      $("#quickDeviceType").val("1");
      $("#deviceTypeList").val("交通部JT/T808-2013");
      var dataList_input3 = {
        value: [
          { "name": "交通部JT/T808-2019(中位)", "id": "21" },
          { "name": "交通部JT/T808-2019", "id": "11" },
          { "name": "交通部JT/T808-2013", "id": "1" },
          { "name": "交通部JT/T808-2013(川标)", "id": "12" },
          { "name": "交通部JT/T808-2013(冀标)", "id": "13" },
          { "name": "交通部JT/T808-2011(扩展)", "id": "0" },
          { "name": "移为", "id": "2" },
          { "name": "天禾", "id": "3" },
          { "name": "BDTD-SM", "id": "5" },
          { "name": "KKS", "id": "6" },
          { "name": "KKS-EV25", "id": "22" },
          { "name": "BSJ-A5", "id": "8" },
          { "name": "ASO", "id": "9" },
          { "name": "F3超长待机", "id": "10" },
          { "name": "JT/T808-2011(1078报批稿)", "id": "23" },
        ]
      };
      $("#deviceTypeList").bsSuggest({
        indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
        indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
        data: dataList_input3,
        effectiveFields: ["name"]
      }).on('onDataRequestSuccess', function (e, result) {
      }).on('onSetSelectValue', function (e, keyword, data) {
        $("#deviceTypeList").val(keyword.key);
        $("#quickDeviceType").val(keyword.id);
        $("#deviceTypeList").closest('.form-group').find('.dropdown-menu').hide()
      }).on('onUnsetSelectValue', function () {
      });
    },
    // 数组原型链拓展方法
    arrayExpand: function () {
      // 删除数组指定对象
      Array.prototype.remove = function (obj) {
        for (var i = 0; i < this.length; i++) {
          var num = this.indexOf(obj);
          if (num !== -1) {
            this.splice(num, 1);
          }
        }
      };
      // 两个数组的差集
      Array.minus = function (a, b) {
        return a.each(function (o) {
          return b.contains(o) ? null : o
        });
      };
      // 数组功能扩展
      Array.prototype.each = function (fn) {
        fn = fn || Function.K;
        var a = [];
        var args = Array.prototype.slice.call(arguments, 1);
        for (var i = 0, len = this.length; i < len; i++) {
          var res = fn.apply(this, [this[i], i].concat(args));
          if (res != null) a.push(res);
        }
        return a;
      };
      // 数组是否包含指定元素
      Array.prototype.contains = function (suArr) {
        for (var i = 0, len = this.length; i < len; i++) {
          if (this[i] == suArr) {
            return true;
          }
        }
        return false;
      }
    },
    // 快速录入,切换通讯类型,联动改变终端号数据
    changeDevice: function (deviceType) {
      var newDeviceArr = deviceDataList.value;
      var data = deviceDataList.value;
      $('#quickDevicesContainer').dropdown({
        data: newDeviceArr,
        pageCount: 50,
        listItemHeight: 31,
        onDataRequestSuccess: function (e, result) {
          $('#quickDevices').removeAttr('disabled');
        },
        onSetSelectValue: function (e, keyword, data) {
          $("#quickDeviceVal").attr("value", keyword.id);
          infoFastInput.hideErrorMsg();
          infoFastInput.checkIsBound("quickDevices", keyword.name); // 校验当前终端编号是否已经被绑定，两个人同时操作的时候可能会出现
          flag2 = true;
          $("#quickDeviceType").val(keyword.originalItem.type).prop('disabled', true);
          $("#quickDevices").closest('.form-group').find('.dropdown-menu').hide();
        },
        onUnsetSelectValue: function () {
          flag2 = false;
          $("#quickDeviceType").removeAttr('disabled');
        }
      });
    }
  };
  return {
    infoFastInput: infoFastInput,
  }
})
