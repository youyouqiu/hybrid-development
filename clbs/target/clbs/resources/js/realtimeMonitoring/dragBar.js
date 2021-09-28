var DragBar = function (selector,options) {
    this.options = options;
    this.$origin = $(selector);
    this.isDragging = false;
    this.downY = 0;
    this.downX = 0;
    this.downTop = 0;
    this.moveHandler = null;
    this.$indicator = null;
    this.delta = 0;
    this.$origin.on('mousedown',DragBar.prototype.mousedownHandler.bind(this));
    $('body').on('mouseup', DragBar.prototype.mouseupHandler.bind(this))
}
DragBar.prototype.mousedownHandler = function (event) {
    event.preventDefault();
    this.isDragging = true;
    this.downX = event.clientX;
    this.downY = event.clientY;
    this.$indicator = $('<div class="indicator"></div>');
    this.$origin.append(this.$indicator);
    var top = parseFloat(this.$indicator.css('top'));
    this.downTop = isNaN(top) ? 0 : top;
    this.moveHandler = DragBar.prototype.mousemoveHandler.bind(this);
    this.$indicator.css('position','absolute');
    $('body').on('mousemove', this.moveHandler);
}

DragBar.prototype.mousemoveHandler = function (event) {
    event.preventDefault();
    if (this.isDragging){
        var deltaY = event.clientY - this.downY;
        var delta = this.delta + deltaY;
        if (this.options && this.options.min !== undefined && delta < this.options.min){
            return;
        }
        if (this.options && this.options.max !== undefined && delta > this.options.max){
            return;
        }
        this.$indicator.css('top',(this.downTop+deltaY)+'px');
    }
}

DragBar.prototype.mouseupHandler = function (event) {
    if (this.isDragging){
        var deltaY = event.clientY - this.downY;
        this.delta += deltaY;
        if (this.options && this.options.min !== undefined && this.delta < this.options.min){
            this.delta = this.options.min;
        }
        if (this.options && this.options.max !== undefined && this.delta > this.options.max){
            this.delta = this.options.max;
        }
        this.isDragging = false;
        this.$origin.empty();
        $('body').off('mousemove', this.moveHandler);
        if (this.options && this.options.onDragEnd){
            this.options.onDragEnd(this.delta);
        }
    }
}

DragBar.prototype.setDelta = function (delta) {
    if (this.options && this.options.min !== undefined && delta >= this.options.min) {
        if (this.options && this.options.max !== undefined && delta <= this.options.max){
            this.delta = delta;
            if (this.options && this.options.onDragEnd){
                this.options.onDragEnd(this.delta);
            }
        }
    }
}