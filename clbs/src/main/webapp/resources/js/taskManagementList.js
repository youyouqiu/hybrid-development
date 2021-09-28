var myTable, myTable2;
var taskManagementList;
var editStatus = '未开始'; // 获取修改状态，默认为未开始

(function (window, $) {
  var table1 = $('#dataTable tr th:gt(0)');
  var table2 = $('#dataTable2 tr th:gt(0)');

  taskManagementList = {
    init: function () {
      $('input').inputClear();
      taskManagementList.tableLayout(table1, '#Ul-menu-text');
      taskManagementList.tableLayout(table2, '#Ul-menu-text2');

      taskManagementList.initTable(); // 任务库
      taskManagementList.initTable2(); // 任务指派
    },
    tableLayout: function (table, menuId) {
      var menu_text = '';
      menu_text += '<li><label><input type="checkbox" checked=\'checked\' class="toggle-vis" data-column="' + parseInt(1) + '" disabled />' + table[0].innerHTML + '</label></li>';
      for (var i = 1; i < table.length; i++) {
        menu_text += '<li><label><input type="checkbox" checked=\'checked\' class="toggle-vis" data-column="' + parseInt(i + 1) + '" />' + table[i].innerHTML + '</label></li>';
      }

      $(menuId).html(menu_text);
    },
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
            var designateNames = row.designateNames;//关联指派

            var editUrlPath = myTable.editUrl + row.id + '.gsp'; //修改地址
            var detailUrlPath = myTable.detailUrl + row.id + '.gsp'; //详情地址

            var result = '';
            if (designateNames == null) {//未开始，执行中
              result += '<button href="' + editUrlPath + '" data-target="#commonLgWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&nbsp;';
              result += '<button type="button" onclick="myTable.deleteItem(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>&nbsp;';
            }

            result += '<button href="' + detailUrlPath + '" data-target="#commonLgWin" data-toggle="modal" type="button" class=" editBtn editBtn-info"><i class="fa fa-sun-o"></i>详情</button>&nbsp;';

            return result;
          }
        },
        {
          'data': 'taskName',
          'class': 'text-center'
        },
        {
          'data': 'designateNames',
          'class': 'text-center'
        }, {
          'data': 'groupName',
          'class': 'text-center'
        }, {
          'data': 'createDataUsername',
          'class': 'text-center'
        }, {
          'data': 'createDataTime',
          'class': 'text-center'
        }, {
          'data': 'remark',
          'class': 'text-center'
        }

      ];
      //ajax参数
      var ajaxDataParamFun = function (d) {
        d.simpleQueryParam = $('#simpleQueryParam').val();
      };
      //表格setting
      var setting = {
        listUrl: '/clbs/a/taskManagement/getTaskList',
        detailUrl: '/clbs/a/taskManagement/getDetailPage_',
        editUrl: '/clbs/a/taskManagement/getTaskEditPage_',
        deleteUrl: '/clbs/a/taskManagement/deleteTask_',
        drawCallbackFun: function () {
          var api = myTable.dataTable;
          var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
          api.column(0).nodes().each(function (cell, i) {
            cell.innerHTML = startIndex + i + 1;
          });
        },
        columnDefs: columnDefs, //表格列定义
        columns: columns, //表格列
        dataTableDiv: 'dataTable', //表格
        ajaxDataParamFun: ajaxDataParamFun, //ajax参数
        pageable: true, //是否分页
        showIndexColumn: true, //是否显示第一列的索引列
        enabledChange: true
      };
      myTable = new TG_Tabel.createNew(setting);
      myTable.init();
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
          'data': null,
          'class': 'text-center',
          render: function (data, type, row, meta) {
            var status = row.status,//状态
              forcedEnd = row.forcedEnd;//强制结束

            var editUrlPath = myTable2.editUrl + row.id; //修改地址
            var detailUrlPath = myTable2.detailUrl + row.id; //详情地址

            var result = '';

            if ((status == '未开始' || status == '执行中') && forcedEnd == null) {//未开始，执行中
              result += '<button onclick="taskManagementList.changeSchedule(\'' + status + '\')" href="' + editUrlPath + '" data-target="#commonLgWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&nbsp;';
            }
            if (status == '执行中' && forcedEnd == null) {//执行中
              result += '<button type="button" onclick="taskManagementList.endSchedule(\'' + row.id + '\')" class="editBtn editBtn-info"><i class="fa fa-end"></i>结束</button>&nbsp;';
            }
            if (status == '未开始') {//未开始
              result += '<button type="button" onclick="myTable2.deleteItem(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>&nbsp;';
            }

            if (status == '执行中' && forcedEnd == 1) {//强制结束修改,结束按钮置灰
              result += '<button  type="button" class="editBtn btn-default editBtn-info"><i class="fa fa-ban"></i>修改</button>&nbsp;';
              result += '<button type="button" class="editBtn btn-default editBtn-info"><i class="fa fa-ban"></i>结束</button>&nbsp;';
            }

            result += '<button href="' + detailUrlPath + '" data-target="#commonLgWin" data-toggle="modal" type="button" class=" editBtn editBtn-info"><i class="fa fa-sun-o"></i>详情</button>&nbsp;';

            return result;
          }
        },
        {
          'data': 'designateName',
          'class': 'text-center'
        },
        {
          'data': 'taskName',
          'class': 'text-center'
        }, {
          'data': 'startDateStr',
          'class': 'text-center'
        }, {
          'data': 'endDateStr',
          'class': 'text-center'
        }, {
          'data': 'status',
          'class': 'text-center'
        }, {
          'data': 'groupName',
          'class': 'text-center'
        }, {
          'data': 'createDataUsername',
          'class': 'text-center'
        }, {
          'data': 'createDataTime',
          'class': 'text-center'
        }, {
          'data': 'remark',
          'class': 'text-center'
        }

      ];
      //ajax参数
      var ajaxDataParamFun = function (d) {
        d.simpleQueryParam = $('#simpleQueryParam2').val();
      };
      //表格setting
      var setting = {
        listUrl: '/clbs/a/taskManagement/getDesignateList',
        detailUrl: '/clbs/a/taskManagement/getDesignateDetail_',
        editUrl: '/clbs/a/taskManagement/getDesignateEditPage_',
        deleteUrl: '/clbs/a/taskManagement/deleteDesignate_',
        columnDefs: columnDefs, //表格列定义
        columns: columns, //表格列
        dataTableDiv: 'dataTable2', //表格
        ajaxDataParamFun: ajaxDataParamFun, //ajax参数
        pageable: true, //是否分页
        showIndexColumn: true, //是否显示第一列的索引列
        enabledChange: true
      };
      myTable2 = new TG_Tabel.createNew(setting);
      myTable2.init();
    },
    //指派修改
    changeSchedule: function (status) {
      editStatus = status;
    },
    //结束指派
    endSchedule: function (id) {
      tg_confirmDialog('操作确认', '结束后,该指派结束日期将自动设为今天.', function () {
        var paramer = {
          id: id
        };
        json_ajax('POST', '/clbs/a/taskManagement/forcedEnd', 'json', true, paramer, taskManagementList.endScheduleCallBack);
      });
    },
    endScheduleCallBack: function (data) {
      if (data.success) {
        myTable2.requestData();
      } else {
        layer.msg(data.msg);
      }
    },
    // 刷新table列表
    refreshTable: function () {
      $('#simpleQueryParam').val('');
      myTable.requestData();
    },
    refreshTable2: function () {
      $('#simpleQueryParam2').val('');
      myTable2.requestData();
    },
    search: function (event) {
      if (event.keyCode == 13) {
        myTable2.requestData();
      }
    }
  };

  $(function () {
    taskManagementList.init();
    $('#refreshTable').on('click', taskManagementList.refreshTable);
    $('#refreshTable2').on('click', taskManagementList.refreshTable2);

    //显示隐藏列
    $('#Ul-menu-text .toggle-vis').on('change', function (e) {
      e.preventDefault();
      var column = myTable.dataTable.column($(this).attr('data-column'));
      column.visible(!column.visible());
      $(this).parent().parent().parent().parent().addClass('open');
    });
    $('#Ul-menu-text2 .toggle-vis').on('change', function (e) {
      e.preventDefault();
      var column = myTable2.dataTable.column($(this).attr('data-column'));
      column.visible(!column.visible());
      $(this).parent().parent().parent().parent().addClass('open');
    });
  });
})(window, $);