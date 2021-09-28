(function($,window){
  $('#userNameSpan').html($('#username').val());
	var changePwd = {
		validates: function () {
		     return $("#changePwdForm").validate({
				errorElement : 'span',
				errorClass : 'help-block',
				rules : {
					username : "required",
					oldpass : {
						required : true,
						maxlength : 25,
						minlength : 6
					},
					newpass : {
						required : true,
						maxlength : 25,
						minlength : 8,
						checkStrength: true
					},
					newpassAgain : {
						required : true,
						maxlength : 25,
						minlength : 8,
						equalTo : "#newpass",
						checkStrength: true
					}
				},
				messages : {
					username : "请输入用户名",
					oldpass : {
							required : "旧密码不能为空",
							maxlength : "长度不超过25",
							minlength : "最少6个字符"
					},
					newpass : {
						required : "请输入新密码",
						maxlength : "长度不超过25",
						minlength : "密码不能小于8个字符",
						checkStrength: '密码必须包含字母、数字和特殊字符（不含空格）中的两者'
					},
					newpassAgain : {
						required : "请输入确认新密码",
						maxlength : "长度不超过25",
						minlength : "确认密码不能小于8个字符",
						equalTo : "两次输入密码不一致",
						checkStrength: '密码必须包含字母、数字和特殊字符（不含空格）中的两者'
					}
				},
				errorPlacement : function(error, element) {
					element.next().remove();
					element.after('<span class="glyphicon glyphicon-remove form-control-feedback" aria-hidden="true"></span>');
					element.closest('.form-group').append(error);
				},
				highlight : function(element) {
					$(element).closest('.form-group').addClass('has-error has-feedback');
				},
				success : function(label) {
					var el=label.closest('.form-group').find("input");
					el.next().remove();
					el.after('<span class="glyphicon glyphicon-ok form-control-feedback" aria-hidden="true"></span>');
					label.closest('.form-group').removeClass('has-error').addClass("has-feedback has-success");
					label.remove();
				},
		    }).form();
		 },
	}
	$(function(){
		$("#submit").click(function(event){
			if(changePwd.validates()) {
				var username = $("#username").val();			
				var newpass = $("#newpass").val();
				var oldpass = $("#oldpass").val();
				$.post('/clbs/c/user/changePwd', {oldpass:oldpass,newpass:newpass},function(data) {
					if(data.success){
						if(data.obj.flag == "2"){
							var msg = data.obj.errMsg;
							layer.msg(msg);
						}else{
							var msg = data.obj.errMsg;
							layer.msg(msg);   
							layer.msg(msg,{closeBtn: 0}, function(){
								location.href ="/clbs/login.html?type=changeState";
								layer.close();
							});
						}
					}else{
						layer.msg(data.msg);
					}
				
			},'json');
			}
			event.preventDefault();
			return false;
		});
	})
}($,window))