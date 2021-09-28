(function(window,$){
	var zTreeCitySel = $("#zTreeCitySel");
	var arrowDown = $("#arrowDown");
	var selectTabBox = $("#selectTabBox");
	var curTimeQuery = $("#curTimeQuery"); // 本月、本季度、本年查询按钮
	var preTimeQuery = $("#preTimeQuery"); // 上月、上季度、上年查询按钮
	var queryWay = "list1"; // 查詢方式
	var startDate; // 開始日期
	var endDate; // 結束日期
	var groupId; // 组织id
	var vehicleId; // 车辆id
	var brand; // 车牌号
	var year; // 年份
	var month; // 月份
	var quarter; // 季度
	var rowNums = 19; // 按日期查询时显示的列数
	var $laydateBox = $(".ToolPanel").find(".layDateBox");
	var $laydateBox1 = $(".ToolPanel").find(".layDateBox1");
	var footerDIV = $(".footerDIV");
	var list1_thead = $("#list1_thead");
	var list2to5_thead = $("#list2to5_thead");
	mileageEnergyReport = {
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
					beforeClick : mileageEnergyReport.beforeClick,
					onClick : mileageEnergyReport.onClick
				}
			};
			$.fn.zTree.init($("#ztreeDemo"), setting, null);
			// 默认显示车辆
			mileageEnergyReport.getVehicleInfoListByGroupId($("#groupId").val()); 
			// 默认显示当天时间
			mileageEnergyReport.defaultCurDate();
			// 初始化页面时按时间查询的按钮的显示与隐藏设置
			mileageEnergyReport.showOrHideTimeQueryBtn("list1");
		},
		beforeClick : function (treeId, treeNode) {
			var check = (treeNode);
			return check;
		},
		onClick(e, treeId, treeNode) {
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
			mileageEnergyReport.getVehicleInfoListByGroupId(v);
		},
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
			$("body").bind("mousedown", mileageEnergyReport.onBodyDown);
		}, 
		onBodyDown : function (event) {
			if (!(event.target.id == "menuBtn" || event.target.id == "zTreeContent" || $(event.target).parents("#zTreeContent").length > 0)) {
				mileageEnergyReport.hideMenu;
			}
		},
		hideMenu : function () {
			$("#zTreeContent").fadeOut("fast");
			$("body").unbind("mousedown", mileageEnergyReport.onBodyDown);
		},
		// 获取选中组织下的车辆
		getVehicleInfoListByGroupId : function (groupId) {
			var url = "/clbs/v/carbonmgt/mileageEnergyReport/initVehicleInfoList";
			var data = {"groupId":groupId};
			json_ajax("POST", url, "json", false, data, mileageEnergyReport.getVehicleInfoListByGroupIdCallback);
		},
		// 获取选中组织下的车辆回调
		getVehicleInfoListByGroupIdCallback : function (data) {
			var datas = data.obj;
            var dataList = {value : [] }, i = datas.vehicleInfoList.length;
            while (i--) {
                dataList.value.push({
                    name : datas.vehicleInfoList[i].brand,
                    id : datas.vehicleInfoList[i].id,
                });
            }
			$("#brands").bsSuggest({
                indexId : 1, //data.value 的第几个数据，作为input输入框的内容
                indexKey : 0, //data.value 的第几个数据，作为input输入框的内容
                keyField : "name",
                effectiveFields : [ "name" ],
                searchFields : [ "name" ],
                data : dataList
            }).on('onDataRequestSuccess', function(e, result) {
            }).on('onSetSelectValue', function(e, keyword, data) {
            }).on('onUnsetSelectValue', function() {
            });
			$("#brands").val(dataList.value[0].name);
			$("#brands").attr("data-id", dataList.value[0].id);
		},
		// 默认显示当前时间
		defaultCurDate : function () {
			var date = new Date();
			var year = date.getFullYear();
			var month = (date.getMonth() + 1) > 9 ? (date.getMonth() + 1) : "0"
					+ (date.getMonth() + 1);
			var day = date.getDate() > 9 ? date.getDate() : "0" + date.getDate();
			var hour = date.getHours() > 9 ? date.getHours() : "0"
					+ date.getHours();
			var minutes = date.getMinutes() > 9 ? date.getMinutes() : "0"
					+ date.getMinutes();
			var seconds = date.getSeconds() > 9 ? date.getSeconds() : "0"
					+ date.getSeconds();
			var curDate = year + "-" + month + "-" + day + " ";
			var startDate = curDate + "00:00:00";
			var endDate = curDate + "23:59:59";
			month = date.getMonth() + 1;
			$("#startDate").val(startDate);
			$("#endDate").val(endDate);
			$("#year").val(year);
			$("#month").val(month);
			if (month >= 1 && month <= 3) {
				$("#quarter").val(1);
			}
			if (month >= 4 && month <= 6) {
				$("#quarter").val(2);
			}
			if (month >= 7 && month <= 9) {
				$("#quarter").val(3);
			}
			if (month >= 10 && month <= 12) {
				$("#quarter").val(4);
			}
		}, 
		// 查询方式切换事件
		selectTabBoxChange : function () {
			mileageEnergyReport.hideErrorMsg;
			var value = $('#selectTabBox').val();
			queryWay = value;
			mileageEnergyReport.showOrHideTimeQueryBtn(value);
			mileageEnergyReport.setDefaultTableTitle(value);
			mileageEnergyReport.defaultCurDate; // 默认时间
			switch(value){
				case "list1":
				$laydateBox1.hide();
				$laydateBox.show();
				footerDIV.hide();
				list1_thead.show();
				list2to5_thead.hide();
				rowNums = 19;
				break;
				case "list2":
				$laydateBox1.hide();
				$laydateBox.show();
				footerDIV.hide();
				list1_thead.hide();
				list2to5_thead.show();
				rowNums = 28;
				break;
				case "list3":
				$laydateBox.hide();
				$laydateBox1.show();
				footerDIV.show();
				$("#ladydataMM").show();
				$("#ladydataJD").hide();
				list1_thead.hide();
				list2to5_thead.show();
				rowNums = 28;
				break;
				case "list4":
				$laydateBox.hide();
				$laydateBox1.show();
				footerDIV.show();
				$("#ladydataMM").hide();
				$("#ladydataJD").show();
				list1_thead.hide();
				list2to5_thead.show();
				rowNums = 28;
				break;
				case "list5":
				$laydateBox.hide();
				$laydateBox1.show();
				footerDIV.hide();
				$("#ladydataMM").hide();
				$("#ladydataJD").hide();
				list1_thead.hide();
				list2to5_thead.show();
				rowNums = 28;
				break;
			}
			$("#dataList").empty();
		},
		// 根据查询方式显示时间查询按钮：本月、上月、本季度、上季度 、本年、上年
		showOrHideTimeQueryBtn : function (queryWay) {
			if (queryWay == "list1" || queryWay == "list2") { // 按日期查询、按日期统计 
				curTimeQuery.hide();
				preTimeQuery.hide();
			} else if (queryWay == "list3") { // 按月统计
				curTimeQuery.show();
				preTimeQuery.show();
				curTimeQuery.text("本  月");
				preTimeQuery.text("上  月");
			} else if (queryWay == "list4") { // 按季度统计
				curTimeQuery.show();
				preTimeQuery.show();
				curTimeQuery.text("本季度");
				preTimeQuery.text("上季度");
			} else if (queryWay == "list5") { // 按年统计
				curTimeQuery.show();
				preTimeQuery.show();
				curTimeQuery.text("本  年");
				preTimeQuery.text("上  年");
			}
		}, 
		// 显示错误信息
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
		setDefaultTableTitle : function (queryWay) {
			var zTreeval = $(".has-feedback").find('input').val();
			var laydateFist = $(".laydate-icon:eq(0)").val();
			var laydateLast = $(".laydate-icon:eq(1)").val();
			var layDateBoxYear = $('.layDateBox1').find('select:eq(0) option:selected').val();
			var layDateBoxMon = $('.layDateBox1').find('select:eq(1) option:selected').val();
			var layDateBoxJD = $('#ladydataJD option:selected').text();
			$("span.groupTitle").text(zTreeval);
			if (queryWay == "list1") { // 按日期查询
				$(".title_name").text("里程能耗列表");
				$(".fwb").text("里程能耗列表");
				$("#dataTableBoxlist1").find('.ladydataTitle').find('h5').text(laydateFist.substr(0, 10) + "--" + laydateLast.substr(0, 10));
			} else if (queryWay == "list2") { // 按日期统计
				$(".title_name").text("里程能耗日报表");
				$(".fwb").text("里程能耗日报表");
				$("#dataTableBoxlist1").find('.ladydataTitle').find('h5').text(laydateFist.substr(0, 10) + "--" + laydateLast.substr(0, 10));
			} else if (queryWay == "list3") { // 按月份统计
				$(".title_name").text("里程能耗月报表");
				$(".fwb").text("里程能耗月报表");
				$("#dataTableBoxlist1").find('.ladydataTitle').find('h5').text(layDateBoxYear + "年" + layDateBoxMon + "月");
			} else if (queryWay == "list4") { // 按季度统计
				$(".title_name").text("里程能耗季度报表");
				$(".fwb").text("里程能耗季度报表");
				$("#dataTableBoxlist1").find('.ladydataTitle').find('h5').text(layDateBoxYear + "年" + layDateBoxJD);
			} else if (queryWay == "list5") { // 按年份统计
				$(".title_name").text("里程能耗年报表");
				$(".fwb").text("里程能耗年报表");
				$("#dataTableBoxlist1").find('.ladydataTitle').find('h5').text(layDateBoxYear + "年");
			}
		},
		// 查询
		inquireClick : function() {
			queryWay = $("#selectTabBox").val();
			startDate = $("#startDate").val();
			endDate = $("#endDate").val();
			groupId = $("#groupId").val();
			vehicleId = $("#brands").attr("data-id");
			year = $("#year").val();
			month = $("#month").val();
			quarter = $("#quarter").val();
			group = $(".has-feedback").find('input').val();
			if (mileageEnergyReport.validates()) {
				mileageEnergyReport.setDefaultValue(queryWay, startDate, endDate, groupId, vehicleId, year, month, quarter);
				startDate = $("#startDate").val();
				endDate = $("#endDate").val();
				mileageEnergyReport.ajaxList(queryWay, startDate, endDate, groupId, vehicleId, year, month, quarter);
			}
		},
		// 根据查询方式，验证车牌号是否已经选择
		validate_brand : function (queryWay, vehicleId) {
			if (queryWay == "list1" && vehicleId == "") {
				mileageEnergyReport.showErrorMsg("不能为空", "brands");
				return false;
			} else {
				mileageEnergyReport.hideErrorMsg();
				return true;
			}
		}, 
		validates : function () {
			return $("#queryForm").validate({
				rules : {
					groupName : {
						required : true
					},
					vehicleId : {
						required : true
					},
					startDate : {
						required : true
					},
					endDate : {
						required : true,
						compareDate : "#startDate"
					}
				},
				messages : {
					groupName : {
						required : "不能为空"
					},
					vehicleId : {
						required : "不能为空"
					},
					startDate : {
						required : "不能为空"
					},
					endDate : {
						required : "不能为空",
						compareDate : "结束时间必须大于开始时间!"
					}
				}
			}).form();
		},
		setDefaultValue : function (queryWay, startDate, endDate, groupId, vehicleId, year, month, quarter) {
			mileageEnergyReport.setDefaultTableTitle(queryWay);
			if (queryWay == "list1") { // 按日期查询
				$("#startDate").val(startDate);
				$("#endDate").val(endDate);
				return;
			} else if (queryWay == "list2") { // 按日期统计
				$("#startDate").val(startDate);
				$("#endDate").val(endDate);
				return;
			} else if (queryWay == "list3") { // 按月份统计
				mileageEnergyReport.getStartAndEndDateByMonth(month);
			} else if (queryWay == "list4") { // 按季度统计
				mileageEnergyReport.getStartAndEndDateByJD(quarter);
			} else if (queryWay == "list5") { // 按年份统计
				mileageEnergyReport.getStartAndEndDateByYear(year);
			}
		},
		getStartAndEndDateByMonth : function (month) {
			var startTime = "";
			var endTime = "";
			if (month == 10 || month == 11 || month == 12)
				startTime = year + "-" + month + "-" + "01" + " 00:00:00";
			else {
				startTime = year + "-0" + month + "-" + "01" + " 00:00:00";
			}
			if (month == 1 || month == 3 || month == 5 || month == 7
					|| month == 8 || month == 10 || month == 12) {
				if (month == 10 || month == 12)
					endTime = year + "-" + month + "-" + "31" + " 23:59:59";
				else
					endTime = year + "-0" + month + "-" + "31" + " 23:59:59";
			} else if (month == 4 || month == 6 || month == 9 || month == 11) {
				if (month == 11)
					endTime = year + "-" + month + "-" + "30" + " 23:59:59";
				else
					endTime = year + "-0" + month + "-" + "30" + " 23:59:59";
			} else {
				endTime = year + "-0" + month + "-" + "29" + " 23:59:59";
			}
			$("#startDate").val(startTime);
			$("#endDate").val(endTime);
		},
		getStartAndEndDateByJD : function (quarter) {
			var startTime = "";
			var endTime = "";
			if (quarter == "1") {
				startTime = year + "-" + "01" + "-" + "01" + " 00:00:00";
				endTime = year + "-" + "03" + "-" + "31" + " 23:59:59";
			} else if (quarter == "2") {
				startTime = year + "-" + "04" + "-" + "01" + " 00:00:00";
				endTime = year + "-" + "06" + "-" + "30" + " 23:59:59";
			} else if (quarter == "3") {
				startTime = year + "-" + "07" + "-" + "01" + " 00:00:00";
				endTime = year + "-" + "09" + "-" + "30" + " 23:59:59";
			} else if (quarter == "4") {
				startTime = year + "-" + "10" + "-" + "01" + " 00:00:00";
				endTime = year + "-" + "12" + "-" + "31" + " 23:59:59";
			}
			$("#startDate").val(startTime);
			$("#endDate").val(endTime);
		},
		getStartAndEndDateByYear : function (year) {
			var startTime = "";
			var endTime = "";
			startTime = year + "-" + "01" + "-" + "01" + " 00:00:00";
			endTime = year + "-" + "12" + "-" + "31" + " 23:59:59";
			$("#startDate").val(startTime);
			$("#endDate").val(endTime);
		},
		// ajax请求数据
		ajaxList:function(queryWay, startDate, endDate, groupId, brand, year, month, quarter, group){
			layer.load(2);
			var url = "/clbs/v/carbonmgt/mileageEnergyReport/list";
			var data = {"queryWay":queryWay, "startDate":startDate, "endDate":endDate, "groupId":groupId, "brand":brand, "year":year, "month":month, "quarter":quarter, "vehicleId":vehicleId, "group":group};
			json_ajax("POST", url, "json", true, data, mileageEnergyReport.ajaxListCallback);
		},
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
								data[i].baseBenchmark,
								data[i].currentAverageEnergyConsumption,
								data[i].energySaving_fuel,
								data[i].energySaving_standardCoal,
								data[i].reduceEmissions_CO2,
								data[i].reduceEmissions_SO2,
								data[i].reduceEmissions_NOX,
								data[i].reduceEmissions_HCX];
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
								data[i].baseBenchmark,
								data[i].currentAverageEnergyConsumption,
								data[i].baseBenchmarkAmount,
								data[i].currentEnergyConsumptionAmount,
								data[i].energySaving_fuel,
								data[i].energySaving_standardCoal,
								data[i].baseEmissions_CO2,
								data[i].baseEmissions_SO2,
								data[i].baseEmissions_NOX,
								data[i].baseEmissions_HCX,
								data[i].curEmissions_CO2,
								data[i].curEmissions_SO2,
								data[i].curEmissions_NOX,
								data[i].curEmissions_HCX,
								data[i].energySavingRate,
								data[i].reduceEmissions_CO2,
								data[i].reduceEmissions_SO2,
								data[i].reduceEmissions_NOX,
								data[i].reduceEmissions_HCX];
        				dataListArray.push(list);
					}
				}
			}
    		mileageEnergyReport.getTable('#dataTable', dataListArray);
		},
		getTable : function (table, data) {
			table = $(table)
			.DataTable(
			{
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
					"sEmptyTable" : queryConfirm,
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
			for(var i = rowNums + 2; i <= dttrtdLength; i++){
				if(dttrtdLength > 21){
					$dataTable.children("td:nth-child("+i+")").css("display","none");
				}
			}
		},
		// 本月、本季度、本年按钮点击事件
		curTimeQueryClick : function () {
			mileageEnergyReport.defaultCurDate();
			mileageEnergyReport.setDefaultTableTitle(queryWay);
			mileageEnergyReport.inquireClick();
		},
		// 上月、上季度、上年按钮点击事件
		preTimeQueryClick : function () {
			var y = $("#year").val();
			var m = $("#month").val();
			var q = $("#quarter").val();
			if (queryWay == "list3") { // 上月
				if (m == "1") {
					$("#year").val(parseInt(y) - 1);
					$("#month").val("12");
				} else {
					$("#month").val(parseInt(m) - 1);
				}
			} else if (queryWay == "list4") { // 上季度 
				if (q == "1") {
					$("#year").val(parseInt(y) - 1);
					$("#quarter").val("4");
				} else {
					$("#quarter").val(parseInt(q) - 1);
				}
			} else if (queryWay == "list5") { // 上年
				if (y != "" && y != "1987") {
					$("#year").val(parseInt(y) - 1);
				}
			}
			mileageEnergyReport.setDefaultTableTitle(queryWay);
			mileageEnergyReport.inquireClick();
		},
	}
	$(function(){
		mileageEnergyReport.init();
		zTreeCitySel.bind("click", mileageEnergyReport.showMenu);
		arrowDown.bind("click", mileageEnergyReport.showMenu);
		selectTabBox.bind("change", mileageEnergyReport.selectTabBoxChange);
		$("#inquireClick").bind("click", mileageEnergyReport.inquireClick);
		curTimeQuery.bind("click", mileageEnergyReport.curTimeQueryClick);
		preTimeQuery.bind("click", mileageEnergyReport.preTimeQueryClick);
	});
})(window,$)