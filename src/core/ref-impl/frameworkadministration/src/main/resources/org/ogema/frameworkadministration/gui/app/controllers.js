'use strict';

/* Controllers */

ngOGFrAdminApp.controller('NavigationCtrl', ['$scope', '$location', '$rootScope', 'ogemaGateway', '$modal', '$window', function ($scope, $location, $rootScope, ogemaGateway, $modal, $window) {
	$scope.isActive = function (viewLocation) {
		return viewLocation === $location.path();
	};
	//  console.log("LOCATION:", $location.$$port);
	$rootScope.loading = 0;
	$rootScope.rootPath = "";


	$scope.showModalHelp = function () {
		var modalInstance = $modal.open({
			templateUrl: 'modalHelp',
			controller: showModalHelpCtrl,
			size: 'lg'
		});

		modalInstance.result.then(function (action) {
			//  console.log('SHOW HELP Bundle dismissed at: ', action);
		}, function () {
			//  console.log('SHOW HELP Modal dismissed at: ' + new Date());
		});


	}

	var showModalHelpCtrl = function ($scope, $modalInstance) {
		//    console.log("showModalHelpCtrl")
		$scope.ok = function () {
			//    console.log("OK")
			$modalInstance.close('ok');
		};

		$scope.cancel = function () {
			//   console.log("CANCEL")
			$modalInstance.dismiss('cancel');
		};
	}

	$scope.showModalLogout = function () {
		var modalInstance = $modal.open({
			templateUrl: 'modalLogout',
			controller: showModalLogoutCtrl,
			size: 'sm'
		});

		modalInstance.result.then(function (action) {
			//    console.log('SHOW LOGOUT Bundle dismissed at: ', action);
			ogemaGateway.getJSON("/apps/ogema/framework/gui", {"action": "logout"}).then(function (result) {
				$window.location.href = "/ogema/index.html";
			});
		}, function () {
			//  console.log('SHOW LOGOUT Modal dismissed at: ' + new Date());
		});


	}

	var showModalLogoutCtrl = function ($scope, $modalInstance) {

		$scope.ok = function () {
			$modalInstance.close('ok');
		};

		$scope.cancel = function () {
			$modalInstance.dismiss('cancel');
		};
	}

}]);

ngOGFrAdminApp.controller('UserAdministrationController', function ($scope) {
	  $scope.oneAtATime = true;

	  $scope.status = {
	    isFirstOpen: true,
	    isFirstDisabled: false
	  };
	});