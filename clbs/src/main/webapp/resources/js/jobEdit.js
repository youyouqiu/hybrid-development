//# sourceURL=professionalAdd.js
(function ($, window) {
    var imageWidth;
    var imageHeight;
    window.jobEdit = {
        doSubmit: function () {
            if (jobEdit.validates()) {
                if ($('#jobIconName').val().length === 0) {
                    layer.msg('请设置职位图标');
                    return false;
                }
                var $jobIconName = $('#jobIconName');
                var str = $jobIconName.val();
                str = str.substring(str.lastIndexOf('/')+1, str.length);
                $jobIconName.val(str);
                $("#editForm").ajaxSubmit(function (data) {
                    var json = eval("(" + data + ")");
                    if (json.success) {
                        $("#commonSmWin").modal("hide");
                        myTable.requestData();
                    } else {
                        layer.msg(json.msg);
                    }
                });
            };
        },
        validates: function () {
            return $("#editForm").validate({
                rules: {
                    jobName: {
                        required: true,
                        isJobNameCanNull: true,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/talkback/basicinfo/monitoring/job/checkJobName",
                            data: {
                                name: function () {
                                    return $("#jobName").val();
                                },
                                id:function () {
                                    return $('#editId').val();
                                }
                            }
                        }
                    },
                    jobIconName: {
                        required: true
                    }
                },
                messages: {
                    jobName: {
                        required: nameNull,
                        isJobNameCanNull: jobNameError,
                        remote: jobNameExist
                    },
                    jobIconName: {
                        required: publicNull
                    }
                }
            }).form();
        },
        //图片上传预览功能
        setImagePreview: function (avalue) {
            jobEdit.uploadImage(); // 上传图片到服务器
            var docObj = document.getElementById("doc");
            var imgObjPreview = document.getElementById("preview");
            if (docObj.files && docObj.files[0]) {
                //火狐下，直接设img属性
                imgObjPreview.style.display = 'block';
                imgObjPreview.style.width = '40px';
                imgObjPreview.style.height = '47px';
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
                localImagId.style.width = '40px';
                localImagId.style.height = '47px';
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
                    url: '/clbs/talkback/basicinfo/monitoring/job/upload_img',
                    type: 'POST',
                    data: formData,
                    async: false,
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function (data) {
                        data = $.parseJSON(data);
                        if (data.imgName === "0") {
                            layer.msg("不支持的图片格式文件，<br/>支持格式（png）");
                            $("#preview").src("");
                        } else if (data.imgName === "1") {
                            layer.msg("图标大小范围为：[30-40]*[37-47]");
                            $("#preview").src("");
                        } else {
                            $("#jobIconName").val(data.imgName);
                        }
                    },
                    error: function (data) {
                        layer.msg(publicUploadingError);
                    }
                });
            }
        }
    };
    $(function () {
        $('input').inputClear();
        $("#doSubmitEdit").on("click", jobEdit.doSubmit);
    })
})($, window)