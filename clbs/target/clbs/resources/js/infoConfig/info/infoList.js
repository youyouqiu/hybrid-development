// 信息列表

define(['infoPublicFun'], function(infoPublicFun) {
  var publicFun = infoPublicFun.publicFun;
  var selectTreeId = publicFun.getSelectTreeId();
  var selectTreeType = publicFun.getSelectTreeType();
  // myTable;
  //初始化搜索类型
  treeSearchType.init();
  treeSearchType.onChange = function (datas) {
    console.log(datas)
    $("#search_condition").attr('placeholder', datas.placeholder)
    search_ztree('treeDemo', 'search_condition', datas.value);
  }
  addFlag = false;//用于新加数据列表第一行变色效果
  var deleteconfigId = '';
  var $keepOpen = $(".keep-open");

  var infoinputList = {
    init: function () {
      $("[data-toggle='tooltip']").tooltip();
      infoinputList.infoTableInit();
    },
    //显示隐藏列
    showMenuText: function () {
      var menu_text = "";
      var table = $("#dataTable tr th:gt(1)");
      menu_text += "<li><label><input type=\"checkbox\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" checked=\"checked\" disabled />" + table[0].innerHTML + "</label></li>"
      for (var i = 1; i < table.length; i++) {
        menu_text += "<li><label><input type=\"checkbox\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" checked=\"checked\" />" + table[i].innerHTML + "</label></li>"
      };
      $("#Ul-menu-text").html(menu_text);
      //显示隐藏列
      $('.toggle-vis').on('change', function (e) {
        var column = myDataTable.column($(this).attr('data-column'));
        column.visible(!column.visible());
        $keepOpen.addClass("open");
      });
    },
    //创建表格
    infoTableInit: function () {
      //表格列定义
      var columnDefs = [{
        //第一列，用来显示序号
        "searchable": false,
        "orderable": false,
        "targets": 0
      }];
      var columns = [{
        //第一列，用来显示序号
        "data": null,
        "class": "text-center"
      }, {
        //第二列，checkbox
        "data": null,
        "class": "text-center",
        render: function (data, type, row, meta) {
          return '<input type="checkbox" name="subChk" value="' + row.configId + '" /> ';
        }
      }, {//第三列，操作按钮列
        "data": null,
        "class": "text-center", //最后一列，操作按钮
        render: function (data, type, row, meta) {
          var editUrlPath = myTable.editUrl + row.configId + ".gsp"; //修改地址
          var detailUrlPath = myTable.detailUrl + row.configId + ".gsp";
          var result = '';
          //修改按钮
          result += '<button href="' + editUrlPath + '" data-target="#commonLgWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
          //详情按钮
          result += '<button href="' + detailUrlPath + '" data-target="#commonWin" data-toggle="modal" type="button" class=" editBtn editBtn-info"><i class="fa fa-sun-o"></i>详情</button>&ensp;';
          //解绑按钮
          result += '<button type="button" onclick="myTable.deleteItem(\'' + row.configId + '\')" class="brokenDeleteButton editBtn brokenDisableClick"><i class="fa fa-chain-broken" style="margin-right:4px; font-size: 14px"></i>解绑</button>&ensp;';
          //删除按钮
          result += '<button onclick="infoinputList.confirmDeleteInfo(\'' + row.carLicense + '\',\'' + row.simcardNumber + '\',\'' + row.deviceNumber + '\',\'' + row.configId + '\')" class="deleteButton editBtn disableClick" type="button"><i class="fa fa-trash-o"></i> 删除</button>';
          return result;
        }
      }, {//第四列，监控对象-车
        "data": "carLicense",
        "class": "text-center"
      }, {//监控对象类型
        "data": "monitorType",
        "class": "text-center",
        render: function (data) {
          if (data == "0") {
            return "车";
          } else if (data == "1") {
            return "人";
          } else if (data == "2") {
            return "物";
          } else {
            return "";
          }
        }
      }, {//车牌颜色
        "data": "plateColor",
        "class": "text-center",
        render: function (data, type, row, meta) {
          if (row.monitorType != '0') return '';
          return getPlateColor(data);
        }
      }, {// 企业
        "data": "groupName",
        "class": "text-center",
      }, {// 分组
        "data": "assignmentName",
        "class": "text-center",
      }, { // 终端手机号
        "data": "simcardNumber",
        "class": "text-center"
      }, {  //真实SIM卡号
        "data": "realId",
        "class": "text-center",
        render: function (data) {
          if (data == null) return '';
          return data;
        }
      }, { // 终端编号
        "data": "deviceNumber",
        "class": "text-center",
      }, { // 通讯类型
        "data": "deviceType",
        "class": "text-center",
        render: function (data) {
          return getProtocolName(data);
        }
      }, { //终端厂商
        "data": "terminalManufacturer",
        "class": "text-center",
        render: function (data, type, row, meta) {
          if (data == null) return '';
          return data;
        }
      }, { //终端型号
        "data": "terminalType",
        "class": "text-center",
        render: function (data, type, row, meta) {
          if (data == null) return '';
          return data;
        }
      }, { // 功能类型
        "data": "functionalType",
        "class": "text-center",
        render: function (data) {
          if (data == "1") {
            return "简易型车机";
          } else if (data == "2") {
            return "行车记录仪";
          } else if (data == "3") {
            return "对讲设备";
          } else if (data == "4") {
            return "手咪设备";
          } else if (data == "5") {
            return "超长待机设备";
          } else if (data == "6") {
            return "定位终端";
          } else {
            return "";
          }
        }
      }, { // 计费日期
        "data": "billingDate",
        "class": "text-center",
        render: function (data, type, row, meta) {
          return (data != null && data.length > 10) ? data.substr(0, 10) : "";
        }
      }, { // 到期日期
        "data": "expireDate",
        "class": "text-center",
        render: function (data, type, row, meta) {
          return (data != null && data.length > 10) ? data.substr(0, 10) : "";
        }
      }, { // 从业人员
        "data": "professionalNames",
        "class": "text-center"
      }, { //加车时间
        "data": "createDateTime",
        "class": "text-center",
        render: function (data, type, row, meta) {
          return (data != null && data.length > 10) ? data.substr(0, 10) : "";
        }
      }, { //加车时间
        "data": "updateDateTime",
        "class": "text-center",
        render: function (data, type, row, meta) {
          return (data != null && data.length > 10) ? data.substr(0, 10) : "";
        }
      }];
      //全选
      $("#checkAll").click(function () {
        $("input[name='subChk']").prop("checked", this.checked);
      });
      //单选
      var subChk = $("input[name='subChk']");
      subChk.click(function () {
        $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
      });
      //批量删除
      $("#del_model").click(function () {
        //判断是否至少选择一项
        var chechedNum = $("input[name='subChk']:checked").length;
        if (chechedNum == 0) {
          layer.msg(selectItem, {move: false});
          return;
        }
        var checkedList = new Array();
        $("input[name='subChk']:checked").each(function () {
          checkedList.push($(this).val());
        });
        myTable.deleteItems({'deltems': checkedList.toString()});
      });
      //ajax参数
      var ajaxDataParamFun = function (d) {
        selectTreeId = publicFun.getSelectTreeId();
        selectTreeType = publicFun.getSelectTreeType();
        d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
        d.groupName = selectTreeId;
        d.groupType = selectTreeType;
      };
      //表格setting
      var setting = {
        detailUrl: "/clbs/m/infoconfig/infoinput/getConfigDetails_",
        listUrl: "/clbs/m/infoconfig/infoinput/list",
        editUrl: "/clbs/m/infoconfig/infoinput/edit_",
        deleteUrl: "/clbs/m/infoconfig/infoinput/delete_",
        deletemoreUrl: "/clbs/m/infoconfig/infoinput/deletemore",
        enableUrl: "/clbs/c/user/enable_",
        disableUrl: "/clbs/c/user/disable_",
        columnDefs: columnDefs, //表格列定义
        columns: columns, //表格列
        dataTableDiv: 'dataTable', //表格
        drawCallbackFun: function () {
          if (addFlag) {
            var curTr = $('#dataTable tbody tr:first-child');
            curTr.addClass('highTr');
            setTimeout(function () {
              curTr.removeClass('highTr');
              addFlag = false;
            }, 1000)
          }
        },
        ajaxDataParamFun: ajaxDataParamFun, //ajax参数
        pageable: true, //是否分页
        showIndexColumn: true, //是否显示第一列的索引列
        enabledChange: true
      };
      //创建表格
      myTable = new TG_Tabel.createNew(setting);
      myTable.init();
    },
    // 刷新table列表
    refreshTable: function () {
      $('#simpleQueryParam').val("");
      selectTreeId = '';
      selectTreeType = '';
      myTable.requestData();
      var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
      treeObj.refresh();
    },
    // 列表(外设列)查看详情功能
    showLogContent: function (msg) { // 显示log详情
      var url = "/clbs//m/infoconfig/infoinput/getPeripherals";
      var parameter = {"vehicleId": msg};
      json_ajax("POST", url, "json", true, parameter, infoinputList.getPeripheralsCallback);
    },
    getPeripheralsCallback: function (data) {
      if (data != null && data != undefined && data != "") {
        if (data.success) {
          $("#detailShow").modal("show");
          $("#detailContent").html(data.obj.pname == "" ? "该监控对象目前还没有绑定外设哦！" : data.obj.pname);
        } else {
        }
      }
    },
    confirmDeleteInfo: function (carLicense, simcardNumber, deviceNumber, configId) {
      $('#confirmDeleteModal').modal('show');
      $("#carLicense").html(carLicense == 'null' || carLicense == '' ? '' : carLicense);
      $("#simcardNumber").html(simcardNumber == 'null' || simcardNumber == '' ? '' : simcardNumber);
      $("#real").html(deviceNumber == 'null' || deviceNumber == "" ? '' : deviceNumber);
      deleteconfigId = configId;
    },
  };
  return {
    infoinputList: infoinputList,
  }
})
