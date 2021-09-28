// 封装map集合
function MapObject(){
    this.elements = {};
    //获取MAP元素个数
    this.size = function () {
        return Object.keys(this.elements).length;
    };
    //判断MAP是否为空
    this.isEmpty = function () {
        return (Object.keys(this.elements).length < 1);
    };
    //删除MAP所有元素
    this.clear = function () {
        this.elements = {};
    };
    //向MAP中增加元素（key, value)
    this.put = function (_key, _value) {
        this.elements[_key] = _value;
    };
    //删除指定KEY的元素，成功返回True，失败返回False
    this.remove = function (_key) {
        delete this.elements[_key];
    };
    //获取指定KEY的元素值VALUE，失败返回NULL
    this.get = function (_key) {
        return this.elements[_key];
    };
    //获取指定索引的元素（使用element.key，element.value获取KEY和VALUE），失败返回NULL
    this.element = function (_index) {
        var keys = Object.keys(this.elements);
        var key = keys[_index];
        return this.elements[key];
    };
    //判断MAP中是否含有指定KEY的元素
    this.containsKey = function (_key) {
        if (this.elements[_key]) {
            return true;
        } else {
            return false;
        }
    };
    //判断MAP中是否含有指定VALUE的元素
    this.containsValue = function (_value) {
        var bln = false;
        try {
            for (var i = 0, len = this.elements.length; i < len; i++) {
                if (this.elements[i].value == _value) {
                    bln = true;
                }
            }
        } catch (e) {
            bln = false;
        }
        return bln;
    };
    //获取MAP中所有VALUE的数组（ARRAY）
    this.values = function () {
        var arr = new Array();
        var keys = Object.keys(this.elements);
        for (var i = 0, len = keys.length; i < len; i++) {
            arr.push(this.elements[keys[i]]);
        }
        return arr;
    };
    //获取MAP中所有KEY的数组（ARRAY）
    this.keys = function () {
        return Object.keys(this.elements);
    };
}

$.fn.extend({
    animateCss: function (animationName) {
        var animationEnd = 'webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend';
        $(this).addClass(animationName).one(animationEnd, function() {
            $(this).removeClass(animationName);
        });
    }
});
