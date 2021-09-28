(function (window, $) {
    editVehicleType = {
        init: function () {
            var url = "/clbs/m/basicinfo/monitoring/vehicle/type/listCategory";
            var data = {"vehicleCategory": ""}
            json_ajax("POST", url, "json", false, data, editVehicleType.categoryCallBack);
        },
        categoryCallBack: function (data) {
            var vc = $("#vehicleCategory1").attr("value");
            var result = data.records;
            var str = "";
            for (var i = 0; i < result.length; i++) {
                if (vc == result[i].id) {
                    str += '<option 	selected="selected"  value="' + result[i].id + '">' + result[i].vehicleCategory + '</option>'
                } else {
                    str += '<option  value="' + result[i].id + '">' + result[i].vehicleCategory + '</option>'
                }

            }
            $("#vehicleCategory1").html(str);
        },
        validates: function () {
            return $("#editForm").validate({
                rules: {
                    vehicleCategory: {
                        maxlength: 20
                    },
                    vehicleType: {
                        maxlength: 20,
                        required: true,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/monitoring/vehicle/type/repetition",
                            dataType: "json",
                            data: {
                                vehicleType: function () {
                                    return $("#vehicleType").val();
                                },
                                category: function () {
                                    return $("#vehicleCategory1 option:selected").text();
                                },
                                vid: function () {
                                    return $("#vId").val();
                                }
                            },
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
            if (editVehicleType.validates()) {
                addHashCode1($("#editForm"));
                $("#editForm").ajaxSubmit(function (data) {
                    var json = eval("(" + data + ")");
                    if (json.success) {
                        $("#commonSmWin").modal("hide");
                        myTable.refresh();
                    } else {
                        layer.msg(json.msg);
                    }
                });
            }
        },
    }
    $(function () {
        editVehicleType.init();
        $('input').inputClear();
        $("#doSubmits").bind("click", editVehicleType.doSubmits);
    })
})(window, $)