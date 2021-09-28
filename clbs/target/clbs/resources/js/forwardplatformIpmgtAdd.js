(function (window, $) {
	addthirdplatform = {
		doSubmit: function () {
			//			addthirdplatform.hideErrorMsg();
			//			if ($.trim($("#description").val()) == "") {
			//				addthirdplatform.showErrorMsg("平台名称不能为空！", "description");
			//				return;
			//			}
			//			var checkedUrl = "/clbs/m/forwardplatform/ipmgt/check808PlatFormSole";
			//			var cFlag = false;
			//			json_ajax("POST", checkedUrl, "json", false, {platFormName : $.trim($("#description").val())}, function (data) {
			//				if (!data) {
			//					addthirdplatform.showErrorMsg("平台名称已存在！", "description");
			//				} else {
			//					cFlag = true;
			//				}
			//			});
			//			
			//			if (!cFlag) {
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
			//			if ($.trim($("#platformPort").val()) == "") {
			//				addthirdplatform.showErrorMsg("端口不能为空！", "platformPort");
			//				return;
			//			}
			//			reg = /^[0-9]*$/;
			//			if (!reg.test($("#platformPort").val()) || $("#platformPort").val() > 65535) {
			//				addthirdplatform.showErrorMsg("请输入正确的端口！", "platformPort");
			//				return;
			//			}
			if (addthirdplatform.addValidate()) {
				addHashCode1($("#addForm"));
				$("#addForm").ajaxSubmit(function () {
					$("#commonSmWin").modal("hide");
					myTable.refresh();
				});
			}
		},
		showErrorMsg: function (msg, inputId) {
			if ($("#error_label_add").is(":hidden")) {
				$("#error_label_add").text(msg);
				$("#error_label_add").insertAfter($("#" + inputId));
				$("#error_label_add").show();
			} else {
				$("#error_label_add").is(":hidden");
			}
		},
		//错误提示信息隐藏
		hideErrorMsg: function () {
			$("#error_label_add").hide();
		},
		//校验
		addValidate: function () {
			return $("#addForm").validate({
				rules: {
					description: {
						required: true,
						maxlength: 20,
						remote: {
							type: "post",
							async: false,
							url: "/clbs/m/forwardplatform/ipmgt/check808PlatFormSole",
							data: {
								platFormName: function () {
									return $.trim($("#description").val());
								},
							},
						}
					},
					platformIp: {
						required: true,
						ip: true
					},
					platformPort: {
						required: true,
						digits: true,
						range: [0, 65535]
					},
				},
				messages: {
					description: {
						required: "平台名称不能为空",
						maxlength: "平台名称请控制在20个字符以内！",
						remote: "该平台名称已存在，请重新输入！"
					},
					platformIp: {
						required: "请输入IP地址",
						ip: "请输入正确的IP地址"
					},
					platformPort: {
						required: "请输入端口号",
						digits: "请输入正确的端口号",
						range: "请输入正确的端口号"
					},
				}
			}).form();
		}
	}
	$(function () {
		$('input').inputClear();
	})
})(window, $)