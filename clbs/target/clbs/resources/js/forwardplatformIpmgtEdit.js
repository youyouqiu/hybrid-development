(function (window, $) {
	editthirdplatform = {
		doSubmit: function () {
			if (editthirdplatform.editValidate()) {
				addHashCode1($('#editForm'));
				$("#editForm").ajaxSubmit(function () {
					$("#commonSmWin").modal("hide");
					myTable.refresh();
				});
			}
		},showErrorMsg: function(msg, inputId){
			if ($("#error_label_edit").is(":hidden")) {
				$("#error_label_edit").text(msg);
				$("#error_label_edit").insertAfter($("#" + inputId));
				$("#error_label_edit").show();
			} else {
				$("#error_label_edit").is(":hidden");
			}
		},
		//错误提示信息隐藏
		hideErrorMsg: function(){
			$("#error_label_edit").hide();
		},
		//校验
        editValidate: function() {
        	 return $("#editForm").validate({
        		 rules: {
        			 description : {
        				required : true,
        				maxlength : 20,
        				remote : {
        					type:"post",
                            async:false,
                            url:"/clbs/m/forwardplatform/ipmgt/check808PlatFormSole",
                            data:{
                            	platFormName:function(){return $.trim($("#description").val());},
                            	pid:function(){return $("#pid").val();},
                            },
        				}
        			 },
        			 platformIp : {
        				 required : true,
        				 ip : true
        			 },
        			 platformPort : {
        				 required : true,
        				 digits : true,
        				 range : [0,65535]
        			 },
        		 },
        		 messages: {
        			 description : {
        				required : "平台名称不能为空",
        				maxlength : "平台名称请控制在20个字符以内！",
        				remote : "该平台名称已存在，请重新输入！"
        			 },
        			 platformIp : {
        				 required : "请输入IP地址",
        				 ip:"请输入正确的IP地址"
        			 },
        			 platformPort : {
        				 required : "请输入端口号",
        				 digits : "请输入正确的端口号",
        				 range : "请输入正确的端口号"
        			 },
        		 }
        	 }).form();
        }
	}
	$(function(){
		$('input').inputClear();
	})
})(window, $)