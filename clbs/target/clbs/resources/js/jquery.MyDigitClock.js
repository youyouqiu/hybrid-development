/**
 * 时钟MyDigitClock
 *
 * 2016-07-14  by LEFFY
 *
 *
 */

(function($) {

    var _options = {};
	var _container = {};

	jQuery.fn.MyDigitClock = function(options) {
		var id = $(this).get(0).id;
		_options[id] = $.extend({}, $.fn.MyDigitClock.defaults, options);

		return this.each(function()
		{
			_container[id] = $(this);
			showClock(id);
		});

		function showClock(id)
		{
			var d = new Date;
			var h = d.getHours();
			var m = d.getMinutes();
			var s = d.getSeconds();
			var ampm = "";
			if (_options[id].bAmPm)
			{
				if (h>12)
				{

					ampm = " 下午";
				}
				else
				{
					ampm = " 上午";
				}
			}

			var templateStr = _options[id].timeFormat + "<span class='PTclock'>"+ ampm +"</span>";

			templateStr = templateStr.replace("{HH}", getDD(h));
			templateStr = templateStr.replace("{MM}", getDD(m));
			templateStr = templateStr.replace("{SS}", getDD(s));

			var obj = $("#"+id);
			obj.css("fontSize", _options[id].fontSize);
			obj.css("fontFamily", _options[id].fontFamily);
			obj.css("color", _options[id].fontColor);
			obj.css("background", _options[id].background);
			obj.css("fontWeight", _options[id].fontWeight);

			//change reading
			obj.html(templateStr)

			//toggle hands
			if (_options[id].bShowHeartBeat)
			{
				obj.find("#ch1").fadeTo(800, 0.1);
				obj.find("#ch2").fadeTo(800, 0.1);
			}

			setTimeout(function(){showClock(id)}, 1000);
		}

		function getDD(num)
		{
			return (num>=10)?num:"0"+num;
		}

		function refreshClock()
		{
			setupClock();
		}
	}
})(jQuery);
function Months(){
    var now = new Date();
    var mm = now.getMonth()+1;
    var cl = '<font color="#ffffff">';
    if (now.getDay() == 0) cl = '<font color="#ffffff">';
    if (now.getDay() == 6) cl = '<font color="#ffffff">';
    return(cl +  mm + '月</font>');
}
function DateToday(){
    var now = new Date();
    var cl = '<font color="#ffffff">';
    if (now.getDay() == 0) cl = '<font color="#ffffff">';
    if (now.getDay() == 6) cl = '<font color="#ffffff">';
    return(cl +  now.getDate());
}
function DayToday(){
    var day = new Array();
    day[0] = "星期日";
    day[1] = "星期一";
    day[2] = "星期二";
    day[3] = "星期三";
    day[4] = "星期四";
    day[5] = "星期五";
    day[6] = "星期六";
    var now = new Date();
    var cl = '<font color="#ffffff">';
    if (now.getDay() == 0) cl = '<font color="#ffffff">';
    if (now.getDay() == 6) cl = '<font color="#ffffff">';
    return(cl +  day[now.getDay()] + '</font>');
  }
