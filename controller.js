var searchApp = angular.module('searchApp', []);

searchApp.controller('SearchController', function ($scope, $http) {
    $scope.query = '';
    $scope.results = [
    ];
    $scope.search = function() {
        $http.get('/search/' + this.query).success(function(data) {
            $scope.results = data;
        });
    }
});