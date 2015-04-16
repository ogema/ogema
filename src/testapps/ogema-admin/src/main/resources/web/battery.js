function battery(canvas0,canvas1,maxval,title) {

var canvas_w,canvas_h;
var batW=300;
var batH=400;
var scale=0.2;
var batX;
var batY;
var r=batW/2;
var headR=40;
var headH=20;
var headW=80;
var endGradients = new Array(batH); // Die Topgradients vom Inhalt der Batterie
									// werden da abgelegt

// Levels fuer Batteriewarnfarben
var low=parseInt(batH/4);
var middle=parseInt(batH/2);
var high=parseInt(batH);


// Definition der Gradienten fuer den Batteryinhalt
// Green
var trunkGreen;

// Red
var trunkRed;

// Yellow
var trunkYellow;

var inited = false;			
// init();
inited = true; // Timer actions start after the initialization is done.

var maximum;
	try{

		canvas_w=canvas1.width;
		canvas_h=canvas1.height;
		var	context0;
		var context1;
			context0 = canvas0.getContext("2d");
			context1 = canvas1.getContext("2d");
	// Init context0
		context0.lineJoin="round";
		context0.lineWidth=20;
		context0.lineCap="round";
		// Init context1
		context1.lineJoin="round";
		context1.lineWidth=20;
		context1.lineCap="round";
		batX=(canvas_w-batW)/2
		batY=(canvas_h-batH)/2
		
		context0.font = '28px Arial';
		context0.textAlign='center'
		context0.textBaseline='top'
		context0.fillText(title, canvas_w/2,0,canvas_w);

	    context1.font = '48px Arial';

	// Definition der Gradienten fuer den Batteryinhalt
	// Green
	trunkGreen = context0.createLinearGradient(0, batY, batX+batW, batY);
	trunkGreen.addColorStop(0, 'rgba(0, 255, 0, 0.8)');
	trunkGreen.addColorStop(0.7, 'rgba(255, 255, 255, 0.8)');
	trunkGreen.addColorStop(1, 'rgba(0, 255, 0, 0.8)');

	<!--// Red-->
	trunkRed = context0.createLinearGradient(0, batY, batX+batW, batY);
	trunkRed.addColorStop(0, 'rgba(255,0,0,0.8)');
	trunkRed.addColorStop(0.7, 'rgba(255,255,255,0.8)');
	trunkRed.addColorStop(1, 'rgba(255,0,0,0.8)');
	<!--// Yellow-->
	trunkYellow = context0.createLinearGradient(0, batY, batX+batW, batY);
	trunkYellow.addColorStop(0, 'rgba(255,150,0,0.8)');
	trunkYellow.addColorStop(0.7, 'rgba(255,255,255,0.8)');
	trunkYellow.addColorStop(1, 'rgba(255,150,0,0.8)');
	// Battery container zeichnen
	var trunkGradient1 = context1.createLinearGradient(0, batY, batX+batW, batY);
	trunkGradient1.addColorStop(0, 'rgba(0, 0, 0, 0.3)');
	trunkGradient1.addColorStop(0.7, 'rgba(180, 180, 180, 0.3)');
	trunkGradient1.addColorStop(1, 'rgba(0, 0, 0, 0.3)');

	var endGradient1=context1.createRadialGradient(batX+r,(batY)/scale,r/8,batX+r,(batY)/scale,r);
	endGradient1.addColorStop(0,'rgba(0, 0, 0, 0.5)');
	endGradient1.addColorStop(0.9,'rgba(255, 255, 255, 0.5)');
	endGradient1.addColorStop(1,'rgba(0, 0, 0, 0)');

	drawTrunk(context1, batX, batY,batW,batH,r,scale,trunkGradient1);
	drawEnd(context1, batX, batY,batW,r,scale,endGradient1);
	drawEnd(context1, batX, batY+batH,batW,r,scale,endGradient1);

	// oberer und unterer Rand zeichen
	trunkGradient1 = context1.createLinearGradient(0, batY, batX+batW, batY);
	trunkGradient1.addColorStop(0, 'rgba(0, 0, 0, 1)');
	trunkGradient1.addColorStop(0.7, 'rgba(255, 255, 255, 1)');
	trunkGradient1.addColorStop(1, 'rgba(0, 0, 0, 1)');
	drawTrunk(context1, batX, batY-10,batW,10,r,scale,trunkGradient1);
	drawTrunk(context1, batX, batY+batH,batW,20,r,scale,trunkGradient1);

	// Battery Kopf zeichnen
	var trunkGradient2 = context1.createLinearGradient(0, batY-headH, batX+r+headR, batY-headH);
	trunkGradient2.addColorStop(0, 'rgba(0, 0, 0, 1.0)');
	trunkGradient2.addColorStop(0.7, 'rgba(200, 200, 200, 1.0)');
	trunkGradient2.addColorStop(1, 'rgba(0, 0, 0, 1.0)');

	var endGradient2=context1.createRadialGradient(batX+r,(batY-headH)/scale,headR/8,batX+r,(batY-headH)/scale,headR);
	endGradient2.addColorStop(0,'rgba(255, 255, 255, 1.0)');
	endGradient2.addColorStop(0.7,'rgba(200, 200, 200, 1.0)');
	endGradient2.addColorStop(1,'rgba(0, 0, 0, 0)');

	drawTrunk(context1, batX+r-headR, batY-headH,headW,headH,headR,scale,trunkGradient2);
	drawEnd(context1, batX+r-headR,(batY-headH),headW,headR,scale,endGradient2);

	// Battery inhalt zeichnen
	var deg = 0.01;
	var trunkGradient0 = context0.createLinearGradient(0, batY, batX+batW, batY);
	trunkGradient0.addColorStop(0, 'rgba(0, 255, 0, 0.5)');
	trunkGradient0.addColorStop(0.7, 'rgba(255, 255, 255, 0.5)');
	trunkGradient0.addColorStop(1, 'rgba(0, 255, 0, 0.5)');

	var endGradient0=context0.createRadialGradient(batX+r,(batY+batH*(1-deg))/scale,r/8,batX+r,(batY+batH*(1-deg))/scale,r);
	endGradient0.addColorStop(0,'rgba(0, 255, 0, 0.5)');
	endGradient0.addColorStop(0.1,'rgba(255, 255, 255, 0.5)');
	endGradient0.addColorStop(0.3,'rgba(0, 255, 0, 0.5)');
	endGradient0.addColorStop(0.5,'rgba(255, 255, 255, 0.5)');
	endGradient0.addColorStop(1,'rgba(0, 255, 0, 0)');
	drawTrunk(context0, batX, batY+batH*(1-deg),batW,batH*deg,r,scale,trunkGradient0);
	drawEnd(context0, batX, batY+batH*(1-deg),batW,r,scale,endGradient0);

	this.repaint=repaintContent;
	maximum=maxval;
	inited = true; // Timer actions start after the initialization is done.
	
	}catch(e){
	alert(e.toString());
}

function repaintContent(val){
	// Battery inhalt zeichnen
	var percent=val;
	var ctx=context0;
	if(percent>maximum)
		percent=maximum;
	if(percent==-1)
		return;

	percent=(percent/maximum)*100;
	var deg = percent/100;
	var pixelsH= parseInt(deg*batH);

	var trunk;
	if(pixelsH<low)
	{
		trunk=trunkRed;
	}else if(pixelsH<middle)
	{
		trunk=trunkYellow;
	}else
	{
		trunk=trunkGreen;
	}

	var end=endGradients[pixelsH];
	if(end==undefined){
	end=ctx.createRadialGradient(batX+r,(batY+batH-pixelsH)/scale,r/8,batX+r,(batY+batH-pixelsH)/scale,r);
	if(pixelsH<low)
	{
		end.addColorStop(0,'rgba(255, 0, 0,0.5)');
		end.addColorStop(0.3,'rgba(255, 0, 0,0.5)');
		end.addColorStop(1,'rgba(255, 0, 0,0)');
		trunk=trunkRed;
	}else if(pixelsH<middle)
	{
		end.addColorStop(0,'rgba(255,150,0,0.5)');
		end.addColorStop(0.3,'rgba(255,150,0,0.5)');
		end.addColorStop(1,'rgba(255,150,0,0)');
		trunk=trunkYellow;
	}else
	{
		end.addColorStop(0,'rgba(0, 255, 0, 0.5)');
		end.addColorStop(0.3,'rgba(0, 255, 0, 0.5)');
		end.addColorStop(1,'rgba(0, 255, 0, 0)');
		trunk=trunkGreen;
	}
	end.addColorStop(0.1,'rgba(255, 255, 255, 0.5)');
	end.addColorStop(0.5,'rgba(255, 255, 255, 0.5)');
	endGradients[pixelsH]=end;
	}

	ctx.clearRect(0, 30, canvas_w,canvas_h);
	drawTrunk(ctx, batX, batY+batH-pixelsH,batW,pixelsH,r,scale,trunk);
	drawEnd(ctx, batX, batY+batH-pixelsH,batW,r,scale,end);

	ctx.fillStyle='black';
	ctx.textAlign='center'
	ctx.fillText(val+"%", batX+(batW/2), batY+batH/2,canvas_w);
	}


	function drawTrunk(context,x,y,w,h,R,scale,gradient)
	{
	context.beginPath();
	context.moveTo(x,y);
	context.lineTo(x,y+h);
	context.save();
	context.scale(1, scale);
	context.arc(x+R,(y+h)/scale,R,Math.PI,Math.PI*2,true);
	context.restore();
	context.lineTo(x+2*R,y);
	context.save();
	context.scale(1,scale);
	context.arc(x+R,(y)/scale,R,0,Math.PI,false);
	context.restore();

	context.fillStyle = gradient;
	context.fill();
	context.closePath();
	}

	function drawEnd(context,x,y,w,R,scale,gradient)
	{
	context.save();
	context.scale(1, scale);
	context.fillStyle = gradient;
	context.fillRect(x,(y-R)/scale,w,w/scale);
	context.restore();
	}
}
