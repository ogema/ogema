function barGraph(canvas, maxval) {

var canvas_w,canvas_h;
var barW=300;
var barH=400;
var scale=0.2;
var barX;
var barY;
var r=barW/2;
var endGradients = new Array(barH); // Die Topgradients vom Inhalt der Batterie
									// werden da abgelegt

// Levels fuer Batteriewarnfarben
var low=0;
var middle=0;
var high=parseInt(barH);


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

var context;
var maximum;
	try{

		this.setValue=repaintContent;
		canvas_w=canvas.width;
		canvas_h=canvas.height;

			context = canvas.getContext("2d");
	// Init context0
		context.lineJoin="round";
		context.lineWidth=20;
		context.lineCap="round";
		context.font = '28px Arial';
		
		barX=(canvas_w-barW)/2
		//barY=(canvas_h-barH)/2
		//barX=10;
		barY=10;

	// Definition der Gradienten fuer den Batteryinhalt
	// Green
	trunkGreen = context.createLinearGradient(0, barY, barX+barW, barY);
	trunkGreen.addColorStop(0, 'rgba(0, 255, 0, 0.5)');
	trunkGreen.addColorStop(0.7, 'rgba(255, 255, 255, 0.5)');
	trunkGreen.addColorStop(1, 'rgba(0, 255, 0, 0.5)');

	<!--// Red-->
	trunkRed = context.createLinearGradient(0, barY, barX+barW, barY);
	trunkRed.addColorStop(0, 'rgba(255,0,0,0.5)');
	trunkRed.addColorStop(0.7, 'rgba(255,255,255,0.5)');
	trunkRed.addColorStop(1, 'rgba(255,0,0,0.5)');
	<!--// Yellow-->
	trunkYellow = context.createLinearGradient(0, barY, barX+barW, barY);
	trunkYellow.addColorStop(0, 'rgba(255,150,0,0.5)');
	trunkYellow.addColorStop(0.7, 'rgba(255,255,255,0.5)');
	trunkYellow.addColorStop(1, 'rgba(255,150,0,0.5)');

	// Battery inhalt zeichnen
					var deg = 0.01;
	var trunkGradient0 = context.createLinearGradient(0, barY, barX+barW, barY);
	trunkGradient0.addColorStop(0, 'rgba(0, 255, 0, 0.5)');
	trunkGradient0.addColorStop(0.7, 'rgba(255, 255, 255, 0.5)');
	trunkGradient0.addColorStop(1, 'rgba(0, 255, 0, 0.5)');

	var endGradient0=context.createRadialGradient(barX+r,(barY+barH*(1-deg))/scale,r/8,barX+r,(barY+barH*(1-deg))/scale,r);
	endGradient0.addColorStop(0,'rgba(0, 255, 0, 0.5)');
	endGradient0.addColorStop(0.1,'rgba(255, 255, 255, 0.5)');
	endGradient0.addColorStop(0.3,'rgba(0, 255, 0, 0.5)');
	endGradient0.addColorStop(0.5,'rgba(255, 255, 255, 0.5)');
	endGradient0.addColorStop(1,'rgba(0, 255, 0, 0)');
					drawTrunk( barX, barY+barH*(1-deg),barW,barH*deg,r,scale,trunkGradient0);
					drawEnd( barX, barY+barH*(1-deg),barW,r,scale,endGradient0);

					maximum=maxval;
	inited = true; // Timer actions start after the initialization is done.
	
					}catch(e){
					alert(e.toString());
					}

function repaintContent(val){
	// Battery inhalt zeichnen
	var percent=val;
	var ctx=context;
	if(percent>maximum)
		percent=maximum;

	percent=(percent/maximum)*100;
	var deg = percent/100;
	var pixelsH= parseInt(deg*barH);

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
	end=ctx.createRadialGradient(barX+r,(barY+barH-pixelsH)/scale,r/8,barX+r,(barY+barH-pixelsH)/scale,r);
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

	ctx.clearRect(0, 0, canvas_w,canvas_h);
	drawTrunk( barX, barY+barH-pixelsH,barW,pixelsH,r,scale,trunk);
	drawEnd( barX, barY+barH-pixelsH,barW,r,scale,end);
	ctx.fillStyle='black';
	ctx.fillText(val+" W", barX+barW/4, barY+barH-pixelsH-r*scale);
	}


	function drawTrunk(x,y,w,h,R,scale,gradient)
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

	function drawEnd(x,y,w,R,scale,gradient)
	{
	context.save();
	context.scale(1, scale);
	context.fillStyle = gradient;
	context.fillRect(x,(y-R)/scale,w,w/scale);
	context.restore();
	}
}
