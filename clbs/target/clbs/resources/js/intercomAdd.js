(function (window, $) {
  intercomAdd = {
    init: function () {
      var index = $('#index').val();
      var getModels = '';
      $.ajax({
        type: 'post',
        async: false,
        url: '/clbs/talkback/intercomplatform/intercommodel/getAllOriginalModel',
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
        indexId: 1,  //data.value 的第几个数据，作为data-id的内容
        indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
        data: dataList,
        effectiveFields: ['name']
      }).on('onDataRequestSuccess', function (e, result) {
      }).on('onSetSelectValue', function (e, keyword, data) {
        $('#index').val(keyword.id);
        intercomAdd.getOriginalModelByIndex();
      }).on('input propertychange', function () {
        $('#index').val('');
      }).on('blur', function () {
        if (!$('#index').val()) {
          $('#modelId').val('');
        }
      });

      if (dataList.value.length > 0) {
        $('#modelId').val(dataList.value[0].name);
        $('#index').val(dataList.value[0].id);
      }
    },
    //原始机型信息
    getOriginalModelByIndex: function () {
      var index = $('#index').val();
      $.ajax({
        type: 'post',
        async: false,
        url: '/clbs/talkback/intercomplatform/intercommodel/getOriginalModelByIndex',
        data: {index: index},
        success: function (data) {
          data = JSON.parse(data);
          var result = data.obj[0];
          $('#videoAbility').val(intercomAdd.changeVal(result.videoAbility));
          $('#knobNum').val(result.knobNum);
          $('#videoFuncEnable').val(intercomAdd.changeVal(result.videoFuncEnable));
          $('#maxGroupNum').val(result.maxGroupNum);
          $('#chanlsNum').val(result.chanlsNum);
          $('#maxFriendNum').val(result.maxFriendNum);
          $('#interceptEnable').val(intercomAdd.changeVal(result.interceptEnable));
          $('#tempGroupEnable').val(intercomAdd.changeVal(result.tempGroupEnable));
          $('#videoCallEnable').val(intercomAdd.changeVal(result.videoCallEnable));
          $('#sensorAbility').val(intercomAdd.changeVal(result.sensorAbility));
          $('#videoConferenceEnable').val(intercomAdd.changeVal(result.videoConferenceEnable));
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
      if (intercomAdd.validates()) {
        $('#addForm').ajaxSubmit(function (data) {
          var json = eval('(' + data + ')');
          if (json.success) {
            $('#commonWin').modal('hide');
            layer.msg(data.msg || '添加成功');
            myTable.requestData();
          } else {
            layer.msg(json.msg);
          }
        });
      }
    },
    validates: function () {
      return $('#addForm').validate({
        rules: {
          name: {
            required: true,
            isfenceName: true
          },
          modelId: {
            required: true
          }
        },
        messages: {
          name: {
            required: '对讲机型名称不能为空'
          },
          modelId: {
            required: '请选择原始机型'
          }
        }
      }).form();
    }
  };
  $(function () {
    intercomAdd.init();
    intercomAdd.getOriginalModelByIndex();
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
    $('#doSubmitsAdd').bind('click', intercomAdd.doSubmits);
    //验证对讲机型名称
    jQuery.validator.addMethod('isfenceName', function (value, element) {
      var reg = /^[0-9a-zA-Z\u4e00-\u9fa5_]{2,50}$/;
      if (reg.test(value.trim())) {
        return true;
      }
      return false;
    }, '请输入汉字、字母、数字或下划线,长度2-50位');
  });
})(window, $);