(function(window,$){
	//显示隐藏列
	var menu_text = "";
	var table = $("#dataTable tr th:gt(1)");
    //单选
    var subChk = $("input[name='subChk']");
	flowSensorManagement = {
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
	                    if(row.id == 'default2'){

						}else {
                            result += '<input  type="checkbox" name="subChk"  value="' + data.id + '" />';
                        }
	                    return result;
	                }
	            },
	            {
	                "data" : null,
	                "class" : "text-center", //最后一列，操作按钮
	                render : function(data, type, row, meta) {
	                	var editUrlPath = myTable.editUrl + data.id+".gsp"; //修改地址
	                    var result = '';
	                    if (row.id == "default2") {
	                    	//禁止修改按钮
		                    result += '<button disabled data-target="#commonWin" data-toggle="modal" type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>修改</button>&ensp;';
		                    //禁止删除按钮
		                    result += '<button disabled type="button" class="deleteButton editBtn btn-default"><i class="fa fa-ban"></i>删除</button>';
	                    } else {
	                    	//修改按钮
		                    result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
		                    //删除按钮
		                    result += '<button type="button" onclick="myTable.deleteItem(\''
		                            + data.id
		                            + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
	                    }
	                    return result;
	                }
	            }, {
	                "data" : "sensorNumber",
	                "class" : "text-center"
	            }, {
	                "data" : "compensateName",
	                "class" : "text-center",
	            } ,{
					"data" : "filterFactorName",
					"class" : "text-center",
				},{
		            "data": "remark",
		            "class": "text-center",
		            render:function(data){
		            	return html2Escape(data)
		            }
		        }
	        ];
		    //ajax参数
		    var ajaxDataParamFun = function(d) {
		        d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
		    };
		    //表格setting
		    var setting = {
		        listUrl : '/clbs/v/TransduserMgt/list_2',
		        editUrl : '/clbs/v/humidity/management/edit_',
		        deleteUrl : '/clbs/v/TransduserMgt/delete_',
		        deletemoreUrl : '/clbs/v/TransduserMgt/deleteMore',
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
	    //加载完成后执行
	    refreshTableClick: function(){
        	$("#simpleQueryParam").val("");
        	myTable.requestData();
	    },
	}
	$(function(){
		$('input').inputClear();
		flowSensorManagement.init();
	    //全选
	    $("#checkAll").bind("click",flowSensorManagement.checkAllClick);
	    //单选
	    subChk.bind("click",flowSensorManagement.subChkClick);
	    //批量删除
	    $("#del_model").bind("click",flowSensorManagement.delModelClick);
	    //加载完成后执行
        $("#refreshTable").bind("click",flowSensorManagement.refreshTableClick);
		
	})
})(window,$)