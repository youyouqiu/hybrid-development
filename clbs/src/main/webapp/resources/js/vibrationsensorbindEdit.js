(function(window,$){
	var default_outputCorrectionCoefficientK = 100; // 输出修正系数K默认值
	var default_outputCorrectionCoefficientB = 100; // 输出修正系数B默认值
	var vehicleList = JSON.parse($("#vehicleList").attr("value"));// 车辆list  
	var vibrationSensorList = JSON.parse($("#vibrationSensorList").attr("value"));// 流量传感器list 
	var selectVibrationSensor = $("#selectVibrationSensor").val(); // 选中值 
    var dataList = {value: []};// 初始化车辆数据 
 	// 初始化振动传感器 
	var dataVibrationList = {value: []};
	editHourlyVehicleSetting = {
		init: function(){
			// 初始化振动传感器 
		    if (vibrationSensorList != null && vibrationSensorList.length > 0) {
		    	 for (var i=0; i< vibrationSensorList.length; i++) {
			        	var vibration={};
			        	vibration.id = vibrationSensorList[i].id;
			        	vibration.name = vibrationSensorList[i].sensorType;
			        	dataVibrationList.value.push(vibration);
			        }
		    }
			$("#vibrationSensor").bsSuggest({
	             indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
	             indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
	             idField: "id",
	             keyField: "name",
	             effectiveFields: ["name"],
	             searchFields:["id"],
	             data: dataVibrationList
	        }).on('onDataRequestSuccess', function (e, result) {
	        }).on('onSetSelectValue', function (e, keyword, data) {
	        	 var vibrationId = keyword.id;
	        	 for (var i=0; i<vibrationSensorList.length; i++) {
	        		 if (vibrationId == vibrationSensorList[i].id) {
	        			$("#formVibrationSensor").val(vibrationSensorList[i].id);
	        			$("#manufacturers").val(vibrationSensorList[i].manufacturers);
	 			 		$("#parity").val(vibrationSensorList[i].parity);
	 			 		$("#filterFactor").val(vibrationSensorList[i].filterFactor);
	 			 		$("#baudRate").val(vibrationSensorList[i].baudRate);
	 			 		$("#inertiaCompEn").val(vibrationSensorList[i].inertiaCompEn);
	 			 		$("#uploadTime").val("01");
				 		$("#outputCorrectionB").val("100");
				 		$("#outputCorrectionK").val("100");
            $("#error_label_add").hide();
	        		 }
	        	 }
	         }).on('onUnsetSelectValue', function () {
	      	});
		    $("#outputCorrectionK").blur(function() {
		    	editHourlyVehicleSetting.checkOutputCorrectionCoefficientKModify(this); // 判断当前修正系数是否被修改
			});
			$("#outputCorrectionB").blur(function() {
				editHourlyVehicleSetting.checkOutputCorrectionCoefficientBModify(this); // 判断当前修正系数是否被修改
			});
	        if (vehicleList != null && vehicleList.length > 0) {
            	//得到车牌id
            	var vehicleId = $("#vehicleId").val();
                for (var i=0; i< vehicleList.length; i++) {
                    var obj = {};
                  	//删除相同车牌信息
            		if(vehicleList[i].vehicleId == vehicleId){
            			vehicleList.splice(vehicleList[i].vehicleId.indexOf(vehicleId),1);
            		}
                  	//处理参考车牌只存在一条数据时防止浏览器抛出错误信息
                  	if(vehicleList[i] == undefined){
                        dataList.value.push(obj);
                  	}else{
                        obj.id = vehicleList[i].id;
                        obj.name = vehicleList[i].brand;
                        dataList.value.push(obj);
                  	}
                }
	        }
	        $("#brands").bsSuggest({
	             indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
	             indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
	             idField: "id",
	             keyField: "name",
	             effectiveFields: ["name"],
	             searchFields:["id"],
	             data: dataList
	        }).on('onDataRequestSuccess', function (e, result) {

	        }).on('onSetSelectValue', function (e, keyword, data) {
	        	 // 当选择参考车牌
	        	 var vehicleId = keyword.id;
	        	 for (var i=0; i<vehicleList.length; i++) {
	        		 if (vehicleId == vehicleList[i].id) {
	        			 $("#formVibrationSensor").val(vehicleList[i].shockSensorId);
	        			 $("#vibrationSensor").attr('data-id',vehicleList[i].shockSensorId);
	        			 $("#vibrationSensor").val(vehicleList[i].sensorType);
	        			 $("#manufacturers").val(vehicleList[i].manufacturers);
	        			 $("#parity").val(vehicleList[i].parity);
	        			 $("#filterFactor").val(vehicleList[i].filterFactor);
	        			 $("#baudRate").val(vehicleList[i].baudRate);
	        			 $("#inertiaCompEn").val(vehicleList[i].inertiaCompEn);
	        			 $("#collectNumber").val(vehicleList[i].collectNumber);
	        			 $("#uploadNumber").val(vehicleList[i].uploadNumber);
	        			 $("#uploadTime").val(vehicleList[i].uploadTime);
	        			 $("#outputCorrectionB").val(vehicleList[i].outputCorrectionB);
	        			 $("#outputCorrectionK").val(vehicleList[i].outputCorrectionK);
	        			 $("#outageFrequencyThreshold").val(vehicleList[i].outageFrequencyThreshold);
	        			 $("#idleFrequencyThreshold").val(vehicleList[i].idleFrequencyThreshold);
	        			 $("#continueOutageTimeThreshold").val(vehicleList[i].continueOutageTimeThreshold);
	        			 $("#continueIdleTimeThreshold").val(vehicleList[i].continueIdleTimeThreshold);
	        			 $("#alarmFrequencyThreshold").val(vehicleList[i].alarmFrequencyThreshold);
	        			 $("#workFrequencyThreshold").val(vehicleList[i].workFrequencyThreshold);
	        			 $("#continueAlarmTimeThreshold").val(vehicleList[i].continueAlarmTimeThreshold);
	        			 $("#continueWorkTimeThreshold").val(vehicleList[i].continueWorkTimeThreshold);
	        		 }
	        	 }
	        }).on('onUnsetSelectValue', function () {
	        });
	        // select change事件 
	        $('#vibrationSensor').change(function(){ 
	   			var selectValue = $(this).children('option:selected').val();//这就是selected的值 
	   			for (var i=0; i<vibrationSensorList.length; i++) {
	           		 if (selectValue == vibrationSensorList[i].id) {
	           			 $("#brands").val("");
	           			 $("#manufacturers").val(vibrationSensorList[i].manufacturers);
	   	       			 $("#parity").val(vibrationSensorList[i].parity);
	   	       			 $("#filterFactor").val(vibrationSensorList[i].filterFactor);
	   	       			 $("#baudRate").val(vibrationSensorList[i].baudRate);
	   	       			 $("#inertiaCompEn").val(vibrationSensorList[i].inertiaCompEn);
	   	       			 $("#uploadTime").val("01");
	   	       			 $("#outputCorrectionB").val("100");
	   	       			 $("#outputCorrectionK").val("100");
                         $("#error_label_add").hide();
	           		 }
	           	 }
	   		}) 
		},
		// 判断修正系数K是否被修改
		checkOutputCorrectionCoefficientKModify: function(ele) {
			if (ele.value != '' && !isNaN(ele.value) && parseFloat(ele.value) != default_outputCorrectionCoefficientK) {
				layer.confirm("修正系数K值一经修改可能导致传感器工作异常，是否确认修改？", 
					{btn : ['确定', '取消']}, function () {
						layer.closeAll();
					}, function () {
					ele.value = default_outputCorrectionCoefficientK;
				});
			}
		},
		// 判断修正系数B是否被修改
		checkOutputCorrectionCoefficientBModify: function(ele){
			if (ele.value != '' && !isNaN(ele.value) && parseFloat(ele.value) != default_outputCorrectionCoefficientB) {
				layer.confirm("修正系数B值一经修改可能导致传感器工作异常，是否确认修改？", 
					{btn : ['确定', '取消']}, 
					function () {
						layer.closeAll();
					}, function () {
						ele.value = default_outputCorrectionCoefficientB;
				});
			}
		},
        showErrorMsg: function(msg, inputId){
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
            $("#error_label_add").is(":hidden");
            $("#error_label_add").hide();
        },
		// 提交
	    doSubmit: function(){
            editHourlyVehicleSetting.hideErrorMsg();
            var vibrationSensor = $("#vibrationSensor").attr("data-id");
            if (!vibrationSensor) {
                editHourlyVehicleSetting.showErrorMsg(sensorNull, "vibrationSensor");
                return;
            }
	    	if(editHourlyVehicleSetting.validates()){
	            $("#editForm").ajaxSubmit(function(data) {
	            
	            	 if (data != null) {
	                     var result = $.parseJSON(data);
	                     if (result.success) {
	                    	 layer.msg(result.msg,{move:false});
	                    		$("#commonWin").modal("hide");
	                     } else{
	                    	 layer.msg(result.msg,{move:false});
	                     }
	                 }
	                 myTable.refresh()
	            });
	        }
	    },
	    validates: function(){
			return $("#editForm").validate({
				rules : {
		        		 vehicleBrand : {
		      				required : true,
		        		  },
		        		  shockSensorId : {
		        				required : true,
		          		  },
		      			autoUploadTime : {
		    				isInteger : true,
		    				max : 300
		    			},
		    			outputCorrectionK : {
		    				isInteger : true,
		    				range : [1,200]
		    			},
		    			outputCorrectionB : {
		    				isInteger : true,
		    				range : [0,200]
		    			},
		    			collectNumber : {
				   			isInteger : true,
				   			max : 65535,
				   		},
				   		uploadNumber : {
				   			isInteger : true,
				   			max : 65535,
						},
						outageFrequencyThreshold : {
							isInteger : true,
							maxlength : 255,
					  	},
					  	idleFrequencyThreshold : {
					  		isInteger : true,
					  		maxlength : 255,
					 	},
					  	continueOutageTimeThreshold : {
					  		isInteger : true,
					  		maxlength : 255,
					  	},
					  	continueIdleTimeThreshold : {
					  		isInteger : true,
					  		maxlength : 255,
					  	},
					  	alarmFrequencyThreshold : {
					  		isInteger : true,
					  		maxlength : 255,
					  	},
					  	workFrequencyThreshold : {
					  		isInteger : true,
					  		maxlength : 255,
					  	},
					  	continueAlarmTimeThreshold : {
					  		isInteger : true,
					  		maxlength : 255,
					  	},
					  	continueWorkTimeThreshold : {
					  		isInteger : true,
					  		maxlength : 255,
					  	}
		      		},
		      		messages : {
		      			vehicleBrand : {
		      				required: publicNull,
		      			},
		      			shockSensorId : {
		      				required: publicNull,
		      			},
		    			autoUploadTime : {
		    				isInteger : requiredInt,
		    				max: max300
		    			},
		    			outputCorrectionK : {
		    				isInteger : requiredInt,
		    				range: outputCorrectionKScope
		    			},
		    			outputCorrectionB : {
		    				isInteger : requiredInt,
		    				range: outputCorrectionBScope
		    			},
		    			collectNumber : {
				   			isInteger : requiredInt,
				   			max : max65535
				   		},
				   		uploadNumber : {
				   			isInteger : requiredInt,
				   			max : max65535
						},
						outageFrequencyThreshold : {
							isInteger : requiredInt,
							maxlength : maxLength255
					  	},
					  	idleFrequencyThreshold : {
					  		isInteger : requiredInt,
					  		maxlength : maxLength255
					 	},
					  	continueOutageTimeThreshold : {
					  		isInteger : requiredInt,
					  		maxlength : maxLength255
					  	},
					  	continueIdleTimeThreshold : {
					  		isInteger : requiredInt,
					  		maxlength : maxLength255
					  	},
					  	alarmFrequencyThreshold : {
					  		isInteger : requiredInt,
					  		maxlength : maxLength255
					  	},
					  	workFrequencyThreshold : {
					  		isInteger : requiredInt,
					  		maxlength : maxLength255
					  	},
					  	continueAlarmTimeThreshold : {
					  		isInteger : requiredInt,
					  		maxlength : maxLength255
					  	},
					  	continueWorkTimeThreshold : {
					  		isInteger : requiredInt,
					  		maxlength : maxLength255
					  	}
		      		}
			}).form();
	    },
	}
	$(function(){
		$('input').inputClear();
		editHourlyVehicleSetting.init();
		// 提交
		$("#doSubmit").bind("click",editHourlyVehicleSetting.doSubmit);
	})
})(window,$)