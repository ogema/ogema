

angular.module('appStoreApp', []).controller('AppStoreController', function ($http) {
    
    var appStore = this;
    
    var context = "/org/ogema/apps/app-installation/servlet";
    
    appStore.appStoreLocations;
    
    appStore.getAppStores = function() {
        
        $http.get(context + '/appstores').
            success(function(data, status, headers, config) {
              // this callback will be called asynchronously
              // when the response is available
              console.log(data);
              appStore.appStoreLocations = data;
            }).
            error(function(data, status, headers, config) {
              // called asynchronously if an error occurs
              // or server returns response with an error status.
              console.log(data);
              appStore.appStoreLocations = data;
            });
        
    };
    
});

