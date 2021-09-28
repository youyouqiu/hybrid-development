(function(window,$){
	var bindId = $("#bindId").val();
	var installTime = $("#installTime").val();
    var deviceNumber = $("#deviceNumber").val();
    var oldDeviceNumber = $("#deviceNumber").val();
    var deviceType = $("#deviceType").val();
    var deviceNumberError = $("#deviceNumber-error");
    var deviceFlag = false;
	var fts="";
	editTerminalManagement = {
        init: function () {
            if (bindId != null && bindId != '') {
                $("#deviceNumber").attr("readonly", true);
                $("#deviceType").attr("disabled", true);
                $("#bindMsg").attr("hidden", false);
            }
            var setting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    contentType: "application/json",
                    dataType: "json",
                },
                view: {
                    dblClickExpand : false,
                    nameIsHTML: true,
                    countClass: "group-number-statistics"
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    beforeClick: editTerminalManagement.beforeClick,
                    onClick: editTerminalManagement.onClick,
                    onAsyncSuccess: editTerminalManagement.zTreeOnAsyncSuccess
                }
            };
            $.fn.zTree.init($("#ztreeDemo"), setting, null);
            fts = $("#functionalType").val();//获取当前终端通讯类型
            laydate.render({elem: '#installDateEdit', theme: '#6dcff6'});
            laydate.render({elem: '#procurementDateEdit', theme: '#6dcff6'});
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
            var zTree = $.fn.zTree.getZTreeObj("ztreeDemo"), nodes = zTree
                .getSelectedNodes(), n = "";
            v = "";
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
            cityObj.attr("value", v);
            cityObj.val(n);
            $("#groupId").val(v);
            $("#zTreeContent").hide();
        },
        //显示菜单
        showMenu: function () {
            if ($("#zTreeContent").is(":hidden")) {
                var inpwidth = $("#zTreeCitySel").width();
                var spwidth = $("#zTreeCitySelSpan").width();
                var allWidth = inpwidth + spwidth + 21;
                if (navigator.appName == "Microsoft Internet Explorer") {
                    $("#zTreeContent").css("width", (inpwidth + 7) + "px");
                } else {
                    $("#zTreeContent").css("width", allWidth + "px");
                }
                $(window).resize(function () {
                    var inpwidth = $("#zTreeCitySel").width();
                    var spwidth = $("#zTreeCitySelSpan").width();
                    var allWidth = inpwidth + spwidth + 21;
                    if (navigator.appName == "Microsoft Internet Explorer") {
                        $("#zTreeContent").css("width", (inpwidth + 7) + "px");
                    } else {
                        $("#zTreeContent").css("width", allWidth + "px");
                    }
                })
                $("#zTreeContent").show();
            } else {
                $("#zTreeContent").hide();
            }
            $("body").bind("mousedown", editTerminalManagement.onBodyDown);
        },
        hideMenu: function () {
            $("#zTreeContent").fadeOut("fast");
            $("body").unbind("mousedown", editTerminalManagement.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "zTreeContent" || $(
                    event.target).parents("#zTreeContent").length > 0)) {
                editTerminalManagement.hideMenu();
            }
        },
        deviceNumberValidates: function () {
            // if (deviceType == "5") {
            //     var regName = /^(?=.*[0-9a-zA-Z])[0-9a-zA-Z]{1,20}$/;
            //     if (deviceNumber != "" && !regName.test(deviceNumber)) {
            //         // deviceNumberError.html("请输入字母/数字/短杠，范围（车）7~15（人）1~20");
            //         deviceNumberError.html("请输入字母/数字，范围（车）7~15（人）1~20");
            //         deviceNumberError.show();
            //         deviceFlag = false;
            //     }
            //     else if (deviceNumber == "") {
            //         deviceNumberError.html("请输入终端号，范围：1~20");
            //         deviceNumberError.show();
            //         deviceFlag = false;
            //     }
            //     else {
            //         editTerminalManagement.deviceAjax();
            //     }
            // }
            // else {
            //     var regName = /^(?=.*[0-9a-zA-Z])[0-9a-zA-Z]{7,15}$/;
            //     if (deviceNumber != "" && !regName.test(deviceNumber)) {
            //         // deviceNumberError.html("请输入字母/数字/短杠，范围（车）7~15（人）1~20");
            //         deviceNumberError.html("请输入字母/数字，范围（车）7~15（人）1~20");
            //         deviceNumberError.show();
            //         deviceFlag = false;
            //     }
            //     else if (deviceNumber == "") {
            //         deviceNumberError.html("请输入终端号，范围：7~15");
            //         deviceNumberError.show();
            //         deviceFlag = false;
            //     }
            //     else {
            //         editTerminalManagement.deviceAjax();
            //     }
            // }
            var regName = /^(?=.*[0-9a-zA-Z])[0-9a-zA-Z]{7,30}$/;
            if (deviceNumber != "" && !regName.test(deviceNumber)) {
                deviceNumberError.html(deviceNumberError2);
                deviceNumberError.show();
                deviceFlag = false;
            } else if (deviceNumber == "") {
                deviceNumberError.html(deviceNumberNull);
                deviceNumberError.show();
                deviceFlag = false;
            } else {
                editTerminalManagement.deviceAjax();
            }
        },
        deviceAjax: function () {
            deviceNumber = $("#deviceNumber").val();
            if (oldDeviceNumber != deviceNumber) {
                $.ajax({
                        type: "post",
                        url: "/clbs/m/basicinfo/equipment/device/repetition",
                        data: {deviceNumber: deviceNumber},
                        success: function (d) {
                            var result = $.parseJSON(d);
                            if (!result) {
                                deviceNumberError.html("终端号已存在！");
                                deviceNumberError.show();
                                deviceFlag = false;
                            }
                            else {
                                deviceNumberError.hide();
                                deviceFlag = true;
                            }
                        }
                    }
                )
            }else{
                deviceFlag = true;
            }
        },
		validates: function(){
			 return $("#editForm").validate({
				 rules : {
				 deviceNumber : {
					required : true,
                    checkDeviceNumber : "#deviceType",
					remote: {
						type:"post",
						async:false,
						url:"/clbs/m/basicinfo/equipment/device/repetition" ,
						dataType:"json",
	                    data:{
	                          username:function(){return $("#deviceNumber").val();}
	                     },
	                     dataFilter: function(data, type) {
	                    	 var oldV = $("#scn").val();
	              			var newV = $("#deviceNumber").val();
	              			var data2 = data;
	              			if (oldV == newV) {
	              				return true;
	              			} else {
	              				if (data2 == "true"){
	                            		return true;
	                             } else {
	                            		return false;
	                             }
	              			}
	                      }
	                   }
				},
				groupId : {
					required : true
				},
                 deviceType: {
                     required: true,
                     maxlength: 50
                 },
                 terminalManufacturer: {
                     required: true,
                 },
                 terminalTypeId: {
                     required: true,
                 },
                 functionalType: {
                     required: true,
                     maxlength: 50
                 },
                 deviceName: {
                     required: false,
                     maxlength: 50
                 },
                 barCode: {
                     maxlength: 64
                 },
                 isStart: {
                     required: false,
                     maxlength: 6
                 },
                 manuFacturer: {
                     maxlength: 100
                 },
                 remark: {
                     required: false,
                     maxlength: 50
                 },
                 manufacturerId: {
				     maxlength:11
                 },
                 deviceModelNumber:{
                     maxlength:30
                 },
                 macAddress: {
                     remote: {
                         type: "post",
                         async: false,
                         url: "/clbs/m/basicinfo/equipment/device/repetitionMacAddress",
                         data: {
                             deviceId: function () {
                                 return $("#deviceId").val();
                             },
                             macAddress: function () {
                                 return $("#macAddress").val();
                             }
                         },
                         dataFilter: function (data) {
                             return JSON.parse(data).success;
                         }
                     },
                     isMacAddress: true
                 },
             },
             messages: {
                 macAddress: {
                     remote:'该MAC地址已存在'
                 },
                 deviceNumber: {
                     required: deviceNumberNull,
                     checkDeviceNumber: deviceNumberError2,
                     remote: deviceNumberExists
                 },
                 groupId: {
                     required: publicNull
                 },
                 deviceType: {
                     required: deviceTypeNull,
                     maxlength: publicSize50
                 },
                 terminalManufacturer: {
                     required: "请选择终端厂商",
                 },
                 terminalTypeId: {
                     required: "请选择终端型号",
                 },
                 functionalType: {
                     required: publicNull,
                     maxlength: publicSize50
                 },
                 deviceName: {
                     required: publicNull,
                     maxlength: publicSize50
                 },
                 barCode: {
                     maxlength: publicSize64
                 },
                 isStart: {
                     required: publicNull,
                     maxlength: publicSize6
                 },
                 manuFacturer: {
                     required: publicNull,
                     maxlength: publicSize100
                 },
                 remark: {
                     maxlength: publicSize50
                 },
                 manufacturerId:{
                     maxlength:'长度不超过11位'
                 },
                 deviceModelNumber:{
                     maxlength:publicSize30
                 }
		   }}).form();
	    },
	    //提交
	    doSubmit: function(){
			deviceType = $("#deviceType").val();
			deviceNumber = $("#deviceNumber").val();
            //editTerminalManagement.deviceNumberValidates();
            if (editTerminalManagement.validates()) {
                addHashCode1($("#editForm"));
                $("#editForm").ajaxSubmit(function (data) {
                    var json = eval("(" + data + ")");
                    if (json.success) {
                        $("#commonWin").modal("hide");
                        myTable.refresh();
                    } else {
                        layer.msg(json.msg);
                    }
                });
            };
		},
		functionalTypeInit:function(){
			$("#functionalType").val(fts);
		},
        getTerminalManufacturer: function () {
            var url = "/clbs/m/basicinfo/equipment/device/TerminalManufacturer";
            json_ajax("GET", url, "json", false, null, editTerminalManagement.TerminalManufacturerCallBack);
        },
        TerminalManufacturerCallBack: function (data) {
            var result = data.obj.result;
            var str = "";
            var terminalManufacturerName = $("#terminalManufacturerName").val();
            for (var i = 0; i < result.length; i++) {
                if (terminalManufacturerName == result[i]) {
                    str += '<option selected="selected" value="' + result[i] + '">' + html2Escape(result[i]) + '</option>'
                } else {
                    str += '<option  value="' + result[i] + '">' + html2Escape(result[i]) + '</option>'
                }
            }
            $("#terminalManufacturer").html(str);
            editTerminalManagement.getTerminalType($("#terminalManufacturer").val());
        },

        getTerminalType: function (name) {
            var url = "/clbs/m/basicinfo/equipment/device/getTerminalTypeByName";
            json_ajax("POST", url, "json", false, {'name':name}, editTerminalManagement.getTerminalTypeCallback);
        },
        getTerminalTypeCallback: function (data) {
            var vt = $("#terminalTypeId").attr("tid");
            var result = data.obj.result;
            var str = "";
            if (result != null && result != '') {
                for (var i = 0; i < result.length; i++) {
                    if (vt == result[i].id) {
                        str += '<option selected="selected" value="' + result[i].id + '">' + html2Escape(result[i].terminalType) + '</option>'
                    } else {
                        str += '<option  value="' + result[i].id + '">' + html2Escape(result[i].terminalType) + '</option>'
                    }
                }
            }
            $("#terminalTypeId").html(str);
            $("#terminalType").val( $("#terminalTypeId").find("option:selected").text());
        },

        // 通讯类型初始化
        agreementType(){
            var url = '/clbs/m/connectionparamsset/protocolList';
            var param={"type":808}
            json_ajax("POST", url, "json", false, param, function (data) {
                var data = data.obj;
                for(var i=0; i<data.length; i++){
                    $('#deviceType').append(
                        "<option value='"+data[i].protocolCode+"'>"+data[i].protocolName+"</option>"
                    );
                }
            });

            var val = $("#agreementType").val();
            $('#deviceType').val(val);
        }
	}
	$(function(){
        editTerminalManagement.agreementType();
		$('input').inputClear();
		editTerminalManagement.init();
		editTerminalManagement.functionalTypeInit();
        editTerminalManagement.getTerminalManufacturer();
        $('#terminalManufacturer').on("change",function () {
            var terminalManufacturerName = $(this).find("option:selected").attr("value");
            editTerminalManagement.getTerminalType(terminalManufacturerName);
        })

        $('#terminalTypeId').on("change",function () {
            var terminalType = $(this).find("option:selected").text();
            $("#terminalType").val(terminalType);
        })
		//显示菜单
		$("#zTreeCitySel").bind("click",editTerminalManagement.showMenu);
		$("#installTime").val(installTime!=null?installTime.substr(0, 10):"");
		//表单提交
		$("#doSubmit").bind("click",editTerminalManagement.doSubmit);
        $("#deviceType").on("change", function () {
            deviceType = $(this).val();
            deviceNumber = $("#deviceNumber").val();
            editTerminalManagement.deviceNumberValidates();
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