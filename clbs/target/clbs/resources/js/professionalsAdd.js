//# sourceURL=professionalAdd.js
(function ($, window) {
    var isarea = '';
    var positionTypeId = "ed057aa7-64b8-4ec1-9b14-dbc62b4286d4";

    professionalsAdd = {
        //初始化
        init: function () {
            var setting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    contentType: "application/json",
                    dataType: "json",
                    dataFilter: professionalsAdd.ajaxDataFilter
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
                    beforeClick: professionalsAdd.beforeClick,
                    onClick: professionalsAdd.onClick,
                    onAsyncSuccess: professionalsAdd.zTreeOnAsyncSuccess
                }
            };
            $.fn.zTree.init($("#ztreeDemo"), setting, null);
            laydate.render({elem: '#workDateAdd', theme: '#6dcff6'});
            laydate.render({elem: '#birthday', theme: '#6dcff6'});
            laydate.render({
                elem: '#drivingStartDate',
                theme: '#6dcff6'
            });
            laydate.render({elem: '#drivingEndDate', theme: '#6dcff6'});
            laydate.render({elem: '#icCardEndDate', theme: '#6dcff6'});
            laydate.render({elem: '#issueCertificateDate', theme: '#6dcff6'});

            $("[data-toggle='tooltip']").tooltip();
        },
        type: function () {
            var typeQuery = "";
            var url = "/clbs/m/basicinfo/enterprise/professionals/listType";
            var data = {"professionalstype": typeQuery}
            json_ajax("POST", url, "json", false, data, professionalsAdd.findList);
        },
        findList: function (data) {
            var result = data.records;
            var dataListArray = [];
            var selected = '<option id="preference" value="">- 请选择岗位类型 -</option>';
            for (var i = 0; i < result.length; i++) {
                // if (result[i].professionalstype == '驾驶员(IC卡)') {
                //     continue;
                // }
                var item = result[i];
                var checked = item.professionalstype == "驾驶员(IC卡)";
                selected += '<option selected="'+ checked +'" value=' + item.id + '>' + item.professionalstype + '</option>';

            }
            ;
            $("#positionType").html(selected);
            $("#required").addClass('hide');
            $("#preference").selected(true);

            $('#positionType').on('change', professionalsAdd.changePositionType);
        },
        changePositionType: function(){
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
                .getSelectedNodes(), v = "";
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
            $("#groupId").val(v);
            cityObj.val(n);
            $("#zTreeContent").hide();
            isarea = treeNode.isarea;
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
            $("body").bind("mousedown", professionalsAdd.onBodyDown);
        },
        hideMenu: function () {
            $("#zTreeContent").fadeOut("fast");
            $("body").unbind("mousedown", professionalsAdd.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "zTreeContent" || $(event.target).parents("#zTreeContent").length > 0)) {
                professionalsAdd.hideMenu();
            }
        },
        //组织树预处理函数
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            professionalsAdd.hideErrorMsg();//清除错误提示样式
            var isAdminStr = $("#isAdmin").attr("value");    // 是否是admin
            var isAdmin = isAdminStr == 'true';
            var userGroupId = $("#userGroupId").attr("value");  // 用户所属组织 id
            var userGroupName = $("#userGroupName").attr("value");  // 用户所属组织 name
            //如果根企业下没有节点,就显示错误提示(根企业下不能创建从业人员)
            if (responseData != null && responseData != "" && responseData != undefined && responseData.length >= 1) {
                if ($("#groupId").val() == "") {
                    $("#groupId").val(responseData[0].uuid);
                    $("#zTreeCitySel").val(responseData[0].name);
                }
                isarea = responseData[0].isarea;
                return responseData;
            } else {
                professionalsAdd.showErrorMsg(userGroupNull, "zTreeCitySel");
                return;
            }
        },
        checkIdentity: function(){
            var identity = $('#identity').val(),
                positionType = $('#positionType').val();

            var err = $('#identity-err');

            if(positionType == positionTypeId && identity == ''){
                err.removeClass('hide');
                return false;
            }

            err.addClass('hide');
            return true;
        },
        identityChange: function(){
            var identity = $('#identity').val();
            var err = $('#identity-err');
            if(identity !== ''){
                err.addClass('hide');
            }
        },
        doSubmit: function () {
            var birthday = $("#birthday").val();
            var time = professionalsAdd.getNowFormatDate();
            if (isarea == "1") {
                layer.msg("不能在区域下增加从业人员");
                return false;
            }
            if (birthday != null && birthday > time) {
                $("#birthdayError").attr("style", "dispaly:block");
                return false;
            }

            professionalsAdd.validates();
            if(!professionalsAdd.checkIdentity()){
                return false;
            }

            if (professionalsAdd.validates()) {
                addHashCode1($("#addForm"));
                $("#addForm").ajaxSubmit(function (data) {
                    var json = eval("(" + data + ")");
                    if (json.success) {
                        $("#commonWin").modal("hide");
                        myTable.requestData();
                    } else {
                        layer.msg(json.msg);
                    }
                });
            }
            ;
        },
        validates: function () {
            return $("#addForm").validate({
                rules: {
                    name: {
                        required: true,
                        checkPeopleName: true,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/enterprise/professionals/repetition",
                            data: {
                                name: function () {
                                    return $("#name").val();
                                },
                                identity: function () {
                                    return $("#identity").val();
                                }
                            },
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
                        maxlength: 20
                    },
                    operationNumber: {
                        maxlength: 64
                    },
                    drivingLicenseNo: {
                        maxlength: 64
                    },
                    remindDays: {
                        digits: true,
                        min: 0,
                        max: 9999
                    },
                    // wjk
                    operationAgencies: {
                        maxlength: 128
                    },
                    icCardAgencies: {
                        maxlength: 128
                    },
                    drivingAgencies: {
                        maxlength: 128
                    },
                    landline: {
                        isLandline: true
                    },
                    drivingEndDate: {
                        compareDate: "#drivingStartDate"
                    },
                    drivingType: {
                        maxlength: 10,
                        isABCNumber: true,
                    },
                },
                messages: {
                    emergencyContactPhone: {
                        isLandline: telPhoneError
                    },
                    phone: {
                        mobilePhone: phoneError
                    },
                    phoneTwo: {
                        mobilePhone: phoneError
                    },
                    phoneThree: {
                        mobilePhone: phoneError
                    },
                    remindDays: {
                        digits: naturalNumber,
                        min: integerFour,
                        max: integerFour
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
                        isTel: phoneError,
                        // required: '请输入手机号码'
                    },
                    emergencyContact: {
                        maxlength: publicSize20Length
                    },
                    email: {
                        email: emailError,
                        maxlength: publicSize20Length
                    },
                    operationNumber: {
                        maxlength: publicSize64Length
                    },
                    drivingLicenseNo: {
                        maxlength: publicSize64Length
                    },
                    operationAgencies: {
                        maxlength: '长度不超过128'
                    },
                    icCardAgencies: {
                        maxlength: '长度不超过128'
                    },
                    drivingAgencies: {
                        maxlength: '长度不超过128'
                    },
                    drivingEndDate: {
                        compareDate: "结束时间必须大于开始时间！"
                    },
                    drivingType: {
                        maxlength: '长度不超过10',
                        isABCNumber: '只能输入字母和数字',
                    },
                }
            }).form();
        },
        //图片上传预览功能
        setImagePreview: function (avalue) {
            professionalsAdd.uploadImage(); // 上传图片到服务器
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
                    layer.msg(publicPictureError);
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
                var d = new Date();
                var random = d.getTime()
                formData.append("file", docObj.files[0]);
                formData.append("id", random);
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
                            $("#preview").src("");
                        } else {
                            $("#photograph").val(data.imgName);
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
        }
    }
    $(function () {
        professionalsAdd.init();
        professionalsAdd.type();
        $('input').inputClear();
        $("#doSubmitAdd").on("click", professionalsAdd.doSubmit);
        $("#zTreeCitySel").on("click", function () {
            professionalsAdd.showMenu(this)
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
                treeObj.checkAllNodes(false)
            }
        });
        $("#overSpeedHide").on("click", professionalsAdd.hiddenparameterFn);
        $('#identity').on('keydown', professionalsAdd.identityChange);
    })
})($, window)