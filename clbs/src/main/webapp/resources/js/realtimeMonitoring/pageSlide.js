//数据超出滚动翻页
;(function (window, $) {
    jQuery.fn.divSlide = function (options) {
        var wrap = $(this);
        var page = 0,
            wrapSize = 0,//容器大小
            dis = 0,
            conSize = 0;//内容大小

        var defaults = {
            type: 'Horizontal', //Horizontal:横向滑动，竖直滑动:vertical
            content: $('.lkyw-filter-lists', wrap),//内容容器
            arrow: $('.lkyw-arrow')//分页按钮
        };
        var settings = $.extend({}, defaults, options);

        wrapSize = settings.type == 'vertical' ? wrap.height() : wrap.width();
        conSize = settings.type == 'vertical' ? settings.content.height() : settings.content.width();
        var rate = (conSize / wrapSize) - 1;

        var methods = {
            /**
             * 初始化 arrow 按钮
             */
            init: function () {
                if (rate == 0) {
                    settings
                        .arrow
                        .addClass('hide');
                } else if (rate > 0) {
                    settings
                        .arrow
                        .eq(0)
                        .addClass('hide');
                }
            },
            /**
             * slide滑动
             */
            slide: function () {
                var arrow = $(this).data('value');
                // console.log('分页', rate);

                if (arrow == 'left') {
                    page > 0
                        ? page--
                        : page = 0;
                }

                if (arrow == 'right') {
                    page < rate
                        ? page++
                        : page = rate;
                }

                dis = page * wrapSize;
                if (page > 0 || page < rate) {
                    /*settings
                        .arrow
                        .removeClass('hide');*/
                    methods._cssObj(settings.content, dis);
                }

                /*if (page == 0) {
                    settings
                        .arrow
                        .eq(0)
                        .addClass('hide');
                } else if (page == rate) {
                    settings
                        .arrow
                        .eq(1)
                        .addClass('hide');
                }*/
            },
            /**
             * css3 控制容器滑动翻页
             * dom : 滑动容器
             * dis : 滑动的距离
             */
            _cssObj: function (dom, dis) {
                if(settings.type == 'Horizontal'){
                    dom.css({
                        '-webkit-transform': 'translateX(-' + dis + 'px)',
                        'transform': 'translateX(-' + dis + 'px)',
                        '-webkit-transition': 'translateX(-' + dis + 'px)',
                        'transition': 'transform .3s'
                    });
                }else if(settings.type == 'vertical'){
                    dom.css({
                        '-webkit-transform': 'translateY(-' + dis + 'px)',
                        'transform': 'translateY(-' + dis + 'px)',
                        '-webkit-transition': 'translateY(-' + dis + 'px)',
                        'transition': 'transform .3s'
                    });
                }
            }
        }

        $(function () {
            /**
             * 初始化
             */
            // methods.init();
            /**
             * 翻页按钮点击事件
             */
            settings
                .arrow
                .on('click', methods.slide);
        })

    }
})(window, $)