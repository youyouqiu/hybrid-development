//@ sourceURL=taskEdit.js
(function (window, $) {
  // 任务项
  var scheduleIndex = $('#schedule-list .item').length; // 任务项个数
  var scheduleMax = 20; // 最多增加任务个数
  var addIndex = scheduleIndex - 1; // 任务项索引

  // 时间
  var startTimeInstance = []; // 开始时间实例集合
  var endTimeInstance = []; // 结束时间实例集合

  var taskEdit = {
    init: function () {
      $('input').inputClear();

      // 判断任务项个数
      if (scheduleIndex === 1) {
        $('#schedule-list').addClass('del-hide');
      }

      // 循环遍历初始化任务项列表
      for (var i = 0; i < scheduleIndex; i++) {
        // 围栏组织树
        taskEdit.fenceTreeInit('#treeTypeDemo' + i);

        // 时间初始化
        startTimeInstance[i] = taskEdit.renderTime('startTime' + i, taskEdit.startTimeCB);
        endTimeInstance[i] = taskEdit.renderTime('endTime' + i, taskEdit.endTimeCB);
      }
      $('.fenceGroupSelect').on('click', showMenuContent);
    },
    //时间
    renderTime: function (id, doneCB) {
      var timeInstance = laydate.render({
        elem: '#' + id
        , type: 'time'
        , theme: '#6dcff6'
        , done: doneCB
        , format: 'HH:mm'
        , btns: ['clear', 'confirm']
      });
      return timeInstance;
    },
    endTimeCB: function (value, date) {
      var elem = $(this)[0].elem.selector;
      var index = elem.substr(elem.length - 1);

      //清空判断
      if (!value) {
        var max = taskEdit.clearLimit('max');
        startTimeInstance[index].config.max = max;
        return;
      }

      date.month = date.month - 1;
      startTimeInstance[index].config.max = date;
    },
    startTimeCB: function (value, date) {
      var elem = $(this)[0].elem.selector;
      var index = elem.substr(elem.length - 1);

      //清空判断
      if (!value) {
        var min = taskEdit.clearLimit('min');
        endTimeInstance[index].config.min = min;
        return;
      }

      date.month = date.month - 1;
      endTimeInstance[index].config.min = date;
    },
    clearLimit: function (type) {
      var date = new Date();
      var max = {
        date: date.getDate(),
        hours: type == 'max' ? 23 : 0,
        minutes: type == 'max' ? 59 : 0,
        month: date.getMonth(),
        seconds: type == 'max' ? 59 : 0,
        year: date.getFullYear()
      };
      return max;
    },
    //围栏下拉组织树
    fenceTreeInit: function (id) {
      var fenceAll = {
        async: {
          url: '/clbs/m/regionManagement/fenceManagement/getFenceTree',
          type: 'post',
          enable: true,
          autoParam: ['id'],
          dataType: 'json',
          otherParam: {'type': 'multiple'},
          dataFilter: taskEdit.FenceAjaxDataFilter
        },
        check: {
          enable: true,
          chkStyle: 'radio',
          radioType: 'all',
          chkboxType: {
            'Y': 's',
            'N': 's'
          }
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
          onAsyncSuccess: taskEdit.onAsyncSuccessFence,
          onClick: taskEdit.onClickFence,
          onCheck: taskEdit.onCheckFence
        }
      };
      $.fn.zTree.init($(id), fenceAll, null);
    },
    FenceAjaxDataFilter: function (treeId, parentNode, responseData) {
      var ret = [],
        data = [];
      var id = $('#' + treeId).data('value');
      var inx = treeId.substr(treeId.length - 1);

      if (responseData && responseData.msg) {
        ret = JSON.parse(responseData.msg);
      }
      for (var i = 0; i < ret.length; i++) {
        var item = ret[i];
        if (item.fenceType != 'zw_m_marker') {
          if (item.pId === '') {
            item.pId = 'top';
          }

          if (item.type == 'fenceParent') {
            item.nocheck = true;
          }

          if (id == item.id) {
            item.checked = true;
            $('#groupSelect' + inx).val(item.name);
          }
          data.push(item);
        }
      }

      return data;
    },
    onAsyncSuccessFence: function (event, treeId, treeNode) {
      var treeObj = $.fn.zTree.getZTreeObj(treeId);
      treeObj.expandAll(true);
    },
    onClickFence: function (event, treeId, treeNode) {
      var treeObj = $.fn.zTree.getZTreeObj(treeId);
      treeObj.checkNode(treeNode, true, true);
      taskEdit.onCheckFence(event, treeId, treeNode);
    },
    onCheckFence: function (event, treeId, treeNode) {
      var treeTypeDemoArr = treeId.split('treeTypeDemo');
      var inx = treeTypeDemoArr[1];
      var name = treeNode.name,
        id = treeNode.id;
      var groupSelect = $('#groupSelect' + inx);

      if (treeNode.type != 'fenceParent' && name && id) {
        groupSelect.val(name);
        groupSelect.attr('data-id', id);
      }
    },
    // 添加任务
    addSchedule: function () {
      if (scheduleIndex >= scheduleMax) {
        tg_alertError('信息', '系统最多支持' + scheduleMax + '个任务项');
        return;
      }

      var html = taskEdit.scheduleDom();
      $('.schedule-list').append(html);
      $('#schedule-list').removeClass('del-hide');
      scheduleIndex += 1;

      //日期
      startTimeInstance[addIndex] = taskEdit.renderTime('startTime' + addIndex, taskEdit.startTimeCB);
      endTimeInstance[addIndex] = taskEdit.renderTime('endTime' + addIndex, taskEdit.endTimeCB);

      //围栏组织树
      taskEdit.fenceTreeInit('#treeTypeDemo' + addIndex);
      $('#groupSelect' + addIndex).on('click', showMenuContent);
    },
    scheduleDom: function () {
      addIndex += 1;
      var html = '';

      html += '<li class="item">' +
        '                        <div class="zw-title clearfix">' +
        '                            <label class="control-label pull-left"><label' +
        '                                    class="text-danger">*</label> 任务项 </label>' +
        '                            <button type="button"' +
        '                                    class="btn btn-primary pull-right add-btn">' +
        '                                <span class="glyphicon glyphicon-plus" aria-hidden="true"></span>' +
        '                            </button>' +
        '                            <button type="button" class="btn btn-danger pull-right del-btn">' +
        '                                <span class="glyphicon glyphicon-trash" aria-hidden="true"></span>' +
        '                            </button>' +
        '                        </div>' +
        '                        <div class="form-group">' +
        '                            <label class="col-md-2 control-label"><label' +
        '                                    class="text-danger">*</label> 控制类型： </label>' +
        '                            <div class="col-md-10">' +
        '                                <label class="radio-inline"><input class="controlType" value="1" type="radio" name="controlType' + addIndex + '" checked>围栏</label>' +
        '                                <label class="radio-inline"><input class="controlType" value="2" type="radio" name="controlType' + addIndex + '" disabled>RFID</label>' +
        '                                <label class="radio-inline"><input class="controlType" value="3" type="radio" name="controlType' + addIndex + '" disabled>NFC</label>' +
        '                                <label class="radio-inline"><input class="controlType" value="4" type="radio" name="controlType' + addIndex + '" disabled>二维码</label>' +
        '                            </div>' +
        '                        </div>' +
        '                        <div class="form-group">' +
        '                            <label class="col-md-2 control-label"><label' +
        '                                    class="text-danger">*</label> 围栏： </label>' +
        '                            <div class="col-md-4 has-feedback">' +
        '                                <!--组织树复选框-->' +
        '                                <input style="cursor: pointer; background-color: #fafafa;"' +
        '                                       placeholder="请选择任务围栏" autocomplete="off" class="form-control"' +
        '                                       id="groupSelect' + addIndex + '" name="fenceInfoId" readonly/>' +
        '                                <span class="fa fa-chevron-down form-control-feedback"' +
        '                                      style="top: 0; right: 15px;cursor:pointer;" aria-hidden="true"' +
        '                                      id="groupSelectSpan' + addIndex + '"></span>' +
        '                                <div id="menuContent' + addIndex + '" class="menuContent">' +
        '                                    <ul id="treeTypeDemo' + addIndex + '" class="ztree"></ul>' +
        '                                </div>' +
        '                            </div>' +
        '                        </div>' +
        '                        <div class="form-group">' +
        '                            <label class="col-md-2 control-label"><label' +
        '                                    class="text-danger">*</label> 开始时间： </label>' +
        '                            <div class="col-md-4">' +
        '                                <input id="startTime' + addIndex + '" readonly="readonly" name="startDate" placeholder="请选择任务开始时间" type="text"' +
        '                                       class="form-control layer-date laydate-icon startTime" autocomplete="off" style="cursor: pointer; background-color: rgb(250, 250, 250);" />' +
        '                            </div>' +
        '                            <label class="col-md-2 control-label"><label' +
        '                                    class="text-danger">*</label> 结束时间： </label>' +
        '                            <div class="col-md-4">' +
        '                                <input id="endTime' + addIndex + '" readonly="readonly" name="endDate" placeholder="请选择任务结束时间" type="text"' +
        '                                       class="form-control layer-date laydate-icon endTime" autocomplete="off" style="cursor: pointer; background-color: rgb(250, 250, 250);" />' +
        '                            </div>' +
        '                        </div>' +
        '                        <div class="form-group">' +
        '                            <label class="col-md-2 control-label">关联报警： </label>' +
        '                            <div class="col-md-10">' +
        '                                <label class="checkbox-inline"><input class="relationAlarm" value="1" name="relationAlarm' + addIndex + '" type="checkbox" checked/> 任务未到岗</label>' +
        '                                <label class="checkbox-inline"><input class="relationAlarm" value="2" name="relationAlarm' + addIndex + '" type="checkbox" checked/> 任务离岗</label>' +
        '                            </div>' +
        '                        </div>' +
        '                    </li>';

      return html;
    },
    //删除任务
    delSchedule: function () {
      var self = $(this);
      tg_confirmDialog(null, null, function () {
        self.parents('.item').remove();
        scheduleIndex -= 1;
        if (scheduleIndex == 1) {
          $('#schedule-list').addClass('del-hide');
        }
      });
    },
    //表单验证
    validates: function () {
      return $('#editForm').validate({
        rules: {
          name: {
            required: true,
            isJobNameCanNull: true,
            remote: {
              url: '/clbs/a/taskManagement/checkTaskName',
              type: 'post',
              dataType: 'json',
              async: false,
              data: {
                id: function () {
                  return $('#taskId').val();
                }
              }
            }
          },
          fenceInfoId: {
            required: true
          },
          startDate: {
            required: true,
            repeatEndCheck: true
          },
          endDate: {
            required: true,
            compareDates: '.startTime'
          }
        },
        messages: {
          name: {
            required: nameNull,
            isJobNameCanNull: nameError,
            remote: nameExists
          },
          fenceInfoId: {
            required: fenceInfoIdNull
          },
          startDate: {
            required: startDateNull
          },
          endDate: {
            required: endDateNull
          }
        }
      }).form();
    },
    //表单提交
    onSubmit: function () {
      if (!taskEdit.validates()) {
        return false;
      }
      var taskItems = [];

      //任务项
      for (var i = 0; i < scheduleIndex; i++) {
        var obj = {};
        var item = $('#schedule-list .item').eq(i);
        var relationAlarm = [];
        //关联报警
        $.each(item.find('.relationAlarm:checked'), function () {
          relationAlarm.push($(this).val());
        });

        obj = {
          // "taskId": item.find('.itemId').val(),
          'taskId': $('#taskId').val(),
          'controlType': parseInt(item.find('.controlType:checked').val(), 10),
          'fenceInfoId': item.find('input[name="fenceInfoId"]').data('id'),
          'startTime': item.find('.startTime').val(),
          'endTime': item.find('.endTime').val(),
          'relationAlarm': relationAlarm.join(',')
        };
        taskItems.push(obj);
      }

      //数据组装
      var paramer = {
        'id': $('#taskId').val(),
        'taskName': $('#taskName').val(),
        'remark': $('#remarks').val(),
        'taskItemsStr': JSON.stringify(taskItems)
      };

      //任务冲突验证
      json_ajax('POST', '/clbs/a/taskManagement/editTask', 'json', true, paramer, taskEdit.submitCallBack);
    },
    submitCallBack: function (data) {
      if (data.success) {
        layer.msg('修改成功！', {
          move: false
        });
        $('#commonLgWin').modal('hide');
        myTable.requestData();
      } else {
        layer.msg(data.msg);
      }
    }
  };

  $(function () {
    taskEdit.init();
    $('#schedule-list').on('click', '.add-btn', taskEdit.addSchedule);
    $('#schedule-list').on('click', '.del-btn', taskEdit.delSchedule);
    $('#doSubmits').on('click', taskEdit.onSubmit);
  });
})(window, $);