//数据超出滚动翻页
(function (window, $) {
    jQuery.fn.divSlide = function (options) {
        var wrap = $(this);
        var page = 0,
            wrapSize = 0,//容器大小
            dis,
            conSize = 0;//内容大小

        var defaults = {
            type: 'vertical', //(竖直滑动 || 横向滑动)
            content: $('.content', wrap),//内容容器
            arrow: $('.arrow-box .arrow')//分页按钮
        };
        var settings = $.extend({}, defaults, options);

        if (settings.type == 'vertical') {
            wrapSize = wrap.height();
            conSize = settings
                .content
                .height();
        }else if(settings.type == 'horizontal'){
            wrapSize = wrap.width();
            conSize = settings
                    .content
                    .width();
        }

        var rate = Math.ceil(conSize / wrapSize) - 1;// 页数

        var methods = {
            /**
             * 初始化 arrow 按钮
             */
            init: function () {
                if (rate == 0) {
                    settings
                        .arrow
                        .addClass('disabled');
                } else if (rate > 0) {
                    settings
                        .arrow
                        .eq(0)
                        .addClass('disabled');
                }
            },
            /**
             * slide滑动
             */
            slide: function () {
                var inx = $(this).index();
                var intFlag = conSize % wrapSize; // 判断是否刚好为整数页

                if (inx == 0) {
                    page > 0
                        ? page--
                        : page = 0;
                }

                if (inx == 1) {
                    page < rate
                        ? page++
                        : page = rate;
                }

                dis = page * wrapSize;
                if (page > 0 || page < rate) {
                    settings
                        .arrow
                        .removeClass('disabled');

                    if(intFlag != 0 && page == rate){
                        dis = (page-1) * wrapSize + intFlag;
                    }

                    methods._cssObj(settings.content, dis);
                }

                if (page == 0) {
                    settings
                        .arrow
                        .eq(0)
                        .addClass('disabled');
                } else if (page == rate) {
                    settings
                        .arrow
                        .eq(1)
                        .addClass('disabled');
                }
            },
            /**
            * css3 控制容器滑动翻页
            * dom : 滑动容器
            * dis : 滑动的距离
            */
            _cssObj: function (dom, dis) {
                if(settings.type == 'vertical'){
                    dom.css({
                        '-webkit-transform': 'translateY(-' + dis + 'px)',
                        'transform': 'translateY(-' + dis + 'px)',
                        '-webkit-transition': 'translateY(-' + dis + 'px)',
                        'transition': 'transform .3s ease-in-out'
                    });
                }else{
                    dom.css({
                        '-webkit-transform': 'translateX(-' + dis + 'px)',
                        'transform': 'translateX(-' + dis + 'px)',
                        '-webkit-transition': 'translateX(-' + dis + 'px)',
                        'transition': 'transform .3s ease-in-out'
                    });
                }

            }
        }

        $(function () {
            /**
             * 初始化
             */
            methods.init();
            /**
            * 翻页按钮点击事件
            */
            settings
                .arrow
                .on('click', methods.slide);
        })

    }
})(window, $)