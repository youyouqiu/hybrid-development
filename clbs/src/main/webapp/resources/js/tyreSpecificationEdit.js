(function(window,$){
	editTyreSpecification = {
		doSubmits: function(){
            if(!editTyreSpecification.validates()) return;
            editTyreSpecification.hideErrorMsg();
            if ($.trim($("#tireSpecifications").val()) == "") {
                editTyreSpecification.showErrorMsg("轮胎规格不能为空！", "tireSpecifications");
                return;
            }
            if ($("#tireSpecifications").val().length>25) {
                editTyreSpecification.showErrorMsg("轮胎规格请控制在25个字以内！", "tireSpecifications");
                return;
            }
            if ($.trim($("#tireRollingTadius").val()) == "") {
                editTyreSpecification.showErrorMsg("轮胎滚动半径不能为空！", "tireRollingTadius");
                return;
            }
            if (!/^[A-Za-z0-9_#\*\u4e00-\u9fa5\-\.\/]+$/.test($("#tireSpecifications").val())) {
                addTyreSpecification.showErrorMsg("请输入中文、字母、数字或特殊符号*、-、_、#、/、.", "tireSpecifications");
                return;
            }
            var tireRollingTadius = $("#tireRollingTadius").val();
            tireRollingTadius = tireRollingTadius.replace(/[^\d]/g,""); //清除"数字"和"."以外的字符
            $("#tireRollingTadius").val(tireRollingTadius);
            if ($("#tireRollingTadius").val()>65535) {
                editTyreSpecification.showErrorMsg("轮胎滚动半径请控制在65535以内！", "tireRollingTadius");
                return;
            }
            addHashCode($("#editForm"));
            $("#editForm").ajaxSubmit(function (data) {
                data = JSON.parse(data);
                if(data.success){
                    $("#commonSmWin").modal("hide");
                    layer.msg("设置成功！",{move:false});
                    //关闭弹窗
                    myTable.refresh();
                }else if(!data.success && data.msg.toString().indexOf("轮胎规格已被设置") > -1) {
                    editTyreSpecification.showErrorMsg(data.msg, "identId");
                    return;
                }else if(!data.success && data.msg.toString().indexOf("系统错误") > -1){
                	layer.msg(data.msg,{move:false});
                }
            });
        },
        showErrorMsg: function(msg, inputId){
            if ($("#error_label_edit").is(":hidden")) {
                $("#error_label_edit").text(msg);
                $("#error_label_edit").insertAfter($("#" + inputId));
                $("#error_label_edit").show();
            } else {
                $("#error_label_edit").is(":hidden");
            }
        },
        validates: function(){
            return $("#editForm").validate({
                rules: {
                    sizeName: {
                        required: true,
                        maxlength : 25,
                    },
                },
                messages: {
                    sizeName: {
                        required : tyreSpecificationNull,
                        maxlength : tyreSpecificationLenth,
                    },
                }
            }).form();
        },
        //错误提示信息隐藏
        hideErrorMsg: function(){
            $("#error_label_edit").hide();
        },
        checkNum: function(obj){
            obj.value = obj.value.replace(/[^\d]/g,""); //清除"数字"和"."以外的字符
        }
	}
	$(function(){
		$('input').inputClear();
        $("#doSubmits").bind("click",editTyreSpecification.doSubmits);
	})
})(window,$)