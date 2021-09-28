(function($,window){
    var $label = $("label");
    var initValue = $("#parameterValue").val();
	alarmSettings = {
		init: function(){
			//车辆list 
			var referVehicleList = JSON.parse($("#referVehicleList").attr("value"));
			// 初始化车辆数据 
	        var dataList = {value: []};
	        if (referVehicleList != null && referVehicleList.length > 0) {
            	var vehicleId = $("#vehicleId").val();
	        	for (var i=0; i<referVehicleList.length; i++) {
	            	//删除相同车牌信息
            		if(referVehicleList[i].vehicleId == vehicleId){
            			referVehicleList.splice(i,1);
            		}
            		var obj = {};
                  	//处理参考车牌只存在一条数据时防止浏览器抛出错误信息
                  	if(referVehicleList[i] == undefined){
                        dataList.value.push(obj);
                  	}else{
    	                obj.id = referVehicleList[i].vehicleId;
    	                obj.name = referVehicleList[i].brand;
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
		        $.ajax({
		            type: 'POST',
		            url: '/clbs/a/alarmSetting/getAlarmParameter_'+vehicleId+'.gsp',
		            async:false,
		            dataType: 'json',
		            success: function (data) {
		            	if (data.obj.length != 0) {	 
		            		//清除当前车辆报警参数
		            		$("input[name='selectinfo']").each(function(){
		            			$(this).val("1");
		            			$(this).next().css("left","70px");
		            			$(this).parent().parent().parent().parent().find("input[id='parameterValue']").val("");
		            		});
		            		//读取参考车辆报警参数
		            		var alarm = data.obj;
		            		for(var i = 0; i < alarm.length; i++){            			
		            			var checkId=alarm[i].alarmParameterId;	 
		            			var paramValue=alarm[i].parameterValue;
		            			$("#"+checkId).parent().parent().parent().parent().find("input[id='parameterValue']").val(paramValue);
		            			$("#"+checkId).val(alarm[i].alarmPush)
		            		}
		            		alarmSettings.selectPosition();
		            	}	            	
		            },
		            error: function () {
		            	layer.msg(systemError, {move: false});	
		            }
		        });
			}).on('onUnsetSelectValue', function () {
			});
	        $(".modal-body").addClass("modal-body-overflow");
		},
	    //提交
		doSubmit: function(){
            var len = $("#settingForm label.error:visible").length;
            if (len > 0) {
            	return;
            }{
	    	var checkedParams = [];
			$("input[name='selectinfo']").each(function(index,item){
	    		var checkedId = $(this).attr("id");
	    		var paramValue =$(this).parent().parent().parent().parent().find("input[id='parameterValue']").val();	    		
	    		var alarmPush=$(this).val();
	    		var pos=$(this).parent().parent().parent().parent().find(".typeName").attr("id");
    			var obj={};
	    		obj.alarmParameterId = checkedId;
				obj.parameterValue = paramValue;
	    		obj.vehicleId = $("#vehicleId").attr("value");
	    		obj.alarmPush = alarmPush; //取button的值
	    		obj.pos = pos;
    			checkedParams.push(obj);
	    	});
			var deviceType = $("input[name='deviceCheck']:checked").val();
			$("#checkedParams").val(JSON.stringify(checkedParams));
			$("#deviceType").val(deviceType);
			addHashCode1($("#settingForm"));
			$("#settingForm").ajaxSubmit(function(data) {
	             $("#commonWin").modal("hide");
	             if(data != null){
	            	 var data = $.parseJSON(data)
	            	 if (data.success) {
			             var checkedList = new Array();
			             var settingUrl = settingMoreUrl.replace("{id}.gsp", checkedList.toString() + ".gsp?deviceType=" + deviceType);
			     		 $("#settingMoreBtn").attr("href",settingUrl);
	            		 layer.msg("设置成功！",{move:false});
	            		 myTable.refresh();
		             } else{
		            	 layer.msg(data.msg,{move:false});
		             }
	             }
	        });
			}
	    },
	    leabelClickFn: function(){	    	
	    	var left=$(this).parent().parent().parent().find(".selectbutton").css("left");
	    	if(left=="70px"){
	    		$(this).parent().parent().parent().find(".selectbutton").animate({left:"118px"},"fast");	    		
	    		$(this).parent().parent().parent().find(".selectvalue").val(2);	    		
	    	}else if(left=="118px"){
	    		$(this).parent().parent().parent().find(".selectbutton").animate({left:"24px"},"fast");
	    		$(this).parent().parent().parent().find(".selectvalue").val(0);	    		
	    	}else{
	    		$(this).parent().parent().parent().find(".selectbutton").animate({left:"70px"},"fast");
	    		$(this).parent().parent().parent().find(".selectvalue").val(1);	    		
	    	}
	    	
	    },
	    leabelClickFn2: function(tt){	
	        var left=$("#"+tt).parent().parent().parent().find(".selectbutton").css("left");
	    	if(left=="70px"){
	    		$("#"+tt).parent().parent().parent().find(".selectbutton").animate({left:"118px"},"fast");	    		
	    		$("#"+tt).parent().parent().parent().find(".selectvalue").val(2);	    
	    	}else if(left=="118px"){
	    		$("#"+tt).parent().parent().parent().find(".selectbutton").animate({left:"24px"},"fast");
	    		$("#"+tt).parent().parent().parent().find(".selectvalue").val(0);
	    	}else{
	    		$("#"+tt).parent().parent().parent().find(".selectbutton").animate({left:"70px"},"fast");
	    		$("#"+tt).parent().parent().parent().find(".selectvalue").val(1);	 
	    	}
	    },
	    selectSwitch:function(){
	    	$(".leftselectbutton span").on("click",function(){
				var leftbutton=$(this).siblings(".selectbutton").css("left");
				var left = $(this).css("left");
				var classname=$(this).attr("class");
				if(classname=="button1"){
					$(this).parent().find(".selectvalue").val("0");
				}else if(classname=="button2"){
					$(this).parent().find(".selectvalue").val("1");
				}else{
					$(this).parent().find(".selectvalue").val("2");
				}
				$(this).siblings(".selectbutton").animate(
						{left: left},"fast")
			})
	    },
        //点击头部文字全选
        topswitch:function(){
        	var allid= new Array("#home1","#home2")
	    	for(var i = 0; i < allid.length; i++){
	    		var $this=allid[i];	 
	    		$($this).find(".noneset").on("click",function(){	  	    		  
		    		$(this).parent().parent().find(".selectbutton").animate({left:"24px"},"fast");	
		    		$(this).parent().parent().find(".selectvalue").val(0);
	    		})
	    		$($this).find(".partset").on("click",function(){	  	    		  
		    		$(this).parent().parent().find(".selectbutton").animate({left:"70px"},"fast");	
		    		$(this).parent().parent().find(".selectvalue").val(1);
	    		})
	    		$($this).find(".wholeset").on("click",function(){	  	    		  
		    		$(this).parent().parent().find(".selectbutton").animate({left:"118px"},"fast");	 
		    		$(this).parent().parent().find(".selectvalue").val(2);
	    		})
	    	}
	    },
	    selectPosition:function(){
	    	$(".selectvalue").each(function(){			
				var selectvalueno=$(this).val();
				selectvalueno=parseInt(selectvalueno);
				if(parseInt(selectvalueno)==0){
					$(this).next().css("left","24px");
				}else if(parseInt(selectvalueno)==1){
					$(this).next().css("left","70px");
				}else{
					$(this).next().css("left","118px");
				}
			})
	    }
	}
	$(function(){
		$('input').inputClear();
		alarmSettings.init();
		myTable.add('commonWin', 'settingForm', null, null);
		$label.bind("click",alarmSettings.leabelClickFn);
		alarmSettings.topswitch();
	    //滑块选择切换
		alarmSettings.selectSwitch();
		//预警按钮设定
		alarmSettings.selectPosition();
	})
	$label.css("cursor","pointer");
	$(".noneset").css("cursor","pointer");
	$(".partset").css("cursor","pointer");
	$(".wholeset").css("cursor","pointer");
	$(".typeName").mouseover(function(){
		$(this).css("color","#6dcff6");
	});
	$(".typeName").mouseleave(function(){
		$(this).css("color","#5D5F63");
	});
	$("#textinfo span").mouseover(function(){
		$(this).css("color","#6dcff6");
	});
	$("#textinfo span").mouseleave(function(){
		$(this).css("color","#5D5F63");
	});
})($,window)