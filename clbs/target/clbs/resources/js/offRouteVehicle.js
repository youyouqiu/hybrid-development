(function (window, $) {
  /**
   * 路线偏离车辆统计
   * */
  var monitorTable; //车辆列表
  var vehicleId = '';//勾选车辆id
  var checkFlag = false; //判断组织节点是否是勾选操作
  var size;//当前权限监控对象数量
  var zTreeIdJson = {};

  var bflag = true; //模糊查询
  var crrentSubV = []; //模糊查询
  var ifAllCheck = true; //刚进入页面小于100自动勾选
  var searchFlag = true;
  var fuzzyQueryParam = '';
  var searchMonth = '';
  var monitorDetailTable;// 详情table
  var curSelectmonitorId = '';// 列表行当前选择监控对象的id
  var curMonthTime = '';// 详情抽屉月份控件当前时间
  var tableInit = false;

  speedingVehicle = {
      init: function () {
          // 显示隐藏列
          speedingVehicle.columnRender();
          // 月份日期控件初始化
          speedingVehicle.monthDataInit();
          // 初始化监控对象树
          speedingVehicle.initTree();
      },
      inquireClick: function (number) {
          $(".ToolPanel").css("display", "block");

          speedingVehicle.getCheckedNodes();
          if (!speedingVehicle.validates()) {
              return;
          }
          // speedingVehicle.columnRender();
          if (tableInit) {
              monitorTable.requestData();
          }else{
              speedingVehicle.initTable();
          }
      },
      /**
       * 列表相关方法
       * */
      // 渲染显示隐藏列
      columnRender: function () {
          //显示隐藏列
          var menu_text = "";
          var table = $("#monitorDataTable tr th:gt(0)");
          for (var i = 0; i < table.length; i++) {
              menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
          }
          $("#Ul-menu-text1").html(menu_text);
      },
      getTable: function (table) {
          $('.toggle-vis1').prop('checked', true);
          monitorTable = $(table).DataTable({
              "columnDefs": [{
                  "targets": 0,
                  "searchable": false // 禁止第一列参与搜索
              }, {
                  "orderable": false
              }],
              "destroy": true,
              "dom": 'tiprl',// 自定义显示项
              "lengthChange": true,// 是否允许用户自定义显示数量
              "bPaginate": true, // 翻页功能
              "bFilter": true, // 列筛序功能
              "searching": true,// 本地搜索
              "ordering": false, // 排序功能
              "Info": true,// 页脚信息
              "autoWidth": true,// 自动宽度
              "stripeClasses": [],
              "pageLength": 10,
              "lengthMenu": [5, 10, 20, 50, 100, 200],
              "pagingType": "simple_numbers", // 分页样式
              "dom": "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
              "oLanguage": {// 国际语言转化
                  "oAria": {
                      "sSortAscending": " - click/return to sort ascending",
                      "sSortDescending": " - click/return to sort descending"
                  },
                  "sLengthMenu": "显示 _MENU_ 记录",
                  "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录。",
                  "sZeroRecords": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                  "sEmptyTable": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                  "sLoadingRecords": "正在加载数据-请等待...",
                  "sInfoEmpty": "当前显示0到0条，共0条记录",
                  "sInfoFiltered": "",
                  "sProcessing": "<img src='../resources/user_share/row_details/select2-spinner.gif'/> 正在加载数据...",
                  "sSearch": "模糊查询：",
                  "sUrl": "",
                  "oPaginate": {
                      "sFirst": "首页",
                      "sPrevious": " 上一页 ",
                      "sNext": " 下一页 ",
                      "sLast": " 尾页 "
                  }
              }
          });
          monitorTable.on('order.dt search.dt', function () {
              monitorTable.column(0, {
                  search: 'applied',
                  order: 'applied'
              }).nodes().each(function (cell, i) {
                  cell.innerHTML = i + 1;
              });
          }).draw();
          //显示隐藏列
          $('.toggle-vis1').off('change').on('change', function (e) {
              var column = monitorTable.column($(this).attr('data-column'));
              column.visible(!column.visible());
              $(".keep-open1").addClass("open");
          });
      },
      initTable: function () {
          tableInit = true;
          var columnDefs = [{
              //第一列，用来显示序号
              "searchable": false,
              "targets": 0
          }];
          var columns = [
              {
                  //第一列，用来显示序号
                  "data": null,
                  "class": "text-center"
              }, {
                  "data": "monitorName",
                  "class": "text-center",
              }, {
                  "data": "plateColor",
                  "class": "text-center",
                  render: function (data, type, row, meta) {
                      if (data) {
                          return getPlateColor(data);
                      }
                      return '';
                  },
              }, {
                  "data": "vehicleType",
                  "class": "text-center"
              }, {
                  "data": "courseDeviation",
                  "class": "text-center",
                  render: function (data, type, row, meta) {
                      return data ? data : 0;
                  }
              }, {
                  "data": "notFollowLine",
                  "class": "text-center",
                  render: function (data, type, row, meta) {
                      return data ? data : 0;
                  }
              }, {
                  "data": "total",
                  "class": "text-center"
              },
          ];

          //ajax参数
          var ajaxDataParamFun = function (d) {
              d.simpleQueryParam = $('#monitorQueryParam').val();
              fuzzyQueryParam = $('#monitorQueryParam').val();
              d.monitorIds = vehicleId;
              var time = $('#timeInterval').val().replace('-', '');
              d.month = time;
              searchMonth = time;
          };
          //表格setting
          var setting = {
              listUrl: "/clbs/cb/cbReportManagement/offRoute/getMonitorDataList",
              columnDefs: columnDefs, //表格列定义
              columns: columns, //表格列
              dataTableDiv: 'monitorDataTable', //表格
              ajaxDataParamFun: ajaxDataParamFun, //ajax参数
              drawCallbackFun: function () {
                  var api = monitorTable.dataTable;
                  var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
                  api.column(0).nodes().each(function (cell, i) {
                      cell.innerHTML = startIndex + i + 1;
                      var monitorId = api.data()[i].monitorId;
                      $(cell).closest('tr').attr('data-id', monitorId);
                  });
              },
              ajaxCallBack: speedingVehicle.drawTableCallbackFun,
              pageable: true, //是否分页
              showIndexColumn: true, //是否显示第一列的索引列
              enabledChange: true
          };
          monitorTable = new TG_Tabel.createNew(setting);
          monitorTable.init();

          $('#exportMonitor').prop('disabled', false);

          //显示隐藏列
          $('.toggle-vis1').off('change').on('change', function (e) {
              var column = monitorTable.dataTable.column($(this).attr('data-column'));
              column.visible(!column.visible());
              $(".keep-open1").addClass("open");
          });

          $('#monitorTableSearch').on('click', function (e) {
              monitorTable.refresh();
          });
          $('#monitorQueryParam').on('keydown', function (e) {
              if(e.keyCode == 13) {
                  monitorTable.refresh();
              }
          });

          $('#monitorDataTable tbody').bind('click', 'tr', function (e) {
              if ($(this).find('.dataTables_empty').length === 0) {
                  speedingVehicle.trClickFun(e);
              }
          });
      },
      drawTableCallbackFun: function () {
          var checks = $("#Ul-menu-text1 .toggle-vis1");
          for (var i = 0, len = checks.length; i < len; i++) {
              (function (index) {
                  var columnIndex = $(checks[index]).attr('data-column'), checked = $(checks[index]).prop("checked");
                  var column = monitorTable.dataTable.column(columnIndex);
                  column.visible(checked);
              })(i)
          }
      },
      //刷新列表
      refreshTable: function () {
          $("#monitorQueryParam").val("");
          monitorTable.refresh();
      },
      exportMonitor: function () {
          if ($("#monitorDataTable tbody tr td").hasClass("dataTables_empty")) {
              layer.msg("列表无任何数据,无法导出");
              return;
          }
          if(getRecordsNum("monitorDataTable_info") > 60000){
              return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
          }
          var paramer = {
              month: searchMonth,
              monitorIds: vehicleId,
              simpleQueryParam: fuzzyQueryParam,
              module: '路线偏离车辆统计报表'
          };
          var url = "/clbs/cb/cbReportManagement/offRoute/exportMonitorDataList";
          json_ajax("post", url, "json", true, paramer, function (result) {
              if (result.success) {
                  layer.confirm(exportTitle, {
                      title: '操作确认',
                      icon: 3, // 问号图标
                      btn: ['确定', '导出管理'] //按钮
                  }, function () {
                      layer.closeAll();
                  }, function () {
                      layer.closeAll();
                      // 打开导出管理弹窗
                      pagesNav.showExportManager();
                  });
              } else if (result.msg) {
                  layer.msg(result.msg);
              }
          });
      },

      /**
       * 车辆详情抽屉相关方法
       * */
      // 列表行点击查看车辆超速统计明细信息
      trClickFun: function (e) {
          e.stopPropagation();
          var _this = $(e.target);
          if (e.target.nodeName !== 'TR') {
              _this = $(e.target).closest('tr');
          }
          if (_this.hasClass('active')) {
              $('#monitorDetail').removeClass('active');
          } else {
              $('#monitorDetailMonth').html($('#timeInterval').val());
              $('#monitorDetail').addClass('active');
              var monitorId = _this.attr('data-id');
              curSelectmonitorId = monitorId;
              var time = $('#timeInterval').val().replace('-', '');
              curMonthTime = time;
              speedingVehicle.getVehicleDetailInfo();
              speedingVehicle.initDetailTable();
          }
          _this.toggleClass('active');
          _this.siblings('tr').removeClass('active');
      },
      exportMonitorDeatil: function () {
          if ($("#monitorDetailTable tbody tr td").hasClass("dataTables_empty")
              || $("#monitorDetailTable tbody").find('td').length === 0) {
              layer.msg("列表无任何数据,无法导出");
              return;
          }
          if(getRecordsNum("monitorDetailTable_info") > 60000){
              return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
          }
          var paramer = {
              month: curMonthTime,
              monitorId: curSelectmonitorId,
              module: '路线偏离车辆明细报表'
          };
          var url = "/clbs/cb/cbReportManagement/offRoute/exportMonitorDetailList";
          json_ajax("post", url, "json", true, paramer, function (result) {
              if (result.success) {
                  layer.confirm(exportTitle, {
                      title: '操作确认',
                      icon: 3, // 问号图标
                      btn: ['确定', '导出管理'] //按钮
                  }, function () {
                      layer.closeAll();
                  }, function () {
                      layer.closeAll();
                      // 打开导出管理弹窗
                      pagesNav.showExportManager();
                  });
              } else if (result.msg) {
                  layer.msg(result.msg);
              }
          });
      },
      /**
       * 获取车辆超速统计明细数据
       * */
      getVehicleDetailInfo: function () {
          var ajaxDataParam = {
              "monitorId": curSelectmonitorId,
              "month": curMonthTime
          };
          var infoUrl = '/clbs/cb/cbReportManagement/offRoute/getMonitorChartStatisticsData';
          json_ajax("post", infoUrl, "json", true, ajaxDataParam, function (result) {
              if (result.success) {
                  var data = result.obj;
                  // 详情抽屉字段赋值
                  if (data.monitorName) {
                      $('#monitorName').html(data.monitorName);
                      $('#monitorOrg').html(data.orgName);
                      $('#plateColor').html(data.plateColorStr);
                      $('#objectType').html(data.vehicleType);
                  }
                  var noStr = '-';
                  $('#monitorCent20To50PerNum').html(data.notFollowLine === undefined ? noStr : data.notFollowLine);
                  $('#monitorLess20PerUnder').html(data.courseDeviation === undefined ? noStr : data.courseDeviation);
                  // 获取详情图表数据
                  speedingVehicle.chartRender(result, 'monitorAccountedEchart', 'monitorTrendEchart');
              } else if (result.msg) {
                  var result = {
                      obj: {
                          detailList: [],
                      }
                  };
                  speedingVehicle.chartRender(result, 'monitorAccountedEchart', 'monitorTrendEchart');
                  layer.msg(result.msg);
              }
          });
      },
      // 详情抽屉列表渲染
      initDetailTable: function () {
          var columnDefs = [{
              //第一列，用来显示序号
              "searchable": false,
              "targets": 0
          }];
          var columns = [
              {
                  //第一列，用来显示序号
                  "data": null,
                  "class": "text-center"
              }, {
                  "data": "alarmStartTime",
                  "class": "text-center",
                  render: function (data, type, row, meta) {
                      if (!data) return '';
                      var date = data.substr(0, 4) + '-' + data.substr(4, 2) + '-' + data.substr(6, 2);
                      var time = data.substr(8, 2) + ':' + data.substr(10, 2) + ':' + data.substr(12, 2);
                      return date + ' ' + time;
                  }
              }, {
                  "data": "lineName",
                  "class": "text-center",
                  render: function (data, type, row, meta) {
                      if (!data) return '-';
                      return data;
                  }
              }, {
                  "data": "description",
                  "class": "text-center"
              }, {
                  "data": "address",
                  "class": "text-center"
              },
          ];

          //ajax参数
          var ajaxDataParamFun = function (d) {
              d.monitorId = curSelectmonitorId;
              d.month = curMonthTime;
          };
          //表格setting
          var setting = {
              listUrl: "/clbs/cb/cbReportManagement/offRoute/getMonitorDetailList",
              columnDefs: columnDefs, //表格列定义
              columns: columns, //表格列
              dataTableDiv: 'monitorDetailTable', //表格
              ajaxDataParamFun: ajaxDataParamFun, //ajax参数
              drawCallbackFun: function () {
                  var api = monitorDetailTable.dataTable;
                  var startIndex = api.context[0]._iDisplayStart;//获取到本页开始的条数
                  api.column(0).nodes().each(function (cell, i) {
                      cell.innerHTML = startIndex + i + 1;
                  });
              },
              pagingType: 'simple',
              pageable: true, //是否分页
              showIndexColumn: true, //是否显示第一列的索引列
              enabledChange: true
          };
          monitorDetailTable = new TG_Tabel.createNew(setting);
          monitorDetailTable.init();

          $("[data-toggle='tooltip']").tooltip();
          $('#monitorDetailExport').prop('disabled', false);
      },
      //解析位置信息
      getAddress: function (e, latitudeLongitude) {
          var _this = $(e);
          var url = '/clbs/v/monitoring/address';
          var latitude = latitudeLongitude.split(',')[1];
          var longitude = latitudeLongitude.split(',')[0];
          var param = {addressReverse: [latitude, longitude, '', "", 'vehicle']};

          $.ajax({
              type: "POST",//通常会用到两种：GET,POST。默认是：GET
              url: url,//(默认: 当前页地址) 发送请求的地址
              dataType: "json", //预期服务器返回的数据类型。"json"
              async: true, // 异步同步，true  false
              data: param,
              traditional: true,
              timeout: 8000, //超时时间设置，单位毫秒
              success: function (data) {//请求成功
                  _this.closest('td').html($.isPlainObject(data) ? '未定位' : data);
              },
          });
      },
      /**
       * 图表渲染
       * @param result 数据
       * @param accountedElement 饼图渲染元素
       * @param trendElement 柱状图渲染元素
       * */
      chartRender: function (result, accountedElement, trendElement) {
          var data = result.obj;
          var accountedData = [
              {value: data.notFollowLine, name: '不按规定线路运行'},
              {value: data.courseDeviation, name: '路线偏离'},
          ];
          speedingVehicle.accountedEchart(accountedData, accountedElement);
          // 组装超速趋势走向图表数据
          var statisticInfo = data.detailList;
          var echartData = {
              'xData': [],
              'courseDeviation': [],
              'notFollowLine': [],
              'monthRate': [],
              'ringRatio': [],
          };
          for (var i = 0; i < statisticInfo.length; i++) {
              var item = statisticInfo[i];
              echartData.xData.push(item.day);
              echartData.courseDeviation.push(item.courseDeviation ? item.courseDeviation : 0);
              echartData.notFollowLine.push(item.notFollowLine ? item.notFollowLine : 0);
              echartData.monthRate.push(item.monthRate ? item.monthRate : 0);
              echartData.ringRatio.push(item.ringRatio ? item.ringRatio : 0);
          }
          speedingVehicle.trendEchart(echartData, trendElement);
      },
      /**
       * 超速车辆占比情况饼图
       * @param data 饼图数据
       * @param element 渲染元素
       * */
      accountedEchart: function (data, element) {
          var myChart = echarts.init(document.getElementById(element));
          var option = {
              title: {
                  text: '报警占比情况',
                  left: 'center'
              },
              color: ['rgb(255,219,92)', 'rgb(55,162,218)'],
              tooltip: {
                  trigger: 'item',
                  formatter: '{b} : {c} ({d}%)'
              },
              legend: {
                  orient: 'horizontal',
                  bottom: 'bottom',
                  data: ['不按规定线路运行', '路线偏离']
              },
              series: [
                  {
                      name: '报警占比',
                      type: 'pie',
                      radius: '55%',
                      data: data,
                      emphasis: {
                          itemStyle: {
                              shadowBlur: 10,
                              shadowOffsetX: 0,
                              shadowColor: 'rgba(0, 0, 0, 0.5)'
                          }
                      }
                  }
              ]
          };
          myChart.clear();
          myChart.setOption(option);
          window.onresize = function () {
              myChart.resize();
          };
      },
      /**
       * 超速趋势走向图表
       * @param echartData 轴数据
       * @param element 渲染元素
       * */
      trendEchart: function (echartData, element) {
          var myChart = echarts.init(document.getElementById(element));
          var option = {
              tooltip: {
                  trigger: 'axis',
                  formatter: function (series) {
                      var time = series[0].axisValue;
                      var timeStr = time.substring(0, 4) + '-' + time.substring(4, 6) + '-' + time.substring(6, 8);
                      var result = '<div class="formatterBox"><h4>' + timeStr + '</h4>';
                      for (var i = 0; i < series.length; i++) {
                          var item = series[i];
                          if (i < 2) {
                              result += '<p><i style="background-color: ' + item.color + '"></i>' + item.seriesName + '：' + item.data + '</p>';
                          } else {
                              var showData = 0;
                              if (item.data < 0) {
                                  showData = '下降 ' + Math.abs(item.data);
                              } else if (item.data > 0) {
                                  showData = '上升 ' + item.data;
                              }
                              result += '<span><i style="background-color: ' + item.color + '"></i>' + item.seriesName + '：' + showData + '%</span>';
                          }
                      }
                      result += '</div>';
                      return result;
                  }
              },
              color: ['rgb(255,219,92)', 'rgb(55,162,218)', 'rgb(253,23,253)', 'rgb(255,0,0)'],
              grid: {
                  top: 60,
                  right: 50,
              },
              legend: {
                  orient: 'horizontal',
                  top: 0,
                  right: 10,
                  type: 'scroll',
                  data: ['不按规定线路运行', '路线偏离', '环比率', '同比率']
              },
              dataZoom: [{
                  type: 'slider',
                  show: true,
                  xAxisIndex: [0],
                  left: '9%',
                  right: 80,
                  bottom: -5,
                  start: 0,
                  end: element === 'trendDetailEchart' ? 50 : 70,
                  maxValueSpan: element === 'trendDetailEchart' ? 16 : 20,
              }],
              xAxis: [
                  {
                      type: 'category',
                      data: echartData.xData,
                      axisPointer: {
                          type: 'shadow'
                      },
                      axisLabel: {
                          formatter: (val) => {
                              var len = val.length;
                              return val.substring(len - 2, len);
                          }
                      }
                  }
              ],
              yAxis: [
                  {
                      type: 'value',
                      name: '',
                      position: 'left',
                      axisLabel: {
                          formatter: '{value}'
                      },
                  },
                  {
                      type: 'value',
                      name: '',
                      position: 'right',
                      axisLabel: {
                          formatter: '{value} %'
                      },
                  }
              ],
              series: [
                  {
                      name: '不按规定线路运行',
                      type: 'bar',
                      barWidth: 10,
                      // itemStyle: {// 隐藏该柱状图显示
                      //     opacity: 0,
                      //     borderWidth: 0,
                      // },
                      data: echartData.notFollowLine,
                  }, {
                      name: '路线偏离',
                      type: 'bar',
                      barWidth: 10,
                      // itemStyle: {// 隐藏该柱状图显示
                      //     opacity: 0,
                      //     borderWidth: 0,
                      // },
                      data: echartData.courseDeviation,
                  }, {
                      name: '环比率',
                      type: 'line',
                      yAxisIndex: 1,
                      symbol: 'none',// 去掉点
                      smooth: true,// 曲线平滑
                      data: echartData.ringRatio
                  }, {
                      name: '同比率',
                      type: 'line',
                      yAxisIndex: 1,
                      symbol: 'none',// 去掉点
                      smooth: true,// 曲线平滑
                      data: echartData.monthRate
                  },
              ]
          };
          myChart.clear();
          myChart.setOption(option);
          window.onresize = function () {
              myChart.resize();
          };
      },
      left_arrow: function () {
          var monitorDetailMonth = $('#monitorDetailMonth').html();
          var date = new Date(monitorDetailMonth.replace(/-/g, '/') + '/01 00:00:00');
          var now = new Date();
          var dateYear = date.getFullYear();
          var dateMonth = date.getMonth();
          var nowYear = now.getFullYear();
          var nowMonth = now.getMonth();
          if (dateYear >= nowYear && dateMonth > nowMonth) {
              return;
          }
          date.setMonth(date.getMonth() - 1);
          dateYear = date.getFullYear();
          dateMonth = date.getMonth() + 1;
          var newDate = dateYear + '-' + speedingVehicle.appendZero(dateMonth);
          $('#monitorDetailMonth').html(newDate);
          curMonthTime = newDate.replace('-', '');
          speedingVehicle.getVehicleDetailInfo();
          speedingVehicle.initDetailTable();
      },
      right_arrow: function () {
          var monitorDetailMonth = $('#monitorDetailMonth').html();
          var date = new Date(monitorDetailMonth.replace(/-/g, '/') + '/01 00:00:00');
          var now = new Date();
          var dateYear = date.getFullYear();
          var dateMonth = date.getMonth();
          var nowYear = now.getFullYear();
          var nowMonth = now.getMonth();
          if (dateYear >= nowYear && dateMonth + 1 > nowMonth) {
              return;
          }
          date.setMonth(date.getMonth() + 1);
          dateYear = date.getFullYear();
          dateMonth = date.getMonth() + 1;
          var newDate = dateYear + '-' + speedingVehicle.appendZero(dateMonth);
          $('#monitorDetailMonth').html(newDate);
          curMonthTime = newDate.replace('-', '');
          speedingVehicle.getVehicleDetailInfo();
          speedingVehicle.initDetailTable();
      },
      appendZero: function (val) {
          if (val < 10) {
              return '0' + val.toString();
          }
          return val.toString();
      },

      /**
       * 监控对象树相关方法
       * */
      // 初始化监控对象树
      initTree: function () {
          //车辆树
          var setting = {
              async: {
                  url: speedingVehicle.getTerrUrl,
                  type: "post",
                  enable: true,
                  autoParam: ["id"],
                  dataType: "json",
                  otherParam: {"type": "multiple", "icoType": "0"},
                  dataFilter: speedingVehicle.ajaxDataFilter
              },
              check: {
                  enable: true,
                  chkStyle: "checkbox",
                  chkboxType: {
                      "Y": "s",
                      "N": "s"
                  },
                  radioType: "all"
              },
              view: {
                  dblClickExpand: false,
                  nameIsHTML: true,
                  countClass: "group-number-statistics"
              },
              data: {
                  simpleData: {
                      enable: true
                  }
              },
              callback: {
                  beforeClick: speedingVehicle.beforeClickVehicle,
                  onAsyncSuccess: speedingVehicle.zTreeOnAsyncSuccess,
                  beforeCheck: speedingVehicle.zTreeBeforeCheck,
                  onCheck: speedingVehicle.onCheckVehicle,
                  onNodeCreated: speedingVehicle.zTreeOnNodeCreated,
                  onExpand: speedingVehicle.zTreeOnExpand
              }
          };
          $.fn.zTree.init($("#monitorTreeDemo"), setting, null);
      },
      //模糊查询树
      searchVehicleTree: function (param) {
          ifAllCheck = false;//模糊查询不自动勾选

          crrentSubV = [];
          if (param == null || param == undefined || param == '') {
              bflag = true;
              speedingVehicle.init();
          } else {
              bflag = true;
              var querySetting = {
                  async: {
                      url: "/clbs/a/search/reportFuzzySearch",
                      type: "post",
                      enable: true,
                      autoParam: ["id"],
                      dataType: "json",
                      otherParam: {"type": $('#queryType').val(), "queryParam": param, "queryType": "vehicle"},
                      dataFilter: speedingVehicle.ajaxQueryDataFilter
                  },
                  check: {
                      enable: true,
                      chkStyle: "checkbox",
                      radioType: "all",
                      chkboxType: {
                          "Y": "s",
                          "N": "s"
                      }
                  },
                  view: {
                      dblClickExpand: false,
                      nameIsHTML: true,
                      countClass: "group-number-statistics"
                  },
                  data: {
                      simpleData: {
                          enable: true
                      }
                  },
                  callback: {
                      beforeClick: speedingVehicle.beforeClickVehicle,
                      onCheck: speedingVehicle.onCheckVehicle,
                      onExpand: speedingVehicle.zTreeOnExpand,
                      onNodeCreated: speedingVehicle.zTreeOnNodeCreated
                  }
              };
              $.fn.zTree.init($("#monitorTreeDemo"), querySetting, null);
          }
      },
      ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
          responseData = JSON.parse(ungzip(responseData))
          var nodesArr;
          if ($('#queryType').val() == "vehicle") {
              nodesArr = filterQueryResult(responseData, crrentSubV);
          } else {
              nodesArr = responseData;
          }
          for (var i = 0; i < nodesArr.length; i++) {
              nodesArr[i].open = true;
          }
          return nodesArr;
      },
      getTerrUrl: function (treeId, treeNode) {
          if (treeNode == null) {
              return "/clbs/m/personalized/ico/vehicleTree";
          } else if (treeNode.type == "assignment") {
              return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
          }
      },
      validates: function () {
          return $("#speedlist").validate({
              rules: {
                  timeInterval: {
                      required: true
                  },
                  monitorGroupSelect: {
                      zTreeChecked: "monitorTreeDemo"
                  }
              },
              messages: {
                  timeInterval: {
                      required: "请选择日期",
                  },
                  monitorGroupSelect: {
                      zTreeChecked: vehicleSelectBrand,
                  }
              }
          }).form();
      },
      unique: function (arr) {
          var result = [], hash = {};
          for (var i = 0, elem; (elem = arr[i]) != null; i++) {
              if (!hash[elem]) {
                  result.push(elem);
                  hash[elem] = true;
              }
          }
          return result;
      },
      ajaxDataFilter: function (treeId, parentNode, responseData) {
          var treeObj = $.fn.zTree.getZTreeObj("monitorTreeDemo");
          if (responseData.msg) {
              var obj = JSON.parse(ungzip(responseData.msg));
              var data;
              if (obj.tree != null && obj.tree != undefined) {
                  data = obj.tree;
                  size = obj.size;
              } else {
                  data = obj
              }
              for (var i = 0; i < data.length; i++) {
                  data[i].open = true;
              }
          }
          return data;
      },
      beforeClickVehicle: function (treeId, treeNode) {
          var zTree = $.fn.zTree.getZTreeObj("monitorTreeDemo");
          zTree.checkNode(treeNode, !treeNode.checked, true, true);
          return false;
      },
      zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
          var treeObj = $.fn.zTree.getZTreeObj("monitorTreeDemo");
          if (size <= 100 && ifAllCheck) {
              treeObj.checkAllNodes(true);
          }
          speedingVehicle.getCharSelect(treeObj);
      },
      zTreeBeforeCheck: function (treeId, treeNode) {
          var flag = true;
          if (!treeNode.checked) {
              if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                  var zTree = $.fn.zTree.getZTreeObj("monitorTreeDemo"), nodes = zTree
                      .getCheckedNodes(true), v = "";
                  var nodesLength = 0;

                  json_ajax("post", "/clbs/m/personalized/ico/getVehicleCount",
                      "json", false, {"parentId": treeNode.id, "type": treeNode.type}, function (data) {
                          if (data.success) {
                              nodesLength += data.obj;
                          } else {
                              layer.msg(data.msg);
                          }
                      });

                  //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                  var ns = [];
                  //节点id
                  var nodeId;
                  for (var i = 0; i < nodes.length; i++) {
                      nodeId = nodes[i].id;
                      if (nodes[i].type == "people" || nodes[i].type == "vehicle") {
                          //查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                          var nd = zTree.getNodeByParam("tId", nodes[i].tId, treeNode);
                          if (nd == null && $.inArray(nodeId, ns) == -1) {
                              ns.push(nodeId);
                          }
                      }
                  }
                  nodesLength += ns.length;
              } else if (treeNode.type == "people" || treeNode.type == "vehicle") { //若勾选的为监控对象
                  var zTree = $.fn.zTree.getZTreeObj("monitorTreeDemo"), nodes = zTree
                      .getCheckedNodes(true), v = "";
                  var nodesLength = 0;
                  //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                  var ns = [];
                  //节点id
                  var nodeId;
                  for (var i = 0; i < nodes.length; i++) {
                      nodeId = nodes[i].id;
                      if (nodes[i].type == "people" || nodes[i].type == "vehicle") {
                          if ($.inArray(nodeId, ns) == -1) {
                              ns.push(nodeId);
                          }
                      }
                  }
                  nodesLength = ns.length + 1;
              }
              if (nodesLength > TREE_MAX_CHILDREN_LENGTH) {
                  layer.msg('最多勾选' + TREE_MAX_CHILDREN_LENGTH + '个监控对象');
                  flag = false;
              }
          }
          if (flag) {
              //若组织节点已被勾选，则是勾选操作，改变勾选操作标识
              if (treeNode.type == "group" && !treeNode.checked) {
                  checkFlag = true;
              }
          }
          return flag;
      },
      onCheckVehicle: function (e, treeId, treeNode) {
          var zTree = $.fn.zTree.getZTreeObj("monitorTreeDemo");
          //若为取消勾选则不展开节点
          if (treeNode.checked) {
              searchFlag = false;
              zTree.expandNode(treeNode, true, true, true, true); // 展开节点
              setTimeout(() => {
                  speedingVehicle.getCheckedNodes();
                  speedingVehicle.validates();
              }, 600);
          }
          crrentSubV = [];
          crrentSubV.push(treeNode.id);
          speedingVehicle.getCheckedNodes();
          speedingVehicle.getCharSelect(zTree);
      },
      zTreeOnNodeCreated: function (event, treeId, treeNode) {
          var id = treeNode.id.toString();
          var list = [];
          if (zTreeIdJson[id] == undefined || zTreeIdJson[id] == null) {
              list = [treeNode.tId];
              zTreeIdJson[id] = list;
          } else {
              zTreeIdJson[id].push(treeNode.tId)
          }
      },
      zTreeOnExpand: function (event, treeId, treeNode) {
          //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
          if (treeNode.type == "group" && !checkFlag) {
              return;
          }
          //初始化勾选操作判断表示
          checkFlag = false;
          var treeObj = $.fn.zTree.getZTreeObj("monitorTreeDemo");
          if (treeNode.type == "group") {
              var url = "/clbs/m/functionconfig/fence/bindfence/putMonitorByGroup";
              json_ajax("post", url, "json", false, {
                  "groupId": treeNode.id,
                  "isChecked": treeNode.checked,
                  "monitorType": "vehicle"
              }, function (data) {
                  var result = data.obj;
                  if (result != null && result != undefined) {
                      $.each(result, function (i) {
                          var pid = i; //获取键值
                          var chNodes = result[i];//获取对应的value
                          if (zTreeIdJson[pid] != undefined) {
                              var parentTid = zTreeIdJson[pid][0];
                              var parentNode = treeObj.getNodeByTId(parentTid);
                              if (parentNode.children === undefined) {
                                  treeObj.addNodes(parentNode, []);
                              }
                          }
                      });
                  }
              })
          }
      },
      getCharSelect: function (treeObj) {
          var nodes = treeObj.getCheckedNodes(true);
          var allNodes = treeObj.getNodes();
          if (nodes.length > 0) {
              $("#monitorGroupSelect").val(allNodes[0].name);
          } else {
              $("#monitorGroupSelect").val("");
          }
      },
      getGroupChild: function (node, assign) { // 递归获取组织及下级组织的分组节点
          var nodes = node.children;
          if (nodes != null && nodes != undefined && nodes.length > 0) {
              for (var i = 0; i < nodes.length; i++) {
                  var node = nodes[i];
                  if (node.type == "assignment") {
                      assign.push(node);
                  } else if (node.type == "group" && node.children != undefined) {
                      speedingVehicle.getGroupChild(node.children, assign);
                  }
              }
          }
      },
      getCheckedNodes: function () {
          var zTree = $.fn.zTree.getZTreeObj("monitorTreeDemo"), nodes = zTree
              .getCheckedNodes(true), v = "", vid = "";
          for (var i = 0, l = nodes.length; i < l; i++) {
              if (nodes[i].type == "vehicle") {
                  v += nodes[i].name + ",";
                  vid += nodes[i].id + ",";
              }
          }
          vehicleId = vid;
      },
      /**
       * 月份日期控件相关方法
       * */
      monthDataInit: function () {
          var now = new Date();
          var year = now.getFullYear();
          var month = now.getMonth() + 1;
          if (month > 12) {
              year += 1;
              month = 1;
          }
          if (month >= 1 && month <= 9) {
              month = "0" + month
          }
          var lastMonthDate = new Date();
          lastMonthDate.setMonth(lastMonthDate.getMonth());
          var lastYear = lastMonthDate.getFullYear();
          var lastMonthText = lastMonthDate.getMonth() + 1;
          if (lastMonthText >= 1 && lastMonthText <= 9) {
              lastMonthText = "0" + lastMonthText
          }
          var lastMonth = lastYear + '-' + lastMonthText;
          var maxMonth = year + '-' + (month) + "-01 00:00:00";
          $('#timeInterval').val(lastMonth);
          laydate.render({
              elem: '#timeInterval'
              , type: 'month'
              , max: maxMonth
              , btns: ['clear', 'confirm']
              , ready: function (date) {
                  $("#layui-laydate2").off('click').on('click', '.laydate-month-list li', function () {
                      $("#layui-laydate2").remove();
                  });
              }
              // 点击月份立即改变input值
              , change: function (value, dates, edate) {
                  var final;

                  // 获取选择的年月
                  var year = dates.year;
                  var month = dates.month;
                  // 获取当前实际的年月
                  var getNow = speedingVehicle.getNow;
                  var myDate = new Date();
                  var nowYear = myDate.getFullYear();
                  var nowMonth = myDate.getMonth() + 1;

                  if (year > nowYear) {
                      final = speedingVehicle.getYesterMonth(1);
                  } else if (year == nowYear) {
                      if (month > nowMonth) {
                          final = speedingVehicle.getYesterMonth(1)
                      } else {
                          final = value;
                      }
                  } else {
                      final = value;
                  }
                  $('#timeInterval').val(final);
              },
          });
      },
      getMonth: function (month) {
          if (month >= 1 && month <= 9) {
              return "0" + month
          }
      },
      getNow: function (s) {
          return s < 10 ? "0" + s : s;
      },
      getYesterMonth: function (del, needSecond) {
          var getNow = speedingVehicle.getNow;
          var myDate = new Date();
          var year = myDate.getFullYear();
          var month = myDate.getMonth() + 1;
          var date = myDate.getDate();
          var h = myDate.getHours();
          var m = myDate.getMinutes();
          var s = myDate.getSeconds();
          var now;
          if (month == 1) {
              year = year - 1;
              month = 12;
          } else {
              month = month - del; // 几月前
          }

          if (needSecond) {
              now = year + "-" + getNow(month) + "-" + getNow(date) + " " + getNow(h) + ":" + getNow(m) + ":" + getNow(s);
          } else {
              now = year + "-" + getNow(month)
          }
          return now;
      },
  };
  $(function () {
      speedingVehicle.init();
      speedingVehicle.getTable('#monitorDataTable');

      $("#monitorGroupSelect").bind("click", showMenuContent);
      $("#exportMonitor").bind("click", speedingVehicle.exportMonitor);//导出
      $("#monitorDetailExport").bind("click", speedingVehicle.exportMonitorDeatil);//导出
      $("#refreshMonitorTable").bind("click", speedingVehicle.refreshTable);

      $('input').inputClear().on('onClearEvent', function (e, data) {
          var id = data.id;
          if (id == 'monitorGroupSelect') {
              var param = $("#monitorGroupSelect").val();
              speedingVehicle.searchVehicleTree(param);
          }
      });
      // 监控对象树模糊查询
      var inputChange;
      $("#monitorGroupSelect").on('input propertychange', function (value) {
          if (inputChange !== undefined) {
              clearTimeout(inputChange);
          }
          if (!(window.ActiveXObject || "ActiveXObject" in window)) {
              searchFlag = true;
          }
          inputChange = setTimeout(function () {
              if (searchFlag) {
                  var param = $("#monitorGroupSelect").val();
                  speedingVehicle.searchVehicleTree(param);
              }
              searchFlag = true;
          }, 500);
      });
      $('#queryType').on('change', function () {
          var param = $("#monitorGroupSelect").val();
          speedingVehicle.searchVehicleTree(param);
      });
  })
}(window, $))