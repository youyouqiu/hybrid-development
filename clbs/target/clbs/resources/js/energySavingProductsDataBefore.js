(function(window,$){
	var zTreeCitySel = $("#zTreeCitySel"); // 组织
	var arrowDown = $("#arrowDown"); // 组织下拉箭头
	var brands = $("#brands"); // 车辆下拉列表
	var inquireClick = $("#inquireClick"); // 查询按钮
	var selectTabBox = $("#selectTabBox"); // 查询方式
	var list1_thead = $("#list1_thead"); // 按日期查询thead
	var list2_thead = $("#list2_thead"); // 按日期统计thead
	var rowNums = 19;
	var productInstallTime = ""; // 节油产品安装时间
	var addVehicleTime = ""; // 加车时间
	var start = {
	  	elem: '#startDate',
	  	format: 'YYYY-MM-DD hh:mm:ss',
	  	min: addVehicleTime, //设定最小日期为当前日期
	  	max: productInstallTime, //最大日期
	  	istime: true,
	  	istoday: false,
	  	choose: function(datas){
	     	end.min = datas; //开始日选好后，重置结束日的最小日期
	     	end.start = datas //将结束日的初始值设定为开始日
	  	}
	};
	var end = {
	  	elem: '#endDate',
	  	format: 'YYYY-MM-DD hh:mm:ss',
	  	min: addVehicleTime,
	  	max: productInstallTime,
	  	istime: true,
	  	istoday: false,
	  	choose: function(datas){
	    	start.max = datas; //结束日选好后，重置开始日的最大日期
	  	}
	};
	energySavingProductsDataBefore = {
		// 初始化方法
		init : function () {
			// 初始化组织树
			var setting = {
				async : {
					url : "/clbs/m/basicinfo/enterprise/professionals/tree",
					type : "post",
					enable : true,
					autoParam : [ "id" ],
					contentType : "application/json",
					dataType : "json",
				},
				view : {
					dblClickExpand : false
				},
				data : {
					simpleData : {
						enable : true
					}
				},
				callback : {
					beforeClick : energySavingProductsDataBefore.beforeClick,
					onClick : energySavingProductsDataBefore.onClick
				}
			};
			$.fn.zTree.init($("#ztreeDemo"), setting, null);
			// 默认显示车辆
			energySavingProductsDataBefore.getVehicleInfoListByGroupId($("#groupId").val()); 
			// 默认显示当天时间
			energySavingProductsDataBefore.defaultCurDate();
			// 默认显示当前组织
			var value = $('#selectTabBox').val();
			energySavingProductsDataBefore.setDefaultTableTitle(value);
			// 默认显示按日期查询
			energySavingProductsDataBefore.selectTabBoxChange();
			// 初始化时间控件
			energySavingProductsDataBefore.initDatePick();
		},
		// 组织树点击前事件
		beforeClick : function (treeId, treeNode) {
			var check = (treeNode);
			return check;
		},
		// 组织树点击事件
		onClick : function (e, treeId, treeNode) {
			var zTree = $.fn.zTree.getZTreeObj("ztreeDemo"), nodes = zTree.getSelectedNodes(), v = "";
			n = "";
			nodes.sort(function compare(a, b) {
				return a.id - b.id;
			});
			for (var i = 0, l = nodes.length; i < l; i++) {
				n += nodes[i].name;
				v += nodes[i].uuid + ",";
			}
			if (v.length > 0)
				v = v.substring(0, v.length - 1);
			var cityObj = $("#zTreeCitySel");
			$("#groupId").val(v);
			cityObj.val(n);
			$("#zTreeContent").hide();
			// 选择组织之后，把组织下面的车辆查询出来
			energySavingProductsDataBefore.getVehicleInfoListByGroupId(v);
		},
		// 显示组织树
		showMenu : function () {
			if ($("#zTreeContent").is(":hidden")) {
				var width = $("#zTreeCitySel").parent().width();
				$("#zTreeContent").css("width",width + "px");
            	$(window).resize(function() {
    				var width = $("#zTreeCitySel").parent().width();
    				$("#zTreeContent").css("width",width + "px");
            	})
				$("#zTreeContent").show();
			} else {
				$("#zTreeContent").hide();
			}
			$("body").bind("mousedown", energySavingProductsDataBefore.onBodyDown);
		},
		// 选择组织后的事件
		onBodyDown : function (event) {
			if (!(event.target.id == "menuBtn" || event.target.id == "zTreeContent" 
					|| $(event.target).parents("#zTreeContent").length > 0)) {
				energySavingProductsDataBefore.hideMenu;
			}
		},
		// 隐藏组织树
		hideMenu : function () {
			$("#zTreeContent").fadeOut("fast");
			$("body").unbind("mousedown", energySavingProductsDataBefore.onBodyDown);
		},
		// 初始化时间选择框
		initDatePick : function () {
			start.min = addVehicleTime;
			end.min = addVehicleTime;
			start.max = productInstallTime;
			end.max = productInstallTime;
			laydate(start);
			laydate(end);
		},
		// 获取选中组织下的车辆
		getVehicleInfoListByGroupId : function (groupId) {
			var url = "/clbs/v/carbonmgt/energySavingBefore/initVehicleInfoList";
			var data = {"groupId":groupId};
			json_ajax("POST", url, "json", false, data, energySavingProductsDataBefore.getVehicleInfoListByGroupIdCallback);
		},
		// 获取选中组织下的车辆回调
		getVehicleInfoListByGroupIdCallback : function (data) {
			$("#brands").html("");
        	$("#brands").append("<option value=''></option>");
        	for (var i = 0; i < data.obj.vehicleInfoList.length; i++) {
        		if (i == 0) {
                	$("#brands").append("<option ds='"+ data.obj.vehicleInfoList[i].createDataTime +"' as='" + data.obj.vehicleInfoList[i].savingProductsInstallTime + "' value=" + data.obj.vehicleInfoList[i].id + " selected>" + data.obj.vehicleInfoList[i].brand + "</option>");
                	addVehicleTime = $("#brands").find("option:selected").attr("ds");
                	productInstallTime = $("#brands").find("option:selected").attr("as");
                	energySavingProductsDataBefore.initDatePick();
        		} else {
        			$("#brands").append("<option ds='"+ data.obj.vehicleInfoList[i].createDataTime +"' as='" + data.obj.vehicleInfoList[i].savingProductsInstallTime + "' value=" + data.obj.vehicleInfoList[i].id + ">" + data.obj.vehicleInfoList[i].brand + "</option>");
        		}
            }
		},
		// 车辆下拉列表change事件
		brandsChange : function () {
			addVehicleTime = $("#brands").find("option:selected").attr("ds");
			productInstallTime = $("#brands").find("option:selected").attr("as");
			energySavingProductsDataBefore.initDatePick();
		},
		// 查询方式切换事件
		selectTabBoxChange : function () {
			energySavingProductsDataBefore.defaultCurDate(); // 默认时间
			energySavingProductsDataBefore.hideErrorMsg();
			var value = $('#selectTabBox').val();
			energySavingProductsDataBefore.setDefaultTableTitle(value);
			
			switch(value){
				case "list1":
					list1_thead.show();
					list2_thead.hide();
					rowNums = 19;
					break;
				case "list2":
					list1_thead.hide();
					list2_thead.show();
					rowNums = 18;
					break;
			}	
			$("#dataList").empty();
		},
		//显示错误提示信息
		showErrorMsg : function (msg, inputId) {
			if ($("#error_label").is(":hidden")) {
				$("#error_label").text(msg);
				$("#error_label").insertAfter($("#" + inputId));
		        $("#error_label").show();
			} else {
				$("#error_label").is(":hidden");
			} 
		},
		// 隐藏错误信息
		hideErrorMsg : function () {
			$("#error_label").hide();
		},
		// 切换查询方式的时候，默认选择当前时间
		defaultCurDate : function () {
			var date = new Date();
			var year = date.getFullYear();
			var month = (date.getMonth()+1) > 9 ? (date.getMonth()+1) : "0" + (date.getMonth()+1);
			var day = date.getDate() > 9 ? date.getDate() : "0" + date.getDate();
			var hour = date.getHours() > 9 ? date.getHours() : "0" + date.getHours();
			var minutes = date.getMinutes() > 9 ? date.getMinutes() : "0" + date.getMinutes();
			var seconds = date.getSeconds() > 9 ? date.getSeconds() : "0" + date.getSeconds();
			var curDate = year + "-" + month + "-" + day + " ";
			var startDate = curDate + "00:00:00";
			var endDate = curDate + "23:59:59";
			$("#startDate").val(startDate);
			$("#endDate").val(endDate);
		},
		// 验证
		validates : function () {
			return $("#queryForm").validate({
	            rules: {
	            	groupName: {
	            		required : true
	            	},
	            	vehicleId : {
	                	required : true
	                },
	                startDate : {
	                	required : true
	                },
	            	endDate: {
	            		required : true,
	                    compareDate:"#startDate"
	                },
	            },
	            messages: {
	            	groupName: {
	            		required : "不能为空"
	            	},
	            	vehicleId : {
	                	required : "不能为空"
	                },
	                startDate : {
	                	required : "不能为空"
	                },
	            	endDate: {
	            		required : "不能为空",
	                    compareDate:"结束时间必须大于开始时间!"
	                }
	            }
	        }).form();
		},
		// 查询
        inquireClick : function () {
    		queryWay = $("#selectTabBox").val();
        	startDate = $("#startDate").val();
        	endDate = $("#endDate").val();
        	groupId = $("#groupId").val();
        	vehicleId = $("#brands").val();
       		if (energySavingProductsDataBefore.validates()) {
       			energySavingProductsDataBefore.setDefaultValue(queryWay, startDate, endDate, groupId, vehicleId);
        		startDate = $("#startDate").val();
        		endDate = $("#endDate").val();
        		energySavingProductsDataBefore.ajaxList(queryWay, startDate, endDate, groupId, vehicleId);
       		}
        },
        setDefaultValue : function (queryWay, startDate, endDate, groupId, vehicleId) {
        	energySavingProductsDataBefore.setDefaultTableTitle(queryWay);
        	if (queryWay == "list1" || queryWay == "list2") { // 按日期查询、按日期统计
        		$("#startDate").val(startDate);
        		$("#endDate").val(endDate);
        	} 
        },
        setDefaultTableTitle: function(queryWay) {
        	var zTreeval = $(".has-feedback").find('input').val();
        	$("span.groupTitle").text(zTreeval);
        },
     	// ajax请求数据
		ajaxList : function(queryWay, startDate, endDate, groupId, vehicleId){
			layer.load(2);
			var url = "/clbs/v/carbonmgt/energySavingBefore/queryByDatePage";
			var data = {"queryWay":queryWay, "startDate":startDate, "endDate":endDate, "groupId":groupId, "vehicleId":vehicleId};
			json_ajax("POST", url, "json", true, data, energySavingProductsDataBefore.ajaxListCallback);
		},
		// 查询按钮请求数据回调
		ajaxListCallback : function (data) {
			layer.closeAll('loading'); 
        	$("#dataList").empty();
        	var dataListArray = [];
        	if (data != null && data != "null" && data != undefined && data.length > 0) {
        		if(queryWay == "list1"){
    				for(var i = 0; i < data.length; i++){
        				var list = 
						[ i + 1, data[i].brand,
								data[i].vehicleType,
								data[i].fuelType,
								data[i].startDate,
								data[i].endDate,
								data[i].duration,
								data[i].mileage,
								data[i].averageSpeed,
								data[i].airConditionerDuration,
								data[i].rollingDuration,
								data[i].totalFuelConsumption,
								data[i].energyPrice,
								data[i].energyTotalFee,
								data[i].energy_100,
								data[i].emissions_CO2,
								data[i].emissions_SO2,
								data[i].emissions_NOX,
								data[i].emissions_HCX];
        				dataListArray.push(list);
					}
				}else{
					for(var i = 0; i < data.length; i++){
        				var list = 
						[ i + 1, data[i].time,
						  		data[i].brand,
								data[i].vehicleType,
								data[i].fuelType,
								data[i].duration,
								data[i].mileage,
								data[i].averageSpeed,
								data[i].airConditionerDuration,
								data[i].rollingDuration,
								data[i].totalFuelConsumption,
								data[i].energyPrice,
								data[i].energyTotalFee,
								data[i].energy_100,
								data[i].emissions_CO2,
								data[i].emissions_SO2,
								data[i].emissions_NOX,
								data[i].emissions_HCX];
        				dataListArray.push(list);
					}
				}
        	}
        	energySavingProductsDataBefore.getTable('#dataTable', dataListArray);
		},
		getTable : function (table, data) {
			table = $(table)
			.DataTable({
				"destroy" : true,
				"dom" : 'tpirl',// 自定义显示项
				"data" : data,
				"lengthChange" : true,// 是否允许用户自定义显示数量
				"bPaginate" : true, // 翻页功能
				"bFilter" : false, // 列筛序功能
				"searching" : false,// 本地搜索
				"ordering" : false, // 排序功能
				"Info" : true,// 页脚信息
				"autoWidth" : true,// 自动宽度
				"stripeClasses" : [],
                "pageLength": 10,
				"lengthMenu" : [ 10, 20, 50, 100, 200 ],
                "pagingType" : "simple_numbers", // 分页样式
				"oLanguage" : {// 国际语言转化
					"oAria" : {
						"sSortAscending" : " - click/return to sort ascending",
						"sSortDescending" : " - click/return to sort descending"
					},
					"sLengthMenu" : "显示 _MENU_ 记录",
					"sInfo" : "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录。",
					"sZeroRecords" : "对不起，查询不到任何相关数据",
					"sEmptyTable" :queryConfirm,
					"sLoadingRecords" : "正在加载数据-请等待...",
					"sInfoEmpty" : "当前显示0到0条，共0条记录",
					"sInfoFiltered" : "（数据库中共为 _MAX_ 条记录）",
					"sProcessing" : "<img src='../resources/user_share/row_details/select2-spinner.gif'/> 正在加载数据...",
					"sSearch" : "模糊查询：",
					"sUrl" : "",
					"oPaginate" : {
						"sFirst" : "首页",
						"sPrevious" : " 上一页 ",
						"sNext" : " 下一页 ",
						"sLast" : " 尾页 "
					}
				},
		        "dom" : "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
			});
			var $dataTable = $("#dataTable tbody tr");
			var dttrtdLength = $dataTable.children().length;
			for(var i = rowNums + 1; i <= dttrtdLength; i++){
				if(dttrtdLength > 18){
					$dataTable.children("td:nth-child("+i+")").css("display","none");
				}
			}
		},
	}
	$(function(){
		energySavingProductsDataBefore.init();
		zTreeCitySel.bind("click", energySavingProductsDataBefore.showMenu);
		arrowDown.bind("click", energySavingProductsDataBefore.showMenu);
		brands.bind("change", energySavingProductsDataBefore.brandsChange);
		inquireClick.bind("click", energySavingProductsDataBefore.inquireClick);
		selectTabBox.bind("change", energySavingProductsDataBefore.selectTabBoxChange);
	});
})(window,$)