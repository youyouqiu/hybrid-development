(function(window,$){
	var veerList = [];//提交的参数
	var default_outputCorrectionCoefficientK = 100; // 输出修正系数K默认值
	var default_outputCorrectionCoefficientB = 100; // 输出修正系数B默认值
	var vehicleList = JSON.parse($("#vehicleList").attr("value"));// 车辆list 
	var sensorList =  JSON.parse($("#sensorList").attr("value"));// 流量传感器list 
	// 初始化车辆数据 
	var dataList = {value: []};
	// 初始化正反转传感器 
	var dataVeerList = {value: []};
	var brand1="";
	bindVeerVehicle = {
		init: function(){
	    	// 初始化正反转动传感器 
			if (sensorList != null && sensorList.length > 0) {
		        for (var i=0; i< sensorList.length; i++) {
		        	 var veer = {};
		        	veer.id = sensorList[i].id;
		        	veer.name = sensorList[i].sensorNumber;
		        	dataVeerList.value.push(veer);
		        	
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
	        	 var veerId = keyword.id;
	        	 for (var i=0; i<sensorList.length; i++) {
	        		 if (veerId == sensorList[i].id) {
	        			 	$("#sensorId").val(sensorList[i].id);
	        			 	$("#sensorNumeber").val(sensorList[i].sensorNumeber);
	        	  			$("#compensate").val(sensorList[i].compensate);
	        	  			$("#remark").val(sensorList[i].remark);
	        		 }
	        	 }
	        	 $("#error_label_add").hide();
	        	 //选择正反转传感器型号时清空参考车牌
	             bindVeerVehicle.resetReferencePlate();
	        }).on('onUnsetSelectValue', function () {
	        });
			$("#correctionFactorK").blur(function() {
				bindVeerVehicle.validates();
				bindVeerVehicle.checkOutputCorrectionCoefficientKModify(this); // 判断当前修正系数是否被修改
			});
			$("#correctionFactorB").blur(function() {
				bindVeerVehicle.validates();
				bindVeerVehicle.checkOutputCorrectionCoefficientBModify(this); // 判断当前修正系数是否被修改
			});
			$("#autoTime").change(function() {// 判断当前自动上传时间是否被修改
				var brand2 = $("#brands").val(); 
				if(brand2 == brand1){
					//清空参考车牌
		        	bindVeerVehicle.resetReferencePlate();
				}
			});
			if (vehicleList != null && vehicleList.length > 0) {
            	var vehicleId = $("#vehicleId").val();
			    for (var i=0; i< vehicleList.length; i++) {
			    	var obj = {};
                    obj.id = vehicleList[i].vehicleId;
                    obj.name = vehicleList[i].brand;
                    dataList.value.push(obj);
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
							//修正系数K值一经修改时清空参考车牌
				            bindVeerVehicle.resetReferencePlate();
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
							//修正系数B值一经修改时清空参考车牌
				            bindVeerVehicle.resetReferencePlate();
						}, function () {
							ele.value = default_outputCorrectionCoefficientB;
				});
			}
		},
		//组装数据
		assembly : function(){
			var sensorId=$("#sensorId").val();
			if(sensorId!="" && sensorId!=null){
				var sensorNumber=$("#sensorNumber").val();
				var vehicleId=$("#vehicleId").val();
				var autoTime=$("#autoTime").val();
				var overValve="";
				var correctionFactorK=$("#correctionFactorK").val();
				var correctionFactorB=$("#correctionFactorB").val();
				var alarmUp="";
				var alarmDown="";
				var remark=$("#remark").val();
				var compensate=$("#compensate").val();
				var filterFactor="";
				var list=[51,sensorId,vehicleId,autoTime,remark,overValve,correctionFactorK,correctionFactorB,alarmUp,alarmDown];
				flag=true;
				veerList.push(list);
			}
			if( (sensorId == "" || sensorId == null)){
                flag=false;
                bindVeerVehicle.showErrorMsg(sensorNull, "sensorNumber");
			}
		},
		//发送请求 
		doSubmit : function(){
			bindVeerVehicle.hideErrorMsg();
			if($("#sensorNumber").attr("data-id")==""){
	            //tg_confirmDialog("系统提示","请选择正确的传感器型号！",null,null);
				bindVeerVehicle.showErrorMsg(sensorNull, "sensorNumber");
                return;
	        }else {
	        	if(bindVeerVehicle.validates()){
	        		bindVeerVehicle.assembly();
	    			if(flag){
	    				var value = '';
	    				for(var i = 0; i < veerList.length; i++) {
	    					value += veerList[i];
						}
	    				value = value.split("").reduce(function (a, b) { a = ((a << 5) - a) + b.charCodeAt(0); return a & a }, 0);

	    				veerList.push("");
		    			var url='/clbs/v/sensorSettings/add';
		    			var sensorType = 3;
		    			var data={"sensorList":veerList,"avoidRepeatSubmitToken":$("#avoidRepeatSubmitToken").val(),"sensorType":sensorType, "resubmitToken":value};
	    				address_submit("POST", url, "json", false,data,true,bindVeerVehicle.callBack);     
	    			} 
	            }
	        }
		},
		callBack : function(data){
			 if (data.success) {
            	 layer.msg("绑定成功！");
            	 $("#commonWin").modal("hide");
             }else{
            	 layer.msg(data.msg,{move:false});
             }
        	 myTable.refresh();
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
	    	  return $("#bindForm").validate({
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
		setTimeout(function(){
	    	$("#sensorNumber").parent().find("ul.dropdown-menu").hide();  
		},100);
		bindVeerVehicle.init();		
		$("#doSubmit").bind("click",bindVeerVehicle.doSubmit);
	})
})(window,$)