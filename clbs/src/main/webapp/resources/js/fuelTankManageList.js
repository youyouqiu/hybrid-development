(function(window,$){
	//显示隐藏列
	var menu_text = "";
	var table = $("#dataTable tr th:gt(1)");
    //单选
    var subChk = $("input[name='subChk']");
	tankManagement = {
		init: function(){
			//显示隐藏列
			menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) +"\" disabled />"+ table[0].innerHTML +"</label></li>"
			for(var i = 1; i < table.length; i++){
			    menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i+2) +"\" />"+ table[i].innerHTML +"</label></li>"
			};
			$("#Ul-menu-text").html(menu_text);

		    //表格列定义
		    var columnDefs = [{
		        //第一列，用来显示序号
		        "searchable" : false,
		        "orderable" : false,
		        "targets" : 0
		    } ];
		    var columns = [ {
		        //第一列，用来显示序号
		        "data" : null,
		        "class" : "text-center"
		    }, {
		        //第二列，checkbox
		        "data" : null,
		        "class" : "text-center",
		        render: function (data, type, row, meta) {
		            return '<input type="checkbox" name="subChk" value="' + row.id + '" /> ';
		        }
		    }, {//第三列，操作按钮列
		        "data": null,
		        "class": "text-center", //最后一列，操作按钮
		        render: function (data, type, row, meta) {
		            var editUrlPath = myTable.editUrl + row.id + ".gsp"; //修改地址
		            var detailUrlPath = myTable.detailUrl + row.id + ".gsp";
		            var result = '';
		            //修改按钮
		            result += '<button id="edit_' + row.id + '" onclick="tankManagement.editAndCheckBound(\'' + row.id + '\')"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
					//详情按钮
		            result += '<button href="' + detailUrlPath + '" data-target="#commonWin" data-toggle="modal" type="button" class=" editBtn editBtn-info"><i class="fa fa-sun-o"></i>详情</button>&ensp;';
		            //删除按钮
		            result += '<button type="button" onclick="myTable.deleteItem(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
		            return result;
		        }
		    }, {// 油箱型号
		        "data" : "type",
		        "class" : "text-center"
		    }, {// 油箱形状
		        "data" : "shapeStr",
		        "class" : "text-center"
		    }, { // 长度
		        "data" : "boxLength",
		        "class" : "text-center",
		    },{ // 宽度
		        "data" : "width",
		        "class" : "text-center"
		    },{ // 高度
		    	"data" : "height", 
		    	"class" : "text-center"
		    },{ // 壁厚
		    	"data" : "thickness", 
		    	"class" : "text-center"
		    },{ // 下圆角半径
		    	"data" : "buttomRadius", 
		    	"class" : "text-center"
		    },{ // 上圆角半径
		    	"data" : "topRadius", 
		    	"class" : "text-center"
		    },{ // 理论容积
		    	"data" : "theoryVolume", 
		    	"class" : "text-center"
		    },{ // 油箱容量
		    	"data" : "realVolume", 
		    	"class" : "text-center"
		    },{ // 备注
		    	"data" : "remark", 
		    	"class" : "text-center",
		    	render:function(data){
	            	return html2Escape(data)
	            }
		    }];
		    //ajax参数
		    var ajaxDataParamFun = function(d) {
		        d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
		    };
		    //表格setting
		    var setting = {
		        detailUrl:"/clbs/v/oilmassmgt/fueltankmgt/fuelTankDetail_",
		        listUrl : "/clbs/v/oilmassmgt/fueltankmgt/list",
		        editUrl : "/clbs/v/oilmassmgt/fueltankmgt/edit_",
		        deleteUrl : "/clbs/v/oilmassmgt/fueltankmgt/delete_",
		        deletemoreUrl : "/clbs/v/oilmassmgt/fueltankmgt/deletemore",
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
	    //全选
		checkAllClick: function(){
	        $("input[name='subChk']").prop("checked",this.checked);
		},
		//单选
	    subChkClick: function(){
	        $("#checkAll").prop("checked",subChk.length ==subChk.filter(":checked").length ? true:false );
	    },
	    //批量删除
	    delModelClick: function(){
	        //判断是否至少选择一项
	        var chechedNum=$("input[name='subChk']:checked").length;
	        if(chechedNum==0){
	        	layer.msg(selectItem,{move:false});
	            return;
	        }
	        var checkedList = new Array();
	        $("input[name='subChk']:checked").each(function () {
	            checkedList.push($(this).val());
	        });
	        layer.confirm(deleteConfirm, {btn : ["确定", "取消"],icon: 3,title: "操作确认"}, function () {
		        json_ajax("POST", myTable.deletemoreUrl, "json", true, {'deltems':checkedList.toString()}, tankManagement.deleteMoreCallback);
	        });
	    },
	    deleteMoreCallback: function(data){
	    	if(data.success){
	    		if (data != null && data.obj != null && data.obj.msg != null && data.obj.msg != "") {
			    	layer.confirm(data.obj.msg, {btn : ["确定"]}, function () {
			    		layer.closeAll();
			    		myTable.refresh();
			    	});
		    	} else {
		    		layer.closeAll();
		    		myTable.refresh();
		    	}
	    	}else{
	    		layer.msg(data.msg,{move:false});
	    	}
	    },
	    refreshTableClick: function(){
        	$("#simpleQueryParam").val("");
        	myTable.requestData();
	    },
	    // 修改的时候判断油箱是否被绑定，如果被绑定，则给予提示
		editAndCheckBound : function (boxId) {
			var url = "/clbs/v/oilmassmgt/fueltankmgt/checkBoxBound";
			var data = {"oilBoxId" : boxId};
			json_ajax("POST", url, "json", false, data, tankManagement.editAndCheckBoundCallback);
		},
		// 修改判断绑定的回调
		editAndCheckBoundCallback : function (data) {
			if(data.success){
				if (null != data && data.obj != null) {
					var url = myTable.editUrl + data.obj.oilBoxId + ".gsp";
					var curEditBtn = $("#edit_" + data.obj.oilBoxId);
					if (data.obj.isBound) { // 已绑定
						layer.confirm(oilBoundVehicle, {btn : ["确定", "取消"]}, function () {
							layer.closeAll();
							curEditBtn.attr("href", url);
							curEditBtn.attr("data-target", "#commonWin");
							curEditBtn.attr("data-toggle", "modal");
							curEditBtn.attr("onclick", "");
							curEditBtn.click();
						}, function () {
							curEditBtn.attr("onclick", "tankManagement.editAndCheckBound('" + data.obj.oilBoxId + "')");
						});
					} else { // 未绑定
						curEditBtn.attr("href", url);
						curEditBtn.attr("data-target", "#commonWin");
						curEditBtn.attr("data-toggle", "modal");
					}
				}
			}else{
				layer.msg(data.msg,{move:false});
			}
			
		},
	}
	$(function(){
		$('input').inputClear();
		tankManagement.init();
	    //全选
	    $("#checkAll").bind("click",tankManagement.checkAllClick);
		//单选
	    subChk.bind("click",tankManagement.subChkClick);
	    //批量删除
	    $("#del_model").bind("click",tankManagement.delModelClick);
        $("#refreshTable").bind("click",tankManagement.refreshTableClick);
	})
})(window,$)