(function (window, $) {
    addModel = {
        getBrandList: function () {
            var url = "/clbs/m/basicinfo/enterprise/brand/listBrand";
            var data = {"brandName": ""};
            json_ajax("POST", url, "json", false, data, addModel.brandListCallback);
        },
        brandListCallback: function (data) {
            if (data.success) {
                var result=data.records;
                var str="";
                for(var i=0;i<result.length;i++){
                    str+='<option value="'+result[i].id+'">'+html2Escape(result[i].brandName)+'</option>'
                }
                $("#brandName").html(str);
            }
        },
        validates: function () {
            return $("#addModelForm").validate({
                rules: {
                    modelName: {
                        required: true,
                        maxlength:32,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/enterprise/brand/repetitionAddModelName",
                            data: {
                                name: function () {
                                    return $("#modelName").val();
                                },
                                brandId: function () {
                                    return $("#brandName").val();
                                }
                            }
                        }
                    },
                    brandId:{
                        required: true,
                    }
                },
                messages: {
                    modelName: {
                        required: '请输入机型名称',
                        maxlength:'输入范围为1-32位',
                        remote: '该机型名称已存在'
                    },
                    brandId:{
                        required: '请选择品牌',
                    }
                }
            }).form();
        },
        doSubmits: function () {
            if (addModel.validates()) {
                addHashCode($("#addModelForm"));
                $("#addModelForm").ajaxSubmit(function (data) {
                    var json = eval("(" + data + ")");
                    if (json.success) {
                        $("#commonSmWin").modal("hide");
                        layer.msg('添加成功！');
                        brandList.getModelList();
                    } else {
                        layer.msg(json.msg);
                    }
                });
            }
        },
    }
    $(function () {
        addModel.getBrandList();
        $('input').inputClear();
        $("#doSubmits").bind("click", addModel.doSubmits);
        
        $("#brandName").on("change",function () {
            $("#modelName-error").hide();
        })
    })
})(window, $)