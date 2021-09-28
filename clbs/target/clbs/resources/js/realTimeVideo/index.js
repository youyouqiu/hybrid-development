require(['tree', 'map', 'table', 'video'], function (Tree, Map, Table, VideoModule) {
    var main = {
        //页面右侧地图模块大小显示
        mapAllShowFn: function () {
            if ($(this).children().hasClass("fa fa-chevron-left")) {
                $(this).children().removeClass("fa fa-chevron-left");
                $(this).children().addClass("fa fa-chevron-right");
                $("#video-module").css({
                    'position': 'absolute',
                    'left': '-75%'
                });
                $("#map-module").css("float", "right");
                $("#map-module").removeClass("col-md-3");
                $("#map-module").addClass("col-md-12");
            } else {
                $(this).children().removeClass("fa fa-chevron-right");
                $(this).children().addClass("fa fa-chevron-left");
                $("#map-module").removeClass("col-md-12");
                $("#map-module").addClass("col-md-3");
                $("#video-module").css({
                    'position': 'absolute',
                    'left': 0
                });
            }
        },
        /**
         *  跳转到视频回放
         */
        jumpToResource: function () {
            var urls = $("#permissionUrls").val();
            if (urls !== null && urls !== undefined) {
                var urlList = urls.split(",");
                if (urlList.indexOf("/realTimeVideo/resource/list") > -1) {
                    return true;
                }
            }
            layer.msg("无操作权限，请联系管理员");
            e.preventDefault();
        },
    };

    $(function () {
        Tree.tree.setVideoModule(VideoModule);
        VideoModule.video.setTreeModule(Tree);
        VideoModule.video.setTableModule(Table);
        Table.table.setTreeModule(Tree);
        Map.map.init();
        Tree.tree.init();
        Table.table.init();
        VideoModule.video.init();
        $('#mapAllShow').on('click', main.mapAllShowFn); //页面右侧地图模块大小显示
        // 跳转至视频回放
        $("#jumpToResource").on("click", main.jumpToResource);
    });
});