(function(window,$){
    //显示隐藏列
	var menu_text = "";
	var table = $("#dataTable tr th:gt(1)");
    //单选
    var subChk = $("input[name='subChk']");
	var selectGroupId = '';
	var selectAssignmentId = '';

    var dtext = "";
    var dn = new Array("交通部JT/T808-2013","交通部JT/T808-2019");
    var dv = new Array("1","11");
    var protocol=1;
	switchSignalManagement = {
		//初始化
		init: function(){
			//数据表格列筛选
			menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(2) +"\" disabled />"+ table[0].innerHTML +"</label></li>"
			for(var i = 1; i < table.length; i++){
			    menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i+2) +"\" />"+ table[i].innerHTML +"</label></li>"
			};
			$("#Ul-menu-text").html(menu_text);
            //生成定制显示列
            for(var i = 0; i < dn.length; i++){
                dtext +="<label class='radio-inline' style='margin-left:10px;'><input name=\"deviceCheck\" value=\"" + dv[i] +"\" type=\"radio\" class=\"device\" />"+ dn[i] +"</label>";
            };
            $("#Ul-menu-text-v").html(dtext);
            $("input[value='1']").prop("checked",true);
			//表格列定义
		    var columnDefs = [{
		        //第一列，用来显示序号
		        "searchable": false,
		        "orderable": false,
		        "targets": 0
		    }];
		    var columns = [{
		        //第一列，用来显示序号
		        "data": null,
		        "class": "text-center"
		    }, {
		        "data": null,
		        "class": "text-center",
		        render: function (data, type, row, meta) {
		            var result = '';
		        	if(row.setingId != null && row.setingId != ""){
	                    result += "<input  type='checkbox' name='subChk'  value='" +  row.vehicleId + "'/>";
		        	}else{
		        		result +="";
		        	}
		            return result;
		        }
		    },
	        {
	            "data": null,
	            "class": "text-center", //最后一列，操作按钮
	            render: function (data, type, row, meta) {
	                var editUrlPath = myTable.editUrl + row.vehicleId + ".gsp"; //修改地址
	                var result = '';
                    //判断设置及修改状态
                    var bottom_name="设置";
                    if(row.setingId != null && row.setingId != ""){
                        bottom_name="修改";
    	                //修改按钮
    	                result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>' + bottom_name + '</button>&ensp;';
    					//恢复默认
						result += '<button type="button" onclick="myTable.relieveItem(\'' + row.vehicleId + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>恢复默认</button>&ensp;';
                    }else{
                    	//设置按钮
    	                result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>' + bottom_name + '</button>&ensp;';
    	              	//禁用恢复默认按钮
						result += '<button disabled type="button" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>恢复默认</button>&ensp;';
                    }
	                return result;
	            }
	        },{
	            "data": "brand",
	            "class": "text-center"
	        }, {
                "data": "groups",
                "class": "text-center"
             }, {
	            "data": "vehicleType",
	            "class": "text-center"
	        }];
		    //ajax参数
		    var ajaxDataParamFun = function (d) {
		        d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
				d.groupId = selectGroupId;
                d.assignmentId = selectAssignmentId;
				d.protocol = $("input[name='deviceCheck']:checked").val();
		    };
		    //表格setting
		    var setting = {
		        listUrl: '/clbs/m/switching/signal/list',
		        bindUrl: '/clbs/m/switching/signal/bind_',
		        editUrl: '/clbs/m/switching/signal/edit_',
		        deleteUrl: '/clbs/m/switching/signal/delete_',
                deletemoreUrl: '/clbs/m/switching/signal/deletemore',
		        columnDefs: columnDefs, //表格列定义
		        columns: columns, //表格列
		        dataTableDiv: 'dataTable', //表格
		        ajaxDataParamFun: ajaxDataParamFun, //ajax参数
		        pageable: true, //是否分页
		        showIndexColumn: true, //是否显示第一列的索引列
		        enabledChange: true
		    };
            //创建表格
		    myTable = new TG_Tabel.createNew(setting);
	        //表格初始化
	        myTable.init();
	        //组织树
	        var treeSetting = {
        		async : {
        			url : "/clbs/m/basicinfo/enterprise/assignment/assignmentTree",
        			tyoe : "post",
        			enable : true,
        			autoParam : [ "id" ],
        			dataType : "json",
        			otherParam : {
        				"isOrg" : "1"
        			},
        			dataFilter: switchSignalManagement.ajaxDataFilter
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
        			onClick : switchSignalManagement.zTreeOnClick
        		}
        	};
	        //组织树初始化
			$.fn.zTree.init($("#treeDemo"), treeSetting, null);
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
		//组织架构单击
		zTreeOnClick: function (event, treeId, treeNode) {
			if (treeNode.type == "assignment") {
				selectAssignmentId = treeNode.id;
				selectGroupId = '';
			}else{
				selectGroupId = treeNode.uuid;
				selectAssignmentId = '';
			}
			myTable.requestData();
		},
		//全选
		checkAllClick: function(){
	        $("input[name='subChk']").prop("checked", this.checked);
		},
		subChkClick: function(){
	        $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
		},
		updataFenceData: function(msg){
	        if (msg != null) {
	            var result = $.parseJSON(msg.body);
	            if (result != null) {
	                myTable.refresh();
	            }
	        }
	    },
        //批量解除
        delModelClick: function(){
            //判断是否至少选择一项
            var chechedNum = $("input[name='subChk']:checked").length;
            if (chechedNum == 0) {
                layer.msg(selectItem,{move:false});
                return
            }
            var checkedList = new Array();
            $("input[name='subChk']:checked").each(function() {
                checkedList.push($(this).val());
            });
            myTable.relieveItems({'deltems': checkedList.toString()});
        },
	  	//刷新列表
        refreshTable:function(){
        	selectGroupId = '';
        	selectAssignmentId = '';
        	var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.selectNode("");
            zTree.cancelSelectedNode();
        	$("#simpleQueryParam").val("");
        	myTable.requestData();
        },
	}
	$(function(){
		switchSignalManagement.init();
		$('input').inputClear().on('onClearEvent',function(e,data){
			var id = data.id;
			if(id == 'search_condition'){
				search_ztree('treeDemo',id,'assignment');
			};
		});
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
        // 组织架构模糊搜索
        $("#search_condition").on("input oninput",function(){
            search_ztree('treeDemo', 'search_condition', 'assignment');
        });
		//全选
	    $("#checkAll").bind("click",switchSignalManagement.checkAllClick);
	    subChk.bind("click",switchSignalManagement.subChkClick);
        //批量删除
        $("#restore_model").bind("click",switchSignalManagement.delModelClick);
	    //刷新
	    $("#refreshTable").on("click",switchSignalManagement.refreshTable);

        //改变协议勾选框
        $(".device").change(function() {
            //取消全选
            $("#checkAll").prop('checked',false);
            //刷新表格
            myTable.requestData();
        });
	})
})(window,$)