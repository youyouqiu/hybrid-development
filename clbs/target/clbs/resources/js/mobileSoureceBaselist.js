/**
 * 
 */
/**
 * 
 */(function(window,$){
	//显示隐藏列
	var menu_text = "";
	var table = $("#dataTable tr th:gt(1)");
    //单选
    var subChk = $("input[name='subChk']");
    mobileSourseBase = {
		//初始化
		init: function(){
			menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(2) +"\" disabled />"+ table[0].innerHTML +"</label></li>"
			for(var i = 1; i < table.length; i++){
			    menu_text += "<li><label><input type=\"checkbox\" checked='checked' class=\"toggle-vis\" data-column=\"" + parseInt(i+2) +"\" />"+ table[i].innerHTML +"</label></li>"
			};
			$("#Ul-menu-text").html(menu_text);
            var $keepOpen = $('.keep-open');
            
            $keepOpen.find('li').off('click').on('click', function (event) {
                 event.stopImmediatePropagation();
             });
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
		    }, /*{
		        "data": null,
		        "class": "text-center",
		       render: function (data, type, row, meta) {
		            var result = '';
		            result += '<input  type="checkbox" name="subChk"  value="' + row.id + '" />';
		            return result;
		        }
		    },*/
	        {
	            "data": null,
	            "class": "text-center", //最后一列，操作按钮
	            render: function (data, type, row, meta) {	            	
	                var editUrlPath = myTable.editUrl+"&vehicleId="+row.id+""; //修改地址
	                var detailurl="/clbs/m/energy/mobileSourceBaseInfo/page?page=detail";
	                var detailurlPath=detailurl+"&vehicleId="+row.id+"";
	                var deleteUrlPath=myTable.detailurl+"&vehicleId="+row.id+"";
	                var result = '';
	                if (row.isSet == 0) {
						// 设置
						result += '<button href="'+editUrlPath+'"  data-toggle="modal" data-target="#commonSmWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>设置</button>&ensp;';
                        // 禁用删除按钮
						 result += '<button disabled type="button" onclick="mobileSourseBase.clearbaseinfo('+row.id+')" class="deleteButton editBtn btn-default clearbaseinfo"><i class="fa fa-ban"></i>清空基准</button>&ensp;';
	                        // 禁用下发参数
	                    //    result += '<button disabled type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
	                        // 详细信息
	                        result += '<button href="'+detailurlPath+'" data-toggle="modal" data-target="#commonSmWin" type="button" class=" editBtn editBtn-info"><i class="fa fa-eye"></i>详情</button>&ensp;';
	                 }else {
						// 修改
	                	 result += '<button href="'+editUrlPath+'"  data-toggle="modal" data-target="#commonSmWin"  type="button" class="editBtn editBtn-info"><i class="fa fa-set"></i>设置</button>&ensp;';
	                        // 可用删除按钮
							 result += '<button  type="button" id='+row.id+' onclick="mobileSourseBase.clearbaseinfo(this.id);" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>清空基准</button>&ensp;';
		                        // 禁用下发参数
		                    //    result += '<button disabled type="button" class="editBtn btn-default deleteButton"><i class="fa fa-ban"></i>下发参数</button>&ensp;';
		                        // 详细信息
		                        result += '<button href="'+detailurlPath+'" data-toggle="modal" data-target="#commonSmWin" type="button" class=" editBtn editBtn-info"><i class="fa fa-eye"></i>详情</button>&ensp;';
	                    }
					return result;
	            }
	        }, {
	            "data": "brand",
	            "class": "text-center",
	            render: function (data, type, row, meta) {
	            	if(data!=null&&data!=undefined){
	            		return data;
	            	}else{
	            		return "";
	            	}
	            }
	           
	        }, {
	            "data": "groupName",
	            "class": "text-center"
	        }, {
	            "data": "vehType",
	            "class": "text-center",
	            render: function (data, type, row, meta) {
	            	if(data!=null&&data!=undefined){
	            		return data;
	            	}else{
	            		return "";
	            	}
	            }
	            	
	        }, {
	            "data": "fuelType",
	            "class": "text-center",
	            render: function (data, type, row, meta) {
	            	if(data!=null&&data!=undefined){
	            		return data;
	            	}else{
	            		return "";
	            	}
	            }
	        }, {
	            "data": "travelBase",
	            "class": "text-center",
	            render: function (data, type, row, meta) {
	            	if(data!=null&&data!=undefined){
	            		return data;
	            	}else{
	            		return "";
	            	}
	            }
	        }, {
	            "data": "idleBase",
	            "class": "text-center",
	            render: function (data, type, row, meta) {
	            	if(data!=null&&data!=undefined){
	            		return data;
	            	}else{
	            		return "";
	            	}
	            }
	        }, /*{
	            "data": "msgLength",
	            "class": "text-center",
	            render: function (data, type, row, meta) {
	            	if(data!=null&&data!=undefined){
	            		return data;
	            	}else{
	            		return "";
	            	}
	            }
	        },*/{
	            "data": "idleThreshold",
	            "class": "text-center",
	            render: function (data, type, row, meta) {
	            	if(data!=null&&data!=undefined){
	            		return data;
	            	}else{
	            		return "";
	            	}
	            }
	        },{
	            "data": "installTime",
	            "class": "text-center",
	            render: function (data, type, row, meta) {
	            	if(data!=null&&data!=undefined){
	            		return (data != null && data.length >= 10) ? data.substr(0, 10) : "";
	            	}else{
	            		return "";
	            	}
	            }
	        },];
		    //ajax参数
		    var ajaxDataParamFun = function (d) {
		        d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
		    };
		    //表格setting
		    var setting = {
		        listUrl: '/clbs/m/energy/mobileSourceBaseInfo/list',
		        editUrl: '/clbs/m/energy/mobileSourceBaseInfo/page?page=edit',
		        deleteUrl: '/clbs/m/energy/mobileSourceBaseInfo/page?page=delete',
		        deletemoreUrl: '/clbs/m/energy/mobileSourceBaseInfo/delete',
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
        //清空基准
        clearbaseinfo:function(data){
        	var id=data;
        	layer.open({
  	 			closeBtn: false,
  	 			//offset:'t',
  	 			title: '操作确认',
  	 		  icon: 3, // 问号图标
  	 			content: '是否确定清空基准',
  	 			btn:['确定','取消'],
  	 			btn1:function(index,layero){  
  	 				var url="/clbs/m/energy/mobileSourceBaseInfo/relieve/"+data+"";
  	 			 json_ajax("GET",url,"json",true,null,function getclearback(data){
  	 				if (data != null) {
  	                    //  var result = $.parseJSON(data);
  	                      if (data.success == true) {                 
  	                      	$("#"+id).removeClass("disableClick").addClass("clearbaseinfo btn-default");
  	                      $("#"+id).find("i").removeClass("fa-trash-o").addClass("fa-ban");
  	                              layer.msg(data.msg,{move:false});  
  	                              myTable.refresh();
  	                          }else{
  	                              if(data != null){
  	                                  layer.msg(data.msg,{move:false});
  	                              }
  	                          }
  	                      }else{
  	                          layer.msg(data.msg,{move:false});
  	                      }
  	        	})
  	 			
  	 			},
  	 			btn2:function(index,layero){
  	 				
  	 			}
  	 		});    
       
        },
    
        //批量删除    
         batchdel:function(){
        	 var value="";
        	  $('input[name="subChk"]:checked').each(function(){//遍历每一个名字为id的复选框，其中选中的执行函数      
        	       value+=$(this).val()+",";//将选中的值添加value中，以逗号分开     
        	       });  
        	 var url="/clbs/m/energy/mobileSourceBaseInfo/delete";  
        	 
	 			 json_ajax("GET",url,"json",true,{"ids":value}, mobileSourceEdit.batchdelback);
            }
        }
	
	$(function(){
		$('input').inputClear();
		mobileSourseBase.init();
		//全选
	    $("#checkAll").bind("click",mobileSourseBase.checkAllClick);
	    subChk.bind("click",mobileSourseBase.subChkClick);
	    //批量删除
	    $("#del_model").bind("click",mobileSourseBase.batchdel);
	    $("#refreshTable").on("click",mobileSourseBase.refreshTable);
	})
})(window,$)