(function (window, $) {

  var dispatch = {
    init: function () {
      var dispatchModule = new DispatchModule();

      dispatch.dispatchData = new DispatchData();
      dispatchModule.set('data', dispatch.dispatchData);

      dispatch.dispatchServices = new DispatchServices();
      dispatchModule.set('dispatchServices', dispatch.dispatchServices);

      dispatch.dispatch = new Dispatch({dispatchModule: dispatchModule});
      dispatchModule.set('dispatch', dispatch.dispatch);

      dispatch.dispatchWebServices = new DispatchWebServices({
        dispatchModule: dispatchModule
      });
      dispatchModule.set('dispatchWebServices', dispatch.dispatchWebServices);

      dispatch.dispatchAmap = new DispatchAmap({
        elementId: 'dispatchMap',
        dispatchModule: dispatchModule
      });
      dispatchModule.set('dispatchAmap', dispatch.dispatchAmap);

      dispatch.dispatchTaskGroup = new DispatchTaskGroup({dispatchModule: dispatchModule});
      dispatchModule.set('dispatchTaskGroup', dispatch.dispatchTaskGroup);

      dispatch.invisiblePersonTable = new InvisiblePersonTable({dispatchModule: dispatchModule});
      dispatchModule.set('invisiblePersonTable', dispatch.dispatchTaskGroup);

      dispatch.dispatchTable = new DispatchTable({dispatchModule: dispatchModule});
      dispatchModule.set('dispatchTable', dispatch.dispatchTable);

      dispatch.dispatchTree = new DispatchTree({
        elementId: 'dispatchTree',
        dispatchModule: dispatchModule
      });
      dispatchModule.set('dispatchTree', dispatch.dispatchTree);

      dispatch.bindHandler();
    },
    /**
     * 给页面部分标签设置固定值
     */
    initPage: function () {
      var wHeight = $(window).height();
      $('#leftTree').css('height', (wHeight - 80) + 'px');
      $('#rightContentMain').css('height', (wHeight - 80) + 'px');
    },
    /**
     * 数据事件绑定
     */
    bindHandler: function () {
      // 任务组创建地图圆形数据弹窗
      dispatch.dispatchData.on('taskGroupDrawCircle', dispatch.dispatchTaskGroup.showTaskGroupCircle.bind(dispatch.dispatchTaskGroup));
      // 任务组创建地图矩形数据弹窗
      dispatch.dispatchData.on('taskGroupDrawRectangular', dispatch.dispatchTaskGroup.showTaskGroupRectangular.bind(dispatch.dispatchTaskGroup));
      // 任务组创建数据添加
      dispatch.dispatchData.on('taskGroupDataAdd', dispatch.dispatchTree.taskGroupDataAddHandler.bind(dispatch.dispatchTree));
      // 调度视图显示与隐藏
      dispatch.dispatchData.on('dispacthViewState', dispatch.dispatch.dispacthViewStateHandler.bind(dispatch.dispatch));
    }
  };

  $(function () {
    dispatch.init();
    dispatch.initPage();
  });

})(window, $);