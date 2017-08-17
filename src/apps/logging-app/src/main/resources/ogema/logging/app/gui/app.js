angular.module('resources',[])
  .factory('Resources', function() {
    var res = [];
    return {
      getResources: function() {
        return res;
      },
      addResources: function(sns) {
        res.push(sns);
      },
      setResources: function(resources) {
        res = resources;
      },
      getResourceObject: function (resource) {
      	for (var i=0;i<res.length;i++) {
      		if (res.Location===resource) return res;
      	}
      	return {};
      }
    };
  })
    .controller('ResourcesCtrl', function($scope,$http, Resources) {

 //*********** variables declaration *****************

    	$scope.resourcesSet = Resources;
    	$scope.path = "/apps/ogema/LoggingApp".toLowerCase() + "?user=" + otusr + "&pw=" + otpwd;
    	$scope.logging = {};
    	$scope.message= '';
    	$scope.loggingInterval=30;
    	$scope.loggingTypes = ['ON_VALUE_CHANGED','FIXED_INTERVAL','ON_VALUE_UPDATE'];
    	$scope.selectedType = 'ON_VALUE_CHANGED';


 //*********** definition of functions *****************

    	function columnSort(a,b) {
    		if (a===b)
    			return 0;
    		else if (a === "Location")
    			return -1;
    		else if (b === "Location")
    			return 1;
    		else if (a === "Type")
    			return -1;
    		else if (b === "Type")
    			return 1;
    		else if (a === "active")
    			return -1;
    		else if (b === "active")
    			return 1;
    		else if (a === "value")
    			return -1;
    		else if (b === "value")
    			return 1;
    		else if (a === "logging")
    			return -1;
    		else if (b === "logging")
    			return 1;
    		else if (a.indexOf("log interval")===0)
    			return -1;
    		else if (b.indexOf("log interval")===0)
    			return 1;
    		else if (a === "logging type")
    			return -1;
    		else if (b === "logging type")
    			return 1;
    		else if (a === "Logging")
    			return -1;
    		else if (b === "Logging")
    			return 1;
    		else
    			return 0;
    	}

    	$scope.getColumns = function() {
    	  var sens = $scope.resourcesSet.getResources();
    	  var cols = [];
    	  for (var i=0;i<sens.length;i++) {
    	  	 Object.keys(sens[i]).forEach(function(prop) {
    	  	 	if (cols.indexOf(prop)<0) {
    	  	 		cols.push(prop);
    	  	 	}
    	  	 });
    	  }
    	  cols.sort(columnSort);
    	  return cols;
    	};

    	$scope.logAllowed = function(res) {
    	  if (res.hasOwnProperty('logging')) {
    	    return true;
    	  }
    	  return false;
    	};

    	$scope.isLogging = function(res) {
    		if ($scope.logging[res.Location]) return true;
    		return false;
    	};

    	$scope.getLogButtonMsg = function(resource) {
    		if ($scope.isLogging(resource)) {
    			return 'stop logging';
    		}
    		return 'log';
    	};

    	// send HTTP GET
    	$scope.getResources = function() {
    		//console.log('Sending get request...');
    		$http.get($scope.path).then(function(response) {
    			$scope.resourcesSet.setResources(response.data);
    			var res = $scope.resourcesSet.getResources();
    			for (var i=0;i<res.length;i++) {
    				$scope.logging[res[i].Location] = false;
    				if (res[i].hasOwnProperty('logging') && res[i].logging.toLowerCase()==='true') {
    					$scope.logging[res[i].Location] = true;
    				}
    			}

    			//console.log('Resources: ');
    			//console.log($scope.resourcesSet.getResources());
    		});
    	};

    	// change log settings; send HTTP POST
    	// bool: true: start logging; false: stop logging
    	$scope.recordData = function(res,bool,loggingInterval,selectedType) {
    		var msg =  {'resource' : res.Location, 'record' : !bool, 'interval':loggingInterval, 'logType' : selectedType };
    		//console.log('Posting message: ');
    		//console.log(msg);
    		$http.post($scope.path, msg).then(function(response) {
				//console.log('Post response:');
				//console.log(response.data);
				$scope.message  = response.data;
				$scope.getResources();
    		});
    	};

    	$scope.filterFn = function(column) {
    	  var bool =  column !== '$$hashKey';
    	  return bool;
    	};

    	$scope.init = function() {
    	   console.log('Initializing resources');
    	   $scope.getResources();
    	};

 //*************** init on startup ******************

    	$scope.init();
    });