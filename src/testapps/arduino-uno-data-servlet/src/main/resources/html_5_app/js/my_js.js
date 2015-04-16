// JavaScript Document


var colorFinnen, colorFaussen, colorTinnen, colorTaussen, tInnen, tAussen, fInnen, fAussen, meldung, prior;

function createColorFinnen(){
	if(fInnen >= 0 && fInnen <=20){
	 colorFinnen = "rgba(255,237,0,0.5)"; //gelb
	}else if(fInnen > 20 && fInnen <70){
	 colorFinnen ="rgba(146, 202, 68, .6)"; //gruen
	}else if(fInnen >=70 && fInnen <=100){
	 colorFinnen = "rgba(255,51,51,.6)"; //rot
	}
}	 
function createColorFaussen(){
	if(fAussen >= 0 && fAussen <=20){
	 colorFaussen = "rgba(255,237,0,0.5)"; //gelb
	}else if(fAussen > 20 && fAussen <70){
	 colorFaussen ="rgba(146, 202, 68, .6)"; //gruen
	}else if(fAussen >=70 && fAussen <=100){
	 colorFaussen = "rgba(255,51,51,.6)"; //rot
	}
}	
function createColorTinnen(){
	if(tInnen >= -10 && tInnen <=16){
	 colorTinnen = "rgba(63,169,245,.5)"; //blau
	}else if(tInnen > 16 && tInnen <=26){
	 colorTinnen ="rgba(146, 202, 68,.6)"; //gruen
	}else if(tInnen >26 && tInnen <=50){
	 colorTinnen = "rgba(255,51,51,.6)"; //rot
	}
}
function createColorTaussen(){
	if(tAussen >= -10 && tAussen <=16){
	 colorTaussen = "rgba(63,169,245,.5)"; //blau
	}else if(tAussen > 16 && tAussen <=26){
	 colorTaussen ="rgba(146, 202, 68,.6)"; //gruen
	}else if(tAussen >26 && tAussen <=50){
	 colorTaussen = "rgba(255,51,51,.6)"; //rot
	}
}	 	 

function setMeldung(){
	$("#meldung").html(meldung);
	
	if( prior == 2 ){
//	  $("#meldung").css({"padding-top":"2.5em","padding-bottom":"2.5em"});
//	  $("#meldung").html("Die Luftfeuchtigkeit ist sehr niedrig");
	  $("#meldung").show().css({"color":"grey","border-color":"#FFED00"}); // setze gelben Rahmen
	}else if( prior == 1 ){
//	  $("#meldung").css({"padding-top":"2.5em","padding-bottom":"2.5em"});
//	  $("#meldung").html("Die Luftfeuchtigkeit ist in Ordnung");
	  $("#meldung").show().css({"color":"grey","border-color":"#92CA44"}); //setze gruenen Rahmen
	}else if( prior == 3 ){
//	 $("#meldung").css({"padding-top":"1.5em","padding-bottom":"1.5em"});
//	 $("#meldung").html("Die Luftfeuchtigkeit ist hoch.<br> Bitte öffnen Sie das Fenster");
	 $("#meldung").show().css({"color":"#FF3333","border-color":"#FF3333"}); //setze roten Rahmen
	}else if( prior == 4 ){
//	 $("#meldung").css({"padding-top":"20px","padding-bottom":"20px"});
//	 $("#meldung").html("Die Luftfeuchtigkeit ist sehr hoch.<br> Es droht Schimmelgefahr. <br>Bitte öffnen Sie das Fenster");
	 $("#meldung").css({"color":"#FF3333","border-color":"#FF3333"}).toggle();  //setze roten blinkenden Rahmen
	}
}

function draw(){
	
	  createColorFinnen();
	  createColorFaussen();
	  createColorTinnen();
	  createColorTaussen();
	  setMeldung();
	  
	  var c = document.getElementById("myCanvas");
	  var canvas = c.getContext('2d');
      var c2 = document.getElementById("myCanvas2");
	  var canvas2 = c2.getContext('2d');
	  var c3 = document.getElementById("myCanvas3");
	  var canvas3 = c3.getContext('2d');
	  var c4 = document.getElementById("myCanvas4");
	  var canvas4 = c4.getContext('2d');
	  canvas.clearRect(0, 0, 400, 300);
	  canvas2.clearRect(0, 0, 400, 300);
	  canvas3.clearRect(0, 0, 400, 300);
	  canvas4.clearRect(0, 0, 400, 300);
 // 	  createColorF();
//	  createColorT();
	  canvas.save();
	  canvas2.save();
	  canvas3.save();
	  canvas4.save();
	  canvas.scale(0.6,0.6);
	  canvas2.scale(0.6,0.6);
	  canvas3.scale(0.6,0.6);
	  canvas4.scale(0.6,0.6);

// canvas, Temperatur innen
	
	  //blue part
	  canvas.fillStyle ="rgba(63,169,245,0.6)";
      canvas.beginPath();
	  canvas.moveTo(270,130);
      canvas.arc(270, 130, 120,  1.5*Math.PI+ 2*Math.PI*0.001 , 1.5*Math.PI + 2*Math.PI*26/60);
	  canvas.closePath();
      canvas.fill();
	  //green part
	  canvas.fillStyle ="rgba(146, 202, 68, .6)";
      canvas.beginPath();
	  canvas.moveTo(270,130);
      canvas.arc(270, 130, 120, 1.5*Math.PI + 2*Math.PI*26.01/60, 1.5*Math.PI + 2*Math.PI*36/60);
	  canvas.closePath();
      canvas.fill();
	  //red part
	  canvas.fillStyle ="rgba(255,51,51,.6)";
      canvas.beginPath();
	  canvas.moveTo(270,130);
      canvas.arc(270, 130, 120, 1.5*Math.PI + 2*Math.PI*36.01/60, 1.5*Math.PI + 2*Math.PI);
	  canvas.closePath();
      canvas.fill();
	  //inner white part
	  canvas.fillStyle = "white";
	  canvas.beginPath();
      canvas.arc(270, 130, 115, 0, 2*Math.PI, true);
      canvas.fill();
      
	  //grey donut     
	  canvas.fillStyle = "lightgrey";
      canvas.beginPath();
      canvas.arc(270, 130, 80, 0, 2*Math.PI, true);
      canvas.fill();
	  canvas.fillStyle = "white";
	  canvas.beginPath();
      canvas.arc(270, 130, 50, 0, 2*Math.PI, true);
      canvas.fill();
	  
	  // Farbanzeige Temperatur innen	  
	  canvas.fillStyle = colorTinnen;
      canvas.beginPath();
	  canvas.moveTo(270,130);
      canvas.arc(270, 130, 100, 1.5*Math.PI, 1.5*Math.PI + (2*Math.PI*tInnen+10)/60+(2*Math.PI/360));
	  canvas.closePath();
      canvas.fill();
	  
	  // Unterschrift temperatur innen
	  canvas.fillStyle = "grey";
	  canvas.font = "50px Arial";
	  canvas.fillText(tInnen+"°C",180,200);
	  canvas.fillStyle = "grey";
	  canvas.font = "18px Arial";
	  canvas.fillText("Temperatur innen", 190, 280);
	  
	  //Farberklaerung
	  //blau
	  canvas.fillStyle ="rgba(63,169,245,0.6)";
	  canvas.beginPath();
      canvas.arc(140, 55, 10, 0, 2*Math.PI);
      canvas.fill();
	  //Text neben blau
	  canvas.fillStyle = "grey";
	  canvas.font = "16px Arial";	
	  canvas.fillText("zu niedrig",53, 60);
	  
	   //gruen
	  canvas.fillStyle ="rgba(146, 202, 68, .7)";
	  canvas.beginPath();
      canvas.arc(115, 100, 10, 0, 2*Math.PI);
      canvas.fill();
	  
	  //Text neben gruen
	  canvas.fillStyle = "grey";
	  canvas.font = "16px Arial";	
	  canvas.fillText("in Ordnung", 22, 105); 
	  
	   //rot
	  canvas.fillStyle = "rgba(255,51,51,.6)";
	  canvas.beginPath();
      canvas.arc(110, 145, 10, 0, 2*Math.PI);
      canvas.fill();
	  
	  //Text neben rot
	  canvas.fillStyle = "grey";
	  canvas.font = "16px Arial";	
	  canvas.fillText("zu hoch", 40, 150);
	
	  canvas.restore();
	    
//canvas 2, Temperatur aussen	  
	  //outer circle
	  //blue part
	  canvas2.fillStyle ="rgba(63,169,245,.6)";
      canvas2.beginPath();
	  canvas2.moveTo(130,130);
      canvas2.arc(130, 130, 120,  1.5*Math.PI+ 2*Math.PI*0.001 , 1.5*Math.PI + 2*Math.PI*26/60);
	  canvas2.closePath();
      canvas2.fill();
	  
	  //green part
	  canvas2.fillStyle ="rgba(146, 202, 68, .6)";
      canvas2.beginPath();
	  canvas2.moveTo(130,130);
      canvas2.arc(130, 130, 120, 1.5*Math.PI + 2*Math.PI*26.01/60, 1.5*Math.PI + 2*Math.PI*36/60);
	  canvas2.closePath();
      canvas2.fill();
	  
	  //red part
	  canvas2.fillStyle = "rgba(255,51,51,.6)";
      canvas2.beginPath();
	  canvas2.moveTo(130,130);
      canvas2.arc(130, 130, 120, 1.5*Math.PI + 2*Math.PI*36.01/60, 1.5*Math.PI + 2*Math.PI);
	  canvas2.closePath();
      canvas2.fill();
	 
	  //inner white part
	  canvas2.fillStyle = "white";
	  canvas2.beginPath();
      canvas2.arc(130, 130, 115, 0, 2*Math.PI, true);
      canvas2.fill();
      
	  //grey donut     
	  canvas2.fillStyle = "lightgrey";
      canvas2.beginPath();
      canvas2.arc(130, 130, 80, 0, 2*Math.PI, true);
      canvas2.fill();
	  canvas2.fillStyle = "white";
	  canvas2.beginPath();
      canvas2.arc(130, 130, 50, 0, 2*Math.PI, true);
      canvas2.fill();
	  
	  // Farbanzeige Temperatur außen	  
	  canvas2.fillStyle = colorTaussen;
      canvas2.beginPath();
	  canvas2.moveTo(130,130);
      canvas2.arc(130, 130, 100, 1.5*Math.PI, 1.5*Math.PI + (2*Math.PI*tAussen+10)/60+(2*Math.PI/360));
	  canvas2.closePath();
      canvas2.fill();
	  
	  //Unterschrift Temperatur außen
	  canvas2.fillStyle = "grey";
	  canvas2.font = "50px Arial";
	  canvas2.fillText(tAussen+"°C",40,200);
	  canvas2.fillStyle = "grey";
	  canvas2.font = "18px Arial";	
	  canvas2.fillText("Temperatur außen", 50, 280);
	  
	  //Farberklaerung
	  //blau
	  canvas2.fillStyle = "rgba(63,169,245,.6)";
	  canvas2.beginPath();
      canvas2.arc(260, 55, 10, 0, 2*Math.PI);
      canvas2.fill();
	  //Text neben blau
	  canvas2.fillStyle = "grey";
	  canvas2.font = "16px Arial";	
	  canvas2.fillText("zu niedrig", 280, 60);
	  
	  //gruen
	  canvas2.fillStyle ="rgba(146, 202, 68, .6)";
	  canvas2.beginPath();
      canvas2.arc(285, 100, 10, 0, 2*Math.PI);
      canvas2.fill();
	  
	  //Text neben gruen
	  canvas2.fillStyle = "grey";
	  canvas2.font = "16px Arial";	
	  canvas2.fillText("in Ordnung", 305, 105); 
	  
	   //rot
	  canvas2.fillStyle = "rgba(255,51,51,.6)";
	  canvas2.beginPath();
      canvas2.arc(290, 145, 10, 0, 2*Math.PI);
      canvas2.fill();
	  
	  //Text neben rot
	  canvas2.fillStyle = "grey";
	  canvas2.font = "16px Arial";	
	  canvas2.fillText("zu hoch", 310, 150); 
	  
	  canvas2.restore(); 
		
//canvas 3, Feuchtigkeit inen
	  //outer circle
	  //yellow part
	  canvas3.fillStyle = "rgba(255,237,0,0.6)";
      canvas3.beginPath();
	  canvas3.moveTo(270,130);
      canvas3.arc(270, 130, 120,  1.5*Math.PI+ 2*Math.PI*0.001 , 1.5*Math.PI + 2*Math.PI*0.2);
	  canvas3.closePath();
      canvas3.fill();
	  
	  //green part
	  canvas3.fillStyle ="rgba(146, 202, 68, .6)";
      canvas3.beginPath();
	  canvas3.moveTo(270,130);
      canvas3.arc(270, 130, 120, 1.5*Math.PI + 2*Math.PI*0.201, 1.5*Math.PI + 2*Math.PI*0.699);
	  canvas3.closePath();
      canvas3.fill();
	  
	  //red part
	  canvas3.fillStyle = "rgba(255,51,51,.6)";
      canvas3.beginPath();
	  canvas3.moveTo(270,130);
      canvas3.arc(270, 130, 120, 1.5*Math.PI + 2*Math.PI*0.7, 1.5*Math.PI + 2*Math.PI);
	  canvas3.closePath();
      canvas3.fill();
	  
	  //inner white part
	  canvas3.fillStyle = "white";
	  canvas3.beginPath();
      canvas3.arc(270, 130, 115, 0, 2*Math.PI, true);
      canvas3.fill();
           
      // grey donut
	  canvas3.fillStyle = "lightgrey";
      canvas3.beginPath();
      canvas3.arc(270, 130, 80, 0, 2*Math.PI, true);
      canvas3.fill();
	  canvas3.fillStyle = "white";
	  canvas3.beginPath();
      canvas3.arc(270, 130, 50, 0, 2*Math.PI, true);
      canvas3.fill();
	  
	  //Farbanzeige, Feuchtigkeit innen
	  canvas3.fillStyle = colorFinnen;
      canvas3.beginPath();
	  canvas3.moveTo(270,130);
      canvas3.arc(270, 130, 100, 1.5*Math.PI, 1.5*Math.PI + 2*Math.PI*fInnen/100);
	  canvas3.closePath();
      canvas3.fill();
	  
	  // Unterschrift
	  canvas3.fillStyle = "grey";
	  canvas3.font = "50px Arial";
	  canvas3.fillText(fInnen+"%",180,200);
	  canvas3.fillStyle = "grey";
	  canvas3.font = "18px Arial";	 
	  canvas3.fillText("Feuchtigkeit innen", 190, 280);
	  
	   //Farberklaerung
	  //gelb
	  canvas3.fillStyle = "rgba(255,237,0,0.6)";
	  canvas3.beginPath();
      canvas3.arc(140, 55, 10, 0, 2*Math.PI);
      canvas3.fill();
	  //Text neben gelb
	  canvas3.fillStyle = "grey";
	  canvas3.font = "16px Arial";	
	  canvas3.fillText("zu niedrig",53, 60);
	  
	   //gruen
	  canvas3.fillStyle ="rgba(146, 202, 68, .6)";
	  canvas3.beginPath();
      canvas3.arc(115, 100, 10, 0, 2*Math.PI);
      canvas3.fill();
	  
	  //Text neben gruen
	  canvas3.fillStyle = "grey";
	  canvas3.font = "16px Arial";	
	  canvas3.fillText("in Ordnung", 22, 105); 
	  
	   //rot
	  canvas3.fillStyle = "rgba(255,51,51,.6)";
	  canvas3.beginPath();
      canvas3.arc(110, 145, 10, 0, 2*Math.PI);
      canvas3.fill();
	  
	  //Text neben rot
	  canvas3.fillStyle = "grey";
	  canvas3.font = "16px Arial";	
	  canvas3.fillText("zu hoch", 40, 150); 
	  
	  canvas3.restore();
	    

//canvas 4, Feuchtigkeit aussen
	  //outer circle
	  //yellow part
	  canvas4.fillStyle ="rgba(255,237,0,0.6)";
      canvas4.beginPath();
	  canvas4.moveTo(130,130);
      canvas4.arc(130, 130, 120,  1.5*Math.PI+ 2*Math.PI*0.001 , 1.5*Math.PI + 2*Math.PI*0.2);
	  canvas4.closePath();
      canvas4.fill();
	  //green part
	  canvas4.fillStyle ="rgba(146, 202, 68, .6)";
      canvas4.beginPath();
	  canvas4.moveTo(130,130);
      canvas4.arc(130, 130, 120, 1.5*Math.PI + 2*Math.PI*0.201, 1.5*Math.PI + 2*Math.PI*0.699);
	  canvas4.closePath();
      canvas4.fill();
	  //red part
	  canvas4.fillStyle = "rgba(255,51,51,.6)";
      canvas4.beginPath();
	  canvas4.moveTo(130,130);
      canvas4.arc(130, 130, 120, 1.5*Math.PI + 2*Math.PI*0.7, 1.5*Math.PI + 2*Math.PI);
	  canvas4.closePath();
      canvas4.fill();
	  //inner white part
	  canvas4.fillStyle = "white";
	  canvas4.beginPath();
      canvas4.arc(130, 130, 115, 0, 2*Math.PI, true);
      canvas4.fill();
           
      //grey donut
	  canvas4.fillStyle = "lightgrey";
      canvas4.beginPath();
      canvas4.arc(130, 130, 80, 0, 2*Math.PI, true);
      canvas4.fill();
	  canvas4.fillStyle = "white";
	  canvas4.beginPath();
      canvas4.arc(130, 130, 50, 0, 2*Math.PI, true);
      canvas4.fill();
	  
	  //Farbanzeige  Feuchtigkeit aussen
	  canvas4.fillStyle = colorFaussen;
      canvas4.beginPath();
	  canvas4.moveTo(130,130);
      canvas4.arc(130, 130, 100, 1.5*Math.PI, 1.5*Math.PI + 2*Math.PI*fAussen/100);
	  canvas4.closePath();
      canvas4.fill();
	  
	  //unterschrift
	  canvas4.fillStyle = "grey";
	  canvas4.font = "50px Arial";
	  canvas4.fillText(fAussen+"%",40,200);
	  canvas4.fillStyle = "grey";
	  canvas4.font = "18px Arial";
	 
	  canvas4.fillText("Feuchtigkeit außen", 50, 280);
	  
	  //Farberklaerung
	  //gelb
	  canvas4.fillStyle = "rgba(255,237,0,0.6)";
	  canvas4.beginPath();
      canvas4.arc(260, 55, 10, 0, 2*Math.PI);
      canvas4.fill();
	  //Text neben gelb
	  canvas4.fillStyle = "grey";
	  canvas4.font = "16px Arial";	
	  canvas4.fillText("zu niedrig", 280, 60);
	  
	  //gruen
	  canvas4.fillStyle ="rgba(146, 202, 68, .6)";
	  canvas4.beginPath();
      canvas4.arc(285, 100, 10, 0, 2*Math.PI);
      canvas4.fill();
	  
	  //Text neben gruen
	  canvas4.fillStyle = "grey";
	  canvas4.font = "16px Arial";	
	  canvas4.fillText("in Ordnung", 305, 105); 
	  
	   //rot
	  canvas4.fillStyle = "rgba(255,51,51,.6)";
	  canvas4.beginPath();
      canvas4.arc(290, 145, 10, 0, 2*Math.PI);
      canvas4.fill();
	  
	  //Text neben rot
	  canvas4.fillStyle = "grey";
	  canvas4.font = "16px Arial";	
	  canvas4.fillText("zu hoch", 310, 150);
	  
	  canvas4.restore(); 
	   
      	
}
  
