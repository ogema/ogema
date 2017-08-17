angular.module('ResourceFlashModule',[])
.factory('ResourceTypes', function() {
	var types = {};
	var classes = {};
	var schedules = {};
	var lastDuration = -1;
	var treeNumber = 0;
	return {
		  getTypes: function() {
			  return types;
		  },
		  setTypes: function(typesIn) {
			  types = typesIn;
		  },
		  getClasses: function() {
			  return classes;
		  },
		  setClasses: function(classesIn) {
			  classes = classesIn;
		  },
		  getSchedules: function() {
			  return schedules;
		  },
		  setSchedules: function(schedulesIn) {
			  schedules = schedulesIn;
		  },
		  setLastDuration: function(duration) {
			  lastDuration = duration;
		  },
		  getLastDuration: function() {
			  return lastDuration;
		  },
		  getTreeNumber: function() {
			  return treeNumber;
		  },
		  setTreeNumber: function(nr) {
			  treeNumber = nr;
		  }
	};
})
.controller('ResourceFlashCtrl', function($scope,$http, ResourceTypes) {
//*********** variables declaration *****************
	var path = "/org/ogema/tests/servlets/resourceflash";
	path = path.toLowerCase() + "?user=" + otusr + "&pw=" + otpwd;
	var factory = ResourceTypes;
	$scope.numbers = [1,10,100,1000,10000,100000];
	$scope.selectedNr = 100;
	function triggerAlert(msg) {
		console.log(msg);
		// TODO
	}
	$scope.update = function() {
		$http.get(path).then(function(response) {
			factory.setTypes(response.data.resourceTypes);
			factory.setClasses(response.data.classes);
			factory.setSchedules(response.data.schedules);
			factory.setTreeNumber(response.data.treeNr);
		});
	};
	$scope.create = function(type,nr) {
		var msg = {};
		msg.nr = nr;
		msg.type = type;
		msg.action = "create";
		$http.post(path,msg).then(function(response) {
			var result;
			if (isString(response.data))
				result = JSON.parse(response.data);
			else
				result = response.data;
			if (result.hasOwnProperty("error")) {
				triggerAlert(type + " not found.");
			}
			else {
				var duration = -1;
				if (result.hasOwnProperty("duration"))
					duration = result.duration;
				factory.setLastDuration(duration);
				$scope.update();
			}
		});
	};

	$scope.createTree = function(nr, withListeners) {
		var type = "complextree";
		if (withListeners)
			type = "complextreewithlisteners";
		$scope.create(type,nr);
	};

	$scope.deleteTree = function() {
		$scope.delete("complextree");
	};

	$scope.delete = function(type) {
		var msg = {};
		msg.type = type;
		msg.action = "delete";
		$http.post(path,msg).then(function(response) {
			var result;
			if (isString(response.data))
				result = JSON.parse(response.data);
			else
				result = response.data;
			var duration = -1;
			if (result.hasOwnProperty("duration"))
				duration = result.duration;
			factory.setLastDuration(duration);
			$scope.update();
		});
	};

	// Resources
	$scope.getResourceTypes = function() {
		return Object.keys(factory.getTypes());
	};
	$scope.getNumber = function(type) {
		var tps = factory.getTypes();
		if (!tps.hasOwnProperty(type) || !tps[type].hasOwnProperty("nr")) {
			return 0;
		}
		return tps[type].nr;
	};
	$scope.getTreeNumber = function() {
		return factory.getTreeNumber();
	};

	$scope.getShortTypeName = function(type) {
		var tps = factory.getTypes();
		if (!tps.hasOwnProperty(type) || !tps[type].hasOwnProperty("short")) {
			return type;
		}
		return tps[type].short;
	};
	$scope.getLastDuration = function() {
		return factory.getLastDuration();
	};

	// Objects
	$scope.getClasses = function() {
		return Object.keys(factory.getClasses());
	};
	$scope.getObjNumber = function(clazz) {
		var tps = factory.getClasses();
		if (!tps.hasOwnProperty(clazz) || !tps[clazz].hasOwnProperty("nr")) {
			return 0;
		}
		return tps[clazz].nr;
	};
	$scope.getShortClassName = function(clazz) {
		var tps = factory.getClasses();
		if (!tps.hasOwnProperty(clazz) || !tps[clazz].hasOwnProperty("short")) {
			return clazz;
		}
		return tps[clazz].short;
	};

   // Schedules
	$scope.getScheduleTypes = function() {
		return Object.keys(factory.getSchedules());
	};
	$scope.getValNumber = function(type) {
		var tps = factory.getSchedules();
		if (!tps.hasOwnProperty(type) || !tps[type].hasOwnProperty("nr")) {
			return 0;
		}
		return tps[type].nr;
	};
	$scope.getShortScheduleName = function(type) {
		var tps = factory.getSchedules();
		if (!tps.hasOwnProperty(type) || !tps[type].hasOwnProperty("short")) {
			return type;
		}
		return tps[type].short;
	};

	$scope.update();  // init on startup
});

function isString(o) {
    return typeof o === "string" || (typeof o === "object" && o.constructor === String);
}