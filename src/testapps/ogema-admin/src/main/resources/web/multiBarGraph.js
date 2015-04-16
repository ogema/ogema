function multiBarGraph(canvas,count, maxval,iconsrcs,title,bW) {

	var canvas_w,canvas_h;
	var barW=bW;
	var barH=400;
	var scale=0.2;
	var barX;
	var barY;
	var r=barW/2;
	var bottom =150
	var offY=40

//	Levels fuer Batteriewarnfarben
	var low=parseInt(barH/4);
	var middle=parseInt(barH/2);
	var high=parseInt(barH);
	var sources=iconsrcs

//	Definition der Gradienten fuer den Batteryinhalt
//	Green
	var trunkGreen;

//	Red
	var trunkRed;

//	Yellow
	var trunkYellow;

	var inited = false;			
//	init();
	inited = true; // Timer actions start after the initialization is done.
	var numberOfBars=count
	var context
	var maximum

	var icons = new Array(numberOfBars);
	var iconorder=0

		try{
			this.setValue=repaintContent;
			canvas_w=canvas.width;
			canvas_h=canvas.height;
			var bGap
			if(barW==undefined){
				bGap=20
				barW=canvas_w/numberOfBars-bGap;
			}else{
				bGap=canvas_w/numberOfBars;
				bGap=bGap-bW
			}
			var r=barW/2;
			barX=10;
			barY=bottom;
			barH=canvas_h-barY-bottom

			context = canvas.getContext("2d");
			// Init context
			context.lineJoin="round";
			context.lineWidth=2;
			context.lineCap="round";
			context.font = '28px Arial';
			context.textAlign='center'
				context.textBaseline='top'
					context.fillText(title, canvas_w/2,0,canvas_w);


			var trunkRed =new Array(numberOfBars)
			var trunkYellow =new Array(numberOfBars)
			var trunkGreen =new Array(numberOfBars)
//			to get image data without drawing, so we are able to determine the correct zoom factor
			if(iconsrcs!=undefined)
				for(i=0;i<numberOfBars;i++)
				{
					var w,h;
					icons[i]=new Image();
					icons[i].src=iconsrcs[i]
					icons[i].onload=function(){
						w=this.width
						h=this.height
						iconorder=getXoffset(this.src);
						if(w>barW || h>bottom){
							var zoomx=(barW)/w
							var zoomy=(bottom-r*scale)/h
							var zoom;

							context.save()

							if(zoomx>zoomy){
								zoom=zoomy
								context.scale(zoom,zoom)
								context.drawImage(this,(barX+(barW+bGap)*iconorder+(barW-w*zoom)/2)/zoom,(barY+barH+r*scale)/zoom)
							}
							else{
								zoom =zoomx
								context.scale(zoom,zoom)
								context.drawImage(this,(barX+(barW+bGap)*iconorder+(barW-w*zoom)/2)/zoom,(barY+barH+r*scale+(bottom-h*zoom)/2)/zoom)
							}
							context.restore()
						}else{
							context.drawImage(this,(barX+(barW+bGap)*iconorder+(barW-w)/2),(barY+barH+r*scale+(bottom-h)/2))			
						}
						iconorder++
					}
				}

			for(i=0;i<numberOfBars;i++)
			{
				trunkRed[i] = context.createLinearGradient((barW+bGap)*i, barY, barX+barW+(barW+bGap)*i, barY);
				trunkRed[i].addColorStop(0, 'rgba(255,0,0,0.9)');
				trunkRed[i].addColorStop(0.7, 'rgba(255,255,255,0.2)');
				trunkRed[i].addColorStop(1, 'rgba(255,0,0,0.9)');
				<!--// Yellow-->
				trunkYellow[i] = context.createLinearGradient((barW+bGap)*i, barY, barX+barW+(barW+bGap)*i, barY);
				trunkYellow[i].addColorStop(0, 'rgba(255,150,0,0.9)');
				trunkYellow[i].addColorStop(0.7, 'rgba(255,255,255,0.2)');
				trunkYellow[i].addColorStop(1, 'rgba(255,150,0,0.9)');
				// Green
				trunkGreen[i] = context.createLinearGradient((barW+bGap)*i, barY, barX+barW+(barW+bGap)*i, barY);
				trunkGreen[i].addColorStop(0, 'rgba(0, 255, 0, 0.9)');
				trunkGreen[i].addColorStop(0.7, 'rgba(255, 255, 255, 0.2)');
				trunkGreen[i].addColorStop(1, 'rgba(0, 255, 0, 0.9)');
			}
			maximum=maxval;
			inited = true; // Timer actions start after the initialization is done.

		}catch(e){
			alert(e.toString());
		}

		function getXoffset(path){
			for(i=0;i<numberOfBars;i++)
				{
					if(endsWith(path,sources[i]))
						return i;
				}
		}
		
		function endsWith(str, suffix) {
		    return str.indexOf(suffix, str.length - suffix.length) !== -1;
		}

		
		function repaintContent(val){
			// inhalt zeichnen
			var ctx=context;
			var trunk;
			for (i = 0; i < numberOfBars; i++)
			{
				var tmpval=val[i];
				if(tmpval>maximum)
					tmpval=maximum;
				if(tmpval<0)
					tmpval=-tmpval

				tmpval=(tmpval/maximum);

				var pixelsH= parseInt(tmpval*barH);

				var end;//=endGradients[pixelsH];
				//if(end==undefined){
				end=ctx.createRadialGradient(barX+(barW+bGap)*i+r,(barY+barH-pixelsH)/scale,r/8,barX+(barW+bGap)*i+r,(barY+barH-pixelsH)/scale,r);
				if(pixelsH<low)
				{
					end.addColorStop(0,'rgba(255, 0, 0,0.5)');
					end.addColorStop(0.3,'rgba(255, 0, 0,0.5)');
					end.addColorStop(1,'rgba(255, 0, 0,0)');
					<!--// Red-->
					trunk=trunkRed[i];
				}else if(pixelsH<middle)
				{
					end.addColorStop(0,'rgba(255,150,0,0.5)');
					end.addColorStop(0.3,'rgba(255,150,0,0.5)');
					end.addColorStop(1,'rgba(255,150,0,0)');
					<!--// Yellow-->
					trunk=trunkYellow[i];
				}else
				{
					end.addColorStop(0,'rgba(0, 255, 0, 0.5)');
					end.addColorStop(0.3,'rgba(0, 255, 0, 0.5)');
					end.addColorStop(1,'rgba(0, 255, 0, 0)');
					// Green
					trunk=trunkGreen[i];
				}
				end.addColorStop(0.1,'rgba(255, 255, 255, 0.5)');
				end.addColorStop(0.5,'rgba(255, 255, 255, 0.5)');

				ctx.clearRect(barX+(barW+bGap)*i,offY,barW,barH+bottom-offY+r*scale);
				drawTrunk( barX+(barW+bGap)*i, barY+barH-pixelsH,barW,pixelsH,r,scale,trunk);
				drawEnd( barX+(barW+bGap)*i, barY+barH-pixelsH,barW,r,scale,end);
				ctx.fillStyle='black';
				ctx.textAlign='center'
				ctx.fillText(val[i]+" W", barX+(barW+bGap)*i+r, barY+barH-pixelsH-r*scale);
			}
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
