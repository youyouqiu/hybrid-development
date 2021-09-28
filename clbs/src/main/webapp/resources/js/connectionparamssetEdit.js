//# sourceURL=connectionparamssetEdit.js 
(function (window, $) {
	edit809platform = {
		doSubmit: function () {
			edit809platform.hideErrorMsg();
			//若没有填写平台名称则默认为ip地址
			var pfn = $("#platformName_edit").val();
			if (undefined == pfn || "" == pfn) {
				$("#platformName_edit").val($("#ip_edit").val());
			}
//			var cFlag = false;
//			var curl = "/clbs/m/connectionparamsset/check809PlatFormSole";
//            json_ajax("POST",curl,"json",false,{platFormName : $("#platformName_edit").val(),"pid" : $("#id_edit").val()}, function(data) {
//            	if (data) {
//            		cFlag = true;
//            		return;
//            	}
//            	edit809platform.showErrorMsg("该平台名称已存在，请重新输入！", "platformName_edit");
//            });
			if (edit809platform.editValidate()) {
				json_ajax("POST", '/clbs/m/connectionparamsset/check809Unique', "json", true, {
					centerId: $('#centerId_edit').val(),
					ip: $('#ip_edit').val(),
					ipBranch: $('#ipBranch').val(),
					id: $('#id_edit').val(),
				}, function (result) {
					if (result.success) {
						$('#ipBranch').val($('#ipBranch').val().trim());
						addHashCode1($("#editForm"));
						$("#editForm").ajaxSubmit(function (data) {
							data = eval("(" + data + ")");
							if (data.success) {
								$("#commonWin").modal("hide");
								var url = "/clbs/m/connectionparamsset/list";
								json_ajax("POST", url, "json", true, null, platformCheck.getCallback);
							} else {
								layer.msg(data.msg);
							}
						});
					} else if (result.msg) {
						layer.msg(result.msg)
					}
				});
			}
		},
		showErrorMsg: function (msg, inputId) {
			if ($("#error_label_edit").is(":hidden")) {
				$("#error_label_edit").text(msg);
				$("#error_label_edit").insertAfter($("#" + inputId));
				$("#error_label_edit").show();
			} else {
				$("#error_label_edit").is(":hidden");
			}
		},
		//错误提示信息隐藏
		hideErrorMsg: function () {
			$("#error_label_edit").hide();
		},
		//校验
		editValidate: function () {
			return $("#editForm").validate({
				rules: {
					platformName: {
						required: true,
						remote: {
							type: "post",
							async: false,
							url: "/clbs/m/connectionparamsset/checkPlatformNameUnique",
							data: {
								platformName: function () {
									return $("#platformName_edit").val();
								},
								id: function () {
									return $("#id_edit").val();
								}

							},
						}
					},
					ipBranch: {
						required: true,
						isDigitsPoint: true,
						ipFilter: true,
						// remote: {
						//     dataFilter: function (data, type) {
						//         var values = $("#ipBranch").val().split('\n');
						//         for (var i = 0; i < values.length; i++) {
						//             if (values[i].length > 15) {
						//                 return false
						//             }
						//         }
						//         return true;
						//     }
						// }
					},
					protocolType: {
						required: true,
						// remote: {
						//     type:"post",
						//     async:false,
						//     url:"/clbs/m/connectionparamsset/check809ProtocolType" ,
						//     data:{
						//    	 protocolType:function(){return $("#protocolType_edit").val();},
						//    	 pid:function(){return $("#id_edit").val();},
						//     },
						// }
					},
					ip: {
						required: true,
						domainNameAndIp: true,
						rangelength: [3, 30]
						// remote: {
						//     type:"post",
						//     async:false,
						//     url:"/clbs/m/connectionparamsset/check809DateSole" ,
						//     data:{
						//         ip:function(){return $("#ip_edit").val();},
						//         groupId:function(){return $("#selectGroup").val();},
						//         centerId:function(){return $("#centerId_edit").val();},
						//         protocolType:function(){return $("#protocolType_edit").val();},
						//         pid:function(){return $("#id_edit").val();}
						//     },
						// }
					},
					port: {
						required: true,
						digits: true,
						range: [0, 65535]
					},
					userName: {
						required: true,
						checkNumber: true,
						rangelength: [1, 20]
					},
					password: {
						required: true,
						// checkCAENumber : "4,1,20"
						rangelength: [1, 20]
					},
					centerId: {
						required: true,
						checkCAENumber: "7,1,20",
						/*remote: {
                            type: 'post',
                            async: false,
                            url: "/clbs/m/connectionparamsset/check809CenterIdUnique",
                            data: {
                                id: function(){
                                    return $("#id_edit").val();
                                },
                                centerId: function (){
                                    return $("#centerId_edit").val();
                                }
                            }
                        }*/
					},
					m: {
						required: true,
						checkCAENumber: "7,1,20"
					},
					ia: {
						required: true,
						checkCAENumber: "7,1,20"
					},
					ic: {
						required: true,
						checkCAENumber: "7,1,20"
					},
					permitId: {
						required: true,
						checkCAENumber: "4,1,20"
					},
					zoneDescription: {
						required: true,
						checkCAENumber: "7,2,6"
					},
					platformId: {
						required: true,
						checkCAENumber: "4,1,20"
					},
					versionFlag: {
						required: true,
						checkVersion: "8,1,20"
					},
				},
				messages: {
					ipBranch: {
						required: '请输入从链路IP地址',
						// remote: '每行最大输入15位'
					},
					platformName: {
						required: "平台名称不能为空",
						remote: "该平台名称已存在，请重新输入！"
					},
					protocolType: {
						required: "请选择协议类型",
						// remote : "该协议类型下已有绑定平台，请重新选择！"
					},
					ip: {
						required: "请输入域名或IP地址",
						domainNameAndIp:'请输入正确的域名或IP地址',
						rangelength: '请输入正确的域名或IP地址'
						// remote:"相同协议类型及主链路IP地址下所属企业、接入码必须唯一，不同协议下主链路IP不能相同，请确认无误后再点击保存"
					},
					/*ipBranch : {
                        required : "请输入从链路IP地址",
                        batchIp : "请输入正确的IP地址",
                        // remote:"IP已存在，请检查后重新输入"
                     },*/
					port: {
						required: "请输入端口号",
						digits: "请输入正确的端口号",
						range: "请输入正确的端口号"
					},
					userName: {
						required: "请输入用户名",
						rangelength: '请输入字母或数字，范围1-20位'
					},
					password: {
						required: "请输入密码",
						// checkCAENumber : "请输入字母/数字,范围1-20"
						rangelength: '长度1-20位'
					},
					centerId: {
						required: "请输入接入码",
						checkCAENumber: "请输入数字,范围1-20",
						remote: "接入码必须唯一，请重新填写"
					},
					m: {
						required: "请输入m",
						checkCAENumber: "请输入数字,范围1-20"
					},
					ia: {
						required: "请输入ia",
						checkCAENumber: "请输入数字,范围1-20"
					},
					ic: {
						required: "请输入ic",
						checkCAENumber: "请输入数字,范围1-20"
					},
					permitId: {
						required: "请输入经营许可证号",
						checkCAENumber: "请输入数字或字母,范围1-20"
					},
					zoneDescription: {
						required: "请输入行政区号",
						checkCAENumber: "请输入数字，长度2-6位"
					},
					platformId: {
						required: "请输入平台id",
						checkCAENumber: "请输入字母/数字，长度1-20位"
					},
					versionFlag: {
						required: "请输入版本号",
						checkVersion: "请输入正确的版本号，例：1.1.1"
					}
				}
			}).form();
		},
		//企业树初始化
		init: function () {
			$(".modal-body").addClass("modal-body-overflow");
			$(".modal-body").css({"height": "auto", "max-height": ($(window).height() - 194) + "px"});

			var setting = {
				async: {
					url: "/clbs/m/basicinfo/enterprise/professionals/tree",
					tyoe: "post",
					enable: true,
					autoParam: ["id"],
					contentType: "application/json",
					dataType: "json",
					dataFilter: edit809platform.ajaxDataFilter
				},
				view: {
					dblClickExpand: false
				},
				data: {
					simpleData: {
						enable: true
					}
				},
				callback: {
					beforeClick: edit809platform.beforeClick,
					onClick: edit809platform.onClick
				}
			};
			$.fn.zTree.init($("#ztreeDemo"), setting, null);
		},
		ajaxDataFilter: function (treeId, parentNode, responseData) {
			edit809platform.hideErrorMsg();//清除错误提示样式
			if (responseData != null && responseData != "" && responseData != undefined && responseData.length >= 1) {
				if ($("#selectGroup").val() == "") {
					$("#selectGroup").val(responseData[0].uuid);
					$("#zTreeCitySel").val(responseData[0].name);
				}
				return responseData;
			} else {
				edit809platform.showErrorMsg("您需要先新增一个组织", "zTreeCitySel");
				return;
			}
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
				v += nodes[i].uuid + ",";
			}
			if (v.length > 0)
				v = v.substring(0, v.length - 1);
			var cityObj = $("#zTreeCitySel");
			cityObj.val(n);
			$("#selectGroup").val(v);
			$("#zTreeContent").hide();
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

			$("body").bind("mousedown", edit809platform.onBodyDown);
		},
		onBodyDown: function (event) {
			if (!(event.target.id == "menuBtn" || event.target.id == "zTreeContent" || $(
				event.target).parents("#zTreeContent").length > 0)) {
				edit809platform.hideMenu();
			}
		},
		hideMenu: function () {
			$("#zTreeContent").fadeOut("fast");
			$("body").unbind("mousedown", edit809platform.onBodyDown);
		},

		//协议初始化
		agreementType() {
			var url = '/clbs/m/connectionparamsset/protocolList';
			var param = {"type": 809};
			json_ajax("POST", url, "json", false, param, function (data) {
				var data = data.obj;
				for (var i = 0; i < data.length; i++) {
					var item = data[i];
					$('#protocolType_edit').append(
						"<option value='" + item.protocolCode + "'>" + item.protocolName + "</option>"
					);
				}
				var type = $("#agreementType").val();
				$('#protocolType_edit').val(type);
			})
		}
	};
	$(function () {
		edit809platform.agreementType();
		$('input').inputClear();
		$('#ipBranch').inputClear();
		edit809platform.init();
		$("#zTreeCitySel").on("click", function () {
			edit809platform.showMenu(this)
		});
		jQuery.validator.addMethod("domainNameAndIp", function (value, element) {
			var reg = /^(?=^.{3,255}$)[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+$/;
			var reg1 = /^(([0-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5])))\.)(([0-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5])))\.){2}([0-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5])))$/;

			if(reg.test(value) || reg1.test(value)){
				return true;
			}
			return false;
		}, "请输入正确的域名或IP地址");

		jQuery.validator.addMethod("checkNumber", function (value, element) {
			var reg = /^\d{1,}$/;
			var reg1 = /^[a-zA-Z0-9]+$/;
			var val = $("#protocolType_edit option:selected").val();

			if(val == 1603){
				if(!reg1.test(value)){
					jQuery.validator.messages.checkNumber = '请输入字母或数字，范围1-20位';
					return false;
				}else{
					return  true;
				}
			} else{
				if(!reg.test(value)){
					jQuery.validator.messages.checkNumber = '请输入数字，范围1-20位';
					return false;
				}else{
					return  true;
				}
			}
		},"");
	})
})(window, $)