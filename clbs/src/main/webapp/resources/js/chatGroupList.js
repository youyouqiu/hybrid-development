(function(window,$){
    //显示隐藏列
	var menu_text = "";
	var table = $("#dataTable tr th:gt(1)");
    //单选
    var subChk = $("input[name='subChk']");
	chatGroupFrom = {
		//初始化
		init: function(){
			//数据表格列筛选
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
                    result += "<input  type='checkbox' name='subChk'  value='" + row.groupId + "'/>";
		            return result;
		        }
		    },
	        {
	            "data": null,
	            "class": "text-center", //最后一列，操作按钮
	            render: function (data, type, row, meta) {
	                var editUrlPath = myTable.editUrl + row.groupId+".gsp"; //修改地址
	                var result = '';
                  	//修改按钮
	                result += '<button href="' + editUrlPath + '" data-target="#commonSmWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
	                //删除按钮
	                result += '<button type="button" onclick="myTable.deleteItem(\'' + row.groupId + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>';
	                return result;
	            }
	        },{
	            "data": "groupName",
	            "class": "text-center",
                    render:function(data){
                        return html2Escape(data);
                    }
	        }, {
	            "data": "groupId",
	            "class": "text-center",
	            render:function(data){
	            	return "<a href='/clbs/cb/chat/chatGroup/show_"+data+".gsp' id='addId' data-toggle='modal' data-target='#commonSmWin'>预览</a>";
	            }
	        },{
                    "data": "createDataTime",
                    "class": "text-center",
                    render:function(data){
                        return html2Escape(data);
                    }
                },{
                    "data": "createDataUsername",
                    "class": "text-center",
                    render:function(data){
                        return html2Escape(data);
                    }
                },{
                    "data": "groupRemark",
                    "class": "text-center",
                    render:function(data){
                        if (data != null && data != '') {
                            if (data.length > 20) {
                                return '<span class="demo demoUp" alt="' + html2Escape(data) + '">' + html2Escape(data).substring(0, 20) + "..." + '</span>';
                            }
                            else {
                                return html2Escape(data);
                            }
                        }
                        else {
                            return '';
                        }
                    }
                }];
		    //ajax参数
		    var ajaxDataParamFun = function (d) {
		        d.groupName = $('#simpleQueryParam').val(); //模糊查询
		    };
		    //表格setting
		    var setting = {
		        listUrl: '/clbs/cb/chat/chatGroup/list',
		        editUrl: '/clbs/cb/chat/chatGroup/edit_',
		        deleteUrl: '/clbs/cb/chat/chatGroup/delete_',
		        deletemoreUrl: '/clbs/cb/chat/chatGroup/deletemore',
		        columnDefs: columnDefs, //表格列定义
		        columns: columns, //表格列
		        dataTableDiv: 'dataTable', //表格
		        ajaxDataParamFun: ajaxDataParamFun, //ajax参数
		        pageable: true, //是否分页
		        showIndexColumn: true, //是否显示第一列的索引列
		        enabledChange: true,
				drawCallbackFun: function () {//鼠标移入后弹出气泡显示单元格内容；
                    $(".demoUp").mouseover(function () {
                        var _this = $(this);
                        if (_this.attr("alt")) {
                            _this.justToolsTip({
                                animation: "moveInTop",
                                width: "auto",
                                contents: _this.attr("alt"),
                                gravity: 'top'
                            });
                        }
                    })
                }
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
		updataFenceData: function(msg){
	        if (msg != null) {
	            var result = $.parseJSON(msg.body);
	            if (result != null) {
	                myTable.refresh();
	            }
	        }
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
            $("input[name='subChk']:checked").each(function() {
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
		chatGroupFrom.init();
		//全选
	    $("#checkAll").bind("click",chatGroupFrom.checkAllClick);
	    subChk.bind("click",chatGroupFrom.subChkClick);
	    //批量删除
	    $("#del_model").bind("click",chatGroupFrom.delModelClick);
	    //刷新
	    $("#refreshTable").on("click",chatGroupFrom.refreshTable);
	})
})(window,$)