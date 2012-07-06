'use strict';

/* Controllers */

ngOGFrGui.controller('NavigationCtrl', [ '$scope', '$location', '$rootScope',
		'ogemaGateway', '$modal', '$window',
		function($scope, $location, $rootScope, ogemaGateway, $modal, $window) {
			$scope.isActive = function(viewLocation) {
				return viewLocation === $location.path();
			};
			// console.log("LOCATION:", $location.$$port);
			$rootScope.loading = 0;
			$rootScope.rootPath = "";

			$scope.showModalHelp = function() {
				var modalInstance = $modal.open({
					templateUrl : 'modalHelp',
					controller : showModalHelpCtrl,
					size : 'lg'
				});

				modalInstance.result.then(function(action) {
					// console.log('SHOW HELP Bundle dismissed at: ', action);
				}, function() {
					// console.log('SHOW HELP Modal dismissed at: ' + new
					// Date());
				});

			}

			var showModalHelpCtrl = function($scope, $modalInstance) {
				// console.log("showModalHelpCtrl")
				$scope.ok = function() {
					// console.log("OK")
					$modalInstance.close('ok');
				};

				$scope.cancel = function() {
					// console.log("CANCEL")
					$modalInstance.dismiss('cancel');
				};
			}

			var failCb = function() {
				console.log("error");
			}

			$scope.showModalLogout = function() {
				var modalInstance = $modal.open({
					templateUrl : 'modalLogout',
					controller : showModalLogoutCtrl,
					size : 'sm'
				});

				modalInstance.result.then(function(action) {
					// console.log('SHOW LOGOUT Bundle dismissed at: ', action);
					//$(location).attr('href', "/apps/ogema/framework/gui?action=logout");
					window.location.replace("/apps/ogema/framework/gui?action=logout");
					//ogemaGateway.getJSON("/apps/ogema/framework/gui", {
					//	"action" : "logout"
					//}).then(function(result) {
						//console.log('SHOW LOGOUT Modal success');
						//$window.location.href = "/ogema/index.html";
					//}, failCb);
				}, function() {
					// console.log('SHOW LOGOUT Modal dismissed at: ' + new
					// Date());
				});

			}

			var showModalLogoutCtrl = function($scope, $modalInstance) {

				$scope.ok = function() {
					$modalInstance.close('ok');
				};

				$scope.cancel = function() {
					$modalInstance.dismiss('cancel');
				};
			}

		} ]);

ngOGFrGui.controller('BundlesCtrl', [
		'$scope',
		'ogemaGateway',
		'$rootScope',
		'$filter',
		'$modal',
		'$window',
		function($scope, ogemaGateway, $rootScope, $filter, $modal, $window) {
			$rootScope.initApplications = function() {
				// console.log("initApplications");
				ogemaGateway.getJSON("/apps/ogema/framework/gui/installedapps",
						{
							"action" : "listAll"
						}).then(function(result) {
					// console.log("BundlesCtrl: ListAll SUCCESS", result);
					$rootScope.applications = result;
					$rootScope.applications.displayButtons = false;
				});
			};

			$scope.openApp = function(bundle) {
				$window.open(bundle.webResourcePaths[0]);
			}

			$scope.showBundleInfo = function(bundle) {
				$rootScope.infoBundle = bundle;
				// console.log("$rootScope.infoBundle", $rootScope.infoBundle);

				var modalInstance = $modal.open({
					templateUrl : 'showBundleInfo',
					controller : showBundleInfoCtrl,
					size : 'lg'
				});

				modalInstance.result.then(function(action) {
					// console.log('SHOW INFO Bundle dismissed at: ', action);
				}, function() {
					// console.log('SHOW INFO Modal dismissed at: ' + new
					// Date());
				});
			};

			var showBundleInfoCtrl = function($scope, $modalInstance) {

				ogemaGateway.getJSON(path, {
					"action" : "getInfo",
					"app" : $scope.infoBundle.id
				}).then(function(result) {

					// console.log("BundlesCtrl: showBundleInfoCtrl", result);
					$scope.status = result;

				});

				$scope.ok = function() {
					$modalInstance.close('ok');
				};

				$scope.cancel = function() {
					$modalInstance.dismiss('cancel');
				};
			};

			$scope.updateBundle = function(bundle) {
				$rootScope.updateBundle = bundle;
				// console.log("$rootScope.updateBundle",
				// $rootScope.updateBundle);
				var modalInstance = $modal.open({
					templateUrl : 'updateBundle',
					controller : updateBundleCtrl,
				});

				modalInstance.result.then(function(action) {
					// console.log('UPDATE BUNDLE: ', action);
				}, function() {
					// console.log('UPDATE Modal dismissed at: ' + new Date());
				});
			};

			var updateBundleCtrl = function($scope, $modalInstance) {
				if ($scope.updateBundle != null) {
					ogemaGateway.getJSON(path, {
						"action" : "update",
						"app" : $scope.updateBundle.id
					}).then(function(result) {
						// $rootScope.loading--;
						// console.log("BundlesCtrl: updateBundleCtrl", result);

						$scope.status = result.statusInfo;

					});
				} else {
					$scope.status = "No Bundle selected";
				}
				$scope.ok = function() {
					$modalInstance.close('ok');
					$scope.updateBundle = null;
				};

				$scope.cancel = function() {
					$modalInstance.dismiss('cancel');
				};
			};

			$rootScope.initApplications();
		} ]);
