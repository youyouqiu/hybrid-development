$(function () {
    $("input").inputClear();
    $.ajax({
        url: '/clbs/inspectAuthorizationDate',
        type: 'POST',
        data: null,
        async: false,
        dataType: 'json',
        success: function (data) {
            if (data != null) {
                if (data.success == true) {
                    if (data.obj.errMsg != null && data.obj.errMsg != "") {
                        layer.msg(data.obj.errMsg, {move: false});
                    }
                } else {
                    if (data != null && data != "") {
                        if (data.msg != null && data.msg != "") {
                            layer.msg(data.msg, {move: false});
                        }
                    }
                }
            }
        },
    });

    init();

    /**
     * 个性设置初始化方法
     */
    function init() {
        $("#urlName").val("");
        $(".homeListBottom").each(function () {
            $(this).find('.innerShadowGrey').addClass('noChoseIcon');
            $(this).find('span').text("");
            $($(this).find('input')).val("");
        });
        $('input[type=radio][name=siteInnerOuter]').change(function () {
            if (this.value == 'siteInnerOuter1') {
                $('#fastNavType').val('0');
                $('#innerContainer').show();
                $('#outerContainer').hide();
                $('#descriptionDanger').hide();
            } else {
                $('#fastNavType').val('1');
                $('#innerContainer').hide();
                $('#outerContainer').show();
                $('#descriptionDanger').show();
            }
        });
        $.ajax({
            url: '/clbs/c/fastNav/list',
            type: 'POST',
            data: null,
            async: false,
            dataType: 'json',
            success: function (data) {
                if (data != null) {
                    if (data.success) {
                        if (data.obj != null && data.obj.length > 0) {
                            var result = data.obj;
                            for (var i = 0; i < result.length; i++) {
                                $(".homeListBottom").each(function () {
                                    if ($(this).attr('data-num') == result[i].order) {
                                        var navType = result[i].navType === null ? 0 : result[i].navType;

                                        $($(this).find('.description')).val(result[i].urlName);
                                        $($(this).find('.urlId')).val(result[i].urlId);
                                        $($(this).find('.realId')).val(result[i].id);
                                        $(this).find('.navType').val(navType);
                                        if (result[i].description != null && result[i].description != '') {
                                            $(this).find('span').text(result[i].description);
                                        } else {
                                            $(this).find('span').text(result[i].urlName);
                                        }
                                        if (result[i].url == null || result[i].url == "") {
                                            //没有权限的地址
                                            $($(this).find('.jumptoweb')).val("No-Authority");
                                        } else {
                                            if (navType == 0) {
                                                $($(this).find('.jumptoweb')).val("/clbs" + result[i].url);
                                            } else {
                                                $($(this).find('.jumptoweb')).val(result[i].url);
                                            }
                                        }
                                        $(this).find('.innerShadowGrey').removeClass('noChoseIcon');
                                    }
                                });
                            }
                        }
                        initDraggable();
                    } else {
                        if (data != null && data != "") {
                            if (data.msg != null && data.msg != "") {
                                layer.msg(data.msg, {move: false});
                            }
                        }
                    }
                }
            },
        });
    }

    function initDraggable() {
        var startX = 0;
        var startY = 0;
        var isDragging = false;
        window.isMoving = false;
        var $ele = null;

        var moveHandler = function (event) {
            event.preventDefault();
            if (isDragging) {
                var clientX = event.clientX;
                var clientY = event.clientY;

                var left = clientX - startX;
                var top = clientY - startY;

                if (Math.abs(left) > 5 || Math.abs(top) > 5) {
                    window.isMoving = true;
                }

                $ele.css({
                    'left': left.toString() + 'px',
                    'top': top.toString() + 'px',
                });
            }
        };

        var upHandler = function (event) {
            event.preventDefault();
            if (isDragging) {
                setTimeout(function () {
                    window.isMoving = false;
                }, 200);
                isDragging = false;
                $ele.css('z-index', '0');
                $('body').off('mousemove', this.moveHandler);

                var allEle = $('.homeListBottom');
                var selfIndex = $ele.attr('data-num') - 1;
                var selfRect = $ele[0].getBoundingClientRect();
                var acceptIndex = null;

                for (var i = 0; i < allEle.length; i++) {
                    if (i === selfIndex) continue;
                    var rect = allEle[i].getBoundingClientRect();
                    var xDiff = selfRect.width - Math.abs(rect.left - selfRect.left);
                    var yDiff = selfRect.height - Math.abs(rect.top - selfRect.top);
                    if (xDiff < 0 || yDiff < 0) continue;
                    var area = xDiff * yDiff;
                    if (area > (selfRect.width * selfRect.height / 2)) {
                        acceptIndex = i;
                        break;
                    }
                }
                console.log(acceptIndex)

                if (acceptIndex !== null) {
                    var $acceptEle = $('.homeListBottom:eq(' + acceptIndex + ')');

                    var editOrder = selfIndex + 1;
                    var editedOrder = $acceptEle.attr('data-num');
                    var editId = $ele.find('.realId').val();
                    var editedId = $acceptEle.find('.realId').val();

                    $ele.attr('data-num', editedOrder);
                    $acceptEle.attr('data-num', editOrder);
                    swapElement($acceptEle, $ele);
                    var url = "/clbs/c/fastNav/editOrder";
                    $.ajax({
                        url: url,
                        type: 'POST',
                        data: {
                            editId: editId,
                            editedId: editedId,
                            editOrder: editOrder,
                            editedOrder: editedOrder,
                        },
                        async: false,
                        dataType: 'json',
                        success: function (data) {
                            if (data != null) {
                                if (!data.success) {
                                    layer.msg('后台报错');
                                }
                            }
                        },
                    });
                }
                $ele.css({
                    'left': '0px',
                    'top': '0px',
                });
                $ele = null;
            }
        };

        $('.homeListBottom').on('mousedown', function (event) {
            event.preventDefault();
            startX = event.clientX;
            startY = event.clientY;
            isDragging = true;
            $ele = $(this);

            $ele.css('z-index', '9999');
            $('body').on('mousemove', moveHandler);

        });
        $('body').on('mouseup', upHandler);
        $('body').on('mouseleave', upHandler);
    }

    function swapElement(a, b) {
        // create a temporary marker div
        var aNext = $('<div>').insertAfter(a);
        a.insertAfter(b);
        b.insertBefore(aNext);
        // remove marker div
        aNext.remove();
    }

    /*
    * 自定义导航功能
    * */
    function ajaxDataFilter(treeId, parentNode, responseData) {
        if (responseData) {
            for (var i = 0; i < responseData.length; i++) {
                responseData[i].open = true;
            }
        }
        if (responseData.success != undefined) {
            responseData = [];
            $("#fastNav #urlName").val('还没有任意页面权限');
        }
        return responseData;
    }

    function beforeClick(treeId, treeNode) {
        var check = (treeNode);
        return check;
    }

    // 分组下拉点击事件
    function onClick(e, treeId, treeNode) {
        var zTree = $.fn.zTree.getZTreeObj("ztreeDemo"),
            nodes = zTree.getSelectedNodes();
        if (treeNode.type == 0) { // 按钮菜单
            $("#description").val('');
            zTree.checkNode(nodes[0], true, false, true);
        }
    }

    function onCheck(e, treeId, treeNode) {
        var type = treeNode.type;
        var zTree = $.fn.zTree.getZTreeObj("ztreeDemo"), nodes = zTree
            .getCheckedNodes(true), v = "";
        if (type == 0) {
            zTree.selectNode(treeNode, false, true);
            $("#urlId").attr("value", nodes[0].id);
            v = nodes[0].name;
            var cityObj = $("#urlName");
            $("#interval").val(v);
            cityObj.val(v);
            $("#menuContent").hide();
        }
    }

    function zTreeOnAsyncSuccess(event, treeId, treeNode, msg) {
        var defautPage = $("#urlId").val();
        if (defautPage != undefined && defautPage != null && defautPage != "") {
            var treeObj = $.fn.zTree.getZTreeObj("ztreeDemo");
            var allNode = treeObj.transformToArray(treeObj.getNodes());
            if (allNode != null && allNode.length > 0) {
                for (var i = 0, len = allNode.length; i < len; i++) {
                    if (allNode[i].id == defautPage && allNode[i].type == 0) {
                        treeObj.checkNode(allNode[i], true, false, true);
                        $("#urlName").html(allNode[i].name);
                    }
                }
            }
        }
    }

    function validates() {
        var fastNavType = $('#fastNavType').val();
        if (fastNavType == '0' || fastNavType == '') {
            var urlId = $("#urlId").val();
            if (urlId == null || urlId == "") {
                return false;
            }
        } else {
            var urlId = $("#urlNameOuter").val();
            if (urlId == null || urlId == "" || urlId.length > 1024) {
                return false;
            }
            var description = $('#description').val();
            if (description == null || description == "") {
                return false;
            }
        }
        return true;
    }

    function submit() {
        if (validates()) {
            var navType = $('#fastNavType').val();
            var urlName, urlId;
            var order = $('#order').val();
            var description = $('#description').val();
            if (navType == '') {
                navType = '0';
            }
            if (navType == '0') {// 站内导航
                urlName = $('#urlName').val();
                urlId = $('#urlId').val();
            } else {// 站外导航
                urlId = $('#urlNameOuter').val();
            }
            $.ajax({
                url: '/clbs/c/fastNav/edit',
                type: 'POST',
                data: {
                    navType: navType,
                    urlId: urlId,
                    urlName: urlName,
                    order: order,
                    description: description
                },
                async: true,
                dataType: 'json',
                success: function (data) {
                    if (data.success) {
                        $("#navEditModal").modal("hide");
                        layer.msg("设置成功！", {move: false});
                        //关闭弹窗
                        init();
                    } else {
                        layer.msg(data.msg, {move: false});
                    }
                },
            });
        } else {
            layer.msg("请检查输入项", {move: false});
        }
    }

    function deleteBtn() {
        if (window.isMoving) {
            return;
        }
        var order = $(this).closest('.homeListBottom').attr('data-num');
        layer.confirm("是否清空功能入口！", {btn: ["确定", "取消"]}, function () {
            var url = "/clbs/c/fastNav/dalete_{order}";
            url = url.replace("{order}", order);
            $.ajax({
                url: url,
                type: 'POST',
                data: null,
                async: false,
                dataType: 'json',
                success: function (data) {
                    if (data != null) {
                        if (data.success) {
                            layer.closeAll();
                            init();
                        } else {
                            if (data != null && data != "") {
                                if (data.msg != null && data.msg != "") {
                                    layer.msg(data.msg, {move: false});
                                }
                            }
                        }
                    }
                },
            });
        });
    }

    $("#urlName,#groupSelectSpan").bind("click", showMenuContent);

    $(".delBtn").on("click", deleteBtn);

    $("#saveQueryPost").on("click", submit);

    $(".homeListBottom").hover(function () {
        //悬停
        if ($($(this).find('.navType')).val() == undefined || $($(this).find('.navType')).val() == null || $($(this).find('.navType')).val() == "") {
            $($(this).find('.delBtn')).css("display", "none");
            $($(this).find('.editButton')).css("display", "none");
        } else {
            $($(this).find('.delBtn')).css("display", "block");
            $($(this).find('.editButton')).css("display", "block");
        }
    }, function () {
        //离开
        $($(this).find('.editButton')).css("display", "none");
        $($(this).find('.delBtn')).css("display", "none");
    });

    $(".innerShadowGrey").on("click", function () {

        if (!window.isMoving) {
            var url = $($(this).closest('.homeListBottom').find('.jumptoweb')).val();
            var navType = $($(this).closest('.homeListBottom').find('.navType')).val();
            if (url != null && url != "") {
                if (url == "No-Authority") {
                    //没权限弹框
                    layer.msg("您没有访问权限，请联系管理员", {move: false});
                } else if (navType == '0') {// 站内跳转
                    window.location.href = url;
                } else if (navType == '1') {// 站外跳转
                    // 判断站外链接是否包含http前缀
                    if (url.substr(0, 7).toLowerCase() != "http://" && url.substr(0, 8).toLowerCase() != "https://") {
                        url = "http://" + url;
                    }
                    var isChrome = navigator.userAgent.toLowerCase().match(/chrome/);
                    if (isChrome) {// 谷歌浏览器添加链接有效性验证
                        $.ajax({
                            url: url,
                            type: 'GET',
                            complete: function (response) {
                                if (response.statusText == "SyntaxError: Failed to execute 'open' on 'XMLHttpRequest': Invalid URL") {
                                    layer.msg('无效链接');
                                } else {
                                    window.open(url, '_blank')
                                }
                            }
                        });
                    } else {
                        window.open(url, '_blank')
                    }
                }
            } else {
                $("#urlNameOuter").val('');
                $("#description").val('');
                $(this).siblings('.editButton').click();
            }
        }
    });

    $('.notPermissions').on("click", function (e) {
        e.preventDefault();
        layer.msg("您没有访问权限，请联系管理员");
    });

    $('.editButton').on('click', function () {

        if (window.isMoving) {
            return;
        }

        var num = $(this).closest('.homeListBottom').attr('data-num');
        var span = $($(this).closest('.homeListBottom').find('span')).text();
        var inp = $($(this).closest('.homeListBottom').find('.description')).val();
        var urlId = $($(this).closest('.homeListBottom').find('.urlId')).val();
        var navType = $($(this).closest('.homeListBottom').find('.navType')).val();
        var jumptoweb = $($(this).closest('.homeListBottom').find('.jumptoweb')).val();

        $("#urlNameOuter").val('');
        if (span != null && span != '') {
            $("#urlId").val(urlId);
            $("#urlName").val(inp);
            $("#interval").val(inp);
            $("#description").val(span);
            // if (span == inp) {
            //     $("#description").val("");
            // } else {
            //     $("#description").val(span);
            // }
        } else {
            $("#interval").val("快速导航设置");
            $("#urlName").val("");
            $("#description").val("");
            $("#urlId").val("");
        }

        if (navType == '0' || navType == '') {
            $('#innerContainer').show();
            $('#outerContainer').hide();
            $('#siteInnerOuter1').prop('checked', true);
            $('#siteInnerOuter2').prop('checked', false);
            $('#descriptionDanger').hide();
        } else {
            $('#innerContainer').hide();
            $('#outerContainer').show();
            $("#urlNameOuter").val(jumptoweb);
            $('#siteInnerOuter1').prop('checked', false);
            $('#siteInnerOuter2').prop('checked', true);
            $('#descriptionDanger').show();
        }

        $('#fastNavType').val(navType);

        // 树结构
        var setting = {
            async: {
                url: "/clbs/c/role/resourceTree",
                type: "post",
                enable: true,
                autoParam: ["id"],
                contentType: "application/json",
                dataType: "json",
                dataFilter: ajaxDataFilter
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
                onClick: onClick,
                onCheck: onCheck,
                onAsyncSuccess: zTreeOnAsyncSuccess,
            }
        };
        $.fn.zTree.init($("#ztreeDemo"), setting, null);

        $("#order").val(num);
        $('#navEditModal').modal('show');
    })
});