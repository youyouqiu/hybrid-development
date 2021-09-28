var scheduledMileageReport;
var myTable, myTable2, myTable3;

;(function (window, $) {
  var peopleMap = null,// 选择人员组织树数据集合
      monitorIds = [],// 选择人员id
    peopleTreeName = [],
      monitorId = '';// 具体明细peopleId
  var tableInx = 0;// table索引

  //排班项
  var isStart = true;//排班是否开始
  var scheduleStartDate = '',
    scheduleEndDate = '';

  // 图表数据集合
  var myChart,
    option;

  var peopleNames = [],
    peopleAttendance = [];
  var chartInx = 0,
    recordsLength = 0;

  //时间
  var startTime = '',
    endTime = '';
  var dateRangePicker;//时间控件实例

  scheduledMileageReport = {
    init: function () {
      $('input').inputClear();
      scheduledMileageReport.getSchedule();
    },
    //排班下拉
    getSchedule: function () {
      json_ajax('post', '/clbs/talkback/reportManagement/scheduledAttendanceReport/getScheduledList',
        'json', true, {}, scheduledMileageReport.scheduleCB);
    },
    scheduleCB: function (data) {
      if (data.success) {
        var html = '';
        var obj = data.obj;
        var initId = '';
        for (var i = 0; i < obj.length; i++) {
          var item = obj[i];

          if (item.status === 1) {
            if(!initId){
              initId = item.id;
            }
            html += '<option data-status="' + item.status + '" data-startdate="' + item.startDate.substr(0, 10) + '" data-enddate="' + item.endDate.substr(0, 10) + '"' +
              ' value="' + item.id + '" >' + item.scheduledName + '</option>';
          }
        }
        $('#schedule').html(html);

        if (obj.length > 0) {
          scheduledMileageReport.getPeople(initId ||obj[0].id);
        }
        scheduledMileageReport.timerInit();
      } else {
        layer.msg(data.msg);
      }
    },
    timerInit: function () {
      var timeInterval = $('#timeInterval');
      var selected = $('#schedule option:selected');
      var startDate = selected.data('startdate'),
        endDate = selected.data('enddate');

      if (startDate && endDate) {
        dateRangePicker = timeInterval.dateRangePicker({
          'isTimeSelected': false,
          'dateLimit': 31,
          'isShowHMS': false,
          'start_date': startDate,
          'end_date': startDate,
          'isOffLineReportFlag': true,
          'maxSelectedDate': endDate >= getYesterDay() ? getYesterDay() : endDate,
          'minSelectedDate': startDate
        });
      }
    },
    //排班change事件
    selectFun: function () {
      var self = $(this);
      var timeInterval = $('#timeInterval');
      var option = self.find('option:selected');
      var value = self.val(),
        startDate = option.data('startdate'),
        endDate = option.data('enddate');

      //重置时间
      var maxDate = endDate >= getYesterDay() ? getYesterDay() : endDate;
      dateRangePicker.setMaxSelectedDate(startDate, endDate, maxDate);
      dateRangePicker.setMinSelectedDate(startDate, endDate, startDate);
      var scheduleDate = startDate + '--' + startDate;
      timeInterval.val(scheduleDate);

      //重置人员
      monitorIds = [];
      peopleTreeName = [];
      $('#groupSelect').val('');
      scheduledMileageReport.getPeople(value);
    },
    //获取人员
    getPeople: function (monitorId) {
      var paramer = {
        'id': monitorId
      };
      json_ajax('post', '/clbs/talkback/reportManagement/scheduledAttendanceReport/findMonitoringObject',
        'json', true, paramer, scheduledMileageReport.getPeopleCB);
    },
    getPeopleCB: function (data) {
      if (data.success && data.obj) {
        peopleMap = data.obj;
        scheduledMileageReport.setPeopleTree();
      } else {
        layer.msg(data.msg);
      }
    },
    setPeopleTree: function () {
      var ztreeData = [];
      var isIncumbency = $('#isIncumbency').is(':checked');

      // 数据组装
      for (var i = 0; i < peopleMap.length; i++) {
        var item = peopleMap[i];
        var newObj = {
          'name': item.monitorName,
          'id': item.monitorId,
          'isIncumbency': item.isIncumbency
        };

        ztreeData.push(newObj);
      }

      // parentNode数据组装
      var zTreeNodes = [{
        'name': '所有人员',
        id: -1,
        open: true,
        children: ztreeData
      }];

      var setting = {
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
          countClass: 'group-number-statistics',
          showIcon: false
        },
        data: {
          simpleData: {
            enable: true
          }
        },
        callback: {
          onCheck: scheduledMileageReport.onCheckPeople,
          beforeClick: scheduledMileageReport.beforeClickPeople
        }
      };
      $.fn.zTree.init($('#treeDemo'), setting, zTreeNodes);
    },
    beforeClickPeople: function (treeId, treeNode) {
      var zTree = $.fn.zTree.getZTreeObj(treeId);
      zTree.checkNode(treeNode, !treeNode.checked, true, true);
      return false;
    },
    onCheckPeople: function (event, treeId, treeNode) {
      var treeObj = $.fn.zTree.getZTreeObj(treeId);
      var checkNodes = treeObj.getCheckedNodes(true);
      peopleTreeName = [];
      monitorIds = [];

      if (treeNode.id === -1 && treeNode.checked) {
        treeObj.expandNode(treeNode, true, true, true, true); // 展开节点
      }

      for (var i = 0; i < checkNodes.length; i++) {
        var item = checkNodes[i];
        if (item.id !== -1) {
          monitorIds.push(item.id);
          peopleTreeName.push(item.name);
        }
      }

      // if(treeNode.id == -1 &&　treeNode.checked && treeNode.children.length>0){
      //     peopleTreeName = [];
      //     peopleTreeName.push('所有人员');
      // }

      $('#groupSelect').val(peopleTreeName.join(','));
    },
    // 查询
    validates: function () {
      return $('#searchForm').validate({
        rules: {
          schedule: {
            required: true
          },
          groupSelect: {
            required: true
          },
          timeInterval: {
            required: true
          }
        },
        messages: {
          schedule: {
            required: inputEmpty
          },
          groupSelect: {
            required: vehicleSelectBrand
          },
          timeInterval: {
            required: inputEmpty
          }
        }
      }).form();
    },
    inquireClick: function () {
      if (!scheduledMileageReport.validates()) return;
      if (!isStart) {
        layer.msg('该排班还未开始');
        return;
      }

      var date = $('#timeInterval').val().split('--');
      startTime = date[0];
      endTime = date[1];

      if ($('#stretch2-body').is(':hidden')) {
        $('#stretch2').click();
      }
      //tab重置
      scheduledMileageReport.setTab(0);

      var paramer = {
        'id': $('#schedule').val(),
        'monitorIds': monitorIds.join(','),
        'startTime': startTime,
        'endTime': endTime
      };
      json_ajax('post', '/clbs/talkback/reportManagement/scheduledMileageReport/list',
        'json', true, paramer, scheduledMileageReport.inquireClickCB);
    },
    inquireClickCB: function (data) {
      if (data.success) {
        scheduledMileageReport.initTable();
        scheduledMileageReport.initTable2();
        scheduledMileageReport.getChartsData();
      }
    },
    getChartsData: function () {
      json_ajax('post', '/clbs/talkback/reportManagement/scheduledMileageReport/getAllSummary',
        'json', true, {}, function (data) {
          var records = data.records;
          peopleNames = [];
          peopleAttendance = [];
          var yArr = [];
          var sum = 0;

          recordsLength = records.length;

          // scheduledMileageReport.sortRecords(records);
          //图形图标数据组装
          for (var i = 0; i < records.length; i++) {
            var item = records[i];
            var dayEffectiveMileage = Number(item.dayEffectiveMileage);
            var obj = {
              'value': dayEffectiveMileage,
              'monitorId': item.monitorId
            };
            peopleNames.push(item.monitorName);
            peopleAttendance.push(obj);
            yArr.push(dayEffectiveMileage);
            sum += dayEffectiveMileage;
          }
          scheduledMileageReport.setCharts();

          //图形统计
          if (yArr.length > 0) {
            var max = Math.max.apply(null, yArr).toFixed(1),
              min = Math.min.apply(null, yArr).toFixed(1),
              avg = (sum / yArr.length).toFixed(1);
            $('#max').text(parseFloat(max) + 'km');
            $('#min').text(parseFloat(min) + 'km');
            $('#average').text(parseFloat(avg) + 'km');
          }
        });
    },
    //报表
    initTable: function () {
      var columnDefs = [{
        //第一列，用来显示序号333
        'searchable': false,
        'orderable': false,
        'targets': 0
      }];
      var columns = [
        {
          //第一列，用来显示序号
          'data': null,
          'class': 'text-center'
        },
        {
          'data': null,
          'class': 'text-center',
          render: function (data, type, row, meta) {
            var result = '';

            result += '<button onclick="scheduledMileageReport.detailBtn(\'' + row.monitorId + '\', this)" type="button" class=" editBtn editBtn-info"><i class="fa fa-sun-o"></i>里程明细</button>&nbsp;';
            tableInx += 1;
            return result;
          }
        },
        {
          'data': 'monitorName',
          'class': 'text-center'
        },
        {
          'data': 'groupName',
          'class': 'text-center'
        }, {
          'data': 'assignmentName',
          'class': 'text-center'
        },
        {
          'data': 'workDays',
          'class': 'text-center'
        }, {
          'data': 'actualWorkDays',
          'class': 'text-center'
        }, {
          'data': 'dayEffectiveMileage',
          'class': 'text-center',
          render: function (data, type, row, meta) {
            if (data) {
              return Number(data).toFixed(1);
            }
          }
        }, {
          'data': 'averageMileage',
          'class': 'text-center',
          render: function (data, type, row, meta) {
            if (data) {
              return Number(data).toFixed(1);
            }
          }
        }
      ];

      //表格setting
      var setting = {
        listUrl: '/clbs/talkback/reportManagement/scheduledMileageReport/getSummary',
        columnDefs: columnDefs, //表格列定义
        columns: columns, //表格列
        dataTableDiv: 'dataTable1', //表格
        pageable: true, //是否分页
        showIndexColumn: true, //是否显示第一列的索引列
        drawCallbackFun: function () {
          var api = myTable.dataTable;
          var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
          api.column(0).nodes().each(function (cell, i) {
            cell.innerHTML = startIndex + i + 1;
          });
        },
        enabledChange: true,
        ajaxCallBack: function (data) {
          var records = data.records;

          //初始化具体明细
          if (records && records.length > 0) {
            $('#alarmExport0').removeAttr('disabled');
            monitorId = records[0].monitorId;
            scheduledMileageReport.initTable3();
          }
        }
      };
      myTable = new TG_Tabel.createNew(setting);
      myTable.init();
    },
    //降序排序
    sortRecords: function (data) {
      data.sort(function (a, b) {
        if (Number(a.dayEffectiveMileage) < Number(b.dayEffectiveMileage)) {
          return 1;
        } else if (Number(a.dayEffectiveMileage) > Number(b.dayEffectiveMileage)) {
          return -1;
        } else {
          return 0;
        }
      });
    },
    initTable2: function () {
      var columnDefs = [{
        //第一列，用来显示序号333
        'searchable': false,
        'orderable': false,
        'targets': 0
      }];
      var columns = [
        {
          //第一列，用来显示序号
          'data': null,
          'class': 'text-center'
        },
        {
          'data': 'monitorName',
          'class': 'text-center'
        },
        {
          'data': 'groupName',
          'class': 'text-center'
        }, {
          'data': 'assignmentName',
          'class': 'text-center'
        },
        {
          'data': 'dayStr',
          'class': 'text-center'
        }, {
          'data': 'actualWorkingPeriod',
          'class': 'text-center',
          render: function (data, type, row, meta) {
            if (data) {
              return data.substr(0, data.length - 1);
            }
          }
        }, {
          'data': 'dayEffectiveMileage',
          'class': 'text-center',
          render: function (data, type, row, meta) {
            if (data) {
              return Number(data).toFixed(1);
            }
          }
        }
      ];
      //表格setting
      var setting = {
        listUrl: '/clbs/talkback/reportManagement/scheduledMileageReport/getAll',
        columnDefs: columnDefs, //表格列定义
        columns: columns, //表格列
        dataTableDiv: 'dataTable2', //表格
        drawCallbackFun: function () {
          var api = myTable2.dataTable;
          var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
          api.column(0).nodes().each(function (cell, i) {
            cell.innerHTML = startIndex + i + 1;
          });
        },
        pageable: true, //是否分页
        showIndexColumn: true, //是否显示第一列的索引列
        enabledChange: true,
        ajaxCallBack: function (data) {
          var records = data.records;
          //初始化具体明细
          if (records && records.length > 0) {
            $('#alarmExport1').removeAttr('disabled');
          }
        }
      };
      myTable2 = new TG_Tabel.createNew(setting);
      myTable2.init();
    },
    initTable3: function () {
      var columnDefs = [{
        //第一列，用来显示序号333
        'searchable': false,
        'orderable': false,
        'targets': 0
      }];
      var columns = [
        {
          //第一列，用来显示序号
          'data': null,
          'class': 'text-center'
        },
        {
          'data': 'monitorName',
          'class': 'text-center'
        },
        {
          'data': 'groupName',
          'class': 'text-center'
        }, {
          'data': 'assignmentName',
          'class': 'text-center'
        },
        {
          'data': 'dayStr',
          'class': 'text-center'
        }, {
          'data': 'actualWorkingPeriod',
          'class': 'text-center',
          render: function (data, type, row, meta) {
            if (data) {
              return data.substr(0, data.length - 1);
            }
          }
        }, {
          'data': 'dayEffectiveMileage',
          'class': 'text-center',
          render: function (data, type, row, meta) {
            if (data) {
              return Number(data).toFixed(1);
            }
          }
        }
      ];
      //ajax参数
      var ajaxDataParamFun = function (d) {
        d.monitorId = monitorId;
      };
      //表格setting
      var setting = {
        listUrl: '/clbs/talkback/reportManagement/scheduledMileageReport/getDetail',
        columnDefs: columnDefs, //表格列定义
        columns: columns, //表格列
        dataTableDiv: 'dataTable3', //表格
        pageable: true, //是否分页
        showIndexColumn: true, //是否显示第一列的索引列
        ajaxDataParamFun: ajaxDataParamFun, //ajax参数
        enabledChange: true,
        ajaxCallBack: function (data) {
          var records = data.records;
          //初始化具体明细
          if (records && records.length > 0) {
            $('#alarmExport2').removeAttr('disabled');
          }
        }
      };
      myTable3 = new TG_Tabel.createNew(setting);
      myTable3.init();
    },
    //具体明细
    detailBtn: function (id, self) {
      var curInx = $(self).parents('tr').find('td').eq(0).text() - 1;

      scheduledMileageReport.attendanceDetail(id, curInx);
      //图表缩放设置
      var start = curInx,
        end = curInx + 19;
      if (curInx > (recordsLength - 20)) {
        start = recordsLength - 20;
        end = recordsLength - 1;
      }

      myChart.dispatchAction({
        type: 'dataZoom',
        startValue: start,
        endValue: end
      });
    },
    //具体明细
    attendanceDetail: function (id, inx) {
      monitorId = id;
      scheduledMileageReport.initTable3();

      //图表切换
      chartInx = inx;
      var options = {
        series: [{
          itemStyle: {
            normal: {
              color: function (param) {
                if (chartInx === param.dataIndex) {
                  return '#428bca';
                } else {
                  return '#6dcff6';
                }
              }
            }
          }
        }]
      };
      myChart.setOption(options);

      //tab切换
      scheduledMileageReport.setTab(2);
    },
    //图形展示
    setCharts: function () {
      option = {
        color: ['#6dcff6'],
        legend: {
          data: ['里程'],
          align: 'left',
          left: 10
        },
        grid: {
          top: '15%',
          left: '10%',
          right: '10%',
          containLabel: true
        },
        xAxis: {
          name: '监控对象',
          type: 'category',
          axisLabel: {
            show: true,
            interval: 0,
            rotate: 45
          },
          data: peopleNames
        },
        yAxis: {
          name: '里程(km)',
          type: 'value',
          // max: 100,
          splitNumber: 10
        },
        dataZoom: [
          {
            type: 'inside',
            startValue: 0,
            endValue: 20
          },
          {
            show: true,
            start: 0,
            end: 20,
            top: 'top'
          }
        ],
        series: [{
          type: 'bar',
          name: '里程',
          barMaxWidth: '40%',
          itemStyle: {
            normal: {
              color: function (param) {
                if (chartInx === param.dataIndex) {
                  return '#428bca';
                } else {
                  return '#6dcff6';
                }
              }
            }
          },
          data: peopleAttendance
        }]
      };

      myChart = echarts.init(document.getElementById('chartsWrap'));
      myChart.setOption(option);

      //图表事件
      myChart.on('click', function (param) {
        var id = param.data.monitorId;
        var inx = param.dataIndex;
        scheduledMileageReport.attendanceDetail(id, inx);
      });
    },
    chartResize: function () {
      if (myChart) {
        myChart.resize();
      }
    },
    //tab切换
    tabChange: function () {
      var inx = $(this).index();
      $('.export-btn').eq(inx).removeClass('hide').siblings('.export-btn').addClass('hide');
    },
    setTab: function (inx) {
      var tabs = $('.nav-tabs li'),
        tabItem = $('.tab-content .tab-pane');
      tabs.eq(inx).addClass('active').siblings('li').removeClass('active');
      tabItem.eq(inx).addClass('active').siblings('.tab-pane').removeClass('active');
      $('.export-btn').eq(inx).removeClass('hide').siblings('.export-btn').addClass('hide');
    },
    export2: function () {
      window.location.href = '/clbs/talkback/reportManagement/scheduledMileageReport/exportDetail_' + monitorId;
    },
    // 离职人员勾选
    changeIncumbency: function () {
      var treeObj = $.fn.zTree.getZTreeObj('treeDemo');
      var isIncumbency = $('#isIncumbency').is(':checked');
      //离职节点
      var incumbency = treeObj.getNodesByFilter(function (item) {
        return item.isIncumbency === 0 && item.id !== -1;
      });

      if (isIncumbency) {//包含离职
        treeObj.showNodes(incumbency);
        for (var i = 0; i < incumbency.length; i++) {
          var item = incumbency[i];
          if (item.checked && peopleIds.indexOf(item.id) === -1) {
            peopleIds.push(item.id);
            peopleTreeName.push(item.name);
          }
        }
      } else {
        treeObj.hideNodes(incumbency);
        for (var i = 0; i < incumbency.length; i++) {
          var item = incumbency[i];
          if (item.checked && peopleIds.indexOf(item.id) !== -1) {
            peopleIds.splice(peopleIds.indexOf(item.id), 1);
            peopleTreeName.splice(peopleTreeName.indexOf(item.name), 1);
          }
        }
      }

      var groupSelect = $('#groupSelect');
      if (peopleIds.length === 0) {
        groupSelect.val('');
      } else {
        groupSelect.val(peopleTreeName.join(','));
      }
    }
  };

  $(function () {
    scheduledMileageReport.init();

    //事件
    $('#schedule').on('change', scheduledMileageReport.selectFun);
    $('#groupSelect').on('click', showMenuContent);
    $('#isIncumbency').on('change', scheduledMileageReport.changeIncumbency);
    $('#inquireClick').on('click', scheduledMileageReport.inquireClick);
    $('#stretch2').on('click', scheduledMileageReport.setCharts);
    $('.nav-tabs').on('click', 'li', scheduledMileageReport.tabChange);
    $('#alarmExport2').on('click', scheduledMileageReport.export2);
    // echarts图表resize
    $('#toggle-left-button').on('click', function () {
      setTimeout(function () {
        scheduledMileageReport.chartResize();
      }, 500);
    });
    $(window).resize(scheduledMileageReport.chartResize);
  });
})(window, $);