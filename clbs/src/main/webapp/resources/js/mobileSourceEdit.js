/**
 * 
 */
(function(window,$){
	 var selectVehicleType;
	mobileSourceEdit = {
		init: function(){
		 /*   selectVehicleType = $("#vehType").attr("value");
			  var url="/clbs/m/basicinfo/monitoring/vehicle/addList";
	            json_ajax("POST",url,"json",true,null, mobileSourceEdit.getTypeCallback);
			var roles = {
				oilWearNumber : {
					required : true,
					maxlength : 20,
					isRightfulString : true,
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
			};*/
		
		},
		Calculation1:function(){			
			var calculation1=$("#travelBaseList").val();
			if(calculation1){
			$("#calculatevalue1").val(calculation1);
			}else{
				$("#calculatevalue1").val("0");
			};
		},
		Calculation2:function(){
			var calculation2=$("#idleBaseLists").val();
			if(calculation2){
			$("#calculatevalue2").val(parseFloat(calculation2))
			}else{
				$("#calculatevalue2").val("0");
			};
		},
		Calculation3:function(){
			var mileage=$("#mileage").val();
			var energyconsumption=$("#energyconsumption").val();
			var calculation3=mileage/energyconsumption;
			$("#calculatevalue3").val(calculation3);
		},
		 validates: function(){
				return $("#editForm").validate({
			          rules : {  			        
			        	  travelBase:{
        		    	   //required : true, 
        		    	   number:true, 
        		    	   maxlength:7,        		    	   
        		       },
        		       idleBase:{
        		    	  // required : true, 
        		    	   number:true,
        		    	   maxlength:7,
        		       },
        		       installTime:{
        		    	   required : true, 
        		    	   
        		       }, 
        		       idleThreshold:{
        		    	 	digits:true,        		       
        		    	   required : true, 
        		    	   number:true,       		    	   
        		    	   range:[0,20]
        		       }
       		  
			          },
				messages : {      			
					travelBase:{
          				//required: "不能为空",
          			    number: "请输入有效的数字",
          			     maxlength: $.validator.format("最多可以输入 {0} 位数"),
          			},
          			idleBase:{
          				//required: "不能为空",
          			    number: "请输入有效的数字",
          			   maxlength: $.validator.format("最多可以输入 {0} 位数"),
          			},
          			installTime:{
          				required: publicNull,          			    
          			},
          			idleThreshold:{
          				required: publicNull,
          			    number: "请输入有效的数字",
          			   range: $.validator.format("请输入范围在 {0} 到 {1} 之间的数值"),
          			   digits:"必须输入整数"
          			},
          			
				}
				}).form();
	},
	dosubmit:function()
	{		
		if(mobileSourceEdit.validates()){
			   $("#editForm").ajaxSubmit(function(data) {
                   if (data != null) {
                       var data = $.parseJSON(data);
                       if (data.success == true) {   
                    	    layer.msg(data.msg,{move:false});
                               $("#commonSmWin").modal("hide");    
                               myTable.refresh();
                       }else{
                           layer.msg(data.msg,{move:false});
                       }
                   }
               });
		}
		
	},
	onlyNumber:function(input, n)
	{
		 input.value = input.value.replace(/[^0-9\.]/ig, '');
		    var dotIdx = input.value.indexOf('.'), dotLeft, dotRight;
		    if (dotIdx >= 0) {
		        dotLeft = input.value.substring(0, dotIdx);
		        dotRight = input.value.substring(dotIdx + 1);
		        if (dotRight.length > n) {
		            dotRight = dotRight.substring(0, n);
		        }
		        input.value = dotLeft + '.' + dotRight;
		    }
	},
	//燃料类型
    fuelTypes:function(){
    	var url = "/clbs/m/basicinfo/monitoring/vehicle/findAllFuelType";
    	json_ajax("POST",url,"json",false,null, mobileSourceEdit.findFuelTypeList);
    },
    findFuelTypeList:function(data){
	var selectFuelType = $("#fuelTypes").attr("value");
    	for(var i = 0; i < data.obj.FuelTypeList.length; i++){
   			if("柴油" == data.obj.FuelTypeList[i].fuelCategory){
				if(data.obj.FuelTypeList[i].fuelType == selectFuelType){
					$("#dieselOil").append("<option selected value=" + data.obj.FuelTypeList[i].id + ">" + data.obj.FuelTypeList[i].fuelType + "</option>");
				}else{
   			   	 	$("#dieselOil").append("<option value=" + data.obj.FuelTypeList[i].id + ">" + data.obj.FuelTypeList[i].fuelType + "</option>");
				}
   			}else if("汽油" == data.obj.FuelTypeList[i].fuelCategory){
				if(data.obj.FuelTypeList[i].fuelType == selectFuelType){
		    		$("#gasoline").append("<option selected value=" + data.obj.FuelTypeList[i].id + ">" + data.obj.FuelTypeList[i].fuelType + "</option>");
				}else{
		    		$("#gasoline").append("<option value=" + data.obj.FuelTypeList[i].id + ">" + data.obj.FuelTypeList[i].fuelType + "</option>");
				}
      		}else if("天然气" == data.obj.FuelTypeList[i].fuelCategory){
  		    	if(data.obj.FuelTypeList[i].fuelType == selectFuelType){
					$("#naturalGas").append("<option selected value=" + data.obj.FuelTypeList[i].id + ">" + data.obj.FuelTypeList[i].fuelType + "</option>");
				}else{
   			   		$("#naturalGas").append("<option value=" + data.obj.FuelTypeList[i].id + ">" + data.obj.FuelTypeList[i].fuelType + "</option>");
				}
  		    }
  		} 
    },
    getTypeCallback: function(data){
        for (var i = 0; i < data.obj.VehicleTypeList.length; i++){
            if (data.obj.VehicleTypeList[i].id == selectVehicleType) { // 选中值 
                $("#vehicleType").append("<option selected value=" + data.obj.VehicleTypeList[i].id + ">" + data.obj.VehicleTypeList[i].vehicleType + "</option>");
            }else{
                $("#vehicleType").append("<option value=" + data.obj.VehicleTypeList[i].id + ">" + data.obj.VehicleTypeList[i].vehicleType + "</option>");
            }
        }
    },
    getdate:function(data) {
     if(data){
        var now = new Date(data),
            y = now.getFullYear(),
            m = now.getMonth() + 1,
            d = now.getDate();
        return y + "-" + (m < 10 ? "0" + m : m) + "-" + (d < 10 ? "0" + d : d) /*+ " " + now.toTimeString().substr(0, 8)*/;
    }
    },
    getDate:function(shijianchuo) {  
    	  //shijianchuo是整数，否则要parseInt转换  
    	  var time = new Date(shijianchuo);  
    	  var y = time.getFullYear();  
    	  var m = time.getMonth()+1;  
    	  var d = time.getDate();  
    	  var h = time.getHours();  
    	  var mm = time.getMinutes();  
    	  var s = time.getSeconds();  
    	  return y+'-'+add0(m)+'-'+add0(d)+' '+add0(h)+':'+add0(mm)+':'+add0(s); 
    	  function add0(m){return m<10?'0'+m:m };  
    	},
    	   //将时间戳变成小时分秒
        changdatainfo:function(data){
        	var day = parseInt(data / (24*60*60));//计算整数天数
        	var afterDay = data - day*24*60*60;//取得算出天数后剩余的秒数
        	var hour = parseInt(afterDay/(60*60));//计算整数小时数
        	var afterHour = data - day*24*60*60 - hour*60*60;//取得算出小时数后剩余的秒数
        	var min = parseInt(afterHour/60);//计算整数分
        	var afterMin = parseInt(data - day*24*60*60 - hour*60*60 - min*60);//取得算出分后剩余的秒数
        	   if(day!=0&&hour!=0&&min!=0){
        	   var time=day+"天"+hour+"小时"+min+"分"+afterMin+"秒";
        	 return time;
        	   }else{
        		   	var time=day+"天"+hour+"小时"+min+"分"+afterMin+"秒";
        		   if(day==0){
        			   var time=time.replace(/0天/,"");
        		   }
        		  if(hour==0){
        		   var time=time.replace(/0小时/,"");
        		   }
        		   if(min==0){
        			   var time=time.replace(/0分/,"");
        		   }
        		    return time;
        	   }
        	        	        	       	
         },
    
	}
	$(function(){
		$('input').inputClear();
		laydate.render({elem:'#productdata',theme:'#6dcff6',done:function(value, date){			
			  if(value){
			  var url = "/clbs/m/energy/mobileSourceBaseInfo/verifyInstallTime";
		    	var carid=$("#vehicleId").val();
		    	data=Date.parse(value);
		    	json_ajax("POST",url,"json",false,{"vehicleId":carid,"installTime":data}, getdataback);
		    	function getdataback(data){	
		    		if (data != null) {
		                if (data.success == true) {
		                        if(date != null){
		                            //layer.msg(data.msg,{move:false});
		                        }
		                    
		                }else{
		                    layer.msg(data.msg,{move:false});
		        			$("#productdata").val("");
		                   
		                }
		            }
		    	}
			  
			  
		  }
		}});
		
		mobileSourceEdit.init();
		///mobileSourceEdit.fuelTypes();	
		var datevalue=$("#productdata").val();
		var datevalue=mobileSourceEdit.getdate(datevalue);
		$("#productdata").val(datevalue);
	   $("#switch").find("li").eq("0").on("click",function(){
	    	$("#doSubmitEdit").attr("disabled","disabled")
	    	$("#basicinfo").show();
	    	 $("#extendinfo").hide();
	    })
	     $("#switch").find("li").eq("1").on("click",function(){
	    	 $("#doSubmitEdit").removeAttr("disabled");
	    	 $("#extendinfo").show();
	    	 $("#basicinfo").hide();
	    })
		$("#doSubmitEdit").on("click",mobileSourceEdit.dosubmit);
		var idlingtime=$("#Idlelength").val();
	    if(idlingtime){
	    var  idlingtimevalue=mobileSourceEdit.changdatainfo(idlingtime/1000);
         }
	    $("#Idlelength").val(idlingtimevalue);
	})
})(window,$)