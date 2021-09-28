(function(window, $) {
	addOilRodSensor = {
		validates : function() {
	
			return $("#addForm")
					.validate(
							{
								rules : {
									riskEvent : {
										required : true,
										maxlength : 20,
										remote : {
											type : "post",
											async : false,
											url : "/clbs/r/riskManagement/TypeLevel/repetition",
											data : {
												riskEvent : function() {
													return $("#riskEvent")
															.val();
												},
												riskType:function(){return $("#riskType option:selected").text();}
											}
										}
									},
									
									
								},
								messages : {
									riskEvent : {
										required : riskEventThingNull,
										maxlength : riskEventThingLength,
										remote : riskEventThingExist
									},
									
									
								}
							}).form();
		},
		// 提交
		doSubmits : function() {
			if (addOilRodSensor.validates()) {
				$("#addForm").ajaxSubmit(function(data) {
					var data = $.parseJSON(data)
					if (data.success) {
						$("#commonWin").modal("hide");
						layer.msg("添加成功！", {
							move : false
						});
						myTable.refresh();
					} else {
						layer.msg(data.msg, {
							move : false
						});
					}
				});
			}
		},
	}
	$(function() {
		$('input').inputClear();
		$("#doSubmits").bind("click", addOilRodSensor.doSubmits);
	})
})(window, $)