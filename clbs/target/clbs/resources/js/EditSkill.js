(function(window, $){
    EditSkill={
        doSubmits:function (){
            if(EditSkill.validates()){
                $("#editForm").ajaxSubmit(function(data){
                    data = JSON.parse(data);
                    if(data.success){
                        $("#commonSmWin").modal("hide");
                        layer.msg("修改成功!", {move:false});
                        modelTable.requestData();
                    }
                })
            }
        },
        validates:function (){
            return $("#editForm").validate({
                rules:{
                    name:{
                        required:true,
                        isCategories:true,
                        maxlength:6,
                        remote:{
                            type:"post",
                            url:"/clbs/talkback/basicinfo/skill/checkSkillName",
                            async:false,
                            data:{
                                name:function (){
                                    return $("#skillName").val();
                                },
                                id:function (){
                                    return $("#editId").val();
                                }
                            }
                        }
                    }
                },
                messages:{
                    name:{
                        required:"技能名称不能为空",
                        remote:"此名称与已有的技能同名，请重新输入",
                        maxlength:"技能名称不能超过6位"
                    }
                }
            }).form()
        }
    };
    $(function () {
        $('input').inputClear();
        $("#doSubmitEdit").bind("click",EditSkill.doSubmits);
        // 判断技能列表名称
        jQuery.validator.addMethod("isCategories", function (value, element) {
            return this.optional(element) || /^[A-Za-z0-9\u4e00-\u9fa5\-]+$/.test(value);
        }, "技能名称可支持汉字、字母、数字或短横杠");
    })
})(window,$);