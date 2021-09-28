//# sourceURL=fenceSpeciesAdd.js
(function (window, $) {
  fenceSpeciesAdd = {
    init: function () {
      $('#settingColor').colpick({
        layout: 'hex',
        submit: 0,
        color: 'ffffff',
        colorScheme: 'dark',
        onChange: function (hsb, hex, rgb, el, bySetColor) {
          $('#fenceColor-error').remove();
          $('#colorBg').css('background-color', '#' + hex);
          if (!bySetColor) $('#fenceColor').val('' + hex);
        }
      });
    },
    doSubmits: function () {
      if (!fenceSpeciesAdd.validates()) return;

      $('#addFenceForm').ajaxSubmit(function (data) {
        data = JSON.parse(data);
        if (data.success) {
          //关闭弹窗
          $('#commonWin').modal('hide');
          layer.msg('添加成功！', {move: false});
          fenceHandle.getFenceList();
        } else if (data.msg) {
          layer.msg(data.msg);
        }
      });
    },
    validates: function () {
      return $('#addFenceForm').validate({
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
                }
              }
              /* dataFilter: function (data, type) {
               var result = JSON.parse(data);
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
      // 处理围栏类型的值
      var chkValArr = [];
      $('input.drawWayCheck:checked').each(function () {
        chkValArr.push($(this).val());
      });

      var chkVal = chkValArr.join(',');

      if (chkVal) {
        $('#drawWay-error').remove();
        $('#drawWay').val(chkVal);
      } else {
        $('#drawWay').val('');
      }
    },
    setColor: function () {
      var reg = /^[0-9a-fA-F]*$/;
      var rgb = $('#fenceColor').val();
      if (rgb.length === 6 && reg.test(rgb)) {
        $('#colorBg').css('background-color', '#' + rgb);
      }
    }
  };
  $(function () {
    fenceSpeciesAdd.init();
    $('input').inputClear();
    $('.drawWayCheck').on('change', fenceSpeciesAdd.drawWayCheckChange);
    $('#doSubmitAdd').bind('click', fenceSpeciesAdd.doSubmits);
    $('#fenceColor').on('input oninput', fenceSpeciesAdd.setColor);
  });
})(window, $);