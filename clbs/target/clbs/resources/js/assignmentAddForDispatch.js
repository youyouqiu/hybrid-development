(function ($, window) {
  var submissionFlag = false;
  var assignmentAdd = {
    //初始化
    init: function () {
      var setting = {
        async: {
          url: '/clbs/m/basicinfo/enterprise/professionals/tree',
          type: 'post',
          enable: true,
          autoParam: ['id'],
          contentType: 'application/json',
          dataType: 'json',
          dataFilter: assignmentAdd.ajaxDataFilter
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
          beforeClick: assignmentAdd.beforeClick,
          onClick: assignmentAdd.onClick,
          onNodeCreated: assignmentAdd.zTreeOnNodeCreated,
          onAsyncSuccess: assignmentAdd.zTreeOnAsyncSuccess

        }
      };
      $.fn.zTree.init($('#ztreeDemo'), setting, null);
    },
    zTreeOnNodeCreated: function (event, treeId, treeNode) {
      var id = treeNode.uuid.toString();
      if (id === $('#groupIdAdd').val()) {
        var treeObj = $.fn.zTree.getZTreeObj('ztreeDemo');
        treeObj.selectNode(treeNode, true, true);
      }
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
      var zTree = $.fn.zTree.getZTreeObj('ztreeDemo'), nodes = zTree
        .getSelectedNodes(), v = '';
      n = '';
      nodes.sort(function compare(a, b) {
        return a.id - b.id;
      });
      for (var i = 0, l = nodes.length; i < l; i++) {
        n += nodes[i].name;
        v += nodes[i].uuid + ',';
      }
      if (v.length > 0)
        v = v.substring(0, v.length - 1);
      var cityObj = $('#zTreeCitySel');
      $('#groupIdAdd').val(v);
      cityObj.val(n);
      $('#zTreeContent').hide();
    },
    showMenu: function (e) {
      var $zTreeContent = $('#zTreeContent');

      if ($zTreeContent.is(':hidden')) {
        var width = $(e).parent().width();
        $zTreeContent.css('width', width + 'px');

        $(window).resize(function () {
          var width = $(e).parent().width();
          $zTreeContent.css('width', width + 'px');
        });

        $zTreeContent.show();
      } else {
        $zTreeContent.hide();
      }

      $('body').bind('mousedown', assignmentAdd.onBodyDown);
    },
    hideMenu: function () {
      $('#zTreeContent').fadeOut('fast');
      $('body').unbind('mousedown', assignmentAdd.onBodyDown);
    },
    onBodyDown: function (event) {
      if (!(event.target.id === 'menuBtn' || event.target.id === 'zTreeContent' || $(event.target).parents('#zTreeContent').length > 0)) {
        assignmentAdd.hideMenu();
      }
    },
    //组织树预处理函数
    ajaxDataFilter: function (treeId, parentNode, responseData) {
      assignmentAdd.hideErrorMsg();//清除错误提示样式

      //如果根企业下没有节点,就显示错误提示(根企业下不能创建分组)
      if (responseData && responseData.length >= 1) {
        var $groupIdAdd = $('#groupIdAdd');

        if ($groupIdAdd.val() === '') {
          var uuid = responseData[0].uuid;
          var name = responseData[0].name;

          for (var i = 0, len = responseData.length; i < len; i++) {
            if (responseData[i].id === responseData[0].pid) {
              uuid = responseData[i].uuid;
              name = responseData[i].name;
              break;
            }
          }

          $groupIdAdd.val(uuid);
          $('#zTreeCitySel').val(name);
        }
        return responseData;
      } else {
        assignmentAdd.showErrorMsg('您需要先新增一个组织', 'zTreeCitySel');
      }
    },
    doSubmit: function () {
      if (!submissionFlag) {  // 防止重复提交
        if (assignmentAdd.validates()) {
          submissionFlag = true;
          addHashCode($("#addForm"));
          $('#addForm').ajaxSubmit(function (data) {
            var result = JSON.parse(data);

            if (result.success || result === 'true') {
              myTable.requestData();
            } else {
              layer.msg(result.msg);
            }

            /* 关闭弹窗 */
            $('#commonWin').modal('hide');
            submissionFlag = false;
          });
        }
      }
    },
    validates: function () {
      return $('#addForm').validate({
        rules: {
          name: {
            required: true,
            maxlength: 10,
            remote: {
              type: 'post',
              async: false,
              url: '/clbs/talkback/basicinfo/enterprise/assignment/repetition',
              data: {
                name: function () {
                  return $('#nameAdd').val();
                },
                group: function () {
                  return $('#groupIdAdd').val();
                }
              }
            }
          },
          groupName: {
            required: true,
            maxlength: 1000,
            assignmentLimit100: '#groupIdAdd'
          },
          contacts: {
            maxlength: 20
          },
          telephone: {
            isLandline: true
          },
          description: {
            maxlength: 50
          }
        },
        messages: {
          name: {
            required: groupNameNull,
            maxlength: publicSize10Length,
            remote: assignmentExists
          },
          groupName: {
            required: organizationNameNull,
            maxlength: publicSize1000Length,
            assignmentLimit100: assignmentExists100
          },
          contacts: {
            maxlength: publicSize20Length
          },
          telephone: {
            isLandline: telPhoneError
          },
          description: {
            maxlength: publicSize50Length
          }
        }
      }).form();
    },
    showErrorMsg: function (msg, inputId) {
      var $errorLabelAdd = $('#error_label_add');

      if ($errorLabelAdd.is(':hidden')) {
        $errorLabelAdd.text(msg);
        $errorLabelAdd.insertAfter($('#' + inputId));
        $errorLabelAdd.show();
      } else {
        $errorLabelAdd.is(':hidden');
      }
    },
    //错误提示信息隐藏
    hideErrorMsg: function () {
      var $errorLabelAdd = $('#error_label_add');

      $errorLabelAdd.is(':hidden');
      $errorLabelAdd.hide();
    }
  };

  $(function () {
    assignmentAdd.init();

    $('#doSubmitAdd').on('click', assignmentAdd.doSubmit);

    $('#zTreeCitySel').on('click', function () {
      assignmentAdd.showMenu(this);
    }).on('input propertychange', function (value) {
      var treeObj = $.fn.zTree.getZTreeObj('ztreeDemo');
      treeObj.checkAllNodes(false);
      search_ztree('ztreeDemo', 'zTreeCitySel', 'group');
    });

    $('input').inputClear().on('onClearEvent', function (e, data) {
      var id = data.id;
      var treeObj;
      if (id === 'zTreeCitySel') {
        search_ztree('ztreeDemo', 'zTreeCitySel', 'group');
        treeObj = $.fn.zTree.getZTreeObj('ztreeDemo');
      }
      treeObj.checkAllNodes(false);
    });
  });
})($, window);