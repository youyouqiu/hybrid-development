(function (window, $) {
  var jobs = $('#allJob').val() ? $('#allJob').val() : '[]';
  var allJob = JSON.parse(jobs); // 所有职位类别
  var skillStr = $('#skillTree').val();
  var skillTree = skillStr !== undefined && skillStr !== null && skillStr !== "" ? JSON.parse(skillStr) : []; // 技能树
  var allDriverType = JSON.parse($('#allDriverType').val()); // 驾驶证类别
  editPersonnelInfo = {
    init: function () {
      var bindId = $('#bindId').val();
      if (bindId != null && bindId != '') {
        $('#peopleNumber').attr('readonly', true);
        $('#bindMsg').attr('hidden', false);
      }
      var setting = {
        async: {
          url: '/clbs/m/basicinfo/enterprise/professionals/tree',
          type: 'post',
          enable: true,
          autoParam: ['id'],
          dataType: 'json',
          otherParam: {
            'vid': ''
          }
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
          beforeClick: editPersonnelInfo.beforeClick,
          onClick: editPersonnelInfo.onClick,
          onAsyncSuccess: editPersonnelInfo.zTreeOnAsyncSuccess
        }
      };
      $.fn.zTree.init($('#ztreeDemo'), setting, null);

      editPersonnelInfo.initJobSelect();
      editPersonnelInfo.skillTreeInit();
      editPersonnelInfo.driverTreeInit();

      if ($('#skillIds').val() != '') {
        var skillIds = $('#skillIds').val().split(',');
        editPersonnelInfo.ztreeCheckedInit(skillIds, 'skillsZtreeDemo');
      }
      if ($('#driverTypeIds').val() != '') {
        var driverTypeIds = $('#driverTypeIds').val().split(',');
        editPersonnelInfo.ztreeCheckedInit(driverTypeIds, 'driverCategoryZtreeDemo');
      }
    },
    // 组织树默认勾选
    ztreeCheckedInit: function (data, zTreeId) {
      var zTree = $.fn.zTree.getZTreeObj(zTreeId);
      for (var i = 0; i < data.length; i++) {
        var nodes = zTree.getNodesByParam('id', data[i], null);
        zTree.checkNode(nodes[0], true, true, true);
      }
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
      })
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
          onClick: editPersonnelInfo.onClickBack,
          onCheck: editPersonnelInfo.onCheckType
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
          onClick: editPersonnelInfo.onClickBack,
          onCheck: editPersonnelInfo.onCheckType,
          onNodeCreated: editPersonnelInfo.zTreeOnNodeCreated
        }
      };
      var zNodes = skillTree;
      $.fn.zTree.init($('#skillsZtreeDemo'), treeSetting, zNodes);
    },
    onClickBack: function (e, treeId, treeNode, clickFlag) {
      var zTreeObj = $.fn.zTree.getZTreeObj(treeId);
      zTreeObj.checkNode(treeNode, !treeNode.checked, true);
      editPersonnelInfo.onCheckType(e, treeId, treeNode);
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
      editPersonnelInfo.getTypeSelect(zTree, treeId);
      editPersonnelInfo.getTypeCheckedNodes(treeId);
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
    beforeClick: function (treeId, treeNode) {
      var check = (treeNode);
      return check;
    },
    onClick: function (e, treeId, treeNode) {
      var zTree = $.fn.zTree.getZTreeObj('ztreeDemo'),
        nodes = zTree.getSelectedNodes(),
        n = '';
      v = '';
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
      cityObj.attr('value', v);
      cityObj.val(n);
      $('#groupId').val(v);
      $('#zTreeContent').hide();
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

      $('body').bind('mousedown', editPersonnelInfo.onBodyDown);
    },
    hideMenu: function () {
      $('#zTreeContent').fadeOut('fast');
      $('body').unbind('mousedown', editPersonnelInfo.onBodyDown);
    },
    onBodyDown: function (event) {
      if (!(event.target.id == 'menuBtn' || event.target.id == 'zTreeContent' || $(
        event.target).parents('#zTreeContent').length > 0)) {
        editPersonnelInfo.hideMenu();
      }
    },
    validates: function () {
      var adminFlag = $('#isAdmin').val() == 'true';
      return $('#editForm').validate({
        rules: {
          peopleNumber: {
            required: true,
            minlength: 2,
            maxlength: 20,
            checkRightPeopleNumber: true,
            remote: {
              type: 'post',
              async: false,
              url: '/clbs/talkback/basicinfo/monitoring/personnel/repetitionEdit',
              dataType: 'json',
              data: {
                peopleNumber: function () {
                  return $('#peopleNumber').val();
                },
                id: function () {
                  return $('#id').val();
                }
              },
              dataFilter: function (data, type) {

                var data2 = data;
                if (data2 == 'true') {
                  return true;
                } else {
                  return false;
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
      if (editPersonnelInfo.validates()) {
        var curSelect = $('.selectChange');
        for (var i = 0; i < curSelect.length; i++) {
          var selectInput = $(curSelect[i]).siblings('input.selectInput');
          selectInput.val($(curSelect[i]).find('option:selected').text());
        }
        $('#editForm').ajaxSubmit(function (data) {
          var json = eval('(' + data + ')');
          if (json.success) {
            $('#commonSmWin').modal('hide');
            myTable.refresh();
          } else {
            layer.msg(json.msg);
          }
        });
      }
    }
  };
  $(function () {
    editPersonnelInfo.init();
    $('#zTreeCitySel').on('click', function () {
      editPersonnelInfo.showMenu(this);
    });

    $('#skills,#driverCategory').bind('click', showMenuContent);

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
      if (id == 'skills') {
        editPersonnelInfo.skillTreeInit();
      }
      if (id == 'driverCategory') {
        editPersonnelInfo.driverTreeInit();
      }
    });
    $('#doSubmits').bind('click', editPersonnelInfo.doSubmits);
  });
})(window, $);

jQuery.validator.addMethod('email', function (value, element) {
  var tel = /^(\w)+(\.\w+)*@(\w)+((\.\w{2,3}){1,3})$/;
  return this.optional(element) || (tel.test(value));
}, '您输入的邮箱格式不正确');