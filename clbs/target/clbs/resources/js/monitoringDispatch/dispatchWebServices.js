var DispatchWebServices = function (options) {
  this._dispatchModule = options.dispatchModule;
  this.hjMediaEngine = null;
  this.hjEventEngine = null;
  this.CallModeEnum = null; // 呼叫模式
  this.callMode = null; // 记录呼叫模式
  this.tempCreateCallback = null;
  this.loginResponseStatus = false;
  // this.initEventService();
  // this.initDispatchService();
};

DispatchWebServices.prototype = {
  /**
   * 登录调度服务和通知服务
   */
  login: function () {
    this.initEventService();
    this.initDispatchService();
  },
  /**
   * JS通知服务初始化数据
   */
  initEventService: function () {
    var $this = this;
    var loginData = $this._dispatchModule.get('data').getDispatchLoginData();
    var eventGlobalConfig = {
      serverIP: loginData.audioServerIP, // '118.190.3.121',
      serverPort: loginData.eventServicePort,
      loginName: loginData.name,
      password: '000000'
    };
    $this.hjEventEngine = hjEventService.createEngine(eventGlobalConfig);
    /**
     * 通知服务登录
     */
    $this.hjEventEngine.login();
    /**
     * 通知服务登录响应事件
     */
    $this.hjEventEngine.onLoginResponse = function (event) {
      // console.log('通知服务登录响应事件', event);
    };// $this.onEventLoginResponse.bind($this);
    /**
     * 客户信息更新事件（添加/更新/删除）
     */
    $this.hjEventEngine.onCustomerUpdateEvent = $this.onCustomerUpdateEvent.bind($this);
    /**
     * 群组信息更新事件（添加/更新/删除）
     */
    $this.hjEventEngine.onGroupUpdateEvent = $this.onGroupUpdateEvent.bind($this);
    /**
     * 用户信息更新事件（添加/更新/删除）
     */
    $this.hjEventEngine.onUserUpdateEvent = $this.onUserUpdateEvent.bind($this);
    /**
     * 用户状态更新事件
     */
    $this.hjEventEngine.onUserStatusUpdateEvent = $this.onUserStatusUpdateEvent.bind($this);
    /**
     * 用户默认组更新事件
     * @param event
     */
    $this.hjEventEngine.onUserDefaultGroupUpdateEvent = $this.onUserDefaultGroupUpdateEvent.bind($this);
  },
  /**
   * 通知服务登录响应事件
   */
  onEventLoginResponse: function (event) {
    // console.log('通知服务登录', event);
  },
  /**
   * 客户信息更新事件（添加/更新/删除）
   */
  onCustomerUpdateEvent: function (event) {
    // console.log('客户信息更新事件', event);
  },
  /**
   * 群组信息更新事件（添加/更新/删除）
   * @param event
   */
  onGroupUpdateEvent: function (event) {
    // console.log('群组信息更新事件', event);
  },
  /**
   * 用户信息更新事件（添加/更新/删除）
   * @param event
   */
  onUserUpdateEvent: function (event) {
    // console.log('用户信息更新事件', event);
  },
  /**
   * 用户状态更新事件
   * @param event
   */
  onUserStatusUpdateEvent: function (event) {
    // console.log('用户状态更新事件', event);
    this._dispatchModule.get('dispatchTree').updateTreeStatus(event.event);
  },
  /**
   * 用户默认组更新事件
   * @param event
   */
  onUserDefaultGroupUpdateEvent: function (event) {
    // console.log('用户默认组更新事件', event);
    this._dispatchModule.get('dispatchTree').userDefaultGroupUpdateEvent(event.event);
  },
  /**
   * JS调度服务初始化数据
   */
  initDispatchService: function () {
    var $this = this;
    /**
     * JS调度服务接口定义
     * 创建服务引擎
     */
    var loginData = $this._dispatchModule.get('data').getDispatchLoginData();
    var mediaGlobalConfig = {
      audioEngine: true,
      videoEngine: false,
      loginName: loginData.name,
      password: '000000',
      attribute: loginData.attributes,
      serverIP: loginData.audioServerIP,
      serverPort: loginData.dispatchServicePort,
      playElement: document.getElementById('received_video')
    };
    $this.hjMediaEngine = hjMediaService.createEngine(mediaGlobalConfig);
    $this.CallModeEnum = hjMediaService.CallModeEnum;
    /**
     * 调度服务登录
     */
    $this.hjMediaEngine.audioEngine.login();
    /**
     * 调度服务登录响应事件
     */
    $this.hjMediaEngine.audioEngine.onLoginResponse = $this.onLoginResponse.bind($this);
    /**
     * 调度服务登出事件
     */
    $this.hjMediaEngine.audioEngine.onLogout = $this.onLogout.bind($this);
    /**
     * 调度服务临时组列表事件
     */
    $this.hjMediaEngine.audioEngine.onTempGroupList = $this.onTempGroupList.bind($this);
    /**
     * 主呼响应事件
     */
    $this.hjMediaEngine.audioEngine.onCallingStartResponse = $this.onCallingStartResponse.bind($this);
    /**
     * 主呼停止事件
     */
    $this.hjMediaEngine.audioEngine.onCallingStop = $this.onCallingStop.bind($this);
    /**
     * 创建临时组响应事件
     * @param event
     */
    $this.hjMediaEngine.audioEngine.onCreateTempGroupResponse = $this.onCreateTempGroupResponse.bind($this);
    /**
     * 临时组更新事件
     * 在其它用户创建或者删除临时组后，对讲服务会推送新的临时组信息
     */
    $this.hjMediaEngine.audioEngine.onTempGroupUpdate = $this.onTempGroupUpdate.bind($this);
    /**
     * 添加临时组成员响应事件
     * @param event
     */
    $this.hjMediaEngine.audioEngine.onAddTempGroupMemberResponse = $this.onAddTempGroupMemberResponse.bind($this);
    /**
     * 监听语音开始事件
     */
    // $this.hjMediaEngine.audioEngine.onInterceptedAudioStart = $this.onInterceptedAudioStart.bind($this);
    /**
     * 监听语音结束事件
     */
    // $this.hjMediaEngine.audioEngine.onInterceptedAudioEnd = $this.onInterceptedAudioEnd.bind($this);
  },
  /**
   * 调度服务登录响应事件
   */
  onLoginResponse: function (event) {
    // console.log('登录响应事件', event);
    this.loginResponseStatus = true;
  },
  /**
   * 调度服务登出事件
   */
  onLogout: function () {
    this.loginResponseStatus = false;
  },
  /**
   * 调度服务临时组列表事件
   */
  onTempGroupList: function (event) {
    // console.log('调度服务临时组列表事件', event);
    this._dispatchModule.get('dispatchTree').dispatchTempGroupData(event);
  },
  /**
   * 组呼
   */
  startGroupCalling: function (id) {
    var $this = this;
    var callingStartParam = {
      callMode: $this.CallModeEnum.GROUP_CALL_MODE,
      targetIdType: hjMediaService.GroupIDTypeEnum.GROUP_ID_TYPE_ID,
      targetId: id
    };
    $this.callMode = $this.CallModeEnum.GROUP_CALL_MODE;
    $this.hjMediaEngine.audioEngine.startCalling(callingStartParam);
  },
  /**
   * 主呼响应事件
   */
  onCallingStartResponse: function (event) {
    // console.log('主呼响应事件', event);
    var $this = this;
    if ($this.callMode === 1) { // 组呼响应触发事件
      $this._dispatchModule.get('dispatch').dispatchGroupCallingResponse(event);
    } else if ($this.callMode === 0) { // 个呼响应触发事件
      $this._dispatchModule.get('dispatch').dispatchMonitorCallingResponse(event);
    } else if ($this.callMode === 2) { // 双工 电话
      $this._dispatchModule.get('dispatch').dispatchMonitorPhoneResponse(event);
    }
  },
  /**
   * 停止组呼
   */
  stopCalling: function () {
    this.hjMediaEngine.audioEngine.stopCalling();
  },
  /**
   * 主呼停止事件
   * 主动停止主呼时不会触发主呼停止事件
   */
  onCallingStop: function (event) {

    // console.log('主呼停止事件', event);
  },
  /**
   * 添加监听对象(群组=》加入)
   */
  addInterceptObject: function (id) {
    var interceptObjectParam = {
      objectList: [
        {
          objectType: 0,
          objectId: id
        }
      ]
    };
    this.hjMediaEngine.audioEngine.addInterceptObject(interceptObjectParam);
  },
  /**
   * 删除监听对象（群组）
   */
  removeInterceptObject: function () {
    var interceptObjectParam = {
      objectList: [
        {
          objectType: 0,
          objectId: id
        }
      ]
    };
    this.hjMediaEngine.audioEngine.removeInterceptObject(interceptObjectParam);
  },
  /**
   * 个呼
   */
  startIndividualCalling: function (id) {
    var $this = this;
    var callingStartParam = {
      callMode: $this.CallModeEnum.INDIVIDUAL_CALL_MODE,
      targetIdType: hjMediaService.UserIDTypeEnum.USER_ID_TYPE_ID,
      targetId: id
    };
    $this.callMode = $this.CallModeEnum.INDIVIDUAL_CALL_MODE;
    $this.hjMediaEngine.audioEngine.startCalling(callingStartParam);
  },
  /**
   * 电话(双工)
   */
  startDuplexCalling: function (id) {
    var $this = this;
    var callingStartParam = {
      callMode: $this.CallModeEnum.DUPLEX_CALL_MODE,
      targetIdType: hjMediaService.UserIDTypeEnum.USER_ID_TYPE_ID,
      targetId: id
    };

    $this.callMode = $this.CallModeEnum.DUPLEX_CALL_MODE;
    $this.hjMediaEngine.audioEngine.startCalling(callingStartParam);
  },
  /**
   * 摇晕(禁言)
   */
  remoteFainMs: function (id) {
    var remoteControlParam = {
      controlCmd: 0,
      targetMsId: id
    };
    this.hjMediaEngine.audioEngine.remoteControlMs(remoteControlParam);
  },
  /**
   * 摇醒(取消禁言)
   */
  remoteWakeMs: function (id) {
    var remoteControlParam = {
      controlCmd: 1,
      targetMsId: id
    };
    this.hjMediaEngine.audioEngine.remoteControlMs(remoteControlParam);
  },
  /**
   * 发送短消息
   */
  sendSMS: function (type, id, content) {
    var smsParam = {
      smsType: 0,
      targetObjectList: [
        {
          objectType: type,
          objectId: id
        }
      ],
      smsContent: content
    };
    this.hjMediaEngine.audioEngine.sendSMS(smsParam);
  },
  /**
   * 创建临时组
   */
  createTempGroup: function (name, ids, callback) {
    var $this = this;
    var tempGroupParam = {
      tempGroupName: name,
      tempGroupMemberMsIdList: ids
    };
    $this.tempCreateCallback = callback;
    $this.hjMediaEngine.audioEngine.createTempGroup(tempGroupParam);
  },
  /**
   * 创建临时组响应事件
   */
  onCreateTempGroupResponse: function (event) {
    // console.log('创建临时组响应事件', event);
    this._dispatchModule.get('dispatchTaskGroup').tempGroupCerateSuccess(event);
  },
  /**
   * 删除临时组
   */
  deleteTempGroup: function (id) {
    var tempGroupParam = {
      tempGroupId: id
    };
    this.hjMediaEngine.audioEngine.deleteTempGroup(tempGroupParam);
  },
  /**
   * 临时组更新事件
   * 在其它用户创建或者删除临时组后，对讲服务会推送新的临时组信息
   */
  onTempGroupUpdate: function (event) {
    // console.log('临时组更新事件', event);
    if (event._opType === 2) { // 临时组删除
      this._dispatchModule.get('dispatchTree').tempGroupDeleteEvent(event);
    }
  },
  /**
   * 添加临时组成员
   */
  addTempGroupMember: function (tempGroupId, ids) {
    var tempGroupMemberParam = {
      tempGroupId: tempGroupId,
      tempGroupMemberMsIdList: ids
    };
    this.hjMediaEngine.audioEngine.addTempGroupMember(tempGroupMemberParam);
  },
  /**
   * 添加临时组成员响应事件
   */
  onAddTempGroupMemberResponse: function (event) {
    // console.log('添加临时组成员响应事件', event);
    this._dispatchModule.get('dispatchTaskGroup').addTempGroupMemberSuccess(event);
  },
  /**
   * 删除临时组成员
   */
  removeTempGroupMember: function (tempGroupId, ids) {
    var tempGroupMemberParam = {
      tempGroupId: tempGroupId,
      tempGroupMemberMsIdList: ids
    };
    this.hjMediaEngine.audioEngine.removeTempGroupMember(tempGroupMemberParam);
  },
  /**
   * 切换用户默认组
   */
  changeMsDefaultGroup: function (groupId, ids) {
    var changeGroupParam = {
      groupId: groupId,
      msIdList: ids
    };
    this.hjMediaEngine.audioEngine.changeMsDefaultGroup(changeGroupParam);
  },
  /**
   * 监听语音开始事件
   */
  onInterceptedAudioStart: function (event) {
    // console.log('监听语音开始事件', event);
    this._dispatchModule.get('dispatch').onInterceptedAudioStart(event);
  },
  /**
   * 监听语音结束事件
   */
  onInterceptedAudioEnd: function (event) {
    // console.log('监听语音结束事件', event);
    this._dispatchModule.get('dispatch').onInterceptedAudioEnd(event);
  },
  /**
   * 加入群組(加入)
   */
  joinGroup: function (id) {
    var groupInfo = {
      groupId: id
    };
    this.hjMediaEngine.audioEngine.joinGroup(groupInfo);
  },
  /**
   * 退出群組
   */
  exitGroup: function (id) {
    var groupInfo = {
      groupId: id
    };
    this.hjMediaEngine.audioEngine.exitGroup(groupInfo);
  }
};