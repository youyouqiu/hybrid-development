(function (window, $) {
    productNameAdd = {
        validates: function () {
            return $("#proAddForm").validate({
                rules: {
                    name: {
                        required: true,
                        maxlength: 20,
                        remote: {
                            type: "get",
                            async: false,
                            url: "/clbs/m/monitoring/vehicle/itemName/chechItem",
                            data: {
                                name: function () {
                                    return $("#name").val();
                                }
                            }
                        }
                    },
                    remark: {
                        maxlength: 50
                    }
                },
                messages: {
                    name: {
                        required: productNameNull,
                        maxlength: publicSize20,
                        remote: productNameExists
                    },
                    remark: {
                        maxlength: publicSize50
                    }
                }
            }).form();
        },
        doSubmits: function () {
            if (productNameAdd.validates()) {
                addHashCode1($("#proAddForm"));
                $("#proAddForm").ajaxSubmit(function (data) {
                    data = JSON.parse(data);
                    if (data.success) {
                        $("#commonSmWin").modal("hide");
                        if (data.msg != null && data.msg != '')
                            layer.msg(data.msg);
                        myTable.requestData();
                    } else {
                        if (data.msg != null && data.msg != '')
                            layer.msg(data.msg);
                    }
                });
            }
        },
    }
    $(function () {
        $('input').inputClear();
        $("#proDoSubmits").bind("click", productNameAdd.doSubmits);
    })
})(window, $)