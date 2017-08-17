/* Creates JSONAdapter object and provides functionality to update and access data retrieved from the URLArray */
function JSONAdapter() {
	
  this.HashmapSize = 0;
  this.keyServletURLArray = new Array(); 
  /* hash map  */
  this.store = [];
  
  this.setKeyServletURLArray = function(keyServletURLArray) {
	  this.keyServletURLArray = keyServletURLArray;
	  
		 for(i = 0; i < this.keyServletURLArray.length; i=i+2) 
		 {
			 if(!this.store[this.keyServletURLArray[i]]) this.store[this.keyServletURLArray[i]] = new Array();
		 }
  } 
   
  this.setHashmapSize = function(max_hashmap_size) {
	 this.HashmapSize = max_hashmap_size; 
  } 
  
  /* Internal method to store a value "value" extracted from the data in a hashmap. This value is accessible by the key "key". 
   * If the hashmap already contains the "key", the value "value" is appended to the list of existing values. */
  this.put = function(key, value) {
	  if(this.store[key].length < this.HashmapSize) {this.store[key].push(value);}
	  else { this.store[key].shift(); 
	  		 this.store[key].push(value);
	  	   }
  }
  
  /* Public method to retrieve a list of values that are stored under the key "key". By providing a start and end 
   * index you can retrieve a sublist of values. All values are multiplied on-the-fly with the scale factor "scale". For example: by providing a 
   * scale of 1000 you can convert values from ampere to milliampere */
  this.get = function(key, start, end, scale) {     /*  ["wind", null, 1000, 1.0]  */
    
	  var returnArray = [];
	  var data = this.store[key]; 
	    
	  if(data != null && data.length > 0) 
			   {
				/* Build returnArray of this form: [[1, value1 * scale)], [2, value2 * scale)], ...]*/
				for(i = 1; i <= data.length; i++) 
				{
					returnArray.push([i, data[i - 1] * scale]);
				}
			}
	  return returnArray;
		
  }
  
  /* get the last entry in the hash map for keyword key */
 this.getLast = function(key) {
	 
	 return this.store[key][this.store[key].length -1];
  }
 

  /* jQuery AJAX Public method that is called to poll data (in this case we expect the data to be JSON-formatted) from the specified url. 
   * The data is parsed and the extracted values are stored under their respective key in the hashmap. */
  this.update = function() {
	var that = this; 
	
	 $.getJSON(this.keyServletURLArray[1], function(data) 
			   {
	    		 if(data[that.keyServletURLArray[0]] != null) 
	    		 {
	    			 that.put(that.keyServletURLArray[0], data[that.keyServletURLArray[0]]);
	    		 }	
			   });
	 
	//  $.getJSON("/drs485de/getMeterList", function(data) 
	// 		   {
	   
	// 		   });
	 
	 
	 
	 
	//  $.getJSON("/rest/resources/AcuDC243_I/currentSensor/reading?user=rest&pw=rest&depth=100" , function(data) 
	// 		   {
		
	// 		   });
	 
//	 $.getJSON("/rest/recordeddata/DRS485DE_0/energyReading/1422543600000/1422546000000?user=rest&pw=rest&interval=60000&mode=AVERAGE" , 
//			 function(data) 
//			   {
//		
//			   });
	
	
//	 for(i = 0; i < this.keyServletURLArray.length; i = i+2)
//	  {	 
//		   $.getJSON(this.keyServletURLArray[i+1], function(data) 
//				   {
//		    		 if(data[that.keyServletURLArray[i]] != null) 
//		    		 {
//		    			 that.put(that.keyServletURLArray[i], data.(that.keyServletURLArray[i]));
//		    		 }	
//				   });
//	  }
  }
}
