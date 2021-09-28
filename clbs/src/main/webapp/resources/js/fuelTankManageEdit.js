//# sourceURL=fluxTankManageEdit.js
(function(window,$){
	var formFlag = true;
	editTank = {
		init: function(){
			var s = $("#shapeValue").val();
			//初始化油箱形状
			$("#shape").val(s);
			//初始化理论容积，根据填写的长、宽、高、壁厚进行计算
			$("#shape").change(function (){
				editTank.regulateForm_circle();
				editTank.calculateTheoryVol();
			});
			$("#boxLength").blur(function (){
				editTank.validateL_W_H();
				editTank.calculateTheoryVol();
			});
			$("#width").blur(function (){
				editTank.validateL_W_H();
				editTank.regulateForm_circle();
				editTank.calculateTheoryVol();
			});
			$("#height").blur(function (){
				editTank.validateL_W_H();
				editTank.regulateForm_circle();
				editTank.calculateTheoryVol();
			});
			$("#thickness").blur(function (){
				editTank.validateL_W_H();
				editTank.calculateTheoryVol();
			});
			$("#buttomRadius").blur(function(){
				editTank.calculateTheoryVol();
			});
			$("#topRadius").blur(function(){
				editTank.calculateTheoryVol();
			});
			editTank.fuelTankValueShow(s);
		},
		//填写壁厚后，验证长、宽、高的填写是否合理
		validateL_W_H: function(){
			var s = $("#shape").val();
			var l = $("#boxLength").val();
			var w = $("#width").val();
			var h = $("#height").val();
			var t = $("#thickness").val();
			editTank.hideErrorMsg();
			if (!isNaN(l) && !isNaN(t)) {
				if (parseFloat(l) <= 2*parseFloat(t)) {
					editTank.showErrorMsg(oilBoxLengthLegal, "boxLength");
					formFlag = false;
					return formFlag;
				} else {
					editTank.hideErrorMsg();
					formFlag = true;
				}
				if (parseFloat(w) <= 2*parseFloat(t)) {
					editTank.showErrorMsg(oilBoxWidthLegal, "width");
					formFlag = false;
					return formFlag;
				} else {
					editTank.hideErrorMsg();
					formFlag = true;
				}
				if (parseFloat(h) <= 2*parseFloat(t)) {
					editTank.showErrorMsg(oilBoxHeightLegal, "height");
					formFlag = false;
					return formFlag;
				} else {
					editTank.hideErrorMsg();
					formFlag = true;
				}
				if (s == "3") {
					if (w != "" && parseInt(w) > 0 && h != "" && parseInt(h) > 0) {
						if (parseFloat(w) < parseFloat(h) / 2) {
							editTank.showErrorMsg(oilBoxWidthError, "width");
							formFlag = false;
							return;
						} else {
							formFlag = true;
							editTank.hideErrorMsg();
						}
					}
				}
			} else {
				editTank.hideErrorMsg();
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
			editTank.hideErrorMsg();
			if (isNaN(r1)) {
				editTank.showErrorMsg(oilButtomThicknessNull, "buttomRadius");
				return false;
			}
			if (isNaN(r2)) {
				editTank.showErrorMsg(oilTopThicknessNull, "topRadius");
				return false;
			}
			if (r1 < t) {
				editTank.showErrorMsg(oilButtomThicknessError, "buttomRadius");
				return false;
			}
			if (r2 < t) {
				editTank.showErrorMsg(oilTopThicknessError, "topRadius");
				return false;
			}
			if (r2 > r1) {
				editTank.showErrorMsg(oilRediusError, "topRadius");
				return false;
			}
			if (r1 > w/2 || r1 > h/2) {
				editTank.showErrorMsg(oilButtomRadiusError, "buttomRadius");
				return false;
			}
			if (r2 > w/2 || r2 > h/2) {
				editTank.showErrorMsg(oilTopRadiusError, "topRadius");
				return false;
			}
			return true;
		},
		//当选择油箱形状后，规范化页面表单的填写
		regulateForm_circle: function(){
			var shape = $("#shape").find("option:selected").val();
			var width = $("#width").val();
			var height = $("#height").val();
			editTank.fuelTankValueShow(shape);
			if (shape == "2") { // 圆形油箱
				if (width != "" && parseInt(width) > 0) {
					$("#height").val(width);
				} else if (height != "" && parseInt(height) > 0) {
					$("#width").val(height);
				}
			}
		},
		//初始化油箱字段显示隐藏
		fuelTankValueShow:function(shape){
			if(shape == "1"){ //长方形油箱
				$("#cuboidForm").show();
			} else {
				$("#cuboidForm").hide();
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
		//隐藏错误信息
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
				var r1 = $("#buttomRadius").val();
				var r2 = $("#topRadius").val();
			}
			if (l != null && l != '' && w != null && w != '' && h != null && h != '' && t != null && t != ''
				&& (b != "1" || (r1 != null && r1 != '' && r2 != null && r2 != '' && editTank.validateR()))) {
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
		shapeChange: function(){
			$("#shapeValue").val($("#shape").find("option:selected").val());
		},
		submitForm: function(){
		    if(editTank.validates() && editTank.validateL_W_H() && formFlag){
		    	var shape = $("#shape").find("option:selected").val();
		    	if (shape == "1") {
		    		if (!editTank.validateR()) { //校验上下圆导角输入值正确性
		    			return;
		    		}
		    	} else {
		    		$("#buttomRadius").val("");
					$("#topRadius").val("");
		    	}
		    	$("#boxSubmitBtn").attr("disabled", true); // 避免重复提交的问题
				addHashCode($("#editForm"));
		    	$("#editForm").ajaxSubmit(function(data) {
		    		var data = $.parseJSON(data)
		    		if(data.success){
		    			$("#boxSubmitBtn").attr("disabled", false);
			      		$("#commonWin").modal("hide");
			      		layer.msg(data.msg,{move:false});
			        	myTable.refresh();
		    		}else{
		    			if(data.msg == null){
		    				layer.msg(data.msg,{move:false});
		    			}else if(data.msg != null){
		    				layer.msg(data.msg,{move:false});
		    			}
		    		}
		   		});
		  	}
		},
		validates: function(){
		    return $("#editForm").validate({
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
		                    	type : function(){return $("#type").val();},
                                oilBoxId : function(){return $("#oilBoxId").val();}
		                    }
		                    /*,
		                    dataFilter: function(data, type) { 
		                		var oldV = $("#oldType").val();
		                		var newV = $("#type").val();
		                		var data2 = data;
		                		if (oldV == newV) {
		                			return true;
		                		} else {
		                			if (data2 == "true"){
		                       			return true; 
			                        } else {
			                       		return false;
			                        } 
		                		}
		                 	}*/
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
		                // isRightfulStr : oilTypeError,
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
		            	isNumber : numberDoubleError,
		            	maxlength : oilBoxThickNessMaxLength
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
		// 关闭按钮点击事件
		cancleBtnClick : function () {
			var oilBoxId = $("#id").val();
			var editBtn = $("#edit_" + oilBoxId);
			editBtn.attr("href", "");
			editBtn.attr("data-target", "");
			editBtn.attr("data-toggle", "");
			editBtn.attr("onclick", "tankManagement.editAndCheckBound('" + oilBoxId + "')");
		},
	}
	$(function(){
		$('input').inputClear();
		editTank.init();
		$("#shape").change(editTank.shapeChange);
		$("#boxSubmitBtn").bind("click",editTank.submitForm);
		$("#cancleBtn").bind("click", editTank.cancleBtnClick);
	})
})(window,$)