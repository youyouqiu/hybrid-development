(function(window,$){
	//显示隐藏列
	var menu_text = "";
	var table = $("#dataTable tr th:gt(1)");
    //单选
    var subChk = $("input[name='subChk']");
    vehiclePeripheralsManagement = {
		//初始化
		init: function(){
			menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(2) +"\" disabled />"+ table[0].innerHTML +"</label></li>"
			for(var i = 1; i < table.length; i++){
			    menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i+2) +"\" />"+ table[i].innerHTML +"</label></li>"
			};
			$("#Ul-menu-text").html(menu_text);
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
		            result += '<input  type="checkbox" name="subChk"  value="' + row.id + '" />';
		            return result;
		        }
		    },
	        {
	            "data": null,
	            "class": "text-center", //最后一列，操作按钮
	            render: function (data, type, row, meta) {
	                var editUrlPath = myTable.editUrl + row.id + ".gsp"; //修改地址
	                var result = '';
	                //修改按钮
	                result += '<button href="' + editUrlPath + '" data-target="#commonSmWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
	                //删除按钮
	                result += '<button type="button" onclick="myTable.deleteItem(\'' + row.id + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
	                return result;
	            }
	        }, {
	            "data": "name",
	            "class": "text-center",
	            render:function(data){
	            	return html2Escape(data)
	            }
	        }, {
	            "data": "identId",
	            "class": "text-center"
	        }, {
	            "data": "msgLength",
	            "class": "text-center"
	        }];
		    //ajax参数
		    var ajaxDataParamFun = function (d) {
		        d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
		    };
		    //表格setting
		    var setting = {
		        listUrl: '/clbs/v/sensorConfig/vehiclePeripheral/list',
		        editUrl: '/clbs/v/sensorConfig/vehiclePeripheral/edit_',
		        deleteUrl: '/clbs/v/sensorConfig/vehiclePeripheral/delete_',
		        deletemoreUrl: '/clbs/v/sensorConfig/vehiclePeripheral/deletemore',
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
		},
		//全选
		checkAllClick: function(){
	        $("input[name='subChk']").prop("checked", this.checked);
		},
		subChkClick: function(){
	        $("#checkAll").prop("checked", subChk.length == subChk.filter(":checked").length ? true : false);
		},
	    //批量删除
	    delModelClick: function(){
	        //判断是否至少选择一项
	        var chechedNum = $("input[name='subChk']:checked").length;
	        if (chechedNum == 0) {
	            layer.msg(selectItem,{move:false});
	            return
	        }
	        var checkedList = new Array();
	        $("input[name='subChk']:checked").each(function () {
	            checkedList.push($(this).val());
	        });
	        myTable.deleteItems({'deltems': checkedList.toString()});
	    },
	  	//刷新列表
        refreshTable:function(){
        	$("#simpleQueryParam").val("");
            myTable.requestData();
        },
	}
	$(function(){
		$('input').inputClear();
		vehiclePeripheralsManagement.init();
		//全选
	    $("#checkAll").bind("click",vehiclePeripheralsManagement.checkAllClick);
	    subChk.bind("click",vehiclePeripheralsManagement.subChkClick);
	    //批量删除
	    $("#del_model").bind("click",vehiclePeripheralsManagement.delModelClick);
	    $("#refreshTable").on("click",vehiclePeripheralsManagement.refreshTable);
	})
})(window,$)