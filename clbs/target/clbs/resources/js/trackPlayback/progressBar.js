var ProgressBar = function (selector,options) {
    this.options = $.extend({},{
        min:0,
        max:0,
        value:0,
        onDrag:null,
        onDragEnd:null,
    },options);
    this.$origin = $(selector);
    this.$back = this.$origin.find('.progress-back');
    this.$indicator = this.$origin.find('.progress-indicator');
    this.width = this.$back.width();
    this.indicatorHalfWidth = this.$indicator.width() / 2;
    this.isDragging = false;
    this.downX = 0;
    this.downLeft = 0 - this.indicatorHalfWidth;
    this.moveHandler = null;
    this.delta = 0;
    this.value = this.options.value;
    this.$indicator.on('mousedown',ProgressBar.prototype.mousedownHandler.bind(this));
    this.$back.on('click',ProgressBar.prototype.clickHandler.bind(this));
    $('body').on('mouseup', ProgressBar.prototype.mouseupHandler.bind(this))
}

ProgressBar.prototype.setOptions = function(options){
    var same = this.options.value === options.value
        && this.options.max === options.max
        && this.options.min === options.min;
    this.options = $.extend({},this.options,options);
    this.value = this.options.value;
    var delta = parseFloat((this.value * this.width ) / (this.options.max - this.options.min));
    var realPosition = delta - this.indicatorHalfWidth;
    if (!same){
        this.$indicator.css('left',(realPosition)+'px');
    }
}

ProgressBar.prototype.clickHandler = function(event){
    event.preventDefault();
    if (this.options && this.options.max <= this.options.min){
        return;
    }
    var targetOffset = $(event.currentTarget).offset();
    var clientX = event.clientX;

    var downX = clientX - targetOffset.left;
    if (downX < 0){
        return;
    }
    if (downX > this.width){
        return;
    }
    var realPosition = downX - this.indicatorHalfWidth;
    this.$indicator.css('left',(realPosition)+'px');
    this.value = parseInt((downX / this.width ) * (this.options.max - this.options.min));

    if (this.options && this.options.onDragEnd){
        this.options.onDragEnd(this.value);
    }
}

ProgressBar.prototype.mousedownHandler = function (event) {
    event.preventDefault();
    if (this.options && this.options.max <= this.options.min){
        return;
    }
    this.isDragging = true;
    this.downX = event.clientX;
    var left = parseFloat(this.$indicator.css('left'));
    this.downLeft = isNaN(left) ? 0 : left;
    this.moveHandler = ProgressBar.prototype.mousemoveHandler.bind(this);
    $('body').on('mousemove', this.moveHandler);
}

ProgressBar.prototype.mousemoveHandler = function (event) {
    event.preventDefault();
    if (this.options && this.options.max <= this.options.min){
        return;
    }
    if (this.isDragging){
        var deltaX = event.clientX - this.downX;
        var delta = this.downLeft + deltaX;
        if (delta < 0 - this.indicatorHalfWidth){
            delta = 0 - this.indicatorHalfWidth;
        }
        if (delta > this.width - this.indicatorHalfWidth){
            delta = this.width - this.indicatorHalfWidth;
        }

        this.$indicator.css('left',(delta)+'px');
        var realPosition = delta + this.indicatorHalfWidth;
        this.value = parseInt((realPosition / this.width ) * (this.options.max - this.options.min));

        if (this.options && this.options.onDrag){
            this.options.onDrag(this.value);
        }
    }
}

ProgressBar.prototype.mouseupHandler = function (event) {
    if (this.options && this.options.max <= this.options.min){
        return;
    }
    if (this.isDragging){
        $('body').off('mousemove', this.moveHandler);
        var deltaX = event.clientX - this.downX;
        var delta = this.downLeft + deltaX;
        if (delta < 0 - this.indicatorHalfWidth){
            delta = 0 - this.indicatorHalfWidth;
        }
        if (delta > this.width - this.indicatorHalfWidth){
            delta = this.width - this.indicatorHalfWidth;
        }
        this.isDragging = false;
        var realPosition = delta + this.indicatorHalfWidth;
        this.value = parseInt((realPosition / this.width ) * (this.options.max - this.options.min));
        if (this.options && this.options.onDragEnd){
            this.options.onDragEnd(this.value);
        }
    }
}