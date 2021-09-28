(function(window,$){
	var icoType=0;
	var vpId="";//人或车辆ID
	var selectList = []; // 勾选的监控对象及类型
	var pIcoTypeFlag = true;
	var setChar; // 树设置
	var hasBegun = [];
	var bflag = true;
	var zTreeIdJson = {};
	var checkFlag = false; //判断组织节点是否是勾选操作
	var size;//当前权限监控对象数量
	individuationIco = {
		init: function(){
			
        },
        icoTree :function(){
        	//组织树
        	setChar = {
                async: {
                    url: individuationIco.getIcoTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    sync:false,
                    otherParam: {"type": "multiple","icoType": icoType},
                    dataFilter: individuationIco.ajaxDataFilter
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
                    beforeClick: individuationIco.beforeClickVehicle,
                    onAsyncSuccess: individuationIco.zTreeOnAsyncSuccess,
                    beforeCheck: individuationIco.zTreeBeforeCheck,
                    onCheck: individuationIco.onCheckVehicle,
                    onExpand: individuationIco.zTreeOnExpand,
                    onNodeCreated: individuationIco.zTreeOnNodeCreated,
                }
            };
            $.fn.zTree.init($("#treeDemo"), setChar, null);
        },
        getIcoTreeUrl : function (treeId, treeNode){
        	if (treeNode == null) {
        		return "/clbs/m/functionconfig/fence/bindfence/alarmSearchTree";
        	}else if(treeNode.type == "assignment") {
        		return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId="+treeNode.id+"&isChecked="+treeNode.checked+"&monitorType=monitor";
        	} 
        },
        //组织树预处理加载函数
        ajaxDataFilter: function(treeId, parentNode, responseData) {
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
    	        	if (data[i].type == "group") {
    	        		data[i].open = true;
    	        	}
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
 			var zTree = $.fn.zTree.getZTreeObj(treeId);
        	// 更新节点数量
        	zTree.updateNodeCount(treeNode);
        	
        	if (treeNode === null) {
                return;
            }
            // 默认展开200个节点
            var initLen = 0;
            notExpandNodeInit = zTree.getNodesByFilter(assignmentNotExpandFilter);
            for (i = 0; i < notExpandNodeInit.length; i++) {
            	zTree.expandNode(notExpandNodeInit[i], true, true, false, true);
                initLen += notExpandNodeInit[i].children.length;
                if (initLen >= 200) {
                    break;
                }
            }
        	
        },
        zTreeOnExpand : function (event, treeId, treeNode) {
        	//判断是否是勾选操作展开的树(是则继续执行，不是则返回)
        	if(treeNode.type == "group" && !checkFlag){
        		return;
        	}
        	//初始化勾选操作判断表示
        	checkFlag = false;
        	 var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
        	 if (treeNode.type == "assignment" && treeNode.children === undefined) {
                 var url = "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign";
                 json_ajax("post", url, "json", false, {"assignmentId": treeNode.id,"isChecked":treeNode.checked,"monitorType":"monitor"}, function (data) {
                     var result = JSON.parse(ungzip(data.msg));
                     if (result != null && result.length > 0) {
                		 treeObj.addNodes(treeNode, result);
                	 }
                 })
             } else if (treeNode.type == "group"){
            	 var url = "/clbs/m/functionconfig/fence/bindfence/putMonitorByGroup";
                 json_ajax("post", url, "json", false, {"groupId": treeNode.id,"isChecked":treeNode.checked,"monitorType":"monitor"}, function (data) {
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
        	 if(vpId.length > 0){
        		 individuationIco.checkCurrentNodes(treeNode);
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
        				individuationIco.getGroupChild(node,assign);
        			}
        		}
        	}
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
	                            	layer.msg("限制校验异常！");
	                            }
	                        });
		            
		            //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
		            var ns = [];
		            //节点id
		            var nodeId;
		            for (var i=0;i<nodes.length;i++) {
		            	nodeId = nodes[i].id;
		                if(nodes[i].type == "people" || nodes[i].type == "vehicle" || nodes[i].type == "thing"){
		                	//查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
		                	var nd = zTree.getNodeByParam("tId",nodes[i].tId,treeNode);
		                	if(nd == null && $.inArray(nodeId,ns) == -1){
		                		ns.push(nodeId);
		                	}
		                }
		            }
		            nodesLength += ns.length;
            	}else if(treeNode.type == "people" || treeNode.type == "vehicle" ||  treeNode.type == "thing"){ //若勾选的为监控对象
            		var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                    	.getCheckedNodes(true), v = "";
		            var nodesLength = 0;
		            //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
		            var ns = [];
		            //节点id
		            var nodeId;
		            for (var i=0;i<nodes.length;i++) {
		            	nodeId = nodes[i].id;
		                if(nodes[i].type == "people" || nodes[i].type == "vehicle" ||  nodes[i].type == "thing"){
		                	if($.inArray(nodeId,ns) == -1){
		                		ns.push(nodeId);
		                	}
		                }
		            }
		            nodesLength = ns.length + 1;
            	}
                if(nodesLength > 5000){
                    layer.msg("最多勾选5000个监控对象！");
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
        	if(treeNode.checked){
        		zTree.expandNode(treeNode, true, true, true, true); // 展开节点
        	}
        	individuationIco.getCheckedNodes();
		},
		getCheckedNodes : function() {
			vpId="";
			selectList = [];
			var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
					.getCheckedNodes(true), v = "";

			for (var i = 0, l = nodes.length; i < l; i++) {
				if (nodes[i].type == "vehicle" || nodes[i].type == "people" ||  nodes[i].type == "thing" && vpId.indexOf(nodes[i].id) == -1) {
					v += nodes[i].name + ",";
					vpId+=nodes[i].id+",";
					var obj = {};
					obj.id = nodes[i].id;
					obj.type = nodes[i].type;
					selectList.push(obj);
				}
			}
			vehicleList = v;
		},
		zTreeOnNodeCreated: function (event, treeId, treeNode) {
	        var zTree = $.fn.zTree.getZTreeObj("treeDemo");
	        var id = treeNode.id.toString();
	        var list = [];
	        if (zTreeIdJson[id] == undefined || zTreeIdJson[id] == null) {
	            list = [treeNode.tId];
	            zTreeIdJson[id] = list;
	        } else {
	        	if ($.inArray(treeNode.tId, zTreeIdJson[id]) === -1) {
	        		zTreeIdJson[id].push(treeNode.tId)
	        	}
	        }
	    },
        //模糊查询
        inputTextAutoSearch: function(){
        	search_ztree('treeDemo', 'search_condition', 'vehicle');
        },
		uploadImageIndex: function(){
            var docObj=document.getElementById("doc"); 
            //检验导入的文件是否为Excel文件   
            var excelPath = document.getElementById("doc").value;
            if(excelPath == null || excelPath == ''){   
                layer.msg("请选择要上传的.png 格式图片！", {move: false});
                return false;   
            }else{
                var fileExtend = excelPath.substring(excelPath.lastIndexOf('.')).toLowerCase(); 
                if(fileExtend ==".png"){
                	 if(docObj.files &&docObj.files[0]){
                         var formData = new FormData(); 
                         formData.append("file", docObj.files[0]);  
                         $.ajax({  
                             url: '/clbs/m/personalized/ico/upload_img', 
                             type: 'POST',  
                             data: formData,  
                             async: false,  
                             cache: false,  
                             contentType: false,  
                             processData: false,  
                             success: function (data) {  
                                 data = $.parseJSON(data);
                                 if(data.state=="1"){
                                 	layer.msg("图片大小不要超过67x37");
                                     $("#vIcoTypePages_1,#vIcoTypePages_2,#vIcoTypePages_3").html("");
                                 	individuationIco.icoLoad();
                             		$(".vIcoContainer").hover(individuationIco.vIcoContainerOverFn);//图标悬浮
                             		$("#thisVIcoInfo").hide();
                                 }else{
                                     $("#vIcoTypePages_1,#vIcoTypePages_2,#vIcoTypePages_3").html("");
                                 	individuationIco.icoLoad();
                             		$(".vIcoContainer").hover(individuationIco.vIcoContainerOverFn);//图标悬浮
                                    $("#vIcoContainer_1").click();
                                    //图片外层赋值尺寸
                                 /*   $("#upIcoSize").css({
                                    	"width" : data.width,
                                    	"height" : data.height
                                    });*/
                                 }
                             },  
                             error: function (data) {  
                                 layer.msg("上传失败！");  
                             }  
                         });  
                     }
                }else{   
                    layer.msg("文件格式需为.png", {move: false});
                    return false;   
                }   
            } 
        },
		//下面用于图片上传预览功能
        setImagePreviewIndex: function(avalue){
        	$("#previewError").hide();
        	individuationIco.uploadImageIndex(); // 上传图片到服务器 
        	 var docObj=document.getElementById("doc");
             var imgObjPreview=document.getElementById("preview");
             if(docObj.files &&docObj.files[0])
             {
                 //火狐下，直接设img属性
                 //imgObjPreview.style.display = 'block';
                 //火狐7以上版本不能用上面的getAsDataURL()方式获取，需要一下方式
                 if(window.navigator.userAgent.indexOf("Chrome") >= 1 || window.navigator.userAgent.indexOf("Safari") >= 1){
                 	//imgObjPreview.src = window.webkitURL.createObjectURL(docObj.files[0]); 
                 }else{
                 	//imgObjPreview.src = window.URL.createObjectURL(docObj.files[0]);
                 }
             }
             else
             {
                 //IE下，使用滤镜
                 docObj.select();
                 var imgSrc = document.selection.createRange().text;
                 $("#doc").val("imgSrc");
                 var localImagId = document.getElementById("localImag-index");
                 //图片异常的捕捉，防止用户修改后缀来伪造图片
             try{
                 localImagId.style.filter="progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale)";
                 localImagId.filters.item("DXImageTransform.Microsoft.AlphaImageLoader").src = imgSrc;
             }
             catch(e)
             {
                 alert("您上传的图片格式不正确，请重新选择!");
                 return false;
             }
                 imgObjPreview.style.display = 'none';
                 document.selection.empty();
             }
             $("#doc").val("");
             return true;
        },
		//加载监控对象图标
		icoLoad : function(){
			var url="/clbs/m/personalized/ico/findIco";
        	json_ajax("POST", url, "json", false,null,individuationIco.callBack);
		},
		//图标悬浮
	    vIcoContainerOverFn: function(e){
	    	//鼠标悬浮id
	    	var vIcoContainer_id = e.currentTarget.id;
	    	//判断是否显示
	    	if($("#"+vIcoContainer_id).children("div.vicoDel").is(":hidden")){
		    	$("#"+vIcoContainer_id).children("div.vicoDel").show();
	    	}else{
		    	$("#"+vIcoContainer_id).children("div.vicoDel").hide();
	    	}
	    },
	    //悬浮删除点击
	    vIcoContainerDeleteFn: function(thisb,thisNum){
	    	//阻止事件冒泡
    		var e = arguments.callee.caller.arguments[0] || window.event;
    		e.stopPropagation();
    		var icoId=thisNum;
			var  url="/clbs/m/personalized/ico/delIco";
			var data={"id":icoId};
			layer.confirm('是否删除图标', {
	      		  title :'操作确认',
	      		  icon : 3, // 问号图标
	      		  btn: [ '确定', '取消'] //按钮
	      		}, function(){
	      			json_ajax("POST", url, "json", false,data,individuationIco.delCallBack);
	      		}
	      	);
	    },
	    //删除图标回调函数
	    delCallBack : function(data){
	    	if(data==true){
				layer.closeAll('dialog');
                $("#vIcoTypePages_1,#vIcoTypePages_2,#vIcoTypePages_3").html("");
				individuationIco.icoLoad();
				$("#thisVIcoInfo").hide();
				$("#ico").val("");
            	$("#icoName").val("");
            	$("#preview").attr("src","");
				$(".vIcoContainer").hover(individuationIco.vIcoContainerOverFn);//图标悬浮
			
	    	}else{
	    		layer.msg("该图标与车辆类别存在绑定关系，无法删除");
	    	}
		},
	    //点击选中
	    vIcoContainerCheckedFn: function(e,id,icoName){
	    	var vIcoChecked_id = e.id;
	    	$("#vIcoTypeNum").find("li").removeClass("vIcoCChecked");
	    	$("#"+vIcoChecked_id).addClass("vIcoCChecked");
	    	$("#ico").val(id);
	    	$("#icoName").val(icoName);
	    	$("#preview").attr("src","/clbs/resources/img/vico/"+icoName+"");
	    	$("#thisVIcoInfo").show();
	    },
		//图标遍历显示
	    callBack : function(data){
	    	var length = data.length / 3;
			if(individuationIco.isInteger(length)){
				length=parseInt(length);
				var newWid = length * 106;
				$("#vIcoTypePages_1,#vIcoTypePages_2,#vIcoTypePages_3").css("width",newWid+"px");
			}else{
				length=parseInt(length)+1;
				var newWid = length * 106;
				$("#vIcoTypePages_1,#vIcoTypePages_2,#vIcoTypePages_3").css("width",newWid+"px");
			}
			for(var i=1;i<=data.length;i++){
				if(i<=length){
                   var str=individuationIco.putHtml(data,i);
                    $("#vIcoTypePages_1").append(str);
				}else if(i<=length*2){
                    var str=individuationIco.putHtml(data,i);
                    $("#vIcoTypePages_2").append(str);
                }else if(i<=length*3){
                    var str=individuationIco.putHtml(data,i);
                    $("#vIcoTypePages_3").append(str);
                }
			}
		},
		//图标文档DOM添加
		putHtml : function(data,i,number){
	    	var str="";
            var icoName=data[i-1].icoName;
            var id=data[i-1].id;
            var state=data[i-1].defultState;
            if(state==0){
                str+='<li class="vIcoContainer" id="vIcoContainer_'+i+'" onclick="individuationIco.vIcoContainerCheckedFn(this,\''+id+'\',\''+icoName+'\')">';
                str+='<div class="vico"><img src="/clbs/resources/img/vico/'+icoName+'"></div>';
                str+='</li>';
            }else {
                str += '<li class="vIcoContainer" id="vIcoContainer_' + i + '" onclick="individuationIco.vIcoContainerCheckedFn(this,\'' + id + '\',\'' + icoName + '\')">';
                str += '<div class="vico"><img src="/clbs/resources/img/vico/' + icoName + '"></div>';
                str += '<div class="vicoDel" id="vicoDel_' + i + '" onclick="individuationIco.vIcoContainerDeleteFn(this,\'' + id + '\')"><img src="/clbs/resources/img/vico/vbhDel.png"/></div>';
                str += '</li>';
            }
            return str;
		},
		isInteger :function(obj){
			return obj%1 === 0
		},
		doSubmits : function(){
			var ico=$("#ico").val();
            individuationIco.getCheckedNodes();
			var data={"vehicleIcon":ico, "listStr":JSON.stringify(selectList)}
			if(vpId==""){
				layer.msg("❤～ 亲！至少选择一辆车吧")
				return false;
			}
			if(ico==""){
				layer.msg("❤～ 亲！请选择一个图标")
				return false;
			}
			var url="/clbs/m/personalized/ico/updateObjectIcon";
        	json_ajax("POST", url, "json", false,data,individuationIco.resultBack);
		},
		resultBack : function(data){
			if(data==true){
				layer.msg("设置图标成功");
			}
		},
		restoreDefault : function(){
			var data={"listStr":JSON.stringify(selectList)}
			var url="/clbs/m/personalized/ico/deflutObjectIcon";
			if(vpId==""){
				layer.msg("❤～ 亲！至少选择一辆车吧")
				return false;
			}
			layer.confirm('是否恢复默认图标', {
	      		  title :'操作确认',
	      		  icon : 3, // 问号图标
	      		  btn: [ '确定', '取消'] //按钮
	      		}, function(){
	      			json_ajax("POST", url, "json", false,data,individuationIco.restoreBack);
	      		});
		},
		restoreBack : function(data){
			if(data==true){
				layer.closeAll('dialog');
				layer.msg("恢复图标成功");
				
			}
		},
        ajaxQueryFilter: function(treeId, parentNode, responseData) {
        	responseData = JSON.parse(ungzip(responseData));
        	var list = [];
        	if (vpId != null && vpId != undefined && vpId != ""){
        		var str=(vpId.slice(vpId.length-1)==',')?vpId.slice(0,-1):vpId;
        		list = str.split(",");
        	}
            return filterQueryResult(responseData,list);
    	},
        searchVehicleTree : function (param){
        	var setQueryChar = {
                    async: {
                        url: "/clbs/a/search/monitorTreeFuzzy",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        sync:false,
                        otherParam: {"type": "multiple","queryParam":param,"webType": "1"},
                        dataFilter: individuationIco.ajaxQueryFilter
                        
                    },
                    check: {
                        enable: true,
                        chkStyle: "checkbox",
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
                        beforeClick: individuationIco.beforeClickVehicle,
                        onAsyncSuccess: individuationIco.fuzzyZTreeOnAsyncSuccess,
                        onCheck: individuationIco.fuzzyOnCheckVehicle,
                    }
                };
                $.fn.zTree.init($("#treeDemo"), setQueryChar, null);
        },
        fuzzyZTreeOnAsyncSuccess : function (event, treeId, treeNode) {
        	var zTree = $.fn.zTree.getZTreeObj("treeDemo");
        	zTree.expandAll(true);
        },
        fuzzyOnCheckVehicle : function(e, treeId, treeNode) {
        	//获取树结构
        	var zTree = $.fn.zTree.getZTreeObj("treeDemo");
        	//获取勾选状态改变的节点
        	var changeNodes = zTree.getChangeCheckedNodes();
        	if(treeNode.checked){ //若是取消勾选事件则不触发5000判断
                var checkedNodes = zTree.getCheckedNodes(true);
                var nodesLength = 0;
                for (var i=0;i<checkedNodes.length;i++) {
                    if(checkedNodes[i].type == "people" || checkedNodes[i].type == "vehicle" || checkedNodes[i].type == "thing" ||  checkedNodes[i].type == "thing"){
                        nodesLength += 1;
                    }
                }
                
                if(nodesLength > 5000){
                	//zTree.checkNode(treeNode,false,true);
                	layer.msg("最多勾选5000个监控对象！");
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
            individuationIco.getCheckedNodes(); // 记录勾选的节点
        },
        /**
         * 选中已选的节点
         */
        checkCurrentNodes : function (treeNode){
        	 var crrentSubV = vpId.split(",");
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
	     }
	}
	$(function(){
    	$('input').inputClear().on('onClearEvent',function(e,data){
    		var id = data.id;
    		if(id == 'search_condition'){
    			individuationIco.icoTree();
    		};
    	});
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
          		  individuationIco.icoTree();
    			}else{
    			  individuationIco.searchVehicleTree(param);
    			}
    		}, 500);
    	});
    	individuationIco.icoTree();
		individuationIco.init();
		individuationIco.icoLoad();
		$('[data-toggle="tooltip"]').tooltip();
		// 滚动展开
		$("#treeDemo").scroll(function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTreeScroll(zTree, this);
        });
        if(navigator.appName=="Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g,"")=="MSIE9.0") {
            var search;
            $("#search_condition").bind("focus",function(){
                search = setInterval(function(){
                	  var param = $("#search_condition").val();
                	  if(param == ''){
                		  individuationIco.icoTree();
          			  }else{
          				individuationIco.searchVehicleTree(param);
          			  }
                },500);
            }).bind("blur",function(){
                clearInterval(search);
            });
        }
		$(".vIcoContainer").hover(individuationIco.vIcoContainerOverFn);//图标悬浮
		$("#next-arrow").on("click",individuationIco.nextArrowFn);
		$("#pro-arrow").on("click",individuationIco.proArrowFn);
        $("#doSubmits").on("click",individuationIco.doSubmits);
        $("#restoreDefault").on("click",individuationIco.restoreDefault);
	})
})(window,$)