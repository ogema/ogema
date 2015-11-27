ngOGFrAdminApp.controller('BundlesCtrl', ['$scope', 'ogemaGateway', '$rootScope', '$filter', '$modal', '$window', '$upload', function ($scope, ogemaGateway, $rootScope, $filter, $modal, $window, $upload) {
        var path = "/install/installedapps"+"?user="+otusr+"&pw="+otpwd;

        $rootScope.initApplications = function () {
            var path = "/install/installedapps"+"?user="+otusr+"&pw="+otpwd;

            ogemaGateway.getJSON(path, {"action": "listAll"}).then(function (result) {
                $rootScope.applications = result;
                $rootScope.applications.displayButtons = false;
            });
        };

        $scope.openApp = function (bundle) {
            $window.open(bundle.webResourcePaths[0]);
        }

        $scope.showBundleInfo = function (bundle) {
            $rootScope.infoBundle = bundle;
            var modalInstance = $modal.open({
                templateUrl: 'showBundleInfo',
                controller: showBundleInfoCtrl,
                size: 'lg'
            });

            modalInstance.result.then(function (action) {
              //  console.log('SHOW INFO Bundle dismissed at: ', action);
            }, function () {
              //  console.log('SHOW INFO Modal dismissed at: ' + new Date());
            });
        };

        var showBundleInfoCtrl = function ($scope, $modalInstance) {

            ogemaGateway.getJSON(path, {"action": "getInfo", "app": $scope.infoBundle.id}).then(function (result) {
                $scope.status = result;
            });

            $scope.ok = function () {
                $modalInstance.close('ok');
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        };


        $scope.editBundle = function (bundle) {
            $rootScope.editBundle = bundle;
            var modalInstance = $modal.open({
                templateUrl: 'editBundle',
                controller: editBundleCtrl,
                scope: $scope,
                size: 'lg'
            });

            modalInstance.result.then(function (policies) {
                var path = "/install/installedapps?action=setPermission&app=" + $rootScope.editBundle.id+"&user="+otusr+"&pw="+otpwd;
                var sendPolicies = angular.toJson({policies: policies.policies})

                ogemaGateway.postFormPolicies(path, sendPolicies).then(function (result) {
                   // console.log(result);
                });

            }, function () {
               // console.log('EDIT Modal dismissed at: ' + new Date());
            });
        };

        var editBundleCtrl = function ($scope, $modalInstance) {

            ogemaGateway.getJSON(path, {"action": "getInfo", "app": $rootScope.editBundle.id}).then(function (result) {
                $scope.status = result;
            });

            $scope.togglePolicyMode = function (policy) {
                if (policy.mode == "allow") {
                    policy.mode = "deny";
                } else if (policy.mode == "deny") {
                    policy.mode = "allow";
                }
            }
            
            /*
            $scope.toggleDeletePolicy = function (policy) {
                console.log("toggleDeletePolicy", policy)
                if(policy.delete === true){
                    policy.delete = false;
                } else {
                    policy.delete = true;
                }
            }
            
          
            $scope.toggleDeletePermission = function (pernmission) {
                console.log("deletePermission", permission)
                if(permission.delete == true){
                    permission.delete = false;
                } else {
                    permission.delete = true;
                }
            }
            */
           
            $scope.ok = function () {
                $modalInstance.close($scope.status);
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        };

        $scope.uninstallApp = function (bundle) {
            $rootScope.uninstallApp = bundle;
            var modalInstance = $modal.open({
                templateUrl: 'uninstallApp',
                controller: uninstallAppCtrl,
            });

            modalInstance.result.then(function (action) {
             //   console.log('uninstallApp: ', action);

            }, function () {
             //   console.log('uninstallApp Modal dismissed at: ' + new Date());
            });
        };

        var uninstallAppCtrl = function ($scope, $modalInstance) {

            $scope.doDelete = function () {
                if ($scope.uninstallApp != null) {
                    ogemaGateway.getJSON(path, {"action": "delete", "app": $scope.uninstallApp.id}).then(function (result) {
                        $scope.status = result.statusInfo;
                        $rootScope.initApplications();
                    });
                } else {
                    $scope.status = "No App selected";
                }
            }

            $scope.ok = function () {
                $modalInstance.close('ok');
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        };


        $scope.updateBundle = function (bundle) {
            $rootScope.updateBundle = bundle;
            var modalInstance = $modal.open({
                templateUrl: 'updateBundle',
                controller: updateBundleCtrl,
            });

            modalInstance.result.then(function (action) {
              //  console.log('UPDATE BUNDLE: ', action);
            }, function () {
              //  console.log('UPDATE Modal dismissed at: ' + new Date());
            });
        };

        var updateBundleCtrl = function ($scope, $modalInstance) {
            if ($scope.updateBundle != null) {
                ogemaGateway.getJSON(path, {"action": "update", "app": $scope.updateBundle.id}).then(function (result) {
                    $scope.status = result.statusInfo;
                });
            } else {
                $scope.status = "No Bundle selected";
            }
            $scope.ok = function () {
                $modalInstance.close('ok');
                $scope.updateBundle = null;
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        };

        // INSTALL NEW APP FROM APPSTAORE

        $scope.installAppFromAppstore = function (appstore) {
            $rootScope.AppStore = appstore;
            var modalInstance = $modal.open({
                templateUrl: 'installAppFromAppstore',
                controller: installAppFromAppstoreCtrl,
                size: 'lg',
            });

            modalInstance.result.then(function (action) {
               // console.log('installAppFromAppstore: ', action);
            }, function () {
              //  console.log('installAppFromAppstore Modal dismissed at: ' + new Date());
            });
        };

        var installAppFromAppstoreCtrl = function ($scope, $modalInstance) {
            var path = "/install/apps";
            var isOpen = false;
            $scope.uploadType = "success";
            $scope.uploadAction = true;
            $scope.uploadActionMsg = "Select File."
            $scope.uploadValue = 0;
            $scope.showProgressbar = false;
            
            $scope.initFileList = function () {
              
            ogemaGateway.getJSON(path, {"name": "localAppDirectory"}).then(function (result) {
                var apps = []
                $.each(result.apps, function (index, value) {
                    apps.push({name: value.name, iconBase64: value.iconBase64, type: value.type, permissionsOpen: false, disabled: true})
                })
                $scope.apps = apps;
            });
                
            }

            $scope.onFileSelect = function ($files) {
                //$files: an array of files selected, each file has name, size, and type.
                $scope.uploadActionMsg = "Uploading File..."
                $scope.showProgressbar = true;
                
                for (var i = 0; i < $files.length; i++) {
                    var file = $files[i];
                    $scope.upload = $upload.upload({
                        url: '/install/uploadApp', //upload.php script, node.js route, or servlet url
                        //method: 'POST' or 'PUT',
                        //headers: {'header-key': 'header-value'},
                        //withCredentials: true,
                        //data: {myObj: $scope.myModelObj},
                        file: file, // or list of files ($files) for html5 only
                        //fileName: 'doc.jpg' or ['1.jpg', '2.jpg', ...] // to modify the name of the file(s)
                        // customize file formData name ('Content-Disposition'), server side file variable name. 
                        //fileFormDataName: myFile, //or a list of names for multiple files (html5). Default is 'file' 
                        // customize how data is added to formData. See #40#issuecomment-28612000 for sample code
                        //formDataAppender: function(formData, key, val){}
                    }).progress(function (evt) {
                        $scope.uploadValue = parseInt(100.0 * evt.loaded / evt.total);
                        $scope.uploadActionMsg = $scope.uploadValue + " %";
                    }).success(function (data, status, headers, config) {
                        // file is uploaded successfully
                        $scope.showProgressbar = false;
                        $scope.uploadActionMsg = data.type + data.message;
                        $scope.initFileList();
                    });
                    //.error(...)
                    //.then(success, error, progress); 
                    // access or attach event listeners to the underlying XMLHttpRequest.
                    //.xhr(function(xhr){xhr.upload.addEventListener(...)})
                }
                /* alternative way of uploading, send the file binary with the file's content-type.
                 Could be used to upload files to CouchDB, imgur, etc... html5 FileReader is needed. 
                 It could also be used to monitor the progress of a normal http post/put request with large data*/
                // $scope.upload = $upload.http({...})  see 88#issuecomment-31366487 for sample code.
            };

            $scope.ok = function () {
                $modalInstance.close('ok');
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
            
            $scope.initFileList();
           
            $scope.setPermissions = function (appstore, filename) {
                $rootScope.permissionsForApp = filename;
                var modalInstance = $modal.open({
                    templateUrl: 'setPermissions',
                    controller: setPermissionsCtrl,
                    size: "lg"
                });

                modalInstance.result.then(function (action) {
                  //  console.log('setPermissions: ', action);
                }, function () {
                  //  console.log('setPermissions Modal dismissed at: ' + new Date());
                });

            }


            var setPermissionsCtrl = function ($scope, $modalInstance) {
                var path = "/install/app";
                var isOpen = false;
                $scope.newpermission = {action: "", condition: "", conditionArgs: [], filter: "", mode: "ALLOW", name: "", checked: true};

                $scope.initAppPermissions = function () {
                    ogemaGateway.getJSON(path, {"appstore": $rootScope.AppStore, "name": $rootScope.permissionsForApp}).then(function (result) {
                        $scope.permissions = $scope.reformatGetAppPermissions(result);
                    });
                };

                $scope.addNewPermission = function (permission) {
                    var perm = JSON.stringify(permission);
                    $scope.permissions.localePerms.push(angular.fromJson(perm));
                }

                $scope.reformatGetAppPermissions = function (appPermissions) {
                    var permissions = {localePerms: []};
                    $.each(appPermissions.localePerms, function (index, value) {
                        var perm = {};
                        if (value.charAt(0) == "(") {
                            var permArray = value.split(" ");

                            perm.name = permArray[0].substr(1, permArray[0].length);
                            perm.filter = permArray[1].substr(1, permArray[1].length - 2);
                            perm.action = permArray[2].substr(1, permArray[2].length - 3);
                            perm.condition = "";
                            perm.conditionArgs = [];

                            permissions.localePerms.push({mode: "ALLOW", name: perm.name, filter: perm.filter, action: perm.action, condition: perm.condition, conditionArgs: perm.conditionArgs, checked: false});

                            /*
                             if (perm.action.indexOf(",") > 0) {
                             var actionsArray = perm.action.split(",");
                             $.each(actionsArray, function (ind, val) {
                             permissions.localePerms.push({mode: "ALLOW", name: perm.name, filter: perm.filter, action: val, condition: "", conditionArgs: [], checked: false});
                             })
                             
                             } else {
                             permissions.localePerms.push({mode: "ALLOW", name: perm.name, filter: perm.filter, action: perm.action, condition: "", conditionArgs: [], checked: false});
                             }
                             */

                        } else {
                            perm.name = value;
                            permissions.localePerms.push({mode: "ALLOW", name: perm.name, filter: "", action: "", condition: "", conditionArgs: [], checked: false});
                        }

                    });
                    return permissions;
                };

                $scope.checkboxChanged = function (obj) {
                  //  console.log("obj, $scope.setPermissions, $scope.permissions", obj, $scope.permissions);
                }

                $scope.togglePermissionMode = function (permission) {
                  
                    if (permission.mode == "ALLOW") {
                        permission.mode = "DENY";
                    } else if (permission.mode == "DENY") {
                        permission.mode = "ALLOW";
                    }
                }

                $scope.setAppPermissions = function () {
                    var path = "/install/permissions?appstore=" + $rootScope.AppStore + "&name=" + $rootScope.permissionsForApp;
                    var permission = {localePerms: []};
                    $.each($scope.permissions.localePerms, function (index, value) {
                        if (value.checked == true || $scope.permissions.checkAll == true) {
                            permission.localePerms.push({mode: value.mode, name: value.name, filter: value.filter, action: value.action, condition: value.condition, conditionArgs: value.conditionArgs});
                        };
                    })
                    permission = {permission: JSON.stringify(permission)};
                    ogemaGateway.postForm(path, permission).then(function (result) {
                        $rootScope.initApplications();
                        $scope.ok();
                    });
                }

                $scope.ok = function () {
                    $modalInstance.close('ok');
                    $scope.updateBundle = null;
                    $rootScope.initApplications();
                };

                $scope.cancel = function () {
                    $modalInstance.dismiss('cancel');
                };

                $scope.initAppPermissions();
            };

        };
        // INSTALL NEW APP FROM FILE

        $rootScope.initApplications();
    }]);

