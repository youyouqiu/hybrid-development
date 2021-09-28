(function ($, window) {
    var endTime;
    var startTime;
    var dataSet = [];//table数据
    var addressMsg = [];
    var startLoc = [];
    var endLoc = [];
    var echartsDate = []; // 图表横轴时间坐标
    var oilConsume = []; // 总油耗
    var allMileage = []; // 总里程
    var echartsSpeed = []; // 速度
    var accState = []; // ACC
    var y_oil_before = null
    ,x_oil_before = null
    ,y_oil_after = null
    ,x_oil_after = null
    ,tableList = []; // table数据数组
    var tmp = [];
    var chart;
	var vehicleId;
	var zTreeIdJson = {};
	var size;//当前权限监控对象数量
    var vehicleList = JSON.parse($("#vehicleList").attr("value"));
	var checkFlag = false;
    var permission =  $('#permission').val();

    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于100自动勾选
    var isSearch = true;

    travelEnergyList = {
        echartsInit: function () {
            var myChart = echarts.init(document.getElementById('sjcontainer'));
            var option = {
                tooltip: {
                    trigger: 'axis',
                    textStyle: {
                        fontSize: 20
                    },
                    formatter: function (a) {
                    	var goString = JSON.stringify(a);
                    	var unit = ['L','km','km/h','ACC','空调'];
                        var relVal = "";
                        var addRelVal = "";
                        var relValTime = '';
                        if(a[2].data == null||a[2].data==""){
                       	 	relVal = "无相关数据";
                        }else{
                            relValTime = a[0].name;
                           for(var i = 0; i < a.length; i++){
                               if(a[i].seriesName != "空调" && a[i].seriesName != "ACC"){
                            	   relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:"+ a[i].color +"'></span>"+ a[i].seriesName +"："+ a[i].data + unit[a[i].seriesIndex] +"";
                               }else{
                            	   if(a[i].data == 1){
                                       relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:"+ a[i].color +"'></span>"+ a[i].seriesName +"：关闭";
                                   }else{
                                       relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:"+ a[i].color +"'></span>"+ a[i].seriesName +"：开启";
                                   }
                               }
                           }
                       }
                       var allVal = relValTime + addRelVal + relVal;
                       return allVal;
                    }
                },
                legend: {
                	itemHeight:12,
                	itemWidth:12,
                	data: [
                	       {name:'总能耗',icon:'circle'},
                	       {name:'总里程',icon:'circle'},
                	       {name:'速度',icon:'circle'},
                	       {name:'ACC',icon:'circle'},
                	],
                    left: 'auto',
                },
                toolbox: {
                    show: false
                },
                xAxis: {
                    type: 'category',
                    boundaryGap: false,
                    data: echartsDate
                },
                yAxis: [
					{
					    type: 'value',
					    name: '总能耗(L)',
					    scale: true,
					    position: 'left',
					    offset: 60,
					    axisLabel: {
					        formatter: '{value}'
					    },
					    splitLine:{
					    	show:false
					    }
					},    
					{
                        type: 'value',
                        name: '总里程(km)',
                        position: 'left',
                        scale: true,
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine:{
                        	show:false
                        }
                    },
                    {
                        type: 'value',
                        name: '速度(km/h)',
                        scale: true,
                        min: 0,
                        max: 240,
                        position: 'right',
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine:{
                        	show:false
                        }
                    },
                    {
                        type: 'value',
                        name: 'ACC',
                        scale: true,
                        position: 'right',
                        offset: 60,
                        min: 0,
                        max: 1,
                        splitNumber: 1,
                        axisLabel: {
                            formatter: '{value}',
                            formatter: function (value) {
                                if (value == 1) {
                                    return '关'
                                } else {
                                    return '开'
                                }
                            },
                        },
                        splitLine:{
                        	show:false
                        }
                    }
                ],
                dataZoom: [{
                    type: 'inside'

                }, {
                    start: 0,
                    end: 10,
                    handleIcon: 'M10.7,11.9v-1.3H9.3v1.3c-4.9,0.3-8.8,4.4-8.8,9.4c0,5,3.9,9.1,8.8,9.4v1.3h1.3v-1.3c4.9-0.3,8.8-4.4,8.8-9.4C19.5,16.3,15.6,12.2,10.7,11.9z M13.3,24.4H6.7V23h6.6V24.4z M13.3,19.6H6.7v-1.4h6.6V19.6z',
                    handleSize: '80%',
                    handleStyle: {
                        color: '#fff',
                        shadowBlur: 3,
                        shadowColor: 'rgba(0, 0, 0, 0.6)',
                        shadowOffsetX: 2,
                        shadowOffsetY: 2
                    }
                }],
                series: [
                    {
                        name: '总能耗',
                        yAxisIndex: 0,
                        type: 'line',
                        smooth: true,
                        showSymbol: false,
                        symbolSize :15,
                        symbol: 'image://../../../resources/img/circle.png',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(248, 123, 0)'
                            }
                        },
                        data: oilConsume,
                        markPoint: {
                        	symbolSize: [48,61],
                        	symbolOffset: [0,-32],
                            silent: true,
                            data: [
                                {
	                                yAxis: y_oil_before,
	                                xAxis: x_oil_before,
	                                symbol:'image://../../../resources/img/criterion_before.png',
	                                label :{
	                                    normal :{
	                                        show:true,
	                                        formatter : ""
	                                    }
	                                }
                                },
                                {
	                                yAxis: y_oil_after,
	                                xAxis: x_oil_after,
	                                symbol:'image://../../../resources/img/criterion_after.png',
	                                label :{
	                                    normal :{
	                                        show:true,
	                                        formatter : ""
	                                    }
	                                }
                                },
                            ]
                        }
                    },
                    {
                        name: '总里程',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: true,
                        showSymbol: false,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(109, 207, 246)'
                            }
                        },
                        data: allMileage,
                    },
                    {
                        name: '速度',
                        yAxisIndex: 2,
                        type: 'line',
                        smooth: true,
                        showSymbol: false,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(145, 218, 0)'
                            }
                        },
                        data: echartsSpeed,
                    },
                    {
                        name: 'ACC',
                        yAxisIndex: 3,
                        type: 'line',
                        smooth: true,
                        showSymbol: false,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(199, 209, 223)'
                            }
                        },
                        areaStyle: {
                            normal: {
                                color: 'rgba(199, 209, 223,0.9)'
                            }
                        },
                        data: accState,
                    }
                ]
            };
            myChart.setOption(option);
            myChart.on('click', travelEnergyList.chartsClickEvent);
            window.onresize = myChart.resize;
        },
        // echarts点击事件
        chartsClickEvent: function(params) {
        	layer.msg('<label style="cursor:pointer;"><input type="radio" data-state="1" name="oilState" checked />基准开始时间</label><br/><label style="cursor:pointer;"><input data-state="2" name="oilState" type="radio" />基准结束时间</label>', {
      	      time: 10000, //10s后自动关闭
      	      btn: ['确定', '取消'],
      	      yes: function(e){
      	    	var time = params.name;
      	        // 判断是否是节油产品安装之前
      	    	var url="/clbs/m/energy/mobileSourceBaseInfo/compareDate";
                var parameter={"vehicleId": vehicleId, "standardTime": time};
                json_ajax("POST", url, "json", false, parameter, function(data) {
                	if (data.success === false) {
                		var state = $("input[name='oilState']:checked").attr("data-state")
              	    	,value = params.value;
                		if (state === '1') { // 基准开始时间
            				if (x_oil_after !== null) {
            					var time_state = travelEnergyList.compareTime(time, x_oil_after);
            					if (time_state === true) {
            						x_oil_before = time;
            						y_oil_before = value;
            						$('#criterionStartTime').attr('value', time);
            						travelEnergyList.echartsInit();
                  	      	    	layer.closeAll();
            					} else {
            						layer.msg(time_state);
            					}
            				} else {
            					x_oil_before = time;
            					y_oil_before = value;
        						$('#criterionStartTime').attr('value', time);
        						travelEnergyList.echartsInit();
              	      	    	layer.closeAll();
            				}
                		} else if (state === '2') {
            				if (x_oil_before !== null) {
            					var time_state = travelEnergyList.compareTime(x_oil_before, time);
            					if (time_state === true) {
            						x_oil_after = time;
            						y_oil_after = value;
            						$('#criterionEndTime').attr('value', time);
            						travelEnergyList.echartsInit(false);
                  	      	    	layer.closeAll();
            					} else {
            						layer.msg(time_state);
            					}
            				} else {
            					x_oil_after = time;
            					y_oil_after = value;
        						$('#criterionEndTime').attr('value', time);
        						travelEnergyList.echartsInit();
              	      	    	layer.closeAll();
            				}
                		}
                	} else {
                        var installTime = data.obj.installTime;
                        layer.closeAll();
                        layer.msg(installTime, {area: '370px'});
                	}
                });
  	          },
      	      btn2: function(){
      	        layer.closeAll();
      	      }
      	   });
        },
        // 选择基准时间比较
        compareTime: function(stime, etime) {
        	var stime_seconds = new Date(stime.replace(/\-/g, '/')).getTime();
        	var etime_seconds = new Date(etime.replace(/\-/g, '/')).getTime();
        	var time_count =  etime_seconds - stime_seconds;
        	if (stime_seconds > etime_seconds ) {
        		return endTimegtStarTime;
        	} else if (time_count < (30 * 60 * 1000) || time_count > (24 * 60 * 60 * 1000)) {
        		return timeError;
        	} else {
        		return true;
        	}
        }, 
        // 基准数据添加
        criterionDataAddFun: function() {
        	var start_time = $('#criterionStartTime').attr('value');
        	var end_time = $('#criterionEndTime').attr('value');
        	if (start_time === undefined || start_time === '') {
        		layer.msg(baseinfoStartime);
        		return false;
        	}
        	if (end_time === undefined || end_time === '') {
        		layer.msg(baseinfoEndtime);
        		return false;
        	}
        	// 后台数据请求
        	if (tableList.length < 10) {
        		var url="/clbs/m/energy/travelEnergy/add";
                var parameter={"vehicleId": vehicleId, "startTime": start_time, "endTime": end_time};
                json_ajax("POST", url, "json", true, parameter, travelEnergyList.addCriterionState);
        	} else {
        		layer.msg(baseDataMax10);
        	}
        },
        // 基准数据添加状态获取
        addCriterionState: function(data) {
        	if (data.success) {
                layer.msg(data.msg);
        		var url="/clbs/m/energy/travelEnergy/travelEnergyList";
                var parameter={"vehicleId": vehicleId};
                json_ajax("POST", url, "json", true, parameter, travelEnergyList.getCriterionData);
        	} else {
        		layer.msg(data.msg);
        	}
        },
        // 获取基准table数据
        getCriterionData: function(data) {
        	var tableData = data.obj.travelEnergy; // table数据
            var energyData = data.obj.energy; // table下面统计数据
			travelEnergyList.energyData(energyData);
        	travelEnergyList.assembleTableData(tableData);
        },
        // 初始化组织树
        treeInit: function(){
        	var setting = {
	            async: {
	                url: travelEnergyList.getTreeUrl,
	                type: "post",
	                enable: true,
	                autoParam: ["id"],
	                dataType: "json",
	                otherParam: {"type": "multiple"},
	                otherParam: {"icoType": "0"},
	                dataFilter: travelEnergyList.ajaxDataFilter
	            },
	            check: {
	                enable: true,
	                chkStyle: "checkbox",
	                radioType: "all",
	                chkboxType: {
					    "Y": "s",
					    "N": "s"
                    }
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
	                beforeClick: travelEnergyList.beforeClickVehicle,
	                onCheck: travelEnergyList.onCheckVehicle,
	                beforeCheck: travelEnergyList.zTreeBeforeCheck,
					onExpand: travelEnergyList.zTreeOnExpand,
					onAsyncSuccess: travelEnergyList.zTreeOnAsyncSuccess,
					onNodeCreated: travelEnergyList.zTreeOnNodeCreated
	            }
	        };
	        $.fn.zTree.init($("#treeDemo"), setting, null);
			$("[data-toggle='tooltip']").tooltip();
        },

        //模糊查询树
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选

            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                travelEnergyList.treeInit();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/m/functionconfig/fence/bindfence/vehicleTreeFuzzy",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": "multiple", "queryParam": param, "queryType": "name"},
                        dataFilter: travelEnergyList.ajaxQueryDataFilter
                    },
                    check: {
                        enable: true,
                        chkStyle: "checkbox",
                        radioType: "all",
                        chkboxType: {
                            "Y": "s",
                            "N": "s"
                        }
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
                        beforeClick: travelEnergyList.beforeClickVehicle,
                        onCheck: travelEnergyList.onCheckVehicle,
                        onExpand: travelEnergyList.zTreeOnExpand,
                        onNodeCreated: travelEnergyList.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData))
            var nodesArr = filterQueryResult(responseData, crrentSubV);
            for (var i=0;i<nodesArr.length;i++){
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        // 组织树请求url
        getTreeUrl : function (treeId, treeNode){
        	if (treeNode == null) {
        		return "/clbs/m/personalized/ico/vehicleTree";
        	}else if(treeNode.type == "assignment") {
        		return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId="+treeNode.id+"&isChecked="+treeNode.checked+"&monitorType=vehicle";
        	} 
        },
        //组织树预处理加载函数
        ajaxDataFilter: function(treeId, parentNode, responseData){
        	var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
    	    if (responseData.msg) {
    	    	var obj = JSON.parse(ungzip(responseData.msg));
    	    	var data;
    	    	if (obj.tree != null && obj.tree != undefined) {
    	    		data = obj.tree;
    	    		size = obj.size;
    	    	}else{
    	    		data = obj
    	    	}
    	    	for (var i = 0; i < data.length; i++) {
    	            data[i].open = true;
    	        }
    	    	return data;
    	    }
        },
        // 组织树加载成功回掉事件
        zTreeOnAsyncSuccess: function(event, treeId, treeNode, msg){
        	var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
        	if(size <= 5000 && ifAllCheck){
            	treeObj.checkAllNodes(true);
            }
        	travelEnergyList.getCharSelect(treeObj);
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
	    beforeClickVehicle: function(treeId, treeNode) {
	        var zTree = $.fn.zTree.getZTreeObj("treeDemo");
	        zTree.checkNode(treeNode, !treeNode.checked, true, true);
	        return false;
	    },
	    unique: function(arr){
	        var result = [], hash = {};
	        for (var i = 0, elem; (elem = arr[i]) != null; i++) {
	            if (!hash[elem]) {
	                result.push(elem);
	                hash[elem] = true;
	            }
	        }
	        return result;
	    },
        zTreeBeforeCheck : function(treeId, treeNode){
        	var flag = true;
            if (!treeNode.checked) {
            	if(treeNode.type == "group" || treeNode.type == "assignment"){ //若勾选的为组织或分组
            		var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                		.getCheckedNodes(true), v = "";
		            var nodesLength = 0;
		            
		            json_ajax("post", "/clbs/m/personalized/ico/getCheckedVehicle",
	                        "json", false, {"parentId": treeNode.id,"type": treeNode.type}, function (data) {
	                            if(data.success){
                            		nodesLength += data.obj;
	                            }else{
	                            	layer.msg(data.msg);
	                            }
	                        });
		            
		            //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
		            var ns = [];
		            //节点id
		            var nodeId;
		            for (var i=0;i<nodes.length;i++) {
		            	nodeId = nodes[i].id;
		                if(nodes[i].type == "people" || nodes[i].type == "vehicle"){
		                	//查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
		                	var nd = zTree.getNodeByParam("tId",nodes[i].tId,treeNode);
		                	if(nd == null && $.inArray(nodeId,ns) == -1){
		                		ns.push(nodeId);
		                	}
		                }
		            }
		            nodesLength += ns.length;
            	}else if(treeNode.type == "people" || treeNode.type == "vehicle"){ //若勾选的为监控对象
            		var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                    	.getCheckedNodes(true), v = "";
		            var nodesLength = 0;
		            //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
		            var ns = [];
		            //节点id
		            var nodeId;
		            for (var i=0;i<nodes.length;i++) {
		            	nodeId = nodes[i].id;
		                if(nodes[i].type == "people" || nodes[i].type == "vehicle"){
		                	if($.inArray(nodeId,ns) == -1){
		                		ns.push(nodeId);
		                	}
		                }
		            }
		            nodesLength = ns.length + 1;
            	}
                if(nodesLength > 5000){
                    layer.msg(maxSelectItem);
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
        onCheckVehicle: function(e, treeId, treeNode){
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
        	//若为取消勾选则不展开节点
        	if(treeNode.checked){
                isSearch = false;
        		zTree.expandNode(treeNode, true, true, true, true); // 展开节点
        	}
        	if(treeNode.type != "assignment" || (treeNode.type == "assignment" && treeNode.children != undefined)){
        		travelEnergyList.getCharSelect(zTree);
        	}
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
        },
		zTreeOnExpand : function (event, treeId, treeNode) {
        	//判断是否是勾选操作展开的树(是则继续执行，不是则返回)
        	if(treeNode.type == "group" && !checkFlag){
        		return;
        	}
        	//初始化勾选操作判断表示
        	checkFlag = false;
        	var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (treeNode.type == "group"){
            	 var url = "/clbs/m/functionconfig/fence/bindfence/putMonitorByGroup";
                 json_ajax("post", url, "json", false, {"groupId": treeNode.id,"isChecked":treeNode.checked,"monitorType":"vehicle"}, function (data) {
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
        },
        getCharSelect: function (treeObj) {
        	var nodes = treeObj.getCheckedNodes(true);
        	var allNodes = treeObj.getNodes();
			if (nodes.length > 0) {
            	$("#groupSelect").val(allNodes[0].name);
            } else {
            	$("#groupSelect").val("");
            }
			$("#charSelect").val("").attr("data-id","").bsSuggest("destroy");
			var veh=[];
			var vid=[];
			for(var i=0;i<nodes.length;i++){
				if(nodes[i].type=="vehicle"){
					veh.push(nodes[i].name)
					vid.push(nodes[i].id)
				}
			}
			var vehName = travelEnergyList.unique(veh);
			var vehId = travelEnergyList.unique(vid);
			$("#charSelect").empty();
            var deviceDataList = {value: []};
            for (var j = 0; j < vehName.length; j++){
                for(var k=0;k<vehicleList.length;k++){
                    if(vehId[j]==vehicleList[k].vehicleId){
                        deviceDataList.value.push({
                            name: vehName[j],
                            id: vehId[j]
                        });
                    }
                }
            };
			$("#charSelect").bsSuggest({
		        indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
		        indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
		        data: deviceDataList,
		        effectiveFields: ["name"]
		    }).on('onDataRequestSuccess', function (e, result) {
		    }).on("click",function(){
		    }).on('onSetSelectValue', function (e, keyword, data) {
		    }).on('onUnsetSelectValue', function () {
		    });
			if(deviceDataList.value.length > 0){
				$("#charSelect").val(deviceDataList.value[0].name).attr("data-id",deviceDataList.value[0].id);
			}
			$("#groupSelect,#groupSelectSpan").bind("click",travelEnergyList.showMenu);
			$("#button").removeClass('disabled loading-state-button').find('i').attr("class", 'caret');
        },
        //判断是否绑定传感器（"true"为绑定,""为非绑定）
        getSensorMessage : function (band) {
            var flog;
            var url = "/clbs/v/oilmassmgt/oilquantitystatistics/getSensorMessage";
            var data = {"band": band};
            json_ajax("POST", url, "json", false,data,function(data){
                flog = data;
            });
            return flog;
        },
        getCallback:function(data){
        	var responseData = JSON.parse(ungzip(data.obj.oilInfo));
        },
        // 前一天
        yesterdayClick: function () {
            travelEnergyList.startDay(-1);
            $('#timeInterval').val(startTime + '--' + endTime);
            vehicleId = $("#charSelect").attr("data-id");
            if (vehicleId !== "") {
                // 清空数据
                travelEnergyList.clearData();
                var url="/clbs/m/energy/travelEnergy/list";
                var parameter={"vehicleId": vehicleId, "startTime": startTime, "endTime": endTime};
                json_ajax("POST", url, "json", true, parameter, travelEnergyList.searchDataFun);
            } else {
                layer.msg(selectMonitoringObjec);
            }
        },
        // 上一天
        upDay: function(){
            travelEnergyList.startDay(1);
            vehicleId = $("#charSelect").attr("data-id");
            if (vehicleId !== "") {
                var dateValue = new Date().getTime();
                var startTimeValue = new Date(startTime.split(" ")[0].replace(/-/g,"/")).getTime();
                if(startTimeValue <= dateValue){
                    $('#timeInterval').val(startTime + '--' + endTime);
                    travelEnergyList.clearData();
                    var url="/clbs/m/energy/travelEnergy/list";
                    var parameter={"vehicleId": vehicleId, "startTime": startTime, "endTime": endTime};
                    json_ajax("POST", url, "json", true, parameter, travelEnergyList.searchDataFun);
                }else{
                    layer.msg("暂时没办法穿越，明天我再帮您看吧！");
                }
            } else {
                layer.msg(selectMonitoringObjec);
            }
        },
        //开始时间
        startDay: function (day) {
            var timeInterval = $('#timeInterval').val().split('--');
            var startValue = timeInterval[0];
            var endValue = timeInterval[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60
                    * 24 * day;

                today.setTime(targetday_milliseconds); //注意，这行是关键代码

                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = travelEnergyList.doHandleMonth(tMonth + 1);
                tDate = travelEnergyList.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " "
                    + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = travelEnergyList.doHandleMonth(endMonth + 1);
                endDate = travelEnergyList.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " "
                    + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = travelEnergyList.doHandleMonth(vMonth + 1);
                vDate = travelEnergyList.doHandleMonth(vDate);
                startTime = vYear + "-" + vMonth + "-" + vDate + " "
                    + "00:00:00";
                if (day == 1) {
                    endTime = vYear + "-" + vMonth + "-" + vDate + " "
                        + "23:59:59";
                } else {
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = travelEnergyList.doHandleMonth(vendMonth + 1);
                    vendDate = travelEnergyList.doHandleMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate + " "
                        + "23:59:59";
                }
            }
            $('#timeInterval').val(startTime + '--' + endTime);
        },
        doHandleMonth: function (month) {
            var m = month;
            if (month.toString().length == 1) {
                m = "0" + month;
            }
            return m;
        },
        //当前时间
        getsTheCurrentTime: function () {
            var nowDate = new Date();
            startTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate()) + " " + "00:00:00";
            endTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate())
                + " "
                + ("23")
                + ":"
                + ("59")
                + ":"
                + ("59");
            $('#timeInterval').val(startTime + '--' + endTime);
        },
        // 查询
        inquireClick: function (number) {
            if (number == 0) {
                travelEnergyList.getsTheCurrentTime();
            } else if (number == -1) {
                travelEnergyList.startDay(-1)
            } else if (number == -3) {
                travelEnergyList.startDay(-3)
            } else if (number == -7) {
                travelEnergyList.startDay(-7)
            }
            travelEnergyList.clearData();
            var timeInterval = $('#timeInterval').val().split('--');
            var startTime_now = timeInterval[0];
            var endTime_now = timeInterval[1];
            var state = travelEnergyList.searchTimeCompare(startTime_now, endTime_now);
            if (state === true) {
            	vehicleId = $("#charSelect").attr("data-id");
                if (vehicleId != "") {
                	// 清空数据
                	var url="/clbs/m/energy/travelEnergy/list";
                    var parameter={"vehicleId": vehicleId, "startTime": startTime_now, "endTime": endTime_now};
                    json_ajax("POST", url, "json", true, parameter, travelEnergyList.searchDataFun);
                } else {
                    layer.msg(selectMonitoringObjec);
                }
            } else {
            	layer.msg(state);
            }
        },
        // 查询点击时间比较
        searchTimeCompare: function(stime, etime) {
        	var s_time = new Date(stime.replace(/\-/g, '/')).getTime()
        	,e_time = new Date(etime.replace(/\-/g, '/')).getTime();
        	if (e_time - s_time < (60 * 60 * 1000)) {
        		return timeScopeHour;
        	} else if (e_time - s_time > (7 * 24 * 60 * 60 * 1000)) {
        		return timeScopeDay;
        	} else {
        		return true;
        	}
        },
        // 查询按钮回掉函数
        searchDataFun: function(data) {
            var brandName = $("input[name='charSelect']").val();
            if (brandName.length > 8) {
                $("#carName").attr("title",brandName).tooltip('fixTitle');
                brandName = brandName.substring(0, 7) + '...';
            }else {
                $('#carName').removeAttr('data-original-title');
			}
            $("#carName").text(brandName);
        	$(".toopTip-btn-left,.toopTip-btn-right").css("display","inline-block");
        	if(data.success==false){
        		return;
			}
        	var echartsData = JSON.parse(ungzip(data.obj.list)); // 图表数据
        	var flag = data.obj.flag;
        	var tableData = data.obj.travelEnergy; // table数据
        	var energyData = data.obj.energy; // table下面统计数据
        	var acc = data.obj.acc; // table下面统计数据
            for (var i = 0, len = echartsData.length; i < len; i++) {
                var value = echartsData[i];
                var vtime=value.vtime;
                if(i!=len-1){
                    var vtime1=echartsData[i+1].vtime;
                }
                var time=vtime1-vtime;
                if(time>300){
                    var j=Math.floor(time/300);
                    for(var k=0;k<j;k++){
                        vtime=vtime+300;
                        var time = travelEnergyList.getTime(vtime);
                        echartsDate.push(time);
                        allMileage.push("");
                        echartsSpeed.push("");
                        oilConsume.push("");
                        accState.push("");
                    }
                }
                var time = travelEnergyList.getTime(value.vtime);
                var acc=parseInt(value.acc);
                var totalOilwearOne;
                var this_mileage ;
				var speed=0;
                if(value.totalOilwearOne==null||value.totalOilwearOne==0){
                    totalOilwearOne="";
                }else{
                    totalOilwearOne=parseFloat(value.totalOilwearOne);
                }

                if (flag) {
					this_mileage = Number(value.mileageTotal).toFixed(1);
                    speed=Number(value.mileageSpeed).toFixed(1);
                } else {
					this_mileage = Number(value.gpsMile).toFixed(1);
                    speed= Number(value.speed).toFixed(1);
                }
                if(this_mileage==null||this_mileage==0){
                    this_mileage="";
                }
                if (this_mileage.substr(this_mileage.length - 1, 1) == '0') {
                    this_mileage = this_mileage.substring(0, this_mileage.indexOf('.'));;
                }
                if (speed.substr(speed.length - 1, 1) == '0'&&speed!='0') {
                    speed = speed.substring(0, speed.indexOf('.'));
                }
                if(speed==null||this_mileage==NaN||this_mileage==0){
                    speed="";
                }
                allMileage.push(this_mileage);
                echartsSpeed.push(speed);
                oilConsume.push(totalOilwearOne);
                echartsDate.push(time);
                accState.push(acc);
            }
        	$('#graphShow').removeClass('hidden');
        	$('#showClick').attr("class", "fa fa-chevron-down");
        	if (echartsData.length > 0) {
        		$('#addCriterionData').removeClass('hidden');
        	} else {
        		$('#addCriterionData').attr('class', 'form-horizontal table-top hidden');
        	}
        	travelEnergyList.echartsInit();
        	travelEnergyList.assembleTableData(tableData);
            travelEnergyList.energyData(energyData);
        },
        // 时间转换
        getTime: function (time) {
        	var date = new Date(time * 1000)
        	,y = date.getFullYear()
        	,m = (date.getMonth() + 1) >= 10 ? (date.getMonth() + 1) : '0' + (date.getMonth() + 1)
        	,d = date.getDate() >= 10 ? date.getDate() : '0' + date.getDate()
        	,hh = date.getHours() >= 10 ? date.getHours() : '0' + date.getHours()
        	,mm = date.getMinutes() >= 10 ? date.getMinutes() : '0' + date.getMinutes()
        	,ss = date.getSeconds() >= 10 ? date.getSeconds() : '0' + date.getSeconds()
        	return y + '-' + m + '-' + d + ' ' + hh + ':' + mm + ':' + ss;
        },
        // 组装table及下面统计数据
        assembleTableData: function(tableData) {
        	tableList = [];
        	for (var i = 0, len = tableData.length; i < len; i++) {
        		var value = tableData[i]
        		,list = [];
				travelTime=travelEnergyList.formatDuring(value.travelTime);
        		list.push(i + 1);
        		if(permission != 'false'){
                    list.push(value.id);
				}
        		list.push(value.brand);
        		list.push(value.groupName);
        		list.push(value.vehicleType);
        		list.push(value.fuelType);
        		list.push(value.startTime);
        		list.push(value.endTime);
        		list.push(travelTime);
        		list.push(value.tavelMile);
        		list.push(value.tavelFuel);
        		list.push(value.travelBaseFuel);
        		if(value.travelBaseCapacity==null){
                    list.push("");
				}else{
                    list.push(value.travelBaseCapacity);
				}
        		tableList.push(list);
        	}
        	travelEnergyList.exampleTable(tableList);
        },
        formatDuring:function(mss) {//毫秒转时间
            var hours = parseInt((mss % (60 * 60 * 24)) / (60 * 60));
            var minutes = parseInt((mss % (60 * 60)) / 60);
            var seconds = parseInt((mss % (60)));
            return hours + "小时" + minutes + "分钟" + seconds.toFixed(0) + "秒";
        },
        energyData : function (data) {
        	if (data !== null && data !== undefined) {
        		$('#allRunMileage').attr('value', Number(data.travelMile) + ' km');
                $('#allRunOil').attr('value', Number(data.travelTotal) + ' L');
                $('#runOilCriterion').attr('value', Number(data.travelBaseList) + ' L/100km');
                $('#averageSpeed').attr('value', Number(data.avgSpeed) + ' km/h');
                if(data.travelTotalCap==null){
                    $('#coTotalCap').attr('value', '');
                    $('#coBaseCap').attr('value','');
				}else{
                    $('#coTotalCap').attr('value', Number(data.travelTotalCap) + ' kg');
                    $('#coBaseCap').attr('value', Number(data.travelBaseCap)+ ' kg/100km');
				}

                $('#dataStatistics').removeClass('hidden');
        	}
        },
        // 时间戳转换日期         
        UnixToDate: function(unixTime, isFull, timeZone) {
            if (typeof (timeZone) == 'number') {
                unixTime = parseInt(unixTime) + parseInt(timeZone) * 60 * 60;
            }
            var time = new Date(unixTime * 1000);
            var ymdhis = "";
            ymdhis += time.getFullYear() + "-";
            ymdhis += (time.getMonth() + 1) + "-";
            ymdhis += time.getDate();
            if (isFull === true) {
                ymdhis += " " + time.getHours() + ":";
                ymdhis += time.getMinutes() + ":";
                ymdhis += time.getSeconds();
            }
            return ymdhis;
        },
        showClick: function () {
            if ($('#showClick').hasClass("fa-chevron-up")) {
                $('#showClick').attr("class", "fa fa-chevron-down");
                $("#graphShow").slideDown(300);
            } else {
                $('#showClick').attr("class", "fa fa-chevron-up");
                $("#graphShow").slideUp('300');
            }
        },
        draggle: function () {
            $("#showClick").attr("class", "fa fa-chevron-down");
            $("#graphShow").show();
        },
        // datatable数据加载
        exampleTable: function (data) {
        	if (data.length > 0) {
        		var html = '';
        		for (var i = 0, len = data.length; i < len; i++) {
        			html += '<tr>'
        			for (var j = 0, jlen = data[i].length; j < jlen; j++) {
        				if (permission == 'false'){
                            html += '<td>'+ data[i][j] +'</td>';
						}else{
                            if (j !== 1) {
                                html += '<td>'+ data[i][j] +'</td>';
                            } else {
                                html += '<td><button  type="button" onclick="travelEnergyList.removeCriterionData(\'' + data[i][j] + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button></td>';
                            }
						}
        			}
        			html += '</tr>'
        		}
                if (permission == 'false' && $('#criterionDataTable thead tr th').length == 13){
                    $('#criterionDataTable thead tr th:eq(1)').remove();
                }
        		$('#criterionDataTable tbody').html(html);
        		$('#dataStatistics').removeClass('hidden');
        	} else {
                $('#criterionDataTable tbody').html('');
			}
        },
        // 基准数据删除
        removeCriterionData: function(id) {
        	layer.confirm(publicDelete, {btn : ["确定", "取消"],icon: 3,title: "操作确认"}, function(){
            	// 传入后台删除数据
            	var url="/clbs/m/energy/travelEnergy/delete";
                var parameter={"id": id,"vehicleId": vehicleId};
                json_ajax("POST", url, "json", true, parameter, travelEnergyList.criterionDelete);
    		});
        },
        // 基准数据删除回掉函数
        criterionDelete: function(data) {
        	if (data.success) {
        		layer.msg(data.msg);
        		//var vehicleId = $('#charSelect').attr('data-id');
        		var url="/clbs/m/energy/travelEnergy/travelEnergyList";
                var parameter={"vehicleId": vehicleId};
                json_ajax("POST", url, "json", true, parameter, travelEnergyList.getCriterionData);
        	} else {
        		layer.msg(data.msg);
        	}
        },
        /*// 保存列表数据
        saveCriterionData: function() {

        	//,vehicleId = $('#charSelect').attr('data-id')
    		var url="/clbs/m/energy/travelEnergy/addEnergyBase"
            ,parameter={'vehicleId': vehicleId};
            json_ajax("POST", url, "json", true, parameter, travelEnergyList.saveCriterionFun);
        },
        // 保存列表数据回掉函数
        saveCriterionFun: function(data) {
        	if (data.success) {
        		layer.msg('保存成功');
        	} else {
        		layer.msg('保存失败');
        	}
            travelEnergyList.getEnergy();
        },
        getEnergy : function(){
            var url="/clbs/m/energy/travelEnergy/getEnergy",parameter={'vehicleId': vehicleId}
            json_ajax("POST", url, "json", true, parameter, travelEnergyList.getEnergyCallBack);
        },
        getEnergyCallBack : function (data) {
            var energyData = data.obj.energy; // table下面统计数据
            travelEnergyList.energyData(energyData);
        },*/
        // 清空数据
        clearData: function() {
        	echartsDate = [];
            oilConsume = [];
            allMileage = [];
            echartsSpeed = [];
            accState = [];
            y_oil_before = null;
            x_oil_before = null;
            y_oil_after = null;
            x_oil_after = null;
            $('#criterionDataTable tbody').html('');
            $('#allRunMileage').attr('value', '');
            $('#allRunOil').attr('value', '');
            $('#runOilCriterion').attr('value', '');
            $('#averageSpeed').attr('value', '');
            $('#coTotalCap').attr('value', '');
            $('#coBaseCap').attr('value', '');
            $('#criterionStartTime, #criterionEndTime').attr('value', '');
            $('#addCriterionData').attr('class', 'form-horizontal table-top hidden');
        },
        goBack: function(GeocoderResult){
        	msgArray = GeocoderResult;
    		var $dataTableTbody = $("#oilTable tbody");
      	  	var dataLength = $dataTableTbody.children("tr").length;
      	  	for(var i = 0; i < dataLength; i++){
      	  		if(msgArray[i] != undefined){
	      	  		$dataTableTbody.children("tr:nth-child("+ (i+1) +")").children("td:nth-child(9)").text(msgArray[i][0]);
	  	  			$dataTableTbody.children("tr:nth-child("+ (i+1) +")").children("td:nth-child(10)").text(msgArray[i][1]);
      	  		}
  	  		}
        },
        //对显示的数据进行逆地址解析
        getAddress: function(){
    		var $dataTableTbody = $("#oilTable tbody");
      	  	var dataLength = $dataTableTbody.children("tr").length;
      	  	var num = 0;
    		for(var i = 0; i < dataLength; i++){
    			num++;
  	  			var n = $dataTableTbody.children("tr:nth-child("+ (i+1) +")").children("td:nth-child(1)").text();
		  		var startMsg = [];
				var endMsg = [];
				//经纬度正则表达式
				var Reg = /^(180\.0{4,7}|(\d{1,2}|1([0-7]\d))\.\d{4,20})(,)(90\.0{4,8}|(\d|[1-8]\d)\.\d{4,20})$/;
				if(startLoc[n-1] != null && Reg.test(startLoc[n-1])){
					startMsg = [startLoc[n-1].split(",")[0],startLoc[n-1].split(",")[1]];
				}else{
					startMsg = ["124.411991","29.043817"];
				};
				if(endLoc[n-1] != null && Reg.test(endLoc[n-1])){
					endMsg = [endLoc[n-1].split(",")[0],endLoc[n-1].split(",")[1]];
				}else{
					endMsg = ["124.411991","29.043817"];
				}
				addressMsg.push(startMsg);
				addressMsg.push(endMsg);
				if(num == dataLength){
					var addressIndex = 0;
					var addressArray = [];
					backAddressMsg(addressIndex,addressMsg,travelEnergyList.goBack,addressArray);
					addressMsg = [];
				}
  	  		};
        }, 
        toHHMMSS: function(data){
            var totalSeconds=data*60*60;
            var  hour =  Math.floor(totalSeconds/60/60);
            var minute = Math.floor(totalSeconds/60%60);
            var second = Math.floor(totalSeconds%60);
            return hour+"小时"+minute+"分钟"+second+"秒"
        },
        timeStamp2String: function(time){
        	var time = time.toString();
        	var startTimeIndex = time.replace("-","/").replace("-","/");
            var val = Date.parse(startTimeIndex);
            var datetime = new Date();
            datetime.setTime(val);
            var year = datetime.getFullYear();
            var month = datetime.getMonth() + 1 < 10 ? "0" + (datetime.getMonth() + 1) : datetime.getMonth() + 1;
            var date = datetime.getDate() < 10 ? "0" + datetime.getDate() : datetime.getDate();
            var hour = datetime.getHours() < 10 ? "0" + datetime.getHours() : datetime.getHours();
            var minute = datetime.getMinutes() < 10 ? "0" + datetime.getMinutes() : datetime.getMinutes();
            var second = datetime.getSeconds() < 10 ? "0" + datetime.getSeconds() : datetime.getSeconds();
            return year + "-" + month + "-" + date + " " + hour + ":" + minute + ":" + second;
        },
        timeAdd: function(time){
            var str = time.toString();
            str = str.replace(/-/g, "/");
            return new Date(str);
        },
        GetDateDiff: function(startTime, endTime, diffType){
            // 将xxxx-xx-xx的时间格式，转换为 xxxx/xx/xx的格式
            startTime = startTime.replace(/-/g, "/");
            endTime = endTime.replace(/-/g, "/");
            // 将计算间隔类性字符转换为小写
            diffType = diffType.toLowerCase();
            var sTime = new Date(startTime); // 开始时间
            var eTime = new Date(endTime); // 结束时间
            // 作为除数的数字
            var divNum = 1;
            switch (diffType) {
                case "second":
                    divNum = 1000;
                    break;
                case "minute":
                    divNum = 1000 * 60;
                    break;
                case "hour":
                    divNum = 1000 * 3600;
                    break;
                case "day":
                    divNum = 1000 * 3600 * 24;
                    break;
                default:
                    break;
            }
            return parseFloat((eTime.getTime() - sTime.getTime()) / parseInt(divNum)); //
        },
        //过滤数组空值
        filterTheNull: function(value){
            for (var i = 0; i < value.length; i++) {
                if (value[i] == null || value[i] == "" || typeof(value[i]) == "undefined") {
                    value.splice(i, 1);
                    i = i - 1;
                }
            }
            return value
        },
	    left_arrow: function(){
            $("#button.dropdown-toggle").click();
            $("#button.dropdown-toggle").click();
	        var trIndex = $(".table-condensed tr").size() - 1;
	        var nowIndex = 0;
	        $(".table-condensed tr").each(function(){
	            if($(this).attr("data-id") == $("input[name='charSelect']").attr("data-id")){
	                nowIndex = $(this).attr("data-index");
	            }
	        })
	        if (0 == nowIndex) {
	            $("input[name='charSelect']").attr("data-id",$(".table-condensed tr").eq(trIndex).attr("data-id"));
	            $("input[name='charSelect']").val($(".table-condensed tr").eq(trIndex).attr("data-key"));
	        }else {
	            $("input[name='charSelect']").attr("data-id",$(".table-condensed tr").eq(nowIndex-1).attr("data-id"));
	            $("input[name='charSelect']").val($(".table-condensed tr").eq(nowIndex-1).attr("data-key"));
	        }
	        $("#inquireClick").click();
	    },
	    right_arrow: function(){
            $("#button.dropdown-toggle").click();
            $("#button.dropdown-toggle").click();
	        var trIndex = $(".table-condensed tr").size() - 1;
	        var nowIndex = 0;
	        $(".table-condensed tr").each(function(){
	            if($(this).attr("data-id") == $("input[name='charSelect']").attr("data-id")){
	                nowIndex = $(this).attr("data-index");
	            }
	        })
	        if (trIndex == nowIndex) {
	            $("input[name='charSelect']").attr("data-id",$(".table-condensed tr").eq(0).attr("data-id"));
	            $("input[name='charSelect']").val($(".table-condensed tr").eq(0).attr("data-key"));
	        }else {
	            var nextIndex = parseInt(nowIndex)+1;
	            $("input[name='charSelect']").attr("data-id",$(".table-condensed tr").eq(nextIndex).attr("data-id"));
	            $("input[name='charSelect']").val($(".table-condensed tr").eq(nextIndex).attr("data-key"));
	        }
	        $("#inquireClick").click();
	    },
	    endTimeClick: function(){
	        var width = $(this).width();
	        var offset = $(this).offset();
	        var left = offset.left - (207 - width);
	        $("#laydate_box").css("left", left + "px");
	    }
    }
    $(function () {
        $("#toggle-left").bind("click", function () {
            setTimeout(function () {
            	travelEnergyList.echartsInit();
            }, 500)
        });
        Array.prototype.isHas = function (a) {
            if (this.length === 0) {
                return false
            }
            ;
            for (var i = 0; i < this.length; i++) {
                if (this[i].seriesName === a) {
                    return true
                }
            }
        };
        travelEnergyList.treeInit();
        travelEnergyList.getsTheCurrentTime();
        $('#timeInterval').dateRangePicker();
        $("#todayClick").bind("click", travelEnergyList.todayClick);
        $("#right-arrow").bind("click", travelEnergyList.yesterdayClick);
        $("#nearlyThreeDays").bind("click", travelEnergyList.nearlyThreeDays);
        $("#nearlySevenDays").bind("click", travelEnergyList.nearlySevenDays);
        $("#showClick,#graphAreaHead").bind("click", travelEnergyList.showClick);
        $("#left-arrow").bind("click",travelEnergyList.upDay);
	    $("#endTime").bind("click",travelEnergyList.endTimeClick);
	    $("#groupSelect").bind("click",showMenuContent); //组织下拉显示
	    
	    
	    $('#criterionDataAdd').on('click', travelEnergyList.criterionDataAddFun);
	    $('#saveCriterionData').on('click', travelEnergyList.saveCriterionData);
	    if (permission == 'false'){
	    	$("#addCriterionData button").remove();
		}

        $('input').inputClear().on('onClearEvent',function(e,data){
            var id = data.id;
            if(id == 'groupSelect'){
                var param = $("#groupSelect").val();
                travelEnergyList.searchVehicleTree(param);
            };
        });

        /**
         * 监控对象树模糊查询
         */
        var inputChange;
        $("#groupSelect").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            };
            if (!(window.ActiveXObject || "ActiveXObject" in window)){
                isSearch = true;
            };
            inputChange = setTimeout(function () {
                if(isSearch) {
                    var param = $("#groupSelect").val();
                    travelEnergyList.searchVehicleTree(param);
                }
                isSearch = true;
            }, 500);
        });

    });
})($, window);