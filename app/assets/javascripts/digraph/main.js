define(['angular', 'cytoscape'], function (angular, cytoscape) {
    'use strict';

    var module = angular.module('nodes.digraph', []);

    // use a factory instead of a directive, because cy.js is not just for visualisation; you need access to the graph model and events etc
    module.factory('nodeGraph', ['$q', function ($q) {
        return function () {
            var deferred = $q.defer();

            $(function () { // on dom ready

                var cy = cytoscape({
                    container: $('#cy')[0],

                    style: cytoscape.stylesheet()
                        .selector('node')
                        .css({
                            'content': 'data(id)',
                            'height': 80,
                            'width': 80,
                            'text-valign': 'center',
                            'color': 'white',
                            'text-outline-width': 2,
                            'text-outline-color': '#888'
                        })
                        .selector('edge')
                        .css({
                            'target-arrow-shape': 'triangle'
                        })
                        .selector('.unknown')
                        .css({
                            'background-color': 'yellow',
                            'line-color': 'yellow',
                            'target-arrow-color': 'yellow',
                            'source-arrow-color': 'yellow'
                        })
                        .selector('.stopped')
                        .css({
                            'background-color': 'red',
                            'line-color': 'red',
                            'target-arrow-color': 'red',
                            'source-arrow-color': 'red'
                        })
                        .selector('.running')
                        .css({
                            'background-color': 'green',
                            'line-color': 'green',
                            'target-arrow-color': 'green',
                            'source-arrow-color': 'green'
                        })
                        .selector(':selected')
                        .css({
                            'background-color': 'black',
                            'line-color': 'black',
                            'target-arrow-color': 'black',
                            'source-arrow-color': 'black',
                            'text-outline-color': 'black'
                        }),

                    layout: {
                        name: 'breadthfirst',
                        directed: true,
                        padding: 10
                    },

                    elements: [],

                    ready: function () {
                        deferred.resolve(this);
                    }
                });

            }); // on dom ready

            return deferred.promise;
        };
    }]);

    module.controller('NodesCtrl', ['$scope', 'nodeGraph', function ($scope, nodeGraph) {

        nodeGraph().then(function (cy) {

            function setState(elem, stateClass) {
                elem.removeClass('running');
                elem.removeClass('stopped');
                elem.removeClass('unknown');
                elem.addClass(stateClass);
            }

            function setNodeState(nodeState) {
                //console.info('setNodeState', nodeState);
                //console.info('Setting node ' + nodeState.id + ' to ' + nodeState.state);
                var node = cy.getElementById(nodeState.id);
                var neighbourhood = node.connectedEdges().add(node);
                setState(neighbourhood, nodeState.state.toLowerCase());
            }

            function addNodes(nodes) {
                var toAdd = {
                    nodes: [],
                    edges: []
                };

                nodes.forEach(function (node) {
                    toAdd.nodes.push({
                        data: {id: node.id}
                    });
                    node.depends.forEach(function (targetId) {
                        toAdd.edges.push({
                            data: {
                                source: node.id,
                                target: targetId
                            }
                        });
                    });
                });
                cy.add(toAdd);
                cy.layout();
                //cy.fit();
            }

            function changeNodes(nodeStates) {
                nodeStates.forEach(function (nodeState) {
                    setNodeState(nodeState);
                });
            }


            var ws = new WebSocket('ws://' + document.location.host + '/websocket');

            ws.onmessage = function (evt) {
                var data = JSON.parse(evt.data);
                //console.info('WebSocket message: ', data);
                switch (data.type) {
                    case 'add':
                        addNodes(data.nodes);
                        break;
                    case 'change':
                        changeNodes(data.changes);
                        break;
                    default:
                        console.error('Unknown message type: "' + data.type + '"');
                        break;
                }
            };

            ws.onerror = function (evt) {
                console.error("WebSocket error: " + evt.data);
            };

            ws.onopen = function (evt) {
                console.info("Websocket connected");
            };

            ws.onclose = function (evt) {
                console.info("WebSocket disconnected");
            };

            $scope.ws = ws;
        });
    }]);

});
