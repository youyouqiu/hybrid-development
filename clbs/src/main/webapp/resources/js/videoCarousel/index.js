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

            // 视频操作
            var videoHandle = new VideoHandle(null, dependency);
            dependency.set('videoHandle', videoHandle);

            // 小窗口
            var smallWindow = new SmallWindow('#multiWindowContainer',null,dependency);
            dependency.set('smallWindow', smallWindow);

            this.bindDataEvent();
            this.initLayout();

            // 初始化
            tree.init();
            // 订阅监控对象状态
            tree.subscribeAllMonitorStatus();
            videoHandle.init();
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

            // 小窗口相关
            data.on('windowCountChange', smallWindow.renderWindow.bind(smallWindow));
            layer.msg('请选择监控对象',{time: -1,offset:['52%','54%']});
        },

        initLayout: function(){
            var tree = this.dependency.get('tree');
            var smallWindow = this.dependency.get('smallWindow');

            $('.tree-type-link').on('click', tree.setTreeType.bind(tree));
            $('.number-icon').on('click', smallWindow.changeWindowCount.bind(smallWindow));
            $('#multiWindowContainer').on('contextmenu', '.window', smallWindow.onRightClick.bind(smallWindow));

            $('#search_condition').inputClear().on('onClearEvent', tree.clearSearch.bind(tree));
            $("#search_condition").on('input propertychange', tree.searchChange.bind(tree));
            // $("#thetree").scroll(tree.onScroll.bind(tree));
            $('#refresh').on('click', tree.refreshTree.bind(tree));
            $('#searchType').on('change',function () {
                if($("#search_condition").val()!==''){
                    tree.searchTree.bind(tree)();
                }
            });
        },
    };
    multiWindow.init();
});