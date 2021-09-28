//# sourceURL=broadCastLoad.js

(function(window,$){
	var size;//当前权限监控对象数量
	var searchTimeout; // 模糊查询定时器
	var vehicleIds;// 选中的车辆ID
	var vehicles=null;
	var connectedVehicles = null;
	var promiseIds = [];
	var sendParamData = [];
	var broadCastSocket = {}; // 广播socket对象
    var subscribeSuccessData = []; // 订阅成功后的监控对象集合
    window.broadCastLoad = {
		init: function(){
			//菜单隐藏
			$('#rMenu').css({"visibility": "hidden"});
			//模态框添加类样式
			$(".modal-body").addClass("modal-body-overflow");
			$(".modal-dialog").css("width","40%");
			var url = '/clbs/m/basicinfo/monitoring/vehicle/treeStateInfo';
			var otherParam = {'webType': 2, 'type': 1}
			broadCastLoad.initTree(url, otherParam);
			$('#search_condition2').on('input propertychange', broadCastLoad.ztreeSearch);// 模糊查询
		},

		ztreeSearch: function () {
			if (searchTimeout !== undefined) {
				clearTimeout(searchTimeout);
			}
			searchTimeout = setTimeout(function () {
				broadCastLoad.search_condition();
			}, 500);
		},
		search_condition: function (event) {
			var value = $("#search_condition2").val();
			var url = '/clbs/m/basicinfo/monitoring/vehicle/treeStateInfo';
			var otherParam = {'webType': 2, 'type': 1}
			if(value!=''){
				otherParam.queryParam = value;
			}
			broadCastLoad.initTree(url, otherParam);
		},
		initTree: function(url,otherParam){
			var setting = {
				async: {
					url: url,
					type: "post",
					enable: true,
					autoParam: ["id"],
					dataType: "json",
					otherParam: otherParam,
					dataFilter: broadCastLoad.ajaxDataFilter
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
					countClass: "group-number-statistics"
				},
				data: {
					simpleData: {
						enable: true
					}
				},
				callback: {
//		                    beforeClick: broadCastLoad.beforeClickVehicle,
					onAsyncSuccess: broadCastLoad.zTreeOnAsyncSuccess,
//		                    beforeCheck: broadCastLoad.zTreeBeforeCheck,
//		                    onCheck: broadCastLoad.onCheckVehicle,
//		                    onExpand: broadCastLoad.zTreeOnExpand
				}
			};
			$.fn.zTree.init($("#treeDemo"), setting, null);
		},
		ajaxDataFilter: function (treeId, parentNode, responseData) {
			var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
			var data = responseData;
			for (var i = 0; i < data.length; i++) {
				data[i].open = true;
				if(data[i].type =='vehicle' || data[i].type =='people' || data[i].type =='thing'){
					data[i].isParent = false;
				}
				if(data[i].id == currentRightClickVehicleId){
					data[i].checked = true;
				}
			}
			return data;
		},
		beforeClickVehicle: function (treeId, treeNode) {
			var zTree = $.fn.zTree.getZTreeObj("treeDemo");
			zTree.checkNode(treeNode, !treeNode.checked, true, true);
			return false;
		},
		zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
			var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
			if(size <= 5000){
				treeObj.checkAllNodes(true);
			}
			broadCastLoad.getCharSelect(treeObj);
		},
		zTreeBeforeCheck: function (treeId, treeNode) {
			// return true;
			var flag = true;
			if (!treeNode.checked) {
				if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
					var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
						.getCheckedNodes(true), v = "";
					var nodesLength = 0;

					json_ajax("post", "/clbs/m/personalized/ico/getCheckedVehicle",
						"json", false, {"parentId": treeNode.id, "type": treeNode.type}, function (data) {
							if (data.success) {
								nodesLength += data.obj;
							} else {
								layer.msg(data.msg);
							}
						});

					//存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
					var ns = [];
					//节点id
					var nodeId;
					for (var i = 0; i < nodes.length; i++) {
						nodeId = nodes[i].id;
						if (nodes[i].type == "people" || nodes[i].type == "vehicle" || nodes[i].type == "thing") {
							//查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
							var nd = zTree.getNodeByParam("tId", nodes[i].tId, treeNode);
							if (nd == null && $.inArray(nodeId, ns) == -1) {
								ns.push(nodeId);
							}
						}
					}
					nodesLength += ns.length;
				} else if (treeNode.type == "people" || treeNode.type == "vehicle" || treeNode.type == "thing") { //若勾选的为监控对象
					var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
						.getCheckedNodes(true), v = "";
					var nodesLength = 0;
					//存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
					var ns = [];
					//节点id
					var nodeId;
					for (var i = 0; i < nodes.length; i++) {
						nodeId = nodes[i].id;
						if (nodes[i].type == "people" || nodes[i].type == "vehicle" || treeNode.type == "thing") {
							if ($.inArray(nodeId, ns) == -1) {
								ns.push(nodeId);
							}
						}
					}
					nodesLength = ns.length + 1;
				}
				if (nodesLength > 5000) {
					layer.msg(maxSelectItem);
					flag = false;
				}
			}
			if (flag) {
				//若组织节点已被勾选，则是勾选操作，改变勾选操作标识
				if (treeNode.type == "group" && !treeNode.checked) {
					checkFlag = true;
				}
			}
			return flag;
		},
		onCheckVehicle: function (e, treeId, treeNode) {
			var zTree = $.fn.zTree.getZTreeObj("treeDemo");
			//若为取消勾选则不展开节点
			if (treeNode.checked) {
				zTree.expandNode(treeNode, true, true, true, true); // 展开节点
			}
			broadCastLoad.getCharSelect(zTree);
			broadCastLoad.getCheckedNodes();
		},
		zTreeOnExpand: function (event, treeId, treeNode) {
			return
			//判断是否是勾选操作展开的树(是则继续执行，不是则返回)
			if (treeNode.type == "group" && !checkFlag) {
				return;
			}
			//初始化勾选操作判断表示
			checkFlag = false;
			var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
			if (treeNode.type == "group") {
				var assign = []; // 当前组织及下级组织的所有分组
				broadCastLoad.getGroupChild(treeNode, assign);
				if (assign != null && assign.length > 0) {
					for (var i = 0; i < assign.length; i++) {
						var node = assign[i];
						if (node.type == "assignment" && node.children === undefined) {
							if (!node.zAsync) { // 判断节点是否进行异步加载，若没有，则先异步加载，避免添加重复节点
								treeObj.reAsyncChildNodes(node, "refresh");
							}
						}
					}
				}
			}
		},

		getGroupChild: function (node, assign) { // 递归获取组织及下级组织的分组节点
			var nodes = node.children;
			if (nodes != null && nodes != undefined && nodes.length > 0) {
				for (var i = 0; i < nodes.length; i++) {
					var node = nodes[i];
					if (node.type == "assignment") {
						assign.push(node);
					} else if (node.type == "group" && node.children != undefined) {
						broadCastLoad.getGroupChild(node.children, assign);
					}
				}
			}
		},
		unique: function (array){
			var n = {}, r = [], len = array.length, val, type;
			for (var i = 0; i < array.length; i++) {
			val = array[i];
			type = typeof val;
			if (!n[val]) {
			n[val] = [type];
			r.push(val);
			} else if (n[val].indexOf(type) < 0) {
			n[val].push(type);
			r.push(val);
			}
			}
			return r;
			} ,
		getCheckedNodes: function () {
			var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
				nodes = zTree.getCheckedNodes(true), vid = [];
			vehicles = {}
			for (var i = 0, l = nodes.length; i < l; i++) {
				if (nodes[i].type == "vehicle" ||nodes[i].type == "people" || nodes[i].type == "thing") {
					vid.push(nodes[i].id);
					vehicles[nodes[i].id] = nodes[i];
				}
			}
			vehicleIds = broadCastLoad.unique(vid).join(',');
			return vehicleIds;
		},
		getCharSelect: function(treeObj){

		},
		start: function(){
			// 关闭之前所有订阅的音频通道
          	broadCastLoad.stop();
			connectedVehicles = [];
			promiseIds = [];
			sendParamData = [];
			var vids = broadCastLoad.getCheckedNodes();
			var channelUrl = '/clbs/realTimeVideo/video/getAllChannels';
			var channelData = {
			  vehicleId : vids,
			  isChecked: false
			}
			json_ajax("post", channelUrl, "json", true, channelData, function (data) {
			   if(data.success == true){
				   var originalData = JSON.parse(ungzip(data.msg));
				   if (originalData.length != 0) {
					   for(var i = 0; i < originalData.length; i++){
						   var item = originalData[i];
						   if (item.channelType == 1) {
							   var vehicle = vehicles[item.vehicleId];
							   var state = false;
							   for (var j = 0; j < connectedVehicles.length; j++) {
							       if (connectedVehicles[j].vehicleId === item.vehicleId) {
                                     state = true;
								   }
							   }
							   if (!state) {
                                 var data = {
                                   vehicleId: item.vehicleId,
                                   channelNum: item.logicChannel,
                                   mobile: vehicle.simcardNumber,
                                   channelType: item.channelType,
                                   streamType: item.streamType,
								   vName: vehicle.name,
                                   requestType: 0,
                                   orderType: 1
                                 };
                                 promiseIds.push(data.vId);
                                 connectedVehicles.push(data);
							   }
						   }
					   }
					   if (connectedVehicles.length > 0) {
                         broadCastLoad.subRealVideo(connectedVehicles);
					   } else {
						 layer.msg('当前监控对象没有音频通道号，请在设置通道号指令里设置音频类型的通道并下发！');
					   }
				   } else {
					   layer.msg("当前监控对象没有音频通道号，请在设置通道号指令里设置音频类型的通道并下发！");
				   }
			   }else{
				   layer.msg('获取通道号失败');
			   }
			});
			if(vids.length==0){
				layer.msg('请勾选监控对象');
				return;
			}

		},
		subRealVideo: function(list){
			if (list.length <= 10) {
			  for (var i = 0; i < list.length; i++) {
				  var value = list[i];
				  json_ajax('POST', '/clbs/v/monitoring/audioAndVideoParameters/' + value.vehicleId, 'json', false, null, function (d) {
					  if (d.success) {
						  broadCastLoad.audioRadioSocket(d.obj, value);
					  }
				  });
			  }
			} else {
				layer.msg('单次同时广播个数不超过10个');
			}
		},
		// 广播socket连接
		audioRadioSocket: function(audioData, list){
			var protocol = 'ws://';
			if (document.location.protocol === 'https:') {
			  protocol = 'wss://';
			}
			var url = protocol + videoRequestUrl + ':' + audioRequestPort + '/' + list.mobile + '/' + list.channelNum + '/2';

			var data = {
				vehicleId: list.vehicleId,
				simcardNumber: list.mobile,
				channelNumber: list.channelNum,
				sampleRate: audioData.samplingRateStr || 8000,
				channelCount: audioData.vocalTractStr || 0,
				audioFormat: audioData.audioFormatStr,
				playType: 'DOWN_WAY',
				dataType: 4,
				userID: audioData.userUuid,
				deviceID: audioData.deviceId,
				streamType: 1,
				deviceType: audioData.deviceType
			};
			var audioSocket = new RTPMediaPlayer({
				url: url,
				type: 'DOWN_WAY',
				data: data,
				audioEnabled: false,
				videoEnabled: false,
				recordEnabled: true,
				onMessage: function($data, $msg) {
					var info = JSON.parse($msg);
					if (info.data.msgBody.code == -1004 ||
						info.data.msgBody.code == -1005 ||
						info.data.msgBody.code == -1006 ||
						info.data.msgBody.code == -1008
					) {
						layer.msg(info.data.msgBody.msg);
					}
				},
				socketOpenFun: function ($data, $$this) {
					var name = connectedVehicles.find(function(x){
						return x.vehicleId === $data.vehicleId;
					}).vName;
					var tr ='<tr>\n' +
						'\t\t\t\t\t\t\t<td>'+ name +'</td>\n' +
						'\t\t\t\t\t\t\t<td>连接成功</td>\n' +
						'\t\t\t\t\t\t</tr>';

					$('#broadCastTbody').append($(tr));
					var id = $data.vehicleId + '-' + $data.channelNumber;
					broadCastSocket[id] = audioSocket;

					var setting = {
						vehicleId: $data.vehicleId,                         // 车辆ID
						simcardNumber: $data.simcardNumber,                 // sim卡号
						channelNumber: JSON.stringify($data.channelNumber), // 通道号
						sampleRate: JSON.stringify($data.sampleRate),       // 采样率
						channelCount: JSON.stringify($data.channelCount),   // 声道数
						audioFormat: $data.audioFormat,                     // 编码格式
						playType: $data.playType,                           // 播放类型 实时 REAL_TIME，回放 TRACK_BACK，对讲 BOTH_WAY，监听 UP_WAY，广播 DOWN_WAY
						dataType: JSON.stringify($data.dataType),           // 数据类型0：音视频，1：视频，2：双向对讲，3：监听，4：中心广播，5：透传
						userID: $data.userID,                               // 用户ID
						deviceID: $data.deviceID,                           // 终端ID
						streamType: JSON.stringify($data.streamType),       // 码流类型0：主码流，1：子码流
						deviceType: $data.deviceType,
					};
					$$this.play(setting);
				},
				openAudioSuccess: function ($state) {
					var name = connectedVehicles.find(function(x){
						return x.vehicleId === $state.vehicleId;
					}).vName;
					var tr ='<tr>\n' +
						'\t\t\t\t\t\t\t<td>'+ name +'</td>\n' +
						'\t\t\t\t\t\t\t<td>连接成功</td>\n' +
						'\t\t\t\t\t\t</tr>';

					$('#broadCastTbody').append($(tr));
					var id = $state.vehicleId + '-' + $state.channelNumber;
					broadCastSocket[id] = audioSocket;
				},
				openAudioFail: function (msg) {
				  layer.msg(msg);
				},
			});
		},
		// 关闭音频广播功能
		closeAudioFun: function(toPostData) {
			for (var key in broadCastSocket) {
				broadCastSocket[key].cmdCloseVideo();
			  	broadCastSocket[key].closeSocket();
			}
			broadCastSocket = {};
		},
		stop: function(){
			if(connectedVehicles){
				$('#broadCastTbody').empty();
				connectedVehicles = null;

				var ids = [], channels =[], channelTypes = [], unique = [];
				console.log(sendParamData);
				sendParamData.map(function(ele){
					ids.push(ele.vehicleId);
					channels.push(ele.channelNum);
					channelTypes.push(ele.channelType);
                    unique.push(ele.unique);
				})
				broadCastLoad.closeAudioFun({
					vehicleId: ids.join(','),
					channelNum: channels.join(','),
					channelType: channelTypes.join(','),
                    unique: unique.join(','),
				});
			}
		},
		close:function(){
			broadCastLoad.stop();
		}
	}

	$(function(){
		broadCastLoad.init();
		$('#broadCastStartButton').on('click', broadCastLoad.start);
		$('#broadCastStopButton').on('click', broadCastLoad.stop);
		$('#broadCastCloseButton').on('click', broadCastLoad.close);
		$('#modalClose').on('click', broadCastLoad.close);
	})
	})(window,$)