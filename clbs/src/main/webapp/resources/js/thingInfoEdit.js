(function (window, $) {
  editItemInformation = {
    init: function () {
      var setting = {
        async: {
          url: '/clbs/m/basicinfo/enterprise/professionals/tree',
          type: 'post',
          enable: true,
          autoParam: ['id'],
          dataType: 'json',
          otherParam: {
            'vid': ''
          }
        },
        view: {
          dblClickExpand: false,
          nameIsHTML: true,
          countClass: 'group-number-statistics'
        },
        data: {
          simpleData: {
            enable: true
          }
        },
        callback: {
          beforeClick: editItemInformation.beforeClick,
          onClick: editItemInformation.onClick,
          onAsyncSuccess: editItemInformation.zTreeOnAsyncSuccess
        }
      };
      $.fn.zTree.init($('#ztreeDemo'), setting, null);
    },
    zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
      var treeObj = $.fn.zTree.getZTreeObj(treeId);
      treeObj.expandAll(true); // 展开所有节点
    },
    beforeClick: function (treeId, treeNode) {
      var check = (treeNode);
      return check;
    },
    onClick: function (e, treeId, treeNode) {
      var zTree = $.fn.zTree.getZTreeObj('ztreeDemo'),
        nodes = zTree.getSelectedNodes(),
        n = '';
      v = '';
      nodes.sort(function compare(a, b) {
        return a.id - b.id;
      });
      for (var i = 0, l = nodes.length; i < l; i++) {
        n += nodes[i].name;
        v += nodes[i].uuid + ';';
      }
      if (v.length > 0)
        v = v.substring(0, v.length - 1);
      var cityObj = $('#zTreeCitySel');
      cityObj.attr('value', v);
      cityObj.val(n);
      $('#groupId').val(v);
      $('#zTreeContent').hide();
    },
    showMenu: function (e) {
      if ($('#zTreeContent').is(':hidden')) {
        var width = $(e).parent().width();
        $('#zTreeContent').css('width', width + 'px');
        $(window).resize(function () {
          var width = $(e).parent().width();
          $('#zTreeContent').css('width', width + 'px');
        });
        $('#zTreeContent').show();
      } else {
        $('#zTreeContent').hide();
      }

      $('body').bind('mousedown', editItemInformation.onBodyDown);
    },
    hideMenu: function () {
      $('#zTreeContent').fadeOut('fast');
      $('body').unbind('mousedown', editItemInformation.onBodyDown);
    },
    onBodyDown: function (event) {
      if (!(event.target.id == 'menuBtn' || event.target.id == 'zTreeContent' || $(
        event.target).parents('#zTreeContent').length > 0)) {
        editItemInformation.hideMenu();
      }
    },
    validates: function () {
      return $('#editForm').validate({
        rules: {
          thingNumber: {
            required: true,
            checkThingNumber: true,
            remote: {
              type: 'post',
              async: false,
              url: '/clbs/m/basicinfo/monitoring/ThingInfo/repetition',
              data: {
                username: function () {
                  return $('#thingNumber').val();
                },
                id: function () {
                  return $('#id').val();
                }
              }
            }
          },
          name: {
            maxlength: 20
          },
          label: {
            maxlength: 20
          },
          model: {
            maxlength: 20
          },
          material: {
            maxlength: 20
          },
          weight: {
            maxlength: 9,
            isRightNumber: true,
            min: 0
          },
          spec: {
            maxlength: 20
          },
          manufacture: {
            maxlength: 20
          },
          dealer: {
            maxlength: 20
          },
          place: {
            maxlength: 10
          },
          remark: {
            maxlength: 50
          }
        },
        messages: {
          thingNumber: {
            required: '物品编号不能为空',
            maxlength: publicSize20,
            remote: thingInfoNumberExists
          },
          name: {

            maxlength: publicSize20
          },
          label: {
            maxlength: publicSize20
          },
          model: {
            maxlength: publicSize20
          },
          material: {
            maxlength: publicSize20
          },
          weight: {
            maxlength: '长度不能超过9位',
            isRightNumber: '请输入正整数',
            min: publicpositive
          },
          spec: {
            maxlength: publicSize20
          },
          manufacture: {
            maxlength: publicSize20
          },
          dealer: {
            maxlength: publicSize20
          },
          place: {
            maxlength: publicSize10
          },
          remark: {
            maxlength: publicSize50
          }
        }
      }).form();
    },
    doSubmits: function () {
      if (editItemInformation.validates()) {
        addHashCode1($("#editForm"));
        $('#editForm').ajaxSubmit(function (data) {
          var json = eval('(' + data + ')');
          if (json.success) {
            $('#commonWin').modal('hide');
            myTable.refresh();
          } else {
            layer.msg(json.msg);
          }
        });
      }
    },
    setImagePreview: function (avalue) {
      editItemInformation.uploadImage(); // 上传图片到服务器
      var docObj = document.getElementById('doc');
      var imgObjPreview = document.getElementById('preview');
      if (docObj.files && docObj.files[0]) {
        //火狐下，直接设img属性
        imgObjPreview.style.display = 'block';
        imgObjPreview.style.width = '200px';
        imgObjPreview.style.height = '200px';
        //火狐7以上版本不能用上面的getAsDataURL()方式获取，需要一下方式
        if (window.navigator.userAgent.indexOf('Chrome') >= 1 || window.navigator.userAgent.indexOf('Safari') >= 1) {
          imgObjPreview.src = window.webkitURL.createObjectURL(docObj.files[0]);
        } else {
          imgObjPreview.src = window.URL.createObjectURL(docObj.files[0]);
        }
      } else {
        //IE下，使用滤镜
        docObj.select();
        var imgSrc = document.selection.createRange().text;
        var localImagId = document.getElementById('localImag');
        //必须设置初始大小
        localImagId.style.width = '200px';
        localImagId.style.height = '200px';
        //图片异常的捕捉，防止用户修改后缀来伪造图片
        try {
          localImagId.style.filter = 'progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale)';
          localImagId.filters.item('DXImageTransform.Microsoft.AlphaImageLoader').src = imgSrc;
        } catch (e) {
          layer.msg('不支持的图片格式文件，<br/>支持格式（png，jpg，gif，jpeg）');
          return false;
        }
        imgObjPreview.style.display = 'none';
        document.selection.empty();
      }
      return true;
    },
    uploadImage: function () {
      var docObj = document.getElementById('doc');
      if (docObj.files && docObj.files[0]) {
        var formData = new FormData();
        formData.append('file', docObj.files[0]);
        $.ajax({
          url: '/clbs/m/basicinfo/enterprise/professionals/upload_img',
          type: 'POST',
          data: formData,
          async: false,
          cache: false,
          contentType: false,
          processData: false,
          success: function (data) {
            data = $.parseJSON(data);
            if (data.imgName == '0') {
              layer.msg('不支持的图片格式文件，<br/>支持格式（png，jpg，gif，jpeg）');
              $('#preview').src('');
            } else {
              $('#thingPhoto').val(data.imgName);
            }
          },
          error: function (data) {
            layer.msg('上传失败！');
          }
        });
      }
    }
  };
  $(function () {
    editItemInformation.init();
    $('input').inputClear();
    $('#doSubmitsEdit').bind('click', editItemInformation.doSubmits);
    $('#zTreeCitySel').on('click', function () {
      editItemInformation.showMenu(this);
    });
    setTimeout('$(".delIcon").hide()', 100);
    laydate.render({elem: '#productDate', theme: '#6dcff6'});
    // 组织树input框的模糊搜索
    $('#zTreeCitySel').on('input propertychange', function (value) {
      var treeObj = $.fn.zTree.getZTreeObj('ztreeDemo');
      treeObj.checkAllNodes(false);
      search_ztree('ztreeDemo', 'zTreeCitySel', 'group');
    });
    // 组织树input框快速清空
    $('input').inputClear().on('onClearEvent', function (e, data) {
      var id = data.id;
      var treeObj;
      if (id == 'zTreeCitySel') {
        search_ztree('ztreeDemo', 'zTreeCitySel', 'group');
        treeObj = $.fn.zTree.getZTreeObj('ztreeDemo');
      }
      treeObj.checkAllNodes(false);
    });
  });
})(window, $);

$.validator.addMethod('checkThingNumber', function (value, element, params) {
  var checkThingNumber = /^[\u4e00-\u9fa5-a-zA-Z0-9]{2,20}$/;
  return this.optional(element) || (checkThingNumber.test(value));
}, '请输入汉字、字母、数字或短横杠，长度2-20位');