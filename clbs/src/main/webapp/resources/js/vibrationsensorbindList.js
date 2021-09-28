(function(window,$){
	var params = [];
	//显示隐藏列
	var menu_text = "";
	var table = $("#dataTable tr th:gt(1)");
 	var selectGroupId = '';
 	var selectAssignmentId = '';
	//单选
	var subChk = $("input[name='subChk']");

    var dtext = "";
    var dn = new Array("交通部JT/T808-2013","交通部JT/T808-2019");
    var dv = new Array("1","11");
    var protocol=1;
    var vibsendfalg=true;
	hourlyVehicleSetting = {
		init: function(){
			// webSocket.init('/clbs/vehicle');
		    // 接收到消息
            // setTimeout(function () {
             //    webSocket.subscribe(headers,'/topic/fencestatus', hourlyVehicleSetting.updataFenceData,null, null);
            // },500);
		   	//监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
		   	window.onbeforeunload = function ()
		   	{
		   		 var cancelStrS = {
		   	            "desc": {
		   	                "MsgId": 40964,
		   	                "UserName": $("#userName").text()
		   	            },
		   	            "data": params
		   	        };
		   		  webSocket.unsubscribealarm(headers,"/app/vehicle/unsubscribestatus", cancelStrS);
		   	}
		  	//显示隐藏列
		   	menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) +"\" disabled />"+ table[0].innerHTML +"</label></li>"
		   	for(var i = 1; i < table.length; i++){
		   	    menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i+2) +"\" />"+ table[i].innerHTML +"</label></li>"
		   	};
		   	$("#Ul-menu-text").html(menu_text);

            //生成定制显示列
            for(var i = 0; i < dn.length; i++){
                dtext +="<label class='radio-inline' style='margin-left:10px;'><input name=\"deviceCheck\" value=\"" + dv[i] +"\" type=\"radio\" class=\"device\" />"+ dn[i] +"</label>";
            };
            $("#Ul-menu-text-v").html(dtext);
            $("input[value='1']").prop("checked",true);
		    var treeSetting = {
	    		async : {
	    			url : "/clbs/m/basicinfo/enterprise/assignment/assignmentTree",
	    			tyoe : "post",
	    			enable : true,
	    			autoParam : [ "id" ],
	    			dataType : "json",
	    			otherParam : {  // 是否可选  Organization
	    				"isOrg" : "1"
	    			},
	    			dataFilter: hourlyVehicleSetting.ajaxDataFilter
	    		},
	    		view : {
	    			selectedMulti : false,
	    			nameIsHTML: true,
	    			fontCss: setFontCss_ztree
	    		},
	    		data : {
	    			simpleData : {
	    				enable : true
	    			}
	    		},
	    		callback : {
	    			onClick : hourlyVehicleSetting.zTreeOnClick,
	    		}
	    	};
			$.fn.zTree.init($("#treeDemo"), treeSetting, null);
			//IE9
		    if(navigator.appName=="Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g,"")=="MSIE9.0") {
		        var search;
		        $("#search_condition").bind("focus",function(){
		            search = setInterval(function(){
		                search_ztree('treeDemo', 'search_condition','assignment');
		            },500);
		        }).bind("blur",function(){
		            clearInterval(search);
		        });
		    }
		    //IE9 end
		  	//表格列定义
		  	var columnDefs = [ {
		  		//第一列，用来显示序号
	  			"searchable" : false,
		  		"orderable" : false,
		  		"targets" : 0
		  	} ];
		  	var columns = [
		  		{
		  			//第一列，用来显示序号
		  			"data" : null,
		  			"class" : "text-center"
		  		},
		  		{
		  			"data" : null,
		  			"class" : "text-center",
		  			render : function(data, type, row, meta) {
		  				var result = '';
		  				if (row.id != null && row.id != "") { //
		  					var obj = {};
		                      obj.fluxVehicleId = row.id;
		                      if (row.paramId != null && row.paramId != "") {
		                      	obj.paramId = row.paramId;
		  					}
		                      if (row.transmissionParamId != null && row.transmissionParamId != "") {
		                      	obj.transmissionParamId = row.transmissionParamId;
		  					}
		                      obj.vehicleId = row.vehicleId;
		                      var jsonStr = JSON.stringify(obj)
		                      result += "<input  type='checkbox' name='subChk'  value='" + jsonStr + "' />";
		  				}
		  				return result;
		  			}
		  		},
		  		{
		  			"data" : null,
		  			"class" : "text-center", //最后一列，操作按钮
		  			render : function(data, type, row, meta) {
		  				var editUrlPath = myTable.editUrl + row.id + ".gsp"; //修改地址
		  				var bindUrlPre = '/clbs/v/workhourmgt/vbbind/bind_{id}.gsp';
		  				var detailUrlPre ='/clbs/v/workhourmgt/vbbind/detail_{id}.gsp';
		  				var result = '';
		  				var paramId = "";
		  				var transmissionParamId = "";
		  				if (row.paramId != null && row.paramId != "") {
		  					paramId = row.paramId;
		  				}
		  				if (row.transmissionParamId != null && row.transmissionParamId != "") {
		  					transmissionParamId = row.transmissionParamId;
		  				}
		  				if (row.id != null && row.id != "") { // 修改
		  					result += '<button href="'
		  						+ editUrlPath
		  						+ '" data-toggle="modal" data-target="#commonWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
		  					// 删除
		  					//删除按钮
		  					result += '<button type="button" onclick="myTable.deleteItem(\''
		  							+ row.id
		  							+ '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>解除绑定</button>&ensp;';
		  					// 下发参数
		  					result += '<button onclick="hourlyVehicleSetting.sendworkhourOne(\''+row.id+'\',\''+paramId+'\',\''+transmissionParamId+'\',\''+row.vehicleId+'\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';
		  				}else { // 设置
		  					result += '<button href="'+ bindUrlPre.replace("{id}", row.vId) +'"  data-toggle="modal" data-target="#commonWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>设置</button>&ensp;';
		  					// 禁用删除按钮
		  					result += '<button disabled type="button" onclick="myTable.deleteItem(\''
		  						+ row.id
		  						+ '\')" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>解除绑定</button>&ensp;';
		  					// 禁用下发参数
		  					result += '<button disabled type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
		  				}
		  				// 详细信息
		  				result += '<button href="'+ detailUrlPre.replace("{id}", row.vId) +'" data-toggle="modal" data-target="#commonWin" type="button" class="editBtn editBtn-info"><i class="fa fa-sun-o"></i>详情</button>&ensp;';
		  				return result;
		  			}
		  		},
		  		{
		  			"data" : "status",
		  			"class" : "text-center",
		  			render : function(data, type, row, meta) {
                        if (data == "0") {
                            return '参数已生效';
                        } else if (data == "1") {
                            return '参数未生效';
                        } else if (data == "2") {
                            return "参数消息有误";
                        } else if (data == "3") {
                            return "参数不支持";
                        } else if (data == "4") {
                            return "参数下发中";
                        } else if (data == "5") {
                            return "终端离线，未下发";
                        } else if (data == "7") {
                            return "终端处理中";
                        } else if (data == "8") {
                            return "终端接收失败";
                        }  else {
                            return "";
                        }
		  			}
		  		},
		  		{
		  			"data" : "brand",
		  			"class" : "text-center",
		  		},
		  		{
		  			"data" : "groups",
		  			"class" : "text-center"
		  		},
		  		{
		  			"data" : "vehicleType",
		  			"class" : "text-center"
		  		},
		  		{
		  			"data" : "sensorType",
		  			"class" : "text-center"
		  		},
		  		{
		  			"data" : "manufacturers",
		  			"class" : "text-center"
		  		},
		  		{
		  			"data" : "baudRate",
		  			"class" : "text-center",
		  			render : function(data, type, row, meta) {
		  				if (data == "1") {
		                         return '2400';
		                     } else if (data == "2") {
		                         return '4800';
		                     }  else if (data == "3") {
		                         return '9600';
		                     }  else if (data == "4") {
		                         return '19200';
		                     }  else if (data == "5") {
		                         return '38400';
		                     } else if (data == "6") {
		                         return '57600';
		                     } else if (data == "7") {
		                         return '115200';
		                     } else {
		                     	return "";
		                     }
		  			}
		  		},
		  		{
		  			"data" : "parity",
		  			"class" : "text-center",
		  			render : function(data, type, row, meta) {
		  				 if (data == "3") {
		                          return '无校验';
		                      } else if (data == "1") {
		                          return '奇校验';
		                      } else if (data == "2"){
		                      	return '偶校验';
		                      }else {
		                      	return "";
		                      }
		  			}
		  		},
		  		{
		  			"data" : "inertiaCompEn",
		  			"class" : "text-center",
		  			render : function(data, type, row, meta) {
		  				 if (data == "2") {
		                          return '禁用';
		                      } else if (data == "1") {
		                          return '使能';
		                      } else {
		                      	return '';
		                      }
		  			}
		  		},
		  		{
		  			"data" : "filterFactor",
		  			"class" : "text-center",
		  			render : function(data, type, row, meta) {
		  				 if (data == "1") {
		                          return '实时';
		                      } else if (data == "2") {
		                          return '平滑';
		                      } else if (data == "3") {
		                          return '平稳';
		                      }  else {
		                      	return '';
		                      }
		  			}
		  		},
		  		{
		  			"data" : "collectNumber",
		  			"class" : "text-center"
		  		},
		  		{
		  			"data" : "uploadNumber",
		  			"class" : "text-center"
		  		},
		  		{
		  			"data" : "uploadTime",
		  			"class" : "text-center",
		  			render : function (data) {
		  				if (data == "01") {
		  					return "被动";
		  				} else if (data == "02") {
		  					return "10";
		  				} else if (data == "03") {
		  					return "20";
		  				} else if (data == "04") {
		  					return "30";
		  				} else {
		  					return "";
		  				}
		  			}
		  		},
		  		{
		  			"data" : "outputCorrectionB",
		  			"class" : "text-center"
		  		},
		  		{
		  			"data" : "outputCorrectionK",
		  			"class" : "text-center"
		  		},
		  		{
		  			"data" : "outageFrequencyThreshold",
		  			"class" : "text-center"
		  		},
		  		{
		  			"data" : "idleFrequencyThreshold",
		  			"class" : "text-center"
		  		},
		  		{
		  			"data" : "continueOutageTimeThreshold",
		  			"class" : "text-center"
		  		},
		  		{
		  			"data" : "continueIdleTimeThreshold",
		  			"class" : "text-center"
		  		},
		  		{
		  			"data" : "alarmFrequencyThreshold",
		  			"class" : "text-center"
		  		},
		  		{
		  			"data" : "workFrequencyThreshold",
		  			"class" : "text-center"
		  		},
		  		{
		  			"data" : "continueAlarmTimeThreshold",
		  			"class" : "text-center"
		  		},
		  		{
		  			"data" : "continueWorkTimeThreshold",
		  			"class" : "text-center"
		  	}];
			//ajax参数
			var ajaxDataParamFun = function(d) {
				d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
				d.groupId = selectGroupId;
				d.assignmentId = selectAssignmentId;
                d.protocol = $("input[name='deviceCheck']:checked").val();
			};
			//表格setting
			var setting = {
				listUrl : '/clbs/v/workhourmgt/vbbind/list',
				editUrl : '/clbs/v/workhourmgt/vbbind/edit_',
				deleteUrl : '/clbs/v/workhourmgt/vbbind/delete_',
				deletemoreUrl : '/clbs/v/workhourmgt/vbbind/deletemore',
				enableUrl : '/clbs/c/user/enable_',
				disableUrl : '/clbs/c/user/disable_',
				columnDefs : columnDefs, //表格列定义
				columns : columns, //表格列
				dataTableDiv : 'dataTable', //表格
				ajaxDataParamFun : ajaxDataParamFun, //ajax参数
				pageable : true, //是否分页
				showIndexColumn : true, //是否显示第一列的索引列
				enabledChange : true
			};
			//创建表格
			myTable = new TG_Tabel.createNew(setting);
			//表格初始化
			myTable.init();
		},
		updataFenceData: function(msg){
			  if (msg != null) {
				  var result = $.parseJSON(msg.body);
					 if (result != null) {
						 myTable.refresh();
					 }
			  }
		},
		//组织树预处理函数
		ajaxDataFilter: function(treeId, parentNode, responseData){
		    var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
		    if (responseData) {
		        for (var i = 0; i < responseData.length; i++) {
		                responseData[i].open = true;
		        }
		    }
		    return responseData;
		},
		//点击节点
		zTreeOnClick: function(event, treeId, treeNode){
			if (treeNode.type == "assignment") {
				selectAssignmentId = treeNode.id;
				selectGroupId = '';
			}else{
				selectGroupId = treeNode.id;
				selectAssignmentId = '';
			}
			myTable.requestData();
		},
		//全选
		checkAllClick: function(){
			$("input[name='subChk']").prop("checked", this.checked);
		},
		subChkClick: function(){
			$("#checkAll").prop(
					"checked",
					subChk.length == subChk.filter(":checked").length ? true
							: false);
		},
		// 下发参数 （单个）
	    sendworkhourOne: function(id,paramId,transmissionParamId,vehicleId){
	    	 var arr = [];
	    	 var obj = {};
	         obj.fluxVehicleId = id;
	         obj.paramId = paramId;
	         obj.transmissionParamId = transmissionParamId;
	         obj.vehicleId = vehicleId;
	         arr.push(obj);
	         var jsonStr = JSON.stringify(arr);
	         hourlyVehicleSetting.sendWorkHour(jsonStr);
	    },
	    // 下发参数
		sendWorkHour: function(sendParam){
			 layer.load(2);
			 $.ajax({
	             type: "POST",
	             url: "/clbs/v/workhourmgt/vbbind/sendWorkHour",
	             data: {
	                 "sendParam": sendParam
	             },
	             dataType: "json",
	             global : true,
	             success: function (data) {
	             	if(vibsendfalg){
                        webSocket.subscribe(headers,'/topic/fencestatus', hourlyVehicleSetting.updataFenceData,null, null);
                        vibsendfalg=false;
                    }
                     layer.closeAll('loading');
	            	 layer.msg(sendCommandComplete,{closeBtn: 0}, function(refresh){
	            		 myTable.refresh(); //执行的刷新语句
	            		 layer.close(refresh);
	            		});
	             }
			 })
		},
		// 批量下发
		sendModelClick: function(){
	        //判断是否至少选择一项
	        var chechedNum = $("input[name='subChk']:checked").length;
	        if (chechedNum == 0) {
	            layer.msg(selectItem);
	            return
	        }
	        var checkedList = new Array();
	        $("input[name='subChk']:checked").each(function() {
	        	var jsonStr = $(this).val();
	        	var jsonObj = $.parseJSON(jsonStr);
	            checkedList.push(jsonObj);
	        });
	        // 下发
	        hourlyVehicleSetting.sendWorkHour(JSON.stringify(checkedList));
		},
		//批量删除
		delModelClick: function(){
			//判断是否至少选择一项
			var chechedNum = $("input[name='subChk']:checked").length;
			if (chechedNum == 0) {
				layer.msg(selectItem);
				return
			}
			var checkedList = new Array();
			$("input[name='subChk']:checked").each(function() {
				var jsonStr = $(this).val();
	        	var jsonObj = $.parseJSON(jsonStr);
	            checkedList.push(jsonObj.fluxVehicleId);
			});
			myTable.deleteItems({
				'deltems' : checkedList.toString()
			});
		},
		// 查询全部
		queryAllClick: function(){
			selectGroupId = "";
			selectAssignmentId = "";
			$('#simpleQueryParam').val("");
			var zTree = $.fn.zTree.getZTreeObj("treeDemo");
			zTree.selectNode("");
            zTree.cancelSelectedNode();
			myTable.requestData();
		},
		// 组织架构模糊搜索
		searchCondition: function(){
	    	search_ztree('treeDemo', 'search_condition', 'assignment');
		},
	}
	$(function(){
		$('input').inputClear().on('onClearEvent',function(e,data){
			var id = data.id;
			if(id == 'search_condition'){
				search_ztree('treeDemo', id, 'assignment');
			};
		});
		hourlyVehicleSetting.init();
		//全选
		$("#checkAll").bind("click",hourlyVehicleSetting.checkAllClick);
		subChk.bind("click",hourlyVehicleSetting.subChkClick);
		// 批量下发
	    $("#send_model").bind("click",hourlyVehicleSetting.sendModelClick);
		//批量删除
		$("#del_model").bind("click",hourlyVehicleSetting.delModelClick);
		// 查询全部
		$('#refreshTable').bind("click",hourlyVehicleSetting.queryAllClick);
		// 组织架构模糊搜索
		$("#search_condition").bind("input oninput",hourlyVehicleSetting.searchCondition);

        //改变协议勾选框
        $(".device").change(function() {
            //取消全选
            $("#checkAll").prop('checked',false);
            //刷新表格
            myTable.requestData();
        });
	})
})(window,$)