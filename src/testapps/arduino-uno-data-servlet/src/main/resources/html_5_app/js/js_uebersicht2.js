// JavaScript Document

 function umschalten(){
  $("#myCanvas").fadeTo("normal", 0).fadeTo("normal", 1);
 }

 
window.onload = function draw(){
//	var um = setInterval(function(){umschalten()},1500);
  // Umrisslinie eines Rechtecks mit abgerundeten Ecken (Radius = 10) zeichnen

  
  qpCanvas({id: 'myCanvas'}).strokeStyle('rgba(255,51,51,.6)').lineWidth(4).strokeRoundRect(2, 2, 196, 96, {r: 20}).lineWidth(1);//rot
  qpCanvas({id: 'myCanvas2'}).strokeStyle('rgba(146, 202, 68, .6)').lineWidth(4).strokeRoundRect(2, 2, 196, 96, {r: 20}).lineWidth(1);//gruen
  qpCanvas({id: 'myCanvas3'}).strokeStyle('rgba(255,237,0,0.5)').lineWidth(4).strokeRoundRect(2, 2, 196, 96, {r: 20}).lineWidth(1);//gelb
  qpCanvas({id: 'myCanvas4'}).strokeStyle('rgba(146, 202, 68, .6)').lineWidth(4).strokeRoundRect(2, 2, 196, 96, {r: 20}).lineWidth(1);//gruen
  qpCanvas({id: 'myCanvas5'}).strokeStyle('rgba(146, 202, 68, .6)').lineWidth(4).strokeRoundRect(2, 2, 196, 96, {r: 20}).lineWidth(1);//gruen
  qpCanvas({id: 'myCanvas6'}).strokeStyle('rgba(146, 202, 68, .6)').lineWidth(4).strokeRoundRect(2, 2, 196, 96, {r: 20}).lineWidth(1);//gruen
  
	  var c = document.getElementById("myCanvas");
	  var canvas = c.getContext('2d');
      var c2 = document.getElementById("myCanvas2");
	  var canvas2 = c2.getContext('2d');
	  var c3 = document.getElementById("myCanvas3");
	  var canvas3 = c3.getContext('2d');
	  var c4 = document.getElementById("myCanvas4");
	  var canvas4 = c4.getContext('2d');
	  var c5 = document.getElementById("myCanvas5");
	  var canvas5 = c5.getContext('2d');
	  var c6 = document.getElementById("myCanvas6");
	  var canvas6 = c6.getContext('2d');
//Canvas 1, Küche	  
	  canvas.fillStyle = "grey";
	  canvas.textBaseline="middle";
	  canvas.font = "24px Arial";
	  canvas.textAlign="center"; 
	  canvas.fillText("Küche", 100, 50);
	 
	  
//Canvas 2, Bad
	  canvas2.fillStyle = "grey";
	  canvas2.font = "24px Arial";
	  canvas2.textBaseline="middle";
	  canvas2.textAlign="center"; 
	  canvas2.fillText("Bad", 100, 50);

//Canvas 3, Wohnzimmer   
	  canvas3.fillStyle = "grey";
	  canvas3.font = "24px Arial";
	  canvas3.textBaseline="middle";
	  canvas3.textAlign="center"; 
	  canvas3.fillText("Wohnzimmer", 100, 50);

//Canvas 4, Esszimmer	  
	  canvas4.fillStyle = "grey";
	  canvas4.font = "24px Arial";
	  canvas4.textBaseline="middle";
	  canvas4.textAlign="center"; 
	  canvas4.fillText("Esszimmer", 100, 50);

//Canvas 5, Schlafzimmer	  
	  canvas5.fillStyle = "grey";
	  canvas5.font = "24px Arial";
	  canvas5.textBaseline="middle";
	  canvas5.textAlign="center"; 
	  canvas5.fillText("Schlafzimmer", 100, 50);

//Canvas 6, Schlafzimmer	  
	  canvas6.fillStyle = "grey";
	  canvas6.font = "24px Arial"
	  canvas6.textBaseline="middle";
	  canvas6.textAlign="center"; 
	  canvas6.fillText("Kinderzimmer", 100, 50);
	  
	 
 
  // Gefülltes Rechteck mit abgerundeten Ecken (Radius = 5) um 20 Grad um die linke obere Ecke gedreht zeichnen
//  qpCanvas({id: 'myCanvas'}).fillStyle('rgba(251, 184, 56, 0.8)').fillRoundRect(60, 10, 100, 150, {r: 5, phi: 20});

  // Je fünf Quadrate (gefüllt und Umriss) mit abgerundeten Ecken um verschiedene Drehwinkel zeichnen
/*
  var intAnz = 5;
  for(var i=0; i<intAnz; i++){
    qpCanvas({id: 'myCanvas'}).fillStyle('rgba('+ (251-i*10) + ', '+ (184-i*10) + ', '+ (56+i*10) + ', 1)').fillRoundRect(220, 220, 40, 40, {r: 8, phi: (0+i*360/intAnz)});
    qpCanvas({id: 'myCanvas'}).strokeStyle('#444').strokeRoundRect(220, 220, 40, 40, {r: 8, phi: (0+i*360/intAnz)});
  }
  */
}