var zustand, tInnen, fInnen;
function room2() {
	$(document).ready(function() {
		zustand = 0;
		tInnen = "";
		fInnen = "";

		// set title
		var preTitleString = window.location.search.substring(1).split("=")[1];
		var titleString;
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

		// set functional buttons
		document.getElementById('fullView').href = 'room.html' + window.location.search;

		setInterval(function() {
			var url = '/rcsservice/getData' + window.location.search;
			if (url.indexOf('?') > 0)
				url += '&';
			else 
				url += '?'
			url += "user=" + otusr + "&pw=" + otpwd;
			$.getJSON(url, function(data) {

				if (data.messageID != null) {
					zustand = data.messageID;
				} else {
					zustand = 0;
				}
				tInnen = data.tempIn;
				fInnen = data.rhIn;
				if (tInnen != undefined) {
					tInnen = Math.round(tInnen*10)/10;
					fInnen = Math.round(fInnen*100);
				}
				draw();
			});
		}, 3000);
	});
}

function draw() {
	var angle, angle2;
	var colors = [];
	var c = document.getElementById("qualitativeView");
	var canvas = c.getContext('2d');
	canvas.clearRect(0, 0, 680, 440);
	// Gray gradient, outer circle
	var radGradientGrau = canvas.createRadialGradient(340, 180, 0, 340, 180, 180);
	var stColHellGrau = "rgba(216, 216, 216, .2)"; // white
	var stColDunkel = "rgba(216, 216, 216, 1)"; // dark grey
	// Set gradient limits
	radGradientGrau.addColorStop(0.91, stColDunkel);
	radGradientGrau.addColorStop(0.96, stColHellGrau);
	radGradientGrau.addColorStop(1, stColDunkel);
	// Green gradient for the outer circle
	var radGradientGruen = canvas.createRadialGradient(340, 180, 0, 340, 180, 180);
	var stColGruen = "rgba(146, 202, 68, 1)"; // grün
	var stColHellGruen = "rgba(146, 202, 68, .2)"; // grün
	// Set gradient limits
	radGradientGruen.addColorStop(0.91, stColGruen);
	radGradientGruen.addColorStop(0.96, stColHellGruen);
	radGradientGruen.addColorStop(1, stColGruen);
	// Red gradient for the outer circle
	var radGradientRot = canvas.createRadialGradient(340, 180, 0, 340, 180, 180);
	var stColRot = "rgba(255,51,51,1)"; // red
	var stColHellRot = "rgba(255,51,51,.2)"; // red
	// Set gradient limits
	radGradientRot.addColorStop(0.91, stColRot);
	radGradientRot.addColorStop(0.96, stColHellRot);
	radGradientRot.addColorStop(1, stColRot);
	// calculate color of the angle
	switch (zustand) {
	case 0:
		// no status message available, 3 grey segments, in the middel, right
		// and left
		angle = 1.94 * Math.PI + (22 - 19) * 0.024 * Math.PI + .5 * Math.PI; // rechte
		// zeiger
		// in
		// der
		// Mitte,
		// linker
		// Zeiger
		// in
		// der
		// mitte
		angle2 = 1.06 * Math.PI - (48 - 26) * 0.12 * Math.PI / 40 + .5 * Math.PI;
		colors[0] = radGradientGrau;
		colors[1] = radGradientGrau;
		colors[2] = radGradientGrau;
		colors[3] = radGradientGrau;
		colors[4] = radGradientGrau;
		colors[5] = radGradientGrau;
		colors[6] = radGradientGrau;
		colors[7] = radGradientGrau;
		colors[8] = radGradientGrau;
		colors[9] = radGradientGrau;
		break;
	case 8:
	case 35:
		// Turn heating on, red segment on up right
		angle = 1.72 * Math.PI + (8 * 0.00875 * Math.PI) + .5 * Math.PI; // tInnen=8°,
		// fInnen
		// =
		// 48%
		angle2 = 1.06 * Math.PI - (48 - 26) * 0.12 * Math.PI / 40 + .5 * Math.PI;
		colors[0] = radGradientRot;
		colors[1] = radGradientGrau;
		colors[2] = radGradientGrau;
		colors[3] = radGradientGrau;
		colors[4] = radGradientGrau;
		colors[5] = radGradientGrau;
		colors[6] = radGradientGrau;
		colors[7] = radGradientGrau;
		colors[8] = radGradientGrau;
		colors[9] = radGradientGrau;
		break;
	case 34:
		// Turn heating off, red segment on right bottom
		angle = 1.87 * Math.PI + (32 - 16) * 0.02 * Math.PI + .5 * Math.PI; // tInnen
		// =
		// 32°,
		// fInnen
		// =
		// 48%,
		// l-Zeiger
		// in
		// der
		// Mitte,
		// r.Zeiger
		// oben
		// rechts
		angle2 = 1.06 * Math.PI - (48 - 26) * 0.12 * Math.PI / 40 + .5 * Math.PI;
		colors[0] = radGradientGrau;
		colors[1] = radGradientGrau;
		colors[2] = radGradientGrau;
		colors[3] = radGradientGrau;
		colors[4] = radGradientRot;
		colors[5] = radGradientGrau;
		colors[6] = radGradientGrau;
		colors[7] = radGradientGrau;
		colors[8] = radGradientGrau;
		colors[9] = radGradientGrau;
		break;
	case 1:
	case 7:
	case 16:
		// Nothing todo, 3 green segments, in the middel, right and left
		angle = 1.94 * Math.PI + (22 - 19) * 0.024 * Math.PI + .5 * Math.PI; // rechte
		// zeiger
		// in
		// der
		// Mitte,
		// linker
		// Zeiger
		// in
		// der
		// mitte
		angle2 = 1.06 * Math.PI - (48 - 26) * 0.12 * Math.PI / 40 + .5 * Math.PI;
		colors[0] = radGradientGrau;
		colors[1] = radGradientGruen;
		colors[2] = radGradientGruen;
		colors[3] = radGradientGruen;
		colors[4] = radGradientGrau;
		colors[5] = radGradientGrau;
		colors[6] = radGradientGruen;
		colors[7] = radGradientGruen;
		colors[8] = radGradientGruen;
		colors[9] = radGradientGrau;
		break;
	case 4:
	case 9:
	case 17:
	case 32:
	case 33:
		// Open window
		angle = 1.94 * Math.PI + (22 - 19) * 0.024 * Math.PI + .5 * Math.PI; // rechter
		// Zeiger
		// in
		// der
		// Mitte,
		// lin.Zeiger
		// links
		// oben
		angle2 = 1.28 * Math.PI - (10 * 0.14 * Math.PI / 19) + .5 * Math.PI;
		colors[0] = radGradientGrau;
		colors[1] = radGradientGrau;
		colors[2] = radGradientGrau;
		colors[3] = radGradientGrau;
		colors[4] = radGradientGrau;
		colors[5] = radGradientRot;
		colors[6] = radGradientGrau;
		colors[7] = radGradientGrau;
		colors[8] = radGradientGrau;
		colors[9] = radGradientGrau;
		break;
	case 6:
		// Fenster zu, 1 red segment on left bottom
		angle = 1.94 * Math.PI + (22 - 19) * 0.024 * Math.PI + .5 * Math.PI; // rechter
		// Zeiger
		// in
		// der
		// Mitte,
		// lin.Zeiger
		// links
		// unten
		angle2 = 0.86 * Math.PI - (86 - 71) * 0.14 * Math.PI / 30 + .5 * Math.PI;
		colors[0] = radGradientGrau;
		colors[1] = radGradientGrau;
		colors[2] = radGradientGrau;
		colors[3] = radGradientGrau;
		colors[4] = radGradientGrau;
		colors[5] = radGradientGrau;
		colors[6] = radGradientGrau;
		colors[7] = radGradientGrau;
		colors[8] = radGradientGrau;
		colors[9] = radGradientRot;
		break;
	default:
		// status is yellow or undefined, yellow smily, grey segmente, both
		// arrows to the middel
		angle = 1.94 * Math.PI + (22 - 19) * 0.024 * Math.PI + .5 * Math.PI; // rechte
		// zeiger
		// in
		// der
		// Mitte,
		// linker
		// Zeiger
		// in
		// der
		// mitte
		angle2 = 1.06 * Math.PI - (48 - 26) * 0.12 * Math.PI / 40 + .5 * Math.PI;
		colors[0] = radGradientGrau;
		colors[1] = radGradientGrau;
		colors[2] = radGradientGrau;
		colors[3] = radGradientGrau;
		colors[4] = radGradientGrau;
		colors[5] = radGradientGrau;
		colors[6] = radGradientGrau;
		colors[7] = radGradientGrau;
		colors[8] = radGradientGrau;
		colors[9] = radGradientGrau;
		break;
	}

	// bow on right 1 top
	canvas.fillStyle = colors[0];
	canvas.beginPath();
	canvas.moveTo(340, 180);
	canvas.arc(340, 180, 180, 1.72 * Math.PI, 1.86 * Math.PI);
	canvas.closePath();
	canvas.fill();
	// message right top
	canvas.fillStyle = colors[0];
	canvas.font = "bold 16px Arial";
	canvas.fillText("Bitte", 475, 50);
	canvas.fillText("Heizung", 490, 70);
	canvas.fillText("aufdrehen", 505, 90);
	// on right 2 top
	canvas.fillStyle = colors[1];
	canvas.beginPath();
	canvas.moveTo(340, 180);
	canvas.arc(340, 180, 180, 1.87 * Math.PI, 1.93 * Math.PI);
	canvas.closePath();
	canvas.fill();
	// right middel
	canvas.fillStyle = colors[2];
	canvas.beginPath();
	canvas.moveTo(340, 180);
	canvas.arc(340, 180, 180, 1.94 * Math.PI, 0.06 * Math.PI);
	canvas.closePath();
	canvas.fill();
	// on rgiht bottom 1
	canvas.fillStyle = colors[3];
	canvas.beginPath();
	canvas.moveTo(340, 180);
	canvas.arc(340, 180, 180, 0.07 * Math.PI, 0.13 * Math.PI);
	canvas.closePath();
	canvas.fill();
	// on right bottom 2
	canvas.fillStyle = colors[4];
	canvas.beginPath();
	canvas.moveTo(340, 180);
	canvas.arc(340, 180, 180, 0.14 * Math.PI, 0.28 * Math.PI);
	canvas.closePath();
	canvas.fill();
	// message right bottom
	canvas.fillStyle = colors[4];
	canvas.font = "bold 16px Arial";
	canvas.fillText("Bitte", 505, 280);
	canvas.fillText("Heizung", 495, 300);
	canvas.fillText("runterdrehen", 475, 320);
	// Bow on left
	// left top 1
	canvas.fillStyle = colors[5];
	canvas.beginPath();
	canvas.moveTo(340, 180);
	canvas.arc(340, 180, 180, 1.28 * Math.PI, 1.14 * Math.PI, true);
	canvas.closePath();
	canvas.fill();
	// message, left top
	canvas.fillStyle = colors[5];
	canvas.font = "bold 16px Arial";
	canvas.fillText("Bitte", 170, 50);
	canvas.fillText("Fenster", 130, 70);
	canvas.fillText("öffnen", 120, 90);
	// left top 2
	canvas.fillStyle = colors[6];
	canvas.beginPath();
	canvas.moveTo(340, 180);
	canvas.arc(340, 180, 180, 1.13 * Math.PI, 1.07 * Math.PI, true);
	canvas.closePath();
	canvas.fill();
	// left middel
	canvas.fillStyle = colors[7];
	canvas.beginPath();
	canvas.moveTo(340, 180);
	canvas.arc(340, 180, 180, 1.06 * Math.PI, 0.94 * Math.PI, true);
	canvas.closePath();
	canvas.fill();
	// left bottom 1
	canvas.fillStyle = colors[8];
	canvas.beginPath();
	canvas.moveTo(340, 180);
	canvas.arc(340, 180, 180, 0.93 * Math.PI, 0.87 * Math.PI, true);
	canvas.closePath();
	canvas.fill();
	// left bottom 1
	canvas.fillStyle = colors[9];
	canvas.beginPath();
	canvas.moveTo(340, 180);
	canvas.arc(340, 180, 180, 0.86 * Math.PI, 0.72 * Math.PI, true);
	canvas.closePath();
	canvas.fill();
	// message left bottom
	canvas.fillStyle = colors[9];
	canvas.font = "bold 16px Arial";
	canvas.fillText("Bitte", 130, 280);
	canvas.fillText("Fenster", 125, 300);
	canvas.fillText("schließen", 120, 320);
	// remove inner white circle
	canvas.fillStyle = "white";
	canvas.beginPath();
	canvas.arc(340, 180, 165, 0, 2 * Math.PI);
	canvas.fill();
	// draw big center circle
	// create gradient
	var radGradient = canvas.createRadialGradient(340, 180, 20, 340, 180, 90)
	var stCol = "rgba(249, 249, 249, .6)";
	var stCol2 = "rgba(242, 242, 242, .4)";
	var stCol3 = "rgba(216, 216, 216, 1)";
	// Set gradient limits
	radGradient.addColorStop(0, stCol);
	radGradient.addColorStop(0.8, stCol2);
	radGradient.addColorStop(1, stCol3);
	canvas.fillStyle = radGradient;
	canvas.beginPath();
	canvas.arc(340, 180, 90, 0, 2 * Math.PI);
	canvas.fill();
	// small center circle
	var radGradient2 = canvas.createRadialGradient(340, 180, 0, 340, 180, 75);
	radGradient2.addColorStop(0, stCol);
	radGradient2.addColorStop(0.8, stCol2);
	radGradient2.addColorStop(1, stCol3);
	canvas.fillStyle = radGradient2;
	canvas.beginPath();
	canvas.arc(340, 180, 75, 0, 2 * Math.PI);
	canvas.fill();
	// smily
	// create green gradient
	var stCol4 = "rgba(185, 225, 99, 1)"; // bright green
	var stCol5 = "rgba(168, 216, 87, 1)"; // middle bright green
	var stCol6 = "rgba(146, 202, 68, 1)"; // green
	var radGradientSmileGruen = canvas.createRadialGradient(340, 180, 0, 340, 180, 40);
	radGradientSmileGruen.addColorStop(0, stCol4);
	radGradientSmileGruen.addColorStop(0.7, stCol5);
	radGradientSmileGruen.addColorStop(1, stCol6);
	// create yellow gradient
	var stCol7 = "rgba(245, 239, 157, .8)"; // bright yellow
	var stCol8 = "rgba(250, 239, 92, .8)"; // middle bright yellow
	var stCol9 = "rgba(255, 237, 0,.8)"; // yellow
	var radGradientSmileGelb = canvas.createRadialGradient(340, 180, 0, 340, 180, 40);
	radGradientSmileGelb.addColorStop(0, stCol7);
	radGradientSmileGelb.addColorStop(0.7, stCol8);
	radGradientSmileGelb.addColorStop(1, stCol9);
	// create red gradient
	var stCol10 = "rgba(252, 192, 192, 1)"; // bright red
	var stCol11 = "rgba(252, 109, 109, 1)"; // middle bright red
	var stCol12 = "rgba(255,51,51,.7)"; // red
	var radGradientSmileRot = canvas.createRadialGradient(340, 180, 0, 340, 180, 40);
	radGradientSmileRot.addColorStop(0, stCol10);
	radGradientSmileRot.addColorStop(0.9, stCol11);
	radGradientSmileRot.addColorStop(1, stCol12);
	// red smily
	switch (zustand) {
	case 0:
		canvas.fillStyle = "lightgrey";
		break;
	case 4:
	case 6:
	case 8:
	case 17:
	case 19:
	case 32:
	case 33:
	case 34:
	case 35:
		canvas.fillStyle = radGradientSmileRot;
		break;
	// yellow smily
	case 3:
	case 5:
	case 18:
		canvas.fillStyle = radGradientSmileGelb;
		break;
	// green smily
	default:
		canvas.fillStyle = radGradientSmileGruen;
		break;
	}

	canvas.beginPath();
	canvas.arc(340, 180, 40, 0, 2 * Math.PI);
	canvas.fill();
	// eyes of smily
	canvas.fillStyle = "white";
	canvas.beginPath();
	canvas.arc(322, 170, 4, 0, 2 * Math.PI);
	canvas.fill();
	canvas.beginPath();
	canvas.arc(355, 170, 4, 0, 2 * Math.PI);
	canvas.fill();
	// mouth of smily
	canvas.fillStyle = "white";
	canvas.beginPath();
	// red smily mouth
	switch (zustand) {
	case 4:
	case 6:
	case 8:
	case 17:
	case 19:
	case 32:
	case 33:
	case 34:
	case 35:
		canvas.moveTo(320, 198);
		canvas.quadraticCurveTo(340, 188, 357, 198);
		canvas.quadraticCurveTo(340, 174, 320, 198);
		break;
	// yellow smily mouth
	case 3:
	case 5:
	case 18:
		canvas.moveTo(320, 194);
		canvas.quadraticCurveTo(340, 199, 357, 194);
		canvas.quadraticCurveTo(340, 191, 320, 194);
		break;
	case 1:
	case 7:
	case 16:
		// green smily mouth
		canvas.moveTo(320, 188);
		canvas.quadraticCurveTo(340, 198, 357, 188);
		canvas.quadraticCurveTo(340, 212, 320, 188);
		break;
	default:
		// yellow mouth
		canvas.moveTo(320, 194);
		canvas.quadraticCurveTo(340, 199, 357, 194);
		canvas.quadraticCurveTo(340, 191, 320, 194);
		break;
	}
	canvas.closePath();
	canvas.fill();
	// show temperature bottom left
	canvas.fillStyle = "rgba(142,142,142,.8)";
	canvas.font = "24px Verdana";
	if (fInnen == undefined || isNaN(fInnen))
		canvas.fillText("Keine Werte verfügbar", 240, 350);
	else
		canvas.fillText(fInnen + "%", 240, 350);
	// show humidity bottom right
	canvas.fillStyle = "rgba(142,142,142,.8)";
	canvas.font = "24px Verdana";
	if (tInnen != undefined && !isNaN(tInnen))
		canvas.fillText("t " + tInnen + "°C", 390, 350);
	// arrow temperature
	canvas.save(); // save old status
	canvas.translate(340, 180); // center canvas
	canvas.rotate(angle); // rotate canvas

	canvas.beginPath(); // draw arrow
	canvas.moveTo(0, -100);
	canvas.lineTo(-10, -100);
	canvas.lineTo(0, -155);
	canvas.lineTo(10, -100);
	canvas.closePath();
	canvas.fillStyle = "lightgrey";
	canvas.fill();
	canvas.restore(); // restore saved canvas

	// arrow humidity
	canvas.save(); // save old status
	canvas.translate(340, 180); // center canvas
	canvas.rotate(angle2); // rotate canvas by angle2

	canvas.beginPath(); // draw arrow
	canvas.moveTo(0, -100);
	canvas.lineTo(-10, -100);
	canvas.lineTo(0, -155);
	canvas.lineTo(10, -100);
	canvas.closePath();
	canvas.fillStyle = "lightgrey";
	canvas.fill();
	canvas.restore(); // restore saved canvas
}
