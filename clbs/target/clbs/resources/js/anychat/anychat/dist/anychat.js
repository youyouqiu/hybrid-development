(function (window) {
    if (!window.anychat) window.anychat = {};
    var ChatObjEventType = function () {
        /**
         * 切换聊天对象
         * @type {string}
         */
        this.OPEN_CHAT_OBJ = "openChatObj";
    };
    window.anychat.chatObjEventType = new ChatObjEventType();
})(window);
(function (window) {
    if (!window.anychat) window.anychat = {};
    var GetUrlParam = function () {
        /**
         * 获取url地址某参数的值
         * @param name 参数名
         * @returns {*}
         */
        this.getUrlParam = function (name) {
            var search = location.search;
            search = search.substring(1);
            var array = search.split("&");
            var paramValue = null;
            for (var i = 0; i < array.length; i++) {
                var str = array[i];
                if (str.indexOf(name + "=") !== -1) {
                    var strArray = str.split("=");
                    paramValue = strArray[1];
                    break;
                }
            }
            return paramValue;
        }
    };
    window.anychat.getUrlParam = new GetUrlParam();
})(window);
(function (window) {
    if (!window.anychat) window.anychat = {};
    var NotificationExt = function () {
        /**
         * 登录成功
         * @type {string}
         */
        this.LOGIN_CHAT_SERVER_SUCCESS = "loginChatServerSuccess";
        /**
         * 用户上线
         * @type {string}
         */
        this.CHAT_USER_ONLINE = "chatUserOnline";
        /**
         * 用户离线
         * @type {string}
         */
        this.CHAT_USER_OFFLINE = "chatUserOffline";
        /**
         * 接到用户信息
         * @type {string}
         */
        this.CHAT_USER_MESSAGE = "chatUserMessage";
        /**
         * 接到组信息
         * @type {string}
         */
        this.CHAT_GROUP_MESSAGE = "chatGroupMessage";
        /**
         * 被踢下线
         * @type {string}
         */
        this.CHAT_KICK = "chatKick";
        /**
         * 通知再重新登录
         * @type {string}
         */
        this.AGAIN_CONNECT = "againConnect";
        /**
         * 获取聊天记录
         * @type {string}
         */
        this.GET_CHAT_LIST = "getChatList";
        /**
         * 接到自己发给别人的信息
         * @type {string}
         */
        this.CHAT_TO_USER_MESSAGE = "chatToUserMessage";
    };
    window.anychat.notificationExt = new NotificationExt();
})(window);
(function (window) {
    if (!window.anychat) window.anychat = {};
    var notificationExt = window.anychat.notificationExt;
    var webSocketEventType = window.juggle.webSocketEventType;
    var Proxy = window.juggle.Proxy;
    var LoginChatProxy = function () {
        Proxy.apply(this);
        this.url = null;
        this.webSocketClient = null;
        /**
         * 初始化，监听消息
         * @param webSocketClient
         */
        this.init = function (webSocketClient) {
            this.webSocketClient = webSocketClient;
            this.webSocketClient.addEventListener(webSocketEventType.getMessage("2"), this.onLoginChatServer, this);
            this.webSocketClient.addEventListener(webSocketEventType.getMessage("3"), this.onUserOnline, this);
            this.webSocketClient.addEventListener(webSocketEventType.getMessage("4"), this.onUserOffline, this);
            this.webSocketClient.addEventListener(webSocketEventType.getMessage("6"), this.onUserMessage, this);
            this.webSocketClient.addEventListener(webSocketEventType.getMessage("7"), this.onGroupMessage, this);
            this.webSocketClient.addEventListener(webSocketEventType.getMessage("10"), this.onUserKick, this);
            this.webSocketClient.addEventListener(webSocketEventType.getMessage("11"), this.onAgainConnect, this);
            this.webSocketClient.addEventListener(webSocketEventType.getMessage("13"), this.onGetMessage, this);
            this.webSocketClient.addEventListener(webSocketEventType.getMessage("14"), this.onToUserMessage, this);
        };
        /**
         * 登录成功
         * @param event
         */
        this.onLoginChatServer = function (event) {
            this.notifyObservers(this.getNotification(notificationExt.LOGIN_CHAT_SERVER_SUCCESS, event.mData));
        };
        /**
         * 好友上线
         * @param event
         */
        this.onUserOnline = function (event) {
            this.notifyObservers(this.getNotification(notificationExt.CHAT_USER_ONLINE, event.mData));
        };
        /**
         * 好友离线
         * @param event
         */
        this.onUserOffline = function (event) {
            this.notifyObservers(this.getNotification(notificationExt.CHAT_USER_OFFLINE, event.mData));
        };
        /**
         * 用户发来消息
         * @param event
         */
        this.onUserMessage = function (event) {
            this.notifyObservers(this.getNotification(notificationExt.CHAT_USER_MESSAGE, event.mData));
            // 回复服务器，这些信息接收到了
            var data = event.mData;
            if (data.message !== null && data.message !== undefined) {
                var messageId = [];
                for (var i = 0; i < data.message.length; i++) {
                    var message = data.message[i];
                    messageId.push(message.chatId);
                }
                this.sendUserMessageReceive(messageId, data.userId);
            }
        };
        /**
         * 组发来消息
         * @param event
         */
        this.onGroupMessage = function (event) {
            this.notifyObservers(this.getNotification(notificationExt.CHAT_GROUP_MESSAGE, event.mData));
            // 回复服务器，这些信息收到了，只告诉服务器最后一条即可
            var data = event.mData;
            if (data.message !== null && data.message !== undefined) {
                this.sendGroupMessageReceive(data.message[data.message.length - 1].chatId, data.chatGroupId);
            }
        };
        /**
         * 被踢下线
         * @param event
         */
        this.onUserKick = function (event) {
            this.notifyObservers(this.getNotification(notificationExt.CHAT_KICK, event.mData));
        };
        /**
         * 通知重新连接服务器
         * @param event
         */
        this.onAgainConnect = function (event) {
            this.notifyObservers(this.getNotification(notificationExt.AGAIN_CONNECT, event.mData));
        };
        /**
         * 获取聊天记录
         * @param event
         */
        this.onGetMessage = function (event) {
            this.notifyObservers(this.getNotification(notificationExt.GET_CHAT_LIST, event.mData));
        };
        /**
         * 发给好友的消息，推送给自己
         * @param event
         */
        this.onToUserMessage = function (event) {
            this.notifyObservers(this.getNotification(notificationExt.CHAT_TO_USER_MESSAGE, event.mData));
        };
        /**
         * 登录聊天服务器
         * @param token
         */
        this.loginChat = function (token) {
            var data = {
                "wsOpCode": 1,
                "token": token
            };
            this.webSocketClient.send(data);
        };
        /**
         * 推送消息
         * @param chatContent
         * @param toType
         * @param toTypeId
         */
        this.sendMessage = function (chatContent, toType, toTypeId) {
            var data = {
                "wsOpCode": 5,
                "chatContent": chatContent,
                "toType": toType,
                "toTypeId": toTypeId
            };
            this.webSocketClient.send(data);
        };
        /**
         * 获取聊天记录
         * @param toType
         * @param toTypeId
         * @param chatCreateTime
         * @param currentPage
         * @param pageSize
         */
        this.getMessage = function (toType, toTypeId, chatCreateTime, currentPage, pageSize) {
            var data = {
                "wsOpCode": 12,
                "toType": toType,
                "toTypeId": toTypeId,
                "chatCreateTime": chatCreateTime,
                "currentPage": currentPage,
                "pageSize": pageSize
            };
            this.webSocketClient.send(data);
        };
        /**
         * 收到某用户的消息
         * @param messageId
         * @param userId
         */
        this.sendUserMessageReceive = function (messageId, userId) {
            var data = {
                "wsOpCode": 8,
                "messageId": messageId,
                "userId": userId
            };
            this.webSocketClient.send(data);
        };
        /**
         * 收到组的消息
         * @param endChatId
         * @param chatGroupId
         */
        this.sendGroupMessageReceive = function (endChatId, chatGroupId) {
            var data = {
                "wsOpCode": 9,
                "endChatId": endChatId,
                "chatGroupId": chatGroupId
            };
            this.webSocketClient.send(data);
        }
    };
    window.anychat.loginChatProxy = new LoginChatProxy();
})(window);
(function (window) {
    if (!window.anychat) window.anychat = {};
    var escape =function (str) {   
        return $('<div>').text(str).html()   
    }; 
    var chatObjEventType = window.anychat.chatObjEventType;
    var EventDispatcher = window.juggle.EventDispatcher;
    var UserObj = function () {
        this.user = null;
        this.view = null;
        this.chatList = [];
        this.shadow = null;
        EventDispatcher.apply(this);
        //初始化
        this.init = function (user) {
            this.user = user;
            this.createView();
        };
        //创建显示对象
        this.createView = function () {
            var view = document.createElement("li");
            if (!this.user.isOnline) {
                view.className = "downLine_P";
            }
            var img;
            if (this.user.userImg === null || this.user.userImg === undefined || this.user.userImg === 'null') {
                if (this.user.isOnline) {
                    img = BASE_PATH +  "anychat/images/new-default.svg";
                }else{
                    img = BASE_PATH +  "anychat/images/new-default-offline.svg";
                }
            } else {
                img = this.user.userImg;
            }
            view.innerHTML = '<img src="' + img + '" alt=""/><span class="right-group-user-name" data-title="' + escape(this.user.userRealName) + '">' + escape(this.user.userRealName) + '</span>';
            this.view = $(view);
            this.view.attr("id", this.user.userId);

        };
        this.onLine = function () {
            this.view.removeClass("downLine_P");
            this.view.find('img').attr('src',BASE_PATH + 'anychat/images/new-default.svg');
        };
        this.offLine = function () {
            this.view.addClass("downLine_P");
            this.view.find('img').attr('src',BASE_PATH + 'anychat/images/new-default-offline.svg');
        };
        //添加监听
        this.addOptListener = function () {
            // this.addClickListener(this, this.onClick);
        };
        this.addClickListener = function (userObj, call) {
            var callFunc = function (event) {
                call.call(userObj, event);
            };
            this.view.on("click", callFunc);
        };
        //点击是派发事件
        this.onClick = function (event) {
            $(".windowL_P li").removeClass("on_P");
            this.view.addClass("on_P");
            this.dispatchEventWith(chatObjEventType.OPEN_CHAT_OBJ);
        }
    };
    window.anychat.UserObj = UserObj;
})(window);

(function (window) {
    if (!window.anychat) window.anychat = {};
    var escape =function (str) {   
        return $('<div>').text(str).html()   
    }; 
    var chatObjEventType = window.anychat.chatObjEventType;
    var EventDispatcher = window.juggle.EventDispatcher;
    var GroupObj = function () {
        this.group = null;
        this.view = null;
        this.chatList = [];
        EventDispatcher.apply(this);
        this.init = function (group) {
            this.group = group;
            this.createView();
        };
        this.createView = function () {
            var view = document.createElement("li");
            view.className = "talkGroup_P";
            view.innerHTML = '<img src="' + BASE_PATH + 'anychat/images/new-group.svg" alt=""/><span class="titleSty2_P" data-title="'+ escape(this.group.chatGroupName) +'">' + escape(this.group.chatGroupName) + '</span>';
            this.view = $(view);
            this.view.find('.titleSty2_P').mouseenter(function () {
                var _this = $(this);
                $(this).data('timeout', setTimeout(function () {
                    var content = _this.data('title')
                    _this.justToolsTip({
                        animation: "moveInTop",
                        width: "auto",
                        contents: content,
                        gravity: 'top',
                        distance:20,
                    });
                }, 3000));
            }).mouseleave(function () {
                clearTimeout($(this).data('timeout'));
            });
        };
        this.addOptListener = function () {
            this.addClickListener(this, this.onClick);
        };
        this.addClickListener = function (groupObj, call) {
            var callFunc = function (event) {
                call.call(groupObj, event);
            };
            this.view.on("click", callFunc);
        };
        this.onClick = function (event) {
            $(".windowL_P li").removeClass("on_P");
            this.view.addClass("on_P");
            this.dispatchEventWith(chatObjEventType.OPEN_CHAT_OBJ);
        }
    };
    window.anychat.GroupObj = GroupObj;
})(window);

(function (window) {

    $.fn.getCursorPosition = function () {
        var el = $(this).get(0), pos = 0;
        if ("selectionStart" in el) {
            pos = el.selectionStart;
        } else if ("selection" in document) {
            el.focus();
            var sel = document.selection.createRange(), selLength = document.selection.createRange().text.length;
            sel.moveStart("character", -el.value.length);
            pos = sel.text.length - selLength;
        }
        return pos;
    }
    if (!window.anychat) window.anychat = {};
    var chatObjEventType = window.anychat.chatObjEventType;
    var getUrlParam = window.anychat.getUrlParam;
    var notificationExt = window.anychat.notificationExt;
    var loginChatProxy = window.anychat.loginChatProxy;
    var GroupObj = window.anychat.GroupObj;
    var UserObj = window.anychat.UserObj;
    var Mediator = window.juggle.Mediator;
    var WebSocketClient = window.juggle.WebSocketClient;
    var webSocketEventType = window.juggle.webSocketEventType;
    var TalkMediator = function () {
        //webscoket对象
        this.webSocketClient = null;
        //聊天组map
        this.groupMap = {};
        //好友列表map
        this.userMap = {};
        // 用户组，好友对照map
        this.groupUserMap = [];
        //当前打开谁的对话框
        this.nowToObj = null;
        //自己的数据
        this.own = null;
        //在线好友数量
        this.onlineNum = 0;
        //好友数量
        this.userNum = 0;
        //每页几条
        this.pageSize = 20;
        //当前页数
        this.currentPage = 0;
        //总页数
        this.totalPage = 0;
        this.maxPage = 9999;
        //当前模块 1代表聊天 2代表聊天记录
        this.nowModule = 1;
        this.token = null;
        // 上次消息的发送时间
        this.lastChatCreateTime = null;
        // 表情html
        this.faceContent = null;
        // 提示消息的过期id
        this.noticeTimeoutId = null;
        this.initView = function (view) {
            //获取url传递过来的token
            this.token = getUrlParam.getUrlParam("token");
            if (this.token === null || this.token === undefined) {
                //token是空应该提示无法登陆
                return;
            }
            //链接聊天服务器
            this.loginChatServer();
            //添加发送聊天信息的监听
            this.addSendChat(this, this.onSendChat);
            this.addHistory(this, this.onClickHistory);
            this.addFirstPage(this, this.onFirstPage);
            this.addPrePage(this, this.onPrePage);
            this.addNextPage(this, this.onNextPage);
            this.addLastPage(this, this.onLastPage);
            this.addReturnChat(this, this.onReturnChat);


            /** 给textarea添加自定义滚动条 */
            var textareaLineHeight = parseInt($(".textarea-wrapper textarea").css("line-height"));
            if (isNaN(textareaLineHeight)) {
                textareaLineHeight = 25;
            }


            $(".textarea-wrapper").mCustomScrollbar({
                scrollInertia: 0,
                theme: "minimal",
                advanced: { autoScrollOnFocus: false },
                mouseWheel: { disableOver: ["select", "option", "keygen", "datalist", ""] },
                keyboard: { enable: false },
                snapAmount: textareaLineHeight
            });

            var textarea = $(".textarea-wrapper textarea"), textareaWrapper = $(".textarea-wrapper"), textareaClone = $(".textarea-wrapper .textarea-clone");
            var self = this;
            textarea.bind("keyup keydown", function (e) {

                // 回车
                if (e.keyCode === 13 && e.shiftKey === false && e.type === 'keydown') {
                    e.preventDefault();
                    $('#sendChat').trigger('click');
                    return false;
                }

                // 退格键
                if (e.keyCode === 8 && e.type === 'keydown') {
                    var $this = $(this), textareaContent = $this.val(), clength = textareaContent.length, cursorPosition = textarea.getCursorPosition();
                    var content4Backspace = textareaContent.substr(0, cursorPosition);
                    var rail4Backspace = textareaContent.substr(cursorPosition, textareaContent.length);
                    if (content4Backspace.length > 0) {
                        var lastRightIndex = content4Backspace.length - 1;
                        var rightQuote = content4Backspace.charAt(lastRightIndex);
                        if (rightQuote === ']') {
                            var leftQuoteIndex = content4Backspace.lastIndexOf('[');
                            var quoteContent = content4Backspace.substring(leftQuoteIndex, lastRightIndex + 1);
                            if (self.checkInFace(quoteContent)) {
                                e.preventDefault();
                                var chunckedContent = content4Backspace.substring(0, leftQuoteIndex) + rail4Backspace;
                                $this.val(chunckedContent)
                                // 设置光标到删除的位置
                                $this[0].selectionEnd = leftQuoteIndex;
                                return false;
                            }
                        }
                    }
                }

                // 其他键
                self.copyTextarea2Clone();
            });
            $(".windowL_P>dd").mCustomScrollbar({
                theme: "minimal",
                advanced: { autoExpandHorizontalScroll: true },
                scrollbarPosition: "outside"
            });
            $(".talkCon_P").mCustomScrollbar({
                theme: "minimal",
                advanced: { autoExpandHorizontalScroll: true },
                scrollbarPosition: "outside"
            });
            $(".hisList_P").mCustomScrollbar({
                theme: "minimal",
                advanced: { autoExpandHorizontalScroll: true },
                scrollbarPosition: "outside"
            });
            var tipContent = $('#tipContent').html();

            $(".tips_P").mouseover(function () {
                var _this = $(this);
                $('.face_P').removeClass('active');
                _this.justToolsTip({
                    animation: "moveInTop",
                    width: "auto",
                    contents: tipContent,
                    gravity: 'top',
                    distance: 30
                });
            });

            $('.face_P').click(function (event) {
                event.stopPropagation();
                var _this = $(this);
                if (_this.hasClass('active')) {
                    _this.removeClass('active');
                } else {
                    _this.addClass('active');
                }
                _this.justToolsTip({
                    animation: "moveInTop",
                    width: "auto",
                    contents: self.faceContent,
                    gravity: 'top',
                    distance: 30,
                    events: 'click',
                    onRemove: function () {
                        _this.removeClass('active');
                    }
                });
                $('.emoji_face a,.favor-item span').on('click', function () {
                    var textarea = $('#sendChatContent');
                    var $this = $(this);
                    var text = $this.attr('title');

                    var textareaContent = textarea.val(), clength = textareaContent.length, cursorPosition = textarea.getCursorPosition();
                    var contentBefore = textareaContent.substr(0, cursorPosition);
                    var contentAfter = textareaContent.substr(cursorPosition, textareaContent.length);

                    var destText = contentBefore + text + contentAfter;
                    textarea.val(destText);

                    // 设置光标到新增的位置
                    $this[0].selectionEnd = cursorPosition + text.length;
                    self.copyTextarea2Clone();
                });

                $('.tab-icon').click(function () {
                    var $this = $(this);
                    if ($this.hasClass('active')) {
                        return;
                    }
                    var target = $this.data('target');
                    $('.tab-icon').removeClass('active');
                    $('.tab-content').removeClass('active');
                    $this.addClass('active');
                    $(target).addClass('active');
                })
            });
            $('.close,.close-window').click(function () {
                if (window.top && window.top.pagesNav) {
                    window.top.pagesNav.closeChat();
                }
            });
            this.initFace();
        };
        this.notice = function (messageParam, typeParam, timeoutParam) {
            var message = messageParam;
            var type = typeParam || 'error';
            var timeout = timeoutParam || 3000; // 毫秒
            var tmpl = ['<div class="prompt-container">',
                '<span class="prompt-wraper">',
                '<i class="prompt-icon"></i>',
                '$msg',
                '</span>',
                '</div>'].join('');
            var container = $('.funList_PJY');
            container.find('.prompt-container').remove();
            container.append($(tmpl.replace('$msg', message)));
            if (this.noticeTimeoutId) {
                clearTimeout(this.noticeTimeoutId);
            }
            this.noticeTimeoutId = setTimeout(function () {
                var container = $('.funList_PJY');
                container.find('.prompt-container').remove();
            }, timeout)
        };
        // 根据JSON数据自动构建表情HTML
        this.initFace = function () {
            var faceJson = window.anychat.faceJson.FACE_JSON;
            var emojiContainer = $('.emoji_face');
            var favorContainer = $('.favor-container');
            var emojiTmpl = '<a title="$name" type="emoji" class="face $className"></a>';
            var gifTmpl = '<div class="favor-item"><span title="$name" class="select-favor-span $className"></span></div>';
            for (var i = 0; i < faceJson.length; i++) {
                var element = faceJson[i];
                var type = element.type;
                var keys = Object.keys(element);
                var name = keys[0];
                var className = element[name];
                var html;
                if (type === 'gif') {
                    html = gifTmpl.replace('$name', name).replace('$className', className);
                    favorContainer.append($(html));
                } else {
                    html = emojiTmpl.replace('$name', name).replace('$className', className);
                    emojiContainer.append($(html));
                }
            }
            this.faceContent = $('#faceContent').html();
            $('#faceContent').remove();
        };
        // 将textarea中的内容复制到clone div 中
        this.copyTextarea2Clone = function (e) {
            var escape = function (str) {
                return $('<div>').text(str).html()
            };
            var textarea = $(".textarea-wrapper textarea"), textareaWrapper = $(".textarea-wrapper"), textareaClone = $(".textarea-wrapper .textarea-clone");
            var textareaLineHeight = parseInt($(".textarea-wrapper textarea").css("line-height"));
            if (isNaN(textareaLineHeight)) {
                textareaLineHeight = 25;
            }

            var textareaContent = textarea.val(), clength = textareaContent.length, cursorPosition = textarea.getCursorPosition();

            // textareaContent="<span>"+escape(textareaContent.substr(0,cursorPosition))+"</span>"+escape(textareaContent.substr(cursorPosition,textareaContent.length));
            textareaContent = "<span>" + escape(textareaContent.substr(0, cursorPosition)) + escape(textareaContent.substr(cursorPosition, textareaContent.length)) + "</span>";
            textareaContent = textareaContent.replace(/\n/g, "<br />");
            // textareaClone.html(textareaContent+"<br />");
            textareaClone.html(textareaContent);
            textarea.css("height", textareaClone.height());

            var textareaCloneSpan = textareaClone.children("span"), textareaCloneSpanOffset = 0;
            var spanHeight = textareaCloneSpan.height();
            var viewLimitBottom = (parseInt(textareaClone.css("min-height"))) - textareaCloneSpanOffset, viewLimitTop = textareaCloneSpanOffset,
                viewRatio = Math.round(spanHeight + textareaWrapper.find(".mCSB_container").position().top);

            if (viewRatio > viewLimitBottom || viewRatio < viewLimitTop) {
                if ((spanHeight - textareaCloneSpanOffset) > 0) {
                    textareaWrapper.mCustomScrollbar("scrollTo", -textareaCloneSpanOffset - textareaLineHeight);
                } else {
                    textareaWrapper.mCustomScrollbar("scrollTo", "top");
                }
            }
        };
        this.addSendChat = function (mediator, call) {
            var callFunc = function (event) {
                call.call(mediator, event);
            };
            $("#sendChat").on("click", callFunc);
        };
        this.addHistory = function (mediator, call) {
            var callFunc = function (event) {
                call.call(mediator, event);
            };
            $(".history_P").on("click", callFunc);
        };
        this.addFirstPage = function (mediator, call) {
            var callFunc = function (event) {
                call.call(mediator, event);
            };
            $("#firstPage").on("click", callFunc);
        };
        this.addPrePage = function (mediator, call) {
            var callFunc = function (event) {
                call.call(mediator, event);
            };
            $("#prePage").on("click", callFunc);
        };
        this.addNextPage = function (mediator, call) {
            var callFunc = function (event) {
                call.call(mediator, event);
            };
            $("#nextPage").on("click", callFunc);
        };
        this.addLastPage = function (mediator, call) {
            var callFunc = function (event) {
                call.call(mediator, event);
            };
            $("#lastPage").on("click", callFunc);
        };
        this.addReturnChat = function (mediator, call) {
            var callFunc = function (event) {
                call.call(mediator, event);
            };
            $("#returnChat").on("click", callFunc);
        };
        //链接聊天服务器
        this.loginChatServer = function () {
            this.logoutChatServer();

            this.webSocketClient = new WebSocketClient(loginChatProxy.url);
            this.webSocketClient.addEventListener(webSocketEventType.CONNECTED, this.onConnected, this);
            this.webSocketClient.addEventListener(webSocketEventType.CLOSE, this.onClose, this);
            loginChatProxy.init(this.webSocketClient);
        };
        this.onClose = function (event) {
            // console.log("与服务器断开链接");
            // window.location.reload();
        };
        //断开聊天服务器
        this.logoutChatServer = function () {
            if (this.webSocketClient !== null && this.webSocketClient !== undefined) {
                this.webSocketClient.close();
                this.webSocketClient.removeEventListeners();
                this.webSocketClient = null;
            }
        };
        //链接成功后发送登陆请求
        this.onConnected = function (event) {
            loginChatProxy.loginChat(this.token);
        };
        // 关心消息数组
        this.listNotificationInterests = [
            notificationExt.LOGIN_CHAT_SERVER_SUCCESS,
            notificationExt.CHAT_USER_ONLINE,
            notificationExt.CHAT_USER_OFFLINE,
            notificationExt.CHAT_USER_MESSAGE,
            notificationExt.CHAT_GROUP_MESSAGE,
            notificationExt.CHAT_KICK,
            notificationExt.AGAIN_CONNECT,
            notificationExt.GET_CHAT_LIST,
            notificationExt.CHAT_TO_USER_MESSAGE];
        // 关心的消息处理
        this.handleNotification = function (data) {
            switch (data.name) {
                case notificationExt.LOGIN_CHAT_SERVER_SUCCESS:
                    this.LoginChatServerSuccess(data.body);
                    break;
                case notificationExt.CHAT_USER_ONLINE:
                    this.userOnline(data.body);
                    break;
                case notificationExt.CHAT_USER_OFFLINE:
                    this.userOffline(data.body);
                    break;
                case notificationExt.CHAT_USER_MESSAGE:
                    this.onUserMessage(data.body);
                    break;
                case notificationExt.CHAT_GROUP_MESSAGE:
                    this.onGroupMessage(data.body);
                    break;
                case notificationExt.CHAT_KICK:
                    // console.log("你即将被踢下线");
                    break;
                case notificationExt.AGAIN_CONNECT:
                    loginChatProxy.loginChat(this.token);
                    break;
                case notificationExt.GET_CHAT_LIST:
                    this.getChatListSuccess(data.body);
                    break;
                case notificationExt.CHAT_TO_USER_MESSAGE:
                    this.onToUserMessage(data.body);
                    break;
            }
        };
        this.onReturnChat = function (event) {
            this.nowModule = 1;
            $(event.target).parents(".windowR_P.left2_P").hide().prev().show();
            //下拉滚动条
            $('.talkCon_P').mCustomScrollbar('scrollTo', 'bottom')
        };
        //设置全局map数组的状态
        this.setGroupUserMapOnline = function (userObj) {
            for (var i = 0; i < this.groupUserMap.length; i++) {
                var groupUsers = this.groupUserMap[i].users;
                for (var j = 0; j < groupUsers.length; j++) {
                    var u = groupUsers[j];
                    if (u.userId === userObj.user.userId) {
                        u.isOnline = userObj.user.isOnline;
                    }
                }
            }
        };

        this.checkInCurrentGroup = function (userObj) {
            var inCurrentGroup = false;
            var currentChatGroupId = this.nowToObj.group.chatGroupId;
            var existGroup = null;
            for (var i = 0; i < this.groupUserMap.length; i++) {
                var ele = this.groupUserMap[i];
                if (ele.chatGroupId == currentChatGroupId) {
                    existGroup = ele;
                    break;
                }
            }
            if (existGroup) {
                var inGroup = null;
                for (var i = 0; i < existGroup.users.length; i++) {
                    var ele = existGroup.users[i];
                    if (ele.userId == userObj.user.userId) {
                        inGroup = ele;
                        break;
                    }
                }
                if (inGroup) {
                    inCurrentGroup = true;
                }
            }
            return inCurrentGroup;
        };

        this.getCurrentGroupUserName = function () {
            var name = '';
            var currentChatGroupId = this.nowToObj.group.chatGroupId;
            var existGroup = null;
            for (var i = 0; i < this.groupUserMap.length; i++) {
                var ele = this.groupUserMap[i];
                if (ele.chatGroupId == currentChatGroupId) {
                    existGroup = ele;
                    break;
                }
            }
            if (existGroup) {
                name = existGroup.users.map(function(ele){
                    return ele.userRealName;
                }).join(','); 
            }
            return name;
        };

        this.getOnlineFromArray = function (userObj) {
            var num = 0;
            var currentChatGroupId = this.nowToObj.group.chatGroupId;
            var existGroup = null;
            for (var i = 0; i < this.groupUserMap.length; i++) {
                var ele = this.groupUserMap[i];
                if (ele.chatGroupId == currentChatGroupId) {
                    existGroup = ele;
                    break;
                }
            }
            if (existGroup) {
                for (var i = 0; i < existGroup.users.length; i++) {
                    var ele = existGroup.users[i];
                    if (ele.isOnline) {
                        num++;
                    }
                }
            }
            return num;
        };

        //好友上线的操作
        this.userOnline = function (body) {
            // console.log('用户上线：',body.chatUser.userRealName);
            // console.log('当前组在线用户：', this.getCurrentGroupUserName());
            
            
            var userObj = this.userMap[body.chatUser.userId];
            //说明在好友列表里
            if (userObj !== null && userObj !== undefined) {
                userObj.user = body.chatUser;
                userObj.user.isOnline = true;
                userObj.onLine();
                //设置全局map数组的状态
                this.setGroupUserMapOnline(userObj);
                // 如果不在当前组，就不管了
                var inCurrentGroup = this.checkInCurrentGroup(userObj);
                if (!inCurrentGroup) {
                    return;
                }
                
                this.onlineNum++;
                // var onlineFromArray = this.getOnlineFromArray(userObj);

                $("#currentOnlineGroup").text(this.onlineNum);
                //设置靠前
                var childNodes = $("#talkUserList")[0].childNodes;
                var displayNode;
                for (var i = 0; i < childNodes.length; i++) {
                    var node = childNodes[i];
                    if (node.id !== null && node.id !== undefined) {
                        displayNode = node;
                        break;
                    }
                }
                if (displayNode !== null && displayNode !== undefined) {
                    if (displayNode.id !== userObj.user.userId) {
                        $("#" + displayNode.id).before($("#" + userObj.user.userId));
                    }
                }
            } else {
                //console.log('发生了用户不在初始全局组的情况，用户：', body.chatUser, '全局组：', this.userMap);
                // console.log('上线用户不在当前全局组：',this.userMap);
                //说明是新进入公司的员工，需要加入好友列表
                // var userObj = new UserObj();
                // userObj.init(body.chatUser);
                // this.userMap[body.chatUser.userId] = userObj;
                // $("#talkUserList").prepend(userObj.view);
                // userObj.addOptListener();
                // userObj.addEventListener(chatObjEventType.OPEN_CHAT_OBJ, this.openChatObjHandle, this);
                // this.onlineNum++;
                // this.userNum++;
                // $("#currentOnlineGroup").text(this.onlineNum);
                // $("#allUser").text(this.userNum);
            }
        };
        //好友下线
        this.userOffline = function (body) {
            
            var userObj = this.userMap[body.userId];
            if (userObj !== null && userObj !== undefined) {
                // console.log('用户下线：',userObj.user.userRealName);
                userObj.user.isOnline = false;
                userObj.offLine();
                //设置全局map数组的状态
                this.setGroupUserMapOnline(userObj);
                // 如果不在当前组，就不管了
                var inCurrentGroup = this.checkInCurrentGroup(userObj);
                if (!inCurrentGroup) {
                    return;
                }
                
                this.onlineNum--;
                // var onlineFromArray = this.getOnlineFromArray(userObj);
                $("#currentOnlineGroup").text(this.onlineNum);
                //设置靠后
                var childNodes = $("#talkUserList")[0].childNodes;
                var displayNode;
                for (var i = childNodes.length - 1; i >= 0; i--) {
                    var node = childNodes[i];
                    if (node.id !== null && node.id !== undefined) {
                        displayNode = node;
                        break;
                    }
                }
                if (displayNode !== null && displayNode !== undefined) {
                    if (displayNode.id !== userObj.user.userId) {
                        $("#" + displayNode.id).after($("#" + userObj.user.userId));
                    }
                }
            } else {
                //这种情况不可能发生
                // console.log('下线用户不在当前全局组');
            }

        };
        //登陆成功
        this.LoginChatServerSuccess = function (body) {
            // console.log('login chart server success :', body);
            //设置自己的信息
            var formatUser2Group = function (user) {
                if (!user.userGroupList) {
                    user.userGroupList = [];
                }
                for (var j = 0; j < user.userGroupList.length; j++) {
                    var groupIndex = user.userGroupList[j];
                    for (var k = 0; k < this.groupUserMap.length; k++) {
                        if (this.groupUserMap[k].chatGroupId === groupIndex) {
                            if (this.groupUserMap[k].users) {
                                // 判断是否已存在
                                var isExist = false;
                                for (var l = 0; l < this.groupUserMap[k].users.length; l++) {
                                    var element = this.groupUserMap[k].users[l];
                                    if (element.userId == user.userId) {
                                        isExist = true;
                                        break;
                                    }
                                }
                                if (!isExist) {
                                    this.groupUserMap[k].users.push(user);
                                }
                            } else {
                                this.groupUserMap[k].users = [user];
                            }
                        }
                    }
                }
                var userObj = new UserObj();
                userObj.init(user);
                this.userMap[user.userId] = userObj;
            }
            this.own = body.chatUser;
            // 清理dom
            $("#talkList").empty();
            var firstGroup;
            // top 窗口的id数组
            var groupIds = [];
            //初始化聊天组
            if (body.chatGroupList !== null && body.chatGroupList !== undefined) {
                for (var i = 0; i < body.chatGroupList.length; i++) {
                    var userGroup = body.chatGroupList[i];
                    groupIds.push(userGroup.chatGroupId);
                    // 填充组
                    this.groupUserMap.push(userGroup);
                    var groupObj = new GroupObj();
                    groupObj.init(userGroup);
                    this.groupMap[userGroup.chatGroupId] = groupObj;
                    $("#talkList").prepend(groupObj.view);
                    groupObj.addOptListener();
                    groupObj.addEventListener(chatObjEventType.OPEN_CHAT_OBJ, this.openChatObjHandle, this);
                    //默认打开第一个聊天组
                    if (i === 0) {
                        firstGroup = groupObj;
                    }
                }
            }


            //初始化好友列表
            if (body.chatUserList !== null && body.chatUserList !== undefined) {
                for (var i = 0; i < body.chatUserList.length; i++) {
                    var user = body.chatUserList[i];
                    formatUser2Group.call(this, user);
                }
            }
            //将自己的信息也归入组里
            formatUser2Group.call(this, this.own);
            //默认打开第一个群聊天窗口
            this.showGroupUser({
                mTarget: firstGroup
            });
            this.nowToObj = firstGroup;
            this.nowToObj.view.addClass("on_P");

            $("#toTypeName").text(this.nowToObj.group.chatGroupName);
            $("#toTypeName2").text(this.nowToObj.group.chatGroupName);

            $('#talkUserList').on('mouseenter', '.right-group-user-name', function () {
                var _this = $(this);
                $(this).data('timeout', setTimeout(function () {
                    var content = _this.data('title')
                    _this.justToolsTip({
                        animation: "moveInTop",
                        width: "auto",
                        contents: content,
                        gravity: 'top',
                        distance:20,
                    });
                }, 3000));
            }).on('mouseleave','.right-group-user-name',function () {
                clearTimeout($(this).data('timeout'));
            });

        };
        //渲染组对应的用户列表
        this.showGroupUser = function (event) {
            //如果没变化，返回
            if (event.mTarget === this.nowToObj) {
                return;
            }
            $('#talkUserList').empty();
            this.onlineNum = 0;
            var groupUsers = event.mTarget.group.users;
            for (var i = 0; i < groupUsers.length; i++) {
                var user = groupUsers[i];

                var userObj = null;
                if (this.userMap[user.userId]) {
                    userObj = this.userMap[user.userId];
                }else{
                    userObj = new UserObj();
                    userObj.init(user);
                    this.userMap[user.userId] = userObj;
                }
                if (user.isOnline) {
                    $("#talkUserList").prepend(userObj.view);
                } else {
                    $("#talkUserList").append(userObj.view);
                }

                userObj.addOptListener();
                userObj.addEventListener(chatObjEventType.OPEN_CHAT_OBJ, this.openChatObjHandle, this);
                if (user.isOnline) {
                    this.onlineNum++;
                }
            }

            $('#chatMemberNumber').html('(' + groupUsers.length + ')');
            $('#currentOnlineGroup').html(this.onlineNum);
            $('#allGroupUser').html(groupUsers.length);
            $('#groupUserDD').mCustomScrollbar("scrollTo", "top");
        },
            this.nvlName = function (user) {
                return user.userRealName == 'null' ? user.userId : user.userRealName
            }
        //切换聊天对象
        this.openChatObjHandle = function (event) {
            //如果没变化，返回
            if (event.mTarget === this.nowToObj) {
                return;
            }
            this.showGroupUser(event);
            //设置当前聊天对象，清空聊天记录
            this.nowToObj = event.mTarget;
            $("#chatList .mCSB_container")[0].innerHTML = "";
            //如果是用户
            if (event.mTarget.user !== null && event.mTarget.user !== undefined) {
                $("#toTypeName").text(this.nvlName(this.nowToObj));
                this.openChat(event.mTarget.chatList);
                //修改聊天记录的
                $("#toTypeName2").text(this.nvlName(this.nowToObj));
            } else {
                //如果是聊天组
                $("#toTypeName").text(this.nowToObj.group.chatGroupName);
                this.openChat(event.mTarget.chatList);
                //修改聊天记录的
                $("#toTypeName2").text(this.nowToObj.group.chatGroupName);
            }
            //设置这个对象没有未读记录
            event.mTarget.view.removeClass("online_P");
            //如果聊天记录打开，需要切换聊天记录的内容
            if (this.nowModule === 2) {
                this.getHistoryChatList(this.maxPage);
            }

        };
        //显示聊天信息
        this.openChat = function (chatList) {
            for (var i = 0; i < chatList.length; i++) {
                var chat = chatList[i];
                var view;
                //如果是自己则聊天内容在右边
                if (chat.userId === this.own.userId) {
                    view = this.createOwnChat(chat);
                } else {
                    view = this.createOtherUserChat(chat);
                }
                var chatCreateTime = chat.chatCreateTime;
                // 显示信息时间
                this.renderMsgTime(chatCreateTime);
                $("#chatList .mCSB_container").append(view);
            }
            //下拉滚动条
            $('.talkCon_P').mCustomScrollbar('scrollTo', 'bottom')
        };
        // 智能格式化日期
        this.formatTime = function (date) {
            var now = new Date();
            var nowYear = now.getFullYear();
            var nowMonth = now.getMonth() + 1;
            var nowDay = now.getDate();
            var nowHour = now.getHours();
            var thisYear = date.getFullYear();
            var thisMonth = date.getMonth() + 1;
            var thisDay = date.getDate();
            var thisHour = date.getHours();
            var thisMinute = date.getMinutes();
            var thisSecond = date.getSeconds();
            // 给时分秒加零
            var formatHMS = function () {
                if (thisHour < 10) thisHour = '0' + thisHour.toString();
                if (thisMinute < 10) thisMinute = '0' + thisMinute.toString();
                if (thisSecond < 10) thisSecond = '0' + thisSecond.toString();
            }

            // 判断是否在同一天
            if (nowYear === thisYear && nowMonth === thisMonth && nowDay === thisDay) {
                // 同一天，判断是否同在上下午
                if ((thisHour <= 12 && nowHour <= 12) || (thisHour > 12 && nowHour > 12)) {
                    // 同上下午
                    formatHMS();
                    return [thisHour, ':', thisMinute, ':', thisSecond].join('');
                } else {
                    if (thisHour <= 12) {
                        // 上午
                        formatHMS();
                        return ['上午 ', thisHour, ':', thisMinute, ':', thisSecond].join('');
                    } else {
                        formatHMS();
                        return ['下午 ', thisHour, ':', thisMinute, ':', thisSecond].join('');
                    }
                }
            } else {
                formatHMS();
                return [thisYear, '年', thisMonth, '月', thisDay, '日 ', thisHour, ':', thisMinute, ':', thisSecond].join('');
            }
            return date;
        };
        // 渲染消息时间
        this.renderMsgTime = function (chatCreateTime) {
            var isFirst = false; //是否第一条消息
            if (!this.lastChatCreateTime) {
                this.lastChatCreateTime = chatCreateTime;
                isFirst = true;
            }
            var thisDate = new Date(chatCreateTime.replace(/-/g, '/'));
            var lastDate = new Date(this.lastChatCreateTime.replace(/-/g, '/'));
            var thisTimestamp = thisDate.getTime();
            var lastTimestamp = lastDate.getTime();
            // 判断是否超过了3分钟
            var diffInMinute = parseInt((thisTimestamp - lastTimestamp) / (1000 * 60));
            if (diffInMinute >= 1 || isFirst) {
                var viewTmpl = '<div class="chat-time">$date</div>';
                var formatedTime = this.formatTime(thisDate);
                var view = $(viewTmpl.replace('$date', formatedTime));
                $("#chatList .mCSB_container").append(view);
            }
            this.lastChatCreateTime = chatCreateTime;
        };
        // 检测是否在表情集合中
        // 如果不存在，返回false
        // 存在，返回对应的className和类型数组 [className,type]
        this.checkInFace = function (element) {
            var faceJson = window.anychat.faceJson.FACE_JSON;
            var has = false;
            var className = null;
            var type = null;
            for (var j = 0; j < faceJson.length; j++) {
                var face = Object.keys(faceJson[j])[0];
                if (face === element) {
                    has = true;
                    className = faceJson[j][face];
                    type = faceJson[j]['type'];
                    break;
                }
            }
            if (has) {
                return [className, type];
            }
            return has;
        };
        // 替换表情为span
        this.replaceFace = function (chatContent) {
            var localContent = chatContent;
            var faceReg = /\[.*?\]/g;
            var regResult = localContent.match(faceReg);
            if (regResult != null && regResult.length > 0) {
                for (var index = 0; index < regResult.length; index++) {
                    var element = regResult[index];
                    var classType = this.checkInFace(element);
                    if (!classType) {
                        continue;
                    }
                    localContent = localContent.replace(element, this.renderFace(classType))
                }
            }
            return localContent;
        };
        // 构造表情dom
        this.renderFace = function (classType) {
            var className = classType[0];
            var type = classType[1];
            if (type === 'gif') {
                return '<span class="favor-span ' + className + '"></span>';
            }
            return '<span class="emoji-span ' + className + '"></span>';
        };
        //他人的聊天显示
        this.createOtherUserChat = function (chat) {
            var view = document.createElement("div");
            var userObj = this.userMap[chat.userId];
            if (!userObj) {
                return $('<div></div>');
            }
            //var chatContent = chat.chatContent;
            view.className = "otherAsk_P";
            var img;
            if (userObj.user.userImg === null || userObj.user.userImg === undefined || userObj.user.userImg === 'null') {
                img = BASE_PATH + "anychat/images/new-default.svg";
            } else {
                img = BASE_PATH + userObj.user.userImg;
            }
            var chatContent = this.replaceFace(chat.chatContent);
            view.innerHTML = '<a href="javascript:;" class="perPic_P"><img src="' + img + '" alt=""/></a>' +
                '<dl>' +
                '<dt>' + this.nvlName(userObj.user) + '</dt>' +
                '<dd><span>' + chatContent + '</span></dd>' +
                '</dl>' +
                '<div class="clear"></div>';
            return $(view);
        };
        //自己的聊天显示
        this.createOwnChat = function (chat) {
            var view = document.createElement("div");
            view.className = "myAsk_P";
            var img;
            if (this.own.userImg === null || this.own.userImg === undefined || this.own.userImg === 'null') {
                img = BASE_PATH + "anychat/images/me.svg";
            } else {
                img = BASE_PATH + this.own.userImg;
            }

            var chatContent = this.replaceFace(chat.chatContent);

            view.innerHTML = '<a href="javascript:;" class="perPic_P"><img src="' + img + '" alt=""/></a>' +
                '<dl>' +
                '<dd><span>' + chatContent + '</span></dd>' +
                '</dl>' +
                '<div class="clear"></div>';
            return $(view);
        };
        //用户发来消息时
        this.onUserMessage = function (body) {
            var showHaveMsg = true;
            //如果是当前聊天对象则直接显示
            if (this.nowToObj.user !== null && this.nowToObj.user !== undefined) {
                if (this.nowToObj.user.userId === body.userId) {
                    this.openChat(body.message);
                    showHaveMsg = false;
                }
            }
            //如果是不是当前聊天对象，增加新消息css
            if (showHaveMsg) {
                this.userMap[body.userId].view.addClass("online_P");
            }
            //加入历史聊天列表
            this.userMap[body.userId].chatList = this.userMap[body.userId].chatList.concat(body.message);
        };
        this.onToUserMessage = function (body) {
            //如果是当前聊天对象则显示
            if (this.nowToObj.user !== null && this.nowToObj.user !== undefined) {
                if (this.nowToObj.user.userId === body.toUserId) {
                    this.openChat([body.message]);
                }
            }
            //加入历史聊天列表
            this.userMap[body.toUserId].chatList = this.userMap[body.toUserId].chatList.concat([body.message]);
        };
        this.onGroupMessage = function (body) {
            // 通知top窗口有新消息来了
            // 判断是否在当前组里面
            if (this.groupMap[body.chatGroupId]) {
                if (window.top && window.top.pagesNav) {
                    window.top.pagesNav.chatNewMsg();
                }
            }
            //如果是当前聊天对象则显示
            var showHaveMsg = true;
            if (this.nowToObj.group !== null && this.nowToObj.group !== undefined) {
                if (this.nowToObj.group.chatGroupId === body.chatGroupId) {
                    this.openChat(body.message);
                    showHaveMsg = false;
                }
            }
            //如果是不是当前聊天对象，增加新消息css
            if (showHaveMsg) {
                this.groupMap[body.chatGroupId].view.addClass("online_P");
            }
            //加入历史聊天列表
            this.groupMap[body.chatGroupId].chatList = this.groupMap[body.chatGroupId].chatList.concat(body.message);
        };
        //发送聊天
        this.onSendChat = function (event) {
            // var chatContent = $("#sendChatContent").val();
            //
            this.copyTextarea2Clone();
            var chatContent = $('.textarea-clone>span').html();
            var originalContent = $("#sendChatContent").val().trim();
            if (originalContent === null || originalContent === "" || originalContent === undefined) {
                this.notice('发送消息不能为空，请重新输入');
                $("#sendChatContent").val('').css('height', '80px');
                $('.textarea-clone').html('').css('min-height', '80px');
                $('#sendChatContentWraper .mCSB_container').css('top', '0px');
                return;
            }
            if (originalContent.length > 1000) {
                this.notice('发送消息内容不能超过1000字符，请分条发送');
                return;
            }
            var toType;
            var toTypeId;
            if (this.nowToObj.user !== null && this.nowToObj.user !== undefined) {
                toType = 1;
                toTypeId = this.nowToObj.user.userId;
            } else {
                toType = 2;
                toTypeId = this.nowToObj.group.chatGroupId;
            }
            //$("#sendChatContent").val();
            loginChatProxy.sendMessage(chatContent, toType, toTypeId);
            $("#sendChatContent").val('').css('height', '80px');
            $('.textarea-clone').html('').css('min-height', '80px');
            $('#sendChatContentWraper .mCSB_container').css('top', '0px');
        };
        this.onClickHistory = function () {
            $(".windowR_P.left2_P").show().prev().hide();
            this.getHistoryChatList(this.maxPage);
            this.nowModule = 2;
        };
        this.setNowTime = function () {
            var date = new Date();
            var year = date.getFullYear();
            var month = date.getMonth() + 1;
            var monthStr;
            if (month < 10) {
                monthStr = "0" + month;
            } else {
                monthStr = month;
            }
            var dateStr;
            var date = date.getDate();
            if (date < 10) {
                dateStr = "0" + date;
            } else {
                dateStr = date;
            }
            $("#historyTime").text(year + "-" + monthStr + "-" + dateStr);
        };
        this.createHistoryChat = function (chat) {

            var view = document.createElement("div");
            view.className = "otherAsk_P history-item";
            var user;
            if (chat.userId === this.own.userId) {
                user = this.own;
            } else {
                user = this.userMap[chat.userId].user;
            }

            var img = BASE_PATH + "anychat/images/me.svg";
            var ownTextClass = '';
            if (chat.userId !== this.own.userId) {
                img = BASE_PATH + "anychat/images/new-default.svg";
            } else {
                ownTextClass = 'history-me';
            }
            var chatContent = this.replaceFace(chat.chatContent);
            var historyTime = this.formatTime(new Date(chat.chatCreateTime.replace(/-/g, '/')));
            view.innerHTML = '<a href="javascript:;" class="perPic_P"><img src="' + img + '" alt=""/></a>' +
                '<dl>' +
                '<dt class="' + ownTextClass + '">' + this.nvlName(user) + '<span class="history-time">' + historyTime + '</span>' + '</dt>' +
                '<dd><div>' + chatContent + '</div></dd>' +
                '</dl>' +
                '<div class="clear"></div>';
            return $(view);
        };
        this.getChatListSuccess = function (body) {
            this.currentPage = body.currentPage;
            this.totalPage = body.totalPage;
            $("#firstPage").removeClass("disabled_P");
            $("#prePage").removeClass("disabled_P");
            $("#nextPage").removeClass("disabled_P");
            $("#lastPage").removeClass("disabled_P");
            if (this.currentPage === this.totalPage || this.totalPage === null || this.totalPage === undefined) {
                $("#nextPage").addClass("disabled_P");
                $("#lastPage").addClass("disabled_P");
            }
            if (this.currentPage === 1) {
                $("#firstPage").addClass("disabled_P");
                $("#prePage").addClass("disabled_P");
            }
            $("#histortChat").text("");
            if (body.message !== null && body.message !== undefined && body.message.length !== 0) {
                for (var i = 0; i < body.message.length; i++) {
                    var msg = body.message[i];
                    var view = this.createHistoryChat(msg);
                    $("#histortChat").append(view);
                    if (i === 0) {
                        var chatCreateTime = msg.chatCreateTime;
                        var array = chatCreateTime.split(" ");
                        $("#historyTime").text(array[0]);
                    }

                }
            } else {
                this.setNowTime();
            }
        };
        this.onFirstPage = function () {
            this.getHistoryChatList(1);
        };
        this.onPrePage = function () {
            this.getHistoryChatList(this.currentPage - 1);
        };
        this.onNextPage = function () {
            this.getHistoryChatList(this.currentPage + 1);
        };
        this.onLastPage = function () {
            this.getHistoryChatList(this.maxPage);

        };
        this.getHistoryChatList = function (currentPage, chatCreateTime) {
            var toType;
            var toTypeId;
            if (this.nowToObj.user !== null && this.nowToObj.user !== undefined) {
                toType = 1;
                toTypeId = this.nowToObj.user.userId;
            } else {
                toType = 2;
                toTypeId = this.nowToObj.group.chatGroupId;
            }
            loginChatProxy.getMessage(toType, toTypeId, chatCreateTime, currentPage, this.pageSize);
        };
        Mediator.apply(this);
    };
    window.anychat.TalkMediator = TalkMediator;
})(window);

(function (window) {
    if (!window.anychat) window.anychat = {};
    var FaceJson = function () {
        /**
         * 表情对应class
         * @type {string}
         */
        this.FACE_JSON = [
            { "[大笑]": "emoji0" },
            { "[口罩]": "emoji1" },
            { "[激动]": "emoji2" },
            { "[吐舌头]": "emoji3" },
            { "[傻呆]": "emoji4" },
            { "[惊恐]": "emoji5" },
            { "[忧郁]": "emoji6" },
            { "[不屑]": "emoji7" },
            { "[嘿哈]": "emoji8" },
            { "[捂脸]": "emoji9" },
            { "[奸笑]": "emoji10" },
            { "[机智]": "emoji11" },
            { "[皱眉]": "emoji12" },
            { "[耶]": "emoji13" },
            { "[幽灵]": "emoji14" },
            { "[祈祷]": "emoji15" },
            { "[给力]": "emoji16" },
            { "[派对]": "emoji17" },
            { "[礼物]": "emoji18" },
            { "[红包]": "emoji19" },
            { "[小鸡]": "emoji20" },
            { "[可爱]": "emoji21" },
            { "[开心]": "emoji22" },
            { "[羞涩]": "emoji23" },
            { "[眉眼]": "emoji24" },
            { "[色眯眯]": "emoji25" },
            { "[飞吻]": "emoji26" },
            { "[亲亲]": "emoji27" },
            { "[嘻嘻]": "emoji28" },
            { "[害羞]": "emoji29" },
            { "[鬼脸]": "emoji30" },
            { "[坏笑]": "emoji31" },
            { "[汗]": "emoji32" },
            { "[囧]": "emoji33" },
            { "[抓狂]": "emoji34" },
            { "[伤心]": "emoji35" },
            { "[紧张]": "emoji36" },
            { "[吃惊]": "emoji37" },
            { "[困惑]": "emoji38" },
            { "[失望]": "emoji39" },
            { "[大哭]": "emoji40" },
            { "[刺瞎]": "emoji41" },
            { "[生气]": "emoji42" },
            { "[愤怒]": "emoji43" },
            { "[困]": "emoji44" },
            { "[恶魔]": "emoji45" },
            { "[外星人]": "emoji46" },
            { "[心动]": "emoji47" },
            { "[心碎]": "emoji48" },
            { "[一见钟情]": "emoji49" },
            { "[闪光]": "emoji50" },
            { "[星星]": "emoji51" },
            { "[感叹]": "emoji52" },
            { "[疑问]": "emoji53" },
            { "[睡觉觉]": "emoji54" },
            { "[口水]": "emoji55" },
            { "[音乐]": "emoji56" },
            { "[火]": "emoji57" },
            { "[大便]": "emoji58" },
            { "[厉害]": "emoji59" },
            { "[鄙视]": "emoji60" },
            { "[揍你]": "emoji61" },
            { "[胜利]": "emoji62" },
            { "[向上]": "emoji63" },
            { "[向下]": "emoji64" },
            { "[向右]": "emoji65" },
            { "[向左]": "emoji66" },
            { "[食指]": "emoji67" },
            { "[强]": "emoji68" },
            { "[OK]": "emoji69" },
            { "[偷笑]": "favor1" ,"type" :"gif" },
            { "[鼓掌]": "favor2" ,"type" :"gif" },
            { "[怒火]": "favor3" ,"type" :"gif" },
            { "[再见]": "favor4" ,"type" :"gif"},
            { "[白眼]": "favor5" ,"type" :"gif" },
            { "[MUA]": "favor6" ,"type" :"gif" },
            { "[色色]": "favor7" ,"type" :"gif" },
            { "[开怀大笑]": "favor8" ,"type" :"gif"},
            
        ];
    };
    window.anychat.faceJson = new FaceJson();
})(window);