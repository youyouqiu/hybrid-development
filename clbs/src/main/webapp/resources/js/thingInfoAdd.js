(function(window,$){
	addItemInformation = {
        init: function () {
            var setting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    contentType: "application/json",
                    dataType: "json",
                    dataFilter: addItemInformation.ajaxDataFilter
                },
                view: {
                    dblClickExpand: false,
                    nameIsHTML: true,
                    countClass: "group-number-statistics"
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    beforeClick: addItemInformation.beforeClick,
                    onClick: addItemInformation.onClick,
                    onAsyncSuccess: addItemInformation.zTreeOnAsyncSuccess
                }
            };
            $.fn.zTree.init($("#ztreeDemo"), setting, null);
        },
        zTreeOnAsyncSuccess:function(event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            treeObj.expandAll(true); // 展开所有节点
        },
        beforeClick: function (treeId, treeNode) {
            var check = (treeNode);
            return check;
        },
        onClick: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("ztreeDemo"),
                nodes = zTree.getSelectedNodes(),
                v = "";
            n = "";
            nodes.sort(function compare(a, b) {
                return a.id - b.id;
            });
            for (var i = 0, l = nodes.length; i < l; i++) {
                n += nodes[i].name;
                v += nodes[i].uuid + ";";
            }
            if (v.length > 0)
                v = v.substring(0, v.length - 1);
            var cityObj = $("#zTreeCitySel");
            cityObj.val(n);
            $("#selectGroup").val(v);
            $("#zTreeContent").hide();
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            addItemInformation.hideErrorMsg();//清除错误提示样式
            var isAdminStr = $("#isAdmin").attr("value");    // 是否是admin
            var isAdmin = isAdminStr == 'true';
            var userGroupId = $("#userGroupId").attr("value");  // 用户所属组织 id
            var userGroupName = $("#userGroupName").attr("value");  // 用户所属组织 name
            if (responseData != null && responseData != "" && responseData != undefined && responseData.length >= 1) {
                if ($("#selectGroup").val() == "") {
                    $("#selectGroup").val(responseData[0].uuid);
                    $("#zTreeCitySel").val(responseData[0].name);
                }
                return responseData;
            } else {
                addItemInformation.showErrorMsg("您需要先新增一个组织", "zTreeCitySel");
                return;
            }
        },
        showErrorMsg: function (msg, inputId) {
            if ($("#error_label_add").is(":hidden")) {
                $("#error_label_add").text(msg);
                $("#error_label_add").insertAfter($("#" + inputId));
                $("#error_label_add").show();
            } else {
                $("#error_label_add").is(":hidden");
            }
        },
        //错误提示信息隐藏
        hideErrorMsg: function () {
            $("#error_label_add").is(":hidden");
            $("#error_label_add").hide();
        },
		validates: function(){
	        return $("#addForm").validate({
	            rules: {
	    			thingNumber : {
	    				required : true,
                        checkThingNumber:true,
	                    remote: {
	    					type:"post",
	                        async:false,
	    					url:"/clbs/m/basicinfo/monitoring/ThingInfo/repetition" ,
	                        data:{  
	                              id : null,
	                         }
	                      }  
	    			},
					name : {
                        maxlength : 20,
					},
                    label : {
                        maxlength : 20,
                    },
                    model : {
                        maxlength : 20,
                    },
                    material : {
                        maxlength : 20,
                    },
	    			weight : {
	    				maxlength : 9,
                        isRightNumber : true,
	    				min: 0
	    			},
                    spec : {
                        maxlength : 20,
                    },
                    manufacture : {
                        maxlength : 20,
                    },
                    dealer : {
                        maxlength : 20,
                    },
                    place : {
                        maxlength: 10,
                    },
	    			remark: {
						maxlength: 50
					}
	            },
	            messages: {
					thingNumber : {
						required: '物品编号不能为空',
						maxlength: publicSize20,
						remote:thingInfoNumberExists
					},
                    name : {

                        maxlength: publicSize20,
                    },
                    label : {
                        maxlength: publicSize20,
                    },
                    model : {
                        maxlength: publicSize20,
                    },
                    material : {
                        maxlength: publicSize20,
                    },
					weight : {
						maxlength: '长度不能超过9位',
                        isRightNumber: '请输入正整数',
					    min: publicpositive
					},
                    spec : {
                        maxlength: publicSize20,
                    },
                    manufacture : {
                        maxlength: publicSize20,
                    },
                    dealer : {
                        maxlength: publicSize20,
                    },
                    place : {
                        maxlength: publicSize10,
                    },
					remark: {
						maxlength: publicSize50
					}
	            }
	        }).form();
		},
		doSubmits: function(){
		    if(addItemInformation.validates()){
		        addHashCode1($("#addForm"));
		        $("#addForm").ajaxSubmit(function(data) {
		        	var json = eval("("+data+")");
	            	if(json.success){
	            		$("#commonWin").modal("hide");
                        myTable.requestData();
	           		}else{
	           			layer.msg(json.msg);
	           		}
		   		});
		  	}
		},
        setImagePreview: function (avalue) {
            addItemInformation.uploadImage(); // 上传图片到服务器
            var docObj = document.getElementById("doc");
            var imgObjPreview = document.getElementById("preview");
            if (docObj.files && docObj.files[0]) {
                //火狐下，直接设img属性
                imgObjPreview.style.display = 'block';
                imgObjPreview.style.width = '200px';
                imgObjPreview.style.height = '200px';
                //火狐7以上版本不能用上面的getAsDataURL()方式获取，需要一下方式
                if (window.navigator.userAgent.indexOf("Chrome") >= 1 || window.navigator.userAgent.indexOf("Safari") >= 1) {
                    imgObjPreview.src = window.webkitURL.createObjectURL(docObj.files[0]);
                } else {
                    imgObjPreview.src = window.URL.createObjectURL(docObj.files[0]);
                }
            }
            else {
                //IE下，使用滤镜
                docObj.select();
                var imgSrc = document.selection.createRange().text;
                var localImagId = document.getElementById("localImag");
                //必须设置初始大小
                localImagId.style.width = "200px";
                localImagId.style.height = "200px";
                //图片异常的捕捉，防止用户修改后缀来伪造图片
                try {
                    localImagId.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale)";
                    localImagId.filters.item("DXImageTransform.Microsoft.AlphaImageLoader").src = imgSrc;
                }
                catch (e) {
                    layer.msg("不支持的图片格式文件，<br/>支持格式（png，jpg，gif，jpeg）");
                    return false;
                }
                imgObjPreview.style.display = 'none';
                document.selection.empty();
            }
            return true;
        },
        uploadImage: function () {
            var docObj = document.getElementById("doc");
            if (docObj.files && docObj.files[0]) {
                var formData = new FormData();
                formData.append("file", docObj.files[0]);
                $.ajax({
                    url: '/clbs/m/basicinfo/enterprise/professionals/upload_img',
                    type: 'POST',
                    data: formData,
                    async: false,
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function (data) {
                        data = $.parseJSON(data);
                        if (data.imgName == "0") {
                            layer.msg("不支持的图片格式文件，<br/>支持格式（png，jpg，gif，jpeg）");
                            $("#preview").src("");
                        } else {
                            $("#thingPhoto").val(data.imgName);
                        }
                    },
                    error: function (data) {
                        layer.msg("上传失败！");
                    }
                });
            }
        },
        showMenu: function (e) {
            if ($("#zTreeContent").is(":hidden")) {
                var width = $(e).parent().width();
                $("#zTreeContent").css("width", width + "px");
                $(window).resize(function () {
                    var width = $(e).parent().width();
                    $("#zTreeContent").css("width", width + "px");
                })
                $("#zTreeContent").show();
            } else {
                $("#zTreeContent").hide();
            }

            $("body").bind("mousedown", addItemInformation.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "zTreeContent" || $(
                    event.target).parents("#zTreeContent").length > 0)) {
                addItemInformation.hideMenu();
            }
        },
        hideMenu: function () {
            $("#zTreeContent").fadeOut("fast");
            $("body").unbind("mousedown", addItemInformation.onBodyDown);
        },
	}
	$(function(){
        addItemInformation.init();
        laydate.render({elem: '#productDate', theme: '#6dcff6'});
		$('input').inputClear();
		$("#doSubmitsAdd").bind("click",addItemInformation.doSubmits);
        $("#zTreeCitySel").on("click", function () {
            addItemInformation.showMenu(this)
        });
        // 组织树input框的模糊搜索
        $("#zTreeCitySel").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("ztreeDemo");
            treeObj.checkAllNodes(false);
            search_ztree('ztreeDemo', 'zTreeCitySel', 'group');
        });
        // 组织树input框快速清空
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            var treeObj;
            if (id == 'zTreeCitySel') {
                search_ztree('ztreeDemo', 'zTreeCitySel', 'group');
                treeObj = $.fn.zTree.getZTreeObj("ztreeDemo");
            }
            treeObj.checkAllNodes(false)
        });
	})
})(window,$)

$.validator.addMethod("checkThingNumber",function(value,element,params){
    var checkThingNumber = /^[\u4e00-\u9fa5-a-zA-Z0-9]{2,20}$/;
    return this.optional(element)||(checkThingNumber.test(value));
},"请输入汉字、字母、数字或短横杠，长度2-20位");