var DispatchTaskGroup = function (options) {
  this._dispatchModule = options.dispatchModule;
  this.interlocutorIdsStr = null;
  // this.selectedMonitorList = [];
  this.init();
};

DispatchTaskGroup.prototype = {
  constructor: DispatchTaskGroup,
  /**
   * 初始化绑定事件监听
   */
  init: function () {
    var $this = this;
    $('#selectedModel').on('click', $this.groupModelSelected.bind($this));
    $('#confirmCreateTaskGroup').on('click', $this.confirmCreateTaskGroupHandler.bind($this));
    $('#cancelCreateTaskGroup, #cancelGroup').on('click', $this.cancelCreateTaskGroupHandler.bind($this));
    $('#staffSkills, #intercomModel, #driverLicenseType, #bloodType, #certificateType').on('click', $this.showFixedConditionListView.bind($this));
    // $('#fixedConditionsArea span.type').on('click', $this.fixedConditionsTypeSelected.bind($this));
    $('#staffSkillsSelectedAll, #intercomModelSelectedAll, #driverLicenseTypeSelectedAll, #bloodTypeSelectedAll, #certificateTypeSelectedAll').on('click', $this.fixedConditionsTypeSelectedAll.bind($this));
    $('#staffSkillsClear, #intercomModelClear, #driverLicenseTypeClear, #bloodTypeClear, #certificateTypeClear').on('click', $this.fixedConditionsTypeClear.bind($this));
    $('#staffSkillsCancel, #intercomModelCancel, #driverLicenseTypeCancel, #bloodTypeCancel, #certificateTypeCancel').on('click', $this.fixedConditionsTypeCancel.bind($this));
    $('#staffSkillsConfirm, #intercomModelConfirm, #driverLicenseTypeConfirm, #bloodTypeConfirm, #certificateTypeConfirm').on('click', $this.fixedConditionsTypeConfirm.bind($this));
    $('#selectedModelCancel').on('click', $this.selectedModelCancelHandler.bind($this));
    $('#fixedConditionsSearch').on('click', $this.fixedConditionsSearchHandler.bind($this));
    $('#personSelected').on('click', $this.personSelectedHandler.bind($this));
  },
  /**
   * 创建分组模式选择
   */
  groupModelSelected: function (e) {
    var $this = this;
    $('#selectedModel').hide();
    var type = $(e.target).attr('data-type');
    $('#confirmCreateTaskGroup').attr('data-type', type);
    switch (type) {
      case '1': // 地图圆形选择
        layer.msg('请在地图上画出圆形', {offset: 't'});
        $this._dispatchModule.get('data').setFixedConditionsState(false);
        $this._dispatchModule.get('dispatchAmap').createCircular();
        break;
      case '2': // 地图矩形选择
        layer.msg('请在地图上画出矩形', {offset: 't'});
        $this._dispatchModule.get('dispatchAmap').createRectangular();
        break;
      case '3': // 固定对象选择
        $this.showFixedObject();
        break;
      case '4': // 固定条件选择
        layer.msg('请在地图上画出圆形', {offset: 't'});
        $this._dispatchModule.get('data').setFixedConditionsState(true);
        $this._dispatchModule.get('dispatchAmap').createCircular();
        break;

    }
  },
  /**
   * 展示圆形创建分组
   */
  showTaskGroupCircle: function () {
    var $this = this;
    var state = $this._dispatchModule.get('data').getFixedConditionsState();
    if (state) {
      $this.showFixedConditions();
    } else {
      var data = $this._dispatchModule.get('data').getTaskGroupDrawCircleData();
      var type = $this._dispatchModule.get('data').getAddType();
      var assignmentNode = $this._dispatchModule.get('data').getAddAssignmentNode();
      var taskGroupNode = $this._dispatchModule.get('data').getTaskGroupNode();
      var assignmentType = taskGroupNode.id === assignmentNode.pId ? '2' : '3';
      var params = {
        longitude: data.center.lng,
        latitude: data.center.lat,
        radius: data.radius,
        assignmentId: type === 2 ? assignmentNode.id : null,
        assignmentType: type === 2 ? assignmentType : null
      };
      $this._dispatchModule.get('dispatchServices').findInterlocutorByCircleArea(params, $this.findInterlocutorByCircleAreaSuccessCallback.bind($this));
    }
  },
  /**
   * 查找对讲对象,通过画的圆形区域,成功回调事件
   */
  findInterlocutorByCircleAreaSuccessCallback: function (data) {
    var $this = this;
    if (data.success) {
      var list = data.obj;
      var html = '';
      list.map(function (item) {
        html += '<li>'
          + '<span>' + item.monitorName + '</span>'
          + '<div>'
          + '<input data-name="' + item.monitorName + '" data-interlocutorId="' + item.interlocutorId + '" data-monitorId="' + item.monitorId + '" type="checkbox" checked />'
          + '</div>'
          + '</li>';
      });
      $('#checkedPersonNumber').text(list.length);
      $('#personSelected').prop('checked', true);
      $('#listMain ul').html(html);
      $this.personListSelectedHandler();
      $('#fixedConditionsArea').hide();
      $('#selectedAll').show();
      $('#listMain').show().removeClass('fixed-condition-list');
      $('#taskGroupTreeArea').hide();
      $('#taskGroupBackgroud').show();
    } else {
      layer.msg('查找对讲对象失败', {offset: 't'});
      $this._dispatchModule.get('dispatchAmap').clearMouseTool();
    }
  },
  /**
   * 展示矩形创建分组
   */
  showTaskGroupRectangular: function () {
    var $this = this;
    var data = $this._dispatchModule.get('data').getTaskGroupDrawRectangleData();
    var type = $this._dispatchModule.get('data').getAddType();
    var assignmentNode = $this._dispatchModule.get('data').getAddAssignmentNode();
    var taskGroupNode = $this._dispatchModule.get('data').getTaskGroupNode();
    var assignmentType = taskGroupNode.id === assignmentNode.pId ? 2 : 3;
    var southWest = data.bounds.southwest;
    var northEast = data.bounds.northeast;
    var params = {
      leftLongitude: southWest.lng, // southWest.lng,
      leftLatitude: northEast.lat,
      rightLongitude: northEast.lng,
      rightLatitude: southWest.lat,
      assignmentId: type === 2 ? assignmentNode.id : null,
      assignmentType: type === 2 ? assignmentType : null
    };
    $this._dispatchModule.get('dispatchServices').findInterlocutorByRectangleArea(
      params,
      $this.findInterlocutorByRectangleAreaSuccessCallback.bind($this)
    );
  },
  /**
   * 查找对讲对象,通过画的矩形区域,成功回调事件
   */
  findInterlocutorByRectangleAreaSuccessCallback: function (data) {
    if (data.success) {
      var $this = this;
      var list = data.obj;
      var html = '';
      list.map(function (item) {
        html += '<li>'
          + '<span>' + item.monitorName + '</span>'
          + '<div>'
          + '<input data-name="' + item.monitorName + '" data-interlocutorId="' + item.interlocutorId + '" data-monitorId="' + item.monitorId + '" type="checkbox" checked />'
          + '</div>'
          + '</li>';
      });
      $('#checkedPersonNumber').text(list.length);
      $('#personSelected').prop('checked', true);
      $('#listMain ul').html(html);
      $this.personListSelectedHandler();
      $('#fixedConditionsArea').hide();
      $('#selectedAll').show();
      $('#listMain').show().removeClass('fixed-condition-list');
      $('#taskGroupTreeArea').hide();
      $('#taskGroupBackgroud').show();
    }
  },
  /**
   * 固定对象选择
   */
  showFixedObject: function () {
    $('#selectedAll').hide();
    $('#listMain').hide().removeClass('fixed-condition-list');
    $('#fixedConditionsArea').hide();
    $('#taskGroupTreeArea').show();
    $('#taskGroupBackgroud').show();
    this._dispatchModule.get('dispatchTree').fixedConditionsTreeLoad();
  },
  /**
   * 固定条件选择
   */
  showFixedConditions: function () {
    var $this = this;
    $this.clearFixedConditionsValue();
    $('#staffSkillsView, #intercomModelView, #driverLicenseTypeView, #certificateTypeView, #bloodTypeView').removeClass('active');
    $this.getAllSkillList();
    $this.getAllIntercomModeList();
    $this.getAllDriverLicenseCategoryList();
    $this.getAllQualificationList();
    $this.getAllBloodTypeList();
    var data = $this._dispatchModule.get('data').getTaskGroupDrawCircleData();

    $('#fixedConditionsRadius').val((data.radius / 1000).toFixed(2));
    $('#selectedAll').show();
    $('#listMain').show().addClass('fixed-condition-list');
    $('#fixedConditionsArea').show();
    $('#taskGroupTreeArea').hide();
    $('#taskGroupBackgroud').show();
    $('#checkedPersonNumber').text(0);
  },
  /**
   * 清空固定条件选择的内容
   */
  clearFixedConditionsValue: function () {
    $('#staffSkills, #intercomModel, #driverLicenseType, #certificateType, #bloodType, #minAge, #maxAge, #fixedConditionsRadius').val('').attr('value', '');
    $('#selectedGender').find('option:nth-child(1)').prop('selected', true);
  },
  /**
   * 获得技能列表
   */
  getAllSkillList: function () {
    var $this = this;
    $this._dispatchModule.get('dispatchServices').getAllSkillList(
      function (data) {
        if (data.success) {
          var obj = data.obj;
          var html = '';
          obj.map(function (item, index) {
            if (index % 3 === 0) {
              html += '<li>';
            }
            html += '<span class="type" data-id="' + item.id + '" title="' + item.skillName + '">' + item.skillName + '</span>';
            if ((index + 1) % 3 === 0) {
              html += '</li>';
            }
          });
          $('#staffSkillsView ul').html(html);
          $('#staffSkillsView ul span.type').on('click', $this.fixedConditionsTypeSelected.bind($this));
        }
      }
    );
  },
  /**
   * 获得对讲机型列表
   */
  getAllIntercomModeList: function () {
    var $this = this;
    $this._dispatchModule.get('dispatchServices').getAllIntercomModeList(
      function (data) {
        if (data.success) {
          var obj = data.obj;
          var html = '';
          obj.map(function (item, index) {
            if (index % 3 === 0) {
              html += '<li>';
            }
            html += '<span class="type" data-id="' + item.id + '" title="' + item.name + '">' + item.name + '</span>';
            if ((index + 1) % 3 === 0) {
              html += '</li>';
            }
          });
          $('#intercomModelView ul').html(html);
          $('#intercomModelView ul span.type').on('click', $this.fixedConditionsTypeSelected.bind($this));
        }
      }
    );
  },
  /**
   * 获得驾照类别列表
   */
  getAllDriverLicenseCategoryList: function () {
    var $this = this;
    $this._dispatchModule.get('dispatchServices').getAllDriverLicenseCategoryList(
      function (data) {
        if (data.success) {
          var obj = data.obj;
          var html = '';
          obj.map(function (item, index) {
            if (index % 3 === 0) {
              html += '<li>';
            }
            html += '<span class="type" data-id="' + item.id + '" title="' + item.name + '">' + item.name + '</span>';
            if ((index + 1) % 3 === 0) {
              html += '</li>';
            }
          });
          $('#driverLicenseTypeView ul').html(html);
          $('#driverLicenseTypeView ul span.type').on('click', $this.fixedConditionsTypeSelected.bind($this));
        }
      }
    );
  },
  /**
   * 获得资格证列表
   */
  getAllQualificationList: function () {
    var $this = this;
    $this._dispatchModule.get('dispatchServices').getAllQualificationList(
      function (data) {
        if (data.success) {
          var obj = data.obj;
          var html = '';
          obj.map(function (item, index) {
            if (index % 3 === 0) {
              html += '<li>';
            }
            html += '<span class="type" data-id="' + item.id + '" title="' + item.name + '">' + item.name + '</span>';
            if ((index + 1) % 3 === 0) {
              html += '</li>';
            }
          });
          $('#certificateTypeView ul').html(html);
          $('#certificateTypeView ul span.type').on('click', $this.fixedConditionsTypeSelected.bind($this));
        }
      }
    );
  },
  /**
   * 获得血型列表
   */
  getAllBloodTypeList: function () {
    var $this = this;
    $this._dispatchModule.get('dispatchServices').getAllBloodTypeList(
      function (data) {
        if (data.success) {
          var obj = data.obj;
          var html = '';
          obj.map(function (item, index) {
            if (index % 3 === 0) {
              html += '<li>';
            }
            html += '<span class="type" data-id="' + item.id + '">' + item.name + '</span>';
            if ((index + 1) % 3 === 0) {
              html += '</li>';
            }
          });
          $('#bloodTypeView ul').html(html);
          $('#bloodTypeView ul span.type').on('click', $this.fixedConditionsTypeSelected.bind($this));
        }
      }
    );
  },
  /**
   * 创建分组确定提交
   */
  confirmCreateTaskGroupHandler: function () {
    var $this = this;
    var type = $('#confirmCreateTaskGroup').attr('data-type');
    switch (type) {
      case '1': // 地图圆形选择
        $this.circleTaskGroupSubmit();
        break;
      case '2': // 地图矩形选择
        $this.circleTaskGroupSubmit();
        break;
      case '3': // 固定对象选择
        $this.fixedObjectTaskGroupSubmit();
        break;
      case '4': // 固定条件选择
        $this.fixedConditionsTaskGroupSubmit();
    }
  },
  /**
   * 地图圆形选择分组提交
   */
  circleTaskGroupSubmit: function () {
    var $this = this;
    var type = $this._dispatchModule.get('data').getAddType();
    if (type === 1) {
      if (!$this.validateGroupName()) {
        return false;
      }
    }

    var interlocutorIds = $this.validateListSelected();

    $this.interlocutorIdsStr = interlocutorIds.join(',');
    if (!interlocutorIds.length) {
      $this.interlocutorIdsStr = '';
      if (type === 1) {
        $this.addTaskAssignmentAndMemberHandler();
      } else {
        layer.msg('人员列表选择不能为空', {offset: 't'});
      }
    } else {
      var assignmentNode = $this._dispatchModule.get('data').getAddAssignmentNode();
      var taskGroupNode = $this._dispatchModule.get('data').getTaskGroupNode();
      if (assignmentNode.id === taskGroupNode.id || assignmentNode.pId === taskGroupNode.id) {
        var params = {
          interlocutorIds: $this.interlocutorIdsStr
        };
        $this._dispatchModule.get('dispatchServices').judgeInterlocutorTaskAssignmentNumIsOverLimit(
          params,
          $this.judgeInterlocutorTaskAssignmentNumIsOverLimitSuccessCallback.bind($this)
        );
      } else {
        $this.addTaskAssignmentAndMemberHandler();
      }
    }
  },
  /**
   * 判断对讲对象的任务组数量是否超出限制成功回调事件
   * @param data
   */
  judgeInterlocutorTaskAssignmentNumIsOverLimitSuccessCallback: function (data) {
    var $this = this;
    if (data.success) {
      var isOverLimit = data.obj.isOverLimit;
      var overLimitMonitorName = data.obj.overLimitMonitorName;
      if (isOverLimit) {
        var info = '勾选的人员中存在超过加入群组的限制，<br/>请取消如下人员的勾选：<br/>' + overLimitMonitorName;
        layer.confirm(info, {
          title: '提示',
          icon: 1,
          move: false,
          btn: ['确定', '取消']
        }, function () {
          layer.closeAll();
        }, function () {

        });
      } else {
        $this.addTaskAssignmentAndMemberHandler();
      }
    }
  },
  /**
   * 提交创建分组
   */
  addTaskAssignmentAndMemberHandler: function () {
    var $this = this;
    var type = $this._dispatchModule.get('data').getAddType();
    if (type === 1) {
      var assignmentName = $('#groupName').val();
      var params = {
        interlocutorIds: $this.interlocutorIdsStr,
        assignmentName: assignmentName
      };
      var assignmentNode = $this._dispatchModule.get('data').getAddAssignmentNode();
      var taskGroupNode = $this._dispatchModule.get('data').getTaskGroupNode();
      if (assignmentNode.id === taskGroupNode.id) { // 创建任务组
        $this._dispatchModule.get('dispatchServices').addTaskAssignmentAndMember(
          params,
          $this.addTaskAssignmentAndMemberSuccessCallback.bind($this)
        );
      } else { // 创建临时组
        var ids = [];
        if ($this.interlocutorIdsStr) {
          ids = $this.interlocutorIdsStr.split(',').map(Number);
        }
        $this._dispatchModule.get('dispatchWebServices').createTempGroup(assignmentName, ids);
        // $this._dispatchModule.get('dispatchServices').addTemporaryAssignment(
        //     params,
        //     $this.addTaskAssignmentAndMemberSuccessCallback.bind($this)
        // );
      }
    } else {
      var assignmentNode = $this._dispatchModule.get('data').getAddAssignmentNode();
      var taskGroupNode = $this._dispatchModule.get('data').getTaskGroupNode();
      if (assignmentNode.pId === taskGroupNode.id) { // 加入任务组
        var params = {
          assignmentId: assignmentNode.id,
          interlocutorIds: $this.interlocutorIdsStr
        };
        $this._dispatchModule.get('dispatchServices').insertTaskAssignmentAndMember(
          params,
          // $this.insertTaskAssignmentAndMemberSuccessCallback.bind($this)
          function (data) {
            $this.insertTaskAssignmentAndMemberSuccessCallback(data);
            $this._dispatchModule.get('dispatchWebServices').changeMsDefaultGroup(
              assignmentNode.intercomGroupId,
              $this.interlocutorIdsStr.split(',').map(Number)
            );
          }
        );
      } else {
        var ids = $this.interlocutorIdsStr.split(',').map(Number);
        $this._dispatchModule.get('dispatchWebServices').addTempGroupMember(
          assignmentNode.intercomGroupId,
          ids
        );
      }
    }
  },
  /**
   * 创建分组成功事件
   * @param data
   */
  addTaskAssignmentAndMemberSuccessCallback: function (data) {
    var $this = this;
    if (data.success) {
      var obj = data.obj;
      var assignmentNode = $this._dispatchModule.get('data').getAddAssignmentNode();
      var taskGroupNode = $this._dispatchModule.get('data').getTaskGroupNode();
      if (taskGroupNode.id === assignmentNode.id) {
        layer.msg('任务组' + obj.assignmentName + '创建成功', {offset: 't'});
        var addAssignmentNode = $this._dispatchModule.get('data').getAddAssignmentNode();
        var nodes = [];
        var assignmentNode = {
          name: obj.assignmentName,
          type: 'assignment',
          iconSkin: 'assignmentSkin',
          id: obj.assignmentId,
          pId: addAssignmentNode.id,
          intercomGroupId: obj.intercomGroupId,
          open: true
        };
        nodes.push(assignmentNode);
        $this._dispatchModule.get('data').setAssignmentNodesList(assignmentNode);
        // $this.selectedMonitorList.map(function (item) {
        //   $this._dispatchModule.get('dispatchTree').removeNode(item.interlocutorId);
        //   var node = {
        //     id: item.monitorId,
        //     name: item.name,
        //     pId: obj.assignmentId,
        //     iconSkin: 'onlineDriving',
        //     type: 'people',
        //     interlocutorId: item.interlocutorId
        //   };
        //   $this._dispatchModule.get('data').setMonitorNodesList(node);
        //   nodes.push(node);
        // });

        $this._dispatchModule.get('dispatchTree').addNodes(nodes);
        /**
         * 切换用户默认组
         */
        $this._dispatchModule.get('dispatchWebServices').changeMsDefaultGroup(
          obj.intercomGroupId,
          $this.interlocutorIdsStr.split(',').map(Number)
        );
      } else {
        layer.msg('临时组创建成功', {offset: 't'});
      }
      $this._dispatchModule.get('dispatchAmap').clearMouseTool();
      $('#taskGroupBackgroud').hide();
      $('#groupName').val('');
      $('#listMain ul').html('');
      $('#checkedPersonNumber').text(0);
    } else {
      layer.msg(data.msg, {offset: 't'});
    }
  },
  /**
   * 临时创建响应事件
   */
  tempGroupCerateSuccess: function (event) {
    var $this = this;
    /**
     * 创建临时组
     * 调用平台接口
     */
    var interlocutorIds = [];
    event._addGroupMemberResult.map(function (item) {
      interlocutorIds.push(item.groupMemberMsId);
    });
    $this._dispatchModule.get('dispatchTree').tempNodesInterlocutorIds = interlocutorIds;
    $this._dispatchModule.get('dispatchServices').addTemporaryAssignment(
      {
        assignmentName: event._tempGroupName,
        intercomGroupId: event._tempGroupId,
        interlocutorIds: interlocutorIds.join(',')
      },
      $this.addTaskAssignmentAndMemberSuccessCallback.bind($this)
    );

    /**
     * 临时组数据组装
     */
    var tempNode = $this._dispatchModule.get('data').getTemporaryGroupNode();
    var selectedList = {};

    // $this.selectedMonitorList.map(function (item) {
    //   selectedList[item.interlocutorId] = item.name;
    // });

    var nodes = [];
    /**
     * 分组节点
     */
    var assignmentNode = {
      name: event._tempGroupName,
      type: 'assignment',
      iconSkin: 'assignmentSkin',
      pId: tempNode.id,
      intercomGroupId: event._tempGroupId,
      open: true,
      id: event._tempGroupId,
      isParent: true
    };
    nodes.push(assignmentNode);
    /**
     * 将分组节点数据添加到全局分组数据中
     */
    $this._dispatchModule.get('data').setAssignmentNodesList(assignmentNode);
    $this._dispatchModule.get('data').setTempGroupNodes(assignmentNode);
    /**
     * 对讲对象节点
     */
    // event._addGroupMemberResult.map(function (item) {
    //   var node = {
    //     name: selectedList[item.groupMemberMsId],
    //     pId: event._tempGroupId,
    //     iconSkin: 'onlineDriving',
    //     type: 'people',
    //     interlocutorId: item.groupMemberMsId
    //   };
    //   nodes.push(node);
    // });
    /**
     * 将对讲对象数据添加到全局数据中
     */
    // $this._dispatchModule.get('data').setMonitorNodesList(nodes);
    $this._dispatchModule.get('dispatchTree').addNodes(nodes);
  },
  /**
   * 加入分组成功事件
   * @param data
   */
  insertTaskAssignmentAndMemberSuccessCallback: function (data) {
    var $this = this;
    if (data.success) {
      layer.msg('加入分组成功', {offset: 't'});
      $this._dispatchModule.get('dispatchAmap').clearMouseTool();

      $('#taskGroupBackgroud').hide();
      $('#groupName').val('');
      $('#listMain ul').html('');
      $('#checkedPersonNumber').text(0);
    } else {
      layer.msg(data.msg, {offset: 't'});
    }
  },
  /**
   * 加入临时组成员响应事件
   */
  addTempGroupMemberSuccess: function (event) {
    var $this = this;
    var interlocutorIds = [];
    event._addGroupMemberResult.map(function (item) {
      interlocutorIds.push(item.groupMemberMsId);
    });
    var params = {
      intercomGroupId: event._tempGroupId,
      interlocutorIds: interlocutorIds.join(',')
    };
    $this._dispatchModule.get('dispatchServices').insertTemporaryAssignmentRecordLog(
      params,
      $this.insertTaskAssignmentAndMemberSuccessCallback.bind($this)
    );

    // var $this = this;
    // console.log('加入临时组成员响应事件', event);
    // var addAssignmentNode = $this._dispatchModule.get('data').getAddAssignmentNode();
    //
    // var selectedList = {};
    // $this.selectedMonitorList.map(function (item) {
    //   selectedList[item.interlocutorId] = item.name;
    // });
    // event._addGroupMemberResult.map(function (item) {
    //   var node = {
    //     name: selectedList[item.groupMemberMsId],
    //     pId: addAssignmentNode.id,
    //     iconSkin: 'onlineDriving',
    //     type: 'people',
    //     interlocutorId: item.groupMemberMsId
    //   };
    //   nodes.push(node);
    // });
    // $this._dispatchModule.get('dispatchTree').addNodes(nodes);
  },
  /**
   * 固定对象
   */
  fixedObjectTaskGroupSubmit: function () {
    var $this = this;
    var type = $this._dispatchModule.get('data').getAddType();
    if (type === 1) {
      if (!$this.validateGroupName()) {
        return false;
      }
    }
    var treeObj = $.fn.zTree.getZTreeObj('taskGroupTree');
    var checkedNodes = treeObj.getCheckedNodes(true);

    var interlocutorIds = [];
    // $this.selectedMonitorList = [];
    checkedNodes.map(function (item) {
      if (item.type !== 'group' && item.type !== 'assignment') {
        interlocutorIds.push(item.interlocutorId);
        // $this.selectedMonitorList.push({
        //   monitorId: item.id,
        //   name: item.name,
        //   interlocutorId: item.interlocutorId
        // });
      }
    });
    $this.interlocutorIdsStr = interlocutorIds.join(',');
    if (interlocutorIds.length > 0) {
      var assignmentNode = $this._dispatchModule.get('data').getAddAssignmentNode();
      var taskGroupNode = $this._dispatchModule.get('data').getTaskGroupNode();
      if (assignmentNode.id === taskGroupNode.id || assignmentNode.pId === taskGroupNode.id) {
        var params = {
          interlocutorIds: $this.interlocutorIdsStr
        };
        $this._dispatchModule.get('dispatchServices').judgeInterlocutorTaskAssignmentNumIsOverLimit(
          params,
          $this.judgeInterlocutorTaskAssignmentNumIsOverLimitSuccessCallback.bind($this)
        );
      } else {
        $this.addTaskAssignmentAndMemberHandler();
      }
    } else {
      var type = $this._dispatchModule.get('data').getAddType();
      if (type === 1) {
        $this.addTaskAssignmentAndMemberHandler();
      } else {
        layer.msg('人员列表选择不能为空', {offset: 't'});
      }
    }
  },
  /**
   * 固定条件选择
   */
  fixedConditionsTaskGroupSubmit: function () {
    var $this = this;
    var type = $this._dispatchModule.get('data').getAddType();
    if (type === 1) {
      if (!$this.validateGroupName()) {
        return false;
      }
    }

    var interlocutorIds = $this.validateListSelected();
    $this.interlocutorIdsStr = interlocutorIds.join(',');
    if (interlocutorIds.length !== 0) {
      var assignmentNode = $this._dispatchModule.get('data').getAddAssignmentNode();
      var taskGroupNode = $this._dispatchModule.get('data').getTaskGroupNode();
      if (assignmentNode.id === taskGroupNode.id || assignmentNode.pId === taskGroupNode.id) {
        var params = {
          interlocutorIds: $this.interlocutorIdsStr
        };
        $this._dispatchModule.get('dispatchServices').judgeInterlocutorTaskAssignmentNumIsOverLimit(
          params,
          $this.judgeInterlocutorTaskAssignmentNumIsOverLimitSuccessCallback.bind($this)
        );
      } else {
        $this.addTaskAssignmentAndMemberHandler();
      }
    } else {
      var type = $this._dispatchModule.get('data').getAddType();
      if (type === 1) {
        $this.addTaskAssignmentAndMemberHandler();
      } else {
        layer.msg('人员列表选择不能为空', {offset: 't'});
      }
    }
  },
  /**
   * 验证组名称是否为空
   */
  validateGroupName: function () {
    var name = $('#groupName').val();
    if (name === '') {
      layer.msg('组名称不能为空', {offset: 't'});
      return false;
    } else if (name.length > 10) {
      layer.msg('组名称长度不能超过10个字符', {offset: 't'});
      return false;
    }
    return true;
  },
  /**
   * 验证人员列表是否勾选
   */
  validateListSelected: function () {
    var $this = this;
    // $this.selectedMonitorList = [];
    var arr = [];
    $('#listMain input').each(function (item, element) {
      if ($(element).is(':checked')) {
        var interlocutorId = $(element).attr('data-interlocutorId');
        var monitorId = $(element).attr('data-monitorid');
        var name = $(element).attr('data-name');
        arr.push(interlocutorId);
        // $this.selectedMonitorList.push({
        //   monitorId: monitorId,
        //   name: name,
        //   interlocutorId: interlocutorId
        // });
      }
    });
    return arr;
  },
  /**
   * 验证组名称是否与已有的冲突
   */
  validateSameGroupName: function () {
    var name = $('#groupName').val();
    var groupList = Object.values(this._dispatchModule.get('data').getAssignmentNodesList());
    var flag = false;
    groupList.map(function (item) {
      var groupName = item.name;
      if (name === groupName) {
        flag = true;
      }
    });
    return flag;
  },
  /**
   * 取消创建分组
   */
  cancelCreateTaskGroupHandler: function () {
    $('#taskGroupBackgroud').hide();
    $('#groupName').val('');
    $('#listMain ul').html('');
    $('#checkedPersonNumber').text(0);
    this._dispatchModule.get('dispatchAmap').clearMouseTool();
  },
  /**
   * 展示固定条件弹窗中人员技能、对讲机型、驾照类别和血型类别列表
   * @param e
   */
  showFixedConditionListView: function (e) {
    $('#driverLicenseTypeView, #intercomModelView, #staffSkillsView, #bloodTypeView, #certificateTypeView').removeClass('active');
    $(e.target).siblings('div.content').addClass('active');
  },
  /**
   * 固定条件弹窗中人员技能、对讲机型、驾照类别和血型类别列表选中
   * @param e
   */
  fixedConditionsTypeSelected: function (e) {
    var classValue = $(e.target).attr('class');
    if (classValue.indexOf('active') === -1) {
      $(e.target).attr('class', 'type active');
    } else {
      $(e.target).attr('class', 'type');
    }
  },
  /**
   * 固定条件弹窗中人员技能、对讲机型、驾照类别和血型类别列表全选
   */
  fixedConditionsTypeSelectedAll: function (e) {
    var type = $(e.target).attr('data-type');
    switch (type) {
      case '1':
        $('#staffSkillsView span.type').attr('class', 'type active');
        break;
      case '2':
        $('#intercomModelView span.type').attr('class', 'type active');
        break;
      case '3':
        $('#driverLicenseTypeView span.type').attr('class', 'type active');
        break;
      case '4':
        $('#bloodTypeView span.type').attr('class', 'type active');
        break;
      case '5':
        $('#certificateTypeView span.type').attr('class', 'type active');
        break;
    }
  },
  /**
   * 固定条件弹窗中人员技能、对讲机型、驾照类别和血型类别列表清空
   * @param e
   */
  fixedConditionsTypeClear: function (e) {
    var type = $(e.target).attr('data-type');
    switch (type) {
      case '1':
        $('#staffSkillsView span.type').attr('class', 'type');
        break;
      case '2':
        $('#intercomModelView span.type').attr('class', 'type');
        break;
      case '3':
        $('#driverLicenseTypeView span.type').attr('class', 'type');
        break;
      case '4':
        $('#bloodTypeView span.type').attr('class', 'type');
        break;
      case '5':
        $('#certificateTypeView span.type').attr('class', 'type');
        break;
    }
  },
  /**
   * 固定条件弹窗中人员技能、对讲机型、驾照类别和血型类别列表取消
   */
  fixedConditionsTypeCancel: function (e) {
    var type = $(e.target).attr('data-type');
    switch (type) {
      case '1':
        $('#staffSkillsView').attr('class', 'content');
        break;
      case '2':
        $('#intercomModelView').attr('class', 'content');
        break;
      case '3':
        $('#driverLicenseTypeView').attr('class', 'content');
        break;
      case '4':
        $('#bloodTypeView').attr('class', 'content');
        break;
      case '5':
        $('#certificateTypeView').attr('class', 'content');
        break;
    }
  },
  /**
   * 固定条件弹窗中人员技能、对讲机型、驾照类别和血型类别列表确认
   */
  fixedConditionsTypeConfirm: function (e) {
    var $this = this;
    var type = $(e.target).attr('data-type');
    switch (type) {
      case '1':
        $this.getFixedConditionSelectedValue('staffSkillsView', 'staffSkills');
        $('#staffSkillsView').attr('class', 'content');
        break;
      case '2':
        $this.getFixedConditionSelectedValue('intercomModelView', 'intercomModel');
        $('#intercomModelView').attr('class', 'content');
        break;
      case '3':
        $this.getFixedConditionSelectedValue('driverLicenseTypeView', 'driverLicenseType');
        $('#driverLicenseTypeView').attr('class', 'content');
        break;
      case '4':
        $this.getFixedConditionSelectedValue('bloodTypeView', 'bloodType');
        $('#bloodTypeView').attr('class', 'content');
        break;
      case '5':
        $this.getFixedConditionSelectedValue('certificateTypeView', 'certificateType');
        $('#certificateTypeView').attr('class', 'content');
        break;
    }
  },
  /**
   * 获取固定条件弹窗中人员技能、对讲机型、驾照类别和血型类别列表选择的内容
   * @param elementId
   * @param inputId
   */
  getFixedConditionSelectedValue: function (elementId, inputId) {
    var text = '';
    var value = '';
    $('#' + elementId + ' span.type').each(function (index, element) {
      if ($(element).hasClass('active')) {
        text += $(element).text() + ',';
        value += $(element).attr('data-id') + ',';
      }
    });
    text = text.substring(0, text.length - 1);
    value = value.substring(0, value.length - 1);
    $('#' + inputId).val(text).attr('value', value);
  },
  /**
   * 判断是创建任务组还是加入分组，然后进行显示想要的视图
   * @param flag
   * @param assignmentName
   */
  isCreateTaskGroup: function (flag, treeNode) {
    if (flag) {
      var title;
      if (treeNode.id === 'ou=temporaryOrganization,ou=Enterprise_top,ou=organization') {
        title = '创建临时组';
      } else {
        title = '创建任务组';
      }
      $('#addGroupTitle').text(title);
      $('#groupName').show();
      $('#groupNameTitle').hide();
    } else {
      $('#addGroupTitle').text('加入群组');
      $('#groupName').hide();
      var name = treeNode.name.split(' ');
      $('#groupNameTitle').show().text(name[0]);
    }
  },
  /**
   * 模式选择视图弹窗关闭设置
   */
  selectedModelCancelHandler: function () {
    $('#selectedModel').hide();
  },
  /**
   * 查找对讲对象,通过固定条件
   */
  fixedConditionsSearchHandler: function () {
    var $this = this;
    var data = $this._dispatchModule.get('data').getTaskGroupDrawCircleData();
    var type = $this._dispatchModule.get('data').getAddType();
    var assignmentNode = $this._dispatchModule.get('data').getAddAssignmentNode();
    var taskGroupNode = $this._dispatchModule.get('data').getTaskGroupNode();
    var assignmentType = taskGroupNode.id === assignmentNode.pId ? '2' : '3';
    var skillIds = $('#staffSkills').attr('value');
    var intercomModelIds = $('#intercomModel').attr('value');
    var driverLicenseCategoryIds = $('#driverLicenseType').attr('value');
    var qualificationIds = $('#certificateType').attr('value');
    var gender = $('#selectedGender').val();
    var radius = $('#fixedConditionsRadius').val();
    var bloodTypeIds = $('#bloodType').attr('value');
    var minAge = $('#minAge').val();
    if (minAge !== '') {
      minAge = Number(minAge);
      if (!$this.isInteger(minAge)) {
        layer.msg('年龄范围输入有误，请输入0-100的整数', {offset: 't'});
        return false;
      }
    }
    var maxAge = $('#maxAge').val();
    if (maxAge !== '') {
      maxAge = Number(maxAge);
      if (!$this.isInteger(maxAge)) {
        layer.msg('年龄范围输入有误，请输入0-100的整数', {offset: 't'});
        return false;
      }
    }
    var ageRange = '';
    if (minAge !== '' && maxAge !== '') {
      if (minAge > maxAge) {
        layer.msg('后面的年龄值要大于等于前面', {offset: 't'});
        return false;
      }
      ageRange = minAge + ',' + maxAge;
    } else if (minAge === '' && maxAge === '') {
      ageRange = '';
    } else if (minAge === '' || maxAge === '') {
      layer.msg('请输入完整的年龄范围', {offset: 't'});
      return false;
    }

    if (radius === '') {
      layer.msg('范围半径不能为空', {offset: 't'});
      return false;
    } else {
      if (parseFloat(radius).toString() == 'NaN') {
        layer.msg('请输入数字', {offset: 't'});
        return false;
      } else {
        if (radius < 0) {
          layer.msg('范围半径不能小于0', {offset: 't'});
          return false;
        }
      }
    }

    var params = {
      longitude: data.center.lng,
      latitude: data.center.lat,
      radius: radius * 1000,
      assignmentId: type === 2 ? assignmentNode.id : null,
      assignmentType: type === 2 ? assignmentType : null,
      skillIds: skillIds,
      intercomModelIds: intercomModelIds,
      driverLicenseCategoryIds: driverLicenseCategoryIds,
      qualificationIds: qualificationIds,
      gender: gender,
      bloodTypeIds: bloodTypeIds,
      ageRange: ageRange
    };
    $this._dispatchModule.get('dispatchServices').findInterlocutorByFixedCondition(
      params,
      $this.findInterlocutorByFixedConditionCallback.bind($this)
    );
  },
  /**
   * 判断输入内容是否为0-100的正整数
   */
  isInteger: function (value) {
    if (value >= 0 && value <= 100) {
      var re = /^[0-9]+$/;
      if (re.test(value)) {
        return true;
      }
    }
    return false;
  },
  /**
   * 查找对讲对象,通过固定条件回调事件
   */
  findInterlocutorByFixedConditionCallback: function (data) {
    if (data.success) {
      var $this = this;
      var list = data.obj;
      var html = '';
      list.map(function (item) {
        html += '<li>'
          + '<span>' + item.monitorName + '</span>'
          + '<div>'
          + '<input data-name="' + item.monitorName + '" data-interlocutorId="' + item.interlocutorId + '" data-monitorId="' + item.monitorId + '" type="checkbox" checked />'
          + '</div>'
          + '</li>';
      });
      $('#checkedPersonNumber').text(list.length);
      $('#personSelected').prop('checked', true);
      $('#listMain ul').html(html);
      $this.personListSelectedHandler();
    }
  },
  /**
   * 列表选择
   */
  personSelectedHandler: function () {
    if ($('#personSelected').is(':checked')) {
      $('#listMain input').each(function (item, element) {
        $(element).prop('checked', true);
      });
      $('#checkedPersonNumber').text($('#listMain input').length);
    } else {
      $('#listMain input').each(function (item, element) {
        $(element).prop('checked', false);
      });
      $('#checkedPersonNumber').text(0);
    }
  },

  /**
   * 人员列表对讲对象勾选响应事件
   */
  personListSelectedHandler: function () {
    $('#listMain input').on('change', function (e) {
      var number = Number($('#checkedPersonNumber').text());
      var allCheckBox = $('#listMain input').length;
      var selectCheckBox = $('#listMain input:checked').length;
      if ($(e.target).is(':checked')) {
        $('#checkedPersonNumber').text(number + 1);
        if (allCheckBox === selectCheckBox) {
          $('#personSelected').prop('checked', true);
        }
      } else {
        $('#checkedPersonNumber').text(number - 1);
        if (selectCheckBox === 0) {
          $('#personSelected').prop('checked', false);
        }
      }
    });

  }
};