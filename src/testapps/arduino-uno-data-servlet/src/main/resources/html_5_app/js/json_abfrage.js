// JavaScript Document

	
		
		
			
		
			$(document).everyTime(3000, function() {
	
				   $.getJSON("/climate_station_servlet/getArduinoData", function(data) {
				    	
					   for(i = 0; i < data.length; i++) {		 
				    		
				    		 if(i == 0){
				    			 
				    			 ID_out = data[i].ID;
				    			 RTemp = data[i].RTemp;
				    			 RH_out = data[i].RH;
				    			 Location_ID_out = data[i].Location_ID; 
				    		 } else {
				    			 
				    			 ID = data[i].ID;
				    			 RTemp = data[i].RTemp;
				    			 RH = data[i].RH;
				    			 Location_ID = data[i].Location_ID; 
				    			 Message_ID = data[i].Message_ID; 
				    			 Priority = data[i].Priority; 
				    		 }
					   }
				   });
			});
		
