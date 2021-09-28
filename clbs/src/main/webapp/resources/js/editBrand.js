(function (window, $) {
    editBrand = {
        validates: function () {
            return $("#editBrandForm").validate({
                    rules: {
                        brandName: {
                            required: true,
                            maxlength:32,
                            remote: {
                                type: "post",
                                async: false,
                                url: "/clbs/m/basicinfo/enterprise/brand/repetitionEditBrandName",
                                data: {
                                    id: function () {
                                        return $("#bId").val();
                                    },
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
                }
            ).form();
        },
        doSubmits: function () {
            if (editBrand.validates()) {
                addHashCode($("#editBrandForm"));
                $("#editBrandForm").ajaxSubmit(function (data) {
                    var json = eval("(" + data + ")");
                    if (json.success) {
                        $("#commonSmWin").modal("hide");
                        layer.msg('修改成功！');
                        brandList.getBrandList();
                    } else {
                        layer.msg(json.msg);
                    }
                });
            }
        }
    }
    $(function () {
        $('input').inputClear();
        $("#doSubmits").bind("click", editBrand.doSubmits);
    })
})
(window, $)