(function(window, $){
    CategoriesEdit={
        doSubmits:function (){
            if(CategoriesEdit.validates()){
                $("#editForm").ajaxSubmit(function (data){
                    var json = eval("(" + data + ")");
                    if(json.success){
                        $("#commonSmWin").modal("hide");
                        layer.msg("修改成功！", {move: false});
                        myTable.requestData();
                        modelTable.refresh();
                    }
                })
            }
        },
        validates:function() {
            return $("#editForm").validate({
                rules:{
                    name:{
                        required:true,
                        isCategories:true,
                        maxlength:6,
                        remote:{
                            type:"post",
                            url:"/clbs/talkback/basicinfo/skill/checkCategoriesName",
                            async:false,
                            data:{
                                id:function (){
                                    return $("#editId").val();
                                },
                                name:function (){
                                    return $("#CategoriesName").val();
                                }
                            }
                        }
                    }
                },
                messages:{
                    name:{
                        required:"技能类别不能为空",
                        remote:"此类别与已有的技能类别同名，请重新输入",
                        maxlength:"技能类别不能超过6位"
                    }
                }
          }).form();
        }
    };
    $(function(){
        $('input').inputClear();
        $("#doSubmitEdit").bind("click",CategoriesEdit.doSubmits);
        // 判断技能列表名称
        jQuery.validator.addMethod("isCategories", function (value, element) {
            return this.optional(element) || /^[A-Za-z0-9\u4e00-\u9fa5\-]+$/.test(value);
        }, "技能类别可支持汉字、字母、数字或短横杠");
    })
})(window, $);