var slideIntervalId;
var animationStop = false;

function preloadAni() {

    'use strict';

    var Box = function (x, y, w, h, s) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.s = s;
        this.a = Math.random() * Math.PI * 2;
        this.hue = Math.random() * 360;
    };

    Box.prototype = {
        constructor: Box,
        update: function () {
            this.a += Math.random() * 0.5 - 0.25;
            this.x += Math.cos(this.a) * this.s;
            this.y += Math.sin(this.a) * this.s;
            this.hue += 5;
            if (this.x > WIDTH) this.x = 0;
            else if (this.x < 0) this.x = WIDTH;
            if (this.y > HEIGHT) this.y = 0;
            else if (this.y < 0) this.y = HEIGHT;
        },
        render: function (ctx) {
            ctx.save();
            ctx.fillStyle = 'hsla(' + this.hue + ', 100%, 50%, 1)';
            ctx.translate(this.x, this.y);
            ctx.rotate(this.a);
            ctx.fillRect(-this.w, -this.h / 2, this.w, this.h);
            ctx.restore();
        }
    };

    var Circle = function (x, y, tx, ty, r) {
        this.x = x;
        this.y = y;
        this.ox = x;
        this.oy = y;
        this.tx = tx;
        this.ty = ty;
        this.lx = x;
        this.ly = y;
        this.r = r;
        this.br = r;
        this.a = Math.random() * Math.PI * 2;
        this.sx = Math.random() * 0.5;
        this.sy = Math.random() * 0.5;
        this.o = Math.random() * 1;
        this.delay = Math.random() * 200;
        this.delayCtr = 0;
        this.hue = Math.random() * 360;
    };

    Circle.prototype = {
        constructor: Circle,
        update: function () {

            if (this.delayCtr < this.delay) {
                this.delayCtr++;
                return;
            }

            this.hue += 1;
            this.a += 0.1;

            this.lx = this.x;
            this.ly = this.y;

            if (!clickToggle) {
                this.x += (this.tx - this.x) * this.sx;
                this.y += (this.ty - this.y) * this.sy;
            } else {
                this.x += (this.ox - this.x) * this.sx;
                this.y += (this.oy - this.y) * this.sy;
            }


            this.r = this.br + Math.cos(this.a) * (this.br * 0.5);
        },
        render: function (ctx) {

            ctx.save();
            ctx.globalAlpha = this.o;
            ctx.fillStyle = 'hsla(' + this.hue + ', 100%, 50%, 1)';
            ctx.translate(this.x, this.y);
            ctx.beginPath();
            ctx.arc(0, 0, this.r, 0, Math.PI * 2);
            ctx.fill();
            ctx.restore();

            if (clickToggle) {
                ctx.save();
                ctx.strokeStyle = 'hsla(' + this.hue + ', 100%, 50%, 1)';
                ctx.beginPath();
                ctx.moveTo(this.lx, this.ly);
                ctx.lineTo(this.x, this.y);
                ctx.stroke();
                ctx.restore();
            }


        }
    };

    var txtCanvas = document.createElement('canvas');
    var txtCtx = txtCanvas.getContext('2d');

    var c = document.getElementById('c');
    var ctx = c.getContext('2d');

    var WIDTH = c.width = window.innerWidth;
    var HEIGHT = c.height = window.innerHeight;
    var imgData = null;
    var idx = null;
    var skip = 4;
    var circles = [];
    var circle = null;
    var a = null;
    var clickToggle = false;
    var boxList = [];
    var box = null;

    txtCanvas.width = WIDTH;
    txtCanvas.height = HEIGHT;

    txtCtx.font = 'bold 120px Arial';
    txtCtx.textAlign = 'center';
    txtCtx.baseline = 'middle';
    txtCtx.fillText('F 3   W O R L D', WIDTH / 2, HEIGHT / 2);

    ctx.font = 'bold 12px Monospace';
    ctx.textAlign = 'center';
    ctx.baseline = 'middle';

    imgData = txtCtx.getImageData(0, 0, WIDTH, HEIGHT).data;

    for (var y = 0; y < HEIGHT; y += skip) {
        for (var x = 0; x < WIDTH; x += skip) {
            idx = (x + y * WIDTH) * 4 - 1;
            if (imgData[idx] > 0) {
                a = Math.PI * 2 * Math.random();
                circle = new Circle(
                    WIDTH / 2 + Math.cos(a) * WIDTH,
                    HEIGHT / 2 + Math.sin(a) * WIDTH,
                    x,
                    y,
                    Math.random() * 4
                );
                circles.push(circle);
            }
        }
    }

    for (var b = 0; b < 10; b++) {
        box = new Box(
            WIDTH * Math.random(),
            HEIGHT * Math.random(),
            5,
            2,
            5 + Math.random() * 5
        );
        boxList.push(box);
    }


    c.addEventListener('click', function (e) {
        clickToggle = !clickToggle;
    });

    var render = function () {
        ctx.globalCompositeOperation = 'source-over';
        ctx.fillStyle = 'rgba(0, 0, 0, 0.5)';
        ctx.fillRect(0, 0, WIDTH, HEIGHT);

        ctx.fillStyle = 'white';
        //ctx.fillText('CLICK TO TOGGLE', WIDTH / 2, HEIGHT / 2 + 100);

        ctx.globalCompositeOperation = 'lighter';

        for (var i = 0, len = circles.length; i < len; i++) {
            circle = circles[i];
            circle.update();
            circle.render(ctx);
        }

        for (var j = 0; j < boxList.length; j++) {
            box = boxList[j];
            box.update();
            box.render(ctx);
        }
    };

    window.requestAnimationFrame = (function () {
        return window.requestAnimationFrame ||
            window.webkitRequestAnimationFrame ||
            window.mozRequestAnimationFrame ||
            function (callback) {
                window.setTimeout(callback, 1000 / 60);
            };
    }());
    (function loop() {
        render();
        if (animationStop) return;
        requestAnimationFrame(loop);
    }());
}

function preload(srcs, isFirst) {
    if (isFirst === "true") {
        preloadAni();
    } else {
        $('.preloader-mask').fadeOut(0);
        return;
    }
    var current = 0, total = srcs.length;
    var _updateText = function (num) {
        current += num;
        if (total - current === 0) {
            $('.preloader-mask').fadeOut(200);
            $(function () {
                slideIntervalId = setInterval(moveRight, 8000)
            });
            animationStop = true;
        }
    };
    var _type = function (name) {
        return {'.jpg': 'image', '.png': 'image'}[name.substring(name.lastIndexOf('.'), name.length)]
    };
    var _load = function (data) {
        data.forEach(function (ele) {
            var type = _type(ele);
            if (type === 'image') {
                var img = new Image();
                img.onload = function () {
                    _updateText(1)
                };
                img.onerror = function () {
                    console.error('preload image faild:' + ele)
                };
                img.src = ele
            }
        })
    };
    _load(srcs);
    _updateText(0);
}

function goDetail() {
    var $body = (window.opera) ? (document.compatMode === "CSS1Compat" ? $('html') : $('body')) : $('html,body');// 这行是 Opera 的补丁, 少了它 Opera 是直接用跳的而且画面闪烁 by willin
    var currentVislble = $('.firstpage-slide:visible')
    var index = $('.firstpage-slide').index(currentVislble)
    switch (index) {
        case 0:
            $body.animate({scrollTop: $('#view-detail').offset().top - 50}, 1000);
            break;
        case 1:
            $body.animate({scrollTop: $('#section3').offset().top - 10}, 1000);
            break;
        case 2:
            $body.animate({scrollTop: $('#section4').offset().top - 35}, 1000);
            break
    }
}

function indicatorClick() {
    var current = $('.slide-indicator.active')
    var currentIndex = $('.slide-indicator').index(current)
    var thisIndex = $('.slide-indicator').index(this)
    if (currentIndex == thisIndex) {
        return
    } else if (currentIndex == 0 && thisIndex == 2) {
        moveLeft(true)
    } else if (currentIndex == 2 && thisIndex == 0) {
        moveRight(true)
    } else if (thisIndex < currentIndex) {
        moveLeft(true)
    } else if (thisIndex > currentIndex) {
        moveRight(true)
    }
}

function scrollAni() {
    var needAnis = $('.animation'),
        pages = $('.page')
    var winHeight, winScrollT, eleOffT, pageOffT, pageHeight, pageInitColor = 255
    winHeight = $(window).height()
    winScrollT = $(this).scrollTop()
    if (winScrollT > 900) {
        $('.gotop').removeClass('hidden')
    } else {
        $('.gotop').addClass('hidden')
    }
    needAnis.each(function () {
        var ele = $(this)
        eleOffT = ele.offset().top + 0 // 多100像素的延迟
        if (winHeight + winScrollT > eleOffT) {
            ele.addClass("animated")
        }
    })
    pages.each(function () {
        var page = $(this)

        pageHeight = page.height()
        pageOffT = page.offset().top
        var rightParam = pageOffT + pageHeight + winHeight - 400,
            leftParam = winHeight + winScrollT
        diff = leftParam - rightParam
        if (leftParam > rightParam) {
            var colorValue = Math.min(255, pageInitColor - Math.ceil(parseFloat(diff) / 3))
            var colorText = 'rgb(' + colorValue + ',' + colorValue + ',' + colorValue + ')'
            page.css('background-color', colorText)
        } else {
            page.css('background-color', 'rgb(255,255,255)')
        }
    })
}

function goTop() {
    var $body = (window.opera) ? (document.compatMode == "CSS1Compat" ? $('html') : $('body')) : $('html,body');
    $body.animate({scrollTop: 0}, 600);
}

function resetInterval() {
    clearInterval(slideIntervalId)
    slideIntervalId = setInterval(moveRight, 8000)
}

function moveLeft(manual) {
    if (manual) {
        resetInterval()
    }
    var currentVislble = $('.firstpage-slide:visible')
    var prev = currentVislble.prev()
    var currentBg = $('.firstpage-bg:visible')
    var prevBg = currentBg.prev()
    var currentIndi = $('.slide-indicator.active')
    var prevIndi = currentIndi.prev()
    if (prev.length == 0) {
        prev = currentVislble.parent().children(':last')
        prevBg = currentBg.parent().children(':last')
        prevIndi = currentIndi.parent().children(':last')
    }
    currentVislble.removeClass('bounceInLeft bounceOutLeft').addClass('bounceOutRight')
    currentBg.removeClass('fadeIn fadeOut').addClass('fadeOut')
    currentIndi.removeClass('active')
    setTimeout(function () {
        currentVislble.addClass('hidden')
        currentBg.addClass('hidden')
        prev.removeClass('hidden bounceInRight bounceOutRight bounceInLeft bounceOutLeft').addClass('bounceInLeft')
        prevBg.removeClass('hidden fadeIn fadeOut').addClass('fadeIn')
        prevIndi.addClass('active')
    }, 400)
}

function moveRight(manual) {
    if (manual) {
        resetInterval()
    }
    var currentVislble = $('.firstpage-slide:visible')
    var next = currentVislble.next()
    var currentBg = $('.firstpage-bg:visible')
    var nextBg = currentBg.next()
    var currentIndi = $('.slide-indicator.active')
    var nextIndi = currentIndi.next()
    if (next.length == 0) {
        next = currentVislble.parent().children(':first')
        nextBg = currentBg.parent().children(':first')
        nextIndi = currentIndi.parent().children(':first')
    }
    currentVislble.removeClass('bounceInRight bounceOutRight').addClass('bounceOutLeft')
    currentBg.removeClass('fadeIn fadeOut').addClass('fadeOut')
    currentIndi.removeClass('active')
    setTimeout(function () {
        currentVislble.addClass('hidden')
        currentBg.addClass('hidden')
        next.removeClass('hidden bounceInLeft bounceOutLeft bounceInRight bounceOutRight').addClass('bounceInRight')
        nextBg.removeClass('hidden fadeIn fadeOut').addClass('fadeIn')
        nextIndi.addClass('active')
    }, 400)
}

function serverInit() {
    var wh = $(window).height();
    if (wh < 715) {
        $("#login-container").css("margin", "0px");
    }

    if (window != top || (document.baseURI || document.URL).indexOf('/login') < 0) {
        top.location.href = location.href;
    }

    var backBg = $("#loginPublish").attr('backBg');
    var loginBg = "/clbs/resources/img/home/" + backBg;
    $("#loginPublish").css('backgroundImage', 'url('+loginBg+')');

    var name = $("#loginLogo").attr('name');
    var webICo = $("#webIco").attr('name');
    var loginLogo = "/clbs/resources/img/logo/" + name;
    $("#loginLogo").attr('src', loginLogo);
    var copyRight = $("#copyRight").attr('name');
    var website = $("#website").attr('name');
    var record = $("#record").attr('name');
    $("#copyRight").html(copyRight);
    $("#website").html(website);
    $("#webIco").attr('href', "/clbs/resources/img/logo/" + webICo + "");
    $("#website").attr('href', 'http://' + website);
    $("#record").html(record);
    var isAdmin = $("#isAdmin").val();
    if (isAdmin != "") {
        $("#commonSmWin").hide();
        window.location.href = "/clbs/login?type=expired";
    }
    /*****************************/
    $('#login_ok').click(function () {
        var userName = $("#username").val();
        $.cookie('userName', userName, {
            expires: 7
        });
        var ip = returnCitySN.cip;
        $.cookie('ip', ip, {
            expires: 7
        })
        setTimeout(function () {
            $("#login_ok").closest('form').submit();
        }, 200)
    })
}

function initCorner() {
    var isIn = false;
    var close = function () {
        setTimeout(function () {
            if (!isIn) {
                $('.corner-show').hide();
            }
        }, 50);
    };
    var show = function () {
        var $show = $('.corner-show'),
            $this = $(this);
        var showHeight = $show.height();
        var thisHeight = $this.height();
        var thisBottom = parseFloat($this.css('bottom'));
        var showBottom = thisBottom - ((showHeight - thisHeight) / 2);
        var src = $this.data('src');

        $show.css('bottom', showBottom + 'px').show();
        $show.find('img').attr('src', src);
    };
    $('.corner-has-show').mouseover(function () {
        isIn = true;
        show.apply(this);
    }).mouseout(function () {
        isIn = false;
        close();
    });
    $('.corner-show').mouseover(function () {
        isIn = true;
    }).mouseout(function () {
        isIn = false;
        close();
    })
}

function init() {
    $(document).ready(function () {

        $('.nav-arrow-right').click(function () {
            moveRight(true)
        })
        $('.nav-arrow-left').click(function () {
            moveLeft(true)
        })
        $('.slide-indicator').click(indicatorClick)
        $('.gotop').click(function () {
            goTop()
        })

        $(window).scroll(scrollAni)
        scrollAni()
        serverInit()
        initCorner()
    });
}
