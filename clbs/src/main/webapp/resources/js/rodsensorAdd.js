(function (window, $) {
  addOilRodSensor = {
    validates: function () {
      return $("#addForm").validate({
        rules: {
          sensorNumber: {
            required: true,
            maxlength: 25,
            isRightSensorModel: true,
            remote: {
              type: "post",
              async: false,
              url: "/clbs/m/basicinfo/equipment/rodsensor/repetition",
              data: {
                username: function () {
                  return $("#sensorNumber").val();
                }
              }
            }
          },
          sensorLength: {
            required: true,
            isIntGtZero: true,
            maxlength: 5
          },
          filteringFactor: {
            required: true
          },
          baudRate: {
            required: true
          },
          oddEvenCheck: {
            required: true
          },
          compensationCanMake: {
            required: true,
          }
        },
        messages: {
          sensorNumber: {
            required: oilSensorNumberNull,
            maxlength: sensorModelError,
            // isRightfulString_oilBoxType: oilSensorTypeError,
            remote: oilSensorNumberExists
          },
          sensorLength: {
            required: oilSensorLengthNull,
            isIntGtZero: numberDoubleError,
            maxlength: oilSensorNumberMaxlength
          },
          filteringFactor: {
            required: oilFilteringFactorNull
          },
          baudRate: {
            required: oilBaudRateNull
          },
          oddEvenCheck: {
            required: oilOddEvenCheckNull
          },
          compensationCanMake: {
            required: oilCompensationCanMakeNull
          }
        }
      }).form();
    },
    //提交
    doSubmits: function () {
      if (addOilRodSensor.validates()) {
        addHashCode($("#addForm"));
        $("#addForm").ajaxSubmit(function (data) {
          var data = $.parseJSON(data)
          if (data.success) {
            $("#commonWin").modal("hide");
            layer.msg(data.msg, {move: false});
            myTable.requestData();
          } else {
            layer.msg(data.msg, {move: false});
          }
        });
      }
    },
  }
  $(function () {
    $('input').inputClear();
    $("#doSubmitsAdd").bind("click", addOilRodSensor.doSubmits);
  })
})(window, $)