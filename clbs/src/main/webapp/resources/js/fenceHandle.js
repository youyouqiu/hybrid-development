//# sourceURL=fenceHandle.js
(function ($, window) {
  window.globalFenceObj = {
    currentFenceId: null,// 当前选择的围栏种类id
    checkFenceObj: {},// 勾选的围栏集合(key:围栏id,value:围栏类型)
    selectSubFenceId: null// 选中的围栏id
  };
  window.fenceHandle = {
    init: function () {
      fenceHandle.setWrapperHeight();
      window.onresize = function () {
        fenceHandle.setWrapperHeight();
      };
      fenceHandle.getFenceList();
    },
    /**
     * 获取围栏种类列表
     * */
    getFenceList: function () {
      var url = '/clbs/m/regionManagement/fenceManagement/getFenceTypeList';
      json_ajax('POST', url, 'json', false, null, function (data) {
        var listHtml = '';
        var result = data.obj;
        if (data.success) {
          for (var i = 0; i < result.length; i++) {
            var activeFence = false;
            if (!globalFenceObj.currentFenceId) {
              activeFence = (i === 0);
              globalFenceObj.currentFenceId = result[0].id;
            } else if (globalFenceObj.currentFenceId == result[i].id) {
              globalFenceObj.currentFenceId = result[i].id;
              activeFence = true;
            }
            var bgColor = '#' + result[i].colorCode;
            var itemObj = JSON.stringify(result[i]).replace(/"/g, '\'');
            listHtml += '<li class="' + (activeFence ? 'active' : '') + '" data-fenceTypeObj="' + itemObj + '">' +
              ' <a href="javascript:void(0)">' + result[i].fenceTypeName + ' <i class="fence-color-circle" style="background-color: ' + bgColor + ';opacity: ' + parseFloat(result[i].transparency / 100) + '"></i></a>' +
              '</li>';
          }
        }
        $('.handleBox button').prop('disabled', false);
        if (listHtml === '') {
          $('.handleBox button').prop('disabled', true);
          $('.handleBox #addFence').prop('disabled', false);
        } else if (globalFenceObj.currentFenceId) {
          fenceHandle.setFenceUrl(globalFenceObj.currentFenceId);
          fenceHandle.getSubFenceList(globalFenceObj.currentFenceId);
        }
        $('#fenceUl').html(listHtml);
      });
    },
    /**
     * 围栏种类切换
     */
    fenceTabChange: function () {
      if ($(this).hasClass('active')) return;
      $(this).addClass('active').siblings('li').removeClass('active');

      var currentFenceObj = JSON.parse($(this).attr('data-fenceTypeObj').replace(/'/g, '"'));
      var fenceTypeId = currentFenceObj.id;
      globalFenceObj.currentFenceId = fenceTypeId;
      globalFenceObj.selectSubFenceId = null;
      // 设置围栏种类底部操作按钮URL
      fenceHandle.setFenceUrl(fenceTypeId);
      // 获取围栏列表
      fenceHandle.getSubFenceList(fenceTypeId);

      // 禁用围栏修改、详情、删除
      var subFenceHasActive = $('#subFenceUl .subFence.active');
      if (!subFenceHasActive.length) {
        $('#subFenceHandle button').prop('disabled', true);
        $('#subFenceHandle .addSubFence').prop('disabled', false);
      }

      fenceHandle.isCheckAll();
    },
    /**
     * 设置围栏种类底部操作按钮URL
     * @param fenceTypeId:围栏种类id
     * */
    setFenceUrl: function (fenceTypeId) {
      var editFenceUrl = '/clbs/m/regionManagement/fenceManagement/getFenceTypeUpdatePage/' + fenceTypeId;
      $('#editFence').attr('href', editFenceUrl);
      var fenceDetailUrl = '/clbs/m/regionManagement/fenceManagement/getFenceTypeDetailPage/' + fenceTypeId;
      $('#fenceDetail').attr('href', fenceDetailUrl);
    },
    /**
     * 删除围栏种类
     * */
    deleteFence: function () {
      layer.confirm('删掉就没啦，请谨慎下手！', {
        title: '操作确认',
        icon: 3, // 问号图标
        move: false,//禁止拖动
        btn: ['确定', '取消']
      }, function () {
        var url = '/clbs/m/regionManagement/fenceManagement/deleteFenceType';
        json_ajax('POST', url, 'json', false, {'fenceTypeId': globalFenceObj.currentFenceId}, function (data) {
          layer.closeAll();
          if (data.success) {
            layer.msg('删除成功');
            globalFenceObj.currentFenceId = null;
            fenceHandle.getFenceList();
          } else if (data.msg) {
            layer.msg(data.msg);
          }
        });
      }, function () {
      });
    },
    /**
     * 获取围栏列表
     * @param fenceTypeId:围栏种类id
     * */
    getSubFenceList: function (fenceTypeId) {
      var url = '/clbs/m/regionManagement/fenceManagement/getFenceInfoListByFenceTypeId';
      json_ajax('POST', url, 'json', false, {'fenceTypeId': fenceTypeId}, function (data) {
        var listHtml = '';
        if (data.success) {
          var result = data.obj;
          if (result.length > 0) {
            listHtml += '<li id="checkAllFence">' +
              '         <input type="checkbox" id="fence1-all" class="check-all">' +
              '         <label for="fence1-all">全部</label>' +
              '      </li>';
          }

          $.each(result, function (index, item) {
            var itemObj = JSON.stringify(item).replace(/"/g, '\'');

            // listHtml += '<li title="' + item.fenceName + '" class="subFence ' + (isSelect ? 'active' : '') + '" data-fenceObj="' + itemObj + '">';
            listHtml += '<li title="' + item.fenceName + '" class="subFence" data-fenceObj="' + itemObj + '">' +
              '             <input type="checkbox"' + (globalFenceObj.checkFenceObj[item.shape] ? 'checked' : '') + '>' +
              '             <label for="fence1-2">' +
              '             <span class="button ' + item.type + '_skin_ico_docu"></span>' + item.fenceName + '</label>' +
              '         </li>';
          });
        }

        $('#fenceContent ul').html(listHtml);
        // 控制底部操作按钮是否可用
        fenceHandle.setSubFenceHandle();
      });
    },
    /**
     * 设置围栏列表容器高度
     * */
    setWrapperHeight: function () {
      $('#fenceListWrapper').css('height', $(window).height() - 80);
    },
    /**
     * 控制围栏底部操作按钮是否可用
     */
    setSubFenceHandle: function () {
      if (globalFenceObj.selectSubFenceId) {
        $('#subFenceHandle button').prop('disabled', false);
      } else {
        $('#subFenceHandle button').prop('disabled', true);
        $('.addSubFence').prop('disabled', false);
      }
    },
    /**
     * 选中围栏(点击li标签，其下的input点击除外)
     * */
    subFenceLiClick: function (e) {
      if (e.target.nodeName === 'INPUT') return;

      if ($(this).attr('id') !== 'checkAllFence' && !$(this).hasClass('active')) {
        var currentFenceObj = JSON.parse($(this).attr('data-fenceobj').replace(/'/g, '"'));
        var curInput = $(this).find('input');
        curInput.prop('checked', true);
        var curSubFenceId = currentFenceObj.shape;
        globalFenceObj.selectSubFenceId = curSubFenceId;
        if (!globalFenceObj.checkFenceObj[curSubFenceId]) {
          globalFenceObj.checkFenceObj[curSubFenceId] = currentFenceObj.type;
        }

        $(this)
          .addClass('active')
          .siblings('li').removeClass('active');

        fenceHandle.showZtreeCheckedToMap(curSubFenceId, currentFenceObj, true);
        // 控制底部操作按钮是否可用
        fenceHandle.setSubFenceHandle();
        // 反向操作 全选/全不选
        fenceHandle.isCheckAll();
      }
    },
    /**
     * 围栏前的input勾选切换
     * */
    subFenceInputChange: function (event, flag) {
      var _this = (flag === true ? event : $(this));

      if (_this.attr('id') !== 'fence1-all') {
        var parentLi = _this.closest('li');
        var currentFenceObj = JSON.parse(parentLi.attr('data-fenceobj').replace(/'/g, '"'));
        var curSubFenceId = currentFenceObj.shape;

        if (_this.is(':checked')) {
          // 非全选操作时（操作单项围栏时）
          if (!flag) {
            parentLi.addClass('active').siblings('li').removeClass('active');
          }

          if (!globalFenceObj.checkFenceObj[curSubFenceId]) {
            globalFenceObj.checkFenceObj[curSubFenceId] = currentFenceObj.type;
          }
          if (curSubFenceId) {
            globalFenceObj.selectSubFenceId = curSubFenceId;
          }
        } else {
          globalFenceObj.checkFenceObj[curSubFenceId] = null;
          globalFenceObj.selectSubFenceId = null;

          if (parentLi.hasClass('active')) {
            parentLi.removeClass('active');
          }
        }
        fenceHandle.showZtreeCheckedToMap(curSubFenceId, currentFenceObj, _this.is(':checked'));
        // 控制底部操作按钮是否可用
        fenceHandle.setSubFenceHandle();
        // 反向操作 全选/全不选
        fenceHandle.isCheckAll();
      } else {// 全选操作
        var allSubFence = _this.closest('ul').find('.subFence');
        $('#subFenceUl input').prop('checked', _this.is(':checked'));

        for (var i = 0; i < allSubFence.length; i++) {
          fenceHandle.subFenceInputChange($(allSubFence[i]).find('input'), true);
        }
      }
    },
    isCheckAll: function () {
      if ($('#subFenceUl').find('li.subFence input:checked').length === $('#subFenceUl').find('li.subFence').length) {
        $('#checkAllFence input:checkbox').prop('checked', true);
      } else {
        $('#checkAllFence input:checkbox').prop('checked', false);
      }
    },
    /**
     * 新增围栏
     * */
    addSubFence: function () {
      mouseToolEdit.close(true);
      amapOperation.clearLabel();
      isAddDragRoute = false;
      $('#drivenRoute').hide();
      $('.lngLat_show').children('span').attr('class', 'fa fa-chevron-up');
      $('.pointList').hide();
      $('.fenceA i').removeClass('active');
      $('.fenceA span').css('color', '#5c5e62');
      isAddFlag = true;
      isAreaSearchFlag = false;
      var currentFenceObj = JSON.parse($('#fenceUl li.active').attr('data-fenceTypeObj').replace(/'/g, '"'));
      var draWay = currentFenceObj.drawWay;
      if (draWay && draWay.length > 0) {
        window.fenceTypeId = currentFenceObj.id;
        window.fenceColor = currentFenceObj.colorCode;
        window.fenceOpacity = currentFenceObj.transparency / 100;
        draWay = draWay.split(',').filter(function (value) {
          return value.trim().length > 0;
        });
        if (draWay.length === 1) {
          // 1:多边形; 2:圆; 3:路线; 4:标注
          switch (draWay[0]) {
            case '1':
              fenceOperation.drawFence('多边形');
              break;
            case '2':
              fenceOperation.drawFence('圆形');
              break;
            case '3':
              fenceOperation.drawFence('路线');
              break;
            case '4':
              fenceOperation.drawFence('标注');
              break;
            default:
              return;
          }
        } else {
          if (draWay.indexOf('1') > -1) {
            $('#polygonRow').show();
          } else {
            $('#polygonRow').hide();
          }
          if (draWay.indexOf('2') > -1) {
            $('#circleRow').show();
          } else {
            $('#circleRow').hide();
          }
          if (draWay.indexOf('3') > -1) {
            $('#lineRow').show();
          } else {
            $('#lineRow').hide();
          }
          if (draWay.indexOf('4') > -1) {
            $('#markerRow').show();
          } else {
            $('#markerRow').hide();
          }
          $('#choseFenceModal').modal('show');
        }
      }
    },
    /**
     * 显示当前勾选围栏到地图
     * */
    showZtreeCheckedToMap: function (fenceId, currentFenceObj, checked) {
      if (checked) {
        fenceOperation.fenceShow(fenceId, currentFenceObj);
        fenceOperation.sectionPointState(fenceId, true);
        // treeMonitoring.showFenceInfo(fenceId, currentFenceObj);
      } else {
        // treeMonitoring.hideFenceInfo(fenceId);
        fenceOperation.hideFence(fenceId);
        fenceOperation.fenceHidden(fenceId);
        fenceOperation.sectionPointState(fenceId, false);
      }
    },
    /**
     *围栏详情
     */
    detailSubFence: function () {
      var subFeninfo = $('#subFenceUl .subFence.active').data('fenceobj');
      subFeninfo = eval('(' + subFeninfo + ')');

      var params = {
        fenceId: subFeninfo.shape,
        type: subFeninfo.type
      };
      json_ajax('POST', '/clbs/m/regionManagement/fenceManagement/getFenceDetail', 'json', true, params, function (data) {
        if (data.success) {
          var dataList = data.obj;
          if (dataList) {
            fenceOperation.fenceDetails(dataList);
            $('#detailsModel').modal('show');
          }
        } else {
          if (data.msg) {
            layer.msg(data.msg);
          }
        }
      });
    }
  };
  $(function () {
    $('input').inputClear();
    fenceHandle.init();

    $('#fenceUl').on('click', 'li', fenceHandle.fenceTabChange);
    $('#deleteFence').on('click', fenceHandle.deleteFence);
    $('#subFenceUl').on('click', 'li', fenceHandle.subFenceLiClick);
    $('#subFenceUl').on('change', 'input', fenceHandle.subFenceInputChange);
    $('.addSubFence').on('click', fenceHandle.addSubFence);
    $('.subFenceDetail').on('click', fenceHandle.detailSubFence);
    $('.editSubFence').on('click', fenceOperation.editSubFence);
    $('.deleteSubFence').on('click', fenceOperation.deleteFence);
  });
})($, window);