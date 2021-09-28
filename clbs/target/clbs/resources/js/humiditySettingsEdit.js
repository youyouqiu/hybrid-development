(function(window,$){
	var sensorList=[];//提交的参数
	var flag=true;
	var vehicleList = JSON.parse($("#vehicleList").attr("value"));
	var transduserManage=JSON.parse($("#transduserManage").attr("value"));
	//初始化参考车牌
	var dataList = {value: []};
	var  TransduserList={value: []};
	HumiditySettings = {
		init : function(){
			if (vehicleList != null && vehicleList.length > 0) {
            	var vehicleId = $("#vehicleId").attr("value");
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
	        	 var vId = keyword.id;
	        	 var url="/clbs/v/sensorSettings/findVehicleBrand";
	        	 var data={"id":vId ,"sensorType":2};
	        	 json_ajax("POST", url, "json", false,data,HumiditySettings.consult);     //发送请求 
	        }).on('onUnsetSelectValue', function () {
	        });
	        //传感器型号下拉选
	        if (transduserManage != null && transduserManage.length > 0) {					  
		        for (var i=0; i< transduserManage.length; i++) {
		        	 var sensor = {};
		        	 sensor.id = transduserManage[i].id;
		        	 sensor.name = transduserManage[i].sensorNumber;
		        	 TransduserList.value.push(sensor);
		        }
		    }
	        //传感器下拉选1
	        $("#sensorNumber").bsSuggest({
	             indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
	             indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
	             idField: "id",
	             keyField: "name",
	             effectiveFields: ["name"],
	             searchFields:["id"],
	             data: TransduserList
	        }).on('onDataRequestSuccess', function (e, result) {
	        }).on('onSetSelectValue', function (e, keyword, data) {
	        	 var transduserId = keyword.id;
	        	 for (var i=0; i<transduserManage.length; i++) {
		        		if (transduserId == transduserManage[i].id) {
                            $("#errorMsg").hide();
		        			$("#sensorId").val(transduserManage[i].id);
		        			$("#compensate").val(transduserManage[i].compensate);
		        			$("#filterFactor").val(transduserManage[i].filterFactor);
		        		}
	        	 }
	        }).on('onUnsetSelectValue', function () {
	        });
	        //传感器下拉选2
	        $("#sensorNumber2").bsSuggest({
	             indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
	             indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
	             idField: "id",
	             keyField: "name",
	             effectiveFields: ["name"],
	             searchFields:["id"],
	             data: TransduserList
	        }).on('onDataRequestSuccess', function (e, result) {
	        }).on('onSetSelectValue', function (e, keyword, data) {
	        	 var transduserId = keyword.id;
	        	 for (var i=0; i<transduserManage.length; i++) {
		        		if (transduserId == transduserManage[i].id) {
                            $("#errorMsg2").hide();
		        			$("#sensorId2").val(transduserManage[i].id);
		        			$("#compensate2").val(transduserManage[i].compensate);
		        			$("#filterFactor2").val(transduserManage[i].filterFactor);
		        		}
	        	 }
	        }).on('onUnsetSelectValue', function () {
	        });
	        //传感器下拉选3
	        $("#sensorNumber3").bsSuggest({
	             indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
	             indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
	             idField: "id",
	             keyField: "name",
	             effectiveFields: ["name"],
	             searchFields:["id"],
	             data: TransduserList
	        }).on('onDataRequestSuccess', function (e, result) {
	        }).on('onSetSelectValue', function (e, keyword, data) {
	        	 var transduserId = keyword.id;
	        	 for (var i=0; i<transduserManage.length; i++) {
		        		if (transduserId == transduserManage[i].id) {
                            $("#errorMsg3").hide();
		        			$("#sensorId3").val(transduserManage[i].id);
		        			$("#compensate3").val(transduserManage[i].compensate);
		        			$("#filterFactor3").val(transduserManage[i].filterFactor);
		        		}
	        	 }
	        }).on('onUnsetSelectValue', function () {
	        });
	        //传感器下拉选4
	        $("#sensorNumber4").bsSuggest({
	             indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
	             indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
	             idField: "id",
	             keyField: "name",
	             effectiveFields: ["name"],
	             searchFields:["id"],
	             data: TransduserList
	        }).on('onDataRequestSuccess', function (e, result) {
	        }).on('onSetSelectValue', function (e, keyword, data) {
	        	 var transduserId = keyword.id;
	        	 for (var i=0; i<transduserManage.length; i++) {
		        		if (transduserId == transduserManage[i].id) {
                            $("#errorMsg4").hide();
		        			$("#sensorId4").val(transduserManage[i].id);
		        			$("#compensate4").val(transduserManage[i].compensate);
		        			$("#filterFactor4").val(transduserManage[i].filterFactor);
		        		}
	        	 }
	        }).on('onUnsetSelectValue', function () {
	        });
		},
		//组装数据
		assembly : function(){
			sensorList=[];
			var validate = true; // 判断校验是否成功
			
			var sensorNumber=$("#sensorNumber").val();
            var dataId=$("#sensorNumber").attr("data-id");
			if(sensorNumber!="" && sensorNumber!=null){
				var sensorId=$("#sensorId").val();
				var vehicleId=$("#vehicleId").val();
				var autoTime=$("#autoTime").val();
				var overValve=$("#overValve").val();
				var correctionFactorK=$("#correctionFactorK").val();
				var correctionFactorB=$("#correctionFactorB").val();
				var alarmUp=$("#alarmUp").val();
				var alarmDown=$("#alarmDown").val();
				var remark=$("#remark").val();
				var compensate=$("#compensate").val();
				var filterFactor=$("#filterFactor").val();
				validate = HumiditySettings.efficacy(1,26,sensorId,vehicleId,autoTime,remark,overValve,correctionFactorK,correctionFactorB,alarmUp,alarmDown,dataId);
			}
			
			var sensorNumber2=$("#sensorNumber2").val();
            var dataId2=$("#sensorNumber2").attr("data-id");
            if (validate) {
            	if(sensorNumber2!="" && sensorNumber2!=null){
    				var sensorId2=$("#sensorId2").val();
    				var vehicleId2=$("#vehicleId").val();
    				var autoTime2=$("#autoTime2").val();
    				var overValve2=$("#overValve2").val();
    				var correctionFactorK2=$("#correctionFactorK2").val();
    				var correctionFactorB2=$("#correctionFactorB2").val();
    				var alarmUp2=$("#alarmUp2").val();
    				var alarmDown2=$("#alarmDown2").val();
    				var remark2=$("#remark2").val();
    				var compensate2=$("#compensate2").val();
    				var filterFactor2=$("#filterFactor2").val();
    				validate = HumiditySettings.efficacy(2,27,sensorId2,vehicleId2,autoTime2,remark2,overValve2,correctionFactorK2,correctionFactorB2,alarmUp2,alarmDown2,dataId2);
    			}
            }
			
			var sensorNumber3=$("#sensorNumber3").val();
            var dataId3=$("#sensorNumber3").attr("data-id");
            if (validate) {
            	if(sensorNumber3!="" && sensorNumber3!=null){
    				var sensorId3=$("#sensorId3").val();
    				var vehicleId3=$("#vehicleId").val();
    				var autoTime3=$("#autoTime3").val();
    				var overValve3=$("#overValve3").val();
    				var correctionFactorK3=$("#correctionFactorK3").val();
    				var correctionFactorB3=$("#correctionFactorB3").val();
    				var alarmUp3=$("#alarmUp3").val();
    				var alarmDown3=$("#alarmDown3").val();
    				var remark3=$("#remark3").val();
    				var compensate3=$("#compensate3").val();
    				var filterFactor3=$("#filterFactor3").val();
    				validate = HumiditySettings.efficacy(3,28,sensorId3,vehicleId3,autoTime3,remark3,overValve3,correctionFactorK3,correctionFactorB3,alarmUp3,alarmDown3,dataId3);
    			}
            }
			
			var sensorNumber4=$("#sensorNumber4").val();
            var dataId4=$("#sensorNumber4").attr("data-id");
            if (validate) {
            	if(sensorNumber4!="" && sensorNumber4!=null){
    				var sensorId4=$("#sensorId4").val();
    				var vehicleId4=$("#vehicleId").val();
    				var autoTime4=$("#autoTime4").val();
    				var overValve4=$("#overValve4").val();
    				var correctionFactorK4=$("#correctionFactorK4").val();
    				var correctionFactorB4=$("#correctionFactorB4").val();
    				var alarmUp4=$("#alarmUp4").val();
    				var alarmDown4=$("#alarmDown4").val();
    				var remark4=$("#remark4").val();
    				var compensate4=$("#compensate4").val();
    				var filterFactor4=$("#filterFactor4").val();
    				validate = HumiditySettings.efficacy(4,29,sensorId4,vehicleId4,autoTime4,remark4,overValve4,correctionFactorK4,correctionFactorB4,alarmUp4,alarmDown4,dataId4)
    			}
            }
			
			if( (sensorNumber == "" || sensorNumber == null) &&
					(sensorNumber2 == "" || sensorNumber2 == null) &&
					(sensorNumber3 == "" || sensorNumber3 == null) &&
					(sensorNumber4 == "" || sensorNumber4 == null)
				  ){
                $("#errorMsg").show();
                flag=false;
                return;
			}
		},
		//提交数据前的效验
		efficacy : function(id,outId,sensorId,vehicleId,autoTime,remark,overValve,correctionFactorK,correctionFactorB,alarmUp,alarmDown,dataId){
            if(dataId==""){
                if(id==1){
                    $("#errorMsg").show();
                }else{
                    $("#errorMsg"+id).show();
                }
                flag=false;
                return flag;
            }
			if(overValve==""||overValve==null){
				flag=false;
				layer.msg("湿度传感器"+id+":超出阀值时间阀值不能为空，请重新输入");
				return flag;
			}else if(parseInt(overValve)>65535){
				flag=false;
				layer.msg("湿度传感器"+id+":超出阀值时间阀值最大值为65535，请重新输入");
				return flag;
			}else{
				flag=true;
			}
			if(correctionFactorK==""||correctionFactorK==null){
				flag=false;
				layer.msg("湿度传感器"+id+":输出修正系数K不能为空，请重新输入");
				return flag;
			}else if(1>parseInt(correctionFactorK)||parseInt(correctionFactorK)>200){
				flag=false;
				layer.msg("湿度传感器"+id+":输出修正系数K 取值范围为1~200，请重新输入");
				return flag;
			}else{
				flag=true;
			}
			if(correctionFactorB==""||correctionFactorB==null){
				flag=false;
				layer.msg("湿度传感器"+id+":输出修正系数B不能为空，请重新输入");
				return flag;
			}else if(0>parseInt(correctionFactorB)||parseInt(correctionFactorB)>200){
				flag=false;
				layer.msg("湿度传感器"+id+":输出修正系数B 取值范围为0~200，请重新输入");
				return flag;
			}else{
				flag=true;
			}
			if(alarmUp==""||alarmUp==null){
				flag=false;
				layer.msg("湿度传感器"+id+":湿度报警上阀值(%)不能为空，请重新输入");
				return flag;
			}else if(1>parseInt(alarmUp)||parseInt(alarmUp)>100){
				flag=false;
				layer.msg("湿度传感器"+id+":湿度报警上阀值(%) 取值范围为1~100，请重新输入");
				return flag;
			}else{
				flag=true;
			}
			if(alarmDown==""||alarmDown==null){
				flag=false;
				layer.msg("湿度传感器"+id+":湿度报警下阀值(%)不能为空，请重新输入");
				return flag;
			}else if(1>parseInt(alarmDown)||parseInt(alarmDown)>100){
				flag=false;
				layer.msg("湿度传感器"+id+":湿度报警下阀值(%) 取值范围为1~100，请重新输入");
				return flag;
			}else{
				flag=true;
			}
			var list=[outId,sensorId,vehicleId,autoTime,remark,overValve,correctionFactorK,correctionFactorB,alarmUp,alarmDown];
			sensorList.push(list);
			return flag;
		},
		doSubmit : function(){
            HumiditySettings.assembly();
            var vehicleId = $("#vehicleId").attr("value");
			var value = '';
			for(var i = 0; i < sensorList.length; i++) {
				var item = sensorList[i];
				for(var j = 0; j < item.length; j++) {
					value += item[j];
				}
			}
			value = value.split("").reduce(function (a, b) { a = ((a << 5) - a) + b.charCodeAt(0); return a & a }, 0);

			var url='/clbs/v/sensorSettings/edit';
			var data={"sensorList":sensorList,"vid":vehicleId,"type":2, 'resubmitToken': value};
			if(flag){
				sensorList.push("0");
				address_submit("POST", url, "json", false,data,true,HumiditySettings.callBack);     //发送请求 
			}
		},
		callBack : function(data){
			 if (data.success==true) {
            	 layer.msg("绑定成功！");
            	 $("#commonWin").modal("hide");
             }else{
            	 layer.msg(data.msg,{move:false});
             }
        	 myTable.refresh();
		},
		addButton : function(data){
			if($("#show3").is(":hidden")){
				$("#humidityContent").find("div.tab-pane").removeClass("active");
				$("#activeShow li").removeClass("active");
				$("#show3").show();
				$("#show3").addClass("active");
				$("#home3").addClass("active");
				$("#home3").show();
			}else{
				$("#humidityContent").find("div.tab-pane").removeClass("active");
				$("#activeShow li").removeClass("active");
				$("#show4").show();
				$("#show4").addClass("active");
				$("#home4").addClass("active");
				$("#home4").show();
				$("#addButton").hide();
			}
		},
		activeShow: function(){
			var outId=$("#outId").val();
			if(outId==27){
				$("#humidityContent").find("div.tab-pane").removeClass("active");
				$("#activeShow li").removeClass("active");
				$("#show2").show();
				$("#show2").addClass("active");
				$("#home2").addClass("active");
				$("#home2").show();
			}else if(outId==28){
				$("#humidityContent").find("div.tab-pane").removeClass("active");
				$("#activeShow li").removeClass("active");
				$("#show3").show();
				$("#show3").addClass("active");
				$("#home3").addClass("active");
				$("#home3").show();
			}else if(outId==29){
				$("#humidityContent").find("div.tab-pane").removeClass("active");
				$("#activeShow li").removeClass("active");
				$("#show4").show();
				$("#show4").addClass("active");
				$("#home4").addClass("active");
				$("#home4").show();
				$("#addButton").hide();
			}
		},
		updateShow : function(){
			var vehicle=JSON.parse($("#vehicle").attr("value"));
			$("#vehicleBrand").val(vehicle[0].brand);
			$("#vehicleId").val(vehicle[0].vehicleId);
			var type=""; 
			for(var i=0;i<vehicle.length;i++){
				if(vehicle[i].sensorOutId==26){
					type="";
				}else if(vehicle[i].sensorOutId==27){
					type=2
				}else if(vehicle[i].sensorOutId==28){
					type=3
				}else if(vehicle[i].sensorOutId==29){
					type=4
					$("#show3").show();
					$("#addButton").hide();
				}
				$("#id"+type+"").val(vehicle[i].id);
				$("#sensorNumber"+type+"").val(vehicle[i].sensorNumber);
				$("#sensorId"+type+"").val(vehicle[i].sensorId);
				$("#vehicleId"+type+"").val(vehicle[i].vehicleId);
				$("#autoTime"+type+"").val(vehicle[i].autoTime);
				$("#overValve"+type+"").val(vehicle[i].overValve);
				$("#correctionFactorK"+type+"").val(vehicle[i].correctionFactorK);
				$("#correctionFactorB"+type+"").val(vehicle[i].correctionFactorB);
				$("#alarmUp"+type+"").val(vehicle[i].alarmUp);
				$("#alarmDown"+type+"").val(vehicle[i].alarmDown);
				$("#remark"+type+"").val(vehicle[i].remark);
				$("#compensate"+type+"").val(vehicle[i].compensate);
				$("#filterFactor"+type+"").val(vehicle[i].filterFactor);
				$("#show"+type+"").show();
			}
		},
		clearInput : function(){
			var type="";
			for(var i=1;i<5;i++){
				if(i==1){
					type="";
				}else{
					type=i
				}
				$("#sensorNumber"+type+"").val("");
				$("#compensate"+type+"").val("");
				$("#filterFactor"+type+"").val("");
				$("#remark"+type+"").val("");
			}
		},
		consult : function(data){
			HumiditySettings.clearInput();
			var type="";
			for(var i=0;i<data.length;i++){
				if(data[i].sensorOutId==26){
					type="";
				}else if(data[i].sensorOutId==27){
					type=2;
				}else if(data[i].sensorOutId==28){
					type=3;
				}else if(data[i].sensorOutId==29){
					type=4;
					$("#addButton").hide();
				}
				$("#sensorNumber"+type+"").val(data[i].sensorNumber);
				$("#sensorId"+type+"").val(data[i].sensorId);
				$("#autoTime"+type+"").val(data[i].autoTime);
				$("#overValve"+type+"").val(data[i].overValve);
				$("#correctionFactorK"+type+"").val(data[i].correctionFactorK);
				$("#correctionFactorB"+type+"").val(data[i].correctionFactorB);
				$("#alarmUp"+type+"").val(data[i].alarmUp);
				$("#alarmDown"+type+"").val(data[i].alarmDown);
				$("#remark"+type+"").val(data[i].remark);
				$("#compensate"+type+"").val(data[i].compensate);
				$("#filterFactor"+type+"").val(data[i].filterFactor);
				$("#show"+type+"").show();
			}
		},
	}
	$(function(){
		$('input').inputClear();
		HumiditySettings.updateShow();
		HumiditySettings.init();
		HumiditySettings.activeShow();
		$("#doSubmit").bind("click",HumiditySettings.doSubmit);
		$("#addButton").bind("click",HumiditySettings.addButton);
	})
})(window,$)