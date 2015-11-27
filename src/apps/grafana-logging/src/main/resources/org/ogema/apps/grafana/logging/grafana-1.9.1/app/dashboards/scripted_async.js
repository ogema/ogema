/* global _ */

/*
 * Complex scripted dashboard
 * This script generates a dashboard object that Grafana can load. It also takes a number of user
 * supplied URL parameters (int ARGS variable)
 *
 * Global accessable variables
 * window, document, $, jQuery, ARGS, moment
 *
 * Return a dashboard object, or a function
 *
 * For async scripts, return a function, this function must take a single callback function,
 * call this function with the dasboard object
 */



// accessable variables in this scope
//var window, document, ARGS, $, jQuery, moment, kbn;

return function(callback) {

	// Setup some variables
	var SERVLET_ADDRESS = "/apps/ogema/grafanalogging/fake_influxdb";
	
	var dashboard;
        
        var dropdown = document.getElementById("grafanaDropdown");
        var csvButton = document.getElementById("grafanaButton");          
            
        csvButton.onclick = function() {
            
            var resource = dropdown.options[dropdown.selectedIndex].value;
            
            var p = "p=admin"
            var q = "q=select undefined(value) from \"" + resource + "\" group by time(1s) order asc";
            var u = "u=admin";
            var query = p + "&" + q + "&" + u;
    
            $.ajax({
                method:         'GET',
                url:            SERVLET_ADDRESS + '/series?' + query,  
                contentType:    'application/json'
            }).done(function(result) {
                
                            
                var resultJSON = JSON.parse(result);
                var data = resultJSON[0].points;
                
                var dataString;
                var csvContent = "data:text/csv;charset=utf-8,";
                data.forEach(function(infoArray, index){

                    dataString = infoArray.join(";");
                    csvContent += index < data.length ? dataString+ "\n" : dataString;
                    
                });
                //download(csvContent, 'download.csv', 'text/csv');
                var encodedUri = encodeURI(csvContent);
                var fileName = resource.replace(/\//g, '_');
                var link = document.createElement("a");
                link.setAttribute("href", encodedUri);
                link.setAttribute("download", fileName + ".csv");
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            });
        
        };
           

	//define pulldowns
	var pulldowns = [
	  {
	    type: "filtering",
	    collapse: true,
	    notice: false,
	    enable: true
	  },
	  {
	    type: "annotations",
	    enable: false
	  }
	];


	// Intialize a skeleton with nothing but a rows array and service object
	dashboard = {
	    rows : [],
	    services : {}
	};

	dashboard.title = "Log Data";
	dashboard.editable = true;
	dashboard.pulldowns = pulldowns;
	//dashboard.refresh = "5s";  // set below

	var refreshBak;
	
	$.ajax({
            method: 'GET',
	    url: SERVLET_ADDRESS + '/series?parameters=',  
	    contentType: 'application/json'
	})
	.done(function(paramsResult) {
     
	   console.log("Parameter callback received ", paramsResult);
	   var params = JSON.parse(paramsResult)[0].parameters;
	   var refr = params.updateInterval;
   	   dashboard.refresh = "1s";
           
           dashboard.time = {
            from: params.frameworktimeStart,
            to: params.frameworktimeEnd
           };
           
	   if (refr > 0) {
		  // dashboard.refresh = String(refr/1000) + "s";
		  refreshBak = String(refr/1000) + "s";
	   } 
	   var rows = params.panels;
	   var panels = {};
	   var isReady = {};
	   var counter = {};
	   var span = {};
	   var panelId = 1;
	   Object.keys(rows).forEach(function(rowName) {
		 isReady[rowName] = false;
		 counter[rowName] = 0;
 //      	 console.log("Resource types/panels in row " + rowName + ": ", rows[rowName]);
         var resourceTypes = rows[rowName];
         panels[rowName] = [];
         span[rowName] = 12;
         var divisor = Object.keys(resourceTypes).length;
         if (divisor > 4) {
        	 divisor = 4;
         }
         span[rowName] = span[rowName]/divisor;

         Object.keys(resourceTypes).forEach(function(pnl) {
        // for (var ct=0;ct<resourceTypes.length;ct++) {
        	 var resType  = resourceTypes[pnl];
        	 var isarray = Array.isArray(resType);
        	 var queryParam;
        	 if (isarray) {
        		 queryParam = 'row=' + rowName + '&panel=' + pnl;
        	 } else {
        		 queryParam  = 'resourceType=' + resType;
        	 }
        	 console.log("   queryParam",queryParam);
	       	 $.ajax({
				    method: 'GET',
				    url:  SERVLET_ADDRESS + '/series?' + queryParam, 
				    contentType: 'application/json'
			  })
			  .done(function(result) {
				 var jsonResult = JSON.parse(result)[0];
				 var resources = jsonResult.loggedResources;
		//		 console.log("New resources",resources);
				 var targets = [];
				 for (var i=0;i<resources.length;i++) {
					 var trgt =  {
			              "target": "randomWalk('" + resources[i] + "')",
			              "column": "value",
			              "series": resources[i],
			              "query": "select value from \"" + resources[i] + "\""
			          };
					 targets.push(trgt);
				 }
				 if (targets.length == 0) {
					 counter[rowName] = counter[rowName] + 1;
					 return;
				 }
				 var steps = false;	// default setting if no interpolation mode provided
				 var lines = true;
        		 if (jsonResult.hasOwnProperty("interpolationMode")) {
        		 	var mode = jsonResult.interpolationMode;
        		 	if (mode === "STEPS") steps = true;
        		 	else if (mode === "NONE") lines = false; // TODO NEAREST; default: linear
        		 }
        		 
				 var panel =    
				      {
					        title: pnl,
					        type: 'graph',
					        id: panelId,
					        span: span[rowName],
					        editable: true,
					        fill: 2,
					        scale: 2,
					        y_formats: [
					          "short",
					          "short"
					        ],
					        points: true,
					        pointradius: 3,
					        linewidth: 2,
					        lines: lines,
					        targets: targets,
					        steppedLine: steps,
					        datasource: "influxdb",
					        tooltip: {
					          shared: false
					        }
				      };
				 panelId++;
				 panels[rowName].push(panel);					    
			//	 console.log("targets",targets);
				 counter[rowName] = counter[rowName] + 1;
			//	 console.log("counter(" + rowName + ") = " + String(counter[rowName]) + ". Target " + String(Object.keys(resourceTypes).length));
                          
                          });
      }); // end panels loop
	  var newRow = {
		    title: rowName,
		    height: '500px',
		    editable: true,
		    panels: panels[rowName]
	  };
	//  console.log("New row",newRow);
	  // wait until counter[rowName] == resourceTypes.length, i.e. all panels for the given row have been initialized
	  var waitCounter = 0;
	  var tick = function() {
  		waitCounter++;
  		setTimeout(function() { 
  			if (counter[rowName] == Object.keys(resourceTypes).length) {
  //			  console.log("panels[" + rowName + "]",panels[rowName]);
  			  if (newRow.panels.length == 0) {
  				  return;
  			  }
  				console.log("new row added to dashboard",newRow);
  				dashboard.rows.push(newRow);
                                  
	            for(var i = 0; i < newRow.panels[0].targets.length; i++) {
	                
	                var target = newRow.panels[0].targets[i];
	                
	                var opt = document.createElement("option");
	                opt.text = target.series;
	                opt.value = target.series;
	                dropdown.options.add(opt);
	                
	            }
  			}
  			else if (waitCounter < 100) {
  				console.log("row not yet finished... waiting another 100ms");
  				tick();
  			}
  	    }, 100);	
  	  }; 
	  tick(); 
    });  // end rows loop
    
	// make sure values are received once, then set update interval to desired value
    setTimeout(function() {
    	if (typeof(refreshBak) == "undefined") {
    		helper.set_interval();
    	}
    	else {
    		helper.set_interval(refreshBak);
  	 	}  
    }   ,2000);

    // when dashboard is composed call the callback function and pass the dashboard
    callback(dashboard);
  });
}
