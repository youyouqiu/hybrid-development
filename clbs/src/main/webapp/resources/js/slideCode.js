/**
 * slideCode
 */
var JPlaceHolder;
(function ($, window, document, undefined) {
    function SliderUnlock(elm, options, success) {
        var me = this;
        var $elm = me.checkElm(elm) ? $(elm) : $;
        var opts = {
            labelTip: "",
            successLabelTip: "",
            duration: 500,
            swipeStart: false,
            min: 0,
            max: $elm.width(),
            index: 0,
            IsOk: false,
            labelIndex: 0
        };
        var suc = me.checkFn(success) ? success : function () {
        };
        opts = $.extend(opts, options || {});
        //$elm
        me.elm = $elm;
        //opts
        me.opts = opts;
        //是否开始滑动
        me.swipeStart = opts.swipeStart;
        //最小值
        me.min = opts.min;
        //最大值
        me.max = opts.max;
        //当前滑动条所处的位置
        me.index = opts.index;
        //是否滑动成功
        me.isOk = opts.isOk;
        //滑块宽度
        me.labelWidth = me.elm.find("#label").width();
        //滑块背景
        me.sliderBg = me.elm.find("#slider_bg");
        //鼠标在滑动按钮的位置
        me.labelIndex = opts.labelIndex;
        //success
        me.success = suc;
    }

    /**
     * 初始化 绑定事件
     */
    SliderUnlock.prototype.init = function () {
        var me = this;
        var loginSuccess = false;
        var ajaxSuccess = true;
        me.updateView();
        me.elm.find("#label").on("mousedown", function (event) {
            var dragWidth = $("#slider").width() - 40;
            var e = event || window.event;
            var $slider_bg = $("#slider_bg");
            var $label = $("#label");
            me.labelIndex = e.clientX - this.offsetLeft;
            $(document).on("mousemove", function (e) {
                var passLogin = e.clientX - me.labelIndex;
                if (passLogin < 0) {
                    $slider_bg.css("width", "0");
                    $label.css("left", "0");
                } else if (passLogin >= dragWidth) {
                    $slider_bg.css("width", dragWidth + "px");
                    $label.css("left", dragWidth + "px");
                    loginSuccess = true;
                    if (ajaxSuccess) {
                        me.success();
                        ajaxSuccess = false;
                    }
                    $label.unbind("mousedown");
                } else {
                    $slider_bg.css("width", passLogin + "px");
                    $label.css("left", passLogin + "px");
                }
            }).bind("mouseup", function () {
                if (loginSuccess) {
                    $slider_bg.css("width", dragWidth + "px");
                    $label.css("left", dragWidth + "px");
                } else {
                    me.handerOut();
                }
                $(document).unbind("mousemove").unbind("mouseup");
            });
        }).on("touchstart", function (event) {
            var e = event || window.event;
            me.labelIndex = e.originalEvent.touches[0].pageX - this.offsetLeft;
            me.handerIn();
        }).on("touchmove", function (event) {
            me.handerMove(event, "mobile");
        }).on("touchend", function (event) {
            me.handerOut();
        });
    };

    /**
     * 鼠标移出
     */
    function mouseUp() {
        $(document).unbind("mousemove", mouseMove).unbind("mouseup", mouseUp);
    }

    /**
     * 鼠标/手指接触滑动按钮
     */
    SliderUnlock.prototype.handerIn = function () {
        var me = this;
        me.swipeStart = true;
        me.min = 0;
        me.max = me.elm.width();
    };
    /**
     * 鼠标/手指移出
     */
    SliderUnlock.prototype.handerOut = function () {
        var me = this;
        //停止
        me.swipeStart = false;
        if (me.index < me.max) {
            me.reset();
            //滑块未到验证点时
            $("#label").css("border-left", "none");
        }
    };
    /**
     * 鼠标/手指移动
     */
    SliderUnlock.prototype.handerMove = function (e, type) {
        var me = this;
        var event = e;
        if (me.swipeStart) {
            //阻止默认事件
            event.preventDefault();
            event = event || window.event;
            //设备类型为移动时
            if (type === "mobile") {
                me.index = event.originalEvent.touches[0].pageX - me.labelIndex;
            } else {
                me.index = event.clientX - me.labelIndex;
            }
            me.move();
        }
    };
    /**
     * 鼠标/手指移动过程
     */
    SliderUnlock.prototype.move = function () {
        var me = this;
        $("#label").css("border-left", "1px solid #cfcfcf");
        if ((me.index + me.labelWidth) >= me.max) {
            me.index = me.max - me.labelWidth - 2;
            //停止
            me.swipeStart = false;
            //解锁
            me.isOk = true;
        }
        if (me.index < 0) {
            me.index = me.min;
            //未解锁
            me.isOk = false;
        }
        if (me.index + me.labelWidth + 2 === me.max && me.max > 0 && me.isOk) {
            //解锁默认操作
            $("#label").unbind().next("#labelTip").text(me.opts.successLabelTip).css("color", "#000");
            me.success();
        }
        me.backgroundTranslate();
        me.updateView();
    };

    // 颜色渐变
    SliderUnlock.prototype.backgroundTranslate = function () {
        var _self = this;
        _self.elm.find(".slide-code-label").css("left", _self.index + "px")
            .next('.slide-code-labeltip').css("opacity", 1-(parseInt($(".slide-code-label").css("left"), 10)/_self.max));
    };

    /**
     * 还原默认状态
     */
    SliderUnlock.prototype.updateView = function () {
        var me = this;
        me.sliderBg.css("width", me.index);
        me.elm.find("#label").css("left", me.index + "px");
    };
    /**
     * 动画还原
     */
    SliderUnlock.prototype.reset = function () {
        var me = this;
        me.index = 0;
        me.sliderBg.animate({"width": 0}, me.opts.duration);
        me.elm.find("#label").animate({"left": me.index}, me.opts.duration)
            .next("#lableTip").animate({"opacity": 1}, me.opts.duration);
        me.updateView();
    };
    /**
     * 检测元素是否存在
     */
    SliderUnlock.prototype.checkElm = function (elm) {
        if ($(elm).length > 0) {
            return true;
        } 
        throw new Error("元素异常");
        
    };
    /**
     * 检测传入参数是否是function
     */
    SliderUnlock.prototype.checkFn = function (fn) {
        if (typeof fn === "function") {
            return true;
        } 
        throw new Error("参数异常");
        
    };
    window.SliderUnlock = SliderUnlock;
}(jQuery, window, document));

/**
 * 修复IE模式下html5-placeholder属性兼容问题
 */
JPlaceHolder = {
    //检测
    _check: function () {
        return 'placeholder' in document.createElement('input');
    },
    //初始化
    init: function () {
        if (!this._check()) {
            this.fix();
        }
    },
    //修复
    fix: function () {
        jQuery(':input[placeholder]').each(function (index, element) {
            var self = $(this), txt = self.attr('placeholder');
            self.wrap($('<div></div>').css({
                position: 'relative',
                zoom: '1',
                border: 'none',
                background: 'none',
                padding: 'none',
                margin: 'none'
            }));
            var pos = self.position(), h = self.outerHeight(true), paddingleft = self.css('padding-left');
            holder = $('<span></span>').text(txt).css({
                position: 'absolute',
                left: pos.left,
                top: (pos.top + 10),
                height: h,
                lienHeight: h,
                paddingLeft: paddingleft,
                color: '#aaa',
                fontFamily: '微软雅黑'
            }).appendTo(self.parent());
            self.focusin(function (e) {
                holder.hide();
            }).focusout(function (e) {
                if (!self.val()) {
                    holder.show();
                }
            });
            holder.click(function (e) {
                holder.hide();
                self.focus();
            });
        });
    },
    reset: function() {
        if (JPlaceHolder.slider) {
            JPlaceHolder.slider.init();
            $("#label").removeAttr("style").css({"left": "0px;"});
            $("#labelTip").text("把我滑到右边试试?").removeAttr("style");
            $("#captchaCode").removeAttr('value');
        }
    },
    encryptPassword: function(password) {
        var key, srcs, encrypted;
        var enableDecryption = $("#enableDecryption").val();
        if (enableDecryption !== "true") {
            return password;
        }
        key = CryptoJS.enc.Utf8.parse($("#key").val());
        srcs = CryptoJS.enc.Utf8.parse(password);
        encrypted = CryptoJS.AES.encrypt(srcs, key, {mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.Pkcs7});
        return encrypted.toString();
    },
    loginSubmit: function () {
        var $form = $(this);
        if ($form.data('submitted') === true) {
            return false;
        }
        var failureInfo = $("#failureInfo");
        var passwordObj = $("#tg_password");
        var password = JPlaceHolder.encryptPassword(passwordObj.val());
        if(password ==null || password ===''){
            failureInfo.html('<div class="login_msg"><font color="red"><span>密码不能为空</span></font></div>');
            return false
        }
        $("#pwdSubmit").val(password);
        $form.data('submitted', true);
        $("#loginForm").ajaxSubmit(function (data) {
            if (data.loginFailure) {
                //登录失败，先清空失败信息
                JPlaceHolder.reset();
                failureInfo.html("");
                //组装失败信息
                var errorInfo = "";
                if ($('body').attr("id") === "loginPublish") {
                    errorInfo = '<div class="login_msg"><font color="red"><span>' + data.msg + '</span></font></div>';
                } else {
                    errorInfo = '<p class="error-info"><span>' + data.msg + '</span></p>';
                }
                failureInfo.html(errorInfo);
                $form.data('submitted', false);
            } else {
                window.location.href = "/clbs/";
            }
        });
        return false;

    }
};
//执行
jQuery(function () {
    var slider = new SliderUnlock("#slider", {
        //拖动滑块成功时显示
        //successLabelTip : "恭喜！验证成功！"
    }, function () {
        $.ajax({
            type: "GET",
            url: "/clbs/getCaptchaString?date=" + new Date().getTime(),
            dataType: "text",
            success: function (data) {
                if (data != "") {
                    $("#captchaCode").val(data);
                    $("#label").css({
                        "background-image": "url(/clbs/resources/img/codes.png)",
                        "border-left": "1px solid #cfcfcf",
                        "border-right": "none"
                    });
                    $("#labelTip").text("您成功啦！请登录！").css("color", "#000000");
                    $(document).unbind("mousemove").unbind("mouseup");
                }
            }
        });
    });
    slider.init();
    JPlaceHolder.init();
    JPlaceHolder.slider = slider;
});
/**
 * 处理IE6-9版本文字不选中
 */
document.body.onselectstart = document.body.ondrag = function () {
    return false;
};