(function(window,$){
	addFlowSensor = {
		init: function(){
			var roles = {
				oilWearNumber : {
					required : true,
                    maxlength: 25,
                    isRightSensorModel: true,
					// isRightfulString : true,
					remote: {
						type:"post",
						url:"/clbs/v/oilmgt/fluxsensormgt/repetition" ,
						dataType:"json",  
	                    data:{  
	                          fluxSensorNumber:function(){return $("#oilWearNumber").val();}  
	                     },  
	                    dataFilter: function(data, type) {
	                    	var data = JSON.parse(data);
	                        if (data.success){
	                        	return true; 
	                        }else if(!data.success && data.msg == null){
	                        	return false;
	                        }else if(!data.success && data.msg.toString().indexOf("系统错误") > -1){
	                        	layer.msg(data.msg,{move:false});
	                        }  
	                    }  
					}  
				}
			};
			var messages = {
				oilWearNumber : {
					required: oilSensorNumberNull,
                    maxlength: sensorModelError,
					// isRightfulString : "请输入合法字符，只能输入字母数字下划线短杠",
					remote: oilSensorNumberExists
				},
				parity : {
					required: publicNull
				},
				baudRate : {
					required: publicNull
				},
				inertiaCompEn : {
					required: publicNull
				}
			};
			myTable.add('commonWin', 'addForm', roles, messages);
		},
	}
	$(function(){
		$('input').inputClear();
		addFlowSensor.init();
		$("#doSubmitAdd").on('click', function () {
			addHashCode($("#addForm"));
		})
	})
})(window,$)