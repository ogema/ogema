var tempIn, tempOut, humidityIn, humidityOut, message, priority;
var yellow = "rgba(255,237,0,0.5)";
var green = "rgba(146, 202, 68, .6)";
var red = "rgba(255,51,51,.6)";
var blue = "rgba(63,169,245,.5)";
var veil_canvas;

function room() {
    $(document).ready(function() {
        tempOut = "";
        humidityOut = "";
        tempIn = "";
        humidityIn = "";
        message = "Daten werden abgerufen ...";
        priority = 1;
        // set title
        var preTitleString = window.location.search.substring(1).split("=")[1];
        var titleString;
        if(preTitleString == "KITCHEN") titleString = "KÜCHE";
        if(preTitleString == "DINING") titleString = "ESSZIMMER";
        if(preTitleString == "LIVING") titleString = "WOHNZIMMER";
        if(preTitleString == "BEDROOM") titleString = "SCHLAFZIMMER";
        if(preTitleString == "BATH") titleString = "BADEZIMMER";
        $("title").html(titleString);
        $("#room").html(titleString);
  
        veil_canvas = document.getElementById("veil").getContext('2d');
        veil_canvas.globalAlpha = 0.2;
        veil_canvas.fillStyle = "grey";
        veil_canvas.fillRect(50, 35, 510, 420);
        // set functional buttons
        $("#links").html('<a href="index.html" id="back">Zimmerübersicht</a>'
                + '<a href="room2.html' + window.location.search + '"' + 'id="overview">Ansicht wechseln</a>');
        setInterval(pollData, 3000);
    });
}

function pollData() {
    $.getJSON("/climate_station_servlet/getData" + window.location.search, function(data) {
    	
    	// upper-right corner
        if (data.tempOut != null && data.tempOut != undefined && !isNaN(data.tempOut)) {
            tempOut = Math.round(data.tempOut);
            veil_canvas.clearRect(300, 35, 260, 180);
        } else { veil_canvas.clearRect(300, 35, 260, 180);
        		 veil_canvas.fillRect(300, 35, 260, 180);
               }
        // lower-right corner
        if (data.rhOut != null && data.rhOut != undefined && !isNaN(data.rhOut)) {
            humidityOut = data.rhOut;
            veil_canvas.clearRect(300, 215, 260, 240);
        } else { veil_canvas.clearRect(300, 215, 260, 240);
        	     veil_canvas.fillRect(300, 215, 260, 240);
        	   }
        // upper-left corner
        if (data.tempIn != null && data.tempIn != undefined && !isNaN(data.tempIn)) {
            tempIn = Math.round(data.tempIn);
            veil_canvas.clearRect(50, 35, 250, 180);
        } else { veil_canvas.clearRect(50, 35, 250, 180);
        	     veil_canvas.fillRect(50, 35, 250, 180);
        	   }
     // lower-left corner
        if (data.rhIn != null && data.rhIn != undefined && !isNaN(data.rhIn)) {
            humidityIn = data.rhIn;
            veil_canvas.clearRect(50, 215, 250, 240);
        } else { veil_canvas.clearRect(50, 215, 250, 240);
        		 veil_canvas.fillRect(50, 215, 250, 240);
        	   }
        
        if (data.message != null && data.message != undefined) {
            message = data.message;
        }
        if (data.prio != null && data.prio != undefined) {
            priority = data.prio;
        }
        draw();
    });
}

function getHumidityColor(rh) {
	if (rh >= 0 && rh <= 20) {
		return yellow;
	} else if (rh > 20 && rh < 70) {
		return green;
	} else if (rh >= 70 && rh <= 100) {
		return red;
	}
}

function getTemperatureColor(temp) {
	if (temp >= -10 && temp <= 16) {
		return blue;
	} else if (temp > 16 && temp <= 26) {
		return green;
	} else if (temp > 26 && temp <= 50) {
		return red;
	}
}

function setmessage() {
	$("#message").html(message);
	if (priority === 2) {
		$("#message").show().css({
			"color" : "grey",
			"border-color" : "#FFED00"
		}); // set message frame color to yellow
	} else if (priority === 1) {
		$("#message").show().css({
			"color" : "grey",
			"border-color" : "#92CA44"
		}); // set message frame color to green
	} else if (priority === 3) {
		$("#message").show().css({
			"color" : "#FF3333",
			"border-color" : "#FF3333"
		}); // set message frame color to red
	} else if (priority === 4) {
		$("#message").css({
			"color" : "#FF3333",
			"border-color" : "#FF3333"
		}).toggle(); // set message frame color to flashing red
	}
}

function draw() {

	setmessage();
	var c = document.getElementById("temperatureIn");
	var canvas = c.getContext('2d');
	var c2 = document.getElementById("temperatureOut");
	var canvas2 = c2.getContext('2d');
	var c3 = document.getElementById("humidityIn");
	var canvas3 = c3.getContext('2d');
	var c4 = document.getElementById("humidityOut");
	var canvas4 = c4.getContext('2d');
	canvas.clearRect(0, 0, 400, 300);
	canvas2.clearRect(0, 0, 400, 300);
	canvas3.clearRect(0, 0, 400, 300);
	canvas4.clearRect(0, 0, 400, 300);
	canvas.save();
	canvas2.save();
	canvas3.save();
	canvas4.save();
	canvas.scale(0.6, 0.6);
	canvas2.scale(0.6, 0.6);
	canvas3.scale(0.6, 0.6);
	canvas4.scale(0.6, 0.6);
	// canvas, Temperatur inside

	// blue part
	canvas.fillStyle = "rgba(63,169,245,0.6)";
	canvas.beginPath();
	canvas.moveTo(270, 130);
	canvas.arc(270, 130, 120, 1.5 * Math.PI + 2 * Math.PI * 0.001, 1.5
			* Math.PI + 2 * Math.PI * 26 / 60);
	canvas.closePath();
	canvas.fill();
	// green part
	canvas.fillStyle = "rgba(146, 202, 68, .6)";
	canvas.beginPath();
	canvas.moveTo(270, 130);
	canvas.arc(270, 130, 120, 1.5 * Math.PI + 2 * Math.PI * 26.01 / 60, 1.5
			* Math.PI + 2 * Math.PI * 36 / 60);
	canvas.closePath();
	canvas.fill();
	// red part
	canvas.fillStyle = "rgba(255,51,51,.6)";
	canvas.beginPath();
	canvas.moveTo(270, 130);
	canvas.arc(270, 130, 120, 1.5 * Math.PI + 2 * Math.PI * 36.01 / 60, 1.5
			* Math.PI + 2 * Math.PI);
	canvas.closePath();
	canvas.fill();
	// inner white part
	canvas.fillStyle = "white";
	canvas.beginPath();
	canvas.arc(270, 130, 115, 0, 2 * Math.PI, true);
	canvas.fill();
	// grey donut
	canvas.fillStyle = "lightgrey";
	canvas.beginPath();
	canvas.arc(270, 130, 80, 0, 2 * Math.PI, true);
	canvas.fill();
	canvas.fillStyle = "white";
	canvas.beginPath();
	canvas.arc(270, 130, 50, 0, 2 * Math.PI, true);
	canvas.fill();

	// pie chart indoor temperature
	canvas.fillStyle = getTemperatureColor(tempIn);
	canvas.beginPath();
	canvas.moveTo(270, 130);
	canvas.arc(270, 130, 100, 1.5 * Math.PI, 1.5 * Math.PI
			+ (2 * Math.PI * tempIn + 10) / 60 + (2 * Math.PI / 360));
	canvas.closePath();
	canvas.fill();
	// description indoor temperature
	canvas.fillStyle = "grey";
	canvas.font = "50px Arial";
	canvas.fillText(tempIn + "°C", 180, 200);
	canvas.fillStyle = "grey";
	canvas.font = "18px Arial";
	canvas.fillText("Temperatur innen", 190, 280);
	// Legend
	// blau
	canvas.fillStyle = "rgba(63,169,245,0.6)";
	canvas.beginPath();
	canvas.arc(140, 55, 10, 0, 2 * Math.PI);
	canvas.fill();
	// Text legend blue
	canvas.fillStyle = "grey";
	canvas.font = "16px Arial";
	canvas.fillText("zu niedrig", 53, 60);
	// green
	canvas.fillStyle = "rgba(146, 202, 68, .7)";
	canvas.beginPath();
	canvas.arc(115, 100, 10, 0, 2 * Math.PI);
	canvas.fill();
	// Text legend green
	canvas.fillStyle = "grey";
	canvas.font = "16px Arial";
	canvas.fillText("in Ordnung", 22, 105);
	// red
	canvas.fillStyle = "rgba(255,51,51,.6)";
	canvas.beginPath();
	canvas.arc(110, 145, 10, 0, 2 * Math.PI);
	canvas.fill();
	// Text legend red
	canvas.fillStyle = "grey";
	canvas.font = "16px Arial";
	canvas.fillText("zu hoch", 40, 150);
	canvas.restore();
	// canvas 2, Temperature outdoor
	// outer circle
	// blue part
	canvas2.fillStyle = "rgba(63,169,245,.6)";
	canvas2.beginPath();
	canvas2.moveTo(130, 130);
	canvas2.arc(130, 130, 120, 1.5 * Math.PI + 2 * Math.PI * 0.001, 1.5
			* Math.PI + 2 * Math.PI * 26 / 60);
	canvas2.closePath();
	canvas2.fill();
	// green part
	canvas2.fillStyle = "rgba(146, 202, 68, .6)";
	canvas2.beginPath();
	canvas2.moveTo(130, 130);
	canvas2.arc(130, 130, 120, 1.5 * Math.PI + 2 * Math.PI * 26.01 / 60, 1.5
			* Math.PI + 2 * Math.PI * 36 / 60);
	canvas2.closePath();
	canvas2.fill();
	// red part
	canvas2.fillStyle = "rgba(255,51,51,.6)";
	canvas2.beginPath();
	canvas2.moveTo(130, 130);
	canvas2.arc(130, 130, 120, 1.5 * Math.PI + 2 * Math.PI * 36.01 / 60, 1.5
			* Math.PI + 2 * Math.PI);
	canvas2.closePath();
	canvas2.fill();
	// inner white part
	canvas2.fillStyle = "white";
	canvas2.beginPath();
	canvas2.arc(130, 130, 115, 0, 2 * Math.PI, true);
	canvas2.fill();
	// grey donut
	canvas2.fillStyle = "lightgrey";
	canvas2.beginPath();
	canvas2.arc(130, 130, 80, 0, 2 * Math.PI, true);
	canvas2.fill();
	canvas2.fillStyle = "white";
	canvas2.beginPath();
	canvas2.arc(130, 130, 50, 0, 2 * Math.PI, true);
	canvas2.fill();
	// pie chart outdoor Temperature
	canvas2.fillStyle = getTemperatureColor(tempOut);
	canvas2.beginPath();
	canvas2.moveTo(130, 130);
	canvas2.arc(130, 130, 100, 1.5 * Math.PI, 1.5 * Math.PI
			+ (2 * Math.PI * tempOut + 10) / 60 + (2 * Math.PI / 360));
	canvas2.closePath();
	canvas2.fill();
	// Description outdoor Temperature
	canvas2.fillStyle = "grey";
	canvas2.font = "50px Arial";
	canvas2.fillText(tempOut + "°C", 40, 200);
	canvas2.fillStyle = "grey";
	canvas2.font = "18px Arial";
	canvas2.fillText("Temperatur außen", 50, 280);
	// Legend
	// blue
	canvas2.fillStyle = "rgba(63,169,245,.6)";
	canvas2.beginPath();
	canvas2.arc(260, 55, 10, 0, 2 * Math.PI);
	canvas2.fill();
	// Text legend blue
	canvas2.fillStyle = "grey";
	canvas2.font = "16px Arial";
	canvas2.fillText("zu niedrig", 280, 60);
	// green
	canvas2.fillStyle = "rgba(146, 202, 68, .6)";
	canvas2.beginPath();
	canvas2.arc(285, 100, 10, 0, 2 * Math.PI);
	canvas2.fill();
	// Text legend green
	canvas2.fillStyle = "grey";
	canvas2.font = "16px Arial";
	canvas2.fillText("in Ordnung", 305, 105);
	// red
	canvas2.fillStyle = "rgba(255,51,51,.6)";
	canvas2.beginPath();
	canvas2.arc(290, 145, 10, 0, 2 * Math.PI);
	canvas2.fill();
	// Text legend red
	canvas2.fillStyle = "grey";
	canvas2.font = "16px Arial";
	canvas2.fillText("zu hoch", 310, 150);
	canvas2.restore();
	// canvas 3, indoor humidity
	// outer circle
	// yellow part
	canvas3.fillStyle = "rgba(255,237,0,0.6)";
	canvas3.beginPath();
	canvas3.moveTo(270, 130);
	canvas3.arc(270, 130, 120, 1.5 * Math.PI + 2 * Math.PI * 0.001, 1.5
			* Math.PI + 2 * Math.PI * 0.2);
	canvas3.closePath();
	canvas3.fill();
	// green part
	canvas3.fillStyle = "rgba(146, 202, 68, .6)";
	canvas3.beginPath();
	canvas3.moveTo(270, 130);
	canvas3.arc(270, 130, 120, 1.5 * Math.PI + 2 * Math.PI * 0.201, 1.5
			* Math.PI + 2 * Math.PI * 0.699);
	canvas3.closePath();
	canvas3.fill();
	// red part
	canvas3.fillStyle = "rgba(255,51,51,.6)";
	canvas3.beginPath();
	canvas3.moveTo(270, 130);
	canvas3.arc(270, 130, 120, 1.5 * Math.PI + 2 * Math.PI * 0.7, 1.5 * Math.PI
			+ 2 * Math.PI);
	canvas3.closePath();
	canvas3.fill();
	// inner white part
	canvas3.fillStyle = "white";
	canvas3.beginPath();
	canvas3.arc(270, 130, 115, 0, 2 * Math.PI, true);
	canvas3.fill();
	// grey donut
	canvas3.fillStyle = "lightgrey";
	canvas3.beginPath();
	canvas3.arc(270, 130, 80, 0, 2 * Math.PI, true);
	canvas3.fill();
	canvas3.fillStyle = "white";
	canvas3.beginPath();
	canvas3.arc(270, 130, 50, 0, 2 * Math.PI, true);
	canvas3.fill();
	// pie chart, indoor humidity
	canvas3.fillStyle = getHumidityColor(humidityIn);
	canvas3.beginPath();
	canvas3.moveTo(270, 130);
	canvas3.arc(270, 130, 100, 1.5 * Math.PI, 1.5 * Math.PI + 2 * Math.PI
			* humidityIn / 100);
	canvas3.closePath();
	canvas3.fill();
	// Description
	canvas3.fillStyle = "grey";
	canvas3.font = "50px Arial";
	canvas3.fillText(humidityIn + "%", 180, 200);
	canvas3.fillStyle = "grey";
	canvas3.font = "18px Arial";
	canvas3.fillText("Feuchtigkeit innen", 190, 280);
	// Legend
	// yellow
	canvas3.fillStyle = "rgba(255,237,0,0.6)";
	canvas3.beginPath();
	canvas3.arc(140, 55, 10, 0, 2 * Math.PI);
	canvas3.fill();
	// Text legend yellow
	canvas3.fillStyle = "grey";
	canvas3.font = "16px Arial";
	canvas3.fillText("zu niedrig", 53, 60);
	// green
	canvas3.fillStyle = "rgba(146, 202, 68, .6)";
	canvas3.beginPath();
	canvas3.arc(115, 100, 10, 0, 2 * Math.PI);
	canvas3.fill();
	// Text legend green
	canvas3.fillStyle = "grey";
	canvas3.font = "16px Arial";
	canvas3.fillText("in Ordnung", 22, 105);
	// red
	canvas3.fillStyle = "rgba(255,51,51,.6)";
	canvas3.beginPath();
	canvas3.arc(110, 145, 10, 0, 2 * Math.PI);
	canvas3.fill();
	// Text legend red
	canvas3.fillStyle = "grey";
	canvas3.font = "16px Arial";
	canvas3.fillText("zu hoch", 40, 150);
	canvas3.restore();
	// canvas 4, outdoor humidity
	// outer circle
	// yellow part
	canvas4.fillStyle = "rgba(255,237,0,0.6)";
	canvas4.beginPath();
	canvas4.moveTo(130, 130);
	canvas4.arc(130, 130, 120, 1.5 * Math.PI + 2 * Math.PI * 0.001, 1.5
			* Math.PI + 2 * Math.PI * 0.2);
	canvas4.closePath();
	canvas4.fill();
	// green part
	canvas4.fillStyle = "rgba(146, 202, 68, .6)";
	canvas4.beginPath();
	canvas4.moveTo(130, 130);
	canvas4.arc(130, 130, 120, 1.5 * Math.PI + 2 * Math.PI * 0.201, 1.5
			* Math.PI + 2 * Math.PI * 0.699);
	canvas4.closePath();
	canvas4.fill();
	// red part
	canvas4.fillStyle = "rgba(255,51,51,.6)";
	canvas4.beginPath();
	canvas4.moveTo(130, 130);
	canvas4.arc(130, 130, 120, 1.5 * Math.PI + 2 * Math.PI * 0.7, 1.5 * Math.PI
			+ 2 * Math.PI);
	canvas4.closePath();
	canvas4.fill();
	// inner white part
	canvas4.fillStyle = "white";
	canvas4.beginPath();
	canvas4.arc(130, 130, 115, 0, 2 * Math.PI, true);
	canvas4.fill();
	// grey donut
	canvas4.fillStyle = "lightgrey";
	canvas4.beginPath();
	canvas4.arc(130, 130, 80, 0, 2 * Math.PI, true);
	canvas4.fill();
	canvas4.fillStyle = "white";
	canvas4.beginPath();
	canvas4.arc(130, 130, 50, 0, 2 * Math.PI, true);
	canvas4.fill();
	// pie chart outdoor humidity
	canvas4.fillStyle = getHumidityColor(humidityOut);
	canvas4.beginPath();
	canvas4.moveTo(130, 130);
	canvas4.arc(130, 130, 100, 1.5 * Math.PI, 1.5 * Math.PI + 2 * Math.PI
			* humidityOut / 100);
	canvas4.closePath();
	canvas4.fill();
	// description
	canvas4.fillStyle = "grey";
	canvas4.font = "50px Arial";
	canvas4.fillText(humidityOut + "%", 40, 200);
	canvas4.fillStyle = "grey";
	canvas4.font = "18px Arial";
	canvas4.fillText("Feuchtigkeit außen", 50, 280);
	// legend
	// yellow
	canvas4.fillStyle = "rgba(255,237,0,0.6)";
	canvas4.beginPath();
	canvas4.arc(260, 55, 10, 0, 2 * Math.PI);
	canvas4.fill();
	// Text legend yellow
	canvas4.fillStyle = "grey";
	canvas4.font = "16px Arial";
	canvas4.fillText("zu niedrig", 280, 60);
	// green
	canvas4.fillStyle = "rgba(146, 202, 68, .6)";
	canvas4.beginPath();
	canvas4.arc(285, 100, 10, 0, 2 * Math.PI);
	canvas4.fill();
	// Text legend green
	canvas4.fillStyle = "grey";
	canvas4.font = "16px Arial";
	canvas4.fillText("in Ordnung", 305, 105);
	// red
	canvas4.fillStyle = "rgba(255,51,51,.6)";
	canvas4.beginPath();
	canvas4.arc(290, 145, 10, 0, 2 * Math.PI);
	canvas4.fill();
	// Text legend red
	canvas4.fillStyle = "grey";
	canvas4.font = "16px Arial";
	canvas4.fillText("zu hoch", 310, 150);
	canvas4.restore();
}