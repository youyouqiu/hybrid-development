// 录入表单验证函数

define(['processPublicFun'], function(processPublicFun) {
  var cid = '';
  var brandValB;
  var deviceValB;
  var simValB;
  var groupingNum = 2;
  var people = 2;
  var intervalFlag = true;
  var id = 2;
  var enterFlag = false;
  var flag1 = false; // 选择还是录入的车牌号
  var flag2 = true; // 选择还是录入的终端号
  var flag3 = true; // 选择还是录入的终端手机号
  var datas;
  var objType = 0;
  // 第一次进页面默认查询的数据
  var vehicleInfoList = [];
  var peopleInfoList = [];
  var thingInfoList = [];
  var deviceInfoList = [];
  var deviceInfoListForPeople = [];
  var simCardInfoList = [];
  var professionalsInfoList = [];
  var professionalDataList;
  var orgId = "";
  var orgName = "";
  var terminalManufacturerInfoList = []; // 终端厂商信息
  var processSubmitFlag = true;// 防止表单重复提交
  var initFlag = true;// 第一次进入页面标识
  var agreementType; //协议类型
  var publicFun = processPublicFun.publicFun;

  var processInput = {
    setObjType: function(type) { objType = type },
    getObjType: function() { return objType },
    setFlag1: function (bool) { flag1 = bool },
    getFlag1: function () { return flag1 },
    setFlag2: function (bool) { flag2 = bool },
    getFlag2: function () { return flag2 },
    setFlag3: function (bool) { flag3 = bool },
    getFlag3: function () { return flag3 },
    //初始化
    init: function () {
      processInput.groupTreeInit();
      processInput.assignmenTreeInit();
      $("#brandVal").attr("value", $("#brands").val());
      $("[data-toggle='tooltip']").tooltip();
      //加载日历控件
      laydate.render({elem: '#openCardTime', theme: '#6dcff6'});
      laydate.render({elem: '#endTime', theme: '#6dcff6'});
      laydate.render({elem: '#installDate', theme: '#6dcff6'});
      // 获取车辆用途数据
      processInput.getCategoryData();
      processInput.getInfoData();
    },
    groupTreeInit: function () {
      var setting = {
        async: {
          url: "/clbs/m/basicinfo/enterprise/professionals/tree",
          type: "post",
          enable: true,
          autoParam: ["id"],
          contentType: "application/json",
          dataType: "json",
        },
        view: {
          dblClickExpand: false
        },
        data: {
          simpleData: {
            enable: true
          }
        },
        callback: {
          beforeClick: processInput.beforeClick,
          onClick: processInput.onClick,
        }
      };
      $.fn.zTree.init($("#ztreeDemo"), setting, null);
    },
    assignmenTreeInit: function () {
      var setting1 = {
        async: {
          url: "/clbs/m/infoconfig/infoinput/tree",
          type: "post",
          enable: true,
          autoParam: ["id"],
          contentType: "application/json",
          dataType: "json",
          dataFilter: processInput.ajaxDataFilter
        },
        view: {
          dblClickExpand: false
        },
        data: {
          simpleData: {
            enable: true
          }
        },
        callback: {
          beforeClick: processInput.beforeClick,
          onClick: processInput.onClick1,
        }
      };
      $.fn.zTree.init($("#ztreeDemo1"), setting1, null); // 分组初始化
    },
    // 获取监控对象,终端号,终端手机号等信息
    getInfoData: function () {
      json_ajax("POST", "/clbs/m/infoconfig/infoinput/addlist_", "json", true, {"id": ""}, processInput.InitCallback);
    },
    InitCallback: function (data) {
      if (data.success) {
        datas = data.obj;
        vehicleInfoList = datas.vehicleInfoList;
        peopleInfoList = datas.peopleInfoList;
        thingInfoList = datas.thingInfoList;
        deviceInfoList = datas.deviceInfoList;
        simCardInfoList = datas.simCardInfoList;
        deviceInfoListForPeople = datas.deviceInfoListForPeople;
        professionalsInfoList = datas.professionalsInfoList;
        if (initFlag) {
          orgId = datas.orgId;
          orgName = datas.orgName;
        }
        terminalManufacturerInfoList = datas.terminalManufacturerInfoList;
        processInput.putConfigValue();
      } else if (data.msg) {
        layer.msg(data.msg);
      }
      $("#processEntry label.monitoringSelect").on("click", processInput.chooseLabClick);
      $("#objectMonitoring").removeClass('disabled loading-state-button').find('i').attr("class", 'caret');
    },
    putConfigValue: function () {
      processInput.setDefaultOrgValue(orgId, orgName); // 设置默认的所属企业
      var dataList = {
        value: []
      };
      $("#brands").bsSuggest("destroy"); // 销毁事件
      if (objType == 0) {
        var i = vehicleInfoList.length;
        while (i--) {
          dataList.value.push({
            name: vehicleInfoList[i].brand,
            id: vehicleInfoList[i].id,
            type: vehicleInfoList[i].monitorType,
          });
        }
      } else if (objType == 1) {
        var i = peopleInfoList.length;
        while (i--) {
          dataList.value.push({
            name: peopleInfoList[i].brand,
            id: peopleInfoList[i].id,
            type: peopleInfoList[i].monitorType,
          });
        }
      } else if (objType == 2) {
        var i = thingInfoList.length;
        while (i--) {
          dataList.value.push({
            name: thingInfoList[i].brand,
            id: thingInfoList[i].id,
            type: thingInfoList[i].monitorType,
          });
        }
      }
      $("#brands").bsSuggest({
        indexId: 1, //data.value 的第几个数据，作为input输入框的内容
        indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
        keyField: "name",
        effectiveFields: ["name"],
        searchFields: ["name"],
        data: dataList
      }).on('onDataRequestSuccess', function (e, result) {
        $('#brands').removeAttr('disabled');
      }).on('onSetSelectValue', function (e, keyword, data) {
        $(".charNew").hide(); // 选择的下拉框的值，隐藏新增模块
        $("#charBtn").hide();
        $("#detailBtn").show();
        $("#brandVal").attr("value", keyword.id);
        // 根据选择的车牌号id，查询车辆详情，并在页面显示
        if (objType == 0) {
          processInput.getVehicleInfoDetailById(keyword.id);
        } else if (objType == 1) {
          processInput.getPeopleInfoDetailById(keyword.id);
        } else if (objType == 2) {
          processInput.getThingInfoDetailById(keyword.id);
        }
        processInput.checkIsBound("brands", keyword.name); // 校验当前车牌号是否已经被绑定，两个人同时操作的时候可能会出现
        processInput.hideErrorMsg();
        processInput.setFlag1(true);
      }).on('onUnsetSelectValue', function () {
        $(".charNew").show(); // 手动填写的值，显示新增模块
        $(".detailMessage").hide();
        $("#brandVal").attr("value", "");
        flag1 = false;
      });
      var deviceDataList = {value: []};
      var j = deviceInfoList.length;
      while (j--) {
        deviceDataList.value.push({
          name: deviceInfoList[j].deviceNumber,
          id: deviceInfoList[j].id,
        });
      }
      $('#devicesContainer').dropdown({
        data: deviceDataList.value,
        pageCount: 50,
        listItemHeight: 31,
        onDataRequestSuccess: function (e, result) {},
        onSetSelectValue: function (e, keyword, data) {
          $(".terminalNew").hide(); // 选择的下拉框的值，隐藏新增模块
          $("#terminalBtn").hide();
          $("#devDetailBtn").show();
          $("#deviceVal").attr("value", keyword.id);
          // 根据选择的终端id，查询终端详情，并在页面显示
          processInput.getDeviceInfoDetailById(keyword.id);
          processInput.checkIsBound("devices", keyword.name); // 校验当前终端编号号是否已经被绑定，两个人同时操作的时候可能会出现
          processInput.hideErrorMsg();
          flag2 = true;
        },
        onUnsetSelectValue: function () {
          $(".terminalNew").show(); // 手动填写的值，显示新增模块
          $(".equipmentMessage").hide();
          $("#deviceVal").attr("value", "");
          flag2 = false;
        }
      });
      // 终端厂商下拉框、终端型号下拉框值组装
      processInput.terminalManufacturerCallBack(terminalManufacturerInfoList);
      var simDataList = {value: []}, k = simCardInfoList.length;
      while (k--) {
        simDataList.value.push({
          name: simCardInfoList[k].simcardNumber,
          id: simCardInfoList[k].id,
        });
      }
      $('#simsContainer').dropdown({
        data: simDataList.value,
        pageCount: 50,
        listItemHeight: 31,
        onDataRequestSuccess: function (e, result) {},
        onSetSelectValue: function (e, keyword, data) {
          $(".SIMNew").hide(); // 选择的下拉框的值，隐藏新增模块
          $("#SIMBtn").hide();
          $("#SIMDetailBtn").show();
          $("#simVal").attr("value", keyword.id);
          // 根据选择的终端手机号id，查询终端手机号详情，并在页面显示
          processInput.getSimCardInfoDetailById(keyword.id);
          processInput.checkIsBound("sims", keyword.name); // 校验当前终端手机号是否已经被绑定，两个人同时操作的时候可能会出现
          processInput.hideErrorMsg();
          flag3 = true;
        },
        onUnsetSelectValue: function () {
          $(".SIMNew").show(); // 手动填写的值，显示新增模块
          $(".SIMMessage").hide();
          $("#simVal").attr("value", "")
          flag3 = false;
        }
      });
      professionalDataList = {value: []}, l = professionalsInfoList.length;
      while (l--) {
        professionalDataList.value.push({
          name: professionalsInfoList[l].name,
          id: professionalsInfoList[l].id,
        });
      }
      $("#professionals").bsSuggest("destroy"); // 销毁事件
      $("#professionals").bsSuggest({
        indexId: 1, //data.value 的第几个数据，作为input输入框的内容
        indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
        data: professionalDataList,
        effectiveFields: ["name"]
      }).on('onDataRequestSuccess', function (e, result) {
      }).on('onSetSelectValue', function (e, keyword, data) {
        if (processInput.checkDoubleChoosePro(keyword.id)) {// 校验是否重复选择了从业人员
          $("#professionalsidVal").attr("value", keyword.id);
          $(this).siblings("input").val(keyword.id);
          // 根据选择的从业人员id，查询从业人员详情，并在页面显示
          processInput.getProfessionalDetailById(keyword.id, "0", "");
        } else {
          processInput.clearValues(this);
        }
      }).on('onUnsetSelectValue', function () {
        $("#professionalsidVal").attr("value", "");
        $(this).siblings("input").val("");
      });
      $(".peopleListMessage").click(function () {
        $(this).parent().parent().next().toggle();
      });
      processInput.inputValueChange();
    },
    terminalManufacturerCallBack: function (result) {
      var str = "";
      for (var i = 0; i < result.length; i++) {
        if (result[i] == '[f]F3') {
          str += '<option selected="selected" value="' + result[i] + '">' + html2Escape(result[i]) + '</option>'
        } else {
          str += '<option  value="' + result[i] + '">' + html2Escape(result[i]) + '</option>'
        }
      }
      $("#terminalManufacturers").html(str);
      processInput.getTerminalType($("#terminalManufacturers").val());
    },
    getTerminalType: function (name) {
      var url = "/clbs/m/basicinfo/equipment/device/getTerminalTypeByName";
      json_ajax("POST", url, "json", false, {'name': name}, processInput.getTerminalTypeCallback);
    },
    getTerminalTypeCallback: function (data) {
      var vt = 'F3-default';
      var result = data.obj.result;
      var str = "";
      if (result != null && result != '') {
        for (var i = 0; i < result.length; i++) {
          if (vt == result[i].terminalType) {
            str += '<option selected="selected" value="' + result[i].id + '">' + html2Escape(result[i].terminalType) + '</option>'
          } else {
            str += '<option  value="' + result[i].id + '">' + html2Escape(result[i].terminalType) + '</option>'
          }
        }
      }
      $("#terminalType").html(str);
      $("#terminalTypeId").val($("#terminalType").val());
    },
    // 文本框复制事件处理
    inputOnPaste: function (eleId) {
      if (eleId == "brands") {
        $("#brandVal").attr("value", "");
        processInput.setFlag1(false);
      }
      if (eleId == "devices") {
        $("#deviceVal").attr("value", "");
        processInput.setFlag2(false);
      }
      if (eleId == "sims") {
        $("#simVal").attr("value", "");
        processInput.setFlag3(false);
      }
    },
    charBtn: function () {
      $(".detailMessage").hide();
      $(".charNew").toggle();
    },
    backSelect: function () {
      $("#addNew").removeClass("hidden");
      $(".detailMessage").show();
      $("#brandVal").attr("value", brandValB);
      $(".charNew").css("display", "none");
    },
    terminalBtn: function () {
      $(".equipmentMessage").hide();
      $(".terminalNew").toggle();
    },
    backSelect2: function () {
      $(".equipmentMessage").show();
      $("#addNew2").removeClass("hidden");
      $("#deviceVal").attr("value", deviceValB);
      $(".terminalNew").css("display", "none");
    },
    //分组新增
    groupingAdd: function (e) {
      $("#menuContent").appendTo($("#zTreeGroupArea"));
      var message = $("#groupMessage").clone(true);
      message.attr("id", "groupMessage" + groupingNum);
      var html = '<div id="groupingNum' + groupingNum + '" class="form-group added_group"><label class="col-sm-3 col-md-3 control-label"><label class="text-danger">*</label> 分组：</label><div class="has-feedback col-sm-5 col-md-3 fastClear"><div class="searchListSelect has-feedback"><input id="citySel' + groupingNum + '" name="citySel" class="form-control zTreeCommon" autocomplete="off" style="background-color: #fafafa; cursor: pointer;" placeholder="请选择分组" value="" type="text"><input class="groupPid" id="groupPid ' + groupingNum + '" name="groupPid" value="" hidden><span class="fa fa-chevron-down form-control-feedback" style="top: 0; right: 10px!important;cursor:pointer;" aria-hidden="true"></span></div></div><div class="col-md-2 topSpace"><button type="button" class="btn btn-danger grouping_Delete deleteIcon"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button> <button type="button"class="btn btn-primary optionalListMessage detailIcon"><span class="glyphicon glyphicon-eye-open"></span>详情</span></button></div>';
      $("#addGrouping").append(html);
      message.insertAfter($("#" + "groupingNum" + groupingNum));
      // 复制详情时将原先的值清空
      $("#" + "groupMessage" + groupingNum).find("#group_name").html("");
      $("#" + "groupMessage" + groupingNum).find("#group_parent").html("");
      $("#" + "groupMessage" + groupingNum).find("#group_principal").html("");
      $("#" + "groupMessage" + groupingNum).find("#group_phone").html("");
      // 克隆组织时，清除其原先的值
      $(".optionalListMessage").unbind("click").click(function (event) {
        $(this).parent().parent().next().toggle();
        event.stopPropagation();
      });
      $(".zTreeCommon").unbind("click").on("click", function () {
        processInput.showMenu1(this)
      });
      $(".zTreeCommon").siblings(".form-control-feedback").on("click", function () {
        $(this).siblings(".zTreeCommon").click()
      });
      groupingNum++;
      $(".grouping_Delete").unbind("click").click(function () {
        $("#menuContent").appendTo($("#zTreeGroupArea"));
        var id = $(this).parent().parent().attr("id");
        var idNum = id.replace("groupingNum", "");
        var listId = "groupMessage" + idNum;
        $("#" + listId).remove();
        $(this).parent().parent().remove();
      });
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
      })
      $(".zTreeCommon").on('input propertychange', function (e) {
        var treeObj = $.fn.zTree.getZTreeObj("ztreeDemo1");
        treeObj.checkAllNodes(false);
        $(this).siblings(".groupPid").val('');
        search_ztree('ztreeDemo1', e.target.id, 'assignment');
      });
    },
    peopleAdd: function () {
      var obj = $("#workPeople").clone(true);
      var message = $("#peopleMessage").clone(true);
      message.attr("id", "peopleMessage" + people);
      message.attr("id", "peopleMessage" + people).css("margin-bottom", 30 + "px");
      var html = obj.html() + '<div class="peopleMessage-style"><button type="button" class="btn btn-danger people_list_Delete deleteIcon"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button> <button type="button"class="btn btn-primary peopleListMessage detailIcon"><span class="glyphicon glyphicon-eye-open"></span>详情</span></button></div>';
      obj.attr("id", "peopleNum" + people).html(html);
      obj.attr("class", "form-group added_group");
      obj.children().children().children("input").attr("id", "professionals" + people);
      obj.children().children().children("input").attr("value", "");
      obj.children("input").attr("id", "professionalsidVal" + people);
      obj.children("input").attr("value", "");
      obj.appendTo($("#people-add-area"));
      message.insertAfter($("#" + "peopleNum" + people));
      // 清除克隆从业人员详情的值
      processInput.clearCloneProfessionalValue(obj.attr("id"));
      var objInput = obj.children().children().children("input");
      if (objInput.val() == "") {
        objInput.siblings("i").hide();
      }
      $("#professionals" + people).bsSuggest({
        indexId: 1, //data.value 的第几个数据，作为input输入框的内容
        indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
        data: professionalDataList,
        effectiveFields: ["name"]
      }).on('onDataRequestSuccess', function (e, result) {
      }).on('onSetSelectValue', function (e, keyword, data) {
        var nump = people - 1;
        if (processInput.checkDoubleChoosePro(keyword.id)) {// 校验是否重复选择了从业人员
          $("#professionalsidVal" + nump).attr("value", keyword.id);
          $(this).siblings("input").val(keyword.id);
          // 根据选择的从业人员id，查询从业人员详情，并在页面显示
          processInput.getProfessionalDetailById(keyword.id, "1", obj.attr("id"));
        } else {
          processInput.clearValues(this);
        }
      }).on('onUnsetSelectValue', function () {
        $("#professionalsidVal").attr("value", "");
        $(this).siblings("input").val("");
      });
      $(".peopleListMessage").off("click").click(function (event) {
        event.stopPropagation();
        $(this).parent().parent().next().toggle();
      });
      people++;
      $(".people_list_Delete").click(function () {
        var id = $(this).parent().parent().attr("id");
        var idNum = id.replace("peopleNum", "");
        var listId = "peopleMessage" + idNum;
        $("#" + listId).remove();
        $(this).parent().parent().remove();
      });
    },
    //终端手机号
    SIMBtn: function () {
      $(".SIMMessage").hide();
      $(".SIMNew").toggle();
    },
    backSelect3: function () {
      $(".SIMMessage").show();
      $("#addNew3").removeClass("hidden");
      $("#simVal").attr("value", simValB);
      $(".SIMNew").css("display", "none");
    },
    addRight: function () {
      var $options = $("#select1 input:checked");
      $options.parents(".checkbox").appendTo($("#select2"));
      var value = $("#select2 input").length
      if (value == 0) {
        value = "";
      }
      $("#checkNum").attr("value", value);
    },
    addLeft: function () {
      var $options = $("#select2 input:checked");
      $options.parents(".checkbox").appendTo($("#select1"));
      var value = $("#select2 input").length
      if (value == 0) {
        value = "";
      }
      $("#checkNum").attr("value", value);
    },
    numberBtn: function () {
      var obj = $("#number1").clone(true);
      var ht = "编号" + id + ":";
      obj.children("label").html(ht);
      var html = obj.html() + '<button type="button" class="btn btn-danger number_Delete addNumber">删除</button>';
      obj.attr("id", "number" + id).html(html);
      obj.insertBefore($("#addArea"));
      id++;
      $(".number_Delete").click(function () {
        $(this).parent().remove();
      })
    },
    ajaxDataFilter: function (treeId, parentNode, responseData) {
      if (responseData) {
        for (var i = 0; i < responseData.length; i++) {
          responseData[i].open = true;
        }
      }
      return responseData;
    },
    beforeClick: function (treeId, treeNode) {
      var check = (treeNode);
      return check;
    },
    onClick: function (e, treeId, treeNode) {
      var zTree = $.fn.zTree.getZTreeObj("ztreeDemo"),
          nodes = zTree.getSelectedNodes(),
          v = "";
      var t = "";
      nodes.sort(function compare(a, b) {
        return a.id - b.id;
      });
      for (var i = 0, l = nodes.length; i < l; i++) {
        t += nodes[i].name;
        v += nodes[i].uuid + ",";
      }
      if (v.length > 0) v = v.substring(0, v.length - 1);
      var cityObj = $("#" + cid);
      cityObj.attr("value", v);
      cityObj.val(t);
      cityObj.siblings().val(v);
      orgId = v;
      orgName = t;
      processInput.getCurGroupDetail(v, cid, cityObj.attr("id"));
      $("#menuContent").hide();
    },
    // 分组下拉点击事件
    onClick1: function (e, treeId, treeNode) {
      var zTree = $.fn.zTree.getZTreeObj("ztreeDemo1"),
          nodes = zTree.getSelectedNodes(),
          v = "";
      var t = "";
      var pName = "";
      nodes.sort(function compare(a, b) {
        return a.id - b.id;
      });
      var type = "";
      for (var i = 0, l = nodes.length; i < l; i++) {
        if (nodes[i].type == "assignment") { // 选择的是分组，才组装值
          type = nodes[i].type;
          t += nodes[i].name;
          v += nodes[i].id + ",";
          pName += nodes[i].pName;
        }
      }
      if (v.length > 0) v = v.substring(0, v.length - 1);
      // 校验是否重复选择了分组; 校验当前分组下面的车辆数是否已经达到上限
      if (type == "assignment" && processInput.checkDoubleChooseAssignment(v) && processInput.checkMaxVehicleCountOfAssignment(v, t)) { // 点击的是分组，才往下执行
        var cityObj = $("#" + cid);
        cityObj.attr("value", v);
        cityObj.val(t);
        cityObj.siblings('input').val(pName);
        processInput.getCurGroupDetail(v, cid, cityObj.attr("id"));
        $("#menuContent").hide();
        processInput.clearErrorMsg();
      }
    },
    // 校验是否重复选择了分组
    checkDoubleChooseAssignment: function (curValue) {
      var model = $("#groupingArea");
      var added = $("#addGrouping");
      var flag = true;
      if (model != null && model != undefined && model != 'undefined' && model.length > 0) {
        //新增 第一个分组下拉框的value值
        var attr = model.children("div").children("div").children("input:first-child").attr("value");
        //原有获取第一个分组下拉框的value值位置不对,新增或者条件判断
        if (curValue == model.children("div").children("input:first-child").attr("value") || curValue == attr) {
          layer.msg(repeateChooseAssignment, {
            time: 1500,
          });
          flag = false;
        }
      }
      if (added != null && added != undefined && added != 'undefined' && added.length > 0) {
        added.children(".added_group").each(function (i) {
          if (curValue == $(this).children("div").children("div").children("input:first-child").attr("value")) {
            layer.msg(repeateChooseAssignment, {
              time: 1500,
            });
            flag = false;
          }
        });
      }
      return flag;
    },
    // 校验当前分组下的车辆数是否已经达到最大值（最大值目前设定为：100台）
    checkMaxVehicleCountOfAssignment: function (assignmentId, assignmentName) {
      var b = true;
      $.ajax({
        type: 'POST',
        url: '/clbs/m/infoconfig/infoinput/checkMaxVehicleCount',
        data: {"assignmentId": assignmentId, "assignmentName": assignmentName},
        dataType: 'json',
        async: false,
        success: function (data) {
          b = data.obj.success;
          if (!data.obj.success) {
            layer.msg("【" + assignmentName + "】" + assignmentMaxCarNum, {
              time: 1500,
            });
          }
        },
        error: function () {
          layer.msg(systemError, {
            time: 1500,
          });
          b = false;
        }
      });
      return b;
    },
    // 获取当前点击组织的详情
    getCurGroupDetail: function (curId, curInputId, clickInputId) {
      $.ajax({
        type: 'POST',
        url: '/clbs/m/infoconfig/infoinput/getGroupDetail',
        data: {"groupId": curId},
        dataType: 'json',
        success: function (data) {
          //var detailDiv = $("#" + clickInputId).parent().parent().next();
          var detailDiv = $("#" + clickInputId).parents('.added_group').next();
          if (data != null && data.obj != null && data.obj.groupInfo != null) {
            if (clickInputId == "citySel") { // 模板
              $("#group_name").text(publicFun.converterNullToBlank(data.obj.groupInfo.name));
              $("#group_parent").text(publicFun.converterNullToBlank(data.obj.groupInfo.groupName));
              $("#group_principal").text(publicFun.converterNullToBlank(data.obj.groupInfo.contacts));
              $("#group_phone").text(publicFun.converterNullToBlank(data.obj.groupInfo.telephone));
            } else { // 新增
              var spans = detailDiv;
              spans.find("span:eq(0)").text(publicFun.converterNullToBlank(data.obj.groupInfo.name));
              spans.find("span:eq(1)").text(publicFun.converterNullToBlank(data.obj.groupInfo.groupName));
              spans.find("span:eq(2)").text(publicFun.converterNullToBlank(data.obj.groupInfo.contacts));
              spans.find("span:eq(3)").text(publicFun.converterNullToBlank(data.obj.groupInfo.telephone));
            }
          }
        },
        error: function () {
          layer.msg(systemError, {
            time: 1500,
          });
        }
      });
    },
    // 克隆组织时，清除详细信息的值
    clearCloneGroupValue: function (clickInputId) {
      var detailDiv = $("#" + clickInputId).parent().parent().next();
      var spans = detailDiv;
      spans.find("span:eq(0)").text("");
      spans.find("span:eq(1)").text("");
      spans.find("span:eq(2)").text("");
      spans.find("span:eq(3)").text("");
      spans.find("span:eq(4)").text("");
      spans.find("span:eq(5)").text("");
    },
    // 所属企业下拉框
    showMenu: function (e) {
      $("#ztreeDemo").show();
      $("#ztreeDemo1").hide();
      var v_id = e.id;
      cid = v_id;
      if ($("#menuContent").is(":hidden")) {
        search_ztree('ztreeDemo', v_id, 'group', '');
        var width = $(e).parent().width();
        $("#menuContent").css("width", width + "px");
        $(window).resize(function () {
          var width = $(e).parent().width();
          $("#menuContent").css("width", width + "px");
        })
        $("#menuContent").insertAfter($("#" + cid));
        $("#menuContent").show();
      } else {
        $("#menuContent").is(":hidden");
      }
      $("body").bind("mousedown", processInput.onBodyDown);
    },
    // 分组下拉框
    showMenu1: function (e) {
      $("#ztreeDemo").hide();
      $("#ztreeDemo1").show();
      var v_id = e.id;
      cid = v_id;
      if ($("#menuContent").is(":hidden")) {
        search_ztree('ztreeDemo1', v_id, 'assignment', '');
        var width = $(e).parent().width();
        $("#menuContent").css("width", width + "px");
        $(window).resize(function () {
          var width = $(e).parent().width();
          $("#menuContent").css("width", width + "px");
        })
        $("#menuContent").insertAfter($("#" + cid));
        $("#menuContent").show();
      } else {
        $("#menuContent").is(":hidden");
      }
      $("body").bind("mousedown", processInput.onBodyDown);
    },
    hideMenu: function () {
      $("#menuContent").fadeOut("fast");
      $("body").unbind("mousedown", processInput.onBodyDown);
    },
    onBodyDown: function (event) {
      if (!(event.target.id == "menuBtn" || event.target.id == "menuContent" || $(event.target).parents("#menuContent").length > 0)) {
        processInput.hideMenu();
      }
    },
    // 校验分组下是否还可录入监控对象
    checkGroupNum: function (groupIds) {
      var submitFlag = false;
      $.ajax({
        type: 'POST',
        url: '/clbs/m/infoconfig/infoinput/getAssignmentCount',
        dataType: 'json',
        data: {"id": groupIds.replace(/\;/g, ','), "type": 1},
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
    doSubmits: function () {
      if (!processSubmitFlag) return;
      layer.load(2);
      var str = "";
      if (processInput.checkIsBound("brands", $("#brands").val())) {
        str += "监控对象[" + $("#brands").val() + "]";
      }
      if (processInput.checkIsBound("devices", $("#devices").val())) {
        if (str != '') str += ',';
        str += "终端号[" + $("#devices").val() + "]";
      }
      if (processInput.checkIsBound("sims", $("#sims").val())) {
        if (str != '') str += ',';
        str += "终端手机号[" + $("#sims").val() + "]";
      }
      if (str.length > 0) {
        layer.closeAll();
        layer.msg("不好意思，你来晚了！【" + str + "】已被别人抢先一步绑定了");
        return;
      }
      processSubmitFlag = false;
      // 服务期限赋值
      var timeInterval = $("#timeInterval").val();
      var serveData = timeInterval.split("--");
      var sTime = serveData[0].substr(0, 10);
      var eTime = serveData[1].substr(0, 10);
      $("#billingDate").val(sTime);
      $("#dueDate").val(eTime);
      var groupId = $('#citySelidVal').val();
      if (!processInput.checkGroupNum(groupId)) {
        processSubmitFlag = true;
        enterFlag = false;
        addFlag = true;
        initFlag = false;
        setTimeout(function () {
          layer.closeAll();
        }, 2000);
        return;
      }
      // sim卡默认启用
      let simIsStart = $("input[name='isStart_sim']").val();
      if (simIsStart === undefined || simIsStart == null || simIsStart == "") {
        $("input[name='isStart_sim']").val(1)
      }
      $("#addForm1").ajaxSubmit(function (message) {
        layer.closeAll('dialog');
        layer.closeAll('loading');
        var json = eval("(" + message + ")");
        enterFlag = false;
        if (json.success) {
          addFlag = true;
          initFlag = false;
          processInput.getInfoData();
          processInput.clearProcessInfo();
          myTable.requestData();
        } else {
          layer.msg(json.msg);
        }
        processSubmitFlag = true;
      });
    },
    // 流程录入完成后,清空相应信息
    clearProcessInfo: function () {
      $("#processEntry label.monitoringSelect").eq(0).click();
      $('#myWizard li').removeAttr('class');
      $('#myWizard span.badge').removeClass('badge-success badge-info');
      $('#myWizard li').eq(0).addClass('active');
      $('#myWizard span.badge').eq(0).addClass('badge-info');
      $('#addForm1 .step-pane').removeClass('active').eq(0).addClass('active');
      $('#brands').val('').css('backgroundColor', '#fff');
      $('#brandVal').val('');
      $('#processPlateColor').val('2');
      $('#charBtn').show();
      $('#detailBtn').hide();
      $('.detailMessage').hide();
      $('.equipmentMessage').hide();
      $('.SIMMessage').hide();
      $('#charNew').hide();
      $('#charNew input[name!="car_groupName"]').val('');
      $('#groupMessage').hide();
      $('#addGrouping').html('');
      $('#devices').val('').css('backgroundColor', '#fff');
      $('#deviceVal').val('');
      $('#terminalBtn').show();
      $('#devDetailBtn').hide();
      $('#installDate').val('');
      $('#sims').val('').css('backgroundColor', '#fff');
      $('#simVal').val('');
      $('#SIMBtn').show();
      $('#SIMDetailBtn').hide();
      $('#SIMNew').hide();
      $('#SIMNew input[name!="sim_groupName"]').val('');
      $('#professionals').val('');
      $('#people-add-area').html('');
      $('#peopleMessage').hide();
    },
    submits: function () {
      var str = "";
      if (processInput.checkIsBound("brands", $("#brands").val())) {
        str += "监控对象[" + $("#brands").val() + "]";
      }
      if (processInput.checkIsBound("devices", $("#devices").val())) {
        if (str != '') str += ',';
        str += "终端号[" + $("#devices").val() + "]";
      }
      if (processInput.checkIsBound("sims", $("#sims").val())) {
        if (str != '') str += ',';
        str += "终端手机号[" + $("#sims").val() + "]";
      }
      if (str.length > 0) {
        // str = str.substr(0, str.length - 1);
        layer.msg("不好意思，你来晚了！【" + str + "】已被别人抢先一步绑定了");
        return;
      }
      layer.confirm(confirmMsg, {btn: [confirmSureBtn, confirmCancleBtn]}, processInput.doSubmits);
    },
    wizard: function () {
      var oldClass = $(this).attr("class");
      if (oldClass == "complete") {
        $(this).attr("class", "active");
        $(this).children("span:first-child").attr("class", "badge badge-info");
        $(this).nextAll().removeClass("active");
        $(this).nextAll().removeClass("complete");
        $(this).nextAll().children("span:first-child").attr("class", "badge");
        var num = $(this).children("span:first-child").text();
        var idNum = "step" + num;
        $("#addForm1 div").removeClass("active");
        $("#" + idNum).addClass("active");
      };
    },
    completionList: function () {
      $("#brandVal").val($(this).text());
      $("#brandVal").attr("value", $(this).attr("value"));
      $("#brands").val($(this).attr("value"));
      $("#queryBrands").addClass("hidden")
    },
    detailToggle: function (property1, property2) {
      $("#" + property1).hide();
      $("." + property2).toggle();
    },
    // 显示错误提示信息
    showErrorMsg: function (msg, inputId) {
      if ($("#error_label").is(":hidden")) {
        $("#error_label").text(msg);
        $("#error_label").insertAfter($("#" + inputId));
        $("#error_label").css('display', 'inline-block');
      } else {
        $("#error_label").is(":hidden");
      }
    },
    hideErrorMsg: function () {
      $("#error_label").hide();
    },
    // 校验车牌号是否已存在
    checkBrand: function () {
      var tempFlag = true;
      $.ajax({
        type: 'POST',
        url: '/clbs/m/basicinfo/monitoring/vehicle/repetition',
        data: {"brand": $("#brands").val()},
        dataType: 'json',
        async: false,
        success: function (data) {
          if (!data) {
            processInput.showErrorMsg(vehicleBrandExists, "brands");
            tempFlag = false;
          } else {
            processInput.hideErrorMsg();
            tempFlag = true;
          }
        },
        error: function () {
          layer.msg("车牌号校验异常！", {
            time: 1500,
          });
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
        data: {"peopleNumber": $("#brands").val()},
        dataType: 'json',
        async: false,
        success: function (data) {
          if (!data) {
            processInput.showErrorMsg(personnelNumberExists, "brands");
            tempFlag = false;
          } else {
            processInput.hideErrorMsg();
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
        data: {"thingNumber": $("#brands").val()},
        dataType: 'json',
        async: false,
        success: function (data) {
          if (!data) {
            processInput.showErrorMsg(thingExists, "brands");
            tempFlag = false;
          } else {
            processInput.hideErrorMsg();
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
        data: {"deviceNumber": $("#devices").val()},
        dataType: 'json',
        async: false,
        success: function (data) {
          if (!data) {
            processInput.showErrorMsg(deviceNumberExists, "devices");
            tempFlag = false;
          } else {
            processInput.hideErrorMsg();
            tempFlag = true;
          }
        },
        error: function () {
          layer.msg("终端号校验异常！", {
            time: 1500,
          });
          tempFlag = false;
        }
      });
      return tempFlag;
    },
    // 校验终端手机号是否已存在
    checkSIM: function () {
      var tempFlag = true;
      $.ajax({
        type: 'POST',
        url: '/clbs/m/basicinfo/equipment/simcard/repetition',
        data: {"simcardNumber": $("#sims").val()},
        dataType: 'json',
        async: false,
        success: function (data) {
          if (!data) {
            processInput.showErrorMsg(simNumberExists, "sims");
            tempFlag = false;
          } else {
            processInput.hideErrorMsg();
            tempFlag = true;
          }
        },
        error: function () {
          layer.msg("终端手机号校验异常！", {
            time: 1500,
          });
          tempFlag = false;
        }
      });
      return tempFlag;
    },
    // 清除从业人员信息
    clearValues: function (element) {
      $("#" + element.id).val("");
      $("#" + element.id).next().val("");
      processInput.clearProDetail(element.id);
    },
    // 清除当前选择框的详细信息
    clearProDetail: function (curId) {
      var detailDivId = $("#" + curId).parent().parent().parent().next().attr("id");
      if (curId == "professionals") {
        detailDivId = "peopleMessage";
      }
      var spans = $("#" + detailDivId);
      spans.find("span:eq(0)").text("");
      spans.find("span:eq(1)").text("");
      spans.find("span:eq(2)").text("");
      spans.find("span:eq(3)").text("");
      spans.find("span:eq(4)").text("");
      spans.find("span:eq(5)").text("");
      spans.find("span:eq(6)").text("");
      spans.find("span:eq(7)").text("");
      spans.find("span:eq(8)").text("");
    },
    // 校验是否重复选择从业人员
    checkDoubleChoosePro: function (curProId) {
      var model = $("#workPeople");
      var added = $("#people-add-area");
      var flag = true;
      if (model != null && model != undefined && model != 'undefined' && model.length > 0) {
        if (curProId == model.children("div").children("div").children("input:first-child").next().attr("value")) {
          layer.msg(repeateChooseProfession, {
            time: 1500,
          });
          flag = false;
        }
      }
      if (added != null && added != undefined && added != 'undefined' && added.length > 0) {
        added.children(".added_group").each(function (i) {
          if (curProId == $(this).children("div").children("div").children("input:first-child").next().attr("value")) {
            layer.msg(repeateChooseProfession, {
              time: 1500,
            });
            flag = false;
          }
        });
      }
      return flag;
    },
    // 设置默认的所属企业值
    setDefaultOrgValue: function (orgId, orgName) {
      $("#car_groupName").val(orgName);
      $("#car_groupId").val(orgId);
      $("#monitoringObjPeopleAdd").val(orgName);
      $("#peo_groupId").val(orgId);
      $("#monitoringObjThingAdd").val(orgName);
      $("#thing_groupId").val(orgId);
      $("#device_pgroupName").val(orgName);
      $("#device_groupId").val(orgId);
      $("#sim_groupName").val(orgName);
      $("#sim_groupId").val(orgId);
    },
    getVehicleInfoDetailById: function (vehicleid) {
      $.ajax({
        type: 'POST',
        url: '/clbs/m/infoconfig/infoinput/getVehicleInfoById',
        data: {
          "vehicleId": vehicleid
        },
        dataType: 'json',
        success: function (data) {
          if (data != null && data.obj != null && data.obj.vehicleInfo != null) {
            $("#vehicle_groupName").text(publicFun.converterNullToBlank(data.obj.vehicleInfo.groupName));
            $("#vehicle_plate_color").text(getPlateColor(data.obj.vehicleInfo.plateColor));
            $("#vehicle_category").text(publicFun.converterNullToBlank(data.obj.vehicleInfo.vehicleCategoryName));
            $("#vehicle_vehicle_type").text(publicFun.converterNullToBlank(data.obj.vehicleInfo.vehiclet));
            $("#vehicle_province").text(publicFun.converterNullToBlank(data.obj.vehicleInfo.province));
            $("#vehicle_city").text(publicFun.converterNullToBlank(data.obj.vehicleInfo.city));
            $("#vehicle_county").text(publicFun.converterNullToBlank(data.obj.vehicleInfo.county));
            $("#vehicle_operating_categories").text(publicFun.converterNullToBlank(data.obj.vehicleInfo.vehiclePurposeName));
          }
        },
        error: function () {
          layer.msg(systemError, {
            time: 1500,
          });
        }
      });
    },
    getPeopleInfoDetailById: function (peopleId) { // 查询人员详情
      $.ajax({
        type: 'POST',
        url: '/clbs/m/basicinfo/monitoring/personnel/getPeopleById',
        data: {
          "id": peopleId
        },
        dataType: 'json',
        success: function (data) {
          if (data != null && data.obj != null) {
            var people = data.obj;
            $("#peo_number").text(publicFun.converterNullToBlank(people.peopleNumber));
            $("#peo_groupName").text(publicFun.converterNullToBlank(people.groupName));
            $("#peo_name").text(publicFun.converterNullToBlank(people.name));
            $("#peo_identity").text(publicFun.converterNullToBlank(people.identity));
            $("#peo_gender").text(publicFun.converterNullToBlank(function () {
              if (people.gender == "1") {
                return "男";
              } else if (people.gender == "2") {
                return "女";
              } else {
                return "";
              }
            }));
            $("#peo_phone").text(publicFun.converterNullToBlank(people.phone));
            $("#peo_email").text(publicFun.converterNullToBlank(people.email));
          }
        },
        error: function () {
          layer.msg(systemError, {
            time: 1500,
          });
        }
      });
    },
    getThingInfoDetailById: function (thingId) { // 查询人员详情
      $.ajax({
        type: 'POST',
        url: '/clbs/m/infoconfig/infoinput/getThingInfoById',
        data: {
          "thingId": thingId
        },
        dataType: 'json',
        success: function (data) {
          if (data != null && data.obj != null) {
            var thing = data.obj.thingInfo;
            $("#thing_groupName").text(publicFun.converterNullToBlank(thing.groupName));
            $("#thing_name").text(publicFun.converterNullToBlank(thing.name));
            $("#thing_category").text(publicFun.converterNullToBlank(thing.categoryName));
            $("#thing_type").text(publicFun.converterNullToBlank(thing.typeName));
            $("#thing_model").text(publicFun.converterNullToBlank(thing.model));
            $("#thing_manufacturer").text(publicFun.converterNullToBlank(thing.manufacture));
          }
        },
        error: function () {
          layer.msg(systemError, {
            time: 1500,
          });
        }
      });
    },
    getDeviceInfoDetailById: function (deviceId) {
      $.ajax({
        type: 'POST',
        url: '/clbs/m/infoconfig/infoinput/getDeviceInfoDetailById',
        data: {
          "deviceId": deviceId
        },
        dataType: 'json',
        success: function (data) {
          if (data != null && data.obj != null && data.obj.deviceInfo != null) {
            $("#device_groupName").text(publicFun.converterNullToBlank(data.obj.deviceInfo.groupName));
            $("#device_device_name").text(publicFun.converterNullToBlank(data.obj.deviceInfo.deviceName));
            $("#device_is_start").text(processInput.getIsStartValue(data.obj.deviceInfo.isStart));
            $("#device_device_type").text(processInput.getDeviceTypeValue(data.obj.deviceInfo.deviceType));
            $("#device_functional_type").text(processInput.getFunctionalTypeValue(data.obj.deviceInfo.functionalType));
            $("#device_manufacturers").text(data.obj.deviceInfo.terminalManufacturer);
            $("#device_type").text(data.obj.deviceInfo.terminalType);
            $("#device_installDate").text(publicFun.converterNullToBlank(data.obj.deviceInfo.installTime));
          }
        },
        error: function () {
          layer.msg(systemError, {
            time: 1500,
          });
        }
      });
    },
    getSimCardInfoDetailById: function (simcardId) {
      $.ajax({
        type: 'POST',
        url: '/clbs/m/infoconfig/infoinput/getSimCardInfoDetailById',
        data: {
          "simcardId": simcardId
        },
        dataType: 'json',
        success: function (data) {
          if (data != null && data.obj != null && data.obj.simcardInfo != null) {
            $("#simcard_groupName").text(publicFun.converterNullToBlank(data.obj.simcardInfo.groupName));
            $("#simcard_is_start").text(processInput.getIsStartValue(data.obj.simcardInfo.isStart));
            $("#iccid").text(publicFun.converterNullToBlank(data.obj.simcardInfo.iCCID));
            $("#imsi").text(publicFun.converterNullToBlank(data.obj.simcardInfo.iMSI));
            $("#imei").text(publicFun.converterNullToBlank(data.obj.simcardInfo.imei));
            $("#simcard_operator").text(publicFun.converterNullToBlank(data.obj.simcardInfo.operator));
            $("#simcard_network_type").text(publicFun.converterNullToBlank(data.obj.simcardInfo.networkType));
            $("#simcard_open_card_time").text(publicFun.converterNullToBlank(data.obj.simcardInfo.openCardTime).length > 10 ? data.obj.simcardInfo.openCardTime.substr(0, 10) : "");
            $("#simcard_end_time").text(publicFun.converterNullToBlank(data.obj.simcardInfo.endTime).length > 10 ? data.obj.simcardInfo.endTime.substr(0, 10) : "");
            $("#simcard_sim_flow").text(publicFun.converterNullToBlank(data.obj.simcardInfo.simFlow));
            $('#realSim').text(data.obj.simcardInfo.realId ? data.obj.simcardInfo.realId : "");
          }
        },
        error: function () {
          layer.msg(systemError, {
            time: 1500,
          });
        }
      });
    },
    getPeopleStatus: function (status) {
      var data = '';
      if (status == '0') {
        data = '正常';
      } else if (status == '1') {
        data = '离职';
      } else {
        data = '停用';
      }
      return data;
    },
    getProfessionalDetailById: function (professionalId, type, ele) {
      $.ajax({
        type: 'POST',
        url: '/clbs/m/infoconfig/infoinput/getProfessionalDetailById',
        data: {"professionalId": professionalId},
        dataType: 'json',
        success: function (data) {
          if (data != null && data.obj != null && data.obj.professionalInfo != null) {
            if (type == "0") { // 模板
              $("#people_name").text(publicFun.converterNullToBlank(data.obj.professionalInfo.name));
              $("#people_gender").text(processInput.getGenderValue(data.obj.professionalInfo.gender));
              $("#people_birthday").text(publicFun.converterNullToBlank(data.obj.professionalInfo.birthday).length > 10 ? data.obj.professionalInfo.birthday.substr(0, 10) : "");
              $("#people_identity").text(publicFun.converterNullToBlank(data.obj.professionalInfo.identity));
              $("#people_phone").text(publicFun.converterNullToBlank(data.obj.professionalInfo.phone));
              $("#people_email").text(publicFun.converterNullToBlank(data.obj.professionalInfo.email));
              $("#people_position_type").text(processInput.getPositionTypeValue(data.obj.professionalInfo.positionType));
              $("#people_job_number").text(publicFun.converterNullToBlank(data.obj.professionalInfo.jobNumber));
              $("#people_card_number").text(publicFun.converterNullToBlank(data.obj.professionalInfo.cardNumber));
              $("#people_status").text(processInput.getPeopleStatus(data.obj.professionalInfo.state));
              $("#people_enterprise").text('');
              $("#people_phone").text(publicFun.converterNullToBlank(data.obj.professionalInfo.phone));
              $("#people_landline").text(publicFun.converterNullToBlank(data.obj.professionalInfo.landline));
            } else { // 后面新增的
              var messageDivId = $("#" + ele).next().attr("id");
              var spans = $("#" + messageDivId);
              spans.find("span:eq(0)").text(publicFun.converterNullToBlank(data.obj.professionalInfo.name));
              spans.find("span:eq(1)").text(processInput.getGenderValue(data.obj.professionalInfo.gender));
              spans.find("span:eq(2)").text(publicFun.converterNullToBlank(data.obj.professionalInfo.birthday).length > 10 ? data.obj.professionalInfo.birthday.substr(0, 10) : "");
              spans.find("span:eq(3)").text(publicFun.converterNullToBlank(data.obj.professionalInfo.identity));
              spans.find("span:eq(4)").text(publicFun.converterNullToBlank(data.obj.professionalInfo.phone));
              spans.find("span:eq(5)").text(publicFun.converterNullToBlank(data.obj.professionalInfo.email));
              spans.find("span:eq(6)").text(processInput.getPositionTypeValue(data.obj.professionalInfo.positionType));
              spans.find("span:eq(7)").text(publicFun.converterNullToBlank(data.obj.professionalInfo.jobNumber));
              spans.find("span:eq(8)").text(publicFun.converterNullToBlank(data.obj.professionalInfo.cardNumber));
            }
          }
        },
        error: function () {
          layer.msg(systemError, {
            time: 1500,
          });
        }
      });
    },
    // 清除克隆从业人员详情的值
    clearCloneProfessionalValue: function (ele) {
      var messageDivId = $("#" + ele).next().attr("id");
      var spans = $("#" + messageDivId);
      spans.find("span:eq(0)").text("");
      spans.find("span:eq(1)").text("");
      spans.find("span:eq(2)").text("");
      spans.find("span:eq(3)").text("");
      spans.find("span:eq(4)").text("");
      spans.find("span:eq(5)").text("");
      spans.find("span:eq(6)").text("");
      spans.find("span:eq(7)").text("");
      spans.find("span:eq(8)").text("");
    },
    // 启停状态
    getIsStartValue: function (isStartIntVal) {
      if (isStartIntVal == 1) {
        return "启用";
      } else if (isStartIntVal == 0) {
        return "停用";
      } else {
        return "启用";
      }
    },
    // 是否视频
    getIsVideoValue: function (isVideoIntVal) {
      if (isVideoIntVal == 1) {
        return "是";
      } else if (isVideoIntVal == 0) {
        return "否";
      } else {
        return "是";
      }
    },
    // 通讯类型
    getDeviceTypeValue: function (deviceTypeIntVal) {
      return getProtocolName(deviceTypeIntVal);
    },
    // 功能类型
    getFunctionalTypeValue: function (functionalTypeIntVal) {
      if (functionalTypeIntVal == 1) {
        return "简易型车机";
      } else if (functionalTypeIntVal == 2) {
        return "行车记录仪";
      } else if (functionalTypeIntVal == 3) {
        return "对讲设备";
      } else if (functionalTypeIntVal == 4) {
        return "手咪设备";
      } else if (functionalTypeIntVal == 5) {
        return "超长待机设备";
      } else if (functionalTypeIntVal == 6) {
        return "定位终端";
      } else {
        return "";
      }
    },
    // 性别
    getGenderValue: function (genderIntVal) {
      if (genderIntVal == 1) {
        return "男";
      } else {
        return "女";
      }
    },
    // 岗位类型
    getPositionTypeValue: function (positionTypeIntVal) {
      var newVal = '';
      var typeQuery = "";
      var url = "/clbs/m/basicinfo/enterprise/professionals/listType";
      var data = {"professionalstype": typeQuery};
      json_ajax("POST", url, "json", false, data, function (data) {
        var result = data.records;
        for (var i = 0; i < result.length; i++) {
          if (result[i].id == positionTypeIntVal) {
            newVal = html2Escape(result[i].professionalstype);
          }
        }
      });
      return newVal;
    },
    // 通道数
    getChannelNumberValue: function (channelNumberIntVal) {
      if (channelNumberIntVal = 1) {
        return "4";
      } else if (channelNumberIntVal = 2) {
        return "5";
      } else if (channelNumberIntVal = 3) {
        return "8";
      } else if (channelNumberIntVal = 4) {
        return "16";
      } else {
        return "";
      }
    },
    // 转换null为空字符串
    converterToBlank: function (value) {
      if (value == null || value == "null" || value == "undefined") {
        return "";
      } else {
        return value + "";
      }
    },
    inputBlur: function () {
      processInput.hideErrorMsg();
      if ($(this).val() != "" && !flag1 && !processInput.check_brand()) {
        return;
      }
    },
    // 校验车辆信息
    check_brand: function () {
      var elementId = "brands";
      // wjk
      var errorMsg1 = '监控对象不能为空';

      if (processInput.checkIsEmpty(elementId, errorMsg1)
          && processInput.checkRightBrand(elementId)
          && processInput.checkBrand()) {
        return true;
      } else {
        return false;
      }
    },
    //不能全是横杠
    checkISdhg: function (elementId) {
      var value = $("#" + elementId).val();
      var regIfAllheng = /^[-]*$/;
      if (regIfAllheng.test(value)) {
        processInput.showErrorMsg('不能全是横杠', elementId);
        return false;
      } else {
        processInput.hideErrorMsg();
        return true;
      }
    },
    // 校验人员信息
    check_people_number: function () {
      var elementId = "brands";
      var maxLength = 8;
      var errorMsg1 = personnelNumberNull;
      var errorMsg2 = publicSize8Length;
      var errorMsg3 = personnelNumberError;
      var errorMsg4 = personnelNumberExists;
      var reg = /^[A-Za-z0-9\u4e00-\u9fa5_-]{2,20}$/;
      if (processInput.checkIsEmpty(elementId, errorMsg1)
          && processInput.checkIsLegal(elementId, reg, null, errorMsg3)
          && processInput.checkPeopleNumber()) {
        return true;
      } else {
        return false;
      }
    },
    // 校验物品信息
    check_thing: function () {
      var elementId = "brands";
      // wjk
      var errorMsg1 = '监控对象不能为空';
      if (processInput.checkIsEmpty(elementId, errorMsg1)
          && processInput.checkRightBrand(elementId)
          && processInput.checkThing()) {
        return true;
      } else {
        return false;
      }
    },
    // 校验车牌号是否填写规范或者回车时不小心输入了异常字符
    checkRightBrand: function (id) {
      // wjk
      var errorMsg3 = '请输入汉字、字母、数字或短横杠，长度2-20位';
      if (checkBrands(id)) {
        processInput.hideErrorMsg();
        return true;
      } else {
        processInput.showErrorMsg(errorMsg3, id);
        return false;
      }
    },
    // 校验人员编号
    checkRightPeopleNumber: function () {
      var reg = /^[A-Za-z0-9\u4e00-\u9fa5_-]{2,20}$/;
      var errorMsg3 = personnelNumberError;
      return processInput.checkIsLegal("brands", reg, null, errorMsg3);
    },
    //校验终端信息
    check_device: function () {
      var elementId = "devices";
      var maxLength = 30;
      var errorMsg1 = deviceNumberSelect;
      var errorMsg2 = '长度不超过30';
      var errorMsg3 = '请输入字母、数字，长度7-30位';
      var errorMsg4 = deviceNumberExists;
      var reg = /^[A-Za-z0-9]{7,30}$/;
      if (processInput.checkIsEmpty(elementId, errorMsg1)
          && processInput.checkLength(elementId, maxLength, errorMsg2)
          && processInput.checkIsLegal(elementId, reg, null, errorMsg3)
          && processInput.checkDevice()) {
        return true;
      } else {
        return false;
      }
    },
    // 校验终端是否填写规范或者回车时不小心输入了异常字符
    checkRightDevice: function () {
      var reg = /^[A-Za-z0-9]{7,30}$/;
      var errorMsg3 = deviceNumberError;
      if (processInput.checkIsLegal("devices", reg, null, errorMsg3)) {
        return true;
      } else {
        return false;
      }
    },
    //校验终端手机号信息
    check_sim: function () {
      var elementId = "sims";
      var maxLength = 20;
      var errorMsg1 = simNumberNull;
      var errorMsg2 = '长度不超过20';
      var errorMsg3 = simNumberError;
      var errorMsg4 = simNumberExists;
      var reg = /^\d{7,20}$/g;
      if (processInput.checkIsEmpty(elementId, errorMsg1)
          && processInput.checkLength(elementId, maxLength, errorMsg2)
          && processInput.checkIsLegal(elementId, reg, null, errorMsg3)
          && processInput.checkSIM()) {
        return true;
      } else {
        return false;
      }
    },
    // 校验终端手机号是否填写规范或者回车时不小心输入了异常字符
    checkRightSim: function () {
      var errorMsg3 = simNumberError;
      var reg = /^\d{7,20}$/g;
      if (processInput.checkIsLegal("sims", reg, null, errorMsg3)) {
        return true;
      } else {
        return false;
      }
    },
    // 校验是否为空
    checkIsEmpty: function (elementId, errorMsg) {
      var value = $("#" + elementId).val();
      if (value == "") {
        processInput.hideErrorMsg();
        processInput.showErrorMsg(errorMsg, elementId);
        return false;
      } else {
        processInput.hideErrorMsg();
        return true;
      }
    },
    // 校验是否已存在
    checkIsExists: function (attr, elementId, requestUrl, errorMsg) {
      var tempFlag = true;
      $.ajax({
        type: 'POST',
        url: requestUrl,
        data: {"device": $("#" + elementId).val()},
        dataType: 'json',
        success: function (data) {
          if (!data) {
            processInput.showErrorMsg(errorMsg, elementId);
            tempFlag = false;
          } else {
            processInput.hideErrorMsg();
            tempFlag = true;
          }
        },
        error: function () {
          processInput.showErrorMsg("校验异常", elementId);
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
      var monitorType = $("#" + elementId).closest('form').find('input[name="monitorType"]:checked').val();
      if (elementId == "brands" || elementId == "quickBrands" || elementId == "fastBrands") {
        data = {"monitorType": monitorType, "inputId": "brands", "inputValue": $("#" + elementId).val()}
      } else if (elementId == "devices" || elementId == "quickDevices" || elementId == "oneDevices") {
        data = {"inputId": "devices", "inputValue": $("#" + elementId).val()}
      } else if (elementId == "sims" || elementId == "quickSims" || elementId == "speedSims") {
        data = {"inputId": "sims", "inputValue": $("#" + elementId).val()}
      }
      $.ajax({
        type: 'POST',
        url: url,
        data: data,
        dataType: 'json',
        async: false,
        success: function (data) {
          if (null != data && data.obj != null && data.obj.isBound) {
            layer.msg("不好意思，你来晚了！【" + data.obj.boundName + "】已被别人抢先一步绑定了");
            tempFlag = true;
          } else {
            tempFlag = false;
          }
        },
        error: function () {
          processInput.showErrorMsg("校验异常", elementId);
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
          processInput.showErrorMsg(errorMsg, elementId);
          return false;
        } else {
          processInput.hideErrorMsg();
          return true;
        }
      } else {
        if (!reg.test(value)) {
          processInput.showErrorMsg(errorMsg, elementId);
          return false;
        } else {
          processInput.hideErrorMsg();
          return true;
        }
      }
    },
    // 校验长度
    checkLength: function (elementId, maxLength, errorMsg) {
      var value = $("#" + elementId).val();
      if (value.length > parseInt(maxLength)) {
        processInput.showErrorMsg(errorMsg, elementId);
        return false;
      } else {
        processInput.hideErrorMsg();
        return true;
      }
    },
    // 清除错误信息
    clearErrorMsg: function () {
      $("label.error").hide();
    },
    keydownEvent: function (e) {
      var key = e.which;
      if ($('#processEntry').is(':hidden')) return;
      if (e.target.id !== 'processEntryBtn' && e.target.nodeName !== 'BODY' && $(e.target).closest('#processEntry').length === 0) return;
      if (key == 13) {
        if (!$("#layui-layer6").is(":hidden") && enterFlag) {
          $(".layui-layer-btn0").click();
        }
        var dataTarget;
        $('ul.steps li').each(function () {
          if ($(this).hasClass('active')) {
            dataTarget = $(this).attr('data-target');
          }
        });
        if (dataTarget == '#step1') {
          $(".nextBtnBrand").click();
        }
        if (dataTarget == '#step2') {
          $(".nextBtnGroup").click();
        }
        if (dataTarget == '#step3') {
          $(".nextBtnDevice").click();
        }
        if (dataTarget == '#step4') {
          $(".nextBtnSim").click();
        }
        if (dataTarget == '#step5') {
          if ($('#billingDateSubmits').is(":hidden")) {
            $(".nextBtnData").click();
          } else if (!enterFlag) {
            enterFlag = true;
            $("#submits").click();
          }
        }
        if (dataTarget == '#step6' && !enterFlag) {
          enterFlag = true;
          $("#submits").click();
        }
      }
    },
    objectMonitoring: function () {
      var html = $('#charNumList table').length;
      if (html == 0) {
        return false;
      }
    },
    //监控车
    chooseCar: function () {
      objType = 0;
      processInput.setObjType(objType);
      processInput.showType(objType);
      $("#brands").val("");
      $("#devices").val("");
      $("#deviceTypeIn").val("");
      $("#functionalTypeIn").val("");
      $("#sims").val("");
      processInput.putConfigValue();
      $("#monitorType").val("0");
      processInput.DeviceChange(objType);
    },
    //监控人
    choosePeople: function () {
      objType = 1;
      processInput.setObjType(objType);
      processInput.showType(objType);
      $("#brands").val("");
      $("#devices").val("");
      $("#deviceTypeIn").val("");
      $("#functionalTypeIn").val("");
      $("#sims").val("");
      processInput.putConfigValue();
      $("#monitorType").val("1");
      processInput.DeviceChange(objType);
    },
    showType: function (index) {
      if (index == 0) { //车
        $(".monitoringObj-car-details").show();
        $(".monitoringObj-car-add").show();
        $(".monitoringObj-people-details").hide();
        $(".monitoringObj-people-add").hide();
        $(".monitoringObj-thing-details").hide();
        $(".monitoringObj-thing-add").hide();
        $("#personList").removeClass("cancelChoose");
        $("#allShowPeopleArea").show();
        $("#peopleDetails").hide();
        $("#thingDetails").hide();
        $("#carDetails").show();
      } else if (index == 1) { //人
        $(".monitoringObj-car-details").hide();
        $(".monitoringObj-car-add").hide();
        $(".monitoringObj-thing-details").hide();
        $(".monitoringObj-thing-add").hide();
        $(".monitoringObj-people-details").show();
        $(".monitoringObj-people-add").show();
        $("#personList").addClass("cancelChoose");
        $("#allShowPeopleArea").hide();
        $("#professionals").val('');
        $("#professionalsID").val('');
        $("#peopleDetails").show();
        $("#carDetails").hide();
        $("#thingDetails").hide();
      } else if (index == 2) {
        $(".monitoringObj-thing-details").show();
        $(".monitoringObj-thing-add").show();
        $(".monitoringObj-car-add").hide();
        $(".monitoringObj-car-details").hide();
        $(".monitoringObj-people-add").hide();
        $(".monitoringObj-people-details").hide();
        $("#personList").addClass("cancelChoose");
        $("#allShowPeopleArea").hide();
        $("#professionals").val('');
        $("#professionalsID").val('');
        $("#peopleDetails").hide();
        $("#carDetails").hide();
        $("#thingDetails").show();
      }
      $(".detailMessage").hide();
      $("#charBtn").show();
      $("#detailBtn").hide();
      $(".delIcon").hide();
    },
    //
    inputValueChange: function () {
      $("#brands").bind("paste", processInput.inputOnPaste("brands"));
      $("#brands").bind("focus", function () {
        if ($("#brands").val() == "" && flag1) {
          intervalFlag = true;
          publicFun.switchBtnIE9("brands", "charNumList", "charBtn", "detailBtn");
          $(".charNew").show(); // 手动填写的值，显示新增模块
          $(".detailMessage").hide();
          $("#brandVal").attr("value", "")
          flag1 = false;
        }
      });
      $("#devices").bind("paste", processInput.inputOnPaste("devices"));
      $("#devices").bind("focus", function () {
        if ($("#devices").val() == "" && flag2) {
          intervalFlag = true;
          publicFun.switchBtnIE9("devices", "devicesList", "terminalBtn", "devDetailBtn");
          $(".terminalNew").show(); // 手动填写的值，显示新增模块
          $(".equipmentMessage").hide();
          $("#deviceVal").attr("value", "");
          flag2 = false;
        }
      });
      $("#sims").bind("paste", processInput.inputOnPaste("sims"));
      $("#sims").bind("focus", function () {
        if ($("#sims").val() == "" && flag3) {
          intervalFlag = true;
          publicFun.switchBtnIE9("sims", "SIMList", "SIMBtn", "SIMDetailBtn");
          $(".SIMNew").show(); // 手动填写的值，显示新增模块
          $(".SIMMessage").hide();
          $("#simVal").attr("value", "")
          flag3 = false;
        }
      });
      //IE9 不支持 input和propertychange
      if (navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") == "MSIE9.0") {
        $("#brands").bind("focus", function () {
          intervalFlag = true;
          publicFun.switchBtnIE9("brands", "charNumList", "charBtn", "detailBtn")
        })
            .bind("blur", function () {
              intervalFlag = false;
            });
        $("#devices").bind("focus", function () {
          intervalFlag = true;
        })
            .bind("blur", function () {
              intervalFlag = false;
            });
        $("#sims").bind("focus", function () {
          intervalFlag = true;
          publicFun.switchBtnIE9("sims", "SIMList", "SIMBtn", "SIMDetailBtn")
        })
            .bind("blur", function () {
              intervalFlag = false;
            });
      }
      else {
        $("#brands").bind("input propertychange", function () {
          publicFun.switchBtn("brands", "charNumList", "charBtn", "detailBtn")
        });
        $("#devices").bind("input propertychange", function () {
          publicFun.switchBtn("devices", "devicesList", "terminalBtn", "devDetailBtn")
        });
        $("#sims").bind("input propertychange", function () {
          publicFun.switchBtn("sims", "SIMList", "SIMBtn", "SIMDetailBtn")
        });
      }
    },
    //根据监控对象类型改变终端类型及功能类型
    DeviceChange: function (type) {
      /*if (type == 0) {*/
      var option='';
      for(var i=0; i<agreementType.length;i++){
        var item = agreementType[i];
        if(item.protocolCode == '1'){
          option += "<option selected value='"+item.protocolCode+"'>"+item.protocolName+"</option>"
        }else{
          option += "<option value='"+item.protocolCode+"'>"+item.protocolName+"</option>"
        }
      };
      $('#deviceTypeIn').html(option);
      $("#functionalTypeIn").html(
          '<option value="1">简易型车机</option>' +
          '<option value="2">行车记录仪</option>' +
          '<option value="3">对讲设备</option>' +
          '<option value="4">手咪设备</option>' +
          '<option value="5">超长待机设备</option>' +
          '<option value="6">定位终端</option>'
      );
    },
    //监控 --综合展示
    monitoringObjToShow: function () {
      //objType
      if (objType == 0) {
        $("#brandNumber").text($("#brand").val());
        $("#vehicleOwner").text($("#vehicleOwnerIn").val());
        $("#vehicleOwnerPhone").text($("#vehicleOwnerPhoneIn").val());
        $("#vehicleType_show").text($("#vehicleType").find("option:selected").text());
      } else if (objType == 1) {
        $("#people_Number").text($("#brand").val());
        $("#peopleIdentity").text($("#identity").val());
        $("#peoplePhone").text($("#phone").val());
      } else if (objType == 2) {
        $("#thing_Number").text($("#brand").val());
        $("#thing_enterprise").text($("#monitoringObjThingAdd").val());
        $("#thing_name1").text($("#thingName").val());
        $("#thing_model1").text($("#thingType").val());
      }
    },
    //终端 -- 综合展示
    terminalToShow: function () {
      $("#deviceNumber").text($("#devices").val());
      $("#deviceName").text($("#deviceNameIn").val());
    },
    //SIM -- 综合展示
    SIMToShow: function () {
      $("#simcardNumber").text($("#sims").val());
      $("#iccid_show").text($("input[name='iccid']").val());
    },
    //车、人、物点击tab切换
    chooseLabClick: function () {
      if (!$(this).hasClass('activeIcon')) {
        flag2 = true, flag3 = true;
        $("#devices").css('backgroundColor', '#fff');
        $("#sims").css('backgroundColor', '#fff');
        $("#addForm1 ul.dropdown-menu").css("display", "none");
        $(this).parents('.lab-group').find('input').attr("checked", false);
        $(this).siblings('input').attr("checked", true);
        $(this).parents('.lab-group').find('label.monitoringSelect').removeClass("activeIcon");
        $(this).addClass('activeIcon');
        //隐藏终端、终端手机号详情按钮，显示新增
        $("#terminalBtn").show();
        $("#devDetailBtn").hide();
        $("#SIMBtn").show();
        $("#SIMDetailBtn").hide();
        //隐藏终端、终端手机号详情和新增信息
        $(".terminalNew").hide();
        $(".equipmentMessage").hide();
        $(".SIMMessage").hide();
        $(".SIMNew").hide();
        //隐藏错误提示
        processInput.hideErrorMsg();
        objType = $(this).siblings('input').val();
        processInput.setObjType(objType);
        if (objType == '0') {
          $('#billingDateSubmits').hide();
          $('#billingDateNext').show();
          $('#personList').show();
        } else {
          $('#billingDateSubmits').show();
          $('#billingDateNext').hide();
          $('#personList').hide();
        }
        processInput.showType(objType);
        $("#brands").val("");
        $("#devices").val("");
        $("#deviceTypeIn").val("");
        $("#functionalTypeIn").val("");
        $("#sims").val("");
        processInput.putConfigValue();
        $("#monitorType").val("0");
        processInput.DeviceChange(objType);
      }
    },
    //选择人
    choosePeopleLabClick: function () {
      if ($("#chooseCar").attr("checked")) {
        $("#chooseCar").removeAttr("checked", "checked");
        $("#chooseCarLab").removeClass("activeIcon");
        $("#choosePeople").attr("checked", true);
        $("#choosePeopleLab").addClass('activeIcon');
        //隐藏终端、终端手机号详情按钮，显示新增
        $("#terminalBtn").show();
        $("#devDetailBtn").hide();
        $("#SIMBtn").show();
        $("#SIMDetailBtn").hide();
        //隐藏终端、终端手机号详情和新增信息
        $(".terminalNew").hide();
        $(".equipmentMessage").hide();
        $(".SIMMessage").hide();
        $(".SIMNew").hide();
        //隐藏错误提示
        processInput.hideErrorMsg();
      }
    },
    //选择车
    chooseCarLabClick: function () {
      if ($("#choosePeople").attr("checked")) {
        $("#choosePeople").removeAttr("checked", "checked");
        $("#choosePeopleLab").removeClass("activeIcon");
        $("#chooseCar").attr("checked", true);
        $("#chooseCarLab").addClass('activeIcon');
        //隐藏终端、终端手机号详情按钮，显示新增
        $("#terminalBtn").show();
        $("#devDetailBtn").hide();
        $("#SIMBtn").show();
        $("#SIMDetailBtn").hide();
        //隐藏终端、终端手机号详情和新增信息
        $(".terminalNew").hide();
        $(".equipmentMessage").hide();
        $(".SIMMessage").hide();
        $(".SIMNew").hide();
        //隐藏错误提示
        processInput.hideErrorMsg();
      }
    },
    CurentTime: function () {
      var now = new Date();
      var year = now.getFullYear();       //年
      var month = now.getMonth() + 1;     //月
      var day = now.getDate();            //日
      var hh = now.getHours();            //时
      var mm = now.getMinutes();          //分
      var clock = year + "-";
      if (month < 10)
        clock += "0";
      clock += month + "-";
      if (day < 10)
        clock += "0";
      clock += day + " ";
      if (hh < 10)
        clock += "0";
      clock += hh + ":";
      if (mm < 10) clock += '0';
      clock += mm;
      return (clock);
    },
    nextyeartime: function () {
      var now = new Date();
      var year = now.getFullYear() + 1;       //年
      var month = now.getMonth() + 1;     //月
      var day = now.getDate();            //日
      var hh = now.getHours();            //时
      var mm = now.getMinutes();          //分
      var clock = year + "-";
      if (month < 10)
        clock += "0";
      clock += month + "-";
      if (day < 10)
        clock += "0";
      clock += day + " ";
      if (hh < 10)
        clock += "0";
      clock += hh + ":";
      if (mm < 10) clock += '0';
      clock += mm;
      return (clock);
    },
    //获取车辆类别
    getVehicleCategory: function () {
      var url = "/clbs/m/basicinfo/monitoring/vehicle/type/findAllVehicleCategoryHasBindingVehicleType.gsp";
      var data = {"vehicleCategory": ""}
      json_ajax("GET", url, "json", false, data, processInput.categoryCallBack);
    },
    categoryCallBack: function (data) {
      var result = data.obj.result;
      var str = "";
      for (var i = 0; i < result.length; i++) {
        if (result[i].vehicleCategory == '其他车辆') {
          str += '<option selected="selected" value="' + result[i].id + '">' + html2Escape(result[i].vehicleCategory) + '</option>'
        } else {
          str += '<option  value="' + result[i].id + '">' + html2Escape(result[i].vehicleCategory) + '</option>'
        }
      }
      $("#category").html(str);
    },
    //获取车辆类型
    getVehicleType: function (id) {
      var url = "/clbs/m/basicinfo/monitoring/vehicle/type/findCategoryById_" + id + ".gsp";
      json_ajax("GET", url, "json", false, null, processInput.getTypeCallback);
    },
    getTypeCallback: function (data) {
      var result = data.obj.vehicleTypeList;
      var str = "";
      if (result != null && result != '') {
        for (var i = 0; i < result.length; i++) {
          if (result[i].vehicleType == '其他车辆') {
            str += '<option selected="selected" value="' + result[i].id + '">' + html2Escape(result[i].vehicleType) + '</option>'
          } else {
            str += '<option  value="' + result[i].id + '">' + html2Escape(result[i].vehicleType) + '</option>'
          }
        }
      }
      $("#vehicleType").html(str);
    },
    // 获取车辆用途数据
    getCategoryData: function () {
      //车辆用途
      var urlPurposeCategory = "/clbs/m/basicinfo/monitoring/vehicle/findAllPurposeCategory";
      json_ajax("POST", urlPurposeCategory, "json", false, null, function (data) {
        var datas = data.obj.VehicleCategoryList;
        var dataList = {value: []}, i = datas.length;
        while (i--) {
          dataList.value.push({
            name: datas[i].purposeCategory,
            id: datas[i].id
          });
          if (datas[i].codeNum != null) {
            $("#processCategory").val(datas[i].purposeCategory);
            $("#vehiclePurpose").val(datas[i].id);
          }
        }
        $("#processCategory").bsSuggest({
          indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
          indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
          data: dataList,
          effectiveFields: ["name"]
        }).on('onDataRequestSuccess', function (e, result) {
        }).on('onSetSelectValue', function (e, keyword, data) {
          $("#processCategory").val(keyword.key);
          $("#vehiclePurpose").val(keyword.id);
        }).on('onUnsetSelectValue', function () {
        });
      });
    },
    // 通讯类型初始化
    agreementType:function () {
      var url = '/clbs/m/connectionparamsset/protocolList';
      var param={"type":808}
      json_ajax("POST", url, "json", false, param, function (data) {
        var data = data.obj;
        agreementType = data;
        for(var i=0; i<data.length; i++){
          $('#quickDeviceType,#communicationType,#deviceTypeIn').append(
              "<option value='"+data[i].protocolCode+"'>"+data[i].protocolName+"</option>"
          );
        }
      })
    }
  };
  return {
    processInput: processInput,
  }
})
