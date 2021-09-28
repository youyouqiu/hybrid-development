(function (window, $) {
    addVehicleType = {
        init: function () {
            var url = "/clbs/m/basicinfo/monitoring/vehicle/type/listCategory";
            var data = {"vehicleCategory": ""}
            json_ajax("POST", url, "json", false, data, addVehicleType.categoryCallBack);
        },
        categoryCallBack: function (data) {
            var result = data.records;
            var str = "";
            for (var i = 0; i < result.length; i++) {
                str += '<option  value="' + result[i].id + '">' + result[i].vehicleCategory + '</option>'
            }
            $("#vehicleCategory1").html(str);
        },
        validates: function () {
            return $("#addForm").validate({
                rules: {
                    vehicleCategory: {
                        maxlength: 20
                    },
                    vehicleType: {
                        required: true,
                        maxlength: 20,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/monitoring/vehicle/type/repetition",
                            data: {
                                vehicleType: function () {
                                    return $("#vehicleType").val();
                                },
                                category: function () {
                                    return $("#vehicleCategory1 option:selected").text();
                                }
                            }
                        }
                    },
                    serviceCycle: {
                        positiveInteger: true
                    },
                    description: {
                        maxlength: 50
                    }
                },
                messages: {
                    vehicleCategory: {
                        maxlength: publicSize20
                    },
                    vehicleType: {
                        required: vehicleTypeNull,
                        maxlength: publicSize20,
                        remote: vehicleTypeExists
                    },
                    serviceCycle: {
                        positiveInteger: '请输入不超过5位的正整数'
                    },
                    description: {
                        maxlength: publicSize50
                    },
                }
            }).form();
        },
        doSubmits: function () {
            if (addVehicleType.validates()) {
                addHashCode1($("#addForm"));
                $("#addForm").ajaxSubmit(function (data) {
                    var json = eval("(" + data + ")");
                    if (json.success) {
                        $("#commonSmWin").modal("hide");
                        myTable.requestData();
                    } else {
                        layer.msg(json.msg);
                    }
                });
            }
        },
    }
    $(function () {
        addVehicleType.init();
        $('input').inputClear();
        $("#doSubmits").bind("click", addVehicleType.doSubmits);
    })
})(window, $)