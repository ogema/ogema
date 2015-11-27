var tempIn, tempOut, humidityIn, humidityOut, message, priority, motionIn;
var yellow = "rgba(255,237,0,0.5)";
var green = "rgba(146, 202, 68, .6)";
var red = "rgba(255,51,51,.6)";
var blue = "rgba(63,169,245,.5)";
var titleString;

var inOperation=false;

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
		titleString;
		if (preTitleString == "KITCHEN")
			titleString = "KÜCHE";
		if (preTitleString == "DINING")
			titleString = "ESSZIMMER";
		if (preTitleString == "LIVING")
			titleString = "WOHNZIMMER";
		if (preTitleString == "BEDROOM")
			titleString = "SCHLAFZIMMER";
		if (preTitleString == "BATH")
			titleString = "BADEZIMMER";
		$("title").html(titleString);
		$("#room").html(titleString);

		var imageObj = new Image();
		imageObj.src = 'x.png';

		var c1 = document.getElementById("temperatureIn");
		var canvas1 = c1.getContext('2d');
		temp_pie(canvas1, "Innentemperatur", '');
		canvas1.drawImage(imageObj, 92, 62);

		var c2 = document.getElementById("temperatureOut");
		var canvas2 = c2.getContext('2d');
		temp_pie(canvas2, "Aussentemperatur", '');
		canvas2.drawImage(imageObj, 92, 62);

		var c3 = document.getElementById("humidityIn");
		var canvas3 = c3.getContext('2d');
		humidity_pie(canvas3, "Relative Feuchte Innen", '');
		canvas3.drawImage(imageObj, 92, 62);

		var c4 = document.getElementById("humidityOut");
		var canvas4 = c4.getContext('2d');
		humidity_pie(canvas4, "Relative Feuchte Außen", '');
		canvas4.drawImage(imageObj, 92, 62);

		var imgName = 'lightx.png';
		document.getElementById('light1').innerHTML = '<img src="' + imgName + '" />';
		imgName = 'swboxx.png';
		document.getElementById('light2').innerHTML = '<img src="' + imgName + '" />';
		imgName = 'manx.png';
		document.getElementById('move').innerHTML = '<img src="' + imgName + '" />';

		imgName = 'waterx.png';
		document.getElementById('water').innerHTML = '<img src="' + imgName + '" />';

		imgName = 'airx.png';
		document.getElementById('air').innerHTML = '<img src="' + imgName + '" />';

		imgName = 'smokex.png';
		document.getElementById('smoke').innerHTML = '<img src="' + imgName + '" />';

		document.getElementById('simpleView').href = 'room2.html' + window.location.search;

		setInterval(pollData, 1500);
	});
}

function pollData() {
	if(!inOperation){
	inOperation=true;
	$.getJSON("/rcsservice/getData" + window.location.search, function(data) {

		// upper-right corner
		if (data.tempOut != null && data.tempOut != undefined && !isNaN(data.tempOut)) {
			tempOut = Math.round(data.tempOut);
		} else
			tempOut = data.tempOut;
		// lower-right corner
		if (data.rhOut != null && data.rhOut != undefined && !isNaN(data.rhOut)) {
			humidityOut = Math.round(data.rhOut*100);
		} else
			humidityOut = data.rhOut;
		// upper-left corner
		if (data.tempIn != null && data.tempIn != undefined && !isNaN(data.tempIn)) {
			tempIn = Math.round(data.tempIn);
		} else
			tempIn = data.tempIn;
		// lower-left corner
		if (data.rhIn != null && data.rhIn != undefined && !isNaN(data.rhIn)) {
			humidityIn = Math.round(data.rhIn*100);
		} else
			humidityIn = data.rhIn;

		if (data.motionIn != null && data.motionIn != undefined) {
			motionIn = data.motionIn;
		} else
			motionIn = 'X';

		if (data.message != null && data.message != undefined) {
			message = data.message;
		}
		if (data.prio != null && data.prio != undefined) {
			priority = data.prio;
		}
		if (data.light1 != null && data.light1 != undefined) {
			light1 = data.light1;
		} else
			light1 = 'X';

		if (data.light2 != null && data.light2 != undefined) {
			light2 = data.light2;
		} else
			light2 = 'X';

		if (data.air != null && data.air != undefined) {
			air = data.air;
		} else
			air = 'X';

		if (data.water != null && data.water != undefined) {
			water = data.water;
		} else
			water = 'X';

		if (data.smoke != null && data.smoke != undefined) {
			smoke = data.smoke;
		} else
			smoke = 'X';
		draw();
		inOperation=false;
	});}
}

function setmessage(msgbox, msg, prio) {
	msgbox.html(msg);
	if (prio === 2) {
		msgbox.show().css({
			"color" : "grey",
			"border-color" : "#FFED00"
		}); // set message frame color to yellow
	} else if (prio === 1) {
		msgbox.show().css({
			"color" : "grey",
			"border-color" : "#92CA44"
		}); // set message frame color to green
	} else if (prio === 3) {
		msgbox.show().css({
			"color" : "#FF3333",
			"border-color" : "#FF3333"
		}); // set message frame color to red
	} else if (prio === 4) {
		msgbox.css({
			"color" : "#FF3333",
			"border-color" : "#FF3333"
		}).toggle(); // set message frame color to flashing red
	}
}

function draw() {
	var msgbox = $("#message1");
	setmessage(msgbox, message, priority);

	if (tempIn != undefined) {
		var c1 = document.getElementById("temperatureIn");
		var canvas1 = c1.getContext('2d');
		if (!isNaN(tempIn)) {
			temp_pie(canvas1, "Temperatur", tempIn);
		} else {
			temp_pie(canvas1, "Temperatur", '');
		}
	}

	if (tempOut != undefined) {
		var c2 = document.getElementById("temperatureOut");
		var canvas2 = c2.getContext('2d');
		if (!isNaN(tempOut)) {
			temp_pie(canvas2, "Temperatur", tempOut);
		} else {
			temp_pie(canvas2, "Temperatur", '');
		}
	}

	if (humidityIn != undefined) {
		var c3 = document.getElementById("humidityIn");
		var canvas3 = c3.getContext('2d');
		if (!isNaN(humidityIn)) {
			humidity_pie(canvas3, "Relative Luftfeuchte", humidityIn);
		} else {
			humidity_pie(canvas3, "Relative Luftfeuchte", '');
		}
	}

	if (humidityOut != undefined) {
		var c4 = document.getElementById("humidityOut");
		var canvas4 = c4.getContext('2d');
		if (!isNaN(humidityOut)) {
			humidity_pie(canvas4, "Relative Luftfeuchte", humidityOut);
		} else {
			humidity_pie(canvas4, "Relative Luftfeuchte", '');
		}
	}

	if (motionIn != 'X') {
		// motion sensor
		if (motionIn == true)
			imgName = 'manp.png';
		else
			imgName = 'mann.png';
		document.getElementById('move').innerHTML = '<img src="' + imgName + '" />';
	}

	if (light1 != 'X') {
		// light1 sensor
		if (light1 == true)
			imgName = 'lightp.png';
		else
			imgName = 'lightn.png';
		document.getElementById('light1').innerHTML = '<img src="' + imgName + '" />';
	}

	if (light2 != 'X') {
		// light2 sensor
		if (light2 == true)
			imgName = 'swboxp.png';
		else
			imgName = 'swboxn.png';
		document.getElementById('light2').innerHTML = '<img src="' + imgName + '" />';
	}

	if (air != 'X') {
		// air sensor
		if (air == true) {
			imgName = 'airp.png';
		} else
			imgName = 'airn.png';
		document.getElementById('air').innerHTML = '<img src="' + imgName + '" />';
	}

	if (water != 'X') {
		// water sensor
		if (water == true) {
			imgName = 'waterp.png';
			setmessage($("#message3"), 'Im Raum ' + titleString + ' wurde Wasseraustritt festgestellt!', 4);
		} else
			imgName = 'watern.png';
		document.getElementById('water').innerHTML = '<img src="' + imgName + '" />';
	}

	if (smoke != 'X') {
		// water sensor
		if (smoke == true) {
			imgName = 'smokep.png';
			setmessage($("#message4"), 'Im Raum ' + titleString + ' wurde Rauchentwicklng festgestellt!', 4);
		} else
			imgName = 'smoken.png';
		document.getElementById('smoke').innerHTML = '<img src="' + imgName + '" />';
	}
}