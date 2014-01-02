angular.module('devices', [ 'ubiqesh' ]).controller('DevicesCtrl', [ '$scope', '$timeout', 'angularUbiqesh', 'angularUbiqeshCollection', 'angularUbiqeshQuery', function($scope, $timeout, angularUbiqesh, angularUbiqeshCollection,angularUbiqeshQuery) {
	var rr = new Ubiqesh('http://localhost:8080/api/1');
	$scope.devices = angularUbiqeshCollection(rr);
} ]);