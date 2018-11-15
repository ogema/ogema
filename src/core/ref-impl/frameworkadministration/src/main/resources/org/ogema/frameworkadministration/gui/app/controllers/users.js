
ngOGFrAdminApp.controller('getUserListCtrl', ['$scope', 'ogemaGateway', '$rootScope', '$filter', '$modal', function ($scope, ogemaGateway, $rootScope, $filter, $modal) {

        //var path = "userlist.json"; // /apps/ogema/frameworkadmin




        var path = "/apps/ogema/frameworkadminuser";

        $scope.newUserIsNatural = true;
        $scope.newUserName = "";
        $scope.newUserPwd = "";
		$scope.oldPwdForUser = "";

        $scope.oldUser = undefined;
        $scope.newUser = "";
        $scope.copiedUserPwd = "";

        $scope.userForNewPwd = undefined;
        $scope.newPwdForUser = "";

        $scope.msg = {
            newUser: "",
            copyUser: "",
            changePassword: ""
        };
        //$rootScope.loading++;

        $scope.initUser = function () {

            //   var path = "/install/installedapps";
            /*
             ogemaGateway.getJSON(path, {"action": "listAll"}).then(function (result) {
             //    $rootScope.loading--;
           //  console.log("BundlesCtrl: ListAll SUCCESS", result);

             $rootScope.users.apps = result;
             });
             */
            ogemaGateway.getJSON(path).then(function (result) {
                // $rootScope.loading--;
             //   console.log("getUserListCtrl SUCCESS", result);
                $scope.users = result.list;
            });

        };
        $scope.editUser = function (user) {
            $rootScope.editUsername = user;

         //   console.log("$rootScope.editUsername", $rootScope.editUsername);
            //path = "/apps/ogema/frameworkadmin"; // "userlist.json"; //

            var modalInstance = $modal.open({
                templateUrl: 'editUser',
                controller: editUserCtrl,
            });

            modalInstance.result.then(function (action) {
             //   console.log('EDIT USER dismissed at: ', action);
                $scope.initUser();
            }, function () {
              //  console.log('EDIT Modal dismissed at: ' + new Date());
            });
        };

        var editUserCtrl = function ($scope, $modalInstance) {

            $scope.permissionActions = ["read", "write", "addsub", "create", "delete", "activity"];
            $scope.newpolicy = {"accessDecision": "allow","permissionName":"org.ogema.accesscontrol.ResourcePermission","uniqueName":null, "resourcePath": "", "resourceType": null, "permissionActions": []};

            $scope.initUserData = function () {
                ogemaGateway.getJSON(path, {"action": "getUserData", "usr": $rootScope.editUsername}).then(function (result) {
                 //   console.log("getUserData", result);
                    $scope.userData = result
                });
            }

            ogemaGateway.getJSON(path, {"action": "getUserPermittedApps", "usr": $rootScope.editUsername}).then(function (result) {
             //   console.log("getUserPermittedApps", result);
                $scope.userPermittedApps = result
            });

            $scope.initUserPolicies = function () {
                ogemaGateway.getJSON(path, {"action": "getUserPolicies", "usr": $rootScope.editUsername}).then(function (result) {
                //    console.log("getUserPolicies", result);
                    $scope.userPolicies = $scope.reformatGetPolicies(result);
                });
            }

            $scope.setUserPolicies = function () {
                var sendPolicies = $scope.reformatSetPolicies($scope.userPolicies);
              //  console.log("setUserPolicies", sendPolicies);
                ogemaGateway.postData("/apps/ogema/frameworkadminuser/setPolicies", sendPolicies).then(function (result) {
               //     console.log("setUserPolicies", result);
                    $scope.initUserPolicies();
                });
            };

            $scope.reformatGetPolicies = function (obj) {
                angular.forEach(obj.resourcePermissions, function (value, index) {
                    value.permissionActions = value.permissionActions.split(",");
                });
                return obj;
            };

            $scope.reformatSetPolicies = function (obj) {
                angular.forEach(obj.resourcePermissions, function (value, index) {
                    value.permissionActions = value.permissionActions.join(",");
                });
                return obj;
            };

            $scope.addNewPolicy = function (obj) {
           //     console.log("addNewPolicy", obj);
                $scope.userPolicies.resourcePermissions.push(obj);
 //               $scope.newpolicy = {"accessDecision": "allow", "permissionName":"org.ogema.accesscontrol.ResourcePermission","uniqueName":null, "permissionPath": "path=*", "resourceType": null, "permissionActions": ["read", "write"]};
            	            $scope.newpolicy = {"accessDecision": "allow", "permissionName":"org.ogema.accesscontrol.ResourcePermission","uniqueName":null, "resourceType": null, "permissionActions": ["read", "write"]};
            };

            $scope.deletePolicy = function (item) {
                var index = $scope.userPolicies.resourcePermissions.indexOf(item)
                $scope.userPolicies.resourcePermissions.splice(index, 1);
            };


            $scope.toggleAccessDecision = function (policy) {
                policy.accessDecision = (policy.accessDecision == "allow") ? "deny" : "allow";
            }

            $scope.toggleAppPermission = function (app) {
             //   console.log("toggleAppPermission", app);
                app.permitted = !app.permitted;
                $scope.userPermittedApps.role = null;
                $scope.userPermittedApps.apps = [app];



                ogemaGateway.postData("/apps/ogema/frameworkadminuser/setPermittedApps", $scope.userPermittedApps).then(function (result) {
                    ogemaGateway.getJSON(path, {"action": "getUserPermittedApps", "usr": $rootScope.editUsername}).then(function (result) {
                     //   console.log("refreshUserPermittedApps", result);
                        $scope.userPermittedApps = result
                    });
                });
            }

            $scope.toggleAdminStatus = function (user) {
             //   console.log("toggleAdminStatus", user, user.isAdmin);
                var action = "grantAdminRights";
                //   user.isAdmin = !user.isAdmin;
                if (!user.isAdmin) {
                    action = "revokeAdminRights"
                }

                ogemaGateway.getJSON("/apps/ogema/frameworkadminuser", {"action": action, "usr": user.name}).then(function (result) {
                    $scope.initUserData();
                });
            }

            $scope.initUserData();
            $scope.initUserPolicies();

            $scope.ok = function () {
                $modalInstance.close('ok');
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        };

        $scope.addNewUser = function (user, pwd, natural) {
         //   console.log("addNewUser", user, pwd, natural);

            var path = "/apps/ogema/frameworkadminuser/createUser";

            if (user != "" && pwd != "" && natural != null) {
                ogemaGateway.postData(path, {user: user, isNatural: natural, pwd: pwd}).then(function (result) {
                //    console.log("RESULT", result)
                    $scope.initUser();
                });
            }
        }

        $scope.copyUser = function (oldUser, newUser, pwd) {
         //   console.log("copyUser", oldUser, newUser, pwd);

            var path = "/apps/ogema/frameworkadminuser/copyUser";

            if (oldUser != "" && newUser != "" && pwd != "") {
                ogemaGateway.postData(path, {userOld: oldUser, userNew: newUser, pwd: pwd}).then(function (result) {
               //     console.log("RESULT", result)
                    $scope.initUser();
                });
            }
        }

        $scope.changePassword = function (user, oldpwd, pwd) {
        //    console.log("changePassword", user, pwd);

            var path = "/apps/ogema/frameworkadminuser/changePassword";

            if (user != "" && pwd != "") {
                ogemaGateway.postData(path, {user: user, oldpwd: oldpwd, pwd: pwd}).then(function (result) {
                //    console.log("RESULT", result)
                    $scope.initUser();
                    alert("User password updated: " + user);
                }).catch(function(status) {
                    //   console.log("Error POST", status, error, result);
                	if (status === 401)
                		alert("Wrong password.");
                	else
                		alert("Password update failed: " + status);
                });
            }
        }


        $scope.deleteUser = function (user) {
            $rootScope.deleteUsername = user;
       //     console.log("$rootScope.deleteUsername", $rootScope.deleteUsername);
            var modalInstance = $modal.open({
                templateUrl: 'deleteUser',
                controller: deleteUserCtrl,
            });

            modalInstance.result.then(function (action) {
             //   console.log('DELETE USER: ', action);
                var path = "/apps/ogema/frameworkadminuser/deleteUser";
                ogemaGateway.postData(path, {user: user}).then(function (result) {
                 //   console.log("RESULT", result)
                    $scope.initUser();
                });
            }, function () {
            //    console.log('DELETE Modal dismissed at: ' + new Date());
            });
        };

		$scope.retrieveIcon = function(bundleId) {
			return '/install/installedapps?action=getIcon&app=' + bundleId + '&user=' + otusr +'&pw=' + otpwd;
		}

        var deleteUserCtrl = function ($scope, $modalInstance) {

            $scope.ok = function () {
                $modalInstance.close('ok');
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        };
        $scope.initUser();
    }]);

