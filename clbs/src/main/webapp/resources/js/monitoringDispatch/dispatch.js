var Dispatch = function (options) {
  this._dispatchModule = options.dispatchModule;
  this.groupCallingTimeout = null;
  this.monitorCallingTimeout = null;
  this.monitorPhoneTimeout = null;
  this.init();
};

Dispatch.prototype = {
  /**
   * 初始化数据
   */
  init: function () {
    var $this = this;
    $this.getDispatchPid();
    $('#dispatchViewClose').on('click', $this.dispatchViewCloseHandler.bind($this));
    $('#dispatchViewLabel').on('click', $this.dispatchViewShowHandler.bind($this));
    $('#groupCalling').on('click', $this.dispatchGroupCalling.bind($this));
    $('#groupListening').on('click', $this.dispatchGroupListening.bind($this));
    $('#monitorCalling').on('click', $this.dispatchMonitorCalling.bind($this));
    $('#monitorPhoneEvent').on('click', $this.dispatchMonitorPhoneEvent.bind($this));
    $('#banned').on('click', $this.dispatchBanned.bind($this));
    // $('#kickedOut').on('click', $this.dispatchKickedOut.bind($this));
    $('#robToMak').on('click', $this.dispatchRobToMak.bind($this));
    $('#groupStopCalling').on('click', $this.dispatchGroupStopCalling.bind($this));
    $('#monitorStopCalling').on('click', $this.dispatchMonitorStopCalling.bind($this));
    $('#sendNoticeInfo').on('click', $this.sendNoticeInfoHandler.bind($this));
    $('#recentlyInformedCell').on('click', $this.notificationRecordList.bind($this));
    $('#noticeContent').on('input', $this.noticeInfosChange.bind($this));
    $('#groupStopPhone').on('click', $this.dispatchMonitorStopPhone.bind($this));
    $('#openLocation').on('click', $this.dispatchOpenLocation.bind($this));
  },
  /**
   * 调度视图显示与隐藏
   */
  dispacthViewStateHandler: function () {
    var flag = this._dispatchModule.get('data').getSchedulingViewState(); // 右侧面板是有有激活的 group组呼/group抢麦/（车、人、物）个呼/（车、人、物）拨打电话 功能
    var type = this._dispatchModule.get('data').getDispatchType(); // 当前群组结点（2）是群组还是车、人、物结点（1）

    if (flag) {
      $('.single-object').hide(); // 先隐藏所有面板
      $('#singleObjectGeneral').hide(); // 监控对象（操作按钮）面板

      if (type === 1) {
        this.dispatchMonitor(); // 初始化面板内容
        $('#dispatchGroup').hide(); // 群组信息面板
        $('#groupGeneral').hide(); // 群组通用（操作按钮）面板
        $('#singleObjectGeneral').show(); // 监控对象（操作按钮）面板
        var nodeType = this._dispatchModule.get('data').getDispatchNode().type; // 当前选中结点的类型
        if (nodeType === 'people') {
          $('#personnelSingleObject').show(); // 监控对象信息面板
        } else if (nodeType === 'vehicle') {
          $('#vehicleSingleObject').show(); // 监控对象信息面板
        } else if (nodeType === 'thing') {
          $('#thingSingleObject').show(); // 监控对象信息面板
        }

        // $('#singleObjectGeneral').show(); // 监控对象（操作按钮）面板
      } else {
        this.dispatchAssignment();
        $('#singleObjectGeneral').hide();
        $('#dispatchGroup').show();
        $('#groupGeneral').show();
      }

      // 通知面板相关逻辑
      $('#informedCell').attr('class', 'active');
      $('#informed').attr('class', 'tab-pane notice-infos active');
      $('#recentlyInformed').removeClass('active');
      $('#recentlyInformedCell').attr('class', '');
      $('#noticeContent').val('');
      $('#recentlyInformedTable tbody').html('');

      $('#dispatchView').attr('class', 'dispatch-view active'); // 右侧面板容器
      $('#rightContentMain').attr('class', 'right-content-main active'); // 地图容器
      $('#dispatchViewLabel').hide(); // 右侧面板容器开关按钮（面板隐藏时显示）
    } else {
      $('#dispatchView').attr('class', 'dispatch-view');
      $('#rightContentMain').attr('class', 'right-content-main');

      setTimeout(function () {
        $('#dispatchViewLabel').show();
      }, 500);
    }
  },
  /**
   * 关闭调度视图
   */
  dispatchViewCloseHandler: function () {
    this._dispatchModule.get('data').setSchedulingViewState(false);
  },
  /**
   * 展开调度视图
   */
  dispatchViewShowHandler: function () {
    this._dispatchModule.get('data').setSchedulingViewState(true);
  },
  /**
   * 获取调度服务会话ID
   */
  getDispatchPid: function () {
    var $this = this;
    $this._dispatchModule.get('dispatchServices').dispatchLogin($this.getDispatchPidCallback.bind($this));
  },
  /**
   * 获取调度服务会话ID回调事件
   * @param data
   */
  getDispatchPidCallback: function (data) {
    if (data.success) {
      this._dispatchModule.get('data').setDispatchLoginData(data.obj.data);
      this._dispatchModule.get('dispatchWebServices').login();
      this._dispatchModule.get('dispatchTree').getTemporaryGroupData();
    }
  },
  /**
   * 调度监控对象
   */
  dispatchMonitor: function () {
    var $this = this;
    var dispatchNode = $this._dispatchModule.get('data').getDispatchNode();
    var params = {
      // monitorId: dispatchNode.id,
      userId: dispatchNode.interlocutorId
    };

    // 当前选中的监控调度对象信息查询
    $this._dispatchModule.get('dispatchServices').getMonitorBounced(
      params,
      $this.dispatchMonitorCallback.bind($this, dispatchNode.type, dispatchNode.name) // 设置面板的值
    );
    $this.dispatchMonitorStatus(dispatchNode); // 根据监控对象的在线状态处理相关的逻辑
  },
  /**
   * 调度监控对象状态判断
   * 离线的监控对象的调度功能图标为灰色，不可点击
   */
  dispatchMonitorStatus: function (dispatchNode) {
    if (dispatchNode.audioOnlineStatus === 1) { // 在线
      $('#monitorCalling,#monitorPhoneEvent,#banned,#openLocation').removeClass('failure');
      var loginData = this._dispatchModule.get('data').getDispatchLoginData();
      if (!loginData.isOwnPreventSpeechRole) {
        $('#banned').addClass('failure');
      }
    } else if (dispatchNode.audioOnlineStatus === 0) { // 离线
      $('#monitorCalling,#monitorPhoneEvent,#banned,#openLocation').addClass('failure');
    }
  },
  /**
   * 调度监控对象查询回调事件
   */
  dispatchMonitorCallback: function (type, name, data) {
    if (data.success) {
      var nodeType = "";
      var obj = data.obj;
      var monitorType = obj.monitorType;
      if (monitorType === '1') {
        nodeType = "people";
        var groupName = obj.groupName === null || obj.groupName === undefined ? '' : obj.groupName;
        var jobName = obj.jobName === null || obj.jobName === undefined ? '' : obj.jobName;
        var skillNames = obj.skillNames === null || obj.skillNames === undefined ? '' : obj.skillNames;
        var driverTypeNames = obj.driverTypeNames === null || obj.driverTypeNames === undefined ? '' : obj.driverTypeNames;
        var qualificationName = obj.qualificationName === null || obj.qualificationName === undefined ? '' : obj.qualificationName;
        var gender = obj.gender === null || obj.gender === undefined ? '' : obj.gender;
        var bloodTypeName = obj.bloodTypeName === null || obj.bloodTypeName === undefined ? '' : obj.bloodTypeName;
        var age = obj.age === null || obj.age === undefined ? '' : obj.age;
        var phone = obj.phone === null || obj.phone === undefined ? '' : obj.phone;
        var userId = obj.userId;
        var name = obj.peopleNumber;
        var id = obj.id;

        $('#dispatchGroupName').text(groupName);
        $('#jobName').text(jobName);
        $('#skillNames').text(skillNames);
        $('#driverTypeNames').text(driverTypeNames);
        $('#qualificationName').text(qualificationName);
        $('#gender').text(gender);
        $('#bloodTypeName').text(bloodTypeName);
        $('#age').text(age);
        $('#phone').text(phone);

        $('#dispatchView').attr('data-id', userId).attr('data-zid', id);

        $('#callObjName').text(name).data('name', name);
        $('#dispatchViewLabel span').text(name);
      } else if (monitorType === '0') {
        nodeType = "vehicle";
        var vehicleName = obj.vehicleNumber || name;
        var userId = obj.userId;
        var id = obj.id;
        var vehColor = ['黑色', '白色', '红色', '蓝色', '紫色', '黄色', '绿色', '粉色', '棕色', '灰色'][+obj.vehicleColor];

        $('#vehicleGroupName').text(obj.groupName);
        $('#brand').text(obj.brand);
        $('#purposeCategory').text(obj.purposeCategory);
        $('#vehicleColor').text(vehColor);
        $('#plateColor').text(getPlateColor(obj.plateColor));
        $('#vehicleCategoryName').text(obj.vehicleCategoryName);
        $('#vehType').text(obj.vehType);
        $('#isStart').text(['停用', '启用'][+obj.isStart]);
        $('#remark').text(obj.remark);

        $('#callObjName').text(vehicleName).data('name', vehicleName);
        $('#dispatchViewLabel span').text(vehicleName);

        $('#dispatchView').attr('data-id', userId).attr('data-zid', id);
      } else if (monitorType === '2') {
        nodeType = "thing";
        var thingName = obj.thingNumber || name;
        var userId = obj.userId;
        var id = obj.id;

        $('#thingNumber').text(obj.thingNumber);
        $('#name').text(obj.name);
        $('#thingGroupName').text(obj.groupName);
        $('#category').text(obj.category);
        $('#type').text(obj.type);
        $('#label').text(obj.label);
        $('#model').text(obj.model);
        $('#material').text(obj.material);
        $('#weight').text(obj.weight);
        $('#spec').text(obj.spec);
        $('#thingRemark').text(obj.remark);

        $('#callObjName').text(thingName).data('name', thingName);
        $('#dispatchViewLabel span').text(thingName);

        $('#dispatchView').attr('data-id', userId).attr('data-zid', id);
      } else {
        $('#callObjName').text(name + '（监控对象类型有误）').data('name', name);
        $('#dispatchViewLabel span').text(name);

        $('#dispatchView').attr('data-id', '').attr('data-zid', '');
      }
      this._dispatchModule.get('data').getDispatchNode().type = nodeType;
    } else {
      $('#callObjName').text(name + '（未获取到详细信息）').data('name', name);
      $('#dispatchViewLabel span').text(name);

      $('#dispatchView').attr('data-id', '').attr('data-zid', '');
    }
  },
  /**
   * 调度群组
   */
  dispatchAssignment: function () {
    var $this = this;
    var dispatchNode = $this._dispatchModule.get('data').getDispatchNode();
    var params = {
      // assignmentId: dispatchNode.id,
      userId: dispatchNode.intercomGroupId
    };
    $this._dispatchModule.get('dispatchServices').getAssignmentBounced(
      params,
      $this.dispatchAssignmentCallback.bind($this)
    );
  },
  /**
   * 调度群组回调事件
   * @param data
   */
  dispatchAssignmentCallback: function (data) {
    if (data.success) {
      var obj = data.obj;
      var groupName = obj.groupName === null || obj.groupName === undefined ? '' : obj.groupName;
      var organizationCode = obj.organizationCode === null || obj.organizationCode === undefined ? '' : obj.organizationCode;
      var address = obj.address === null || obj.address === undefined ? '' : obj.address;
      var contactName = obj.contactName === null || obj.contactName === undefined ? '' : obj.contactName;
      var phone = obj.phone === null || obj.phone === undefined ? '' : obj.phone;
      var description = obj.description === null || obj.description === undefined ? '' : obj.description;
      var userId = obj.userId;
      var name = obj.name;
      var id = obj.id;
      $('#organisation').text(groupName);
      $('#organizationCode').text(organizationCode);
      $('#address').text(address);
      $('#contactName').text(contactName);
      $('#contactPhone').text(phone);
      $('#description').text(description);
      $('#dispatchView').attr('data-id', userId).attr('data-zid', id);
      $('#callObjName').text(name).data('name', name);
      $('#dispatchViewLabel span').text(name);
    }
  },
  /**
   * 组呼
   */
  dispatchGroupCalling: function () {
    var $this = this;
    if (!$('#robToMak').hasClass('active')) {
      $('#groupCalling').attr('data-active', true);
      if (!$('#groupCalling').hasClass('active')) {
        var loginResponseStatus = $this._dispatchModule.get('dispatchWebServices').loginResponseStatus;
        if (loginResponseStatus) {
          var dispatchNode = $this._dispatchModule.get('data').getDispatchNode();
          if ($this.isAssignmentObjOnLine(dispatchNode.intercomGroupId)) {
            var id = $('#dispatchView').attr('data-id');
            $this._dispatchModule.get('dispatchWebServices').startGroupCalling(id);
            $('#groupCallName').text($('#callObjName').data('name'));
            $('#groupCall').show();

            if ($('#robToMak').attr('data-active')) {
              $('#robToMak').addClass('active');
              var id = $('#dispatchView').attr('data-zid');
              $this._dispatchModule.get('dispatchServices').addLog({
                type: 9,
                id: id
              });
            } else {
              $('#groupCalling').addClass('active');
              $this.groupCallingTimeout = setTimeout(function () {
                $this.dispatchGroupStopCalling();
              }, 35000);
              var id = $('#dispatchView').attr('data-zid');
              $this._dispatchModule.get('dispatchServices').addLog({
                type: 6,
                id: id
              });

              // 修改组呼状态文字
              $('#groupCalling .text span').text('关闭组呼');
            }
            $('#robToMak,#groupCalling').removeAttr('data-active');
          }
        } else {
          layer.msg('服务异常，请稍后重试', {offset: 't'});
        }
      } else {
        this.dispatchGroupStopCalling();
      }
    } else {
      layer.msg('请先停止抢麦', {offset: 't'});
    }
  },
  /**
   * 组（群）呼响应事件
   */
  dispatchGroupCallingResponse: function (event) {
    if (event.result === 0) {

    } else if (event.result === 1) {
      layer.msgBody('呼叫对象不在线', {offset: 't'});
    }
  },
  /**
   *  个呼响应事件
   */
  dispatchMonitorCallingResponse: function (event) {
    if (event.result === 0) {

    } else if (event.result === 1) {
      layer.msgBody('呼叫对象不在线', {offset: 't'});
    }
  },
  /**
   * 电话响应事件
   */
  dispatchMonitorPhoneResponse: function (event) {
    var $this = this;
    if (event.result === 0) {
      clearTimeout($this.monitorPhoneTimeout);
    } else if (event.result === 1) {
      layer.msgBody('呼叫对象不在线', {offset: 't'});
    }
  },
  /**
   * 加入
   */
  dispatchGroupListening: function () {
    var $this = this;
    var id = Number($('#dispatchView').attr('data-id'));
    if (!$('#groupListening').hasClass('active')) {
      var loginResponseStatus = $this._dispatchModule.get('dispatchWebServices').loginResponseStatus;
      if (loginResponseStatus) {
        this._dispatchModule.get('dispatchWebServices').joinGroup(id);
        layer.msg('加入成功', {offset: 't'});
        $('#groupListening').addClass('active');
        var id = $('#dispatchView').attr('data-zid');
        $this._dispatchModule.get('dispatchServices').addLog({
          type: 7,
          id: id
        });
        // 修改加入群组状态文字
        $('#groupListening .text span').text('退出群组');
      }
    } else {
      this._dispatchModule.get('dispatchWebServices').exitGroup(id);
      layer.msg('退出成功', {offset: 't'});
      $('#groupListening').removeClass('active');
      var id = $('#dispatchView').attr('data-zid');
      $this._dispatchModule.get('dispatchServices').addLog({
        type: 8,
        id: id
      });
      // 修改抢麦状态文字
      $('#groupListening .text span').text('加入群组');
    }
  },
  /**
   * 个呼
   */
  dispatchMonitorCalling: function () {
    var $this = this;
    if (!$('#monitorPhoneEvent').hasClass('active')) {
      if (!$('#monitorCalling').hasClass('failure')) {
        if (!$('#monitorCalling').hasClass('active')) {
          var loginResponseStatus = $this._dispatchModule.get('dispatchWebServices').loginResponseStatus;
          /**
           * 判断调度服务是否掉线
           */
          if (loginResponseStatus) {
            var id = Number($('#dispatchView').attr('data-id'));
            this._dispatchModule.get('dispatchWebServices').startIndividualCalling(id);
            // 修改个呼状态文字
            $('#monitorCalling .text span').text('关闭个呼');
            /**
             * 显示个呼弹窗
             */
            $('#monitorCalling').addClass('active');
            $('#monitorCallName').text($('#callObjName').data('name'));
            $('#monitorCall').show();
            /**
             * 35秒后关闭个呼
             */
            $this.monitorCallingTimeout = setTimeout(function () {
              $this.dispatchMonitorStopCalling();
            }, 35000);
            /**
             * 添加个呼日志
             */
            var id = $('#dispatchView').attr('data-zid');
            $this._dispatchModule.get('dispatchServices').addLog({
              type: 1,
              id: id
            });
          } else {
            layer.msg('服务异常，请稍后重试', {offset: 't'});
          }
        } else {
          this.dispatchMonitorStopCalling();
        }
      }
    } else {
      layer.msg('请先停止电话', {offset: 't'});
    }
  },
  /**
   * 电话
   */
  dispatchMonitorPhoneEvent: function () {
    var $this = this;
    if (!$('#monitorCalling').hasClass('active')) {
      if (!$('#monitorPhoneEvent').hasClass('failure')) {
        if (!$('#monitorPhoneEvent').hasClass('active')) {
          var loginResponseStatus = $this._dispatchModule.get('dispatchWebServices').loginResponseStatus;
          if (loginResponseStatus) {
            var id = Number($('#dispatchView').attr('data-id'));
            this._dispatchModule.get('dispatchWebServices').startDuplexCalling(id);
            // 修改电话状态文字
            $('#monitorPhoneEvent .text span').text('断开电话');

            /**
             * 展示电话弹窗
             */
            $('#monitorPhoneEvent').addClass('active');
            $('#monitorPhoneName').text($('#callObjName').data('name'));
            $('#monitorPhone').show();
            /**
             * 30秒后关闭电话
             */
            $this.monitorPhoneTimeout = setTimeout(function () {
              $this.dispatchMonitorStopPhone();
            }, 30000);
            /**
             * 添加电话日志记录
             */
            var id = $('#dispatchView').attr('data-zid');
            $this._dispatchModule.get('dispatchServices').addLog({
              type: 2,
              id: id
            });
          } else {
            layer.msg('服务异常，请稍后重试', {offset: 't'});
          }
        } else {
          this.dispatchMonitorStopPhone();
        }
      }
    } else {
      layer.msg('请先停止个呼', {offset: 't'});
    }
  },
  /**
   * 禁言
   */
  dispatchBanned: function () {
    var $this = this;
    var id = $('#dispatchView').attr('data-id');
    if (!$('#banned').hasClass('failure')) {
      if (!$('#banned').hasClass('active')) {
        var loginResponseStatus = $this._dispatchModule.get('dispatchWebServices').loginResponseStatus;
        if (loginResponseStatus) {
          $('#banned').addClass('active');
          $this._dispatchModule.get('dispatchWebServices').remoteFainMs(id);
          layer.msg('禁言成功', {offset: 't'});
          // 修改禁言状态文字
          $('#banned .text span').text('关闭禁言');

          /**
           * 添加禁言日志记录
           */
          var id = $('#dispatchView').attr('data-zid');
          $this._dispatchModule.get('dispatchServices').addLog({
            type: 3,
            id: id
          });
        } else {
          layer.msg('服务异常，请稍后重试', {offset: 't'});
        }
      } else {
        $('#banned').removeClass('active');
        $this._dispatchModule.get('dispatchWebServices').remoteWakeMs(id);
        layer.msg('取消禁言成功', {offset: 't'});
        // 修改禁言状态文字
        $('#banned .text span').text('启用禁言');
        /**
         * 取消禁言日志记录
         */
        var id = $('#dispatchView').attr('data-zid');
        $this._dispatchModule.get('dispatchServices').addLog({
          type: 4,
          id: id
        });
      }
    }
  },
  /**
   * 踢出
   */
  // dispatchKickedOut: function () {
  //   var $this = this;
  //   layer.confirm('您确定要将该监控对象踢出群组吗？', {
  //     title: '操作确认',
  //     icon: 3, // 问号图标
  //     move: false,//禁止拖动
  //     btn: ['确定', '取消']
  //   }, function () {
  //     layer.closeAll();
  //   }, function () {
  //
  //   });
  // },
  /**
   * 抢麦
   */
  dispatchRobToMak: function () {
    var $this = this;
    if (!$('#groupCalling').hasClass('active')) {
      $('#robToMak').attr('data-active', true);
      if (!$('#robToMak').hasClass('active')) {
        var loginResponseStatus = $this._dispatchModule.get('dispatchWebServices').loginResponseStatus;
        /**
         * 判断调度服务是否掉线
         */
        if (loginResponseStatus) {
          var dispatchNode = $this._dispatchModule.get('data').getDispatchNode();
          /**
           * 判断群组内是否有在线监控对象
           */
          if ($this.isAssignmentObjOnLine(dispatchNode.intercomGroupId)) {
            var id = Number($('#dispatchView').attr('data-id'));
            this._dispatchModule.get('dispatchWebServices').startGroupCalling(id);
            $this.groupCallingTimeout = setTimeout(function () {
              $this.dispatchGroupStopCalling();
            }, 35000);
            $('#groupCallName').text($('#callObjName').data('name'));
            $('#groupCall').show();
            if ($('#robToMak').attr('data-active')) {
              $('#robToMak').addClass('active');
              var id = $('#dispatchView').attr('data-zid');
              $this._dispatchModule.get('dispatchServices').addLog({
                type: 9,
                id: id
              });

              // 修改抢麦状态文字
              $('#robToMak .text span').text('关闭抢麦');
            } else {
              $('#groupCalling').addClass('active');
              var id = $('#dispatchView').attr('data-zid');
              $this._dispatchModule.get('dispatchServices').addLog({
                type: 6,
                id: id
              });
            }
            $('#robToMak,#groupCalling').removeAttr('data-active');
          }
        }
      } else {
        this.dispatchGroupStopCalling();
      }
    } else {
      layer.msg('请先停止组呼', {offset: 't'});
    }
  },
  /**
   * 调度通知记录的最近通知
   */
  notificationRecordList: function () {
    var $this = this;
    var dispatchNode = $this._dispatchModule.get('data').getDispatchNode();
    var receiveId;
    if (dispatchNode.type === 'assignment') {
      receiveId = dispatchNode.intercomGroupId;
    } else {
      receiveId = dispatchNode.interlocutorId;
    }
    var id = $('#dispatchView').attr('data-zid');
    var params = {
      receiveId: id,
      pageSize: 1,
      limitSize: 5
    };
    $this._dispatchModule.get('dispatchServices').notificationRecordList(
      params,
      $this.notificationRecordListCallback.bind($this)
    );
  },
  /**
   * 调度通知记录的最近通知查询结束事件
   */
  notificationRecordListCallback: function (data) {
    if (data.success) {
      var obj = data.obj;
      var html = '';
      obj.map(function (item) {
        var content = item.content.length > 10 ? item.content.substring(0, 10) + '...' : item.content;
        html += '<tr>'
          + '<td title="' + item.content + '">' + content + '</td>'
          + '<td>' + item.notificationTime + '</td>'
          + '</tr>';
      });
      $('#recentlyInformedTable tbody').html(html);
    }
  },
  /**
   * 保存调度通知记录
   */
  // addNotificationRecord: function () {
  //   var $this = this;
  //   var dispatchNode = $this._dispatchModule.get('data').getDispatchNode();
  //   var content = $('#noticeContent').value();
  //   var params = {
  //     receiveId: dispatchNode.id,
  //     content: content,
  //   };
  //   $this._dispatchModule.get('dispatchServices').addNotificationRecord(
  //     params,
  //     $this.addNotificationRecordCallback.bind($this)
  //   );
  // },
  /**
   * 停止（挂断）组呼
   */
  dispatchGroupStopCalling: function () {
    clearTimeout(this.groupCallingTimeout);
    $('#robToMak,#groupCalling').removeAttr('data-active');
    $('#groupCalling,#robToMak').removeClass('active');
    $('#groupCall').hide();
    this._dispatchModule.get('dispatchWebServices').stopCalling();
    // 修改组呼状态文字
    $('#groupCalling .text span').text('打开组呼');
    // 修改抢麦状态文字
    $('#robToMak .text span').text('开启抢麦');
  },
  /**
   * 停止个呼
   */
  dispatchMonitorStopCalling: function () {
    clearTimeout(this.monitorCallingTimeout);
    $('#monitorCalling').removeClass('active');
    $('#monitorCall').hide();
    this._dispatchModule.get('dispatchWebServices').stopCalling();
    $('#monitorCalling .text span').text('打开个呼');
  },
  /**
   * 停止电话
   */
  dispatchMonitorStopPhone: function () {
    var $this = this;
    clearTimeout($this.monitorPhoneTimeout);
    $('#monitorPhone').hide();
    $('#monitorPhoneEvent').removeClass('active');
    $this._dispatchModule.get('dispatchWebServices').stopCalling();
    $('#monitorPhoneEvent .text span').text('拨打电话');
  },
  /**
   * 发送通知信息
   */
  sendNoticeInfoHandler: function () {
    var $this = this;
    var dispatchNode = $this._dispatchModule.get('data').getDispatchNode();
    var objectType;
    var objectId;
    var type;
    var content = $('#noticeContent').val();
    if (content !== '') {
      var loginResponseStatus = $this._dispatchModule.get('dispatchWebServices').loginResponseStatus;
      if (loginResponseStatus) {
        var dispatchNode = $this._dispatchModule.get('data').getDispatchNode();
        // if ($this.isAssignmentObjOnLine(dispatchNode.intercomGroupId)) {
        if (dispatchNode.type === 'assignment') {
          objectType = 0;
          objectId = dispatchNode.intercomGroupId;
          type = 2;
        } else {
          objectType = 1;
          objectId = dispatchNode.interlocutorId;
          type = 1;
        }

        $this._dispatchModule.get('dispatchWebServices').sendSMS(objectType, objectId, content);

        var id = $('#dispatchView').attr('data-zid');
        var params = {
          receiveId: id,
          content: content,
          type: type
        };
        $this._dispatchModule.get('dispatchServices').addNotificationRecord(
          params,
          $this.addNotificationRecordCallback.bind($this)
        );
      }
    }
  },
  /**
   * 保存调度通知记录回调事件
   * @param data
   */
  addNotificationRecordCallback: function (data) {
    if (data.success) {
      var e = $('#noticeContent').offset();
      var top = e.top + 20;
      var left = e.left + 90;
      layer.msg('通知发送成功', {offset: [top, left]});
      $('#noticeContent').val('');
      $('#wordsNumber').text(0);
      $('#sendNoticeInfo').removeClass('dispatch-primary');
    }
  },
  /**
   * 通知输入内容监听
   */
  noticeInfosChange: function () {
    var value = $('#noticeContent').val();
    if (value === '') {
      $('#sendNoticeInfo').removeClass('dispatch-primary');
    } else {
      $('#sendNoticeInfo').addClass('dispatch-primary');
    }
    $('#wordsNumber').html(value.length);
  },
  /**
   * 开始定位
   */
  dispatchOpenLocation: function () {
    var $this = this;
    if (!$('#openLocation').hasClass('failure')) {
      var loginResponseStatus = $this._dispatchModule.get('dispatchWebServices').loginResponseStatus;
      if (loginResponseStatus) {
        layer.msg('开启定位成功', {offset: 't'});
        var id = $('#dispatchView').attr('data-zid');
        $this._dispatchModule.get('dispatchServices').addLog({
          type: 5,
          id: id
        });
      } else {
        layer.msg('服务异常，请稍后再试', {offset: 't'});
      }
    }
  },
  /**
   * 判断群组内监控对象是否全部离线
   */
  isAssignmentObjOnLine: function (intercomGroupId) {
    var $this = this;
    var assignmentNode = $this._dispatchModule.get('data').getAssignmentNodes(intercomGroupId);
    var status = false;
    if (assignmentNode !== null && assignmentNode !== undefined) {
      $this._dispatchModule.get('dispatchServices').searchGroupOfList(
        {
          intercomGroupId: intercomGroupId,
          interlocutorStatus: 1
        },
        function (data) {
          if (data.success) {
            var obj = data.obj;
            if (obj.length > 0) {
              status = true;
            } else {
              layer.msg('当前群组内无在线用户', {offset: 't'});
              status = false;
            }
          }
        },
        false
      );
    }
    return status;
  },
  /**
   * 调度view初始化
   */
  dispatchViewDefaultStatus: function () {
    /**
     * 停止个呼
     */
    if ($('#monitorCalling').hasClass('active')) {
      this.dispatchMonitorStopCalling();
    }
    /**
     * 停止电话
     */
    if ($('#monitorPhoneEvent').hasClass('active')) {
      this.dispatchMonitorStopPhone();
    }
    /**
     * 关闭禁言
     */
    if ($('#banned').hasClass('active')) {
      $('#banned').removeClass('active');
      $this._dispatchModule.get('dispatchWebServices').remoteWakeMs(id);
      layer.msg('取消禁言成功', {offset: 't'});
      /**
       * 取消禁言日志记录
       */
      var id = $('#dispatchView').attr('data-zid');
      $this._dispatchModule.get('dispatchServices').addLog({
        type: 4,
        id: id
      });
    }
    /**
     * 停止组呼
     */
    if ($('#groupCalling').hasClass('active')) {
      this.dispatchGroupStopCalling();
    }
    /**
     * 取消加入
     */
    if ($('#groupListening').hasClass('active')) {
      this._dispatchModule.get('dispatchWebServices').exitGroup(id);
      layer.msg('退出成功', {offset: 't'});
      $('#groupListening').removeClass('active');
      var id = $('#dispatchView').attr('data-zid');
      $this._dispatchModule.get('dispatchServices').addLog({
        type: 8,
        id: id
      });
    }
    /**
     * 停止抢麦
     */
    if ($('#robToMak').hasClass('active')) {
      this.dispatchGroupStopCalling();
    }
    $('#rightContentMain').removeClass('active');
    $('#dispatchView').removeClass('active');
    $('#groupCalling, #groupListening, #robToMak, #monitorCalling, #monitorPhoneEvent, #banned').remove('active');
    $('#monitorCall, #monitorPhone, #groupCall').hide();
  }
};