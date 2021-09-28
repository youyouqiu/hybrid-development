/**
 * 
 */
(function(window,$){
	 var selectVehicleType;
	mobileSourceEdit = {
		
	onlyNumber:function(input, n)
	{
		 input.value = input.value.replace(/[^0-9\.]/ig, '');
		    var dotIdx = input.value.indexOf('.'), dotLeft, dotRight;
		    if (dotIdx >= 0) {
		        dotLeft = input.value.substring(0, dotIdx);
		        dotRight = input.value.substring(dotIdx + 1);
		        if (dotRight.length > n) {
		            dotRight = dotRight.substring(0, n);
		        }
		        input.value = dotLeft + '.' + dotRight;
		    }
	},
    getdate:function(data) {
     if(data){
        var now = new Date(data),
            y = now.getFullYear(),
            m = now.getMonth() + 1,
            d = now.getDate();
        return y + "-" + (m < 10 ? "0" + m : m) + "-" + (d < 10 ? "0" + d : d) /*+ " " + now.toTimeString().substr(0, 8)*/;
    }
    },
    getDate:function(shijianchuo) {  
    	  //shijianchuo是整数，否则要parseInt转换  
    	  var time = new Date(shijianchuo);  
    	  var y = time.getFullYear();  
    	  var m = time.getMonth()+1;  
    	  var d = time.getDate();  
    	  var h = time.getHours();  
    	  var mm = time.getMinutes();  
    	  var s = time.getSeconds();  
    	  return y+'-'+add0(m)+'-'+add0(d)+' '+add0(h)+':'+add0(mm)+':'+add0(s); 
    	  function add0(m){return m<10?'0'+m:m };  
    	},
    	   //将时间戳变成小时分秒
        changdatainfo:function(data){
        	var day = parseInt(data / (24*60*60));//计算整数天数
        	var afterDay = data - day*24*60*60;//取得算出天数后剩余的秒数
        	var hour = parseInt(afterDay/(60*60));//计算整数小时数
        	var afterHour = data - day*24*60*60 - hour*60*60;//取得算出小时数后剩余的秒数
        	var min = parseInt(afterHour/60);//计算整数分
        	var afterMin = data - day*24*60*60 - hour*60*60 - min*60;//取得算出分后剩余的秒数
        	   if(day!=0&&hour!=0&&min!=0){
        	   var time=day+"天"+hour+"小时"+min+"分"+afterMin+"秒";
        	 return time;
        	   }else{
        		   	var time=day+"天"+hour+"小时"+min+"分"+afterMin+"秒";
        		   if(day==0){
        			   var time=time.replace(/0天/,"");
        		   }
        		  /* if(hour==0){
        		   var time=time.replace(/0小时/,"");
        		   }
        		   if(min==0){
        			   var time=time.replace(/0分/,"");
        		   }*/
        		    return time;
        	   }
        	        	        	       	
         },
    
	}
	$(function(){
		$('input').inputClear();
		var datevalue=$("#productdata").val();
		var datevalue=mobileSourceEdit.getdate(datevalue);
		$("#productdata").val(datevalue);	 	
		var idlingtime=$("#Idlelength").val();
	    if(idlingtime){
	    var  idlingtimevalue=mobileSourceEdit.changdatainfo(idlingtime/1000);
         }

	    $("#Idlelength").val(idlingtimevalue);
	})
})(window,$)