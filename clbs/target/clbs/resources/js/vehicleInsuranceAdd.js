(function (window, $) {
  var dataList = {
    value: []
  };
  addVehicleInsurance = {
    init: function () {
      laydate.render({elem: '#startTime', theme: '#6dcff6'});
      laydate.render({elem: '#endTime', theme: '#6dcff6'});
    },
    validates: function () {
      return $("#addForm").validate({
        ignore: '',
        rules: {
          insuranceId: {
            required: true,
            isRightfulStr: true,
            maxlength: 30,
            remote: {
              type: "post",
              async: false,
              url: "/clbs/m/basicinfo/monitoring/vehicle/insurance/checkRepeat",
              data: {
                insuranceId: function () {
                  return $("#insuranceId").val();
                }
              }
            }
          },
          vehicleId: {
            required: true
          },
          insuranceType: {
            maxlength: 50
          },
          company: {
            maxlength: 50
          },
          preAlert: {
            digits: true,
            range: [1, 60]
          },
          amountInsured: {
            digits: true,
            min: 1,
            maxlength: 9
          },
          discount: {
            range: [0, 100],
            decimalTwo: true
          },
          actualCost: {
            min: 0,
            maxlength: 9,
            decimalOne: true
          },
          agent: {
            maxlength: 10
          },
          phone: {
            isTel: true
          },
          remark: {
            maxlength: 50
          },
          endTime: {
            compareDate: "#startTime"
          }
        },
        messages: {
          insuranceId: {
            required: vehicleInsuranceId,
            maxlength: publicSize30,
            remote: insuranceIdExists
          },
          vehicleId: {
            required: vehicleBrand
          },
          insuranceType: {
            maxlength: publicSize50
          },
          company: {
            maxlength: publicSize50
          },
          preAlert: {
            digits: mustInt,
            range: vehiclePreAlert
          },
          amountInsured: {
            digits: mustInt,
            min: mustInt,
            maxlength: publicSize9
          },
          discount: {
            range: vehicleDiscount
          },
          actualCost: {
            min: mustPositiveNumber,
            maxlength: publicSize9
          },
          agent: {
            maxlength: publicSize10
          },
          remark: {
            maxlength: publicSize50
          },
          endTime: {
            compareDate: "到期时间必须大于开始时间！"
          }
        }
      }).form();
    },
    doSubmits: function () {
      if (addVehicleInsurance.validates()) {
        addHashCode1($("#addForm"));
        $("#addForm").ajaxSubmit(function (data) {
          data = JSON.parse(data);
          if (data.success) {
            $("#commonWin").modal("hide");
            layer.msg('添加成功！');
            myTable.requestData();
          } else {
            if (data.msg != null)
              layer.msg(data.msg);
          }
        });
      }
    },
    getBrandList: function () {
      var url = '/clbs/m/basicinfo/monitoring/vehicle/insurance/findVehicleList';
      json_ajax("GET", url, "json", false, null, function (data) {
        if (data.success && data.obj.length > 0) {
          for (var i = 0; i < data.obj.length; i++) {
            dataList.value.push({
              name: data.obj[i].brand,
              id: data.obj[i].id
            });
          }
        }
        $("#objectMonitoring").removeClass('disabled loading-state-button').find('i').attr("class", 'caret');
      });
      $("#brand").bsSuggest("destroy"); // 销毁事件

      $("#brand").bsSuggest({
        indexId: 1, //data.value 的第几个数据，作为input输入框的内容
        indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
        keyField: "name",
        effectiveFields: ["name"],
        searchFields: ["name"],
        data: dataList
      }).on('onDataRequestSuccess', function (e, result) {
      }).on('onSetSelectValue', function (e, keyword, data) {
        $("#vehicleId").attr("value", keyword.id);
      }).on('onUnsetSelectValue', function () {
        $("#vehicleId").attr("value", "");
      });
      setTimeout(function () {
        $("#brand").blur();
      }, 300);
    }
  };

  $(function () {
    addVehicleInsurance.init();
      $('input').inputClear().on('onClearEvent',function(e,data){
          var id = data.id;
          if(id === 'brand'){
              $("#vehicleId").attr("value", '')
          }
      });
      $("#brand").on('input propertychange',function () {
          $("#vehicleId").attr("value", "");
      });
    addVehicleInsurance.getBrandList();
    $("#addDoSubmits").bind("click", addVehicleInsurance.doSubmits);
  })
})(window, $);