String.prototype.endsWith = function (suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

var matchPaths = function (path1, path2) {
    var workPath1 = path1.endsWith("/") ? path1.substring(0, path1.length - 1) : path1;
    var workPath2 = path2.endsWith("/") ? path2.substring(0, path2.length - 1) : path2;
    return workPath1 === workPath2;
};

var stringify = function (obj, prop) {
    var placeholder = '____PLACEHOLDER____';
    var fns = [];
    var json = JSON.stringify(obj, function (key, value) {
        if (typeof value === 'function') {
            fns.push(value);
            return placeholder;
        }
        return value;
    }, 2);
    json = json.replace(new RegExp('"' + placeholder + '"', 'g'), function (_) {
        return fns.shift();
    });
    return json;
};

var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() {
        this.constructor = d;
    }

    __.prototype = b.prototype;
    d.prototype = new __();
};

(function (window) {
    var Snapshot = (function () {
        function Snapshot(message) {
            this.nodePayload = message.payload;
            this.nodeName = message.name;
            this.nodePath = message.path;
            this.nodeParent = message.parent;
            this.nodeHasChildren = message.hasChildren;
            this.nodeNumChildren = message.numChildren;
            this.nodePriority = message.priority;
        }

        Snapshot.prototype.val = function () {
            return this.nodePayload;
        };
        Snapshot.prototype.name = function () {
            return this.nodeName;
        };
        Snapshot.prototype.parent = function () {
            return this.nodeParent;
        };
        Snapshot.prototype.path = function () {
            return this.nodePath;
        };
        Snapshot.prototype.ref = function () {
            return new Ubiqesh(this.nodePath + "/" + this.nodeName);
        };
        Snapshot.prototype.child = function (childPath) {
            new Ubiqesh(this.nodePath + "/" + this.nodeName + "/" + childPath);
        };
        Snapshot.prototype.forEach = function (childAction) {
            var dataRef = new Ubiqesh(this.nodePath);
            dataRef.on("child_added", childAction);
        };
        Snapshot.prototype.hasChildren = function () {
            return this.nodeHasChildren;
        };
        Snapshot.prototype.numChildren = function () {
            return this.nodeNumChildren;
        };
        Snapshot.prototype.getPriority = function () {
            return this.nodePriority;
        };
        return Snapshot;
    })();

    var UbiqeshRPC = (function (_super) {
        __extends(UbiqeshRPC, _super);
        function UbiqeshRPC(uri) {
            _super.call(this, uri);
            _super.prototype.sendRpc.call(this, 'init', {
                path: uri
            });
        }

        UbiqeshRPC.prototype.attachListener = function (path, event_type) {
            _super.prototype.sendRpc.call(this, 'attachListener', {
                path: path,
                event_type: event_type
            });
        };
        UbiqeshRPC.prototype.detachListener = function (path, event_type) {
            _super.prototype.sendRpc.call(this, 'detachListener', {
                path: path,
                event_type: event_type
            });
        };
        UbiqeshRPC.prototype.attachQuery = function (path, query) {
            _super.prototype.sendRpc.call(this, 'attachQuery', {
                path: path,
                "query": stringify(query)
            });
        };
        UbiqeshRPC.prototype.detachQuery = function (path, query) {
            _super.prototype.sendRpc.call(this, 'detachQuery', {
                path: path,
                "query": stringify(query)
            });
        };
        UbiqeshRPC.prototype.send = function (path, data) {
            _super.prototype.sendRpc.call(this, 'event', {
                path: path,
                data: data
            });
        };
        UbiqeshRPC.prototype.push = function (path, name, data) {
            _super.prototype.sendRpc.call(this, 'push', {
                path: path,
                name: name,
                data: data
            });
        };
        UbiqeshRPC.prototype.set = function (path, data, priority) {
            _super.prototype.sendRpc.call(this, 'set', {
                path: path,
                data: data,
                priority: priority
            });
        };
        UbiqeshRPC.prototype.update = function (path, data) {
            _super.prototype.sendRpc.call(this, 'update', {
                path: path,
                data: data
            });
        };
        UbiqeshRPC.prototype.remove = function (path) {
            _super.prototype.sendRpc.call(this, 'remove', {
                path: path
            });
        };
        UbiqeshRPC.prototype.setPriority = function (path, priority) {
            _super.prototype.sendRpc.call(this, 'setPriority', {
                path: path,
                priority: priority
            });
        };
        UbiqeshRPC.prototype.pushOnDisconnect = function (path, name, payload) {
            _super.prototype.sendRpc.call(this, 'pushOnDisconnect', {
                path: path,
                name: name,
                payload: payload
            });
        };
        UbiqeshRPC.prototype.setOnDisconnect = function (path, data, priority) {
            _super.prototype.sendRpc.call(this, 'setOnDisconnect', {
                path: path,
                data: data,
                priority: priority
            });
        };
        UbiqeshRPC.prototype.updateOnDisconnect = function (path, data) {
            _super.prototype.sendRpc.call(this, 'updateOnDisconnect', {
                path: path,
                data: data
            });
        };
        UbiqeshRPC.prototype.removeOnDisconnect = function (path) {
            _super.prototype.sendRpc.call(this, 'removeOnDisconnect', {
                path: path
            });
        };

        return UbiqeshRPC;
    })(RPC);

    var UbiqeshOnDisconnect = (function () {
        function UbiqeshOnDisconnect(path, con) {
            this.con = con;
            this.path = path;
        }

        UbiqeshOnDisconnect.prototype.push = function (payload) {
            var name = UUID.generate();
            this.con.pushOnDisconnect(this.path, name, payload);
        };

        UbiqeshOnDisconnect.prototype.set = function (payload) {
            this.con.setOnDisconnect(this.path, payload);
        };

        UbiqeshOnDisconnect.prototype.update = function (payload) {
            this.con.updateOnDisconnect(this.path, payload);
        };

        UbiqeshOnDisconnect.prototype.setWithPriority = function (payload, priority) {
            this.con.setOnDisconnect(this.path, payload, priority);
        };
        UbiqeshOnDisconnect.prototype.remove = function () {
            this.con.removeOnDisconnect(this.path);
        };
        return UbiqeshOnDisconnect;
    })();

    var UbiqeshOnlineSwitch = (function () {
        function UbiqeshOnlineSwitch(path, connectionHandler) {
            var con = new UbiqeshConnection(path);
            con.handleMessage = function (message) {

            };
            con.connectionHandler(connectionHandler);
        }

        return UbiqeshOnlineSwitch;
    })();

    var Ubiqesh = (function () {
        function Ubiqesh(path) {
            var self = this;
            this.events = {};
            this.events_once = {};
            this.path = path;
            this.rootPath;
            this.parentPath;
            this.rpc = new UbiqeshRPC(path);
            this.rpc.addMessageHandler(function (message) {
                if (self.events[message.type] != null) {
                    var snapshot = new Snapshot(message);
                    var workpath = self.path;
                    // if (workpath.indexOf(snapshot.path()) == 0) {
                    if (matchPaths(workpath, snapshot.path())) {
                        var callback = self.events[message.type];
                        if (callback != null) {
                            callback(snapshot, message.prevChildName);
                        }

                        var callback = self.events_once[message.type];
                        if (callback != null) {
                            callback(snapshot, message.prevChildName);
                            self.events_once[message.type] = null;
                        }
                    }
                }
            });
        }

        Ubiqesh.prototype.child = function (childname) {
            return new Ubiqesh(this.path + "/" + childname);
        };
        Ubiqesh.prototype.on = function (event_type, callback) {
            this.events[event_type] = callback;
            this.rpc.attachListener(this.path, event_type);
        };
        Ubiqesh.prototype.once = function (event_type, callback) {
            this.events_once[event_type] = callback;
        };
        Ubiqesh.prototype.off = function (event_type, callback) {
            this.events[event_type] = null;
            this.events_once[event_type] = null;
            this.rpc.detachListener(this.path, event_type);
        };
        Ubiqesh.prototype.query = function (query, child_added, child_changed, child_removed) {
            this.events['query_child_added'] = child_added;
            this.events['query_child_changed'] = child_changed;
            this.events['query_child_removed'] = child_removed;
            this.rpc.attachQuery(this.path, stringify(query));
        };
        Ubiqesh.prototype.remove_query = function (query) {
            this.rpc.detachQuery(this.path, stringify(query));
        };
        Ubiqesh.prototype.send = function (data) {
            this.rpc.send(this.path, data);
        };
        Ubiqesh.prototype.push = function (data) {
            var name = UUID.generate();
            this.rpc.push(this.path, name, data);
            return new Ubiqesh(this.path + "/" + name);
        };
        Ubiqesh.prototype.set = function (data) {
            this.rpc.set(this.path, data);
            if (data != null) {
                return new Ubiqesh(this.path);
            } else {
                return null;
            }
        };
        Ubiqesh.prototype.update = function (content) {
            this.rpc.update(this.path, data);
            if (content != null) {
                return new Ubiqesh(this.path);
            } else {
                return null;
            }
        };
        Ubiqesh.prototype.setWithPriority = function (data, priority) {
            this.rpc.set(this.path, data, priority);
            if (data != null) {
                return new Ubiqesh(this.path);
            } else {
                return null;
            }
        };
        Ubiqesh.prototype.remove = function () {
            this.rpc.remove(this.path);
        };
        Ubiqesh.prototype.setPriority = function (priority) {
            this.rpc.setPriority(this.path, priority);
            return new Ubiqesh(this.path);
        };
        Ubiqesh.prototype.parent = function () {
            new Ubiqesh(this.parentPath);
        };

        Ubiqesh.prototype.root = function () {
            new Ubiqesh(this.rootPath);
        };
        Ubiqesh.prototype.name = function () {
            var name = this.path.substring(this.path.lastIndexOf('/') + 1, this.path.length);
            return name;
        };
        Ubiqesh.prototype.ref = function () {
            return this;
        };
        Ubiqesh.prototype.onDisconnect = function () {
            return new UbiqeshOnDisconnect(this.path, this.rpc);
        };
        return Ubiqesh;
    })();

    window.Ubiqesh = Ubiqesh;
})(window);
