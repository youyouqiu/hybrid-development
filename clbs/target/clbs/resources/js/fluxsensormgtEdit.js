
(function(window,$){
	editFlowSensor = {
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
	                    	var oldV = $("#scn").val();
	              			var newV = $("#oilWearNumber").val();
	              			var data = JSON.parse(data);
	              			if (oldV == newV) {
	              				return true;
	              			} else {
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
					required: "不能为空"
				},
				baudRate : {
					required: "不能为空"
				},
				inertiaCompEn : {
					required: "不能为空"
				}
			};
			myTable.edit('commonWin', 'editForm', roles, messages);
		}
	}
	$(function(){
		$('input').inputClear();
		editFlowSensor.init();
		$("#doSubmitEdit").on('click', function () {
			addHashCode($("#editForm"));
		})
	})
})(window,$)