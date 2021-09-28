(function (window, $) {
    productNameEdit = {
        validates: function () {
            return $("#proEditForm").validate({
                rules: {
                    name: {
                        required: true,
                        maxlength: 20,
                        remote: {
                            type: "get",
                            async: false,
                            url: "/clbs/m/monitoring/vehicle/itemName/chechItemById",
                            data: {
                                name: function () {
                                    return $("#productName").val();
                                },
                                id: function () {
                                    return $("#pid").val();
                                }
                            },
                            dataFilter: function (data, type) {
                                var oldV = $("#oldName").val();
                                var newV = $("#productName").val();
                                console.log(oldV, newV, data);
                                if (oldV == newV) {
                                    return true;
                                } else {
                                    if (data == "true") {
                                        return true;
                                    } else {
                                        return false;
                                    }
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
            if (productNameEdit.validates()) {
                addHashCode1($("#proEditForm"));
                $("#proEditForm").ajaxSubmit(function (data) {
                    data = JSON.parse(data);
                    if (data.success) {
                        $("#commonSmWin").modal("hide");
                        if (data.msg != null && data.msg != '') {
                            layer.msg(data.msg);
                        }
                        myTable.refresh();
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
        $("#editDoSubmits").bind("click", productNameEdit.doSubmits);
    })
})(window, $)