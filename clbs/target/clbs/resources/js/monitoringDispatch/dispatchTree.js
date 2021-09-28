var DispatchTree = function (options) {
  this._dispatchModule = options.dispatchModule;
  this.options = options;
  this.monitorNodes = {};
  this.assignmentNodes = {};
  // this.group = null;
  this.taskAssignmentNode = null;
  this.taskGroupNode = null;
  this.queryType = 'name';
  this.treeSearchTimeout = null;
  this.beforeAsync = true;
  this.fixedBeforeAsync = true;
  this.needFilterInterlocutorId = null;
  this.loadDispatchTreeType = 0;
  this.tempNodesInterlocutorIds = [];
  this.isInitAddTempGroup = false;
  this.isLoadTree = false;
  this.init();
};

// 防止快速点击分组复选框,会请求多次后台接口导致分组下有重复对讲对象
var quickRepeatClickCheck = true;

DispatchTree.prototype = {
  constructor: DispatchTree,
  // 初始化方法
  init: function () {
    var $this = this;
    $('#loadTreeStatus').on('click', $this.loadTreeStatusHandler.bind($this));
    // $('#searchCondition').inputClear().on('onClearEvent', $this.searchConditionClearHandler.bind($this));
    $('#searchType').on('change', $this.searchTypeChangeHandler.bind($this));
    $('#searchTree').on('click', $this.searchTreeHandler.bind($this));
  },
  /**
   * 初始化组织树
   * * @param type 对讲对象状态 0:全部; 1:在线; 2:离线
   */
  initTree: function (params) {
    var $this = this;
    var options = $this.options;
    var elementId = options.elementId;
    var setting = {
      async: {
        url: params.url,
        type: 'post',
        enable: true,
        autoParam: ['id'],
        dataType: 'json',
        otherParam: params.data,
        dataFilter: $this.ajaxDataFilter.bind($this)
      },
      check: {
        enable: false,
        chkStyle: 'checkbox',
        chkboxType: {
          'Y': 's',
          'N': 's'
        },
        radioType: 'all'
      },
      view: {
        addHoverDom: $this.addHoverDom.bind($this),
        removeHoverDom: $this.removeHoverDom.bind($this),
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
        beforeAsync: $this.zTreeBeforeAsync.bind($this),
        onClick: $this.ztreeOnClick.bind($this),
        beforeExpand: $this.zTreeBeforeExpand.bind($this),
        onExpand: $this.ztreeOnExpand.bind($this),
        onAsyncSuccess: $this.zTreeOnAsyncSuccess.bind($this)
      }
    };
    $.fn.zTree.init($('#' + elementId), setting, null);
  },
  /**
   * 对讲对象组织树加载成功事件
   */
  zTreeOnAsyncSuccess: function () {
    this.addTempNodes();
  },
  /**
   * 组织树请求回调
   * @param treeId
   * @param parentNode
   * @param responseData
   * @returns {*}
   */
  ajaxDataFilter: function (treeId, parentNode, responseData) {
    var $this = this;
    if (responseData.success) {
      responseData = JSON.parse(ungzip(responseData.obj));
      $this.treeNodesClassify(responseData);
      responseData = $this.dispacthTreeNodeStatus(responseData);
      return responseData;
    }
    return null;
  },
  /**
   * 对讲对象组织树初始用户状态组装
   */
  dispacthTreeNodeStatus: function (data) {
    data.map(function (item) {
      if (item.type !== 'group' && item.type !== 'assignment') {
        item.iconSkin = item.audioOnlineStatus === 0 ? 'peopleSkin' : 'onlineDriving';
      }
      // if (item.type === 'assignment') {
      //   if (item.mNum !== null && item.mNum !== undefined) {
      //     item.name = item.name + ' (' + item.mNum + ')';
      //   }
      // }
    });
    return data;
  },
  /**
   * 组织树数据分类
   * @param data
   */
  treeNodesClassify: function (data) {
    var $this = this;
    // $this._dispatchModule.get('data').setTemporaryGroupNode(null);
    // $this._dispatchModule.get('data').setTaskGroupNode(null);
    $this._dispatchModule.get('data').clearGroupNode();
    $this._dispatchModule.get('data').clearAssignmentNode();
    $this._dispatchModule.get('data').clearMonitorNode();
    var groupNodesList = {};
    var assignmentNodesList = {};
    var monitorNodesList = {};
    data.map(function (item) {
      var type = item.type;
      if (type == 'group') {
        if (item.id === 'ou=temporaryOrganization,ou=Enterprise_top,ou=organization') {
          $this._dispatchModule.get('data').setTemporaryGroupNode(item);
        }
        if (item.id === 'ou=taskOrganization,ou=Enterprise_top,ou=organization') {
          $this._dispatchModule.get('data').setTaskGroupNode(item);
        }
        groupNodesList[item.id] = item;
      } else if (type === 'assignment') {
        assignmentNodesList[item.intercomGroupId] = item;
      } else {
        monitorNodesList[item.interlocutorId] = item;
      }
    });
    $this._dispatchModule.get('data').setGroupNodesList(groupNodesList);
    $this._dispatchModule.get('data').setAssignmentNodesList(assignmentNodesList);
    $this._dispatchModule.get('data').setMonitorNodesList(monitorNodesList);
  },
  /**
   * 鼠标移动到节点上时，显示用户自定义控件
   * @param treeId
   * @param treeNode
   */
  addHoverDom: function (treeId, treeNode) {
    var $this = this;
    var type = treeNode.type;
    var elementId = $('#' + treeNode.tId + '_span');
    var name = treeNode.name;
    var pId = treeNode.pId;

    // 显示新增任务组/临时组按钮
    if (type === 'group' && (name === '临时组' || name === '任务组')) {
      var addGroupElementId = 'addGroupBtn_' + treeNode.tId;

      if (!$('#' + addGroupElementId).length) {
        var addGroupElement = '<span class="button add" id="' + addGroupElementId + '" title="新增"></span>';
        elementId.after(addGroupElement);
        $('#' + addGroupElementId).on('click', $this.taskGroup.bind($this, treeNode));
      }
    }
    // 显示任务组/临时组的新增监控对象按钮和解散按钮
    else if (type === 'assignment' && (pId === 'ou=temporaryOrganization,ou=Enterprise_top,ou=organization' || pId === 'ou=taskOrganization,ou=Enterprise_top,ou=organization')) {
      var addMonitorElementId = 'addMonitorBtn_' + treeNode.tId;
      var deleteGroupElementId = 'deleteGroupElement_' + treeNode.tId;

      // 解散按钮
      if (!$('#' + deleteGroupElementId).length) {
        var deleteGroupElement = '<span class="button remove" id="' + deleteGroupElementId + '" title="解散"></span>';
        elementId.after(deleteGroupElement);
        $('#' + deleteGroupElementId).on('click', $this.deleteGroup.bind($this, treeNode));
      }

      // 添加监控对象按钮
      if (!$('#' + addMonitorElementId).length) {
        var addMonitorElement = '<span class="button add" id="' + addMonitorElementId + '" title="新增"></span>';
        elementId.after(addMonitorElement);
        $('#' + addMonitorElementId).on('click', $this.groupsWithMonitor.bind($this, treeNode));
      }
    }
    // 单个监控对象移除按钮
    else if (type === 'people' || type === 'vehicle' || type === 'thing') {
      var deleteMonitorElementId = 'deleteMonitorBtn_' + treeNode.tId;

      if (!$('#' + deleteMonitorElementId).length) {
        var options = $this.options;
        var treeObj = $.fn.zTree.getZTreeObj(options.elementId);
        var parentNode = treeObj.getNodeByParam('id', treeNode.pId, null);

        if (parentNode.pId === 'ou=temporaryOrganization,ou=Enterprise_top,ou=organization' || parentNode.pId === 'ou=taskOrganization,ou=Enterprise_top,ou=organization') {
          var deleteMonitorElement = '<span class="button kicked-out" id="' + deleteMonitorElementId + '" title="踢出"></span>';
          elementId.after(deleteMonitorElement);
          $('#' + deleteMonitorElementId).on('click', $this.deleteMonitor.bind($this, treeNode));
        }
      }
    }
  },
  /**
   * 鼠标移出节点时，隐藏用户自定义控件
   * @param treeId
   * @param treeNode
   */
  removeHoverDom: function (treeId, treeNode) {
    $('#addGroupBtn_' + treeNode.tId).unbind().remove();
    $('#addMonitorBtn_' + treeNode.tId).unbind().remove();
    $('#deleteGroupElement_' + treeNode.tId).unbind().remove();
    $('#deleteMonitorBtn_' + treeNode.tId).unbind().remove();
  },
  /**
   * 创建任务组
   * @param treeNode
   */
  taskGroup: function (treeNode) {
    var $this = this;
    $this.taskGroupNode = treeNode;
    $this._dispatchModule.get('data').setAddType(1);
    $this._dispatchModule.get('data').setAddAssignmentNode(treeNode);
    $('#selectedModel').show();
    $this._dispatchModule.get('dispatchTaskGroup').isCreateTaskGroup(true, treeNode);
    return false;
  },
  /**
   * 群组加人
   * @param treeNode
   */
  groupsWithMonitor: function (treeNode) {
    var $this = this;
    var params = {
      assignmentId: treeNode.pId === 'ou=temporaryOrganization,ou=Enterprise_top,ou=organization' ? treeNode.intercomGroupId
        : treeNode.id,
      assignmentType: treeNode.pId === 'ou=temporaryOrganization,ou=Enterprise_top,ou=organization' ? '3' : '2'
    };
    $this._dispatchModule.get('dispatchServices').judgeAssignmentIfJoinMonitor(
      params,
      function (data) {
        if (data.success) {
          var obj = data.obj;
          if (obj) {
            $this.taskAssignmentNode = treeNode;
            $this._dispatchModule.get('data').setAddType(2);
            $this._dispatchModule.get('data').setAddAssignmentNode(treeNode);
            $('#selectedModel').show();
            $this._dispatchModule.get('dispatchTaskGroup').isCreateTaskGroup(false, treeNode);
          } else {
            layer.msg('群组已达到最大400人的限制，无法再加入人员', {offset: 't'});
          }
        }
      }
    );
    return false;
  },
  /**
   * 解散群组
   * @param treeNode
   */
  deleteGroup: function (treeNode) {
    var $this = this;
    layer.confirm('您确定要解散该群组吗？', {
      title: '操作确认',
      icon: 3, // 问号图标
      move: false, //禁止拖动
      btn: ['确定', '取消']
    }, function () {
      var taskGroupNode = $this._dispatchModule.get('data').getTaskGroupNode();
      var dispatchNode = $this._dispatchModule.get('data').getDispatchNode();

      if (taskGroupNode.id === treeNode.pId) { // 解散任务组
        var params = {
          assignmentId: treeNode.id,
          assignmentType: 2
        };

        $this._dispatchModule.get('dispatchServices').unbindAssignmentAndMonitor(
          params,
          function (data) {
            if (data.success) {
              layer.msg('解散任务组成功', {offset: 't'});
              var options = $this.options;
              var treeObj = $.fn.zTree.getZTreeObj(options.elementId);
              treeObj.removeNode(treeNode);

              if (dispatchNode) {
                if (dispatchNode.defaultGroupId === treeNode.intercomGroupId ||
                  dispatchNode.intercomGroupId === treeNode.intercomGroupId
                ) {
                  $this._dispatchModule.get('dispatch').dispatchViewDefaultStatus();
                }
              }
            } else {
              layer.msg('解散任务组失败', {offset: 't'});
            }
          }
        );
      } else { // 解散临时组
        var loginResponseStatus = $this._dispatchModule.get('dispatchWebServices').loginResponseStatus;

        if (loginResponseStatus) {
          $this._dispatchModule.get('dispatchWebServices').deleteTempGroup(treeNode.intercomGroupId);

          if (dispatchNode) {
            if (dispatchNode.defaultGroupId === treeNode.intercomGroupId ||
              dispatchNode.intercomGroupId === treeNode.intercomGroupId
            ) {
              $this._dispatchModule.get('dispatch').dispatchViewDefaultStatus();
            }
          }
        } else {
          layer.msg('服务异常，请稍后重试', {offset: 't'});
        }
      }

      layer.closeAll();
    }, function () {

    });
    return false;
  },
  /**
   * 踢出监控对象
   * @param treeNode
   */
  deleteMonitor: function (treeNode) {
    var $this = this;
    layer.confirm('您确定要将该监控对象踢出群组吗？', {
      title: '操作确认',
      icon: 3, // 问号图标
      move: false,//禁止拖动
      btn: ['确定', '取消']
    }, function () {
      var options = $this.options;
      var treeObj = $.fn.zTree.getZTreeObj(options.elementId);
      var taskGroupNode = $this._dispatchModule.get('data').getTaskGroupNode();
      var dispatchNode = $this._dispatchModule.get('data').getDispatchNode();
      var pNode = treeObj.getNodeByParam('id', treeNode.pId, null);
      if (taskGroupNode.id === pNode.pId) { // 踢出任务组内对讲对象
        var params = {
          assignmentId: pNode.id,
          interlocutorId: treeNode.interlocutorId
        };
        $this._dispatchModule.get('dispatchServices').removeTaskAssignmentInterlocutor(
          params,
          function (data) {
            if (data.success) {
              layer.msg('踢出成功', {offset: 't'});
              if (dispatchNode !== null && dispatchNode !== undefined) {
                if (dispatchNode.interlocutorId === treeNode.interlocutorId) {
                  $this._dispatchModule.get('dispatch').dispatchViewDefaultStatus();
                }
              }
            } else {
              layer.msg('踢出失败', {offset: 't'});
            }
          }
        );
      } else { // 踢出临时组对讲对象
        var loginResponseStatus = $this._dispatchModule.get('dispatchWebServices').loginResponseStatus;
        if (loginResponseStatus) {
          $this._dispatchModule.get('dispatchWebServices').removeTempGroupMember(
            pNode.intercomGroupId,
            [treeNode.interlocutorId]
          );
          var params = {
            intercomGroupId: pNode.intercomGroupId,
            interlocutorId: treeNode.interlocutorId
          };
          $this._dispatchModule.get('dispatchServices').removeTemporaryAssignmentInterlocutorRecordLog(
            params,
            function (data) {
              if (data.success) {
                layer.msg('踢出成功', {offset: 't'});
                if (dispatchNode !== null && dispatchNode !== undefined) {
                  if (dispatchNode.interlocutorId === treeNode.interlocutorId) {
                    $this._dispatchModule.get('dispatch').dispatchViewDefaultStatus();
                  }
                }
                // treeObj.removeNode(treeNode);
              } else {
                layer.msg('踢出失败', {offset: 't'});
              }
            }
          );
        } else {
          layer.msg('服务异常，请稍后重试', {offset: 't'});
        }
      }
      layer.closeAll(); // 关闭layer
    }, function () {

    });
    return false;
  },
  /**
   * 获取临时组数据
   */
  getTemporaryGroupData: function () {
    var $this = this;
    var params = {
      url: '/clbs/talkback/monitoring/dispatch/getInterlocutorTree',
      data: {'interlocutorStatus': 0}
    };
    $this.initTree(params);
  },
  /**
   * 任务组添加
   */
  addTaskGroup: function (nodes) {
    var $this = this;
    var options = $this.options;
    var treeObj = $.fn.zTree.getZTreeObj(options.elementId);
    treeObj.addNodes($this.taskGroupNode, nodes);
  },
  /**
   * 任务组人员添加
   */
  addAssignmentMembers: function (nodes) {
    var $this = this;
    var options = $this.options;
    var treeObj = $.fn.zTree.getZTreeObj(options.elementId);
    treeObj.addNodes($this.taskAssignmentNode, nodes);
  },
  /**
   * 确定创建任务组
   */
  taskGroupDataAddHandler: function () {
    $('#taskGroupBackgroud').hide();
    var type = this._dispatchModule.get('data').getAddType();
    if (type === 1) {
      var temporaryGroupData = [
        {
          id: 112,
          pId: 11,
          type: 'assignment',
          type: 'assignment',
          iconSkin: 'assignmentSkin',
          name: '任务组二',
          open: true
        },
        {id: 1111, pId: 112, name: '王力宏1', type: 'people', iconSkin: 'peopleSkin'},
        {id: 1112, pId: 112, name: '张学友1', type: 'people', iconSkin: 'peopleSkin'},
        {id: 1113, pId: 112, name: '王力宏12', type: 'people', iconSkin: 'peopleSkin'},
        {id: 1114, pId: 112, name: '张学友12', type: 'people', iconSkin: 'peopleSkin'}
      ];
      this.addTaskGroup(temporaryGroupData);
    } else if (type === 2) {
      var temporaryGroupData = [
        {id: 1111, pId: 112, name: '王力宏8', type: 'people', iconSkin: 'peopleSkin'},
        {id: 1112, pId: 112, name: '张学友8', type: 'people', iconSkin: 'peopleSkin'}
      ];
      this.addAssignmentMembers(temporaryGroupData);
    }
    layer.msg('任务组xxx创建成功', {offset: 't'});
  },
  /**
   * 固定对象选择组织树加载
   */
  fixedConditionsTreeLoad: function () {
    var $this = this;
    $this.fixedBeforeAsync = true;
    var type = $this._dispatchModule.get('data').getAddType();
    var assignmentNode = $this._dispatchModule.get('data').getAddAssignmentNode();
    var taskGroupNode = $this._dispatchModule.get('data').getTaskGroupNode();
    var assignmentType = taskGroupNode.id === assignmentNode.pId ? 2 : 3;
    // var options = $this.options;
    var setting = {
      async: {
        url: '/clbs/talkback/monitoring/dispatch/findInterlocutorByFixedInterlocutor',
        type: 'post',
        enable: true,
        autoParam: ['id'],
        dataType: 'json',
        otherParam: {
          assignmentId: type === 2 ? assignmentNode.id : null,
          assignmentType: type === 2 ? assignmentType : null
        },
        dataFilter: $this.fixedConditionsAjaxDataFilter.bind($this)
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
        beforeAsync: $this.fixedZTreeBeforeAsync.bind($this),
        beforeExpand: $this.fixedZTreeBeforeExpand.bind($this),
        onExpand: $this.fixedZtreeOnExpand.bind($this),
        onAsyncSuccess: $this.fixedZTreeOnAsyncSuccess.bind($this),
        beforeCheck: $this.fixedZTreeBeforeCheck.bind($this),
        onCheck: $this.fixedZTreeOnCheck.bind($this)
      }
    };
    $.fn.zTree.init($('#taskGroupTree'), setting, null);
  },
  /**
   * 固定条件组织树加载数据
   * @param treeId
   * @param parentNode
   * @param responseData
   */
  fixedConditionsAjaxDataFilter: function (treeId, parentNode, responseData) {
    this.needFilterInterlocutorId = responseData.obj.needFilterInterlocutorId;
    responseData = JSON.parse(ungzip(responseData.obj.treeInfo));
    responseData = this.dispatchObjStatusHandler(responseData);
    return responseData;
  },
  /**
   * 固定对象组织树加载数据加载成功数据
   * 用于展开第一个分组
   */
  fixedZTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
    // var nodes = JSON.parse(ungzip(msg.obj.treeInfo));
    // var treeObj = $.fn.zTree.getZTreeObj('taskGroupTree');
    // for (var i = 0; i < nodes.length; i++) {
    //   if (nodes[i].type === 'assignment') {
    //     var node = treeObj.getNodeByParam('id', nodes[i].id);
    //     treeObj.expandNode(node, true, false, true, true);
    //     break;
    //   }
    // }
  },
  fixedZTreeBeforeCheck: function () {
    this.fixedBeforeAsync = false;
  },
  /**
   * 固定对象组织树勾选事件
   */
  fixedZTreeOnCheck: function (event, treeId, treeNode) {
    var $this = this;
    var treeObj = $.fn.zTree.getZTreeObj('taskGroupTree');

    if (treeNode.type === 'assignment' && !treeNode.children) {
      if (!treeNode.checked) {
        return;
      }
      if (!quickRepeatClickCheck) {
        return;
      }
      quickRepeatClickCheck = false;
      this._dispatchModule.get('dispatchServices').searchGroupOfList(
        {
          intercomGroupId: treeNode.intercomGroupId,
          interlocutorStatus: 1
        },
        function (data) {
          if (data.success) {
            var obj = data.obj;
            var list = [];
            obj.map(function (item) {
              if ($this.needFilterInterlocutorId.indexOf(item.userId) === -1) {
                list.push({
                  type: item.type,
                  name: item.userName,
                  pId: treeNode.id,
                  checked: true,
                  audioOnlineStatus: item.audioOnlineStatus,
                  defaultGroupId: item.defaultGroupId,
                  interlocutorId: item.userId,
                  iconSkin: item.audioOnlineStatus === 0 ? 'peopleSkin' : 'onlineDriving'
                });
              }
            });
            treeObj.addNodes(treeNode, list);
            treeObj.expandNode(treeNode, true, false, true);
            // setTimeout(function () {
            //   $this.fixedBeforeAsync = true;
            // }, 250);
            // var num = Number($('#checkedPersonNumber').text());
            // $('#checkedPersonNumber').text(num + list.length);

            /**
             * 判断勾选的监控对象是否超过400个
             */
            var checkedNodes = treeObj.getCheckedNodes(true);
            var number = 0;
            checkedNodes.map(function (item) {
              if (item.type !== 'group' && item.type !== 'assignment') {
                number += 1;
              }
            });

            var type = $this._dispatchModule.get('data').getAddType();
            var assignmentNode = $this._dispatchModule.get('data').getAddAssignmentNode();
            var taskGroupNode = $this._dispatchModule.get('data').getTaskGroupNode();
            if ((type === 1 && assignmentNode.id === taskGroupNode.id) ||
              (type !== 1 && assignmentNode.pId === taskGroupNode.id)) {
              if ($this.needFilterInterlocutorId.length + number > 400) {
                layer.msg('数量超过限制，请重新选择', {offset: 't'});
                treeObj.checkNode(treeNode, false, true);
                $('#checkedPersonNumber').text(number - list.length);
                return false;
              }
            }
            $('#checkedPersonNumber').text(number);
          }
          quickRepeatClickCheck = true;
        },
        true
      );
      // treeObj.expandNode(treeNode, true, false, true, true);
    } else if (treeNode.type === 'people' || treeNode.type === 'vehicle' || treeNode.type === 'thing') {
      /**
       * 判断勾选的监控对象是否超过400个
       */
      var checkedNodes = treeObj.getCheckedNodes(true);
      var number = 0;
      checkedNodes.map(function (item) {
        if (item.type !== 'group' && item.type !== 'assignment') {
          number += 1;
        }
      });
      var type = $this._dispatchModule.get('data').getAddType();
      var assignmentNode = $this._dispatchModule.get('data').getAddAssignmentNode();
      var taskGroupNode = $this._dispatchModule.get('data').getTaskGroupNode();

      if ((type === 1 && assignmentNode.id === taskGroupNode.id) || (type !== 1 && assignmentNode.pId === taskGroupNode.id)) {
        if ($this.needFilterInterlocutorId.length + number > 400) {
          layer.msg('数量超过限制，请重新选择', {offset: 't'});
          $('#checkedPersonNumber').text(number - 1);
          treeObj.checkNode(treeNode, false, true);
          return false;
        }
      }
      $('#checkedPersonNumber').text(number);
    } else {
      var checkedNodes = treeObj.getCheckedNodes(true);
      var number = 0;
      checkedNodes.map(function (item) {
        if (item.type !== 'group' && item.type !== 'assignment') {
          number += 1;
        }
      });

      var type = $this._dispatchModule.get('data').getAddType();
      var assignmentNode = $this._dispatchModule.get('data').getAddAssignmentNode();
      var taskGroupNode = $this._dispatchModule.get('data').getTaskGroupNode();

      if ((type === 1 && assignmentNode.id === taskGroupNode.id) || (type !== 1 && assignmentNode.pId === taskGroupNode.id)) {
        if ($this.needFilterInterlocutorId.length + number > 400) {
          layer.msg('数量超过限制，请重新选择', {offset: 't'});
          treeObj.checkNode(treeNode, false, true);
          $('#checkedPersonNumber').text(number - treeNode.children.length);
          return false;
        }
      }

      $('#checkedPersonNumber').text(number);
    }
  },
  /**
   * 异步加载之前的事件回调函数
   * @returns {boolean}
   */
  zTreeBeforeAsync: function () {
    return this.beforeAsync;
  },
  /**
   * 左侧组织树监控对象单击事件
   * @param event
   * @param treeId
   * @param treeNode
   */
  ztreeOnClick: function (event, treeId, treeNode) {
    var type = treeNode.type;
    if (type !== 'group') {
      var flag = $('#groupCalling').hasClass('active') || // group组呼是否有对象处于激活状态
        $('#groupListening').hasClass('active') || // group加入群组是否处于激活状态
        $('#robToMak').hasClass('active') || // group抢麦是否有对象处于激活状态
        $('#monitorCalling').hasClass('active') || // 车/人/物 个呼是否有对象处于激活状态
        $('#monitorPhoneEvent').hasClass('active') || // 车/人/物 拨打电话是否有对象处于激活状态
        $('#banned').hasClass('active'); // 车/人/物 禁言是否有对象处于激活状态

      if (type !== 'assignment' && type !== 'group') {
        if (flag) {
          var name = $('#callObjName').text();
          if (treeNode.name !== name) {
            layer.msg('请先停止对' + name + '的调度操作', {offset: 't'});
          }
        } else {
          this._dispatchModule.get('data').setDispatchNode(treeNode);
          this._dispatchModule.get('data').setDispatchType(1);
          this._dispatchModule.get('data').setSchedulingViewState(true);
        }
        this._dispatchModule.get('dispatchAmap').getMapPonitInfo(treeNode.interlocutorId);
      } else if (type === 'assignment') {
        if (flag) {
          var name = $('#callObjName').text();
          if (treeNode.name !== name) {
            layer.msg('请先停止对' + name + '的调度操作', {offset: 't'});
          }
        } else {
          this._dispatchModule.get('data').setDispatchNode(treeNode);
          this._dispatchModule.get('data').setDispatchType(2);
          this._dispatchModule.get('data').setSchedulingViewState(true);
        }
      }
    }
  },
  /**
   * 组织树模糊查询
   * @param value
   */
  fuzzySearchInterlocutor: function (value) {
    var params = {
      url: '/clbs/talkback/monitoring/dispatch/fuzzySearchInterlocutor',
      data: {
        interlocutorStatus: 0,
        queryParam: value,
        queryType: this.queryType
      }
    };
    this.fuzzyInitTree(params);
    // this.initTree(params);
  },
  /**
   * 组织树模糊查询输入监听事件
   * @param e
   */
  searchConditionHandler: function (e) {
    var $this = this;
    if ($this.treeSearchTimeout !== null) {
      clearTimeout($this.treeSearchTimeout);
      $this.treeSearchTimeout = null;
    }
    var value = $(e.target).val();
    $this.treeSearchTimeout = setTimeout(function () {
      $this.fuzzySearchInterlocutor(value);
    }, 500);
  },
  /**
   * 组织树模糊查询清空事件
   */
  searchConditionClearHandler: function (e) {
    // var value = $(e.target).val();
    // this.fuzzySearchInterlocutor(value);
    var $this = this;
    $this.beforeAsync = true;
    var params = {
      url: '/clbs/talkback/monitoring/dispatch/getInterlocutorTree',
      data: {'interlocutorStatus': 0}
    };
    $this.initTree(params);
  },
  /**
   * 加载不同状态的组织树
   * 0:全部; 1:在线; 2:离线
   */
  loadTreeStatusHandler: function (e) {
    var $this = this;
    $this.beforeAsync = true;
    var type = $(e.target).attr('data-type');
    $this.loadDispatchTreeType = type;
    var params = {
      url: '/clbs/talkback/monitoring/dispatch/getInterlocutorTree',
      data: {'interlocutorStatus': type}
    };
    $this.initTree(params);
  },
  /**
   * 组织树模糊查询类型选择监听事件
   * @param e
   */
  searchTypeChangeHandler: function (e) {
    var type = $(e.target).val();
    this.queryType = type;
  },
  /**
   * 对组织树进行条件模糊查询
   */
  searchTreeHandler: function () {
    var $this = this;
    $this.beforeAsync = true;
    var value = $('#searchCondition').val();
    if (value !== '') {
      $this.fuzzySearchInterlocutor(value);
    } else {
      var params = {
        url: '/clbs/talkback/monitoring/dispatch/getInterlocutorTree',
        data: {'interlocutorStatus': 0}
      };
      $this.initTree(params);
    }
  },
  /**
   * 添加nodes到组织树
   */
  addNodes: function (nodes) {
    var $this = this;
    // $this.beforeAsync = false;
    var options = $this.options;
    var treeObj = $.fn.zTree.getZTreeObj(options.elementId);
    var addAssignmentNode = this._dispatchModule.get('data').getAddAssignmentNode();
    treeObj.addNodes(addAssignmentNode, nodes);
    // setTimeout(function () {
    //   $this.beforeAsync = true;
    // }, 250);
  },
  /**
   * 父节点展开之前的事件回调函数
   */
  zTreeBeforeExpand: function (treeId, treeNode) {
    // if (!treeNode.open) {
    this.beforeAsync = false;
    // }
  },
  /**
   * 组织树展开事件
   */
  ztreeOnExpand: function (event, treeId, treeNode) {
    var $this = this;
    if (treeNode.type === 'assignment' && treeNode.children === undefined) {
      $this.searchGroupOfList(treeNode);
    }
  },
  /**
   * 查询群组内的监控对象
   */
  searchGroupOfList: function (node) {
    var $this = this;
    $this._dispatchModule.get('dispatchServices').searchGroupOfList(
      {
        intercomGroupId: node.intercomGroupId,
        interlocutorStatus: $this.loadDispatchTreeType
      },
      function (infos) {
        $this.searchGroupOfListCallback(infos, node);
      },
      false
    );
  },
  /**
   * 群组监控对象查询完成事件
   */
  searchGroupOfListCallback: function (data, node) {
    var $this = this;
    if (data.success) {
      var obj = data.obj;
      var list = [];
      obj.map(function (item) {
        list.push({
          type: item.type,
          name: item.userName,
          pId: node.id,
          audioOnlineStatus: item.audioOnlineStatus,
          defaultGroupId: item.defaultGroupId,
          interlocutorId: item.userId,
          iconSkin: item.audioOnlineStatus === 0 ? 'peopleSkin' : 'onlineDriving'
        });
      });
      var options = $this.options;
      var treeObj = $.fn.zTree.getZTreeObj(options.elementId);
      treeObj.addNodes(node, list);
      // node.name = node.name + ' (' + list.length + ')';
      // treeObj.updateNode(node);
      $this._dispatchModule.get('data').setMonitorNodesList(list);
    }
  },
  /**
   * 用户状态改变后更新tree
   */
  updateTreeStatus: function (event) {
    var $this = this;
    var options = this.options;
    var node = $this._dispatchModule.get('data').getMonitorNode(event.userId);
    if (node !== null && node !== undefined) {
      var treeObj = $.fn.zTree.getZTreeObj(options.elementId);
      var treeNode = treeObj.getNodeByParam('name', node.name);
      if (treeNode !== null && treeNode !== undefined) {
        treeNode.iconSkin = event.audioStatus === 0 ? 'peopleSkin' : 'onlineDriving';
        treeNode.audioOnlineStatus = event.audioStatus;
        treeObj.updateNode(treeNode);
        var dispatchNode = $this._dispatchModule.get('data').getDispatchNode();
        if (dispatchNode !== null && dispatchNode !== undefined) {
          if (dispatchNode.interlocutorId === event.userId) {
            $this._dispatchModule.get('dispatch').dispatchMonitorStatus(treeNode);
          }
        }
      }
    }
  },
  /**
   * 删除指定id的组织树节点
   */
  removeNode: function (id) {
    var options = this.options;
    var treeObj = $.fn.zTree.getZTreeObj(options.elementId);
    var node = treeObj.getNodeByParam('interlocutorId', id, null);
    treeObj.removeNode(node);
  },
  fixedZTreeBeforeAsync: function () {
    return this.fixedBeforeAsync;
  },
  fixedZTreeBeforeExpand: function () {
    this.fixedBeforeAsync = false;
  },
  fixedZtreeOnExpand: function (event, treeId, treeNode) {
    var $this = this;
    if (treeNode.type === 'assignment' && treeNode.children === undefined) {
      if (!quickRepeatClickCheck) {
        return;
      }
      quickRepeatClickCheck = false;
      $this.searchFixedGroupOfList(treeNode);
    }
    // else {
    //   this.fixedBeforeAsync = true;
    // }
  },
  searchFixedGroupOfList: function (node) {
    var $this = this;
    $this._dispatchModule.get('dispatchServices').searchGroupOfList(
      {
        intercomGroupId: node.intercomGroupId,
        interlocutorStatus: 1
      },
      function (infos) {
        $this.searchFixedGroupOfListCallback(infos, node);
      },
      true
    );
  },
  /**
   * 群组监控对象查询完成事件
   */
  searchFixedGroupOfListCallback: function (data, node) {
    var $this = this;
    if (data.success) {
      var obj = data.obj;
      var list = [];
      obj.map(function (item) {
        if ($this.needFilterInterlocutorId.indexOf(item.userId) === -1) {
          list.push({
            type: item.type,
            name: item.userName,
            pId: node.id,
            audioOnlineStatus: item.audioOnlineStatus,
            defaultGroupId: item.defaultGroupId,
            interlocutorId: item.userId,
            iconSkin: item.audioOnlineStatus === 0 ? 'peopleSkin' : 'onlineDriving'
          });
        }
      });
      var treeObj = $.fn.zTree.getZTreeObj('taskGroupTree');
      treeObj.addNodes(node, list);
      // setTimeout(function () {
      //   $this.fixedBeforeAsync = true;
      // }, 250);
    }
    quickRepeatClickCheck = true;
  },
  /**
   * 模糊搜索组织树
   * * @param type 对讲对象状态 0:全部; 1:在线; 2:离线
   */
  fuzzyInitTree: function (params) {
    var $this = this;
    var options = $this.options;
    var elementId = options.elementId;
    var setting = {
      async: {
        url: params.url,
        type: 'post',
        enable: true,
        autoParam: ['id'],
        dataType: 'json',
        otherParam: params.data,
        dataFilter: $this.fuzzyAjaxDataFilter.bind($this)
      },
      check: {
        enable: false,
        chkStyle: 'checkbox',
        chkboxType: {
          'Y': 's',
          'N': 's'
        },
        radioType: 'all'
      },
      view: {
        addHoverDom: $this.addHoverDom.bind($this),
        removeHoverDom: $this.removeHoverDom.bind($this),
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
        beforeAsync: $this.zTreeBeforeAsync.bind($this),
        onClick: $this.ztreeOnClick.bind($this),
        beforeExpand: $this.zTreeBeforeExpand.bind($this),
        onExpand: $this.ztreeOnExpand.bind($this),
        onAsyncSuccess: $this.fuzzyZTreeOnAsyncSuccess.bind($this)
      }
    };
    $.fn.zTree.init($('#' + elementId), setting, null);
  },
  /**
   * 模糊搜索初始加载数据
   * @returns {*}
   */
  fuzzyAjaxDataFilter: function (treeId, parentNode, responseData) {
    if (responseData.success) {
      var $this = this;
      responseData = JSON.parse(ungzip(responseData.obj));
      $this.treeNodesClassify(responseData);
      responseData = $this.dispacthTreeNodeStatus(responseData);
      return responseData;
    }
    return null;
  },
  /**
   * 初始化的对讲对象树状态替换
   */
  dispatchObjStatusHandler: function (data) {
    data.map(function (item) {
      if (item.type === 'people') {
        item.iconSkin = item.audioOnlineStatus === 0 ? 'peopleSkin' : 'onlineDriving';
      }
    });
    return data;
  },
  /**
   * 对讲对象树异步加载成功
   * @param event
   * @param treeId
   * @param treeNode
   * @param msg
   */
  fuzzyZTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
    var $this = this;
    if ($this.queryType === 'assignment') {
      var nodes = JSON.parse(ungzip(msg.obj));
      var options = $this.options;
      var treeObj = $.fn.zTree.getZTreeObj(options.elementId);
      for (var i = 0; i < nodes.length; i++) {
        if (nodes[i].type === 'assignment') {
          var node = treeObj.getNodeByParam('id', nodes[i].id);
          treeObj.expandNode(node, true, false, true, true);
          break;
        }
      }
    }
  },
  /**
   * 用户默认组更新事件
   */
  userDefaultGroupUpdateEvent: function (event) {
    var $this = this;
    if (event.defaultGroupId !== 0 && $this.loadDispatchTreeType !== 2) {
      var node = $this._dispatchModule.get('data').getMonitorNode(event.userId);
      if (node !== null && node !== undefined) { // 未有包含该对讲对象的分组展开
        $this.deleteDispatchObjNode(node, event.defaultGroupId);
        $this.addDispatchObjNode(node, event);
      } else {
        $this._dispatchModule.get('dispatchServices').getInterlocutorInfoById(
          {interlocutorId: event.userId},
          function (data) {
            if (data.success) {
              var obj = data.obj;
              if (obj !== null && obj !== undefined) {
                var node = {
                  name: obj.userName,
                  pId: event.defaultGroupId,
                  iconSkin: 'onlineDriving',
                  type: obj.type,
                  interlocutorId: obj.userId,
                  audioOnlineStatus: obj.audioOnlineStatus
                };
                $this.addDispatchObjNode(node, event);
              }
            }
          }
        );
      }
    } else {
      var node = $this._dispatchModule.get('data').getMonitorNode(event.userId);
      if (node !== null && node !== undefined) {
        $this.deleteDispatchObjNode(node, event.defaultGroupId);
      }
    }
  },
  /**
   * 删除分组中需要移除的节点和数量统计变化
   */
  deleteDispatchObjNode: function (node, defaultGroupId) {
    var $this = this;
    var options = $this.options;
    var treeObj = $.fn.zTree.getZTreeObj(options.elementId);
    var treeNodes = treeObj.getNodesByParam('name', node.name, null);
    if (treeNodes.length > 0) {
      for (var i = 0; i < treeNodes.length; i++) {
        var node = treeNodes[i];
        var parentNode = treeObj.getNodeByParam('id', node.pId, null);
        if (parentNode.intercomGroupId !== defaultGroupId) {
          treeObj.removeNode(node);
          /**
           * 删除对讲对象后，改变分组中监控对象数量
           */
          // if (parentNode !== null && parentNode !== undefined) {
          //   var names = parentNode.name.split(' ');
          //   if (names.length > 1) {
          //     var number = Number(names[1].substr(1).replace(')', '')) - 1;
          //     if (number >= 0) {
          //       parentNode.name = names[0] + ' (' + number + ')';
          //     }
          //   }
          //   treeObj.updateNode(parentNode);
          // }
          $this._dispatchModule.get('data').removeMonitorNode(node.interlocutorId);
          break;
        }
      }
    }
  },
  /**
   * 移入分组的对讲对象的变化和数量统计
   */
  addDispatchObjNode: function (treeNode, event) {
    var $this = this;
    var options = $this.options;
    var treeObj = $.fn.zTree.getZTreeObj(options.elementId);
    var groupNode = $this._dispatchModule.get('data').getAssignmentNodes(event.defaultGroupId);
    if (groupNode !== null && groupNode !== undefined) {
      var treeGroupNode = treeObj.getNodeByParam('id', groupNode.id, null);
      treeNode.pId = treeGroupNode.id;
      if (treeGroupNode.open) { // 分组已经展开，往分组中添加对讲对象
        /**
         * 判断分组内是否已存在相同监控对象
         */
        var childrenNodes = treeGroupNode.children;
        if (childrenNodes !== null && childrenNodes !== undefined) {
          for (var i = 0; i < childrenNodes.length; i++) {
            var node = childrenNodes[i];
            if (node.interlocutorId === event.userId) {
              return false;
            }
          }
        }

        treeObj.addNodes(treeGroupNode, treeNode, true);
        $this._dispatchModule.get('data').setMonitorNodesList(treeNode);

        /**
         * 改变分组的对讲对象数量
         */
        // var names = treeGroupNode.name.split(' ');
        // if (names.length > 1) {
        //   var number = Number(names[1].substr(1).replace(')', '')) + 1;
        //   treeGroupNode.name = names[0] + ' (' + number + ')';
        // }
        // treeObj.updateNode(treeGroupNode);
      } else { // 分组未展开或无子节点，进行展开分组操作
        treeGroupNode.isParent = true;
        // var names = treeGroupNode.name.split(' ');
        // treeGroupNode.name = names[0];
        treeObj.updateNode(treeGroupNode);
        if (treeGroupNode.pId === 'ou=taskOrganization,ou=Enterprise_top,ou=organization' ||
          treeGroupNode.pId === 'ou=temporaryOrganization,ou=Enterprise_top,ou=organization') {
          treeObj.expandNode(treeGroupNode, true, false, true, true);
        }
      }
    }
  },
  /**
   * 临时组解散事件
   */
  tempGroupDeleteEvent: function (event) {
    var $this = this;
    var node = $this._dispatchModule.get('data').getAssignmentNodes(event._tempGroupId);
    if (node !== null && node !== undefined) {
      var params = {
        assignmentId: event._tempGroupId,
        assignmentType: 3
      };
      $this._dispatchModule.get('dispatchServices').unbindAssignmentAndMonitor(
        params,
        function (data) {
          if (data.success) {
            var treeObj = $.fn.zTree.getZTreeObj($this.options.elementId);
            var groupNode = treeObj.getNodeByParam('id', node.id, null);
            treeObj.removeNode(groupNode);
            $this._dispatchModule.get('data').removeAssignmentNode(groupNode.intercomGroupId);
            $this._dispatchModule.get('data').removeTempGroupNodes(groupNode.intercomGroupId);
          }
        }
      );
    }
  },
  /**
   * 调度服务临时组组装
   */
  dispatchTempGroupData: function (data) {
    var $this = this;
    var tempGroup = $this._dispatchModule.get('data').getTemporaryGroupNode();
    var dispatchLoginData = $this._dispatchModule.get('data').getDispatchLoginData();
    var userOwnTemp = dispatchLoginData.userOwnTempAssignmentIntercomGroupIdList;
    var tempGroupList = data._tempGroupList;
    for (var i = 0; i < tempGroupList.length; i++) {
      var temp = tempGroupList[i];
      if (userOwnTemp.indexOf(temp.tempGroupId) !== -1) {
        var assignmentNode = {
          name: temp.tempGroupName,
          type: 'assignment',
          iconSkin: 'assignmentSkin',
          pId: tempGroup.id,
          intercomGroupId: temp.tempGroupId,
          isParent: true,
          id: temp.tempGroupId
        };
        $this._dispatchModule.get('data').setAssignmentNodesList(assignmentNode);
        $this._dispatchModule.get('data').setTempGroupNodes(assignmentNode);
      }
    }
    if (!$this.isInitAddTempGroup) {
      $this.addTempNodes();
    }
  },
  /**
   * 添加临时组node节点
   */
  addTempNodes: function () {
    var $this = this;
    var options = $this.options;
    var tempNodes = Object.values($this._dispatchModule.get('data').getTempGroupNodes());

    if (tempNodes.length > 0) {
      $this.isInitAddTempGroup = true;
      var tempGroup = $this._dispatchModule.get('data').getTemporaryGroupNode();
      var treeObj = $.fn.zTree.getZTreeObj(options.elementId);
      var tempGroupNode = treeObj.getNodeByParam('id', tempGroup.id, null);
      treeObj.addNodes(tempGroupNode, tempNodes);
      for (var i = 0; i < tempNodes.length; i++) {
        $this._dispatchModule.get('data').setAssignmentNodesList(tempNodes[i]);
      }
      var firstNode = tempGroupNode.children[0];
      firstNode.isParent = true;
      treeObj.expandNode(firstNode, true, false, true, true);
    }
  }
};