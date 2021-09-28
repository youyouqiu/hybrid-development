/*
/!*! laydate-v5.0.2 日期与时间组件 MIT License  http://www.layui.com/laydate/  By 贤心 *!/
!
    function () {
        var s = window.layui && layui.define,
            o = {
                getPath: function () {
                    var A = document.scripts,
                        y = A[A.length - 1],
                        z = y.src;
                    if (y.getAttribute("merge")) {
                        return
                    }
                    return z.substring(0, z.lastIndexOf("/") + 1)
                }(),
                getStyle: function (A, y) {
                    var z = A.currentStyle ? A.currentStyle : window.getComputedStyle(A, null);
                    return z[z.getPropertyValue ? "getPropertyValue" : "getAttribute"](y)
                },
                link: function (y, F, z) {
                    if (!l.path) {
                        return
                    }
                    var G = document.getElementsByTagName("head")[0],
                        E = document.createElement("link");
                    if (typeof F === "string") {
                        z = F
                    }
                    var B = (z || y).replace(/\.|\//g, "");
                    var A = "layuicss-" + B,
                        D = 0;
                    E.rel = "stylesheet";
                    E.href = l.path + y;
                    E.id = A;
                    if (!document.getElementById(A)) {
                        G.appendChild(E)
                    }
                    if (typeof F !== "function") {
                        return
                    }
                    (function C() {
                        if (++D > 8 * 1000 / 100) {
                            return window.console && console.error("laydate.css: Invalid")
                        }
                        parseInt(o.getStyle(document.getElementById(A), "width")) === 1989 ? F() : setTimeout(C, 100)
                    }())
                }
            },
            l = {
                v: "5.0.2",
                config: {},
                index: (window.laydate && window.laydate.v) ? 100000 : 0,
                path: o.getPath,
                set: function (y) {
                    var z = this;
                    z.config = o.extend({},
                        z.config, y);
                    return z
                },
                ready: function (A) {
                    var z = "laydate",
                        y = "",
                        B = (s ? "modules/laydate/" : "theme/") + "default/laydate.css?v=" + l.v + y;
                    s ? layui.addcss(B, A, z) : o.link(B, A, z);
                    return this
                }
            },
            r = function () {
                var y = this;
                return {
                    hint: function (z) {
                        y.hint.call(y, z)
                    },
                    config: y.config
                }
            },
            k = "laydate",
            d = ".layui-laydate",
            j = "layui-this",
            g = "layui-show",
            u = "layui-hide",
            v = "laydate-disabled",
            // v = "",
            c = "开始日期超出了结束日期<br>建议重新选择",
            q = [100, 200000],
            h = "layui-laydate-list",
            n = "laydate-selected",
            x = "layui-laydate-hint",
            a = "laydate-day-prev",
            b = "laydate-day-next",
            w = "layui-laydate-footer",
            p = ".laydate-btns-confirm",
            f = "laydate-time-text",
            m = ".laydate-btns-time",
            t = function (y) {
                var z = this;
                z.index = ++l.index;
                z.config = e.extend({},
                    z.config, l.config, y);
                l.ready(function () {
                    z.init()
                })
            },
            e = function (y) {
                return new i(y)
            },
            i = function (y) {
                var z = 0,
                    A = typeof y === "object" ? [y] : (this.selector = y, document.querySelectorAll(y || null));
                for (; z < A.length; z++) {
                    this.push(A[z])
                }
            };
        i.prototype = [];
        i.prototype.constructor = i;
        e.extend = function () {
            var y = 1,
                z = arguments,
                A = function (D, C) {
                    D = D || (C.constructor === Array ? [] : {});
                    for (var B in C) {
                        D[B] = (C[B] && (C[B].constructor === Object)) ? A(D[B], C[B]) : C[B]
                    }
                    return D
                };
            z[0] = typeof z[0] === "object" ? z[0] : {};
            for (; y < z.length; y++) {
                if (typeof z[y] === "object") {
                    A(z[0], z[y])
                }
            }
            return z[0]
        };
        e.ie = function () {
            var y = navigator.userAgent.toLowerCase();
            return (!!window.ActiveXObject || "ActiveXObject" in window) ? ((y.match(/msie\s(\d+)/) || [])[1] || "11") : false
        }();
        e.stope = function (y) {
            y = y || win.event;
            y.stopPropagation ? y.stopPropagation() : y.cancelBubble = true
        };
        e.each = function (B, z) {
            var y, A = this;
            if (typeof z !== "function") {
                return A
            }
            B = B || [];
            if (B.constructor === Object) {
                for (y in B) {
                    if (z.call(B[y], y, B[y])) {
                        break
                    }
                }
            } else {
                for (y = 0; y < B.length; y++) {
                    if (z.call(B[y], y, B[y])) {
                        break
                    }
                }
            }
            return A
        };
        e.digit = function (z, B, y) {
            var C = "";
            z = String(z);
            B = B || 2;
            for (var A = z.length; A < B; A++) {
                C += "0"
            }
            return z < Math.pow(10, B) ? C + (z | 0) : z
        };
        e.elem = function (A, y) {
            var z = document.createElement(A);
            e.each(y || {},
                function (B, C) {
                    z.setAttribute(B, C)
                });
            return z
        };
        i.addStr = function (z, y) {
            z = z.replace(/\s+/, " ");
            y = y.replace(/\s+/, " ").split(" ");
            e.each(y,
                function (A, B) {
                    if (!new RegExp("\\b" + B + "\\b").test(z)) {
                        z = z + " " + B
                    }
                });
            return z.replace(/^\s|\s$/, "")
        };
        i.removeStr = function (z, y) {
            z = z.replace(/\s+/, " ");
            y = y.replace(/\s+/, " ").split(" ");
            e.each(y,
                function (A, B) {
                    var C = new RegExp("\\b" + B + "\\b");
                    if (C.test(z)) {
                        z = z.replace(C, "")
                    }
                });
            return z.replace(/\s+/, " ").replace(/^\s|\s$/, "")
        };
        i.prototype.find = function (A) {
            var C = this;
            var B = 0,
                z = [],
                y = typeof A === "object";
            this.each(function (D, E) {
                var F = y ? [A] : E.querySelectorAll(A || null);
                for (; B < F.length; B++) {
                    z.push(F[B])
                }
                C.shift()
            });
            if (!y) {
                C.selector = (C.selector ? C.selector + " " : "") + A
            }
            e.each(z,
                function (D, E) {
                    C.push(E)
                });
            return C
        };
        i.prototype.each = function (y) {
            return e.each.call(this, this, y)
        };
        i.prototype.addClass = function (z, y) {
            return this.each(function (A, B) {
                B.className = i[y ? "removeStr" : "addStr"](B.className, z)
            })
        };
        i.prototype.removeClass = function (y) {
            return this.addClass(y, true)
        };
        i.prototype.hasClass = function (z) {
            var y = false;
            this.each(function (A, B) {
                if (new RegExp("\\b" + z + "\\b").test(B.className)) {
                    y = true
                }
            });
            return y
        };
        i.prototype.attr = function (y, A) {
            var z = this;
            return A === undefined ?
                function () {
                    if (z.length > 0) {
                        return z[0].getAttribute(y)
                    }
                }() : z.each(function (B, C) {
                    C.setAttribute(y, A)
                })
        };
        i.prototype.removeAttr = function (y) {
            return this.each(function (z, A) {
                A.removeAttribute(y)
            })
        };
        i.prototype.html = function (y) {
            return this.each(function (z, A) {
                A.innerHTML = y
            })
        };
        i.prototype.val = function (y) {
            return this.each(function (z, A) {
                A.value = y
            })
        };
        i.prototype.append = function (y) {
            return this.each(function (z, A) {
                typeof y === "object" ? A.appendChild(y) : A.innerHTML = A.innerHTML + y
            })
        };
        i.prototype.remove = function (y) {
            return this.each(function (z, A) {
                y ? A.removeChild(y) : A.parentNode.removeChild(A)
            })
        };
        i.prototype.on = function (y, z) {
            return this.each(function (A, B) {
                B.attachEvent ? B.attachEvent("on" + y,
                    function (C) {
                        C.target = C.srcElement;
                        z.call(B, C)
                    }) : B.addEventListener(y, z, false)
            })
        };
        i.prototype.off = function (y, z) {
            return this.each(function (A, B) {
                B.detachEvent ? B.detachEvent("on" + y, z) : B.removeEventListener(y, z, false)
            })
        };
        t.isLeapYear = function (y) {
            return (y % 4 === 0 && y % 100 !== 0) || y % 400 === 0
        };
        t.prototype.config = {
            type: "date",
            range: false,
            format: "yyyy-MM-dd",
            value: null,
            min: "1900-1-1",
            max: "2099-12-31",
            trigger: "focus",
            show: false,
            showBottom: true,
            btns: ["clear", "now", "confirm"],
            lang: "cn",
            theme: "#6dcff6",
            position: null,
            calendar: false,
            mark: {},
            zIndex: null,
            done: null,
            change: null,
            showTimeLine: false
        };
        t.prototype.lang = function () {
            var z = this,
                y = z.config,
                A = {
                    cn: {
                        weeks: ["日", "一", "二", "三", "四", "五", "六"],
                        time: ["时", "分", "秒"],
                        timeTips: "选择时间",
                        startTime: "开始时间",
                        endTime: "结束时间",
                        dateTips: "返回日期",
                        month: ["一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"],
                        tools: {
                            confirm: "确定",
                            clear: "清空",
                            now: "现在"
                        }
                    },
                    en: {
                        weeks: ["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"],
                        time: ["Hours", "Minutes", "Seconds"],
                        timeTips: "Select Time",
                        startTime: "Start Time",
                        endTime: "End Time",
                        dateTips: "Select Date",
                        month: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
                        tools: {
                            confirm: "Confirm",
                            clear: "Clear",
                            now: "Now"
                        }
                    }
                };
            return A[y.lang] || A["cn"]
        };
        t.prototype.init = function () {
            var B = this,
                z = B.config,
                A = "yyyy|y|MM|M|dd|d|HH|H|mm|m|ss|s",
                y = z.position === "static",
                C = {
                    year: "yyyy",
                    month: "yyyy-MM",
                    date: "yyyy-MM-dd",
                    time: "HH:mm:ss",
                    datetime: "yyyy-MM-dd HH:mm:ss"
                };
            z.elem = e(z.elem);
            z.eventElem = e(z.eventElem);
            if (!z.elem[0]) {
                return
            }
            if (z.range === true) {
                z.range = "-"
            }
            if (z.format === C.date) {
                z.format = C[z.type]
            }
            B.format = z.format.match(new RegExp(A + "|.", "g")) || [];
            B.EXP_IF = "";
            B.EXP_SPLIT = "";
            e.each(B.format,
                function (D, F) {
                    var E = new RegExp(A).test(F) ? "\\b\\d{1," +
                        function () {
                            if (/yyyy/.test(F)) {
                                return 4
                            }
                            if (/y/.test(F)) {
                                return 308
                            }
                            return 2
                        }() + "}\\b" : "\\" + F;
                    B.EXP_IF = B.EXP_IF + E;
                    B.EXP_SPLIT = B.EXP_SPLIT + (B.EXP_SPLIT ? "|" : "") + "(" + E + ")"
                });
            B.EXP_IF = new RegExp("^" + (z.range ? B.EXP_IF + "\\s\\" + z.range + "\\s" + B.EXP_IF : B.EXP_IF) + "$");
            B.EXP_SPLIT = new RegExp(B.EXP_SPLIT, "g");
            if (!B.isInput(z.elem[0])) {
                if (z.trigger === "focus") {
                    z.trigger = "click"
                }
            }
            if (!z.elem.attr("lay-key")) {
                z.elem.attr("lay-key", B.index);
                z.eventElem.attr("lay-key", B.index)
            }
            z.mark = e.extend({},
                (z.calendar && z.lang === "cn") ? {
                    "0-1-1": "元旦",
                    "0-2-14": "情人",
                    "0-3-8": "妇女",
                    "0-3-12": "植树",
                    "0-4-1": "愚人",
                    "0-5-1": "劳动",
                    "0-5-4": "青年",
                    "0-6-1": "儿童",
                    "0-9-10": "教师",
                    "0-9-18": "国耻",
                    "0-10-1": "国庆",
                    "0-12-25": "圣诞"
                } : {},
                z.mark);
            e.each(["min", "max"],
                function (G, I) {
                    var J = [],
                        H = [];
                    if (typeof z[I] === "number") {
                        var E = z[I],
                            K = new Date().getTime(),
                            F = 86400000,
                            D = new Date(E ? (E < F ? K + E * F : E) : K);
                        J = [D.getFullYear(), D.getMonth() + 1, D.getDate()];
                        E < F || (H = [D.getHours(), D.getMinutes(), D.getSeconds()])
                    } else {
                        J = (z[I].match(/\d+-\d+-\d+/) || [""])[0].split("-");
                        H = (z[I].match(/\d+:\d+:\d+/) || [""])[0].split(":")
                    }
                    z[I] = {
                        year: J[0] | 0 || new Date().getFullYear(),
                        month: J[1] ? (J[1] | 0) - 1 : new Date().getMonth(),
                        date: J[2] | 0 || new Date().getDate(),
                        hours: H[0] | 0,
                        minutes: H[1] | 0,
                        seconds: H[2] | 0
                    }
                });
            B.elemID = "layui-laydate" + z.elem.attr("lay-key");
            if (z.show || y) {
                B.render()
            }
            y || B.events()
        };
        t.prototype.render = function () {
            var H = this,
                K = H.config,
                B = H.lang(),
                hh = K.noSecond,
                J = K.position === "static",
                E = H.elem = e.elem("div", {
                    id: H.elemID,
                    "class": ["layui-laydate", K.range ? " layui-laydate-range" : "", J ? " layui-laydate-static" : "", K.theme && K.theme !== "default" && !/^#/.test(K.theme) ? (" laydate-theme-" + K.theme) : "", hh ? ' noSecond' : ''].join("")
                }),
                G = H.elemMain = [],
                I = H.elemHeader = [],
                C = H.elemCont = [],
                A = H.table = [],
                F = H.footer = e.elem("div", {
                    "class": w
                }),
                D = H.timeline = e.elem("div", {
                    "class": "layui-laydate-time"
                });
            if (K.zIndex) {
                E.style.zIndex = K.zIndex
            }
            e.each(new Array(2),
                function (N) {
                    if (!K.range && N > 0) {
                        return true
                    }
                    var M = e.elem("div", {
                            "class": "layui-laydate-header"
                        }),
                        R = [function () {
                            var S = e.elem("i", {
                                "class": "layui-icon laydate-icon laydate-prev-y"
                            });
                            S.innerHTML = "&#xe65a;";
                            return S
                        }(),
                            function () {
                                var S = e.elem("i", {
                                    "class": "layui-icon laydate-icon laydate-prev-m"
                                });
                                S.innerHTML = "&#xe603;";
                                return S
                            }(),
                            function () {
                                var S = e.elem("div", {
                                        "class": "laydate-set-ym"
                                    }),
                                    T = e.elem("span"),
                                    U = e.elem("span");
                                S.appendChild(T);
                                S.appendChild(U);
                                return S
                            }(),
                            function () {
                                var S = e.elem("i", {
                                    "class": "layui-icon laydate-icon laydate-next-m"
                                });
                                S.innerHTML = "&#xe602;";
                                return S
                            }(),
                            function () {
                                var S = e.elem("i", {
                                    "class": "layui-icon laydate-icon laydate-next-y"
                                });
                                S.innerHTML = "&#xe65b;";
                                return S
                            }()],
                        L = e.elem("div", {
                            "class": "layui-laydate-content"
                        }),
                        O = e.elem("table"),
                        Q = e.elem("thead"),
                        P = e.elem("tr");
                    e.each(R,
                        function (S, T) {
                            M.appendChild(T)
                        });
                    Q.appendChild(P);
                    e.each(new Array(6),
                        function (S) {
                            var T = O.insertRow(0);
                            e.each(new Array(7),
                                function (U) {
                                    if (S === 0) {
                                        var V = e.elem("th");
                                        V.innerHTML = B.weeks[U];
                                        P.appendChild(V)
                                    }
                                    T.insertCell(U)
                                })
                        });
                    O.insertBefore(Q, O.children[0]);
                    L.appendChild(O);
                    G[N] = e.elem("div", {
                        "class": "layui-laydate-main laydate-main-list-" + N
                    });
                    G[N].appendChild(M);
                    G[N].appendChild(L);
                    I.push(R);
                    C.push(L);
                    A.push(O)
                });
            e(D).html(function () {
                var L = '<ul class="laydate-timeline-presets">' + '<li time-type="5">5分钟后<div class="laydate-time-button"></div></li>' + '<li time-type="10">10分钟后<div class="laydate-time-button"></div></li>' + '<li time-type="15">15分钟后<div class="laydate-time-button"></div></li>' + '<li time-type="20">20分钟后<div class="laydate-time-button"></div></li>' + "</ul>" + '<div class="laydate-timeline-bar"></div>';
                return L
            }());
            e(F).html(function () {
                var L = [],
                    M = [];
                if (K.type === "datetime") {
                    L.push('<span lay-type="datetime" class="laydate-btns-time">' + B.timeTips + "</span>")
                }
                e.each(K.btns,
                    function (N, O) {
                        var P = B.tools[O] || "btn";
                        if (K.range && O === "now") {
                            return
                        }
                        if (J && O === "clear") {
                            P = K.lang === "cn" ? "重置" : "Reset"
                        }
                        M.push('<span lay-type="' + O + '" class="laydate-btns-' + O + '">' + P + "</span>")
                    });
                L.push('<div class="laydate-footer-btns">' + M.join("") + "</div>");
                return L.join("")
            }());
            e.each(G,
                function (M, L) {
                    E.appendChild(L)
                });
            K.showBottom && E.appendChild(F);
            if (H.config.showTimeLine) {
                E.appendChild(D)
            }
            if (/^#/.test(K.theme)) {
                var z = e.elem("style"),
                    y = ["#{{id}} .layui-laydate-header{background-color:{{theme}};}", "#{{id}} .layui-this{background-color:{{theme}} !important;}"].join("").replace(/{{id}}/g, H.elemID).replace(/{{theme}}/g, K.theme);
                if ("styleSheet" in z) {
                    z.setAttribute("type", "text/css");
                    z.styleSheet.cssText = y
                } else {
                    z.innerHTML = y
                }
                e(E).addClass("laydate-theme-molv");
                E.appendChild(z)
            }
            H.remove();
            J ? K.elem.append(E) : (document.body.appendChild(E), H.position());
            H.checkDate().calendar();
            H.changeEvent();
            t.thisElem = H.elemID;
            typeof K.ready === "function" && K.ready(e.extend({},
                K.dateTime, {
                    month: K.dateTime.month + 1
                }))
        };
        t.prototype.remove = function () {
            var A = this,
                y = A.config,
                z = e("#" + A.elemID);
            if (z[0] && y.position !== "static") {
                A.checkDate(function () {
                    z.remove()
                })
            }
            return A
        };
        t.prototype.position = function () {
            var E = this,
                I = E.config,
                B = E.bindElem || I.elem[0],
                H = B.getBoundingClientRect(),
                D = E.elem.offsetWidth,
                z = E.elem.offsetHeight,
                y = function (J) {
                    J = J ? "scrollLeft" : "scrollTop";
                    return document.body[J] | document.documentElement[J]
                },
                F = function (J) {
                    return document.documentElement[J ? "clientWidth" : "clientHeight"]
                },
                C = 5,
                A = H.left,
                G = H.bottom;
            if (A + D + C > F("width")) {
                A = F("width") - D - C
            }
            if (G + z + C > F()) {
                G = H.top > z ? H.top - z : F() - z;
                G = G - C * 2
            }
            if (I.position) {
                E.elem.style.position = I.position
            }
            E.elem.style.left = A + (I.position === "fixed" ? 0 : y(1)) + "px";
            E.elem.style.top = G + (I.position === "fixed" ? 0 : y()) + "px"
        };
        t.prototype.hint = function (A) {
            var z = this,
                y = z.config,
                B = e.elem("div", {
                    "class": x
                });
            B.innerHTML = A || "";
            e(z.elem).find("." + x).remove();
            z.elem.appendChild(B);
            clearTimeout(z.hinTimer);
            z.hinTimer = setTimeout(function () {
                    e(z.elem).find("." + x).remove()
                },
                3000)
        };
        t.prototype.getAsYM = function (z, A, y) {
            y ? A-- : A++;
            if (A < 0) {
                A = 11;
                z--
            }
            if (A > 11) {
                A = 0;
                z++
            }
            return [z, A]
        };
        t.prototype.systemDate = function (z) {
            var y = z || new Date();
            return {
                year: y.getFullYear(),
                month: y.getMonth(),
                date: y.getDate(),
                hours: z ? z.getHours() : 0,
                minutes: z ? z.getMinutes() : 0,
                seconds: z ? z.getSeconds() : 0
            }
        };
        t.prototype.checkDate = function (E) {
            var B = this,
                z = new Date(),
                J = B.config,
                I = J.dateTime = J.dateTime || B.systemDate(),
                H,
                D,
                y = B.bindElem || J.elem[0],
                A = B.isInput(y) ? "val" : "html",
                G = B.isInput(y) ? y.value : (J.position === "static" ? "" : y.innerHTML),
                F = function (K) {
                    if (K.year > q[1]) {
                        K.year = q[1],
                            D = true
                    }
                    if (K.month > 11) {
                        K.month = 11,
                            D = true
                    }
                    if (K.hours > 23) {
                        K.hours = 0,
                            D = true
                    }
                    if (K.minutes > 59) {
                        K.minutes = 0,
                            K.hours++,
                            D = true
                    }
                    if (K.seconds > 59) {
                        K.seconds = 0,
                            K.minutes++,
                            D = true
                    }
                    H = l.getEndDate(K.month + 1, K.year);
                    if (K.date > H) {
                        K.date = H,
                            D = true
                    }
                },
                C = function (N, M, K) {
                    var L = ["startTime", "endTime"];
                    M = M.match(B.EXP_SPLIT);
                    K = K || 0;
                    if (J.range) {
                        B[L[K]] = B[L[K]] || {}
                    }
                    e.each(B.format,
                        function (O, P) {
                            var Q = parseFloat(M[O]);
                            if (M[O].length < P.length) {
                                D = true
                            }
                            if (/yyyy|y/.test(P)) {
                                if (Q < q[0]) {
                                    Q = q[0],
                                        D = true
                                }
                                N.year = Q
                            } else {
                                if (/MM|M/.test(P)) {
                                    if (Q < 1) {
                                        Q = 1,
                                            D = true
                                    }
                                    N.month = Q - 1
                                } else {
                                    if (/dd|d/.test(P)) {
                                        if (Q < 1) {
                                            Q = 1,
                                                D = true
                                        }
                                        N.date = Q
                                    } else {
                                        if (/HH|H/.test(P)) {
                                            if (Q < 1) {
                                                Q = 0,
                                                    D = true
                                            }
                                            N.hours = Q;
                                            J.range && (B[L[K]].hours = Q)
                                        } else {
                                            if (/mm|m/.test(P)) {
                                                if (Q < 1) {
                                                    Q = 0,
                                                        D = true
                                                }
                                                N.minutes = Q;
                                                J.range && (B[L[K]].minutes = Q)
                                            } else {
                                                if (/ss|s/.test(P)) {
                                                    if (Q < 1) {
                                                        Q = 0,
                                                            D = true
                                                    }
                                                    N.seconds = Q;
                                                    J.range && (B[L[K]].seconds = Q)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    F(N)
                };
            if (E === "limit") {
                return F(I),
                    B
            }
            G = G || J.value;
            if (typeof G === "string") {
                G = G.replace(/\s+/g, " ").replace(/^\s|\s$/g, "")
            }
            if (B.startState && !B.endState) {
                delete B.startState;
                B.endState = true
            }
            if (typeof G === "string" && G) {
                if (B.EXP_IF.test(G)) {
                    if (J.range) {
                        G = G.split(" " + J.range + " ");
                        B.startDate = B.startDate || B.systemDate();
                        B.endDate = B.endDate || B.systemDate();
                        J.dateTime = e.extend({},
                            B.startDate);
                        e.each([B.startDate, B.endDate],
                            function (K, L) {
                                C(L, G[K], K)
                            })
                    } else {
                        C(I, G)
                    }
                } else {
                    B.hint("日期格式不合法<br>必须遵循下述格式：<br>" + (J.range ? (J.format + " " + J.range + " " + J.format) : J.format) + "<br>已为你重置");
                    D = true
                }
            } else {
                if (G && G.constructor === Date) {
                    J.dateTime = B.systemDate(G)
                } else {
                    J.dateTime = B.systemDate();
                    delete B.startState;
                    delete B.endState;
                    delete B.startDate;
                    delete B.endDate;
                    delete B.startTime;
                    delete B.endTime
                }
            }
            F(I);
            if (D && G) {
                B.setValue(J.range ? (B.endDate ? B.parse() : "") : B.parse())
            }
            E && E();
            return B
        };
        t.prototype.mark = function (C, z) {
            var A = this,
                B, y = A.config;
            e.each(y.mark,
                function (D, F) {
                    var E = D.split("-");
                    if ((E[0] == z[0] || E[0] == 0) && E[1] == z[1] && E[2] == z[2]) {
                        B = F || z[2]
                    }
                });
            B && C.html('<span class="laydate-day-mark">' + B + "</span>");
            return A
        };
        t.prototype.limit = function (A, B, F, z) {
            var E = this,
                H = E.config,
                D = {},
                G = H[F > 41 ? "endDate" : "dateTime"],
                C,
                y = e.extend({},
                    G, B || {});
            e.each({
                    now: y,
                    min: H.min,
                    max: H.max
                },
                function (I, J) {
                    D[I] = E.newDate(e.extend({
                            year: J.year,
                            month: J.month,
                            date: J.date
                        },
                        function () {
                            var K = {};
                            e.each(z,
                                function (L, M) {
                                    K[M] = J[M]
                                });
                            return K
                        }())).getTime()
                });
            C = D.now < D.min || D.now > D.max;
            A && A[C ? "addClass" : "removeClass"](v);
            return C
        };
        t.prototype.calendar = function (J) {
            var D = this,
                L = D.config,
                K = J || L.dateTime,
                A = new Date(),
                y,
                F,
                I,
                z = D.lang(),
                H = L.type !== "date" && L.type !== "datetime",
                C = J ? 1 : 0,
                B = e(D.table[C]).find("td"),
                E = e(D.elemHeader[C][2]).find("span");
            if (K.year < q[0]) {
                K.year = q[0],
                    D.hint("最低只能支持到公元" + q[0] + "年")
            }
            if (K.year > q[1]) {
                K.year = q[1],
                    D.hint("最高只能支持到公元" + q[1] + "年")
            }
            if (!D.firstDate) {
                D.firstDate = e.extend({},
                    K)
            }
            A.setFullYear(K.year, K.month, 1);
            y = A.getDay();
            F = l.getEndDate(K.month, K.year);
            I = l.getEndDate(K.month + 1, K.year);
            e.each(B,
                function (N, P) {
                    var O = [K.year, K.month],
                        M = 0;
                    P = e(P);
                    P.removeAttr("class");
                    if (N < y) {
                        M = F - y + N;
                        P.addClass("laydate-day-prev");
                        O = D.getAsYM(K.year, K.month, "sub")
                    } else {
                        if (N >= y && N < I + y) {
                            M = N - y;
                            if (!L.range) {
                                M + 1 === K.date && P.addClass(j)
                            }
                        } else {
                            M = N - I - y;
                            P.addClass("laydate-day-next");
                            O = D.getAsYM(K.year, K.month)
                        }
                    }
                    O[1]++;
                    O[2] = M + 1;
                    P.attr("lay-ymd", O.join("-")).html(O[2]);
                    D.mark(P, O).limit(P, {
                            year: O[0],
                            month: O[1] - 1,
                            date: O[2]
                        },
                        N)
                });
            e(E[0]).attr("lay-ym", K.year + "-" + (K.month + 1));
            e(E[1]).attr("lay-ym", K.year + "-" + (K.month + 1));
            if (L.lang === "cn") {
                e(E[0]).attr("lay-type", "year").html(K.year + "年");
                e(E[1]).attr("lay-type", "month").html((K.month + 1) + "月")
            } else {
                e(E[0]).attr("lay-type", "month").html(z.month[K.month]);
                e(E[1]).attr("lay-type", "year").html(K.year)
            }
            if (H) {
                if (L.range) {
                    J ? D.endDate = (D.endDate || {
                        year: K.year + (L.type === "year" ? 1 : 0),
                        month: K.month + (L.type === "month" ? 0 : -1)
                    }) : (D.startDate = D.startDate || {
                        year: K.year,
                        month: K.month
                    });
                    if (J) {
                        D.listYM = [[D.startDate.year, D.startDate.month + 1], [D.endDate.year, D.endDate.month + 1]];
                        D.list(L.type, 0).list(L.type, 1);
                        L.type === "time" ? D.setBtnStatus("时间", e.extend({},
                            D.systemDate(), D.startTime), e.extend({},
                            D.systemDate(), D.endTime)) : D.setBtnStatus(true)
                    }
                }
                if (!L.range) {
                    D.listYM = [[K.year, K.month + 1]];
                    D.list(L.type, 0)
                }
            }
            if (L.range && !J) {
                var G = D.getAsYM(K.year, K.month);
                D.calendar(e.extend({},
                    K, {
                        year: G[0],
                        month: G[1]
                    }))
            }
            if (!L.range) {
                D.limit(e(D.footer).find(p), null, 0, ["hours", "minutes", "seconds"])
            }
            if (L.range && J && !H) {
                D.stampRange()
            }
            return D
        };
        t.prototype.list = function (C, F) {
            var G = this,
                B = G.config,
                E = B.dateTime,
                U = G.lang(),
                M = B.range && B.type !== "date" && B.type !== "datetime",
                K = e.elem("ul", {
                    "class": h + " " + ({
                        year: "laydate-year-list",
                        month: "laydate-month-list",
                        time: "laydate-time-list"
                    })[C]
                }),
                O = G.elemHeader[F],
                T = e(O[2]).find("span"),
                Q = G.elemCont[F || 0],
                A = e(Q).find("." + h)[0],
                I = B.lang === "cn",
                J = I ? "年" : "",
                S = G.listYM[F] || {},
                D = ["hours", "minutes", "seconds"],
                P = ["startTime", "endTime"][F];
            if (S[0] < 1) {
                S[0] = 1
            }
            if (C === "year") {
                var y, N = y = S[0] - 7;
                if (N < 1) {
                    N = y = 1
                }
                e.each(new Array(15),
                    function (W) {
                        var V = e.elem("li", {
                            "lay-ym": y
                        });
                        y == S[0] && e(V).addClass(j);
                        V.innerHTML = y + J;
                        K.appendChild(V);
                        G.limit(e(V), {
                                year: y
                            },
                            F);
                        y++
                    });
                e(T[I ? 0 : 1]).attr("lay-ym", (y - 8) + "-" + S[1]).html((N + J) + " - " + (y - 1 + J))
            } else {
                if (C === "month") {
                    e.each(new Array(12),
                        function (W) {
                            var V = e.elem("li", {
                                "lay-ym": W
                            });
                            W + 1 == S[1] && e(V).addClass(j);
                            V.innerHTML = U.month[W] + (I ? "月" : "");
                            K.appendChild(V);
                            G.limit(e(V), {
                                    year: S[0],
                                    month: W
                                },
                                F)
                        });
                    e(T[I ? 0 : 1]).attr("lay-ym", S[0] + "-" + S[1]).html(S[0] + J)
                } else {
                    if (C === "time") {
                        var H = function () {
                            e(K).find("ol").each(function (W, V) {
                                e(V).find("li").each(function (Y, X) {
                                    G.limit(e(X), [{
                                        hours: Y
                                    },
                                        {
                                            hours: G[P].hours,
                                            minutes: Y
                                        },
                                        {
                                            hours: G[P].hours,
                                            minutes: G[P].minutes,
                                            seconds: Y
                                        }][W], F, [["hours"], ["hours", "minutes"], ["hours", "minutes", "seconds"]][W])
                                })
                            });
                            if (!B.range) {
                                G.limit(e(G.footer).find(p), G[P], 0, ["hours", "minutes", "seconds"])
                            }
                        };
                        if (B.range) {
                            if (!G[P]) {
                                G[P] = {
                                    hours: 0,
                                    minutes: 0,
                                    seconds: 0
                                }
                            }
                        } else {
                            G[P] = E
                        }
                        e.each([24, 60, 60],
                            function (X, Y) {
                                var V = e.elem("li"),
                                    W = ["<p>" + U.time[X] + "</p><ol>"];
                                e.each(new Array(Y),
                                    function (Z) {
                                        W.push("<li" + (G[P][D[X]] === Z ? ' class="' + j + '"' : "") + ">" + e.digit(Z, 2) + "</li>")
                                    });
                                V.innerHTML = W.join("") + "</ol>";
                                K.appendChild(V)
                            });
                        H()
                    }
                }
            }
            if (A) {
                Q.removeChild(A)
            }
            Q.appendChild(K);
            if (C === "year" || C === "month") {
                e(G.elemMain[F]).addClass("laydate-ym-show");
                e(K).find("li").on("click",
                    function () {
                        var V = e(this).attr("lay-ym") | 0;
                        if (e(this).hasClass(v)) {
                            return
                        }
                        if (F === 0) {
                            E[C] = V;
                            if (M) {
                                G.startDate[C] = V
                            }
                        } else {
                            if (M) {
                                G.endDate[C] = V
                            } else {
                                var W = C === "year" ? G.getAsYM(V, S[1] - 1, "sub") : G.getAsYM(S[0], V, "sub");
                                e.extend(E, {
                                    year: W[0],
                                    month: W[1]
                                })
                            }
                        }
                        if (B.type === "year" || B.type === "month") {
                            e(K).find("." + j).removeClass(j);
                            e(this).addClass(j);
                            if (B.type === "month" && C === "year") {
                                G.listYM[F][0] = V;
                                M && (G[["startDate", "endDate"][F]].year = V);
                                G.list("month", F)
                            }
                        } else {
                            G.checkDate("limit").calendar();
                            G.closeList()
                        }
                        G.setBtnStatus();
                        B.range || G.done(null, "change");
                        e(G.footer).find(m).removeClass(v)
                    })
            } else {
                var L = e.elem("span", {
                        "class": f
                    }),
                    z = function () {
                        e(K).find("ol").each(function (X) {
                            var W = this,
                                V = e(W).find("li");
                            W.scrollTop = 30 * (G[P][D[X]] - 2);
                            if (W.scrollTop <= 0) {
                                V.each(function (Y, Z) {
                                    if (!e(this).hasClass(v)) {
                                        W.scrollTop = 30 * (Y - 2);
                                        return true
                                    }
                                })
                            }
                        })
                    },
                    R = e(O[2]).find("." + f);
                z();
                L.innerHTML = B.range ? [U.startTime, U.endTime][F] : U.timeTips;
                e(G.elemMain[F]).addClass("laydate-time-show");
                if (R[0]) {
                    R.remove()
                }
                O[2].appendChild(L);
                e(K).find("ol").each(function (W) {
                    var V = this;
                    e(V).find("li").on("click",
                        function () {
                            var X = this.innerHTML | 0;
                            if (e(this).hasClass(v)) {
                                return
                            }
                            if (B.range) {
                                G[P][D[W]] = X
                            } else {
                                E[D[W]] = X
                            }
                            e(V).find("." + j).removeClass(j);
                            e(this).addClass(j);
                            G.setBtnStatus(null, e.extend({},
                                G.systemDate(), G.startTime), e.extend({},
                                G.systemDate(), G.endTime));
                            H();
                            z();
                            (G.endDate || B.type === "time") && G.done(null, "change")
                        })
                })
            }
            return G
        };
        t.prototype.listYM = [];
        t.prototype.closeList = function () {
            var z = this,
                y = z.config;
            e.each(z.elemCont,
                function (A, B) {
                    e(this).find("." + h).remove();
                    e(z.elemMain[A]).removeClass("laydate-ym-show laydate-time-show")
                });
            e(z.elem).find("." + f).remove()
        };
        t.prototype.setBtnStatus = function (z, F, A) {
            var C = this,
                B = C.config,
                E, D = e(C.footer).find(p),
                y = B.range && B.type !== "date" && B.type !== "datetime";
            if (y) {
                F = F || C.startDate;
                A = A || C.endDate;
                E = C.newDate(F).getTime() > C.newDate(A).getTime();
                if (!B.beyondTime) {
                    (C.limit(null, F) || C.limit(null, A)) ? D.addClass(v) : D[E ? "addClass" : "removeClass"](v);
                    if (z && E) {
                        C.hint(typeof z === "string" ? c.replace(/日期/g, z) : c)
                    }
                }
            }
        };
        t.prototype.parse = function (B) {
            var z = this,
                y = z.config,
                A = B ? e.extend({},
                    z.endDate, z.endTime) : (y.range ? e.extend({},
                    z.startDate, z.startTime) : y.dateTime),
                C = z.format.concat();
            e.each(C,
                function (D, E) {
                    if (/yyyy|y/.test(E)) {
                        C[D] = e.digit(A.year, E.length)
                    } else {
                        if (/MM|M/.test(E)) {
                            C[D] = e.digit(A.month + 1, E.length)
                        } else {
                            if (/dd|d/.test(E)) {
                                C[D] = e.digit(A.date, E.length)
                            } else {
                                if (/HH|H/.test(E)) {
                                    C[D] = e.digit(A.hours, E.length)
                                } else {
                                    if (/mm|m/.test(E)) {
                                        C[D] = e.digit(A.minutes, E.length)
                                    } else {
                                        if (/ss|s/.test(E)) {
                                            C[D] = e.digit(A.seconds, E.length)
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            if (y.range && !B) {
                return C.join("") + " " + y.range + " " + z.parse(1)
            }
            return C.join("")
        };
        t.prototype.newDate = function (y) {
            return new Date(y.year || 1, y.month || 0, y.date || 1, y.hours || 0, y.minutes || 0, y.seconds || 0)
        };
        t.prototype.setValue = function (B) {
            var A = this,
                y = A.config,
                z = A.bindElem || y.elem[0],
                C = A.isInput(z) ? "val" : "html";
            y.position === "static" || e(z)[C](B || "");
            return this
        };
        t.prototype.stampRange = function () {
            var C = this,
                z = C.config,
                B, y, A = e(C.elem).find("td");
            if (z.range && !C.endDate) {
                e(C.footer).find(p).addClass(v)
            }
            if (!C.endDate) {
                return
            }
            B = C.newDate({
                year: C.startDate.year,
                month: C.startDate.month,
                date: C.startDate.date
            }).getTime();
            y = C.newDate({
                year: C.endDate.year,
                month: C.endDate.month,
                date: C.endDate.date
            }).getTime();
            if (B > y) {
                return C.hint(c)
            }
            e.each(A,
                function (D, F) {
                    var G = e(F).attr("lay-ymd").split("-"),
                        E = C.newDate({
                            year: G[0],
                            month: G[1] - 1,
                            date: G[2]
                        }).getTime();
                    e(F).removeClass(n + " " + j);
                    if (E === B || E === y) {
                        e(F).addClass(e(F).hasClass(a) || e(F).hasClass(b) ? n : j)
                    }
                    if (E > B && E < y) {
                        e(F).addClass(n)
                    }
                })
        };
        t.prototype.done = function (C, A) {
            var B = this,
                z = B.config,
                D = e.extend({},
                    B.startDate ? e.extend(B.startDate, B.startTime) : z.dateTime),
                y = e.extend({},
                    e.extend(B.endDate, B.endTime));
            e.each([D, y],
                function (E, F) {
                    if (!("month" in F)) {
                        return
                    }
                    e.extend(F, {
                        month: F.month + 1
                    })
                });
            C = C || [B.parse(), D, y];
            typeof z[A || "done"] === "function" && z[A || "done"].apply(z, C);
            return B, C[0]
        };

        t.prototype.choose = function (F) {
            var C = this,
                z = C.config,
                D = z.dateTime,
                B = e(C.elem).find("td"),
                A = F.attr("lay-ymd").split("-"),
                E = function (H) {
                    var G = new Date();
                    H && e.extend(D, A);
                    if (z.range) {
                        C.startDate ? e.extend(C.startDate, A) : (C.startDate = e.extend({},
                            A, C.startTime));
                        C.startYMD = A
                    }
                };
            A = {
                year: A[0] | 0,
                month: (A[1] | 0) - 1,
                date: A[2] | 0
            };
            if (F.hasClass(v)) {
                return
            }
            if (z.range) {
                e.each(["startTime", "endTime"],
                    function (G, H) {
                        C[H] = C[H] || {
                            hours: 0,
                            minutes: 0,
                            seconds: 0
                        }
                    });
                if (!C.startState) C.endState = true;
                if (C.endState) {
                    E();
                    delete C.endState;
                    delete C.endDate;
                    C.startState = true;
                    B.removeClass(j + " " + n);
                    F.addClass(j)
                } else {
                    if (C.startState) {
                        F.addClass(j);
                        C.endDate ? e.extend(C.endDate, A) : (C.endDate = e.extend({},
                            A, C.endTime));
                        if (C.newDate(A).getTime() < C.newDate(C.startYMD).getTime()) {
                            var y = e.extend({},
                                C.endDate, {
                                    hours: C.startDate.hours,
                                    minutes: C.startDate.minutes,
                                    seconds: C.startDate.seconds
                                });
                            e.extend(C.endDate, C.startDate, {
                                hours: C.endDate.hours,
                                minutes: C.endDate.minutes,
                                seconds: C.endDate.seconds
                            });
                            C.startDate = y
                        }
                        z.showBottom || C.done();
                        C.stampRange();
                        C.endState = true;
                        C.done(null, "change")
                    } else {
                        F.addClass(j);
                        E();
                        C.startState = true
                    }
                }
                e(C.footer).find(p)[C.endDate ? "removeClass" : "addClass"](v)
            } else {
                if (z.position === "static") {
                    E(true);
                    C.calendar().done().done(null, "change")
                } else {
                    if (z.type === "date") {
                        E(true);
                        C.setValue(C.parse()).remove().done()
                    } else {
                        if (z.type === "datetime") {
                            E(true);
                            C.calendar().done(null, "change")
                        }
                    }
                }
            }
        };
        t.prototype.timetool = function (C, D) {
            var E = this,
                A = E.config,
                F = A.dateTime,
                y = new Date(),
                B = y.getTime() + Number(D) * 60 * 1000,
                z = new Date(B);
            e.extend(F, E.systemDate(z), {
                hours: z.getHours(),
                minutes: z.getMinutes(),
                seconds: z.getSeconds()
            });
            E.setValue(E.parse()).remove();
            E.calendar();
            E.done()
        };
        t.prototype.tool = function (A, B) {
            var C = this,
                z = C.config,
                D = z.dateTime,
                y = z.position === "static",
                E = {
                    datetime: function () {
                        if (e(A).hasClass(v)) {
                            return
                        }
                        C.list("time", 0);
                        z.range && C.list("time", 1);
                        e(A).attr("lay-type", "date").html(C.lang().dateTips)
                    },
                    date: function () {
                        C.closeList();
                        e(A).attr("lay-type", "datetime").html(C.lang().timeTips)
                    },
                    clear: function () {
                        C.setValue("").remove();
                        y && (e.extend(D, C.firstDate), C.calendar());
                        z.range && (delete C.startState, delete C.endState, delete C.endDate, delete C.startTime, delete C.endTime);
                        C.done(["", {},
                            {}])
                    },
                    now: function () {
                        var F = new Date();
                        e.extend(D, C.systemDate(), {
                            hours: F.getHours(),
                            minutes: F.getMinutes(),
                            seconds: F.getSeconds()
                        });
                        C.setValue(C.parse()).remove();
                        y && C.calendar();
                        C.done()
                    },
                    confirm: function () {

                        if (z.range) {
                            if (!C.endDate) {
                                return C.hint("请先选择日期范围")
                            }
                            if (e(A).hasClass(v) && !z.beyondTime) {
                                return C.hint(z.type === "time" ? c.replace(/日期/g, "时间") : c)
                            }
                        } else {
                            if (e(A).hasClass(v)) {
                                return C.hint("不在有效日期或时间范围内")
                            }
                        }
                        var tt = C.done();
                        if (C.config.elem[0].id !== "timepiker") {
                            C.setValue(C.parse()).remove()
                        } else {
                            C.remove();
                            if (tt !== '') {
                                timevalue = tt.split("-");
                                var time1 = $(".drp-calendar-date").eq(0).val();
                                var time2 = $(".drp-calendar-date").eq(1).val();
                                $(".drp-calendar-date").eq(0).val(time1.substring(0, 11) + "" + timevalue[0]);
                                $(".drp-calendar-date").eq(1).val(time2.substring(0, 11) + "" + timevalue[1]);
                                $("#timepiker").html("");
                                $("#timeInterval").val(time1.substring(0, 11) + "" + timevalue[0] + "--" + time2.substring(0, 11) + "" + timevalue[1]);
                            } else {
                                $(".drp-calendar-date").eq(0).val("");
                                $(".drp-calendar-date").eq(1).val("");

                            }
                            console.log("时间" + C.parse());
                        }

                    }
                };
            E[B] && E[B]()
        };
        t.prototype.change = function (B) {
            var E = this,
                A = E.config,
                F = A.dateTime,
                y = A.range && (A.type === "year" || A.type === "month"),
                C = E.elemCont[B || 0],
                z = E.listYM[B],
                D = function (H) {
                    var G = ["startDate", "endDate"][B],
                        I = e(C).find(".laydate-year-list")[0],
                        J = e(C).find(".laydate-month-list")[0];
                    if (I) {
                        z[0] = H ? z[0] - 15 : z[0] + 15;
                        E.list("year", B)
                    }
                    if (J) {
                        H ? z[0]-- : z[0]++;
                        E.list("month", B)
                    }
                    if (I || J) {
                        e.extend(F, {
                            year: z[0]
                        });
                        if (y) {
                            E[G].year = z[0]
                        }
                        A.range || E.done(null, "change");
                        E.setBtnStatus();
                        A.range || E.limit(e(E.footer).find(p), {
                            year: z[0]
                        })
                    }
                    return I || J
                };
            return {
                prevYear: function () {
                    if (D("sub")) {
                        return
                    }
                    F.year--;
                    E.checkDate("limit").calendar();
                    A.range || E.done(null, "change")
                },
                prevMonth: function () {
                    var G = E.getAsYM(F.year, F.month, "sub");
                    e.extend(F, {
                        year: G[0],
                        month: G[1]
                    });
                    E.checkDate("limit").calendar();
                    A.range || E.done(null, "change")
                },
                nextMonth: function () {
                    var G = E.getAsYM(F.year, F.month);
                    e.extend(F, {
                        year: G[0],
                        month: G[1]
                    });
                    E.checkDate("limit").calendar();
                    A.range || E.done(null, "change")
                },
                nextYear: function () {
                    if (D()) {
                        return
                    }
                    F.year++;
                    E.checkDate("limit").calendar();
                    A.range || E.done(null, "change")
                }
            }
        };
        t.prototype.changeEvent = function () {
            var z = this,
                y = z.config;
            e(z.elem).on("click",
                function (A) {
                    e.stope(A)
                });
            e.each(z.elemHeader,
                function (A, B) {
                    e(B[0]).on("click",
                        function (C) {
                            z.change(A).prevYear()
                        });
                    e(B[1]).on("click",
                        function (C) {
                            z.change(A).prevMonth()
                        });
                    e(B[2]).find("span").on("click",
                        function (F) {
                            var E = e(this),
                                D = E.attr("lay-ym").split("-"),
                                C = E.attr("lay-type");
                            z.listYM[A] = [D[0] | 0, D[1] | 0];
                            z.list(C, A);
                            e(z.footer).find(m).addClass(v)
                        });
                    e(B[3]).on("click",
                        function (C) {
                            z.change(A).nextMonth()
                        });
                    e(B[4]).on("click",
                        function (C) {
                            z.change(A).nextYear()
                        })
                });
            e.each(z.table,
                function (A, C) {
                    var B = e(C).find("td");
                    B.on("click",
                        function () {
                            z.choose(e(this))
                        })
                });
            e(z.timeline).find("ul li").on("click",
                function () {
                    var A = e(this).attr("time-type");
                    z.timetool(this, A)
                });
            e(z.footer).find("span").on("click",
                function () {
                    var A = e(this).attr("lay-type");
                    z.tool(this, A)
                })
        };
        t.prototype.isInput = function (y) {
            return /input|textarea/.test(y.tagName.toLocaleLowerCase())
        };
        t.prototype.events = function () {
            var A = this,
                y = A.config,
                z = function (B, C) {
                    B.on(y.trigger,
                        function () {
                            C && (A.bindElem = this);
                            A.render()
                        })
                };
            if (!y.elem[0] || y.elem[0].eventHandler) {
                return
            }
            z(y.elem, "bind");
            z(y.eventElem);
            e(document).on("click",
                function (B) {
                    if (B.target === y.elem[0] || B.target === y.eventElem[0] || B.target === e(y.closeStop)[0]) {
                        return
                    }
                    A.remove()
                }).on("keydown",
                function (B) {
                    if (B.keyCode === 13) {
                        if (e("#" + A.elemID)[0] && A.elemID === t.thisElem) {
                            B.preventDefault();
                            e(A.footer).find(p)[0].click()
                        }
                    }
                });
            e(window).on("resize",
                function () {
                    if (!A.elem || !e(d)[0]) {
                        return false
                    }
                    A.position()
                });
            y.elem[0].eventHandler = true
        };
        l.render = function (y) {
            var z = new t(y);
            return r.call(z)
        };
        l.getEndDate = function (A, z) {
            var y = new Date();
            y.setFullYear(z || y.getFullYear(), A || (y.getMonth() + 1), 1);
            return new Date(y.getTime() - 1000 * 60 * 60 * 24).getDate()
        };
        window.lay = window.lay || e;
        s ? (l.ready(), layui.define(function (y) {
            l.path = layui.cache.dir;
            y(k, l)
        })) : ((typeof define === "function" && define.amd) ? define(function () {
            return l
        }) : function () {
            l.ready();
            window.laydate = l
        }())
    }();*/


/**

 @Name : layDate 5.0.9 日期时间控件
 @Author: 贤心
 @Site：http://www.layui.com/laydate/
 @License：MIT

 */

;!function(){
    "use strict";

    var isLayui = window.layui && layui.define, ready = {
            getPath: function(){
                var jsPath = document.currentScript ? document.currentScript.src : function(){
                    var js = document.scripts
                        ,last = js.length - 1
                        ,src;
                    for(var i = last; i > 0; i--){
                        if(js[i].readyState === 'interactive'){
                            src = js[i].src;
                            break;
                        }
                    }
                    return src || js[last].src;
                }();
                return jsPath.substring(0, jsPath.lastIndexOf('/') + 1);
            }()

            //获取节点的style属性值
            ,getStyle: function(node, name){
                var style = node.currentStyle ? node.currentStyle : window.getComputedStyle(node, null);
                return style[style.getPropertyValue ? 'getPropertyValue' : 'getAttribute'](name);
            }

            //载入CSS配件
            ,link: function(href, fn, cssname){

                //未设置路径，则不主动加载css
                if(!laydate.path) return;

                var head = document.getElementsByTagName("head")[0], link = document.createElement('link');
                if(typeof fn === 'string') cssname = fn;
                var app = (cssname || href).replace(/\.|\//g, '');
                var id = 'layuicss-'+ app, timeout = 0;

                link.rel = 'stylesheet';
                link.href = laydate.path + href;
                link.id = id;

                if(!document.getElementById(id)){
                    head.appendChild(link);
                }

                if(typeof fn !== 'function') return;

                //轮询css是否加载完毕
                (function poll() {
                    if(++timeout > 8 * 1000 / 100){
                        return window.console && console.error('laydate.css: Invalid');
                    };
                    parseInt(ready.getStyle(document.getElementById(id), 'width')) === 1989 ? fn() : setTimeout(poll, 100);
                }());
            }
        }

        ,laydate = {
            v: '5.0.9'
            ,config: {} //全局配置项
            ,index: (window.laydate && window.laydate.v) ? 100000 : 0
            ,path: ready.getPath

            //设置全局项
            ,set: function(options){
                var that = this;
                that.config = lay.extend({}, that.config, options);
                return that;
            }

            //主体CSS等待事件
            ,ready: function(fn){
                var cssname = 'laydate', ver = ''
                    ,path = (isLayui ? 'modules/laydate/' : 'theme/') + 'default/laydate.css?v='+ laydate.v + ver;
                isLayui ? layui.addcss(path, fn, cssname) : ready.link(path, fn, cssname);
                return this;
            }
        }

        //操作当前实例
        ,thisDate = function(){
            var that = this;
            return {
                //提示框
                hint: function(content){
                    that.hint.call(that, content);
                }
                ,config: that.config
                ,obj: that
            };
        }

        //字符常量
        ,MOD_NAME = 'laydate', ELEM = '.layui-laydate', THIS = 'layui-this', SHOW = 'layui-show', HIDE = 'layui-hide', DISABLED = 'laydate-disabled', TIPS_OUT = '开始日期超出了结束日期<br>建议重新选择', LIMIT_YEAR = [100, 200000]

        ,ELEM_STATIC = 'layui-laydate-static', ELEM_LIST = 'layui-laydate-list', ELEM_SELECTED = 'laydate-selected', ELEM_HINT = 'layui-laydate-hint', ELEM_PREV = 'laydate-day-prev', ELEM_NEXT = 'laydate-day-next', ELEM_FOOTER = 'layui-laydate-footer', ELEM_CONFIRM = '.laydate-btns-confirm', ELEM_TIME_TEXT = 'laydate-time-text', ELEM_TIME_BTN = '.laydate-btns-time'

        //组件构造器
        ,Class = function(options){
            var that = this;
            that.index = ++laydate.index;
            that.config = lay.extend({}, that.config, laydate.config, options);
            laydate.ready(function(){
                that.init();
            });
        }

        //DOM查找
        ,lay = function(selector){
            return new LAY(selector);
        }

        //DOM构造器
        ,LAY = function(selector){
            var index = 0
                ,nativeDOM = typeof selector === 'object' ? [selector] : (
                this.selector = selector
                    ,document.querySelectorAll(selector || null)
            );
            for(; index < nativeDOM.length; index++){
                this.push(nativeDOM[index]);
            }
        };


    /*
      lay对象操作
    */

    LAY.prototype = [];
    LAY.prototype.constructor = LAY;

    //普通对象深度扩展
    lay.extend = function(){
        var ai = 1, args = arguments
            ,clone = function(target, obj){
            target = target || (obj.constructor === Array ? [] : {});
            for(var i in obj){
                //如果值为对象，则进入递归，继续深度合并
                target[i] = (obj[i] && (obj[i].constructor === Object))
                    ? clone(target[i], obj[i])
                    : obj[i];
            }
            return target;
        }

        args[0] = typeof args[0] === 'object' ? args[0] : {};

        for(; ai < args.length; ai++){
            if(typeof args[ai] === 'object'){
                clone(args[0], args[ai])
            }
        }
        return args[0];
    };

    //ie版本
    lay.ie = function(){
        var agent = navigator.userAgent.toLowerCase();
        return (!!window.ActiveXObject || "ActiveXObject" in window) ? (
            (agent.match(/msie\s(\d+)/) || [])[1] || '11' //由于ie11并没有msie的标识
        ) : false;
    }();

    //中止冒泡
    lay.stope = function(e){
        e = e || window.event;
        e.stopPropagation
            ? e.stopPropagation()
            : e.cancelBubble = true;
    };

    //对象遍历
    lay.each = function(obj, fn){
        var key
            ,that = this;
        if(typeof fn !== 'function') return that;
        obj = obj || [];
        if(obj.constructor === Object){
            for(key in obj){
                if(fn.call(obj[key], key, obj[key])) break;
            }
        } else {
            for(key = 0; key < obj.length; key++){
                if(fn.call(obj[key], key, obj[key])) break;
            }
        }
        return that;
    };

    //数字前置补零
    lay.digit = function(num, length, end){
        var str = '';
        num = String(num);
        length = length || 2;
        for(var i = num.length; i < length; i++){
            str += '0';
        }
        return num < Math.pow(10, length) ? str + (num|0) : num;
    };

    //创建元素
    lay.elem = function(elemName, attr){
        var elem = document.createElement(elemName);
        lay.each(attr || {}, function(key, value){
            elem.setAttribute(key, value);
        });
        return elem;
    };

    //追加字符
    LAY.addStr = function(str, new_str){
        str = str.replace(/\s+/, ' ');
        new_str = new_str.replace(/\s+/, ' ').split(' ');
        lay.each(new_str, function(ii, item){
            if(!new RegExp('\\b'+ item + '\\b').test(str)){
                str = str + ' ' + item;
            }
        });
        return str.replace(/^\s|\s$/, '');
    };

    //移除值
    LAY.removeStr = function(str, new_str){
        str = str.replace(/\s+/, ' ');
        new_str = new_str.replace(/\s+/, ' ').split(' ');
        lay.each(new_str, function(ii, item){
            var exp = new RegExp('\\b'+ item + '\\b')
            if(exp.test(str)){
                str = str.replace(exp, '');
            }
        });
        return str.replace(/\s+/, ' ').replace(/^\s|\s$/, '');
    };

    //查找子元素
    LAY.prototype.find = function(selector){
        var that = this;
        var index = 0, arr = []
            ,isObject = typeof selector === 'object';

        this.each(function(i, item){
            var nativeDOM = isObject ? [selector] : item.querySelectorAll(selector || null);
            for(; index < nativeDOM.length; index++){
                arr.push(nativeDOM[index]);
            }
            that.shift();
        });

        if(!isObject){
            that.selector =  (that.selector ? that.selector + ' ' : '') + selector
        }

        lay.each(arr, function(i, item){
            that.push(item);
        });

        return that;
    };

    //DOM遍历
    LAY.prototype.each = function(fn){
        return lay.each.call(this, this, fn);
    };

    //添加css类
    LAY.prototype.addClass = function(className, type){
        return this.each(function(index, item){
            item.className = LAY[type ? 'removeStr' : 'addStr'](item.className, className)
        });
    };

    //移除css类
    LAY.prototype.removeClass = function(className){
        return this.addClass(className, true);
    };

    //是否包含css类
    LAY.prototype.hasClass = function(className){
        var has = false;
        this.each(function(index, item){
            if(new RegExp('\\b'+ className +'\\b').test(item.className)){
                has = true;
            }
        });
        return has;
    };

    //添加或获取属性
    LAY.prototype.attr = function(key, value){
        var that = this;
        return value === undefined ? function(){
            if(that.length > 0) return that[0].getAttribute(key);
        }() : that.each(function(index, item){
            item.setAttribute(key, value);
        });
    };

    //移除属性
    LAY.prototype.removeAttr = function(key){
        return this.each(function(index, item){
            item.removeAttribute(key);
        });
    };

    //设置HTML内容
    LAY.prototype.html = function(html){
        return this.each(function(index, item){
            item.innerHTML = html;
        });
    };

    //设置值
    LAY.prototype.val = function(value){
        return this.each(function(index, item){
            item.value = value;
        });
    };

    //追加内容
    LAY.prototype.append = function(elem){
        return this.each(function(index, item){
            typeof elem === 'object'
                ? item.appendChild(elem)
                :  item.innerHTML = item.innerHTML + elem;
        });
    };

    //移除内容
    LAY.prototype.remove = function(elem){
        return this.each(function(index, item){
            elem ? item.removeChild(elem) : item.parentNode.removeChild(item);
        });
    };

    //事件绑定
    LAY.prototype.on = function(eventName, fn){
        return this.each(function(index, item){
            item.attachEvent ? item.attachEvent('on' + eventName, function(e){
                e.target = e.srcElement;
                fn.call(item, e);
            }) : item.addEventListener(eventName, fn, false);
        });
    };

    //解除事件
    LAY.prototype.off = function(eventName, fn){
        return this.each(function(index, item){
            item.detachEvent
                ? item.detachEvent('on'+ eventName, fn)
                : item.removeEventListener(eventName, fn, false);
        });
    };


    /*
      组件操作
    */


    //是否闰年
    Class.isLeapYear = function(year){
        return (year % 4 === 0 && year % 100 !== 0) || year % 400 === 0;
    };

    //默认配置
    Class.prototype.config = {
        type: 'date' //控件类型，支持：year/month/date/time/datetime
        ,range: false //是否开启范围选择，即双控件
        ,format: 'yyyy-MM-dd' //默认日期格式
        ,value: null //默认日期，支持传入new Date()，或者符合format参数设定的日期格式字符
        ,isInitValue: true //用于控制是否自动向元素填充初始值（需配合 value 参数使用）
        ,min: '1900-1-1' //有效最小日期，年月日必须用“-”分割，时分秒必须用“:”分割。注意：它并不是遵循 format 设定的格式。
        ,max: '2099-12-31' //有效最大日期，同上
        ,trigger: 'focus' //呼出控件的事件
        ,show: false //是否直接显示，如果设置true，则默认直接显示控件
        ,showBottom: true //是否显示底部栏
        ,btns: ['clear', 'now', 'confirm'] //右下角显示的按钮，会按照数组顺序排列
        ,lang: 'cn' //语言，只支持cn/en，即中文和英文
        ,theme: 'default' //主题
        ,position: null //控件定位方式定位, 默认absolute，支持：fixed/absolute/static
        ,calendar: false //是否开启公历重要节日，仅支持中文版
        ,mark: {} //日期备注，如重要事件或活动标记
        ,zIndex: null //控件层叠顺序
        ,done: null //控件选择完毕后的回调，点击清空/现在/确定也均会触发
        ,change: null //日期时间改变后的回调
    };

    //多语言
    Class.prototype.lang = function(){
        var that = this
            ,options = that.config
            ,text = {
            cn: {
                weeks: ['日', '一', '二', '三', '四', '五', '六']
                ,time: ['时', '分', '秒']
                ,timeTips: '选择时间'
                ,startTime: '开始时间'
                ,endTime: '结束时间'
                ,dateTips: '返回日期'
                ,month: ['一', '二', '三', '四', '五', '六', '七', '八', '九', '十', '十一', '十二']
                ,tools: {
                    confirm: '确定'
                    ,clear: '清空'
                    ,now: '现在'
                }
            }
            ,en: {
                weeks: ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa']
                ,time: ['Hours', 'Minutes', 'Seconds']
                ,timeTips: 'Select Time'
                ,startTime: 'Start Time'
                ,endTime: 'End Time'
                ,dateTips: 'Select Date'
                ,month: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
                ,tools: {
                    confirm: 'Confirm'
                    ,clear: 'Clear'
                    ,now: 'Now'
                }
            }
        };
        return text[options.lang] || text['cn'];
    };

    //初始准备
    Class.prototype.init = function(){
        var that = this
            ,options = that.config
            ,dateType = 'yyyy|y|MM|M|dd|d|HH|H|mm|m|ss|s'
            ,isStatic = options.position === 'static'
            ,format = {
            year: 'yyyy'
            ,month: 'yyyy-MM'
            ,date: 'yyyy-MM-dd'
            ,time: 'HH:mm:ss'
            ,datetime: 'yyyy-MM-dd HH:mm:ss'
        };

        options.elem = lay(options.elem);
        options.eventElem = lay(options.eventElem);

        if(!options.elem[0]) return;

        //日期范围分隔符
        if(options.range === true) options.range = '-';

        //根据不同type，初始化默认format
        if(options.format === format.date){
            options.format = format[options.type];
        }

        //将日期格式转化成数组
        that.format = options.format.match(new RegExp(dateType + '|.', 'g')) || [];

        //生成正则表达式
        that.EXP_IF = '';
        that.EXP_SPLIT = '';
        lay.each(that.format, function(i, item){
            var EXP =  new RegExp(dateType).test(item)
                ? '\\d{'+ function(){
                if(new RegExp(dateType).test(that.format[i === 0 ? i + 1 : i - 1]||'')){
                    if(/^yyyy|y$/.test(item)) return 4;
                    return item.length;
                }
                if(/^yyyy$/.test(item)) return '1,4';
                if(/^y$/.test(item)) return '1,308';
                return '1,2';
            }() +'}'
                : '\\' + item;
            that.EXP_IF = that.EXP_IF + EXP;
            that.EXP_SPLIT = that.EXP_SPLIT + '(' + EXP + ')';
        });
        that.EXP_IF = new RegExp('^'+ (
            options.range ?
                that.EXP_IF + '\\s\\'+ options.range + '\\s' + that.EXP_IF
                : that.EXP_IF
        ) +'$');
        that.EXP_SPLIT = new RegExp('^'+ that.EXP_SPLIT +'$', '');

        //如果不是input|textarea元素，则默认采用click事件
        if(!that.isInput(options.elem[0])){
            if(options.trigger === 'focus'){
                options.trigger = 'click';
            }
        }

        //设置唯一KEY
        if(!options.elem.attr('lay-key')){
            options.elem.attr('lay-key', that.index);
            options.eventElem.attr('lay-key', that.index);
        }

        //记录重要日期
        options.mark = lay.extend({}, (options.calendar && options.lang === 'cn') ? {
            '0-1-1': '元旦'
            ,'0-2-14': '情人'
            ,'0-3-8': '妇女'
            ,'0-3-12': '植树'
            ,'0-4-1': '愚人'
            ,'0-5-1': '劳动'
            ,'0-5-4': '青年'
            ,'0-6-1': '儿童'
            ,'0-9-10': '教师'
            ,'0-9-18': '国耻'
            ,'0-10-1': '国庆'
            ,'0-12-25': '圣诞'
        } : {}, options.mark);

        //获取限制内日期
        lay.each(['min', 'max'], function(i, item){
            var ymd = [], hms = [];
            if(typeof options[item] === 'number'){ //如果为数字
                var day = options[item]
                    ,time = new Date().getTime()
                    ,STAMP = 86400000 //代表一天的时间戳
                    ,thisDate = new Date(
                    day ? (
                        day < STAMP ? time + day*STAMP : day //如果数字小于一天的时间戳，则数字为天数，否则为时间戳
                    ) : time
                );
                ymd = [thisDate.getFullYear(), thisDate.getMonth() + 1, thisDate.getDate()];
                day < STAMP || (hms = [thisDate.getHours(), thisDate.getMinutes(), thisDate.getSeconds()]);
            } else {
                ymd = (options[item].match(/\d+-\d+-\d+/) || [''])[0].split('-');
                hms = (options[item].match(/\d+:\d+:\d+/) || [''])[0].split(':');
            }
            options[item] = {
                year: ymd[0] | 0 || new Date().getFullYear()
                ,month: ymd[1] ? (ymd[1] | 0) - 1 : new Date().getMonth()
                ,date: ymd[2] | 0 || new Date().getDate()
                ,hours: hms[0] | 0
                ,minutes: hms[1] | 0
                ,seconds: hms[2] | 0
            };
        });

        that.elemID = 'layui-laydate'+ options.elem.attr('lay-key');

        if(options.show || isStatic) that.render();
        isStatic || that.events();

        //默认赋值
        if(options.value && options.isInitValue){
            if(options.value.constructor === Date){
                that.setValue(that.parse(0, that.systemDate(options.value)));
            } else {
                that.setValue(options.value);
            }
        }
    };

    //控件主体渲染
    Class.prototype.render = function(){
        var that = this
            ,options = that.config
            ,lang = that.lang()
            ,isStatic = options.position === 'static'

            //主面板
            ,elem = that.elem = lay.elem('div', {
                id: that.elemID
                ,'class': [
                    'layui-laydate'
                    ,options.range ? ' layui-laydate-range' : ''
                    ,isStatic ? (' '+ ELEM_STATIC) : ''
                    ,options.theme && options.theme !== 'default' && !/^#/.test(options.theme) ? (' laydate-theme-' + options.theme) : ''
                ].join('')
            })

            //主区域
            ,elemMain = that.elemMain = []
            ,elemHeader = that.elemHeader = []
            ,elemCont = that.elemCont = []
            ,elemTable = that.table = []

            //底部区域
            ,divFooter = that.footer = lay.elem('div', {
                'class': ELEM_FOOTER
            });

        if(options.zIndex) elem.style.zIndex = options.zIndex;

        //单双日历区域
        lay.each(new Array(2), function(i){
            if(!options.range && i > 0){
                return true;
            }

            //头部区域
            var divHeader = lay.elem('div', {
                    'class': 'layui-laydate-header'
                })

                //左右切换
                ,headerChild = [function(){ //上一年
                    var elem = lay.elem('i', {
                        'class': 'layui-icon laydate-icon laydate-prev-y'
                    });
                    elem.innerHTML = '&#xe65a;';
                    return elem;
                }(), function(){ //上一月
                    var elem = lay.elem('i', {
                        'class': 'layui-icon laydate-icon laydate-prev-m'
                    });
                    elem.innerHTML = '&#xe603;';
                    return elem;
                }(), function(){ //年月选择
                    var elem = lay.elem('div', {
                        'class': 'laydate-set-ym'
                    }), spanY = lay.elem('span'), spanM = lay.elem('span');
                    elem.appendChild(spanY);
                    elem.appendChild(spanM);
                    return elem;
                }(), function(){ //下一月
                    var elem = lay.elem('i', {
                        'class': 'layui-icon laydate-icon laydate-next-m'
                    });
                    elem.innerHTML = '&#xe602;';
                    return elem;
                }(), function(){ //下一年
                    var elem = lay.elem('i', {
                        'class': 'layui-icon laydate-icon laydate-next-y'
                    });
                    elem.innerHTML = '&#xe65b;';
                    return elem;
                }()]

                //日历内容区域
                ,divContent = lay.elem('div', {
                    'class': 'layui-laydate-content'
                })
                ,table = lay.elem('table')
                ,thead = lay.elem('thead'), theadTr = lay.elem('tr');

            //生成年月选择
            lay.each(headerChild, function(i, item){
                divHeader.appendChild(item);
            });

            //生成表格
            thead.appendChild(theadTr);
            lay.each(new Array(6), function(i){ //表体
                var tr = table.insertRow(0);
                lay.each(new Array(7), function(j){
                    if(i === 0){
                        var th = lay.elem('th');
                        th.innerHTML = lang.weeks[j];
                        theadTr.appendChild(th);
                    }
                    tr.insertCell(j);
                });
            });
            table.insertBefore(thead, table.children[0]); //表头
            divContent.appendChild(table);

            elemMain[i] = lay.elem('div', {
                'class': 'layui-laydate-main laydate-main-list-'+ i
            });

            elemMain[i].appendChild(divHeader);
            elemMain[i].appendChild(divContent);

            elemHeader.push(headerChild);
            elemCont.push(divContent);
            elemTable.push(table);
        });

        //生成底部栏
        lay(divFooter).html(function(){
            var html = [], btns = [];
            if(options.type === 'datetime'){
                html.push('<span lay-type="datetime" class="laydate-btns-time">'+ lang.timeTips +'</span>');
            }
            lay.each(options.btns, function(i, item){
                var title = lang.tools[item] || 'btn';
                if(options.range && item === 'now') return;
                if(isStatic && item === 'clear') title = options.lang === 'cn' ? '重置' : 'Reset';
                btns.push('<span lay-type="'+ item +'" class="laydate-btns-'+ item +'">'+ title +'</span>');
            });
            html.push('<div class="laydate-footer-btns">'+ btns.join('') +'</div>');
            return html.join('');
        }());

        //插入到主区域
        lay.each(elemMain, function(i, main){
            elem.appendChild(main);
        });
        options.showBottom && elem.appendChild(divFooter);

        //生成自定义主题
        if(/^#/.test(options.theme)){
            var style = lay.elem('style')
                ,styleText = [
                '#{{id}} .layui-laydate-header{background-color:{{theme}};}'
                ,'#{{id}} .layui-this{background-color:{{theme}} !important;}'
            ].join('').replace(/{{id}}/g, that.elemID).replace(/{{theme}}/g, options.theme);

            if('styleSheet' in style){
                style.setAttribute('type', 'text/css');
                style.styleSheet.cssText = styleText;
            } else {
                style.innerHTML = styleText;
            }

            lay(elem).addClass('laydate-theme-molv');
            elem.appendChild(style);
        }

        //移除上一个控件
        that.remove(Class.thisElemDate);

        //如果是静态定位，则插入到指定的容器中，否则，插入到body
        isStatic ? options.elem.append(elem) : (
            document.body.appendChild(elem)
                ,that.position() //定位
        );

        that.checkDate().calendar(); //初始校验
        that.changeEvent(); //日期切换

        Class.thisElemDate = that.elemID;

        typeof options.ready === 'function' && options.ready(lay.extend({}, options.dateTime, {
            month: options.dateTime.month + 1
        }));
    };

    //控件移除
    Class.prototype.remove = function(prev){
        var that = this
            ,options = that.config
            ,elem = lay('#'+ (prev || that.elemID));
        if(!elem.hasClass(ELEM_STATIC)){
            that.checkDate(function(){
                elem.remove();
            });
        }
        return that;
    };

    //定位算法
    Class.prototype.position = function(){
        var that = this
            ,options = that.config
            ,elem = that.bindElem || options.elem[0]
            ,rect = elem.getBoundingClientRect() //绑定元素的坐标
            ,elemWidth = that.elem.offsetWidth //控件的宽度
            ,elemHeight = that.elem.offsetHeight //控件的高度

            //滚动条高度
            ,scrollArea = function(type){
                type = type ? 'scrollLeft' : 'scrollTop';
                return document.body[type] | document.documentElement[type];
            }
            ,winArea = function(type){
                return document.documentElement[type ? 'clientWidth' : 'clientHeight']
            }, margin = 5, left = rect.left, top = rect.bottom;

        //如果右侧超出边界
        if(left + elemWidth + margin > winArea('width')){
            left = winArea('width') - elemWidth - margin;
        }

        //如果底部超出边界
        if(top + elemHeight + margin > winArea()){
            top = rect.top > elemHeight //顶部是否有足够区域显示完全
                ? rect.top - elemHeight
                : winArea() - elemHeight;
            top = top - margin*2;
        }

        if(options.position){
            that.elem.style.position = options.position;
        }
        that.elem.style.left = left + (options.position === 'fixed' ? 0 : scrollArea(1)) + 'px';
        that.elem.style.top = top + (options.position === 'fixed' ? 0 : scrollArea()) + 'px';
    };

    //提示
    Class.prototype.hint = function(content){
        var that = this
            ,options = that.config
            ,div = lay.elem('div', {
            'class': ELEM_HINT
        });

        if(!that.elem) return;

        div.innerHTML = content || '';
        lay(that.elem).find('.'+ ELEM_HINT).remove();
        that.elem.appendChild(div);

        clearTimeout(that.hinTimer);
        that.hinTimer = setTimeout(function(){
            lay(that.elem).find('.'+ ELEM_HINT).remove();
        }, 3000);
    };

    //获取递增/减后的年月
    Class.prototype.getAsYM = function(Y, M, type){
        type ? M-- : M++;
        if(M < 0){
            M = 11;
            Y--;
        }
        if(M > 11){
            M = 0;
            Y++;
        }
        return [Y, M];
    };

    //系统消息
    Class.prototype.systemDate = function(newDate){
        var thisDate = newDate || new Date();
        return {
            year: thisDate.getFullYear() //年
            ,month: thisDate.getMonth() //月
            ,date: thisDate.getDate() //日
            ,hours: newDate ? newDate.getHours() : 0 //时
            ,minutes: newDate ? newDate.getMinutes() : 0 //分
            ,seconds: newDate ? newDate.getSeconds() : 0 //秒
        }
    };

    //日期校验
    Class.prototype.checkDate = function(fn){
        var that = this
            ,thisDate = new Date()
            ,options = that.config
            ,dateTime = options.dateTime = options.dateTime || that.systemDate()
            ,thisMaxDate, error

            ,elem = that.bindElem || options.elem[0]
            ,valType = that.isInput(elem) ? 'val' : 'html'
            ,value = that.isInput(elem) ? elem.value : (options.position === 'static' ? '' : elem.innerHTML)

            //校验日期有效数字
            ,checkValid = function(dateTime){
                if(dateTime.year > LIMIT_YEAR[1]) dateTime.year = LIMIT_YEAR[1], error = true; //不能超过20万年
                if(dateTime.month > 11) dateTime.month = 11, error = true;
                if(dateTime.hours > 23) dateTime.hours = 0, error = true;
                if(dateTime.minutes > 59) dateTime.minutes = 0, dateTime.hours++, error = true;
                if(dateTime.seconds > 59) dateTime.seconds = 0, dateTime.minutes++, error = true;

                //计算当前月的最后一天
                thisMaxDate = laydate.getEndDate(dateTime.month + 1, dateTime.year);
                if(dateTime.date > thisMaxDate) dateTime.date = thisMaxDate, error = true;
            }

            //获得初始化日期值
            ,initDate = function(dateTime, value, index){
                var startEnd = ['startTime', 'endTime'];
                value = (value.match(that.EXP_SPLIT) || []).slice(1);
                index = index || 0;
                if(options.range){
                    that[startEnd[index]] = that[startEnd[index]] || {};
                }
                lay.each(that.format, function(i, item){
                    var thisv = parseFloat(value[i]);
                    if(value[i].length < item.length) error = true;
                    if(/yyyy|y/.test(item)){ //年
                        if(thisv < LIMIT_YEAR[0]) thisv = LIMIT_YEAR[0], error = true; //年不能低于100年
                        dateTime.year = thisv;
                    } else if(/MM|M/.test(item)){ //月
                        if(thisv < 1) thisv = 1, error = true;
                        dateTime.month = thisv - 1;
                    } else if(/dd|d/.test(item)){ //日
                        if(thisv < 1) thisv = 1, error = true;
                        dateTime.date = thisv;
                    } else if(/HH|H/.test(item)){ //时
                        if(thisv < 1) thisv = 0, error = true;
                        dateTime.hours = thisv;
                        options.range && (that[startEnd[index]].hours = thisv);
                    } else if(/mm|m/.test(item)){ //分
                        if(thisv < 1) thisv = 0, error = true;
                        dateTime.minutes = thisv;
                        options.range && (that[startEnd[index]].minutes = thisv);
                    } else if(/ss|s/.test(item)){ //秒
                        if(thisv < 1) thisv = 0, error = true;
                        dateTime.seconds = thisv;
                        options.range && (that[startEnd[index]].seconds = thisv);
                    }
                });
                checkValid(dateTime)
            };

        if(fn === 'limit') return checkValid(dateTime), that;

        value = value || options.value;
        if(typeof value === 'string'){
            value = value.replace(/\s+/g, ' ').replace(/^\s|\s$/g, '');
        }

        //如果点击了开始，单未选择结束就关闭，则重新选择开始
        if(that.startState && !that.endState){
            delete that.startState;
            that.endState = true;
        };

        if(typeof value === 'string' && value){
            if(that.EXP_IF.test(value)){ //校验日期格式
                if(options.range){
                    value = value.split(' '+ options.range +' ');
                    that.startDate = that.startDate || that.systemDate();
                    that.endDate = that.endDate || that.systemDate();
                    options.dateTime = lay.extend({}, that.startDate);
                    lay.each([that.startDate, that.endDate], function(i, item){
                        initDate(item, value[i], i);
                    });
                } else {
                    initDate(dateTime, value)
                }
            } else {
                that.hint('日期格式不合法<br>必须遵循下述格式：<br>'+ (
                    options.range ? (options.format + ' '+ options.range +' ' + options.format) : options.format
                ) + '<br>已为你重置');
                error = true;
            }
        } else if(value && value.constructor === Date){ //如果值为日期对象时
            options.dateTime = that.systemDate(value);
        } else {
            options.dateTime = that.systemDate();
            delete that.startState;
            delete that.endState;
            delete that.startDate;
            delete that.endDate;
            delete that.startTime;
            delete that.endTime;
        }

        checkValid(dateTime);

        if(error && value){
            that.setValue(
                options.range ? (that.endDate ? that.parse() : '') : that.parse()
            );
        }
        fn && fn();
        return that;
    };

    //公历重要日期与自定义备注
    Class.prototype.mark = function(td, YMD){
        var that = this
            ,mark, options = that.config;
        lay.each(options.mark, function(key, title){
            var keys = key.split('-');
            if((keys[0] == YMD[0] || keys[0] == 0) //每年的每月
                && (keys[1] == YMD[1] || keys[1] == 0) //每月的每日
                && keys[2] == YMD[2]){ //特定日
                mark = title || YMD[2];
            }
        });
        mark && td.html('<span class="laydate-day-mark">'+ mark +'</span>');

        return that;
    };

    //无效日期范围的标记
    Class.prototype.limit = function(elem, date, index, time){
        var that = this
            ,options = that.config, timestrap = {}
            ,dateTime = options[index > 41 ? 'endDate' : 'dateTime']
            ,isOut, thisDateTime = lay.extend({}, dateTime, date || {});
        lay.each({
            now: thisDateTime
            ,min: options.min
            ,max: options.max
        }, function(key, item){
            timestrap[key] = that.newDate(lay.extend({
                year: item.year
                ,month: item.month
                ,date: item.date
            }, function(){
                var hms = {};
                lay.each(time, function(i, keys){
                    hms[keys] = item[keys];
                });
                return hms;
            }())).getTime();  //time：是否比较时分秒
        });

        isOut = timestrap.now < timestrap.min || timestrap.now > timestrap.max;
        elem && elem[isOut ? 'addClass' : 'removeClass'](DISABLED);
        return isOut;
    };

    //日历表
    Class.prototype.calendar = function(value){
        var that = this
            ,options = that.config
            ,dateTime = value || options.dateTime
            ,thisDate = new Date(), startWeek, prevMaxDate, thisMaxDate
            ,lang = that.lang()

            ,isAlone = options.type !== 'date' && options.type !== 'datetime'
            ,index = value ? 1 : 0
            ,tds = lay(that.table[index]).find('td')
            ,elemYM = lay(that.elemHeader[index][2]).find('span');

        if(dateTime.year < LIMIT_YEAR[0]) dateTime.year = LIMIT_YEAR[0], that.hint('最低只能支持到公元'+ LIMIT_YEAR[0] +'年');
        if(dateTime.year > LIMIT_YEAR[1]) dateTime.year = LIMIT_YEAR[1], that.hint('最高只能支持到公元'+ LIMIT_YEAR[1] +'年');

        //记录初始值
        if(!that.firstDate){
            that.firstDate = lay.extend({}, dateTime);
        }

        //计算当前月第一天的星期
        thisDate.setFullYear(dateTime.year, dateTime.month, 1);
        startWeek = thisDate.getDay();

        prevMaxDate = laydate.getEndDate(dateTime.month || 12, dateTime.year); //计算上个月的最后一天
        thisMaxDate = laydate.getEndDate(dateTime.month + 1, dateTime.year); //计算当前月的最后一天

        //赋值日
        lay.each(tds, function(index, item){
            var YMD = [dateTime.year, dateTime.month], st = 0;
            item = lay(item);
            item.removeAttr('class');
            if(index < startWeek){
                st = prevMaxDate - startWeek + index;
                item.addClass('laydate-day-prev');
                YMD = that.getAsYM(dateTime.year, dateTime.month, 'sub');
            } else if(index >= startWeek && index < thisMaxDate + startWeek){
                st = index - startWeek;
                if(!options.range){
                    st + 1 === dateTime.date && item.addClass(THIS);
                }
            } else {
                st = index - thisMaxDate - startWeek;
                item.addClass('laydate-day-next');
                YMD = that.getAsYM(dateTime.year, dateTime.month);
            }
            YMD[1]++;
            YMD[2] = st + 1;
            item.attr('lay-ymd', YMD.join('-')).html(YMD[2]);
            that.mark(item, YMD).limit(item, {
                year: YMD[0]
                ,month: YMD[1] - 1
                ,date: YMD[2]
            }, index);
        });

        //同步头部年月
        lay(elemYM[0]).attr('lay-ym', dateTime.year + '-' + (dateTime.month + 1));
        lay(elemYM[1]).attr('lay-ym', dateTime.year + '-' + (dateTime.month + 1));

        if(options.lang === 'cn'){
            lay(elemYM[0]).attr('lay-type', 'year').html(dateTime.year + '年')
            lay(elemYM[1]).attr('lay-type', 'month').html((dateTime.month + 1) + '月');
        } else {
            lay(elemYM[0]).attr('lay-type', 'month').html(lang.month[dateTime.month]);
            lay(elemYM[1]).attr('lay-type', 'year').html(dateTime.year);
        }

        //初始默认选择器
        if(isAlone){
            if(options.range){
                value ? that.endDate = (that.endDate || {
                    year: dateTime.year + (options.type === 'year' ? 1 : 0)
                    ,month: dateTime.month + (options.type === 'month' ? 0 : -1)
                }) : (that.startDate = that.startDate || {
                    year: dateTime.year
                    ,month: dateTime.month
                });
                if(value){
                    that.listYM = [
                        [that.startDate.year, that.startDate.month + 1]
                        ,[that.endDate.year, that.endDate.month + 1]
                    ];
                    that.list(options.type, 0).list(options.type, 1);
                    //同步按钮可点状态
                    options.type === 'time' ? that.setBtnStatus('时间'
                        ,lay.extend({}, that.systemDate(), that.startTime)
                        ,lay.extend({}, that.systemDate(), that.endTime)
                    ) : that.setBtnStatus(true);
                }
            }
            if(!options.range){
                that.listYM = [[dateTime.year, dateTime.month + 1]];
                that.list(options.type, 0);
            }
        }

        //赋值双日历
        if(options.range && !value){
            var EYM = that.getAsYM(dateTime.year, dateTime.month)
            that.calendar(lay.extend({}, dateTime, {
                year: EYM[0]
                ,month: EYM[1]
            }));
        }

        //通过检测当前有效日期，来设定确定按钮是否可点
        if(!options.range) that.limit(lay(that.footer).find(ELEM_CONFIRM), null, 0, ['hours', 'minutes', 'seconds']);

        //标记选择范围
        if(options.range && value && !isAlone) that.stampRange();
        return that;
    };

    //生成年月时分秒列表
    Class.prototype.list = function(type, index){
        var that = this
            ,options = that.config
            ,dateTime = options.dateTime
            ,lang = that.lang()
            ,isAlone = options.range && options.type !== 'date' && options.type !== 'datetime' //独立范围选择器

            ,ul = lay.elem('ul', {
            'class': ELEM_LIST + ' ' + ({
                year: 'laydate-year-list'
                ,month: 'laydate-month-list'
                ,time: 'laydate-time-list'
            })[type]
        })
            ,elemHeader = that.elemHeader[index]
            ,elemYM = lay(elemHeader[2]).find('span')
            ,elemCont = that.elemCont[index || 0]
            ,haveList = lay(elemCont).find('.'+ ELEM_LIST)[0]
            ,isCN = options.lang === 'cn'
            ,text = isCN ? '年' : ''

            ,listYM = that.listYM[index] || {}
            ,hms = ['hours', 'minutes', 'seconds']
            ,startEnd = ['startTime', 'endTime'][index];

        if(listYM[0] < 1) listYM[0] = 1;

        if(type === 'year'){ //年列表
            var yearNum, startY = yearNum = listYM[0] - 7;
            if(startY < 1) startY = yearNum = 1;
            lay.each(new Array(15), function(i){
                var li = lay.elem('li', {
                    'lay-ym': yearNum
                }), ymd = {year: yearNum};
                yearNum == listYM[0] && lay(li).addClass(THIS);
                li.innerHTML = yearNum + text;
                ul.appendChild(li);
                if(yearNum < that.firstDate.year){
                    ymd.month = options.min.month;
                    ymd.date = options.min.date;
                } else if(yearNum >= that.firstDate.year){
                    ymd.month = options.max.month;
                    ymd.date = options.max.date;
                }
                that.limit(lay(li), ymd, index);
                yearNum++;
            });
            lay(elemYM[isCN ? 0 : 1]).attr('lay-ym', (yearNum - 8) + '-' + listYM[1])
                .html((startY + text) + ' - ' + (yearNum - 1 + text));
        } else if(type === 'month'){ //月列表
            lay.each(new Array(12), function(i){
                var li = lay.elem('li', {
                    'lay-ym': i
                }), ymd = {year: listYM[0], month: i};
                i + 1 == listYM[1] && lay(li).addClass(THIS);
                li.innerHTML = lang.month[i] + (isCN ? '月' : '');
                ul.appendChild(li);
                if(listYM[0] < that.firstDate.year){
                    ymd.date = options.min.date;
                } else if(listYM[0] >= that.firstDate.year){
                    ymd.date = options.max.date;
                }
                that.limit(lay(li), ymd, index);
            });
            lay(elemYM[isCN ? 0 : 1]).attr('lay-ym', listYM[0] + '-' + listYM[1])
                .html(listYM[0] + text);
        } else if(type === 'time'){ //时间列表
            //检测时分秒状态是否在有效日期时间范围内
            var setTimeStatus = function(){
                lay(ul).find('ol').each(function(i, ol){
                    lay(ol).find('li').each(function(ii, li){
                        that.limit(lay(li), [{
                            hours: ii
                        }, {
                            hours: that[startEnd].hours
                            ,minutes: ii
                        }, {
                            hours: that[startEnd].hours
                            ,minutes: that[startEnd].minutes
                            ,seconds: ii
                        }][i], index, [['hours'], ['hours', 'minutes'], ['hours', 'minutes', 'seconds']][i]);
                    });
                });
                if(!options.range) that.limit(lay(that.footer).find(ELEM_CONFIRM), that[startEnd], 0, ['hours', 'minutes', 'seconds']);
            };
            if(options.range){
                if(!that[startEnd]) that[startEnd] = {
                    hours: 0
                    ,minutes: 0
                    ,seconds: 0
                };
            } else {
                that[startEnd] = dateTime;
            }
            lay.each([24, 60, 60], function(i, item){
                var li = lay.elem('li'), childUL = ['<p>'+ lang.time[i] +'</p><ol>'];
                lay.each(new Array(item), function(ii){
                    childUL.push('<li'+ (that[startEnd][hms[i]] === ii ? ' class="'+ THIS +'"' : '') +'>'+ lay.digit(ii, 2) +'</li>');
                });
                li.innerHTML = childUL.join('') + '</ol>';
                ul.appendChild(li);
            });
            setTimeStatus();
        }

        //插入容器
        if(haveList) elemCont.removeChild(haveList);
        elemCont.appendChild(ul);

        //年月
        if(type === 'year' || type === 'month'){
            //显示切换箭头
            lay(that.elemMain[index]).addClass('laydate-ym-show');

            //选中
            lay(ul).find('li').on('click', function(){
                var ym = lay(this).attr('lay-ym') | 0;
                if(lay(this).hasClass(DISABLED)) return;

                if(index === 0){
                    dateTime[type] = ym;
                    if(isAlone) that.startDate[type] = ym;
                    that.limit(lay(that.footer).find(ELEM_CONFIRM), null, 0);
                } else { //范围选择
                    if(isAlone){ //非date/datetime类型
                        that.endDate[type] = ym;
                    } else { //date/datetime类型
                        var YM = type === 'year'
                            ? that.getAsYM(ym, listYM[1] - 1, 'sub')
                            : that.getAsYM(listYM[0], ym, 'sub');
                        lay.extend(dateTime, {
                            year: YM[0]
                            ,month: YM[1]
                        });
                    }
                }

                if(options.type === 'year' || options.type === 'month'){
                    lay(ul).find('.'+ THIS).removeClass(THIS);
                    lay(this).addClass(THIS);

                    //如果为年月选择器，点击了年列表，则切换到月选择器
                    if(options.type === 'month' && type === 'year'){
                        that.listYM[index][0] = ym;
                        isAlone && (that[['startDate', 'endDate'][index]].year = ym);
                        that.list('month', index);
                    }
                } else {
                    that.checkDate('limit').calendar();
                    that.closeList();
                }

                that.setBtnStatus(); //同步按钮可点状态
                options.range || that.done(null, 'change');
                lay(that.footer).find(ELEM_TIME_BTN).removeClass(DISABLED);
            });
        } else {
            var span = lay.elem('span', {
                'class': ELEM_TIME_TEXT
            }), scroll = function(){ //滚动条定位
                lay(ul).find('ol').each(function(i){
                    var ol = this
                        ,li = lay(ol).find('li')
                    ol.scrollTop = 30*(that[startEnd][hms[i]] - 2);
                    if(ol.scrollTop <= 0){
                        li.each(function(ii, item){
                            if(!lay(this).hasClass(DISABLED)){
                                ol.scrollTop = 30*(ii - 2);
                                return true;
                            }
                        });
                    }
                });
            }, haveSpan = lay(elemHeader[2]).find('.'+ ELEM_TIME_TEXT);
            scroll()
            span.innerHTML = options.range ? [lang.startTime,lang.endTime][index] : lang.timeTips
            lay(that.elemMain[index]).addClass('laydate-time-show');
            if(haveSpan[0]) haveSpan.remove();
            elemHeader[2].appendChild(span);

            lay(ul).find('ol').each(function(i){
                var ol = this;
                //选择时分秒
                lay(ol).find('li').on('click', function(){
                    var value = this.innerHTML | 0;
                    if(lay(this).hasClass(DISABLED)) return;
                    if(options.range){
                        that[startEnd][hms[i]]  = value;
                    } else {
                        dateTime[hms[i]] = value;
                    }
                    lay(ol).find('.'+ THIS).removeClass(THIS);
                    lay(this).addClass(THIS);

                    setTimeStatus();
                    scroll();
                    (that.endDate || options.type === 'time') && that.done(null, 'change');

                    //同步按钮可点状态
                    that.setBtnStatus();
                });
            });
        }

        return that;
    };

    //记录列表切换后的年月
    Class.prototype.listYM = [];

    //关闭列表
    Class.prototype.closeList = function(){
        var that = this
            ,options = that.config;

        lay.each(that.elemCont, function(index, item){
            lay(this).find('.'+ ELEM_LIST).remove();
            lay(that.elemMain[index]).removeClass('laydate-ym-show laydate-time-show');
        });
        lay(that.elem).find('.'+ ELEM_TIME_TEXT).remove();
    };

    //检测结束日期是否超出开始日期
    Class.prototype.setBtnStatus = function(tips, start, end){
        var that = this
            ,options = that.config
            ,isOut, elemBtn = lay(that.footer).find(ELEM_CONFIRM)
            ,isAlone = options.range && options.type !== 'date' && options.type !== 'time';
        if(isAlone){
            start = start || that.startDate;
            end = end || that.endDate;
            isOut = that.newDate(start).getTime() > that.newDate(end).getTime();

            //如果不在有效日期内，直接禁用按钮，否则比较开始和结束日期
            (that.limit(null, start) || that.limit(null, end))
                ? elemBtn.addClass(DISABLED)
                : elemBtn[isOut ? 'addClass' : 'removeClass'](DISABLED);

            //是否异常提示
            if(tips && isOut) that.hint(
                typeof tips === 'string' ? TIPS_OUT.replace(/日期/g, tips) : TIPS_OUT
            );
        }
    };

    //转义为规定格式的日期字符
    Class.prototype.parse = function(state, date){
        var that = this
            ,options = that.config
            ,dateTime = date || (state
            ? lay.extend({}, that.endDate, that.endTime)
            : (options.range ? lay.extend({}, that.startDate, that.startTime) : options.dateTime))
            ,format = that.format.concat();

        //转义为规定格式
        lay.each(format, function(i, item){
            if(/yyyy|y/.test(item)){ //年
                format[i] = lay.digit(dateTime.year, item.length);
            } else if(/MM|M/.test(item)){ //月
                format[i] = lay.digit(dateTime.month + 1, item.length);
            } else if(/dd|d/.test(item)){ //日
                format[i] = lay.digit(dateTime.date, item.length);
            } else if(/HH|H/.test(item)){ //时
                format[i] = lay.digit(dateTime.hours, item.length);
            } else if(/mm|m/.test(item)){ //分
                format[i] = lay.digit(dateTime.minutes, item.length);
            } else if(/ss|s/.test(item)){ //秒
                format[i] = lay.digit(dateTime.seconds, item.length);
            }
        });

        //返回日期范围字符
        if(options.range && !state){
            return format.join('') + ' '+ options.range +' ' + that.parse(1);
        }

        return format.join('');
    };

    //创建指定日期时间对象
    Class.prototype.newDate = function(dateTime){
        dateTime = dateTime || {};
        return new Date(
            dateTime.year || 1
            ,dateTime.month || 0
            ,dateTime.date || 1
            ,dateTime.hours || 0
            ,dateTime.minutes || 0
            ,dateTime.seconds || 0
        );
    };

    //赋值
    Class.prototype.setValue = function(value){
        var that = this
            ,options = that.config
            ,elem = that.bindElem || options.elem[0]
            ,valType = that.isInput(elem) ? 'val' : 'html'

        options.position === 'static' || lay(elem)[valType](value || '');
        return this;
    };

    //标记范围内的日期
    Class.prototype.stampRange = function(){
        var that = this
            ,options = that.config
            ,startTime, endTime
            ,tds = lay(that.elem).find('td');

        if(options.range && !that.endDate) lay(that.footer).find(ELEM_CONFIRM).addClass(DISABLED);
        if(!that.endDate) return;

        startTime = that.newDate({
            year: that.startDate.year
            ,month: that.startDate.month
            ,date: that.startDate.date
        }).getTime();

        endTime = that.newDate({
            year: that.endDate.year
            ,month: that.endDate.month
            ,date: that.endDate.date
        }).getTime();

        if(startTime > endTime) return that.hint(TIPS_OUT);

        lay.each(tds, function(i, item){
            var ymd = lay(item).attr('lay-ymd').split('-')
                ,thisTime = that.newDate({
                year: ymd[0]
                ,month: ymd[1] - 1
                ,date: ymd[2]
            }).getTime();
            lay(item).removeClass(ELEM_SELECTED + ' ' + THIS);
            if(thisTime === startTime || thisTime === endTime){
                lay(item).addClass(
                    lay(item).hasClass(ELEM_PREV) || lay(item).hasClass(ELEM_NEXT)
                        ? ELEM_SELECTED
                        : THIS
                );
            }
            if(thisTime > startTime && thisTime < endTime){
                lay(item).addClass(ELEM_SELECTED);
            }
        });
    };

    //执行done/change回调
    Class.prototype.done = function(param, type){
        var that = this
            ,options = that.config
            ,start = lay.extend({}, that.startDate ? lay.extend(that.startDate, that.startTime) : options.dateTime)
            ,end = lay.extend({}, lay.extend(that.endDate, that.endTime))

        lay.each([start, end], function(i, item){
            if(!('month' in item)) return;
            lay.extend(item, {
                month: item.month + 1
            });
        });

        param = param || [that.parse(), start, end];
        typeof options[type || 'done'] === 'function' && options[type || 'done'].apply(options, param);

        return that;
    };

    //选择日期
    Class.prototype.choose = function(td){
        var that = this
            ,options = that.config
            ,dateTime = options.dateTime

            ,tds = lay(that.elem).find('td')
            ,YMD = td.attr('lay-ymd').split('-')

            ,setDateTime = function(one){
            var thisDate = new Date();

            //同步dateTime
            one && lay.extend(dateTime, YMD);

            //记录开始日期
            if(options.range){
                that.startDate ? lay.extend(that.startDate, YMD) : (
                    that.startDate = lay.extend({}, YMD, that.startTime)
                );
                that.startYMD = YMD;
            }
        };

        YMD = {
            year: YMD[0] | 0
            ,month: (YMD[1] | 0) - 1
            ,date: YMD[2] | 0
        };

        if(td.hasClass(DISABLED)) return;

        //范围选择
        if(options.range){

            lay.each(['startTime', 'endTime'], function(i, item){
                that[item] = that[item] || {
                    hours: 0
                    ,minutes: 0
                    ,seconds: 0
                };
            });

            if(that.endState){ //重新选择
                setDateTime();
                delete that.endState;
                delete that.endDate;
                that.startState = true;
                tds.removeClass(THIS + ' ' + ELEM_SELECTED);
                td.addClass(THIS);
            } else if(that.startState){ //选中截止
                td.addClass(THIS);

                that.endDate ? lay.extend(that.endDate, YMD) : (
                    that.endDate = lay.extend({}, YMD, that.endTime)
                );

                //判断是否顺时或逆时选择
                if(that.newDate(YMD).getTime() < that.newDate(that.startYMD).getTime()){
                    var startDate = lay.extend({}, that.endDate, {
                        hours: that.startDate.hours
                        ,minutes: that.startDate.minutes
                        ,seconds: that.startDate.seconds
                    });
                    lay.extend(that.endDate, that.startDate, {
                        hours: that.endDate.hours
                        ,minutes: that.endDate.minutes
                        ,seconds: that.endDate.seconds
                    });
                    that.startDate = startDate;
                }

                options.showBottom || that.done();
                that.stampRange(); //标记范围内的日期
                that.endState = true;
                that.done(null, 'change');
            } else { //选中开始
                td.addClass(THIS);
                setDateTime();
                that.startState = true;
            }
            lay(that.footer).find(ELEM_CONFIRM)[that.endDate ? 'removeClass' : 'addClass'](DISABLED);
        } else if(options.position === 'static'){ //直接嵌套的选中
            setDateTime(true);
            that.calendar().done().done(null, 'change');
        } else if(options.type === 'date'){
            setDateTime(true);
            that.setValue(that.parse()).remove().done();
        } else if(options.type === 'datetime'){
            setDateTime(true);
            that.calendar().done(null, 'change');
        }
    };

    //底部按钮
    Class.prototype.tool = function(btn, type){
        var that = this
            ,options = that.config
            ,dateTime = options.dateTime
            ,isStatic = options.position === 'static'
            ,active = {
            //选择时间
            datetime: function(){
                if(lay(btn).hasClass(DISABLED)) return;
                that.list('time', 0);
                options.range && that.list('time', 1);
                lay(btn).attr('lay-type', 'date').html(that.lang().dateTips);
            }

            //选择日期
            ,date: function(){
                that.closeList();
                lay(btn).attr('lay-type', 'datetime').html(that.lang().timeTips);
            }

            //清空、重置
            ,clear: function(){
                that.setValue('').remove();
                isStatic && (
                    lay.extend(dateTime, that.firstDate)
                        ,that.calendar()
                )
                options.range && (
                    delete that.startState
                        ,delete that.endState
                        ,delete that.endDate
                        ,delete that.startTime
                        ,delete that.endTime
                );
                that.done(['', {}, {}]);
            }

            //现在
            ,now: function(){
                var thisDate = new Date();
                lay.extend(dateTime, that.systemDate(), {
                    hours: thisDate.getHours()
                    ,minutes: thisDate.getMinutes()
                    ,seconds: thisDate.getSeconds()
                });
                that.setValue(that.parse()).remove();
                isStatic && that.calendar();
                that.done();
            }

            //确定
            ,confirm: function(){
                if(options.range){
                    if(!that.endDate) return that.hint('请先选择日期范围');
                    if(lay(btn).hasClass(DISABLED)) return that.hint(
                        options.type === 'time' ? TIPS_OUT.replace(/日期/g, '时间') : TIPS_OUT
                    );
                } else {
                    if(lay(btn).hasClass(DISABLED)) return that.hint('不在有效日期或时间范围内');
                }
                that.done();
                that.setValue(that.parse()).remove()
            }
        };
        active[type] && active[type]();
    };

    //统一切换处理
    Class.prototype.change = function(index){
        var that = this
            ,options = that.config
            ,dateTime = options.dateTime
            ,isAlone = options.range && (options.type === 'year' || options.type === 'month')

            ,elemCont = that.elemCont[index || 0]
            ,listYM = that.listYM[index]
            ,addSubYeay = function(type){
            var startEnd = ['startDate', 'endDate'][index]
                ,isYear = lay(elemCont).find('.laydate-year-list')[0]
                ,isMonth = lay(elemCont).find('.laydate-month-list')[0];

            //切换年列表
            if(isYear){
                listYM[0] = type ? listYM[0] - 15 : listYM[0] + 15;
                that.list('year', index);
            }

            if(isMonth){ //切换月面板中的年
                type ? listYM[0]-- : listYM[0]++;
                that.list('month', index);
            }

            if(isYear || isMonth){
                lay.extend(dateTime, {
                    year: listYM[0]
                });
                if(isAlone) that[startEnd].year = listYM[0];
                options.range || that.done(null, 'change');
                that.setBtnStatus();
                options.range || that.limit(lay(that.footer).find(ELEM_CONFIRM), {
                    year: listYM[0]
                });
            }
            return isYear || isMonth;
        };

        return {
            prevYear: function(){
                if(addSubYeay('sub')) return;
                dateTime.year--;
                that.checkDate('limit').calendar();
                options.range || that.done(null, 'change');
            }
            ,prevMonth: function(){
                var YM = that.getAsYM(dateTime.year, dateTime.month, 'sub');
                lay.extend(dateTime, {
                    year: YM[0]
                    ,month: YM[1]
                });
                that.checkDate('limit').calendar();
                options.range || that.done(null, 'change');
            }
            ,nextMonth: function(){
                var YM = that.getAsYM(dateTime.year, dateTime.month);
                lay.extend(dateTime, {
                    year: YM[0]
                    ,month: YM[1]
                });
                that.checkDate('limit').calendar();
                options.range || that.done(null, 'change');
            }
            ,nextYear: function(){
                if(addSubYeay()) return;
                dateTime.year++
                that.checkDate('limit').calendar();
                options.range || that.done(null, 'change');
            }
        };
    };

    //日期切换事件
    Class.prototype.changeEvent = function(){
        var that = this
            ,options = that.config;

        //日期选择事件
        lay(that.elem).on('click', function(e){
            lay.stope(e);
        });

        //年月切换
        lay.each(that.elemHeader, function(i, header){
            //上一年
            lay(header[0]).on('click', function(e){
                that.change(i).prevYear();
            });

            //上一月
            lay(header[1]).on('click', function(e){
                that.change(i).prevMonth();
            });

            //选择年月
            lay(header[2]).find('span').on('click', function(e){
                var othis = lay(this)
                    ,layYM = othis.attr('lay-ym')
                    ,layType = othis.attr('lay-type');

                if(!layYM) return;

                layYM = layYM.split('-');

                that.listYM[i] = [layYM[0] | 0, layYM[1] | 0];
                that.list(layType, i);
                lay(that.footer).find(ELEM_TIME_BTN).addClass(DISABLED);
            });

            //下一月
            lay(header[3]).on('click', function(e){
                that.change(i).nextMonth();
            });

            //下一年
            lay(header[4]).on('click', function(e){
                that.change(i).nextYear();
            });
        });

        //点击日期
        lay.each(that.table, function(i, table){
            var tds = lay(table).find('td');
            tds.on('click', function(){
                that.choose(lay(this));
            });
        });

        //点击底部按钮
        lay(that.footer).find('span').on('click', function(){
            var type = lay(this).attr('lay-type');
            that.tool(this, type);
        });
    };

    //是否输入框
    Class.prototype.isInput = function(elem){
        return /input|textarea/.test(elem.tagName.toLocaleLowerCase());
    };

    //绑定的元素事件处理
    Class.prototype.events = function(){
        var that = this
            ,options = that.config

            //绑定呼出控件事件
            ,showEvent = function(elem, bind){
                elem.on(options.trigger, function(){
                    bind && (that.bindElem = this);
                    that.render();
                });
            };

        if(!options.elem[0] || options.elem[0].eventHandler) return;

        showEvent(options.elem, 'bind');
        showEvent(options.eventElem);

        //绑定关闭控件事件
        lay(document).on('click', function(e){
            if(e.target === options.elem[0]
                || e.target === options.eventElem[0]
                || e.target === lay(options.closeStop)[0]){
                return;
            }
            that.remove();
        }).on('keydown', function(e){
            if(e.keyCode === 13){
                if(lay('#'+ that.elemID)[0] && that.elemID === Class.thisElem){
                    e.preventDefault();
                    lay(that.footer).find(ELEM_CONFIRM)[0].click();
                }
            }
        });

        //自适应定位
        lay(window).on('resize', function(){
            if(!that.elem || !lay(ELEM)[0]){
                return false;
            }
            that.position();
        });

        options.elem[0].eventHandler = true;
    };

    // 对外暴露
    laydate.Class = Class;

    //核心接口
    laydate.render = function(options){
        var inst = new Class(options);
        return thisDate.call(inst);
    };

    //得到某月的最后一天
    laydate.getEndDate = function(month, year){
        var thisDate = new Date();
        //设置日期为下个月的第一天
        thisDate.setFullYear(
            year || thisDate.getFullYear()
            ,month || (thisDate.getMonth() + 1)
            ,1);
        //减去一天，得到当前月最后一天
        return new Date(thisDate.getTime() - 1000*60*60*24).getDate();
    };

    //暴露lay
    window.lay = window.lay || lay;

    //加载方式
    isLayui ? (
        laydate.ready()
            ,layui.define(function(exports){ //layui加载
            laydate.path = layui.cache.dir;
            exports(MOD_NAME, laydate);
        })
    ) : (
        (typeof define === 'function' && define.amd) ? define(function(){ //requirejs加载
            return laydate;
        }) : function(){ //普通script标签加载
            laydate.ready();
            window.laydate = laydate
        }()
    );

}();
