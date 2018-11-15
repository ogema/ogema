'use strict';

/* Services */

ngOGFrAdminApp.service('ogemaGateway', ['$http', '$q', '$rootScope', '$filter', function($http, $q, $rootScope, $filter) {

       this.getJSON = function(path, data, config) {
            path = $rootScope.rootPath+appendOtpToPath(path);
            var deferred = $q.defer();
            $http({
                method: 'GET',
                url: path,
                params: data,
                //  headers : { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' }
            }).success(function(result, status) {
                deferred.resolve(result);

            }).error(function(status, error, result) {
               // console.log("Error calling LIST", status, error, result);
                deferred.reject();

                //deferred.resolve();
            });
            return deferred.promise;
        };


        this.postForm = function(path, data, config) {
            path = $rootScope.rootPath+appendOtpToPath(path);
          //  console.log("SERVICE postForm: path, data, config: ", path, data, config);
            var deferred = $q.defer();
            var formdata = $.param(data);

            $http({
                method: 'POST',
                url: path,
                data: formdata, // {permission: data}
                headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}
            })
                    .success(function(data) {
                      //  console.log(data);
                        deferred.resolve(data);
                    }).error(function(status, error, result) {
                        // console.log("Error POST", status, error, result);

                deferred.reject();
                //deferred.resolve();
            });
            ;
            return deferred.promise;
        };

        this.postFormPolicies = function(path, data, config) {
            path = $rootScope.rootPath+appendOtpToPath(path);
            var deferred = $q.defer();
            $http({
                method: 'POST',
                url: path,
                data: $.param({settings: data}), // {permission: data}
                headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}
            })
                    .success(function(data) {
                     //   console.log(data);
                        deferred.resolve(data);
                    }).error(function(status, error, result) {
                      //   console.log("Error POST", status, error, result);

                deferred.reject();
                //deferred.resolve();
            });
            ;
            return deferred.promise;
        };

  /**
   * The workhorse; converts an object to x-www-form-urlencoded serialization.
   * @param {Object} obj
   * @return {String}
   */
  var param = function(obj) {
    var query = '', name, value, fullSubName, subName, subValue, innerObj, i;

    for(name in obj) {
      value = obj[name];

      if(value instanceof Array) {
        for(i=0; i<value.length; ++i) {
          subValue = value[i];
          fullSubName = name + '[' + i + ']';
          innerObj = {};
          innerObj[fullSubName] = subValue;
          query += param(innerObj) + '&';
        }
      }
      else if(value instanceof Object) {
        for(subName in value) {
          subValue = value[subName];
          fullSubName = name + '[' + subName + ']';
          innerObj = {};
          innerObj[fullSubName] = subValue;
          query += param(innerObj) + '&';
        }
      }
      else if(value !== undefined && value !== null)
        query += encodeURIComponent(name) + '=' + encodeURIComponent(value) + '&';
    }

    return query.length ? query.substr(0, query.length - 1) : query;
  };


        this.postData = function(path, data, config) {
            path = $rootScope.rootPath+appendOtpToPath(path);
           // console.log("SERVICE postData: path, data, config: ", path, data, config);
            var deferred = $q.defer();
            var formdata = $filter('json')(data); // $.param(data);
          //  console.log("FORMDATA:", formdata);
            $http({
                method: 'POST',
                url: path,
                data: formdata,
                headers: {'Content-Type': 'application/json; charset=UTF-8'} // application/json // application/x-www-form-urlencoded
            })
                    .success(function(data) {
                     //   console.log(data);
                        deferred.resolve(data);
                    }).error(function(html, status, error, result) {
                      //   console.log("Error POST", status, error, result);

                deferred.reject(status);
                //deferred.resolve();
            });
            ;
            return deferred.promise;
        };


        this.getAllResourcesList = function(path, config) {

            // console.log("SERVICE getAllResources: path: ", path);
            var deferred = $q.defer();

            $http.get(path, config).
                    success(function(result, status) {
                     //   console.log(result)
                        var resources = result.subresources;
                        /* angular.forEach(resources, function(element, key) {

                            resolveResourcelink(element, path).then(function(result) {
                                if (result != undefined) {
                                    //console.log("resolved: ",result);
                                    resources[key] = result;
                                }
                            }, function(error) {
                                console.log("ERROR: ", error)
                            })
                        })*/
                        deferred.resolve(resources);
                    }).
                    error(function(status, error, result) {
                        //console.log("Error calling LIST", status, error, result);

                        deferred.reject();
                        //deferred.resolve();
                    });
            return deferred.promise;
        };


        this.getAllResources = function(path, config) {

            console.log("SERVICE getAllResources: path: ", path);
            var deferred = $q.defer();

            $http.get(path, config).
                    success(function(result, status) {
                      //  config.log("getAllResources:", result);
                        var deferred = $q.defer();
                        var promise = deferred.promise;
                        promise.then(function() {
                       //     console.log("SERVICE getAllResources: result: ", result);
                            angular.forEach(result.subresources, function(element, key) {
                                element = getResource(path + element.resourcelink.link);
                            })
                        }).then(function() {
                            deferred.resolve(result);
                        }, function(error) {
                        //    console.log("SERVICE getAllResources: ERROR: ", error);
                            deferred.reject();
                        });
                        deferred.resolve(result);
                    }).
                    error(function(status, error, result) {
                        //console.log("Error calling LIST", status, error, result);

                        deferred.reject();
                        //deferred.resolve();
                    });
            return deferred.promise;
        };

        function getResource(path, config) {

            // console.log("SERVICE getResource: path: ", path);
            var deferred = $q.defer();

            $http.get(path, config).
                    success(function(result, status) {
                        deferred.resolve(result);
                    }).
                    error(function(status, error, result) {
                        //console.log("Error calling Resource", status, error, result);
                        // deferred.reject();
                        deferred.resolve();
                    });
            return deferred.promise;
        }
        ;

        function resolveResourcelink(element, path, config) {
            if (typeof element.resourcelink != undefined) {

                path = path + element.resourcelink.link;
               //  console.log("SERVICE getResource: path: ", path);
                var deferred = $q.defer();

                $http.get(path, config).
                        success(function(result, status) {
                            deferred.resolve(unifyResult(result)); // organizeList || unifyResult ?
                        }).
                        error(function(status, error, result) {
                            //console.log("Error calling Resource", status, error, result);
                            // deferred.reject();
                            deferred.resolve();
                        });
                return deferred.promise;
            } else {
                return element
            }
        }
        ;

        function appendOtpToPath(path) {
        	if (path.indexOf("&pw=") > 0)
        		return path;
        	var separator = (path.indexOf('/') > 0) ? "&" : "?";
        	return path + separator + "user=" + otusr +  "&pw=" + otpwd;
        }

        function organizeList(list) {

            var result = list
            //    console.log("ORGANIZE LIST: ", list);

            angular.forEach(result, function(value, key) {
                // console.log("ORGANIZE LIST: ",value, key);
                switch (key) {
                    case "subresources":
                        angular.forEach(value, function(val, ky) {
                            //     value.push(unifyResult(val));
                            value[ky] = unifySubresource(val);
                        });
                        break;
                }
            });

            // console.log(result);
            return result;
        }
        ;

        function unifyResult(obj) {

            // console.log("unifyResult: ",obj);
            angular.forEach(obj, function(value, key) {
                if (typeof value == "object" && value !== null)
                {
                    //        console.log("key == ", key);

                    switch (key) {
                        case "subresources":
                            if (value.length > 0) {
                                //   console.log("Subresources: ", value)
                                angular.forEach(value, function(val, ky) {
                                    //      console.log("processing subresource: ", ky, val );
                                    angular.forEach(val, function(v, k) {
                                        value[ky] = v;
                                    });
                                });
                            }
                            ;
                            break;
                        case "resourcelink":
                       //     console.log("resourcelink: ", value);
                            value.name = "Resourcelink";
                            value.value = value.resourcelink.name;
                            break;
                    }



                    //  value[key]=unifySubresource(value);

                    unifyResult(value);

                } else {
                    // do something...

                }

            });
            return obj;
        }


        this.getObject = function(obj, key, val) {
            var objects = [];
            for (var i in obj) {
                if (!obj.hasOwnProperty(i))
                    continue;
                if (typeof obj[i] === 'object') {
                    objects = objects.concat(this.getObject(obj[i], key, val));
                } else
                //if key matches and value matches or if key matches and value is not passed (eliminating the case where key matches but passed value does not)
                if (i === key && obj[i] === val || i === key && val === '') { //
                    if (obj.path.indexOf("location") === -1)
                        objects.push(obj);
                } else if (obj[i] === val && key === '') {
                    //only add if the object is not already in the array
                    if (objects.lastIndexOf(obj) === -1) {
                        objects.push(obj);
                    }
                }
            }
            return objects;
        };

        this.getObjectValue = function(obj, prop, val) {
            var result = this.getObject(obj, prop, val);
            if (result.length === 0) {
                result = "";
            } else {
                result = result[0].value;
            }
            return result;
        };

        this.clone = function(obj) {
            if (null === obj || "object" !== typeof obj)
                return obj;
            var copy = obj.constructor();
            for (var attr in obj) {
                if (obj.hasOwnProperty(attr))
                    copy[attr] = obj[attr];
            }
            return copy;
        };
    }]);