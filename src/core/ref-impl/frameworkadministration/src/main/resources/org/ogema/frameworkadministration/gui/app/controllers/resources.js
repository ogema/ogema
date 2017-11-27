'use strict';
/* Controllers */

ngOGFrAdminApp.controller('ResourcesCtrl', ['$scope', 'ogemaGateway', '$interval', '$rootScope', '$filter', function ($scope, ogemaGateway, $interval, $rootScope, $filter) {
        $scope.restUser = "rest";
        $scope.restPwd = "rest";
        $scope.restPath = "/rest/resources/";// "http://192.168.0.14:8080/rest/resources/"; "http://10.45.20.127:8080/rest/resources/"
        $scope.tableData = [];

        $scope.gridOptions = {data: 'tableData',
            enableCellSelection: true,
            enableRowSelection: false,
            enableCellEditOnFocus: true,
            selectedItems: $scope.gridSelections,
            multiSelect: false,
            sortInfo: {fields: ['name', 'value'], directions: ['asc', 'desc']},
            beforeSelectionChange: function (rowItem, event) {
                return $scope.checkSelectedCell(rowItem, event);
            },
            columnDefs: [{
                    field: 'name',
                    displayName: 'Name',
                    enableCellEdit: false
                }, {
                    field: 'value',
                    displayName: 'Wert',
                    enableCellEdit: true,
                    cellEditableCondition: "row.entity.name === 'active' || row.entity.name === 'value'"
                }
            ]
        };

        if ($scope.treeData == undefined) {
            $scope.treeData = [];
        }

        $scope.treeOptions = {
            nodeChildren: "subresources",
            dirSelectable: true,
            injectClasses: {
                ul: "a1",
                li: "a2",
                liSelected: "a7",
                iExpanded: "a3",
                iCollapsed: "a4",
                iLeaf: "a5",
                label: "a6",
                labelSelected: "a8"
            }
        };

        $scope.showSelected = function (sel) {
            var tabWidth = 4;
            var tableData = [];
            $scope.jsonData = JSON.stringify(sel, null, tabWidth);
            $scope.selectedTreeElement = sel;
            //  console.log("$rootScope: ", $rootScope);
            angular.forEach(sel, function (element, key) {
                //      console.log("element, key: ", element, key)

                switch (key) {
                    case "$$hashKey":
                        // Do nothing -> remove $$hashKey 
                        break;
                    case "subresources":
                        // Do nothing -> remove subresources 
                        break;
                    default:
                        tableData.push({name: key, value: element});
                        break;
                }

            });
            $scope.tableData = tableData;
            //    console.log("$scope.tableData: ", $scope.tableData);
        };

        $scope.checkSelectedCell = function (rowItem, event) {
            var check = false;

            //    console.log("checkSelectedCell(rowItem, event, $scope.selectedTreeElement): \n", rowItem, event, $scope.selectedTreeElement);
            //    console.log(rowItem);
            switch (rowItem.entity.name) {
                case "referencing":
                    $scope.gridOptions.selectRow(rowItem.rowIndex, false);
                    break;
                case "decorating":

                    break;
                    // case "path":
                    //     console.log("PATH");
                    // break;

                default:
                    check = true;
                    break;
            }
            return check;
        };

        $scope.$watch('searchText', function (newValue, oldValue) {

            $scope.treeData = $filter('filter')($scope.treeDataTotal, {name: $scope.searchText});

        });

        $scope.$watch('restPath', function (newValue, oldValue) {

            $scope.getAllResources();

        });

        $scope.updateResource = function (v1, v2, v3) {
            console.log(v1);
            console.log(v2);
            console.log(v3);
            console.log(JSON.stringify(v2));
            let msg = {};
            let path = "";
            let resType = "";
            for (let o of v2) {
                console.log(o);
                msg[o.name] = o.value;
                if (o.name === 'path') {
                    path = o.value;
                }
                if (o.name === 'type') {
                    resType = o.value;
                }
            }
            //FIXME need @type attribute...
            msg["@type"] = "FloatResource";
            if (resType.indexOf("StringResource") !== -1) {
                msg["@type"] = "StringResource";
            } else if (resType.indexOf("TimeResource") !== -1) {
                msg["@type"] = "TimeResource";
            } else if (resType.indexOf("IntegerResource") !== -1) {
                msg["@type"] = "IntegerResource";
            }
            console.log(JSON.stringify(msg));
            console.log(path);
            let url = $scope.restPath + path + "?user=" + encodeURIComponent($scope.restUser) + "&pw=" + encodeURIComponent($scope.restPwd);
            console.log(url);
            $.ajax({
                type: "PUT",
                url: $scope.restPath + path + "?user=" + encodeURIComponent($scope.restUser) + "&pw=" + encodeURIComponent($scope.restPwd),
                contentType: "application/json",
                data: JSON.stringify(msg)
            });
            $scope.loadNode(path, true);
        };

        $scope.loadNode = function (path, expanded, parent) {
            if (!expanded) {
                return;
            }
            console.log("loading %s", path);
            var url = $scope.restPath + path + "?user=" + encodeURIComponent($scope.restUser) + "&pw=" + encodeURIComponent($scope.restPwd);
            ogemaGateway.getJSON(url, {"depth": 2}).then(function (result) {
                $scope.resources = result;

                result = $scope.unifyResult(result);
                //console.log("result: ", result);
                var currentNode = $scope.treeData;
                path.split('/').forEach(resourceName => {
                    //console.log("next=" + field);
                    let subResIdx = currentNode.subresources.findIndex(it => {
                        return it.name === resourceName;
                    });
                    let sub = currentNode.subresources[subResIdx];
                    if (sub.path === path) {
                        if (typeof parent !== 'undefined') {
                            parent.subresources[subResIdx] = result;
                        }
                        currentNode.subresources[subResIdx] = result;
                        let expandedIdx = $scope.expandedNodes.findIndex(it => {
                            return it.path === path;
                        });
                        if (expandedIdx === -1) {
                            $scope.expandedNodes.push(result);
                        } else {
                            $scope.expandedNodes[expandedIdx] = result;
                        }
                        /*
                         console.log("%d expanded nodes", $scope.expandedNodes.length);
                         for (let exp of $scope.expandedNodes) {
                         console.log("expanded: %s", exp.path);
                         }
                         */
                    }
                    currentNode = sub;
                });
            }, function (error) {
                console.log("ERROR: ", error);
            });

            return true;
        };

        $scope.getAllResources = function () {
            //console.log($scope.expandedNodes);
            $scope.searchText = "";
            var path = $scope.restPath + "?user=" + encodeURIComponent($scope.restUser) + "&pw=" + encodeURIComponent($scope.restPwd);
            ogemaGateway.getJSON(path, {"depth": 1}).then(function (result) {
                $scope.resources = result;
                //console.log("$scope.resources: ", $scope.resources);

                $scope.result = result;

                $scope.treeDataTotal = $scope.unifyResult(result);
                $scope.treeData = $scope.treeDataTotal;

                //  console.log("getAllResources:", $scope.treeDataTotal)
                if (typeof $scope.expandedNodes !== 'undefined') {
                    for (let node of $scope.expandedNodes) {
                        $scope.loadNode(node.path, true);
                    }
                }
            }, function (error) {
                console.log("ERROR: ", error);
            });
        };

        $scope.unifyResult = function (obj) {

            //console.log("unifyResult: ",obj);
            angular.forEach(obj, function (value, key) {
                if (typeof value === "object" && value !== null)
                {
                    switch (key) {
                        case "subresources":
                            if (value.length > 0) {
                                angular.forEach(value, function (val, ky) {
                                    angular.forEach(val, function (v, k) {
                                        value[ky] = v;
                                    });
                                });
                            }
                            ;
                            break;
                        case "resourcelink":
                            //   console.log("resourcelink: ", value);
                            value.name = "Resourcelink";
                            value.value = value.resourcelink.name;
                            break;
                    }
                    $scope.unifyResult(value);
                } else {
                    // do something... 
                }
            });
            return obj;
        };
        $scope.getAllResources();
    }]);

ngOGFrAdminApp.controller('tableView', ['$scope', function ($scope) {

    }]);



function createSubTree(level, width, prefix) {
    if (level > 0) {
        var res = [];
        for (var i = 1; i <= width; i++)
            res.push({"label": "Node " + prefix + i, "id": "id" + prefix + i, "children": createSubTree(level - 1, width, prefix + i + ".")});
        return res;
    } else
        return [];
}
