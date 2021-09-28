// (function(window,$){
	//显示隐藏列
	var menu_text = "";
	var table = $("#dataTable tr th:gt(1)");
	var subChk = $("input[name='subChk']");
	var clickTreeNodeName;
	var addEventIndex = 2,addPhoneIndex = 2,addInfoDemandIndex = 2,addBaseStationIndex = 2;
	var vehicleList = "";
	var vehicleIdList = "";
	var commandNodes = "";
	var treeClickFlag;
	var currentCommandType;
	var currentVehicle;
	var loadTime;
	var requestRootURL = "/clbs/m/functionconfig/fence/bindfence";
	var zTreeIdJson = {};
	var setChar;
	var realsendflag = true;
	realTimeCommand = {
		init: function(){
            //显示隐藏列
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) +"\" disabled />"+ table[0].innerHTML +"</label></li>"
            for(var i = 1; i < table.length; i++){
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i+2) +"\" />"+ table[i].innerHTML +"</label></li>"
            };
            $("#Ul-menu-text").html(menu_text);
            realTimeCommand.initTreeData("1");
            //指令类型树
            var setting = {
    			check: {
    				enable: true,
    				chkboxType: { "Y": "ps", "N": "s" }
    			},
    			view: {
    				showIcon: false,
    				//showLine: false
    			},
    			data: {
    				simpleData: {
    					enable: true
    				},
    			},
                callback: {
                    onClick: realTimeCommand.onClickCommand,
                    onCheck: realTimeCommand.onCheckCommand
                }
    		};
            realTimeCommand.baseStationReportModeCheckFn("1");
    		var zNodes = [
    			{ id:1, pId:0, name:"交通部JTB808",open:true},//,nocheck:true
    			{ id:11, pId:1, name:"通讯参数"},
    			{ id:12, pId:1, name:"终端参数"},

    			{ id:13, pId:1, name:"终端控制",open:true},
    			{ id:131, pId:13, name:"无线升级"},
    			{ id:132, pId:13, name:"控制终端连接指定服务器"},

    			{ id:14, pId:1, name:"位置汇报参数"},
    			{ id:15, pId:1, name:"终端属性查询"},
    			{ id:16, pId:1, name:"电话参数"},
    			{ id:17, pId:1, name:"视频拍照参数"},
    			{ id:18, pId:1, name:"GNSS参数"},
    			{ id:19, pId:1, name:"事件设置"},
    			{ id:20, pId:1, name:"电话本设置"},
    			{ id:21, pId:1, name:"信息点播菜单"},
    			{ id:22, pId:1, name:"基站参数设置"},
    		];
			$.fn.zTree.init($("#commandTreeDemo"), setting, zNodes);
            $("[data-toggle='tooltip']").tooltip();
            realTimeCommand.baseStationReportModeCheckFn();
		},
		updataFenceData: function(msg){
			if (msg != null) {
				var result = $.parseJSON(msg.body);
				if (result != null) {
					myTable.refresh();
				}
			}
		},
		//当前时间(时分秒)
		getHoursMinuteSeconds: function(){
            var nowDate = new Date();
            loadTime = 
            + (nowDate.getHours() < 10 ? "0" + nowDate.getHours() : nowDate.getHours())
            + ":" 
            + (nowDate.getMinutes() < 10 ? "0" + nowDate.getMinutes() : nowDate.getMinutes())
            + ":" 
            + (nowDate.getSeconds() < 10 ? "0" + nowDate.getSeconds() : nowDate.getSeconds());
            $("#baseStationStartTimePoint,#baseStationFixedTime").val(loadTime);
		},
		initTreeData:function (deviceType) {
            //组织树
            setChar = {
                async: {
                    url: realTimeCommand.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
//                    otherParam: {"type": "multiple", "deviceType": deviceType},
                    otherParam: {"type": "multiple","icoType": "0","deviceType": deviceType},
                    dataFilter: realTimeCommand.ajaxDataFilter
                },
                check: {
                    enable: true,
                    chkStyle: "checkbox",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    },
                    radioType: "all"
                },
                view: {
                    dblClickExpand: false,
                    nameIsHTML: true,
                    fontCss: setFontCss_ztree,
                    countClass: "group-number-statistics"
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                	beforeClick:realTimeCommand.beforeClick,
                    onClick: realTimeCommand.onClickVehicle,
                    onAsyncSuccess: realTimeCommand.zTreeOnAsyncSuccess,
                    beforeCheck: realTimeCommand.zTreeBeforeCheck,
                    onCheck: realTimeCommand.onCheckVehicle,
                    onExpand: realTimeCommand.zTreeOnExpand,
                    onNodeCreated: realTimeCommand.zTreeOnNodeCreated,
                }
            };
//            $.ajax({
//                async: false,
//                type: "post",
//                url: requestRootURL + "/count",
//                data: {"type": "multiple", "deviceType": deviceType},
//                success: function (msg) {
//                    var count = parseInt(msg);
//                    if (count > 0 && count <= 5000) {
//                        setChar.async.url = requestRootURL + "/vehicleTreeByDeviceType";
//                    }
//                }
//            });
            $.fn.zTree.init($("#treeDemo"), setChar, null);
        },
        getTreeUrl : function (treeId, treeNode){
        	if (treeNode == null) {
        		return "/clbs/m/personalized/ico/vehicleTree";
        	}else if(treeNode.type == "assignment") {
        		return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId="+treeNode.id+"&isChecked="+treeNode.checked+"&monitorType=vehicle";
        	} 
        },
		initReferVehicleList: function(data){
			if(currentCommandType !== null && currentCommandType !== ""){
				//车辆list
				var referVehicleList = data;
				// 初始化车辆数据
		        var dataList = {value: []};
		        if (referVehicleList !== null && referVehicleList.length > 0) {
		        	for (var i=0; i< referVehicleList.length; i++) {
		            	var obj = {};
		                obj.id = referVehicleList[i].vid;
		                obj.name = referVehicleList[i].brand;
		                dataList.value.push(obj);
		            }
		            //取消全选勾
		            $("#checkAll").prop('checked',false);
		            $("input[name=subChk]").prop("checked",false);
		    	}
		        $("#reportPlate,#terminalPlate,#UpgradePlate,#specifyServerPlate,#locationPlate,#telephonePlate," +
					"#videoCameraPlate,#gnssPlate,#eventPlate,#phoneBookPlate,#infoDemandPlate," +
					"#baseStationPlate").bsSuggest({
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
			        var url="/clbs/v/monitoring/command/getCommandParam";
			        var minotor = realTimeCommand.getMinotorObj(currentCommandType);
		            var parameter={"vid": vehicleId,"commandType":currentCommandType,"isRefer":true,"minotor":minotor};
		            json_ajax("POST",url,"json",true,parameter, realTimeCommand.setCommand);
				}).on('onUnsetSelectValue', function () {
				});
			}
		},
		getMinotorObj: function(commandType){
			  switch (commandType){
				case 11:
					return $("#reportObject").val();
				case 12:
					return $("#terminalObject").val();
				case 131:
					return $("#UpgradeObject").val();
				case 132:
					return $("#specifyServerObject").val();
				case 14:
					return $("#locationObject").val();
				case 16:
					return $("#telephoneObject").val();
				case 17:
					return $("#videoCameraObject").val();
				case 18:
					return $("#gnssObject").val();
				case 19:
					return $("#eventObject").val();
				case 20:
					return $("#phoneBookObject").val();
				case 21:
					return $("#infoDemandObject").val();
				case 22:
					return $("#baseStationObject").val();
				default: return "";
				}
		},
        //组织树预处理加载函数
        ajaxDataFilter: function(treeId, parentNode, responseData) {
        	 if (responseData.msg) {
     	    	var obj = JSON.parse(ungzip(responseData.msg));
     	    	var data;
     	    	if (obj.tree != null && obj.tree != undefined) {
     	    		data = obj.tree;
     	    	}else{
     	    		data = obj
     	    	}
     	        for (var i = 0; i < data.length; i++) {
     	        	if (data[i].type == "group") {
     	        		data[i].open = true;
     	        	}
     	        }
     	    }
    	    return data;
    	},
    	beforeClick :function (treeId,treeNode){
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, false, true);
            return true;
        },
    	//车辆树车牌信息点击事件
    	onClickVehicle: function(e,treeId,treeNode) {
    		//判断点击企业及分组以为(车)
            if (treeNode.iconSkin !== "assignmentSkin" && treeNode.iconSkin !== "groupSkin") {
        		treeClickFlag = true;
        		currentVehicle = treeNode.id;
        		myTable.requestData();
        		var url="/clbs/v/monitoring/command/getCommandTypes";
                var parameter={"vid": currentVehicle};
                json_ajax("POST",url,"json",true,parameter, realTimeCommand.initCommandTypes);
            }
        },
        initCommandTypes : function(data) {
        	var zTreeObj = $.fn.zTree.getZTreeObj("commandTreeDemo");
        	zTreeObj.checkAllNodes(false);
        	$("#reportParameters,.report-para-footer,#infoDemandMenu,#phoneBookSetting,#eventSetting," +
				"#terminalParameters,#terminalControl,#unlimitedUpgrade,#connectSpecifyServer,#locationReporting," +
				"#terminalSearch,#telephoneParameters,#videoCameraParameters,#gnssParameters," +
				".report-para-footer-control,.report-para-footer-control-1").hide();
			if(data.success === true){
        		if (data.msg === null && data.obj.commandTypes !== null
					&&data.obj.commandTypes.length !== undefined && data.obj.commandTypes.length !== 0) {
        			var commandTypes = data.obj.commandTypes;
        			var zTree = $.fn.zTree.getZTreeObj("commandTreeDemo");
            		for(var i = 0, commandLength = commandTypes.length; i < commandLength; i++){
            			var commandType = commandTypes[i];
            			  var nodes = zTree.getNodesByParam("id", commandType.commandType, null);
                          zTree.checkNode(nodes[0], true, true);
            		}
            	}
           	}else{
           		layer.msg(data.msg,{move:false});
           	}
		},
        // 车辆树加载成功事件
        zTreeOnAsyncSuccess: function(event, treeId, treeNode, msg){
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            var avid = $("#avid").val();
            if(avid !== undefined && avid !== ""){
            	var node = treeObj.getNodesByParam("name", avid, null);
            	if(node !== undefined && node.length > 0){
            		treeObj.checkNode(node[0], true, true);
            	}
            	vehicleList = avid;
            	$("#alarmSearch").click();
            }
            
        	// 更新节点数量
            treeObj.updateNodeCount(treeNode);
            // 默认展开200个节点
            var initLen = 0;
            notExpandNodeInit = treeObj.getNodesByFilter(assignmentNotExpandFilter);
            for (i = 0; i < notExpandNodeInit.length; i++) {
            	treeObj.expandNode(notExpandNodeInit[i], true, true, false, true);
                initLen += notExpandNodeInit[i].children.length;
                if (initLen >= 200) {
                    break;
                }
            }
            // webSocket.subscribe(headers, '/topic/fencestatus', realTimeCommand.updataFenceData,null, null);
        },
        //车辆树勾选事件
		onCheckVehicle : function(e, treeId, treeNode) {
			var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
					.getCheckedNodes(true), v = "";t = "";
			//若为取消勾选则不展开节点
        	if(treeNode.checked){
        		zTree.expandNode(treeNode, true, true, true, true); // 展开节点
        	}
			for (var i = 0, l = nodes.length; i < l; i++) {
				if (nodes[i].type === "vehicle") {
					v += nodes[i].name + ",";
					t += nodes[i].id + ",";
				}
			}
			vehicleList = v;
			vehicleIdList = t;
		},
		zTreeOnExpand : function (event, treeId, treeNode) {
			//判断是否是勾选操作展开的树(是则继续执行，不是则返回)
        	var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (treeNode.type == "group" && treeNode.checked){
            	 var url = "/clbs/m/functionconfig/fence/bindfence/putMonitorByGroup";
                 json_ajax("post", url, "json", false, {"groupId": treeNode.id,"isChecked":treeNode.checked,"monitorType":"vehicle","deviceType":"1"}, function (data) {
                	 var result = data.obj;
                     if (result != null && result != undefined){
                    	 $.each(result, function(i) {
                    		  var pid = i; //获取键值
                    		  var chNodes = result[i] //获取对应的value
	                		  var parentTid = zTreeIdJson[pid][0];
	                          var parentNode = treeObj.getNodeByTId(parentTid);
	                          if (parentNode.children === undefined) {
	                        	  treeObj.addNodes(parentNode, []);
	                          }
                		 });
                     }
                 })
             }
            realTimeCommand.checkCurrentNodes(treeNode);
        },
        zTreeBeforeCheck : function(treeId, treeNode){
        	var flag = true;
        	if (!treeNode.checked) {
           		var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
        		.getCheckedNodes(true), v = "";
               	var nodesLength = 0;
               	for (var i=0;i<nodes.length;i++) {
               		if(nodes[i].type == "people" || nodes[i].type == "vehicle"){
               			nodesLength += 1;
               		}
               	}
               	if (treeNode.type == "group" || treeNode.type == "assignment"){ // 判断若勾选节点数大于5000，提示
               		var zTree = $.fn.zTree.getZTreeObj("treeDemo")
               		json_ajax("post", "/clbs/m/personalized/ico/getCheckedVehicle", 
                       		"json", false, {"parentId": treeNode.id,"type": treeNode.type,"deviceType":"1"}, function (data) {
                       			nodesLength += data.obj;
                        })
               	} else if (treeNode.type == "people" || treeNode.type == "vehicle"){
               		nodesLength += 1;
               	}
               	if(nodesLength > 5000){
               		layer.msg(treeMaxLength5000);
               		flag = false;
               	}
           	}
        	if(flag){
        		//若组织节点已被勾选，则是勾选操作，改变勾选操作标识
            	if(treeNode.type == "group" && !treeNode.checked){
            		checkFlag = true;
            	}
        	}
        	return flag;
        },
        zTreeOnNodeCreated: function (event, treeId, treeNode) {
	        var zTree = $.fn.zTree.getZTreeObj("treeDemo");
	        var id = treeNode.id.toString();
	        var list = [];
	        if (zTreeIdJson[id] == undefined || zTreeIdJson[id] == null) {
	            list = [treeNode.tId];
	            zTreeIdJson[id] = list;
	        } else {
	            zTreeIdJson[id].push(treeNode.tId)
	        }
	    },
		//指令类型树 勾选事件
		onCheckCommand: function(){
			var zTree = $.fn.zTree.getZTreeObj("commandTreeDemo");
			var nodes = zTree.getCheckedNodes(true);
	        var changedNodes = zTree.getChangeCheckedNodes();
	        for (var i = 0, l = changedNodes.length; i < l; i++) {
	            changedNodes[i].checkedOld = changedNodes[i].checked;
	        }
		},
		//指令类型树 点击事件
		onClickCommand : function(e,treeId,treeNode) {
    		$("#eventMain-container").find(">div").find("div.eventIdInfo>input").removeAttr("disabled","disabled");
            clickTreeNodeName = treeNode.tId;
            realTimeCommand.executeCommandShow();
            currentCommandType = treeNode.id;
            if(treeClickFlag){//仅当点击了车辆树某辆车时，才进行指令参数的查询。否则，只是批量新增指令参数。
	            var url="/clbs/v/monitoring/command/getCommandParam";
	            var parameter={"vid": currentVehicle,"commandType":treeNode.id,"isRefer":false};
	            json_ajax("POST",url,"json",true,parameter, realTimeCommand.setCommand);
            }else{
            	var url="/clbs/v/monitoring/command/getReferCommand";
 	            var parameter={"commandType":treeNode.id};
 	            json_ajax("POST",url,"json",true,parameter, realTimeCommand.setRefer);
            }
            //判断当前点击的指令类型是否有值(当前未勾选)
            if(treeNode.checked === false){
            	//事件设置
            	//先清空事件设置里面的网页内容
            	$("#eventSetting").html("");
            	//再定义新的内容
            	var html = 
        		"<div class='form-group'>"+
					"<label class='col-md-3 control-label'>监控对象：</label>"+
					"<div class='col-md-3'>"+
						"<input type='text' id='eventObject' class='form-control' disabled/>"+
					"</div>"+
					"<label class='col-md-2 control-label'>参考对象：</label>"+
					"<div class='col-md-3'>"+
						"<div class='input-group'>"+
							"<input name='brands' type='text' class='form-control' id='eventPlate'>"+
							"<div class='input-group-btn'>"+
								"<button type='button' class='btn btn-white dropdown-toggle' data-toggle='dropdown'>"+
									"<span class='caret'></span>"+
								"</button>"+
								"<ul class='dropdown-menu dropdown-menu-right' role='menu'>"+
								"</ul>"+
							"</div>"+
						"</div>"+
					"</div>"+
				"</div>"+
				"<div class='form-group'>"+
					"<label class='col-md-3 control-label'>操作类型：</label>"+
					"<div class='col-md-3'>"+
						"<select id='eventOperateType' name='eOperationType' class='form-control' onchange='realTimeCommand.eventSettingOperateType()'>"+
						 "<option value='0'>删除所有</option>"+
							"<option value='1'>更新</option>"+
							"<option value='2'>追加</option>"+
							"<option value='3'>修改</option>"+
							"<option value='4'>删除</option>"+
						"</select>"+
					"</div>"+
					"<label class='col-md-3 control-label'></label>"+
					"<div class='col-md-3'></div>"+
				"</div>"+
				"<div  id='eventMain-container'>"+
					"<div class='form-group' >"+
						"<label class='col-md-3 control-label'>事件ID：</label>"+
						"<div class='col-md-3 eventIdInfo'>"+
							"<input id='eventId_2' type='text' name='eventId' value='1' class='form-control' onblur='realTimeCommand.inputBlur()'/>"+
						"</div>"+
						"<label class='col-md-2 control-label'>事件内容：</label>"+
						"<div class='col-md-3'>"+
							"<input type='text' id='eventContent_2'  name='eventContent' placeholder='请输入事件内容' class='form-control' onblur='realTimeCommand.inputBlur()'/>"+
						"</div>"+
						"<div class='col-md-1'>"+
							"<button id='event-add-btn' onclick='realTimeCommand.addEventSetting();' type='button' class='btn btn-primary addIcon'>"+
								"<span class='glyphicon glyphiconPlus' aria-hidden='true'></span>"+
							"</button>"+
						"</div>"+
					"</div>"+
				"</div>";
            	//重新写入事件设置内容
            	$("#eventSetting").html(html);
            	//电话本设置
            	//先清空事件设置里面的网页内容
            	$("#phoneBookSetting").html("");
            	//再定义新的内容
            	var phoneHtml =
				"<div class='form-group'>"+
					"<label class='col-md-3 control-label'>监控对象：</label>"+
					"<div class='col-md-3'>"+
						"<input type='text' id='phoneBookObject' class='form-control' disabled/>"+
					"</div>"+
					"<label class='col-md-2 control-label'>参考对象：</label>"+
					"<div class='col-md-3'>"+
						"<div class='input-group'>"+
							"<input name='brands' type='text' class='form-control' id='phoneBookPlate'>"+
							"<div class='input-group-btn'>"+
								"<button type='button' class='btn btn-white dropdown-toggle' data-toggle='dropdown'>"+
									"<span class='caret'></span>"+
								"</button>"+
								"<ul class='dropdown-menu dropdown-menu-right' role='menu'>"+
								"</ul>"+
							"</div>"+
						"</div>"+
					"</div>"+
				"</div>"+
				"<div class='form-group'>"+
					"<label class='col-md-3 control-label'>操作类型：</label>"+
					"<div class='col-md-3'>"+
						"<select id='phoneBookOperateType' name='pOperationType' class='form-control' onchange='realTimeCommand.phoneBookSettingOperateType()'>"+
						 "<option value='0'>删除所有</option>"+
							"<option value='1'>更新</option>"+
							"<option value='2'>追加</option>"+
							"<option value='3'>修改</option>"+
						"</select>"+
					"</div>"+
					"<label class='col-md-3 control-label'></label>"+
					"<div class='col-md-3'></div>"+
				"</div>"+
				"<div id='phoneBook-MainContent'>"+
					"<div class='form-group'>"+
						"<label class='col-md-3 control-label'>联系人ID：</label>"+
						"<div class='col-md-3 phoneBookIdInfo'>"+
							"<input type='text' id='phoneBookId_2' name='phoneBookId' value='1' class='form-control' onblur='realTimeCommand.inputBlur()'/>"+
						"</div>"+
						"<label class='col-md-2 control-label'>联系人：</label>"+
						"<div class='col-md-3'>"+
							"<input type='text' id='phoneBookContact_2' name='contact' placeholder='请输入联系人' class='form-control' onblur='realTimeCommand.inputBlur()'/>"+
						"</div>"+
					"</div>"+
					"<div class='form-group'>"+
						"<label class='col-md-3 control-label'>电话号码：</label>"+
						"<div class='col-md-3'>"+
							"<input type='text' id='phoneBookNumber_2' name='phoneNo' placeholder='请输入电话号码' class='form-control' onblur='realTimeCommand.inputBlur()'/>"+
						"</div>"+
						"<label class='col-md-2 control-label'>呼叫类型：</label>"+
						"<div class='col-md-3'>"+
							"<select id='phoneBookOperateType_2' name='callType' class='form-control'>"+
								"<option value='1'>呼入</option>"+
								"<option value='2'>呼出</option>"+
								"<option value='3'>呼入/呼出</option>"+
							"</select>"+
						"</div>"+
						"<div class='col-md-1'>"+
							"<button id='phoneBook-add-btn' onclick='realTimeCommand.addPhoneBookEvent()' type='button' class='btn btn-primary addIcon'>"+
								"<span class='glyphicon glyphiconPlus' aria-hidden='true'></span>"+
							"</button>"+
						"</div>"+
					"</div>"+
				"</div>";
        		//重新写入电话本设置内容
            	$("#phoneBookSetting").html(phoneHtml);
            	//信息点播设置
            	//先清空事件设置里面的网页内容
            	$("#infoDemandMenu").html("");
            	//再定义新的内容
            	var infoHtml =
				"<div class='form-group'>"+
					"<label class='col-md-3 control-label'>监控对象：</label>"+
					"<div class='col-md-3'>"+
						"<input type='text' id='infoDemandObject' class='form-control' disabled/>"+
					"</div>"+
					"<label class='col-md-2 control-label'>参考对象：</label>"+
					"<div class='col-md-3'>"+
						"<div class='input-group'>"+
							"<input name='brands' type='text' class='form-control' id='infoDemandPlate'>"+
							"<div class='input-group-btn'>"+
								"<button type='button' class='btn btn-white dropdown-toggle' data-toggle='dropdown'>"+
									"<span class='caret'></span>"+
								"</button>"+
								"<ul class='dropdown-menu dropdown-menu-right' role='menu'>"+
								"</ul>"+
							"</div>"+
						"</div>"+
					"</div>"+
				"</div>"+
				"<div class='form-group'>"+
					"<label class='col-md-3 control-label'>操作类型：</label>"+
					"<div class='col-md-3'>"+
						"<select id='infoDemandOperateType' name='iOperationType' class='form-control' onchange='realTimeCommand.infoDemandMenuSettingOperateType()'>"+
						 "<option value='0'>删除所有</option>"+
							"<option value='1'>更新</option>"+
							"<option value='2'>追加</option>"+
							"<option value='3'>修改</option>"+
						"</select>"+
					"</div>"+
					"<label class='col-md-3 control-label'></label>"+
					"<div class='col-md-3'></div>"+
				"</div>"+
				"<div class='form-group' id='infoDemand-MainContent'>"+
					"<label class='col-md-3 control-label'>信息ID：</label>"+
					"<div class='col-md-3 infoDemandMenuId'>"+
						"<input type='text' id='infoDemandId_2' name='infoId' value='1' class='form-control' onblur='realTimeCommand.inputBlur()'/>"+
					"</div>"+
					"<label class='col-md-2 control-label'>信息名称：</label>"+
					"<div class='col-md-3'>"+
						"<input type='text' id='infoDemandName_2' name='infoContent' placeholder='请输入信息名称' class='form-control' onblur='realTimeCommand.inputBlur()'/>"+
					"</div>"+
					"<div class='col-md-1'>"+
						"<button id='infoDemand-add-btn' onclick='realTimeCommand.addInfoDemandEvent()' type='button' class='btn btn-primary addIcon'>"+
							"<span class='glyphicon glyphiconPlus' aria-hidden='true'></span>"+
						"</button>"+
					"</div>"+
				"</div>";
        		//重新写入信息点播设置内容
            	$("#infoDemandMenu").html(infoHtml);
            }
            //判断当前点击的指令类型是否有值(当前勾选)
            else{
            	//如果有指令类型有勾选则查询显示
            	  //读取每个指令类型的参数值
            }
		},
		setRefer: function(data){
			if(data.success == true){
				$("#phoneBookObject,#infoDemandObject,#eventObject,#gnssObject,#videoCameraObject,#telephoneObject,#locationObject,#specifyServerObject,#UpgradeObject,#terminalObject,#reportObject,#baseStationObject").val(vehicleList);
				if(data.msg == null&&data.obj.referVehicleList!= null){
        			realTimeCommand.initReferVehicleList(data.obj.referVehicleList);
        		}
			}else{
				layer.msg(data.msg,{move:false});
			}
		},
		setCommand: function(data){
	        	if(data.success == true){
	        		$("#phoneBookObject,#infoDemandObject,#eventObject,#gnssObject,#videoCameraObject,#telephoneObject,#locationObject,#specifyServerObject,#UpgradeObject,#terminalObject,#reportObject,#baseStationObject").val(data.obj.vid);
	        		if(data.msg == null&&data.obj.referVehicleList!= null){
	        			realTimeCommand.initReferVehicleList(data.obj.referVehicleList);
	        		}
	        		if (data.msg == null&&data.obj.communicationParam!= null) {
        			    var communicationParam = data.obj.communicationParam;
            			$("#reportMainApn").val(communicationParam.mainServerAPN);
            			$("#reportMainAddress").val(communicationParam.mainServerAddress);
            			$("#reportBackupApn").val(communicationParam.backupsServerAPN);
            			$("#reportBackupAddress").val(communicationParam.backupsServerAddress);
            			$("#reportServerTcp").val(communicationParam.serverTCPPort);
            			$("#reportServerUdp").val(communicationParam.serverUDPPort);
            			$("#reportServerAccound").val(communicationParam.mainServerCallUserName);
            			$("#reportServerPwd").val(communicationParam.mainServerCallUserPwd);
            			$("#reportBackupAccound").val(communicationParam.backupsServerCallUserName);
            			$("#reportBackupPwd").val(communicationParam.backupsServerCallUserPwd);
	        		}else{

	        		}
	        		if(data.msg == null&&data.obj.deviceParam!= null){
	        			var deviceParam = data.obj.deviceParam;
	        			$("#terminalSendTime").val(deviceParam.heartSpace);
	        			$("#terminalAnswerTime").val(deviceParam.tcpAckTimeOut);
	        			$("#terminalAnswerTcp").val(deviceParam.tcpReUpTimes);
	        			$("#terminalAnswerUdp").val(deviceParam.udpAckTimeOut);
	        			$("#terminalUdpNum").val(deviceParam.udpReUpTimes);
	        			$("#terminalAnswerSms").val(deviceParam.smsAckTimeOut);
	        			$("#terminalSmsNum").val(deviceParam.smsReUpTimes);
	        		}
	        		if(data.msg == null&&data.obj.wirelessUpdateParam!= null){
	        			var wirelessUpdateParam = data.obj.wirelessUpdateParam;
	        			$("#UpgradeDialName").val(wirelessUpdateParam.wDailUserName);
	        			$("#UpgradeDialPwd").val(wirelessUpdateParam.wDailPwd);
	        			$("#UpgradeAddress").val(wirelessUpdateParam.wAddress);
	        			$("#UpgradeTcpTort").val(wirelessUpdateParam.wTcpPort);
	        			$("#UpgradeUdpTort").val(wirelessUpdateParam.wUdpPort);
	        			$("#UpgradeManufacturer").val(wirelessUpdateParam.manufactorId);
	        			$("#UpgradeHardware").val(wirelessUpdateParam.hardwareVersion);
	        			$("#UpgradeFirmware").val(wirelessUpdateParam.firmwareVersion);
	        			$("#UpgradeUrlAddress").val(wirelessUpdateParam.url);
	        			$("#UpgradeTimeLimit").val(wirelessUpdateParam.wTimeLimit);
	        			$("#UpgradeConnection").val(wirelessUpdateParam.wAccessControl);
	        			$("#UpgradeDial").val(wirelessUpdateParam.wDailName);
	        		}
	        		if(data.msg == null&&data.obj.deviceConnectServerParam!= null){
	        			var deviceConnectServerParam = data.obj.deviceConnectServerParam;
	        			$("#specifyServerConnect").val(deviceConnectServerParam.cAccessControl);
	        			$("#specifyServerDial").val(deviceConnectServerParam.cDailName);
	        			$("#specifyServerDialName").val(deviceConnectServerParam.cDailUserName);
	        			$("#specifyServerDialPwd").val(deviceConnectServerParam.cDailPwd);
	        			$("#specifyServerAddress").val(deviceConnectServerParam.cAddress);
	        			$("#specifyServerTcpPort").val(deviceConnectServerParam.cTcpPort);
	        			$("#specifyServerUdpPort").val(deviceConnectServerParam.cUdpPort);
	        			$("#specifyServerTimeLimit").val(deviceConnectServerParam.cTimeLimit);
	        		}
	        		if(data.msg == null&&data.obj.positionParam!= null){
	        			var positionParam = data.obj.positionParam;
	        			$("#locationTactics").val(positionParam.positionUpTactics);
	        			$("#locationProgram").val(positionParam.positionUpScheme);
	        			$("#locationNoLogin").val(positionParam.driverLoggingOutUpTimeSpace);
	        			$("#locationSleep").val(positionParam.dormancyUpTimeSpace);
	        			$("#locationAlarmTime").val(positionParam.emergencyAlarmUpTimeSpace);
	        			$("#locationDefaultTime").val(positionParam.defaultTimeUpSpace);
	        			$("#locationDefaultDistance").val(positionParam.defaultDistanceUpSpace);
	        			$("#locationNoLoginDistance").val(positionParam.driverLoggingOutUpDistanceSpace);
	        			$("#locationSleepDistance").val(positionParam.dormancyUpDistanceSpace);
	        			$("#locationAlarmDistance").val(positionParam.emergencyAlarmUpDistanceSpace);
	        		}
	        		if(data.msg == null&&data.obj.phoneParam!= null){
	        			var phoneParam = data.obj.phoneParam;
	        			$("#telephoneNumber").val(phoneParam.platformPhoneNumber);
	        			$("#telephoneResetNumber").val(phoneParam.resetPhoneNumber);
	        			$("#telephoneSetNumber").val(phoneParam.reInitialPhoneNumber);
	        			$("#telephoneSnsNumber").val(phoneParam.platformSMSPhoneNumber);
	        			$("#telephoneSnsAlarm").val(phoneParam.receiveDeviceSMSTxtAlarmPhoneNumber);
	        			$("#telephoneStrategy").val(phoneParam.deviceAnswerPhoneType);
	        			$("#telephoneMaxTime").val(phoneParam.timesMaxCallTime);
	        			$("#telephoneMonthTime").val(phoneParam.monthlyMaxCallTime);
	        			$("#telephoneMonitor").val(phoneParam.listenPhoneNumber);
	        			$("#monitorPrivilege").val(phoneParam.platformPrivilegeSMSNumber);
	        		}
	        		if(data.msg == null&&data.obj.cameraParam!= null){
        			    var cameraParam = data.obj.cameraParam;
        				$("#videoCameraSwitchOne").val(cameraParam.cameraTimerOpenFlag1);
            			$("#videoCameraStorageOne").val(cameraParam.cameraTimerSaveFlag1);
            			$("#videoCameraSwitchTwo").val(cameraParam.cameraTimerOpenFlag2);
            			$("#videoCameraStorageTwo").val(cameraParam.cameraTimerSaveFlag2);
            			$("#videoCameraSwitchThree").val(cameraParam.cameraTimerOpenFlag3);
            			$("#videoCameraStorageThree").val(cameraParam.cameraTimerSaveFlag3);
            			$("#videoCameraSwitchFour").val(cameraParam.cameraTimerOpenFlag4);
            			$("#videoCameraStorageFour").val(cameraParam.cameraTimerSaveFlag4);
            			$("#videoCameraSwitchFive").val(cameraParam.cameraTimerOpenFlag5);
            			$("#videoCameraStorageFive").val(cameraParam.cameraTimerSaveFlag5);
            			$("#videoCameraTimeUnit").val(cameraParam.timingUnit);
            			$("#videoCameraTimeInterval").val(cameraParam.timingSpace);
            			$("#videoCameraSwitchMarkOne").val(cameraParam.cameraDistanceOpenFlag1);
            			$("#videoCameraStorageMarkOne").val(cameraParam.cameraDistanceSaveFlag1);
            			$("#videoCameraSwitchMarkTwo").val(cameraParam.cameraDistanceOpenFlag2);
            			$("#videoCameraStorageMarkTwo").val(cameraParam.cameraDistanceSaveFlag2);
            			$("#videoCameraSwitchMarkThree").val(cameraParam.cameraDistanceOpenFlag3);
            			$("#videoCameraStorageMarkThree").val(cameraParam.cameraDistanceSaveFlag3);
            			$("#videoCameraSwitchMarkFour").val(cameraParam.cameraDistanceOpenFlag4);
            			$("#videoCameraStorageMarkFour").val(cameraParam.cameraDistanceSaveFlag4);
            			$("#videoCameraSwitchMarkFive").val(cameraParam.cameraDistanceOpenFlag5);
            			$("#videoCameraStorageMarkFive").val(cameraParam.cameraDistanceSaveFlag5);
            			$("#videoCameraDistanceUnit").val(cameraParam.distanceUnit);
            			$("#videoCameraDistanceInterval").val(cameraParam.distanceSpace);
            			$("#videoCameraQuality").val(cameraParam.pictureQuality);
            			$("#videoCameraBrightness").val(cameraParam.luminance);
            			$("#videoCameraContrast").val(cameraParam.contrast);
            			$("#videoCameraSaturation").val(cameraParam.saturation);
            			$("#videoCameraChroma").val(cameraParam.chroma);
	        		}
	        		if(data.msg == null&&data.obj.gnssParam!= null){
        				var gnssParam = data.obj.gnssParam;
            			$("#GPSFlag").val(gnssParam.gPSFlag);
            			$("#beidouFlag").val(gnssParam.beidouFlag);
            			$("#GLONASSFlag").val(gnssParam.gLONASSFlag);
            			$("#GalileoFlag").val(gnssParam.galileoFlag);
            			$("#GNSSBaudRate").val(gnssParam.gNSSBaudRate);
            			$("#GNSSPositionOutputRate").val(gnssParam.gNSSPositionOutputRate);
            			$("#GNSSPositionCollectRate").val(gnssParam.gNSSPositionCollectRate);
            			$("#GNSSPositionUploadType").val(gnssParam.gNSSPositionUploadType);
	        		}
	        		if(data.msg == null&&data.obj.event!= null&&data.obj.event.length>0){
	        			var event = data.obj.event;
	        			var addEventIndex1;
	        			var html="";
	        			$("#eventOperateType").val(event[0].operationType);
	        			for(var i=0;i<event.length;i++){
	        				if(i>0){
	        					addEventIndex1 = i+2;
	        				    html += "<div class='form-group' id='eventMain-container_"+ addEventIndex1 +"'><label class='col-md-3 control-label'>事件ID：</label><div class='col-md-3 eventIdInfo'><input type='text' name='eventId' id='eventId_"+ addEventIndex1 +"'  value='"+ event[i].eventId +"' class='form-control' onblur='realTimeCommand.inputBlur()'></div>" +
	        					"<label class='col-md-2 control-label'>事件内容：</label><div class='col-md-3'><input type='text' name='eventContent' id='eventContent_"+ addEventIndex1 +"' placeholder='请输入事件内容' value='"+ event[i].eventContent +"' class='form-control' onblur='realTimeCommand.inputBlur()'></div><div class='col-md-1'><button type='button' " +
	        					"class='btn btn-danger eventSettingDelete deleteIcon'><span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button></div></div>";
	        				}else{
	        					$("#eventId_2").val(event[i].eventId);
	        					$("#eventContent_2").val(event[i].eventContent);
	        				}
	        			}
	        			if($('input[id ^= "eventId"]').length==1){
	        				addEventIndex = event[event.length-1].eventId+1;
	        				$("#eventMain-container").append(html);
	        			}
	        		}
					if(data.msg == null&&data.obj.phone!= null&&data.obj.phone.length>0){
						var phone = data.obj.phone;
						var addPhoneIndex1;
						var html="";
						$("#phoneBookOperateType").val(phone[0].operationType);
						for(var i=0;i<phone.length;i++){
							if(i>0){
								addPhoneIndex1 = i+2;
								html += "<div id='phoneBook-MainContent_"+ addPhoneIndex1 +"'><div class='form-group'><label class='col-md-3 control-label'>电话本ID：</label><div class='col-md-3 phoneBookIdInfo'><input type='text' id='phoneBookId_"+ addPhoneIndex1 +"' name='phoneBookId'  value='"+ phone[i].phoneBookId +"' class='form-control' onblur='realTimeCommand.inputBlur()'/></div><label class='col-md-2 control-label'>联系人：</label><div class='col-md-3'><input type='text' id='phoneBookContact_"+ addPhoneIndex1 +"' name='contact' placeholder='请输入联系人' value='"+ phone[i].contact +"' class='form-control' onblur='realTimeCommand.inputBlur()'/></div></div><div class='form-group'><label class='col-md-3 control-label'>电话号码：</label><div class='col-md-3'><input type='text' id='phoneBookNumber_"+ addPhoneIndex1 +"' name='phoneNo' value='"+ phone[i].phoneNo +"' placeholder='请输入电话号码' class='form-control' onblur='realTimeCommand.inputBlur()'/></div><label class='col-md-2 control-label'>呼叫类型：</label><div class='col-md-3'><select id='phoneBookOperateType_"+ addPhoneIndex1 +"' value='"+ phone[i].callType +"' name='callType' class='form-control'><option value='1'>呼入</option><option value='2'>呼出</option><option value='3'>呼入/呼出</option></select></div><div class='col-md-1'><button type='button' class='btn btn-danger phoneBookDelete deleteIcon'><span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button></div></div></div>";
							}else{
								$("#phoneBookId_2").val(phone[i].phoneBookId);
	        					$("#phoneBookContact_2").val(phone[i].contact);
	        					$("#phoneBookNumber_2").val(phone[i].phoneNo);
	        					$("#phoneBookOperateType_2").val(phone[i].callType);
							}
						}
						if($('input[id ^= "phoneBookId"]').length==1){
							addPhoneIndex = parseInt(phone[phone.length-1].phoneBookId)+1;
	        				$("#phoneBook-MainContent").append(html);
	        			}
					}
					if(data.msg == null&&data.obj.info!= null&&data.obj.info.length>0){
						var info = data.obj.info;
						var addInfoDemandIndex1;
						var html="";
						$("#infoDemandOperateType").val(info[0].operationType);
						for(var i=0;i<info.length;i++){
							if(i>0){
								addInfoDemandIndex1 = i+2;
								html += "<div class='form-group' id='infoDemand-MainContent_"+ addInfoDemandIndex1 +"'><label class='col-md-3 control-label'>信息ID：</label><div class='col-md-3 infoDemandMenuId'><input type='text' id='infoDemandId_"+ addInfoDemandIndex1 +"'   name='infoId' value='"+ info[i].infoId +"' class='form-control' onblur='realTimeCommand.inputBlur()'/></div><label class='col-md-2 control-label'>信息名称：</label><div class='col-md-3'><input type='text' id='infoDemandName"+ addInfoDemandIndex1 +"'  name='infoContent' value='"+ info[i].infoContent +"' placeholder='请输入信息名称' class='form-control' onblur='realTimeCommand.inputBlur()'/></div><div class='col-md-1'><button type='button' class='btn btn-danger infoDemandDelete deleteIcon'><span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button></div></div>";
							}else{
								$("#infoDemandId_2").val(info[i].infoId);
		        				 $("#infoDemandName_2").val(info[i].infoContent);
							}

						}
						if($('input[id ^= "infoId"]').length==1){
							addInfoDemandIndex = info[info.length-1].infoId+1;
							$("#infoDemand-MainContent").append(html);
	        			}

					}
	        	}else{
	        		layer.msg(data.msg,{move:false});
	        	}
	        },
		getCommandCheckedNodes : function() {
			var zTree = $.fn.zTree.getZTreeObj("commandTreeDemo"), nodes = zTree
					.getCheckedNodes(true), v = "";
			for (var i = 0, l = nodes.length; i < l; i++) {
				if (nodes[i].id != 1) {
					v += nodes[i].id + ",";
				}
			}
			commandNodes = v;
		},
		//执行类型及参数显示
		executeCommandShow: function(){
            switch(clickTreeNodeName){
            	//通讯参数
            	case "commandTreeDemo_2":
            		$("#reportParameters,.report-para-footer").show();
            		$("#baseStationParameters,#infoDemandMenu,#phoneBookSetting,#eventSetting,#terminalParameters,#terminalControl,#unlimitedUpgrade,#connectSpecifyServer,#locationReporting,#terminalSearch,#telephoneParameters,#videoCameraParameters,#gnssParameters,.report-para-footer-control,.report-para-footer-control-1").hide();
            		break;
            	//终端参数
            	case "commandTreeDemo_3":
            		$("#terminalParameters,.report-para-footer").show();
            		$("#baseStationParameters,#infoDemandMenu,#phoneBookSetting,#eventSetting,#reportParameters,#terminalControl,#unlimitedUpgrade,#connectSpecifyServer,#locationReporting,#terminalSearch,#telephoneParameters,#videoCameraParameters,#gnssParameters,.report-para-footer-control,.report-para-footer-control-1").hide();
            		break;
            	//终端控制
            	case "commandTreeDemo_4":
            		$("#terminalControl,.report-para-footer-control").show();
            		$("#baseStationParameters,#infoDemandMenu,#phoneBookSetting,#eventSetting,#reportParameters,#terminalParameters,#unlimitedUpgrade,#connectSpecifyServer,#locationReporting,#terminalSearch,#telephoneParameters,#videoCameraParameters,#gnssParameters,.report-para-footer,.report-para-footer-control-1").hide();
            		break;
            	//无限升级
            	case "commandTreeDemo_5":
            		$("#unlimitedUpgrade,.report-para-footer").show();
            		$("#baseStationParameters,#infoDemandMenu,#phoneBookSetting,#eventSetting,#reportParameters,#terminalControl,#terminalParameters,#connectSpecifyServer,#locationReporting,#terminalSearch,#telephoneParameters,#videoCameraParameters,#gnssParameters,.report-para-footer-control,.report-para-footer-control-1").hide();
            		break;
            	//控制终端连接指定服务器
            	case "commandTreeDemo_6":
            		$("#connectSpecifyServer,.report-para-footer").show();
            		$("#baseStationParameters,#infoDemandMenu,#phoneBookSetting,#eventSetting,#reportParameters,#unlimitedUpgrade,#terminalControl,#terminalParameters,#locationReporting,#terminalSearch,#telephoneParameters,#videoCameraParameters,#gnssParameters,.report-para-footer-control,.report-para-footer-control-1").hide();
            		realTimeCommand.connectionControlSH();
            		break;
            	//位置汇报参数
            	case "commandTreeDemo_7":
            		$("#locationReporting,.report-para-footer").show();
            		$("#baseStationParameters,#infoDemandMenu,#phoneBookSetting,#eventSetting,#reportParameters,#unlimitedUpgrade,#terminalControl,#connectSpecifyServer,#terminalParameters,#terminalSearch,#telephoneParameters,#videoCameraParameters,#gnssParameters,.report-para-footer-control,.report-para-footer-control-1").hide();
            		realTimeCommand.connectionControlSH();
            		break;
            	//终端查询
            	case "commandTreeDemo_8":
            		$("#terminalSearch,.report-para-footer-control-1").show();
            		$("#baseStationParameters,#infoDemandMenu,#phoneBookSetting,#eventSetting,#locationReporting,#reportParameters,#unlimitedUpgrade,#terminalControl,#connectSpecifyServer,#terminalParameters,#telephoneParameters,#videoCameraParameters,#gnssParameters,.report-para-footer,.report-para-footer-control").hide();
            		break;
            	//电话参数
            	case "commandTreeDemo_9":
            		$("#telephoneParameters,.report-para-footer").show();
            		$("#baseStationParameters,#infoDemandMenu,#phoneBookSetting,#eventSetting,#terminalSearch,#locationReporting,#reportParameters,#unlimitedUpgrade,#terminalControl,#connectSpecifyServer,#terminalParameters,#videoCameraParameters,#gnssParameters,.report-para-footer-control,.report-para-footer-control-1").hide();
            		break;
            	//视频拍照参数
            	case "commandTreeDemo_10":
            		$("#videoCameraParameters,.report-para-footer").show();
            		$("#baseStationParameters,#infoDemandMenu,#phoneBookSetting,#eventSetting,#telephoneParameters,#terminalSearch,#locationReporting,#reportParameters,#unlimitedUpgrade,#terminalControl,#connectSpecifyServer,#terminalParameters,#gnssParameters,.report-para-footer-control,.report-para-footer-control-1").hide();
            		break;
            	//GNSS参数
            	case "commandTreeDemo_11":
            		$("#gnssParameters,.report-para-footer").show();
            		$("#baseStationParameters,#infoDemandMenu,#phoneBookSetting,#eventSetting,#videoCameraParameters,#telephoneParameters,#terminalSearch,#locationReporting,#reportParameters,#unlimitedUpgrade,#terminalControl,#connectSpecifyServer,#terminalParameters,.report-para-footer-control,.report-para-footer-control-1").hide();
            		break;
	        	//事件设置
	        		case "commandTreeDemo_12":
            		$("#eventSetting,.report-para-footer").show();
            		$("#baseStationParameters,#infoDemandMenu,#phoneBookSetting,#gnssParameters,#videoCameraParameters,#telephoneParameters,#terminalSearch,#locationReporting,#reportParameters,#unlimitedUpgrade,#terminalControl,#connectSpecifyServer,#terminalParameters,.report-para-footer-control,.report-para-footer-control-1").hide();
            		break;
	        	//电话本设置
	        	case "commandTreeDemo_13":
            		$("#phoneBookSetting,.report-para-footer").show();
            		$("#baseStationParameters,#infoDemandMenu,#eventSetting,#gnssParameters,#videoCameraParameters,#telephoneParameters,#terminalSearch,#locationReporting,#reportParameters,#unlimitedUpgrade,#terminalControl,#connectSpecifyServer,#terminalParameters,.report-para-footer-control,.report-para-footer-control-1").hide();
            		break;
            	//信息点播菜单
	        	case "commandTreeDemo_14":
            		$("#infoDemandMenu,.report-para-footer").show();
            		$("#baseStationParameters,#phoneBookSetting,#eventSetting,#gnssParameters,#videoCameraParameters,#telephoneParameters,#terminalSearch,#locationReporting,#reportParameters,#unlimitedUpgrade,#terminalControl,#connectSpecifyServer,#terminalParameters,.report-para-footer-control,.report-para-footer-control-1").hide();
            		break;
            	//基站参数设置
	        	case "commandTreeDemo_15":
            		$("#baseStationParameters,.report-para-footer").show();
            		$("#infoDemandMenu,#phoneBookSetting,#eventSetting,#gnssParameters,#videoCameraParameters,#telephoneParameters,#terminalSearch,#locationReporting,#reportParameters,#unlimitedUpgrade,#terminalControl,#connectSpecifyServer,#terminalParameters,.report-para-footer-control,.report-para-footer-control-1").hide();
            		break;
            }
		},
        initTable: function(){
			menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) +"\" disabled />"+ table[0].innerHTML +"</label></li>"
			for(var i = 1; i < table.length; i++){
			    menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i+2) +"\" />"+ table[i].innerHTML +"</label></li>"
			};
			$("#Ul-menu-text").html(menu_text);
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
							var obj = {};
		                    if (row.paramId != null && row.paramId != "") {
		                    	obj.paramId = row.paramId;
							}
	                     obj.id = row.id;
		       	         obj.paramId = row.paramId;
		       	         obj.vehicleId = row.vehicleId;
		       	         obj.type = row.commandType;
		       	         obj.dId = row.dId;
	                     var jsonStr = JSON.stringify(obj)
	                     result += "<input  type='checkbox' name='subChk'  value='" + jsonStr + "' />";
						 return result;
					}
				},
				{
					"data" : null,
					"class" : "text-center", //最后一列，操作按钮
					render : function(data, type, row, meta) {
						var editUrlPath = myTable.editUrl + row.id + ".gsp?paramId="+row.paramId+"&commandType="+row.commandType+"&vehicleId="+row.vehicleId+""; //修改地址
						var result = '';
						if(row.paramId == "终端关机"){
							result += '<button type="button" onclick="realTimeCommand.sendOne(\''+row.id+'\',\''+row.paramId+'\',\''+row.vehicleId+'\',\''+row.commandType+'\',\''+row.dId+'\')" type="button" class="editBtn editBtn-info btn-realtime-command-send"><i class="fa fa-issue"></i>终端关机</button>&ensp;';
							//删除按钮
							result += '<button type="button" onclick="myTable.deleteItem(\''
									+ row.id
									+ '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>&ensp;';
						}else if(row.paramId == "终端复位"){
							result += '<button type="button" onclick="realTimeCommand.sendOne(\''+row.id+'\',\''+row.paramId+'\',\''+row.vehicleId+'\',\''+row.commandType+'\',\''+row.dId+'\')" type="button" class="editBtn editBtn-info btn-realtime-command-send"><i class="fa fa-issue"></i>终端复位</button>&ensp;';
							//删除按钮
							result += '<button type="button" onclick="myTable.deleteItem(\''
									+ row.id
									+ '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>&ensp;';
						}else if(row.paramId == "恢复出厂设置"){
							result += '<button type="button" onclick="realTimeCommand.sendOne(\''+row.id+'\',\''+row.paramId+'\',\''+row.vehicleId+'\',\''+row.commandType+'\',\''+row.dId+'\')" type="button" class="editBtn editBtn-info btn-realtime-command-send"><i class="fa fa-issue"></i>恢复出厂设置</button>&ensp;';
							//删除按钮
							result += '<button type="button" onclick="myTable.deleteItem(\''
									+ row.id
									+ '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>&ensp;';
						}else if(row.paramId == "关闭数据通信"){
							result += '<button type="button" onclick="realTimeCommand.sendOne(\''+row.id+'\',\''+row.paramId+'\',\''+row.vehicleId+'\',\''+row.commandType+'\',\''+row.dId+'\')" type="button" class="editBtn editBtn-info btn-realtime-command-send"><i class="fa fa-issue"></i>关闭数据通信</button>&ensp;';
							//删除按钮
							result += '<button type="button" onclick="myTable.deleteItem(\''
									+ row.id
									+ '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>&ensp;';
						}else if(row.paramId == "关闭所有无线通信"){
							result += '<button type="button" onclick="realTimeCommand.sendOne(\''+row.id+'\',\''+row.paramId+'\',\''+row.vehicleId+'\',\''+row.commandType+'\',\''+row.dId+'\')" type="button" class="editBtn editBtn-info btn-realtime-command-send"><i class="fa fa-issue"></i>关闭所有无线通信</button>&ensp;';
							//删除按钮
							result += '<button type="button" onclick="myTable.deleteItem(\''
									+ row.id
									+ '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>&ensp;';
						}else if(row.paramId == "终端查询"){
							result += '<button type="button" onclick="realTimeCommand.sendOne(\''+row.id+'\',\''+row.paramId+'\',\''+row.vehicleId+'\',\''+row.commandType+'\',\''+row.dId+'\')" type="button" class="editBtn editBtn-info btn-realtime-command-send"><i class="glyphicon glyphicon-search"></i>终端查询</button>&ensp;';
							//删除按钮
							result += '<button type="button" onclick="myTable.deleteItem(\''
									+ row.id
									+ '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>&ensp;';
						}else{
							//修改按钮
							result += '<button href="'+editUrlPath+'" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
							//删除按钮
							result += '<button type="button" onclick="myTable.deleteItem(\''
									+ row.id
									+ '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>&ensp;';
							//下发按钮
							result += '<button type="button" onclick="realTimeCommand.sendOne(\''+row.id+'\',\''+row.paramId+'\',\''+row.vehicleId+'\',\''+row.commandType+'\',\''+row.dId+'\')" type="button" class="editBtn editBtn-info"><i class="fa fa-issue"></i>下发参数</button>&ensp;';
						}
						return result;
					}
				}, {
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
			                } else if  (data == "5") {
			                	return "终端离线，未下发";
			                } else {
			                    return "";
			                }
					}
				}, {
					"data" : "commandType",
					"class" : "text-center",
                    render : function(data, type, row, meta) {
                        if (data == "11") {
                            return '通讯参数';
                        }else if (data == "12"){
                            return '终端参数';
                        }else if (data == "13"){
                            return '终端控制';
                        }else if (data == "131"){
                            return '无线升级';
                        }else if (data == "132"){
                            return '控制终端连接指定服务器';
                        }else if (data == "14"){
                            return '位置汇报参数';
                        }else if (data == "15"){
                            return '终端查询';
                        }else if (data == "16"){
                            return '电话参数';
                        }else if (data == "17"){
                            return '视频拍照参数';
                        }else if (data == "18"){
                            return 'GNSS参数';
                        }else if (data == "19"){
                            return '事件设置';
                        }else if (data == "20"){
                            return '电话本设置';
                        }else if (data == "21"){
                            return '信息点播菜单';
                        }else if (data == "22"){
                            return '基站参数设置';
                        }

                    }
				},{
					"data" : "brand",
					"class" : "text-center",
				},  {
					"data" : "groupName",
					"class" : "text-center"
				}, {
					"data" : "createDataTime",
					"class" : "text-center"
				}
			];//ajax参数
			var ajaxDataParamFun = function(d) {
				d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
				d.vehicleIdList = vehicleIdList;
			};
			//表格setting
			var setting = {
				listUrl : "/clbs/v/monitoring/command/list",
				editUrl : "/clbs/v/monitoring/command/edit_",
				deleteUrl : "/clbs/v/monitoring/command/delete_",
				deletemoreUrl : "/clbs/v/monitoring/command/deletemore",
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
		//全选
		checkAllClick: function(){
			$("input[name='subChk']").prop("checked", this.checked);
		},
		//单选
		subChk: function(){
			$("#checkAll").prop("checked",subChk.length == subChk.filter(":checked").length ? true: false);
		},
		//批量删除
		delModel: function(){
			//判断是否至少选择一项
			var chechedNum = $("input[name='subChk']:checked").length;
			if (chechedNum == 0) {
				return;

			}
			var checkedList = new Array();
			$("input[name='subChk']:checked").each(function() {
				var jsonObj = $.parseJSON($(this).val());
	            checkedList.push(jsonObj.id);
			});
			myTable.deleteItems({
				'deltems' : checkedList.toString()
			});
		},
		//刷新
		refreshTable: function(){
        	$("#simpleQueryParam").val("");
            myTable.requestData();
		},
		saveCommand: function(){
		  $("#vid").val(vehicleIdList);
		  realTimeCommand.getCommandCheckedNodes();
		  $("#commandNodes").val(commandNodes);
		  if(!realTimeCommand.checkSubmit() || !realTimeCommand.checkGenerate()){
			  return;
		  };
		  if(realTimeCommand.validateSubmit()){
		  $("#saveCommandForm").ajaxSubmit(function() {
			  $("#reportParameters,.report-para-footer,#infoDemandMenu,#phoneBookSetting,#eventSetting,#terminalParameters,#terminalControl,#unlimitedUpgrade,#connectSpecifyServer,#locationReporting,#terminalSearch,#telephoneParameters,#videoCameraParameters,#gnssParameters,.report-para-footer-control,.report-para-footer-control-1").hide();
	        	 myTable.refresh()
	   		});
		  }
		},
		 // 显示错误提示信息
        showErrorMsg: function(msg, inputId){
            if ($("#error_label").is(":hidden")) {
                $("#error_label").text(msg);
                $("#error_label").insertAfter($("#" + inputId));
                $("#error_label").show();
            } else {
                $("#error_label").is(":hidden");
            }
        },
        hideErrorMsg: function(){
            $("#error_label").hide();
        },
        inputBlur: function(){
        	realTimeCommand.hideErrorMsg();
        },
		validateSubmit: function(){
			var commandTypes = commandNodes.split(",");
			for(var i =0;i<commandTypes.length;i++){
				var commandType = commandTypes[i];
				if(commandType =="19"){
					var events = $("input[id^='eventId']");
					var eventContents = $("input[id^='eventContent']");
					for(var j=0;j<events.length;j++){
						if(events[j].value==""){
							realTimeCommand.showErrorMsg(commandIncidentIdNull, "eventId_2");
							return false;
						}else{
							realTimeCommand.hideErrorMsg();
						}
						if(eventContents[j].value==""){
							realTimeCommand.showErrorMsg(commandIncidentDataNull, eventContents[j].id);
							return false;
						}else{
							realTimeCommand.hideErrorMsg();
						}
					}
				}else if(commandType =="20"){
					var events = $("input[id^='phoneBookId']");
					var eventContents = $("input[id^='phoneBookContact']");
					var phoneBookNumbers = $("input[id^='phoneBookNumber']");
					for(var j=0;j<events.length;j++){
						if(events[j].value==""){
							realTimeCommand.showErrorMsg(commandContactIdNull, "phoneBookId_2");
							return false;
						}else{
							realTimeCommand.hideErrorMsg();
						}
						if(eventContents[j].value==""){
							realTimeCommand.showErrorMsg(commandContactNameNull, eventContents[j].id);
							return false;
						}else{
							realTimeCommand.hideErrorMsg();
						}
						if(phoneBookNumbers[j].value==""){
							realTimeCommand.showErrorMsg(commandContactPhoneNull, phoneBookNumbers[j].id);
							return false;
						}else{
							realTimeCommand.hideErrorMsg();
						}
					}
				}else if(commandType =="21"){
					var events = $("input[id^='infoDemandId']");
					var eventContents = $("input[id^='infoDemandName']");
					for(var j=0;j<events.length;j++){
						if(events[j].value==""){
							realTimeCommand.showErrorMsg(commandMessageIdNull, event[j].id);
							return false;
						}else{
							realTimeCommand.hideErrorMsg();
						}
						if(eventContents[j].value==""){
							realTimeCommand.showErrorMsg(commandMessageNameNull, eventContents[j].id);
							return false;
						}else{
							realTimeCommand.hideErrorMsg();
						}
					}
				}else if(commandType =="17"){
					var videoCameraTimeInterval = $("#videoCameraTimeInterval").val();
					var videoCameraDistanceInterval = $("#videoCameraDistanceInterval").val();
					if(videoCameraTimeInterval==""){
						realTimeCommand.showErrorMsg(commandIntervalNull, "videoCameraTimeInterval");
						return false;
					}else{
						realTimeCommand.hideErrorMsg();
					}
					if(videoCameraDistanceInterval==""){
						realTimeCommand.showErrorMsg(commandDistanceNull, "videoCameraDistanceInterval");
						return false;
					}else{
						realTimeCommand.hideErrorMsg();
					}
				}
			}
			return true;
		},
		checkSubmit: function(){
			 var zTree = $.fn.zTree.getZTreeObj("treeDemo");
	          nodes = zTree.getCheckedNodes(true);
			  if(nodes.length==0){
				  layer.msg(commandVehicleNull);
				  return false;
			  }
			  return true;
		},
		checkGenerate:function(){
			var flag = false;
			var cZTree = $.fn.zTree.getZTreeObj("commandTreeDemo");
	          cNodes = cZTree.getCheckedNodes(true);
			  for (var i = 0, l = cNodes.length; i < l; i++) {
					if (cNodes[i].id != "13"&&cNodes[i].id != "15"&&cNodes[i].id != "1") {
						 flag = true;
					}
				}
			  if(!flag){
				  layer.msg(commandSettingValueNull);
				  return false;
			  }
			  return true;
		},
		//提交点击
		generateClick: function(){
			if(!realTimeCommand.checkSubmit()){
				  return;
			};
			var flag = false;
			var cZTree = $.fn.zTree.getZTreeObj("commandTreeDemo");
	        cNodes = cZTree.getCheckedNodes(true);
	        for (var i = 0, l = cNodes.length; i < l; i++) {
				if (cNodes[i].id == "13") {
						flag = true;
				}
			}
			if(!flag){
				layer.msg(commandDeviceNull);
				return false;
			}
        	var url="/clbs/v/monitoring/command/generateDeviceControl";
            var parameter={"vid": vehicleIdList};
            json_ajax("POST",url,"json",true,parameter, null);
            myTable.refresh();
		},
		generateDeviceSearch: function(){
			if(!realTimeCommand.checkSubmit()){
				 return;
			};
			var flag = false;
			var cZTree = $.fn.zTree.getZTreeObj("commandTreeDemo");
	        cNodes = cZTree.getCheckedNodes(true);
	        for (var i = 0, l = cNodes.length; i < l; i++) {
				if (cNodes[i].id == "15") {
					flag = true;
				}
			}
			if(!flag){
				layer.msg(commandDeviceNull);
				return false;
			}
        	var url="/clbs/v/monitoring/command/generateDeviceSearch";
            var parameter={"vid": vehicleIdList};
            json_ajax("POST",url,"json",true,parameter, null);
            myTable.refresh();
		},
		//下发参数 （单个）
	    sendOne: function(id,paramId,vehicleId,commandType,dId){
	    	 var arr = [];
	    	 var obj = {};
	         obj.id = id;
	         obj.paramId = paramId;
	         obj.vehicleId = vehicleId;
	         obj.type = commandType;
	         obj.dId = dId;
	         arr.push(obj);
	         var jsonStr = JSON.stringify(arr);
	         realTimeCommand.sendCommand(jsonStr);
	    },
	    // 下发参数
		sendCommand: function(sendParam){
			 layer.load(2);
			 $.ajax({
	             type: "POST",
	             url: "/clbs/v/monitoring/command/sendParam",
	             data: {
	                 "sendParam": sendParam
	             },
	             dataType: "json",
	             global : true,
	             success: function (data) {
	             	if(realsendflag){
                        webSocket.subscribe(headers, '/topic/fencestatus', realTimeCommand.updataFenceData,null, null);
                        realsendflag=false;
                    }
	            	 if(data.success){
	            		 layer.closeAll('loading');
		            	 layer.msg(publicIssuedSuccess,{closeBtn: 0}, function(refresh){
		            		 myTable.refresh();
		            		 layer.close(refresh);
		            		}); 
	            	 }else{
	            		 layer.msg(data.msg,{move:false});
	            	 }
	             }
			 })
		},
		// 批量下发
		sendBatch: function(){
	        //判断是否至少选择一项
	        var chechedNum = $("input[name='subChk']:checked").length;
	        if (chechedNum == 0) {
	            layer.msg(commandSelectNull);
	            return
	        }
	        var checkedList = new Array();
	        $("input[name='subChk']:checked").each(function() {
	        	var jsonStr = $(this).val();
	        	var jsonObj = $.parseJSON(jsonStr);
	            checkedList.push(jsonObj);
	        });
	        // 下发
	        realTimeCommand.sendCommand(JSON.stringify(checkedList));
		},
		ajaxQueryDataFilter: function(treeId, parentNode, responseData) {
        	responseData = JSON.parse(ungzip(responseData));
        	var list = [];
        	if (vehicleIdList != null && vehicleIdList != undefined && vehicleIdList != ""){
        		var str=(vehicleIdList.slice(vehicleIdList.length-1)==',')?vehicleIdList.slice(0,-1):vehicleIdList;
        		list = str.split(",");
        	}
            return filterQueryResult(responseData,list);
    	},
		 //模糊查询
        inputTextAutoSearch: function(param){
//        	search_ztree('treeDemo', 'search_condition', 'vehicle');
        	if (param != null && param != undefined && param != '') {
        		var setQueryChar = {
                        async: {
                            url: "/clbs/m/personalized/ico/vehicleTreeFuzzy",
                            type: "post",
                            enable: true,
                            autoParam: ["id"],
                            dataType: "json",
                            otherParam: {"type": "multiple","queryParam":param, "deviceType":"1"},
                            dataFilter: realTimeCommand.ajaxQueryDataFilter
                        },
                        check: {
                            enable: true,
                            chkStyle: "checkbox",
                            chkboxType: {
                                "Y": "s",
                                "N": "s"
                            },
                            radioType: "all"
                        },
                        view: {
                            dblClickExpand: false,
                            nameIsHTML: true,
                            fontCss: setFontCss_ztree,
                            countClass: "group-number-statistics"
                        },
                        data: {
                            simpleData: {
                                enable: true
                            }
                        },
                        callback: {
                        	beforeClick:realTimeCommand.beforeClick,
                            onClick: realTimeCommand.onClickVehicle,
                            onAsyncSuccess: realTimeCommand.fuzzyZTreeOnAsyncSuccess,
                            //beforeCheck: realTimeCommand.fuzzyZTreeBeforeCheck,
                            onCheck: realTimeCommand.fuzzyOnCheckVehicle,
                            //onExpand: realTimeCommand.zTreeOnExpand,
                            //onNodeCreated: realTimeCommand.zTreeOnNodeCreated,
                        }
                    };
        		$.fn.zTree.init($("#treeDemo"), setQueryChar, null);
        	}else{
        		realTimeCommand.initTreeData("1");
        	}
        	
        },
        /**
         * 选中已选的节点
         */
        checkCurrentNodes : function (treeNode){
        	 var crrentSubV = vehicleIdList.split(",");
        	 var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
        	 if (treeNode != undefined && treeNode != null && treeNode.type === "assignment" && treeNode.children != undefined) {
        		 var list = treeNode.children;
        		 if (list != null && list.length > 0) {
                   	 for (var j=0; j<list.length; j++){
                   		 var znode = list[j];
                   		 if (crrentSubV != null && crrentSubV != undefined && crrentSubV.length !== 0 && $.inArray(znode.id, crrentSubV) != -1){
                   			treeObj.checkNode(znode, true, true);
                   		 }
                   	 }
                 }
        	 }
         },
         
         fuzzyZTreeOnAsyncSuccess : function (event, treeId, treeNode) {
         	var zTree = $.fn.zTree.getZTreeObj("treeDemo");
         	zTree.expandAll(true);
         },
         fuzzyZTreeBeforeCheck : function(treeId, treeNode){
         	var flag = true;
         	if (!treeNode.checked) {
                 var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                     .getCheckedNodes(true), v = "";
                 var nodesLength = 0;
                 for (var i=0;i<nodes.length;i++) {
                     if(nodes[i].type == "people" || nodes[i].type == "vehicle"){
                         nodesLength += 1;
                     }
                 }
                 if (treeNode.type == "group" || treeNode.type == "assignment"){ // 判断若勾选节点数大于5000，提示
                     var zTree = $.fn.zTree.getZTreeObj("treeDemo")
                     json_ajax("post", "/clbs/a/search/monitorTreeFuzzyCount",
                         "json", false, {"type": "multiple","queryParam":fuzzyParam}, function (data) {
                             nodesLength += data;
                         })
                 } else if (treeNode.type == "people" || treeNode.type == "vehicle"){
                     nodesLength += 1;
                 }
                 if(nodesLength > 5000){
                     layer.msg(treeMaxLength5000);
                     flag = false;
                 }
             }
         	return flag;
         },
         fuzzyOnCheckVehicle : function(e, treeId, treeNode) {
         	//获取树结构
         	var zTree = $.fn.zTree.getZTreeObj("treeDemo");
         	//获取勾选状态改变的节点
         	var changeNodes = zTree.getChangeCheckedNodes();
         	if(treeNode.checked){ //若是取消勾选事件则不触发5000判断
                 var checkedNodes = zTree.getCheckedNodes(true);
                 var nodesLength = 0;
                 //
                 for (var i=0;i<checkedNodes.length;i++) {
                     if(checkedNodes[i].type == "people" || checkedNodes[i].type == "vehicle"){
                         nodesLength += 1;
                     }
                 }
                 
                 if(nodesLength > 5000){
                 	//zTree.checkNode(treeNode,false,true);
                 	layer.msg(treeMaxLength5000);
                 	for (var i=0;i<changeNodes.length;i++) {
                 		changeNodes[i].checked = false;
                 		zTree.updateNode(changeNodes[i]);
                 	}
                 }
         	}
         	//获取勾选状态被改变的节点并改变其原来勾选状态（用于5000准确校验）
             for(var i=0;i<changeNodes.length;i++){
             	changeNodes[i].checkedOld = changeNodes[i].checked;
             }
         },
         
        connectionControlSH: function(){
        	setTimeout(function(){
        		if(!($("#connectSpecifyServer").is(":hidden"))){
                	if($("#specifyServerConnect").val() == 1){
                		$("#specifyServerDial,#specifyServerDialName,#specifyServerDialPwd,#specifyServerAddress,#specifyServerTcpPort,#specifyServerUdpPort,#specifyServerTimeLimit").attr("readonly","readonly");
                	}else{
                		$("#specifyServerDial,#specifyServerDialName,#specifyServerDialPwd,#specifyServerAddress,#specifyServerTcpPort,#specifyServerUdpPort,#specifyServerTimeLimit").removeAttr("readonly");
                	}
        		}
        		if(!($("#locationReporting").is(":hidden"))){
                	if($("#locationTactics").val() == 0){
            			$("#locationDefaultDistance,#locationSleepDistance,#locationAlarmDistance,#locationNoLoginDistance").attr("readonly","readonly");
            			$("#locationDefaultTime,#locationSleep,#locationAlarmTime,#locationNoLogin").removeAttr("readonly");
            		}else if($("#locationTactics").val() == 1){
            			$("#locationDefaultTime,#locationSleep,#locationAlarmTime,#locationNoLogin").attr("readonly","readonly");
            			$("#locationDefaultDistance,#locationSleepDistance,#locationAlarmDistance,#locationNoLoginDistance").removeAttr("readonly");
            		}else if($("#locationTactics").val() == 2){
            			$("#locationDefaultTime,#locationSleep,#locationAlarmTime,#locationNoLogin").removeAttr("readonly");
            			$("#locationDefaultDistance,#locationSleepDistance,#locationAlarmDistance,#locationNoLoginDistance").removeAttr("readonly");
            		}
        		}
        	},100);
        },
		//事件设置添加事件
		addEventSetting: function(){
			addEventIndex++;
			var html = "<div class='form-group' id='eventMain-container_"+ addEventIndex +"'><label class='col-md-3 control-label'>事件ID：</label><div class='col-md-3'><input type='text' name='eventId' id='eventId_"+ addEventIndex +"' value='"+ (addEventIndex-1) +"' class='form-control' onblur='realTimeCommand.inputBlur()'></div><label class='col-md-2 control-label'>事件内容：</label><div class='col-md-3'><input type='text' name='eventContent' id='eventContent_"+ addEventIndex +"' placeholder='请输入事件内容' class='form-control' onblur='realTimeCommand.inputBlur()'></div><div class='col-md-1'><button type='button' class='btn btn-danger eventSettingDelete deleteIcon'><span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button></div></div>";
			$("#eventMain-container").append(html);
			$(".eventSettingDelete").on("click",function(){
				$(this).parent().parent().remove();
			});
		},
		//事件设置添加事件(id为下拉时)
		addEventSettingIsSelect: function(){
			addEventIndex++;
			var html = "<div class='form-group' id='eventMain-container_"+ addEventIndex +"'><label class='col-md-3 control-label'>事件ID：</label><div class='col-md-3'><select id='eventId_"+ addEventIndex +"' name='eventId' class='form-control' onchange='realTimeCommand.eventSettIdParFn("+ addEventIndex +")'><option value='1'>1</option><option value='2'>2</option><option value='3'>3</option></select></div><label class='col-md-2 control-label'>事件内容：</label><div class='col-md-3'><input type='text' name='eventContent' id='eventContent_"+ addEventIndex +"' placeholder='请输入事件内容' class='form-control' onblur='realTimeCommand.inputBlur()'></div><div class='col-md-1'><button type='button' class='btn btn-danger eventSettingDelete deleteIcon'><span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button></div></div>";
			$("#eventMain-container").append(html);
			$(".eventSettingDelete").on("click",function(){
				$(this).parent().parent().remove();
			});
		},
		//电话本设置添加事件
		addPhoneBookEvent: function(){
			addPhoneIndex++;
			var html = "<div id='phoneBook-MainContent_"+ addPhoneIndex +"'><div class='form-group'><label class='col-md-3 control-label'>联系人ID：</label><div class='col-md-3 phoneBookIdInfo'><input type='text' id='phoneBookId_"+ addPhoneIndex +"' name='phoneBookId' value='"+ (addPhoneIndex-1) +"' class='form-control' onblur='realTimeCommand.inputBlur()'/></div><label class='col-md-2 control-label'>联系人：</label><div class='col-md-3'><input type='text' id='phoneBookContact_"+ addPhoneIndex +"' name='contact' placeholder='请输入联系人' class='form-control' onblur='realTimeCommand.inputBlur()'/></div></div><div class='form-group'><label class='col-md-3 control-label'>电话号码：</label><div class='col-md-3'><input type='text' id='phoneBookNumber_"+ addPhoneIndex +"' name='phoneNo' placeholder='请输入电话号码' class='form-control' onblur='realTimeCommand.inputBlur()' /></div><label class='col-md-2 control-label'>呼叫类型：</label><div class='col-md-3'><select id='phoneBookOperateType_"+ addPhoneIndex +"' name='callType' class='form-control'><option value='1'>呼入</option><option value='2'>呼出</option><option value='3'>呼入/呼出</option></select></div><div class='col-md-1'><button type='button' class='btn btn-danger phoneBookDelete deleteIcon'><span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button></div></div></div>";
			$("#phoneBook-MainContent").append(html);
			$(".phoneBookDelete").on("click",function(){
				$(this).parent().parent().parent().remove();
			});
		},
		//电话本设置添加事件(联系人ID为下拉时)
		addPhoneBookEventIsSelect: function(){
			addPhoneIndex++;
			var html = "<div id='phoneBook-MainContent_"+ addPhoneIndex +"'><div class='form-group'><label class='col-md-3 control-label'>联系人ID：</label><div class='col-md-3 phoneBookIdInfo'><select id='phoneBookId_"+ addPhoneIndex +"' name='phoneBookId' class='form-control' onchange='realTimeCommand.phoneBookSettIdParFn("+ addPhoneIndex +")'><option value='1'>1</option><option value='2'>2</option><option value='3'>3</option></select></div><label class='col-md-2 control-label'>联系人：</label><div class='col-md-3'><input type='text' id='phoneBookContact_"+ addPhoneIndex +"' name='contact' placeholder='请输入联系人' class='form-control' onblur='realTimeCommand.inputBlur()'/></div></div><div class='form-group'><label class='col-md-3 control-label'>电话号码：</label><div class='col-md-3'><input type='text' id='phoneBookNumber_"+ addPhoneIndex +"' name='phoneNo' placeholder='请输入电话号码' class='form-control' onblur='realTimeCommand.inputBlur()' /></div><label class='col-md-2 control-label'>呼叫类型：</label><div class='col-md-3'><select id='phoneBookOperateType_"+ addPhoneIndex +"' name='callType' class='form-control'><option value='1'>呼入</option><option value='2'>呼出</option><option value='3'>呼入/呼出</option></select></div><div class='col-md-1'><button type='button' class='btn btn-danger phoneBookDelete deleteIcon'><span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button></div></div></div>";
			$("#phoneBook-MainContent").append(html);
			$(".phoneBookDelete").on("click",function(){
				$(this).parent().parent().parent().remove();
			});
		},
		//信息点播菜单添加事件
		addInfoDemandEvent: function(){
			addInfoDemandIndex++;
			var html = "<div class='form-group' id='infoDemand-MainContent_"+ addInfoDemandIndex +"'><label class='col-md-3 control-label'>信息ID：</label><div class='col-md-3'><input type='text' id='infoDemandId_"+ addInfoDemandIndex +"' value='"+ (addInfoDemandIndex-1) +"'  name='infoId' class='form-control' onblur='realTimeCommand.inputBlur()'/></div><label class='col-md-2 control-label'>信息名称：</label><div class='col-md-3'><input type='text' id='infoDemandName"+ addInfoDemandIndex +"'  name='infoContent' placeholder='请输入信息名称' class='form-control' onblur='realTimeCommand.inputBlur()'/></div><div class='col-md-1'><button type='button' class='btn btn-danger infoDemandDelete deleteIcon'><span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button></div></div>";
			$("#infoDemandMenu").append(html);
			$(".infoDemandDelete").on("click",function(){
				$(this).parent().parent().remove();
			});
		},
		//信息点播菜单添加事件(信息ID为下拉时)
		addInfoDemandEventIsSelect: function(){
			addInfoDemandIndex++;
			var html = "<div class='form-group' id='infoDemand-MainContent_"+ addInfoDemandIndex +"'><label class='col-md-3 control-label'>信息ID：</label><div class='col-md-3'><select id='infoDemandId_"+ addInfoDemandIndex +"' name='infoId' class='form-control' onchange='realTimeCommand.infoDemandSettIdParFn("+ addInfoDemandIndex +")'><option value='1'>1</option><option value='2'>2</option><option value='3'>3</option></select></div><label class='col-md-2 control-label'>信息名称：</label><div class='col-md-3'><input type='text' id='infoDemandName"+ addInfoDemandIndex +"'  name='infoContent' placeholder='请输入信息名称' class='form-control' onblur='realTimeCommand.inputBlur()'/></div><div class='col-md-1'><button type='button' class='btn btn-danger infoDemandDelete deleteIcon'><span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button></div></div>";
			$("#infoDemandMenu").append(html);
			$(".infoDemandDelete").on("click",function(){
				$(this).parent().parent().remove();
			});
		},
		//基站参数设置 定点时间添加
		addBaseStationEvent: function(){
			var bsfpLength = $("#baseStation-MainContent").find("div.form-group").length;
			var bs = parseInt(bsfpLength) + 1;
			if(bs > 12){
				layer.msg(commandDesignatedTimeError);
			}else{
				addBaseStationIndex++;
				var html = "<div class='form-group'><label class='col-md-3 control-label'>定点时间：</label><div class='col-md-3'><input type='text' id='baseStationFixedTime_"+addBaseStationIndex+"' name='baseStationFixedTime' onclick='' class='form-control'/></div><div class='col-md-1'><button type='button' class='btn btn-danger baseStationDelete deleteIcon'><span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button></div><div class='col-md-5'></div></div>";
				$("#baseStation-MainContent").append(html);
		        laydate.render({elem: '#baseStationFixedTime_'+addBaseStationIndex,type: 'time',theme: '#6dcff6'});
		        $("#baseStationFixedTime_"+addBaseStationIndex).val(loadTime);
				$(".baseStationDelete").on("click",function(){
					$(this).parent().parent().remove();
				});
			}
		},
        //事件设置  操作类型
        eventSettingOperateType: function(){
        	var eventOperateTypeValue = $("#eventOperateType").find("option:selected").val();
        	//更新 追加
        	if(eventOperateTypeValue == 1 || eventOperateTypeValue == 2){
        		realTimeCommand.resetEventSetting();
        		var emcLength = $("#eventMain-container").children("div").length;
        		if(emcLength > 1){
        			$("#eventMain-container>div.form-group").each(function(i){
        				if(i>0){$(this).remove();}
        			});
        		}
        		$(".eventIdInfo").find("input").removeAttr("disabled","disabled");
        		$("#event-add-btn").removeAttr("disabled","disabled");
        		$("#eventContent_2").val("");
        	}
        	//修改 id为下拉
        	else if(eventOperateTypeValue == 3){
        		realTimeCommand.eventSettingUpdateOrDel();
        	}
        	//删除 id为下拉
        	else if(eventOperateTypeValue == 4){
        		realTimeCommand.eventSettingUpdateOrDel();
        	}else{
        		realTimeCommand.resetEventSetting();
        		$(".eventIdInfo").find("input").removeAttr("disabled","disabled");
        		$("#event-add-btn").removeAttr("disabled","disabled");
        	}
        },
        //事件设置  修改删除时调用
        eventSettingUpdateOrDel: function(){
        	addEventIndex = 2;
        	$("#eventMain-container").html("");
    		var html = 
    			"<div class='form-group'>"+
					"<label class='col-md-3 control-label'>事件ID：</label>"+
					"<div class='col-md-3 eventIdInfo'>"+
					"<select id='eventId_2' name='eventId' class='form-control' onchange='realTimeCommand.eventSettIdFn()'>"+
						"<option value='1'>1</option>"+
						"<option value='2'>2</option>"+
						"<option value='3'>3</option>"+
					"</select>"+
					"</div>"+
					"<label class='col-md-2 control-label'>事件内容：</label>"+
					"<div class='col-md-3'><input type='text' id='eventContent_2' name='eventContent' placeholder='请输入事件内容' class='form-control' onblur='realTimeCommand.inputBlur()'></div>"+
					"<div class='col-md-1'><button id='event-add-btn' onclick='realTimeCommand.addEventSettingIsSelect();' type='button' class='btn btn-primary addIcon'><span class='glyphicon glyphiconPlus' aria-hidden='true'></span></button></div>"+
				"</div>";
    		$("#eventMain-container").append(html);
        },
        //事件设置 操作类型其他选项还原
        resetEventSetting: function(){
    		$("#eventMain-container").html("");
    		var html = 
    			"<div class='form-group'>"+
					"<label class='col-md-3 control-label'>事件ID：</label>"+
					"<div class='col-md-3 eventIdInfo'>"+
					"<input id='eventId_2' type='text' name='eventId' value='1' class='form-control' onblur='realTimeCommand.inputBlur();'/>"+
					"</div>"+
					"<label class='col-md-2 control-label'>事件内容：</label>"+
					"<div class='col-md-3'><input type='text' id='eventContent_2' name='eventContent' placeholder='请输入事件内容' class='form-control' onblur='realTimeCommand.inputBlur()'></div>"+
					"<div class='col-md-1'><button id='event-add-btn' onclick='realTimeCommand.addEventSetting();' type='button' class='btn btn-primary addIcon'><span class='glyphicon glyphiconPlus' aria-hidden='true'></span></button></div>"+
				"</div>";
    		$("#eventMain-container").append(html);
        },
        //事件设置  事件ID选择改变
        eventSettIdFn: function(){
        	var eIdVal = $("#eventId_2").find("option:selected").val();
        	layer.msg(eIdVal);
        },
        //事件设置  事件ID选择改变(带参数)
        eventSettIdParFn: function(id){
        	var eIdVal = $("#eventId_"+id).find("option:selected").val();
        	layer.msg(eIdVal);
        },
        //电话本设置 操作类型
        phoneBookSettingOperateType: function(){
        	var phoneBookOperateTypeValue = $("#phoneBookOperateType").find("option:selected").val();
        	//更新  追加
        	if(phoneBookOperateTypeValue == 1 || phoneBookOperateTypeValue == 2){
        		var pbmLength = $("#phoneBook-MainContent").children("div").length;
        		if(pbmLength > 2){
        			$("#phoneBook-MainContent>div").each(function(j){
        				if(j>1){
        					$(this).remove();
        				}
        			});
        		}
        		realTimeCommand.resetPhoneBookSetting();
        		$("#phoneBook-add-btn").removeAttr("disabled","disabled");
        		$("#phoneBookContact_2,#phoneBookNumber_2").val("");
        	}
        	//修改 联系人ID下拉
        	else if(phoneBookOperateTypeValue == 3){
        		$("#phoneBook-MainContent").html("");
        		var html = 
        		"<div class='form-group'>"+
					"<label class='col-md-3 control-label'>联系人ID：</label>"+
					"<div class='col-md-3 phoneBookIdInfo'>"+
						"<select id='phoneBookId_2' name='phoneBookId' class='form-control' onchange='realTimeCommand.phoneBookSettIdFn()'>"+
							"<option value='1'>1</option>"+
							"<option value='2'>2</option>"+
							"<option value='3'>3</option>"+
						"</select>"+
					"</div>"+
					"<label class='col-md-2 control-label'>联系人：</label>"+
					"<div class='col-md-3'>"+
						"<input type='text' id='phoneBookContact_2' name='contact' placeholder='请输入联系人' class='form-control' onblur='realTimeCommand.inputBlur();'/>"+													
					"</div>"+
				"</div>"+
				"<div class='form-group'>"+
					"<label class='col-md-3 control-label'>电话号码：</label>"+
					"<div class='col-md-3'>"+
						"<input type='text' id='phoneBookNumber_2' name='phoneNo' placeholder='请输入电话号码' class='form-control' onblur='realTimeCommand.inputBlur();'/>"+													
					"</div>"+
					"<label class='col-md-2 control-label'>呼叫类型：</label>"+
					"<div class='col-md-3'>"+
						"<select id='phoneBookOperateType_2' name='callType' class='form-control'>"+
							"<option value='1'>呼入</option>"+
							"<option value='2'>呼出</option>"+
							"<option value='3'>呼入/呼出</option>"+
						"</select>"+
					"</div>"+
					"<div class='col-md-1'>"+
						"<button id='phoneBook-add-btn' type='button' class='btn btn-primary addIcon' onclick='realTimeCommand.addPhoneBookEventIsSelect()'>"+
							"<span class='glyphicon glyphiconPlus' aria-hidden='true'></span>"+
						"</button>"+
					"</div>"+
				"</div>";
        		$("#phoneBook-MainContent").append(html);
        	}
        	//其他选项
        	else{
        		realTimeCommand.resetPhoneBookSetting();
        		$(".phoneBookIdInfo").find("input").removeAttr("disabled","disabled");
        		$("#phoneBook-add-btn").removeAttr("disabled","disabled");
        	}
        },
        //电话本设置 操作类型其他选项还原
        resetPhoneBookSetting: function(){
        	$("#phoneBook-MainContent").html("");
    		var html = 
    		"<div class='form-group'>"+
				"<label class='col-md-3 control-label'>联系人ID：</label>"+
				"<div class='col-md-3 phoneBookIdInfo'>"+
					"<input type='text' id='phoneBookId_2' name='phoneBookId' value='1' class='form-control' onblur='realTimeCommand.inputBlur();'/>	"+													
				"</div>"+
				"<label class='col-md-2 control-label'>联系人：</label>"+
				"<div class='col-md-3'>"+
					"<input type='text' id='phoneBookContact_2' name='contact' placeholder='请输入联系人' class='form-control' onblur='realTimeCommand.inputBlur();'/>	"+													
				"</div>"+
			"</div>"+
			"<div class='form-group'>"+
				"<label class='col-md-3 control-label'>电话号码：</label>"+
				"<div class='col-md-3'>"+
					"<input type='text' id='phoneBookNumber_2' name='phoneNo' placeholder='请输入电话号码' class='form-control' onblur='realTimeCommand.inputBlur();'/>"+												
				"</div>"+
				"<label class='col-md-2 control-label'>呼叫类型：</label>"+
				"<div class='col-md-3'>"+
					"<select id='phoneBookOperateType_2' name='callType' class='form-control'>"+
						"<option value='1'>呼入</option>"+
						"<option value='2'>呼出</option>"+
						"<option value='3'>呼入/呼出</option>"+
					"</select>"+
				"</div>"+
				"<div class='col-md-1'>"+
					"<button id='phoneBook-add-btn' type='button' class='btn btn-primary addIcon' onclick='realTimeCommand.addPhoneBookEvent()'>"+
						"<span class='glyphicon glyphiconPlus' aria-hidden='true'></span>"+
					"</button>"+
				"</div>"+
			"</div>";
    		$("#phoneBook-MainContent").append(html);
        },
        //电话本设置 联系人ID改变
        phoneBookSettIdFn: function(){
        	var pbIdVal = $("#phoneBookId_2").find("option:selected").val();
        	layer.msg("联系人ID-"+pbIdVal);
        },
        //电话本设置 联系人ID改变(带参数)
        phoneBookSettIdParFn: function(id){
        	var pbIdVal = $("#phoneBookId_"+id).find("option:selected").val();
        	layer.msg("联系人ID-"+pbIdVal);
        },
        //信息点播菜单 操作类型
        infoDemandMenuSettingOperateType: function(){
        	var infoDemandOperateTypeValue = $("#infoDemandOperateType").find("option:selected").val();
        	//更新 追加
        	if(infoDemandOperateTypeValue == 1 || infoDemandOperateTypeValue == 2){
        		var iddLength = $("#infoDemandMenu").children("div").length;
        		if(iddLength>3){
        			$("#infoDemandMenu>div").each(function(k){
        				if(k>2){
        					$(this).remove();
        				}
        			});
        		}
        		realTimeCommand.resetInfoDemandMenuSetting();
        		$("#infoDemand-add-btn").removeAttr("disabled","disabled");
        		$("#infoDemandName_2").val("");
        	}
        	//修改 信息id下拉
        	else if(infoDemandOperateTypeValue == 3){
        		var idmLength = $("#infoDemandMenu").find("div.form-group").length;
        		if(idmLength > 2){
        			$("#infoDemandMenu>div").each(function(k){
        				if(k > 1){
        					$(this).remove();
        				}
        			});
					var html = 
					"<div class='form-group' id='infoDemand-MainContent'>"+
						"<label class='col-md-3 control-label'>信息ID：</label>"+
						"<div class='col-md-3 infoDemandMenuId'>"+
							"<select id='infoDemandId_2' name='infoId' class='form-control' onchange='realTimeCommand.infoDemandSettIdFn()'>"+
								"<option value='1'>1</option>"+
								"<option value='2'>2</option>"+
								"<option value='3'>3</option>"+
							"</select>"+
						"</div>"+
						"<label class='col-md-2 control-label'>信息名称：</label>"+
						"<div class='col-md-3'>"+
							"<input type='text' id='infoDemandName_2' name='infoContent' placeholder='请输入信息名称' class='form-control'  onblur='realTimeCommand.inputBlur();'/>"+													
						"</div>"+
						"<div class='col-md-1'>"+
							"<button id='infoDemand-add-btn' type='button' class='btn btn-primary addIcon' onclick='realTimeCommand.addInfoDemandEventIsSelect()'>"+
								"<span class='glyphicon glyphiconPlus' aria-hidden='true'></span>"+
							"</button>"+
						"</div>"+
					"</div>";
					$("#infoDemandMenu").append(html);
        		}
        	}else{
        		realTimeCommand.resetInfoDemandMenuSetting();
        		$(".infoDemandMenuId").find("input").removeAttr("disabled","disabled");
        		$("#infoDemand-add-btn").removeAttr("disabled","disabled");
        	}
        },
        //信息点播菜单  操作类型其他选项还原
        resetInfoDemandMenuSetting: function(){
        	var idmLength = $("#infoDemandMenu").find("div.form-group").length;
    		if(idmLength > 2){
    			$("#infoDemandMenu>div").each(function(k){
    				if(k > 1){
    					$(this).remove();
    				}
    			});
				var html = 
				"<div class='form-group' id='infoDemand-MainContent'>"+
					"<label class='col-md-3 control-label'>信息ID：</label>"+
					"<div class='col-md-3 infoDemandMenuId'>"+
						"<input type='text' id='infoDemandId_2' name='infoId' value='1' class='form-control' onblur='realTimeCommand.inputBlur();'/>"+
					"</div>"+
					"<label class='col-md-2 control-label'>信息名称：</label>"+
					"<div class='col-md-3'>"+
						"<input type='text' id='infoDemandName_2' name='infoContent' placeholder='请输入信息名称' class='form-control'  onblur='realTimeCommand.inputBlur();'/>"+													
					"</div>"+
					"<div class='col-md-1'>"+
						"<button id='infoDemand-add-btn' type='button' class='btn btn-primary addIcon' onclick='realTimeCommand.addInfoDemandEvent()'>"+
							"<span class='glyphicon glyphiconPlus' aria-hidden='true'></span>"+
						"</button>"+
					"</div>"+
				"</div>";
				$("#infoDemandMenu").append(html);
    		}
        },
        //信息点播菜单 信息ID改变
        infoDemandSettIdFn: function(){
        	var idIdVal = $("#infoDemandId_2").find("option:selected").val();
        	layer.msg("信息ID-"+idIdVal);
        },
        //信息点播菜单 信息ID改变(带参数)
        infoDemandSettIdParFn: function(id){
        	var idIdVal = $("#infoDemandId_"+id).find("option:selected").val();
        	layer.msg("信息ID-"+idIdVal);
        },
        //基站参数设置 上报模式
        baseStationReportModeCheckFn: function(){
        	var baseStationReportModeTypeValue = $("#baseStationReportMode").find("option:selected").val();
        	if(baseStationReportModeTypeValue == 0){
        		$("#baseStationStartTimePoint,#baseStationReportInterval").removeAttr("disabled","disabled");
        		$("#baseStationFixedTime").attr("disabled","disabled");
        		$("#baseStation-add-btn").hide();
        	}else if(baseStationReportModeTypeValue == 1){
        		$("#baseStationFixedTime").removeAttr("disabled","disabled");
        		$("#baseStation-add-btn").show();
        		$("#baseStationStartTimePoint,#baseStationReportInterval").attr("disabled","disabled");
        	}
        },
        // 应答
        responseSocket: function() {
        	realTimeCommand.isGetSocketLayout();
        },
        isGetSocketLayout: function() {
        	setTimeout(function(){
        		if (webSocket.conFlag) {
        			webSocket.subscribe(headers, '/user/' + $("#userName").text() + '/check', realTimeCommand.updateTable, "/app/vehicle/inspect", null);
        		} else {
        			realTimeCommand.isGetSocketLayout();
        		}
        	}, 2000);
        },
        // 应答socket回掉函数
        updateTable: function(msg) {
        	if (msg != null) {
        		var json = $.parseJSON(msg.body);
                var msgData = json.data;
                if (msgData != undefined) {
                	var msgId = msgData.msgHead.msgID;
                    // if (msgId == 0x9300) {
                    //     var dataType = msgData.msgBody.dataType;
                    //     $("#msgDataType").val(dataType);
                    //     $("#infoId").val(msgData.msgBody.data.infoId);
                    //     $("#objectType").val(msgData.msgBody.data.objectType);
                    //     $("#objectId").val(msgData.msgBody.data.objectId);
                    //     $("#question").text(msgData.msgBody.data.infoContent);
                    //     if (dataType == 0x9301) {
                    //         $("#answer").val("");
                    //         $("#msgTitle").text("平台查岗");
                    //         $("#goTraceResponse").modal('show');
                    //         $("#error_label").hide();
                    //     }
                    //     if (dataType == 0x9302) {
                    //         $("#answer").val("");
                    //         $("#msgTitle").text("下发平台间报文");
                    //         $("#goTraceResponse").modal('show');
                    //     }
                    // }
                }
        	}
        },
        // 应答确定
        platformMsgAck: function() {
            var answer = $("#answer").val();
            if (answer == "") {
            	realTimeCommand.showErrorMsg("应答不能为空", "answer");
                return;
            } 
            $("#goTraceResponse").modal('hide');
            var msgDataType = $("#msgDataType").val();
            var infoId = $("#infoId").val();
            var objectType = $("#objectType").val();
            var objectId = $("#objectId").val();
            var url = "/clbs/m/connectionparamsset/platformMsgAck";
            json_ajax("POST", url, "json", false, {
                "infoId": infoId,
                "answer": answer,
                "msgDataType": msgDataType,
                "objectType": objectType,
                "objectId": objectId
            });
        },
//        showErrorMsg: function(msg, inputId) {
//    	    if ($("#error_label").is(":hidden")) {
//    	        $("#error_label").text(msg);
//    	        $("#error_label").insertAfter($("#" + inputId));
//    	        $("#error_label").show();
//    	    } else {
//    	        $("#error_label").is(":hidden");
//    	    }
//    	}
	}
	$(function(){
		$('input').inputClear().on('onClearEvent',function(e,data){
			var id = data.id;
			if(id == 'search_condition'){
//				search_ztree('treeDemo', id, 'vehicle');
				realTimeCommand.initTreeData("1");
			};
		});
		realTimeCommand.init();
		realTimeCommand.getHoursMinuteSeconds();
		realTimeCommand.initTable();
		realTimeCommand.responseSocket();
		$("#generateListBtn").on("click",realTimeCommand.generateClick);
		$("#event-add-btn").on("click",realTimeCommand.addEventSetting);
		$("#phoneBook-add-btn").on("click",realTimeCommand.addPhoneBookEvent);
		$("#infoDemand-add-btn").on("click",realTimeCommand.addInfoDemandEvent);
		$("#baseStation-add-btn").on("click",realTimeCommand.addBaseStationEvent);
		$("#checkAll").bind("click",realTimeCommand.checkAllClick);
		subChk.bind("click",realTimeCommand.subChk);
        $("#jiaoTong_f3_standby").bind("click",realTimeCommand.getTree);
		$("#del_model").bind("click",realTimeCommand.delModel);
		$("#refreshTable").bind("click",realTimeCommand.refreshTable);
		$("#saveCommand").bind("click",realTimeCommand.saveCommand);
		$("#generateDeviceSearch").on("click",realTimeCommand.generateDeviceSearch);
		// 批量下发
	    $("#send_model").bind("click",realTimeCommand.sendBatch);
	    //自动模糊查询
//        $("#search_condition").on('input oninput',realTimeCommand.inputTextAutoSearch);
	    // 树结构模糊搜索
    	var inputChange;
    	$("#search_condition").on('input propertychange', function(value){
    		if (inputChange !== undefined) {
    			clearTimeout(inputChange);
    		};
    		inputChange = setTimeout(function(){
    			// search
    			var param = $("#search_condition").val();
    			if(param == ''){
    				realTimeCommand.initTreeData("1");
    			}else{
    				realTimeCommand.inputTextAutoSearch(param);
    			}
    		}, 500);
    	});
    	// 滚动展开
		$("#treeDemo").scroll(function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTreeScroll(zTree, this);
        });
        //IE9
        if(navigator.appName=="Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g,"")=="MSIE9.0") {
            var search;
            $("#search_condition").bind("focus",function(){
                search = setInterval(function(){
//                    search_ztree('treeDemo', 'search_condition','vehicle');
                	realTimeCommand.inputTextAutoSearch(param);
                },500);
            }).bind("blur",function(){
                clearInterval(search);
            });
        }
        laydate.render({elem: '#baseStationStartTimePoint',type: 'time',theme: '#6dcff6'});
        laydate.render({elem: '#baseStationFixedTime',type: 'time',theme: '#6dcff6'});
        // 应答确定
        $('#parametersResponse').on('click', realTimeCommand.platformMsgAck);
	})
// })(window,$)