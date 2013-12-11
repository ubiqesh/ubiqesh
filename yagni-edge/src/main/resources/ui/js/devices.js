angular.module('devices', [ 'yagni' ]).controller('DevicesCtrl', [ '$scope', '$timeout', 'angularYagni', 'angularYagniCollection', 'angularYagniQuery', function($scope, $timeout, angularYagni, angularYagniCollection,angularYagniQuery) {
	var rr = new Yagni('http://localhost:8080/api/1');
	$scope.devices = angularYagniCollection(rr);
} ]);