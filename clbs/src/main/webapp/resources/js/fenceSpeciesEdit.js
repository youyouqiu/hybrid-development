//# sourceURL=fenceSpeciesEdit.js
(function (window, $) {
  var color = $('#fenceColor').val();// 默认显示颜色
  var checkDrawWay = $('.drawWayCheck');// 绘制方式
  var drawWay = $('#drawWay').val().split(',');// 绘制方式
  var drawType = JSON.parse('[' + $('#drawType').val() + ']');// 已经绘制过的围栏,不能再更改
  fenceSpeciesEdit = {
    init: function () {
      $('#colorBg').css('background-color', color ? '\#' + color : '#fff');
      for (var i = 0; i < checkDrawWay.length; i++) {
        var item = $(checkDrawWay[i]).val();
        if (drawWay.indexOf(item) != -1) {
          $(checkDrawWay[i]).prop('checked', true);
        }
        if (drawType.indexOf(item) != -1) {
          $(checkDrawWay[i]).prop('disabled', true);
        }
      }

      $('#settingColor').colpick({
        layout: 'hex',
        submit: 0,
        color: color ? color : 'ffffff',
        colorScheme: 'dark',
        onChange: function (hsb, hex, rgb, el, bySetColor) {
          $('#fenceColor-error').remove();
          $('#colorBg').css('background-color', '#' + hex);
          if (!bySetColor) $('#fenceColor').val('' + hex);
        }
      });
    },
    doSubmits: function () {
      if (!fenceSpeciesEdit.validates()) return;

      $('#editFenceForm').ajaxSubmit(function (data) {
        data = JSON.parse(data);
        if (data.success) {
          //关闭弹窗
          $('#commonWin').modal('hide');
          layer.msg('修改成功！', {move: false});
          fenceHandle.getFenceList();

          // 修改成功后，重新加载当前分类的围栏（刷新围栏）
          var curFenceEle = $('#subFenceUl li input').not('.check-all').parent();
          if (curFenceEle.length) {
            $.each(curFenceEle, function (index, ele) {
              var fenceObj = JSON.parse($(ele).data('fenceobj').replace(/'/g, '"'));
              // 在地图上显示已勾选的围栏
              if ($(ele).find('input').is(':checked')) {
                fenceHandle.showZtreeCheckedToMap(null, fenceObj, true);
              }
            });
          }

          if ($('#subFenceUl').find('li.subFence input:checked').length === $('#subFenceUl').find('li.subFence').length) {
            $('#checkAllFence input:checkbox').prop('checked', true);
          } else {
            $('#checkAllFence input:checkbox').prop('checked', false);
          }
        } else if (data.msg) {
          layer.msg(data.msg);
        }
      });
    },
    validates: function () {
      return $('#editFenceForm').validate({
        rules: {
          fenceTypeName: {
            required: true,
            isfenceName: true,
            maxlength: 10,
            remote: {
              type: 'post',
              async: false,
              url: '/clbs/m/regionManagement/fenceManagement/judgeFenceTypeNameIsCanBeUsed',
              dataType: 'json',
              data: {
                fenceTypeName: function () {
                  return $('#fenceTypeName').val();
                },
                fenceTypeId: function () {
                  return $('#fenceTypeId').val();
                }
              }
              /*dataFilter: function (data, type) {
               var result = JSON.parse(data);
               var oldVal = $('#oldFenceTypeName').val().trim();
               var newVal = $('#fenceTypeName').val().trim();
               if (oldVal == newVal) return true;
               if (result.success) return true;
               return false;
               }*/
            }
          },
          colorCode: {
            required: true,
            maxlength: 6,
            minlength: 6
          },
          transparency: {
            required: true,
            digits: true,
            range: [0, 100]
          },
          drawWay: {
            required: true
          }
        },
        messages: {
          fenceTypeName: {
            required: fenceNameNull,
            isfenceName: fenceNameError,
            maxlength: publicSize10,
            remote: fenceNameRepeat
          },
          colorCode: {
            required: fenceColorNull,
            maxlength: '请输入正确的格式',
            minlength: '请输入正确的格式'
          },
          transparency: {
            required: fenceTransparencyNull,
            digits: fenceTransparencyError,
            range: fenceTransparencyError
          },
          drawWay: {
            required: fenceDrawWayNull
          }
        }
      }).form();
    },
    drawWayCheckChange: function () {
      var allCheck = $('.drawWayCheck:checked');
      var curVal = '';
      for (var i = 0; i < allCheck.length; i++) {
        curVal += $(allCheck[i]).val() + ',';
        $('#drawWay-error').remove();
      }
      $('#drawWay').val(curVal);
    },
    drawWayClick: function () {
      var checked = this.checked;
      var alreadyDrawFence = $('#alreadyDrawFence').val();
      var value = $(this).val();
      var result = true;

      if (!checked) {
        alreadyDrawFence = alreadyDrawFence.split(',');
        if (alreadyDrawFence.indexOf(value) != -1) {
          layer.msg('该绘制方式下已绘制了具体围栏，不能取消！');
          result = false;
        }
      }
      return result;
    },
    setColor: function () {
      var reg = /^[0-9a-fA-F]*$/;
      var rgb = $('#fenceColor').val();
      if (rgb.length == 6 && reg.test(rgb)) {
        $('#colorBg').css('background-color', '#' + rgb);
      }
    }
  };
  $(function () {
    fenceSpeciesEdit.init();
    $('input').inputClear();
    $('.drawWayCheck').on('change', fenceSpeciesEdit.drawWayCheckChange);
    $('.drawWayCheck').on('click', fenceSpeciesEdit.drawWayClick);
    $('#doSubmitEdit').bind('click', fenceSpeciesEdit.doSubmits);
    $('#fenceColor').on('input oninput', fenceSpeciesEdit.setColor);
  });
})(window, $);