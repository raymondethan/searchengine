var searchApp = angular.module('searchApp', []);

searchApp.controller('SearchController', function ($scope) {
    $scope.results = [
        {'name': 'Foo',
            'snippet': 'Bar'},
        {'name': 'Hello',
            'snippet': 'World'},
    ];
});