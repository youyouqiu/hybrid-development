(function(window, $) {
	// 显示隐藏列
	var menu_text = "";
	var table = $("#dataTable tr th:gt(1)");
	// 单选
	var subChk = $("input[name='subChk']");
	oilRodSensorManagement = {
		init : function() {
			// 显示隐藏列
			menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\""
					+ parseInt(2)
					+ "\" disabled />"
					+ table[0].innerHTML
					+ "</label></li>"
			for (var i = 1; i < table.length; i++) {
				menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\""
						+ parseInt(i + 2)
						+ "\" />"
						+ table[i].innerHTML
						+ "</label></li>"
			}
			;
			$("#Ul-menu-text").html(menu_text);
			// 表格列定义
			var columnDefs = [ {
				// 第一列，用来显示序号
				"searchable" : false,
				"orderable" : false,
				"targets" : 0
			} ];
			var columns = [
					{
						// 第一列，用来显示序号
						"data" : null,
						"class" : "text-center"
					},
					{
						"data" : null,
						"class" : "text-center",
						render : function(data, type, row, meta) {
							var result = '';
							result += '<input  type="checkbox" name="subChk"  value="'
									+ row.id + '" />';
							return result;
						}
					},
					{
						"data" : null,
						"class" : "text-center", // 最后一列，操作按钮
						render : function(data, type, row, meta) {
							var editUrlPath = myTable.editUrl + row.id + ".gsp"; // 修改地址
							var result = '';
							// 修改按钮
							result += '<button href="'
									+ editUrlPath
									+ '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
							// 删除按钮
							result += '<button type="button" onclick="myTable.deleteItem(\''
									+ row.id
									+ '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
							return result;
						}
					}, {
						"data" : "riskEvent",
						"class" : "text-center"
					}, {
						"data" : "riskType",
						"class" : "text-center",
					}, {
						"data" : "description",
						"class" : "text-center",

					} /*
						 * , { "data": "baudRate", "class": "text-center",
						 * render:function(data,type,row,mete){ switch(data){
						 * case "01": return '2400'; break; case "02": return
						 * '4800'; break; case "03": return '9600'; break; case
						 * "04": return '19200'; break; case "05": return
						 * '38400'; break; case "06": return '57600'; break;
						 * case "07": return '115200'; break; default : return
						 * '9600'; break; } } },{ "data":"oddEvenCheck",
						 * "class":"text-center",
						 * render:function(data,type,row,mete){ switch(data){
						 * case 1: return '奇校验'; break; case 2: return '偶校验';
						 * break; case 3: return '无校验'; break; default : return
						 * '无校验'; break; } } }, { "data": "compensationCanMake",
						 * "class": "text-center",
						 * render:function(data,type,row,mete){ if(data==1){
						 * return '使能'; } else{ return '禁用'; } } }
						 */
			];
			// ajax参数
			var ajaxDataParamFun = function(d) {
				d.simpleQueryParam = $('#simpleQueryParam').val(); // 模糊查询
			};
			// 表格setting
			var setting = {
				listUrl : '/clbs/r/riskManagement/TypeLevel/list',
				editUrl : '/clbs/r/riskManagement/TypeLevel/edit_',
				deleteUrl : '/clbs/r/riskManagement/TypeLevel/delete_',
				deletemoreUrl : '/clbs/r/riskManagement/TypeLevel/deletemore',
				columnDefs : columnDefs, // 表格列定义
				columns : columns, // 表格列
				dataTableDiv : 'dataTable', // 表格
				ajaxDataParamFun : ajaxDataParamFun, // ajax参数
				pageable : true, // 是否分页
				showIndexColumn : true, // 是否显示第一列的索引列
				enabledChange : true
			};
			// 创建表格
			myTable = new TG_Tabel.createNew(setting);
			// 表格初始化
			myTable.init();
		},
		// 全选
		checkAllClick : function() {
			$("input[name='subChk']").prop("checked", this.checked);
		},
		// 单选
		subChkClick : function() {
			$("#checkAll").prop(
					"checked",
					subChk.length == subChk.filter(":checked").length ? true
							: false);
		},
		// 批量删除
		delModelClick : function() {
			// 判断是否至少选择一项
			var chechedNum = $("input[name='subChk']:checked").length;
			if (chechedNum == 0) {
				layer.msg(selectItem, {
					move : false
				});
				return;
			}
			var checkedList = new Array();
			$("input[name='subChk']:checked").each(function() {
				checkedList.push($(this).val());
			});
			layer.confirm(publicDelete, {
				btn : [ "确定", "取消" ]
			}, function() {
				json_ajax("POST", myTable.deletemoreUrl, "json", true, {
					'deltems' : checkedList.toString()
				}, oilRodSensorManagement.deleteMoreCallback);
			});
		},
		// 批量删除回调
		deleteMoreCallback : function(data) {
			if (data.success) {
				if (data != null && data.obj != null && data.obj.msg != null
						&& data.obj.msg != "") {
					layer.confirm(data.obj.msg, {
						btn : [ "确定" ]
					}, function() {
						layer.closeAll();
						myTable.refresh();
					});
				} else {
					layer.closeAll();
					myTable.refresh();
				}
			} else {
				layer.msg(data.msg, {
					move : false
				});
			}

		},
		// 列表刷新
		refreshTableClick : function() {
			$("#simpleQueryParam").val("");
			myTable.filter();
		},
	}
	$(function() {
		$('input').inputClear();
		oilRodSensorManagement.init();
		// 全选
		$("#checkAll").bind("click", oilRodSensorManagement.checkAllClick);
		// 单选
		subChk.bind("click", oilRodSensorManagement.subChkClick);
		// 批量删除
		$("#del_model").bind("click", oilRodSensorManagement.delModelClick);
		// 列表刷新
		$("#refreshTable").bind("click",
				oilRodSensorManagement.refreshTableClick);
	})
})(window, $)