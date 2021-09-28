(function (window, $) {
    editModel = {
        getBrandList: function () {
            var url = "/clbs/m/basicinfo/enterprise/brand/listBrand";
            var data = {"brandName": ""};
            json_ajax("POST", url, "json", false, data, editModel.brandListCallback);
        },
        brandListCallback: function (data) {
            if (data.success) {
                var vc = $("#brandName").attr("value");
                var result = data.records;
                var str = "";
                for (var i = 0; i < result.length; i++) {
                    if (vc == result[i].id) {
                        str += '<option selected="selected" value="' + result[i].id + '">' + html2Escape(result[i].brandName) + '</option>'
                    } else {
                        str += '<option value="' + result[i].id + '">' + html2Escape(result[i].brandName) + '</option>'
                    }
                }
                $("#brandName").html(str);
            }
        },
        validates: function () {
            return $("#editModelForm").validate({
                rules: {
                    modelName: {
                        required: true,
                        maxlength:32,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/enterprise/brand/repetitionEditModelName",
                            data: {
                                id: function () {
                                    return $("#mId").val();
                                },
                                name: function () {
                                    return $("#modelName").val();
                                },
                                brandId: function () {
                                    return $("#brandName").val();
                                }
                            }
                        }
                    }
                },
                messages: {
                    modelName: {
                        required: '请输入机型名称',
                        maxlength:'输入范围为1-32位',
                        remote: '该机型名称已存在'
                    }
                }
            }).form();
        },
        doSubmits: function () {
            if (editModel.validates()) {
                addHashCode($("#editModelForm"));
                $("#editModelForm").ajaxSubmit(function (data) {
                    var json = eval("(" + data + ")");
                    if (json.success) {
                        $("#commonSmWin").modal("hide");
                        layer.msg('修改成功！');
                        brandList.getModelList();
                    } else {
                        layer.msg(json.msg);
                    }
                });
            }
        },
    }
    $(function () {
        editModel.getBrandList();
        $('input').inputClear();
        $("#doSubmits").bind("click", editModel.doSubmits);

        $("#brandName").on("change",function () {
            $("#modelName-error").hide();
        })
    })
})(window, $)