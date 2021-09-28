(function(window,$){
	editVibrationSensor = {
		init: function(){
			var roles = {
					sensorType : {
					required : true,
					maxlength : 20,
					isRightfulString_oilBoxType : true,
					remote: {
						type:"post",
						url:"/clbs/v/workhourmgt/vb/repetition" ,
						dataType:"json",  
	                    data:{  
	                    	sensorNumber:function(){return $("#sensorType").val();}  
	                     },  
	                     dataFilter: function(data, type) { 
	                    	var oldV = $("#scn").val();
	              			var newV = $("#sensorType").val();
	              			if (oldV == newV) {
	              				return true;
	              			}else{
	              				var data = JSON.parse(data)
	              				if (data.success){
                            		return true; 
	                            }else if(!data.success && data.msg == null) {
		                        	return false;
		                        }else if(!data.success && data.msg != null){
		                        	layer.msg(data.msg,{move:false});
		                        }  
	              			}
	                      }  
	                   } 
				}
			};
			var messages = {
					sensorType : {
					required:sensorTypeNull,
					maxlength: publicSize20Length,
					isRightfulString_oilBoxType : oilSensorTypeError, 
					remote: sensorTypeExists
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
			myTable.edit('commonWin', 'editForm', roles, messages);
		},
	}
	$(function(){
		$('input').inputClear();
		editVibrationSensor.init();
	})
})(window,$)