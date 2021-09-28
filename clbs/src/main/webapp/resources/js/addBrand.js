(function (window, $) {
    addBrand = {
        validates: function () {
            return $("#addBrandForm").validate({
                rules: {
                    brandName: {
                        required: true,
                        maxlength:32,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/enterprise/brand/repetitionAddBrandName",
                            data: {
                                name: function () {
                                    return $("#brandName").val();
                                }
                            }
                        }
                    }
                },
                messages: {
                    brandName: {
                        required: '请输入品牌名称',
                        maxlength:'输入范围为1-32位',
                        remote: '该品牌名称已存在'
                    }
                }
            }).form();
        },
        doSubmits: function () {
            if (addBrand.validates()) {
                addHashCode($("#addBrandForm"));
                $("#addBrandForm").ajaxSubmit(function (data) {
                    var json = eval("(" + data + ")");
                    if (json.success) {
                        $("#commonSmWin").modal("hide");
                        layer.msg('添加成功！');
                        brandList.getBrandList();
                    } else {
                        layer.msg(json.msg);
                    }
                });
            }
        },
    }
    $(function () {
        $('input').inputClear();
        $("#doSubmits").bind("click", addBrand.doSubmits);
    })
})(window, $)