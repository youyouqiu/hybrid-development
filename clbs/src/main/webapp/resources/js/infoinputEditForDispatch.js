(function (window, $) {
  var submissionEditFlag = false;
  var checkedAssginmentEdit = $('#checkedAssginmentId').val().split(','); // 信息录入群组字段已被选中的数据
  $('#checkedAssginmentId').val(checkedAssginmentEdit.join(';')); // 把以逗号分割的分组ID数据字符串转换成以分号分割的字符串
  $('#editCitySelidVal').val(checkedAssginmentEdit.join(';')); // 把以逗号分割的分组ID数据字符串转换成以分号分割的字符串

  var editPublicFun = {
    ajaxDataFilter: function (treeId, parentNode, responseData) {
      var flag = true;
      if (responseData) {
        for (var i = 0; i < responseData.length; i++) {
          if (flag && responseData[i].type === 'assignment') {
            if (responseData[i].canCheck < 100) {
              responseData[i].checked = true;
              $('#editGroupId').val(responseData[i].name);
              $('#quickCitySelidVal').val(responseData[i].id);
              flag = false;
            }
          }
          responseData[i].open = true;
        }
      }
      return responseData;
    },
    // 清除错误信息
    clearErrorMsg: function () {
      $('label.error').hide();
    },
    // 显示错误提示信息
    showErrorMsg: function (msg, inputId) {
      var $error_label = $('#error_label');
      if ($error_label.is(':hidden')) {
        $error_label.text(msg);
        $error_label.insertAfter($('#' + inputId));
        $error_label.show();
      } else {
        $error_label.is(':hidden');
      }
    },
    //错误提示信息隐藏
    hideErrorMsg: function (id) {
      $('#' + id + '-error').hide();
    },
    //树点击之前事件
    beforeClick: function (treeId, treeNode) {
      var zTree = $.fn.zTree.getZTreeObj(treeId);
      zTree.checkNode(treeNode, !treeNode.checked, treeNode, true);
      return false;
    },
    onAsyncSuccess: function (event, treeId, treeNode, msg) {
      // 初始化群组节点选中数组
      var treeObj = $.fn.zTree.getZTreeObj(treeId);
      // var nodes = treeObj.getCheckedNodes(true);
      // for (var i = 0; i < nodes.length; i++) {
      //   if (nodes[i].type === 'assignment') {
      //     checkedAssginmentEdit.push(nodes[i]);
      //   }
      // }

      for (var i = 0; i < checkedAssginmentEdit.length; i++) {
        var nodesById = treeObj.getNodesByParam('id', checkedAssginmentEdit[i], null);
        if (nodesById.length > 0) {
          treeObj.checkNode(nodesById[0], true, true);
        }
      }
    },
    //树点击事件
    onClick: function (e, treeId, treeNode) {
      if (treeId !== undefined) { //快速录入
        var zTree = $.fn.zTree.getZTreeObj(treeId);
        var v = '';
        var t = '';
        if (treeNode.checked) { // 勾选操作才进行验证，取消勾选不进行验证
          // 获取群组和企业ID信息
          var caId;
          // 定义群组和企业唯一标识
          var caIdentification;
          var nodes = [];

          if (treeNode.type === 'assignment') { // 为群组节点直接校验
            caIdentification = 1;
            caId = treeNode.id;
            nodes.push(treeNode);
          } else if (treeNode.type === 'group') { // 为企业节点获取勾选节点然后去除校验过的节点
            caIdentification = 2;
            caId = treeNode.id;
            nodes = zTree.getCheckedNodes(true);

            var curArr = [];
            curArr = checkedAssginmentEdit;

            for (var h = 0; h < curArr.length; h++) {
              nodes.remove(curArr[h]);
            }
          }
          //获取还可录入的群组id
          editPublicFun.getAllAssignmentVehicleNumber(caId, caIdentification, $('#editIntercomForm #monitorId').val());
          nodes.sort(function compare(a, b) {
            return a.id - b.id;
          });

          var amtNames = ''; // 车辆数超过100的群组
          for (var i = 0, l = nodes.length; i < l; i++) {
            if (nodes[i].type === 'assignment') { // 选择的是群组，才组装值
              if (!editPublicFun.checkMaxVehicleCountOfAssignment(nodes[i].id)) {
                nodes[i].checked = false;
                amtNames += nodes[i].name + ',';
              }
            }
          }
          // 判断系统是否出问题
          if (systemErrorFlag) {
            layer.msg(systemError, {
              time: 1500
            });
            return;
          }

          if (amtNames.length > 0) {
            amtNames = amtNames.substr(0, amtNames.length - 1);
            layer.msg('【分组:' + amtNames + '】' + assignmentMaxCarNum);
          } else {
            editPublicFun.clearErrorMsg();
          }
        }

        // 组装校验通过的值，初始化节点选中数组
        checkedAssginmentEdit = [];
        var checkedNodes = zTree.getCheckedNodes(true);
        for (var i = 0; i < checkedNodes.length; i++) {
          if (checkedNodes[i].type === 'assignment') {
            t += checkedNodes[i].name + ',';
            v += checkedNodes[i].id + ';';

            checkedAssginmentEdit.push(checkedNodes[i]);
          }
        }

        if (v.length > 0) v = v.substring(0, v.length - 1);
        if (t.length > 0) t = t.substring(0, t.length - 1);
        $('#editGroupId').val(t);
        $('#editCitySelidVal').val(v);
      }
    },
    // 获取还可录入的群组id(校验群组车辆上限用)
    getAllAssignmentVehicleNumber: function (cdId, identifier, monitorId) {
      if (cdId && identifier) {
        var param = {
          id: cdId,
          type: identifier
        };

        if (monitorId) {
          param.monitorId = monitorId;
        }

        $.ajax({
          type: 'POST',
          // url: '/clbs/m/infoconfig/infoinput/getAssignmentCount',
          url: '/clbs/talkback/inconfig/infoinput/getAssignmentCount',
          dataType: 'json',
          data: param,
          async: false,
          success: function (data) {
            if (data.success) {
              ais = data.obj.ais;
            } else if (data.msg) {
              layer.msg(data.msg);
              systemErrorFlag = true;
            }
          },
          error: function () {
            layer.msg(systemError, {
              time: 1500
            });
            systemErrorFlag = true;
          }
        });
      }
    },
    // 校验当前群组下的车辆数是否已经达到最大值（最大值目前设定为：100台）
    checkMaxVehicleCountOfAssignment: function (assignmentId) {
      var b = true;
      if ($.inArray(assignmentId, ais) === -1) {
        b = false;
      }
      return b;
    }
  };

  intercomObjEdit = {
    init: function () {
      // 处理并初始化终端手机号数据
      var simList = JSON.parse($('#simCardInfoList').val());
      intercomObjEdit.simInit(simList);
      // 处理并初始化原始机型数据
      var originalModelList = JSON.parse($('#originalModelList').val());
      intercomObjEdit.originalModelInit(originalModelList);
      // 处理并初始化监控对象数据 车/人/物
      var vehicleInfoList = JSON.parse($('#vehicleInfoList').val());
      var peopleInfoList = JSON.parse($('#peopleInfoList').val());
      var thingInfoList = JSON.parse($('#thingInfoList').val());
      intercomObjEdit.monitorInit(vehicleInfoList, peopleInfoList, thingInfoList);
      // 群组树初始化
      intercomObjEdit.groupTreeInit();
      // 启用表单验证
      intercomObjEdit.validates();
    },
    simInit: function (dataList) {
      var list = {value: []};
      var i = dataList.length;
      while (i--) {
        list.value.push({
          name: dataList[i].simcardNumber,
          id: dataList[i].id
        });
      }

      $('#quickSimsContainerEdit').dropdown({
        data: list.value,
        pageCount: 50,
        listItemHeight: 31,
        onDataRequestSuccess: function (e, result) {
          $('#sims').removeAttr('disabled');
        },
        onSetSelectValue: function (e, keyword, data) {
          $('#simcardId').val(keyword.id);
          $('#sims').val(keyword.name);
          $("#sims").closest('.form-group').find('.dropdown-menu').hide();
          editPublicFun.hideErrorMsg('sims');
        }
      });

      // $('#sims').bsSuggest({
      //   idField: 'id',
      //   keyField: 'name',
      //   effectiveFields: ['name'],
      //   searchFields: ['id'],
      //   data: list
      // }).on('onSetSelectValue', function (e, keyword) {
      //   $('#simcardId').val(keyword.id);
      //   $('#sims').val(keyword.key);
      //
      //   editPublicFun.hideErrorMsg('sims');
      // }).on('input propertychange', function () {
      //   $('#simcardId').val('');
      //   editPublicFun.hideErrorMsg('sims');
      // });
    },
    originalModelInit: function (dataList) {
      var list = {value: []};
      var i = dataList.length;
      while (i--) {
        list.value.push({
          name: dataList[i].modelId,
          id: dataList[i].index
        });
      }

      $('#originalModel').bsSuggest({
        idField: 'id',
        keyField: 'name',
        effectiveFields: ['name'],
        searchFields: ['id'],
        data: list
      }).on('onSetSelectValue', function (e, keyword) {
        $('#originalModelId').val(keyword.id);
        $('#originalModel').val(keyword.key);
      }).on('focus', function () {
        $(this).siblings('i.delIcon').remove();
      });
    },
    monitorInit: function (vehicleInfoList, peopleInfoList, thingInfoList) {
      var list = {value: []};
      var monitorType = $('#monitorType').val();
      var dataList = [];

      if (+monitorType === 0) {
        dataList = vehicleInfoList;
      } else if (+monitorType === 1) {
        dataList = peopleInfoList;
      } else if (+monitorType === 2) {
        dataList = thingInfoList;
      }

      var i = dataList.length;
      while (i--) {
        list.value.push({
          name: dataList[i].brand,
          id: dataList[i].id
        });
      }

      $('#monitorName').bsSuggest({
        idField: 'id',
        keyField: 'name',
        effectiveFields: ['name'],
        searchFields: ['id'],
        data: list
      }).on('onSetSelectValue', function (e, keyword) {
        $('#monitorId').val(keyword.id);
        $('#monitorName').val(keyword.key);
        editPublicFun.hideErrorMsg('monitorName');
      }).on('input propertychange', function () {
        $('#monitorId').val('');
        editPublicFun.hideErrorMsg('monitorName');
      });
    },
    groupTreeInit: function () {
      var setting = {
        async: {
          url: '/clbs/talkback/basicinfo/enterprise/assignment/assignmentTree',
          type: 'post',
          enable: true,
          autoParam: ['id'],
          contentType: 'application/json',
          dataType: 'json',
          dataFilter: editPublicFun.ajaxDataFilter
        },
        check: {
          enable: true,
          chkStyle: 'checkbox',
          chkboxType: {
            'Y': 's',
            'N': 's'
          },
          radioType: 'all'
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
          beforeClick: editPublicFun.beforeClick,
          onClick: editPublicFun.onClick,
          onCheck: editPublicFun.onClick,
          onAsyncSuccess: editPublicFun.onAsyncSuccess
        }
      };

      $.fn.zTree.init($('#editTree'), setting, null);
    },
    validates: function () {
      $('#editIntercomForm').validate({
        rules: {
          sims: {
            required: true,
            isIntercomSim: true,
            minlength: 7,
            maxlength: 20,
            digits: true,
            remote: {
              type: 'post',
              async: false,
              dataType: 'json',
              url: '/clbs/m/basicinfo/equipment/simcard/repetition', // 检查是否已存在
              data: {
                simcardNumber: function () {
                  return $('#sims').val();
                }
              },
              dataFilter: function (data) {
                var simCardId = $('#simcardId').val();
                if ((!data || data === 'false') && !simCardId) {
                  return false;
                } else {
                  editPublicFun.hideErrorMsg();
                  return true;
                }
              }
            }
          },
          modelId: {
            required: true
          },
          devices: {
            required: true,
            minlength: 7,
            intercomTerminalNo: true
          },
          devicePassword: {
            required: true,
            devicePwd: true,
            minlength: 8
          },
          brands: {
            required: true,
            monitorForDispatch: true
          },
          assignmentName: {
            required: true
          }
        },
        messages: {
          sims: {
            required: simNumberSelect,
            minlength: simNumberError,
            maxlength: simNumberError,
            digits: simNumberError,
            isIntercomSim: simFirstNumberNot0,
            remote: simNumberExists
          },
          modelId: {
            required: originalModelNull
          },
          devices: {
            required: equipmentIdentityNull,
            minlength: equipmentIdentityLegality,
            intercomTerminalNo: equipmentIdentityLegality
          },
          devicePassword: {
            required: quickDevicePasswordNull,
            devicePwd: quickDevicePasswordLegality,
            minlength: quickDevicePasswordLegality
          },
          brands: {
            required: '请新增或选择监控对象',
            monitorForDispatch: '请输入 2-20 位中文、字母、数字或短横杠'
          },
          assignmentName: {
            required: groupsNameNull
          }
        },
        onkeyup: false
      }).form();
    },
    doSubmit: function () {
      if ($('#editIntercomForm').valid() && !submissionEditFlag) {
        submissionEditFlag = true;
        $('#editIntercomForm').ajaxSubmit(function (data) {
          submissionEditFlag = false;

          var json = eval('(' + data + ')');
          if (json.success) {
            $('#commonSmWin').modal('hide');
            myTable.requestData();
          } else {
            layer.msg(json.msg || '修改监控对象失败');
          }
        });
      }
    }
  };

  $(function () {
    intercomObjEdit.init();
    $('[data-toggle=\'tooltip\']').tooltip();

    // 功能复选框事件
    $('#txtInfo').on('change', function (e) {
      $(this).attr('value', e.target.checked ? 1 : 0);
    });
    $('#imgInfo').on('change', function (e) {
      $(this).attr('value', e.target.checked ? 1 : 0);
    });
    $('#offlineVoiceInfo').on('change', function (e) {
      $(this).attr('value', e.target.checked ? 1 : 0);
    });

    // $('#editGroupId').on('input propertychange', function () {
    //   var treeObj = $.fn.zTree.getZTreeObj('editTree');
    //   treeObj.checkAllNodes(false);
    //   $('#editCitySelidVal').val('');
    //   search_ztree('editTree', 'editGroupId', 'assignment');
    // });
    $('#sims').on('input propertychange', function () {
      $('#simcardId').val('');
      editPublicFun.hideErrorMsg('sims');
    });

    // 群组树点击事件
    $('#editGroupId').on('click', showMenuContent);

    $('#doSubmit').on('click', intercomObjEdit.doSubmit);
  });
})(window, $);