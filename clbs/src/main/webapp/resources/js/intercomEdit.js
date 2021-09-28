(function (window, $) {
  intercomEdit = {
    init: function () {
      var index = $('#index').val();
      var getModels = '';
      $.ajax({
        type: 'post',
        url: '/clbs/talkback/intercomplatform/intercommodel/getAllOriginalModel',
        async: false,
        data: {index: index},
        success: function (data) {
          data = JSON.parse(data);
          getModels = data.obj;
        }
      });

      var dataList = {value: []};
      for (var i = 0; i < getModels.length; i++) {
        dataList.value.push({
          name: getModels[i].modelId,
          id: getModels[i].index
        });
      }

      $('#modelId').bsSuggest('destroy'); // 销毁事件
      $('#modelId').bsSuggest({
        indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
        indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
        data: dataList,
        effectiveFields: ['name']
      }).on('onDataRequestSuccess', function (e, result) {
      }).on('onSetSelectValue', function (e, keyword, data) {
        $('#index').val(keyword.id);
        $(this).next('.error').hide();
        intercomEdit.getOriginalModelByIndex();
      }).on('onUnsetSelectValue', function () {
      }).on('input propertychange', function () {
        $('#index').val('');
        $(this).next('.error').hide();
      });

      var originalModelId = $('#index').val();
      for (var i = 0; i < getModels.length; i++) {
        if (getModels[i].index == originalModelId) {
          return $('#modelId').val(getModels[i].modelId);
        }
      }
    },
    getOriginalModelByIndex: function () {
      var index = $('#index').val();
      $.ajax({
        type: 'post',
        url: '/clbs/talkback/intercomplatform/intercommodel/getOriginalModelByIndex',
        async: false,
        data: {index: index},
        success: function (data) {
          data = JSON.parse(data);
          var result = data.obj[0];
          $('#videoAbility').val(intercomEdit.changeVal(result.videoAbility));
          $('#knobNum').val(result.knobNum);
          $('#videoFuncEnable').val(intercomEdit.changeVal(result.videoFuncEnable));
          $('#maxGroupNum').val(result.maxGroupNum);
          $('#chanlsNum').val(result.chanlsNum);
          $('#maxFriendNum').val(result.maxFriendNum);
          $('#interceptEnable').val(intercomEdit.changeVal(result.interceptEnable));
          $('#tempGroupEnable').val(intercomEdit.changeVal(result.tempGroupEnable));
          $('#videoCallEnable').val(intercomEdit.changeVal(result.videoCallEnable));
          $('#sensorAbility').val(intercomEdit.changeVal(result.sensorAbility));
          $('#videoConferenceEnable').val(intercomEdit.changeVal(result.videoConferenceEnable));
        }
      });
    },
    changeVal: function (num) {
      if (num == 1) {
        return '支持';
      } else if (num == 0) {
        return '不支持';
      }
    },
    doSubmits: function () {
      if (intercomEdit.validates()) {
        $('#EditForm').ajaxSubmit(function (data) {
          data = JSON.parse(data);
          if (data.success) {
            $('#commonWin').modal('hide');
            layer.msg('修改成功', {move: false});
            myTable.requestData();
          } else {
            layer.msg(data.msg);
          }
        });
      }
    },
    validates: function () {
      return $('#EditForm').validate({
        rules: {
          name: {
            required: true,
            isfenceName: true
          },
          modelId: {
            required: true,
            originalModelId: true
          }
        },
        messages: {
          name: {
            required: '对讲机型名称不能为空'
          },
          modelId: {
            required: '原始机型不能为空',
            originalModelId: '请选择原始机型'
          }
        }
      }).form();
    }
  };
  $(function () {
    intercomEdit.init();
    intercomEdit.getOriginalModelByIndex();
    $('input').inputClear().on('onClearEvent', function (e, data) {
      var id = data.id;
      if (id == 'modelId') {
        $('#videoAbility').val('');
        $('#knobNum').val('');
        $('#videoFuncEnable').val('');
        $('#maxGroupNum').val('');
        $('#chanlsNum').val('');
        $('#maxFriendNum').val('');
        $('#interceptEnable').val('');
        $('#tempGroupEnable').val('');
        $('#videoCallEnable').val('');
        $('#sensorAbility').val('');
        $('#videoConferenceEnable').val('');
      }
    });
    $('#doSubmitsEdit').bind('click', intercomEdit.doSubmits);
    jQuery.validator.addMethod('isfenceName', function (value, element) {
      var reg = /^[0-9a-zA-Z\u4e00-\u9fa5_]{2,50}$/;
      if (reg.test(value.trim())) {
        return true;
      }
      return false;
    }, '请输入汉字、字母、数字或下划线,长度2-50位');
  });
})(window, $);