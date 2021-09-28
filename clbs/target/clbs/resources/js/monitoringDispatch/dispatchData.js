var DispatchData = function () {
  this._eventHandlerList = {};
  this._taskGroupDrawCircleData = null; // 任务组地图圆形选择地图数据
  this._taskGroupDrawRectangleData = null; // 任务组地图矩形选择地图数据
  this._fixedConditionsState = false; // 创建分组是否选择固定条件
  this._addType = null; // 1 新增任务组 2 加入任务组
  this._addAssignmentNode = null; // 加入分组的node
  this._schedulingViewState = false; // 调度视图显示与隐藏状态
  this._dispatchType = null; // 调度为（车、人、物）对象(1)还是群组(2)
  this._dispatchLoginData = null; // 调度服务数据

  this._taskGroupNode = null; // 任务组node
  this._temporaryGroupNode = null; // 临时组node
  this._groupNodesList = null; // 对讲对象组织树组织node集合
  this._assignmentNodesList = null; // 对讲对象组织树分组node集合
  this._monitorNodesList = null; // 对讲对象组织树对象node集合
  this._oldMonitorNodesList = null; // 对讲对象组织树对象原本位置集合

  this._dispatchNode = null; // 调度选择的组织树node节点
  this._tempGroupNodes = {}; // 临时组节点数据集合
};

DispatchData.prototype = {
  on: function (eventName, eventHandler) {
    this._eventHandlerList[eventName] = eventHandler;
  },
  off: function (eventName) {
    delete this._eventHandlerList[eventName];
  },

  runHandler: function (eventName) {
    var eventHandler = this._eventHandlerList[eventName];
    if (typeof eventHandler === 'function') {
      eventHandler.apply();
    }
  },

  setTaskGroupDrawCircleData: function (data) {
    this._taskGroupDrawCircleData = data;
    this.runHandler('taskGroupDrawCircle');
  },
  getTaskGroupDrawCircleData: function () {
    return this._taskGroupDrawCircleData;
  },

  setTaskGroupDrawRectangleData: function (data) {
    this._taskGroupDrawRectangleData = data;
    this.runHandler('taskGroupDrawRectangular');
  },
  getTaskGroupDrawRectangleData: function () {
    return this._taskGroupDrawRectangleData;
  },

  setFixedConditionsState: function (data) {
    this._fixedConditionsState = data;
  },
  getFixedConditionsState: function () {
    return this._fixedConditionsState;
  },

  setAddType: function (data) {
    this._addType = data;
  },
  getAddType: function () {
    return this._addType;
  },

  setSchedulingViewState: function (data) {
    this._schedulingViewState = data;
    this.runHandler('dispacthViewState');
  },
  getSchedulingViewState: function () {
    return this._schedulingViewState;
  },

  setDispatchType: function (data) {
    this._dispatchType = data;
  },
  getDispatchType: function (value) {
    return this._dispatchType;
  },

  setDispatchLoginData: function (data) {
    this._dispatchLoginData = data;
  },
  getDispatchLoginData: function () {
    return this._dispatchLoginData;
  },

  setAddAssignmentNode: function (data) {
    this._addAssignmentNode = data;
  },
  getAddAssignmentNode: function () {
    return this._addAssignmentNode;
  },

  setTaskGroupNode: function (data) {
    this._taskGroupNode = data;
  },
  getTaskGroupNode: function () {
    return this._taskGroupNode;
  },

  setTemporaryGroupNode: function (data) {
    this._temporaryGroupNode = data;
  },
  getTemporaryGroupNode: function () {
    return this._temporaryGroupNode;
  },

  setGroupNodesList: function (nodes) {
    var $this = this;
    if ($this._groupNodesList === null) {
      $this._groupNodesList = nodes;
    } else {
      $this._groupNodesList[nodes.id] = nodes;
    }
  },
  getGroupNodesList: function () {
    return this._groupNodesList;
  },
  removeGroupNode: function (node) {
    delete this._groupNodesList[node.id];
  },
  editGroupNode: function (node) {
    this._groupNodesList[node.id] = node;
  },
  clearGroupNode: function () {
    this._groupNodesList = null;
  },

  setAssignmentNodesList: function (nodes) {
    var $this = this;
    if ($this._assignmentNodesList === null) {
      $this._assignmentNodesList = nodes;
    } else {
      $this._assignmentNodesList[nodes.intercomGroupId] = nodes;
    }
  },
  getAssignmentNodesList: function () {
    return this._assignmentNodesList;
  },
  getAssignmentNodes: function (intercomGroupId) {
    return this._assignmentNodesList === null ? null : this._assignmentNodesList[intercomGroupId];
  },
  removeAssignmentNode: function (intercomGroupId) {
    delete this._assignmentNodesList[intercomGroupId];
  },
  editAssignmentNode: function (node) {
    this._assignmentNodesList[node.intercomGroupId] = node;
  },
  clearAssignmentNode: function () {
    this._assignmentNodesList = null;
  },

  setMonitorNodesList: function (nodes) {
    var $this = this;
    if ($this._monitorNodesList === null) {
      this._monitorNodesList = nodes;
      this._oldMonitorNodesList = nodes;
    } else {
      if (Array.isArray(nodes)) {
        nodes.map(function (item) {
          if ($this._monitorNodesList[item.interlocutorId] === undefined) {
            $this._oldMonitorNodesList[item.interlocutorId] = item;
          }
          $this._monitorNodesList[item.interlocutorId] = item;
        });
      } else {
        if ($this._monitorNodesList[nodes.interlocutorId] === undefined) {
          $this._oldMonitorNodesList[nodes.interlocutorId] = nodes;
        }
        $this._monitorNodesList[nodes.interlocutorId] = nodes;
      }
    }
  },
  getMonitorNode: function (interlocutorId) {
    return this._monitorNodesList === null ? null : this._monitorNodesList[interlocutorId];
  },
  removeMonitorNode: function (interlocutorId) {
    delete this._monitorNodesList[interlocutorId];
  },
  editMonitorNode: function (node) {
    this._monitorNodesList[node.interlocutorId] = node;
  },
  clearMonitorNode: function () {
    this._monitorNodesList = null;
  },
  
  setDispatchNode: function (node) {
    this._dispatchNode = node;
  },
  getDispatchNode: function () {
    return this._dispatchNode;
  },
  setTempGroupNodes: function (node) {
    this._tempGroupNodes[node.intercomGroupId] = node;
  },
  getTempGroupNodes: function () {
    return this._tempGroupNodes;
  },
  removeTempGroupNodes: function (intercomGroupId) {
    delete this._tempGroupNodes[intercomGroupId];
  }
};