var SmallWindow = function (selector, options, dependency) {
    this.dependency = dependency;
    this.$selector = $(selector);

};

SmallWindow.prototype.init = function () {
    var dataDependency = this.dependency.get('data');
    dataDependency.setWindowCount(4);
};

SmallWindow.prototype.renderWindow = function () {
    var dataDependency = this.dependency.get('data');

    var windowCount = dataDependency.getWindowCount();
    var windows = this.$selector.find('.window');
    var windowsLength = windows.length;

    windows.each(function (idx, ele) {
        var className = ele.className;
        var updateClass = 'window videoWindow-' + windowCount.toString();
        if (className.indexOf('disabled') > -1) {
            updateClass += ' disabled';
        }
        ele.className = updateClass;
    });

    if (windowsLength >= windowCount) {
        for (var i = windowsLength - 1; i >= windowCount; i--) {
            $(windows[i]).remove();
        }
    } else {
        for (var j = windowsLength; j < windowCount; j++) {
            var id = 'amapContainer-' + Math.random().toString();
            var $wrapper = $('<div class="disabled window videoWindow-' + windowCount.toString() + '"></div>');
            var $default = $($('#windowDefaultTmpl').html());
            $default.find('.video-bag').attr('id', id);
            $default.find('.video-bag video').attr('id', 'videoSource_' + j);
            $wrapper.append($default);
            this.$selector.append($wrapper);
        }
    }

    // 左下角数字高亮
    var $target = $('#window-' + windowCount + '');
    if ($target.hasClass('active')) {
        return;
    }
    $target.siblings().removeClass('active');
    $target.addClass('active');
};

SmallWindow.prototype.changeWindowCount = function (event) {
    var dataDependency = this.dependency.get('data');

    var $target = $(event.currentTarget);
    var type = $target.data('type');

    dataDependency.setWindowCount(parseInt(type));
};

SmallWindow.prototype.onRightClick = function (event) {
    event.preventDefault();
    event.stopPropagation();
};
