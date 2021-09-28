//# sourceURL=connectionparamssetAdd.js
(function (window, $) {
  add809platform = {
    doSubmit: function () {
      add809platform.hideErrorMsg();
      //若没有填写平台名称则默认为ip地址
      var platformNameAdd = $("#platformName_add");
      var pfn = platformNameAdd.val();
      if (undefined === pfn || "" === pfn) {
        platformNameAdd.val($("#ip_add").val());
      }
      if (add809platform.addValidate()) {
        json_ajax("POST", '/clbs/m/connectionparamsset/check809Unique', "json", true, {
          centerId:$('#centerId_add').val(),
          ip:$('#ip_add').val(),
          ipBranch:$('#ipBranch').val(),
        }, function (result) {
          if(result.success){
            $('#ipBranch').val($('#ipBranch').val().trim());
            addHashCode1($("#addForm"));
            $("#addForm").ajaxSubmit(function (data) {
              data = eval("(" + data + ")");
              if (data.success) {
                $("#commonWin").modal("hide");
                var url = "/clbs/m/connectionparamsset/list";
                json_ajax("POST", url, "json", true, null, platformCheck.getCallback);
              } else {
                layer.msg(data.msg);
              }
            });
          }else if(result.msg){
            layer.msg(result.msg)
          }
        });
      }
    },
    showErrorMsg: function (msg, inputId) {
      var errorLabelAdd = $("#error_label_add");
      if (errorLabelAdd.is(":hidden")) {
        errorLabelAdd.text(msg);
        errorLabelAdd.insertAfter($("#" + inputId));
        errorLabelAdd.show();
      } else {
        errorLabelAdd.is(":hidden");
      }
    },
    //错误提示信息隐藏
    hideErrorMsg: function () {
      $("#error_label_add").hide();
    },
    //校验
    addValidate: function () {
      return $("#addForm").validate({
        rules: {
          platformName: {
            required: true,
            remote: {
              type: 'post',
              async: false,
              url: '/clbs/m/connectionparamsset/checkPlatformNameUnique',
              data: {
                platformName: function () {
                  return $("#platformName_add").val();
                }
              }
            }
          },
          ipBranch: {
            required: true,
            isDigitsPoint: true,
            ipFilter: true,
            /*remote: {
                dataFilter: function (data, type) {
                    var values = $("#ipBranch").val().split(/\n|#/);
                    console.log('values', values);
                    var ipArr = [];
                    for (var i = 0; i < values.length; i++) {
                        var item = values[i];
                        if (item) {
                            ipArr.push(item);
                            var reg = /^((2[0-4]\d|25[0-5]|[01]?\d\d?)\.){3}(2[0-4]\d|25[0-5]|[01]?\d\d?)$/;
                            if (!reg.test(item)) {
                                ipFilterMsg = '请输入正确的ip地址';
                                return false;
                            }
                        }
                    }
                    if (ipArr.length > 10) {
                        ipFilterMsg = '最多输入10个ip地址';
                        return false;
                    }
                    return true;
                }
            }*/
          },
          protocolType: {
            required: true,
          },
          ip: {
            required: true,
            domainNameAndIp: true,
            rangelength: [3, 30]
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
            rangelength: [1, 20]
          },
          centerId: {
            required: true,
            checkCAENumber: "7,1,20",
            /* remote: {
                 type: 'post',
                 async: false,
                 url: '/clbs/m/connectionparamsset/check809CenterIdUnique',
                 data: {
                     centerId: function () {
                         return $("#centerId_add").val();
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
            checkVersion: true
          },
          groupId: {
            required: true
          },
          grouProperty: {
            required: true
          }
        },
        messages: {
          ipBranch: {
            required: '请输入从链路IP地址',
            // remote: ipFilterMsg
          },
          platformName: {
            required: "平台名称不能为空",
            remote: "该平台名称已存在，请重新输入！"
          },
          protocolType: {
            required: "请选择协议类型",
          },
          ip: {
            required: "请输入域名或IP地址",
            domainNameAndIp:'请输入正确的域名或IP地址',
            rangelength: '请输入正确的域名或IP地址'
          },
          /*ipBranch: {
              required: "请输入从链路IP地址",
              batchIp: "请输入正确的IP地址",
          },*/
          port: {
            required: "请输入端口号",
            digits: "请输入正确的端口号",
            range: "请输入正确的端口号"
          },
          userName: {
            required: "请输入用户名",
            // checkNumber: "请输入字母或数字，范围1-20位",
            rangelength: '请输入字母或数字，范围1-20位'
          },
          password: {
            required: "请输入密码",
            rangelength: '长度1-20位'
          },
          centerId: {
            required: "请输入接入码",
            checkCAENumber: "请输入数字,范围1-20",
            remote: '接入码必须唯一，请重新填写'
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
          },
          groupId: {
            required: "请选择所属企业"
          },
          grouProperty: {
            required: "请选择企业类型"
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
          type: "post",
          enable: true,
          autoParam: ["id"],
          contentType: "application/json",
          dataType: "json",
          dataFilter: add809platform.ajaxDataFilter
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
          onClick: add809platform.onClick
        }
      };
      $.fn.zTree.init($("#ztreeDemo"), setting, null);
    },
    ajaxDataFilter: function (treeId, parentNode, responseData) {
      add809platform.hideErrorMsg();//清除错误提示样式
      if (responseData != null && responseData !== "" && responseData !== undefined && responseData.length >= 1) {
        var selectGroup = $("#selectGroup");
        if (selectGroup.val() === "") {
          selectGroup.val(responseData[0].uuid);
          $("#zTreeCitySel").val(responseData[0].name);
        }
        return responseData;
      }
      add809platform.showErrorMsg("您需要先新增一个组织", "zTreeCitySel");
    },
    onClick: function (e, treeId, treeNode) {
      $("#zTreeCitySel").val(treeNode.name);
      $("#selectGroup").val(treeNode.uuid);
      $("#zTreeContent").hide();
    },
    showMenu: function (e) {
      var zTreeContent = $("#zTreeContent");
      if (zTreeContent.is(":hidden")) {
        var width = $(e).parent().width();
        zTreeContent.css("width", width + "px");
        $(window).resize(function () {
          var width = $(e).parent().width();
          zTreeContent.css("width", width + "px");
        })
        zTreeContent.show();
      } else {
        zTreeContent.hide();
      }

      $("body").bind("mousedown", add809platform.onBodyDown);
    },
    onBodyDown: function (event) {
      if (!(event.target.id === "menuBtn" || event.target.id === "zTreeContent" || $(
          event.target).parents("#zTreeContent").length > 0)) {
        add809platform.hideMenu();
      }
    },
    hideMenu: function () {
      $("#zTreeContent").fadeOut("fast");
      $("body").unbind("mousedown", add809platform.onBodyDown);
    },

    //协议初始化
    agreementType() {
      var url = '/clbs/m/connectionparamsset/protocolList';
      var param = {"type": 809};
      json_ajax("POST", url, "json", false, param, function (data) {
        var data = data.obj;
        for (var i = 0; i < data.length; i++) {
          var item = data[i];
          $('#protocolType_add').append(
              "<option value='" + item.protocolCode + "'>" + item.protocolName + "</option>"
          );
        }
      })
    }
  };
  $(function () {
    add809platform.agreementType();
    $('input').inputClear();
    $('#ipBranch').inputClear();
    add809platform.init();
    $("#zTreeCitySel").on("click", function () {
      add809platform.showMenu(this)
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
      var reg = /^[0-9]*$/ ;
      var reg1 = /^[a-zA-Z0-9]+$/;
      var val = $("#protocolType_add option:selected").val();

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
})(window, $);