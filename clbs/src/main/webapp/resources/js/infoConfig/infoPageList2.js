(function (window, $) {
  var fastInitFlag = true;
  var processInitFlag = true;
  var sweepInitFlag = true;

  require.config({
    // baseUrl: "/clbs/resources/js/infoConfig/info",
    paths: {
      "infoList": "info/infoList",
      "infoPublicFun": "info/infoPublicFun",
      "fast": "info/fast",
      "scanCode": "info/scanCode",
      "processInput": "process/processInput",
    },
  });

  // 加载模块
  require(['processInput', 'infoList', 'infoPublicFun', 'fast', 'scanCode'], function (processInputA, infoinputListA, infoPublicFunA, infoFastInputA, sweepCodeEntryA) {
    // alert("加载成功-2！");
    var processInput = processInputA.processInput;
    var infoinputList = infoinputListA.infoinputList;
    var publicFun = infoPublicFunA.publicFun;
    var infoFastInput = infoFastInputA.infoFastInput;
    var sweepCodeEntry = sweepCodeEntryA.sweepCodeEntry;

    /**
     * 录入方法初始化
     **/
    $(function () {
      publicFun.groupTreeInit('quick');
      publicFun.leftTree();
      /**
       * 信息录入方式tab切换
       * */
      $(".entryBtn").on('click', function () {
        searchFlag = true;
        $(this).removeClass('btn-default').addClass('btn-primary');
        $(this).siblings().removeClass('btn-primary').addClass('btn-default');
        var curId = $(this).attr('data-target');
        if (curId == 'quickEntry' || curId == 'fastEntry') {
          quickMonitorType = $("#" + curId + ' input[type="radio"]:checked').val();
        }
        if (curId == 'fastEntry') {
          if (fastInitFlag) {
            publicFun.groupTreeInit('fast');
            fastInitFlag = false;
          }
        } else if (curId == 'sweepCodeEntry') {
          setTimeout(function () {
            $('#scanSim').focus();
          }, 20);
          if (sweepInitFlag) {
            publicFun.groupTreeInit('sweep');
            sweepInitFlag = false;
          }
        } else if (curId == 'processEntry') {
          if (processInitFlag) {
            processInput.init();
            processInitFlag = false;
          }
        }
        $("#" + curId).addClass('active').siblings().removeClass('active');
      });
      /**
       * 信息列表相关方法初始化
       * */
      infoinputList.init();
      $('input').inputClear().on('onClearEvent', function (e, data) {
        var id = data.id;
        if (id == 'search_condition') {
          search_ztree('treeDemo', id, treeSearchType.value);
        }
      });
      //IE9
      if (navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") == "MSIE9.0") {
        var search;
        $("#search_condition").bind("focus", function () {
          search = setInterval(function () {
            search_ztree('treeDemo', 'search_condition', treeSearchType.value);
          }, 500);
        }).bind("blur", function () {
          clearInterval(search);
        });
      }
      infoinputList.showMenuText();
      if (navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") == "MSIE9.0") {
        infoinputList.refreshTable();
      }
      $("#refreshTable").on("click", infoinputList.refreshTable);
      /**
       * 快速录入以及极速录入相关方法初始化
       * */
      var datas;
      infoFastInput.arrayExpand();
      $('input').inputClear().on('onClearEvent', function (e, data) {
        var id = data.id;
        switch (id) {
          case 'speedDevices':
            infoFastInput.setInputDisabled();
            break;
          case 'oneDevices':
            $("#oneDevicesName").val('');
            break;
          case 'quickBrands':
            $("#quickPlateColor").removeAttr('disabled');
            break;
          case 'fastBrands':
            $("#fastPlateColor").removeAttr('disabled');
            break;
          case 'quickGroupId':
            searchFlag = false;
            $("#quickCitySelidVal").val('');
            publicFun.groupTreeInit('quick');
            break;
          case 'speedGroupid':
            searchFlag = false;
            $("#speedCitySelidVal").val('');
            publicFun.groupTreeInit('fast');
            break;
          case 'sweepCodeGroupid':
            searchFlag = false;
            $("#sweepCodeCitySelidVal").val('');
            publicFun.groupTreeInit('sweep');
            break;
          case 'quickDevices':
            $("#quickDeviceVal").attr('value', '');
            break;
          case 'quickSims':
            $("#quickSimVal").attr('value', '');
            break;
          default:
            break;
        }
        setTimeout(function () {
          $('#' + id).focus();
        }, 20);
      });
      infoFastInput.init();
      $(".groupZtree").on("click", showMenuContent);
      $('#quickSubmits').on("click", infoFastInput.doSubmits);
      $("#speedSubmits").bind("click", infoFastInput.speedDoSubmits);
      $("#quickBrands").blur(function () {
        infoFastInput.blurFun("quickBrands")
      });
      $("#quickBrands").bind("paste", function () {
        infoFastInput.inputOnPaste("quickBrands")
      });
      $("#quickDevices").blur(function () {
        infoFastInput.blurFun("quickDevices")
      });
      $("#quickDevices").bind("paste", function () {
        infoFastInput.inputOnPaste("quickDevices")
      });
      $("#fastBrands").blur(function () {
        infoFastInput.blurFun("fastBrands")
      });
      $("#fastBrands").bind("paste", function () {
        infoFastInput.inputOnPaste("fastBrands")
      });
      $("#speedSims").blur(function () {
        infoFastInput.blurFun("speedSims")
      });
      $("#speedSims").bind("paste", function () {
        infoFastInput.inputOnPaste("speedSims")
      });
      $("#quickDeviceType").bind("change", function () {
        infoFastInput.check_deviceType();
      });
      $("#quickSims").blur(function () {
        setTimeout(function () {
          infoFastInput.blurFun("quickSims")
        }, 310);
      });
      $("#quickSims").bind("paste", function () {
        infoFastInput.inputOnPaste("quickSims")
      });
      //监听浏览器窗口变化
      $(window).resize(infoFastInput.windowResize);
      $("#toggle-left").on("click", infoFastInput.toggleLeft);
      //极速录入终端为标识查询
      $("#searchDevices").bind('click', infoFastInput.speedSearchDevices);
      $("#speedDevices").bind('click', infoFastInput.inputClick);//键盘回车事件
      $(document).bind('click', infoFastInput.documentClick);
      //模糊匹配
      $("#speedDevices").bind('input onproperchange', infoFastInput.searchList);//判断用户输入的是否是已经有的唯一标识
      $("#speedDevices").bind('blur', infoFastInput.judgehasFlag);
      $("#onlyLogo").on('mouseover ', function () {
        hasFlag1 = false;
      });
      $("#onlyLogo").on('mouseout', function () {
        hasFlag1 = true;
      });
      //新增终端
      $("#oneDevices").bind('input onproperchange change', function () {
        $("#oneDevicesName").val($(this).val());
      });
      //车、人、物点击tab切换
      $("#quickEntryForm label.monitoringSelect,#fastEntryForm label.monitoringSelect").on("click", infoFastInput.chooseLabClick);
      $(".select-value,.btn-width-select").buttonGroupPullDown();
      $("#speedEntryLi").on("click", infoFastInput.speedEntryLiClickFn);
      $("#quickEntryLi").on("click", infoFastInput.quickEntryLiClickFn);
      $("#quickGroupId").on('input propertychange', function (value) {
        var treeObj = $.fn.zTree.getZTreeObj("quickTreeDemo");
        treeObj.checkAllNodes(false);
        $("#quickCitySelidVal").val('');
        search_ztree('quickTreeDemo', 'quickGroupId', 'assignment');
      });
      $("#speedGroupid").on('input propertychange', function (value) {
        var treeObj = $.fn.zTree.getZTreeObj("fastTreeDemo");
        treeObj.checkAllNodes(false);
        $("#speedCitySelidVal").val('');
        search_ztree('fastTreeDemo', 'speedGroupid', 'assignment');
      });
      /**
       * 流程录入服务期限日历框初始化
       * */
      var start_data = processInput.CurentTime();
      var end_data = processInput.nextyeartime();
      $('#timeInterval').dateRangePicker({
        'type': 'after',
        'format': 'YYYY-MM-DD',
        'start_date': start_data,
        'end_date': end_data,
      });
      /**
       * 扫码录入相关方法初始化
       * */
      sweepCodeEntry.PCKeyDownEvent();
      //扫码录入显示执行
      $("#scanCodeEntryShow").on("click", sweepCodeEntry.scanCodeEntryShow);
      // 组织架构模糊搜索
      $("#search_condition").on("input oninput", function () {
        search_ztree("treeDemo", 'search_condition', treeSearchType.value);
      });
      //车、人、物点击tab切换
      $("#addScanCodeEntry label.monitoringSelect").on("click", sweepCodeEntry.chooseLabClick);
      $("#sweepCodeGroupid").on('input propertychange', function (value) {
        var treeObj = $.fn.zTree.getZTreeObj("sweepTreeDemo");
        treeObj.checkAllNodes(false);
        $("#sweepCodeCitySelidVal").val('');
        search_ztree('sweepTreeDemo', 'sweepCodeGroupid', 'assignment');
      });
      /**
       * 控制极速录入未注册设备流程说明图片的显示隐藏
       * */
      var mainWrapper = $('.main-content-wrapper');
      $("#fastStartGuide").on('click', function () {
        $('body').css('overflow-y', 'hidden');
        if (mainWrapper.hasClass('main-content-toggle-left')) {
          $('#fastInstructions').css('padding-left', '15px');
        } else {
          $('#fastInstructions').css('padding-left', '255px');
        }
        $('#fastInstructions').show();
      })
      $('#fastInstructions').on('click', function () {
        $('#fastInstructions').hide();
        $('body').css('overflow-y', 'auto');
      })
      // 输入框输入类型过滤
      inputValueFilter('#simpleQueryParam');
      $("#confirmDeleteSubmit").bind('click', function () {
        var url = '/clbs/m/infoconfig/infoinput/deleteConfig_' + deleteconfigId + '.gsp';
        json_ajax('POST', url, 'json', false, null, function (data) {
          if (data.success) {
            layer.msg('删除成功');
            $("#confirmDeleteModal").modal('hide');
            myTable.requestData()
          }else{
            layer.msg(data.msg);
          }
        })
      })
    })
  });
})(window, $);
