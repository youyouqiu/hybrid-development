$(function () {
    window.multiWindow = {
        init: function () {
            var dependency = new Dependency();
            this.dependency = dependency;

            var data = new Data();
            this.data = data;
            dependency.set('data', data);

            // 树
            var tree = new Tree('treeDemo',null,dependency);
            dependency.set('tree', tree);

            // 小窗口
            var smallWindow = new SmallWindow('#multiWindowContainer',null,dependency);
            dependency.set('smallWindow', smallWindow);

            this.bindDataEvent();
            this.initLayout();

            // 初始化
            tree.init();
            tree.subscribeAllMonitorStatus();
            smallWindow.init();
        },

        bindDataEvent: function () {
            var data = this.data;
            var tree = this.dependency.get('tree');
            var smallWindow = this.dependency.get('smallWindow');

            // 树相关
            data.on('allCountChange', tree.onAllCountChange.bind(tree));
            data.on('onlineCountChange', tree.onOnlineCountChange.bind(tree));
            data.on('offlineCountChange', tree.onOffCountChange.bind(tree));
            data.on('treeTypeChange', tree.treeTypeChange.bind(tree));
            data.on('statusVidObjChange', tree.updateStatus.bind(tree));
            data.on('removeSubscribVid', tree.cancelSubscribeLocation.bind(tree));
            data.on('activeWindowIndexChange', tree.activeWindowIndexChange.bind(tree));

            // 小窗口相关
            data.on('windowCountChange', smallWindow.renderWindow.bind(smallWindow));
            data.on('addSubscribVid', smallWindow.addSubscribe.bind(smallWindow));
            data.on('removeSubscribVid', smallWindow.removeSubscribe.bind(smallWindow));
            data.on('updateSubscribVid', smallWindow.updateSubscribe.bind(smallWindow));
            data.on('visibleSensorKeyArrayChange', smallWindow.entireUpdateSubscribe.bind(smallWindow));
            data.on('mapTypeChange', smallWindow.onMapTypeChange.bind(smallWindow));
            data.on('showTrafficChange', smallWindow.onTrafficChange.bind(smallWindow));
            data.on('activeWindowIndexChange', smallWindow.activeWindowIndexChange.bind(smallWindow));
        },

        initLayout: function(){
            var tree = this.dependency.get('tree');
            var smallWindow = this.dependency.get('smallWindow');

            $('.tree-type-link').on('click', tree.setTreeType.bind(tree));
            $('.number-icon').on('click', smallWindow.changeWindowCount.bind(smallWindow));

            $('#multiWindowContainer').on('click', '.window', smallWindow.toggleFocus.bind(smallWindow));
            $('#multiWindowContainer').on('contextmenu', '.window', smallWindow.onRightClick.bind(smallWindow));
            $('#multiWindowContainer').on('click', '.cancel-subscribe-icon', smallWindow.removeSubscribeByButton.bind(smallWindow));
            $('#multiWindowContainer').on('click', '.status-arrow', smallWindow.toggleStatusBox.bind(smallWindow));
            $('#multiWindowContainer').on('click', '.full-screen-icon', smallWindow.toggleFullScreen.bind(smallWindow));
            $('#multiWindowContainer').on('click', '.track-icon', smallWindow.toggleTrack.bind(smallWindow));
            $('#multiWindowContainer').on('mousedown', '.move-icon', smallWindow.dragMouseDown.bind(smallWindow));
            $('#multiWindowContainer').on('click', '.locate-icon', smallWindow.relocateAndTrack.bind(smallWindow));
            $(document).on('mousemove', smallWindow.dragMouseMove.bind(smallWindow));
            $(document).on('mouseup mouseleave', smallWindow.dragMouseUp.bind(smallWindow));
            $('.map-icon').on('click',  smallWindow.toggleMapSettingVisible.bind(smallWindow));
            $('.map-setting-container input[name="map-type"]').on('change', smallWindow.onMapTypeClick.bind(smallWindow));
            $('.map-setting-container input[name="showTraffic"]').on('change', smallWindow.onShowTrafficClick.bind(smallWindow));
            $('#search_condition').inputClear().on('onClearEvent', tree.clearSearch.bind(tree));
            $("#search_condition").on('input propertychange', tree.searchChange.bind(tree));
            $('#searchType').on('change',function () {
                if($("#search_condition").val()!==''){
                    tree.searchTree.bind(tree)();
                }
            });
            // $("#thetree").scroll(tree.onScroll.bind(tree));
            $('#refresh').on('click', tree.refreshTree.bind(tree));
        },


    };

    multiWindow.init();
})