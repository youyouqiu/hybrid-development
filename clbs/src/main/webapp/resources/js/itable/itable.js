var IBaseComponent = (function () {
    function IBaseComponent() {
    }

    IBaseComponent.prototype.buildDom = function (tmpl) {
        var dom = $.parseHTML(tmpl);
        return $(dom);
    };
    IBaseComponent.prototype.defaultColumnRender = function (data) {
        if (data === undefined || data === null) {
            return '';
        }
        if (typeof data === 'string') {
            return data;
        }
        return data.toString();
    };
    IBaseComponent.prototype.defaultGetUniqueId = function (rowData, rowIndex) {
        var str = '';
        var keys = Object.keys(rowData);
        for (var i = 0; i < keys.length; i++) {
            var value = rowData[keys[i]];
            if (value == null) {
                str += '';
                continue;
            }
            str += value.toString();
        }
        return str;
    };
    IBaseComponent.prototype.sequenceColumnRender = function (data, rowIndex) {
        return (rowIndex + 1).toString();
    };
    IBaseComponent.prototype.shadowCopyArray = function (data) {
        var finalData = [];
        for (var i = 0, len = data.length; i < len; i++) {
            finalData.push(data[i]);
        }
        return finalData;
    };
    IBaseComponent.prototype.defaultSorter = function (a, b) {
        var aValue = a === null || a === undefined ? '' : a.toString();
        var bValue = b === null || a === undefined ? '' : b.toString();
        return aValue.localeCompare(bValue);
    };
    IBaseComponent.prototype.sortData = function (data, columns, sortColumnIndex, sortDirection) {
        var _this = this;
        var finalData = data;
        if (sortColumnIndex !== undefined) {
            var sorter_1 = columns[sortColumnIndex].sorter;
            var name_1 = columns[sortColumnIndex].name;
            var renderFunction_1 = columns[sortColumnIndex].render;
            finalData = finalData.sort(function (a, b) {
                var aColumnValue = renderFunction_1(a[name_1]);
                var bColumnValue = renderFunction_1(b[name_1]);
                return typeof sorter_1 === 'function' ? sorter_1(aColumnValue, bColumnValue) : _this.defaultSorter(aColumnValue, bColumnValue);
            });
            if (sortDirection === SortDirection.DESCEND) {
                finalData = finalData.reverse();
            }
        }
        return finalData;
    };
    IBaseComponent.prototype.packageColumn = function (columns) {
        var currentSortColumnIndex, currentSortDirection;
        for (var i = 0; i < columns.length; i++) {
            var element = columns[i];
            if (element.render == undefined || element.render === null) {
                if (element.isSequence) {
                    element.render = this.sequenceColumnRender;
                }
                else {
                    element.render = this.defaultColumnRender;
                }
            }
            if (element.sorter && element.defaultSortOrder && currentSortColumnIndex === undefined) {
                currentSortColumnIndex = i;
                currentSortDirection = element.defaultSortOrder;
            }
        }
        return [currentSortColumnIndex, currentSortDirection];
    };
    IBaseComponent.prototype.parsePercent = function (percent) {
        return parseFloat(percent) / 100;
    };
    IBaseComponent.prototype.valueToString = function (value) {
        if (value == null) {
            return '';
        }
        return value.toString();
    };
    IBaseComponent.prototype.findRow = function (rows, options, id, map) {
        if (map && map[id]) {
            return map[id];
        }
        for (var i = 0, len = rows.length; i < len; i++) {
            var row = rows[i];
            var rowId = this.getRowId(row, options);
            if (rowId === id) {
                return [row, i];
            }
        }
        return null;
    };
    IBaseComponent.prototype.getRowId = function (row, options, i) {
        if (row.____id) {
            return row.____id;
        }
        var rowId;
        if (typeof options.getUniqueId === 'string') {
            rowId = this.valueToString(row[options.getUniqueId]);
        }
        else if (typeof options.getUniqueId === 'function') {
            rowId = this.valueToString(options.getUniqueId(row, i));
        }
        else {
            rowId = this.defaultGetUniqueId(row, i);
        }
        row.____id = rowId;
        return rowId;
    };
    IBaseComponent.prototype.detectIE = function () {
        var ua = window.navigator.userAgent;
        var msie = ua.indexOf("MSIE ");
        return msie > -1;
    };
    IBaseComponent.prototype.isNumber = function (num) {
        return '0123456789'.indexOf(num) > -1;
    };
    IBaseComponent.prototype.isAlphabet = function (char) {
        return 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'.indexOf(char) > -1;
    };
    return IBaseComponent;
}());
var SortDirection;
(function (SortDirection) {
    SortDirection.ASCEND = "ascend";
    SortDirection.DESCEND = "descend";
}(SortDirection || (SortDirection = {})));
var __extends = (this && this.__extends) || (function () {
    var extendStatics = function (d, b) {
        extendStatics = Object.setPrototypeOf ||
            ({__proto__: []} instanceof Array && function (d, b) {
                d.__proto__ = b;
            }) ||
            function (d, b) {
                for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
            };
        return extendStatics(d, b);
    };
    return function (d, b) {
        extendStatics(d, b);

        function __() {
            this.constructor = d;
        }

        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
}());
var ITable = (function (_super) {
    __extends(ITable, _super);

    function ITable($this, optionsParam) {
        var _this = _super.call(this) || this;
        _this.setOption(optionsParam, $this);
        return _this;
    }

    ITable.prototype.setOption = function (optionsParam, $this) {
        var $thisRef = $this === undefined ? this.state.$dom.$origin : $this;
        this.destory();
        var defaults = {
            name: 'table',
            cancelActiveRow: false,
            clickMeansActive: false,
            dblClickMeansLock: false,
            flashWhenUpdate: false,
            scrollWhenAppend: false,
            activeScrollDuration: 200,
            activeRowBgColor: '#DCF5FF',
            virtualRender: false,
            visibleRowsCount: 20,
            trHeight: 41,
            scrollThreshold: 20,
            striped: true,
        };
        var options = $.extend(defaults, optionsParam);
        var _a = this.packageColumn(options.columns), currentSortColumnIndex = _a[0], currentSortDirection = _a[1];
        this.options = options;
        this.state = {
            width: null,
            height: null,
            isResizing: false,
            data: this.options.handleSort ? this.buildStateData(options.data) : this.buildStateData(options.data, currentSortColumnIndex, currentSortDirection),
            dataIndexMap: {},
            optionDataIndexMap: {},
            currentSortColumnIndex: currentSortColumnIndex,
            currentSortDirection: currentSortDirection,
            innerScrollTop: 0,
        };
        for (var i = 0; i < this.options.data.length; i++) {
            var row = this.options.data[i];
            this.state.optionDataIndexMap[this.getRowId(row, this.options)] = [row, i];
        }
        for (var i = 0; i < this.state.data.length; i++) {
            var row = this.state.data[i];
            this.state.dataIndexMap[this.getRowId(row, this.options)] = [row, i];
        }
        this.initHtml($thisRef);
        this.bindEvent();
    };
    ITable.prototype.getOption = function () {
        return this.options;
    };
    ITable.prototype.getState = function () {
        return this.state;
    };
    // 设置分页组件
    ITable.prototype.setPagination = function (optionsParam) {
        console.log(optionsParam, 'optionsParam');
    };
    ITable.prototype.initHtml = function ($this) {
        var options = this.options;
        var width = $this.width();
        var height = $this.height();
        var $root = this.buildDom(ITable.rootTmpl);
        var $container = this.buildDom(ITable.containerTmpl);
        var $outer = this.buildDom(ITable.outerTmpl);
        var $inner = this.buildDom(ITable.innerTmpl);
        var $tableWraper = this.buildDom(ITable.tableWraperTmpl);
        var $table = this.buildDom(options.striped ? ITable.tableTmpl : ITable.tableTmpl.replace('table-striped', ''));
        var $colgroup = this.buildDom(ITable.colgroupTmpl);
        var $thead = this.buildDom(ITable.theadTmpl);
        var $tbody = this.buildDom(ITable.tbodyTmpl);
        this.state.height = height;
        this.state.width = width;
        this.state.$dom = {
            $origin: $this,
            $root: $root,
            $container: $container,
            $inner: $inner,
            $tableWraper: $tableWraper,
            $table: $table,
            $colgroup: $colgroup,
            $thead: $thead,
            $tbody: $tbody,
        };
        $table.attr('id', options.tableId + options.name);
        if (typeof options.width === 'number') {
            $root.css('width', options.width + "px");
        }
        $this.empty();
        $this.append($root.get(0));
        $root.append($container.get(0));
        $container.append($outer.get(0));
        $outer.append($inner.get(0));
        $inner.append($tableWraper.get(0));
        $tableWraper.append($table.get(0));
        $table.append($colgroup.get(0)).append($thead.get(0)).append($tbody.get(0));
        var $theadTr = this.buildDom(ITable.trTmpl);
        $thead.append($theadTr.get(0));
        for (var i = 0; i < options.columns.length; i++) {
            var column = options.columns[i];
            var $col = this.buildCol(column, i);
            var $th = this.buildTh(column, i);
            $colgroup.append($col.get(0));
            $theadTr.append($th.get(0));
        }
    };
    ITable.prototype.bindEvent = function () {
        this.state.$dom.$thead.on('mousedown', "." + ITable.resizeHandleClass, this.handleResizeMousedown.bind(this));
        this.state.$dom.$thead.on('dblclick', "." + ITable.resizeHandleClass, this.handleResizeDblClick.bind(this));
        this.state.$dom.$thead.on('click', "." + ITable.sortHandleClass, this.handleSortClick.bind(this));
        this.state.$dom.$thead.on('click', "." + ITable.thCellDivClass, this.handleThClick.bind(this));
        this.state.$dom.$thead.on('mouseenter', 'th', this.handleThHover.bind(this));
        this.state.$dom.$tbody.on('mouseenter', 'td', this.handleTdHover.bind(this));
        this.state.$dom.$tbody.on('mouseleave', this.handleTbodyLeave.bind(this));
        this.state.$dom.$tbody.on('click', 'td', this.handleTdClick.bind(this));
        this.state.$dom.$tbody.on('contextmenu', 'td', this.handleTdRightClick.bind(this));
        this.state.$dom.$tbody.on('dblclick', 'td', this.handleTdDblClick.bind(this));
        this.state.$dom.$root.on('mouseenter', this.handleEnter.bind(this));
        this.state.$dom.$inner.on('scroll', this.handleScroll.bind(this));
    };
    ITable.prototype.buildCol = function (column, i) {
        var $col = this.buildDom(ITable.colTmpl);
        if (column.width !== undefined && column.width !== null && i < this.options.columns.length - 1) {
            var columnWidth = typeof column.width === 'number' ? column.width : this.parsePercent(column.width) * this.state.width;
            columnWidth = Math.floor(columnWidth);
            $col.css({
                'width': columnWidth.toString() + 'px',
                'minWidth': columnWidth.toString() + 'px',
            });
        }
        return $col;
    };
    ITable.prototype.buildTh = function (column, i) {
        var $th = this.buildDom(ITable.thTmpl);
        var $thCell = this.buildDom(ITable.thCellDivTmpl);
        $thCell.html(column.title);
        $th.append($thCell.get(0));
        $th.attr('data-name', column.name);
        if (column.sorter) {
            if (column.sortDirections.indexOf(SortDirection.ASCEND) > -1) {
                var $ascend = this.buildDom(ITable.upTmpl);
                if (column.defaultSortOrder === SortDirection.ASCEND) {
                    $ascend.addClass(ITable.sortOnClass);
                }
                else {
                    $ascend.addClass(ITable.sortOffClass);
                }
                $th.append($ascend.get(0));
            }
            if (column.sortDirections.indexOf(SortDirection.DESCEND) > -1) {
                var $descend = this.buildDom(ITable.downTmpl);
                if (column.defaultSortOrder === SortDirection.DESCEND) {
                    $descend.addClass(ITable.sortOnClass);
                }
                else {
                    $descend.addClass(ITable.sortOffClass);
                }
                $th.append($descend.get(0));
            }
        }
        if ((column.resizable === true && i < this.options.columns.length - 1)
            || column.sorter) {
            $th.addClass(ITable.actionClass);
        }
        if (column.resizable === true && i < this.options.columns.length - 1) {
            $th.addClass(ITable.resizeClass).append($.parseHTML(ITable.resizeHandleTmpl));
        }
        return $th;
    };
    ITable.prototype.getResizeDom = function () {
        var r = [this.state.$dom.$resizingDom];
        if (this.detectIE()) {
            var index = this.state.$dom.$resizingDom.index();
            var $th = this.state.$dom.$thead.find("th:eq(" + index.toString() + ")");
            r.push($th);
        }
        return r;
    };
    ITable.prototype.handleResizeDblClick = function (event) {
        var $parent = $(event.target).parent();
        var index = $parent.index();
        var $col = $parent.closest('table').find("colgroup>col:eq(" + index + ")");
        this.state.$dom.$resizingDom = $col;
        var columnName = this.options.columns[index].name;
        var maxWidth = this.options.columns[index].maxWidth;
        var minWidth = this.options.columns[index].minWidth;
        var charLength = 0;
        for (var i = 0; i < this.state.data.length; i++) {
            var element = this.state.data[i];
            var elementLength = 0;
            var elementStr = this.options.columns[index].render(element[columnName], i, index, element);
            var elementCount = elementStr.length;
            for (var j = 0; j < elementCount; j++) {
                var c = elementStr[j];
                if (this.isAlphabet(c) || this.isNumber(c)) {
                    elementLength += 0.6;
                }
                else {
                    elementLength += 1;
                }
            }
            elementLength = Math.ceil(elementLength);
            if (elementLength > charLength) {
                charLength = elementLength;
            }
        }
        if (charLength > 0) {
            var $firstTd = this.state.$dom.$tbody.find('td:eq(0)');
            var defaultFontSize = 14;
            var actualFontSize = parseInt($firstTd.css('font-size'));
            actualFontSize = actualFontSize === null || isNaN(actualFontSize) ? defaultFontSize : actualFontSize;
            var padding = parseInt($firstTd.css('padding'));
            padding = padding === null || isNaN(padding) ? 4 : padding;
            var width_1 = (charLength * actualFontSize) + (padding * 2);
            if (maxWidth !== undefined && maxWidth !== null) {
                width_1 = Math.min(width_1, maxWidth);
            }
            if (minWidth !== undefined && minWidth !== null) {
                width_1 = Math.max(width_1, minWidth);
            }
            var currentWidth = this.state.$dom.$resizingDom.outerWidth();
            var delta = width_1 - currentWidth;
            var rootWidth = this.state.$dom.$root.outerWidth();
            rootWidth = Math.floor(rootWidth + delta);
            this.getResizeDom().forEach(function ($dom) {
                $dom.css({
                    'width': width_1.toString() + 'px',
                    'minWidth': width_1.toString() + 'px',
                });
            });
            this.state.$dom.$root.css({
                'width': rootWidth.toString() + 'px',
            });
        }
    };
    ITable.prototype.handleSortClick = function (event) {
        this.handleSortClickDomOpe($(event.target));
    };
    ITable.prototype.handleSortClickDomOpe = function ($target) {
        var $i = $target.closest('i');
        if ($i.length === 0) {
            return;
        }
        var $th = $target.closest('th');
        var $lastI;
        var index = $th.index();
        var direction = $i.hasClass('up') ? SortDirection.ASCEND : SortDirection.DESCEND;
        var customSort = typeof this.options.customHandleSort === 'function';
        if (this.state.currentSortColumnIndex === index && this.state.currentSortDirection === direction) {
            index = undefined;
            direction = undefined;
        }
        else {
            $lastI = $th.closest('tr')
                .find("th:eq(" + this.state.currentSortColumnIndex + ")")
                .find("i." + (this.state.currentSortDirection === SortDirection.ASCEND ? 'up' : 'down'));
        }
        if (!customSort) {
            this.state.currentSortColumnIndex = index;
            this.state.currentSortDirection = direction;
            if (index === undefined) {
                $i.removeClass(ITable.sortOnClass).addClass(ITable.sortOffClass);
            }
            else {
                $i.removeClass(ITable.sortOffClass).addClass(ITable.sortOnClass);
                $lastI.removeClass(ITable.sortOnClass).addClass(ITable.sortOffClass);
            }
            if (typeof this.options.handleSort === 'function') {
                this.options.handleSort(this.state.currentSortColumnIndex, this.state.currentSortDirection);
            }
            else {
                this.updateStateData(this.buildStateData(this.options.data));
            }
        }
        else {
            this.options.customHandleSort(index, direction);
        }
    };
    ITable.prototype.handleThClick = function (event) {
        var $target = $(event.target);
        var $th = $target.closest('th');
        if (!$th.hasClass(ITable.actionClass)) {
            return;
        }
        var direction;
        var $upSortHandle = $th.find("." + ITable.sortHandleClass + ".up");
        var $downSortHandle = $th.find("." + ITable.sortHandleClass + ".down");
        var $onSortHandle = $th.find("." + ITable.sortHandleClass + "." + ITable.sortOnClass);
        if ($onSortHandle.length === 0) {
            if ($upSortHandle.length === 1) {
                direction = 'up';
            }
            else {
                direction = 'down';
            }
        }
        else {
            if ($upSortHandle.hasClass(ITable.sortOnClass)) {
                if ($downSortHandle.length === 1) {
                    direction = 'down';
                }
                else {
                    direction = 'up';
                }
            }
            else {
                direction = 'down';
            }
        }
        var $domTarget = $th.find("." + ITable.sortHandleClass + "." + direction);
        this.handleSortClickDomOpe($domTarget);
    };
    ITable.prototype.handleResizeMousedown = function (event) {
        this.state.isResizing = true;
        this.state.resizePrevPageX = event.pageX;
        var $parent = $(event.target).parent();
        var index = $parent.index();
        var $col = $parent.closest('table').find("colgroup>col:eq(" + index + ")");
        this.state.$dom.$resizingDom = $col;
        this.state.documentMouseMoveHandler = this.handleResizeMousemove.bind(this);
        this.state.documentMouseUpHandler = this.handleResizeMouseup.bind(this);
        $(document).on('mousemove', this.state.documentMouseMoveHandler);
        $(document).on('mouseup', this.state.documentMouseUpHandler);
    };
    ITable.prototype.handleResizeMousemove = function (event) {
        if (this.state.isResizing === true) {
            var index = this.state.$dom.$resizingDom.index();
            var minWidth = this.options.columns[index].minWidth;
            var maxWidth = this.options.columns[index].maxWidth;
            var delta = event.pageX - this.state.resizePrevPageX;
            var nowWidth = this.state.$dom.$resizingDom.outerWidth();
            var width_2 = Math.floor(nowWidth + delta);
            if (minWidth !== undefined && minWidth !== null) {
                width_2 = Math.max(width_2, minWidth);
                delta = width_2 - nowWidth;
            }
            if (maxWidth !== undefined && maxWidth !== null) {
                width_2 = Math.min(width_2, maxWidth);
                delta = width_2 - nowWidth;
            }
            var rootWidth = this.state.$dom.$root.outerWidth();
            rootWidth = Math.floor(rootWidth + delta);
            this.state.resizePrevPageX = event.pageX;
            this.getResizeDom().forEach(function ($dom) {
                $dom.css({
                    'width': width_2.toString() + 'px',
                    'minWidth': width_2.toString() + 'px',
                });
            });
            this.state.$dom.$root.css({
                'width': rootWidth.toString() + 'px',
            });
        }
    };
    ITable.prototype.handleResizeMouseup = function (event) {
        if (this.state.isResizing) {
            $(document).off('mousemove', this.state.documentMouseMoveHandler);
            $(document).off('mouseup', this.state.documentMouseUpHandler);
            this.state.isResizing = false;
            this.state.$dom.$resizingDom = undefined;
        }
    };
    ITable.prototype.handleScroll = function (event) {
        var visibleScroll = this.state.$dom.$inner.scrollTop();
        if (this.options.virtualRender) {
            var diff = Math.floor(Math.abs(visibleScroll - this.state.innerScrollTop) / this.options.trHeight);
            if (diff > this.options.scrollThreshold) {
                this.state.innerScrollTop = visibleScroll;
                this.virtualRender();
            }
        }
        if (typeof this.options.handleScroll === 'function') {
            this.options.handleScroll(visibleScroll);
        }
    };
    ITable.prototype.handleEnter = function (event) {
        if (typeof this.options.handleEnter === 'function') {
            this.options.handleEnter(this.options.name);
        }
    };
    ITable.prototype.handleTbodyLeave = function (event) {
        if (typeof this.options.handleTbodyLeave === 'function') {
            this.options.handleTbodyLeave(this.options.name);
        }
        else {
            this.handleTdHoverDomOpe(undefined, undefined);
        }
    };
    ITable.prototype.handleThHover = function (event) {
        var $th = $(event.target);
        if ($th[0].tagName !== 'TH') {
            $th = $th.closest('th');
        }
        var cellIndex = $th.index();
        if (typeof this.options.handleThHover === 'function') {
            this.options.handleThHover(cellIndex, $th);
        }
    };
    ITable.prototype.handleTdHover = function (event) {
        var $td = $(event.target);
        if ($td[0].tagName !== 'TD') {
            $td = $td.closest('td');
        }
        var $tr = $td.closest('tr');
        var rowIndex = $tr.index();
        var cellIndex = $td.index();
        if (typeof this.options.handleTdHover === 'function') {
            this.options.handleTdHover(rowIndex, cellIndex, $td);
        }
        this.handleTdHoverDomOpe(rowIndex, cellIndex);
    };
    ITable.prototype.handleTdHoverDomOpe = function (rowIndex, cellIndex) {
        if (this.state.lastHoverRowIndex !== undefined) {
            if (this.state.lastHoverRowIndex === rowIndex) {
                return;
            }
            this.state.$dom.$tbody.find("tr:eq(" + this.state.lastHoverRowIndex + ")").removeClass('hover');
        }
        if (rowIndex !== undefined) {
            var $tr = this.state.$dom.$tbody.find("tr:eq(" + rowIndex + ")");
            $tr.addClass('hover');
        }
        this.state.lastHoverRowIndex = rowIndex;
        this.state.lastHoverCellIndex = cellIndex;
    };
    ITable.prototype.handleTdRightClick = function (event) {
        event.preventDefault();
        var $td = $(event.target);
        if ($td[0].tagName !== 'TD') {
            $td = $td.closest('td');
        }
        var $tr = $td.closest('tr');
        var rowId = $tr.attr('id');
        if (typeof rowId !== 'undefined') {
            rowId = rowId.toString();
        }
        var cellIndex = $td.index();
        if (typeof this.options.handleTdRightClick === 'function') {
            this.options.handleTdRightClick(rowId, cellIndex, $td, event);
        }
    };
    ITable.prototype.handleTdClick = function (event) {
        var $td = $(event.target);
        if ($td[0].tagName !== 'TD') {
            $td = $td.closest('td');
        }
        var $tr = $td.closest('tr');
        var rowId = $tr.attr('id');
        if (typeof rowId !== 'undefined') {
            rowId = rowId.toString();
        }
        var cellIndex = $td.index();
        if (typeof this.options.handleTdClick === 'function') {
            if (this.state.lastClickRowId === rowId && this.options.cancelActiveRow) {
                this.options.handleTdClick(undefined, undefined, $td);
            }
            else {
                this.options.handleTdClick(rowId, cellIndex, $td);
            }
        }
        if (this.state.lastClickRowId === rowId && !this.options.cancelActiveRow) {
            return;
        }
        if (!this.options.clickMeansActive) {
            return;
        }
        this.handleTdClickDomOpe(rowId, cellIndex);
    };
    ITable.prototype.handleTdClickDomOpe = function (rowId, cellIndex) {
        if (this.state.lastClickRowId !== undefined) {
            this.state.$dom.$tbody.find("#" + this.state.lastClickRowId).removeClass('active');
        }
        if (this.state.lastClickRowId === rowId || rowId === undefined || this.state.lastLockedRowId === rowId) {
            this.state.lastClickRowId = undefined;
            this.state.lastClickCellIndex = undefined;
        }
        else {
            this.state.lastClickRowId = rowId;
            this.state.lastClickCellIndex = cellIndex;
            var row = this.state.$dom.$tbody.find("#" + rowId);
            var rawRow = this.findRow(this.state.data, this.options, rowId, this.state.dataIndexMap);
            if (cellIndex === -1 && rawRow !== null) {
                var _ = rawRow[0], rowIndex = rawRow[1];
                var rowHeight = this.options.trHeight;
                var visibleHeight = this.state.$dom.$inner.height();
                var visibleScroll = this.state.$dom.$inner.scrollTop();
                var needScroll = rowHeight * rowIndex;
                if (needScroll < (visibleScroll + rowHeight) || needScroll > (visibleScroll + visibleHeight - rowHeight)) {
                    this.updateScrollTop(needScroll, this.options.activeScrollDuration);
                }
            }
            row.addClass('active');
        }
        if (this.state.lastLockedRowId !== undefined) {
            this.handleTdDblClickDomOpe(undefined, undefined);
            if (typeof this.options.handleTdDblClick === 'function') {
                this.options.handleTdDblClick(undefined, undefined);
            }
        }
    };
    ITable.prototype.handleTdDblClick = function (event) {
        var $td = $(event.target);
        if ($td[0].tagName !== 'TD') {
            $td = $td.closest('td');
        }
        $td.justToolsTip();
        var $tr = $td.closest('tr');
        var rowId = $tr.attr('id');
        if (typeof rowId !== 'undefined') {
            rowId = rowId.toString();
        }
        var cellIndex = $td.index();
        if (this.state.lastLockedRowId !== rowId) {
            if (typeof this.options.handleTdDblClick === 'function') {
                this.options.handleTdDblClick(rowId, cellIndex, $td);
            }
        }
        if (!this.options.dblClickMeansLock) {
            return;
        }
        this.handleTdDblClickDomOpe(rowId, cellIndex);
        // console.warn(this.options, 'this.options');
        // this.handleTbodyLeave();
        // if (typeof this.options.handleTbodyLeave === 'function') {
        //     console.warn('handleTdDblClick222');
        //     this.options.handleTbodyLeave(this.options.name);
        // }

    };
    ITable.prototype.handleTdDblClickDomOpe = function (rowId, _cellIndex) {
        if (this.state.lastLockedRowId === rowId || rowId === undefined) {
            this.state.$dom.$tbody.find("#" + this.state.lastLockedRowId).removeClass('locked');
            this.state.lastLockedRowId = undefined;
            return;
        }

        this.state.lastLockedRowId = rowId;
        var rowData = this.findRow(this.state.data, this.options, rowId, this.state.dataIndexMap);
        if (rowData !== null) {
            var row = rowData[0], index = rowData[1];
            this.state.data.splice(index, 1);
            this.state.data.splice(0, 0, row);
            this.state.dataIndexMap = {};
            for (var i = 0; i < this.state.data.length; i++) {
                var row = this.state.data[i];
                this.state.dataIndexMap[this.getRowId(row, this.options)] = [row, i];
            }
        }

        if (this.options.virtualRender) {
            this.updateScrollTop(0);
            this.render();
        }
        else {
            this.render();
            this.updateScrollTop(0);
        }
    };
    ITable.prototype.updateScrollTop = function (scrollTop, duration) {
        var dur = !duration ? 0 : duration;
        if (dur > 0) {
            this.state.$dom.$inner.animate({
                'scrollTop': scrollTop
            }, dur);
        }
        else {
            this.state.$dom.$inner.scrollTop(scrollTop);
        }
    };
    ITable.prototype.render = function () {
        var columns = this.options.columns;
        var data = this.state.data;
        var thHeight = this.state.$dom.$table.find('thead').length > 0 ? 39 : 0;
        this.state.$dom.$tableWraper.css('height', data.length * this.options.trHeight + thHeight);
        this.updateScrollTop(0);
        if (this.options.virtualRender) {
            this.virtualRender();
            return;
        }
        this.state.$dom.$tbody.empty();
        this.state.dataIndexMap = {};
        this.state.optionDataIndexMap = {};
        for (var i = 0; i < data.length; i++) {
            var row = data[i];
            var $tr = this.buildRow(row, columns, i);
            this.state.$dom.$tbody.append($tr.get(0));
            this.state.dataIndexMap[this.getRowId(row, this.options)] = [row, i];
        }

        for (var i = 0; i < this.options.data.length; i++) {
            var row = this.options.data[i];
            this.state.optionDataIndexMap[this.getRowId(row, this.options)] = [row, i];
        }
    };
    ITable.prototype.virtualRender = function () {
        var columns = this.options.columns;
        var data = this.state.data;
        var visibleScroll = this.state.innerScrollTop;
        var scrolledRowsCount = Math.floor(visibleScroll / this.options.trHeight);
        var startIndex = scrolledRowsCount - this.options.scrollThreshold - 10;
        var endIndex = scrolledRowsCount + this.options.visibleRowsCount + this.options.scrollThreshold;
        if (startIndex < 0) {
            startIndex = 0;
        }
        else if (startIndex > data.length - this.options.visibleRowsCount) {
            startIndex = data.length - this.options.visibleRowsCount;
        }
        if (endIndex > data.length) {
            endIndex = data.length;
        }
        else if (endIndex < this.options.visibleRowsCount) {
            endIndex = this.options.visibleRowsCount;
        }
        this.state.visibleStartIndex = startIndex;
        this.state.visibleEndIndex = endIndex;
        if (this.options.virtualRender && this.options.parentTable) {
            this.options.parentTable.getState().visibleStartIndex = startIndex;
            this.options.parentTable.getState().visibleEndIndex = endIndex;
        }
        var html = '';
        for (var i = startIndex; i < endIndex; i++) {
            var row = data[i];
            if (!row) {
                continue;
            }
            html += this.buildRowHTML(row, columns, i);
        }
        this.state.$dom.$table.css('transform', 'translateY(' + startIndex * this.options.trHeight + 'px)');
        this.state.$dom.$tbody.html(html);
    };
    ITable.prototype.buildRow = function (row, columns, rowIndex) {
        var $tr = this.buildDom(ITable.trTmpl);
        var rowId = this.getRowId(row, this.options, rowIndex);
        $tr.attr('id', rowId);
        if (rowId === this.state.lastClickRowId) {
            $tr.addClass('active');
        }
        if (rowId === this.state.lastLockedRowId) {
            $tr.addClass('locked');
        }
        var originId = this.state.$dom.$origin.selector.split(' ')[0].replace('#', '');
        for (var j = 0; j < columns.length; j++) {
            var value = row[columns[j].name];
            var $td = this.buildDom(ITable.tdTmpl);
            $td.attr('data-field', columns[j].name);
            $td.attr('id', originId + "_" + rowId + "_" + columns[j].name);
            if (!!columns[j].className) {
                $td.addClass(columns[j].className);
            }
            var $cellDiv = this.buildDom(ITable.cellDivTmpl);
            $cellDiv.html(columns[j].render(value, rowIndex, j, row));
            $td.append($cellDiv.get(0));
            $tr.append($td.get(0));
        }
        return $tr;
    };
    ITable.prototype.buildRowHTML = function (row, columns, rowIndex) {
        var trHTML = '<tr';
        var rowId = this.getRowId(row, this.options, rowIndex);
        trHTML += " id=\"" + rowId + "\"";
        if (rowId === this.state.lastLockedRowId) {
            trHTML += ' class="locked"';
        }
        else if (rowId === this.state.lastClickRowId) {
            trHTML += ' class="active"';
        }
        trHTML += '>';
        for (var j = 0; j < columns.length; j++) {
            var value = row[columns[j].name];
            var originId = this.state.$dom.$origin.selector.split(' ')[0].replace('#', '') + '_' + rowId + "_" + columns[j].name;
            var tdHTML = "<td id=\"" + originId + "\" data-field=\"" + columns[j].name + "\"";
            if (!!columns[j].className) {
                tdHTML += " class=\"" + columns[j].className + "\"";
            }
            tdHTML += '>';
            var cellDivHTML = "<div class=\"cell-div\">" + columns[j].render(value, rowIndex, j, row) + "</div>";
            tdHTML += cellDivHTML + '</td>';
            trHTML += tdHTML;
        }
        trHTML += '</tr>';
        return trHTML;
    };
    ITable.prototype.updateStateSort = function (sortColumnIndex, sortDirection) {
        var $lastI = this.state.$dom.$thead.find('tr')
            .find("th:eq(" + this.state.currentSortColumnIndex + ")")
            .find("i." + (this.state.currentSortDirection === SortDirection.ASCEND ? 'up' : 'down'));
        if (this.state.currentSortColumnIndex !== undefined && sortColumnIndex === undefined) {
            $lastI.removeClass(ITable.sortOnClass).addClass(ITable.sortOffClass);
            this.state.currentSortColumnIndex = undefined;
            this.state.currentSortDirection = undefined;
        }
        else {
            var $i = this.state.$dom.$thead.find('tr')
                .find("th:eq(" + sortColumnIndex + ")")
                .find("i." + (sortDirection === SortDirection.ASCEND ? 'up' : 'down'));
            $i.removeClass(ITable.sortOffClass).addClass(ITable.sortOnClass);
            this.state.currentSortColumnIndex = sortColumnIndex;
            this.state.currentSortDirection = sortDirection;
        }
    };
    ITable.prototype.updateStateData = function (data) {
        if (!this.options.handleSort) {
            this.state.data = this.buildStateData(data, this.state.currentSortColumnIndex, this.state.currentSortDirection);
        }
        else {
            this.state.data = this.buildStateData(data);
        }
        this.state.dataIndexMap = {};
        for (var i = 0; i < this.state.data.length; i++) {
            var row = this.state.data[i];
            this.state.dataIndexMap[this.getRowId(row, this.options)] = [row, i];
        }
        this.render();
    };
    ITable.prototype.updateOptionData = function (row) {
        var rowId = this.getRowId(row, this.options);
        var optionRowData = this.findRow(this.options.data, this.options, rowId, this.state.optionDataIndexMap);
        if (optionRowData === null) {
            console.error('update option data not found:', row);
            return;
        }
        var optionRow = optionRowData[0];
        var _a = this.findRow(this.state.data, this.options, rowId, this.state.dataIndexMap), stateRow = _a[0],
            rowIndex = _a[1];
        var currentRowKeys = Object.keys(optionRow);
        // console.warn([...currentRowKeys], 'currentRowKeys1');
        // 状态信息 报警记录 OBD数据 优化
        // 所属企业 监控对象 标识颜色 对象类型 终端号 终端手机号 定位方式 序号 从业人员 车架号 驾驶证号 车主 车主电话 从业人员姓名 从业人员手机1 2 3
        var screenData = ['id', '____id', 'groupName', 'monitorName', 'plateColorName', 'vehicleType', 'deviceNumber', 'simcardNumber', 'locationType', 'sequenceNumber', 'professionalsName', 'frameNumber', 'drivingLicenseNo', 'vehicleOwner', 'vehicleOwnerPhone', 'alarmProfessionalsName', 'phone', 'phoneTwo', 'phoneThree'];
        for (var i = 0; i < screenData.length; i++) {
            for (var j = 0; j < currentRowKeys.length; j++) {
                if (screenData[i] === currentRowKeys[j]) {
                    currentRowKeys.splice(j, 1);
                }
            }
        }
        var originId = this.state.$dom.$origin.selector.split(' ')[0];
        var keyObj = {};
        for (var j = 0; j < this.options.columns.length; j++) {
            var c = this.options.columns[j];
            keyObj[c.name] = c;
        }
        /* var trRow = $(originId + " #" + rowId);
         var tdNum = $(originId + "_" + rowId + "_sequenceNumber div").text();
         var $tr = this.buildRow(row, this.options.columns, Number(tdNum) - 1);
         trRow.replaceWith($tr);*/
        var _loop_1 = function (i) {
            var key = currentRowKeys[i];
            if (row[key] !== undefined) {
                optionRow[key] = row[key];
                stateRow[key] = row[key];
                // console.warn(this_1.state.$dom.$tbody[0].innerHTML, 'this_1.state.$dom.$tbody');
                // console.warn("#" + rowId + "_" + key);
                // var $td_1 = this_1.state.$dom.$tbody.find("#" + rowId + "_" + key);
                // var td_1 = this_1.state.$dom.$tbody[0].innerHTML.indexOf(rowId + "_" + key);
                // console.warn(td_1, 'td_1');
                var $td_1 = $(originId + "_" + rowId + "_" + key);
                if ($td_1.length == 0) return;
                // console.warn($td_1, '$td_1');
                // if (td_1 != -1) {
                var column = keyObj[key];
                /*var j = 0;
                for (; j < this_1.options.columns.length; j++) {
                    var c = this_1.options.columns[j];
                    if (c.name === key) {
                        column = c;
                        break;
                    }
                }*/
                if (column !== undefined) {
                    var toChangeHTML = column.render(stateRow[key], rowIndex, j, row);
                    /* var currentHTML = $td_1.children('div').html();
                   if (currentHTML !== toChangeHTML) {*/
                    if (this_1.options.flashWhenUpdate) {
                        $td_1.addClass('change');
                        setTimeout(function () {
                            $td_1.removeClass('change');
                        }, 1100);
                    }
                    $td_1.children('div')[0].innerHTML = toChangeHTML;
                    // }
                }
                // }
            }
        };
        var this_1 = this;
        for (var i = 0; i < currentRowKeys.length; i++) {
            _loop_1(i);
        }
    };
    ITable.prototype.replaceOptionData = function (data) {
        this.options.data = data;
        this.state.optionDataIndexMap = {};
        for (var i = 0; i < this.options.data.length; i++) {
            var row = this.options.data[i];
            this.state.optionDataIndexMap[this.getRowId(row, this.options)] = [row, i];
        }
        this.updateStateData(data);
    };
    ITable.prototype.appendOptionData = function (row) {
        for (var i = 0; i < row.length; i++) {
            var item = row[i];
            this.options.data.push(item);
            this.state.data.push(item);
            var $tr = this.buildRow(item, this.options.columns, this.getDataLength() - 1);
            this.state.$dom.$tbody.append($tr.get(0));
        }
        if (this.options.scrollWhenAppend) {
            var height = this.state.$dom.$table.outerHeight();
            this.updateScrollTop(height - 40, 500);
        }
    };
    ITable.prototype.prependOptionData = function (row) {
        var _a, _b;
        (_a = this.options.data).splice.apply(_a, [0, 0].concat(row));
        (_b = this.state.data).splice.apply(_b, [0, 0].concat(row));
        this.render();
        this.updateScrollTop(0, 500);
    };
    ITable.prototype.deleteOptionData = function (ids) {
        if (ids.indexOf(this.state.lastClickRowId) >= 0) {
            this.state.lastClickRowId = undefined;
        }
        if (ids.indexOf(this.state.lastLockedRowId) >= 0) {
            this.state.lastLockedRowId = undefined;
        }
        for (var i = this.options.data.length - 1; i >= 0; i--) {
            var item = this.options.data[i];
            if (ids.indexOf(item.id) !== -1) {
                this.options.data.splice(i, 1);
            }
        }
        for (var i = this.state.data.length - 1; i >= 0; i--) {
            var item = this.state.data[i];
            if (ids.indexOf(item.id) !== -1) {
                this.state.data.splice(i, 1);
            }
        }
        this.state.dataIndexMap = {};
        for (var i = 0; i < this.state.data.length; i++) {
            var row = this.state.data[i];
            this.state.dataIndexMap[this.getRowId(row, this.options)] = [row, i];
        }
        /*for (var i = 0; i < ids.length; i++) {
            var id = ids[i];
            var _a = this.findRow(this.options.data, this.options, id, this.state.optionDataIndexMap),
                optionIndex = _a[1];
            this.options.data.splice(optionIndex, 1);
            var _b = this.findRow(this.state.data, this.options, id, this.state.dataIndexMap), stateIndex = _b[1];
            this.state.data.splice(stateIndex, 1);
        }*/
        this.render();
    };
    ITable.prototype.setActiveRow = function (id) {
        this.handleTdClickDomOpe(id, -1);
    };
    ITable.prototype.setLockedRow = function (id) {
        this.handleTdDblClickDomOpe(id, -1);
    };
    ITable.prototype.buildStateData = function (data, sortColumnIndex, sortDirection) {
        var finalData = this.shadowCopyArray(data);
        finalData = this.sortData(finalData, this.options.columns, sortColumnIndex, sortDirection);
        return finalData;
    };
    ITable.prototype.getDataLength = function () {
        if (!this.state || !this.state.data) {
            return 0;
        }
        return this.state.data.length;
    };
    ITable.prototype.destory = function () {
        if (this.state) {
            this.state.$dom.$origin.empty();
            this.state.$dom = undefined;
            this.state = undefined;
        }
        if (this.options) {
            this.options = undefined;
        }
    };
    ITable.arrowClass = 'i-arrow';
    ITable.actionClass = 'i-table-has-actions';
    ITable.resizeClass = 'i-table-resizable';
    ITable.sortHandleClass = 'i-table-sort-handle';
    ITable.thCellDivClass = 'i-th-cell-div';
    ITable.sortOnClass = 'on';
    ITable.sortOffClass = 'off';
    ITable.resizeHandleClass = 'i-table-resizable-handle';
    ITable.rootTmpl = '<div class="i-root-container"></div>';
    ITable.containerTmpl = '<div class="i-table-container"></div>';
    ITable.outerTmpl = '<div class="i-table-outer"></div>';
    ITable.innerTmpl = '<div class="i-table-inner"></div>';
    ITable.tableWraperTmpl = '<div class="i-table-wraper"></div>';
    ITable.tableTmpl = '<table class="table table-striped table-bordered i-table"></table>';
    ITable.colgroupTmpl = '<colgroup></colgroup>';
    ITable.colTmpl = '<col></col>';
    ITable.theadTmpl = '<thead></thead>';
    ITable.trTmpl = '<tr></tr>';
    ITable.thTmpl = '<th></th>';
    ITable.tbodyTmpl = '<tbody></tbody>';
    ITable.tdTmpl = '<td></td>';
    ITable.resizeHandleTmpl = "<span class=\"" + ITable.resizeHandleClass + "\"></span>";
    ITable.cellDivTmpl = '<div class="cell-div"></div>';
    ITable.thCellDivTmpl = "<div class=\"" + ITable.thCellDivClass + "\"></div>";
    ITable.upTmpl = "<i class=\"" + ITable.sortHandleClass + " up\">\n  <svg viewBox=\"0 0 1024 1024\"  width=\"1em\" height=\"1em\" fill=\"currentColor\" aria-hidden=\"true\"><path d=\"M858.9 689L530.5 308.2c-9.4-10.9-27.5-10.9-37 0L165.1 689c-12.2 14.2-1.2 35 18.5 35h656.8c19.7 0 30.7-20.8 18.5-35z\"></path></svg>\n  </i>";
    ITable.downTmpl = "<i class=\"" + ITable.sortHandleClass + " down\">\n  <svg viewBox=\"0 0 1024 1024\"  width=\"1em\" height=\"1em\" fill=\"currentColor\" aria-hidden=\"true\"><path d=\"M840.4 300H183.6c-19.7 0-30.7 20.8-18.5 35l328.4 380.8c9.4 10.9 27.5 10.9 37 0L858.9 335c12.2-14.2 1.2-35-18.5-35z\"></path></svg>\n  </i>";
    return ITable;
}(IBaseComponent));
var IFreezeHeadTable = (function (_super) {
    __extends(IFreezeHeadTable, _super);

    function IFreezeHeadTable($this, optionsParam) {
        return _super.call(this, $this, optionsParam) || this;
    }

    IFreezeHeadTable.prototype.initHtml = function ($this) {
        var options = this.options;
        var width = $this.width();
        var height = $this.height();
        var $headContainer = this.buildDom(IFreezeHeadTable.headContainerTmpl);
        var $headTable = this.buildDom(ITable.tableTmpl);
        var $headColgroup = this.buildDom(ITable.colgroupTmpl);
        var $headOuter = this.buildDom(ITable.outerTmpl);
        var $headInner = this.buildDom(ITable.innerTmpl);
        var $root = this.buildDom(ITable.rootTmpl);
        var $container = this.buildDom(IFreezeHeadTable.contentContainerTmpl);
        var $outer = this.buildDom(ITable.outerTmpl);
        var $inner = this.buildDom(ITable.innerTmpl);
        var $tableWraper = this.buildDom(ITable.tableWraperTmpl);
        var $table = this.buildDom(options.striped ? ITable.tableTmpl : ITable.tableTmpl.replace('table-striped', ''));
        var $colgroup = this.buildDom(ITable.colgroupTmpl);
        var $thead = this.buildDom(ITable.theadTmpl);
        var $tbody = this.buildDom(ITable.tbodyTmpl);
        this.state.height = height;
        this.state.width = width;
        this.state.$dom = {
            $origin: $this,
            $root: $root,
            $container: $container,
            $inner: $inner,
            $tableWraper: $tableWraper,
            $table: $table,
            $colgroup: $headColgroup,
            $thead: $thead,
            $tbody: $tbody,
        };
        if (typeof options.width === 'number') {
            $root.css('width', options.width + "px");
        }
        $this.empty();
        $this.append($root.get(0));
        $root.append($headContainer.get(0)).append($container.get(0));
        $headContainer.append($headOuter.get(0));
        $headOuter.append($headInner.get(0));
        $headInner.append($headTable.get(0));
        $container.append($outer.get(0));
        $outer.append($inner.get(0));
        $inner.append($tableWraper.get(0));
        $tableWraper.append($table.get(0));
        $headTable.append($headColgroup.get(0)).append($thead.get(0));
        $table.append($colgroup.get(0)).append($tbody.get(0));
        var $theadTr = this.buildDom(ITable.trTmpl);
        $thead.append($theadTr.get(0));
        for (var i = 0; i < options.columns.length; i++) {
            var column = options.columns[i];
            var $headCol = this.buildCol(column, i);
            var $col = this.buildCol(column, i);
            var $th = this.buildTh(column, i);
            $headColgroup.append($headCol.get(0));
            $colgroup.append($col.get(0));
            $theadTr.append($th.get(0));
        }
    };
    IFreezeHeadTable.prototype.getResizeDom = function () {
        var index = this.state.$dom.$resizingDom.index();
        var $col = this.state.$dom.$table.find("colgroup>col:eq(" + index + ")");
        var r = [this.state.$dom.$resizingDom, $col];
        if (this.detectIE()) {
            var index_1 = this.state.$dom.$resizingDom.index();
            var $th = this.state.$dom.$thead.find("th:eq(" + index_1.toString() + ")");
            r.push($th);
        }
        return r;
    };
    IFreezeHeadTable.headContainerTmpl = '<div class="i-table-head-container"></div>';
    IFreezeHeadTable.contentContainerTmpl = '<div class="i-table-content-container"></div>';
    return IFreezeHeadTable;
}(ITable));
var IFreezeColumnTable = (function (_super) {
    __extends(IFreezeColumnTable, _super);

    function IFreezeColumnTable($this, optionsParam) {
        var _this = _super.call(this) || this;
        _this.setOption(optionsParam, $this);
        return _this;
    }

    IFreezeColumnTable.prototype.setOption = function (optionsParam, $this) {
        var $thisRef = $this === undefined ? this.state.$dom.$origin : $this;
        this.destory(false);
        var defaults = {
            name: 'freezeColumnTable',
        };
        var options = $.extend(defaults, optionsParam);
        this.options = options;
        var _a = this.packageColumn(options.columns), currentSortColumnIndex = _a[0], currentSortDirection = _a[1];
        var _b = this.splitColumns(options), leftColumns = _b[0], rightColumns = _b[1];
        var leftWidth = 0;
        var rightWidth = 0;
        leftColumns.forEach(function (element) {
            leftWidth += typeof element.width === 'number' ? element.width : parseFloat(element.width);
        });
        rightColumns.forEach(function (element) {
            rightWidth += typeof element.width === 'number' ? element.width : parseFloat(element.width);
        });
        var stateData = this.buildStateData(options.data, currentSortColumnIndex, currentSortDirection);
        var _c = this.splitData(stateData, leftColumns, rightColumns), leftData = _c[0], rightData = _c[1];
        var leftOption = $.extend(false, {}, options);
        var rightOption = $.extend(false, {}, options);
        leftOption.name = 'left';
        leftOption.columns = leftColumns;
        leftOption.data = leftData;
        leftOption.width = leftWidth;
        leftOption.parentTable = this;
        leftOption.handleScroll = this.handleScrollTop.bind(this);
        leftOption.handleEnter = this.handleActiveTable.bind(this);
        leftOption.handleTbodyLeave = this.handleTbodyLeave.bind(this);
        leftOption.handleSort = this.handleSort.bind(this);
        if (typeof this.options.customHandleSort === 'function') {
            leftOption.customHandleSort = this.customHandleSort.bind(this);
        }
        leftOption.handleTdHover = this.handleTdHover.bind(this);
        leftOption.handleTdClick = this.handleTdClick.bind(this);
        leftOption.handleTdDblClick = this.handleTdDblClick.bind(this);
        rightOption.name = 'right';
        rightOption.columns = rightColumns;
        rightOption.data = rightData;
        rightOption.width = rightWidth;
        rightOption.parentTable = this;
        rightOption.handleScroll = this.handleScrollTop.bind(this);
        rightOption.handleEnter = this.handleActiveTable.bind(this);
        rightOption.handleTbodyLeave = this.handleTbodyLeave.bind(this);
        rightOption.handleSort = this.handleSort.bind(this);
        if (typeof this.options.customHandleSort === 'function') {
            rightOption.customHandleSort = this.customHandleSort.bind(this);
        }
        rightOption.handleTdHover = this.handleTdHover.bind(this);
        rightOption.handleTdClick = this.handleTdClick.bind(this);
        rightOption.handleTdDblClick = this.handleTdDblClick.bind(this);
        this.leftOptions = leftOption;
        this.rightOptions = rightOption;
        this.state = {
            width: null,
            height: null,
            isResizing: false,
            data: stateData,
            dataIndexMap: {},
            optionDataIndexMap: {}
        };
        for (var i = 0; i < this.options.data.length; i++) {
            var row = this.options.data[i];
            this.state.optionDataIndexMap[this.getRowId(row, this.options)] = [row, i];
        }
        for (var i = 0; i < this.state.data.length; i++) {
            var row = this.state.data[i];
            this.state.dataIndexMap[this.getRowId(row, this.options)] = [row, i];
        }
        this.initHtml($thisRef);
    };
    IFreezeColumnTable.prototype.getOption = function () {
        return this.options;
    };
    IFreezeColumnTable.prototype.getState = function () {
        return this.state;
    };
    // 设置分页组件
    IFreezeColumnTable.prototype.setPagination = function (optionsParam) {
        console.log(optionsParam, 'optionsParam');

    };
    IFreezeColumnTable.prototype.initHtml = function ($this) {
        var width = $this.width();
        var height = $this.height();
        var $row = this.buildDom(IFreezeColumnTable.rowTmpl);
        var $left = this.buildDom(IFreezeColumnTable.leftTmpl);
        var $right = this.buildDom(IFreezeColumnTable.rightTmpl);
        this.state.height = height;
        this.state.width = width;
        this.state.$dom = {
            $origin: $this,
            $root: $row,
            $container: null,
            $inner: null,
            $table: null,
            $colgroup: null,
            $thead: null,
            $tbody: null,
        };
        this.$left = $left;
        $this.empty();
        $this.append($row.get(0));
        $row.append($left.get(0)).append($right.get(0));
        $left.css('width', this.leftOptions.width + "px");
        $right.css('paddingLeft', this.leftOptions.width + "px");
        this.leftTable = this.leftOptions.freezeHead ?
            new IFreezeHeadTable($this.find('.i-left'), this.leftOptions)
            : new ITable($this.find('.i-left'), this.leftOptions);
        this.rightTable = this.rightOptions.freezeHead ?
            new IFreezeHeadTable($this.find('.i-right'), this.rightOptions)
            : new ITable($this.find('.i-right'), this.rightOptions);
        $right.on('scroll', this.handleScrollRight.bind(this));
    };
    IFreezeColumnTable.prototype.splitColumns = function (options) {
        var leftColumns = [];
        var rightColumns = [];
        for (var i = 0; i < options.columns.length; i++) {
            var element = options.columns[i];
            if (element.isFrozen) {
                leftColumns.push(element);
            }
            else {
                rightColumns.push(element);
            }
        }
        return [leftColumns, rightColumns];
    };
    IFreezeColumnTable.prototype.splitData = function (data, leftColumns, rightColumns) {
        var leftData = [];
        var rightData = [];
        for (var i = 0; i < data.length; i++) {
            var row = data[i];
            leftData.push(row);
            rightData.push(row);
        }
        return [leftData, rightData];
    };
    IFreezeColumnTable.prototype.getRealCellIndex = function (cellIndex) {
        if (this.activeTableName === 'left') {
            return cellIndex;
        }
        return this.leftOptions.columns.length + cellIndex;
    };
    IFreezeColumnTable.prototype.updateOptionData = function (row) {
        var rowId = this.getRowId(row, this.options);
        var optionRowData = this.findRow(this.options.data, this.options, rowId, this.state.optionDataIndexMap);
        if (optionRowData === null) {
            console.log('not find:', row);
            return;
        }
        var optionRow = optionRowData[0];
        var _a = this.findRow(this.state.data, this.options, rowId, this.state.dataIndexMap), stateRow = _a[0],
            i = _a[1];
        var currentRowKeys = Object.keys(optionRow);
        for (var i_1 = 0; i_1 < currentRowKeys.length; i_1++) {
            var key = currentRowKeys[i_1];
            if (row[key] !== undefined) {
                optionRow[key] = row[key];
                stateRow[key] = row[key];
            }
        }
        this.leftTable.updateOptionData(row);
        this.rightTable.updateOptionData(row);
    };
    IFreezeColumnTable.prototype.replaceOptionData = function (data) {
        this.options.data = data;
        var stateData = this.buildStateData(this.options.data, this.state.currentSortColumnIndex, this.state.currentSortDirection);
        this.state.data = stateData;
        var _a = this.splitColumns(this.options), leftColumns = _a[0], rightColumns = _a[1];
        var _b = this.splitData(this.options.data, leftColumns, rightColumns), leftData = _b[0], rightData = _b[1];
        this.leftTable.replaceOptionData(leftData);
        this.rightTable.replaceOptionData(rightData);
    };
    IFreezeColumnTable.prototype.appendOptionData = function (row) {
        var _a, _b;
        (_a = this.options.data).push.apply(_a, row);
        (_b = this.state.data).push.apply(_b, row);
        this.leftTable.appendOptionData(row);
        this.rightTable.appendOptionData(row);
    };
    IFreezeColumnTable.prototype.prependOptionData = function (row) {
        var _a, _b;
        (_a = this.options.data).splice.apply(_a, [0, 0].concat(row));
        (_b = this.state.data).splice.apply(_b, [0, 0].concat(row));
        this.leftTable.prependOptionData(row);
        this.rightTable.prependOptionData(row);
    };
    IFreezeColumnTable.prototype.deleteOptionData = function (ids) {
        if (ids.indexOf(this.state.lastClickRowId) >= 0) {
            this.state.lastClickRowId = undefined;
        }
        if (ids.indexOf(this.state.lastLockedRowId) >= 0) {
            this.state.lastLockedRowId = undefined;
        }
        for (var i = 0; i < ids.length; i++) {
            var id = ids[i];
            var _a = this.findRow(this.options.data, this.options, id, this.state.optionDataIndexMap),
                optionIndex = _a[1];
            this.options.data.splice(optionIndex, 1);
            var _b = this.findRow(this.state.data, this.options, id, this.state.dataIndexMap), stateIndex = _b[1];
            this.state.data.splice(stateIndex, 1);
        }
        this.leftTable.deleteOptionData(ids);
        this.rightTable.deleteOptionData(ids);
    };
    IFreezeColumnTable.prototype.setActiveRow = function (id) {
        if (this.state.lastClickRowId === id) {
            this.state.lastClickRowId = undefined;
        }
        else {
            this.state.lastClickRowId = id;
        }
        this.leftTable.setActiveRow(id);
        this.rightTable.setActiveRow(id);
    };
    IFreezeColumnTable.prototype.setLockedRow = function (id) {
        if (this.state.lastLockedRowId === id) {
            this.state.lastLockedRowId = undefined;
        }
        else {
            this.state.lastLockedRowId = id;
        }
        this.leftTable.setLockedRow(id);
        this.rightTable.setLockedRow(id);
    };
    IFreezeColumnTable.prototype.updateStateData = function (data) {
        this.state.data = this.buildStateData(data, this.state.currentSortColumnIndex, this.state.currentSortDirection);
        var _a = this.splitColumns(this.options), leftColumns = _a[0], rightColumns = _a[1];
        var _b = this.splitData(this.state.data, leftColumns, rightColumns), leftData = _b[0], rightData = _b[1];
        this.leftTable.updateStateData(leftData);
        this.rightTable.updateStateData(rightData);
    };
    IFreezeColumnTable.prototype.buildStateData = function (data, sortColumnIndex, sortDirection) {
        var finalData = this.shadowCopyArray(data);
        finalData = this.sortData(finalData, this.options.columns, sortColumnIndex, sortDirection);
        return finalData;
    };
    IFreezeColumnTable.prototype.handleSort = function (sortColumnIndex, sortDirection) {
        var data = this.options.data;
        var _a = this.splitColumns(this.options), leftColumns = _a[0], rightColumns = _a[1];
        var finalData = this.shadowCopyArray(data);
        var index = undefined;
        if (sortColumnIndex !== undefined) {
            index = this.activeTableName === 'left' ? sortColumnIndex : leftColumns.length + sortColumnIndex;
        }
        finalData = this.sortData(finalData, this.options.columns, index, sortDirection);
        var _b = this.splitData(finalData, leftColumns, rightColumns), leftData = _b[0], rightData = _b[1];
        this.state.currentSortColumnIndex = index;
        this.state.currentSortDirection = sortDirection;
        if (this.activeTableName === 'left') {
            this.rightTable.updateStateSort(undefined, undefined);
        }
        else {
            this.leftTable.updateStateSort(undefined, undefined);
        }
        this.leftTable.updateStateData(leftData);
        this.rightTable.updateStateData(rightData);
        if (typeof this.options.handleSort === 'function') {
            this.options.handleSort(sortColumnIndex, sortDirection);
        }
    };
    IFreezeColumnTable.prototype.customHandleSort = function (sortColumnIndex, sortDirection) {
        var data = this.options.data;
        var _a = this.splitColumns(this.options), leftColumns = _a[0], rightColumns = _a[1];
        var index = undefined;
        if (sortColumnIndex !== undefined) {
            index = this.activeTableName === 'left' ? sortColumnIndex : leftColumns.length + sortColumnIndex;
        }
        if (typeof this.options.customHandleSort === 'function') {
            this.options.customHandleSort(sortColumnIndex, sortDirection);
        }
    };
    IFreezeColumnTable.prototype.handleTdClick = function (rowId, cellIndex, $td) {
        if (this.options.clickMeansActive) {
            if (!(this.state.lastClickRowId === rowId && !this.options.cancelActiveRow)) {
                if (this.activeTableName === 'left') {
                    this.rightTable.handleTdClickDomOpe(rowId, cellIndex);
                }
                else {
                    this.leftTable.handleTdClickDomOpe(rowId, cellIndex);
                }
            }
        }
        if (typeof this.options.handleTdClick === 'function') {
            this.options.handleTdClick(rowId, this.getRealCellIndex(cellIndex), $td);
        }
        this.state.lastClickRowId = rowId;
    };
    IFreezeColumnTable.prototype.handleTdDblClick = function (rowId, cellIndex, $td) {
        this.state.lastLockedRowId = rowId;
        if (this.options.dblClickMeansLock) {
            if (this.activeTableName === 'left') {
                this.rightTable.handleTdDblClickDomOpe(rowId, cellIndex);
            }
            else {
                this.leftTable.handleTdDblClickDomOpe(rowId, cellIndex);
            }
        }
        if (typeof this.options.handleTdDblClick === 'function') {
            this.options.handleTdDblClick(rowId, this.getRealCellIndex(cellIndex), $td);
        }
    };
    IFreezeColumnTable.prototype.handleTdHover = function (rowIndex, cellIndex, $td) {
        this.rightTable.handleTdHoverDomOpe(rowIndex, cellIndex);
        this.leftTable.handleTdHoverDomOpe(rowIndex, cellIndex);
        if (typeof this.options.handleTdHover === 'function') {
            this.options.handleTdHover(rowIndex, this.getRealCellIndex(cellIndex), $td);
        }
    };
    IFreezeColumnTable.prototype.handleActiveTable = function (name) {
        this.activeTableName = name;
    };
    IFreezeColumnTable.prototype.handleScrollTop = function (scrollTop) {
        if (this.activeTableName === 'left') {
            this.rightTable.updateScrollTop(scrollTop);
        }
        else {
            this.leftTable.updateScrollTop(scrollTop);
        }
        if (typeof this.options.handleScroll === 'function') {
            this.options.handleScroll(scrollTop);
        }
    };
    IFreezeColumnTable.prototype.handleTbodyLeave = function (event) {
        this.leftTable.handleTdHoverDomOpe(undefined, undefined);
        this.rightTable.handleTdHoverDomOpe(undefined, undefined);
    };
    IFreezeColumnTable.prototype.handleScrollRight = function (event) {
        var scrollRight = $(event.target).scrollLeft();
        if (scrollRight > 0) {
            if (!this.attachedClass2LeftTable) {
                this.attachedClass2LeftTable = true;
                this.$left.addClass(IFreezeColumnTable.leftShadowClass);
            }
        }
        else {
            this.attachedClass2LeftTable = false;
            this.$left.removeClass(IFreezeColumnTable.leftShadowClass);
        }
    };
    IFreezeColumnTable.prototype.updateScrollTop = function (scrollTop, duration) {
        this.rightTable.updateScrollTop(scrollTop);
        this.leftTable.updateScrollTop(scrollTop);
    };
    IFreezeColumnTable.prototype.render = function () {
        this.leftTable.render();
        this.rightTable.render();
    };
    IFreezeColumnTable.prototype.getDataLength = function () {
        if (!this.state || !this.state.data) {
            return 0;
        }
        return this.state.data.length;
    };
    IFreezeColumnTable.prototype.destory = function (withChild) {
        if (this.state) {
            this.state.$dom.$origin.empty();
            this.state.$dom = undefined;
            this.state = undefined;
        }
        if (withChild === true && this.leftTable) {
            this.leftTable.destory();
            this.rightTable.destory();
            this.$left = undefined;
        }
        if (this.options) {
            this.options = undefined;
        }
    };
    IFreezeColumnTable.leftShadowClass = 'i-table-scroll-middle';
    IFreezeColumnTable.rowTmpl = '<div class="i-row"></div>';
    IFreezeColumnTable.leftTmpl = '<div class="i-left"></div>';
    IFreezeColumnTable.rightTmpl = '<div class="i-right"></div>';
    return IFreezeColumnTable;
}(IBaseComponent));
;
(function ($) {
    $.fn.extend({
        itable: function (options) {
            if (!options.columns || options.columns.length === 0) {
                throw new Error("列定义不能为空");
            }
            if (options.freezeColumn || options.freezeHead) {
                var hasFrozenColumn = false;
                for (var i = 0; i < options.columns.length; i++) {
                    var element = options.columns[i];
                    if (typeof element.width !== 'number') {
                        throw new Error("当使用固定列或者固定头部时，每列的宽度必须指定为具体的像素值");
                    }
                    if (hasFrozenColumn === false && element.isFrozen === true) {
                        hasFrozenColumn = true;
                    }
                    if (options.freezeColumn && element.isSequence && !element.isFrozen) {
                        throw new Error('当使用固定列的同时包含序号列时，序号列也必须为固定列');
                    }
                }
                if (options.freezeColumn && hasFrozenColumn === false) {
                    throw new Error("当使用固定列时，应该将需要固定的列的 isFrozen 属性设置为 true");
                }
            }
            var hasSortColumn = false;
            for (var i = 0; i < options.columns.length; i++) {
                var element = options.columns[i];
                if (element.defaultSortOrder) {
                    if (hasSortColumn === false) {
                        hasSortColumn = true;
                    }
                    else {
                        throw new Error("只能有一个列为默认排序列");
                    }
                }
            }
            if (options.freezeColumn === true) {
                return new IFreezeColumnTable(this, options);
            }
            if (options.freezeHead === true) {
                return new IFreezeHeadTable(this, options);
            }
            return new ITable(this, options);
        }
    });
}(jQuery));
