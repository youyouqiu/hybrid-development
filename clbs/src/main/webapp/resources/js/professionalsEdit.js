//# sourceURL=professionalsEdit.js
(function ($, window) {
    var positionTypeId = "ed057aa7-64b8-4ec1-9b14-dbc62b4286d4";
    professionals = {
        //初始化
        init: function () {
            if (navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.match(/9./i) == "9.") {
                $("#preview").attr("src", "/clbs/resources/assets/img/showPhoto.png").show();
            }
            //图片路径
            var photograph = $("#photograph").attr("value");
            if (photograph != null && photograph != "") {
                var d = new Date();
                var radomNum = d.getTime();
                $("#preview").attr('src', photograph+"?t="+radomNum).show();
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
                    beforeClick: professionals.beforeClick,
                    onClick: professionals.onClick,
                    onAsyncSuccess: professionals.zTreeOnAsyncSuccess
                }
            };
            $.fn.zTree.init($("#ztreeDemo"), setting, null);
            laydate.render({elem: '#workDate', theme: '#6dcff6'});
            laydate.render({elem: '#birthdayDate', theme: '#6dcff6'});
            laydate.render({elem: '#drivingStartDate', theme: '#6dcff6'});
            laydate.render({elem: '#drivingEndDate', theme: '#6dcff6'});
            laydate.render({elem: '#icCardEndDate', theme: '#6dcff6'});
            laydate.render({elem: '#issueCertificateDate', theme: '#6dcff6'});

            $("[data-toggle='tooltip']").tooltip();
        },
        //ic卡录入相关字段锁定(只读)
        setLock: function(){
            var type = $('#editAddType').val();
            // console.log('录入类型', type);
            if(type == 1){
                $(".onlyRead").attr('disabled',true);
            }
        },
        type: function () {
            var typeQuery = "";
            var url = "/clbs/m/basicinfo/enterprise/professionals/listType";
            var data = {"professionalstype": typeQuery}
            json_ajax("POST", url, "json", false, data, professionals.findList);
        },
        findList: function (data) {
            var type = $("#positionType").attr("value");
            var result = data.records;
            var selected = '<option selected="selected" value="">- 请选择岗位类型 -</option>';
            for (var i = 0; i < result.length; i++) {

                // if(result[i].professionalstype == '驾驶员(IC卡)' && type != 'ed057aa7-64b8-4ec1-9b14-dbc62b4286d4'){
                //     continue;
                // }

                if (result[i].id == type) {
                    selected += '<option selected="selected" value="' + result[i].id + '">' + result[i].professionalstype + '</option>';
                } else {
                    selected += '<option  value="' + result[i].id + '">' + result[i].professionalstype + '</option>';
                }
            }
            ;
            $("#positionType").html(selected);
            $("#required").addClass('hide');

            if($("#positionType").val() == positionTypeId){
                $("#required").removeClass('hide');
            }
            $("#positionType").change("change",professionals.changePositionType);
            /*if(type == 'ed057aa7-64b8-4ec1-9b14-dbc62b4286d4'){
                $("#positionType").attr('disabled',true)
            }*/
        },
        changePositionType: function () {
            var select = $(this).val();
            var required = $('#required');
            if(select == positionTypeId){
                required.removeClass('hide');
            }else{
                required.addClass('hide');
            }
        },
        zTreeOnAsyncSuccess: function(event, treeId, treeNode, msg) {
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
            cityObj.val(n);
            $("#groupId").val(v);
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
            $("body").bind("mousedown", professionals.onBodyDown);
        },
        hideMenu: function () {
            $("#zTreeContent").fadeOut("fast");
            $("body").unbind("mousedown", professionals.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "zTreeContent" || $(event.target).parents("#zTreeContent").length > 0)) {
                professionals.hideMenu();
            }
        },
        doSubmit: function () {
            if (professionals.validates()) {
                var birthday = $("#birthdayDate").val();
                var time = professionals.getNowFormatDate();
                if (birthday != null && birthday > time) {
                    $("#birthError").text(professionalsBrithdayError);
                    $("#birthError").removeAttr("style");
                    return false;
                }
                addHashCode1($("#editForm"));
                $("#editForm").ajaxSubmit(function (data) {
                    var json = eval("(" + data + ")");
                    if (json.success) {
                        $("#commonWin").modal("hide");
                        myTable.refresh();
                        layer.msg('修改成功！');
                    } else {
                        layer.msg(json.msg);
                    }
                });
            }
            ;
        },
        validates: function () {
            return $("#editForm").validate({
                rules: {
                    name: {
                        required: true,
                        checkPeopleName: true,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/enterprise/professionals/repetition",
                            dataType: "json",
                            data: {
                                name: function () {
                                    return $("#name").val();
                                },
                                identity: function () {
                                    return $("#identity").val();
                                }
                            },
                            dataFilter: function (data, type) {
                                var oldV = $("#scn").val();
                                var newV = $("#name").val();
                                var data2 = data;
                                if (oldV == newV) {
                                    return true;
                                } else {
                                    if (data2 == "true") {
                                        return true;
                                    } else {
                                        return false;
                                    }
                                }
                            }
                        }
                        /*remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/enterprise/professionals/repetitionsNPI",
                            data: {
                                positionType: function () {
                                    return $("#positionType").val();
                                },
                                identity: function () {
                                    return $("#identity").val();
                                }
                            },
                            dataFilter: function(data, type) {
                                data = JSON.parse(data);
                                if(!data.success){
                                    return !(data.msg == '3');
                                }
                            }
                        }*/
                    },
                    groupName: {
                        required: true,
                        maxlength: 1000
                    },
                    positionType: {
                        maxlength: 20
                    },
                    identity: {
                        isIdCardNo: true,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/enterprise/professionals/repetitions",
                            dataType: "json",
                            data: {
                                identity: function () {
                                    return $("#identity").val();
                                },
                                id: function () {
                                    return $("#id").val();
                                },
                                name: function () {
                                    return $("#name").val();
                                },
                            },
                            dataFilter: function (data, type) {
                                if (data == "true") {
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        }
                        /*remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/enterprise/professionals/repetitionsNPI",
                            dataType: "json",
                            data: {
                                positionType: function () {
                                    return $("#positionType").val();
                                },
                                name: function () {
                                    return $("#name").val();
                                },
                            },
                            dataFilter: function(data, type) {
                                data = JSON.parse(data);
                                if(!data.success){
                                    if(data.msg == '1' || data.msg == '2' || data.msg == '4'){
                                        return false
                                    }
                                }
                                return true;
                            }
                        }*/
                    },
                    jobNumber: {
                        maxlength: 30
                    },
                    cardNumber: {
                        maxlength: 30
                    },
                    gender: {
                        maxlength: 4
                    },
                    phone: {
                        mobilePhone: true,
                        // required: true
                    },
                    phoneTwo: {
                        mobilePhone: true
                    },
                    phoneThree: {
                        mobilePhone: true
                    },
                    emergencyContact: {
                        maxlength: 20
                    },
                    emergencyContactPhone: {
                        isLandline: true
                    },
                    email: {
                        email: true,
                        maxlength : 20
                    },
                    operationNumber: {
                        maxlength: 64
                    },
                    drivingLicenseNo: {
                        maxlength: 64
                    },
                    remindDays: {
                        digits:true,
                        min: 0,
                        max:9999
                    },
                    drivingEndDate:{
                        compareDate:"#drivingStartDate"
                    },
                    landline:{
                        isLandline:true
                    },
                    /*qualificationCategory: {
                        digits: true,
                        minlength: 5,
                        maxlength: 5
                    },*/
                  drivingType: {
                    maxlength: 10,
                    isABCNumber: true,
                  },
                },
                messages: {
                    /*qualificationCategory: {
                        digits: '请输入5位数字',
                        minlength: '请输入5位数字',
                        maxlength: '请输入5位数字'
                    },*/
                    emergencyContactPhone: {
                        isLandline: telPhoneError
                    },
                    phoneTwo: {
                        mobilePhone: phoneError
                    },
                    phoneThree: {
                        mobilePhone: phoneError
                    },
                    remindDays: {
                        digits:naturalNumber,
                        min: integerFour,
                        max:integerFour
                    },
                    name: {
                        required: nameNull,
                        checkPeopleName: personnelNameNull,
                        remote: professionalsNameExistsIdentity
                    },
                    groupName: {
                        required: publicNull,
                        maxlength: publicSize1000Length
                    },
                    positionType: {
                        maxlength: publicSize20Length
                    },
                    identity: {
                        isIdCardNo: identityError,
                        remote: personnelIdentityExists
                    },
                    jobNumber: {
                        maxlength: publicSize30Length
                    },
                    cardNumber: {
                        maxlength: publicSize30Length
                    },
                    gender: {
                        maxlength: publicSize4Length
                    },
                    phone: {
                        mobilePhone: phoneError,
                        // required: '请输入手机号码'
                    },
                    emergencyContact: {
                        maxlength: publicSize20Length
                    },
                    email: {
                        email: emailError,
                        maxlength : publicSize20Length
                    },
                    operationNumber: {
                        maxlength: publicSize64Length
                    },
                    drivingLicenseNo: {
                        maxlength: publicSize64Length
                    },
                    drivingEndDate:{
                        compareDate:"结束时间必须大于开始时间！"
                    },
                  drivingType: {
                    maxlength: '长度不超过10',
                    isABCNumber: '只能输入字母和数字',
                  },
                }
            }).form();
        },
        //下面用于图片上传预览功能
        setImagePreview: function (avalue) {
            professionals.uploadImage(); // 上传图片到服务器
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
                var imgSrc = "/clbs/resources/img/showMediaImg.png";
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
                    alert(publicPictureError);
                    return false;
                }
                imgObjPreview.style.display = 'none';
                document.selection.empty();
            }
            return true;
        },
        //上传图片
        uploadImage: function () {
            var docObj = document.getElementById("doc");
            if (docObj.files && docObj.files[0]) {
                var formData = new FormData();
                formData.append("file", docObj.files[0]);
                formData.append("id", $('#id').val());
                $.ajax({
                    url: '/clbs/m/basicinfo/enterprise/professionals/uploadImg',
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
                            $("#preview").src("").hide();
                        } else {
                            $("#photograph").val(data.imgName).show();
                        }
                    },
                    error: function (data) {
                        layer.msg(publicUploadingError);
                    }
                });
            }
        },
        getNowFormatDate: function () {
            var now = new Date();
            var year = now.getFullYear();       //年
            var month = now.getMonth() + 1;     //月
            var day = now.getDate();            //日
            var hh = now.getHours(); //时
            var mm = now.getMinutes() % 60;  //分
            var ss = now.getSeconds();
            if (now.getMinutes() / 60 > 1) {
                hh += Math.floor(now.getMinutes() / 60);
            }
            var clock = year + "-";
            if (month < 10)
                clock += "0";
            clock += month + "-";

            if (day < 10)
                clock += "0";
            clock += day + " ";
            return (clock);
        },
        //点击显示隐藏驾驶信息
        hiddenparameterFn: function () {
            var clickId = $(this).attr('id');
            if (!($("#" + clickId + "-content").is(":hidden"))) {
                $("#" + clickId + "-content").slideUp();
                $("#" + clickId).children("font").text("显示更多");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-down");
            } else {
                $("#" + clickId + "-content").slideDown();
                $("#" + clickId).children("font").text("隐藏信息");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-up");
            }
        },
      // 身份证、从业资格证和驾驶证图片显示
      practitionersPhoto: function () {
        var identityCardPhotoUrl = $("#identityCardPhotoEdit").val();
        var qualificationCertificatePhotoUrl = $("#qualificationCertificatePhotoEdit").val();
        var driverLicensePhotoUrl = $("#driverLicensePhotoEdit").val();
        if (identityCardPhotoUrl != '') {
          identityCardPhotoUrl = identityCardPhotoUrl;
          $('#identityCardImageUrlEdit').attr('src', identityCardPhotoUrl);
        } else {
          $('#identityCardImageEdit').hide();
          $('#identityCardTextEdit').hide();
          // $('#practitionersInfoPhotoTableEdit').css('width', '75%');
        }
        if (qualificationCertificatePhotoUrl != '') {
          qualificationCertificatePhotoUrl = qualificationCertificatePhotoUrl;
          $('#qualificationCertificateImageUrlEdit').attr('src', qualificationCertificatePhotoUrl);
        } else {
          $('#qualificationCertificateImageEdit').hide();
          $('#qualificationCertificateTextEdit').hide();
          // $('#practitionersInfoPhotoTableEdit').css('width', '50%');
        }
        if (driverLicensePhotoUrl != '') {
          driverLicensePhotoUrl = driverLicensePhotoUrl;
          $('#driverLicenseImageUrlEdit').attr('src', driverLicensePhotoUrl);
        } else {
          $('#driverLicenseImageEdit').hide();
          $('#driverLicenseTextEdit').hide();
          // $('#practitionersInfoPhotoTableEdit').css('width', '25%');
        }
      }
    }
    $(function () {
        var photograph = $("#photograph").val();
        var src;
        if (photograph == "") {
            src = "/clbs/resources/img/showMedia_img.png";
            $("#preview").hide();
        } else {
            var d = new Date();
            var radomNum = d.getTime();
            src =  photograph+"?t="+radomNum;
            $("#preview").show();
        }
        professionals.practitionersPhoto();
        $("#preview").attr("src", src);
        professionals.init();
        professionals.setLock();
        $('input').inputClear();
        professionals.type();
        $("#zTreeCitySel").on("click", function () {
            professionals.showMenu(this);
        });
        $("#zTreeCitySel").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("ztreeDemo");
            treeObj.checkAllNodes(false);
            search_ztree('ztreeDemo', 'zTreeCitySel', 'group');
        });

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            var treeObj
            if (id == 'zTreeCitySel') {
                search_ztree('ztreeDemo', 'zTreeCitySel', 'group');
                treeObj = $.fn.zTree.getZTreeObj("ztreeDemo");
            }
            treeObj.checkAllNodes(false)
        });
        $("#doSubmitEdit").on("click", professionals.doSubmit);
        setTimeout('$(".delIcon").hide()', 100);

        $("#overSpeedHide").on("click", professionals.hiddenparameterFn);
    })
})($, window)