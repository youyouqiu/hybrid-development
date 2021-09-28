/**
 *
 * @authors AXH
 * @date    2017-05-22 10:17:05
 * @update  2017-06-27 15:48:05
 * @version 1.1.4
 */
;
(function ($, window, document, undefined) {

    var InputClear = function (elements) {
        this.elements = elements;
        this.init();
    };

    InputClear.prototype = {
        init: function (options) {
            var that = this;
            that.elements.on('focus', function () {
                that.inputChange($(this), that);
            }).on('input propertychange', function () {
                var $element = $(this);
                that.inputChange($(this), that);
                $element.trigger("onChangeEvent", $element);
            }).on('blur', function () {
                var $this = $(this);
                $this.siblings('i.delIcon').remove();
            });
            /*IE浏览器去掉初始化del按钮*/
            setTimeout(function () {
                if ((navigator.userAgent.indexOf('MSIE') >= 0) && (navigator.userAgent.indexOf('Opera') < 0)) {
                    that.elements.next('i.delIcon').remove();
                }
            }, 20);
        },
        /*输入框内容监听事件*/
        inputChange: function ($element, that, options) {
            if ($element.attr('readonly') == undefined && (!$element.hasClass('layer-date')) && $element.attr('type') === 'text' && $element.attr('hidden') != 'hidden') {
                $element.next('i.delIcon').remove();
                var value = $element.val();
                var disabledDelIcon = $element.data('disabled-delicon');
                if (value.substr(0, 1).replace(/\s/g, '') == '' || value.substr(-1).replace(/\s/g, '') == '') {
                    if ($element.is("INPUT")) {
                        value = value.replace(/(^\s+)|(\s+$)/g, '');
                        $element.val(value);
                    }
                }

                if (value !== '' && !disabledDelIcon) {
                    var html = '<i class="delIcon"></i>';
                    $(html).insertAfter($element);
                    $('.delIcon').unbind('mousedown').bind('mousedown', function (e) {
                        e.stopPropagation();
                        that.delClick($(this), that);
                    });
                }
            }
        },
        /*删除按钮点击事件*/
        delClick: function ($element, that) {
            var curTagName = that.elements[0].tagName.toLowerCase();
            var inputObj = $($element.prevAll(curTagName)[0]);
            inputObj.val('').removeAttr('data-id');
            setTimeout(function () {
                inputObj.focus()
            }, 20);
            $element.remove();
            inputObj.siblings('.myHolder').show();
            inputObj.siblings('.input-group-btn').find('ul.dropdown-menu tr').removeClass('hover');
            inputObj.trigger('onClearEvent', inputObj);
        }
    };

    $.fn.inputClear = function (options) {
        var thisEvent = new InputClear(this, options);
        return this;
    }
})(jQuery, window, document);