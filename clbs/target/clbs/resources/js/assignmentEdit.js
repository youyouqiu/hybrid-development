(function($,window){
    var assignmentEdit = {
        doSubmit: function(){
            if(assignmentEdit.validates()){
                addHashCode($("#editForm"));
                $("#editForm").ajaxSubmit(function(data) {
                    var result = JSON.parse(data);
                    if (result.success){
                        $("#commonLgWin").modal("hide");
                        /* 关闭弹窗 */
                        myTable.refresh();
                    } else{
                        layer.msg(result.msg);
                    }
                });
            };
        },
        validates: function(){
            return $("#editForm").validate({
                rules : {
                    name : {
                        required : true,
                        maxlength : 30,
                        remote: {
                            type:"post",
                            async:false,
                            url:"/clbs/m/basicinfo/enterprise/assignment/repetition",
                            dataType:"json",
                            data:{
                                name:function(){return $("#name").val();},
                                group:function(){return $("#groupId").val();},
                                assignmentId:function(){return $("#assignmentId").val();}
                            }
                        }
                    },
                    groupName : {
                        required : true,
                        maxlength : 1000
                    },
                    contacts : {
                        maxlength : 20
                    },
                    telephone : {
                        isLandline : true
                    },
                    description : {
                        maxlength : 50
                    }
                },
                messages: {
                    name : {
                        required : groupNameNull,
                        maxlength : publicSize30Length,
                        remote: assignmentExists
                    },
                    groupName : {
                        required : groupNameNull,
                        maxlength :publicSize1000Length
                    },
                    contacts : {
                        maxlength :publicSize20Length
                    },
                    telephone : {
                        isLandline :  telPhoneError
                    },
                    description : {
                        maxlength :publicSize50Length
                    }
                }
            }).form();
        },
    }
    $(function(){
        $('input').inputClear();
        $("#doSubmitEdit").on("click",assignmentEdit.doSubmit);
    })
})($,window)