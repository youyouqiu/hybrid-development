(function (window, $) {
    var dataList = {
        value: []
    };
    addVehicleTrave = {
        init: function () {
            laydate.render({elem: '#startTime', theme: '#6dcff6', type: 'datetime'});
            laydate.render({elem: '#endTime', theme: '#6dcff6', type: 'datetime'});
        },
        validates: function () {
            return $("#addForm").validate({
                ignore: '',
                rules: {
                    travelId: {
                        required: true,
                        isRightfulStr: true,
                        maxlength: 20,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/cb/vehicle/travel/isRepeateTravelId",
                            data: {
                                travelId: function () {
                                    return $("#travelId").val();
                                }
                            },
                            dataFilter: function (data, type) {
                                return data == 'false' ? true : false;
                            }
                        }
                    },
                    vehicleId: {
                        required: true
                    },
                    endTime: {
                        compareDate: "#startTime"
                    },
                    address: {
                        maxlength: 20
                    },
                    travelContent: {
                        maxlength: 500
                    },
                    remark: {
                        maxlength: 50
                    }
                },
                messages: {
                    travelId: {
                        required: travelIdNull,
                        maxlength: publicSize20,
                        remote: travelIdExists
                    },
                    vehicleId: {
                        required: vehicleBrand
                    },
                    endTime: {
                        compareDate: vehicleTime
                    },
                    address: {
                        maxlength: publicSize20
                    },
                    travelContent: {
                        maxlength: publicSize500
                    },
                    remark: {
                        maxlength: publicSize50
                    }
                }
            }).form();
        },
        doSubmits: function () {
            if (addVehicleTrave.validates()) {
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
            var url = '/clbs/m/basicinfo/monitoring/vehicle/getAllBuses';
            json_ajax("POST", url, "json", false, null, function (data) {
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
        }
    }
    $(function () {
        addVehicleTrave.init();
        addVehicleTrave.getBrandList();
        $('input').inputClear().on('onClearEvent',function(e,data){
            var id = data.id;
            if(id == 'brand'){
                $("#vehicleId").attr("value", '')
            }
        });
        $("#brand").on('input propertychange',function () {
            $("#vehicleId").attr("value", "");
        });
        $("#doAddSubmits").bind("click", addVehicleTrave.doSubmits);
    })
})(window, $)