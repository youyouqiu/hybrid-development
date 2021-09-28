(function ($, window) {
    var userGroupId;//用户组织ID
    var loginLogo;//登录页logo
    var videoBackground;//视频背景图
    var loginBg;//登录页背景图
    var homeLogo;//首页logo
    var webIco; //网页标题ico
    var indexTitle;//顶部标题
    var copyright;//版权信息
    var websiteName;//官网域名
    var recordNumber;//备案方案
    var resourceName;//首页默认资源名称
    var beforeLoginLogo;//修改前Logo
    var beforeVideoBackground;//修改前的视频背景图
    var beforeLoginBg;//修改前登录页背景图
    var beforeHomeLogo;//修改前Logo
    var beforeWebIco;
    var beforePlatformSite;
    var frontPage;
    var serviceExpireReminder;//服务到期提前提醒天数
    var paramData = {};//公共提交参数


    // 登录页个性化配置相关参数
    var titleArr = ['备案号阴影', '', '按钮颜色', '', '备案号字色'];
    var curImg = "";
    var colorArr = [];
    var imgCancas;
    var defaultLoginPersonalization = {// 登录页个性化配置
        inputPosition: 2,
        logoPosition: 3,
        recordNumberColor: "rgb(45,45,45)",
        recordNumberShadow: "rgb(255,255,255)",
        buttonColor: "rgb(85,101,123)"
    };
    var loginPersonalization = {// 登录页个性化配置
        inputPosition: 2,
        logoPosition: 3,
        recordNumberColor: "rgb(45,45,45)",
        recordNumberShadow: "rgb(255,255,255)",
        buttonColor: "rgb(85,101,123)"
    };
    var platformSite = '';

    personalizedConfiguration = {
        init: function () {
            $("[data-toggle='tooltip']").tooltip();

            $("label.error").hide();
            var url = "/clbs/m/intercomplatform/personalized/find";
            var data = {"uuid": userGroupId};
            json_ajax("POST", url, "json", false, data, personalizedConfiguration.initCallback);
        },
        initCallback: function (data) {
            $('#doc-ico').val('');
            if (data.success == true) {
                var list = data.obj.list;
                beforeLoginLogo = list.loginLogo;
                beforeLoginBg = list.loginBackground;
                beforeHomeLogo = list.homeLogo;
                beforeVideoBackground = list.videoBackground;
                beforeWebIco = list.webIco;
                beforePlatformSite = list.platformSite;

                var homeLogo = "/clbs/resources/img/logo/" + list.homeLogo;
                var webIco = "/clbs/resources/img/logo/" + list.webIco;
                var videoBg = "/clbs/resources/img/logo/" + beforeVideoBackground;
                var copyright = list.copyright;
                var websiteName = list.websiteName;
                var recordNumber = list.recordNumber;
                var serviceExpireReminder = list.serviceExpireReminder;
                var bottomTitleMsg = copyright + "," + websiteName + "," + recordNumber;
                var topTitleMsg = list.topTitle;
                frontPage = list.frontPage; // 默认首页id
                $("#serviceNum").html(serviceExpireReminder);
                $("#remindDate").val(serviceExpireReminder);
                $("#topTitleMsg").html(html2Escape(topTitleMsg));
                $("#indexTitle").val(topTitleMsg);
                $("#bottomTitleMsg").html(html2Escape(bottomTitleMsg));
                $("#platformSite").html(beforePlatformSite);
                $("#platformSiteVal").val(beforePlatformSite);
                $("#copyright").val(copyright);
                $("#websiteName").val(websiteName);
                $("#recordNumber").val(recordNumber);
                $("#photographBg").val(list.loginBackground);
                $("#preview-ico").attr('src', webIco);
                $("#photograph-ico").val(list.webIco);
                $("#webIcoShow").attr('src', webIco);
                $("#preview-index").attr('src', homeLogo);
                $("#homeLogo").attr('src', homeLogo);
                $("#videoBackground").attr('src', videoBg);
                $("#preview-videoBg").attr('src', videoBg);
                $("#photograph-index").val(list.homeLogo);
                $("#frontPage").attr("value", frontPage);
                /*$("#previewBg").attr('src', loginBg);
               $("#loginBg").attr('src', loginBg);
               $("#preview").attr('src', loginLogo);
               $("#photograph").val(list.loginLogo);
               $("#loginLogo").attr('src', loginLogo);*/

                // 渲染登录页个性化配置
                var loginLogo = "/clbs/resources/img/logo/" + list.loginLogo;
                var loginBg = "/clbs/resources/img/home/" + list.loginBackground;
                $('#loginLogoImg').attr('src', loginLogo);
                $('.previewBox').css('backgroundImage', 'url(' + loginBg + ')');
                $("#editLoginLogoImg").val(beforeLoginLogo);
                $("#editLoginBgImg").val(beforeLoginBg);
                $("#photograph-videoBg").val(beforeVideoBackground);
                $("#loginCopyRight").text(copyright);
                $(".loginRecord").text(recordNumber);
                if (list.loginPersonalization) {
                    loginPersonalization = JSON.parse(list.loginPersonalization);
                    $('#editLoginSetting .inputPos[value=' + loginPersonalization.inputPosition + ']').prop('checked', true);
                    $('#editLoginSetting .logoPos[value=' + loginPersonalization.logoPosition + ']').prop('checked', true);
                    personalizedConfiguration.btnAndFontColorRender();
                    personalizedConfiguration.inputPosChange();
                    personalizedConfiguration.logoPosChange();
                }

                // 树结构
                var setting = {
                    async: {
                        url: "/clbs/c/role/resourceTree",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        contentType: "application/json",
                        dataType: "json",
                        dataFilter: personalizedConfiguration.ajaxDataFilter
                    },
                    check: {
                        enable: true,
                        chkStyle: "radio",
                        radioType: "all",
                        chkboxType: {
                            "Y": "s",
                            "N": "s"
                        }
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
                        beforeClick: personalizedConfiguration.beforeClick,
                        onClick: personalizedConfiguration.onClick,
                        onCheck: personalizedConfiguration.onCheck,
                        onAsyncSuccess: personalizedConfiguration.zTreeOnAsyncSuccess,
                    }
                };
                $.fn.zTree.init($("#ztreeDemo"), setting, null);
            } else {
                layer.msg(data.msg, {move: false});
            }
        },
        //上传登录页背景图片
        uploadBgImage: function () {
            var docObj = document.getElementById("addBg");
            if (docObj.files && docObj.files[0]) {
                var formData = new FormData();
                formData.append("file", docObj.files[0]);
                $.ajax({
                    url: '/clbs/m/intercomplatform/personalized/upload_bgimg',
                    type: 'POST',
                    data: formData,
                    async: false,
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function (data) {
                        data = $.parseJSON(data);
                        if (data.imgName == "0") {
                            $("#saveLoginSetting").prop("disabled", true);
                            layer.msg("图片格式不正确！请选择格式为PNG、JPG、JPEG、SVG、GIF的文件");
                        } else {
                            $("#saveLoginSetting").removeAttr("disabled", "disabled");
                            $("#editLoginBgImg").val(data.imgName);
                        }
                    },
                    error: function (data) {
                        layer.msg("上传失败！");
                    }
                });
            }
        },
        //登录页背景图片上传预览功能
        setBgImagePreview: function (avalue) {
            personalizedConfiguration.uploadBgImage(); // 上传图片到服务器
            var docObj = document.getElementById("addBg");
            var imgObjPreview = document.getElementById("curImg");
            if (docObj.files && docObj.files[0]) {
                //火狐下，直接设img属性
                imgObjPreview.style.display = 'block';
                imgObjPreview.style.width = "100%";
                //imgObjPreview.style.height = '123px';
                //火狐7以上版本不能用上面的getAsDataURL()方式获取，需要一下方式
                if (window.navigator.userAgent.indexOf("Chrome") >= 1 || window.navigator.userAgent.indexOf("Safari") >= 1) {
                    curImg = window.webkitURL.createObjectURL(docObj.files[0]);

                } else {
                    curImg = window.URL.createObjectURL(docObj.files[0]);
                }
                imgObjPreview.src = curImg;
                $('#checkBg .imgPreview').show();
                $('.previewBox').css('backgroundImage', 'url(' + curImg + ')');
                personalizedConfiguration.colorRender(curImg);
                /*  personalizedConfiguration.getImageWidth(curImg, function (width, height) {
                      if (width > 1920 && height > 1080) {
                          $('#editLoginBgImg').val(beforeLoginBg);
                          layer.msg("图片大小不要超过1920x1080");
                      } else {

                      }
                  });*/
            }
            else {
                //IE下，使用滤镜
                docObj.select();
                var imgSrc = document.selection.createRange().text;
                var localImagId = document.getElementById("localBgImag");
                //必须设置初始大小
                localImagId.style.width = "100%";
                // localImagId.style.height = "123px";
                //图片异常的捕捉，防止用户修改后缀来伪造图片
                try {
                    localImagId.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale)";
                    localImagId.filters.item("DXImageTransform.Microsoft.AlphaImageLoader").src = imgSrc;
                }
                catch (e) {
                    alert("您上传的图片格式不正确，请重新选择!");
                    return false;
                }
                imgObjPreview.style.display = 'none';
                document.selection.empty();
            }
            return true;
        },
        //上传图片
        uploadImage: function () {
            var docObj = document.getElementById("addLogo");
            if (docObj.files && docObj.files[0]) {
                var formData = new FormData();
                formData.append("file", docObj.files[0]);
                $.ajax({
                    url: '/clbs/m/intercomplatform/personalized/upload_img',
                    type: 'POST',
                    data: formData,
                    async: false,
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function (data) {
                        data = $.parseJSON(data);
                        if (data.imgName == "0") {
                            layer.msg("图片格式不正确！请选择格式为PNG、JPG、JPEG、SVG、GIF的文件");
                            $("#saveLoginSetting").prop("disabled", true);
                        } else {
                            $("#saveLoginSetting").removeAttr("disabled", "disabled");
                            $("#editLoginLogoImg").val(data.imgName);
                        }
                    },
                    error: function (data) {
                        layer.msg("上传失败！");
                    }
                });
            }
        },
        //下面用于图片上传预览功能
        setImagePreview: function (avalue) {
            personalizedConfiguration.uploadImage(); // 上传图片到服务器
            var docObj = document.getElementById("addLogo");
            var imgObjPreview = document.getElementById("curLogoImg");
            if (docObj.files && docObj.files[0]) {
                //火狐下，直接设img属性
                // imgObjPreview.style.display = 'block';
                // imgObjPreview.style.width = "100%";
                //火狐7以上版本不能用上面的getAsDataURL()方式获取，需要一下方式
                var curSrc = '';
                if (window.navigator.userAgent.indexOf("Chrome") >= 1 || window.navigator.userAgent.indexOf("Safari") >= 1) {
                    curSrc = window.webkitURL.createObjectURL(docObj.files[0]);
                } else {
                    curSrc = window.URL.createObjectURL(docObj.files[0]);
                }
                imgObjPreview.src = curSrc;
                $('#loginLogoImg').attr('src', curSrc);
                $('#checkLogo .imgPreview').show();
                /* personalizedConfiguration.getImageWidth(curSrc, function (width, height) {
                     if (width > 689 && height > 123) {
                         layer.msg("图片大小不要超过689x123");
                         $('#editLoginLogoImg').val(beforeLoginLogo);
                     } else {

                     }
                 });*/
            }
            else {
                //IE下，使用滤镜
                docObj.select();
                var imgSrc = document.selection.createRange().text;
                var localImagId = document.getElementById("localImag");
                //必须设置初始大小
                localImagId.style.width = "100%";
                // localImagId.style.height = "123px";
                //图片异常的捕捉，防止用户修改后缀来伪造图片
                try {
                    localImagId.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale)";
                    localImagId.filters.item("DXImageTransform.Microsoft.AlphaImageLoader").src = imgSrc;
                }
                catch (e) {
                    alert("您上传的图片格式不正确，请重新选择!");
                    return false;
                }
                imgObjPreview.style.display = 'none';
                document.selection.empty();
            }
            return true;
        },
        //平台网页标题 - 上传图片
        uploadImageIco: function () {
            var docObj = document.getElementById("doc-ico");
            if (docObj.files && docObj.files[0]) {
                var formData = new FormData();
                formData.append("file", docObj.files[0]);
                $.ajax({
                    url: '/clbs/m/intercomplatform/personalized/upload_ico',
                    type: 'POST',
                    data: formData,
                    async: false,
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function (data) {
                        data = $.parseJSON(data);
                        if (data.imgName == "0") {
                            $("#webIco").attr("disabled", "disabled");
                            layer.msg("图片格式不正确！请选择ico文件");
                        } else {
                            $("#webIco").removeAttr("disabled", "disabled");
                            $("#photograph-ico").val(data.imgName);
                        }
                    },
                    error: function (data) {
                        layer.msg("上传失败！");
                    }
                });
            }
        },
        //下面用于图片上传预览功能
        setImagePreviewico: function (avalue) {
            personalizedConfiguration.uploadImageIco(); // 上传图片到服务器
            var docObj = document.getElementById("doc-ico");
            var imgObjPreview = document.getElementById("preview-ico");
            if (docObj.files && docObj.files[0]) {
                //火狐下，直接设img属性
                imgObjPreview.style.display = 'block';
                // imgObjPreview.style.width = '240px';
                imgObjPreview.style.height = '80px';
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
                var localImagId = document.getElementById("localImag-ico");
                //必须设置初始大小
                // localImagId.style.width = "240px";
                localImagId.style.height = "80px";
                //图片异常的捕捉，防止用户修改后缀来伪造图片
                try {
                    localImagId.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale)";
                    localImagId.filters.item("DXImageTransform.Microsoft.AlphaImageLoader").src = imgSrc;
                }
                catch (e) {
                    alert("您上传的图片格式不正确，请重新选择!");
                    return false;
                }
                imgObjPreview.style.display = 'none';
                document.selection.empty();
            }
            return true;
        },
        //平台首页logo - 上传图片
        uploadImageIndex: function () {
            var docObj = document.getElementById("doc-index");
            if (docObj.files && docObj.files[0]) {
                var formData = new FormData();
                formData.append("file", docObj.files[0]);
                $.ajax({
                    url: '/clbs/m/intercomplatform/personalized/upload_img',
                    type: 'POST',
                    data: formData,
                    async: false,
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function (data) {
                        data = $.parseJSON(data);
                        if (data.imgName == "0") {
                            $("#IndexLogo").attr("disabled", "disabled");
                            layer.msg("图片格式不正确！请选择png文件");
                        } else {
                            $("#IndexLogo").removeAttr("disabled", "disabled");
                            $("#photograph-index").val(data.imgName);
                        }
                    },
                    error: function (data) {
                        layer.msg("上传失败！");
                    }
                });
            }
        },
        //下面用于图片上传预览功能
        setImagePreviewIndex: function (avalue) {
            personalizedConfiguration.uploadImageIndex(); // 上传图片到服务器
            var docObj = document.getElementById("doc-index");
            var imgObjPreview = document.getElementById("preview-index");
            if (docObj.files && docObj.files[0]) {
                //火狐下，直接设img属性
                imgObjPreview.style.display = 'block';
                // imgObjPreview.style.width = '240px';
                imgObjPreview.style.height = '80px';
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
                var localImagId = document.getElementById("localImag-index");
                //必须设置初始大小
                // localImagId.style.width = "240px";
                localImagId.style.height = "80px";
                //图片异常的捕捉，防止用户修改后缀来伪造图片
                try {
                    localImagId.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale)";
                    localImagId.filters.item("DXImageTransform.Microsoft.AlphaImageLoader").src = imgSrc;
                }
                catch (e) {
                    alert("您上传的图片格式不正确，请重新选择!");
                    return false;
                }
                imgObjPreview.style.display = 'none';
                document.selection.empty();
            }
            return true;
        },
        //登录页预览
        loginPagesShow: function () {
            $('#loginPagesPreview').html($('#loginPage').html());
            $("#loginPagesShow").modal("show");
            $("#loginPagesShow").on("hidden.bs.modal", function () {
                $('#loginPagesPreview').html('');
            });
        },
        //平台首页照片查看
        indexPagesPhotoShow: function () {
            $("#indexLogoPhotoShow").modal("show");
        },
        //平台网页ico
        webIcoPhotoShow: function () {
            $("#webIcoPhotoShow").modal("show");
        },
        topTitle: function () {
            personalizedConfiguration.updateAll();
            var url = "/clbs/m/intercomplatform/personalized/update";
            var data = paramData;
            json_ajax("POST", url, "json", false, data, personalizedConfiguration.topTitleCallback);
        },
        topTitleCallback: function (data) {
            if (data.success == true) {
                $("#editIndexTitle").modal('hide');
                personalizedConfiguration.init();
            } else {
                layer.msg(data.msg, {move: false});
            }
        },
        // 修改提前提醒天数
        saveServiceReminder: function () {
            if (!personalizedConfiguration.serviceNumValidate()) return;
            personalizedConfiguration.updateAll();
            var url = "/clbs/m/intercomplatform/personalized/update";
            var data = paramData;
            json_ajax("POST", url, "json", false, data, personalizedConfiguration.serviceReminderCallback);
        },
        //开始聚合对象数量输入验证
        serviceNumValidate: function () {
            return $("#servicesForm").validate({
                rules: {
                    remindDate: {
                        required: true,
                        digits: true,
                        range: [30, 999]
                    }
                },
                messages: {
                    remindDate: {
                        required: remindDateNull,
                        digits: remindDateError,
                        range: remindDateError
                    }
                }
            }).form();
        },
        serviceReminderCallback: function (data) {
            $('#editServiceDate').modal('hide');
            personalizedConfiguration.init();
        },
        BottomTitle: function () {
            personalizedConfiguration.updateAll();
            var url = "/clbs/m/intercomplatform/personalized/update";
            var data = paramData;
            json_ajax("POST", url, "json", false, data, personalizedConfiguration.BottomTitleCallback);
        },
        BottomTitleCallback: function (data) {
            $('#editBottomTitle').modal('hide');
            personalizedConfiguration.init();
        },
        frontPage: function () {
            personalizedConfiguration.updateAll();
            var url = "/clbs/m/intercomplatform/personalized/update";
            var data = paramData;
            json_ajax("POST", url, "json", false, data, personalizedConfiguration.frontPageCallback);
        },
        frontPageCallback: function (data) {
            $('#editResourceName').modal('hide');
            personalizedConfiguration.init();
        },
        IndexLogo: function () {
            personalizedConfiguration.updateAll();
            if (homeLogo != beforeHomeLogo) {
                var imgSrc = $("#preview-index").attr("src");
                var url = "/clbs/m/intercomplatform/personalized/update";
                var data = paramData;
                personalizedConfiguration.getImageWidth(imgSrc, function (width, height) {
                    if (width > 240 && height > 80) {
                        layer.msg("图片大小不要超过240x80");
                    } else {
                        json_ajax("POST", url, "json", false, data, personalizedConfiguration.IndexLogoCallback);
                    }
                });
            } else {
                $("#editIndexLogo").modal("hide");
            }
        },
        IndexLogoCallback: function (data) {
            $('#editIndexLogo').modal('hide');
            personalizedConfiguration.init();
        },
        webIco: function () {
            personalizedConfiguration.updateAll();
            if (webIco != beforeWebIco) {
                var imgSrc = $("#preview-ico").attr("src");
                var url = "/clbs/m/intercomplatform/personalized/update";
                var data = paramData;
                json_ajax("POST", url, "json", false, data, personalizedConfiguration.webIcoCallback);
            } else {
                $("#editWebIco").modal("hide");
            }
        },
        webIcoCallback: function (data) {
            $('#editWebIco').modal('hide');
            personalizedConfiguration.init();
        },
        getImageWidth: function (url, callback) {
            var img = new Image();
            img.src = url;
            // 如果图片被缓存，则直接返回缓存数据
            if (img.complete) {
                callback(img.width, img.height);
            } else {
                // 完全加载完毕的事件
                img.onload = function () {
                    callback(img.width, img.height);
                }
            }
        },
        updateAll: function () {
            loginLogo = $("#editLoginLogoImg").val();
            loginBg = $("#editLoginBgImg").val();
            homeLogo = $("#photograph-index").val();
            webIco = $("#photograph-ico").val();
            indexTitle = $("#indexTitle").val();
            copyright = $("#copyright").val();
            websiteName = $("#websiteName").val();
            recordNumber = $("#recordNumber").val();
            resourceName = $("#resourceName").val();
            frontPage = $("#frontPage").val();
            platformSite = $("#platformSiteVal").val();
            serviceExpireReminder = $("#remindDate").val();
            videoBackground = $("#photograph-videoBg").val();

            paramData = {
                "topTitle": indexTitle,
                "loginLogo": loginLogo,
                "loginBackground": loginBg,
                "homeLogo": homeLogo,
                "webIco": webIco,
                "copyright": copyright,
                "websiteName": websiteName,
                "recordNumber": recordNumber,
                "groupId": userGroupId,
                "name": beforeLoginLogo,
                "beforeLoginBg": beforeLoginBg,
                "frontPage": frontPage,
                "serviceExpireReminder": serviceExpireReminder,
                "platformSite": platformSite,
                "loginPersonalization": JSON.stringify(loginPersonalization),
                "videoBackground": videoBackground,
            }
        },
        defaultLoginSetting: function () {
            personalizedConfiguration.popupWindow(0);
        },
        defaultPlatformSite: function () {
            personalizedConfiguration.popupWindow(7);
        },
        defaultIndexLogo: function () {
            personalizedConfiguration.popupWindow(1);
        },
        defaultIndexTitle: function () {
            personalizedConfiguration.popupWindow(2);
        },
        defaultBottomTitle: function () {
            personalizedConfiguration.popupWindow(3);
        },
        defaultResourceName: function () {
            personalizedConfiguration.popupWindow(4);
        },
        defaultWebIco: function () {
            personalizedConfiguration.popupWindow(5);
        },
        defaultServices: function () {
            personalizedConfiguration.popupWindow(6);
        },
        popupWindow: function (type) {
            var url = "/clbs/m/intercomplatform/personalized/default";
            var data = {
                "topTitle": indexTitle,
                "loginLogo": loginLogo,
                "loginBackground": loginBg,
                "homeLogo": homeLogo,
                "webIco": webIco,
                "copyright": copyright,
                "websiteName": websiteName,
                "recordNumber": recordNumber,
                "groupId": userGroupId,
                "type": type,
                "frontPage": frontPage,
                "serviceExpireReminder": serviceExpireReminder,
                "loginPersonalization": JSON.stringify(loginPersonalization),
                "platformSite": platformSite,
                "videoBackground": videoBackground
            };
            layer.confirm('是否恢复默认信息', {
                title: '操作确认',
                icon: 3, // 问号图标
                btn: ['确定', '取消'] //按钮
            }, function () {
                json_ajax("POST", url, "json", false, data, null);
                layer.closeAll('dialog');
                personalizedConfiguration.init();
            });
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            if (responseData) {
                for (var i = 0; i < responseData.length; i++) {
                    responseData[i].open = true;
                }
                var obj = {
                    id: "isHome",
                    name: "首页",
                    pId: "0",
                    type: 1,
                    nocheck: true
                };
                responseData.unshift(obj);
            }
            return responseData;
        },
        beforeClick: function (treeId, treeNode) {
            if (treeNode.type == '0' || treeNode.name == '首页') return true;
            return false;
            /* var check = (treeNode);
             return check;*/
        },
        // 分组下拉点击事件
        onClick: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("ztreeDemo"),
                nodes = zTree.getSelectedNodes();
            if (treeNode.type == 0) { // 按钮菜单
                zTree.checkNode(nodes[0], true, false, true);
            } else if (treeNode.id == 'isHome') {
                zTree.selectNode(treeNode, false, true);
                $("#frontPage").attr("value", treeNode.id);
                frontPage = treeNode.id;
                var cityObj = $("#resourceName");
                cityObj.val(treeNode.name);
                var nodes = zTree.getCheckedNodes(true);
                if (nodes[0]) {
                    zTree.checkNode(nodes[0], false);
                }
                $("#menuContent").hide();
            }
        },
        onCheck: function (e, treeId, treeNode) {
            var type = treeNode.type;
            var zTree = $.fn.zTree.getZTreeObj("ztreeDemo"), nodes = zTree
                .getCheckedNodes(true), v = "";
            if (type == 0) {
                zTree.selectNode(treeNode, false, true);
                $("#frontPage").attr("value", nodes[0].id);
                frontPage = nodes[0].id;
                v = nodes[0].name;
                var cityObj = $("#resourceName");
                cityObj.val(v);
                $("#menuContent").hide();
            }
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var defautPage = $("#frontPage").val();
            if (defautPage != undefined && defautPage != null && defautPage != "") {
                var treeObj = $.fn.zTree.getZTreeObj("ztreeDemo");
                var allNode = treeObj.transformToArray(treeObj.getNodes());
                if (allNode != null && allNode.length > 0) {
                    for (var i = 0, len = allNode.length; i < len; i++) {
                        if (allNode[i].id == frontPage && (allNode[i].type == 0 || allNode[i].name == '首页')) {
                            treeObj.checkNode(allNode[i], true, false, true);
                            treeObj.selectNode(allNode[i], false, true);
                            $("#resourceName").val(allNode[i].name);
                            $("#frontPageMsg").html(allNode[i].name);
                        }
                    }
                }
            } else {
                $("#frontPageMsg").html('');
            }
        },
        // 获取当前点击资源详情
        getResourceDetail: function (id) {
            $.ajax({
                type: 'POST',
                url: '/clbs/c/role/getResourceById',
                data: {"id": id},
                dataType: 'json',
                success: function (data) {
                    $("#resourceName").val(data.obj.resourceInfo.resourceName);
                },
                error: function () {
                    layer.msg(systemError, {
                        time: 1500,
                    });
                }
            });
        },
        //将null转成空字符串
        converterNullToBlank: function (nullValue) {
            if (nullValue == null || nullValue == undefined || nullValue == "null" || nullValue == "") {
                return "-";
            }
            return nullValue;
        },
        // 清除错误信息
        clearErrorMsg: function () {
            $("label.error").hide();
        },
        // 分组下拉框
        showMenu: function (e) {
            $("#ztreeDemo").show();
            var v_id = e.id;
            rid = v_id;
            if ($("#menuContent").is(":hidden")) {
                var width = $(e).parent().width();
                $("#menuContent").css("width", width + "px");
                $(window).resize(function () {
                    var width = $(e).parent().width();
                    $("#menuContent").css("width", width + "px");
                })
                $("#menuContent").insertAfter($("#" + rid));
                $("#menuContent").show();
            } else {
                $("#menuContent").is(":hidden");
            }
            $("body").bind("mousedown", personalizedConfiguration.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "menuContent" || $(event.target).parents("#menuContent").length > 0)) {
                personalizedConfiguration.hideMenu();
            }
        },
        hideMenu: function () {
            $("#menuContent").fadeOut("fast");
            $("body").unbind("mousedown", personalizedConfiguration.onBodyDown);
        },
        setDefLoginSetting: function () {
            personalizedConfiguration.setDefWindow(0);
        },
        setDefPlatformSite: function () {
            personalizedConfiguration.setDefWindow(7);
        },
        setDefIndexLogo: function () {
            personalizedConfiguration.setDefWindow(1);
        },
        setDefIndexTitle: function () {
            personalizedConfiguration.setDefWindow(2);
        },
        setDefBottomTitle: function () {
            personalizedConfiguration.setDefWindow(3);
        },
        setDefResourceName: function () {
            personalizedConfiguration.setDefWindow(4);
        },
        setDefWebIco: function () {
            personalizedConfiguration.setDefWindow(5);
        },
        setDefServices: function () {
            personalizedConfiguration.setDefWindow(6);
        },
        setDefWindow: function (type) {
            personalizedConfiguration.updateAll();
            var url = "/clbs/m/intercomplatform/personalized/makeDefault";
            var param = paramData;
            param.type = type;
            layer.confirm('是否设置为默认', {
                title: '操作确认',
                icon: 3, // 问号图标
                btn: ['确定', '取消'] //按钮
            }, function () {
                json_ajax("POST", url, "json", false, param, function (data) {
                    layer.closeAll('dialog');
                    if (data.success) {
                        personalizedConfiguration.init();
                    } else if (data.msg) {
                        layer.msg(data.msg);
                    }
                });
            });
        },

        /**
         * 登录页个性化配置相关方法
         * */
        // 图片颜色拾取方法
        colorRender: function (t) {
            var e = new Image;
            e.src = t;
            e.onload = function () {
                var t = e.naturalWidth, n = e.naturalHeight;
                n = $('#curImg').height(), t = $('#curImg').width(), F = n, R = t;
                var o = document.createElement("canvas");
                o.width = t, o.height = n, U = o.getContext("2d"), U.drawImage(e, 0, 0, t, n);
                imgCancas = U;
                // 过滤，条件：透明度大于50%，不能太白
                for (var r, i, a, s, c = U.getImageData(0, 0, t, n).data, l = t * n, u = [], f = [], d = [], p = 1, h = 0; h < l; h += p)
                    if (r = 4 * h,
                        i = c[r + 0],
                        a = c[r + 1],
                        s = c[r + 2],
                    c[r + 3] >= 125 && !(i > 250 && a > 250 && s > 250)) {
                        var m = [i, a, s];
                        var b = new colz.Color(m)
                            , g = {
                            rgb_color: [i, a, s],
                            hsl_color: b.hsl,
                            offset: r,
                            hsl_color_h: b.hsl.h,
                            hex: "ff" + b.hex.slice(1, 7)
                        };
                        u.push(g);
                        var v = b.hsl.s;
                        -1 == f.indexOf(v) && f.push(v)
                    }
                // u 为过滤后的像素点，是个数据结构，包含rgb，hsl
                // f 为饱和度通道，并且去重了
                f.sort(function (t, e) {
                    return t - e
                });
                // 去除了饱和度低于10%和大于90%的数据点，保存在S中
                var y = parseInt(.1 * f.length)
                    , w = parseInt(.9 * f.length)
                    , E = f[y]
                    , _ = f[w]
                    , S = [];
                u.forEach(function (t) {
                    t.hsl_color.s >= E && t.hsl_color.s <= _ && S.push(t)
                }),
                    S.forEach(function (t) {
                        var e = t.hsl_color.l;
                        -1 == d.indexOf(e) && d.push(e)
                    }),
                    d.sort(function (t, e) {
                        return t - e
                    });
                // 去除了亮度度低于10%和大于90%的数据点，将剩下的点 按 10%,20%,30%,40%,80%,90% 分割到了5个数组 z P T N L
                var j = d[Math.floor(.1 * d.length)]
                    , C = d[Math.floor(.2 * d.length)]
                    , O = d[Math.floor(.3 * d.length)]
                    , M = d[Math.floor(.4 * d.length)]
                    , I = d[Math.floor(.8 * d.length)]
                    , A = d[Math.floor(.9 * d.length)]
                    , z = []
                    , P = []
                    , T = []
                    , N = []
                    , L = [];
                S.forEach(function (t) {
                    var e = t.hsl_color.l;
                    e >= j && e <= C ? z.push(t) : e >= C && e <= O ? P.push(t) : e >= O && e <= M ? T.push(t) : e >= M && e <= I ? N.push(t) : e >= I && e <= A && L.push(t)
                }),
                    z.sort(function (t, e) {
                        return t.hsl_color.l - e.hsl_color.l
                    }),
                    P.sort(function (t, e) {
                        return t.hsl_color.l - e.hsl_color.l
                    }),
                    T.sort(function (t, e) {
                        return t.hsl_color.l - e.hsl_color.l
                    }),
                    N.sort(function (t, e) {
                        return t.hsl_color.s + t.hsl_color.l - (e.hsl_color.s + e.hsl_color.l)
                    }),
                    L.sort(function (t, e) {
                        return t.hsl_color.s + t.hsl_color.l - (e.hsl_color.s + e.hsl_color.l)
                    });
                // D 是个数组，包含五个点，每个都是之前亮度过滤后的数据段的代表点，点里包含颜色和offset，offset是点的索引，点是按行串联的，offset除以宽度就是行索引，余数就是列索引
                var D = [z[parseInt(.1 * z.length)], P[parseInt(.2 * P.length)], T[parseInt(.3 * T.length)], N[parseInt(.9 * N.length)], L[parseInt(.9 * L.length)]];
                colorArr = D;
                personalizedConfiguration.roundRender();
            }
        },
        // 上传图片后,绘制图片上的小圆形
        roundRender: function () {
            var t = $('#curImg').width();
            $('#roundBox').html('');
            var roundHtml = '';
            if (colorArr[1] === undefined) {// 当上传图片为纯色时
                loginPersonalization = JSON.parse(JSON.stringify(defaultLoginPersonalization));
                for (var i = 0; i < 5; i++) {
                    var color = 'rgb(' + colorArr[0].rgb_color.join(',') + ')';
                    $('.colorItem').eq(i).css('backgroundColor', color);
                    var top = Math.ceil(colorArr[0].offset / t / 4 - 9) + 'px';
                    var left = colorArr[0].offset / 4 % t - 9 + 'px';
                    if (titleArr[i] != '') {
                        roundHtml += '<div class="roundItem" style="top: ' + top + ';left: ' + left + ';background-color: ' + color + '"><div class="roundTitle">' + titleArr[i] + '</div></div>';
                    }
                }
            } else {
                for (var i = 0; i < colorArr.length; i++) {
                    var color = 'rgb(' + colorArr[i].rgb_color.join(',') + ')';
                    $('.colorItem').eq(i).css('backgroundColor', color);
                    var top = Math.ceil(colorArr[i].offset / t / 4 - 9) + 'px';
                    var left = colorArr[i].offset / 4 % t - 9 + 'px';
                    if (titleArr[i] != '') {
                        roundHtml += '<div class="roundItem" style="top: ' + top + ';left: ' + left + ';background-color: ' + color + '"><div class="roundTitle">' + titleArr[i] + '</div></div>';
                    }
                    if (titleArr[i] == '按钮颜色') {
                        loginPersonalization.buttonColor = color;
                    }
                    if (titleArr[i] == '备案号字色') {
                        loginPersonalization.recordNumberColor = color;
                    }
                    if (titleArr[i] == '备案号阴影') {
                        loginPersonalization.recordNumberShadow = color;
                    }
                }
            }
            $('#roundBox').html(roundHtml);
            personalizedConfiguration.btnAndFontColorRender();
        },
        // 渲染按钮及备案号颜色及阴影
        btnAndFontColorRender: function () {
            $('#login_ok').css("backgroundColor", loginPersonalization.buttonColor);
            $('#personalized').css("color", loginPersonalization.recordNumberColor);
            $('.loginRecord').css("color", loginPersonalization.recordNumberColor);
            var color = loginPersonalization.recordNumberShadow;
            var shadowSolor = "-1px 0 color, 0 1px color, 1px 0 color, 0 -1px color";
            var newColor = shadowSolor.replace(/color/g, color);
            $('#personalized').css("textShadow", newColor);
        },
        getScope: function (value, max) {
            if (value < -9) return -9;
            if (value > max) return max;
            return value;
        },
        // 图片上的小圆拖动选取颜色
        roundItemDrag: function (e) {
            var ele = $(this);
            var index = $(this).index('.roundItem');
            var curImg = $('#curImg');
            var maxLeft = curImg.width() - 10;
            var maxTop = curImg.height() - 10;
            var disX = e.clientX - ele.position().left;
            var disY = e.clientY - ele.position().top;
            var left, top;
            $(document).on("mousemove", function (e) {
                left = personalizedConfiguration.getScope(e.clientX - disX, maxLeft);
                top = personalizedConfiguration.getScope(e.clientY - disY, maxTop);
                ele.css("left", left);
                ele.css("top", top);

                var spanHtml = '';
                var spanColorArr = imgCancas.getImageData(left + 4, top + 4, 11, 11).data;
                for (var i = 0; i < spanColorArr.length; i++) {
                    if (i % 4 == 0) {
                        var color = 'rgba(' + spanColorArr[i] + ',' + spanColorArr[i + 1] + ',' + spanColorArr[i + 2] + ',' + spanColorArr[i + 3] + ')';
                        if (i == 240) {
                            spanHtml += '<span class="centerSpan" style="background-color: ' + color + '"></span>';

                        } else {
                            spanHtml += '<span style="background-color: ' + color + '"></span>';
                        }
                    }
                }
                $('#colorMarker').css({"left": left + 10, "top": top - 72}).html(spanHtml).show();
            });

            $(document).on("mouseup", function () {
                $('#colorMarker').html('').hide();
                var centerTop = Math.floor(top + 9);
                var centerLeft = Math.floor(left + 9);
                // var offset = ((centerTop * curImg.width()) + centerLeft) * 4;

                var spanColorArr = imgCancas.getImageData(centerLeft, centerTop, 1, 1).data;// 获取当前像素点的颜色值
                var color = 'rgba(' + spanColorArr[0] + ',' + spanColorArr[1] + ',' + spanColorArr[2] + ',' + spanColorArr[3] + ')';
                $('.roundItem').eq(index).css('backgroundColor', color);
                if (index == 1) {// 按钮颜色
                    loginPersonalization.buttonColor = color;
                    $('#login_ok').css("backgroundColor", color);
                }
                if (index == 2) {// 备案号字体颜色
                    loginPersonalization.recordNumberColor = color;
                    $('#personalized').css("color", color);
                    $('.loginRecord').css("color", color);
                }
                if (index == 0) {// 备案号阴影颜色
                    loginPersonalization.recordNumberShadow = color;
                    var shadowSolor = "-1px 0 color, 0 1px color, 1px 0 color, 0 -1px color";
                    var newColor = shadowSolor.replace(/color/g, color);
                    $('#personalized').css("textShadow", newColor);
                }
                $("#saveLoginSetting").removeAttr("disabled", "disabled");
                $(document).off('mousemove mouseup');
            })
        },
        // 输入框位置切换
        inputPosChange: function () {
            var curVal = $('.inputPos:checked').val();
            var loginWrapper = $('#login-wrapper');
            var logoPos = $('.logoPos:checked').val();
            var loginLogo = $('.login_logo');
            var textAlign = '';
            switch (curVal) {
                case '1':
                    textAlign = 'left';
                    break;
                case '2':
                    textAlign = 'center';
                    break;
                case '3':
                    textAlign = 'right';
                    break;
            }
            loginPersonalization.inputPosition = curVal;
            loginWrapper.css('textAlign', textAlign);
            if (logoPos == '3') {
                loginLogo.attr('class', 'login_logo').css('textAlign', textAlign);
            }
        },
        // logo位置切换
        logoPosChange: function () {
            var curVal = $('.logoPos:checked').val();
            var inputPos = $('.inputPos:checked').val();
            var loginLogo = $('.login_logo');
            loginPersonalization.logoPosition = curVal;
            switch (curVal) {
                case '1':
                    loginLogo.attr('class', 'login_logo logoLeft').css('textAlign', 'left');
                    break;
                case '2':
                    loginLogo.attr('class', 'login_logo logoRight').css('textAlign', 'right');
                    break;
                case '3':
                    if (inputPos == '1') {
                        loginLogo.attr('class', 'login_logo').css('textAlign', 'left').css('textAlign', 'left');
                    }
                    if (inputPos == '2') {
                        loginLogo.attr('class', 'login_logo').css('textAlign', 'center');
                    }
                    if (inputPos == '3') {
                        loginLogo.attr('class', 'login_logo').css('textAlign', 'right');
                    }
                    break;
            }
        },
        // 修改登录页个性化配置
        saveLoginSetting: function () {
            personalizedConfiguration.updateAll();
            var url = "/clbs/m/intercomplatform/personalized/update";
            var data = paramData;
            json_ajax("POST", url, "json", false, data, personalizedConfiguration.updateLoginSettingCallback);
        },
        updateLoginSettingCallback: function (data) {
            $('#editLoginSetting').modal('hide');
            personalizedConfiguration.init();
        },
        savePlatformSite: function () {
            personalizedConfiguration.updateAll();
            var url = "/clbs/m/intercomplatform/personalized/update";
            var data = paramData;
            if ($('#platformSiteVal').val() == '') {
                layer.msg('域名不可为空');
                return;
            }
            json_ajax("POST", url, "json", false, data, personalizedConfiguration.platformSiteCallback);
        },
        platformSiteCallback: function (data) {
            if (data.success == true) {
                $('#editPlatformSite').modal('hide');
                personalizedConfiguration.init();
            } else if (data.msg) {
                layer.msg(data.msg, {move: false});
            }
        },
        /**
         * 视频窗口水印设置
         * */
        // 上传图片
        uploadImageVideoBg: function () {
            var docObj = document.getElementById("videoBgFile");
            if (docObj.files && docObj.files[0]) {
                var formData = new FormData();
                formData.append("file", docObj.files[0]);
                $.ajax({
                    url: '/clbs/m/intercomplatform/personalized/upload_img',
                    type: 'POST',
                    data: formData,
                    async: false,
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function (data) {
                        data = $.parseJSON(data);
                        if (data.imgName == "0") {
                            $("#saveVideoBg").attr("disabled", "disabled");
                            layer.msg("图片格式不正确！请选择png文件");
                        } else {
                            $("#saveVideoBg").removeAttr("disabled", "disabled");
                            $("#photograph-videoBg").val(data.imgName);
                        }
                    },
                    error: function (data) {
                        layer.msg("上传失败！");
                    }
                });
            }
        },
        // 图片预览功能
        setImagePreviewVideoBg: function (avalue) {
            personalizedConfiguration.uploadImageVideoBg(); // 上传图片到服务器
            var docObj = document.getElementById("videoBgFile");
            var imgObjPreview = document.getElementById("preview-videoBg");
            if (docObj.files && docObj.files[0]) {
                //火狐下，直接设img属性
                imgObjPreview.style.display = 'block';
                // imgObjPreview.style.width = '240px';
                imgObjPreview.style.height = '80px';
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
                var localImagId = document.getElementById("videoBgBox");
                //必须设置初始大小
                // localImagId.style.width = "240px";
                localImagId.style.height = "80px";
                //图片异常的捕捉，防止用户修改后缀来伪造图片
                try {
                    localImagId.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale)";
                    localImagId.filters.item("DXImageTransform.Microsoft.AlphaImageLoader").src = imgSrc;
                }
                catch (e) {
                    alert("您上传的图片格式不正确，请重新选择!");
                    return false;
                }
                imgObjPreview.style.display = 'none';
                document.selection.empty();
            }
            return true;
        },
        // 视频窗口水印预览
        videoBgShow: function () {
            $("#videoBgPhotoShow").modal("show");
        },
        // 修改视频窗口水印
        saveVideoBg: function () {
            personalizedConfiguration.updateAll();
            if (videoBackground != beforeVideoBackground) {
                var imgSrc = $("#preview-videoBg").attr("src");
                var url = "/clbs/m/intercomplatform/personalized/update";
                var data = paramData;
                personalizedConfiguration.getImageWidth(imgSrc, function (width, height) {
                    /* if (width > 240 && height > 80) {
                         layer.msg("图片大小不要超过240x80");
                     } else {*/
                    json_ajax("POST", url, "json", false, data, function (result) {
                        if (result.success) {
                            $('#editVideoBg').modal('hide');
                            personalizedConfiguration.init();
                        } else if (result.msg) {
                            layer.msg(result.msg);
                        }
                    });
                    // }
                });
            } else {
                $("#editVideoBg").modal("hide");
            }
        },
    };
    $(function () {
        userGroupId = $("#userGroupId").val();
        $('input').inputClear();
        personalizedConfiguration.init();
        $("#topTitle").on("click", personalizedConfiguration.topTitle);
        $("#BottomTitle").on("click", personalizedConfiguration.BottomTitle);
        $("#resourceName").on("click", function () {
            personalizedConfiguration.showMenu(this)
        });
        $("#resourceName").siblings('span').on("click", function () {
            $("#resourceName").click();
        });

        $("#saveServiceDate").on('click', personalizedConfiguration.saveServiceReminder);

        $("#frontPageSubmit").on("click", personalizedConfiguration.frontPage);
        $("#IndexLogo").on("click", personalizedConfiguration.IndexLogo);
        $("#webIco").on("click", personalizedConfiguration.webIco);
        // 修改视频窗口水印
        $("#saveVideoBg").on("click", personalizedConfiguration.saveVideoBg);
        $("#updateLoginLogo").on("click", personalizedConfiguration.updateLoginLogo);
        $("#defaultLoginSetting").on("click", personalizedConfiguration.defaultLoginSetting);
        $("#defaultPlatformSite").on("click", personalizedConfiguration.defaultPlatformSite);
        $("#defaultIndexLogo").on("click", personalizedConfiguration.defaultIndexLogo);
        $("#defaultWebIco").on("click", personalizedConfiguration.defaultWebIco);
        $("#defaultIndexTitle").on("click", personalizedConfiguration.defaultIndexTitle);
        $("#defaultBottomTitle").on("click", personalizedConfiguration.defaultBottomTitle);
        $("#defaultResourceName").on("click", personalizedConfiguration.defaultResourceName);
        $("#defaultServiceDate").on("click", personalizedConfiguration.defaultServices);
        $("#defaultVideoBg").on("click", function () {
            personalizedConfiguration.popupWindow(8);
        });

        $("#setDefLoginSetting").on("click", personalizedConfiguration.setDefLoginSetting);
        $("#setDefPlatformSite").on("click", personalizedConfiguration.setDefPlatformSite);
        $("#setDefIndexLogo").on("click", personalizedConfiguration.setDefIndexLogo);
        $("#setDefWebIco").on("click", personalizedConfiguration.setDefWebIco);
        $("#setDefIndexTitle").on("click", personalizedConfiguration.setDefIndexTitle);
        $("#setDefBottomTitle").on("click", personalizedConfiguration.setDefBottomTitle);
        $("#setDefResourceName").on("click", personalizedConfiguration.setDefResourceName);
        $("#setDefServiceDate").on("click", personalizedConfiguration.setDefServices);
        $("#setDefVideoBg").on("click", function () {
            personalizedConfiguration.setDefWindow(8);
        });

        /**
         * 登录页个性化配置
         * */
        $("#roundBox").on("mousedown", ".roundItem", personalizedConfiguration.roundItemDrag);
        $('.selectArea').on('click', 'i.editButton', function () {
            var curInput = $(this).closest('.imgPreview').prev('.inpFilePhoto').find('input');
            curInput.click();
        })
        $('.inputPos').on('change', function () {
            personalizedConfiguration.inputPosChange();
            $("#saveLoginSetting").removeAttr("disabled", "disabled");
        });
        $('.logoPos').on('change', function () {
            personalizedConfiguration.logoPosChange();
            $("#saveLoginSetting").removeAttr("disabled", "disabled");
        });
        $("#editLoginSetting").on("hidden.bs.modal", function () {
            $('.imgPreview').hide();
            $('#addBg,#addLogo').val('');
            personalizedConfiguration.init();
        });
        $("#saveLoginSetting").on("click", personalizedConfiguration.saveLoginSetting);
        $("#savePlatformSite").on("click", personalizedConfiguration.savePlatformSite);

//		INSERT INTO `zw_c_logo_config` (`id`, `login_logo`, `home_logo`, `top_title`, `copyright`, `website_name`, `record_number`, `group_id`, `web_ico`, `front_page`, `flag`, `create_data_time`, `create_data_username`, `update_data_time`, `update_data_username`) VALUES('2','loginLogo.png','indexLogo.png','中位F3物联网监控平台','©2015-2018 中位（北京）科技有限公司','www.zwlbs.com','京ICP备15041746号-1','defult','favicon.ico',NULL,'1','2017-06-05 09:57:22','yangyi1','2017-06-05 16:23:37','lyman');

    })
}($, window))