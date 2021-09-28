(function(window,$){
    //显示隐藏列
    var menu_text = "";
    var table = $("#dataTable tr th:gt(1)");
	//单选
	var subChk = $("input[name='subChk']");
	vehicleModelsManagement = {
		//初始化
		init: function(){
		    menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(2) +"\" disabled />"+ table[0].innerHTML +"</label></li>"
		    for(var i = 1; i < table.length; i++){
		        menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i+2) +"\" />"+ table[i].innerHTML +"</label></li>"
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
	   			{ //第一列，用来显示序号
	   				"data" : null,
	   				"class" : "text-center"
	   			}, { // checkbox
	   				"data" : null,
	   				"class" : "text-center",
	   				render: function (data, type, row, meta) {
			            return '<input type="checkbox" name="subChk" value="' + row.vehicleId + '" /> ';
			        }
	   			}, { // 操作设置列
	   				"data" : null,
	   				"class" : "text-center", //最后一列，操作按钮
	   				render : function(data, type, row, meta) {
	   					var setUrl = "/clbs/v/carbonmgt/mobileSourceBaseInfo/add_" + row.vehicleId + ".gsp";
	   					var editUrl = myTable.editUrl + row.vehicleId;
	   					var detailUrl = myTable.detailUrl + row.vehicleId;
	   					var result = '';
	   					if (null != row.createDataUsername && row.createDataUsername != "") {
	   					    //修改
		   					result += '<button href="' + editUrl + '" data-target="#commonSmWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
	   					} else {
		   					//设置
		   					result += '<button href="' + setUrl + '" data-target="#commonSmWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>设置</button>&ensp;';
	   					}
	   					//删除
	   					result += '<button onclick="myTable.deleteItem(\''+ row.vehicleId + '\')" type="button" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>恢复默认</button>&ensp;';
	   					//详情
	   					result += '<button href="' + detailUrl + '" data-target="#commonSmWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>详情</button>';
	   					return result;
	   			   }
	   			}, { // 车牌号
	   				"data" : "brand",
	   				"class" : "text-center",
	   			}, { // 所属企业
	   				"data" : "groupName",
	   				"class" : "text-center"
	   			}, { // 分组
	   				"data" : "assignmentName",
	   				"class" : "text-center"
	   			}, { // 车辆类型
	   				"data" : "vehicleType",
	   				"class" : "text-center"
	   			}, { // 燃油类别
	   				"data" : "fuelType",
	   				"class" : "text-center"
	   			}, { // 终端号
	   				"data" : "deviceNumber",
	   				"class" : "text-center"
	   			}, { // 终端手机号
	   				"data" : "simcardNumber",
	   				"class" : "text-center"
	   			}, { // 计算基准能耗
	   				"data" : "calculateBaseEnergy",
	   				"class" : "text-center"
	   			}, { // 核定基准能耗
	   				"data" : "estimatesBaseEnergy",
	   				"class" : "text-center"
	   			}, { // 大修时间
	   				"data" : "overhauledTime",
	   				"class" : "text-center"
	   			}, { // 大修间隔
	   				"data" : "overhauledInterval",
	   				"class" : "text-center"
	   			}, { // 节油产品安装时间
	   				"data" : "savingProductsInstallTime",
	   				"class" : "text-center",
	   		}];
	        //ajax参数
	        var ajaxDataParamFun = function(d) {
	            d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
	        };
			//表格setting
			var setting = {
				listUrl : '/clbs/v/carbonmgt/mobileSourceBaseInfo/list',
				editUrl :  '/clbs/v/carbonmgt/mobileSourceBaseInfo/edit_',
				deleteUrl :  '/clbs/v/carbonmgt/mobileSourceBaseInfo/delete_',
				detailUrl : '/clbs/v/carbonmgt/mobileSourceBaseInfo/details_',
				deletemoreUrl : '/clbs/v/carbonmgt/mobileSourceBaseInfo/deletemore',
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
			$("input[name='subChk']").prop("checked", this.checked);
		},
		//单选
		subChkClick: function(){
			$("#checkAll").prop("checked",subChk.length == subChk.filter(":checked").length ? true: false);
		},
		//批量删除
		delModelClick: function(){
			//判断是否至少选择一项
			var chechedNum = $("input[name='subChk']:checked").length;
			if (chechedNum == 0) {
				layer.msg(selectItem, {move:false});
				return
			}
			var checkedList = new Array();
			$("input[name='subChk']:checked").each(function() {
				checkedList.push($(this).val());
			});
			myTable.deleteItems({
				'deltems' : checkedList.toString()
			});
		},
		//刷新列表
        refreshTable:function(){
        	$("#simpleQueryParam").val("");
            myTable.requestData();
        },
        // 批量删除
        delModelClick : function () {
        	//判断是否至少选择一项
			var chechedNum = $("input[name='subChk']:checked").length;
			if (chechedNum == 0) {
				layer.msg(selectItem);
				return;
			}
			var checkedList = new Array();
			$("input[name='subChk']:checked").each(function() {
				checkedList.push($(this).val());
			});
			myTable.deleteItems({
				'deltems' : checkedList.toString()
			});
        },
	}
	$(function(){
		$('input').inputClear();
		vehicleModelsManagement.init();
		//全选
		$("#checkAll").bind("click",vehicleModelsManagement.checkAllClick);
		//单选
		subChk.bind("click",vehicleModelsManagement.subChkClick);
		//批量删除
		$("#del_model").bind("click",vehicleModelsManagement.delModelClick);
		$("#refreshTable").on("click",vehicleModelsManagement.refreshTable);
	})
})(window,$)