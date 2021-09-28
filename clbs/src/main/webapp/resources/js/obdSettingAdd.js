(function (window, $) {
    var obdTypeList = JSON.parse($("#obdTypeList").val());// 车型名称/发动机类型list

    // 参考对象list
    var referentList = JSON.parse($("#referent").val());
    var dataReferentList = {value: []};

    obdSettingAdd = {
        init: function () {
            obdSettingAdd.initSelect(1);
            obdSettingAdd.initReferent();
        },
        initReferent: function () {
            if (referentList != null && referentList.length > 0) {
                for (var i = 0; i < referentList.length; i++) {
                    var item = referentList[i];
                    var flux = {};
                    flux.id = item.id;
                    flux.name = item.brand;
                    flux.time = item.time;
                    dataReferentList.value.push(flux);
                }
            }

            $("#referentList").bsSuggest({
                indexId: 1,
                indexKey: 0,
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: dataReferentList,
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data) {
                var id = keyword.id;
                for(var i = 0; i < dataReferentList.value.length; i++){
                    var item = dataReferentList.value[i];
                    if (item.id == id) {
                        $('#time').val(item.time);
                    }
                }
                for (var i = 0; i < referentList.length; i++) {
                    var item = referentList[i];
                    if (item.id == id) {
                        $('#deviceType').val(item.vehicleType);
                        $("#deviceType").trigger('change');
                        $('#typeList').val(item.obdVehicleTypeId);
                        $("#typeList").trigger('change');
                    }
                }
                $("#error_label_add").hide();
            }).on('onUnsetSelectValue', function () {
            });
        },
        initSelect: function (cate) {
            var html = '';
            var obdType = [];

            for (var i = 0, len = obdTypeList.length; i < len; i++) {
                var item = obdTypeList[i];
                if (item.type == cate) {
                    obdType.push(item);
                    html += '<option value="' + item.id + '" data-id="' + item.code + '">' + item.name + '</option>'
                }
            }

            $('#typeList').html(html);
            $('#vehicleTypeId').val(obdType[0].code != null ? obdType[0].code : "");
        },
        typeChange: function () {
            var code = $('#typeList option:selected').data('id');
            $('#vehicleTypeId').val(code != null ? code : "");
        },
        // 提交
        doSubmit: function () {
            if (obdSettingAdd.validates()) {
                if($("#typeList").val() == null || $("#typeList").val() == ""){
                    layer.msg("请选择车型/发动机类型");
                    return;
                }
                addHashCode($("#bindForm"));
                $("#bindForm").ajaxSubmit(function (data) {
                    if (data !== null) {
                        var result = JSON.parse(data);
                        if (result.success) {
                            $("#commonWin").modal("hide");
                            myTable.refresh();
                        } else if (result.msg) {
                            layer.msg(result.msg, {move: false});
                        }
                    }
                });
            }
        },
        //表单验证
        showErrorMsg: function (msg, inputId) {
            if ($("#error_label_add").is(":hidden")) {
                $("#error_label_add").text(msg);
                $("#error_label_add").insertAfter($("#" + inputId));
                $("#error_label_add").show();
            } else {
                $("#error_label_add").is(":hidden");
            }
        },
        hideErrorMsg: function () {
            $("#error_label_add").is(":hidden");
            $("#error_label_add").hide();
        },
        validates: function () {
            return $("#bindForm").validate({
                rules: {
                    time: {
                        isInteger: true,
                        range: [1, 10],
                        maxlength: 2
                    },
                },
                messages: {
                    time: {
                        isInteger: "请输入1-10之间的整数",
                        range: "请输入1-10之间的整数"
                    },
                }
            }).form();
        },
        //车辆类型切换
        deviceTypeChange: function () {
            var group = $('#deviceNameGroup'),
                nameInput = $('#name');
            var deviceType = $(this).val();
            if (deviceType == 0) {
                group.removeClass('typeGroup');
                nameInput.attr('placeholder', '请输入车型名称');
            } else {
                group.addClass('typeGroup');
                nameInput.attr('placeholder', '请输入发动机类型');
            }
            obdSettingAdd.initSelect(deviceType);
            $('#name-error').text('').hide();
        }
    };
    $(function () {
        $('input').inputClear();
        obdSettingAdd.init();
        $("#deviceType").on("change", obdSettingAdd.deviceTypeChange);
        $("#doSubmit").bind("click", obdSettingAdd.doSubmit);
        $("#typeList").on("change", obdSettingAdd.typeChange);
    })
})(window, $);