"use strict";
var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g = Object.create((typeof Iterator === "function" ? Iterator : Object).prototype);
    return g.next = verb(0), g["throw"] = verb(1), g["return"] = verb(2), typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (g && (g = 0, op[0] && (_ = 0)), _) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.applyCallbacks = exports.withLifecycleCallbacks = void 0;
/**
 * Extend a dataProvider to execute callbacks before and after read and write calls.
 *
 * @param {DataProvider} dataProvider The dataProvider to wrap
 * @param {ResourceCallbacks[]} handlers An array of ResourceCallbacks
 *
 * @typedef {Object} ResourceCallbacks
 * @property {string} resource The resource name
 * @property {AfterCreate} [afterCreate] A callback (or array of callbacks) executed after create
 * @property {AfterDelete} [afterDelete] A callback (or array of callbacks) executed after delete
 * @property {AfterDeleteMany} [afterDeleteMany] A callback (or array of callbacks) executed after deleteMany
 * @property {AfterGetList} [afterGetList] A callback (or array of callbacks) executed after getList
 * @property {AfterGetMany} [afterGetMany] A callback (or array of callbacks) executed after getMany
 * @property {AfterGetManyReference} [afterGetManyReference] A callback (or array of callbacks) executed after getManyReference
 * @property {AfterGetOne} [afterGetOne] A callback (or array of callbacks) executed after getOne
 * @property {AfterRead} [afterRead] A callback (or array of callbacks) executed after read (getList, getMany, getManyReference, getOne)
 * @property {AfterSave} [afterSave] A callback (or array of callbacks) executed after save (create, update, updateMany)
 * @property {AfterUpdate} [afterUpdate] A callback (or array of callbacks) executed after update
 * @property {AfterUpdateMany} [afterUpdateMany] A callback (or array of callbacks) executed after updateMany
 * @property {BeforeCreate} [beforeCreate] A callback (or array of callbacks) executed before create
 * @property {BeforeDelete} [beforeDelete] A callback (or array of callbacks) executed before delete
 * @property {BeforeDeleteMany} [beforeDeleteMany] A callback (or array of callbacks) executed before deleteMany
 * @property {BeforeGetList} [beforeGetList] A callback (or array of callbacks) executed before getList
 * @property {BeforeGetMany} [beforeGetMany] A callback (or array of callbacks) executed before getMany
 * @property {BeforeGetManyReference} [beforeGetManyReference] A callback (or array of callbacks) executed before getManyReference
 * @property {BeforeGetOne} [beforeGetOne] A callback (or array of callbacks) executed before getOne
 * @property {BeforeSave} [beforeSave] A callback (or array of callbacks) executed before save (create, update, updateMany)
 * @property {BeforeUpdate} [beforeUpdate] A callback (or array of callbacks) executed before update
 * @property {BeforeUpdateMany} [beforeUpdateMany] A callback (or array of callbacks) executed before updateMany
 *
 * Warnings:
 * - As queries issued in the callbacks are not done through react-query,
 *   any change in the data will not be automatically reflected in the UI.
 * - The callbacks are not executed in a transaction. In case of error,
 *   the backend may be left in an inconsistent state.
 * - When calling the API directly using fetch or another client,
 *   the callbacks will not be executed, leaving the backend in a possibly inconsistent state.
 * - If a callback triggers the query it's listening to, this will lead to a infinite loop.
 *
 * @example
 *
 * const dataProvider = withLifecycleCallbacks(
 *   jsonServerProvider("http://localhost:3000"),
 *   [
 *     {
 *       resource: "posts",
 *       afterRead: async (data, dataProvider, resource) => {
 *         // rename field to the record
 *         data.user_id = data.userId;
 *         return data;
 *       },
 *       // executed after create, update and updateMany
 *       afterSave: async (record, dataProvider, resource) => {
 *         // update the author's nb_posts
 *         const { total } = await dataProvider.getList("users", {
 *           filter: { id: record.user_id },
 *           pagination: { page: 1, perPage: 1 },
 *         });
 *         await dataProvider.update("users", {
 *           id: user.id,
 *           data: { nb_posts: total },
 *           previousData: user,
 *         });
 *         return record;
 *       },
 *       beforeDelete: async (params, dataProvider, resource) => {
 *         // delete all comments linked to the post
 *         const { data: comments } = await dataProvider.getManyReference(
 *           "comments",
 *           {
 *             target: "post_id",
 *             id: params.id,
 *           }
 *         );
 *         if (comments.length > 0) {
 *           await dataProvider.deleteMany("comments", {
 *             ids: comments.map((comment) => comment.id),
 *           });
 *         }
 *         // update the author's nb_posts
 *         const { data: post } = await dataProvider.getOne("posts", {
 *           id: params.id,
 *         });
 *         const { total } = await dataProvider.getList("users", {
 *           filter: { id: post.user_id },
 *           pagination: { page: 1, perPage: 1 },
 *         });
 *         await dataProvider.update("users", {
 *           id: user.id,
 *           data: { nb_posts: total - 1 },
 *           previousData: user,
 *         });
 *         return params;
 *       },
 *     },
 *   ]
 * );
 */
var withLifecycleCallbacks = function (dataProvider, handlers) {
    return __assign(__assign({}, dataProvider), { getList: function (resource, params) {
            return __awaiter(this, void 0, void 0, function () {
                var newParams, result, _a;
                return __generator(this, function (_b) {
                    switch (_b.label) {
                        case 0:
                            newParams = params;
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'beforeGetList',
                                    params: newParams,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 1:
                            newParams = _b.sent();
                            return [4 /*yield*/, dataProvider.getList(resource, newParams)];
                        case 2:
                            result = _b.sent();
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'afterGetList',
                                    params: result,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 3:
                            result = _b.sent();
                            _a = result;
                            return [4 /*yield*/, Promise.all(result.data.map(function (record) {
                                    return (0, exports.applyCallbacks)({
                                        name: 'afterRead',
                                        params: record,
                                        dataProvider: dataProvider,
                                        handlers: handlers,
                                        resource: resource,
                                    });
                                }))];
                        case 4:
                            _a.data = _b.sent();
                            return [2 /*return*/, result];
                    }
                });
            });
        }, getOne: function (resource, params) {
            return __awaiter(this, void 0, void 0, function () {
                var newParams, result, _a;
                return __generator(this, function (_b) {
                    switch (_b.label) {
                        case 0:
                            newParams = params;
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'beforeGetOne',
                                    params: newParams,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 1:
                            newParams = _b.sent();
                            return [4 /*yield*/, dataProvider.getOne(resource, newParams)];
                        case 2:
                            result = _b.sent();
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'afterGetOne',
                                    params: result,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 3:
                            result = _b.sent();
                            _a = result;
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'afterRead',
                                    params: result.data,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 4:
                            _a.data = _b.sent();
                            return [2 /*return*/, result];
                    }
                });
            });
        }, getMany: function (resource, params) {
            return __awaiter(this, void 0, void 0, function () {
                var newParams, result, _a;
                return __generator(this, function (_b) {
                    switch (_b.label) {
                        case 0:
                            newParams = params;
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'beforeGetMany',
                                    params: newParams,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 1:
                            newParams = _b.sent();
                            return [4 /*yield*/, dataProvider.getMany(resource, newParams)];
                        case 2:
                            result = _b.sent();
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'afterGetMany',
                                    params: result,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 3:
                            result = _b.sent();
                            _a = result;
                            return [4 /*yield*/, Promise.all(result.data.map(function (record) {
                                    return (0, exports.applyCallbacks)({
                                        name: 'afterRead',
                                        params: record,
                                        dataProvider: dataProvider,
                                        handlers: handlers,
                                        resource: resource,
                                    });
                                }))];
                        case 4:
                            _a.data = _b.sent();
                            return [2 /*return*/, result];
                    }
                });
            });
        }, getManyReference: function (resource, params) {
            return __awaiter(this, void 0, void 0, function () {
                var newParams, result, _a;
                return __generator(this, function (_b) {
                    switch (_b.label) {
                        case 0:
                            newParams = params;
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'beforeGetManyReference',
                                    params: newParams,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 1:
                            newParams = _b.sent();
                            return [4 /*yield*/, dataProvider.getManyReference(resource, newParams)];
                        case 2:
                            result = _b.sent();
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'afterGetManyReference',
                                    params: result,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 3:
                            result = _b.sent();
                            _a = result;
                            return [4 /*yield*/, Promise.all(result.data.map(function (record) {
                                    return (0, exports.applyCallbacks)({
                                        name: 'afterRead',
                                        params: record,
                                        dataProvider: dataProvider,
                                        handlers: handlers,
                                        resource: resource,
                                    });
                                }))];
                        case 4:
                            _a.data = _b.sent();
                            return [2 /*return*/, result];
                    }
                });
            });
        }, update: function (resource, params) {
            return __awaiter(this, void 0, void 0, function () {
                var newParams, _a, result, _b;
                return __generator(this, function (_c) {
                    switch (_c.label) {
                        case 0:
                            newParams = params;
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'beforeUpdate',
                                    params: newParams,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 1:
                            newParams = _c.sent();
                            _a = newParams;
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'beforeSave',
                                    params: newParams.data,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 2:
                            _a.data = _c.sent();
                            return [4 /*yield*/, dataProvider.update(resource, newParams)];
                        case 3:
                            result = _c.sent();
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'afterUpdate',
                                    params: result,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 4:
                            result = _c.sent();
                            _b = result;
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'afterSave',
                                    params: result.data,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 5:
                            _b.data = _c.sent();
                            return [2 /*return*/, result];
                    }
                });
            });
        }, create: function (resource, params) {
            return __awaiter(this, void 0, void 0, function () {
                var newParams, _a, result, _b;
                return __generator(this, function (_c) {
                    switch (_c.label) {
                        case 0:
                            newParams = params;
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'beforeCreate',
                                    params: newParams,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 1:
                            newParams = _c.sent();
                            _a = newParams;
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'beforeSave',
                                    params: newParams.data,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 2:
                            _a.data = _c.sent();
                            return [4 /*yield*/, dataProvider.create(resource, newParams)];
                        case 3:
                            result = _c.sent();
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'afterCreate',
                                    params: result,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 4:
                            result = _c.sent();
                            _b = result;
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'afterSave',
                                    params: result.data,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 5:
                            _b.data = _c.sent();
                            return [2 /*return*/, result];
                    }
                });
            });
        }, delete: function (resource, params) {
            return __awaiter(this, void 0, void 0, function () {
                var newParams, result;
                return __generator(this, function (_a) {
                    switch (_a.label) {
                        case 0:
                            newParams = params;
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'beforeDelete',
                                    params: newParams,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 1:
                            newParams = _a.sent();
                            return [4 /*yield*/, dataProvider.delete(resource, newParams)];
                        case 2:
                            result = _a.sent();
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'afterDelete',
                                    params: result,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 3:
                            result = _a.sent();
                            return [2 /*return*/, result];
                    }
                });
            });
        }, updateMany: function (resource, params) {
            return __awaiter(this, void 0, void 0, function () {
                var newParams, _a, result, afterSaveHandlers, records;
                return __generator(this, function (_b) {
                    switch (_b.label) {
                        case 0:
                            newParams = params;
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'beforeUpdateMany',
                                    params: newParams,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 1:
                            newParams = _b.sent();
                            _a = newParams;
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'beforeSave',
                                    params: newParams.data,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 2:
                            _a.data = _b.sent();
                            return [4 /*yield*/, dataProvider.updateMany(resource, newParams)];
                        case 3:
                            result = _b.sent();
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'afterUpdateMany',
                                    params: result,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 4:
                            result = _b.sent();
                            afterSaveHandlers = handlers.filter(function (h) {
                                return (h.resource === resource || h.resource === '*') &&
                                    h.afterSave;
                            });
                            if (!(afterSaveHandlers.length > 0)) return [3 /*break*/, 7];
                            return [4 /*yield*/, dataProvider.getMany(resource, {
                                    //@ts-ignore
                                    ids: result.data,
                                })];
                        case 5:
                            records = (_b.sent()).data;
                            return [4 /*yield*/, Promise.all(records.map(function (record) {
                                    return (0, exports.applyCallbacks)({
                                        name: 'afterSave',
                                        params: record,
                                        dataProvider: dataProvider,
                                        handlers: handlers,
                                        resource: resource,
                                    });
                                }))];
                        case 6:
                            _b.sent();
                            _b.label = 7;
                        case 7: return [2 /*return*/, result];
                    }
                });
            });
        }, deleteMany: function (resource, params) {
            return __awaiter(this, void 0, void 0, function () {
                var newParams, result;
                return __generator(this, function (_a) {
                    switch (_a.label) {
                        case 0:
                            newParams = params;
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'beforeDeleteMany',
                                    params: newParams,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 1:
                            newParams = _a.sent();
                            return [4 /*yield*/, dataProvider.deleteMany(resource, newParams)];
                        case 2:
                            result = _a.sent();
                            return [4 /*yield*/, (0, exports.applyCallbacks)({
                                    name: 'afterDeleteMany',
                                    params: result,
                                    dataProvider: dataProvider,
                                    handlers: handlers,
                                    resource: resource,
                                })];
                        case 3:
                            result = _a.sent();
                            return [2 /*return*/, result];
                    }
                });
            });
        } });
};
exports.withLifecycleCallbacks = withLifecycleCallbacks;
/**
 * Apply callbacks to the params for the given resource and hook
 * @param {DataProvider} dataProvider The dataProvider
 * @param {ResourceCallbacks[]} handlers An array of ResourceCallbacks
 * @param {string} resource The resource name
 * @param {string} hook The hook name (beforeGetList, afterGetOne, etc.)
 * @param {U} params The params / result to pass to the callbacks
 * @returns {Promise<U>} The params / result after the callbacks have been applied
 */
var applyCallbacks = function (_a) {
    return __awaiter(this, arguments, void 0, function (_b) {
        var newParams, handlersToApply, _i, handlersToApply_1, handler, callbacksValue, _c, _d, callback;
        var name = _b.name, params = _b.params, dataProvider = _b.dataProvider, handlers = _b.handlers, resource = _b.resource;
        return __generator(this, function (_e) {
            switch (_e.label) {
                case 0:
                    newParams = params;
                    handlersToApply = handlers.filter(function (h) { return (h.resource === resource || h.resource === '*') && h[name]; });
                    _i = 0, handlersToApply_1 = handlersToApply;
                    _e.label = 1;
                case 1:
                    if (!(_i < handlersToApply_1.length)) return [3 /*break*/, 9];
                    handler = handlersToApply_1[_i];
                    callbacksValue = handler[name];
                    if (!Array.isArray(callbacksValue)) return [3 /*break*/, 6];
                    _c = 0, _d = callbacksValue !== null && callbacksValue !== void 0 ? callbacksValue : [];
                    _e.label = 2;
                case 2:
                    if (!(_c < _d.length)) return [3 /*break*/, 5];
                    callback = _d[_c];
                    return [4 /*yield*/, callback(newParams, dataProvider, resource)];
                case 3:
                    newParams = _e.sent();
                    _e.label = 4;
                case 4:
                    _c++;
                    return [3 /*break*/, 2];
                case 5: return [3 /*break*/, 8];
                case 6: return [4 /*yield*/, callbacksValue(newParams, dataProvider, resource)];
                case 7:
                    newParams = _e.sent();
                    _e.label = 8;
                case 8:
                    _i++;
                    return [3 /*break*/, 1];
                case 9: return [2 /*return*/, newParams];
            }
        });
    });
};
exports.applyCallbacks = applyCallbacks;
//# sourceMappingURL=withLifecycleCallbacks.js.map