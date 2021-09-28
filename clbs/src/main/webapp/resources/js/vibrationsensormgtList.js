(function(window,$){
	//显示隐藏列
	var menu_text = "";
	var table = $("#dataTable tr th:gt(1)");
    //单选
    var subChk = $("input[name='subChk']");
	vibrationSensorManagement = {
		init: function(){
			menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) +"\" disabled />"+ table[0].innerHTML +"</label></li>"
			for(var i = 1; i < table.length; i++){
			    menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i+2) +"\" />"+ table[i].innerHTML +"</label></li>"
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
		            {
		                //第一列，用来显示序号
		                "data" : null,
		                "class" : "text-center"
		            },
		            {
		                "data" : null,
		                "class" : "text-center",
		                render : function(data, type, row, meta) {
		                    var result = '';
		                    result += '<input  type="checkbox" name="subChk"  value="' + row.id + '" />';
		                    return result;
		                }
		            },
		            {
		                "data" : null,
		                "class" : "text-center", //最后一列，操作按钮
		                render : function(data, type, row, meta) {
		                    var editUrlPath = myTable.editUrl + row.id + ".gsp"; //修改地址
		                    var result = '';
		                    if (row.sensorType == "F3-Vibration-Sensor") {
		                    	//禁止修改按钮
			                    result += '<button disabled data-target="#commonWin" data-toggle="modal" type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>修改</button>&ensp;';
			                    //禁止删除按钮
			                    result += '<button disabled type="button" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>删除</button>';
		                    } else {
		                    	//修改按钮
			                    result += '<button href="'+editUrlPath+'" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
			                    //删除按钮
			                    result += '<button type="button" onclick="myTable.deleteItem(\''
			                            + row.id
			                            + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
		                    }
		                    return result;
		                }
		            }, {
		                "data" : "sensorType",
		                "class" : "text-center"
		            }, {
		                "data" : "manufacturers",
		                "class" : "text-center"
		            },{
		                "data" : "parity",
		                "class" : "text-center",
		                render : function(data, type, row, meta) {
		                    if (data == "3") {
		                        return '无校验';
		                    } else if (data == "1") {
		                        return '奇校验';
		                    } else if (data == "2"){
		                    	return '偶校验';
		                    }else {
		                    	return "";
		                    }
		                }
		            } ,{
						"data" : "baudRate",
						"class" : "text-center",
		                render : function(data, type, row, meta) {
		                	if (data == "1") {
		                        return '2400';
		                    } else if (data == "2") {
		                        return '4800';
		                    }  else if (data == "3") {
		                        return '9600';
		                    }  else if (data == "4") {
		                        return '19200';
		                    }  else if (data == "5") {
		                        return '38400';
		                    } else if (data == "6") {
		                        return '57600';
		                    } else if (data == "7") {
		                        return '115200';
		                    } else {
		                    	return "";
		                    }
		                }
					},{
		                "data" : "inertiaCompEn",
		                "class" : "text-center",
		                render : function(data, type, row, meta) {
		                    if (data == "2") {
		                        return '禁用';
		                    } else if (data == "1") {
		                        return '使能';
		                    } else {
		                    	return '';
		                    }
		                }
		            },{
		                "data" : "filterFactor",
		                "class" : "text-center",
		                render : function(data, type, row, meta) {
		                    if (data == "2") {
		                        return '平滑';
		                    } else if (data == "1") {
		                        return '实时';
		                    } else if (data == "3") {
		                        return '平稳';
		                    } else {
		                    	return '';
		                    }
		                }
		            },{
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
		        listUrl : '/clbs/v/workhourmgt/vb/list',
		        editUrl : '/clbs/v/workhourmgt/vb/edit_',
		        deleteUrl : '/clbs/v/workhourmgt/vb/delete_',
		        deletemoreUrl : '/clbs/v/workhourmgt/vb/deletemore',
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
	        	layer.msg(selectItem);
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
		refreshTable: function(){
        	$("#simpleQueryParam").val("");
        	myTable.requestData();
		},
	}
	$(function(){
		var myTable;
		$('input').inputClear();
		vibrationSensorManagement.init();
	    //全选
	    $("#checkAll").bind("click",vibrationSensorManagement.checkAllClick);
		//单选
	    subChk.bind("click",vibrationSensorManagement.subChkClick);
	    //批量删除
	    $("#del_model").bind("click",vibrationSensorManagement.delModelClick);
        $("#refreshTable").bind("click",vibrationSensorManagement.refreshTable);
	})
})(window,$)