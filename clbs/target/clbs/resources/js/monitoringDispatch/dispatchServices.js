var DispatchServices = function () {

};

DispatchServices.prototype = {
  /**
   * 调度服务登录
   */
  dispatchLogin: function (callback) {
    var url = '/clbs/talkback/monitoring/dispatch/dispatchLogin';
    this._ajax(url, 'POST', 'json', true, {}, 8000, callback);
  },
  /**
   * 查找对讲对象,通过画的圆形区域
   * @param data
   * longitude 圆心经度
   * latitude 圆心纬度
   * radius 半径
   * assignmentId 如果是加入分组, 该字段就是加入的分组id，如果是创建该字段为null
   * assignmentType 分组类型 2：任务组; 3:临时组, 如果是创建该字段为null
   * @param callback
   */
  findInterlocutorByCircleArea: function (data, callback) {
    var url = '/clbs/talkback/monitoring/dispatch/findInterlocutorByCircleArea';
    this._ajax(url, 'POST', 'json', true, data, 8000, callback);
  },
  /**
   * 查找对讲对象,通过画的矩形区域
   * @param data
   * assignmentId 如果是加入分组, 该字段就是加入的分组id, 如果是创建该字段为null
   * leftLongitude 矩形区域左上角的经度
   * leftLatitude 矩形区域左上角的纬度
   * rightLongitude 矩形区域右下角的经度
   * rightLatitude 矩形区域右下角的纬度
   * assignmentType 分组类型 2：任务组; 3:临时组, 如果是创建该字段为null
   * @param callback
   */
  findInterlocutorByRectangleArea: function (data, callback) {
    var url = '/clbs/talkback/monitoring/dispatch/findInterlocutorByRectangleArea';
    this._ajax(url, 'POST', 'json', true, data, 8000, callback);
  },
  /**
   * 判断对讲对象的任务组数量是否超出限制
   * @param data
   * interlocutorIds  对讲对象id 逗号分隔
   * @param callback
   */
  judgeInterlocutorTaskAssignmentNumIsOverLimit: function (data, callback) {
    var url = '/clbs/talkback/monitoring/dispatch/judgeInterlocutorTaskAssignmentNumIsOverLimit';
    this._ajax(url, 'POST', 'json', true, data, 8000, callback);
  },
  /**
   * 创建任务组
   * @param data
   * assignmentName 任务组名称
   * interlocutorIds 组内对讲对象id 逗号分隔
   * @param callback
   */
  addTaskAssignmentAndMember: function (data, callback) {
    var url = '/clbs/talkback/monitoring/dispatch/addTaskAssignmentAndMember';
    this._ajax(url, 'POST', 'json', true, data, 8000, callback);
  },
  /**
   * 不可见人员列表
   * @param callback
   */
  getInvisiblePersonList: function (callback) {
    var url = '/clbs/talkback/monitoring/dispatch/invisibleList';
    this._ajax(url, 'POST', 'json', true, {}, 30000, callback);
  },
  /**
   * 不可见人员详情
   * @param data
   * @param callback
   */
  getInvisiblePersonDetails: function (data, callback) {
    var url = '/clbs/talkback/monitoring/dispatch/invisibleDetailsInfo';
    this._ajax(url, 'POST', 'json', true, data, 8000, callback);
  },
  /**
   * 海量点获取
   * @param callback
   */
  getMassPoint: function (callback) {
    var url = '/clbs/talkback/monitoring/dispatch/getMassPoint';
    this._ajax(url, 'POST', 'json', true, {}, 8000, callback);
  },
  /**
   * 点击监控对象获取地图海量点弹框信息
   * @param data
   * @param callback
   */
  getMapPointInfo: function (data, callback) {
    var url = '/clbs/talkback/monitoring/dispatch/getPointInfo';
    this._ajax(url, 'POST', 'json', true, data, 8000, callback);
  },
  /**
   * 获得技能列表
   */
  getAllSkillList: function (callback) {
    var url = '/clbs/talkback/monitoring/dispatch/getAllSkillList';
    this._ajax(url, 'POST', 'json', true, {}, 8000, callback);
  },
  /**
   * 获得对讲机型列表
   */
  getAllIntercomModeList: function (callback) {
    var url = '/clbs/talkback/monitoring/dispatch/getAllIntercomModeList';
    this._ajax(url, 'POST', 'json', true, {}, 8000, callback);
  },
  /**
   * 获得驾照类别列表
   */
  getAllDriverLicenseCategoryList: function (callback) {
    var url = '/clbs/talkback/monitoring/dispatch/getAllDriverLicenseCategoryList';
    this._ajax(url, 'POST', 'json', true, {}, 8000, callback);
  },
  /**
   * 获得资格证列表
   */
  getAllQualificationList: function (callback) {
    var url = '/clbs/talkback/monitoring/dispatch/getAllQualificationList';
    this._ajax(url, 'POST', 'json', true, {}, 8000, callback);
  },
  /**
   * 获得血型列表
   * @param callback
   */
  getAllBloodTypeList: function (callback) {
    var url = '/clbs/talkback/monitoring/dispatch/getAllBloodTypeList';
    this._ajax(url, 'POST', 'json', true, {}, 8000, callback);
  },
  /**
   * 加入任务组
   */
  insertTaskAssignmentAndMember: function (data, callback) {
    var url = '/clbs/talkback/monitoring/dispatch/insertTaskAssignmentAndMember';
    this._ajax(url, 'POST', 'json', true, data, 8000, callback);
  },
  /**
   * 解散任务组
   */
  unbindAssignmentAndMonitor: function (data, callback) {
    var url = '/clbs/talkback/monitoring/dispatch/unbindAssignmentAndMonitor';
    this._ajax(url, 'POST', 'json', true, data, 8000, callback);
  },
  /**
   * 踢出任务组内对讲对象
   * assignmentId 分组id
   * interlocutorId  对讲对象id
   */
  removeTaskAssignmentInterlocutor: function (data, callback) {
    var url = '/clbs/talkback/monitoring/dispatch/removeTaskAssignmentInterlocutor';
    this._ajax(url, 'POST', 'json', true, data, 8000, callback);
  },
  /**
   * 查找对讲对象,通过固定条件
   */
  findInterlocutorByFixedCondition: function (data, callback) {
    var url = '/clbs/talkback/monitoring/dispatch/findInterlocutorByFixedCondition';
    this._ajax(url, 'POST', 'json', true, data, 8000, callback);
  },
  /**
   * 创建临时组
   */
  addTemporaryAssignment: function (data, callback) {
    var url = '/clbs/talkback/monitoring/dispatch/addTemporaryAssignment';
    this._ajax(url, 'POST', 'json', true, data, 8000, callback);
  },
  /**
   * 加入临时组 记录日志
   */
  insertTemporaryAssignmentRecordLog: function (data, callback) {
    var url = '/clbs/talkback/monitoring/dispatch/insertTemporaryAssignmentRecordLog';
    this._ajax(url, 'POST', 'json', true, data, 8000, callback);
  },
  /**
   * 踢出临时组内对讲对象记录日志
   * @param data
   * @param callback
   */
  removeTemporaryAssignmentInterlocutorRecordLog: function (data, callback) {
    var url = '/clbs/talkback/monitoring/dispatch/removeTemporaryAssignmentInterlocutorRecordLog';
    this._ajax(url, 'POST', 'json', true, data, 8000, callback);
  },
  /**
   * 调度监控对象弹框信息查询
   */
  getMonitorBounced: function (data, callback) {
    var url = '/clbs/talkback/monitoring/dispatch/monitorBounced';
    this._ajax(url, 'POST', 'json', false, data, 8000, callback);
  },
  /**
   * 调度群组弹框信息查询
   */
  getAssignmentBounced: function (data, callback) {
    var url = '/clbs/talkback/monitoring/dispatch/assignmentBounced';
    this._ajax(url, 'POST', 'json', true, data, 8000, callback);
  },
  /**
   * 调度通知记录的最近通知
   */
  notificationRecordList: function (data, callback) {
    var url = '/clbs/talkback/monitoring/dispatch/notificationRecordList';
    this._ajax(url, 'POST', 'json', true, data, 8000, callback);
  },
  /**
   * 保存调度通知记录
   */
  addNotificationRecord: function (data, callback) {
    var url = '/clbs/talkback/monitoring/dispatch/addNotificationRecord';
    this._ajax(url, 'POST', 'json', true, data, 8000, callback);
  },
  /**
   * 查询对讲组内用户
   */
  searchGroupOfList: function (data, callback, async) {
    var url = '/clbs/talkback/monitoring/dispatch/getInterlocutorAssignmentMember';
    this._ajax(url, 'POST', 'json', async, data, 8000, callback);
  },
  /**
   * 报警处理
   */
  alarmHandle: function (data, callback) {
    var url = '/clbs/talkback/monitoring/dispatch/handleAlarm';
    this._ajax(url, 'POST', 'json', true, data, 8000, callback);
  },
  /**
   * 添加日志
   */
  addLog: function (data) {
    var url = '/clbs/talkback/monitoring/dispatch/addLog';
    this._ajax(url, 'POST', 'json', true, data, 8000, null);
  },
  /**
   * 判断分组是否能加入对讲对象
   */
  judgeAssignmentIfJoinMonitor: function (data, callback) {
    var url = '/clbs/talkback/monitoring/dispatch/judgeAssignmentIfJoinMonitor';
    this._ajax(url, 'POST', 'json', true, data, 8000, callback);
  },
  /**
   * 查询对讲用户信息
   */
  getInterlocutorInfoById: function (data, callback) {
    var url = '/clbs/talkback/monitoring/dispatch/getInterlocutorInfoById';
    this._ajax(url, 'POST', 'json', true, data, 8000, callback);
  },
  /**
   * 获取围栏数据
   */
  getFenceData: function (callback) {
    var url = '/clbs/talkback/monitoring/dispatch/getUserFenceInfo';
    this._ajax(url, 'POST', 'json', true, {}, 8000, callback);
  },
  /**
   * ajax服务请求
   * @param url
   * @param type
   * @param dataType
   * @param async
   * @param timeout
   * @param callback
   * @private
   */
  _ajax: function (
    url,
    type,
    dataType,
    async,
    data,
    timeout,
    callback
  ) {
    var $this = this;
    $.ajax(
      {
        url: url,
        type: type,
        dataType: dataType,
        async: async,
        data: data,
        timeout: timeout,
        beforeSend: $this._beforeSend,
        success: callback,
        complete: $this._complete
      });
  },
  /**
   * ajax请求前事件
   * @private
   */
  _beforeSend: function () {

  },
  /**
   * ajax请求完成事件
   * @private
   */
  _complete: function () {

  }
};