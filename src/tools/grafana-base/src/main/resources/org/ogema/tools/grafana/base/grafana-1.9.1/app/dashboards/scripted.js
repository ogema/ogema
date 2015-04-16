/* global _ */

/*
 * Complex scripted dashboard
 * This script generates a dashboard object that Grafana can load. It also takes a number of user
 * supplied URL parameters (int ARGS variable)
 *
 * Return a dashboard object, or a function
 *
 * For async scripts, return a function, this function must take a single callback function as argument,
 * call this callback function with the dashboard object (look at scripted_async.js for an example)
 */



// accessable variables in this scope
var window, document, ARGS, $, jQuery, moment, kbn;

// Setup some variables
var dashboard;

// All url parameters are available via the ARGS object
var ARGS;

//define pulldowns
var pulldowns = [
  {
    type: "filtering",
    collapse: false,
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

dashboard.title = "Sensors";
dashboard.editable = true;
dashboard.pulldowns = pulldowns;
dashboard.refresh = "10s";

// Set default time
// time can be overriden in the url using from/to parameteres, but this is
// handled automatically in grafana core during dashboard initialization
dashboard.time = {
  from: "now-5m",
  to: "now"
};

var rows = 1;
var seriesName = 'argName';
console.log("ARGS",ARGS);
if(!_.isUndefined(ARGS.rows)) {
  rows = parseInt(ARGS.rows, 10);
}

if(!_.isUndefined(ARGS.name)) {
  seriesName = ARGS.name;
}
console.log("rows",rows);

var sensors = [];
/*
function getLoggedSensors() {
	$.ajax({
	    method: 'GET',
	    url: '/apps/ogema/grafanatestapp/fake_influxdb/series?q=loggedSensors'
	  })
	  .done(function(result) {
		 sensors = JSON.parse(result);
		 console.log("New sensors",sensors);
		 setTimeout(function(){
			 getLoggedSensors();			 
		 },10000);
	  });
}

getLoggedSensors();
*/


for (var i = 0; i < rows; i++) {
	
 // dashboard.rows: put one sensor type per row
 // one panel per plot
	
  dashboard.rows.push({
    title: 'Chart',
    height: '500px',
    editable: true,
    panels: [   
      {
        title: 'Events',
        type: 'graph',
        span: 12,
        editable: true,
        fill: 1,
        scale: 2,
        y_formats: [
          "short",
          "short"
        ],
        fill: 1,
        points: true,
        pointradius: 5,
        linewidth: 2,
        targets: [
          {
              "target": "randomWalk('argName')",
              "column": "value",
              "series": "testTempSens1",
              "query": "select value from \"testTempSens1\""
          },
          {
              "target": "randomWalk('argName')",
              "column": "value",
              "series": "testTempSens2",
              "query": "select value from \"testTempSens2\""
          },
          {
              "target": "randomWalk('argName')",
              "column": "value",
              "series": "testTempSens3",
              "query": "select value from \"testTempSens3\""
          },
          {
              "target": "randomWalk('argName')",
              "column": "value",
              "series": "testTempSens4",
              "query": "select value from \"testTempSens4\""
          }
        ],
       
        
        "datasource": "OgemaDatasource",
        tooltip: {
          shared: true
        }
      }
    ]
  });
}


return dashboard;
