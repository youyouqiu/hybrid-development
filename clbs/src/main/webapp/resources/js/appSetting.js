(function ($, window) {
    var loginLogo;//登录页logo
    var beforeLoginLogo;//修改前Logo
    var infoList = {};
    var groupObj = {};
    var statisticsOrgInfo = {};
    var curId = '';
    //对象详情
    var detailTab = 0;

    appSetting = {
        init: function () {
            var url = "/clbs/m/app/personalized/list";
            json_ajax("POST", url, "json", false, null, appSetting.initCallback);

            //获取用户组织头像
            // appSetting.getUserGroupImg();
            //获取当前组织
            appSetting.getCurOrganization();
            //获取参考组织信息
            appSetting.getOrganizationInfo();
            //获取综合统计参考组织信息
            appSetting.getStatisticsOrgInfo();
            //获取对象详情参考组织信息
            appSetting.getDetailOrganization();
        },
        //对表格内容赋值
        initCallback: function (data) {
            layer.closeAll('loading');
            if (data.statusCode == '200') {
                infoList = data.obj;
                appSetting.setInfo(infoList);
            } else {
                if (data.msg) {
                    layer.msg(data.msg, {move: false});
                }
            }
        },
        setInfo: function (list) {
            beforeLoginLogo = list.login.logo;
            var loginLogo = "/clbs/resources/img/app/" + list.login.logo;
            var topTitleMsg = list.login.title;
            var bottomTitleMsg = list.login.url;
            var frontPageMsg = list.login.about;
            var forgetMsg = list.login.forgetPwd;
            var aggregationNum = list.app.aggrNum;
            var historyTimeRange = list.app.queryHistoryPeriod;
            var alarmTimeRange = list.app.queryAlarmPeriod;
            var selectObjNum = list.app.maxStatObjNum;
            var aboutMe = list.personal.aboutUs;

            $("#userGroupPhoto").attr('src', "/clbs/resources/img/app/" + list.personal.groupAvatar);
            $("#userImgPreview").attr('src', "/clbs/resources/img/app/" + list.personal.groupAvatar);

            $("#preview").attr('src', loginLogo);
            $("#loginLogo").attr('src', loginLogo);
            $("#topTitleMsg").html(topTitleMsg);
            $("#indexTitle").val(topTitleMsg);
            $("#bottomTitleMsg").html(bottomTitleMsg);
            $("#platformSite").val(bottomTitleMsg);
            $("#frontPageMsg").html(frontPageMsg);
            $("#loginMsg").val(frontPageMsg);
            $("#forgetMsg").html(forgetMsg);
            $("#forgetVal").val(forgetMsg);
            $("#aggregationNum").html(aggregationNum);
            $("#setAggregationNum").val(aggregationNum);
            $("#historyTimeRange").html(historyTimeRange);
            $("#setHistoryTimeRange").val(historyTimeRange);
            $("#alarmTimeRange").html(alarmTimeRange);
            $("#setAlarmTimeRange").val(alarmTimeRange);
            $("#selectObjNum").html(selectObjNum);
            $("#setSelectObjNum").val(selectObjNum);
            $("#aboutMe").html(aboutMe);
            $("#setAboutMe").val(aboutMe);

            if (list.adas == '') {
                $("#activeSafety").html('');
            } else {
                $("#activeSafety").html(list.adas == 1 ? '开启' : '关闭');
            }
            // $(".adasFlag").val(list.adas);
            $("input[name='adasFlag'][value='" + list.adas + "']").prop("checked", "checked");
        },
        //获取用户组织头像
        getUserGroupImg: function () {
            var url = '/clbs/m/app/personalized/groupAvatar';
            json_ajax("GET", url, "json", false, null, function (data) {
                if (data.statusCode == '200') {
                    var imgUrl = data.obj.url;
                    $("#userGroupPhoto").attr('src', imgUrl);
                    $("#userImgPreview").attr('src', imgUrl);
                } else {
                    if (data.msg) {
                        layer.msg(data.msg, {move: false});
                    }
                }
            })
        },

        //登录页logo上传图片
        uploadImage: function () {
            var docObj = document.getElementById("doc");
            var imgUrl = $("#preview").attr('src');
            if (docObj.files && docObj.files[0]) {
                var formData = new FormData();
                formData.append("imageFile", docObj.files[0]);
                $.ajax({
                    url: '/clbs/m/app/personalized/logo',
                    type: 'POST',
                    data: formData,
                    async: false,
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function (data) {
                        data = $.parseJSON(data);
                        if (data.statusCode == 200) {
                            $("#updateLoginLogo").removeAttr("disabled", "disabled");
                            $("#photograph").val(data.obj.image);
                            appSetting.init();
                            $("#editLoginLogo").modal('hide');
                        } else {
                            if (data.exceptionDetailMsg) {
                                layer.msg(data.exceptionDetailMsg);
                            }
                        }
                    },
                    error: function (data) {
                        layer.msg("上传失败！");
                    }
                });
            } else if (imgUrl && imgUrl != '') {
                appSetting.init();
                $("#editLoginLogo").modal('hide');
            }
        },
        //登录页logo图片上传预览功能
        setImagePreview: function (avalue) {
            var docObj = document.getElementById("doc");
            var imgObjPreview = document.getElementById("preview");
            if (docObj.files && docObj.files[0]) {
                //火狐下，直接设img属性
                //imgObjPreview.style.height = '123px';
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
        //登录页照片查看
        logoPagesPhotoShow: function () {
            $("#logoPhotoShow").modal("show");
        },
        //用户头像 - 上传图片
        uploadImageIco: function () {
            var docObj = document.getElementById("userDoc");
            var imgSrc = $("#userImgPreview").attr('src');
            if (docObj.files && docObj.files[0]) {
                var formData = new FormData();
                formData.append("avatarFile", docObj.files[0]);
                $.ajax({
                    url: '/clbs/m/app/personalized/groupAvatar',
                    type: 'POST',
                    data: formData,
                    async: false,
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function (data) {
                        data = $.parseJSON(data);
                        if (data.statusCode == 200) {
                            $("#saveUserGroupImg").removeAttr("disabled", "disabled");
                            $("#userPhotograph").val(data.obj.avatar);
                            appSetting.init();
                            $("#editUserGroupImg").modal('hide');
                        } else {
                            if (data.exceptionDetailMsg) {
                                layer.msg(data.exceptionDetailMsg);
                            }
                        }
                    },
                    error: function (data) {
                        layer.msg("上传失败！");
                    }
                });
            } else if (imgSrc && imgSrc != '') {
                appSetting.init();
                $("#editUserGroupImg").modal('hide');
            }
        },
        //用户头像预览功能
        setImagePreviewUserImg: function (avalue) {
            var docObj = document.getElementById("userDoc");
            var imgObjPreview = document.getElementById("userImgPreview");
            if (docObj.files && docObj.files[0]) {
                //火狐下，直接设img属性
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
                var localImagId = document.getElementById("userPhoto");
                //必须设置初始大小
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
        //用户头像查看
        userGroupPhotoShow: function () {
            $("#userPhotoShow").modal("show");
        },

        //登录页logo修改
        updateLoginLogo: function () {
            // var imgSrc = $("#preview").attr("src");
            appSetting.uploadImage(); // 上传图片到服务器
        },
        getImageWidth: function (url, callback) {
            var img = new Image();
            img.src = url;
            // 如果图片被缓存，则直接返回缓存数据
            if (img.complete) {
                callback(img.width, img.height);
            } else {
                // 完全加载完毕的事件
                // img.onload = function () {
                callback(img.width, img.height);
                // }
            }
        },
        //登录页标题修改
        topTitle: function () {
            var url = "/clbs/m/app/personalized/title";
            var data = {
                "title": $("#indexTitle").val(),
            };
            json_ajax("POST", url, "json", false, data, appSetting.topTitleCallback);
        },
        topTitleCallback: function (data) {
            if (data.statusCode == '200') {
                $("#editIndexTitle").modal('hide');
                appSetting.init();
            } else {
                if (data.msg) {
                    layer.msg(data.msg, {move: false});
                }
            }
        },
        //平台网址修改
        updatePlatformSite: function () {
            var url = "/clbs/m/app/personalized/url";
            var data = {
                "url": $("#platformSite").val(),
            };
            json_ajax("POST", url, "json", false, data, appSetting.updatePlatformSiteCallback);
        },
        updatePlatformSiteCallback: function (data) {
            if (data.statusCode == '200') {
                $("#editPlatformSite").modal('hide');
                appSetting.init();
            } else {
                if (data.msg) {
                    layer.msg(data.msg, {move: false});
                }
            }
        },
        //关于登录提示修改
        updateLoginMsg: function () {
            var url = "/clbs/m/app/personalized/aboutLogin";
            var loginMsg = $("#loginMsg").val();
            var arr = loginMsg.split(/\n/);
            var num = arr.length - 1;
            if (num > 1) {
                appSetting.showErrorMsg('支持两行输入,每行最多输入24个字符', 'loginMsg', true);
                return;
            }
            for (var i = 0; i <= num; i++) {
                if (arr[i].length > 24) {
                    appSetting.showErrorMsg('支持两行输入,每行最多输入24个字符', 'loginMsg', true);
                    return;
                }
            }
            appSetting.showErrorMsg('', 'loginMsg', false);
            var data = {
                "aboutLogin": loginMsg,
            };
            json_ajax("POST", url, "json", false, data, appSetting.updateLoginMsgCallback);
        },
        updateLoginMsgCallback: function (data) {
            if (data.statusCode == '200') {
                appSetting.init();
                $("#editLoginMsg").modal('hide');
            } else {
                if (data.msg) {
                    layer.msg(data.msg, {move: false});
                }
            }
        },
        //忘记密码提示修改
        updateForget: function () {
            var url = "/clbs/m/app/personalized/pwdComment";
            var forgetVal = $("#forgetVal").val();
            var arr = forgetVal.split(/\n/);
            var num = arr.length - 1;
            if (num > 1) {
                appSetting.showErrorMsg('支持两行输入,每行最多输入24个字符', 'forgetVal', true);
                return;
            }
            for (var i = 0; i <= num; i++) {
                if (arr[i].length > 24) {
                    appSetting.showErrorMsg('支持两行输入,每行最多输入24个字符', 'forgetVal', true);
                    return;
                }
            }
            appSetting.showErrorMsg('', 'forgetVal', false);
            var data = {
                "pwdComment": forgetVal,
            };
            json_ajax("POST", url, "json", false, data, appSetting.updateForgetCallback);
        },
        updateForgetCallback: function (data) {
            if (data.statusCode == '200') {
                $("#editForgetMsg").modal('hide');
                appSetting.init();
            } else {
                if (data.msg) {
                    layer.msg(data.msg, {move: false});
                }
            }
        },

        //用户组织头像修改
        updateUserGroupImg: function () {
            var imgSrc = $("#userImgPreview").attr("src");
            appSetting.getImageWidth(imgSrc, function (width, height) {
                if ((width / height) == 1) {
                    if (width > 250 || height > 250) {
                        layer.msg("上传尺寸请小于250*250!");
                        return;
                    }
                    appSetting.uploadImageIco();// 上传图片到服务器
                } else if (width == 0 && height == 0 && imgSrc) {
                    appSetting.uploadImageIco();// 上传图片到服务器
                }
                else {
                    layer.msg("图片比例需为1:1!");
                }
            });
        },
        //关于我们显示内容(个人中心)修改
        updateAboutMe: function () {
            var url = "/clbs/m/app/personalized/aboutUs";
            var aboutMe = $("#setAboutMe").val();
            var arr = aboutMe.split(/\n/);
            var num = arr.length - 1;
            if (num > 2) {
                appSetting.showErrorMsg('支持三行输入,每行最多输入24个字符', 'setAboutMe', true);
                return;
            }
            for (var i = 0; i <= num; i++) {
                if (arr[i].length > 24) {
                    appSetting.showErrorMsg('支持三行输入,每行最多输入24个字符', 'setAboutMe', true);
                    return;
                }
            }
            appSetting.showErrorMsg('', 'setAboutMe', false);
            var data = {
                "aboutUs": aboutMe,
            };
            json_ajax("POST", url, "json", false, data, appSetting.updateAboutMeCallback);
        },
        updateAboutMeCallback: function (data) {
            if (data.statusCode == '200') {
                $("#editAboutMe").modal('hide');
                appSetting.init();
            } else {
                if (data.msg) {
                    layer.msg(data.msg, {move: false});
                }
            }
        },

        //开始聚合对象数量修改
        updateAggregationNum: function () {
            if (!appSetting.aggregationValidate()) return;
            var url = "/clbs/m/app/personalized/aggrNum";
            var data = {
                "aggrNum": $("#setAggregationNum").val(),
            };
            json_ajax("POST", url, "json", false, data, appSetting.updateAggregationNumCallback);
        },
        updateAggregationNumCallback: function (data) {
            if (data.statusCode == '200') {
                $("#editAggregation").modal('hide');
                appSetting.init();
            } else {
                if (data.msg) {
                    layer.msg(data.msg, {move: false});
                }
            }
        },
        //开始聚合对象数量输入验证
        aggregationValidate: function () {
            return $("#aggForm").validate({
                rules: {
                    setAggregationNum: {
                        required: true,
                        digits: true,
                        range: [30, 50]
                    }
                },
                messages: {
                    setAggregationNum: {
                        required: aggregationNumNull,
                        digits: aggregationNumError,
                        range: aggregationNumError
                    }
                }
            }).form();
        },
        //最多查询时间范围(历史数据)修改
        updateHistoryTimeRange: function () {
            var url = "/clbs/m/app/personalized/historyPeriod";
            var data = {
                "historyPeriod": $("#setHistoryTimeRange").val(),
            };
            json_ajax("POST", url, "json", false, data, appSetting.updateHistoryTimeRangeCallback);
        },
        updateHistoryTimeRangeCallback: function (data) {
            if (data.statusCode == '200') {
                appSetting.init();
                $("#editHistoryTimeRange").modal('hide');
            } else {
                if (data.msg) {
                    layer.msg(data.msg, {move: false});
                }
            }
        },
        //最多查询时间范围(报警查询)修改
        updateAlarmTimeRange: function () {
            var url = "/clbs/m/app/personalized/alarmPeriod";
            var data = {
                "alarmPeriod": $("#setAlarmTimeRange").val(),
            };
            json_ajax("POST", url, "json", false, data, appSetting.updateAlarmTimeRangeCallback);
        },
        updateAlarmTimeRangeCallback: function (data) {
            if (data.statusCode == '200') {
                appSetting.init();
                $("#editAlarmTimeRange").modal('hide');
            } else {
                if (data.msg) {
                    layer.msg(data.msg, {move: false});
                }
            }
        },
        //最多选择对象数量修改
        updateSelectObjNum: function () {
            if (!appSetting.selectObjNumValidate()) return;
            var url = "/clbs/m/app/personalized/maxStatObjNum";
            var data = {
                "maxStatObjNum": $("#setSelectObjNum").val(),
            };
            json_ajax("POST", url, "json", false, data, appSetting.updateSelectObjNumCallback);
        },
        updateSelectObjNumCallback: function (data) {
            if (data.statusCode == '200') {
                appSetting.init();
                $("#editSelectObjNum").modal('hide');
            } else {
                if (data.msg) {
                    layer.msg(data.msg, {move: false});
                }
            }
        },
        //最多选择对象数量输入验证
        selectObjNumValidate: function () {
            return $("#selectObjForm").validate({
                rules: {
                    setSelectObjNum: {
                        required: true,
                        digits: true,
                        range: [50, 100]
                    }
                },
                messages: {
                    setSelectObjNum: {
                        required: selectObjNumNull,
                        digits: selectObjNumError,
                        range: selectObjNumError
                    }
                }
            }).form();
        },

        //将null转成空字符串
        converterNullToBlank: function (nullValue) {
            if (nullValue == null || nullValue == undefined || nullValue == "null" || nullValue == "")
                return "-";
            else
                return nullValue;
        },
        //设为默认公共方法
        setDefault: function (url, paramName, tarId) {
            if (paramName && tarId) {
                var curVal = $("#" + tarId).text();
                if (tarId == 'loginLogo') {
                    var arr = $("#loginLogo").attr('src').split('/');
                    curVal = arr[arr.length - 1];
                }
                if (tarId == 'userGroupPhoto') {
                    var arr = $("#userGroupPhoto").attr('src').split('/');
                    curVal = arr[arr.length - 1];
                }
                if (tarId == 'activeSafety') {
                    curVal = curVal == '开启' ? 1 : 0;
                }
                var data = {};
                data['' + paramName + ''] = curVal;
                if (curVal === '') {
                    layer.msg('请先设置内容');
                    return;
                }
            } else {
                data = null;
            }

            layer.confirm('是否设置为默认', {
                title: '操作确认',
                icon: 3, // 问号图标
                btn: ['确定', '取消'] //按钮
            }, function () {
                json_ajax("POST", url, "json", false, data, appSetting.setPublicCallback);
                layer.closeAll('dialog');
            });
        },
        setPublicCallback: function (data) {
            if (data.statusCode == '200') {
                appSetting.init();
                layer.msg('设置成功');
            } else {
                if (data.msg) {
                    layer.msg(data.msg, {move: false});
                }
            }
        },

        //恢复默认公共方法
        recoverDefault: function (url) {
            layer.confirm('是否恢复默认信息', {
                title: '操作确认',
                icon: 3, // 问号图标
                btn: ['确定', '取消'] //按钮
            }, function () {
                json_ajax("POST", url, "json", false, null, appSetting.recoverPublicCallback);
                // layer.closeAll('dialog');
            });
        },
        recoverPublicCallback: function (data) {
            if (data.statusCode == '200') {
                appSetting.init();
                layer.msg('已恢复');
            } else if (data.statusCode == 400) {
                layer.msg('恢复默认失败,原默认内容为空', {move: false});
            } else {
                if (data.msg) {
                    layer.msg(data.msg, {move: false});
                }
            }
        },
        //全选报警设置
        checkAllAlarm: function () {
            $(".alarmCheck").prop('checked', true);
        },
        //取消全选报警设置
        noCheckAllAlarm: function () {
            $(".alarmCheck").prop('checked', false);
        },

        //获取当前组织
        getCurOrganization: function () {
            var url = '/clbs/m/app/personalized/groupInfoByUser';
            json_ajax("GET", url, "json", false, null, function (data) {
                if (data.statusCode == '200') {
                    var list = data.obj;
                    var id = list.uuid;
                    curId = list.uuid;
                    $('.curOrganization').val(list.name).attr('data-id', id);
                    //获取组织报警类型配置信息
                    appSetting.getOrganizationSetting(id);
                    // 获取配置的综合统计数据
                    appSetting.getStatisticsList(id);
                    // 获取配置对象详情数据
                    appSetting.getDetailConfig(id);
                } else {
                    if (data.msg) {
                        layer.msg(data.msg, {move: false});
                    }
                }
            })
        },
        //获取参考组织信息
        getOrganizationInfo: function () {
            var url = '/clbs/m/app/group/reference';
            json_ajax("GET", url, "json", false, null, function (data) {
                groupObj = data;
                appSetting.getOrganizationInfoBack(groupObj);
            })
        },
        getOrganizationInfoBack: function (data) {
            var html = '<option value="-1">请选择参考组织</option>';
            if (data.statusCode == '200') {
                var list = data.obj.grups;
                var len = list.length;
                if (len > 0) {
                    for (var i = 0; i < len; i++) {
                        html += '<option value="' + list[i].id + '">' + list[i].name + '</option>'
                    }
                }
            } else {
                if (data.msg) {
                    layer.msg(data.msg, {move: false});
                }
            }
            $("#organization").html(html);
        },
        //查看组织报警类型配置信息
        getOrganizationSetting: function (curId) {
            var url = '/clbs/m/app/group/' + curId + '/alarmType/config/list';
            if (curId != '-1') {
                var data = {
                    'id': curId
                };
                json_ajax("POST", url, "json", false, data, function (data) {
                    var alarmCheckList = $(".alarmCheck");
                    alarmCheckList.prop('checked', false);//设置全部报警类型为取消勾选状态
                    var html = '';

                    if (data.statusCode == '200') {
                        var alarmList = data.obj.alarmTypes;
                        var alarmLen = alarmList.length;
                        var checkLen = alarmCheckList.length;
                        if (alarmLen > 0) {
                            var typeArr = [];
                            var index = null;
                            for (var i = 0; i < alarmLen; i++) {
                                var obj = {
                                    category: alarmList[i].category,
                                    value: [{
                                        name: alarmList[i].name,
                                        type: alarmList[i].type,
                                    }],
                                };
                                index = null;
                                var newLen = typeArr.length;
                                for (var j = 0; j < newLen; j += 1) {
                                    if (typeArr[j].category == alarmList[i].category) {
                                        index = j;
                                        break;
                                    }
                                }
                                if (index == null) {
                                    typeArr.push(obj);
                                } else {
                                    typeArr[index].value.push(obj.value[0]);
                                }
                            }

                            var typeLen = typeArr.length;
                            for (var i = 0; i < typeLen; i++) {
                                //组装报警类型配置信息
                                html += '<li class="title">' + typeArr[i].category + '</li>';
                                var value = typeArr[i].value;
                                var valueLen = typeArr[i].value.length;
                                for (var j = 0; j < valueLen; j++) {
                                    html += '<li><span>' + value[j].name + '</span></li>';
                                    //设置报警类型选中状态
                                    for (var k = 0; k < checkLen; k++) {
                                        if ($(alarmCheckList[k]).attr('value') == value[j].type) {
                                            $(alarmCheckList[k]).prop('checked', true);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (data.msg) {
                            layer.msg(data.msg, {move: false});
                        }
                    }
                    $("#alarmDeatilUl").html(html);
                })
            }
        },
        //保存报警类型配置信息
        saveAlarmSettingInfo: function () {
            var checkAlarm = $(".alarmCheck:checked");
            var len = checkAlarm.length;
            var id = $(".curOrganization").data('id');
            var alarmType = [];
            var paramObj = {
                'id': id,
            };
            if (len > 0) {
                for (var i = 0; i < len; i++) {
                    var _this = $(checkAlarm[i]);
                    var curObj = {
                        category: _this.closest('li').prevAll('.title:eq(0)').text(),
                        name: _this.siblings('span').text(),
                        appVersion: _this.siblings("input").val(),
                        type: _this.val()
                    };
                    alarmType.push(curObj);
                }
            }
            paramObj.alarmTypes = JSON.stringify(alarmType);
            var url = '/clbs/m/app/group/' + id + '/alarmType/config';
            json_ajax("POST", url, "json", false, paramObj, function (data) {
                if (data.statusCode == '200') {
                    $("#editAlarmSetting").modal('hide');
                    appSetting.init();
                    layer.msg('修改成功');
                } else {
                    if (data.msg) {
                        layer.msg(data.msg, {move: false});
                    }
                }
            })
        },
        //显示隐藏错误提示信息
        showErrorMsg: function (msg, inputId, flag) {
            var siblingsError = $("#" + inputId).siblings('label.error')
            if (flag) {
                if (siblingsError.length == 0) {
                    $('<label id="error_label" class="error">' + msg + '</label>').insertAfter($("#" + inputId));
                }
            } else {
                siblingsError.remove();
            }
        },
        //获取配置的综合统计数据
        getStatisticsList: function (id) {
            var url = '/clbs/m/app/statistics/config/list';
            json_ajax("POST", url, "json", false, {id: id}, function (data) {
                var html = '';
                if (data.statusCode == '200') {
                    var list = data.obj.statistics;
                    var len = list.length;
                    var checkList = $(".statisticsCheck");
                    var checkLen = checkList.length;

                    // if (len > 0) {
                        checkList.prop('checked', false);//设置全部综合统计为取消勾选状态

                        for (var i = 0; i < len; i++) {
                            html += '<li><span>' + list[i].name + '</span></li>';
                            for (var j = 0; j < checkLen; j++) {
                                if ($(checkList[j]).siblings('span').text() == list[i].name) {
                                    $(checkList[j]).prop('checked', true);
                                    break;
                                }
                            }
                        }
                    // }
                }
                $("#statisticsDeatilUl").html(html);
            })
        },
        //获取综合统计参考组织信息
        getStatisticsOrgInfo: function () {
            var url = '/clbs/m/app/statistics/group';
            json_ajax("POST", url, "json", false, null, function (data) {
                statisticsOrgInfo = data;
                appSetting.getStatisticsOrgInfoBack(statisticsOrgInfo);
            })
        },
        getStatisticsOrgInfoBack: function (data) {
            var html = '<option value="-1">请选择参考组织</option>';
            if (data.statusCode == '200') {
                var list = data.obj.grups;
                var len = list.length;

                if (len > 0) {
                    for (var i = 0; i < len; i++) {
                        html += '<option value="' + list[i].id + '">' + list[i].name + '</option>';
                    }
                }
            } else {
                if (data.msg) {
                    layer.msg(data.msg, {move: false});
                }
            }
            $("#organization1").html(html);
        },
        //保存综合统计配置信息
        saveStatisticsInfo: function () {
            var checkData = $(".statisticsCheck:checked");
            var len = checkData.length;
            var id = $(".curOrganization").data('id');
            var statisticsData = [];
            var paramObj = {
                'id': id,
            };
            if (len > 0) {
                for (var i = 0; i < len; i++) {
                    var _this = $(checkData[i]);
                    var curObj = {
                        name: _this.siblings('span').text(),
                        appVersion: _this.siblings("input").val(),
                        number: _this.val()
                    };
                    statisticsData.push(curObj);
                }
            }
            paramObj.statistics = JSON.stringify(statisticsData);
            var url = '/clbs/m/app/statistics/config';
            json_ajax("POST", url, "json", false, paramObj, function (data) {
                if (data.statusCode == '200') {
                    $("#editStatistics").modal('hide');
                    appSetting.init();
                    layer.msg('修改成功');
                } else {
                    if (data.msg) {
                        layer.msg(data.msg, {move: false});
                    }
                }
            })
        },
        //全选综合统计设置
        checkAllStatistics: function () {
            $(".statisticsCheck").prop('checked', true);
        },
        //取消全选综合统计设置
        noCheckAllStatistics: function () {
            $(".statisticsCheck").prop('checked', false);
        },
        //保存主动安全信息
        saveActiveSafetyInfo: function () {
            var data = {
                adasFlag: $(".adasFlag:checked").val()
            }
            var url = '/clbs/m/app/personalized/adasFlag';
            json_ajax("POST", url, "json", false, data, function (data) {
                if (data.statusCode == '200') {
                    $("#editActiveSafety").modal('hide');
                    appSetting.init();
                    layer.msg('修改成功');
                } else {
                    if (data.msg) {
                        layer.msg(data.msg, {move: false});
                    }
                }
            })
        },
        /**
         * 对象详情设置
         */
        tabChange: function () {
            detailTab = $(this).index();
            appSetting.getDetailOrganization();
        },
        //获取对象详情参考组织
        getDetailOrganization: function(){
            var param = {
                type: detailTab
            };
            json_ajax("GET", '/clbs/m/app/monitor/config/reference', "json", false, param, function (data) {
                appSetting.getDetailReference(data, '#organization2');
            })
        },
        getDetailReference: function (data, id) {
            var html = '<option value="-1">请选择参考组织</option>';
            if (data.statusCode == '200') {
                var list = data.obj.groups;
                var len = list.length;

                if (len > 0) {
                    for (var i = 0; i < len; i++) {
                        html += '<option value="' + list[i].id + '">' + list[i].name + '</option>';
                    }
                }
            } else {
                if (data.msg) {
                    layer.msg(data.msg, {move: false});
                }
            }
            $(id).html(html);
        },
        //设置详情配置
        setDetailConfig: function(){
            if(!appSetting.validateCheck()){//验证是否勾选4个
                return;
            }

            var id = $(".curOrganization").data('id');
            var checkList = $('.detailCheck:checked');
            var monitorConfigs = [];
            for(var i=0;i<checkList.length;i++){
                var _this = $(checkList[i]);
                var obj = {
                    category: _this.closest('li').prevAll('.title:eq(0)').text(),
                    name: _this.siblings('span').text(),
                    type: _this.val(),
                    appVersion: 20102,
                };

                monitorConfigs.push(obj);
            }

            var paramObj={
                id: id,
                monitorConfigs: JSON.stringify(monitorConfigs),
            };

            json_ajax("POST", '/clbs/m/app/monitor/config/update', "json", false, paramObj, function (data) {
                if (data.statusCode == '200') {
                    $("#editDetailSetting").modal('hide');
                    appSetting.init();
                    layer.msg('修改成功');
                } else {
                    if (data.msg) {
                        layer.msg(data.msg, {move: false});
                    }
                }
            })
        },
        validateCheck: function(){
            var vehicleCon = $('#vehicleCon .detailCheck:checked').length;
            var personCon = $('#personCon .detailCheck:checked').length;
            var thingCon = $('#thingCon .detailCheck:checked').length;

            if(vehicleCon == 4 && personCon == 4 && thingCon == 4){
                return true;
            }

            if(vehicleCon != 4){
                detailTab = 0;
            }else if(personCon != 4){
                detailTab = 1;
            }else if(thingCon != 4){
                detailTab = 2;
            }
            appSetting.getDetailOrganization();
            $('.nav-tabs li').eq(detailTab).addClass('active').siblings('li').removeClass('active');
            $('.tab-content .tab-pane').eq(detailTab).addClass('active').siblings('.tab-pane').removeClass('active');
            layer.msg('车人物属性勾选项必须等于4项，请确认');
            return false;
        },
        //获取详情配置
        getDetailConfig: function(id){
            var paramObj = {
                id: id
            };

            json_ajax("POST", '/clbs/m/app/monitor/config/list', "json", false, paramObj, function (data) {
                if (data.statusCode == '200') {
                    var datas = data.obj.monitorConfigs;
                    if(datas.length > 0){
                        appSetting.setDetailDatas(datas);
                    }
                } else {
                    if (data.msg) {
                        layer.msg(data.msg, {move: false});
                    }
                }
            })
        },
        setDetailDatas: function(datas){
            var vehicleArr = [],
                personArr = [],
                thingArr = [];

            var detailCheck = $('.detailCheck');
            detailCheck.prop('checked', false);

            for(var i=0;i<datas.length;i++){
                var item = datas[i];
                //修改弹层选中已有配置
                appSetting.setDetailCheck(item, detailCheck);

                switch (item.type){
                    case '0'://车
                        vehicleArr.push(item);
                        break;
                    case '1'://人
                        personArr.push(item);
                        break;
                    case '2'://物
                        thingArr.push(item);
                        break;
                    default:
                        break;
                }
            }

            //数据组装
            var vehicleInfos = appSetting.setNewData(vehicleArr);
            var personInfos = appSetting.setNewData(personArr);
            var thingInfos = appSetting.setNewData(thingArr);

            //dom组装
            var vehicleHtml = '',
                personHtml = '',
                thingHtml = '';
            vehicleHtml = appSetting.setDetailDom(vehicleInfos);
            personHtml = appSetting.setDetailDom(personInfos);
            thingHtml = appSetting.setDetailDom(thingInfos);
            $('#vehicleCon2 ul').html(vehicleHtml);
            $('#personCon2 ul').html(personHtml);
            $('#thingCon2 ul').html(thingHtml);
        },
        setDetailCheck: function(item, detailCheck){
            for(var i=0;i<detailCheck.length;i++){
                var dom = $(detailCheck[i]);
                var name = dom.siblings('span').text(),
                    category = dom.closest('li').prevAll('.title:eq(0)').text(),
                    type = dom.val();

                if(item.category == category && item.type == type && item.name == name){
                    dom.prop('checked', true);
                }
            }
        },
        setNewData: function(data){
            // data.sort(function(a, b){
            //     return a.category.localeCompare(b.category);
            // });
            var dataArr = {};

            for(var i=0;i<data.length;i++){
                var item = data[i];
                var category = item.category;

                if(!(category in dataArr)){
                    dataArr[category] = [item.name];
                }else{
                    dataArr[category].push(item.name);
                }
            }

            return dataArr;
        },
        setDetailDom: function(obj){
            var html = '';
            for(var key in obj){
                html += '<li class="title">'+ key +'</li>';
                for(var j=0;j<obj[key].length;j++){
                    var name =obj[key][j];
                    html += '<li><span>'+name+'</span></li>';
                }
            }
            return html;
        }
    };
    $(function () {
        $('input').inputClear();
        appSetting.init();
        //登录页logo修改
        $("#updateLoginLogo").on("click", appSetting.updateLoginLogo);
        //登录页标题修改
        $("#topTitle").on("click", appSetting.topTitle);
        //平台网址修改
        $("#savePlatformSite").on("click", appSetting.updatePlatformSite);
        //关于登录提示修改
        $("#saveLoginMsg").on("click", appSetting.updateLoginMsg);
        //忘记密码提示修改
        $("#saveForget").on("click", appSetting.updateForget);

        //用户组织头像修改
        $("#saveUserGroupImg").on("click", appSetting.updateUserGroupImg);
        //关于我们显示内容(个人中心)修改
        $("#saveAboutMe").on("click", appSetting.updateAboutMe);

        //开始聚合对象数量修改
        $("#saveAggregationNum").on("click", appSetting.updateAggregationNum);
        //最多查询时间范围(历史数据)修改
        $("#saveHistoryTimeRange").on("click", appSetting.updateHistoryTimeRange);
        //最多查询时间范围(报警查询)修改
        $("#saveAlarmTimeRange").on("click", appSetting.updateAlarmTimeRange);
        //最多选择对象数量修改
        $("#saveSelectObjNum").on("click", appSetting.updateSelectObjNum);

        //设为默认
        $("#setDefLoginLogo").on("click", function () {
            appSetting.setDefault('/clbs/m/app/personalized/logo/default', 'image', 'loginLogo');
        });
        $("#setDefIndexTitle").on("click", function () {
            appSetting.setDefault('/clbs/m/app/personalized/title/default', 'title', 'topTitleMsg');
        });
        $("#setDefBottomTitle").on("click", function () {
            appSetting.setDefault('/clbs/m/app/personalized/url/default', 'url', 'bottomTitleMsg');
        });
        $("#setDefResourceName").on("click", function () {
            appSetting.setDefault('/clbs/m/app/personalized/aboutLogin/default', 'aboutLogin', 'frontPageMsg');
        });
        $("#setDefForgetMsg").on("click", function () {
            appSetting.setDefault('/clbs/m/app/personalized/pwdComment/default', 'pwdComment', 'forgetMsg');
        });
        $("#setDefAggregation").on("click", function () {
            appSetting.setDefault('/clbs/m/app/personalized/aggrNum/default', 'aggrNum', 'aggregationNum');
        });
        $("#setDefHistoryTimeRange").on("click", function () {
            appSetting.setDefault('/clbs/m/app/personalized/historyPeriod/default', 'historyPeriod', 'historyTimeRange');
        });
        $("#setDefAlarmTimeRange").on("click", function () {
            appSetting.setDefault('/clbs/m/app/personalized/alarmPeriod/default', 'alarmPeriod', 'alarmTimeRange');
        });
        $("#setDefSelectObjNum").on("click", function () {
            appSetting.setDefault('/clbs/m/app/personalized/maxStatObjNum/default', 'maxStatObjNum', 'selectObjNum');
        });
        $("#setDefUserGroupImg").on("click", function () {
            appSetting.setDefault('/clbs/m/app/personalized/groupAvatar/default', 'avatar', 'userGroupPhoto');
        });
        $("#setDefAboutMe").on("click", function () {
            appSetting.setDefault('/clbs/m/app/personalized/aboutUs/default', 'aboutUs', 'aboutMe');
        });
        $("#setDefAlarmType").on("click", function () {
            appSetting.setDefault('/clbs/m/app/group/alarmType/default');
        });
        $("#setDefStatistics").on("click", function () {
            appSetting.setDefault('/clbs/m/app/statistics/default');
        });
        $("#setDefActiveSafety").on("click", function () {
            appSetting.setDefault('/clbs/m/app/personalized/adasFlag/default', 'adasFlag', 'activeSafety');
        });
        $("#setDefDetail").on("click", function () {
            appSetting.setDefault('/clbs/m/app/monitor/config/default');
        });

        //恢复默认
        $("#defaultLoginLogo").on("click", function () {
            appSetting.recoverDefault('/clbs/m/app/personalized/logo/reset');
        });
        $("#defaultIndexTitle").on("click", function () {
            appSetting.recoverDefault('/clbs/m/app/personalized/title/reset');
        });
        $("#defaultBottomTitle").on("click", function () {
            appSetting.recoverDefault('/clbs/m/app/personalized/url/reset');
        });
        $("#defaultResourceName").on("click", function () {
            appSetting.recoverDefault('/clbs/m/app/personalized/aboutLogin/reset');
        });
        $("#defaultForgetMsg").on("click", function () {
            appSetting.recoverDefault('/clbs/m/app/personalized/pwdComment/reset');
        });
        $("#defaultAggregation").on("click", function () {
            appSetting.recoverDefault('/clbs/m/app/personalized/aggrNum/reset');
        });
        $("#defaultHistoryTimeRange").on("click", function () {
            appSetting.recoverDefault('/clbs/m/app/personalized/historyPeriod/reset');
        });
        $("#defaultAlarmTimeRange").on("click", function () {
            appSetting.recoverDefault('/clbs/m/app/personalized/alarmPeriod/reset');
        });
        $("#defaultSelectObjNum").on("click", function () {
            appSetting.recoverDefault('/clbs/m/app/personalized/maxStatObjNum/reset');
        });
        $("#defaultUserGroupImg").on("click", function () {
            appSetting.recoverDefault('/clbs/m/app/personalized/groupAvatar/reset');
        });
        $("#defaultAboutMe").on("click", function () {
            appSetting.recoverDefault('/clbs/m/app/personalized/aboutUs/reset');
        });
        $("#defaultAlarmType").on("click", function () {
            appSetting.recoverDefault('/clbs/m/app/group/alarmType/reset');
        });
        $("#defaultStatistics").on("click", function () {
            appSetting.recoverDefault('/clbs/m/app/statistics/reset');
        });
        $("#defaultActiveSafety").on("click", function () {
            appSetting.recoverDefault('/clbs/m/app/personalized/adasFlag/reset');
        });
        $("#defaultDetail").on("click", function () {
            appSetting.recoverDefault('/clbs/m/app/monitor/config/reset');
        });

        //全选报警类型
        $("#checkAllAlarm").on('click', appSetting.checkAllAlarm);
        //取消全选报警类型
        $("#noCheckAllAlarm").on('click', appSetting.noCheckAllAlarm);
        //切换参考组织
        $("#organization").on('change', function () {
            var curId = $(this).val();
            //获取组织报警类型配置信息
            appSetting.getOrganizationSetting(curId);
        })
        //保存报警配置信息
        $("#saveAlarmType").on('click', appSetting.saveAlarmSettingInfo);

        $(".appSetting .btn-default").on('click', function (e) {
            var _this = $(e.target);
            if (_this.data('dismiss') == 'modal') {// 弹窗数据还原
                // appSetting.init();
                $("label.error").remove();
                appSetting.setInfo(infoList);
                appSetting.getOrganizationInfoBack(groupObj);
                appSetting.getStatisticsOrgInfoBack(statisticsOrgInfo);
                //获取组织报警类型配置信息
                appSetting.getOrganizationSetting(curId);
                appSetting.getStatisticsList(curId);
                //对象详情还原
                appSetting.getDetailConfig(curId);
            }
        });

        // 点击报警类型/综合统计切换勾选状态
        $("#editAlarmSetting .mult-check-ul,#editStatistics .mult-check-ul").on('click', 'li', function (e) {
            var name = e.target.tagName;
            if (name != 'INPUT') {
                var _this = $(this);
                var input = _this.find('input');
                if (input.length > 0) {
                    var check = input.is(':checked');
                    input.prop('checked', !check);
                }
            }
        })

        //保存综合统计配置信息
        $("#saveStatistics").on('click', appSetting.saveStatisticsInfo);
        //全选综合统计
        $("#checkAllStatistics").on('click', appSetting.checkAllStatistics);
        //取消全选综合统计
        $("#noCheckAllStatistics").on('click', appSetting.noCheckAllStatistics);
        //切换参考组织
        $("#organization1").on('change', function () {
            var curId = $(this).val();
            // 获取配置的综合统计数据
            appSetting.getStatisticsList(curId);
        });
        //保存主动安全信息
        $("#saveActiveSafety").on('click', appSetting.saveActiveSafetyInfo);
        //对象详情
        $('.nav-tabs').on('click', 'li', appSetting.tabChange);
        $('#saveDetailType').on('click', appSetting.setDetailConfig);
        $("#organization2").on('change', function () {
            var curId = $(this).val();
            appSetting.getDetailConfig(curId);
        })
    })
})
($, window)