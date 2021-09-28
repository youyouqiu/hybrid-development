(function(window,$){
	var formFlag = true;
	addTank = {
		shapeChange: function(){
			addTank.regulateForm_circle();
			addTank.calculateTheoryVol();
		},
		boxLengthBlur: function(){
			addTank.validateL_W_H();
			addTank.calculateTheoryVol();
		},
		widthBlur: function(){
			addTank.validateL_W_H();
			addTank.regulateForm_circle();
			addTank.calculateTheoryVol();
		},
		heightBlur: function(){
			addTank.validateL_W_H();
			addTank.regulateForm_circle();
			addTank.calculateTheoryVol();
		},
		thicknessBlur: function(){
			addTank.validateL_W_H();
			addTank.calculateTheoryVol();
		},
		buttomRadiusBlur: function(){
			addTank.calculateTheoryVol();
		},
		topRadiusBlur: function(){
			addTank.calculateTheoryVol();
		},
		// 填写壁厚后，验证长、宽、高的填写是否合理
		validateL_W_H: function(){
			var s = $("#shape").val();
			var l = $("#boxLength").val();
			var w = $("#width").val();
			var h = $("#height").val();
			var t = $("#thickness").val();
			addTank.hideErrorMsg();
			if (!isNaN(l) && !isNaN(t)) {
				if (parseFloat(l) <= 2*parseFloat(t)) {
					addTank.showErrorMsg(oilBoxLengthLegal, "boxLength");
					formFlag = false;
					return formFlag;
				} else {
					addTank.hideErrorMsg();
					formFlag = true;
				}
				if (parseFloat(w) <= 2*parseFloat(t)) {
					addTank.showErrorMsg(oilBoxWidthLegal, "width");
					formFlag = false;
					return formFlag;
				} else {
					addTank.hideErrorMsg();
					formFlag = true;
				}
				if (parseFloat(h) <= 2*parseFloat(t)) {
					addTank.showErrorMsg(oilBoxHeightLegal, "height");
					formFlag = false;
					return formFlag;
				} else {
					addTank.hideErrorMsg();
					formFlag = true;
				}
				if (s == "3") {
					if (w != "" && parseInt(w) > 0 && h != "" && parseInt(h) > 0) {
						if (parseFloat(w) < parseFloat(h) / 2) {
							addTank.showErrorMsg(oilBoxWidthError, "width");
							formFlag = false;
							return;
						} else {
							formFlag = true;
							addTank.hideErrorMsg();
						}
					}
				}
			} else {
				addTank.hideErrorMsg();
				formFlag = true;
			}
			return formFlag;
		},
		//校验上下圆导角是否填写正确
		validateR:function(){
			var b = parseFloat($("#shape").val());
			var w = parseFloat($("#width").val());
			var h = parseFloat($("#height").val());
			var t = parseFloat($("#thickness").val());
			var r1 = parseFloat($("#buttomRadius").val());
			var r2 = parseFloat($("#topRadius").val());
			addTank.hideErrorMsg();
			if (isNaN(r1)) {
				addTank.showErrorMsg(oilButtomThicknessNull, "buttomRadius");
				return false;
			}
			if (isNaN(r2)) {
				addTank.showErrorMsg(oilTopThicknessNull, "topRadius");
				return false;
			}
			if (r1 < t) {
				addTank.showErrorMsg(oilButtomThicknessError, "buttomRadius");
				return false;
			}
			if (r2 < t) {
				addTank.showErrorMsg(oilTopThicknessError, "topRadius");
				return false;
			}
			if (r2 > r1) {
				addTank.showErrorMsg(oilRediusError, "topRadius");
				return false;
			}
			if (r1 > w/2 || r1 > h/2) {
				addTank.showErrorMsg(oilButtomRadiusError, "buttomRadius");
				return false;
			}
			if (r2 > w/2 || r2 > h/2) {
				addTank.showErrorMsg(oilTopRadiusError, "topRadius");
				return false;
			}
			return true;
		},
		// 当选择油箱形状后，规范化页面表单的填写
		regulateForm_circle: function(){
			var shape = $("#shape").find("option:selected").val();
			var width = $("#width").val();
			var height = $("#height").val();
			if(shape == "1"){ //长方形油箱
				$("#cuboidForm").show();
			} else {
				$("#cuboidForm").hide();
			}
			if (shape == "2") { // 圆形油箱
				if (width != "" && parseInt(width) > 0) {
					$("#height").val(width);
				} else if (height != "" && parseInt(height) > 0) {
					$("#width").val(height);
				}
			}
		},
		//显示错误提示信息
		showErrorMsg: function(msg, inputId){
			if ($("#error_label").is(":hidden")) {
				$("#error_label").text(msg);
				$("#error_label").insertAfter($("#" + inputId));
		        $("#error_label").show();
			} else {
				$("#error_label").is(":hidden");
			} 
		},
		// 隐藏错误信息
		hideErrorMsg: function(){
			$("#error_label").hide();
		},
		//监控长、宽、高、壁厚的填写情况，若都填写了，则根据填写的值到后台计算油箱的理论容积
		calculateTheoryVol: function(){
			var l = $("#boxLength").val();
			var w = $("#width").val();
			var h = $("#height").val();
			var t = $("#thickness").val();
			var b = $("#shape").find("option:selected").val();
			var r1 = "";
			var r2 = "";
			if (b == "1") {
				r1 = $("#buttomRadius").val();
				r2 = $("#topRadius").val();
			}
			if (l != null && l != '' && w != null && w != '' && h != null && h != '' && t != null && t != '' 
					&& (b != "1" || (r1 != null && r1 != '' && r2 != null && r2 != '' && addTank.validateR()))) {
				$.ajax({
					type : 'POST',
					url : '/clbs/v/oilmassmgt/fueltankmgt/getTheoryVol',
					async : false,
					data : {"boxLength" : l,
							"width" : w, 
							"height" : h, 
							"thickness" : t, 
							"shape" : b, 
							"buttomRadius" : r1, 
							"topRadius" : r2},
					dataType : 'json',
					success : function(data) {
						if (data != null && data.obj != null) {
							if (parseFloat(data.obj.theoryVol) < 0) {
								$("#theoryVolume").val("0");
							} else {
								$("#theoryVolume").val(data.obj.theoryVol);
							}
						}
					},
					error : function() {
						layer.msg(systemError, {move: false});	
					}
				});
			}
		},
		validates: function(){
		    return $("#addForm").validate({
		        rules: {
		        	type: {
		                required: true,
                        isRightSensorModel : true,
		                maxlength : 25,
		                remote : {
		                	type:"post",
							async:false,
							url:"/clbs/v/oilmassmgt/fueltankmgt/repetition" ,
		                    data:{  
		                    	type : function(){return $("#type").val();}  
		                     }
		                }
		            },
		            shape : {
		            	required: true
		            },
		            boxLength: {
		                required: true,
		                isNumber : true,
		                maxlength : 5
		            },
		            width: {
		                required: true,
		                isNumber : true,
		                maxlength : 5
		            },
		            height: {
		                required: true,
		                isNumber : true,
		                maxlength : 5
		            }, 
		            thickness : {
		            	required : true ,
		            	maxlength : 10,
		            	isNumber : true,
		            	isInt1to10 : true
		            }, 
		            theoryVolume : {
		            	required : false        	
		            }, 
		            realVolume : {
		            	required : false,
		            	isNumber : true,
		            	maxlength : 6
		            }
		        },
		        messages: {
		        	type: {
		                required : oilTypeNull,
		                // isRightfulString_oilBoxType:oilTypeError,
		                maxlength : oilTypeMaxLength,
		                remote : oilTypeExists
		            },
		            shape : {
		            	required: oilShapeNull
		            },
		            boxLength: {
		            	required : oilBoxLengthNull,
		            	isNumber : numberDoubleError,
		                maxlength : oilBoxLengthMaxLength
		            },
		            width: {
		            	required: oilBoxWidthNull,
		            	isNumber : numberDoubleError,
		                maxlength : oilBoxWidthMaxLength
		            },
		            height: {
		            	required: oilBoxHeightNull,
		            	isNumber : numberDoubleError,
		                maxlength : oilBoxHeightMaxLength
		            }, 
		            thickness : {
		            	required: oilThickNessNull,     
		            	maxlength : oilBoxThickNessMaxLength,
		            	isNumber : numberDoubleError,
		            	isInt1to10 : oilBoxThickNessRange
		            }, 
		            theoryVolume : {
		            	required: oilBoxTheoryVolumeNull
		            }, 
		            realVolume : {
		            	required: oilBoxRealVolumeNull, 
		            	isNumber : numberDoubleError,
		            	maxlength : oilBoxRealVolumeMaxLength
		            }		
		        }
		    }).form();
		},
		submitForm: function(){
		    if(addTank.validates() && addTank.validateL_W_H() && formFlag){
		    	var shape = $("#shape").find("option:selected").val();
		    	if (shape == "1") {
		    		if (!addTank.validateR()) { //校验上下圆导角输入值正确性
		    			return;
		    		}
		    	} else {
		    		$("#buttomRadius").val("");
					$("#topRadius").val("");
		    	}
		    	$("#boxSubmitBtn").attr("disabled", true); //避免重复提交的问题
				addHashCode($("#addForm"));
		    	$("#addForm").ajaxSubmit(function(data) {
		    		var data = $.parseJSON(data)
		    		if(data.success){
		    			$("#boxSubmitBtn").attr("disabled", false);
			      		$("#commonWin").modal("hide");
			      		layer.msg(data.msg,{move:false});
			        	myTable.requestData();
		    		}else{
		    			layer.msg(data.msg,{move:false});
		    		}
		   		});
		  	}
		},
	}
	$(function(){
		$('input').inputClear();
		$("#shape").change(addTank.shapeChange);
		$("#boxLength").blur(addTank.boxLengthBlur);
		$("#width").blur(addTank.widthBlur);
		$("#height").blur(addTank.heightBlur);
		$("#thickness").blur(addTank.thicknessBlur);
		$("#buttomRadius").blur(addTank.buttomRadiusBlur);
		$("#topRadius").blur(addTank.topRadiusBlur);
		$("#boxSubmitBtn").bind("click",addTank.submitForm);
	})
})(window,$)