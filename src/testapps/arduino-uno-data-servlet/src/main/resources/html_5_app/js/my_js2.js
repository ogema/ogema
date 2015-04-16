// JavaScript Document



var angle, angle2, fInnen, tInnen, zustand, colR1, colR2, colR3, colR4, colR5, colR6, colR7, colR8, colR9, colR10 ;




function draw(){
	
	  var c = document.getElementById("myCanvas");
	  var canvas = c.getContext('2d');      
	  canvas.clearRect(0, 0, 680, 440);
	  
	   // Graues Radient für den äußeren Kreis
	  var radGradientGrau = canvas.createRadialGradient(340, 180, 0, 340, 180, 180);
	  var stColHellGrau = "rgba(216, 216, 216, .2)";  //weiß    
	  var stColDunkel = "rgba(216, 216, 216, 1)"; //dunkelgrau
      // Verlaufspunkte setzen
      radGradientGrau.addColorStop(0.91, stColDunkel);
	  radGradientGrau.addColorStop(0.96, stColHellGrau);
 	  radGradientGrau.addColorStop(1, stColDunkel);
	  
	   // Grünes Radient für den äußeren Kreis
	  var radGradientGruen = canvas.createRadialGradient(340, 180, 0, 340, 180, 180);
	  var stColGruen = "rgba(146, 202, 68, 1)"; //grün
	  var stColHellGruen = "rgba(146, 202, 68, .2)"; //grün
      // Verlaufspunkte setzen
      radGradientGruen.addColorStop(0.91, stColGruen);
	  radGradientGruen.addColorStop(0.96, stColHellGruen);
 	  radGradientGruen.addColorStop(1, stColGruen);
	  
	   // Rotes Radient für den äußeren Kreis
	  var radGradientRot = canvas.createRadialGradient(340, 180, 0, 340, 180, 180);  
	  var stColRot = "rgba(255,51,51,1)"; //rot
	  var stColHellRot = "rgba(255,51,51,.2)"; //rot
      // Verlaufspunkte setzen
      radGradientRot.addColorStop(0.91, stColRot);
	  radGradientRot.addColorStop(0.96, stColHellRot);
 	  radGradientRot.addColorStop(1, stColRot);
	 
	  
 // Farbe und winkel für den äüßeren Kreis rechts berechnen	  
 function rechneWinkelFarbe(){
	if(zustand == 8 || zustand == 35){ // Heizung aufdrehen, rotes Segment oben rechts
		angle = 1.72*Math.PI+(8*0.00875*Math.PI)+.5*Math.PI; //tInnen=8°, fInnen = 48%
		angle2 = 1.06*Math.PI-(48-26)*0.12*Math.PI/40+.5*Math.PI;  
		colR1 =radGradientRot;
		colR2 =radGradientGrau; 
		colR3 =radGradientGrau;
		colR4 =radGradientGrau;
		colR5 =radGradientGrau;
		colR6 =radGradientGrau;
		colR7 = radGradientGrau; 
		colR8 = radGradientGrau;
		colR9 = radGradientGrau;
		colR10 = radGradientGrau;
	}else if(zustand == 34){   //  Heizung runterdrehen, rotes Segment rechts unten
		angle = 1.87*Math.PI+(32-16)*0.02*Math.PI+.5*Math.PI; //tInnen = 32°, fInnen = 48%, l-Zeiger in der Mitte, r.Zeiger oben rechts
		angle2 = 1.06*Math.PI-(48-26)*0.12*Math.PI/40+.5*Math.PI;
		colR1 =radGradientGrau;
		colR2 =radGradientGrau; 
		colR3 =radGradientGrau;
		colR4 =radGradientGrau;
		colR5 =radGradientRot;
		colR6 =radGradientGrau;
		colR7 = radGradientGrau; 
		colR8 = radGradientGrau;
		colR9 = radGradientGrau;
		colR10 = radGradientGrau;
	}else if(zustand == 1 || zustand == 7 || zustand == 16){ //Alles ok, 3 segmente gruen mitte rechts und links
		angle = 1.94*Math.PI+(22-19)*0.024*Math.PI+.5*Math.PI; //rechte zeiger in der Mitte, linker Zeiger in der mitte
		angle2 = 1.06*Math.PI-(48-26)*0.12*Math.PI/40+.5*Math.PI;   
		colR1 =radGradientGrau;		 
		colR2 =radGradientGruen;
		colR3 =radGradientGruen;
		colR4 =radGradientGruen;
		colR5 =radGradientGrau;
		colR6 =radGradientGrau;		 
		colR7 =radGradientGruen;
		colR8 =radGradientGruen;
		colR9 =radGradientGruen;
		colR10 =radGradientGrau;
	}else if(zustand == 4 || zustand == 9 || zustand == 32 || zustand == 33 || zustand == 17){  //Fenster öffnen
		angle = 1.94*Math.PI+(22-19)*0.024*Math.PI+.5*Math.PI; //rechter Zeiger in der Mitte, lin.Zeiger links oben
		angle2 = 1.28*Math.PI-(10*0.14*Math.PI/19)+.5*Math.PI; 
		colR1 = radGradientGrau;		 
		colR2 = radGradientGrau;
		colR3 = radGradientGrau;		
		colR4 = radGradientGrau;
		colR5 = radGradientGrau;
		colR6 = radGradientRot;
		colR7 = radGradientGrau; 
		colR8 = radGradientGrau;
		colR9 = radGradientGrau;
		colR10 = radGradientGrau;
	}else if(zustand == 6){ // Fenster zu, 1 segment rot unten links
		angle = 1.94*Math.PI+(22-19)*0.024*Math.PI+.5*Math.PI; //rechter Zeiger in der Mitte, lin.Zeiger links unten
		angle2 = 0.86*Math.PI-(86-71)*0.14*Math.PI/30+.5*Math.PI;
		colR1 = radGradientGrau;		 
		colR2 = radGradientGrau;
		colR3 = radGradientGrau;		
		colR4 = radGradientGrau;	
		colR5 = radGradientGrau;
		colR6 = radGradientGrau;		 
		colR7 = radGradientGrau;
		colR8 = radGradientGrau;		
		colR9 = radGradientGrau;	
		colR10 = radGradientRot;
	}else { 					//Zustand gelb oder undefiniert, gelber Smile, segmente grau, beide Zeiger in der Mitte 
		angle = 1.94*Math.PI+(22-19)*0.024*Math.PI+.5*Math.PI; //rechte zeiger in der Mitte, linker Zeiger in der mitte
		angle2 = 1.06*Math.PI-(48-26)*0.12*Math.PI/40+.5*Math.PI;   
		colR1 =radGradientGrau;		 
		colR2 =radGradientGrau;
		colR3 =radGradientGrau;		
		colR4 =radGradientGrau;	
		colR5 =radGradientGrau;
		colR6 =radGradientGrau;		 
		colR7 =radGradientGrau;
		colR8 =radGradientGrau;		
		colR9 =radGradientGrau;	
		colR10 =radGradientGrau;
	}
		
};
/*
 // Farbe und winkel für den äüßeren Kreis links berechnen	  	
 function rechneWinkelFarbeLinks(){
	if(fInnen>=0 && fInnen<20){ // 1) rotes Segment rechts oben
		angle2 = 1.28*Math.PI-(fInnen*0.14*Math.PI/19)+.5*Math.PI;
		
		colR6 =radGradientRot;
		colR7 = radGradientGrau; 
		colR8 = radGradientGrau;
		colR9 = radGradientGrau;
		colR10 = radGradientGrau;
	}else if(fInnen>=20 && fInnen<=25){   //  2) 1 segment gruen oben rechts
		angle2 = 1.13*Math.PI-(fInnen-20)*0.06*Math.PI/6+.5*Math.PI;
		//+.5*Math.PI;
		colR6 =radGradientGrau;
		colR7 =radGradientGruen; 
		colR8 =radGradientGrau;
		colR9 =radGradientGrau;
		colR10 =radGradientGrau;
	}else if(fInnen>25 && fInnen<65){ // 3) 3 segmente gruen mitte rechts
		angle2 = 1.06*Math.PI-(fInnen-26)*0.12*Math.PI/40+.5*Math.PI;  
		//+.5*Math.PI;
		colR6 =radGradientGrau;		 
		colR7 =radGradientGruen;
		colR8 =radGradientGruen;
		colR9 =radGradientGruen;
		colR10 =radGradientGrau;
	}else if(fInnen>=65 && fInnen<=70){  // 4) 1 segment gruen unten rechts
		angle2 = 0.93*Math.PI-(fInnen-65)*0.06*Math.PI/6+.5*Math.PI;
		//+.5*Math.PI;
		colR6 =radGradientGrau;		 
		colR7 =radGradientGrau;
		colR8 =radGradientGrau;		
		colR9 =radGradientGruen;
		colR10 =radGradientGrau;
	}else if(fInnen >70 && fInnen <=100){ // 5) 1 rotes Segment rechts unten
		angle2 = 0.86*Math.PI-(fInnen-71)*0.14*Math.PI/30+.5*Math.PI;
		//+.5*Math.PI;
		colR6 =radGradientGrau;		 
		colR7 =radGradientGrau;
		colR8 =radGradientGrau;		
		colR9 =radGradientGrau;	
		colR10 =radGradientRot;
	}
};
*/
	  rechneWinkelFarbe();
	  
	  
	 
	  


//Bogen rechts	
	  //rote halbkreis rechts oben
 	  canvas.fillStyle =colR1;
      canvas.beginPath();
	  canvas.moveTo(340,180);
      canvas.arc(340, 180, 180,  1.72*Math.PI, 1.86*Math.PI );
	  canvas.closePath();
      canvas.fill();
	  //Meldung
	  canvas.fillStyle =colR1;
	  canvas.font = "bold 16px Arial";
	  canvas.fillText("Bitte", 475, 50);	
	  canvas.fillText("Heizung", 490, 70);
	  canvas.fillText("aufdrehen", 505, 90);
	  
	  //rechtsgrün
	  canvas.fillStyle =colR2; //"rgba(146, 202, 68, .7)"
      canvas.beginPath();
	  canvas.moveTo(340,180);
      canvas.arc(340, 180, 180,  1.87*Math.PI, 1.93*Math.PI );//0.06*PI
	  canvas.closePath();
      canvas.fill();
	  //rechtsgrün Mitte
	  canvas.fillStyle =colR3;
      canvas.beginPath();
	  canvas.moveTo(340,180);
      canvas.arc(340, 180, 180,  1.94*Math.PI, 0.06*Math.PI ); //0.12*PI
	  canvas.closePath();
      canvas.fill();
	   //rechtsgrün
	  canvas.fillStyle =colR4;
      canvas.beginPath();
	  canvas.moveTo(340,180);
      canvas.arc(340, 180, 180, 0.07*Math.PI, 0.13*Math.PI );//0.06*PI
	  canvas.closePath();
      canvas.fill();
	  
	  //Halbkreis rechts rot unten
 	  canvas.fillStyle =colR5;
      canvas.beginPath();
	  canvas.moveTo(340,180);
      canvas.arc(340, 180, 180,  0.14*Math.PI, 0.28*Math.PI );
	  canvas.closePath();
      canvas.fill();
	  //Meldung
	  canvas.fillStyle =colR5;
	  canvas.font = "bold 16px Arial";
	  canvas.fillText("Bitte", 505, 280);	
	  canvas.fillText("Heizung", 495, 300);
	  canvas.fillText("runterdrehen", 475, 320);
	  
//Bogen links	  
	  //rote halbkreis links oben
 	  canvas.fillStyle =colR6;
      canvas.beginPath();
	  canvas.moveTo(340,180);
      canvas.arc(340, 180, 180,  1.28*Math.PI, 1.14*Math.PI, true );
	  canvas.closePath();
      canvas.fill();
	  //Meldung links oben
	  canvas.fillStyle =colR6;
	  canvas.font = "bold 16px Arial";
	  canvas.fillText("Bitte", 170, 50);	
	  canvas.fillText("Fenster", 130, 70);
	  canvas.fillText("öffnen", 120, 90);
	  
	  //linksgrün
	  canvas.fillStyle =colR7;
      canvas.beginPath();
	  canvas.moveTo(340,180);
      canvas.arc(340, 180, 180,  1.13*Math.PI, 1.07*Math.PI, true );
	  canvas.closePath();
      canvas.fill();
	  //linksgrün Mitte
	  canvas.fillStyle =colR8;
      canvas.beginPath();
	  canvas.moveTo(340,180);
      canvas.arc(340, 180, 180,  1.06*Math.PI, 0.94*Math.PI, true );
	  canvas.closePath();
      canvas.fill();
	   //linksgrün
	  canvas.fillStyle =colR9;
      canvas.beginPath();
	  canvas.moveTo(340,180);
      canvas.arc(340, 180, 180, 0.93*Math.PI, 0.87*Math.PI, true );
	  canvas.closePath();
      canvas.fill();
	  
	  //Halbkreis links rot unten
 	  canvas.fillStyle =colR10;
      canvas.beginPath();
	  canvas.moveTo(340,180);
      canvas.arc(340, 180, 180,  0.86*Math.PI, 0.72*Math.PI, true );
	  canvas.closePath();
      canvas.fill();
	  //Meldung links unten
	  canvas.fillStyle =colR10;
	  canvas.font = "bold 16px Arial";
	  canvas.fillText("Bitte", 130, 280);	
	  canvas.fillText("Fenster", 125, 300);
	  canvas.fillText("schließen", 120, 320);
	  
	  //wiesßer kreis
	  canvas.fillStyle ="white";
      canvas.beginPath();	 
      canvas.arc(340, 180, 165,  0, 2*Math.PI );	  
      canvas.fill();
	  
	

//Großer Kreis in der MItte
	  //Verlauf erstellen
	  var radGradient = canvas.createRadialGradient(340, 180, 20, 340, 180, 90)
	  var stCol = "rgba(249, 249, 249, .6)";	  
	  var stCol2 = "rgba(242, 242, 242, .4)";
	  var stCol3 = "rgba(216, 216, 216, 1)";
      // Verlaufspunkte setzen
      radGradient.addColorStop(0, stCol);
	  radGradient.addColorStop(0.8, stCol2);
 	  radGradient.addColorStop(1, stCol3);
      // Füll-Style mit Gradient auszeichnen
      canvas.fillStyle = radGradient;
	  //Kreis zeichnen
      canvas.beginPath();
      canvas.arc(340, 180, 90, 0, 2*Math.PI);
      canvas.fill();

//Kleinerer Kreis in der Mitte:
	  var radGradient2 = canvas.createRadialGradient(340, 180, 0, 340, 180, 75);
	 
	  radGradient2.addColorStop(0, stCol);
	  radGradient2.addColorStop(0.8, stCol2);
 	  radGradient2.addColorStop(1, stCol3);
      
	  canvas.fillStyle = radGradient2;
	  canvas.beginPath();
      canvas.arc(340, 180, 75, 0, 2*Math.PI);
      canvas.fill();
	  
 //Smile:
	  //den gruenen Verlauf erstellen
	  var stCol4 = "rgba(185, 225, 99, 1)"; // hellgruen	  
	  var stCol5 = "rgba(168, 216, 87, 1)"; //mittelgruen
	  var stCol6 = "rgba(146, 202, 68, 1)"; //gruen
	  var radGradientSmileGruen = canvas.createRadialGradient(340, 180, 0, 340, 180, 40);
	 
	  radGradientSmileGruen.addColorStop(0, stCol4);
	  radGradientSmileGruen.addColorStop(0.7, stCol5);
 	  radGradientSmileGruen.addColorStop(1, stCol6);
      
	  //den gelben Verlauf erstellen
	  var stCol7 = "rgba(245, 239, 157, .8)"; //hellgelb	  
	  var stCol8 = "rgba(250, 239, 92, .8)";  //mittelgelb
	  var stCol9 = "rgba(255, 237, 0,.8)";    //gelb
	  var radGradientSmileGelb = canvas.createRadialGradient(340, 180, 0, 340, 180, 40);
	 
	  radGradientSmileGelb.addColorStop(0, stCol7);
	  radGradientSmileGelb.addColorStop(0.7, stCol8);
 	  radGradientSmileGelb.addColorStop(1, stCol9);
	  
	  //den roten Verlauf erstellen
	  var stCol10 = "rgba(252, 192, 192, 1)"; // hellrot
	  var stCol11 = "rgba(252, 109, 109, 1)"; //mittelrot
	  var stCol12 = "rgba(255,51,51,.7)"; 	  //rot
	  var radGradientSmileRot = canvas.createRadialGradient(340, 180, 0, 340, 180, 40);
	 
	  radGradientSmileRot.addColorStop(0, stCol10);
	  radGradientSmileRot.addColorStop(0.9, stCol11);
 	  radGradientSmileRot.addColorStop(1, stCol12);
	  
	  //rotes Smile Farbe
	  if( zustand == 4 || zustand == 6 || zustand == 8 ||zustand == 17 ||zustand == 19 ||zustand == 32 ||zustand == 33 ||zustand == 34 
	  ||zustand == 35 ){
		  canvas.fillStyle = radGradientSmileRot;
	  //gelbes Smile Farbe
	  }else if( zustand == 3 || zustand == 5 || zustand == 18 ){ 
		  canvas.fillStyle = radGradientSmileGelb;
	  // grünes Smile Farbe
	  }else{									
		  canvas.fillStyle = radGradientSmileGruen;
	  }
		  
		 
	  canvas.beginPath();
      canvas.arc(340, 180, 40, 0, 2*Math.PI);
      canvas.fill();
	  
  	  //smile Augen:
	  canvas.fillStyle ="white";
      canvas.beginPath();
      canvas.arc(322, 170, 4, 0, 2*Math.PI);
      canvas.fill();
	  canvas.beginPath();
      canvas.arc(355, 170, 4, 0, 2*Math.PI);
      canvas.fill();
	  //smile Mund:
	  canvas.fillStyle ="white";
      canvas.beginPath();
	  
	  //rotes Smile Mundwinkel
	  if(zustand == 4 || zustand == 6 || zustand == 8 ||zustand == 17 ||zustand == 19 ||zustand == 32 ||zustand == 33 ||zustand == 34 
	  ||zustand == 35){ 	  
		canvas.moveTo(320, 198);
		canvas.quadraticCurveTo(340,188,357,198);
	    canvas.quadraticCurveTo(340,174,320,198);
	  //gelbes Smile Mundwinkel  
	  }else if(zustand == 3 || zustand == 5 || zustand == 18){ 
	    canvas.moveTo(320, 194);
		canvas.quadraticCurveTo(340,199,357,194);
	    canvas.quadraticCurveTo(340,191,320,194); 		 
	  }else if(zustand == 1 || zustand == 7 || zustand == 16) {     //grünes Smile Mundwinkel
		canvas.moveTo(320, 188);
	    canvas.quadraticCurveTo(340,198,357,188);
	    canvas.quadraticCurveTo(340,212,320,188);
	  }else{                                  // Mundwinkel gelb
		canvas.moveTo(320, 194);
		canvas.quadraticCurveTo(340,199,357,194);
	    canvas.quadraticCurveTo(340,191,320,194); 
	  }
	  canvas.closePath();
      canvas.fill();
	  
	  //Anzeige T links unten
	  canvas.fillStyle ="rgba(142,142,142,.8)";
	  canvas.font = "24px Verdana";
	  canvas.fillText(fInnen+"%", 240, 350);
	 

	  
	  //Anzeige F rechts unten
	  canvas.fillStyle ="rgba(142,142,142,.8)";
	  canvas.font = "24px Verdana";
	  canvas.fillText("t "+tInnen+"°C", 390, 350);
	  
	  
//Zeiger t
	  canvas.save();                     // Ausgangszustand speichern
  	  canvas.translate(340, 180);		 // canvas zum eigenen Mittelpunkt verschieben
 	  canvas.rotate(angle);	//canvas rotieren
  
  	  canvas.beginPath();				 //Zeiger zeichnen
      canvas.moveTo(0, -100);
	  canvas.lineTo(-10, -100);
  	  canvas.lineTo(0, -155); 
	  canvas.lineTo(10, -100);
	  canvas.closePath(); 	 	  
  	  canvas.fillStyle = "lightgrey";	
  	  canvas.fill();
/*		  
	  img2 = document.getElementById("linkehand");
	  canvas.drawImage(img2,-30,-160,40,60);
	  canvas.restore();					 //gespeicherten canvas-Zustand wiederherstellen
*/
	  canvas.restore();					 //gespeicherten canvas-Zustand wiederherstellen
	  
	  
//Zeiger f
	  canvas.save();                     // Ausgangszustand speichern
  	  canvas.translate(340, 180);		 // canvas zum eigenen Mittelpunkt verschieben
 	  canvas.rotate(angle2);			 //canvas um angle2 rotieren
	 
    canvas.beginPath();				 //Zeiger zeichnen
      canvas.moveTo(0, -100);
	  canvas.lineTo(-10, -100);
  	  canvas.lineTo(0, -155); 
	  canvas.lineTo(10, -100);
	  canvas.closePath(); 	 	  
  	  canvas.fillStyle = "lightgrey";	
  	  canvas.fill();
	  
/*
	  img = document.getElementById("rechtehand");
	  canvas.drawImage(img,-10,-160,40,60);
*/	 	
	  canvas.restore();				 //gespeicherten canvas-Zustand wiederherstellen
	  
	  

}