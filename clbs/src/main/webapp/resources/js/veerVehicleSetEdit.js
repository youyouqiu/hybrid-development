(function(window,$){
	var default_outputCorrectionCoefficientK = 100; // 输出修正系数K默认值
	var default_outputCorrectionCoefficientB = 100; // 输出修正系数B默认值
	var vehicleList = JSON.parse($("#vehicleList").attr("value"));// 车辆list 
	var sensorList =  JSON.parse($("#sensorList").attr("value"));// 正反转传感器list
	var selectVeerSensor = $("#selectVeerSensor").val(); // 选中值 
    var dataList = {value: []};// 初始化车辆数据 
    // 初始化正反转传感器 
	var dataVeerList = {value: []};
	var dataformInit = $("#editForm").serializeArray();   
    var jsonTextInit = JSON.stringify({ dataform: dataformInit });
    var brand1="";
	editVeerVehicle = {
		init: function(){
			// 初始化传感器 
			if (sensorList != null && sensorList.length > 0) {
				 for (var i=0; i< sensorList.length; i++) {
		        	 var sensor = {};
		        	 sensor.id = sensorList[i].id;
		        	 sensor.name = sensorList[i].sensorNumber;
		        	 dataVeerList.value.push(sensor);
		        }
			}
			// select change事件 
			$("#sensorNumber").bsSuggest({
				 indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
	             indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
	             idField: "id",
	             keyField: "name",
	             effectiveFields: ["name"],
	             searchFields:["id"],
	             data: dataVeerList
	        }).on('onDataRequestSuccess', function (e, result) {
	        }).on('onSetSelectValue', function (e, keyword, data) {
	        	 // 当选择传感器
	        	 var transduserId = keyword.id;
	        	 for (var i=0; i<sensorList.length; i++) {
	        		 if (transduserId == sensorList[i].id) {
	        			 	$("#sensorId").val(sensorList[i].id);
	        			 	$("#sensorNumber").val(sensorList[i].sensorNumber);
	        	  			$("#compensate").val(sensorList[i].compensate);
	        	  			$("#remark").val(sensorList[i].remark);
	        		 }
	        	 }
	        	 $("#error_label_add").hide();
	        	 //选择正反转传感器型号时清空参考车牌
	        	 editVeerVehicle.resetReferencePlate();
	        }).on('onUnsetSelectValue', function () {
	        });
			$("#correctionFactorK").blur(function() {
				editVeerVehicle.validates();
				editVeerVehicle.checkOutputCorrectionCoefficientKModify(this); // 判断当前修正系数是否被修改
			});
			$("#correctionFactorB").blur(function() {
				editVeerVehicle.validates();
				editVeerVehicle.checkOutputCorrectionCoefficientBModify(this); // 判断当前修正系数是否被修改
			});
			$("#autoTime").change(function() {// 判断当前自动上传时间是否被修改
				var brand2 = $("#brands").val(); 
				if(brand2 == brand1){
					//清空参考车牌
		        	editVeerVehicle.resetReferencePlate();
				}
			});
	        if (vehicleList != null && vehicleList.length > 0) {
            	var vehicleId = $("#vehicleId").val();
	            for (var i=0; i< vehicleList.length; i++) {
	            	var obj = {};
                  	//删除相同车牌信息
            		if(vehicleList[i].vehicleId == vehicleId){
            			vehicleList.splice(i,1);
            		}
                  	//处理参考车牌只存在一条数据时防止浏览器抛出错误信息
                  	if(vehicleList[i] == undefined){
                        dataList.value.push(obj);
                  	}else{
                        obj.id = vehicleList[i].vehicleId;
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
	        		 if (vehicleId == vehicleList[i].vehicleId) {
	        			 $("#sensorNumber").attr('data-id',vehicleList[i].id);
	        			 $("#sensorNumber").val(vehicleList[i].sensorNumber);
	        			 $("#sensorId").val(vehicleList[i].sensorId);
	        			 $("#compensate").val(vehicleList[i].compensate);
	        			 $("#autoTime").val(vehicleList[i].autoTime);
	        			 $("#correctionFactorK").val(vehicleList[i].correctionFactorK);
	        			 $("#correctionFactorB").val(vehicleList[i].correctionFactorB);
	        			 $("#remark").val(vehicleList[i].remark);
	        			 brand1 = vehicleList[i].brand;
	        			 
	        			 default_outputCorrectionCoefficientK = $("#correctionFactorK").val();
	        			 default_outputCorrectionCoefficientB = $("#correctionFactorB").val();
	        		 }
	        	 }
	        }).on('onUnsetSelectValue', function () {
	        });
		},
		// 清空参考车牌
		resetReferencePlate: function(){
        	$("#brands").val("");
            $("#brands").attr("data-id","");
        },
		// 判断修正系数K是否被修改
		checkOutputCorrectionCoefficientKModify: function(ele){
			if (ele.value != '' && !isNaN(ele.value) && parseFloat(ele.value) != default_outputCorrectionCoefficientK) {
				layer.confirm("修正系数K值一经修改可能导致传感器工作异常，是否确认修改？", 
						{btn : ['确定', '取消']}, function () {
							layer.closeAll();
							default_outputCorrectionCoefficientK = parseFloat(ele.value);
							//清空参考车牌
				        	editVeerVehicle.resetReferencePlate();
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
							default_outputCorrectionCoefficientB = parseFloat(ele.value);
							//清空参考车牌
				        	editVeerVehicle.resetReferencePlate();
						}, function () {
							ele.value = default_outputCorrectionCoefficientB;
				});
			}
		},
		// 提交
	    doSubmit: function(){
	    	editVeerVehicle.hideErrorMsg();
	       	if(editVeerVehicle.validates()){
                var dataform = $("#editForm").serializeArray();  
                var jsonText = JSON.stringify({ dataform: dataform });  
                if(jsonTextInit==jsonText){   
               		/*alert("表单值没有改变！");*/
               		$("#commonWin").modal("hide");
                }else{   
                    /*alert("表单值改变了！");*/   
		       		if($("#sensorNumber").attr("data-id")==""){
			            //tg_confirmDialog("系统提示","请选择正确的传感器型号！",null,null);
		       			editVeerVehicle.showErrorMsg(sensorNull, "sensorNumber");
		                return;
					}else{
						addHashCode($("#editForm"));
						$("#editForm").ajaxSubmit(function(data) {
				            $("#commonWin").modal("hide");
				            if (data != null) {
				            	var result = $.parseJSON(data);
				                if (result.success) {
			                       	 layer.msg("修改成功！",{move:false});
				                } else{
			                       	 layer.msg(result.msg,{move:false});
				                }
				             }
				             myTable.refresh()
			             });
					}
	       		}
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
		validates: function(){
			return $("#editForm").validate({
				rules : {
		       		 brand : {
		     				required : true,
		       		  },
		       		 sensorId : {
		       				required : true,
		         	  },
		         	 correctionFactorK : {
			   				isInteger : true,
			   				range : [1,200]
			   		  },
			   		 correctionFactorB : {
			   				isInteger : true,
			   				range : [0,200]
			   		  }
	     		},
	     		messages : {
	     			brand : {
	     				required: publicNull,
	     			},
	     			sensorId : {
	     				required: publicNull,
	     			},
	     			correctionFactorK : {
		   				isInteger : requiredInt,
		   				range: "输入值必须介于1到200之间"
		   			},
		   			correctionFactorB : {
		   				isInteger : requiredInt,
		   				range: "输入值必须介于0到200之间"
		   			}
	     		}
			}).form();
		},
	}
	$(function(){
		$('input').inputClear();
		editVeerVehicle.init();
		$("#doSubmit").bind("click",editVeerVehicle.doSubmit);
	})
})(window,$)