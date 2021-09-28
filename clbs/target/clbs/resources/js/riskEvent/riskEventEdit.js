(function(window,$){
	editOilRodSensor = {
		validates: function(){
			 return $("#editForm").validate({
				 rules : {
		            riskEvent: {
		                required: true,
		                maxlength : 20,
		                isRightfulString_oilBoxType : true, 
		                remote: {
							type:"post",
							async:false,
							url:"/clbs/r/riskManagement/TypeLevel/repetition" ,
							dataType:"json",  
		                    data:{  
		                    	riskEvent : function() {
									return $("#riskEvent")
											.val();
								},
								riskType:function(){return $("#riskType option:selected").text();}
		                    },  
		                    dataFilter: function(data, type) { 
		             			var oldRiskEvent = $("#oldRiskEvent").val();
		             			var newRiskEvent = $("#riskEvent").val();
		             			var oldRiskType = $("#oldRiskType").val();
		             			var newRiskType = $("#riskType option:selected").text();
		             			var isRepeate = data;
		             	        if (oldRiskEvent ==newRiskEvent&& oldRiskType== newRiskType) {
		             				return true;
		             			} else {
		             				return isRepeate;
		             			}
		                     }  
		                  }
		            },
		          
		        },
		        messages: {
		        	riskEvent : {
                        required : riskEventThingNull,
                        maxlength : riskEventThingLength,
                        remote : riskEventThingExist
					},
					
		        }
		      }).form();
		},
		doSubmits: function(){
		    if(editOilRodSensor.validates()){
		        $("#editForm").ajaxSubmit(function(data) {
		        	 var data = $.parseJSON(data)
		        	 if(data.success){
		        		 $("#commonWin").modal("hide");
		        		 layer.msg("设置成功！",{move:false});
			        	 myTable.refresh();
		        	 }else{
		        		 layer.msg(data.msg,{move:false});
		        	 }
		   		});
		  	}
		},
	}
	$(function(){
		$('input').inputClear();
		$("#doSubmits").bind("click",editOilRodSensor.doSubmits);
	})
})(window,$)