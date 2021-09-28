(function(window,$){
	var startTime,endTime,myChart,option;
	//显示隐藏列
	var menu_text = "";
	var table = $("#fuelPriceTable tr th:gt(1)");
    var oiltype = null;
    var oilPrices = [];
    var oilPricestime = [];
    var fuelDataLength = 13;
    var difference;
	var fueldataList = ["","",""];
	var fuelBtnGroupList;
	var myTable;
	var pId = "";
	var $subChk = $("input[name='subChk']");
	var time1;
    var time2;
    var diqu;
    var oiltype2;
    var fuelTypeQuery2;
	fuelPriceManagement = {
		init: function(){
			//图表
	        myChart = echarts.init(document.getElementById("graphicsMain"));
            option = {
                title: {
                    text: '价格趋势图',
                    left: '45%'
                },
                tooltip: {
                    trigger: 'axis',
                    padding: [5,10,5,10],
                    textStyle: {
                    	fontSize: 18
                    }
                },
                legend: {
                    data:['油价'],
                    left: 'auto',
                },
                grid: {
                    left: '5%',
                    right: '10%',
                    bottom: '10%',
                    containLabel: true
                },
                toolbox: {
                    feature: {
                        saveAsImage: {
                			show: false
                        }
                    }
                },
                xAxis: {
                    name: '日期',
                    nameLocation: 'start',
                    type: 'category',
                    boundaryGap: false,
                    data: oilPricestime,
                    axisLabel: {
                    	show: true,
                    	rotate: 45
                    },
                    inverse: true
                },
                yAxis: {
                    name: '价格',
                    type: 'value',
                    max: 10,
                    min: 1,
                    splitNumber: 10
                },
                series: [
                    {
                        name:'油价',
                        type:'line',
                        stack: '总量',
                        data:oilPrices
                    },
                ]
            };
            myChart.setOption(option);
            window.onresize = myChart.resize;
		},
		//燃料类型数据显示
        fuelCategory:function(){
        	var url = "/clbs/m/basicinfo/monitoring/vehicle/findAllFuelType";
        	json_ajax("POST",url,"json",false,null, fuelPriceManagement.getFuelTypeCallback); 
        },
		getFuelTypeCallback:function(data){
		    function renderBtn(data) {
		        var button = document.createElement('button')
                var textNode = document.createTextNode(data.fuelType)
                var parameter = '';
                var endIndex = data.fuelType.indexOf('#') == -1 ? data.fuelType.length : data.fuelType.indexOf('#')
                var btnClassType = ''
                switch (data.fuelCategory) {
                    case '柴油':
                        btnClassType = 'btn-diesel';
                        break
                    case '汽油':
                        btnClassType = 'btn-gasoline';
                        break
                    case '天然气':
                        btnClassType = 'btn-cnlng';
                        break
                }
                parameter = data.fuelType.substring(0, endIndex)
                button.id = data.id
                button.type = 'button'
                button.className = 'btn btn-lg btn-block active'
                button.classList.add(btnClassType)
                button.title = data.fuelType
                button.setAttribute('onclick', 'fuelPriceManagement.OilType(' + parameter + ')')
                button.appendChild(textNode)
                return button
            }
            var btns = document.createDocumentFragment()
            data.obj.FuelTypeList.forEach(function(item){
                btns.appendChild(renderBtn(item))
            })
            $("#fuelBtnGroup").append(btns)
		},
        OilType:function (Qtype) {
            oiltype = Qtype;
            fuelPriceManagement.OilPricesQuery();
            fuelPriceManagement.validates();
        },
        OilPricesQuery: function () {
           var timeInterval = $('#timeInterval').val().split('--');
           var timeStart = timeInterval[0];
           var timeEnd = timeInterval[1];
           var district = $("#district").val();
           if(oiltype == null){
               layer.msg("请选择燃料类型");
               return null;
           }else if(oiltype!='92'&&oiltype!='95'&&oiltype!='97'&&oiltype!='0'){
        	   layer.msg("暂不支持该类型");
        	   $("#fuelPriceTable tbody tr td").hide();
           }
           if(time1==timeStart&&time2==timeEnd&&diqu==district&&oiltype2==oiltype){
        	   return false;
           }
            time1=timeStart;
            time2=timeEnd;
           diqu=district;
             oiltype2=oiltype;
		   $.ajax({
               type: "POST",
               url: "/clbs/v/carbonmgt/basicManagement/oilPricesQuery",
               data: {
                   "timeStart": timeStart,
                   "timeEnd": timeEnd,
                   "district": district,
                   "oiltype":oiltype
               },
               dataType: "json",
               async: false,
               success: function (data) {
                   if(data.success){
                	   var dataArray = [];
                       var oilPricesQuery = data.obj.oilPricesQuery;
                       oilPrices = [];
                       oilPricestime = [];
                       for(var i = 0;i<oilPricesQuery.length;i++){
                    	   var oilPrice = oilPricesQuery[i].oilPrice;
                    	   var dayTime = oilPricesQuery[i].dayTime;
                    	   oilPrices.push(oilPrice);
                    	   oilPricestime.push(dayTime);
                           var dataList
                    	   if(oiltype == 0){
                               dataList = [dayTime,oiltype+"#柴油",oilPrice];
                           }else{
                               dataList = [dayTime,oiltype+"#汽油",oilPrice];
                           }
                    	   dataArray.push(dataList);
                       }
                       fuelPriceManagement.init();
                       //判断数据条数
                       if(dataArray.length>=13){
                           fuelPriceManagement.exampleTable(dataArray);
                       }else{
                    	   //得到当前显示表格的数据差值
                    	   difference = fuelDataLength - dataArray.length;
                    	   for(var i=0;i<difference;i++){
                    		   dataArray.push(fueldataList);
                    	   }
                           fuelPriceManagement.exampleTable(dataArray);
                       }
                   }else{
                	   layer.msg(data.msg);
                   }
               }
		   })
        },
        exampleTable:function(data){
            table = $('#fuelPriceTable').DataTable({
                "destroy":true,
                "dom": 'trlip',// 自定义显示项
                "scrollX": true,
                "data": data,
                "lengthChange": false,// 是否允许用户自定义显示数量
                "bPaginate": false, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true,// 本地搜索
                "ordering": false, // 排序功能
                "Info": false,// 页脚信息
                "autoWidth": true,// 自动宽度
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
                    "sInfoEmpty": "当前显示0到0条，共0条记录",
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
                "columnDefs": [
	               { 'width': "40%", "targets": 0 },
	               { 'width': "30%", "targets": 1 },
	               { 'width': "30%", "targets": 2 },
	             ],
            });
        },
        validates: function(){
            return $("#fuelPriceForm").validate({
                rules: {
                    groupId: {
                        required: true
                    },
                    charSelect:{
                        required: true
                    },
                    startTime:{
                    	required: true
                    },
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval",                    
                    }
                },
                messages: {
                    groupId: {
                        required: publicNull
                    },
                    charSelect:{
                        required: publicNull
                    },
                    startTime:{
                    	required: publicNull
                    },
                    endTime: {
                        required: publicNull,
                        compareDate: publicSelectThanDate,
                    }
                }
            }).form();
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
                    : nowDate.getDate());
            endTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate());
        },
        //按钮组
        btnClickHighlighted: function(){
        	$("#fuelBtnGroup").find("button").removeClass("fuelPriceBtnGroup-Highlight");
        	fuelBtnGroupList = $("#fuelBtnGroup").find("button").length;
        	for(var i=0;i < fuelBtnGroupList;i++){
        		$(this).addClass("fuelPriceBtnGroup-Highlight");
        	}
        },
        //加载燃料类型列表
        roadFuelType:function(){
        	var fuelTypeQuery = $("#fuelTypeQuery").val();
        	var url = "/clbs/v/carbonmgt/basicManagement/roadFuelType";
        	var data = {"fuelType":fuelTypeQuery};
        	json_ajax("POST",url,"json",false,data,fuelPriceManagement.findFuelTypeList);

        },
        findFuelTypeList:function(data){
        	if(data.success){
        		var result=data.records; 
            	var dataListArray=[];
            	var permission = $("#permission").val();
            	for(var i=0;i<result.length;i++){
            		if(permission=="true"){
            		var list=[
            			i+1,
            			'<input  type="checkbox" name="subChk"  value="' + result[i].id + '" />',
            			'<button onclick="fuelPriceManagement.findFuelTypeById(\''+result[i].id+'\')" data-target="#updateFuelType" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;<button type="button"  onclick="fuelPriceManagement.deleteFuelType(\''+result[i].id+'\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>',
            			result[i].fuelCategory,result[i].fuelType,result[i].describes,
            		];
            		}else{
            			var list=[
            	        			i+1,
            	        			result[i].fuelCategory,result[i].fuelType,result[i].describes,
            	        		];
            		}
            		dataListArray.push(list);
            	}
                reloadData(dataListArray);
        	}else{
        		layer.msg(publicError);
        	}	
        },
        //增加燃料类型
        addFuelType:function(){
        	var fuelCategory = $("#addFuelCategory").val();
        	var fuelType = $("#fuelType").val();
        	var describes = $("#typeDescribe").val();
        	var reg=/^[0-9a-zA-Z-\#\u4E00-\u9FA5]{1,20}$/;
        	var reg1=/^[0-9a-zA-Z_\u4E00-\u9FA5]{0,50}$/;
        	if(!fuelPriceManagement.validates()){
        	    return;
            }
        	var value = fuelCategory + fuelType + describes;
        	var resubmitToken = value.split("").reduce(function (a, b) { a = ((a << 5) - a) + b.charCodeAt(0); return a & a }, 0);
        	var url = "/clbs/v/carbonmgt/basicManagement/addFuelType";
        	var data = {"fuelCategory":fuelCategory,"fuelType":fuelType,"describes":describes,resubmitToken};
            console.log('data',data);
        	json_ajax("POST",url,"json",false,data,fuelPriceManagement.addCallback);
        },
        validates: function(){
            return $("#speedLimit").validate({
                rules : {
                    fuelType : {
                        required : true,
                        fuelType:true,
                        maxlength : 20
                    },
                    description:{
                        maxlength : 50
                    }
                },
                messages : {
                    fuelType : {
                        required : '请输入燃料类型',
                        maxlength : publicSize20
                    },
                    description:{
                        maxlength : publicSize50
                    }
                }
            }).form();
        },
        addCallback:function(data){
        	if(data.success == false){
        		layer.msg(data.msg);
        		$("#fuelType").focus(function(){
        			$("#fuelType").val("");
        			$("#typeDescribe").val("");
        		});
        	}else{
        	    $("#addFuelType").modal('hide');
                $("#fuelTypeQuery").val("");
        	    fuelPriceManagement.roadFuelType();
        	}
        },
        //搜索
        searchFuelType:function(){
            fuelPriceManagement.roadFuelType();
        },
        //搜索回车键盘监听事件
        findType:function(event){
        	if(event.keyCode == 13){
        		fuelPriceManagement.roadFuelType();
        	}
        },
        findFuelTypeById:function(id){
        	pId = id;
        	var data = {"id":id};
        	var url = "/clbs/v/carbonmgt/basicManagement/findFuelTypeById";
        	json_ajax("POST",url,"json",false,data,fuelPriceManagement.findFuelTypeByIdCallback);
        },
        findFuelTypeByIdCallback:function(data){
            if(data.success){
                var result = data.obj.fuelType;
                $("#reviseFuelCategory").val(result.fuelCategory);
                $("#reviseFuelType").val(result.fuelType);
                $("#reviseTypeDescribes").val(result.describes);
            }else{
                layer.msg(data.msg);
            }

        },
        //修改燃料类型
        updateFuelType:function(){
        	var fuelCategoryVal = $("#reviseFuelCategory").val();
        	var fuelTypeVal = $("#reviseFuelType").val();
        	var describesVal = $("#reviseTypeDescribes").val();
        	var reg=/^[0-9a-zA-Z-\#\u4E00-\u9FA5]{1,20}$/;
        	var reg1=/^[0-9a-zA-Z_\u4E00-\u9FA5]{0,50}$/;

        	if(!fuelPriceManagement.editValidates()){
        		return;
        	}
        	var url="/clbs/v/carbonmgt/basicManagement/updateFuelType";

            var value = fuelCategoryVal + fuelTypeVal + describesVal;
            var resubmitToken = value.split("").reduce(function (a, b) { a = ((a << 5) - a) + b.charCodeAt(0); return a & a }, 0);
        	var data = {"id":pId,"fuelCategory":fuelCategoryVal,"fuelType":fuelTypeVal,"describes":describesVal,resubmitToken};
        	json_ajax("POST",url,"json",false,data,fuelPriceManagement.updateCallback);
        },
        editValidates: function(){
            return $("#editSpeedLimit").validate({
                rules : {
                    reviseFuelType : {
                        required : true,
                        fuelType:true,
                        maxlength : 20
                    },
                    describes:{
                        maxlength : 50
                    }
                },
                messages : {
                    reviseFuelType : {
                        required : '请输入燃料类型',
                        maxlength : publicSize20
                    },
                    describes:{
                        maxlength : publicSize50
                    }
                }
            }).form();
        },
        updateCallback:function(data){
        	if (data.success == true) {
        		$("#updateFuelType").modal('hide');
        		fuelPriceManagement.roadFuelType();
			} else {
        	    if(data.msg != null && data.msg != "" && data.msg != undefined) {
                    layer.msg(data.msg);
                } else {
        	        layer.msg("修改失败,请重试");
                }
				$("#updateFuelType").focus(function() {
					$("#reviseFuelType").val("");
					$("#reviseTypeDescribes").val("");
				});
			}
        },
        //根据id删除燃料类型
        deleteFuelType:function(id){
        	layer.confirm(publicDelete, {
      		  title :'操作确认',
      		  icon : 3, // 问号图标
      		  btn: [ '确定', '取消'] //按钮
      		}, function(){
      			var url="/clbs/v/carbonmgt/basicManagement/deleteFuelType";
              	var data={"id" : id};
              	json_ajax("POST", url, "json", false,data,fuelPriceManagement.deleteFuelTypeCallback);
      		});
        },
        //根据id删除燃料类型回调函数
        deleteFuelTypeCallback: function(result){
        	if(result.success){
              	layer.closeAll('dialog');
              	fuelPriceManagement.roadFuelType();
				$("#fuelBtnGroup").empty();
        	    fuelPriceManagement.fuelCategory();
        	}else{
        	    layer.msg(result.msg);
            }
        },
        //批量删除燃料类型
        deleteFuelTypeMuch:function(){
        	//判断是否至少选择一项
            var chechedNum = $("input[name='subChk']:checked").length;
            if (chechedNum == 0) {
                layer.msg(publicNoChecked);
                return
            }
         	var ids="";
            $("input[name='subChk']:checked").each(function() {
                ids+=($(this).val())+",";
            });
            var url="/clbs/v/carbonmgt/basicManagement/deleteFuelTypeMuch";
        	var data={"ids" : ids};
            layer.confirm(publicDelete, {
      		  title :'操作确认',
      		  icon : 3, // 问号图标
      		  btn: [ '确定', '取消'] //按钮
      		}, function(){
      		 	json_ajax("POST", url, "json", false,data,fuelPriceManagement.deleteFuelTypeMuchCallback);
      		});
        },
        //批量删除车辆用途回调函数
        deleteFuelTypeMuchCallback:function(result){
        	if(result.success){
              	layer.closeAll('dialog');
              	fuelPriceManagement.roadFuelType();
				$("#fuelBtnGroup").empty();
        	}else{
        		layer.msg(result.msg);
        	}
        },
        //全选
        checkAll : function(e){
	    	   $("input[name='subChk']").prop("checked", e.checked);
	    },
        //单选
        subChk: function(){
            $("#checkAll").prop("checked",$subChk.length == $subChk.filter(":checked").length ? true: false);
        },
        // 清空输入框内容
        cleanTypeVal:function(){
        	$("#addFuelCategory").val("柴油");
        	$("#fuelType").val("");
        	$("#typeDescribe").val("");
        },
		defaultDate : function(){
			var startDate = new Date();
			startDate.setDate(1);
			var defaultStartTime = startDate.getFullYear()
	            + "-"
	            + (parseInt(startDate.getMonth() + 1) < 10 ? "0"
	            + parseInt(startDate.getMonth() + 1)
	                : parseInt(startDate.getMonth() + 1))
	            + "-"
	            + (startDate.getDate() < 10 ? "0" + startDate.getDate()
	            	: startDate.getDate())
	            + " " + "00:00:00";
			var endDate = new Date();
			var defaultEndTime = endDate.getFullYear()
	            + "-"
	            + (parseInt(endDate.getMonth() + 1) < 10 ? "0"
	            + parseInt(endDate.getMonth() + 1)
	                : parseInt(endDate.getMonth() + 1))
	            + "-"
	            + (endDate.getDate() < 10 ? "0" + endDate.getDate()
	                : endDate.getDate())
	            + " " + endDate.getHours() 
	            + ":" + endDate.getMinutes() 
	            + ":" + endDate.getSeconds();
			var defaultTime=defaultStartTime+"--"+defaultEndTime;
			$('#timeInterval').val(defaultTime);
		},
		
	}
	$(function(){
		//初始化
        getTable('dataTable');
		fuelPriceManagement.init();
		$('input').inputClear();
		fuelPriceManagement.roadFuelType();
		//数据显示加载
		fuelPriceManagement.fuelCategory();
		//菜单切换重新绘制图表
        $("#toggle-left").bind("click", function () {
            setTimeout(function () {
        		//初始化
        		fuelPriceManagement.init();
            }, 500)
        });
		//echart初始化
        myChart.setOption(option);
        //当前时间
		fuelPriceManagement.getsTheCurrentTime();
        //设置当前时间显示
		$('#timeInterval').dateRangePicker();
		//设置默认时间
		fuelPriceManagement.defaultDate();
        //按钮组点击
        $("button.active").bind("click",fuelPriceManagement.btnClickHighlighted);
        //增加燃料类型
        $("#addFuelTypeSubm").on("click",fuelPriceManagement.addFuelType);
        //搜索燃料类型
        $("#search_button").on("click",fuelPriceManagement.searchFuelType);
        //批量删除 燃料类型
        $("#del_model").on("click",fuelPriceManagement.deleteFuelTypeMuch);
        //全选(燃料类型)
        $subChk.on("click",fuelPriceManagement.subChk);
	})
})(window,$)