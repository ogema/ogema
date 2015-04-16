// define sevensegment class
function pic(ctx0,iX,iY,picsrc,title,toScale) {

	var miX = iX;					// X-Coordinate
	var miY = iY;					// Y-Coordinate
	var picture= new Image()
	picture.src=picsrc;	// definition of Tacho-Image
	var context0=ctx0;
	
	var zoomx=1,zoomy=1,zoomp=1,zoomi=1
	var scale
	// methods
	
	var w
	var h
	var canvas_w=context0.canvas.width
	var canvas_h=context0.canvas.height

	picture.onload=function(){
		w=picture.width
		h=picture.height
		if(w>canvas_w-miX || h>canvas_h-miY){
			var zoomx=(canvas_w-miX)/w
			var zoomy=(canvas_h-miY-100)/h

			
			if(zoomx>zoomy){
				zoomp=zoomy
			}
			else{
				zoomp =zoomx
			}
		}
		scale=zoomp
		miX=(canvas_w-w*scale)/2
		//miY=(canvas_h-100-h*scale)/2
		context0.save()
		context0.scale(scale,scale)
		context0.drawImage(picture,0,miY/scale)
		context0.restore()
	}
	
	context0.textBaseline='top'
	context0.font = '28px Arial';
	context0.textAlign='center'
	context0.fillText(title, canvas_w/2,0,canvas_w);
	zoomx=1,zoomy=1,zoomi=1
	
	}

