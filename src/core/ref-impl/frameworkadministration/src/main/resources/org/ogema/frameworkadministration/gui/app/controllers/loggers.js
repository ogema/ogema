ngOGFrAdminApp.controller('getLoggersCtrl', ['$scope', 'ogemaGateway', '$rootScope', '$filter', '$modal',function($scope, ogemaGateway, $rootScope, $filter, $modal) {

        var path = "/apps/ogema/frameworkadmin";

        $rootScope.loading++;
        
        $scope.currentPage = 1;
        $scope.itemsPerPage = 20;
        
        ogemaGateway.getJSON(path).then(function(result) {
            $rootScope.loading--;
          //  console.log("getLoggersCtrl SUCCESS", result);

            $scope.logFilePath = result.path;
            $scope.sizeFile = result.sizeFile;
            $scope.sizeCache = result.sizeCache;
            $scope.loggers = result.list;
            $scope.bulk = {file: "NO CHANGE", cache: "NO CHANGE", console: "NO CHANGE"};

            $scope.logLevels = [
                {value: "TRACE", text: "TRACE"},
                {value: "DEBUG", text: "DEBUG"},
                {value: "INFO", text: "INFO"},
                {value: "WARNING", text: "WARNING"},
                {value: "ERROR", text: "ERROR"},
                {value: "NO_LOGGING", text: "NO_LOGGING"}
            ];

            $scope.logLevelsBulk = [
                {value: "NO CHANGE", text: "NO CHANGE"},
                {value: "TRACE", text: "TRACE"},
                {value: "DEBUG", text: "DEBUG"},
                {value: "INFO", text: "INFO"},
                {value: "WARNING", text: "WARNING"},
                {value: "ERROR", text: "ERROR"},
                {value: "NO_LOGGING", text: "NO_LOGGING"}
            ];

/*

              $scope.numPages = function () {
                 return Math.ceil($scope.todos.length / $scope.numPerPage);
                };
  
  $scope.$watch('currentPage + numPerPage', function() {
    var begin = (($scope.currentPage - 1) * $scope.numPerPage)
    , end = begin + $scope.numPerPage;
    
    $scope.filteredLoggers = $scope.loggers.slice(begin, end);
  });
*/
            $scope.sendBulkChange = function() {
                // console.log("filter", $scope.logFilter);
                // console.log("bulk", $scope.bulk);
                // console.log("loggers", $scope.loggers);

                var filtered = $filter('filter')($scope.loggers, $scope.logFilter);
                // console.log("filtered", filtered);

                if (filtered.length > 0 && ($scope.bulk.file != "NO CHANGE" || $scope.bulk.cache != "NO CHANGE" || $scope.bulk.console != "NO CHANGE")) {
                    $.each(filtered, function(index, value) {
                        // console.log(value);
                        if ($scope.bulk.file != "NO CHANGE") {
                            value.file = $scope.bulk.file;
                        }
                        if ($scope.bulk.cache != "NO CHANGE") {
                            value.cache = $scope.bulk.cache;
                        }
                        if ($scope.bulk.console != "NO CHANGE") {
                            value.console = $scope.bulk.console;
                        }
                    });
                ogemaGateway.postData(path, {action: "bulkChange", elements:filtered}).then(function(result) {
                 //   console.log(result);
                });

                }

            }

            $scope.postSingleValue = function(name, type, data) {
                var path = "/apps/ogema/frameworkadmin";
                var doAction = "singleChange";
                if(type == "value"){
                    doAction = "sizeChange";
                }
            //    console.log(name, type, data);

                var postValue = {"name": name};
                postValue[type] = data;
            //    console.log(postValue);
                
                var postValueArray = [postValue];
                
             //   console.log($filter('json')(postValueArray));
                ogemaGateway.postData(path, {action: doAction, elements:postValueArray}).then(function(result) {
                  //  console.log(result);
                    if(name == "sizeFile") {
                        $scope.sizeFile = result;
                    } else if(name == "sizeCache") {
                        $scope.sizeCache = result;
                    }
                });


                // console.log($filter('json')($scope.loggers));
                // return $http.post('/updateUser', {id: $scope.user.id, name: data});
            };


        });
        
         $scope.showCache = function() {
            
         //   console.log("SHOW CACHE");
            var modalInstance = $modal.open({
                templateUrl: 'showCache',
                controller: showCacheCtrl,
                size: 'lg'
            });

            modalInstance.result.then(function(action) {
            //    console.log('showCache Bundle dismissed at: ', action);
            }, function() {
             //   console.log('showCache Modal dismissed at: ' + new Date());
            });
        };

        var showCacheCtrl = function($scope, $modalInstance) {
            
             var path = "/apps/ogema/frameworkadmin";
            $scope.loadCache = function(){
            ogemaGateway.postData(path, {"action": "getCache"}).then(function(result) {
             //   console.log("showCache", result);
                $scope.showCache = result.join();
            });
            }
            
            $scope.ok = function() {
                $modalInstance.close('ok');
            };

            $scope.loadCache();
        };
    }]);

