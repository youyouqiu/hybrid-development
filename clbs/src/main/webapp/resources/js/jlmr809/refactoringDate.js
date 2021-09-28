// 重构日期控件(添加季度选择功能)
(function (window, $) {
    refactoringDate = {
        /**
         * 季度初始化
         * @param ohd 季度input dom对象非jquery对象
         * @param sgl 有值单个，无值默认范围
         */
        renderSeasonDate: function (ohd, sgl, max) {
            var ele = $(ohd);
            laydate.render({
                elem: ohd,
                type: 'month',
                format: 'yyyy年M季度',
                range: sgl ? null : '~',
                min: "1900-1-1",
                value: new Date().getFullYear() + '年1季度',
                max: max ? max : "2099-12-31",
                btns: ['clear', 'confirm'],
                ready: function (value, date, endDate) {
                    var hd = $("#layui-laydate" + ele.attr("lay-key"));
                    if (hd.length > 0) {
                        hd.click(function () {
                            ren($(this));
                        });
                    }
                    ren(hd);
                },
                done: function (value, date, endDate) {
                    if (!refactoringDate.isNull(date) && date.month > 0 && date.month < 5) {
                        ele.attr("startDate", date.year + "-" + date.month);
                    } else {
                        ele.attr("startDate", "");
                    }
                    if (!refactoringDate.isNull(endDate) && endDate.month > 0 && endDate.month < 5) {
                        ele.attr("endDate", endDate.year + "-" + endDate.month)
                    } else {
                        ele.attr("endDate", "");
                    }
                }
            });
            var ren = function (thiz) {
                var mls = thiz.find(".laydate-month-list");
                mls.each(function (i, e) {
                    $(this).find("li").each(function (inx, ele) {
                        var cx = ele.innerHTML;
                        if (inx < 4) {
                            ele.innerHTML = cx.replace(/月/g, "季度");
                        } else {
                            ele.style.display = "none";
                        }
                    });
                });
            }
        },
        isNull: function (s) {
            if (s == null || typeof(s) == "undefined" || s == "") return true;
            return false;
        },
    };
}(window, $))