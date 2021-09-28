function toFixed(source, digit, omitZero) {
    var sourceIn = source;
    if (typeof sourceIn !== 'number') {
        try {
            sourceIn = parseFloat(sourceIn);
        }
        catch (error) {
            return 0;
        }
    }
    if (sourceIn === null || sourceIn === undefined || isNaN(sourceIn)) {
        return 0;
    }
    var afterFixed = sourceIn.toFixed(digit);
    if (omitZero) {
        afterFixed = parseFloat(afterFixed);
    }
    return afterFixed;
}
function tryParseFloat(source, defaultValue) {
    if (defaultValue === void 0) { defaultValue = 0; }
    var r = parseFloat(source);
    if (!isNaN(r)) {
        return r;
    }
    return defaultValue;
}
function isEmpty(obj) {
    if (obj instanceof Date) {
        return false;
    }
    if (obj === undefined || obj === null) {
        return true;
    }
    if (typeof obj === 'object') {
        return Object.keys(obj).length === 0 || obj.length === 0;
    }
    return false;
}
function getHourFromSecond(second) {
    var hourSecond = 60 * 60;
    var hour = second / hourSecond;
    hour = toFixed(hour, 1, true);
    return hour;
}
function getMinuteFromSecond(second) {
    var minuteSecond = 60;
    var minute = second / minuteSecond;
    minute = toFixed(minute, 1, true);
    return minute;
}
function get2DimensionArray(array, radix) {
    var arr = array;
    var newArr = [];
    while (arr.length)
        newArr.push(arr.splice(0, radix));
    return newArr;
}
function printKeys(obj) {
    if (isEmpty(obj)) {
    }
    else {
    }
}
function isChineseChar(str) {
    var reg = /[^/u4e00\-/u9fa5]/;
    return reg.test(str);
}
function getNow(s) {
    return s < 10 ? "0" + s : s;
}
function getCurrentTime(type) {
    var myDate = new Date();
    var year = myDate.getFullYear();
    var month = myDate.getMonth() + 1;
    var date = myDate.getDate();
    var h = myDate.getHours();
    var m = myDate.getMinutes();
    var s = myDate.getSeconds();
    var now;
    if (type === 0) {
        now = year + "-" + getNow(month) + "-" + getNow(date) + " " + getNow(h) + ":" + getNow(m) + ":" + getNow(s);
    }
    else if (type === 1) {
        now = year + "-" + getNow(month) + "-" + getNow(date);
    }
    else if (type === 2) {
        now = getNow(h) + ":" + getNow(m) + ":" + getNow(s);
    }
    return now;
}
function GetDateStr(day, AddDayCount) {
    var dd = new Date(day);
    dd.setDate(dd.getDate() + AddDayCount);
    var y = dd.getFullYear();
    var m = dd.getMonth() + 1;
    var d = dd.getDate();
    return getNow(y) + "-" + getNow(m) + "-" + getNow(d);
}
function formateDate(date, split, type) {
    var arr = date.split(split);
    var newDate = '';
    if (type === 0) {
        newDate = arr[0] + "\u5E74" + parseInt(arr[1], 10) + "\u6708" + parseInt(arr[2], 10) + "\u65E5";
    }
    else if (type === 1) {
        newDate = arr[0] + "\u5E74" + parseInt(arr[1], 10) + "\u6708";
    }
    else {
        newDate = parseInt(arr[1], 10) + "\u6708" + parseInt(arr[2], 10) + "\u65E5";
    }
    return newDate;
}
function range(start, end, step) {
    var arr = [];
    for (var i = start; i < end; i += step) {
        arr.push(i);
    }
    return arr;
}
var AutoConnectionEnum;
(function (AutoConnectionEnum) {
    AutoConnectionEnum["AFTER"] = "AFTER";
    AutoConnectionEnum["BEFORE"] = "BEFORE";
    AutoConnectionEnum["NONE"] = "NONE";
})(AutoConnectionEnum || (AutoConnectionEnum = {}));
var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
var LineChart = (function () {
    function LineChart(selector, optionsParam) {
        this.getPathPartition = function (serie) {
            var total = [];
            var partition = [];
            var serieColor = serie.color;
            var prevColor = serie.data.length > 0 && serie.data[0].color && serie.data[0].color.length > 0
                ? serie.data[0].color : serieColor;
            serie.data.forEach(function (item, index) {
                if (item.color && item.color.length > 0 && item.color != prevColor) {
                    total.push({
                        data: [].concat(partition),
                        color: prevColor,
                    });
                    partition = [item];
                    if (serie.autoConnectPartition === 'BEFORE' && total.length > 0) {
                        var lastPartition = total[total.length - 1].data;
                        if (lastPartition.length > 0) {
                            var lastItem = lastPartition[lastPartition.length - 1];
                            partition.splice(0, 0, lastItem);
                            if (index == serie.data.length - 1) {
                                total.push({
                                    data: [].concat(partition),
                                    color: item.color,
                                });
                            }
                        }
                    }
                    else if (serie.autoConnectPartition === 'AFTER' && total.length > 0) {
                        var lastPartition = total[total.length - 1].data;
                        if (lastPartition.length > 0) {
                            lastPartition.push(item);
                        }
                    }
                    prevColor = item.color;
                }
                else {
                    partition.push(item);
                    if (index == serie.data.length - 1) {
                        total.push({
                            data: [].concat(partition),
                            color: prevColor,
                        });
                    }
                }
            });
            return total;
        };
        this.getYValue4Serie = function (xValue, serie) {
            var yValue = null;
            if (serie.data && serie.data.length > 0) {
                for (var i = 0; i < serie.data.length; i++) {
                    var item = serie.data[i];
                    if (item.index === xValue) {
                        yValue = item;
                        break;
                    }
                }
            }
            return yValue;
        };
        this.setOption(optionsParam, selector);
    }
    LineChart.prototype.setOption = function (optionsParam, selector) {
        var defaults = {
            series: [],
            axisFontSize: 12,
            padding: {
                top: 30,
                bottom: 20,
                left: 15,
                right: 15,
            },
            playIndex: 0,
            axisColor: '#999',
            axisWidth: 1,
            snap: true,
            xAxisInterval: 0.1,
            rectWidth: 80,
            rectHeight: 36,
        };
        var options = $.extend(defaults, optionsParam);
        this.options = options;
        this.state = {
            selector: selector,
            data: this.buildStateData(options.series),
            width: 0,
            height: 0,
            xScale: null,
            yScale: null,
            xAxis: null,
            yAxis1: null,
            yAxis2: null,
            x1: 0,
            y1: null,
            x2: null,
            y2: null,
            xMax: null,
            xMaxText: null,
            xMin: null,
            yMax: null,
            yMin: null,
            timeFormater: LineChart.timeFormater,
            paths: null,
            linePaths: null,
            dotts: null,
            yValue: [],
            currentXText: null,
            rectColor: '#30C100',
        };
        this.initHtml();
    };
    LineChart.prototype.getOption = function () {
        return this.options;
    };
    LineChart.prototype.getState = function () {
        return this.state;
    };
    LineChart.prototype.initHtml = function () {
        var _this = this;
        this.state.$dom = {
            $origin: $(this.state.selector),
        };
        this.state.$dom.$origin.empty();
        this.state.width = this.state.$dom.$origin.width();
        this.state.height = this.state.$dom.$origin.height();
        var _a = this.state, height = _a.height, width = _a.width;
        var _b = this.options, data = _b.series, padding = _b.padding, rectColorFunc = _b.rectColorFunc, playIndex = _b.playIndex;
        var extent = data.map(function (serie) {
            var minAndMax = d3.extent(serie.data.filter(function (x) { return x.value !== null; }), function (x) { return x.value; });
            return {
                serieXExtent: [0, serie.data.length - 1],
                serieYExtent: [
                    isEmpty(serie.yMinValue) ? minAndMax[0] : serie.yMinValue,
                    isEmpty(serie.yMaxValue) ? minAndMax[1] : serie.yMaxValue,
                ],
            };
        });
        var xMin = d3.min(extent, function (x) { return x.serieXExtent[0]; });
        var xMax = d3.max(extent, function (x) { return x.serieXExtent[1]; });
        if (xMin === xMax) {
            xMin = -1;
            xMax = 1;
        }
        var yMin = extent.map(function (x) { return (!x.serieYExtent[0] ? 0 : x.serieYExtent[0]); });
        var yMax = extent.map(function (x) { return (!x.serieYExtent[1] ? 0 : x.serieYExtent[1]); });
        for (var i = 0; i < yMin.length; i += 1) {
            if (yMin[i] === yMax[i]) {
                if (yMin[i] === 0) {
                    yMax[i] += 1;
                }
                else {
                    yMin[i] -= 1;
                    yMax[i] += 1;
                }
            }
        }
        var xScale = d3.scaleLinear()
            .domain([xMin, xMax])
            .range([0 + padding.left, width - padding.right]);
        var yScale = yMin.map(function (x, i) { return d3.scaleLinear()
            .domain([yMin[i], yMax[i]])
            .range([height - padding.bottom, 0 + padding.top]); });
        var xAxisData = [
            { x: xMin, y: yMin[0] },
            { x: xMax, y: yMin[0] },
        ];
        var yAxisData1 = [
            { x: xMin, y: yMin[0] },
            { x: xMin, y: yMax[0] },
        ];
        var xMaxValue = this.getXvalueFromPlayIndex(xMax);
        var currentXValue = this.getXvalueFromPlayIndex(playIndex);
        var xMaxText = '';
        if (xMaxValue !== undefined && xMaxValue !== null
            && xMaxValue.date !== null && xMaxValue.date > 0) {
            xMaxText = LineChart.timeFormater(xMaxValue.date);
        }
        var currentXText = '';
        if (currentXValue !== undefined && currentXValue !== null
            && currentXValue.date !== null && currentXValue.date > 0) {
            currentXText = LineChart.timeFormater(currentXValue.date);
        }
        var xAxis = d3.line()
            .x(function (d) { return xScale(d.x) + 0.5; })
            .y(function (d) { return yScale[0](d.y) + 0.5; })(xAxisData);
        var yAxis1 = d3.line()
            .x(function (d) { return xScale(d.x) + 0.5; })
            .y(function (d) { return yScale[0](d.y) + 0.5; })(yAxisData1);
        var yAxis2;
        if (data.length === 2) {
            var yAxisData2 = [
                { x: xMax, y: yMin[1] },
                { x: xMax, y: yMax[1] },
            ];
            yAxis2 = d3.line()
                .x(function (d) { return xScale(d.x) + 0.5; })
                .y(function (d) { return yScale[1](d.y) + 0.5; })(yAxisData2);
        }
        var paths = [];
        var linePaths = [];
        var dotts = [];
        data.forEach(function (serie, index) {
            var pathPartition = _this.getPathPartition(serie);
            pathPartition.forEach(function (partition) {
                var path = d3.line()
                    .x(function (d) { return xScale(d.index); })
                    .y(function (d) { return yScale[index](d.value); })(partition.data);
                paths.push({
                    path: path,
                    width: serie.width,
                    color: partition.color,
                });
            });
            if (serie.line && serie.line.length > 0) {
                linePaths = linePaths.concat(serie.line.map(function (lineItem) { return (__assign({ x1: xScale(xMin), y1: yScale[index](lineItem.value), x2: xScale(xMax), text: lineItem.value.toString() }, lineItem)); }));
            }
            if (serie.dott && serie.dott.length > 0) {
                serie.dott.forEach(function (dot) {
                    var x = xScale(dot.x);
                    var yValue = _this.getYValue4Serie(dot.x, serie);
                    var y = yScale[index](yValue.value);
                    dotts.push({
                        x: x,
                        y: y,
                        color: dot.color,
                    });
                });
            }
        });
        var _c = this.state, x1 = _c.x1, rectColor = _c.rectColor;
        x1 = xScale(playIndex);
        var y1 = yScale[0](yMax[0]);
        var x2 = x1;
        var y2 = yScale[0](yMin[0]);
        var yValue = this.getYaxisValue(playIndex).data;
        if (typeof rectColorFunc === 'function') {
            var colorFromFunc = rectColorFunc(currentXValue);
            if (colorFromFunc !== null) {
                rectColor = colorFromFunc;
            }
        }
        this.setState({
            width: width,
            height: height,
            xScale: xScale,
            yScale: yScale,
            xAxis: xAxis,
            yAxis1: yAxis1,
            yAxis2: yAxis2,
            x1: x1,
            y1: y1,
            x2: x2,
            y2: y2,
            xMax: xMax,
            xMaxText: xMaxText,
            xMin: xMin,
            yMax: yMax,
            yMin: yMin,
            paths: paths,
            linePaths: linePaths,
            dotts: dotts,
            currentXText: currentXText,
            yValue: yValue,
            rectColor: rectColor,
        });
        this.render();
    };
    LineChart.prototype.render = function () {
        var _a = this.options, data = _a.series, axisFontSize = _a.axisFontSize, axisColor = _a.axisColor, axisWidth = _a.axisWidth, padding = _a.padding, rectWidth = _a.rectWidth, rectHeight = _a.rectHeight;
        var _b = this.state, width = _b.width, height = _b.height, xScale = _b.xScale, yScale = _b.yScale, xAxis = _b.xAxis, yAxis1 = _b.yAxis1, yAxis2 = _b.yAxis2, x1 = _b.x1, y1 = _b.y1, x2 = _b.x2, y2 = _b.y2, xMax = _b.xMax, xMaxText = _b.xMaxText, xMin = _b.xMin, yMax = _b.yMax, yMin = _b.yMin, paths = _b.paths, linePaths = _b.linePaths, dotts = _b.dotts, currentXText = _b.currentXText, yValue = _b.yValue, rectColor = _b.rectColor;
        var mouseDownHandler = this.mouseDown.bind(this);
        this.state.$dom.$svg = d3.select(this.state.selector).append("svg")
            .attr("width", this.state.width)
            .attr("height", this.state.height)
            .on("mousedown", function () {
                var coords = d3.mouse(this);
                mouseDownHandler(coords[0], coords[1]);
            });
        this.drawAxis(xAxis, axisColor, axisWidth, xScale, xMax, yScale, yMin, axisFontSize, xMaxText, yAxis1, xMin, yMax, data, yAxis2);
        this.drawPaths(paths);
        this.drawDotts(dotts);
        this.drawLines(linePaths, axisColor, axisFontSize);
        this.drawIndicator();
    };
    LineChart.prototype.mouseDown = function (x, y) {
        this.state.prevX = x;
        this.state.isDragging = true;
        var documentMouseMoveHandler = this.mouseMove.bind(this);
        this.state.documentMouseMoveHandler = documentMouseMoveHandler;
        var documentMouseUpHandler = this.mouseUp.bind(this);
        this.state.documentMouseUpHandler = documentMouseUpHandler;
        d3.select('body')
            .on('mouseup', function () {
                documentMouseUpHandler();
            })
            .on('mousemove', function () {
                documentMouseMoveHandler();
            });
        this.handleOnDrag(x);
    };
    LineChart.prototype.mouseMove = function () {
        d3.event.preventDefault();
        var coords = d3.mouse(this.state.$dom.$svg.node());
        this.handleOnDrag(coords[0]);
    };
    LineChart.prototype.mouseUp = function (x, y) {
        if (this.state.isDragging) {
            d3.select('body')
                .on('mousemove', null);
            this.state.isDragging = false;
            var coords = d3.mouse(this.state.$dom.$svg.node());
            this.handleOnDragEnd(coords[0]);
        }
    };
    LineChart.prototype.handleOnDrag = function (x) {
        var _a = this.options, _b = _a.padding, left = _b.left, right = _b.right, onDrag = _a.onDrag, rectColorFunc = _a.rectColorFunc;
        var _c = this.state, width = _c.width, xScale = _c.xScale, timeFormater = _c.timeFormater;
        var rectColor = this.state.rectColor;
        var newX1 = Math.max(x, left);
        newX1 = Math.min(newX1, width - right);
        var xIndex = xScale.invert(newX1);
        var xValue = this.getXvalueFromPlayIndex(xIndex);
        var yValue = this.getYaxisValue(xIndex);
        var currentXText = '';
        if (xValue !== undefined && xValue !== null && xValue.date !== null && xValue.date > 0) {
            currentXText = timeFormater(xValue.date);
            if (typeof rectColorFunc === 'function') {
                var colorFromFunc = rectColorFunc(xValue);
                if (colorFromFunc !== null) {
                    rectColor = colorFromFunc;
                }
            }
        }
        this.setState({
            x1: newX1,
            x2: newX1,
            currentXText: currentXText,
            yValue: yValue.data,
            rectColor: rectColor,
        });
        this.drawIndicator();
        if (typeof onDrag === 'function' && yValue.hasYValue === true) {
            onDrag({
                xValue: xValue.index,
                playIndex: xValue.index,
            });
        }
    };
    LineChart.prototype.handleOnDragEnd = function (x) {
        var _a = this.options, _b = _a.padding, left = _b.left, right = _b.right, onDragEnd = _a.onDragEnd, snap = _a.snap, rectColorFunc = _a.rectColorFunc;
        var _c = this.state, width = _c.width, xScale = _c.xScale, timeFormater = _c.timeFormater;
        var rectColor = this.state.rectColor;
        var newX1 = Math.max(x, left);
        newX1 = Math.min(newX1, width - right);
        var xIndex = Math.max(0, xScale.invert(newX1));
        if (snap === true) {
            xIndex = this.getClosestXvalue(xIndex);
            newX1 = xScale(xIndex);
        }
        var yValue = this.getYaxisValue(xIndex);
        var xValue = this.getXvalueFromPlayIndex(xIndex);
        var currentXText = '';
        if (xValue.date !== null && xValue.date > 0) {
            currentXText = timeFormater(xValue.date);
            if (typeof rectColorFunc === 'function') {
                var colorFromFunc = rectColorFunc(xValue);
                if (colorFromFunc !== null) {
                    rectColor = colorFromFunc;
                }
            }
        }
        this.setState({
            x1: newX1,
            x2: newX1,
            currentXText: currentXText,
            yValue: yValue.data,
            rectColor: rectColor,
        });
        this.drawIndicator();
        if (typeof onDragEnd === 'function' && yValue.hasYValue === true) {
            onDragEnd({
                xValue: xValue.index,
                playIndex: xValue.index,
            });
        }
    };
    LineChart.prototype.drawIndicator = function () {
        var _a = this.options, data = _a.series, axisFontSize = _a.axisFontSize, padding = _a.padding, rectWidth = _a.rectWidth, rectHeight = _a.rectHeight;
        var _b = this.state, width = _b.width, x1 = _b.x1, y1 = _b.y1, x2 = _b.x2, y2 = _b.y2, currentXText = _b.currentXText, yValue = _b.yValue, rectColor = _b.rectColor;
        var halfRectWidth = rectWidth / 2;
        var topRectX = x1 > rectWidth / 2 ? x1 - (rectWidth / 2) : 0;
        var topTextX = x1 > rectWidth / 2 ? x1 - (rectWidth / 2) + 2 : 2;
        var bottomRectX = x1 > rectWidth / 2 ? x1 - (rectWidth / 2) : 0;
        var bottomTextX = x1 > rectWidth / 2 ? x1 - (rectWidth / 2) + 17 : 17;
        if (x1 <= halfRectWidth) {
            topRectX = 0;
            topTextX = 2;
            bottomRectX = 0;
            bottomTextX = 18;
        }
        else if (x1 > halfRectWidth && x1 < (width - halfRectWidth)) {
            topRectX = x1 - (halfRectWidth);
            topTextX = x1 - (halfRectWidth) + 2;
            bottomRectX = x1 - (halfRectWidth);
            bottomTextX = x1 - (halfRectWidth) + 18;
        }
        else {
            var maxDistance = width - rectWidth;
            topRectX = maxDistance;
            topTextX = maxDistance + 2;
            bottomRectX = maxDistance;
            bottomTextX = maxDistance + 18;
        }
        var isExist = this.state.$dom.$svg.select('.indicator');
        this.state.$dom.$svg.select('.indicator').remove();
        var g = this.state.$dom.$svg.append("g").attr('class', 'indicator');
        g.append("line")
            .attr("x1", x1)
            .attr('y1', y1 + 23 - (40 - rectHeight))
            .attr('x2', x2)
            .attr("y2", y2)
            .attr('stroke', '#30C100')
            .attr('stroke-width', '1');
        g.append("rect")
            .attr("fill", rectColor)
            .attr('width', rectWidth)
            .attr("height", rectHeight)
            .attr('x', topRectX)
            .attr('y', y1 - padding.top / 2)
            .attr('rx', 3)
            .attr('ry', 3)
            .attr('opacity', 0.7);
        for (var index = 0; index < yValue.length; index++) {
            var yValueItem = yValue[index];
            for (var itemIndex = 0; itemIndex < yValueItem.formattedText.split('\n').length; itemIndex++) {
                var itemText = yValueItem.formattedText.split('\n')[itemIndex];
                g.append("text")
                    .attr("x", topTextX)
                    .attr('y', y1 + (axisFontSize + 2) * (index + itemIndex))
                    .attr('fill', 'white')
                    .attr("font-size", axisFontSize - 1)
                    .text(itemText);
            }
        }
        g.append("rect")
            .attr("fill", '#30C100')
            .attr('width', rectWidth)
            .attr("height", 15)
            .attr('x', bottomRectX)
            .attr('y', y2 + 1)
            .attr('rx', 3)
            .attr('ry', 3)
            .attr('opacity', 0.7);
        g.append("text")
            .attr("x", bottomTextX)
            .attr('y', y2 + 12)
            .attr('fill', 'white')
            .attr("font-size", axisFontSize - 1)
            .text(currentXText);
    };
    LineChart.prototype.drawLines = function (linePaths, axisColor, axisFontSize) {
        var g = this.state.$dom.$svg.append("g");
        for (var i = 0; i < linePaths.length; i++) {
            var lineItem = linePaths[i];
            g.append("line")
                .attr("x1", lineItem.x1)
                .attr('y1', lineItem.y1)
                .attr('x2', lineItem.x2)
                .attr("y2", lineItem.y1)
                .attr('stroke', lineItem.color);
            g.append("text")
                .attr("x", lineItem.x1 - 12)
                .attr('y', lineItem.y1)
                .attr('fill', axisColor)
                .attr("font-size", axisFontSize - 1)
                .text(lineItem.text);
        }
    };
    LineChart.prototype.drawDotts = function (dotts) {
        var g = this.state.$dom.$svg.append("g");
        for (var i = 0; i < dotts.length; i++) {
            var dot = dotts[i];
            g.append("rect")
                .attr("x", dot.x - 2)
                .attr('y', dot.y - 2)
                .attr('width', 5)
                .attr("height", 5)
                .attr('stroke', 'darkgray')
                .attr('fill', dot.color);
        }
    };
    LineChart.prototype.drawPaths = function (paths) {
        var g = this.state.$dom.$svg.append("g");
        for (var i = 0; i < paths.length; i++) {
            var onePath = paths[i];
            g.append("path")
                .datum(onePath.data)
                .attr("fill", "none")
                .attr('stroke', onePath.color)
                .attr('stroke-width', onePath.width)
                .attr("d", onePath.path);
        }
    };
    LineChart.prototype.drawAxis = function (xAxis, axisColor, axisWidth, xScale, xMax, yScale, yMin, axisFontSize, xMaxText, yAxis1, xMin, yMax, data, yAxis2) {
        var g = this.state.$dom.$svg.append("g")
            .attr('shape-rendering', 'crispEdges');
        g.append('path')
            .attr('d', xAxis)
            .attr('stroke', axisColor)
            .attr('fill', 'none')
            .attr('stroke-width', axisWidth);
        g.append('text')
            .attr('dx', xScale(xMax) - 50)
            .attr('dy', yScale[0](yMin[0]) + 15)
            .attr('fill', axisColor)
            .attr('font-size', axisFontSize - 1)
            .attr('letter-spacing', '1')
            .text(xMaxText);
        { }
        g.append('path')
            .attr('d', yAxis1)
            .attr('stroke', axisColor)
            .attr('fill', 'none')
            .attr('stroke-width', axisWidth);
        g.append('text')
            .attr('dx', xScale(xMin) - 10)
            .attr('dy', yScale[0](yMax[0]) - 5)
            .attr('fill', axisColor)
            .attr('font-size', axisFontSize)
            .text(data[0].label);
        if (!isEmpty(yAxis2)) {
            g.append('path')
                .attr('d', yAxis2)
                .attr('stroke', axisColor)
                .attr('fill', 'none')
                .attr('stroke-width', axisWidth);
            g.append('text')
                .attr('dx', xScale(xMax) - 15)
                .attr('dy', yScale[1](yMax[1]) - 5)
                .attr('fill', axisColor)
                .attr('font-size', axisFontSize)
                .text(data[1].label);
        }
    };
    LineChart.prototype.setState = function (stateParam) {
        var keys = Object.keys(stateParam);
        for (var i = 0; i < keys.length; i++) {
            var key = keys[i];
            this.state[key] = stateParam[key];
        }
    };
    LineChart.prototype.getXvalueFromPlayIndex = function (xValue) {
        var _a = this.options, data = _a.series, xAxisInterval = _a.xAxisInterval;
        for (var i = 0; i < data.length; i += 1) {
            var serie = data[i];
            var serieData = serie.data;
            if (serieData.length > xValue) {
                for (var j = 0; j < serieData.length; j += 1) {
                    var item = serieData[j];
                    var itemXValue = item.index;
                    if (itemXValue - xAxisInterval < xValue && xValue < itemXValue + xAxisInterval) {
                        return item;
                    }
                }
            }
        }
        return null;
    };
    LineChart.prototype.getPlayIndexFromXvalue = function (xValue) {
        var _a = this.options, data = _a.series, xAxisInterval = _a.xAxisInterval;
        for (var i = 0; i < data.length; i += 1) {
            var serie = data[i];
            var serieData = serie.data;
            for (var j = 0; j < serieData.length; j += 1) {
                var item = serieData[j];
                var itemXValue = item.index;
                if (itemXValue - xAxisInterval < xValue && xValue < itemXValue + xAxisInterval) {
                    return j;
                }
            }
        }
        return null;
    };
    LineChart.prototype.getClosestXvalue = function (xValue) {
        var data = this.options.series;
        var neighborhoodArr = [];
        data.forEach(function (serie) {
            var serieData = serie.data;
            var neighborhood = [null, null];
            for (var i = 0; i < serieData.length; i += 1) {
                var item = serieData[i];
                var itemXValue = item.index;
                if (xValue >= itemXValue) {
                    neighborhood[0] = itemXValue;
                }
                if (xValue <= itemXValue && neighborhood[1] === null) {
                    neighborhood[1] = itemXValue;
                    break;
                }
            }
            neighborhoodArr.push(neighborhood);
        });
        var closestXValue = null;
        var closestDiff = null;
        neighborhoodArr.forEach(function (neighbor) {
            var beforeDiff = Math.abs(xValue - neighbor[0]);
            var afterDiff = Math.abs(neighbor[1] - xValue);
            var minDiff = Math.min(beforeDiff, afterDiff);
            if (closestDiff === null || closestDiff > minDiff) {
                closestDiff = minDiff;
                closestXValue = beforeDiff > afterDiff ? neighbor[1] : neighbor[0];
            }
        });
        return closestXValue;
    };
    LineChart.prototype.getYaxisValue = function (xValue) {
        if (xValue === null) {
            return { data: [], hasYValue: false };
        }
        var _a = this.options, data = _a.series, xAxisInterval = _a.xAxisInterval;
        var r = { data: [], hasYValue: false };
        var hasYValue = false;
        data.forEach(function (serie) {
            var serieData = serie.data, yValueFunc = serie.yValueFunc;
            var yValue;
            for (var i = 0; i < serieData.length; i++) {
                var item = serieData[i];
                var itemXValue = item.index;
                if (itemXValue - xAxisInterval < xValue && xValue < itemXValue + xAxisInterval) {
                    yValue = item;
                    break;
                }
            }
            var rItem = {
                name: serie.label,
            };
            if (yValue === undefined) {
                rItem.value = null;
                rItem.text = '--- ';
            }
            else if (yValue.value === null) {
                rItem.yValue = yValue;
                rItem.value = null;
                rItem.text = '--- ';
                hasYValue = true;
            }
            else if (yValue.value === undefined || yValue.date === null || yValue.date === 0 || yValue.date.toString() === '0') {
                rItem.value = null;
                rItem.text = '无数据';
            }
            else {
                var rItemValue = yValue.value;
                rItem.yValue = yValue;
                rItem.value = rItemValue;
                rItem.text = rItemValue.toString() + " " + serie.unit;
                hasYValue = true;
            }
            if (typeof yValueFunc === 'function') {
                rItem.formattedText = yValueFunc(rItem, serie);
            }
            else {
                rItem.formattedText = rItem.name + ": " + rItem.text;
            }
            r.data.push(rItem);
        });
        r.hasYValue = hasYValue;
        return r;
    };
    LineChart.prototype.shadowCopyArray = function (data) {
        var finalData = [];
        for (var i = 0, len = data.length; i < len; i++) {
            finalData.push(data[i]);
        }
        return finalData;
    };
    LineChart.prototype.buildStateData = function (data) {
        var finalData = this.shadowCopyArray(data);
        return finalData;
    };
    LineChart.prototype.updatePositionByPlayIndex = function (playIndex) {
        this.options.playIndex = playIndex;
        var rectColorFunc = this.options.rectColorFunc;
        var _a = this.state, xScale = _a.xScale, timeFormater = _a.timeFormater;
        var rectColor = this.state.rectColor;
        if (xScale === null) {
            return;
        }
        var xValue = this.getXvalueFromPlayIndex(playIndex);
        var newX1 = xScale(xValue.index);
        var yValue = this.getYaxisValue(xValue.index);
        var currentXText = '';
        if (xValue !== undefined && xValue !== null && xValue.date !== null && xValue.date > 0) {
            currentXText = timeFormater(xValue.date);
            if (typeof rectColorFunc === 'function') {
                var colorFromFunc = rectColorFunc(xValue);
                if (colorFromFunc !== null) {
                    rectColor = colorFromFunc;
                }
            }
        }
        this.setState({
            x1: newX1,
            x2: newX1,
            yValue: yValue.data,
            currentXText: currentXText,
            rectColor: rectColor,
        });
        this.drawIndicator();
    };
    LineChart.prototype.destory = function () {
        if (this.state) {
            this.state.$dom.$origin.empty();
            this.state.$dom = undefined;
            this.state = undefined;
        }
        if (this.options) {
            this.options = undefined;
        }
    };
    LineChart.timeFormater = function (date) {
        if (typeof date === 'number') {
            date = new Date(date);
        }
        var hour = date.getHours();
        var minute = date.getMinutes();
        var second = date.getSeconds();
        var h = hour >= 10 ? hour.toString(10) : '0' + hour.toString(10);
        var m = minute >= 10 ? minute.toString(10) : '0' + minute.toString(10);
        var s = second >= 10 ? second.toString(10) : '0' + second.toString(10);
        return h + ":" + m + ":" + s;
    };
    return LineChart;
}());
var SimpleLine = (function () {
    function SimpleLine(selector, optionsParam) {
        this.getPathPartition = function (serie) {
            var total = [];
            var partition = [];
            var serieColor = serie.color;
            var prevColor = serie.data.length > 0 && serie.data[0].color && serie.data[0].color.length > 0
                ? serie.data[0].color : serieColor;
            serie.data.forEach(function (item, index) {
                if (item.color && item.color.length > 0 && item.color != prevColor) {
                    total.push({
                        data: [].concat(partition),
                        color: prevColor,
                    });
                    partition = [item];
                    if (serie.autoConnectPartition === 'BEFORE' && total.length > 0) {
                        var lastPartition = total[total.length - 1].data;
                        if (lastPartition.length > 0) {
                            var lastItem = lastPartition[lastPartition.length - 1];
                            partition.splice(0, 0, lastItem);
                        }
                    }
                    else if (serie.autoConnectPartition === 'AFTER' && total.length > 0) {
                        var lastPartition = total[total.length - 1].data;
                        if (lastPartition.length > 0) {
                            lastPartition.push(item);
                        }
                    }
                    prevColor = item.color;
                }
                else {
                    partition.push(item);
                    if (index == serie.data.length - 1) {
                        total.push({
                            data: [].concat(partition),
                            color: prevColor,
                        });
                    }
                }
            });
            return total;
        };
        this.setOption(optionsParam, selector);
    }
    SimpleLine.prototype.setOption = function (optionsParam, selector) {
        var defaults = {
            series: [],
            padding: {
                top: 0,
                bottom: 0,
                left: 0,
                right: 0,
            }
        };
        var options = $.extend(defaults, optionsParam);
        this.options = options;
        this.state = {
            selector: selector,
            data: this.buildStateData(options.series),
            width: 0,
            height: 0,
            paths: null,
        };
        this.initHtml();
    };
    SimpleLine.prototype.getOption = function () {
        return this.options;
    };
    SimpleLine.prototype.getState = function () {
        return this.state;
    };
    SimpleLine.prototype.initHtml = function () {
        var _this = this;
        this.state.$dom = {
            $origin: $(this.state.selector),
        };
        this.state.$dom.$origin.empty();
        this.state.width = this.state.$dom.$origin.width();
        this.state.height = this.state.$dom.$origin.height();
        var _a = this.state, height = _a.height, width = _a.width;
        var _b = this.options, data = _b.series, padding = _b.padding;
        var extent = data.map(function (serie) {
            var minAndMax = d3.extent(serie.data.filter(function (x) { return x.value !== null; }), function (x) { return x.value; });
            return {
                serieXExtent: [0, serie.data.length - 1],
                serieYExtent: [
                    isEmpty(serie.yMinValue) ? minAndMax[0] : serie.yMinValue,
                    isEmpty(serie.yMaxValue) ? minAndMax[1] : serie.yMaxValue,
                ],
            };
        });
        var xMin = d3.min(extent, function (x) { return x.serieXExtent[0]; });
        var xMax = d3.max(extent, function (x) { return x.serieXExtent[1]; });
        var yMin = extent.map(function (x) { return (!x.serieYExtent[0] ? 0 : x.serieYExtent[0]); });
        var yMax = extent.map(function (x) { return (!x.serieYExtent[1] ? 0 : x.serieYExtent[1]); });
        for (var i = 0; i < yMin.length; i += 1) {
            if (yMin[i] === yMax[i]) {
                if (yMin[i] === 0) {
                    yMax[i] += 1;
                }
                else {
                    yMin[i] -= 1;
                    yMax[i] += 1;
                }
            }
        }
        var xScale = d3.scaleLinear()
            .domain([xMin, xMax])
            .range([0 + padding.left, width - padding.right]);
        var yScale = yMin.map(function (x, i) { return d3.scaleLinear()
            .domain([yMin[i], yMax[i]])
            .range([height - padding.bottom, 0 + padding.top]); });
        var paths = [];
        data.forEach(function (serie, index) {
            var pathPartition = _this.getPathPartition(serie);
            pathPartition.forEach(function (partition) {
                var path = d3.line()
                    .x(function (d) { return xScale(d.index); })
                    .y(function (d) { return yScale[index](d.value); })(partition.data);
                paths.push(__assign({ path: path }, serie, { color: partition.color }));
            });
        });
        this.setState({
            width: width,
            height: height,
            paths: paths,
        });
        this.render();
    };
    SimpleLine.prototype.render = function () {
        var paths = this.state.paths;
        this.state.$dom.$svg = d3.select(this.state.selector).append("svg")
            .attr("width", this.state.width)
            .attr("height", this.state.height)
            .attr('class', 'simple-line-svg');
        this.drawPaths(paths);
    };
    SimpleLine.prototype.drawPaths = function (paths) {
        var g = this.state.$dom.$svg.append("g");
        for (var i = 0; i < paths.length; i++) {
            var onePath = paths[i];
            g.append("path")
                .datum(onePath.data)
                .attr("fill", "none")
                .attr('stroke', onePath.color)
                .attr('stroke-width', onePath.width)
                .attr("d", onePath.path);
        }
    };
    SimpleLine.prototype.setState = function (stateParam) {
        var keys = Object.keys(stateParam);
        for (var i = 0; i < keys.length; i++) {
            var key = keys[i];
            this.state[key] = stateParam[key];
        }
    };
    SimpleLine.prototype.shadowCopyArray = function (data) {
        var finalData = [];
        for (var i = 0, len = data.length; i < len; i++) {
            finalData.push(data[i]);
        }
        return finalData;
    };
    SimpleLine.prototype.buildStateData = function (data) {
        var finalData = this.shadowCopyArray(data);
        return finalData;
    };
    return SimpleLine;
}());
