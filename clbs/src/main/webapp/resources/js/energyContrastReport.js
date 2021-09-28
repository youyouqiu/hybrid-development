if (!Array.prototype.find) {
  Object.defineProperty(Array.prototype, 'find', {
    value: function(predicate) {
     // 1. Let O be ? ToObject(this value).
      if (this == null) {
        throw new TypeError('"this" is null or not defined');
      }

      var o = Object(this);

      // 2. Let len be ? ToLength(? Get(O, "length")).
      var len = o.length >>> 0;

      // 3. If IsCallable(predicate) is false, throw a TypeError exception.
      if (typeof predicate !== 'function') {
        throw new TypeError('predicate must be a function');
      }

      // 4. If thisArg was supplied, let T be thisArg; else let T be undefined.
      var thisArg = arguments[1];

      // 5. Let k be 0.
      var k = 0;

      // 6. Repeat, while k < len
      while (k < len) {
        // a. Let Pk be ! ToString(k).
        // b. Let kValue be ? Get(O, Pk).
        // c. Let testResult be ToBoolean(? Call(predicate, T, « kValue, k, O »)).
        // d. If testResult is true, return kValue.
        var kValue = o[k];
        if (predicate.call(thisArg, kValue, k, o)) {
          return kValue;
        }
        // e. Increase k by 1.
        k++;
      }

      // 7. Return undefined.
      return undefined;
    }
  });
};
(function(window,$){
	var myTable;
	// var begin;
	// var over;
	//车辆列表
	var vehicleList=[];
	//车辆id列表
	var vehicleId=[];
	//基准列表
	var jizhunList=JSON.parse($("#jizhunList").attr("value"));;
	// 存储切换列显示
	//var toggleColumn=[];

	var pushToggleColumn=function(v){
		var w=getToggleColumn()
		w.push(v)
		w=JSON.stringify(w)
		window.sessionStorage.setItem('ennery_contrast_report_cus_column',w)
	};
	var changeToggleColumn=function(k,v){
		var w=getToggleColumn()
		w[k]=v
		w=JSON.stringify(w)
		window.sessionStorage.setItem('ennery_contrast_report_cus_column',w)
	};
	//window.sessionStorage.clear()
	var getToggleColumn=function(){
		var w=window.sessionStorage.getItem('ennery_contrast_report_cus_column')
		if(!w){
			w=[]
		}else{
			w=JSON.parse(w)
		}
		return w
	};
	//开始时间
	var startTime;
	//结束时间
	var endTime;
	var checkFlag = false; //判断组织节点是否是勾选操作
	var size;//当前权限监控对象数量

    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于100自动勾选
    var isSearch = true;

	energyContrastReport = {
    	init: function(){
            //显示隐藏列
            var menu_text = "";
            var table = $("#dataTable tr:eq(1) th:gt(1)");
            var toggleColumn=getToggleColumn()
            var makeText=function(index,text){
//            	toggleColumn.push({
//            		index:index,
//            		visible:true
//            	})
//            	pushToggleColumn({
//            		index:index,
//            		visible:true
//            	})
            	var checkedStr=''
            	var found=false
            	for(var i=0;i<toggleColumn.length;i++){
            		if(toggleColumn[i].index==index){
            			found=true
            			if(toggleColumn[i].visible){
            				checkedStr='checked="checked"'
            			}
            		}
            	}
            	if(!found){
            		pushToggleColumn({
            			index:index,
            			visible:true
            		})
            		checkedStr='checked="checked"'
            	}
            	return  "<li><label><input type=\"checkbox\"  "+checkedStr+" class=\"toggle-vis\" data-column=\"" + index +"\" />"+ text +"</label></li>"
            }
            menu_text += makeText(3,'所属企业');
            menu_text += makeText(4,'行驶时长');
            menu_text += makeText(7,'行驶能耗基准');
            menu_text += makeText(8,'行驶空调时长');
            menu_text += makeText(9,'行驶空调能耗基准');
            menu_text += makeText(13,'怠速能耗基准');
            menu_text += makeText(14,'怠速空调时长');
            menu_text += makeText(15,'怠速空调能耗基准');
            menu_text += makeText(20,'标准煤');
            menu_text += makeText(21,'CO2(kg)');
            menu_text += makeText(22,'SO2(kg)');
            menu_text += makeText(23,'NOX(kg)');
            menu_text += makeText(24,'HCX(kg)');
            $("#Ul-menu-text").html(menu_text);
            var $keepOpen = $('.keep-open');
            
            $keepOpen.find('li').off('click').on('click', function (event) {
                 event.stopImmediatePropagation();
             });
            
			//车辆树
			var setting = {
				async: {
					url: energyContrastReport.getenergyContrastReportTreeUrl,
					type: "post",
					enable: true,
					autoParam: ["id"],
					dataType: "json",
					otherParam: {"type": "multiple","icoType": "0"},
					dataFilter: energyContrastReport.ajaxDataFilter

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
					beforeClick: energyContrastReport.beforeClickVehicle,
                    onAsyncSuccess: energyContrastReport.zTreeOnAsyncSuccess,
                    beforeCheck: energyContrastReport.zTreeBeforeCheck,
					onCheck: energyContrastReport.onCheckVehicle,
					onExpand: energyContrastReport.zTreeOnExpand
				}
			};
			$.fn.zTree.init($("#treeDemo"), setting, null);
        },
        //模糊查询树
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选

            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                energyContrastReport.treeInit();
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
                        dataFilter: energyContrastReport.ajaxQueryDataFilter
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
                        beforeClick: energyContrastReport.beforeClickVehicle,
                        onCheck: energyContrastReport.onCheckVehicle,
                        onExpand: energyContrastReport.zTreeOnExpand,
                        onNodeCreated: energyContrastReport.zTreeOnNodeCreated
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
        getenergyContrastReportTreeUrl : function (treeId, treeNode){
        	if (treeNode == null) {
        		return "/clbs/m/personalized/ico/vehicleTree";
        	}else if(treeNode.type == "assignment") {
        		return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId="+treeNode.id+"&isChecked="+treeNode.checked+"&monitorType=vehicle";
        	} 
        },
        //设置开始时间和结束时间，
        //开始时间是向前减天数来完成，比如-1代表向前一天，借宿时间则是正数，如1代表后一天
        //如果不传endDay,代表结束日期为今天
        startDay: function (day,endDay) {
        	var timeInterval = $('#timeInterval').val().split('--');
        	var startValue = timeInterval[0];
        	var endValue = timeInterval[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * day;
                today.setTime(targetday_milliseconds); //注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = energyContrastReport.doHandleMonth(tMonth + 1);
                tDate = energyContrastReport.doHandleMonth(tDate);
                var num = -(day + 1);
                if(endDay){
                	num=endDay
                }
                startTime = tYear + "-" + tMonth + "-" + tDate + " "
                    + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = energyContrastReport.doHandleMonth(endMonth + 1);
                endDate = energyContrastReport.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " "
                    + "23:59:59";
            } else {
            	var startTimeIndex = startValue.slice(0,10).replace("-","/").replace("-","/");
            	var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
            	if(endDay===null){
            		vtoday_milliseconds = (new Date()).getTime() + 1000 * 60 * 60 * 24 * day;
            	}
            	var dateList = new Date();
            	dateList.setTime(vtoday_milliseconds);
            	var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
            	var vDate = dateList.getDate();
                vMonth = energyContrastReport.doHandleMonth(vMonth + 1);
                vDate = energyContrastReport.doHandleMonth(vDate);
                startTime = vYear + "-" + vMonth + "-" + vDate + " "
                    + "00:00:00";
                if(day == 1){
                	endTime = vYear + "-" + vMonth + "-" + vDate + " "
                    + "23:59:59";
                }else{
                	var endNum = -1;
                	if(endDay){
                		endNum=endDay
                    }
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    if(endDay===null){
                    	vendtoday_milliseconds = (new Date()).getTime();
                	}
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = energyContrastReport.doHandleMonth(vendMonth + 1);
                    vendDate = energyContrastReport.doHandleMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate + " "
                        + "23:59:59";
                }
            }
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
            var atime = $("#atime").val();
            if(atime!=undefined && atime!=""){
            	startTime = atime;
            }
        },
        getPrevMonth: function () {
        	var timeInterval = $('#timeInterval').val().split('--');
        	var startValue = timeInterval[0];
        	var startTimeIndex = startValue.slice(0,10).replace("-","/").replace("-","/");
        	var d=new Date(startTimeIndex)
        	d.setMonth(d.getMonth()-1)
        	startTime = d.getFullYear() + "-" + (d.getMonth()+1) + "-" + "01" + " "
            + "00:00:00";
        	d.setMonth(d.getMonth()+1)
        	d.setDate(0)
        	
        	endTime = d.getFullYear() + "-" + (d.getMonth()+1) + "-" + d.getDate() + " "
                + "23:59:59";
        	
        },
        inquireClick: function (number) {
        	
        	var vehicleId = $('#charSelect').attr("data-id"); // 车id
            if(vehicleId == "" || vehicleId == null){
                layer.msg(monitoringObjecNull);
                return;
            }
        	if(number==0){ 
        		energyContrastReport.getsTheCurrentTime();
        	}else if(number==-1){
        		energyContrastReport.startDay(-1)
        	}else if(number==-30){//当月
        		var today=new Date();
        		var toReduceDay=- today.getDate()+1; // 需要减的天数
        		energyContrastReport.startDay(toReduceDay,null)
        	}else if(number ==-60){//前一月
//        		var today=new Date();
//        		var toReduceEndDay=- today.getDate(); // 需要减的结束天数
//        		var prevMonth=new Date(today.getFullYear(),today.getMonth(),0)
//        		var toReduceStartDay=- today.getDate()-prevMonth.getDate()+1; // 需要减的开始天数
//        		energyContrastReport.startDay(toReduceStartDay,toReduceEndDay)
        		energyContrastReport.getPrevMonth();
        	}
        	if(number != 1){
        		$('#timeInterval').val(startTime + '--' + endTime);
        		startTime=startTime;
            	endTime=endTime;
        	}else{
        		var timeInterval = $('#timeInterval').val().split('--');
            	startTime = timeInterval[0];
            	endTime = timeInterval[1];
        	};
        	startTime=startTime.substr(0,startTime.length-3)
        	endTime=endTime.substr(0,endTime.length-3)
        	energyContrastReport.getCheckedNodes();
        	if(!energyContrastReport.validates()){
        		return;
        	};
        	//查询范围不能超过31天
        	var startDate=new Date(startTime.replace(/-/g,'/'))//因为Safari不支持yyyy-MM-dd,只支持yyyy/MM/dd
        	var endDate=new Date(endTime.replace(/-/g,'/'))
        	var daysDiff=Math.floor((endDate.getTime()-startDate.getTime())/(24*3600*1000))
        	if(daysDiff>30){
        		layer.msg(timeScopeDay);
                return;
        	}
        	var v=jizhunList.find(function(ele){
        		return ele.vehicleId==vehicleId
        	})
        	if(v.installTime){
        		var selectedDate=new Date(startTime.replace(/-/g,'/'))
            	var limitDate=new Date()
            	limitDate.setTime(v.installTime)
            	if(selectedDate<limitDate){
                    layer.msg("选择日期不能早于节油产品安装日期 "+energyContrastReport.formatTime(v.installTime,true));
                    return;
                }
        	}
        	var url="/clbs/m/energy/energyContrast/getInfo";
            var parameter={"vehicleId":vehicleId,"startTime":startTime,"endTime":endTime};
            json_ajax("POST",url,"json",true,parameter,energyContrastReport.getCallback);
        },
        exportAlarm:function(){
        	energyContrastReport.getCheckedNodes();
        	if(!energyContrastReport.validates()){
        		return;
        	};
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
        	/*var timeInterval = $('#timeInterval').val().split('--');
        	startTime = timeInterval[0];
        	endTime = timeInterval[1];
        	var url="/clbs/m/reportManagement/energyContrastReport/export";
        	var parameter={"vehicleId":vehicleId,"startTime":startTime,"endTime":endTime,"vehicleList":vehicleList};
            json_ajax("POST",url,"json",true,parameter,energyContrastReport.exportCallback);*/
        	var url="/clbs/m/energy/energyContrast/export";
			window.location.href=url;
        },
        validates: function(){
            return $("#speedlist").validate({
            	rules: {
                	startTime:{
	                       required:true
	                },               
                    endTime: {
                    	required:true,
                    	compareDate: "#timeInterval",
                    },
	                groupSelect:{
	                	zTreeChecked:"treeDemo"
	                }
                },
                messages: {
                	startTime:{
	                      required:publicSelectStartDate,
	                },                
                    endTime: {
                    	required:publicSelectEndDate,
                    	compareDate: publicSelectThanDate,
                    },
                    groupSelect:{
	                	zTreeChecked:vehicleSelectBrand,
	                }
                }
            }).form();
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
		getCharSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true)
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
            var vehName = energyContrastReport.unique(veh);
            var vehId = energyContrastReport.unique(vid);
            $("#charSelect").empty();
            var deviceDataList = {value: []};
            for (var j = 0; j < vehName.length; j++){
            	for(var k=0;k<jizhunList.length;k++){
					if(vehId[j]==jizhunList[k].vehicleId){
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

            	var v=jizhunList.find(function(ele){
            		return ele.vehicleId==keyword.id
            	})
            	
            	var installTime=v.installTime;
            	var date=new Date();
            	date.setTime(installTime)
            	if(installTime!=null&&installTime!=undefined){;
            	}
            }).on('onUnsetSelectValue', function () {
            });
            if(deviceDataList.value.length > 0){
				$("#charSelect").val(deviceDataList.value[0].name).attr("data-id",deviceDataList.value[0].id);
				var v=jizhunList.find(function(ele){
            		return ele.vehicleId==deviceDataList.value[0].id
            	})
				var installTime=v.installTime;
            	var date=new Date();
            	date.setTime(installTime)
            	if(installTime!=null&&installTime!=undefined){
            		//$('#timeInterval').dateRangePicker({start_date:date});
            	}
			}
            $("#groupSelect,#groupSelectSpan").bind("click",energyContrastReport.showMenu);
            $("#button").removeClass('disabled loading-state-button').find('i').attr("class", 'caret');
        },
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
    	    }
    	    return data;
		},
		beforeClickVehicle: function(treeId, treeNode) {
	           var zTree = $.fn.zTree.getZTreeObj("treeDemo");
	           zTree.checkNode(treeNode, !treeNode.checked, true, true);
	           return false;
	   },
       zTreeOnAsyncSuccess: function(event, treeId, treeNode, msg){
    	   var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
    	   if(size <= 5000){
    		   treeObj.checkAllNodes(true);
           }
    	   if(!($("#groupSelect").val())){
				var nodes = treeObj.getNodes();
				$("#groupSelect").val(nodes[0].name);
    	   }
    	   energyContrastReport.getCharSelect(treeObj);
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
		onCheckVehicle : function(e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                isSearch = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
            }

            energyContrastReport.getCheckedNodes();

            if (treeNode.type != "assignment" || (treeNode.type == "assignment" && treeNode.children != undefined)) {
                energyContrastReport.getCharSelect(zTree);
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
	             	 var assign = []; // 当前组织及下级组织的所有分组
	             	 energyContrastReport.getGroupChild(treeNode,assign);
	             	 if (assign != null && assign.length > 0) {
	             		 for (var i=0;i<assign.length;i++) {
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
	       getGroupChild: function (node,assign) { // 递归获取组织及下级组织的分组节点
		       	var nodes = node.children;
		       	if (nodes != null && nodes != undefined && nodes.length > 0){
		       		for (var i= 0; i < nodes.length; i++) {
		       			var node = nodes[i];
		       			if (node.type == "assignment") {
		       				assign.push(node);
		       			}else if (node.type == "group" && node.children != undefined){
		       				energyContrastReport.getGroupChild(node.children,assign);
		       			}
		       		}
		       	}
	      },
	    getCheckedNodes : function() {
				var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
						.getCheckedNodes(true), v = "",vid="";
				for (var i = 0, l = nodes.length; i < l; i++) {
					if (nodes[i].type == "vehicle") {
						v += nodes[i].name + ",";
						vid+=nodes[i].id+",";
					}
				}
				vehicleList = v;
				vehicleId=vid;
		},
		// 格式化timestamp为YYYY/MM/DD HH:mm:ss 格式，传入onlyDate为true，则格式化为YYYY/MM/DD
		formatTime :function(dateStr,onlyDate) {
				var formatNumber = function(n) {
				  n = n.toString()
				  return n[1] ? n : '0' + n
				}
				var date=new Date();
				date.setTime(dateStr)
				var year = date.getFullYear()
				var month = date.getMonth() + 1
				var day = date.getDate()
				var hour = date.getHours()
				var minute = date.getMinutes()
				var second = date.getSeconds()

				if(onlyDate){
					return [year, month, day].map(formatNumber).join('/') 
				}else{
					return [year, month, day].map(formatNumber).join('/') + ' ' + [hour, minute, second].map(formatNumber).join(':')
				}
		},
		getCallback:function(data){
			if(data.success==true){
        		dataListArray = [];//用来储存显示数据
        		if(data.obj != null && data.obj.length != 0){
        		 	var energyContrastReportList = data.obj//集合
        		 	//格式化数字的工具
        		 	//参数的意思为：要处理的对象，保留的位，是否省略0
        		 	var toFixed=function(source,digit,omitZero){
        		 		if(typeof source == 'number'){
        		 			var afterFixed=source.toFixed(digit) //此时 afterFixed 为string类型
        		 			if(omitZero){
        		 				afterFixed=parseFloat(afterFixed)
        		 			}
        		 			return afterFixed
        		 		}
        		 	}
        		 	var oneDigit=['travelMile','travelFuel','idleFuel','totalFuel','totalBase','travelEnergyFuel','idleEnergyFuel','fuel','standardCoal','co2Amount'];
        		 	var threeDigit=['travelBaseFuel','idleAirBaseFuel','idleAirBaseFuel','travelBaseFuel','idleBaseFuel','so2Amount','noxAmount','hcxAmount'];

        		 	for(var i=0;i<energyContrastReportList.length;i++){
        		 		for(var k in energyContrastReportList[i]){
        		 			var _item=energyContrastReportList[i][k];
        		 			if(_item!=null){
            		 			try{
            		 				if(oneDigit.indexOf(k)>=0){
            		 					energyContrastReportList[i][k]=toFixed(_item,1,true)
            		 				}else if(threeDigit.indexOf(k)>=0){
            		 					energyContrastReportList[i][k]=toFixed(_item,3,true)
            		 				}
            		 			}catch(e){
            		 			}
            		 		}
        		 		}
        		 	}
        			for(var i=0;i<energyContrastReportList.length;i++){
        					var s=0;

        					var dateList=
        						[
        						 s++,
        						 energyContrastReportList[i].date,				energyContrastReportList[i].brand,
        						 energyContrastReportList[i].groupName, 		energyContrastReportList[i].formatTravelTime,
        						 energyContrastReportList[i].travelMile,		energyContrastReportList[i].travelFuel,
        						 energyContrastReportList[i].travelBaseFuel, 	energyContrastReportList[i].formatTravelAirOpenTime,
        						 energyContrastReportList[i].travelAirBaseFuel, energyContrastReportList[i].travelEnergyFuel,
        						 energyContrastReportList[i].formatIdleTime,  	energyContrastReportList[i].idleFuel,
        						 energyContrastReportList[i].idleBaseFuel,		energyContrastReportList[i].formatIdleAirOpenTime,
        						 energyContrastReportList[i].idleAirBaseFuel,  	energyContrastReportList[i].idleEnergyFuel,
        						 energyContrastReportList[i].totalFuel,			energyContrastReportList[i].totalBase,
        						 energyContrastReportList[i].fuel,  			energyContrastReportList[i].standardCoal,
        						 energyContrastReportList[i].co2Amount,			energyContrastReportList[i].so2Amount,
        						 energyContrastReportList[i].noxAmount,  		energyContrastReportList[i].hcxAmount,
        						 ];
        					dataListArray.push(dateList);
        			}
       
        			energyContrastReport.getTable('#dataTable',dataListArray);
        			$('#dataTable tr:last td:eq(0)').attr('colspan','2').html('合计')
        			$('#dataTable tr:last td:eq(1)').remove()
//        			var sumEle=energyContrastReportList[energyContrastReportList.length-1]
//        			var tds=$('#sumTr td');
//        			$(tds[1]).html(sumEle.brand);			            $(tds[2]).html(sumEle.groupName);
//        			$(tds[3]).html(sumEle.formatTravelTime);			$(tds[4]).html(sumEle.travelMile);
//        			$(tds[5]).html(sumEle.travelFuel);			        $(tds[6]).html(sumEle.travelBaseFuel);
//        			$(tds[7]).html(sumEle.formatTravelAirOpenTime);	    $(tds[8]).html(sumEle.travelAirBaseFuel);
//        			$(tds[9]).html(sumEle.travelEnergyFuel);	        $(tds[10]).html(sumEle.formatIdleTime);
//        			$(tds[11]).html(sumEle.idleFuel);			        $(tds[12]).html(sumEle.idleBaseFuel);
//        			$(tds[13]).html(sumEle.formatIdleAirOpenTime);	    $(tds[14]).html(sumEle.idleAirBaseFuel);
//        			$(tds[15]).html(sumEle.idleEnergyFuel);		        $(tds[16]).html(sumEle.totalFuel);
//        			$(tds[17]).html(sumEle.totalBase);			        $(tds[18]).html(sumEle.fuel);
//        			$(tds[19]).html(sumEle.standardCoal);		        $(tds[20]).html(sumEle.co2Amount);
//        			$(tds[21]).html(sumEle.so2Amount);			        $(tds[22]).html(sumEle.noxAmount);
//        			$(tds[23]).html(sumEle.hcxAmount);
        		}else{
        			energyContrastReport.getTable('#dataTable',dataListArray);
//        			var sumEle={}
//        			var tds=$('#sumTr td');
//        			$(tds[3]).html('');			$(tds[4]).html('');
//        			$(tds[5]).html('');			$(tds[6]).html('');
//        			$(tds[7]).html('');		$(tds[8]).html('');
//        			$(tds[9]).html('');	$(tds[10]).html('');
//        			$(tds[11]).html('');			$(tds[12]).html('');
//        			$(tds[13]).html('');	$(tds[14]).html('');
//        			$(tds[15]).html('');		$(tds[16]).html('');
//        			$(tds[17]).html('');			$(tds[18]).html('');
//        			$(tds[19]).html('');		$(tds[20]).html('');
//        			$(tds[21]).html('');			$(tds[22]).html('');
//        			$(tds[23]).html('');
        		}	 
        	}else{
				if(data.msg != null){
					layer.msg(data.msg,{move:false});
				}
			}
		},
		exportCallback:function(date){
			if(date==true){
				var url="/clbs/m/reportManagement/energyContrastReport/export";
				window.location.href=url;
			}else{
				layer.msg(exportFail,{move:false});
			}
		},
		getTable: function(table, data){
            myTable = $(table).DataTable({
              "destroy": true,
              "dom": 'tiprl',// 自定义显示项
              "data": data,
              "lengthChange": false,// 是否允许用户自定义显示数量
              "bPaginate": false, // 翻页功能
              "bFilter": false, // 列筛序功能
              "searching": true,// 本地搜索
              "ordering": false, // 排序功能
              "Info": false,// 页脚信息
              "autoWidth": true,// 自动宽度
			  "stripeClasses" : [],
			  "lengthMenu" : [ 50, 100, 200 ],
              "pagingType" : "simple_numbers", // 分页样式
              "dom" : "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
              "oLanguage": {// 国际语言转化
                  "oAria": {
                      "sSortAscending": " - click/return to sort ascending",
                      "sSortDescending": " - click/return to sort descending"
                  },
                  "sLengthMenu": "显示 _MENU_ 记录",
                  "sInfo": "",
                  "sZeroRecords": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                  "sEmptyTable": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                  "sLoadingRecords": "正在加载数据-请等待...",
                  "sInfoEmpty": "",
                  "sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
                  "sProcessing": "<img src='../resources/user_share/row_details/select2-spinner.gif'/> 正在加载数据...",
                  "sSearch": "模糊查询：",
                  "sUrl": "",
                  "oPaginate": {
                      "sFirst": "首页",
                      "sPrevious": " 上一页 ",
                      "sNext": " 下一页 ",
                      "sLast": " 尾页 "
                  }
              },
              "order": [
                  [0, null]
              ],// 第一列排序图标改为默认
	          });
	          myTable.on('order.dt search.dt', function () {
	              myTable.column(0, {
	                  search: 'applied',
	                  order: 'applied'
	              }).nodes().each(function (cell, i) {
	                  cell.innerHTML = i + 1;
	              });
	          }).draw();
	          //显示隐藏列
	          $('.toggle-vis').on('change', function (e) {
	        	  var checked=$(e.currentTarget||e.target).prop('checked')
	        	  var columnIndex=$(this).attr('data-column')
	        	  var toggleColumn=getToggleColumn()
	        	  for(var n=0;n<toggleColumn.length;n++){
	        		  if(toggleColumn[n].index==columnIndex){
	        			  changeToggleColumn(n,{
	        				  index:columnIndex,
	        				  visible:checked
	        			  })
	        			  //toggleColumn[n].visible=checked
	        		  }
	        	  }
	              var column = myTable.column(columnIndex);
	              column.visible(checked);
	          });

	          var toggleColumn=getToggleColumn()
	          for(var n=0;n<toggleColumn.length;n++){
        		  var column = myTable.column(toggleColumn[n].index);
        		  column.visible(toggleColumn[n].visible);
        	  }

	          $("#search_button").on("click",function(){
	              var tsval = $("#simpleQueryParam").val()
	              myTable.search(tsval, false, false).draw();
	          });
        },
    } 
    $(function(){
    	//初始化页面
    	energyContrastReport.init();
    	//当前时间
    	$('input').inputClear();
    	energyContrastReport.getsTheCurrentTime();
    	$('#timeInterval').dateRangePicker();
    	$("#groupSelect").bind("click",showMenuContent);
    	//导出
        $("#exportAlarm").bind("click",energyContrastReport.exportAlarm);
        energyContrastReport.getTable('#dataTable',[]);

        $('input').inputClear().on('onClearEvent',function(e,data){
            var id = data.id;
            if(id == 'groupSelect'){
                var param = $("#groupSelect").val();
                energyContrastReport.searchVehicleTree(param);
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
                    energyContrastReport.searchVehicleTree(param);
                }
                isSearch = true;
            }, 500);
        });

    })
})(window,$)