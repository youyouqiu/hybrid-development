(function(window,$){
	addTyreSpecification = {
		doSubmits: function(){
		    if(!addTyreSpecification.validates()) return;
            addTyreSpecification.hideErrorMsg();
            if ($.trim($("#tireSpecifications").val()) == "") {
                addTyreSpecification.showErrorMsg(tyreSpecificationNull, "tireSpecifications");
                return;
            }
            if ($("#tireSpecifications").val().length>25) {
                addTyreSpecification.showErrorMsg(tyreSpecificationLenth, "tireSpecifications");
                return;
            }
            if ($.trim($("#tireRollingTadius").val()) == "") {
                addTyreSpecification.showErrorMsg(tyreSpecificationRadiusNull, "tireRollingTadius");
                return;
            }
            var tireRollingTadius = $("#tireRollingTadius").val();
            tireRollingTadius = tireRollingTadius.replace(/[^\d]/g,""); //清除"数字"和"."以外的字符
            $("#tireRollingTadius").val(tireRollingTadius);
            if ($("#tireRollingTadius").val()>65535) {
                addTyreSpecification.showErrorMsg(tyreSpecificationRadiusError, "tireRollingTadius");
                return;
            }
            if (!/^[A-Za-z0-9_#\*\u4e00-\u9fa5\-\.\/]+$/.test($("#tireSpecifications").val())) {
                addTyreSpecification.showErrorMsg("请输入中文、字母、数字或特殊符号*、-、_、#、/、.", "tireSpecifications");
                return;
            }
            addHashCode($("#addForm"));
            $("#addForm").ajaxSubmit(function (data) {
                data = JSON.parse(data);
                if(data.success){
                    $("#commonSmWin").modal("hide");
                    layer.msg("增加成功！",{move:false});
                    //关闭弹窗
                    myTable.requestData();
                }else if(!data.success && data.msg.toString().indexOf("轮胎规格已被设置") > -1) {
                    addTyreSpecification.showErrorMsg(data.msg, "tireSpecifications");
                    return;
                }else if(!data.success && data.msg.toString().indexOf("系统错误") > -1){
                	layer.msg(data.msg,{move:false});
                }
            });
        },
        showErrorMsg: function(msg, inputId){
            if ($("#error_label_add").is(":hidden")) {
                $("#error_label_add").text(msg);
                $("#error_label_add").insertAfter($("#" + inputId));
                $("#error_label_add").show();
            } else {
                $("#error_label_add").is(":hidden");
            }
        },
        validates: function(){
            return $("#addForm").validate({
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
            $("#error_label_add").hide();
        },
        checkNum: function(obj){
			obj.value = obj.value.replace(/[^\d]/g,""); //清除"数字"和"."以外的字符
		}
	}
	$(function(){
		$('input').inputClear();
        $("#doSubmits").bind("click",addTyreSpecification.doSubmits);
	})
})(window,$)