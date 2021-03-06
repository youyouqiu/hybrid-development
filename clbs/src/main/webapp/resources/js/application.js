var app = function() {
    var e = function() {
        t(),
        o(),
        a(),
        l(),
        m(),
        n(),
        i(),
        j(),
        k()
    }
      , t = function() {
        //$("#toggle-left-button").tooltip();
    }
      , n = function() {
        $(".actions > .fa-chevron-down").click(function() {
            $(this).parent().parent().next().slideToggle("fast"),
            $(this).toggleClass("fa-chevron-down fa-chevron-up")
        })
    }
      , o = function() {
        $("#toggle-left").bind("click",'button', function(e) {
            $(".sidebarRight").hasClass(".sidebar-toggle-right") || ($(".sidebarRight").removeClass("sidebar-toggle-right"),
            $(".main-content-wrapper").removeClass("main-content-toggle-right")),
            $(".sidebar").toggleClass("sidebar-toggle"),
            $(".main-content-wrapper").toggleClass("main-content-toggle-left"),
            $(".imitateMenuBg").toggleClass("imitateMenuBg-left"),
            $(".defaultFootBg").toggleClass("defaultFootBg-left"),
            e.stopPropagation()
        })
    }
      , a = function() {
        $("#toggle-right").bind("click", function(e) {
            $(".sidebar").hasClass(".sidebar-toggle") || ($(".sidebar").addClass("sidebar-toggle"),
            $(".main-content-wrapper").addClass("main-content-toggle-left")),
            $(".sidebarRight").toggleClass("sidebar-toggle-right animated bounceInRight"),
            $(".main-content-wrapper").toggleClass("main-content-toggle-right"),
            $(window).width() < 660 && ($(".sidebar").removeClass("sidebar-toggle"),
            $(".main-content-wrapper").removeClass("main-content-toggle-left main-content-toggle-right")),
            e.stopPropagation()
        })
    }
      , i = function() {
        $(".actions > .fa-times").click(function() {
            $(this).parent().parent().parent().fadeOut()
        })
    }
      , j = function() {
    	  $('#simpleQueryParam').bind('keyup', function(event) {
    			if (event.keyCode == "13") {
    				//??????????????????
    				$('#search_button').click();
    			}
    		})
      }
      , k = function() {
    	  $('#search_condition').bind('keyup', function(event) {
    			if (event.keyCode == "13") {
    				//??????????????????
    				$('#treeSearch').click();
    			}
    		})
      }
     , l = function() {
        $("#leftside-navigation .sub-menu > a").click(function(e) {
            $("#leftside-navigation ul ul").slideUp();
            $(this).addClass('activeLine').find('i:eq(1)').toggleClass('fa-angle-right fa-angle-down');
            $(this).next().is(":visible") || $(this).next().slideDown()
                                            .parents('.sub-menu').siblings().children('a').removeClass('activeLine')
                                            .find('i:eq(1)').removeClass('fa-angle-down').addClass("fa-angle-right");
            e.stopPropagation();
        });
        $("ul.second-menu-ul li >a").click(function(e) {
        	$(this).parent().siblings().children('a').removeClass('open');
        	e.stopPropagation();
        });
        /*---???????????? links---*/
        var url = window.location;
        var element = $('ul.second-menu-ul li a').filter(function() {
            return this.href == url || url.href.indexOf(this.href) == 0;
        }).addClass('open');
        if ($('ul.second-menu-ul li a').is('a')) {
        	element.parents('li.sub-menu').addClass('active').siblings().removeClass('active');
        	//element.parents('li.sub-menu').children('a').addClass('activeLine');
        	element.parents('li.sub-menu').children('a').addClass('activeLine').find('i:eq(1)').toggleClass('fa-angle-right fa-angle-down'); /*??????i ????????????????????????*/
        	element.parents('ul.second-menu-ul').show();
        };
        /*---????????????  links---*/
        var urlSec = window.location;
        var elementSec = $('.second-menu-ul ul a').filter(function() {
            return this.href == urlSec || urlSec.href.indexOf(this.href) == 0;
        }).addClass('open');
        if ($('.second-menu ul li').is('li')) {
        	elementSec.parents('li.sub-menu').addClass('active').siblings().removeClass('active');
        	elementSec.parents('li.second-menu').children('a').find('i:eq(1)').toggleClass('fa-angle-right fa-angle-down'); /*??????i ????????????????????????*/
        	elementSec.parents('li.second-menu').siblings().children('ul').hide();
        };
        //????????????????????????????????????
        $("#leftside-navigation .second-menu > a.open").parent().siblings().children("ul").hide();
    }
   , m = function() {
        $("#leftside-navigation .second-menu > a").click(function(e) {
            $("#leftside-navigation ul ul ul").slideUp(),
            $(this).next().is(":visible") || $(this).next().slideDown(),
            e.stopPropagation();
        })
    }
      , s = function() {
        $(".timer").countTo()
    }
      , r = function() {
        $("#map").vectorMap({
            map: "world_mill_en",
            backgroundColor: "transparent",
            regionStyle: {
                initial: {
                    fill: "#1ABC9C"
                },
                hover: {
                    "fill-opacity": .8
                }
            },
            markerStyle: {
                initial: {
                    r: 10
                },
                hover: {
                    r: 12,
                    stroke: "rgba(255,255,255,0.8)",
                    "stroke-width": 3
                }
            },
            markers: [{
                latLng: [27.9881, 86.9253],
                name: "36 Employees",
                style: {
                    fill: "#E84C3D",
                    stroke: "rgba(255,255,255,0.7)",
                    "stroke-width": 3
                }
            }, {
                latLng: [48.8582, 2.2945],
                name: "58 Employees",
                style: {
                    fill: "#E84C3D",
                    stroke: "rgba(255,255,255,0.7)",
                    "stroke-width": 3
                }
            }, {
                latLng: [-40.6892, -74.0444],
                name: "109 Employees",
                style: {
                    fill: "#E84C3D",
                    stroke: "rgba(255,255,255,0.7)",
                    "stroke-width": 3
                }
            }, {
                latLng: [34.05, -118.25],
                name: "85 Employees ",
                style: {
                    fill: "#E84C3D",
                    stroke: "rgba(255,255,255,0.7)",
                    "stroke-width": 3
                }
            }]
        })
    }
      , c = function() {
        var e = new Skycons({
            color: "white"
        });
        e.set("clear-day", Skycons.CLEAR_DAY),
        e.set("clear-night", Skycons.CLEAR_NIGHT),
        e.set("partly-cloudy-day", Skycons.PARTLY_CLOUDY_DAY),
        e.set("partly-cloudy-night", Skycons.PARTLY_CLOUDY_NIGHT),
        e.set("cloudy", Skycons.CLOUDY),
        e.set("rain", Skycons.RAIN),
        e.set("sleet", Skycons.SLEET),
        e.set("snow", Skycons.SNOW),
        e.set("wind", Skycons.WIND),
        e.set("fog", Skycons.FOG),
        e.play()
    }
      , g = function() {
        Morris.Donut({
            element: "donut-example",
            data: [{
                label: "Chrome",
                value: 73
            }, {
                label: "Firefox",
                value: 71
            }, {
                label: "Safari",
                value: 69
            }, {
                label: "Internet Explorer",
                value: 40
            }, {
                label: "Opera",
                value: 20
            }, {
                label: "Android Browser",
                value: 10
            }],
            colors: ["#1abc9c", "#293949", "#e84c3d", "#3598db", "#2dcc70", "#f1c40f"]
        })
    }
      , d = function() {
        $(".slider-span").slider()
    }
    ;
    return {
        init: e,
        timer: s,
        map: r,
        sliders: d,
        weather: c,
        morrisPie: g
    }
}();
$(document).ready(function() {
    app.init()
});

 /*------------------
????????????------------*/
$(document).ready(function(){
  $(function(){
  	$("#clockAP").MyDigitClock({
        fontSize:15,
        fontColor:"grey",
        background:"#fff",
        fontWeight:"bold",
        bAmPm:true,
        timeFormat: ''}
    );
    $("#clock").MyDigitClock({
        fontSize:32,
        fontColor: "grey",
        fontWeight:"bold",
        bAmPm:true,
        background:'#fff',
        timeFormat: '{HH}:{MM}'
    });

      var clockAP = $("#clockAP span").html();
      var timeInfo = {'DayToday':DayToday(), 'DateToday': DateToday(), 'Months':Months(), 'clockAP':clockAP};
      localStorage.setItem("timeInfo",JSON.stringify(timeInfo));

    $("#monthsDay p").html(DayToday()).addClass('clockBoxCon').removeClass('day');
    $("#monthsDay h1").html(DateToday()).addClass('clockBoxCon');
    $("#monthsDay span").html(Months());
    $("#clock").addClass('clockBoxCon');
    $("#clockAP").addClass('clockBoxCon');
  });
  /**
   * ???bootstrap??????????????????modal????????????body??????modal-open?????????????????????body overflow ???hidden?????????????????????
   * ?????????modal???????????????modal-open?????????body??????????????????
   * ???????????????????????????modal???????????????????????????modal?????????????????????modal-open????????????????????????body??????????????????????????????????????????modal??????
   * ?????????????????????????????????????????????????????????modal???body?????????????????????
   * ???????????????????????????????????????????????????????????????????????????modal??????????????????modal-open???
   * TODO: ?????????iframe?????????????????????
   */
  $(document).on("hidden.bs.modal",".modal",
		function(){
			if($('.modal.in').length>0){
				$(document.body).addClass("modal-open")
			}
		}
	)

});


