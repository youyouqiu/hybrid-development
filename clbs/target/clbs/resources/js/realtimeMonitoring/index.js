$(function () {
    window.realtimeMonitoring = {
        dragBar: undefined,
        init: function () {
            var dependency = new Dependency(false);

            //列表拖动实例
            realtimeMonitoring.dragBar = new DragBar('#tableMapDrag', {
                min: -500,
                max: 0,
                onDragEnd: realtimeMonitoring.onDragBarDragEnd
            });
            dependency.set('dragBar', realtimeMonitoring.dragBar);

            //地图实例
            realtimeMonitoring.map = new Map('mapContainer', null, dependency);
            dependency.set('map', realtimeMonitoring.map);

            /*事件绑定*/
            $('#lkywFilter').divSlide();
            $("#toolClick").on("click", realtimeMonitoring.toolClick);
        },
        /**
         * 列表拖动
         * @param delta
         */
        onDragBarDragEnd: function (delta) {
            var $topPart = $('#lkywTop');
            var $bottomPart = $('#lkywBottom');
            var bottom = parseFloat($topPart.css('bottom'));
            $topPart.css('bottom', (100-delta) + 'px');
            $bottomPart.css('height', 50-delta + 'px');
        },
        /**
         * 地图工具图标按钮
         */
        toolClick: function () {
            var $toolOperateClick = $("#toolOperateClick");
            if ($toolOperateClick.css("margin-right") == "-800px") {
                $toolOperateClick.animate({marginRight: "7px"});
            } else {
                $("#disSetMenu,#mapDropSettingMenu,#measurementMenu").hide();
                $('#measurementMenu').hide();
                $toolOperateClick.animate({marginRight: "-800px"});
                $("#toolOperateClick i").removeClass('active');
                $("#toolOperateClick span").css('color', '#5c5e62');
            };
        },
    };

    realtimeMonitoring.init();
})