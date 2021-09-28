(function (window, $) {
	addthirdplatform = {
		doSubmit: function () {
//			addthirdplatform.hideErrorMsg();
//			if ($.trim($("#description").val()) == "") {
//				addthirdplatform.showErrorMsg("平台名称不能为空！", "description");
//				return;
//			}
//			if ($("#description").val().length > 20) {
//				addthirdplatform.showErrorMsg("平台名称请控制在20个字符以内！", "description");
//				return;
//			}
//			if ($.trim($("#platformIp").val()) == "") {
//				addthirdplatform.showErrorMsg("IP地址不能为空！", "platformIp");
//				return;
//			}
//			var reg = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/
//
//			if (!reg.test($("#platformIp").val())) {
//				addthirdplatform.showErrorMsg("请输入正确的IP地址！", "platformIp");
//				return;
//			}
			
			if (addthirdplatform.addValidate()) {
				addHashCode1($("#addForm"));
				$("#addForm").ajaxSubmit(function () {
					$("#commonSmWin").modal("hide");
					myTable.refresh();
				});
			}
		},showErrorMsg: function(msg, inputId){
			if ($("#error_label_add").is(":hidden")) {
				$("#error_label_add").text(msg);
				$("#error_label_add").insertAfter($("#" + inputId));
				$("#error_label_add").show();
			} else {
				$("#error_label_add").is(":hidden");
			}
		},
		//错误提示信息隐藏
		hideErrorMsg: function(){
			$("#error_label_add").hide();
		},
		//校验
        addValidate: function() {
        	 return $("#addForm").validate({
        		 rules: {
        			 platformName : {
        				required : true,
        				maxlength : 20,
        				remote : {
        					type:"post",
                            async:false,
                            url:"/clbs/access/platform/check808InputPlatFormSole",
                            data:{
                            	platFormName:function(){return $.trim($("#description").val());},
                            },
        				}
        			 },
        			 ip : {
        				 required : true,
        				 ip : true
        			 },
        		 },
        		 messages: {
        			 platformName : {
        				required : "平台名称不能为空",
        				maxlength : "平台名称请控制在20个字符以内！",
        				remote : "该平台名称已存在，请重新输入！"
        			 },
        			 ip : {
        				 required : "请输入IP地址",
        				 ip:"请输入正确的IP地址"
        			 },
        		 }
        	 }).form();
        }
	}
	$(function(){
		$('input').inputClear();
	})
})(window, $)