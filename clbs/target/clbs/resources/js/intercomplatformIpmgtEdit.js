(function (window, $) {
	editIntercomplatform = {
		doSubmit: function () {
			editIntercomplatform.hideErrorMsg();
			if ($.trim($("#platformName_edit").val()) == "") {
				editIntercomplatform.showErrorMsg("平台名称不能为空！", "platformName_edit");
				return;
			}
			if ($("#platformName_edit").val().length > 20) {
				editIntercomplatform.showErrorMsg("平台名称请控制在20个字符以内！", "platformName_edit");
				return;
			}
			if ($.trim($("#platformIp_edit").val()) == "") {
				editIntercomplatform.showErrorMsg("IP地址不能为空！", "platformIp_edit");
				return;
			}
			var reg = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/

			if (!reg.test($("#platformIp_edit").val())) {
				editIntercomplatform.showErrorMsg("请输入正确的IP地址！", "platformIp_edit");
				return;
			}
			if ($.trim($("#platformPort_edit").val()) == "") {
				editIntercomplatform.showErrorMsg("端口不能为空！", "platformPort_edit");
				return;
			}
			reg = /^[0-9]*$/;
			if (!reg.test($("#platformPort_edit").val()) || $("#platformPort_edit").val() > 99999) {
				editIntercomplatform.showErrorMsg("请输入正确的端口！", "platformPort_edit");
				return;
			}

			if ($("#description_edit").val().length > 20) {
				editIntercomplatform.showErrorMsg("描述请控制在20个字符以内！", "description_edit");
				return;
			}
			addHashCode1($("#editForm"));
			$("#editForm").ajaxSubmit(function () {
				$("#commonSmWin").modal("hide");
				myTable.refresh();
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
		//错误提示信息隐藏
		hideErrorMsg: function(){
			$("#error_label_edit").hide();
		},
	}
	$(function(){
		$('input').inputClear();
		$("#editSubmit").bind("click", editIntercomplatform.doSubmit);
	})
})(window, $)