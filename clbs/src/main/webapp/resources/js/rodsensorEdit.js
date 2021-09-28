(function (window, $) {
  editOilRodSensor = {
    validates: function () {
      return $("#editForm").validate({
        rules: {
          sensorNumber: {
            required: true,
            maxlength: 25,
            isRightSensorModel: true,
            //isRightfulString_oilBoxType: true,
            remote: {
              type: "post",
              async: false,
              url: "/clbs/m/basicinfo/equipment/rodsensor/repetition",
              dataType: "json",
              data: {
                username: function () {
                  return $("#sensorNumber").val();
                }
              },
              dataFilter: function (data, type) {
                var oldV = $("#scn").val();
                var newV = $("#sensorNumber").val();
                var data2 = data;
                if (oldV == newV) {
                  return true;
                } else {
                  if (data2 == "true") {
                    return true;
                  } else {
                    return false;
                  }
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
            required: true
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
    doSubmits: function () {
      if (editOilRodSensor.validates()) {
        addHashCode($("#editForm"));
        $("#editForm").ajaxSubmit(function (data) {
          var data = $.parseJSON(data);
          if (data.success) {
            $("#commonWin").modal("hide");
            if (data.msg == '' || data.msg == null) {
              data.msg = "修改成功！";
            }
            layer.msg(data.msg, {move: false});
            myTable.refresh();
          } else {
            layer.msg(data.msg, {move: false});
          }
        });
      }
    },
  }
  $(function () {
    $('input').inputClear();
    $("#doSubmitsEdit").bind("click", editOilRodSensor.doSubmits);
  })
})(window, $)