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
	var SERVLET_ADDRESS = "/apps/ogema/grafanatest/fake_influxdb";
	
	var dashboard;


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
	dashboard.title = "Schedule viewer";
	dashboard.editable = true;
	dashboard.pulldowns = pulldowns;
	//dashboard.refresh = "5s";  // set below
	dashboard.time = {
	  from: "now-5m",
	  to: "now"
	};
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
	   var restrictions = {};
	   if (params.hasOwnProperty("restrictions")) restrictions = params.restrictions;
	   console.log("restrictions is",restrictions);
	   Object.keys(rows).forEach(function(rowName) {
		 isReady[rowName] = false;
		 counter[rowName] = 0;
//       	 console.log("Resource types/panels in row " + rowName + ": ", rows[rowName]);
         var resourceTypes = rows[rowName];
         panels[rowName] = [];
         span[rowName] = 12;
         var divisor = Object.keys(resourceTypes).length;
         if (divisor > 4) {
        	 divisor = 4;
         }
         span[rowName] = span[rowName]/divisor;    
         var rowRestr = {};
         if (restrictions.hasOwnProperty(rowName)) {
         	rowRestr = restrictions[rowName];
         }
          console.log("rowRest is",rowRestr);
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
        	 if (rowRestr.hasOwnProperty(pnl)) {
        	 	queryParam = queryParam + '&restrictions=' + rowRestr[pnl]; 
        	 }
        	console.log("   queryParam",queryParam);
	       	 $.ajax({
				    method: 'GET',
				    url:  SERVLET_ADDRESS + '/series?' + queryParam, 
				    contentType: 'application/json'
			  })
			  .done(function(result) {
				 var resources = JSON.parse(result)[0].loggedResources;
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
				 var panel =    
				      {
					        id: panelId,
						 	title: pnl,
					        type: 'graph',
					        span: span[rowName],
					        editable: true,
					        fill: 2,
					        scale: 2,
					        y_formats: [
					          "short",
					          "short"
					        ],
					        points: true,
					        pointradius: 5,
					        linewidth: 2,
					        targets: targets,
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
  	//		  console.log("panels[" + rowName + "]",panels[rowName]);
  			  if (newRow.panels.length == 0) {
  				  return;
  			  }
  		//		console.log("new row added to dashboard",newRow);
  				dashboard.rows.push(newRow);
  			}
  			else if (waitCounter < 100) {
  		//		console.log("row not yet finished... waiting another 100ms");
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