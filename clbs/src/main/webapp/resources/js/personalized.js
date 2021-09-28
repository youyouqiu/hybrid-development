$(function () {
    var userGroupId = $("#userGroupId").val();
    var data = {
        "uuid": userGroupId
    };
    var url = "/clbs/m/intercomplatform/personalized/find";
    json_ajax("POST", url, "json", false, data, function (data) {
        if (data.success == true) {
            var list = data.obj.list;
            var topTitleMsg = list.topTitle;
            var storage = window.localStorage;
            storage.title = topTitleMsg;
            $("#personalizedTitle").html(html2Escape(topTitleMsg));

            var copyright = list.copyright;
            var websiteName = list.websiteName;
            var recordNumber = list.recordNumber;
            $("#copyRight").html(html2Escape(copyright));
            $("#website").html(html2Escape(websiteName));
            $("#website").attr("href", "http://" + websiteName);
            $("#record").html(html2Escape(recordNumber));
            var homeLogo = "/clbs/resources/img/logo/" + list.homeLogo;
            $(".brand").attr("style", "background:url(" + homeLogo + ") no-repeat 0px 0px !important;");

            // 设置视频播放窗口背景图
            if (list.videoBackground) {
                var videoBg = "/clbs/resources/img/logo/" + list.videoBackground;
                // var videoBg = "/clbs/resources/img/logo/video_logo2.png";
                var style = document.createElement('style');
                style.type = 'text/css';
                style.innerHTML = 'video{ background-image:url("' + videoBg + '")!important; background-size: contain; }';
                document.getElementsByTagName('head').item(0).appendChild(style);
            } else {
                $("video").attr("style", "background-image:url('/clbs/resources/img/realTimeVideo/video_logo2.png')!important;");
            }

            var webIco = list.webIco;
            $("#icoLink").attr("href", "/clbs/resources/img/logo/" + webIco + "");

            var storage = window.localStorage;
            storage.homeLogo = homeLogo;
            storage.videoBg = videoBg;
        }
    });
    /**
     * 判断两个节点之间的节点是否存在一个节点包含指定的属性  包含起始节点
     * @param attrName 属性名
     * @param startNode dom节点
     * @param endNode 遍历结束节点
     */
    var hasAttrBetween = function(attrName, startNode, endNode){
        endNode = endNode || document.body
        if(startNode.hasAttribute(attrName)){
            return true
        }else if(startNode.parentNode != endNode){
            return hasAttrBetween(attrName, startNode.parentNode, endNode)
        }
        return endNode.hasAttribute(attrName)
    }
    $(".panel-heading").bind("click", function (e) {
        if(hasAttrBetween('noTrigger',e.target)){
            return
        }
        var id = $(this).context.id;
        if ($("#" + id + "-body").is(":hidden")) {
            $("#" + id + "-body").slideDown();
            setTimeout(function () { // 显示表格页码(页码被绝对定位,不做此设置位置会显示异常)
                $("#" + id + "-body").find('.dataTables_length').show();
                $("#" + id + "-body").find('.dataTables_info').show();
                $("#" + id + "-body").find('.dataTables_paginate.paging_simple_numbers').show();
            }, 300);
            //$("#"+id+"-body").css("display","block");
            $("#" + id + "-chevron").removeClass("chevron-up").addClass("chevron-down");
        } else { // 隐藏表格页码(页码被绝对定位,不做此设置位置会显示异常)
            $("#" + id + "-body").find('.dataTables_length').hide();
            $("#" + id + "-body").find('.dataTables_info').hide();
            $("#" + id + "-body").find('.dataTables_paginate.paging_simple_numbers').hide();
            $("#" + id + "-body").slideUp();
            //$("#"+id+"-body").css("display","none");
            $("#" + id + "-chevron").removeClass("chevron-down").addClass("chevron-up");
        }
    });
});