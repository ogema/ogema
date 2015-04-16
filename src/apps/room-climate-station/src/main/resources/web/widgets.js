var yellow = "rgba(255,237,0,0.5)";
var green = "rgba(146, 202, 68, .6)";
var red = "rgba(255,51,51,.6)";
var blue = "rgba(63,169,245,.5)";

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
	} else if (temp > 26 && temp <= 60) {
		return red;
	}
}

function temp_pie(canvas, titel, temp) {

	canvas.clearRect(0, 0, 400, 300);
	canvas.save();
	canvas.scale(0.6, 0.6);
	// canvas, Temperatur inside

	// blue part
	canvas.fillStyle = "rgba(63,169,245,0.6)";
	canvas.beginPath();
	canvas.moveTo(270, 130);
	canvas.arc(270, 130, 120, 1.5 * Math.PI, 1.5 * Math.PI + 2 * Math.PI * 16
			/ 60);
	canvas.closePath();
	canvas.fill();
	// green part
	canvas.fillStyle = "rgba(146, 202, 68, .6)";
	canvas.beginPath();
	canvas.moveTo(270, 130);
	canvas.arc(270, 130, 120, 1.5 * Math.PI + 2 * Math.PI * 16 / 60, 1.5
			* Math.PI + 2 * Math.PI * 26 / 60);
	canvas.closePath();
	canvas.fill();
	// red part
	canvas.fillStyle = "rgba(255,51,51,.6)";
	canvas.beginPath();
	canvas.moveTo(270, 130);
	canvas.arc(270, 130, 120, 1.5 * Math.PI + 2 * Math.PI * 26 / 60, 1.5
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
	canvas.fillStyle = getTemperatureColor(temp);
	canvas.beginPath();
	canvas.moveTo(270, 130);
	canvas.arc(270, 130, 100, 1.5 * Math.PI, 1.5 * Math.PI
			+ (2 * Math.PI * temp) / 60 + (2 * Math.PI / 360));
	canvas.closePath();
	canvas.fill();
	// description indoor temperature
	canvas.fillStyle = "grey";
	canvas.font = "50px Arial";
	canvas.fillText(temp + "°C", 180, 200);
	canvas.fillStyle = "grey";
	canvas.font = "18px Arial";
	canvas.fillText(titel, 190, 280);
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
}

function humidity_pie(canvas, titel, rh) {
	canvas.clearRect(0, 0, 400, 300);
	canvas.save();
	canvas.scale(0.6, 0.6);
	// canvas 3, indoor humidity
	// outer circle
	// yellow part
	canvas.fillStyle = "rgba(255,237,0,0.6)";
	canvas.beginPath();
	canvas.moveTo(270, 130);
	canvas.arc(270, 130, 120, 1.5 * Math.PI, 1.5
			* Math.PI + 2 * Math.PI * 0.2);
	canvas.closePath();
	canvas.fill();
	// green part
	canvas.fillStyle = "rgba(146, 202, 68, .6)";
	canvas.beginPath();
	canvas.moveTo(270, 130);
	canvas.arc(270, 130, 120, 1.5 * Math.PI + 2 * Math.PI * 0.2, 1.5
			* Math.PI + 2 * Math.PI * 0.7);
	canvas.closePath();
	canvas.fill();
	// red part
	canvas.fillStyle = "rgba(255,51,51,.6)";
	canvas.beginPath();
	canvas.moveTo(270, 130);
	canvas.arc(270, 130, 120, 1.5 * Math.PI + 2 * Math.PI * 0.7, 1.5 * Math.PI
			+ 2 * Math.PI);
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
	// pie chart, indoor humidity
	canvas.fillStyle = getHumidityColor(rh);
	canvas.beginPath();
	canvas.moveTo(270, 130);
	canvas.arc(270, 130, 100, 1.5 * Math.PI, 1.5 * Math.PI + 2 * Math.PI * rh
			/ 100);
	canvas.closePath();
	canvas.fill();
	// Description
	canvas.fillStyle = "grey";
	canvas.font = "50px Arial";
	canvas.fillText(rh + "%", 180, 200);
	canvas.fillStyle = "grey";
	canvas.font = "18px Arial";
	canvas.fillText(titel, 190, 280);
	// Legend
	// yellow
	canvas.fillStyle = "rgba(255,237,0,0.6)";
	canvas.beginPath();
	canvas.arc(140, 55, 10, 0, 2 * Math.PI);
	canvas.fill();
	// Text legend yellow
	canvas.fillStyle = "grey";
	canvas.font = "16px Arial";
	canvas.fillText("zu niedrig", 53, 60);
	// green
	canvas.fillStyle = "rgba(146, 202, 68, .6)";
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
}
