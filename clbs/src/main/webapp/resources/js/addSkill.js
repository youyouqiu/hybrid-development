(function (window, $) {
  var SkillCategory = JSON.parse($('#SkillCategory').val());
  addSkill = {
    getCategories: function () {
      var str = '';
      for (var i = 0; i < SkillCategory.length; i++) {
        str += '<option value="' + SkillCategory[i].id + '"> ' + html2Escape(SkillCategory[i].name) + ' </option>';
      }
      $('#CategoriesName').html(str);
    },
    validates: function () {
      return $('#AddForm').validate({
        rules: {
          name: {
            required: true,
            isCategories: true,
            maxlength: 6,
            remote: {
              type: 'post',
              url: '/clbs/talkback/basicinfo/skill/checkSkillName',
              async: false,
              data: {
                name: function () {
                  return $('#skillName').val();
                }
              }
            }
          },
          categoriesId: {
            required: true
          }
        },
        messages: {
          name: {
            required: '技能名称不能为空',
            remote: '此名称与已有的技能同名，请重新输入',
            maxlength: '技能名称不能超过6位'
          },
          categoriesId: {
            required: '技能类别不能为空'
          }
        }
      }).form();
    },
    doSubmits: function () {
      if (addSkill.validates()) {
        $('#AddForm').ajaxSubmit(function (data) {
          data = JSON.parse(data);
          if (data.success) {
            $('#commonSmWin').modal('hide');
            layer.msg('添加成功！', {move: false});
            modelTable.requestData();
          }
        });
      }
    }
  };

  $(function () {
    var $skillCategory = $('#SkillCategory');
    if (!$skillCategory.val() || $skillCategory.val() === '[]') {
      setTimeout(function () {
        layer.msg('请先至少新增一个技能类别！');
        $('#commonSmWin').modal('hide');
      }, 1000);
    }

    addSkill.getCategories();
    $('input').inputClear();
    $('#AddSkill').bind('click', addSkill.doSubmits);
    // 判断技能列表名称
    jQuery.validator.addMethod('isCategories', function (value, element) {
      return this.optional(element) || /^[A-Za-z0-9\u4e00-\u9fa5\-]+$/.test(value);
    }, '技能名称可支持汉字、字母、数字或短横杠');
  });
})(window, $);