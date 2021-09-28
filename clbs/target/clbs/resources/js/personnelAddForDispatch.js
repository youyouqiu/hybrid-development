(function (window, $) {
  var jobs = $('#allJob').val() ? $('#allJob').val() : '[]';
  var allJob = JSON.parse(jobs); // 所有职位类别
  var skillStr = $('#skillTree').val();
  var skillTree = skillStr !== undefined && skillStr !== null && skillStr !== "" ? JSON.parse(skillStr) : []; // 技能树
  var allDriverType = JSON.parse($('#allDriverType').val()); // 驾驶证类别
  addPersonnelInfo = {
    init: function () {
      var setting = {
        async: {
          url: '/clbs/m/basicinfo/enterprise/professionals/tree',
          type: 'post',
          enable: true,
          autoParam: ['id'],
          contentType: 'application/json',
          dataType: 'json',
          dataFilter: addPersonnelInfo.ajaxDataFilter
        },
        view: {
          dblClickExpand: false,
          nameIsHTML: true,
          countClass: 'group-number-statistics'
        },
        data: {
          simpleData: {
            enable: true
          }
        },
        callback: {
          beforeClick: addPersonnelInfo.beforeClick,
          onClick: addPersonnelInfo.onClick,
          onAsyncSuccess: addPersonnelInfo.zTreeOnAsyncSuccess
        }
      };
      $.fn.zTree.init($('#ztreeDemo'), setting, null);
      addPersonnelInfo.initJobSelect();

      addPersonnelInfo.skillTreeInit();
      addPersonnelInfo.driverTreeInit();
    },
    // 初始化职位类别下拉框数据
    initJobSelect: function () {
      var jobsDataList = {value: []};
      for (var j = 0; j < allJob.length; j++) {
        jobsDataList.value.push({
          name: allJob[j].jobName,
          id: allJob[j].id
        });
      }
      $('#jobName').bsSuggest({
        indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
        indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
        data: jobsDataList,
        effectiveFields: ['name']
      }).on('onDataRequestSuccess', function (e, result) {
      }).on('onSetSelectValue', function (e, keyword, data) {
        $('#jobId').val(keyword.id);
      }).on('onUnsetSelectValue', function () {
        $('#jobId').val('');
      }).on('input propertychange', function () {
        $('#jobId').val('');
      });
    },
    // 驾驶证类别树
    driverTreeInit: function (flag) {
      var treeSetting = {
        view: {
          showIcon: false,
          selectedMulti: false,
          nameIsHTML: true,
          fontCss: setFontCss_ztree
        },
        check: {
          enable: true,
          chkStyle: 'checkbox',
          chkboxType: {
            'Y': 'ps',
            'N': 'ps'
          },
          radioType: 'all'
        },
        data: {
          simpleData: {
            enable: true
          }
        },
        callback: {
          onClick: addPersonnelInfo.onClickBack,
          onCheck: addPersonnelInfo.onCheckType
        }
      };
      var zNodes = allDriverType;
      $.fn.zTree.init($('#driverCategoryZtreeDemo'), treeSetting, zNodes);
    },
    // 技能树
    skillTreeInit: function (flag) {
      var treeSetting = {
        view: {
          showIcon: false,
          selectedMulti: false,
          nameIsHTML: true,
          fontCss: setFontCss_ztree
        },
        check: {
          enable: true,
          chkStyle: 'checkbox',
          chkboxType: {
            'Y': 'ps',
            'N': 'ps'
          },
          radioType: 'all'
        },
        data: {
          simpleData: {
            enable: true,
            idKey: 'id',
            pIdKey: 'pid'
          }
        },
        callback: {
          onClick: addPersonnelInfo.onClickBack,
          onCheck: addPersonnelInfo.onCheckType,
          onNodeCreated: addPersonnelInfo.zTreeOnNodeCreated
        }
      };
      var zNodes = skillTree;
      $.fn.zTree.init($('#skillsZtreeDemo'), treeSetting, zNodes);
    },
    onClickBack: function (e, treeId, treeNode, clickFlag) {
      var zTreeObj = $.fn.zTree.getZTreeObj(treeId);
      zTreeObj.checkNode(treeNode, !treeNode.checked, true);
      addPersonnelInfo.onCheckType(e, treeId, treeNode);
    },
    onCheckType: function (e, treeId, treeNode) {
      var zTree = $.fn.zTree.getZTreeObj(treeId);
      //若为取消勾选则不展开节点
      if (treeNode.checked) {
        var nodes = zTree.getCheckedNodes(true);
        if (treeId == 'driverCategoryZtreeDemo' && nodes.length > 3) {
          zTree.checkNode(treeNode, false, true, true); // 取消节点勾选节点
          layer.msg('最多只能勾选3项');
          return false;
        }
        zTree.expandNode(treeNode, true, true, true, true); // 展开节点
      }
      addPersonnelInfo.getTypeSelect(zTree, treeId);
      addPersonnelInfo.getTypeCheckedNodes(treeId);
    },
    getTypeSelect: function (treeObj, treeId) {
      var nodes = treeObj.getCheckedNodes(true);
      var allNodes = treeObj.getNodes();
      if (treeId == 'skillsZtreeDemo') {
        if (nodes.length > 0) {
          $('#skills').val(allNodes[0].name);
        } else {
          $('#skills').val('');
        }
      } else {
        if (nodes.length > 0) {
          $('#driverCategory').val(allNodes[0].name);
        } else {
          $('#driverCategory').val('');
        }
      }
    },
    getTypeCheckedNodes: function (treeId) {
      var typezTree = $.fn.zTree.getZTreeObj(treeId),
        nodes = typezTree.getCheckedNodes(true),
        allId = '', allName = '';
      for (var i = 0, l = nodes.length; i < l; i++) {
        if (treeId == 'skillsZtreeDemo' && nodes[i].pid || treeId == 'driverCategoryZtreeDemo') {
          allId += nodes[i].id + ',';
          allName += nodes[i].name + ',';
        }
      }
      if (treeId == 'skillsZtreeDemo') {
        $('#skills').val(allName == '' ? '' : allName.substring(0, allName.length - 1));
        $('#skillIds').val(allId == '' ? '' : allId.substring(0, allId.length - 1));
      } else {
        $('#driverCategory').val(allName == '' ? '' : allName.substring(0, allName.length - 1));
        $('#driverTypeIds').val(allId == '' ? '' : allId.substring(0, allId.length - 1));
      }
    },
    zTreeOnNodeCreated: function (event, treeId, treeNode, msg) {
      var treeObj = $.fn.zTree.getZTreeObj(treeId);
      treeObj.expandAll(true); // 展开所有节点
    },
    zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
      var treeObj = $.fn.zTree.getZTreeObj(treeId);
      treeObj.expandAll(true); // 展开所有节点
    },
    beforeClickPermission: function (treeId, treeNode) {
      var zTree = $.fn.zTree.getZTreeObj('permissionDemo');
      zTree.checkNode(treeNode, !treeNode.checked, true, true);
      return false;
    },
    onClick: function (e, treeId, treeNode) {
      var zTree = $.fn.zTree.getZTreeObj('ztreeDemo'),
        nodes = zTree.getSelectedNodes(),
        v = '';
      n = '';
      nodes.sort(function compare(a, b) {
        return a.id - b.id;
      });
      for (var i = 0, l = nodes.length; i < l; i++) {
        n += nodes[i].name;
        v += nodes[i].uuid + ';';
      }
      if (v.length > 0)
        v = v.substring(0, v.length - 1);
      var cityObj = $('#zTreeCitySel');
      cityObj.val(n);
      $('#selectGroup').val(v);
      $('#zTreeContent').hide();
    },
    ajaxDataFilter: function (treeId, parentNode, responseData) {
      addPersonnelInfo.hideErrorMsg();//清除错误提示样式
      var isAdminStr = $('#isAdmin').attr('value');    // 是否是admin
      var isAdmin = isAdminStr == 'true';
      var userGroupId = $('#userGroupId').attr('value');  // 用户所属组织 id
      var userGroupName = $('#userGroupName').attr('value');  // 用户所属组织 name
      //如果根企业下没有节点,就显示错误提示(根企业下不能人员)
      if (responseData != null && responseData != '' && responseData != undefined && responseData.length >= 1) {
        if ($('#selectGroup').val() == '') {
          $('#selectGroup').val(responseData[0].uuid);
          $('#zTreeCitySel').val(responseData[0].name);
        }
        return responseData;
      } else {
        addPersonnelInfo.showErrorMsg('您需要先新增一个组织', 'zTreeCitySel');
        return;
      }

    },
    showMenu: function (e) {
      if ($('#zTreeContent').is(':hidden')) {
        var width = $(e).parent().width();
        $('#zTreeContent').css('width', width + 'px');
        $(window).resize(function () {
          var width = $(e).parent().width();
          $('#zTreeContent').css('width', width + 'px');
        });
        $('#zTreeContent').show();
      } else {
        $('#zTreeContent').hide();
      }

      $('body').bind('mousedown', addPersonnelInfo.onBodyDown);
    },
    hideMenu: function () {
      $('#zTreeContent').fadeOut('fast');
      $('body').unbind('mousedown', addPersonnelInfo.onBodyDown);
    },
    onBodyDown: function (event) {
      if (!(event.target.id == 'menuBtn' || event.target.id == 'zTreeContent' || $(
        event.target).parents('#zTreeContent').length > 0)) {
        addPersonnelInfo.hideMenu();
      }
    },
    validates: function () {
      var adminFlag = $('#isAdmin').val() == 'true';
      return $('#addForm').validate({
        rules: {
          peopleNumber: {
            required: true,
            minlength: 2,
            maxlength: 20,
            checkRightPeopleNumber: true,
            remote: {
              type: 'post',
              async: false,
              url: '/clbs/talkback/basicinfo/monitoring/personnel/repetitionAdd',
              data: {
                peopleNumber: function () {
                  return $('#peopleNumber').val();
                }
              }
            }
          },
          groupName: {
            required: true,
            isGroupRequired: adminFlag
          },
          jobId: {
            required: true
          },
          phone: {
            isTel: true
          },
          remark: {
            maxlength: 50
          },
          identity: {
            isIdCardNo: true
          }
        },
        messages: {
          peopleNumber: {
            required: '监控对象不能为空',
            minlength: personnelNumberError,
            maxlength: personnelNumberError,
            checkRightPeopleNumber: personnelNumberError,
            remote: personnelNumberExists
          },
          groupName: {
            required: '请选择所属组织',
            isGroupRequired: publicSelectGroupNull
          },
          jobId: {
            required: '请选择职位类别'
          },
          phone: {
            isTel: telPhoneError
          },
          remark: {
            maxlength: publicSize50
          }
        }
      }).form();
    },
    doSubmits: function () {
      if (addPersonnelInfo.validates()) {
        var curSelect = $('.selectChange');
        for (var i = 0; i < curSelect.length; i++) {
          var selectInput = $(curSelect[i]).siblings('input.selectInput');
          selectInput.val($(curSelect[i]).find('option:selected').text());
        }
        $('#addForm').ajaxSubmit(function (data) {
          var json = eval('(' + data + ')');
          if (json.success) {
            layer.msg('新增成功<br>请在"对讲信息列表"中进行绑定操作!');
            $('#commonSmWin').modal('hide');
            myTable.requestData();
          } else {
            layer.msg(json.msg);
          }
        });
      }
    },
    showErrorMsg: function (msg, inputId) {
      if ($('#error_label_add').is(':hidden')) {
        $('#error_label_add').text(msg);
        $('#error_label_add').insertAfter($('#' + inputId));
        $('#error_label_add').show();
      } else {
        $('#error_label_add').is(':hidden');
      }
    },
    //错误提示信息隐藏
    hideErrorMsg: function () {
      $('#error_label_add').is(':hidden');
      $('#error_label_add').hide();
    }
  };
  $(function () {
    addPersonnelInfo.init();

    $('#skills,#driverCategory').bind('click', showMenuContent);

    $('#doSubmits').bind('click', addPersonnelInfo.doSubmits);
    $('#zTreeCitySel').on('click', function () {
      addPersonnelInfo.showMenu(this);
    });
    // 组织树input框的模糊搜索
    $('#zTreeCitySel').on('input propertychange', function (value) {
      var treeObj = $.fn.zTree.getZTreeObj('ztreeDemo');
      treeObj.checkAllNodes(false);
      search_ztree('ztreeDemo', 'zTreeCitySel', 'group');
    });
    // 组织树input框快速清空
    $('input').inputClear().on('onClearEvent', function (e, data) {
      var id = data.id;
      var treeObj;
      if (id == 'zTreeCitySel') {
        search_ztree('ztreeDemo', 'zTreeCitySel', 'group');
        treeObj = $.fn.zTree.getZTreeObj('ztreeDemo');
        treeObj.checkAllNodes(false);
      }
      if (id == 'jobName') {
        $('#jobId').val('');
      }
      if (id == 'skills') {
        addPersonnelInfo.skillTreeInit();
      }
      if (id == 'driverCategory') {
        addPersonnelInfo.driverTreeInit();
      }
    });
  });
})(window, $);

jQuery.validator.addMethod('email', function (value, element) {
  var tel = /^(\w)+(\.\w+)*@(\w)+((\.\w{2,3}){1,3})$/;
  return this.optional(element) || (tel.test(value));
}, '您输入的邮箱格式不正确');