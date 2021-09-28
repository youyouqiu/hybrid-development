;
(function ($) {
    var DropDown = (function () {
        function DropDown($this, optionsParam) {
            this.containerClass = 'i-dropdown-container';
            this.inputClass = 'i-input-field';
            this.arrowClass = 'i-arrow';
            this.listContainerClass = 'i-list-container';
            this.listWraperClass = 'i-list-wraper';
            this.listClass = 'i-list';
            this.listItemClass = 'i-list-item';
            var defaults = {
                containerTmpl: '<div class="i-dropdown-container"></div>',
                inputTmpl: '<input type="text" class="i-input-field"/>',
                arrowTmpl: "<span class=\"" + this.arrowClass + "\"></span>",
                listContainerTmpl: '<div class="i-list-container"></div>',
                listWraperTmpl: '<div class="i-list-wraper"></div>',
                listTmpl: '<div class="i-list"></div>',
                listItemTmpl: '<div class="i-list-item"></div>',
                pageCount: 50,
                thresholdPercent: 0.2,
                searchDelay: 300,
                listItemHeight: 30,
            };
            var options = $.extend(defaults, optionsParam);
            this.$this = $this;
            this.options = options;
            this.state = {
                isOpen: false,
                isFocus: false,
                isChange: true,
                width: null,
                height: null,
                selectedValue: options.selectedValue,
                inputString: '',
                lastScrollTop: 0,
                indexSection: [0, options.pageCount],
                threshold: Math.ceil(options.pageCount * options.thresholdPercent),
                data: this.buildStateData(options.data, undefined),
                listItemHeight: options.listItemHeight || 30,
                searchDelayTimeoutId: null,
                searchUrl: options.searchUrl,
            };
            this.initHtml();
            if (this.options.onDataRequestSuccess) {
                this.state.$dom.$arrow
                    .removeClass('disabled loading-state-button')
                    .find('.loading-state')
                    .attr("class", 'caret');
                this.options.onDataRequestSuccess();
            }
        }

        DropDown.prototype.initHtml = function () {
            var $this = this.$this;
            var $container = $this;
            var $input = $this.find("." + this.inputClass);
            var $arrow = $this.find("." + this.arrowClass + " button");
            var $listContainer = $this.find("." + this.listContainerClass);
            var $listWraper = $this.find("." + this.listWraperClass);
            var $list = $this.find("." + this.listClass);
            this.state.$dom = {
                $container: $container,
                $input: $input,
                $arrow: $arrow,
                $listContainer: $listContainer,
                $listWraper: $listWraper,
                $list: $list
            };
            $listWraper.css('height', this.state.data.length * this.state.listItemHeight);
            $listContainer.hide();
            if (this.state.selectedValue !== undefined && this.state.selectedValue !== null) {
                this.setCurrentItem(this.state.selectedValue);
            }
            this.state.arrowClickHandler = this.toggleOpen.bind(this);
            this.state.documentClickHandler = this.documentClick.bind(this);
            this.state.listScrollHandler = this.listScroll.bind(this);
            this.state.inputChangeHandler = this.inputChange.bind(this);
            this.state.inputClickHandler = this.searchOpen.bind(this);
            this.state.listItemClickHandler = this.clickListItem.bind(this);
            $list.on('click', '.i-list-item', this.state.listItemClickHandler);
            $arrow.on('click', this.state.arrowClickHandler);
            $(document).on('click', this.state.documentClickHandler);
            $listContainer.on('scroll', this.state.listScrollHandler);
            $input.on('input', this.state.inputChangeHandler);
            $input.on('click', this.state.inputClickHandler);
        };
        DropDown.prototype.toggleOpen = function (e) {
            e.stopPropagation();
            var state = this.state;
            if (!state.isOpen) {
                this.state.data = this.buildStateData(this.options.data, undefined);
                this.open();
                if (this.state.isChange) {
                    this.resetScrollAndTransform(true);
                    this.buildListItems();
                }
            }
            else {
                this.close();
            }
        };
        DropDown.prototype.searchOpen = function (e) {
            e.stopPropagation();
            this.state.data = this.buildStateData(this.options.data, this.state.inputString);
            this.open();
            this.resetScrollAndTransform();
            this.buildListItems();
        };
        DropDown.prototype.open = function () {
            this.state.$dom.$listContainer.show();
            this.state.isOpen = true;
        };
        DropDown.prototype.close = function () {
            this.state.$dom.$listContainer.hide();
            this.state.isOpen = false;
        };
        DropDown.prototype.buildListItems = function () {
            var data = this.state.data.slice(this.state.indexSection[0], this.state.indexSection[1]);
            this.state.$dom.$list.empty();
            for (var i = 0, len = data.length; i < len; i++) {
                var element = data[i];
                var $listItem = $($.parseHTML(this.options.listItemTmpl));
                $listItem.html(element.name).data('id', element.id).data('index', i).css({
                    height: this.state.listItemHeight
                });
                if (element.id === this.state.selectedValue) {
                    $listItem.addClass('active');
                }
                this.state.$dom.$list.append($listItem[0]);
            }
        };
        DropDown.prototype.clickListItem = function (e) {
            e.stopPropagation();
            var $target = $(e.target);
            var id = $target.data('id');
            $target.addClass('active').siblings('.active').removeClass('active');
            this.setCurrentItem(id);
            this.close();
            this.state.isChange = false;
        };
        DropDown.prototype.setCurrentItem = function (id) {
            this.state.selectedValue = id;
            var filterItems = this.state.data.filter(function (x) {
                return x.id === id;
            });
            if (filterItems.length > 0) {
                var currentItem = filterItems[0];
                var index = currentItem.originalIndex;
                this.updateStartEndIndex((index + 20) * this.options.listItemHeight);
                this.state.inputString = currentItem.name;
                this.state.$dom.$input.val(currentItem.name).data('id', currentItem.id);
                this.state.$dom.$input.removeClass('invalid');
                if (this.options.onSetSelectValue) {
                    this.options.onSetSelectValue(null, {
                        id: currentItem.id,
                        name: currentItem.name,
                        originalItem: currentItem
                    }, this);
                }
            }
        };
        DropDown.prototype.resetScrollAndTransform = function (notUpdate) {
            if (!notUpdate) {
                this.state.indexSection = [0, this.options.pageCount];
                this.state.lastScrollTop = 0;
            }
            this.state.isChange = true;
            this.state.$dom.$listWraper.css('height', this.state.data.length * this.state.listItemHeight);
            this.state.$dom.$listContainer.scrollTop(this.state.lastScrollTop);
            this.state.$dom.$list.css('transform', 'translateY(' + this.state.lastScrollTop + 'px)');
        };
        DropDown.prototype.documentClick = function (e) {
            var state = this.state;
            if (state.isOpen) {
                state.$dom.$listContainer.hide();
                state.isOpen = false;
            }
        };
        DropDown.prototype.listScroll = function (e) {
            var scrollTop = e.target.scrollTop;
            var scrollPX = scrollTop - this.state.lastScrollTop;
            var scrollCount = Math.floor(Math.abs(scrollPX) / this.state.listItemHeight);
            if (scrollCount >= this.state.threshold) {
                this.updateStartEndIndex(scrollTop);
                this.buildListItems();
                this.state.$dom.$list.css('transform', 'translateY(' + this.state.lastScrollTop + 'px)');
            }
        };
        DropDown.prototype.updateStartEndIndex = function (scrollTop) {
            var data = this.options.data;
            var scrolledRowsCount = Math.floor(scrollTop / this.options.listItemHeight);
            var startIndex = scrolledRowsCount - this.state.threshold - 10;
            var endIndex = scrolledRowsCount + this.options.pageCount + this.state.threshold;
            if (startIndex > data.length - this.options.pageCount) {
                startIndex = data.length - this.options.pageCount;
            }
            if (startIndex < 0) {
                startIndex = 0;
            }
            if (endIndex < this.options.pageCount) {
                endIndex = this.options.pageCount;
            }
            if (endIndex > data.length) {
                endIndex = data.length;
            }
            this.state.indexSection = [startIndex, endIndex];
            this.state.lastScrollTop = startIndex * this.state.listItemHeight;
        };
        DropDown.prototype.buildStateData = function (data, key) {
            if (key === undefined || key.trim().length === 0) {
                return data.map(function (item, index) {
                    item.index = index;
                    item.originalIndex = index;
                    return item;
                });
            }
            var searchString = key.trim();
            var filteredData = [];
            for (var i = 0, j = 0, len = data.length; i < len; i++) {
                if (data[i].name.indexOf(searchString) > -1) {
                    data[i].index = j;
                    data[i].originalIndex = i;
                    filteredData.push(data[i]);
                    j++;
                }
            }
            return filteredData;
        };
        DropDown.prototype.inputChange = function (e) {
            var id = e.target.value;
            if (!this.state.isOpen) {
                this.open();
            }
            if (this.state.searchDelayTimeoutId !== null) {
                clearTimeout(this.state.searchDelayTimeoutId);
            }
            this.state.searchDelayTimeoutId = setTimeout(this.handleInputChange.bind(this), this.options.searchDelay, id);
        };
        DropDown.prototype.handleInputChange = function (id) {
            this.state.inputString = id;
            var _this = this;
            if (this.state.searchUrl) {
                if (this.state.searchUrl.indexOf('getProfessionalSelect') !== -1) {// 从业人员模糊搜索
                    json_ajax("POST", this.state.searchUrl, "json", false, {"keyword": id}, function (data) {
                        if (data.success) {
                            _this.state.data = data.obj.map(function (item) {
                                var newItem = {
                                    name: item.identity ? item.name + "(" + item.identity + ")" : item.name,
                                    id: item.id,
                                };
                                return newItem;
                            });
                        } else {
                            _this.state.data = [];
                        }
                    });
                } else {
                    json_ajax("GET", this.state.searchUrl, "json", false, {"search": id}, function (data) {
                        _this.state.data = [];
                        if (data.obj.fuzzyMonitor) {// 监控对象模糊搜索
                            _this.state.data = data.obj.fuzzyMonitor.map(function (item) {
                                var newItem = {
                                    name: item.brand,
                                    id: item.id,
                                    type: item.plateColor
                                };
                                return newItem;
                            })
                        } else if (data.obj.fuzzyDevice) {// 终端号模糊搜索
                            _this.state.data = data.obj.fuzzyDevice.map(function (item) {
                                var newItem = {
                                    name: item.deviceNumber,
                                    id: item.id,
                                    type: item.deviceType,
                                };
                                return newItem;
                            })
                        } else if (data.obj.fuzzySimCard) {// SIM卡模糊搜索
                            _this.state.data = data.obj.fuzzySimCard.map(function (item) {
                                var newItem = {
                                    name: item.simcardNumber,
                                    id: item.id,
                                };
                                return newItem;
                            })
                        }
                    });
                }
            } else {
                this.state.data = this.buildStateData(this.options.data, id);
            }
            this.state.selectedValue = null;
            this.state.$dom.$input.data('id', '');
            this.resetScrollAndTransform();
            this.buildListItems();
            this.state.$dom.$input.addClass('invalid');
            if (this.options.onUnsetSelectValue) {
                this.options.onUnsetSelectValue();
            }
        };
        DropDown.prototype.destory = function () {
            $(document).off('click', this.state.documentClickHandler);
            this.state.$dom.$list.off('click', '.i-list-item', this.state.listItemClickHandler);
            this.state.$dom.$arrow.off('click', this.state.arrowClickHandler);
            this.state.$dom.$listContainer.off('scroll', this.state.listScrollHandler);
            this.state.$dom.$input.off('input', this.state.inputChangeHandler);
            this.state.$dom.$input.off('click', this.state.inputClickHandler);
        };
        return DropDown;
    }());
    $.fn.extend({
        dropdown: function (options) {
            return new DropDown(this, options);
        }
    });
})(jQuery);
