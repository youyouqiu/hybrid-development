/**
 * 右键菜单  设置休眠唤醒
 */
/*(function(window,$){*/
	
	var _timingWakeUpCheckList;//定时唤醒日设置集合
    var nowDate = new Date();//当前日期
	var nowTime;//当前时间
	var vehicleId = $("#obtainVid").val();//车辆ID
	
	vedioSleepSettingLoad = {
			
		init: function(){
			
        	//菜单隐藏
            $('#rMenu').css({"visibility": "hidden"});
            
			//模态框添加类样式
			$(".modal-body").addClass("modal-body-overflow");
			$(".modal-dialog").css("width","60%");

            //时间初始化
            laydate.render({elem: '#wakeOpenOneTime',theme: '#6dcff6',type: 'time',format: 'HH:mm'});
            laydate.render({elem: '#wakeCloseOneTime',theme: '#6dcff6',type: 'time',format: 'HH:mm'});
            laydate.render({elem: '#wakeOpenTwoTime',theme: '#6dcff6',type: 'time',format: 'HH:mm'});
            laydate.render({elem: '#wakeCloseTwoTime',theme: '#6dcff6',type: 'time',format: 'HH:mm'});
            laydate.render({elem: '#wakeOpenThreeTime',theme: '#6dcff6',type: 'time',format: 'HH:mm'});
            laydate.render({elem: '#wakeCloseThreeTime',theme: '#6dcff6',type: 'time',format: 'HH:mm'});
            laydate.render({elem: '#wakeOpenFourTime',theme: '#6dcff6',type: 'time',format: 'HH:mm'});
            laydate.render({elem: '#wakeCloseFourTime',theme: '#6dcff6',type: 'time',format: 'HH:mm'});

            //定时唤醒日设置集合
			_timingWakeUpCheckList = new vedioSleepSettingLoad.mapList();
			
			//获取当前年月日
			nowTime = nowDate.getFullYear()
		    + "-" + (parseInt(nowDate.getMonth() + 1) < 10 ? "0" + parseInt(nowDate.getMonth() + 1) : parseInt(nowDate.getMonth() + 1))
		    + "-" + (nowDate.getDate() < 10 ? "0" + nowDate.getDate() : nowDate.getDate());
			
			var vehicleId = $("#obtainVid").val();
			var url = "/clbs/realTimeVideo/videoSetting/getVideoSleepParam";
			json_ajax("POST", url, "json", true, {"vehicleId":vehicleId}, vedioSleepSettingLoad.getVideoSleepParamCallBack);
		},
		
		//获取休眠唤醒参数回调方法（组装参数至界面）
		getVideoSleepParamCallBack: function(data){
			if (data.obj != null) { //若设置了休眠唤醒参数则组装显示值
				var info = data.obj;
				$("#makeUpManually").val(info.wakeupHandSign);//手动唤醒
				$("#awakeningConditions").val(info.wakeupConditionSign);//条件唤醒
				vedioSleepSettingLoad.awakeningConditionsChangeFn();//条件唤醒参数显示隐藏
				$("#wakeUpRegularly").val(info.wakeupTimeSign);//定时唤醒
				vedioSleepSettingLoad.wakeUpRegularlyChangeFn();//定时唤醒参数显示隐藏
				
				//唤醒条件勾选判断
				var wakeupConditionTen = info.wakeupCondition;//十进制唤醒条件
				var wakeupConditionTwo = wakeupConditionTen.toString(2);//二进制唤醒条件
				var wctLength = wakeupConditionTwo.length;//二进制唤醒条件值长度
				if (wctLength == 1) {
					wakeupConditionTwo = "00" + wakeupConditionTwo;
				} else if (wctLength == 2) {
					wakeupConditionTwo = "0" + wakeupConditionTwo;
				}
				if (wakeupConditionTwo.substring(0,1) == "1") { //车辆开门
					$("#openTheCar").attr("checked","checked");
				}
				if (wakeupConditionTwo.substring(1,2) == "1") { //碰撞侧翻报警
					$("#collisionRolloverAlarm").attr("checked","checked");
				}
				if (wakeupConditionTwo.substring(2) == "1") { //紧急报警
					$("#emergencyAlarm").attr("checked","checked");
				}
				
				//定时唤醒周几勾选判断
				var wakeupTimeTen = info.wakeupTime;//十进制周几值
				var wakeupTimeTwo = wakeupTimeTen.toString(2);//二进制周几值
				var wtLength = wakeupTimeTwo.length;//二进制周几值长度
				var wtFlag = false;
				if (wtLength == 1) {
					wakeupTimeTwo = "000000" + wakeupTimeTwo;
				} else if (wtLength == 2) {
					wakeupTimeTwo = "00000" + wakeupTimeTwo;
				} else if (wtLength == 3) {
					wakeupTimeTwo = "0000" + wakeupTimeTwo;
				} else if (wtLength == 4) {
					wakeupTimeTwo = "000" + wakeupTimeTwo;
				} else if (wtLength == 5) {
					wakeupTimeTwo = "00" + wakeupTimeTwo;
				} else if (wtLength == 6) {
					wakeupTimeTwo = "0" + wakeupTimeTwo;
				}
				if (wakeupTimeTwo.substring(0,1) == "1") { //周日
					$("#regularly_7").attr("checked","checked");
					vedioSleepSettingLoad.listAddKeyAndValueFn(_timingWakeUpCheckList,"regularly_7","1");
					wtFlag = true;
				}
				if (wakeupTimeTwo.substring(1,2) == "1") { //周六
					$("#regularly_6").attr("checked","checked");
					vedioSleepSettingLoad.listAddKeyAndValueFn(_timingWakeUpCheckList,"regularly_6","1");
					wtFlag = true;
				}
				if (wakeupTimeTwo.substring(2,3) == "1") { //周五
					$("#regularly_5").attr("checked","checked");
					vedioSleepSettingLoad.listAddKeyAndValueFn(_timingWakeUpCheckList,"regularly_5","1");
					wtFlag = true;
				}
				if (wakeupTimeTwo.substring(3,4) == "1") { //周四
					$("#regularly_4").attr("checked","checked");
					vedioSleepSettingLoad.listAddKeyAndValueFn(_timingWakeUpCheckList,"regularly_4","1");
					wtFlag = true;
				}
				if (wakeupTimeTwo.substring(4,5) == "1") { //周三
					$("#regularly_3").attr("checked","checked");
					vedioSleepSettingLoad.listAddKeyAndValueFn(_timingWakeUpCheckList,"regularly_3","1");
					wtFlag = true;
				}
				if (wakeupTimeTwo.substring(5,6) == "1") { //周二
					$("#regularly_2").attr("checked","checked");
					vedioSleepSettingLoad.listAddKeyAndValueFn(_timingWakeUpCheckList,"regularly_2","1");
					wtFlag = true;
				}
				if (wakeupTimeTwo.substring(6) == "1") { //周一
					$("#regularly_1").attr("checked","checked");
					vedioSleepSettingLoad.listAddKeyAndValueFn(_timingWakeUpCheckList,"regularly_1","1");
					wtFlag = true;
				}
				
				if (wtFlag) { //解除时间段操作限制
					$(".disabled-attribute").removeAttr("disabled");
				}
				
				//定时唤醒时间段勾选判断
				var wakeupTimeFlagTen = info.wakeupTimeFlag;//十进制时间段值
				var wakeupTimeFlagTwo = wakeupTimeFlagTen.toString(2);//二进制时间段值
				var wtfLength = wakeupTimeFlagTwo.length;//二进制时间段值长度
				if (wtfLength == 1) {
					wakeupTimeFlagTwo = "000" + wakeupTimeFlagTwo;
				} else if (wtfLength == 2) {
					wakeupTimeFlagTwo = "00" + wakeupTimeFlagTwo;
				} else if (wtfLength == 3) {
					wakeupTimeFlagTwo = "0" + wakeupTimeFlagTwo;
				}
				if (wakeupTimeFlagTwo.substring(0,1) == "1") { //时间段4
					$("#regularlyFourTime").attr("checked","checked");
				}
				if (wakeupTimeFlagTwo.substring(1,2) == "1") { //时间段3
					$("#regularlyThreeTime").attr("checked","checked");
				}
				if (wakeupTimeFlagTwo.substring(2,3) == "1") { //时间段2
					$("#regularlyTwoTime").attr("checked","checked");
				}
				if (wakeupTimeFlagTwo.substring(3) == "1") { //时间段1
					$("#regularlyOneTime").attr("checked","checked");
				}
				
				//时间段具体值组装
				var wakeupTime;//时间段唤醒时间
				var wakeupClose;//时间段关闭时间
				if($("#regularlyOneTime").is(":checked")){
					$("#wakeOpenOneTime").val(info.wakeupTime1);
					$("#wakeCloseOneTime").val(info.wakeupClose1);
				}
				if($("#regularlyTwoTime").is(":checked")){
					$("#wakeOpenTwoTime").val(info.wakeupTime2);
					$("#wakeCloseTwoTime").val(info.wakeupClose2);
				}
				if($("#regularlyThreeTime").is(":checked")){
					$("#wakeOpenThreeTime").val(info.wakeupTime3);
					$("#wakeCloseThreeTime").val(info.wakeupClose3);
				}
				if($("#regularlyFourTime").is(":checked")){
					$("#wakeOpenFourTime").val(info.wakeupTime4);
					$("#wakeCloseFourTime").val(info.wakeupClose4);
				}
				
			}
		},
		
		//封装map集合
		mapList: function (){
		    this.elements = new Array();
		    //获取MAP元素个数
		    this.size = function () {
		        return this.elements.length;
		    };
		    //判断MAP是否为空
		    this.isEmpty = function () {
		        return (this.elements.length < 1);
		    };
		    //删除MAP所有元素
		    this.clear = function () {
		        this.elements = new Array();
		    };
		    //向MAP中增加元素（key, value)
		    this.put = function (_key, _value) {
		        this.elements.push({
		            key: _key,
		            value: _value
		        });
		    };
		    //删除指定KEY的元素，成功返回True，失败返回False
		    this.remove = function (_key) {
		        var bln = false;
		        try {
		            for (var i = 0, len = this.elements.length; i < len; i++) {
		                if (this.elements[i].key == _key) {
		                    this.elements.splice(i, 1);
		                    return true;
		                }
		            }
		        } catch (e) {
		            bln = false;
		        }
		        return bln;
		    };
		    //获取指定KEY的元素值VALUE，失败返回NULL
		    this.get = function (_key) {
		        try {
		            for (var i = 0, len = this.elements.length; i < len; i++) {
		                if (this.elements[i].key == _key) {
		                    return this.elements[i].value;
		                }
		            }
		        } catch (e) {
		            return null;
		        }
		    };
		    //获取指定索引的元素（使用element.key，element.value获取KEY和VALUE），失败返回NULL
		    this.element = function (_index) {
		        if (_index < 0 || _index >= this.elements.length) {
		            return null;
		        }
		        return this.elements[_index];
		    };
		    //判断MAP中是否含有指定KEY的元素
		    this.containsKey = function (_key) {
		        var bln = false;
		        try {
		            for (var i = 0, len = this.elements.length; i < len; i++) {
		                if (this.elements[i].key == _key) {
		                    bln = true;
		                }
		            }
		        } catch (e) {
		            bln = false;
		        }
		        return bln;
		    };
		    //判断MAP中是否含有指定VALUE的元素
		    this.containsValue = function (_value) {
		        var bln = false;
		        try {
		            for (var i = 0, len = this.elements.length; i < len; i++) {
		                if (this.elements[i].value == _value) {
		                    bln = true;
		                }
		            }
		        } catch (e) {
		            bln = false;
		        }
		        return bln;
		    };
		    //获取MAP中所有VALUE的数组（ARRAY）
		    this.values = function () {
		        var arr = new Array();
		        for (var i = 0, len = this.elements.length; i < len; i++) {
		            arr.push(this.elements[i].value);
		        }
		        return arr;
		    };
		    //获取MAP中所有KEY的数组（ARRAY）
		    this.keys = function () {
		        var arr = new Array();
		        for (var i = 0, len = this.elements.length; i < len; i++) {
		            arr.push(this.elements[i].key);
		        }
		        return arr;
		    };
		},
		 
		//集合添加函数(去重)
		listAddKeyAndValueFn: function(_thisList,_thisId,_value){
			if(_thisList.isEmpty()){
				_thisList.put(_thisId,_value);
			}else{
				if(_thisList.containsKey(_thisId)){
					_thisList.remove(_thisId);
				}
				_thisList.put(_thisId,_value);
			}
		},
		
		//定时唤醒日设置点击函数
		timingWakeUpCheckFn: function(){
			var _thisId = $(this).attr("id");
			//定时唤醒日设置集合添加key value
			if($("#"+_thisId).is(":checked")){
				vedioSleepSettingLoad.listAddKeyAndValueFn(_timingWakeUpCheckList,_thisId,"1");
			}else{
				vedioSleepSettingLoad.listAddKeyAndValueFn(_timingWakeUpCheckList,_thisId,"0");
			}
			//时间段唤醒时间禁用与否
			if(_timingWakeUpCheckList.containsValue("1")){
				$(".disabled-attribute").removeAttr("disabled");
			}else{
				$(".disabled-attribute").prop("disabled",true);
			}
		},
		
		//条件唤醒下拉框改变函数
		awakeningConditionsChangeFn: function(){
			if($("#awakeningConditions").val() == 1){
				$("#conditionAlarmType").removeClass("hidden");
			}else{
				$("#conditionAlarmType").addClass("hidden");
			}
		},
		
		//定时唤醒下拉框改变函数
		wakeUpRegularlyChangeFn: function(){
			if($("#wakeUpRegularly").val() == 1){
				$("#wakeUpRegularlyAlarmType").removeClass("hidden");
			}else{
				$("#wakeUpRegularlyAlarmType").addClass("hidden");
			}
		},
		
		//显示错误信息
		showErrorMsg: function(msg, inputId){
            var error = $("#error_label_add").length;
            if(error == 0){
                $(".modal-footer").append("<label id='error_label_add' class='error hidden'></label>");
            }
            if ($("#error_label_add").hasClass("hidden")) {
                $("#error_label_add").text(msg);
                $("#error_label_add").insertAfter($("#" + inputId));
                $("#error_label_add").removeClass("hidden");
            } else {
                $("#error_label_add").addClass("hidden");
            }
        },
        
        //隐藏错误提示信息
        hideErrorMsg: function(){
            $("#error_label_add").addClass("hidden");
        },
		
		//设置休眠唤醒提交函数
		sendSubmitFn: function(){
			
			//获取休眠唤醒模式
			//手动唤醒
			var makeUpManually;
			if($("#makeUpManually>option:selected").val() == 1){
				makeUpManually = 1;
			}else{
				makeUpManually = 0;
			}
			//条件唤醒
			var awakeningConditions;
			if($("#awakeningConditions>option:selected").val() == 1){
				awakeningConditions = 1;
			}else{
				awakeningConditions = 0;
			}
			//定时唤醒
			var wakeUpRegularly;
			if($("#wakeUpRegularly>option:selected").val() == 1){
				wakeUpRegularly = 1;
			}else{
				wakeUpRegularly = 0;
			}
			
			//获取唤醒条件类型
			//紧急报警
			var emergencyAlarm;
			if($("#emergencyAlarm").is(":checked")){
				emergencyAlarm = 1;
			}else{
				emergencyAlarm = 0;
			}
			//碰撞侧翻报警
			var collisionRolloverAlarm;
			if($("#collisionRolloverAlarm").is(":checked")){
				collisionRolloverAlarm = 1;
			}else{
				collisionRolloverAlarm = 0;
			}
			//车辆开门
			var openTheCar;
			if($("#openTheCar").is(":checked")){
				openTheCar = 1;
			}else{
				openTheCar = 0;
			}
			var wakeUpConditionType = openTheCar.toString() + collisionRolloverAlarm.toString() + emergencyAlarm.toString();//唤醒条件类型二进制
			var wakeupCondition = parseInt(wakeUpConditionType,2);//唤醒条件类型十进制
			
			//获取定时唤醒日设置
			//周一
			var regularly_1;
			if($("#regularly_1").is(":checked")){
				regularly_1 = 1;
			}else{
				regularly_1 = 0;
			}
			//周二
			var regularly_2;
			if($("#regularly_2").is(":checked")){
				regularly_2 = 1;
			}else{
				regularly_2 = 0;
			}
			//周三
			var regularly_3;
			if($("#regularly_3").is(":checked")){
				regularly_3 = 1;
			}else{
				regularly_3 = 0;
			}
			//周四
			var regularly_4;
			if($("#regularly_4").is(":checked")){
				regularly_4 = 1;
			}else{
				regularly_4 = 0;
			}
			//周五
			var regularly_5;
			if($("#regularly_5").is(":checked")){
				regularly_5 = 1;
			}else{
				regularly_5 = 0;
			}
			//周六
			var regularly_6;
			if($("#regularly_6").is(":checked")){
				regularly_6 = 1;
			}else{
				regularly_6 = 0;
			}
			//周日
			var regularly_7;
			if($("#regularly_7").is(":checked")){
				regularly_7 = 1;
			}else{
				regularly_7 = 0;
			}
			var wakeUpTimeSetting = regularly_7.toString() + regularly_6.toString() + regularly_5.toString() + regularly_4.toString() + regularly_3.toString() + regularly_2.toString() + regularly_1.toString();//定时唤醒日二进制
			var wakeupTime = parseInt(wakeUpTimeSetting,2);//定时唤醒日十进制
			
			//获取定时唤醒启用标志
			//时间段1唤醒时间
			var regularlyOneTime;
			var wakeupTime1;//时间段1唤醒时间
			var wakeupClose1;//时间段1关闭时间
			if($("#regularlyOneTime").is(":checked")){
				regularlyOneTime = 1;
				wakeupTime1 = $("#wakeOpenOneTime").val();
				wakeupClose1 = $("#wakeCloseOneTime").val();
			}else{
				regularlyOneTime = 0;
				wakeupTime1 = "00:00";
				wakeupClose1 = "00:00";
			}
			
			//时间段2唤醒时间
			var regularlyTwoTime;
			var wakeupTime2;//时间段2唤醒时间
			var wakeupClose2;//时间段2关闭时间
			if($("#regularlyTwoTime").is(":checked")){
				regularlyTwoTime = 1;
				wakeupTime2 = $("#wakeOpenTwoTime").val();
				wakeupClose2 = $("#wakeCloseTwoTime").val();
			}else{
				regularlyTwoTime = 0;
				wakeupTime2 = "00:00";
				wakeupClose2 = "00:00";
			}
			
			//时间段3唤醒时间
			var regularlyThreeTime;
			var wakeupTime3;//时间段3唤醒时间
			var wakeupClose3;//时间段3关闭时间
			if($("#regularlyThreeTime").is(":checked")){
				regularlyThreeTime = 1;
				wakeupTime3 = $("#wakeOpenThreeTime").val();
				wakeupClose3 = $("#wakeCloseThreeTime").val();
			}else{
				regularlyThreeTime = 0;
				wakeupTime3 = "00:00";
				wakeupClose3 = "00:00";
			}
			
			//时间段4唤醒时间
			var regularlyFourTime;
			var wakeupTime4;//时间段4唤醒时间
			var wakeupClose4;//时间段4关闭时间
			if($("#regularlyFourTime").is(":checked")){
				regularlyFourTime = 1;
				wakeupTime4 = $("#wakeOpenFourTime").val();
				wakeupClose4 = $("#wakeCloseFourTime").val();
			}else{
				regularlyFourTime = 0;
				wakeupTime4 = "00:00";
				wakeupClose4 = "00:00";
			}
			var regularWakeUpEnableFlag = regularlyFourTime.toString() + regularlyThreeTime.toString() + regularlyTwoTime.toString() + regularlyOneTime.toString();//定时唤醒启用标志二进制
			var wakeupTimeFlag = parseInt(regularWakeUpEnableFlag,2);//定时唤醒启用标志十进制

			if(vedioSleepSettingLoad.validates() && vedioSleepSettingLoad.judgetimerepeat()){

				//提交接口
				var url = "/clbs/realTimeVideo/video/sendParamCommand";//请求地址
				//参数
				var parameter = {
					vehicleId: vehicleId,//车辆id
					
					wakeupHandSign: makeUpManually,//手动唤醒模式
					wakeupConditionSign: awakeningConditions,//条件唤醒模式
					wakeupTimeSign: wakeUpRegularly,//定时唤醒模式
					
					wakeupCondition: wakeupCondition,//唤醒条件类型
					wakeupTime: wakeupTime,//定时唤醒日设置
					wakeupTimeFlag: wakeupTimeFlag,//定时唤醒启用标志
					wakeupTime1: wakeupTime1,//时间段1唤醒时间
					wakeupClose1: wakeupClose1,//时间段1关闭时间
					wakeupTime2: wakeupTime2,//时间段2唤醒时间
					wakeupClose2: wakeupClose2,//时间段2关闭时间
					wakeupTime3: wakeupTime3,//时间段3唤醒时间
					wakeupClose3: wakeupClose3,//时间段3关闭时间
					wakeupTime4: wakeupTime4,//时间段4唤醒时间
					wakeupClose4: wakeupClose4,//时间段4关闭时间
					orderType: 9
				};
				json_ajax("POST", url, "json", true, parameter, function(data){
					if(data.success){
						$("#commonWin").modal("hide");
						layer.msg("下发成功");
						//日志记录
		    			window.logFindCilck();
					}else{
						layer.msg(data.msg);
					}
				});
				
			}
		},
		//验证时间不能为空和开始时间不能早于关闭时间
		validates:function(){
			return $("#settingForm").validate({
				ignore : [],	    	
				rules : {		    			
			        wakeOpenOneTime:{
			     		isCheckedRequested2:"#regularlyOneTime,#wakeUpRegularly",	     		
			     	},
			     	wakeOpenTwoTime:{
			     		isCheckedRequested2:"#regularlyTwoTime,#wakeUpRegularly",	     		
			     	},
			     	wakeOpenThreeTime:{
			     		isCheckedRequested2:"#regularlyThreeTime,#wakeUpRegularly",	     		
			     	},
			     	wakeOpenFourTime:{
			     		isCheckedRequested2:"#regularlyFourTime,#wakeUpRegularly",	     		
			     	},
			     	wakeCloseOneTime:{
			     		isCheckedRequested2:"#regularlyOneTime,#wakeUpRegularly",
			     		isCheckedtime:"#regularlyOneTime,#wakeUpRegularly,#wakeOpenOneTime,#wakeCloseOneTime"
			     	},
			     	wakeCloseTwoTime:{
			     		isCheckedRequested2:"#regularlyTwoTime,#wakeUpRegularly",	
			     		isCheckedtime:"#regularlyTwoTime,#wakeUpRegularly,#wakeOpenTwoTime,#wakeCloseTwoTime"
			     	},
			     	wakeCloseThreeTime:{
			     		isCheckedRequested2:"#regularlyThreeTime,#wakeUpRegularly",
			     		isCheckedtime:"#regularlyThreeTime,#wakeUpRegularly,#wakeOpenThreeTime,#wakeCloseThreeTime"
			     	},
			     	wakeCloseFourTime:{
			     		isCheckedRequested2:"#wakeuptiregularlyFourTimeme4,#wakeUpRegularly",
			     		isCheckedtime:"#regularlyFourTime,#wakeUpRegularly,#wakeOpenFourTime,#wakeCloseFourTime"
			     	},
		    	},
		    	messages:{
		    		wakeOpenOneTime:{
			     		isCheckedRequested2:"请选择时间",	     		
			     	},
			     	wakeOpenTwoTime:{
			     		isCheckedRequested2:"请选择时间",	     		
			     	},
			     	wakeOpenThreeTime:{
			     		isCheckedRequested2:"请选择时间",	     		
			     	},
			     	wakeOpenFourTime:{
			     		isCheckedRequested2:"请选择时间",	     		
			     	},
			     	wakeCloseOneTime:{
			     		isCheckedRequested2:"请选择时间",	
			     		isCheckedtime:"请选择在唤醒时间之后的时间"
			     	},
			     	wakeCloseTwoTime:{
			     		isCheckedRequested2:"请选择时间",	
			     		isCheckedtime:"请选择在唤醒时间之后的时间"
			     	},
			     	wakeCloseThreeTime:{
			     		isCheckedRequested2:"请选择时间",
			     		isCheckedtime:"请选择在唤醒时间之后的时间"
			     	},
			     	wakeCloseFourTime:{
			     		isCheckedRequested2:"请选择时间",
			     		isCheckedtime:"请选择在唤醒时间之后的时间"
			     	},
		    	}	
	    	}).form();
	    },
	    //判断时间段是否有重复
	    judgetimerepeat:function(){
	    	var startTimeArr = [];
	    	var endTimeArr = [];
	    	var timeE = '',timeS = '';
	    	for(var i = 0,len = $('.startTime').length; i < len ; i++){
	    		if($('.startTime').eq(i).parent().parent().find('input[type="checkbox"]').is(":checked")){
	    	        timeS = $('.startTime').eq(i).val();
	    	        startTimeArr.push(timeS);
	    		}
	    	}
	    	for(var j = 0,len = $('.endTime').length; j < len; j++){
	    	    if($('.endTime').eq(j).parent().parent().find('input[type="checkbox"]').is(":checked")){  
	    	    	timeE = $('.endTime').eq(j).val();
	    	        endTimeArr.push(timeE);
	    	    }
	    	}
	    	var begin = startTimeArr.sort();
	    	var over = endTimeArr.sort();
	    	for(var k=1;k<begin.length;k++){
	    	    if (begin[k] <= over[k-1]){
	    	        layer.msg("时间段存在重叠！");
	    	        return false;
	    	    }
	    	}
	    	return true;
	    },
		
			
	}
	
	$(function(){
		
		vedioSleepSettingLoad.init();
		$("#regularly_1,#regularly_2,#regularly_3,#regularly_4,#regularly_5,#regularly_6,#regularly_7").on("click",vedioSleepSettingLoad.timingWakeUpCheckFn); //定时唤醒日设置点击函数调用
		$("#sendSubmit").on("click",vedioSleepSettingLoad.sendSubmitFn);//下发函数
		
	})
	
/*})(window,$)*/