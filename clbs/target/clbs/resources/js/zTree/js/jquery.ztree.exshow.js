/*
 扩展zTree显示功能
 */
(function ($) {
    //default consts of exshow
    var _consts = {
            id: {
                COUNT: "_count",
                ALIASES: "_aliases",// 监控对象别名
            }
        },
        _setting = {
            view: {
                countClass: "",
                countIsHTML: false,
                aliasesClass: "aliasesStyle",
                aliasesIsHTML: false
            },
            data: {
                key: {
                    icon: "icon",
                    count: "count",
                    aliases: "aliases"
                }
            }
        },
        //add method of operate data
        _data = {
            getNodeCount: function (setting, node) {
                var countKey = setting.data.key.count;
                return "" + node[countKey];
            },
            getNodeAliases: function (setting, node) {
                var aliasesKey = setting.data.key.aliases;
                return "" + node[aliasesKey];
            }
        },
        //add method of operate zTree dom
        _view = {
            setNodeNameCount: function (setting, node) {
                var title = data.nodeTitle(setting, node),
                    nObj = $$(node, consts.id.SPAN, setting),
                    cObj = $$(node, consts.id.COUNT, setting);
                nObj.empty();
                if (setting.view.nameIsHTML) {
                    nObj.html(data.nodeName(setting, node));
                } else {
                    nObj.text(data.nodeName(setting, node));
                }
                if (cObj) {
                    cObj.empty();
                    var children = data.nodeChildren(setting, node);
                    if (!children) {
                        return;
                    }
                    if (setting.view.countIsHTML) {
                        cObj.html("(" + children.length + ")");
                    } else {
                        cObj.text("(" + children.length + ")");
                    }
                }
                if (tools.apply(setting.view.showTitle, [setting.treeId, node], setting.view.showTitle)) {
                    var aObj = $$(node, consts.id.A, setting);
                    aObj.attr("title", !title ? "" : title);
                }
            },
            setNodeNameAliases: function (setting, node) {
                var title = data.nodeTitle(setting, node),
                    nObj = $$(node, consts.id.SPAN, setting),
                    cObj = $$(node, consts.id.ALIASES, setting);
                nObj.empty();
                if (setting.view.nameIsHTML) {
                    nObj.html(data.nodeName(setting, node));
                } else {
                    nObj.text(data.nodeName(setting, node));
                }
                if (cObj) {
                    cObj.empty();
                    cObj.html("(" + node.aliases + ")");
                    cObj.attr('class', setting.view.aliasesClass);
                }
                if (tools.apply(setting.view.showTitle, [setting.treeId, node], setting.view.showTitle)) {
                    var aObj = $$(node, consts.id.A, setting);
                    aObj.attr("title", !title ? "" : title);
                }
            }
        },
        _zTreeTools = function (setting, zTreeTools) {
            zTreeTools.updateNodeCount = function (node) {
                if (!node) return;
                var nObj = $$(node, setting);
                if (nObj.get(0) && tools.uCanDo(setting)) {
                    view.setNodeNameCount(setting, node);
                    view.setNodeTarget(setting, node);
                    view.setNodeUrl(setting, node);
                    view.setNodeLineIcos(setting, node);
                    view.setNodeFontCss(setting, node);
                }
            };
            zTreeTools.updateNodeAliases = function (node) {
                if (!node) return;
                var nObj = $$(node, setting);
                if (nObj.get(0) && tools.uCanDo(setting)) {
                    view.setNodeNameAliases(setting, node);
                    view.setNodeTarget(setting, node);
                    view.setNodeUrl(setting, node);
                    view.setNodeLineIcos(setting, node);
                    view.setNodeFontCss(setting, node);
                }
            };
            zTreeTools.updateNodeIconSkin = function (node) {
                if (!node) return;
                var nObj = $$(node, setting);
                if (nObj.get(0) && tools.uCanDo(setting)) {
                    var icoObj = $$(node, consts.id.ICON, setting);
                    icoObj.attr("class", view.makeNodeIcoClass(setting, node));
                }
            }
        },

        _z = {
            view: _view,
            data: _data
        };

    $.extend(true, $.fn.zTree.consts, _consts);
    $.extend(true, $.fn.zTree._z, _z);

    var zt = $.fn.zTree,
        tools = zt._z.tools,
        consts = zt.consts,
        view = zt._z.view,
        data = zt._z.data,
        $$ = tools.$;

    data.exSetting(_setting);
    data.addZTreeTools(_zTreeTools);

    //	Override method in core
    var _makeDOMNodeIcon = view.makeDOMNodeIcon;
    view.makeDOMNodeIcon = function (html, setting, node) {
        if (_makeDOMNodeIcon) _makeDOMNodeIcon.apply(view, arguments);
        var countStr = data.getNodeCount(setting, node),
            count = setting.view.countIsHTML ? countStr : countStr.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

        if (count !== "undefined") {
            html.push("<span id='", node.tId, consts.id.COUNT, "' class='", setting.view.countClass, "'>(", count, ")</span>");
        }
        var aliasesStr = data.getNodeAliases(setting, node),
            aliases = setting.view.aliasesIsHTML ? aliasesStr : aliasesStr.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

        if (aliases && aliases !== "null" && aliases !== "undefined" && aliases !== "") {
            html.push("<span id='", node.tId, consts.id.ALIASES, "' class='", setting.view.aliasesClass, "'>(", aliases, ")</span>");
        }
    };

    var _makeNodeLineClass = view.makeNodeLineClass;
    if (!!_makeNodeLineClass) {
        view.makeNodeLineClass = function (setting, node) {
            if (node.type === 'vehicle' || node.type === 'thing' || node.type === 'people') {
                return 'vehicle ' + _makeNodeLineClass.apply(view, arguments);
            }
            return _makeNodeLineClass.apply(view, arguments);
        }
    }
})(jQuery);