(function (window, $) {
    var dataList = {
        value: []
    };
    dangerousTripEdit = {
        init: function () {
            laydate.render({elem: '#transportDate', theme: '#6dcff6', type: 'datetime'});

            var url = '/clbs/m/monitoring/vehicle/itemName/findList';
            json_ajax("GET", url, "json", false, null, function (data) {
                var selectVal = '<option value="">- 请选择品名 -</option>';
                var list = data.obj;
                var itemName = $("#itemNameId").attr("value");
                if (data.success && list.length > 0) {
                    for (var i = 0; i < list.length; i++) {
                        if (list[i].id == itemName) {
                            selectVal += '<option selected value="' + list[i].id + '" dangerType="' + list[i].dangerType + '" unit="' + list[i].unit + '">' + list[i].name + '</option>';
                        }
                        else {
                            selectVal += '<option value="' + list[i].id + '" dangerType="' + list[i].dangerType + '" unit="' + list[i].unit + '">' + list[i].name + '</option>'
                        }
                    }
                }
                $("#itemNameId").html(selectVal);
            });
        },
        validates: function () {
            return $("#editForm").validate({
                ignore: '',
                rules: {
                    vehicleId: {
                        required: true
                    },
                    itemNameId: {
                        required: true
                    },
                    count: {
                        min:1,
                        digits:true,
                        maxlength: 9
                    },
                    startSite: {
                        maxlength: 20
                    },
                    viaSite: {
                        maxlength: 20
                    },
                    aimSite: {
                        maxlength: 20
                    },
                    remark: {
                        maxlength: 50
                    }
                },
                messages: {
                    vehicleId: {
                        required: brandNull
                    },
                    itemNameId: {
                        required: productNameChknull
                    },
                    count: {
                        min:mustInt,
                        digits:mustInt,
                        maxlength: publicSize9
                    },
                    startSite: {
                        maxlength: publicSize20
                    },
                    viaSite: {
                        maxlength: publicSize20
                    },
                    aimSite: {
                        maxlength: publicSize20
                    },
                    remark: {
                        maxlength: publicSize50
                    }
                }
            }).form();
        },
        doSubmits: function () {
            if (dangerousTripEdit.validates()) {
                $("#editForm").ajaxSubmit(function (data) {
                    data = JSON.parse(data);
                    if (data.success) {
                        $("#commonWin").modal("hide");
                        if (data.msg != null && data.msg != '') {
                            layer.msg(data.msg);
                        }
                        subTable.refresh();
                    } else {
                        if (data.msg != null && data.msg != '')
                            layer.msg(data.msg);
                    }
                });
            }
        },
        //获取车牌号
        getBrandList: function () {
            var brandId = $("#vehicleId").val();
            var brandName = '';
            var url = '/clbs/m/basicinfo/monitoring/vehicle/findTransportList';
            json_ajax("GET", url, "json", false, null, function (data) {
                if (data.success && data.obj.length > 0) {
                    for (var i = 0; i < data.obj.length; i++) {
                        dataList.value.push({
                            name: data.obj[i].brand,
                            id: data.obj[i].id
                        });
                        if (data.obj[i].id == brandId) {
                            brandName = data.obj[i].brand;
                        }
                    }
                    $("#brand").val(brandName);
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
        },
        //根据品名变化获取危险品类别与单位
        itemNameChange: function () {
            var dangerType = $("#itemNameId").find(":selected").attr("dangerType");
            var unit = $("#itemNameId").find(":selected").attr("unit");
            if (dangerType == 'null' || dangerType == undefined)
                dangerType = '';
            if (unit == 'null' || unit == undefined)
                unit = '';
            $("#dangerType").val(dangerType);
            $("#unit").val(unit);
        },
        //获取押运员
        getProfessinoal: function () {
            var url = '/clbs/m/monitoring/vehicle/transport//findProfessionalsInfoList';
            json_ajax("GET", url, "json", false, null, function (data) {
                var selectVal = '<option value="">- 请选择从业人员 -</option>';
                var list = data.obj;
                var professinoalName = $("#professinoalId").attr("value");
                if (data.success && list.length > 0) {
                    for (var i = 0; i < list.length; i++) {
                        if (list[i].id == professinoalName) {
                            selectVal += '<option value="' + list[i].id + '" selected phone="' + list[i].phone + '" card="' + list[i].cardNumber + '">' + list[i].name + '</option>'
                        }
                        else {
                            selectVal += '<option value="' + list[i].id + '" phone="' + list[i].phone + '" card="' + list[i].cardNumber + '">' + list[i].name + '</option>'
                        }
                    }
                }
                $("#professinoalId").html(selectVal);
            });
        },
        //根据押运员变化获取资格证号与电话
        professinoalChange: function () {
            var phone = $("#professinoalId").find(":selected").attr("phone");
            var card = $("#professinoalId").find(":selected").attr("card");
            if (phone == 'null' || phone == undefined)
                phone = '';
            if (card == 'null' || card == undefined)
                card = '';
            $("#card").val(card);
            $("#phone").val(phone);
        }
    }
    $(function () {
        dangerousTripEdit.init();
        dangerousTripEdit.getBrandList();
        dangerousTripEdit.getProfessinoal();
        dangerousTripEdit.itemNameChange();
        dangerousTripEdit.professinoalChange();
        $('input').inputClear().on('onClearEvent',function(e,data){
            var id = data.id;
            if(id == 'brand'){
                $("#vehicleId").attr("value", '')
            }
        });
        $("#brand").on('input propertychange',function () {
            $("#vehicleId").attr("value", "");
        });
        $("#tripDoSubmitsEdit").bind("click", dangerousTripEdit.doSubmits);

        $("#itemNameId").on("change", dangerousTripEdit.itemNameChange);
        $("#professinoalId").on("change", dangerousTripEdit.professinoalChange);
    })
})(window, $)