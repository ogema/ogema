angular.module('switch',[])
  .factory('Switches', function() {
    var switches = {};
    var multiSwitches  = {};
    var thermostats = {};
    return {
      getSwitchesLocation: function() {
        return Object.keys(switches);
      },
      getSwitches: function() {
        return switches;
      },
      setSwitches: function(witches) {
        switches = witches;
      },
      getMSwitchesLocation: function() {
        return Object.keys(multiSwitches);
      },
      getMSwitches: function() {
        return multiSwitches;
      },
      setMSwitches: function(witches) {
        multiSwitches = witches;
      },
      getTSwitchesLocation: function() {
        return Object.keys(thermostats);
      },
      getTSwitches: function() {
        return thermostats;
      },
      setTSwitches: function(witches) {
        thermostats = witches;
      }
    };
  })
    .controller('SwitchCtrl', function($scope,$http, Switches) {
 
 //*********** variables declaration *****************
 
    	$scope.switchesSet = Switches;
    	var path = "/apps/ogema/BasicSwitchGui".toLowerCase();    	
   
 //*********** definition of functions *****************
 
 		$scope.getName = function(item) {
 			var swtches = $scope.switchesSet.getSwitches();
 			if (!swtches.hasOwnProperty(item)) return '';
 			return swtches[item].name;
 		}
 
		$scope.getValue = function(item) {
		   var swtches = $scope.switchesSet.getSwitches();
		   if (!swtches.hasOwnProperty(item)) return '';
		   return swtches[item].value;
		};
		
		$scope.getLocation = function(item) {
		   var swtches = $scope.switchesSet.getSwitches();
		   if (!swtches.hasOwnProperty(item)) return '';
		   return swtches[item].loc;
		}
		
		$scope.getDevice = function(item) {
		   var swtches = $scope.switchesSet.getSwitches();
		   if (!swtches.hasOwnProperty(item)) return '';
		   if (!swtches[item].hasOwnProperty('device')) return swtches[item].name;		   
		   return swtches[item].device.name;
		}
		
		$scope.getDeviceType = function(item) {
		   var swtches = $scope.switchesSet.getSwitches();
		   if (!swtches.hasOwnProperty(item)) return '';
		   if (!swtches[item].hasOwnProperty('device')) return swtches[item].type;		   
		   return swtches[item].device.type;
		}
		
		$scope.getSwitchBtnMsg = function(item) {
			var swtches = $scope.switchesSet.getSwitches();
			if (!swtches.hasOwnProperty(item)) return '';
			if (swtches[item].value.toLowerCase() === "on") {
				return "Switch off";
			}
			else {
				return "Switch on";
			}
		}
		
		
		$scope.toggleSwitch = function(item) {	
			var msg = {'swtch' : item};
			$http.post(path, msg).then(function(response) {
				$scope.getSwitches();
			});
		
		}
		
		$scope.toggleMSwitch = function(item, value) {	
			var msg = {'mswtch' : item, 'value' : value};
			$http.post(path, msg).then(function(response) {
				$scope.getSwitches();
			});
		}
		
		$scope.getMName = function(item) {
 			var swtches = $scope.switchesSet.getMSwitches();
 			if (!swtches.hasOwnProperty(item)) return '';
 			return swtches[item].name;
 		}
 
		$scope.getMValue = function(item) {
		   var swtches = $scope.switchesSet.getMSwitches();
		   if (!swtches.hasOwnProperty(item)) return '';
		   return swtches[item].value*100;
		};
		
		$scope.getMLocation = function(item) {
		   var swtches = $scope.switchesSet.getMSwitches();
		   if (!swtches.hasOwnProperty(item)) return '';
		   return swtches[item].loc;
		};
		
		$scope.toggleTSwitch = function(item, value) {	 
			var msg = {'thermo' : item, 'value' : value};
			$http.post(path, msg).then(function(response) {
				$scope.getSwitches();
			});
		};
		
		$scope.getTName = function(item) {
 			var swtches = $scope.switchesSet.getTSwitches();
 			if (!swtches.hasOwnProperty(item)) return '';
 			return swtches[item].name;
 		}
 
		$scope.getTValue = function(item) {
		   var swtches = $scope.switchesSet.getTSwitches();
		   if (!swtches.hasOwnProperty(item)) return '';
		   return swtches[item].value;
		};
		
		$scope.getTLocation = function(item) {
		   var swtches = $scope.switchesSet.getTSwitches();
		   if (!swtches.hasOwnProperty(item)) return '';
		   return swtches[item].loc;
		};
		
		$scope.getTCharge = function(item) {  // state of charge
		   var swtches = $scope.switchesSet.getTSwitches();
		   if (!swtches.hasOwnProperty(item)) return '';
		   return swtches[item].charge;
		};
		
		$scope.getTSetpoint = function(item) {  // temperature setpoint / °C
		   var swtches = $scope.switchesSet.getTSwitches();
		   if (!swtches.hasOwnProperty(item)) return '';
		   return swtches[item].crStp;
		};
		
		$scope.getTValve = function(item) {  // valve position [0,1]
		   var swtches = $scope.switchesSet.getTSwitches();
		   if (!swtches.hasOwnProperty(item)) return '';
		   return swtches[item].valve*100;
		};
		
		$scope.getAllowedTempValue = function(input) {
			var MIN = 10;  // degrees Celsius
			var MAX = 40;
			if (input < 0) return MIN;
			else if (input > 100) return MAX;
			return Math.round((MIN + (MAX-MIN)*input/100)*10)/10;
		};
		
		function getInverseSetpoint(temp) {
			var MIN = 10;  // degrees Celsius
			var MAX = 40; 
			if (temp > MAX) return 100;
			else if (temp < MIN) return 0;
			return 100 * (temp-MIN) / (MAX-MIN);
		}
  
    	// send HTTP GET
    	$scope.getSwitches = function() {
    		//console.log('Sending get request...');
    		$http.get(path).then(function(response) {
    			$scope.switchesSet.setSwitches(response.data.switches);  
    			$scope.switchesSet.setMSwitches(response.data.multiswitches);
    			$scope.switchesSet.setTSwitches(response.data.thermostats);
    			$scope.value = {};
    			$scope.Tvalue = {};
    			Object.keys(response.data.multiswitches).forEach(function (path) {
    				try {
    					$scope.value[path] = response.data.multiswitches[path].value*100;
    				} catch  (e) {}
    			});
    			Object.keys(response.data.thermostats).forEach(function (path) {
    				try {
    					console.log("Trying to calculate setpoint for ",response.data.thermostats[path]);
    					$scope.Tvalue[path] = getInverseSetpoint(response.data.thermostats[path].crStp);
    					console.log("Value,Setpoint",response.data.thermostats[path].crStp,$scope.Tvalue[path]);
    				} catch  (e) {
    				    console.log("Exception",e);
    				}
    			});
    			console.log('Switches:',$scope.switchesSet.getSwitches());	    		
    		});
    	};
    	
    		
    	$scope.init = function() {
    		$scope.getSwitches();
    	};
    	
 //*************** init on startup ******************
  	
    	$scope.init();
    });