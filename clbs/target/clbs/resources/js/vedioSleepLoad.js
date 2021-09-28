//@ sourceURL=videoSleepLoad.js
/**
 * 右键菜单  休眠唤醒
 */
/*(function(window,$){*/
	
	var _thisVehicleId;//车辆ID
	
	vedioSleepLoad = {
			
		init: function(){
			
        	//菜单隐藏
            $('#rMenu').css({"visibility": "hidden"});
            
			//模态框添加类样式
			$(".modal-body").addClass("modal-body-overflow");
			$(".modal-dialog").css("width","40%");
            
            //透传消息类型下拉框改变函数
            vedioSleepLoad.transparentMsgTypeChangeFn();
            
            //获取当前右键车辆ID信息
            _thisVehicleId = $("#obtainVid").val();

			
		},
		
		//透传消息类型下拉框改变函数
		transparentMsgTypeChangeFn: function(){
			if($("#transparentMsgType").val() == 5){
				$("#transparentMsgTypeContent").removeClass("hidden");
			}else{
				$("#transparentMsgTypeContent").addClass("hidden");
			}
		},

		//显示错误信息
		showErrorMsg: function(msg, inputId){
            var error = $("#error_label_add").length;
            if(error == 0){
                $("#videoSleepFooter").append("<label id='error_label_add' class='error'></label>");
            }
		    $("#error_label_add").removeClass("hidden");
		    $("#error_label_add").text(msg);
		    $("#error_label_add").insertAfter($("#" + inputId));

            // if ($("#error_label_add").hasClass("hidden")) {
            //     $("#error_label_add").text(msg);
            //     $("#error_label_add").insertAfter($("#" + inputId));
            //     $("#error_label_add").removeClass("hidden");
            // } else {
            //     $("#error_label_add").addClass("hidden");
            // }
        },
        
        //隐藏错误提示信息
        hideErrorMsg: function(){
            $("#error_label_add").addClass("hidden");
        },
        
		//下发
      sendVideoSleepSubmitFun: function(){
			//获取value值
			var msgId = $("#massageId").val();
			var wakeUpTime = $("#wakeUpTime").val();
			//验证消息ID
			if(msgId == null || msgId == ""){
				vedioSleepLoad.showErrorMsg("消息ID不能为空","massageId");
				return false;
			}else{
				if(msgId < 0 || msgId > 255){
					vedioSleepLoad.showErrorMsg("消息ID范围：0 ~ 255","massageId");
					return false;
				}else{
					vedioSleepLoad.hideErrorMsg();
				}
			}
			//验证唤醒时长
			if(wakeUpTime == null || wakeUpTime == ""){
				vedioSleepLoad.showErrorMsg("唤醒时长不能为空","wakeUpTime");
				return false;
			}else{
				if(wakeUpTime < 0 || wakeUpTime > 65536){
					vedioSleepLoad.showErrorMsg("唤醒时长范围：0 ~ 65536","wakeUpTime");
					return false;
				}else{
					vedioSleepLoad.hideErrorMsg();
				}
			}
			//下发
			var url = "/clbs/realTimeVideo/video/sendParamCommand";
			var parameter = {awakenTime: wakeUpTime,msgId: msgId,orderType: 10,vehicleId: _thisVehicleId};
			json_ajax("POST", url, "json", true, parameter, function(data){
				if(data.success == true){
					layer.msg("下发成功");
					$("#commonSmWin").modal("hide");
					//日志记录
	    			realTimeVideLoad.logFindCilck();
				}
			});
		},
		
	}
	
	$(function(){
		
		vedioSleepLoad.init();
		$("input").inputClear();
		$("#sendVideoSleepSubmit").on("click",vedioSleepLoad.sendVideoSleepSubmitFun);//下发
		
	})
	
/*})(window,$)*/