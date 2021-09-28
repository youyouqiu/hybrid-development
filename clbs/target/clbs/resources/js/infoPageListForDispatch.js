// 对讲信息列表

(function (window, $) {
  var addFlag = false; //用于新加数据列表第一行变色效果
  var menu_text = '';
  infoinputList = {
    init: function () {
      $('[data-toggle=\'tooltip\']').tooltip();
      infoinputList.infoTableInit();
      publicFun.displayTH();
    },
    // 显示隐藏列
    showMenuText: function () {
      // 列筛选
      var table = $('#dataTable tr th:gt(1)');
      menu_text += '<li><label><input type="checkbox" checked="checked" class="toggle-vis" data-column="' + parseInt(2) + '" disabled />' + table[0].innerHTML + '</label></li>';
      for (var i = 1; i < table.length; i++) {
        menu_text += '<li><label><input type="checkbox" checked="checked" class="toggle-vis" data-column="' + parseInt(i + 2) + '" />' + table[i].innerHTML + '</label></li>';
      }
      $('#Ul-menu-text').html(menu_text);
      // 显示隐藏列
      $('.toggle-vis').on('change', function (e) {
        var column = myDataTable.column($(this).attr('data-column'));
        column.visible(!column.visible());
        $('#customizeColumns').click();
        publicFun.displayTH();
      });
    },
    // 创建表格
    infoTableInit: function () {
      // 表格列定义
      var columnDefs = [
        {
          // 第一列，用来显示序号
          'searchable': false,
          'orderable': false,
          'targets': 0
        }
      ];
      var columns = [
        {
          // 第一列，用来显示序号
          'data': null,
          'class': 'text-center'
        },
        {
          // 第二列，checkbox
          'data': null,
          'class': 'text-center',
          render: function (data, type, row) {
            return '<input type="checkbox" name="subChk" value="' + row.configId + '" /> ';
          }
        },
        {
          // 第三列，操作按钮列
          'data': null,
          'class': 'text-center',
          render: function (data, type, row) {
            var editUrlPath = myTable.editUrl + row.configId + '.gsp'; // 修改对讲信息地址
            var friendSetPath = '/clbs/talkback/inconfig/infoinput/getAddFriendsPage_' + row.monitorType + '_' + row.configId; // 对讲信息好友设置地址

            // 修改按钮
            var result = '';
            result += '<button href="' + editUrlPath + '" data-target="#commonSmWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&nbsp;';
            if (!row.configId) {
              result += '<button disabled class="editBtn btn-default deleteButton" type="button"><i class="fa fa-edit"></i>生成</button>&nbsp;';
            } else {
              result += '<button onclick="infoinputList.generatorIntercomInfo(\'' + row.configId + '\')" class="editBtn editBtn-info" type="button"><i class="fa fa-edit"></i>生成</button>&nbsp;';
            }

            // 好友设置按钮
            // 验证是否对讲对象, 并且好友数量大于0
            // if (row.userId && row.maxFriendNum) {
            if (row.userId) {
              result += '<button href="' + friendSetPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa faEdit"></i>好友设置</button>&nbsp;';
            } else {
              result += '<button disabled class="editBtn btn-default deleteButton" type="button"><i class="fa faEdit"></i>好友设置</button>&nbsp;';
            }

            // 解绑按钮
            result += '<button type="button" onclick="infoinputList.deleteIntercomInfo(\'' + row.configId + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>解绑</button>';
            return result;
          }
        },
        {
          // 第四列，监控对象
          'data': 'monitorName',
          'class': 'text-center'
        },
        {
          // 监控对象类型
          'data': 'monitorType',
          'class': 'text-center',
          render: function (data) {
            if (data === '0') {
              return '车';
            } else if (data === '1') {
              return '人';
            } else if (data === '2') {
              return '物';
            } else {
              return '';
            }
          }
        },
        {
          // 生成状态
          'data': 'status',
          'class': 'text-center',
          render: function (data) {
            if (data === 0) {
              return '未生成';
            } else if (data === 1) {
              return '已生成';
            } else if (data === 2) {
              return '失败';
            } else {
              return '';
            }
          }
        },
        {
          // 所属企业
          'data': 'groupName',
          'class': 'text-center'
        },
        {
          // 群组
          'data': 'assignmentName',
          'class': 'text-center'
        },
        {
          // 终端手机号
          'data': 'simcardNumber',
          'class': 'text-center'
        },
        {
          // 对讲设备标识
          'data': 'intercomDeviceId',
          'class': 'text-center'
        },
        {
          // 原始机型
          'data': 'modelId',
          'class': 'text-center'
        },
        {
          // 对讲机型
          'data': 'intercomModelName',
          'class': 'text-center'
        },
        {
          // 优先级
          'data': 'priority',
          'class': 'text-center'
        },
        {
          // 客户代码
          'data': 'customerCode',
          'class': 'text-center'
        },
        {
          // 个呼号码
          'data': 'number',
          'class': 'text-center'
        },
        {
          'data': 'maxGroupNum',
          'class': 'text-center',
          render: function (data, type, row) {
            var currentGroupNum = row.currentGroupNum ? row.currentGroupNum : 0;
            var maxGroupNum = data ? data : 0;
            return currentGroupNum.toString() + '/' + maxGroupNum.toString();
          }
        }
      ];

      // 非admin账户不显示录音状态字段
      if ($('#isAdmin').val() !== 'false') {
        columns.push({
          // 录音状态
          'data': 'recordEnable',
          'class': 'text-center',
          render: function (data, type, row) {
            if (+data === 0) {
              if (!row.userId) {
                return '<div class="mySwitch openRecordEnable disabledRecord">已关闭</div>';
              } else {
                return '<div class="mySwitch openRecordEnable" onclick="infoinputList.updateRecordStatus(\'' + row.configId + '\', \'' + row.recordEnable + '\')">已关闭</div>';
              }
            } else {
              if (!row.userId) {
                return '<div class="mySwitch openRecordEnable disabledRecord">已开启</div>';
              } else {
                return '<div class="mySwitch" onclick="infoinputList.updateRecordStatus(\'' + row.configId + '\', \'' + row.recordEnable + '\')">已开启</div>';
              }
            }
          }
        });
      }

      // 全选
      $('#checkAll').click(function () {
        $('input[name=\'subChk\']').prop('checked', this.checked);
      });
      // 单选
      var subChk = $('input[name=\'subChk\']');
      subChk.click(function () {
        $('#checkAll').prop('checked', subChk.length === subChk.filter(':checked').length);
      });
      // 批量删除
      $('#del_model').click(function () {
        // 判断是否至少选择一项
        var chechedNum = $('input[name=\'subChk\']:checked').length;
        if (chechedNum === 0) {
          layer.msg(selectItem, {move: false});
          return;
        }
        var checkedList = [];
        $('input[name=\'subChk\']:checked').each(function () {
          checkedList.push($(this).val());
        });
        myTable.deleteItems({'deltems': checkedList.toString()});
      });
      // ajax参数
      var ajaxDataParamFun = function (d) {
        d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
        // d.groupName = selectTreeId;
        // d.groupType = selectTreeType;
      };
      //表格setting
      var setting = {
        detailUrl: '/clbs/m/infoconfig/infoinput/getConfigDetails_',
        listUrl: '/clbs/talkback/inconfig/infoinput/list',
        editUrl: '/clbs/talkback/inconfig/infoinput/edit_',
        deleteUrl: '/clbs/talkback/inconfig/intercomObject/delete',
        deletemoreUrl: '/clbs/talkback/inconfig/intercomObject/delete',
        enableUrl: '/clbs/c/user/enable_',
        disableUrl: '/clbs/c/user/disable_',
        columnDefs: columnDefs, //表格列定义
        columns: columns, //表格列
        dataTableDiv: 'dataTable', //表格
        drawCallbackFun: function () {
          if (addFlag) {
            var curTr = $('#dataTable tbody tr:first-child');
            curTr.addClass('highTr');
            setTimeout(function () {
              curTr.removeClass('highTr');
              addFlag = false;
            }, 1000);
          }
        },
        ajaxDataParamFun: ajaxDataParamFun, //ajax参数
        pageable: true, //是否分页
        showIndexColumn: true, //是否显示第一列的索引列
        enabledChange: true
      };

      // 创建表格
      myTable = new TG_Tabel.createNew(setting);
      // 表格初始化
      myTable.init();
    },
    // 刷新table列表
    refreshTable: function () {
      $('#simpleQueryParam').val('');
      myTable.requestData();
    },
    getPeripheralsCallback: function (data) {
      if (data != null && data != undefined && data != '') {
        if (data.success) {
          $('#detailShow').modal('show');
          $('#detailContent').html(data.obj.pname == '' ? '该监控对象目前还没有绑定外设哦！' : data.obj.pname);
        } else {

        }
      }
    },
    // 生成对讲对象
    generatorIntercomInfo: function (id) {
      var url = '/clbs/talkback/inconfig/intercomObject/generatorIntercomInfo';
      json_ajax('POST', url, 'json', false, {
        'configId': id
      }, function (data) {
        if (data.success) {
          if (data.msg) {
            layer.msg(data.msg);
          }
          myTable.refresh();
        } else {
          layer.msg(data.msg || '生成失败');
        }
      });
    },
    // 批量生成对讲对象
    delMooreIntercomInfo: function () {
      var $subChk = $('input[name=\'subChk\']:checked');

      // 判断是否至少已选择一项
      var chechedNum = $subChk.length;
      if (chechedNum === 0) {
        layer.msg(selectItem, {move: false});
        return;
      }

      var checkedList = [];
      $subChk.each(function () {
        checkedList.push($(this).val());
      });

      var url = '/clbs/talkback/inconfig/intercomObject/generatorIntercomInfoBatch';
      json_ajax('POST', url, 'json', false, {
        'configIds': checkedList.toString()
      }, function (data) {
        if (data.success) {
          if (data.msg) {
            layer.msg(data.msg);
          }
          myTable.refresh();
        } else if (data.msg) {
          layer.msg(data.msg);
        } else {
          layer.msg(data.msg || '生成失败');
        }
      });
    },
    // 解绑对讲对象
    deleteIntercomInfo: function (id) {
      layer.confirm('确定要解绑对讲对象吗！', {icon: 3, btn: ['确定', '取消']}, function () {
        var url = myTable.deleteUrl;
        json_ajax('POST', url, 'json', false, {
          'configIds': id
        }, function (data) {
          layer.closeAll();
          if (data.success) {
            myTable.refresh();
          } else {
            layer.msg('解绑失败');
          }
        });
      });
    },
    // 批量解绑对讲对象
    delModelClick: function () {
      var $subChk = $('input[name=\'subChk\']:checked');
      //判断是否至少选择一项
      var chechedNum = $subChk.length;
      if (chechedNum === 0) {
        layer.msg(selectItem, {move: false});
        return;
      }

      var checkedList = [];
      $subChk.each(function () {
        checkedList.push($(this).val());
      });

      myTable.deleteItems({
        'configIds': checkedList.toString()
      });
    },
    // 修改录音状态
    updateRecordStatus: function (id, recordEnable) {
      layer.confirm('确认修改录音状态?', {btn: ['确定', '取消'], icon: 7, title: '操作确认'}, function () {
        var url = '/clbs/talkback/inconfig/intercomObject/updateRecordStatus';
        json_ajax('POST', url, 'json', false, {
          'configId': id,
          'recordEnable': +recordEnable === 0 ? 1 : 0
        }, function (data) {
          layer.closeAll();
          if (data.success) {
            myTable.refresh();
          } else {
            layer.msg('修改录音状态失败');
          }
        });
      });
    }
  };

  // 公共方法
  var searchFlag = true;
  var publicFun = {
    // 群组树
    groupTreeInit: function (type) {
      var setting = {
        async: {
          url: '/clbs/talkback/basicinfo/enterprise/assignment/assignmentTree',
          type: 'post',
          enable: true,
          autoParam: ['id'],
          contentType: 'application/json',
          dataType: 'json',
          dataFilter: publicFun.ajaxDataFilter
        },
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
          dblClickExpand: false
        },
        data: {
          simpleData: {
            enable: true
          }
        },
        callback: {
          beforeClick: publicFun.beforeClick,
          onClick: publicFun.onClick,
          onCheck: publicFun.onClick,
          onAsyncSuccess: publicFun.onAsyncSuccess
        }
      };

      $.fn.zTree.destroy();
      $.fn.zTree.init($('#quickTreeDemo'), setting, null);
    },
    onAsyncSuccess: function (event, treeId, treeNode, msg) {
      // 初始化群组节点选中数组
      var nodes1 = $.fn.zTree.getZTreeObj(treeId).getCheckedNodes(true);
      for (var i = 0; i < nodes1.length; i++) {
        if (nodes1[i].type === 'assignment') {
          checkedAssginment.push(nodes1[i]);
        }
      }
    },
    ajaxDataFilter: function (treeId, parentNode, responseData) {
      var flag = true;
      if (responseData) {
        for (var i = 0; i < responseData.length; i++) {
          if (flag && responseData[i].type === 'assignment' && searchFlag) {
            if (responseData[i].canCheck < 100) {
              responseData[i].checked = true;
              $('#quickGroupId').val(responseData[i].name);
              $('#quickCitySelidVal').val(responseData[i].id);
              flag = false;
            }
          }
          responseData[i].open = true;
        }
      }
      return responseData;
    },
    //树点击之前事件
    beforeClick: function (treeId, treeNode) {
      var zTree = $.fn.zTree.getZTreeObj(treeId);
      zTree.checkNode(treeNode, !treeNode.checked, treeNode, true);
      return false;
    },
    //树点击事件
    onClick: function (e, treeId, treeNode) {
      if (treeId !== undefined) { //快速录入与极速录入
        var zTree = $.fn.zTree.getZTreeObj(treeId);
        var v = '';
        var t = '';
        if (treeNode.checked) { // 勾选操作才进行验证，取消勾选不进行验证
          // 获取群组和企业ID信息
          var caId;
          // 定义群组和企业唯一标识
          var caIdentification;
          var nodes = [];
          if (treeNode.type === 'assignment') { // 为群组节点直接校验
            caIdentification = 1;
            caId = treeNode.id;
            nodes.push(treeNode);
          } else if (treeNode.type === 'group') { // 为企业节点获取勾选节点然后去除校验过的节点
            caIdentification = 2;
            caId = treeNode.id;
            nodes = zTree.getCheckedNodes(true);

            var curArr = [];
            curArr = checkedAssginment;
            for (var h = 0; h < curArr.length; h++) {
              nodes.remove(curArr[h]);
            }
          }
          //获取还可录入的群组id
          publicFun.getAllAssignmentVehicleNumber(caId, caIdentification);
          nodes.sort(function compare(a, b) {
            return a.id - b.id;
          });

          var amtNames = ''; // 车辆数超过100的群组
          for (var i = 0, l = nodes.length; i < l; i++) {
            if (nodes[i].type === 'assignment') { // 选择的是群组，才组装值
              if (!publicFun.checkMaxVehicleCountOfAssignment(nodes[i].id)) {
                nodes[i].checked = false;
                amtNames += nodes[i].name + ',';
              }
            }
          }
          // 判断系统是否出问题
          if (systemErrorFlag) {
            layer.msg(systemError, {
              time: 1500
            });
            return;
          }
          if (amtNames.length > 0) {
            amtNames = amtNames.substr(0, amtNames.length - 1);
            layer.msg('【分组:' + amtNames + '】' + assignmentMaxCarNum);
          } else {
            publicFun.clearErrorMsg();
          }
        }

        // 组装校验通过的值，初始化节点选中数组
        checkedAssginment = [];
        var checkedNodes = zTree.getCheckedNodes(true);
        for (var i = 0; i < checkedNodes.length; i++) {
          if (checkedNodes[i].type === 'assignment') {
            t += checkedNodes[i].name + ',';
            v += checkedNodes[i].id + ';';

            checkedAssginment.push(checkedNodes[i]);
          }
        }

        if (v.length > 0) v = v.substring(0, v.length - 1);
        if (t.length > 0) t = t.substring(0, t.length - 1);
        $('#quickGroupId').val(t);
        $('#quickCitySelidVal').val(v);
      }
    },
    // 获取还可录入的群组id(校验群组车辆上限用)
    getAllAssignmentVehicleNumber: function (cdId, identifier) {
      if (cdId !== '' && identifier !== '') {
        $.ajax({
          type: 'POST',
          url: '/clbs/m/infoconfig/infoinput/getAssignmentCount',
          dataType: 'json',
          data: {'id': cdId, 'type': identifier},
          async: false,
          success: function (data) {
            if (data.success) {
              ais = data.obj.ais;
            } else if (data.msg) {
              layer.msg(data.msg);
              systemErrorFlag = true;
            }
          },
          error: function () {
            layer.msg(systemError, {
              time: 1500
            });
            systemErrorFlag = true;
          }
        });
      }
    },
    // 校验当前群组下的车辆数是否已经达到最大值（最大值目前设定为：100台）
    checkSweepMaxVehicleCountOfAssignment: function (assignmentId, assignmentName) {
      var b = true;
      $.ajax({
        type: 'POST',
        url: '/clbs/m/infoconfig/infoinput/checkMaxVehicleCount',
        data: {'assignmentId': assignmentId, 'assignmentName': assignmentName},
        dataType: 'json',
        async: false,
        success: function (data) {
          if (data.success) {
            b = data.obj.success;
          } else if (data.msg) {
            layer.msg(data.msg);
          }
        },
        error: function () {
          layer.msg(systemError, {
            time: 1500
          });
          b = false;
          systemErrorFlag = true;
        }
      });
      return b;
    },
    // 校验当前群组下的车辆数是否已经达到最大值（最大值目前设定为：100台）
    checkMaxVehicleCountOfAssignment: function (assignmentId) {
      var b = true;
      if ($.inArray(assignmentId, ais) === -1) {
        b = false;
      }
      return b;
    },
    // 校验单个群组下的车辆数是否已经达到最大值（主要用于默认群组勾选校验）
    checkSingleMaxVehicle: function (assignmentId, assignmentName) {
      var b = false;
      $.ajax({
        type: 'POST',
        url: '/clbs/m/infoconfig/infoinput/checkMaxVehicleCount',
        data: {'assignmentId': assignmentId, 'assignmentName': assignmentName},
        dataType: 'json',
        async: false,
        success: function (data) {
          if (data.success) {
            b = data.obj.success;
          } else if (data.msg) {
            layer.msg(data.msg);
          }
        },
        error: function () {
          layer.msg(systemError, {
            time: 1500
          });
          b = false;
          systemErrorFlag = true;
        }
      });
      return b;
    },
    //错误提示信息隐藏
    hideErrorMsg: function () {
      $('#error_label').hide();
    },
    // 清除错误信息
    clearErrorMsg: function () {
      $('label.error').hide();
    },
    /**
     * 非admin用户隐藏是否录音列
     */
    displayTH: function () {
      if ($('#isAdmin').val() === 'false') {
        $th = $('#dataTable').find('thead tr th:last-child');
        if ($th.text() === '录音状态') {
          $('#dataTable').find('thead tr th:last-child').remove();
        }
      }
    }
  };


  /**
   * 快速录入
   */
  var quickMonitorType = 1; // 0: 选择车；1: 选择人；2: 选择物
  // 第一次进页面默认查询的数据
  var vehicleInfoList = []; // 车数据集
  var peopleInfoList = []; // 人数据集
  var thingInfoList = []; // 物数据集
  var originalModelList = []; // 原始机型数据集
  var simCardInfoList = []; // 终端手机号信息集合
  var ais = []; // 还能存入的群组id
  var checkedAssginment = []; // 信息录入群组字段已被选中的数据
  var orgId = '';
  var orgName = '';
  var flag1 = false; // 选择还是录入的车牌号
  var flag2 = false; // 选择还是录入的原始机型值
  var flag3 = true; // 选择还是录入的终端手机号
  var hasFlag = true, hasFlag1 = true; // 是否有该唯一标识
  var quickRefresh = true; // 快速录入信息是否刷新
  var infoFastInput = {
    //初始化文件树
    init: function () {
      infoFastInput.getInfoData();
    },
    getInfoData: function () {
      var urlList = '/clbs/talkback/inconfig/infoFastInput/add';
      var parameterList = {'id': ''};
      json_ajax('POST', urlList, 'json', true, parameterList, infoFastInput.InitCallback);
    },
    InitCallback: function (data) {
      if (data.success) {
        var datas = data.obj;
        vehicleInfoList = datas.vehicleInfoList;
        peopleInfoList = datas.peopleInfoList;
        thingInfoList = datas.thingInfoList;
        simCardInfoList = datas.simCardInfoList;
        originalModelList = datas.originalModelList;
        orgId = datas.orgId;
        orgName = datas.orgName;

        infoFastInput.getCallbackList();
      } else if (data.msg) {
        layer.msg(data.msg);
      }

      // 请求成功关闭加载动画
      setTimeout(function () {
        layer.closeAll('loading');
      }, 200);
    },
    inputClick: function () {
      var $speedDevices = $('#speedDevices');
      var value = $speedDevices.val();
      var flag = false;
      $('#searchDevices-id li').each(function () {
        var name = $(this).text();
        if (name.indexOf(value) === -1) {
          $(this).hide();
          $('#searchDevices-id').hide();
        } else {
          flag = true;
          $(this).css('display', 'block');
        }
        if (flag) {
          $('#searchDevices-id').show();
        }
      });
      var width = $speedDevices.parent('div').width();
      $('.searchDevices-div ul').css('width', width + 'px');
    },
    menuClick: function () {
      // flag4 = true;
      hasFlag = true;
      $('#speedDevices').next('label').hide();
      var device = $(this).data('device');
      var car = $(this).attr('data-car');
      var sim = $(this).attr('data-sim');
      var deviceType = $(this).attr('data-deviceType');
      var manufacturerId = $(this).attr('data-manufacturerId');
      var deviceModelNumber = $(this).attr('data-deviceModelNumber');
      var provinceId = $(this).attr('data-provinceId');
      var cityId = $(this).attr('data-cityId');
      var plateColor = $(this).attr('data-plateColor');
      var $speedSims = $('#speedSims');
      // 限制输入
      if (+deviceType === 0 || +deviceType === 1 || (+deviceType >= 8 && +deviceType <= 18)) {
        // 设置sim不可修改
        $speedSims.prop('disabled', true).css({
          'cursor': 'not-allowed',
          'background': 'rgb(238, 238, 238)'
        });
        $('#sim_searchDevice').removeAttr('disabled');
        $speedSims.unbind();//$("#speedSims").prop("disabled", true).css('cursor', 'not-allowed');
        $('#sim_searchDevice').prop('disabled', true);
        //设置终端号可修改
        $('#oneDevices').prop('disabled', false).css({'cursor': 'text', 'background': 'rgb(255, 255, 255)'});
        $('#searchOneDevices').prop('disabled', false);
      } else {
        //设置终端号不可修改
        $('#oneDevices').prop('disabled', true).css({
          'cursor': 'not-allowed',
          'background': 'rgb(238, 238, 238)'
        });
        $('#searchOneDevices').prop('disabled', true);

        //设置sim可修改
        $speedSims.removeAttr('disabled').css({'cursor': 'text', 'background': 'rgb(255, 255, 255)'});
        $speedSims.bind('click', infoFastInput.searchList);
        $('#sim_searchDevice').prop('disabled', false);
        infoFastInput.getsiminfoset();
      }
      $('input').inputClear();
    },
    checkIsNull: function (data) {
      return data !== 'undefined' && data !== 'null' && data !== '';
    },
    getCallbackList: function () {
      // 组织数据初始化
      $('#groupId').attr('value', orgId);
      $('#groupName').attr('value', orgName);

      // 监控对象
      var dataList = {
        value: []
      };

      // 快速录入 -> 选择监控对象类型逻辑处理
      // 0: 选择车；1: 选择人；2: 选择物
      if (+quickMonitorType === 0) {
        var i = vehicleInfoList.length;
        while (i--) {
          dataList.value.push({
            name: vehicleInfoList[i].brand ? vehicleInfoList[i].brand : '',
            id: vehicleInfoList[i].id,
            type: vehicleInfoList[i].plateColor
          });
        }
      } else if (+quickMonitorType === 1) {
        var j = peopleInfoList.length;
        while (j--) {
          dataList.value.push({
            name: peopleInfoList[j].brand ? peopleInfoList[j].brand : '',
            id: peopleInfoList[j].id,
            type: peopleInfoList[j].monitorType
          });
        }
      } else if (+quickMonitorType === 2) {
        var l = thingInfoList.length;
        while (l--) {
          dataList.value.push({
            name: thingInfoList[l].brand ? thingInfoList[l].brand : '',
            id: thingInfoList[l].id,
            type: thingInfoList[l].monitorType
          });
        }
      }

      // 终端手机号
      var simDataList = {value: []}, o = simCardInfoList.length;
      while (o--) {
        simDataList.value.push({
          name: simCardInfoList[o].simcardNumber,
          id: simCardInfoList[o].id
        });
      }

      // 对讲设备标识
      var originalModelDataList = {value: []}, k = originalModelList.length;
      while (k--) {
        originalModelDataList.value.push({
          name: originalModelList[k].modelId,
          id: originalModelList[k].index
        });
      }

      // 快速录入（监控对象、对讲设备标识等信息初始化）
      if (quickRefresh) {
        // 终端手机号数据初始化
        $('#quickSimsContainer').dropdown({
          data: simDataList.value,
          pageCount: 50,
          listItemHeight: 31,
          onDataRequestSuccess: function () {
            $('#quickSims').removeAttr('disabled');
          },
          onSetSelectValue: function (e, keyword) {
            $('#quickSimVal').attr('value', keyword.id);
            infoFastInput.hideErrorMsg();
            infoFastInput.checkIsBound('sims', keyword.name); // 校验当前终端手机号是否已经被绑定，两个人同时操作的时候可能会出现
            flag3 = true;
            $('#quickSims').closest('.form-group').find('.dropdown-menu').hide();
          },
          onUnsetSelectValue: function () {
            flag3 = false;
          }
        });

        // 对讲设备标识 -> 原始机型数据初始化
        var $quickOriginalModel = $('#quickOriginalModel');
        var $quickOriginalModelVal = $('#quickOriginalModelVal');
        if (originalModelDataList.value.length) {
          $quickOriginalModel.attr('value', originalModelDataList.value[0].name);
          $quickOriginalModelVal.attr('value', originalModelDataList.value[0].id);
        }
        $quickOriginalModel.bsSuggest('destroy'); // 销毁事件
        $quickOriginalModel.bsSuggest({
          indexId: 1, // 每组数据的第几个数据，作为input输入框的 data-id，设为 -1 且 idField 为空则不设置此值
          indexKey: 0, // 每组数据的第几个数据，作为input输入框的内容
          data: originalModelDataList, // 提示所用的数据，注意格式
          effectiveFields: ['name'] // 有效显示于列表中的字段，非有效字段都会过滤，默认全部有效。
        }).on('onSetSelectValue', function (e, keyword, data) {
          $quickOriginalModelVal.attr('value', keyword.id);
          $quickOriginalModel.attr('value', keyword.key);
          infoFastInput.checkIsBound('devices', keyword.key); // 校验当前对讲对象值是否已经被绑定，两个人同时操作的时候可能会出现
          infoFastInput.hideErrorMsg();
          flag2 = true;
          $quickOriginalModel.closest('.form-group').find('.dropdown-menu').hide();
          $('#quickEntryForm .input-group input').attr('style', 'background-color:#ffffff !important;');
        }).on('onUnsetSelectValue', function () {
          flag2 = false;
        }).on('focus', function () {
          $(this).siblings('i.delIcon').remove();
        });

        var $quickBrands = $('#quickBrands');
        $quickBrands.bsSuggest('destroy'); // 销毁事件
        $quickBrands.bsSuggest({
          indexId: 1,  // 每组数据的第几个数据，作为input输入框的 data-id，设为 -1 且 idField 为空则不设置此值
          indexKey: 0, // 每组数据的第几个数据，作为input输入框的内容
          data: dataList, // 提示所用的数据，注意格式
          effectiveFields: ['name'] // 有效显示于列表中的字段，非有效字段都会过滤，默认全部有效。
        }).on('onDataRequestSuccess', function (e, result) {
          $quickBrands.removeAttr('disabled');
        }).on('onSetSelectValue', function (e, keyword) {
          $('#quickBrandVal').attr('value', keyword.id);
          infoFastInput.checkIsBound('brands', keyword.key); // 校验当前车牌号是否已经被绑定，两个人同时操作的时候可能会出现
          infoFastInput.hideErrorMsg();
          flag1 = true;
          $quickBrands.closest('.form-group').find('.dropdown-menu').hide();
          $('#quickEntryForm .input-group input').attr('style', 'background-color:#ffffff !important;');
        }).on('onUnsetSelectValue', function () {
          flag1 = false;
        });
      }

      $('input').inputClear().on('onClearEvent', function (e, data) {
        var id = data.id;

        setTimeout(function () {
          $('#' + id).focus();
        }, 20);
      });
    },
    // 文本框复制事件处理
    inputOnPaste: function (eleId) {
      if (eleId === 'quickBrands') {
        flag1 = false;
        $('#quickBrandVal').attr('value', '');
      }
      if (eleId === 'quickSims') {
        flag3 = false;
        $('#quickSimVal').attr('value', '');
      }
    },
    //ajax请求回调函数
    getCallback: function (data) {
      if (data.success) {
        for (var i = 0; i < data.obj.vehicleInfoList.length; i++)
          $('#quickBrands').append('<option value=' + data.obj.vehicleInfoList[i].id + '>' + data.obj.vehicleInfoList[i].brand + '</option>');
        for (var i = 0; i < data.obj.simcardInfoList.length; i++)
          $('#quickSims').append('<option value=' + data.obj.simcardInfoList[i].id + '>' + data.obj.simcardInfoList[i].simcardNumber + '</option>');
        $('.group_select').css('display', '');
      } else if (data.msg) {
        layer.msg(data.msg);
      }
    },
    // 提交事件
    doSubmits: function () {
      // 检查终端手机号下拉菜单的值
      if (
        !infoFastInput.checkIsEmpty('quickSims', simNumberNull) ||
        (!flag3 && !infoFastInput.check_sim('quickSims')) ||
        !infoFastInput.checkRightSim('quickSims')
      ) {
        return;
      }

      if (infoFastInput.checkIsBound('sims', $('#quickSims').val())) {
        return;
      }

      // 检查对讲设备标识 -> 原始机型
      if (!infoFastInput.checkIsEmpty('quickOriginalModel', originalModelNull)) {
        return;
      }
      // 检查对讲设备标识
      if (!infoFastInput.checkIsEmpty('equipmentIdentity', equipmentIdentityNull)) {
        return;
      }
      if (!infoFastInput.checkEquipmentIdentityLegality()) {
        return;
      }

      // 检查设备密码
      if (!infoFastInput.checkIsEmpty('quickDevicePassword', quickDevicePasswordNull)) {
        return;
      }
      if (!infoFastInput.checkDevicePasswordLegality()) {
        return;
      }

      // 检查监控对象下拉菜单的值
      if (+quickMonitorType === 0) {
        if ((!flag1 && !infoFastInput.check_brand()) || !infoFastInput.checkRightBrand('quickBrands')) {
          return;
        }
      } else if (+quickMonitorType === 1) {
        if ((!flag1 && !infoFastInput.check_people_number()) || !infoFastInput.checkRightPeopleNumber()) {
          return;
        }
      } else if (+quickMonitorType === 2) {
        if ((!flag1 && !infoFastInput.check_thing()) || !infoFastInput.checkRightBrand('quickBrands')) {
          return;
        }
      }

      if (infoFastInput.checkIsBound('brands', $('#quickBrands').val())) {
        return;
      }

      if (infoFastInput.validate_addForm1()) {
        // 检查群组
        if ($('#quickCitySelidVal').val() === '') {
          infoFastInput.showErrorMsg('请选择群组', 'quickGroupId');
          return;
        }

        infoFastInput.hideErrorMsg();
        $('#quickSubmits').prop('disabled', true);
        $('#quickEntryForm').ajaxSubmit(function (data) {
          data = JSON.parse(data);

          if ((data.success && data.success === 'false') || !data.success) {
            layer.msg(data.msg || '录入失败');
          } else if (data.msg) {
            quickRefresh = true;
            infoFastInput.getInfoData(); // 重新加载监控对象, 终端手机号等信息
            infoFastInput.clearQuickInfo();
            addFlag = true;
            myTable.requestData();
          }
          infoFastInput.refreshToken();
          $('#quickSubmits').removeAttr('disabled');
        });
      }
    },
    refreshToken: function () {
      var url = '/clbs/m/basicinfo/enterprise/brand/generateFormToken';
      json_ajax('POST', url, 'json', false, null, function (data) {
        $('.avoidRepeatSubmitToken').val(data.msg);
      });
    },
    // 快速录入完成后,清空相应信息
    clearQuickInfo: function () {
      $('#quickBrands').val('').css('backgroundColor', '#fafafa');
      $('#quickBrandVal').val('');
      $('#quickSims').val('').css('backgroundColor', '#fafafa');
      $('#quickSimVal').val('');
    },
    //快速录入验证
    validate_addForm1: function () {
      return $('#quickEntryForm').validate({
        rules: {
          deviceType: {
            required: true
          },
          groupid: {
            required: true
          }
        },
        messages: {
          deviceType: {
            required: deviceDeviceTypeNull
          },
          groupid: {
            required: assignmentNameNull
          }
        }
      }).form();
    },
    /**
     * 校验终端手机号是否填写规范或者回车时不小心输入了异常字符
     * @param id
     * @returns {*|boolean}
     */
    checkRightSim: function (id) {
      /* var reg = /^((13[0-9]{1})|(14[5,7,9]{1})|(15[^4]{1})|(166)|(18[0-9]{1})|(19[8-9]{1})|(17[0,1,3,5,6,7,8]{1}))+\d{8}$/;
       var reg1 = /^((\d{3,4}-\d{7,9})|([1-9]{1}\d{6,12}))$/g;*/
      var reg = /^\d{7,20}$/g;
      return infoFastInput.checkIsLegal(id, reg, null, simNumberError);
    },
    // 显示错误提示信息
    showErrorMsg: function (msg, inputId) {
      var $error_label = $('#error_label');
      if ($error_label.is(':hidden')) {
        $error_label.text(msg);
        $error_label.insertAfter($('#' + inputId));
        $error_label.show();
      } else {
        $error_label.is(':hidden');
      }
    },
    //错误提示信息隐藏
    hideErrorMsg: function () {
      $('#error_label').hide();
    },
    /**
     * 校验终端手机号是否已存在
     * @param eleId {string} DOM元素的id属性值
     * @returns {boolean} 是否存在（true:存在；false:不存在）
     */
    checkSIMisExist: function (eleId) {
      var tempFlag = true;
      $.ajax({
        type: 'POST',
        url: '/clbs/m/basicinfo/equipment/simcard/repetition',
        data: {'simcardNumber': $('#' + eleId).val()},
        dataType: 'json',
        async: false,
        success: function (data) { // 注意：此处接口直接返回了一个boolean值，即data的类型为boolean
          if (!data) {
            infoFastInput.showErrorMsg(simNumberExists, eleId);
            tempFlag = false;
          } else {
            infoFastInput.hideErrorMsg();
            tempFlag = true;
          }
        },
        error: function () {
          layer.msg('终端手机号校验异常！');
          tempFlag = false;
        }
      });
      return tempFlag;
    },
    /**
     * 校验人员编号是否已存在
     * @returns {boolean} 是否存在
     */
    checkPeopleIsExist: function () {
      var tempFlag = true;
      $.ajax({
        type: 'POST',
        url: '/clbs/m/basicinfo/monitoring/personnel/repetitionAdd',
        data: {'peopleNumber': $('#quickBrands').val()},
        dataType: 'json',
        async: false,
        success: function (data) { // data: boolean
          if (!data) {
            infoFastInput.showErrorMsg(personnelNumberExists, 'quickBrands');
            tempFlag = false;
          } else {
            infoFastInput.hideErrorMsg();
            tempFlag = true;
          }
        },
        error: function () {
          layer.msg('人员编号校验异常！', {
            time: 1500
          });
          tempFlag = false;
        }
      });
      return tempFlag;
    },
    // 校验物品是否已存在
    checkThing: function () {
      var tempFlag = true;
      $.ajax({
        type: 'POST',
        url: '/clbs/m/basicinfo/monitoring/ThingInfo/checkThingNumberSole',
        data: {'thingNumber': $('#quickBrands').val()},
        dataType: 'json',
        async: false,
        success: function (data) {
          if (!data) {
            infoFastInput.showErrorMsg(thingExists, 'quickBrands');
            tempFlag = false;
          } else {
            infoFastInput.hideErrorMsg();
            tempFlag = true;
          }
        },
        error: function () {
          layer.msg('物品编号校验异常！');
          tempFlag = false;
        }
      });
      return tempFlag;
    },
    /**
     * 文本框失去焦点事件回调
     * @param eleId {string} DOM元素标签的id属性值
     */
    blurFun: function (eleId) {
      infoFastInput.hideErrorMsg();
      var inputVal = $('#' + eleId).val();

      if (eleId === 'quickBrands' && inputVal !== '' && !flag1 && !infoFastInput.check_brand()) {
        return;
      } else {
        infoFastInput.hideErrorMsg();
      }

      if (eleId === 'quickSims' && inputVal !== '' && !flag3 && !infoFastInput.check_sim('quickSims')) {
        return;
      } else {
        infoFastInput.hideErrorMsg();
      }
    },
    // 校验车牌号是否填写规范或者回车时不小心输入了异常字符
    checkRightBrand: function (id) {
      var errorMsg3 = '请输入汉字、字母、数字或短横杠，长度2-20位';
      if ($('#' + id).val() === '') {
        errorMsg3 = vehicleBrandNull;
      }
      if (checkBrands(id)) {
        infoFastInput.hideErrorMsg();
        return true;
      } else {
        infoFastInput.showErrorMsg(errorMsg3, id);
        return false;
      }
    },
    // 校验人员编号
    checkRightPeopleNumber: function () {
      var reg = /^[A-Za-z0-9\u4e00-\u9fa5_-]{2,20}$/;
      var errorMsg3 = personnelNumberError;
      return infoFastInput.checkIsLegal('quickBrands', reg, null, errorMsg3);
    },
    // 校验对讲设备标识
    checkEquipmentIdentityLegality: function () {
      var reg = /^[A-Z0-9]{7}$/;
      return infoFastInput.checkIsLegal('equipmentIdentity', reg, null, equipmentIdentityLegality);
    },
    // 校验对讲设备密码
    checkDevicePasswordLegality: function () {
      var reg = /^[a-zA-Z0-9]{8}$/;
      return infoFastInput.checkIsLegal('quickDevicePassword', reg, null, quickDevicePasswordLegality);
    },
    // 校验终端是否填写规范或者回车时不小心输入了异常字符
    checkRightDevice: function (id, errorMsg) {
      var reg = /^[A-Za-z0-9]{7,30}$/;
      var errorMsg3 = '请输入字母、数字，长度7~30位';

      return !!infoFastInput.checkIsLegal(id, reg, null, errorMsg3);
    },
    // 校验车辆信息
    check_brand: function () {
      var elementId = 'quickBrands';
      var maxLength = 10;
      // var errorMsg1 = vehicleBrandNull;

      // wjk
      var errorMsg1 = '请输入汉字、字母、数字或短横杠，长度2-20位';

      var errorMsg2 = vehicleBrandMaxlength;
      var errorMsg3 = vehicleBrandError;
      var errorMsg4 = vehicleBrandExists;
      // var reg = /^[\u4eac\u6d25\u5180\u664b\u8499\u8fbd\u5409\u9ed1\u6caa\u82cf\u6d59\u7696\u95fd\u8d63\u9c81\u8c6b\u9102\u6e58\u7ca4\u6842\u743c\u5ddd\u8d35\u4e91\u6e1d\u85cf\u9655\u7518\u9752\u5b81\u65b0\u6d4b]{1}[A-Z]{1}[A-Z_0-9]{5}$/;
      if ($('#quickBrands').val() === '') {
        errorMsg1 = vehicleBrandNull;
      }

      return !!(
        infoFastInput.checkIsEmpty(elementId, errorMsg1) &&
        infoFastInput.checkRightBrand(elementId)
      );
    },
    // 校验人员信息
    check_people_number: function () {
      var elementId = 'quickBrands';
      var maxLength = 8;
      //var errorMsg1 = personnelNumberNull;
      var errorMsg1 = '监控对象不能为空';
      var errorMsg2 = publicSize8Length;
      var errorMsg3 = personnelNumberError;
      var errorMsg4 = personnelNumberExists;
      var reg = /^[A-Za-z0-9\u4e00-\u9fa5_-]{2,20}$/;
      return !!(infoFastInput.checkIsEmpty(elementId, errorMsg1)
        /* && infoFastInput.checkLength(elementId, maxLength, errorMsg2)*/
        && infoFastInput.checkIsLegal(elementId, reg, null, errorMsg3)
        && infoFastInput.checkPeopleIsExist());
    },
    // 校验物品信息
    check_thing: function () {
      var elementId = 'quickBrands';
      var errorMsg1 = publicMonitorNull;
      return !!(infoFastInput.checkIsEmpty(elementId, errorMsg1)
        && infoFastInput.checkRightBrand(elementId)
        && infoFastInput.checkThing());
    },
    /**
     * 校验终端手机号信息是否合法
     * @param eleId {string} DOM标签的id属性值
     * @returns {boolean} 是否合法
     */
    check_sim: function (eleId) {
      var maxLength = 20;
      /*var reg = /^((13[0-9]{1})|(14[5,7,9]{1})|(15[^4]{1})|(166)|(18[0-9]{1})|(19[8-9]{1})|(17[0,1,3,5,6,7,8]{1}))+\d{8}$/;
       var reg1 = /^((\d{3,4}-\d{7,9})|([1-9]{1}\d{6,12}))$/g;*/
      var reg = /^[1-9]\d{6,19}$/g;
      return !!(
        infoFastInput.checkIsEmpty(eleId, simNumberNull) &&
        infoFastInput.checkLength(eleId, maxLength, simNumberMaxlength) &&
        infoFastInput.checkIsLegal(eleId, reg, null, simNumberError) &&
        infoFastInput.checkSIMisExist(eleId)
      );
    },
    // 校验是否为空
    checkIsEmpty: function (elementId, errorMsg) {
      var $ele = $('#' + elementId);
      var value = $ele.val().replace(/(^\s*)|(\s*$)/g, '');
      $ele.val(value);
      if (value === '') {
        infoFastInput.hideErrorMsg();
        infoFastInput.showErrorMsg(errorMsg, elementId);
        return false;
      } else {
        infoFastInput.hideErrorMsg();
        return true;
      }
    },
    // 校验是否已存在
    checkIsExists: function (attr, elementId, requestUrl, errorMsg) {
      var tempFlag = true;
      $.ajax({
        type: 'POST',
        url: requestUrl,
        data: {'device': $('#' + elementId).val()},
        dataType: 'json',
        success: function (data) {
          if (!data.success) {
            infoFastInput.showErrorMsg(errorMsg, elementId);
            tempFlag = false;
          } else {
            infoFastInput.hideErrorMsg();
            tempFlag = true;
          }
        },
        error: function () {
          infoFastInput.showErrorMsg('校验异常', elementId);
          tempFlag = false;
        }
      });
      return tempFlag;
    },
    // 校验是否已被绑定
    checkIsBound: function (elementId, elementValue) {
      var tempFlag = false;
      var data = {
        monitorType: quickMonitorType,
        inputId: elementId,
        inputValue: elementValue
      };

      $.ajax({
        type: 'POST',
        url: '/clbs/talkback/inconfig/infoinput/checkIsBound',
        data: data,
        dataType: 'json',
        async: false,
        success: function (data) {
          if (data.success) {
            if (data && data.obj && (data.obj.isBound || data.obj.isBoundLocateObject || data.obj.isBoundTalkback)) {
              layer.msg('不好意思，你来晚了！【' + data.obj.boundName + '】已被别人抢先一步绑定了');
              tempFlag = true;
            } else {
              tempFlag = false;
            }
          } else if (data.msg) {
            layer.msg(data.msg);
          }
        },
        error: function () {
          infoFastInput.showErrorMsg('校验异常', elementId);
          tempFlag = false;
        }
      });
      return tempFlag;
    },
    /**
     * 校验填写数据的合法性
     * @param elementId
     * @param reg
     * @param reg1
     * @param errorMsg
     * @returns {boolean}
     */
    checkIsLegal: function (elementId, reg, reg1, errorMsg) {
      var value = $('#' + elementId).val();

      if (reg1 != null) {
        if (!reg.test(value) && !reg1.test(value)) {
          infoFastInput.showErrorMsg(errorMsg, elementId);
          return false;
        } else {
          infoFastInput.hideErrorMsg();
          return true;
        }
      } else {
        if (!reg.test(value)) {
          infoFastInput.showErrorMsg(errorMsg, elementId);
          return false;
        } else {
          // wjk
          // 是否全是'-'或'_'
          var regIfAllheng = /^[-]*$/;
          var regIfAllxiahuaxian = /^[_]*$/;

          if (regIfAllheng.test(value) || regIfAllxiahuaxian.test(value)) {
            infoFastInput.showErrorMsg('不能全是横杠或下划线', elementId);
            return false;
          } else {
            infoFastInput.hideErrorMsg();
            return true;
          }
        }
      }
    },
    checkISdhg: function (elementId) {
      var value = $('#' + elementId).val();
      var regIfAllheng = /^[-]*$/;
      if (regIfAllheng.test(value)) {
        infoFastInput.showErrorMsg('不能全是横杠', elementId);
        return false;
      } else {
        infoFastInput.hideErrorMsg();
        return true;
      }
    },
    // 校验长度
    checkLength: function (elementId, maxLength, errorMsg) {
      var value = $('#' + elementId).val();
      if (value.length > parseInt(maxLength)) {
        infoFastInput.showErrorMsg(errorMsg, elementId);
        return false;
      } else {
        infoFastInput.hideErrorMsg();
        return true;
      }
    },
    // 清除错误信息
    clearErrorMsg: function () {
      $('label.error').hide();
    },
    /**
     * @return {string || null}
     */
    GetHttpAddress: function (name) {
      var reg = new RegExp('(^|&)' + name + '=([^&]*)(&|$)');
      var r = window.location.search.substr(1).match(reg);
      if (r != null) return unescape(r[2]);
      return null;
    },
    // 监听浏览器窗口变化
    windowResize: function () {
      var width = $('#quickGroupId').parent().width();
      var speedWidth = $('#speedGroupid').parent().width();
      $('#menuContent').css('width', width + 'px');
      $('#speedMenuContent').css('width', speedWidth + 'px');
      setTimeout(function () {
        var width = $('#speedDevices').parent('div').width();
        $('.searchDevices-div ul').css('width', width + 'px');
      }, 200);
    },
    toggleLeft: function () {
      setTimeout(function () {
        var width = $('#speedDevices').parent('div').width();
        $('.searchDevices-div ul').css('width', width + 'px');
      }, 500);
    },
    searchList: function () {
      hasFlag = false;
      var flag = false;
      var value = $('#speedDevices').val();
      $('#searchDevices-id li').each(function () {
        var name = $(this).text();
        if (name.indexOf(value) === -1) {
          $(this).hide();
          $('#searchDevices-id').hide();
        } else {
          $(this).css('display', 'block');
          flag = true;
        }
        if (name === value) {
          //当有用户输入的标识的时候，默认点击该选项，加载相应标识下的数据
          hasFlag = true;
          $(this).click();
        }
      });
      if (flag) {
        $('#searchDevices-id').show();
      }
    },
    searchList2: function () {
      var value = $('#speedSims').val();
      var flag = false;
      $('#sim-searchDevices-id li').each(function () {
        var name = $(this).text();
        if (name.indexOf(value) === -1) {
          $(this).hide();
          $('#sim-searchDevices-id').hide();
        } else {
          flag = true;
          $(this).css('display', 'block');
        }

        if (flag) {
          $('#sim-searchDevices-id').show();
        }
      });
      var width = $('#speedSims').parent('div').width();
      $('#sim-searchDevices-id').css('width', width + 'px');
    },
    //车、人、物点击tab切换
    chooseLabClick: function () {
      $('#entryContentBox ul.dropdown-menu').css('display', 'none');
      infoFastInput.hideErrorMsg();
      $(this).parents('.form-group').find('input').prop('checked', false);
      $(this).siblings('input').prop('checked', true);
      $(this).parents('.lab-group').find('label.monitoringSelect').removeClass('activeIcon');
      $(this).addClass('activeIcon');
      quickMonitorType = $(this).siblings('input').val();
      var curForm = $(this).closest('form');

      $('label.error').hide(); // 隐藏validate验证错误信息
      $('#quickBrands').val('').attr('style', 'background:#FFFFFF');

      quickRefresh = curForm.attr('id') === 'quickEntryForm';
      infoFastInput.getCallbackList();
    },
    //将静态的终端手机号数据放在下拉框
    getsiminfoset: function () {
      //终端手机号
      var simDataList = {value: []}, k = simCardInfoList.length;
      while (k--) {
        simDataList.value.push({
          name: simCardInfoList[k].simcardNumber,
          id: simCardInfoList[k].id
        });
      }
      $('#speedSims').bsSuggest('destroy'); // 销毁事件
      $('#speedSims').bsSuggest({
        indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
        indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
        data: simDataList,
        effectiveFields: ['name']
      }).on('onDataRequestSuccess', function (e, result) {
      }).on('onSetSelectValue', function (e, keyword, data) {
        $('#speedSimVal').attr('value', keyword.id);
        infoFastInput.hideErrorMsg();
        infoFastInput.checkIsBound('sims', keyword.name); // 校验当前终端手机号是否已经被绑定，两个人同时操作的时候可能会出现
        $('#speedSims').closest('.form-group').find('.dropdown-menu').hide();
      }).on('onUnsetSelectValue', function () {
        flag6 = false;
      });
    },
    // 数组原型链拓展方法
    arrayExpand: function () {
      // 删除数组指定对象
      Array.prototype.remove = function (obj) {
        for (var i = 0; i < this.length; i++) {
          var num = this.indexOf(obj);
          if (num !== -1) {
            this.splice(num, 1);
          }
        }
      };
      // 两个数组的差集
      Array.minus = function (a, b) {
        return a.each(function (o) {
          return b.contains(o) ? null : o;
        });
      };
      // 数组功能扩展
      Array.prototype.each = function (fn) {
        fn = fn || Function.K;
        var a = [];
        var args = Array.prototype.slice.call(arguments, 1);
        for (var i = 0, len = this.length; i < len; i++) {
          var res = fn.apply(this, [this[i], i].concat(args));
          if (res != null) a.push(res);
        }
        return a;
      };
      // 数组是否包含指定元素
      Array.prototype.contains = function (suArr) {
        for (var i = 0, len = this.length; i < len; i++) {
          if (this[i] === suArr) {
            return true;
          }
        }
        return false;
      };
    }
  };

  $(function () {
    // ==========信息录入相关逻辑初始化================
    var $input = $('input');

    // 切换数据录入方式
    /*$('.entryBtn').on('click', function () {
     searchFlag = true;
     $(this).removeClass('btn-default').addClass('btn-primary');
     $(this).siblings().removeClass('btn-primary').addClass('btn-default');
     var curId = $(this).attr('data-target');
     if (curId === 'quickEntry' || curId === 'fastEntry') {
     quickMonitorType = $('#' + curId + ' input[type="radio"]:checked').val();
     }
     if (curId === 'fastEntry') {
     if (fastInitFlag) {
     publicFun.groupTreeInit('fast');
     fastInitFlag = false;
     }
     } else if (curId === 'processEntry') {
     if (processInitFlag) {
     processInput.init();
     processInitFlag = false;
     }
     }
     $('#' + curId).addClass('active').siblings().removeClass('active');
     });*/

    // 初始化快速录入列表
    infoFastInput.init();

    // 监控对象事件处理
    var $quickBrands = $('#quickBrands');
    $quickBrands.blur(function () {
      infoFastInput.blurFun('quickBrands');
    });
    $quickBrands.bind('paste', function () {
      infoFastInput.inputOnPaste('quickBrands');
    });

    // 终端手机号事件处理
    var $quickSims = $('#quickSims');
    $quickSims.blur(function () {
      infoFastInput.blurFun('quickSims');
    });
    $quickSims.bind('paste', function () {
      infoFastInput.inputOnPaste('quickSims');
    });

    // 功能复选框事件
    $('#quickFeaturesTest').on('change', function (e) {
      $(this).attr('value', e.target.checked ? 1 : 0);
    });
    $('#quickFeaturesPic').on('change', function (e) {
      $(this).attr('value', e.target.checked ? 1 : 0);
    });
    $('#quickFeaturesVoice').on('change', function (e) {
      $(this).attr('value', e.target.checked ? 1 : 0);
    });

    // 信息录入群组树初始化
    publicFun.groupTreeInit('quick');

    infoFastInput.arrayExpand();
    $input.inputClear().on('onClearEvent', function (e, data) {
      var id = data.id;
      if (id === 'quickBrands') {
        $('#quickPlateColor').removeAttr('disabled');
      }

      if (id === 'search_condition') {
        search_ztree('groupTreeDemo', id, 'assignment');
      }

      if (id === 'quickGroupId') {
        searchFlag = false;
        $('#quickGroupId').val('');
        $('#quickCitySelidVal').val('');
        publicFun.groupTreeInit('quick');
      }

      setTimeout(function () {
        $('#' + id).focus();
      }, 20);
    });

    // 群组树点击事件
    $('.groupZtree').on('click', showMenuContent);

    // 表单提交
    $('#quickSubmits').on('click', infoFastInput.doSubmits);

    //车、人、物点击tab切换
    $('#quickEntryForm label.monitoringSelect,#fastEntryForm label.monitoringSelect').on('click', infoFastInput.chooseLabClick);

    $('.select-value,.btn-width-select').buttonGroupPullDown();

    $('#quickGroupId').on('input propertychange', function () {
      var treeObj = $.fn.zTree.getZTreeObj('quickTreeDemo');
      treeObj.checkAllNodes(false);
      $('#quickCitySelidVal').val('');
      search_ztree('quickTreeDemo', 'quickGroupId', 'assignment');
    });
    $('#speedGroupid').on('input propertychange', function () {
      var treeObj = $.fn.zTree.getZTreeObj('fastTreeDemo');
      treeObj.checkAllNodes(false);
      $('#speedCitySelidVal').val('');
      search_ztree('fastTreeDemo', 'speedGroupid', 'assignment');
    });

    // IE9
    if (navigator.appName === 'Microsoft Internet Explorer' && navigator.appVersion.split(';')[1].replace(/[ ]/g, '') === 'MSIE9.0') {
      var search;
      $('#search_condition').bind('focus', function () {
        search = setInterval(function () {
          search_ztree('groupTreeDemo', 'search_condition', 'assignment');
        }, 500);
      }).bind('blur', function () {
        clearInterval(search);
      });
    }

    // =======信息列表相关逻辑初始化=========
    infoinputList.init();

    //批量生成
    $('#generateId').bind('click', infoinputList.delMooreIntercomInfo);
    //批量删除
    $('#del_model').bind('click', infoinputList.delModelClick);

    infoinputList.showMenuText();
    if (navigator.appName === 'Microsoft Internet Explorer' && navigator.appVersion.split(';')[1].replace(/[ ]/g, '') === 'MSIE9.0') {
      infoinputList.refreshTable();
    }
    $('#refreshTable').on('click', infoinputList.refreshTable);

    //监听浏览器窗口变化
    $(window).resize(infoFastInput.windowResize);
    $('#toggle-left').on('click', infoFastInput.toggleLeft);
  });
})(window, $);