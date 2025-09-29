var RA_STORE = 'RaStore';
// localStorage isn't available in incognito mode. We need to detect it
var testLocalStorage = function () {
    // eslint-disable-next-line eqeqeq
    if (typeof window === 'undefined' || window.localStorage == undefined) {
        return false;
    }
    try {
        window.localStorage.setItem('test', 'test');
        window.localStorage.removeItem('test');
        return true;
    }
    catch (e) {
        return false;
    }
};
var localStorageAvailable = testLocalStorage();
/**
 * Store using localStorage, or memory storage in incognito mode
 *
 * @example
 *
 * import { localStorageStore } from 'react-admin';
 *
 * const App = () => (
 *    <Admin store={localStorageStore()}>
 *       ...
 *   </Admin>
 * );
 */
export var localStorageStore = function (version, appKey) {
    if (version === void 0) { version = '1'; }
    if (appKey === void 0) { appKey = ''; }
    var prefix = "".concat(RA_STORE).concat(appKey);
    var prefixLength = prefix.length;
    var subscriptions = {};
    var publish = function (key, value) {
        Object.keys(subscriptions).forEach(function (id) {
            if (!subscriptions[id])
                return; // may happen if a component unmounts after a first subscriber was notified
            if (subscriptions[id].key === key) {
                subscriptions[id].callback(value);
            }
        });
    };
    // Whenever the local storage changes in another document, look for matching subscribers.
    // This allows to synchronize state across tabs
    var onLocalStorageChange = function (event) {
        var _a;
        if (((_a = event.key) === null || _a === void 0 ? void 0 : _a.substring(0, prefixLength)) !== prefix) {
            return;
        }
        var key = event.key.substring(prefixLength + 1);
        var value = event.newValue ? tryParse(event.newValue) : undefined;
        Object.keys(subscriptions).forEach(function (id) {
            if (!subscriptions[id])
                return; // may happen if a component unmounts after a first subscriber was notified
            if (subscriptions[id].key === key) {
                if (value === null) {
                    // an event with a null value is sent when the key is deleted.
                    // to enable default value, we need to call setValue(undefined) instead of setValue(null)
                    subscriptions[id].callback(undefined);
                }
                else {
                    subscriptions[id].callback(value == null ? undefined : value);
                }
            }
        });
    };
    return {
        setup: function () {
            if (localStorageAvailable) {
                var storedVersion = getStorage().getItem("".concat(prefix, ".version"));
                if (storedVersion && storedVersion !== version) {
                    var storage_1 = getStorage();
                    Object.keys(storage_1).forEach(function (key) {
                        if (key.startsWith(prefix)) {
                            storage_1.removeItem(key);
                        }
                    });
                }
                getStorage().setItem("".concat(prefix, ".version"), version);
                window.addEventListener('storage', onLocalStorageChange);
            }
        },
        teardown: function () {
            if (localStorageAvailable) {
                window.removeEventListener('storage', onLocalStorageChange);
            }
        },
        getItem: function (key, defaultValue) {
            var valueFromStorage = getStorage().getItem("".concat(prefix, ".").concat(key));
            return valueFromStorage == null
                ? defaultValue
                : tryParse(valueFromStorage);
        },
        setItem: function (key, value) {
            if (value === undefined) {
                getStorage().removeItem("".concat(prefix, ".").concat(key));
            }
            else {
                getStorage().setItem("".concat(prefix, ".").concat(key), JSON.stringify(value));
            }
            publish(key, value);
        },
        removeItem: function (key) {
            getStorage().removeItem("".concat(prefix, ".").concat(key));
            publish(key, undefined);
        },
        removeItems: function (keyPrefix) {
            var storage = getStorage();
            Object.keys(storage).forEach(function (key) {
                if (key.startsWith("".concat(prefix, ".").concat(keyPrefix))) {
                    storage.removeItem(key);
                    var publishKey = key.substring(prefixLength + 1);
                    publish(publishKey, undefined);
                }
            });
        },
        reset: function () {
            var storage = getStorage();
            Object.keys(storage).forEach(function (key) {
                if (key.startsWith(prefix)) {
                    storage.removeItem(key);
                    var publishKey = key.substring(prefixLength + 1);
                    publish(publishKey, undefined);
                }
            });
        },
        subscribe: function (key, callback) {
            var id = Math.random().toString();
            subscriptions[id] = {
                key: key,
                callback: callback,
            };
            return function () {
                delete subscriptions[id];
            };
        },
    };
};
var tryParse = function (value) {
    try {
        return JSON.parse(value);
    }
    catch (e) {
        return value;
    }
};
var LocalStorageShim = /** @class */ (function () {
    function LocalStorageShim() {
        this.valuesMap = new Map();
    }
    LocalStorageShim.prototype.getItem = function (key) {
        if (this.valuesMap.has(key)) {
            return String(this.valuesMap.get(key));
        }
        return null;
    };
    LocalStorageShim.prototype.setItem = function (key, value) {
        this.valuesMap.set(key, value);
    };
    LocalStorageShim.prototype.removeItem = function (key) {
        this.valuesMap.delete(key);
    };
    LocalStorageShim.prototype.removeItems = function (keyPrefix) {
        var _this = this;
        this.valuesMap.forEach(function (value, key) {
            if (key.startsWith(keyPrefix)) {
                _this.valuesMap.delete(key);
            }
        });
    };
    LocalStorageShim.prototype.clear = function () {
        this.valuesMap.clear();
    };
    LocalStorageShim.prototype.key = function (i) {
        if (arguments.length === 0) {
            throw new TypeError("Failed to execute 'key' on 'Storage': 1 argument required, but only 0 present."); // this is a TypeError implemented on Chrome, Firefox throws Not enough arguments to Storage.key.
        }
        var arr = Array.from(this.valuesMap.keys());
        return arr[i];
    };
    Object.defineProperty(LocalStorageShim.prototype, "length", {
        get: function () {
            return this.valuesMap.size;
        },
        enumerable: false,
        configurable: true
    });
    return LocalStorageShim;
}());
var memoryStorage = new LocalStorageShim();
export var getStorage = function () {
    return localStorageAvailable ? window.localStorage : memoryStorage;
};
//# sourceMappingURL=localStorageStore.js.map