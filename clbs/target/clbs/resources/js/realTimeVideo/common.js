define(function () {
    /**
     * 自定义map对象
     */
    var map = function () {
        this.data = new Object();

        this.set = function(key, value) {
            this.data[key] = value;
        }

        this.get = function(key) {
            return this.data[key];
        }

        this.has = function (key) {
            return !!this.data[key];
        }

        this.size = function () {
            return Object.keys(this.data).length;
        }

        this.keys = function () {
            return Object.keys(this.data);
        }

        this.remove = function (key) {
            delete this.data[key];
        }
    };

    /**
     * 获取地址栏参数
     */
    var getAddressUrl = function (name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) {
            return decodeURIComponent(r[2]);
        }
        return null;
    };

    return {
        map: map,
        getAddressUrl: getAddressUrl,
    };
});