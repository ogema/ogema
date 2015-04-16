// define sevensegment class
function tacho(ctx0,ctx1,iValueMin, iValueMax, iX, iY,picsrc,iconsrc,title) {

	//elements
	var mValueMin = iValueMin;		// Displayed Min-Value
	var mValueMax = iValueMax;		// Displayed Min-Value
	var value
	
	var miX = iX;					// X-Coordinate
	var miY = iY;					// Y-Coordinate
	var instrument = new Image()
	instrument.src=picsrc;	// definition of Tacho-Image
	icon=new Image()
	
	var oldValue=0;
	var inited=false
	this.inited=function(){return inited};
	var context0=ctx0;
	var context1=ctx1;
	
	var zoomx=1,zoomy=1,zoomp=1,zoomi=1
	var scale
	// methods
	this.setValue = function(val){
		value=val
	}
	this.animate=animate;
	this.redrawInstrument=redraw;
	this.drawAll=drawValueWithImage;
	var maxArg=Math.PI*0.78 //(180-40)/180;		// Displayed Max-Value
	
	var w
	var h
	var canvas_w=context0.canvas.width
	var canvas_h=context0.canvas.height
	var top=30;
	var bottom=100
	var left
	
	var tempValue = 0;
	var stepTime = 30;

	instrument.onload=function(){
		w=instrument.width
		h=instrument.height
	//	if(w>canvas_w || h>canvas_h){
			var zoomx=(canvas_w)/w
			var zoomy=(canvas_h-top-bottom)/h

			
			if(zoomx>zoomy){
				zoomp=zoomy
			}
			else{
				zoomp =zoomx
			}
		//}
		scale=zoomp
		left=(canvas_w-w*scale)/2
		context0.save()
		context0.scale(scale,scale)
		context0.drawImage(instrument,left,top/scale)
		context0.restore()
		icon.src=iconsrc
	}
	
	context0.textBaseline='top'
	context0.font = '28px Arial';
	context0.textAlign='center'
	context0.fillText(title, canvas_w/2,0,canvas_w);
	zoomx=1,zoomy=1,zoomi=1
	
	if(iconsrc!=undefined)
		{

			icon.onload=function (){
				var iconw=icon.width
				var iconh=icon.height
				if(iconw>canvas_w)
					zoomx=canvas_w/iconw
				if(iconh>bottom)
					zoomy=bottom/iconh
				if(zoomy<=zoomx)
					zoomi=zoomy
				else
					zoomi=zoomx
			context0.save()
			context0.scale(zoomi,zoomi)
			//context0.drawImage(icon,((canvas_w-iconw*zoomi)/2)/zoomi,(h*scale+top+(bottom-iconh*zoomi)/2)/zoomi)
			context0.drawImage(icon,0,(h*scale+top+(bottom-iconh*zoomi)/2)/zoomi)
			context0.restore()
			go()
			}
		}
	
	function getRadian(iValue,maxValue)
	{
		return (iValue*maxArg/maxValue-maxArg/2);
	}
	function drawValue(iValue) {
		context1.clearRect(0,0,context1.canvas.width,context1.canvas.height);
		context1.fillStyle = "rgb(250,100,10)";  // color is orange
		context1.save();
		context1.scale(scale,scale)
		context1.translate(300+iX+left, 350+(iY+top)/scale);
		context1.rotate(getRadian(iValue,mValueMax));
		context1.beginPath();
		context1.moveTo(-10, -70);
		context1.lineTo(-5, -230);
		context1.lineTo(+5, -230);		
		context1.lineTo(10, -70);
		context1.lineTo(-10, -70);
		context1.fill();
		context1.restore();
	
	}
	
	function drawValueWithImage(iValue, consumer) {
		 context0.drawImage(instrument, 0, 0);
		context1.fillStyle = "rgb(250,100,10)";  // color is orange
		context1.save();
		context1.translate(300+iX, 350+iY);
		context1.rotate(getRadian(iValue,mValueMax));
		context1.beginPath();
		context1.moveTo(-10, -70);
		context1.lineTo(-5, -230);
		context1.lineTo(+5, -230);		
		context1.lineTo(10, -70);
		context1.lineTo(-10, -70);
		context1.fill();
		context1.restore();
	
	}

	function animate(iValue) {
	//	context0.drawImage(TachoDisp, miX, miY);
		var step = (iValue - oldValue) / 10;
		if(step<0)
			step=-step;
		if(iValue>oldValue) {
			tempValue+=step;
			if(iValue >= tempValue) {
				drawValue(tempValue);
			//	var val1=parseInt(tempValue);
				//SegmentObject.setValue(context1, val1);
				//decSegment.setDecValue(context1, (tempValue-val1)*100);
				return setTimeout(function() {animate(iValue);}, stepTime);
			}
		}
		else {
			tempValue-=step;
			if(iValue <= tempValue) {
				drawValue(tempValue);
				//var val1=parseInt(tempValue);
			//	SegmentObject.setValue(context1, val1);
			//	decSegment.setDecValue(context1, (tempValue-val1)*100);
				return setTimeout(function() {animate(iValue);}, stepTime);
			}
		}
//		if(!inited)
//			drawValue(0);
		oldValue=iValue;
		tempValue = oldValue;
	}

	function animate2() {
		if(oldValue-value>1){
			oldValue-=1
			drawValue(oldValue)
		}else if(oldValue-value<-1){
			oldValue+=1
			drawValue(oldValue)
		}else if(!inited){
			drawValue(0)
				inited=true;
			}
	}

	function go() {
		var timer=setInterval(animate2,stepTime)
	}

	function redraw() {
	context0.beginPath();
	context0.moveTo(0,0);
	context0.lineTo(500,500);
	context0.stroke();
	context0.drawImage(instrument, miX, miY);
	}
}

